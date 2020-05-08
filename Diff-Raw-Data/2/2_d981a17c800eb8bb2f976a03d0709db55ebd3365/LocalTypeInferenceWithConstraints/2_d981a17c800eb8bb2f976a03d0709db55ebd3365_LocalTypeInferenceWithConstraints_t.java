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
 import cd.ir.ast.UnaryOp.UOp;
 import cd.ir.ast.Var;
 import cd.ir.symbols.ArrayTypeSymbol;
 import cd.ir.symbols.ClassSymbol;
 import cd.ir.symbols.MethodSymbol;
 import cd.ir.symbols.TypeSymbol;
 import cd.ir.symbols.VariableSymbol;
 import cd.ir.symbols.VariableSymbol.Kind;
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
 		ConstraintGenerator constraintGen = new ConstraintGenerator(mdecl,
 				typeSymbols);
 		constraintGen.generate();
 		ConstraintSolver solver = new ConstraintSolver(
 				constraintGen.getConstraintSystem());
 		solver.solve();
 		if (!solver.hasSolution()) {
 			throw new SemanticFailure(Cause.TYPE_INFERENCE_ERROR,
 					"Type inference was unable to resolve type constraints");
 		} else {
 			for (VariableSymbol varSym : mdecl.sym.getLocals()) {
 				Set<TypeSymbol> possibleTypes = constraintGen
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
 			this.constantTypeSetFactory = new ConstantTypeSetFactory(typeSymbols);
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
 				TypeVariable typeVar = constraintSystem.addTypeVariable("param_" + varSym.name);
 				ConstantTypeSet typeConst = constantTypeSetFactory.make(varSym.getType());
 				constraintSystem.addEquality(typeVar, typeConst);
 				localSymbolVariables.put(varSym, typeVar);
 			}
 
 			// type variables for local variables
 			for (VariableSymbol varSym : msym.getLocals()) {
 				TypeVariable typeVar = constraintSystem.addTypeVariable("local_" + varSym.name);
 				localSymbolVariables.put(varSym, typeVar);
 			}
 
 			// type variable and constraints for return value (if any)
 			if (msym.returnType == typeSymbols.getVoidType()) {
 				allowedReturnTypeSet = constantTypeSetFactory.makeEmpty();
 			} else {
 				allowedReturnTypeSet = constantTypeSetFactory.makeDeclarableSubtypes(msym.returnType);
 			}
 
 			ConstraintStmtVisitor constraintVisitor = new ConstraintStmtVisitor();
 			mdecl.accept(constraintVisitor, null);
 		}
 
 		public class ConstraintStmtVisitor extends AstVisitor<Void, Void> {
 			ConstraintExprVisitor exprVisitor = new ConstraintExprVisitor();
 
 			@Override
 			public Void returnStmt(ReturnStmt ast, Void arg) {
 				if (ast.arg() != null) {
 					TypeSet exprTypeSet = exprVisitor.visit(ast.arg(), null);
 					constraintSystem.addUpperBound(exprTypeSet,
 							allowedReturnTypeSet);
 				}
 				return null;
 			}
 
 			@Override
 			public Void assign(Assign assign, Void arg) {
 				Expr lhs = assign.left();
 				if (lhs instanceof Var) {
 					VariableSymbol varSym = ((Var) lhs).getSymbol();
 					if (varSym.getKind().equals(Kind.LOCAL)) {
 						TypeSet exprTypeSet = exprVisitor.visit(assign.right(),
 								null);
 						TypeVariable localTypeVar = localSymbolVariables
 								.get(varSym);
 						constraintSystem.addInequality(exprTypeSet,
 								localTypeVar);
 					}
 				}
 				return null;
 			}
 			
 			@Override
 			public Void builtInWrite(BuiltInWrite ast, Void arg) {
 				TypeSet argTypeSet = exprVisitor.visit(ast.arg(), null);
 				ConstantTypeSet intTypeSet = constantTypeSetFactory.makeInt();
 				constraintSystem.addEquality(argTypeSet, intTypeSet);
 				return null;
 			}
 			
 			@Override
 			public Void builtInWriteFloat(BuiltInWriteFloat ast, Void arg) {
 				TypeSet argTypeSet = exprVisitor.visit(ast.arg(), null);
 				ConstantTypeSet floatTypeSet = constantTypeSetFactory.makeFloat();
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
 		}
 
 		public class ConstraintExprVisitor extends ExprVisitor<TypeSet, Void> {
 			@Override
 			public TypeSet var(Var ast, Void arg) {
 				VariableSymbol varSym = ast.getSymbol();
 				if (varSym.getKind() == Kind.FIELD) {
 					return constantTypeSetFactory.make(varSym.getType());
 				} else {
 					return localSymbolVariables.get(varSym);
 				}
 			}
 
 			@Override
 			public TypeSet intConst(IntConst ast, Void arg) {
 				return constantTypeSetFactory.makeInt();
 			}
 
 			@Override
 			public TypeSet floatConst(FloatConst ast, Void arg) {
 				return constantTypeSetFactory.makeFloat();
 			}
 
 			@Override
 			public TypeSet booleanConst(BooleanConst ast, Void arg) {
 				return constantTypeSetFactory.makeBoolean();
 			}
 			
 			@Override
 			public TypeSet nullConst(NullConst ast, Void arg) {
 				ConstantTypeSet allReferenceTypeSet = constantTypeSetFactory.makeReferenceTypeSet();
 				TypeVariable nullTypeVar = constraintSystem.addTypeVariable();
 				constraintSystem.addUpperBound(nullTypeVar, allReferenceTypeSet);
 				return nullTypeVar;
 			}
 			
 			@Override
 			public TypeSet newObject(NewObject ast, Void arg) {
 				TypeSymbol classSym = typeSymbols.getType(ast.typeName);
 				return constantTypeSetFactory.make(classSym);
 			}
 			
 			@Override
 			public TypeSet newArray(NewArray ast, Void arg) {
 				TypeSymbol arraySym = typeSymbols.get(ast.typeName);
 				return constantTypeSetFactory.make(arraySym);
 			}
 			
 			@Override
 			public TypeSet thisRef(ThisRef ast, Void arg) {
 				ClassSymbol classSymbol = mdecl.sym.owner;
 				return constantTypeSetFactory.makeDeclarableSubtypes(classSymbol);
 			}
 			
 			@Override
 			public TypeSet cast(Cast ast, Void arg) {
 				TypeSet exprTypeSet = visit(ast.arg(), null);
 				// only reference types can be casted
 				ConstantTypeSet allRefTyes = constantTypeSetFactory.makeReferenceTypeSet();
 				constraintSystem.addUpperBound(exprTypeSet, allRefTyes);
 				
 				TypeSymbol castResultType = typeSymbols.getType(ast.typeName);
 				return constantTypeSetFactory.makeDeclarableSubtypes(castResultType);
 			}
 			
 			@Override
 			public TypeSet field(Field ast, Void arg) {
 				String fieldName = ast.fieldName;
 				Expr receiver = ast.arg();
 				TypeSet receiverTypeSet = visit(receiver, null);
 
 				Collection<ClassSymbol> classSymbols = classFieldSymbolCache.get(fieldName);
 				TypeVariable resultType = constraintSystem.addTypeVariable();
 
 				for (ClassSymbol classSym : classSymbols) {
 					VariableSymbol fieldSymbol = classSym.getField(fieldName);
 					TypeSymbol fieldType = fieldSymbol.getType();
 					ConstraintCondition condition = new ConstraintCondition(classSym, receiverTypeSet);
 					ConstantTypeSet fieldTypeSet = constantTypeSetFactory.make(fieldType);
 					constraintSystem.addEquality(resultType, fieldTypeSet, condition);
 				}
 				return resultType;
 			}
 			
 			
 			@Override
 			public TypeSet index(Index idx, Void arg) {
 				Expr arrayExpr = idx.left();
 				TypeSet indexTypeSet = visit(idx.right(), null);
 				constraintSystem.addEquality(indexTypeSet,
 						constantTypeSetFactory.makeInt());
 
 				TypeVariable resultVar = constraintSystem.addTypeVariable();
 				TypeSet arrayExprTypeSet = visit(arrayExpr, null);
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
 			public TypeSet builtInRead(BuiltInRead ast, Void arg) {
 				return constantTypeSetFactory.makeInt();
 			}
 			
 			@Override
 			public TypeSet builtInReadFloat(BuiltInReadFloat ast, Void arg) {
 				return constantTypeSetFactory.makeFloat();
 			}
 			
 			@Override
 			public TypeSet binaryOp(BinaryOp binaryOp, Void arg) {
 				BOp op = binaryOp.operator;
 				TypeSet leftTypeSet = visit(binaryOp.left(), null);
 				TypeSet rightTypeSet = visit(binaryOp.right(), null);
 				TypeSet resultSet;
 				
 				ConstantTypeSet numTypes = constantTypeSetFactory.makeNumericalTypeSet();
 				ConstantTypeSet booleanType = constantTypeSetFactory.makeBoolean();
 
 				switch (op) {
 				case B_TIMES:
 				case B_DIV:
 				case B_MOD:
 				case B_PLUS:
 				case B_MINUS:
 					constraintSystem.addUpperBound(leftTypeSet, numTypes);
 					constraintSystem.addEquality(leftTypeSet, rightTypeSet);
 					resultSet = leftTypeSet;
 					break;
 				case B_AND:
 				case B_OR:
 					constraintSystem.addEquality(leftTypeSet, rightTypeSet);
 					constraintSystem.addEquality(leftTypeSet, booleanType);
 					resultSet = booleanType;
 					break;
 				case B_EQUAL:
 				case B_NOT_EQUAL:
 					constraintSystem.addEquality(leftTypeSet, rightTypeSet);
 					resultSet = booleanType;
 					break;
 				case B_LESS_THAN:
 				case B_LESS_OR_EQUAL:
 				case B_GREATER_THAN:
 				case B_GREATER_OR_EQUAL:
 					constraintSystem.addUpperBound(leftTypeSet, numTypes);
 					constraintSystem.addEquality(leftTypeSet, rightTypeSet);
 					resultSet = booleanType;
 					break;
 				default:
 					throw new IllegalStateException("binary operator " + op + " not supported");
 				}
 				
 				return resultSet;
 			}
 			
 			public void createMethodCallConstraints(String methodName,
 					Expr receiver, List<Expr> arguments,
 					Optional<TypeVariable> methodCallResultTypeVar) {
 				Collection<MethodSymbol> methodSymbols = methodSymbolCache.get(
 						methodName, arguments.size());
 				TypeSet receiverTypeSet = visit(receiver, null);
 				Set<ClassSymbol> possibleReceiverTypes = new HashSet<>();
 
 				for (MethodSymbol msym : methodSymbols) {
 					ImmutableSet<ClassSymbol> msymClassSubtypes = typeSymbols
 							.getClassSymbolSubtypes(msym.owner);
 					possibleReceiverTypes.addAll(msymClassSubtypes);
 					ConstraintCondition condition = new ConstraintCondition(
 							msym.owner, receiverTypeSet);
 					for (int argNum = 0; argNum < arguments.size(); argNum++) {
 						Expr argument = arguments.get(argNum);
 						TypeSet argTypeSet = visit(argument, null);
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
 				// method
 				// with the right name and number of arguments
 				ConstantTypeSet possibleReceiverTypeSet = new ConstantTypeSet(
 						possibleReceiverTypes);
 				constraintSystem.addUpperBound(receiverTypeSet,
 						possibleReceiverTypeSet);
 			}
 			
 			@Override
 			public TypeSet methodCall(MethodCallExpr call, Void arg) {
 				TypeVariable methodCallResultTypeVar = constraintSystem
 						.addTypeVariable();
 				createMethodCallConstraints(call.methodName, call.receiver(),
 						call.argumentsWithoutReceiver(),
 						Optional.of(methodCallResultTypeVar));
 				return methodCallResultTypeVar;
 			}
 			
 			@Override
 			public TypeSet unaryOp(UnaryOp unaryOp, Void arg) {
 				UOp op = unaryOp.operator;
 				TypeSet subExprTypeSet = visit(unaryOp.arg(), null);
 				
 				ConstantTypeSet numTypes = constantTypeSetFactory.makeNumericalTypeSet();
 				ConstantTypeSet booleanType = constantTypeSetFactory.makeBoolean();
 
 				switch (op) {
 				case U_BOOL_NOT:
					constraintSystem.addEquality(subExprTypeSet, booleanType);
 					break;
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
