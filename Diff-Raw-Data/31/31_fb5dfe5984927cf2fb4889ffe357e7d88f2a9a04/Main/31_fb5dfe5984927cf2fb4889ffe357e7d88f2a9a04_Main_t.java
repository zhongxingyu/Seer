 package main;
 
 import nodes.AddNode;
 import nodes.ConstantNode;
 import nodes.OperationNode;
import nodes.SubstractNode;
 
 /**
  *
  * @author Johanna
  */
 public class Main {
 
     public static void main(String[] args) {
         OperationNode root = new AddNode();
        root.insertNode(new ConstantNode(2));
        root.insertNode(new ConstantNode(3));
         
         System.out.println(root.evaluate());
     }
 }
