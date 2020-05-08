 package org.analogweb.core;
 
 import java.util.ArrayList;
import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
import java.util.Set;
 
 import javax.servlet.ServletContext;
 
 import org.analogweb.AttributesHandler;
 import org.analogweb.ContainerAdaptor;
 import org.analogweb.ContainerAdaptorFactory;
 import org.analogweb.Direction;
 import org.analogweb.DirectionFormatter;
 import org.analogweb.DirectionHandler;
 import org.analogweb.DirectionResolver;
 import org.analogweb.ExceptionHandler;
 import org.analogweb.InvocationFactory;
 import org.analogweb.InvocationMetadataFactory;
 import org.analogweb.InvocationProcessor;
 import org.analogweb.Invoker;
 import org.analogweb.Modules;
 import org.analogweb.ModulesAware;
 import org.analogweb.ModulesBuilder;
 import org.analogweb.MultiModule;
 import org.analogweb.RequestAttributesFactory;
 import org.analogweb.RequestContextFactory;
 import org.analogweb.ResultAttributes;
 import org.analogweb.ResultAttributesFactory;
 import org.analogweb.TypeMapper;
 import org.analogweb.TypeMapperContext;
 import org.analogweb.exception.MissingModuleException;
 import org.analogweb.exception.MissingModulesProviderException;
 import org.analogweb.util.Assertion;
 import org.analogweb.util.Maps;
 import org.analogweb.util.ReflectionUtils;
 
 /**
  * @author snowgoose
  */
 public class DefaultModulesBuilder implements ModulesBuilder {
 
     private Class<? extends ContainerAdaptorFactory<? extends ContainerAdaptor>> modulesProviderClass;
     private Class<? extends ContainerAdaptorFactory<? extends ContainerAdaptor>> invocationInstanceProviderClass;
     private Class<? extends Invoker> invokerClass;
     private Class<? extends InvocationFactory> invocationFactoryClass;
     private Class<? extends DirectionResolver> directionResolverClass;
     private Class<? extends DirectionHandler> directionHandlerClass;
     private Class<? extends ExceptionHandler> exceptionHandlerClass;
     private Class<? extends TypeMapperContext> typeMapperContextClass;
     private Class<? extends RequestContextFactory> requestContextFactoryClass;
     private Class<? extends RequestAttributesFactory> requestAttributesFactoryClass;
     private Class<? extends ResultAttributesFactory> resultAttributesFactoryClass;
     private final List<Class<? extends InvocationProcessor>> invocationProcessorClasses;
     private final List<Class<? extends InvocationMetadataFactory>> invocationMetadataFactoryClasses;
     private final List<Class<? extends AttributesHandler>> attributesHandlerClasses;
     private final Map<Class<? extends Direction>, Class<? extends DirectionFormatter>> directionFormatterClasses;
     private final List<Class<? extends MultiModule>> ignoreClasses;
 
     public DefaultModulesBuilder() {
         this.invocationProcessorClasses = new LinkedList<Class<? extends InvocationProcessor>>();
         this.invocationMetadataFactoryClasses = new LinkedList<Class<? extends InvocationMetadataFactory>>();
         this.attributesHandlerClasses = new LinkedList<Class<? extends AttributesHandler>>();
         this.directionFormatterClasses = Maps.newConcurrentHashMap();
         this.ignoreClasses = new LinkedList<Class<? extends MultiModule>>();
     }
 
     @Override
     public Modules buildModules(final ServletContext servletContext,
             final ContainerAdaptor defaultContainer) {
 
         Assertion.notNull(getModulesProviderClass(), "ModulesProviderClass");
 
         final ContainerAdaptor moduleContainerAdaptor = createModuleContainerAdaptor(
                 servletContext, defaultContainer);
 
         if (moduleContainerAdaptor == null) {
             throw new MissingModulesProviderException();
         }
 
         return new Modules() {
 
             @Override
             public List<InvocationMetadataFactory> getInvocationMetadataFactories() {
                 return getComponentInstances(moduleContainerAdaptor,
                         getInvocationMetadataFactoryClasses());
             }
 
             @Override
             public Invoker getInvoker() {
                 return getComponentInstance(moduleContainerAdaptor, getInvokerClass());
             }
 
             private ContainerAdaptor invocationInstanceProvider;
 
             @Override
             public ContainerAdaptor getInvocationInstanceProvider() {
                 if (this.invocationInstanceProvider == null) {
                     ContainerAdaptorFactory<?> factory = moduleContainerAdaptor
                             .getInstanceOfType(getInvocationInstanceProviderClass());
                     this.invocationInstanceProvider = factory
                             .createContainerAdaptor(servletContext);
                 }
                 return this.invocationInstanceProvider;
             }
 
             @Override
             public List<InvocationProcessor> getInvocationProcessors() {
                 return getComponentInstances(moduleContainerAdaptor,
                         getInvocationProcessorClasses());
             }
 
             @Override
             public InvocationFactory getInvocationFactory() {
                 return getComponentInstance(moduleContainerAdaptor, getInvocationFactoryClass());
             }
 
             @Override
             public DirectionResolver getDirectionResolver() {
                 return getComponentInstance(moduleContainerAdaptor, getDirectionResolverClass());
             }
 
             @Override
             public DirectionHandler getDirectionHandler() {
                 return getComponentInstance(moduleContainerAdaptor, getDirectionHandlerClass());
             }
 
             @Override
             public ExceptionHandler getExceptionHandler() {
                 return getComponentInstance(moduleContainerAdaptor, getExceptionHandlerClass());
             }
 
             @Override
             public TypeMapper findTypeMapper(Class<? extends TypeMapper> clazz) {
                 return getComponentInstance(moduleContainerAdaptor, clazz);
             }
 
             @Override
             public TypeMapperContext getTypeMapperContext() {
                 return getComponentInstance(moduleContainerAdaptor, getTypeMapperContextClass());
             }
 
             @Override
             public ContainerAdaptor getOptionalContainerAdaptor() {
                 return defaultContainer;
             }
 
             @Override
             public RequestContextFactory getRequestContextFactory() {
                 return getComponentInstance(moduleContainerAdaptor, getRequestContextFactoryClass());
             }
 
             @Override
             public RequestAttributesFactory getRequestAttributesFactory() {
                 return getComponentInstance(moduleContainerAdaptor,
                         getRequestAttributesFactoryClass());
             }
 
             @Override
             public List<AttributesHandler> getAttributesHandlers() {
                 return getComponentInstances(moduleContainerAdaptor, getAttributesHandlerClasses());
             }
 
             @Override
             public ResultAttributesFactory getResultAttributesFactory() {
                 return getComponentInstance(moduleContainerAdaptor,
                         getResultAttributesFactoryClass());
             }
 
             @Override
             public ResultAttributes getResultAttributes() {
                 return getResultAttributesFactory().createResultAttributes(
                         getAttributesHandlersMap());
             }
 
             private Map<String, AttributesHandler> attributesHandlerMap;
 
             @Override
             public Map<String, AttributesHandler> getAttributesHandlersMap() {
                 if (this.attributesHandlerMap == null) {
                     this.attributesHandlerMap = Maps.newConcurrentHashMap();
                     for (AttributesHandler resolver : getAttributesHandlers()) {
                         attributesHandlerMap.put(resolver.getScopeName(), resolver);
                     }
                 }
                 return this.attributesHandlerMap;
             }
 
             @Override
             public DirectionFormatter findDirectionFormatter(
                     Class<? extends Direction> mapToDirection) {
                 Class<? extends DirectionFormatter> formatterClass = getDirectionFormatterClass(mapToDirection);
                 if(formatterClass != null){
                     return getComponentInstance(moduleContainerAdaptor, formatterClass);
                 }
                 return null;
             }
 
             private <T> T getComponentInstance(ContainerAdaptor adaptor, Class<T> componentClass) {
                 Assertion.notNull(componentClass, "component-class");
                 T instance = adaptor.getInstanceOfType(componentClass);
                 if (instance == null) {
                     instance = getOptionalContainerAdaptor().getInstanceOfType(componentClass);
                     if (instance == null) {
                         throw new MissingModuleException(componentClass);
                     }
                 }
                 if (instance instanceof ModulesAware) {
                     ((ModulesAware) instance).setModules(this);
                 }
                 return instance;
             }
 
             private <T> List<T> getComponentInstances(ContainerAdaptor adaptor,
                     List<Class<? extends T>> componentClasses) {
                 List<T> instances = new ArrayList<T>();
                Set<String> instanceFQDNs = new HashSet<String>();
                 for (Class<? extends T> clazz : componentClasses) {
                    LinkedList<T> clazzInstances = new LinkedList<T>();
                    clazzInstances.addAll(adaptor.getInstancesOfType(clazz));
                    clazzInstances.addAll(getOptionalContainerAdaptor().getInstancesOfType(clazz));
                     for (T clazzInstance : clazzInstances) {
                        // filter same FQDN's instance.
                         String FQDN = clazzInstance.getClass().getCanonicalName();
                         if (instanceFQDNs.contains(FQDN) == false) {
                             instances.add(clazzInstance);
                         }
                         instanceFQDNs.add(FQDN);
                     }
                 }
                 Iterator<T> itr = instances.iterator();
                 while (itr.hasNext()) {
                     T next = itr.next();
                     for (Class<? extends MultiModule> moduleClass : getIgnoringClasses()) {
                         if (moduleClass.isInstance(next)) {
                             itr.remove();
                         }
                     }
                 }
                 return instances;
             }
 
             @Override
             public void dispose() {
                 setDirectionHandlerClass(null);
                 setDirectionResolverClass(null);
                 setExceptionHandlerClass(null);
                 setInvocationFactoryClass(null);
                 setInvocationInstanceProviderClass(null);
                 setInvokerClass(null);
                 setModulesProviderClass(null);
                 setRequestAttributesFactoryClass(null);
                 setRequestContextFactoryClass(null);
                 setResultAttributesFactoryClass(null);
                 setTypeMapperContextClass(null);
                 this.attributesHandlerMap = null;
             }
 
         };
     }
 
     protected ContainerAdaptor createModuleContainerAdaptor(final ServletContext servletContext,
             final ContainerAdaptor defaultContainer) {
         if (getModulesProviderClass().equals(StaticMappingContainerAdaptorFactory.class)) {
             return defaultContainer;
         } else {
             ContainerAdaptorFactory<? extends ContainerAdaptor> factory = ReflectionUtils
                     .getInstanceQuietly(getModulesProviderClass());
             return factory.createContainerAdaptor(servletContext);
         }
     }
 
     @Override
     public ModulesBuilder setModulesProviderClass(
             Class<? extends ContainerAdaptorFactory<? extends ContainerAdaptor>> modulesProviderClass) {
         this.modulesProviderClass = modulesProviderClass;
         return this;
     }
 
     @Override
     public ModulesBuilder addInvocationMetadataFactoriesClass(
             Class<? extends InvocationMetadataFactory> actionMethodMetadataFactoryClass) {
         this.invocationMetadataFactoryClasses.add(actionMethodMetadataFactoryClass);
         return this;
     }
 
     @Override
     public ModulesBuilder setInvokerClass(Class<? extends Invoker> actionInvokerClass) {
         this.invokerClass = actionInvokerClass;
         return this;
     }
 
     @Override
     public ModulesBuilder setInvocationInstanceProviderClass(
             Class<? extends ContainerAdaptorFactory<? extends ContainerAdaptor>> actionInstanceProviderClass) {
         this.invocationInstanceProviderClass = actionInstanceProviderClass;
         return this;
     }
 
     @Override
     public ModulesBuilder setInvocationFactoryClass(
             Class<? extends InvocationFactory> actionInvocationFactoryClass) {
         this.invocationFactoryClass = actionInvocationFactoryClass;
         return this;
     }
 
     @Override
     public ModulesBuilder setDirectionResolverClass(
             Class<? extends DirectionResolver> actionResultResolverClass) {
         this.directionResolverClass = actionResultResolverClass;
         return this;
     }
 
     @Override
     public ModulesBuilder setDirectionHandlerClass(
             Class<? extends DirectionHandler> actionResultHandlerClass) {
         this.directionHandlerClass = actionResultHandlerClass;
         return this;
     }
 
     @Override
     public ModulesBuilder setTypeMapperContextClass(
             Class<? extends TypeMapperContext> typeMapperContextClass) {
         this.typeMapperContextClass = typeMapperContextClass;
         return this;
     }
 
     @Override
     public ModulesBuilder setExceptionHandlerClass(
             Class<? extends ExceptionHandler> exceptionHandlerClass) {
         this.exceptionHandlerClass = exceptionHandlerClass;
         return this;
     }
 
     @Override
     public ModulesBuilder setRequestContextFactoryClass(
             Class<? extends RequestContextFactory> requestContextFactoryClass) {
         this.requestContextFactoryClass = requestContextFactoryClass;
         return this;
     }
 
     @Override
     public ModulesBuilder addInvocationProcessorClass(
             Class<? extends InvocationProcessor> actionInvocationProcessorClass) {
         this.invocationProcessorClasses.add(actionInvocationProcessorClass);
         return this;
     }
 
     @Override
     public ModulesBuilder setRequestAttributesFactoryClass(
             Class<? extends RequestAttributesFactory> requestAttributesFactoryClass) {
         this.requestAttributesFactoryClass = requestAttributesFactoryClass;
         return this;
     }
 
     @Override
     public ModulesBuilder addAttributesHandlerClass(
             Class<? extends AttributesHandler> requestAttributesResolverClass) {
         this.attributesHandlerClasses.add(requestAttributesResolverClass);
         return this;
     }
 
     protected Class<? extends ContainerAdaptorFactory<? extends ContainerAdaptor>> getModulesProviderClass() {
         return this.modulesProviderClass;
     }
 
     protected List<Class<? extends InvocationMetadataFactory>> getInvocationMetadataFactoryClasses() {
         return this.invocationMetadataFactoryClasses;
     }
 
     protected Class<? extends Invoker> getInvokerClass() {
         return this.invokerClass;
     }
 
     protected Class<? extends ContainerAdaptorFactory<?>> getInvocationInstanceProviderClass() {
         return this.invocationInstanceProviderClass;
     }
 
     protected Class<? extends InvocationFactory> getInvocationFactoryClass() {
         return this.invocationFactoryClass;
     }
 
     protected Class<? extends DirectionResolver> getDirectionResolverClass() {
         return this.directionResolverClass;
     }
 
     protected Class<? extends DirectionHandler> getDirectionHandlerClass() {
         return this.directionHandlerClass;
     }
 
     protected Class<? extends ExceptionHandler> getExceptionHandlerClass() {
         return this.exceptionHandlerClass;
     }
 
     protected Class<? extends TypeMapperContext> getTypeMapperContextClass() {
         return this.typeMapperContextClass;
     }
 
     protected Class<? extends RequestContextFactory> getRequestContextFactoryClass() {
         return this.requestContextFactoryClass;
     }
 
     protected List<Class<? extends InvocationProcessor>> getInvocationProcessorClasses() {
         return this.invocationProcessorClasses;
     }
 
     protected Class<? extends RequestAttributesFactory> getRequestAttributesFactoryClass() {
         return requestAttributesFactoryClass;
     }
 
     protected List<Class<? extends AttributesHandler>> getAttributesHandlerClasses() {
         return attributesHandlerClasses;
     }
 
     protected Class<? extends ResultAttributesFactory> getResultAttributesFactoryClass() {
         return resultAttributesFactoryClass;
     }
 
     protected Class<? extends DirectionFormatter> getDirectionFormatterClass(
             Class<? extends Direction> mapToDirection) {
         return this.directionFormatterClasses.get(mapToDirection);
     }
 
     protected List<Class<? extends MultiModule>> getIgnoringClasses() {
         return this.ignoreClasses;
     }
 
     @Override
     public ModulesBuilder setResultAttributesFactoryClass(
             Class<? extends ResultAttributesFactory> resultAttributesFactoryClass) {
         this.resultAttributesFactoryClass = resultAttributesFactoryClass;
         return this;
     }
 
     @Override
     public ModulesBuilder clearInvocationMetadataFactoriesClass() {
         this.invocationMetadataFactoryClasses.clear();
         return this;
     }
 
     @Override
     public ModulesBuilder clearInvocationProcessorClass() {
         this.invocationProcessorClasses.clear();
         return this;
     }
 
     @Override
     public ModulesBuilder clearAttributesHanderClass() {
         this.attributesHandlerClasses.clear();
         return this;
     }
 
     @Override
     public ModulesBuilder addDirectionFormatterClass(
             Class<? extends Direction> mapToDirectionClass,
             Class<? extends DirectionFormatter> directionFormatterClass) {
         this.directionFormatterClasses.put(mapToDirectionClass, directionFormatterClass);
         return this;
     }
 
     @Override
     public ModulesBuilder clearDirectionFormatterClass() {
         this.directionFormatterClasses.clear();
         return this;
     }
 
     @Override
     public ModulesBuilder ignore(Class<? extends MultiModule> multiModuleClass) {
         Assertion.notNull(multiModuleClass, MultiModule.class.getCanonicalName());
         this.ignoreClasses.add(multiModuleClass);
         return this;
     }
 
 }
