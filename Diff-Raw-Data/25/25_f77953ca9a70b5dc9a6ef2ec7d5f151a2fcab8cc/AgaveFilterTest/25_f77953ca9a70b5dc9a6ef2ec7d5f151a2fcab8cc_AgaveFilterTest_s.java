 /**
  * Copyright (c) 2008, Damian Carrillo
  * All rights reserved.
  * 
  * Redistribution and use in source and binary forms, with or without modification, are permitted 
  * provided that the following conditions are met:
  * 
  *   * Redistributions of source code must retain the above copyright notice, this list of 
  *     conditions and the following disclaimer.
  *   * Redistributions in binary form must reproduce the above copyright notice, this list of 
  *     conditions and the following disclaimer in the documentation and/or other materials 
  *     provided with the distribution.
  *   * Neither the name of the copyright holder's organization nor the names of its contributors 
  *     may be used to endorse or promote products derived from this software without specific 
  *     prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR 
  * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND 
  * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR 
  * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL 
  * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, 
  * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER 
  * IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT 
  * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 package agave;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URL;
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.servlet.DelegatingServletInputStream;
 import javax.servlet.http.HttpServletResponse;
 
 import org.jmock.Expectations;
 import org.junit.Assert;
 import org.junit.Test;
 
 import agave.conversion.BooleanConverter;
 import agave.internal.HandlerDescriptor;
 import agave.internal.ReflectionInstanceFactory;
 import agave.sample.AliasedForm;
 import agave.sample.LoginForm;
 import agave.sample.MultipleHandler;
 import agave.sample.SampleHandler;
 
 
 /**
  * @author <a href="mailto:damiancarrillo@gmail.com">Damian Carrillo</a>
  */
 public class AgaveFilterTest extends MockedEnvironmentTest {
     
     @Test
     public void testScanClassesDirForLoginHandler() throws Exception {
         AgaveFilter filter = scanRoot();
 
         context.checking(new Expectations() {{
             allowing(request).getRequestURI(); will(returnValue("/app/login"));
             allowing(request).getContextPath(); will(returnValue("/app"));
         }});
         
         filter.init(filterConfig);
         HandlerDescriptor desc = filter.getHandlerRegistry().findMatch(request);
         Assert.assertNotNull(desc);
         Assert.assertEquals(SampleHandler.class, desc.getHandlerClass());
     }
     
     @Test
     public void testScanClassesDirForMultipleHandler() throws Exception {
         AgaveFilter filter = scanRoot();
         
         context.checking(new Expectations() {{
             allowing(request).getRequestURI(); will(returnValue("/app/test1"));
             allowing(request).getContextPath(); will(returnValue("/app"));
         }});
         
         HandlerDescriptor desc = filter.getHandlerRegistry().findMatch(request);
         Assert.assertEquals(MultipleHandler.class, desc.getHandlerClass());
     }
 
     @Test
     public void testScanClassesDirForHandlerForms() throws Exception {
         AgaveFilter filter = scanRoot();
         
         context.checking(new Expectations() {{
             allowing(request).getRequestURI(); will(returnValue("/app/login"));
             allowing(request).getContextPath(); will(returnValue("/app"));
         }});
         
         HandlerDescriptor desc = filter.getHandlerRegistry().findMatch(request);
         
         Assert.assertNotNull(desc);
         Assert.assertEquals(SampleHandler.class, desc.getHandlerClass());
         Assert.assertEquals(LoginForm.class, desc.getFormClass());
         Assert.assertEquals(LoginForm.class.getMethod("setUsername", String.class), 
             desc.getMutators().get("username"));
         Assert.assertEquals(LoginForm.class.getMethod("setPassword", String.class), 
             desc.getMutators().get("password"));
         Assert.assertEquals(LoginForm.class.getMethod("setRemembered", Boolean.class), 
             desc.getMutators().get("remembered"));
         Assert.assertEquals(BooleanConverter.class, desc.getConverters().get("remembered"));
     }
 
     @Test
     public void testScanClassesDirForHandlerFormsWithAliasedProperties() throws Exception {
         AgaveFilter filter = scanRoot();
         
         context.checking(new Expectations() {{
             allowing(request).getRequestURI(); will(returnValue("/app/aliased"));
             allowing(request).getContextPath(); will(returnValue("/app"));
         }});
         
         HandlerDescriptor desc = filter.getHandlerRegistry().findMatch(request);
         
         Assert.assertNotNull(desc);
         Assert.assertEquals(SampleHandler.class, desc.getHandlerClass());
         Assert.assertEquals(AliasedForm.class, desc.getFormClass());
         Assert.assertEquals(AliasedForm.class.getMethod("setSomeProperty", String.class), 
             desc.getMutators().get("someAlias"));
         Assert.assertEquals(AliasedForm.class.getMethod("setAnotherProperty", Boolean.class),
             desc.getMutators().get("anotherAlias"));
         Assert.assertEquals(BooleanConverter.class, desc.getConverters().get("anotherAlias"));
     }
 
     @Test
     public void testInit() throws Exception {
         AgaveFilter filter = createSilentAgaveFilter();
 
         context.checking(new Expectations() {{
             specialize(this, new HashMap<String, String[]>());
             allowing(request).getRequestURI(); will(returnValue("/app/login/"));
             allowing(request).getContextPath(); will(returnValue("/app"));
         }});
 
         filter.init(filterConfig);
 
         Assert.assertNotNull(filter.getHandlerRegistry());
         Assert.assertNotNull(filter.getHandlerRegistry().findMatch(request));
     }
     
     @Test
     public void testDoFilter() throws Exception {
         AgaveFilter filter = createSilentAgaveFilter();
 
         final Map<String, String[]> parameterMap = new HashMap<String, String[]>();
         parameterMap.put("username", new String[] {"damian"});
         parameterMap.put("password", new String[] {"password"});
         parameterMap.put("remembered", new String[] {"false"});
 
         context.checking(new Expectations() {{
             specialize(this, parameterMap);
             allowing(request).getRequestURI(); will(returnValue("/app/login"));
             allowing(request).getContextPath(); will(returnValue("/app"));
             allowing(request).getContentType(); will(returnValue("application/x-www-form-urlencoded"));
 
             one(request).setAttribute("loggedIn", Boolean.TRUE);
             one(response).setStatus(400);
         }});
 
         filter.init(filterConfig);
         filter.doFilter(request, response, filterChain);
 
         parameterMap.put("password", new String[] {"secret"});
 
         context.checking(new Expectations() {{
             specialize(this, parameterMap);
             allowing(request).getRequestURI(); will(returnValue("/login"));
             allowing(request).getContentType(); will(returnValue("application/x-www-form-urlencoded"));
 
             one(request).setAttribute("loggedIn", Boolean.FALSE);
             one(response).setStatus(400);
         }});
 
         filter.init(filterConfig);
         filter.doFilter(request, response, filterChain);
     }
 
     @Test(expected = NullPointerException.class)
     public void testThrowsNullPointerException() throws Exception {
         AgaveFilter filter = createSilentAgaveFilter();
         final Map<String, String[]> parameterMap = new HashMap<String, String[]>();
 
         context.checking(new Expectations() {{
             specialize(this, parameterMap);
             allowing(request).getRequestURI(); will(returnValue("/app/throws/nullPointerException"));
             allowing(request).getContextPath(); will(returnValue("/app"));
             allowing(request).getContentType(); will(returnValue("application/x-www-form-urlencoded"));
         }});
 
         filter.init(filterConfig);
         filter.doFilter(request, response, filterChain);
     }
 
     @Test(expected = IOException.class)
     public void testThrowsIOException() throws Exception {
         AgaveFilter filter = new AgaveFilter();
         final Map<String, String[]> parameterMap = new HashMap<String, String[]>();
 
         context.checking(new Expectations() {{
             specialize(this, parameterMap);
             allowing(request).getRequestURI(); will(returnValue("/app/throws/ioException"));
             allowing(request).getContextPath(); will(returnValue("/app"));
             allowing(request).getContentType(); will(returnValue("application/x-www-form-urlencoded"));
         }});
 
         filter.init(filterConfig);
         filter.doFilter(request, response, filterChain);
     }
 
     @Test
     public void testWithoutForm() throws Exception {
         AgaveFilter filter = new AgaveFilter();
         final Map<String, String[]> parameterMap = new HashMap<String, String[]>();
 
         context.checking(new Expectations() {{
             specialize(this, parameterMap);
             allowing(request).getRequestURI(); will(returnValue("/app/lacks/form"));
             allowing(request).getContextPath(); will(returnValue("/app"));
             allowing(request).getContentType(); will(returnValue("application/x-www-form-urlencoded"));
 
             one(request).setAttribute("noErrors", Boolean.TRUE);
         }});
 
         filter.init(filterConfig);
         filter.doFilter(request, response, filterChain);
     }
 
     @Test
     public void testWithNoMatchingPattern() throws Exception {
         AgaveFilter filter = new AgaveFilter();
         final Map<String, String[]> parameterMap = new HashMap<String, String[]>();
 
         context.checking(new Expectations() {{
             specialize(this, parameterMap);
             allowing(request).getRequestURI(); will(returnValue("/app/no/matching/pattern"));
             allowing(request).getContextPath(); will(returnValue("/app"));
             allowing(request).getContentType(); will(returnValue("application/x-www-form-urlencoded"));
 
             one(filterChain).doFilter(request, response);
         }});
 
         filter.init(filterConfig);
         filter.doFilter(request, response, filterChain);
     }
 
     @Test
     public void testURIParamsOverrideRequestParams() throws Exception {
         AgaveFilter filter = new AgaveFilter();
         final Map<String, String[]> parameterMap = new HashMap<String, String[]>();
         parameterMap.put("username", new String[] {"damian"});
         parameterMap.put("password", new String[] {"password"});
 
         context.checking(new Expectations() {{
             specialize(this, parameterMap);
             allowing(request).getRequestURI(); will(returnValue("/app/uri-params/damian/secret"));
             allowing(request).getContextPath(); will(returnValue("/app"));
             allowing(request).getContentType(); will(returnValue("application/x-www-form-urlencoded"));
             allowing(request).getParameterMap(); will(returnValue(parameterMap));
 
             one(request).setAttribute("username", "damian");
             one(request).setAttribute("password", "secret");
         }});
 
         filter.init(filterConfig);
         filter.doFilter(request, response, filterChain);
     }
     
     @Test
     public void testMultipartRequest() throws Exception {
         AgaveFilter filter = new AgaveFilter();
         final InputStream in = getClass().getClassLoader().getResourceAsStream("multipart-sample-tomcat");
 
         context.checking(new Expectations() {{
             specialize(this, new HashMap<String, String[]> ());
             allowing(request).getParameterMap(); will(returnValue(new HashMap<String, String[]>()));
             allowing(request).getRequestURI(); will(returnValue("/app/upload/file"));
             allowing(request).getContextPath(); will(returnValue("/app"));
             allowing(request).getContentType(); will(returnValue("multipart/form-data"));
             allowing(request).getInputStream(); will(returnValue(new DelegatingServletInputStream(in)));
             
             one(request).setAttribute("file", false);
             one(response).setStatus(400);
         }});
 
         filter.init(filterConfig);
         filter.doFilter(request, response, filterChain);
     }
     
     @Test
     public void testNullDestination() throws Exception {
         AgaveFilter filter = new AgaveFilter();
 
         context.checking(new Expectations() {{
             specialize(this, new HashMap<String, String[]>());
             allowing(request).getParameterMap(); will(returnValue(new HashMap<String, String[]>()));
             allowing(request).getRequestURI(); will(returnValue("/app/shout/hello"));
             allowing(request).getContextPath(); will(returnValue("/app"));
             allowing(request).getContentType(); will(returnValue("application/x-www-form-urlencoded"));
             allowing(response).setStatus(HttpServletResponse.SC_FORBIDDEN);
             allowing(response).isCommitted(); will(returnValue(true));
         }});
 
         filter.init(filterConfig);
         filter.doFilter(request, response, filterChain);
     }
     
     @Test
     public void testReturnDestinationWithForward() throws Exception {
         AgaveFilter filter = new AgaveFilter();
         
         context.checking(new Expectations() {{
             specialize(this, new HashMap<String, String[]>());
             allowing(servletContext).getContextPath(); will(returnValue("/app"));
             allowing(request).getParameterMap(); will(returnValue(new HashMap<String, String[]>()));
             allowing(request).getRequestURI(); will(returnValue("/app/say/hello"));
             allowing(request).getContextPath(); will(returnValue("/app"));
             allowing(request).getContentType(); will(returnValue("application/x-www-form-urlencoded"));
             allowing(request).getMethod(); will(returnValue("GET"));
             allowing(response).isCommitted(); will(returnValue(false));
             
            one(request).getRequestDispatcher("/app/say.jsp?said=hello"); will(returnValue(requestDispatcher)); 
             one(requestDispatcher).forward(request, response);
         }});
 
         filter.init(filterConfig);
         filter.doFilter(request, response, filterChain);
     }
     
     @Test
     public void testReturnDestinationWithRedirectAfterPost() throws Exception {
         AgaveFilter filter = new AgaveFilter();
         
         context.checking(new Expectations() {{
             specialize(this, new HashMap<String, String[]>());
             allowing(request).getParameterMap(); will(returnValue(new HashMap<String, String[]>()));
             allowing(request).getRequestURI(); will(returnValue("/app/say/hello"));
             allowing(request).getContextPath(); will(returnValue("/app"));
             allowing(request).getContentType(); will(returnValue("application/x-www-form-urlencoded"));
             allowing(request).getMethod(); will(returnValue("POST"));
             allowing(response).isCommitted(); will(returnValue(false));
             
             one(servletContext).getContextPath(); will(returnValue("/app"));
            one(response).sendRedirect("/app/say.jsp?said=hello");
         }});
 
         filter.init(filterConfig);
         filter.doFilter(request, response, filterChain);
     }
     
     @Test
     public void testReturnDestinationWithExplicitRedirect() throws Exception {
         AgaveFilter filter = new AgaveFilter();
         
         URL rootUrl = getClass().getClassLoader().getResource("agave");
         final String realPath = new File(rootUrl.toURI()).getAbsolutePath();
 
         context.checking(new Expectations() {{
             specialize(this, new HashMap<String, String[]>());
             allowing(request).getParameterMap(); will(returnValue(new HashMap<String, String[]>()));
             allowing(request).getRequestURI(); will(returnValue("/app/whisper/hello"));
             allowing(request).getContextPath(); will(returnValue("/app"));
             allowing(request).getContentType(); will(returnValue("application/x-www-form-urlencoded"));
             allowing(request).getMethod(); will(returnValue("GET"));
             allowing(response).isCommitted(); will(returnValue(false));
             
             one(servletContext).getContextPath(); will(returnValue("/app"));
            one(response).sendRedirect("/app/whisper.jsp?how=very%20softly%20&amp;%20sweetly&said=hello");
         }});
 
         filter.init(filterConfig);
         filter.doFilter(request, response, filterChain);
     }
     
     @Test
     public void testReturnURI() throws Exception {
         AgaveFilter filter = new AgaveFilter();
         
         URL rootUrl = getClass().getClassLoader().getResource("agave");
         final String realPath = new File(rootUrl.toURI()).getAbsolutePath();
 
         context.checking(new Expectations() {{
             specialize(this, new HashMap<String, String[]>());
             allowing(request).getParameterMap(); will(returnValue(new HashMap<String, String[]>()));
             allowing(request).getRequestURI(); will(returnValue("/app/proclaim/hello"));
             allowing(request).getContextPath(); will(returnValue("/app"));
             allowing(request).getContentType(); will(returnValue("application/x-www-form-urlencoded"));
             allowing(request).getMethod(); will(returnValue("GET"));
             allowing(response).isCommitted(); will(returnValue(false));
             
             one(response).sendRedirect("http://www.utexas.edu/");
         }});
 
         filter.init(filterConfig);
         filter.doFilter(request, response, filterChain);
     }
 
     @Test
     public void testWithNoUserSuppliedInstanceFactory() throws Exception {
         AgaveFilter filter = new AgaveFilter();
 
         context.checking(new Expectations() {{
             specialize(this, new HashMap<String, String[]>());
         }});
 
         filter.init(filterConfig);
         Assert.assertTrue(filter.getInstanceFactory() instanceof ReflectionInstanceFactory);
     }
     
 }
