 /*
  * Copyright (c) 2010 Sharegrove Inc.
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
 
 package org.msjs.page;
 
 import static junit.framework.Assert.assertEquals;
import org.easymock.EasyMock;
 import org.jdom.Document;
 import org.jdom.Element;
 import org.jdom.Namespace;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import org.msjs.config.BasicConfiguration;
 import org.msjs.script.ScriptContextTestProvider;
 
 import javax.servlet.http.HttpServletRequest;
 import java.io.FileNotFoundException;
 import java.util.List;
 import java.util.concurrent.Executors;
 
 public class TestPage {
     private static String testRoot;
     private static PageContextProvider provider;
     private static HttpServletRequest mockRequest;
 
     @BeforeClass
     public static void setup() {
         testRoot = "test";
         provider = new PageContextProvider(new ScriptContextTestProvider(),
                 Executors.newSingleThreadExecutor(),
                 BasicConfiguration.getConfiguration());
        mockRequest = EasyMock.createNiceMock(HttpServletRequest.class);
        EasyMock.expect(mockRequest.getRequestURL()).andReturn(new StringBuffer("mocked://test/empty.msjs")).anyTimes();
        EasyMock.expect(mockRequest.getQueryString()).andReturn("").anyTimes();
        EasyMock.replay(mockRequest);

 
     }
 
 
     @Test
     public void basicTest() throws FileNotFoundException {
         Page page = new Page(provider.get(testRoot + "/empty", false));
         Document rendering = page.render(mockRequest);
         assertEquals("html", rendering.getRootElement().getName());
     }
 
     @Test
     public void documentStuff() throws FileNotFoundException {
         Page page = new Page(provider.get(testRoot + "/empty", false));
         Document rendering = page.render(mockRequest);
 
         Element e = rendering.getRootElement();
 
         //there should be two children
 
         List children = e.getChildren();
 
         assertEquals(2, children.size());
         Element head = (Element) children.get(0);
         assertEquals("head", head.getName());
 
         final Namespace ns = Namespace.getNamespace("http://www.w3.org/1999/xhtml");
         Element meta = head.getChild("meta", ns);
         assertEquals("Content-Type", meta.getAttributeValue("http-equiv"));
 
         Element body = (Element) children.get(1);
 
         assertEquals("body", body.getName());
 
     }
 
 }
