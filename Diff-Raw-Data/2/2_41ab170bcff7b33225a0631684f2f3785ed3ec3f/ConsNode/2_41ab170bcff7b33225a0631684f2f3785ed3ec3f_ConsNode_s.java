 package tree;
 
 public class ConsNode extends ParNode {
 	
 	private ExprNode exprA, exprB;
 	
 	public ConsNode(ExprNode exprA, ExprNode exprB) {
 		this.exprA = exprA;
 		this.exprB = exprB;
 	}
 	
 	public ExprNode getExprA() {
 		return exprA;
 	}
 
 	public void setExprA(ExprNode exprA) {
 		this.exprA = exprA;
 	}
 
 	public ExprNode getExprB() {
 		return exprB;
 	}
 
 	public void setExprB(ExprNode exprB) {
 		this.exprB = exprB;
 	}
 
 	@Override
 	public void parse() {
 		this.exprA.parse();	
 		this.exprB.parse();	
 	}
 	
 	@Override
 	public String codeC() {
 		StringBuilder str = new StringBuilder();
 		str.append("wh::cons(");
 		str.append(exprA.codeC());
		str.append(" ");
 		str.append(exprB.codeC());
 		str.append(");");
 		return str.toString();
 	}
 
 	@Override
 	public String display() {
 		StringBuilder str = new StringBuilder();
 		str.append("|-cons\r\n");
 		str.append(this.format(exprA.display()));	
 		str.append(this.format(exprB.display()));	
 		return str.toString();
 	}
 
 	
 
 	
 }
