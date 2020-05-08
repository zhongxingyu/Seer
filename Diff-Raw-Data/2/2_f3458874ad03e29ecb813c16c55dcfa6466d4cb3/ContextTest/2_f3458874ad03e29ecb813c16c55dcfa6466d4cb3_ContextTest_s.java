 /**
  * Copyright (C) 2011 Justin Lee <jlee@antwerkz.com>
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *         http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.milo;
 
 import java.io.File;
 import java.util.Set;
 import javax.servlet.ServletException;
 
 import org.testng.Assert;
 import org.testng.annotations.Test;
 
 @Test(dataProvider = "containers")
 public class ContextTest extends MiloTestBase {
    private static final String CONTEXT_ROOT = "../basic/target/basic";
 
     public void getResourcePaths(ServletContainer container) throws ServletException {
         final MiloServletContext context = container.createContext("ROOT", "/", CONTEXT_ROOT);
         validate(context.getResourcePaths("/"));
         validate(context.getResourcePaths("/test-classes/com/.."));
         Assert.assertNull(context.getResourcePaths(null));
     }
 
     @Test(dataProvider = "containers", expectedExceptions = {IllegalArgumentException.class})
     public void badPath(ServletContainer container) throws ServletException {
         final MiloServletContext context = container.createContext("ROOT", "/", CONTEXT_ROOT);
         validate(context.getResourcePaths("test-classes/com/.."));
     }
 
     private void validate(Set<String> paths) {
         Assert.assertFalse(paths.isEmpty());
         for (String path : paths) {
             final File file = new File(path);
             if(file.isDirectory()) {
                 Assert.assertTrue(path.endsWith("/"));
             } else {
                 Assert.assertFalse(path.endsWith("/"));
             }
         }
     }
 }
