 package org.analogweb.core;
 
 import java.io.File;
 import java.lang.reflect.Method;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 import java.util.StringTokenizer;
 
 import javax.servlet.FilterConfig;
 import javax.servlet.ServletContext;
 
 import org.analogweb.Application;
 import org.analogweb.ApplicationProperties;
 import org.analogweb.ContainerAdaptor;
 import org.analogweb.InvocationMetadata;
 import org.analogweb.InvocationMetadataFactory;
 import org.analogweb.Module;
 import org.analogweb.Modules;
 import org.analogweb.ModulesBuilder;
 import org.analogweb.ModulesConfig;
 import org.analogweb.RequestPathMapping;
 import org.analogweb.exception.MissingRequiredParameterException;
 import org.analogweb.util.ApplicationPropertiesHolder;
 import org.analogweb.util.ClassCollector;
 import org.analogweb.util.ReflectionUtils;
 import org.analogweb.util.ResourceUtils;
 import org.analogweb.util.StringUtils;
 import org.analogweb.util.logging.Log;
 import org.analogweb.util.logging.Logs;
 import org.analogweb.util.logging.Markers;
 
 /**
  * @author snowgoose
  */
 public class WebApplication implements Application {
 
     private static final Log log = Logs.getLog(WebApplication.class);
     private FilterConfig filterConfig;
     protected static final String INIT_PARAMETER_ROOT_COMPONENT_PACKAGES = "application.packages";
     protected static final String INIT_PARAMETER_APPLICATION_SPECIFIER = "application.specifier";
     protected static final String INIT_PARAMETER_APPLICATION_TEMPORARY_DIR = "application.tmpdir";
     protected static final String DEFAULT_PACKAGE_NAME = "org.analogweb";
     private Modules modules;
     private RequestPathMapping requestPathMapping;
     private String applicationSpecifier;
     private ClassLoader classLoader;
 
     public WebApplication(final FilterConfig filterConfig, ClassLoader classLoader) {
         this.filterConfig = filterConfig;
         this.classLoader = classLoader;
         log.log(Markers.BOOT_APPLICATION, "IB000001");
         ApplicationProperties props = configureApplicationProperties(filterConfig);
         Collection<String> actionPackageNames = props.getComponentPackageNames();
         log.log(Markers.BOOT_APPLICATION, "DB000001", actionPackageNames);
         Set<String> modulesPackageNames = new HashSet<String>();
         modulesPackageNames.addAll(actionPackageNames);
         modulesPackageNames.add(DEFAULT_PACKAGE_NAME);
         log.log(Markers.BOOT_APPLICATION, "DB000002", modulesPackageNames);
        initApplication(modulesPackageNames, actionPackageNames, props.geApplicationSpecifier());
         log.log(Markers.BOOT_APPLICATION, "IB000002");
     }
 
     private ApplicationProperties configureApplicationProperties(final FilterConfig filterConfig) {
         ApplicationPropertiesHolder.configure(this, new ApplicationPropertiesHolder.Creator() {
             @Override
             public ApplicationProperties create() {
                 return new ApplicationProperties() {
 
                     private Collection<String> packageNames;
                     private String applicationSpecifier;
                     private String tempDirectoryPath;
 
                     @Override
                     public File getTempDir() {
                         if (this.tempDirectoryPath == null) {
                             this.tempDirectoryPath = createTempDirPath(filterConfig);
 
                         }
                         return new File(tempDirectoryPath);
                     }
 
                     @Override
                     public Collection<String> getComponentPackageNames() {
                         if (this.packageNames == null) {
                             this.packageNames = createUserDefinedPackageNames(filterConfig);
                         }
                         return this.packageNames;
                     }
 
                     @Override
                    public String geApplicationSpecifier() {
                         if (this.applicationSpecifier == null) {
                             this.applicationSpecifier = createApplicationSpecifier(filterConfig);
                         }
                         return this.applicationSpecifier;
                     }
                 };
             }
 
             private Set<String> createUserDefinedPackageNames(FilterConfig filterConfig) {
                 String tokenizedRootPackageNames = filterConfig
                         .getInitParameter(INIT_PARAMETER_ROOT_COMPONENT_PACKAGES);
                 if (StringUtils.isNotEmpty(tokenizedRootPackageNames)) {
                     StringTokenizer tokenizer = new StringTokenizer(tokenizedRootPackageNames, ",");
                     Set<String> packageNames = new HashSet<String>();
                     while (tokenizer.hasMoreTokens()) {
                         packageNames.add(tokenizer.nextToken());
                     }
                     return packageNames;
                 } else {
                     throw new MissingRequiredParameterException(
                             INIT_PARAMETER_ROOT_COMPONENT_PACKAGES);
                 }
             }
 
             private String createApplicationSpecifier(FilterConfig filterConfig) {
                 String specifier = filterConfig
                         .getInitParameter(INIT_PARAMETER_APPLICATION_SPECIFIER);
                 if (StringUtils.isEmpty(specifier)) {
                     return StringUtils.EMPTY;
                 } else {
                     return specifier;
                 }
             }
 
             private String createTempDirPath(FilterConfig filterConfig) {
                 String tmpDirPath = filterConfig
                         .getInitParameter(INIT_PARAMETER_APPLICATION_TEMPORARY_DIR);
                 if (StringUtils.isEmpty(tmpDirPath)) {
                     return System.getProperty("java.io.tmpdir") + "/"
                             + WebApplication.class.getCanonicalName();
                 } else {
                     return tmpDirPath + "/" + WebApplication.class.getCanonicalName();
                 }
             }
         });
         return ApplicationPropertiesHolder.current();
     }
 
     protected void initApplication(Set<String> modulePackageNames,
             Collection<String> invocationPackageNames, String specifier) {
         Collection<Class<?>> moduleClasses = collectClasses(modulePackageNames
                 .toArray(new String[modulePackageNames.size()]));
         ModulesBuilder modulesBuilder = processConfigPreparation(ReflectionUtils
                 .filterClassAsImplementsInterface(ModulesConfig.class, moduleClasses));
 
         ServletContext servletContext = getFilterConfig().getServletContext();
 
         ContainerAdaptor defaultContainer = setUpDefaultContainer(servletContext, moduleClasses);
         Modules modules = modulesBuilder.buildModules(servletContext, defaultContainer);
         setModules(modules);
         log.log(Markers.BOOT_APPLICATION, "DB000003", modules);
         Collection<Class<?>> collectedActionClasses = collectClasses(invocationPackageNames
                 .toArray(new String[invocationPackageNames.size()]));
         setRequestPathMapping(createRequestPathMapping(collectedActionClasses,
                 modules.getInvocationMetadataFactories()));
         setApplicationSpecifier(specifier);
     }
 
     protected ModulesBuilder processConfigPreparation(List<Class<ModulesConfig>> configs) {
         ModulesBuilder modulesBuilder = new DefaultModulesBuilder();
         List<ModulesConfig> moduleConfigInstances = new ArrayList<ModulesConfig>();
         for (Class<ModulesConfig> configClass : configs) {
             ModulesConfig config = ReflectionUtils.getInstanceQuietly(configClass);
             if (config != null) {
                 moduleConfigInstances.add(config);
             }
         }
         Collections.sort(moduleConfigInstances, getModulesConfigComparator());
         for (ModulesConfig config : moduleConfigInstances) {
             log.log(Markers.BOOT_APPLICATION, "IB000003", config);
             modulesBuilder = config.prepare(modulesBuilder);
         }
         return modulesBuilder;
     }
 
     protected Comparator<ModulesConfig> getModulesConfigComparator() {
         return new ModulesConfigComparator();
     }
 
     protected ContainerAdaptor setUpDefaultContainer(ServletContext servletContext,
             Collection<Class<?>> rootModuleClasses) {
         StaticMappingContainerAdaptorFactory factory = new StaticMappingContainerAdaptorFactory();
         StaticMappingContainerAdaptor adaptor = factory.createContainerAdaptor(servletContext);
         for (Class<?> moduleClass : rootModuleClasses) {
             if (Module.class.isAssignableFrom(moduleClass)) {
                 adaptor.register(moduleClass);
             }
         }
         return adaptor;
     }
 
     protected RequestPathMapping createRequestPathMapping(Collection<Class<?>> collectedClasses,
             List<InvocationMetadataFactory> factories) {
         RequestPathMapping mapping = new DefaultRequestPathMapping();
         for (Class<?> clazz : collectedClasses) {
             for (InvocationMetadataFactory factory : factories) {
                 if (factory.containsInvocationClass(clazz)) {
                     Method[] methods = ReflectionUtils.getMethods(clazz);
                     for (Method method : methods) {
                         InvocationMetadata actionMethodMetadata = factory.createInvocationMetadata(
                                 clazz, method);
                         if (actionMethodMetadata != null) {
                             log.log(Markers.BOOT_APPLICATION, "IB000004",
                                     actionMethodMetadata.getDefinedPath(),
                                     actionMethodMetadata.getInvocationClass(),
                                     actionMethodMetadata.getMethodName());
                             mapping.mapInvocationMetadata(actionMethodMetadata.getDefinedPath(),
                                     actionMethodMetadata);
                         }
                     }
                 }
             }
         }
         return mapping;
     }
 
     protected Collection<Class<?>> collectClasses(String[] rootPackageNames) {
         Collection<Class<?>> collectedClasses = new HashSet<Class<?>>();
         for (String packageName : rootPackageNames) {
             for (URL resourceURL : ResourceUtils.findPackageResources(packageName, classLoader)) {
                 for (ClassCollector collector : ClassCollector.DEFAULT_COLLECTORS) {
                     collectedClasses.addAll(collector
                             .collect(packageName, resourceURL, classLoader));
                 }
             }
         }
         return collectedClasses;
     }
 
     @Override
     public RequestPathMapping getRequestPathMapping() {
         return requestPathMapping;
     }
 
     protected void setRequestPathMapping(RequestPathMapping requestPathMapping) {
         this.requestPathMapping = requestPathMapping;
     }
 
     @Override
     public Modules getModules() {
         return this.modules;
     }
 
     protected void setModules(Modules modules) {
         this.modules = modules;
     }
 
     @Override
     public String getApplicationSpecifier() {
         return applicationSpecifier;
     }
 
     protected void setApplicationSpecifier(String suffix) {
         this.applicationSpecifier = suffix;
     }
 
     protected final FilterConfig getFilterConfig() {
         return this.filterConfig;
     }
 
     @Override
     public void dispose() {
         this.classLoader = null;
         this.filterConfig = null;
         this.applicationSpecifier = null;
         this.modules.dispose();
         this.modules = null;
         this.requestPathMapping.dispose();
         this.requestPathMapping = null;
         ApplicationPropertiesHolder.dispose(this);
     }
 
 }
