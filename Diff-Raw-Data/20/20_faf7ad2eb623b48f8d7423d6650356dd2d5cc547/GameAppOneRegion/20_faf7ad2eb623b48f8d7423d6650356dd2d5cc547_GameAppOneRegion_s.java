 package game;
 
 import java.util.Vector;
 import java.util.Collection;
 import com.jme3.math.Quaternion;
 import com.jme3.math.Vector3f;
 import java.io.IOException;
 import java.util.HashMap;
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
 import rice.p2p.scribe.ScribeImpl.TopicManager;
 import rice.p2p.scribe.Topic;
 import rice.pastry.commonapi.PastryIdFactory;
 import rice.p2p.scribe.ScribeMultiClient;
 import java.util.Map;
 import rice.Continuation;
 import rice.p2p.commonapi.NodeHandleSet;
 import rice.p2p.commonapi.rawserialization.InputBuffer;
 import rice.p2p.commonapi.rawserialization.MessageDeserializer;
 import rice.p2p.past.Past;
 import rice.p2p.scribe.rawserialization.ScribeContentDeserializer;
 
 
 public class GameAppOneRegion implements ScribeMultiClient, Application
 {  	    
   CancellableTask messageToSelfTask;		
 		
   /**
   * The Endpoint represents the underlieing node.  By making calls on the 
   * Endpoint, it assures that the message will be delivered to a MyApp on whichever
   * node the message is intended for.
   */
   protected Endpoint endpoint;
   protected Node node;
   Scribe myScribe;
   TopicManager tm;
   Topic myTopic;
   GameModel game;
   //Vector<Topic> sendToTopics;
   //Vector3f lastPosition;
   Past pastInst;
   PastryIdFactory pastIDF;
 
   Map<Id,ChestObject> cachedChests;
 
   int remainingHealth = 1000;  
   
   public GameAppOneRegion(Node node, GameModel game, Past pastInst, PastryIdFactory pastIDF)
   {
     // We are only going to use one instance of this application on each PastryNode
     this.endpoint = node.buildEndpoint(this, "myinstance");
     
     // the rest of the initialization code could go here
     this.node = node;
     // now we can receive messages
     this.endpoint.register();
 
     cachedChests = new HashMap<Id,ChestObject>();
   
     
     // Schedule a msg to be sent to self if we are not the bootstrap
     if (game != null){
       messageToSelfTask = this.endpoint.scheduleMessage(new MessageToSelf(), 100, 100);
     //  sendToTopics = new Vector<Topic>();           
     }
     
     // Lets set up scribe 
     myScribe = new ScribeImpl(node,"myScribeInstance");
     
     // tell scribe how to deserialize our messages
     myScribe.setContentDeserializer(new ScribeContentDeserializer() {
       public ScribeContent deserializeScribeContent(InputBuffer buf, Endpoint endpoint, short type) throws IOException {
         switch (type) {
           case MoveScribeMsg.TYPE:
             return new MoveScribeMsg(buf, endpoint);
           case RegionChangeScribeMsg.TYPE:
             return new RegionChangeScribeMsg(buf, endpoint);
           case ChatScribeMsg.TYPE:
             return new ChatScribeMsg(buf, endpoint);
           case TestMessage.TYPE:
             return new TestMessage(buf, endpoint);
           case ObjectStateChangeScribe.TYPE:
             return new ObjectStateChangeScribe(buf, endpoint);
           case AttackScribe.TYPE:
             return new AttackScribe(buf, endpoint);
         
         }
         throw new IllegalArgumentException("Invalid type:"+type);
       }
     });
     
     // tell the endpoint how to deserialize our message
      endpoint.setDeserializer(new MessageDeserializer() {	   
 	      public Message deserialize(InputBuffer buf, short type, int priority,
 	          NodeHandle sender) throws IOException {
 	        switch (type) {
             case MoveDirectMsg.TYPE:
               // just call the special deserialization constructor.
               return new MoveDirectMsg(buf, endpoint);
             case ObjectReqMsg.TYPE:
               return new ObjectReqMsg(buf, endpoint);	        
             case ObjectStateChangeMsg.TYPE:
               return new ObjectStateChangeMsg(buf, endpoint);	        
             case AttackMsg.TYPE:
               return new AttackMsg(buf, endpoint);
         
 	     
 	        }
 	        // maybe throw an IOException if you want
 	        return null;
 	      }   
 	    });
     
      this.pastInst = pastInst;
      this.pastIDF = pastIDF;     
             
     //tm = new ScribeImpl.TopicManager()
     this.game = game;
   }
 
   public Node getNode(){
 	  return node;
   }
   
   /**
    * Subscribes to myTopic.
    */
   public void subscribe(Topic top) {
     //myScribe.addChild(myTopic, endpoint.getLocalNodeHandle());
       myScribe.subscribe(top, this); 
      // scribeTopics.add(top);         
   } 
  
   /**
    * Unsubscribes to myTopic.
    */
   public void unsubscribe(Topic top) {
     //ScribeImpl.TopicManager
       //myScribe.removeChild(myTopic, endpoint.getLocalNodeHandle());
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
  
   public void sendMoveEventMulticast(Vector3f position, Quaternion rotation, String name)
   {
       MoveScribeMsg myMessage = new MoveScribeMsg(
         endpoint.getLocalNodeHandle().getId(),
         position.x,position.y,position.z,
         rotation.getW(), rotation.getX(), rotation.getY(), rotation.getZ(),
         name);
       
       myScribe.publish(myTopic,myMessage);
    }	   
   
   public void sendObjectChangeMulticast(ChestObject obj)
   {
      ObjectStateChangeScribe msg = new ObjectStateChangeScribe(endpoint.getLocalNodeHandle().getId(),obj);
      myScribe.publish(myTopic, msg);
   }
   
   public void sendBeenAttackedMulticast(int damageTaken)
   {
     PlayerData myData = new PlayerData(this.endpoint.getLocalNodeHandle().getId(),
             game.getPlayerPosition(),
             game.getPlayerRotation(),
             game.getPlayerName());
     this.remainingHealth=this.remainingHealth-damageTaken;
     myData.health=this.remainingHealth;
     myData.lastDamageTaken=damageTaken;
     myScribe.publish(myTopic,new AttackScribe(this.endpoint.getLocalNodeHandle().getId(),myData)); 
   }
   
    // joined:   false = left region
    //           true = entered region
    public void sendRegionMulticast(boolean joined)
    {
       RegionChangeScribeMsg message =
               new RegionChangeScribeMsg(endpoint.getLocalNodeHandle().getId(), joined);
       
       //My topic will be the correct topic that we are either leaving or joining
       myScribe.publish(myTopic, message);
                   
    }	   
    
    public void sendChatMessage(String message)
    {
     ChatScribeMsg chatMsg = new ChatScribeMsg(
             endpoint.getLocalNodeHandle().getId(),
             message);
     
     myScribe.publish(myTopic, chatMsg);      
   }
   
    public void performAttack(Id playerAttacked, int damageDealt)
    {
      AttackMsg attMsg = new AttackMsg(this.endpoint.getLocalNodeHandle().getId(),damageDealt);
      this.endpoint.route(playerAttacked, attMsg, null);
    }   
    
    // Model calls this when a chest update message is called
    public void updateChest(ChestObject chest)
    {
      System.out.println("sending poke message to " + chest.id);
      ObjectStateChangeMsg message = new ObjectStateChangeMsg(endpoint.getLocalNodeHandle().getId(),chest,false);
      endpoint.route(chest.id, message, null);
    }
    
   /**
   * Called when we receive a message directly.
   */
   public void deliver(Id id, Message message)
   {	  	 
       if (message instanceof MessageToSelf)
       {
         if (game != null)
         {
             if (game.regionChanged)
             {
                 game.regionChanged=false;
                 
                 Region newRegion = GameModel.getPlayersRegion(game.getPlayerPosition());
                 if (myTopic != null)
                 {                    
                   myScribe.unsubscribe(myTopic, this);                  
                   sendRegionMulticast(false);
                 }
                 
                 // Set the new region
                 game.region = newRegion;
                 
                 // This function removes everyone in old regions, because we will be re-adding them
                 //game.removePlayersByRegion(oldRegion,newRegion);                
          
                 // Get the object in this region
                 final Id thisChestID = pastIDF.buildId("RegionState (x:"+newRegion.x+" y:"+newRegion.y+")");
                 System.out.println("Chest ID: "+thisChestID);
                 this.lookupObject(thisChestID);               
                                 
                 System.out.println("Region is x:"+game.region.x+" y:"+game.region.y);                
                 myTopic = new Topic(new PastryIdFactory(node.getEnvironment()), "Region x:"+game.region.x+" y:"+game.region.y);                
                 this.subscribe(myTopic);               
                 System.out.println("NewTopic that we are subscribed to:"+myTopic);                
                 
                 // We need to tell everyone in our new region that we are there!
                 sendRegionMulticast(true);
             }
             
             if (game.positionChanged)
             {                    
                 game.positionChanged = false;
                 sendMoveEventMulticast(
                         game.getPlayerPosition(),
                         game.getPlayerRotation(),
                         game.getPlayerName());
             }            
         }
       }
       else if (message instanceof MoveDirectMsg)
       {
          MoveDirectMsg formMessage = (MoveDirectMsg)message;
          Vector3f position = new Vector3f(formMessage.x,formMessage.y,formMessage.z);
                   Quaternion rotation = new Quaternion(formMessage.rx, formMessage.ry, formMessage.rz, formMessage.rw);
                   PlayerData pd = new PlayerData(formMessage.from,position,rotation,formMessage.name);
                   game.updatePlayer(pd);        
       }
       else if (message instanceof ObjectReqMsg)
       {
         ObjectReqMsg orm = (ObjectReqMsg)message;
         //Is this a request for an object, or someone telling me that obj's data?
         if (orm.isRequest){        
           if (cachedChests.containsKey(orm.objectId)){
             // Its cached, get it and send out a response
             System.out.println("Recvd object Req Msg, I have it cached: "+orm.from+" "+orm.objectId);          
             ChestObject result = cachedChests.get(orm.objectId);
             ObjectReqMsg returnObjectMsg = new ObjectReqMsg(endpoint.getLocalNodeHandle().getId(),result.id,result.x,result.y,result.state, result.message);
             endpoint.route(orm.from, returnObjectMsg, null);
           }else{
             // We need to get it from past
             System.out.println("Recvd object Req Msg, Have to get it from Past: "+orm.from+" "+orm.objectId);                      
             this.lookupObject(orm.objectId,orm.from);            
           }
         }else{
           // Someone is telling us about this chest, lets update it!!!
           ChestObject newChest = new ChestObject(orm.objectId,orm.x,orm.y,orm.message);
           newChest.state = orm.state;
           game.updateObject(newChest);
         }
       } 
       else if (message instanceof ObjectStateChangeMsg)
       {        
         ObjectStateChangeMsg oscMessage = (ObjectStateChangeMsg)message;
         //This is a message that is someone trying to change a box's state
         if (oscMessage.coordinatorAck)
         {
           //This is a response saying the change was allowed.
           // First Update the box in our model's view
           ChestObject newChest = new ChestObject(oscMessage.objectId,oscMessage.x,oscMessage.y,oscMessage.message);
           newChest.state = oscMessage.state;
           game.updateObject(newChest);
           
           // Then broadcast to our group that the change has happened
           sendObjectChangeMulticast(newChest);          
         }
         else
         {
           //We are the coordinator, someone wants to make the change, let's facilitate         
             
           System.out.println("SOMEONE WANTS TO CHANGE A CHEST");
           
           // First Insert into the PAST Table
           ChestObject newChest = new ChestObject(oscMessage.objectId,oscMessage.x,oscMessage.y,oscMessage.message);
           newChest.state=oscMessage.state;
           this.pastUpdate(newChest);
           // Second Update our cache
           cachedChests.remove(newChest.id);
           cachedChests.put(newChest.id, newChest);
           
           // Finally acknowledge the asker
           oscMessage.coordinatorAck=true;
           Id sender = oscMessage.from;
           oscMessage.from=this.endpoint.getLocalNodeHandle().getId();
           this.endpoint.route(sender, message, null);          
         }                
       }
       else if (message instanceof AttackMsg)
       {
        System.out.println("OH NO!<?! I am attacked");
         sendBeenAttackedMulticast(((AttackMsg)message).damage);
       }                        
       
   }
 
     /**
      * This is Called when we receive a message from a scribe topic
      */
     public void deliver(Topic topic, ScribeContent content)
     {
       
         if(game != null && content instanceof MoveScribeMsg)
         {
             MoveScribeMsg message = (MoveScribeMsg)content;
 
             // Msg not from self
             if(message.from != endpoint.getLocalNodeHandle().getId())
             {
                 System.out.println("Move Rcvd: " + content);
                 Vector3f position = new Vector3f(message.x,message.y,message.z);
                 Quaternion rotation = new Quaternion(message.rx, message.ry, message.rz, message.rw);
                 PlayerData pd = new PlayerData(message.from,position,rotation,message.name);
                 game.updatePlayer(pd);
             }
         }
         else if(game != null && content instanceof RegionChangeScribeMsg)
         {
             RegionChangeScribeMsg message = (RegionChangeScribeMsg)content;
             if(message.from != endpoint.getLocalNodeHandle().getId())
             {
                 System.out.println("RegionChange Rcvd: "+content);
                 if(message.joinedRegion)
                 {
                     //sendMoveEventMulticast(game.getPlayerPosition(), game.getPlayerRotation());                      
                     MoveDirectMsg dmm = new MoveDirectMsg(
                             endpoint.getLocalNodeHandle().getId(),
                             game.getPlayerPosition(),
                             game.getPlayerRotation(),
                             game.getPlayerName());
                     endpoint.route(message.from, dmm,null);
                 }
                 else
                 {
                     //Someone has left our region. Fuck them, remove em                        
                     game.removePlayer(message.from);
                 }
             }                                
         }
         else if (game!=null && content instanceof ChatScribeMsg)
         {
           System.out.println("got chat from " + ((ChatScribeMsg)content).from);
           game.receiveChatMessage(((ChatScribeMsg)content).chatter);        
         }
         else if (game!=null && content instanceof TestMessage)
         {
           System.out.println("received TestMessage");
         }
         else if (game!=null && content instanceof ObjectStateChangeScribe)
         {
           ObjectStateChangeScribe message = (ObjectStateChangeScribe)content;
           if (message.from != endpoint.getLocalNodeHandle().getId())
           {
             ChestObject chest = new ChestObject(message.objectId,message.x,message.y,message.message);
             chest.state=message.state;
             game.updateObject(chest);
           }
         }
         else if (game!=null && content instanceof AttackScribe)
         {
           AttackScribe message = (AttackScribe)content;
           if (message.from != endpoint.getLocalNodeHandle().getId())
          {
             System.out.println("Haha, this other guy is getting attacked");
             game.updatePlayer(message.player);
           }
         }
     }
 
   
   public void update(NodeHandle handle, boolean joined)
   {
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
             game.removePlayer(handle.getId());
           }
       }
       
       if (joined){
         // We need to see if we are still hashed to this object
         Iterator it = cachedChests.entrySet().iterator();
         Vector<Id> cacheToRemove = new Vector<Id>();
         while (it.hasNext()){
           Map.Entry<Id,ChestObject> pairs = (Map.Entry)it.next();
           Id id = pairs.getKey();
           NodeHandleSet nhs = this.endpoint.localLookup(id, 5, false);
           if (nhs.size() == 1 && nhs.getHandle(0).equals(endpoint.getLocalNodeHandle())){
             System.out.println("We Still own this object, don't kick it outta cache");            
           }else{
             System.out.println("Someone has taken over ownership of the object, remove it from cache");
             cacheToRemove.add(id);
           }
         }
         
         for (int i=0; i<cacheToRemove.size(); i++){
           cachedChests.remove(cacheToRemove.get(i));          
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
 
   public void subscribeFailed(Collection<Topic> topic) {
 //    System.out.println("MyScribeClient.childFailed("+topic+")");
   }
    public void subscribeFailed(Topic topic) {
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
   
     
   //Now handle the Past Stuff
   class MyContinuation implements Continuation
     {
       GameAppOneRegion callee;
       Id reqFor;
       
       MyContinuation(GameAppOneRegion callee, Id reqFor) {
         this.callee = callee;
         this.reqFor = reqFor;
       }
         
       public void receiveResult(Object result) {
         if (result instanceof PastRegionObject)
         {
           PastRegionObject pc = (PastRegionObject)result;
 //          System.out.println("Received a "+pc);
           callee.onRegionObjectLookupResult(pc);
         }
         else if (result instanceof ChestObject)
         {
           ChestObject chest = (ChestObject)result;
 //          System.out.println("Received a "+chest);
           callee.onChestObjectLookupResult(chest,reqFor);
         }
       }
 
       public void receiveException(Exception result) {
         System.out.println("There was an error: "+result);
       }
     };
     
     public void lookupObject(final Id id)
     {
         //System.out.println("LOOKING UP REGION!");
         Continuation c = new GameAppOneRegion.MyContinuation(this,null);
         pastInst.lookup(id, c);
     }
     
     public void lookupObject(final Id id, final Id reqFor)
     {
         //System.out.println("LOOKING UP REGION!");
         Continuation c = new GameAppOneRegion.MyContinuation(this,reqFor);
         pastInst.lookup(id, c);
     }
     
     public void onRegionObjectLookupResult(PastRegionObject result)
     {
     //    System.out.println("GOT REGION UPDATE!");
         for(Iterator<Id> i = result.chestIds.iterator(); i.hasNext(); )
         {          
             // We have the region directory, lets ask coordinator for the object
             Id chestId = i.next();
             ObjectReqMsg askForObjectMsg = new ObjectReqMsg(endpoint.getLocalNodeHandle().getId(),chestId);
             endpoint.route(chestId,askForObjectMsg,null);
         }
     }
     
     public void onChestObjectLookupResult(ChestObject result, Id reqFor)
     {
       // System.out.println("GOT CHEST UPDATE!");
       // game.updateObject(result);
       // We need to forward these results on to the user who requested them
       cachedChests.put(result.id, result);
       ObjectReqMsg returnObjectMsg = new ObjectReqMsg(endpoint.getLocalNodeHandle().getId(),result.id,result.x,result.y,result.state, result.message);
       endpoint.route(reqFor, returnObjectMsg, null);
     }           
       
     public void pastUpdate(final ChestObject obj)
     {
       this.pastInst.insert(obj, new Continuation<Boolean[], Exception>() {
 	        // the result is an Array of Booleans for each insert
 	          public void receiveResult(Boolean[] results) {                     
 	          }          
 	          public void receiveException(Exception result) {
 	            System.out.println("Error storing "+obj);
   	          result.printStackTrace();
 	          }
 	        });    
     }
                        
     public Region[] getNewRegions(Region newReg, Region oldReg){
       if (oldReg.x==-1 && oldReg.y==-1){
         Region retReg[] = new Region[9];
         int count=0;
         for (int x=-1; x<2; x++){
           for (int y=-1; y<2; y++){                        
             retReg[count] = new Region(newReg.x+x,newReg.y+y);
             count++;
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
 
