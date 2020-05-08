 package interpreter;
 
 import ast.*;
 
 import java.math.BigInteger;
 import java.util.ArrayList;
 import java.util.Arrays;
 
 /**
  * This class interprets the user program step by step.
  */
 public class Interpreter implements ASTVisitor {
     /**
      * temporarily saved value
      */
     private Value tempValue;
     /**
      * temporarily saved state
      */
     private State currentState;
     /**
      * temporarily saved return value
      */
     private Value returnValue;
     /**
      * temporarily saved function call parameters
      */
     private Value[] parameters;
 
     /**
      * exception to stop the execution of a statement in case of a
      * function call
      */
     private static class StopStatementException extends RuntimeException {
     }
 
     public State step(State state) {
         currentState = state;
         try {
             state.getCurrentStatement().accept(this);
         } catch (StopStatementException ignored) {
             //nothing to do
         }
         return state;
     }
 
     /**
      * Checks the assumptions, given a specified state.
      * @param state state used to evaluate the assumptions
      * @param assumptions assumptions to evaluate
      */
     public void checkAssumptions(State state, Assumption[] assumptions) {
         currentState = state;
         for (Assumption assumption : assumptions) {
             assumption.accept(this);
         }
     }
 
     @Override
     public void visit(Conditional conditional) {
         conditional.getCondition().accept(this);
         if (((BooleanValue) tempValue).getValue()) {
             currentState.createScope(conditional.getTrueConditionBody(),
                     null);
         } else if (conditional.getFalseConditionBody() != null) {
             currentState.createScope(conditional.getFalseConditionBody(),
                     null);
         }
         adjustStatement();
     }
 
     @Override
     public void visit(Loop loop) {
         loop.getCondition().accept(this);
         boolean condition = ((BooleanValue) tempValue).getValue();
         if (condition) {
             currentState.createScope(loop.getLoopBody(),
                                      null);
             adjustStatement();
         }
         Invariant[] invariants = loop.getInvariants();
         for (Invariant invariant : invariants) {
             invariant.accept(this);
         }
         if (!condition) {
             Ensure[] ensures = loop.getPostconditions();
             for (Ensure ensure : ensures) {
                 ensure.accept(this);
             }
             adjustStatement();
         }
     }
 
     @Override
     public void visit(ArrayAssignment arrayAssignment) {
         Expression[] indices = arrayAssignment.getIndices();
         ArrayList<Integer> lengths = new ArrayList<Integer>();
         for (Expression index : indices) {
             index.accept(this);
             int pos = ((IntegerValue) tempValue).getValue().intValue();
             lengths.add(pos);
         }
         arrayAssignment.getValue().accept(this);
         currentState.setArray(arrayAssignment.getIdentifier().toString(),
                               tempValue.toString(), lengths);
         adjustStatement();
     }
 
     @Override
     public void visit(ArithmeticExpression arithmeticExpression) {
         arithmeticExpression.getSubexpression1().accept(this);
         ArithmeticOperator operator =
                 arithmeticExpression.getArithmeticOperator();
         if (operator instanceof BinaryOperator) {
             BigInteger value1 = ((IntegerValue) tempValue).getValue();
             arithmeticExpression.getSubexpression2().accept(this);
             BigInteger value2 = ((IntegerValue) tempValue).getValue();
             if (operator instanceof Addition) {
                 BigInteger newValue = value1.add(value2);
                 tempValue = new IntegerValue(newValue.toString());
             }
             if (operator instanceof Subtraction) {
                 BigInteger newValue = value1.subtract(value2);
                 tempValue = new IntegerValue(newValue.toString());
             }
             if (operator instanceof Multiplication) {
                 BigInteger newValue = value1.multiply(value2);
                 tempValue = new IntegerValue(newValue.toString());
             }
             if (operator instanceof Division) {
                 BigInteger newValue = divide(value1, value2);
                 tempValue = new IntegerValue(newValue.toString());
             }
             if (operator instanceof Modulo) {
                 BigInteger newValue = value2.multiply(divide(value1, value2));
                 newValue = value1.subtract(newValue);
                 tempValue = new IntegerValue(newValue.toString());
             }
         } else {
             //Unary minus
             BigInteger newValue =
                     ((IntegerValue) tempValue).getValue().negate();
             tempValue = new IntegerValue(newValue.toString());
         }
     }
 
     public static BigInteger divide(BigInteger dividend, BigInteger divisor) {
         BigInteger result = dividend.divide(divisor);
         if (dividend.signum() == -1 &&
                 !dividend.remainder(divisor).equals(BigInteger.ZERO)) {
             result = result.subtract(
                     new BigInteger(divisor.signum(), new byte[]{1}));
         }
         return result;
     }
 
     @Override
     public void visit(NumericLiteral number) {
         tempValue = number.getValue();
     }
 
     @Override
     public void visit(LogicalExpression logicalExpression) {
         logicalExpression.getSubexpression1().accept(this);
         LogicalOperator operator = logicalExpression.getLogicalOperator();
         if (operator instanceof BinaryOperator) {
             Value tempValue1 = tempValue;
             logicalExpression.getSubexpression2().accept(this);
             Value tempValue2 = tempValue;
             if (operator instanceof Disjunction) {
                 boolean newValue = ((BooleanValue) tempValue1).getValue()
                         || ((BooleanValue) tempValue2).getValue();
                 tempValue = new BooleanValue(Boolean.toString(newValue));
             }
             if (operator instanceof Conjunction) {
                 boolean newValue = ((BooleanValue) tempValue1).getValue()
                         && ((BooleanValue) tempValue2).getValue();
                 tempValue = new BooleanValue(Boolean.toString(newValue));
             }
             if (operator instanceof Greater) {
                 boolean newValue =
                         ((IntegerValue) tempValue1).getValue().intValue()
                             > ((IntegerValue) tempValue2).getValue().intValue();
                 tempValue = new BooleanValue(Boolean.toString(newValue));
             }
             if (operator instanceof GreaterEqual) {
                 boolean newValue =
                         ((IntegerValue) tempValue1).getValue().intValue()
                            >= ((IntegerValue) tempValue2).getValue().intValue();
                 tempValue = new BooleanValue(Boolean.toString(newValue));
             }
             if (operator instanceof Less) {
                 boolean newValue =
                         ((IntegerValue) tempValue1).getValue().intValue()
                             < ((IntegerValue) tempValue2).getValue().intValue();
                 tempValue = new BooleanValue(Boolean.toString(newValue));
             }
             if (operator instanceof LessEqual) {
                 boolean newValue =
                         ((IntegerValue) tempValue1).getValue().intValue()
                            <= ((IntegerValue) tempValue2).getValue().intValue();
                 tempValue = new BooleanValue(Boolean.toString(newValue));
             }
             if (operator instanceof Equal) {
                 boolean newValue = tempValue1.equals(tempValue2);
                 tempValue = new BooleanValue(Boolean.toString(newValue));
             }
             if (operator instanceof NotEqual) {
                 boolean newValue = !tempValue1.equals(tempValue2);
                 tempValue = new BooleanValue(Boolean.toString(newValue));
             }
         } else {
             //Negation
             boolean newValue = !((BooleanValue) tempValue).getValue();
             tempValue = new BooleanValue(Boolean.toString(newValue));
         }
     }
 
     @Override
     public void visit(BooleanLiteral bool) {
         tempValue = bool.getValue();
     }
 
     @Override
     public void visit(FunctionCall functionCall) {
         if ("length".equals(functionCall.getFunctionIdentifier().toString())) {
             functionCall.getParameters()[0].accept(this);
             int arrayLength = ((ArrayValue) tempValue).getValues().length;
             tempValue = new IntegerValue(Integer.toString(arrayLength));
             return;
         }
         Value value = currentState.getReturnValues().get(functionCall);
         if (value == null && returnValue != null) {
             currentState.createFunctionResult(functionCall, returnValue);
             value = returnValue;
             returnValue = null;
         }
         if (value == null) {
             //result not calculated yet
             Expression[] parameterExpressions = functionCall.getParameters();
             parameters = new Value[parameterExpressions.length];
             for (int i = 0; i < parameters.length; i++) {
                 parameterExpressions[i].accept(this);
                 parameters[i] = tempValue;
             }
             functionCall.getFunction().accept(this);
             //statement must be started again
             throw new StopStatementException();
         } else {
             tempValue = value;
         }
     }
 
     @Override
     public void visit(VariableRead variableRead) {
         tempValue = currentState.getVariables().get(variableRead.getVariable());
     }
 
     @Override
     public void visit(ArrayRead arrayRead) {
         Value value = currentState.getVariables().get(arrayRead.getVariable());
         Expression[] indices = arrayRead.getIndices();
         for (Expression index : indices) {
             index.accept(this);
             int pos = ((IntegerValue) tempValue).getValue().intValue();
             Value[] values = ((ArrayValue) value).getValues();
             value = values[pos];
         }
         tempValue = value;
     }
 
     @Override
     public void visit(Function function) {
         currentState.createScope(function.getFunctionBlock(), function);
         FunctionParameter[] params = function.getParameters();
         for (int i = 0; i < params.length; i++) {
             Value value = parameters[i];
             String varName = params[i].getName();
             if (value instanceof ArrayValue) {
                 int depth = 0;
                 for (Type temp = value.getType(); temp instanceof ArrayType;
                      temp = ((ArrayType) temp).getType()) {
                     depth += 1;
                 }
                 int[] lengths = new int[depth];
                 depth = 0;
                 for (Value temp = value; temp instanceof ArrayValue;
                      temp = ((ArrayValue) temp).getValues()[0]) {
                     lengths[depth] = ((ArrayValue) temp).getValues().length;
                     depth += 1;
                 }
                 currentState.createArray(varName, value.getType(), lengths);
                 copyArray(varName, value, depth);
             } else {
                 currentState.createVar(varName, value.toString(),
                                        value.getType());
             }
         }
         Assumption[] assumptions = function.getAssumptions();
         for (Assumption assumption : assumptions) {
             assumption.accept(this);
         }
         adjustStatement();
     }
 
     /**
      * Copies an array in case of a method call.
      * @param varName new array name
      * @param value array value to copy
      * @param maxDepth depth of the array
      */
     private void copyArray(String varName, Value value, int maxDepth) {
         Integer[] counters = new Integer[maxDepth];
         helpArrayCopy(varName, value, counters, 0);
     }
 
     /**
      *
      * @param varName new array name
      * @param value array value to copy
      * @param counters counter for current position
      * @param depth depth of the array
      */
     private void helpArrayCopy(String varName, Value value,
                                Integer[] counters, int depth) {
         if (value instanceof ArrayValue) {
             int length = ((ArrayValue) value).getValues().length;
             for (int i = 0; i < length; i++) {
                 counters[depth] = i;
                 helpArrayCopy(varName, ((ArrayValue) value).getValues()[i],
                         counters, depth + 1);
             }
         } else {
             currentState.setArray(varName, value.toString(),
                     Arrays.asList(counters));
         }
     }
 
     @Override
     public void visit(Program program) {
         //no-op
     }
 
     @Override
     public void visit(Assignment assignment) {
         assignment.getValue().accept(this);
         currentState.setVar(assignment.getIdentifier().getName(),
                 tempValue.toString());
         adjustStatement();
     }
 
     @Override
     public void visit(Assertion assertion) {
         assertion.getExpression().accept(this);
         if (!((BooleanValue) tempValue).getValue()) {
             throw new AssertionFailureException("Assertion failed!",
                                                 assertion.getPosition());
         }
         adjustStatement();
     }
 
     @Override
     public void visit(Assumption assumption) {
         assumption.getExpression().accept(this);
         if (!((BooleanValue) tempValue).getValue()) {
             throw new AssertionFailureException("Assumption failed!",
                                                 assumption.getPosition());
         }
     }
 
     @Override
     public void visit(Axiom axiom) {
         //not needed in interpreter
     }
 
     @Override
     public void visit(Ensure ensure) {
         ensure.getExpression().accept(this);
         if (!((BooleanValue) tempValue).getValue()) {
             throw new AssertionFailureException("Ensure failed!",
                                                 ensure.getPosition());
         }
     }
 
     @Override
     public void visit(Invariant invariant) {
         invariant.getExpression().accept(this);
         if (!((BooleanValue) tempValue).getValue()) {
             throw new AssertionFailureException("Invariant failed!",
                                                 invariant.getPosition());
         }
     }
 
     @Override
     public void visit(ReturnStatement returnStatement) {
         returnStatement.getReturnValue().accept(this);
         returnValue = tempValue;
         adjustStatement();
     }
 
     @Override
     public void visit(VariableDeclaration varDec) {
         if (varDec.getValue() == null) {
             currentState.createVar(varDec.getName(), null, varDec.getType());
         } else {
             varDec.getValue().accept(this);
             currentState.createVar(varDec.getName(),
                         tempValue.toString(), varDec.getType());
         }
         adjustStatement();
     }
 
     @Override
     public void visit(ArrayDeclaration arrDec) {
         Expression[] indices = arrDec.getIndices();
         int[] lengths = new int[indices.length];
         for (int i = 0; i < indices.length; i++) {
             indices[i].accept(this);
             lengths[i] = ((IntegerValue) tempValue).getValue().intValue();
         }
         currentState.createArray(arrDec.getName(), arrDec.getType(), lengths);
         adjustStatement();
     }
 
     @Override
     public void visit(ExistsQuantifier existsQuantifier) {
         Range range = existsQuantifier.getRange();
         if (range != null) {
             range.getLowerBound().accept(this);
             int lower = ((IntegerValue) tempValue).getValue().intValue();
             range.getUpperBound().accept(this);
             int upper = ((IntegerValue) tempValue).getValue().intValue();
             currentState.createScope(null, null);
             String varName = existsQuantifier.getIdentifier().toString();
             currentState.createVar(varName, null, new IntegerType());
             boolean satisfied = false;
             for (int i = lower; i <= upper; i++) {
                 currentState.setVar(varName, Integer.toString(i));
                 existsQuantifier.getSubexpression1().accept(this);
                 if (((BooleanValue) tempValue).getValue()) {
                     //found a satisfying value
                     satisfied = true;
                 }
             }
             tempValue = new BooleanValue(Boolean.toString(satisfied));
             currentState.destroyScope();
         } else {
             tempValue = new BooleanValue("true");
         }
     }
 
     @Override
     public void visit(ForAllQuantifier forAllQuantifier) {
         Range range = forAllQuantifier.getRange();
         if (range != null) {
             range.getLowerBound().accept(this);
             int lower = ((IntegerValue) tempValue).getValue().intValue();
             range.getUpperBound().accept(this);
             int upper = ((IntegerValue) tempValue).getValue().intValue();
             currentState.createScope(null, null);
             String varName = forAllQuantifier.getIdentifier().toString();
             currentState.createVar(varName, null, new IntegerType());
             boolean valid = true;
             for (int i = lower; i <= upper; i++) {
                 currentState.setVar(varName, Integer.toString(i));
                 forAllQuantifier.getSubexpression1().accept(this);
                 if (!((BooleanValue) tempValue).getValue()) {
                     //found a counterexample
                     valid = false;
                 }
             }
             tempValue = new BooleanValue(Boolean.toString(valid));
             currentState.destroyScope();
         } else {
             tempValue = new BooleanValue("false");
         }
     }
 
     @Override
     public void visit(StatementBlock statementBlock) {
         //not needed
     }
 
     /**
      * Adjust the statement at the end of a step.
      */
     private void adjustStatement() {
         Function currentFunction = currentState.getCurrentFunction();
         currentState.adjustStatement();
         if (currentState.getCurrentStatement() == null) {
             if (currentState.isFunctionScope()) {
                 Ensure[] ensures = currentFunction.getEnsures();
                 try {
                     for (Ensure ensure : ensures) {
                         ensure.accept(this);
                     }
                 } finally {
                     currentState.destroyScope();
                 }
             } else {
                 currentState.destroyScope();
                 if (currentState.getCurrentStatement() instanceof Conditional) {
                     adjustStatement();
                 }
             }
         }
     }
 }
