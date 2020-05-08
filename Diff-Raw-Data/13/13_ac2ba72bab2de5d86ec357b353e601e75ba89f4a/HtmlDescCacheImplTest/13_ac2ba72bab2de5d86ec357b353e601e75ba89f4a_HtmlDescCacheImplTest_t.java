 /*
  * Copyright 2004-2009 the Seasar Foundation and the Others.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
  * either express or implied. See the License for the specific language
  * governing permissions and limitations under the License.
  */
 package org.seasar.teeda.extension.html.impl;
 
 import java.io.File;
 import java.io.InputStream;
 
 import org.seasar.framework.exception.ResourceNotFoundRuntimeException;
 import org.seasar.framework.mock.servlet.MockServletContextImpl;
 import org.seasar.framework.util.ResourceUtil;
 import org.seasar.teeda.extension.exception.HtmlNotFoundRuntimeExcpetion;
 import org.seasar.teeda.extension.html.HtmlDesc;
 import org.seasar.teeda.extension.html.HtmlParser;
 import org.seasar.teeda.extension.unit.TeedaExtensionTestCase;
 
 /**
  * @author higa
  * @author shot
  */
 public class HtmlDescCacheImplTest extends TeedaExtensionTestCase {
 
     public void testCreateAndGetHtmlDesc() throws Exception {
         String path = convertPath("HtmlNodeHandler.html");
         HtmlDescCacheImpl cache = new HtmlDescCacheImpl();
         HtmlParser htmlParser = getHtmlParser();
         cache.setHtmlParser(htmlParser);
         cache.setServletContext(getServletContext());
         cache.setContainer(getContainer());
         HtmlDesc htmlDesc = cache.createHtmlDesc(path);
         assertNotNull("1", htmlDesc);
         assertFalse("2", htmlDesc.isModified());
         assertEquals("3", "<html><body>Hello</body></html>", htmlDesc
                 .getHtmlNode().toString());
         assertSame("4", htmlDesc, cache.getHtmlDesc(path));
         File file = ResourceUtil.getResourceAsFile(path);
         Thread.sleep(1000);
         file.setLastModified(System.currentTimeMillis());
         assertTrue("5", htmlDesc.isModified());
     }
 
     public void testCreateHtmlDesc_notFound() throws Exception {
         HtmlDescCacheImpl cache = new HtmlDescCacheImpl();
         cache.setServletContext(new MyMockServletContextImpl());
         try {
            cache.createHtmlDesc("<script/>");
             fail();
         } catch (HtmlNotFoundRuntimeExcpetion e) {
             success();
            assertTrue(e.getMessage().indexOf("&lt;script/&gt;") >= 0);
         }
     }
 
     public static class MyMockServletContextImpl extends MockServletContextImpl {
 
         private static final long serialVersionUID = 1L;
 
         public MyMockServletContextImpl() {
             super(null);
         }
 
         public MyMockServletContextImpl(String path) {
             super(path);
         }
 
         public String getRealPath(String path) {
             return null;
         }
 
         public InputStream getResourceAsStream(String path) {
             return null;
         }
 
     }
 }
