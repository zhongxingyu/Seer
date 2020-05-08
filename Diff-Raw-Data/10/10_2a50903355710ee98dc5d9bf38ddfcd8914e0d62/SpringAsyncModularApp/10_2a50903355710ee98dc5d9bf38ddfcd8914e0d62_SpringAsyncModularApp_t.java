 package org.nohope.spring.app;
 
 import org.apache.commons.lang3.tuple.ImmutablePair;
 import org.apache.xbean.finder.ResourceFinder;
 import org.nohope.logging.Logger;
 import org.nohope.logging.LoggerFactory;
 import org.springframework.beans.factory.NoSuchBeanDefinitionException;
 import org.springframework.context.ConfigurableApplicationContext;
 import org.springframework.core.io.ClassPathResource;
 import org.nohope.IMatcher;
 import org.nohope.ITranslator;
 import org.nohope.app.AsyncApp;
 import org.nohope.reflection.IntrospectionUtils;
 import org.nohope.typetools.collection.CollectionUtils;
 
 import javax.annotation.Nonnull;
 import javax.annotation.Nullable;
 import javax.inject.Inject;
 import java.io.File;
 import java.lang.annotation.Annotation;
 import java.lang.reflect.Constructor;
 import java.lang.reflect.ParameterizedType;
 import java.lang.reflect.Type;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import java.util.LinkedHashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import java.util.Set;
 
 import static org.nohope.reflection.IntrospectionUtils.instanceOf;
 import static org.nohope.reflection.IntrospectionUtils.searchConstructors;
 import static org.nohope.spring.SpringUtils.*;
 
 /**
  * <b>Technical background</b>
  * <p/>
  * This class assumes following classpath hierarchy:
  * <pre>
  *     classpath:
  *          META-INF/[appMetaInfNamespace]/[appName]-defaultContext.xml // mandatory
  *          [appName]-context.xml // <-- optional, will override default service context
  *
  *          META-INF/[moduleMetaInfNamespace]/[moduleName1].properties // <-- module descriptor, mandatory
  *          META-INF/[moduleMetaInfNamespace]/[moduleName1]-defaultContext.xml // mandatory
  *          [moduleName1]-context.xml // <-- optional, will override default module context
  * </pre>
  * <p/>
  * Module descriptor is simple properties file content should looks like:
  * <pre>
  *     class=com.mypackage.ConcreteModule  // reserved
  *
  *     // any additional parameters
  *     key1=val1
  *     key2=val2
  * </pre>
  * <p/>
  * Module name retrieved from properties file name.
  * <p/>
  * <b>Modules cross-dependencies injecting</b>
  * <p/>
  * Modules may request specific sub-modules. Consider next inheritance scheme
  * <pre>
  *       Module -- C
  *         /\
  *        /  \
  *       A    B
  * </pre>
  *
  * And three classes
  * <pre>
  *     class Module1 implements A {}
  *     class Module2 implements A {}
  *     class Module3 implements B {
  *         // &#064;Dependency could be amended in case of constructor have only one IDependencyProvider parameter
  *         {@link Inject &#064;Inject}
  *         Module3({@link IDependencyProvider IDependencyProvider&lt;A&gt;} prop) {
  *             prop; // will contain Module1 & Module2
  *         }
  *     }
  *     class Module4 implements C {
  *         // &#064;Dependency should be declared for each IDependencyProvider parameter if
  *         // there is more than one such constructor parameter
  *         {@link Inject &#064;Inject}
  *         Module3({@link Dependency &#064;Dependency(A.class)} {@link IDependencyProvider IDependencyProvider}&lt;A&gt; p1,
  *                 {@link Dependency &#064;Dependency(B.class)} {@link IDependencyProvider IDependencyProvider}&lt;B&gt; p2) {
  *             prop1; // will contain Module1 & Module2
  *             prop2; // will contain Module3
  *         }
  *     }
  * </pre>
  * <p/>
  * <b>Start procedure</b>
  * <p/>
  * On {@link  #start() start} app will search for it's default service context, override it
  * with optionally passed service context. Then it searches for module definitions,
  * doing the same operations with their context (<b>note:</b> service context will be passed as a
  * parent to module context, so module will have access to all app beans) invoking appropriate
  * methods ({@link HandlerWithStorage#onModuleDiscovered(Class, ConfigurableApplicationContext, Properties, String)
  * onModuleDiscovered},
  * {@link HandlerWithStorage#onModuleCreated(Object, ConfigurableApplicationContext, Properties, String)
  * onModuleDiscovered}). After all
  * {@link HandlerWithStorage#onModuleDiscoveryFinished()
  * onModuleDiscoveryFinished}
  * will be executed.
  *
  * @param <M> module interface
  * @author <a href="mailto:ketoth.xupack@gmail.com">ketoth xupack</a>
  * @since 7/27/12 3:31 PM
  */
 public final class SpringAsyncModularApp<M, H extends Handler<M>> extends AsyncApp {
     private static final Logger LOG = LoggerFactory.getLogger(SpringAsyncModularApp.class);
 
     private static final String META_INF = "META-INF/";
     private static final String DEFAULT_MODULE_FOLDER = "module/";
     private static final String DEFAULT_CONTEXT_POSTFIX = "-defaultContext.xml";
     private static final String PROPERTIES_EXTENSION = ".properties";
     private static final String CONTEXT_POSTFIX = "-context.xml";
 
     private final ResourceFinder finder = new ResourceFinder(META_INF);
 
     private final Class<? extends M> targetModuleClass;
     private final String appMetaInfNamespace;
     private final String moduleMetaInfNamespace;
     private final String appName;
     private final ConfigurableApplicationContext ctx;
     private final H handler;
 
     /**
      * {@link SpringAsyncModularApp} constructor.
      *
      * @param targetModuleClass      module type
      * @param appName                this app name (if {@code null} passed then class name with lowercase
      *                               first letter will be used)
      * @param appMetaInfNamespace    path relative to {@code META-INF} folder in classpath
      *                               to discover app context (if {@code null} passed then package path of app class will be used)
      * @param moduleMetaInfNamespace path relative to {@code META-INF} folder in classpath
      *                               to discover module context (if {@code null} passed then package path of targetClass
      *                               parameter will be used will be used)
      */
     public SpringAsyncModularApp(@Nonnull final Class<? extends M> targetModuleClass,
                                  @Nonnull final Class<? extends H> handlerClass,
                                  @Nullable final String appName,
                                  @Nullable final String appMetaInfNamespace,
                                  @Nullable final String moduleMetaInfNamespace) {
         this.targetModuleClass = targetModuleClass;
         this.appMetaInfNamespace =
                 appMetaInfNamespace == null
                         ? getPackage(handlerClass)
                         : toValidPath(appMetaInfNamespace);
         this.moduleMetaInfNamespace =
                 moduleMetaInfNamespace == null
                         ? getPackage(handlerClass) + DEFAULT_MODULE_FOLDER
                         : toValidPath(moduleMetaInfNamespace);
         this.appName =
                 appName == null
                         ? lowercaseClassName(handlerClass)
                         : appName;
 
         ctx = overrideRule(null, this.appMetaInfNamespace, this.appName);
 
         handler = instantiate(ctx, handlerClass);
         handler.setAppContext(ctx);
         handler.setAppName(appName);
         setProperties(ctx, handler);
     }
 
     /**
      * {@link SpringAsyncModularApp} constructor. Service name and search paths will
      * found reflectively (see {@link #SpringAsyncModularApp(Class, Class, String, String, String)}).
      *
      * @param targetModuleClass module type
      */
     public SpringAsyncModularApp(@Nonnull final Class<? extends M> targetModuleClass,
                                  @Nonnull final Class<? extends H> handlerClass) {
         this(targetModuleClass, handlerClass, null, null, null);
     }
 
     public SpringAsyncModularApp(@Nonnull final Class<? extends M> targetModuleClass,
                                  @Nonnull final Class<? extends H> handlerClass,
                                  @Nonnull final String appName) {
         this(targetModuleClass, handlerClass, appName, null, null);
     }
 
     /**
      * Searches for plugin definitions in classpath and instantiates them.
      */
     @Override
     protected void onStart() throws Exception {
         final Map<String, Properties> properties = finder.mapAvailableProperties(moduleMetaInfNamespace);
 
         final List<ModuleDescriptor<Class<? extends M>>> descriptors = new ArrayList<>();
         for (final Map.Entry<String, Properties> e : properties.entrySet()) {
             final String moduleFileName = e.getKey();
             final Properties moduleProperties = e.getValue();
 
             if (!moduleFileName.endsWith(PROPERTIES_EXTENSION)) {
                 continue;
             }
             final String moduleName = moduleFileName.substring(0, moduleFileName.lastIndexOf('.'));
 
             if (!moduleProperties.containsKey("class")) {
                 LOG.warn("Unable to process module '{}' - no class property found", moduleName);
                 continue;
             }
 
             final String moduleClassName = moduleProperties.getProperty("class");
             final Class<?> moduleClazz;
             try {
                 moduleClazz = Class.forName(moduleClassName);
 
                 if (!targetModuleClass.isAssignableFrom(moduleClazz)) {
                     LOG.error("Unable to load module '{}' class '{}' is not subclass of '{}'",
                             moduleName, moduleClazz.getCanonicalName(), targetModuleClass.getCanonicalName());
                     continue;
                 }
             } catch (final ClassNotFoundException ex) {
                 LOG.warn(ex, "Unable to load module '{}' class '{}' was not found",
                         moduleName, moduleClassName);
                 continue;
             }
 
             final Class<? extends M> finalClass = moduleClazz.asSubclass(targetModuleClass);
 
             final ConfigurableApplicationContext moduleContext = overrideRule(ctx,
                     moduleMetaInfNamespace, moduleName);
 
             try {
                 moduleContext.getBean(IDependencyProvider.class);
                 throw new IllegalStateException("IDependencyProvider already defined");
             } catch (NoSuchBeanDefinitionException ignored) {
             }
 
             descriptors.add(new ModuleDescriptor<Class<? extends M>>(
                     moduleName,
                     finalClass,
                     moduleProperties,
                     moduleContext
             ));
         }
 
         final Map<Class<? extends M>, ModuleDescriptor<Class<? extends M>>> unitializedModules =
                 CollectionUtils.toMap(
                         new LinkedHashMap<Class<? extends M>, ModuleDescriptor<Class<? extends M>>>(),
                         descriptors,
                         new ITranslator<ModuleDescriptor<Class<? extends M>>, Class<? extends M>>() {
                             @Override
                             public Class<? extends M> translate(final ModuleDescriptor<Class<? extends M>> source) {
                                 return source.getModule();
                             }
                         });
 
         final List<M> initializedModules = new ArrayList<>();
 
         for (final Class<? extends M> clazz : getResolutionOrder(unitializedModules.keySet())) {
             final ModuleDescriptor<Class<? extends M>> descriptor = unitializedModules.get(clazz);
             final ConfigurableApplicationContext ctx = descriptor.getContext();
 
             handler.onModuleDiscovered(descriptor.getModule(), ctx, descriptor.getProperties(), descriptor.getName());
 
             try {
                 final IDependencyProvider bean = ctx.getBean(IDependencyProvider.class);
                 throw new IllegalStateException("IDependencyProvider already injected! (" + bean + ')');
             } catch (NoSuchBeanDefinitionException ignored) {
             }
 
             for (final Class<?> dependencyClass : getDependencies(clazz)) {
                 final AbstractDependencyProvider<M> provider = new AbstractDependencyProvider<>();
 
                 final List<M> instantiatedDependencies = new ArrayList<>();
                 for (final M dep : initializedModules) {
                     if (IntrospectionUtils.instanceOf(dep.getClass(), dependencyClass)) {
                         instantiatedDependencies.add(dep);
                     }
                 }
 
                 if (!instantiatedDependencies.isEmpty()) {
                     provider.setModules(instantiatedDependencies);
                     registerSingleton(ctx, dependencyClass.getCanonicalName(), provider,
                             Dependency.class, dependencyClass);
                 }
             }
 
             final M module = instantiate(ctx, clazz);
             LOG.debug("Module {}(class={}) loaded", descriptor.getName(), clazz.getCanonicalName());
 
             handler.onModuleCreated(module, ctx, descriptor.getProperties(), descriptor.getName());
             initializedModules.add(module);
         }
 
         handler.onModuleDiscoveryFinished();
 
         LOG.info("Service started: {}", this.getClass().getCanonicalName());
     }
 
     static <T> Map<Class<? extends T>, Set<Class<?>>> getDependencyMatrix(final Collection<Class<? extends T>> modules) {
         final Map<Class<? extends T>, Set<Class<?>>> dependencyMatrix =
                 new LinkedHashMap<>();
 
         for (final Class<? extends T> module : modules) {
             if (dependencyMatrix.containsKey(module)) {
                 throw new IllegalStateException();
             }
 
             dependencyMatrix.put(module, new LinkedHashSet<Class<?>>());
             for (final Class<?> dep : getDependencies(module)) {
                 for (final Class<?> m : modules) {
                     if (instanceOf(m, dep)) {
                         dependencyMatrix.get(module).add(m);
                     }
                 }
             }
         }
 
         return dependencyMatrix;
     }
 
     static <T> List<Class<? extends T>> getResolutionOrder(final Collection<Class<? extends T>> modules) {
         final Map<Class<? extends T>, Set<Class<?>>> dependencyMatrix = getDependencyMatrix(modules);
         final List<Class<? extends T>> instantiationOrder = new ArrayList<>();
 
         int size;
         do {
             size = dependencyMatrix.size();
             final Iterator<Map.Entry<Class<? extends T>, Set<Class<?>>>> iter =
                     dependencyMatrix.entrySet().iterator();
             while (iter.hasNext()) {
                 final Map.Entry<Class<? extends T>, Set<Class<?>>> e = iter.next();
                 if (instantiationOrder.containsAll(e.getValue())) {
                     iter.remove();
                     instantiationOrder.add(e.getKey());
                 }
             }
         } while (size != dependencyMatrix.size());
 
         if (!dependencyMatrix.isEmpty()) {
             throw new IllegalArgumentException("Cycle references found " + dependencyMatrix);
         }
 
         return instantiationOrder;
     }
 
     static <T> Set<Class<?>> getDependencies(final Class<T> clazz) {
         final Set<Class<?>> dependencies = new LinkedHashSet<>();
        final Set<Constructor<? extends T>> constructors =
                searchConstructors(clazz, new IMatcher<Constructor<? extends T>>() {
                     @Override
                    public boolean matches(final Constructor<? extends T> obj) {
                         return obj.isAnnotationPresent(Inject.class)
                                && !obj.isSynthetic()
                                ;
                     }
                 });
 
         if (constructors.size() > 1) {
             throw new IllegalStateException("More than one injectable constructor found for " + clazz);
         }
 
        for (final Constructor<? extends T> constructor : constructors) {
             int paramIndex = 0;
             final LinkedHashMap<Integer, Map.Entry<Class<?>, Class<?>>> values = new LinkedHashMap<>();
             final Annotation[][] annotations = constructor.getParameterAnnotations();
             for (final Type type : constructor.getGenericParameterTypes()) {
                 final Class<?> typeClass = IntrospectionUtils.getClass(type);
                 if (instanceOf(typeClass, IDependencyProvider.class)) {
                     if (instanceOf(type, ParameterizedType.class)) {
                         final Type hold = ((ParameterizedType) type).getActualTypeArguments()[0];
                         final Class<?> heldClass = IntrospectionUtils.getClass(hold);
                         if (heldClass == null) {
                             throw new IllegalStateException("Missing type information for dependency provider of "
                                                             + constructor.getDeclaringClass().getCanonicalName()
                                                             + " module");
                         }
 
                         Class<?> value = null;
                         for (final Annotation a : annotations[paramIndex]) {
                             if (a instanceof Dependency) {
                                 value = ((Dependency) a).value();
                                 break;
                             }
                         }
 
                         values.put(paramIndex, new ImmutablePair<Class<?>, Class<?>>(heldClass, value));
                         if (!dependencies.add(heldClass)) {
                             throw new IllegalArgumentException("Duplicate "
                                                                + heldClass
                                                                + " providers found for "
                                                                + clazz);
                         }
                     } else {
                         throw new IllegalStateException("Unsupported type information found for dependency provider of "
                                                         + constructor.getDeclaringClass().getCanonicalName()
                                                         + " module (no type specified?)");
                     }
                 }
                 paramIndex++;
             }
 
             // ensure spring will be able to inject dependencies properly
             final Iterator<Map.Entry<Integer, Map.Entry<Class<?>, Class<?>>>> i = values.entrySet().iterator();
             if (values.size() == 1) {
                 final Map.Entry<Integer, Map.Entry<Class<?>, Class<?>>> e = i.next();
                 final Map.Entry<Class<?>, Class<?>> pair = e.getValue();
                 if (pair.getValue() != null && !pair.getKey().equals(pair.getValue())) {
                     throw parameterQualifierMismatch(constructor, e.getKey(), pair.getKey());
                 }
             } else if (values.size() > 1) {
                 while (i.hasNext()) {
                     final Map.Entry<Integer, Map.Entry<Class<?>, Class<?>>> e = i.next();
                     final Map.Entry<Class<?>, Class<?>> pair = e.getValue();
                     if (!pair.getKey().equals(pair.getValue())) {
                         throw parameterQualifierMismatch(constructor, e.getKey(), pair.getKey());
                     }
                 }
             }
         }
 
         return dependencies;
     }
 
     private static IllegalStateException parameterQualifierMismatch(final Constructor<?> c,
                                                                     final int index,
                                                                     final Class<?> expected) {
         return new IllegalStateException("Parameter " + index + " of " + c
                                          + " must be annotated with @Dependency("
                                          + expected.getSimpleName()
                                          + ".class)");
     }
 
     @Override
     protected void onPlannedStop() {
         handler.onApplicationStop();
     }
 
     @Override
     protected void onForcedShutdown() {
         LOG.info("Externally requested termination, performing onPlannedStop()");
         onPlannedStop();
     }
 
     @Nonnull
     Class<? extends M> getTargetModuleClass() {
         return targetModuleClass;
     }
 
     @Nonnull
     String getAppMetaInfNamespace() {
         return appMetaInfNamespace;
     }
 
     @Nonnull
     String getModuleMetaInfNamespace() {
         return moduleMetaInfNamespace;
     }
 
     @Nonnull
     protected String getAppName() {
         return appName;
     }
 
     @Nonnull
     public H getHandler() {
         return handler;
     }
 
     @Nonnull
     @SuppressWarnings("ConstantConditions")
     private static ConfigurableApplicationContext overrideRule(@Nullable final ConfigurableApplicationContext ctx,
                                                                @Nonnull final String namespace,
                                                                @Nonnull final String name) {
         final String parentPath = concat(META_INF, namespace, name + DEFAULT_CONTEXT_POSTFIX);
         final String childPath = concat(namespace, name + CONTEXT_POSTFIX);
 
         if (!isResourceExists(parentPath)) {
             throw new IllegalArgumentException("Unable to create parent context for " + parentPath);
         }
 
         if (isResourceExists(childPath)) {
             return ensureCreate(ctx, parentPath, childPath);
         }
 
         LOG.warn("Unable to override {} with {}", parentPath, childPath);
         return ensureCreate(ctx, parentPath);
     }
 
     //TODO: move to utility classes?
     @Nonnull
     static String concat(final String... paths) {
         if (paths.length == 0) {
             return "";
         }
 
         File file = new File(paths[0]);
         for (int i = 1; i < paths.length ; i++) {
             file = new File(file, paths[i]);
         }
 
         return file.getPath();
     }
 
     private static String getPackage(@Nonnull final Class<?> clazz) {
         return toValidPath(clazz.getPackage().getName());
     }
 
     @Nonnull
     private static String lowercaseClassName(@Nonnull final Class<?> clazz) {
         if (clazz.isAnonymousClass()) {
             return lowercaseClassName(clazz.getEnclosingClass());
         }
         final String str = clazz.getSimpleName();
         return str.substring(0, 1).toLowerCase() + str.substring(1);
     }
 
     @Nonnull
     private static String toValidPath(@Nonnull final String str) {
         return str.endsWith("/") ? str : (str + '/');
     }
 
     private static boolean isResourceExists(@Nonnull final String path) {
         return new ClassPathResource(path).exists();
     }
 }
