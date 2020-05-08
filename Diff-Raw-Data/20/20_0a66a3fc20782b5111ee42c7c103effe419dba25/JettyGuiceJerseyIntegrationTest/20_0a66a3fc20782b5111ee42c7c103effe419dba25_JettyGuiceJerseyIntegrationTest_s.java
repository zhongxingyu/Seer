 /*
  * Copyright (c) 2013 David Hartveld
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
  * SOFTWARE.
  */
 
 package com.hartveld.commons.web.jetty.test;
 
 import static org.apache.http.HttpStatus.SC_FORBIDDEN;
 import static org.apache.http.HttpStatus.SC_OK;
 import static org.hamcrest.Matchers.is;
 import static org.junit.Assert.assertThat;
 
 import java.io.IOException;
 import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.util.EntityUtils;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Ignore;
 import org.junit.Rule;
 import org.junit.Test;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class JettyGuiceJerseyIntegrationTest {
 
 	private static final Logger LOG = LoggerFactory.getLogger(JettyGuiceJerseyIntegrationTest.class);
 
 	@Rule
 	public JettyServerRule server = new JettyServerRule(0, "api", "com.hartveld.commons.web.jetty.test", "/web-test", null);
 
	private HttpClient client;
 
 	@Before
 	public void setUp() {
		client = new DefaultHttpClient();
 	}
 
 	@After
 	public void tearDown() {
		client.getConnectionManager().shutdown();
 	}
 
 	@Test
 	public void testThatResourceContentsCanBeRetrieved() throws Exception {
 		final String url = "http://localhost:" + server.getPort() + "/api/demo/hello";
 
 		final HttpGet get = new HttpGet(url);
 		final HttpResponse response = client.execute(get);
 
 		LOG.trace("Request to: {}", url);
 		LOG.trace("Response  : {}", response.getStatusLine());
 		LOG.trace("Contents  : {}", contentsOf(response));
 
 		assertThat(response.getStatusLine().getStatusCode(), is(SC_OK));
 	}
 
 	@Test
 	@Ignore("Not yet implemented")
 	public void testThatSecuredResourceCannotBeRetrieved() throws Exception {
 		final String url = "http://localhost:" + server.getPort() + "/api/secured/forbidden";
 
 		final HttpGet get = new HttpGet(url);
 		final HttpResponse response = client.execute(get);
 
 		LOG.trace("Request to: {}", url);
 		LOG.trace("Response  : {}", response.getStatusLine());
 		LOG.trace("Contents  : {}", contentsOf(response));
 
 		assertThat(response.getStatusLine().getStatusCode(), is(SC_FORBIDDEN));
 	}
 
 	private static String contentsOf(final HttpResponse response) throws IOException {
 		return EntityUtils.toString(response.getEntity());
 	}
 
 }
