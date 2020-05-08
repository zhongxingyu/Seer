 package black;
 
 import hypeerweb.Node;
 import hypeerweb.SimplifiedNodeDomain;
 import hypeerweb.Node.State;
 
 import java.util.HashSet;
 import org.junit.After;
 import org.junit.Before;
 
 import testing.ExpectedResult;
 
 
 import junit.framework.TestCase;
 
 public class Node2 extends TestCase 
 {
 
     Node node0;
     Node node1;
     
     @Before
     public void setUp() throws Exception 
     {
        node0 = new Node(0);
        node1 = new Node(0);
     }
 
     @After
     public void tearDown() throws Exception 
     {
         node0 = new Node(0);
         node1 = new Node(0);
     }
     
     public void testAddNeighbor() 
     {
         node0.addNeighbor(node1);
         HashSet<Integer> a = node0.getNeighborsIds();
         assert(!a.isEmpty());
     }
     
     public void testRemoveNeighbor() 
     {
         node0.addNeighbor(node1);
         node0.removeNeighbor(node1);
         HashSet<Integer> a = node0.getNeighborsIds();
         assert(a.isEmpty());
      }
 
     public void testAddUpPointer() 
     {
        node0.addUpPointer(node1);
        HashSet<Integer> a = node0.getInvSurNeighborsIds();
        assert(!a.isEmpty());
     }
     
     public void testRemoveUpPointer() 
     {
         node0.addUpPointer(node1);
         node0.removeUpPointer(node1);
         HashSet<Integer> a = node0.getInvSurNeighborsIds();
         assert(a.isEmpty());
      }
     
     public void testAddDownPointer() 
     {
         node0.addDownPointer(node1);
         HashSet<Integer> a = node0.getSurNeighborsIds();
         assert(!a.isEmpty());
      }
      
      public void testRemoveDownPointer() 
      {
          node0.removeUpPointer(node1);
          HashSet<Integer> a = node0.getSurNeighborsIds();
          assert(a.isEmpty());
      }
     
     
     public void  testConstructSimplifiedNodeDomain() 
     {
         SimplifiedNodeDomain snd = node0.constructSimplifiedNodeDomain();
         ExpectedResult expectedResult = new ExpectedResult(1, 0);
         assert snd.equals(expectedResult);
     
     }
     
     
     
     public void testInsertSelf()
     {
         node0.setFold(node0);
         node0.setState(State.CAP);
         node1.insertSelf(node0);
         assert(node0.getHighestNeighbor().compareTo(node1) == 0);
         
         for (int i = 2; i < 15; i++)
         { 
             Node node = new Node(i);
             node.insertSelf(node0);
         }
        assert(node0.getHighestNeighbor().getWebId() == 7);
         assert(node0.getSurrogateFoldId() == 7);
     } 
     
     public void testFindCapNode()
     {
         for (int i = 2; i < 15; i++)
         { 
             Node node = new Node(i);
             node.insertSelf(node0);
         }
         
         assert (node0.findCapNode(node0).getWebId() == 7);
     }
    
     public void testRemoveFromHyPeerWeb()
     {
         for (int i = 2; i < 15; i++)
         { 
             Node node = new Node(i);
             node.setFold(node0);
             node.setState(State.CAP);
             node.insertSelf(node0);
         }
         
         for (int i = 2; i < 15; i++)
         { 
             node0.removeFromHyPeerWeb();
         }
         
         assert (node0.getHighestNeighbor() == Node.NULL_NODE);
     }
      
     
     public void testGetLowestNeighborWithoutChild()
     {
         
     }
 }
