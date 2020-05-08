 /**
  * Copyright 2013 ArcBees Inc.
  *
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
  * use this file except in compliance with the License. You may obtain a copy of
  * the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations under
  * the License.
  */
 
 package com.arcbees.plugin.template.utils;
 
 import java.util.Map;
 import java.util.Set;
 
 import org.junit.Assert;
 import org.junit.Test;
 
 public class FetchTemplatesTest {
     private String BASE = "https://raw.github.com/ArcBees/IDE-Templates/1.0.0/src/main/resources/com/arcbees/plugin/template/presenter/nested";
     @Test
     public void testGettingTemlateFiles() {
         String dir = "target/test";
         FetchTemplates fetchTemplates = new FetchTemplates(dir);
         fetchTemplates.addPath(BASE + "/__name__Module.java.vm");
         fetchTemplates.addPath(BASE + "/__name__Presenter.java.vm");
         fetchTemplates.addPath(BASE + "/__name__UiHandlers.java.vm");
         fetchTemplates.addPath(BASE + "/__name__View.java.vm");
         fetchTemplates.addPath(BASE + "/__name__View.ui.xml.vm");
         
         fetchTemplates.run();
         
         Map<String, FetchTemplate> fetched = fetchTemplates.getPathsToFetch();
         Set<String> paths = fetched.keySet();
         for (String path : paths) {
            System.out.println("fetched=" + fetched.get(path).getFetched());
             Assert.assertNotNull(fetched.get(path).getFetched());
         }
     }
 }
