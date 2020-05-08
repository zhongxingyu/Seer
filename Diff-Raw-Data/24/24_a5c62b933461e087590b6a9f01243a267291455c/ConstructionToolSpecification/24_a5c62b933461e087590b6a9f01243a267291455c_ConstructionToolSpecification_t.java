 package ussr.builder.constructionTools;
 
 import java.awt.Color;
 import java.io.Serializable;
 import java.rmi.RemoteException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 
 import javax.swing.JOptionPane;
 import com.jme.scene.Geometry;
 import ussr.model.Module;
 import ussr.physics.jme.JMEModuleComponent;
 import ussr.physics.jme.JMESimulation;
 import ussr.physics.jme.pickers.CustomizedPicker;
 import ussr.remote.facade.RemotePhysicsSimulationImpl;
 import ussr.builder.enumerations.ConstructionTools;
 import ussr.builder.enumerations.OdinModules;
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
 @SuppressWarnings("serial")
 public class ConstructionToolSpecification extends CustomizedPicker implements Serializable{
 
 	/**
 	 * The physical simulation
 	 */
 	private JMESimulation jmeSimulation;
 
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
 				this.modularRobotName = selectModulesTypes[index].getMRobotName();
 				this.selectOperations = new SelectOperationsAbstractFactory().getSelectOperations(jmeSimulation,modularRobotName);
 				this.construction = selectOperations.getConstruction();				
 			}
 		}
 		if (modularRobotName == null){
 			throw new Error("Not supported modular robot");
 		}
 
 		try {
 			RemotePhysicsSimulationImpl.getGUICallbackControl().adaptConstructRobotTabToSelectedModuleType(this.modularRobotName);
 		} catch (RemoteException e) {
 			throw new Error("Failed  adapt GUI to module selected in simulation environment, due to remote exception.");
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
 		int selectedModuleID = selectedModule.getID();
 		try {
 			RemotePhysicsSimulationImpl.getGUICallbackControl().setSelectedModuleID(selectedModuleID);
 		} catch (RemoteException e1) {
 			throw new Error("Failed to set ID of module selected in simulation environment due to remote exception.");
 		}		
 
 		this.jmeSimulation = (JMESimulation)component.getSimulation();		
 		instantiateTool(jmeSimulation);		
 
 		if (this.toolName.equals(ConstructionTools.MOVE_MODULE_FROM_CON_TO_CON)){
 			try {
 				RemotePhysicsSimulationImpl.getGUICallbackControl().adaptConstructRobotTabToChosenTool(ConstructionTools.MOVE_MODULE_FROM_CON_TO_CON);
 			} catch (RemoteException e) {
 				throw new Error("Failed adapt Construction Tab to selection tool named as" +ConstructionTools.MOVE_MODULE_FROM_CON_TO_CON.toString()+", due to remote exception.");
 			}
 		}
 		callAppropriateTool();		
 	}
 
 	/* Method executed when the module is selected with the left side of the mouse in simulation environment.
 	 * Here the connector number is extracted from the string of TriMesh. Initial format of string is for example: "Connector 1 #1"
 	 * @see ussr.physics.jme.pickers.CustomizedPicker#pickTarget(com.jme.scene.Spatial)
 	 */
 	@Override
 	protected void pickTarget(Geometry target,JMESimulation jmeSimulation) {
 		//jmeSimulation.moveDisplayTo(); //FOR TESTING
 
 		if (toolName.equals(ConstructionTools.NEW_MODULE_ON_SELECTED_CONNECTOR)){			
 			this.selectedConnectorNr = BuilderHelper.extractConnectorNr(jmeSimulation, target);
 			try {
 				RemotePhysicsSimulationImpl.getGUICallbackControl().adaptConstructRobotTabToChosenTool(ConstructionTools.NEW_MODULE_ON_SELECTED_CONNECTOR);
 			} catch (RemoteException e) {
 				throw new Error("Failed adapt Construction Tab to selection tool named as" +ConstructionTools.NEW_MODULE_ON_SELECTED_CONNECTOR.toString()+", due to remote exception.");
 			}
 		}
 	}
 
 	/**
 	 * Checks if construction tool type is matching the module type selected in simulation environment. If
 	 * yes calls appropriate tool.  
 	 */
 	private void callAppropriateTool(){
 		if (this.modularRobotName.equals(SupportedModularRobots.ATRON)&& isAtron()||this.modularRobotName.equals(SupportedModularRobots.MTRAN)&& isMtran()||this.modularRobotName.equals(SupportedModularRobots.ODIN)&&isOdin()||this.modularRobotName.equals(SupportedModularRobots.CKBOTSTANDARD)&&isCKBotStandard()){		
 			callTool();	
 		}
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
 		if (typeofModule.contains(SupportedModularRobots.ODIN.toString().toLowerCase().replace("o", "O"))){			
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
 
 	boolean firstTime = true;
 	boolean firstTimeNew = true;
 	List<Color> colorsComponents;
 	ArrayList<Color> colorsConnectors;
 
 
 	Module lastModuleNEW;
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
 			callBackNewModuleAdded();
 			break;
 		case ON_CHOSEN_CONNECTOR_NR:	
 			//FIXME
 
 			this.selectOperations.addNewModuleOnConnector(this);
 
 			/*if (selectedModuleType.contains("OdinBall")){
 				//int storeConnectorNumber = this.selectedConnectorNr;
 				if (firstTimeNew){
 					firstTimeNew =false;
 				this.selectOperations.addNewModuleOnConnector(this);
 				this.selectedConnectorNr = 0;
 				int nrModules = jmeSimulation.getModules().size();
 				this.selectedModule = jmeSimulation.getModules().get(nrModules -1);
 				this.selectOperations.addNewModuleOnConnector(this);
 				//this.selectedModule = null;
 				//this.selectedConnectorNr=1000;
 				}
 			}else{
 
 				this.selectOperations.addNewModuleOnConnector(this);
 			}*/
 
 
 			callBackNewModuleAdded();
 			break;
 		case MOVE_MODULE_FROM_CON_TO_CON:
 			activateJumpFromConToConTool();
 			callBackNewModuleAdded();
 			break;
 		case NEW_MODULES_ON_ALL_CONNECTORS:
 			this.selectOperations.addModulesOnAllConnectors(this);
 			callBackNewModuleAdded();
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
 	 * Activates tool for moving new module around selected one from to connnector onto another.
 	 */
 	private void activateJumpFromConToConTool(){
 		String selectedModuleType = selectedModule.getProperty(BuilderHelper.getModuleTypeKey());
 
 		if (selectedModuleType.contains("Odin")){
 			if (selectedModuleType.contains("Ball")){
 				Random rand = new Random();
 				this.selectedConnectorNr = rand.nextInt(selectedModule.getConnectors().size()-1);
 			}else{
 				this.selectedConnectorNr =0;
 			}
 		}else{
		    //Random rand = new Random();
			//this.selectedConnectorNr = rand.nextInt(selectedModule.getConnectors().size()-1);
			this.selectedConnectorNr =-1;
			
 		}
 
 
 		int selectedModuleId = selectedModule.getID();	
 		int amountModules = jmeSimulation.getModules().size();
 		Module lastModule = jmeSimulation.getModules().get(amountModules-1);
 		String lastModuleType = lastModule.getProperty(BuilderHelper.getModuleTypeKey());
 		int lastModuleId = lastModule.getID();
 
 
 		if (timesSelected==0&&firstTime==true){
 			firstTime=false;
 			this.selectOperations.addNewModuleOnConnector(this);
 			int amountModulesNEW = jmeSimulation.getModules().size();
 			lastModuleNEW = jmeSimulation.getModules().get(amountModulesNEW-1);
 			colorsComponents = lastModuleNEW.getColorList();
 			colorsConnectors = BuilderHelper.getColorsConnectors(lastModuleNEW);
 
 			lastModuleNEW.setColor(Color.GRAY);
 		}else if (timesSelected==this.construction.getConnectors().length){
 			resetTimesSelected();
 		}else if (selectedModuleId == lastModuleId){
 			firstTime=true;
 			resetTimesSelected();
 			lastModuleNEW.setColorList(colorsComponents);
 			BuilderHelper.setColorsConnectors(lastModuleNEW, colorsConnectors);
 			colorsComponents = null;
 		}else{				
 			if (selectedModuleType.contains("Odin")){
 				if (lastModuleType.equalsIgnoreCase(selectedModuleType)|| selectedModuleType.contains("Ball")==false){
 
 				}else{
 					this.construction.moveModuleAccording(timesSelected, selectedModule, lastModule, true);
 				}
 			}else{
 				this.construction.moveModuleAccording(timesSelected, selectedModule, lastModule, true);
 			}
 		}
 	}
 
 	/**
 	 * Calls back GUI in order to indicate new module addition in simulation environment.
 	 */
 	private void callBackNewModuleAdded(){
 		try {
 			RemotePhysicsSimulationImpl.getGUICallbackControl().newModuleAdded();
 		} catch (RemoteException e) {
 			throw new Error ("Failed to call back GUI in order to indicate new module addition, due to remote exception.");
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
