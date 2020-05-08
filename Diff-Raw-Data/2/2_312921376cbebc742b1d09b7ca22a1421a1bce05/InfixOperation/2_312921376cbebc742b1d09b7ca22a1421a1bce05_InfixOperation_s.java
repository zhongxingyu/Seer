 package de.unisiegen.tpml.core.expressions;
 
 import de.unisiegen.tpml.core.prettyprinter.PrettyStringBuilder;
 import de.unisiegen.tpml.core.prettyprinter.PrettyStringBuilderFactory;
 import de.unisiegen.tpml.core.typechecker.TypeSubstitution;
 
 /**
  * Instances of this class are used to represent infix operations, which act as syntactic sugar
  * for applications.
  * 
  * The string representation of an infix operation is <code>e1 op e2</code> where <code>op</code>
  * is a binary operator.
  *
  * @author Benedikt Meurer
  * @version $Rev$
  *
  * @see de.unisiegen.tpml.core.expressions.Application
  * @see de.unisiegen.tpml.core.expressions.BinaryOperator
  * @see de.unisiegen.tpml.core.expressions.Expression
  */
 public final class InfixOperation extends Expression {
   //
   // Attributes
   //
   
   /**
    * The operator of the infix operation.
    * 
    * @see #getOp()
    */
   private BinaryOperator op;
   
   /**
    * The first operand.
    * 
    * @see #getE1()
    */
   private Expression e1;
   
   /**
    * The second operand.
    * 
    * @see #getE2()
    */
   private Expression e2;
   
   
   
   //
   // Constructor
   //
   
   /**
    * Allocates a new <code>InfixOperation</code> with the specified parameters.
    * 
    * @param op the binary operator.
    * @param e1 the first operand.
    * @param e2 the second operand.
    * 
    * @throws NullPointerException if <code>op</code>, <code>e1</code> or <code>e2</code> is <code>null</code>.
    */
   public InfixOperation(BinaryOperator op, Expression e1, Expression e2) {
     if (op == null) {
       throw new NullPointerException("op is null");
     }
     if (e1 == null) {
       throw new NullPointerException("e1 is null");
     }
     if (e2 == null) {
       throw new NullPointerException("e2 is null");
     }
     this.op = op;
     this.e1 = e1;
     this.e2 = e2;
   }
   
   
   
   //
   // Accessors
   //
   
   /**
    * Returns the binary operator that is applied to <code>e1</code> and <code>e2</code>.
    * 
    * @return the binary operator.
    * 
    * @see #getE1()
    * @see #getE2()
    */
   public BinaryOperator getOp() {
     return this.op;
   }
   
   /**
    * Returns the first operand.
    * 
    * @return the first operand.
    * 
    * @see #getOp()
    * @see #getE2()
    */
   public Expression getE1() {
     return this.e1;
   }
   
   /**
    * Returns the second operand.
    * 
    * @return the second operand.
    * 
    * @see #getOp()
    * @see #getE1()
    */
   public Expression getE2() {
     return this.e2;
   }
 
   
   
   //
   // Primitives
   //
   
   /**
    * {@inheritDoc}
    *
    * @see de.unisiegen.tpml.core.expressions.Expression#substitute(de.unisiegen.tpml.core.typechecker.TypeSubstitution)
    */
   @Override
   public InfixOperation substitute(TypeSubstitution substitution) {
     return new InfixOperation(this.op, this.e1.substitute(substitution), this.e2.substitute(substitution));
   }
   
   /**
    * {@inheritDoc}
    *
    * @see de.unisiegen.tpml.core.expressions.Expression#substitute(java.lang.String, de.unisiegen.tpml.core.expressions.Expression)
    */
   @Override
   public InfixOperation substitute(String id, Expression e) {
     return new InfixOperation(this.op, this.e1.substitute(id, e), this.e2.substitute(id, e));
   }
 
   
   
   //
   // Pretty printing
   //
   
   /**
    * {@inheritDoc}
    *
    * @see de.unisiegen.tpml.core.expressions.Expression#toPrettyStringBuilder(de.unisiegen.tpml.core.prettyprinter.PrettyStringBuilderFactory)
    */
   public @Override PrettyStringBuilder toPrettyStringBuilder(PrettyStringBuilderFactory factory) {
     PrettyStringBuilder builder = factory.newBuilder(this, this.op.getPrettyPriority());
     builder.addBuilder(this.e1.toPrettyStringBuilder(factory), this.op.getPrettyPriority());
     builder.addText(" " + this.op.toString() + " ");
    builder.addBuilder(this.e2.toPrettyStringBuilder(factory), this.op.getPrettyPriority());
     return builder;
   }
 
   
   
   //
   // Base methods
   //
   
   /**
    * {@inheritDoc}
    *
    * @see de.unisiegen.tpml.core.expressions.Expression#equals(java.lang.Object)
    */
   @Override
   public boolean equals(Object obj) {
     if (obj instanceof InfixOperation) {
       InfixOperation other = (InfixOperation)obj;
       return (this.op.equals(other.op) && this.e1.equals(other.e1) && this.e2.equals(other.e2));
     }
     return false;
   }
 
   /**
    * {@inheritDoc}
    *
    * @see de.unisiegen.tpml.core.expressions.Expression#hashCode()
    */
   @Override
   public int hashCode() {
     return this.op.hashCode() + this.e1.hashCode() + this.e2.hashCode();
   }
 }
