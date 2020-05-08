 package org.libj.xquery;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertTrue;
 import static org.libj.xquery.Compiler.eval;
 import static org.libj.xquery.Compiler.compile;
 
 public class Asserts {
     public static void assertEval(String script, Object expected) {
        assertEquals(expected, eval(script));
     }
 
     public static void assertEvalString(String script, String expected) {
        assertEquals(expected, eval(script).toString());
     }
 
     public static void assertEvalRegex(String script, String expected) {
         String result = eval(script).toString();
         assertTrue(String.format("%s doesn't match pattern %s", result, expected), result.matches(expected));
     }
 
     // for performance testing...
 
     public static void assertEvalMillis(String script, int targetMillis) {
         XQuery q = compile(script);
         long start = System.currentTimeMillis();
         q.eval();
         long end = System.currentTimeMillis();
         long executionMillis = end - start;
         assertTrue(String.format("Timeout: %d > %d", executionMillis, targetMillis), executionMillis <= targetMillis);
     }
 
     public static void assertRepeatedEvalMillis(String script, int n, int targetMillis) {
         XQuery q = compile(script);
         long start = System.currentTimeMillis();
         for (int i = 0; i < n; i++) {
             q.eval();
         }
         long end = System.currentTimeMillis();
         long executionMillis = end - start;
         assertTrue(String.format("Timeout: %d > %d (loop %d)", executionMillis, targetMillis, n), executionMillis <= targetMillis);
     }
 
     public static void assertRepeatedEvalPerSecond(String script, int n) {
         assertRepeatedEvalMillis(script, n, 1000);
     }
 
     public static void assertRepeatedCompileMillis(String script, int n, int targetMillis) {
         long start = System.currentTimeMillis();
         for (int i = 0; i < n; i++) {
             XQuery q = compile(script);
         }
         long end = System.currentTimeMillis();
         long executionMillis = end - start;
         assertTrue(String.format("Timeout: %d > %d (loop %d)", executionMillis, targetMillis, n), executionMillis < targetMillis);
     }
 
     public static void assertRepeatedCompilePerSecond(String script, int n) {
         assertRepeatedCompileMillis(script, n, 1000);
     }
 }
