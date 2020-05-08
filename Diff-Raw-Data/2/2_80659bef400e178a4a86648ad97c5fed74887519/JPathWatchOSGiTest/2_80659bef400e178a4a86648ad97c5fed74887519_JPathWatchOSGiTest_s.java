 /*
  * Copyright 2012-2013 Steven Swor.
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
 package com.github.sworisbreathing.sfmf4j.osgi.test.impl;
 
 import com.github.sworisbreathing.sfmf4j.osgi.test.AbstractOSGiTest;
 import org.ops4j.pax.exam.Constants;
 import static org.ops4j.pax.exam.CoreOptions.*;
 import org.ops4j.pax.exam.Option;
 import org.ops4j.pax.exam.util.PathUtils;
 
 /**
  *
  * @author sswor
  */
 public class JPathWatchOSGiTest extends AbstractOSGiTest {
 
     @Override
     protected Option implementationOption() {
         return composite(
                wrappedBundle(mavenBundle("jpathwatch", "jpathwatch", "0.95")).startLevel(Constants.START_LEVEL_SYSTEM_BUNDLES),
                 //mavenBundle(sfmf4jGroupId(), "sfmf4j-jpathwatch", sfmf4jVersion()));
                 bundle("reference:file:" + PathUtils.getBaseDir() + "/target/classes"));
     }
 }
