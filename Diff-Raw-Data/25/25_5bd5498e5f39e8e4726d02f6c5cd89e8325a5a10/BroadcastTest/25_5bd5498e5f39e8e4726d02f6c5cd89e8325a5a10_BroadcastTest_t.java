 package black;
 
 import static org.junit.Assert.*;
 import hypeerweb.BroadcastVisitor;
 import hypeerweb.Contents;
 import hypeerweb.HyPeerWeb;
 import hypeerweb.Node;
 import hypeerweb.Parameters;
 import hypeerweb.SimplifiedNodeDomain;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 import testing.ExpectedResult;
 
 public class BroadcastTest {
 
    private static final int MAX_SIZE = 30;
     HyPeerWeb hypeerweb;
     @Before
     public void setUp() throws Exception {
         hypeerweb = HyPeerWeb.getSingleton();
         hypeerweb.clear();
     }
 
     @After
     public void tearDown() throws Exception {
         hypeerweb = HyPeerWeb.getSingleton();
         hypeerweb.clear();       
     }
    
   
    
    
    
 
     @Test
    public void testBroadcast() {        
        
        
         //create hyperweb with size=MAX_SIZE
         hypeerweb.clear();
         Node node0 = new Node(0);
         hypeerweb.addToHyPeerWeb(node0, null);
 
         for (int i = 1; i < MAX_SIZE; i++) {
             Node node = new Node(0);
             hypeerweb.addToHyPeerWeb(node, node0);
         }
         
         //broadcast from every node in the web
         Node broadcastNode;
         BroadcastVisitor bv;
         Parameters params;
         for(int i=0; i<MAX_SIZE; i++){
             broadcastNode = hypeerweb.getNode(i);
             bv = new BroadcastVisitor();
             String curKey = "message"+i;
             String curMessage = "test"+i;
             params = bv.createInitialParameters();
             params.set(curKey, curMessage);
             
             broadcastNode.accept(bv, params);
             
             for(int j=0;j<MAX_SIZE; j++){
                 Contents curContents = hypeerweb.getNode(j).getContents();
                 assertTrue(curContents.containsKey(curKey));
                 String message = (String) curContents.get(curKey);
                 assertTrue(message.equals(curMessage));                
             }
             
             
             
         }
         
         
    
     }
 }
