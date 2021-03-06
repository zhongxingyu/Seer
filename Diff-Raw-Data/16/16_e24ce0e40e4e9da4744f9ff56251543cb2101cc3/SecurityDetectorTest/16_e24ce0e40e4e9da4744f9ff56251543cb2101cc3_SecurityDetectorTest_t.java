 /*
  * Copyright (C) 2011 The Android Open Source Project
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
 public class SecurityDetectorTest extends AbstractCheckTest {
     @Override
     protected Detector getDetector() {
         return new SecurityDetector();
     }
 
     public void testBroken() throws Exception {
         assertEquals(
             "AndroidManifest.xml:12: Warning: Exported service does not require permission",
             lintProject(
                     "exportservice1.xml=>AndroidManifest.xml",
                     "res/values/strings.xml"));
     }
 
     public void testBroken2() throws Exception {
         assertEquals(
             "AndroidManifest.xml:12: Warning: Exported service does not require permission",
             lintProject(
                     "exportservice1.xml=>AndroidManifest.xml",
                     "res/values/strings.xml"));
     }
 
    public void testBroken3() throws Exception {
        // Not defining exported, but have intent-filters
        assertEquals(
            "AndroidManifest.xml:12: Warning: Exported service does not require permission",
            lintProject(
                    "exportservice5.xml=>AndroidManifest.xml",
                    "res/values/strings.xml"));
    }

     public void testOk1() throws Exception {
         // Defines a permission on the <service> element
         assertEquals(
             "No warnings.",
             lintProject(
                     "exportservice3.xml=>AndroidManifest.xml",
                     "res/values/strings.xml"));
     }
 
     public void testOk2() throws Exception {
         // Defines a permission on the parent <application> element
         assertEquals(
             "No warnings.",
             lintProject(
                     "exportservice3.xml=>AndroidManifest.xml",
                     "res/values/strings.xml"));
     }
 
     public void testUri() throws Exception {
         assertEquals(
             "AndroidManifest.xml:24: Warning: Content provider shares everything; this is potentially dangerous.\n" +
             "AndroidManifest.xml:25: Warning: Content provider shares everything; this is potentially dangerous.",
 
             lintProject(
                     "grantpermission.xml=>AndroidManifest.xml",
                     "res/values/strings.xml"));
     }
 
     public void testWorldWriteable() throws Exception {
         assertEquals(
             "WorldWriteableFile.java:20: Warning: Using MODE_WORLD_WRITEABLE with openFileOutput can be risky, review carefully",
 
             lintProject(
                 // Java files must be renamed in source tree
                 "src/test/pkg/WorldWriteableFile.java.txt=>src/test/pkg/WorldWriteableFile.java"));
     }
 }
