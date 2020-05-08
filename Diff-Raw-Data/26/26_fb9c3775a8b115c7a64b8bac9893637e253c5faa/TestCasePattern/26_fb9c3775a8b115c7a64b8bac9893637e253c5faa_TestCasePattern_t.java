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
 
 import java.text.ParseException;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.List;
 
 import org.junit.Test;
 
 import com.edugility.objexj.engine.Program;
 
 import static org.junit.Assert.*;
 
 public class TestCasePattern {
 
   public TestCasePattern() {
     super();
   }
 
   @Test
   public void testCompile() throws IOException, ParseException {
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
   public void testLastException() throws IOException, ParseException {
     final String sourceCode = "^java.lang.Exception*/(java.lang.Exception(message == \"third\"))$";
     final Pattern<Exception> pattern = Pattern.compile(sourceCode);
     assertNotNull(pattern);
     final Program<Exception> program = pattern.getProgram();
     assertNotNull(program);
     final List<Exception> input = new ArrayList<Exception>();
     input.add(new IllegalStateException("first"));
     input.add(new IllegalArgumentException("second"));
     final Exception third = new RuntimeException("third");
     input.add(third);
     final Matcher<Exception> matcher = pattern.matcher(input);
     assertNotNull(matcher);
     assertTrue(matcher.matches());
     assertEquals(2, matcher.groupCount());
     final List<? extends Exception> group1 = matcher.group(1);
     assertNotNull(group1);
     assertEquals(1, group1.size());
     final Exception group1Exception = group1.get(0);
     assertSame(third, group1Exception);
   }
 
   @Test
  public void testUnanchoredLastException() throws IOException, ParseException {
    final String sourceCode = "java.sql.SQLException$";
    final Pattern<Exception> pattern = Pattern.compile(sourceCode);
    assertNotNull(pattern);
    final List<Exception> input = new ArrayList<Exception>();
    input.add(new IllegalStateException("first"));
    input.add(new IllegalArgumentException("second"));
    final Exception third = new RuntimeException("third");
    input.add(third);
    final Matcher<Exception> matcher = pattern.matcher(input);
    assertNotNull(matcher);
    assertFalse(matcher.matches());
  }

  @Test
   public void testDoubleVariableAssignment() throws IOException, ParseException {
     final String sourceCode = "^java.lang.Exception(msg = message; return true)*/(java.lang.Exception(msg = message; message == \"third\"))$";
     final Pattern<Exception> pattern = Pattern.compile(sourceCode);
     assertNotNull(pattern);
     final Program<Exception> program = pattern.getProgram();
     assertNotNull(program);
     final List<Exception> input = new ArrayList<Exception>();
     input.add(new IllegalStateException("first"));
     input.add(new IllegalArgumentException("second"));
     final Exception third = new RuntimeException("third");
     input.add(third);
     final Matcher<Exception> matcher = pattern.matcher(input);
     assertNotNull(matcher);
     assertTrue(matcher.matches());
     assertEquals(2, matcher.groupCount());
     final List<? extends Exception> group1 = matcher.group(1);
     assertNotNull(group1);
     assertEquals(1, group1.size());
     final Exception group1Exception = group1.get(0);
     assertSame(third, group1Exception);
     assertEquals(third.getMessage(), matcher.get("msg"));
   }
 
 
   @Test
   public void testVariables() throws IOException, ParseException {
     final String sourceCode = "^java.lang.Exception(msg = message; return true)";
     final Pattern<Exception> pattern = Pattern.compile(sourceCode);
     assertNotNull(pattern);
     final Program<Exception> program = pattern.getProgram();
     assertNotNull(program);
     final List<Exception> input = new ArrayList<Exception>();
     final Exception first = new IllegalStateException("first");
     input.add(first);
     final Matcher<Exception> matcher = pattern.matcher(input);
     assertNotNull(matcher);
     assertTrue(matcher.matches());
     assertEquals(1, matcher.groupCount());
     final List<? extends Exception> group1 = matcher.group(0);
     assertNotNull(group1);
     assertEquals(1, group1.size());
     final Exception group1Exception = group1.get(0);
     assertSame(first, group1Exception);
     assertEquals(first.getMessage(), matcher.get("msg"));
   }
 
 }
