 package chameleon.support.expression;
 
 import java.util.List;
 
 import chameleon.core.declaration.Declaration;
 import chameleon.core.expression.InvocationTarget;
 import chameleon.core.lookup.DeclarationSelector;
 import chameleon.core.lookup.LookupException;
 import chameleon.oo.language.ObjectOrientedLanguage;
 import chameleon.oo.type.Type;
 import chameleon.support.member.simplename.method.NormalMethod;
 
 /**
  * @author Marko van Dooren
  */
 public class SuperConstructorDelegation extends ConstructorDelegation<SuperConstructorDelegation> {
 
   public SuperConstructorDelegation(){
     super(null);
   }
 
   protected Type actualType() throws LookupException {
     return language(ObjectOrientedLanguage.class).voidType();
   }
   
   // @FIXME: does not work with multiple inheritance. Call is ambiguous.
   public NormalMethod getMethod() throws LookupException {
 	    return nearestAncestor(Type.class).getDirectSuperTypes().get(0).lexicalLookupStrategy().lookUp(selector());
   }
   
   protected SuperConstructorDelegation cloneInvocation(InvocationTarget target) {
     return new SuperConstructorDelegation();
   }
 
   public <X extends Declaration> X getElement(DeclarationSelector<X> selector) throws LookupException {
   	X result = null;
   	Type parent = nearestAncestor(Type.class);
  	List<Type> types = parent.getDirectSuperTypes();
   	for(Type type: types) {
   		result = type.targetContext().lookUp(selector);
   		if(result != null) {
   			break;
   		}
   	}
   	return result;
   }
 
 }
