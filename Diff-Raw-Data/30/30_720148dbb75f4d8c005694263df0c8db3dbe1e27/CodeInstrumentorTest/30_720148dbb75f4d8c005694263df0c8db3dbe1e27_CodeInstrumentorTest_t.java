 /*
  * Copyright 2009 Google Inc.
  *
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
  * use this file except in compliance with the License. You may obtain a copy of
  * the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations under
  * the License.
  */
 package com.google.jstestdriver.coverage;
 
 import com.google.common.collect.Lists;
 
 import junit.framework.TestCase;
 
 import java.util.Iterator;
 import java.util.List;
 
 public class CodeInstrumentorTest extends TestCase {
 
   public void testSourceFileLineCoverageDecoration() throws Exception {
     new CoverageAsserter()
       .instrument("LCOV_HASH=LCOV.initNoop('FILE',0,[1, 2]);LCOV_HASH[1]++;").code(" var a = 1;")
       .instrument("LCOV_HASH[2]++;").code(" var b = 2;")
       .assertCoverage();
   }
  
  //TODO(corysmith): Figure out how to fix this test.
  /*public void testSourceFileLineCommentDecoration() throws Exception {
    new CoverageAsserter()
    .instrument("LCOV_HASH=LCOV.initNoop('FILE',0,[1, 2]);").code("//foo")
    .noInstrument().code("// bar")
    .assertCoverage();
  }*/
 
 
   public void testDoNotDecorateStatementsAcrossMultipleLines() throws Exception {
     new CoverageAsserter()
        .instrument("LCOV_HASH=LCOV.initNoop('FILE',0,[1, 2, 5]);LCOV_HASH[1]++;").code("var a = 0;")
        .instrument("LCOV_HASH[2]++;").code("a = true")
        .noInstrument().code("? 1")
        .noInstrument().code(": 2;")
        .instrument("LCOV_HASH[5]++;").code(" a = 3;")
        .assertCoverage();
   }
 
   public void testDecorateIfStatement() throws Exception {
     new CoverageAsserter()
         .instrument("LCOV_HASH=LCOV.initNoop('FILE',0,[1, 2, 3, 5, 7]);LCOV_HASH[1]++;")
                                       .code("var a = 0;")
         .instrument("LCOV_HASH[2]++;").code("if (a) {")
         .instrument("LCOV_HASH[3]++;").code("  a = 1;")
         .noInstrument().code(               "}else{")
         .instrument("LCOV_HASH[5]++;").code("  a = 2;")
         .noInstrument().code(               "}")
         .instrument("LCOV_HASH[7]++;").code("a = 3;")
         .assertCoverage();
       
   }
   
   public void testDecorateNakedIfWithMultilineStatement() throws Exception {
     new CoverageAsserter()
          .instrument("LCOV_HASH=LCOV.initNoop('FILE',0,[1, 2, 3, 5]);LCOV_HASH[1]++;")
                                         .code("var a = 0;")
          .instrument("LCOV_HASH[2]++;") .code("if (a)")
          .instrument("{LCOV_HASH[3]++;").code("hello(")
          .noInstrument()                .code("6);", "6);}")
          .instrument("LCOV_HASH[5]++;") .code("a = 3;")
          .assertCoverage();
   }
   
   public void testDecorateFunctionStatement() throws Exception {
     new CoverageAsserter()
       .instrument("LCOV_HASH=LCOV.initNoop('FILE',0,[1, 2, 3, 5]);LCOV_HASH[1]++;")
                                     .code(" var a = 0;")
       .instrument("LCOV_HASH[2]++;").code("function foo (a) {")
       .instrument("LCOV_HASH[3]++;").code("  a = 1;")
       .noInstrument()               .code("}")
       .instrument("LCOV_HASH[5]++;").code(" a = 3;")
       .assertCoverage();
   }
   
   public void testDecorateWhileStatement() throws Exception {
     new CoverageAsserter()
         .instrument("LCOV_HASH=LCOV.initNoop('FILE',0,[1, 2, 3, 5]);LCOV_HASH[1]++;")
         .code(" var a = 0;")
         .instrument("LCOV_HASH[2]++;").code("while (a) {")
         .instrument("LCOV_HASH[3]++;").code("  a = 1;")
         .noInstrument()               .code("}")
         .instrument("LCOV_HASH[5]++;").code("a = 3;")
         .assertCoverage();
   }
   
   public void testDecorateForStatement() throws Exception {
     new CoverageAsserter()
         .instrument("LCOV_HASH=LCOV.initNoop('FILE',0,[1, 2, 5, 7]);LCOV_HASH[1]++;")
                                       .code("var a = 0;")
         .instrument("LCOV_HASH[2]++;").code("for (")
         .noInstrument()               .code("     var a = 1;")
         .noInstrument()               .code("     a < 20; a++){")
         .instrument("LCOV_HASH[5]++;").code("  a = 1;")
         .noInstrument()               .code("}")
         .instrument("LCOV_HASH[7]++;").code("a = 3;")
         .assertCoverage();
   }
   
   public void testDecorateNakedForStatement() throws Exception {
     new CoverageAsserter()
     .instrument("LCOV_HASH=LCOV.initNoop('FILE',0,[1, 2, 5, 6]);LCOV_HASH[1]++;")
     .code("var a = 0;")
     .instrument("LCOV_HASH[2]++;").code("for (")
     .noInstrument()               .code("     var a = 1;")
     .noInstrument()               .code("     a < 20; a++)")
     .instrument("{LCOV_HASH[5]++;").code("a = 1;", "a = 1;}")
     .instrument("LCOV_HASH[6]++;").code("a = 3;")
     .assertCoverage();
   }
 
   public void testDecorateNakedIfStatement() throws Exception {
     new CoverageAsserter()
          .instrument("LCOV_HASH=LCOV.initNoop('FILE',0,[1, 2, 3, 5, 6]);LCOV_HASH[1]++;")
                                         .code("var a = 0;")
          .instrument("LCOV_HASH[2]++;") .code("if (a)")
          .instrument("{LCOV_HASH[3]++;").code("  a = 1;",  "  a = 1;}")
          .noInstrument()                .code("else")
          .instrument("{LCOV_HASH[5]++;").code("  a = 2;", "  a = 2;}")
          .instrument("LCOV_HASH[6]++;") .code("a = 3;")
          .assertCoverage();
   }
   
   public void testDecorateNakedNestedIfStatements() throws Exception {
     new CoverageAsserter()
     .instrument("LCOV_HASH=LCOV.initNoop('FILE',0,[1, 2, 3, 4, 5]);LCOV_HASH[1]++;")
                                    .code("var a = 0;")
     .instrument("LCOV_HASH[2]++;") .code("if (a)")
     .instrument("{LCOV_HASH[3]++;").code("  if(a)")
     .instrument("{LCOV_HASH[4]++;").code("      a = 2;", "      a = 2;}}")
     .instrument("LCOV_HASH[5]++;") .code("a = 3;")
     .assertCoverage();
   }
 
   private static class CoverageAsserter {
     List<Instrumentation> instruments = Lists.newLinkedList();
     List<CodeLine> source = Lists.newLinkedList();
     String filePath = "filename.js";
     CoverageNameMapper mapper = new CoverageNameMapper();
     String hash = Integer.toString(
         Math.abs(
             String.valueOf(
                 mapper.map(filePath)).hashCode()),
                 Character.MAX_RADIX);
 
     public CoverageAsserter instrument(String instrument) {
       instruments.add(new LcovInstrumentation(instrument.replaceAll("LCOV_HASH",
         "LCOV_" + hash).replaceAll("'FILE'", String.valueOf(mapper.map(filePath)))));
       return this;
     }
     public CoverageAsserter noInstrument() {
       instruments.add(new NoInstrumentation());
       return this;
     }
     public CoverageAsserter code(String code) {
       source.add(new CodeLine(code));
       return this;
     }
     public CoverageAsserter code(String original, String expected) {
       source.add(new CodeLine(original, expected));
       return this;
     }
     
     public void assertCoverage() {
       StringBuilder sourceCode = new StringBuilder();
       for (CodeLine line : source) {
         sourceCode.append(line.getSource());
       }
       
       Instrumentor decorator =
           new CodeInstrumentor(mapper);
       Iterator<Instrumentation> instrumentsIterator = instruments.iterator();
       Iterator<CodeLine> sourceIterator = source.iterator();
       InstrumentedCode decorated = decorator.instrument(
           new Code(filePath, sourceCode.toString()));
       for(String actual : decorated.getInstrumentedCode().split("\n")) {
         instrumentsIterator.next().assertLine(actual);
         sourceIterator.next().assertLine(actual);
       }
     }
   }
   
   private static class CodeLine {
     private final String original;
     private final String expected;
 
     public CodeLine(String code) {
       expected = code;
       this.original = code;
     }
 
     public CodeLine(String original, String expected) {
       this.original = original;
       this.expected = expected;
     }
 
     public void assertLine(String actual) {
       assertTrue("[" + actual + "] does not contain [" + expected +"]",
         actual.contains(expected.trim()));
     }
 
     public String getSource() {
       return original + "\n";
     }
   }
 
   private static interface Instrumentation {
     public void assertLine(String actual);
   }
 
   private static class LcovInstrumentation implements Instrumentation{
 
     private final String instrument;
 
     public LcovInstrumentation(String instrument) {
       this.instrument = instrument;
     }
 
     public void assertLine(String actual) {
       assertTrue("["+actual + "] does not contain [" + instrument + "]",
         actual.contains(instrument));
     }
   }
 
   private static class NoInstrumentation implements Instrumentation {
 
       public void assertLine(String actual) {
         assertFalse(actual + " does contain LCOV_", actual.contains("LCOV_"));
       }
   }
 }
