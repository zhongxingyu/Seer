 /* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
  * This code is licensed under the GPL 2.0 license, availible at the root
  * application directory.
  */
 package org.geoserver.filters;
 
 import javax.servlet.http.HttpServletRequestWrapper;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.ServletInputStream;
 import java.io.Reader;
 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 import java.io.IOException;
 
 import java.lang.reflect.Method;
 import java.util.Collection;
 import java.util.Iterator;
 
 import org.geoserver.test.GeoServerTestSupport;
 
 import com.mockrunner.mock.web.MockHttpServletRequest;
 import com.mockrunner.mock.web.MockHttpServletResponse;
 import com.mockrunner.mock.web.MockHttpSession;
 import com.mockrunner.mock.web.MockServletContext;
 
 public class BufferedRequestWrapperTest extends GeoServerTestSupport{
 
 	protected final String[] testStrings = new String[]{
 		"Hello, this is a test",
 		"LongLongLongLongLongLongLongLongLongLongLongLongLongLongLongLong",
 		""
 	};
 
 	protected HttpServletRequest makeRequest(String body){
 		MockHttpServletRequest request = new MockHttpServletRequest();
         request.setScheme("http");
         request.setServerName("localhost");
         request.setContextPath("/geoserver");
         request.setRequestURI("/geoserver");
         request.setQueryString("");
         request.setRemoteAddr("127.0.0.1");
         request.setServletPath("/geoserver");
 
 		request.setMethod("POST");
 		request.setBodyContent(body);
 
         MockHttpSession session = new MockHttpSession();
         session.setupServletContext(new MockServletContext());
         request.setSession(session);
 
         request.setUserPrincipal(null);
 
 		return request;
 	}
 
 	public void testGetInputStream() throws Exception{
 		for (int i = 0; i < testStrings.length; i++){
 			doInputStreamTest(testStrings[i]);
 		}
 	}
 
 	public void testGetReader() throws Exception{
 	    for (int i = 0; i < testStrings.length; i++){
 			doGetReaderTest(testStrings[i]);
 		}
 	}
 
 	public void doInputStreamTest(String testString) throws Exception{
 		HttpServletRequest req = makeRequest(testString);
 
 		BufferedRequestWrapper wrapper = new BufferedRequestWrapper(req, testString);
 		ServletInputStream sis = req.getInputStream();
 		byte b[] = new byte[32];
 		int amountRead;
 
 		while (( sis.readLine(b, 0, 32)) > 0){ /*clear out the request body*/ }
 
 		sis = wrapper.getInputStream();
 		StringBuffer buff = new StringBuffer();
 
 		while ((amountRead = sis.readLine(b, 0, 32)) != 0){
 			buff.append(new String(b, 0, amountRead));
 		}
 
 		assertEquals(buff.toString(), testString);
 		//compare(req, wrapper);
 	}
 
     public void doGetReaderTest(String testString) throws Exception{
 		HttpServletRequest req = makeRequest(testString);
 
 		BufferedReader br = req.getReader();
 		String line;
 
 		while ((br.readLine()) != null){ /* clear out the body */ }
 
 		BufferedRequestWrapper wrapper = new BufferedRequestWrapper(req, testString);
 		StringBuffer buff = new StringBuffer();
 		br = wrapper.getReader();
 		
 		while ((line = br.readLine()) != null){
 			buff.append(line);
 		}
 
 		assertEquals(buff.toString(), testString);
 		//compare(req, wrapper);
 	}
 	
 	public static void compare(HttpServletRequest reqA, HttpServletRequest reqB){
 		Method[] methods = HttpServletRequest.class.getMethods();
 
 		for (int i = 0; i < methods.length; i++){
 			try {
 				if (methods[i].getParameterTypes().length == 0){
 					Object resultA = methods[i].invoke(reqA);
 					Object resultB = methods[i].invoke(reqB);
		            assertEquals(resultA, resultB);
 				} 
 			} catch (Exception e){
 				// don't do anything, it's fine
 			}
 		}
 	}
 }
