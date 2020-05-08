 package de.zib.gndms.gndmc.gorfx;
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
 
 import de.zib.gndms.common.model.gorfx.types.*;
 import de.zib.gndms.common.rest.Facets;
 import de.zib.gndms.common.rest.GNDMSResponseHeader;
 import de.zib.gndms.common.rest.Specifier;
 import org.springframework.http.HttpStatus;
 import org.springframework.http.ResponseEntity;
 import org.springframework.web.client.HttpClientErrorException;
 
 import java.util.List;
 import java.util.UUID;
 
 /**
  * @author try ma ik jo rr a zib
  * @date 14.03.11  11:38
  * @brief Performs all requests necessary for taskflow execution.
  *
  * To put the caller in control over the taskflow this class provides
  * handler methods for the result of imported calls. 
  *
  * \note The handler methods are only called if the preceding server response
  * was positive.
  */
 public abstract class AbstractTaskFlowExecClient implements TaskStatusHandler {
 
     private GORFXClient gorfxClient; ///< A ready to uses instance of the gorfx client.
     private TaskFlowClient tfClient; ///< A ready to uses instance of the taskflow client.
     private TaskClient taskClient;   ///< A ready to uses instance of the task client.
     private long pollingDelay = 1000; ///< delay in ms to poll the task status, once the task is running.
 
 
     /**
      * @brief Executes a complete task flow.
      *
      * @param order The order of the taskflow.
      * @param dn    The DN of the user calling the task flow.
      * \note for now the workflow id is generated on the fly.
      */
     public void execTF( Order order, String dn ) {
 
         execTF( order, dn, true, null );
     }
 
 
     /**
      *
      * @brief Executes a complete task flow.
      *
      * This method is imported when you want to understand the
      * Taskflow protocol.
      *
      * @param order The order of the taskflow.
      * @param dn    The DN of the user calling the task flow.
      *              
      * \note for now the workflow id is generated on the fly.
      * @param withQuote Activates co-scheduling
      * @param desiredQuote A quote holding desired time values for
      * the tasflow execution. 
      */
     public void execTF( Order order, String dn, boolean withQuote, final Quote desiredQuote ) {
 
         String wid = UUID.randomUUID().toString();
 
         GNDMSResponseHeader context = setupContext( new GNDMSResponseHeader() );
 
         if( null == gorfxClient ) {
             throw new IllegalStateException( "You need to set gorfxClient before executing a TaskFlow!" );
         }
 
         /**
          * \code this is important
          */              
 
         // sends the order and creates the task flow
         ResponseEntity<Specifier<Facets>> res = gorfxClient.createTaskFlow( order.getTaskFlowType(), order, dn, wid, context );
 
        if ( !HttpStatus.CREATED.equals( res.getStatusCode() ) )
            throw new RuntimeException( "createTaskFlow failed " + res.getStatusCode().name() );
 
         // the taskflow id is stored under "id" in the urlmap
         String tid = res.getBody().getUriMap().get( "id" );
 
         Integer q = null;
         if( withQuote ) {
             if( null == tfClient ) {
                 throw new IllegalStateException( "No TaskFlowClient set." );
             }
 
             if( desiredQuote != null ) {
                 tfClient.setQuote( order.getTaskFlowType(), tid, desiredQuote, dn, wid );
             }
             // queries the quotes for the task flow
             ResponseEntity<List<Specifier<Quote>>> res2 = tfClient.getQuotes( order.getTaskFlowType(), tid, dn, wid );
 
             if ( !HttpStatus.OK.equals( res2.getStatusCode() ) )
                 throw new RuntimeException( "getQuotes failed " + res2.getStatusCode().name() );
 
             // lets the implementors of this class choose a quote
             q = selectQuote( res2.getBody() );
         }
 
         //
         // 'til here it is valid to change the order and request new quotes
         // 
         
         // accepts quote q and triggers task creation
         ResponseEntity<Specifier<Facets>> res3 = tfClient.createTask( order.getTaskFlowType(), tid, q, dn, wid );
 
         if(! HttpStatus.CREATED.equals( res3.getStatusCode() ) )
             throw new RuntimeException( "createTask failed " + res3.getStatusCode().name() );
 
         final Specifier<Facets> taskSpecifier = res3.getBody();
 
         // let the implementor do smart things with the task specifier
         handleTaskSpecifier( taskSpecifier );
 
 
         // the task id is stored under "taskId" in the specifiers urlmap
         waitForFinishOrFail( taskSpecifier, this, taskClient, pollingDelay, dn, wid );
 
         /**
          * \endcode
          */
     }
 
 
     /**
      * Polls a running task, until its either finished or failed.
      *
      * @param taskSpecifier The specifier of the task.
      * @param statusHandler The handler for the task status, can update some sort of UI.
      * @param taskClient  The task client, which should be used for polling.
      * @param pollingDelay The pollingDelay, its the delay between polling.
      * @param dn  The user DN.
      * @param wid The workflow id.
      *
      * @return The final task status, finished or failed.
      */
     public static TaskStatus waitForFinishOrFail( final Specifier<Facets> taskSpecifier,
                                                   final TaskStatusHandler statusHandler,
                                                   final TaskClient taskClient, 
                                                   final long pollingDelay,
                                                   final String dn,
                                                   final String wid )
     {
 
         TaskStatus ts;
         String taskId = taskSpecifier.getUriMap().get( "taskId" );
 
         ResponseEntity<TaskStatus> stat;
         boolean done = false;
         do {
             // queries the status of the task execution
             stat = taskClient.getStatus( taskId, dn, wid );
             if(! HttpStatus.OK.equals( stat.getStatusCode() ) )
                 throw new RuntimeException( "Task::getStatus failed " + stat.getStatusCode().name() );
             ts =  stat.getBody();
 
             // allows the implementor to do something with the task status
             statusHandler.handleStatus( ts );
             try {
                 Thread.sleep( pollingDelay );
             } catch ( InterruptedException e ) {
                 throw new RuntimeException( e );
             }
 
             // finished without an error, good(?)
             if( finished( ts ) ) {
                 // collect the result
                 ResponseEntity<TaskResult> tr = null;
                 try {
                     tr = taskClient.getResult( taskId, dn, wid );
                 } catch( HttpClientErrorException e ) {
                     if( 404 == e.getStatusCode().value() )
                         continue;
                 }
 
                 if(! HttpStatus.OK.equals( tr.getStatusCode() ) )
                     throw new RuntimeException( "Failed to obtain task result " + tr.getStatusCode().name() );
 
                 // do something with it
                 statusHandler.handleResult( tr.getBody() );
 
                 done = true;
             }
             else if( failed( ts ) ) { // must be failed, not so good
                 // find out way
                 ResponseEntity<TaskFailure> tf = taskClient.getErrors( taskId, dn, wid );
                 if(! HttpStatus.OK.equals( tf.getStatusCode() ) )
                     throw new RuntimeException( "Failed to obtain task errors " + tf.getStatusCode().name() );
 
                 // handle the failure
                 statusHandler.handleFailure( tf.getBody() );
 
                 done = true;
             }
         } while( ! done ); // run 'til the task hits a final state
 
         return ts;
     }
 
 
     /**
      * Same as the above method, but without a status handler.
      *
      * @param taskSpecifier The specifier of the task.
      * @param taskClient  The task client, which should be used for polling.
      * @param pollingDelay The pollingDelay, its the delay between polling.
      * @param dn  The user DN.
      * @param wid The workflow id.
      *
      * @return The final task status, finished or failed.
      */
     public static TaskStatus waitForFinishOrFail( final Specifier<Facets> taskSpecifier,
                                                   final TaskClient taskClient,
                                                   final long pollingDelay,
                                                   final String dn,
                                                   final String wid )
     {
        return waitForFinishOrFail( taskSpecifier, new LazyStatusHandler(), taskClient,
                pollingDelay,dn, wid );
     }
 
 
     /**
      * Offers implementing clients the possibility to add values to the request context.
      *
      * The request context is used to create taskflows and the right place to provide
      * myProxyTokens.
      *
      * @param context The create request context.
      * @return The augmented context
      */
     protected GNDMSResponseHeader setupContext( final GNDMSResponseHeader context ) {
 
         // example: context.addMyProxyToken( "c3grid", "foo", "bar" );
         return context;
     }
 
 
     /**
      * @brief Allows the caller to select a quote.
      * 
      * @param quotes All available quotes.
      * 
      * @return The index of the accepted quote. \c null will disable
      *         quote usage.
      */
     protected abstract Integer selectQuote( List<Specifier<Quote>> quotes );
 
     /** 
      * @brief Allows additional handling for the task specifier.
      * 
      * @param ts The task specifier, including all task facets as
      * payload.
      */
     protected abstract void handleTaskSpecifier( Specifier<Facets> ts );
 
     /** 
      * @brief Handler for the task result.
      *
      * Override this method to gain access to the task(flow) result an
      * send it to the user, post process it or store it for later
      * usage.
      *@param res The result object.
      */
     public abstract void handleResult( TaskResult res );
 
     /** 
      * @brief Handler for task failures.
      * 
      * Override this method to gain access to the task(flow) error
      * object, e.g. to send an error-report to someone who cares.
      *
      * @param fail The failure object.
      */
     public abstract void handleFailure( TaskFailure fail );
 
 
     /** 
      * @brief Checks if ts is FINISHED.
      * 
      * @param ts The current task state.
      * 
      * @return \c true if ts is FINISHED
      */
     private static boolean finished( TaskStatus ts ) {
         return TaskStatus.Status.FINISHED.equals( ts.getStatus() );
     }
 
 
     /** 
      * @brief Checks if ts is FINISHED.
      * 
      * @param ts The current task state.
      * 
      * @return \c true if ts is FINISHED
      */
     private static boolean failed( TaskStatus ts ) {
         return TaskStatus.Status.FAILED.equals( ts.getStatus() );
     }
 
 
     /**
      * @brief Delivers the value of ::gorfxClient.
      * 
      * @return The value of ::gorfxClient.
      */
     public GORFXClient getGorfxClient() {
         return gorfxClient;
     }
 
 
     /**
      * @brief Sets the value of ::gorfxClient to \e gorfxClient.
      * 
      * @param gorfxClient The new value of ::gorfxClient.
      */
     public void setGorfxClient( GORFXClient gorfxClient ) {
         this.gorfxClient = gorfxClient;
     }
 
 
     /**
      * @brief Delivers the value of ::tfClient.
      * 
      * @return The value of ::tfClient.
      */
     public TaskFlowClient getTfClient() {
         return tfClient;
     }
 
 
     /**
      * @brief Sets the value of ::tfClient to \e tfClient.
      * 
      * @param tfClient The new value of ::tfClient.
      */
     public void setTfClient( TaskFlowClient tfClient ) {
         this.tfClient = tfClient;
     }
 
 
     /**
      * @brief Delivers the value of ::taskClient.
      * 
      * @return The value of ::taskClient.
      */
     public TaskClient getTaskClient() {
         return taskClient;
     }
 
 
     /**
      * @brief Sets the value of ::taskClient to \e taskClient.
      * 
      * @param taskClient The new value of ::taskClient.
      */
     public void setTaskClient( TaskClient taskClient ) {
         this.taskClient = taskClient;
     }
 
 
     /**
      * @brief Delivers the value of ::pollingDelay.
      * 
      * @return The value of ::pollingDelay.
      */
     public long getPollingDelay() {
         return pollingDelay;
     }
 
 
     /**
      * @brief Sets the value of ::pollingDelay to \e pollingDelay.
      * 
      * @param pollingDelay The new value of ::pollingDelay.
      */
     public void setPollingDelay( long pollingDelay ) {
         this.pollingDelay = pollingDelay;
     }
 
 
     public static class LazyStatusHandler implements TaskStatusHandler {
 
         @Override
         public void handleStatus( final TaskStatus stat ) {
             // this handler is lazy, it does nothing
         }
 
 
         @Override
         public void handleResult( final TaskResult body ) {
             // not required here
         }
 
 
         @Override
         public void handleFailure( final TaskFailure body ) {
             // not required here
         }
     }
 }
