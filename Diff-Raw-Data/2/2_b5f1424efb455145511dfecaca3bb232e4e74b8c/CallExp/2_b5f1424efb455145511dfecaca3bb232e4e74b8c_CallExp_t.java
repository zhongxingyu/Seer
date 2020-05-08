 package descent.internal.compiler.parser;
 
 import static descent.internal.compiler.parser.LINK.LINKd;
 import static descent.internal.compiler.parser.STC.STClazy;
 import static descent.internal.compiler.parser.Scope.CSXany_ctor;
 import static descent.internal.compiler.parser.Scope.CSXlabel;
 import static descent.internal.compiler.parser.Scope.CSXsuper_ctor;
 import static descent.internal.compiler.parser.Scope.CSXthis_ctor;
 import static descent.internal.compiler.parser.TOK.TOKcomma;
 import static descent.internal.compiler.parser.TOK.TOKdelegate;
 import static descent.internal.compiler.parser.TOK.TOKdot;
 import static descent.internal.compiler.parser.TOK.TOKdotexp;
 import static descent.internal.compiler.parser.TOK.TOKdottd;
 import static descent.internal.compiler.parser.TOK.TOKdotvar;
 import static descent.internal.compiler.parser.TOK.TOKimport;
 import static descent.internal.compiler.parser.TOK.TOKoverloadset;
 import static descent.internal.compiler.parser.TOK.TOKsuper;
 import static descent.internal.compiler.parser.TOK.TOKtemplate;
 import static descent.internal.compiler.parser.TOK.TOKthis;
 import static descent.internal.compiler.parser.TOK.TOKtype;
 import static descent.internal.compiler.parser.TOK.TOKvar;
 import static descent.internal.compiler.parser.TY.Taarray;
 import static descent.internal.compiler.parser.TY.Tarray;
 import static descent.internal.compiler.parser.TY.Tclass;
 import static descent.internal.compiler.parser.TY.Tdelegate;
 import static descent.internal.compiler.parser.TY.Tfunction;
 import static descent.internal.compiler.parser.TY.Tpointer;
 import static descent.internal.compiler.parser.TY.Tsarray;
 import static descent.internal.compiler.parser.TY.Tstruct;
 import static descent.internal.compiler.parser.TY.Tvoid;
 import melnorme.miscutil.tree.TreeVisitor;
 
 import org.eclipse.core.runtime.Assert;
 
 import descent.core.compiler.IProblem;
 import descent.internal.compiler.parser.ast.IASTVisitor;
 
 
 public class CallExp extends UnaExp {
 
 	public Expressions arguments, sourceArguments;
 
 	public CallExp(char[] filename, int lineNumber, Expression e) {
 		super(filename, lineNumber, TOK.TOKcall, e);
 		this.arguments = null;
 	}
 
 	public CallExp(char[] filename, int lineNumber, Expression e, Expression earg1) {
 		super(filename, lineNumber, TOK.TOKcall, e);
 		this.arguments = new Expressions(1);
 		this.arguments.add(earg1);
 		this.sourceArguments = new Expressions(1);
 		this.sourceArguments.add(earg1);
 	}
 
 	public CallExp(char[] filename, int lineNumber, Expression e, Expression earg1, Expression earg2) {
 		super(filename, lineNumber, TOK.TOKcall, e);
 		this.arguments = new Expressions(2);
 		this.arguments.add(earg1);
 		this.arguments.add(earg2);
 		this.sourceArguments = new Expressions(2);
 		this.sourceArguments.add(earg1);
 		this.sourceArguments.add(earg2);
 	}
 
 	public CallExp(char[] filename, int lineNumber, Expression e, Expressions exps) {
 		super(filename, lineNumber, TOK.TOKcall, e);
 		this.arguments = exps;
 		if (exps != null) {
 			this.sourceArguments = new Expressions(arguments);
 		}
 	}
 
 	@Override
 	public void accept0(IASTVisitor visitor) {
 		boolean children = visitor.visit(this);
 		if (children) {
 			TreeVisitor.acceptChildren(visitor, sourceE1);
 			TreeVisitor.acceptChildren(visitor, sourceArguments);
 		}
 		visitor.endVisit(this);
 	}
 	
 	@Override
 	public boolean canThrow(SemanticContext context) {
 	    if (e1.canThrow(context))
 			return true;
 
 		/*
 		 * If any of the arguments can throw, then this expression can throw
 		 */
 		for (int i = 0; i < size(arguments); i++) {
 			Expression e = (Expression) arguments.get(i);
 
 			if (context.isD1()) {
 				if (e.canThrow(context))
 					return true;
 			} else {
 				if (e != null && e.canThrow(context))
 					return true;
 			}
 		}
 		
 		if (!context.isD1()) {
 		    if (context.global.errors != 0 && null == e1.type)
 		    	return false;			// error recovery
 		}
 
 		/*
 		 * If calling a function or delegate that is typed as nothrow, then this
 		 * expression cannot throw. Note that pure functions can throw.
 		 */
 		Type t = e1.type.toBasetype(context);
 		if (t.ty == Tfunction && ((TypeFunction) t).isnothrow)
 			return false;
 		if (t.ty == Tdelegate
 				&& ((TypeFunction) ((TypeDelegate) t).next).isnothrow)
 			return false;
 
 		return true;
 	}
 
 	@Override
 	public int checkSideEffect(int flag, SemanticContext context) {
 		if (context.isD2()) {
 			if (flag != 2)
 				return 1;
 
 			if (e1.checkSideEffect(2, context) != 0)
 				return 1;
 
 			/*
 			 * If any of the arguments have side effects, this expression does
 			 */
 			for (int i = 0; i < size(arguments); i++) {
 				Expression e = (Expression) arguments.get(i);
 
 				if (e.checkSideEffect(2, context) != 0)
 					return 1;
 			}
 
 			/*
 			 * If calling a function or delegate that is typed as pure, then
 			 * this expression has no side effects.
 			 */
 			Type t = e1.type.toBasetype(context);
 			if (t.ty == Tfunction && ((TypeFunction) t).ispure)
 				return 0;
 			if (t.ty == Tdelegate
 					&& ((TypeFunction) ((TypeDelegate) t).next).ispure)
 				return 0;
 
 		}
 		return 1;
 	}
 
 	@Override
 	public int getNodeType() {
 		return CALL_EXP;
 	}
 
 	@Override
 	public Expression interpret(InterState istate, SemanticContext context) {
 		Expression e = EXP_CANT_INTERPRET;
 
 		if (e1.op == TOKvar) {
 			FuncDeclaration fd = ((VarExp) e1).var.isFuncDeclaration();
 			if (fd != null) {
 				boolean doInlineDup = true;
 				if (context.isD2()) {
 					doInlineDup = false;
 
 					BUILTIN b = fd.isBuiltin(context);
 					if (b != BUILTIN.BUILTINunknown) {
 						Expressions args = new Expressions(size(arguments));
 						args.setDim(size(arguments));
 						for (int i = 0; i < size(args); i++) {
 							Expression earg = (Expression) arguments.get(i);
 							earg = earg.interpret(istate, context);
 							if (earg == EXP_CANT_INTERPRET) {
 								return earg;
 							}
 							args.set(i, earg);
 						}
 						e = eval_builtin(b, args, context);
 						if (null == e) {
 							e = EXP_CANT_INTERPRET;
 						}
 					} else {
 						doInlineDup = true;
 					}
 				}
 				
 				// Inline .dup
 				if (doInlineDup) {
 					if (fd.ident != null
 							&& equals(fd.ident, Id.adDup)
 							&& arguments != null && arguments.size() == 2) {
 						e = arguments.get(1);
 						e = e.interpret(istate, context);
 						if (e != EXP_CANT_INTERPRET) {
 							e = expType(type, e, context);
 						}
 					} else {
 						context.startFunctionInterpret(this);
 						try {
 							Expression eresult = fd.interpret(istate, arguments,
 									context);
 							if (eresult != null) {
 								e = eresult;
 							} else if (fd.type.toBasetype(context).nextOf().ty == Tvoid && 0 == context.global.errors) {
 								e = EXP_VOID_INTERPRET;
 							} else {
 								if (istate.stackOverflow) {
 									if (context.acceptsErrors()) {
 										context.acceptProblem(Problem.newSemanticTypeError(IProblem.ExpressionLeadsToStackOverflowAtCompileTime, this, toChars(context)));
 									}
 								} else {
 									if (context.acceptsErrors()) {
 										context.acceptProblem(Problem.newSemanticTypeError(IProblem.ExpressionIsNotEvaluatableAtCompileTime, this, toChars(context)));
 									}
 								}
 							}
 						} finally {
 							context.endFunctionInterpret(this);
 						}
 					}
 				}
 			}
 		}
 		return e;
 	}
 	
 	@Override
 	public boolean isLvalue(SemanticContext context) {
 		if (context.isD1()) {
 		    if (type.toBasetype(context).ty == Tstruct)
 				return true;
 		}
 		Type tb = e1.type.toBasetype(context);
 		if (tb.ty == Tfunction && ((TypeFunction) tb).isref)
 			return true; // function returns a reference
 		return false;
 	}
 
 	@Override
 	public Expression optimize(int result, SemanticContext context) {
 		Expression e = this;
 
 		e1 = e1.optimize(result, context);
 		
 		boolean condition;
 		if (context.isD2()) {
 			condition = e1.op == TOKvar;
 		} else {
 			condition = e1.op == TOKvar && (result & WANTinterpret) != 0;
 		}
 		
 		if (condition) {
 			FuncDeclaration fd = ((VarExp) e1).var.isFuncDeclaration();
 			if (fd != null) {
 			    BUILTIN b = fd.isBuiltin(context);
 			    if (context.isD2() && b != BUILTIN.BUILTINunknown) {
 					e = eval_builtin(b, arguments, context);
 					if (null == e)	{		// failed
 					    e = this;		// evaluate at runtime
 					}
 			    } else {
 			    	context.startFunctionInterpret(this);
 			    	
 			    	try {
 						Expression eresult = fd.interpret(null, arguments, context);
 						if (eresult != null && eresult != EXP_VOID_INTERPRET) {
 							e = eresult;
 						} else if ((result & WANTinterpret) != 0) {
 							if (context.acceptsErrors()) {
 								context.acceptProblem(Problem.newSemanticTypeError(IProblem.ExpressionIsNotEvaluatableAtCompileTime, this, toChars(context)));
 							}
 						}
 			    	} finally {
 			    		context.endFunctionInterpret(this);
 			    	}
 			    }
 			}
 		}
 		
 		// Descent: for code evaluation
 		if (e != this) {
 			this.sourceE1.setEvaluatedExpression(e, context);
 		}
 		
 		e.copySourceRange(this);
 		return e;
 	}
 
 	@Override
 	public void scanForNestedRef(Scope sc, SemanticContext context) {
 		e1.scanForNestedRef(sc, context);
 		arrayExpressionScanForNestedRef(sc, arguments, context);
 	}
 
 	@Override
 	public Expression semantic(Scope sc, SemanticContext context) {
 		TypeFunction tf;
 		FuncDeclaration f = null;
 		//int i;
 		Type t1 = null;
 		int istemp;
 		Objects targsi = null;
 	    TemplateInstance tierror = null;
 
 		if (type != null) {
 			return this; // semantic() already run
 		}
 
 		if (e1.op == TOKdelegate) {
 			DelegateExp de = (DelegateExp) e1;
 
 			e1 = new DotVarExp(de.filename, de.lineNumber, de.e1, de.func);
 			return semantic(sc, context);
 		}
 
 		boolean gotoLagain = false;
 
 		/* Transform:
 		 *	array.id(args) into id(array,args)
 		 *	aa.remove(arg) into delete aa[arg]
 		 */
 		if (e1.op == TOKdot) {
 			// BUG: we should handle array.a.b.c.e(args) too
 
 			DotIdExp dotid = (DotIdExp) (e1);
 			dotid.e1 = dotid.e1.semantic(sc, context);
 			Assert.isNotNull(dotid.e1);
 			if (dotid.e1.type != null) {
 				TY e1ty = dotid.e1.type.toBasetype(context).ty;
 				if (e1ty == Taarray
 						&& equals(dotid.ident, Id.remove)) {
 					if (arguments == null || arguments.size() != 1) {
 						if (context.acceptsErrors()) {
 							context.acceptProblem(Problem.newSemanticTypeError(
 									IProblem.ExpectedKeyAsArgumentToRemove, this));
 						}
 						// goto Lagain;
 						gotoLagain = true;
 					}
 					if (!gotoLagain) {
 						Expression key = arguments.get(0);
 						key = key.semantic(sc, context);
 						key = resolveProperties(sc, key, context);
 						key.rvalue(context);
 
 						TypeAArray taa = (TypeAArray) dotid.e1.type
 								.toBasetype(context);
 						key = key.implicitCastTo(sc, taa.index, context);
 						if (context.isD1()) {
 							key = key.implicitCastTo(sc, taa.key, context);
 						}
 						return new RemoveExp(filename, lineNumber, dotid.e1, key);
 					}
 				} else if (e1ty == Tarray || e1ty == Tsarray || e1ty == Taarray) {
 					if (arguments == null) {
 						arguments = new Expressions(1);
 					}
 					arguments.shift(dotid.e1);
 					if (context.isD2()) {
 						e1 = new DotIdExp(dotid.filename, dotid.lineNumber, new IdentifierExp(dotid.filename, dotid.lineNumber, Id.empty), dotid.ident);
 					} else {
 						e1 = new IdentifierExp(dotid.filename, dotid.lineNumber, dotid.ident);
 					}
 				}
 			}
 		}
 		
 		/*
 		 * This recognizes: foo!(tiargs)(funcargs)
 		 */
 		if (e1.op == TOKimport && null == e1.type) {
 			ScopeExp se = (ScopeExp) e1;
 			TemplateInstance ti = se.sds.isTemplateInstance();
 			if (ti != null && 0 == ti.semanticdone) {
 				/*
 				 * Attempt to instantiate ti. If that works, go with it. If
 				 * not, go with partial explicit specialization.
 				 */
 				int errors = context.global.errors;
 				context.global.gag++;
 				ti.semantic(sc, context);
 				context.global.gag--;
 				if (errors != context.global.errors) {
 					context.global.errors = errors;
 					targsi = ti.tiargs;
 					tierror = ti; // for error reporting
 					e1 = new IdentifierExp(filename, lineNumber, ti.name);
 				}
 			}
 		}
 
 		/*
 		 * This recognizes: expr.foo!(tiargs)(funcargs)
 		 */
 		if (e1.op == TOK.TOKdotti && null == e1.type) {
 			DotTemplateInstanceExp se = (DotTemplateInstanceExp) e1;
 			TemplateInstance ti = se.ti;
 			if (0 == ti.semanticdone) {
 				/*
 				 * Attempt to instantiate ti. If that works, go with it. If
 				 * not, go with partial explicit specialization.
 				 */
 			    ti.semanticTiargs(sc, context);
 			    
 			    if (context.isD1()) {
 					Expression etmp;
 					int errors = context.global.errors;
 					context.global.gag++;
 					etmp = e1.semantic(sc, context);
 					context.global.gag--;
 					if (errors != context.global.errors) {
 						context.global.errors = errors;
 						targsi = ti.tiargs;
 						e1 = new DotIdExp(filename, lineNumber, se.e1, ti.name);
 					} else
 						e1 = etmp;
 			    } else {
 					Expression etmp = e1.trySemantic(sc, context);
 					if (etmp != null) {
 						e1 = etmp; // it worked
 					} else // didn't work
 					{
 						targsi = ti.tiargs;
 						tierror = ti; // for error reporting
 						e1 = new DotIdExp(filename, lineNumber, se.e1, ti.name);
 					}
 			    }
 			}
 		}
 
 		if (!gotoLagain) {
 			istemp = 0;
 		}
 
 		boolean loopLagain = true;
 		Lagain: while (loopLagain) {
 			loopLagain = false;
 			f = null;
 			if (e1.op == TOKthis || e1.op == TOKsuper) {
 				// semantic() run later for these
 			} else {
 				super.semantic(sc, context);
 
 				/* Look for e1 being a lazy parameter
 				 */
 				if (e1.op == TOKvar) {
 					VarExp ve = (VarExp) e1;
 
 					if ((ve.var.storage_class & STClazy) != 0) {
 						tf = new TypeFunction(null, ve.var.type, 0, LINKd);
 						TypeDelegate t = new TypeDelegate(tf);
 						ve.type = t.semantic(filename, lineNumber, sc, context);
 					}
 				}
 
 				if (e1.op == TOKimport) { // Perhaps this should be moved to ScopeExp.semantic()
 					ScopeExp se = (ScopeExp) e1;
 					e1 = new DsymbolExp(filename, lineNumber, se.sds);
 					e1 = e1.semantic(sc, context);
 				}
 				// patch for #540 by Oskar Linde
 				else if (e1.op == TOKdotexp) {
 					DotExp de = (DotExp) e1;
 
 					if (de.e2.op == TOKimport) { // This should *really* be moved to ScopeExp::semantic()
 						ScopeExp se = (ScopeExp) de.e2;
 						de.e2 = new DsymbolExp(filename, lineNumber, se.sds);
 						de.e2 = de.e2.semantic(sc, context);
 					}
 
 					if (de.e2.op == TOKtemplate) {
 						TemplateExp te = (TemplateExp) de.e2;
 						e1 = new DotTemplateExp(filename, lineNumber, de.e1, te.td);
 					}
 				}
 
 			}
 
 			if (e1.op == TOKcomma) {
 				CommaExp ce = (CommaExp) e1;
 
 				e1 = ce.e2;
 				e1.type = ce.type;
 				ce.e2 = this;
 				ce.type = null;
 				return ce.semantic(sc, context);
 			}
 
 			t1 = null;
 			if (e1.type != null) {
 				t1 = e1.type.toBasetype(context);
 			}
 
 			// Check for call operator overload
 			if (t1 != null) {
 				AggregateDeclaration ad;
 
 				if (t1.ty == Tstruct) {
 					ad = ((TypeStruct) t1).sym;
 					
 					if (!context.isD1()) {
 						// First look for constructor
 						if (ad.ctor(context) != null && arguments != null
 								&& arguments.size() > 0) {
 							// Create variable that will get constructed
 							IdentifierExp idtmp = context.uniqueId("__ctmp");
 							VarDeclaration tmp = new VarDeclaration(filename,
 									lineNumber, t1, idtmp, null);
 							Expression av = new DeclarationExp(filename,
 									lineNumber, tmp);
 							av = new CommaExp(filename, lineNumber, av,
 									new VarExp(filename, lineNumber, tmp));
 
 							Expression e;
 							CtorDeclaration cf = ad.ctor(context).isCtorDeclaration();
 							if (cf != null)
 								e = new DotVarExp(filename, lineNumber, av, cf,
 										true);
 							else {
 								TemplateDeclaration td = ad.ctor(context)
 										.isTemplateDeclaration();
 								e = new DotTemplateExp(filename, lineNumber,
 										av, td);
 							}
 							e = new CallExp(filename, lineNumber, e, arguments);
 							if (!context.STRUCTTHISREF()) {
 								/*
 								 * Constructors return a pointer to the instance
 								 */
 								e = new PtrExp(filename, lineNumber, e);
 							}
 							e = e.semantic(sc, context);
 							return e;
 						}
 					}
 					
 				    // No constructor, look for overload of opCall
 					Dsymbol opCall = search_function(ad, Id.call, context);
 					if (opCall != null) {
 						
 						// Descent: for binding resolution
 						sourceE1.setResolvedSymbol(opCall, context);
 						
 						// goto L1;	
 						return semantic_L1(sc, context);
 					}
 					
 					if (e1.op != TOKtype) {
 						if (context.acceptsErrors()) {
 							context.acceptProblem(Problem.newSemanticTypeError(IProblem.KindSymbolDoesNotOverload, this, ad.kind(), ad.toChars(context)));
 						}
 					}
 					
 					/* It's a struct literal
 					 */
 					Expression e = new StructLiteralExp(filename, lineNumber,
 							(StructDeclaration) ad, arguments);
 					e.copySourceRange(this);
 					e = e.semantic(sc, context);
 					e.copySourceRange(this);
 					e.type = e1.type; // in case e1.type was a typedef
 					return e;
 				} else if (t1.ty == Tclass) {
 					ad = ((TypeClass) t1).sym;
 					// goto L1;
 					return semantic_L1(sc, context);
 				}
 			}
 
 			arrayExpressionSemantic(arguments, sc, context);
 			preFunctionArguments(filename, lineNumber, sc, arguments, context);
 
 			if (e1.op == TOKdotvar && t1.ty == Tfunction || e1.op == TOKdottd) {
 				DotVarExp dve = null;
 				DotTemplateExp dte = null;
 				AggregateDeclaration ad;
 				UnaExp ue = (UnaExp) (e1);
 
 				if (e1.op == TOKdotvar) { // Do overload resolution
 					dve = (DotVarExp) (e1);
 
 					f = dve.var.isFuncDeclaration();
 					Assert.isNotNull(f);
 					
 					f = f.overloadResolve(filename, lineNumber, ue.e1, arguments, context, this);
 					
 					// Descent: for binding resolution
 					if (this.sourceE1 != null) {
 						this.sourceE1.setResolvedSymbol(f, context);
 					}
 
 					ad = f.toParent().isAggregateDeclaration();
 				} else {
 					dte = (DotTemplateExp) (e1);
 					TemplateDeclaration td = dte.td;
 					Assert.isNotNull(td);
 					if (arguments == null) {
 						// Should fix deduce() so it works on null argument
 						arguments = new Expressions(0);
 					}
 					f = td.deduceFunctionTemplate(sc, filename, lineNumber, targsi, ue.e1, arguments, context);
 					if (f == null) {
 						type = Type.terror;
 						return this;
 					}
 					ad = td.toParent().isAggregateDeclaration();
 				}
 				
 				if (context.isD2()) {
 					/* Now that we have the right function f, we need to get the
 					 * right 'this' pointer if f is in an outer class, but our
 					 * existing 'this' pointer is in an inner class.
 					 * This code is analogous to that used for variables
 					 * in DotVarExp::semantic().
 					 */
 				  // L10:
 					boolean repeat = true;
 					while(repeat) {
 						repeat = false;
 
 						Type t = ue.e1.type.toBasetype(context);
 						if (f.needThis()
 								&& ad != null
 								&& !(t.ty == Tpointer
 										&& ((TypePointer) t).next.ty == Tstruct && ((TypeStruct) ((TypePointer) t).next).sym == ad)
 								&& !(t.ty == Tstruct && ((TypeStruct) t).sym == ad)) {
 							ClassDeclaration cd = ad.isClassDeclaration();
 							ClassDeclaration tcd = t.isClassHandle();
 
 							if (null == cd
 									|| null == tcd
 									|| !(tcd == cd || cd.isBaseOf(tcd, null,
 											context))) {
 								if (tcd != null && tcd.isNested()) { 
 									// Try again with outer scope
 									ue.e1 = new DotVarExp(filename, lineNumber, ue.e1, tcd.vthis);
 									ue.e1 = ue.e1.semantic(sc, context);
 									// goto L10;
 									repeat = true;
 									continue;
 								}
 								if (context.acceptsErrors()) {
 									context
 											.acceptProblem(Problem
 													.newSemanticTypeError(
 															IProblem.ThisForSymbolNeedsToBeType,
 															this,
 															f.toChars(context),
 															ad.toChars(context),
 															t.toChars(context)));
 								}
 							}
 						}
 					}
 				} else {
 					if (f.needThis()) {
 					    ue.e1 = getRightThis(filename, lineNumber, sc, ad, ue.e1, f, context);
 					}
 					
 					/* Cannot call public functions from inside invariant
 					 * (because then the invariant would have infinite recursion)
 					 */
 					if (sc.func != null && sc.func.isInvariantDeclaration() != null &&
 					    ue.e1.op == TOKthis &&
 					    f.addPostInvariant(context)
 					   ) {
 						if (context.acceptsErrors()) {
 							context.acceptProblem(Problem.newSemanticTypeError(IProblem.CannotCallPublicExportFunctionFromImmutable, this, f.toChars(context)));
 						}
 					}
 				}
 
 				checkDeprecated(sc, f, context);
 				if (context.isD2()) {
 					checkPurity(sc, f, context);
 				}
 				accessCheck(sc, ue.e1, f, context);
 				if (!f.needThis()) {
 					VarExp ve = new VarExp(filename, lineNumber, f);
 					e1 = new CommaExp(filename, lineNumber, ue.e1, ve);
 					e1.type = f.type;
 				} else {
 					if (e1.op == TOKdotvar) {
 						dve.var = f;
 					} else {
 						e1 = new DotVarExp(filename, lineNumber, dte.e1, f);
 					}
 					e1.type = f.type;
 					
 					if (context.isD2()) {
 						// Const member function can take
 						// const/invariant/mutable this
 						if (!(f.type.isConst())) {
 							// Check for const/invariant compatibility
 							Type tthis = ue.e1.type.toBasetype(context);
 							if (tthis.ty == Tpointer)
 								tthis = tthis.nextOf().toBasetype(context);
 							if (f.type.isInvariant()) {
 								if (tthis.mod != Type.MODinvariant) {
 									if (context.acceptsErrors()) {
 										context.acceptProblem(Problem.newSemanticTypeError(IProblem.SymbolCanOnlyBeCalledOnAnInvariantObject, this, e1.toChars(context)));
 									}
 								}
 							} else {
 								if (tthis.mod != 0) {
 									if (context.acceptsErrors()) {
 										context.acceptProblem(Problem.newSemanticTypeError(IProblem.SymbolCanOnlyBeCalledOnAMutableObject, this, e1.toChars(context), tthis.toChars(context)));
 									}
 								}
 							}
 
 							/*
 							 * Cannot call mutable method on a final struct
 							 */
 							if (tthis.ty == Tstruct && ue.e1.op == TOKvar) {
 								VarExp v = (VarExp) ue.e1;
 								if ((v.var.storage_class & STC.STCfinal) != 0) {
 									if (context.acceptsErrors()) {
 										context.acceptProblem(Problem.newSemanticTypeError(IProblem.CannotCallMutableMethodOnFinalStruct, this));
 									}
 								}
 							}
 						}
 					}
 
 					// See if we need to adjust the 'this' pointer
 					ad = f.isThis();
 					ClassDeclaration cd = ue.e1.type.isClassHandle();
 					if (ad != null && cd != null
 							&& ad.isClassDeclaration() != null && ad != cd
 							&& ue.e1.op != TOKsuper) {
 						ue.e1 = ue.e1.castTo(sc, ad.type, context); //new CastExp(filename, lineNumber, ue.e1, ad.type);
 						ue.e1 = ue.e1.semantic(sc, context);
 					}
 				}
 				t1 = e1.type;
 			} else if (e1.op == TOKsuper) {
 				// Base class constructor call
 				ClassDeclaration cd = null;
 
 				if (sc.func != null) {
 					cd = sc.func.toParent().isClassDeclaration();
 				}
 				if (cd == null || cd.baseClass == null
 						|| sc.func.isCtorDeclaration() == null) {
 					if (context.acceptsErrors()) {
 						context.acceptProblem(Problem.newSemanticTypeErrorLoc(IProblem.SuperClassConstructorCallMustBeInAConstructor, this));
 					}
 					type = Type.terror;
 					return this;
 				} else {
 					boolean condition;
 					if (context.isD1()) {
 						f = cd.baseClass.ctor(context);
 						condition = f != null;
 					} else {
 						condition = cd.baseClass.ctor(context) == null;
 					}
 					
 					if (condition) {
 						if (context.acceptsErrors()) {
 							context.acceptProblem(Problem.newSemanticTypeErrorLoc(IProblem.NoSuperClassConstructor, this, cd.baseClass.toChars(context)));
 						}
 						type = Type.terror;
 						return this;
 					} else {
 						if (0 == sc.intypeof) {
 							if (sc.noctor != 0 || (sc.callSuper & CSXlabel) != 0) {
 								if (context.acceptsErrors()) {
 									context.acceptProblem(Problem.newSemanticTypeErrorLoc(IProblem.ConstructorCallsNotAllowedInLoopsOrAfterLabels, this));
 								}
 							}
 							if ((sc.callSuper & (CSXsuper_ctor | CSXthis_ctor)) != 0) {
 								if (context.acceptsErrors()) {
 									context.acceptProblem(Problem.newSemanticTypeError(IProblem.MultipleConstructorCalls, this));
 								}
 							}
 							sc.callSuper |= CSXany_ctor | CSXsuper_ctor;
 						}
 
 						if (context.isD1()) {
 							f = f.overloadResolve(filename, lineNumber, null, arguments, context, this);
 						} else {
 							f = resolveFuncCall(sc, filename, lineNumber, cd.baseClass.ctor(context), null, null, arguments, 0, context);
 						}
 						
 						// Descent: for binding resolution
 						if (this.sourceE1 != null) {
 							this.sourceE1.setResolvedSymbol(f, context);
 						}
 						
 						checkDeprecated(sc, f, context);
 						
 						if (context.isD2()) {
 							checkPurity(sc, f, context);
 						}
 						
 						e1 = new DotVarExp(e1.filename, e1.lineNumber, e1, f);
 						e1 = e1.semantic(sc, context);
 						t1 = e1.type;
 					}
 				}
 			} else if (e1.op == TOKthis) {
 				// same class constructor call
 				AggregateDeclaration cd = null;
 				if (sc.func != null) {
 					if (context.isD1()) {
 						cd = sc.func.toParent().isClassDeclaration();
 					} else {
 						cd = sc.func.toParent().isAggregateDeclaration();
 					}
 				}
 				if (cd == null || sc.func.isCtorDeclaration() == null) {
 					if (context.acceptsErrors()) {
 						context.acceptProblem(Problem.newSemanticTypeError(IProblem.ClassConstructorCallMustBeInAConstructor, getLineNumber(), getErrorStart(), getErrorLength()));
 					}
 					type = Type.terror;
 					return this;
 				} else {
 					if (0 == sc.intypeof)
 				    {
 						if (sc.noctor != 0 || (sc.callSuper & CSXlabel) != 0) {
 							if (context.acceptsErrors()) {
 								context.acceptProblem(Problem.newSemanticTypeError(IProblem.ConstructorCallsNotAllowedInLoopsOrAfterLabels, getLineNumber(), getErrorStart(), getErrorLength()));
 							}
 						}
 						if ((sc.callSuper & (CSXsuper_ctor | CSXthis_ctor)) != 0) {
 							if (context.acceptsErrors()) {
 								context.acceptProblem(Problem.newSemanticTypeError(IProblem.MultipleConstructorCalls, this));
 							}
 						}
 						sc.callSuper |= CSXany_ctor | CSXthis_ctor;
 				    }
 
 					if (context.isD1()) {
 						f = cd.ctor(context);
 						f = f.overloadResolve(filename, lineNumber, null, arguments, context, this);
 					} else {
					    f = resolveFuncCall(sc, filename, lineNumber, cd.ctor(context), null, null, arguments, 0, context);
 					}
 					
 					// Descent: for binding resolution
 					if (this.sourceE1 != null) {
 						this.sourceE1.setResolvedSymbol(f, context);
 					}
 					
 					checkDeprecated(sc, f, context);
 					
 					if (context.isD2()) {
 						checkPurity(sc, f, context);
 					}
 					
 					e1 = new DotVarExp(e1.filename, e1.lineNumber, e1, f);
 					e1 = e1.semantic(sc, context);
 					t1 = e1.type;
 
 					// BUG: this should really be done by checking the static
 					// call graph
 					if (f == sc.func) {
 						if (context.acceptsErrors()) {
 							context.acceptProblem(Problem.newSemanticTypeError(
 									IProblem.CyclicConstructorCall, this, toChars(context)));
 						}
 					}
 				}
 			} else if (!context.isD1() && e1.op == TOKoverloadset) {
 				OverExp eo = (OverExp) e1;
 				FuncDeclaration f3 = null;
 				for (int i = 0; i < size(eo.vars.a); i++) {
 					Dsymbol s = (Dsymbol) eo.vars.a.get(i);
 					FuncDeclaration f2 = s.isFuncDeclaration();
 					if (f2 != null) {
 						f2 = f2.overloadResolve(filename, lineNumber, null,
 								arguments, 1, context, this); // SEMANTIC this
 						// is caller?
 					} else {
 						TemplateDeclaration td = s.isTemplateDeclaration();
 						f2 = td
 								.deduceFunctionTemplate(sc, filename,
 										lineNumber, targsi, null, arguments, 1,
 										context);
 					}
 					if (f2 != null) {
 						if (f3 != null)
 							/*
 							 * Error if match in more than one overload set,
 							 * even if one is a 'better' match than the other.
 							 */
 							ScopeDsymbol.multiplyDefined(filename, lineNumber,
 									f3, f2, context);
 						else
 							f3 = f2;
 					}
 				}
 				if (null == f3) {
 					/*
 					 * No overload matches, just set f and rely on error message
 					 * being generated later.
 					 */
 					f3 = (FuncDeclaration) eo.vars.a.get(0);
 				}
 				e1 = new VarExp(filename, lineNumber, f3);
 				// goto Lagain;
 				loopLagain = true;
 				continue;
 			} else if (t1 == null) {
 				if (context.acceptsErrors()) {
 					context.acceptProblem(Problem.newSemanticTypeError(
 							IProblem.FunctionExpectedBeforeCall, this, e1
 									.toChars(context)));
 				}
 				type = Type.terror;
 				return this;
 			} else if (t1.ty != Tfunction) {
 				if (t1.ty == Tdelegate) {
 					TypeDelegate td = (TypeDelegate) t1;
 					Assert.isTrue(td.next.ty == Tfunction);
 					tf = (TypeFunction) (td.next);
 					if (context.isD2()) {
 					    if (sc.func != null && sc.func.isPure() && !tf.ispure) {
 					    	if (context.acceptsErrors()) {
 					    		context.acceptProblem(Problem.newSemanticTypeError(
 										IProblem.PureFunctionCannotCallImpure, this, sc.func.toChars(context), "delegate", e1.toChars(context)));
 					    	}
 					    }
 					}
 					// goto Lcheckargs;
 					return semantic_Lcheckargs(sc, tf, f, context);
 				} else if (t1.ty == Tpointer && ((TypePointer)t1).next.ty == Tfunction) {
 					Expression e;
 
 					e = new PtrExp(filename, lineNumber, e1);
 					t1 = ((TypePointer)t1).next;
 					if (context.isD2()) {
 					    if (sc.func != null && sc.func.isPure() && !((TypeFunction)t1).ispure) {
 					    	if (context.acceptsErrors()) {
 					    		context.acceptProblem(Problem.newSemanticTypeError(
 										IProblem.PureFunctionCannotCallImpure, this, sc.func.toChars(context), "pointer", e1.toChars(context)));
 					    	}
 					    }
 					}
 					e.type = t1;
 					e1 = e;
 				} else if (e1.op == TOKtemplate) {
 					TemplateExp te = (TemplateExp) e1;
 					
 					// Descent: temporary adjust error position so errors doesn't
 					// appear inside templates, but always on the invocation site
 					context.startTemplateEvaluation(te.td, sc);
 					try {
 						f = te.td.deduceFunctionTemplate(sc, filename, lineNumber, targsi, null, arguments, context);
 					} finally {
 						context.endTemplateEvaluation(te.td, sc);
 					}
 					if (f == null) {
 						type = Type.terror;
 						return this;
 					}
 					if (f.needThis() && hasThis(sc) != null) {
 						// Supply an implicit 'this', as in
 						//	  this.ident
 
 						e1 = new DotTemplateExp(filename, lineNumber, (new ThisExp(filename, lineNumber))
 								.semantic(sc, context), te.td);
 						// goto Lagain;
 						loopLagain = true;
 						continue Lagain;
 					}
 
 					e1 = new VarExp(filename, lineNumber, f);
 					// goto Lagain;
 					loopLagain = true;
 					continue Lagain;
 				} else {
 					if (context.acceptsErrors()) {
 						context.acceptProblem(Problem.newSemanticTypeError(IProblem.FunctionExpectedBeforeCallNotSymbolOfType, this, e1.toChars(context), e1.type.toChars(context)));
 					}
 					type = Type.terror;
 					return this;
 				}
 			} else if (e1.op == TOKvar) {
 				// Do overload resolution
 				VarExp ve = (VarExp) e1;
 
 				f = ve.var.isFuncDeclaration();
 				Assert.isNotNull(f);
 
 				if (context.isD1()) {
 					// Look to see if f is really a function template
 //					if (false && istemp == 0 && f.parent != null) {
 //						TemplateInstance ti = f.parent.isTemplateInstance();
 //	
 //						if (ti != null
 //								&& (equals(ti.name, f.ident) || equals(ti.toAlias(context).ident, f.ident)) && ti.tempdecl != null) {
 //							/* This is so that one can refer to the enclosing
 //							 * template, even if it has the same name as a member
 //							 * of the template, if it has a !(arguments)
 //							 */
 //							TemplateDeclaration tempdecl = ti.tempdecl;
 //							if (tempdecl.overroot != null) {
 //								tempdecl = tempdecl.overroot; // then get the start
 //							}
 //							e1 = new TemplateExp(filename, lineNumber, tempdecl);
 //							istemp = 1;
 //							// goto Lagain;
 //							loopLagain = true;
 //							continue Lagain;
 //						}
 //					}
 					
 					f = f.overloadResolve(filename, lineNumber, null, arguments, context, this);
 				} else {
 					if (ve.hasOverloads) {
 					    f = f.overloadResolve(filename, lineNumber, null, arguments, context, this); // SEMANTIC this is caller?
 					}
 				}
 				
 				// Descent: for binding resolution
 				if (this.sourceE1 != null) {
 					this.sourceE1.setResolvedSymbol(f, context);
 				}
 				
 				checkDeprecated(sc, f, context);
 				
 				if (context.isD2()) {
 					checkPurity(sc, f, context);
 				}
 
 				if (f.needThis() && hasThis(sc) != null) {
 					// Supply an implicit 'this', as in
 					//	  this.ident
 
 					e1 = new DotVarExp(filename, lineNumber, new ThisExp(filename, lineNumber), f);
 					// goto Lagain;
 					loopLagain = true;
 					continue Lagain;
 				}
 
 				accessCheck(sc, null, f, context);
 
 				ve.var = f;
 				ve.type = f.type;
 				t1 = f.type;
 			}
 		}
 
 		Assert.isTrue(t1.ty == Tfunction);
 		tf = (TypeFunction) (t1);
 
 		// Lcheckargs:
 		return semantic_Lcheckargs(sc, tf, f, context);
 	}
 
 	private Expression semantic_L1(Scope sc, SemanticContext context) {
 		// overload of opCall, therefore it's a call
 		// Rewrite as e1.call(arguments)
 		Expression e = new DotIdExp(filename, lineNumber, e1, Id.call);
 		e = new CallExp(filename, lineNumber, e, arguments);
 		e.copySourceRange(this);
 		e = e.semantic(sc, context);
 		return e;
 	}
 
 	private Expression semantic_Lcheckargs(Scope sc, TypeFunction tf,
 			FuncDeclaration f, SemanticContext context) {
 		Assert.isTrue(tf.ty == Tfunction);
 		type = tf.next;
 
 		if (arguments == null) {
 			arguments = new Expressions(0);
 		}
 		functionArguments(filename, lineNumber, sc, tf, arguments, context);
 
 		if (context.isD1()) {
 			Assert.isNotNull(type);
 		} else {
 		    if (null == type) {
 		    	if (context.acceptsErrors()) {
 		    		context.acceptProblem(Problem.newSemanticTypeError(IProblem.ForwardReferenceToInferredReturnTypeOfFunctionCall, this, toChars(context)));
 		    	}
 		    	type = Type.terror;
 		    }
 		}
 
 		if (f != null && f.tintro() != null) {
 			Type t = type;
 			int[] offset = { 0 };
 			tf = (TypeFunction) f.tintro;
 
 			if (tf.next.isBaseOf(t, offset, context) && offset[0] != 0) {
 				type = tf.next;
 				return castTo(sc, t, context);
 			}
 		}
 
 		return this;
 	}
 
 	@Override
 	public Expression syntaxCopy(SemanticContext context) {
 		Expression e = context.newCallExp(filename, lineNumber, e1.syntaxCopy(context), arraySyntaxCopy(arguments, context));
 		e.copySourceRange(this);
 		return e;
 	}
 
 	@Override
 	public void toCBuffer(OutBuffer buf, HdrGenState hgs,
 			SemanticContext context) {
 		expToCBuffer(buf, hgs, e1, op.precedence, context);
 		buf.writeByte('(');
 		argsToCBuffer(buf, arguments, hgs, context);
 		buf.writeByte(')');
 	}
 
 	@Override
 	public Expression toLvalue(Scope sc, Expression e, SemanticContext context) {
 		if (context.isD1()) {
 			if (type.toBasetype(context).ty == Tstruct) {
 				return this;
 			} else {
 				return super.toLvalue(sc, e, context);
 			}
 		} else {
 			if (isLvalue(context))
 				return this;
 
 			return super.toLvalue(sc, e, context);
 		}
 	}
 	
 	@Override
 	public int getErrorStart() {
 		return e1.getErrorStart();
 	}
 	
 	@Override
 	public int getErrorLength() {
 		return e1.getErrorLength();
 	}
 	
 	@Override
 	public void setResolvedExpression(Expression exp, SemanticContext context) {
 		this.sourceE1.setResolvedExpression(exp, context);
 	}
 	
 	@Override
 	public void setEvaluatedExpression(Expression exp, SemanticContext context) {
 		this.sourceE1.setEvaluatedExpression(exp, context);
 	}
 
 }
