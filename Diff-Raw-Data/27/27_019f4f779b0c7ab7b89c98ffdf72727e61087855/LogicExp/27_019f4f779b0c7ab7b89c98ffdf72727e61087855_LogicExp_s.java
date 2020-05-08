 package translator.Expressions;
 
 import translator.JavaType;
 import translator.JavaScope;
 import translator.JavaStatic;
 
 import xtc.tree.GNode;
 
 /**
  * An expression with two operands and an operator.
  */
 public class LogicExp extends JavaExpression {
 
 	/**
 	 * The operator being used here.
 	 */
 	String operator;
 
 	/**
 	 * the first operand.
 	 */
 	JavaExpression first;
 
 	/**
 	 * The second operand.
 	 */
 	JavaExpression second;
 
 	public LogicExp(JavaScope scope, GNode n, String operator) {
 		super(scope, n);
 		this.operator = operator;
 	}
 
 	protected void onInstantiate(GNode n) {
		JavaStatic.dumpNode(n);;
 		this.first = (JavaExpression)this.dispatch((GNode)n.get(0));
 		if (n.size() > 1)
 			this.second = (JavaExpression)this.dispatch((GNode)n.get(1));
 
 		this.setType(JavaType.getType("boolean"));
 	}
 
 	public String print() {
 		if (!this.operator.equals("!"))
 			return "(" + first.print() + " " + operator + " " + second.print() + ")";
 		return "(" + this.operator + first.print() + ")";
 	}
 }
 
