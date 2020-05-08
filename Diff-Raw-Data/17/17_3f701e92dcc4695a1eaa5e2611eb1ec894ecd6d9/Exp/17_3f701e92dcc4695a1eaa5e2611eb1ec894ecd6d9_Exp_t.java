 package providedtests.phase2;
 
 import database.HyPeerWebDatabase;
 import hypeerweb.HyPeerWeb;
 import node.Node;
 import node.SimplifiedNodeDomain;
 import node.WebId;
 import node.ExpectedResult;
 
 public class Exp {
     public static HyPeerWeb hypeerweb = null;
     private static final int PHASE_1_TEST_COUNT = 66;
     private static final int PHASE_2_TEST_COUNT = 10912;
     private static int errorCount = 0;
 
     public static void main(String[] args) {
         HyPeerWebDatabase.initHyPeerWebDatabase(null);
         HyPeerWebDatabase.clear();
         hypeerweb = HyPeerWeb.getSingleton();
         testPhase1();
         testPhase2();
     }
     
     private static void testPhase1() {
         System.out.println("Testing Phase 1");
         hypeerweb.clear();
         errorCount = 0;
         testSimpleNodeOperationsOnNodeBeforeItHasBeenAddedToHyPeerWeb();
         testAddNode();
         testAdding();
         testRemoving();
         testTheCreationOfAHyPeerWebFromAnExistingDababase();
         
         if (errorCount > 0) {
             double decimalPercent = ((double)PHASE_1_TEST_COUNT - (double) errorCount) / (double) PHASE_1_TEST_COUNT;
             int integerPercent = (int) (decimalPercent * 100.0d);
             System.out.println("    Database Error: Phase 1 Score = " + integerPercent + "%");
         } else {
             System.out.println("    No Database Errors: Phase 1 Score = 100%");
         }
         
     }
 
     private static void testSimpleNodeOperationsOnNodeBeforeItHasBeenAddedToHyPeerWeb() {
         boolean error = false;
 
         // testing Node(Integer) constructor
         Node node = new Node(3);
         SimplifiedNodeDomain simpleDomain = node.constructSimplifiedNodeDomain();
 
         if (simpleDomain.getWebId() != 3) {
             System.err.println("When invoking Node(int) constructor as 'new Node(3)', the webId should be 3.");
             error = true;
             errorCount++;
         }
         if (simpleDomain.getNeighbors().size() != 0) {
             System.err.println("When invoking Node(int) constructor as 'new Node(3)', the number of neighbors should be 0.");
             error = true;
             errorCount++;
 
         }
         if (simpleDomain.getUpPointers().size() != 0) {
             System.err.println("When invoking Node(int) constructor as 'new Node(3)', the number of upPointers should be 0.");
             error = true;
             errorCount++;
 
         }
         if (simpleDomain.getDownPointers().size() != 0) {
             System.err.println("When invoking Node(int) constructor as 'new Node(3)', the number of downPointers should be 0.");
             error = true;
             errorCount++;
         }
         if (simpleDomain.getFold() != 3) {
             System.err.println("When invoking Node(int) constructor as 'new Node(3)', the fold should be 3.");
             error = true;
             errorCount++;
         }
         
         if (simpleDomain.getSurrogateFold() != -1) {
             System.err.println("When invoking Node(int) constructor as 'new Node(3)', the surrogateFold should be null.");
             error = true;
             errorCount++;
         }
         
         if (simpleDomain.getInverseSurrogateFold() != -1) {
             System.err.println("When invoking Node(int) constructor as 'new Node(3)', the inverseSurrogateFold should be null.");
             error = true;
             errorCount++;
         }
 
         node.setWebId(new WebId(4));
         simpleDomain = node.constructSimplifiedNodeDomain();
         if (simpleDomain.getWebId() != 4) {
             System.err.println("When invoking node.setWebId(WebId) the webId should now be 4 but is " + simpleDomain.getWebId() + ".");
             error = true;
             errorCount++;
         }
 
         error = testAddingAndRemovingNeighbors(node, error);
         error = testAddingAndRemovingUpPointers(node, error);
         error = testAddingAndRemovingDownPointers(node, error);
 
         Node node5 = new Node(5);
         node.setFold(node5);
         simpleDomain = node.constructSimplifiedNodeDomain();
         if (simpleDomain.getFold() != 5) {
             System.err.println("When invoking node.setFold(Node) with node5 the fold should now be node 5 but is node " + simpleDomain.getFold() + ".");
             error = true;
             errorCount++;
         }
         
         node.setFold(Node.NULL_NODE);
         simpleDomain = node.constructSimplifiedNodeDomain();
         if (simpleDomain.getFold() != -1) {
             System.err.println("When invoking node.setFold(Node) with Node.NULL_NODE the fold should now be Node.NULL_NODE but is node "    + simpleDomain.getSurrogateFold() + ".");
             error = true;
             errorCount++;
         }
 
         Node node6 = new Node(6);
         node.setSurrogateFold(node6);
         simpleDomain = node.constructSimplifiedNodeDomain();
         if (simpleDomain.getSurrogateFold() != 6) {
             System.err.println("When invoking node.setSurrogateFold(Node) with node6 the surrogateFold should now be node 6 but is node "    + simpleDomain.getSurrogateFold() + ".");
             error = true;
             errorCount++;
         }
         
         node.setSurrogateFold(Node.NULL_NODE);
         simpleDomain = node.constructSimplifiedNodeDomain();
         if (simpleDomain.getSurrogateFold() != -1) {
             System.err.println("When invoking node.setSurrogateFold(Node) with Node.NULL_NODE the surrogateFold should now be Node.NULL_NODE but is node "    +
                                simpleDomain.getSurrogateFold() + ".");
             error = true;
             errorCount++;
         }
 
         Node node7 = new Node(7);
         node.setInverseSurrogateFold(node7);
         simpleDomain = node.constructSimplifiedNodeDomain();
         if (simpleDomain.getInverseSurrogateFold() != 7) {
             System.err.println("When invoking node.setInverseSurrogateFold(Node) with node7 the inverseSurrogateFold should now be node 7 but is node "    + 
                     simpleDomain.getSurrogateFold() + ".");
             error = true;
             errorCount++;
         }
         
         node.setSurrogateFold(Node.NULL_NODE);
         simpleDomain = node.constructSimplifiedNodeDomain();
         if (simpleDomain.getSurrogateFold() != -1) {
             System.err.println("When invoking node.setSurrogateFold(Node) with Node.NULL_NODE the surrogateFold should now be Node.NULL_NODE but is node "    +
                                simpleDomain.getSurrogateFold() + ".");
             error = true;
             errorCount++;
         }
 
         if (!error) {
             System.out.println("    No errors in simple node operations.");
         }
 
         hypeerweb.clear();
     }
 
     private static boolean testAddingAndRemovingNeighbors(Node node,
             boolean error) {
         Node node0 = new Node(0);
         Node node1 = new Node(1);
         Node node2 = new Node(2);
         
         Node neighbor0 = node0;
         Node neighbor1 = node1;
         Node neighbor2 = node2;
 
         node.addNeighbor(neighbor0);
         SimplifiedNodeDomain simpleDomain = node.constructSimplifiedNodeDomain();
         if (simpleDomain.getNeighbors().size() != 1) {
             System.err.println("When invoking addNeighbor(Node) on a node with no neighbors the number of neighbors should be 1 but is "    + simpleDomain.getNeighbors().size() + ".");
         }
         if (!simpleDomain.getNeighbors().contains(0)) {
             System.err.println("When invoking addNeighbor(Node) on a node with no neighbors node 1 should be a neighbor but is not.");
         }
 
         node.addNeighbor(neighbor1);
         node.addNeighbor(neighbor2);
         simpleDomain = node.constructSimplifiedNodeDomain();
         if (simpleDomain.getNeighbors().size() != 3) {
             System.err.println("After adding node0, node1, and node2 as neighbors to node, the number of neighbors should be 3 but is "    + simpleDomain.getNeighbors().size() + ".");
         }
         if (!simpleDomain.getNeighbors().contains(0)) {
             System.err.println("After adding node0, node1, and node2 as neighbors to node, node 0 should be a neighbor but is not.");
         }
         if (!simpleDomain.getNeighbors().contains(1)) {
             System.err.println("After adding node0, node1, and node2 as neighbors to node, node 1 should be a neighbor but is not.");
         }
         if (!simpleDomain.getNeighbors().contains(2)) {
             System.err.println("After adding node0, node1, and node2 as neighbors to node, node 2 should be a neighbor but is not.");
         }
 
         node.removeNeighbor(neighbor0);
         node.removeNeighbor(neighbor2);
         simpleDomain = node.constructSimplifiedNodeDomain();
         if (simpleDomain.getNeighbors().size() != 1) {
             System.err.println("After adding node0, node1, and node2 as neighbors to node and then removing node0 and node2 as neighbors, the number of neighbors should be 1 but is " +
                                simpleDomain.getNeighbors().size() + ".");
         }
         if (simpleDomain.getNeighbors().contains(0)) {
             System.err.println("After adding node0, node1, and node2 as neighbors to node and then removing node0 and node2 as neighbors, node 0 should NOT be a neighbor but is.");
         }
         if (simpleDomain.getNeighbors().contains(2)) {
             System.err.println("After adding node0, node1, and node2 as neighbors to node and then removing node0 and node2 as neighbors, node 2 should NOT be a neighbor but is.");
         }
         if (!simpleDomain.getNeighbors().contains(1)) {
             System.err.println("After adding node0, node1, and node2 as neighbors to node and then removing node0 and node2 as neighbors, node 1 should be a neighbor but is NOT.");
         }
 
         node.removeNeighbor(neighbor1);
         simpleDomain = node.constructSimplifiedNodeDomain();
         if (simpleDomain.getNeighbors().size() != 0) {
             System.err.println("After adding node0, node1, and node2 as neighbors to node and then removing node0, node1, and node2 as neighbors, " +
                                "the number of neighbors should be 0 but is "    + simpleDomain.getNeighbors().size());
         }
 
         return error;
     }
 
     private static boolean testAddingAndRemovingUpPointers(Node node,
             boolean error) {
         Node node0 = new Node(0);
         Node node1 = new Node(1);
         Node node2 = new Node(2);
         
         Node upPointer0 = node0;
         Node upPointer1 = node1;
         Node upPointer2 = node2;
 
         node.addUpPointer(upPointer0);
         SimplifiedNodeDomain simpleDomain = node.constructSimplifiedNodeDomain();
         if (simpleDomain.getUpPointers().size() != 1) {
             System.err.println("When invoking addUpPointer(Node) on a node with no upPointers the number of upPointers should be 1 but is "    + simpleDomain.getUpPointers().size() + ".");
         }
         if (!simpleDomain.getUpPointers().contains(0)) {
             System.err.println("When invoking addUpPointer(Node) on a node with no upPointers node 1 should be an upPointer but is not.");
         }
 
         node.addUpPointer(upPointer1);
         node.addUpPointer(upPointer2);
         simpleDomain = node.constructSimplifiedNodeDomain();
         if (simpleDomain.getUpPointers().size() != 3) {
             System.err.println("After adding node0, node1, and node2 as upPointers to node, the number of upPointers should be 3 but is "    + simpleDomain.getUpPointers().size() + ".");
         }
         if (!simpleDomain.getUpPointers().contains(0)) {
             System.err.println("After adding node0, node1, and node2 as upPointers to node, node 0 should be a upPointer but is not.");
         }
         if (!simpleDomain.getUpPointers().contains(1)) {
             System.err.println("After adding node0, node1, and node2 as upPointers to node, node 1 should be a upPointer but is not.");
         }
         if (!simpleDomain.getUpPointers().contains(2)) {
             System.err.println("After adding node0, node1, and node2 as upPointers to node, node 2 should be a upPointer but is not.");
         }
 
         node.removeUpPointer(upPointer0);
         node.removeUpPointer(upPointer2);
         simpleDomain = node.constructSimplifiedNodeDomain();
         if (simpleDomain.getUpPointers().size() != 1) {
             System.err.println("After adding node0, node1, and node2 as upPointers to node and then removing node0 and node2 as upPointers, the number of upPointers should be 1 but is " +
                                simpleDomain.getUpPointers().size());
         }
         if (simpleDomain.getUpPointers().contains(0)) {
             System.err.println("After adding node0, node1, and node2 as upPointers to node and then removing node0 and node2 as upPointers, node 0 should NOT be a upPointer but is.");
         }
         if (simpleDomain.getUpPointers().contains(2)) {
             System.err.println("After adding node0, node1, and node2 as upPointers to node and then removing node0 and node2 as upPointers, node 2 should NOT be a upPointer but is.");
         }
         if (!simpleDomain.getUpPointers().contains(1)) {
             System.err.println("After adding node0, node1, and node2 as upPointers to node and then removing node0 and node2 as upPointers, node 1 should be a upPointer but is NOT.");
         }
 
         node.removeUpPointer(upPointer1);
         simpleDomain = node.constructSimplifiedNodeDomain();
         if (simpleDomain.getUpPointers().size() != 0) {
             System.err.println("After adding node0, node1, and node2 as upPointers to node and then removing node0, node1, and node2 as upPointers, " +
                                "the number of upPointers should be 0 but is " + simpleDomain.getUpPointers().size() + ".");
         }
 
         return error;
     }
 
     private static boolean testAddingAndRemovingDownPointers(Node node,
             boolean error) {
         Node node0 = new Node(0);
         Node node1 = new Node(1);
         Node node2 = new Node(2);
         
         Node downPointer0 = node0;
         Node downPointer1 = node1;
         Node downPointer2 = node2;
 
         node.addDownPointer(downPointer0);
         SimplifiedNodeDomain simpleDomain = node.constructSimplifiedNodeDomain();
         if (simpleDomain.getDownPointers().size() != 1) {
             System.err.println("When invoking addDownPointer(Node) on a node with no downPointers the number of downPointers should be 1 but is " +
                                simpleDomain.getDownPointers().size() + ".");
         }
         if (!simpleDomain.getDownPointers().contains(0)) {
             System.err.println("When invoking addDownPointer(Node) on a node with no downPointers node 1 should be a downPointer but is not.");
         }
 
         node.addDownPointer(downPointer1);
         node.addDownPointer(downPointer2);
         simpleDomain = node.constructSimplifiedNodeDomain();
         if (simpleDomain.getDownPointers().size() != 3) {
             System.err.println("After adding node0, node1, and node2 as downPointers to node, the number of downPointers should be 3 but is "    + 
                                simpleDomain.getDownPointers().size() + ".");
         }
         if (!simpleDomain.getDownPointers().contains(0)) {
             System.err.println("After adding node0, node1, and node2 as downPointers to node, node 0 should be a downPointer but is not.");
         }
         if (!simpleDomain.getDownPointers().contains(1)) {
             System.err.println("After adding node0, node1, and node2 as downPointers to node, node 1 should be a downPointer but is not.");
         }
         if (!simpleDomain.getDownPointers().contains(2)) {
             System.err.println("After adding node0, node1, and node2 as downPointers to node, node 2 should be a downPointer but is not.");
         }
 
         node.removeDownPointer(downPointer0);
         node.removeDownPointer(downPointer2);
         simpleDomain = node.constructSimplifiedNodeDomain();
         if (simpleDomain.getDownPointers().size() != 1) {
             System.err.println("After adding node0, node1, and node2 as downPointers to node and then removing node0 and node2 as downPointers, " + 
                                "the number of downPointers should be 1 but is "    + simpleDomain.getDownPointers().size() + ".");
         }
         if (simpleDomain.getDownPointers().contains(0)) {
             System.err.println("After adding node0, node1, and node2 as downPointers to node and then removing node0 and node2 as downPointers, node 0 should NOT be a downPointer but is.");
         }
         if (simpleDomain.getDownPointers().contains(2)) {
             System.err.println("After adding node0, node1, and node2 as downPointers to node and then removing node0 and node2 as downPointers, node 2 should NOT be a downPointer but is.");
         }
         if (!simpleDomain.getDownPointers().contains(1)) {
             System.err.println("After adding node0, node1, and node2 as downPointers to node and then removing node0 and node2 as downPointers, node 1 should be a downPointer but is NOT.");
         }
 
         node.removeDownPointer(downPointer1);
         simpleDomain = node.constructSimplifiedNodeDomain();
         if (simpleDomain.getDownPointers().size() != 0) {
             System.err.println("After adding node0, node1, and node2 as downPointers to node and then removing node0, node1, and node2 as downPointers, " +
                                "the number of downPointers should be 0 but is " + simpleDomain.getDownPointers().size() + ".");
         }
 
         return error;
     }
 
     private static void testAddNode() {
         boolean error = false;
         hypeerweb.clear();
         Node node0 = new Node(0);
         hypeerweb.addNode(node0);
         Node node = hypeerweb.getNode(0);
         if (node == null) {
             System.err.println("When a node is added to the HyPeerWeb, it should be in the database but hypeerweb.getNode(0) returned null.");
         }
         if (node != null && node != node0) {
             System.err.println("When a node is added to the HyPeerWeb, it should not be a copy of the node added but it appears to be.");
             error = true;
             errorCount++;
 
         }
         if (!hypeerweb.contains(node0)) {
             System.err.println("After adding a newly created node0 to the HyPeerWeb, the HyPeerWeb does not contain node0.");
             error = true;
             errorCount++;
         }
         if (node != null && !node.equals(node0)) {
             System.err.println("After adding a newly created node0 to the HyPeerWeb, the node in theHyPeerWeb does not equal node0.");
             error = true;
             errorCount++;
         }
 
         SimplifiedNodeDomain simpleDomain = hypeerweb.getNode(0).constructSimplifiedNodeDomain();
         if (simpleDomain == null) {
             System.err.println("After adding a newly created node0 to the HyPeerWeb, the node does not exist in the database.");
             error = true;
             errorCount++;
         }
         if (simpleDomain != null && !simpleDomain.equals(node0.constructSimplifiedNodeDomain())) {
             System.err.println("After adding a newly created node0 to the HyPeerWeb, the node in the database is not the same as the node added.");
             error = true;
             errorCount++;
         }
 
         if (!error) {
             System.out.println("    No errors found while testing the addition of a node to the HyPeerWeb.");
         }
 
         hypeerweb.clear();
     }
 
     private static void testAdding() {
         boolean error = false;
         hypeerweb.clear();
 
         Node node0 = new Node(0);
         Node node1 = new Node(1);
         Node node2 = new Node(2);
         Node node3 = new Node(3);
         Node node4 = new Node(4);
         Node node5 = new Node(5);
         Node node6 = new Node(6);
         Node node7 = new Node(7);
         
         Node neighbor1 = node1;
         Node neighbor2 = node2;
         
         Node upPointer3 = node3;
         
         Node downPointer4 = node4;
         Node downPointer5 = node5;
 
         hypeerweb.addNode(node0);
 
         node0.addNeighbor(neighbor1);
         node0.addNeighbor(neighbor2);
         SimplifiedNodeDomain simpleDomain = hypeerweb.getNode(0).constructSimplifiedNodeDomain();
         if (simpleDomain == null) {
             System.err.println("When adding node1 and node2 as neighbors to node0 which is already in the database,\n" + "    node0 disappeared and is not in the database.");
         }
         if (simpleDomain != null && simpleDomain.getNeighbors().size() != 2) {
             System.err.println("When adding node1 and node2 as neighbors to node0 which is already in the database,\n" +
                                "    there should be two neighbors of node0 in the database but there are not.");
             error = true;
             errorCount++;
         }
         if (simpleDomain != null && !simpleDomain.getNeighbors().contains(1)) {
             System.err.println("When adding node1 and node2 as neighbors to node0 which is already in the database,\n" +
                                "    the persistant version of node0 should have node1 as a neighbor but does not.");
             error = true;
             errorCount++;
         }
         if (simpleDomain != null && !simpleDomain.getNeighbors().contains(2)) {
             System.err.println("When adding node1 and node2 as neighbors to node0 which is already in the database,\n" +
                                "    the persistant version of node0 should have node2 as a neighbor but does not.");
             error = true;
             errorCount++;
         }
 
         node0.addUpPointer(upPointer3);
         simpleDomain = hypeerweb.getNode(0).constructSimplifiedNodeDomain();
         if (simpleDomain == null) {
             System.err.println("When adding node3 as an UpPointer to node0 which is already in the database,\n"    +
                                "    node0 disappeared and is not in the database.");
             error = true;
             errorCount++;
         }
         if (simpleDomain != null && simpleDomain.getUpPointers().size() != 1) {
             System.err
                     .println("When adding node1 and node2 as neighbors to node0 which is already in the database,\n" + 
                              "    there should be two neighbors of node0 in the database but there are not.");
             error = true;
             errorCount++;
         }
         if (simpleDomain != null && !simpleDomain.getUpPointers().contains(3)) {
             System.err
                     .println("When adding node3 as an UpPointer to node0 which is already in the database,\n" + 
                              "    the persistant version of node0 should have node3 as an UpPointer but does not.");
             error = true;
             errorCount++;
         }
 
         node0.addDownPointer(downPointer4);
         node0.addDownPointer(downPointer5);
         simpleDomain = hypeerweb.getNode(0).constructSimplifiedNodeDomain();
         if (simpleDomain == null) {
             System.err.println("When adding node1 and node2 as downPointers to node0 which is already in the database,\n" + 
                                "    node0 disappeared and is not in the database.");
             error = true;
             errorCount++;
         }
         if (simpleDomain != null && simpleDomain.getDownPointers().size() != 2) {
             System.err.println("When adding node1 and node2 as downPointers to node0 which is already in the database,\n" +
                                "    there should be two downPointers of node0 in the database but there are not.");
             error = true;
             errorCount++;
         }
         if (simpleDomain != null && !simpleDomain.getDownPointers().contains(4)) {
             System.err.println("When adding node4 and node5 as DownPointers to node0 which is already in the database,\n" +
                                "    the persistant version of node0 should have node4 as a DownPointer but does not.");
             error = true;
             errorCount++;
         }
         if (simpleDomain != null && !simpleDomain.getDownPointers().contains(5)) {
             System.err.println("When adding node4 and node5 as DownPointers to node0 which is already in the database,\n" +
                                "    the persistant version of node0 should have node5 as a DownPointer but does not.");
             error = true;
             errorCount++;
         }
 
         node0.setFold(node6);
         simpleDomain = hypeerweb.getNode(0).constructSimplifiedNodeDomain();
         if (simpleDomain == null) {
             System.err.println("When setting node6 as the fold to node0 which is already in the database,\n" +
                                "    node0 disappeared and is not in the database.");
             error = true;
             errorCount++;
         }
         if (simpleDomain != null && simpleDomain.getFold() != 6) {
             System.err.println("When setting node6 as the fold to node0 which is already in the database,\n" +
                                "    the persistant version of node0 should have node6 as its fold but does not, it has node " + simpleDomain.getFold() + ".");
             error = true;
             errorCount++;
         }
 
         node0.setSurrogateFold(node7);
         simpleDomain = hypeerweb.getNode(0).constructSimplifiedNodeDomain();
         if (simpleDomain == null) {
             System.err.println("When setting node7 as the surrogateFold to node0 which is already in the database,\n" +
                                "    node0 disappeared and is not in the database.");
             error = true;
             errorCount++;
         }
         if (simpleDomain != null && simpleDomain.getSurrogateFold() != 7) {
             System.err.println("When setting node7 as the surrogateFold to node0 which is already in the database,\n" +
                                "    the persistant version of node0 should have node7 as its surrogateFold but does not, it is node "    + simpleDomain.getSurrogateFold() + ".");
             error = true;
             errorCount++;
         }
 
         node0.setSurrogateFold(Node.NULL_NODE);
         simpleDomain = hypeerweb.getNode(0).constructSimplifiedNodeDomain();
         if (simpleDomain == null) {
             System.err.println("When setting the surrogateFold of node0 (which is already in the database) to null,\n" +
                                "    node0 disappeared and is not in the database.");
             error = true;
             errorCount++;
         }
         if (simpleDomain != null && simpleDomain.getSurrogateFold() != -1) {
             System.err.println("When setting the surrogateFold of node0 (which is already in the database) to null\n" +
                                "    the persistant version of node0 should have the surrogateFold as null but does not, " + "    it is node "    + simpleDomain.getSurrogateFold() + ".");
             error = true;
             errorCount++;
         }
 
         if (!error) {
             System.out.println("    No errors found while testing the addition of information to a node already in the HyPeerWeb.");
         }
 
         hypeerweb.clear();
     }
 
     private static void testRemoving() {
         boolean error = false;
         hypeerweb.clear();
 
         Node node0 = new Node(0);
         Node node1 = new Node(1);
         Node node2 = new Node(2);
         Node node3 = new Node(3);
         Node node4 = new Node(4);
         Node node5 = new Node(5);
         Node node6 = new Node(6);
 
         hypeerweb.addNode(node0);
 
         node0.addNeighbor(node1);
         node0.addNeighbor(node2);
         node0.addUpPointer(node3);
         node0.addUpPointer(node4);
         node0.addDownPointer(node5);
         node0.addDownPointer(node6);
 
         node0.removeNeighbor(node1);
         SimplifiedNodeDomain simpleDomain = node0.constructSimplifiedNodeDomain();
         if (simpleDomain.getNeighbors().contains(1)) {
             System.err.println("When removing node1 as a neighbor of node0 which is already in the HyPeerWeb,\n" +
                                    "    node1 was not removed from node0 in the database.");
             error = true;
             errorCount++;
         }
         if (!simpleDomain.getNeighbors().contains(2)) {
             System.err.println("When removing node1 as a neighbor of node0 which is already in the HyPeerWeb,\n" +
                                "    node2 was also removed.");
             error = true;
             errorCount++;
         }
 
         node0.removeNeighbor(node2);
         simpleDomain = node0.constructSimplifiedNodeDomain();
         if (simpleDomain.getNeighbors().contains(2)) {
             System.err.println("When removing node2 as a neighbor of node0 which is already in the HyPeerWeb,\n" +
                                    "    node2 was not removed.");
             error = true;
             errorCount++;
         }
 
         node0.removeUpPointer(node4);
         simpleDomain = node0.constructSimplifiedNodeDomain();
         if (simpleDomain.getUpPointers().contains(4)) {
             System.err.println("When removing node4 as an UpPointer of node0 which is already in the HyPeerWeb,\n" +
                                "    node4 was not removed from node0 in the database.");
             error = true;
             errorCount++;
         }
         if (!simpleDomain.getUpPointers().contains(3)) {
             System.err.println("When removing node4 as an UpPointer of node0 which is already in the HyPeerWeb,\n" +
                                    "    node3 was also removed.");
             error = true;
             errorCount++;
         }
 
         node0.removeDownPointer(node5);
         simpleDomain = node0.constructSimplifiedNodeDomain();
         if (simpleDomain.getDownPointers().contains(5)) {
             System.err.println("When removing node5 as a DownPointer of node0 which is already in the HyPeerWeb,\n" +
                                "    node5 was not removed from node0 in the database.");
             error = true;
             errorCount++;
         }
         if (!simpleDomain.getDownPointers().contains(6)) {
             System.err.println("When removing node1 as a DownPointer of node0 which is already in the HyPeerWeb,\n" +
                                    "    node6 was also removed.");
             error = true;
             errorCount++;
         }
 
         node0.removeDownPointer(node6);
         simpleDomain = node0.constructSimplifiedNodeDomain();
         if (simpleDomain.getDownPointers().contains(6)) {
             System.err.println("When removing node6 as a DownPointer of node0 which is already in the HyPeerWeb,\n"    + 
                                "    node6 was not removed.");
             error = true;
             errorCount++;
         }
 
         if (!error) {
             System.out.println("    No errors found while testing the removal of information from a node already in the HyPeerWeb.");
         }
 
         hypeerweb.clear();
     }
 
     private static void testTheCreationOfAHyPeerWebFromAnExistingDababase() {
         boolean error = false;
         hypeerweb.clear();
 
         Node node0 = new Node(0);
         Node node1 = new Node(1);
         Node node2 = new Node(2);
         
         Node neighbor0 = node0;
         Node neighbor1 = node1;
         Node neighbor2 = node2;
         
         Node upPointer2 = node2;
         
         Node downPointer1 = node1;
 
         hypeerweb.addNode(node0);
         hypeerweb.addNode(node1);
         hypeerweb.addNode(node2);
 
         node0.addNeighbor(neighbor1);
         node0.addNeighbor(neighbor2);
         node0.setFold(Node.NULL_NODE);
         node0.setSurrogateFold(node1);
         node1.addNeighbor(neighbor0);
         node1.addUpPointer(upPointer2);
         node1.setFold(node2);
         node1.setSurrogateFold(node0);
         node2.addNeighbor(neighbor0);
         node2.addDownPointer(downPointer1);
         node2.setFold(node1);
         hypeerweb.saveToDatabase();
 
         hypeerweb.reload("temp.db");
         hypeerweb.clear();
         hypeerweb.reload("temp2.db");
         hypeerweb.reload("temp.db");
         if (hypeerweb.size() != 0) {
             System.err.println("When opening an empty database, 'temp.db', the database was not empty.");
             error = true;
             errorCount++;
         }
         Node node = hypeerweb.getNode(0);
        if (node != Node.NULL_NODE) {
             System.err.println("When opening an empty database, 'temp.db', it should be empty");
             error = true;
             errorCount++;
         }
         hypeerweb.clear();
 
         hypeerweb.reload();
         if (hypeerweb.size() != 3) {
             System.err.println("After reloading the original database, the HyPeerWeb should have 3 nodes in it but it has "    + hypeerweb.size() + ".");
             error = true;
             errorCount++;
         }
 
         node = hypeerweb.getNode(0);
         if (node == null) {
             System.err.println("After reloading the original database, node 0 was not found." + hypeerweb.size() + ".");
             error = true;
             errorCount++;
         } else {
             SimplifiedNodeDomain simpleDomain = node.constructSimplifiedNodeDomain();
             if (simpleDomain.getNeighbors().size() != 2) {
                 System.err.println("After reloading the original database, there should be only 2 neighbors of node 0 but there are "    + simpleDomain.getNeighbors().size() + ".");
                 error = true;
                 errorCount++;
             }
             if (!simpleDomain.getNeighbors().contains(1)) {
                 System.err.println("After reloading the original database, node 0 does not have node 1 as a neighbor.");
                 error = true;
                 errorCount++;
             }
             if (!simpleDomain.getNeighbors().contains(2)) {
                 System.err.println("After reloading the original database, node 0 does not have node 2 as a neighbor.");
                 error = true;
                 errorCount++;
             }
             if (simpleDomain.getUpPointers().size() != 0) {
                 System.err.println("After reloading the original database, node 0 should have no UpPointers but has " + simpleDomain.getUpPointers().size() + ".");
                 error = true;
                 errorCount++;
             }
             if (simpleDomain.getDownPointers().size() != 0) {
                 System.err.println("After reloading the original database, node 0 should have no DownPointers but has "    + simpleDomain.getDownPointers().size() + ".");
                 error = true;
                 errorCount++;
             }
             if (simpleDomain.getFold() != -1) {
                 int fold = simpleDomain.getFold();
                 System.err.println("After reloading the original database, node 0 should have a null fold but, node " + Integer.toString(fold)    + " was found instead.");
                 error = true;
                 errorCount++;
             }
             if (simpleDomain.getSurrogateFold() != 1) {
                 int surrogateFold = simpleDomain.getSurrogateFold();
                 System.err.println("After reloading the original database, node 0 should have node1 as a surroguteFold but, " + 
                                    (surrogateFold == -1 ? " the surrogateFold is null."    : (" found node " + Integer.toString(surrogateFold) + ".")));
                 error = true;
                 errorCount++;
             }
         }
 
         node = hypeerweb.getNode(1);
         if (node == null) {
             System.err.println("After reloading the original database, node 1 was not found." + hypeerweb.size() + ".");
             error = true;
             errorCount++;
         } else {
             SimplifiedNodeDomain simpleDomain = node.constructSimplifiedNodeDomain();
             if (simpleDomain.getNeighbors().size() != 1) {
                 System.err.println("After reloading the original database, there should be only 1 neighbor of node 1 but there are "    + simpleDomain.getNeighbors().size() + ".");
                 error = true;
                 errorCount++;
             }
             if (!simpleDomain.getNeighbors().contains(0)) {
                 System.err.println("After reloading the original database, node 1 does not have node 0 as a neighbor.");
                 error = true;
                 errorCount++;
             }
             if (simpleDomain.getUpPointers().size() != 1) {
                 System.err.println("After reloading the original database, node 1 should have 1 UpPointer but has "    + simpleDomain.getUpPointers().size() + ".");
                 error = true;
                 errorCount++;
             }
             if (!simpleDomain.getUpPointers().contains(2)) {
                 System.err.println("After reloading the original database, node 1 does not have node 2 as an UpPointer.");
                 error = true;
                 errorCount++;
             }
             if (simpleDomain.getDownPointers().size() != 0) {
                 System.err.println("After reloading the original database, node 1 should have no DownPointers but has " + simpleDomain.getDownPointers().size() + ".");
                 error = true;
                 errorCount++;
             }
             if (simpleDomain.getFold() != 2) {
                 int fold = simpleDomain.getFold();
                 System.err.println("After reloading the original database, node 1 should have node 2 as a fold but "
                                  + (fold == -1 ? " the fold is null." : (" found node " + Integer.toString(fold) + ".")));
                 error = true;
                 errorCount++;
             }
             if (simpleDomain.getSurrogateFold() != 0) {
                 int surrogateFold = simpleDomain.getFold();
                 System.err.println("After reloading the original database, node 1 should have node 0 as a surrogateFold but, "
                                  + (surrogateFold == -1 ? " the surrogateFold is null."    : (" found node " + Integer.toString(surrogateFold) + ".")));
                 error = true;
                 errorCount++;
             }
         }
 
         node = hypeerweb.getNode(2);
         if (node == null) {
             System.err.println("After reloading the original database, node 2 was not found." + hypeerweb.size() + ".");
             error = true;
             errorCount++;
         } else {
             SimplifiedNodeDomain simpleDomain = node.constructSimplifiedNodeDomain();
             if (simpleDomain.getNeighbors().size() != 1) {
                 System.err.println("After reloading the original database, there should be only 1 neighbor of node 2 but there are "    + simpleDomain.getNeighbors().size() + ".");
                 error = true;
                 errorCount++;
             }
             if (!simpleDomain.getNeighbors().contains(0)) {
                 System.err.println("After reloading the original database, node 2 does not have node 0 as a neighbor.");
                 error = true;
                 errorCount++;
             }
             if (simpleDomain.getUpPointers().size() != 0) {
                 System.err.println("After reloading the original database, node 2 should have no UpPointers but has " + simpleDomain.getUpPointers().size() + ".");
                 error = true;
                 errorCount++;
             }
             if (simpleDomain.getDownPointers().size() != 1) {
                 System.err.println("After reloading the original database, node 2 should have 1 DownPointer but has " + simpleDomain.getDownPointers().size() + ".");
                 error = true;
                 errorCount++;
             }
             if (!simpleDomain.getDownPointers().contains(1)) {
                 System.err.println("After reloading the original database, node 2 does not have node 1 as a DownPointer.");
                 error = true;
                 errorCount++;
             }
             if (simpleDomain.getFold() != 1) {
                 int fold = simpleDomain.getFold();
                 System.err.println("After reloading the original database, node 2 should have node 1 as a fold but "
                                  + (fold == -1 ? " the fold is null." : (" found node "    + Integer.toString(fold) + ".")));
                 error = true;
                 errorCount++;
             }
             if (simpleDomain.getSurrogateFold() != -1) {
                 int surrogateFold = simpleDomain.getFold();
                 System.err.println("After reloading the original database, node 2 should have node 1 as a surrogateFold but, "
                                 + (surrogateFold == -1 ? " the surrogateFold is null." : (" found node " + Integer.toString(surrogateFold) + ".")));
                 error = true;
                 errorCount++;
             }
         }
 
         hypeerweb.clear();
 
         hypeerweb.reload("temp.db");
         hypeerweb.clear();
         if (hypeerweb.size() != 0) {
             System.err.println("When opening a database and then clearing it, 'temp.db', the database should be empty.");
             error = true;
             errorCount++;
         }
 
         if (!error) {
             System.out.println("    No errors found while testing the creation of a HyPeerWeb from an existing database.");
         }
     }
     
     static private void testPhase2() {
         System.out.println("\nTesting Phase 2");
         hypeerweb.clear();
         errorCount = 0;
         
         boolean insertionError = false;
         System.out.print("    ");
         
         for (int size = 1; size <= HYPEERWEB_SIZE; size++) {
             if(VERBOSE) {
                 System.out.println("Testing Node Insertion on HyPeerWeb of size " + size + "/" + HYPEERWEB_SIZE);
             } else {
                 System.out.print(size + " ");
             }
             
             hypeerweb.clear();
             Node node0 = new Node(0);
             hypeerweb.addToHyPeerWeb(node0, null);
             Node firstNode = hypeerweb.getNode(0);
             SimplifiedNodeDomain simplifiedNodeDomain = firstNode.constructSimplifiedNodeDomain();
             ExpectedResult expectedResult = new ExpectedResult(1, 0);
 
             if (!simplifiedNodeDomain.equals(expectedResult)) {
                 insertionError = true;
                 printErrorMessage(size, null, simplifiedNodeDomain, expectedResult);
             }
             
             for (int startNodeId = 0; startNodeId < size - 1; startNodeId++) {
                 createHyPeerWebWith(size - 1);
                 Node node = new Node(0);
                 Node startNode = hypeerweb.getNode(startNodeId);
                 hypeerweb.addToHyPeerWeb(node, startNode);
                 
                 for (int i = 0; i < size; i++) {
                 	
                     Node nodei = hypeerweb.getNode(i);
                     simplifiedNodeDomain = nodei.constructSimplifiedNodeDomain();
                     expectedResult = new ExpectedResult(size, i);
 
                     if (!simplifiedNodeDomain.equals(expectedResult)) {
                         insertionError = true;
                         printErrorMessage(size, startNode, simplifiedNodeDomain, expectedResult);
                     }
                 }
             }
         }
         if(!VERBOSE){
             System.out.println();
         }
         
         if (insertionError) {
             double decimalPercent = ((double)PHASE_2_TEST_COUNT - (double) errorCount) / (double) PHASE_2_TEST_COUNT;
             int integerPercent = (int) (decimalPercent * 100.0d);
             System.out.println("    Insertion Error: Phase 2 Score = " + integerPercent + "%");
         } else {
             System.out.println("    No Insertion Errors: Phase 2 Score = 100%");
         }
     }
     
     private static void printErrorMessage(int size, Node startNode, SimplifiedNodeDomain simplifiedNodeDomain, ExpectedResult expectedResult) {
         errorCount++;
 
         System.out.println("-------------------------------------------------------------");
         System.out.println("INSERTION ERROR when HyPeerWebSize = " + size + ", Start Location: " + startNode);
         System.out.println("Node Information:");
         System.out.println(simplifiedNodeDomain);
         System.out.println();
         System.out.println("Expected Information");
         System.out.println(expectedResult);
     }
 
     static private void createHyPeerWebWith(int numberOfNodes) {
         hypeerweb.clear();
         Node node0 = new Node(0);
         hypeerweb.addToHyPeerWeb(node0, null);
 
         for (int i = 1; i < numberOfNodes; i++) {
             Node node = new Node(0);
             node0.addToHyPeerWeb(node);
         }
     }
     
     private static final int HYPEERWEB_SIZE = 32;
     private static final boolean VERBOSE = true;
 }
