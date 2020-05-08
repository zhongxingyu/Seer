 /*
  * This file is a component of thundr, a software library from 3wks.
  * Read more: http://www.3wks.com.au/thundr
  * Copyright (C) 2013 3wks, <thundr@3wks.com.au>
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
 package com.threewks.thundr.action.staticResource;
 
 import static org.hamcrest.Matchers.*;
 import static org.junit.Assert.*;
 import static org.mockito.Matchers.anyString;
 import static org.mockito.Mockito.*;
 
 import java.io.IOException;
 import java.util.Map;
 
 import javax.servlet.ServletContext;
 import javax.servlet.ServletException;
 
 import org.junit.Test;
 
 import com.atomicleopard.expressive.Expressive;
 import com.threewks.thundr.exception.BaseException;
 import com.threewks.thundr.route.RouteType;
 import com.threewks.thundr.test.mock.servlet.MockHttpServletRequest;
 import com.threewks.thundr.test.mock.servlet.MockHttpServletResponse;
 import com.threewks.thundr.test.mock.servlet.MockServletContext;
 
 public class StaticResourceActionResolverTest {
 	private MockServletContext servletContext = new MockServletContext();
 	private StaticResourceActionResolver resolver = new StaticResourceActionResolver(servletContext);
 
 	private StaticResourceAction action = new StaticResourceAction();
 	private RouteType routeType = RouteType.GET;
 	private MockHttpServletRequest req = new MockHttpServletRequest();
 	private MockHttpServletResponse resp = new MockHttpServletResponse();
 	private Map<String, String> pathVars = Expressive.<String, String> map();
 
 	@Test
 	public void shouldMatchActionNameOfStatic() {
 		assertThat(resolver.createActionIfPossible("static"), is(notNullValue()));
 		assertThat(resolver.createActionIfPossible("STATIC"), is(notNullValue()));
 		assertThat(resolver.createActionIfPossible("static:/something"), is(notNullValue()));
 		assertThat(resolver.createActionIfPossible(""), is(nullValue()));
 		assertThat(resolver.createActionIfPossible("   "), is(nullValue()));
 		assertThat(resolver.createActionIfPossible("something.else"), is(nullValue()));
 	}
 
 	@Test
 	public void shouldResolveByInvokingServeAndReturningNull() throws ServletException, IOException {
 		resolver = spy(resolver);
 		doNothing().when(resolver).serve(action, req, resp);
 
 		Object result = resolver.resolve(action, routeType, req, resp, pathVars);
 		assertThat(result, is(nullValue()));
 
 		verify(resolver).serve(action, req, resp);
 	}
 
 	@Test
 	public void shouldWrapExceptionWhileServingInBaseExceptionIfThereIsNoCause() throws ServletException, IOException {
 		resolver = spy(resolver);
 		IOException cause = new IOException();
 		doThrow(cause).when(resolver).serve(action, req, resp);
 
 		try {
 			resolver.resolve(action, routeType, req, resp, pathVars);
 			fail("Expected a BaseException");
 		} catch (BaseException e) {
 			assertThat(e.getCause(), is((Throwable) cause));
 		}
 	}
 
 	@Test
 	public void shouldWrapCauseExceptionWhileServingInBaseExceptionIfThereIsACause() throws ServletException, IOException {
 		resolver = spy(resolver);
 		IOException cause = new IOException();
 		IOException exception = new IOException(cause);
 		doThrow(exception).when(resolver).serve(action, req, resp);
 
 		try {
 			resolver.resolve(action, routeType, req, resp, pathVars);
 			fail("Expected a BaseException");
 		} catch (BaseException e) {
 			assertThat(e.getCause(), is((Throwable) cause));
 		}
 	}
 
 	@Test
 	public void shouldDeriveMimeTypeFromServletContext() {
 		ServletContext mockServletContext = mock(ServletContext.class);
 		resolver = new StaticResourceActionResolver(mockServletContext);
 		when(mockServletContext.getMimeType("resource.file")).thenReturn("text/file");
 		String mimeType = resolver.deriveMimeType("resource.file");
 		assertThat(mimeType, is("text/file"));
 	}
 
 	@Test
 	public void shouldDeriveMimeTypeFromDefaultSetIfCannotDetermineFromServletContext() {
 		ServletContext mockServletContext = mock(ServletContext.class);
 		resolver = new StaticResourceActionResolver(mockServletContext);
 		assertThat(resolver.deriveMimeType("resource.html"), is("text/html"));
 		assertThat(resolver.deriveMimeType("resource.htm"), is("text/html"));
 		assertThat(resolver.deriveMimeType("resource.css"), is("text/css"));
 		assertThat(resolver.deriveMimeType("resource.js"), is("text/javascript"));
 		assertThat(resolver.deriveMimeType("resource.jpeg"), is("image/jpeg"));
 		assertThat(resolver.deriveMimeType("resource.jpg"), is("image/jpeg"));
 		assertThat(resolver.deriveMimeType("resource.png"), is("image/png"));
 		assertThat(resolver.deriveMimeType("resource.gif"), is("image/gif"));
 		assertThat(resolver.deriveMimeType("resource.ico"), is("image/vnd.microsoft.icon"));
 		assertThat(resolver.deriveMimeType("resource.htc"), is("text/x-component"));
 	}
 
 	@Test
 	public void shouldAllowResourcesFromPathsThatArentWebInf() {
 		assertThat(resolver.isAllowed("/"), is(true));
 		assertThat(resolver.isAllowed("/static"), is(true));
 		assertThat(resolver.isAllowed("/static/"), is(true));
 		assertThat(resolver.isAllowed("/WEB-INF/"), is(false));
 		assertThat(resolver.isAllowed("/WEB-INF/jsp/something.jsp"), is(false));
 	}
 
 	@Test
 	public void shouldKnowWhichMimeTypesCanBeCompressed() {
 		assertThat(resolver.matchesCompressedMimeTypes("text/plain"), is(true));
 		assertThat(resolver.matchesCompressedMimeTypes("text/html"), is(true));
 		assertThat(resolver.matchesCompressedMimeTypes("text/css"), is(true));
 		assertThat(resolver.matchesCompressedMimeTypes("text/anything"), is(true));
 		assertThat(resolver.matchesCompressedMimeTypes("image/png"), is(false));
 	}
 
 	@Test
 	public void shouldZipIfMatchesCompressibleMimeTypeAndClientAcceptsGzip() {
 		assertThat(resolver.shouldZip("gzip,deflate,sdch", "text/html"), is(true));
 		assertThat(resolver.shouldZip("gzip,deflate,sdch", "text/css"), is(true));
 		assertThat(resolver.shouldZip("gzip", "text/html"), is(true));
 		assertThat(resolver.shouldZip("deflate,sdch", "text/html"), is(false));
 		assertThat(resolver.shouldZip("deflate,sdch", "image/png"), is(false));
 		assertThat(resolver.shouldZip("gzip", "image/png"), is(false));
 	}
 
 	@Test
 	public void shouldSendErrorWhenServingResourceIsNotAllowed() throws ServletException, IOException {
 		req.url("/resource.html");
 		resolver = spy(resolver);
 		when(resolver.isAllowed(anyString())).thenReturn(false);
 
 		resolver.serve(action, req, resp);
 
 		assertThat(resp.isCommitted(), is(true));
 		assertThat(resp.status(), is(404));
 	}
 
 	@Test
 	public void shouldSendErrorWhenServingResourceWhichDoesNotExist() throws ServletException, IOException {
 		ServletContext servletContext = mock(ServletContext.class);
 		resolver = new StaticResourceActionResolver(servletContext);
 
 		resolver.serve(action, req, resp);
 
 		assertThat(resp.isCommitted(), is(true));
 		assertThat(resp.status(), is(404));
 	}
 }
