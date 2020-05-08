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
 
 package com.hartveld.commons.web.jetty;
 
 import org.eclipse.jetty.server.Server;
 import org.eclipse.jetty.server.ServerConnector;
 import org.eclipse.jetty.servlet.DefaultServlet;
 import org.eclipse.jetty.servlet.ServletContextHandler;
 import org.eclipse.jetty.servlet.ServletHolder;
 import org.eclipse.jetty.util.resource.Resource;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class JettyGuiceJerseyServer implements AutoCloseable {
 
 	private static final Logger LOG = LoggerFactory.getLogger(JettyGuiceJerseyServer.class);
 
 	private final Server server;
	private final ServerConnector connector;
 
 	public JettyGuiceJerseyServer(final int port, final String apiContext, final String apiPackage, final String staticResourcesClassPathBase) {
 		LOG.trace("Creating server for port: {}", port);
 
 		server = new Server(port);
 
		connector = new ServerConnector(server);
		server.addConnector(connector);

 		server.setHandler(context(apiPackage, apiContext, staticResourcesClassPathBase));
 	}
 
 	public int getPort() {
		return connector.getLocalPort();
 	}
 
 	public void start() throws Exception {
 		LOG.trace("Starting server ...");
 
 		server.start();
 
		LOG.trace("Actual server port: {}", connector.getLocalPort());
 	}
 
 	@Override
 	public void close() throws Exception {
 		LOG.trace("Stopping server ...");
 
 		server.stop();
 	}
 
 	private static ServletContextHandler context(final String apiPackage, final String apiContext, final String staticResourceClassPathBase) {
 		LOG.trace("Creating context with api @ {} - static files @ {}", apiContext, staticResourceClassPathBase);
 
 		final Resource newClassPathResource = Resource.newClassPathResource(staticResourceClassPathBase);
 		if (newClassPathResource == null) {
 			throw new RuntimeException("Static resource path does not exist: " + staticResourceClassPathBase);
 		}
 
 		final String resourceBasePath = newClassPathResource.toString();
 		LOG.trace("Static resources URL resolves to: {}", resourceBasePath);
 
 		final ServletContextHandler context = new ServletContextHandler();
 
 		final ServletHolder holder = context.addServlet(DefaultServlet.class, "/*");
 		holder.setInitParameter("resourceBase", resourceBasePath);
 		holder.setInitParameter("pathInfoOnly", "true");
 
 		context.setWelcomeFiles(new String[] { "index.html" });
 
 		context.addFilter(com.google.inject.servlet.GuiceFilter.class, '/' + apiContext + "/*", null);
 		context.addEventListener(new GuiceJerseyApiContextListener(apiPackage, apiContext));
 
 		return context;
 	}
 
 }
