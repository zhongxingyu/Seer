 package ussr.remote.facade;
 
 import java.rmi.Remote;
 import java.rmi.RemoteException;
 
 import ussr.aGui.enumerations.HintPanelTypes;
 import ussr.builder.enumerations.ConstructionTools;
 import ussr.builder.enumerations.SupportedModularRobots;
 
 public interface GUICallbackControl extends Remote {
 
 	/**
 	 * Adapts Construct Robot tab to the tool chosen by user. To be more precise disables and enables relevant components of the tab. 
 	 * @param chosenTool,the tool chosen by the user in Construct Robot tab.
 	 */
 	public void adaptConstructRobotTabToChosenTool(ConstructionTools chosenTool)throws RemoteException;
 
 	/**
 	 * Populates the table in Assign behavior tab with labels of entity(module, connector and so on) selected in simulation environment.
 	 * @param labels, the string of labels separated by comma to populate the table with.
 	 */
 	public void updateTableWithLabels(String labels) throws RemoteException;
 	
 	/**
 	 * Sets the global ID of the module selected in simulation environment.
 	 * @param selectedModuleID, the global ID of the module selected in simulation environment.
 	 */
 	public void setSelectedModuleID(int selectedModuleID)throws RemoteException;
 	
 	/**
 	 * Adapts construct robot to the module type selected in simulation environment.
 	 * @param supportedModularRobot, the type of modular robot to adapt to.
 	 */
 	public void adaptConstructRobotTabToSelectedModuleType(SupportedModularRobots supportedModularRobot)throws RemoteException;
 	
 	/**
 	 * Updates the hint panel in the tab called Assign Behaviors by changing icon and text of it.
 	 * @param hintPanelTypes, the type of hint panel.
 	 * @param text, the text for hint panel to display.
 	 */
 	public void updateHintPanelAssignBehaviorsTab(HintPanelTypes hintPanelTypes, String text)throws RemoteException;
 }
