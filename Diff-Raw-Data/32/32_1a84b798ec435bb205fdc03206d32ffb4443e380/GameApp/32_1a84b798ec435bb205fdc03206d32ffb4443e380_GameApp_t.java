 package game;
 
 import java.util.Vector;
 import java.util.Collection;
 import com.jme3.math.Quaternion;
 import com.jme3.math.Vector3f;
 import java.io.IOException;
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
 import rice.p2p.commonapi.rawserialization.InputBuffer;
 import rice.p2p.commonapi.rawserialization.MessageDeserializer;
 import rice.p2p.past.Past;
 import rice.p2p.scribe.rawserialization.ScribeContentDeserializer;
 
 
 public class GameApp implements ScribeMultiClient, Application
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
   Vector<Topic> sendToTopics;
   //Vector3f lastPosition;
   Past pastInst;
   PastryIdFactory pastIDF;
   
   
   public GameApp(Node node, GameModel game, Past pastInst, PastryIdFactory pastIDF)
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
       
       for(int i = 0; i<sendToTopics.size(); i++)
       {
         myScribe.publish(sendToTopics.get(i), myMessage);         
       }    
    }	   
   
    // joined:   false = left region
    //           true = entered region
    public void sendRegionMulticast(boolean joined, Region newReg, Region oldReg)
    {
       RegionChangeScribeMsg message =
               new RegionChangeScribeMsg(endpoint.getLocalNodeHandle().getId(), joined);
       
       if (joined)
       {
           //If it is when we first joined the game, everyone must know
           if (oldReg.x==-1 && oldReg.y==-1){
             for(int i = 0; i<sendToTopics.size(); i++){
                 myScribe.publish(sendToTopics.get(i), message);
             }            
           }
           else
           {
               int diffX = newReg.x-oldReg.x;
               int diffY = newReg.y-oldReg.y;
 
               for (int i=-1; i<2; i++)
               {
                   Topic t;
                   if (diffX==0)
                   {
                     t = new Topic(new PastryIdFactory(
                             node.getEnvironment()),
                             "Region x:"+(newReg.x+i)+" y:"+(newReg.y+diffY));
                   }
                   else
                   {
                     t = new Topic(new PastryIdFactory(
                             node.getEnvironment()),
                             "Region x:"+(newReg.x+diffX)+" y:"+(newReg.y+i));         
                   }
                   myScribe.publish(t, message);
               }
           }          
       }
       else
       {
           // We are leaving a region, tell
           int diffX = newReg.x-oldReg.x;
           int diffY = newReg.y-oldReg.y;
           
           for (int i=-1; i<2; i++)
           {
             Topic t;
             if (diffX==0)
             {
               t = new Topic(new PastryIdFactory(
                       node.getEnvironment()),
                       "Region x:"+(oldReg.x+i)+" y:"+(oldReg.y-diffY));            
             }
             else
             {
               t = new Topic(new PastryIdFactory(
                       node.getEnvironment()),
                       "Region x:"+(oldReg.x-diffX)+" y:"+(oldReg.y+i));
             }
             myScribe.publish(t, message);
           }          
       }            
    }	   
    
    public void sendChatMessage(String message)
    {
     ChatScribeMsg chatMsg = new ChatScribeMsg(
             endpoint.getLocalNodeHandle().getId(),
             message);
     
     for(int i = 0; i<sendToTopics.size(); i++)
     {
       myScribe.publish(sendToTopics.get(i), chatMsg);
     }  
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
                   System.out.println("INITIAL topic:"+myTopic);                
                   myScribe.unsubscribe(myTopic, this);
                   
                   // We are leaving a region in this case, we need to tell everyone that we are leaving!
                   sendRegionMulticast(false,newRegion,game.region);
                 }
                 
                 // Set the new region
                 Region oldRegion = game.region;
                 game.region = newRegion;
                 
                 // This function removes everyone in old regions, because we will be re-adding them
                 //game.removePlayersByRegion(oldRegion,newRegion);                
          
                 // Get the object in this region
                 Region ret[] = getNewRegions(newRegion,oldRegion);
                 for (int i=0; i<ret.length; i++){                 
                   final Id thisChestID = pastIDF.buildId("RegionState (x:"+ret[i].x+" y:"+ret[i].y+")");
                   System.out.println("Chest ID: "+thisChestID);
                   this.lookupObject(thisChestID);
                 }
                                 
                 System.out.println("Region is x:"+game.region.x+" y:"+game.region.y);                
                 myTopic = new Topic(new PastryIdFactory(node.getEnvironment()), "Region x:"+game.region.x+" y:"+game.region.y);                
                 this.subscribe(myTopic);               
                 System.out.println("NewTopic that we are subscribed to:"+myTopic);                
                 sendToTopics = getSendToTopics(game.region);
                 
                 // We need to tell everyone in our new region that we are there!
                 sendRegionMulticast(true, game.region,oldRegion);                                             
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
     }
 
   
   /**
    * Called when you hear about a new neighbor.
    * Don't worry about this method for now.
    */
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
       GameApp callee;
       
       MyContinuation(GameApp callee) {
         this.callee = callee;
       }
         
       public void receiveResult(Object result) {
         if (result instanceof PastRegionObject)
         {
           PastRegionObject pc = (PastRegionObject)result;
           System.out.println("Received a "+pc);
           callee.onRegionObjectLookupResult(pc);
         }
         else if (result instanceof ChestObject)
         {
           ChestObject chest = (ChestObject)result;
           System.out.println("Received a "+chest);
           callee.onChestObjectLookupResult(chest);          
         }
       }
 
       public void receiveException(Exception result) {
         System.out.println("There was an error: "+result);
       }
     };
     
     public void lookupObject(final Id id)
     {
         System.out.println("LOOKING UP REGION!");
         Continuation c = new GameApp.MyContinuation(this);
         pastInst.lookup(id, c);
     }
     
     public void onRegionObjectLookupResult(PastRegionObject result)
     {
         System.out.println("GOT REGION UPDATE!");
         for(Iterator<Id> i = result.chestIds.iterator(); i.hasNext(); )
         {
             Id chestId = i.next();
             Continuation c = new GameApp.MyContinuation(this);
             pastInst.lookup(chestId, c);
             //game.updateObject(item);
         }
     }
     
     public void onChestObjectLookupResult(ChestObject result)
     {
         System.out.println("GOT CHEST UPDATE!");
         game.updateObject(result);
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
 
