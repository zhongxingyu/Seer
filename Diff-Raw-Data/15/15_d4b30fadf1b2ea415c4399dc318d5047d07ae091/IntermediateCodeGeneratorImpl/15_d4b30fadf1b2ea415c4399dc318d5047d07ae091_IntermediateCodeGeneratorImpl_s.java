 package de.peeeq.pscript.intermediateLang;
 
 import java.util.List;
 
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.resource.Resource;
 
 import com.google.common.base.Function;
 import com.google.inject.Inject;
 
 import de.peeeq.immutablecollections.ImmutableList;
 import de.peeeq.pscript.attributes.AttrExprType;
 import de.peeeq.pscript.attributes.AttrTypeExprType;
 import de.peeeq.pscript.attributes.AttrVarDefType;
 import de.peeeq.pscript.attributes.infrastructure.AttributeManager;
 import de.peeeq.pscript.pscript.ClassDef;
 import de.peeeq.pscript.pscript.ElseBlock;
 import de.peeeq.pscript.pscript.Entity;
 import de.peeeq.pscript.pscript.Expr;
 import de.peeeq.pscript.pscript.ExprAdditive;
 import de.peeeq.pscript.pscript.ExprAnd;
 import de.peeeq.pscript.pscript.ExprAssignment;
 import de.peeeq.pscript.pscript.ExprBoolVal;
 import de.peeeq.pscript.pscript.ExprBuildinFunction;
 import de.peeeq.pscript.pscript.ExprComparison;
 import de.peeeq.pscript.pscript.ExprEquality;
 import de.peeeq.pscript.pscript.ExprFunctioncall;
 import de.peeeq.pscript.pscript.ExprIdentifier;
 import de.peeeq.pscript.pscript.ExprIntVal;
 import de.peeeq.pscript.pscript.ExprList;
 import de.peeeq.pscript.pscript.ExprMember;
 import de.peeeq.pscript.pscript.ExprMult;
 import de.peeeq.pscript.pscript.ExprNot;
 import de.peeeq.pscript.pscript.ExprNumVal;
 import de.peeeq.pscript.pscript.ExprOr;
 import de.peeeq.pscript.pscript.ExprSign;
 import de.peeeq.pscript.pscript.ExprStrval;
 import de.peeeq.pscript.pscript.FuncDef;
 import de.peeeq.pscript.pscript.InitBlock;
 import de.peeeq.pscript.pscript.NativeType;
 import de.peeeq.pscript.pscript.OpAdditive;
 import de.peeeq.pscript.pscript.OpAssign;
 import de.peeeq.pscript.pscript.OpComparison;
 import de.peeeq.pscript.pscript.OpDivReal;
 import de.peeeq.pscript.pscript.OpEquals;
 import de.peeeq.pscript.pscript.OpGreater;
 import de.peeeq.pscript.pscript.OpGreaterEq;
 import de.peeeq.pscript.pscript.OpLess;
 import de.peeeq.pscript.pscript.OpLessEq;
 import de.peeeq.pscript.pscript.OpMinus;
 import de.peeeq.pscript.pscript.OpMinusAssign;
 import de.peeeq.pscript.pscript.OpModInt;
 import de.peeeq.pscript.pscript.OpModReal;
 import de.peeeq.pscript.pscript.OpMult;
 import de.peeeq.pscript.pscript.OpMultiplicative;
 import de.peeeq.pscript.pscript.OpPlus;
 import de.peeeq.pscript.pscript.OpPlusAssign;
 import de.peeeq.pscript.pscript.OpUnequals;
 import de.peeeq.pscript.pscript.PackageDeclaration;
 import de.peeeq.pscript.pscript.Program;
 import de.peeeq.pscript.pscript.Statement;
 import de.peeeq.pscript.pscript.Statements;
 import de.peeeq.pscript.pscript.StmtExpr;
 import de.peeeq.pscript.pscript.StmtIf;
 import de.peeeq.pscript.pscript.StmtReturn;
 import de.peeeq.pscript.pscript.StmtWhile;
 import de.peeeq.pscript.pscript.TypeDef;
 import de.peeeq.pscript.pscript.VarDef;
 import de.peeeq.pscript.pscript.util.ElseBlockSwitch;
 import de.peeeq.pscript.pscript.util.EntitySwitchVoid;
 import de.peeeq.pscript.pscript.util.ExprSwitch;
 import de.peeeq.pscript.pscript.util.OpAdditiveSwitch;
 import de.peeeq.pscript.pscript.util.OpAssignmentSwitchVoid;
 import de.peeeq.pscript.pscript.util.OpComparisonSwitch;
 import de.peeeq.pscript.pscript.util.OpEqualitySwitch;
 import de.peeeq.pscript.pscript.util.OpMultiplicativeSwitch;
 import de.peeeq.pscript.pscript.util.StatementSwitch;
 import de.peeeq.pscript.pscript.util.TypeDefSwitchVoid;
 import de.peeeq.pscript.types.PScriptTypeBool;
 import de.peeeq.pscript.types.PScriptTypeVoid;
 import de.peeeq.pscript.types.PscriptType;
 import de.peeeq.pscript.utils.NotNullList;
 import de.peeeq.pscript.utils.Utils;
 
 public class IntermediateCodeGeneratorImpl implements IntermediateCodeGenerator  {
 
 	
 	@Inject
 	private AttributeManager attrManager;
 
 	private ILprog prog;
 
 	private int localVarNumberOffset;
 
 	@Override
 	public ILprog translateProg(Resource resource) {
 		
 		try {
 			EList<EObject> programs = resource.getContents();
 			translatePrograms(programs);
 			
 			
 		} catch (Throwable t) {
 			System.err.println("Unexpected Error while translating to intermediate language: ");
 			t.printStackTrace();
 		}		
 		
 		return prog; 
 	}
 
 	@Override
 	public ILprog translatePrograms(Iterable<? extends EObject> programs) {
 		prog = new ILprog(attrManager);
 		List<PackageDeclaration> packages = new NotNullList<PackageDeclaration>();
 		for (EObject p : programs) {
 			if (p instanceof Program) {
 				Program program = (Program) p;
 				for (PackageDeclaration pack : program.getPackages()) {
 					packages.add(pack);
 				}
 			} else {
 				throw new Error("A resource should only consist of programs, but " + p + " is of type " + p.getClass());
 			}
 		}
 		
 		
 		for (PackageDeclaration pack : packages) {
 			translatePackage(pack);
 		}
 		return prog;
 	}
 
 	private void translatePackage(final PackageDeclaration pack) {
 		for (Entity elem : pack.getElements()) {
 			new EntitySwitchVoid() {
 
 				@Override
 				public void caseInitBlock(InitBlock initBlock) {
 					String name = prog.getNewName(pack.getName() + "_init");
 					List<ILvar> params = new NotNullList<ILvar>();
 					List<ILvar> locals = collectLocals(initBlock.getBody());
 					List<ILStatement> body = translateStatements(initBlock.getBody(), locals);
 					
 					
 					PscriptType returntype = PScriptTypeVoid.instance();
 					ILfunction function = new ILfunction(name, params , returntype , locals, body);
 					prog.addFunction(function);
 				}
 
 				@Override
 				public void caseTypeDef(TypeDef typeDef) {
 					new TypeDefSwitchVoid() {
 						
 						@Override
 						public void caseNativeType(NativeType nativeType) {
 							prog.addNativeTranslation(nativeType.getName(), nativeType.getOrigName());							
 						}
 						
 						
 
 						@Override
 						public void caseClassDef(ClassDef classDef) {
 							// TODO translate classes
 						}
 					}.doSwitch(typeDef);
 					
 				}
 
 				@Override
 				public void caseFuncDef(FuncDef funcDef) {
 					ILfunction function = prog.getFunc(funcDef);
 					
 					List<ILvar> params = translateParams(funcDef.getParameters());
 					List<ILvar> locals = collectLocals(funcDef.getBody());
 					locals.addAll(params);
 					List<ILStatement> body = translateStatements(funcDef.getBody(), locals);
 					
 					PscriptType returnType;
 					if (funcDef.getType() != null) {
 						returnType = attrManager.getAttValue(AttrTypeExprType.class, funcDef.getType());
 					} else {
 						returnType = PScriptTypeVoid.instance();
 					}
 					function.set(params, returnType , locals, body);  
					prog.addFunction(function);
 				}
 
 				@Override
 				public void caseVarDef(VarDef varDef) {
 					// TODO add init code
 				}
 				
 			}.doSwitch(elem);
 		}		
 	}
 
 	protected List<ILvar> collectLocals(Statements body) {
 		ImmutableList<VarDef> varDefs = Utils.collectRec(body, VarDef.class);
 		
 		List<ILvar> vars = Utils.map(varDefs, new Function<VarDef, ILvar>() {
 
 			@Override
 			public ILvar apply(VarDef from) {
 				PscriptType type = attrManager.getAttValue(AttrVarDefType.class, from);
 				// we do not have to rename vars, as they are already unique 
 				//(or should they be renamed as the might shadow global variables?)
 				return new ILvar(from.getName(), type);
 			}
 		});
 		
 		return Utils.removeDuplicates(vars);	
 	}
 
 	protected List<ILStatement> translateStatements(Statements body, List<ILvar> locals) {
 		return translateStatements(body.getStatements(), locals);
 	}
 
 	private List<ILStatement> translateStatements(EList<Statement> statements, List<ILvar> locals) {
 		List<ILStatement> result = new NotNullList<ILStatement>();
 		for (Statement s : statements) {
 			result.addAll(translateStatement(s, locals));
 		}
 		return result ;
 	}
 
 	private List<ILStatement> translateStatement(Statement s, final List<ILvar> locals) {
 		final List<ILStatement> result = new NotNullList<ILStatement>();
 		return new StatementSwitch<List<ILStatement>> () {
 
 			@Override
 			public List<ILStatement> caseStmtWhile(StmtWhile stmtWhile) {
				ILvar resultVar = getNewLocalVar("whileCond", PScriptTypeBool.instance(), locals);
				List<ILStatement> cond = translateExpr(stmtWhile.getCond(), resultVar, locals);
 				List<ILStatement> whileBody = translateStatements(stmtWhile.getBody(), locals);
 				
 				List<ILStatement> loopBody = new NotNullList<ILStatement>();
 				loopBody.addAll(cond);
				loopBody.add(new ILexitwhen(resultVar));
 				loopBody.addAll(whileBody);
 				result.add(new ILloop(loopBody));	
 				
 				return result;
 			}
 
 			@Override
 			public List<ILStatement> caseStmtReturn(StmtReturn stmtReturn) {
 				PscriptType returnType = attrManager.getAttValue(AttrExprType.class, stmtReturn.getE());
 				ILvar resultVar = getNewLocalVar("returnResult", returnType , locals);
 				result.addAll(translateExpr(stmtReturn.getE(), resultVar, locals));
 				result.add(new ILreturn(resultVar));
 				return result;
 			}
 
 			@Override
 			public List<ILStatement> caseStmtIf(StmtIf stmtIf) {
				ILvar resultVar = getNewLocalVar("whileCond", PScriptTypeBool.instance(), locals);
 				List<ILStatement> cond = translateExpr(stmtIf.getCond(), resultVar, locals);
 				List<ILStatement> thenBlock = translateStatements(stmtIf.getThenBlock(), locals);
 				List<ILStatement> elseBlock = translateElseBlock(stmtIf.getElseBlock(), locals);
 				result.addAll(cond);
 				result.add(new ILif(resultVar, thenBlock, elseBlock));
 				return result;
 			}
 
 			@Override
 			public List<ILStatement> caseStmtExpr(StmtExpr stmtExpr) {
 				return translateExpr(stmtExpr.getE(), null, locals);
 			}
 
 			@Override
 			public List<ILStatement> caseVarDef(VarDef varDef) {
 				return translateAssignment(getLocalVar(varDef.getName(), locals), varDef.getE(), locals);
 			}
 			
 		}.doSwitch(s);
 	}
 
 	protected List<ILStatement> translateElseBlock(ElseBlock elseBlock,	final List<ILvar> locals) {
 		return new ElseBlockSwitch<List<ILStatement>>() {
 
 			@Override
 			public List<ILStatement> caseStatements(Statements statements) {
 				return translateStatements(statements, locals);
 			}
 		}.doSwitch(elseBlock);
 	}
 
 	protected List<ILStatement> translateAssignment(ILvar localVar, Expr e, List<ILvar> locals ) {
 		return translateExpr(e, localVar, locals);
 	}
 
 	protected ILvar getLocalVar(String name, List<ILvar> locals) {
 		for (ILvar v : locals) {
 			if (v.getName().equals(name)) {
 				return v;
 			}
 		}
 		throw new Error("Variable " + name + " not found.");
 	}
 
 	protected ILvar getNewLocalVar(String name, PscriptType typ, List<ILvar> locals) {
 		int counter = locals.size() + localVarNumberOffset;
 		while (varExists(locals, name + counter)) {
 			counter++;
 			localVarNumberOffset++;
 		}
 		ILvar v = new ILvar(name + counter, typ);
 		locals.add(v);
 		return v;
 	}
 
 	private boolean varExists(List<ILvar> locals, String name) {
 		for (ILvar v : locals) {
 			if (v.getName().equals(name)) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	protected List<ILStatement> translateExpr(final Expr e, final ILvar resultVar, final List<ILvar> locals) {
 		final List<ILStatement> result = new NotNullList<ILStatement>();
 		return new ExprSwitch<List<ILStatement>>() {
 
 
 			private List<ILStatement> translateBinary(Expr left, Iloperator op, Expr right) {
 				PscriptType leftType = attrManager.getAttValue(AttrExprType.class, left);
 				PscriptType rightType = attrManager.getAttValue(AttrExprType.class, right);				
 				ILvar leftResult = getNewLocalVar("tempL", leftType, locals);
 				ILvar rightResult = getNewLocalVar("tempR", rightType, locals);
 				result.addAll(translateExpr(left, leftResult, locals));
 				result.addAll(translateExpr(right, rightResult, locals));
 				result.add(new Ilbinary(resultVar, leftResult, op, rightResult));
 				return result;
 			}
 			
 			private List<ILStatement> translateUnary(Expr right, Iloperator op,
 					List<ILvar> locals) {
 				PscriptType rightType = attrManager.getAttValue(AttrExprType.class, right);
 				ILvar rightResult = getNewLocalVar("tempU", rightType , locals);
 				result.addAll(translateExpr(right, rightResult, locals));
 				result.add(new Ilunary(resultVar, op, rightResult));
 				return result;
 			}
 			
 			@Override
 			public List<ILStatement> caseExprEquality(ExprEquality exprEquality) {
 				Iloperator op = new OpEqualitySwitch<Iloperator>() {
 
 					@Override
 					public Iloperator caseOpUnequals(OpUnequals opUnequals) {
 						return Iloperator.UNEQUALITY;
 					}
 
 					@Override
 					public Iloperator caseOpEquals(OpEquals opEquals) {
 						return Iloperator.EQUALITY;
 					}
 				}.doSwitch(exprEquality.getOp());
 				
 				return translateBinary(exprEquality.getLeft(), op , exprEquality.getRight());
 			}
 
 			
 
 			@Override
 			public List<ILStatement> caseExprIdentifier(ExprIdentifier exprIdentifier) {
 				VarDef decl = exprIdentifier.getNameVal();
 				ILvar var = getVar(decl, locals);
 				result.add(new ILcopy(resultVar, var));
 				return result;
 			}
 
 			@Override
 			public List<ILStatement> caseExprAdditive(ExprAdditive exprAdditive) {
 				Iloperator op = translateOp(exprAdditive.getOp());
 				return translateBinary(exprAdditive.getLeft(), op , exprAdditive.getRight());
 			}
 
 			@Override
 			public List<ILStatement> caseExprBuildinFunction(ExprBuildinFunction exprBuildinFunction) {
 				ILvar[] paramVars = new ILvar[0];
 				if (exprBuildinFunction.getParameters() != null && exprBuildinFunction.getParameters().getParams() != null) {
 					EList<Expr> params = exprBuildinFunction.getParameters().getParams();
 					// translate params
 					paramVars = new ILvar[params.size()];
 					for (int i=0; i<params.size(); i++) {
 						PscriptType typ = attrManager.getAttValue(AttrExprType.class, params.get(i));
 						//						paramVars[i] = getLocalVar( exprBuildinFunction.getName() +  "_param" + i , locals);
 						paramVars[i] = getNewLocalVar( exprBuildinFunction.getName() +  "_param" + i, typ, locals);
 						result.addAll(translateExpr(params.get(i), paramVars[i], locals));
 					}
 					
 				}
 				result.add(new IlbuildinFunctionCall(resultVar, exprBuildinFunction.getName(), paramVars));
 				return result;
 			}
 
 			@Override
 			public List<ILStatement> caseExprSign(ExprSign exprSign) {
 				return translateUnary(exprSign.getRight(), translateOp(exprSign.getOp()), locals);
 			}
 
 			
 
 			@Override
 			public List<ILStatement> caseExprMember(ExprMember exprMember) {
 				// this is either a function call or a class variable access
 				if (exprMember.getRight() instanceof ExprFunctioncall) {
 					ExprFunctioncall fc = (ExprFunctioncall) exprMember.getRight();
 					ExprList argList = fc.getParameters();
 					List<Expr> args = new NotNullList<Expr>();
 					args.add(exprMember.getLeft());
 					if (argList != null && argList.getParams() != null) {
 						args.addAll(argList.getParams());
 					}
 					return translateFunctionCall(resultVar, fc.getNameVal(), args, locals);
 				}
 				if (exprMember.getRight() instanceof ExprIdentifier) {
 					// TODO class variable access
 					throw new Error("not implemented");
 				}
 				throw new Error("no other case possible? / not implemented");
 			}
 
 			@Override
 			public List<ILStatement> caseExprNumVal(ExprNumVal exprNumVal) {
 				result.add(new IlsetConst(resultVar, new ILconstNum(exprNumVal.getNumVal())));
 				return result;
 			}
 
 			@Override
 			public List<ILStatement> caseExprAssignment(final ExprAssignment exprAssignment) {
 				
 				new OpAssignmentSwitchVoid() {
 
 					@Override
 					public void caseOpPlusAssign(OpPlusAssign opPlusAssign) {
 						throw new Error("not implemented"); // TODO
 					}
 
 					@Override
 					public void caseOpAssign(OpAssign opAssign) {
 						Expr left = exprAssignment.getLeft();
 						if (left instanceof ExprIdentifier) {
 							ExprIdentifier id = (ExprIdentifier) left;
 							VarDef decl = id.getNameVal();
 							ILvar var = getVar(decl, locals);
 							result.addAll(translateExpr(exprAssignment.getRight(), var, locals));
 						}
 						// TODO other assignments
 					}
 
 					@Override
 					public void caseOpMinusAssign(OpMinusAssign opMinusAssign) {
 						throw new Error("not implemented"); // TODO
 					}
 					
 				}.doSwitch(exprAssignment.getOp());
 				// TODO different assignment operators
 				
 								
 				return result;
 			}
 
 			// Constants:
 			
 			@Override
 			public List<ILStatement> caseExprStrval(ExprStrval exprStrval) {
 				result.add(new IlsetConst(resultVar, new ILconstString(exprStrval.getStrVal())));
 				return result;
 			}
 			
 			@Override
 			public List<ILStatement> caseExprIntVal(ExprIntVal exprIntVal) {
 				result.add(new IlsetConst(resultVar, new ILconstInt(exprIntVal.getIntVal())));
 				return result;
 			}
 			
 			@Override
 			public List<ILStatement> caseExprBoolVal(ExprBoolVal exprBoolVal) {
 				result.add(new IlsetConst(resultVar, new ILconstBool(exprBoolVal.getBoolVal())));
 				return result;
 			}
 
 			@Override
 			public List<ILStatement> caseExprNot(ExprNot exprNot) {
 				return translateUnary(exprNot.getRight(), Iloperator.NOT , locals);
 			}
 
 			@Override
 			public List<ILStatement> caseExprOr(ExprOr exprOr) {
 				Expr left = exprOr.getLeft();
 				Expr right = exprOr.getRight();
 				result.addAll(translateExpr(left, resultVar, locals));
 				result.add(new ILif(resultVar, new NotNullList<ILStatement>(), translateExpr(right, resultVar, locals)));
 				return result;
 			}
 
 			@Override
 			public List<ILStatement> caseExprAnd(ExprAnd exprAnd) {
 				Expr left = exprAnd.getLeft();
 				Expr right = exprAnd.getRight();
 				result.addAll(translateExpr(left, resultVar, locals));
 				result.add(new ILif(resultVar, translateExpr(right, resultVar, locals), new NotNullList<ILStatement>()));
 				return result;
 			}
 
 			@Override
 			public List<ILStatement> caseExprMult(ExprMult exprMult) {
 				return translateBinary(exprMult.getLeft(), translateOp(exprMult.getOp()), exprMult.getRight());
 			}
 			
 			@Override
 			public List<ILStatement> caseExprComparison(ExprComparison exprComparison) {
 				return translateBinary(exprComparison.getLeft(), translateOp(exprComparison.getOp()), exprComparison.getRight());
 			}
 
 			@Override
 			public List<ILStatement> caseExprFunctioncall(
 					ExprFunctioncall exprFunctioncall) {
 				List<Expr> args = new NotNullList<Expr>();
 				if (exprFunctioncall.getParameters() != null) {
 					args = exprFunctioncall.getParameters().getParams();
 				}
 				return translateFunctionCall(resultVar, exprFunctioncall.getNameVal(), args , locals);
 			}
 
 			
 
 			
 
 			
 
 		}.doSwitch(e);
 	}
 
 	protected Iloperator translateOp(OpComparison op) {
 		return new OpComparisonSwitch<Iloperator>() {
 
 			@Override
 			public Iloperator caseOpLessEq(OpLessEq opLessEq) {
 				return Iloperator.LESS_EQ;
 			}
 
 			@Override
 			public Iloperator caseOpGreater(OpGreater opGreater) {
 				return Iloperator.GREATER;
 			}
 
 			@Override
 			public Iloperator caseOpGreaterEq(OpGreaterEq opGreaterEq) {
 				return Iloperator.GREATER_EQ;
 			}
 
 			@Override
 			public Iloperator caseOpLess(OpLess opLess) {
 				return Iloperator.LESS;
 			}
 			
 		}.doSwitch(op);
 	}
 
 	protected Iloperator translateOp(OpMultiplicative op) {
 		return new OpMultiplicativeSwitch<Iloperator>() {
 
 			@Override
 			public Iloperator caseOpMult(OpMult opMult) {
 				return Iloperator.MULT;
 			}
 
 			@Override
 			public Iloperator caseOpModInt(OpModInt opModInt) {
 				return Iloperator.MOD_INT;
 			}
 
 			@Override
 			public Iloperator caseOpDivReal(OpDivReal opDivReal) {
 				return Iloperator.DIV_REAL;
 			}
 
 			@Override
 			public Iloperator caseOpModReal(OpModReal opModReal) {
 				return Iloperator.MOD_REAL;
 			}
 		}.doSwitch(op);
 	}
 
 	protected Iloperator translateOp(OpAdditive op) {
 		return new OpAdditiveSwitch<Iloperator>() {
 
 			@Override
 			public Iloperator caseOpPlus(OpPlus opPlus) {
 				return Iloperator.PLUS;
 			}
 
 			@Override
 			public Iloperator caseOpMinus(OpMinus opMinus) {
 				return Iloperator.MINUS;
 			}
 			
 		}.doSwitch(op);
 	}
 
 	protected List<ILStatement> translateFunctionCall(ILvar resultVar, FuncDef func, List<Expr> args, List<ILvar> locals) {
 		List<ILStatement> result = new NotNullList<ILStatement>();
 		ILvar[] argumentVars = new ILvar[args.size()];
 		PscriptType[] argumentTypes = new PscriptType[args.size()];
 		for (int i=0; i<argumentVars.length; i++) {
 			Expr arg = args.get(i);
 			argumentTypes[i] = attrManager.getAttValue(AttrExprType.class, arg);
 			argumentVars[i] = getNewLocalVar(func.getName() + "_param" + i, argumentTypes[i], locals);
 			
 			result.addAll(translateExpr(arg, argumentVars[i], locals));
 		}
 		
 		String name = prog.getFuncDefName(func);
 		
 		result.add(new ILfunctionCall(resultVar, name, argumentTypes, argumentVars));
 		
 		return result;
 	}
 
 
 	protected ILvar getVar(VarDef decl, List<ILvar> locals) {
 		for (ILvar v : locals) {
 			if (v.getName().equals(decl.getName())) {
 				return v;
 			}
 		}
 		ILvar v = prog.getGlobalVarDef(decl);
 		if (v != null) {
 			return v;
 		}
 		throw new Error("Variable " + decl + " not found.");
 	}
 
 	protected List<ILvar> translateParams(EList<VarDef> parameters) {
 		return Utils.map(parameters, new Function<VarDef, ILvar>() {
 
 			@Override
 			public ILvar apply(VarDef from) {
 				PscriptType type = attrManager.getAttValue(AttrTypeExprType.class, from.getType()); 
 				return new ILvar(from.getName(), type);
 			}
 		});
 	}
 
 	
 }
