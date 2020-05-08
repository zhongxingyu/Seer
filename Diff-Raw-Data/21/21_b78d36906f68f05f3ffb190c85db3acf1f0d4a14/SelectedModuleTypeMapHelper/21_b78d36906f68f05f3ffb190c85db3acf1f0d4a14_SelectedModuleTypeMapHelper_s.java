 package ussr.builder.helpers;
 
 import ussr.builder.enumerations.SupportedModularRobots;
 
 /**
  * Acts as a helper class for ConstructionToolSpecification.java in a way that maps modular robot name to
  * the state of selection. Meaning, that if the robot is selected in the simulation environment the if will 
  * by identified by boolean true. The class is used with methods like isATRON(), isMTRAN() and so on.  
  * @author Konstantinas
  */
 public class SelectedModuleTypeMapHelper {
 
 	/**
 	 * Modular robot supported by builder.
 	 */
 	private SupportedModularRobots modularRobotName;
 
 
 	/**
 	 * The state of selection in simulation environment with a mouse.
 	 */
 	private boolean isSelected;	
 
 
 	/**
 	 * Acts as a helper class for ConstructionToolSpecification.java in a way that maps modular robot name to
 	 * the state of selection. Meaning, that if the robot is selected in the simulation environment the if will 
 	 * by identified by boolean true. 
 	 * @param modularRobotName, modular robot supported by builder.
 	 * @param isSelected,the state of selection in simulation environment with a mouse.
 	 */
 	public SelectedModuleTypeMapHelper(SupportedModularRobots modularRobotName,boolean isSelected){
 		this.modularRobotName = modularRobotName;
 		this.isSelected = isSelected;		
 	}
 
 	/**
 	 * Returns modular robot name supported by "builder". 
 	 * @return modularRobotName, modular robot supported by builder
 	 */
	public SupportedModularRobots getModularRobotName() {
 		return modularRobotName;
 	}
 
 	/**
 	 * Returns true if the module is selected in simulation environment.
 	 * @return isSelected, true if the module is selected in simulation environment.
 	 */
 	public boolean isSelected() {
 		return isSelected;
 	}
 
 }
