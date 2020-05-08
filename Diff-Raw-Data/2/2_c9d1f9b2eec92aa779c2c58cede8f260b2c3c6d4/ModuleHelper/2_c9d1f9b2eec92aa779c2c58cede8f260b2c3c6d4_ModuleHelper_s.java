 package org.mosaic.lifecycle.impl;
 
 import java.beans.PropertyChangeEvent;
 import java.lang.annotation.Annotation;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.net.URL;
 import java.util.*;
 import javax.annotation.Nonnull;
 import javax.annotation.Nullable;
 import org.mosaic.database.dao.annotation.Dao;
 import org.mosaic.lifecycle.Module;
 import org.mosaic.lifecycle.annotation.*;
 import org.mosaic.lifecycle.impl.dependency.*;
 import org.mosaic.lifecycle.impl.registrar.AbstractRegistrar;
 import org.mosaic.lifecycle.impl.registrar.BeanServiceRegistrar;
 import org.mosaic.lifecycle.impl.registrar.MethodEndpointRegistrar;
 import org.mosaic.util.reflection.MethodHandle;
 import org.mosaic.util.reflection.MethodHandleFactory;
 import org.osgi.framework.Bundle;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.MethodInvocationException;
 import org.springframework.beans.TypeMismatchException;
 import org.springframework.beans.factory.support.BeanDefinitionValidationException;
 import org.springframework.util.ReflectionUtils;
 
 import static java.lang.reflect.Modifier.*;
 import static org.mosaic.util.reflection.impl.AnnotationUtils.getAnnotation;
 import static org.springframework.util.ReflectionUtils.getUniqueDeclaredMethods;
 
 /**
  * @author arik
  */
 public class ModuleHelper
 {
     private static final Logger LOG = LoggerFactory.getLogger( ModuleHelper.class );
 
     @Nonnull
     private final ModuleImpl module;
 
     @Nonnull
     private final MethodHandleFactory methodHandleFactory;
 
     @Nullable
     private Set<Class<?>> componentClasses;
 
     @Nullable
     private List<AbstractRegistrar> registrars;
 
     @Nullable
     private List<AbstractDependency> dependencies;
 
     public ModuleHelper( @Nonnull ModuleImpl module, @Nonnull MethodHandleFactory methodHandleFactory )
     {
         this.module = module;
         this.methodHandleFactory = methodHandleFactory;
     }
 
     public Collection<Module.Dependency> getDependencies()
     {
         Collection<Module.Dependency> dependencies = new LinkedList<>();
         if( this.dependencies != null )
         {
             dependencies.addAll( this.dependencies );
         }
         return dependencies;
     }
 
     public Collection<Module.Dependency> getUnsatisfiedDependencies()
     {
         Collection<Module.Dependency> dependencies = new LinkedList<>();
         if( this.dependencies != null )
         {
             for( AbstractDependency dependency : this.dependencies )
             {
                 if( !dependency.isSatisfied() )
                 {
                     dependencies.add( dependency );
                 }
             }
         }
         return dependencies;
     }
 
     public void onBeansCreated( @Nonnull Object bean, @Nonnull String beanName )
     {
         // detect and inject this module to any bean method annotated with @ModuleRef
         for( Method method : ReflectionUtils.getUniqueDeclaredMethods( bean.getClass() ) )
         {
             if( getAnnotation( method, ModuleRef.class ) != null )
             {
                 try
                 {
                     method.invoke( bean, this.module );
                 }
                 catch( IllegalAccessException e )
                 {
                     throw new BeanDefinitionValidationException( "Insufficient access to invoke a @ModuleRef method in bean '" + beanName + "'", e );
                 }
                 catch( IllegalArgumentException e )
                 {
                     throw new TypeMismatchException( this.module, Module.class, e );
                 }
                 catch( InvocationTargetException e )
                 {
                     throw new MethodInvocationException( new PropertyChangeEvent( bean, method.getName(), null, this.module ), e );
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
 
     public void onBeanInitialized( @Nonnull Object bean, @Nonnull String beanName )
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
 
     public void refreshModuleComponents()
     {
         Bundle bundle = this.module.getBundle();
 
         Set<Class<?>> componentClasses = new HashSet<>();
         Enumeration<URL> entries = bundle.findEntries( "/", "*.class", true );
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
                         clazz = bundle.loadClass( path.replace( '/', '.' ) );
 
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
                    catch( ClassNotFoundException | NoClassDefFoundError e )
                     {
                         LOG.warn( "Could not read or parse class '{}' from module '{}': {}", path, this.module, e.getMessage(), e );
                     }
                 }
             }
         }
         this.componentClasses = componentClasses;
 
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
                             new BeanServiceRegistrar( this.module,
                                                       beanName,
                                                       serviceType,
                                                       rankAnn == null ? 0 : rankAnn.value(),
                                                       serviceAnn.properties() ) );
                 }
             }
 
             // iterate class methods for dependencies
             detectBeanRelationships( beanName, componentClass, this.dependencies, this.registrars );
         }
     }
 
     public void startDependencies()
     {
         if( this.dependencies != null )
         {
             for( AbstractDependency dependency : this.dependencies )
             {
                 try
                 {
                     dependency.start();
                 }
                 catch( Exception e )
                 {
                     LOG.warn( "Could not start {}: {}", dependency, e.getMessage(), e );
                 }
             }
         }
     }
 
     public boolean hasUnsatisfiedDependencies()
     {
         if( this.dependencies != null )
         {
             // check if all dependencies are satisified
             for( AbstractDependency dependency : this.dependencies )
             {
                 if( !dependency.isSatisfied() )
                 {
                     LOG.debug( "Module '{}' cannot be activated - dependency {} is unsatisfied", this.module.getName(), dependency );
                     return true;
                 }
             }
         }
         return false;
     }
 
     @Nullable
     public ModuleApplicationContext createApplicationContext()
     {
         ModuleApplicationContext moduleApplicationContext = null;
 
         if( this.componentClasses != null && !this.componentClasses.isEmpty() )
         {
             // create application context
             moduleApplicationContext = new ModuleApplicationContext( this.module, this.componentClasses );
             moduleApplicationContext.refresh();
         }
 
         return moduleApplicationContext;
     }
 
     public void registerDependencies()
     {
         if( this.registrars != null )
         {
             for( AbstractRegistrar registrar : this.registrars )
             {
                 try
                 {
                     registrar.register();
                 }
                 catch( Exception e )
                 {
                     LOG.warn( "Could not register {}: {}", registrar, e.getMessage(), e );
                 }
             }
         }
     }
 
     public void unregisterDependencies()
     {
         if( this.registrars != null )
         {
             for( AbstractRegistrar registrar : this.registrars )
             {
                 try
                 {
                     registrar.unregister();
                 }
                 catch( Exception e )
                 {
                     LOG.warn( "Could not unregister {}: {}", registrar, e.getMessage(), e );
                 }
             }
         }
     }
 
     public void stopDependencies()
     {
         if( this.dependencies != null )
         {
             for( AbstractDependency dependency : this.dependencies )
             {
                 try
                 {
                     dependency.stop();
                 }
                 catch( Exception e )
                 {
                     LOG.warn( "Could not stop {}: {}", dependency, e.getMessage(), e );
                 }
             }
         }
     }
 
     public void discardModuleComponents()
     {
         this.registrars = null;
         this.dependencies = null;
         this.componentClasses = null;
     }
 
     private void detectBeanRelationships( @Nonnull String beanName,
                                           @Nonnull Class<?> componentClass,
                                           @Nonnull List<? super AbstractDependency> dependencies,
                                           @Nonnull List<AbstractRegistrar> registrars )
     {
         for( Method method : getUniqueDeclaredMethods( componentClass ) )
         {
             MethodHandle methodHandle = this.methodHandleFactory.findMethodHandle( method );
 
             // detect @ServiceRef dependencies
             ServiceRef serviceRefAnn = methodHandle.getAnnotation( ServiceRef.class );
             if( serviceRefAnn != null )
             {
                 dependencies.add( new OptimisticServiceRefDependency( this.module, serviceRefAnn.value(), serviceRefAnn.required(), beanName, methodHandle ) );
             }
 
             // detect @MethodEndpointRef dependencies
             MethodEndpointRef methodEndpointRefAnn = methodHandle.getAnnotation( MethodEndpointRef.class );
             if( methodEndpointRefAnn != null )
             {
                 String filter = "(type=" + methodEndpointRefAnn.value().getName() + ")";
                 dependencies.add( new OptimisticServiceRefDependency( this.module, filter, methodEndpointRefAnn.required(), beanName, methodHandle ) );
             }
 
             // detect @ServiceRefs dependency
             ServiceRefs serviceRefsAnn = methodHandle.getAnnotation( ServiceRefs.class );
             if( serviceRefsAnn != null )
             {
                 dependencies.add( new ServiceRefsDependency( this.module, serviceRefsAnn.value(), beanName, methodHandle ) );
             }
 
             // detect @ServiceRefs dependency
             MethodEndpointRefs methodEndpointRefsAnn = methodHandle.getAnnotation( MethodEndpointRefs.class );
             if( methodEndpointRefsAnn != null )
             {
                 String filter = "(type=" + methodEndpointRefsAnn.value().getName() + ")";
                 dependencies.add( new ServiceRefsDependency( this.module, filter, beanName, methodHandle ) );
             }
 
             // detect @ServiceBind dependency
             ServiceBind serviceBindAnn = methodHandle.getAnnotation( ServiceBind.class );
             if( serviceBindAnn != null )
             {
                 dependencies.add( new ServiceBindDependency( this.module, serviceBindAnn.value(), serviceBindAnn.updates(), beanName, methodHandle ) );
             }
 
             // detect @ServiceBind dependency
             MethodEndpointBind methodEndpointBindAnn = methodHandle.getAnnotation( MethodEndpointBind.class );
             if( methodEndpointBindAnn != null )
             {
                 String filter = "(type=" + methodEndpointBindAnn.value().getName() + ")";
                 dependencies.add( new ServiceBindDependency( this.module, filter, methodEndpointBindAnn.updates(), beanName, methodHandle ) );
             }
 
             // detect @ServiceUnbind dependency
             ServiceUnbind serviceUnbindAnn = methodHandle.getAnnotation( ServiceUnbind.class );
             if( serviceUnbindAnn != null )
             {
                 dependencies.add( new ServiceUnbindDependency( this.module, serviceUnbindAnn.value(), beanName, methodHandle ) );
             }
 
             // detect @ServiceUnbind dependency
             MethodEndpointUnbind methodEndpointUnbindAnn = methodHandle.getAnnotation( MethodEndpointUnbind.class );
             if( methodEndpointUnbindAnn != null )
             {
                 String filter = "(type=" + methodEndpointUnbindAnn.value().getName() + ")";
                 dependencies.add( new ServiceUnbindDependency( this.module, filter, beanName, methodHandle ) );
             }
 
             // detect @ServiceUnbind dependency
             Dao daoAnn = methodHandle.getAnnotation( Dao.class );
             if( daoAnn != null )
             {
                 dependencies.add( new DaoRefDependency( this.module, beanName, methodHandle, daoAnn.value() ) );
             }
 
             // detect method endpoints
             for( Annotation annotation : methodHandle.getAnnotations() )
             {
                 Class<? extends Annotation> annotationType = annotation.annotationType();
                 if( annotationType.isAnnotationPresent( MethodEndpointMarker.class ) )
                 {
                     registrars.add( new MethodEndpointRegistrar( this.module, beanName, annotation, methodHandle ) );
                 }
             }
         }
     }
 }
