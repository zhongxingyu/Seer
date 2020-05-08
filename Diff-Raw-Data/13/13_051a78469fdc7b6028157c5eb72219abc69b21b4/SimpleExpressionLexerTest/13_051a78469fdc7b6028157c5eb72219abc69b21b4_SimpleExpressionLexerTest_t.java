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
 
 import java.util.Arrays;
 import java.util.Collection;
 
 import org.jbasics.math.expression.simple.SimpleExpressionLexer;
 import org.jbasics.testing.Java14LoggingTestCase;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.junit.runners.Parameterized;
 import org.junit.runners.Parameterized.Parameters;
 
 @RunWith(Parameterized.class)
 public class SimpleExpressionLexerTest extends Java14LoggingTestCase {
 
 	@Parameters
 	public static Collection<String[]> data() {
 		return Arrays.asList(
 				new String[] { "($4020_15 - $7012_c) + ($5020_v - $5120_c) * 100" },
 				new String[] { "42 * 7 + HalloWelt" },
 				new String[] { "$4010_v" },
 				new String[] { "$2060_c+$100_a-$900_b" }
 		);
 	}
 
 	private final String testExpression;
 
 	public SimpleExpressionLexerTest(String testExpression) {
 		this.testExpression = testExpression;
 	}
 
 	@Test
 	public void test() {
 		this.logger.info("------------");
		this.logger.info("Expression: " + this.testExpression);
		SimpleExpressionLexer temp = new SimpleExpressionLexer(this.testExpression);
 		while (temp.next().curentType() != SimpleExpressionLexer.TokenType.EOF) {
 			this.logger.info("Found: " + temp.curentType() + " {" + temp.currentContent() + "}");
 		}
 		this.logger.info("Found: " + temp.curentType() + " {" + temp.currentContent() + "}");
 	}
 
 }
