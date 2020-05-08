 package descent.internal.compiler.parser;
 
 import static descent.internal.compiler.parser.Constfold.Cast;
 import static descent.internal.compiler.parser.MATCH.MATCHconst;
 import static descent.internal.compiler.parser.TOK.TOKarrayliteral;
 import static descent.internal.compiler.parser.TOK.TOKcall;
 import static descent.internal.compiler.parser.TOK.TOKconst;
 import static descent.internal.compiler.parser.TOK.TOKimmutable;
 import static descent.internal.compiler.parser.TOK.TOKnull;
 import static descent.internal.compiler.parser.TOK.TOKshared;
 import static descent.internal.compiler.parser.TOK.TOKstring;
 import static descent.internal.compiler.parser.TOK.TOKstructliteral;
 import static descent.internal.compiler.parser.TOK.TOKsymoff;
 import static descent.internal.compiler.parser.TOK.TOKvar;
 import static descent.internal.compiler.parser.TY.Tarray;
 import static descent.internal.compiler.parser.TY.Tclass;
 import static descent.internal.compiler.parser.TY.Tpointer;
 import static descent.internal.compiler.parser.TY.Tsarray;
 import static descent.internal.compiler.parser.TY.Tstruct;
 import static descent.internal.compiler.parser.TY.Tvoid;
 import static descent.internal.compiler.parser.Type.MODconst;
 import static descent.internal.compiler.parser.Type.MODinvariant;
 import static descent.internal.compiler.parser.Type.MODshared;
 import melnorme.miscutil.tree.TreeVisitor;
 import descent.core.compiler.IProblem;
 import descent.internal.compiler.parser.ast.IASTVisitor;
 
 public class CastExp extends UnaExp {
 
 	public Type to, sourceTo;
 	public TOK tok, tok2;
 	public int modifierStart, modifier2Start;
 	public int mod;
 
 	public CastExp(char[] filename, int lineNumber, Expression e1, TOK tok, int modifierStart) {
 		super(filename, lineNumber, TOK.TOKcast, e1);
 		this.modifierStart = modifierStart;
 		this.to = null;
 		this.tok = tok;
 	}
 	
 	public CastExp(char[] filename, int lineNumber, Expression e1, int mod, TOK tok, int modifierStart) {
 		super(filename, lineNumber, TOK.TOKcast, e1);
 		this.modifierStart = modifierStart;
 		this.to = null;
 		this.tok = tok;
 		this.mod = mod;
 	}
 	
 	public CastExp(char[] filename, int lineNumber, Expression e1, int mod, TOK tok, int modifierStart, TOK tok2, int modifier2Start) {
 		super(filename, lineNumber, TOK.TOKcast, e1);
 		this.modifierStart = modifierStart;
 		this.to = null;
 		this.tok = tok;
 		this.mod = mod;
 		this.tok2 = tok2;
 		this.modifier2Start = modifier2Start;
 	}
 
 	public CastExp(char[] filename, int lineNumber, Expression e1, Type t) {
 		super(filename, lineNumber, TOK.TOKcast, e1);
 		this.to = this.sourceTo = t;
 		this.tok = null;
 	}
 
 	@Override
 	public void accept0(IASTVisitor visitor) {
 		boolean children = visitor.visit(this);
 		if (children) {
 			TreeVisitor.acceptChildren(visitor, sourceTo);
 			TreeVisitor.acceptChildren(visitor, sourceE1);
 		}
 		visitor.endVisit(this);
 	}
 
 	@Override
 	public void checkEscape(SemanticContext context) {
 		Type tb = type.toBasetype(context);
 		if (tb.ty == Tarray && e1.op == TOKvar
 				&& e1.type.toBasetype(context).ty == Tsarray) {
 			VarExp ve = (VarExp) e1;
 			VarDeclaration v = ve.var.isVarDeclaration();
 			if (v != null) {
 				if (!v.isDataseg(context) && !v.isParameter()) {
 					if (context.acceptsErrors()) {
 						context.acceptProblem(Problem.newSemanticTypeError(
 								IProblem.EscapingReferenceToLocal, this, v.toChars(context)));
 					}
 				}
 			}
 		}
 	}
 
 	@Override
 	public int checkSideEffect(int flag, SemanticContext context) {
 		/* if not:
 		 *  cast(void)
 		 *  cast(classtype)func()
 		 */
 		if (!to.equals(Type.tvoid)
 				&& !(to.ty == Tclass && e1.op == TOKcall && e1.type.ty == Tclass)) {
 			return super.checkSideEffect(flag, context);
 		}
 		return 1;
 	}
 
 	@Override
 	public int getNodeType() {
 		return CAST_EXP;
 	}
 
 	@Override
 	public Expression interpret(InterState istate, SemanticContext context) {
 		// Expression e;
 		Expression e1;
 
 		e1 = this.e1.interpret(istate, context);
 		if (e1 == EXP_CANT_INTERPRET) {
 			// goto Lcant;
 			return EXP_CANT_INTERPRET;
 		}
 		return Cast(type, to, e1, context);
 	}
 
 	@Override
 	public char[] opId(SemanticContext context) {
 		return Id.cast;
 	}
 
 	@Override
 	public Expression optimize(int result, SemanticContext context) {
 		if (type == null) {
 			// SEMANTIC 
 			// throw new IllegalStateException("assert(type);");
 			return e1;
 		}
 		TOK op1 = e1.op;
 		
 	    Expression e1old = e1;
 	    
 		e1 = e1.optimize(result, context);
 		if (context.isD2() || (context.isD1() && (result & WANTinterpret) != 0)) {
 			e1 = fromConstInitializer(e1, context);
 		}
 		
 		if (context.isD2()) {
 		    if (e1 == e1old &&
 	    		e1.op == TOKarrayliteral &&
 	    		type.toBasetype(context).ty == Tpointer &&
 	    		e1.type.toBasetype(context).ty != Tsarray) {
 				// Casting this will result in the same expression, and
 				// infinite loop because of Expression::implicitCastTo()
 				return this; // no change
 			}
 		}
 
 		if (context.isD1()) {
 			if ((e1.op == TOKstring || e1.op == TOKarrayliteral)
 					&& (type.ty == Tpointer || type.ty == Tarray)
 					&& type.nextOf().equals(e1.type.nextOf())) {
 				e1.type = type;
 				return e1;
 			}
 		} else {
 			if ((e1.op == TOKstring || e1.op == TOKarrayliteral) && (type.ty == Tpointer || type.ty == Tarray)
 					&& e1.type.nextOf().size(context) == type.nextOf().size(context)) {
 				Expression e = e1.castTo(null, type, context);
 				return e;
 			}
 
 			if (e1.op == TOKstructliteral && e1.type.implicitConvTo(type, context).ordinal() >= MATCHconst.ordinal()) {
 				e1.type = type;
 				return e1;
 			}
 		}
 		/* The first test here is to prevent infinite loops
 		 */
 		if (op1 != TOKarrayliteral && e1.op == TOKarrayliteral) {
 			return e1.castTo(null, to, context);
 		}
 		
 		boolean condition;
 		if (context.isD1()) {
 			condition = e1.op == TOKnull && (type.ty == Tpointer || type.ty == Tclass);
 		} else {
 			condition = e1.op == TOKnull && (type.ty == Tpointer || type.ty == Tclass || type.ty == Tarray);
 		}
 		
 		if (condition) {
 			e1.type = type;
 			return e1;
 		}
 
 		if ((result & WANTflags) != 0 && type.ty == Tclass
 				&& e1.type.ty == Tclass) {
 			// See if we can remove an unnecessary cast
 			ClassDeclaration cdfrom;
 			ClassDeclaration cdto;
 			int[] offset = { 0 };
 
 			cdfrom = e1.type.isClassHandle();
 			cdto = type.isClassHandle();
 			if (cdto.isBaseOf(cdfrom, offset, context) && offset[0] == 0) {
 				e1.type = type;
 				return e1;
 			}
 		}
 		
 		if (context.isD2()) {
 			// We can convert 'head const' to mutable
 			if (to.constOf(context).equals(e1.type.constOf(context)))
 			// if (to.constConv(e1.type) >= MATCHconst)
 			{
 				e1.type = type;
 				return e1;
 			}
 		}
 
 		Expression e;
 
 		if (e1.isConst()) {
 			if (e1.op == TOKsymoff) {
 				if (type.size(context) == e1.type.size(context)
 						&& type.toBasetype(context).ty != Tsarray) {
 					e1.type = type;
 					return e1;
 				}
 				return this;
 			}
 			if (to.toBasetype(context).ty == Tvoid) {
 				e = this;
 			} else {
 				e = Cast(type, to, e1, context);
 			}
 		} else {
 			e = this;
 		}
 		return e;
 	}
 
 	@Override
 	public Expression semantic(Scope sc, SemanticContext context) {
 		Expression e;
 		//BinExp b;
 		//UnaExp u;
 
 		if (type != null) {
 			return this;
 		}
 		
 		super.semantic(sc, context);
 		if (e1.type != null) // if not a tuple
 		{
 			e1 = resolveProperties(sc, e1, context);
 			
			if (context.isD1()) {
 				if (null == to) {
 				    /* Handle cast(const) and cast(immutable), etc.
 				     */
 				    to = e1.type.castMod(mod, context);
 				} else {
 					to = to.semantic(filename, lineNumber, sc, context);
 				}
 			} else {
				to = to.semantic(filename, lineNumber, sc, context);
 			}
 			
 			boolean condition;
 			if (context.isD1()) {
 				condition = true;
 			} else {
 				condition = !to.equals(e1.type);
 			}
 
 			if (condition) {
 				e = op_overload(sc, context);
 				if (e != null) {
 					return e.implicitCastTo(sc, to, context);
 				}
 			}
 
 			Type t1b = null;
 			if (!context.isD1()) {
 				t1b = e1.type.toBasetype(context);
 			}
 
 			Type tob = to.toBasetype(context);
 			
 			if (context.isD1()) {
 				condition = tob.ty == Tstruct && 
 					!tob.equals(e1.type.toBasetype(context)) &&
 				    null == ((TypeStruct)to).sym.search(null, 0, Id.call, 0, context);
 			} else {
 				condition = tob.ty == Tstruct && 
 					!tob.equals(t1b) &&
 				    null == ((TypeStruct)tob).sym.search(null, 0, Id.call, 0, context);
 			}
 			
 			if (condition) 
 			{
 				/* Look to replace:
 				 *	cast(S)t
 				 * with:
 				 *	S(t)
 				 */
 
 				// Rewrite as to.call(e1)
 				e = new TypeExp(filename, lineNumber, to);
 				e = new DotIdExp(filename, lineNumber, e, Id.call);
 				e = new CallExp(filename, lineNumber, e, e1);
 				e = e.semantic(sc, context);
 				return e;
 			}
 		} else if (!context.isD1()) {
 			if (null == to) {
 				if (context.acceptsErrors()) {
 					context.acceptProblem(Problem.newSemanticTypeError(IProblem.CannotCastTuple, this));
 				}
 				to = Type.terror;
 			}
 		}
 		
 		if (!context.isD1()) {
 		    if (context.global.params.safe && !sc.module.safe
 					&& 0 == sc.intypeof) { // Disallow unsafe casts
 				Type tob = to.toBasetype(context);
 				Type t1b = e1.type.toBasetype(context);
 				if (!t1b.isMutable() && tob.isMutable()) {
 					// Cast not mutable to mutable
 					// Lunsafe:
 					return semantic_Lunsafe(sc, e1, to, context);
 				} else if (t1b.isShared() && !tob.isShared()) {
 					// Cast away shared
 					// goto Lunsafe;
 					return semantic_Lunsafe(sc, e1, to, context);
 				} else if (tob.ty == Tpointer) {
 					if (t1b.ty != Tpointer) {
 						// goto Lunsafe;
 						return semantic_Lunsafe(sc, e1, to, context);
 					}
 					Type tobn = tob.nextOf().toBasetype(context);
 					Type t1bn = t1b.nextOf().toBasetype(context);
 
 					if (!t1bn.isMutable() && tobn.isMutable()) {
 						// Cast away pointer to not mutable
 						// goto Lunsafe;
 						return semantic_Lunsafe(sc, e1, to, context);
 					}
 
 					if (t1bn.isShared() && !tobn.isShared()) {
 						// Cast away pointer to shared
 						// goto Lunsafe;
 						return semantic_Lunsafe(sc, e1, to, context);
 					}
 
 					if (tobn.isTypeBasic() != null
 							&& tobn.size(context) < t1bn.size(context)) {
 						// Allow things like casting a long* to an int*
 						;
 					} else if (tobn.ty != Tvoid) {
 						// Cast to a pointer other than void*
 						// goto Lunsafe;
 						return semantic_Lunsafe(sc, e1, to, context);
 					}
 				}
 				// BUG: Check for casting array types, such as void[] to int*[]
 			}
 		}
 		
 		e = e1.castTo(sc, to, context);
 		return e;
 	}
 	
 	private Expression semantic_Lunsafe(Scope sc, Expression e1, Type to, SemanticContext context) {
 		if (context.acceptsErrors()) {
 			context.acceptProblem(Problem.newSemanticTypeError(IProblem.CastNotAllowedInSafeMode, this, e1.type.toChars(context), to.toChars(context)));
 		}
 		
 		return e1.castTo(sc, to, context);
 	}
 
 	@Override
 	public Expression syntaxCopy(SemanticContext context) {
 		if (context.isD1()) {
 			return new CastExp(filename, lineNumber, e1.syntaxCopy(context), to.syntaxCopy(context));
 		} else {
 		    return to != null ? new CastExp(filename, lineNumber, e1.syntaxCopy(context), to.syntaxCopy(context))
 		      : new CastExp(filename, lineNumber, e1.syntaxCopy(context), mod, tok, modifierStart, tok2, modifier2Start);
 		}
 	}
 
 	@Override
 	public void toCBuffer(OutBuffer buf, HdrGenState hgs,
 			SemanticContext context) {
 		buf.writestring("cast(");
 		if (context.isD1()) {
 			to.toCBuffer(buf, null, hgs, context);
 		} else {
 		    if (to != null)
 		    	to.toCBuffer(buf, null, hgs, context);
 		        else
 		        {
 		    	switch (mod)
 		    	{   case 0:
 		    		break;
 		    	    case MODconst:
 		    		buf.writestring(TOKconst.charArrayValue);
 		    		break;
 		    	    case MODinvariant:
 		    		buf.writestring(TOKimmutable.charArrayValue);
 		    		break;
 		    	    case MODshared:
 		    		buf.writestring(TOKshared.charArrayValue);
 		    		break;
 		    	    case MODshared | MODconst:
 		    		buf.writestring(TOKshared.charArrayValue);
 		    		buf.writeByte(' ');
 		    		buf.writestring(TOKconst.charArrayValue);
 		    		break;
 		    	    default:
 		    			throw new IllegalStateException();
 		    	}
 		        }
 		}
 		buf.writeByte(')');
 		expToCBuffer(buf, hgs, e1, op.precedence, context);
 	}
 
 }
