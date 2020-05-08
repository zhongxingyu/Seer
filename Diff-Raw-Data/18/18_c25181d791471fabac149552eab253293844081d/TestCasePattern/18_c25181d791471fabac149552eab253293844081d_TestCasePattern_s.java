 /* -*- mode: Java; c-basic-offset: 2; indent-tabs-mode: nil -*-
  *
  * Copyright (c) 2013 Edugility LLC.
  *
  * Permission is hereby granted, free of charge, to any person
  * obtaining a copy of this software and associated documentation
  * files (the "Software"), to deal in the Software without
  * restriction, including without limitation the rights to use, copy,
  * modify, merge, publish, distribute, sublicense and/or sell copies
  * of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be
  * included in all copies or substantial portions of the Software.
  *
  * THIS SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
  * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
  * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
  * NONINFRINGEMENT.  IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
  * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
  * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
  * DEALINGS IN THE SOFTWARE.
  *
  * The original copy of this license is available at
  * http://www.opensource.org/license/mit-license.html.
  */
 package com.edugility.objexj;
 
 import java.io.IOException;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import org.junit.Test;
 
 import com.edugility.objexj.engine.Program;
 
 import static org.junit.Assert.*;
 
 public class TestCasePattern {
 
   public TestCasePattern() {
     super();
   }
 
   @Test
   public void testCompile() throws IOException {
     final String sourceCode = "^(java.lang.Character(charValue() == 'a')/(java.lang.Character(fred = \"bozo\"; return charValue() == 'b')))/java.lang.Character(charValue() == 'c')";
     final Pattern<Character> pattern = Pattern.compile(sourceCode);
     assertNotNull(pattern);
     final Program<Character> program = pattern.getProgram();
     assertNotNull(program);
     final List<Character> input = Arrays.asList('a', 'b', 'c');
     final Matcher<Character> matcher = pattern.matcher(input);
     assertNotNull(matcher);
     assertTrue(matcher.matches());
     assertTrue(matcher.matches());
     assertEquals(3, matcher.groupCount());
     assertEquals(Arrays.asList('a', 'b', 'c'), matcher.group(0));
     assertEquals(Arrays.asList('a', 'b'), matcher.group(1));
     assertEquals(Arrays.asList('b'), matcher.group(2));
     assertEquals("bozo", matcher.get("fred"));
   }
 
   @Test
  public void testExceptions() throws IOException {
    final String sourceCode = "^java.lang.Exception*/(java.lang.Exception)$";
     final Pattern<Exception> pattern = Pattern.compile(sourceCode);
     assertNotNull(pattern);
     final Program<Exception> program = pattern.getProgram();
     assertNotNull(program);
    System.out.println(program);
     final List<Exception> input = new ArrayList<Exception>();
     input.add(new IllegalStateException("first"));
     input.add(new IllegalArgumentException("second"));
    input.add(new RuntimeException("third"));
     final Matcher<Exception> matcher = pattern.matcher(input);
     assertNotNull(matcher);
     assertEquals(2, matcher.groupCount());
   }
 
 }
