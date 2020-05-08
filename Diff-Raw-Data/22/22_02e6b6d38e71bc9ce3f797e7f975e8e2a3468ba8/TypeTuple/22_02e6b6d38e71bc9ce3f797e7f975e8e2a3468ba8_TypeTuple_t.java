 package descent.internal.compiler.parser;
 
 import descent.core.compiler.IProblem;
 import descent.internal.compiler.parser.ast.IASTVisitor;
 
 import static descent.internal.compiler.parser.STC.STCin;
 
 import static descent.internal.compiler.parser.TY.Ttuple;
 
 // DMD 1.020
 public class TypeTuple extends Type {
 
 	public Arguments arguments;
 
 	private TypeTuple() {
 		super(TY.Ttuple, null);
 	}
 
 	public TypeTuple(Arguments arguments) {
 		super(Ttuple, null);
 		this.arguments = arguments;
 	}
 	
 	public TypeTuple(Expressions exps, SemanticContext context) {
 		super(Ttuple, null);
 		Arguments arguments = new Arguments();
 		if (exps != null) {
 			arguments.setDim(exps.size());
 			for (int i = 0; i < exps.size(); i++) {
 				Expression e = exps.get(i);
 				if (e.type.ty == Ttuple) {
 					context.acceptProblem(Problem.newSemanticTypeError(
 							IProblem.CannotFormTupleOfTuples, e, new String[] { toChars(context) }));
 				}
 				Argument arg = new Argument(STCin, e.type, null, null);
 				arguments.set(i, arg);
 			}
 		}
 		this.arguments = arguments;
 	}
 
 	@Override
 	public void accept0(IASTVisitor visitor) {
 		visitor.visit(this);
 		visitor.endVisit(this);
 	}
 
 	@Override
 	public boolean equals(Object o) {
 		if (this == o)
 			return true;
 		if (null == o)
 			return false;
 		if (!(o instanceof Type))
 			return false;
 
 		Type t = (Type) o;
 		// printf("TypeTuple.equals(%s, %s)\n", toChars(), t.toChars());
 		if (t.ty == Ttuple) {
 			TypeTuple tt = (TypeTuple) t;
 
 			if (arguments.size() == tt.arguments.size()) {
 				for (int i = 0; i < tt.arguments.size(); i++) {
 					Argument arg1 = (Argument) arguments.get(i);
 					Argument arg2 = (Argument) tt.arguments.get(i);
 
 					if (!arg1.type.equals(arg2.type))
 						return false;
 				}
 				return true;
 			}
 		}
 		return false;
 	}
 
 	@Override
 	public int getNodeType() {
 		return TYPE_TUPLE;
 	}
 
 	@Override
 	public Expression getProperty(Loc loc, char[] ident, int start, int length,
 			SemanticContext context) {
 		Expression e;
 
 		if (equals(ident, Id.length)) {
 			e = new IntegerExp(loc, arguments.size(), Type.tsize_t);
 		} else {
 			context.acceptProblem(Problem.newSemanticTypeError(
 					IProblem.NoPropertyForTuple, this, new String[] { new String(ident),
 							toChars(context) }));
 			e = new IntegerExp(loc, 1, Type.tint32);
 		}
 		return e;
 	}
 
 	@Override
 	public TypeInfoDeclaration getTypeInfoDeclaration(SemanticContext context) {
 		return new TypeInfoTupleDeclaration(this, context);
 	}
 
 	@Override
 	public Type reliesOnTident() {
 		if (null != arguments) {
 			for (int i = 0; i < arguments.size(); i++) {
 				Argument arg = (Argument) arguments.get(i);
 				Type t = arg.type.reliesOnTident();
 				if (null != t)
 					return t;
 			}
 		}
 		return null;
 	}
 
 	@Override
 	public Type semantic(Loc loc, Scope sc, SemanticContext context) {
 		if (null == deco)
 			deco = merge(context).deco;
 
 		/* Don't return merge(), because a tuple with one type has the
 		 * same deco as that type.
 		 */
 		return this;
 	}
 
 	@Override
 	public Type syntaxCopy() {
 		Arguments args = Argument.arraySyntaxCopy(arguments);
 		Type t = TypeTuple.newArguments(args);
 		return t;
 	}
 
 	@Override
 	public void toCBuffer2(OutBuffer buf, IdentifierExp ident, HdrGenState hgs,
 			SemanticContext context) {
 		OutBuffer buf2 = new OutBuffer();
 		argsToCBuffer(buf2, hgs, arguments, 0, context);
 		buf.prependstring(buf2.toChars());
 		if (ident != null) {
 			buf.writeByte(' ');
 			buf.writestring(ident.toChars());
 		}
 	}
 
 	@Override
 	public void toDecoBuffer(OutBuffer buf, SemanticContext context) {
 		 OutBuffer buf2 = new OutBuffer();
 		 Argument.argsToDecoBuffer(buf2, arguments, context);
 		 int len = buf2.data.length();
		 buf.printf("" + ty.mangleChar + len + buf2.extractData());
 	}
 
 	public static TypeTuple newArguments(Arguments arguments) {
 		TypeTuple tt = new TypeTuple();
 		tt.arguments = arguments;
 		return tt;
 	}
 
 	public static TypeTuple newExpressions(Expressions exps,
 			SemanticContext context) {
 		TypeTuple tt = new TypeTuple();
 		Arguments arguments = new Arguments();
 		if (exps != null) {
 			arguments.setDim(exps.size());
 			for (int i = 0; i < exps.size(); i++) {
 				Expression e = exps.get(i);
 				if (e.type.ty == TY.Ttuple) {
 					context.acceptProblem(Problem.newSemanticTypeError(
 							IProblem.CannotFormTupleOfTuples, e));
 				}
 				Argument arg = new Argument(STCin, e.type, null, null);
 				arguments.set(i, arg);
 			}
 		}
 		tt.arguments = arguments;
 		return tt;
 	}
 	
 
 }
