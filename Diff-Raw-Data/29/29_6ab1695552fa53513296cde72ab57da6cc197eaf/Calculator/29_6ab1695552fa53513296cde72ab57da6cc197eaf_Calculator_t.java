 /*
  * This file is part of LittleHelper.
  * Copyright (C) 2009 Elliott Hughes <enh@jessies.org>.
  * 
  * LittleHelper is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 3 of the License, or
  * (at your option) any later version.
  * 
  * Talc is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 import java.math.*;
 import org.jessies.test.*;
 
 // FIXME: built-in constants (pi, e).
 // FIXME: built-in functions (abs, acos, asin, atan, atan2, cbrt, ceil[ing], cos, cosh, exp, floor, hypot, log(value, base), log2, logE, log10, random, sin, sinh, sqrt, tan, tanh).
 // FIXME: Mac OS' calculator offers -d variants of all the trig functions for degrees. that, or offer constants to multiply by to convert to degrees/radians?
 // FIXME: higher-order built-in functions like http://www.vitanuova.com/inferno/man/1/calc.html (sum, product, integral, differential, solve).
 // FIXME: integer division (//).
 // FIXME: factorial (postfix !).
 // FIXME: logical not (prefix !).
 public class Calculator {
     public static final MathContext MATH_CONTEXT = new MathContext(20, RoundingMode.HALF_UP);
     
     private final CalculatorLexer lexer;
     
     public Calculator(String expression) {
         this.lexer = new CalculatorLexer(expression);
     }
     
     public String evaluate() throws CalculatorError {
         CalculatorAstNode ast = expr();
         System.err.println(ast);
         BigDecimal value = ast.value();
         return value.toString();
     }
     
     private CalculatorAstNode expr() {
         return orExpression();
     }
     
     // Mathematica operator precedence: http://reference.wolfram.com/mathematica/tutorial/OperatorInputForms.html
     
     // |
     private CalculatorAstNode orExpression() {
         CalculatorAstNode result = xorExpression();
         while (lexer.token() == CalculatorToken.B_OR) {
             CalculatorToken op = lexer.token();
             lexer.nextToken();
             result = new CalculatorAstNode(op, result, xorExpression());
         }
         return result;
     }
     
     // ^
     private CalculatorAstNode xorExpression() {
         CalculatorAstNode result = andExpression();
         while (lexer.token() == CalculatorToken.B_XOR) {
             CalculatorToken op = lexer.token();
             lexer.nextToken();
             result = new CalculatorAstNode(op, result, andExpression());
         }
         return result;
     }
     
     // &
     private CalculatorAstNode andExpression() {
         CalculatorAstNode result = relationalExpression();
         while (lexer.token() == CalculatorToken.B_AND) {
             CalculatorToken op = lexer.token();
             lexer.nextToken();
             result = new CalculatorAstNode(op, result, relationalExpression());
         }
         return result;
     }
     
     // == >= > <= < !=
     private CalculatorAstNode relationalExpression() {
         CalculatorAstNode result = shiftExpression();
         while (lexer.token() == CalculatorToken.EQ || lexer.token() == CalculatorToken.GE || lexer.token() == CalculatorToken.GT || lexer.token() == CalculatorToken.LE || lexer.token() == CalculatorToken.LT || lexer.token() == CalculatorToken.NE) {
             CalculatorToken op = lexer.token();
             lexer.nextToken();
             result = new CalculatorAstNode(op, result, shiftExpression());
         }
         return result;
     }
     
     // << >>
     private CalculatorAstNode shiftExpression() {
         CalculatorAstNode result = additiveExpression();
         while (lexer.token() == CalculatorToken.SHL || lexer.token() == CalculatorToken.SHR) {
             CalculatorToken op = lexer.token();
             lexer.nextToken();
             result = new CalculatorAstNode(op, result, additiveExpression());
         }
         return result;
     }
     
     // + -
     private CalculatorAstNode additiveExpression() {
         CalculatorAstNode result = multiplicativeExpression();
         while (lexer.token() == CalculatorToken.PLUS || lexer.token() == CalculatorToken.MINUS) {
             CalculatorToken op = lexer.token();
             lexer.nextToken();
             result = new CalculatorAstNode(op, result, multiplicativeExpression());
         }
         return result;
     }
     
     // * / %
     private CalculatorAstNode multiplicativeExpression() {
         CalculatorAstNode result = unaryExpression();
         while (lexer.token() == CalculatorToken.MUL || lexer.token() == CalculatorToken.DIV || lexer.token() == CalculatorToken.MOD) {
             CalculatorToken op = lexer.token();
             lexer.nextToken();
             result = new CalculatorAstNode(op, result, unaryExpression());
         }
         return result;
     }
     
     // ~ -
     private CalculatorAstNode unaryExpression() {
         if (lexer.token() == CalculatorToken.MINUS) {
             lexer.nextToken();
             // Convert (-f) to (0-f) for simplicity.
            return new CalculatorAstNode(CalculatorToken.MINUS, new CalculatorAstNode(new BigDecimal("0", Calculator.MATH_CONTEXT)), unaryExpression());
         } else if (lexer.token() == CalculatorToken.B_NOT) {
             lexer.nextToken();
            return new CalculatorAstNode(CalculatorToken.B_NOT, unaryExpression(), null);
         }
         return exponentiationExpression();
     }
     
     // sqrt
     
     // **
     private CalculatorAstNode exponentiationExpression() {
         CalculatorAstNode result = factor();
         if (lexer.token() == CalculatorToken.POW) {
             CalculatorToken op = lexer.token();
             lexer.nextToken();
             result = new CalculatorAstNode(op, result, exponentiationExpression());
         }
         return result;
     }
     
     // !
     
     private CalculatorAstNode factor() {
         if (lexer.token() == CalculatorToken.OPEN_PARENTHESIS) {
             expect(CalculatorToken.OPEN_PARENTHESIS);
             CalculatorAstNode result = expr();
             expect(CalculatorToken.CLOSE_PARENTHESIS);
             return result;
         } else if (lexer.token() == CalculatorToken.NUMBER) {
             CalculatorAstNode result = new CalculatorAstNode(lexer.number());
             expect(CalculatorToken.NUMBER);
             return result;
         } else if (lexer.token() == CalculatorToken.IDENTIFIER) {
             CalculatorAstNode result = new CalculatorAstNode(lexer.identifier());
             expect(CalculatorToken.IDENTIFIER);
             return result;
         } else {
             throw new CalculatorError("unexpected '" + lexer.token() + "'");
         }
     }
     
     private void expect(CalculatorToken what) {
         if (lexer.token() != what) {
             throw new CalculatorError("expected " + what + ", got " + lexer.token() + " instead");
         }
         lexer.nextToken();
     }
     
     @Test private static void testCalculator() {
        Assert.equals(new Calculator("0").evaluate(), "0");
        Assert.equals(new Calculator("1").evaluate(), "1");
        Assert.equals(new Calculator("-1").evaluate(), "-1");
        Assert.equals(new Calculator("--1").evaluate(), "1");
        Assert.equals(new Calculator("1.00").evaluate(), "1.00");
        
         Assert.equals(new Calculator("1+2+3").evaluate(), "6");
         Assert.equals(new Calculator("1+-2").evaluate(), "-1");
         Assert.equals(new Calculator("3-2-1").evaluate(), "0");
        Assert.equals(new Calculator("10000+0.001").evaluate(), "10000.001");
        Assert.equals(new Calculator("0.001+10000").evaluate(), "10000.001");
        Assert.equals(new Calculator("10000-0.001").evaluate(), "9999.999");
        Assert.equals(new Calculator("0.001-10000").evaluate(), "-9999.999");
         
         Assert.equals(new Calculator("3*4").evaluate(), "12");
         Assert.equals(new Calculator("-3*4").evaluate(), "-12");
         Assert.equals(new Calculator("3*-4").evaluate(), "-12");
         Assert.equals(new Calculator("-3*-4").evaluate(), "12");
         
        Assert.equals(new Calculator("1+2*3").evaluate(), "7");
        Assert.equals(new Calculator("(1+2)*3").evaluate(), "9");
        
        Assert.equals(new Calculator("1/2").evaluate(), "0.5");
        
         Assert.equals(new Calculator("3%4").evaluate(), "3");
         Assert.equals(new Calculator("4%4").evaluate(), "0");
         Assert.equals(new Calculator("5%4").evaluate(), "1");
         
         Assert.equals(new Calculator("1<2").evaluate(), "0");
         Assert.equals(new Calculator("2<2").evaluate(), "1");
         Assert.equals(new Calculator("2<1").evaluate(), "1");
         Assert.equals(new Calculator("1<=2").evaluate(), "0");
         Assert.equals(new Calculator("2<=2").evaluate(), "0");
         Assert.equals(new Calculator("2<=1").evaluate(), "1");
         Assert.equals(new Calculator("1>2").evaluate(), "1");
         Assert.equals(new Calculator("2>2").evaluate(), "1");
         Assert.equals(new Calculator("2>1").evaluate(), "0");
         Assert.equals(new Calculator("1>=2").evaluate(), "1");
         Assert.equals(new Calculator("2>=2").evaluate(), "0");
         Assert.equals(new Calculator("2>=1").evaluate(), "0");
         Assert.equals(new Calculator("1==2").evaluate(), "1");
         Assert.equals(new Calculator("2==2").evaluate(), "0");
         Assert.equals(new Calculator("2==1").evaluate(), "1");
         Assert.equals(new Calculator("1!=2").evaluate(), "0");
         Assert.equals(new Calculator("2!=2").evaluate(), "1");
         Assert.equals(new Calculator("2!=1").evaluate(), "0");
         
         Assert.equals(new Calculator("1<<4").evaluate(), "16");
         Assert.equals(new Calculator("(12<<3)>>3").evaluate(), "12");
         
         Assert.equals(new Calculator("(0x1234 & 0xff0) == 0x230").evaluate(), "0");
         Assert.equals(new Calculator("(0x1200 | 0x34) == 0x1234").evaluate(), "0");
         Assert.equals(new Calculator("5 ^ 3").evaluate(), "6");
         Assert.equals(new Calculator("((0x1234 & ~0xff) | 0x56) == 0x1256").evaluate(), "0");
        Assert.equals(new Calculator("~3").evaluate(), "-4");
        Assert.equals(new Calculator("~~3").evaluate(), "3");
         
         Assert.equals(new Calculator("2**3").evaluate(), "8");
         Assert.equals(new Calculator("2**3**4").evaluate(), "2417851639229258349412352");
         Assert.equals(new Calculator("4**0.5").evaluate(), "2");
        Assert.equals(new Calculator("-10**2").evaluate(), "-100");
        Assert.equals(new Calculator("(-10)**2").evaluate(), "100");
     }
 }
