 /**
  * XWeb project
  * https://github.com/abdollahpour/xweb
  * Hamed Abdollahpour - 2013
  */
 
 package ir.xweb.module;
 
 import org.junit.Test;
 
 import javax.servlet.ServletContext;
 import javax.servlet.ServletOutputStream;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 import java.io.IOException;
 import java.util.Collections;
 import java.util.HashMap;
 
 import static org.mockito.Mockito.*;
 
 public class TestWikiModule {
 
     final ServletContext servletContext = mock(ServletContext.class);
 
     final Manager manager;
 
     final WikiModule wikiModule;
 
     public TestWikiModule() throws IOException {
         when(servletContext.getInitParameterNames()).thenReturn(Collections.emptyEnumeration());
 
         manager = new Manager(servletContext);
         manager.load(getClass().getResource("/WEB-INF/xweb.xml"));
 
         wikiModule = manager.getModule(WikiModule.class);
     }
 
     @Test
     public void testGet() throws IOException {
         final HttpServletRequest request = mock(HttpServletRequest.class);
         final HttpServletResponse response = mock(HttpServletResponse.class);
 
         when(response.getOutputStream()).thenReturn(new ServletOutputStream() {
             @Override
             public void write(int b) throws IOException {
                 System.out.write(b);
             }
         });
 
 
         HashMap<String, String> params = new HashMap<String, String>();
         params.put("get", "sample");
 
         wikiModule.process(this.servletContext, request, response, new ModuleParam(params), null);
     }
 
     @Test
     public void testGetWithZip1() throws IOException {
         final HttpServletRequest request = mock(HttpServletRequest.class);
         final HttpServletResponse response = mock(HttpServletResponse.class);
 
         when(request.getHeader("Accept-Encoding")).thenReturn("gzip");
 
         when(response.getOutputStream()).thenReturn(new ServletOutputStream() {
             @Override
             public void write(int b) throws IOException {
             }
         });
 
         HashMap<String, String> params = new HashMap<String, String>();
         params.put("get", "sample");
 
         wikiModule.process(this.servletContext, request, response, new ModuleParam(params), null);
 
         verify(response, times(1)).addHeader("Content-Encoding", "gzip");
     }
 
     @Test
     public void testGetWithZip2() throws IOException {
         final HttpServletRequest request = mock(HttpServletRequest.class);
         final HttpServletResponse response = mock(HttpServletResponse.class);
 
         when(response.getOutputStream()).thenReturn(new ServletOutputStream() {
             @Override
             public void write(int b) throws IOException {
             }
         });
 
         HashMap<String, String> params = new HashMap<String, String>();
         params.put("get", "sample");
 
         wikiModule.process(this.servletContext, request, response, new ModuleParam(params), null);
 
         verify(response, times(0)).addHeader("Content-Encoding", "gzip");
     }
 
 }
