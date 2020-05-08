 package ussr.aGui;
 
 
 import java.awt.Dimension;
 import java.awt.Insets;
 import java.util.ArrayList;
 
 import javax.swing.JTabbedPane;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 
 import ussr.aGui.controllers.GeneralController;
 import ussr.aGui.controllers.MainFrameSeparateController;
 import ussr.aGui.enumerations.MainFrameIcons;
 import ussr.aGui.enumerations.MainFrameComponentsText;
 import ussr.aGui.fileChoosing.FileChoosingInter;
 import ussr.aGui.fileChoosing.fileDialog.FileDialogCustomizedInter;
 import ussr.aGui.fileChoosing.jFileChooser.JFileChooserCustomizedInter;
 import ussr.aGui.tabs.TabsInter;
 import ussr.aGui.tabs.constructionTabs.ConstructionTabs;
 import ussr.aGui.tabs.designHelpers.JComponentsFactory;
 import ussr.remote.GUIRemoteSimulationAdapter;
 
 /**
  * Holds methods and constants common for different design(visual appearance) of main GUI window (MainFrames). 
  * @author Konstantinas
  */
 @SuppressWarnings("serial")
 public abstract class MainFrames extends GuiFrames implements MainFramesInter {
 	/**
 	 * The main GUI window.
 	 */
 	protected static MainFrames mainFrame;
 
 	/**
 	 * Insets of main frame border.
 	 */
 	protected static Insets insets; 
 	
 	/**
 	 * Container for keeping main GUI window components, the height of which determine the height of the window.  
 	 */
 	protected ArrayList<javax.swing.JComponent> components = new ArrayList<javax.swing.JComponent>();
 
 	/**
 	 * Containers for keeping all tabs plugged-in the main GUI window(MainFrame), tabs in the first and second tabbed panes respectively.
 	 */
 	protected ArrayList<TabsInter> allTabs,tabsFirstTabbedPane,tabsSecondTabbedPane;
 	
 	/**
 	 *  A number of objects for file choosing.
 	 */
 	public static FileChoosingInter fcOpenSimulationDialog,fcSaveSimulationDialog,
 	                                fcOpenRobotDialog,fcSaveRobotDialog ;
 	
 	/**
 	 * Initializes visual appearance and functionality common to all instances of main GUI window.  
 	 */
 	public MainFrames(){
 		populateTabs();
 		initFrameProperties();
 		/*Starts simulation adapter, which in turn starts simulation server (only once) */	
 		new GUIRemoteSimulationAdapter();
 		initFileChoosing();
 		}
 	
 	/**
 	 * Initializes a number of file choosers depending on operating system(OS).
 	 * To be more specific, in case of Mac OS initializes File Dialogs instead of jFileChoosers.
 	 * The main reason for that is that jFileChooser is not so well supported on Mac, from point of view
 	 * on (look and feel) and file filters.  
 	 */
 	private void initFileChoosing(){
 		if (GeneralController.getOperatingSystemName().contains("Mac")){
 		   
 			//System.setProperty("apple.awt.fileDialogForDirectories", "true");			
 			fcOpenSimulationDialog = FileDialogCustomizedInter.FD_OPEN_SIMULATION;
 			fcSaveSimulationDialog = FileDialogCustomizedInter.FD_SAVE_SIMULATION;
 			fcOpenRobotDialog = FileDialogCustomizedInter.FD_OPEN_ROBOT;
 			fcSaveRobotDialog = FileDialogCustomizedInter.FD_SAVE_ROBOT;
 			//System.setProperty("apple.awt.fileDialogForDirectories", "false");	
 			
 		}else{
 			fcOpenSimulationDialog = JFileChooserCustomizedInter.FC_OPEN_SIMULATION;
 			fcSaveSimulationDialog = JFileChooserCustomizedInter.FC_SAVE_SIMULATION;
 			fcOpenRobotDialog = JFileChooserCustomizedInter.FC_OPEN_ROBOT;
 			fcSaveRobotDialog = JFileChooserCustomizedInter.FC_SAVE_ROBOT;
 		}
 	}
 
 	/**
 	 * Filters out and populates the tabs assigned to the first and second tabbed panes, into separate array lists. 
 	 */
 	public void populateTabs(){
 		tabsFirstTabbedPane = new ArrayList<TabsInter>();
 		tabsSecondTabbedPane = new ArrayList<TabsInter>();
 		allTabs = new ArrayList<TabsInter>();
 
 		for (int tabNr=0; tabNr<ALL_TABS.length;tabNr++){
 			TabsInter currentTab = ALL_TABS[tabNr];
 			if (currentTab.isFirstTabbedPane()){
 				tabsFirstTabbedPane.add(currentTab);	
 			}else {
 				tabsSecondTabbedPane.add(currentTab);
 			}
 			allTabs.add(currentTab);
 		}
 	}	
 
 	/**
 	 * Initializes the visual appearance of main GUI windows.
 	 * Follows Strategy  pattern.
 	 */
 	public abstract void initComponents();	
 
 	/**
 	 * Initializes the basic properties of the Frame common to all MainFrame instances.
 	 * For example: top-left icon,title and so on.
 	 */
 	public void initFrameProperties(){		
 		setUSSRicon(this);
 		setTitle(USSR_TITLE);
 		setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
 		setAlwaysOnTop(false);
 		pack();
 		insets = this.getInsets();		
 	}
 
 
 	/**
 	 * Initializes visual appearance (view) of menu bar.
 	 * @param width, the width of menu bar.
 	 * @param height, the height of menu bar.
 	 */
 	public void initJMenuBar(int width, int height){
 
 		/*Instantiate components*/
 		jMenuBarMain = new javax.swing.JMenuBar();			
 	
 		jMenuSimulation = new javax.swing.JMenu();
 		
 		jMenuItemNewSimulation =  new javax.swing.JMenuItem();
 		jMenuItemOpenSimulation = new javax.swing.JMenuItem();
 		jMenuItemExit = new javax.swing.JMenuItem();		
 		jMenuItemSave = new javax.swing.JMenuItem();
 		
 		jSeparator1 = new javax.swing.JSeparator();
 		jSeparator2 = new javax.swing.JSeparator();
 		jSeparator3 = new javax.swing.JSeparator();
 		
 		jMenuRender = new javax.swing.JMenu();	
 		
 		jCheckBoxMenuItemPhysics = new javax.swing.JCheckBoxMenuItem();
 		jCheckBoxMenuItemWireFrame = new javax.swing.JCheckBoxMenuItem();
 		jCheckBoxMenuBounds = new javax.swing.JCheckBoxMenuItem();
 		jCheckBoxMenuItemNormals = new javax.swing.JCheckBoxMenuItem();
 		jCheckBoxMenuItemLights = new javax.swing.JCheckBoxMenuItem();
 		jCheckBoxMenuBufferDepth = new javax.swing.JCheckBoxMenuItem();	
 		
 		jMenuWindow = new javax.swing.JMenu();
 		jMenuHide = new javax.swing.JMenu();
 		
 		jCheckBoxMenuItemInteraction= new javax.swing.JCheckBoxMenuItem();
 		jCheckBoxMenuItemDebugging = new javax.swing.JCheckBoxMenuItem();		
 		jCheckBoxMenuItemDisplayForHints = new javax.swing.JCheckBoxMenuItem();
 
         /*Description of components*/
 		jMenuBarMain.setPreferredSize(new Dimension(width,height));
 		
 		jMenuSimulation.setText(MainFrameComponentsText.SIMULATION.getUserFriendlyName());
 		
 		jMenuItemNewSimulation.setText(MainFrameComponentsText.NEW.getUserFriendlyName());
 		jMenuItemNewSimulation.setIcon(MainFrameIcons.NEW_SIMULATION_SMALL.getImageIcon());
 		jMenuItemNewSimulation.setDisabledIcon(MainFrameIcons.NEW_SIMULATION_SMALL_DISABLED.getImageIcon());
 		jMenuItemNewSimulation.setEnabled(true);
 		jMenuItemNewSimulation.addActionListener(new java.awt.event.ActionListener() {
 			public void actionPerformed(java.awt.event.ActionEvent evt) {
 				 MainFrameSeparateController.jButtonMenuItemNewSimulationActionPerformed();
 			}
 		});
 		jMenuSimulation.add(jMenuItemNewSimulation);
 
 		jMenuItemOpenSimulation.setText(MainFrameComponentsText.OPEN.getUserFriendlyName());
 		jMenuItemOpenSimulation.setIcon(MainFrameIcons.OPEN_SMALL.getImageIcon());
 		jMenuItemOpenSimulation.setDisabledIcon(MainFrameIcons.OPEN_SMALL_DISABLED.getImageIcon());		
 		jMenuItemOpenSimulation.addActionListener(new java.awt.event.ActionListener() {
 			public void actionPerformed(java.awt.event.ActionEvent evt) {
 				MainFrameSeparateController.openActionPerformed(fcOpenSimulationDialog);
 			}
 		});
 
 		jMenuSimulation.add(jMenuItemOpenSimulation);   
 
 		jMenuSimulation.add(jSeparator2);
 
 		jMenuItemSave.setText(MainFrameComponentsText.SAVE.getUserFriendlyName());
 		jMenuItemSave.setIcon(MainFrameIcons.SAVE_SMALL.getImageIcon());
 		jMenuItemSave.setDisabledIcon(MainFrameIcons.SAVE_SMALL_DISABLED.getImageIcon());	
 		jMenuItemSave.addActionListener(new java.awt.event.ActionListener() {
 			public void actionPerformed(java.awt.event.ActionEvent evt) {
 				MainFrameSeparateController.saveActionPerformed(fcSaveSimulationDialog);
 				
 			}
 		});
 
 		jMenuSimulation.add(jMenuItemSave);
 
 		jMenuSimulation.add(jSeparator1);
 		
 		jMenuItemExit.setText(MainFrameComponentsText.EXIT.getUserFriendlyName());
 		jMenuItemExit.setIcon(MainFrameIcons.EXIT_SMALL.getImageIcon());		
 		jMenuItemExit.addActionListener(new java.awt.event.ActionListener() {
 			public void actionPerformed(java.awt.event.ActionEvent evt) {
 				MainFrameSeparateController.jMenuItemExitActionPerformed();
 			}
 		});
 		jMenuSimulation.add(jMenuItemExit);
 
 		jMenuBarMain.add(jMenuSimulation);
 
 		jMenuRender.setText(MainFrameComponentsText.RENDER.getUserFriendlyName());
 		jCheckBoxMenuItemPhysics.setSelected(false);
 		jCheckBoxMenuItemPhysics.setText(MainFrameComponentsText.PHYSICS.getUserFriendlyName());
 		jCheckBoxMenuItemPhysics.addActionListener(new java.awt.event.ActionListener() {
 			public void actionPerformed(java.awt.event.ActionEvent evt) {
 				MainFrameSeparateController.jCheckBoxMenuItemPhysicsActionPerformed(jCheckBoxMenuItemPhysics);
 			}
 		});
 		jMenuRender.add(jCheckBoxMenuItemPhysics);
 
 		jCheckBoxMenuItemWireFrame.setSelected(false);
 		jCheckBoxMenuItemWireFrame.setText(MainFrameComponentsText.WIRE_FRAME.getUserFriendlyName());
 		jCheckBoxMenuItemWireFrame.addActionListener(new java.awt.event.ActionListener() {
 			public void actionPerformed(java.awt.event.ActionEvent evt) {
 				MainFrameSeparateController.jCheckBoxMenuItemWireFrameActionPerformed(jCheckBoxMenuItemWireFrame);
 			}
 		});
 		jMenuRender.add(jCheckBoxMenuItemWireFrame);
 
 		jCheckBoxMenuBounds.setSelected(false);
 		jCheckBoxMenuBounds.setText(MainFrameComponentsText.BOUNDS.getUserFriendlyName());
 		jCheckBoxMenuBounds.addActionListener(new java.awt.event.ActionListener() {
 			public void actionPerformed(java.awt.event.ActionEvent evt) {
 				MainFrameSeparateController.jCheckBoxMenuBoundsActionPerformed(jCheckBoxMenuBounds);
 			}
 		});
 		jMenuRender.add(jCheckBoxMenuBounds);
 
 		jCheckBoxMenuItemNormals.setSelected(false);
 		jCheckBoxMenuItemNormals.setText(MainFrameComponentsText.NORMALS.getUserFriendlyName());
 		jCheckBoxMenuItemNormals.addActionListener(new java.awt.event.ActionListener() {
 			public void actionPerformed(java.awt.event.ActionEvent evt) {
 				MainFrameSeparateController.jCheckBoxMenuItemNormalsActionPerformed(jCheckBoxMenuItemNormals);
 			}
 		});
 		jMenuRender.add(jCheckBoxMenuItemNormals);
 
 		jCheckBoxMenuItemLights.setSelected(false);
 		jCheckBoxMenuItemLights.setText(MainFrameComponentsText.LIGHTS.getUserFriendlyName());
 		jCheckBoxMenuItemLights.addActionListener(new java.awt.event.ActionListener() {
 			public void actionPerformed(java.awt.event.ActionEvent evt) {
 				MainFrameSeparateController.jCheckBoxMenuItemLightsActionPerformed(jCheckBoxMenuItemLights);
 			}
 		});
 		jMenuRender.add(jCheckBoxMenuItemLights);
 
 		jCheckBoxMenuBufferDepth.setSelected(false);
 		jCheckBoxMenuBufferDepth.setText(MainFrameComponentsText.BUFFER_DEPTH.getUserFriendlyName());
 		jCheckBoxMenuBufferDepth.addActionListener(new java.awt.event.ActionListener() {
 			public void actionPerformed(java.awt.event.ActionEvent evt) {
 				MainFrameSeparateController.jCheckBoxMenuBufferDepthActionPerformed(jCheckBoxMenuBufferDepth);
 			}
 		});
 
 		jMenuRender.add(jCheckBoxMenuBufferDepth);
 
 		jMenuBarMain.add(jMenuRender);		
 		
 		jMenuWindow.setText(MainFrameComponentsText.WINDOW.getUserFriendlyName());	
 		
 		jMenuHide.setText(MainFrameComponentsText.HIDE.getUserFriendlyName());
 		
 		jCheckBoxMenuItemInteraction.setText(MainFrameComponentsText.INTERACTION.getUserFriendlyName());
 		jCheckBoxMenuItemInteraction.setSelected(false);
 		jCheckBoxMenuItemInteraction.addActionListener(new java.awt.event.ActionListener() {
 			public void actionPerformed(java.awt.event.ActionEvent evt) {
 				MainFrameSeparateController.hideTabbedPanesActionPerformed(jCheckBoxMenuItemInteraction.isSelected(),jCheckBoxMenuItemDebugging.isSelected());
 			}
 		});
 		jMenuHide.add(jCheckBoxMenuItemInteraction);
 		
 		jCheckBoxMenuItemDebugging.setText(MainFrameComponentsText.DEBUGGING.getUserFriendlyName());
 		jCheckBoxMenuItemDebugging.setSelected(false);
 		jCheckBoxMenuItemDebugging.addActionListener(new java.awt.event.ActionListener() {
 			public void actionPerformed(java.awt.event.ActionEvent evt) {
 				MainFrameSeparateController.hideTabbedPanesActionPerformed(jCheckBoxMenuItemInteraction.isSelected(),jCheckBoxMenuItemDebugging.isSelected());
 			}
 		});
 		jMenuHide.add(jCheckBoxMenuItemDebugging);		
 		
 		jMenuHide.add(jSeparator3);
 		
 		jCheckBoxMenuItemDisplayForHints.setText(MainFrameComponentsText.DISPLAY_FOR_HINTS.getUserFriendlyName());
 		jCheckBoxMenuItemDisplayForHints.addActionListener(new java.awt.event.ActionListener() {
 			public void actionPerformed(java.awt.event.ActionEvent evt) {
 				MainFrameSeparateController.jCheckBoxMenuItemDisplayForHintsActionPerformed(jCheckBoxMenuItemDisplayForHints);
 			}
 		});
 		
 		jMenuHide.add(jCheckBoxMenuItemDisplayForHints);
 		
 		jMenuWindow.add(jMenuHide);
 		
 		jMenuBarMain.add(jMenuWindow);
 		setJMenuBar(jMenuBarMain); 
 	}
 
 
 	/**
 	 * Returns menu item for saving simulation.
 	 * @return menu item for saving simulation.
 	 */
 	public static javax.swing.JMenuItem getJMenuItemSave() {
 		return jMenuItemSave;
 	}
 
 	/**
 	 * Returns menu item for opening (loading) simulation.
 	 * @return menu item for opening (loading) simulation.
 	 */
 	public static javax.swing.JMenuItem getJMenuItemOpen() {
 		return jMenuItemOpenSimulation;
 	}
 
 	/**
 	 * Returns the main menu bar of GUI window.
 	 * @return jMenuBarMain, the main menu bar of GUI window.
 	 */
 	public static javax.swing.JMenuBar getJMenuBarMain() {
 		return jMenuBarMain;
 	}
 
 	/**
 	 * Initializes the tool bar for general control
 	 * @param width, tool bar width.
 	 * @param height, tool bar height.
 	 */
 	public void initJToolbarGeneralControl(int width,int height){
 		/*Instantiation of components*/
 		jButtonNewSimulation = new javax.swing.JButton();
 		jButtonRunRealTime = new javax.swing.JButton();
 		jButtonRunStepByStep = new javax.swing.JButton();		
 		jButtonRunFast = new javax.swing.JButton();
 		jButtonPause = new javax.swing.JButton();
 		jButtonTerminate = new javax.swing.JButton();
 		jButtonReloadSimulation = new javax.swing.JButton();
 		jButtonSave = JComponentsFactory.initSaveButton();
 		jButtonOpen = JComponentsFactory.initOpenButton();
 			
 		jToolBarSeparator3 = new javax.swing.JToolBar.Separator();			
 		jToolBarSeparator4 = new javax.swing.JToolBar.Separator();
 		jToolBarSeparator5 = new javax.swing.JToolBar.Separator();
 		jToolBarSeparator6 = new javax.swing.JToolBar.Separator();
 		
 		jToggleButtonConstructRobot = new javax.swing.JToggleButton();
 		jToggleButtonVisualizer = new javax.swing.JToggleButton();
 		
 		jToggleButtonMaximizeDebugging = new javax.swing.JToggleButton();
 		jToggleButtonMaximizeInteraction = new javax.swing.JToggleButton();
 		
 		/*Description of components*/
 		jToolBarGeneralControl = new javax.swing.JToolBar();
 		jToolBarGeneralControl.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
 		jToolBarGeneralControl.setRollover(true);
 		jToolBarGeneralControl.setFloatable(false);
 		jToolBarGeneralControl.setToolTipText(MainFrameComponentsText.GENERAL_CONTROL.getUserFriendlyName());
 		jToolBarGeneralControl.setPreferredSize(new Dimension(width,height));
 
 		jButtonNewSimulation.setToolTipText(MainFrameComponentsText.START_NEW_SIMULATION.getUserFriendlyName());		
 		jButtonNewSimulation.setIcon(MainFrameIcons.NEW_SIMULATION.getImageIcon());
 		jButtonNewSimulation.setRolloverIcon(MainFrameIcons.NEW_SIMULATION_ROLLOVER.getImageIcon());		
 		jButtonNewSimulation.setDisabledIcon(MainFrameIcons.NEW_SIMULATION_DISABLED.getImageIcon());		
 		jButtonNewSimulation.setFocusable(false);    
 		jButtonNewSimulation.setPreferredSize(BUTTON_DIMENSION);      
 		jButtonNewSimulation.addActionListener(new java.awt.event.ActionListener() {
 			public void actionPerformed(java.awt.event.ActionEvent evt) {
 			 MainFrameSeparateController.jButtonMenuItemNewSimulationActionPerformed();        	  
 			}
 		});
 		jToolBarGeneralControl.add(jButtonNewSimulation);
 		
 		jButtonOpen.addActionListener(new java.awt.event.ActionListener() {
 			public void actionPerformed(java.awt.event.ActionEvent evt) {
 				MainFrameSeparateController.openActionPerformed(fcOpenSimulationDialog);
 			}
 		});		
 		jToolBarGeneralControl.add(jButtonOpen);		
 		
 		jButtonSave.addActionListener(new java.awt.event.ActionListener() {
 			public void actionPerformed(java.awt.event.ActionEvent evt) {
 				MainFrameSeparateController.saveActionPerformed(fcSaveSimulationDialog);
 			}
 		});		
 		jToolBarGeneralControl.add(jButtonSave);
 		
 		jButtonReloadSimulation.setToolTipText(MainFrameComponentsText.RESTART_CURRENT_SIMULATION.getUserFriendlyName());
 		jButtonReloadSimulation.setIcon(MainFrameIcons.RELOAD_CURRENT_SIMULATION.getImageIcon());
 		jButtonReloadSimulation.setRolloverIcon(MainFrameIcons.RELOAD_CURRENT_SIMULATION_ROLLOVER.getImageIcon());		
 		jButtonReloadSimulation.setDisabledIcon(MainFrameIcons.RELOAD_CURRENT_SIMULATION_ROLLOVER_DISABLED.getImageIcon());
 		jButtonReloadSimulation.setPreferredSize(BUTTON_DIMENSION);
 		jButtonReloadSimulation.setFocusable(false);   
 		jButtonReloadSimulation.addActionListener(new java.awt.event.ActionListener() {
 			public void actionPerformed(java.awt.event.ActionEvent evt) {
 				MainFrameSeparateController.jButtonReloadCurrentSimulationActionPerformed();
 			}
 		});
 		jToolBarGeneralControl.add(jButtonReloadSimulation);
 		
 		jToggleButtonConstructRobot.setToolTipText(MainFrameComponentsText.CONSTRUCT_ROBOT.getUserFriendlyName());
 		jToggleButtonConstructRobot.setIcon(MainFrameIcons.CONSTRUCT_ROBOT.getImageIcon());
 		jToggleButtonConstructRobot.setSelectedIcon(MainFrameIcons.CONSTRUCT_ROBOT_ROLLOVER.getImageIcon());
 		jToggleButtonConstructRobot.setRolloverIcon(MainFrameIcons.CONSTRUCT_ROBOT_ROLLOVER.getImageIcon());
 		jToggleButtonConstructRobot.setDisabledIcon(MainFrameIcons.CONSTRUCT_ROBOT_DISABLED.getImageIcon());		
 		jToggleButtonConstructRobot.setFocusable(false);		
 		jToggleButtonConstructRobot.setPreferredSize(BUTTON_DIMENSION);
 		jToggleButtonConstructRobot.addActionListener(new java.awt.event.ActionListener() {
 			public void actionPerformed(java.awt.event.ActionEvent evt) {				
 				MainFrameSeparateController.jButtonConstructRobotActionPerformed(jToggleButtonConstructRobot,jTabbedPaneFirst);
 
 			}
 		});
 		jToolBarGeneralControl.add(jToggleButtonConstructRobot);
 		
 		//jToolBarSeparator3.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
 		jToolBarGeneralControl.add(jToolBarSeparator3);
 		
 		jButtonRunRealTime.setToolTipText(MainFrameComponentsText.RUN_REAL_TIME.getUserFriendlyName());		
 		jButtonRunRealTime.setIcon(MainFrameIcons.RUN_REAL_TIME.getImageIcon());
 		jButtonRunRealTime.setRolloverIcon(MainFrameIcons.RUN_REAL_TIME_ROLLOVER.getImageIcon());		
 		jButtonRunRealTime.setDisabledIcon(MainFrameIcons.RUN_REAL_TIME_DISABLED.getImageIcon());		
 		jButtonRunRealTime.setFocusable(false);    
 		jButtonRunRealTime.setPreferredSize(BUTTON_DIMENSION);      
 		jButtonRunRealTime.addActionListener(new java.awt.event.ActionListener() {
 			public void actionPerformed(java.awt.event.ActionEvent evt) {
 				MainFrameSeparateController.jButtonRunRealTimeActionPerformed();        	  
 			}
 		});
 		jToolBarGeneralControl.add(jButtonRunRealTime);
 
 		jButtonRunFast.setToolTipText(MainFrameComponentsText.RUN_FAST.getUserFriendlyName());
 		jButtonRunFast.setIcon(MainFrameIcons.RUN_FAST.getImageIcon());		
 		jButtonRunFast.setRolloverIcon(MainFrameIcons.RUN_FAST_ROLLOVER.getImageIcon());
 		jButtonRunFast.setDisabledIcon(MainFrameIcons.RUN_FAST_DISABLED.getImageIcon());		
 		jButtonRunFast.setFocusable(false);
 		jButtonRunFast.setPreferredSize(BUTTON_DIMENSION);      
 		jButtonRunFast.addActionListener(new java.awt.event.ActionListener() {
 			public void actionPerformed(java.awt.event.ActionEvent evt) {
 				MainFrameSeparateController.jButtonRunFastActionPerformed();        	  
 			}
 		});
 		jToolBarGeneralControl.add(jButtonRunFast);
 
 		jButtonRunStepByStep.setToolTipText(MainFrameComponentsText.RUN_STEP_BY_STEP.getUserFriendlyName());
 		jButtonRunStepByStep.setIcon(MainFrameIcons.RUN_STEP_BY_STEP.getImageIcon());				
 		jButtonRunStepByStep.setRolloverIcon(MainFrameIcons.RUN_STEP_BY_STEP_ROLLOVER.getImageIcon());
 	    jButtonRunStepByStep.setDisabledIcon(MainFrameIcons.RUN_STEP_BY_STEP_DISABLED.getImageIcon());		
 		jButtonRunStepByStep.setFocusable(false);
 		jButtonRunStepByStep.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
 		jButtonRunStepByStep.setPreferredSize(BUTTON_DIMENSION);
 		jButtonRunStepByStep.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
 		jButtonRunStepByStep.addActionListener(new java.awt.event.ActionListener() {
 			public void actionPerformed(java.awt.event.ActionEvent evt) {
 				MainFrameSeparateController.jButtonRunStepByStepActionPerformed();
 			}
 		});
 		jToolBarGeneralControl.add(jButtonRunStepByStep);
 
 		jButtonPause.setToolTipText(MainFrameComponentsText.PAUSE.getUserFriendlyName());
 		jButtonPause.setIcon(MainFrameIcons.PAUSE.getImageIcon());
 		jButtonPause.setRolloverIcon(MainFrameIcons.PAUSE_ROLLOVER.getImageIcon());
 		jButtonPause.setDisabledIcon(MainFrameIcons.PAUSE_DISABLED.getImageIcon());	
 		jButtonPause.setPreferredSize(BUTTON_DIMENSION);
 		jButtonPause.setFocusable(false);   
 		jButtonPause.addActionListener(new java.awt.event.ActionListener() {
 			public void actionPerformed(java.awt.event.ActionEvent evt) {
 				MainFrameSeparateController.jButtonPauseActionPerformed();
 			}
 		});
 		jToolBarGeneralControl.add(jButtonPause);
 
 		jButtonTerminate.setToolTipText(MainFrameComponentsText.TERMINATE.getUserFriendlyName());
 		jButtonTerminate.setIcon(MainFrameIcons.TERMINATE.getImageIcon());
 		jButtonTerminate.setRolloverIcon(MainFrameIcons.TERMINATE_ROLLOVER.getImageIcon());		
 		jButtonTerminate.setDisabledIcon(MainFrameIcons.TERMINATE_DISABLED.getImageIcon());
 		jButtonTerminate.setPreferredSize(BUTTON_DIMENSION);
 		jButtonTerminate.setFocusable(false);   
 		jButtonTerminate.addActionListener(new java.awt.event.ActionListener() {
 			public void actionPerformed(java.awt.event.ActionEvent evt) {
 				MainFrameSeparateController.jButtonTerminateActionPerformed();
 			}
 		});
 		jToolBarGeneralControl.add(jButtonTerminate);			
 		
 		//jToolBarSeparator4.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
 		jToolBarGeneralControl.add(jToolBarSeparator4);		
 		
 		jToggleButtonMaximizeInteraction.setToolTipText(MainFrameComponentsText.HIDE_DEBUGGING.getUserFriendlyName());
 		jToggleButtonMaximizeInteraction.setIcon(MainFrameIcons.TABBED_PANES.getImageIcon());
 		jToggleButtonMaximizeInteraction.setRolloverIcon(MainFrameIcons.INTERACTION_MAXIMIZED.getImageIcon());
 		jToggleButtonMaximizeInteraction.setSelectedIcon(MainFrameIcons.INTERACTION_MAXIMIZED.getImageIcon()); 
 		jToggleButtonMaximizeInteraction.setSelectedIcon(MainFrameIcons.TABBED_PANES_DISABLED.getImageIcon()); 
 		jToggleButtonMaximizeInteraction.setSelected(false);
 		jToggleButtonMaximizeInteraction.setFocusable(false);
 		jToggleButtonMaximizeInteraction.setPreferredSize(BUTTON_DIMENSION);
 		jToggleButtonMaximizeInteraction.addActionListener(new java.awt.event.ActionListener() {
 			public void actionPerformed(java.awt.event.ActionEvent evt) {
 				MainFrameSeparateController.hideTabbedPanesActionPerformed(jToggleButtonMaximizeDebugging.isSelected(),jToggleButtonMaximizeInteraction.isSelected());
 				mainFrame.repaint();
 			}
 		});
 		
 		jToolBarGeneralControl.add(jToggleButtonMaximizeInteraction);
 		
 		jToggleButtonMaximizeDebugging.setToolTipText(MainFrameComponentsText.HIDE_INTERACTION.getUserFriendlyName());
 		jToggleButtonMaximizeDebugging.setIcon(MainFrameIcons.TABBED_PANES.getImageIcon());
 		jToggleButtonMaximizeDebugging.setRolloverIcon(MainFrameIcons.DEBUGGING_MAXIMIZED.getImageIcon());
 		jToggleButtonMaximizeDebugging.setSelectedIcon(MainFrameIcons.DEBUGGING_MAXIMIZED.getImageIcon());
 		jToggleButtonMaximizeDebugging.setDisabledIcon(MainFrameIcons.TABBED_PANES_DISABLED.getImageIcon());
 		jToggleButtonMaximizeDebugging.setSelected(false);
 		jToggleButtonMaximizeDebugging.setFocusable(false);
 		jToggleButtonMaximizeDebugging.setPreferredSize(BUTTON_DIMENSION);
 		jToggleButtonMaximizeDebugging.addActionListener(new java.awt.event.ActionListener() {
 			public void actionPerformed(java.awt.event.ActionEvent evt) {
 				MainFrameSeparateController.hideTabbedPanesActionPerformed(jToggleButtonMaximizeDebugging.isSelected(),jToggleButtonMaximizeInteraction.isSelected());
 			}
 		});
 	
 		jToolBarGeneralControl.add(jToggleButtonMaximizeDebugging);
 		
 		jToolBarGeneralControl.add(jToolBarSeparator6);
 		
 		jToggleButtonVisualizer.setToolTipText(MainFrameComponentsText.VISUALIZE_COMMUNICATION_OF_MODULES.getUserFriendlyName());
 		jToggleButtonVisualizer.setIcon(MainFrameIcons.VISUALIZER.getImageIcon());
 		jToggleButtonVisualizer.setSelectedIcon(MainFrameIcons.VISUALIZER_ROLLOVER.getImageIcon());		
 		jToggleButtonVisualizer.setRolloverIcon(MainFrameIcons.VISUALIZER_ROLLOVER.getImageIcon());
 	    jToggleButtonVisualizer.setDisabledIcon(MainFrameIcons.VISUALIZER_DISABLED.getImageIcon());
 		jToggleButtonVisualizer.setFocusable(false);
 		jToggleButtonVisualizer.setEnabled(false);
 		jToggleButtonVisualizer.setPreferredSize(BUTTON_DIMENSION);
 		jToggleButtonVisualizer.addActionListener(new java.awt.event.ActionListener() {
 			public void actionPerformed(java.awt.event.ActionEvent evt) {
 				MainFrameSeparateController.jButtonVisualizerActionPerformed(jToggleButtonVisualizer,jTabbedPaneFirst);
 			}
 		});
 		jToolBarGeneralControl.add(jToggleButtonVisualizer);
 
 		//jToolBarSeparator5.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
 		jToolBarGeneralControl.add(jToolBarSeparator5);
 		
 		getContentPane().add(jToolBarGeneralControl);
 	}
 
 	/**
 	 * Deselects the button associated with tabs for construction of modular robot morphology.
 	 */
 	public static void deSelectConstructRobotButton(){
 		if(jToggleButtonConstructRobot.isSelected()){
 			jToggleButtonConstructRobot.doClick();
 		}
 	}
 
 	/**
 	 * Returns the button for starting new (default) simulation.
 	 * @return the button for starting new (default) simulation.
 	 */
 	public static javax.swing.JButton getJButtonNewSimulation() {
 		return jButtonNewSimulation;
 	}
 
 	/**
 	 * Returns the button for controlling the size of  first tabbed pane. 
 	 * @return the button for controlling the size of  first tabbed pane. 
 	 */
 	public static javax.swing.JToggleButton getJToggleButtonMaximizeInteraction() {
 		return jToggleButtonMaximizeInteraction;
 	}
 
 	/**
 	 * Returns the button for controlling the size of second tabbed pane. 
 	 * @return the button for controlling the size of second tabbed pane. 
 	 */
 	public static javax.swing.JToggleButton getJToggleButtonMaximizeDebugging() {
 		return jToggleButtonMaximizeDebugging;
 	}
 
 	/**
 	 * Returns the button for opening Tab called Visualize Communication of modules. 
 	 * @return the button for opening Tab called Visualize Communication of modules.
 	 */
 	public static javax.swing.JToggleButton getJToggleButtonVisualizer() {
 		return jToggleButtonVisualizer;
 	}
 
 	/**
 	 * Returns the button for saving remote simulation. 
 	 * @return the button for saving remote simulation.
 	 */
 	public static javax.swing.JButton getJButtonSave() {
 		return jButtonSave;
 	}
 
 	/**
 	 * Returns the button for opening(loading) simulation situated in general tool bar.
 	 * @return the button for opening(loading) simulation situated in general tool bar.
 	 */
 	public static javax.swing.JButton getJButtonOpen() {
 		return jButtonOpen;
 	}
 
 	/**
 	 * Used for keeping users choice in the Option pane for limiting amount of robots in construct robot tabs. 
 	 */
 	private static int rememberedChoice =-2;// defaul value
 	
 	/**
 	 * Returns the users choice in the Option pane for limiting amount of robots in construct robot tabs. 
 	 * @return the users choice in the Option pane for limiting amount of robots in construct robot tabs. 
 	 */
 	public static int getRememberedChoice() {
 		return rememberedChoice;
 	}
 
 	/**
 	 * Sets the users choice in the Option pane for limiting amount of robots in construct robot tabs. 
 	 * @param rememberedChoice, used for keeping users choice in the Option pane for limiting amount of robots in construct robot tabs. 
 	 */
 	public static void setRememberedChoice(int rememberedChoice) {
 		MainFrames.rememberedChoice = rememberedChoice;
 	}
 	/**
 	 * Initializes and returns the first tabbed pane (from the top in main GUI window).
 	 * @param width, the width of first tabbed pane.
 	 * @param height, the height of first tabbed pane.
 	 * @return the first tabbed pane.
 	 */
 	public javax.swing.JTabbedPane initFirstTabbedPane(int width,int height){
 		
 		jTabbedPaneFirst  = new javax.swing.JTabbedPane();
 		jTabbedPaneFirst.setToolTipText(MainFrameComponentsText.INTERACTION_WITH_SIMULATION_ENVIRONMENT.getUserFriendlyName());
 		jTabbedPaneFirst.setTabLayoutPolicy(javax.swing.JTabbedPane.SCROLL_TAB_LAYOUT);
 		jTabbedPaneFirst.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
 		jTabbedPaneFirst.setPreferredSize(new Dimension(width, height));		
 		jTabbedPaneFirst.setFocusable(false);
 		jTabbedPaneFirst.setEnabled(false);	
 		jTabbedPaneFirst.addChangeListener(new ChangeListener() {
 			@Override
 			public void stateChanged(ChangeEvent evt) {
 			
 				  JTabbedPane pane = (JTabbedPane)evt.getSource();
				 if (pane.getTabCount()<5){
 					 
 				 } else{
 			        int selectedTabIndex = pane.getSelectedIndex();  
 			        String selectedTabTitle = pane.getTitleAt(selectedTabIndex); 
 			       
 				if (selectedTabTitle.equalsIgnoreCase(MainFramesInter.CONSTRUCT_ROBOT_TAB.getTabTitle())||selectedTabTitle.equalsIgnoreCase(MainFramesInter.ASSIGN_LABELS_TAB.getTabTitle())){
 					ConstructionTabs.adaptToNrRobots(rememberedChoice);
 				}
 				 }
 			}
 		});
 		addTabs(tabsFirstTabbedPane,jTabbedPaneFirst);//Plug in tabs in tabbed pane
 		getContentPane().add(jTabbedPaneFirst);	
 		return jTabbedPaneFirst;
 	}
 
 	public static void setFocusOnConstructionTab() {
 	    jTabbedPaneFirst.setSelectedComponent(MainFramesInter.CONSTRUCT_ROBOT_TAB.getJComponent());
 	}
 
 	/**
 	 * Plugs in tabs in tabbed panes.
 	 * @param tabsContainer, the container of tabs.
 	 * @param jTabbedPane, tabbed pane to plug tabs into.
 	 */
 	private void  addTabs(final ArrayList<TabsInter> tabsContainer ,final javax.swing.JTabbedPane jTabbedPane){
 
 		for (int index =0; index < tabsContainer.size(); index++){
 			TabsInter currentTab = tabsContainer.get(index);				
 
 			if (currentTab.getImageIconDirectory()==null &&currentTab.isInitiallyVisible()){			
 				jTabbedPane.addTab(currentTab.getTabTitle(),currentTab.getJComponent());
 
 			}else if (currentTab.isInitiallyVisible()){				
 				jTabbedPane.addTab(currentTab.getTabTitle(),new javax.swing.ImageIcon(currentTab.getImageIconDirectory()),currentTab.getJComponent());
 			}		
 		}	
 	}
 
 
 	/**
 	 * Initializes second tabbed pane from the top in the design of main GUI window.
 	 * @param width, the width of tabbed pane.
 	 * @param height,the height of tabbed pane.
 	 */
 	public javax.swing.JTabbedPane initSecondTabbedPane(int width, int height){
 		jTabbedPaneSecond = new javax.swing.JTabbedPane();
 		jTabbedPaneSecond.setTabLayoutPolicy(javax.swing.JTabbedPane.SCROLL_TAB_LAYOUT);
 		jTabbedPaneSecond.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
 		jTabbedPaneSecond.setPreferredSize(new Dimension(width, height));
 		addTabs(tabsSecondTabbedPane,jTabbedPaneSecond);//Plug in tabs in tabbed pane and check boxes in menu bar		
 		getContentPane().add(jTabbedPaneSecond);
 		return jTabbedPaneSecond;
 	}
 	
 	/**
 	 * Starts the main GUI window (frame).
 	 * Follows strategy pattern. 
 	 */
 	public abstract void activate();
 
 	/**
 	 * Returns second tabbed pane from the top in the design of main GUI window.
 	 * @return second tabbed pane from the top in the design of main GUI window.
 	 */
 	public static javax.swing.JTabbedPane getJTabbedPaneSecond() {
 		return jTabbedPaneSecond;
 	}
 
 	/**
 	 * Returns first tabbed pane from the top in the design of main GUI window.
 	 * @return first tabbed pane from the top in the design of main GUI window.
 	 */
 	public static javax.swing.JTabbedPane getJTabbedPaneFirst() {
 		return jTabbedPaneFirst;
 	}	
 
 	/**
 	 * Controls visibility of the first tabbed pane.
 	 * @param visible, true for being visible.
 	 */
 	public static void setJTabbedPaneFirstVisible(boolean visible) {
      jTabbedPaneFirst.setVisible(visible);
 	}
 	
 	/**
 	 * Controls visibility of the second tabbed pane.
 	 * @param visible, true for being visible.
 	 */
 	public static void setJTabbedPaneSecondVisible(boolean visible) {
 		 jTabbedPaneSecond.setVisible(visible);
 	}
 	
 	/**
 	 * Controls visibility of both tabbed panes.
 	 * @param visible, true for being visible.
 	 */
 	public static void setTabbedPanesVisible(boolean visible){
 		jTabbedPaneFirst.setVisible(visible);
 		jTabbedPaneSecond.setVisible(visible);
 	}
 	
 	/**
 	 * Controls enabling of the first tabbed pane.
 	 * @param enabled, true for being enabled.
 	 */
 	public static void setJTabbedPaneFirstEnabled(boolean enabled) {
 		jTabbedPaneFirst.setEnabled(enabled);
 	}
 
 	/**
 	 * Returns the toolbar for general control of simulation and mainframe.
 	 * @return jToolBarGeneralControl,the toolbar for general control of simulation and mainframe.
 	 */
 	public static javax.swing.JToolBar getJToolBarGeneralControl() {
 		return jToolBarGeneralControl;
 	}
 	
 	/**
 	 * Returns viable width of the frame (excluding border(decoration) and gaps between border and internal components). 
 	 * @return viable width of the frame (excluding border(decoration) and gaps between border and internal components). 
 	 */
 	public static int getMainFrameViableWidth(){
 		return mainFrame.getWidth() -insets.right-insets.left-2*HORIZONTAL_GAPS;
 	}
 	
 	/**
 	 * Returns Gui component for controlling visibility of tabbed pane for debugging.
 	 * @return Gui component for controlling visibility of tabbed pane for debugging.
 	 */
 	public static javax.swing.JCheckBoxMenuItem getJCheckBoxMenuItemDebugging() {
 		return jCheckBoxMenuItemDebugging;
 	}
 
 	/**
 	 * Returns Gui component for controlling visibility of tabbed pane for interaction with simulation.
 	 * @return Gui component for controlling visibility of tabbed pane for interaction with simulation.
 	 */
 	public static javax.swing.JCheckBoxMenuItem getJCheckBoxMenuItemInteraction() {
 		return jCheckBoxMenuItemInteraction;
 	}
 	
 	/**
 	 * Controls the state of selection of components associated with the second tabbed pane.
 	 * @param selected, true for selected.
 	 */
 	public static void setComponentsAssocWithFirstTabbedPaneSelected(boolean selected){
 		jCheckBoxMenuItemInteraction.setSelected(selected);
 		jToggleButtonMaximizeDebugging.setSelected(selected);
 	}
 	
 	/**
 	 * Controls the state of selection of components associated with the second tabbed pane.
 	 * @param selected, true for selected.
 	 */
 	public static void setComponentsAssocWithSecondTabbedPaneSelected(boolean selected){
 		jCheckBoxMenuItemDebugging.setSelected(selected);
 		jToggleButtonMaximizeInteraction.setSelected(selected);
 	}
 	
 	/**
 	 * Returns current instance of main frame (GUI window).
 	 * @return current instance of main frame (GUI window).
 	 */
 	public static MainFrames getMainFrame() {
 		return mainFrame;
 	}
 	
 	/**
 	 *  Sets current instance of main frame (GUI window).
 	 * @param mainFrame, current instance of main frame (GUI window).
 	 */
 	public static void setMainFrame(MainFrames mainFrame) {
 		MainFrames.mainFrame = mainFrame;
 	}
 	
 
 	/*Declaration of MainFrame components*/
 	private static javax.swing.JMenuBar jMenuBarMain;
 	private javax.swing.JMenu jMenuSimulation,jMenuRender,jMenuWindow,jMenuHide;
 
 	private javax.swing.JMenuItem jMenuItemExit, jMenuItemNewSimulation;
 	private static javax.swing.JMenuItem jMenuItemOpenSimulation,jMenuItemSave;
 
 	private javax.swing.JCheckBoxMenuItem jCheckBoxMenuItemPhysics,jCheckBoxMenuItemWireFrame,jCheckBoxMenuBounds,
 	                                     jCheckBoxMenuItemNormals,jCheckBoxMenuItemLights,jCheckBoxMenuBufferDepth;
 
 	private static javax.swing.JCheckBoxMenuItem jCheckBoxMenuItemInteraction,jCheckBoxMenuItemDebugging,
 	                                             jCheckBoxMenuItemDisplayForHints;
 	                                              
 	private javax.swing.JSeparator jSeparator1,jSeparator2,jSeparator3;
 
 	private javax.swing.JToolBar.Separator jToolBarSeparator3,jToolBarSeparator4,jToolBarSeparator5,jToolBarSeparator6;
 
 	private static javax.swing.JToolBar jToolBarGeneralControl;
 	
 	private javax.swing.JButton jButtonRunRealTime,jButtonRunStepByStep,jButtonRunFast,
 	                           jButtonPause,jButtonTerminate,jButtonReloadSimulation;
 	
 	private static javax.swing.JButton jButtonSave,jButtonOpen,jButtonNewSimulation;
 	private static javax.swing.JToggleButton jToggleButtonConstructRobot,jToggleButtonVisualizer,
 	                                         jToggleButtonMaximizeDebugging,jToggleButtonMaximizeInteraction;
 
 	private static javax.swing.JTabbedPane jTabbedPaneFirst,jTabbedPaneSecond;
 }
 
