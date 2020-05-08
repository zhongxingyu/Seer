 /*
  * Copyright 2004-2006 the Seasar Foundation and the Others.
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
 package org.seasar.teeda.core.webapp;
 
 import java.io.InputStream;
 
 import javax.faces.FactoryFinder;
 import javax.faces.application.StateManager;
 import javax.faces.application.ViewHandler;
 import javax.faces.internal.FacesConfigOptions;
 import javax.faces.webapp.FacesServlet;
 import javax.servlet.ServletContext;
 import javax.servlet.ServletContextEvent;
 
 import org.seasar.framework.container.servlet.PortletExtendedS2ContainerListener;
 import org.seasar.framework.log.Logger;
 import org.seasar.framework.util.InputStreamUtil;
import org.seasar.framework.util.StringUtil;
 import org.seasar.teeda.core.JsfConstants;
 import org.seasar.teeda.core.Version;
 import org.seasar.teeda.core.config.faces.FacesConfigBuilder;
 import org.seasar.teeda.core.config.faces.assembler.AssemblerAssembler;
 import org.seasar.teeda.core.config.faces.element.FacesConfig;
 import org.seasar.teeda.core.config.webapp.WebappConfigBuilder;
 import org.seasar.teeda.core.config.webapp.element.WebappConfig;
 import org.seasar.teeda.core.util.DIContainerUtil;
 
 /**
  * @author shot
  * @author manhole
  * @author shinsuke
  */
 public class TeedaPortletExtendedConfigureListener extends
         PortletExtendedS2ContainerListener {
 
     private static final String FACES_INIT_DONE = TeedaPortletExtendedConfigureListener.class
             .getName()
             + ".FACES_INIT_DONE";
 
     private static Logger logger = Logger
             .getLogger(TeedaPortletExtendedConfigureListener.class);
 
     public void contextInitialized(ServletContextEvent event) {
         super.contextInitialized(event);
         printVersion();
         logger.debug("JSF initialize start");
         try {
             initializeFaces(event.getServletContext());
         } catch (RuntimeException e) {
             logger.log(e);
             throw e;
         }
         logger.debug("JSF initialize end");
     }
 
     public void contextDestroyed(ServletContextEvent event) {
         super.contextDestroyed(event);
         FactoryFinder.releaseFactories();
     }
 
     protected void initializeFaces(ServletContext servletContext) {
         Boolean b = (Boolean) servletContext.getAttribute(FACES_INIT_DONE);
         boolean isAlreadyInitialized = (b != null) ? b.booleanValue() : false;
         if (!isAlreadyInitialized) {
             initializeFacesConfigOptions(servletContext);
            initializeFacesConfigCustomOptions(servletContext);
             FacesConfigBuilder facesConfigBuilder = (FacesConfigBuilder) DIContainerUtil
                     .getComponent(FacesConfigBuilder.class);
 
             FacesConfig facesConfig = facesConfigBuilder.createFacesConfigs();
 
             AssemblerAssembler assembler = (AssemblerAssembler) DIContainerUtil
                     .getComponent(AssemblerAssembler.class);
 
             assembler.assembleFactories(facesConfig);
             assembler.assembleApplication(facesConfig);
             assembler.assembleManagedBeans(facesConfig);
             assembler.assmbleNavigationRules(facesConfig);
             assembler.assembleLifecycle(facesConfig);
             assembler.assembleRenderKits(facesConfig);
 
             WebappConfigBuilder webAppConfigBuilder = (WebappConfigBuilder) DIContainerUtil
                     .getComponent(WebappConfigBuilder.class);
 
             InputStream is = null;
             WebappConfig webappConfig = null;
             try {
                 is = servletContext
                         .getResourceAsStream(JsfConstants.WEB_XML_PATH);
                 webappConfig = webAppConfigBuilder.build(is);
             } finally {
                 InputStreamUtil.close(is);
             }
 
             servletContext.setAttribute(WebappConfig.class.getName(),
                     webappConfig);
 
             servletContext.setAttribute(FACES_INIT_DONE, Boolean.TRUE);
         }
 
     }
 
     protected void initializeFacesConfigOptions(ServletContext servletContext) {
         FacesConfigOptions.setConfigFiles(servletContext
                 .getInitParameter(FacesServlet.CONFIG_FILES_ATTR));
         String savingMethod = servletContext
                 .getInitParameter(StateManager.STATE_SAVING_METHOD_PARAM_NAME);
         if (savingMethod != null) {
             FacesConfigOptions
                     .setSavingStateInClient(StateManager.STATE_SAVING_METHOD_CLIENT
                             .equalsIgnoreCase(savingMethod));
         }
         String suffix = servletContext
                 .getInitParameter(ViewHandler.DEFAULT_SUFFIX_PARAM_NAME);
         if (suffix != null) {
             FacesConfigOptions.setDefaultSuffix(suffix);
         } else {
             FacesConfigOptions.setDefaultSuffix(ViewHandler.DEFAULT_SUFFIX);
         }
         String lifecycleId = servletContext
                 .getInitParameter(FacesServlet.LIFECYCLE_ID_ATTR);
         if (lifecycleId != null) {
             FacesConfigOptions.setLifecycleId(lifecycleId);
         }
     }
 
     protected void initializeFacesConfigCustomOptions(
             ServletContext servletContext) {
        String javaScriptNotPermittedPath = servletContext
                .getInitParameter(JsfConstants.JAVASCRIPT_NOT_PERMITTED_PATH);
        if (javaScriptNotPermittedPath != null) {
            String[] paths = StringUtil.split(javaScriptNotPermittedPath, ",");
            FacesConfigOptions.setJavascriptNotPermittedPath(paths);
         }
         String compressState = servletContext
                 .getInitParameter(JsfConstants.COMPRESS_STATE_ATTR);
         if (compressState != null) {
             FacesConfigOptions.setCompressState(compressState
                     .equalsIgnoreCase("true"));
         }
     }
 
     private void printVersion() {
         logger.debug("Teeda version :" + Version.getVersion());
     }
 }
