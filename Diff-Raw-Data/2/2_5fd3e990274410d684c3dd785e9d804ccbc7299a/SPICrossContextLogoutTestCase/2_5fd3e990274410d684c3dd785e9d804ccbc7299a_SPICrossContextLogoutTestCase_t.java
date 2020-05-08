 /*
  * JBoss, Home of Professional Open Source.
  * Copyright 2011, Red Hat Middleware LLC, and individual contributors
  * as indicated by the @author tags. See the copyright.txt file in the
  * distribution for a full listing of individual contributors.
  *
  * This is free software; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation; either version 2.1 of
  * the License, or (at your option) any later version.
  *
  * This software is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this software; if not, write to the Free
  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
  */
 package org.gatein.wci.spi;
 
 import org.gatein.wci.ServletContainer;
 import org.gatein.wci.ServletContextDispatcher;
 import org.gatein.wci.TestServlet;
 import org.gatein.wci.WebApp;
 import org.gatein.wci.WebAppRegistry;
 import org.gatein.wci.WebRequest;
 import org.gatein.wci.WebResponse;
 import org.gatein.wci.authentication.AuthenticationEvent;
 import org.gatein.wci.authentication.AuthenticationListener;
 import org.gatein.wci.authentication.GenericAuthentication;
 import org.gatein.wci.authentication.TicketService;
 import org.gatein.wci.endpoint.EndPointTestCase;
 import org.gatein.wci.impl.DefaultServletContainerFactory;
 import org.gatein.wci.security.Credentials;
 import org.gatein.wci.security.WCIController;
 import org.gatein.wci.spi.callbacks.NormalCallback;
 import org.jboss.unit.Failure;
 import org.jboss.unit.driver.DriverCommand;
 import org.jboss.unit.driver.DriverResponse;
 import org.jboss.unit.driver.response.EndTestResponse;
 import org.jboss.unit.driver.response.FailureResponse;
 import org.jboss.unit.remote.driver.handler.deployer.response.DeployResponse;
 import org.jboss.unit.remote.driver.handler.deployer.response.UndeployResponse;
 import org.jboss.unit.remote.driver.handler.http.response.InvokeGetResponse;
 
 import javax.servlet.ServletContext;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 import static org.jboss.unit.api.Assert.*;
 import static org.jboss.unit.api.Assert.assertTrue;
 
 /**
  * TestCase to test cross-context session invalidation on logout
  *
  * @author <a href="mailto:mstrukel@redhat.com">Marko Strukelj</a>
  */
 public class SPICrossContextLogoutTestCase extends EndPointTestCase
 {
    /**
     * .
     */
    private final Value v = new Value();
 
    /**
     * .
     */
    private WCIController wciController = new TestController();
 
    /**
     * .
     */
    private WebAppRegistry registry;
 
    /**
     * .
     */
    private Set<String> keys;
 
    /**
     * .
     */
    private ServletContainer container;
 
    /**
     * Session attribute for the test - contains some session data
     */
    private static final String CROSS_CTX_TEST_ATTR = "cross-ctx-test";
 
    /**
     * Key to take note of include invocation
     */
    private static final String COUNT_KEY = "count";
 
    public DriverResponse service(TestServlet testServlet, WebRequest req, WebResponse resp) throws ServletException, IOException
    {
       Credentials credentials = wciController.getCredentials(req, resp);
 
       if (getRequestCount() == 1)
       {
          assertNull(req.getUserPrincipal());
          container = DefaultServletContainerFactory.getInstance().getServletContainer();
          container.addAuthenticationListener(new TestListener(v));
          assertEquals("", v.value);
          container.login(req, resp, credentials, TicketService.DEFAULT_VALIDITY);
 
          if ("Tomcat/7.x".equals(container.getContainerInfo()) || "JBossas/6.x".equals(container.getContainerInfo()))
          {
             assertEquals("login", v.value);
             assertNotNull(req.getUserPrincipal());
             assertTrue(req.isUserInRole("test"));
          }
          else
          {
             // Test Ticket Service
             String ticket = GenericAuthentication.TICKET_SERVICE.createTicket(credentials, TicketService.DEFAULT_VALIDITY);
             Credentials resultCredentials = GenericAuthentication.TICKET_SERVICE.validateTicket(ticket, false);
             assertEquals(credentials.getUsername(), resultCredentials.getUsername());
             assertEquals(credentials.getPassword(), resultCredentials.getPassword());
             assertNotNull(GenericAuthentication.TICKET_SERVICE.validateTicket(ticket, true));
             assertNull(GenericAuthentication.TICKET_SERVICE.validateTicket(ticket, true));
 
             // Test login Event
             assertEquals("login", v.value);
             assertTrue(resp.isCommitted());
          }
 
          String url = resp.renderURL("/", null, null);
          return new InvokeGetResponse(url);
       }
       else if (getRequestCount() == 2)
       {
 
          // Counter to keep an eye on includes
          final Map<String, String> rets = new HashMap<String, String>();
 
          // Set some session state in this context
          req.getSession().setAttribute(CROSS_CTX_TEST_ATTR, "1");
 
          // Set some session state in another context
          ServletContext appContext = testServlet.getServletContext().getContext("/test-spi-app");
          ServletContextDispatcher dispatcher = new ServletContextDispatcher(req, resp, container);
 
          NormalCallback cb = new NormalCallback(appContext, registry.getWebApp("/test-spi-app").getClassLoader())
          {
             public Object doCallback(ServletContext dispatchedServletContext, HttpServletRequest dispatchedRequest, HttpServletResponse dispatchedResponse, Object handback) throws ServletException, IOException
             {
                dispatchedRequest.getSession().setAttribute(CROSS_CTX_TEST_ATTR, "2");
                rets.put(COUNT_KEY, "1");
                return super.doCallback(dispatchedServletContext, dispatchedRequest, dispatchedResponse, handback);
             }
          };
 
          DriverResponse response = cb.test(null, dispatcher);
          if (response != null)
             return response;
 
          // Check that the callback was invoked and the other context really was used with another session
          assertEquals("1", req.getSession().getAttribute(CROSS_CTX_TEST_ATTR));
          assertEquals("1", rets.get(COUNT_KEY));
 
          String url = resp.renderURL("/", null, null);
          return new InvokeGetResponse(url);
       }
       else if (getRequestCount() == 3)
       {
          // Check that the sessions retained values between requests
          // test-spi-server context
          assertEquals("1", req.getSession().getAttribute(CROSS_CTX_TEST_ATTR));
 
          // test-spi-app context
 
          // Map to get data from includes
          final Map<String, String> rets = new HashMap<String, String>();
 
          ServletContext appContext = testServlet.getServletContext().getContext("/test-spi-app");
          ServletContextDispatcher dispatcher = new ServletContextDispatcher(req, resp, container);
 
          NormalCallback cb = new NormalCallback(appContext, registry.getWebApp("/test-spi-app").getClassLoader())
          {
             public Object doCallback(ServletContext dispatchedServletContext, HttpServletRequest dispatchedRequest, HttpServletResponse dispatchedResponse, Object handback) throws ServletException, IOException
             {
                rets.put(CROSS_CTX_TEST_ATTR, (String) dispatchedRequest.getSession().getAttribute(CROSS_CTX_TEST_ATTR));
                rets.put(COUNT_KEY, "1");
                return super.doCallback(dispatchedServletContext, dispatchedRequest, dispatchedResponse, handback);
             }
          };
 
          DriverResponse response = cb.test(null, dispatcher);
          if (response != null)
             return response;
 
          // Check that the callback was invoked and the other context really was used with another session
          assertEquals("1", rets.get(COUNT_KEY));
          assertEquals("2", rets.get(CROSS_CTX_TEST_ATTR));
 
 
          // Perform logout
 
          if ("Tomcat/7.x".equals(container.getContainerInfo()) || "JBossas/6.x".equals(container.getContainerInfo()))
          {
             assertEquals("login", v.value);
 
             container.logout(req, resp);
 
             assertEquals("logout", v.value);
             assertNull(req.getUserPrincipal());
          }
          else
          {
             // Test logout
             assertNotNull(req.getSession(false));
             assertEquals("login", v.value);
             container.logout(req, resp);
             assertNull(req.getSession(false));
 
             // Test logout Event
             assertEquals("logout", v.value);
          }
 
          // Session state must be gone
          assertNull(req.getSession().getAttribute(CROSS_CTX_TEST_ATTR));
 
          // Get session state from the other context again
          cb = new NormalCallback(appContext, registry.getWebApp("/test-spi-app").getClassLoader())
          {
             public Object doCallback(ServletContext dispatchedServletContext, HttpServletRequest dispatchedRequest, HttpServletResponse dispatchedResponse, Object handback) throws ServletException, IOException
             {
                rets.put(COUNT_KEY, "2");
                rets.put(CROSS_CTX_TEST_ATTR, (String) dispatchedRequest.getSession().getAttribute("cross-ctx-test"));
                return super.doCallback(dispatchedServletContext, dispatchedRequest, dispatchedResponse, handback);
             }
          };
 
          response = cb.test(null, dispatcher);
          if (response != null)
             return response;
 
          // check that in the other context session state is deleted as well
          assertEquals("2", rets.get(COUNT_KEY));
          assertEquals(null, rets.get(CROSS_CTX_TEST_ATTR));
 
          return new UndeployResponse("test-spi-app.war");
       }
       else if (getRequestCount() == 4)
       {
          return new EndTestResponse();
       }
 
       return new FailureResponse(Failure.createAssertionFailure("End test reached"));
    }
 
    public DriverResponse invoke(TestServlet testServlet, DriverCommand driverCommand)
    {
       if (getRequestCount() == -1)
       {
          container = DefaultServletContainerFactory.getInstance().getServletContainer();
          if (container == null)
          {
             return new FailureResponse(Failure.createAssertionFailure("No servlet container present"));
          }
 
          // Register and save the deployed web apps
          registry = new WebAppRegistry();
          container.addWebAppListener(registry);
          keys = new HashSet<String>(registry.getKeys());
 
          // Deploy the application web app
          return new DeployResponse("test-spi-app.war");
       }
       else if (getRequestCount() == 0)
       {
          FailureResponse failureResponse = checkDeployments("/test-spi-app", 1);
          if (failureResponse != null)
          {
             return failureResponse;
          }
          else
          {
             // call service method
             return new InvokeGetResponse("/test-spi-server");
          }
       }
       else
       {
          return new FailureResponse(Failure.createAssertionFailure(""));
       }
    }
 
    protected FailureResponse checkDeployments(String appContext, int count)
    {
       // Compute the difference with the previous deployed web apps
       Set diff = new HashSet<String>(registry.getKeys());
       diff.removeAll(keys);
 
       // It should be 1
       if (diff.size() != count)
       {
          return new FailureResponse(Failure.createAssertionFailure("The size of the new web application deployed should be " + count + ", it is " + diff.size() + " instead." +
                "The previous set was " + keys + " and the new set is " + registry.getKeys()));
       }
       if (!diff.contains(appContext))
       {
          return new FailureResponse(Failure.createErrorFailure("Could not find the requested webapp [" + appContext + "] in the list of depoyed webapps."));
       }
 
       //
       WebApp webApp = registry.getWebApp(appContext);
       if (webApp == null)
       {
          return new FailureResponse(Failure.createAssertionFailure("The web app " + appContext + " was not found"));
       }
       if (!appContext.equals(webApp.getContextPath()))
       {
          return new FailureResponse(Failure.createAssertionFailure("The web app context is not equals to the expected value [" + appContext + "] but has the value " + webApp.getContextPath()));
       }
 
       return null;
    }
 
    class Value
    {
       public String value = "";
    }
 
   public static class TestListener implements AuthenticationListener
    {
       private Value value;
 
       public TestListener(Value value)
       {
          this.value = value;
       }
 
       public void onLogin(AuthenticationEvent ae)
       {
          value.value = "login";
       }
 
       public void onLogout(AuthenticationEvent ae)
       {
          value.value = "logout";
       }
    }
 
 }
