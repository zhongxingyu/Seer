 /*
  * File:         LogEventListBuilder.java
  * Project:		 Spiderweb Network Graph Visualizer
  * Created:      01/06/2011
  * Last Changed: Date: 26/07/2011 
  * Author:       <A HREF="mailto:smith_matthew@live.com">Matthew Smith</A>
  * 
  * This code was produced at Carleton University 2011
  */
 package spiderweb.graph.savingandloading;
 
 import spiderweb.graph.*;
 import spiderweb.graph.LogEvent;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 
 /**
  * The LogEventListBuilder class is used for loading graphs from a text(.txt) file.
  * It is used by the P2PNetworkGraphLoader to load graphs from text files.
  * 
  * @author <A HREF="mailto:smith_matthew@live.com">Matthew Smith</A>
  * @version Date: 21/07/2011 
  */
 public class LogEventListBuilder extends ProgressAdapter {
 	
 	//[start] private variables
 	private P2PNetworkGraph fullGraph;
 	private LinkedList<LogEvent> logEvents;
 	//[end] private variables
 	
 	//[start] Constructor and listener initializer
 	public LogEventListBuilder(P2PNetworkGraph fullGraph) {
 		super();
 		this.fullGraph = fullGraph;
 	}
 	
 	public LogEventListBuilder() {
 		this(new P2PNetworkGraph());
 	}
 	//[end] Constructor and listener initializer
 
 	//[start] Getters
 	
 	public P2PNetworkGraph getFullGraph() {
 		return fullGraph;
 	}
 	//[end] Getters
 	
 	//[start] List Creator
 	public LinkedList<LogEvent> createList(BufferedReader logFile) {
 		logEvents = new LinkedList<LogEvent>();
 		try {
 			//[start] Create local variables for the creation of the list
 			P2PNetworkGraph tempGraph = new P2PNetworkGraph();
 			String str; //will contain each log event as it is read.
 			List<LogEvent> colouringEvents = new LinkedList<LogEvent>();//list of events that represent the colouring and decolouring of graph elements
 			List<P2PVertex> queryPeers = new LinkedList<P2PVertex>();//
 			HashMap<String, List<String>> peerMap = new HashMap<String, List<String>>();
 			
 			final int totalLines = Integer.parseInt(logFile.readLine()); //the total number of lines so the loading bar can size itself properly
 			int lineCount = 0;
 			//[end] Create local variables for the creation of the list
 			
 			//notify the listeners that the log events have begun loading
 			taskChanged(totalLines, "LogEvents");
 						
 			//[start] Loop reading the file and creating the list
 			logEvents.add(LogEvent.getStartEvent()); //a start event to know when to stop playback of a reversing graph
 			while ((str = logFile.readLine()) != null) //reading lines log file
 			{
 				//Increment the line number and notify the loading listeners
 				lineCount++;
 				progress(lineCount);
 				
 				LogEvent gev = new LogEvent(str); //create the log event
 				
 				if (gev.isConstructing()){ //construct the fullGraph as we go
 					fullGraph.graphConstructionEvent(gev);
 				}
 				
 				createColouringEvents(gev,colouringEvents,queryPeers, tempGraph); //add in colouring events if needed
 				createOfflineEvents(gev, peerMap); //create events for when a peer goes offline
 				logEvents.add(gev); //add this read log event to the list
 				createBackOnlineEvents(gev, peerMap, tempGraph);
 
 				// Update the temporary graph afterwards so it can be used as a reference
 				if (gev.isStructural()) { //construct the tempGraph which has the current connections including disconnects as a reference.
 					tempGraph.graphEvent(gev, true, fullGraph);
 				}
 				
 			}//end while
 			for(LogEvent leftOver : colouringEvents) {
 				logEvents.add(leftOver); //if any colouring events are left over add them into the list
 			}
 			logEvents.add(LogEvent.getEndEvent(logEvents.get(logEvents.size()-1))); //add an end log to know to stop the playback of the graph 100 ms after 
 			//[end] Loop reading the file and creating the list
 		} catch(IOException e) {
 			e.printStackTrace();
 		}
 		return (LinkedList<LogEvent>) logEvents;
 	}
 	//[end] List Creator
 	
 	//[start] Create re-online events
 	/**
 	 * peer was previously online, went offline, and now came back online, re-add their documents and re-connect to their peers
 	 */
 	private void createBackOnlineEvents(LogEvent gev, HashMap<String, List<String>> peerMap, P2PNetworkGraph tempGraph) {
 		//if the event is a peer coming online and the map contains a key for this peer (created when a peer goes offline)
 			//re-connect all documents and peers
 		if(gev.getType().equals("online") && peerMap.containsKey(Integer.toString(gev.getParam(1)))) {
 			for(String evt : peerMap.get(Integer.toString(gev.getParam(1)))) { //iterate over the list of events created when this peer went offline.
 				LogEvent event = new LogEvent(gev.getTime()+evt); //create the event to add
 				if(event.getType().equals("publish")) {
 					logEvents.add(event); //re-publish documents
 				} else if(event.getType().equals("connect")) { 
 					//if the peer we are trying to connect to has gone offline(known through the reference graph) don't re-connect
 					if(tempGraph.containsVertex(new PeerVertex(event.getParam(2)))){ 
 						logEvents.add(event); //but if they are online, connect!
 					}
 				}
 			}
 			peerMap.remove(Integer.toString(gev.getParam(1))); //once re-connected, remove the peer from the map until they go back offline
 		}
 	}
 	//[end] Create re-online events
 	
 	//[start] Create events for when a peer goes offline
 	private void createOfflineEvents(LogEvent gev, HashMap<String, List<String>> peerMap) {
 		if(gev.getType().equals("offline")) { // if a peer goes offline
 			P2PVertex peerGoingOffline = new PeerVertex(gev.getParam(1));
 			
 			if(!peerMap.containsKey(Integer.toString(gev.getParam(1)))) {
 				peerMap.put(Integer.toString(gev.getParam(1)), new LinkedList<String>());
 			}
 			for(P2PConnection edge : fullGraph.getIncidentEdges(peerGoingOffline)) { //check through the nodes they are attached to
 				
 				P2PVertex opposite = fullGraph.getOpposite(peerGoingOffline, edge);
 				if(opposite.getClass().equals(PeerDocumentVertex.class))//store their documents to re-publish if they decide to come back online 
 				{
 					peerMap.get(Integer.toString(gev.getParam(1))).add(":publish:"+gev.getParam(1)+":"+((PeerDocumentVertex) opposite).getDocumentNumber());
					logEvents.add(new LogEvent(gev.getTime(),"remove",gev.getParam(1),((PeerDocumentVertex) opposite).getDocumentNumber(), 0));
 				}
 				else if(opposite.getClass().equals(PeerVertex.class)) {//store their connected peers for when they come back online
 					peerMap.get(Integer.toString(gev.getParam(1))).add(":connect:"+gev.getParam(1)+":"+((PeerVertex) opposite).getKey());
 				}
 			}
 			
 		}
 	}
 	//[end] Create events for when a peer goes offline
 
 	//[start] Create and add colouring Events
 	/**
 	 * 
 	 * @param gev	The LogEvent which is being handled.
 	 * @param colouringEvents	The list of Colouring Events for which to add to.
 	 * @param queryPeers	List of peers who are querying(for highlighting edges as the query propagates.
 	 */
 	private void createColouringEvents(LogEvent gev, List<LogEvent> colouringEvents, List<P2PVertex> queryPeers, P2PNetworkGraph tempGraph) {
 		String eventType = gev.getType();
 		if(eventType.equals("query") || eventType.equals("queryhit"))
 		{
 			colouringEvents.add(LogEvent.createOpposingLogEvent(gev,2000)); // add an opposing event to decolour/debold
 			if(eventType.equals("query")) { //if it was a query then add the peer to the list of querying peers
 				queryPeers.add(new PeerVertex(gev.getParam(1))); 
 			}
 		} 
 		else if(eventType.equals("queryreachespeer")) {
 			colouringEvents.add(LogEvent.createOpposingLogEvent(gev,750)); // add
 			P2PVertex queriedPeer = new PeerVertex(gev.getParam(1));
 			//[start] bold the edge between the peer which the query reached and the sender
 			for(P2PVertex querySender : queryPeers) {
 				if(tempGraph.findEdge(querySender, queriedPeer) != null) {
 					LogEvent ev = new LogEvent(gev.getTime()+1,"queryedge",querySender.getKey(),queriedPeer.getKey(),0);
 					
 					colouringEvents.add(ev);
 					colouringEvents.add(LogEvent.createOpposingLogEvent(ev,750));
 					break;
 				}
 			}
 			//[end] bold the edge between the peer which the query reached and the sender
 			queryPeers.add(queriedPeer);
 		}
 		Collections.sort(colouringEvents); // sort the events prior to adding them to the list of log events
 		
 		//List is sorted in order of ascending time so iterate from the start.
 		for(Iterator<LogEvent> iter = colouringEvents.iterator(); iter.hasNext(); ) {
 			
 			LogEvent event = (LogEvent)iter.next();
 			if(event.getTime() < gev.getTime() ) { //add only if the event takes place before the LogEvent that was read this iteration
 				logEvents.add(event); //add the colouring event
 				iter.remove(); //remove it from the list now that it is added
 			} else {
 				break; //if this Event's time is greater than gev, the rest should be too, then there is no point continuing
 			}
 		}
 	}
 	//[end] Create and add colouring Events
 }
