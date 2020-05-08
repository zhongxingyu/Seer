 /*
  * Copyright 2010 Alibaba Group Holding Limited.
  * All rights reserved.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *    http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  *
  */
 package com.alibaba.citrus.service.requestcontext.parser;
 
 import static com.alibaba.citrus.test.TestEnvStatic.*;
 import static com.alibaba.citrus.util.CollectionUtil.*;
 import static org.easymock.EasyMock.*;
 import static org.easymock.classextension.EasyMock.*;
 import static org.junit.Assert.*;
 
 import java.io.File;
 import java.net.URLEncoder;
 import java.util.Enumeration;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.commons.fileupload.FileItem;
 import org.easymock.classextension.EasyMock;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 import com.alibaba.citrus.service.requestcontext.AbstractRequestContextsTests;
 import com.alibaba.citrus.util.i18n.LocaleUtil;
 
 /**
  * <code>ParserRequestContext</code>
  */
 public class ParserRequestContextTests extends AbstractRequestContextsTests<ParserRequestContext> {
     @BeforeClass
     public static void initFactory() {
         createBeanFactory("services-parser.xml");
     }
 
     @Before
     public void init() throws Exception {
         invokeReadFileServlet("form.html");
         initRequestContext();
 
         // thread context룬Աurl encodingȷִ
         LocaleUtil.setContext(null, "GBK");
     }
 
     @Test
     public void urlEncodeForm() throws Exception {
         invokeReadFileServlet("form2.html");
         initRequestContext();
 
         assertEquals("hello", requestContext.getParameters().getString("myparam"));
         assertEquals("hello", requestContext.getParameters().getStrings("myparam")[0]);
         assertEquals("л񹲺͹", requestContext.getParameters().getStrings("myparam")[1]);
 
         // requestӿֱȡֵ
         assertEquals("hello", newRequest.getParameter("myparam"));
         assertEquals("hello", newRequest.getParameterValues("myparam")[0]);
         assertEquals("л񹲺͹", newRequest.getParameterValues("myparam")[1]);
     }
 
     @Test
     public void multipartForm() throws Exception {
         assertEquals("hello", requestContext.getParameters().getString("myparam"));
 
         // ȡõfile item
         FileItem fileItem = requestContext.getParameters().getFileItem("myfile");
 
         assertEquals("myfile", fileItem.getFieldName());
         assertEquals(new File(srcdir, "smallfile.txt"), new File(fileItem.getName()));
         assertFalse(fileItem.isFormField());
         assertEquals(new String("л񹲺͹".getBytes("GBK"), "8859_1"), fileItem.getString());
         assertEquals("л񹲺͹", fileItem.getString("GBK"));
         assertTrue(fileItem.isInMemory());
 
         // ȡöfile items
         FileItem[] fileItems = requestContext.getParameters().getFileItems("myfile");
         String[] fileNames = requestContext.getParameters().getStrings("myfile");
 
         assertEquals(fileItems.length, fileNames.length);
         assertEquals(4, fileNames.length);
 
         assertEquals(new File(srcdir, "smallfile.txt"), new File(fileItems[0].getName()));
         assertEquals(new File(srcdir, "smallfile_.JPG"), new File(fileItems[1].getName())); // case insensitive
         assertEquals(new File(srcdir, "smallfile.gif"), new File(fileItems[2].getName()));
         assertEquals(new File(srcdir, "smallfile"), new File(fileItems[3].getName()));
 
         assertEquals(new File(srcdir, "smallfile.txt"), new File(fileNames[0]));
         assertEquals(new File(srcdir, "smallfile_.JPG"), new File(fileNames[1])); // case insensitive
         assertEquals(new File(srcdir, "smallfile.gif"), new File(fileNames[2]));
         assertEquals(new File(srcdir, "smallfile"), new File(fileNames[3]));
 
         // requestӿֱȡֵ
         assertEquals("hello", newRequest.getParameter("myparam"));
         assertEquals(new File(srcdir, "smallfile.txt"), new File(newRequest.getParameter("myfile")));
     }
 
     @Test
     public void uploaded_file_whitelist() throws Exception {
         invokeReadFileServlet("form.html");
         initRequestContext("parser_whitelist");
 
         // ȡöfile items
         FileItem[] fileItems = requestContext.getParameters().getFileItems("myfile");
         String[] fileNames = requestContext.getParameters().getStrings("myfile");
 
        assertEquals(fileItems.length, fileNames.length);
         assertEquals(4, fileNames.length);
 
        assertNull(fileItems[0]);
        assertEquals(new File(srcdir, "smallfile_.JPG"), new File(fileItems[1].getName())); // case insensitive
        assertEquals(new File(srcdir, "smallfile.gif"), new File(fileItems[2].getName()));
        assertNull(fileItems[3]);
 
         assertEquals("", fileNames[0]);
         assertEquals(new File(srcdir, "smallfile_.JPG"), new File(fileNames[1])); // case insensitive
         assertEquals(new File(srcdir, "smallfile.gif"), new File(fileNames[2]));
         assertEquals("", fileNames[3]);
     }
 
     @Test
     public void uploaded_file_whitelist2() throws Exception {
         invokeReadFileServlet("form.html");
         initRequestContext("parser_whitelist2");
 
         // ȡöfile items
         FileItem[] fileItems = requestContext.getParameters().getFileItems("myfile");
         String[] fileNames = requestContext.getParameters().getStrings("myfile");
 
        assertEquals(fileItems.length, fileNames.length);
         assertEquals(4, fileNames.length);
 
         assertEquals(new File(srcdir, "smallfile.txt"), new File(fileItems[0].getName()));
        assertNull(fileItems[1]);
        assertNull(fileItems[2]);
        assertEquals(new File(srcdir, "smallfile"), new File(fileItems[3].getName()));
 
         assertEquals(new File(srcdir, "smallfile.txt"), new File(fileNames[0]));
         assertEquals("", fileNames[1]);
         assertEquals("", fileNames[2]);
         assertEquals(new File(srcdir, "smallfile"), new File(fileNames[3]));
     }
 
     @Test
     public void cookies() throws Exception {
         assertEquals("mycookievalue", requestContext.getCookies().getString("mycookie"));
 
         requestContext.getCookies().setCookie("hello", "baobao");
 
         commitToClient();
 
         assertEquals("baobao", clientResponse.getNewCookieValue("hello"));
     }
 
     @Test
     @SuppressWarnings("unchecked")
     public void getParameterNames() {
         List<String> keys = createArrayList();
 
         for (Enumeration<String> e = newRequest.getParameterNames(); e.hasMoreElements();) {
             keys.add(e.nextElement());
         }
 
         assertArrayEquals(requestContext.getParameters().keySet().toArray(new String[0]), keys.toArray(new String[0]));
     }
 
     @Test
     @SuppressWarnings({ "unchecked", "rawtypes" })
     public void getRequestMap() throws Exception {
         Map paramMap = newRequest.getParameterMap();
 
         // unmodifiable
         try {
             paramMap.put("test", "fail");
             fail("UnsupportedOperationException expected");
         } catch (UnsupportedOperationException e) {
         }
 
         try {
             paramMap.remove("myparam");
             fail("UnsupportedOperationException expected");
         } catch (UnsupportedOperationException e) {
         }
 
         try {
             ((Map.Entry) paramMap.entrySet().iterator().next()).setValue(null);
             fail("UnsupportedOperationException expected");
         } catch (UnsupportedOperationException e) {
         }
 
         // containsKey
         assertTrue(paramMap.containsKey("myparam"));
         assertFalse(paramMap.containsKey(new Object())); // not a String key
 
         // get
         assertNull(paramMap.get(new Object())); // not a String key
 
         String[] myparam = (String[]) paramMap.get("myparam");
         String[] myfile = (String[]) paramMap.get("myfile");
         String[] submit = (String[]) paramMap.get("submit");
 
         assertEquals("hello", myparam[0]);
         assertEquals("л񹲺͹", myparam[1]);
 
         assertEquals(new File(srcdir, "smallfile.txt"), new File(myfile[0]));
 
         assertEquals("upload", submit[0]);
 
         // keySet
         assertArrayEquals(requestContext.getParameters().keySet().toArray(new String[0]),
                 paramMap.keySet().toArray(new String[0]));
 
         // values
         String[][] values = new String[requestContext.getParameters().size()][];
 
         int i = 0;
         for (String key : requestContext.getParameters().keySet()) {
             values[i++] = requestContext.getParameters().getStrings(key);
         }
 
         assertArrayEquals(values, paramMap.values().toArray(new String[0][]));
     }
 
     @Test
     public void toQueryString() throws Exception {
         ParameterParser params = requestContext.getParameters();
 
         assertEquals("myparam=hello&myparam=%D6%D0%BB%AA%C8%CB%C3%F1%B9%B2%BA%CD%B9%FA&submit=upload",
                 params.toQueryString());
 
         params.setStrings("array", new String[] { "11", "22", "33" });
 
         assertEquals(
                 "myparam=hello&myparam=%D6%D0%BB%AA%C8%CB%C3%F1%B9%B2%BA%CD%B9%FA&submit=upload&array=11&array=22&array=33",
                 params.toQueryString());
     }
 
     @Test
     public void parse_post() throws Exception {
         // post/putУform data
         Map<String, String[]> engineDecodedParams = createLinkedHashMap();
         engineDecodedParams.put("a", new String[] { "2" });
 
         initMockRequest("POST", "a=1", null, engineDecodedParams);
         initRequestContext();
         assertTrue(requestContext.isUseBodyEncodingForURI());
         assertArrayEquals(new String[] { "2" }, newRequest.getParameterValues("a"));
 
         initMockRequest("PUT", "a=1", null, engineDecodedParams);
         initRequestContext();
         assertTrue(requestContext.isUseBodyEncodingForURI());
         assertArrayEquals(new String[] { "2" }, newRequest.getParameterValues("a"));
     }
 
     @Test
     public void parse_get() throws Exception {
         String utf8_й = URLEncoder.encode("й", "UTF-8");
         String gbk_й = URLEncoder.encode("й", "GBK");
 
         String utf8_й_raw = new String("й".getBytes("UTF-8"), "8859_1"); // ģieΪurlencoding
         String gbk_й_raw = new String("й".getBytes("GBK"), "8859_1"); // ģieΪurlencoding
 
         Map<String, String[]> engineDecodedParams = createLinkedHashMap();
         engineDecodedParams.put("a", new String[] { "China" });
 
         // get/headУнquery
         // useBodyEncodingForURI=trueʹָcharset
         initMockRequest("GET", "a=" + gbk_й, "GBK", null);
         initRequestContext();
         assertFalse(requestContext.isUseServletEngineParser());
         assertTrue(requestContext.isUseBodyEncodingForURI());
         assertArrayEquals(new String[] { "й" }, newRequest.getParameterValues("a"));
 
         initMockRequest("HEAD", gbk_й + "=1", "GBK", null);
         initRequestContext();
         assertFalse(requestContext.isUseServletEngineParser());
         assertTrue(requestContext.isUseBodyEncodingForURI());
         assertArrayEquals(new String[] { "1" }, newRequest.getParameterValues("й"));
 
         initMockRequest("GET", "a=" + gbk_й_raw, "GBK", null);
         initRequestContext();
         assertFalse(requestContext.isUseServletEngineParser());
         assertTrue(requestContext.isUseBodyEncodingForURI());
         assertArrayEquals(new String[] { "й" }, newRequest.getParameterValues("a"));
 
         initMockRequest("HEAD", gbk_й_raw + "=1", "GBK", null);
         initRequestContext();
         assertFalse(requestContext.isUseServletEngineParser());
         assertTrue(requestContext.isUseBodyEncodingForURI());
         assertArrayEquals(new String[] { "1" }, newRequest.getParameterValues("й"));
 
         // get/headУȷuseServletEngineParserʱuseBodyEncodingForURIЧ
         initMockRequest("GET", null, null, engineDecodedParams);
         initRequestContext("parser_useServletEngineParser");
         assertTrue(requestContext.isUseServletEngineParser());
         assertTrue(requestContext.isUseBodyEncodingForURI());
         assertArrayEquals(new String[] { "China" }, newRequest.getParameterValues("a"));
 
         // get/headУuseBodyEncodingForURI=falseʹutf-8
         initMockRequest("GET", "a=" + utf8_й, "GBK", null);
         initRequestContext("parser_dont_useBodyEncodingForURI");
         assertFalse(requestContext.isUseServletEngineParser());
         assertFalse(requestContext.isUseBodyEncodingForURI());
         assertEquals("UTF-8", requestContext.getURIEncoding());
         assertArrayEquals(new String[] { "й" }, newRequest.getParameterValues("a"));
 
         initMockRequest("GET", "a=" + utf8_й_raw, "GBK", null);
         initRequestContext("parser_dont_useBodyEncodingForURI");
         assertFalse(requestContext.isUseServletEngineParser());
         assertFalse(requestContext.isUseBodyEncodingForURI());
         assertEquals("UTF-8", requestContext.getURIEncoding());
         assertArrayEquals(new String[] { "й" }, newRequest.getParameterValues("a"));
 
         // get/headУuseBodyEncodingForURI=trueδָcharsetʹISO-8859-1
         initMockRequest("GET", "a=" + gbk_й, null, null);
         initRequestContext();
         assertFalse(requestContext.isUseServletEngineParser());
         assertTrue(requestContext.isUseBodyEncodingForURI());
         assertArrayEquals(new String[] { new String("й".getBytes("GBK"), "8859_1") },
                 newRequest.getParameterValues("a"));
     }
 
     @Test
     public void parse_query() throws Exception {
         // ֵ
         initMockRequest("GET", "  &a=1&a=2&b=3& ", "GBK", null);
         initRequestContext();
         assertArrayEquals(new String[] { "1", "2" }, newRequest.getParameterValues("a"));
         assertArrayEquals(new String[] { "3" }, newRequest.getParameterValues("b"));
         assertArrayEquals(new Object[] { "a", "b" }, newRequest.getParameterMap().keySet().toArray());
 
         // ֵ
         initMockRequest("GET", "a", "GBK", null);
         initRequestContext();
         assertArrayEquals(new String[] {}, newRequest.getParameterValues("a"));
         assertArrayEquals(new Object[] { "a" }, newRequest.getParameterMap().keySet().toArray());
 
         initMockRequest("GET", "a&b=&=1", "GBK", null);
         initRequestContext();
         assertArrayEquals(new String[] {}, newRequest.getParameterValues("a"));
         assertArrayEquals(new String[] {}, newRequest.getParameterValues("b"));
         assertArrayEquals(new Object[] { "a", "b" }, newRequest.getParameterMap().keySet().toArray());
     }
 
     @Test
     public void parse_trimming() throws Exception {
         initMockRequest("GET", "  &a=++&a=+2+&b=+3+& ", "GBK", null);
         initRequestContext();
         assertArrayEquals(new String[] { "", "2" }, newRequest.getParameterValues("a"));
         assertArrayEquals(new String[] { "3" }, newRequest.getParameterValues("b"));
         assertArrayEquals(new Object[] { "a", "b" }, newRequest.getParameterMap().keySet().toArray());
     }
 
     private void initMockRequest(String method, String queryString, String charset,
                                  Map<String, String[]> engineDecodedParams) {
         request = createMock(HttpServletRequest.class);
         response = createMock(HttpServletResponse.class);
 
         request.setAttribute(EasyMock.<String> anyObject(), anyObject());
         expectLastCall().anyTimes();
 
         expect(request.getLocale()).andReturn(Locale.CHINA).anyTimes();
         expect(request.getContentType()).andReturn(null).anyTimes();
         expect(request.getMethod()).andReturn(method).anyTimes();
         expect(request.getQueryString()).andReturn(queryString).anyTimes();
         expect(request.getCharacterEncoding()).andReturn(charset).anyTimes();
 
         if (engineDecodedParams != null) {
             expect(request.getParameterMap()).andReturn(engineDecodedParams).anyTimes();
         }
 
         replay(request, response);
     }
 }
