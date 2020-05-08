 package gov.nih.nci.caintegrator.studyQueryService;
 
 import gov.nih.nci.caintegrator.domain.finding.bean.Finding;
 import gov.nih.nci.caintegrator.dto.query.QueryDTO;
 import gov.nih.nci.caintegrator.enumeration.FindingStatus;
 import gov.nih.nci.caintegrator.exceptions.FindingsQueryException;
 import gov.nih.nci.caintegrator.service.findings.AnalysisFinding;
 import gov.nih.nci.caintegrator.service.findings.strategies.SessionBasedFindingStrategy;
 import gov.nih.nci.caintegrator.service.task.Task;
 import gov.nih.nci.caintegrator.service.task.TaskResult;
 
 import java.util.Collection;
 import java.util.List;
 
 /**
  * FindingsManagerImpl is a generic implementation
  * of the FindingsManager and provides all methods
  * necessary to control submission and retrieval of
  * queries and results. Aside from generating tasks,
  * most of the responsibilities handed to it are
  * delegated to the appropriate strategy that can handle the 
  * query passed in.
  * @author caIntegrator team
  *
  */
 public class FindingsManagerImpl implements FindingsManager{
     private List<SessionBasedFindingStrategy> strategyList;
     
    
     /**
      * Submit query looks at the list of strategies it has available
      * and chooses the correct strategy based on the queryDTO type.
      * It then creates a new Task to be handed by to the user, while
      * it called the execute method of the strategy asynchronously.
      */
     public Task submitQuery(QueryDTO queryDTO) throws FindingsQueryException {
             SessionBasedFindingStrategy strategy = chooseStrategy(queryDTO);
             Task task = new Task(queryDTO.getQueryName(),queryDTO.toString(),FindingStatus.Running,queryDTO);
             strategy.getTaskResult().setTask(task);
             strategy.getTaskResult().getTask().setQueryDTO(queryDTO);            
             strategy.executeQuery();  
             return task;
     }
     
     /**
      * This method locates the desired Task by calling chooseStrategy
      * in order to use the correct strategy to retrieve the Task and
      * it status.
      * @param task
      * @return Task
       */    
     public Task checkStatus(Task task){        
         SessionBasedFindingStrategy strategy = chooseStrategy(task.getQueryDTO());
         TaskResult taskResult = strategy.retrieveTaskResult(task);
         task = taskResult.getTask();
         if(taskResult instanceof AnalysisFinding) {
             task.setElapsedTime(((AnalysisFinding)taskResult).getElapsedTime());
            task.setStatus(((AnalysisFinding)taskResult).getStatus());
         }
         return task;
     }
 
     public Collection<Finding> getFindings(QueryDTO queryDTO) {
         // TODO Auto-generated method stub
         return null; 
     }
 
     public Collection<Finding> getFindings(Task task) {
         // TODO Auto-generated method stub
         return null;
     }
  
     /**
      * This method provides the correct strategy for the Findings Manager
      * to use, based on the type of queryDTO it is passed. The canHandle method
      * is called on each strategy's query handlers, which implement the
      * method.
      * @param queryDTO
      * @return SessionBasedFindingStrategy
      */
     public SessionBasedFindingStrategy chooseStrategy(QueryDTO queryDTO){       
         for(SessionBasedFindingStrategy s : strategyList){
             if(s.canHandle(queryDTO)){
                 return s;                
             }
         } 
         return null;
     }
 
    /**
     * This method locates the desired TaskResult by calling chooseStrategy
     * in order to use the correct strategy to retrieve the result.
     * @param task
     * @return TaskResult
      */
     public TaskResult getTaskResult(Task task) {
         SessionBasedFindingStrategy strategy = chooseStrategy(task.getQueryDTO());
         TaskResult taskResult = strategy.retrieveTaskResult(task);
         return taskResult;
     }
     
    /**
      * Returns the strategyList. This list is injected by
      * the user on startup. 
      * @return List<SessionBasedFindingStrategy>
      */
     public List<SessionBasedFindingStrategy> getStrategyList() {
         return strategyList;
     }
 
     /**
      * @param strategyList The strategyList to set.
      */
     public void setStrategyList(List<SessionBasedFindingStrategy> strategyList) {
         this.strategyList = strategyList;
     }
 
 }
