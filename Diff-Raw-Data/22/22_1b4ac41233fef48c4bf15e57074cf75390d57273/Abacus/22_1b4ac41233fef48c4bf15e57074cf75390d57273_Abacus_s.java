 package core.common.util;
 
 import java.util.Map;
 
 import org.eclipse.jdt.core.dom.Expression;
 import org.eclipse.jdt.core.dom.InfixExpression;
 import org.eclipse.jdt.core.dom.NumberLiteral;
 import org.eclipse.jdt.core.dom.InfixExpression.Operator;
 
 public class Abacus {
 	public static int computeExpression(Expression exp, Map<String, Integer> fields){
 		int result = 0;
 		if(exp instanceof NumberLiteral){
 			result = Integer.parseInt(exp.toString());
 		}else if(exp instanceof InfixExpression){
 			InfixExpression infix = (InfixExpression)exp;
 			Expression left = infix.getLeftOperand();
 			Expression right = infix.getRightOperand();
 			
 			int leftVal = 0;
 			int rightVal = 0;
 			
 			if(left instanceof NumberLiteral){
 				leftVal = Integer.parseInt(left.toString());
 			}else{
 				leftVal = fields.get(left.toString());
 			}
 			
 			if(right instanceof NumberLiteral){
 				rightVal = Integer.parseInt(right.toString());
 			}else{
 				rightVal = fields.get(right.toString());
 			}
 			
 			Operator op = infix.getOperator();
 			switch(op.toString()){
 			case "+":
 				result = leftVal + rightVal;
 				break;
 			case "*":
 				result = leftVal * rightVal;
 				break;
 			case "-":
 				result = leftVal - rightVal;
 				break;
 			case "/":
 				result = leftVal / rightVal;
 				break;
 			default:
 				System.err.println("undefined operator" + op);
 				break;
 			}
 		} else if( exp == null){
 			result = 0;
 		} else {
			result = fields.get(exp.toString());
 		}
 		return result;
 	}
 	
 	public static boolean compareValue(Operator op, int left, int right){
 		boolean result = false;
 		switch(op.toString()){
 		case ">":
 			result = left > right ? true: false;
 			break;
 		case "<":
 			result = left < right ? true: false;
 			break;
 		case "==":
 			result = left == right ? true: false;
 			break;
 		case "!=":
 			result = left != right ? true: false;
 			break;
 		default:
 			System.err.println("Operator doesn't exist!");
 			break;
 		}
 		return result;
 	}
 }
