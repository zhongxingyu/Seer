 package de.unisiegen.tpml.core.expressions;
 
 
 /**
  * Instances of this class represent arithmetic operators in the expression hierarchy.
  *
  * @author Benedikt Meurer
  * @version $Rev$
  * 
  * @see de.unisiegen.tpml.core.expressions.BinaryOperator
  * @see de.unisiegen.tpml.core.expressions.IntegerConstant
  */
 public final class ArithmeticOperator extends BinaryOperator {
   //
   // Class methods
   //
   
   /**
    * Returns the arithmetic plus operator.
    * 
    * @return a new instance of the plus operator.
    */
   public static final ArithmeticOperator newPlus() {
     return new ArithmeticOperator("+", PRIO_PLUS);
   }
   
   /**
    * Returns the arithmetic minus operator.
    * 
    * @return a new instance of the minus operator.
    */
   public static final ArithmeticOperator newMinus() {
     return new ArithmeticOperator("-", PRIO_MINUS);
   }
   
   /**
    * Returns the arithmetic multiplication operator.
    * 
    * @return a new instance of the mult operator.
    */
   public static final ArithmeticOperator newMult() {
     return new ArithmeticOperator("*", PRIO_MULT);
   }
   
   /**
    * Returns the arithmetic division operator.
    * 
    * @return a new instance of the div operator.
    */
   public static final ArithmeticOperator newDiv() {
     return new ArithmeticOperator("/", PRIO_DIV);
   }
   
   /**
    * Returns the arithmetic modulo operator.
    * 
    * @return a new instance of the mod operator.
    */
   public static final ArithmeticOperator newMod() {
     return new ArithmeticOperator("mod", PRIO_MOD);
   }
 
   
   
   //
   // Constructor (private)
   //
   
   /**
    * Allocates a new <code>ArithmeticOperator</code> with the given <code>text</code> and
    * <code>prettyPriority</code> (for pretty printing in infix operation).
    * 
    * @param text the string representation of the operator.
    * @param prettyPriority the pretty print priority for infix pretty printing.
    * 
    * @throws NullPointerException if <code>text</code> is <code>null</code>.
    * 
    * @see BinaryOperator#BinaryOperator(String, int)
    */
   private ArithmeticOperator(String text, int prettyPriority) {
     super(text, prettyPriority);
   }
   
   
   
   //
   // Primitives
   //
   
   /**
    * {@inheritDoc}
    *
    * @see de.unisiegen.tpml.core.expressions.BinaryOperator#applyTo(de.unisiegen.tpml.core.expressions.Expression, de.unisiegen.tpml.core.expressions.Expression)
    */
   @Override
   public Expression applyTo(Expression e1, Expression e2) throws BinaryOperatorException {
     if (e1 == null) {
       throw new NullPointerException("e1 is null");
     }
     if (e2 == null) {
       throw new NullPointerException("e2 is null");
     }
     try {
       // determine the integer values of the operands
       int n1 = ((IntegerConstant)e1).intValue();
       int n2 = ((IntegerConstant)e2).intValue();
       
       // try to perform the application
      if (getPrettyPriority() == PRIO_PLUS) {
         return new IntegerConstant(n1 + n2);
       }
      else if (getPrettyPriority() == PRIO_MINUS) {
         return new IntegerConstant(n1 - n2);
       }
      else if (getPrettyPriority() == PRIO_MULT) {
         return new IntegerConstant(n1 * n2);
       }
      else if (getPrettyPriority() == PRIO_DIV) {
         return new IntegerConstant(n1 / n2);
       }
      else if (getPrettyPriority() == PRIO_MOD) {
         return new IntegerConstant(n1 % n2);
       }
       else {
         // programming error
         throw new IllegalStateException("inconsistent arithmetic operator class");
       }
     }
     catch (ClassCastException e) {
       // one of the Expression to IntegerConstant casts failed
       throw new BinaryOperatorException(this, e1, e2);
     }
     catch (ArithmeticException e) {
       // internal Java operators throw this on divide-by-zero
       return Exn.DIVIDE_BY_ZERO;
     }
   }
   
   /**
    * {@inheritDoc}
    *
    * @see de.unisiegen.tpml.core.expressions.Expression#clone()
    */
   @Override
   public ArithmeticOperator clone() {
     return new ArithmeticOperator(getText(), getPrettyPriority());
   }
 }
