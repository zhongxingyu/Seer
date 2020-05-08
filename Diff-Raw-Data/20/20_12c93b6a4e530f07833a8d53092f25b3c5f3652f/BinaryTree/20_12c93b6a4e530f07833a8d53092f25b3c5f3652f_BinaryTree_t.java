 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package caesar.ast;
 
 /**
  *
  * @author hohy
  */
 public class BinaryTree extends ExpressionTree {
 
     Operator operator;
     ExpressionTree leftOperand;
     ExpressionTree rightOperand;
 
     public BinaryTree(Operator operator, ExpressionTree leftOperand, ExpressionTree rightOperand) {
         this.operator = operator;
         this.leftOperand = leftOperand;
         this.rightOperand = rightOperand;
     }
     
     @Override
     public void accept(TreeVisitor visitor) {
         visitor.visit(this);
     }
 
     public ExpressionTree getLeftOperand() {
         return leftOperand;
     }
 
     public void setLeftOperand(ExpressionTree leftOperand) {
         this.leftOperand = leftOperand;
     }
 
     public Operator getOperator() {
         return operator;
     }
 
     public void setOperator(Operator operator) {
         this.operator = operator;
     }
 
     public ExpressionTree getRightOperand() {
         return rightOperand;
     }
 
     public void setRightOperand(ExpressionTree rightOperand) {
         this.rightOperand = rightOperand;
     }
 
     @Override
     public String getType() {
         if (operator == Operator.DIVIDE) {
             return "Real";
         }
         String lo = leftOperand.getType();
         String ro = rightOperand.getType();
 //        if(lo == Type.ARRAY) lo = Type.INTEGER;
 //        if(ro == Type.ARRAY) ro = Type.INTEGER;
 
        // pokud nevim typ nektereho operandu, tak nemuzu urcovat typ vysledku...
        if(lo == null || ro == null) return null;
        
        if ((lo != null) && (ro != null) && (lo.equals("Integer") && ro.equals("Integer"))) {
             return "Integer";
         }

        return null;
     }
 }
