 package jnome.core.expression;
 
 import java.util.List;
 import java.util.Set;
 
 import jnome.core.type.JavaTypeReference;
 
 import org.rejuse.association.OrderedMultiAssociation;
 import org.rejuse.association.SingleAssociation;
 import org.rejuse.java.collections.Visitor;
 
 import chameleon.core.element.Element;
 import chameleon.core.expression.Expression;
 import chameleon.core.expression.InvocationTarget;
 import chameleon.core.lookup.LookupException;
 import chameleon.core.type.Type;
 import chameleon.core.type.TypeReference;
 import chameleon.core.validation.Valid;
 import chameleon.core.validation.VerificationResult;
 import chameleon.support.expression.ArrayIndex;
 import chameleon.util.Util;
 
 /**
  * @author Marko van Dooren
  */
 public class ArrayCreationExpression extends Expression<ArrayCreationExpression>{
 
   public ArrayCreationExpression(JavaTypeReference type) {
     setTypeReference(type);
   }
 
 	/**
 	 * TYPE
 	 *
 	 */
 	private SingleAssociation<ArrayCreationExpression,JavaTypeReference> _typeReference = new SingleAssociation<ArrayCreationExpression,JavaTypeReference>(this);
 
 
   public JavaTypeReference getTypeReference() {
     return _typeReference.getOtherEnd();
   }
 
     public void setTypeReference(JavaTypeReference type) {
         SingleAssociation<? extends TypeReference, ? super ArrayCreationExpression> tref = type.parentLink();
         SingleAssociation<? extends JavaTypeReference, ? super ArrayCreationExpression> ref = (SingleAssociation<? extends JavaTypeReference, ? super ArrayCreationExpression>)tref;
         _typeReference.connectTo(ref);
     }
 
 	/**
 	 * DIMENSION INITIALIZERS
 	 */
 	private OrderedMultiAssociation<ArrayCreationExpression,ArrayIndex> _dimensionInitializers = new OrderedMultiAssociation<ArrayCreationExpression,ArrayIndex>(
 		this);
 
 
   public OrderedMultiAssociation getDimensionInitializersLink() {
     return _dimensionInitializers;
   }
 
   public void addDimensionInitializer(ArrayIndex init) {
 	    _dimensionInitializers.add(init.parentLink());
 	  }
 
 	  public void removeDimensionInitializer(ArrayIndex init) {
 	    _dimensionInitializers.remove(init.parentLink());
 	  }
 
   public List<ArrayIndex> getDimensionInitializers() {
     return _dimensionInitializers.getOtherEnds();
   }
 
 	/**
 	 * ARRAY INITIALIZER
 	 *
 	 * @uml.property name="_init"
 	 * @uml.associationEnd
 	 * @uml.property name="_init" multiplicity="(0 -1)" elementType="org.jnome.mm.expression.ArrayInitializer"
 	 */
 	private SingleAssociation<ArrayCreationExpression,ArrayInitializer> _init = new SingleAssociation<ArrayCreationExpression,ArrayInitializer>(this);
 
   public ArrayInitializer getInitializer() {
     return _init.getOtherEnd();
   }
 
   public void setInitializer(ArrayInitializer initializer) {
     if (initializer != null) {
       _init.connectTo(initializer.parentLink());
     }
     else {
       _init.connectTo(null);
     }
   }
 
   protected Type actualType() throws LookupException {
     JavaTypeReference tref = getTypeReference();
     if(tref != null) {
 		  return tref.getType();
     } else {
     	throw new LookupException("Type reference of array creation expression is null.");
     }
   }
 
   public ArrayCreationExpression clone() {
     final ArrayCreationExpression result = new ArrayCreationExpression((JavaTypeReference)getTypeReference().clone());
     if(getInitializer() != null) {
       result.setInitializer((ArrayInitializer)getInitializer().clone());
     }
     new Visitor() {
       public void visit(Object element) {
         //result.addDimensionInitializer(((DimensionInitializer)element).cloneDimInit());
     	  result.addDimensionInitializer(((ArrayIndex)element).clone());
       }
     }.applyTo(getDimensionInitializers());
     return result;
   }
 
  /*@
    @ also public behavior
    @
    @ post \result.containsAll(getDimensionInitializers());
    @ post getInitializer() != null ==> \result.contains(getInitializer());
    @*/
   public List<? extends Element> children() {
     final List<? extends Element> result = getDimensionInitializers();
     Util.addNonNull(getInitializer(), result);
     return result;
   }
 
   public Set getDirectExceptions() throws LookupException {
   	TypeReference ref = new TypeReference("java.lang.NegativeArraySizeException");
   	ref.setUniParent(getNamespace().defaultNamespace());
     return Util.createNonNullSet(ref.getType());
   }
 
 	@Override
 	public VerificationResult verifySelf() {
 		return Valid.create();
 	}
 
 //  public AccessibilityDomain getAccessibilityDomain() throws LookupException {
 //    AccessibilityDomain result = getTypeReference().getType().getTypeAccessibilityDomain();
 //    Iterator iter = getDimensionInitializers().iterator();
 //    while(iter.hasNext()) {
 //      DimensionInitializer init = (DimensionInitializer)iter.next();
 //      if(init != null) {
 //        result = result.intersect(init.getExpression().getAccessibilityDomain());
 //      }
 //    }
 //    if(getInitializer() != null) {
 //      result = result.intersect(getInitializer().getAccessibilityDomain());
 //    }
 //    return result;
 //  }
 
 }
