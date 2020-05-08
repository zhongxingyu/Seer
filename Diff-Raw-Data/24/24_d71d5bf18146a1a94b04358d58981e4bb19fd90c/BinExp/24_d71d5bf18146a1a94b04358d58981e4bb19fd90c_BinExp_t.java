 package descent.internal.compiler.parser;
 
 import java.math.BigInteger;
 
 import org.eclipse.core.runtime.Assert;
 
 import descent.core.compiler.IProblem;
 import descent.internal.compiler.parser.Constfold.BinExp_fp;
 import descent.internal.compiler.parser.Constfold.BinExp_fp2;
 import static descent.internal.compiler.parser.Constfold.Equal;
 import static descent.internal.compiler.parser.Constfold.Index;
 
 import static descent.internal.compiler.parser.MATCH.MATCHnomatch;
 
 import static descent.internal.compiler.parser.TOK.TOKadd;
 import static descent.internal.compiler.parser.TOK.TOKaddass;
 import static descent.internal.compiler.parser.TOK.TOKandass;
 import static descent.internal.compiler.parser.TOK.TOKarrayliteral;
 import static descent.internal.compiler.parser.TOK.TOKassign;
 import static descent.internal.compiler.parser.TOK.TOKassocarrayliteral;
 import static descent.internal.compiler.parser.TOK.TOKcast;
 import static descent.internal.compiler.parser.TOK.TOKcatass;
 import static descent.internal.compiler.parser.TOK.TOKdivass;
 import static descent.internal.compiler.parser.TOK.TOKequal;
 import static descent.internal.compiler.parser.TOK.TOKge;
 import static descent.internal.compiler.parser.TOK.TOKgt;
 import static descent.internal.compiler.parser.TOK.TOKin;
 import static descent.internal.compiler.parser.TOK.TOKindex;
 import static descent.internal.compiler.parser.TOK.TOKle;
 import static descent.internal.compiler.parser.TOK.TOKlt;
 import static descent.internal.compiler.parser.TOK.TOKmin;
 import static descent.internal.compiler.parser.TOK.TOKminass;
 import static descent.internal.compiler.parser.TOK.TOKminusminus;
 import static descent.internal.compiler.parser.TOK.TOKmodass;
 import static descent.internal.compiler.parser.TOK.TOKmulass;
 import static descent.internal.compiler.parser.TOK.TOKnull;
 import static descent.internal.compiler.parser.TOK.TOKorass;
 import static descent.internal.compiler.parser.TOK.TOKplusplus;
 import static descent.internal.compiler.parser.TOK.TOKremove;
 import static descent.internal.compiler.parser.TOK.TOKshlass;
 import static descent.internal.compiler.parser.TOK.TOKshrass;
 import static descent.internal.compiler.parser.TOK.TOKstar;
 import static descent.internal.compiler.parser.TOK.TOKstring;
 import static descent.internal.compiler.parser.TOK.TOKstructliteral;
 import static descent.internal.compiler.parser.TOK.TOKsymoff;
 import static descent.internal.compiler.parser.TOK.TOKug;
 import static descent.internal.compiler.parser.TOK.TOKuge;
 import static descent.internal.compiler.parser.TOK.TOKul;
 import static descent.internal.compiler.parser.TOK.TOKule;
 import static descent.internal.compiler.parser.TOK.TOKushrass;
 import static descent.internal.compiler.parser.TOK.TOKvar;
 import static descent.internal.compiler.parser.TOK.TOKxorass;
 
 import static descent.internal.compiler.parser.TY.Tarray;
 import static descent.internal.compiler.parser.TY.Tbool;
 import static descent.internal.compiler.parser.TY.Tclass;
 import static descent.internal.compiler.parser.TY.Terror;
 import static descent.internal.compiler.parser.TY.Tpointer;
 import static descent.internal.compiler.parser.TY.Tsarray;
 import static descent.internal.compiler.parser.TY.Tstruct;
 import static descent.internal.compiler.parser.TY.Tvoid;
 
 // DMD 1.020
 public abstract class BinExp extends Expression {
 
 	public Expression e1, sourceE1;
 	public Expression e2, sourceE2;
 
 	public BinExp(Loc loc, TOK op, Expression e1, Expression e2) {
 		super(loc, op);
 		this.e1 = e1;
 		this.sourceE1 = e1;
 		this.e2 = e2;
 		this.sourceE2 = e2;
 		if (e1 != null && e2 != null) {
 			this.start = e1.start;
 			this.length = e2.start + e2.length - e1.start;
 		}
 	}
 
 	public Expression BinExp_semantic(Scope sc, SemanticContext context) {
 		e1 = e1.semantic(sc, context);
 		if (e1.type == null) {
 			context.acceptProblem(Problem.newSemanticTypeError(IProblem.SymbolHasNoValue, e1, new String[] { e1.toChars(context) }));
 			e1.type = Type.terror;
 		}
 		e2 = e2.semantic(sc, context);
 		if (e2.type == null) {
 			context.acceptProblem(Problem.newSemanticTypeError(IProblem.SymbolHasNoValue, e2, new String[] { e2.toChars(context) }));
 			e2.type = Type.terror;
 		}
 		Assert.isNotNull(e1.type);
 		return this;
 	}
 
 	@Override
 	public int checkSideEffect(int flag, SemanticContext context) {
 		if (op == TOKplusplus || op == TOKminusminus || op == TOKassign
 				|| op == TOKaddass || op == TOKminass || op == TOKcatass
 				|| op == TOKmulass || op == TOKdivass || op == TOKmodass
 				|| op == TOKshlass || op == TOKshrass || op == TOKushrass
 				|| op == TOKandass || op == TOKorass || op == TOKxorass
 				|| op == TOKin || op == TOKremove) {
 			return 1;
 		}
 		return super.checkSideEffect(flag, context);
 	}
 
 	public Expression commonSemanticAssignIntegral(Scope sc,
 			SemanticContext context) {
 		Expression e;
 
 		if (type == null) {
 			BinExp_semantic(sc, context);
 			e2 = resolveProperties(sc, e2, context);
 
 			e = op_overload(sc, context);
 			if (e != null) {
 				return e;
 			}
 
 			e1 = e1.modifiableLvalue(sc, null, context);
 			e1.checkScalar(context);
 			type = e1.type;
 			if (type.toBasetype(context).ty == Tbool) {
 				e2 = e2.implicitCastTo(sc, type, context);
 			}
 
 			typeCombine(sc, context);
 			e1.checkIntegral(context);
 			e2.checkIntegral(context);
 		}
 
 		return this;
 	}
 
 	public Expression commonSemmanticAssign(Scope sc, SemanticContext context) {
 		Expression e;
 
 		if (type == null) {
 			BinExp_semantic(sc, context);
 			e2 = resolveProperties(sc, e2, context);
 
 			e = op_overload(sc, context);
 			if (e != null) {
 				return e;
 			}
 
 			e1 = e1.modifiableLvalue(sc, null, context);
 			e1.checkScalar(context);
 			type = e1.type;
 			if (type.toBasetype(context).ty == Tbool) {
 				context.acceptProblem(Problem.newSemanticTypeError(
 						IProblem.OperatorNotAllowedOnBoolExpression, this, new String[] { toChars(context) }));
 			}
 			typeCombine(sc, context);
 			e1.checkArithmetic(context);
 			e2.checkArithmetic(context);
 
 			if (op == TOKmodass && e2.type.iscomplex()) {
 				context.acceptProblem(Problem.newSemanticTypeError(
 						IProblem.CannotPerformModuloComplexArithmetic, this));
 				return new IntegerExp(loc, 0);
 			}
 		}
 
 		return this;
 	}
 
 	public void incompatibleTypes(SemanticContext context) {
 		context.acceptProblem(Problem.newSemanticTypeError(IProblem.IncompatibleTypesForOperator, e1, e2, new String[] { e1.type.toChars(context), e2.type.toChars(context), op.toString() }));
 	}
 
 	public boolean isunsigned() {
 		return e1.type.isunsigned() || e2.type.isunsigned();
 	}
 
 	public Expression scaleFactor(Scope sc, SemanticContext context) {
 		BigInteger stride;
 		Type t1b = e1.type.toBasetype(context);
 		Type t2b = e2.type.toBasetype(context);
 
 		if (t1b.ty == Tpointer && t2b.isintegral()) { // Need to adjust operator by the stride
 			// Replace (ptr + int) with (ptr + (int * stride))
 			Type t = Type.tptrdiff_t;
 
 			stride = new BigInteger(String.valueOf(t1b.next.size(loc, context)));
 			if (!t.equals(t2b)) {
 				e2 = e2.castTo(sc, t, context);
 			}
 			if (t1b.next.isbit()) {
 				// BUG: should add runtime check for misaligned offsets
 				// This perhaps should be done by rewriting as &p[i]
 				// and letting back end do it.
 				e2 = new UshrExp(loc, e2, new IntegerExp(loc, 3, t));
 			} else {
 				e2 = new MulExp(loc, e2, new IntegerExp(loc, new integer_t(
 						stride), t));
 			}
 			e2.type = t;
 			type = e1.type;
 			// TODO check "t2b.ty" 
 		} else if (t2b.ty != Tarray && t1b.isintegral()) { // Need to adjust operator by the stride
 			// Replace (int + ptr) with (ptr + (int * stride))
 			Type t = Type.tptrdiff_t;
 			Expression e;
 
 			stride = new BigInteger(String.valueOf(t2b.next.size(loc, context)));
 			if (!t.equals(t1b)) {
 				e = e1.castTo(sc, t, context);
 			} else {
 				e = e1;
 			}
 			if (t2b.next.isbit()) {
 				// BUG: should add runtime check for misaligned offsets
 				e = new UshrExp(loc, e, new IntegerExp(loc, 3, t));
 			} else {
 				e = new MulExp(loc, e, new IntegerExp(loc,
 						new integer_t(stride), t));
 			}
 			e.type = t;
 			type = e2.type;
 			e1 = e2;
 			e2 = e;
 		}
 		return this;
 	}
 
 	@Override
 	public Expression semantic(Scope sc, SemanticContext context) {
 		return BinExp_semantic(sc, context);
 	}
 
 	public Expression semanticp(Scope sc, SemanticContext context) {
 		BinExp_semantic(sc, context);
 		e1 = resolveProperties(sc, e1, context);
 		e2 = resolveProperties(sc, e2, context);
 		return this;
 	}
 
 	@Override
 	public Expression syntaxCopy(SemanticContext context) {
 		BinExp e;
 
 		e = (BinExp) copy();
 		e.type = null;
 		e.e1 = e.e1.syntaxCopy(context);
 		e.e2 = e.e2.syntaxCopy(context);
 		return e;
 	}
 
 	@Override
 	public void toCBuffer(OutBuffer buf, HdrGenState hgs,
 			SemanticContext context) {
 		expToCBuffer(buf, hgs, e1, op.precedence, context);
 		buf.writeByte(' ');
 		buf.writestring(op.toString());
 		buf.writeByte(' ');
 		expToCBuffer(buf, hgs, e2, op.precedence.ordinal() + 1,
 				context);
 	}
 
 	public Expression typeCombine(Scope sc, SemanticContext context) {
 		Type t1;
 		Type t2;
 		Type t;
 		TY ty;
 
 		e1 = e1.integralPromotions(sc, context);
 		e2 = e2.integralPromotions(sc, context);
 
 		// BUG: do toBasetype()
 		t1 = e1.type;
 		t2 = e2.type;
 		Assert.isNotNull(t1);
 		Assert.isNotNull(t2);
 
 		Type t1b = t1.toBasetype(context);
 		Type t2b = t2.toBasetype(context);
 
 		ty = Type.impcnvResult[t1b.ty.ordinal()][t2b.ty.ordinal()];
 		if (ty != Terror) {
 			TY ty1;
 			TY ty2;
 
 			ty1 = Type.impcnvType1[t1b.ty.ordinal()][t2b.ty.ordinal()];
 			ty2 = Type.impcnvType2[t1b.ty.ordinal()][t2b.ty.ordinal()];
 
 			if (t1b.ty == ty1) // if no promotions
 			{
 				if (same(t1, t2, context)) {
 					if (type == null) {
 						type = t1;
 					}
 					return this;
 				}
 
 				if (same(t1b, t2b, context)) {
 					if (type == null) {
 						type = t1b;
 					}
 					return this;
 				}
 			}
 
 			if (type == null) {
 				type = Type.basic[ty.ordinal()];
 			}
 
 			t1 = Type.basic[ty1.ordinal()];
 			t2 = Type.basic[ty2.ordinal()];
 			e1 = e1.castTo(sc, t1, context);
 			e2 = e2.castTo(sc, t2, context);
 			return this;
 		}
 
 		t = t1;
 		if (same(t1, t2, context)) {
 			if ((t1.ty == Tstruct || t1.ty == Tclass)
 					&& (op == TOKmin || op == TOKadd)) {
 				return typeCombine_Lincompatible_End(t, context);
 			}
 		} else if (t1.isintegral() && t2.isintegral()) {
 			int sz1 = t1.size(loc, context);
 			int sz2 = t2.size(loc, context);
 			boolean sign1 = t1.isunsigned();
 			boolean sign2 = t2.isunsigned();
 
 			if (sign1 == sign2) {
 				if (sz1 < sz2) {
 					// goto Lt2;
 					e1 = e1.castTo(sc, t2, context);
 					t = t2;
 					if (type == null) {
 						type = t;
 					}
 					return this;
 				} else {
 					// goto Lt1;
 					e2 = e2.castTo(sc, t1, context);
 					t = t1;
 					if (type == null) {
 						type = t;
 					}
 					return this;
 				}
 			}
 			if (!sign1) {
 				if (sz1 >= sz2) {
 					// goto Lt1;
 					e2 = e2.castTo(sc, t1, context);
 					t = t1;
 					if (type == null) {
 						type = t;
 					}
 					return this;
 				} else {
 					// goto Lt2
 					e1 = e1.castTo(sc, t2, context);
 					t = t2;
 					if (type == null) {
 						type = t;
 					}
 					return this;
 				}
 			} else {
 				if (sz2 >= sz1) {
 					// goto Lt2
 					e1 = e1.castTo(sc, t2, context);
 					t = t2;
 					if (type == null) {
 						type = t;
 					}
 					return this;
 				} else {
 					// goto Lt1;
 					e2 = e2.castTo(sc, t1, context);
 					t = t1;
 					if (type == null) {
 						type = t;
 					}
 					return this;
 				}
 			}
 		} else if (t1.ty == Tpointer && t2.ty == Tpointer) {
 			// Bring pointers to compatible type
 			Type t1n = t1.next;
 			Type t2n = t2.next;
 
 			assert (!same(t1n, t2n, context));
 			if (t1n.ty == Tvoid) {
 				t = t2;
 			} else if (t2n.ty == Tvoid) {
 				;
 			} else if (t1n.ty == Tclass && t2n.ty == Tclass) {
 				ClassDeclaration cd1 = t1n.isClassHandle();
 				ClassDeclaration cd2 = t2n.isClassHandle();
 				int offset[] = { 0 };
 
 				if (cd1.isBaseOf(cd2, offset, context)) {
 					if (offset[0] != 0) {
 						e2 = e2.castTo(sc, t, context);
 					}
 				} else if (cd2.isBaseOf(cd1, offset, context)) {
 					t = t2;
 					if (offset[0] != 0) {
 						e1 = e1.castTo(sc, t, context);
 					}
 				} else {
 					return typeCombine_Lincompatible_End(t, context);
 				}
 			} else {
 				return typeCombine_Lincompatible_End(t, context);
 			}
 		} else if ((t1.ty == Tsarray || t1.ty == Tarray) && e2.op == TOKnull
 				&& t2.ty == Tpointer && t2.next.ty == Tvoid) {
 			// goto Lx1;
 			t = t1.next.arrayOf(context);
 			e1 = e1.castTo(sc, t, context);
 			e2 = e2.castTo(sc, t, context);
 			if (type == null) {
 				type = t;
 			}
 			return this;
 		} else if ((t2.ty == Tsarray || t2.ty == Tarray) && e1.op == TOKnull
 				&& t1.ty == Tpointer && t1.next.ty == Tvoid) {
 			// goto Lx2;
 			t = t2.next.arrayOf(context);
 			e1 = e1.castTo(sc, t, context);
 			e2 = e2.castTo(sc, t, context);
 			if (type == null) {
 				type = t;
 			}
 			return this;
 		} else if ((t1.ty == Tsarray || t1.ty == Tarray)
 				&& t1.implicitConvTo(t2, context) != MATCHnomatch) {
 			// goto Lt2;
 			e1 = e1.castTo(sc, t2, context);
 			t = t2;
 			if (type == null) {
 				type = t;
 			}
 			return this;
 		} else if ((t2.ty == Tsarray || t2.ty == Tarray)
 				&& t2.implicitConvTo(t1, context) != MATCHnomatch) {
 			// goto Lt1;
 			e2 = e2.castTo(sc, t1, context);
 			t = t1;
 			if (type == null) {
 				type = t;
 			}
 			return this;
 		} else if (t1.ty == Tclass || t2.ty == Tclass) {
 			MATCH i1;
 			MATCH i2;
 
 			i1 = e2.implicitConvTo(t1, context);
 			i2 = e1.implicitConvTo(t2, context);
 
 			if (i1 != MATCHnomatch && i2 != MATCHnomatch) {
 				// We have the case of class vs. void*, so pick class
 				if (t1.ty == Tpointer) {
 					i1 = MATCHnomatch;
 				} else if (t2.ty == Tpointer) {
 					i2 = MATCHnomatch;
 				}
 			}
 
 			if (i2 != MATCHnomatch) {
 				// goto Lt2;
 				e1 = e1.castTo(sc, t2, context);
 				t = t2;
 				if (type == null) {
 					type = t;
 				}
 				return this;
 			} else if (i1 != MATCHnomatch) {
 				// goto Lt1;
 				e2 = e2.castTo(sc, t1, context);
 				t = t1;
 				if (type == null) {
 					type = t;
 				}
 				return this;
 			} else {
 				return typeCombine_Lincompatible_End(t, context);
 			}
 		} else if ((e1.op == TOKstring || e1.op == TOKnull)
 				&& e1.implicitConvTo(t2, context) != MATCHnomatch) {
 			// goto Lt2;
 			e1 = e1.castTo(sc, t2, context);
 			t = t2;
 			if (type == null) {
 				type = t;
 			}
 			return this;
 		} else if ((e2.op == TOKstring || e2.op == TOKnull)
 				&& e2.implicitConvTo(t1, context) != MATCHnomatch) {
 			// goto Lt1;
 			e2 = e2.castTo(sc, t1, context);
 			t = t1;
 			if (type == null) {
 				type = t;
 			}
 			return this;
 		} else if (t1.ty == Tsarray
 				&& t2.ty == Tsarray
 				&& e2.implicitConvTo(t1.next.arrayOf(context), context) != MATCHnomatch) {
 			t = t1.next.arrayOf(context);
 			e1 = e1.castTo(sc, t, context);
 			e2 = e2.castTo(sc, t, context);
 		} else if (t1.ty == Tsarray
 				&& t2.ty == Tsarray
 				&& e1.implicitConvTo(t2.next.arrayOf(context), context) != MATCHnomatch) {
 			t = t2.next.arrayOf(context);
 			e1 = e1.castTo(sc, t, context);
 			e2 = e2.castTo(sc, t, context);
 		} else {
 			incompatibleTypes(context);
 		}
 		if (type == null) {
 			type = t;
 		}
 		return this;
 	}
 
 	private Expression typeCombine_Lincompatible_End(Type t,
 			SemanticContext context) {
 		incompatibleTypes(context);
 		if (type == null) {
 			type = t;
 		}
 		return this;
 	}
 
 	public final Expression interpretCommon(InterState istate, BinExp_fp fp,
 			SemanticContext context) {
 		Expression e;
 		Expression e1;
 		Expression e2;
 
 		e1 = this.e1.interpret(istate, context);
 		if (e1 == EXP_CANT_INTERPRET)
 			return EXP_CANT_INTERPRET; //goto Lcant;
 		if (!e1.isConst())
 			return EXP_CANT_INTERPRET; //goto Lcant;
 
 		e2 = this.e2.interpret(istate, context);
 		if (e2 == EXP_CANT_INTERPRET)
 			return EXP_CANT_INTERPRET; //goto Lcant;
 		if (!e2.isConst())
 			return EXP_CANT_INTERPRET; //goto Lcant;
 
 		e = fp.call(type, e1, e2, context);
 		return e;
 	}
 
 	public final Expression interpretCommon2(InterState istate, BinExp_fp2 fp,
 			SemanticContext context) {
 		Expression e;
 		Expression e1;
 		Expression e2;
 
 		e1 = this.e1.interpret(istate, context);
 		if (e1 == EXP_CANT_INTERPRET) {
 			// goto Lcant;
 			return EXP_CANT_INTERPRET;
 		}
 		if (!e1.isConst() && e1.op != TOKstring && e1.op != TOKarrayliteral
 				&& e1.op != TOKstructliteral) {
 			// goto Lcant;
 			return EXP_CANT_INTERPRET;
 		}
 
 		e2 = this.e2.interpret(istate, context);
 		if (e2 == EXP_CANT_INTERPRET) {
 			// goto Lcant;
 			return EXP_CANT_INTERPRET;
 		}
 		if (!e2.isConst() && e2.op != TOKstring && e2.op != TOKarrayliteral
 				&& e2.op != TOKstructliteral) {
 			// goto Lcant;
 			return EXP_CANT_INTERPRET;
 		}
 
 		e = fp.call(op, type, e1, e2, context);
 		return e;
 
 		//	Lcant:
 		//	    return EXP_CANT_INTERPRET;
 	}
 
 	public final Expression interpretAssignCommon(InterState istate,
 			BinExp_fp fp, SemanticContext context) {
 		return interpretAssignCommon(istate, fp, 0, context);
 	}
 
 	public final Expression interpretAssignCommon(InterState istate,
 			BinExp_fp fp, int post, SemanticContext context) {
 		Expression e = EXP_CANT_INTERPRET;
 		Expression e1 = this.e1;
 
 		if (null != fp) {
 			if (e1.op == TOKcast) {
 				CastExp ce = (CastExp) e1;
 				e1 = ce.e1;
 			}
 		}
 		if (e1 == EXP_CANT_INTERPRET)
 			return e1;
 		Expression e2 = this.e2.interpret(istate, context);
 		if (e2 == EXP_CANT_INTERPRET)
 			return e2;
 
 		/* Assignment to variable of the form:
 		 *        v = e2
 		 */
 		if (e1.op == TOKvar) {
 			VarExp ve = (VarExp) e1;
 			VarDeclaration v = ve.var.isVarDeclaration();
 			if (null != v && !v.isDataseg(context)) {
 				/* Chase down rebinding of out and ref
 				 */
 				if (null != v.value && v.value.op == TOKvar) {
 					ve = (VarExp) v.value;
 					v = ve.var.isVarDeclaration();
 					assert (null != v);
 				}
 
 				Expression ev = v.value;
 				if (null != fp && null == ev) {
 					context.acceptProblem(Problem.newSemanticTypeError(
 							IProblem.VariableIsUsedBeforeInitialization, v, new String[] { v.toChars(context) }));
 					return e;
 				}
 				if (null != fp)
 					e2 = fp.call(v.type, ev, e2, context);
 				else
 					e2 = Constfold.Cast(v.type, v.type, e2, context);
 				if (e2 != EXP_CANT_INTERPRET) {
 					if (!v.isParameter()) {
 						for (int i = 0; true; i++) {
 							if (i == size(istate.vars)) {
 								if (istate.vars == null) {
 									istate.vars = new Dsymbols();
 								}
 								istate.vars.add(v);
 								break;
 							}
 							if (v == (VarDeclaration) istate.vars.get(i))
 								break;
 						}
 					}
 					v.value = e2;
 					e = Constfold.Cast(type, type, post > 0 ? ev : e2, context);
 				}
 			}
 		}
 		/* Assignment to struct member of the form:
 		 *   (symoffexp) = e2
 		 */
 		else if (e1.op == TOKstar && ((PtrExp) e1).e1.op == TOKsymoff) {
 			SymOffExp soe = (SymOffExp) ((PtrExp) e1).e1;
 			VarDeclaration v = soe.var.isVarDeclaration();
 
 			if (v.isDataseg(context))
 				return EXP_CANT_INTERPRET;
 			if (null != fp && null == v.value) {
 				context.acceptProblem(Problem.newSemanticTypeError(
 						IProblem.VariableIsUsedBeforeInitialization, v, new String[] { v.toChars(context) }));
 				return e;
 			}
 			if (v.value.op != TOKstructliteral)
 				return EXP_CANT_INTERPRET;
 			StructLiteralExp se = (StructLiteralExp) v.value;
 			int fieldi = se.getFieldIndex(type, soe.offset.intValue(), context);
 			if (fieldi == -1)
 				return EXP_CANT_INTERPRET;
 			Expression ev = se.getField(type, soe.offset.intValue(), context);
 			if (null != fp)
 				e2 = fp.call(type, ev, e2, context);
 			else
 				e2 = Constfold.Cast(type, type, e2, context);
 			if (e2 == EXP_CANT_INTERPRET)
 				return e2;
 
 			if (!v.isParameter()) {
 				for (int i = 0; true; i++) {
 					if (i == istate.vars.size()) {
 						istate.vars.add(v);
 						break;
 					}
 					if (v == (VarDeclaration) istate.vars.get(i))
 						break;
 				}
 			}
 
 			/* Create new struct literal reflecting updated fieldi
 			 */
 			Expressions expsx = new Expressions();
 			expsx.setDim(se.elements.size());
 			for (int j = 0; j < se.elements.size(); j++) {
 				if (j == fieldi)
 					expsx.add(e2);
 				else
 					expsx.add(j, se.elements.get(j));
 			}
 			v.value = new StructLiteralExp(se.loc, se.sd, expsx);
 			v.value.type = se.type;
 
 			e = Constfold.Cast(type, type, post > 0 ? ev : e2, context);
 		}
 		/* Assignment to array element of the form:
 		 *   a[i] = e2
 		 */
 		else if (e1.op == TOKindex && ((IndexExp) e1).e1.op == TOKvar) {
 			IndexExp ie = (IndexExp) e1;
 			VarExp ve = (VarExp) ie.e1;
 			VarDeclaration v = ve.var.isVarDeclaration();
 
 			if (null == v || v.isDataseg(context))
 				return EXP_CANT_INTERPRET;
 			if (null == v.value) {
 				if (null != fp) {
 					context.acceptProblem(Problem.newSemanticTypeError(
 							IProblem.VariableIsUsedBeforeInitialization, v, new String[] { v.toChars(context) }));
 					return e;
 				}
 
 				Type t = v.type.toBasetype(context);
 				if (t.ty == Tsarray) {
 					/* This array was void initialized. Create a
 					 * default initializer for it.
 					 * What we should do is fill the array literal with
 					 * null data, so use-before-initialized can be detected.
 					 * But we're too lazy at the moment to do it, as that
 					 * involves redoing Index() and whoever calls it.
 					 */
 					Expression ev = v.type.defaultInit(context);
 					int dim = ((TypeSArray) t).dim.toInteger(context)
 							.intValue();
 					Expressions elements = new Expressions();
 					for (int i = 0; i < dim; i++)
 						elements.add(ev);
 					ArrayLiteralExp ae = new ArrayLiteralExp(Loc.ZERO, elements);
 					ae.type = v.type;
 					v.value = ae;
 				} else
 					return EXP_CANT_INTERPRET;
 			}
 
 			ArrayLiteralExp ae = null;
 			AssocArrayLiteralExp aae = null;
 			StringExp se = null;
 			if (v.value.op == TOKarrayliteral)
 				ae = (ArrayLiteralExp) v.value;
 			else if (v.value.op == TOKassocarrayliteral)
 				aae = (AssocArrayLiteralExp) v.value;
 			else if (v.value.op == TOKstring)
 				se = (StringExp) v.value;
 			else
 				return EXP_CANT_INTERPRET;
 
 			Expression index = ie.e2.interpret(istate, context);
 			if (index == EXP_CANT_INTERPRET)
 				return EXP_CANT_INTERPRET;
 			Expression ev = null;
 			if (null != fp || null != ae || null != se) // not for aae, because key might not be there
 			{
 				ev = Index.call(type, v.value, index, context);
 				if (ev == EXP_CANT_INTERPRET)
 					return EXP_CANT_INTERPRET;
 			}
 
 			if (null != fp)
 				e2 = fp.call(type, ev, e2, context);
 			else
 				e2 = Constfold.Cast(type, type, e2, context);
 			if (e2 == EXP_CANT_INTERPRET)
 				return e2;
 
 			if (!v.isParameter()) {
 				for (int i = 0; true; i++) {
 					if (i == istate.vars.size()) {
 						istate.vars.add(v);
 						break;
 					}
 					if (v == (VarDeclaration) istate.vars.get(i))
 						break;
 				}
 			}
 
 			if (null != ae) {
 				/* Create new array literal reflecting updated elem
 				 */
 				int elemi = index.toInteger(context).intValue();
 				Expressions expsx = new Expressions();
 				expsx.setDim(ae.elements.size());
 				for (int j = 0; j < ae.elements.size(); j++) {
 					if (j == elemi)
 						expsx.add(e2);
 					else
 						expsx.add(ae.elements.get(j));
 				}
 				v.value = new ArrayLiteralExp(ae.loc, expsx);
 				v.value.type = ae.type;
 			} else if (null != aae) {
 				/* Create new associative array literal reflecting updated key/value
 				 */
 				Expressions keysx = aae.keys;
 				Expressions valuesx = new Expressions();
 				valuesx.setDim(aae.values.size());
 				int updated = 0;
 				for (int j = aae.values.size(); j > 0;) {
 					j--;
 					Expression ekey = (Expression) aae.keys.get(j);
 					Expression ex = Equal.call(TOKequal, Type.tbool, ekey,
 							index, context);
 					if (ex == EXP_CANT_INTERPRET)
 						return EXP_CANT_INTERPRET;
 					if (ex.isBool(true)) {
 						valuesx.set(j, e2);
 						updated = 1;
 					} else
 						valuesx.set(j, aae.values.get(j));
 				}
 				if (0 == updated) { // Append index/e2 to keysx[]/valuesx[]
 					valuesx.add(e2);
 					keysx = new Expressions(keysx);
 					keysx.add(index);
 				}
 				v.value = new AssocArrayLiteralExp(aae.loc, keysx, valuesx);
 				v.value.type = aae.type;
 			} else if (null != se) {
 				/* Create new string literal reflecting updated elem
 				 */
 				int elemi = index.toInteger(context).intValue();
 				char[] s = new char[se.len + 1];
 				//s = (unsigned char )mem.calloc(se.len + 1, se.sz);
 				System.arraycopy(se.string, 0, s, 0, se.length);
 				//memcpy(s, se.string, se.len  se.sz);
 				int value = e2.toInteger(context).intValue();
 				switch (se.sz) {
 				/* FIXME semantic 
 				 * (I'm not sure what sort of bit manipulation I can do
 				 *  with Java's char[] type) and whether there will be
 				 *  adequate space allocated. The char[] array in StringExp
 				 *  may need to be changed to a byte[] which would suck.
 				 *
 				 case 1:        s[elemi] = value; break;
 				 case 2:        ((unsigned short )s)[elemi] = value; break;
 				 case 4:        ((unsigned )s)[elemi] = value; break;
 				 */
 
 				//---temporary code for Descent testing---
 				case 1:
 				case 2:
 				case 4:
 					s[elemi] = (char) value;
 					break;
 				//---end temporary code block---
 
 				default:
 					assert (false);
 					break;
 				}
 				StringExp se2 = new StringExp(se.loc, s, se.len);
 				se2.committed = se.committed;
 				se2.postfix = se.postfix;
 				se2.type = se.type;
 				v.value = se2;
 			} else {
 				assert (false);
 			}
 			e = Constfold.Cast(type, type, post > 0 ? ev : e2, context);
 		}
 		return e;
 	}
 
 	public Expression op_overload(Scope sc, SemanticContext context) {
 		//AggregateDeclaration ad;
 		Type t1 = e1.type.toBasetype(context);
 		Type t2 = e2.type.toBasetype(context);
 		char[] id = opId();
 		char[] id_r = opId_r();
 
 		Match m;
 		Expressions args1 = new Expressions(1);
 		Expressions args2 = new Expressions(1);
 		int argsset = 0;
 
 		AggregateDeclaration ad1;
 		if (t1.ty == Tclass)
 			ad1 = ((TypeClass) t1).sym;
 		else if (t1.ty == Tstruct)
 			ad1 = ((TypeStruct) t1).sym;
 		else
 			ad1 = null;
 
 		AggregateDeclaration ad2;
 		if (t2.ty == Tclass)
 			ad2 = ((TypeClass) t2).sym;
 		else if (t2.ty == Tstruct)
 			ad2 = ((TypeStruct) t2).sym;
 		else
 			ad2 = null;
 
 		Dsymbol s = null;
 		Dsymbol s_r = null;
 		FuncDeclaration fd = null;
 		TemplateDeclaration td = null;
 		if (ad1 != null && id != null) {
 			s = search_function(ad1, id, context);
 		}
 		if (ad2 != null && id_r != null) {
 			s_r = search_function(ad2, id_r, context);
 		}
 
 		if (s != null || s_r != null) {
 			/* Try:
 			 *	a.opfunc(b)
 			 *	b.opfunc_r(a)
 			 * and see which is better.
 			 */
 			Expression e;
 			FuncDeclaration lastf;
 
 			args1.setDim(1);
 			args1.set(0, e1);
 			args2.setDim(1);
 			args2.set(0, e2);
 			argsset = 1;
 
 			m = new Match();
 			m.last = MATCHnomatch;
 
 			if (s != null) {
 				fd = s.isFuncDeclaration();
 				if (fd != null) {
 					overloadResolveX(m, fd, args2, context);
 				} else {
 					td = s.isTemplateDeclaration();
 					templateResolve(m, td, sc, loc, null, args2, context);
 				}
 			}
 
 			lastf = m.lastf;
 
 			if (s_r != null) {
 				fd = s_r.isFuncDeclaration();
 				if (fd != null) {
 					overloadResolveX(m, fd, args1, context);
 				} else {
 					td = s_r.isTemplateDeclaration();
 					templateResolve(m, td, sc, loc, null, args1, context);
 				}
 			}
 
 			if (m.count > 1) {
 				// Error, ambiguous
 				context.acceptProblem(Problem.newSemanticTypeError(IProblem.BothOverloadsMuchArgumentList, this, new String[] { m.lastf.type.toChars(context), m.nextf.type
 								.toChars(context), m.lastf.toChars(context) }));
 			} else if (m.last == MATCHnomatch) {
 				m.lastf = m.anyf;
 			}
 
 			if (op == TOKplusplus || op == TOKminusminus)
 				// Kludge because operator overloading regards e++ and e--
 				// as unary, but it's implemented as a binary.
 				// Rewrite (e1 ++ e2) as e1.postinc()
 				// Rewrite (e1 -- e2) as e1.postdec()
 				e = build_overload(loc, sc, e1, null, id, context);
 			else if (lastf != null && m.lastf == lastf
 					|| m.last == MATCHnomatch)
 				// Rewrite (e1 op e2) as e1.opfunc(e2)
 				e = build_overload(loc, sc, e1, e2, id, context);
 			else
 				// Rewrite (e1 op e2) as e2.opfunc_r(e1)
 				e = build_overload(loc, sc, e2, e1, id_r, context);
 			return e;
 		}
 
 		if (isCommutative()) {
 			s = null;
 			s_r = null;
 			if (ad1 != null && id_r != null) {
 				s_r = search_function(ad1, id_r, context);
 			}
 			if (ad2 != null && id != null) {
 				s = search_function(ad2, id, context);
 			}
 
 			if (s != null || s_r != null) {
 				/* Try:
 				 *	a.opfunc_r(b)
 				 *	b.opfunc(a)
 				 * and see which is better.
 				 */
 				Expression e;
 				FuncDeclaration lastf;
 
 				if (0 == argsset) {
 					args1.setDim(1);
 					args1.set(0, e1);
 					args2.setDim(1);
 					args2.set(0, e2);
 				}
 
 				m = new Match();
 				m.last = MATCHnomatch;
 
 				if (s_r != null) {
 					fd = s_r.isFuncDeclaration();
 					if (fd != null) {
 						overloadResolveX(m, fd, args2, context);
 					} else {
 						td = s_r.isTemplateDeclaration();
 						templateResolve(m, td, sc, loc, null, args2, context);
 					}
 				}
 				lastf = m.lastf;
 
 				if (s != null) {
 					fd = s.isFuncDeclaration();
 					if (fd != null) {
 						overloadResolveX(m, fd, args1, context);
 					} else {
 						td = s.isTemplateDeclaration();
 						templateResolve(m, td, sc, loc, null, args1, context);
 					}
 				}
 
 				if (m.count > 1) {
 					// Error, ambiguous
 					context.acceptProblem(Problem.newSemanticTypeError(
 							IProblem.BothOverloadsMuchArgumentList, this, new String[] { m.lastf.type.toChars(context), m.nextf.type.toChars(context), m.lastf.toChars(context) }));
 				} else if (m.last == MATCHnomatch) {
 					m.lastf = m.anyf;
 				}
 
 				if (lastf != null && m.lastf == lastf || id_r != null
 						&& m.last == MATCHnomatch)
 					// Rewrite (e1 op e2) as e1.opfunc_r(e2)
 					e = build_overload(loc, sc, e1, e2, id_r, context);
 				else
 					// Rewrite (e1 op e2) as e2.opfunc(e1)
 					e = build_overload(loc, sc, e2, e1, id, context);
 
 				// When reversing operands of comparison operators,
 				// need to reverse the sense of the op
 				switch (op) {
 				case TOKlt:
 					op = TOKgt;
 					break;
 				case TOKgt:
 					op = TOKlt;
 					break;
 				case TOKle:
 					op = TOKge;
 					break;
 				case TOKge:
 					op = TOKle;
 					break;
 
 				// Floating point compares
 				case TOKule:
 					op = TOKuge;
 					break;
 				case TOKul:
 					op = TOKug;
 					break;
 				case TOKuge:
 					op = TOKule;
 					break;
 				case TOKug:
 					op = TOKul;
 					break;
 
 				// These are symmetric
 				case TOKunord:
 				case TOKlg:
 				case TOKleg:
 				case TOKue:
 					break;
 				}
 
 				return e;
 			}
 		}
 
 		return null;
 	}
 	
 	@Override
 	public Expression optimize(int result, SemanticContext context) {
 		e1 = e1.optimize(result, context);
 	    e2 = e2.optimize(result, context);
 	    return this;
 	}
 
 }
