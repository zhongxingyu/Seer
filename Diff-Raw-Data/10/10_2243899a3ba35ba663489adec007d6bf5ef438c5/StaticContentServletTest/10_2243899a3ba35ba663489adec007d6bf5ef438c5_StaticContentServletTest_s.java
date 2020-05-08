 /* vim: set ts=2 et sw=2 cindent fo=qroca: */
 
 package com.globant.katari.core.web;
 
 import java.util.Hashtable;
 
 import java.io.File;
 import java.io.FileWriter;
 import java.io.StringWriter;
 import java.io.PrintWriter;
 
 import javax.servlet.ServletConfig;
 import javax.servlet.ServletContext;
 import javax.servlet.ServletOutputStream;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.springframework.mock.web.MockHttpServletRequest;
 import org.springframework.mock.web.MockHttpServletResponse;
 import org.springframework.mock.web.MockServletConfig;
 
 import org.junit.Test;
 import org.junit.Before;
 import static org.junit.Assert.*;
 
 import static org.easymock.classextension.EasyMock.*;
 
 public class StaticContentServletTest {
 
   HttpServletRequest request;
 
   ServletConfig config;
 
   @Before
   public void setUp() {
     // Mocks the servlet request to simulate a request for an image..
     request = createMock(HttpServletRequest.class);
     expect(request.getRequestURI()).andReturn(
         "/module/user/test_image.gif");
     expectLastCall().anyTimes();
     expect(request.getRequestURL()).andReturn(new StringBuffer());
     expectLastCall().anyTimes();
     expect(request.getServletPath()).andReturn("/test_image.gif");
     expectLastCall().anyTimes();
     expect(request.getDateHeader("If-Modified-Since")).andReturn(0l);
     expect(request.getContextPath()).andReturn("/katari-web");
     expectLastCall().anyTimes();
     expect(request.getMethod()).andReturn("GET");
     expectLastCall().anyTimes();
     expect(request.getProtocol()).andReturn("http");
     expectLastCall().anyTimes();
     replay(request);
 
     // Creates an enumeration with all the parameter names.
     Hashtable<String, String> parameters = new Hashtable<String, String>();
     parameters.put("staticContentServlet", "");
     parameters.put("mimeType_gif", "");
     parameters.put("mimeType_jpeg", "");
 
     // Mocks the servlet context.
     ServletContext context = createMock(ServletContext.class);
     expect(context.getServletContextName()).andReturn("/module/user");
     expectLastCall().anyTimes();
 
     // Under some conditions, the init method asks context to log the call.
     context.log(isA(String.class));
     expectLastCall().anyTimes();
     replay(context);
 
     // Mocks the servlet config.
     config = createNiceMock(ServletConfig.class);
     expect(config.getServletContext()).andReturn(context);
     expectLastCall().anyTimes();
     expect(config.getInitParameterNames()).andReturn(parameters.keys());
     expect(config.getInitParameter("staticContentPath")).andReturn(
         "com/globant/katari/core/web");
     expectLastCall().anyTimes();
     expect(config.getInitParameter("mimeType_gif")).andReturn(
         "image/gif");
     expectLastCall().anyTimes();
     expect(config.getInitParameter("mimeType_jpeg")).andReturn(
         "image/jpeg");
     expectLastCall().anyTimes();
     expect(config.getInitParameter("requestCacheContent")).andReturn("true");
     expectLastCall().anyTimes();
     expect(config.getServletName()).andReturn("StaticContentServlet");
     expectLastCall().anyTimes();
     replay(config);
   }
 
   /* Tests if service correctly dispatches the request.
    */
   @Test
   public final void testService() throws Exception {
 
     // Mocks the Response's output stream
     ServletOutputStream mockOutputStream =
       createMock(ServletOutputStream.class);
     mockOutputStream.write(isA(byte[].class), eq(0), eq(776));
     mockOutputStream.flush();
     replay(mockOutputStream);
 
     // Mocks the servlet response.
     HttpServletResponse response = createMock(HttpServletResponse.class);
     expect(response.getOutputStream()).andReturn(mockOutputStream);
     expectLastCall().anyTimes();
     response.setContentType("image/gif");
     expectLastCall().anyTimes();
     response.setDateHeader(same("Date"), anyLong());
     expectLastCall().anyTimes();
     response.setDateHeader(same("Expires"), anyLong());
     expectLastCall().anyTimes();
     response.setDateHeader(same("Retry-After"), anyLong());
     expectLastCall().anyTimes();
     response.setHeader(same("Cache-Control"), (String)anyObject());
     expectLastCall().anyTimes();
     response.setDateHeader(same("Last-Modified"), anyLong());
     expectLastCall().anyTimes();
 
     replay(response);
 
     StaticContentServlet staticContentServlet = new StaticContentServlet();
     staticContentServlet.init(config);
     staticContentServlet.service(request, response);
   }
 
   /* Tests the doPost Method.
    */
   @Test
   public final void testDoPost() throws Exception {
     // Mocks the servlet request.
     HttpServletRequest request = createMock(HttpServletRequest.class);
     expect(request.getRequestURI()).andReturn(
         "/module/user/test_image.gif");
     expectLastCall().anyTimes();
     expect(request.getRequestURL()).andReturn(new StringBuffer());
     expectLastCall().anyTimes();
 
     //expect(request.getServletPath()).andReturn("/test_image.gif");
     //expectLastCall().anyTimes();
     expect(request.getServletPath()).andReturn(null);
     expectLastCall().anyTimes();
     expect(request.getPathInfo()).andReturn(null);
     expectLastCall().anyTimes();
 
     expect(request.getDateHeader("If-Modified-Since")).andReturn(0l);
     expect(request.getContextPath()).andReturn("/katari-web");
     expectLastCall().anyTimes();
     expect(request.getMethod()).andReturn("GET");
     expectLastCall().anyTimes();
     expect(request.getProtocol()).andReturn("http");
     expectLastCall().anyTimes();
     replay(request);
 
     // Mocks the Response's output stream
     ServletOutputStream mockOutputStream = createMock(ServletOutputStream.class);
     mockOutputStream.write(isA(byte[].class), eq(0), eq(776));
     mockOutputStream.flush();
     replay(mockOutputStream);
 
     // Mocks the servlet response.
     HttpServletResponse response = createMock(HttpServletResponse.class);
     expect(response.getOutputStream()).andReturn(mockOutputStream);
     expectLastCall().anyTimes();
     response.setContentType("image/gif");
     expectLastCall().anyTimes();
     response.setDateHeader(same("Date"), anyLong());
     expectLastCall().anyTimes();
     response.setDateHeader(same("Expires"), anyLong());
     expectLastCall().anyTimes();
     response.setDateHeader(same("Retry-After"), anyLong());
     expectLastCall().anyTimes();
     response.setHeader(same("Cache-Control"), (String)anyObject());
     expectLastCall().anyTimes();
     response.setDateHeader(same("Last-Modified"), anyLong());
     expectLastCall().anyTimes();
     response.sendError(404);
     StringWriter writer = new StringWriter();
     expect(response.getWriter()).andReturn(new PrintWriter(writer));
     response.flushBuffer();
 
     replay(response);
 
     StaticContentServlet staticContentServlet = new StaticContentServlet();
     staticContentServlet.init(config);
     staticContentServlet.doPost(request, response);
     assertTrue(writer.toString().matches(".*404.*"));
   }
 
   /* Tests that the servlet throws an exception if the staticContentPath is not
    * specified. 
    */
   @Test(expected = RuntimeException.class)
   public void testInit_noStaticContentPath() {
     MockServletConfig config = new MockServletConfig();
     StaticContentServlet staticContentServlet = new StaticContentServlet();
     staticContentServlet.init(config);
   }
 
   /* Tests that the servlet loads a resource from the disk in debug mode.
    */
   @Test
   public void testService_debug() throws Exception {
     MockServletConfig config = new MockServletConfig();
     config.addInitParameter("mimeType_txt", "text");
     config.addInitParameter("staticContentPath",
         "com/globant/katari/core/web");
     config.addInitParameter("debug", "true");
     config.addInitParameter("debugPrefix", "target/test-data");
 
     StaticContentServlet staticContentServlet = new StaticContentServlet();
     staticContentServlet.init(config);
 
     MockHttpServletRequest request = new MockHttpServletRequest();
     request.setMethod("GET");
     request.setServletPath("/sample.txt");
 
     // Make sure the directory exists and sample.txt is not found there.
     File dest = new File("target/test-data/com/globant/katari/core/web");
     dest.mkdirs();
     File sample = new File(dest, "sample.txt");
     sample.delete();
 
     MockHttpServletResponse response = new MockHttpServletResponse();
     staticContentServlet.service(request, response);
     String output = response.getContentAsString().trim();
     assertEquals("Sample not modified.", output);
 
     // Now, create a new file and check that it was found.
     FileWriter out = new FileWriter(sample);
     out.write("Sample is now modified.");
     out.close();
 
     response = new MockHttpServletResponse();
     staticContentServlet.service(request, response);
     output = response.getContentAsString().trim();
     assertEquals("Sample is now modified.", output);
   }
 }
 
