 package nodes;
 
 /**
  *
  * @author Johanna 
  */
 public class ConstantNode extends Node{
     
     private double value;
     
     public ConstantNode(double value){
         this.value=value;
     }

     @Override
     public double evaluate() {
         return value;
     }
 
     @Override
     public Node getChild(int child) {
         return null;
     }
 
 }
