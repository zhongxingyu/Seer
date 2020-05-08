 package compiler.imcode;
 
 import java.util.LinkedList;
 
 import compiler.abstree.AbsCodeVisitor;
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
 import compiler.frames.FrmAccess;
 import compiler.frames.FrmArgAccess;
 import compiler.frames.FrmDesc;
 import compiler.frames.FrmFrame;
 import compiler.frames.FrmLabel;
 import compiler.frames.FrmLocAccess;
 import compiler.frames.FrmVarAccess;
 import compiler.semanal.SemDesc;
 import compiler.semanal.SistemskeFunkcije;
 import compiler.semanal.type.SemArrayType;
 import compiler.semanal.type.SemRecordType;
 import compiler.semanal.type.SemType;
 
 public class IMCodeGenerator implements AbsCodeVisitor {
 
 	public LinkedList<ImcChunk> chunks = new LinkedList<ImcChunk>();
 
 	private FrmFrame currentFrame;
 
 	@Override
 	public ImcCode codeVisit(AbsAlloc acceptor) {
		SemType t = SemDesc.getActualType(acceptor.type);
 		ImcCALL call = new ImcCALL(FrmLabel.newLabel("malloc"));
 		call.args.add(new ImcCONST(SistemskeFunkcije.FAKE_FP));
 		call.size.add(4);
 		call.args.add(new ImcCONST(t.size()));
 		call.size.add(4);
 		//TODO: finish it
 		return call;
 	}
 	@Override
 	public ImcCode codeVisit(AbsArrayType acceptor) {
 		return null;
 	}
 	@Override
 	public ImcCode codeVisit(AbsAssignStmt acceptor) {
 		ImcExpr srcExpr = (ImcExpr)acceptor.srcExpr.codeVisit(this);
 		ImcExpr dstExpr = (ImcExpr)acceptor.dstExpr.codeVisit(this);
 		return new ImcMOVE(dstExpr,srcExpr);
 	}
 	@Override
 	public ImcCode codeVisit(AbsAtomConst acceptor) {
 		switch (acceptor.type) {
 		case AbsAtomConst.BOOL:
 			return new ImcCONST(acceptor.value.equals("true") ? 1 : 0);
 		case AbsAtomConst.CHAR:
 			return new ImcCONST((int) acceptor.value.charAt(1));
 		case AbsAtomConst.INT:
 			return new ImcCONST(Integer.parseInt(acceptor.value));
 		default:
 			return null;
 		}
 	}
 	@Override
 	public ImcCode codeVisit(AbsAtomType acceptor) {
 		return null;
 	}
 	@Override
 	public ImcCode codeVisit(AbsBinExpr acceptor) {
 		if (acceptor.oper == AbsBinExpr.RECACCESS){
 			SemRecordType srt = (SemRecordType) SemDesc.getActualType(acceptor.fstExpr);
 			int offset = 0;
 			for (int i = 0; i < srt.getNumFields(); i++) {
 				if (((AbsValName)acceptor.sndExpr).name.equals(srt.getFieldName(i).name)){
 					
 					ImcCode fexpCode = acceptor.fstExpr.codeVisit(this);
 					ImcExpr fexp = null;
 					if (fexpCode instanceof ImcMEM){
 						fexp = ((ImcMEM) fexpCode).expr;
 					}else if (fexpCode instanceof ImcCALL){
 						fexp = new ImcMEM((ImcCALL) fexpCode);
 					}else if (fexpCode instanceof ImcTEMP){
 						fexp = new ImcMEM((ImcTEMP) fexpCode);
 					}
 					if (acceptor.fstExpr instanceof AbsValName){
 						AbsVarDecl varDecl = (AbsVarDecl)(SemDesc.getNameDecl(acceptor.fstExpr) 
 								instanceof AbsVarDecl ? SemDesc.getNameDecl(acceptor.fstExpr) : null);
 						FrmAccess access = FrmDesc.getAccess(varDecl);
 						if (access instanceof FrmArgAccess ){
 							fexp = fexp instanceof ImcMEM ? fexp : new ImcMEM(fexp);
 						}
 					}
 					return new ImcMEM(new ImcBINOP(ImcBINOP.ADD, fexp , new ImcCONST(offset)));
 				}
 				offset += srt.getFieldType(i).size();
 			}
 		}else if (acceptor.oper == AbsBinExpr.ARRACCESS){
 
 			ImcCode fexpCode = acceptor.fstExpr.codeVisit(this);
 			ImcExpr fexp = null;
 			if (fexpCode instanceof ImcMEM){
 				fexp = ((ImcMEM) fexpCode).expr;
 			}else if (fexpCode instanceof ImcCALL){
 				fexp = new ImcMEM((ImcCALL) fexpCode);
 			}else if (fexpCode instanceof ImcTEMP){
 				fexp = new ImcMEM((ImcTEMP) fexpCode);
 			}
 			ImcExpr sexp = (ImcExpr) acceptor.sndExpr.codeVisit(this);
 
 			SemArrayType arr = (SemArrayType) SemDesc.getActualType(acceptor.fstExpr);
 
 			ImcBINOP ind = new ImcBINOP(ImcBINOP.SUB, sexp, new ImcCONST(arr.loBound));
 			ImcBINOP ofset = new ImcBINOP(ImcBINOP.MUL, ind, new ImcCONST(arr.type.size()));
 			
 			if (acceptor.fstExpr instanceof AbsValName){
 				AbsVarDecl varDecl = (AbsVarDecl)(SemDesc.getNameDecl(acceptor.fstExpr) 
 						instanceof AbsVarDecl ? SemDesc.getNameDecl(acceptor.fstExpr) : null);
 				FrmAccess access = FrmDesc.getAccess(varDecl);
 				if (access instanceof FrmArgAccess){
 					fexp = fexp instanceof ImcMEM ? fexp : new ImcMEM(fexp);
 				}
 			}
 			
 			
 			
 			//return new ImcMEM(new ImcBINOP(ImcBINOP.ADD, fexp instanceof ImcMEM ? fexp : new ImcMEM(fexp), ofset));
 			return new ImcMEM(new ImcBINOP(ImcBINOP.ADD, fexp , ofset));
 		}else{
 			ImcExpr fexp = (ImcExpr) acceptor.fstExpr.codeVisit(this);
 			ImcExpr sexp = (ImcExpr) acceptor.sndExpr.codeVisit(this);
 			return new ImcBINOP(acceptor.oper, fexp, sexp);
 		}
 		return null;
 	}
 	@Override
 	public ImcCode codeVisit(AbsBlockStmt acceptor) {
 		return acceptor.stmts.codeVisit(this);
 	}
 	@Override
 	public ImcCode codeVisit(AbsCallExpr acceptor) {
 
 		ImcCALL call = null;
 		if(SistemskeFunkcije.isSys(acceptor.name.name)) {
 			call = new ImcCALL(FrmLabel.newLabel(acceptor.name.name));
 			call.args.add(new ImcCONST(SistemskeFunkcije.FAKE_FP));
 			call.size.add(4);
 		} else {
 			FrmFrame frame = FrmDesc.getFrame(SemDesc.getNameDecl(acceptor.name));
 			call = new ImcCALL(frame.label);
 			if (currentFrame.equals(frame)){
 				call.args.add(new ImcMEM(new ImcTEMP(currentFrame.FP)));
 				call.size.add(4);
 			}else{
 				call.args.add(new ImcTEMP(currentFrame.FP));
 				call.size.add(4);
 			}
 		}
 		for(AbsValExpr expression: acceptor.args.exprs) {
 			if (SemDesc.getActualType(expression) instanceof SemRecordType ||
 					SemDesc.getActualType(expression) instanceof SemArrayType){
 				call.args.add(((ImcMEM)expression.codeVisit(this)).expr);
 				call.size.add(4);
 			}else{	
 				call.args.add((ImcExpr)expression.codeVisit(this));
 				call.size.add(4);
 			}
 		}
 		return call;
 	}
 	@Override
 	public ImcCode codeVisit(AbsConstDecl acceptor) {
 		return null;
 	}
 	@Override
 	public ImcCode codeVisit(AbsDeclName acceptor) {
 		return null;
 	}
 	@Override
 	public ImcCode codeVisit(AbsDecls acceptor) {
 		for (AbsDecl decl : acceptor.decls) {
 			if(decl instanceof AbsFunDecl || decl instanceof AbsProcDecl) {
 				decl.codeVisit(this);
 			}
 		}
 		return null;
 	}
 	@Override
 	public ImcCode codeVisit(AbsExprStmt acceptor) {
 		return new ImcEXP((ImcExpr)acceptor.expr.codeVisit(this));
 	}
 	@Override
 	public ImcCode codeVisit(AbsForStmt acceptor) {
 		ImcSEQ seq = new ImcSEQ();
 
 		ImcExpr var = (ImcExpr)acceptor.name.codeVisit(this);
 		ImcExpr loBound = (ImcExpr)acceptor.loBound.codeVisit(this);
 		ImcExpr hiBound = (ImcExpr)acceptor.hiBound.codeVisit(this);
 
 		ImcLABEL tl = new ImcLABEL(FrmLabel.newLabel());
 		ImcLABEL fl = new ImcLABEL(FrmLabel.newLabel());
 		ImcLABEL sl = new ImcLABEL(FrmLabel.newLabel());
 
 		seq.stmts.add(new ImcMOVE(var, loBound));
 		seq.stmts.add(sl);
 		seq.stmts.add(new ImcCJUMP(new ImcBINOP(ImcBINOP.LEQ, var, hiBound), tl.label, fl.label));
 		seq.stmts.add(tl);
 		seq.stmts.add((ImcStmt)acceptor.stmt.codeVisit(this));
 		seq.stmts.add(new ImcMOVE(var, new ImcBINOP(ImcBINOP.ADD, var, new ImcCONST(1))));
 		seq.stmts.add(new ImcJUMP(sl.label));
 		seq.stmts.add(fl);
 
 		return seq;
 	}
 	@Override
 	public ImcCode codeVisit(AbsFunDecl acceptor) {
 		currentFrame = FrmDesc.getFrame(acceptor);
 		chunks.add(new ImcCodeChunk(currentFrame, (ImcStmt)acceptor.stmt.codeVisit(this)));
 		acceptor.decls.codeVisit(this);
 		return null;
 	}
 	@Override
 	public ImcCode codeVisit(AbsIfStmt acceptor) {
 		ImcSEQ statement = new ImcSEQ();
 
 		ImcExpr cond = (ImcExpr)acceptor.cond.codeVisit(this);
 
 		ImcLABEL trueLabel = new ImcLABEL(FrmLabel.newLabel());
 		ImcLABEL falseLabel = new ImcLABEL(FrmLabel.newLabel());
 		ImcLABEL finish = new ImcLABEL(FrmLabel.newLabel());
 
 		statement.stmts.add(new ImcCJUMP(cond, trueLabel.label, falseLabel.label));
 		statement.stmts.add(trueLabel);
 		statement.stmts.add((ImcStmt)acceptor.thenStmt.codeVisit(this));
 		statement.stmts.add(new ImcJUMP(finish.label));
 		statement.stmts.add(falseLabel);
 		statement.stmts.add((ImcStmt)acceptor.elseStmt.codeVisit(this));
 		statement.stmts.add(finish);
 
 		return statement;
 	}
 	@Override
 	public ImcCode codeVisit(AbsNilConst acceptor) {
 		return new ImcCONST(0);
 	}
 	@Override
 	public ImcCode codeVisit(AbsPointerType acceptor) {
 		return null;
 	}
 	@Override
 	public ImcCode codeVisit(AbsProcDecl acceptor) {
 		currentFrame = FrmDesc.getFrame(acceptor);
 		chunks.add(new ImcCodeChunk(currentFrame, (ImcStmt)acceptor.stmt.codeVisit(this)));
 		acceptor.decls.codeVisit(this);
 		return null;
 	}
 	@Override
 	public ImcCode codeVisit(AbsProgram acceptor) {
 		currentFrame = FrmDesc.getFrame(acceptor);
 		ImcCode code = acceptor.stmt.codeVisit(this);
 		acceptor.decls.codeVisit(this);
 		chunks.add(new ImcCodeChunk(FrmDesc.getFrame(acceptor), (ImcStmt)code));
 
 		for (AbsDecl decl : acceptor.decls.decls) {
 			if (decl instanceof AbsVarDecl) {
 				AbsVarDecl varDecl = (AbsVarDecl)decl;
 				FrmVarAccess access = (FrmVarAccess)FrmDesc.getAccess(varDecl);
 				SemType type = SemDesc.getActualType(varDecl.type);
 				ImcDataChunk dataChunk = new ImcDataChunk(access.label, (type != null ? type.size() : 0));
 				chunks.add(dataChunk);
 			} 
 		}
 		
 		return null;
 	}
 	@Override
 	public ImcCode codeVisit(AbsRecordType acceptor) {
 		return null;
 	}
 	@Override
 	public ImcCode codeVisit(AbsStmts acceptor) {
 		ImcSEQ seq = new ImcSEQ();
 		for (AbsStmt stmt: acceptor.stmts) {
 			seq.stmts.add((ImcStmt) stmt.codeVisit(this));
 		}
 		return seq;
 	}
 	@Override
 	public ImcCode codeVisit(AbsTypeDecl acceptor) {
 		return null;
 	}
 	@Override
 	public ImcCode codeVisit(AbsTypeName acceptor) {
 		return null;
 	}
 	@Override
 	public ImcCode codeVisit(AbsUnExpr acceptor) {
 		switch (acceptor.oper) {
 		case AbsUnExpr.ADD:
 			return new ImcBINOP(ImcBINOP.ADD, new ImcCONST(0), (ImcExpr)acceptor.expr.codeVisit(this));
 		case AbsUnExpr.SUB:
 			return new ImcBINOP(ImcBINOP.SUB, new ImcCONST(0), (ImcExpr)acceptor.expr.codeVisit(this));
 		case AbsUnExpr.NOT:
 			return new ImcBINOP(ImcBINOP.EQU, new ImcCONST(0), (ImcExpr)acceptor.expr.codeVisit(this));
 		case AbsUnExpr.MEM:
 			return  ((ImcMEM)acceptor.expr.codeVisit(this)).expr;
 		case AbsUnExpr.VAL:
 			return new ImcMEM((ImcExpr)acceptor.expr.codeVisit(this));
 		}		
 		return null;
 	}
 	@Override
 	public ImcCode codeVisit(AbsValExprs acceptor) {
 		ImcSEQ seq = new ImcSEQ();
 		for (AbsValExpr expr: acceptor.exprs) {
 			seq.stmts.add((ImcStmt)expr.codeVisit(this));
 		}
 		return seq;
 	}
 	@Override
 	public ImcCode codeVisit(AbsValName acceptor) {
 		ImcCode code = null;
 		AbsDecl decl = SemDesc.getNameDecl(acceptor);
 		FrmFrame frame = FrmDesc.getFrame(decl);
 		FrmAccess access = FrmDesc.getAccess(decl);
 		if(access instanceof FrmVarAccess) {
 			FrmVarAccess va = (FrmVarAccess)access;
 			code = new ImcMEM(new ImcNAME(va.label));
 		}
 		if(access instanceof FrmArgAccess) {
 
 			FrmArgAccess argument = (FrmArgAccess)access;
 
 			ImcExpr t = new ImcTEMP(currentFrame.FP);
 			for (int i = argument.frame.level; i < currentFrame.level; i++) {
 				t = new ImcMEM(t);
 			}
 			code = new ImcMEM(new ImcBINOP(ImcBINOP.ADD, t, new ImcCONST(argument.offset)));
 		}
 		if(access instanceof FrmLocAccess) {
 			FrmLocAccess argument = (FrmLocAccess)access;
 			ImcExpr t = new ImcTEMP(currentFrame.FP);
 			for (int i = argument.frame.level; i < currentFrame.level; i++) {
 				t = new ImcMEM(t);
 			}
 			code = new ImcMEM(new ImcBINOP(ImcBINOP.ADD, t, new ImcCONST(argument.offset)));
 		}
 		if(decl instanceof AbsFunDecl) {
 			code = (new ImcTEMP(frame.RV));
 		}
 		if(decl instanceof AbsConstDecl) {
 			code = new ImcCONST(SemDesc.getActualConst(decl));
 		}
 		return code;
 	}
 	@Override
 	public ImcCode codeVisit(AbsVarDecl acceptor) {
 		return null;
 	}
 	@Override
 	public ImcCode codeVisit(AbsWhileStmt acceptor) {
 		ImcSEQ s = new ImcSEQ();
 
 		ImcExpr cond = (ImcExpr)acceptor.cond.codeVisit(this);
 
 		ImcLABEL loopBody = new ImcLABEL(FrmLabel.newLabel());
 		ImcLABEL finish = new ImcLABEL(FrmLabel.newLabel());
 		ImcLABEL startLabel = new ImcLABEL(FrmLabel.newLabel());
 
 		s.stmts.add(startLabel);
 		s.stmts.add(new ImcCJUMP(cond, loopBody.label, finish.label));
 		s.stmts.add(loopBody);
 		s.stmts.add((ImcStmt)acceptor.stmt.codeVisit(this));
 		s.stmts.add(new ImcJUMP(startLabel.label));
 		s.stmts.add(finish);
 
 		return s;
 	}
 
 }
