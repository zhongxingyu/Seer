 package game;
 
 import java.util.Vector;
 import com.jme3.math.Quaternion;
 import com.jme3.math.Vector3f;
 import java.util.Map;
 import java.util.HashMap;
 import rice.p2p.commonapi.Application;
 import rice.p2p.commonapi.CancellableTask;
 import rice.p2p.commonapi.Endpoint;
 import rice.p2p.commonapi.Id;
 import rice.p2p.commonapi.Message;
 import rice.p2p.commonapi.Node;
 import rice.p2p.commonapi.NodeHandle;
 import rice.p2p.commonapi.RouteMessage;
 import rice.p2p.scribe.*;
 import rice.p2p.scribe.Scribe;
 import rice.p2p.scribe.ScribeContent;
 import rice.p2p.scribe.ScribeImpl;
 import rice.p2p.scribe.ScribeImpl.*;
 import rice.p2p.scribe.Topic;
 import rice.pastry.commonapi.PastryIdFactory;
 import java.util.Map;
 
 
 public class GameAppLeader implements Application, ScribeClient {
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
   boolean isGroupLeader=false;
   Topic groupLeader;
   Map<NodeHandle, Region> groupLeaderToRegion = new HashMap<NodeHandle,Region>();
   NodeHandle bootstrapNodeHandle;
   //Vector3f lastPosition;
   
   public GameAppLeader(Node node, GameModel game)
   {
     // We are only going to use one instance of this application on each PastryNode
     this.endpoint = node.buildEndpoint(this, "myinstance");
        
     // the rest of the initialization code could go here
     this.node = node;
     // now we can receive messages
     this.endpoint.register();   
   
     // Schedule a msg to be sent to self if we are not the bootstrap
     if (game != null){
       messageToSelfTask = this.endpoint.scheduleMessage(new MessageToSelf(), 100, 100);
       sendToTopics = new Vector<Topic>();           
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
       //myScribe.addChild(top, endpoint.getLocalNodeHandle());      
      // scribeTopics.add(top);               
   } 
  
   /**
    * Unsubscribes to myTopic.
    */
   public void unsubscribe(Topic top) {
     System.out.println("contains b4 unsubscribe?:" +myScribe.containsTopic(top));
     
     myScribe.unsubscribe(top, this);     
     //myScribe.removeChild(top, endpoint.getLocalNodeHandle());
     System.out.println("contains after unsubscribe?:" +myScribe.containsTopic(top));
     
   } 
   
   /**
    * Called to route a message to a specific id, not using Scribe 
    */
   public void routeMyMsg(Id id) {
     //System.out.println(this+" sending to "+id);    
     //Message msg = new MyMsg(endpoint.getId(), id);
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
  
   public void sendMoveEventMulticast(Vector3f position, Quaternion rotation)
   {
       ScribeMoveMsg myMessage = new ScribeMoveMsg(
        endpoint.getLocalNodeHandle().getId(),
         position.x,position.y,position.z,
         rotation.getW(), rotation.getX(), rotation.getY(), rotation.getZ());
       for(int i = 0; i<sendToTopics.size(); i++){
         myScribe.publish(sendToTopics.get(i), myMessage);              
       }
   }
   
   public void sendRegionMulticast(boolean joined, Region newReg, Region oldReg)
    {
       RegionChangeScribeMsg message = new RegionChangeScribeMsg(endpoint.getLocalNodeHandle().getId(),joined);
       
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
               System.out.print("\n");                  
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
                   this.unsubscribe(myTopic);
                   // We are leaving a region in this case, we need to tell everyone that we are leaving!
                   sendRegionMulticast(false,newRegion,game.region);
                   // We also need to assign a new group leader
                   if (isGroupLeader){
                     GroupLeaderRequestMsg removeMe = new GroupLeaderRequestMsg(true,endpoint.getLocalNodeHandle(),game.region);
                     endpoint.route(null,removeMe,bootstrapNodeHandle);                   
                   }
                 }
                 // Set the new region
                 Region oldRegion = game.region;
                 game.region = newRegion;
                                 
                 // This function removes everyone in old regions, because we will be re-adding them
                 //game.removePlayersByRegion(oldRegion,newRegion);                
                 
                 System.out.println("Region is x:"+game.region.x+" y:"+game.region.y);                
                 myTopic = new Topic(new PastryIdFactory(node.getEnvironment()), "Region x:"+game.region.x+" y:"+game.region.y);
     
                 // The region may be empty, so ask to be the leader,
                 System.out.println("Can I be the leader?");  
                 endpoint.route(null ,new GroupLeaderRequestMsg(endpoint.getLocalNodeHandle(),newRegion), bootstrapNodeHandle);
                                                 
                 this.subscribe(myTopic);                
                 sendToTopics = getSendToTopics(game.region);
                 // We need to tell everyone in our new region that we are there!
                 sendRegionMulticast(true, game.region,oldRegion);
                 sendMoveEventMulticast(game.getPlayerPosition(), game.getPlayerRotation());                                               
             }
             if (game.positionChanged){        
              //   System.out.println("Pos Changed");            
                 game.positionChanged=false;
                 sendMoveEventMulticast(game.getPlayerPosition(), game.getPlayerRotation());
             }            
         }
       }else if (message instanceof BootstrapIDMessage){
         // We need to know who the bootstrap is
         System.out.println("Bootstrap Node Discovered");
         this.bootstrapNodeHandle = ((BootstrapIDMessage)message).bootstrapID;        
       }else if (message instanceof GroupLeaderRequestMsg){
         handleLeaderReqMsg((GroupLeaderRequestMsg)message);
       }
   }
 
   /**
    * This is Called when we receive a message from a scribe topic
    */
   public void deliver(Topic topic, ScribeContent content) {
 	    if (game != null && content instanceof ScribeMoveMsg)
             {
                 ScribeMoveMsg message = (ScribeMoveMsg)content;
                 // Msg not from self
                 if (message.from != endpoint.getLocalNodeHandle()){
                   System.out.println("Move Rcvd: "+content);	  
                   Vector3f position = new Vector3f(message.x,message.y,message.z);
                   Quaternion rotation = new Quaternion(message.rx, message.ry, message.rz, message.rw);
                   PlayerData pd = new PlayerData(position,rotation);
                   game.updatePlayer(message.from, pd);
                 }
             }
             else if (game != null && content instanceof RegionChangeScribeMsg)
             {
                 RegionChangeScribeMsg message = (RegionChangeScribeMsg)content;
                 if (message.from != endpoint.getLocalNodeHandle()){
                     System.out.println("RegionChange Rcvd: "+content);
                     if (message.joinedRegion){
                         sendMoveEventMulticast(game.getPlayerPosition(), game.getPlayerRotation());                    
                     }else{
                        //Someone has left our region. Fuck them, remove em                        
                        game.removePlayer(message.from);
                     }
                 }                                
             }
             else if (game != null && content instanceof NeedNewLeaderScribeMsg)
             {
               //The bootstrap wants one of us to be the new leader, hurry ask to be it!
               endpoint.route(null ,new GroupLeaderRequestMsg(endpoint.getLocalNodeHandle(),game.region), bootstrapNodeHandle);                
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
             System.out.println("Somone joined the ring BOOT");
             BootstrapIDMessage bootMsg = new BootstrapIDMessage(endpoint.getLocalNodeHandle());
             endpoint.route(handle.getId(), bootMsg, handle);
           }else{
             System.out.println("Someone exited the ring BOOT");
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
            game.removePlayer(handle.getId());
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
   //  System.out.println("Stats: "+myScribe.getChildrenOfTopic(topic)+ " // "+myScribe.getClients(topic));    
   }
 
   public void childRemoved(Topic topic, NodeHandle child) {
     //System.out.println("MyScribeClient.childRemoved("+topic+","+child+")");
   }
 
   public void subscribeFailed(Topic topic) {
 //    System.out.println("MyScribeClient.childFailed("+topic+")");
   }
   public boolean anycast(Topic topic, ScribeContent content) {
     return true;
   }    
   
   public void handleLeaderReqMsg(GroupLeaderRequestMsg reqMsg){  
     if (game == null && reqMsg.isARequest && !reqMsg.removalRequest){
       GroupLeaderRequestMsg response = new GroupLeaderRequestMsg(reqMsg.region,false); 
       System.out.println("Request made for a new Group Leader");
       // The bootstrap node is being asked if a node can be a leader
       if (mapHasRegion(reqMsg.region)){
        //This means there is already a group leader, tell this guy to piss off
         System.out.println("No, someone is this already");
       }else{
         //No group leader for this region, give it to him and change msg to accepted
         response.acceptingLeader=true;
         groupLeaderToRegion.put(reqMsg.requester, reqMsg.region);
         System.out.println("You will get it, new map"+groupLeaderToRegion);
       }
       endpoint.route(null, response, reqMsg.requester);
     }else if (game == null && !reqMsg.isARequest && reqMsg.removalRequest){
       // This is someone telling the bootstrap they are not the leader anymore
       System.out.println("Someone told bootstrap that they are not leader anymore"); 
       Topic leadersOldTopic = new Topic(new PastryIdFactory(node.getEnvironment()), "Region x:"+(reqMsg.region.x)+" y:"+(reqMsg.region.y));
       // Lets verify that this region has a group leader, and remove him
       if (groupLeaderToRegion.containsKey(reqMsg.requester)){        
         groupLeaderToRegion.remove(reqMsg.requester);
       }
       myScribe.publish(leadersOldTopic, new NeedNewLeaderScribeMsg(endpoint.getLocalNodeHandle()));
     }else if (game != null && !reqMsg.isARequest && !reqMsg.removalRequest){
       // This is someone telling us that we may or maynot be the new group leader
       if (reqMsg.acceptingLeader && reqMsg.region.equals(game.region)){
         System.out.println("I am the new group leader");
         isGroupLeader=true;
       }                           
     }    
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
   
   public boolean mapHasRegion(Region r){
     Region regions[] = groupLeaderToRegion.values().toArray(new Region[0]);
     for (int i = 0; i<regions.length; i++){
       if (regions[i].equals(r))
         return true;
     }
     return false;
   }
 
 }
 
 
