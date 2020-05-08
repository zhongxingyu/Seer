 package chameleon.support.expression;
 
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.rejuse.association.SingleAssociation;
 
 import chameleon.core.declaration.TargetDeclaration;
 import chameleon.core.element.Element;
 import chameleon.core.expression.Expression;
 import chameleon.core.expression.InvocationTarget;
 import chameleon.core.expression.NamedTarget;
 import chameleon.core.lookup.LookupException;
 import chameleon.core.lookup.LookupStrategy;
 import chameleon.core.namespace.NamespaceElementImpl;
 import chameleon.core.statement.CheckedExceptionList;
 import chameleon.core.validation.Valid;
 import chameleon.core.validation.VerificationResult;
 import chameleon.oo.type.Type;
import chameleon.oo.type.inheritance.InheritanceRelation;
 import chameleon.util.Util;
 
 /**
  * @author Marko van Dooren
  */
 public class SuperTarget extends NamespaceElementImpl<SuperTarget,Element> implements InvocationTarget<SuperTarget,Element> {
 
   public SuperTarget() {
 	}
   
   public SuperTarget(InvocationTarget target) {
   	setTarget(target);
   }
 
 	/**
 	 * TARGET
 	 */
 	private SingleAssociation<InvocationTarget,InvocationTarget> _target = new SingleAssociation<InvocationTarget,InvocationTarget>(this);
 
   public InvocationTarget<?,?> getTarget() {
     return _target.getOtherEnd();
   }
 
   public void setTarget(InvocationTarget target) {
     if (target != null) {
       _target.connectTo(target.parentLink());
     }
     else {
       _target.connectTo(null);
     }
   }
 
   public List<Element> children() {
   	return Util.createNonNullList(getTarget());
   }
 
   public SuperTarget clone() {
     SuperTarget result = new SuperTarget();
     if(getTarget() != null) {
       result.setTarget(getTarget().clone());
     }
     return result;
   }
 
   public void prefix(InvocationTarget target) {
     // Do nothing.
   }
 
   public void substituteParameter(String name, Expression expr) throws LookupException {
     // Do nothing.
   }
 
   public void prefixRecursive(InvocationTarget target) throws LookupException {
     prefix(target);
   }
 
   public Set<Type> getDirectExceptions() throws LookupException {
     return new HashSet<Type>();
   }
 
   public Set<Type> getExceptions() throws LookupException {
     return new HashSet<Type>();
   }
 
   public CheckedExceptionList getCEL() {
     return new CheckedExceptionList();
   }
 
   public CheckedExceptionList getAbsCEL() {
     return new CheckedExceptionList();
   }
 
   public TargetDeclaration getTargetDeclaration() throws LookupException {
     if(getTarget() != null) {
       return ((NamedTarget)getTarget()).getElement();
     } else {
      Type outer = nearestAncestor(Type.class);
      List<InheritanceRelation> inh = outer.inheritanceRelations();
      Type result = inh.get(0).superClass();
			return result;
     }
   }
 
   public LookupStrategy targetContext() throws LookupException {
     return getTargetDeclaration().targetContext();
   }
 
   /**
    * A super target is always valid. If invocations on a super target must always resolve to an effective declaration, 
    * as is the case in Java, then the language must add that rule. For mixins, for example, that must only be the case for
    * an actual combination of mixins.
    */
 	@Override
 	public VerificationResult verifySelf() {
 		return Valid.create();
 	}
 
 }
