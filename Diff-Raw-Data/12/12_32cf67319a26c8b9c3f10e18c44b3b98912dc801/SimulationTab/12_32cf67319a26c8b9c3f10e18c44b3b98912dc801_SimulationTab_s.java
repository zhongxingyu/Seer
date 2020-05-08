 package ussr.aGui.tabs.simulation;
 
 
 import java.awt.Dimension;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.swing.BorderFactory;
 
 import javax.swing.ImageIcon;
 
 import javax.swing.border.TitledBorder;
 
 import javax.swing.tree.DefaultMutableTreeNode;
 import javax.swing.tree.DefaultTreeCellRenderer;
 import javax.swing.tree.DefaultTreeModel;
 import javax.swing.tree.TreeSelectionModel;
 
 import ussr.aGui.GuiFrames;
 import ussr.aGui.MainFrames;
 
 import ussr.aGui.tabs.Tabs;
 import ussr.aGui.tabs.controllers.SimulationTabController;
 import ussr.aGui.tabs.designHelpers.hintPanel.HintPanel;
 import ussr.aGui.tabs.enumerations.TabsComponentsText;
 import ussr.aGui.tabs.enumerations.TabsIcons;
 import ussr.aGui.tabs.enumerations.hintPanel.HintsSimulationTab;
 import ussr.builder.simulationLoader.SimulationSpecification;
 
 /**
  * Defines visual appearance of the tab called Simulation.
  * @author Konstantinas
  */
 public class SimulationTab extends Tabs {
 
 	/**
 	 * The constrains of grid bag layout used during design of the tab.
 	 */
 	private GridBagConstraints gridBagConstraints = new GridBagConstraints();
 
 	/**
 	 * The name of the node selected in the simulation tree by user.
 	 */
 	private static String selectedNodeName;
 
 	/**
 	 * The panel for displaying hints.
 	 */
 	private static HintPanel  hintPanel;
 
 	/**
 	 * Defines visual appearance of the tab called "Simulation".
 	 * @param initiallyVisible, true if the tab is visible after activation of main GUI window. 
 	 * @param firstTabbedPane, location of the tab in the main GUI frame. True if it is the first tabbed pane from the top.
 	 * @param tabTitle, the title of the tab.
 	 * @param imageIconDirectory,the directory for icon displayed in the top-left corner of the tab.
 	 */
 	public SimulationTab(boolean initiallyVisible, boolean firstTabbedPane, String tabTitle, String imageIconDirectory){
 		super(initiallyVisible,firstTabbedPane,tabTitle,imageIconDirectory);
 
 		/*instantiate new panel, which will be the container for all components situated in the tab*/		
 		super.jComponent = new javax.swing.JPanel(new GridBagLayout());
 
 		initComponents();
 	}
 
 	/**
 	 * Initializes the visual appearance of all components in the tab.
 	 * Follows Strategy  pattern.
 	 */
 	public void initComponents() {
 		/*Instantiate components*/
 		jScrollPaneTreeSimulation = new javax.swing.JScrollPane();
 		jPanelEditor = new javax.swing.JPanel(new GridBagLayout());
 		jTreeSimulation = new javax.swing.JTree();
 		jSplitPaneSimulationTreeAndEditor = new javax.swing.JSplitPane();
 
 
 		/*Define components*/
 		DefaultTreeModel treeModel = new DefaultTreeModel(initializeSimulationRootNode());
 		jTreeSimulation.setModel(treeModel);
 		jTreeSimulation.setExpandsSelectedPaths(true);
 
 		ImageIcon closedIcon = TabsIcons.EXPANSION_CLOSED_SMALL.getImageIcon();
 		ImageIcon openIcon = TabsIcons.EXPANSION_OPENED_SMALL.getImageIcon();
 		ImageIcon leafIcon = TabsIcons.FINAL_LEAF_SMALL.getImageIcon();
 		if (openIcon != null) {
 			DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();		    
 			renderer.setClosedIcon(closedIcon);
 			renderer.setOpenIcon(openIcon);
 			renderer.setLeafIcon(leafIcon);
 			jTreeSimulation.setCellRenderer(renderer);
 		}
 		jTreeSimulation.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
 		jTreeSimulation.addTreeSelectionListener(new javax.swing.event.TreeSelectionListener() {
 			public void valueChanged(javax.swing.event.TreeSelectionEvent evt) {
 				DefaultMutableTreeNode node = (DefaultMutableTreeNode)
 				jTreeSimulation.getLastSelectedPathComponent();
 				if (node == null)
 					/*Nothing is selected.*/	
 					return;
 				Object nodeInfo = node.getUserObject();
 				if (node.isLeaf()||node.isNodeRelated(firstNodeHierarchySimulation)) {
 					selectedNodeName = node.getPath()[node.getPath().length-1].toString();
 					SimulationTabController.jTreeItemSelectedActionPerformed(selectedNodeName);
 				} 
 			}
 		});
 
 		jScrollPaneTreeSimulation.setViewportView(jTreeSimulation);
 		jScrollPaneTreeSimulation.setPreferredSize(new Dimension(300,300));
 
 
 		TitledBorder displayTitle;
 		displayTitle = BorderFactory.createTitledBorder(TabsComponentsText.EDIT_VALUE.getUserFriendlyName());
 		displayTitle.setTitleJustification(TitledBorder.CENTER);
 		jPanelEditor.setBorder(displayTitle);
 		jPanelEditor.setPreferredSize(new Dimension(300,300));
 
 		jSplitPaneSimulationTreeAndEditor.setOrientation(javax.swing.JSplitPane.HORIZONTAL_SPLIT);
 
 		jSplitPaneSimulationTreeAndEditor.setLeftComponent(jScrollPaneTreeSimulation);
 		jSplitPaneSimulationTreeAndEditor.setRightComponent(jPanelEditor);
 	
 		jSplitPaneSimulationTreeAndEditor.setPreferredSize(new Dimension(300,300));
 
 		jSplitPaneSimulationTreeAndEditor.setDividerSize(5);
		jSplitPaneSimulationTreeAndEditor.setDividerLocation(0.9);
 		jSplitPaneSimulationTreeAndEditor.setVisible(false);
 		gridBagConstraints.fill = GridBagConstraints.BOTH;
 		gridBagConstraints.gridx = 0;
 		gridBagConstraints.gridy = 0;
 
 		super.jComponent.add(jSplitPaneSimulationTreeAndEditor,gridBagConstraints);
 
 		hintPanel = new HintPanel(600, HINT_PANEL_HEIGHT);
 		hintPanel.setBorderTitle("Display for hints");
 		hintPanel.setText(HintsSimulationTab.SIMULATION.getHintText());
 		hintPanel.setVisible(false);
 
 		gridBagConstraints.fill = GridBagConstraints.BOTH;		
 		gridBagConstraints.anchor = GridBagConstraints.PAGE_END;
 		gridBagConstraints.gridx = 0;
 		gridBagConstraints.gridy = 1;
 
 		super.jComponent.add(hintPanel,gridBagConstraints);
 	}
 
 	/**
 	 * Initializes the root tree node(simulation) including all sub nodes.
 	 * @return simulation root tree node including all sub nodes.
 	 */
 	private DefaultMutableTreeNode initializeSimulationRootNode(){
 
 		for(int index =0;index<SimulationTabTreeNodes.values().length;index++){
 			SimulationTabTreeNodes currentNode = SimulationTabTreeNodes.values()[index];
 			switch(currentNode.getPlaceInHierarchy()){
 			case FIRST:
 				firstNodeHierarchySimulation = new DefaultMutableTreeNode(currentNode.getUserFriendlyName());
 				//set default mutable tree node for future easier reference.
 				currentNode.setDefaultMutableTreeNode(firstNodeHierarchySimulation);
 				break;
 			case SECOND:
 				secondNodeHierarchy = new DefaultMutableTreeNode(currentNode.getUserFriendlyName());
 				currentNode.setDefaultMutableTreeNode(secondNodeHierarchy);
 				firstNodeHierarchySimulation.add(secondNodeHierarchy);
 				break;
 			case THIRD:
 				thirdNodeHierarchy = new DefaultMutableTreeNode(currentNode.getUserFriendlyName());
 				currentNode.setDefaultMutableTreeNode(thirdNodeHierarchy);
 				secondNodeHierarchy.add(thirdNodeHierarchy);
 				break;
 			case NOT_USED://do nothing
 				break;
 			default: throw new Error("Place in hierarchy "+ currentNode.getPlaceInHierarchy()+ " is not supported yet.");
 			}
 		}
 		return firstNodeHierarchySimulation;
 	}
 
 	/**
 	 * Used to keep track after unique robot number.
 	 */
 	private static int robotNumber=0;
 
 	/**
 	 * Sets unique robot number.
 	 * @param robotNumber, unique robot number
 	 */
 	public static void setRobotNumber(int robotNumber) {
 		SimulationTab.robotNumber = robotNumber;
 	}
 
 	/**
 	 * Used to keep track after the last unique ID, which was assigned to module of modular robot in simulation environment.
 	 */
 	private static int lastID;
 
 	/**
 	 * The array for storing unique IDs generated for modular robot in simulation environment.
 	 */
 	private static List<Integer> idsModules = new ArrayList<Integer>(); 
 
 	/**
 	 * Adds robot nodes in the simulation tree.
 	 * @param simulationSpecification, object keeping information about simulation.
 	 * @param fromSimulationXMLFile, flag to indicate that robot nodes are added from loaded simulation file(true), false for
 	 * nodes being created from loaded Robot XML file.
 	 */
 	public static void addRobotNodes(SimulationSpecification simulationSpecification, boolean fromSimulationXMLFile, boolean includeIds){
 
 		DefaultTreeModel model = (DefaultTreeModel)jTreeSimulation.getModel();
 		DefaultMutableTreeNode robotsNode = (DefaultMutableTreeNode) model.getChild(model.getRoot(),2);//2 stands for the level in hierarchy.
 
 		if (fromSimulationXMLFile &&includeIds){//Robots loaded from simulation XML file
 			robotNumber =0;//reset
 
 			robotsNode.removeAllChildren();			
 			int nrRobotsInSimulation = simulationSpecification.getRobotsInSimulation().size();
 
 			for (int robotNr=1;robotNr<nrRobotsInSimulation+1;robotNr++){// starts counting from 1, because it is more user friendly in the tree
 				DefaultMutableTreeNode thirdNodeHierarchyRobot =  new DefaultMutableTreeNode(SimulationTabTreeNodes.ROBOT_NR.getUserFriendlyName()+"."+robotNr);
 				robotsNode.add(thirdNodeHierarchyRobot);
 				robotNumber =robotNr;// keep track for robot number
 
 				int amountModules = simulationSpecification.getRobotsInSimulation().get(robotNr-1).getAmountModules();
 				if(robotNr==1){
 					idsModules.clear();
 					for (int id=0;id<amountModules;id++){
 						idsModules.add(id);
 						System.out.println("ID:"+ id);
 					}
 					lastID = idsModules.get(idsModules.size()-1);
 					simulationSpecification.getRobotsInSimulation().get(0).setIdsModules(idsModules);
 				}else{
 					idsModules.clear();
 					for (int id=lastID+1;id<lastID+amountModules+1;id++){
 						idsModules.add(id);
 						System.out.println("ID:"+ id);
 					}
 					lastID = idsModules.get(idsModules.size()-1);
 
 					simulationSpecification.getRobotsInSimulation().get(robotNr-1).setIdsModules(idsModules);
 				}		         
 			}
 
 		} else if(fromSimulationXMLFile==false && includeIds==true ){// robots loaded from Robot XML file
 			robotNumber++;// keep track for robot number
 			int amountRobots = simulationSpecification.getRobotsInSimulation().size();
 			int amountModules = simulationSpecification.getRobotsInSimulation().get(amountRobots-1).getAmountModules();
 
 			idsModules.clear();
 
 			for (int id=lastID+1;id<lastID+amountModules+1;id++){
 				idsModules.add(id);
 				System.out.println("ID:"+ id);
 			}
 			lastID = idsModules.get(idsModules.size()-1);
 			simulationSpecification.getRobotsInSimulation().get(amountRobots-1).setIdsModules(idsModules);
 			DefaultMutableTreeNode thirdNodeHierarchyRobot =  new DefaultMutableTreeNode(SimulationTabTreeNodes.ROBOT_NR.getUserFriendlyName()+"."+robotNumber);
 			model.insertNodeInto(thirdNodeHierarchyRobot, robotsNode, robotsNode.getChildCount());
 
 		}else if(fromSimulationXMLFile==false && includeIds==false){//Refreshes  node structure after modular robot was deleted.
 			robotNumber--;// keep track for robot number
 			for (int robotNumber=1;robotNumber<simulationSpecification.getRobotsInSimulation().size()+1;robotNumber++){
 
 				DefaultMutableTreeNode thirdNodeHierarchyRobot =  new DefaultMutableTreeNode(SimulationTabTreeNodes.ROBOT_NR.getUserFriendlyName()+"."+robotNumber);
 				model.insertNodeInto(thirdNodeHierarchyRobot, robotsNode, robotsNode.getChildCount());
 			}
 		}
 		SimulationTabController.setSimulationSpecification(simulationSpecification);
 		model.reload();	// refresh tree model so that there is no left overs, like frozen graphical elements	
 		jTreeSimulationExpandAllNodes();
 		jScrollPaneTreeSimulation.setViewportView(jTreeSimulation);		
 	} 
 
 
 	/**
 	 * Expands all nodes in the simulation tree. 
 	 */
 	public static void jTreeSimulationExpandAllNodes(){
 		for (int rowNr = 0; rowNr < jTreeSimulation.getRowCount(); rowNr++) {
 			jTreeSimulation.expandRow(rowNr);
 		}
 	}
 
 	/**
 	 * Sets the tab visible or not.
 	 * @param visible, true for visible
 	 */
 	public static void setTabVisible(boolean visible) {			
 		jSplitPaneSimulationTreeAndEditor.setVisible(visible);
 		hintPanel.setVisible(visible);	
 		hintPanel.setText(HintsSimulationTab.SIMULATION.getHintText());
 	}
 
 	/**
 	 * Resizes all components of the tab in order to fit precisely with dimensions of tabbed pane,
 	 * which depends on the dimensions of different screens.
 	 */
 	public static void resizeComponents(){
 		int simulationTabIndex = 0;
 		int heightIcon = MainFrames.getJTabbedPaneFirst().getIconAt(simulationTabIndex).getIconHeight();
 		int height = MainFrames.getJTabbedPaneFirst().getHeight()-(4*heightIcon)- HINT_PANEL_HEIGHT ;
 		int width = (int)MainFrames.getJTabbedPaneFirst().getWidth()-10*GuiFrames.VERTICAL_GAPS;
 		jSplitPaneSimulationTreeAndEditor.setPreferredSize(new Dimension(width,height));		
 		jSplitPaneSimulationTreeAndEditor.validate();	
 	}
 
 	/**
 	 * Returns the panel for displaying hints.
 	 * @return the panel for displaying hints.
 	 */
 	public static HintPanel getHintPanel() {
 		return hintPanel;
 	}
 
 	/**
 	 * Returns the name of the node selected in the simulation tree by user.
 	 * @return the name of the node selected in the simulation tree by user.
 	 */
 	public static String getSelectedNodeName() {
 		return selectedNodeName;
 	}
 
 	/**
 	 * Returns the simulation tree.
 	 * @return the simulation tree.
 	 */
 	public static javax.swing.JTree getJTreeSimulation() {
 		return jTreeSimulation;
 	}
 
 	/**
 	 * Returns the panel for editing values of nodes in simulation tree.
 	 * @return the panel for editing values of nodes in simulation tree.
 	 */
 	public static javax.swing.JPanel getJPanelEditor() {
 		return jPanelEditor;
 	}
 	
 	/**
 	 * Cleans the panel for editing node values from all components in it. 
 	 */
 	public static void cleanJPanelEditor(){
 		jPanelEditor.removeAll();
 		jPanelEditor.revalidate();
 		jPanelEditor.repaint();		
 	}
 	
 	/**
 	 * Returns robots node from the tree of simulation nodes.
 	 * @return robots node from the tree of simulation nodes.
 	 */
 	public static DefaultMutableTreeNode getRobotsNode(){
 		DefaultTreeModel model = (DefaultTreeModel)jTreeSimulation.getModel();
 		 return (DefaultMutableTreeNode) model.getChild(model.getRoot(),2);	
 	}
 	
 	/**
 	 * Returns default mutable tree model of simulation tree.
 	 * @return default mutable tree model of simulation tree.
 	 */
 	public static DefaultTreeModel getSimulationTreeModel(){
 		return (DefaultTreeModel)jTreeSimulation.getModel();
 	}
 	
 	private static javax.swing.JTree jTreeSimulation;
 	private static javax.swing.JScrollPane jScrollPaneTreeSimulation;
 
 	private static javax.swing.JPanel jPanelEditor;
 
 	private static  DefaultMutableTreeNode firstNodeHierarchySimulation,secondNodeHierarchy,thirdNodeHierarchy;
 
 	private static javax.swing.JSplitPane jSplitPaneSimulationTreeAndEditor;
 }
