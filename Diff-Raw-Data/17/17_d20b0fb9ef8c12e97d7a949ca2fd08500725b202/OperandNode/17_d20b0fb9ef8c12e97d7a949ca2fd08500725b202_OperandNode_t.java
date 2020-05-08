 package data;
 
 import java.util.Random;
 
 import utilities.Settings;
 
 public class OperandNode extends Node {
     
    private final static String OPERAND_X = "x";
     private String operand;
    private final int MAX = 15;
 
     public OperandNode() {
         Random rand = new Random();
         int randInt = rand.nextInt(MAX);
         
         if (randInt >= 10) {
            operand = OPERAND_X;
         } else {
             operand = Integer.toString(randInt);
         }
         
         if (Settings.debug()) {
             System.out.println(operand);
         }
     }
 
     public void setOperand(String o) {
         operand = o;
     }
     
     public String getOperand() {
         return operand;
     }
     
     public String getDataItem() {
         return operand;
     }
     
     public String toString() {
         return operand;
     }
     
     public double evaluate(double xval) {
        if (operand.equals(OPERAND_X)) {
             return xval;
         } else {
             return Double.parseDouble(operand);
         }
     }
 }
