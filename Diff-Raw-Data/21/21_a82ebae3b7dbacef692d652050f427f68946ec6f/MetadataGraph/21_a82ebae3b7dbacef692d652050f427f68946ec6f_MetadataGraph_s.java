 /*
  * "Copyright (c) 2010-12 The Regents of the University  of California. 
  * All rights reserved.
  *
  * Permission to use, copy, modify, and distribute this software and its
  * documentation for any purpose, without fee, and without written agreement is
  * hereby granted, provided that the above copyright notice, the following
  * two paragraphs and the author appear in all copies of this software.
  *
  * IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY FOR
  * DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES ARISING OUT
  * OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF THE UNIVERSITY OF
  * CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  *
  * THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
  * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY
  * AND FITNESS FOR A PARTICULAR PURPOSE.  THE SOFTWARE PROVIDED HEREUNDER IS
  * ON AN "AS IS" BASIS, AND THE UNIVERSITY OF CALIFORNIA HAS NO OBLIGATION TO
  * PROVIDE MAINTENANCE, SUPPORT, UPDATES, ENHANCEMENTS, OR MODIFICATIONS."
  *
  * Author:  Jorge Ortiz (jortiz@cs.berkeley.edu)
  * StreamFS release version 2.2
  */
 
 package local.metadata.context;
 
 import local.db.*;
 import local.rest.*;
 import local.rest.resources.*;
 import local.rest.resources.util.*;
 import net.sf.json.*;
 import jdsl.graph.api.*;
 import jdsl.graph.ref.*;
 import jdsl.graph.algo.*;
 import jdsl.core.api.ObjectIterator;
 
 //import com.sun.net.httpserver.*;
 //import java.util.concurrent.Executors;
 import java.util.*;
 import java.io.*;
 import java.net.*;
 import java.util.logging.Logger;
 import java.util.logging.Level;
 
 import local.analytics.*;
 
 public class MetadataGraph{
 
 	private static MetadataGraph metadataGraph = null;
 	private Hashtable<String, Vertex> pubNodes = null;
 	private Hashtable<String, Vertex> nonpubNodes = null;
 	private Hashtable<String, Vertex> symlinkNodes = null;
 	private Hashtable<String, Vertex> externalNodes = null;
 	private IncidenceListGraph internalGraph = null;
 
     //link to router for aggregation
     //private PipedInputStream pipedIn = null;
     //private PipedOutpuStream pipedOut =null;
     private ObjectInputStream routerIn = null;
     private ObjectOutputStream routerOut = null;
     private static boolean tellRouter = false;
 
 	protected static transient final Logger logger = Logger.getLogger(MetadataGraph.class.getPackage().getName());
 	protected static MySqlDriver database = (MySqlDriver) DBAbstractionLayer.database;
 
 	private MetadataGraph(){
 		internalGraph = new IncidenceListGraph();
 		pubNodes = new Hashtable<String, Vertex>();
 		nonpubNodes = new Hashtable<String, Vertex>();
 		externalNodes = new Hashtable<String, Vertex>();
 		symlinkNodes = new Hashtable<String, Vertex>();
 		//populateInternalGraph();
 	}
 
 	public static MetadataGraph getInstance(){
 		if(metadataGraph == null)
 			metadataGraph = new MetadataGraph();
 		return metadataGraph;
 	}
 
     public void setRouterCommInfo(PipedInputStream rIn, PipedOutputStream rOut){
         try {
             if(routerOut == null){
                 routerOut = new ObjectOutputStream(rOut);
                 routerOut.flush();
             }
             if(routerIn == null)
                 routerIn = new ObjectInputStream(rIn);
         } catch(Exception e) {
             logger.log(Level.SEVERE, "", e);
             System.exit(1);
         }
     }
 
     public void setRouterCommInfo(String routerHost, int routerPort){
         try {
             Socket s = new Socket(InetAddress.getByName(routerHost), routerPort);
             routerOut = new ObjectOutputStream(s.getOutputStream());
             routerOut.flush();
             routerIn = new ObjectInputStream(s.getInputStream());
         } catch(Exception e) {
             logger.log(Level.SEVERE, "", e);
             System.exit(1);
         }
     }
 
     public String queryAgg(String path, String aggtype, String units, JSONObject queryJson){
         try{
             RouterCommand rcmd = new RouterCommand(RouterCommand.CommandType.PULL);
             rcmd.setSrcVertex(path);
             rcmd.setAggType(aggtype);
             rcmd.setUnits(units);
 
             //extract the timestamp limits from
             //queryJson
 
             routerOut.writeObject(rcmd);
             routerOut.flush();
 
             rcmd = (RouterCommand)routerIn.readObject();
             return rcmd.data;
         } catch(Exception e){
             logger.log(Level.WARNING, "",e);
         }
         return null;
     }
 
     /**
      * Turns the node with the given pathname as an in/active aggregation point for 
      * the specified units.
      *
      * @param pathname the path of the node.
      * @param units the units of data to un/aggregate.
      * @param state true sets the aggregation point on, false turns it off.
      * @return void.
      */
     public void setAggPoint(String pathname, String units, boolean state){
         try{
             RouterCommand rcmd = new RouterCommand(RouterCommand.CommandType.CREATE_AGG_PNT);
             rcmd.setSrcVertex(pathname);
             rcmd.setUnits(units);
             rcmd.setAggState(state);
 
             routerOut.writeObject(rcmd);
             routerOut.flush();
 
             rcmd = (RouterCommand)routerIn.readObject();
         } catch(Exception e){
             logger.log(Level.WARNING, "",e);
         }
     }
 
 	public synchronized void populateInternalGraph(boolean tRouter){
         tellRouter = tRouter;
 		JSONArray hardlinks = database.getAllHardLinks();
 		for(int i=0; i<hardlinks.size(); i++){
 			String thisPath = (String)hardlinks.get(i);
 			Resource thisResource = RESTServer.getResource(thisPath);
 			if(thisResource !=null){
 				thisPath = thisResource.getURI();
 				Vertex v = internalGraph.insertVertex(thisPath);
 				v.set("path", thisPath);
 
                 routerAddNode(thisPath);
 
 				if(RESTServer.getResource(thisPath).getType()==ResourceUtils.PUBLISHER_RSRC ||
 					RESTServer.getResource(thisPath).getType()==ResourceUtils.GENERIC_PUBLISHER_RSRC)
 					pubNodes.put(thisPath,v);
 				else
 					nonpubNodes.put(thisPath, v);
 			}
 		}
 
 		//create the links between each of the nodes by adding an edge from parent
 		//to the node
 		for(int i=0; i<hardlinks.size(); i++){
 			String thisPath = (String)hardlinks.get(i);
 			Vertex thisNode = getVertex(thisPath);
 			if(!thisPath.equals("/")){
 				String parentPath = getParentPath(thisPath);
 				Vertex parentNode = getVertex(parentPath);
 				if(parentNode != null && thisNode != null){
 					internalGraph.insertDirectedEdge(parentNode, thisNode, "hardlink");
 
                     routerAddLink(parentPath, thisPath);
                 }
 			}
 		}
 
 		//handle the symlinks by walking the path from a source vertex to a destination vertex
 		JSONArray symlinks = database.getAllSymlinks();
 		for(int i=0; i<symlinks.size(); i++){
 			String thisPath = (String)symlinks.get(i);
 			Resource r = RESTServer.getResource(thisPath);
 			if(r!=null){
 				thisPath = RESTServer.getResource(thisPath).getURI();
 				Vertex v = internalGraph.insertVertex(thisPath);
 				v.set("path", thisPath);
 				symlinkNodes.put(thisPath, v);
 
                 routerAddNode(thisPath);
 			}
 		}
 
 		//add nodes to the graph that are external symlinks to remote instance of streamfs
 		JSONArray externalLinks = database.getAllExternalLinks();
 		for(int i=0; i<externalLinks.size(); i++){
 			String thisPath = (String)externalLinks.get(i);
 			thisPath = RESTServer.getResource(thisPath).getURI();
 			Vertex v = internalGraph.insertVertex(thisPath);
 			v.set("path", thisPath);
 			externalNodes.put(thisPath, v);
 		}
 
 		//create the links between the symlinks, their parent, and the vertex they link to
 		for(int i=0; i<symlinks.size(); i++){
 
 			//create a link between this symlink and the resource it points to
 			SymlinkResource thisResource = (SymlinkResource)RESTServer.getResource((String)symlinks.get(i));
 			if(thisResource !=null){
 				String thisPath = thisResource.getURI();
 				String linksToPath = thisResource.getLinkString();
 				Vertex symlinkNode = getVertex(thisPath);
 				if(linksToPath.startsWith("/")){
 					Vertex linksToNode =getVertex(linksToPath);
 					internalGraph.insertDirectedEdge(symlinkNode, linksToNode, "linksto");
                    
                     routerAddLink(thisPath, linksToPath);
 				}
 
 				//create a link between this resource and its parent
 				String symlinkParentPath = getParentPath((String)symlinks.get(i));
 				symlinkParentPath = RESTServer.getResource(symlinkParentPath).getURI();
 				Vertex parentVertex = getVertex(symlinkParentPath);
 				internalGraph.insertDirectedEdge(parentVertex, symlinkNode, "symlink");
 
                 routerAddLink(symlinkParentPath, thisPath);
 			} else {
 				logger.fine("Could not get: " + (String)symlinks.get(i));
 			}
 		}
 	}
 
 	/**
 	 * Looks for the vertex object based on path and returns it.
 	 */
 	private Vertex getVertex(String path){
 		Resource thisResource = RESTServer.getResource(path);
 		if(thisResource !=null){
 			path = RESTServer.getResource(path).getURI();
 			Vertex vertex = nonpubNodes.get(path);
 			if(vertex==null)
 				vertex = pubNodes.get(path);
 			if(vertex ==null)
 				vertex = symlinkNodes.get(path);
 			if(vertex ==null)
 				vertex = externalNodes.get(path);
 			return vertex;
 		}
 		return null;
 	}
 
 	/**
 	 * Looks for the vertex object by path and removed it from the associated list.
 	 */
 	private boolean removeVertex(String path){
 		path = RESTServer.getResource(path).getURI();
 		if(path!=null){
 			nonpubNodes.remove(path);
 			pubNodes.remove(path);
 			symlinkNodes.remove(path);
 			externalNodes.remove(path);
 			return true;
 		}
 		return false;
 	}
 
 	/**
 	 * Parses the path and returns the path fo the parent.
 	 */
 	private String getParentPath(String path){
 		Resource resource = RESTServer.getResource(path);
 		if(resource == null)
 			return null;
 		path = RESTServer.getResource(path).getURI();
 		if(path.equals("/"))
 			return null;
 		StringTokenizer tokenizer = new StringTokenizer(path, "/");
 		Vector<String> elts = new Vector<String>();
 		while(tokenizer.hasMoreElements())
 			elts.add(tokenizer.nextToken());
 		StringBuffer parentPathBuf = new StringBuffer();
 		for(int k=0; k<elts.size()-1; k++)
 			parentPathBuf.append("/").append(elts.elementAt(k));
 		parentPathBuf.append("/");
 		return parentPathBuf.toString();
 	}
 
     private void routerAddNode(String nodePath){
         if(tellRouter){
             try {
                 setRouterCommInfo("localhost", 9999);
                 //Thread.sleep(500);
                 //tell the router about it
                 RouterCommand rcmd = new RouterCommand(RouterCommand.CommandType.ADD_NODE);
                 rcmd.setSrcVertex(nodePath);
                 routerOut.writeObject(rcmd);
                 routerOut.flush();
 
                 rcmd = (RouterCommand)routerIn.readObject();
                 logger.info("Heard reply");
             } catch(Exception e){
                 logger.log(Level.WARNING, "", e);
             }
         }
     }
 
     private void routerRemoveNode(String nodePath){
         if(tellRouter){
             try {
                 setRouterCommInfo("localhost", 9999);
                 //Thread.sleep(500);
                 //tell the router about it
                 RouterCommand rcmd = new RouterCommand(RouterCommand.CommandType.REMOVE_NODE);
                 rcmd.setSrcVertex(nodePath);
                 routerOut.writeObject(rcmd);
                 routerOut.flush();
 
                 rcmd = (RouterCommand)routerIn.readObject();
                 logger.info("Heard reply");
             } catch(Exception e){
                 logger.log(Level.WARNING, "", e);
             }
         }
     }
 
     private void routerAddLink(String parentPath, String childPath){
         if(tellRouter){
             try {
                 setRouterCommInfo("localhost", 9999);
                 //tell the router about it
                 RouterCommand rcmd = new RouterCommand(RouterCommand.
                                                         CommandType.ADD_LINK);
                 rcmd.setSrcVertex(parentPath);
                 rcmd.setDstVertex(childPath);
                 routerOut.writeObject(rcmd);
                 routerOut.flush();
 
                 rcmd = (RouterCommand)routerIn.readObject();
                 logger.info("Heard reply");
             } catch(Exception e){
                 logger.log(Level.WARNING, "", e);
             }
         }
     }
 
     private void routerRemoveLink(String parentPath, String childPath){
         if(tellRouter){
             try {
                 setRouterCommInfo("localhost", 9999);
                 //tell the router about it
                 RouterCommand rcmd = new RouterCommand(RouterCommand.
                                                         CommandType.REMOVE_LINK);
                 rcmd.setSrcVertex(parentPath);
                 rcmd.setDstVertex(childPath);
                 routerOut.writeObject(rcmd);
                 routerOut.flush();
 
                 rcmd = (RouterCommand)routerIn.readObject();
                 logger.info("Heard reply");
             } catch(Exception e){
                 logger.log(Level.WARNING, "", e);
             }
         }
     }
 
 	public synchronized boolean addNode(String resourcePath){
 		logger.info("Attempting to add: " + resourcePath);
 		if(resourcePath !=null){
 			Resource resource = RESTServer.getResource(resourcePath);
 			Vertex thisVertex = null;
 			boolean symlink=false;
 			if((thisVertex =internalGraph.insertVertex(resourcePath)) !=null){
 				thisVertex.set("path", resourcePath);
 				String linksToStr = null; 	//only used if this vertex is a symlink
 				if(resource.getType() == ResourceUtils.DEFAULT_RSRC || 
 					resource.getType() == ResourceUtils.GENERIC_PUBLISHER_RSRC){
 					pubNodes.put(resource.getURI(), thisVertex);
 				} else if(resource.getType() == ResourceUtils.SYMLINK_RSRC){
 					symlinkNodes.put(resource.getURI(), thisVertex);
 					linksToStr = ((SymlinkResource)resource).getLinkString();
 					Vertex linksToNode = null;
 					if(linksToStr.startsWith("http://")){
 						Vertex node= getVertex(linksToStr);
 						if(node ==null){
 							node = internalGraph.insertVertex(linksToStr);
 							node.set("path", linksToStr);
 						}
 						externalNodes.put(linksToStr, node);
 					} else {
 						linksToNode = getVertex(linksToStr);
 					}
 					internalGraph.insertDirectedEdge(thisVertex, linksToNode, "linksto");
 					symlink=true;
 
                     routerAddLink(resourcePath, linksToStr);
 				} else {
 					nonpubNodes.put(resource.getURI(), thisVertex);
 					String parent = getParentPath(resource.getURI());
 					Vertex parentVertex = getVertex(parent);
 				}
 
 				String parent = getParentPath(resource.getURI());
 				Vertex parentVertex = getVertex(parent);
 				if(symlink){
 					internalGraph.insertDirectedEdge(parentVertex, thisVertex, "symlink");
                     routerAddLink(parent, resourcePath);
 					if(hasCycle()){
 						//there is a cycle introduced by this symlink, backtrack the change and return false
 						removeNode((String)thisVertex.get("path"));
 						Vertex linksToVertex = getVertex(linksToStr);
 						removeNode((String)linksToVertex.get("path"));
 
                         routerRemoveNode((String)thisVertex.get("path"));
                         routerRemoveNode((String)linksToVertex.get("path"));
 						return false;
 					} else {
 						logger.fine("NO CYCLE DETECTED");
 					}
 				} else{
 					internalGraph.insertDirectedEdge(parentVertex, thisVertex, "hardlink");
 
                     routerAddLink(parent, resourcePath);
 				}
 				
 				return true;
 			}
 		}
 		return false;
 	}
 
 	public synchronized boolean removeNode(String resourcePath){
 		Vertex thisVertex = getVertex(resourcePath);
 		if(thisVertex!=null){
 			internalGraph.removeVertex(thisVertex);
 			removeVertex(resourcePath);
 
             routerRemoveNode(resourcePath);
 		}
 
 		return false;
 	}
 
 
 	/**
 	 * Checks if there is a cycle in the graph
 	 */
 	public boolean hasCycle(){
 		DirectedFindCycleDFS dfs = new DirectedFindCycleDFS();
 		Vertex root= getVertex("/");
 		dfs.execute(internalGraph, root);
 		ObjectIterator iterator = dfs.getCycle();
 		if(iterator.hasNext())
 			return true;
 		return false;
 	}
 
 	/**
 	 * Check if there exists a path between the source resource and the destination resource is the resource/context graph.
 	 * @param srcResource the path of the source resource.
 	 * @param dstResource the path of the destination resource.
 	 * @return an array of arrays, where each element is a two-element array that consists of the src vertex and destination
 	 * 	vertex for an edge on the path described by the array returned.  Null if no path exists.
 	 */
 	public JSONArray pathExists(String srcResourcePath, String dstResourcePath){
 		Vertex srcVertex = getVertex(srcResourcePath);
 		Vertex dstVertex = getVertex(dstResourcePath);
 		JSONArray pathElts = null;
 		if(srcVertex!=null && dstVertex!=null){
 			PathFinder pathFinder =new PathFinder();
 			pathFinder.execute(internalGraph, srcVertex, dstVertex);
 			if(pathFinder.pathExists()){
 				pathElts = new JSONArray();
 				EdgeIterator edgesInPath = pathFinder.reportPath();
 				while(edgesInPath.hasNext()){
 					Edge e = edgesInPath.nextEdge();
 					JSONArray thisEdge = new JSONArray();
 					Vertex[] vertices = internalGraph.endVertices(e);
 					thisEdge.add(0, vertices[0].get("path"));
 					thisEdge.add(1, vertices[1].get("path"));
 					pathElts.add(thisEdge);
 				}
 			}
 
 		}
 		return pathElts;
 	}
 
 	/**
 	 * Used to find paths in the graph.
 	 */
 	public class PathFinder extends IntegerDijkstraPathfinder{
 		public PathFinder(){
 			super();
 		}
 
 		protected int weight(Edge e){
 			return 1;
 		}
 	}
 
 
 }
