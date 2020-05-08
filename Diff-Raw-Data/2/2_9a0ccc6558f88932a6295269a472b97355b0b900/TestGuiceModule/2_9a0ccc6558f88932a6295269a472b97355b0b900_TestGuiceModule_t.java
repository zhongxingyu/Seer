 /**
  * Copyright (C) 2012 Ness Computing, Inc.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.nesscomputing.httpserver;
 
 import static java.lang.String.format;
 
 import java.io.IOException;
 import java.io.Writer;
 import java.net.InetSocketAddress;
 import java.net.ServerSocket;
 import java.util.UUID;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.http.HttpStatus;
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 
 import com.google.inject.Binder;
 import com.google.inject.Guice;
 import com.google.inject.Inject;
 import com.google.inject.Injector;
 import com.google.inject.Module;
 import com.google.inject.Scopes;
 import com.google.inject.Singleton;
 import com.google.inject.Stage;
 import com.google.inject.name.Named;
 import com.google.inject.name.Names;
 import com.google.inject.servlet.ServletModule;
 import com.nesscomputing.config.Config;
 import com.nesscomputing.config.ConfigModule;
 import com.nesscomputing.galaxy.GalaxyConfigModule;
 import com.nesscomputing.httpclient.HttpClient;
 import com.nesscomputing.httpclient.guice.HttpClientModule;
 import com.nesscomputing.httpclient.response.StringContentConverter;
 import com.nesscomputing.lifecycle.junit.LifecycleRule;
 import com.nesscomputing.lifecycle.junit.LifecycleRunner;
 import com.nesscomputing.lifecycle.junit.LifecycleStatement;
 import com.nesscomputing.testing.lessio.AllowNetworkAccess;
 import com.nesscomputing.testing.lessio.AllowNetworkListen;
 
 /**
  * Test that the guice filter gets injected correctly and dispatches to a servlet that has an injected value on the c'tor.
  */
 @AllowNetworkListen(ports={0})
 @AllowNetworkAccess(endpoints= {"127.0.0.1:0"})
 @RunWith(LifecycleRunner.class)
 public class TestGuiceModule
 {
     @LifecycleRule
     public final LifecycleStatement lifecycleRule = LifecycleStatement.defaultLifecycle();
 
     @Inject
     @Named("test")
     private final HttpClient httpClient = null;
 
     private String baseUri = null;
 
     private int port = 0;
 
     @Inject
     @Named("magic")
     private UUID checkUuid = null;
 
     @Before
     public void setUp() throws Exception
     {
         port = findUnusedPort();
         Assert.assertFalse(port == 0);
         baseUri = format("http://localhost:%d/magic", port);
 
         final Config config = Config.getFixedConfig("galaxy.internal.port.http", Integer.toString(port));
 
         final Injector injector = Guice.createInjector(Stage.PRODUCTION,
                                                        new ConfigModule(config),
                                                        new HttpServerModule(config),
                                                        new HttpClientModule("test"),
                                                        lifecycleRule.getLifecycleModule(),
                                                        new GalaxyConfigModule(),
                                                        new Module() {
                                                             @Override
                                                             public void configure(Binder binder) {
                                                                 binder.requireExplicitBindings();
                                                                 binder.disableCircularProxies();
                                                             }
                                                        },
                                                        new ServletModule() {
                                                            @Override
                                                            public void configureServlets() {
                                                                bind(UUID.class).annotatedWith(Names.named("magic")).toInstance(UUID.randomUUID());
                                                                bind(MagicServlet.class).in(Scopes.SINGLETON);
                                                                serve("/magic").with(MagicServlet.class);
                                                            }
                                                        });
 
         injector.injectMembers(this);
     }
 
     @Test
     public void testSimpleGet() throws Exception
     {
         final String content = httpClient.get(baseUri, StringContentConverter.DEFAULT_RESPONSE_HANDLER).perform();
 
         Assert.assertEquals(String.format("guice servlet: %s", checkUuid), content);
     }
 
     private static int findUnusedPort()
         throws IOException
     {
         int port;
 
         ServerSocket socket = new ServerSocket();
         try {
             socket.bind(new InetSocketAddress(0));
             port = socket.getLocalPort();
         }
         finally {
             socket.close();
         }
 
         return port;
     }
 
     @Singleton
     public static class MagicServlet extends HttpServlet
     {
        private static final long serialVersionUID = 1L;

         private final UUID magicUuid;
 
         @Inject
         public MagicServlet(@Named("magic") final UUID magicUuid)
         {
             this.magicUuid = magicUuid;
         }
 
         @Override
         protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException
         {
             final Writer writer = resp.getWriter();
             resp.setStatus(HttpStatus.SC_OK);
             resp.setCharacterEncoding("ISO-8859-1");
             resp.setContentType("text/plain");
             writer.write(String.format("guice servlet: %s", magicUuid));
         }
     }
 }
