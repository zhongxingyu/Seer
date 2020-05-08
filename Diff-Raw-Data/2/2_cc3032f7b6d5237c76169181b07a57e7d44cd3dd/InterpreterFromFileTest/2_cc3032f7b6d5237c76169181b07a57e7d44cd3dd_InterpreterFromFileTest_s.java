 /**
  * Lisp Subinterpreter, an interpreter for a sublanguage of Lisp
  * Copyright (C) 2011  Meisam Fathi Salmi <fathi@cse.ohio-state.edu>
  * 
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package edu.osu.cse.meisam.interpreter;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.Vector;
 
 import junit.framework.Assert;
 
 /**
  * @author Meisam Fathi Salmi <fathi@cse.ohio-state.edu>
  * 
  */
 public class InterpreterFromFileTest extends TestStub {
 
     private boolean isInterPreterPassingTestFile(final File fileName) {
         return fileName.getName().startsWith("pass-lex-parse-interpret")
                 && isTestFile(fileName);
     }
 
     private boolean isInterpreterFailingTestFile(final File fileName) {
         return isTestFile(fileName)
                 && (fileName.getName()
                         .startsWith("pass-lex-parse-fail-interpret-"));
     }
 
     private Collection getInterpreterPassingTestFiles() {
         final File[] allFiles = getFiles(TestStub.TEST_DIR);
         final Vector testFiles = new Vector(allFiles.length / 2);
 
         for (int i = 0; i < allFiles.length; i++) {
             if (isInterPreterPassingTestFile(allFiles[i])) {
                 testFiles.add(allFiles[i]);
             }
         }
         return testFiles;
     }
 
     private Collection getInterpreterFailingTestFiles() {
         final File[] allFiles = getFiles(TestStub.TEST_DIR);
         final Vector testFiles = new Vector(allFiles.length / 2);
 
         for (int i = 0; i < allFiles.length; i++) {
             if (isInterpreterFailingTestFile(allFiles[i])) {
                 testFiles.add(allFiles[i]);
             }
         }
         return testFiles;
     }
 
     public void testInterpreterByPassingTests() {
         final Collection testFiles = getInterpreterPassingTestFiles();
         for (final Iterator iterator = testFiles.iterator(); iterator.hasNext();) {
             final File testFile = (File) iterator.next();
             verifyAllNodes(testFile);
         }
     }
 
     public void testInterpreterByFailingTests() {
         final Collection testFiles = getInterpreterFailingTestFiles();
         for (final Iterator iterator = testFiles.iterator(); iterator.hasNext();) {
             final File testFile = (File) iterator.next();
             try {
                 final String fileContent = readfromFile(testFile);
                 final InputProvider inputProvider = new StringInputProvider(
                         fileContent);
                 final Interpreter interpreter = new Interpreter(inputProvider,
                         System.out);
                 try {
                     interpreter.interpret();
                     Assert.fail(testFile + " should've faild, but it didn't ");
                } catch (final ParserException e) {
                     // good job
                 }
             } catch (final IOException e) {
                 fail("When testing " + testFile + ", " + e.getMessage());
             }
 
         }
     }
 
     private void verifyAllNodes(final File testFile) {
         try {
             final String fileContent = readfromFile(testFile);
             final InputProvider inputProvider = new StringInputProvider(
                     fileContent);
             final Lexer lexer = new Lexer(inputProvider);
             final Parser parser = new Parser(lexer);
 
             final ParseTree parseTree = parser.parseNextSExpresion();
 
             // TODO
         } catch (final IOException e) {
             fail("When testing " + testFile + ", " + e.getMessage());
         }
     }
 
     private String getInterpreterExpectedOutputFileName(final File testFile) {
         final String pathname = testFile.getAbsolutePath().replace("input",
                 "parser");
         return pathname;
     }
 
 }
