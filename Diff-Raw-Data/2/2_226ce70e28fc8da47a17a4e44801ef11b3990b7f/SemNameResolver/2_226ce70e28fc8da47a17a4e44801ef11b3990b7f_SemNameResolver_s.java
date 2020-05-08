 package compiler.semanal;
 
 import compiler.abstree.AbsVisitor;
 import compiler.abstree.tree.AbsAlloc;
 import compiler.abstree.tree.AbsArrayType;
 import compiler.abstree.tree.AbsAssignStmt;
 import compiler.abstree.tree.AbsAtomConst;
 import compiler.abstree.tree.AbsAtomType;
 import compiler.abstree.tree.AbsBinExpr;
 import compiler.abstree.tree.AbsBlockStmt;
 import compiler.abstree.tree.AbsCallExpr;
 import compiler.abstree.tree.AbsConstDecl;
 import compiler.abstree.tree.AbsDecl;
 import compiler.abstree.tree.AbsDeclName;
 import compiler.abstree.tree.AbsDecls;
 import compiler.abstree.tree.AbsExprStmt;
 import compiler.abstree.tree.AbsForStmt;
 import compiler.abstree.tree.AbsFunDecl;
 import compiler.abstree.tree.AbsIfStmt;
 import compiler.abstree.tree.AbsNilConst;
 import compiler.abstree.tree.AbsPointerType;
 import compiler.abstree.tree.AbsProcDecl;
 import compiler.abstree.tree.AbsProgram;
 import compiler.abstree.tree.AbsRecordType;
 import compiler.abstree.tree.AbsStmt;
 import compiler.abstree.tree.AbsStmts;
 import compiler.abstree.tree.AbsTypeDecl;
 import compiler.abstree.tree.AbsTypeName;
 import compiler.abstree.tree.AbsUnExpr;
 import compiler.abstree.tree.AbsValExpr;
 import compiler.abstree.tree.AbsValExprs;
 import compiler.abstree.tree.AbsValName;
 import compiler.abstree.tree.AbsVarDecl;
 import compiler.abstree.tree.AbsWhileStmt;
 
 public class SemNameResolver implements AbsVisitor{
 
 	public boolean error = false;
 
 	@Override
 	public void visit(AbsAlloc acceptor) {
 		acceptor.type.accept(this);
 	}
 
 	@Override
 	public void visit(AbsArrayType acceptor) {
 		acceptor.type.accept(this);
 		acceptor.loBound.accept(this);
 		acceptor.hiBound.accept(this);
 	}
 
 	@Override
 	public void visit(AbsAssignStmt acceptor) {
 		acceptor.dstExpr.accept(this);
 		acceptor.srcExpr.accept(this);
 	}
 
 	@Override
 	public void visit(AbsAtomConst acceptor) {
 		// nothing to do here,
 	}
 
 	@Override
 	public void visit(AbsAtomType acceptor) {
 		// nothing to do here,
 	}
 
 	@Override
 	public void visit(AbsBinExpr acceptor) {
 		acceptor.fstExpr.accept(this);
 		acceptor.sndExpr.accept(this);
 	}
 
 	@Override
 	public void visit(AbsBlockStmt acceptor) {
 		acceptor.stmts.accept(this);
 	}
 
 	@Override
 	public void visit(AbsCallExpr acceptor) {
 		notDeclaredError(acceptor.name.name, acceptor.begLine, acceptor.begColumn);
 		acceptor.args.accept(this);
 	}
 
 	@Override
 	public void visit(AbsConstDecl acceptor) {
 		try {
 			SemTable.ins(acceptor.name.name, acceptor);
 		} catch (SemIllegalInsertException e) {
 			isDeclaredError(acceptor.name.name, acceptor.begLine, acceptor.begColumn);
 		}
 		acceptor.value.accept(this);
 	}
 
 	@Override
 	public void visit(AbsDeclName acceptor) {
 		System.out.println("you should not see thise... public void visit(AbsDeclName acceptor)");
 	}
 
 	@Override
 	public void visit(AbsDecls acceptor) {
 		for (AbsDecl decl : acceptor.decls) {
 			decl.accept(this);
 		}
 	}
 
 	@Override
 	public void visit(AbsExprStmt acceptor) {
 		acceptor.expr.accept(this);
 	}
 
 	@Override
 	public void visit(AbsForStmt acceptor) {
 		notDeclaredError(acceptor.name.name, acceptor.begLine, acceptor.begColumn);
 		acceptor.loBound.accept(this);
 		acceptor.hiBound.accept(this);
 		acceptor.stmt.accept(this);
 	}
 
 	@Override
 	public void visit(AbsFunDecl acceptor) {
 		try {
 			SemTable.ins(acceptor.name.name, acceptor);
 		} catch (SemIllegalInsertException e) {
 			isDeclaredError(acceptor.name.name, acceptor.begLine, acceptor.begColumn);
 		}
 		SemTable.newScope();
 		try {
 			SemTable.ins(acceptor.name.name, acceptor);
 		} catch (SemIllegalInsertException e) {
 			isDeclaredError(acceptor.name.name, acceptor.begLine, acceptor.begColumn);
 		}
 
 		acceptor.decls.accept(this);
 		acceptor.type.accept(this);
 		acceptor.pars.accept(this);
 		acceptor.stmt.accept(this);
 		SemTable.oldScope();
 	}
 
 	@Override
 	public void visit(AbsIfStmt acceptor) {
 		acceptor.cond.accept(this);
 		acceptor.thenStmt.accept(this);
 		acceptor.elseStmt.accept(this);
 	}
 
 	@Override
 	public void visit(AbsNilConst acceptor) {
 		// should not do anything
 	}
 
 	@Override
 	public void visit(AbsPointerType acceptor) {
 		acceptor.type.accept(this);
 	}
 
 	@Override
 	public void visit(AbsProcDecl acceptor) {
 		try {
 			SemTable.ins(acceptor.name.name, acceptor);
 		} catch (SemIllegalInsertException e) {
 			isDeclaredError(acceptor.name.name, acceptor.begLine, acceptor.begColumn);
 		}
 		SemTable.newScope();
 		try {
 			SemTable.ins(acceptor.name.name, acceptor);
 		} catch (SemIllegalInsertException e) {
 			isDeclaredError(acceptor.name.name, acceptor.begLine, acceptor.begColumn);
 		}
 
 		acceptor.decls.accept(this);
 		acceptor.pars.accept(this);
 		acceptor.stmt.accept(this);
 		SemTable.oldScope();
 	}
 
 	@Override
 	public void visit(AbsProgram acceptor) {
 		acceptor.decls.accept(this);
 		acceptor.stmt.accept(this);
 	}
 
 	@Override
 	public void visit(AbsRecordType acceptor) {
 		SemTable.newScope();
 		acceptor.fields.accept(this);
 		SemTable.oldScope();
 	}
 
 	@Override
 	public void visit(AbsStmts acceptor) {
 		for (AbsStmt stmt: acceptor.stmts){
 			stmt.accept(this);
 		}
 	}
 
 	@Override
 	public void visit(AbsTypeDecl acceptor) {
 		try {
 			SemTable.ins(acceptor.name.name, acceptor);
 		} catch (SemIllegalInsertException e) {
 			isDeclaredError(acceptor.name.name, acceptor.begLine, acceptor.begColumn);
 		}
 		acceptor.type.accept(this);
 	}
 
 	@Override
 	public void visit(AbsTypeName acceptor) {
		isDeclaredError(acceptor.name, acceptor.begLine, acceptor.begColumn);
 	}
 
 	@Override
 	public void visit(AbsUnExpr acceptor) {
 		acceptor.expr.accept(this);
 	}
 
 	@Override
 	public void visit(AbsValExprs acceptor) {
 		// seznami spremenljivk v klicih procedur in funkcij
 		for (AbsValExpr expr: acceptor.exprs){
 			expr.accept(this);
 		}
 	}
 
 	@Override
 	public void visit(AbsValName acceptor) {
 		notDeclaredError(acceptor.name, acceptor.begLine, acceptor.begColumn);
 	}
 
 	@Override
 	public void visit(AbsVarDecl acceptor) {
 		try {
 			SemTable.ins(acceptor.name.name, acceptor);
 		} catch (SemIllegalInsertException e) {
 			isDeclaredError(acceptor.name.name, acceptor.begLine, acceptor.begColumn);
 		}
 		acceptor.type.accept(this);
 	}
 
 	@Override
 	public void visit(AbsWhileStmt acceptor) {
 		acceptor.cond.accept(this);
 		acceptor.stmt.accept(this);
 	}
 
 	private void isDeclaredError(String name, int line, int col){
 		System.out.println(String.format("var %s is redefined at (%d,%d)", name, line, col));
 	}
 
 	private void notDeclaredError(String name, int line, int col){
 		if (SemTable.fnd(name)==null){
 			System.out.println(String.format("var %s is undefined at (%d,%d)", name, line, col));
 		}
 	}
 
 }
