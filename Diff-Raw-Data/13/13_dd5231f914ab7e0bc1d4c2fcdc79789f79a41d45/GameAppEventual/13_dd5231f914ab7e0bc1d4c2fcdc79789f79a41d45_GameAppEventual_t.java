 package game;
 
 import java.util.Collection;
 import java.util.Vector;
 import com.jme3.math.Quaternion;
 import com.jme3.math.Vector3f;
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
   
   public GameAppEventual(Node node, GameModel game)
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
     }
     
     // Lets set up scribe 
     myScribe = new ScribeImpl(node,"myScribeInstance");
     this.game = game;
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
           Vector<NodeHandle> changedPlayers = allPlayerData.dealWithNewPlayerDataSet(message);
           for (int i=0; i<changedPlayers.size(); i++){
             PlayerData data = allPlayerData.nodeToPlayerData.get(changedPlayers.get(i));
             game.updatePlayer(changedPlayers.get(i), data.position, data.rotation);
           }
         }
       }
       else if (game != null && content instanceof RegionChangeScribeMsg)
       {
         RegionChangeScribeMsg message = (RegionChangeScribeMsg)content;
         if (message.from != endpoint.getLocalNodeHandle()){
           System.out.println("RegionChange Rcvd: "+content);
           if (message.joinedRegion){
             System.out.println("Tell them where I AM!");
             sendMoveEventMulticast();
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
 }
