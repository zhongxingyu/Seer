 package interpreter;
 
 import ast.*;
 import parser.IllegalTypeException;
 import parser.TypeChecker;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 
 /**
  * This class encapsulates the context of a program execution.
  */
 public class ProgramExecution {
     /**
      * state the program currently is in
      */
     private final State currentState;
     /**
      * list of this program's statement breakpoints
      */
     private ArrayList<StatementBreakpoint> statementBreakpoints;
     /**
      * list of this program's global breakpoints
      */
     private ArrayList<GlobalBreakpoint> globalBreakpoints;
     /**
      * type checker to evaluate the type correctness of global statements
      */
     private final TypeChecker typeChecker;
     /**
      * interpreter to evaluate breakpoint expressions 
      */
     private final Interpreter interpreter;
 
     /**
      * Constructor.
      * 
      * @param ast program whose execution context is saved
      * @param interpreter interpreter to evaluate breakpoint expressions
      */
     public ProgramExecution(ASTRoot ast, Interpreter interpreter, ArrayList<StatementBreakpoint> sbreakpoints,
     		ArrayList<GlobalBreakpoint> gbreakpoints, String[] parameterValues) {
         this.statementBreakpoints = sbreakpoints;
         this.globalBreakpoints = gbreakpoints;
         currentState = new State(ast);
         typeChecker = new TypeChecker();
         this.interpreter = interpreter;
         initParams(parameterValues);
         interpreter.checkAssumptions(currentState,
                 ((Program) ast).getMainFunction().getAssumptions());
     }
 
     private void initParams(String[] values) {
         FunctionParameter[] parameters = currentState.getCurrentFunction().getParameters();
         if (values == null)return;
         for (int i = 0; i < values.length; i++) {
             if (parameters[i].getType() instanceof ArrayType) {
                 initArray(parameters[i], values[i]);
             } else {
                 currentState.createVar(parameters[i].getName(),
                         values[i], parameters[i].getType());
             }
         }
     }
 
     public void initArray(FunctionParameter parameter, String value) {
     	Type type = parameter.getType();
         int depth = 0;
         while (type instanceof ArrayType) {
             type = ((ArrayType) type).getType();
             depth += 1;
         }
         int[] maxIndex = new int[depth];
         Integer[] counters = new Integer[depth];
         depth = 0;
         boolean valid = true;
         for (int i = 0; (i < value.length()) && valid; i++) {
             switch (value.charAt(i)) {
                 case '{':
                     if (depth < maxIndex.length) {
                         depth += 1;
                         counters[depth - 1] = 1;
                     } else {
                         valid = false;
                     }
                     break;
                 case ',':
                     if (depth > 0) {
                         counters[depth - 1] += 1;
                     } else {
                         valid = false;
                     }
                     break;
                 case '}':
                     if (depth > 0) {
                         if (counters[depth - 1] > maxIndex[depth - 1]) {
                             maxIndex[depth - 1] = counters[depth - 1];
                         }
                         depth -= 1;
                     } else {
                         valid = false;
                     }
                     break;
             }
         }
         for (int i = 0; i < maxIndex.length; i++) {
             if (maxIndex[i] <= 0 || !valid) {
                 maxIndex[i] = 1;
             }
         }
         currentState.createArray(parameter.getName(), parameter.getType(),
                 maxIndex);
         if (!valid) {
             return;
         }
         depth = 0;
         counters = new Integer[maxIndex.length];
         StringBuilder temp = new StringBuilder("");
         for (int i = 0; (i < value.length()) && valid; i++) {
             switch (value.charAt(i)) {
                 case '{':
                     temp = new StringBuilder("");
                     depth += 1;
                     counters[depth - 1] = 0;
                     break;
                 case ',':
                     if (depth == counters.length) {
                         currentState.setArray(parameter.getName(), temp.toString(),
                                 Arrays.asList(counters));
                     }
                     temp = new StringBuilder("");
                     counters[depth - 1] += 1;
                     break;
                 case '}':
                     if (depth == counters.length) {
                         currentState.setArray(parameter.getName(), temp.toString(),
                                 Arrays.asList(counters));
                     }
                     temp = new StringBuilder("");
                     depth -= 1;
                     break;
                 default:
                     temp.append(value.charAt(i));
             }
         }
     }
 
     public Breakpoint checkBreakpoints() {
         for (StatementBreakpoint statementBreakpoint : statementBreakpoints) {
         	if (currentState.getCurrentStatement().getPosition().getLine() 
         			== statementBreakpoint.getLine()) {
                 return statementBreakpoint;
             }
         }
         for (GlobalBreakpoint globalBreakpoint : globalBreakpoints) {
         	if (globalBreakpoint.isActive()) {
     			typeChecker.setCurrentScope(currentState.getCurrentScope());
     			try {
                     Expression condition = globalBreakpoint.getExpression();
                     Ensure ensure = new Ensure(new Position(), condition);
                     typeChecker.setFunctionCallAllowed(false);
                     ensure.accept(typeChecker);
                     typeChecker.setFunctionCallAllowed(true);
                     ensure.accept(interpreter);
                     return globalBreakpoint;
                 } catch (IllegalTypeException ignored) {
                 } catch (AssertionFailureException ignored) {
                 }
     		}
         }
         return null;
     }
 
     public State getCurrentState() {
         return currentState;
     }
 
     public ArrayList<StatementBreakpoint> getStatementBreakpoints() {
         return statementBreakpoints;
     }
 
     public ArrayList<GlobalBreakpoint> getGlobalBreakpoints() {
         return globalBreakpoints;
     }
 
     public void setStatementBreakpoints(
             ArrayList<StatementBreakpoint> statementBreakpoints) {
         this.statementBreakpoints = statementBreakpoints;
     }
 
     public void setGlobalBreakpoints(
             ArrayList<GlobalBreakpoint> globalBreakpoints) {
         this.globalBreakpoints = globalBreakpoints;
     }
     
     public HashMap<Identifier, Value> getVariables() {
         return currentState.getVariables();
     }
 }
