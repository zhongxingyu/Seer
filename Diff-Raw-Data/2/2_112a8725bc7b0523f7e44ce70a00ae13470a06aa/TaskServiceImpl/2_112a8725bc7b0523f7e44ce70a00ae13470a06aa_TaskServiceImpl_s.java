 package de.zib.gndms.GORFX.service;
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
 
 import de.zib.gndms.common.GORFX.service.TaskService;
 import de.zib.gndms.common.model.gorfx.types.*;
 import de.zib.gndms.common.rest.Facet;
 import de.zib.gndms.common.rest.Facets;
 import de.zib.gndms.common.rest.GNDMSResponseHeader;
 import de.zib.gndms.common.rest.UriFactory;
 import de.zib.gndms.common.stuff.devel.NotYetImplementedException;
 import de.zib.gndms.logic.model.gorfx.taskflow.TaskTypeConverter;
 import de.zib.gndms.logic.model.TaskExecutionService;
 import de.zib.gndms.model.gorfx.types.TaskState;
 import de.zib.gndms.neomodel.common.Dao;
 import de.zib.gndms.neomodel.common.Session;
 import de.zib.gndms.neomodel.gorfx.Task;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.http.HttpStatus;
 import org.springframework.http.ResponseEntity;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.*;
 
 import javax.annotation.PostConstruct;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 /**
  * @author try ma ik jo rr a zib
  * @date 01.03.11  12:06
  * @brief Controller for the TaskService.
  */
 @Controller
 @RequestMapping( "/gorfx/tasks" )
 public class TaskServiceImpl implements TaskService {
 
     protected final Logger logger = LoggerFactory.getLogger( this.getClass() );
 
     private TaskExecutionService executor;
     private String serviceUrl;
     private UriFactory uriFactory;
     private List<String> facets;
     private Dao dao;
 
     @PostConstruct
     void init( ) {
         facets = new ArrayList<String>( 3 );
         facets.add( "status" );
         facets.add( "result" );
         facets.add( "errors" );
         uriFactory = new UriFactory( serviceUrl );
     }
 
     @RequestMapping( value = "/", method = RequestMethod.GET )
     public ResponseEntity<TaskServiceInfo> getServiceInfo() {
         return new ResponseEntity<TaskServiceInfo>( new TaskServiceInfo(), null, HttpStatus.OK );
     }
 
 
     @RequestMapping( value = "/config", method = RequestMethod.GET )
     public ResponseEntity<TaskServiceConfig> getServiceConfig( @RequestHeader String dn ) {
         throw new NotYetImplementedException();
     }
 
 
     @RequestMapping( value = "/config", method = RequestMethod.POST )
     public ResponseEntity<String> setServiceConfig( @RequestBody TaskServiceConfig cfg, @RequestHeader String dn ) {
         throw new NotYetImplementedException();
     }
 
 
     @RequestMapping( value = "/_{id}", method = RequestMethod.GET )
     public ResponseEntity<Facets> getTaskFacets( @PathVariable String id, @RequestHeader( "DN" ) String dn ) {
 
         logger.debug( "get task called with id " + id );
         Session session = dao.beginSession();
         try {
             findTask( id, session ); // ensures that id is valid
             session.success();
         } finally {
             session.finish();
         }
 
         Map<String,String> uriargs = new HashMap<String, String>( 2 );
         uriargs.put( UriFactory.SERVICE, "gorfx" );
         uriargs.put( UriFactory.TASK_ID, id );
 
         ArrayList<Facet> fl = new ArrayList<Facet>( facets.size() );
         for( String f : facets ) {
             String fn = uriFactory.taskUri( uriargs, f );
             fl.add( new Facet( f, fn ) );
         }
         return new ResponseEntity<Facets>( new Facets( fl ), getHeader( id, null, dn, null ), HttpStatus.OK );
     }
 
 
     @RequestMapping( value = "/_{id}", method = RequestMethod.DELETE )
     public ResponseEntity<Void> deleteTask( @PathVariable String id, @RequestHeader( "DN" ) String dn,
                                               @RequestHeader( "WId" ) String wid ) {
 
         logger.debug( "delete task called with id " + id );
         Session session = dao.beginSession();
         try {
             // todo check if task for taskling is running
             Task t = findTask( id, session ); // ensures that id is valid
             Task.fullDelete( t, session );
             session.success();
         } finally {
             session.finish();
         }
 
         return new ResponseEntity<Void>( null, getHeader( id, null, dn, wid  ), HttpStatus.OK );
     }
 
 
     @RequestMapping( value = "/_{id}/status", method = RequestMethod.GET )
     public ResponseEntity<TaskStatus> getStatus( @PathVariable String id, @RequestHeader( "DN" ) String dn,
                                                  @RequestHeader( "WId" ) String wid ) {
 
         Session session = dao.beginSession();
         try {
             Task t = findTask( id, session );
             t.getTerminationTime();
             TaskStatus status = TaskTypeConverter.statusFromTask( t );
             session.success();
             return new ResponseEntity<TaskStatus>( status, getHeader( id, "status", dn, wid  ), HttpStatus.OK );
         } finally {
             session.finish();
         }
     }
 
 
 
     @RequestMapping( value = "/_{id}/status", method = RequestMethod.POST )
     public ResponseEntity<Void> changeStatus( @PathVariable String id, @RequestBody TaskControl status,
                                               @RequestHeader( "DN" ) String dn,
                                               @RequestHeader( "WId" ) String wid ) {
 
         // todo set alt status to task
         return null;
     }
 
 
     @RequestMapping( value = "/_{id}/result", method = RequestMethod.GET )
     public ResponseEntity<TaskResult> getResult( @PathVariable String id, @RequestHeader( "DN" ) String dn,
                                                  @RequestHeader( "WId" ) String wid ) {
 
         Session session = dao.beginSession();
         try {
             HttpStatus hs = HttpStatus.NOT_FOUND;
             Task t = findTask( id, session );
             TaskResult res = null;
             if( TaskState.FINISHED.equals( t.getTaskState() ) ) {
                 Object pl = t.getPayload();
                 if( pl != null ) {
                    res = TaskResult.class.cast( res );
                     hs = HttpStatus.OK;
                 }
             }
             session.success();
 
             return new ResponseEntity<TaskResult>( res, getHeader( id, "result", dn, wid  ), hs );
         } finally {
             session.finish();
         }
     }
 
 
     @RequestMapping( value = "/_{id}/errors", method = RequestMethod.GET )
     public ResponseEntity<TaskFailure> getErrors( @PathVariable String id,
                                                   @RequestHeader( "DN" ) String dn,
                                                   @RequestHeader( "WId" ) String wid ) {
 
         Session session = dao.beginSession();
         try {
             HttpStatus hs = HttpStatus.NOT_FOUND;
             Task t = findTask( id, session );
             TaskFailure fail = null;
             if( TaskState.FAILED.equals( t.getTaskState() ) ) {
                 LinkedList<Exception> pl = t.getCause();
                 if( pl != null ) {
                     hs = HttpStatus.OK;
                     fail = TaskTypeConverter.failStackToList( pl );
                 }
             }
             session.success();
 
             return new ResponseEntity<TaskFailure>( fail, getHeader( id, "errors", dn, wid  ), hs );
         } finally {
             session.finish();
         }
     }
 
 
     protected Task findTask( String id, Session session ) throws NoSuchResourceException {
 
         Task t = session.findTask( id );
 
         if( t == null )
             throw new NoSuchResourceException( id );
 
         return t;
     }
 
 
     protected GNDMSResponseHeader getHeader( String id, String facet, String dn, String wid ) {
 
         Map<String,String> uriargs = new HashMap<String, String>( 2 );
         uriargs.put( UriFactory.TASK_ID, id );
         uriargs.put( UriFactory.SERVICE, "gorfx" );
 
         return new GNDMSResponseHeader( uriFactory.taskServiceUri( uriargs ),
             uriFactory.taskUri( uriargs, facet ), serviceUrl, dn, wid );
     }
 
     @ExceptionHandler( NoSuchResourceException.class )
     public ResponseEntity<Void> handleNoSuchResourceException( NoSuchResourceException ex ) {
         logger.debug( "handling exception for: " + ex.getMessage() );
         return new ResponseEntity<Void>( null, getHeader( ex.getMessage(), null, null, null ), HttpStatus.NOT_FOUND );
     }
 
     public void setServiceUrl( String serviceUrl ) {
         this.serviceUrl = serviceUrl;
     }
 
 
     @Autowired
     public void setExecutor( TaskExecutionService executor ) {
         this.executor = executor;
     }
 
     @Autowired
     public void setDao( Dao dao ) {
         this.dao = dao;
     }
 }
