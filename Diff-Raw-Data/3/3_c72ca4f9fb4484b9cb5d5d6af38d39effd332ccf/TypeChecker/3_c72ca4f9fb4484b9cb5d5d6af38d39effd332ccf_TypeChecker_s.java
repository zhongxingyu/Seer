 package parser;
 
 import ast.*;
 import interpreter.Scope;
 import interpreter.Value;
 
 import java.util.HashMap;
 import java.util.Iterator;
 
 /**
  * This class checks the type correctness of a user program.
  */
 public class TypeChecker implements ASTVisitor {
     /**
      * temporarily saves the current function
      */
     private Function currentFunction;
     /**
      * temporarily saves the current type
      */
     private Type tempType;
     /**
      * temporarily saves the current scope
      */
     private Scope currentScope;
     /**
      * temporarily saves whether function calls are allowed
      */
     private boolean functionCallAllowed = true;
     /**
      * temporarily saves the functions of the program (except main)
      */
     private Function[] functions;
 
     /**
      * Checks the given AST for type correctness.
      * @param ast AST to check
      */
     public void checkTypes(ASTRoot ast) {
         ast.accept(this);
     }
 
     /**
      * Sets the current scope of the type checker
      * (for type checking single expressions)
      * @param currentScope new scope
      */
     public void setCurrentScope(Scope currentScope) {
         this.currentScope = currentScope;
     }
 
     /**
      * Sets whether function calls are allowed
      * @param functionCallAllowed flag indicating whether
      *                            function calls are allowed
      */
     public void setFunctionCallAllowed(boolean functionCallAllowed) {
         this.functionCallAllowed = functionCallAllowed;
     }
 
     /**
      * Checks the type correctness of a given conditional statement.
      * @param conditional conditional to check
      */
     @Override
     public void visit(Conditional conditional) {
         conditional.getCondition().accept(this);
         if (!(tempType instanceof BooleanType)) {
             throw new IllegalTypeException("Condition must be of boolean type!",
                                            conditional.getPosition());
         }
         currentScope = new Scope(currentScope,
                                  conditional.getTrueConditionBody(), null);
         conditional.getTrueConditionBody().accept(this);
         currentScope = currentScope.getParent();
         if (conditional.getFalseConditionBody() != null) {
             currentScope = new Scope(currentScope,
                                      conditional.getFalseConditionBody(), null);
             conditional.getFalseConditionBody().accept(this);
             currentScope = currentScope.getParent();
         }
     }
 
     /**
      * Checks the type correctness of a given loop statement.
      * @param loop loop to check
      */
     @Override
     public void visit(Loop loop) {
         loop.getCondition().accept(this);
         if (!(tempType instanceof BooleanType)) {
             throw new IllegalTypeException("Condition must be of boolean type!",
                                            loop.getPosition());
         }
         Invariant[] invariants = loop.getInvariants();
         for (Invariant invariant : invariants) {
             invariant.accept(this);
         }
         currentScope = new Scope(currentScope, loop.getLoopBody(), null);
         loop.getLoopBody().accept(this);
         currentScope = currentScope.getParent();
         Ensure[] ensures = loop.getPostconditions();
         for (Ensure ensure : ensures) {
             ensure.accept(this);
         }
     }
 
     /**
      * Checks the type correctness of a given array assignment statement.
      * @param arrayAssignment array assignment to check
      */
     @Override
     public void visit(ArrayAssignment arrayAssignment) {
         HashMap<Identifier, Value> vars = currentScope.getVariables();
         Identifier identifier = arrayAssignment.getIdentifier();
         Value value = vars.get(identifier);
         arrayAssignment.getValue().accept(this);
         Type type = baseType(value.getType(), arrayAssignment.getIndices().length,
                      arrayAssignment.getPosition());
         if (type instanceof ArrayType) {
             throw new IllegalTypeException("Cannot assign a value to an array "
                                            + "that is not fully indexed!",
                                             arrayAssignment.getPosition());
         }
         if (!type.equals(tempType)) {
             throw new IllegalTypeException("Base type of the array does not "
                                            + "match the type of assigned value",
                                            arrayAssignment.getPosition());
         }
         arrayAssignment.setDepth(
                 currentScope.getDepthOfVariable(arrayAssignment.getIdentifier()));
         arrayAssignment.setType(value.getType());
     }
 
     /**
      * Checks the type correctness of a given numeric literal.
      * There will be no type error.
      * @param number literal to check
      */
     @Override
     public void visit(NumericLiteral number) {
         tempType = new IntegerType();
     }
 
     /**
      * Checks the type correctness of a given arithmetic expression.
      * @param arithmeticExpression expression to check
      */
     @Override
     public void visit(ArithmeticExpression arithmeticExpression) {
         arithmeticExpression.getSubexpression1().accept(this);
         ArithmeticOperator operator =
                 arithmeticExpression.getArithmeticOperator();
         if (operator instanceof BinaryOperator) {
             Type tempType1 = tempType;
             arithmeticExpression.getSubexpression2().accept(this);
             if (!(tempType instanceof IntegerType)
                || !(tempType1 instanceof IntegerType)) {
                 throw new IllegalTypeException("Operands must be integer "
                                                + "expressions!",
                                             arithmeticExpression.getPosition());
             }
         } else {
             //UnaryMinus
             if (!(tempType instanceof IntegerType)) {
                 throw new IllegalTypeException("Operand must be an integer "
                                                + "expression!",
                                             arithmeticExpression.getPosition());
             }
         }
         tempType = new IntegerType();
     }
 
     /**
      * Checks the type correctness of a given boolean literal.
      * There will be no type error.
      * @param bool literal to check
      */
     @Override
     public void visit(BooleanLiteral bool) {
         tempType = new BooleanType();
     }
 
     /**
      * Checks the type correctness of a given logical expression.
      * @param logicalExpression expression to check
      */
     @Override
     public void visit(LogicalExpression logicalExpression) {
         Position position = logicalExpression.getPosition();
         logicalExpression.getSubexpression1().accept(this);
         LogicalOperator operator = logicalExpression.getLogicalOperator();
         if (operator instanceof BinaryOperator) {
             Type tempType1 = tempType;
             logicalExpression.getSubexpression2().accept(this);
             if (operator instanceof Conjunction
                     || operator instanceof Disjunction) {
                 if (!(tempType instanceof BooleanType)
                         || !(tempType1 instanceof BooleanType)) {
                     throw new IllegalTypeException("Operands must be "
                                                     + "boolean expressions!",
                                                     position);
                 }
             } else if (operator instanceof Equal
                        || operator instanceof NotEqual) {
                 if (!tempType.equals(tempType1)) {
                     throw new IllegalTypeException("Operands must be of "
                                                     + "equal types!",
                                                     position);
                 }
             } else {
                 //greater, greater equal, less, less equal
                 if (!(tempType instanceof IntegerType)
                         || !(tempType1 instanceof IntegerType)) {
                     throw new IllegalTypeException("Operands must be "
                                                    + "integer expressions!",
                                                     position);
                 }
             }
         } else {
             //Negation
             if (!(tempType instanceof BooleanType)) {
                 throw new
                   IllegalTypeException("Operand must be a logical expression!",
                                        position);
             }
         }
         tempType = new BooleanType();
     }
 
     /**
      * Checks the type correctness of a given function call
      * and inserts the correct function reference.
      * @param functionCall function call to check
      */
     @Override
     public void visit(FunctionCall functionCall) {
         if ("length".equals(functionCall.getFunctionIdentifier().toString())) {
             if (functionCall.getParameters().length != 1
                     || !(functionCall.getParameters()[0] instanceof VariableRead)) {
                 throw new IllegalTypeException("Parameter of 'length' must be exactly "
                                                + "one array!", functionCall.getPosition());
             }
             functionCall.getParameters()[0].accept(this);
             if (!(tempType instanceof ArrayType)) {
                 throw new IllegalTypeException("Parameter of 'length' must be an "
                                                + "array!", functionCall.getPosition());
             }
             tempType = new IntegerType();
             return;
         }
         if (!functionCallAllowed) {
             throw new FunctionCallNotAllowedException("Function call not "
                                                       + "allowed here!",
                                                     functionCall.getPosition());
         }
         String functionName = functionCall.getFunctionIdentifier().getName();
         Function callee = null;
         for (Function function : functions) {
             if (function.getName().equals(functionName)) {
                 callee = function;
             }
         }
         if (callee == null) {
             throw new IllegalTypeException("No such function:" + functionName,
                                            functionCall.getPosition());
         }
         functionCall.setFunction(callee);
         Expression[] parameterExpressions = functionCall.getParameters();
         FunctionParameter[] parameters = callee.getParameters();
         if (parameterExpressions.length != parameters.length) {
             throw new IllegalTypeException("Wrong number of parameters used!",
                                             functionCall.getPosition());
         }
         for (int i = 0; i < parameters.length; i++) {
             parameterExpressions[i].accept(this);
             if (!tempType.equals(parameters[i].getType())) {
                 throw new IllegalTypeException("Wrong type used as parameter!",
                                                functionCall.getPosition());
             }
         }
         tempType = callee.getReturnType();
     }
 
     /**
      * Checks the type correctness of a given reading variable access.
      * @param variableRead read expression to check
      */
     @Override
     public void visit(VariableRead variableRead) {
         HashMap<Identifier, Value> vars = currentScope.getVariables();
         Identifier identifier = variableRead.getVariable();
         Value value = vars.get(identifier);
         if (value == null) {
             throw new IllegalTypeException("Variable " + identifier.getName()
                                            + " was read but not declared!",
                                            variableRead.getPosition());
         }
         tempType = value.getType();
         variableRead.setType(tempType);
         variableRead.setDepth(currentScope.getDepthOfVariable(
                 variableRead.getVariable()));
     }
 
     /**
      * Checks the type correctness of a given reading array access.
      * @param arrayRead read expression to check
      */
     @Override
     public void visit(ArrayRead arrayRead) {
         HashMap<Identifier, Value> vars = currentScope.getVariables();
         Identifier identifier = arrayRead.getVariable();
         Value value = vars.get(identifier);
         if (value == null) {
             throw new IllegalTypeException("Variable " + identifier.getName()
                                            + " was read but not declared!",
                                            arrayRead.getPosition());
         }
         tempType = baseType(value.getType(), arrayRead.getIndices().length,
                      arrayRead.getPosition());
         arrayRead.setType(tempType);
         arrayRead.setDepth(currentScope.getDepthOfVariable(arrayRead.getVariable()));
     }
 
     /**
      * Checks the type correctness of a given function.
      * @param function function to check
      */
     @Override
     public void visit(Function function) {
         currentFunction = function;
         currentScope = new Scope(null, function.getFunctionBlock(), function);
         if (currentFunction.getReturnType() instanceof ArrayType) {
             throw new IllegalTypeException("Functions must not return arrays.",
                                            function.getPosition());
         }
         FunctionParameter[] params = function.getParameters();
         for (FunctionParameter param : params) {
             if (param.getType() instanceof ArrayType) {
                 int dimension = 0;
                 for (Type type = param.getType(); type instanceof ArrayType;
                      type = ((ArrayType) type).getType()) {
                     dimension += 1;
                 }
                 int[] lengths = new int[dimension];
                 for (int i = 0; i < lengths.length; i++) {
                     lengths[i] = 1;
                 }
                 currentScope.createArray(param.getName(), param.getType(),
                         lengths);
             } else {
                 currentScope.createVar(param.getName(), null, param.getType());
             }
         }
         Assumption[] assumptions = function.getAssumptions();
         for (Assumption assumption : assumptions) {
             assumption.accept(this);
         }
         function.getFunctionBlock().accept(this);
         Ensure[] ensures = currentFunction.getEnsures();
         for (Ensure ensure : ensures) {
             ensure.accept(this);
         }
         currentScope = currentScope.getParent();
     }
 
     /**
      * Checks the type correctness of a given program.
      * @param program program to check
      */
     @Override
     public void visit(Program program) {
         functionCallAllowed = true;
         for (Axiom axiom : program.getAxioms()) {
             axiom.accept(this);
         }
         functions = program.getFunctions();
         for (int i = 0; i < functions.length - 1; i++) {
             for (int j = i + 1; j < functions.length; j++) {
                 if (functions[i].getName().equals(functions[j].getName())) {
                     throw new IllegalTypeException("Function overloaded!",
                                                    program.getPosition());
                 }
             }
         }
         for (Function function : functions) {
             function.accept(this);
         }
         program.getMainFunction().accept(this);
     }
 
     /**
      * Checks the type correctness of a given variable assignment.
      * @param assignment assignment to check
      */
     @Override
     public void visit(Assignment assignment) {
         assignment.getValue().accept(this);
         HashMap<Identifier, Value> vars = currentScope.getVariables();
         Identifier identifier = assignment.getIdentifier();
         Value value = vars.get(identifier);
         if (value == null) {
             throw new IllegalTypeException("Variable " + identifier.getName()
                                            + " was read but not declared!",
                                            assignment.getPosition());
         }
         if (value.getType() instanceof ArrayType) {
             throw new IllegalTypeException("Cannot assign a value to an array "
                                            + "that is not fully indexed!",
                                            assignment.getPosition());
         }
         if (!value.getType().equals(tempType)) {
             throw new IllegalTypeException("Type of variable does not match "
                                            + "the type of assigned value!",
                                            assignment.getPosition());
         }
         assignment.setType(tempType);
         assignment.setDepth(
                 currentScope.getDepthOfVariable(assignment.getIdentifier()));
     }
 
     /**
      * Checks the type correctness of a given assertion.
      * @param assertion assertion to check
      */
     @Override
     public void visit(Assertion assertion) {
         assertion.getExpression().accept(this);
         if (!(tempType instanceof BooleanType)) {
             throw new IllegalTypeException("Expression must have bool type!",
                                             assertion.getPosition());
         }
     }
 
     /**
      * Checks the type correctness of a given assumption.
      * @param assumption assumption to check
      */
     @Override
     public void visit(Assumption assumption) {
         functionCallAllowed = false;
         assumption.getExpression().accept(this);
         functionCallAllowed = true;
         if (!(tempType instanceof BooleanType)) {
             throw new IllegalTypeException("Expression must have bool type!",
                                             assumption.getPosition());
         }
     }
 
     /**
      * Checks the type correctness of a given axiom.
      * @param axiom axiom to check
      */
     @Override
     public void visit(Axiom axiom) {
         functionCallAllowed = false;
         currentScope = new Scope(null, null, null);
         axiom.getExpression().accept(this);
         currentScope = currentScope.getParent();
         functionCallAllowed = true;
         if (!(tempType instanceof BooleanType)) {
             throw new IllegalTypeException("Expression must have bool type!",
                                             axiom.getPosition());
         }
     }
 
     /**
      * Checks the type correctness of a given ensure.
      * @param ensure ensure to check
      */
     @Override
     public void visit(Ensure ensure) {
         functionCallAllowed = false;
         ensure.getExpression().accept(this);
         functionCallAllowed = true;
         if (!(tempType instanceof BooleanType)) {
             throw new IllegalTypeException("Expression must have bool type!",
                                             ensure.getPosition());
         }
     }
 
     /**
      * Checks the type correctness of a given invariant.
      * @param invariant invariant to check
      */
     @Override
     public void visit(Invariant invariant) {
         functionCallAllowed = false;
         invariant.getExpression().accept(this);
         functionCallAllowed = true;
         if (!(tempType instanceof BooleanType)) {
             throw new IllegalTypeException("Expression must have bool type!",
                                             invariant.getPosition());
         }
     }
 
     /**
      * Checks the type correctness of a given return statement.
      * @param returnStatement return statement to check
      */
     @Override
     public void visit(ReturnStatement returnStatement) {
         Type currentReturnType = currentFunction.getReturnType();
         functionCallAllowed = false;
         returnStatement.getReturnValue().accept(this);
         functionCallAllowed = true;
         if (!currentReturnType.equals(tempType)) {
             throw new IllegalTypeException("Type of returned expression does "
                                 + "not match type that the function returns!",
                                 returnStatement.getPosition());
         }
     }
 
     /**
      * Checks the type correctness of a given variable declaration.
      * @param varDec declaration to check
      */
     @Override
     public void visit(VariableDeclaration varDec) {
         if (currentScope.existsInScope(new Identifier(varDec.getName()))) {
             throw new IllegalTypeException("Variable already defined in scope!",
                                            varDec.getPosition());
         }
         if (varDec.getType() instanceof ArrayType) {
             throw new IllegalTypeException("Arrays must be initialized " +
                                            "via \"array\" keyword!",
                                            varDec.getPosition());
         }
         if (varDec.getValue() != null) {
             varDec.getValue().accept(this);
             if (!varDec.getType().equals(tempType)) {
                 throw new IllegalTypeException("Type of variable does not match"
                                                + " the type of initial value",
                                                varDec.getPosition());
             }
         }
         currentScope.createVar(varDec.getName(), null, varDec.getType());
         varDec.setDepth(currentScope.getDepthOfVariable(
                 new Identifier(varDec.getName())));
     }
 
     /**
      * Checks the type correctness of a given array declaration.
      * @param arrDec declaration to check
      */
     @Override
     public void visit(ArrayDeclaration arrDec) {
         if (currentScope.existsInScope(new Identifier(arrDec.getName()))) {
             throw new IllegalTypeException("Array already declared in scope!",
                                            arrDec.getPosition());
         }
         if (!(arrDec.getType() instanceof ArrayType)) {
             throw new IllegalTypeException("\"array\" keyword only allowed " +
                                            "on array declaration!",
                                            arrDec.getPosition());    
         }
         Expression[] indices = arrDec.getIndices();
         if (baseType(arrDec.getType(), indices.length, arrDec.getPosition())
                 instanceof ArrayType) {
             throw new IllegalTypeException("Type of array declaration does not"
                                            + "match the number of indices",
                                            arrDec.getPosition());
         }
         int[] lengths = new int[indices.length];
         for (int i = 0; i < lengths.length; i++) {
             lengths[i] = 1;
         }
         currentScope.createArray(arrDec.getName(), arrDec.getType(), lengths);
         arrDec.setDepth(currentScope.getDepthOfVariable(
                 new Identifier(arrDec.getName())));
     }
 
     /**
      * Checks the type correctness of a given exists quantifier.
      * @param existsQuantifier quantifier to check
      */
     @Override
     public void visit(ExistsQuantifier existsQuantifier) {
         if (existsQuantifier.getRange() != null) {
             existsQuantifier.getRange().getLowerBound().accept(this);
             Type tempType1 = tempType;
             existsQuantifier.getRange().getUpperBound().accept(this);
             if (!(tempType instanceof IntegerType)
                         || !(tempType1 instanceof IntegerType)) {
                 throw new IllegalTypeException("Range bounds must be integer "
                                                + "expressions!",
                                                 existsQuantifier.getPosition());
             }
         }
         currentScope = new Scope(currentScope, null, null);
         Identifier ident = existsQuantifier.getIdentifier();
         currentScope.createVar(ident.getName(), null, new IntegerType());
         existsQuantifier.setDepth(currentScope.getDepthOfVariable(ident));
         functionCallAllowed = false;
         existsQuantifier.getSubexpression1().accept(this);
         functionCallAllowed = true;
         if (!(tempType instanceof BooleanType)) {
             throw new IllegalTypeException("Subexpression must have bool type!",
                                             existsQuantifier.getPosition());
         }
         currentScope = currentScope.getParent();
         tempType = new BooleanType();
     }
 
     /**
      * Checks the type correctness of a given for all quantifier.
      * @param forAllQuantifier quantifier to check
      */
     @Override
     public void visit(ForAllQuantifier forAllQuantifier) {
         if (forAllQuantifier.getRange() != null) {
             forAllQuantifier.getRange().getLowerBound().accept(this);
             Type tempType1 = tempType;
             forAllQuantifier.getRange().getUpperBound().accept(this);
             if (!(tempType instanceof IntegerType)
                         || !(tempType1 instanceof IntegerType)) {
                 throw new IllegalTypeException("Range bounds must be integer "
                                                + "expressions!",
                                                 forAllQuantifier.getPosition());
             }
         }
         currentScope = new Scope(currentScope, null, null);
         Identifier ident = forAllQuantifier.getIdentifier();
         currentScope.createVar(ident.getName(), null, new IntegerType());
         forAllQuantifier.setDepth(currentScope.getDepthOfVariable(ident));
         functionCallAllowed = false;
         forAllQuantifier.getSubexpression1().accept(this);
         functionCallAllowed = true;
         if (!(tempType instanceof BooleanType)) {
             throw new IllegalTypeException("Subexpression must have bool type!",
                                             forAllQuantifier.getPosition());
         }
         currentScope = currentScope.getParent();
         tempType = new BooleanType();
     }
 
     /**
      * Checks the type correctness of a given statement block.
      * @param statementBlock statement block to check
      */
     @Override
     public void visit(StatementBlock statementBlock) {
         Iterator<Statement> statements = statementBlock.getIterator();
         while (statements.hasNext()) {
             statements.next().accept(this);
         }
     }
 
     /**
      * Returns the type of the array in the specified depth.
      * For example, if i has type t, then baseType(t, 1, ...) returns the
      * type of i[], baseType(t, 2, ...) the type of i[][], ...
      *
      * An IllegalTypeException if thrown if the deconstruction of
      * the type goes too far and there is no array left.
      *
      * @param arrayType the original array type
      * @param depth specified depth
      * @param position position for the IllegalTypeException
      * @return type of the array in the specified depth
      */
     private static Type baseType(Type arrayType, int depth, Position position) {
         Type type = arrayType;
         for (int i = 0; i < depth; i++) {
             if (!(type instanceof ArrayType)) {
                 throw new IllegalTypeException("Variable was indexed although "
                                                 + "not an array!", position);
             }
             type = ((ArrayType) type).getType();
         }
         return type;
     }
 }
