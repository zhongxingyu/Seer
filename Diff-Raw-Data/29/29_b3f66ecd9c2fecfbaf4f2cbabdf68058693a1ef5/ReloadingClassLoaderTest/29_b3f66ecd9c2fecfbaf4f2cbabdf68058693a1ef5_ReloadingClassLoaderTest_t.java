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
 package org.apache.myfaces.extensions.scripting.loader;
 
 import org.apache.myfaces.extensions.scripting.AbstractGeneratorTestCase;
import org.junit.Ignore;
 
 import java.io.File;
 
 /**
  * <p>Test class for
  * <code>org.apache.myfaces.extensions.scripting.loader.ReloadingClassLoader</code></p>
  */
@Ignore
 public class ReloadingClassLoaderTest extends AbstractGeneratorTestCase {
 
     // ------------------------------------------ Test methods
 
     public void testCompileAndLoadFile() throws Exception {
         // Compile a dynamically generated class ..
         compileFile("/src/main/java", "/target/test-classes", "org/apache/myfaces/extensions/scripting/Greeter.java", new String[]{
                 "package org.apache.myfaces.extensions.scripting;   ",
                 "                                                   ",
                 "public class Greeter {                             ",
                 "                                                   ",
                 "   public String sayHello() {                      ",
                 "       return \"Hello World\";                     ",
                 "   }                                               ",
                 "}                                                  "
         });
 
         ClassLoader classLoader = buildClassLoader("/target/test-classes");
 
         // .. and try to load it afterwards.
         Class greeterClass = classLoader.loadClass("org.apache.myfaces.extensions.scripting.Greeter");
         assertNotNull(greeterClass);
         assertEquals("org.apache.myfaces.extensions.scripting.Greeter", greeterClass.getName());
 
         Object greeter = greeterClass.newInstance();
         String greeting = (String) greeter.getClass().getMethod("sayHello").invoke(greeter);
         assertEquals("Hello World", greeting);
     }
 
     public void testCompileAndLoadFileMultipleTimes() throws Exception {
         // Compile a dynamically generated class ..
         compileFile("/src/main/java", "/target/test-classes", "org/apache/myfaces/extensions/scripting/Greeter.java", new String[]{
                 "package org.apache.myfaces.extensions.scripting;   ",
                 "                                                   ",
                 "public class Greeter {                             ",
                 "                                                   ",
                 "   public String sayHello() {                      ",
                 "       return \"Hello World\";                     ",
                 "   }                                               ",
                 "}                                                  "
         });
 
         ClassLoader classLoader = buildClassLoader("/target/test-classes");
 
         // .. and try to load it afterwards ..
         Class firstGreeterClass = classLoader.loadClass("org.apache.myfaces.extensions.scripting.Greeter");
         assertNotNull(firstGreeterClass);
         assertEquals("org.apache.myfaces.extensions.scripting.Greeter", firstGreeterClass.getName());
 
         // .. more than once using the same classloader.
         Class secondGreeterClass = classLoader.loadClass("org.apache.myfaces.extensions.scripting.Greeter");
         assertNotNull(secondGreeterClass);
         assertEquals("org.apache.myfaces.extensions.scripting.Greeter", secondGreeterClass.getName());
 
         assertSame(firstGreeterClass, secondGreeterClass);
     }
 
     public void testRecompileAndLoadFile() throws Exception {
         // Compile a dynamically generated class.
         compileFile("/src/main/java", "/target/test-classes", "org/apache/myfaces/extensions/scripting/Greeter.java", new String[]{
                 "package org.apache.myfaces.extensions.scripting;   ",
                 "                                                   ",
                 "public class Greeter {                             ",
                 "                                                   ",
                 "   public String sayHello() {                      ",
                 "       return \"Hello World\";                     ",
                 "   }                                               ",
                 "}                                                  "
         });
 
         ClassLoader classLoader = buildClassLoader("/target/test-classes");
 
         Class firstGreeterClass = classLoader.loadClass("org.apache.myfaces.extensions.scripting.Greeter");
         assertNotNull(firstGreeterClass);
         assertEquals("org.apache.myfaces.extensions.scripting.Greeter", firstGreeterClass.getName());
 
         Object firstGreeter = firstGreeterClass.newInstance();
         String greeting = (String) firstGreeterClass.getMethod("sayHello").invoke(firstGreeter);
         assertEquals("Hello World", greeting);
 
         Thread.sleep(1000);
 
         // Modify the source code
         // Compile a dynamically generated class ..
         compileFile("/src/main/java", "/target/test-classes", "org/apache/myfaces/extensions/scripting/Greeter.java", new String[]{
                 "package org.apache.myfaces.extensions.scripting;   ",
                 "                                                   ",
                 "public class Greeter {                             ",
                 "                                                   ",
                 "   public String sayHello() {                      ",
                 "       return \"Hello Universe\";                  ",
                 "   }                                               ",
                 "}                                                  "
         });
 
         // Reload the class and verify it again
         Class secondGreeterClass = classLoader.loadClass("org.apache.myfaces.extensions.scripting.Greeter");
         assertNotNull(secondGreeterClass);
         assertEquals("org.apache.myfaces.extensions.scripting.Greeter", secondGreeterClass.getName());
 
         // However, it has to be a new Class object
         assertNotSame(firstGreeterClass, secondGreeterClass);
 
         Object secondGreeter = secondGreeterClass.newInstance();
         greeting = (String) secondGreeterClass.getMethod("sayHello").invoke(secondGreeter);
         assertEquals("Hello Universe", greeting);
     }
 
     public void testOverrideFile() throws Exception {
         ClassLoader classLoader = buildClassLoader("/target/test-classes");
         Greeter greeter = (Greeter) classLoader.loadClass(
                 "org.apache.myfaces.extensions.scripting.loader.DefaultGreeter").newInstance();
         assertEquals("Hello", greeter.sayHello());
 
         // Compile a dynamically generated class.
         compileFile("/src/main/java", "/target/test-classes", "org/apache/myfaces/extensions/scripting/loader/DefaultGreeter.java", new String[]{
                 "package org.apache.myfaces.extensions.scripting.loader;    ",
                 "                                                           ",
                 "public class DefaultGreeter implements Greeter {           ",
                 "                                                           ",
                 "   public String sayHello() {                              ",
                 "       return \"Hi\";                                      ",
                 "   }                                                       ",
                 "}                                                          "
         });
 
         greeter = (Greeter) classLoader.loadClass(
                 "org.apache.myfaces.extensions.scripting.loader.DefaultGreeter").newInstance();
         assertEquals("Hi", greeter.sayHello());
     }
 
     public void testOverrideFileAndCheckDependentClass() throws Exception {
         ReloadingClassLoader classLoader = buildClassLoader("/target/test-classes");
         PersonGreeter greeter = (PersonGreeter) classLoader.loadClass(
                 "org.apache.myfaces.extensions.scripting.loader.DefaultPersonGreeter").newInstance();
         assertEquals("Hello John Doe!", greeter.sayHello("John Doe"));
 
         // Compile a dynamically generated class.
         compileFile("/src/main/java", "/target/test-classes", "org/apache/myfaces/extensions/scripting/loader/DefaultGreeter.java", new String[]{
                 "package org.apache.myfaces.extensions.scripting.loader;    ",
                 "                                                           ",
                 "public class DefaultGreeter implements Greeter {           ",
                 "                                                           ",
                 "   public String sayHello() {                              ",
                 "       return \"Hi\";                                      ",
                 "   }                                                       ",
                 "}                                                          "
         });
 
         // Note that the person greeter still uses the outdated version of the class file.
         greeter = (PersonGreeter) classLoader.loadClass(
                 "org.apache.myfaces.extensions.scripting.loader.DefaultPersonGreeter").newInstance();
         assertEquals("Hello John Doe!", greeter.sayHello("John Doe"));
 
         // However, now we're forcefully reloading this class in order to reflect the changes.
         classLoader.reloadClass("org.apache.myfaces.extensions.scripting.loader.DefaultPersonGreeter");
 
         // Note that the person greeter still uses the outdated version of the class file.
         greeter = (PersonGreeter) classLoader.loadClass(
                 "org.apache.myfaces.extensions.scripting.loader.DefaultPersonGreeter").newInstance();
         assertEquals("Hi John Doe!", greeter.sayHello("John Doe"));
     }
 
     public void testCompileAndLoadFileWithDependencies() throws Exception {
         compileFile("/src/main/java", "/target/test-classes", "org/apache/myfaces/extensions/scripting/DummyCompiler.java", new String[]{
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
 
         // Create a new class loader to load the dynamically compiled class
         ClassLoader classLoader = buildClassLoader("/target/test-classes");
 
         // Load the dynamically compiled class
         Class dummyCompilerClass = classLoader.loadClass("org.apache.myfaces.extensions.scripting.DummyCompiler");
         assertNotNull(dummyCompilerClass);
         assertEquals("org.apache.myfaces.extensions.scripting.DummyCompiler", dummyCompilerClass.getName());
     }
 
     public void testLoadParentClassFile() throws Exception {
         ClassLoader classLoader = buildClassLoader("/target/test-classes");
 
         Class compilerInterface = classLoader.loadClass("org.apache.myfaces.extensions.scripting.compiler.Compiler");
         assertNotNull(compilerInterface);
         assertEquals("org.apache.myfaces.extensions.scripting.compiler.Compiler", compilerInterface.getName());
     }
 
     public void testOutdatedCheck() throws Exception {
         // Compile a dynamically generated class ..
         compileFile("/src/main/java", "/target/test-classes", "org/apache/myfaces/extensions/scripting/Greeter.java", new String[]{
                 "package org.apache.myfaces.extensions.scripting;   ",
                 "                                                   ",
                 "public class Greeter {                             ",
                 "                                                   ",
                 "   public String sayHello() {                      ",
                 "       return \"Hello World\";                     ",
                 "   }                                               ",
                 "}                                                  "
         });
 
         ReloadingClassLoader classLoader = buildClassLoader("/target/test-classes");
 
         Class firstGreeterClass = classLoader.loadClass("org.apache.myfaces.extensions.scripting.Greeter");
         assertNotNull(firstGreeterClass);
         assertEquals("org.apache.myfaces.extensions.scripting.Greeter", firstGreeterClass.getName());
 
         assertFalse(classLoader.isOutdated(firstGreeterClass));
 
         Thread.sleep(1000);
 
         // Modify the source code ..
         compileFile("/src/main/java", "/target/test-classes", "org/apache/myfaces/extensions/scripting/Greeter.java", new String[]{
                 "package org.apache.myfaces.extensions.scripting;   ",
                 "                                                   ",
                 "public class Greeter {                             ",
                 "                                                   ",
                 "   public String sayHello() {                      ",
                 "       return \"Hello Universe\";                  ",
                 "   }                                               ",
                 "}                                                  "
         });
 
         // .. and test whether the classloader recognizes that we've got an outdated Class reference here.
         assertTrue(classLoader.isOutdated(firstGreeterClass));
     }
 
     // ------------------------------------------ Utility methods
 
     protected ReloadingClassLoader buildClassLoader(String compilationDirectory) {
         return new ReloadingClassLoader(getClass().getClassLoader(),
                     new File(buildAbsolutePath(compilationDirectory)));
     }
 
 }
