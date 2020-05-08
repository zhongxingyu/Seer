 package jp.ac.osaka_u.ist.sel.metricstool.main.data.target.unresolved;
 
 
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.OPERATOR;
 
 
 /**
  * 񍀉Zi[邽߂̃NX
  * 
  * @author y-higo
  * 
  */
 public class UnresolvedBinominalOperation implements UnresolvedTypeInfo{
 
     /**
      * ȂRXgN^
      */
     public UnresolvedBinominalOperation() {
     }
 
     /**
      * Zq2̃Iyh^ď
      * 
      * @param operator Zq
      * @param firstOperand ijIyh
      * @param secondOperand ijIyh
      */
     public UnresolvedBinominalOperation(final OPERATOR operator,
             final UnresolvedTypeInfo firstOperand, final UnresolvedTypeInfo secondOperand) {
 
         if ((null == operator) || (null == firstOperand) || (null == secondOperand)) {
             throw new NullPointerException();
         }
 
         this.operator = operator;
         this.firstOperand = firstOperand;
         this.secondOperand = secondOperand;
     }
     
     /**
      * ̃NX̌^Ԃ
      * 
      * @return ̃NX̌^
      */
     public String getTypeName() {
         return "UnresolvedBinominalOperation";
     }
 
     /**
      * Zq擾
      * 
      * @return Zq
      */
     public OPERATOR getOperator() {
         return this.operator;
     }
 
     /**
      * ijIyh擾
      * 
      * @return ijIyh
      */
     public UnresolvedTypeInfo getFirstOperand() {
         return this.firstOperand;
     }
 
     /**
      * ijIyh擾
      * 
      * @return ijIyh
      */
     public UnresolvedTypeInfo getSecondOperand() {
         return this.secondOperand;
     }
 
     /**
      * ZqZbg
      * 
      * @param operator Zq
      */
     public void setOperator(final OPERATOR operator) {
 
         if (null == operator) {
             throw new NullPointerException();
         }
 
         this.operator = operator;
     }
 
     /**
      * ijIyhZbg
      * 
      * @param firstOperand ijIyh
      */
     public void setFirstOperand(final UnresolvedTypeInfo firstOperand) {
 
        if (null == operator) {
             throw new NullPointerException();
         }
 
         this.firstOperand = firstOperand;
     }
 
     /**
      * ijIyhZbg
      * 
      * @param secondOperand ijIyh
      */
     public void setSecondOperand(final UnresolvedTypeInfo secondOperand) {
 
        if (null == operator) {
             throw new NullPointerException();
         }
 
         this.secondOperand = secondOperand;
     }
 
     private OPERATOR operator;
 
     private UnresolvedTypeInfo firstOperand;
 
     private UnresolvedTypeInfo secondOperand;
 }
