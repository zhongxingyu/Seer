 package voldemort.store.parser;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import voldemort.versioning.ClockEntry;
 import voldemort.versioning.Occured;
 import voldemort.versioning.VectorClock;
 
 public class ClientNode {
 
     private String client;
     private VectorClock version;
     private ArrayList<AccessNode> accesses;
 
     public ClientNode(String client) {
         this.client = client;
     }
 
     public String getClient() {
         return client;
     }
 
     public void addAccess(AccessNode node) {
         if(client.equals(node.getClient()) && node.getOpType() == AccessType.SET) {
             accesses.add(node);
         }
     }
 
     public void setVersion(VectorClock v) {
         List<ClockEntry> versions = v.getEntries();
         List<ClockEntry> clientVersions = version.getEntries();
         List<ClockEntry> newVersions = new ArrayList<ClockEntry>();
         int i = 0;
         int j = 0;
         while(i < versions.size() && j < clientVersions.size()) {
             ClockEntry v1 = versions.get(i);
             ClockEntry v2 = clientVersions.get(j);
             if(v1.getNodeId() == v2.getNodeId()) {
                 newVersions.add(new ClockEntry(v1.getNodeId(), Math.min(v1.getVersion(),
                                                                         v2.getVersion())));
                 i++;
                 j++;
             } else if(v1.getNodeId() < v2.getNodeId()) {
                 newVersions.add(v1.clone());
                 i++;
             } else {
                 newVersions.add(v2.clone());
                 j++;
             }
         }
 
         for(int k = i; k < versions.size(); k++)
             newVersions.add(versions.get(k).clone());
         for(int k = j; k < versions.size(); k++)
             newVersions.add(clientVersions.get(k).clone());
 
         version = new VectorClock(newVersions, System.currentTimeMillis());
 
         for(AccessNode node: accesses) {
             if(node.getVersion().compare(version) == Occured.AFTER) {
                 node.setDirty();
             }
         }
     }
 
     public void accept(GraphVisitor visitor) {
         visitor.visit(this);
     }
 }
