 package de.zib.gndms.logic.model;
 
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
 
 

 import de.zib.gndms.kit.configlet.ConfigletProvider;
 import de.zib.gndms.kit.security.CredentialProvider;
 import de.zib.gndms.kit.security.RequiresCredentialProvider;
 import de.zib.gndms.kit.util.WidAux;
 import de.zib.gndms.logic.model.gorfx.LifetimeExceededException;
 import de.zib.gndms.logic.model.gorfx.permissions.PermissionConfiglet;
 import de.zib.gndms.model.common.types.FilePermissions;
import de.zib.gndms.model.gorfx.types.DelegatingOrder;
 import de.zib.gndms.model.gorfx.types.TaskState;
 import de.zib.gndms.neomodel.common.Dao;
 import de.zib.gndms.neomodel.common.Session;
 import de.zib.gndms.neomodel.gorfx.Task;
 import de.zib.gndms.neomodel.gorfx.TaskAccessor;
 import de.zib.gndms.neomodel.gorfx.Taskling;
 import org.jetbrains.annotations.NotNull;
 import org.jetbrains.annotations.Nullable;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import javax.persistence.EntityManager;
 import javax.persistence.EntityManagerFactory;
 import java.io.Serializable;
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 
 
 /**
  * A TaskAction is used to execute a model of type {@link Taskling} with an underlying model of {@link de.zib.gndms.neomodel.gorfx.Task}
  *
  *
  * @author  try ste fan pla nti kow zib
  *
  * User: stepn Date: 15.09.2008 Time: 11:26:48
  *
  * @Parameter O ist the Order class
  */
 @SuppressWarnings({ "AbstractMethodCallInConstructor" })
 public abstract class TaskAction<O extends Serializable> extends
         AbstractModelDaoAction<Taskling, Taskling> implements RequiresCredentialProvider
 {
     /**
      * The ExecutionService on which this TaskAction runs
      */
     private TaskExecutionService service;
 
     /**
      * Used for logging during task execution
      */
     protected final Logger logger = LoggerFactory.getLogger( this.getClass() );
 
 
     /***
      * Workflow id
      */
     private String wid;
 
     /**
      * An EntityManagerFactory for task actions that need it
      */
 	private EntityManagerFactory emf;
 
     /**
      * Unserialized version of the order
      */
     private O order;
 
     /**
      * Class of the order, required for casting.
      */
     private Class<O> orderClass;
 
     private ConfigletProvider configletProvider;
 
     private CredentialProvider credentialProvider;
     protected volatile ModelUpdateListener<Taskling> modelUpdateListener = null;
 
 
     public TaskAction() {
         super();
     }
 
 
     protected TaskAction( final Class<O> orderClass ) {
 
         this.orderClass = orderClass;
     }
 
 
     /**
      * Initializes a TaskAction from an EntityManager, a dao and a (shallow) model.
      *
      * The model is made persistent by the EntityManager.
      * The EntityManager and the model are stored, using {@code setOwnEntityManager()} and {@code setModelAndBackup()}.
      * A Backup of the model is done.
      *
      *
      * @param em an EntityManager, storing AbstractTasks
      * @param dao the dao for storing task nodes in the graph database
      * @param model an AbstractTask to be stored as model of {@code this} and to be stored in the database
      */
     public TaskAction(final @NotNull EntityManager em, final @NotNull Dao dao, final @NotNull Taskling model) {
         super();
         setOwnDao(dao);
         setOwnEntityManager(em);
         setModel(model);
     }
 
 
     /***
      * Sets the task's workflow id from WidAux before calling {@link #execute()} via {@code super.call()}
      * 
      * @return model after being finished
      * @throws RuntimeException
      */
     @Override
     public Taskling call() throws RuntimeException {
         final Taskling result;
         Thread.currentThread().setName( "taskaction" );
         try {
             final Session session = getDao().beginSession();
             try {
                 final Taskling ling = getModel();
                 final Task task = ling.getTask(session);
                 WidAux.initWid(task.getWID());
                 if( task.getOrder( ) != null )
                    WidAux.initGORFXid( ( (DelegatingOrder ) task.getOrder( )).getActId() );
                 session.success();
             }
             finally { session.finish(); }
             result = super.call();
         } catch ( RuntimeException e ) {
             logger.debug( "", e );
             throw e;
         }
         finally {
             WidAux.removeGORFXid();
             WidAux.removeWid();
         }
         return result;
     }
 
     /**
      * Invokes {@link #onTransit(String,TaskState,boolean,boolean)} in a loop which is expected to change the task state.
      * This goes on until either the state FINISHED or FAILED is reached.
      *
      * All Tasks become FAILED when an unhandled exception is caught.
      * 
      * @param em the EntityManager being executed on its persistence context.
      * @return the model after the last round of execution
      */
     @SuppressWarnings({ "ThrowableInstanceNeverThrown", "ObjectAllocationInLoop" })
     @Override
     public Taskling execute(final @NotNull EntityManager em) {
         // Cross-Loop state
         String thisWID = wid == null? "(null)" : wid;
         boolean inFirstStep   = true;
         boolean afterLastStep = false;
 
         while ( ! afterLastStep ) {
             // In-Loop state
             Session session;
             boolean isRestartedTask;
             TaskState state;
             boolean altTaskState;
             String descr;
         
             // Capture state snapshot from Task and prep in-loop state
             session = getDao().beginSession();
             try {
                 final Task task = getTask(session);
                 state           = task.getAltTaskState();
                 altTaskState    = state != null;
                 if (! altTaskState)
                     state = task.getTaskState();
 
                 descr              = task.getDescription();
                 isRestartedTask    = ! TaskState.CREATED.equals(state);
                 if (state.isRestartedState()) {
                     // canonize state if it isn't
                     state = state.getCanonicalState();
                     task.setTaskState(state);
                 }
                 else
                     isRestartedTask &= inFirstStep;
 
                 afterLastStep = task.isDone();
                 session.success();
             }
             finally { session.finish(); }
 
 
             if ( inFirstStep )
                 trace("execute() with " + descr, null);
 
             if (! afterLastStep ) {
                 trace("transit(" + state.name() + ")", null);
                 try {
                     if (state.isDoneState()) {
                         try {
                             onTransit(thisWID, state, isRestartedTask, altTaskState);
                         }
                         finally {
                             session = getDao().beginSession();
                             try {
                                 getModel().getTask(session).setDone(true);
                                 session.success();
                             }
                             finally { session.finish(); }
                         }
                     }
                     else {
                         checkTimeout(getModel());
                         onTransit(thisWID, state, isRestartedTask, altTaskState);
                     }
                 }
                 catch (Exception e) {
                     // Oh yes, this is possible
                     if (e == null)
                         fail(new NullPointerException());
                     else
                         fail(e);
                 }
             }
 
             if (altTaskState) {
                 // todo handle altTaskState
             }
             inFirstStep = false;
         }
 
         trace("return getModel()", null);
         return getModel();
     }
 
     protected @NotNull TaskAccessor getTaskSnapshot(@NotNull Dao dao) {
         final Session session = dao.beginSession();
         try {
             final TaskAccessor snapshot = getTask(session).getSnapshot();
             session.success();
             return snapshot;
         }
         finally { session.finish(); }
     }
 
     protected @NotNull Task getTask(@NotNull Session session) {
         Taskling model = getModel();
         if (model == null)
             throw new NullPointerException("Task action without model (Taskling)");
         else
             return model.getTask(session);
     }
 
 
     @Override
     protected final boolean isExecutingInsideTransaction() {
         return false;
     }
 
 
     protected void onTransit(@NotNull String wid,
                              @NotNull TaskState state, boolean isRestartedTask, boolean altTaskState) throws Exception {
         throw new UnsupportedOperationException("not yet implemented");
     }
 
 
     protected void _transitWithPayload( @NotNull TaskState taskState, @Nullable Serializable payload, boolean hasPayload ) {
         final @NotNull Session session = getDao().beginSession();
         try {
             final Task task = session.findTask(getModel().getId());
             _doTransit( taskState, payload, hasPayload, task );
             session.success();
         }
         finally { session.finish(); }
     }
 
 
     protected void _doTransit( TaskState taskState, Serializable payload, boolean hasPayload, Task task ) {
 
         logger.debug( "transit to: " + taskState.toString()  );
         task.setTaskState(task.getTaskState().transit(taskState));
         if( hasPayload )
             task.setPayload(payload);
         if (TaskState.FINISHED.equals(taskState))
             task.setProgress(task.getMaxProgress());
     }
 
 
     protected void transitWithPayload( Serializable payload, @NotNull TaskState taskState) {
         _transitWithPayload( taskState, payload, true );
     }
 
 
     protected void transit(@NotNull TaskState taskState) {
         _transitWithPayload( taskState, null, false );
     }
 
 
     protected void autoTransitWithPayload(Serializable payload ) {
         _autoTransitWithPayload( payload, true );
     }
 
 
     protected void autoTransit() {
         _autoTransitWithPayload( null, false );
     }
 
 
     protected void _autoTransitWithPayload(Serializable payload, boolean hasPayload ) {
         final @NotNull Session session = getDao().beginSession();
         try {
             final Task task = getTask(session);
             final TaskState taskState = task.getTaskState().getCanonicalState();
             final TaskState nextState;
             switch(taskState) {
                 case CREATED: nextState = TaskState.INITIALIZED; break;
                 case INITIALIZED: nextState = TaskState.IN_PROGRESS; break;
                 case IN_PROGRESS: nextState = TaskState.FINISHED; break;
                 case FINISHED: nextState = TaskState.FINISHED; break;
                 default: nextState = TaskState.FAILED; break;
             }
             _doTransit( nextState, payload, hasPayload, task );
             session.success();
         }
         finally { session.finish(); }
     }
 
 
     protected void _failWithPayload( boolean hasPayload, Serializable payload, @NotNull Exception... exceptions) {
         
         trace( "latest exception", exceptions[0] );
         final Session session = getDao().beginSession();
         try {
             final Task task = getTask(session);
             task.setTaskState(TaskState.FAILED);
             if( hasPayload )
                 task.setPayload(payload);
             for (Exception e: exceptions)
                 task.addCause(e);
             session.success();
         }
         finally { session.finish(); }
     }
 
     protected void failWithPayload(Serializable payload, @NotNull Exception... exceptions) {
         _failWithPayload( true, payload, exceptions );
     }
 
     protected void fail(@NotNull Exception... exceptions) {
         _failWithPayload( false, null, exceptions );
     }
 
     protected void removeAltTaskState() {
         getDao().removeAltTaskState(getModel().getId());
     }
 
 
     @SuppressWarnings({ "HardcodedFileSeparator" })
     protected void trace(final @NotNull String userMsg, final Throwable cause) {
         final Taskling model = getModel();
         final String msg;
         if (model == null)
             msg = userMsg;
         else {
             Session session = getDao().beginSession();
             try {
                 final Task task = model.getTask(session);
                 final TaskState state = task.getTaskState();
                 final String descr = task.getDescription();
                 msg = "TA of AbstractTask " + model.getId()
                         + ('/' + state.toString()) + ':'
                         + (userMsg.length() > 0 ? ' ' : "") + userMsg
                         + (" DESCR: '" + descr + '\'');
                 session.finish();
             }
             finally { session.success(); }
         }
        if (cause == null)
            logger.trace(msg);
         else
            logger.trace(msg, cause);
 
     }
 
 
     @Override
     public Taskling getModel() {
         return super.getModel();
     }
 
 
     @Override
     public void setModel(final @NotNull Taskling ling) {
 
         if(! hasDao( ) )
             throw new IllegalStateException( "no dao provided" );
 
         final Session session = getDao().beginSession();
         final String newWID;
         try {
             newWID = ling.getTask(session).getWID();
             session.success();
         }
         finally { session.finish();}
         super.setModel(ling);    // Overridden method
         wid = newWID;
     }
 
 
     private boolean hasDao() {
         return getDao() != null;
     }
 
 
     /**
      * Inform all listeners about the current model.
      */
     protected void refreshTaskResource() {
         getModelUpdateListener().onModelChange(getModel());
     }
 
 
     /**
      * Returns the TaskExecutionService corresponding to this TaskAction or a parent TaskAction.
      *
      * @return the TaskExecutionService corresponding to this TaskAction or a parent TaskAction.
      */
     public TaskExecutionService getService() {
         if (service == null) {
             final TaskAction taskAction = nextParentOfType(TaskAction.class);
             return taskAction == null ? null : taskAction.getService();
         }
         return service;
     }
 
 
     public void setService(final @NotNull TaskExecutionService serviceParam) {
         if (service == null)
             service = serviceParam;
         else
            throw new IllegalStateException("Can't overwrite service");
     }
 
 
     /**
      * Stopps this action if the associated task lifetime is already exceeded.
      *
      * If the lifetime is exceeded, this sets the TaskState
      * to {@code FAILURE} by throwing an exception.
      *
      * @param ling shallow model to be checked
      * @throws LifetimeExceededException
      */
     private void checkTimeout( @NotNull Taskling ling ) {
 
         Session session = getDao().beginSession();
         try {
             Task model = ling.getTask(session);
 
             // check the task lifetime
             Calendar terminationTime = model.getTerminationTime();
 
             // todo provoke exception and check infinite looping
             if( terminationTime != null
                 && new GregorianCalendar().compareTo( terminationTime ) >= 1 )
             {
                 logger.debug( "Task lifetime exceeded" );
                 throw new LifetimeExceededException();
 //                boolean contained = false;
 //                try {
 //                    // check if model is still there
 //                    contained = em.contains( model );
 //                    if( containt ) {
 //                        model.fail( new RuntimeException( "Task lifetime exceeded" ) );
 //                        getLogger().debug(  "Try to persist task" );
 //                    }
 //                    tx.commit( );
 //                } catch ( Exception e ) {
 //                    // exception here  doesn't really matter
 //                    // task is doomed anyway
 //                    e.printStackTrace(  );
 //                } finally {
 //                    tx.finish();
 //                }
 //                // interrupt this thread
 //                getLogger().debug(  "Stopping task thread" );
 //                //Thread.currentThread().interrupt();
 //                if( containt ) refreshTaskResource();
 //                fail( new RuntimeException( "Task lifetime exceeded" ) );
             }
             session.success();
         }
         finally { session.finish(); }
     }
 
 
 	@Override
 	public EntityManager getEntityManager() {
 		return getOwnEntityManager();
 	}
 
 
 	public EntityManagerFactory getEmf() {
 		return emf;
 	}
 
 
     /**
      * Delivers the logger of this class.
      *
      * This is just for compatibility reasons.
      *
      * @return The logger.
      */
     protected Logger getLogger() {
         return logger;
     }
 
 	public void setEmf(final @NotNull EntityManagerFactory emfParam) {
 		emf = emfParam;
 	}
 
     public ConfigletProvider getConfigletProvider() {
         return configletProvider;
     }
 
 
     public void setConfigletProvider( ConfigletProvider configletProvider ) {
         this.configletProvider = configletProvider;
     }
 
 
 
     public void setCredentialProvider( CredentialProvider cp ) {
         this.credentialProvider = cp;
     }
 
 
     public CredentialProvider getCredentialProvider() {
 
         return this.credentialProvider;
     }
 
 
     /**
      * Extracts the actual permissions from a tasks permission info object
      *
      * @return the actual permissions
      */
     public FilePermissions actualPermissions( ) {
         Session session = getDao().beginSession();
         try {
             Task task = getTask(session);
             FilePermissions filePermissions = null;
             PermissionConfiglet pc = configletProvider.getConfiglet( PermissionConfiglet.class,
                     task.getPermissionInfo().getPermissionConfigletName() );
             if( pc != null ) {
                 filePermissions = pc.permissionsFor(task.getPermissionInfo().getUserName());
             }
 
             session.success();
             return filePermissions;
         }
         finally { session.finish(); }
 
 
     }
 
 
     public @NotNull ModelUpdateListener<Taskling> getModelUpdateListener() {
         return modelUpdateListener;
     }
 
     protected void setModelUpdateListener(@NotNull ModelUpdateListener<Taskling> listener) {
         modelUpdateListener = listener;
     }
 
 
     protected void updateMaxProgress( final int max ) {
         Session session = getDao().beginSession();
         try{
             getModel().getTask( session ).setMaxProgress( max );
             session.success();
         }finally {
             session.finish();
         }
     }
 
 
     protected void updateProgress( final int progress ) {
         Session session = getDao().beginSession();
         try{
             getModel().getTask( session ).setProgress( progress );
             session.success();
         }finally {
             session.finish();
         }
     }
 
     // returns cached instance of order
     public O getOrder() {
         return order;
     }
 
 
     protected void setOrder( O order ) {
         this.order = order;
     }
 
 
     /**
      * This should be called in every In_State_ method, cause after a restart order might have
      * not been initialized.
      */
     protected void ensureOrder() {
 
         if ( getOrder() == null ) {
             Session session = getDao().beginSession();
             try {
                 Task task = getTask( session );
                 setOrder( orderClass.cast( task.getOrder() ) );
                 session.success();
             } finally {
                 session.finish();
             }
         }
     }
 
 
     public Class<O> getOrderClass() {
 
         return orderClass;
     }
 
 
     public void setOrderClass( final Class<O> orderClass ) {
 
         this.orderClass = orderClass;
     }
 }
