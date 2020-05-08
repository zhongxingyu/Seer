 package de.unisiegen.tpml.core.typechecker;
 
 import java.util.Enumeration;
import java.util.HashSet;
 import java.util.Set;
 
 import de.unisiegen.tpml.core.types.MonoType;
 import de.unisiegen.tpml.core.types.PolyType;
 import de.unisiegen.tpml.core.types.Type;
 import de.unisiegen.tpml.core.types.TypeVariable;
 import de.unisiegen.tpml.core.util.AbstractEnvironment;
 
 /**
  * Default implementation of the <code>TypeEnvironment</code> interface.
  *
  * @author Benedikt Meurer
  * @version $Rev$
  *
  * @see de.unisiegen.tpml.core.typechecker.TypeEnvironment
  * @see de.unisiegen.tpml.core.util.AbstractEnvironment
  */
 final class DefaultTypeEnvironment extends AbstractEnvironment<String, Type> implements TypeEnvironment {
   //
   // Constructor (package)
   //
   
   /**
    * Allocates a new empty <code>DefaultTypeEnvironment</code>.
    * 
    * @see AbstractEnvironment#AbstractEnvironment()
    */
   DefaultTypeEnvironment() {
     super();
   }
   
   /**
    * Allocates a new <code>DefaultTypeEnvironment</code> based on the <code>environment</code>.
    * 
    * @param environment the type environment from which to copy the mappings.
    * 
    * @throws NullPointerException if <code>environment</code> is <code>null</code>.
    * 
    * @see AbstractEnvironment#AbstractEnvironment(AbstractEnvironment)
    */
   DefaultTypeEnvironment(DefaultTypeEnvironment environment) {
     super(environment);
   }
   
   
   
   //
   // Primitives
   //
   
   /**
    * {@inheritDoc}
    *
    * @see de.unisiegen.tpml.core.typechecker.TypeEnvironment#closure(de.unisiegen.tpml.core.types.MonoType)
    */
   public PolyType closure(MonoType tau) {
     // determine the quantified type variables
    HashSet<TypeVariable> quantifiedVariables = new HashSet<TypeVariable>();
     quantifiedVariables.addAll(tau.free());
     quantifiedVariables.removeAll(free());
     
     // allocate the polymorphic type
     return new PolyType(quantifiedVariables, tau);
   }
   
   /**
    * {@inheritDoc}
    *
    * @see de.unisiegen.tpml.core.typechecker.TypeEnvironment#containsIdentifier(java.lang.String)
    */
   public boolean containsIdentifier(String identifier) {
     return containsSymbol(identifier);
   }
 
   /**
    * {@inheritDoc}
    *
    * @see de.unisiegen.tpml.core.typechecker.TypeEnvironment#free()
    */
   public Set<TypeVariable> free() {
    HashSet<TypeVariable> free = new HashSet<TypeVariable>();
     for (Mapping<String, Type> mapping : this.mappings) {
       free.addAll(mapping.getEntry().free());
     }
     return free;
   }
   
   /**
    * {@inheritDoc}
    *
    * @see de.unisiegen.tpml.core.typechecker.TypeEnvironment#identifiers()
    */
   public Enumeration<String> identifiers() {
     return symbols();
   }
 
   /**
    * {@inheritDoc}
    *
    * @see de.unisiegen.tpml.core.typechecker.TypeEnvironment#extend(java.lang.String, de.unisiegen.tpml.core.types.Type)
    */
   public TypeEnvironment extend(String identifier, Type type) {
     DefaultTypeEnvironment environment = new DefaultTypeEnvironment(this);
     environment.put(identifier, type);
     return environment;
   }
   
   /**
    * {@inheritDoc}
    *
    * @see de.unisiegen.tpml.core.typechecker.TypeEnvironment#substitute(de.unisiegen.tpml.core.typechecker.TypeSubstitution)
    */
   public TypeEnvironment substitute(TypeSubstitution s) {
     // create a new environment with the (possibly) new types
     DefaultTypeEnvironment environment = new DefaultTypeEnvironment();
     for (Mapping<String, Type> mapping : this.mappings) {
       Type newType = mapping.getEntry().substitute(s);
       if (!newType.equals(mapping.getEntry())) {
         environment.mappings.add(new Mapping<String, Type>(mapping.getSymbol(), newType));
       }
       else {
         environment.mappings.add(mapping);
       }
     }
     return environment;
   }
 }
