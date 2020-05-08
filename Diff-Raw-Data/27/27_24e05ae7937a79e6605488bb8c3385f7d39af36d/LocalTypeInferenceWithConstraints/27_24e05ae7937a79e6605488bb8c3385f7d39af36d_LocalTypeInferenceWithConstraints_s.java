 package cd.semantic.ti;
 
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import cd.exceptions.SemanticFailure;
 import cd.exceptions.SemanticFailure.Cause;
 import cd.ir.AstVisitor;
 import cd.ir.ExprVisitor;
 import cd.ir.ExprVisitorWithoutArg;
 import cd.ir.ast.Assign;
 import cd.ir.ast.BinaryOp;
 import cd.ir.ast.BinaryOp.BOp;
 import cd.ir.ast.BooleanConst;
 import cd.ir.ast.BuiltInRead;
 import cd.ir.ast.BuiltInReadFloat;
 import cd.ir.ast.BuiltInWrite;
 import cd.ir.ast.BuiltInWriteFloat;
 import cd.ir.ast.Cast;
 import cd.ir.ast.Expr;
 import cd.ir.ast.Field;
 import cd.ir.ast.FloatConst;
 import cd.ir.ast.IfElse;
 import cd.ir.ast.Index;
 import cd.ir.ast.IntConst;
 import cd.ir.ast.MethodCall;
 import cd.ir.ast.MethodCallExpr;
 import cd.ir.ast.MethodDecl;
 import cd.ir.ast.NewArray;
 import cd.ir.ast.NewObject;
 import cd.ir.ast.NullConst;
 import cd.ir.ast.ReturnStmt;
 import cd.ir.ast.ThisRef;
 import cd.ir.ast.UnaryOp;
 import cd.ir.ast.WhileLoop;
 import cd.ir.ast.UnaryOp.UOp;
 import cd.ir.ast.Var;
 import cd.ir.symbols.ArrayTypeSymbol;
 import cd.ir.symbols.ClassSymbol;
 import cd.ir.symbols.MethodSymbol;
 import cd.ir.symbols.TypeSymbol;
 import cd.ir.symbols.VariableSymbol;
 import cd.semantic.TypeSymbolTable;
 import cd.semantic.ti.constraintSolving.ConstantTypeSet;
 import cd.semantic.ti.constraintSolving.ConstantTypeSetFactory;
 import cd.semantic.ti.constraintSolving.ConstraintSolver;
 import cd.semantic.ti.constraintSolving.ConstraintSystem;
 import cd.semantic.ti.constraintSolving.TypeSet;
 import cd.semantic.ti.constraintSolving.TypeVariable;
 import cd.semantic.ti.constraintSolving.constraints.ConstraintCondition;
 
 import com.google.common.base.Optional;
 import com.google.common.collect.ImmutableSet;
 
 public class LocalTypeInferenceWithConstraints extends LocalTypeInference {
 
 	@Override
 	public void inferTypes(MethodDecl mdecl, TypeSymbolTable typeSymbols) {
 		ConstraintGenerator generator = new ConstraintGenerator(mdecl,
 				typeSymbols);
 		generator.generate();
 		ConstraintSystem constraintSystem = generator.getConstraintSystem();
 		ConstraintSolver solver = new ConstraintSolver(constraintSystem);
 		solver.solve();
 		if (!solver.hasSolution()) {
 			throw new SemanticFailure(Cause.TYPE_INFERENCE_ERROR,
 					"Type inference was unable to resolve type constraints");
 		} else {
 			for (VariableSymbol varSym : mdecl.sym.getLocals()) {
 				Set<TypeSymbol> possibleTypes = generator
 						.getPossibleTypes(varSym);
 				TypeSymbol type = null;
 				if (possibleTypes.isEmpty()) {
 					// Use the bottom type if there are no types in the type
 					// set. Since the constraint system has been solved
 					// successfully, this usually (always?) means that the
 					// variable symbol is not used at all.
 					type = typeSymbols.getBottomType();
 				} else if (possibleTypes.size() == 1) {
 					type = possibleTypes.iterator().next();
 				} else if (possibleTypes.size() > 1) {
 					// NOTE: we may still try to take the join (lca). This is
 					// sometimes necessary.
 					TypeSymbol[] typesArray = possibleTypes
 							.toArray(new TypeSymbol[possibleTypes.size()]);
 					TypeSymbol lca = typeSymbols.getLCA(typesArray);
 					if (lca != typeSymbols.getTopType()) {
 						type = lca;
 					} else {
 						throw new SemanticFailure(Cause.TYPE_INFERENCE_ERROR,
 								"Type inference resulted in ambiguous type for "
 										+ varSym.name);
 					}
 				}
 				varSym.setType(type);
 			}
 		}
 
 	}
 
 	/**
 	 * ConstraintGenerator is responsible for creating as many type variables
 	 * and constraints as necessary for a method.
 	 */
 	public class ConstraintGenerator extends AstVisitor<TypeVariable, Void> {
 		private final TypeSymbolTable typeSymbols;
 		private final MethodDecl mdecl;
 		private final ConstraintSystem constraintSystem;
 
 		private final MethodSymbolCache methodSymbolCache;
 		private final ClassSymbolFieldCache classFieldSymbolCache;
 		private final ConstantTypeSetFactory constantTypeSetFactory;
 
 		private ConstantTypeSet allowedReturnTypeSet;
 
 		// Map to remember the type variables for our parameters and locals,
 		// i.e. what we are eventually interested in.
 		// Note to avoid confusion: VariableSymbols are symbols for program
 		// variables while
 		// these TypeVariables are constraint solver variables describing the
 		// type of such program variables
 		private final Map<VariableSymbol, TypeVariable> localSymbolVariables = new HashMap<>();
 
 		public ConstraintGenerator(MethodDecl mdecl, TypeSymbolTable typeSymbols) {
 			this.typeSymbols = typeSymbols;
 			this.mdecl = mdecl;
 			this.constraintSystem = new ConstraintSystem();
 			this.methodSymbolCache = MethodSymbolCache.of(typeSymbols);
 			this.classFieldSymbolCache = ClassSymbolFieldCache.of(typeSymbols);
 			this.constantTypeSetFactory = new ConstantTypeSetFactory(
 					typeSymbols);
 		}
 
 		public ConstraintSystem getConstraintSystem() {
 			return constraintSystem;
 		}
 
 		public Set<TypeSymbol> getPossibleTypes(VariableSymbol varSym) {
 			return localSymbolVariables.get(varSym).getTypes();
 		}
 
 		public void generate() {
 			// variables and constraints for parameters (types given!)
 			MethodSymbol msym = mdecl.sym;
 			for (VariableSymbol varSym : msym.getParameters()) {
 				TypeVariable typeVar = constraintSystem
 						.addTypeVariable("param_" + varSym.name);
 				ConstantTypeSet typeConst = constantTypeSetFactory.make(varSym
 						.getType());
 				constraintSystem.addEquality(typeVar, typeConst);
 				localSymbolVariables.put(varSym, typeVar);
 			}
 
 			// type variables for local variables
 			for (VariableSymbol varSym : msym.getLocals()) {
 				TypeVariable typeVar = constraintSystem
 						.addTypeVariable("local_" + varSym.name);
 				localSymbolVariables.put(varSym, typeVar);
 			}
 
 			// type variable and constraints for return value (if any)
 			if (msym.returnType == typeSymbols.getVoidType()) {
 				allowedReturnTypeSet = constantTypeSetFactory.makeEmpty();
 			} else {
 				allowedReturnTypeSet = constantTypeSetFactory
 						.makeDeclarableSubtypes(msym.returnType);
 			}
 
 			ConstraintStmtVisitor constraintVisitor = new ConstraintStmtVisitor();
 			mdecl.accept(constraintVisitor, null);
 		}
 
 		public class ConstraintStmtVisitor extends AstVisitor<Void, Void> {
 			ConstraintExprVisitor exprVisitor = new ConstraintExprVisitor();
 
 			@Override
 			public Void returnStmt(ReturnStmt ast, Void arg) {
 				if (ast.arg() != null) {
 					TypeSet exprTypeSet = exprVisitor.visit(ast.arg());
 					constraintSystem.addUpperBound(exprTypeSet,
 							allowedReturnTypeSet);
 				}
 				return null;
 			}
 
 			@Override
 			public Void assign(Assign assign, Void arg) {
 				TypeSet lhsTypeSet = exprVisitor.visit(assign.left());
 				TypeSet exprTypeSet = exprVisitor.visit(assign.right());
 				constraintSystem.addInequality(exprTypeSet, lhsTypeSet);
 				return null;
 			}
 
 			@Override
 			public Void builtInWrite(BuiltInWrite ast, Void arg) {
 				TypeSet argTypeSet = exprVisitor.visit(ast.arg());
 				ConstantTypeSet intTypeSet = constantTypeSetFactory.makeInt();
 				constraintSystem.addEquality(argTypeSet, intTypeSet);
 				return null;
 			}
 
 			@Override
 			public Void builtInWriteFloat(BuiltInWriteFloat ast, Void arg) {
 				TypeSet argTypeSet = exprVisitor.visit(ast.arg());
 				ConstantTypeSet floatTypeSet = constantTypeSetFactory
 						.makeFloat();
 				constraintSystem.addEquality(argTypeSet, floatTypeSet);
 				return null;
 			}
 
 			@Override
 			public Void methodCall(MethodCall call, Void arg) {
 				exprVisitor.createMethodCallConstraints(call.methodName,
 						call.receiver(), call.argumentsWithoutReceiver(),
 						Optional.<TypeVariable> absent());
 				return null;
 			}
 
 			@Override
 			public Void ifElse(IfElse ast, Void arg) {
 				visit(ast.then(), null);
 				visit(ast.otherwise(), null);
 				TypeSet ifExprTypeSet = exprVisitor.visit(ast.condition(), arg);
 				TypeSet booleanType = constantTypeSetFactory.makeBoolean();
 				constraintSystem.addEquality(ifExprTypeSet, booleanType);
 				return null;
 			}
 
 			@Override
 			public Void whileLoop(WhileLoop ast, Void arg) {
 				visit(ast.body(), null);
 				TypeSet whileConditionExprTypeSet = exprVisitor.visit(
 						ast.condition(), arg);
 				TypeSet booleanType = constantTypeSetFactory.makeBoolean();
 				constraintSystem.addEquality(whileConditionExprTypeSet,
 						booleanType);
 				return null;
 			}
 		}
 
 		public class ConstraintExprVisitor extends
 				ExprVisitorWithoutArg<TypeSet> {
 
 			@Override
 			public TypeSet var(Var ast) {
 				VariableSymbol varSym = ast.getSymbol();
 				switch (varSym.getKind()) {
 				case FIELD:
 					TypeSymbol type = varSym.getType();
 					return constantTypeSetFactory.makeDeclarableSubtypes(type);
 				case LOCAL:
 				case PARAM:
 					return localSymbolVariables.get(varSym);
 				default:
 					throw new IllegalStateException("no such variable kind");
 				}
 			}
 
 			@Override
 			public TypeSet intConst(IntConst ast) {
 				return constantTypeSetFactory.makeInt();
 			}
 
 			@Override
 			public TypeSet floatConst(FloatConst ast) {
 				return constantTypeSetFactory.makeFloat();
 			}
 
 			@Override
 			public TypeSet booleanConst(BooleanConst ast) {
 				return constantTypeSetFactory.makeBoolean();
 			}
 
 			@Override
 			public TypeSet nullConst(NullConst ast) {
 				return constantTypeSetFactory.makeNull();
 			}
 
 			@Override
 			public TypeSet newObject(NewObject ast) {
 				TypeSymbol classSym = typeSymbols.getType(ast.typeName);
 				return constantTypeSetFactory.make(classSym);
 			}
 
 			@Override
 			public TypeSet newArray(NewArray ast) {
 				TypeSymbol arraySym = typeSymbols.get(ast.typeName);
 				return constantTypeSetFactory.make(arraySym);
 			}
 
 			@Override
 			public TypeSet thisRef(ThisRef ast) {
 				ClassSymbol classSymbol = mdecl.sym.owner;
 				return constantTypeSetFactory.make(classSymbol);
 			}
 
 			@Override
 			public TypeSet cast(Cast ast) {
 				TypeSet exprTypeSet = visit(ast.arg());
 				// only reference types can be cast
 				ConstantTypeSet allRefTyes = constantTypeSetFactory
 						.makeReferenceTypeSet();
 				constraintSystem.addUpperBound(exprTypeSet, allRefTyes);
 
 				TypeSymbol castResultType = typeSymbols.getType(ast.typeName);
				return constantTypeSetFactory.makeDeclarableSubtypes(castResultType);
 			}
 
 			@Override
 			public TypeSet field(Field ast) {
 				String fieldName = ast.fieldName;
 				Expr receiver = ast.arg();
 				TypeSet receiverTypeSet = visit(receiver);
 
 				Collection<ClassSymbol> declaringClassSymbols = classFieldSymbolCache
 						.get(fieldName);
 				Set<ClassSymbol> possibleClassSymbols = new HashSet<>();
 
 				TypeVariable resultType = constraintSystem.addTypeVariable();
 
 				for (ClassSymbol classSym : declaringClassSymbols) {
 					possibleClassSymbols.addAll(typeSymbols
 							.getClassSymbolSubtypes(classSym));
 					VariableSymbol fieldSymbol = classSym.getField(fieldName);
 					TypeSymbol fieldType = fieldSymbol.getType();
 					ConstraintCondition condition = new ConstraintCondition(
 							classSym, receiverTypeSet);
 					ConstantTypeSet fieldTypeSet = constantTypeSetFactory
 							.make(fieldType);
 					constraintSystem.addEquality(resultType, fieldTypeSet,
 							condition);
 				}
 
 				// The receiver *must* be a subtype of any class that has a
 				// field with the right name
 				ConstantTypeSet possibleClassTypeSet = new ConstantTypeSet(
 						possibleClassSymbols);
 				constraintSystem.addUpperBound(receiverTypeSet,
 						possibleClassTypeSet);
 
 				return resultType;
 			}
 
 			@Override
 			public TypeSet index(Index idx) {
 				Expr arrayExpr = idx.left();
 				TypeSet indexTypeSet = visit(idx.right());
 				constraintSystem.addEquality(indexTypeSet,
 						constantTypeSetFactory.makeInt());
 
 				TypeVariable resultVar = constraintSystem.addTypeVariable();
 				TypeSet arrayExprTypeSet = visit(arrayExpr);
 				ConstantTypeSet arrayTypesSet = constantTypeSetFactory
 						.makeArrayTypeSet();
 				constraintSystem.addUpperBound(arrayExprTypeSet, arrayTypesSet);
 				for (ArrayTypeSymbol arrayType : typeSymbols
 						.getArrayTypeSymbols()) {
 					ConstraintCondition condition = new ConstraintCondition(
 							arrayType, arrayExprTypeSet);
 					ConstantTypeSet arrayElementTypeSet = constantTypeSetFactory
 							.make(arrayType.elementType);
 					constraintSystem.addEquality(resultVar,
 							arrayElementTypeSet, condition);
 				}
 				return resultVar;
 			}
 
 			@Override
 			public TypeSet builtInRead(BuiltInRead ast) {
 				return constantTypeSetFactory.makeInt();
 			}
 
 			@Override
 			public TypeSet builtInReadFloat(BuiltInReadFloat ast) {
 				return constantTypeSetFactory.makeFloat();
 			}
 
 			@Override
 			public TypeSet binaryOp(BinaryOp binaryOp) {
 				BOp op = binaryOp.operator;
 				TypeSet leftTypeSet = visit(binaryOp.left());
 				TypeSet rightTypeSet = visit(binaryOp.right());
 
 				ConstantTypeSet booleanTypeSet, numTypeSet;
 				numTypeSet = constantTypeSetFactory.makeNumericalTypeSet();
 				booleanTypeSet = constantTypeSetFactory.makeBoolean();
 
 				switch (op) {
 				case B_TIMES:
 				case B_DIV:
 				case B_MOD:
 				case B_PLUS:
 				case B_MINUS:
 					constraintSystem.addUpperBound(leftTypeSet, numTypeSet);
 					constraintSystem.addEquality(leftTypeSet, rightTypeSet);
 					return leftTypeSet;
 				case B_AND:
 				case B_OR:
 					constraintSystem.addEquality(leftTypeSet, rightTypeSet);
 					constraintSystem.addEquality(leftTypeSet, booleanTypeSet);
 					return booleanTypeSet;
 				case B_EQUAL:
 				case B_NOT_EQUAL:
					constraintSystem.addEquality(leftTypeSet, rightTypeSet);
 					return booleanTypeSet;
 				case B_LESS_THAN:
 				case B_LESS_OR_EQUAL:
 				case B_GREATER_THAN:
 				case B_GREATER_OR_EQUAL:
 					constraintSystem.addUpperBound(leftTypeSet, numTypeSet);
 					constraintSystem.addEquality(leftTypeSet, rightTypeSet);
 					return booleanTypeSet;
 				default:
 					throw new IllegalStateException("no such binary operator");
 				}
 			}
 
 			public void createMethodCallConstraints(String methodName,
 					Expr receiver, List<Expr> arguments,
 					Optional<TypeVariable> methodCallResultTypeVar) {
 				Collection<MethodSymbol> methodSymbols = methodSymbolCache.get(
 						methodName, arguments.size());
 				TypeSet receiverTypeSet = visit(receiver);
 				Set<ClassSymbol> possibleReceiverTypes = new HashSet<>();
 
 				for (MethodSymbol msym : methodSymbols) {
 					ImmutableSet<ClassSymbol> msymClassSubtypes = typeSymbols
 							.getClassSymbolSubtypes(msym.owner);
 					possibleReceiverTypes.addAll(msymClassSubtypes);
 					ConstraintCondition condition = new ConstraintCondition(
 							msym.owner, receiverTypeSet);
 					for (int argNum = 0; argNum < arguments.size(); argNum++) {
 						Expr argument = arguments.get(argNum);
 						TypeSet argTypeSet = visit(argument);
 						VariableSymbol paramSym = msym.getParameter(argNum);
 						TypeSymbol paramType = paramSym.getType();
 						ConstantTypeSet expectedArgType = constantTypeSetFactory
 								.makeDeclarableSubtypes(paramType);
 						constraintSystem.addUpperBound(argTypeSet,
 								expectedArgType, condition);
 					}
 
 					if (methodCallResultTypeVar.isPresent()) {
 						TypeSymbol resultSym = msym.returnType;
 						ConstantTypeSet expectedResultTypes = constantTypeSetFactory
 								.make(resultSym);
 						constraintSystem.addLowerBound(
 								methodCallResultTypeVar.get(),
 								expectedResultTypes, condition);
 					}
 				}
 
 				// the receiver _must_ be a subtype of any class that has a
 				// method with the right name and number of arguments
 				ConstantTypeSet possibleReceiverTypeSet = new ConstantTypeSet(
 						possibleReceiverTypes);
 				constraintSystem.addUpperBound(receiverTypeSet,
 						possibleReceiverTypeSet);
 			}
 
 			@Override
 			public TypeSet methodCall(MethodCallExpr call) {
 				TypeVariable methodCallResultTypeVar = constraintSystem
 						.addTypeVariable();
 				createMethodCallConstraints(call.methodName, call.receiver(),
 						call.argumentsWithoutReceiver(),
 						Optional.of(methodCallResultTypeVar));
 				return methodCallResultTypeVar;
 			}
 
 			@Override
 			public TypeSet unaryOp(UnaryOp unaryOp) {
 				UOp op = unaryOp.operator;
 				TypeSet subExprTypeSet = visit(unaryOp.arg());
 				ConstantTypeSet numTypes = constantTypeSetFactory
 						.makeNumericalTypeSet();
 				ConstantTypeSet booleanType = constantTypeSetFactory
 						.makeBoolean();
 
 				switch (op) {
 				case U_BOOL_NOT:
 					constraintSystem.addEquality(subExprTypeSet, booleanType);
 					return booleanType;
 				case U_MINUS:
 				case U_PLUS:
 					constraintSystem.addUpperBound(subExprTypeSet, numTypes);
 					break;
 				}
 				return subExprTypeSet;
 			}
 		}
 	}
 
 }
