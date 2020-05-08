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
 
 import cz.datalite.zk.annotation.ZkBinding;
 import cz.datalite.zk.annotation.ZkBindings;
 import cz.datalite.zk.annotation.ZkConfirm;
 import cz.datalite.zk.annotation.ZkEvent;
 import cz.datalite.zk.annotation.ZkEvents;
 import cz.datalite.zk.annotation.ZkException;
 import cz.datalite.zk.annotation.invoke.Invoke;
 import java.lang.annotation.Annotation;
 import java.lang.reflect.Method;
 import java.util.Collections;
 import java.util.List;
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
 public class AnnotationProcessor {
 
     /** Master component, the controller is bound to it */
     final Component master;
 
     /** Instance of proceeded class */
     Object controller;
 
     public AnnotationProcessor( Component target, Object controller ) {
         this.master = target;
         this.controller = controller;
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
     public void processAnnotations( Method method ) {
         List<Invoke> invokes = Collections.emptyList();
         // the most inner object, invoked last (if the previous proceed right and event is not dropped)
         invokes = processAnnotation( ZkEventsProcessor.INSTANCE, ZkEvents.class, invokes, method );
         invokes = processAnnotation( ZkEventProcessor.INSTANCE, ZkEvent.class, invokes, method );
         invokes = processAnnotation( ZkExceptionProcessor.INSTANCE, ZkException.class, invokes, method );
         invokes = processAnnotation( ZkBindingProcessor.INSTANCE, ZkBinding.class, invokes, method );
         invokes = processAnnotation( ZkBindingsProcessor.INSTANCE, ZkBindings.class, invokes, method );
         invokes = processAnnotation( ZkConfirmProcessor.INSTANCE, ZkConfirm.class, invokes, method );
         // the most outer object, invoked first
 
         registerInvokes( invokes );
     }
 
     /**
      * Method calls annotation processor if the annotation is not null
      * @param <T> type of proceeded annotation
      * @param processor annotation processor which can handle given type of annotation
      * @param type of annotation
      * @param invokes set of inner invokes
      * @param method inspecting method
      * @return set of decorated invokes
      */
     private <T> List<Invoke> processAnnotation( Processor<T> processor, Class<T> type, List<Invoke> invokes, Method method ) {
         T annotation = findAnnotation( method, type );
         if ( annotation != null ) {
             return processor.process( annotation, invokes, method, master, controller );
         }
         return invokes;
     }
 
     /**
      * Finds desired annotation on the given method. If the annotation
      * is not set then method returns null.
      * @param <T> type of annotation
      * @param method investigated method
      * @param type type of annotation
      * @return found annotation or NULL if not set
      */
     private <T> T findAnnotation( Method method, Class<T> type ) {
         for ( Annotation annotation : method.getAnnotations() ) {
             if ( type.isAssignableFrom( annotation.getClass() ) ) {
                 return ( T ) annotation;
             }
         }
         return null;
     }
 
     /**
      * Bounds invokes to the components to handle executions.
      * @param invokes set of invokes to be bound
      */
     private void registerInvokes( List<Invoke> invokes ) {
         for ( final Invoke invoke : invokes ) {
             invoke.getTarget().addEventListener( invoke.getEvent(), new InvokeListener( invoke ) );
         }
     }
 }
