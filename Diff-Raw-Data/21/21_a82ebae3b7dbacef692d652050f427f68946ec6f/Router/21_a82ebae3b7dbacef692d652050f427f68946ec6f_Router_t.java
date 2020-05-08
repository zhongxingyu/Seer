 package local.analytics;
 
 import local.analytics.util.*;
 import util.*;
 import org.json.*;
 import jdsl.graph.api.*;
 import jdsl.graph.ref.*;
 import jdsl.graph.algo.*;
 import jdsl.core.api.ObjectIterator;
 import java.util.logging.Logger;
 import java.util.logging.Level;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.regex.*;
 import java.util.UUID;
 import java.io.*;
 import java.net.*;
 import java.util.Random;
 
 public class Router implements Runnable{
 
 	private ConcurrentHashMap<String, Vertex> nodeList = null;
 	private IncidenceListGraph graph = null;
     private PathFinder pathFinder = null;
   
     /*private static PipedInputStream pIncoming = null;
     private static PipedOutputStream pOutgoing = null; 
     private static ObjectInputStream incoming = null;
     private static ObjectOutputStream outgoing = null;*/
 
 	protected static transient final Logger logger = Logger.getLogger(Router.class.getPackage().getName());
 
     public static void main2(String[] args){
         /*boolean b = Pattern.matches("(/+.)++/*", "/a/b//");
         System.out.println(b);
 
         String test = "//a////b/////c";
         test = test.replaceAll("/+", "/");
         if(!test.endsWith("/"))
             test +="/";
         System.out.println(test);*/
 
         Router router = new Router();
 
         String nodePath00 = "/";
         String nodePath01 = "/a";
         String nodePath02 = "/a/b";
         String nodePath03 = "/a/c/";
         router.addNodeEvent(nodePath00);
         router.addNodeEvent(nodePath01);
         router.addNodeEvent(nodePath02);
         router.addNodeEvent(nodePath03);
         router.createLink(nodePath00, nodePath01);
         router.createLink(nodePath01, nodePath02);
         router.createLink(nodePath01, nodePath03);
         router.setUnitAndTypeAtPath(nodePath01, "KW", ProcType.AGGREGATE, true);
 
         //test data production
         try {
             JSONObject fakeData = new JSONObject();
             fakeData.put("ts", 1L);
             fakeData.put("value",1.0);
             router.sendData(nodePath01, nodePath02, fakeData.toString(), "KW");
 
             fakeData.put("ts", 2L);
             fakeData.put("value",4.0);
             router.sendData(nodePath01, nodePath03, fakeData.toString(), "KW");
             
             fakeData.put("ts", 3L);
             fakeData.put("value",3.0);
             router.sendData(nodePath01, nodePath02, fakeData.toString(), "KW");
             
             fakeData.put("ts", 4L);
             fakeData.put("value",8.0);
             router.sendData(nodePath01, nodePath03, fakeData.toString(), "KW");
         } catch(Exception e){
             e.printStackTrace();
         }
     }
 
     //For Mobisys
     public static void main(String[] args){
         Router router = new Router();
 
         //router.scenario1(router);
         router.scenario2(router);
     }
 
     private void scenario1(Router router){
         String root = "/";
         String spaces = "/spaces";
         String room1 = "/spaces/room1";
         String room2 = "/spaces/room2";
         String laptop1 = "/spaces/room1/laptop1";
         String laptop2= "/spaces/room1/laptop2";
         String lamp1 = "/spaces/room1/lamp1";
         String lamp2 = "/spaces/room1/lamp2";
 
 
         //add nodes
         router.addNodeEvent(root);
         router.addNodeEvent(spaces);
         router.addNodeEvent(room1);
         router.addNodeEvent(room2);
         router.addNodeEvent(laptop1);
         router.addNodeEvent(laptop2);
         router.addNodeEvent(lamp1);
         router.addNodeEvent(lamp2);
 
         router.createLink(root, spaces);
         
         router.createLink(spaces, room1);
         router.createLink(spaces, room2);
         
         router.createLink(room1, lamp1);
         router.createLink(room1, lamp2);
         router.createLink(room1, laptop1);
         router.createLink(room1, laptop2);
 
         router.setUnitAndTypeAtPath(room1, "KW", ProcType.AGGREGATE, true);
         router.setUnitAndTypeAtPath(room2, "KW", ProcType.AGGREGATE, true);
 
         //test data production
         try {
             //initial
             Random rand = new Random();
             for(int i=0; i<4; i++){
                 JSONObject fakeData = new JSONObject();
                 fakeData.put("ts", (long)i);
                 double lampwatts = 60;
                 if(rand.nextDouble()>0.5)
                     lampwatts += rand.nextDouble();
                 else
                     lampwatts += rand.nextDouble();
                 fakeData.put("value",lampwatts);
                 router.sendData(room1, lamp1, fakeData.toString(), "KW");
 
                 fakeData.put("ts", (long)i+1);
                 lampwatts = 60;
                 if(rand.nextDouble()>0.5)
                     lampwatts += rand.nextDouble();
                 else
                     lampwatts += rand.nextDouble();
                 fakeData.put("value",lampwatts);
                 router.sendData(room1, lamp2, fakeData.toString(), "KW");
 
                 //laptops produce data
                 fakeData.put("ts", (long)i+2);
                 double laptopwatts = 30;
                 if(rand.nextDouble()>0.5)
                     laptopwatts += rand.nextDouble();
                 else
                     laptopwatts += rand.nextDouble();
                 fakeData.put("value",laptopwatts);
                 router.sendData(room1, laptop1, fakeData.toString(), "KW");
 
                 fakeData.put("ts", (long)i+3);
                 laptopwatts = 30;
                 if(rand.nextDouble()>0.5)
                     laptopwatts += rand.nextDouble();
                 else
                     laptopwatts += rand.nextDouble();
                 fakeData.put("value",laptopwatts);
                 router.sendData(room1, laptop2, fakeData.toString(), "KW");
             }
 
             //laptop1 leaves and laptop1 is turned off
             router.removeLink(room1, laptop1);
             System.out.println("\n\na========LAPTOP1 HAS LEFT ROOM1============\n\n");
             
             for(int i=10; i<26; i++){
                 JSONObject fakeData = new JSONObject();
                 fakeData.put("ts", (long)i);
                 double lampwatts = 60;
                 if(rand.nextDouble()>0.5)
                     lampwatts += rand.nextDouble();
                 else
                     lampwatts += rand.nextDouble();
                 fakeData.put("value",lampwatts);
                 router.sendData(room1, lamp1, fakeData.toString(), "KW");
 
                 fakeData.put("ts", (long)i+1);
                 lampwatts = 60;
                 if(rand.nextDouble()>0.5)
                     lampwatts += rand.nextDouble();
                 else
                     lampwatts += rand.nextDouble();
                 fakeData.put("value",lampwatts);
                 router.sendData(room1, lamp2, fakeData.toString(), "KW");
 
                 //laptops produce data
                 double laptopwatts = 30;
                 fakeData.put("ts", (long)i+3);
                 laptopwatts = 30;
                 if(rand.nextDouble()>0.5)
                     laptopwatts += rand.nextDouble();
                 else
                     laptopwatts += rand.nextDouble();
                 fakeData.put("value",laptopwatts);
                 router.sendData(room1, laptop2, fakeData.toString(), "KW");
             }
 
             router.createLink(room2, laptop1);
             System.out.println("\n\na========LAPTOP1 HAS JOINED ROOM2===========\n\n");
             for(int i=12; i<100; i+=4){
                 JSONObject fakeData = new JSONObject();
                 fakeData.put("ts", (long)i);
                 double laptopwatts = 30;
                 if(rand.nextDouble()>0.5)
                     laptopwatts += rand.nextDouble();
                 else
                     laptopwatts += rand.nextDouble();
                 fakeData.put("value",laptopwatts);
                 router.sendData(room2, laptop1, fakeData.toString(), "KW");
             }
 
 
             
         } catch(Exception e){
             e.printStackTrace();
         }
     }
 
     private void scenario2(Router router){
         String root = "/";
         String spaces = "/spaces";
         String room1 = "/spaces/room1";
         String room2 = "/spaces/room2";
         String laptop1 = "/spaces/room1/laptop1";
         String laptop2= "/spaces/room1/laptop2";
         String lamp1 = "/spaces/room1/lamp1";
         String lamp2 = "/spaces/room1/lamp2";
         String users = "/users";
         String user1 = "/users/jortiz";
         String user2 = "/users/dculler";
 
         //add nodes
         router.addNodeEvent(root);
         router.addNodeEvent(spaces);
         router.addNodeEvent(room1);
         router.addNodeEvent(room2);
         router.addNodeEvent(laptop1);
         router.addNodeEvent(laptop2);
         router.addNodeEvent(lamp1);
         router.addNodeEvent(lamp2);
         router.addNodeEvent(users);
         router.addNodeEvent(user1);
         router.addNodeEvent(user2);
 
         router.createLink(root, spaces);
         router.createLink(root, users);
         
         router.createLink(spaces, room1);
         router.createLink(spaces, room2);
         
         router.createLink(room1, lamp1);
         router.createLink(room1, lamp2);
         router.createLink(room1, laptop1);
         router.createLink(room1, laptop2);
 
         router.createLink(users, user1);
         router.createLink(users, user2);
         router.createLink(user1, lamp1);
         router.createLink(user1, laptop1);
         router.createLink(user2, lamp2);
         router.createLink(user2, laptop2);
 
         router.setUnitAndTypeAtPath(room1, "KW", ProcType.AGGREGATE, true);
         router.setUnitAndTypeAtPath(room2, "KW", ProcType.AGGREGATE, true);
         router.setUnitAndTypeAtPath(user1, "KW", ProcType.AGGREGATE, true);
         router.setUnitAndTypeAtPath(user2, "KW", ProcType.AGGREGATE, true);
 
         //test data production
         try {
             //initial
             Random rand = new Random();
             for(int i=0; i<4; i++){
                 JSONObject fakeData = new JSONObject();
                 fakeData.put("ts", (long)i);
                 double lampwatts = 60;
                 if(rand.nextDouble()>0.5)
                     lampwatts += rand.nextDouble();
                 else
                     lampwatts += rand.nextDouble();
                 fakeData.put("value",lampwatts);
                 router.sendDataToParents(lamp1, fakeData.toString(), "KW");
 
                 fakeData.put("ts", (long)i+1);
                 lampwatts = 60;
                 if(rand.nextDouble()>0.5)
                     lampwatts += rand.nextDouble();
                 else
                     lampwatts += rand.nextDouble();
                 fakeData.put("value",lampwatts);
                 router.sendDataToParents(lamp2, fakeData.toString(), "KW");
 
                 //laptops produce data
                 fakeData.put("ts", (long)i+2);
                 double laptopwatts = 30;
                 if(rand.nextDouble()>0.5)
                     laptopwatts += rand.nextDouble();
                 else
                     laptopwatts += rand.nextDouble();
                 fakeData.put("value",laptopwatts);
                 router.sendDataToParents(laptop1, fakeData.toString(), "KW");
 
                 fakeData.put("ts", (long)i+3);
                 laptopwatts = 30;
                 if(rand.nextDouble()>0.5)
                     laptopwatts += rand.nextDouble();
                 else
                     laptopwatts += rand.nextDouble();
                 fakeData.put("value",laptopwatts);
                 router.sendDataToParents(laptop2, fakeData.toString(), "KW");
             }
 
             //laptop1 leaves and lamp1 is turned off
             router.removeLink(room1, laptop1);
             System.out.println("\n\na========LAPTOP1 HAS LEFT ROOM1============\n\n");
             
             for(int i=10; i<26; i++){
                 JSONObject fakeData = new JSONObject();
                 fakeData.put("ts", (long)i);
                 double lampwatts = 0;
                 if(rand.nextDouble()>0.5)
                     lampwatts += rand.nextDouble();
                 else
                     lampwatts += rand.nextDouble();
                 fakeData.put("value",lampwatts);
                 router.sendDataToParents(lamp1, fakeData.toString(), "KW");
 
                 fakeData.put("ts", (long)i+1);
                 lampwatts = 60;
                 if(rand.nextDouble()>0.5)
                     lampwatts += rand.nextDouble();
                 else
                     lampwatts += rand.nextDouble();
                 fakeData.put("value",lampwatts);
                 router.sendDataToParents(lamp2, fakeData.toString(), "KW");
 
                 //laptops produce data
                 double laptopwatts = 30;
                 fakeData.put("ts", (long)i+3);
                 laptopwatts = 30;
                 if(rand.nextDouble()>0.5)
                     laptopwatts += rand.nextDouble();
                 else
                     laptopwatts += rand.nextDouble();
                 fakeData.put("value",laptopwatts);
                 router.sendDataToParents(laptop2, fakeData.toString(), "KW");
             }
 
             router.createLink(room2, laptop1);
             System.out.println("\n\na========LAPTOP1 HAS JOINED ROOM2===========\n\n");
             for(int i=12; i<100; i+=4){
                 JSONObject fakeData = new JSONObject();
                 fakeData.put("ts", (long)i);
                 double laptopwatts = 30;
                 if(rand.nextDouble()>0.5)
                     laptopwatts += rand.nextDouble();
                 else
                     laptopwatts += rand.nextDouble();
                 fakeData.put("value",laptopwatts);
                 router.sendDataToParents(laptop1, fakeData.toString(), "KW");
             }
 
 
             
         } catch(Exception e){
             e.printStackTrace();
         }
     }
 
     //////////////////////////////////////////////////////////////////////////////////
 
 	public Router(){
         graph = new IncidenceListGraph();
 		nodeList = new ConcurrentHashMap<String, Vertex>();
         pathFinder = new PathFinder();
         
     }
 
     /*public Router(PipedInputStream in, PipedOutputStream out){
         graph = new IncidenceListGraph();
 		nodeList = new ConcurrentHashMap<String, Vertex>();
         pathFinder = new PathFinder();
 
         try {
             //create connection to piped objects
             pIncoming = in;
             pOutgoing = out;
         } catch(Exception e){
             logger.log(Level.WARNING, "", e);
             System.exit(1);
         }
     }*/
 
     public Router(String host, String port){
         //starts a router on a separate host and port
     }
 
     public void run(){
         try {
             ObjectInputStream incoming = null;
             ObjectOutputStream outgoing = null;
             ServerSocket server = new ServerSocket(9999, 100, InetAddress.getByName("localhost"));
             /*incoming = new ObjectInputStream(pIncoming);
             logger.info("ObjectInputStream created");
             //this.wait();
             outgoing = new ObjectOutputStream(pOutgoing);
             outgoing.flush();
             //Thread.sleep(2000);
             logger.info("ObjectOutputStream created");*/
 
             boolean keepRunning = true;
             while(keepRunning){
                 logger.info("Listening for incoming connections");
                 Socket s = server.accept();
                 incoming = new ObjectInputStream(s.getInputStream());
                 outgoing = new ObjectOutputStream(s.getOutputStream());
                 RouterCommand cmd = (RouterCommand) incoming.readObject();
                 RouterCommand cmdrep = null;
                 switch(cmd.type){
                     case PUSH:
                         if(cmd.sourcepath !=null && cmd.destpath !=null &&
                                 cmd.data != null && cmd.units != null)
                             sendData(cmd.sourcepath, cmd.destpath, cmd.data, cmd.units);
                         cmdrep = new RouterCommand(RouterCommand.CommandType.PUSH_ACK);
                         outgoing.writeObject(cmdrep);
                         outgoing.flush();
                         break;
                     case PULL:
			logger.info("PULL CALLED");
                         String reply = null;
                         if(cmd.lowts>0 && cmd.hights>0 && cmd.sourcepath!=null && cmd.units !=null)
                             reply = pullFromNode(cmd.sourcepath, cmd.units, 
                                         cmd.lowts, cmd.hights);
                         cmdrep = new RouterCommand(RouterCommand.CommandType.PULL_ACK);
                         cmdrep.data = reply;
                         outgoing.writeObject(cmdrep);
                         outgoing.flush();
                         break;
                     case ADD_NODE:
                         logger.info("ADD_NODE CALLED");
                         if(cmd.sourcepath != null)
                             addNodeEvent(cmd.sourcepath);
                         cmdrep = new RouterCommand(RouterCommand.CommandType.ADD_NODE_ACK);
                         outgoing.writeObject(cmdrep);
                         outgoing.flush();
                         break;
                     case REMOVE_NODE:
                         if(cmd.sourcepath != null)
                             removeNodeEvent(cmd.sourcepath);
                         cmdrep = new RouterCommand(RouterCommand.CommandType.REMOVE_NODE_ACK);
                         outgoing.writeObject(cmdrep);
                         outgoing.flush();
                         break;
                     case ADD_LINK:
			logger.info("ADD_LINK CALLED");
                         if(cmd.sourcepath !=null && cmd.destpath !=null)
                             createLink(cmd.sourcepath, cmd.destpath);
                         cmdrep = new RouterCommand(RouterCommand.CommandType.ADD_LINK_ACK);
                         outgoing.writeObject(cmdrep);
                         outgoing.flush();
                         break;
                     case REMOVE_LINK:
                         if(cmd.sourcepath !=null && cmd.destpath !=null)
                             removeLink(cmd.sourcepath, cmd.destpath);
                         cmdrep = new RouterCommand(RouterCommand.CommandType.REMOVE_LINK_ACK);
                         outgoing.writeObject(cmdrep);
                         outgoing.flush();
                         break;
                     case CREATE_AGG_PNT:
                         if(cmd.sourcepath !=null && cmd.units !=null){
                             setUnitAndTypeAtPath(cmd.sourcepath, cmd.units, 
                                     ProcType.AGGREGATE, cmd.state);
                             cmdrep = new RouterCommand(RouterCommand.
                                                 CommandType.CREATE_AGG_PNT_ACK);
                             outgoing.writeObject(cmdrep);
                             outgoing.flush();
                         }
                         break;
                     case STOP_ROUTER:
                         keepRunning = false;
                         break;
                 }
                 incoming.close();
                 outgoing.close();
             }
             
         } catch (Exception e){
             logger.log(Level.SEVERE, "", e);
             System.exit(1);
         }
         return;
     }
 
     public String pullFromNode(String path, String units, long lowts, long hights){
         return null;
     }
 
     public void setUnitAndTypeAtPath(String path, String unit, ProcType type, boolean state){
         path = cleanPath(path);
         Vertex vertex = nodeList.get(path);
         if(vertex!=null){
             Node node = (Node)vertex.get("Node");
             if(state == true)
                 node.addUnit(unit, type);
             else
                 node.removeUnitProcType(unit, type);
         }
     }
 
 	public void addNodeEvent(String path){
         path = cleanPath(path);
         //insert into graph
 	   	Vertex node = graph.insertVertex(path);
         Node thisNode = new Node(path, 16, generateObjId(), this);
         node.set("Node", thisNode);
         nodeList.put(path, node);
 	}
 
     public void addNodeEvent(String path, String linksToPath){
         path = cleanPath(path);
         linksToPath = cleanPath(linksToPath);
 
         //get or add the linksToPath node and create a link to it
         Vertex linksToVertex = nodeList.get(linksToPath);
         Node linksToNode  = null;
         String objId = null;
         if(linksToVertex == null){
             objId = generateObjId();
             linksToNode = new Node(linksToPath, 16, objId, this);
         } else {
             linksToNode = (Node)linksToVertex.get("Node");
             objId = linksToNode.getObjectId();
         }
 
         //insert into graph
 	   	Vertex node = graph.insertVertex(path);
         Node thisNode = new Node(path, 16, objId, this);
         thisNode.setSymlinkFlag(true);
         node.set("Node", thisNode);
         nodeList.put(path, node);
 	}
 
 	public void removeNodeEvent(String path){
         //clean up the path
         if((path == null) || (path != null && !path.startsWith("/")))
             return;
         path = path.replaceAll("/+", "/");
         if(!path.endsWith("/"))
             path += "/";
 
         //remove the node from the graph
         Vertex node = nodeList.get(path);
         if(node != null){
            graph.removeVertex(node);
            nodeList.remove(path);
         }
 	}
 
     public void createLink(String parentPath, String childPath){
         parentPath = cleanPath(parentPath);
         childPath = cleanPath(childPath);
         Vertex parentVertex = nodeList.get(parentPath);
         Vertex childVertex = nodeList.get(childPath);
 
         if(parentVertex == null){
             this.addNodeEvent(parentPath);
             parentVertex = nodeList.get(parentPath);
         }
 
         if(childVertex == null){
             this.addNodeEvent(childPath);
             childVertex = nodeList.get(childPath);
         }
         Edge e = graph.aConnectingEdge(parentVertex, childVertex);
         if(e == Edge.NONE){
             graph.insertDirectedEdge(parentVertex, childVertex, "edge");
            logger.info("Added edge between " + parentPath + " and " + childPath);
         }
     }
 
     public void removeLink(String source, String destination){
         String parentPath = cleanPath(source);
         String childPath = cleanPath(destination);
         Vertex parentVertex = nodeList.get(parentPath);
         Vertex childVertex = nodeList.get(childPath);
 
         if(parentVertex == null){
             this.addNodeEvent(parentPath);
             parentVertex = nodeList.get(parentPath);
         }
 
         if(childVertex == null){
             this.addNodeEvent(childPath);
             childVertex = nodeList.get(childPath);
         }
         Edge e = graph.aConnectingEdge(parentVertex, childVertex);
         if(e != Edge.NONE)
             graph.removeEdge(e);
     }
 
     private String cleanPath(String path){
         //clean up the path
         if(path == null)
             return path;
 
         if(!path.startsWith("/"))
             path = "/" + path;
         path = path.replaceAll("/+", "/");
         if(!path.endsWith("/"))
             path += "/";
         return path;
     }
 
     //Data is sent from the child to the parent only
 	public void sendData(String parentPath, String childPath, String data, 
             String unitsStr){
         parentPath = cleanPath(parentPath);
         childPath = cleanPath(childPath);
         try{
             //String unitsStr = childPropsObj.getString("units");
             //children send data to parent only
             if(this.isChild(childPath, parentPath)){
                 Vertex parentVertex = nodeList.get(parentPath);
                 Node parentNode= (Node)parentVertex.get("Node");
                 if(parentNode !=null)
                     parentNode.push(childPath, data, unitsStr);
             }
         } catch(Exception e){
             e.printStackTrace();
         }
 	}
 
     public void sendDataToParents(String childPath, String data, String unitsLabel){
         childPath= cleanPath(childPath);
         Vertex childVertex = nodeList.get(childPath);
         if(childVertex != null){
             EdgeIterator edgeIt = graph.incidentEdges(childVertex, EdgeDirection.IN);
             while(edgeIt.hasNext()){
                 Edge e = edgeIt.nextEdge();
                 Vertex parent = graph.origin(e);
                 Node parentNode = (Node)parent.get("Node");
                 if(parentNode != null)
                     parentNode.push(childPath, data, unitsLabel);
             }
         }
     }
 
     public boolean isChild(String childPath, String parentPath){
         childPath = cleanPath(childPath);
         parentPath = cleanPath(parentPath);
         Vertex childV = nodeList.get(childPath);
         Vertex parentV = nodeList.get(parentPath);
 
         if(childV != null && parentV != null &&
                 graph.areAdjacent(childV, parentV)){
 
             pathFinder.execute(graph, parentV, childV);
             if(pathFinder.pathExists() && pathFinder.distance(childV)==1)
                 return true;
             return false;
         }
         return false;
     }
 
     private class PathFinder extends IntegerDijkstraPathfinder{
         public PathFinder(){
             super();
         }
 
         protected int weight(Edge e){
             return 1;
         }
     }
 
     private String generateObjId(){
         String id = UUID.randomUUID().toString();
         return id.substring(0, id.length()/2);
     }
 
 }
