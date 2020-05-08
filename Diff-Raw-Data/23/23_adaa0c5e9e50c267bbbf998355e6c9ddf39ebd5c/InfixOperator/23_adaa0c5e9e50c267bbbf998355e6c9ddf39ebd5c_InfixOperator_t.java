 package chameleon.support.member.simplename.operator.infix;
 
 import chameleon.oo.method.Method;
 import chameleon.oo.method.MethodHeader;
 import chameleon.support.member.simplename.operator.Operator;
import chameleon.util.CreationStackTrace;
 
 /**
  * @author Marko van Dooren
  */
 public class InfixOperator extends Operator {
 
	private CreationStackTrace _trace = new CreationStackTrace();

   public InfixOperator(MethodHeader header) {
     super(header);
   }
 
   protected InfixOperator cloneThis() {
     return new InfixOperator((MethodHeader) header().clone());
   }
 
   public boolean sameKind(Method other) {
 	  return(other instanceof InfixOperator);
   }
 
 }
