 package game;
 
 import java.util.Collection;
 import java.util.Vector;
 import com.jme3.math.Quaternion;
 import com.jme3.math.Vector3f;
 import java.util.Iterator;
 import rice.p2p.commonapi.Application;
 import rice.p2p.commonapi.CancellableTask;
 import rice.p2p.commonapi.Endpoint;
 import rice.p2p.commonapi.Id;
 import rice.p2p.commonapi.Message;
 import rice.p2p.commonapi.Node;
 import rice.p2p.commonapi.NodeHandle;
 import rice.p2p.commonapi.RouteMessage;
 import rice.p2p.scribe.Scribe;
 import rice.p2p.scribe.ScribeContent;
 import rice.p2p.scribe.ScribeImpl;
 import rice.p2p.scribe.Topic;
 import rice.pastry.commonapi.PastryIdFactory;
 import rice.p2p.scribe.ScribeMultiClient;
 import java.util.Map;
 import rice.Continuation;
 import rice.p2p.past.Past;
 import rice.p2p.past.PastContent;
 
 public class GameAppEventual implements ScribeMultiClient, Application {
   	    
     CancellableTask messageToSelfTask;		
 		
 	/**
    * The Endpoint represents the underlieing node.  By making calls on the 
    * Endpoint, it assures that the message will be delivered to a MyApp on whichever
    * node the message is intended for.
    */
   protected Endpoint endpoint;
   protected Node node;
   Scribe myScribe;
   Topic myTopic;
   GameModel game;
   Vector<Topic> sendToTopics;
   //Vector3f lastPosition;
   PlayerDataScribeMsg allPlayerData;
   Past pastInst;
   PastryIdFactory pastIDF;  
   Vector<PastRegionObject> ourPastRegions;
   
   
   public GameAppEventual(Node node, GameModel game, Past pastInst, PastryIdFactory pastIDF)
   {
     
     // We are only going to use one instance of this application on each PastryNode
     this.endpoint = node.buildEndpoint(this, "myinstance");
     
     // the rest of the initialization code could go here
     this.node = node;
     // now we can receive messages
     this.endpoint.register();
   
     // Schedule a msg to be sent to self if we are not the bootstrap
     if (game != null){
       messageToSelfTask = this.endpoint.scheduleMessage(new MessageToSelf(), 100, 40);
       sendToTopics = new Vector<Topic>();
       allPlayerData = new PlayerDataScribeMsg(this.endpoint.getLocalNodeHandle());     
       ourPastRegions = new Vector();
     }
     
     // Lets set up scribe 
     myScribe = new ScribeImpl(node,"myScribeInstance");
     this.game = game;
     
     this.pastInst=pastInst;
     this.pastIDF = pastIDF;    
     
   }
 
   public Node getNode(){
 	  return node;
   }
   
   /**
    * Subscribes to myTopic.
    */
   public void subscribe(Topic top) {
       myScribe.subscribe(top, this); 
      // scribeTopics.add(top);         
   } 
  
   /**
    * Unsubscribes to myTopic.
    */
   public void unsubscribe(Topic top) {
     //myScribe.addChild(myTopic, endpoint.getLocalNodeHandle());
       myScribe.unsubscribe(top, this); 
   } 
   
   /**
    * Called to route a message to a specific id, not using Scribe 
    */
   public void routeMyMsg(Id id) {
 //    System.out.println(this+" sending to "+id);    
   //  Message msg = new MyMsg(endpoint.getId(), id);
     //endpoint.route(id, msg, null);
   }
   
   /**
    * Called to directly send a message to the nh, not using Scribe
    */
   public void routeMyMsgDirect(NodeHandle nh) {
 //    System.out.println(this+" sending direct to "+nh);    
   //  Message msg = new MyMsg(endpoint.getId(), nh.getId());
     //endpoint.route(null, msg, nh);
   }
     
   public void sendMulticast(String s){
     // not using this At the moment, using more specific multicasts
   }
  
   public void sendMoveEventMulticast()
   {
   /*    ScribeMoveMsg myMessage = new ScribeMoveMsg(
         endpoint.getLocalNodeHandle(),
         position.x,position.y,position.z,
         rotation.getW(), rotation.getX(), rotation.getY(), rotation.getZ());
   */
     //Make my player data item
       PlayerData myPlayer = new PlayerData();
       myPlayer.position = game.getPlayerPosition();
       myPlayer.rotation = game.getPlayerRotation();
       myPlayer.setTime();
       allPlayerData.updatePlayerData(endpoint.getLocalNodeHandle(), myPlayer); 
       for(int i = 0; i<sendToTopics.size(); i++){
         myScribe.publish(sendToTopics.get(i), allPlayerData);
       }
       //System.out.println("Move Msg Sent!!");
     }	   
 
   public void sendChatMessage(String message){
     ChatScribeMsg chatMsg = new ChatScribeMsg(endpoint.getLocalNodeHandle(),message);
     for(int i = 0; i<sendToTopics.size(); i++){
       myScribe.publish(sendToTopics.get(i), chatMsg);
     }  
   }
   
    public void sendRegionMulticast(boolean joined, Region newReg, Region oldReg)
    {
       RegionChangeScribeMsg message = new RegionChangeScribeMsg(endpoint.getLocalNodeHandle(),joined);
       if (joined){
           //If it is when we first joined the game, everyone must know
           if (oldReg.x==-1 && oldReg.y==-1){
             for(int i = 0; i<sendToTopics.size(); i++){
                 myScribe.publish(sendToTopics.get(i), message);
             }            
           }else{
               int diffX = newReg.x-oldReg.x;
               int diffY = newReg.y-oldReg.y;
               System.out.print("Telling these people I joined:");
               for (int i=-1; i<2; i++){
                   Topic t;
                   if (diffX==0){
                     t = new Topic(new PastryIdFactory(node.getEnvironment()), "Region x:"+(newReg.x+i)+" y:"+(newReg.y+diffY));
                     System.out.print("x:"+(newReg.x+i)+" y:"+ (newReg.y+diffY)+" / ");
                   }else{
                     t = new Topic(new PastryIdFactory(node.getEnvironment()), "Region x:"+(newReg.x+diffX)+" y:"+(newReg.y+i));  
                     System.out.print("x:"+(newReg.x+diffX)+" y:"+ (newReg.y+i)+" / ");        
                   }
                   myScribe.publish(t, message);
               }
               System.out.println("\n");
           }          
       }else{
           // We are leaving a region, tell
           int diffX = newReg.x-oldReg.x;
           int diffY = newReg.y-oldReg.y;
           for (int i=-1; i<2; i++){
             Topic t;
             if (diffX==0){
               t = new Topic(new PastryIdFactory(node.getEnvironment()), "Region x:"+(oldReg.x+i)+" y:"+(oldReg.y-diffY));            
             }else{
               t = new Topic(new PastryIdFactory(node.getEnvironment()), "Region x:"+(oldReg.x-diffX)+" y:"+(oldReg.y+i));
             }
             myScribe.publish(t, message);
           }          
       }            
    }	   
     
   /**
    * Called when we receive a message directly.
    */
   public void deliver(Id id, Message message) {	  	 
       if (message instanceof MessageToSelf){
         if (game != null){
             if (game.regionChanged){              
                 game.regionChanged=false;
                 Region newRegion = GameModel.getPlayersRegion(game.getPlayerPosition());
                 if (myTopic != null){                    
                   myScribe.unsubscribe(myTopic, this);
                   // We are leaving a region in this case, we need to tell everyone that we are leaving!
                   // DO NOT NEED TO DO THIS IN EVENTUAL, WE DO NOT REMOVE PLAYERS
                   //sendRegionMulticast(false,newRegion,game.region);
                 }
                 // Set the new region
                 Region oldRegion = game.region;
                 game.region = newRegion;
                 
                 // Get the object in this region
                 Region ret[] = getNewRegions(newRegion,oldRegion);
                for (int i=0; i<ret.length; i++){
                   final Id thisChestID = pastIDF.buildId("RegionState (x:"+ret[i].x+" y:"+ret[i].y+")");
                   System.out.println("Chest ID: "+thisChestID);
                   this.lookupObject(thisChestID);
                 }
                                                 
                 // This function removes everyone in old regions, because we will be re-adding them
                 //game.removePlayersByRegion(oldRegion,newRegion);                
                 
                 System.out.println("Region is x:"+game.region.x+" y:"+game.region.y);                
                 myTopic = new Topic(new PastryIdFactory(node.getEnvironment()), "Region x:"+game.region.x+" y:"+game.region.y);
                 this.subscribe(myTopic);
                 sendToTopics = getSendToTopics(game.region);
                 // We need to tell everyone in our new region that we are there!
                 sendRegionMulticast(true, game.region,oldRegion);
                 sendMoveEventMulticast();                                               
             }
             if (game.positionChanged){
                 game.positionChanged=false;
                 sendMoveEventMulticast();
             }            
         }
       }
       else if (message instanceof PlayerDataDirectMsg)
       {
         System.out.println("Received Other Players View DIRECTLY");
         PlayerDataScribeMsg content = ((PlayerDataDirectMsg)message).toScribeMsg();
         changeWorldPlayerView(content);
       }
       
   }
 
   /**
    * This is Called when we receive a message from a scribe topic
    */
   public void deliver(Topic topic, ScribeContent content) {
 	    if (game != null && content instanceof PlayerDataScribeMsg)
       {
         PlayerDataScribeMsg message = (PlayerDataScribeMsg)content;
         // Msg not from self
         if (message.from != endpoint.getLocalNodeHandle()){
           System.out.println("Move Rcvd: "+content);	  
           // Lets get the players that are new and or have changed position
           changeWorldPlayerView(message);
         }
       }
       else if (game != null && content instanceof RegionChangeScribeMsg)
       {
         RegionChangeScribeMsg message = (RegionChangeScribeMsg)content;
         if (message.from != endpoint.getLocalNodeHandle()){
           System.out.println("RegionChange Rcvd: "+content);
           if (message.joinedRegion){
             System.out.println("Tell them where I AM!");
             //sendMoveEventMulticast();
             // Send this message directly to the who we got this from
             PlayerDataDirectMsg directMsg = allPlayerData.toDirectMsg();
             endpoint.route(null,directMsg,message.from);
           }else{
             //Someone has left our region. Fuck them, remove em
             // WE DO NOT REMOVE PLAYERS THAT LEAVE OUR REGION!
             //game.removePlayer(message.from);
           }
         }                                
       }
       else if (game!=null && content instanceof ChatScribeMsg)
       {
         //Chat Msg, we need to call game's methods to get it drawn
         game.receiveChatMessage(((ChatScribeMsg)content).chatter);        
       }
   }
 
   
   /**
    * Called when you hear about a new neighbor.
    * Don't worry about this method for now.
    */
   public void update(NodeHandle handle, boolean joined) {
     System.out.println("UPDATE MESSAGE!");
     
       if (game == null){
           if(joined){
             System.out.println("Somone joined the ring");
           //  System.out.println("Sending out the Subscrbe!");
           //  TopicMsg tm = new TopicMsg();
           //  tm.addTopicName(myTopic);
           //  endpoint.route(null, tm, handle);            
           }else{
             System.out.println("Someone exited the ring");
           }
       }else{
           // This is all game nodes, NOT bootstrap
           if (joined){
             System.out.println("Someone joined the ring");
             // Someone joined, everyone send out you position incase he is in your group!            
             //sendMoveEventMulticast(game.getPlayerPosition(), game.getPlayerRotation());            
           }else{
             System.out.println("Someone exited the ring");
             //Someone left, we need to remove their player from our map!
             game.removePlayer(handle);
             allPlayerData.playerLeftGame(handle);
           }
       }
   }
   
   /**
    * Called when a message travels along your path.
    * Don't worry about this method for now.
    */
   public boolean forward(RouteMessage message) {
     return true;
   }
   
   @Override
   public String toString() {
     return "MyApp "+endpoint.getId();
   }
 
   public void childAdded(Topic topic, NodeHandle child) {
 //    System.out.println("MyScribeClient.childAdded("+topic+","+child+")");
   }
 
   public void childRemoved(Topic topic, NodeHandle child) {
 //    System.out.println("MyScribeClient.childRemoved("+topic+","+child+")");
   }
 
   public void subscribeFailed(Topic topic) {
 //    System.out.println("MyScribeClient.childFailed("+topic+")");
   }
   
     public void subscribeFailed(Collection<Topic> topic) {
 //    System.out.println("MyScribeClient.childFailed("+topic+")");
   }
       public void subscribeSuccess(Collection<Topic> topic) {
 //    System.out.println("MyScribeClient.childFailed("+topic+")");
   }
   
   
   public boolean anycast(Topic topic, ScribeContent content) {
     return true;
   }    
   
   public Vector<Topic> getSendToTopics(Region r){
       Vector<Topic> top = new Vector<Topic>();
       //Get rid of messages past boundaries
       for (int x = -1; x<2; x++){          
         for(int y=-1; y<2; y++){
             Topic addTop = new Topic(new PastryIdFactory(node.getEnvironment()), "Region x:"+(game.region.x+x)+" y:"+(game.region.y+y));
             top.add(addTop);          
          }
       }      
       return top;
   }    
   
   public void changeWorldPlayerView(PlayerDataScribeMsg sentSet){
     Vector<NodeHandle> changedPlayers = allPlayerData.dealWithNewPlayerDataSet(sentSet);
     for (int i=0; i<changedPlayers.size(); i++){
       PlayerData data = allPlayerData.nodeToPlayerData.get(changedPlayers.get(i));
       game.updatePlayer(changedPlayers.get(i), data.position, data.rotation);
       System.out.println("player: "+changedPlayers.get(i)+" pos: "+data.position);
     }
   }
   
     class MyContinuation implements Continuation
     {
       GameAppEventual callee;
       
       MyContinuation(GameAppEventual callee) {
         this.callee = callee;
       }
         
       public void receiveResult(Object result) {
         PastRegionObject pc = (PastRegionObject)result;
         System.out.println("Received a "+pc);
         callee.onObjectLookupResult(pc);
       }
 
       public void receiveException(Exception result) {
         System.out.println("There was an error: "+result);
       }
     };
     
     public void lookupObject(final Id id)
     {
         System.out.println("LOOKING UP REGION!");
         Continuation c = new MyContinuation(this);
         pastInst.lookup(id, c);
     }
     
     public void onObjectLookupResult(PastRegionObject result)
     {
         System.out.println("GOT REGION UPDATE!");
         for(Iterator<ChestObject> i = result.chests.iterator(); i.hasNext(); )
         {
             ChestObject item = i.next();
             game.updateObject(item);
         }
     }
     
     public Region[] getNewRegions(Region newReg, Region oldReg){
       if (oldReg.x==-1 && oldReg.y==-1){
         Region retReg[] = new Region[9];
         int count=0;
         for (int x=-1; x<2; x++){
           for (int y=-1; y<2; y++){                        
             retReg[count] = new Region(newReg.x+x,newReg.y+y);
           }
         }
         return retReg;
       }   

       
       int diffX = newReg.x-oldReg.x;
       int diffY = newReg.y-oldReg.y;
       Region retReg[] = new Region[3];
       for (int i=-1; i<2; i++){
         if (diffX==0){
           retReg[i+1] = new Region(newReg.x+i,newReg.y+diffY);
         }else{
           retReg[i+1] = new Region(newReg.x+diffX,newReg.y+i);
         }
       }
       return retReg;                      
     }
     public Region[] getRegionsWeLeft(Region newReg, Region oldReg){    
       int diffX = newReg.x-oldReg.x;
       int diffY = newReg.y-oldReg.y;      
       Region retReg[] = new Region[3];
       for (int i=-1; i<2; i++){
         if (diffX==0){
           retReg[i+1] = new Region(oldReg.x+i,oldReg.y-diffY);            
         }else{
           retReg[i+1] = new Region(oldReg.x-diffX,oldReg.y+i);
         }
       }
       return retReg;
     }
         
     
     
 }
