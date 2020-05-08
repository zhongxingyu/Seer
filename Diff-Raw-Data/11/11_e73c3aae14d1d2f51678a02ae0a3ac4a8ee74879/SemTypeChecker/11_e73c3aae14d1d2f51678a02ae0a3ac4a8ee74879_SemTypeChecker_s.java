 package compiler.semanal;
 
 import java.util.TreeSet;
 
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
 import compiler.semanal.type.SemArrayType;
 import compiler.semanal.type.SemAtomType;
 import compiler.semanal.type.SemPointerType;
 import compiler.semanal.type.SemRecordType;
 import compiler.semanal.type.SemSubprogramType;
 import compiler.semanal.type.SemType;
 
 public class SemTypeChecker implements AbsVisitor{
 
 	public boolean error = false;
 	private SemType typeInt = new SemAtomType(SemAtomType.INT);
 	private SemType typeBool = new SemAtomType(SemAtomType.BOOL);
 	//private SemType typeChar = new SemAtomType(SemAtomType.CHAR);
 	private SemType typeVoid = new SemAtomType(SemAtomType.VOID);
 
 	@Override
 	public void visit(AbsAlloc acceptor) {
 		SemDesc.setActualType(acceptor, typeInt);
 	}
 
 	@Override
 	public void visit(AbsArrayType acceptor) {
 		acceptor.loBound.accept(this); //tega naceloma ne rabmo k vemo da je notr int
 		acceptor.hiBound.accept(this);
 		acceptor.type.accept(this);
 		SemType type = SemDesc.getActualType(acceptor.type);
 		Integer loBound = SemDesc.getActualConst(acceptor.loBound);
 		Integer hiBound = SemDesc.getActualConst(acceptor.hiBound);
 		if (type != null){
 			SemDesc.setActualType(acceptor, new SemArrayType(type, loBound, hiBound));
 		}else{
 			noTypeError(acceptor.type.begLine, acceptor.type.begColumn);
 		}
 	}
 
 	@Override
 	public void visit(AbsAssignStmt acceptor) {
 		acceptor.dstExpr.accept(this);
 		acceptor.srcExpr.accept(this);
 		SemType ftype = SemDesc.getActualType(acceptor.dstExpr);
 		SemType stype = SemDesc.getActualType(acceptor.srcExpr);
 		if (ftype != null && stype != null){
 			if (ftype.coercesTo(stype)){
 				if (ftype instanceof SemAtomType || ftype instanceof SemPointerType){
 					SemDesc.setActualType(acceptor, ftype);
 				}else{
 					integerPointerTypeError(acceptor.begLine,acceptor.begColumn);
 				}
 			}else{
 				missmatchTypeError(acceptor.begLine,acceptor.begColumn);
 			}
 		}else{
 			noTypeError(acceptor.begLine,acceptor.begColumn);
 		}
 	}
 
 	@Override
 	public void visit(AbsAtomConst acceptor) {
 		SemAtomType type = new SemAtomType(acceptor.type);
 		SemDesc.setActualType(acceptor, type);
 	}
 
 	@Override
 	public void visit(AbsAtomType acceptor) {
 		SemAtomType type = new SemAtomType(acceptor.type);
 		SemDesc.setActualType(acceptor, type);
 	}
 
 	@Override
 	public void visit(AbsBinExpr acceptor) {
 		acceptor.fstExpr.accept(this);
 		acceptor.sndExpr.accept(this);
 		SemType ftype = SemDesc.getActualType(acceptor.fstExpr);
 		SemType stype = SemDesc.getActualType(acceptor.sndExpr);
 		if (ftype != null && (stype != null || acceptor.oper == AbsBinExpr.RECACCESS)){
 			switch (acceptor.oper) {
 			case AbsBinExpr.ADD:
 			case AbsBinExpr.SUB:
 			case AbsBinExpr.MUL:
 			case AbsBinExpr.DIV:
 				SemAtomType integer = new SemAtomType(AbsAtomType.INT);
 				if (ftype.coercesTo(stype)){
 					if (ftype.coercesTo(integer)){
 						SemDesc.setActualType(acceptor, ftype);
 					}else{
 						integerTypeError(acceptor.begLine,acceptor.begColumn);
 					}
 				}else{
 					missmatchTypeError(acceptor.begLine,acceptor.begColumn);
 				}
 				break;
 			case AbsBinExpr.EQU:
 			case AbsBinExpr.NEQ:
 			case AbsBinExpr.LTH:
 			case AbsBinExpr.GTH:
 			case AbsBinExpr.LEQ:
 			case AbsBinExpr.GEQ:
 				if (ftype.coercesTo(stype)){
 					if (ftype instanceof SemAtomType || ftype instanceof SemPointerType){
 						SemDesc.setActualType(acceptor, typeBool);
 					}else{
 						integerPointerTypeError(acceptor.begLine,acceptor.begColumn);
 					}
 				}else{
 					missmatchTypeError(acceptor.begLine,acceptor.begColumn);
 				}
 				break;
 			case AbsBinExpr.AND:
 			case AbsBinExpr.OR:
 				SemAtomType bool = new SemAtomType(AbsAtomType.BOOL);
 				if (ftype.coercesTo(stype)){
 					if (ftype.coercesTo(bool)){
 						SemDesc.setActualType(acceptor, ftype);
 					}else{
 						booleanTypeError(acceptor.begLine,acceptor.begColumn);
 					}
 				}else{
 					missmatchTypeError(acceptor.begLine,acceptor.begColumn);
 				}
 				break;
 			case AbsBinExpr.ARRACCESS:
 				if (ftype instanceof SemArrayType){
 					if (stype.coercesTo(typeInt)){
 						SemDesc.setActualType(acceptor, ((SemArrayType) ftype).type);
 					}else{
 						integerTypeError(acceptor.sndExpr.begLine, acceptor.sndExpr.begColumn);
 					}
 				}else{
 					arrayTypeError(acceptor.begLine,acceptor.begColumn);
 				}
 				break;
 			case AbsBinExpr.RECACCESS:
 				if (ftype instanceof SemRecordType){
 					SemRecordType record = (SemRecordType) ftype;
 
 					if (acceptor.sndExpr instanceof AbsValName){
 						AbsValName valname = (AbsValName) acceptor.sndExpr;
 						int i = 0;
 						AbsDeclName fn;
 						while((fn = record.getFieldName(i++))!= null){
 							if (fn.name.equals(valname.name)){
 								SemDesc.setActualType(acceptor, record.getFieldType(--i));
 								break;
 							}
 						}
 					}else{
 						System.out.println("some weird record errrorr");
 					}
 				}else{
 					recordTypeError(acceptor.begLine,acceptor.begColumn);
 				}
 				break;
 			}
 		}else{
 			noTypeError(acceptor.begLine,acceptor.begColumn);
 		}
 	}
 
 
 	@Override
 	public void visit(AbsBlockStmt acceptor) {
 		acceptor.stmts.accept(this);
 	}
 
 	@Override
 	public void visit(AbsCallExpr acceptor) {
 		acceptor.args.accept(this);
 		AbsDecl decl = SemDesc.getNameDecl(acceptor.name);
 		SemType type = SemDesc.getActualType(decl);
 		if (type instanceof SemSubprogramType){
 			SemSubprogramType thisType = new SemSubprogramType(((SemSubprogramType) type).getResultType());
 			for (AbsValExpr args: acceptor.args.exprs){
 				SemType argType = SemDesc.getActualType(args);
 				if (argType != null){
 					thisType.addParType(argType);
 				}else{
 					noTypeError(acceptor.begLine, acceptor.begColumn);
 				}
 			}
 			if (type.coercesTo(thisType)){
 				SemSubprogramType sub = (SemSubprogramType) type;
 				SemDesc.setActualType(acceptor, sub.getResultType());
 			}else{
 				argumentsTypeError(acceptor.begLine, acceptor.begColumn);
 			}
 		}else{
 			subprogramTypeError(acceptor.begLine, acceptor.begColumn);
 		}
 	}
 
 	@Override
 	public void visit(AbsConstDecl acceptor) {
 		acceptor.value.accept(this);
 		SemType type = SemDesc.getActualType(acceptor.value);
 		if (type != null){
 			SemDesc.setActualType(acceptor, type);
 		}
 	}
 
 	@Override
 	public void visit(AbsDeclName acceptor) {
 		System.out.println("Gandalf: the force is strong with you hairy!");
 	}
 
 	@Override
 	public void visit(AbsDecls acceptor) {
 		for (AbsDecl decl : acceptor.decls) {
 			decl.accept(this);
 		}
 	}
 
 	@Override
 	public void visit(AbsExprStmt acceptor) {
 		if (acceptor.expr instanceof AbsCallExpr){
 			AbsCallExpr call = (AbsCallExpr) acceptor.expr;
 			AbsDecl proc = SemDesc.getNameDecl(call.name);
 			if (proc instanceof AbsProcDecl){
 				acceptor.expr.accept(this);
 				SemType type = SemDesc.getActualType(acceptor.expr);
 				if (type != null){
 					SemDesc.setActualType(acceptor, type);
 				}else{
 					noTypeError(acceptor.expr.begLine, acceptor.expr.begColumn);
 				}
 			}else{
 				procedureTypeError(acceptor.expr.begLine, acceptor.expr.begColumn);
 			}
 		}else{
 			subprogramTypeError(acceptor.expr.begLine, acceptor.expr.begColumn);
 		}
 	}
 
 	@Override
 	public void visit(AbsForStmt acceptor) {
 		AbsDecl var = SemDesc.getNameDecl(acceptor.name);
 		SemType varType = SemDesc.getActualType(var);
 
 		acceptor.loBound.accept(this);
 		acceptor.hiBound.accept(this);
 
 		SemType lo = SemDesc.getActualType(acceptor.loBound);
 		SemType hi = SemDesc.getActualType(acceptor.hiBound);
 		if (varType == null){
 			noTypeError(acceptor.name.begLine, acceptor.name.begColumn);
 		}else if (!varType.coercesTo(typeInt)){
 			integerTypeError(acceptor.name.begLine, acceptor.name.begColumn);
 		}
 		if (lo== null){
 			noTypeError(acceptor.loBound.begLine, acceptor.loBound.begColumn);
 		}else if (!lo.coercesTo(typeInt)){
 			integerTypeError(acceptor.loBound.begLine, acceptor.loBound.begColumn);
 		}
 		if (hi == null){
 			noTypeError(acceptor.hiBound.begLine, acceptor.hiBound.begColumn);
 		}else if (!hi.coercesTo(typeInt)){
 			integerTypeError(acceptor.hiBound.begLine, acceptor.hiBound.begColumn);
 		}
 
 		acceptor.stmt.accept(this);
 	}
 
 	@Override
 	public void visit(AbsFunDecl acceptor) {
 		acceptor.pars.accept(this);
 		acceptor.type.accept(this);
 
 		SemType resultType = SemDesc.getActualType(acceptor.type);
 		SemSubprogramType type = new SemSubprogramType(resultType);
 
 		for (AbsDecl decl: acceptor.pars.decls){
 			SemType paramType = SemDesc.getActualType(decl);
 			if (paramType != null){
 				type.addParType(paramType);
 			}else{
 				noTypeError(decl.begLine, decl.begColumn);
 			}
 		}
 
 		SemDesc.setActualType(acceptor, type);
 		acceptor.decls.accept(this);
 		acceptor.stmt.accept(this);
 	}
 
 	@Override
 	public void visit(AbsIfStmt acceptor) {
 		acceptor.cond.accept(this);
 		acceptor.thenStmt.accept(this);
 		acceptor.elseStmt.accept(this);
 		SemType condType = SemDesc.getActualType(acceptor.cond);
 		if (condType == null){
 			noTypeError(acceptor.begLine, acceptor.begColumn);
 		}else if (!(condType instanceof SemAtomType) || 
 				((SemAtomType)condType).type != SemAtomType.BOOL){
 			booleanTypeError(acceptor.begLine, acceptor.begColumn);
 		}
 	}
 
 	@Override
 	public void visit(AbsNilConst acceptor) {
 		SemDesc.setActualType(acceptor, new SemPointerType(typeVoid));
 	}
 
 	@Override
 	public void visit(AbsPointerType acceptor) {
 		acceptor.type.accept(this);
 		SemType type = SemDesc.getActualType(acceptor.type);
 		if (type != null){
 			SemDesc.setActualType(acceptor, new SemPointerType(type));
 		}else{
 			noTypeError(acceptor.begLine, acceptor.begColumn);
 		}
 	}
 
 	@Override
 	public void visit(AbsProcDecl acceptor) {
 		acceptor.pars.accept(this);
 		SemSubprogramType type = new SemSubprogramType(typeVoid);
 
 		for (AbsDecl decl: acceptor.pars.decls){
 			SemType paramType = SemDesc.getActualType(decl);
 			if (paramType != null){
 				type.addParType(paramType);
 			}else{
 				noTypeError(decl.begLine, decl.begColumn);
 			}
 		}
 		SemDesc.setActualType(acceptor, type);
 
 		acceptor.decls.accept(this);
 		acceptor.stmt.accept(this);
 	}
 
 	@Override
 	public void visit(AbsProgram acceptor) {
 		acceptor.decls.accept(this);
 		acceptor.stmt.accept(this);
 	}
 
 	@Override
 	public void visit(AbsRecordType acceptor) {
 		acceptor.fields.accept(this);
 
 		SemRecordType type = new SemRecordType();
 		TreeSet<String> usedNames = new TreeSet<String>();
 		for (AbsDecl d : acceptor.fields.decls) {
 			if (d instanceof AbsTypeDecl){
 				AbsTypeDecl decl = (AbsTypeDecl) d;
 				SemType declType = SemDesc.getActualType(decl);
 				if (declType != null){
 					if (!usedNames.contains(decl.name.name)){
 						type.addField(decl.name, declType);
 						usedNames.add(decl.name.name);
 					}else{
 						recordNameError(decl.name.name, d.begLine, d.begColumn);
 					}
 
 				}else{
 					noTypeError(d.begLine, d.begColumn);
 				}
 			}else{
 				noTypeError(d.begLine, d.begColumn);
 			}
 		}
 
 		SemDesc.setActualType(acceptor, type);
 
 	}
 
 	@Override
 	public void visit(AbsStmts acceptor) {
 		for (AbsStmt stmt: acceptor.stmts){
 			stmt.accept(this);
 		}
 	}
 
 	@Override
 	public void visit(AbsTypeDecl acceptor) {
 		acceptor.type.accept(this);
 		SemType type = SemDesc.getActualType(acceptor.type);
 		if (type != null){
 			SemDesc.setActualType(acceptor, type);
 		}else{
 			noTypeError(acceptor.begLine, acceptor.begColumn);
 		}
 
 	}
 
 	@Override
 	public void visit(AbsTypeName acceptor) {
 		AbsDecl decl = SemDesc.getNameDecl(acceptor);
 		if (decl instanceof AbsTypeDecl){
 			SemType type = SemDesc.getActualType(decl);
 			if (type != null){
 				SemDesc.setActualType(acceptor, type);
 			}else{
 				noTypeError(acceptor.begLine, acceptor.begColumn);
 			}
 		}else{
 			noTypeError(acceptor.begLine, acceptor.begColumn);
 		}
 	}
 
 	@Override
 	public void visit(AbsUnExpr acceptor) {
 		acceptor.expr.accept(this);
 		SemType type = SemDesc.getActualType(acceptor.expr);
 		switch (acceptor.oper) {
 		case AbsUnExpr.ADD:
 		case AbsUnExpr.SUB:
 			if (type instanceof SemAtomType && ((SemAtomType) type).type == SemAtomType.INT){
 				SemDesc.setActualType(acceptor, type);
 			}else{
 				integerTypeError(acceptor.expr.begLine, acceptor.expr.begColumn);
 			}
 			break;
 		case AbsUnExpr.NOT:
 			if (type instanceof SemAtomType && ((SemAtomType) type).type == SemAtomType.BOOL){
 				SemDesc.setActualType(acceptor, type);
 			}else{
 				booleanTypeError(acceptor.expr.begLine, acceptor.expr.begColumn);
 			}
 			break;
 		case AbsUnExpr.MEM:
 			if (type != null){
 				SemPointerType ptr = new SemPointerType(type);
 				SemDesc.setActualType(acceptor, ptr);
 			}else{
 				noTypeError(acceptor.expr.begLine, acceptor.expr.begColumn);
 			}
 			break;
 		case AbsUnExpr.VAL:
 			if (type != null){
 				if (type instanceof SemPointerType){
 					SemDesc.setActualType(acceptor, ((SemPointerType) type).type);
 				}else{
 					pointerTypeError(acceptor.expr.begLine, acceptor.expr.begColumn);
 				}
 			}else{
 				noTypeError(acceptor.expr.begLine, acceptor.expr.begColumn);
 			}
 			break;
 		default:
 			System.out.println("something weird going on in visit(AbsUnExpr)");
 			break;
 		}
 	}
 
 	@Override
 	public void visit(AbsValExprs acceptor) {
 		for (AbsValExpr expr: acceptor.exprs){
 			expr.accept(this);
 		}
 	}
 
 	@Override
 	public void visit(AbsValName acceptor) {
 		AbsDecl decl = SemDesc.getNameDecl(acceptor);
 		SemType type = SemDesc.getActualType(decl);
 		if (type != null){
 			SemDesc.setActualType(acceptor, type);
 		}
 	}
 
 	@Override
 	public void visit(AbsVarDecl acceptor) {
 		acceptor.type.accept(this);
 		SemType type = SemDesc.getActualType(acceptor.type);
 		if (type != null){
 			SemDesc.setActualType(acceptor, type);
 		}else{
 			noTypeError(acceptor.begLine, acceptor.begColumn);
 		}
 	}
 
 	@Override
 	public void visit(AbsWhileStmt acceptor) {
 		acceptor.cond.accept(this);
 		SemType cond = SemDesc.getActualType(acceptor.cond);
 		if (cond == null){
 			noTypeError(acceptor.cond.begLine, acceptor.cond.begColumn);
 		}else if (!cond.coercesTo(typeBool)){
 			integerTypeError(acceptor.cond.begLine, acceptor.cond.begColumn);
 		}
 
 		acceptor.stmt.accept(this);
 	}
 
 	private void argumentsTypeError(int begLine, int begColumn) {
 		error = true;
 		System.out.println(String.format("type error: arguments don't match declaration (%d,%d)", begLine, begColumn));
 	}
 	private void subprogramTypeError(int begLine, int begColumn) {
 		error = true;
 		System.out.println(String.format("type error: expected subprogram (%d,%d)", begLine, begColumn));
 	}
 	private void procedureTypeError(int begLine, int begColumn) {
 		error = true;
 		System.out.println(String.format("type error: expected subprogram (%d,%d)", begLine, begColumn));
 	}
 	private void integerTypeError(int begLine, int begColumn) {
 		error = true;
 		System.out.println(String.format("type error: expected integer (%d,%d)", begLine, begColumn));
 	}
 	private void integerPointerTypeError(int begLine, int begColumn) {
 		error = true;
 		System.out.println(String.format("type error: expected integer or pointer (%d,%d)", begLine, begColumn));
 	}
 	private void pointerTypeError(int begLine, int begColumn) {
 		error = true;
 		System.out.println(String.format("type error: expected pointer (%d,%d)", begLine, begColumn));
 	}
 	private void booleanTypeError(int begLine, int begColumn) {
 		error = true;
 		System.out.println(String.format("type error: expected boolean (%d,%d)", begLine, begColumn));
 	}
 	private void arrayTypeError(int begLine, int begColumn) {
 		error = true;
 		System.out.println(String.format("type error: expected array (%d,%d)", begLine, begColumn));
 	}
 	private void recordTypeError(int begLine, int begColumn) {
 		error = true;
 		System.out.println(String.format("type error: expected record (%d,%d)", begLine, begColumn));
 	}
 	private void missmatchTypeError(int begLine, int begColumn) {
 		error = true;
 		System.out.println(String.format("type error: missmatch at (%d,%d)", begLine, begColumn));
 	}
 	private void noTypeError(int begLine, int begColumn) {
 		error = true;
 		System.out.println(String.format("can not resolve type at (%d,%d)", begLine, begColumn));
 	}
 	private void recordNameError(String name, int begLine, int begColumn) {
 		error = true;
 		System.out.println(String.format("record element '%s' already exists (%d,%d)",name, begLine, begColumn));
 	}
 
 }
