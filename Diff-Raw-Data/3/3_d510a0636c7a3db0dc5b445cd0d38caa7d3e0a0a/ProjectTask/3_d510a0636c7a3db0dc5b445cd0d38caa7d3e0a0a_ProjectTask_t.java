 package org.promasi.game.project;
 
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.locks.Lock;
 import java.util.concurrent.locks.ReentrantLock;
 
 import org.promasi.game.GameException;
 import org.promasi.sdsystem.SdSystem;
 import org.promasi.sdsystem.SdSystemBridge;
 import org.promasi.sdsystem.SdSystemException;
 import org.promasi.sdsystem.sdobject.equation.IEquation;
 import org.promasi.utilities.exceptions.NullArgumentException;
 import org.promasi.utilities.serialization.SerializationException;
 
 
 public class ProjectTask
 {
 	/**
 	 * 
 	 */
 	public static final double CONST_PROGRESS_MAX_VALUE=100.0;
 	
     /**
      * The name of the task.
      */
     protected String _name;
 
     /**
      * The description of the task.
      */
     protected String _description;
 
     /**
      * 
      */
     protected SdSystem _sdSystem;
     
     /**
      * The completed percentage.
      */
     protected double _progress;
     
     /**
      * 
      */
     private Map<String, Double> _employeeSkills;
     
     /**
      * 
      */
     protected List<SdSystemBridge> _sdSystemBridges;
     
     /**
      * 
      */
     protected IEquation _progressEquation;
     
     /**
      * 
      */
     private Lock _lockObject;
     
     /**
      * 
      */
     private double _sumEmployees;
 
     /**
      * Initializes the object.
      */
     public ProjectTask( String taskName, String description, SdSystem sdSystem, IEquation progressEquation )throws GameException
     {
         if(description==null)
         {
         	throw new GameException("Wrong argument description==null");
         }
         
         if(taskName==null)
         {
         	throw new GameException("Wrong argument taskName==null");
         }
         
         if(sdSystem==null){
         	throw new GameException("Wrong argument sdSystem==null"); 
         }
         
         if( progressEquation == null ){
         	throw new GameException("Wrong argument progressEquationString==null");
         }
         
         _progressEquation = progressEquation;
         _employeeSkills=new HashMap<String, Double>();
         _description=description;
         _name=taskName;
         _sdSystem=sdSystem;
         _sdSystemBridges=new LinkedList<SdSystemBridge>();
         
         _lockObject = new ReentrantLock();
         
         _progress= 0;
     }
 
     /**
      * 
      * @param outputSdObjectId
      * @param inputSdObjectId
      * @param inputProjectTask
      * @return
      */
     protected boolean makeBridge( String outputSdObjectId, String inputSdObjectId, ProjectTask inputProjectTask){
     	boolean result = false;
     	
     	try{
     		_lockObject.lock();
         	if(outputSdObjectId != null && inputSdObjectId != null && inputProjectTask != null){
             	SdSystemBridge bridge=new SdSystemBridge(outputSdObjectId, _sdSystem, inputSdObjectId, inputProjectTask._sdSystem);
             	_sdSystemBridges.add(bridge);
             	result = true;
         	}
     	}catch( SdSystemException e){
     		result = false;
     	}finally{
     		_lockObject.unlock();
     	}
     	
     	return result;
     }
     
     /**
      * @return The {@link #_name}.
      */
     public String getName ( )
     {
         return _name;
     }
 
     /**
      * @return The {@link #_description}.
      */
     public String getDescription ( )
     {
         return _description;
     }
 
     /**
      * @return The {@link #_progress}.
      */
     public double getProgress ( )
     {
         return _progress;
     }
     
     /**
      * 
      * @param systemClock
      * @return
      * @throws NullArgumentException
      */
     public boolean executeTask(int currentStep)
     {
     	boolean result = false;
     	
     	try{
     		_lockObject.lock();
         	if(_sumEmployees<=0.0){
         		_sumEmployees=1;
         	}
         	
         	if(currentStep>=0){
             	for(Map.Entry<String, Double> entry : _employeeSkills.entrySet()){
             		_sdSystem.setInput(entry.getKey(), entry.getValue());
             	}
                 
                 _sdSystem.executeStep();
                 if( _progress < CONST_PROGRESS_MAX_VALUE ){
                 	_progress=_progressEquation.calculateEquation( _sdSystem.getSystemValues() );
                	if(_progress > CONST_PROGRESS_MAX_VALUE){
                		_progress = CONST_PROGRESS_MAX_VALUE;
                	}
                 }
             	
         		for(Map.Entry<String, Double> entry : _employeeSkills.entrySet()){
         			_sdSystem.setInput(entry.getKey(), 0.0);
         		}
             	
         		_employeeSkills.clear();
             	_sumEmployees=0;
             	result = true;
         	}
         	
     	}catch(SdSystemException e){
     		result = false;
 		}finally{
 			_lockObject.unlock();
 		}
 
     	return result;
     }
 	
     /**
      * 
      */
     public boolean executeBridges(){
     	boolean result = true;
     	
     	try{
     		_lockObject.lock();
         	for(SdSystemBridge bridge :_sdSystemBridges){
         		result &= bridge.executeStep();
         	}
     	}finally{
     		_lockObject.unlock();
     	}
 
     	return result;
     }
     
 	/**
 	 * 
 	 * @return
 	 * @throws SerializationException 
 	 * @throws NullArgumentException 
 	 */
 	public ProjectTaskMemento getMemento(){
 		return new ProjectTaskMemento(this);
 	}
 
 	/**
 	 * 
 	 * @param employeeSkills
 	 * @return
 	 */
 	public boolean applyEmployeeSkills(Map<String, Double> employeeSkills){
 		boolean result = false;
 		
 		try{
 			_lockObject.lock();
 			if( employeeSkills!=null){
 				for(Map.Entry<String, Double> entry : employeeSkills.entrySet()){
 					if(entry.getKey()!=null && entry.getValue()!=null){
 						if(_employeeSkills.containsKey(entry.getKey())){
 							Double oldValue=_employeeSkills.get(entry.getKey());
 							_employeeSkills.put(entry.getKey(), entry.getValue()+oldValue);
 						}else{
 							_employeeSkills.put(entry.getKey(), entry.getValue());
 						}
 					}
 				}
 				
 				_sumEmployees+=1;
 				result = true;
 			}
 		}finally{
 			_lockObject.unlock();
 		}
 
 		return result;
 	}
 	
 	/**
 	 * 
 	 * @param ouputId
 	 * @return
 	 * @throws NullArgumentException
 	 * @throws IllegalArgumentException
 	 */
 	public Double getOutput(final String outputId)throws SdSystemException{
 		if(outputId==null){
 			throw new SdSystemException("Wrong argument outputId==null");
 		}
 		
 		try{
 			_lockObject.lock();
 			return _sdSystem.getValue(outputId);
 		}finally{
 			_lockObject.unlock();
 		}
 	}
 	
 	/**
 	 * 
 	 * @return
 	 */
 	public List<String> getOutputs(){
 		return _sdSystem.getInputs();
 	}
 	
 	/**
 	 * 
 	 * @return
 	 */
 	public List<String> getInputs(){
 		return _sdSystem.getInputs();
 	}
 	
 	/**
 	 * 
 	 * @param outputId
 	 * @return
 	 * @throws NullArgumentException
 	 */
 	public boolean hasOutput(final String outputId)throws SdSystemException{
 		if(outputId==null){
 			throw new SdSystemException("Wrong argument outputId==null");
 		}
 		
 		return _sdSystem.hasOutput(outputId);
 	}
 
     /**
      * 
      * @return
      */
     public boolean isValidTask(){
     	try{
     		_lockObject.lock();
     		return _progress < CONST_PROGRESS_MAX_VALUE;
     	}finally{
     		_lockObject.unlock();
     	}
     }
 }
