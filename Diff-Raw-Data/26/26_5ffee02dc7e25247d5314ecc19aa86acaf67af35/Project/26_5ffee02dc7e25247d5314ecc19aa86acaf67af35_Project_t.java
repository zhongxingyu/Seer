 package org.promasi.game.project;
 
 
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.TreeMap;
 
 import org.promasi.utilities.exceptions.NullArgumentException;
 import org.promasi.utilities.serialization.SerializationException;
 
/**
 * 
 * @author m1cRo
 * Represent the project on PROMASI
 * simulation model.
 */
 public class Project
 {
 	/**
	 * The main task name.
 	 */
 	public static final String CONST_DEPLOY_TASK_NAME="DEPLOY";
 	
     /**
      * The name of the project.
      */
     protected String _name;
 
     /**
      * The description of the project.
      */
     protected String _description;
 
     /**
      * Project duration in steps
      */
     protected int _projectDuration;
 
 	/**
     * The difficulty level of current project.
      */
     protected double _difficultyLevel;
     
     /**
     * Overall project complete in percents.
      */
     protected double _overallProgress;
     
     /**
     * The current execution step.
      */
     private int _currentStep;
     
     /**
     * Bridges needed to link the
     * different projects tasks.
      */
     protected List<TaskBridge> _taskBridges;
     
     /**
     * Amount received after project accomplished.
      */
     protected double _projectPrice;
     
     /**
      * The tasks for this project.
      */
     protected Map<String, ProjectTask> _projectTasks;
 
     /**
      * Initializes the object.
      */
     public Project(  String name, String description, int projectDuration, Map<String, ProjectTask> projectTasks, double projectPrice, double difficultyLevel)throws NullArgumentException, IllegalArgumentException
     {
     	if(name==null)
     	{
     		throw new NullArgumentException("Wrong argument name==null");
     	}
     	
     	if(description==null)
     	{
     		throw new NullArgumentException("Wrong argument description==null");
     	}
     	
     	if(projectDuration<=0)
     	{
     		throw new NullArgumentException("Wrong argument projectDuration");
     	}
     	
     	if(projectTasks==null)
     	{
     		throw new NullArgumentException("Wrong argument projectTasks==null");
     	}
     	
     	if(difficultyLevel<=0){
     		throw new IllegalArgumentException("Wrong argument difficultyLevel");
     	}
     	
     	if(!projectTasks.containsKey(CONST_DEPLOY_TASK_NAME)){
     		throw new IllegalArgumentException("Wrong argument projectTasks does not contain task named "+CONST_DEPLOY_TASK_NAME);
     	}
     	
     	for(Map.Entry<String, ProjectTask> entry : projectTasks.entrySet()){
     		if ( entry.getKey()==null || entry.getValue()==null ){
     			throw new IllegalArgumentException("Wrong argument projectTasks contains null");
     		}
     	}
     	
     	_overallProgress=0;
     	_projectDuration=projectDuration;
     	_name=name;
     	_description=description;
         _projectTasks = new TreeMap<String, ProjectTask>(projectTasks);
         _projectPrice=projectPrice;
         _difficultyLevel=difficultyLevel;
         _taskBridges=new LinkedList<TaskBridge>();
     }
 
     /**
      * @return The {@link #_name}.
      */
     public String getName ( )
     {
         return _name;
     }
     
     /**
      * 
      * @param outputTaskName
      * @param outputId
      * @param inputTaskName
      * @param inputId
      * @return
      * @throws NullArgumentException
      * @throws IllegalArgumentException
      */
     public boolean makeBridge(final String outputTaskName, final String outputId, String inputTaskName, final String inputId)throws NullArgumentException, IllegalArgumentException{
     	if(outputTaskName==null)
     	{
     		throw new NullArgumentException("Wrong argument outputTaskName==null");
     	}
     	
     	if(inputTaskName==null){
     		throw new NullArgumentException("Wrong argument inputTaskName==null");
     	}
     	
     	if(outputId==null)
     	{
     		throw new NullArgumentException("Wrong argument outputId==null");
     	}
     	
     	if(inputId==null)
     	{
     		throw new NullArgumentException("Wrong argument inputId==null");
     	}
     	
     	if(!_projectTasks.containsKey(outputTaskName)){
     		throw new IllegalArgumentException("Wrong argument outputTaskName");
     	}
     	
     	if(!_projectTasks.containsKey(inputTaskName)){
     		throw new IllegalArgumentException("Wrong argument inputTaskName");
     	}
     	
     	TaskBridge bridge=new TaskBridge(outputTaskName, inputTaskName, outputId, inputId, _projectTasks);
     	_taskBridges.add(bridge);
     	return true;
     }
     
     
     /**
      * 
      * @return
      */
     public double getProjectPrice()
     {
     	return _projectPrice;
     }
     
     /**
      * 
      * @return
      */
     public synchronized String getDescription(){
     	return _description;
     }
     
     /**
      * @return The {@link #_projectTasks}. The list is read only.
      */
     public synchronized Map<String,ProjectTask> getProjectTasks ( ){
         return  new TreeMap<String,ProjectTask>(_projectTasks);
     }
     
     /**
      * 
      * @param systemClock
      * @return
      * @throws NullArgumentException
      */
     public synchronized boolean executeStep(){
     	if( _projectDuration<_currentStep ){
     		return false;
     	}
     	
     	for(Map.Entry<String, ProjectTask> entry : _projectTasks.entrySet()){
     		System.out.print(String.format("\n\nExecuting ProjectTask : %s\n", entry.getKey()) );
     		entry.getValue().executeTask(_currentStep);
     	}
     	
     	for(Map.Entry<String, ProjectTask> entry : _projectTasks.entrySet()){
     		entry.getValue().executeBridges();
     	}
 
     	ProjectTask overallProject=_projectTasks.get(CONST_DEPLOY_TASK_NAME);
     	_overallProgress=overallProject.getProgress();
     	_currentStep++;
     	return true;
     }
     
     /**
      * 
      * @param projectTaskName
      * @return
      * @throws NullArgumentException
      * @throws IllegalArgumentException
      */
     public ProjectTask getProjectTask(final String projectTaskName)throws NullArgumentException, IllegalArgumentException{
     	if(projectTaskName==null){
     		throw new NullArgumentException("Wrong argument projectTaskName==null");
     	}
     	
     	if(_projectTasks.containsKey(projectTaskName)){
     		return _projectTasks.get(projectTaskName);
     	}else{
     		throw new IllegalArgumentException("Wrong argument projectTaskName");
     	}
     }
     
     /**
      * 
      * @return
      * @throws SerializationException
      */
     public synchronized SerializableProject getSerializableProject()throws SerializationException{
     	try {
 			return new SerializableProject(this);
 		} catch (NullArgumentException e) {
 			throw new SerializationException("Serialization failed because " + e.getMessage());
 		}
     }
     
     /**
      * 
      * @return
      */
     public synchronized double getPrestigePoints(){
 		return _difficultyLevel*_overallProgress/100.0d;
     }
     
     /**
      * 
      * @return
      */
     public synchronized boolean isExpired(){
     	ProjectTask mainTask=_projectTasks.get(CONST_DEPLOY_TASK_NAME);
     	try {
 			if(mainTask.getOutput(ProjectTask.CONST_PROGRESS_SDOBJECT_NAME)>=100){
 				return true;
 			}
 		} catch (IllegalArgumentException e) {
 			return true;
 		} catch (NullArgumentException e) {
 			return true;
 		}
     	
     	if(_currentStep>=_projectDuration){
     		return true;
     	}
 		
     	return false;
     }
     
     /**
      * 
      * @return
      */
     public synchronized int getCurrentStep(){
     	return _currentStep;
     }
     
     /**
      * 
      * @return
      */
     public int getProjectDuration() {
 		return _projectDuration;
 	}
 }
