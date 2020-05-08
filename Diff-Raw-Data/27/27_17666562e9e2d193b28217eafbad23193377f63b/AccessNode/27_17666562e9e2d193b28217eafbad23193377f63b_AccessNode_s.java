 package voldemort.store.parser;
 
 import java.util.ArrayList;
 
 import voldemort.utils.ByteArray;
 import voldemort.versioning.VectorClock;
 
 public class AccessNode {
 
     private AccessType op;
     private String client;
     private ByteArray key;
     private VectorClock version;
     private ArrayList<AccessNode> successors;
     private ClientNode clientNode;
     private boolean dirty;
     private long timestamp;
 
     public AccessNode(AccessType op,
                       String client,
                       ByteArray key,
                       VectorClock version,
                       long timestamp) {
         this.op = op;
         this.client = client;
         this.key = key;
         this.version = version;
         this.timestamp = timestamp;
         this.dirty = false;
     }
 
     public boolean addSuccessor(AccessNode node) {
         if(!key.equals(node.key))
             return false;
 
         if(op == AccessType.SET && node.op == AccessType.GET) {
             if(timestamp < node.timestamp) {
                 successors.add(node);
                 return true;
             }
         }
 
         return false;
     }
 
     public boolean addClientNode(ClientNode node) {
         if(op == AccessType.GET && node.getClient().equals(client)) {
             clientNode = node;
             return true;
         }
         return false;
     }
 
     public void setDirty() {
         if(dirty == true)
             return;
         dirty = true;
         if(op == AccessType.SET) {
             for(AccessNode n: successors) {
                 n.setDirty();
             }
         }
 
         if(op == AccessType.GET) {
             clientNode.setVersion(version);
         }
     }
 
     public boolean isDirty() {
         return dirty;
     }
 
     public String getClient() {
         return client;
     }
 
     public AccessType getOpType() {
         return op;
     }
 
     public VectorClock getVersion() {
         return version;
     }
 
     public void accept(GraphVisitor visitor) {
         visitor.visit(this);
     }
 }
