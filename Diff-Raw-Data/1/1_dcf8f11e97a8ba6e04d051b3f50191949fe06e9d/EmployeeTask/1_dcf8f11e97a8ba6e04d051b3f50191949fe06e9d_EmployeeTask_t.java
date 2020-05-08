 /**
  * 
  */
 package org.promasi.game.company;
 
 import java.util.List;
 import java.util.Map;
 
 import org.promasi.game.GameException;
 import org.promasi.game.model.generated.EmployeeTaskModel;
 import org.promasi.game.project.ProjectTask;
 import org.promasi.utilities.exceptions.NullArgumentException;
 import org.promasi.utilities.serialization.SerializationException;
 
 /**
  * @author m1cRo
  * Represent the employee task on ProMaSi system.
  * An employee task actually is related with and instance of
  * the {@link ProjectTask} and each employee has a list of {@link EmployeeTask}
  * assigned to him.
  */
 public class EmployeeTask
 {
 	/**
 	 * 
 	 */
 	protected ProjectTask _projectTask;
 	
 	/**
 	 * 
 	 */
 	protected int _firstStep;
 	
 	/**
 	 * 
 	 */
 	protected int _lastStep;
 	
 	/**
 	 * 
 	 */
 	protected List<String> _dependencies;
 	
 	/**
 	 * 
 	 */
 	protected String _taskName;
 	
 	/**
 	 * 
 	 * @param projectTask
 	 * @param startDate
 	 * @param endDate
 	 * @throws NullArgumentException
 	 * @throws IllegalArgumentException
 	 */
 	public EmployeeTask(String taskName, List<String> dependencies, ProjectTask projectTask,final int firstStep, final int lastStep)throws GameException
 	{
 		if(projectTask==null)
 		{
 			throw new GameException("Wrong argument projectTask==null");
 		}
 		
 		if(firstStep<0)
 		{
 			throw new GameException("Wrong argument startStep < 0");
 		}
 		
 		if(lastStep<=firstStep)
 		{
 			throw new GameException("Wrong argument endDate<=startStep");
 		}
 		
 		if( taskName == null ){
 			throw new GameException("Wrong argument taskName == null");
 		}
 		
 		if(dependencies == null){
 			throw new GameException("Wrong argument dependencies == null");
 		}
 		
 		_firstStep=firstStep;
 		_lastStep=lastStep;
 		_projectTask=projectTask;
 		_taskName = taskName;
 		_dependencies = dependencies;
 	}
 	
 	/**
 	 * 
 	 * @return
 	 */
 	public String getTaskName(){
 		return _taskName;
 	}
 	
 	/**
 	 * 
 	 * @return
 	 * @throws SerializationException
 	 */
 	public EmployeeTaskModel getMemento(){
 		EmployeeTaskModel result = new EmployeeTaskModel();
 		
 		result.setFirstStep(_firstStep);
 		result.setLastStep(_lastStep);
 		result.setProjectTaskName(_taskName);
 		result.setTaskName(_taskName);
		result.getDependencies().addAll(_dependencies);
 		
 		return result;
 	}
 	
 	/**
 	 * 
 	 * @param employeeSkills
 	 * @param currentStep
 	 * @return
 	 */
 	public boolean executeTask(Map<String, Double> employeeSkills, int currentStep)
 	{
 		if(currentStep<_firstStep || currentStep>_lastStep){
 			return false;
 		}
 		
 		return _projectTask.applyEmployeeSkills(employeeSkills);
 	}
 	
 	/**
 	 * Will check if the current task has any 
 	 * time line conflict with the given instance of {@link EmployeeTask}
 	 * @param task Tack to check the conflict with.
 	 * @return true if any conflict was found, false otherwise.
 	 */
 	public boolean conflictsWithTask( EmployeeTask task){
 		boolean result = true;
 		
 		if( task != null && !task.getTaskName().equals(_taskName) ){
 			if( _firstStep > task._lastStep ){
 				result = false;
 			}else if( _lastStep < task._firstStep ){
 				result = false;
 			}else if( task._dependencies.contains(_taskName) || _dependencies.contains(task.getTaskName()) ){
 				result = false;
 			}
 		}
 
 		return result;
 	}
 	
 	/**
 	 * 
 	 * @param task
 	 * @return
 	 */
 	public boolean hasDependencie( EmployeeTask task ){
 		boolean result = false;
 		
 		if( task != null )
 		{
 			result = _dependencies.contains(task._taskName);
 		}
 		
 		return result;
 	}
 	
 	/**
 	 * 
 	 * @param dependencieTask
 	 * @return
 	 */
 	public boolean applyDependencie(EmployeeTask dependencieTask)
 	{
 		boolean result = false;
 		
 		if( dependencieTask != null ){
 			if( _dependencies.contains(dependencieTask.getTaskName()) ){
 				if( dependencieTask._lastStep > _firstStep){
 					int duration = _lastStep - _firstStep;
 					_firstStep = dependencieTask._lastStep;
 					_lastStep = _firstStep + duration;
 				}
 				
 				result = true;
 			}
 		}
 
 		return result;
 	}
 	
 	/**
 	 * 
 	 * @param currentStep
 	 * @return
 	 * @throws IllegalArgumentException
 	 */
 	public boolean isValid(int currentStep){
 		if(currentStep<0){
 			return true;
 		}
 		
 		if(currentStep>_lastStep){
 			return false;
 		}
 		
 		return _projectTask.isValidTask();
 	}
 	
 	/**
 	 * Will remove the given task name from
 	 * the tasks dependencies.
 	 * @param dependencie
 	 * @return true if succeed, false otherwise.
 	 */
 	public boolean removeDependencie( String dependencie ){
 		boolean result = false;
 		
 		if( dependencie != null && _dependencies.contains(dependencie) ){
 			result = _dependencies.remove(dependencie);
 		}
 		
 		return result;
 	}
 	
 	/**
 	 * 
 	 * @return
 	 */
 	public int getFirstStep(){
 		return _firstStep;
 	}
 
 }
