 package types;
 
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.HashMap;
 
 import crux.Symbol;
 import ast.*;
 
 /**
  * A visitor that performed type checking on an AST.
  */
 public class TypeChecker implements CommandVisitor {
     /* A map of types TODO */
     private Map<Command, Type> typeMap;
 
     /* Buffered error messages. */
     private StringBuilder errorBuffer;
 
     /* Keep track of paths that needs a return. */
     private boolean needsReturn;
 
     /* A list of found return types for the current function. */
     private Map<Return, Type> foundRetTypes;
 
     /* A list found types in an expression list. */
     private List<Type> exprTypes;
 
     // TODO am I suppose to implement these some where?
     /* Useful error strings:
      *
      * "Function " + func.name() + " has a void argument in position " + pos + "."
      * "Function " + func.name() + " has an error in argument in position " + pos + ": " + error.getMessage()
      *
      * "Function main has invalid signature."
      *
      * "Not all paths in function " + currentFunctionName + " have a return."
      *
      * "IfElseBranch requires bool condition not " + condType + "."
      * "WhileLoop requires bool condition not " + condType + "."
      *
      * "Function " + currentFunctionName + " returns " + currentReturnType + " not " + retType + "."
      *
      * "Variable " + varName + " has invalid type " + varType + "."
      * "Array " + arrayName + " has invalid base type " + baseType + "."
      */
 
     /**
      * Create a new type checker.
      */
     public TypeChecker() {
         typeMap = new HashMap<Command, Type>();
         errorBuffer = new StringBuilder();
         //foundRetTypes = new LinkedList<Type>();
         foundRetTypes = new HashMap<Return, Type>();
         exprTypes = new LinkedList<Type>();
     }
 
     /**
      * Report an error.
      * @param lineNum The line number the error occured on.
      * @param charPos The character position the error occured on.
      * @param message A describing of the error.
      */
     private void reportError(int lineNum, int charPos, String message) {
         errorBuffer.append("TypeError(" + lineNum + "," + charPos + ")");
         errorBuffer.append("[" + message + "]" + "\n");
     }
 
 	/**
 	 * Put a command with its type in the type map.
 	 * @param node The command node.
 	 * @param type The type for the node.
 	 */
     private void put(Command node, Type type) {
         if (type instanceof ErrorType) {
             reportError(node.lineNumber(), node.charPosition(), ((ErrorType)type).getMessage());
         	typeMap.put(node, type);
         } else {
         	typeMap.put(node, type);
         }
     }
 
     /**
      * Get the type for a given node.
      * @param node The node to get the type for.
      * @return The found type or null.
      */
     public Type getType(Command node) {
         return typeMap.get(node);
     }
 
     /**
      * Check a given AST for type errors.
      * @param ast The AST to check.
      * @return Indication of success or failre.
      */
     public boolean check(Command ast) {
         ast.accept(this);
         return !hasError();
     }
 
     /**
      * Query for found errors.
      * @return Indication of error presence.
      */
     public boolean hasError() {
         return errorBuffer.length() != 0;
     }
 
     /**
      * Get the error report.
      * @return The error report.
      */
     public String errorReport() {
         return errorBuffer.toString();
     }
 
     /**
      * Visit and get type for node.
      * @param node The node to visit.
      * @return The type of the node.
      */
     private Type visitRetriveType(Visitable node) {
 		node.accept(this);
 		return getType((Command) node); // TODO not good to cast...
     }
 
 	// Visitor methods ===================================
 
     @Override
     public void visit(ExpressionList node) {
         for(Expression expr : node) {
 			//expr.accept(this);
 			exprTypes.add(visitRetriveType(expr));
         }
     }
 
     @Override
     public void visit(DeclarationList node) {
         for (Declaration decl : node) {
         	decl.accept(this);
         }
     }
 
     @Override
     public void visit(StatementList node) {
         //entryNbrRets = foundRetTypes.size();
         for (Statement stmt : node) {
         	needsReturn = true; // TODO should we always require return from all statements? and then check after loop thath !needsReturn
         	stmt.accept(this);
         }
         if (needsReturn) {
         	// fail
         }
         //if ((0 == foundRetTypes.size() == entryNbrRets) {
         //if (foundRetTypes.size() == 0) {
 		// fail, implicit by our var still = true I guess.
         //}
     	}
 
     @Override
     public void visit(AddressOf node) {
         Type type = node.symbol().type();
 		//put(node, new AddressType(type));
 		put(node, type);
     }
 
     @Override
     public void visit(LiteralBool node) {
         put(node, new BoolType());
     }
 
     @Override
     public void visit(LiteralFloat node) {
         put(node, new FloatType());
     }
 
     @Override
     public void visit(LiteralInt node) {
         put(node, new IntType());
     }
 
     @Override
     public void visit(VariableDeclaration node) {
     	Type varType = node.symbol().type();
    	if (varType.equivalent(new IntType()) || varType.equivalent(new FloatType()) || varType.equivalent(new BoolType())) {
 			put(node, varType); // TODO do we really need to put this?
     	} else {
      	 	put(node, new ErrorType("Variable " + node.symbol().name() + " has invalid type " + varType + "."));
     	}
     }
 
     @Override
     public void visit(ArrayDeclaration node) {
         throw new RuntimeException("Implement this");
     }
 
     @Override
     public void visit(FunctionDefinition node) {
         Symbol func = node.function();
         List<Symbol> args = node.arguments();
         Type returnType = ((FuncType) func.type()).returnType();
 
         if (func.name().equals("main")) {
         	if (args.size() != 	0 || !(returnType instanceof VoidType)) {
 				put(node, new ErrorType("Function main has invalid signature."));
 				return;
 			}
         } else {
         	int pos = 0;
         	for (Symbol arg : args) {
 				Type argType = arg.type();
 				if (argType instanceof ErrorType) {
 					put(node, new ErrorType("Function " + func.name() + " has an error in argument in position " + pos + ": " + ((ErrorType) argType).getMessage()));
 					return;
 				} else if (argType instanceof VoidType) {
  	 	 	 		put(node, new ErrorType("Function " + func.name() + " has a void argument in position " + pos + "."));
 					return;
 				}
 				++pos;
         	}
         }
 
         // TODO check so all paths return. 1) jack: register retrurns for statement, while, if and propagate up. 2) eric: recursive helper method. 3) SSA on blog, retval must be initialized by all paths in the end.
         // what if a path ends with return; wo/ value? -- set it to voidType
         // 
 		needsReturn = true;
 		foundRetTypes.clear();
         visit(node.body());
 		if (!(returnType instanceof VoidType) && needsReturn) { 
         	put(node, new ErrorType("Not all paths in function " + func.name() + " have a return."));
 		} else {
         	// TODO check correct return type for all found return types. 
         	// * what if a function returns a value but the signature is void-returning? -- autosolved
 			//for (Type foundRetType : foundRetTypes) {
 			for (Return retNode : foundRetTypes.keySet()) {
 				//assert(foundRetType != null);
 				Type foundRetType = foundRetTypes.get(retNode);
 				if (!foundRetType.equivalent(returnType)) {
 					put(retNode, new ErrorType("Function " + func.name() + " returns " + returnType + " not " + foundRetType + "."));
 				}
 			}
         	put(node, returnType);
 			}
     	}
 
     	@Override
     	public void visit(Comparison node) {
     		Type lhs = visitRetriveType(node.leftSide());
     		Type rhs = visitRetriveType(node.rightSide());
         	put(node, lhs.compare(rhs));
     	}
 
     	@Override
     	public void visit(Addition node) {
         	Type lhs = visitRetriveType(node.leftSide());
         	Type rhs = visitRetriveType(node.rightSide());
         	if (lhs != null && rhs != null) {
         		Type res = lhs.add(rhs);
         		//System.out.println("Addresult: " + res);
 				put(node, res);
         	}
     	}
 
     	@Override
     	public void visit(Subtraction node) {
         	Type lhs = visitRetriveType(node.leftSide());
         	Type rhs = visitRetriveType(node.rightSide());
 			put(node, lhs.sub(rhs));
     	}
 
     	@Override
     	public void visit(Multiplication node) {
         	Type lhs = visitRetriveType(node.leftSide());
         	Type rhs = visitRetriveType(node.rightSide());
 			put(node, lhs.mul(rhs));
     	}
 
     	@Override
     	public void visit(Division node) {
         	Type lhs = visitRetriveType(node.leftSide());
         	Type rhs = visitRetriveType(node.rightSide());
 			put(node, lhs.div(rhs));
     	}
 
     	@Override
     	public void visit(LogicalAnd node) {
         	Type lhs = visitRetriveType(node.leftSide());
         	Type rhs = visitRetriveType(node.rightSide());
 			put(node, lhs.and(rhs));
     	}
 
     	@Override
     	public void visit(LogicalOr node) {
         	Type lhs = visitRetriveType(node.leftSide());
         	Type rhs = visitRetriveType(node.rightSide());
 			put(node, lhs.or(rhs));
     	}
 
     	@Override
     	public void visit(LogicalNot node) {
			Type type = visitRetriveType(node.expression());
			if (type.equivalent(new BoolType())) {
				put(node, type);
			} else {
				put(node, new ErrorType("Cannot negate " + type + "."));
			}
     	}
 
     	@Override
     	public void visit(Dereference node) {
         	put(node, visitRetriveType(node.expression()));
     	}
 
     	@Override
     	public void visit(Index node) {
         	throw new RuntimeException("Implement this");
     	}
 
     	@Override
     	public void visit(Assignment node) {
         	Type srcType = visitRetriveType(node.source());
             //Type destType = newvisitRetriveType(node.destination());
         	Type destType = new AddressType(visitRetriveType(node.destination())); // TODO suppose to use address here or in visit(Addressof
             //assert(destType instanceof IntType);
         	put(node, destType.assign(srcType));
     	}
 
     	@Override
     	public void visit(Call node) {
     		Symbol func = node.function();
     		FuncType funcType = (FuncType) func.type(); // TODO sigh so ugly, right?
     		ExpressionList args = node.arguments();
 			exprTypes.clear();
 			args.accept(this);
 			TypeList callArgTypes = new TypeList(exprTypes);
 
 
 
         	//Type argTypes = visitRetriveType(args);
         	//// TODO verify that argTypes == func->type->argtypes
 			//if (argTypes == null) {
 			//System.out.println("Oh snap argtypes == null");
 			//TypeList argList = new TypeList();
 			//argList.append(new VoidType());
 			//argTypes = argList;
 			//}
 
 			//assert(funcType.call(argTypes) != null);
 			//System.out.println("in call node" + funcType.call(argTypes) );
 			put(node, funcType.call(callArgTypes));
     	}
 
     	@Override
     	public void visit(IfElseBranch node) {
         	Type condType = visitRetriveType(node.condition());
         	if (!condType.equivalent(new BoolType())) {
      	 		put(node, new ErrorType("IfElseBranch requires bool condition not " + condType + "."));
      	 		return;
         	}
 
 
 			// needstrue IF not both branches has return OR 
 			int prevNbrRets = foundRetTypes.size();
 			visit(node.thenBlock());
 			boolean thenHasReturn = (foundRetTypes.size() > prevNbrRets);
 			//needsReturn = (foundRetTypes.size() == prevNbrRets); // TODO or only set to true when needed and let visit(Return) always set to false?
 			prevNbrRets = foundRetTypes.size();
 			visit(node.elseBlock());
 			boolean elseHasReturn = (foundRetTypes.size() > prevNbrRets); // TODO handle empty else block (stmtlist.size() == 0)
 
 			needsReturn = (thenHasReturn ^ elseHasReturn);
 			//if (foundRetTypes.size() > prevNbrRets) {
 			//needsReturn = true;
 			//}
 
 
     	}
 
     	@Override
     	public void visit(WhileLoop node) {
         	throw new RuntimeException("Implement this");
     	}
 
     	@Override
     	public void visit(Return node) {
     		Type retType = visitRetriveType(node.argument());
         	//if (retType != null) { // Error nodes gives no type for put().
             //if (retType instanceof AddressType) {
                 //foundRetTypes.put(node, ((AddressType) retType).base()); // TODO ugly hack to pass test03
             //} else {
     			foundRetTypes.put(node, retType);
             //}
         	if (!(retType instanceof ErrorType)) { // TODO ugly hack to not get doublerror reporting, modded put() to record error since we need it above
         		put(node, retType);
         	}
         	//}
         	needsReturn = false;
     	}
 
     	@Override
     	public void visit(ast.Error node) {
         	put(node, new ErrorType(node.message()));
     	}
 	}
