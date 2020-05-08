 package org.openstreetmap.OSMZmiany;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
 
 import javax.xml.parsers.SAXParser;
 import javax.xml.parsers.SAXParserFactory;
 
 import org.openstreetmap.osmosis.core.xml.common.DateParser;
 import org.xml.sax.Attributes;
 import org.xml.sax.SAXException;
 import org.xml.sax.helpers.DefaultHandler;
 
 
 public class DataContainer extends DefaultHandler{
 	short mode=0;
 
 	DateParser dp = new DateParser();
 	
 	class Node{
 		long id;
 		double lat,lon;
 		long changesetId;
 		short mode;
 		public Node(long id, double lat, double lon, long changesetId, short mode){
 			this.id=id;
 			this.lat=lat;
 			this.lon=lon;
 			this.changesetId=changesetId;
 			this.mode=mode;
 		}
 	}
 	
 	class Way{
 		long id;
 		long changesetId;
 		short mode;		
 		public Way(){}
 	}
 	
 	class Changeset{
 		long id;
 		long userId;
 		long time;
 		public Changeset(long id,long time,long user){this.id=id;this.userId=user;this.time=time;}
 	}
 	
 	
 	
	private Map<Long,Node> nodes = new ConcurrentHashMap<Long,Node>();
	private Map<Long,Way> ways = new ConcurrentHashMap<Long,Way>();
	private Map<Long,User> users = new HashMap<Long,User>();
 	
	private Map<Long,Integer> changesetsIndex = new ConcurrentHashMap<Long,Integer>();
	private Vector <Changeset> changesets = new Vector <Changeset>();
 
	private ArrayList<DataContainerListener> datacontainerListener = new ArrayList<DataContainerListener>();
 	
 	
 	
 	public DataContainer(){       
 	}
 	
 	
 	synchronized void addData(InputStream i){
 		System.out.println("Adding data...");
 		
 		try {
 			SAXParserFactory factory = SAXParserFactory.newInstance();
 			factory.setValidating(false);
 			SAXParser saxParser = factory.newSAXParser();
 			saxParser.parse(i, this);
 		} catch (IOException e) {
 			System.out.println("IOException: " + e);
 			e.printStackTrace();
 			System.exit(10);
 		} catch (SAXException e) {
 			System.out.println("SAXException: " + e);
 			e.printStackTrace();
 			System.exit(10);
 		} catch (Exception e) {
 			System.out.println("Other Exception: " + e);
 			e.printStackTrace();
 			System.exit(10);
 		}
 		for(int j=0;j<datacontainerListener.size();j++)
 			datacontainerListener.get(j).dataChanged();
 	}
 	
 	synchronized public void startElement(String namespaceURI, String localName, String qName, Attributes atts) {
 		 if (qName.equals("create")) {
 			 mode = 0;
 		 } else if (qName.equals("modify")) {
 			 mode = 1;
 		 } else if (qName.equals("delete")) {
 			 mode = 2;
 		 } else if (qName.equals("node")) {
 			 //TODO It can be faster!
 			 
 			 //NODES 
 			 double lat = Double.parseDouble(atts.getValue("lat"));
 			 double lon = Double.parseDouble(atts.getValue("lon"));
 			 long id = Long.parseLong(atts.getValue("id"));
 			 
 			 //USER
 			 long uid = Long.parseLong(atts.getValue("uid"));
 			 String userName = atts.getValue("user");
 			 
 			 //CHANGESET
 			 long changesetId = Long.parseLong(atts.getValue("changeset"));
 			 long time = dp.parse(atts.getValue("timestamp")).getTime();
 			 
 			 
 			 
 			 Node node=getNode(id,lat,lon,changesetId,mode);
 			 MapFilter mf=Configuration.instance.getSelectedProfile().getMapFilter();
 			 if(mf!=null&&!mf.nodeFilter(node))
 				 getNodes().remove(id);
 			 else{			 
 				 getChangeset(changesetId,time,uid);
 				 getUser(uid,userName);
 			 }
 		 }
 	}
 
 
 	synchronized private Node getNode(long id, double lat, double lon, long changesetId,
 			short mode) {
 		
 		Node node=getNodes().get(changesetId);
 		if(node==null){
 			node=new Node(id,lat,lon,changesetId,mode);
 			getNodes().put(id, node);
 		}else{
 			node.lat=lat;
 			node.lon=lon;
 			node.mode=mode;
 		}
 		return node;
 	}
 
 
 	synchronized private Changeset getChangeset(long changesetId, long time, long user) {
 		Integer changesetI=getChangesetsIndex().get(changesetId);
 		Changeset chs=null;
 		if(changesetI==null){
 			chs=new Changeset(changesetId,time,user);
 			getChangesets().add(chs);
 			getChangesetsIndex().put(changesetId, getChangesets().size()-1);
 		}else{
 			chs=getChangesets().get(changesetI);
 			chs.time=time;
 		}
 		return chs;
 	}
 
 
 	synchronized private User getUser(long uid, String username) {
 		User user=getUsers().get(uid);
 		if(user==null){
 			user=new User(uid,username);
 			getUsers().put(uid, user);
 		}
 		return user;
 	}
 
 
 	
 	synchronized public void removeData(MapFilter mf) {
 		//Remove data
 		
 		//nodes
 		Iterator<Long> nodeL=getNodes().keySet().iterator();
 		Vector <Long> toRemove=new Vector <Long>();
 		while(nodeL.hasNext()){
 			Long t=nodeL.next();
 			if(!mf.nodeFilter(getNodes().get(t))){
 				toRemove.add(t);
 			}			
 		}
 		for(int i=0;i<toRemove.size();i++)
 			getNodes().remove(toRemove.get(i));
 		
 		//TODO changesets, ways
 	}
 
 
 	synchronized public void clear() {
 		this.getChangesets().clear();
 		this.getNodes().clear();
 		this.getWays().clear();
 		this.getUsers().clear();
 		this.getChangesetsIndex().clear();
 		for(int j=0;j<datacontainerListener.size();j++)
 			datacontainerListener.get(j).dataChanged();
 	}
 	
 	public void addDataContainerListener(DataContainerListener dcl){
 		datacontainerListener.add(dcl);
 	}
 
 
 	public Map<Long,Node> getNodes() {
 		return nodes;
 	}
 
 
 	public void setNodes(HashMap<Long,Node> nodes) {
 		this.nodes = nodes;
 	}
 
 
 	public Map<Long,Way> getWays() {
 		return ways;
 	}
 
 
 	public void setWays(HashMap<Long,Way> ways) {
 		this.ways = ways;
 	}
 
 
 	public Map<Long,User> getUsers() {
 		return users;
 	}
 
 
 	public void setUsers(HashMap<Long,User> users) {
 		this.users = users;
 	}
 
 
 	public Map<Long,Integer> getChangesetsIndex() {
 		return changesetsIndex;
 	}
 
 
 	public void setChangesetsIndex(HashMap<Long,Integer> changesetsIndex) {
 		this.changesetsIndex = changesetsIndex;
 	}
 
 
 	public Vector <Changeset> getChangesets() {
 		return changesets;
 	}
 
 
 	public void setChangesets(Vector <Changeset> changesets) {
 		this.changesets = changesets;
 	}
 }
