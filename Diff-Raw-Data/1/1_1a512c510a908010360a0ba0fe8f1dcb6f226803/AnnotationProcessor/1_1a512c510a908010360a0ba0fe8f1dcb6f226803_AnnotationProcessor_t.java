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
 import org.zkoss.lang.Library;
 import org.zkoss.zk.ui.Component;
 
 /**
  * <p>Annotation Processor handles all ZK annotations on the methods.
  * For each annotation there is defined specific annotation processor 
  * which handles that annotation type and provides desired functionality.
  * AnnotationProcessor cares about right order of decorating {@link Invoke}
  * object because if they would be decorated in wrong order then the
  * final functionality could be undeterministic or at least undesired.</p>
  *
  * <p>Instance of annotation processor is bound to the instance of component
  * controller but there is possible way to refactor it as unbound to anything.
  * Then the class would create templates which would be cloned and bound to 
  * the specific instance.</p>
  *
  * @author Karel Čemus <cemus@datalite.cz>
  */
 public class AnnotationProcessor<T> {
 
     /** Map of created processors. Serves as annotation cache */
     private static final Map< Class, AnnotationProcessor> processors = new HashMap<Class, AnnotationProcessor>();
 
     /** list of processors producing method invokers or similar */
     private static final List<Initializer> initializers = new ArrayList<Initializer>();
 
     /** list of processors producing wrappers for invoke object providing additional functionality */
     private static final List<Wrapper> wrappers = new ArrayList<Wrapper>();
 
     /** configuration key in property files */
     private static final String CONFIG = "zk-dl.annotation.cache";
 
     /** caching is enabled / disabled */
     private static boolean cache = true;
 
     static {
         initializers.add(new GeneralInitializerProcessor(ZkEvent.class, MethodInvoker.class));
         initializers.add(new GeneralInitializerProcessor(ZkEvents.class, MethodInvoker.class));
 
         wrappers.add(new GeneralWrapperProcessor(ZkException.class, ZkExceptionHandler.class));
        wrappers.add(new GeneralWrapperProcessor(ZkExceptions.class, ZkExceptionHandler.class));
         wrappers.add(new GeneralWrapperProcessor(ZkBinding.class, ZkBindingHandler.class));
         wrappers.add(new GeneralWrapperProcessor(ZkBindings.class, ZkBindingHandler.class));
         wrappers.add(new GeneralWrapperProcessor(ZkConfirm.class, ZkConfirmHandler.class));
         wrappers.add(new GeneralWrapperProcessor(ZkBlocking.class, ZkBlockingHandler.class));
         wrappers.add(new GeneralWrapperProcessor(ZkAsync.class, ZkAsyncHandler.class));
 
         // loading property of caching or not
         cache = Boolean.parseBoolean( System.getProperty( CONFIG, Library.getProperty( CONFIG, "true" ) ) );
     }
 
     /** List of all events gathered from class */
     private List<Invoke> invokes = new LinkedList<Invoke>();
 
     /**
      * Returns AnnotationProcessor for existing class
      * @param type annotated class
      * @return processor
      */
     public static <T> AnnotationProcessor<T> getProcessor( Class<T> type ) {
         AnnotationProcessor<T> ap = processors.get( type );
         if ( ap == null ) {
             ap = new AnnotationProcessor<T>( type );
             if ( cache ) { // if caching is enabled
                 processors.put( type, ap );
             }
         }
         return ap;
     }
 
     /**
      * <p>Registers all detected events to the component and bound events
      * to given controller. Bounds invokes to the components to handle executions.</p>
      * @param component targeted master component
      * @param controller object with given
      */
     public void registerTo( Component component, T controller ) {
         for ( final Invoke invoke : invokes ) {
             invoke.bind( component ).addEventListener( invoke.getEvent(), new InvokeListener( invoke, component, controller ) );
         }
     }
 
     private AnnotationProcessor( Class<T> type ) {
         for ( final Method method : ReflectionHelper.getAllMethods( type ) ) {
             List<Invoke> result = processAnnotations( method );
             invokes.addAll( result );
         }
     }
 
     /**
      * <p>Basic method responsible for processing annotations on the methods. This method
      * is called by composer processor and the rest of annotating is proceed here.</p>
      *
      * <p>This method holds processing order of annotations. There is defined exact
      * order from the most inner to the most outer. If somebody whats to add another
      * annotation then he have to decide which annotatin should it be and set its
      * order in decorater order. Then the adding the new row on the rigth place 
      * is enough to register it.</p>
      * @param method proceed method
      */
     private List<Invoke> processAnnotations( Method method ) {
         List<Invoke> events = new ArrayList<Invoke>();
 
         for ( Initializer initializer : initializers ) {
             events.addAll( initializer.process( method ) );
         }
 
         if ( !events.isEmpty() ) {
             List<Invoke> output = new LinkedList<Invoke>();
             for ( Invoke invoke : events ) {
                 for ( Wrapper wrapper : wrappers ) {
                     invoke = wrapper.process( method, invoke );
                 }
                 invoke.setSource(invoke);
                 output.add(invoke);
             }
             events = output;
         }
         return events;
     }
 }
