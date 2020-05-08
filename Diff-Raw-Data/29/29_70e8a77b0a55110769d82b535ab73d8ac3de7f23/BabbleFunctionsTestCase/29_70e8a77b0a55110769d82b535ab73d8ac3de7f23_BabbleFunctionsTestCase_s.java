 package org.babblelang.tests;
 
 import org.babblelang.engine.impl.Function;
 import org.junit.Assert;
 import org.junit.Test;
 
 import javax.script.ScriptException;
 
 public class BabbleFunctionsTestCase extends BabbleTestBase {
     @Test
     public void testFunctionLiteral() throws Exception {
         Assert.assertTrue(interpret("def add = (a:int, b:int):int -> ( a + b )") instanceof Function);
     }
 
     @Test
     public void testFunctionCall() throws Exception {
         Assert.assertEquals(2, interpret("def add = (a:int, b:int):int -> ( a + b ) ; add(1,1)"));
         Assert.assertEquals("ab", interpret("def add = (a, b) -> ( a + b ) ; add(\"a\",\"b\")"));
     }
 
     @Test
     public void testParameterPassing() throws Exception {
         Assert.assertEquals("ab", interpret("def add = (a, b) -> ( a + b ) ; add(a:\"a\",b:\"b\")"));
         Assert.assertEquals("ba", interpret("def add = (a, b) -> ( a + b ) ; add(b:\"a\",a:\"b\")"));
 
         try {
             Assert.assertEquals("ba", interpret("def add = (a, b) -> ( a + b ) ; add(b:\"a\")"));
             Assert.fail("Should report missing parameter");
         } catch (ScriptException e) {
            Assert.assertEquals("java.lang.IllegalArgumentException: Line 1, missing parameter : a in <input> at line number 1", e.getMessage());
         }
     }
 
     @Test
     public void testCallableError() throws Exception {
         try {
             Assert.assertEquals(null, interpret("def add = (a, b) -> ( a + b ) ; add(a:\"a\",b:\"b\")(\"foobar\")"));
             Assert.fail("Should report not callable expression");
         } catch (ScriptException e) {
             Assert.assertEquals("java.lang.RuntimeException: add(a:\"a\",b:\"b\") is not callable in <input> at line number 1", e.getMessage());
         }
     }
 
     @Test
     public void testParameterPassingWithDefaults() throws Exception {
         Assert.assertEquals("ab", interpret("def add = (a=\"hello\", b) -> ( a + b ) ; add(a:\"a\",b:\"b\")"));
         Assert.assertEquals("hellob", interpret("def add = (a=\"hello\", b) -> ( a + b ) ; add(b:\"b\")"));
         Assert.assertEquals("ahello", interpret("def add = (a, b=\"hello\") -> ( a + b ) ; add(\"a\")"));
         Assert.assertEquals("hello world", interpret("def add = (a=\"hello\", b=\" world\") -> ( a + b ) ; add()"));
 
         try {
             Assert.assertEquals("ba", interpret("def add = (a=\"b\", b) -> ( a + b ) ; add(a:\"a\")"));
             Assert.fail("Should report \"Missing parameter : b\"");
         } catch (ScriptException e) {
            Assert.assertEquals("java.lang.IllegalArgumentException: Line 1, missing parameter : b in <input> at line number 1", e.getMessage());
         }
     }
 
     @Test
     public void testParameterPassingWithFunctionDefaults() throws Exception {
         Assert.assertEquals(1, interpret("def apply = (a, f=(v) -> (v)) -> ( f(a) ) ; apply(1)"));
         Assert.assertEquals(2, interpret("def apply = (a, f=(v) -> (v+1)) -> ( f(a) ) ; apply(1)"));
     }
 
     @Test
     public void testFunctionScope() throws Exception {
         Assert.assertEquals(102, interpret("def a = 99 ; def add = (a:int, b:int):int -> ( a = a + 1 ; a + b ) ; add(1,1) + a"));
     }
 
     @Test
     public void testClosureScope() throws Exception {
         Assert.assertEquals(2005, interpret("def a = 99 ; def c = 1000; def adder = (a:int):(b:int):int -> ( (b:int):int -> ( a + b + c) ) ; def plus1 = adder(1) ; plus1(1) + plus1(2)"));
     }
 
     @Test
     public void testClosureMemory() throws Exception {
         Assert.assertEquals(2005, interpret("def a = 99 ; def c = 1000; def adder = (a:int,d:int):(b:int):int -> ( (b:int):int -> ( a + b + c ) ) ; def plus1 = adder(1,0) ; plus1(1) + plus1(2)"));
     }
 
     @Test
     public void testRecursion() throws Exception {
         Assert.assertEquals(120, interpret("def fac = (n:int):int -> ( if(n<=1) then ( 1 ) else ( n * recurse(n-1) ) ) ; fac(5)"));
     }
 
     @Test
     public void testMutualRecursion() throws Exception {
         Assert.assertEquals(720, interpret("def fac = (n:int):int -> ( if(n<=1) then ( 1 ) else ( n * fac2(n-1) ) ) ; def fac2 = (n:int):int -> ( if(n<=1) then ( 1 ) else ( n * fac(n-1) ) ) ; fac(6)"));
     }
 }
