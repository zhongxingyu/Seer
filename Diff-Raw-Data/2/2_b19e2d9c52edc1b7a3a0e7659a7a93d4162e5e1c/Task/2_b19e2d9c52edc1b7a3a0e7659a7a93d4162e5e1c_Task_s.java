 package eu.europeana.uim.plugin.source;
 
 import java.util.Queue;
 import java.util.Set;
 import java.util.logging.Logger;
 
 import eu.europeana.uim.adapter.UimDatasetAdapter;
 import eu.europeana.uim.orchestration.ExecutionContext;
 import eu.europeana.uim.plugin.ingestion.IngestionPlugin;
 import eu.europeana.uim.plugin.ingestion.IngestionPluginFailedException;
 import eu.europeana.uim.storage.StorageEngineException;
 import eu.europeana.uim.store.Collection;
 import eu.europeana.uim.store.MetaDataRecord;
 import eu.europeana.uim.store.UimDataSet;
 
 /**
  * Generic task to processed by the workflow pipeline. It extends Runnable to provide getter and
  * setter for all kinds of additional information.
  * 
  * @param <U>
  *            generic data set
  * @param <I>
  *            generic identifier
  * 
  * @author Manuel Bernhardt <bernhardt.manuel@gmail.com>
  * @author Markus Muhr (markus.muhr@kb.nl)
  * @since Mar 4, 2011
  */
 public class Task<U extends UimDataSet<I>, I> implements Runnable {
     private static Logger                log       = Logger.getLogger(Task.class.getName());
 
     private TaskStatus                   status    = TaskStatus.NEW;
     private Throwable                    throwable;
 
     private Queue<Task<U, I>>            success   = null;
     private Queue<Task<U, I>>            failure   = null;
 
     private Set<Task<U, I>>              assigned  = null;
 
     private boolean                      savepoint = false;
     private boolean                      mandatory = false;
     private IngestionPlugin<U, I>        step;
 
     private UimDatasetAdapter<U, I>      adapter;
 
     private U                            dataset;
     private final ExecutionContext<U, I> context;
     private boolean                      successfulProcessing;
 
     /**
      * Creates a new instance of this class.
      * 
      * @param dataset
      * @param context
      */
     public Task(U dataset, ExecutionContext<U, I> context) {
         super();
         this.dataset = dataset;
         this.context = context;
     }
 
     @Override
     public void run() {
         U localDataset = dataset;
         if (adapter != null) {
             localDataset = adapter.adapt(localDataset);
         }
        successfulProcessing = step.process(dataset, context);
         if (adapter != null) {
             dataset = adapter.unadapt(localDataset);
         }
     }
 
     /**
      * @return status of the task
      */
     public TaskStatus getStatus() {
         return status;
     }
 
     /**
      * @param status
      *            status of the task
      */
     public void setStatus(TaskStatus status) {
         this.status = status;
     }
 
     /**
      * Initialization of the task.
      */
     public void setUp() {
         // nothing to do right now
     }
 
     /**
      * Task is done or finished and should therefore teared down.
      */
     public void tearDown() {
         // nothing to do right now
     }
 
     /**
      * @return Is it a save point?
      */
     public boolean isSavepoint() {
         return savepoint;
     }
 
     /**
      * @param savepoint
      *            Is it a save point?
      */
     public void setSavepoint(boolean savepoint) {
         this.savepoint = savepoint;
     }
 
     /**
      * Save the content to the storage backend.
      * 
      * @throws StorageEngineException
      */
     @SuppressWarnings({ "unchecked", "rawtypes" })
     public void save() throws StorageEngineException {
         if (dataset instanceof MetaDataRecord) {
             context.getStorageEngine().updateMetaDataRecord((MetaDataRecord)dataset);
         } else if (dataset instanceof Collection) {
             context.getStorageEngine().updateCollection((Collection)dataset);
         } else {
             log.warning("Dataset type '" + dataset.getClass().getSimpleName() +
                         "' is not supported for saving operations!");
         }
     }
 
     /**
      * @return plugin to be executed by this task
      */
     public IngestionPlugin<U, I> getStep() {
         return step;
     }
 
     /**
      * @param step
      *            plugin to be executed by this task
      * @param mandatory
      *            Is this plugin mandatory, so that a unsuccessful processing of the record lead to
      *            a failure or is it optional and not processed records can still be further
      *            processed?
      */
     public void setStep(IngestionPlugin<U, I> step, boolean mandatory) {
         this.step = step;
         this.mandatory = mandatory;
     }
 
     /**
      * @return adapter used to adapt the given data set to the underlying plugin, null if no
      *         adaption is necessary
      */
     public UimDatasetAdapter<U, I> getAdapter() {
         return adapter;
     }
 
     /**
      * @param adapter
      *            adapter used to adapt the given data set to the underlying plugin, null if no
      *            adaption is necessary
      */
     public void setAdapter(UimDatasetAdapter<U, I> adapter) {
         this.adapter = adapter;
     }
 
     /**
      * @param success
      *            queue with successful handled tasks
      */
     public void setOnSuccess(Queue<Task<U, I>> success) {
         this.success = success;
     }
 
     /**
      * @return queue with successful handled tasks
      */
     public Queue<Task<U, I>> getOnSuccess() {
         return success;
     }
 
     /**
      * @param failure
      *            queue with failed tasks (but not fatal ones for the whole workflow)
      */
     public void setOnFailure(Queue<Task<U, I>> failure) {
         this.failure = failure;
     }
 
     /**
      * @return queue with failed tasks (but not fatal ones for the whole workflow)
      */
     public Queue<Task<U, I>> getOnFailure() {
         return failure;
     }
 
     /**
      * @param throwable
      *            contains a thrown exception, if it is of type
      *            {@link IngestionPluginFailedException} the workflow must be teared down as a
      *            plugin is not able to proceed work
      */
     public void setThrowable(Throwable throwable) {
         this.throwable = throwable;
     }
 
     /**
      * @return thrown exception, if it is of type {@link IngestionPluginFailedException} the
      *         workflow must be teared down as a plugin is not able to proceed work
      */
     public Throwable getThrowable() {
         return throwable;
     }
 
     /**
      * @return Is this plugin mandatory, so that a unsuccessful processing of the record lead to a
      *         failure or is it optional and not processed records can still be further processed?
      */
     public boolean isMandatory() {
         return mandatory;
     }
 
     /**
      * @return true, if processing of a {@link MetaDataRecord} was successful, otherwise false
      */
     public boolean isSuccessfulProcessing() {
         return successfulProcessing;
     }
 
     /**
      * @return data set that is processed
      */
     public U getDataset() {
         return dataset;
     }
 
     /**
      * @return execution context
      */
     public ExecutionContext<U, I> getExecutionContext() {
         return context;
     }
 
     /**
      * @return set of assigned tasks
      */
     public Set<Task<U, I>> getAssigned() {
         return assigned;
     }
 
     /**
      * @param assigned
      *            set of assigned tasks
      */
     public void setAssigned(Set<Task<U, I>> assigned) {
         this.assigned = assigned;
     }
 }
