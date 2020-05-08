 package com.teamdev.students.calculator.implementation;
 
 import com.teamdev.students.calculator.intefaces.Evaluator;
 import com.teamdev.students.calculator.intefaces.Operation;
 
 import java.math.BigDecimal;
 import java.util.*;
 
 public class CalculatorEvaluator implements Evaluator<BigDecimal, Operation<BigDecimal>> {
 
     private Deque<FunctionStackElement> functionStack = new LinkedList<FunctionStackElement>();
     private Map<String, BigDecimal> valueMap = new HashMap<String, BigDecimal>();
     private Deque<String> operationStack = new LinkedList<String>();
     private Map<String, Operation<BigDecimal>> operationMap = new HashMap<String, Operation<BigDecimal>>();
     private List<String> outputQueue = new LinkedList<String>();
     private String operatorBaseName = "operator";
     private int operationCount;
     private String functionBaseName = "function";
     private String valueBaseName = "value";
     private int valueCount;
     private String leftParenthesisName = "leftParenthesis";
 
    /**
     * parses the output queue as RPN
     * @return - result of calculation
     */
     private BigDecimal evaluateResult() {
         if (!popOutOperationStack()) {
             return null;
         }
         for (ListIterator<String> iterator = outputQueue.listIterator(); iterator.hasNext(); ) {
             String element = iterator.next();
             if (!element.startsWith(valueBaseName)) {
                 iterator.remove();
                 Operation<BigDecimal> operation = operationMap.get(element);
                 int argumentsCount = operation.getArgumentsCount();
                 BigDecimal[] arguments = new BigDecimal[argumentsCount];
                 for (int i = 0; i < argumentsCount; ++i) {
                     arguments[i] = valueMap.get(iterator.previous());
                     iterator.remove();
                 }
                 String valueName = valueBaseName + valueCount++;
                BigDecimal tempResult = operation.getResult(arguments);
                if(tempResult == null){
                    return null;
                }
                valueMap.put(valueName, tempResult);
                 iterator.add(valueName);
             }
         }
         return valueMap.get(outputQueue.get(0));
     }
 
     private boolean popOutOperationStack() {
         while (operationStack.peek() != null) {
             if (operationStack.peek().equals(leftParenthesisName)) {
                 return false;
             }
             outputQueue.add(operationStack.pop());
         }
         return true;
     }
 
     @Override
     /**
      * adds value into valueMap by its name (value base name + current number of values)
      * adds value into the output queue
      * if there is a function in function stack and its current arguments count = 0, then increases the count
      * returns false only if there is a function in a function stack and it doesn't receive any arguments but value is pushed
      */
     public boolean pushValue(BigDecimal value) {
         String valueName = valueBaseName + valueCount++;
         valueMap.put(valueName, value);
         outputQueue.add(valueName);
         FunctionStackElement lastFunction = functionStack.peek();
         if (lastFunction != null && lastFunction.getAddedArgumentsCount() == 0) {
             return lastFunction.incrementArgumentsCount();
         }
         return true;
     }
 
     @Override
     /**
      *  adds an operator into operation map where key is a name =(operator base name + current operation count)
      *  and value is the operator
      *  pops out an operation stack while an element at the the top of the stack has less precedence
      *  than new operator or the new operator is left associative and has precedence less or equal to the top element
      */
     public void pushOperator(Operation<BigDecimal> operator) {
         String name = operatorBaseName + operationCount++;
         operationMap.put(name, operator);
         if (!operationStack.isEmpty()) {
             Operation<BigDecimal> peekedOperator = operationMap.get(operationStack.peek());
             while (peekedOperator != null
                     && (operator.getAssociativity() == Associativity.LEFT
                     && operator.getPrecedence() <= peekedOperator.getPrecedence()
                     || operator.getPrecedence() < peekedOperator.getPrecedence())
                     ) {
 
                 outputQueue.add(operationStack.pop());
                 peekedOperator = operationMap.get(operationStack.peek());
             }
         }
         operationStack.push(name);
     }
 
     @Override
     /**
      * pushes a left parenthesis operator into an operation stack
      */
     public void pushLeftParenthesis() {
         operationStack.push(leftParenthesisName);
     }
 
     @Override
     /**
      * pops out elements from an operation stack til the top element is a left parenthesis operator
      * the left parenthesis operator is popped out too and if the next top element at the operation stack
      * is a function (starts with function base name), then pops it out of the stack and adds it to the output queue
      * the function is popped out of the function stack
      * returns false if no left parenthesis operator is found or the popped function has wrong number of arguments
      */
     public boolean pushRightParenthesis() {
         String operator = operationStack.peek();
         while (operator != null) {
             if (!operator.startsWith(leftParenthesisName)) {
                 operationStack.pop();
                 outputQueue.add(operator);
             } else {
                 operationStack.pop();
                 operator = operationStack.peek();
                 if (operator != null && operator.startsWith(functionBaseName)) {
                     operationStack.pop();
                     outputQueue.add(operator);
                     FunctionStackElement lastFunction = functionStack.pop();
                     return lastFunction.hasMinimumArgumentsCount();
                 }
                 return true;
             }
             operator = operationStack.peek();
         }
         return false;
     }
 
     @Override
     /**
      * pushes a function into a function stack, operation stack and adds it to an operation map
      * if the function stack is not empty, then increases the number of arguments of the top function
      * returns false only if the function, which number of arguments was increases, doesn't take one more argument
      */
     public boolean pushFunction(Operation<BigDecimal> operator) {
         String name = functionBaseName + operationCount++;
         operationMap.put(name, operator);
         FunctionStackElement lastFunction = functionStack.peek();
         functionStack.push(new FunctionStackElement(operator));
         operationStack.push(name);
         if (lastFunction != null && lastFunction.getAddedArgumentsCount() == 0) {
             return lastFunction.incrementArgumentsCount();
         }
         return true;
     }
 
     @Override
     /**
      * pops out elements from an operation stack and adds it to output queue
      * til it's not a left parenthesis operator
      * increases a number of arguments of the function
      * returns false if no left parenthesis was found or the function doesn't take one more arguments
      */
     public boolean pushFunctionSeparator() {
         String operator = operationStack.peek();
         while (operator != null) {
             if (!operator.startsWith(leftParenthesisName)) {
                 operationStack.pop();
                 outputQueue.add(operator);
             } else {
                 FunctionStackElement lastFunction = functionStack.peek();
                 return lastFunction.incrementArgumentsCount();
             }
             operator = operationStack.peek();
         }
         return false;
     }
 
     @Override
     /**
      * returns a result
      */
     public BigDecimal getResult() {
         return evaluateResult();
     }
 
     private class FunctionStackElement {
         private int addedArgumentsCount;
         private Operation<BigDecimal> function;
 
         public FunctionStackElement(Operation<BigDecimal> function) {
             this.function = function;
         }
 
         public int getAddedArgumentsCount() {
             return addedArgumentsCount;
         }
 
         public boolean incrementArgumentsCount() {
             ++addedArgumentsCount;
             function.setArgumentsCount(addedArgumentsCount);
             return addedArgumentsCount <= function.getMaximumArgumentsCount();
         }
 
         public boolean hasMinimumArgumentsCount() {
             return addedArgumentsCount >= function.getMinimumArgumentsCount();
         }
     }
 }
