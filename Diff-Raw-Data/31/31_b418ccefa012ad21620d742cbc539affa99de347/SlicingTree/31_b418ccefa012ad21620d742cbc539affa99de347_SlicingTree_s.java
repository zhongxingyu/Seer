 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package octa;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 
 /**
  *
  * @author abuzaher
  */
 public class SlicingTree {
 
     private ArrayList<SlicingTreeNode> nodeList;
     private int numInternalNodes;
     private int numExternalNodes;
     private boolean updated;
     private int minFaceArea;
     SlicingGraph graph;
 
     public int getExternalNodeStartIdx() {
         return this.numInternalNodes;
     }
 
     public SlicingTree(SlicingGraph graph) {
         this.nodeList = new ArrayList<SlicingTreeNode>();
         this.updated = false;
         // intialize minFaceArea to a big value
         this.minFaceArea = Integer.MAX_VALUE;
         // the reference for the slicing graph, it will be used later for updating each faces
         // vertex list
         this.graph = graph;
     }
 
     public void setNumInternalNodes(int numInternalNodes) {
         this.numInternalNodes = numInternalNodes;
     }
 
     public void setNumExternalNodes(int numExternalNodes) {
         this.numExternalNodes = numExternalNodes;
     }
 
     public void appendNode(SlicingTreeNode node) {
         this.nodeList.add(node);
     }
 
     public ArrayList<SlicingTreeNode> getNodeList() {
         return nodeList;
     }
 
     public void printInfo(ArrayList<SlicingTreeNode> traverserdList) {
         System.out.println("RESULT of preorder traversal");
         for (Iterator<SlicingTreeNode> it = traverserdList.iterator(); it.hasNext();) {
             SlicingTreeNode node = it.next();
             if (node instanceof SlicingTreeInternalNode) {
 
                 System.out.println("Node " + node.getId() + " is an internal node.");
 
                 SlicingTreeNode leftChild = ((SlicingTreeInternalNode) node).getLeftChild();
 
                 if (leftChild instanceof SlicingTreeInternalNode) {
                     System.out.println("Left child of node " + node.getId() + ": internal node: " + leftChild.getId());
                 } else {
                     System.out.println("Left child of node " + node.getId() + ": external node: " + leftChild.getId());
                 }
 
                 SlicingTreeNode rightChild = ((SlicingTreeInternalNode) node).getRightChild();
                 if (rightChild instanceof SlicingTreeInternalNode) {
                     System.out.println("Right child of node " + node.getId() + ": internal node: " + rightChild.getId());
                 } else {
                     System.out.println("Left child of node " + node.getId() + ": external node: " + rightChild.getId());
                 }
 
             } else {
                 System.out.println("Node " + node.getId() + " is an external node.");
 
             }
         }
     }
 
     /*
      * print tree info
      */
     public void printInfo() {
         System.out.println("Number of internal nodes (cuts): " + this.numInternalNodes);
         System.out.println("Number of external nodes (faces): " + this.numExternalNodes);
 
         if (!updated) {
             for (int i = 0; i < this.numInternalNodes; i++) {
                 SlicingTreeInternalNode node = (SlicingTreeInternalNode) this.nodeList.get(i);
                 System.out.println("Parent of inernal node (cut)" + node.getId() + ": " + node.getParentId());
                 System.out.println("Node Type: " + node.getCutType());
                 System.out.println("Slicing path for this node: " + node.getSlicingPath());
             }
             for (int i = this.numInternalNodes; i < nodeList.size(); i++) {
                 SlicingTreeExternalNode node = (SlicingTreeExternalNode) this.nodeList.get(i);
                 System.out.println("Parent of leaf node (face) " + node.getId() + ": " + node.getParentId());
                 System.out.println("Area under face: " + node.getId() + ": " + node.getFaceArea());
             }
         } else {
             for (Iterator<SlicingTreeNode> it = nodeList.iterator(); it.hasNext();) {
                 SlicingTreeNode node = it.next();
                 if (node instanceof SlicingTreeInternalNode) {
 
                     System.out.println("Node " + node.getId() + " is an internal node with area: " + node.getFaceArea());
                     System.out.println("Node " + node.getId() + " is cut by path: " + ((SlicingTreeInternalNode) node).getSlicingPath());
 
                     SlicingTreeNode leftChild = ((SlicingTreeInternalNode) node).getLeftChild();
 
                     if (leftChild instanceof SlicingTreeInternalNode) {
                         System.out.println("Left child of node " + node.getId() + ": internal node: " + leftChild.getId() + " with values " + leftChild.getClockwiseMemberVertices());
                     } else {
                         System.out.println("Left child of node " + node.getId() + ": external node: " + leftChild.getId() + " with values " + leftChild.getClockwiseMemberVertices());
                     }
 
                     SlicingTreeNode rightChild = ((SlicingTreeInternalNode) node).getRightChild();
                     if (rightChild instanceof SlicingTreeInternalNode) {
                         System.out.println("Right child of node " + node.getId() + ": internal node: " + rightChild.getId() + " with values " + rightChild.getClockwiseMemberVertices());
                     } else {
                         System.out.println("Right child of node " + node.getId() + ": external node: " + rightChild.getId() + " with values " + rightChild.getClockwiseMemberVertices());
                     }
 
                 } else {
                     System.out.println("Node " + node.getId() + " is an external node with face area of: " + node.getFaceArea());
                 }
                 System.out.println("Node " + node.getId() + " has member vertices: " + node.getClockwiseMemberVertices());
             }
             System.out.println("Min Face Area: " + this.minFaceArea);
         }
 
     }
 
     /*
      * 1. updates the child parent references in the the tree
      * 2. updates the minFaceArea
      * 3. update collective area of each internal node
      * 4. update the list of member vertices in each node in clockwise order
      */
     public void updateTree() {
         for (Iterator<SlicingTreeNode> it = nodeList.iterator(); it.hasNext();) {
             SlicingTreeNode treeNode = it.next();
             int parentId = treeNode.getParentId();
             int parentIndex = parentId - 1;
             // update child parent relationship
             if (parentIndex > -1) {
                 SlicingTreeInternalNode parent = (SlicingTreeInternalNode) this.nodeList.get(parentIndex);
                 if (parent.getRightChild() == null) {
                     parent.setRightChild(treeNode);
                 } else {
                     parent.setLeftChild(treeNode);
                 }
                 // if you see that both the childs ref has been updated
                if (parent.getLeftChild() != null && parent.getRightChild() != null){
                     // right child is internal node and left child is external node
                     // this is invalid, swap thow two
                     // the invalididy is due to the way we take the input from the user, we take internal nodes first, and external nodes later
                     // but if a node has both type of child, the external node must alwasys the right child in this case
                    if (parent.getRightChild() instanceof SlicingTreeInternalNode && parent.getLeftChild() instanceof SlicingTreeExternalNode){
                         SlicingTreeNode temp = parent.getLeftChild();
                         parent.setLeftChild(parent.getRightChild());
                         parent.setRightChild(temp);
                     }
                 }
             }
             // update minFaceArea
             if (treeNode instanceof SlicingTreeExternalNode) {
                 int currentNodeFaceArea = ((SlicingTreeExternalNode) treeNode).getFaceArea();
                 if (this.minFaceArea > currentNodeFaceArea) {
                     this.minFaceArea = currentNodeFaceArea;
                 }
             }
         }
         // update the collective area of each internal node
         // NOTE: the iteration must start form extenal nodes
        for (int i = nodeList.size() - 1; i > 0; i--) {
             SlicingTreeNode node = nodeList.get(i);
            SlicingTreeNode parent = nodeList.get(node.getParentId() - 1);
            parent.setFaceArea(parent.getFaceArea() + node.getFaceArea());
 
             // code to update the list of vertex in each node
             if (node instanceof SlicingTreeExternalNode) {
                 // if external node then just copy from the SlicingGraph
                 SlicingTreeExternalNode leaf = (SlicingTreeExternalNode) node;
                 int graphEquivFaceIdx = i - this.numInternalNodes;
                 ArrayList<Integer> vertices = this.graph.getFaces().get(graphEquivFaceIdx).getClockwiseMemberVertices();
                 leaf.setClockwiseMemberVertices(vertices);
             } else {
                 // internal nodes need to merge the two child nodes list and it MUST
                 // preserve the clockwise node order
                 SlicingTreeInternalNode inode = (SlicingTreeInternalNode) node;
 
                 ArrayList<Integer> rightList = inode.getRightChild().getClockwiseMemberVertices();
                 ArrayList<Integer> leftList = inode.getLeftChild().getClockwiseMemberVertices();
                 ArrayList<Integer> mergedList = clockWiseMerge(rightList, leftList, inode.getSlicingPath());
                 inode.setClockwiseMemberVertices(mergedList);
                 // need to prune inner vertices too
 //                inode.pruneInteriorVertices();
             }
         }
         this.updated = true;
     }
 
     /*
      * Traverse the tree in root, right, left manner
      */
     public ArrayList<SlicingTreeNode> reversePreorderTraverse(int startIdx) {
         ArrayList<SlicingTreeNode> traveresedList = new ArrayList<SlicingTreeNode>();
         SlicingTreeNode rootNode = this.getNodeList().get(startIdx);
 
         // first add the rootnode in the traversed list
         traveresedList.add(rootNode);
 //        System.out.println(rootNode.getId());
         if (rootNode instanceof SlicingTreeInternalNode) {
             // get right traversed list recursively
             ArrayList<SlicingTreeNode> rightTrversedList = this.reversePreorderTraverse(this.getNodeList().indexOf(((SlicingTreeInternalNode) rootNode).getRightChild()));
             // get left traversed list recursively
             ArrayList<SlicingTreeNode> leftTraversedList = this.reversePreorderTraverse(this.getNodeList().indexOf(((SlicingTreeInternalNode) rootNode).getLeftChild()));
 
             // now that we have all the traversed list, let's add them into our main list
             traveresedList.addAll(rightTrversedList);
             traveresedList.addAll(leftTraversedList);
         }
         return traveresedList;
     }
 
     private ArrayList<Integer> clockWiseMerge(ArrayList<Integer> rightList, ArrayList<Integer> leftList, ArrayList<Integer> slicingPath) {
         // FIXME: wee need deepcopy, cloning does a shallow copy
         //        ArrayList<Integer> mergedList = (ArrayList<Integer>) rightList.clone();
         // this is the deep copy function being used
         //        ArrayList<Integer> mergedList = (ArrayList<Integer>) DeepCopier.deepCopy(rightList);
         ////        CircularList<Integer> targetList = new CircularList<Integer>((ArrayList<Integer>) DeepCopier.deepCopy(rightList));
         //
         //        int idx = 0;
         //        int insertIdx = 1;
         //        System.out.println("Trying to mege: " + rightList  + " and " + leftList);
         //        for (int i = 0; i < leftList.size(); i++) {
         //            System.out.println(mergedList);
         //            Integer searchVertex = leftList.get(i);
         //            idx = mergedList.indexOf(searchVertex);
         //            if (idx != -1) {
         //                insertIdx = idx + 1;
         //            } else {
         //                mergedList.add(insertIdx, searchVertex);
         //
         //                insertIdx += 1;
         //            }
         //            System.out.println(mergedList);
         //            System.out.println("");
         //        }
         //        return mergedList;
 
         ArrayList<Integer> mergedList = new ArrayList<Integer>();
 
         Integer topElement = slicingPath.get(0);
         Integer bottomElement = slicingPath.get(slicingPath.size() - 1);
 
         try {
 
             CircularList<Integer> rightCircularList = new CircularList<Integer>(rightList, topElement);
             CircularList<Integer> leftCircularList = new CircularList<Integer>(leftList, bottomElement);
 
            System.out.println("Trying to merge: " + rightList + " and " + leftList + " with slicing path " + slicingPath);
 
             Integer element;
 
             mergedList.add(topElement);
 
             element = rightCircularList.getNext();
             while (element.intValue() != bottomElement.intValue()) {
                 mergedList.add(element);
                 element = rightCircularList.getNext();
             }
 
            System.out.println("After right merging: " + mergedList);
 
             mergedList.add(bottomElement);
             element = leftCircularList.getNext();
             while (element.intValue() != topElement.intValue()) {
                 mergedList.add(element);
                 element = leftCircularList.getNext();
             }
            System.out.println("Merger List: " + mergedList);
             return mergedList;
         } catch (Exception ex) {
 //            Logger.getLogger(SlicingTree.class.getName()).log(Level.SEVERE, null, ex);
         }
         return mergedList;
     }
 }
