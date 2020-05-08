 package de.zib.gndms.taskflows.failure.server;
 
 
 /*
  * Copyright 2008-2011 Zuse Institute Berlin (ZIB)
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 
 import de.zib.gndms.kit.config.MapConfig;
 import de.zib.gndms.logic.model.gorfx.TaskFlowAction;
 import de.zib.gndms.model.gorfx.types.DelegatingOrder;
 import de.zib.gndms.model.gorfx.types.TaskState;
 import de.zib.gndms.neomodel.common.Dao;
 import de.zib.gndms.neomodel.common.Session;
 import de.zib.gndms.neomodel.gorfx.Taskling;
 import de.zib.gndms.taskflows.failure.client.FailureTaskFlowMeta;
 import de.zib.gndms.taskflows.failure.client.model.FailureOrder;
 import de.zib.gndms.taskflows.failure.server.utils.ExceptionThrower;
 import org.jetbrains.annotations.NotNull;
 
 import javax.persistence.EntityManager;
 import java.lang.reflect.Constructor;
 import java.lang.reflect.InvocationTargetException;
 
 /**
  * @author bachmann@zib.de
  * @date 14.02.11  17:53
  * @brief The action of the dummy task flow.
  *
  * @see FailureOrder
  */
 public class FailureTFAction extends TaskFlowAction< FailureOrder > {
 
     @Override
     public Class< FailureOrder > getOrderBeanClass( ) {
         return FailureOrder.class;
     }
 
 
     public FailureTFAction() {
         super( FailureTaskFlowMeta.TASK_FLOW_TYPE_KEY );
     }
 
 
     public FailureTFAction(@NotNull EntityManager em, @NotNull Dao dao, @NotNull Taskling model) {
 
         super( em, dao, model );
         setKey( FailureTaskFlowMeta.TASK_FLOW_TYPE_KEY );
     }
 
 
     @Override
     protected void onCreated(@NotNull String wid,
                              @NotNull TaskState state, boolean isRestartedTask, boolean altTaskState)
             throws Exception
     {
         ensureOrder();
         FailureOrder order = getOrder().getOrderBean();
 
         handle(FailureOrder.FailurePlace.CREATED, true);
         super.onCreated( wid, state, isRestartedTask, altTaskState );
         handle( FailureOrder.FailurePlace.CREATED, false );
     }
 
     @Override
     protected void onInitialized(@NotNull String wid,
                                  @NotNull TaskState state, boolean isRestartedTask, boolean altTaskState)
             throws Exception {
         ensureOrder();
         FailureOrder order = getOrder().getOrderBean();
 
         handle( FailureOrder.FailurePlace.INITIALIZED, true );
         super.onInitialized(wid, state, isRestartedTask, altTaskState);
         handle(FailureOrder.FailurePlace.INITIALIZED, false);
     }
 
     @Override
     protected void onInProgress(@NotNull String wid,
                                 @NotNull TaskState state, boolean isRestartedTask, boolean altTaskState)
             throws Exception {
         ensureOrder();
         FailureOrder order = getOrder().getOrderBean();
 
         handle(FailureOrder.FailurePlace.INPROGRESS, true);
         super.onInProgress(wid, state, isRestartedTask, altTaskState);
         handle( FailureOrder.FailurePlace.INPROGRESS, false );
     }
 
     @Override
     protected void onFinished(@NotNull String wid,
                               @NotNull TaskState state, boolean isRestartedTask, boolean altTaskState)
             throws Exception {
         ensureOrder();
         FailureOrder order = getOrder().getOrderBean();
 
         handle( FailureOrder.FailurePlace.FINISHED, true );
         super.onFinished(wid, state, isRestartedTask, altTaskState);
         handle(FailureOrder.FailurePlace.FINISHED, false);
     }
 
     @Override
     protected void onFailed(@NotNull String wid,
                             @NotNull TaskState state, boolean isRestartedTask, boolean altTaskState) throws Exception {
         ensureOrder();
         FailureOrder order = getOrder().getOrderBean();
 
         handle( FailureOrder.FailurePlace.FAILED, true );
         super.onFailed(wid, state, isRestartedTask, altTaskState);
         handle( FailureOrder.FailurePlace.FAILED, false );
     }
 
     
     private void handle( FailureOrder.FailurePlace where, boolean beforeSuper ) throws InterruptedException {
         ensureOrder();
         FailureOrder order = getOrder().getOrderBean();
 
         if( beforeSuper )
             Thread.sleep( order.getSleepBeforeSuper( where ) );
         else
             Thread.sleep( order.getSleepAfterSuper( where ) );
 
         if( where.equals( order.getWhere() ) && beforeSuper == order.isBeforeSuper() ) {
             if( order.isThrowInSession() ) {
                 final Session session = getDao().beginSession();
                 try {
                    ExceptionThrower.throwUnchecked( order.getException() );
                     session.success();
                 }
                 finally { session.finish(); }
             }
             else
                ExceptionThrower.throwUnchecked( order.getException() );
         }
     }
     
     
     private Throwable createException( final String className, final String message ) {
         final Class clazz;
         try {
             clazz = Class.forName( className );
         } catch( ClassNotFoundException e ) {
             throw new IllegalArgumentException( "Could not find class " + className );
         }
         
         if( ! Throwable.class.isAssignableFrom( clazz ) ) {
             throw new IllegalArgumentException( "Class " + className + " needs be a Throwable derivative!");
         }
 
         boolean argument = true;
         Constructor constructor;
         try {
             constructor = clazz.getConstructor( new Class<?>[]{ String.class } );
         } catch( NoSuchMethodException e ) {
             try {
                 constructor = clazz.getConstructor( new Class<?>[]{ } );
                 argument = false;
             } catch (NoSuchMethodException e1) {
                 throw new IllegalArgumentException( "Class " + className + " has no constructor with a String argument. THIS IS NOT HAPPENING :/" );
             }
         }
 
         try {
             if( argument )
                 return ( Throwable )constructor.newInstance( message );
             else
                 return ( Throwable )constructor.newInstance();
         } catch( InstantiationException e ) {
             throw new IllegalStateException( e );
         } catch( IllegalAccessException e ) {
             throw new IllegalStateException( e );
         } catch( InvocationTargetException e ) {
             throw new IllegalStateException( e );
         }
     }
 
 
     protected @NotNull
     MapConfig getOfferTypeConfig() {
         return new MapConfig( getTaskFlowTypeConfigMapData() );
     }
 
 
     @Override
     public DelegatingOrder<FailureOrder> getOrder() {
 
         DelegatingOrder< FailureOrder > order = null;
 
         final Session session = getDao().beginSession();
         try {
             order = ( DelegatingOrder< FailureOrder > ) getTask( session ).getOrder( );
             session.success();
         }
         finally { session.finish(); }
 
         return order;
     }
 }
