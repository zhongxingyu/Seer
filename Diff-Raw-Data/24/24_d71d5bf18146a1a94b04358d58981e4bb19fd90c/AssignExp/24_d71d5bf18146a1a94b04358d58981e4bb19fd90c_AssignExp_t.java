 package descent.internal.compiler.parser;
 
 import melnorme.miscutil.tree.TreeVisitor;
 import descent.core.compiler.IProblem;
 import descent.internal.compiler.parser.ast.IASTVisitor;
 
 // DMD 1.020
 public class AssignExp extends BinExp {
 
 	public boolean ismemset;
 
 	public AssignExp(Loc loc, Expression e1, Expression e2) {
 		super(loc, TOK.TOKassign, e1, e2);
		ismemset = false;
 	}
 
 	@Override
 	public void accept0(IASTVisitor visitor) {
 		boolean children = visitor.visit(this);
 		if (children) {
 			TreeVisitor.acceptChildren(visitor, e1);
 			TreeVisitor.acceptChildren(visitor, e2);
 		}
 		visitor.endVisit(this);
 	}
 
 	@Override
 	public Expression checkToBoolean(SemanticContext context) {
 		context.acceptProblem(Problem.newSemanticTypeError(IProblem.ExpressionDoesNotGiveABooleanResult, this));
 		return this;
 	}
 
 	@Override
 	public int getNodeType() {
 		return ASSIGN_EXP;
 	}
 
 	@Override
 	public Expression interpret(InterState istate, SemanticContext context) {
 		return interpretAssignCommon(istate, null, context);
 	}
 
 	@Override
 	public char[] opId() {
 		return Id.assign;
 	}
 
 	@Override
 	public Expression semantic(Scope sc, SemanticContext context) {
 		/* Look for operator overloading of a[i]=value.
 		 * Do it before semantic() otherwise the a[i] will have been
 		 * converted to a.opIndex() already.
 		 */
 		if (e1.op == TOK.TOKarray) {
 			Type t1;
 			ArrayExp ae = (ArrayExp) e1;
 			AggregateDeclaration ad;
 			char[] id = Id.index;
 
 			ae.e1 = ae.e1.semantic(sc, context);
 			t1 = ae.e1.type.toBasetype(context);
 
 			if (t1.ty == TY.Tstruct || t1.ty == TY.Tclass) {
 				if (t1.ty == TY.Tstruct) {
 					ad = ((TypeStruct) t1).sym;
 				} else // t1.ty == TY.Tclass
 				{
 					ad = ((TypeClass) t1).sym;
 				}
 
 				// Rewrite (a[i] = value) to (a.opIndexAssign(value, i))
 				if (null != search_function(ad, Id.indexass, context)) {
 					Expression e = new DotIdExp(loc, ae.e1, new IdentifierExp(
 							Loc.ZERO, Id.indexass));
 					Expressions a = new Expressions(ae.arguments);
 
 					a.add(0, e2);
 					e = new CallExp(loc, e, a);
 					e = e.semantic(sc, context);
 					return e;
 				} else {
 					// Rewrite (a[i] = value) to (a.opIndex(i, value))
 					if (null != search_function(ad, id, context)) {
 						Expression e = new DotIdExp(loc, ae.e1,
 								new IdentifierExp(Loc.ZERO, id));
 
 						context.acceptProblem(Problem.newSemanticTypeError(
 								IProblem.OperatorAssignmentOverloadWithOpIndexIllegal, this));
 
 						e = new CallExp(loc, e, ae.arguments.get(0), e2);
 						e = e.semantic(sc, context);
 						return e;
 					}
 				}
 			}
 		}
 
 		/* Look for operator overloading of a[i..j]=value.
 		 * Do it before semantic() otherwise the a[i..j] will have been
 		 * converted to a.opSlice() already.
 		 */
 		if (e1.op == TOK.TOKslice) {
 			Type t1;
 			SliceExp ae = (SliceExp) e1;
 			AggregateDeclaration ad;
 
 			ae.e1 = ae.e1.semantic(sc, context);
 			ae.e1 = resolveProperties(sc, ae.e1, context);
 			t1 = ae.e1.type.toBasetype(context);
 
 			if (t1.ty == TY.Tstruct || t1.ty == TY.Tclass) {
 				if (t1.ty == TY.Tstruct) {
 					ad = ((TypeStruct) t1).sym;
 
 				} else // t1.ty == TY.Tclass
 				{
 					ad = ((TypeClass) t1).sym;
 				}
 
 				// Rewrite (a[i..j] = value) to (a.opIndexAssign(value, i, j))
 				if (null != search_function(ad, Id.sliceass, context)) {
 					Expression e = new DotIdExp(loc, ae.e1, new IdentifierExp(
 							Loc.ZERO, Id.sliceass));
 					Expressions a = new Expressions();
 
 					a.add(e2);
 					if (null != ae.lwr) {
 						a.add(ae.lwr);
 						assert (null != ae.upr);
 						a.add(ae.upr);
 					} else {
 						assert (null == ae.upr);
 					}
 
 					e = new CallExp(loc, e, a);
 					e = e.semantic(sc, context);
 					return e;
 				}
 			}
 		}
 
 		Expression e1old = e1;
 		Type t1;
 
 		super.semantic(sc, context);
 		e2 = resolveProperties(sc, e2, context);
 		assert (null != e1.type);
 
 		t1 = e1.type.toBasetype(context);
 
 		if (t1.ty == TY.Tfunction) {
 			// Rewrite f=value to f(value)
 			Expression e;
 
 			e = new CallExp(loc, e1, e2);
 			e = e.semantic(sc, context);
 			return e;
 		}
 
 		/* If it is an assignment from a 'foreign' type,
 		 * check for operator overloading.
 		 */
 		if (t1.ty == TY.Tclass || t1.ty == TY.Tstruct) {
 			if (MATCH.MATCHnomatch == e2.type.implicitConvTo(e1.type, context)) {
 				Expression e = op_overload(sc, context);
 				if (null != e) {
 					return e;
 				}
 			}
 		}
 
 		e2.rvalue(context);
 		if (e1.op == TOK.TOKarraylength) {
 			// e1 is not an lvalue, but we let code generator handle it
 			ArrayLengthExp ale = (ArrayLengthExp) e1;
 			ale.e1 = ale.e1.modifiableLvalue(sc, null, context);
 		} else if (e1.op == TOK.TOKslice) {
 			;
 		} else {
 			// Try to do a decent error message with the expression
 			// before it got constant folded
 			e1 = e1.modifiableLvalue(sc, e1old, context);
 		}
 
 		if (e1.op == TOK.TOKslice && null != t1.next
 				&& !(t1.next.equals(e2.type.next))) { // memset
 			e2 = e2.implicitCastTo(sc, t1.next, context);
 		} else if (t1.ty == TY.Tsarray) {
 			context.acceptProblem(Problem.newSemanticTypeError(IProblem.CannotAssignToStaticArray, this, new String[] { e1.toChars(context) }));
 		} else {
 			e2 = e2.implicitCastTo(sc, e1.type, context);
 		}
 
 		type = e1.type;
 		assert (null != type);
 		
 		return this;
 	}
 
 }
