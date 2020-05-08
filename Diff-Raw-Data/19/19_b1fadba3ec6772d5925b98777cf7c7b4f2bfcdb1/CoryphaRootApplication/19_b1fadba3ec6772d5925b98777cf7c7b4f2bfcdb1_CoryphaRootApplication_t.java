 /*-----------------------------------------------------------------------
   
 Copyright (c) 2007-2010, The University of Manchester, United Kingdom.
 All rights reserved.
 
 Redistribution and use in source and binary forms, with or without 
 modification, are permitted provided that the following conditions are met:
 
  * Redistributions of source code must retain the above copyright notice, 
       this list of conditions and the following disclaimer.
  * Redistributions in binary form must reproduce the above copyright 
       notice, this list of conditions and the following disclaimer in the 
       documentation and/or other materials provided with the distribution.
  * Neither the name of The University of Manchester nor the names of 
       its contributors may be used to endorse or promote products derived 
       from this software without specific prior written permission.
 
 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
 AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
 IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE 
 ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE 
 LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR 
 CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF 
 SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS 
 INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN 
 CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
 ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
 POSSIBILITY OF SUCH DAMAGE.
 
 -----------------------------------------------------------------------*/
 package uk.ac.manchester.rcs.corypha.core;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.concurrent.CopyOnWriteArrayList;
 
 import org.hibernate.cfg.AnnotationConfiguration;
 import org.ini4j.Ini;
 import org.ini4j.InvalidFileFormatException;
 import org.ini4j.Profile.Section;
 import org.restlet.Application;
 import org.restlet.Request;
 import org.restlet.Response;
 import org.restlet.Restlet;
 import org.restlet.representation.Representation;
 import org.restlet.resource.ClientResource;
 import org.restlet.resource.Directory;
 import org.restlet.routing.Router;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import freemarker.template.Configuration;
 import freemarker.template.TemplateModelException;
 
 /**
  * @author Bruno Harbulot
  * 
  */
 public class CoryphaRootApplication extends Application {
     private final static Logger LOGGER = LoggerFactory
             .getLogger(CoryphaRootApplication.class);
 
     public final static String MODULE_CLASSES_CTX_PARAM = "corypha_modules";
 
     public final static String MENU_PROVIDERS_CTX_ATTRIBUTE = "corypha_menu_providers";
 
     public final static String BASE_URL_CTX_PARAM = "corypha_base_url";
 
     public final static String CONFIG_INI_URL_CTX_PARAM = "corypha_config_ini_url";
 
     private final CopyOnWriteArrayList<CoryphaModule> modules = new CopyOnWriteArrayList<CoryphaModule>();
 
     private final CopyOnWriteArrayList<IMenuProvider> menuProviders = new CopyOnWriteArrayList<IMenuProvider>();
 
     private final AnnotationConfiguration hibernateConfiguration = new AnnotationConfiguration();
 
     private void loadConfig(InputStream configIniInputStream)
             throws InvalidFileFormatException, IOException,
             TemplateModelException {
         Ini ini = new Ini(configIniInputStream);
 
         Configuration freemarkerConfig = CoryphaTemplateUtil
                 .getConfiguration(getContext());
         freemarkerConfig.setSharedVariable("maintitle", ini.get("core",
                 "maintitle"));
 
         Section iniSection = ini.get("sidenav");
         CopyOnWriteArrayList<String> menuItemsHtml = new CopyOnWriteArrayList<String>();
         for (String iniMenuItem : iniSection.getAll("item")) {
             menuItemsHtml.add(iniMenuItem);
         }
         freemarkerConfig.setSharedVariable("sidemenuitems", menuItemsHtml);
         freemarkerConfig.setSharedVariable("sidemenutitle", iniSection
                 .get("title"));
     }
 
     @Override
     public Restlet createInboundRoot() {
         String configIniUrl = getContext().getParameters().getFirstValue(
                 CONFIG_INI_URL_CTX_PARAM);
         if (configIniUrl != null) {
             try {
                 ClientResource configResource = new ClientResource(configIniUrl);
                 Representation entity = configResource.get();
                 if (configResource.getStatus().isSuccess() && (entity != null)) {
                     loadConfig(entity.getStream());
                 } else {
                     LOGGER.error(String.format(
                             "Unable to load config file %s.", configIniUrl));
                 }
             } catch (InvalidFileFormatException e) {
                 LOGGER
                         .error(String.format(
                                 "Error while loading config from %s.",
                                 configIniUrl), e);
             } catch (TemplateModelException e) {
                 LOGGER
                         .error(String.format(
                                 "Error while loading config from %s.",
                                 configIniUrl), e);
             } catch (IOException e) {
                 LOGGER
                         .error(String.format(
                                 "Error while loading config from %s.",
                                 configIniUrl), e);
             }
         }
 
         final String baseUrl = getContext().getParameters().getFirstValue(
                 BASE_URL_CTX_PARAM);
         if (baseUrl == null) {
             LOGGER.warn(String.format(
                     "No base url defined (%s context parameter).",
                     BASE_URL_CTX_PARAM));
         } else {
             LOGGER.info(String.format("Using base reference: %s", baseUrl));
         }
 
         Router router = new Router(getContext()) {
             @Override
             public void handle(Request request, Response response) {
                 if (baseUrl == null) {
                     getContext().getParameters().set(BASE_URL_CTX_PARAM,
                             request.getRootRef().toString() + "/");
                 }
                 super.handle(request, response);
             }
         };
         router.setDefaultMatchingMode(Router.MODE_BEST_MATCH);
 
         Map<String, CoryphaApplication> prefixToCmsApps = new HashMap<String, CoryphaApplication>();
 
         String[] cmsApplicationProviderClassNames = getContext()
                 .getParameters().getValuesArray(MODULE_CLASSES_CTX_PARAM);
 
         getContext().getAttributes().put(MENU_PROVIDERS_CTX_ATTRIBUTE,
                 this.menuProviders);
 
         Router htdocsRouter = new Router(getContext());
         router.attach("/htdocs", htdocsRouter);
         loadModule(router, htdocsRouter, prefixToCmsApps, new DefaultModule());
 
         for (String cmsModuleClassName : cmsApplicationProviderClassNames) {
             try {
                 Class<?> cmsModuleClass = Class.forName(cmsModuleClassName);
                 if (CoryphaModule.class.isAssignableFrom(cmsModuleClass)) {
                     CoryphaModule cmsModule = (CoryphaModule) cmsModuleClass
                             .newInstance();
 
                     loadModule(router, htdocsRouter, prefixToCmsApps, cmsModule);
                 } else {
                     LOGGER.error(String.format(
                             "Cannot load %s since it is not a subclass of %s",
                             cmsModuleClassName, CoryphaModule.class));
                 }
             } catch (ClassNotFoundException e) {
                 LOGGER.error(String.format("Cannot load %s: class not found.",
                         cmsModuleClassName), e);
             } catch (InstantiationException e) {
                 LOGGER.error(String.format(
                         "Cannot load %s: cannot instantiate.",
                         cmsModuleClassName), e);
             } catch (IllegalAccessException e) {
                 LOGGER.error(String.format("Cannot load %s: illegal access.",
                         cmsModuleClassName), e);
             }
         }
 
         // TODO remove hard-coding of path.
         Directory htdocsCoreDirectory = new Directory(getContext(),
                 "clap://thread/uk/ac/manchester/rcs/corypha/core/htdocs");
         htdocsRouter.attach("/core", htdocsCoreDirectory);
         Directory htdocsJqueryDatatablesDirectory = new Directory(getContext(),
                 "clap://thread/uk/ac/manchester/rcs/corypha/external/jquery-datatables/htdocs");
         htdocsRouter.attach("/jquery-datatables",
                 htdocsJqueryDatatablesDirectory);
         Directory htdocsJqueryUiDirectory = new Directory(getContext(),
                 "clap://thread/uk/ac/manchester/rcs/corypha/external/jquery-ui/htdocs");
         htdocsRouter.attach("/jquery-ui", htdocsJqueryUiDirectory);
         Directory htdocsJqueryDirectory = new Directory(getContext(),
                 "clap://thread/uk/ac/manchester/rcs/corypha/external/jquery/htdocs");
         htdocsRouter.attach("/jquery", htdocsJqueryDirectory);
 
        InputStream hibernateCfgInputStream = AnnotationConfiguration.class
                .getResourceAsStream("/hibernate.cfg.xml");
        if (hibernateCfgInputStream != null) {
            try {
                hibernateCfgInputStream.close();
            } catch (IOException e) {
                LOGGER
                        .error("Error while trying to close the hibernate.cfg.xml input stream");
            }
            this.hibernateConfiguration.configure();
        }
         getContext().getAttributes().put(
                 HibernateFilter.HIBERNATE_CONFIGURATION_ATTRIBUTE,
                 this.hibernateConfiguration);
 
         return router;
     }
 
     /**
      * @param router
      * @param prefixToCmsApps
      * @param cmsModuleClassName
      * @param cmsModule
      */
     private void loadModule(Router router, Router htdocsRouter,
             Map<String, CoryphaApplication> prefixToCmsApps,
             CoryphaModule cmsModule) {
         this.modules.add(cmsModule);
 
         if (cmsModule instanceof IApplicationProvider) {
             CoryphaApplication cmsApplication = ((IApplicationProvider) cmsModule)
                     .getApplication();
             if (cmsApplication != null) {
                 cmsApplication.setContext(getContext());
                 String autoPrefix = cmsApplication.getAutoPrefix();
                 if (!prefixToCmsApps.containsKey(autoPrefix)) {
                     if (autoPrefix.length() == 0) {
                         router.attachDefault(cmsApplication);
                     } else {
                         router.attach("/" + autoPrefix, cmsApplication);
                         Restlet htdocsRestlet = cmsApplication
                                 .getHtdocsRestlet();
                         if (htdocsRestlet != null) {
                             htdocsRouter
                                     .attach("/" + autoPrefix, htdocsRestlet);
                         }
                     }
                     prefixToCmsApps.put(autoPrefix, cmsApplication);
                     LOGGER.info(String.format(
                             "Loaded application from %s at prefix %s.",
                             cmsModule.getClass(), autoPrefix));
                 } else {
                     LOGGER
                             .error(String
                                     .format(
                                             "Cannot load application from %s: prefix %s already in use.",
                                             cmsModule.getClass(), autoPrefix));
                 }
             } else {
                 LOGGER.warn(String.format(
                         "No application in this application provider: %s.",
                         cmsModule.getClass()));
             }
         }
 
         if (cmsModule instanceof IMenuProvider) {
             this.menuProviders.add((IMenuProvider) cmsModule);
         }
 
         if (cmsModule instanceof IHibernateConfigurationContributor) {
             ((IHibernateConfigurationContributor) cmsModule)
                     .configureHibernate(this.hibernateConfiguration);
         }
     }
 }
