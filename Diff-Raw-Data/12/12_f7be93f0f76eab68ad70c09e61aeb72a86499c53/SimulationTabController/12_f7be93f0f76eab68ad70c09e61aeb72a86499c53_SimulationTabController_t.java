 package ussr.aGui.tabs.controllers;
 
 import java.awt.GridBagConstraints;
 import java.awt.Insets;
 import java.rmi.RemoteException;
 
 import javax.swing.Icon;
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JComboBox;
 import javax.swing.JLabel;
 import javax.swing.JSpinner;
 import javax.swing.tree.DefaultMutableTreeNode;
 import javax.swing.tree.DefaultTreeModel;
 
 import ussr.aGui.designHelpers.JComponentsFactory;
 import ussr.aGui.enumerations.tabs.TabsIcons;
 import ussr.aGui.helpers.hintPanel.HintPanelTypes;
 import ussr.aGui.tabs.simulation.SimulationTab;
 import ussr.aGui.tabs.simulation.SimulationTabTreeNodes;
 import ussr.aGui.tabs.simulation.SimulationTreeEditors;
 import ussr.aGui.tabs.simulation.enumerations.PlaneMaterials;
 import ussr.aGui.tabs.simulation.enumerations.TextureDescriptions;
 import ussr.builder.simulationLoader.SimulationSpecification;
 import ussr.description.geometry.VectorDescription;
 import ussr.physics.PhysicsParameters;
 
 
 
 public class SimulationTabController extends TabsControllers {
 
 	/**
 	 * The name of the node selected in the simulation tree by user.
 	 */
 	private static String selectedNodeName;
 
 	/**
 	 * The robot number selected in the simulation tree by user.
 	 */
 	private static int selectedRobotNr;
 
 	/**
 	 * The object describing simulation parameters and robots in it.
 	 */
 	private static SimulationSpecification simulationSpecification;
 
 	/**
 	 * Updates the panel for editing values of simulation tree node, according to  the node selected by user.
 	 * @param nameSelectedNode, the name of the node selected in the simulation tree.
 	 */
 	public static void jTreeItemSelectedActionPerformed(String nameSelectedNode) {
 		selectedNodeName = nameSelectedNode;
 
 		SimulationTab.getJPanelEditor().removeAll();
 		SimulationTab.getJPanelEditor().revalidate();
 		SimulationTab.getJPanelEditor().repaint();	
 
 
 		GridBagConstraints gridBagConstraints = new GridBagConstraints();
 		gridBagConstraints.fill = GridBagConstraints.CENTER;
 		gridBagConstraints.gridx =0;
 		gridBagConstraints.gridy =0;
 		gridBagConstraints.insets = new Insets(0,0,10,0);
 
 		if (nameSelectedNode.contains("Robot nr.")){			
 
 			SimulationTab.getJPanelEditor().add(JComponentsFactory.createNewLabel(nameSelectedNode),gridBagConstraints);
 
 			gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
 			gridBagConstraints.gridx =0;
 			gridBagConstraints.gridy =1;
 
 			SimulationTab.getJPanelEditor().add(SimulationTreeEditors.addRobotEditor(),gridBagConstraints);
 
 			int robotNr = extractRobotNumber(nameSelectedNode);	
 			selectedRobotNr = robotNr;
 
 			SimulationTreeEditors.getJTextFieldMorphologyLocation().setText(simulationSpecification.getRobotsInSimulation().get(robotNr-1).getMorphologyLocation());
 
 
 		}else{
 			SimulationTabTreeNodes treeNode = SimulationTabTreeNodes.valueOf(nameSelectedNode.replace(" ", "_").toUpperCase());
 
 			SimulationTab.getJPanelEditor().add(JComponentsFactory.createNewLabel(treeNode.getUserFriendlyName()),gridBagConstraints);
 			if (treeNode.getJPanelEditor()==null){
 				// do nothing
 			}else{	
 				gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
 				gridBagConstraints.gridx =0;
 				gridBagConstraints.gridy =1;
 				SimulationTab.getJPanelEditor().add(treeNode.getJPanelEditor(),gridBagConstraints);
 
 				SimulationTab.getHintPanel().setType(HintPanelTypes.INFORMATION);
 				SimulationTab.getHintPanel().setText(" ");
 				SimulationTab.getHintPanel().setText(treeNode.getHintSimulationTab().getHintText());
 			}
 		}
 		SimulationTab.getJPanelEditor().validate();
 	}
 
 
 	/**
 	 * Extracts robot number from the name of selected robot node. For example: "Robot nr.1"
 	 * @param nameSelectedNode, the name of the node selected in the simulation tree.
 	 * @return robot number, in the example above it is 1.
 	 */
 	private static int extractRobotNumber(String nameSelectedNode){
 
 		int stringLenght = nameSelectedNode.toCharArray().length;
 		int selectedRobotNr=-1;
 
 		switch(stringLenght){
 		case 10:// up to 9 robots
 			char robotNumber = nameSelectedNode.toCharArray()[9];
 			selectedRobotNr = Integer.parseInt(robotNumber+"");
 			break;
 		case 11:// up to 99 robots (NOT TESTED YET)
 			char robotNr1 = nameSelectedNode.toCharArray()[9];
 			char robotNr2 = nameSelectedNode.toCharArray()[10];
 			selectedRobotNr = Integer.parseInt((robotNr1+"")+(robotNr2+"")+"");
 			break;
 		default: throw new Error("Robot title changed in tree structure or missing support.The robot title should be for instance: Robot Nr.1");
 		}
 		if (selectedRobotNr ==-1){
 			throw new Error("Inconsistentcy in numeration of robots");
 		}
 		return selectedRobotNr;
 	}
 
 	/**
 	 * Sets the value of plane size in the spinner component of edit panel, according to the value of remote simulation parameter..
 	 * @param spinnerPlaneSize, the component in the edit value panel.
 	 */
 	public static void setJSpinnerPlaneSizeValue(JSpinner spinnerPlaneSize) {
 		try {
 			spinnerPlaneSize.setValue(remotePhysicsSimulation.getWorldDescriptionControl().getPlaneSize());
 		} catch (RemoteException e) {
 			throw new Error("Failed to extract plane size, due to remote exception.");
 		}		
 	}
 
 	/**
 	 * Sets selected the plane texture and displays it in the panel edit value, according to the value of remote simulation parameter..
 	 * @param jComboBoxPlaneTexture, the component in the edit value panel.
 	 * @param iconLabel,the component in the edit value panel.
 	 */
 	public static void setSelectedJComboBoxPlaneTexture(JComboBox jComboBoxPlaneTexture, JLabel iconLabel){
 		String fileName;
 		try {
 			fileName = remotePhysicsSimulation.getWorldDescriptionControl().getPlaneTextureFileName();
 		} catch (RemoteException e) {
 			throw new Error("Failed to extract plane texture file name, due to remote exception.");
 		}	
 
 		for (int textureNr=0;textureNr<TextureDescriptions.values().length;textureNr++){
 			if (fileName.equals(TextureDescriptions.values()[textureNr].getRawFileDirectoryName())){
 				jComboBoxPlaneTexture.setSelectedItem(TextureDescriptions.values()[textureNr].getUserFriendlyName());
 				iconLabel.setIcon(TextureDescriptions.values()[textureNr].getImageIcon());
 				iconLabel.setSize(100, 100);
 			}
 		}
 	}
 
 	/**
 	 * Sets the camera position in the panel edit value, according to the value of remote simulation parameter.
 	 * @param comboBoxCameraPosition,the component in the edit value panel. 
 	 */
 	public static void setSelectedJComboBoxCameraPosition(JComboBox comboBoxCameraPosition) {
 		try {
 			comboBoxCameraPosition.setSelectedItem(remotePhysicsSimulation.getWorldDescriptionControl().getCameraPosition());
 		} catch (RemoteException e) {
 			throw new Error("Failed to extract camera position, due to remote exception.");
 		}
 
 	}
 
 	/**
 	 * Sets (de)/selected check box in the the panel edit value, according to the value of remote simulation parameter. 
 	 * @param checkBoxTheWorldIsFlat,the component in the edit value panel.
 	 */
 	public static void setSelectedJCheckBoxTheWorldIsFlat(JCheckBox checkBoxTheWorldIsFlat) {
 		try {
 			checkBoxTheWorldIsFlat.setSelected(remotePhysicsSimulation.getWorldDescriptionControl().theWorldIsFlat());
 		} catch (RemoteException e) {
 			throw new Error("Failed to extract the world is flat, due to remote exception.");
 		}
 
 	}
 
 	/**
 	 * Sets (de)/selected check box in the the panel edit value, according to the value of remote simulation parameter. 
 	 * @param checkBoxHasBackgroundScenery,the component in the edit value panel.
 	 */
 	public static void setSelectedJCheckBoxHasBackgroundScenery(JCheckBox checkBoxHasBackgroundScenery) {
 		try {
 			checkBoxHasBackgroundScenery.setSelected(remotePhysicsSimulation.getWorldDescriptionControl().hasBackgroundScenery());
 		} catch (RemoteException e) {
 			throw new Error("Failed to extract has background scenery, due to remote exception.");
 		}
 
 	}
 
 	/**
 	 * Sets (de)/selected check box in the the panel edit value, according to the value of remote simulation parameter. 
 	 * @param checkBoxHasHeavyObstacles,the component in the edit value panel.
 	 */
 	public static void setSelectedjCheckBoxHasHeavyObstacles(JCheckBox checkBoxHasHeavyObstacles) {
 		try {
 			checkBoxHasHeavyObstacles.setSelected(remotePhysicsSimulation.getWorldDescriptionControl().hasHeavyObstacles());
 		} catch (RemoteException e) {
 			throw new Error("Failed to extract has heavy obstacles, due to remote exception.");
 		}
 
 	}
 
 	/**
 	 * Sets (de)/selected check box in the the panel edit value, according to the value of remote simulation parameter. 
 	 * @param checkBoxIsFrameGrabbingActive,the component in the edit value panel.
 	 */
 	public static void setSelectedJCheckBoxIsFrameGrabbingActive(JCheckBox checkBoxIsFrameGrabbingActive) {
 		try {
 			checkBoxIsFrameGrabbingActive.setSelected(remotePhysicsSimulation.getWorldDescriptionControl().getIsFrameGrabbingActive());
 		} catch (RemoteException e) {
 			throw new Error("Failed to extract is frame grabbing active, due to remote exception.");
 		}
 	}
 
 	/**
 	 * Sets the value of damping linear velocity in the component of edit value panel, according to the value of remote simulation parameter. 
 	 * @param spinnerDampingLinearVelocity,the component in the edit value panel.
 	 */
 	public static void setValuejSpinnerDampingLinearVelocity(JSpinner spinnerDampingLinearVelocity) {
 		spinnerDampingLinearVelocity.setValue(simulationSpecification.getConverter().convertWorldDamping(true));
 	}
 
 	/**
 	 * Sets the value of damping angular velocity in the component of edit value panel,according to the value of remote simulation parameter. 
 	 * @param spinnerDampingAngularVelocity,the component in the edit value panel.
 	 */
 	public static void setValuejSpinnerDampingAngularVelocity(JSpinner spinnerDampingAngularVelocity) {		
 		spinnerDampingAngularVelocity.setValue(simulationSpecification.getConverter().convertWorldDamping(false));
 	}
 
 	/**
 	 * Sets the value of physics simulation step size in the component of edit value panel,according to the value of remote simulation parameter. 
 	 * @param spinnerPhysicsSimulationStepSize,the component in the edit value panel.
 	 */
 	public static void setValuejSpinnerPhysicsSimulationStepSize(JSpinner spinnerPhysicsSimulationStepSize) {
 		spinnerPhysicsSimulationStepSize.setValue(simulationSpecification.getConverter().convertPhysicsSimulationStepSize());
 	}
 
 	/**
 	 * Sets de/selected the value of realistic collision in the component of edit value panel,according to the value of remote simulation parameter. 
 	 * @param checkBoxRealisticCollision, the component in the edit value panel.
 	 */
 	public static void setSelectedJCheckBoxRealisticCollision(JCheckBox checkBoxRealisticCollision) {
 		checkBoxRealisticCollision.setSelected(simulationSpecification.getConverter().covertRealisticCollision());
 	}
 
 	/**
 	 * Sets the value of gravity in the component of edit value panel,according to the value of remote simulation parameter.
 	 * @param spinnerGravity, the component in the edit value panel.
 	 */
 	public static void setValuejSpinnerGravity(JSpinner spinnerGravity) {
 		spinnerGravity.setValue(simulationSpecification.getConverter().covertGravity());
 	}
 
 	/**
 	 * Sets the value of constraint for mix in the component of edit value panel,according to the value of remote simulation parameter.
 	 * @param spinnerConstraintForceMix,the component in the edit value panel.
 	 */
 	public static void setValuejSpinnerConstraintForceMix(JSpinner spinnerConstraintForceMix) {
 		spinnerConstraintForceMix.setValue(simulationSpecification.getConverter().convertConstraintForceMix());
 	}
 
 	/**
 	 * Sets the value of error reduction parameter in the component of edit value panel,according to the value of remote simulation parameter.
 	 * @param spinnerErrorReductionParameter,the component in the edit value panel.
 	 */
 	public static void setValueJSpinnerErrorReductionParameter(JSpinner spinnerErrorReductionParameter) {
 		spinnerErrorReductionParameter.setValue(simulationSpecification.getConverter().convertErrorReductionParameter());
 	}
 
 	/**
 	 * Sets the value of resolution parameter in the component of edit value panel,according to the value of remote simulation parameter.
 	 * @param spinnerResolutionFactor,the component in the edit value panel.
 	 */
 	public static void setValueJSpinnerResolutionFactor(JSpinner spinnerResolutionFactor) {
 		spinnerResolutionFactor.setValue(simulationSpecification.getConverter().convertResolutionFactor());
 	}
 
 	/**
 	 * Sets the value of use module event queue parameter in the component of edit value panel,according to the value of remote simulation parameter.
 	 * @param checkBoxUseMouseEventQueue,the component in the edit value panel.
 	 */
 	public static void setSelectedJCheckBoxUseMouseEventQueue(JCheckBox checkBoxUseMouseEventQueue) {
 		checkBoxUseMouseEventQueue.setSelected(simulationSpecification.getConverter().convertUseModuleEventQueue());
 	}
 
 	/**
 	 * Sets the value of parameter(synchronize with controllers) in the component of edit value panel,according to the value of remote simulation parameter.
 	 * @param checkBoxSynchronizeWithControllers,the component in the edit value panel.
 	 */
 	public static void setSelectedjCheckBoxSynchronizeWithControllers(JCheckBox checkBoxSynchronizeWithControllers) {
 		checkBoxSynchronizeWithControllers.setSelected(simulationSpecification.getConverter().convertSyncWithControllers());
 	}
 
 	/**
 	 * Sets the value of physics simulation controller step factor in the component of edit value panel,according to the value of remote simulation parameter.
 	 * @param physicsSimulationControllerStepFactor,the component in the edit value panel.
 	 */
 	public static void setValuejPhysicsSimulationControllerStepFactor(JSpinner physicsSimulationControllerStepFactor) {
 		physicsSimulationControllerStepFactor.setValue(simulationSpecification.getConverter().convertPhysicsSimulationControllerStepFactor());
 	}
 	
 	/**
 	 * Sets the value of plane material in the component of edit value panel,according to the value of remote simulation parameter.
 	 * @param comboBoxPlaneMaterial,the component in the edit value panel.
 	 */
 	public static void setValuejComboBoxPlaneMaterial(JComboBox comboBoxPlaneMaterial) {		
 		comboBoxPlaneMaterial.setSelectedItem(PlaneMaterials.valueOf(simulationSpecification.getConverter().covertPlaneMaterial().toString()).getUserFriendlyName());
 	}
 
 	/**
 	 * Sets the value of parameter maintain rotational joint positions in the component of edit value panel,according to the value of remote simulation parameter.
 	 * @param checkBoxMaintainRotJointPositions,the component in the edit value panel.
 	 */
 	public static void setSelectedjCheckBoxMaintainRotJointPositions(JCheckBox checkBoxMaintainRotJointPositions) {
 		checkBoxMaintainRotJointPositions.setSelected(PhysicsParameters.get().getMaintainRotationalJointPositions());
 	}
 
 	/**
 	 * Executes movement of modular robot, according to which button of coordinate system is selected.
 	 * @param jButton, the button of coordinate system(in panel edit value) is selected.
 	 */
 	public static void jButtonsCoordinateArrowsActionPerformed(JButton jButton) {
 
 		VectorDescription changeInPosition = new VectorDescription(0,0,0);
 
 		Icon icon = jButton.getIcon();
 		if(icon.equals(TabsIcons.Y_POSITIVE_BIG.getImageIcon())){			
 			changeInPosition = new VectorDescription(0,jSpinnerCoordinateStepValue,0);
 		}else if(icon.equals(TabsIcons.Y_NEGATIVE_BIG.getImageIcon())){
 			changeInPosition = new VectorDescription(0,-jSpinnerCoordinateStepValue,0);			
 		}else if (icon.equals(TabsIcons.X_POSITIVE_BIG.getImageIcon())){
 			changeInPosition = new VectorDescription(jSpinnerCoordinateStepValue,0,0);
 		}else if (icon.equals(TabsIcons.X_NEGATIVE_BIG.getImageIcon())){
 			changeInPosition = new VectorDescription(-jSpinnerCoordinateStepValue,0,0);
 		}else if (icon.equals(TabsIcons.Z_POSITIVE_BIG.getImageIcon())){
 			changeInPosition = new VectorDescription(0,0,jSpinnerCoordinateStepValue);
 		}else if (icon.equals(TabsIcons.Z_NEGATIVE_BIG.getImageIcon())){
 			changeInPosition = new VectorDescription(0,0,-jSpinnerCoordinateStepValue);
 		}
 
 		int robotNr = extractRobotNumber(selectedNodeName);		
 
 		int amountModules = simulationSpecification.getRobotsInSimulation().get(robotNr-1).getAmountModules();
 		int firstModuleId = simulationSpecification.getRobotsInSimulation().get(robotNr-1).getIdsModules().get(0);
 		System.out.println("First: "+ firstModuleId);
 		System.out.println("Modules: "+ amountModules);
	/*	if(robotNr==1){
 		moveRobot(0,amountModules,changeInPosition);
		}else{*/
 			
 			moveRobot(firstModuleId,firstModuleId+amountModules,changeInPosition);
		//}
 
 	/*	if (robotNr==1){			
 			moveRobot(0,amountFirstRobotModules,changeInPosition); 
 		}else{
 			int amountAllRobotModules=0;
 
 			while(robotNr!= 0){
 
 				amountAllRobotModules =amountAllRobotModules+ simulationSpecification.getRobotsInSimulation().get(robotNr-1).getAmountModules();
 				robotNr--;
 			}
 			moveRobot(amountAllRobotModules-amountFirstRobotModules,amountAllRobotModules,changeInPosition);
 		}*/
 	}
 	
 	/**
 	 * The step for moving modular robot along each coordinate axis.
 	 */
 	private static float jSpinnerCoordinateStepValue=0.1f; 
 
 	/**
 	 * Sets the step for moving modular robot along each coordinate axis.
 	 * @param spinnerStepValue, the step for moving modular robot along each coordinate axis.
 	 */
 	public static void setjSpinnerCoordinateValue(float spinnerStepValue) {
 		jSpinnerCoordinateStepValue = spinnerStepValue;
 	}
 	
 	/**
 	 * Moves the robot to new position depending on the change in position.
 	 * @param firstModuleId, the id of first module of modular robot.
 	 * @param amountModules, amount of modules constituting modular robot.
 	 * @param changeInPosition, the change in position.
 	 */
 	private static void moveRobot(int firstModuleId,int amountModules,VectorDescription changeInPosition){
 		for (int moduleID=firstModuleId; moduleID<amountModules;moduleID++){
 			try {
 				VectorDescription modulePosition = remotePhysicsSimulation.getSimulationTabControl().getModulePosition(moduleID);	
 				remotePhysicsSimulation.getSimulationTabControl().setModulePosition(moduleID, new VectorDescription(modulePosition.getX()+changeInPosition.getX(),modulePosition.getY()+changeInPosition.getY(),modulePosition.getZ()+changeInPosition.getZ()));
 			} catch (RemoteException e) {
 				throw new Error("Failed to move modular robot in new position, due to remote exception.");
 			}
 		}
 	}
 
 	/**
 	 * Displays the image of texture selected in comboBox.
 	 * @param comboBoxPlaneTexture,the component in the edit value panel.
 	 * @param iconLabel,the component in the edit value panel.
 	 */
 	public static void jComboBoxPlaneTextureActionPerformed(JComboBox comboBoxPlaneTexture, JLabel iconLabel) {
 		String selectedTexture = TextureDescriptions.toJavaUSSRConvention(comboBoxPlaneTexture.getSelectedItem().toString());
 		iconLabel.setIcon(TextureDescriptions.valueOf(selectedTexture).getImageIcon());
 	}
 
 	public static void jButtonDeleteRobotActionPerformed() {
 
 		DefaultTreeModel model = (DefaultTreeModel)SimulationTab.getJTreeSimulation().getModel();
 		DefaultMutableTreeNode robotsNode = (DefaultMutableTreeNode) model.getChild(model.getRoot(),2);
 		
 		System.out.println("Selected Robot nr."+ selectedRobotNr);
         		
 		for (int index=0;index<simulationSpecification.getRobotsInSimulation().size();index++){
 			System.out.println("Size"+index + " :" +simulationSpecification.getRobotsInSimulation().get(index).getIdsModules().size() );
 		}
 		
 		System.out.println("ID:"+ simulationSpecification.getRobotsInSimulation().get(selectedRobotNr-1).getIdsModules().get(0));
 		try {
 			remotePhysicsSimulation.getSimulationTabControl().deleteModules(simulationSpecification.getRobotsInSimulation().get(selectedRobotNr-1).getIdsModules());
 		} catch (RemoteException e) {
 			throw new Error("Some");
 		}
 		simulationSpecification.getRobotsInSimulation().remove(selectedRobotNr-1);
 		
 
 		robotsNode.removeAllChildren();
 		//SimulationTab.setRobotNumber(0);//reset
 		SimulationSpecification spec = simulationSpecification;
 		SimulationTab.addRobotNodes(spec, false,false);
		
 		model.reload();
		SimulationTab.jTreeSimulationExpandAllNodes();
 		
 		//robotsNode.remove(selectedRobotNr-1);
 		
 		
 		
 		
 		/*model.reload();
 		SimulationTab.jTreeSimulationExpandAllNodes();
 
 		for (int childNr=0;childNr<robotsNode.getChildCount();childNr++){
 			DefaultMutableTreeNode currentChild = (DefaultMutableTreeNode)robotsNode.getChildAt(childNr);
 			//STOPPED HERE FIND THE NAME OF THE CHILD 
 			//System.out.println("Path:"+currentChild.);
 		}*/
 
 	}
 
 	/**
 	 * Returns the object, describing simulation parameters and robots in it.
 	 * @return the object, describing simulation parameters and robots in it.
 	 */
 	public static SimulationSpecification getSimulationSpecification() {
 		return simulationSpecification;
 	}
 
 	/**
 	 * Sets the object, describing simulation parameters and robots in it.
 	 * @param simulationSpecification object, describing simulation parameters and robots in it.
 	 */
 	public static void setSimulationSpecification(SimulationSpecification simulationSpecification) {
 		SimulationTabController.simulationSpecification = simulationSpecification;
 	}
 }
