 package descent.internal.compiler.parser;
 
 import melnorme.miscutil.tree.TreeVisitor;
 import descent.core.compiler.IProblem;
 import descent.internal.compiler.parser.ast.IASTVisitor;
 import static descent.internal.compiler.parser.DYNCAST.DYNCAST_DSYMBOL;
 import static descent.internal.compiler.parser.DYNCAST.DYNCAST_EXPRESSION;
 import static descent.internal.compiler.parser.DYNCAST.DYNCAST_TYPE;
 
 import static descent.internal.compiler.parser.MATCH.MATCHconvert;
 import static descent.internal.compiler.parser.MATCH.MATCHexact;
 import static descent.internal.compiler.parser.MATCH.MATCHnomatch;
 
 import static descent.internal.compiler.parser.TY.Taarray;
 import static descent.internal.compiler.parser.TY.Tarray;
 import static descent.internal.compiler.parser.TY.Tbit;
 import static descent.internal.compiler.parser.TY.Tchar;
 import static descent.internal.compiler.parser.TY.Tclass;
 import static descent.internal.compiler.parser.TY.Tdchar;
 import static descent.internal.compiler.parser.TY.Tident;
 import static descent.internal.compiler.parser.TY.Tpointer;
 import static descent.internal.compiler.parser.TY.Tsarray;
 import static descent.internal.compiler.parser.TY.Tvoid;
 import static descent.internal.compiler.parser.TY.Twchar;
 
 // DMD 1.020
 public class TypeSArray extends TypeArray {
 
 	public Expression dim, sourceDim;
 
 	public TypeSArray(Type next, Expression dim) {
 		super(TY.Tsarray, next);
 		this.dim = dim;
 		this.sourceDim = dim;
 	}
 
 	@Override
 	public void accept0(IASTVisitor visitor) {
 		boolean children = visitor.visit(this);
 		if (children) {
 			TreeVisitor.acceptChildren(visitor, next);
 			TreeVisitor.acceptChildren(visitor, dim);
 		}
 		visitor.endVisit(this);
 	}
 
 	@Override
 	public int alignsize(SemanticContext context) {
 		return next.alignsize(context);
 	}
 
 	@Override
 	public MATCH deduceType(Scope sc, Type tparam,
 			TemplateParameters parameters, Objects dedtypes,
 			SemanticContext context) {
 		// Extra check that array dimensions must match
 		if (null != tparam) {
 			if (tparam.ty == Tsarray) {
 				TypeSArray tp = (TypeSArray) tparam;
 				if (dim.toInteger(context) != tp.dim.toInteger(context))
 					return MATCHnomatch;
 			}
 
 			else if (tparam.ty == Taarray) {
 				TypeAArray tp = (TypeAArray) tparam;
 				if (tp.index.ty == Tident) {
 					TypeIdentifier tident = (TypeIdentifier) tp.index;
 
 					if (tident.idents.size() == 0) {
 						IdentifierExp id = tident.ident;
 
 						for (int i = 0; i < parameters.size(); i++) {
 							TemplateParameter $tp = (TemplateParameter) parameters
 									.get(i);
 
 							if ($tp.ident.equals(id)) { // Found the corresponding template parameter
 								TemplateValueParameter tvp = $tp
 										.isTemplateValueParameter();
 								if (null == tvp || !tvp.valType.isintegral())
 									return MATCHnomatch; // goto Lnomatch;
 
 								if (null != dedtypes.get(i)) {
 									if (!dim.equals(dedtypes.get(i)))
 										return MATCHnomatch; // goto Lnomatch;
 								} else {
 									dedtypes.set(i, dim);
 								}
 								return next.deduceType(sc, tparam.nextOf(),
 										parameters, dedtypes, context);
 							}
 						}
 					}
 				}
 			}
 
 			else if (tparam.ty == Tarray) {
 				MATCH m;
 
 				m = next.deduceType(sc, tparam.nextOf(), parameters, dedtypes,
 						context);
 				if (m == MATCHexact)
 					m = MATCHconvert;
 				return m;
 			}
 		}
 
 		return super.deduceType(sc, tparam, parameters, dedtypes, context);
 	}
 
 	@Override
 	public Expression defaultInit(SemanticContext context) {
 		return next.defaultInit(context);
 	}
 
 	@Override
 	public Expression dotExp(Scope sc, Expression e, IdentifierExp ident,
 			SemanticContext context) {
 		if (equals(ident, Id.length)) {
 			e = dim;
 		} else if (equals(ident, Id.ptr)) {
 			e = e.castTo(sc, next.pointerTo(context), context);
 		} else {
 			e = super.dotExp(sc, e, ident, context);
 		}
 		return e;
 	}
 
 	@Override
 	public int getNodeType() {
 		return TYPE_S_ARRAY;
 	}
 
 	@Override
 	public TypeInfoDeclaration getTypeInfoDeclaration(SemanticContext context) {
 		return new TypeInfoStaticArrayDeclaration(this, context);
 	}
 
 	@Override
 	public boolean hasPointers(SemanticContext context) {
 		return next.hasPointers(context);
 	}
 
 	@Override
 	public MATCH implicitConvTo(Type to, SemanticContext context) {
 		// printf("TypeSArray.implicitConvTo()\n");
 
 		// Allow implicit conversion of static array to pointer or dynamic array
 		if ((context.global.params.useDeprecated && to.ty == Tpointer)
 				&& (to.next.ty == Tvoid || next.equals(to.next)
 				/* || to.next.isBaseOf(next) */)) {
 			return MATCHconvert;
 		}
 		if (to.ty == Tarray) {
 			int offset = 0;
 
 			if (next.equals(to.next)
 					|| (to.next.isBaseOf(next, new int[] { offset }, context) && offset == 0)
 					|| to.next.ty == Tvoid)
 				return MATCHconvert;
 		}
 		return super.implicitConvTo(to, context);
 	}
 
 	@Override
 	public boolean isString(SemanticContext context) {
 		TY nty = next.toBasetype(context).ty;
 		return nty == Tchar || nty == Twchar || nty == Tdchar;
 	}
 
 	@Override
 	public boolean isZeroInit(SemanticContext context) {
 		return next.isZeroInit(context);
 	}
 
 	@Override
 	public int memalign(int salign, SemanticContext context) {
 		return next.memalign(salign, context);
 	}
 
 	@Override
 	public void resolve(Loc loc, Scope sc, Expression[] pe, Type[] pt,
 			Dsymbol[] ps, SemanticContext context) {
 		// printf("TypeSArray.resolve() %s\n", toChars());
 		next.resolve(loc, sc, pe, pt, ps, context);
 		// printf("s = %p, e = %p, t = %p\n", ps, pe, pt);
 		if (null != pe[0]) { // It's really an index expression
 			Expression e;
 			e = new IndexExp(loc, pe[0], dim);
 			pe[0] = e;
 		} else if (null != ps[0]) {
 			Dsymbol s = ps[0];
 			TupleDeclaration td = s.isTupleDeclaration();
 			if (null != td) {
 				ScopeDsymbol sym = new ArrayScopeSymbol(td);
 				sym.parent = sc.scopesym;
 				sc = sc.push(sym);
 
 				dim = dim.semantic(sc, context);
 				dim = dim.optimize(WANTvalue | WANTinterpret, context);
 				int d = dim.toUInteger(context).intValue();
 
 				sc = sc.pop();
 
 				if (d >= td.objects.size()) {
 					context.acceptProblem(Problem.newSemanticTypeError(
 							IProblem.TupleIndexExceedsBounds, this, new String[] { String.valueOf(d), String.valueOf(td.objects.size()) }));
 					super.resolve(loc, sc, pe, pt, ps, context); // goto
 					// Ldefault;
 				}
 				ASTDmdNode o = (ASTDmdNode) td.objects.get(d);
 				if (o.dyncast() == DYNCAST_DSYMBOL) {
 					ps[0] = (Dsymbol) o;
 					return;
 				}
 				if (o.dyncast() == DYNCAST_EXPRESSION) {
 					ps[0] = null;
 					pe[0] = (Expression) o;
 					return;
 				}
 
 				/*
 				 * Create a new TupleDeclaration which is a slice [d..d+1] out
 				 * of the old one. Do it this way because
 				 * TemplateInstance.semanticTiargs() can handle unresolved
 				 * Objects this way.
 				 */
 				Objects objects = new Objects(1);
 				objects.add(o);
 
 				TupleDeclaration tds = new TupleDeclaration(loc, td.ident,
 						objects);
 				ps[0] = tds;
 			} else
 				super.resolve(loc, sc, pe, pt, ps, context); // goto
 			// Ldefault;
 		} else {
 			// Ldefault:
 			super.resolve(loc, sc, pe, pt, ps, context);
 		}
 	}
 
 	@Override
 	public Type semantic(Loc loc, Scope sc, SemanticContext context) {
 		Type t = null;
 		Expression e = null;
 		Dsymbol s = null;
 		next.resolve(loc, sc, new Expression[] { e }, new Type[] { t },
 				new Dsymbol[] { s }, context);
 		if (null != dim && null != s && null != s.isTupleDeclaration()) {
 			TupleDeclaration sd = s.isTupleDeclaration();
 
 			dim = semanticLength(sc, sd, dim, context);
 			dim = dim.optimize(WANTvalue | WANTinterpret, context);
 			int d = dim.toUInteger(context).intValue();
 
 			if (d >= sd.objects.size()) {
 				context.acceptProblem(Problem.newSemanticTypeError(
 						IProblem.TupleIndexExceedsBounds, this, new String[] { String.valueOf(d), String.valueOf(sd.objects.size()) }));
 				return Type.terror;
 			}
 			ASTDmdNode o = (ASTDmdNode) sd.objects.get(d);
 			if (o.dyncast() != DYNCAST_TYPE) {
 				context.acceptProblem(Problem.newSemanticTypeError(IProblem.SymbolNotAType, this, new String[] { toChars(context) }));
 				return Type.terror;
 			}
 			t = (Type) o;
 			return t;
 		}
 
 		next = next.semantic(loc, sc, context);
 		Type tbn = next.toBasetype(context);
 
 		if (null != dim) {
 			long n, n2;
 
 			dim = semanticLength(sc, tbn, dim, context);
 
 			dim = dim.optimize(WANTvalue | WANTinterpret, context);
 			int d1 = dim.toInteger(context).intValue();
 			dim = dim.castTo(sc, tsize_t, context);
 			dim = dim.optimize(WANTvalue, context);
 			int d2 = dim.toInteger(context).intValue();
 
 			if (d1 != d2) {
 				// goto Loverflow;
 				context.acceptProblem(Problem.newSemanticTypeError(IProblem.IndexOverflowForStaticArray, sourceDim, new String[] { String.valueOf(d1) }));
 				dim = new IntegerExp(Loc.ZERO, 1, tsize_t);
 			}
 
 			if (tbn.isintegral() || tbn.isfloating() || tbn.ty == Tpointer
 					|| tbn.ty == Tarray || tbn.ty == Tsarray
 					|| tbn.ty == Taarray || tbn.ty == Tclass) {
 				/* Only do this for types that don't need to have semantic()
 				 * run on them for the size, since they may be forward referenced.
 				 */
 				n = tbn.size(loc, context);
 				n2 = n * d2;
 				if ((int) n2 < 0) {
 					//goto Loverflow;
 					context.acceptProblem(Problem.newSemanticTypeError(IProblem.IndexOverflowForStaticArray, sourceDim, new String[] { String.valueOf(d1) }));
 					dim = new IntegerExp(Loc.ZERO, 1, tsize_t);
 				}
 				if (n2 >= 0x1000000) // put a 'reasonable' limit on it
 				{
 					//goto Loverflow;
 					context.acceptProblem(Problem.newSemanticTypeError(IProblem.IndexOverflowForStaticArray, sourceDim, new String[] { String.valueOf(d1) }));
 					dim = new IntegerExp(Loc.ZERO, 1, tsize_t);
 				}
 				if (n != 0 && ((n2 / n) != d2)) {
 					//Loverflow:
 					context.acceptProblem(Problem.newSemanticTypeError(IProblem.IndexOverflowForStaticArray, sourceDim, new String[] { String.valueOf(d1) }));
 					dim = new IntegerExp(Loc.ZERO, 1, tsize_t);
 				}
 			}
 		}
 
 		switch (tbn.ty) {
 		case Ttuple: { // Index the tuple to get the type
 			assert (null != dim);
 			TypeTuple tt = (TypeTuple) tbn;
 			int d = dim.toUInteger(context).intValue();
 
 			if (d >= tt.arguments.size()) {
 				context.acceptProblem(Problem.newSemanticTypeError(
 						IProblem.TupleIndexExceedsBounds, this, new String[] { String.valueOf(d), String.valueOf(tt.arguments.size()) }));
 				return Type.terror;
 			}
 			Argument arg = (Argument) tt.arguments.get(d);
 			return arg.type;
 		}
 		case Tfunction:
 		case Tnone:
 			context.acceptProblem(Problem.newSemanticTypeError(IProblem.CannotHaveArrayOfType, this, new String[] { tbn.toChars(context) }));
 			tbn = next = tint32;
 			break;
 		}
 		if (tbn.isauto()) {
 			context.acceptProblem(Problem.newSemanticTypeError(
 					IProblem.CannotHaveArrayOfAuto, this, new String[] { tbn.toChars(context) }));
 		}
 
 		return merge(context);
 	}
 
 	@Override
 	public int size(Loc loc, SemanticContext context) {
 		int sz;
 
 		if (null == dim)
 			return super.size(loc, context);
 		sz = dim.toInteger(context).intValue();
 		if (next.toBasetype(context).ty == Tbit) // if array of bits
 		{
 			if (sz + 31 < sz) {
 				// goto Loverflow;
 				context.acceptProblem(Problem.newSemanticTypeError(IProblem.IndexOverflowForStaticArray, sourceDim, new String[] { String.valueOf(sz) }));
 				return 1;
 			}
 			sz = ((sz + 31) & ~31) / 8; // size in bytes, rounded up to 32 bit
 			// dwords
 		} else {
 			int n, n2;
 
 			n = next.size(context);
 			n2 = n * sz;
 			if ((n != 0) && (n2 / n) != sz) {
 				// goto Loverflow;
 				context.acceptProblem(Problem.newSemanticTypeError(IProblem.IndexOverflowForStaticArray, sourceDim, new String[] { String.valueOf(sz) }));
 				return 1;
 			}
 			sz = n2;
 		}
 		return sz;
 	}
 
 	@Override
 	public Type syntaxCopy() {
 		Type t = next.syntaxCopy();
 		Expression e = dim.syntaxCopy();
 		t = new TypeSArray(t, e);
 		return t;
 	}
 
 	@Override
 	public void toDecoBuffer(OutBuffer buf, SemanticContext context) {
		/* TODO buf.writeByte(mangleChar[ty.ordinal(]); */
 		if (null != dim)
 			buf.printf(dim.toInteger(context) + "u");
 		if (null != next)
 			next.toDecoBuffer(buf, context);
 	}
 
 	@Override
 	public Expression toExpression() {
 		Expression e = next.toExpression();
 		if (e != null) {
 			Expressions arguments = new Expressions(1);
 			arguments.add(dim);
 			e = new ArrayExp(dim.loc, e, arguments);
 			e.setSourceRange(start, length);
 		}
 		return e;
 	}
 
 	@Override
 	public void toPrettyBracket(OutBuffer buf, HdrGenState hgs,
 			SemanticContext context) {
 		buf.writestring("[");
 		buf.writestring(dim.toChars(context));
 		buf.writestring("]");
 	}
 
 	@Override
 	public void toTypeInfoBuffer(OutBuffer buf, SemanticContext context) {
		/* TODO buf.writeByte(mangleChar[Tarray.ordinal()]); */
 		if (null != next)
 			next.toTypeInfoBuffer(buf, context);
 	}
 
 	// PERHAPS type *toCtype();
 	// PERHAPS type *toCParamtype();
 	// PERHAPS dt_t **toDt(dt_t **pdt);
 }
