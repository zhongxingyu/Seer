 package ussr.aGui.tabs.simulation;
 
 import java.awt.Dimension;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Insets;
 import java.util.Enumeration;
 import java.util.Hashtable;
 
 import javax.swing.BorderFactory;
 import javax.swing.DefaultComboBoxModel;
 import javax.swing.border.TitledBorder;
 import ussr.aGui.FramesInter;
 import ussr.aGui.designHelpers.JComponentsFactory;
 import ussr.aGui.enumerations.tabs.TabsComponentsText;
 import ussr.aGui.enumerations.tabs.TabsIcons;
 import ussr.aGui.tabs.Tabs;
 import ussr.aGui.tabs.controllers.SimulationTabController;
 import ussr.aGui.tabs.simulation.enumerations.CameraPositions;
 import ussr.aGui.tabs.simulation.enumerations.PlaneMaterials;
 import ussr.aGui.tabs.simulation.enumerations.TextureDescriptions;
 import ussr.builder.saveLoadXML.XMLTagsUsed;
 
 /**
  * Contains methods  defining visual appearance of edit value panel in Simulation Tab for each
  * node in tree view. 
  * 
  * NOTE Nr.1: each method is creating new panel, which is later added into panel with title edit value.
  * @author Konstantinas
  *
  */
 public class SimulationTreeEditors{
 	
 
 	/**
 	 * The visual appearance of the editor for node called Robots.
 	 */
 	public final static javax.swing.JPanel ROBOTS_EDITOR =  addRobotsEditor();
 	
 	//public static SimulationSpecification userSimulationSpecification = SimulationTabController.getSimulationSpecification(); 
 	//= new SimulationSpecification();
 	
 
 	/**
 	 * Defines visual appearance of editor panel(edit value) for tree node called physics simulation step size.
 	 * @return jPanelEditor, the editor panel with new components in it.
 	 */
 	public static javax.swing.JPanel addPhysicsSimulationStepSizeEditor() {
 		javax.swing.JPanel jPanelTreeNode = new javax.swing.JPanel();
 		jSpinnerPhysicsSimulationStepSize = new javax.swing.JSpinner();
 		jSpinnerPhysicsSimulationStepSize.setPreferredSize(new Dimension(60,20));
 		jSpinnerPhysicsSimulationStepSize.setModel(new javax.swing.SpinnerNumberModel(Float.valueOf(0.0f), null, null, Float.valueOf(1.0f)));
 		jSpinnerPhysicsSimulationStepSize.addChangeListener(new javax.swing.event.ChangeListener() {
             public void stateChanged(javax.swing.event.ChangeEvent evt) {            	
             	SimulationTabController.getSimulationSpecification().getSimPhysicsParameters().put(XMLTagsUsed.PHYSICS_SIMULATION_STEP_SIZE, jSpinnerPhysicsSimulationStepSize.getValue().toString());
             	//userSimulationSpecification.getSimPhysicsParameters().put(XMLTagsUsed.PHYSICS_SIMULATION_STEP_SIZE, jSpinnerPhysicsSimulationStepSize.getValue().toString());
             	System.out.println("PHYSICS_SIMULATION_STEP_SIZE:"+ jSpinnerPhysicsSimulationStepSize.getValue().toString());
             }
            
         });
 		jPanelTreeNode.add(jSpinnerPhysicsSimulationStepSize);	
 
 		return jPanelTreeNode;
 	}
 	
 	/**
 	 * Defines visual appearance of editor panel(edit value) for tree node called Resolution Factor.
 	 * @return jPanelEditor, the editor panel with new components in it.
 	 */
 	public static javax.swing.JPanel addResolutionFactorEditor() {
 		javax.swing.JPanel jPanelTreeNode = new javax.swing.JPanel();
 		jSpinnerResolutionFactor = new javax.swing.JSpinner();
 		jSpinnerResolutionFactor.setPreferredSize(new Dimension(60,20));
 		jSpinnerResolutionFactor.setModel(new javax.swing.SpinnerNumberModel(1, null, null, 1));
 		jSpinnerResolutionFactor.addChangeListener(new javax.swing.event.ChangeListener() {
             public void stateChanged(javax.swing.event.ChangeEvent evt) {
             	SimulationTabController.getSimulationSpecification().getSimPhysicsParameters().put(XMLTagsUsed.RESOLUTION_FACTOR, jSpinnerResolutionFactor.getValue().toString());
             	//userSimulationSpecification.getSimPhysicsParameters().put(XMLTagsUsed.RESOLUTION_FACTOR, jSpinnerResolutionFactor.getValue().toString());
             	System.out.println("RESOLUTION_FACTOR:"+ jSpinnerResolutionFactor.getValue().toString());
             }           
         });
 		jPanelTreeNode.add(jSpinnerResolutionFactor);	
 		return jPanelTreeNode;
 	}
 	
 	/**
 	 * Defines visual appearance of editor panel(edit value) for tree node called Robots.
 	 * @return jPanelEditor, the editor panel with new components in it.
 	 */
 	public static javax.swing.JPanel  addRobotsEditor(){
 		javax.swing.JPanel jPanelTreeNode = new javax.swing.JPanel();
 		jPanelTreeNode.add(JComponentsFactory.createNewLabel(TabsComponentsText.LOAD_NEW_ROBOT.getUserFriendlyName()));
 		jPanelTreeNode.add(Tabs.initOpenButton());		
 		return jPanelTreeNode;
 	}
 
 
 	public static javax.swing.JTextField getJTextFieldMorphologyLocation() {
 		return jTextFieldMorphologyLocation;
 	}
 	
 	/**
 	 * Defines visual appearance of editor panel(edit value) for tree node called RobotNr....
 	 * @return jPanelEditor, the editor panel with new components in it.
 	 */
 	public static javax.swing.JPanel addRobotEditor(){
 		javax.swing.JPanel jPanelTreeNode = new javax.swing.JPanel(new GridBagLayout());
 		/*Instantiation of components*/
 		jPanelMoveRobot = new javax.swing.JPanel(new GridBagLayout());
 		jPanelMorphologyLocation = new javax.swing.JPanel(new GridBagLayout()); 
 		jTextFieldMorphologyLocation = new javax.swing.JTextField();
 		jScrollPaneMorphologyLocation =  new javax.swing.JScrollPane();
 		jButtonYpositive = new javax.swing.JButton();
 		jButtonYnegative = new javax.swing.JButton();
 		jButtonXpositive = new javax.swing.JButton();
 		jButtonXnegative = new javax.swing.JButton();
 		jButtonZpositive = new javax.swing.JButton();
 		jButtonZnegative = new javax.swing.JButton();
 		jButtonDeleteRobot = new javax.swing.JButton();
 		jSpinnerCoordinateValue = new javax.swing.JSpinner();		
 		
 		/*Description of components*/
 		GridBagConstraints constraintsjPanelMorphologyLocation = new GridBagConstraints();
 		constraintsjPanelMorphologyLocation.fill = GridBagConstraints.CENTER;
 		constraintsjPanelMorphologyLocation.gridx = 0;
 		constraintsjPanelMorphologyLocation.gridy = 0;
 		
		jPanelMorphologyLocation.add(JComponentsFactory.createNewLabel("Morphology file location:"),constraintsjPanelMorphologyLocation);
 		
 		
 		//constraintsjPanelMorphologyLocation.fill = GridBagConstraints.HORIZONTAL;
 		//constraintsjPanelMorphologyLocation.gridx = 1;
 		//constraintsjPanelMorphologyLocation.gridy = 0;
 		//jPanelMorphologyLocation.add(Tabs.initOpenButton());
 		
 		jTextFieldMorphologyLocation.setPreferredSize(new Dimension(800,20));
 		jTextFieldMorphologyLocation.setText(" " );
		jTextFieldMorphologyLocation.setEditable(false);
 		jScrollPaneMorphologyLocation.setPreferredSize(new Dimension(150,40));
 		jScrollPaneMorphologyLocation.setViewportView(jTextFieldMorphologyLocation);
 		
 		constraintsjPanelMorphologyLocation.fill = GridBagConstraints.HORIZONTAL;
 		constraintsjPanelMorphologyLocation.gridx = 0;
 		constraintsjPanelMorphologyLocation.gridy = 1;
 		constraintsjPanelMorphologyLocation.insets = new Insets(5,0,10,0); 
 		
 		jPanelMorphologyLocation.add(jScrollPaneMorphologyLocation,constraintsjPanelMorphologyLocation);
 		
 		GridBagConstraints constraintsJPanelTreeNode = new GridBagConstraints();
 		constraintsJPanelTreeNode.fill = GridBagConstraints.HORIZONTAL;
 		constraintsJPanelTreeNode.gridx = 0;
 		constraintsJPanelTreeNode.gridy = 0;
 		
 		jPanelTreeNode.add(jPanelMorphologyLocation,constraintsJPanelTreeNode);
 		
 		
 		
 		GridBagConstraints constraintsJPanelMoveRobot = new GridBagConstraints();
 		TitledBorder displayTitle1;
 		displayTitle1 = BorderFactory.createTitledBorder(TabsComponentsText.NEW_POSITION.getUserFriendlyName());
 		displayTitle1.setTitleJustification(TitledBorder.CENTER);
 		jPanelMoveRobot.setBorder(displayTitle1);
 		
 		jButtonYpositive.setToolTipText(TabsComponentsText.MOVE_UP.getUserFriendlyName());
 		jButtonYpositive.setIcon(TabsIcons.Y_POSITIVE_BIG.getImageIcon());
 		jButtonYpositive.setRolloverIcon(TabsIcons.Y_POSITIVE_ROLLOVER_BIG.getImageIcon());					
 		jButtonYpositive.setFocusable(false);		
 		jButtonYpositive.setPreferredSize(new Dimension(45,30));
 		jButtonYpositive.addActionListener(new java.awt.event.ActionListener() {
 			public void actionPerformed(java.awt.event.ActionEvent evt) {
 				SimulationTabController.jButtonsCoordinateArrowsActionPerformed(jButtonYpositive);
 			}
 		});
 		constraintsJPanelMoveRobot.fill = GridBagConstraints.HORIZONTAL;
 		constraintsJPanelMoveRobot.gridx = 1;
 		constraintsJPanelMoveRobot.gridy = 0;	
 
 		jPanelMoveRobot.add(jButtonYpositive,constraintsJPanelMoveRobot);
 
 
 		jButtonXnegative.setToolTipText(TabsComponentsText.MOVE_LEFT.getUserFriendlyName());
 		jButtonXnegative.setIcon(TabsIcons.X_NEGATIVE_BIG.getImageIcon());	
 		jButtonXnegative.setRolloverIcon(TabsIcons.X_NEGATIVE_ROLLOVER_BIG.getImageIcon());			
 		jButtonXnegative.setFocusable(false);		
 		jButtonXnegative.setPreferredSize(new Dimension(45,30));
 		jButtonXnegative.addActionListener(new java.awt.event.ActionListener() {
 			public void actionPerformed(java.awt.event.ActionEvent evt) {
 				SimulationTabController.jButtonsCoordinateArrowsActionPerformed(jButtonXnegative);
 			}
 		});
 		constraintsJPanelMoveRobot.fill = GridBagConstraints.HORIZONTAL;
 		constraintsJPanelMoveRobot.gridx = 0;
 		constraintsJPanelMoveRobot.gridy = 1;	
 		jPanelMoveRobot.add(jButtonXnegative,constraintsJPanelMoveRobot);
 
 		jButtonZpositive.setToolTipText(TabsComponentsText.MOVE_CLOSER.getUserFriendlyName());
 		jButtonZpositive.setIcon(TabsIcons.Z_POSITIVE_BIG.getImageIcon());	
 		jButtonZpositive.setRolloverIcon(TabsIcons.Z_POSITIVE_ROLLOVER_BIG.getImageIcon());		
 		jButtonZpositive.setFocusable(false);		
 		jButtonZpositive.setPreferredSize(FramesInter.BUTTON_DIMENSION);
 		jButtonZpositive.addActionListener(new java.awt.event.ActionListener() {
 			public void actionPerformed(java.awt.event.ActionEvent evt) {
 				SimulationTabController.jButtonsCoordinateArrowsActionPerformed(jButtonZpositive);
 			}
 		});
 		constraintsJPanelMoveRobot.fill = GridBagConstraints.HORIZONTAL;
 		constraintsJPanelMoveRobot.gridx = 0;
 		constraintsJPanelMoveRobot.gridy = 2;	
 		jPanelMoveRobot.add(jButtonZpositive,constraintsJPanelMoveRobot);
 
 
 		jSpinnerCoordinateValue = new javax.swing.JSpinner();
 		jSpinnerCoordinateValue.setModel(new javax.swing.SpinnerNumberModel(Float.valueOf(0.1f), Float.valueOf(0.0f), Float.valueOf(100.0f), Float.valueOf(0.01f)));
 		jSpinnerCoordinateValue.setPreferredSize(new Dimension(45,30));
 		jSpinnerCoordinateValue.addChangeListener(new javax.swing.event.ChangeListener() {
 	            public void stateChanged(javax.swing.event.ChangeEvent evt) {
 	              SimulationTabController.setjSpinnerCoordinateValue(Float.parseFloat(jSpinnerCoordinateValue.getValue().toString()));
 	            }
 	        });
 		
 		constraintsJPanelMoveRobot.fill = GridBagConstraints.HORIZONTAL;
 		constraintsJPanelMoveRobot.gridx = 1;
 		constraintsJPanelMoveRobot.gridy = 1;
 
 		jPanelMoveRobot.add(jSpinnerCoordinateValue,constraintsJPanelMoveRobot);
 
 		jButtonZnegative.setToolTipText(TabsComponentsText.MOVE_AWAY.getUserFriendlyName());
 		jButtonZnegative.setIcon(TabsIcons.Z_NEGATIVE_BIG.getImageIcon());				
 		jButtonZnegative.setRolloverIcon(TabsIcons.Z_NEGATIVE_ROLLOVER_BIG.getImageIcon());	
 		jButtonZnegative.setFocusable(false);		
 		jButtonZnegative.setPreferredSize(new Dimension(45,30));
 		jButtonZnegative.addActionListener(new java.awt.event.ActionListener() {
 			public void actionPerformed(java.awt.event.ActionEvent evt) {
 				SimulationTabController.jButtonsCoordinateArrowsActionPerformed(jButtonZnegative);
 			}
 		});
 		constraintsJPanelMoveRobot.fill = GridBagConstraints.HORIZONTAL;
 		constraintsJPanelMoveRobot.gridx = 2;
 		constraintsJPanelMoveRobot.gridy = 0;	
 		jPanelMoveRobot.add(jButtonZnegative,constraintsJPanelMoveRobot);
 
 
 		jButtonXpositive.setToolTipText(TabsComponentsText.MOVE_RIGHT.getUserFriendlyName());
 		jButtonXpositive.setIcon(TabsIcons.X_POSITIVE_BIG.getImageIcon());	
 		jButtonXpositive.setRolloverIcon(TabsIcons.X_POSITIVE_ROLLOVER_BIG.getImageIcon());		
 		jButtonXpositive.setFocusable(false);		
 		jButtonXpositive.setPreferredSize(new Dimension(45,30));
 		jButtonXpositive.addActionListener(new java.awt.event.ActionListener() {
 			public void actionPerformed(java.awt.event.ActionEvent evt) {
 				SimulationTabController.jButtonsCoordinateArrowsActionPerformed(jButtonXpositive);
 			}
 		});
 		constraintsJPanelMoveRobot.fill = GridBagConstraints.HORIZONTAL;
 		constraintsJPanelMoveRobot.gridx = 2;
 		constraintsJPanelMoveRobot.gridy = 1;	
 		jPanelMoveRobot.add(jButtonXpositive,constraintsJPanelMoveRobot);
 
 
 
 		jButtonYnegative.setToolTipText(TabsComponentsText.MOVE_DOWN.getUserFriendlyName());
 		jButtonYnegative.setIcon(TabsIcons.Y_NEGATIVE_BIG.getImageIcon());	
 		jButtonYnegative.setRolloverIcon(TabsIcons.Y_NEGATIVE_ROLLOVER_BIG.getImageIcon());		
 		jButtonYnegative.setFocusable(false);		
 		jButtonYnegative.setPreferredSize(new Dimension(45,30));
 		jButtonYnegative.addActionListener(new java.awt.event.ActionListener() {
 			public void actionPerformed(java.awt.event.ActionEvent evt) {
 				SimulationTabController.jButtonsCoordinateArrowsActionPerformed(jButtonYnegative);
 			}
 		});
 		constraintsJPanelMoveRobot.fill = GridBagConstraints.HORIZONTAL;
 		constraintsJPanelMoveRobot.gridx = 1;
 		constraintsJPanelMoveRobot.gridy = 2;	
 		jPanelMoveRobot.add(jButtonYnegative,constraintsJPanelMoveRobot);
 		
 		
 		constraintsJPanelTreeNode.fill = GridBagConstraints.HORIZONTAL;
 		constraintsJPanelTreeNode.gridx = 0;
 		constraintsJPanelTreeNode.gridy = 1;
 		jPanelTreeNode.add(jPanelMoveRobot,constraintsJPanelTreeNode);
 		
 		jButtonDeleteRobot.setToolTipText(TabsComponentsText.DELETE.getUserFriendlyName());
 		jButtonDeleteRobot.setIcon(TabsIcons.DELETE.getImageIcon());
 		jButtonDeleteRobot.setSelectedIcon(TabsIcons.DELETE.getImageIcon());
 		jButtonDeleteRobot.setRolloverIcon(TabsIcons.DELETE_ROLLOVER.getImageIcon());		
 		jButtonDeleteRobot.setDisabledIcon(TabsIcons.DELETE_DISABLED.getImageIcon());		
 		jButtonDeleteRobot.setFocusable(false);
 		jButtonDeleteRobot.setPreferredSize(FramesInter.BUTTON_DIMENSION);
 		jButtonDeleteRobot.addActionListener(new java.awt.event.ActionListener() {
 			public void actionPerformed(java.awt.event.ActionEvent evt) {
 				SimulationTabController.jButtonDeleteRobotActionPerformed();
 			}
 		});
 		
 		constraintsJPanelTreeNode.fill = GridBagConstraints.CENTER;
 		constraintsJPanelTreeNode.gridx = 0;
 		constraintsJPanelTreeNode.gridy = 2;
 		
 		jPanelTreeNode.add(jButtonDeleteRobot,constraintsJPanelTreeNode);
 		
 		return jPanelTreeNode;
 	}
 
 	
 
 	/**
 	 * Defines visual appearance of editor panel(edit value) for tree node called Plane Size.
 	 * @return jPanelEditor, the editor panel with new components in it.
 	 */
 	public static javax.swing.JPanel addPlaneSizeEditor(){
 		javax.swing.JPanel jPanelTreeNode = new javax.swing.JPanel();
 		jSpinnerPlaneSize = new javax.swing.JSpinner();
 		jSpinnerPlaneSize.setModel(new javax.swing.SpinnerNumberModel(0, 0, 1000, 10));
 		jSpinnerPlaneSize.addChangeListener(new javax.swing.event.ChangeListener() {
             public void stateChanged(javax.swing.event.ChangeEvent evt) {
             	
             	SimulationTabController.getSimulationSpecification().getSimWorldDecsriptionValues().put(XMLTagsUsed.PLANE_SIZE, jSpinnerPlaneSize.getValue().toString());
             	//userSimulationSpecification.getSimWorldDecsriptionValues().put(XMLTagsUsed.PLANE_SIZE, jSpinnerPlaneSize.getValue().toString());
             	System.out.println("PLANE_SIZE:"+ jSpinnerPlaneSize.getValue().toString());
             	
             	/*FOR TESTING*/
             	Hashtable<XMLTagsUsed,String> table =  (Hashtable<XMLTagsUsed,String>)SimulationTabController.getSimulationSpecification().getSimPhysicsParameters();
             	
             	  for (Enumeration<XMLTagsUsed> e =table.keys() ; e.hasMoreElements();)
             	   {
             	       String str = (String) table.get( e.nextElement() );
             	   
             	       System.out.println (str);
             	   }
             }           
         });
 		jPanelTreeNode.add(jSpinnerPlaneSize);
 		
 		
 		return jPanelTreeNode;
 	}
 
 	/**
 	 * Defines visual appearance of editor panel(edit value) for tree node called Plane texture.
 	 * @return jPanelEditor, the editor panel with new components in it.
 	 */
 	public static javax.swing.JPanel addPlaneTextureEditor(){
 		javax.swing.JPanel jPanelTreeNode = new javax.swing.JPanel(new GridBagLayout());
 		GridBagConstraints gridBagConstraintsTexture = new GridBagConstraints();
 
 	    iconLabel = new javax.swing.JLabel();
 	    iconLabel.setSize(100, 100); 
 		
 		jComboBoxPlaneTexture = new javax.swing.JComboBox(); 
 		jComboBoxPlaneTexture.setModel(new DefaultComboBoxModel(TextureDescriptions.getAllInUserFriendlyFromat()));
 		jComboBoxPlaneTexture.addActionListener(new java.awt.event.ActionListener() {
 			public void actionPerformed(java.awt.event.ActionEvent evt) {
 				SimulationTabController.jComboBoxPlaneTextureActionPerformed(jComboBoxPlaneTexture,iconLabel);
 				
 				String selectedTexture = TextureDescriptions.toJavaUSSRConvention(jComboBoxPlaneTexture.getSelectedItem().toString());
 				
 				SimulationTabController.getSimulationSpecification().getSimWorldDecsriptionValues().put(XMLTagsUsed.PLANE_TEXTURE,TextureDescriptions.valueOf(selectedTexture).getRawFileDirectoryName());
 				//userSimulationSpecification.getSimWorldDecsriptionValues().put(XMLTagsUsed.PLANE_TEXTURE,TextureDescriptions.valueOf(selectedTexture).getRawFileDirectoryName());
 				System.out.println("PLANE_TEXTURE:"+ TextureDescriptions.valueOf(selectedTexture).getRawFileDirectoryName());
 			}
 		});
 
 		gridBagConstraintsTexture.fill = GridBagConstraints.CENTER;
 		gridBagConstraintsTexture.gridx = 0;
 		gridBagConstraintsTexture.gridy = 0;
 
 		jPanelTreeNode.add(jComboBoxPlaneTexture,gridBagConstraintsTexture);
 
 		javax.swing.JPanel previewPanel = new javax.swing.JPanel(new GridBagLayout());
 		previewPanel.setPreferredSize(new Dimension(125,140));
 		previewPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createTitledBorder(""), "Preview", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.ABOVE_TOP));
 		gridBagConstraintsTexture.fill = GridBagConstraints.CENTER;
 		gridBagConstraintsTexture.gridx = 0;
 		gridBagConstraintsTexture.gridy = 1;
 		gridBagConstraintsTexture.gridwidth =1;
 		gridBagConstraintsTexture.insets = new Insets(10,0,0,0);	  
 
 		previewPanel.add(iconLabel);
 		jPanelTreeNode.add(previewPanel,gridBagConstraintsTexture);
 		
 		return jPanelTreeNode;
 	}
 
 	/**
 	 * Defines visual appearance of editor panel(edit value) for tree node called Camera position.
 	 * @return jPanelEditor, the editor panel with new components in it.
 	 */
 	public static javax.swing.JPanel addCameraPositionEditor(){
 		javax.swing.JPanel jPanelTreeNode = new javax.swing.JPanel();
 		jComboBoxCameraPosition = new javax.swing.JComboBox(); 
 		jComboBoxCameraPosition.setModel(new DefaultComboBoxModel(CameraPositions.getAllInUserFriendlyFromat()));
 		jComboBoxCameraPosition.addActionListener(new java.awt.event.ActionListener() {
 			public void actionPerformed(java.awt.event.ActionEvent evt) {
 			
 				String selectedCameraPosition = CameraPositions.toJavaUSSRConvention(jComboBoxCameraPosition.getSelectedItem().toString());
 				
 				SimulationTabController.getSimulationSpecification().getSimWorldDecsriptionValues().put(XMLTagsUsed.CAMERA_POSITION,selectedCameraPosition);
 				//userSimulationSpecification.getSimWorldDecsriptionValues().put(XMLTagsUsed.CAMERA_POSITION,selectedCameraPosition);
 				System.out.println("CAMERA_POSITION:"+ selectedCameraPosition);
 				
 			}
 		});
 		
 		jPanelTreeNode.add(jComboBoxCameraPosition);
 		
 		return jPanelTreeNode;
 	}
 
 	/**
 	 * Defines visual appearance of editor panel(edit value) for tree node called The world is flat.
 	 * @return jPanelEditor, the editor panel with new components in it.
 	 */
 	public static javax.swing.JPanel addTheWorldIsFlatEditor(){
 		javax.swing.JPanel jPanelTreeNode = new javax.swing.JPanel();
 		jCheckBoxTheWorldIsFlat =  new javax.swing.JCheckBox ();
 		jCheckBoxTheWorldIsFlat.addActionListener(new java.awt.event.ActionListener() {
 			public void actionPerformed(java.awt.event.ActionEvent evt) {
 				
 				SimulationTabController.getSimulationSpecification().getSimWorldDecsriptionValues().put(XMLTagsUsed.THE_WORLD_IS_FLAT,jCheckBoxTheWorldIsFlat.isSelected()+"");
 				//userSimulationSpecification.getSimWorldDecsriptionValues().put(XMLTagsUsed.THE_WORLD_IS_FLAT,jCheckBoxTheWorldIsFlat.isSelected()+"");
 				System.out.println("THE_WORLD_IS_FLAT:"+ jCheckBoxTheWorldIsFlat.isSelected()+"");
 			}
 		});
 		jPanelTreeNode.add(jCheckBoxTheWorldIsFlat);
 		
 		return jPanelTreeNode;
 	}
 
 	/**
 	 * Defines visual appearance of editor panel(edit value) for tree node called has background scenery.
 	 * @return jPanelEditor, the editor panel with new components in it.
 	 */
 	public static javax.swing.JPanel addHasBackgroundSceneryEditor(){
 		javax.swing.JPanel jPanelTreeNode = new javax.swing.JPanel();
 		jCheckBoxHasBackgroundScenery =  new javax.swing.JCheckBox ();
 		jCheckBoxHasBackgroundScenery.addActionListener(new java.awt.event.ActionListener() {
 			public void actionPerformed(java.awt.event.ActionEvent evt) {
 				SimulationTabController.getSimulationSpecification().getSimWorldDecsriptionValues().put(XMLTagsUsed.HAS_BACKGROUND_SCENERY,jCheckBoxHasBackgroundScenery.isSelected()+"");
 				//userSimulationSpecification.getSimWorldDecsriptionValues().put(XMLTagsUsed.HAS_BACKGROUND_SCENERY,jCheckBoxHasBackgroundScenery.isSelected()+"");
 				System.out.println("HAS_BACKGROUND_SCENERY:"+ jCheckBoxHasBackgroundScenery.isSelected()+"");
 			}
 		});
 		jPanelTreeNode.add(jCheckBoxHasBackgroundScenery);
 		return jPanelTreeNode;
 	}
 
 	/**
 	 * Defines visual appearance of editor panel(edit value) for tree node called has heavy obstacles.
 	 * @return jPanelEditor, the editor panel with new components in it.
 	 */
 	public static javax.swing.JPanel addHasHeavyObstaclesEditor(){
 		javax.swing.JPanel jPanelTreeNode = new javax.swing.JPanel();
 		jCheckBoxHasHeavyObstacles = new javax.swing.JCheckBox ();
 		jCheckBoxHasHeavyObstacles.addActionListener(new java.awt.event.ActionListener() {
 			public void actionPerformed(java.awt.event.ActionEvent evt) {				
 				SimulationTabController.getSimulationSpecification().getSimWorldDecsriptionValues().put(XMLTagsUsed.HAS_HEAVY_OBSTACLES,jCheckBoxHasHeavyObstacles.isSelected()+"");
 				//userSimulationSpecification.getSimWorldDecsriptionValues().put(XMLTagsUsed.HAS_HEAVY_OBSTACLES,jCheckBoxHasHeavyObstacles.isSelected()+"");
 				System.out.println("HAS_HEAVY_OBSTACLES:"+ jCheckBoxHasHeavyObstacles.isSelected()+"");
 			}
 		});
 		jPanelTreeNode.add(jCheckBoxHasHeavyObstacles);
 		return jPanelTreeNode;
 	}
 
 	/**
 	 * Defines visual appearance of editor panel(edit value) for tree node called is frame grabbing active.
 	 * @return jPanelEditor, the editor panel with new components in it.
 	 */
 	public static javax.swing.JPanel addIsFrameGrabbingActiveEditor(){
 		javax.swing.JPanel jPanelTreeNode = new javax.swing.JPanel();
 		jCheckBoxIsFrameGrabbingActive = new javax.swing.JCheckBox ();
 		jCheckBoxIsFrameGrabbingActive.addActionListener(new java.awt.event.ActionListener() {
 			public void actionPerformed(java.awt.event.ActionEvent evt) {				
 				SimulationTabController.getSimulationSpecification().getSimWorldDecsriptionValues().put(XMLTagsUsed.IS_FRAME_GRABBING_ACTIVE,jCheckBoxIsFrameGrabbingActive.isSelected()+"");
 				//userSimulationSpecification.getSimWorldDecsriptionValues().put(XMLTagsUsed.IS_FRAME_GRABBING_ACTIVE,jCheckBoxIsFrameGrabbingActive.isSelected()+"");
 				System.out.println("IS_FRAME_GRABBING_ACTIVE:"+ jCheckBoxIsFrameGrabbingActive.isSelected()+"");
 			}
 		});
 		jPanelTreeNode.add(jCheckBoxIsFrameGrabbingActive);
 		return jPanelTreeNode;
 	}
 
 	/**
 	 * Defines visual appearance of editor panel(edit value) for tree node called damping.
 	 * @return jPanelEditor, the editor panel with new components in it.
 	 */
 	public static javax.swing.JPanel addDampingEditor(){
 		GridBagConstraints gridBagConstraintsDamping = new GridBagConstraints();
 		javax.swing.JPanel jPanelTreeNode = new javax.swing.JPanel(new GridBagLayout());	
 
 		gridBagConstraintsDamping.fill = GridBagConstraints.HORIZONTAL;
 		gridBagConstraintsDamping.gridx = 0;
 		gridBagConstraintsDamping.gridy = 1;
 		gridBagConstraintsDamping.gridwidth = 1;
 		gridBagConstraintsDamping.insets = new Insets(20,0,0,0);
 
 		jPanelTreeNode.add(JComponentsFactory.createNewLabel("Linear velocity"),gridBagConstraintsDamping);
 
 		jSpinnerDampingLinearVelocity = new javax.swing.JSpinner();
 		jSpinnerDampingLinearVelocity.setPreferredSize(new Dimension(60,20));
 		jSpinnerDampingLinearVelocity.setModel(new javax.swing.SpinnerNumberModel(Float.valueOf(0.0f), null, null, Float.valueOf(1.0f)));
 		jSpinnerDampingLinearVelocity.addChangeListener(new javax.swing.event.ChangeListener() {
             public void stateChanged(javax.swing.event.ChangeEvent evt) {            	
             	SimulationTabController.getSimulationSpecification().getSimPhysicsParameters().put(XMLTagsUsed.WORLD_DAMPING_LINEAR_VELOCITY, jSpinnerDampingLinearVelocity.getValue().toString());
             	//userSimulationSpecification.getSimPhysicsParameters().put(XMLTagsUsed.WORLD_DAMPING_LINEAR_VELOCITY, jSpinnerDampingLinearVelocity.getValue().toString());
             	System.out.println("WORLD_DAMPING_LINEAR_VELOCITY:"+ jSpinnerDampingLinearVelocity.getValue().toString());
             }           
         });
 		gridBagConstraintsDamping.fill = GridBagConstraints.HORIZONTAL;
 		gridBagConstraintsDamping.gridx = 1;
 		gridBagConstraintsDamping.gridy = 1;
 		jPanelTreeNode.add(jSpinnerDampingLinearVelocity,gridBagConstraintsDamping);
 
 		gridBagConstraintsDamping.fill = GridBagConstraints.HORIZONTAL;
 		gridBagConstraintsDamping.gridx = 0;
 		gridBagConstraintsDamping.gridy = 2;
 		gridBagConstraintsDamping.insets = new Insets(10,0,0,0);
 		jPanelTreeNode.add(JComponentsFactory.createNewLabel("Angular velocity"),gridBagConstraintsDamping);
 
 		jSpinnerDampingAngularVelocity = new javax.swing.JSpinner();
 		jSpinnerDampingAngularVelocity.setPreferredSize(new Dimension(60,20));
 		jSpinnerDampingAngularVelocity.setModel(new javax.swing.SpinnerNumberModel(Float.valueOf(0.0f), null, null, Float.valueOf(1.0f)));
 		jSpinnerDampingAngularVelocity.addChangeListener(new javax.swing.event.ChangeListener() {
             public void stateChanged(javax.swing.event.ChangeEvent evt) {            	
             	SimulationTabController.getSimulationSpecification().getSimPhysicsParameters().put(XMLTagsUsed.WORLD_DAMPING_ANGULAR_VELOCITY, jSpinnerDampingAngularVelocity.getValue().toString());
             	//userSimulationSpecification.getSimPhysicsParameters().put(XMLTagsUsed.WORLD_DAMPING_ANGULAR_VELOCITY, jSpinnerDampingAngularVelocity.getValue().toString());
             	System.out.println("WORLD_DAMPING_ANGULAR_VELOCITY:"+ jSpinnerDampingAngularVelocity.getValue().toString());
             }           
         });
 		gridBagConstraintsDamping.fill = GridBagConstraints.HORIZONTAL;
 		gridBagConstraintsDamping.gridx = 1;
 		gridBagConstraintsDamping.gridy = 2;
 		gridBagConstraintsDamping.insets = new Insets(10,0,0,0);
 		jPanelTreeNode.add(jSpinnerDampingAngularVelocity,gridBagConstraintsDamping);	
 		return jPanelTreeNode;
 	}
 
 
 	
 
 	/**
 	 * Defines visual appearance of editor panel(edit value) for tree node called Realistic Collision.
 	 * @return jPanelEditor, the editor panel with new components in it.
 	 */
 	public static javax.swing.JPanel addRealisticCollisionEditor() {
 		javax.swing.JPanel jPanelTreeNode = new javax.swing.JPanel();
 		jCheckBoxRealisticCollision = new javax.swing.JCheckBox ();
 		jCheckBoxRealisticCollision.addActionListener(new java.awt.event.ActionListener() {
 			public void actionPerformed(java.awt.event.ActionEvent evt) {				
 				SimulationTabController.getSimulationSpecification().getSimPhysicsParameters().put(XMLTagsUsed.REALISTIC_COLLISION,jCheckBoxRealisticCollision.isSelected()+"");
 				//userSimulationSpecification.getSimPhysicsParameters().put(XMLTagsUsed.REALISTIC_COLLISION,jCheckBoxRealisticCollision.isSelected()+"");
 				System.out.println("REALISTIC_COLLISION:"+ jCheckBoxRealisticCollision.isSelected()+"");
 			}
 		});
 		jPanelTreeNode.add(jCheckBoxRealisticCollision);
 		return jPanelTreeNode;
 	}
 
 	/**
 	 * Defines visual appearance of editor panel(edit value) for tree node called gravity.
 	 * @return jPanelEditor, the editor panel with new components in it.
 	 */
 	public static javax.swing.JPanel addGravityEditor() {
 		javax.swing.JPanel jPanelTreeNode = new javax.swing.JPanel();
 		jSpinnerGravity = new javax.swing.JSpinner();
 		jSpinnerGravity.setPreferredSize(new Dimension(60,20));
 		jSpinnerGravity.setModel(new javax.swing.SpinnerNumberModel(Float.valueOf(-100.0f), null, null, Float.valueOf(1.0f)));
 		jSpinnerGravity.addChangeListener(new javax.swing.event.ChangeListener() {
             public void stateChanged(javax.swing.event.ChangeEvent evt) {            	
             	SimulationTabController.getSimulationSpecification().getSimPhysicsParameters().put(XMLTagsUsed.GRAVITY, jSpinnerGravity.getValue().toString());
             	//userSimulationSpecification.getSimPhysicsParameters().put(XMLTagsUsed.GRAVITY, jSpinnerGravity.getValue().toString());
             	System.out.println("GRAVITY:"+ jSpinnerGravity.getValue().toString());
             }           
         });
 		jPanelTreeNode.add(jSpinnerGravity);	
 		return jPanelTreeNode;
 	}
 	
 	/**
 	 * Defines visual appearance of editor panel(edit value) for tree node called Plane material.
 	 * @return jPanelEditor, the editor panel with new components in it.
 	 */
 	public static javax.swing.JPanel addPlaneMaterialEditor() {
 		javax.swing.JPanel jPanelTreeNode = new javax.swing.JPanel();
 		jComboBoxPlaneMaterial = new javax.swing.JComboBox(); 
 		jComboBoxPlaneMaterial.setModel(new DefaultComboBoxModel(PlaneMaterials.getAllInUserFriendlyFromat()));
 		jComboBoxPlaneMaterial.addActionListener(new java.awt.event.ActionListener() {
 			public void actionPerformed(java.awt.event.ActionEvent evt) {				
 				PlaneMaterials selectedPlaneMaterial = PlaneMaterials.valueOf(PlaneMaterials.toJavaUSSRConvention(jComboBoxPlaneMaterial.getSelectedItem().toString()));
 				SimulationTabController.getSimulationSpecification().getSimPhysicsParameters().put(XMLTagsUsed.PLANE_MATERIAL,selectedPlaneMaterial.toString());
 				//userSimulationSpecification.getSimPhysicsParameters().put(XMLTagsUsed.PLANE_MATERIAL,selectedPlaneMaterial.toString());
 				System.out.println("PLANE_MATERIAL:"+ selectedPlaneMaterial.toString());
 			}
 		});
 		jPanelTreeNode.add(jComboBoxPlaneMaterial);
 		return jPanelTreeNode;
 	}
 	
 	
 	/**
 	 * Defines visual appearance of editor panel(edit value) for tree node called Maintain rotational joint positions.
 	 * @return jPanelEditor, the editor panel with new components in it.
 	 */
 	public static javax.swing.JPanel addMaintainRotationalJointPositionsEditor() {
 		javax.swing.JPanel jPanelTreeNode = new javax.swing.JPanel();
 		jCheckBoxMaintainRotJointPositions = new javax.swing.JCheckBox ();
 		jCheckBoxMaintainRotJointPositions.addActionListener(new java.awt.event.ActionListener() {
 			public void actionPerformed(java.awt.event.ActionEvent evt) {				
 				SimulationTabController.getSimulationSpecification().getSimPhysicsParameters().put(XMLTagsUsed.MAINTAIN_ROTATIONAL_JOINT_POSITIONS,jCheckBoxMaintainRotJointPositions.isSelected()+"");
 				//userSimulationSpecification.getSimPhysicsParameters().put(XMLTagsUsed.MAINTAIN_ROTATIONAL_JOINT_POSITIONS,jCheckBoxMaintainRotJointPositions.isSelected()+"");
 				System.out.println("MAINTAIN_ROTATIONAL_JOINT_POSITIONS:"+ jCheckBoxMaintainRotJointPositions.isSelected()+"");
 			}
 		});
 		jPanelTreeNode.add(jCheckBoxMaintainRotJointPositions);
 		return jPanelTreeNode;
 	}
 	
 
 	/**
 	 * Defines visual appearance of editor panel(edit value) for tree node called Constraint Force Mix.
 	 * @return jPanelEditor, the editor panel with new components in it.
 	 */
 	public static javax.swing.JPanel addConstraintForceMixEditor() {
 		javax.swing.JPanel jPanelTreeNode = new javax.swing.JPanel();
 		jSpinnerConstraintForceMix = new javax.swing.JSpinner();
 		jSpinnerConstraintForceMix.setPreferredSize(new Dimension(60,20));
 		jSpinnerConstraintForceMix.setModel(new javax.swing.SpinnerNumberModel(Float.valueOf(0.0f), null, null, Float.valueOf(1.0f)));
 		jSpinnerConstraintForceMix.addChangeListener(new javax.swing.event.ChangeListener() {
             public void stateChanged(javax.swing.event.ChangeEvent evt) {            	
             	SimulationTabController.getSimulationSpecification().getSimPhysicsParameters().put(XMLTagsUsed.CONSTRAINT_FORCE_MIX, jSpinnerConstraintForceMix.getValue().toString());
             	//userSimulationSpecification.getSimPhysicsParameters().put(XMLTagsUsed.CONSTRAINT_FORCE_MIX, jSpinnerConstraintForceMix.getValue().toString());
             	System.out.println("CONSTRAINT_FORCE_MIX:"+ jSpinnerConstraintForceMix.getValue().toString());
             }           
         });
 		jPanelTreeNode.add(jSpinnerConstraintForceMix);
 		return jPanelTreeNode;
 	}
 
 	/**
 	 * Defines visual appearance of editor panel(edit value) for tree node called Error Reduction Parameter.
 	 * @return jPanelEditor, the editor panel with new components in it.
 	 */
 	public static javax.swing.JPanel addErrorReductionParameterEditor() {
 		javax.swing.JPanel jPanelTreeNode = new javax.swing.JPanel();
 		jSpinnerErrorReductionParameter = new javax.swing.JSpinner();
 		jSpinnerErrorReductionParameter.setPreferredSize(new Dimension(60,20));
 		jSpinnerErrorReductionParameter.setModel(new javax.swing.SpinnerNumberModel(Float.valueOf(0.0f), Float.valueOf(0.0f), Float.valueOf(1.0f), Float.valueOf(0.1f)));
 		jSpinnerErrorReductionParameter.addChangeListener(new javax.swing.event.ChangeListener() {
             public void stateChanged(javax.swing.event.ChangeEvent evt) {
             	SimulationTabController.getSimulationSpecification().getSimPhysicsParameters().put(XMLTagsUsed.ERROR_REDUCTION_PARAMETER, jSpinnerErrorReductionParameter.getValue().toString());
             	//userSimulationSpecification.getSimPhysicsParameters().put(XMLTagsUsed.ERROR_REDUCTION_PARAMETER, jSpinnerErrorReductionParameter.getValue().toString());
             	System.out.println("ERROR_REDUCTION_PARAMETER:"+ jSpinnerErrorReductionParameter.getValue().toString());
             }           
         });
 		jPanelTreeNode.add(jSpinnerErrorReductionParameter);	
 		return jPanelTreeNode;
 	}
 
 	/**
 	 * Defines visual appearance of editor panel(edit value) for tree node called Use Module Event Queue.
 	 * @return jPanelEditor, the editor panel with new components in it.
 	 */
 	public static javax.swing.JPanel addUseModuleEventQueueEditor() {
 		javax.swing.JPanel jPanelTreeNode = new javax.swing.JPanel();
 		jCheckBoxUseMouseEventQueue = new javax.swing.JCheckBox ();
 		jCheckBoxUseMouseEventQueue.addActionListener(new java.awt.event.ActionListener() {
 			public void actionPerformed(java.awt.event.ActionEvent evt) {
 				SimulationTabController.getSimulationSpecification().getSimPhysicsParameters().put(XMLTagsUsed.USE_MODULE_EVENT_QUEUE,jCheckBoxUseMouseEventQueue.isSelected()+"");
 				//userSimulationSpecification.getSimPhysicsParameters().put(XMLTagsUsed.USE_MODULE_EVENT_QUEUE,jCheckBoxUseMouseEventQueue.isSelected()+"");
 				System.out.println("USE_MODULE_EVENT_QUEUE:"+ jCheckBoxUseMouseEventQueue.isSelected()+"");
 			}
 		});
 		jPanelTreeNode.add(jCheckBoxUseMouseEventQueue);
 		return jPanelTreeNode;
 	}
 
 	/**
 	 * Defines visual appearance of editor panel(edit value) for tree node called Synchronize With Controllers.
 	 * @return jPanelEditor, the editor panel with new components in it.
 	 */
 	public static  javax.swing.JPanel addSynchronizeWithControllersEditor() {
 		javax.swing.JPanel jPanelTreeNode = new javax.swing.JPanel();
 		jCheckBoxSynchronizeWithControllers = new javax.swing.JCheckBox ();
 		jCheckBoxSynchronizeWithControllers.addActionListener(new java.awt.event.ActionListener() {
 			public void actionPerformed(java.awt.event.ActionEvent evt) {				
 				SimulationTabController.getSimulationSpecification().getSimPhysicsParameters().put(XMLTagsUsed.SYNC_WITH_CONTROLLERS,jCheckBoxSynchronizeWithControllers.isSelected()+"");
 				//userSimulationSpecification.getSimPhysicsParameters().put(XMLTagsUsed.SYNC_WITH_CONTROLLERS,jCheckBoxSynchronizeWithControllers.isSelected()+"");
 				System.out.println("SYNC_WITH_CONTROLLERS:"+ jCheckBoxSynchronizeWithControllers.isSelected()+"");
 			}
 		});
 		jPanelTreeNode.add(jCheckBoxSynchronizeWithControllers);	
 		return jPanelTreeNode;
 	}
 
 	/**
 	 * Defines visual appearance of editor panel(edit value) for tree node called Physics Simulation Controller Step Factor.
 	 * @return jPanelEditor, the editor panel with new components in it.
 	 */
 	public static javax.swing.JPanel addPhysicsSimulationControllerStepFactor() {
 		javax.swing.JPanel jPanelTreeNode = new javax.swing.JPanel();
 		jSpinnerPhysicsSimContStepFactor = new javax.swing.JSpinner();
 		jSpinnerPhysicsSimContStepFactor.setPreferredSize(new Dimension(60,20));
 		jSpinnerPhysicsSimContStepFactor.setModel(new javax.swing.SpinnerNumberModel(1, null, null, 1));
 		jSpinnerPhysicsSimContStepFactor.addChangeListener(new javax.swing.event.ChangeListener() {
             public void stateChanged(javax.swing.event.ChangeEvent evt) {
             	
             	SimulationTabController.getSimulationSpecification().getSimPhysicsParameters().put(XMLTagsUsed.PHYSICS_SIMULATION_CONTROLLER_STEP_FACTOR, jSpinnerPhysicsSimContStepFactor.getValue().toString());
             	//userSimulationSpecification.getSimPhysicsParameters().put(XMLTagsUsed.PHYSICS_SIMULATION_CONTROLLER_STEP_FACTOR, jSpinnerPhysicsSimContStepFactor.getValue().toString());
             	System.out.println("PHYSICS_SIMULATION_CONTROLLER_STEP_FACTOR:"+ jSpinnerPhysicsSimContStepFactor.getValue().toString());
             }           
         });
 		jPanelTreeNode.add(jSpinnerPhysicsSimContStepFactor);
 		return jPanelTreeNode;
 	}
 
 	public static void update (){
 		
 		/*World Description*/
 		SimulationTabController.setJSpinnerPlaneSizeValue(jSpinnerPlaneSize);
 		SimulationTabController.setValuejSpinnerPhysicsSimulationStepSize(jSpinnerPhysicsSimulationStepSize);
 		SimulationTabController.setSelectedJComboBoxPlaneTexture(jComboBoxPlaneTexture,iconLabel);
 		SimulationTabController.setSelectedJComboBoxCameraPosition(jComboBoxCameraPosition);
 		SimulationTabController.setSelectedJCheckBoxTheWorldIsFlat(jCheckBoxTheWorldIsFlat);
 		SimulationTabController.setSelectedJCheckBoxHasBackgroundScenery(jCheckBoxHasBackgroundScenery);
 		SimulationTabController.setSelectedjCheckBoxHasHeavyObstacles(jCheckBoxHasHeavyObstacles);
 		SimulationTabController.setSelectedJCheckBoxIsFrameGrabbingActive(jCheckBoxIsFrameGrabbingActive);
 		
 		/*Physics parameters*/
 		SimulationTabController.setValuejSpinnerGravity(jSpinnerGravity);	
 		SimulationTabController.setValuejSpinnerDampingLinearVelocity(jSpinnerDampingLinearVelocity);
 		SimulationTabController.setValuejSpinnerDampingAngularVelocity(jSpinnerDampingAngularVelocity);
 		SimulationTabController.setValuejSpinnerGravity(jSpinnerGravity);
 		SimulationTabController.setSelectedJCheckBoxRealisticCollision(jCheckBoxRealisticCollision);		
 		SimulationTabController.setValuejComboBoxPlaneMaterial(jComboBoxPlaneMaterial);
 		SimulationTabController.setSelectedjCheckBoxMaintainRotJointPositions(jCheckBoxMaintainRotJointPositions);
 		SimulationTabController.setValuejSpinnerConstraintForceMix(jSpinnerConstraintForceMix);
 		SimulationTabController.setValueJSpinnerErrorReductionParameter(jSpinnerErrorReductionParameter);		
 		SimulationTabController.setValueJSpinnerResolutionFactor(jSpinnerResolutionFactor);		
 		SimulationTabController.setSelectedJCheckBoxUseMouseEventQueue(jCheckBoxUseMouseEventQueue);		
 		SimulationTabController.setSelectedjCheckBoxSynchronizeWithControllers(jCheckBoxSynchronizeWithControllers);		
 		SimulationTabController.setValuejPhysicsSimulationControllerStepFactor(jSpinnerPhysicsSimContStepFactor);
 		
 	}
 
 /*	public static void addMorphologyEditor() {
 		Map<String,String> fileDescriptionsAndExtensions= new HashMap<String,String>();
 		fileDescriptionsAndExtensions.put(FileChooserFrameInter.ROBOT_FILE_DESCRIPTION, FileChooserFrameInter.DEFAULT_FILE_EXTENSION);
 		//System.out.println("BOOO"+RobotSpecification.getMorphologyLocation());
 
 		//FileChooserFrameInter fcOpenFrame = new FileChooserOpenFrame(fileDescriptionsAndExtensions,FileChooserFrameInter.FC_XML_CONTROLLER,TemporaryRobotSpecification.getMorphologyLocation());
 		//fcOpenFrame.setSelectedFile(new File("some.xml"));
 
 		//jPanelEditor.add(MainFrames.initOpenButton(fcOpenFrame));
 	}*/
 
 
 	private static javax.swing.JButton jButtonYpositive,jButtonYnegative,
 	jButtonXpositive,jButtonXnegative,jButtonZpositive,jButtonZnegative,jButtonDeleteRobot;
 	
 	private static javax.swing.JScrollPane jScrollPaneMorphologyLocation;
 
 	private static javax.swing.JComboBox jComboBoxCameraPosition,jComboBoxPlaneMaterial,jComboBoxPlaneTexture;
 
 	private static javax.swing.JCheckBox jCheckBoxTheWorldIsFlat,jCheckBoxHasBackgroundScenery, jCheckBoxHasHeavyObstacles,
 	jCheckBoxIsFrameGrabbingActive,jCheckBoxRealisticCollision,jCheckBoxUseMouseEventQueue,
 	jCheckBoxSynchronizeWithControllers,jCheckBoxMaintainRotJointPositions;
     
     private static javax.swing.JTextField jTextFieldMorphologyLocation;
 	
 	private static javax.swing.JLabel iconLabel; 
 	private static javax.swing.JPanel jPanelMoveRobot,jPanelMorphologyLocation;
 	private static javax.swing.JSpinner jSpinnerPlaneSize,jSpinnerDampingLinearVelocity, jSpinnerDampingAngularVelocity,
 	jSpinnerPhysicsSimulationStepSize, jSpinnerGravity,jSpinnerConstraintForceMix,
 	jSpinnerErrorReductionParameter, jSpinnerResolutionFactor, jSpinnerPhysicsSimContStepFactor,
 	jSpinnerCoordinateValue;
 	
 	
 }
