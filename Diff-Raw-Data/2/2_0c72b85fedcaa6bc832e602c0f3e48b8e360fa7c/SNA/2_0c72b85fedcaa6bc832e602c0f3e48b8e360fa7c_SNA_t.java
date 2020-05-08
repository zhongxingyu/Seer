 package sna;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.StringReader;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.jdom2.Document;
 import org.jdom2.Element;
 import org.jdom2.JDOMException;
 import org.jdom2.input.SAXBuilder;
 import org.neo4j.graphalgo.impl.centrality.BetweennessCentrality;
 import org.neo4j.graphalgo.impl.centrality.ClosenessCentrality;
 import org.neo4j.graphalgo.impl.centrality.EigenvectorCentralityPower;
 import org.neo4j.graphalgo.impl.centrality.ParallellCentralityCalculation;
 import org.neo4j.graphalgo.impl.shortestpath.*;
 import org.neo4j.graphalgo.impl.util.DoubleAdder;
 import org.neo4j.graphalgo.impl.util.IntegerAdder;
 import org.neo4j.graphdb.*;
 import org.neo4j.graphdb.factory.GraphDatabaseFactory;
 import org.neo4j.graphdb.index.Index;
 import org.neo4j.tooling.GlobalGraphOperations;
 import org.xml.sax.InputSource;
 
 public class SNA {
 
     public static final String BASE_URI = "https://api.vk.com/method/";
     private static final int MAX_LEVEL = 1;
     private static final String DB_PATH = "GroupDB";
     private static final String FRIEND_LIST_KEY = "friend_list";
     private static final GraphDatabaseService graphDb = new GraphDatabaseFactory().newEmbeddedDatabase(DB_PATH);
     private static Index<Node> nodeIndex;
 
     private static enum RelTypes implements RelationshipType {
 
         FRIEND
     }
 
     private static void registerShutdownHook(final GraphDatabaseService graphDb) {
         Runtime.getRuntime().addShutdownHook(new Thread() {
             @Override
             public void run() {
                 graphDb.shutdown();
             }
         });
     }
 
     public static String requestGET(String address) {
         String result = "";
         try {
             URL url = new URL(address);
             HttpURLConnection conn = (HttpURLConnection) url.openConnection();
             conn.setRequestProperty("User-Agent", "Java bot");
             conn.connect();
             int code = conn.getResponseCode();
             if (code == 200) {
                 BufferedReader in = new BufferedReader(new InputStreamReader(
                         conn.getInputStream()));
                 String inputLine;
                 while ((inputLine = in.readLine()) != null) {
                     result += inputLine;
                 }
                 in.close();
             }
             conn.disconnect();
             conn = null;
         } catch (Exception e) {
             e.printStackTrace();
         }
         return result;
     }
 
     public static String[] getPersonFriends(String uid) {
         String response = requestGET("https://api.vk.com/method/friends.get?uid=" + uid);
         int start = response.indexOf('[');
         int end = response.indexOf(']');
         String uidsStr = response.substring(start + 1, end);
         return uidsStr.split(",");
     }
 
     public static String getPersonXMLData(String id) {
         StringBuilder request = new StringBuilder(BASE_URI);
         request = request.append("users.get.xml?uid=");
         request = request.append(id);
         request = request.append("&fields=");
         request = request.append("sex");
         request = request.append(",bdate");
         request = request.append(",city");
         request = request.append(",can_post");
         request = request.append(",status");
         request = request.append(",relation");
         request = request.append(",nickname");
         request = request.append(",relatives");
         request = request.append(",activities");
         request = request.append(",interests");
         request = request.append(",movies");
         request = request.append(",tv");
         request = request.append(",books");
         request = request.append(",games");
         request = request.append(",about");
         request = request.append(",personal");
         String response = requestGET(request.toString());
         return response;
     }
 
     public static String getOneField(String name, Element e) {
         if (e.getChild(name) != null) {
             String temp = e.getChild(name).getText().replaceAll("\'", "");
             if (!temp.equals("")) {
                 return temp;
             }
         }
         return "?";
     }
 
     public static HashMap<String, Object> parsePersonXML(String xml) {
         HashMap<String, Object> personData = new HashMap<String, Object>();
         try {
             SAXBuilder builder = new SAXBuilder();
             Document doc = builder.build(new InputSource(new StringReader(xml)));
             Element root = doc.getRootElement();
             Element user = root.getChild("user");
 
             personData.put("uid", getOneField("uid", user));
             personData.put("first_name", getOneField("first_name", user));
             personData.put("last_name", getOneField("last_name", user));
             personData.put("sex", getOneField("sex", user));
             personData.put("bdate", getOneField("bdate", user));
             personData.put("city", getOneField("city", user));
             personData.put("can_post", getOneField("can_post", user));
             personData.put("status", getOneField("status", user));
             personData.put("relation", getOneField("relation", user));
             personData.put("nickname", getOneField("nickname", user));
             personData.put("interests", getOneField("interests", user));
             personData.put("movies", getOneField("movies", user));
             personData.put("tv", getOneField("tv", user));
             personData.put("books", getOneField("books", user));
             personData.put("games", getOneField("games", user));
             personData.put("about", getOneField("about", user));
         } catch (JDOMException ex) {
             Logger.getLogger(SNA.class.getName()).log(Level.SEVERE, null, ex);
         } catch (IOException ex) {
             Logger.getLogger(SNA.class.getName()).log(Level.SEVERE, null, ex);
         }
         return personData;
     }
 
     public static void setRelationship(Node tempPerson, Node parentPerson) {
         Iterable<Relationship> relationships = tempPerson.getRelationships(Direction.BOTH);
         boolean relationAlreadyExist = false;
         for (Relationship tempRel : relationships) {
             if (tempRel.getOtherNode(tempPerson).getId() == parentPerson.getId()) {
                 relationAlreadyExist = true;
                 break;
             }
         }
         if (!relationAlreadyExist) {
             Transaction tx = graphDb.beginTx();
             try {
                 parentPerson.createRelationshipTo(tempPerson, RelTypes.FRIEND);
                 tx.success();
             } finally {
                 tx.finish();
             }
         }
     }
 
     public static Node addToDB(HashMap<String, Object> personProperties) {
         Node tempPerson = null;
         Node existingNode = nodeIndex.get("uid", personProperties.get("uid")).getSingle();
         Transaction tx = graphDb.beginTx();
         try {
             if (existingNode == null) {
                 tempPerson = graphDb.createNode();
                 Iterator<String> keySetIterator = personProperties.keySet().iterator();
                 while (keySetIterator.hasNext()) {
                     String key = keySetIterator.next();
                     tempPerson.setProperty(key, personProperties.get(key));
                 }
                 nodeIndex.add(tempPerson, "uid", personProperties.get("uid"));
                 tx.success();
             }
         } finally {
             tx.finish();
         }
         return tempPerson;
     }
 
     public static Node addToDB(HashMap<String, Object> personProperties, String parentUid) {
         Node tempPerson = addToDB(personProperties);
         Node parentPerson = nodeIndex.get("uid", parentUid).getSingle();
         Transaction tx = graphDb.beginTx();
         try {
             if (tempPerson == null) {
                 tempPerson = nodeIndex.get("uid", personProperties.get("uid")).getSingle();
             }
             setRelationship(tempPerson, parentPerson);
             tx.success();
         } finally {
             tx.finish();
         }
         return tempPerson;
     }
 
     public static void downloadData(String beginUid) {
         HashMap<String, Object> personData = parsePersonXML(getPersonXMLData(beginUid));
         String[] friendUids = getPersonFriends(beginUid);
         personData.put(FRIEND_LIST_KEY, friendUids);
         addToDB(personData);
         for (String tempFriend : friendUids) {
             recDownloadData(tempFriend, beginUid, 1);
         }
     }
 
     public static void recDownloadData(String tempUid, String parentUid, int lvl) {
         if (lvl == MAX_LEVEL) {
             return;
         }
         System.out.println("Parsing uid = " + tempUid + " parentUid = " + parentUid);
         String[] friendsUids = getPersonFriends(tempUid);
         Node existingNode = nodeIndex.get("uid", tempUid).getSingle();
         if (existingNode == null) {
             HashMap<String, Object> personData = parsePersonXML(getPersonXMLData(tempUid));
             System.out.println(" first_name = " + personData.get("first_name") + " last_name = " + personData.get("last_name") + " lvl = " + lvl);
             personData.put(FRIEND_LIST_KEY, friendsUids);
             existingNode = addToDB(personData, parentUid);
         } else {
             setRelationship(existingNode, nodeIndex.get("uid", parentUid).getSingle());
         }
         for (String tempFriend : friendsUids) {
             recDownloadData(tempFriend, tempUid, lvl + 1);
         }
     }
 
     public static void downloadGroupMembers(String gid){
        String response = requestGET("https://api.vk.com/method/groups.getMembers?gid=" + gid);
         int start = response.indexOf('[');
         int end = response.indexOf(']');
         String uidsStr = response.substring(start + 1, end);
         String[] ids =  uidsStr.split(",");
         for(String id : ids){
             downloadData(id);
         }
     }
     
     public static void calculateMetrics() {
         SingleSourceShortestPathBFS BFS;
         BFS = new SingleSourceShortestPathBFS(null, Direction.BOTH, RelTypes.FRIEND);
         HashSet<Node> nodes = new HashSet<Node>();
         GlobalGraphOperations glOper = GlobalGraphOperations.at(graphDb);
         Iterator<Node> allNodes = glOper.getAllNodes().iterator();
         while (allNodes.hasNext()) {
             nodes.add(allNodes.next());
         }
         BetweennessCentrality betweenness;
         betweenness = new BetweennessCentrality(BFS, nodes);
         betweenness.calculate();
 
         EigenvectorCentralityPower eigenvector;
         eigenvector = new EigenvectorCentralityPower(Direction.BOTH, null, nodes, null, 0.01);
         eigenvector.calculate();
         
         ClosenessCentrality closeness;
         closeness = new ClosenessCentrality(BFS, null, null, nodes, null);
         closeness.calculate();
         
         /*ParallellCentralityCalculation shortestPathCalculations = new ParallellCentralityCalculation(BFS, nodes);
         shortestPathCalculations.addCalculation(betweenness);
         shortestPathCalculations.addCalculation(closeness);
         shortestPathCalculations.calculate();*/
     }
 
     public static void setAllRelations() {
         Transaction tx = graphDb.beginTx();
         try {
             GlobalGraphOperations glOper = GlobalGraphOperations.at(graphDb);
             Iterator<Node> allNodes = glOper.getAllNodes().iterator();
             while (allNodes.hasNext()) {
                 setOneNodeRelations(allNodes.next());
             }
             tx.success();
         } finally {
             tx.finish();
         }
     }
 
     public static void setOneNodeRelations(Node tempNode) {
         if (tempNode.hasProperty(FRIEND_LIST_KEY)) {
             String[] friendsUidList = (String[]) tempNode.getProperty(FRIEND_LIST_KEY);
             for (String tempFriendUid : friendsUidList) {
                 Node tempFriend = nodeIndex.get("uid", tempFriendUid).getSingle();
                 if (tempFriend == null) {
                     continue;
                 }
                 Iterable<Relationship> relationships = tempFriend.getRelationships(Direction.BOTH);
                 boolean relationAlreadyExist = false;
                 for (Relationship tempRel : relationships) {
                     if (tempRel.getEndNode().getId() == tempNode.getId()) {
                         relationAlreadyExist = true;
                         break;
                     }
                 }
                 if (!relationAlreadyExist) {
                     tempNode.createRelationshipTo(tempFriend, RelTypes.FRIEND);
                 }
             }
         }
     }
     
     public static void main(String[] args) {
         registerShutdownHook(graphDb);
         nodeIndex = graphDb.index().forNodes("uids");
         //dowloadData("86030925");
         downloadGroupMembers("29899098");
         setAllRelations();
         //ExecutionEngine engine = new ExecutionEngine(graphDb);
         //ExecutionResult result = engine.execute("start n=node(*), m = node(*) where id(n) <> 0 and id(m) <> 0 and n.uid = m.uid return n.id, m.id");
         //System.out.println(result.toString());
         calculateMetrics();
 
     }
 }
