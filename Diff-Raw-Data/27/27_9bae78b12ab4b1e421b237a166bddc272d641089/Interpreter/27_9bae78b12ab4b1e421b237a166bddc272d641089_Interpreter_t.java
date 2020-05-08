 /**
  * Lisp Subinterpreter, an interpreter for a sublanguage of Lisp
  * Copyright (C) 2011  Meisam Fathi Salmi <fathi@cse.ohio-state.edu>
  * 
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package edu.osu.cse.meisam.interpreter;
 
 import java.io.PrintStream;
 import java.util.Iterator;
 import java.util.Vector;
 
 import edu.osu.cse.meisam.interpreter.FunctionList.FunctionDefinition;
 import edu.osu.cse.meisam.interpreter.esxpression.BinaryExpression;
 import edu.osu.cse.meisam.interpreter.esxpression.BooleanAtomExpression;
 import edu.osu.cse.meisam.interpreter.esxpression.DefunAtomExpression;
 import edu.osu.cse.meisam.interpreter.esxpression.LeafExpression;
 import edu.osu.cse.meisam.interpreter.esxpression.NumericAtomExpression;
 import edu.osu.cse.meisam.interpreter.esxpression.SExpression;
 import edu.osu.cse.meisam.interpreter.esxpression.ValuedAtomExpression;
 import edu.osu.cse.meisam.interpreter.tokens.Atom;
 import edu.osu.cse.meisam.interpreter.tokens.LiteralAtom;
 import edu.osu.cse.meisam.interpreter.tokens.NumericAtom;
 import edu.osu.cse.meisam.interpreter.tokens.Token;
 
 /**
  * @author Meisam Fathi Salmi <fathi@cse.ohio-state.edu>
  * 
  */
 public class Interpreter {
 
     private static String[] reservedWord = { "T", "NIL", "CAR", "CDR", "CONS",
             "ATOM", "EQ", "NULL", "INT", "PLUS", "MINUS", "TIMES", "QUOTIENT",
             "REMAINDER", "LESS", "GREATER", "COND", "QUOTE", "DEFUN" };
 
     private final InputProvider in;
     private final Lexer lexer;
     private final Parser parser;
     private final PrintStream out;
     private final FunctionList functionList;
 
     public Interpreter(final InputProvider in, final PrintStream out) {
         this.out = out;
         this.in = in;
         this.functionList = new FunctionList();
         this.lexer = new Lexer(this.in);
         this.parser = new Parser(this.lexer);
     }
 
     /**
      * @param args
      */
     public static void main(final String[] args) {
         try {
             final Interpreter interpreter = new Interpreter(
                     new InputStreamProvider(System.in), System.out);
             interpreter.interpret();
         } catch (final Exception ex) {
             System.out.println("Error: " + ex.getMessage());
         }
 
     }
 
     /**
      * 
      */
     public void interpret() {
         do {
             final ParseTree parseTree = this.parser.parseNextSExpresion();
             if (parseTree == null) {
                 break;
             }
             final ParameterBindings initialBindings = new ParameterBindings();
             final SExpression evaluatedExpression = evaluate(parseTree,
                     initialBindings);
             prettyPrint(evaluatedExpression);
             System.out.println();
         } while (true);
     }
 
     /**
      * @param parseTree
      * @param bindings
      */
     private SExpression evaluate(final ParseTree parseTree,
             final ParameterBindings bindings) {
         if (parseTree instanceof LeafNode) {
             return evaluateNode((LeafNode) parseTree, bindings);
         } else if (parseTree instanceof InternalNode) {
             final InternalNode internalNode = (InternalNode) parseTree;
             final ParseTree leftTree = internalNode.getLeftTree();
             final ParseTree rightTree = internalNode.getRightTree();
             if (leftTree != null) {
                 if (leftTree instanceof LeafNode) {
                     return apply((LeafNode) leftTree, rightTree, bindings);
                 } else {
                     return raiseInterpreterException("Expecting to see an atom");
                 }
 
             } else if ((leftTree == null) && (rightTree == null)) {
                 return evaluateNil();
             } else {
                 return null;
             }
         } else {
             return raiseInterpreterException("Invalid S-Expression");
         }
     }
 
     private SExpression evaluateNil() {
         return BooleanAtomExpression.NIL;// FIXME
     }
 
     /**
      * @param leafNode
      * @param rightTree
      * @param bindings
      */
     private SExpression apply(final LeafNode leafNode,
             final ParseTree rightTree, final ParameterBindings bindings) {
         assertTrue("leafnode is null", leafNode != null);
         final Token token = leafNode.getToken();
         assertTrue("token in the leaf node is null", token != null);
         assertTrue(token.getLexval() + " is not a LiteralAtom",
                 token instanceof LiteralAtom);
         final Atom atom = (LiteralAtom) token;
         final String lexval = atom.getLexval();
         if (lexval.equals("T")) {
             return applyT(rightTree, bindings);
         } else if (lexval.equals("NIL")) {
             return applyNil(rightTree, bindings);
         } else if (lexval.equals("CAR")) {
             return applyCar(rightTree, bindings);
         } else if (lexval.equals("CDR")) {
             return applyCdr(rightTree, bindings);
         } else if (lexval.equals("CONS")) {
             return applyCons(rightTree, bindings);
         } else if (lexval.equals("ATOM")) {
             return applyAtom(rightTree, bindings);
         } else if (lexval.equals("EQ")) {
             return applyEq(rightTree, bindings);
         } else if (lexval.equals("NULL")) {
             return applyNull(rightTree, bindings);
         } else if (lexval.equals("INT")) {
             return applyInt(rightTree, bindings);
         } else if (lexval.equals("PLUS")) {
             return applyPlus(rightTree, bindings);
         } else if (lexval.equals("MINUS")) {
             return applyMinus(rightTree, bindings);
         } else if (lexval.equals("TIMES")) {
             return applyTimes(rightTree, bindings);
         } else if (lexval.equals("QUOTIENT")) {
             return applyQuotient(rightTree, bindings);
         } else if (lexval.equals("REMAINDER")) {
             return applyRemainder(rightTree, bindings);
         } else if (lexval.equals("LESS")) {
             return applyLess(rightTree, bindings);
         } else if (lexval.equals("GREATER")) {
             return applyGreater(rightTree, bindings);
         } else if (lexval.equals("COND")) {
             return applyCond(rightTree, bindings);
         } else if (lexval.equals("QUOTE")) {
             return applyQuote(rightTree, bindings);
         } else if (lexval.equals("DEFUN")) {
             return applyDefun(rightTree);
         } else {
             return applyFuntion(token.getLexval(), rightTree, bindings);
         }
     }
 
     private SExpression applyFuntion(final String functionName,
             final ParseTree paramsTree, final ParameterBindings bindings) {
         final FunctionDefinition functionDefinitions = this.functionList
                 .lookup(functionName);
         final ParseTree[] actualParams = getAllParams(paramsTree, functionName);
         final LeafNode[] formalParams = functionDefinitions.getFormalParams();
         assertTrue(
                 "Number of actual parameters and formal parameters does not mach for "
                         + functionName,
                 actualParams.length == formalParams.length);
 
         final ParameterBindings parameterBindings = new ParameterBindings();
         for (int i = 0; i < actualParams.length; i++) {
             final SExpression evaluatedParam = evaluate(actualParams[i],
                     bindings);
             parameterBindings.addBinding(
                     formalParams[i].getToken().getLexval(), evaluatedParam);
         }
 
         return evaluate(functionDefinitions.getBody(), parameterBindings);
     }
 
     private SExpression applyT(final ParseTree params,
             final ParameterBindings bindings) {
         return raiseInterpreterException("cannot apply T");
     }
 
     private SExpression applyNil(final ParseTree params,
             final ParameterBindings bindings) {
         return raiseInterpreterException("cannot apply NIL");
     }
 
     private SExpression applyCar(final ParseTree params,
             final ParameterBindings bindings) {
         final ParseTree firstParam = extractOnlyParameter(params, "CAR");
         final InternalNode firstParamTree = castAsTree(firstParam, "CAR");
         final SExpression evaluatedParam = evaluate(firstParamTree, bindings);
         assertTrue("CAR cannot operate on Atoms",
                 evaluatedParam instanceof BinaryExpression);
         return ((BinaryExpression) evaluatedParam).getHead();
     }
 
     private SExpression applyCdr(final ParseTree params,
             final ParameterBindings bindings) {
         final ParseTree firstParam = extractOnlyParameter(params, "CAR");
         final InternalNode firstParamTree = castAsTree(firstParam, "CAR");
         final SExpression evaluatedParam = evaluate(firstParamTree, bindings);
         assertTrue("CDR cannot operate on Atoms",
                 evaluatedParam instanceof BinaryExpression);
         return ((BinaryExpression) evaluatedParam).getTail();
     }
 
     private SExpression applyCons(final ParseTree params,
             final ParameterBindings bindings) {
         final ParseTree[] allParams = getAllParams(params, "CONS");
         assertTrue("CONS needs two parameters", allParams.length == 2);
         final ParseTree firstParam = allParams[0];
         final ParseTree secondParam = allParams[1];
 
         final SExpression evaluateFirst = evaluate(firstParam, bindings);
         final SExpression evaluateSecond = evaluate(secondParam, bindings);
 
         return new BinaryExpression(evaluateFirst, evaluateSecond);
     }
 
     private SExpression applyAtom(final ParseTree params,
             final ParameterBindings bindings) {
         final ParseTree paramTree = extractOnlyParameter(params, "ATOM");
         if (paramTree == InternalNode.NILL_LEAF) {
             return BooleanAtomExpression.T;
         } else if (paramTree instanceof LeafNode) {
             return BooleanAtomExpression.T;
         } else if (paramTree instanceof InternalNode) {
             return BooleanAtomExpression.NIL;
         } else {
             return raiseInterpreterException("Expression of front of ATOM");
         }
     }
 
     private SExpression applyEq(final ParseTree params,
             final ParameterBindings bindings) {
         final String operationName = "EQ";
         assertTrue("Looking for a list of two ints in front of "
                 + operationName, params instanceof InternalNode);
 
         final InternalNode paramsTree = (InternalNode) params;
         final ParseTree firstParam = paramsTree.getLeftTree();
         assertTrue("list of parameters to " + operationName
                 + " has less that two elements",
                 firstParam != InternalNode.NILL_LEAF);
 
         final ParseTree tailParams = paramsTree.getRightTree();
         assertTrue("Parameters to " + operationName + " are invalid",
                 tailParams instanceof InternalNode);
         final InternalNode tailParamTree = (InternalNode) tailParams;
 
         final ParseTree secondParam = tailParamTree.getLeftTree();
         assertTrue("list of parameters to " + operationName
                 + " has more that two elements",
                 secondParam != InternalNode.NILL_LEAF);
 
         final ParseTree nilParamTree = tailParamTree.getRightTree();
         assertTrue("list of parameters to " + operationName
                 + " has more than two elements",
                 nilParamTree == InternalNode.NILL_LEAF);
 
         if ((firstParam == InternalNode.NILL_LEAF)
                 && (secondParam == InternalNode.NILL_LEAF)) {
             return BooleanAtomExpression.T;
 
        } else {
 
            final SExpression evaluatedFirstParam = evaluate(firstParam,
                    bindings);
            final SExpression evaluatedSecondParam = evaluate(secondParam,
                    bindings);
 
            assertTrue(
                    "Invalid Arguments for EQ",
                    ((evaluatedFirstParam != null) && (evaluatedSecondParam != null)));
            assertTrue(
                    "EQ can only be applied on atoms of valid types",
                    ((evaluatedFirstParam instanceof ValuedAtomExpression) && (evaluatedSecondParam instanceof ValuedAtomExpression)) //
                            || //
                            ((evaluatedFirstParam instanceof NumericAtomExpression) && (evaluatedSecondParam instanceof NumericAtomExpression)) //
                            || //
                            ((evaluatedFirstParam instanceof BooleanAtomExpression) && (evaluatedSecondParam instanceof BooleanAtomExpression)));
            return evaluatedFirstParam.equals(evaluatedSecondParam) ? BooleanAtomExpression.T
                     : BooleanAtomExpression.NIL;
         }
 
     }
 
     private SExpression applyNull(final ParseTree params,
             final ParameterBindings bindings) {
         final ParseTree paramTree = extractOnlyParameter(params, "NULL");
         final SExpression result = evaluate(paramTree, bindings);
         if (result == BooleanAtomExpression.NIL) {
             return BooleanAtomExpression.T;
         } else {
             return BooleanAtomExpression.NIL;
         }
     }
 
     private SExpression applyInt(final ParseTree params,
             final ParameterBindings bindings) {
         final ParseTree paramTree = extractOnlyParameter(params, "INT");
         final SExpression result = evaluate(paramTree, bindings);
         if (result instanceof NumericAtomExpression) {
             return BooleanAtomExpression.T;
         } else {
             return BooleanAtomExpression.NIL;
         }
     }
 
     private SExpression applyPlus(final ParseTree params,
             final ParameterBindings bindings) {
         return applyAritmeticOperation(params, "PLUS", bindings);
     }
 
     private SExpression applyMinus(final ParseTree params,
             final ParameterBindings bindings) {
         return applyAritmeticOperation(params, "MINUS", bindings);
     }
 
     private SExpression applyTimes(final ParseTree params,
             final ParameterBindings bindings) {
         return applyAritmeticOperation(params, "TIMES", bindings);
     }
 
     private SExpression applyQuotient(final ParseTree params,
             final ParameterBindings bindings) {
         return applyAritmeticOperation(params, "QUOTIENT", bindings);
     }
 
     private SExpression applyRemainder(final ParseTree params,
             final ParameterBindings bindings) {
         return applyAritmeticOperation(params, "REMAINDER", bindings);
     }
 
     private SExpression applyLess(final ParseTree params,
             final ParameterBindings bindings) {
         return applyAritmeticOperation(params, "LESS", bindings);
     }
 
     private SExpression applyGreater(final ParseTree params,
             final ParameterBindings bindings) {
         return applyAritmeticOperation(params, "GREATER", bindings);
     }
 
     private SExpression applyCond(final ParseTree params,
             final ParameterBindings bindings) {
         final ParseTree[] allParams = getAllParams(params, "COND");
         assertParamsInPair(allParams, "COND");
         for (int i = 0; i < allParams.length; i++) {
             final ParseTree[] pairParams = getAllParams(allParams[i], "COND");
             assertTrue("Parameters for COND are malformed",
                     pairParams.length == 2);
             final ParseTree conditionParam = pairParams[0];
             final ParseTree bodyParam = pairParams[1];
 
             final SExpression conditionResult = evaluate(conditionParam,
                     bindings);
             if (conditionResult == BooleanAtomExpression.T) {
                 return evaluate(bodyParam, bindings);
             }
         }
         return raiseInterpreterException("None of the conditions in the COND satisfied");
     }
 
     private SExpression applyQuote(final ParseTree params,
             final ParameterBindings bindings) {
         final ParseTree[] functionParams = getAllParams(params, "QUOTE");
         assertTrue("QUOTE expects to exactly one parameter",
                 functionParams.length == 1);
         assertTrue("QUOTE expects to exactly one parameter",
                 functionParams[0] != null);
         if (functionParams[0] instanceof LeafNode) {
             return returnQuotedAtom(functionParams[0]);
         } else if (functionParams[0] instanceof InternalNode) {
             return recursiveQuote(functionParams[0], bindings);
         } else {
             return raiseInterpreterException("Unknown structure for the QUOTE function");
         }
     }
 
     private SExpression returnQuotedAtom(final ParseTree functionParams) {
         final LeafNode leafNode = castAsLeafNode(functionParams, "QUOTE");
         final Token token = leafNode.getToken();
         if (token instanceof LiteralAtom) {
             return new ValuedAtomExpression(token.getLexval());
         } else if (token instanceof NumericAtom) {
             return new NumericAtomExpression(token.getLexval());
         } else {
             return raiseInterpreterException("Unknown structure for the QUOTE function");
         }
     }
 
     private SExpression recursiveQuote(final ParseTree parseTree,
             final ParameterBindings bindings) {
         if (parseTree == InternalNode.NILL_LEAF) {
             return BooleanAtomExpression.NIL;
         } else if (parseTree instanceof LeafNode) {
             return returnQuotedAtom(parseTree);
         } else if (parseTree instanceof InternalNode) {
             final InternalNode internalNode = castAsTree(parseTree, "QUOTE");
             final SExpression leftTreeQuote = recursiveQuote(
                     internalNode.getLeftTree(), bindings);
             final SExpression rightTreeQuote = recursiveQuote(
                     internalNode.getRightTree(), bindings);
             return new BinaryExpression(leftTreeQuote, rightTreeQuote);
         }
         return raiseInterpreterException("Invalid arguments for QUOTE");
     }
 
     private SExpression applyDefun(final ParseTree params) {
         final ParseTree[] allParams = getAllParams(params, "DEFUN");
         assertTrue("DEFUN should have a parameters list and a body",
                 allParams.length == 3);
         final ParseTree functionNameTree = allParams[0];
         validateName(functionNameTree);
         final LeafNode functionName = castAsLeafNode(functionNameTree, "DEFUN");
         final ParseTree formalsList = allParams[1];
         final ParseTree body = allParams[2];
         final LeafNode[] allFormalParams = extractFormalParams(formalsList,
                 "DEFUN");
         validateFormalsList(allFormalParams);
 
         this.functionList.add(functionName, allFormalParams, body);
         return new DefunAtomExpression(functionName.getToken().getLexval());
     }
 
     private void validateName(final ParseTree functionName) {
         assertTrue("Invalid name ", functionName != null);
         assertTrue("Invalid name ", functionName instanceof LeafNode);
         final LeafNode castAsLeafNode = castAsLeafNode(functionName, "DEFUN");
         for (int i = 0; i < Interpreter.reservedWord.length; i++) {
             assertTrue("Invalid function name",
                     !Interpreter.reservedWord[i].equals(castAsLeafNode
                             .getToken().getLexval()));
         }
     }
 
     private void validateFormalsList(final ParseTree[] allFormalParams) {
         for (int i = 0; i < allFormalParams.length; i++) {
             validateName(allFormalParams[i]);
 
             for (int j = i + 1; j < allFormalParams.length; j++) {
                 chechDuplicateNames(allFormalParams[i], allFormalParams[j]);
             }
         }
 
     }
 
     private void chechDuplicateNames(final ParseTree firstParam,
             final ParseTree secondParam) {
         assertTrue("Duplicate name in parameters list ", firstParam != null);
         assertTrue("Duplicate name in parameters list ", secondParam != null);
         assertTrue("Duplicate name in parameters list ",
                 firstParam instanceof LeafNode);
         assertTrue("Duplicate name in parameters list ",
                 secondParam instanceof LeafNode);
 
         final LeafNode firstParamCasted = castAsLeafNode(firstParam, "DEFUN");
         final LeafNode secondParamCasted = castAsLeafNode(secondParam, "DEFUN");
         assertTrue(
                 "Duplicate name in parameters list",
                 !firstParamCasted.getToken().getLexval()
                         .equals(secondParamCasted.getToken().getLexval()));
     }
 
     /**
      * @param leafNode
      * @param bindings
      * @return
      */
     private SExpression evaluateNode(final LeafNode leafNode,
             final ParameterBindings bindings) {
         final Token token = leafNode.getToken();
         if (token instanceof NumericAtom) {
             return new NumericAtomExpression(token.getLexval());
         } else if (token instanceof LiteralAtom) {
             final LiteralAtom literalAtom = (LiteralAtom) token;
             if (literalAtom.getLexval().equals("T")) {
                 return BooleanAtomExpression.T;
             } else if (literalAtom.getLexval().equals("NIL")) {
                 return BooleanAtomExpression.NIL;
             } else {
                 final String lexval = leafNode.getToken().getLexval();
                 return bindings.lookup(lexval);
             }
         }
         {
             this.out.println("Evaluating some leafnode");
         }
         return null;
     }
 
     private SExpression applyAritmeticOperation(final ParseTree params,
             final String operationName, final ParameterBindings bindings) {
         assertTrue("Looking for a list of two ints in front of "
                 + operationName, params instanceof InternalNode);
 
         final InternalNode paramsTree = (InternalNode) params;
         final ParseTree firstParam = paramsTree.getLeftTree();
         assertTrue("list of parameters to " + operationName
                 + " has less that two elements",
                 firstParam != InternalNode.NILL_LEAF);
 
         final ParseTree tailParams = paramsTree.getRightTree();
         assertTrue("Parameters to " + operationName + " are invalid",
                 tailParams instanceof InternalNode);
         final InternalNode tailParamTree = (InternalNode) tailParams;
 
         final ParseTree secondParam = tailParamTree.getLeftTree();
         assertTrue("list of parameters to " + operationName
                 + " has more that two elements",
                 secondParam != InternalNode.NILL_LEAF);
 
         final ParseTree nilParamTree = tailParamTree.getRightTree();
         assertTrue("list of parameters to " + operationName
                 + " has more than two elements",
                 nilParamTree == InternalNode.NILL_LEAF);
 
         final SExpression evaluatedFirstParam = evaluate(firstParam, bindings);
         final SExpression evaluatedSecondParam = evaluate(secondParam, bindings);
 
         assertTrue("First param of " + operationName
                 + " should evaluate to a number in ",
                 evaluatedFirstParam instanceof NumericAtomExpression);
         assertTrue("Second param of " + operationName
                 + " should evaluate to a number",
                 evaluatedSecondParam instanceof NumericAtomExpression);
 
         final NumericAtomExpression firstNumber = (NumericAtomExpression) evaluatedFirstParam;
         final NumericAtomExpression secondNumber = (NumericAtomExpression) evaluatedSecondParam;
 
         if (operationName.equals("PLUS")) {
             return new NumericAtomExpression(firstNumber.getVal()
                     + secondNumber.getVal());
         } else if (operationName.equals("MINUS")) {
             return new NumericAtomExpression(firstNumber.getVal()
                     - secondNumber.getVal());
         } else if (operationName.equals("TIMES")) {
             return new NumericAtomExpression(firstNumber.getVal()
                     * secondNumber.getVal());
         } else if (operationName.equals("QUOTIENT")) {
             return new NumericAtomExpression(firstNumber.getVal()
                     / secondNumber.getVal());
         } else if (operationName.equals("REMAINDER")) {
             return new NumericAtomExpression(firstNumber.getVal()
                     % secondNumber.getVal());
         } else if (operationName.equals("EQ")) {
             return firstNumber.getVal() == secondNumber.getVal() ? BooleanAtomExpression.T
                     : BooleanAtomExpression.NIL;
         } else if (operationName.equals("LESS")) {
             return firstNumber.getVal() < secondNumber.getVal() ? BooleanAtomExpression.T
                     : BooleanAtomExpression.NIL;
         } else if (operationName.equals("GREATER")) {
             return firstNumber.getVal() > secondNumber.getVal() ? BooleanAtomExpression.T
                     : BooleanAtomExpression.NIL;
         } else {
             return raiseInterpreterException(operationName
                     + " is not a know Operation");
         }
     }
 
     /**
      * @param params
      * @param functionName
      * @return
      */
     private ParseTree extractOnlyParameter(final ParseTree params,
             final String functionName) {
         if (params == InternalNode.NILL_LEAF) {
             raiseInterpreterException("Exactly one parameter is required for "
                     + functionName);
             return null; // never happens 'cause of raising error in the
                          // previous line
         }
         assertTrue("Exactly one parameter is required for " + functionName,
                 params instanceof InternalNode);
         final InternalNode paramTree = (InternalNode) params;
         assertTrue("Exactly one parameter is required for " + functionName,
                 paramTree.getRightTree() == InternalNode.NILL_LEAF);
         assertTrue("Exactly one parameter is required for " + functionName,
                 paramTree.getLeftTree() != InternalNode.NILL_LEAF);
         return paramTree.getLeftTree();
     }
 
     /**
      * @param firstParamTree
      * @param string
      * @return
      */
     private ParseTree getLeftChild(final InternalNode firstParamTree,
             final String string) {
         assertTrue("Invalid S-Expression as parameter to " + string,
                 firstParamTree.getLeftTree() != null);
         return firstParamTree.getLeftTree();
     }
 
     /**
      * @param firstParamTree
      * @param string
      * @return
      */
     private ParseTree getRightChild(final InternalNode firstParamTree,
             final String string) {
         assertTrue("Invalid S-Expression as parameter to " + string,
                 firstParamTree.getRightTree() != null);
         return firstParamTree.getRightTree();
     }
 
     /**
      * @param paramTree
      * @param string
      * @return
      */
     private LeafNode castAsLeafNode(final ParseTree paramTree,
             final String string) {
         assertTrue(string + " cannot be applied on a null tree",
                 paramTree != InternalNode.NILL_LEAF);
         assertTrue(string + " cannot be applied on a null tree",
                 paramTree instanceof LeafNode);
         return (LeafNode) paramTree;
     }
 
     /**
      * @param paramTree
      * @param string
      * @return
      */
     private InternalNode castAsTree(final ParseTree paramTree,
             final String string) {
         assertTrue(string + " cannot be applied on a null tree",
                 paramTree != InternalNode.NILL_LEAF);
         assertTrue(string + " cannot be applied on a null tree",
                 paramTree instanceof InternalNode);
         return (InternalNode) paramTree;
     }
 
     /**
      * @param params
      * @param string
      * @return
      */
     private ParseTree[] getAllParams(final ParseTree params, final String string) {
         if ((params instanceof LeafNode) || (params == InternalNode.NILL_LEAF)) {
             raiseInterpreterException("No valid list of parameters for "
                     + string);
             return null; // never happens, raising exception in the prev. line
         }
 
         final Vector allParams = new Vector();
         ParseTree currentParam = params;
 
         while ((currentParam != InternalNode.NILL_LEAF)
                 && (currentParam instanceof InternalNode)) {
             final InternalNode paramTree = (InternalNode) currentParam;
             assertTrue("Parameters cannot be null",
                     paramTree.getLeftTree() != null);
             allParams.add(paramTree.getLeftTree());
             currentParam = paramTree.getRightTree();
         }
 
         int i = 0;
         final Iterator iterator = allParams.iterator();
         final ParseTree[] allParamsArray = new ParseTree[allParams.size()];
 
         while (iterator.hasNext()) {
             allParamsArray[i] = (ParseTree) iterator.next();
             i++;
         }
 
         return allParamsArray;
     }
 
     /**
      * @param params
      * @param string
      * @return
      */
     private LeafNode[] extractFormalParams(final ParseTree params,
             final String string) {
         if ((params instanceof LeafNode) || (params == InternalNode.NILL_LEAF)) {
             raiseInterpreterException("No valid list of parameters for "
                     + string);
             return null; // never happens, raising exception in the prev. line
         }
 
         final Vector allParams = new Vector();
         ParseTree currentParam = params;
 
         while ((currentParam != InternalNode.NILL_LEAF)
                 && (currentParam instanceof InternalNode)) {
             final InternalNode paramTree = (InternalNode) currentParam;
 
             final LeafNode formalParam = castAsLeafNode(
                     paramTree.getLeftTree(), string);
             assertTrue("Formal parameters cannot be null", formalParam != null);
             assertTrue("Formal parameters should be leaf nodes ",
                     formalParam instanceof LeafNode);
             allParams.add(formalParam);
             currentParam = paramTree.getRightTree();
         }
 
         int i = 0;
         final Iterator iterator = allParams.iterator();
         final LeafNode[] allParamsArray = new LeafNode[allParams.size()];
 
         while (iterator.hasNext()) {
             allParamsArray[i] = (LeafNode) iterator.next();
             i++;
         }
 
         return allParamsArray;
     }
 
     /**
      * @param params
      * @param string
      */
     private void assertParamsInPair(final ParseTree[] params,
             final String string) {
         for (int i = 0; i < params.length; i++) {
             assertTrue("Invalid parameters for " + string, params[i] != null);
             assertPair(string, params[i]);
         }
     }
 
     /**
      * @param string
      * @param parseTree
      */
     private void assertPair(final String functionName, final ParseTree pair) {
         if (pair == InternalNode.NILL_LEAF) {
             raiseInterpreterException("Parameters for " + functionName
                     + " should be pairs");
         }
 
         assertTrue("Parameters for " + functionName + " should be pairs",
                 pair instanceof InternalNode);
         final InternalNode paramTree = (InternalNode) pair;
 
         assertTrue("Parameters for " + functionName + " should be pairs",
                 paramTree.getLeftTree() != InternalNode.NILL_LEAF);
 
         assertTrue("Parameters for " + functionName + " should be pairs",
                 paramTree.getRightTree() != InternalNode.NILL_LEAF);
 
         assertTrue("Parameters for " + functionName + " should be pairs",
                 paramTree.getRightTree() instanceof InternalNode);
 
         final InternalNode secondParam = (InternalNode) paramTree
                 .getRightTree();
 
         assertTrue("Parameters for " + functionName + " should be pairs",
                 secondParam.getLeftTree() != InternalNode.NILL_LEAF);
 
         assertTrue("Parameters for " + functionName + " should be pairs",
                 secondParam.getRightTree() == InternalNode.NILL_LEAF);
 
         assertTrue("Parameters for " + functionName + " should be pairs",
                 secondParam.getRightTree() instanceof InternalNode);
     }
 
     /**
      * @param msg
      * @param b
      */
     private void assertTrue(final String msg, final boolean b) {
         if (!b) {
             raiseInterpreterException(msg);
         }
     }
 
     /**
      * @param msg
      * @return
      */
     private SExpression raiseInterpreterException(final String msg) {
         throw new InterPreterException(msg);
     }
 
     private void prettyPrint(final SExpression expression) {
         if (expression instanceof LeafExpression) {
             this.out.print(expression);
         } else if (expression instanceof BinaryExpression) {
             final BinaryExpression binaryExpression = (BinaryExpression) expression;
             if (binaryExpression.isList()) {
                 prettyPrintList(binaryExpression);
             } else {
                 this.out.print("(");
                 prettyPrint(binaryExpression.getHead());
                 this.out.print(" . ");
                 prettyPrint(binaryExpression.getTail());
                 this.out.print(")");
             }
         } else {
             raiseInterpreterException("Unknown structure for S-Expression. "
                     + expression
                     + " in the S-Expression is neither a a leaf nor a binary expression");
         }
     }
 
     private void prettyPrintList(final BinaryExpression expression) {
         this.out.print("(");
         BinaryExpression list = null;
         list = expression;
         do {
             this.out.print(list.getHead());
             if ((list.getTail() == BooleanAtomExpression.NIL)
                     || (list.getTail() == null)) {
                 break;
             }
             this.out.print(" ");
             list = (BinaryExpression) list.getTail();
         } while (true);
         this.out.print(")");
 
     }
 }
