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
 
 public class TokenizerImpl implements Tokenizer {
     /**
      * Used to split without skipping delimiters. For example:
      * <code>"foo*bar".split(WITH_DELIMITER.format("[*]"))</code> becomes:
      * <code>"foo", "*", "bar"</code>
      */
     private static final String WITH_DELIMITER = "((?<=%1$s)|(?=%1$s))";
 
     private final String[] tokenStrings;
 
     private int pos = 0;
 
     public TokenizerImpl(final String input) {
        this.tokenStrings = input.split(String.format(WITH_DELIMITER, "[*\\s+,/()^%-]"));
     }
 
     @Override
     public boolean hasNextToken() {
         return pos < tokenStrings.length;
     }
 
     @Override
     public String getNextToken() {
         final String result = tokenStrings[pos++];
         while (pos < tokenStrings.length && tokenStrings[pos].matches("\\s")) {
             // skip white spaces
             ++pos;
         }
         return result;
     }
 }
