 /*
  * Copyright (C) 2012 The Android Open Source Project
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.android.tools.lint.checks;
 
 import com.android.tools.lint.detector.api.Detector;
 
 @SuppressWarnings("javadoc")
 public class OnClickDetectorTest extends AbstractCheckTest {
     @Override
     protected Detector getDetector() {
         return new OnClickDetector();
     }
 
     public void test() throws Exception {
         assertEquals(
             "onclick.xml:10: Error: Corresponding method handler 'public void nonexistent(android.view.View)' not found\n" +
             "onclick.xml:16: Error: Corresponding method handler 'public void wrong1(android.view.View)' not found\n" +
             "onclick.xml:22: Error: Corresponding method handler 'public void wrong2(android.view.View)' not found\n" +
             "onclick.xml:28: Error: Corresponding method handler 'public void wrong3(android.view.View)' not found\n" +
             "onclick.xml:34: Error: Corresponding method handler 'public void wrong4(android.view.View)' not found\n" +
            "onclick.xml:46: Error: Corresponding method handler 'public void simple_typo(android.view.View)' not found (did you mean test.pkg.OnClickActivity#simple_tyop(Landroid/view/View;)V ?)",
 
             lintProject(
                 "bytecode/.classpath=>.classpath",
                 "bytecode/AndroidManifest.xml=>AndroidManifest.xml",
                 "res/layout/onclick.xml=>res/layout/onclick.xml",
                 "bytecode/OnClickActivity.java.txt=>src/test/pkg/OnClickActivity.java",
                 "bytecode/OnClickActivity.class.data=>bin/classes/test/pkg/OnClickActivity.class"
                 ));
     }
 
     public void testOk() throws Exception {
         // No onClick attributes
         assertEquals(
                 "No warnings.",
 
                 lintProject("res/layout/accessibility.xml"));
     }
 
 }
