 package eu.europeana.uim.gui.cp.client.services;
 
 import java.util.List;
 import java.util.Set;
 
 import com.google.gwt.user.client.rpc.AsyncCallback;
 
 import eu.europeana.uim.gui.cp.shared.ExecutionDTO;
 import eu.europeana.uim.gui.cp.shared.ParameterDTO;
 
 /**
  * Definition of the asynchronous service to retrieve orchestration dependent information from the
  * server.
  * 
  * @author Markus Muhr (markus.muhr@kb.nl)
  * @since May 23, 2011
  */
 public interface ExecutionServiceAsync {
     /**
      * Triggers a new execution.
      * 
      * @param workflow
      *            what should be done?
      * @param collection
      *            on what data?
      * @param executionName
      *            name for execution
      * @param parameters
      *            execution specific parameters which override potentially configured ones
      * @param async
      */
     void startCollection(String workflow, Long collection, String executionName,
             Set<ParameterDTO> parameters, AsyncCallback<ExecutionDTO> async);
 
     /**
      * Triggers a new execution.
      * 
      * @param workflow
      *            what should be done?
      * @param provider
      *            on what data?
      * @param executionName
      *            name for execution
      * @param parameters
      *            execution specific parameters which override potentially configured ones
      * @param async
      */
     void startProvider(String workflow, Long provider, String executionName,
             Set<ParameterDTO> parameters, AsyncCallback<ExecutionDTO> async);
 
     /**
      * Returns list of current running executions
      * 
      * @param id
      * @param async
      */
     void getExecution(Long id, AsyncCallback<ExecutionDTO> async);
 
     /**
      * Returns list of current running executions
      * 
      * @param async
      */
     void getActiveExecutions(AsyncCallback<List<ExecutionDTO>> async);
 
     /**
      * Returns list of completed executions
      * 
      * @param async
      */
     void getPastExecutions(AsyncCallback<List<ExecutionDTO>> async);
     
     /**
      * Returns list of completed executions for a list of workflows
      * @param workflows the list of workflows, null means all.
      * @param async
      */
    void getPastExecutions(List<String> workflows,AsyncCallback<List<ExecutionDTO>> async);
 
     /**
      * Pause the given execution (if it is paused or not running, nothing happens and false is
      * returned).
      * 
      * @param execution
      * @param async
      */
     void pauseExecution(Long execution, AsyncCallback<Boolean> async);
 
     /**
      * Resumes the given execution (if it is not paused, nothing happens and false is returned).
      * 
      * @param execution
      * @param async
      */
     void resumeExecution(Long execution, AsyncCallback<Boolean> async);
 
     /**
      * Cancels the given execution (if it is not running,, nothing happens and false is returned).
      * 
      * @param execution
      * @param async
      */
     void cancelExecution(Long execution, AsyncCallback<Boolean> async);
 }
