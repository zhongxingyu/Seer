 package de.zib.gndms.GORFX.service;
 
 import de.zib.gndms.GORFX.service.util.WidAux;
 import de.zib.gndms.common.GORFX.service.TaskFlowService;
 import de.zib.gndms.common.model.gorfx.types.*;
 import de.zib.gndms.common.rest.*;
 import de.zib.gndms.common.stuff.devel.NotYetImplementedException;
 import de.zib.gndms.gndmc.gorfx.TaskClient;
 import de.zib.gndms.kit.security.SpringSecurityContextHolder;
 import de.zib.gndms.logic.model.TaskAction;
 import de.zib.gndms.logic.model.TaskExecutionService;
 import de.zib.gndms.logic.model.gorfx.taskflow.TaskFlowAux;
 import de.zib.gndms.logic.model.gorfx.taskflow.TaskFlowFactory;
 import de.zib.gndms.logic.model.gorfx.taskflow.TaskFlowProvider;
 import de.zib.gndms.logic.model.gorfx.taskflow.UnsatisfiableOrderException;
 import de.zib.gndms.model.common.NoSuchResourceException;
 import de.zib.gndms.model.common.PermissionInfo;
 import de.zib.gndms.model.common.PersistentContract;
 import de.zib.gndms.model.gorfx.types.DelegatingOrder;
 import de.zib.gndms.neomodel.common.Dao;
 import de.zib.gndms.neomodel.gorfx.TaskBuilder;
 import de.zib.gndms.neomodel.gorfx.TaskFlow;
 import de.zib.gndms.neomodel.gorfx.Taskling;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.http.HttpHeaders;
 import org.springframework.http.HttpStatus;
 import org.springframework.http.ResponseEntity;
 import org.springframework.security.access.annotation.Secured;
 import org.springframework.security.core.context.SecurityContextHolder;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.*;
 
 import javax.annotation.PostConstruct;
 import javax.inject.Inject;
 import java.util.*;
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
 
 /**
  * @author try ma ik jo rr a zib
  * @date 13.01.2011 15:17:47
  *
  * @brief A controller for a REST taskflow resource which implements the
  * TaskFlowService interface.
  *
  * The taskflow service acts as interface to instantiated taskflow
  * resources. The instantiation or creation happens through the GORFX
  * service itself.
  *
  * This implicates that all method invocation concern a single
  * taskflow, not the service as a whole.
  */
 @Controller
 @RequestMapping( "/gorfx" )
 public class TaskFlowServiceImpl implements TaskFlowService {
 
     // private ORQDao orqDao;
     private String localBaseUrl;
     private String baseUrl; // inject or read from properties, doesn't contain gorfx postfix
     private TaskFlowProvider taskFlowProvider;
     private final List<String> facetsNames = new ArrayList<String>( 6 );
     private UriFactory uriFactory;
     private TaskClient taskClient;
     private final Logger logger = LoggerFactory.getLogger( this.getClass() );
     private TaskExecutionService executorService;
     private Dao dao;
 
 
     @PostConstruct
     public void init() {
         facetsNames.add( "order" );
         facetsNames.add( "quote" );
         facetsNames.add( "task" );
         facetsNames.add( "result" );
         facetsNames.add( "status" );
         facetsNames.add( "errors" );
         uriFactory = new UriFactory(baseUrl);
         taskClient.setServiceURL( localBaseUrl );
     }
 
 
     @RequestMapping( value = "/_{type}/_{id}", method = RequestMethod.GET )
     @Secured( "ROLE_USER" )
     public ResponseEntity<Facets> getFacets( @PathVariable String type, @PathVariable String id, @RequestHeader( "DN" ) String dn ) {
 
         Map<String, String> uriargs = taskFlowUriMap( type, id );
 
         GNDMSResponseHeader header = new GNDMSResponseHeader( uriFactory.taskFlowTypeUri( uriargs, null ), null, baseUrl, dn, null );
 
         if ( taskFlowProvider.exists( type ) ) {
             TaskFlow tf = taskFlowProvider.getFactoryForTaskFlow( type ).find( id );
             if ( tf != null ) {
                 ArrayList<Facet> fl = new ArrayList<Facet>( 6 );
                 for ( String f : facetsNames ) {
                     String fn = uriFactory.taskFlowUri( uriargs, f );
                     fl.add( new Facet( f, fn ) );
                 }
 
                 return new ResponseEntity<Facets>( new Facets( fl ), header, HttpStatus.OK );
             } else
                 logger.debug( "request for non existing " + type + ": " + id );
         } else
             logger.debug( "request for non existing type " + type + ": " + id );
 
         return new ResponseEntity<Facets>( new Facets( ), header, HttpStatus.NOT_FOUND );
     }
 
 
     @RequestMapping( value = "/_{type}/_{id}", method = RequestMethod.DELETE )
     @Secured( "ROLE_USER" )
     public ResponseEntity< Integer > deleteTaskflow( @PathVariable String type, @PathVariable String id,
                                                 @RequestHeader( "DN" ) String dn,
                                                 @RequestHeader( "WId" ) String wid ) {
 
         logger.debug( "removing " + type + " taskflow: " + id );
         HttpStatus hs = HttpStatus.NOT_FOUND;
         if ( taskFlowProvider.exists( type ) ) {
             TaskFlowFactory tff = taskFlowProvider.getFactoryForTaskFlow( type );
             TaskFlow tf = tff.find( id );
             if ( tf != null ) {
                 if ( tf.hasTaskling() )
                     taskClient.deleteTask( tf.getTaskling().getId(), dn, wid );
                 tff.delete( id );
                 hs = HttpStatus.OK;
             }
         }
 
         return new ResponseEntity< Integer >( new Integer( 0 ), getHeader( type, id, null, dn, wid ), hs );
     }
 
 
     @RequestMapping( value = "/_{type}/_{id}/order", method = RequestMethod.GET )
     @Secured( "ROLE_USER" )
     public ResponseEntity<Order> getOrder( @PathVariable String type, @PathVariable String id,
                                            @RequestHeader( "DN" ) String dn,
                                            @RequestHeader( "WId" ) String wid ) {
         HttpStatus hs = HttpStatus.NOT_FOUND;
         Order order = null;
         try {
             TaskFlow tf = findTF( type, id );
             order = tf.getOrder().getOrderBean();
             if ( order != null )
                 hs = HttpStatus.OK;
         } catch ( NoSuchResourceException e ) { /* intentionally */ }
 
         return new ResponseEntity<Order>( order, getHeader( type, id, "order", dn, wid ), hs );
     }
 
 
     @RequestMapping( value = "/_{type}/_{id}/order", method = RequestMethod.POST )
     @Secured( "ROLE_USER" )
     public ResponseEntity<Void> setOrder( @PathVariable String type, @PathVariable String id,
                                           @RequestBody Order orq, @RequestHeader( "DN" ) String dn,
                                           @RequestHeader( "WId" ) String wid ) {
 
         HttpStatus hs = HttpStatus.NOT_FOUND;
         if ( taskFlowProvider.exists( type ) ) {
             final TaskFlowFactory tff = taskFlowProvider.getFactoryForTaskFlow( type );
             final TaskFlow tf = tff.find( id );
             if ( tf != null )
                 hs = TaskFlowServiceAux.setAndValidateOrder( orq, tf, tff );
         }
 
         return new ResponseEntity<Void>( null, getHeader( type, id, "order", dn, wid ), hs );
     }
 
 
     @RequestMapping( value = "/_{type}/_{id}/quote", method = RequestMethod.GET )
     @Secured( "ROLE_USER" )
     public ResponseEntity<List<Specifier<Quote>>> getQuotes( @PathVariable String type, @PathVariable String id,
                                                              @RequestHeader( "DN" ) String dn,
                                                              @RequestHeader( "WId" ) String wid ) {
         WidAux.initWid( wid );
         logger.debug( "quote called" );
         HttpStatus hs = HttpStatus.NOT_FOUND;
         List<Specifier<Quote>> res = null;
         if ( taskFlowProvider.exists( type ) ) {
             TaskFlowFactory tff = taskFlowProvider.getFactoryForTaskFlow( type );
             TaskFlow tf = tff.find( id );
             if ( tf != null ) {
                 try {
                     List<Quote> quoteList = TaskFlowServiceAux.createQuotes( tff, tf );
 
                     res = new ArrayList<Specifier<Quote>>( quoteList.size() );
                     HashMap<String, String> urimap = new HashMap<String, String>( 3 );
                     urimap.put( "service", "gorfx" );
                     urimap.put( UriFactory.TASKFLOW_ID, id );
                     urimap.put( "type", type );
                     for ( int i = 0; i < quoteList.size(); ++i ) {
                         urimap.put( "idx", String.valueOf( i ) );
                         Specifier<Quote> sq = new Specifier<Quote>();
                         sq.setUriMap( Collections.unmodifiableMap( urimap ) );
                         sq.setUrl( uriFactory.quoteUri( urimap ) );
                         sq.setPayload( quoteList.get( i ) );
                         res.add( sq );
                     }
                     hs = HttpStatus.OK;
                 } catch ( UnsatisfiableOrderException e ) {
                     DelegatingOrder o = tf.getOrder();
                     logger.debug( "Unsatisfiable order: " + o.getLoggableDescription() );
                     tf.setUnfulfillableOrder( true );
                     hs = HttpStatus.BAD_REQUEST;
                 } catch ( Exception e ) {
                     logger.warn( "Exception on order calculation for " + tf.getOrder()
                             .getLoggableDescription(), e );
                    throw new RuntimeException( e );
                 }
             }
         }
 
         WidAux.removeWid();
         return new ResponseEntity<List<Specifier<Quote>>>( res, getHeader( type, id, "quote", dn, wid ), hs );
     }
 
 
 
 
     @RequestMapping( value = "/_{type}/_{id}/quote", method = RequestMethod.POST )
     @Secured( "ROLE_USER" )
     public ResponseEntity<Integer> setQuote( @PathVariable String type, @PathVariable String id,
                                              @RequestBody Quote cont,
                                              @RequestHeader( "DN" ) String dn,
                                              @RequestHeader( "WId" ) String wid ) {
 
         HttpStatus hs = HttpStatus.NOT_FOUND;
         try {
             TaskFlow tf = findTF( type, id );
             tf.setPreferredQuote( cont );
             hs = HttpStatus.OK;
         } catch ( NoSuchResourceException e ) {
 
         }
 
         return new ResponseEntity<Integer>( 37, getHeader( type, id, "quote", dn, wid ), hs );
     }
 
 
     @RequestMapping( value = "/_{type}/_{id}/quote/_{idx}", method = RequestMethod.GET )
     @Secured( "ROLE_USER" )
     public ResponseEntity<Quote> getQuote( @PathVariable String type, @PathVariable String id, @PathVariable int idx,
                                            @RequestHeader( "DN" ) String dn, @RequestHeader( "WId" ) String wid ) {
 
         HttpStatus hs = HttpStatus.NOT_FOUND;
         Quote quote = null;
         try {
             TaskFlow tf = findTF( type, id );
             List<Quote> quoteList = tf.getQuotes();
             if ( quoteList != null && idx >= 0 && idx < quoteList.size() ) {
                 quote = quoteList.get( idx );
                 hs = HttpStatus.OK;
             }
         } catch ( NoSuchResourceException e ) {
             // intentionally
         }
 
         return new ResponseEntity<Quote>( quote, getHeader( type, id, "quote", dn, wid ), hs );
     }
 
 
     @RequestMapping( value = "/_{type}/_{id}/quote/_{idx}", method = RequestMethod.DELETE )
     @Secured( "ROLE_USER" )
     public ResponseEntity< Integer > deleteQuotes( @PathVariable String type, @PathVariable String id,
                                               @PathVariable int idx, @RequestHeader( "DN" ) String dn,
                                               @RequestHeader( "WId" ) String wid ) {
 
         throw new NotYetImplementedException(); // together with the ProperlyNeverWillBeException
     }
 
 
     @RequestMapping( value = "/_{type}/_{id}/task", method = RequestMethod.GET )
     @Secured( "ROLE_USER" )
     public ResponseEntity<Specifier<Facets>> getTask( @PathVariable String type, @PathVariable String id,
                                                       @RequestHeader( "DN" ) String dn, @RequestHeader( "WId" ) String wid ) {
 
         HttpStatus hs = HttpStatus.NOT_FOUND;
         Specifier<Facets> spec = null;
         WidAux.initWid( wid );
         try {
             TaskFlow tf = findTF( type, id );
             Taskling t = tf.getTaskling();
             if ( t != null ) {
                 logger.debug( "getTask task called" );
                 spec = TaskClient.TaskServiceAux
                         .getTaskSpecifier( taskClient, t.getId(), uriFactory,
                                 taskFlowUriMap( type, id ), dn );
                 hs = HttpStatus.OK;
             }
         } catch ( Exception e ) {
             logger.warn( "Exception while getting task", e );
         }
 
         logger.debug( "returning with " + hs.name() );
         WidAux.removeWid();
         return new ResponseEntity<Specifier<Facets>>( spec, getHeader( type, id, "task", dn, wid ), hs );
     }
 
 
     @RequestMapping( value = "/_{type}/_{id}/task", method = RequestMethod.PUT )
     @Secured( "ROLE_USER" )
     public ResponseEntity<Specifier<Facets>> createTask( @PathVariable String type, @PathVariable String id,
                                                          @RequestParam( value = "quote",
                                                                  required = false ) Integer quoteId,
                                                          @RequestHeader( "DN" ) String dn, @RequestHeader( "WId" ) String wid ) {
 
         HttpStatus hs = HttpStatus.NOT_FOUND;
 
         WidAux.initWid( wid );
         logger.debug( "create task called" );
         HttpHeaders headers = getHeader( type, id, "task", dn, wid );
         if ( taskFlowProvider.exists( type ) ) {
             TaskFlowFactory tff = taskFlowProvider.getFactoryForTaskFlow( type );
             TaskFlow tf = tff.find( id );
             if ( tf != null ) {
                 if ( tf.getTaskling() != null )
                     hs = HttpStatus.CONFLICT;
                 else {
                     tf.getOrder().setSecurityContextHolder( new SpringSecurityContextHolder(
                             SecurityContextHolder.getContext() ) );
                     TaskAction ta = tff.createAction();
                     Taskling taskling;
                     if ( tf.hasQuotes() ) {
                         logger.debug( "submitting using quote: " + quoteId);
                         taskling = submitTaskActionWithQuotes( quoteId, tf, ta, wid );
                     } else
                         taskling = executorService.submitTaskAction( dao, ta, tf.getOrder(), wid );
                     hs = HttpStatus.CREATED;
                     Specifier<Facets> spec = null;
                     try {
                         spec = TaskClient.TaskServiceAux
                                 .getTaskSpecifier( taskClient, taskling.getId(),
                                         uriFactory, taskFlowUriMap( type, id ), dn );
                     } catch ( Exception e ) {
                         logger.warn( "Exception while getting task", e );
                     }
 
                     return new ResponseEntity<Specifier<Facets>>( spec, headers, hs );
                 }
             }
         }
         logger.debug( "Problem, returning " + hs.name() );
         WidAux.removeWid();
         return new ResponseEntity<Specifier<Facets>>( null, headers, hs );
     }
 
 
     private Taskling submitTaskActionWithQuotes( final Integer quoteId, final TaskFlow tf,
                                                  final TaskAction ta, final String wid )
     {
 
         Taskling taskling;List<Quote> quotes = tf.getQuotes();
         Quote quote = quoteId != null && quoteId >= 0 && quoteId < quotes.size()  
                   ? quotes.get( quoteId ) : quotes.get( 0 );
         taskling = executorService.submitTaskAction( dao, ta, 
                 new TaskBuilder().setOrder( tf.getOrder() )
                 .setContract( PersistentContract.acceptQuoteNow( quote ) )
                 .setPermissionInfo( new PermissionInfo( "default",
                         "PermissionConfiglet" ) )
                 
                 , wid );
         return taskling;
     }
 
 
     @RequestMapping( value = "/_{type}/_{id}/status", method = RequestMethod.GET )
     @Secured( "ROLE_USER" )
     public ResponseEntity<TaskFlowStatus> getStatus( @PathVariable String type, @PathVariable String id,
                                                      @RequestHeader( "DN" ) String dn,
                                                      @RequestHeader( "WId" ) String wid ) {
         HttpStatus hs = HttpStatus.NOT_FOUND;
         TaskFlowStatus tfs = null;
         try {
             TaskFlow tf = findTF( type, id );
             tfs = TaskFlowAux.statusFromTaskFlow( dao, tf );
             Taskling t = tf.getTaskling();
             if ( t != null ) {
                 // maybe call task client
                 Specifier<Void> spec = new Specifier<Void>();
                 Map<String, String> urimap = taskUriMap( type, id, t );
                 spec.setUrl( uriFactory.taskUri( urimap, "status" ) );
                 spec.setUriMap( urimap );
                 tfs.setTaskSpecifier( spec );
 
                 hs = HttpStatus.OK;
             }
         } catch ( NoSuchResourceException e ) {
             // intentionally
         }
         return new ResponseEntity<TaskFlowStatus>( tfs, getHeader( type, id, "status", dn, wid ), hs );
     }
 
 
     @RequestMapping( value = "/_{type}/_{id}/result", method = RequestMethod.GET )
     @Secured( "ROLE_USER" )
     public ResponseEntity<Specifier<TaskResult>> getResult( @PathVariable String type, @PathVariable String id,
                                                             @RequestHeader( "DN" ) String dn,
                                                             @RequestHeader( "WId" ) String wid ) {
 
         HttpStatus hs = HttpStatus.NOT_FOUND;
         Specifier<TaskResult> spec = null;
         try {
             spec = createTaskSpecifier( TaskResult.class, type, id, "result" );
 
             ResponseEntity<TaskResult> res = taskClient.getResult( spec.getUriMap().get( "taskId" ), dn, wid );
             if ( HttpStatus.OK.equals( res.getStatusCode() ) ) {
                 spec.setPayload( res.getBody() );
                 hs = HttpStatus.OK;
             } else spec = null;
         } catch ( NoSuchResourceException e ) {
             // intentionally
         }
         return new ResponseEntity<Specifier<TaskResult>>( spec, getHeader( type, id, "result", dn, wid ), hs );
     }
 
 
     @RequestMapping( value = "/_{type}/_{id}/errors", method = RequestMethod.GET )
     @Secured( "ROLE_USER" )
     public ResponseEntity<TaskFlowFailure> getErrors( @PathVariable String type,
                                                       @PathVariable String id,
                                                       @RequestHeader( "DN" ) String dn,
                                                       @RequestHeader( "WId" ) String wid ) {
 
         HttpStatus hs = HttpStatus.NOT_FOUND;
         TaskFlowFailureImpl taskFlowFailure = new TaskFlowFailureImpl();
         try {
             Specifier<TaskFailure> spec = createTaskSpecifier( TaskFailure.class, type, id, "errors" );
             ResponseEntity<TaskFailure> res = taskClient.getErrors( spec.getUriMap().get( "taskId" ), dn, wid );
             if ( res.getStatusCode() == HttpStatus.OK ) {
                 spec.setPayload( res.getBody() );
                 hs = HttpStatus.OK;
             }
         } catch ( NoSuchResourceException e ) {
             // intentionally
         }
         return new ResponseEntity<TaskFlowFailure>( taskFlowFailure,
                 getHeader( type, id, "result", dn, wid ), hs );
     }
 
 
     protected GNDMSResponseHeader getHeader( String type, String id, String facet, String dn, String wid ) {
 
         Map<String, String> uriargs = taskFlowUriMap( type, id );
         return new GNDMSResponseHeader( uriFactory.taskFlowTypeUri( uriargs, facet ), facet, baseUrl, dn, wid );
     }
 
 
     private Map<String, String> taskFlowUriMap( String type, String id ) {
 
         HashMap<String, String> urimap = new HashMap<String, String>( 3 );
         urimap.put( UriFactory.SERVICE, "gorfx" );
         urimap.put( UriFactory.TASKFLOW_TYPE, type );
         urimap.put( UriFactory.TASKFLOW_ID, id );
         return urimap;
     }
 
 
     private Map<String, String> taskUriMap( String type, String id, Taskling t ) {
         return TaskClient.TaskServiceAux.taskUriMap( t.getId(), taskFlowUriMap( type, id ) );
     }
 
 
     private <T> Specifier<T> createTaskSpecifier( Class<T> resClass, String type, String id, String facet ) throws NoSuchResourceException {
 
         final TaskFlow tf = findTF( type, id );
         final Taskling t = tf.getTaskling();
         final Map<String, String> urimap = taskUriMap( type, id, t );
         final Specifier<T> spec;
         if ( t != null ) {
             spec = new Specifier<T>();
             spec.setUrl( uriFactory.taskUri( urimap, facet ) );
             spec.setUriMap( urimap );
         } else
             throw new NoSuchResourceException( "task", urimap );
 
         return spec;
     }
 
 
     protected TaskFlow findTF( String type, String id ) throws NoSuchResourceException {
 
         if ( taskFlowProvider.exists( type ) ) {
             TaskFlowFactory tff = taskFlowProvider.getFactoryForTaskFlow( type );
             TaskFlow tf = tff.find( id );
             if ( tf != null )
                 return tf;
             else
                 return TaskFlowServiceAux.fromTask( dao, taskFlowProvider, type, id );
         }
 
         throw new NoSuchResourceException();
     }
 
 
     public Dao getDao() {
         return dao;
     }
 
 
     @Inject
     public void setDao( Dao dao ) {
         this.dao = dao;
     }
 
 
     public TaskExecutionService getExecutorService() {
         return executorService;
     }
 
 
     @Inject
     public void setExecutorService( TaskExecutionService executorService ) {
         this.executorService = executorService;
     }
 
 
     public void setBaseUrl(String baseUrl) {
         this.baseUrl = baseUrl;
     }
 
 
     public void setLocalBaseUrl( String localBaseUrl ) {
         this.localBaseUrl = localBaseUrl;
     }
 
     @Inject
     public void setTaskFlowProvider( TaskFlowProvider taskFlowProvider ) {
         this.taskFlowProvider = taskFlowProvider;
     }
 
 
     @Inject
     public void setTaskClient( TaskClient taskClient ) {
         this.taskClient = taskClient;
     }
 }
