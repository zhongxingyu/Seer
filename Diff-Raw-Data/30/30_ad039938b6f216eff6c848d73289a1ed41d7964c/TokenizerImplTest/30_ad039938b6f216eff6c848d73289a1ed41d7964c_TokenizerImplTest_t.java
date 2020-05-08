 /*
  * The MIT License
  *
  * Copyright 2013 Marcin Zarebski <zarebski.m[AT]gmail.com>.
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
 package calculator.parse;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import org.junit.Assert;
 import org.junit.Test;
 
 public class TokenizerImplTest {
     @Test
     public void testHasNextToken() {
     }
 
     @Test
    public void testGetNextToken_number() throws Exception {
        final String input = "1.2";
        checkTokenizer(input, "1.2");
    }

    @Test
    public void testGetNextToken_simpleExpression() throws Exception {
        final String input = "1+2 *3";
        checkTokenizer(input, "1", "+", "2", "*", "3");
    }

    @Test
    public void testGetNextToken_bigExpression() throws Exception {
        final String input = "1+2*3-A(B(1.2^3.4/5.6\t% C123( 0,9, 1 ,5)))";
        checkTokenizer(input, "1", "+", "2", "*", "3", "-", "A", "(", "B", "(", "1.2", "^", "3.4", "/", "5.6", "%",
                "C123", "(", "0", ",", "9", ",", "1", ",", "5", ")", ")", ")");
    }

    private void checkTokenizer(final String input, final String... strings) {
        final List<String> expected = Arrays.asList(strings);
 
         Tokenizer tokenizer = new TokenizerImpl(input);
 
         final List<String> result = new ArrayList<>();
         while (tokenizer.hasNextToken()) {
             result.add(tokenizer.getNextToken());
         }
 
         Assert.assertEquals(expected, result);
     }
 }
