 package org.jmlspecs.jml4.boogie.ast;
 
 import java.util.ArrayList;
 
 import org.eclipse.jdt.internal.compiler.ast.ASTNode;
 import org.jmlspecs.jml4.boogie.BoogieSource;
 
 public class FunctionCall extends Expression {
 	private String functionName;
 	private ArrayList/*<Expression>*/ arguments;
 
 	public FunctionCall(String functionName, Expression[] exprs, ASTNode javaNode, Scope scope) {
 		super(javaNode, scope);
 		this.functionName = functionName;
 		this.arguments = new ArrayList();
 		if (exprs != null) {
 			for (int i = 0; i < exprs.length; i++) {
 				this.arguments.add(exprs[i]);
 			}
 		}
 	}
 	
 	public String getFunctionName() {
 		return functionName;
 	}
 	
 	public ArrayList getArguments() {
 		return arguments;
 	}
 
 	public void toBuffer(BoogieSource out) {
 		out.append(getFunctionName() + TOKEN_LPAREN);
 		for (int i = 0; i < getArguments().size(); i++) {
 			((Expression)getArguments().get(i)).toBuffer(out);
			if (i < getArguments().size() - 1) {
				out.append(TOKEN_COMMA + TOKEN_SPACE);
			}
 		}
 		out.append(TOKEN_RPAREN);
 	}
 
 	public void traverse(Visitor visitor) {
 		if (visitor.visit(this)) {
 			for (int i = 0; i < getArguments().size(); i++) {
 				((BoogieNode)getArguments().get(i)).traverse(visitor);
 			}
 		}
 		visitor.endVisit(this);
 	}
 }
