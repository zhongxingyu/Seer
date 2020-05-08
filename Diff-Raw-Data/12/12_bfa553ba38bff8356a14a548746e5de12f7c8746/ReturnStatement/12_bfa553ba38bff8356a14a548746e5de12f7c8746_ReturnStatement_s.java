 package translator.Statements;
 
 import translator.JavaScope;
 import translator.Expressions.JavaExpression;
 import translator.Printer.CodeBlock;
 
 import xtc.tree.GNode;
 
 public class ReturnStatement extends JavaStatement{
 	JavaExpression expression;
 
 	public ReturnStatement (JavaScope scope, GNode n) {
 		super(scope, n);
 	}
 
 	protected void onInstantiate(GNode n) {
 		expression = (JavaExpression)this.dispatch((GNode)n.get(0));
 	}
 
 	public void print(CodeBlock b) {
		if (expression != null)
 			b.pln("return " + expression.print() + ";");
 	}
 }
