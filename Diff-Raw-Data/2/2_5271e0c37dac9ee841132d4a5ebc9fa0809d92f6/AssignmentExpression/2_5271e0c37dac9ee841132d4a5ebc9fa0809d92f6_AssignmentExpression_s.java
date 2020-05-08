 package chameleon.support.expression;
 
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.rejuse.association.SingleAssociation;
 
 import chameleon.core.element.Element;
 import chameleon.core.expression.Assignable;
 import chameleon.core.expression.Expression;
 import chameleon.core.expression.InvocationTarget;
 import chameleon.core.lookup.LookupException;
 import chameleon.core.validation.BasicProblem;
 import chameleon.core.validation.Valid;
 import chameleon.core.validation.VerificationResult;
 import chameleon.oo.type.Type;
 import chameleon.util.Util;
 
 /**
  * @author Marko van Dooren
  */
 public class AssignmentExpression extends Expression<AssignmentExpression> {
 
   /**
    * @param first
    * @param second
    */
   public AssignmentExpression(Expression var, Expression value) {
     
 	  setVariable(var);
     setValue(value);
   }
 
 	/**
 	 * VARIABLE
 	 */
 	private SingleAssociation<AssignmentExpression,Expression> _variable = new SingleAssociation<AssignmentExpression,Expression>(this);
 
 
   public Expression getVariable() {
     return _variable.getOtherEnd();
   }
 
   public void setVariable(Expression var) {
   	if(var != null) {
       _variable.connectTo(var.parentLink());
   	}
   	else {
   		_variable.connectTo(null);
   	}
   }
 
 	/**
 	 * VALUE
 	 */
 	private SingleAssociation<AssignmentExpression,Expression> _value = new SingleAssociation<AssignmentExpression,Expression>(this);
 
   public Expression getValue() {
     return (Expression)_value.getOtherEnd();
   }
 
   public void setValue(Expression expression) {
   	if(expression != null) {
       _value.connectTo(expression.parentLink());
   	} else {
   		_value.connectTo(null);
   	}
   }
 
   protected Type actualType() throws LookupException {
     return getVariable().getType();
   }
 
   public AssignmentExpression clone() {
     return new AssignmentExpression(getVariable().clone(), ((Expression<? extends Expression>)getValue()).clone());
   }
 
   public List<Element> children() {
     List<Element> result = Util.createNonNullList(getVariable());
     Util.addNonNull(getValue(), result);
     return result;
   }
 
   public Set<Type> getDirectExceptions() throws LookupException {
     return new HashSet<Type>();
   }
 
 	@Override
 	public VerificationResult verifySelf() {
 		VerificationResult result = Valid.create();
 		try {
 			Expression var = getVariable();
 			if(var == null) {
 				result = result.and(new BasicProblem(this, "The assignment has no variable at the left-hand side"));
 			}
 			Expression value = getValue();
 			if(value == null) {
 				result = result.and(new BasicProblem(this, "The assignment has no valid expression at the right-hand side"));
 			}
 			Type varType = var.getType();
 			Type exprType = value.getType();
			if(! exprType.assignableTo(varType)) {
 				result = result.and(new InvalidType(this, varType, exprType));
 			}
 		}
 	  catch (LookupException e) {
 			result = result.and(new BasicProblem(this, "The type of the expression is not assignable to the type of the variable."));
 	  }
 	  return result;
 	}
 
 	public static class InvalidType extends BasicProblem {
 
 		public InvalidType(Element element, Type varType, Type exprType) {
 			super(element, "The type of the left-hand side ("+exprType.getFullyQualifiedName()+") is not assignable to a variable of type "+varType.getFullyQualifiedName());
 		}
 		
 	}
 //  public AccessibilityDomain getAccessibilityDomain() throws LookupException {
 //    return getVariable().getAccessibilityDomain().intersect(getValue().getAccessibilityDomain());
 //  }
 
   
 }
