 package org.unbunt.ellatest;
 
 import static org.testng.Assert.*;
 import org.testng.annotations.BeforeTest;
 import org.testng.annotations.Test;
 import org.unbunt.ella.Ella;
 import static org.unbunt.ella.Ella.eval;
 import static org.unbunt.ella.Ella.evalIncremental;
 import org.unbunt.ella.exception.*;
 import static org.unbunt.ellatest.TestUtils.ensureType;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 @Test(groups = { "interpreter" }, dependsOnGroups = { "parser" })
 public class InterpreterTestsNG extends AbstractTest {
     protected Ella interp;
 
     @BeforeTest
     public void init() {
         // nothing to do, yet
     }
 
     @Test
     public void emptyStringLiteral() throws EllaParseException, EllaIOException, EllaException {
         // NOTE: This is actually a parser test, but we run it to check the resulting value here.
         Object result = eval(".'';");
         assertEquals(result, "");
     }
 
     @Test
     public void blockScope() throws EllaIOException, EllaParseException, EllaException {
         Object result;
 
         result = eval("{ var i := 23; } .i;");
         assertNull(result, "Variable defined after scope left");
 
         result = eval("var i := 23; { .i; }");
         assertNotNull(result, "Variable undefined in child scope");
         assertTrue(Long.class.equals(result.getClass()), "Variable not of type long");
         assertTrue((Long)result == 23, "Variable not of assigned value");
     }
 
     @Test
     public void javaIntegration() throws EllaIOException, EllaParseException, EllaException {
         Object result;
 
         Date before = new Date();
 
         result = eval("var Date := new JClass('java.util.Date'); .new Date();");
         assertNotNull(result, "Null on java object creation after class import via new JClass()");
         assertTrue(Date.class.equals(result.getClass()), "Date constructor returned not a Date object");
         assertTrue(before.compareTo((Date) result) <= 0, "Date created, but no-arg constructor misbehaved");
 
         result = eval(".Sys.importPackage('java.util'); .new Date();");
         assertNotNull(result, "Null on java object creation after package import via Sys.importPackage()");
 
         result = eval("import java.util.*; .new Date();");
         assertNotNull(result, "Null on java object creation after pacakge import via import statement");
 
         result = eval("import java.util.Date; .new Date();");
         assertNotNull(result, "Null on java object creation after class import via import statement");
 
         result = eval("import java.util.Date as MyDate; .new MyDate();");
         assertNotNull(result, "Null on java object creation after class import via import statement with alias");
 
         result = eval("{ import java.util.*; } .new Date();");
         assertNull(result, "Dynamic environment from package loader not destroyed after leaving scope");
 
         long time = 928439284;
         Date refDate = new Date(time);
         long refTime = refDate.getTime();
 
         result = eval("import java.util.Date; .new Date(" + time +");");
         assertNotNull(result, "Null on java object creation with on-arg constructor");
         assertTrue(Date.class.equals(result.getClass()), "One-arg Date constructor didn't return a date");
         assertTrue(
                 refDate.compareTo((Date) result) == 0,
                 "Date created from one-arg constructor doesn't match reference"
         );
 
         result = eval("import java.util.Date; var d := new Date(" + time + "); .d.getTime();");
         assertNotNull(result, "Method inocation on java object returned null");
         assertTrue(result instanceof Number, "Date.getTime() did not return a number");
         assertTrue(
                 ((Number)result).longValue() == refTime,
                 "Value returned from Date.getTime() does not match reference time"
         );
 
         result = eval("import java.util.Date; var d := new Date(" + time + "); .d.time;");
         assertNotNull(result, "Method invocation via bean property returned null");
         assertTrue(result instanceof Number, "Date.getTime() via bean property did not return a number");
         assertTrue(
                 ((Number)result).longValue() == refTime,
                 "Value returned from Date.getTime() via bean property does not match reference time"
         );
     }
 
     @Test
     public void inheritance() throws EllaIOException, EllaParseException, EllaException {
         Object result;
 
         result = eval("{"
                       + "var A := { value: 'A' }; "
                       + "var B := { parent: A, value: 'B', getValue: fun() { super.value; } }; "
                       + "var b := new B(); b.getValue();"
                       + "}");
         assertNotNull(result, "Getter on instanciated object returned null");
         assertTrue(String.class.equals(result.getClass()), "Getter on instanciated object did not return string");
         assertTrue("A".equals(result), "Super on property returned unexpected value");
 
         result = eval(
                 "{"
                 + "var A := { getValue: fun() { return 'A'; } }; "
                 + "var B := { parent: A, getValue: fun() { return super.getValue(); } }; "
                 + "var b := new B(); b.getValue(); "
                 + "}"
         );
         assertNotNull(result, "Getter on instanciated object returned null");
         assertTrue(String.class.equals(result.getClass()), "Getter on instanciated object did not return string");
         assertTrue("A".equals(result), "Super-call returned unexpected value");
 
         result = eval(
                 "{"
                 + "var A := { a: fun() { this.b(); }, b: fun() { 23; } }; "
                 + "var B := { parent: A, a: fun() { super.a(); }, b: fun() { 42; } }; "
                 + "var b := new B(); b.a(); "
                 + "}"
         );
         assertNotNull(result, "Object method invocation returned null");
         assertTrue(result instanceof Number, "Object method invocation did not returned a number");
         assertTrue(((Number)result).intValue() == 42, "This invoked from super did not execute correct method");
     }
 
     @Test
     public void blockClosures() throws EllaIOException, EllaParseException {
         Object result;
 
         try {
             result = eval(
                     "{\n"
                     + "fun invokeBlock() {\n"
                     + "    fun foo(block) { block(42); return 23; }\n"
                     + "    foo { val => return val; };\n"
                     + "}\n"
                     + "invokeBlock();"
                     + "}\n"
             );
         } catch (EllaException e) {
             throw new RuntimeException(e);
         }
         assertNotNull(result, "Function invocation returned null");
         assertTrue(result instanceof Number, "Function invocation did not return a number");
         assertTrue(((Number)result).intValue() == 42, "Block closure did not return correctly");
 
         try {
             eval(
                     "{\n"
                     + "var block := {=> return 23; };\n"
                     + "block();\n"
                     + "}"
             );
             assertTrue(false,
                        "Failed to catch non-local return in top-level block invocation (didn't throw exception)");
         } catch (EllaException e) {
             assertTrue(e.isCausedBy(EllaNonLocalReturnException.class),
                        "Incorrect exception thrown on non-local return in top-level block invocation");
         }
 
         try {
             eval(
                     "{\n"
                     + "fun returnBlock() { return {=> return 23; }; }"
                     + "var block := returnBlock();\n"
                     + "block();\n"
                     + "}"
             );
             assertTrue(false, "Failed to catch non-local return in block invocation (didn't throw exception)");
         } catch (EllaException e) {
             assertTrue(e.isCausedBy(EllaNonLocalReturnException.class),
                        "Incorrect exception thrown on non-local return in block invocation");
         }
 
         try {
             eval(
                     "{\n"
                     + "fun test() {\n"
                     + "    fun returnBlock() { return {=> return 23; }; }"
                     + "    var block := returnBlock();\n"
                     + "    block();\n"
                     + "}\n"
                     + "test();\n"
                     + "}"
             );
             assertTrue(false, "Failed to catch non-local return in block invocation from foreign function "
                               + "(didn't throw exception)");
         } catch (EllaException e) {
             assertTrue(e.isCausedBy(EllaNonLocalReturnException.class),
                        "Incorrect exception thrown on non-local return in block invocation from foreign function");
         }
 
         try {
             result = eval(
                     "{\n"
                     + "\n"
                     + "fun ifFunc (cond, trueBody) {\n"
                     + "    if (cond()) {\n"
                     + "        trueBody();\n"
                     + "    }\n"
                     + "    return 13;\n"
                     + "}\n"
                     + "\n"
                     + "fun test() {\n"
                     + "    ifFunc({=> 1 == 1; }, {=> ifFunc({=> 2 == 2; }, {=> return 42; }); });\n"
                     + "    return 23;\n"
                     + "}\n"
                     + "\n"
                     + "test();\n"
                     + "\n"
                     + "}"
             );
         } catch (EllaException e) {
             throw new RuntimeException(e);
         }
         assertNotNull(result);
         assertTrue(result instanceof Number);
         assertTrue(((Number)result).intValue() == 42,
                    "Nested block closure returns to wrong home function");
     }
 
     @Test
     public void loops() throws EllaIOException, EllaParseException, EllaException {
         Object result;
 
         result = eval(
                 "var i := 0;\n" +
                 "var j := 0;\n" +
                 ".loop {=>\n" +
                 "    if (i == 3) {\n" +
                 "        break;\n" +
                 "    }\n" +
                 "    if (i == 42) {\n" +
                 "        exit;\n" +
                 "    }\n" +
                 "\n" +
                 "    i = i + 1;\n" +
                 "    continue;\n" +
                 "    j = j + 1;\n" +
                 "};\n" +
                 ".j;"
         );
         assertNotNull(result, "Loop does not honour break statement");
         assertTrue(result instanceof Number);
         assertTrue(((Number)result).intValue() == 0,
                    "Loop does not honour continue statement");
     }
 
     @Test
     public void args() throws EllaIOException, EllaParseException, EllaException {
         int arg = 42;
         Object result = eval(".ARGV[0];", arg);
         Number num = ensureType(Number.class, result);
         assertTrue(num.intValue() == arg, "Passing argument to script failed");
     }
 
     @Test
     public void evalFile() throws EllaIOException, EllaParseException, EllaException {
         int arg = 42;
         Object result = eval(file("eval-file"), arg);
         Number num = ensureType(Number.class, result);
         assertTrue(num.intValue() == arg, "Passing argument to script failed");
     }
 
     @Test
     public void includeFile() throws EllaIOException, EllaParseException, EllaException {
         eval(file("include-file"));
     }
 
     @Test
     public void includeFileClosureUpdate() throws EllaIOException, EllaParseException, EllaException {
         eval(file("include-file-closure-update"));
     }
 
     @Test
     public void includeFileClosureAfterDynamic()
             throws EllaIOException, EllaParseException, EllaException {
         eval(file("include-file-closure-after-dynamic"));
     }
 
     @Test
     public void includeFileDynenv() throws EllaIOException, EllaParseException, EllaException {
         eval(file("include-file-dynenv"));
     }
 
     @Test
     public void includeFileClosureTerminated() throws EllaIOException, EllaParseException, EllaException {
         eval(file("include-file-closure-terminated"));
     }
 
     @Test
     public void includeFileNested() throws EllaIOException, EllaParseException, EllaException {
         eval(file("include-file-nested"));
     }
 
     @Test
     public void includeFileNestedResource() throws EllaIOException, EllaParseException, EllaException {
         eval(file("include-file-nested-resource"));
     }
 
     @Test
     public void floatVsIntSlotDistinction() throws EllaIOException, EllaParseException, EllaException {
         Object result = eval(file("float-vs-int-slot-distinction"));
         assertEquals(((Number) result).intValue(), 3);
     }
 
     @Test
     @SuppressWarnings({"UnnecessaryUnboxing"})
     public void floatLiteral() throws EllaIOException, EllaParseException, EllaException {
         Object result = eval(".1.23;");
         assertEquals(((Double) result).doubleValue(), 1.23d);
     }
 
     @Test
     @SuppressWarnings({"UnnecessaryUnboxing"})
     public void numPropagateInfinity() throws EllaIOException, EllaParseException, EllaException {
         Object result = eval(".(1 / 0.0) * 1.bigRealValue();");
         assertEquals(Double.POSITIVE_INFINITY, ((Double)result).doubleValue());
     }
 
     @Test
     public void numPropagateNaN() throws EllaIOException, EllaParseException, EllaException {
         Object result = eval(".(0 / 0.0) * 1.bigRealValue();");
         assertTrue(Double.isNaN((Double)result));
     }
 
     @Test
     public void numFailOnNaNToBigReal() throws EllaIOException, EllaRecognitionException {
         try {
             eval(".(0 / 0.0).bigRealValue();");
         } catch (Exception e) {
             assertEquals(e.getMessage(), "Cannot convert NaN to BigDecimal");
             return;
         }
         assertTrue(false, "No exception thrown on NaN to BigDecimal conversion");
     }
 
     @Test
     public void numDoubleSpecials() throws EllaIOException, EllaParseException, EllaException {
         eval(file("num-double-specials"));
     }
 
     @Test
     public void operatorPrecedence() throws EllaIOException, EllaParseException, EllaException {
         Object result;
 
         result = eval(". 2 + 3 * 4;");
         assertEquals(result, 14l, "Broken precedance rules with + vs. * operators");
 
         result= eval("{ Num.mult = Num.*; 2 + 3 mult 4; }");
         assertEquals(result, 20l, "Broken precedance rules with + vs. generic binary operators");
     }
 
     @Test
     public void numBigReal() throws EllaIOException, EllaParseException, EllaException {
         Object result = eval(file("num-bigreal"));
         assertEquals(result, "success");
     }
 
     @Test
     public void hostIntegrationMethodSelect() throws EllaIOException, EllaParseException, EllaException {
         eval(file("host-integration-method-select"));
     }
 
     @Test
     public void array() throws EllaIOException, EllaParseException, EllaException {
         Object result = eval(file("array"));
         List<Long> expected = new ArrayList<Long>(5);
         expected.add(1l);
         expected.add(2l);
         expected.add(3l);
         expected.add(4l);
         expected.add(5l);
         assertEquals(result, expected);
     }
 
     public void arrayFilter() throws EllaParseException, EllaIOException, EllaException {
         eval(file("array-filter"));
     }
 
     @SuppressWarnings({"ConstantConditions"})
     @Test
     public void exceptions() throws EllaIOException, EllaParseException {
         EllaException ex = null;
         try {
             eval(file("exceptions"));
         } catch (EllaException e) {
             ex = e;
         }
 
         assertNotNull(ex, "Expected EllaException");
         assertEquals(ex.getMessage(), "intentionally-uncaught-exception");
     }
 
     @Test
     public void whileExit() throws EllaIOException, EllaParseException, EllaException {
         Object result = eval(file("while-exit"));
         assertEquals(result, 42l);
     }
 
     @Test
     public void nativeClone() throws EllaIOException, EllaParseException, EllaException {
         eval(file("native-clone"));
     }
 
     @Test
     public void numRange() throws EllaIOException, EllaParseException, EllaException {
         eval(file("num-range"));
     }
 
     @Test
     public void typeCheck() throws EllaIOException, EllaParseException, EllaException {
         Object result = eval(file("type-check"));
         assertEquals(result, 42l);
     }
 
     @Test
     public void incrementalEnvRetain() throws EllaIOException, EllaException, EllaParseException {
         Object result = evalIncremental(file("incremental-env-retain"));
         assertEquals(result, new ArrayList<Object>());
     }
 
     @Test
     public void dict() throws EllaParseException, EllaIOException, EllaException {
         eval(file("dict"));
     }
 
     @Test
     public void stringProto() throws EllaParseException, EllaIOException, EllaException {
         eval(file("string-proto"));
     }

    /**
     * TODO: Fix the bug triggered by this test.
     *
     * @throws EllaParseException
     * @throws EllaIOException
     * @throws EllaException
     */
    @Test(enabled = false)
    public void bugTryFinallyReturn() throws EllaParseException, EllaIOException, EllaException {
        Object result = eval(file("bug-try-finally-return"));
        assertEquals(result, 42l);
    }
 }
