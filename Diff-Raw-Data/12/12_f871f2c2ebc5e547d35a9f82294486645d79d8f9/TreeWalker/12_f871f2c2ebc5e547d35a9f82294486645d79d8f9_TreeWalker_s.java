 package ch04.ex04_05;
 
abstract class TreeWalker {
   Strategy strategy;
  TreeNode root;
   
  abstract boolean search(Object target);
 }
