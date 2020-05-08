 /*
  * Copyright 2004-2007 the Seasar Foundation and the Others.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
  * either express or implied. See the License for the specific language
  * governing permissions and limitations under the License.
  */
 package org.seasar.velocity.tools;
 
 import javax.servlet.ServletConfig;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.velocity.Template;
 import org.apache.velocity.context.Context;
 import org.apache.velocity.tools.view.ToolboxManager;
 import org.apache.velocity.tools.view.servlet.VelocityViewServlet;
 import org.seasar.framework.container.ComponentNotFoundRuntimeException;
 import org.seasar.framework.container.S2Container;
 import org.seasar.framework.container.factory.SingletonS2ContainerFactory;
 
 /**
  * @author <a href="mailto:sato@ouobpo.org">Sato Tadayosi</a>
  * @author TANIGUCHI Hikaru
  * 
  * $Id$
  */
 public class S2VelocityViewServlet extends VelocityViewServlet {
     /**
      * VelocityLayoutViewServlet互換のプロパティキー。 このプロパティキーの
      * vmファイル名をvelocity.propertiesに書いておけばよい
      */
     private static final String PROPERTY_ERROR_TEMPLATE = "tools.view.servlet.error.template";
 
     /** TOOLBOX_KEY servlet初期化パラメータにこの値が指定されている場合はコンテナ委任型になる */
     private static final String TOOLBOX_KEY_CONTAINER_MANAGED = "container";
 
     protected String errorTemplate;
 
     public void init(ServletConfig config) throws ServletException {
         super.init(config);
         errorTemplate = getVelocityProperty(PROPERTY_ERROR_TEMPLATE, null);
     }
 
     /**
      * Overrides {@link S2VelocityViewServlet} to change toolboxManager
      * implementation from ServletToolboxManager to S2ServletToolboxManager.
      * 
      * Initializes the S2ServletToolboxManager for this servlet's toolbox (if
      * any).
      * 
      * @param config
      *            servlet configuration
      */
     protected void initToolbox(ServletConfig config) throws ServletException {
         /* check the servlet config and context for a toolbox param */
         String file = findInitParameter(config, TOOLBOX_KEY);
 
         /* if we have a toolbox, get a manager for it */
         if (file != null) {
             if (TOOLBOX_KEY_CONTAINER_MANAGED.equals(file)) {
                getVelocityEngine().getLog().info("S2VelocityViewServlet: trying to init ContainerBasedToolboxManager");
                 initContainerBasedToolboxManager();
             } else {
                 toolboxManager = S2ServletToolboxManager.getInstance(getServletContext(), file); // NOTE
                                                                                                     // changed
                                                                                                     // here.
             }
         } else {
            getVelocityEngine().getLog().info("VelocityViewServlet: No toolbox entry in configuration.");
         }
     }
 
     protected void initContainerBasedToolboxManager() {
         S2Container container = SingletonS2ContainerFactory.getContainer();
         if (container != null) {
             try {
                 toolboxManager = (ToolboxManager) container.getComponent(ToolboxManager.class);
                getVelocityEngine().getLog().info("S2VelocityViewServlet: Toolbox found in S2Container");
             } catch (ComponentNotFoundRuntimeException e) {
                 toolboxManager = new ContainerBasedToolboxManager();
                getVelocityEngine().getLog().info("S2VelocityViewServlet: Toolbox not found in S2Container, using default ContainerBasedToolboxManager.");
             }
         } else {
            getVelocityEngine().getLog().error("S2VelocityViewServlet: no container found but container managed specified.");
         }
     }
 
     protected void error(HttpServletRequest request, HttpServletResponse response, Exception e)
             throws ServletException {
         if (errorTemplate == null) {
             super.error(request, response, e);
         }
 
         try {
             // get a velocity context
             Context ctx = createContext(request, response);
 
             // retrieve and render the error template
             Template et = getTemplate(errorTemplate);
             mergeTemplate(et, ctx, response);
 
         } catch (Exception e2) {
             // then punt the original to a higher authority
             super.error(request, response, e);
         }
     }
 
 }
