 package coeditor;
 
 import java.io.IOException;
 import java.nio.ByteBuffer;
 import java.nio.CharBuffer;
 import java.util.Hashtable;
 import java.util.Set;
 import java.util.Map;
 import java.util.concurrent.CopyOnWriteArraySet;
 import java.util.concurrent.atomic.AtomicInteger;
 
 import javax.servlet.http.HttpServletRequest;
 
 import org.apache.catalina.websocket.MessageInbound;
 import org.apache.catalina.websocket.StreamInbound;
 import org.apache.catalina.websocket.WebSocketServlet;
 import org.apache.catalina.websocket.WsOutbound;
 
 import com.google.gson.Gson;
 
 import db.Config;
 import storage.KeyValueStore;
 
 public class CoeditorWebSocketServlet extends WebSocketServlet {
 
   private final Gson gson = new Gson();
 
   private final KeyValueStore s3 = new KeyValueStore(Config.bucketName);
 
   private static final long serialVersionUID = 1L;
   
   private final AtomicInteger connectionIds = new AtomicInteger(0);
 
   private final Map<Integer, CoeditorMessageInbound> connections = new Hashtable<Integer, CoeditorMessageInbound>();
 
   private final Map<String, Document> documents = new Hashtable<String, Document>();
   
   @Override
   protected StreamInbound createWebSocketInbound(String subProtocol, HttpServletRequest request) {
       return new CoeditorMessageInbound(connectionIds.incrementAndGet());
   }
 
   private final class CoeditorMessageInbound extends MessageInbound {
 
       private final int connectionId;
       private Document document;
     
       private CoeditorMessageInbound(int id) {
         this.connectionId = id;
       }
 
       @Override
       protected void onOpen(WsOutbound outbound) {
 
         connections.put(connectionId, this);
 
       }
 
       @Override
       protected void onClose(int status) {
           
         closeDocument();
         
         connections.remove(this);
       }
 
       @Override
       protected void onBinaryMessage(ByteBuffer message) throws IOException {
           throw new UnsupportedOperationException("Binary message not supported.");
       }
 
       @Override
       protected void onTextMessage(CharBuffer message) throws IOException {
         Message msg = gson.fromJson(message.toString(), Message.class);
 
         String clientId = msg.clientId;
         String action = msg.action;
         
         if (action != null && action.equals("create")) {
           // step 1: open document for client
           String docId = msg.content;
           if (createDocument(docId, clientId)) {
           // step 2: send back HEADTEXT
             String headText = documents.get(docId).headText;
             System.out.println("Headtext: " + headText);
             ChangeSet testChangeSet = new ChangeSet(headText);
 
             Message response = new Message("server", "response", gson.toJson(testChangeSet));
             sendMessage(gson.toJson(response));
 
           } else {
 
             System.out.println("[open] error: file cannot be created.");
 
             sendErrorMessage("[open] error: file cannot be created.");
           }
         }
         else if (action != null && action.equals("open")) {
           // step 1: open document for client
           String docId = msg.content;
           if (openDocument(docId, clientId)) {
           // step 2: send back HEADTEXT
             String headText = documents.get(docId).headText;
             System.out.println("Headtext: " + headText);
             ChangeSet testChangeSet = new ChangeSet(headText);
 
             Message response = new Message("server", "open", gson.toJson(testChangeSet));
             sendMessage(gson.toJson(response));
 
           } else {
 
             System.out.println("[open] error: file cannot be opened.");
 
             sendErrorMessage("[open] error: file cannot be opened.");
           }
         } else if (action != null && action.equals("close")) {
           
           closeDocument();
           
           Message response = new Message("server", "close", "close");
           sendMessage(gson.toJson(response));
         
         } else if (action != null && action.equals("save")) {
           
           saveDocument();
           
           Message response = new Message("server", "save", "");
           sendMessage(gson.toJson(response));
         
         } else if (action != null && action.equals("delete")) {
           String docId = msg.content;
 
           System.out.println("hhahaa");
           
         	deleteDocument(docId);
         	
         } else if (action != null && action.equals("newChange")) {
          
           ChangeSet newChange = gson.fromJson(msg.content, ChangeSet.class);
 
           Client client = document.activeUsers.get(connectionId);
           
           ChangeSet newChangePrime = this.document.applyChangeSet(newChange, client.latestVersion);
           
           Message syncMsg = new Message("server", "sync", gson.toJson(newChangePrime));
           broadcast(gson.toJson(syncMsg));
           
           Message ackMsg = new Message("server", "ACK", "ACK");
           sendMessage(gson.toJson(ackMsg));
           
           document.addRevisionRecord(clientId, newChangePrime);
 
         } else if (action != null && action.equals("getActiveUsers")) {
         	String docId = msg.content;
         	System.out.println(getActiveUsers(docId));
         	Message response = new Message("server", "activeUsers", getActiveUsers(docId));
         	sendMessage(gson.toJson(response));
         }
       	else {
           System.out.println("[new message] error: no action");
           sendErrorMessage(action + " is not supported");
         }
       }
 
       private String getActiveUsers(String docId) {
       	if (document.docId.equals(docId)) {
 	      	boolean first = true;
 	      	String result = "[";
 	      	for (Client c : document.activeUsers.values()) {
 	      		if (first)
 	      			first = false;
 	      		else
 	      			result += ",";
 	      		result = result + "\"" + c.clientId + "\"";
 	      	}
 	      	result += "]";
 	      	
 	      	return result;
       	}
       	else return "[]";
       }
       
       private boolean openDocument(String docId, String clientId) {
 
         if (!documents.containsKey(docId)) {
           document = new Document(docId, s3);
           documents.put(docId, this.document);
         }
         else {
           document = documents.get(docId);
         }
 
         document.open(connectionId, clientId);
         
         return true;
       }
       
       private boolean closeDocument() {
         
         if (document != null && document.isOpen) {
           this.document.close(connectionId);
           return true;
         }
         else
           return false;
       }
       
       private boolean saveDocument() {
         this.document.save();
         return true;
       }
       
       private boolean deleteDocument(String docId) {
       	
       	if (document != null && document.docId.equals(docId))
       		closeDocument();
       	
       	System.out.println("delete");
       	
       	s3.deleteKey(docId);
       	
       	return true;
       }
 
       private boolean createDocument(String docId, String clientId) {
         this.document = new Document(docId, s3);
         documents.put(docId, this.document);
         
         this.document.create(connectionId, clientId);
         
         return true;
       }
 
       private void sendMessage(String message) {
         CharBuffer buffer = CharBuffer.wrap(message);
         try {
           this.getWsOutbound().writeTextMessage(buffer);
         } catch (IOException e) {
           // TODO Auto-generated catch block
           e.printStackTrace();
         }
       }
 
       private void sendErrorMessage(String message) {
         Message errorMsg = new Message("server", "error", message);
         sendMessage(gson.toJson(errorMsg));
       }
       
       private void broadcast(String message) {
         Set<Integer> targets = this.document.getActiveUser();
         for (int t : targets) {
         	if (t != connectionId)
         		connections.get(t).sendMessage(message);
         }
       }
   }
 }
