 /**
  * 
  */
 package org.promasi.game.company;
 
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.TreeMap;
 import java.util.concurrent.locks.Lock;
 import java.util.concurrent.locks.ReentrantLock;
 
 import org.joda.time.DateTime;
 import org.promasi.game.GameException;
 import org.promasi.game.model.generated.DepartmentModel;
 import org.promasi.game.model.generated.EmployeeModel;
 import org.promasi.game.model.generated.EmployeeTaskModel;
 import org.promasi.game.project.ProjectTask;
 import org.promasi.utilities.design.Observer;
 import org.promasi.utilities.exceptions.NullArgumentException;
 import org.promasi.utilities.logger.ILogger;
 import org.promasi.utilities.logger.LoggerFactory;
 import org.promasi.utilities.serialization.SerializationException;
 
 /**
  * @author alekstheod
  *
  */
 public class Department extends Observer<IDepartmentListener> implements IEmployeeListener{
 
     /**
      * All the hired employees of the company.
      */
     protected Map<String, Employee> _employees;
     
     /**
      * 
      */
     private Lock _lockObject;
     
     /**
      * 
      */
     private ILogger _logger = LoggerFactory.getInstance(Department.class);
     
     /**
      * 
      */
     private String _director;
     
     /**
      * 
      */
     public Department(){
     	_employees = new TreeMap<String, Employee>();
     	_lockObject = new ReentrantLock();
     	_logger.debug("Initialization succeed");
     }
     
     /**
      * @return The {@link #_employees}. The list cannot be modified.
      * @throws SerializationException 
      */
     public Map<String,EmployeeModel> getEmployees ( )
     {
     	Map<String,EmployeeModel> employees=new TreeMap<String, EmployeeModel>();
     	
     	try{
     		_lockObject.lock();
         	for(Map.Entry<String, Employee> entry : _employees.entrySet())
         	{
         		employees.put(entry.getKey(), entry.getValue().getMemento());
         	}
     	}finally{
     		_lockObject.unlock();
     	}
 
         return employees;
     }
     
     /**
      * 
      * @param employee
      * @throws SerializationException 
      */
     protected boolean hireEmployee(String supervisor, Employee employee, DateTime time)
     {
        	boolean result = false;
     	if(employee != null){
         	try{
         		_lockObject.lock();
             	if( !_employees.containsKey( employee.getEmployeeId() ) ){
                 	_employees.put(employee.getEmployeeId(), employee);
                 	employee.addListener(this);
                 	DepartmentModel memento = getMemento();
                     for(IDepartmentListener listener : getListeners()){
                     	listener.employeeHired(_director, memento, employee.getMemento(), time);
                     }
                     
                     employee.setSupervisor(_director);
                     result = true;
                     _logger.debug("Employee hired '" + employee.getEmployeeId() + "'");
             	}else{
             		_logger.warn("Hire employee failed because employee with the same id is already hired '" + employee.getEmployeeId() + "'");
             	}
         	}finally{
         		_lockObject.unlock();
         	}
     	}
     	
     	return result;
     }
     
     /**
      * 
      * @param listener
      * @return
      */
     public boolean addEmployeeListener( IEmployeeListener listener ){
     	boolean result = false;
     	if( listener != null ){
     		try{
     			result = true;
 				_lockObject.lock();
 				for (Map.Entry<String, Employee> employee : _employees.entrySet()){
 					result &= employee.getValue().addListener(listener);
 				}
 			}finally{
 				_lockObject.unlock();
 			}
     	}
 
 		return result;
     }
     
     /**
      * 
      * @param listener
      * @return
      */
     public boolean removeEmployeeListener( IEmployeeListener listener ){
     	boolean result = false;
     	if( listener != null ){
     		try{
     			result = true;
 				_lockObject.lock();
 				for (Map.Entry<String, Employee> employee : _employees.entrySet()){
 					result &= employee.getValue().removeListener(listener);
 				}
 			}finally{
 				_lockObject.unlock();
 			}
     	}
 
 		return result;
     }
 
     /**
      * 
      * @return
      */
     public DepartmentModel getMemento(){
     	DepartmentModel result = new DepartmentModel();
     	
     	DepartmentModel.Employees employees = new DepartmentModel.Employees();
     	for( Map.Entry<String, Employee > entry : _employees.entrySet() ){
     		DepartmentModel.Employees.Entry newEntry = new DepartmentModel.Employees.Entry();
     		newEntry.setKey(entry.getKey());
     		newEntry.setValue(entry.getValue().getMemento());
     	}
     	
     	result.setEmployees(employees);
     	return result;
     }
     
     /**
      * 
      * @param employee
      * @return
      * @throws NullArgumentException
      * @throws SerializationException 
      */
     public boolean dischargeEmployee(final String employeeId, MarketPlace marketPlace, DateTime time){
     	boolean result = false;
     	if(employeeId!=null){
         	if( _employees.containsKey(employeeId) ){
             	try{
             		_lockObject.lock();
                 	Employee currentEmployee=_employees.get(employeeId);
                 	_employees.get(employeeId).removeListener(this);
                 	_employees.remove(employeeId);
                 	if(marketPlace.dischargeEmployee(currentEmployee)){
                 		DepartmentModel memento = getMemento();
                         for(IDepartmentListener listener : getListeners()){
                         	listener.employeeDischarged(_director, memento, currentEmployee.getMemento(), time);
                         }
                         
                         currentEmployee.clearListeners();
                         currentEmployee.setSupervisor(null);
                         result = true;
                 	}else{
                 		_employees.put(currentEmployee.getEmployeeId(), currentEmployee);
                 	}
             	}finally{
             		_lockObject.unlock();
             	}
         	}
     	}
     	
     	return result;
     }
     
     /**
      * 
      * @param employee
      * @return
      */
     public boolean isEmployeeHired(Employee employee)
     {
     	if(_employees.containsKey(employee.getEmployeeId()) )
     	{
     		return true;
     	}
     	
     	return false;
     }
     
     /**
      * Will remove all assigned tasks to
      * to the departments employees.
      * @return true if succeed, false otherwise.
      */
     public boolean removeAssignedTasks(){
     	boolean result = true;
     	try{
     		_lockObject.lock();
     		for(Map.Entry<String, Employee> entry : _employees.entrySet()){
     			result &= entry.getValue().removeAllTasks();
     		}
     	}finally{
     		_lockObject.unlock();
     	}
     	
     	return result;
     }
     
     public boolean removeTasks( List< EmployeeTaskModel > tasks ){
     	boolean result = true;
     	try{
     		_lockObject.lock();
     		if( tasks != null ){
 	    		for(Map.Entry<String, Employee> entry : _employees.entrySet()){
 	    			for(EmployeeTaskModel task : tasks ){
 	    				result &= entry.getValue().removeEmployeeTask(task.getProjectTaskName());
 	    			}
 	    		}
     		}
     	}finally{
     		_lockObject.unlock();
     	}
     	
     	return result;
     }
     
     /**
      * 
      * @param employee
      * @param employeeTask
      */
     public boolean assignTasks( String employeeId, List<EmployeeTaskModel> employeeTasks , Map<String, ProjectTask> projectTasks, DateTime time ){
     	boolean result = false;
     	
     	try{
     		_lockObject.lock();
         	if(employeeId!=null && employeeTasks != null ){
         		result = true;
         		Map<String, EmployeeTask> allTasks = new TreeMap<>();
         		for( Map.Entry<String, Employee> entry : _employees.entrySet()){
         			Map<String, EmployeeTask> tasks = entry.getValue().getAssignedTasks();
         			for( Map.Entry<String, EmployeeTask> taskEntry : tasks.entrySet()){
         				if(!allTasks.containsKey(entry.getKey()) ){
         					allTasks.put(taskEntry.getKey(), taskEntry.getValue());
         				}
         			}
         		}
         		
         		List<EmployeeTask> tasks=new  LinkedList<EmployeeTask>();
         		try {
     	    		for(EmployeeTaskModel employeeTask : employeeTasks){
     	    			
     	        		String taskName=employeeTask.getProjectTaskName();
     	    			ProjectTask projectTask=projectTasks.get(taskName);
     	    			if(_employees.containsKey(employeeId)){
         	    			EmployeeTask task=new EmployeeTask( employeeTask.getTaskName(), 
 							        	    					employeeTask.getDependencies(), 
 							        	    					projectTask,
 							        	    					employeeTask.getFirstStep(),
 							        	    					employeeTask.getLastStep());
         	    			
         	        		for( Map.Entry<String, EmployeeTask> taskEntry : allTasks.entrySet()){
     	        				task.applyDependencie(taskEntry.getValue());
         	        		}
         	        		
         	        		for( EmployeeTask preparedTask : tasks){
         	        			task.applyDependencie(preparedTask);
         	        		}
         	        		
         	    			result = tasks.add(task);
     	    			}
     	    		}
         		
     				result &= _employees.get( employeeId ).assignTasks(tasks);
     			} catch (GameException e) {
     				result = false;
     				_logger.warn("Assign tasks failed because an internal exception '" + e.getMessage()+ "'");
     			}
         	}
         	
         	if( result ){
         		for ( IDepartmentListener listener : getListeners() ){
         			listener.tasksAssigned(_director, getMemento(), time);
         		}
         	}else{
         		for ( IDepartmentListener listener : getListeners() ){
         			listener.tasksAssignFailed(_director, getMemento(), time);
         		}
         	}
     	}finally{
     		_lockObject.unlock();
     	}
     	
     	return result;
     }
 
     /**
      * 
      * @param marketPlace
      * @return
      */
     public boolean dischargeEmployees(MarketPlace marketPlace, DateTime time){
     	boolean result = false;
     	
     	if( marketPlace != null ){
         	try{
         		_lockObject.lock();
         		result = true;
         		for(Map.Entry<String, Employee> entry : _employees.entrySet()){
         			entry.getValue().removeListener(this);
         			entry.getValue().removeAllTasks();
         			result &= marketPlace.dischargeEmployee(entry.getValue());
         		}
         		
         		_employees.clear();
                 for(IDepartmentListener listener : getListeners()){
                 	listener.employeeDischarged(_director, getMemento(), null, time);
                 }
                 
         	}finally{
         		_lockObject.unlock();
         	}	
     	}
 
     	return result;
     }
     
     /**
      * 
      * @param currentStep
      * @return
      */
     public boolean executeWorkingStep( int currentStep ){
     	boolean result = true;
     	
     	try{
     		_lockObject.lock();
     		for ( Map.Entry<String, Employee> entry : _employees.entrySet() ){
     			result &= entry.getValue().executeTasks(currentStep);
     		}
     	}finally{
     		_lockObject.unlock();
     	}	
 
     	return result;
     }
     
     /**
      * 
      * @return
      */
     public double calculateFees(){
     	double result = 0;
     	
     	try{
     		_lockObject.lock();
     		for(Map.Entry<String, Employee> entry : _employees.entrySet()){
     			result += entry.getValue().getSalary();
     		}
     	}finally{
     		_lockObject.unlock();
     	}	
 
     	return result;
     }
     
     /**
      * 
      * @param director
      */
     public void setDirector( String director ){
     	try{
     		_lockObject.lock();
     		_director = director;
     		for(Map.Entry<String, Employee> entry : _employees.entrySet()){
     			entry.getValue().setSupervisor(_director);
     		}
     	}finally{
     		_lockObject.unlock();
     	}	
     }
     
     /**
      * 
      * @return
      */
     public String getDirector(){
     	try{
     		_lockObject.lock();
     		return _director;
     	}finally{
     		_lockObject.unlock();
     	}
     }
     
     @Override
     public void clearListeners(){
     	try{
     		_lockObject.lock();
     		super.clearListeners();
     		for ( Map.Entry<String, Employee> employee : _employees.entrySet()){
     			employee.getValue().clearListeners();
     		}
     	}finally{
     		_lockObject.unlock();
     	}
     }
 
 	@Override
 	public void taskAssigned(String supervisor, EmployeeModel employee) {}
 
 	@Override
 	public void taskDetached(String supervisor, EmployeeModel employee,
 			EmployeeTaskModel task) {
 		for( Map.Entry<String, Employee> entry : _employees.entrySet() ){
 			entry.getValue().removeEmployeeTaskDependencie(task.getTaskName());
 		}
 	}
 
 	@Override
 	public void tasksAssignFailed(String supervisor, EmployeeModel employee) {}
 }
