 package org.jessies.calc;
 
 /*
  * This file is part of LittleHelper.
  * Copyright (C) 2009 Elliott Hughes <enh@jessies.org>.
  * 
  * LittleHelper is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 3 of the License, or
  * (at your option) any later version.
  * 
  * This is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 import java.util.*;
 
 public class CalculatorParser {
     private final Calculator calculator;
     private final CalculatorLexer lexer;
     
     public CalculatorParser(Calculator calculator, String expression) {
         this.calculator = calculator;
         this.lexer = new CalculatorLexer(expression);
     }
     
     public Node parse() {
         final Node result = parseExpr();
         expect(CalculatorToken.END_OF_INPUT);
         return result;
     }
     
     private Node parseExpr() {
         return parseAssignmentExpression();
     }
     
     // Mathematica operator precedence: http://reference.wolfram.com/mathematica/tutorial/OperatorInputForms.html
     
     // = (assignment)
     private Node parseAssignmentExpression() {
         Node result = parseOrExpression();
         if (lexer.token() == CalculatorToken.ASSIGN) {
             lexer.nextToken();
             result = new CalculatorFunctionApplicationNode(getFunction("define"), Arrays.asList(result, parseOrExpression()));
         }
         return result;
         
     }
     
     // ||
     private Node parseOrExpression() {
         Node result = parseAndExpression();
         while (lexer.token() == CalculatorToken.L_OR) {
             lexer.nextToken();
             // FIXME: make Or varargs.
             result = new CalculatorFunctionApplicationNode(getFunction(CalculatorToken.L_OR), Arrays.asList(result, parseAndExpression()));
         }
         return result;
     }
     
     // &&
     private Node parseAndExpression() {
         Node result = parseBitOrExpression();
         while (lexer.token() == CalculatorToken.L_AND) {
             lexer.nextToken();
             // FIXME: make And varargs.
             result = new CalculatorFunctionApplicationNode(getFunction(CalculatorToken.L_AND), Arrays.asList(result, parseBitOrExpression()));
         }
         return result;
     }
     
     // |
     private Node parseBitOrExpression() {
         Node result = parseBitAndExpression();
         while (lexer.token() == CalculatorToken.B_OR) {
             lexer.nextToken();
             // FIXME: make BitOr varargs.
             result = new CalculatorFunctionApplicationNode(getFunction(CalculatorToken.B_OR), Arrays.asList(result, parseBitAndExpression()));
         }
         return result;
     }
     
     // &
     private Node parseBitAndExpression() {
         Node result = parseNotExpression();
         while (lexer.token() == CalculatorToken.B_AND) {
             lexer.nextToken();
             // FIXME: make BitAnd varargs.
             result = new CalculatorFunctionApplicationNode(getFunction(CalculatorToken.B_AND), Arrays.asList(result, parseNotExpression()));
         }
         return result;
     }
     
     // !
     private Node parseNotExpression() {
         if (lexer.token() == CalculatorToken.PLING) {
             lexer.nextToken();
             return new CalculatorFunctionApplicationNode(getFunction("Not"), Collections.singletonList(parseNotExpression()));
         } else {
             return parseRelationalExpression();
         }
     }
     
     // == >= > <= < !=
     private Node parseRelationalExpression() {
         Node result = parseShiftExpression();
         while (lexer.token() == CalculatorToken.EQ || lexer.token() == CalculatorToken.GE || lexer.token() == CalculatorToken.GT || lexer.token() == CalculatorToken.LE || lexer.token() == CalculatorToken.LT || lexer.token() == CalculatorToken.NE) {
             final CalculatorFunction function = getFunction(lexer.token());
             lexer.nextToken();
             result = new CalculatorFunctionApplicationNode(function, Arrays.asList(result, parseShiftExpression()));
         }
         return result;
     }
     
     // << >>
     private Node parseShiftExpression() {
         Node result = parseAdditiveExpression();
         while (lexer.token() == CalculatorToken.SHL || lexer.token() == CalculatorToken.SHR) {
             final CalculatorFunction function = getFunction(lexer.token());
             lexer.nextToken();
             result = new CalculatorFunctionApplicationNode(function, Arrays.asList(result, parseAdditiveExpression()));
         }
         return result;
     }
     
     // + -
     private Node parseAdditiveExpression() {
         Node result = parseMultiplicativeExpression();
         while (lexer.token() == CalculatorToken.PLUS || lexer.token() == CalculatorToken.MINUS) {
             final CalculatorFunction function = getFunction(lexer.token());
             lexer.nextToken();
             result = new CalculatorFunctionApplicationNode(function, Arrays.asList(result, parseMultiplicativeExpression()));
         }
         return result;
     }
     
     // * / %
     private Node parseMultiplicativeExpression() {
         Node result = parseUnaryExpression();
         while (lexer.token() == CalculatorToken.MUL || lexer.token() == CalculatorToken.DIV || lexer.token() == CalculatorToken.MOD) {
             final CalculatorFunction function = getFunction(lexer.token());
             lexer.nextToken();
             result = new CalculatorFunctionApplicationNode(function, Arrays.asList(result, parseUnaryExpression()));
         }
         return result;
     }
     
     // ~ -
     private Node parseUnaryExpression() {
         if (lexer.token() == CalculatorToken.MINUS) {
             lexer.nextToken();
             // Convert (-f) to (-1*f) for simplicity.
             return new CalculatorFunctionApplicationNode(getFunction(CalculatorToken.MUL), Arrays.asList(IntegerNode.valueOf(-1), parseUnaryExpression()));
         } else if (lexer.token() == CalculatorToken.B_NOT) {
             lexer.nextToken();
             return new CalculatorFunctionApplicationNode(getFunction(CalculatorToken.B_NOT), Collections.singletonList(parseUnaryExpression()));
         }
         return parseSqrtExpression();
     }
     
     // sqrt
     private Node parseSqrtExpression() {
         if (lexer.token() == CalculatorToken.SQRT) {
             lexer.nextToken();
             return new CalculatorFunctionApplicationNode(getFunction("sqrt"), Collections.singletonList(parseSqrtExpression()));
         } else {
             return parseExponentiationExpression();
         }
     }
     
     // ^
     private Node parseExponentiationExpression() {
         Node result = parseFactorialExpression();
         if (lexer.token() == CalculatorToken.POW) {
             lexer.nextToken();
             result = new CalculatorFunctionApplicationNode(getFunction(CalculatorToken.POW), Arrays.asList(result, parseExponentiationExpression()));
         }
         return result;
     }
     
     // postfix-!
     private Node parseFactorialExpression() {
         Node result = parseFactor();
         if (lexer.token() == CalculatorToken.PLING) {
             expect(CalculatorToken.PLING);
             result = new CalculatorFunctionApplicationNode(getFunction("Factorial"), Collections.singletonList(result));
         }
         return result;
     }
     
     private Node parseFactor() {
         if (lexer.token() == CalculatorToken.OPEN_PARENTHESIS) {
             expect(CalculatorToken.OPEN_PARENTHESIS);
             Node result = parseExpr();
             expect(CalculatorToken.CLOSE_PARENTHESIS);
             return result;
         } else if (lexer.token() == CalculatorToken.NUMBER) {
             Node result = lexer.number();
             expect(CalculatorToken.NUMBER);
             return result;
         } else if (lexer.token() == CalculatorToken.IDENTIFIER) {
             final String identifier = lexer.identifier();
             expect(CalculatorToken.IDENTIFIER);
            if (lexer.token() == CalculatorToken.OPEN_PARENTHESIS) {
                 final CalculatorFunction fn = getFunction(identifier);
                if (fn == null) {
                    throw new CalculatorError("undefined  function '" + identifier + "'");
                }
                return new CalculatorFunctionApplicationNode(fn, parseArgs());
            } else {
                Node result = calculator.getConstant(identifier);
                if (result == null) {
                     result = new CalculatorVariableNode(identifier);
                 }
                return result;
             }
         } else {
             throw new CalculatorError("unexpected " + quoteTokenForErrorMessage(lexer.token()));
         }
     }
     
     // '(' expr [ ',' expr ] ')'
     private List<Node> parseArgs() {
         final List<Node> result = new LinkedList<Node>();
         expect(CalculatorToken.OPEN_PARENTHESIS);
         while (lexer.token() != CalculatorToken.CLOSE_PARENTHESIS) {
             result.add(parseExpr());
             if (lexer.token() == CalculatorToken.COMMA) {
                 expect(CalculatorToken.COMMA);
                 continue;
             }
         }
         expect(CalculatorToken.CLOSE_PARENTHESIS);
         return result;
     }
     
     private void expect(CalculatorToken what) {
         if (lexer.token() != what) {
             throw new CalculatorError("expected " + quoteTokenForErrorMessage(what) + ", got " + quoteTokenForErrorMessage(lexer.token()) + " instead");
         }
         lexer.nextToken();
     }
     
     private final CalculatorFunction getFunction(String name) {
         return calculator.getFunction(name);
     }
     
     private final CalculatorFunction getFunction(CalculatorToken token) {
         return calculator.getFunction(token);
     }
     
     private static String quoteTokenForErrorMessage(CalculatorToken token) {
         String result = token.toString();
         if (result.length() > 2) {
             // We probably already have something usable like "end of input".
             return result;
         }
         // Quote operators.
         return "'" + result + "'";
     }
 }
