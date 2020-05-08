 package game;
 
 import java.io.IOException;
 import java.net.InetAddress;
 import java.net.InetSocketAddress;
 import rice.environment.Environment;
 import rice.p2p.commonapi.Endpoint;
 import rice.p2p.commonapi.rawserialization.InputBuffer;
 import rice.p2p.past.Past;
 import rice.p2p.past.PastContent;
 import rice.p2p.past.PastImpl;
 import rice.p2p.past.rawserialization.PastContentDeserializer;
 import rice.pastry.NodeIdFactory;
 import rice.pastry.PastryNode;
 import rice.pastry.PastryNodeFactory;
 import rice.pastry.commonapi.PastryIdFactory;
 import rice.pastry.socket.SocketPastryNodeFactory;
 import rice.pastry.standard.RandomNodeIdFactory;
 import rice.persistence.LRUCache;
 import rice.persistence.MemoryStorage;
 import rice.persistence.PersistentStorage;
 import rice.persistence.Storage;
 import rice.persistence.StorageManagerImpl;
 
 // This Node will be the main node that all other game nodes connect to
 // This will place the game nodes into the initial group and send out the
 // initial information needed
 
 public class GameNode {
 
   //GameAppLeader app;
  //GameApp app;
  GameAppEventual app;
   
     
 	public GameNode(int bindport, InetSocketAddress bootaddress, Environment env, GameModel game) throws Exception {
 	    // Generate the NodeIds Randomly
 		NodeIdFactory nidFactory = new RandomNodeIdFactory(env);	    
 	    // construct the PastryNodeFactory, this is how we use rice.pastry.socket
 	    PastryNodeFactory factory = new SocketPastryNodeFactory(nidFactory, bindport, env);
 	    // construct a node
 	    PastryNode node = factory.newNode();	            
       
       /////////////////////////////////////
       // Now set up Past
       /////////////////////////////////////
       
       // The "hash function" for our DHT
       PastryIdFactory pastIDF = new rice.pastry.commonapi.PastryIdFactory(env);
       // create a different storage root for each node
       String storageDirectory = "./storage"+node.getId().hashCode();
       // create the persistent part
       Storage store = new PersistentStorage(pastIDF, storageDirectory, 4 * 1024 * 1024, node
           .getEnvironment());                  
       //Arguments - Node, StorageType, NumReplicas, Name
       Past pastImpl = new PastImpl(node, new StorageManagerImpl(pastIDF, store, new LRUCache(
           new MemoryStorage(pastIDF), 512 * 1024, node.getEnvironment())), 3, "GamePastInstance");           
       
       pastImpl.setContentDeserializer(new PastContentDeserializer() {
         public PastContent deserializePastContent(InputBuffer buf, Endpoint endpoint, short type) throws IOException {
           switch (type) {
             case ChestObject.TYPE:
               return new ChestObject(buf, endpoint);
             case PastRegionObject.TYPE:
               return new PastRegionObject(buf, endpoint);
           }
           throw new IllegalArgumentException("Invalid type:"+type);
         }
     });
 
       
                               
 	    // construct a new MyApp
	    //app = new GameApp(node, game,pastImpl,pastIDF);
       //app = new GameAppLeader(node, game);
  	  app = new GameAppEventual(node, game,pastImpl,pastIDF);
   
       
 	    node.boot(bootaddress);
 
 	    // the node may require sending several messages to fully boot into the ring
 	    synchronized(node) {
 	      while(!node.isReady() && !node.joinFailed()) {
 	        // delay so we don't busy-wait
 	        node.wait(500);
 	        
 	        // abort if can't join
 	        if (node.joinFailed()) {
 	          throw new IOException("Could not join the FreePastry ring.  Reason:"+node.joinFailedReason()); 
 	        }
 	      }       
 	    }
 
       game.regionChanged = true;  
 	    System.out.println("Finished creating new Game node "+node);	    		   	   
 	    //app.subscribe();            
 	}
 
         /*
 	public static void main(String[] args) throws Exception {
 		// Loads pastry configurations
 	    Environment env = new Environment();
 	    // disable the UPnP setting (in case you are testing this on a NATted LAN)
 	    env.getParameters().setString("nat_search_policy","never");
 	    
 	    try {
 	      // the port to use locally
 	      int bindport = Integer.parseInt(args[0]);
 	      // build the bootaddress from the command line args
 	      InetAddress bootaddr = InetAddress.getByName(args[1]);
 	      int bootport = Integer.parseInt(args[2]);
 	      InetSocketAddress bootaddress = new InetSocketAddress(bootaddr, bootport);
 	      // launch our node!
 	      GameNode gamenode = new GameNode(bindport, bootaddress, env);
 	    } catch (Exception e) {
 	      // remind user how to use
 	      System.out.println("Shit didnt work, did you pass the right args?");
 	      throw e;
 	    }
 	}
         */
 }	
