 package org.eclipse.dltk.ast.statements;
 
 import org.eclipse.dltk.ast.ASTVisitor;
 import org.eclipse.dltk.ast.DLTKToken;
 import org.eclipse.dltk.ast.expressions.Expression;
 import org.eclipse.dltk.utils.CorePrinter;
 
 /**
  * If statement.
  */
 public class IfStatement extends Statement {
 	/**
 	 * Condition expression.
 	 */
	private Expression fCondition;
 
 	/**
 	 * Then statement of if.
 	 */
 	private Statement fThenStatement;
 
 	/**
 	 * Else statement of if. Can be null, or possible EmptyStatement..
 	 */
 	private Statement fElseStatement;
 
	public IfStatement(DLTKToken ifToken, Expression condition,
 			Statement thenStatement) {
 
 		super(ifToken);
 		this.fCondition = condition;
 		this.fThenStatement = thenStatement;
 	}
 	
	public IfStatement(Expression condition, Statement thenStatement, Statement elseStatement) {
 		this.fCondition = condition;
 		this.fThenStatement = thenStatement;
 		fElseStatement = elseStatement;
 	}
 
 	public void traverse(ASTVisitor pVisitor) throws Exception {
 
 		if (pVisitor.visit(this)) {
 			if (fCondition != null) {
 				fCondition.traverse(pVisitor);
 			}
 			if (fThenStatement != null) {
 				fThenStatement.traverse(pVisitor);
 			}
 			if (fElseStatement != null) {
 				fElseStatement.traverse(pVisitor);
 			}
 			pVisitor.endvisit(this);
 		}
 	}
 
 	public int getKind() {
 		return S_IF;
 	}
 
 	// TODO: Replace to acceptElse for similarity.
 	/**
 	 * @deprecated
 	 */
 	public void setElse(Statement elses) {
 		this.acceptElse(elses);
 	}
 
 	/**
 	 * Acccept Else statement.
 	 * 
 	 * @param elseStatement
 	 */
 	public void acceptElse(Statement elseStatement) {
 		this.fElseStatement = elseStatement;
 		if (this.fElseStatement != null) {
 			this.setEnd(this.fElseStatement.sourceEnd());
 		}
 	}
 
 	/**
 	 * Return else statement.
 	 * 
 	 * @return - else statement. Be aware can be null.
 	 */
 	public Statement getElse() {
 		return fElseStatement;
 	}
 
 	public Statement getThen() {
 		return fThenStatement;
 	}
 
	public Expression getCondition() {
 		return this.fCondition;
 	}
 
 	public void printNode(CorePrinter output) {
 		output.formatPrintLn("if: ");
 		if (this.fCondition != null) {
 			this.fCondition.printNode(output);
 		}
 		if (this.fThenStatement != null) {
 			if (!(this.fThenStatement instanceof Block)) {
 				output.indent();
 			}
 			this.fThenStatement.printNode(output);
 			if (!(this.fThenStatement instanceof Block)) {
 				output.dedent();
 			}
 		}
 		if (this.fElseStatement != null) {
 			output.formatPrintLn("else:");
 			if (!(this.fElseStatement instanceof Block)) {
 				output.indent();
 			}
 			this.fElseStatement.printNode(output);
 			if (!(this.fElseStatement instanceof Block)) {
 				output.dedent();
 			}
 		}
 
 	}
 }
