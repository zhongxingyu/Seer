 package ussr.aGui.tabs.controllers;
 
 import java.rmi.RemoteException;
 
 import javax.swing.AbstractButton;
 import javax.swing.JComboBox;
 import ussr.builder.enumerations.ATRONStandardRotations;
 import ussr.builder.enumerations.CKBotStandardRotations;
 import ussr.builder.enumerations.ConstructionTools;
 import ussr.builder.enumerations.MTRANStandardRotations;
 import ussr.builder.enumerations.SupportedModularRobots;
 import ussr.aGui.tabs.additionalResources.HintPanelInter;
 import ussr.aGui.tabs.constructionTabs.ConstructRobotTab;
 import ussr.aGui.tabs.constructionTabs.ConstructRobotTabInter;
 
 
 /**
  * Controls events of Construct Robot Tab  and underlying logic of simulator.
  * @author Konstantinas
  */
 public class ConstructRobotTabController extends TabsControllers implements ConstructRobotTabInter{
 
 	/**
 	 *  The name of modular robot chosen in GUI.
 	 */
 	private  static SupportedModularRobots chosenMRname;	
 
 	/**
 	 * Adds initial construction module according to selected module type and adapts the Tab to modular robot type.
 	 * @param button, button selected in the group of radio button.
 	 */
 	public static void jButtonGroupActionPerformed(AbstractButton button ) {
 
 		String chosenModularRobot = button.getText();			
 		chosenMRname = SupportedModularRobots.valueOf(chosenModularRobot.toUpperCase());
 
 		/*Adapt Tab components to chosen modular robot */
 		adaptTabToChosenMR(chosenMRname);
 
 		try {
 			/*Add initial construction module*/
 			builderControl.addInitialConstructionModule(chosenMRname);
 		} catch (RemoteException e1) {
 			throw new Error("Failed to add initial construction module due to remote exception");
 		}
 
 		try {
 			/*Set default construction tool to be "On selected  connector"*/
 			builderControl.setConstructionToolSpecPicker(ConstructionTools.NEW_MODULE_ON_SELECTED_CONNECTOR);
 		} catch (RemoteException e) {
 			throw new Error("Failed to initate picker called " + ConstructionTools.NEW_MODULE_ON_SELECTED_CONNECTOR + " , due to remote exception");
 		}
 
 		/*Informing user*/
 		ConstructRobotTab.getHintPanel().setText(HintPanelInter.builInHintsConstrucRobotTab[1]);
 	}
 
 
 	/**
 	 * Adapt the tab to chosen modular robot.
 	 * @param chosenMRname, modular robot name chosen in button group.
 	 */
 	private static void adaptTabToChosenMR(SupportedModularRobots chosenMRname){
 
 		/*Disable and enable components*/
 		ConstructRobotTab.setRadioButtonsEnabled(false);
 		ConstructRobotTab.getJComboBoxEntity().setEnabled(true);
 		ConstructRobotTab.setEnabledRotationToolBar(true);
 		ConstructRobotTab.setEnabledGenericToolBar(true);
 		ConstructRobotTab.setEnabledConstructionToolsToolBar(true);
 		ConstructRobotTab.setEnabledButtonsArrows(false);
 
 		/*Adapt tab to chosen modular robot*/
 		switch(chosenMRname){
 		case ATRON:
 			adaptTabToATRON();
 			break;
 		case MTRAN:
 			adaptTabToMTRAN();
 			break;
 		case ODIN:
 			adaptTabToOdin();
 			break;
 		case CKBOTSTANDARD:
 			adaptTabToCKBOTSTANDARD();
 			break;
 		}	
 	}
 
 	/**
 	 * Adapts tab to ATRON modular robot
 	 */
 	private static void adaptTabToATRON(){
 		ConstructRobotTab.getjComboBoxStandardRotations().setModel(new javax.swing.DefaultComboBoxModel(ATRONStandardRotations.values()));
 		ConstructRobotTab.getJComboBoxNrConnectorsConstructionTool().setModel(new javax.swing.DefaultComboBoxModel(ATRON_CONNECTORS));
 	}
 
 	/**
 	 * Adapts tab to MTRAN modular robot
 	 */
 	private static void adaptTabToMTRAN(){
 		ConstructRobotTab.getJButtonMove().setEnabled(false);
 		ConstructRobotTab.getjComboBoxStandardRotations().setModel(new javax.swing.DefaultComboBoxModel(MTRANStandardRotations.values()));
 		ConstructRobotTab.getJComboBoxNrConnectorsConstructionTool().setModel(new javax.swing.DefaultComboBoxModel(MTRAN_CONNECTORS));
 	}
 
 	/**
 	 * Adapts tab to Odin modular robot
 	 */
 	private static void adaptTabToOdin(){
 		ConstructRobotTab.setEnabledRotationToolBar(false);// for Odin not yet relevant
 		ConstructRobotTab.getJComboBoxNrConnectorsConstructionTool().setModel(new javax.swing.DefaultComboBoxModel(ODIN_CONNECTORS));
 	}
 
 	/**
 	 * Adapts tab to CKBOTSTANDARD modular robot
 	 */
 	private static void adaptTabToCKBOTSTANDARD(){
 		ConstructRobotTab.getjComboBoxStandardRotations().setModel(new javax.swing.DefaultComboBoxModel( CKBotStandardRotations.values() ));
 		ConstructRobotTab.getJComboBoxNrConnectorsConstructionTool().setModel(new javax.swing.DefaultComboBoxModel(CKBOT_CONNECTORS));
 	}
 
 	/**
 	 * Default chosen entity for operations on existing modules or robot. 
 	 */
 	private static String chosenItem ="Module" ;
 
 	/**
 	 * Initializes the tool for deleting module or robot with the mouse selecting.
 	 */
 	public static void jButtonDeleteActionPerformed() {
 		if (chosenItem.equalsIgnoreCase("Module")){
 			try {
 				builderControl.setRemoveModulePicker();
 			} catch (RemoteException e) {
 				throw new Error("Failed to initialize picker called Remove Module, due to remote exception");
 			}
 		}else if (chosenItem.equalsIgnoreCase("Robot")){
 			//TODO SUPPORT ROBOT DELETION
 		}
 		/*Informing user*/
 		ConstructRobotTab.getHintPanel().setText(HintPanelInter.builInHintsConstrucRobotTab[5]); 
 	}
 
 	/**
 	 * Initializes the tool for moving module or robot with the mouse.
 	 */
 	public static void jButtonMoveActionPerformed() {	
 		if (chosenItem.equalsIgnoreCase("Module")){
 			try {
 				builderControl.setMoveModulePicker();
 			} catch (RemoteException e) {
 				throw new Error("Failed to initialize picker called Move Module, due to remote exception");
 			}
 		}else if (chosenItem.equalsIgnoreCase("Robot")){
 			//TODO Support moving robot 
 		}
 		/*Informing user*/
 		ConstructRobotTab.getHintPanel().setText(HintPanelInter.builInHintsConstrucRobotTab[3]);//Informing user
 	}
 
 	/**
 	 * Initializes the tool for coloring  the connectors of the module or robot with color coding. 
 	 */
 	public static void jButtonColorConnectorsActionPerformed() {	
 		if (chosenItem.equalsIgnoreCase("Module")){
 			try {
 				builderControl.setColorModuleConnectorsPicker();
 			} catch (RemoteException e) {
 				throw new Error("Failed to initate picker called Color Module Connectors, due to remote exception");
 			}	
 		}else if (chosenItem.equalsIgnoreCase("Robot")){
 			//TODO  Support robot coloring of connectors.
 		}
 		/*Informing user*/
 		ConstructRobotTab.getHintPanel().setText(HintPanelInter.builInHintsConstrucRobotTab[6]);//Informing user
 
 	}
 
 	/**
 	 * Enables or disables the components in  toolbar because of missing implementation for robot.
 	 * TODO WILL CHANGE WITH ADDDING ROBOT SUPPORT.
 	 * @param jComboBoxEntity
 	 */
 	public static void jComboBoxEntityActionPerformed(JComboBox jComboBoxEntity) {
 		chosenItem = jComboBoxEntity.getSelectedItem().toString();
 		if (chosenItem.equalsIgnoreCase("Module") ){
 			ConstructRobotTab.setEnabledGenericToolBar(true);
 		}else if(chosenItem.equalsIgnoreCase("Robot")){
 			//TODO  Support robot deletion, moving and coloring of connectors.
 			ConstructRobotTab.setEnabledGenericToolBar(false);
 		}		
 	}
 
 	/**
 	 * Initializes the tool for rotating modules selected in simulation environment with opposite rotation. 
 	 * @param jmeSimulation, the physical simulation.     
 	 */	
 	public static void jButtonOppositeRotationActionPerformed() {
 		try {
 			builderControl.setConstructionToolSpecPicker(ConstructionTools.MODULE_OPPOSITE_ROTATION);
 		} catch (RemoteException e) {
 			throw new Error("Failed to initate picker called "+ ConstructionTools.MODULE_OPPOSITE_ROTATION.toString()+ " , due to remote exception");
 		}
 		/*Informing user*/
 		ConstructRobotTab.getHintPanel().setText(HintPanelInter.builInHintsConstrucRobotTab[2]);
 	}
 
 	/**
 	 * Standard rotation of the module chosen in GUI.
 	 */
 	public static String chosenStandardRotation;
 
 	/**
 	 * Initializes the tool for rotating initial module selected in simulation environment with standard rotations chosen in GUI. 
 	 * @param comboBoxStandardRotations, the GUI component.  
 	 */	
 	public static void jComboBoxStandardRotationsActionPerformed(JComboBox comboBoxStandardRotations) {
 		chosenStandardRotation = comboBoxStandardRotations.getSelectedItem().toString(); 
 		try {
 			builderControl.setConstructionToolSpecPicker(ConstructionTools.STANDARD_ROTATIONS, chosenStandardRotation);
 		} catch (RemoteException e) {
 			throw new Error("Failed to initate picker called "+ ConstructionTools.STANDARD_ROTATIONS.toString()+ ", due to remote exception");
 		}
 		/*Informing user*/
 		ConstructRobotTab.getHintPanel().setText(HintPanelInter.builInHintsConstrucRobotTab[2]);
 	}
 
 	/**
 	 * Initializes the tool for adding new modules on selected connectors of the module in interest in simulation environment. 	
 	 */
 	public static void jButtonOnSelectedConnectorActionPerformed() {
 		/*Disable tab components no longer available*/
 		ConstructRobotTab.setEnabledRotationToolBar(false);
 		ConstructRobotTab.getJButtonMove().setEnabled(false);
 
 		try {
 			builderControl.setConstructionToolSpecPicker(ConstructionTools.NEW_MODULE_ON_SELECTED_CONNECTOR);
 		} catch (RemoteException e) {
 			throw new Error("Failed to initate picker called " + ConstructionTools.NEW_MODULE_ON_SELECTED_CONNECTOR.toString() + " , due to remote exception");
 		}		
 
 		/*Informing user*/
 		ConstructRobotTab.getHintPanel().setText(HintPanelInter.builInHintsConstrucRobotTab[7]); 
 	}
 
 	/**
 	 * Connector number chosen by user in GUI.
 	 */
 	private static int chosenConnectorNr;
 
 	/**
 	 * Initializes the tool for adding new modules on connector chosen in combo box(GUI) by user. Later user selects the module to apply it to.
 	 * @param comboBoxNrConnectorsConstructionTool, JComboBox containing the number of connectors.
 	 */
 	public static void jComboBoxNrConnectorsConstructionToolActionPerformed(JComboBox comboBoxNrConnectorsConstructionTool) {
 		/*Disable tab components no longer available*/
 		ConstructRobotTab.setEnabledRotationToolBar(false);
 		ConstructRobotTab.getJButtonMove().setEnabled(false);
 
 		chosenConnectorNr = Integer.parseInt(comboBoxNrConnectorsConstructionTool.getSelectedItem().toString());
 		try {
 			builderControl.setConstructionToolSpecPicker(ConstructionTools.ON_CHOSEN_CONNECTOR_NR,chosenConnectorNr);
 		} catch (RemoteException e) {
 			throw new Error("Failed to initate picker called "+ ConstructionTools.ON_CHOSEN_CONNECTOR_NR.toString()+ ", due to remote exception");
 		}
 
 		/*Informing user*/
 		ConstructRobotTab.getHintPanel().setText(HintPanelInter.builInHintsConstrucRobotTab[8]); 
 	}
 
 	/**
 	 * Initializes the tool for adding new modules to all connectors of selected module.
 	 */
 	public static void jButtonConnectAllModulesActionPerformed() {
 		/*Disable tab components no longer available*/
 		ConstructRobotTab.setEnabledRotationToolBar(false);
 		ConstructRobotTab.getJButtonMove().setEnabled(false);
 
 		try {
 			builderControl.setConstructionToolSpecPicker(ConstructionTools.NEW_MODULES_ON_ALL_CONNECTORS);
 		} catch (RemoteException e) {
 			throw new Error("Failed to initate picker called "+ ConstructionTools.NEW_MODULES_ON_ALL_CONNECTORS.toString() + ", due to remote exception");
 		}
 		/*Informing user*/
 		ConstructRobotTab.getHintPanel().setText(HintPanelInter.builInHintsConstrucRobotTab[9]);
 
 	}
 
 	/**
 	 * Used to keep track on which connector number the module is positioned. 
 	 */
 	private static int  connectorNr =0;
 
 	/**
 	 * Initializes the tool for moving new module from connector to another connector.
 	 */
 	public static void jButtonJumpFromConnToConnectorActionPerformed() {
 		/*Disable tab components no longer available*/
 		ConstructRobotTab.setEnabledRotationToolBar(false);
 		ConstructRobotTab.getJButtonMove().setEnabled(false);		
 
 		try {
			builderControl.setConstructionToolSpecPicker(ConstructionTools.MOVE_MODULE_FROM_CON_TO_CON,0);
 		} catch (RemoteException e) {
 			throw  new Error ("Failed to initialize picker named as "+ ConstructionTools.MOVE_MODULE_FROM_CON_TO_CON.toString()+ " ,due to remote exception");
 		}
 		/*Informing user*/
 		ConstructRobotTab.getHintPanel().setText(HintPanelInter.builInHintsConstrucRobotTab[10]); 
 	}
 
 	/**
 	 * Moves new module from connector to another connector with increasing number of connector.
 	 */
 	public static void jButtonOnNextConnectorActionPerformed() {
 
 		ConstructRobotTabController.connectorNr++;
 
 		switch(chosenMRname){
 		case ATRON:
 			if (ConstructRobotTabController.connectorNr>=ATRON_CONNECTORS.length){ ConstructRobotTabController.connectorNr=0;} //reset to zero      
 			break;
 		case MTRAN:
 			if (ConstructRobotTabController.connectorNr>=MTRAN_CONNECTORS.length){ConstructRobotTabController.connectorNr=0;}
 			break;
 		case ODIN:
 			if (ConstructRobotTabController.connectorNr>=12){ connectorNr=0;}// OdinBall
 			break;
 		case CKBOTSTANDARD:
 			if (ConstructRobotTabController.connectorNr>=CKBOT_CONNECTORS.length){ ConstructRobotTabController.connectorNr=0;}
 			break;			
 		default: throw  new Error ("Modular robot with name "+ chosenMRname+ " is not supported yet");
 		}
 
 		try {
 			if (builderControl.getModuleCountingFromEndType(1).equalsIgnoreCase("OdinBall")){
 				//do nothing
 			}else{
				builderControl.moveToNextConnector(chosenMRname, ConstructRobotTabController.connectorNr, selectedModuleID);
 			}
 		} catch (RemoteException e) {
 			throw  new Error ("Failed to move module on next connector, due to remote exception.");
 		}
 		//TODO CHECK IF THE MODULE IS ALREADY ON CONNECTOR AND THEN DO NOT PLACE NEW ONE THERE.		
 	}
 
 	/**
 	 * Moves new module from connector to another connector with decreasing number of connector.
 	 */
 	public static void jButtonOnPreviousConnectorActionPerformed() {
 		ConstructRobotTabController.connectorNr--;
 
 		if (ConstructRobotTabController.connectorNr<0){
 			switch(chosenMRname){
 			case ATRON:			
 				ConstructRobotTabController.connectorNr =ATRON_CONNECTORS.length-1;//reset			
 				break;
 			case MTRAN:			
 				ConstructRobotTabController.connectorNr=MTRAN_CONNECTORS.length-1;//reset			
 				break;
 			case ODIN:
 				ConstructRobotTabController.connectorNr =11;//reset	
 				break;
 			case CKBOTSTANDARD:
 				ConstructRobotTabController.connectorNr= CKBOT_CONNECTORS.length-1;//reset	
 				break;
 			}
 		}
 		try {
 			if (builderControl.getModuleCountingFromEndType(1).equalsIgnoreCase("OdinBall")){
 				//do nothing
 			}else{
				builderControl.moveToNextConnector(chosenMRname, ConstructRobotTabController.connectorNr, selectedModuleID);
 			}
 		} catch (RemoteException e) {
 			throw  new Error ("Failed to move module on previous connector, due to remote exception.");
 		}
 	}
 
 	/**
 	 * Adapts Construct Robot Tab to the the type modular robot is simulation environment.
 	 * Called from ConstructionToolSpecification.
 	 * @param supportedModularRobot, supported modular robot.
 	 */
 	public static void adjustTabToSelectedModule(SupportedModularRobots supportedModularRobot){
 		ConstructRobotTab.setRadioButtonsEnabled(true);
 
 		switch(supportedModularRobot){
 		case ATRON:
 			ConstructRobotTab.getRadionButtonATRON().setSelected(true);	
 			break;
 		case ODIN:
 			ConstructRobotTab.getRadionButtonODIN().setSelected(true);
 			break;
 		case MTRAN:
 			ConstructRobotTab.getRadioButtonMTRAN().setSelected(true);
 			break;
 		case CKBOTSTANDARD:
 			ConstructRobotTab.getRadionButtonCKBOTSTANDARD().setSelected(true);
 			break;
 		default: throw new Error("Modular robot named as "+ supportedModularRobot.toString() + "is not supported yet" );
 		}	
 		adaptTabToChosenMR(supportedModularRobot);
 		ConstructRobotTab.setRadioButtonsEnabled(false);
 	}
 
 	/**
 	 * Adapts Construct Robot Tab to the the type of first module in simulation environment.
 	 */
 	public static void adaptTabToModuleInSimulation(){
 		int amountModules =0;		
 		try {
 			amountModules =  builderControl.getIDsModules().size();
 		} catch (RemoteException e) {
 			throw new Error("Failed to identify amount of modules in simulation environment, due to remote exception");
 		}
 
 		if (amountModules>2){
 			/*Adapt to first module type*/
 			String modularRobotName ="";
 			try {
 				modularRobotName = builderControl.getModuleType(0);
 			} catch (RemoteException e) {
 				throw new Error ("Failed to identify the type of the first module in simulation environment, due to remote exception.");
 			}
 
 			if (modularRobotName.toUpperCase().contains(SupportedModularRobots.ATRON.toString())){
 				adaptTabToChosenMR(SupportedModularRobots.ATRON);
 				chosenMRname = SupportedModularRobots.ATRON;
 			} else if (modularRobotName.toUpperCase().contains(SupportedModularRobots.ODIN.toString())){
 				adaptTabToChosenMR(SupportedModularRobots.ODIN);
 				chosenMRname = SupportedModularRobots.ODIN;
 			} else if (modularRobotName.toUpperCase().contains(SupportedModularRobots.MTRAN.toString())){
 				adaptTabToChosenMR(SupportedModularRobots.MTRAN);
 				chosenMRname = SupportedModularRobots.MTRAN;
 			}else if(modularRobotName.toUpperCase().contains(SupportedModularRobots.CKBOTSTANDARD.toString())){
 				adaptTabToChosenMR(SupportedModularRobots.CKBOTSTANDARD);
 				chosenMRname = SupportedModularRobots.CKBOTSTANDARD;
 			}else{
 				throw new Error ("Modular robot type "+modularRobotName+ "is not supported yet" );
 			}
 			ConstructRobotTab.setEnabledRotationToolBar(false);
			ConstructRobotTab.getJButtonMove().setEnabled(false);			
 
 			try {
 				/*Set default construction tool to be "On selected  connector"*/
 				builderControl.setConstructionToolSpecPicker(ConstructionTools.NEW_MODULE_ON_SELECTED_CONNECTOR);
 			} catch (RemoteException e) {
 				throw new Error("Failed to initate picker called " + ConstructionTools.NEW_MODULE_ON_SELECTED_CONNECTOR + " , due to remote exception");
 			}
 		}
 
 	}	
 
 	/**
 	 * Removes current robots in simulation environment and enables tab elements for constructing new robot.
 	 */
 	public static void jButtonStartNewRobotActionPerformed() {
 		ConstructRobotTab.setRadioButtonsEnabled(true);
 		ConstructRobotTab.setEnabledAllToolBars(false);		
 
 		//TODO SOMETIMES FAILS WHY?
 		try {
 			builderControl.removeAllModules();
 		} catch (RemoteException e) {
 			throw new Error("Failed to to remove all modules, due to remote exception");
 		}		
 
 		/*Informing user*/
 		ConstructRobotTab.getHintPanel().setText(HintPanelInter.builInHintsConstrucRobotTab[11]);
 	}
 
 	/**
 	 * Initializes the tool for varying the properties of modules (or types of modules in Odin case) selected in simulation environment.
 	 * Is specific to each modular robot.
 	 */
 	public static void jButtonVariateModulePropertiesActionPerformed() {	
 		try {
 			builderControl.setConstructionToolSpecPicker(ConstructionTools.VARIATE_MODULE_OR_PROPERTIES);
 		} catch (RemoteException e) {
 			throw new Error("Failed to initate picker called "+ ConstructionTools.VARIATE_MODULE_OR_PROPERTIES.toString() + ", due to remote exception");
 		}				
 	}
 
 	/**
 	 * Initializes the tool for changing module rotation with each selection of module in simulation environment.
 	 */
 	public static void jButtonStandardRotationsLoopActionPerformed() {
 		try {
 			builderControl.setConstructionToolSpecPicker(ConstructionTools.AVAILABLE_ROTATIONS);
 		} catch (RemoteException e) {
 			throw new Error("Failed to initate picker called"+ConstructionTools.AVAILABLE_ROTATIONS.toString() +", due to remote exception");
 		}
 	}
 
 	/**
 	 * Adapts Construct Robot tab to the tool chosen by user. To be more precise disables and enables relevant components of the tab. 
 	 * @param chosenTool,the tool chosen by the user in Construct Robot tab.
 	 */
 	public static void adaptConstructRobotTabToChosenTool(ConstructionTools chosenTool)throws RemoteException{
 
 		switch(chosenTool){
 
 		case MOVE_MODULE_FROM_CON_TO_CON:
 			ConstructRobotTab.setEnabledButtonsArrows(true);	
 			break;
 		case NEW_MODULE_ON_SELECTED_CONNECTOR:
 			ConstructRobotTab.setEnabledRotationToolBar(false);
 			ConstructRobotTab.getJButtonMove().setEnabled(false);
 			break;			
 		default: throw new Error ("The tool named as "+ chosenTool.toString() + " is not supported yet.");
 		}
	}
	
	/**
	 * The global ID of the module selected in simulation environment.
	 */
	private static int selectedModuleID;
	
	/**
	 * Sets the global ID of the module selected in simulation environment.
	 * @param selectedModuleID, the global ID of the module selected in simulation environment.
	 */
	public static void setSelectedModuleID(int selectedModuleID){
		ConstructRobotTabController.selectedModuleID = selectedModuleID;
	}
 }
