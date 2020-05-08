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
 
import static org.junit.Assert.*;

 import javax.naming.LimitExceededException;
 
 import junit.framework.Assert;
 
 import org.junit.Test;
 
 /**
  * @author Meisam Fathi Salmi <fathi@cse.ohio-state.edu>
  * 
  */
 public class LexerTest {
 
     /**
      * Test method for {@link edu.osu.cse.meisam.interpreter.Lexer#nextToken()}.
      */
     @Test
     public void testNexTokenWithEmptyInput() {
         InputProvider inputProvider = new StringInputProvider("");
         Lexer lexer = new Lexer(inputProvider );
         
         Assert.assertEquals("$", lexer.nextToken());
     }
 
     @Test
     public void testNexTokenWithWhiteSpaceInput() {
         InputProvider inputProvider = new StringInputProvider(" ");
         Lexer lexer = new Lexer(inputProvider );
         
         Assert.assertEquals("$", lexer.nextToken());
     }
 
     @Test
     public void testNexTokenWithtLongWhiteSpaceInput() {
         InputProvider inputProvider = new StringInputProvider("   \t\n  \r \n\r");
         Lexer lexer = new Lexer(inputProvider );
         
         Assert.assertEquals("$", lexer.nextToken());
     }
 
     @Test
     public void testNextTokenWithSingleDigit() {
         InputProvider inputProvider = new StringInputProvider("1");
         Lexer lexer = new Lexer(inputProvider );
         
         Assert.assertEquals("1", lexer.nextToken());
         Assert.assertEquals("$", lexer.nextToken());
     }
 
     @Test
     public void testNextTokenWithSingleNumber() {
         InputProvider inputProvider = new StringInputProvider("1234");
         Lexer lexer = new Lexer(inputProvider );
         
         Assert.assertEquals("1234", lexer.nextToken());
         Assert.assertEquals("$", lexer.nextToken());
     }
 
     @Test
     public void testNextTokenWithSingleNumberEndWhiteSpace() {
         InputProvider inputProvider = new StringInputProvider("1234  ");
         Lexer lexer = new Lexer(inputProvider );
         
         Assert.assertEquals("1234", lexer.nextToken());
         Assert.assertEquals("$", lexer.nextToken());
     }
 
     @Test
     public void testNextTokenWithSingleNumberStartWhiteSpace() {
         InputProvider inputProvider = new StringInputProvider("  1234");
         Lexer lexer = new Lexer(inputProvider );
         
         Assert.assertEquals("1234", lexer.nextToken());
         Assert.assertEquals("$", lexer.nextToken());
     }
 
     @Test
     public void testNextTokenWithDoubleId() {
         InputProvider inputProvider = new StringInputProvider("1234 1");
         Lexer lexer = new Lexer(inputProvider );
         
         Assert.assertEquals("1234", lexer.nextToken());
         Assert.assertEquals("1", lexer.nextToken());
         Assert.assertEquals("$", lexer.nextToken());
     }
 
 
     @Test
     public void testNextTokenWithSingleNegativeDigit() {
         InputProvider inputProvider = new StringInputProvider("-1");
         Lexer lexer = new Lexer(inputProvider );
         
         Assert.assertEquals("-1", lexer.nextToken());
         Assert.assertEquals("$", lexer.nextToken());
     }
 
     @Test
     public void testNextTokenWithSinglePositiveDigit() {
         InputProvider inputProvider = new StringInputProvider("+1");
         Lexer lexer = new Lexer(inputProvider );
         
         Assert.assertEquals("1", lexer.nextToken());
         Assert.assertEquals("$", lexer.nextToken());
     }
 
     @Test
     public void testNextTokenWithPositiveNumber() {
         InputProvider inputProvider = new StringInputProvider("+1455");
         Lexer lexer = new Lexer(inputProvider );
         
         Assert.assertEquals("1455", lexer.nextToken());
         Assert.assertEquals("$", lexer.nextToken());
     }
 
     @Test
     public void testNextTokenWithNegativeNumber() {
         InputProvider inputProvider = new StringInputProvider("-1455");
         Lexer lexer = new Lexer(inputProvider );
         
         Assert.assertEquals("-1455", lexer.nextToken());
         Assert.assertEquals("$", lexer.nextToken());
     }
 
     @Test
     public void testNextTokenWithSpacePositiveNumber() {
         InputProvider inputProvider = new StringInputProvider(" +1455");
         Lexer lexer = new Lexer(inputProvider );
         
         Assert.assertEquals("1455", lexer.nextToken());
         Assert.assertEquals("$", lexer.nextToken());
     }
 
     @Test
     public void testNextTokenWithSpaceNegativeNumber() {
         InputProvider inputProvider = new StringInputProvider(" -1455");
         Lexer lexer = new Lexer(inputProvider );
         
         Assert.assertEquals("-1455", lexer.nextToken());
         Assert.assertEquals("$", lexer.nextToken());
     }
 
     @Test
     public void testNextTokenWithMultiSpacePositiveNumber() {
         InputProvider inputProvider = new StringInputProvider(" \t\n\r +1455");
         Lexer lexer = new Lexer(inputProvider );
         
         Assert.assertEquals("1455", lexer.nextToken());
         Assert.assertEquals("$", lexer.nextToken());
     }
 
     @Test
     public void testNextTokenWithMultiSpaceNegativeNumber() {
         InputProvider inputProvider = new StringInputProvider(" \t\n\r -1455");
         Lexer lexer = new Lexer(inputProvider );
         
         Assert.assertEquals("-1455", lexer.nextToken());
         Assert.assertEquals("$", lexer.nextToken());
     }
 
     @Test
     public void testNextTokenWithTwoSignedNumber() {
         InputProvider inputProvider = new StringInputProvider(" \t\n\r -1455   \t\n\r  +999");
         Lexer lexer = new Lexer(inputProvider );
         
         Assert.assertEquals("-1455", lexer.nextToken());
         Assert.assertEquals("999", lexer.nextToken());
         Assert.assertEquals("$", lexer.nextToken());
     }
 
     @Test
     public void testNextTokenWithTwoNumber() {
         InputProvider inputProvider = new StringInputProvider(" \t\n\r -1455   \t\n\r  999");
         Lexer lexer = new Lexer(inputProvider );
         
         Assert.assertEquals("-1455", lexer.nextToken());
         Assert.assertEquals("999", lexer.nextToken());
         Assert.assertEquals("$", lexer.nextToken());
     }
 
     @Test
     public void testNextTokenWithSingleLetter() {
         InputProvider inputProvider = new StringInputProvider("A");
         Lexer lexer = new Lexer(inputProvider );
         
         Assert.assertEquals("A", lexer.nextToken());
         Assert.assertEquals("$", lexer.nextToken());
     }
 
     @Test
     public void testNextTokenWithSingleId() {
         InputProvider inputProvider = new StringInputProvider("ABC");
         Lexer lexer = new Lexer(inputProvider );
         
         Assert.assertEquals("ABC", lexer.nextToken());
         Assert.assertEquals("$", lexer.nextToken());
     }
 
     @Test
     public void testNextTokenWithSingleIdEndWhiteSpace() {
         InputProvider inputProvider = new StringInputProvider("A  ");
         Lexer lexer = new Lexer(inputProvider );
         
         Assert.assertEquals("A", lexer.nextToken());
         Assert.assertEquals("$", lexer.nextToken());
     }
 
     @Test
     public void testNextTokenWithSingleIdStartWhiteSpace() {
         InputProvider inputProvider = new StringInputProvider("  ABC");
         Lexer lexer = new Lexer(inputProvider );
         
         Assert.assertEquals("ABC", lexer.nextToken());
         Assert.assertEquals("$", lexer.nextToken());
     }
 
     @Test
     public void testNextTokenWithDoubleNumbers() {
         InputProvider inputProvider = new StringInputProvider("ABC A");
         Lexer lexer = new Lexer(inputProvider );
         
         Assert.assertEquals("ABC", lexer.nextToken());
         Assert.assertEquals("A", lexer.nextToken());
         Assert.assertEquals("$", lexer.nextToken());
     }
     
     @Test
     public void testNextTokenInvalidNumberId() {
         InputProvider inputProvider = new StringInputProvider("42A");
         Lexer lexer = new Lexer(inputProvider );
         
         try {
             lexer.nextToken();
         } catch (LexerExeption e) {
            assertTrue(e.getMessage().contains("42A"));
         }
         Assert.assertEquals("$", lexer.nextToken());
     }
 
     @Test
     public void testNextTokenInvalidIdNumber() {
         InputProvider inputProvider = new StringInputProvider("A41");
         Lexer lexer = new Lexer(inputProvider );
         
         try {
             lexer.nextToken();
         } catch (LexerExeption e) {
            assertTrue(e.getMessage().contains("A4"));
         }
         Assert.assertEquals("1", lexer.nextToken());
         Assert.assertEquals("$", lexer.nextToken());
     }
 
 
     @Test
     public void testNextTokenInvalidIdNumberSpaced() {
         InputProvider inputProvider = new StringInputProvider("     A41     ");
         Lexer lexer = new Lexer(inputProvider );
         
         try {
             lexer.nextToken();
         } catch (LexerExeption e) {
            assertTrue(e.getMessage().contains("A4"));
         }
         Assert.assertEquals("1", lexer.nextToken());
         Assert.assertEquals("$", lexer.nextToken());
     }
     
     @Test
     public void testNextTokenIdNumber() {
         InputProvider inputProvider = new StringInputProvider("  A   1  ");
         Lexer lexer = new Lexer(inputProvider );
         Assert.assertEquals("A", lexer.nextToken());
         Assert.assertEquals("1", lexer.nextToken());
         Assert.assertEquals("$", lexer.nextToken());
     }
   
     @Test
     public void testNextTokenNumberId() {
         InputProvider inputProvider = new StringInputProvider("  1   A  ");
         Lexer lexer = new Lexer(inputProvider );
         Assert.assertEquals("1", lexer.nextToken());
         Assert.assertEquals("A", lexer.nextToken());
         Assert.assertEquals("$", lexer.nextToken());
     }
     
     @Test
     public void testNextTokenIdNumberNTimes() {
         InputProvider inputProvider = new StringInputProvider("  A   1 AAA 123 CCC BBB 111 000  ");
         Lexer lexer = new Lexer(inputProvider );
         Assert.assertEquals("A", lexer.nextToken());
         Assert.assertEquals("1", lexer.nextToken());
         Assert.assertEquals("AAA", lexer.nextToken());
         Assert.assertEquals("123", lexer.nextToken());
         Assert.assertEquals("CCC", lexer.nextToken());
         Assert.assertEquals("BBB", lexer.nextToken());
         Assert.assertEquals("111", lexer.nextToken());
         Assert.assertEquals("000", lexer.nextToken());
         Assert.assertEquals("$", lexer.nextToken());
     }
     
     @Test
     public void testNextTokenPlusOperation() {
         InputProvider inputProvider = new StringInputProvider("+");
         Lexer lexer = new Lexer(inputProvider );
         try {
             lexer.nextToken();
            fail("Lexer should have reported error");
         } catch (LexerExeption e) {
         }
     }
     
     @Test
     public void testNextTokenPlusOperationSpace() {
         InputProvider inputProvider = new StringInputProvider("+ ");
         Lexer lexer = new Lexer(inputProvider );
         try {
             lexer.nextToken();
            fail("Lexer should have reported error");
         } catch (LexerExeption e) {
         }
     }
   
     @Test
     public void testNextTokenPlusOperationSpaces() {
         InputProvider inputProvider = new StringInputProvider("+ \n\r\t");
         Lexer lexer = new Lexer(inputProvider );
         try {
             lexer.nextToken();
            fail("Lexer should have reported error");
         } catch (LexerExeption e) {
         }
     }
     
     @Test
     public void testNextTokenSpacePlusOperation() {
         InputProvider inputProvider = new StringInputProvider(" +");
         Lexer lexer = new Lexer(inputProvider );
         try {
             lexer.nextToken();
            fail("Lexer should have reported error");
         } catch (LexerExeption e) {
         }
     }
     
     @Test
     public void testNextTokenOpenParentheses() {
         InputProvider inputProvider = new StringInputProvider("(");
         Lexer lexer = new Lexer(inputProvider );
         Assert.assertEquals("(", lexer.nextToken());
         Assert.assertEquals("$", lexer.nextToken());
     }
     
     @Test
     public void testNextTokenCloseParentheses() {
         InputProvider inputProvider = new StringInputProvider(")");
         Lexer lexer = new Lexer(inputProvider );
         Assert.assertEquals(")", lexer.nextToken());
         Assert.assertEquals("$", lexer.nextToken());
     }  
     
     @Test
     public void testNextTokenOpenParenthesesSpace() {
         InputProvider inputProvider = new StringInputProvider("( ");
         Lexer lexer = new Lexer(inputProvider );
         Assert.assertEquals("(", lexer.nextToken());
         Assert.assertEquals("$", lexer.nextToken());
     }
     
     @Test
     public void testNextTokenCloseParenthesesSpace() {
         InputProvider inputProvider = new StringInputProvider(") ");
         Lexer lexer = new Lexer(inputProvider );
         Assert.assertEquals(")", lexer.nextToken());
         Assert.assertEquals("$", lexer.nextToken());
     }  
     
     @Test
     public void testNextTokenOpenParenthesesSpaces() {
         InputProvider inputProvider = new StringInputProvider("( \t\n\r");
         Lexer lexer = new Lexer(inputProvider );
         Assert.assertEquals("(", lexer.nextToken());
         Assert.assertEquals("$", lexer.nextToken());
     }
     
     @Test
     public void testNextTokenCloseParenthesesSpaces() {
         InputProvider inputProvider = new StringInputProvider(") \t\n\r");
         Lexer lexer = new Lexer(inputProvider );
         Assert.assertEquals(")", lexer.nextToken());
         Assert.assertEquals("$", lexer.nextToken());
     }  
     
     
     @Test
     public void testNextTokenSpacesOpenParenthesesSpaces() {
         InputProvider inputProvider = new StringInputProvider(" \t\n\r ( \t\n\r");
         Lexer lexer = new Lexer(inputProvider );
         Assert.assertEquals("(", lexer.nextToken());
         Assert.assertEquals("$", lexer.nextToken());
     }
     
     @Test
     public void testNextTokenSpacesCloseParenthesesSpaces() {
         InputProvider inputProvider = new StringInputProvider(" \t\n\r ) \t\n\r");
         Lexer lexer = new Lexer(inputProvider );
         Assert.assertEquals(")", lexer.nextToken());
         Assert.assertEquals("$", lexer.nextToken());
     }  
     
     @Test
     public void testNextTokenOpenParenthesesTwo() {
         InputProvider inputProvider = new StringInputProvider("((");
         Lexer lexer = new Lexer(inputProvider );
         Assert.assertEquals("(", lexer.nextToken());
         Assert.assertEquals("(", lexer.nextToken());
         Assert.assertEquals("$", lexer.nextToken());
     }
     
     @Test
     public void testNextTokenCloseParenthesesTwo() {
         InputProvider inputProvider = new StringInputProvider("))");
         Lexer lexer = new Lexer(inputProvider );
         Assert.assertEquals(")", lexer.nextToken());
         Assert.assertEquals(")", lexer.nextToken());
         Assert.assertEquals("$", lexer.nextToken());
     }  
 }
