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
 	private int isRecord = 0;
 
 	@Override
 	public void visit(AbsAlloc acceptor) {
 		acceptor.type.accept(this);
 	}
 
 	@Override
 	public void visit(AbsArrayType acceptor) {
 		acceptor.loBound.accept(this);
 		acceptor.hiBound.accept(this);
 		
 		Integer lval = SemDesc.getActualConst(acceptor.loBound);
 		Integer hval = SemDesc.getActualConst(acceptor.hiBound);
 		
 		if (lval == null){
 			notAValueError("loBound", acceptor.loBound.begLine, acceptor.loBound.begColumn);
 		}
 		if (hval == null){
 			notAValueError("hiBound", acceptor.hiBound.begLine, acceptor.hiBound.begColumn);
 		}
 		acceptor.type.accept(this);
 	}
 
 	@Override
 	public void visit(AbsAssignStmt acceptor) {
 		acceptor.dstExpr.accept(this);
 		acceptor.srcExpr.accept(this);
 	}
 
 	@Override
 	public void visit(AbsAtomConst acceptor) {
 		if (acceptor.type == AbsAtomConst.INT){
 			try {
 				SemDesc.setActualConst(acceptor, Integer.parseInt(acceptor.value));
 			} catch (Exception e) {
 				System.out.println(String.format("error parsing int value at (%d,%d)", acceptor.begLine, acceptor.endLine));
 			}
 		}
 	}
 
 	@Override
 	public void visit(AbsAtomType acceptor) {
 		// nothing to do here,
 	}
 
 	@Override
 	public void visit(AbsBinExpr acceptor) {
 		acceptor.fstExpr.accept(this);
 		if (acceptor.oper == AbsBinExpr.RECACCESS){
 			return;
 		}
 		acceptor.sndExpr.accept(this);
 		Integer fval = SemDesc.getActualConst(acceptor.fstExpr);
 		Integer sval = SemDesc.getActualConst(acceptor.sndExpr);
 		if (fval != null && sval != null){
 			switch (acceptor.oper) {
 			case AbsBinExpr.ADD:
 					SemDesc.setActualConst(acceptor, fval + sval);
 				break;
 			case AbsBinExpr.SUB:
 				SemDesc.setActualConst(acceptor, fval - sval);
 				break;
 			case AbsBinExpr.MUL:
 				SemDesc.setActualConst(acceptor, fval * sval);
 				break;
 			case AbsBinExpr.DIV:
 				if (sval != 0){
 					SemDesc.setActualConst(acceptor, fval / sval);
 				}
 				break;
 			}
 		}
 	}
 
 	@Override
 	public void visit(AbsBlockStmt acceptor) {
 		acceptor.stmts.accept(this);
 	}
 
 	@Override
 	public void visit(AbsCallExpr acceptor) {
 		acceptor.args.accept(this);
 		AbsDecl decl = SemTable.fnd(acceptor.name.name);
 		if (decl == null){
 			notDeclaredError(acceptor.name.name, acceptor.begLine, acceptor.begColumn);
 		}else{
 			SemDesc.setNameDecl(acceptor.name, decl);
 		}
 	}
 
 	@Override
 	public void visit(AbsConstDecl acceptor) {
 		if (isRecord == 0){
 			try {
 				SemTable.ins(acceptor.name.name, acceptor);
 			} catch (SemIllegalInsertException e) {
 				isDeclaredError(acceptor.name.name, acceptor.begLine, acceptor.begColumn);
 			}
 		}
 		
 		acceptor.value.accept(this);
 		
 		Integer val = SemDesc.getActualConst(acceptor.value);
 		if (val != null){
 			SemDesc.setActualConst(acceptor, val);
 		}else{
 			//notAValueError(acceptor.name.name, acceptor.begLine, acceptor.begColumn);
 		}
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
 		AbsDecl decl = SemTable.fnd(acceptor.name.name);
 		if (decl == null){
 			notDeclaredError(acceptor.name.name, acceptor.begLine, acceptor.begColumn);
 		}else{
 			SemDesc.setNameDecl(acceptor.name, decl);
 		}
 		acceptor.loBound.accept(this);
 		acceptor.hiBound.accept(this);
 		acceptor.stmt.accept(this);
 	}
 
 	@Override
 	public void visit(AbsFunDecl acceptor) {
 		SemTable.newScope();
 		try {
 			SemTable.ins(acceptor.name.name, acceptor);
 		} catch (SemIllegalInsertException e) {
 			isDeclaredError(acceptor.name.name, acceptor.begLine, acceptor.begColumn);
 		}
 		acceptor.pars.accept(this);
 		acceptor.decls.accept(this);
 		acceptor.type.accept(this);
 		acceptor.stmt.accept(this);
 		SemTable.oldScope();
 		try {
 			SemTable.ins(acceptor.name.name, acceptor);
 		} catch (SemIllegalInsertException e) {
 			isDeclaredError(acceptor.name.name, acceptor.begLine, acceptor.begColumn);
 		}
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
 		try {
 			SemTable.ins(acceptor.name.name, acceptor);
 		} catch (SemIllegalInsertException e) {
 			isDeclaredError(acceptor.name.name, acceptor.begLine, acceptor.begColumn);
 		}
 	}
 
 	@Override
 	public void visit(AbsProgram acceptor) {
 		acceptor.decls.accept(this);
 		acceptor.stmt.accept(this);
 	}
 
 	@Override
 	public void visit(AbsRecordType acceptor) {
 		isRecord++;
 		acceptor.fields.accept(this);
 		isRecord--;
 	}
 
 	@Override
 	public void visit(AbsStmts acceptor) {
 		for (AbsStmt stmt: acceptor.stmts){
 			stmt.accept(this);
 		}
 	}
 
 	@Override
 	public void visit(AbsTypeDecl acceptor) {
 		if (isRecord == 0){
 			try {
 				SemTable.ins(acceptor.name.name, acceptor);
 			} catch (SemIllegalInsertException e) {
 				isDeclaredError(acceptor.name.name, acceptor.begLine, acceptor.begColumn);
 			}
 		}
 		acceptor.type.accept(this);
 		SemDesc.setNameDecl(acceptor.name, acceptor);
 	}
 
 	@Override
 	public void visit(AbsTypeName acceptor) {
 		AbsDecl decl = SemTable.fnd(acceptor.name);
 		if (decl==null) {
 			
 		}else{
 			SemDesc.setNameDecl(acceptor, decl);
 		}
 	}
 
 	@Override
 	public void visit(AbsUnExpr acceptor) {
 		acceptor.expr.accept(this);
 	}
 
 	@Override
 	public void visit(AbsValExprs acceptor) {
 		for (AbsValExpr expr: acceptor.exprs){
 			expr.accept(this);
 		}
 	}
 
 	@Override
 	public void visit(AbsValName acceptor) {
 		AbsDecl decl = SemTable.fnd(acceptor.name);
 		if (decl == null){
 			notDeclaredError(acceptor.name, acceptor.begLine, acceptor.begColumn);
 		}else{
 			SemDesc.setNameDecl(acceptor, decl);
 			Integer val = SemDesc.getActualConst(decl);
 			if (val != null){
 				SemDesc.setActualConst(acceptor, val);
 			}
 		}
 	}
 
 	@Override
 	public void visit(AbsVarDecl acceptor) {
 		if (isRecord == 0){
 			try {
 				SemTable.ins(acceptor.name.name, acceptor);
 			} catch (SemIllegalInsertException e) {
 				isDeclaredError(acceptor.name.name, acceptor.begLine, acceptor.begColumn);
 			}
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
 		error = true;
 	}
 	
 	private void notAValueError(String name, int line, int col){
 		System.out.println(String.format("const %s can not be evalueted at (%d,%d)", name, line, col));
 		error = true;
 	}
 
 	private void notDeclaredError(String name, int line, int col){
 		System.out.println(String.format("var %s is undefined at (%d,%d)", name, line, col));
 		error = true;
 	}
 
 }
