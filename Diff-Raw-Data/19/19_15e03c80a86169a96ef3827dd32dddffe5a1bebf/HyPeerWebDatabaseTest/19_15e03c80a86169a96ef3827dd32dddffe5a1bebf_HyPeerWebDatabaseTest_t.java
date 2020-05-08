 package white;
 
 import hypeerweb.HyPeerWebDatabase;
 import hypeerweb.Node;
 import hypeerweb.SimplifiedNodeDomain;
 
 import java.sql.SQLException;
 import java.util.HashMap;
 
 
 import junit.framework.TestCase;
 
 public class HyPeerWebDatabaseTest extends TestCase {
     
      
        
     public void testInitializing() {
         HyPeerWebDatabase.initHyPeerWebDatabase("TestDatabase.db");
         HyPeerWebDatabase db = HyPeerWebDatabase.getSingleton();
         db.clear();
         assert(true);
     }
     
     public void testSavingNodes() throws SQLException {
         HyPeerWebDatabase.initHyPeerWebDatabase("TestDatabase.db");
         HyPeerWebDatabase db = HyPeerWebDatabase.getSingleton();
         
         HashMap<Integer,Node> nodes = new HashMap<Integer,Node>();
         
         Node a = new Node(1);
         nodes.put(a.getWebId(), a);
         Node b = new Node(2);
         nodes.put(b.getWebId(), b);
         
         db.save(nodes.values());
         
         SimplifiedNodeDomain loadedNode = db.getNode(1);
         assert(loadedNode.getWebId()==1);
         loadedNode = db.getNode(2);
         assert(loadedNode.getWebId()==2);       
         
         db.clear();
         assert(true);
     }
     
     public void testSavingNeighbors() throws SQLException {
         HyPeerWebDatabase.initHyPeerWebDatabase("TestDatabase.db");
         HyPeerWebDatabase db = HyPeerWebDatabase.getSingleton();
         
         HashMap<Integer,Node> nodes = new HashMap<Integer,Node>();
         
         Node a = new Node(1);        
         Node b = new Node(2);
         Node c = new Node(3);
         
         a.addNeighbor(b);
         b.addNeighbor(a);
         a.addNeighbor(c);
         c.addNeighbor(a);
         
         nodes.put(a.getWebId(), a);
         nodes.put(b.getWebId(), b);
        nodes.put(c.getWebId(), c);
         
         db.save(nodes.values());
         
         SimplifiedNodeDomain simpleDomain = db.getNode(1);
         assert(simpleDomain.getNeighbors().size() == 2);
         assert(simpleDomain.getNeighbors().contains(2));
         assert(simpleDomain.getNeighbors().contains(3));
         
         simpleDomain = db.getNode(2);
         assert(simpleDomain.getNeighbors().size() == 1);
         assert(simpleDomain.getNeighbors().contains(1));
         assert(!simpleDomain.getNeighbors().contains(3));
         
         simpleDomain = db.getNode(3);
         assert(simpleDomain.getNeighbors().size() == 1);
         assert(simpleDomain.getNeighbors().contains(1));
         assert(!simpleDomain.getNeighbors().contains(2));
         
              
         
         db.clear();
         assert(true);
     }
     
     
     public void testSavingDownPointers() throws SQLException {
         HyPeerWebDatabase.initHyPeerWebDatabase("TestDatabase.db");
         HyPeerWebDatabase db = HyPeerWebDatabase.getSingleton();
         
         HashMap<Integer,Node> nodes = new HashMap<Integer,Node>();
         
         Node a = new Node(1);        
         Node b = new Node(2);
         Node c = new Node(3);
         
         a.addDownPointer(b);
         b.addDownPointer(a);
         a.addDownPointer(c);
         c.addDownPointer(a);
         
         nodes.put(a.getWebId(), a);
         nodes.put(b.getWebId(), b);
        nodes.put(c.getWebId(), c);
         
         db.save(nodes.values());
         
         SimplifiedNodeDomain simpleDomain = db.getNode(1);
         assert(simpleDomain.getDownPointers().size() == 2);
         assert(simpleDomain.getDownPointers().contains(2));
         assert(simpleDomain.getDownPointers().contains(3));
         
         simpleDomain = db.getNode(2);
         assert(simpleDomain.getDownPointers().size() == 1);
         assert(simpleDomain.getDownPointers().contains(1));
         assert(!simpleDomain.getDownPointers().contains(3));
         
         simpleDomain = db.getNode(3);
         assert(simpleDomain.getDownPointers().size() == 1);
         assert(simpleDomain.getDownPointers().contains(1));
         assert(!simpleDomain.getDownPointers().contains(2));
         
              
         
         db.clear();
         assert(true);
     }
     
     public void testSavingUpPointers() throws SQLException {
         HyPeerWebDatabase.initHyPeerWebDatabase("TestDatabase.db");
         HyPeerWebDatabase db = HyPeerWebDatabase.getSingleton();
         
         HashMap<Integer,Node> nodes = new HashMap<Integer,Node>();
         
         Node a = new Node(1);        
         Node b = new Node(2);
         Node c = new Node(3);
         
         a.addUpPointer(b);
        b.addDownPointer(a);
        
         b.addUpPointer(a);
        a.addDownPointer(b);
        
         a.addUpPointer(c);
        c.addDownPointer(a);
        
         c.addUpPointer(a);
        a.addDownPointer(c);
         
         nodes.put(a.getWebId(), a);
         nodes.put(b.getWebId(), b);
        nodes.put(c.getWebId(), c);
         
         db.save(nodes.values());
         
         SimplifiedNodeDomain simpleDomain = db.getNode(1);
         assert(simpleDomain.getUpPointers().size() == 2);
         assert(simpleDomain.getUpPointers().contains(2));
         assert(simpleDomain.getUpPointers().contains(3));
         
         simpleDomain = db.getNode(2);
         assert(simpleDomain.getUpPointers().size() == 1);
         assert(simpleDomain.getUpPointers().contains(1));
         assert(!simpleDomain.getUpPointers().contains(3));
         
         simpleDomain = db.getNode(3);
         assert(simpleDomain.getUpPointers().size() == 1);
         assert(simpleDomain.getUpPointers().contains(1));
         assert(!simpleDomain.getUpPointers().contains(2));
         
              
         
         db.clear();
         assert(true);
     }
     
     
 }
