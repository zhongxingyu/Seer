 /*
  * Copyright (c) 2009 Stephan Schloepke and innoQ Deutschland GmbH
  *
  * Stephan Schloepke: http://www.schloepke.de/
  * innoQ Deutschland GmbH: http://www.innoq.com/
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  * THE SOFTWARE.
  */
 package org.jbasics.math.expression.simple;
 
 import static org.junit.Assert.assertTrue;
 
 import java.math.BigDecimal;
 import java.math.MathContext;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.logging.Level;
 
 import org.jbasics.testing.Java14LoggingTestCase;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.junit.runners.Parameterized;
 import org.junit.runners.Parameterized.Parameters;
 
 @RunWith(Parameterized.class)
 public class SimpleExpressionParserTest extends Java14LoggingTestCase implements SimpleSymbolResolver {
     private final static MathContext MC = MathContext.DECIMAL128;
     private final static BigDecimal A = new BigDecimal(123);
     private final static BigDecimal B = new BigDecimal(458);
 
     @Parameters
     public static Collection<Object[]> testData() {
         return Arrays.asList(
         		new Object[] { "100 + 345 + 7", new BigDecimal(100 + 345 + 7) }, // Test
         		new Object[] { "10 * 20", new BigDecimal(10 * 20) }, // Test
         		new Object[] { "10 + 5 * 2 - 7", new BigDecimal(10 + 5 * 2 - 7) }, // Test
                 new Object[] { "a * a + b * b", A.multiply(A, MC).add(B.multiply(B, MC)) }, // Test
         		new Object[] { "10 * 20 * 30 * 5 + 10", new BigDecimal(10 * 20 * 30 * 5 + 10) }, // Test
         		new Object[] { "13.56 + 34.5 * 756.3 * .4", new BigDecimal(13.56 + 34.5 * 756.3 * .4) } // Test
         );
     }
 
     private final String expressionString;
     private final BigDecimal expected;
 
     public SimpleExpressionParserTest(String expressionString, BigDecimal expected) {
         this.expressionString = expressionString;
         this.expected = expected;
     }
 
     @Test
     public void testParsing() {
         SimpleExpression temp = SimpleExpression.parse(this.expressionString);
         BigDecimal result = temp.eval(this, MC);
        logger.log(Level.INFO, "Test Soll: {0} = {1}", new Object[] {this.expressionString, this.expected});
        logger.log(Level.INFO, "  -> Ist:  {0} = {1}", new Object[] {temp, result});
         assertTrue(this.expected.compareTo(result) == 0);
     }
 
     public BigDecimal resolve(String symbol) {
         if ("a".equals(symbol)) {
             return A;
         } else if ("b".equals(symbol)) {
             return B;
         } else {
             return null;
         }
     }
 }
