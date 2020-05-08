 package ussr.builder.constructionTools;
 
 import java.io.Serializable;
 
 import javax.swing.JOptionPane;
 import com.jme.scene.Geometry;
 import ussr.model.Module;
 import ussr.physics.jme.JMEModuleComponent;
 import ussr.physics.jme.JMESimulation;
 import ussr.physics.jme.pickers.CustomizedPicker;
 import ussr.builder.enumerations.ConstructionTools;
 import ussr.builder.enumerations.SupportedModularRobots;
 import ussr.builder.helpers.BuilderHelper;
 import ussr.builder.helpers.SelectedModuleTypeMapHelper;
 
 /**
  * The main responsibility of this class is to specify the tool for construction of
  * modular robot's morphology. In order to do that, some parameters should be passed in 
  * constructor others are extracted from simulation environment (when user selects the
  * modules or connectors on the modules).
  * @author Konstantinas
  */
 public class ConstructionToolSpecification extends CustomizedPicker implements Serializable{
 
 	/**
 	 * The interface to construction of modular robot morphology. This one is on the level of modules of modular robot(creation and movement of them).  
 	 */
 	private SelectOperationsTemplate selectOperations;
 
 	/**
 	 * The interface to construction of modular robot morphology. This one is on the level of components of modules.  
 	 */
 	private ConstructionTemplate construction;
 
 	/**
 	 * The module selected in simulation environment with the left side of the mouse.
 	 */
 	private Module selectedModule;    
 
 	/**
 	 * The connector number on the module, selected with the left side of mouse in simulation environment.
 	 */	
 	private int selectedConnectorNr = 1000;//just to avoid having default 0 value, which is also number of connector. 
 
 	/**
 	 * The connector number on the module chosen in GUI comboBox.
 	 */	
 	private int chosenConnectorNr = 1000;//just to avoid having default 0 value, which is also number of connector. 
 
 	/**
 	 * The name of the modular robot. For example: ATRON, MTRAN, ODIN and so on
 	 */
 	private SupportedModularRobots modularRobotName;
 
 	/**
 	 * The name of the tool from GUI. For example these can be "ON_SELECTED_CONNECTOR", "ON_CHOSEN_CONNECTOR","LOOP" and so on.
 	 */
 	private ConstructionTools toolName;	
 
 	/**
 	 * The name of rotations, which are standard to particular modular robot. For example for ATRON this can be EW, meaning east-west.
 	 */
 	private String standardRotationName;
 
 	/**
 	 * For calling tools handling construction of morphology of modular robot, in particular tools like "ON_SELECTED_CONNECTOR","ON_ALL_CONNECTORS" and "VARIATION". 
 	 * @param toolName,the name of the tool from GUI. For example, in this case, these can be "ON_SELECTED_CONNECTOR", "ON_ALL_CONNECTORS" and "VARIATION".
 	 */
 	public  ConstructionToolSpecification(ConstructionTools toolName){
 		this.toolName = toolName;		
 	}
 
 	/**
 	 * For calling tools handling construction of morphology of modular robot,in particular tools like "ON_CHOSEN_CONNECTOR" or "LOOP".
 	 * @param toolName,the name of the tool from GUI. For example, in this  case, these can be "ChosenConnector" or "Loop".
 	 * @param chosenConnectorNr,the connector number on module, chosen in GUI comboBox ("ON_CHOSEN_CONNECTOR")or just passed as default ("LOOP").
 	 */
 	public  ConstructionToolSpecification(ConstructionTools toolName,int chosenConnectorNr){
 		this.toolName = toolName;
 		this.selectedConnectorNr = chosenConnectorNr;
 	}
 
 	/**
 	 * For calling tools handling construction of morphology of modular robot, in particular tools like "STANDARD_ROTATION". 
 	 * @param toolName, the name of the tool from GUI. For example, in this case, this is "STANDARD_ROTATION".
 	 * @param standardRotationName,the name of rotation, which is standard to particular modular robot. For example for ATRON this can be EW, meaning east-west.
 	 */
 	public  ConstructionToolSpecification(ConstructionTools toolName, String standardRotationName) {
 		this.toolName = toolName;
 		this.standardRotationName = standardRotationName;	
 	}
 
 	/**
 	 * Instantiates the tool.
 	 * @param jmeSimulation, the physical simulation.
 	 */
 	private void instantiateTool(JMESimulation jmeSimulation){
 
 		/*Keeps and updates the data about the module type currently selected in simulation environment*/
 		SelectedModuleTypeMapHelper[] selectModulesTypes = {
 				new SelectedModuleTypeMapHelper(SupportedModularRobots.ATRON,isAtron()),
 				new SelectedModuleTypeMapHelper(SupportedModularRobots.MTRAN,isMtran()),
 				new SelectedModuleTypeMapHelper(SupportedModularRobots.ODIN,isOdin()),
 				new SelectedModuleTypeMapHelper(SupportedModularRobots.CKBOTSTANDARD,isCKBotStandard())
 		};
 		/*Identifies selected module and readjusts tools accordingly, also calls for GUI re-adjustment*/
 		for(int index =0;index<selectModulesTypes.length;index++){
 			if(selectModulesTypes[index].isSelected()==true){
				this.modularRobotName = selectModulesTypes[index].getModularRobotName();
 				this.selectOperations = new SelectOperationsAbstractFactory().getSelectOperations(jmeSimulation,modularRobotName);
 				this.construction = selectOperations.getConstruction();				
 			}
 		}
 
 	}
 
 	/**
 	 * Used to keep track how many times entities were selected in simulation environment.
 	 */
 	int timesSelected =-1;
 	
 	/**
 	 * Returns amount of times entities were selected in simulation environment.
 	 * @return timesSelected, amount of times entities were selected in simulation environment.
 	 */
 	public int getTimesSelected() {
 		return timesSelected;
 	}
 
 	/**
 	 * Resets counter to initial value.
 	 */
 	public void resetTimesSelected() {
 		timesSelected = -1;
 	}
 
 	/* Method executed when the module is selected with the left side of the mouse in simulation environment.
 	 * Here is identified the module selected in simulation environment, moreover checked if pickTarget()method
 	 * resulted in success and the call for appropriate tool is made. 
 	 * @see ussr.physics.jme.pickers.CustomizedPicker#pickModuleComponent(ussr.physics.jme.JMEModuleComponent)
 	 */
 	@Override
 	protected void pickModuleComponent(JMEModuleComponent component) {
 		timesSelected++;
 		this.selectedModule = component.getModel();
 		instantiateTool((JMESimulation)component.getSimulation());		
 
 
 		//if (this.toolName.equals(ConstructionTools.LOOP)){
 			//ConstructRobotTab.setEnabledButtonsArrows(true);	
 		//}
 		callAppropriateTool();		
 	}
 
 	/* Method executed when the module is selected with the left side of the mouse in simulation environment.
 	 * Here the connector number is extracted from the string of TriMesh. Initial format of string is for example: "Connector 1 #1"
 	 * @see ussr.physics.jme.pickers.CustomizedPicker#pickTarget(com.jme.scene.Spatial)
 	 */
 	@Override
 	protected void pickTarget(Geometry target,JMESimulation jmeSimulation) {
 
 		if (toolName.equals(ConstructionTools.NEW_MODULE_ON_SELECTED_CONNECTOR)){			
 			this.selectedConnectorNr = BuilderHelper.extractConnectorNr(jmeSimulation, target);
			System.out.println("Connector:"+selectedConnectorNr );
 			//Adapt Construct Robot tab
 			//ConstructRobotTab.setEnabledRotationToolBar(false);
 			//ConstructRobotTab.getJButtonMove().setEnabled(false);
 		}
 	}
 
 	/**
 	 * Checks if construction tool type is matching the module type selected in simulation environment. If
 	 * yes calls appropriate tool. If no, For example: if the tool is for ATRON modular robot (modularRobotName) 
 	 * and the module type selected in simulation environment is MTRAN. Then the method will complain.	 * 
 	 */
 	private void callAppropriateTool(){
 		if (this.modularRobotName.equals(SupportedModularRobots.ATRON)&& isAtron()||this.modularRobotName.equals(SupportedModularRobots.MTRAN)&& isMtran()||this.modularRobotName.equals(SupportedModularRobots.ODIN)&&isOdin()||this.modularRobotName.equals(SupportedModularRobots.CKBOTSTANDARD)&&isCKBotStandard()){		
 			callTool();	
 		}else{
 			reAdjustUserInput();
 		}
 	}
 
 
 	/**
 	 * Makes the tools more "generic", in a sense that each tool is working for all Supported modular robots, without the need to
 	 * specify it explicitly in the GUI.
 	 * Also makes feedback calls to GUI to adapt to the modular robot the user is working with right now. 
 	 */
 	/**
 	 * 
 	 */
 	private void reAdjustUserInput(){
 
 		/*	Keeps and updates the data about the module type currently selected in simulation environment
 		SelectedModuleTypeMapHelper[] selectModulesTypes = {
 				new SelectedModuleTypeMapHelper(SupportedModularRobots.ATRON,isAtron()),
 				new SelectedModuleTypeMapHelper(SupportedModularRobots.MTRAN,isMtran()),
 				new SelectedModuleTypeMapHelper(SupportedModularRobots.ODIN,isOdin()),
 				new SelectedModuleTypeMapHelper(SupportedModularRobots.CKBOTSTANDARD,isCKBotStandard())
 		};
         Identifies selected module and readjusts tools accordingly, also calls for GUI re-adjustment
 		for(int index =0;index<selectModulesTypes.length;index++){
 			if(selectModulesTypes[index].isSelected()==true){
 				this.modularRobotName = selectModulesTypes[index].getModularRobotName();
 				this.selectOperations = new SelectOperationsAbstractFactory().getSelectOperations(jmeSimulation,modularRobotName);
 				this.construction = selectOperations.getConstruction();				
 			}
 		}*/
 
 		//ConstructRobotTabController.adjustTabToSelectedModule(this.modularRobotName);//Adapt GUI to selected module(modular robot) type
 	}
 
 	/**
 	 * Checks if the module selected in simulation environment is an ATRON module 
 	 * @return true, if selected module is an ATRON module
 	 */
 	private boolean isAtron(){
 		String typeofModule = this.selectedModule.getProperty(BuilderHelper.getModuleTypeKey());		
 		if (typeofModule.contains(SupportedModularRobots.ATRON.toString())){
 			return true;
 		}
 		return false;
 	}
 
 	/**
 	 * Checks if the module selected in simulation environment is an MTRAN module 
 	 * @return true, if selected module is an MTRAN module
 	 */
 	private boolean isMtran(){		
 		String typeofModule = this.selectedModule.getProperty(BuilderHelper.getModuleTypeKey());
 		if (typeofModule.contains(SupportedModularRobots.MTRAN.toString())){
 			return true;
 		}
 		return false;
 	}
 
 	/**
 	 * Checks if the module selected in simulation environment is an Odin module 
 	 * @return true, if selected module is an Odin module
 	 */
 	private boolean isOdin(){
 		String typeofModule = this.selectedModule.getProperty(BuilderHelper.getModuleTypeKey());		
		if (typeofModule.contains(SupportedModularRobots.ODIN.toString())){			
 			return true;
 		}
 		return false;
 	}
 
 	/**
 	 * Checks if the module selected in simulation environment is an CKBotStandard module 
 	 * @return true, if selected module is an CKBotStandard module
 	 */
 	private boolean isCKBotStandard(){
 		String typeofModule = this.selectedModule.getProperty(BuilderHelper.getModuleTypeKey());		
 		if (typeofModule.toUpperCase().contains(SupportedModularRobots.CKBOTSTANDARD.toString())){			
 			return true;
 		}
 		return false;
 	}
 
 	/**
 	 * Calls the tool for construction of modular robot morphology. 
 	 */
 	private void callTool(){
 		
 		switch(toolName){
 		case NEW_MODULE_ON_SELECTED_CONNECTOR:
 			if (connectorsMatch()){
 				this.selectOperations.addNewModuleOnConnector(this);
 			}else{//Just skip(connector number will be 1000) 		
 			}
 			break;
 		case ON_CHOSEN_CONNECTOR_NR://break through
 		case MOVE_MODULE_FROM_CON_TO_CON:
 			this.selectOperations.addNewModuleOnConnector(this);
 			break;
 		case NEW_MODULES_ON_ALL_CONNECTORS:
 			this.selectOperations.addModulesOnAllConnectors(this);
 			break;
 		case STANDARD_ROTATIONS:
 			this.selectOperations.rotateModuleStandardRotation(this, this.standardRotationName);
 			break;
 		case MODULE_OPPOSITE_ROTATION:
 			this.selectOperations.rotateModuleWithOppositeRotation(this);
 			break;
 		case VARIATE_MODULE_OR_PROPERTIES:
 			this.selectOperations.variateModule(this);
 			break;
 		case AVAILABLE_ROTATIONS:
 			this.selectOperations.rotateModuleStandardRotationInLoop(this);
 			break;
 		default: throw new Error ("The tool with name: " + toolName +", is not supported yet.");
 		}
 	}
 
 	/**
 	 * Checks for match between the number of connector extracted from TriMesh(method pickTarget()) string and existing connectors of the module 
 	 * @return true, if pickTarget() method resulted in success with extraction of connector number from trimesh string and there is such number
 	 * of connector on selected module. 
 	 */
 	private boolean connectorsMatch(){		
 		int amountConnectors = this.selectedModule.getConnectors().size();
 		for (int connector=0;connector<amountConnectors;connector++){
 			String connectorNr = this.selectedModule.getConnectors().get(connector).getProperty(BuilderHelper.getModuleConnectorNrKey());		
 			if (connectorNr== null){
 				JOptionPane.showMessageDialog(null, "Something is wrong with property called: ussr.connector_number, implemented by Ulrik Pagh Schultz. Or property is not set at all.","Error", JOptionPane.ERROR_MESSAGE);				
 			}else if(Integer.parseInt(connectorNr)==this.selectedConnectorNr){			
 				return true;				
 			}
 		}		
 		return false;		
 	} 
 
 	/**
 	 * Returns the name of modular robot specified in selection tool
 	 * @return modularRobotName, the name of modular robot
 	 */
 	public SupportedModularRobots getModularRobotName() {
 		return modularRobotName;
 	}
 
 	/**
 	 * Returns the object of assigned construction strategy.  
 	 * @return construction, the object of assigned construction strategy. For example for ATRON this will be an instance of ATRONConstructionStrategy.java.
 	 */
 	public ConstructionTemplate getConstruction() {
 		return construction;
 	}
 
 	/**
 	 * Returns the connector chosen in GUI.
 	 * @return selectedConnectorNr,the connector number selected in GUI or on the module in simulation environment.
 	 */
 	public int getChosenConnectorNr() {
 		return chosenConnectorNr;
 	}
 
 	/**
 	 * Returns the module selected in simulation environment with the left side of the mouse
 	 * @return, the module selected in simulation environment with the left side of the mouse
 	 */
 	public Module getSelectedModule() {
 		return selectedModule;
 	}
 
 	/** 
 	 * Returns the connector number selected on module in simulation environment or the one chosen in GUI
 	 * @param selectedConnectorNr, the connector number selected on module in simulation environment or the one chosen in GUI
 	 */
 	public void setSelectedConnectorNr(int selectedConnectorNr) {
 		this.selectedConnectorNr = selectedConnectorNr;
 	}
 
 	/**
 	 * Returns connector number selected in simulation environment.
 	 * @return selectedConnectorNr, connector number selected in simulation environment.
 	 */
 	public int getSelectedConnectorNr() {
 		return selectedConnectorNr;
 	}	
 
 }
