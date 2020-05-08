 /**
  *
  * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
  * Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
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
  * in Jahia's FLOSS exception. You should have recieved a copy of the text
  * describing the FLOSS exception, and it is also available here:
  * http://www.jahia.com/license"
  *
  * Commercial and Supported Versions of the program
  * Alternatively, commercial and supported versions of the program may be used
  * in accordance with the terms contained in a separate written agreement
  * between you and Jahia Limited. If you are unsure which license is appropriate
  * for your use, please contact the sales department at sales@jahia.com.
  */
 package org.jahia.modules.external.osgi;
 
 import org.apache.commons.beanutils.BeanUtils;
 import org.eclipse.gemini.blueprint.context.BundleContextAware;
 import org.jahia.data.templates.JahiaTemplatesPackage;
 import org.jahia.exceptions.JahiaInitializationException;
 import org.jahia.osgi.BundleUtils;
 import org.jahia.services.JahiaAfterInitializationService;
 import org.jahia.services.SpringContextSingleton;
 import org.jahia.services.content.JCRStoreProvider;
 import org.jahia.services.content.JCRStoreService;
 import org.osgi.framework.*;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.NoSuchBeanDefinitionException;
 
 import java.io.File;
 import java.lang.reflect.InvocationTargetException;
 import java.util.LinkedHashMap;
 import java.util.Map;
 
 /**
  * Created by IntelliJ IDEA.
  *
  * @author : rincevent
  * @since : JAHIA 6.1
  *        Created : 04/03/13
  */
 public class ExternalProviderActivator implements BundleActivator,JahiaAfterInitializationService, BundleContextAware {
     private static org.slf4j.Logger logger = LoggerFactory.getLogger(ExternalProviderActivator.class);
     private BundleContext context;
 
     /**
      * Called when this bundle is started so the Framework can perform the
      * bundle-specific activities necessary to start this bundle. This method
      * can be used to register services or to allocate any resources that this
      * bundle needs.
      * <p/>
      * <p/>
      * This method must complete and return to its caller in a timely manner.
      *
      * @param context The execution context of the bundle being started.
      * @throws Exception If this method throws an exception, this
      *                   bundle is marked as stopped and the Framework will remove this
      *                   bundle's listeners, unregister all services registered by this
      *                   bundle, and release all services used by this bundle.
      */
     @Override
     public void start(BundleContext context) throws Exception {
         this.context = context;
         context.addBundleListener(new BundleListener() {
             @Override
             public void bundleChanged(BundleEvent event) {
                 if (event.getType() == BundleEvent.STARTED) {
                     mountBundle(event.getBundle());
                 } else if (event.getType() == BundleEvent.STOPPED) {
                     unmountBundle(event.getBundle());
                 }
             }
         });
         Bundle[] bundles = context.getBundles();
         for (Bundle bundle : bundles) {
             if (bundle.getState() == Bundle.ACTIVE) {
                 mountBundle(bundle);
             }
         }
     }
 
     private void mountBundle(Bundle bundle) {
         final JahiaTemplatesPackage pkg = BundleUtils.isJahiaModuleBundle(bundle) ? BundleUtils.getModule(
                 bundle) : null;
         if (null != pkg && pkg.getSourcesFolder() != null) {
             mountSourcesProvider(pkg);
         }
     }
 
     private void unmountBundle(Bundle bundle) {
         final JahiaTemplatesPackage pkg = BundleUtils.isJahiaModuleBundle(bundle) ? BundleUtils.getModule(
                 bundle) : null;
         if (null != pkg && pkg.getSourcesFolder() != null) {
             unmountSourcesProvider(pkg);
         }
     }
 
     /**
      * Called when this bundle is stopped so the Framework can perform the
      * bundle-specific activities necessary to stop the bundle. In general, this
      * method should undo the work that the <code>BundleActivator.start</code>
      * method started. There should be no active threads that were started by
      * this bundle when this bundle returns. A stopped bundle must not call any
      * Framework objects.
      * <p/>
      * <p/>
      * This method must complete and return to its caller in a timely manner.
      *
      * @param context The execution context of the bundle being stopped.
      * @throws Exception If this method throws an exception, the
      *                   bundle is still marked as stopped, and the Framework will remove
      *                   the bundle's listeners, unregister all services registered by the
      *                   bundle, and release all services used by the bundle.
      */
     @Override
     public void stop(BundleContext context) throws Exception {
         Bundle[] bundles = context.getBundles();
         for (Bundle bundle : bundles) {
             if (bundle.getState() == Bundle.ACTIVE) {
                 unmountBundle(bundle);
             }
         }
     }
 
     public void mountSourcesProvider(JahiaTemplatesPackage templatePackage) {
         JCRStoreService jcrStoreService = (JCRStoreService) SpringContextSingleton.getBean("JCRStoreService");
         JCRStoreProvider provider = jcrStoreService.getSessionFactory().getProviders().get(
                 "module-" + templatePackage.getRootFolder() + "-" + templatePackage.getVersion().toString());
         if (provider == null) {
             try {
                 Object dataSource = SpringContextSingleton.getBeanInModulesContext("ModulesDataSourcePrototype");
                 logger.info("Mounting source for bundle "+templatePackage.getName());
                 Map<String, Object> properties = new LinkedHashMap<String, Object>();
                 File oldStructure = new File(templatePackage.getSourcesFolder(), "src/main/webapp");
                 if (oldStructure.exists()) {
                     properties.put("root", templatePackage.getSourcesFolder().toURI().toString() + "src/main/webapp");
                 } else {
                     properties.put("root",
                             templatePackage.getSourcesFolder().toURI().toString() + "src/main/resources");
                 }
                 properties.put("module", templatePackage);
 
                 BeanUtils.populate(dataSource, properties);
 
                 JCRStoreProvider ex = (JCRStoreProvider) SpringContextSingleton.getBeanInModulesContext(
                         "ExternalStoreProviderPrototype");
                 properties.clear();
                 properties.put("key",
                         "module-" + templatePackage.getRootFolder() + "-" + templatePackage.getVersion().toString());
                 properties.put("mountPoint", "/modules/" + templatePackage.getRootFolderWithVersion() + "/sources");
                 properties.put("dataSource", dataSource);
 
                 BeanUtils.populate(ex, properties);
 
                 ex.start();
                start(context);
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
 
     public void unmountSourcesProvider(JahiaTemplatesPackage templatePackage) {
         JCRStoreService jcrStoreService = (JCRStoreService) SpringContextSingleton.getBean("JCRStoreService");
         JCRStoreProvider provider = jcrStoreService.getSessionFactory().getProviders().get(
                 "module-" + templatePackage.getRootFolder() + "-" + templatePackage.getVersion().toString());
         if (provider != null) {
             logger.info("Unmounting source for bundle "+templatePackage.getName());
             provider.stop();
         }
     }
 
     @Override
     public void initAfterAllServicesAreStarted() throws JahiaInitializationException {
         try {
             start(context);
         } catch (Exception e) {
             logger.error(e.getMessage(), e);
         }
     }
 
     @Override
     public void setBundleContext(BundleContext bundleContext) {
         this.context = bundleContext;
         try {
             start(bundleContext);
         } catch (Exception e) {
             logger.error(e.getMessage(), e);
         }
     }
 }
