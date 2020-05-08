 package jnome.core.expression;
 
 
 import jnome.core.type.JavaTypeReference;
 
 import org.rejuse.association.SingleAssociation;
 
import chameleon.core.expression.InvocationTarget;
import chameleon.core.lookup.LookupException;
 import chameleon.core.type.TypeReference;
 import chameleon.support.expression.LiteralWithTypeReference;
 
 /**
  * @author Marko van Dooren
  */
 public class ClassLiteral extends LiteralWithTypeReference {
 
   public ClassLiteral(TypeReference tref) {
     super("class");
     setTarget(tref);
     setTypeReference(new JavaTypeReference("java.lang.Class"));
   }
 
   public ClassLiteral clone() {
    ClassLiteral result = new ClassLiteral(target().clone());
     result.setTypeReference((JavaTypeReference)getTypeReference().clone());
     return result;
   }
  
 	/**
 	 * TARGET
 	 */
 	private SingleAssociation<LiteralWithTypeReference,TypeReference> _typeReference = new SingleAssociation<LiteralWithTypeReference,TypeReference>(this);
 
   
   public TypeReference target() {
     return _typeReference.getOtherEnd();
   }
   
   public void setTarget(TypeReference type) {
     _typeReference.connectTo(type.parentLink());
   }
 
 //  public AccessibilityDomain getAccessibilityDomain() throws LookupException {
 //    return getTypeReference().getType().getTypeAccessibilityDomain();
 //  }
 }
