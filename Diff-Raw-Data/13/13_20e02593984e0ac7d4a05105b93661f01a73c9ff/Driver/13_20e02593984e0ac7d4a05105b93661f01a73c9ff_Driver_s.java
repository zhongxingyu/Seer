 /**
  * User: Gifflen
  * Chapter 9 Problem 2
  *  Problem: This program asks you to implement a complete binary tree using an array (or ArrayList).
  *  Write the program so that I can enter an index and it will print out the given left and right child for that node.
  */
 public class Driver {
     public  static void main(String[] args){
         BinaryTree<Integer> btint = new BinaryTree<Integer>();
         btint.addNode(1);
         btint.addNode(2);
         btint.addNode(3);
         btint.addNode(4);
         btint.addNode(5);
         btint.addNode(6);
         btint.addNode(7);
         //btint.addNode(8);
         //btint.addNode(9);
         //btint.addNode(10);
         //btint.addNode(11);
 
         System.out.println("Root Data:"+btint.getData(0));
         System.out.println("Depth :"+btint.getDepth());
         System.out.println(btint.getLeft(0));
         System.out.println(btint.getRight(1));

         System.out.println("Branch Left of root: "+btint.getLeftData(0));
         System.out.println("Branch Right of root: "+ btint.getRightData(0));
 
     }
 }
