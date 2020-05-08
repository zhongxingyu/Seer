 /*
  * Licensed to the Apache Software Foundation (ASF) under one
  * or more contributor license agreements.  See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership.  The ASF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License.  You may obtain a copy of the License at
  *
  *   http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied.  See the License for the
  * specific language governing permissions and limitations
  * under the License.
  */
 package org.apache.myfaces.extensions.scripting.compiler;
 
 import java.io.File;
 
 import org.apache.myfaces.extensions.scripting.AbstractGeneratorTestCase;
import org.junit.Ignore;
 
 /**
  * <p>Test class for all Java implementations of
  * <code>org.apache.myfaces.extensions.scripting.compiler.Compiler</code>.</p>
  */
@Ignore
 public abstract class AbstractJavaCompilerTestCase extends AbstractGeneratorTestCase {
 
     // ------------------------------------------ Test methods
 
     public void testCompileGeneratedFile() throws Exception {
         writeFile("/src/main/java/org/apache/myfaces/extensions/scripting/HelloWorld.java", new String[]{
                 "package org.apache.myfaces.extensions.scripting;   ",
                 "                                                   ",
                 "public class HelloWorld {                          ",
                 "                                                   ",
                 "   public static void main(String[] args) {        ",
                 "       System.out.println(\"Hello World\");        ",
                 "   }                                               ",
                 "}                                                  "
         });
 
         Compiler compiler = createCompiler();
         CompilationResult result = compiler.compile(
                 new File(buildAbsolutePath("/src/main/java")),
                 new File(buildAbsolutePath("/target/test-classes")),
                 "org/apache/myfaces/extensions/scripting/HelloWorld.java", getCurrentClassLoader());
 
         assertFalse(result.hasErrors());
         assertTrue(new File(
                 buildAbsolutePath("/target/test-classes/"), "org/apache/myfaces/extensions/scripting/HelloWorld.class").exists());
     }
 
     public void testCompileGeneratedFileWithError() throws Exception {
         writeFile("/src/main/java/org/apache/myfaces/extensions/scripting/HelloWorld.java", new String[]{
                 "package org.apache.myfaces.extensions.scripting;   ",
                 "                                                   ",
                 "public class HelloWorld {                          ",
                 "                                                   ",
                 "   public static void main(String[] args) {        ",
                 "       System.println(\"Hello World\");            ",
                 "   }                                               ",
                 "}                                                  "
         });
 
         Compiler compiler = createCompiler();
         CompilationResult result = compiler.compile(
                 new File(buildAbsolutePath("/src/main/java")),
                 new File(buildAbsolutePath("/target/test-classes")),
                 "org/apache/myfaces/extensions/scripting/HelloWorld.java", getCurrentClassLoader());
 
         assertTrue(result.hasErrors());
         assertFalse(new File(
                 buildAbsolutePath("/target/test-classes/"), "org/apache/myfaces/extensions/scripting/HelloWorld.class").exists());
     }
 
     public void testCompileFileWithDependencies() throws Exception {
         writeFile("/src/main/java/org/apache/myfaces/extensions/scripting/DummyCompiler.java", new String[]{
                 "package org.apache.myfaces.extensions.scripting;                                   ",
                 "                                                                                   ",
                 "import java.io.File;                                                               ",
                 "import org.apache.myfaces.extensions.scripting.compiler.Compiler;                  ",
                 "import org.apache.myfaces.extensions.scripting.compiler.CompilationResult;         ",
                 "                                                                                   ",
                 "public class DummyCompiler implements Compiler {                                   ",
                 "                                                                                   ",
                 "   public CompilationResult compile(File s, File t, String f, ClassLoader c) {     ",
                 "       return null;                                                                ",
                 "   }                                                                               ",
                 "                                                                                   ",
                 "   public CompilationResult compile(File s, File t, File f, ClassLoader c) {       ",
                 "       return null;                                                                ",
                 "   }                                                                               ",
                 "}                                                                                  "
 
         });
 
         Compiler compiler = createCompiler();
         CompilationResult result = compiler.compile(
                 new File(buildAbsolutePath("/src/main/java")),
                 new File(buildAbsolutePath("/target/test-classes")),
                 "org/apache/myfaces/extensions/scripting/DummyCompiler.java", getCurrentClassLoader());
 
         assertFalse(result.hasErrors());
         assertTrue(new File(
                 buildAbsolutePath("/target/test-classes/"), "org/apache/myfaces/extensions/scripting/DummyCompiler.class").exists());
     }
 
     public void testCompileFileWithDependenciesWithoutClassloader() throws Exception {
         writeFile("/src/main/java/org/apache/myfaces/extensions/scripting/DummyCompiler.java", new String[]{
                 "package org.apache.myfaces.extensions.scripting;                                   ",
                 "                                                                                   ",
                 "import java.io.File;                                                               ",
                 "import org.apache.myfaces.extensions.scripting.compiler.Compiler;                  ",
                 "import org.apache.myfaces.extensions.scripting.compiler.CompilationResult;         ",
                 "                                                                                   ",
                 "public class DummyCompiler implements Compiler {                                   ",
                 "                                                                                   ",
                 "   public CompilationResult compile(File s, File t, String f, ClassLoader c) {     ",
                 "       return null;                                                                ",
                 "   }                                                                               ",
                 "                                                                                   ",
                 "   public CompilationResult compile(File s, File t, File f, ClassLoader c) {       ",
                 "       return null;                                                                ",
                 "   }                                                                               ",
                 "}                                                                                  "
 
         });
 
         Compiler compiler = createCompiler();
         CompilationResult result = compiler.compile(
                 new File(buildAbsolutePath("/src/main/java")),
                 new File(buildAbsolutePath("/target/test-classes")),
                 "aorg/apache/myfaces/extensions/scripting/DummyCompiler.java", null);
 
         assertTrue(result.hasErrors());
         assertFalse(new File(
                 buildAbsolutePath("/target/test-classes/"), "org/apache/myfaces/extensions/scripting/DummyCompiler.class").exists());
     }
 
     // ------------------------------------------ Utility methods
 
     protected ClassLoader getCurrentClassLoader() {
         return getClass().getClassLoader();
     }
 
     // ------------------------------------------ Template methods
 
     /**
      * <p>Creates a new instance of the compiler implementation that this class should test.</p>
      *
      * @return a new instance of the compiler implementation that this class should test
      */
     protected abstract Compiler createCompiler();
 
 }
