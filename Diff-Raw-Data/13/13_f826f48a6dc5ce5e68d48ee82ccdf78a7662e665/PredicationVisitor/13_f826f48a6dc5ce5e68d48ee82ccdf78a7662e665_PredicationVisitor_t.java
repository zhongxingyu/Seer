 package srt.tool;
 
 import srt.ast.AssertStmt;
 import srt.ast.AssignStmt;
 import srt.ast.AssumeStmt;
 import srt.ast.BinaryExpr;
 import srt.ast.BlockStmt;
 import srt.ast.DeclRef;
 import srt.ast.Expr;
 import srt.ast.HavocStmt;
 import srt.ast.IfStmt;
 import srt.ast.Stmt;
 import srt.ast.TernaryExpr;
 import srt.ast.UnaryExpr;
 import srt.ast.visitor.impl.DefaultVisitor;
 
 public class PredicationVisitor extends DefaultVisitor {
 
 	private String freshVariableSeed;
 
 	private DeclRef parentPredicate;
 
 	public PredicationVisitor() {
 		super(true);
 		freshVariableSeed = "$A";
 	}
 
 	/**
 	 * Fresh variable generator
 	 * 
 	 * @return a fresh variable that has not been used before
 	 */
 	private String getFreshVariable() {
 		String result = freshVariableSeed;
 		if (freshVariableSeed.charAt(freshVariableSeed.length()-1) == 'Z') {
 			freshVariableSeed.concat("A");
 		} else {
 			char lastChar = freshVariableSeed.charAt(freshVariableSeed.length()-1);
 			freshVariableSeed = freshVariableSeed.substring(0, freshVariableSeed.length()-1);
 			freshVariableSeed += (char) (lastChar+1);
 		}
 		return result;
 	}
 
 	@Override
 	public Object visit(IfStmt ifStmt) {
 		// Get a fresh variable for this if condition
 		BlockStmt stmts;
 		Expr rhs;
 
 		DeclRef newPredicate = new DeclRef(getFreshVariable());
 
 		if (parentPredicate == null) {
 			rhs = ifStmt.getCondition();
 		} else {
 			rhs = new BinaryExpr(BinaryExpr.LAND, parentPredicate, ifStmt.getCondition());
 		}
 		AssignStmt predicateIfAssignStmt = new AssignStmt(newPredicate, rhs);
 
 		DeclRef oldParentPredicate = parentPredicate;
 		parentPredicate = newPredicate;
 
 		// Process everything in the IF body with the current predicate set to the fresh variable
 		Stmt thenStmt = (Stmt) visit(ifStmt.getThenStmt());
 
 		// If there is an else statement
 		if (ifStmt.getElseStmt() != null) {
 			// Create predicate with negated if-condition

 			parentPredicate = oldParentPredicate;
 			newPredicate = new DeclRef(getFreshVariable());
 			if (parentPredicate == null) {
 				rhs = new UnaryExpr(UnaryExpr.LNOT, ifStmt.getCondition());
 			} else {
 				rhs = new BinaryExpr(BinaryExpr.LAND, parentPredicate, new UnaryExpr(UnaryExpr.LNOT, ifStmt.getCondition()));
 			}
 			AssignStmt predicateElseAssignStmt = new AssignStmt(newPredicate, rhs);
 
 			oldParentPredicate = parentPredicate;
 			parentPredicate = newPredicate;		
 			Stmt elseStmt = (Stmt) visit(ifStmt.getElseStmt());
 			stmts = new BlockStmt(new Stmt[] { predicateIfAssignStmt, thenStmt,  predicateElseAssignStmt, elseStmt},
 					/* basedOn= */ifStmt);
 		} else {
 			stmts = new BlockStmt(new Stmt[] { predicateIfAssignStmt, thenStmt},
 					/* basedOn= */ifStmt);
 		}
 		parentPredicate = oldParentPredicate;
 		return stmts;
 	}
 
 	@Override
 	public Object visit(AssertStmt assertStmt) {
 		return super.visit(assertStmt);
 	}
 
 	@Override
 	public Object visit(AssignStmt assignment) {
		if (parentPredicate != null) {
			Expr e = new TernaryExpr(parentPredicate, assignment.getRhs(), assignment.getLhs());
			return new AssignStmt(assignment.getLhs(), e);
		} else {
			return super.visit(assignment);	
		}
 	}
 
 	@Override
 	public Object visit(AssumeStmt assumeStmt) {
 		return super.visit(assumeStmt);
 	}
 
 	@Override
 	public Object visit(HavocStmt havocStmt) {
 		return super.visit(havocStmt);
 	}
 
 }
