 package org.mosaic.lifecycle.impl;
 
 import com.google.common.cache.Cache;
 import com.google.common.cache.CacheBuilder;
 import com.google.common.reflect.TypeToken;
 import com.yammer.metrics.core.MetricName;
 import com.yammer.metrics.core.MetricsRegistry;
 import com.yammer.metrics.core.Timer;
 import com.yammer.metrics.core.TimerContext;
 import java.beans.PropertyChangeEvent;
 import java.lang.annotation.Annotation;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.net.URL;
 import java.nio.file.Path;
 import java.nio.file.Paths;
 import java.util.*;
 import java.util.concurrent.Callable;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.TimeUnit;
 import javax.annotation.Nonnull;
 import javax.annotation.Nullable;
 import org.joda.time.Duration;
 import org.mosaic.database.dao.annotation.Dao;
 import org.mosaic.lifecycle.*;
 import org.mosaic.lifecycle.annotation.*;
 import org.mosaic.lifecycle.impl.dependency.*;
 import org.mosaic.lifecycle.impl.registrar.AbstractRegistrar;
 import org.mosaic.lifecycle.impl.registrar.BeanServiceRegistrar;
 import org.mosaic.lifecycle.impl.registrar.MethodEndpointRegistrar;
 import org.mosaic.util.reflection.MethodHandle;
 import org.mosaic.util.reflection.impl.MethodHandleFactoryImpl;
 import org.osgi.framework.*;
 import org.osgi.framework.wiring.BundleRevision;
 import org.osgi.framework.wiring.BundleWiring;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.BeansException;
 import org.springframework.beans.MethodInvocationException;
 import org.springframework.beans.TypeMismatchException;
 import org.springframework.beans.factory.support.BeanDefinitionValidationException;
 import org.springframework.util.ReflectionUtils;
 
 import static java.lang.System.currentTimeMillis;
 import static java.lang.reflect.Modifier.*;
 import static org.mosaic.util.reflection.impl.AnnotationUtils.getAnnotation;
 import static org.springframework.util.ReflectionUtils.getUniqueDeclaredMethods;
 
 /**
  * @author arik
  */
 public class ModuleImpl implements Module
 {
     private static final Logger LOG = LoggerFactory.getLogger( ModuleImpl.class );
 
     @Nonnull
     private final ModuleManagerImpl moduleManager;
 
     @Nonnull
     private final MethodHandleFactoryImpl methodHandleFactory;
 
     @Nonnull
     private final Bundle bundle;
 
     @Nonnull
     private final Path path;
 
     @Nullable
     private Collection<String> resources;
 
     @Nonnull
     private ModuleState state = ModuleState.UNKNOWN;
 
     @Nullable
     private ModuleApplicationContext applicationContext;
 
     @Nullable
     private Set<Class<?>> componentClasses;
 
     @Nullable
     private List<AbstractRegistrar> registrars;
 
     @Nullable
     private List<AbstractDependency> dependencies;
 
     @Nullable
     private MetricsImpl metrics;
 
     public ModuleImpl( @Nonnull ModuleManagerImpl moduleManager,
                        @Nonnull MethodHandleFactoryImpl methodHandleFactory,
                        @Nonnull Bundle bundle )
     {
         this.moduleManager = moduleManager;
         this.methodHandleFactory = methodHandleFactory;
         this.bundle = bundle;
         this.path = Paths.get( bundle.getLocation() );
     }
 
     @Nonnull
     public Bundle getBundle()
     {
         return this.bundle;
     }
 
     @Nullable
     public BundleContext getBundleContext()
     {
         return this.bundle.getBundleContext();
     }
 
     @Nullable
     public ClassLoader getClassLoader()
     {
         BundleRevision bundleRevision = this.bundle.adapt( BundleRevision.class );
         if( bundleRevision != null )
         {
             BundleWiring bundleWiring = bundleRevision.getWiring();
             if( bundleWiring != null )
             {
                 ClassLoader classLoader = bundleWiring.getClassLoader();
                 if( classLoader != null )
                 {
                     return classLoader;
                 }
             }
         }
         return null;
     }
 
     @Nonnull
     @Override
     public Path getPath()
     {
         return this.path;
     }
 
     @Nonnull
     @Override
     public Map<String, String> getHeaders()
     {
         Map<String, String> headers = new HashMap<>();
 
         Dictionary<String, String> dictionary = this.bundle.getHeaders();
         Enumeration<String> keyesEnumeration = dictionary.keys();
         while( keyesEnumeration.hasMoreElements() )
         {
             String headerName = keyesEnumeration.nextElement();
             headers.put( headerName, dictionary.get( headerName ) );
         }
         return headers;
     }
 
     @Override
     public long getId()
     {
         return this.bundle.getBundleId();
     }
 
     @Nonnull
     @Override
     public String getName()
     {
         return this.bundle.getSymbolicName();
     }
 
     @Nonnull
     @Override
     public String getVersion()
     {
         return this.bundle.getVersion().toString();
     }
 
     @Nonnull
     @Override
     public ModuleState getState()
     {
         return this.state;
     }
 
     @Override
     public void waitForActivation( @Nonnull Duration timeout ) throws InterruptedException
     {
         long start = currentTimeMillis();
         long duration = timeout.getMillis();
 
         ModuleState state;
         do
         {
             state = getState();
             if( state == ModuleState.ACTIVE )
             {
                 return;
             }
             Thread.sleep( 500 );
        } while( start + duration > currentTimeMillis() );
 
         // module not found and the timeout has passed - throw an exception
         throw new IllegalStateException( "Module '" + this + "' has not been activated within " + timeout );
     }
 
     @Override
     public long getLastModified()
     {
         return this.bundle.getLastModified();
     }
 
     @Nonnull
     @Override
     public Collection<String> getResources()
     {
         if( this.resources == null )
         {
             return Collections.emptyList();
         }
         else
         {
             return this.resources;
         }
     }
 
     @Nonnull
     @Override
     public Collection<ServiceExport> getExportedServices()
     {
         ServiceReference<?>[] registeredServices = this.bundle.getRegisteredServices();
         if( registeredServices == null )
         {
             return Collections.emptyList();
         }
 
         Collection<ServiceExport> serviceExports = new LinkedList<>();
         for( final ServiceReference<?> serviceReference : registeredServices )
         {
             String[] classNames = ( String[] ) serviceReference.getProperty( Constants.OBJECTCLASS );
             for( String className : classNames )
             {
                 final TypeToken<?> type;
                 try
                 {
                     if( className.equals( MethodEndpoint.class.getName() ) )
                     {
                         type = TypeToken.of( MethodEndpoint.class );
                     }
                     else
                     {
                         type = TypeToken.of( this.bundle.loadClass( className ) );
                     }
                     serviceExports.add( new ServiceExportImpl( type, serviceReference ) );
                 }
                 catch( ClassNotFoundException e )
                 {
                     throw new IllegalStateException( "Could not load service type '" + className + "': " + e.getMessage(), e );
                 }
             }
         }
         return serviceExports;
     }
 
     @Nonnull
     @Override
     public <T> ServiceExport exportService( @Nonnull Class<? super T> type, T service, @Nonnull DP... properties )
     {
         // prepare properties dictionary
         Dictionary<String, Object> dict = null;
         if( properties.length > 0 )
         {
             dict = new Hashtable<>( properties.length );
             for( DP dp : properties )
             {
                 dict.put( dp.getKey(), dp.getValue() );
             }
         }
 
         // register
         ServiceRegistration<? super T> sr = this.bundle.getBundleContext().registerService( type, service, dict );
 
         // return service registration wrapper
         ServiceReference<? super T> ref = sr.getReference();
         return new ServiceExportImpl( TypeToken.of( type ), ref );
     }
 
     @Nullable
     @Override
     public URL getResource( @Nonnull String name )
     {
         return this.bundle.getEntry( name );
     }
 
     @Override
     public void start() throws ModuleStartException
     {
         try
         {
             this.bundle.start();
         }
         catch( BundleException e )
         {
             if( e.getType() == BundleException.RESOLVE_ERROR )
             {
                 throw new ModuleResolveException( this, e );
             }
             else
             {
                 throw new ModuleStartException( this, e.getMessage(), e );
             }
         }
         catch( Exception e )
         {
             throw new ModuleStartException( this, e.getMessage(), e );
         }
     }
 
     @Override
     public void stop() throws ModuleStopException
     {
         try
         {
             if( this.bundle.getState() != Bundle.STOPPING )
             {
                 this.bundle.stop( 0 );
             }
         }
         catch( Exception e )
         {
             throw new ModuleStopException( this, e.getMessage(), e );
         }
     }
 
     @Nonnull
     @Override
     public <T> T getBean( @Nonnull Class<? extends T> type )
     {
         if( this.state != ModuleState.ACTIVE )
         {
             throw new IllegalStateException( "Module '" + this + "' is not active" );
         }
 
         ModuleApplicationContext applicationContext = this.applicationContext;
         if( applicationContext == null )
         {
             throw new IllegalStateException( "Module '" + this + "' has no context" );
         }
         try
         {
             return applicationContext.getBean( type );
         }
         catch( BeansException e )
         {
             throw new IllegalArgumentException( "Could not get or find bean of type '" + type.getName() + "' in module '" + this + "': " + e.getMessage(), e );
         }
     }
 
     @Nonnull
     @Override
     public <T> T getBean( @Nonnull String beanName, @Nonnull Class<? extends T> type )
     {
         if( this.state != ModuleState.ACTIVE )
         {
             throw new IllegalStateException( "Module '" + this + "' is not active" );
         }
 
         ModuleApplicationContext applicationContext = this.applicationContext;
         if( applicationContext == null )
         {
             throw new IllegalStateException( "Module '" + this + "' has no context" );
         }
         try
         {
             return applicationContext.getBean( beanName, type );
         }
         catch( BeansException e )
         {
             throw new IllegalArgumentException( "Could not get or find bean of type '" + type.getName() + "' in module '" + this + "': " + e.getMessage(), e );
         }
     }
 
     @Nullable
     @Override
     public Metrics getMetrics()
     {
         return this.metrics;
     }
 
     public synchronized void handleBundleEvent( BundleEvent event )
     {
         switch( event.getType() )
         {
             case BundleEvent.INSTALLED:
                 onBundleInstall();
                 break;
 
             case BundleEvent.RESOLVED:
                 onBundleResolved();
                 break;
 
             case BundleEvent.STARTING:
                 onBundleStarting();
                 break;
 
             case BundleEvent.STARTED:
                 onBundleStarted();
                 break;
 
             case BundleEvent.STOPPING:
                 onBundleStopping();
                 break;
 
             case BundleEvent.STOPPED:
                 onBundleStopped();
                 break;
 
             case BundleEvent.UNRESOLVED:
                 onBundleUnresolved();
                 break;
 
             case BundleEvent.UPDATED:
                 onBundleUpdated();
                 break;
 
             case BundleEvent.UNINSTALLED:
                 onBundleUninstalled();
                 break;
         }
     }
 
     @Nullable
     public Object getBean( @Nonnull String beanName )
     {
         ModuleApplicationContext applicationContext = this.applicationContext;
         if( applicationContext != null )
         {
             return applicationContext.getBean( beanName );
         }
         else
         {
             return null;
         }
     }
 
     public void beanCreated( @Nonnull Object bean, @Nonnull String beanName )
     {
         // detect and inject this module to any bean method annotated with @ModuleRef
         for( Method method : ReflectionUtils.getUniqueDeclaredMethods( bean.getClass() ) )
         {
             if( getAnnotation( method, ModuleRef.class ) != null )
             {
                 try
                 {
                     method.invoke( bean, this );
                 }
                 catch( IllegalAccessException e )
                 {
                     throw new BeanDefinitionValidationException( "Insufficient access to invoke a @ModuleRef method in bean '" + beanName + "'", e );
                 }
                 catch( IllegalArgumentException e )
                 {
                     throw new TypeMismatchException( this, Module.class, e );
                 }
                 catch( InvocationTargetException e )
                 {
                     throw new MethodInvocationException( new PropertyChangeEvent( bean, method.getName(), null, this ), e );
                 }
             }
         }
 
         // notify any dependencies founded on this bean
         if( this.dependencies != null )
         {
             for( AbstractDependency dependency : this.dependencies )
             {
                 if( dependency instanceof AbstractBeanDependency )
                 {
                     AbstractBeanDependency beanDependency = ( AbstractBeanDependency ) dependency;
                     if( beanName.equals( beanDependency.getBeanName() ) )
                     {
                         beanDependency.beanCreated( bean );
                     }
                 }
             }
         }
     }
 
     public void beanInitialized( @Nonnull Object bean, @Nonnull String beanName )
     {
         if( this.dependencies != null )
         {
             for( AbstractDependency dependency : this.dependencies )
             {
                 if( dependency instanceof AbstractBeanDependency )
                 {
                     AbstractBeanDependency beanDependency = ( AbstractBeanDependency ) dependency;
                     if( beanName.equals( beanDependency.getBeanName() ) )
                     {
                         beanDependency.beanInitialized( bean );
                     }
                 }
             }
         }
     }
 
     public synchronized void activateIfReady()
     {
         if( this.applicationContext == null && canBeActivated() )
         {
             this.state = ModuleState.ACTIVATING;
 
             if( this.componentClasses != null && !this.componentClasses.isEmpty() )
             {
                 // create application context
                 ModuleApplicationContext moduleApplicationContext = new ModuleApplicationContext( this, this.componentClasses );
                 moduleApplicationContext.refresh();
                 this.applicationContext = moduleApplicationContext;
             }
 
             // register all declared services
             if( this.registrars != null )
             {
                 for( AbstractRegistrar registrar : this.registrars )
                 {
                     registrar.register();
                 }
             }
 
             // there! we've started!
             this.state = ModuleState.ACTIVE;
 
             LOG.info( "Module {} has been ACTIVATED", getName() );
             this.moduleManager.notifyModuleActivated( this );
         }
     }
 
     public boolean canBeActivated()
     {
         if( this.dependencies != null )
         {
             // check if all dependencies are satisified
             for( AbstractDependency dependency : this.dependencies )
             {
                 if( !dependency.isSatisfied() )
                 {
                     return false;
                 }
             }
         }
         return true;
     }
 
     public synchronized void deactivate()
     {
         if( this.applicationContext != null && this.state != ModuleState.DEACTIVATING )
         {
             this.state = ModuleState.DEACTIVATING;
 
             // unregister all declared services
             if( this.registrars != null )
             {
                 for( AbstractRegistrar registrar : this.registrars )
                 {
                     registrar.unregister();
                 }
             }
 
             // stop all dependencies
             if( this.dependencies != null )
             {
                 for( AbstractDependency dependency : this.dependencies )
                 {
                     dependency.stop();
                 }
             }
 
             // stop application context
             this.applicationContext.close();
 
             // remove reference to the application context to allow garbage collection of beans
             this.applicationContext = null;
 
             this.state = ModuleState.STARTED;
             LOG.info( "Module {} has been DEACTIVATED", getName() );
 
             this.moduleManager.notifyModuleDeactivated( this );
         }
     }
 
     @Override
     public String toString()
     {
         return "Module[" + this.bundle.getSymbolicName() + "-" + this.bundle.getVersion() + " -> " + this.bundle.getBundleId() + "]";
     }
 
     private void refreshResourcesCache()
     {
         List<String> resources = new ArrayList<>();
 
         Enumeration<URL> entries = this.bundle.findEntries( "/", "*", true );
         if( entries != null )
         {
             while( entries.hasMoreElements() )
             {
                 String path = entries.nextElement().getPath();
                 if( !path.endsWith( "/" ) )
                 {
                     resources.add( path );
                 }
             }
         }
         Collections.sort( resources );
         this.resources = Collections.unmodifiableCollection( resources );
     }
 
     private void onBundleInstall()
     {
         this.state = ModuleState.INSTALLED;
         LOG.debug( "Module {} has been INSTALLED", getName() );
         this.moduleManager.notifyModuleInstalled( this );
     }
 
     private void onBundleResolved()
     {
         refreshResourcesCache();
         this.metrics = new MetricsImpl();
         this.state = ModuleState.RESOLVED;
         LOG.debug( "Module {} has been RESOLVED", getName() );
     }
 
     private void onBundleStarting()
     {
         LOG.debug( "Bootstrapping module '{}'", getName() );
         this.state = ModuleState.STARTING;
 
         // add beans to application context, and also populate our dependencies during so
         this.componentClasses = findComponentClasses();
         this.registrars = new LinkedList<>();
         this.dependencies = new LinkedList<>();
         for( Class<?> componentClass : this.componentClasses )
         {
             String beanName = componentClass.getName();
 
             // is this a service bean?
             Service serviceAnn = getAnnotation( componentClass, Service.class );
             if( serviceAnn != null )
             {
                 Rank rankAnn = getAnnotation( componentClass, Rank.class );
                 for( Class<?> serviceType : serviceAnn.value() )
                 {
                     this.registrars.add(
                             new BeanServiceRegistrar( this,
                                                       beanName,
                                                       serviceType,
                                                       rankAnn == null ? 0 : rankAnn.value(),
                                                       serviceAnn.properties() ) );
                 }
             }
 
             // iterate class methods for dependencies
             detectBeanDependencies( beanName, componentClass, this.dependencies, this.registrars );
         }
     }
 
     private void onBundleStarted()
     {
         if( this.dependencies != null )
         {
             // open all dependencies
             for( AbstractDependency dependency : this.dependencies )
             {
                 dependency.start();
             }
         }
         this.state = ModuleState.STARTED;
 
         // activate, provided all dependencies are satisfied
         activateIfReady();
     }
 
     private void onBundleStopping()
     {
         LOG.debug( "Module {} is STOPPING", getName() );
 
         // deactivate
         deactivate();
 
         this.state = ModuleState.STOPPING;
 
         // clear our registrars, dependencies and component classes to avoid keeping stuff in memory (in particular the class loader)
         this.registrars = null;
         this.dependencies = null;
         this.componentClasses = null;
     }
 
     private void onBundleStopped()
     {
         this.state = ModuleState.RESOLVED;
         LOG.info( "Module {} has been STOPPED", getName() );
     }
 
     private void onBundleUpdated()
     {
         refreshResourcesCache();
         LOG.debug( "Module {} has been UPDATED", getName() );
     }
 
     private void onBundleUnresolved()
     {
         this.resources = null;
         if( this.metrics != null )
         {
             this.metrics.shutdown();
         }
         this.metrics = null;
         this.state = ModuleState.INSTALLED;
         LOG.debug( "Module {} has been UNRESOLVED", getName() );
     }
 
     private void onBundleUninstalled()
     {
         this.state = ModuleState.UNINSTALLED;
         LOG.info( "Module {} has been UNINSTALLED", getName() );
         this.moduleManager.notifyModuleUninstalled( this );
     }
 
     private Set<Class<?>> findComponentClasses()
     {
         Set<Class<?>> componentClasses = new HashSet<>();
 
         Enumeration<URL> entries = this.bundle.findEntries( "/", "*.class", true );
         if( entries != null )
         {
             while( entries.hasMoreElements() )
             {
                 URL entry = entries.nextElement();
                 String path = entry.getPath();
                 if( path.toLowerCase().endsWith( ".class" ) )
                 {
                     // remove the "/" prefix
                     if( path.startsWith( "/" ) )
                     {
                         path = path.substring( 1 );
                     }
 
                     // remove the ".class" suffix
                     path = path.substring( 0, path.length() - ".class".length() );
 
                     // load the class and determine if it's a @Component class (@Bean is @Component by proxy too)
                     Class<?> clazz;
                     try
                     {
                         clazz = this.bundle.loadClass( path.replace( '/', '.' ) );
 
                         int modifiers = clazz.getModifiers();
                         if( isAbstract( modifiers ) || isInterface( modifiers ) || !isPublic( modifiers ) )
                         {
                             // if abstract, interface or non-public then skip it
                             continue;
                         }
                         else if( getAnnotation( clazz, Bean.class ) == null )
                         {
                             // if not a @Bean then skip it
                             continue;
                         }
                         componentClasses.add( clazz );
                     }
                     catch( ClassNotFoundException | NoClassDefFoundError ignore )
                     {
                     }
                 }
             }
         }
         return componentClasses;
     }
 
     private void detectBeanDependencies( @Nonnull String beanName,
                                          @Nonnull Class<?> componentClass,
                                          @Nonnull List<AbstractDependency> dependencies,
                                          @Nonnull List<AbstractRegistrar> registrars )
     {
         for( Method method : getUniqueDeclaredMethods( componentClass ) )
         {
             MethodHandle methodHandle = this.methodHandleFactory.findMethodHandle( method );
 
             // detect @ServiceRef dependencies
             ServiceRef serviceRefAnn = methodHandle.getAnnotation( ServiceRef.class );
             if( serviceRefAnn != null )
             {
                 if( serviceRefAnn.autoSelectIfMultiple() )
                 {
                     dependencies.add( new OptimisticServiceRefDependency( this, serviceRefAnn.value(), serviceRefAnn.required(), beanName, methodHandle ) );
                 }
                 else
                 {
                     dependencies.add( new PessimisticServiceRefDependency( this, serviceRefAnn.value(), serviceRefAnn.required(), beanName, methodHandle ) );
                 }
             }
 
             // detect @ServiceRefs dependency
             ServiceRefs serviceRefsAnn = methodHandle.getAnnotation( ServiceRefs.class );
             if( serviceRefsAnn != null )
             {
                 dependencies.add( new ServiceRefsDependency( this, serviceRefsAnn.value(), beanName, methodHandle ) );
             }
 
             // detect @ServiceBind dependency
             ServiceBind serviceBindAnn = methodHandle.getAnnotation( ServiceBind.class );
             if( serviceBindAnn != null )
             {
                 dependencies.add( new ServiceBindDependency( this, serviceBindAnn.value(), serviceBindAnn.updates(), beanName, methodHandle ) );
             }
 
             // detect @ServiceUnbind dependency
             ServiceUnbind serviceUnbindAnn = methodHandle.getAnnotation( ServiceUnbind.class );
             if( serviceUnbindAnn != null )
             {
                 dependencies.add( new ServiceUnbindDependency( this, serviceUnbindAnn.value(), beanName, methodHandle ) );
             }
 
             // detect @ServiceUnbind dependency
             Dao daoAnn = methodHandle.getAnnotation( Dao.class );
             if( daoAnn != null )
             {
                 dependencies.add( new DaoRefDependency( this, beanName, methodHandle, daoAnn.value() ) );
             }
 
             // detect method endpoints
             for( Annotation annotation : methodHandle.getAnnotations() )
             {
                 Class<? extends Annotation> annotationType = annotation.annotationType();
                 if( annotationType.isAnnotationPresent( MethodEndpointMarker.class ) )
                 {
                     registrars.add( new MethodEndpointRegistrar( this, beanName, annotation, methodHandle ) );
                 }
             }
         }
     }
 
     private class MetricsTimerImpl implements MetricsTimer
     {
         @Nonnull
         private final ThreadLocal<Deque<TimerContext>> timerContexts = new ThreadLocal<Deque<TimerContext>>()
         {
             @Override
             protected Deque<TimerContext> initialValue()
             {
                 return new LinkedList<>();
             }
         };
 
         @Nonnull
         private final MetricName name;
 
         @Nonnull
         private final Timer timer;
 
         private MetricsTimerImpl( @Nonnull MetricName name, @Nonnull Timer timer )
         {
             this.name = name;
             this.timer = timer;
         }
 
         @Nonnull
         @Override
         public Module getModule()
         {
             return ModuleImpl.this;
         }
 
         @Nonnull
         @Override
         public String getName()
         {
             return this.name.toString();
         }
 
         @Override
         public void startTimer()
         {
             this.timerContexts.get().push( this.timer.time() );
         }
 
         @Override
         public void stopTimer()
         {
             this.timerContexts.get().pop().stop();
         }
 
         @Override
         public long count()
         {
             return this.timer.count();
         }
 
         @Override
         public double fifteenMinuteRate()
         {
             return this.timer.fifteenMinuteRate();
         }
 
         @Override
         public double fiveMinuteRate()
         {
             return this.timer.fiveMinuteRate();
         }
 
         @Override
         public double meanRate()
         {
             return this.timer.meanRate();
         }
 
         @Override
         public double oneMinuteRate()
         {
             return this.timer.oneMinuteRate();
         }
 
         @Override
         public double max()
         {
             return this.timer.max();
         }
 
         @Override
         public double min()
         {
             return this.timer.min();
         }
 
         @Override
         public double mean()
         {
             return this.timer.mean();
         }
 
         @Override
         public double stdDev()
         {
             return this.timer.stdDev();
         }
 
         @Override
         public double sum()
         {
             return this.timer.sum();
         }
     }
 
     private class MetricsImpl implements Metrics
     {
         @Nullable
         private MetricsRegistry metricsRegistry;
 
         @Nullable
         private Cache<MetricName, MetricsTimerImpl> timerCache;
 
         private MetricsImpl()
         {
             this.metricsRegistry = new MetricsRegistry();
             this.timerCache = CacheBuilder.newBuilder()
                                           .initialCapacity( 1000 )
                                           .concurrencyLevel( 20 )
                                           .build();
         }
 
         @Nonnull
         @Override
         public MetricsTimer getTimer( @Nonnull String group, @Nonnull String type, @Nonnull String name )
         {
             final MetricsRegistry metricsRegistry = this.metricsRegistry;
             Cache<MetricName, MetricsTimerImpl> cache = this.timerCache;
 
             if( metricsRegistry != null && cache != null )
             {
                 final MetricName key = new MetricName( group, type, name );
                 try
                 {
                     return cache.get( key, new Callable<MetricsTimerImpl>()
                     {
                         @Override
                         public MetricsTimerImpl call() throws Exception
                         {
                             Timer timer = metricsRegistry.newTimer( key, TimeUnit.MILLISECONDS, TimeUnit.SECONDS );
                             return new MetricsTimerImpl( key, timer );
                         }
                     } );
                 }
                 catch( ExecutionException e )
                 {
                     throw new IllegalStateException( "Could not create metrics timer for '" + group + ":" + type + ":" + name + "'", e );
                 }
             }
             throw new IllegalStateException( "Could not create metrics timer for '" + group + ":" + type + ":" + name + "'" );
         }
 
         @Nonnull
         @Override
         public Collection<? extends MetricsTimer> getTimers()
         {
             Cache<MetricName, MetricsTimerImpl> cache = this.timerCache;
             if( cache != null )
             {
                 return cache.asMap().values();
             }
             throw new IllegalStateException( "Metrics timers are not available" );
         }
 
         private void shutdown()
         {
             MetricsRegistry metricsRegistry = this.metricsRegistry;
             if( metricsRegistry != null )
             {
                 metricsRegistry.shutdown();
             }
             this.metricsRegistry = null;
 
             Cache<MetricName, MetricsTimerImpl> cache = this.timerCache;
             if( cache != null )
             {
                 cache.invalidateAll();
             }
             this.timerCache = null;
         }
     }
 
     private class ServiceExportImpl implements ServiceExport
     {
         private final TypeToken<?> type;
 
         private final ServiceRegistration<?> serviceRegistration;
 
         private final ServiceReference<?> serviceReference;
 
         public ServiceExportImpl( TypeToken<?> type, ServiceReference<?> serviceReference )
         {
             this.type = type;
             this.serviceReference = serviceReference;
 
             Method getRegistrationMethod = ReflectionUtils.findMethod( serviceReference.getClass(), "getRegistration" );
             getRegistrationMethod.setAccessible( true );
             try
             {
                 this.serviceRegistration = ( ServiceRegistration<?> ) getRegistrationMethod.invoke( serviceReference );
             }
             catch( IllegalAccessException | InvocationTargetException e )
             {
                 throw new IllegalStateException( "Could not obtain OSGi service registration from a service reference '" + serviceReference + "':" + e.getMessage(), e );
             }
         }
 
         @Nonnull
         @Override
         public Module getProvider()
         {
             return ModuleImpl.this;
         }
 
         @Nonnull
         @Override
         public TypeToken<?> getType()
         {
             return type;
         }
 
         @Nonnull
         @Override
         public Map<String, Object> getProperties()
         {
             Map<String, Object> properties = new LinkedHashMap<>( 10 );
             for( String key : serviceReference.getPropertyKeys() )
             {
                 properties.put( key, serviceReference.getProperty( key ) );
             }
             return properties;
         }
 
         @Nonnull
         @Override
         public Collection<Module> getConsumers()
         {
             Collection<Module> modules = new LinkedList<>();
             Bundle[] usingBundles = serviceReference.getUsingBundles();
             if( usingBundles != null )
             {
                 for( Bundle usingBundle : usingBundles )
                 {
                     Module module = moduleManager.getModule( usingBundle.getBundleId() );
                     if( module != null )
                     {
                         modules.add( module );
                     }
                 }
             }
             return modules;
         }
 
         @Override
         public boolean isRegistered()
         {
             return this.serviceReference.getBundle() != null;
         }
 
         @Override
         public void unregister()
         {
             this.serviceRegistration.unregister();
         }
     }
 }
