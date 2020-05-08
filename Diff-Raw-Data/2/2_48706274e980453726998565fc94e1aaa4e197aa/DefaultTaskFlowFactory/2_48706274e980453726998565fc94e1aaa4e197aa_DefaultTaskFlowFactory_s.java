 /*
 * Copyright 2008-${YEAR} Zuse Institute Berlin (ZIB)
  *
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  */
 
 package de.zib.gndms.logic.model.gorfx.taskflow;
 
 import com.google.common.cache.Cache;
 import com.google.common.cache.CacheBuilder;
 import com.google.common.cache.CacheLoader;
 import de.zib.gndms.common.model.gorfx.types.Order;
 import de.zib.gndms.common.model.gorfx.types.TaskFlowInfo;
 import de.zib.gndms.logic.model.gorfx.AbstractQuoteCalculator;
 import de.zib.gndms.model.common.repository.Dao;
 import de.zib.gndms.model.common.repository.TransientDao;
 import de.zib.gndms.model.gorfx.types.DelegatingOrder;
 import de.zib.gndms.neomodel.common.Session;
 import de.zib.gndms.neomodel.gorfx.TaskFlow;
 import de.zib.gndms.neomodel.gorfx.TaskFlowType;
 import de.zib.gndms.stuff.GNDMSInjector;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.util.ArrayList;
 import java.util.Map;
 import java.util.NoSuchElementException;
 import java.util.UUID;
 import java.util.concurrent.TimeUnit;
 
 /**
  * @author Maik Jorra
  * @email jorra@zib.de
  * @date 20.07.11  16:33
  * @brief
  */
 public abstract class DefaultTaskFlowFactory<O extends Order, C extends AbstractQuoteCalculator<O>> implements TaskFlowFactory<O, C> {
 
     public final int MAX_CACHE_SIZE = 10000;
     protected Logger logger = LoggerFactory.getLogger( this.getClass() );
     private String taskFlowKey;
     private GNDMSInjector injector;
     private Class<C> calculatorClass;
     private Class<O> orderClass;
     private final Dao<String, TaskFlow<O>, Void> taskFlows = new TransientDao<String, TaskFlow<O>, Void>() {
         {
             setModels(
                 (Cache<String,TaskFlow<O>>) (Object) CacheBuilder.newBuilder()
                     .expireAfterAccess( 12, TimeUnit.HOURS )
                     .maximumSize( MAX_CACHE_SIZE )
                     .initialCapacity( 100 )
                     .build( new CacheLoader<String, TaskFlow<O>>() {
                         @Override
                         public TaskFlow<O> load( String key ) throws Exception {
                             DefaultTaskFlowFactory.this.logger.trace( "load: "+ key );
                             return new CreatableTaskFlow<O>( key );
                         }
                     } )
             );
         }
 
 
         @Override
         public String create() {
             String id = UUID.randomUUID().toString();
             cacheGet( id );
             return id;
         }
     };
 
 
 
 
 
     protected DefaultTaskFlowFactory( String taskFlowKey, Class<C> calculatorClass, Class<O> orderClass ) {
         this.taskFlowKey = taskFlowKey;
         this.calculatorClass = calculatorClass;
         this.orderClass = orderClass;
     }
 
 
     @Override
     public String getTaskFlowKey() {
         return taskFlowKey;  // not required here
     }
 
 
     @Override
     public int getVersion() {
         return 0;
     }
 
 
     @Override
     public C getQuoteCalculator() {
         try {
             return calculatorClass.newInstance();
         // one of the two exceptions are thrown s.th. is wrong with the calculator class
         // and need debugging, so rethrowing is ok
         } catch ( InstantiationException e ) {
             throw new RuntimeException( e );
         } catch ( IllegalAccessException e ) {
             throw new RuntimeException( e );
         }
     }
 
 
     @Override
     public TaskFlowInfo getInfo() {
         return null;  // not required here
     }
 
 
     @Override
     public TaskFlow<O> create() {
 
         String key = taskFlows.create();
         return taskFlows.get( key );
     }
 
 
     @Override
     public TaskFlow<O> createOrphan() {
         return prepare( new CreatableTaskFlow<O>( ) );
     }
 
 
     /**
      * Applies taskFlow specific stuff a newly created taskFlow instance.
      *
      * Stuff is usually the injection of required handlers and the like.
      * @param taskFlow A newly created taskflow.
      * @return The prepared in-parameter.
      */
     protected abstract TaskFlow<O> prepare( TaskFlow<O> taskFlow );
 
 
     @Override
     public boolean adopt( TaskFlow<O> taskflow ) {
 
         String key = taskflow.getId();
         try {
             taskFlows.get( key );
         } catch( NoSuchElementException e ) {
             taskFlows.add( taskflow, key );
             return true;
         }
 
         return false;
     }
 
 
     @Override
     public TaskFlow<O> find( String id ) {
         try {
             return taskFlows.get( id );
         } catch( NoSuchElementException e ) {
             // intentionally
         }
         return null;
     }
 
 
     @Override
     public void delete( String id ) {
         taskFlows.deleteByKey( id );
     }
 
 
     @Override
     public Class<O> getOrderClass() {
         return orderClass;
     }
 
 
     @Override
     public void registerType( Session session ) {
 
         TaskFlowType taskFlowType;
         try {
             taskFlowType = session.findTaskFlowType( getTaskFlowKey() );
             if( taskFlowType != null ) { // this factory is known
                 if( taskFlowType.getVersion() == getVersion() ) { // and the version is ok, too
                     session.success();
                     return;
                 } else  { // version mismatch
                     taskFlowType.delete(); // delete it
                     taskFlowType = null; // and make new one
                 }
             }
             //  setup fresh this factory is new
             taskFlowType = session.createTaskFlowType();
             setupTaskFlowType( taskFlowType );
 
         } finally { session.finish(); }
     }
 
 
     /**
      * Initial configuration of the taskflowType
      *
      * Override this method to customize the taskflowType, default setup is fine in most cases.
      *
      * @param taskFlowType A newly created instance of the taskflowtype
      *
      * @note This method is only called when the factory is created for the first time, even after a shutdown it won't
      * be called again. Except a change of the version number of this factory.
      */
     protected void setupTaskFlowType( TaskFlowType taskFlowType )  {
 
         taskFlowType.setTaskFlowTypeKey( this.getTaskFlowKey() );
         taskFlowType.setVersion( getVersion() );
         taskFlowType.setCalculatorFactoryClassName( this.getClass().getName() );
         taskFlowType.setTaskActionFactoryClassName( this.getClass().getName() );
         taskFlowType.setConfigMapData( getDefaultConfig() );
     }
 
     /**
      * Implementing classes should provide a useful default config.
      *
      * @return The default configuration of the map.
      *
      * @note This method is only called when the factory is created for the first time, even after a shutdown it won't
      * be called again. Except a change of the version number of this factory.
      */
    protected abstract Map<String,String> getDefaultConfig();
 
 
     @Override
     public Iterable<String> depends() {
         return new ArrayList<String>( 0 );
     }
 
 
     @Override
     public DelegatingOrder<O> getOrderDelegate( O order ) {
         return new DelegatingOrder<O>( order );
     }
 
 
     protected void injectMembers( C newInstance ) {
         getInjector().injectMembers( newInstance );
     }
 
 
     public GNDMSInjector getInjector() {
 
         return injector;
     }
 
 
     public void setInjector( final GNDMSInjector injector ) {
 
         this.injector = injector;
     }
 
 
     // i'd love to put this class in the factories interface where it belongs,
     // but thanks to poor design of the java language this is not possible...
     protected static class CreatableTaskFlow<O extends Order> extends TaskFlow<O> {
 
         public CreatableTaskFlow() {
         }
 
 
         public CreatableTaskFlow( String id ) {
             super( id );
         }
 
 
         public void setId( String id ) {
             super.setId( id );
         }
     }
 }
