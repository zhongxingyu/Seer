 /*
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.google.devtools.j2objc.translate;
 
 import com.google.devtools.j2objc.GenerationTest;
 
 import java.io.IOException;
 
 /**
  * Unit tests for {@link OuterReferenceFixer}.
  *
  * @author Keith Stanger
  */
 public class OuterReferenceFixerTest extends GenerationTest {
 
   public void testSuperConstructorExpression() throws IOException {
     addSourceFile("class A { class Inner { } }", "A.java");
     String translation = translateSourceFile(
         "class B extends A.Inner { B(A a) { a.super(); } }", "B", "B.m");
     assertTranslation(translation, "[super initWithA:a]");
   }

  public void testLocalClassCaptureVariablesInsideGenericClass() throws IOException {
    String translation = translateSourceFile(
        "class Test<T> { void test() { final Object o = null; class Inner { "
        + "public void foo() { o.toString(); } } new Inner(); } }", "Test", "Test.m");
    assertTranslation(translation, "[[Test_test_Inner alloc] initWithId:o]");
  }
 }
