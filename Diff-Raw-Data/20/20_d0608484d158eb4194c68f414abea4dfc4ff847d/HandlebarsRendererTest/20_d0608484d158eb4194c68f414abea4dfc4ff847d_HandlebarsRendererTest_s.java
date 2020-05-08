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
 package com.threewks.thundr.view.handlebars;
 
 import static org.hamcrest.Matchers.*;
 import static org.junit.Assert.assertThat;
 import static org.mockito.Matchers.anyString;
 import static org.mockito.Mockito.*;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.MalformedURLException;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.net.URL;
 
 import javax.servlet.ServletContext;
 
 import org.junit.Before;
 import org.junit.Rule;
 import org.junit.Test;
 import org.junit.rules.ExpectedException;
 import org.mockito.Mockito;
 
 import com.github.jknack.handlebars.Handlebars;
 import com.github.jknack.handlebars.HandlebarsException;
 import com.threewks.thundr.test.TestSupport;
 import com.threewks.thundr.test.mock.servlet.MockServletContext;
 import com.threewks.thundr.view.ViewResolutionException;
 
 public class HandlebarsRendererTest {
 
 	@Rule
 	public ExpectedException thrown = ExpectedException.none();
 
 	private ServletContext servletContext = mock(ServletContext.class);
 	private HandlebarsRenderer handlebarsRenderer;
 
 	@Before
 	public void before() throws MalformedURLException, URISyntaxException {
		URL url = this.getClass().getResource("test.hbs");
 		InputStream hbsStream = this.getClass().getResourceAsStream("test.hbs");
 		when(servletContext.getResource(anyString())).thenReturn(url);
 		when(servletContext.getResourceAsStream(anyString())).thenReturn(hbsStream);
 
 		handlebarsRenderer = new HandlebarsRenderer(false);
 	}
 
 	@Test
 	public void shouldCreateAndRetainDebugStatus() {
 		assertThat(new HandlebarsRenderer(true).isDebug(), is(true));
 		assertThat(new HandlebarsRenderer(false).isDebug(), is(false));
 	}
 
 	@Test
 	public void shouldInitialiseOnFirstInvocationOfGetOrCompileTemplate() {
 		assertThat(cache(), is(nullValue()));
 		assertThat(handlebars(), is(nullValue()));
 
		handlebarsRenderer.getOrCompileTemplate("test", servletContext);
 
 		assertThat(handlebars(), is(notNullValue()));
 		assertThat(cache(), is(notNullValue()));
 	}
 
 	@Test
 	public void shouldThrowViewResolutionExceptionWhenIOExceptionOccurs() throws IOException {
 		thrown.expect(ViewResolutionException.class);
 		thrown.expectMessage("Failed to find Handlebars template 'test': Expected");
 
 		handlebarsRenderer.init(new MockServletContext());
 		IOException ioException = new IOException("Expected");
 		Handlebars handlebars = mock(Handlebars.class);
 		when(handlebars.compile(Mockito.any(URI.class))).thenThrow(ioException);
 		TestSupport.setField(handlebarsRenderer, "handlebars", handlebars);
 
 		handlebarsRenderer.getOrCompileTemplate("test", servletContext);
 	}
 
 	@Test
 	public void shouldThrowViewResolutionExceptionWhenHandlebarsExceptionOccurs() throws IOException {
 		thrown.expect(ViewResolutionException.class);
 		thrown.expectMessage("Failed to compile Handlebars template 'test': Expected");
 
 		handlebarsRenderer.init(new MockServletContext());
 		HandlebarsException handlebarsException = new HandlebarsException("Expected", null);
 		Handlebars handlebars = mock(Handlebars.class);
 		when(handlebars.compile(Mockito.any(URI.class))).thenThrow(handlebarsException);
 		TestSupport.setField(handlebarsRenderer, "handlebars", handlebars);
 
 		handlebarsRenderer.getOrCompileTemplate("test", servletContext);
 	}
 
 	private Handlebars handlebars() {
 		return TestSupport.getField(handlebarsRenderer, "handlebars");
 	}
 
 	private ReloadingConcurrentMapCache cache() {
 		return TestSupport.getField(handlebarsRenderer, "cache");
 	}
 
 }
