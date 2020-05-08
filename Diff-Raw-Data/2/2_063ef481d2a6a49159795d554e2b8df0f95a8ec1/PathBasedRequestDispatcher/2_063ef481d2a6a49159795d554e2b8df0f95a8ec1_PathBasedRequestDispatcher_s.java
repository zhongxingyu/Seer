 /**
  * Copyright (c) 2010 Ignasi Barrera
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
 
 package org.sjmvc.web;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Properties;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.sjmvc.config.Configuration;
 import org.sjmvc.config.ConfigurationException;
 import org.sjmvc.controller.Controller;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * Base class for the {@link RequestDispatcher} implementations.
  * 
  * @author Ignasi Barrera
  */
 public class PathBasedRequestDispatcher implements RequestDispatcher
 {
     /** The logger. */
     private static final Logger LOGGER = LoggerFactory.getLogger(PathBasedRequestDispatcher.class);
 
     /** Mappings from request path to {@link Controller} class objects. */
     protected Map<String, Class<Controller>> controllerClasses;
 
     /**
      * Creates the request dispatcher.
      * 
      * @throws ConfigurationException If controller mapping configuration cannot be read.
      */
     public PathBasedRequestDispatcher() throws ConfigurationException
     {
         super();
         controllerClasses = new HashMap<String, Class<Controller>>();
         readConfiguration();
     }
 
     @Override
     public void dispatch(HttpServletRequest req, HttpServletResponse resp) throws Exception
     {
        LOGGER.debug("Looking up for a controller to handle request to: {}", req.getRequestURI());
 
         String controllerPath = null;
         Class<Controller> controllerClass = null;
         String requestedPath = getRequestedPath(req);
 
         for (String path : controllerClasses.keySet())
         {
             if (requestedPath.startsWith(path))
             {
                 controllerPath = path;
                 controllerClass = controllerClasses.get(path);
 
                 LOGGER.debug("Using {} controller to handle request to: {}", controllerClass
                     .getName(), req.getRequestURI());
             }
         }
 
         if (controllerClass != null)
         {
             // Instantiate the controller on each request to make it thread-safe
             Controller controller = controllerClass.newInstance();
             String viewName = controller.execute(req, resp);
             String viewPath =
                 Configuration.VIEW_PATH + controllerPath + "/" + viewName
                     + Configuration.VIEW_SUFFIX;
 
             req.setAttribute("currentView", viewPath);
         }
         else
         {
             LOGGER.error("No controller was found to handle request to: {}", req.getRequestURI());
 
             resp.sendError(HttpServletResponse.SC_NOT_FOUND,
                 "No controller was found to handle request to: " + req.getRequestURI());
         }
     }
 
     /**
      * Get the requested path relative to the servlet path.
      * 
      * @param req The request.
      * @return The requested path.
      */
     private String getRequestedPath(final HttpServletRequest req)
     {
         return req.getRequestURI().replaceFirst(req.getContextPath(), "").replaceFirst(
             req.getServletPath(), "");
     }
 
     /**
      * Load configured controller mappings.
      * 
      * @throws Exception If mappings cannot be loaded.
      */
     protected void readConfiguration() throws ConfigurationException
     {
         Properties config = Configuration.getConfiguration();
 
         LOGGER.info("Loading controller mappings...");
 
         for (Object mappingKey : config.keySet())
         {
             String key = (String) mappingKey;
 
             if (Configuration.isControllerProperty(key))
             {
                 String path = config.getProperty(key);
                 String clazz =
                     config.getProperty(key.replace(Configuration.CONTROLLER_PATH_SUFFIX,
                         Configuration.CONTROLLER_CLASS_SUFFIX));
 
                 if (clazz == null)
                 {
                     throw new ConfigurationException("Missing controller class for path: " + path);
                 }
 
                 try
                 {
                     ClassLoader cl = Thread.currentThread().getContextClassLoader();
 
                     @SuppressWarnings("unchecked")
                     Class<Controller> controllerClass =
                         (Class<Controller>) Class.forName(clazz, true, cl);
 
                     controllerClasses.put(path, controllerClass);
 
                     LOGGER.info("Mapping {} to {}", path, controllerClass.getName());
                 }
                 catch (Exception ex)
                 {
                     throw new ConfigurationException("Could not get controller class: " + clazz);
                 }
             }
         }
     }
 }
