 /*
  * Copyright (c) 2011, DataLite. All rights reserved.
  *
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 2.1 of the License, or (at your option) any later version.
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
  * MA 02110-1301  USA
  */
 package cz.datalite.zk.annotation.processor;
 
 import cz.datalite.helpers.ReflectionHelper;
 import cz.datalite.zk.annotation.*;
 import cz.datalite.zk.annotation.invoke.*;
 import java.lang.reflect.Method;
 import java.util.*;
 import org.zkoss.bind.annotation.Command;
 import org.zkoss.lang.Library;
 import org.zkoss.zk.ui.Component;
 
 /**
  * <p>Annotation Processor handles all ZK annotations on the methods. For each
  * annotation there is defined specific annotation processor which handles that
  * annotation type and provides desired functionality. AnnotationProcessor cares
  * about right order of decorating {@link Invoke} object because if they would
  * be decorated in wrong order then the final functionality could be
  * undeterministic or at least undesired.</p>
  *
  * <p>Instance of annotation processor is bound to the instance of component
  * controller but there is possible way to refactor it as unbound to anything.
  * Then the class would create templates which would be cloned and bound to the
  * specific instance.</p>
  *
  * @author Karel Čemus <cemus@datalite.cz>
  */
 public class AnnotationProcessor<T> {
 
     /** Map of created processors. Serves as annotation cache */
     private static final Map< Class, AnnotationProcessor> processors = new HashMap<Class, AnnotationProcessor>();
 
     /** list of processors producing method invokers or similar */
     private static final List<Initializer> initializers = new ArrayList<Initializer>();
     
     /** list of processors producing wrappers for invoke object providing additional
      * functionality */
     private static final List<Wrapper> wrappers = new ArrayList<Wrapper>();
 
     /** configuration key in property files */
     private static final String CONFIG = "zk-dl.annotation.cache";
 
     /** caching is enabled / disabled */
     private static boolean cache = true;
 
     static {
         initializers.add( new GeneralInitializerProcessor( Command.class, CommandInvoker.class ) );
         initializers.add( new GeneralInitializerProcessor( ZkEvent.class, MethodInvoker.class ) );
         initializers.add( new GeneralInitializerProcessor( ZkEvents.class, MethodInvoker.class ) );
 
         wrappers.add( new GeneralWrapperProcessor( ZkException.class, ZkExceptionHandler.class ) );
         wrappers.add( new GeneralWrapperProcessor( ZkExceptions.class, ZkExceptionHandler.class ) );
         wrappers.add( new GeneralWrapperProcessor( ZkBinding.class, ZkBindingHandler.class ) );
         wrappers.add( new GeneralWrapperProcessor( ZkBindings.class, ZkBindingHandler.class ) );
         wrappers.add( new GeneralWrapperProcessor( ZkConfirm.class, ZkConfirmHandler.class ) );
         wrappers.add( new GeneralWrapperProcessor( ZkBlocking.class, ZkBlockingHandler.class ) );
         wrappers.add( new GeneralWrapperProcessor( ZkAsync.class, ZkAsyncHandler.class ) );
 
         // loading property of caching or not
         cache = Boolean.parseBoolean( System.getProperty( CONFIG, Library.getProperty( CONFIG, "true" ) ) );
     }
 
     /** List of cached ZkEvents */
    private Map<Method,Set<MethodCache>> zkEvents = new HashMap<Method, Set<MethodCache>>();
     
     /** List of cached Commands */
     private Map<Method,Cache> commands = new HashMap<Method, Cache>();
 
     /**
      * Returns AnnotationProcessor for existing class
      *
      * @param type annotated class
      * @return processor
      */
     public static <T> AnnotationProcessor<T> getProcessor( Class<T> type ) {
         AnnotationProcessor<T> ap = processors.get( type );
         if ( ap == null ) {
             ap = new AnnotationProcessor<T>( type );
             if ( cache ) // if caching is enabled
                 processors.put( type, ap );
         }
         return ap;
     }
 
     /**
      * <p>Registers all detected events to the component and bound events to
      * given controller. Bounds invokes to the components to handle
      * executions.</p>
      *
      * @param root targeted master component
      * @param controller object with given
      */
     public void registerZkEventsTo( Component root, T controller ) {
        for ( final Set<MethodCache> set : zkEvents.values() ) {
            for ( final MethodCache cached : set ) {
                // get the target component
                final Component target = cached.invoker.bind( root );
                // build the event context
                final ZkEventContext context = new ZkEventContext( cached.method, cached.invoker, controller, root );
                // attach listener
                target.addEventListener( cached.methodInvoker.getEventName(), new InvokeListener( context ) );
            }
         }
     }
 
     private AnnotationProcessor( Class<T> type ) {
         for ( final Method method : ReflectionHelper.getAllMethods( type ) ) {
             // extract core invokers
             List<Invoke> invokers = processInvokerAnnotations( method );
             
             // extract wrapping invokers, preserves order
             List<Invoke> wrapped = processWrappingAnnotations( method, invokers );
             
             // cache and separate invokers
             store( method, invokers, wrapped );
         }
     }
     
     /**
      * Classifies the zkEvent invokers and command invokers to sepparate maps and stores
      * them into the cache to allow quick reattatching without another class processing.
      * 
      * Both list with invokers should be same sized.
      * 
      * @param method proccessed method
      * @param invokers core invokers
      * @param wrapped  wrapped invokers
      */
     private void store( final Method method, final List<Invoke> invokers, final List<Invoke> wrapped ) {
         assert invokers.size() == wrapped.size();
               
        zkEvents.put( method, new HashSet<MethodCache>() );
         
         for ( int i = 0; i < invokers.size(); ++i ) {
             // core invoker
             final Invoke invoker = invokers.get( i );
             // wrapped invoker
             final Invoke wrapper = wrapped.get( i );
             
             if ( invoker instanceof MethodInvoker ) {
                
                 // store method invoker
                zkEvents.get( method ).add( new MethodCache( method, (MethodInvoker) invoker, wrapper ) );
             } else {
                 // store command invoker
                 commands.put( method, new Cache( method, wrapper ) );
             }            
         }
     }
 
     /**
      * <p>Basic method responsible for processing annotations on the methods.
      * This method is called by composer processor and the rest of annotating is
      * proceed here.</p>
      *
      * <p>This method holds processing order of annotations. There is defined
      * exact order from the most inner to the most outer. If somebody whats to
      * add another annotation then he have to decide which annotatin should it
      * be and set its order in decorater order. Then the adding the new row on
      * the rigth place is enough to register it.</p>
      * 
      * <p>This method processes the core invokers only.</p>
      *
      * @param method proceed method
      */
     private List<Invoke> processInvokerAnnotations( Method method ) {
         // core invokers
         List<Invoke> invokers = new ArrayList<Invoke>();
         
         // read all core invokers
         for ( Initializer initializer : initializers ) {
             invokers.addAll( initializer.process( method ) );
         }
         
         return invokers;
     }
     
     /**
      * <p>Basic method responsible for processing annotations on the methods.
      * This method is called by composer processor and the rest of annotating is
      * proceed here.</p>
      *
      * <p>This method holds processing order of annotations. There is defined
      * exact order from the most inner to the most outer. If somebody whats to
      * add another annotation then he have to decide which annotatin should it
      * be and set its order in decorater order. Then the adding the new row on
      * the rigth place is enough to register it.</p>
      * 
      * <p>This method processes the wrapping invokers only.</p>
      *
      * @param method proceed method
      */
     private List<Invoke> processWrappingAnnotations( Method method, List<Invoke> invokers ) {
         // wrapped invokers
         List<Invoke> invokes = new ArrayList<Invoke>();
         
         if ( !invokers.isEmpty() ) {
             invokes.addAll( invokers );
             final List<Invoke> output = new LinkedList<Invoke>();
             for ( Invoke invoke : invokes ) {
                 output.add( processWrappers( method, invoke) );
             }
             invokes = output;
         }
         
         return invokes;
     }
     
     
     /** 
      
      * Process all annotation wrappers on given method and decorate the given
      * core invoker.
      * @param method method to be processed
      * @param invoker invoker to be decorated
      * @return decorated invoker
      */
     private Invoke processWrappers( Method method, Invoke invoker ) {
         Invoke invoke = invoker;
         for ( Wrapper wrapper : wrappers ) {
             invoke = wrapper.process( method, invoke );
         }
         return invoke;
     }
 
     private static class Cache {
 
         /** method to be executed */
         protected final Method method;
 
         /** wrapped invoker */
         protected final Invoke invoker;
 
         public Cache( Method method, Invoke invoker ) {
             this.method = method;
             this.invoker = invoker;
         }
     }
 
     private static class MethodCache extends Cache {
 
         /** inner invoker */
         protected MethodInvoker methodInvoker;
 
         public MethodCache( Method method, MethodInvoker methodInvoker, Invoke wrappedInvoker ) {
             super( method, wrappedInvoker );
             this.methodInvoker = methodInvoker;
         }
     }
 }
