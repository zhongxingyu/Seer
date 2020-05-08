 /******************************************************************************
  * JBoss, a division of Red Hat                                               *
  * Copyright 2011, Red Hat Middleware, LLC, and individual                    *
  * contributors as indicated by the @authors tag. See the                     *
  * copyright.txt in the distribution for a full listing of                    *
  * individual contributors.                                                   *
  *                                                                            *
  * This is free software; you can redistribute it and/or modify it            *
  * under the terms of the GNU Lesser General Public License as                *
  * published by the Free Software Foundation; either version 2.1 of           *
  * the License, or (at your option) any later version.                        *
  *                                                                            *
  * This software is distributed in the hope that it will be useful,           *
  * but WITHOUT ANY WARRANTY; without even the implied warranty of             *
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU           *
  * Lesser General Public License for more details.                            *
  *                                                                            *
  * You should have received a copy of the GNU Lesser General Public           *
  * License along with this software; if not, write to the Free                *
  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA         *
  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.                   *
  ******************************************************************************/
 package org.gatein.wci.spi;
 
 import static org.jboss.unit.api.Assert.fail;
 
 import java.io.IOException;
 import java.util.HashSet;
 import java.util.Set;
 
 import javax.servlet.ServletContext;
 import javax.servlet.ServletException;
 
 import org.gatein.wci.ServletContainer;
 import org.gatein.wci.ServletContextDispatcher;
 import org.gatein.wci.ServletTestCase;
 import org.gatein.wci.TestServlet;
 import org.gatein.wci.WebApp;
 import org.gatein.wci.WebAppRegistry;
 import org.gatein.wci.WebRequest;
 import org.gatein.wci.WebResponse;
 import org.gatein.wci.impl.DefaultServletContainerFactory;
 import org.gatein.wci.spi.callbacks.ExceptionCallback;
 import org.gatein.wci.spi.callbacks.NormalCallback;
 import org.jboss.unit.Failure;
 import org.jboss.unit.FailureType;
 import org.jboss.unit.driver.DriverCommand;
 import org.jboss.unit.driver.DriverResponse;
 import org.jboss.unit.driver.response.EndTestResponse;
 import org.jboss.unit.driver.response.FailureResponse;
 import org.jboss.unit.remote.driver.handler.deployer.response.DeployResponse;
 import org.jboss.unit.remote.driver.handler.deployer.response.UndeployResponse;
 import org.jboss.unit.remote.driver.handler.http.response.InvokeGetResponse;
 
 /**
  * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
  * @version $Revision$
  */
 public class SPINativeSkipTestCase extends ServletTestCase
 {
 
    /** . */
    private WebAppRegistry registry;
 
    /** . */
    private Set<String> keys;
 
    /** . */
    private ServletContainer container;
 
 
    @Override
    public DriverResponse service(TestServlet testServlet, WebRequest req, WebResponse resp) throws ServletException,
          IOException
    {
       if (getRequestCount() == 2)
       {
       // Check that this web app is here
          String key = req.getContextPath();
          if (!keys.contains(key))
          {
             fail("The current test web app with key " + key + " is not seen as deployed among " + keys);
          }
 
          // Should try
          ServletContext appContext = testServlet.getServletContext().getContext("/test-native-skip-with-gateinservlet-app");
 
          //
          if (appContext == null)
          {
             fail("Cannot get access to the /test-native-skip-with-gateinservlet-app context");
          }
 
          //
          WebApp webApp = registry.getWebApp("/test-native-skip-with-gateinservlet-app");
          NormalCallback cb1 = new NormalCallback(appContext, webApp.getClassLoader());
          Exception ex = new Exception();
          ExceptionCallback cb2 = new ExceptionCallback(appContext, ex, ex);
          Error err = new Error();
          ExceptionCallback cb3 = new ExceptionCallback(appContext, err, err);
          RuntimeException rex = new RuntimeException();
          ExceptionCallback cb4 = new ExceptionCallback(appContext, rex, rex);
          IOException ioe = new IOException();
          ExceptionCallback cb5 = new ExceptionCallback(appContext, ioe, ioe);
 
          //
          ServletContextDispatcher dispatcher = new ServletContextDispatcher(req, resp, container);
          DriverResponse response = cb1.test(null, dispatcher);
          response = cb2.test(response, dispatcher);
          response = cb3.test(response, dispatcher);
          response = cb4.test(response, dispatcher);
          response = cb5.test(response, dispatcher);
 
          return new UndeployResponse("test-native-skip-with-gateinservlet-app.war");
       }
 
       else if (getRequestCount() == 3)
       {
          return new UndeployResponse("test-native-skip-app.war");
       }
       else if (getRequestCount() == 4)
       {
          return new EndTestResponse();
       }
       else
       {
          return new FailureResponse(Failure.createAssertionFailure("Test not expected to reach RequestCount of " + getRequestCount()));  
       }
    }
 
    @Override
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
          
          // Deploy the web app with init param of gatein.wci.native.DisableRegistration set to true
          return new DeployResponse("test-native-skip-app.war");
       }
       else if (getRequestCount() == 0)
       {
          //make sure the test-native-skip-app.war is actually skipped
          if (registry.getWebApp("/test-native-skip-app") != null)
          {
             return new FailureResponse(Failure.createAssertionFailure("The test-native-skip-app.war should not be seen by the native implemetentation."));
          }
          
          // Compute the difference with the previous deployed web apps
          Set diff = new HashSet<String>(registry.getKeys());
          diff.removeAll(keys);
          
          // It should be 0, since the test-native-skip-app.war should not get registered by the native implementation
          if (diff.size() != 0)
          {
             return new FailureResponse(Failure.createAssertionFailure("The size of the new web application deployed should be 0, it is " + diff.size() + " instead." +
             "The previous set was " + keys + " and the new set is " + registry.getKeys()));
          }
          
          return new DeployResponse("test-native-skip-with-gateinservlet-app.war");
       }
       else if (getRequestCount() == 1)
       {
          // Compute the difference with the previous deployed web apps
          Set diff = new HashSet<String>(registry.getKeys());
          diff.removeAll(keys);
 
          // It should be 1
          if (diff.size() != 1)
          {
             return new FailureResponse(Failure.createAssertionFailure("The size of the new web application deployed should be 1, it is " + diff.size() + " instead." +
             "The previous set was " + keys + " and the new set is " + registry.getKeys()));
          }
          
          WebApp webapp = registry.getWebApp("/test-native-skip-with-gateinservlet-app");
          //make sure the test-native-skip-with-gateinservlet-app.war is picked up
          if (webapp== null)
          {
             return new FailureResponse(Failure.createAssertionFailure("The test-native-skip-with-gateinservler-app.war should be seen."));
          }
          if (!webapp.getContextPath().equals("/test-native-skip-with-gateinservlet-app"))
          {
             return new FailureResponse(Failure.createAssertionFailure("The web app context is not equals to the expected value but has the value " + webapp.getContextPath()));
          }
          return new InvokeGetResponse("/test-spi-server");
       }
       else
       {
          return new FailureResponse(new Failure("Test not expected to reach RequestCount of " + getRequestCount(), FailureType.ERROR));
       }
    }
 
 }
 
