 package cd.semantic.ti;
 
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import cd.ir.ExprVisitorWithoutArg;
 import cd.ir.ast.BinaryOp;
 import cd.ir.ast.BooleanConst;
 import cd.ir.ast.BuiltInRead;
 import cd.ir.ast.BuiltInReadFloat;
 import cd.ir.ast.Cast;
 import cd.ir.ast.Expr;
 import cd.ir.ast.Field;
 import cd.ir.ast.FloatConst;
 import cd.ir.ast.Index;
 import cd.ir.ast.IntConst;
 import cd.ir.ast.MethodCallExpr;
 import cd.ir.ast.NewArray;
 import cd.ir.ast.NewObject;
 import cd.ir.ast.NullConst;
 import cd.ir.ast.ThisRef;
 import cd.ir.ast.UnaryOp;
 import cd.ir.ast.Var;
 import cd.ir.ast.BinaryOp.BOp;
 import cd.ir.ast.UnaryOp.UOp;
 import cd.ir.symbols.ArrayTypeSymbol;
 import cd.ir.symbols.ClassSymbol;
 import cd.ir.symbols.MethodSymbol;
 import cd.ir.symbols.PrimitiveTypeSymbol;
 import cd.ir.symbols.TypeSymbol;
 import cd.ir.symbols.VariableSymbol;
 import cd.semantic.TypeSymbolTable;
 import cd.semantic.ti.constraintSolving.ConstantTypeSet;
 import cd.semantic.ti.constraintSolving.ConstantTypeSetFactory;
 import cd.semantic.ti.constraintSolving.ConstraintSystem;
 import cd.semantic.ti.constraintSolving.TypeSet;
 import cd.semantic.ti.constraintSolving.TypeVariable;
 import cd.semantic.ti.constraintSolving.constraints.ConstraintCondition;
 
 import com.google.common.base.Optional;
 import com.google.common.collect.ImmutableSet;
 
 /**
  * Recursively generates the type set for a expressions and potentially adds
  * type constraints to the constraint system.
  * 
  * The type sets associated with fields, parameters, local variables and return
  * values are looked up in the context.
  */
 public class ExprConstraintGenerator extends ExprVisitorWithoutArg<TypeSet> {
 
 	private final MethodSymbol method;
 	private final ConstraintGeneratorContext context;
 
 	public ExprConstraintGenerator(MethodSymbol method,
 			ConstraintGeneratorContext context) {
 		this.method = method;
 		this.context = context;
 	}
 
 	/**
 	 * Convenience shortcut for {@code context.getConstantTypeSetFactory()}.
 	 */
 	private ConstantTypeSetFactory getTypeSetFactory() {
 		return context.getConstantTypeSetFactory();
 	}
 
 	/**
 	 * Convenience shortcut for {@code context.getConstraintSystem()}.
 	 */
 	private ConstraintSystem getSystem() {
 		return context.getConstraintSystem();
 	}
 
 	/**
 	 * Convenience shortcut for {@link context.getTypeSymbolTable()}.
 	 */
 	private TypeSymbolTable getTypeSymbols() {
 		return context.getTypeSymbolTable();
 	}
 
 	@Override
 	public TypeSet var(Var ast) {
 		return context.getVariableTypeSet(ast.getSymbol());
 	}
 
 	@Override
 	public TypeSet intConst(IntConst ast) {
 		return getTypeSetFactory().makeInt();
 	}
 
 	@Override
 	public TypeSet floatConst(FloatConst ast) {
 		return getTypeSetFactory().makeFloat();
 	}
 
 	@Override
 	public TypeSet booleanConst(BooleanConst ast) {
 		return getTypeSetFactory().makeBoolean();
 	}
 
 	@Override
 	public TypeSet nullConst(NullConst ast) {
		return getTypeSetFactory().makeReferenceTypeSet();
 	}
 
 	@Override
 	public TypeSet newObject(NewObject ast) {
 		return getTypeSetFactory().make(ast.typeName);
 	}
 
 	@Override
 	public TypeSet newArray(NewArray ast) {
 		return getTypeSetFactory().make(ast.typeName);
 	}
 
 	@Override
 	public TypeSet thisRef(ThisRef ast) {
 		return getTypeSetFactory().make(method.getOwner());
 	}
 
 	@Override
 	public TypeSet cast(Cast ast) {
 		TypeSet exprTypeSet = visit(ast.arg());
 		// only reference types can be cast
 		ConstantTypeSet allRefTyes = getTypeSetFactory().makeReferenceTypeSet();
 		getSystem().addUpperBound(exprTypeSet, allRefTyes);
 
 		TypeSymbol castResultType = getTypeSymbols().getType(ast.typeName);
 		return getTypeSetFactory().makeDeclarableSubtypes(castResultType);
 	}
 
 	@Override
 	public TypeSet field(Field ast) {
 		String fieldName = ast.fieldName;
 		TypeSet receiverTypeSet = visit(ast.arg());
 
 		Collection<ClassSymbol> declaringClassSymbols = context
 				.getClassesDeclaringField(fieldName);
 		Set<ClassSymbol> possibleClassSymbols = new HashSet<>();
 
 		TypeVariable resultType = getSystem().addTypeVariable();
 
 		for (ClassSymbol classSym : declaringClassSymbols) {
 			possibleClassSymbols.addAll(getTypeSymbols()
 					.getClassSymbolSubtypes(classSym));
 			VariableSymbol fieldSymbol = classSym.getField(fieldName);
 			TypeSet fieldTypeSet = context.getVariableTypeSet(fieldSymbol);
 			ConstraintCondition condition = new ConstraintCondition(classSym,
 					receiverTypeSet);
 			getSystem().addEquality(resultType, fieldTypeSet, condition);
 		}
 
 		// The receiver *must* be a subtype of any class that has a
 		// field with the right name
 		ConstantTypeSet possibleClassTypeSet = new ConstantTypeSet(
 				possibleClassSymbols);
 		getSystem().addUpperBound(receiverTypeSet, possibleClassTypeSet);
 
 		return resultType;
 	}
 
 	@Override
 	public TypeSet index(Index index) {
 		TypeSet arrayExprTypeSet = visit(index.left());
 		TypeSet indexTypeSet = visit(index.right());
 		TypeVariable resultVar = getSystem().addTypeVariable();
 
 		getSystem().addEquality(indexTypeSet, getTypeSetFactory().makeInt());
 
 		ConstantTypeSet arrayTypesSet = getTypeSetFactory().makeArrayTypeSet();
 		getSystem().addUpperBound(arrayExprTypeSet, arrayTypesSet);
 
 		for (ArrayTypeSymbol arrayType : getTypeSymbols().getArrayTypeSymbols()) {
 			ConstraintCondition condition = new ConstraintCondition(arrayType,
 					arrayExprTypeSet);
 			// Also allow objects in the array whose type is a subtype of the
 			// declared array element type
 			ConstantTypeSet arrayElementTypeSet = getTypeSetFactory()
 					.makeDeclarableSubtypes(arrayType.elementType);
 			getSystem().addEquality(resultVar, arrayElementTypeSet, condition);
 		}
 		return resultVar;
 	}
 
 	@Override
 	public TypeSet builtInRead(BuiltInRead ast) {
 		return getTypeSetFactory().makeInt();
 	}
 
 	@Override
 	public TypeSet builtInReadFloat(BuiltInReadFloat ast) {
 		return getTypeSetFactory().makeFloat();
 	}
 
 	@Override
 	public TypeSet binaryOp(BinaryOp binaryOp) {
 		BOp op = binaryOp.operator;
 		TypeSet leftTypeSet = visit(binaryOp.left());
 		TypeSet rightTypeSet = visit(binaryOp.right());
 
 		ConstantTypeSet booleanTypeSet, numTypeSet;
 		numTypeSet = getTypeSetFactory().makeNumericalTypeSet();
 		booleanTypeSet = getTypeSetFactory().makeBoolean();
 
 		switch (op) {
 		case B_TIMES:
 		case B_DIV:
 		case B_MOD:
 		case B_PLUS:
 		case B_MINUS:
 			getSystem().addUpperBound(leftTypeSet, numTypeSet);
 			getSystem().addEquality(leftTypeSet, rightTypeSet);
 			return leftTypeSet;
 		case B_AND:
 		case B_OR:
 			getSystem().addEquality(leftTypeSet, rightTypeSet);
 			getSystem().addEquality(leftTypeSet, booleanTypeSet);
 			return booleanTypeSet;
 		case B_EQUAL:
 		case B_NOT_EQUAL:
 			// The following only prevents primitive types from being
 			// compared with reference types and different primitive
 			// types. However, it is possible to compare references of
 			// any type, even if neither is a subtype of the other.
 			for (PrimitiveTypeSymbol primitiveType : getTypeSymbols()
 					.getPrimitiveTypeSymbols()) {
 				ConstraintCondition leftCondition = new ConstraintCondition(
 						primitiveType, leftTypeSet);
 				ConstraintCondition rightCondition = new ConstraintCondition(
 						primitiveType, rightTypeSet);
 				ConstantTypeSet primitiveTypeSet = getTypeSetFactory().make(
 						primitiveType);
 				getSystem().addEquality(rightTypeSet, primitiveTypeSet,
 						leftCondition);
 				getSystem().addEquality(leftTypeSet, primitiveTypeSet,
 						rightCondition);
 			}
 			return booleanTypeSet;
 		case B_LESS_THAN:
 		case B_LESS_OR_EQUAL:
 		case B_GREATER_THAN:
 		case B_GREATER_OR_EQUAL:
 			getSystem().addUpperBound(leftTypeSet, numTypeSet);
 			getSystem().addEquality(leftTypeSet, rightTypeSet);
 			return booleanTypeSet;
 		default:
 			throw new IllegalStateException("no such binary operator");
 		}
 	}
 
 	public void createMethodCallConstraints(String methodName, Expr receiver,
 			List<Expr> arguments, Optional<TypeVariable> methodCallResultTypeVar) {
 		// The canonical (non-overriding) method symbols with that name and
 		// number of parameters
 		Collection<MethodSymbol> methodSymbols = context.getMatchingMethods(
 				methodName, arguments.size());
 
 		TypeSet receiverTypeSet = visit(receiver);
 		Set<ClassSymbol> possibleReceiverTypes = new HashSet<>();
 
 		for (MethodSymbol msym : methodSymbols) {
 			ImmutableSet<ClassSymbol> msymClassSubtypes = getTypeSymbols()
 					.getClassSymbolSubtypes(msym.getOwner());
 			possibleReceiverTypes.addAll(msymClassSubtypes);
 
 			for (ClassSymbol msymClassSubtype : msymClassSubtypes) {
 				// Generate a conditional constraint for each of the subtypes of
 				// the method's owner. The receiver type set may only contain a
 				// subtype of the method's owner that does NOT override the
 				// method. Thus, if we only created a constraint whose condition
 				// only checks if the method's owner is in the receiver type
 				// set, the condition would never be satisfied.
 				ConstraintCondition condition = new ConstraintCondition(
 						msymClassSubtype, receiverTypeSet);
 				for (int argNum = 0; argNum < arguments.size(); argNum++) {
 					Expr argument = arguments.get(argNum);
 					TypeSet argTypeSet = visit(argument);
 					VariableSymbol paramSym = msym.getParameter(argNum);
 					TypeSet parameterTypeSet = context
 							.getVariableTypeSet(paramSym);
 					getSystem().addInequality(argTypeSet, parameterTypeSet,
 							condition);
 				}
 
 				if (methodCallResultTypeVar.isPresent()) {
 					TypeSet resultTypeSet = context.getReturnTypeSet(msym);
 					TypeSet lhsTypeSet = methodCallResultTypeVar.get();
 					getSystem().addInequality(resultTypeSet, lhsTypeSet,
 							condition);
 				}
 			}
 		}
 
 		// the receiver _must_ be a subtype of any class that has a
 		// method with the right name and number of arguments
 		ConstantTypeSet possibleReceiverTypeSet = new ConstantTypeSet(
 				possibleReceiverTypes);
 		getSystem().addUpperBound(receiverTypeSet, possibleReceiverTypeSet);
 	}
 
 	@Override
 	public TypeSet methodCall(MethodCallExpr call) {
 		TypeVariable methodCallResultTypeVar = getSystem().addTypeVariable();
 		createMethodCallConstraints(call.methodName, call.receiver(),
 				call.argumentsWithoutReceiver(),
 				Optional.of(methodCallResultTypeVar));
 		return methodCallResultTypeVar;
 	}
 
 	@Override
 	public TypeSet unaryOp(UnaryOp unaryOp) {
 		UOp op = unaryOp.operator;
 		TypeSet subExprTypeSet = visit(unaryOp.arg());
 		ConstantTypeSet numTypes = getTypeSetFactory().makeNumericalTypeSet();
 		ConstantTypeSet booleanType = getTypeSetFactory().makeBoolean();
 
 		switch (op) {
 		case U_BOOL_NOT:
 			getSystem().addEquality(subExprTypeSet, booleanType);
 			return booleanType;
 		case U_MINUS:
 		case U_PLUS:
 			getSystem().addUpperBound(subExprTypeSet, numTypes);
 			break;
 		}
 		return subExprTypeSet;
 	}
 
 }
