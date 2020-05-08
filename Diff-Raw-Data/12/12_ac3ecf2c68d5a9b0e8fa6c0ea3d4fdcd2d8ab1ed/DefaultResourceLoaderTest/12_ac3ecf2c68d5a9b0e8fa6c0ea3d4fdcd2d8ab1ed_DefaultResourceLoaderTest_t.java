 /**
  *    Copyright 2012 MegaFon
  *
  *    Licensed under the Apache License, Version 2.0 (the "License");
  *    you may not use this file except in compliance with the License.
  *    You may obtain a copy of the License at
  *
  *        http://www.apache.org/licenses/LICENSE-2.0
  *
  *    Unless required by applicable law or agreed to in writing, software
  *    distributed under the License is distributed on an "AS IS" BASIS,
  *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *    See the License for the specific language governing permissions and
  *    limitations under the License.
  */
 package ru.histone.resourceloaders;
 
 import org.junit.Before;
 import org.junit.Rule;
 import org.junit.Test;
 import org.junit.rules.ExpectedException;
 import ru.histone.utils.IOUtils;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.URISyntaxException;
 import java.net.URL;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.matchers.JUnitMatchers.containsString;
 
 public class DefaultResourceLoaderTest {
     private ResourceLoader resourceLoader;
     private String baseHref;
 
     @Rule
     public ExpectedException thrown = ExpectedException.none();
 
     @Before
     public void before() throws URISyntaxException {
         resourceLoader = new DefaultResourceLoader();
 
         URL url = getClass().getClassLoader().getResource("resourceloader/test.txt");
        baseHref = "file:/" + new File(url.toURI()).getParent() + "/";
        //baseHref = baseHref.replace("\\","/");
     }
 
     @Test
     public void testSuccessfulLoad() throws IOException {
         Resource resource = resourceLoader.load("test.txt", baseHref);
         assertNotNull(resource);
 
         String content = IOUtils.toString(resource.getInputStream());
         assertEquals("test content", content);
     }
 
     @Test
     public void testWrongFileLocation() throws IOException {
         thrown.expect(ResourceLoadException.class);
         thrown.expectMessage(containsString("Can't read file"));
         resourceLoader.load("unknown.txt", baseHref);
     }
 
     @Test
     public void testBaseLocation() throws IOException {
         thrown.expect(ResourceLoadException.class);
         thrown.expectMessage(containsString("Can't read file"));
         resourceLoader.load("test.txt", "file:/tmp/unknown/");
     }
 
     @Test
     public void testNullBaseLocation() throws IOException {
         thrown.expect(ResourceLoadException.class);
         thrown.expectMessage(containsString("Base HREF is empty and resource location is not absolute!"));
         resourceLoader.load("test.txt", null);
     }
 
 
 }
