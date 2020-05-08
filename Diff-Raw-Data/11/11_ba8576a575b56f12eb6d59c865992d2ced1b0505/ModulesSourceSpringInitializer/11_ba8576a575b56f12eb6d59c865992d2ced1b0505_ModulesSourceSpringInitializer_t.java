 /**
  * This file is part of Jahia, next-generation open source CMS:
  * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
  * of enterprise application convergence - web, search, document, social and portal -
  * unified by the simplicity of web content management.
  *
  * For more information, please visit http://www.jahia.com.
  *
  * Copyright (C) 2002-2013 Jahia Solutions Group SA. All rights reserved.
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; either version 2
  * of the License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
  *
  * As a special exception to the terms and conditions of version 2.0 of
  * the GPL (or any later version), you may redistribute this Program in connection
  * with Free/Libre and Open Source Software ("FLOSS") applications as described
  * in Jahia's FLOSS exception. You should have received a copy of the text
  * describing the FLOSS exception, and it is also available here:
  * http://www.jahia.com/license
  *
  * Commercial and Supported Versions of the program (dual licensing):
  * alternatively, commercial and supported versions of the program may be used
  * in accordance with the terms and conditions contained in a separate
  * written agreement between you and Jahia Solutions Group SA.
  *
  * If you are unsure which license is appropriate for your use,
  * please contact the sales department at sales@jahia.com.
  */
 
 package org.jahia.modules.external.modules.osgi;
 
 import org.apache.commons.beanutils.BeanUtils;
 import org.eclipse.gemini.blueprint.context.BundleContextAware;
 import org.jahia.data.templates.JahiaTemplatesPackage;
 import org.jahia.exceptions.JahiaInitializationException;
 import org.jahia.osgi.BundleUtils;
 import org.jahia.services.JahiaAfterInitializationService;
 import org.jahia.services.SpringContextSingleton;
 import org.jahia.services.content.JCRStoreProvider;
 import org.jahia.services.content.JCRStoreService;
 import org.jahia.services.render.scripting.bundle.BundleScriptResolver;
 import org.osgi.framework.Bundle;
 import org.osgi.framework.BundleContext;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.NoSuchBeanDefinitionException;
 
 import java.io.File;
 import java.lang.reflect.InvocationTargetException;
 import java.util.HashMap;
 import java.util.LinkedHashMap;
 import java.util.Map;
 
 public class ModulesSourceSpringInitializer implements JahiaAfterInitializationService, BundleContextAware {
     private static Logger logger = LoggerFactory.getLogger(ModulesSourceSpringInitializer.class);
     private BundleContext context;
     private static ModulesSourceSpringInitializer instance;
     private JCRStoreService jcrStoreService;
     private Map<String, ModulesSourceHttpServiceTracker> httpServiceTrackers = new HashMap<String, ModulesSourceHttpServiceTracker>();
 
     public static ModulesSourceSpringInitializer getInstance() {
         if (instance == null) {
             instance = new ModulesSourceSpringInitializer();
         }
         return instance;
     }
 
     @Override
     public void initAfterAllServicesAreStarted() throws JahiaInitializationException {
         try {
             logger.info("All services are started. Started mounting modules sources.");
             Bundle[] bundles = context.getBundles();
             for (Bundle bundle : bundles) {
                 if (bundle.getState() == Bundle.ACTIVE) {
                     mountBundle(bundle);
                 }
             }
             logger.info("Done mounting modules sources.");
         } catch (Exception e) {
             logger.error(e.getMessage(), e);
         }
     }
 
     @Override
     public void setBundleContext(BundleContext bundleContext) {
         this.context = bundleContext;
     }
 
     public void mountSourcesProvider(JahiaTemplatesPackage templatePackage) {
         if (context != null) {
             JCRStoreProvider provider = jcrStoreService.getSessionFactory().getProviders().get(
                     "module-" + templatePackage.getRootFolder() + "-" + templatePackage.getVersion().toString());
             if (provider == null) {
                 try {
                     Object dataSource = SpringContextSingleton.getBeanInModulesContext("ModulesDataSourcePrototype");
                     logger.info("Mounting source for bundle {}", templatePackage.getName());
                     Map<String, Object> properties = new LinkedHashMap<String, Object>();
                     properties.put("root",templatePackage.getSourcesFolder().toURI().toString());
                     properties.put("module", templatePackage);
 
                     BeanUtils.populate(dataSource, properties);
 
                     JCRStoreProvider ex = (JCRStoreProvider) SpringContextSingleton.getBeanInModulesContext(
                             "ExternalStoreProviderPrototype");
                     properties.clear();
                     properties.put("key", "module-" + templatePackage.getRootFolder() + "-" +
                                           templatePackage.getVersion().toString());
                     properties.put("mountPoint", "/modules/" + templatePackage.getRootFolderWithVersion() + "/sources");
                     properties.put("dataSource", dataSource);
 
                     BeanUtils.populate(ex, properties);
 
                     ex.start();
                 } catch (IllegalAccessException e) {
                     logger.error(e.getMessage(), e);
                 } catch (InvocationTargetException e) {
                     logger.error(e.getMessage(), e);
                 } catch (JahiaInitializationException e) {
                     logger.error(e.getMessage(), e);
                 } catch (NoSuchBeanDefinitionException e) {
                     logger.debug(e.getMessage(), e);
                 } catch (Exception e) {
                     logger.error(e.getMessage(), e);
                 }
             }
         }
     }
 
     public void unmountSourcesProvider(JahiaTemplatesPackage templatePackage) {
         if (context != null) {
             JCRStoreService jcrStoreService = (JCRStoreService) SpringContextSingleton.getBean("JCRStoreService");
             JCRStoreProvider provider = jcrStoreService.getSessionFactory().getProviders().get(
                     "module-" + templatePackage.getRootFolder() + "-" + templatePackage.getVersion().toString());
             if (provider != null) {
                 logger.info("Unmounting source for bundle {}", templatePackage.getName());
                 provider.stop();
             }
         }
     }
 
     public void mountBundle(Bundle bundle) {
         final JahiaTemplatesPackage pkg = BundleUtils.isJahiaModuleBundle(bundle) ? BundleUtils.getModule(
                 bundle) : null;
         if (null != pkg && pkg.getSourcesFolder() != null) {
             mountSourcesProvider(pkg);
             if (!httpServiceTrackers.containsKey(bundle.getSymbolicName())) {
                 ModulesSourceHttpServiceTracker modulesSourceHttpServiceTracker = new ModulesSourceHttpServiceTracker(pkg);
                 modulesSourceHttpServiceTracker.open(true);
                 httpServiceTrackers.put(bundle.getSymbolicName(), modulesSourceHttpServiceTracker);
             }
         }
     }
 
     public void unmountBundle(Bundle bundle) {
         final JahiaTemplatesPackage pkg = BundleUtils.isJahiaModuleBundle(bundle) ? BundleUtils.getModule(
                 bundle) : null;
         if (null != pkg && pkg.getSourcesFolder() != null) {
             unmountSourcesProvider(pkg);
         }
        httpServiceTrackers.remove(bundle.getSymbolicName());
     }
 
     public ModulesSourceHttpServiceTracker getHttpServiceTracker(String bundleSymbolicName) {
         return httpServiceTrackers.get(bundleSymbolicName);
     }
 
     public void setJcrStoreService(JCRStoreService jcrStoreService) {
         this.jcrStoreService = jcrStoreService;
     }
 }
