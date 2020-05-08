 /*
  * Created on 05.jul.2005
  *
  * Copyright (c) 2005, Karl Trygve Kalleberg <karltk@ii.uib.no>
  * 
  * Licensed under the IBM Common Public License, v1.0
  */
 package org.spoofax.interpreter.test.library;
 
 import java.io.IOException;
 import java.util.Map;
 import java.util.TreeMap;
 
 import org.spoofax.interpreter.InterpreterException;
 import org.spoofax.interpreter.test.AbstractInterpreterTest;
 
 public class LibraryTest extends AbstractInterpreterTest {
 
     @Override
     protected void setUp() throws Exception {
         super.setUp("tests/data/library");
     }
 
     public void testChar() throws IOException, InterpreterException {
         interpTest("char-test", "(3,0)");
     }
     
     public void testOldParseOptions() throws IOException, InterpreterException {
         interpTest("old-parse-options-test", "(2,0)");
     }
     public void testTuple() throws IOException, InterpreterException 
     {  interpTest("tuple-test", "(2,0)"); }
     public void testReals() throws IOException, InterpreterException 
     {  interpTest("reals-test", "(8,0)"); }
     public void testListSet() throws IOException, InterpreterException 
     { interpTest("list-set-test", "(25,0)"); }
     public void testListSort() throws IOException, InterpreterException 
     { interpTest("list-sort-test", "(7,0)"); }
     public void testListZip() throws IOException, InterpreterException 
     { interpTest("list-zip-test", "(4,0)"); }
     public void testIntList() throws IOException, InterpreterException 
     { interpTest("int-list-test", "(15,0)"); }
     public void testListMisc() throws IOException, InterpreterException 
     { interpTest("list-misc-test", "(23,0)"); }
     public void testListIndex() throws IOException, InterpreterException 
     { interpTest("list-index-test", "(17,0)"); }
     public void testListFilter() throws IOException, InterpreterException 
     { interpTest("list-filter-test", "(2,0)");   }
     public void testIntegers() throws IOException, InterpreterException 
     { interpTest("integers-test", "(101,0)"); }
     public void testIteration() throws IOException, InterpreterException 
     { interpTest("iteration-test", "(25,0)"); }
     public void testUnification() throws IOException, InterpreterException 
     { interpTest("unification-test", "(2,0)"); }
     public void testStrcmp() throws IOException, InterpreterException 
     { interpTest("strcmp-test", "(16,0)"); }
     public void testTermZip() throws IOException, InterpreterException 
     { interpTest("term-zip-test", "(8,0)"); }
     public void testEnvTraversal() throws IOException, InterpreterException 
     { interpTest("env-traversal-test", "(2,0)"); }
     public void testSets() throws IOException, InterpreterException 
     { interpTest("sets-test", "(15,0)"); }
     public void testTables() throws IOException, InterpreterException 
     { interpTest("tables-test", "(59,0)"); }
     public void testSubstitution() throws IOException, InterpreterException 
     { interpTest("substitution-test", "(1,0)");   }
 
     public void testTermCommon() throws IOException, InterpreterException {
         interpTest("term-common-test", "(12,0)");
     }
     
     public void testListBasic() throws IOException, InterpreterException 
     { interpTest("list-basic-test" ,"(41,0)"); }
     public void testStringMisc() throws IOException, InterpreterException 
     { interpTest("string-misc-test", "(29,0)"); }
 
     public void testDynamicRulesLowlevel() throws IOException, InterpreterException 
     { interpTest("dynamic-rules-lowlevel-test", "(46,0)"); }
     public void testDynamicRulesHighlevel() throws IOException, InterpreterException 
    { interpTest("dynamic-rules-highlevel-test", "(10,0)"); }
 
     public void testSystemIOFile() throws IOException, InterpreterException {
         interpTest("system-io-file-test", "(0,0)");
     }
     
     public void testPOSIX() throws IOException, InterpreterException {
         interpTest("system-posix-test", "(0,0)");
     }
 
     public void testPOSIXXSI() throws IOException, InterpreterException {
         interpTest("system-posix-xsi-test", "(0,0)");
     }
     
 /*
  *  Relies on ANSI C and/or POSIX semantics
     public void testPOSIXError() {  interpTest("posix-error-test");   }
     public void testIO() { interpTest("io-test"); }
     public void testDir() { interpTest("dir-test"); }
     public void testPOSIXFile() { interpTest("posix-file-test"); }
     public void testPOSIXProcess() { interpTest("posix-process-test"); }
     public void testTime() { interpTest("time-test"); }
 
  *  Known not to work.
     public void testShare() throws IOException, InterpreterException 
     { interpTest("share-test", "(0,0)"); }
     public void testPlaceholder() throws IOException, InterpreterException 
     {  interpTest("placeholder-test", "(0,0)");   }
 */
 
     public void testString() throws IOException, InterpreterException 
     { interpTest("string-test", "(71,0)"); }
     public void testRename() throws IOException, InterpreterException 
     { interpTest("rename-test", "(2,0)"); }
     public void testScopedFiniteMap() throws IOException, InterpreterException 
     { interpTest("scoped-finite-map-test", "(5,0)"); }
     public void testSimpleTraversal() throws IOException, InterpreterException 
     { interpTest("simple-traversal-test", "(6,0)"); }
     public void testCollect() throws IOException, InterpreterException 
     { interpTest("collect-test", "(15,0)"); }
     public void testParenthesize() throws IOException, InterpreterException
     { interpTest("parenthesize-test", "(4,0)"); }
     public void testParseOptions() throws IOException, InterpreterException 
     { interpTest("parse-options-test" ,"(23,0)"); }
 
     public void interpTest(String test, String result) throws IOException, InterpreterException {
         // FIXME tuplify 'result'
         super.interpTest(test, "()", result);
     }
     public static void main(String[] args) {
         Map<Integer, Long> timeSpans = new TreeMap<Integer, Long>();
         long timeSpan = 0;
         for(int i = 0; i < 1; i++) {
             long timeStart = System.currentTimeMillis();
             junit.textui.TestRunner.run(LibraryTest.class);
             timeSpan = System.currentTimeMillis() - timeStart;
             timeSpans.put(i, timeSpan);
         }
         for (Integer i : timeSpans.keySet()) {
             System.out.println("->" + i + " = " + timeSpans.get(i));
         }
     }
 }
 
