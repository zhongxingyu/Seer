 /*
  * $Id$
  * 
  * Copyright 2009 Hiroki Ata
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
 package org.aexlib.gae;
 
 import java.io.File;
 
import org.aexlib.gae.tool.TestEnvironment;

 import com.google.appengine.tools.development.ApiProxyLocalImpl;
 import com.google.apphosting.api.ApiProxy;
 
 import junit.framework.TestCase;
 
 public class GAEBaseTestCase extends TestCase {
     @Override
     protected void setUp() throws Exception {
         super.setUp();
         ApiProxy.setEnvironmentForCurrentThread(new TestEnvironment());
         ApiProxy.setDelegate(new ApiProxyLocalImpl(new File("target")){});
     }
 
     @Override
     protected void tearDown() throws Exception {
         // not strictly necessary to null these out but there's no harm either
         ApiProxy.setDelegate(null);
         ApiProxy.setEnvironmentForCurrentThread(null);
         super.tearDown();
     }
 
     public void testDummy() {
         
     }
 }
