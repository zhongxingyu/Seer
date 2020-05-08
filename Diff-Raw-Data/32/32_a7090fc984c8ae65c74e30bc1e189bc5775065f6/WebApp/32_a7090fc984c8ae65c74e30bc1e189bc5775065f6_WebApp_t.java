 /*
  * The MIT License
  *
  * Copyright (c) 2010 Fabrice Medio
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
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  * THE SOFTWARE.
  */
 
 package baggage.hypertoolkit;
 
 import org.eclipse.jetty.server.Connector;
 import org.eclipse.jetty.server.Server;
 import org.eclipse.jetty.server.nio.SelectChannelConnector;
 import org.eclipse.jetty.servlet.ServletContextHandler;
 import org.eclipse.jetty.servlet.ServletHolder;
 
 import java.util.function.Supplier;
 
public class WebApp {
     private RouteFinder routeFinder;
 
    public WebApp(Supplier<RequestHandler> defaultHandler, String webDir) {
        this.routeFinder = new RouteFinder(defaultHandler, webDir);
     }
 
     public void route(String route, Supplier<RequestHandler> handler) {
         routeFinder.route(route, handler);
     }
 
     public void start(int httpPort) {
         Server server = new Server();
         Connector connector = new SelectChannelConnector();
         connector.setPort(httpPort);
         connector.setHost("0.0.0.0");
 
         server.setConnectors(new Connector[]{
                 connector,
         });
 
         ServletContextHandler contextHandler = new ServletContextHandler();
         contextHandler.setContextPath("/");
         server.setHandler(contextHandler);
 
         Dispatcher dispatcher = new Dispatcher(routeFinder);
         contextHandler.addServlet(new ServletHolder(dispatcher), "/");
         server.setHandler(contextHandler);
 
         try {
             server.start();
             server.join();
         } catch (Exception e) {
             throw new RuntimeException(e);
         }
     }
 }
