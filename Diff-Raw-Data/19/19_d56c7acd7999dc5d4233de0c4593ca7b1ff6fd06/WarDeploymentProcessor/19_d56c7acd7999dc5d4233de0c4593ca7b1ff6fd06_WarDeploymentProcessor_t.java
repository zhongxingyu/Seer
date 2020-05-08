 /*
  * JBoss, Home of Professional Open Source.
  * Copyright 2010, Red Hat, Inc., and individual contributors
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
 
 package org.jboss.as.undertow.deployment;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.EventListener;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import javax.servlet.Filter;
 import javax.servlet.Servlet;
 import javax.servlet.ServletContainerInitializer;
 
 import io.undertow.server.handlers.blocking.BlockingHttpServerExchange;
 import io.undertow.servlet.api.ClassIntrospecter;
 import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.ErrorPage;
 import io.undertow.servlet.api.FilterInfo;
 import io.undertow.servlet.api.InstanceFactory;
 import io.undertow.servlet.api.InstanceHandle;
 import io.undertow.servlet.api.ListenerInfo;
 import io.undertow.servlet.api.ServletContainerInitializerInfo;
 import io.undertow.servlet.api.ServletInfo;
 import io.undertow.servlet.api.ThreadSetupAction;
 import io.undertow.servlet.util.ConstructorInstanceFactory;
 import io.undertow.servlet.util.ImmediateInstanceFactory;
 import org.jboss.as.controller.PathElement;
 import org.jboss.as.ee.component.EEModuleDescription;
 import org.jboss.as.naming.ManagedReference;
 import org.jboss.as.server.deployment.Attachments;
 import org.jboss.as.server.deployment.DeploymentPhaseContext;
 import org.jboss.as.server.deployment.DeploymentUnit;
 import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
 import org.jboss.as.server.deployment.DeploymentUnitProcessor;
 import org.jboss.as.server.deployment.SetupAction;
 import org.jboss.as.server.deployment.reflect.DeploymentClassIndex;
 import org.jboss.as.undertow.extension.HttpListenerService;
 import org.jboss.as.undertow.extension.WebSubsystemServices;
 import org.jboss.as.web.deployment.WarMetaData;
 import org.jboss.as.web.deployment.WebAttachments;
 import org.jboss.as.web.deployment.component.ComponentInstantiator;
 import org.jboss.dmr.ModelNode;
 import org.jboss.metadata.ear.jboss.JBossAppMetaData;
 import org.jboss.metadata.ear.spec.EarMetaData;
 import org.jboss.metadata.javaee.spec.ParamValueMetaData;
 import org.jboss.metadata.web.jboss.JBossServletMetaData;
 import org.jboss.metadata.web.jboss.JBossWebMetaData;
 import org.jboss.metadata.web.spec.DispatcherType;
import org.jboss.metadata.web.spec.ErrorPageMetaData;
 import org.jboss.metadata.web.spec.FilterMappingMetaData;
 import org.jboss.metadata.web.spec.FilterMetaData;
 import org.jboss.metadata.web.spec.ListenerMetaData;
 import org.jboss.metadata.web.spec.ServletMappingMetaData;
 import org.jboss.metadata.web.spec.TldMetaData;
 import org.jboss.modules.Module;
 import org.jboss.msc.service.ServiceBuilder;
 import org.jboss.msc.service.ServiceController.Mode;
 import org.jboss.msc.service.ServiceName;
 import org.jboss.msc.service.ServiceRegistryException;
 import org.jboss.msc.service.ServiceTarget;
 import org.jboss.security.SecurityConstants;
 import org.jboss.security.SecurityUtil;
 import org.jboss.vfs.VirtualFile;
 
 import static org.jboss.as.web.WebMessages.MESSAGES;
 
 public class WarDeploymentProcessor implements DeploymentUnitProcessor {
 
     private final String defaultHost;
 
     public WarDeploymentProcessor(final String defaultHost) {
         this.defaultHost = defaultHost;
     }
 
     @Override
     public void deploy(final DeploymentPhaseContext phaseContext) throws DeploymentUnitProcessingException {
         final DeploymentUnit deploymentUnit = phaseContext.getDeploymentUnit();
         final WarMetaData warMetaData = deploymentUnit.getAttachment(WarMetaData.ATTACHMENT_KEY);
         if (warMetaData == null) {
             return;
         }
         String hostName = hostNameOfDeployment(warMetaData, defaultHost);
         processDeployment(hostName, warMetaData, deploymentUnit, phaseContext.getServiceTarget());
     }
 
     static String hostNameOfDeployment(final WarMetaData metaData, final String defaultHost) {
         Collection<String> hostNames = null;
         if (metaData.getMergedJBossWebMetaData() != null) {
             hostNames = metaData.getMergedJBossWebMetaData().getVirtualHosts();
         }
         if (hostNames == null || hostNames.isEmpty()) {
             hostNames = Collections.singleton(defaultHost);
         }
         String hostName = hostNames.iterator().next();
         if (hostName == null) {
             throw MESSAGES.nullHostName();
         }
         return hostName;
     }
 
     @Override
     public void undeploy(final DeploymentUnit context) {
         //AbstractSecurityDeployer<?> deployer = new WarSecurityDeployer();
         //deployer.undeploy(context);
     }
 
     private void processDeployment(final String hostName, final WarMetaData warMetaData, final DeploymentUnit deploymentUnit, final ServiceTarget serviceTarget)
             throws DeploymentUnitProcessingException {
         final VirtualFile deploymentRoot = deploymentUnit.getAttachment(Attachments.DEPLOYMENT_ROOT).getRoot();
         final Module module = deploymentUnit.getAttachment(Attachments.MODULE);
         if (module == null) {
             throw new DeploymentUnitProcessingException(MESSAGES.failedToResolveModule(deploymentRoot));
         }
         final DeploymentClassIndex deploymentClassIndex = deploymentUnit.getAttachment(Attachments.CLASS_INDEX);
         final JBossWebMetaData metaData = warMetaData.getMergedJBossWebMetaData();
         final List<SetupAction> setupActions = deploymentUnit.getAttachmentList(org.jboss.as.ee.component.Attachments.WEB_SETUP_ACTIONS);
         Map<String, ComponentInstantiator> components = deploymentUnit.getAttachment(WebAttachments.WEB_COMPONENT_INSTANTIATORS);
 
         ScisMetaData scisMetaData = deploymentUnit.getAttachment(ScisMetaData.ATTACHMENT_KEY);
 
         // see AS7-2077
         // basically we want to ignore components that have failed for whatever reason
         // if they are important they will be picked up when the web deployment actually starts
         if (components != null) {
             final Set<ServiceName> failed = deploymentUnit.getAttachment(org.jboss.as.ee.component.Attachments.FAILED_COMPONENTS);
             Iterator<Map.Entry<String, ComponentInstantiator>> it = components.entrySet().iterator();
             while (it.hasNext()) {
                 Map.Entry<String, ComponentInstantiator> entry = it.next();
                 boolean skip = false;
                 for (final ServiceName serviceName : entry.getValue().getServiceNames()) {
                     if (failed.contains(serviceName)) {
                         skip = true;
                         break;
                     }
                 }
                 if (skip) {
                     it.remove();
                 }
             }
         } else {
             components = new HashMap<String, ComponentInstantiator>();
         }
 
         DeploymentInfo deploymentInfo = createServletConfig(metaData, deploymentUnit, module, deploymentClassIndex, components, scisMetaData, deploymentRoot);
 
         final String pathName = pathNameOfDeployment(deploymentUnit, metaData);
         deploymentInfo.setContextPath(pathName);
 
         String metaDataSecurityDomain = metaData.getSecurityDomain();
         if (metaDataSecurityDomain == null) {
             metaDataSecurityDomain = getJBossAppSecurityDomain(deploymentUnit);
         }
         if (metaDataSecurityDomain != null) {
             metaDataSecurityDomain = metaDataSecurityDomain.trim();
         }
 
         String securityDomain = metaDataSecurityDomain == null ? SecurityConstants.DEFAULT_APPLICATION_POLICY : SecurityUtil
                 .unprefixSecurityDomain(metaDataSecurityDomain);
 
         // Setup an deployer configured ServletContext attributes
         final List<ServletContextAttribute> attributes = deploymentUnit.getAttachment(ServletContextAttribute.ATTACHMENT_KEY);
 
         try {
             final ServiceName deploymentServiceName = ServiceName.JBOSS.append("undertow", deploymentInfo.getContextPath());
             UndertowDeploymentService service = new UndertowDeploymentService(deploymentInfo);
             final ServiceBuilder<UndertowDeploymentService> builder = serviceTarget.addService(deploymentServiceName, service)
                     .addDependency(WebSubsystemServices.LISTENER.append(defaultHost), HttpListenerService.class, service.getConnector());
 
             deploymentUnit.addToAttachmentList(Attachments.DEPLOYMENT_COMPLETE_SERVICES, deploymentServiceName);
 
             for (Map.Entry<String, ComponentInstantiator> entry : components.entrySet()) {
                 builder.addDependencies(entry.getValue().getServiceNames());
             }
             // add any dependencies required by the setup action
             for (final SetupAction action : setupActions) {
                 builder.addDependencies(action.dependencies());
                 deploymentInfo.addThreadSetupAction(new ThreadSetupAction() {
 
                     @Override
                     public Handle setup(final BlockingHttpServerExchange exchange) {
                         action.setup(Collections.<String, Object>emptyMap());
                         return new Handle() {
                             @Override
                             public void tearDown() {
                                 action.teardown(Collections.<String, Object>emptyMap());
                             }
                         };
                     }
                 });
             }
             /*
             if (metaData.getDistributable() != null) {
                 DistributedCacheManagerFactoryService factoryService = new DistributedCacheManagerFactoryService();
                 DistributedCacheManagerFactory factory = factoryService.getValue();
                 if (factory != null) {
                     ServiceName factoryServiceName = webappServiceName.append("session");
                     webappBuilder.addDependency(DependencyType.OPTIONAL, factoryServiceName, DistributedCacheManagerFactory.class, config.getDistributedCacheManagerFactoryInjector());
 
                     ServiceBuilder<DistributedCacheManagerFactory> factoryBuilder = serviceTarget.addService(factoryServiceName, factoryService);
                     boolean enabled = factory.addDeploymentDependencies(webappServiceName, deploymentUnit.getServiceRegistry(), serviceTarget, factoryBuilder, metaData);
                     factoryBuilder.setInitialMode(enabled ? Mode.ON_DEMAND : Mode.NEVER).install();
                 }
             }*/
 
             // OSGi web applications are activated in {@link WebContextActivationProcessor} according to bundle lifecycle changes
             //if (deploymentUnit.hasAttachment(Attachments.OSGI_MANIFEST)) {
             //    webappBuilder.setInitialMode(Mode.NEVER);
             //    ContextActivator activator = new ContextActivator(webappBuilder.install());
             //    deploymentUnit.putAttachment(ContextActivator.ATTACHMENT_KEY, activator);
             // } else {
             builder.setInitialMode(Mode.ACTIVE);
             builder.install();
             // }
 
         } catch (ServiceRegistryException e) {
             throw new DeploymentUnitProcessingException(MESSAGES.failedToAddWebDeployment(), e);
         }
 
         // Process the web related mgmt information
         //final ModelNode node = deploymentUnit.getDeploymentSubsystemModel("web");
         //node.get(WebDeploymentDefinition.CONTEXT_ROOT.getName()).set("".equals(pathName) ? "/" : pathName);
         //node.get(WebDeploymentDefinition.VIRTUAL_HOST.getName()).set(hostName);
         //processManagement(deploymentUnit, metaData);
     }
 
     static String pathNameOfDeployment(final DeploymentUnit deploymentUnit, final JBossWebMetaData metaData) {
         String pathName;
         if (metaData.getContextRoot() == null) {
             final EEModuleDescription description = deploymentUnit.getAttachment(org.jboss.as.ee.component.Attachments.EE_MODULE_DESCRIPTION);
             if (description != null) {
                 // if there is a EEModuleDescription we need to take into account that the module name may have been overridden
                 pathName = "/" + description.getModuleName();
             } else {
                 pathName = "/" + deploymentUnit.getName().substring(0, deploymentUnit.getName().length() - 4);
             }
         } else {
             pathName = metaData.getContextRoot();
             if ("/".equals(pathName)) {
                 pathName = "";
             } else if (pathName.length() > 0 && pathName.charAt(0) != '/') {
                 pathName = "/" + pathName;
             }
         }
         return pathName;
     }
 
     void processManagement(final DeploymentUnit unit, JBossWebMetaData metaData) {
         for (final JBossServletMetaData servlet : metaData.getServlets()) {
             try {
                 final String name = servlet.getName();
                 final ModelNode node = unit.createDeploymentSubModel("web", PathElement.pathElement("servlet", name));
                 node.get("servlet-class").set(servlet.getServletClass());
                 node.get("servlet-name").set(servlet.getServletName());
             } catch (Exception e) {
                 // Should a failure in creating the mgmt view also make to the deployment to fail?
                 continue;
             }
         }
 
     }
 
     private DeploymentInfo createServletConfig(final JBossWebMetaData mergedMetaData, final DeploymentUnit deploymentUnit, final Module module, final DeploymentClassIndex classReflectionIndex, final Map<String, ComponentInstantiator> components, final ScisMetaData scisMetaData, final VirtualFile deploymentRoot) throws DeploymentUnitProcessingException {
         try {
             final DeploymentInfo d = new DeploymentInfo();
             d.setContextPath(mergedMetaData.getContextRoot());
             d.setDeploymentName(deploymentUnit.getName());
             d.setResourceLoader(new DeploymentResourceLoader(deploymentRoot));
             d.setClassLoader(module.getClassLoader());
 
             //TODO: do this properly
             d.setClassIntrospecter(new ClassIntrospecter() {
                 @Override
                 public <T> InstanceFactory<T> createInstanceFactory(final Class<T> clazz) {
                     try {
                         return new ConstructorInstanceFactory<T>(clazz.getDeclaredConstructor());
                     } catch (NoSuchMethodException e) {
                         throw new RuntimeException(e);
                     }
                 }
             });
 
             final Map<String, List<ServletMappingMetaData>> servletMappings = new HashMap<String, List<ServletMappingMetaData>>();
 
             if (mergedMetaData.getServletMappings() != null) {
                 for (final ServletMappingMetaData mapping : mergedMetaData.getServletMappings()) {
                     List<ServletMappingMetaData> list = servletMappings.get(mapping.getServletName());
                     if (list == null) {
                         servletMappings.put(mapping.getServletName(), list = new ArrayList<ServletMappingMetaData>());
                     }
                     list.add(mapping);
                 }
             }
             if (mergedMetaData.getServlets() != null) {
                 for (final JBossServletMetaData servlet : mergedMetaData.getServlets()) {
                     final ServletInfo s;
                     final ComponentInstantiator creator = components.get(servlet.getServletClass());
 
                    if (servlet.getJspFile() != null) {
                         //TODO: real JSP support
                         s = new ServletInfo(servlet.getName(), (Class<? extends Servlet>) getClass().getClassLoader().loadClass("org.apache.jasper.servlet.JspServlet"));
                     } else if (creator != null) {
                         //TODO: fix this once we have web-common
                         InstanceFactory<Servlet> factory = createInstanceFactory(creator);
                         s = new ServletInfo(servlet.getName(), (Class<? extends Servlet>) classReflectionIndex.classIndex(servlet.getServletClass()).getModuleClass(), factory);
                     } else {
                         s = new ServletInfo(servlet.getName(), (Class<? extends Servlet>) classReflectionIndex.classIndex(servlet.getServletClass()).getModuleClass());
                     }
                     s.setAsyncSupported(servlet.isAsyncSupported())
                             .setJspFile(servlet.getJspFile())
                             .setEnabled(servlet.isEnabled());
                     if (servlet.getRunAs() != null) {
                         s.setRunAs(servlet.getRunAs().getRoleName());
                     }
 
                     List<ServletMappingMetaData> mappings = servletMappings.get(servlet.getName());
                     if (mappings != null) {
                         for (ServletMappingMetaData mapping : mappings) {
                             s.addMappings(mapping.getUrlPatterns());
                         }
                     }
                     if (servlet.getInitParam() != null) {
                         for (ParamValueMetaData initParam : servlet.getInitParam()) {
                             s.addInitParam(initParam.getParamName(), initParam.getParamValue());
                         }
                     }
                     d.addServlet(s);
                 }
             }
 
             if (mergedMetaData.getFilters() != null) {
                 for (final FilterMetaData filter : mergedMetaData.getFilters()) {
                     ComponentInstantiator creator = components.get(filter.getFilterClass());
                     FilterInfo f;
                     if (creator != null) {
                         InstanceFactory<Filter> instanceFactory = createInstanceFactory(creator);
                         f = new FilterInfo(filter.getName(), (Class<? extends Filter>) classReflectionIndex.classIndex(filter.getFilterClass()).getModuleClass(), instanceFactory);
                     } else {
                         f = new FilterInfo(filter.getName(), (Class<? extends Filter>) classReflectionIndex.classIndex(filter.getFilterClass()).getModuleClass());
                     }
                     f.setAsyncSupported(filter.isAsyncSupported());
                     d.addFilter(f);
 
                     if (filter.getInitParam() != null) {
                         for (ParamValueMetaData initParam : filter.getInitParam()) {
                             f.addInitParam(initParam.getParamName(), initParam.getParamValue());
                         }
                     }
                 }
             }
             if (mergedMetaData.getFilterMappings() != null) {
                 for (final FilterMappingMetaData mapping : mergedMetaData.getFilterMappings()) {
                     if (mapping.getUrlPatterns() != null) {
                         for (final String url : mapping.getUrlPatterns()) {
                             if (mapping.getDispatchers() != null && !mapping.getDispatchers().isEmpty()) {
                                 for (DispatcherType dispatcher : mapping.getDispatchers()) {
 
                                     d.addFilterUrlMapping(mapping.getFilterName(), url, javax.servlet.DispatcherType.valueOf(dispatcher.name()));
                                 }
                             } else {
                                 d.addFilterUrlMapping(mapping.getFilterName(), url, javax.servlet.DispatcherType.REQUEST);
                             }
                         }
                     }
                     if (mapping.getServletNames() != null) {
                         for (String servletName : mapping.getServletNames()) {
                             if (mapping.getDispatchers() != null && !mapping.getDispatchers().isEmpty()) {
                                 for (DispatcherType dispatcher : mapping.getDispatchers()) {
                                     d.addFilterServletNameMapping(mapping.getFilterName(), servletName, javax.servlet.DispatcherType.valueOf(dispatcher.name()));
                                 }
                             } else {
                                 d.addFilterServletNameMapping(mapping.getFilterName(), servletName, javax.servlet.DispatcherType.REQUEST);
                             }
                         }
                     }
                 }
             }
 
             for (final Map.Entry<ServletContainerInitializer, Set<Class<?>>> sci : scisMetaData.getHandlesTypes().entrySet()) {
                 final ImmediateInstanceFactory<ServletContainerInitializer> instanceFactory = new ImmediateInstanceFactory<ServletContainerInitializer>(sci.getKey());
                 d.addServletContainerInitalizer(new ServletContainerInitializerInfo(sci.getKey().getClass(), instanceFactory, sci.getValue()));
             }
 
             if (mergedMetaData.getListeners() != null) {
                 for (ListenerMetaData listener : mergedMetaData.getListeners()) {
                     addListener(classReflectionIndex, components, d, listener);
                 }
 
             }
             if (mergedMetaData.getContextParams() != null) {
                 for (ParamValueMetaData param : mergedMetaData.getContextParams()) {
                     d.addInitParameter(param.getParamName(), param.getParamValue());
                 }
             }
 
             if (mergedMetaData.getWelcomeFileList() != null &&
                     mergedMetaData.getWelcomeFileList().getWelcomeFiles() != null) {
                 d.addWelcomePages(mergedMetaData.getWelcomeFileList().getWelcomeFiles());
             } else {
                 d.addWelcomePages("index.html", "index.htm", "index.jsp");
             }
 
            if (mergedMetaData.getErrorPages() != null) {
                for (final ErrorPageMetaData page : mergedMetaData.getErrorPages()) {
                    final ErrorPage errorPage;
                    if (page.getExceptionType() == null || page.getExceptionType().isEmpty()) {
                        errorPage = new ErrorPage(page.getLocation(), Integer.parseInt(page.getErrorCode()));
                    } else {
                        errorPage = new ErrorPage(page.getLocation(), (Class<? extends Throwable>)classReflectionIndex.classIndex(page.getExceptionType()).getModuleClass());
                    }
                    d.addErrorPages(errorPage);
                }
            }

             TldsMetaData tldsMetaData = deploymentUnit.getAttachment(TldsMetaData.ATTACHMENT_KEY);
             if (tldsMetaData != null) {
                 if (tldsMetaData.getTlds() != null) {
                     for (Map.Entry<String, TldMetaData> tld : tldsMetaData.getTlds().entrySet()) {
                         for (final ListenerMetaData listener : tld.getValue().getListeners()) {
                             addListener(classReflectionIndex, components, d, listener);
                         }
                     }
                 }
                 List<TldMetaData> sharedTlds = tldsMetaData.getSharedTlds(deploymentUnit);
                 if (sharedTlds != null) {
                     for (TldMetaData metaData : sharedTlds) {
                         if (metaData.getListeners() != null) {
                             for (final ListenerMetaData listener : metaData.getListeners()) {
                                 addListener(classReflectionIndex, components, d, listener);
                             }
                         }
                     }
                 }
             }
 
             return d;
         } catch (ClassNotFoundException e) {
             throw new DeploymentUnitProcessingException(e);
         }
     }
 
     private void addListener(final DeploymentClassIndex classReflectionIndex, final Map<String, ComponentInstantiator> components, final DeploymentInfo d, final ListenerMetaData listener) throws ClassNotFoundException {
         ComponentInstantiator creator = components.get(listener.getListenerClass());
         ListenerInfo l;
         if (creator != null) {
             InstanceFactory<EventListener> factory = createInstanceFactory(creator);
             l = new ListenerInfo((Class<? extends EventListener>) classReflectionIndex.classIndex(listener.getListenerClass()).getModuleClass(), factory);
         } else {
             l = new ListenerInfo((Class<? extends EventListener>) classReflectionIndex.classIndex(listener.getListenerClass()).getModuleClass());
         }
         d.addListener(l);
     }
 
     private <T> InstanceFactory<T> createInstanceFactory(final ComponentInstantiator creator) {
         return new InstanceFactory<T>() {
             @Override
             public InstanceHandle<T> createInstance() throws InstantiationException {
                 final ManagedReference instance = creator.getReference();
                 return new InstanceHandle<T>() {
                     @Override
                     public T getInstance() {
                         return (T) instance.getInstance();
                     }
 
                     @Override
                     public void release() {
                         instance.release();
                     }
                 };
             }
         };
     }
 
     /**
      * Try to obtain the security domain configured in jboss-app.xml at the ear level if available
      */
     private String getJBossAppSecurityDomain(final DeploymentUnit deploymentUnit) {
         String securityDomain = null;
         DeploymentUnit parent = deploymentUnit.getParent();
         if (parent != null) {
             final EarMetaData jbossAppMetaData = parent.getAttachment(org.jboss.as.ee.structure.Attachments.EAR_METADATA);
             if (jbossAppMetaData instanceof JBossAppMetaData) {
                 securityDomain = ((JBossAppMetaData) jbossAppMetaData).getSecurityDomain();
             }
         }
         return securityDomain;
     }
 }
