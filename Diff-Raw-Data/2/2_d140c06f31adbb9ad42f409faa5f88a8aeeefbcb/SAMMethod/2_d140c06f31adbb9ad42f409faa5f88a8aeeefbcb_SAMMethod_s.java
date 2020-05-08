 /////////////////////////////////////////////////////////////////////////
 //
 // © University of Southampton IT Innovation Centre, 2011
 //
 // Copyright in this library belongs to the University of Southampton
 // University Road, Highfield, Southampton, UK, SO17 1BJ
 //
 // This software may not be used, sold, licensed, transferred, copied
 // or reproduced in whole or in part in any manner or form or in or
 // on any media by any person other than in accordance with the terms
 // of the Licence Agreement supplied with the software, or otherwise
 // without the prior written consent of the copyright owners.
 //
 // This software is distributed WITHOUT ANY WARRANTY, without even the
 // implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 // PURPOSE, except where stated in the Licence Agreement supplied with
 // the software.
 //
 //	Created By :			Thomas Leonard
 //	Created Date :			2011-04-04
 //	Created for Project :		SERSCIS
 //
 /////////////////////////////////////////////////////////////////////////
 //
 //  License : GNU Lesser General Public License, version 2.1
 //
 /////////////////////////////////////////////////////////////////////////
 
 package eu.serscis;
 
 import java.util.HashSet;
 import java.util.Set;
 import eu.serscis.sam.node.*;
 import java.io.StringReader;
 import java.io.PushbackReader;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.io.FileWriter;
 import java.io.File;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.FileReader;
 import java.io.Reader;
 import java.io.IOException;
 import org.deri.iris.Configuration;
 import org.deri.iris.KnowledgeBaseFactory;
 import org.deri.iris.api.IKnowledgeBase;
 import org.deri.iris.api.basics.IPredicate;
 import org.deri.iris.api.basics.IQuery;
 import org.deri.iris.api.basics.IRule;
 import org.deri.iris.api.basics.ITuple;
 import org.deri.iris.api.basics.ILiteral;
 import org.deri.iris.api.terms.IVariable;
 import org.deri.iris.api.terms.ITerm;
 import org.deri.iris.storage.IRelation;
 import org.deri.iris.rules.IRuleSafetyProcessor;
 import org.deri.iris.RuleUnsafeException;
 import org.deri.iris.compiler.BuiltinRegister;
 import static org.deri.iris.factory.Factory.*;
 import eu.serscis.sam.lexer.Lexer;
 import eu.serscis.sam.parser.Parser;
 import eu.serscis.sam.parser.ParserException;
 import static eu.serscis.Constants.*;
 
 class SAMMethod {
 	private Set<String> locals = new HashSet<String>();
 	private SAMClass parent;
 	private String localPrefix;
 	private AMethod method;
 	private ITerm methodNameFull;
 	private int nextCallSite = 1;
 
 
 	public SAMMethod(SAMClass parent, AMethod m, ITerm methodNameFull) throws Exception {
 		this.parent = parent;
 		this.method = m;
 		this.methodNameFull = methodNameFull;
 		this.localPrefix = methodNameFull.getValue() + ".";
 	}
 
 	private String parsePattern(PPattern parsed) {
 		if (parsed instanceof ANamedPattern) {
 			return ((ANamedPattern) parsed).getName().getText();
 		} else {
 			return "*";
 		}
 	}
 
 	public void addDatalog() throws Exception {
 		ACode code = (ACode) method.getCode();
 
 		// mayAccept(type, param)
 		IRelation acceptRel = parent.model.getRelation(mayAcceptP);
 		AParams params = (AParams) method.getParams();
 		if (params != null) {
 			addParam(methodNameFull, acceptRel, params.getParam());
 
 			for (PParamsTail tail : params.getParamsTail()) {
 				AParam param2 = (AParam) ((AParamsTail) tail).getParam();
 				addParam(methodNameFull, acceptRel, param2);
 			}
 		}
 
 		processCode(code.getStatement());
 	}
 
 	private void processCode(List<PStatement> statements) throws Exception {
 		for (PStatement ps : statements) {
 			if (ps instanceof AAssignStatement) {
 				AAssignStatement s = (AAssignStatement) ps;
 
 				AAssign assign = (AAssign) s.getAssign();
 				PExpr expr = (PExpr) s.getExpr();
 
 				String callSite = methodNameFull.getValue() + "-" + nextCallSite;
 				nextCallSite++;
 
 				// hasCallSite(methodFull, callSite).
 				IRelation rel = parent.model.getRelation(hasCallSiteP);
 				rel.add(BASIC.createTuple(methodNameFull, TERM.createString(callSite)));
 
 				IPredicate valueP = null;
 
 				if (expr instanceof ACallExpr) {
 					ACallExpr callExpr = (ACallExpr) expr;
 
 					// mayCallObject(?Caller, ?CallerInvocation, ?CallSite, ?Value) :-
 					//	isA(?Caller, ?Type),
 					//	liveMethod(?Caller, ?CallerInvocation, method),
 					//	value(?Caller, ?CallerInvocation, ?TargetVar, ?Value).
 
 					ITuple tuple = BASIC.createTuple(
 							TERM.createVariable("Caller"),
 							TERM.createVariable("CallerInvocation"),
 							TERM.createString(callSite),
 							TERM.createVariable("Value"));
 
 					ILiteral head = BASIC.createLiteral(true, BASIC.createAtom(mayCallObjectP, tuple));
 
 					ILiteral isA = BASIC.createLiteral(true, BASIC.createAtom(isAP, BASIC.createTuple(
 								TERM.createVariable("Caller"),
 								TERM.createString(this.parent.name))));
 					ILiteral liveMethod = BASIC.createLiteral(true, BASIC.createAtom(liveMethodP, BASIC.createTuple(
 								TERM.createVariable("Caller"),
 								TERM.createVariable("CallerInvocation"),
 								methodNameFull)));
 
 					IRule rule = BASIC.createRule(makeList(head), makeList(isA, liveMethod, getValue(callExpr.getName())));
 					//System.out.println(rule);
 					parent.model.rules.add(rule);
 
 					addArgs(callSite, (AArgs) callExpr.getArgs());
 
 					// callsMethod(callSite, method)
 					String targetMethod = parsePattern(callExpr.getMethod());
 					if ("*".equals(targetMethod)) {
 						rel = parent.model.getRelation(callsAnyMethodP);
 						rel.add(BASIC.createTuple(TERM.createString(callSite)));
 					} else {
 						rel = parent.model.getRelation(callsMethodP);
 						rel.add(BASIC.createTuple(TERM.createString(callSite), TERM.createString(targetMethod)));
 					}
 
 					valueP = didGetP;
 				} else if (expr instanceof ANewExpr) {
 					ANewExpr newExpr = (ANewExpr) expr;
 
 					// mayCreate(classname, newType, var)
 					rel = parent.model.getRelation(mayCreateP);
 					String newType = ((AType) newExpr.getType()).getName().getText();
 					rel.add(BASIC.createTuple(TERM.createString(callSite),
 								  TERM.createString(newType)));
 
 					addArgs(callSite, (AArgs) newExpr.getArgs());
 
 					valueP = didCreateP;
 				} else if (expr instanceof ACopyExpr) {
 					// a = b
 					ACopyExpr copyExpr = (ACopyExpr) expr;
 
 					if (assign == null) {
 						throw new RuntimeException("Pointless var expression");
 					}
 
 					ILiteral isA = BASIC.createLiteral(true, BASIC.createAtom(isAP,
 								BASIC.createTuple(
 									TERM.createVariable("Caller"),
 									TERM.createString(parent.name))));
 
 					TName sourceVar = copyExpr.getName();
 					assignVar(assign, makeList(isA, getValue(sourceVar)));
 				} else {
 					throw new RuntimeException("Unknown expr type: " + expr);
 				}
 
 				if (assign != null && valueP != null) {
 					ITuple tuple = BASIC.createTuple(
 							TERM.createVariable("Caller"),
 							TERM.createVariable("CallerInvocation"),
 							TERM.createString(callSite),
 							TERM.createVariable("Value"));
 					ILiteral value = BASIC.createLiteral(true, BASIC.createAtom(valueP, tuple));
 
 					assignVar(assign, makeList(value));
 				}
 			} else if (ps instanceof ADeclStatement) {
 				ADeclStatement decl = (ADeclStatement) ps;
 				declareLocal(decl.getType(), decl.getName());
 			} else if (ps instanceof AReturnStatement) {
 				returnOrThrow(mayReturnP, methodNameFull, ((AReturnStatement) ps).getName());
 			} else if (ps instanceof AThrowStatement) {
 				returnOrThrow(mayThrowP, methodNameFull, ((AThrowStatement) ps).getName());
 			} else if (ps instanceof ATryStatement) {
 				ATryStatement ts = (ATryStatement) ps;
 				processCode(ts.getStatement());
 				for (PCatchBlock c : ts.getCatchBlock()) {
 					ACatchBlock cb = (ACatchBlock) c;
 					declareLocal(cb.getType(), cb.getName());
 
 					// local(?Object, ?Innovation, name, ?Exception) :-
 					//	didGetException(?Object, ?Invocation, ?CallSite, ?Exception),
 					//	hasCallSite(?Method, ?CallSite).
 					// (note: we currently catch all exceptions from this method, not just those in the try block)
 					ILiteral head = BASIC.createLiteral(true, BASIC.createAtom(localP,
 								BASIC.createTuple(
 									TERM.createVariable("Object"),
 									TERM.createVariable("Invocation"),
									TERM.createString(cb.getName().getText()),
 									TERM.createVariable("Exception"))));
 					ILiteral didGetException = BASIC.createLiteral(true, BASIC.createAtom(didGetExceptionP,
 								BASIC.createTuple(
 									TERM.createVariable("Object"),
 									TERM.createVariable("Invocation"),
 									TERM.createVariable("CallSite"),
 									TERM.createVariable("Exception"))));
 					ILiteral hasCallSite = BASIC.createLiteral(true, BASIC.createAtom(hasCallSiteP,
 								BASIC.createTuple(
 									methodNameFull,
 									TERM.createVariable("CallSite"))));
 					IRule rule = BASIC.createRule(makeList(head), makeList(didGetException, hasCallSite));
 					//System.out.println(rule);
 					parent.model.rules.add(rule);
 
 					processCode(cb.getStatement());
 				}
 			} else {
 				throw new RuntimeException("Unknown statement type: " + ps);
 			}
 		}
 	}
 
 	private void returnOrThrow(IPredicate pred, ITerm methodNameFull, TName name) throws ParserException {
 		// mayReturn(?Target, ?TargetInvocation, ?Method, ?Value) :-
 		//	isA(?Target, name),
 		//	liveMethod(?Target, ?TargetInvocation, ?Method),
 		//	(value)
 		ITuple tuple = BASIC.createTuple(
 				// XXX: badly named: should be Target, but getValue uses "Caller"
 				TERM.createVariable("Caller"),
 				TERM.createVariable("CallerInvocation"),
 				methodNameFull,
 				TERM.createVariable("Value"));
 
 		ILiteral head = BASIC.createLiteral(true, BASIC.createAtom(pred, tuple));
 
 		ILiteral isA = BASIC.createLiteral(true, BASIC.createAtom(isAP, BASIC.createTuple(
 					TERM.createVariable("Caller"),
 					TERM.createString(this.parent.name))));
 		ILiteral live = BASIC.createLiteral(true, BASIC.createAtom(liveMethodP, BASIC.createTuple(
 					TERM.createVariable("Caller"),
 					TERM.createVariable("CallerInvocation"),
 					methodNameFull)));
 
 		IRule rule = BASIC.createRule(makeList(head), makeList(isA, live, getValue(name)));
 		//System.out.println(rule);
 		parent.model.rules.add(rule);
 	}
 
 	private void declareLocal(AAssign assign) {
 		AType type = (AType) assign.getType();
 		if (type != null) {
 			declareLocal(type, assign.getName());
 		}
 	}
 
 	private void declareLocal(PType type, TName aName) {
 		String name = aName.getText();
 		if (locals.contains(name)) {
 			throw new RuntimeException("Duplicate definition of local " + name);
 		} else if (parent.fields.contains(name)) {
 			throw new RuntimeException("Local variable shadows field of same name: " + name);
 		} else {
 			locals.add(name);
 		}
 	}
 
 	/* Assign a local or field, as appropriate:
 	 *   local(?Caller, ?CallerInvocation, 'var', ?Value) :- body
 	 * or
 	 *   field(?Caller, 'var', ?Value) :- body
 	 */
 	private void assignVar(AAssign assign, List<ILiteral> body) {
 		ILiteral head;
 
 		declareLocal(assign);
 
 		String varName = assign.getName().getText();
 		if (locals.contains(varName)) {
 			ITuple tuple = BASIC.createTuple(TERM.createVariable("Caller"),
 							 TERM.createVariable("CallerInvocation"),
 							 TERM.createString(expandLocal(varName)),
 							 TERM.createVariable("Value"));
 			head = BASIC.createLiteral(true, BASIC.createAtom(localP, tuple));
 		} else if (parent.fields.contains(varName)) {
 			ITuple tuple = BASIC.createTuple(TERM.createVariable("Caller"),
 							 TERM.createString(varName),
 							 TERM.createVariable("Value"));
 			head = BASIC.createLiteral(true, BASIC.createAtom(fieldP, tuple));
 		} else {
 			throw new RuntimeException("Undeclared variable: " + varName);
 		}
 
 		IRule rule = BASIC.createRule(makeList(head), body);
 		//System.out.println(rule);
 		parent.model.rules.add(rule);
 	}
 
 	private void addParam(ITerm method, IRelation acceptRel, PParam param) {
 		String name = ((AParam) param).getName().getText();
 		acceptRel.add(BASIC.createTuple(method, TERM.createString(expandLocal(name))));
 
 		if (locals.contains(name)) {
 			throw new RuntimeException("Duplicate definition of local " + name);
 		} else if (parent.fields.contains(name)) {
 			throw new RuntimeException("Local variable shadows field of same name: " + name);
 		} else {
 			locals.add(name);
 		}
 	}
 
 	/* mayGet(?Target, ?TargetInvocation, ?Method, ?Pos, ?Value) :-
 	 * 	didCall(?Caller, ?CallerInvocation, ?CallSite, ?Target, ?TargetInvocation, ?Method),
 	 *	local|field,
 	 */
 	private void addArg(String callSite, int pos, PExpr expr) throws ParserException {
 		ILiteral didCall = BASIC.createLiteral(true, didCallP, BASIC.createTuple(
 					TERM.createVariable("Caller"),
 					TERM.createVariable("CallerInvocation"),
 					TERM.createString(callSite),
 					TERM.createVariable("Target"),
 					TERM.createVariable("TargetInvocation"),
 					TERM.createVariable("Method")
 					));
 
 		IRule rule;
 
 		if (expr instanceof AStringExpr) {
 			ILiteral head = BASIC.createLiteral(true, maySendP, BASIC.createTuple(
 						TERM.createVariable("Target"),
 						TERM.createVariable("TargetInvocation"),
 						TERM.createVariable("Method"),
 						CONCRETE.createInt(pos),
 						TERM.createString(getString(((AStringExpr) expr).getStringLiteral()))
 						));
 
 			rule = BASIC.createRule(makeList(head), makeList(didCall));
 		} else {
 			TName varName = ((ACopyExpr) expr).getName();
 			ILiteral head = BASIC.createLiteral(true, maySendP, BASIC.createTuple(
 						TERM.createVariable("Target"),
 						TERM.createVariable("TargetInvocation"),
 						TERM.createVariable("Method"),
 						CONCRETE.createInt(pos),
 						TERM.createVariable("Value")
 						));
 
 			rule = BASIC.createRule(makeList(head), makeList(didCall, getValue(varName)));
 		}
 
 		parent.model.rules.add(rule);
 		//System.out.println(rule);
 	}
 
 	private void addArgs(String callSite, AArgs args) throws ParserException {
 		if (args == null) {
 			return;
 		}
 		int pos = 0;
 		PExpr arg0 = args.getExpr();
 
 		addArg(callSite, pos, arg0);
 
 		for (PArgsTail tail : args.getArgsTail()) {
 			pos += 1;
 			PExpr arg = ((AArgsTail) tail).getExpr();
 			addArg(callSite, pos, arg);
 		}
 	}
 
 	/* Returns
 	 *   local(?Caller, ?CallerInvocation, 'var', ?Value)
 	 * or
 	 *   field(?Caller, 'var', ?Value)
 	 * or
 	 *   equals(?Caller, ?Value)  (for "this")
 	 * depending on whether varName refers to a local or a field.
 	 */
 	private ILiteral getValue(TName var) throws ParserException {
 		String sourceVar = var.getText();
 		if (locals.contains(sourceVar)) {
 			ITuple tuple = BASIC.createTuple(
 					TERM.createVariable("Caller"),
 					TERM.createVariable("CallerInvocation"),
 					TERM.createString(expandLocal(sourceVar)),
 					TERM.createVariable("Value"));
 			return BASIC.createLiteral(true, BASIC.createAtom(localP, tuple));
 		} else if (parent.fields.contains(sourceVar)) {
 			ITuple tuple = BASIC.createTuple(
 					TERM.createVariable("Caller"),
 					TERM.createString(sourceVar),
 					TERM.createVariable("Value"));
 			return BASIC.createLiteral(true, BASIC.createAtom(fieldP, tuple));
 		} else if (sourceVar.equals("this")) {
 			return BASIC.createLiteral(true, BUILTIN.createEqual(
 					TERM.createVariable("Caller"),
 					TERM.createVariable("Value")));
 		} else {
 			throw new ParserException(var, "Unknown variable " + sourceVar);
 		}
 	}
 
 	private String expandLocal(String local) {
 		return localPrefix + local;
 	}
 }
