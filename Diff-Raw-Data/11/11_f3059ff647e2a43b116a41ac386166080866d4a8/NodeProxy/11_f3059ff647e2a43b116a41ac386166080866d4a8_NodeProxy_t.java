 package command;
 
 import hypeerweb.Connections;
 import hypeerweb.HyPeerWeb;
 import hypeerweb.Node;
 //import GlobalObjectId;
 public class NodeProxy
     extends Node implements java.io.Serializable
 {
     private GlobalObjectId globalObjectId;
 
     public NodeProxy(GlobalObjectId globalObjectId){
         this.globalObjectId = globalObjectId;
     }
 
     public int compareTo(hypeerweb.Node p0){
         String[] parameterTypeNames = new String[1];
         parameterTypeNames[0] = "hypeerweb.Node";
         Object[] actualParameters = new Object[1];
         actualParameters[0] = p0;
         Command command = new Command(globalObjectId.getLocalObjectId(), "hypeerweb.Node", "compareTo", parameterTypeNames, actualParameters, true);
         Object result = PeerCommunicator.getSingleton().sendSynchronous(globalObjectId, command);
         return (Integer)result;
     }
     
     /*public int compareTo(NodeProxy p0){
         String[] parameterTypeNames = new String[1];
         parameterTypeNames[0] = "command.NodeProxy";
         Object[] actualParameters = new Object[1];
         actualParameters[0] = p0;
         Command command = new Command(globalObjectId.getLocalObjectId(), "hypeerweb.Node", "compareTo", parameterTypeNames, actualParameters, true);
         Object result = PeerCommunicator.getSingleton().sendSynchronous(globalObjectId, command);
         return (Integer)result;
     }*/
 
     /*public volatile int compareTo(java.lang.Object p0){
         String[] parameterTypeNames = new String[1];
         parameterTypeNames[0] = "java.lang.Object";
         Object[] actualParameters = new Object[1];
         actualParameters[0] = p0;
         Command command = new Command(globalObjectId.getLocalObjectId(), "hypeerweb.Node", "compareTo", parameterTypeNames, actualParameters, true);
         Object result = PeerCommunicator.getSingleton().sendSynchronous(globalObjectId, command);
         return (Integer)result;
     }*/
     
 
     public void accept(hypeerweb.Visitor p0, hypeerweb.Parameters p1){
         /*String[] parameterTypeNames = new String[2];
         parameterTypeNames[0] = "interface hypeerweb.Visitor";
         parameterTypeNames[1] = "hypeerweb.Parameters";
         Object[] actualParameters = new Object[2];
         actualParameters[0] = p0;
         actualParameters[1] = p1;
         Command command = new Command(globalObjectId.getLocalObjectId(), "hypeerweb.Node", "accept", parameterTypeNames, actualParameters, false);
         PeerCommunicator.getSingleton().sendASynchronous(globalObjectId, command);*/
         p0.visit(this, p1);
     }
 
     public void setState(hypeerweb.Node.State p0){
         String[] parameterTypeNames = new String[1];
         parameterTypeNames[0] = "hypeerweb.Node$State";
         Object[] actualParameters = new Object[1];
         actualParameters[0] = p0;
         Command command = new Command(globalObjectId.getLocalObjectId(), "hypeerweb.Node", "setState", parameterTypeNames, actualParameters, false);
         PeerCommunicator.getSingleton().sendASynchronous(globalObjectId, command);
     }
 
     public void setFold(hypeerweb.Node p0){
         String[] parameterTypeNames = new String[1];
         parameterTypeNames[0] = "hypeerweb.Node";
         Object[] actualParameters = new Object[1];
         actualParameters[0] = p0;
         Command command = new Command(globalObjectId.getLocalObjectId(), "hypeerweb.Node", "setFold", parameterTypeNames, actualParameters, false);
         PeerCommunicator.getSingleton().sendASynchronous(globalObjectId, command);
     }
 
     public hypeerweb.Contents getContents(){
         String[] parameterTypeNames = new String[0];
         Object[] actualParameters = new Object[0];
         Command command = new Command(globalObjectId.getLocalObjectId(), "hypeerweb.Node", "getContents", parameterTypeNames, actualParameters, true);
         Object result = PeerCommunicator.getSingleton().sendSynchronous(globalObjectId, command);
         return (hypeerweb.Contents)result;
     }
 
     public hypeerweb.Node getHighestNeighbor(){
         String[] parameterTypeNames = new String[0];
         Object[] actualParameters = new Object[0];
         Command command = new Command(globalObjectId.getLocalObjectId(), "hypeerweb.Node", "getHighestNeighbor", parameterTypeNames, actualParameters, true);
         Object result = PeerCommunicator.getSingleton().sendSynchronous(globalObjectId, command);
         return (hypeerweb.Node)result;
     }
 
     public hypeerweb.Node getLowestNeighbor(){
         String[] parameterTypeNames = new String[0];
         Object[] actualParameters = new Object[0];
         Command command = new Command(globalObjectId.getLocalObjectId(), "hypeerweb.Node", "getLowestNeighbor", parameterTypeNames, actualParameters, true);
         Object result = PeerCommunicator.getSingleton().sendSynchronous(globalObjectId, command);
         return (hypeerweb.Node)result;
     }
 
     public hypeerweb.Node getHighestSurrogateNeighbor(){
         String[] parameterTypeNames = new String[0];
         Object[] actualParameters = new Object[0];
         Command command = new Command(globalObjectId.getLocalObjectId(), "hypeerweb.Node", "getHighestSurrogateNeighbor", parameterTypeNames, actualParameters, true);
         Object result = PeerCommunicator.getSingleton().sendSynchronous(globalObjectId, command);
         return (hypeerweb.Node)result;
     }
 
     public hypeerweb.SimplifiedNodeDomain constructSimplifiedNodeDomain(){
         String[] parameterTypeNames = new String[0];
         Object[] actualParameters = new Object[0];
         Command command = new Command(globalObjectId.getLocalObjectId(), "hypeerweb.Node", "constructSimplifiedNodeDomain", parameterTypeNames, actualParameters, true);
         Object result = PeerCommunicator.getSingleton().sendSynchronous(globalObjectId, command);
         return (hypeerweb.SimplifiedNodeDomain)result;
     }
 
     public hypeerweb.Node getFold(){
         String[] parameterTypeNames = new String[0];
         Object[] actualParameters = new Object[0];
         Command command = new Command(globalObjectId.getLocalObjectId(), "hypeerweb.Node", "getFold", parameterTypeNames, actualParameters, true);
         Object result = PeerCommunicator.getSingleton().sendSynchronous(globalObjectId, command);
         return (hypeerweb.Node)result;
     }
 
     public int getHeight(){
         String[] parameterTypeNames = new String[0];
         Object[] actualParameters = new Object[0];
         Command command = new Command(globalObjectId.getLocalObjectId(), "hypeerweb.Node", "getHeight", parameterTypeNames, actualParameters, true);
         Object result = PeerCommunicator.getSingleton().sendSynchronous(globalObjectId, command);
         return (Integer)result;
     }
 
     public void setWebId(hypeerweb.WebId p0){
         String[] parameterTypeNames = new String[1];
         parameterTypeNames[0] = "hypeerweb.WebId";
         Object[] actualParameters = new Object[1];
         actualParameters[0] = p0;
         Command command = new Command(globalObjectId.getLocalObjectId(), "hypeerweb.Node", "setWebId", parameterTypeNames, actualParameters, false);
         PeerCommunicator.getSingleton().sendASynchronous(globalObjectId, command);
     }
 
     public void setSurrogateFold(hypeerweb.Node p0){
         String[] parameterTypeNames = new String[1];
         parameterTypeNames[0] = "hypeerweb.Node";
         Object[] actualParameters = new Object[1];
         actualParameters[0] = p0;
         Command command = new Command(globalObjectId.getLocalObjectId(), "hypeerweb.Node", "setSurrogateFold", parameterTypeNames, actualParameters, false);
         PeerCommunicator.getSingleton().sendASynchronous(globalObjectId, command);
     }
 
     public void setInverseSurrogateFold(hypeerweb.Node p0){
         String[] parameterTypeNames = new String[1];
         parameterTypeNames[0] = "hypeerweb.Node";
         Object[] actualParameters = new Object[1];
         actualParameters[0] = p0;
         Command command = new Command(globalObjectId.getLocalObjectId(), "hypeerweb.Node", "setInverseSurrogateFold", parameterTypeNames, actualParameters, false);
         PeerCommunicator.getSingleton().sendASynchronous(globalObjectId, command);
     }
 
     public void addNeighbor(hypeerweb.Node p0){
         String[] parameterTypeNames = new String[1];
         parameterTypeNames[0] = "hypeerweb.Node";
         Object[] actualParameters = new Object[1];
         actualParameters[0] = p0;
         Command command = new Command(globalObjectId.getLocalObjectId(), "hypeerweb.Node", "addNeighbor", parameterTypeNames, actualParameters, false);
         PeerCommunicator.getSingleton().sendASynchronous(globalObjectId, command);
     }
 
     public void removeNeighbor(hypeerweb.Node p0){
         String[] parameterTypeNames = new String[1];
         parameterTypeNames[0] = "hypeerweb.Node";
         Object[] actualParameters = new Object[1];
         actualParameters[0] = p0;
         Command command = new Command(globalObjectId.getLocalObjectId(), "hypeerweb.Node", "removeNeighbor", parameterTypeNames, actualParameters, false);
         PeerCommunicator.getSingleton().sendASynchronous(globalObjectId, command);
     }
 
     public int getWebId(){
         String[] parameterTypeNames = new String[0];
         Object[] actualParameters = new Object[0];
         Command command = new Command(globalObjectId.getLocalObjectId(), "hypeerweb.Node", "getWebId", parameterTypeNames, actualParameters, true);
         Object result = PeerCommunicator.getSingleton().sendSynchronous(globalObjectId, command);
         return (Integer)result;
     }
 
     public void addUpPointer(hypeerweb.Node p0){
         String[] parameterTypeNames = new String[1];
         parameterTypeNames[0] = "hypeerweb.Node";
         Object[] actualParameters = new Object[1];
         actualParameters[0] = p0;
         Command command = new Command(globalObjectId.getLocalObjectId(), "hypeerweb.Node", "addUpPointer", parameterTypeNames, actualParameters, false);
         PeerCommunicator.getSingleton().sendASynchronous(globalObjectId, command);
     }
 
     public void removeUpPointer(hypeerweb.Node p0){
         String[] parameterTypeNames = new String[1];
         parameterTypeNames[0] = "hypeerweb.Node";
         Object[] actualParameters = new Object[1];
         actualParameters[0] = p0;
         Command command = new Command(globalObjectId.getLocalObjectId(), "hypeerweb.Node", "removeUpPointer", parameterTypeNames, actualParameters, false);
         PeerCommunicator.getSingleton().sendASynchronous(globalObjectId, command);
     }
 
     public void addDownPointer(hypeerweb.Node p0){
         String[] parameterTypeNames = new String[1];
         parameterTypeNames[0] = "hypeerweb.Node";
         Object[] actualParameters = new Object[1];
         actualParameters[0] = p0;
         Command command = new Command(globalObjectId.getLocalObjectId(), "hypeerweb.Node", "addDownPointer", parameterTypeNames, actualParameters, false);
         PeerCommunicator.getSingleton().sendASynchronous(globalObjectId, command);
     }
 
     public void removeDownPointer(hypeerweb.Node p0){
         String[] parameterTypeNames = new String[1];
         parameterTypeNames[0] = "hypeerweb.Node";
         Object[] actualParameters = new Object[1];
         actualParameters[0] = p0;
         Command command = new Command(globalObjectId.getLocalObjectId(), "hypeerweb.Node", "removeDownPointer", parameterTypeNames, actualParameters, false);
         PeerCommunicator.getSingleton().sendASynchronous(globalObjectId, command);
     }
 
     public java.util.HashSet getNeighborsIds(){
         String[] parameterTypeNames = new String[0];
         Object[] actualParameters = new Object[0];
         Command command = new Command(globalObjectId.getLocalObjectId(), "hypeerweb.Node", "getNeighborsIds", parameterTypeNames, actualParameters, true);
         Object result = PeerCommunicator.getSingleton().sendSynchronous(globalObjectId, command);
         return (java.util.HashSet)result;
     }
 
     public java.util.HashSet getSurNeighborsIds(){
         String[] parameterTypeNames = new String[0];
         Object[] actualParameters = new Object[0];
         Command command = new Command(globalObjectId.getLocalObjectId(), "hypeerweb.Node", "getSurNeighborsIds", parameterTypeNames, actualParameters, true);
         Object result = PeerCommunicator.getSingleton().sendSynchronous(globalObjectId, command);
         return (java.util.HashSet)result;
     }
 
     public java.util.HashSet getInvSurNeighborsIds(){
         String[] parameterTypeNames = new String[0];
         Object[] actualParameters = new Object[0];
         Command command = new Command(globalObjectId.getLocalObjectId(), "hypeerweb.Node", "getInvSurNeighborsIds", parameterTypeNames, actualParameters, true);
         Object result = PeerCommunicator.getSingleton().sendSynchronous(globalObjectId, command);
         return (java.util.HashSet)result;
     }
 
     public boolean insertSelf(hypeerweb.Node p0){
         String[] parameterTypeNames = new String[1];
         parameterTypeNames[0] = "hypeerweb.Node";
         Object[] actualParameters = new Object[1];
         actualParameters[0] = p0;
         Command command = new Command(globalObjectId.getLocalObjectId(), "hypeerweb.Node", "insertSelf", parameterTypeNames, actualParameters, false);
         Object result = PeerCommunicator.getSingleton().sendSynchronous(globalObjectId, command);
         return (Boolean) result;
     }
 
     public hypeerweb.Node findCapNode(hypeerweb.Node p0){
         String[] parameterTypeNames = new String[1];
         parameterTypeNames[0] = "hypeerweb.Node";
         Object[] actualParameters = new Object[1];
         actualParameters[0] = p0;
         Command command = new Command(globalObjectId.getLocalObjectId(), "hypeerweb.Node", "findCapNode", parameterTypeNames, actualParameters, true);
         Object result = PeerCommunicator.getSingleton().sendSynchronous(globalObjectId, command);
         return (hypeerweb.Node)result;
     }
 
     public hypeerweb.Node getLowestNeighborWithoutChild(){
         String[] parameterTypeNames = new String[0];
         Object[] actualParameters = new Object[0];
         Command command = new Command(globalObjectId.getLocalObjectId(), "hypeerweb.Node", "getLowestNeighborWithoutChild", parameterTypeNames, actualParameters, true);
         Object result = PeerCommunicator.getSingleton().sendSynchronous(globalObjectId, command);
         return (hypeerweb.Node)result;
     }
 
     public void removeFromHyPeerWeb(){
         String[] parameterTypeNames = new String[0];
         Object[] actualParameters = new Object[0];
         Command command = new Command(globalObjectId.getLocalObjectId(), "hypeerweb.Node", "removeFromHyPeerWeb", parameterTypeNames, actualParameters, false);
         PeerCommunicator.getSingleton().sendASynchronous(globalObjectId, command);
     }
 
     public int getFoldId(){
         String[] parameterTypeNames = new String[0];
         Object[] actualParameters = new Object[0];
         Command command = new Command(globalObjectId.getLocalObjectId(), "hypeerweb.Node", "getFoldId", parameterTypeNames, actualParameters, true);
         Object result = PeerCommunicator.getSingleton().sendSynchronous(globalObjectId, command);
         return (Integer)result;
     }
 
     public int getSurrogateFoldId(){
         String[] parameterTypeNames = new String[0];
         Object[] actualParameters = new Object[0];
         Command command = new Command(globalObjectId.getLocalObjectId(), "hypeerweb.Node", "getSurrogateFoldId", parameterTypeNames, actualParameters, true);
         Object result = PeerCommunicator.getSingleton().sendSynchronous(globalObjectId, command);
         return (Integer)result;
     }
 
     public int getInverseSurrogateFoldId(){
         String[] parameterTypeNames = new String[0];
         Object[] actualParameters = new Object[0];
         Command command = new Command(globalObjectId.getLocalObjectId(), "hypeerweb.Node", "getInverseSurrogateFoldId", parameterTypeNames, actualParameters, true);
         Object result = PeerCommunicator.getSingleton().sendSynchronous(globalObjectId, command);
         return (Integer)result;
     }
 
     public hypeerweb.Connections getConnections(){
         String[] parameterTypeNames = new String[0];
         Object[] actualParameters = new Object[0];
         Command command = new Command(globalObjectId.getLocalObjectId(), "hypeerweb.Node", "getConnections", parameterTypeNames, actualParameters, true);
         Object result = PeerCommunicator.getSingleton().sendSynchronous(globalObjectId, command);
         return (hypeerweb.Connections)result;
     }
 
     public void setConnections(hypeerweb.Connections p0){
         String[] parameterTypeNames = new String[1];
         parameterTypeNames[0] = "hypeerweb.Connections";
         Object[] actualParameters = new Object[1];
         actualParameters[0] = p0;
         Command command = new Command(globalObjectId.getLocalObjectId(), "hypeerweb.Node", "setConnections", parameterTypeNames, actualParameters, false);
         PeerCommunicator.getSingleton().sendASynchronous(globalObjectId, command);
     }
     
     public void parentNotify()
     {
         String[] parameterTypeNames = new String[0];
         Object[] actualParameters = new Object[0];
        Command command = new Command(globalObjectId.getLocalObjectId(), "hypeerweb.Node", "parentNotify", parameterTypeNames, actualParameters, false);
         PeerCommunicator.getSingleton().sendASynchronous(globalObjectId, command);
     }
     
    public hypeerweb.Connections getChildConnections()
     {
         String[] parameterTypeNames = new String[0];
         Object[] actualParameters = new Object[0];
        Command command = new Command(globalObjectId.getLocalObjectId(), "hypeerweb.Node", "getChildConnections", parameterTypeNames, actualParameters, true);
         Object result = PeerCommunicator.getSingleton().sendSynchronous(globalObjectId, command);
        return (hypeerweb.Connections)result;
     }
 
     public command.GlobalObjectId getId(){
         /*String[] parameterTypeNames = new String[0];
         Object[] actualParameters = new Object[0];
         Command command = new Command(globalObjectId.getLocalObjectId(), "hypeerweb.ProxyableObject", "getId", parameterTypeNames, actualParameters, true);
         Object result = PeerCommunicator.getSingleton().sendSynchronous(globalObjectId, command);
         return (command.GlobalObjectId)result;*/
         return globalObjectId;
     }
 
     public boolean equals(java.lang.Object p0){
         String[] parameterTypeNames = new String[1];
         parameterTypeNames[0] = "java.lang.Object";
         Object[] actualParameters = new Object[1];
         actualParameters[0] = p0;
         Command command = new Command(globalObjectId.getLocalObjectId(), "java.lang.Object", "equals", parameterTypeNames, actualParameters, true);
         Object result = PeerCommunicator.getSingleton().sendSynchronous(globalObjectId, command);
         return (Boolean)result;
     }
 
     public java.lang.String toString(){
         /*String[] parameterTypeNames = new String[0];
         Object[] actualParameters = new Object[0];
         Command command = new Command(globalObjectId.getLocalObjectId(), "java.lang.Object", "toString", parameterTypeNames, actualParameters, true);
         Object result = PeerCommunicator.getSingleton().sendSynchronous(globalObjectId, command);
         return (java.lang.String)result;*/
         return "I'm a proxy node: " + globalObjectId.toString();
     }
 
     public int hashCode(){
         String[] parameterTypeNames = new String[0];
         Object[] actualParameters = new Object[0];
         Command command = new Command(globalObjectId.getLocalObjectId(), "java.lang.Object", "hashCode", parameterTypeNames, actualParameters, true);
         Object result = PeerCommunicator.getSingleton().sendSynchronous(globalObjectId, command);
         return (Integer)result;
     }
     
     public Object writeReplace()
     {
         return this;
     }
     
     public Object readResolve()
     {
         GlobalObjectId newId = new GlobalObjectId();
         if (globalObjectId.onSameMachineAs(newId))
         {
             //HyPeerWeb hypeerweb = HyPeerWeb.getSingleton();
             //return hypeerweb.getNode(getWebId());
             return ObjectDB.getSingleton().getValue(globalObjectId.getLocalObjectId());
         }
         else
         {
             return this;
         }
     }
 
 }
