 package main;
 
 import nodes.AddNode;
 import nodes.ConstantNode;
 import nodes.OperationNode;
 
 /**
  *
  * @author Johanna
  */
 public class Main {
 
     public static void main(String[] args) {
         OperationNode root = new AddNode();
        root.insertNode(new ConstantNode(3d));
         
         System.out.println(root.evaluate());
     }
 }
