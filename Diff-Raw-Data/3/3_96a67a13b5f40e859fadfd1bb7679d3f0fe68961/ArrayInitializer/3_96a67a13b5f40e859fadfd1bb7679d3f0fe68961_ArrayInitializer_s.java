 package descent.internal.compiler.parser;
 
 import java.math.BigInteger;
 
 import melnorme.miscutil.tree.TreeVisitor;
 import descent.core.compiler.IProblem;
 import descent.internal.compiler.parser.ast.IASTVisitor;
 
 public class ArrayInitializer extends Initializer {
 	
 	private final static integer_t amax = new integer_t(new BigInteger("80000000", 16));
 
 	public Expressions index, sourceIndex;
 	public Initializers value, sourceValue;
 	public long dim; // length of array being initialized
 	public Type type; // type that array will be used to initialize
 	public int sem; // !=0 if semantic() is run
 
 	public ArrayInitializer(Loc loc) {
 		super(loc);
 	}
 
 	@Override
 	public void accept0(IASTVisitor visitor) {
 		boolean children = visitor.visit(this);
 		if (children) {
 			TreeVisitor.acceptChildren(visitor, sourceIndex);
 			TreeVisitor.acceptChildren(visitor, sourceValue);
 		}
 		visitor.endVisit(this);
 	}
 
 	public void addInit(Expression index, Initializer value) {
 		if (this.index == null) {
 			this.index = new Expressions();
 			this.value = new Initializers();
 			this.sourceIndex = new Expressions();
 			this.sourceValue = new Initializers();
 		}
 		this.index.add(index);
 		this.value.add(value);
 		this.sourceIndex.add(index);
 		this.sourceValue.add(value);
 		dim = 0;
 		type = null;
 	}
 
 	@Override
 	public int getNodeType() {
 		return ARRAY_INITIALIZER;
 	}
 
 	@Override
 	public Type inferType(Scope sc, SemanticContext context) {
 		if (value != null) {
 			for (int i = 0; i < value.size(); i++) {
 				if (index.get(i) != null) {
 					// goto Lno;
 					if (context.acceptsErrors()) {
 						context.acceptProblem(Problem.newSemanticTypeError(
 								IProblem.CannotInferTypeFromThisArrayInitializer, this));
 					}
 					return Type.terror;
 				}
 			}
 			if (value.size() > 0) {
 				Initializer iz = value.get(0);
 				if (iz != null) {
 					Type t = iz.inferType(sc, context);
 					t = new TypeSArray(t, new IntegerExp(iz.loc, value.size()), context.encoder);
 					t = t.semantic(loc, sc, context);
 					return t;
 				}
 			}
 		}
 
 		if (context.acceptsErrors()) {
 			context.acceptProblem(Problem.newSemanticTypeError(
 					IProblem.CannotInferTypeFromThisArrayInitializer, this));
 		}
 		return Type.terror;
 	}
 
 	@Override
 	public Initializer semantic(Scope sc, Type t, SemanticContext context) {
 		int i;
 		long length;
 
 		if (sem != 0) {
 			return this;
 		}
 		sem = 1;
 		type = t;
 		t = t.toBasetype(context);
 		switch (t.ty) {
 		case Tpointer:
 		case Tsarray:
 		case Tarray:
 			break;
 
 		default:
 			if (context.acceptsErrors()) {
 				context.acceptProblem(Problem.newSemanticTypeError(IProblem.CannotUseArrayToInitialize, this, type.toChars(context)));
 			}
 			return this;
 		}
 
 		length = 0;
 		for (i = 0; i < size(index); i++) {
 			Expression idx;
 			Initializer val;
 
 			idx = index.get(i);
 			if (idx != null) {
 				idx = idx.semantic(sc, context);
 				idx = idx.optimize(WANTvalue | WANTinterpret, context);
 				index.set(i, idx);
 				length = idx.toInteger(context).longValue();
 			}
 
 			val = value.get(i);
			if (val == null || t == null) {
				System.out.println();
			}
 			
 			val = val.semantic(sc, t.nextOf(), context);
 			value.set(i, val);
 			length++;
 			// This was length == 0 in DMD, with length
 			// an unsigned. So in a long, it's:
 			if (length == (Integer.MAX_VALUE + 1) * 2) {
 				if (context.acceptsErrors()) {
 					context.acceptProblem(Problem.newSemanticTypeError(
 							IProblem.ArrayDimensionOverflow, this));
 				}
 			}
 			if (length > dim) {
 				dim = length;
 			}
 		}
 		
 	    if (new integer_t(dim).multiply(t.nextOf().size(context)).compareTo(amax) >= 0) {
 	    	if (context.acceptsErrors()) {
 	    		context.acceptProblem(Problem.newSemanticTypeError(IProblem.ArrayDimensionExceedsMax, this, String.valueOf(dim), amax.divide(t.nextOf().size(context)).toString() ));
 	    	}
 	    }
 		return this;
 	}
 
 	@Override
 	public Initializer syntaxCopy(SemanticContext context) {
 		ArrayInitializer ai = new ArrayInitializer(loc);
 
 		if (!(size(index) == size(value))) {
 			throw new IllegalStateException("assert(index.dim == value.dim);");
 		}
 
 		ai.index = new Expressions(size(index));
 		ai.value = new Initializers(size(value));
 		
 		ai.index.setDim(size(index));
 		ai.value.setDim(size(value));
 		for (int i = 0; i < size(ai.value); i++) {
 			Expression e = index.get(i);
 			if (e != null) {
 				e = e.syntaxCopy(context);
 			}
 			ai.index.set(i, e);
 
 			Initializer init = value.get(i);
 			init = init.syntaxCopy(context);
 			ai.value.set(i, init);
 		}
 		return ai;
 	}
 
 	public Initializer toAssocArrayInitializer(SemanticContext context) {
 		Expressions keys;
 		Expressions values;
 		Expression e;
 
 		keys = new Expressions();
 		keys.setDim(value.size());
 		values = new Expressions();
 		values.setDim(value.size());
 
 		for (int i = 0; i < value.size(); i++) {
 			e = index.get(i);
 			if (null == e) {
 				// goto Lno;
 				return toAssocArrayInitializer_Lno(context);
 			}
 			keys.set(i, e);
 
 			Initializer iz = value.get(i);
 			if (null == iz) {
 				// goto Lno;
 				return toAssocArrayInitializer_Lno(context);
 			}
 			e = iz.toExpression(context);
 			if (null == e) {
 				// goto Lno;
 				return toAssocArrayInitializer_Lno(context);
 			}
 			values.set(i, e);
 		}
 		e = new AssocArrayLiteralExp(loc, keys, values);
 		return new ExpInitializer(loc, e);
 	}
 
 	private Initializer toAssocArrayInitializer_Lno(SemanticContext context) {
 		if (context.acceptsErrors()) {
 			context.acceptProblem(Problem.newSemanticTypeError(IProblem.NotAnAssociativeArrayInitializer, this));
 		}
 		return this;
 	}
 
 	@Override
 	public void toCBuffer(OutBuffer buf, HdrGenState hgs,
 			SemanticContext context) {
 		buf.writebyte('[');
 		for (int i = 0; i < index.size(); i++) {
 			if (i > 0) {
 				buf.writebyte(',');
 			}
 			Expression ex = index.get(i);
 			if (ex != null) {
 				ex.toCBuffer(buf, hgs, context);
 				buf.writebyte(':');
 			}
 			Initializer iz = value.get(i);
 			if (iz != null) {
 				iz.toCBuffer(buf, hgs, context);
 			}
 		}
 		buf.writebyte(']');
 	}
 
 	@Override
 	public Expression toExpression(SemanticContext context) {
 		Expressions elements;
 		Expression e;
 
 		elements = new Expressions();
 		for (int i = 0; i < size(value); i++) {
 			if (index.get(i) != null) {
 				// goto Lno;
 				return toExpression_Lno(context);
 			}
 			Initializer iz = value.get(i);
 			if (null == iz) {
 				// goto Lno;
 				return toExpression_Lno(context);
 			}
 			Expression ex = iz.toExpression(context);
 			if (null == ex) {
 				// goto Lno;
 				return toExpression_Lno(context);
 			}
 			elements.add(ex);
 		}
 		e = new ArrayLiteralExp(loc, elements);
 		e.type = type;
 		e.copySourceRange(this);
 		return e;
 	}
 	
 	private Expression toExpression_Lno(SemanticContext context) {
 		if (context.acceptsErrors()) {
 			context.acceptProblem(Problem.newSemanticTypeError(IProblem.ArrayInitializersAsExpressionsNotAllowed, this));
 		}
 		return null;
 	}
 
 	@Override
 	public ArrayInitializer isArrayInitializer() {
 		return this;
 	}
 
 }
