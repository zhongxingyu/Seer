 package eu.europeana.uim.gui.cp.client.services;
 
 import java.util.List;
 import java.util.Set;
 
 import com.google.gwt.user.client.rpc.RemoteService;
 import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
 
 import eu.europeana.uim.gui.cp.shared.ExecutionDTO;
 import eu.europeana.uim.gui.cp.shared.ParameterDTO;
 
 /**
  * Definition of the service to retrieve orchestration dependent information from the server.
  * 
  * @author Manuel Bernhardt <bernhardt.manuel@gmail.com>
  * @author Markus Muhr (markus.muhr@kb.nl)
  * @since May 23, 2011
  */
 @RemoteServiceRelativePath("execution")
 public interface ExecutionService extends RemoteService {
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
      * @return created execution
      */
     ExecutionDTO startCollection(String workflow, Long collection, String executionName,
             Set<ParameterDTO> parameters);
 
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
      * @return created execution
      */
     ExecutionDTO startProvider(String workflow, Long provider, String executionName,
             Set<ParameterDTO> parameters);
 
     /**
      * @param id
      * @return null or the queried execution (either be it active or an old stored one)
      */
     ExecutionDTO getExecution(Long id);
 
     /**
      * @return list of current running executions
      */
     List<ExecutionDTO> getActiveExecutions();
 
     /**
      * @return list of completed executions
      */
     List<ExecutionDTO> getPastExecutions();
     
     /**
      * @param executions the list of workflows to be considered. Null means all.
      * @return list of completed executions from a list of specific workflows
      */
    List<ExecutionDTO> getPastExecutions(String[] executions);
 
     /**
      * Pause the given execution (if it is paused or not running, nothing happens and false is
      * returned).
      * 
      * @param execution
      * @return true, if pausing was successfull
      */
     Boolean pauseExecution(Long execution);
 
     /**
      * Resumes the given execution (if it is not paused, nothing happens and false is returned).
      * 
      * @param execution
      * @return true, if resuming was successfull
      */
     Boolean resumeExecution(Long execution);
 
     /**
      * Cancels the given execution (if it is not running,, nothing happens and false is returned).
      * 
      * @param execution
      * @return true, if cancellation was successfull
      */
     Boolean cancelExecution(Long execution);
 }
