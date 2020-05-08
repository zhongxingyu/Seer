 package compiler.imcode;
 
import java.util.ArrayList;
 import java.util.LinkedList;

 import compiler.abstree.AbsVisitor;
 import compiler.abstree.tree.*;
 import compiler.semanal.SemDesc;
 import compiler.semanal.SistemskeFunkcije;
 import compiler.semanal.type.*;
 import compiler.frames.*;
 
 public class IMCodeGenerator implements AbsVisitor {
 
 	public LinkedList<ImcChunk> chunks = new LinkedList<ImcChunk>();
 	
 	private FrmFrame curFrame = null;
 	
 	private ImcCode result;
 	private ImcCode getResult() {
 		ImcCode temp = result;
 		result = null;
 		return temp;
 	}
 	private void setResult(ImcCode result) {
 		this.result = result;
 	}
 	private LinkedList<Boolean> noMem = new LinkedList<Boolean>();
 
 	@Override
 	public void visit(AbsAlloc acceptor) {
 		SemType t = SemDesc.getActualType(acceptor.type);
 		ImcCALL c = new ImcCALL(FrmLabel.newLabel("malloc"));
 		c.args.add(new ImcCONST(SistemskeFunkcije.FAKE_FP));
 		c.size.add(4);
 		c.args.add(new ImcCONST(t.size()));
 		c.size.add(4);
 		setResult(c);
 	}
 
 	@Override
 	public void visit(AbsArrayType acceptor) {}
 
 	@Override
 	public void visit(AbsAssignStmt acceptor) {
 		acceptor.dstExpr.accept(this);
 		ImcExpr dst = (ImcExpr) getResult();
 		
 		acceptor.srcExpr.accept(this);
 		ImcExpr src = (ImcExpr) getResult();
 		
 		setResult(new ImcMOVE(dst, src));
 	}
 
 	@Override
 	public void visit(AbsAtomConst acceptor) {
 		int value = 0;
 		switch (acceptor.type) {
 		case AbsAtomConst.BOOL:
 			value = (acceptor.value.equals("true")) ? 1 : 0;
 			break;
 		case AbsAtomConst.CHAR:
 			value = (int)acceptor.value.charAt(1);
 			break;
 		case AbsAtomConst.INT:
 			value = Integer.parseInt(acceptor.value);
 			break;
 		}
 		setResult(new ImcCONST(value));
 	}
 
 	@Override
 	public void visit(AbsAtomType acceptor) {}
 
 	@Override
 	public void visit(AbsBinExpr acceptor) {
 		switch (acceptor.oper) {
 		case AbsBinExpr.RECACCESS:
 			noMem.addFirst(true);
 			acceptor.fstExpr.accept(this);
 			ImcExpr rec = (ImcExpr) getResult();
 			SemRecordType rt = (SemRecordType)SemDesc.getActualType(acceptor.fstExpr);
 			String n = ((AbsValName)acceptor.sndExpr).name;
 			
 			SemType sndType = null;
 			
 			int offset = 0;
 			for(int i=0; i<rt.getNumFields(); i++) {
 				if(n.equals(rt.getFieldName(i).name)) {
 					sndType = rt.getFieldType(i);
 					break;
 				}
 				offset += rt.getFieldType(i).size();
 			}
 			if(sndType instanceof SemRecordType || sndType instanceof SemArrayType) {
 				setResult(new ImcBINOP(ImcBINOP.ADD, rec, new ImcCONST(offset)));
 			} else {
 				setResult(new ImcMEM(new ImcBINOP(ImcBINOP.ADD, rec, new ImcCONST(offset))));
 			}
 			noMem.removeFirst();
 			break;
 		case AbsBinExpr.ARRACCESS:
 			noMem.addFirst(true);
 			acceptor.fstExpr.accept(this);
 			ImcExpr arr = (ImcExpr) getResult();
 			SemArrayType at = (SemArrayType)SemDesc.getActualType(acceptor.fstExpr);
 			
 			noMem.removeFirst();
 			acceptor.sndExpr.accept(this);
 			ImcExpr index = (ImcExpr)getResult();
 			noMem.addFirst(true);
 			
 			ImcBINOP tIndex = new ImcBINOP(ImcBINOP.SUB, index, new ImcCONST(at.loBound));
 			ImcBINOP tOffset = new ImcBINOP(ImcBINOP.MUL, tIndex, new ImcCONST(at.type.size()));
 			
 			setResult(new ImcMEM(new ImcBINOP(ImcBINOP.ADD, arr, tOffset)));
 			noMem.removeFirst();
 			break;
 		default:
 			acceptor.fstExpr.accept(this);
 			ImcExpr limc = (ImcExpr) getResult();
 			
 			acceptor.sndExpr.accept(this);
 			ImcExpr rimc = (ImcExpr) getResult();
 			
 			setResult(new ImcBINOP(acceptor.oper, limc, rimc));	
 			break;
 		}
 	}
 
 	@Override
 	public void visit(AbsBlockStmt acceptor) {
 		acceptor.stmts.accept(this);
 	}
 
 	@Override
 	public void visit(AbsCallExpr acceptor) {
 		ImcCALL c = null;
 		ImcExpr ex = null;
 		if(SistemskeFunkcije.isFunction(acceptor.name.name) || SistemskeFunkcije.isProcedure(acceptor.name.name)) {
 			c = new ImcCALL(FrmLabel.newLabel(acceptor.name.name));
 			c.args.add(new ImcCONST(SistemskeFunkcije.FAKE_FP));
 			c.size.add(4);
 		} else {
 			FrmFrame f = FrmDesc.getFrame(SemDesc.getNameDecl(acceptor.name));
 			c = new ImcCALL(f.label);
			if(curFrame == f) {
				c.args.add(new ImcMEM(new ImcTEMP(curFrame.FP)));
				c.size.add(4);
			} else {
				c.args.add(new ImcTEMP(curFrame.FP));
				c.size.add(4);
 			}
 		}
 		for(AbsValExpr e: acceptor.args.exprs) {
 			e.accept(this);
 			if(SemDesc.getActualType(e) instanceof SemRecordType || SemDesc.getActualType(e) instanceof SemArrayType) {
 				ex = (ImcExpr)getResult();
 				if(ex instanceof ImcMEM) {
 					setResult(((ImcMEM)ex).expr);
 				} else {
 					setResult(ex);
 				}
 			}
 			c.args.add((ImcExpr)getResult());
 			c.size.add(4); //SemDesc.getActualType(e).size()
 		}
 		setResult(c);
 	}
 
 	@Override
 	public void visit(AbsConstDecl acceptor) {}
 
 	@Override
 	public void visit(AbsDeclName acceptor) {}
 
 	@Override
 	public void visit(AbsDecls acceptor) {
 		for (AbsDecl decl : acceptor.decls) {
 			if(decl instanceof AbsFunDecl || decl instanceof AbsProcDecl) {
 				decl.accept(this);
 			}
 		}
 	}
 
 	@Override
 	public void visit(AbsExprStmt acceptor) {
 		acceptor.expr.accept(this);
 		setResult(new ImcEXP((ImcExpr)getResult()));
 	}
 
 	@Override
 	public void visit(AbsForStmt acceptor) {
 		ImcSEQ s = new ImcSEQ();
 		
 		acceptor.name.accept(this);
 		ImcExpr ne = (ImcExpr)getResult();
 		
 		acceptor.loBound.accept(this);
 		ImcExpr le = (ImcExpr)getResult();
 		
 		acceptor.hiBound.accept(this);
 		ImcExpr he = (ImcExpr)getResult();
 		
 		ImcLABEL tl = new ImcLABEL(FrmLabel.newLabel());
 		ImcLABEL fl = new ImcLABEL(FrmLabel.newLabel());
 		ImcLABEL sl = new ImcLABEL(FrmLabel.newLabel());
 		
 		s.stmts.add(new ImcMOVE(ne, le));
 		s.stmts.add(sl);
 		s.stmts.add(new ImcCJUMP(new ImcBINOP(ImcBINOP.LEQ, ne, he), tl.label, fl.label));
 		s.stmts.add(tl);
 		acceptor.stmt.accept(this);
 		s.stmts.add((ImcStmt)getResult());
 		s.stmts.add(new ImcMOVE(ne, new ImcBINOP(ImcBINOP.ADD, ne, new ImcCONST(1))));
 		s.stmts.add(new ImcJUMP(sl.label));
 		s.stmts.add(fl);
 		
 		setResult(s);
 	}
 
 	@Override
 	public void visit(AbsFunDecl acceptor) {
 		FrmFrame f = FrmDesc.getFrame(acceptor);
 		FrmFrame tmpFrm = curFrame;
 		curFrame = f;
 		acceptor.stmt.accept(this);
 		chunks.add(new ImcCodeChunk(f, (ImcStmt)getResult()));
 		acceptor.decls.accept(this);
 		curFrame = tmpFrm;
 	}
 
 	@Override
 	public void visit(AbsIfStmt acceptor) {
 		ImcSEQ s = new ImcSEQ();
 		
 		acceptor.cond.accept(this);
 		ImcExpr ce = (ImcExpr)getResult();
 		
 		ImcLABEL tl = new ImcLABEL(FrmLabel.newLabel());
 		ImcLABEL fl = new ImcLABEL(FrmLabel.newLabel());
 		ImcLABEL el = new ImcLABEL(FrmLabel.newLabel());
 		
 		s.stmts.add(new ImcCJUMP(ce, tl.label, fl.label));
 		s.stmts.add(tl);
 		acceptor.thenStmt.accept(this);
 		s.stmts.add((ImcStmt)getResult());
 		s.stmts.add(new ImcJUMP(el.label));
 		s.stmts.add(fl);
 		acceptor.elseStmt.accept(this);
 		s.stmts.add((ImcStmt)getResult());
 		s.stmts.add(el);
 		
 		setResult(s);
 	}
 
 	@Override
 	public void visit(AbsNilConst acceptor) {
 		setResult(new ImcCONST(0));
 	}
 
 	@Override
 	public void visit(AbsPointerType acceptor) {}
 
 	@Override
 	public void visit(AbsProcDecl acceptor) {
 		FrmFrame f = FrmDesc.getFrame(acceptor);
 		FrmFrame tmpFrm = curFrame;
 		curFrame = f;
 		acceptor.stmt.accept(this);
 		chunks.add(new ImcCodeChunk(f, (ImcStmt)getResult()));
 		acceptor.decls.accept(this);
 		curFrame = tmpFrm;
 	}
 
 	@Override
 	public void visit(AbsProgram acceptor) {
 		FrmFrame f = FrmDesc.getFrame(acceptor);
 		curFrame = f;
 		noMem.add(false); //init noMem
 		
 		for(AbsDecl decl: acceptor.decls.decls) {
 			if (decl instanceof AbsVarDecl) {
 				AbsVarDecl v = (AbsVarDecl)decl;
 				FrmVarAccess a = (FrmVarAccess)FrmDesc.getAccess(v);
 				SemType t = SemDesc.getActualType(v.type);
 				chunks.add(new ImcDataChunk(a.label, t.size()));
 			}
 		}
 		acceptor.decls.accept(this);
 		
 		acceptor.stmt.accept(this);
 		chunks.add(new ImcCodeChunk(f, (ImcStmt)getResult()));
 	}
 
 	@Override
 	public void visit(AbsRecordType acceptor) {}
 
 	@Override
 	public void visit(AbsStmts acceptor) {
 		ImcSEQ s = new ImcSEQ();
 		for(AbsStmt stmt: acceptor.stmts) {
 			stmt.accept(this);
 			s.stmts.add((ImcStmt)getResult());
 		}
 		setResult(s);
 	}
 
 	@Override
 	public void visit(AbsTypeDecl acceptor) {}
 
 	@Override
 	public void visit(AbsTypeName acceptor) {}
 
 	@Override
 	public void visit(AbsUnExpr acceptor) {
 		acceptor.expr.accept(this);
 		switch (acceptor.oper) {
 		case AbsUnExpr.ADD:
 			setResult(new ImcBINOP(ImcBINOP.ADD, new ImcCONST(0), (ImcExpr)getResult()));
 			break;
 		case AbsUnExpr.SUB:
 			setResult(new ImcBINOP(ImcBINOP.SUB, new ImcCONST(0), (ImcExpr)getResult()));
 			break;
 		case AbsUnExpr.NOT:
 			setResult(new ImcBINOP(ImcBINOP.EQU, new ImcCONST(0), (ImcExpr)getResult()));
 			break;
 		case AbsUnExpr.MEM:
 			setResult(((ImcMEM)getResult()).expr);
 			break;
 		case AbsUnExpr.VAL:
 			setResult(new ImcMEM((ImcExpr)getResult()));
 			break;
 		}		
 	}
 
 	@Override
 	public void visit(AbsValExprs acceptor) {
 		for (AbsValExpr expr: acceptor.exprs) {
 			expr.accept(this);
 		}
 	}
 
 	@Override
 	public void visit(AbsValName acceptor) {
 		AbsDecl d = SemDesc.getNameDecl(acceptor);
 		FrmFrame f = FrmDesc.getFrame(d);
 		FrmAccess a = FrmDesc.getAccess(d);
 		if(a instanceof FrmVarAccess) {
 			FrmVarAccess va = (FrmVarAccess)a;
 			if(noMem.getFirst()) {
 				setResult(new ImcNAME(va.label));
 			} else {
 				setResult(new ImcMEM(new ImcNAME(va.label)));
 			}
 		}
 		if(a instanceof FrmArgAccess) {
 			FrmArgAccess aa = (FrmArgAccess)a;
 			ImcExpr t = new ImcTEMP(curFrame.FP);
 			for(int i=0; i<curFrame.level - aa.frame.level; i++)
 				t = new ImcMEM(t);
 			if(noMem.getFirst()) {
 				setResult(new ImcBINOP(ImcBINOP.ADD, t, new ImcCONST(aa.offset)));
 			} else {
 				setResult(new ImcMEM(new ImcBINOP(ImcBINOP.ADD, t, new ImcCONST(aa.offset))));
 			}
 			SemType type = SemDesc.getActualType(aa.var);
 			if(type instanceof SemArrayType || type instanceof SemRecordType) {
 				setResult(new ImcMEM((ImcExpr)getResult()));
 			}
 				
 		}
 		if(a instanceof FrmLocAccess) {
 			FrmLocAccess la = (FrmLocAccess)a;
 			ImcExpr t = new ImcTEMP(curFrame.FP);
 			for(int i=0; i<curFrame.level - la.frame.level; i++)
 				t = new ImcMEM(t);
 			if(noMem.getFirst()) {
 				setResult(new ImcBINOP(ImcBINOP.ADD, t, new ImcCONST(la.offset)));
 			} else {
 				setResult(new ImcMEM(new ImcBINOP(ImcBINOP.ADD, t, new ImcCONST(la.offset))));
 			}
 		}
 		if(d instanceof AbsFunDecl) {
 			AbsFunDecl fd = (AbsFunDecl)d;
 			SemType t = SemDesc.getActualType(fd);
 			setResult(new ImcTEMP(f.RV));
 			if(t instanceof SemArrayType || t instanceof SemRecordType) {
 				setResult(new ImcMEM((ImcExpr)getResult()));
 			}
 		}
 		if(d instanceof AbsConstDecl) {
 			setResult(new ImcCONST(SemDesc.getActualConst(d)));
 		}
 	}
 
 	@Override
 	public void visit(AbsVarDecl acceptor) {}
 
 	@Override
 	public void visit(AbsWhileStmt acceptor) {
 		ImcSEQ s = new ImcSEQ();
 		
 		acceptor.cond.accept(this);
 		ImcExpr ce = (ImcExpr)getResult();
 		
 		ImcLABEL tl = new ImcLABEL(FrmLabel.newLabel());
 		ImcLABEL fl = new ImcLABEL(FrmLabel.newLabel());
 		ImcLABEL sl = new ImcLABEL(FrmLabel.newLabel());
 		
 		s.stmts.add(sl);
 		s.stmts.add(new ImcCJUMP(ce, tl.label, fl.label));
 		s.stmts.add(tl);
 		acceptor.stmt.accept(this);
 		s.stmts.add((ImcStmt)getResult());
 		s.stmts.add(new ImcJUMP(sl.label));
 		s.stmts.add(fl);
 		
 		setResult(s);
 	}
 
 }
