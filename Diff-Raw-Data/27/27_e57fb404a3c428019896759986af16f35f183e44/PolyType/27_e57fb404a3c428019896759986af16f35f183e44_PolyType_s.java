 package de.unisiegen.tpml.core.types;
 
 import java.util.Iterator;
 import java.util.Set;
 import java.util.TreeSet;
 
 import de.unisiegen.tpml.core.prettyprinter.PrettyStringBuilder;
 import de.unisiegen.tpml.core.prettyprinter.PrettyStringBuilderFactory;
 import de.unisiegen.tpml.core.typechecker.TypeSubstitution;
 import de.unisiegen.tpml.core.typechecker.TypeUtilities;
 
 /**
  * Instances of this class represent polymorphic types, which are basicly monomorphic types with
  * a set of quantified type variables.
  *
  * @author Benedikt Meurer
  * @version $Rev$
  *
  * @see de.unisiegen.tpml.core.types.Type
  */
 public final class PolyType extends Type {
   //
   // Attributes
   //
   
   /**
    * The quantified type variables.
    * 
    * @see #getQuantifiedVariables()
    */
   private Set<TypeVariable> quantifiedVariables;
   
   /**
    * The monomorphic type.
    * 
    * @see #getTau()
    */
   private MonoType tau;
   
   
   
   //
   // Constructor
   //
   
   /**
    * Allocates a new <code>PolyType</code> with the given <code>quantifiedVariables</code> and
    * the monomorphic type <code>tau</code>.
    * 
    * @param quantifiedVariables the set of quantified type variables for <code>tau</code>.
    * @param tau the monomorphic type.
    * 
    * @throws NullPointerException if <code>quantifiedVariables</code> or <code>tau</code> is <code>null</code>.
    * @see MonoType
    */
   public PolyType(Set<TypeVariable> quantifiedVariables, MonoType tau) {
     if (quantifiedVariables == null) {
       throw new NullPointerException("quantifiedVariables is null");
     }
     if (tau == null) {
       throw new NullPointerException("tau is null");
     }
     this.quantifiedVariables = quantifiedVariables;
     this.tau = tau;
   }
   
   
   
   //
   // Accessors
   //
   
   /**
    * Returns the set of quantified variables.
    * 
    * @return the quantified type variables.
    */
   public Set<TypeVariable> getQuantifiedVariables() {
     return this.quantifiedVariables;
   }
   
   /**
    * Returns the monomorphic type.
    * 
    * @return the monomorphic type.
    */
   public MonoType getTau() {
     return this.tau;
   }
   
   
   
   //
   // Primitives
   //
   
   /**
    * {@inheritDoc}
    *
    * @see de.unisiegen.tpml.core.types.Type#free()
    */
   @Override
   public TreeSet<TypeVariable> free() {
     TreeSet<TypeVariable> free = new TreeSet<TypeVariable>();
     free.addAll(this.tau.free());
    free.remove(this.quantifiedVariables);
     return free;
   }
   
   /**
    * {@inheritDoc}
    *
    * @see de.unisiegen.tpml.core.types.Type#substitute(de.unisiegen.tpml.core.typechecker.TypeSubstitution)
    */
   @Override
   public Type substitute(TypeSubstitution substitution) {
     // determine the monomorphic type
     MonoType tau = this.tau;
     
     // perform a bound rename on the type variables
     TreeSet<TypeVariable> quantifiedVariables = new TreeSet<TypeVariable>();
     for (TypeVariable tvar : this.quantifiedVariables) {
       // generate a type variable that is not present in the substitution
       TypeVariable tvarn = tvar;
       while (!tvarn.substitute(substitution).equals(tvarn)) {
         tvarn = new TypeVariable(tvarn.getIndex(), tvarn.getOffset() + 1);
       }
       
       // check if we had to generate a new type variable
       if (!tvar.equals(tvarn)) {
         // substitute tvarn for tvar in tau
         tau = tau.substitute(TypeUtilities.newSubstitution(tvar, tvarn));
       }
       
       // add the type variable to the set
       quantifiedVariables.add(tvarn);
     }
     
     // apply the substitution to the monomorphic type
     tau = tau.substitute(substitution);
     
     // generate the polymorphic type
     return new PolyType(quantifiedVariables, tau);
   }
 
   
   
   //
   // Pretty printing
   //
   
   /**
    * {@inheritDoc}
    *
    * @see de.unisiegen.tpml.core.types.Type#toPrettyStringBuilder(de.unisiegen.tpml.core.prettyprinter.PrettyStringBuilderFactory)
    */
   @Override
   public PrettyStringBuilder toPrettyStringBuilder(PrettyStringBuilderFactory factory) {
     PrettyStringBuilder builder = factory.newBuilder(this, PRIO_POLY);
     if (!this.quantifiedVariables.isEmpty()) {
       builder.addText("\u2200");
       for (Iterator<TypeVariable> it = this.quantifiedVariables.iterator(); it.hasNext(); ) {
         builder.addText(it.next().toString());
         if (it.hasNext()) {
           builder.addText(", ");
         }
       }
       builder.addText(".");
     }
     builder.addBuilder(this.tau.toPrettyStringBuilder(factory), PRIO_POLY_TAU);
     return builder;
   }
   
   
   
   //
   // Base methods
   //
   
   /**
    * {@inheritDoc}
    *
    * @see java.lang.Object#equals(java.lang.Object)
    */
   @Override
   public boolean equals(Object obj) {
     if (obj instanceof PolyType) {
       PolyType other = (PolyType)obj;
       return (this.quantifiedVariables.equals(other.quantifiedVariables) && this.tau.equals(other.tau));
     }
     return false;
   }
  
   /**
    * {@inheritDoc}
    *
    * @see java.lang.Object#hashCode()
    */
   @Override
   public int hashCode() {
     return this.quantifiedVariables.hashCode() + this.tau.hashCode();
   }
 }
