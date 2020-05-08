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
 package org.miloframework.tests;
 
 import java.util.Arrays;
 import java.util.Set;
 import javax.servlet.ServletException;
 
 import org.miloframework.MiloServletContext;
 import org.miloframework.ServletContainer;
 import org.testng.Assert;
 import org.testng.annotations.Test;
 
 @Test(dataProvider = "containers")
 public class ContextTest extends MiloTestBase {
    private static final String CONTEXT_ROOT = "../resourcePaths/target/resourcePaths";
 
     public void getResourcePaths(ServletContainer container) throws ServletException {
         final MiloServletContext context = container.createContext("ROOT", "/", CONTEXT_ROOT);
         validate(context.getResourcePaths("/"), "/welcome.html", "/catalog/", "/customer/", "/META-INF/", "/WEB-INF/");
         validate(context.getResourcePaths("/catalog/"), "/catalog/index.html", "/catalog/products.html",
             "/catalog/offers/", "/catalog/moreOffers/");
         Assert.assertNull(context.getResourcePaths(null));
     }
 
     @Test(dataProvider = "containers", expectedExceptions = {IllegalArgumentException.class})
     public void badPath(ServletContainer container) throws ServletException {
         final MiloServletContext context = container.createContext("ROOT", "/", CONTEXT_ROOT);
         Assert.assertNull(context.getResourcePaths("test-classes/com/.."));
     }
 
     private void validate(Set<String> paths, String... expected) {
         Assert.assertEquals(paths.size(), expected.length, String.format("expected %s but got %s",
             Arrays.toString(expected), paths));
         for (String path : expected) {
             Assert.assertTrue(paths.contains(path), String.format("Looking for %s in %s", path, paths));
         }
     }
 }
