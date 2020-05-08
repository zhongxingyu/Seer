 package de.unisiegen.tpml.core.expressions;
 
 import de.unisiegen.tpml.core.prettyprinter.PrettyStringBuilder;
 import de.unisiegen.tpml.core.prettyprinter.PrettyStringBuilderFactory;
 
 /**
  * Instances of this class represent exceptions in the expression hierarchy.
  *
  * @author Benedikt Meurer
  * @version $Rev$
  * 
  * @see de.unisiegen.tpml.core.expressions.Expression
  * @see de.unisiegen.tpml.core.expressions.Expression#isException()
  */
 public final class Exn extends Expression {
   //
   // Constants
   //
   
   /**
    * The <b>(DIVIDE-BY-ZERO)</b> exception.
    */
   public static final Exn DIVIDE_BY_ZERO = new Exn("divide_by_zero");
   
   /**
    * The <b>(EMPTY-LIST)</b> exception.
    */
   public static final Exn EMPTY_LIST = new Exn("empty_list");
   
   
   
   //
   // Attributes
   //
 
   /**
    * The name of the exception.
    * 
    * @see #toString()
    */
   private String name;
   
   
   
   //
   // Constructor (private)
   //
   
   /**
    * Allocates a new <code>Exn</code> instance with the specified <code>name</code>.
    * 
    * @param name the name of the exception.
    * 
    * @throws NullPointerException if <code>name</code> is <code>null</code>.
    * 
    * @see #DIVIDE_BY_ZERO
    */
   private Exn(String name) {
     if (name == null) {
       throw new NullPointerException("name is null");
     }
     this.name = name;
   }
   
   
   
   //
   // Attributes
   //
   
   /**
    * Returns the name of the exception.
    * 
    * @return the name of the exception.
    */
   public String getName() {
     return this.name;
   }
   
   
   
   //
   // Primitives
   //
   
   /**
    * {@inheritDoc}
    *
    * Substitution below exceptions is not possible, so for the <code>Exn</code> class, this method
    * always returns a reference to the exception itself.
    * 
    * @see de.unisiegen.tpml.core.expressions.Expression#substitute(java.lang.String, de.unisiegen.tpml.core.expressions.Expression)
    */
   @Override
   public Expression substitute(String id, Expression e) {
     return this;
   }
   
   /**
    * {@inheritDoc}
    *
    * @see de.unisiegen.tpml.core.expressions.Expression#isException()
    */
   @Override
   public boolean isException() {
     return true;
   }
 
   
   
   //
   // Pretty printing
   //
   
   /**
    * {@inheritDoc}
    *
    * @see de.unisiegen.tpml.core.expressions.Expression#toPrettyStringBuilder(de.unisiegen.tpml.core.prettyprinter.PrettyStringBuilderFactory)
    */
   @Override 
   public PrettyStringBuilder toPrettyStringBuilder(PrettyStringBuilderFactory factory) {
     PrettyStringBuilder builder = factory.newBuilder(this, PRIO_EXN);
    builder.addText(this.name);
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
     if (obj instanceof Exn) {
       Exn other = (Exn)obj;
       return (this.name.equals(other.name));
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
     return this.name.hashCode();
   }
 }
