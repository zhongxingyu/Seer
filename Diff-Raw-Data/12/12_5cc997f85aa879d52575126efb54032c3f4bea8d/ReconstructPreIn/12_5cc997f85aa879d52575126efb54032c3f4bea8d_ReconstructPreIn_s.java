 package trees;
 
 import java.util.Collections;
 
 /**
  * Created with IntelliJ IDEA.
  * User: vvlad
  * Date: 7/6/13
  * Time: 8:55 PM
  * Reconstructs Binary Tree from preOrder and inOrder traversals
  */
 public class ReconstructPreIn {
     //int preIndex;
    int[] inOrderBackIndex;
    int[] preOrder;
    int[] inOrder;
 
     public BinaryTree reconstruct(int[] preOrder, int[] inOrder) {
         if (preOrder.length!=inOrder.length)
             return null;
         this.preOrder = preOrder;
        this.inOrder = inOrder;
         //preIndex = 0;
 
         int size = 0;
         for(int i=0;i<inOrder.length;i++)
             if (inOrder[i]>size)
                 size = inOrder[i];
 
         this.inOrderBackIndex = new int[size+1];
 
         for(int i=0;i<inOrder.length;i++)
            inOrderBackIndex[inOrder[i]] = i;
 
         return reconstruct(0, preOrder.length-1, 0, inOrder.length-1);
     }
 
     private BinaryTree reconstruct(int preStart, int preEnd, int inStart, int inEnd) {
         if (preStart>preEnd)
             return null;
 
         if (preStart==preEnd)
             return new BinaryTree(preOrder[preStart]);
 
 
         BinaryTree myNode = new BinaryTree(preOrder[preStart]);
 
         int vertexIndex = inOrderBackIndex[preOrder[preStart]];
         if (vertexIndex>inEnd || vertexIndex<inStart)
             vertexIndex=0;
 
 
         int leftOffset = vertexIndex - inStart;
 
         myNode.left = reconstruct(preStart+1, preStart+leftOffset, inStart, inStart+leftOffset-1);
         myNode.right = reconstruct(preStart+leftOffset+1, preEnd, inStart+leftOffset+1, inEnd+leftOffset);
 
         return myNode;
     }
 }
