 package edu.msergey.jalg.exercises.ch5.ex86;
 
 import edu.msergey.jalg.exercises.ch5.ex59.SimpleBinaryTree;
 
 public class LeafCounter {
    public static int countLeaves(SimpleBinaryTree tree) {
         if (tree == null) return 0;
         if (tree.leftNode == null && tree.rightNode == null) return 1;
         return countLeaves(tree.leftNode) + countLeaves(tree.rightNode);
     }
 }
