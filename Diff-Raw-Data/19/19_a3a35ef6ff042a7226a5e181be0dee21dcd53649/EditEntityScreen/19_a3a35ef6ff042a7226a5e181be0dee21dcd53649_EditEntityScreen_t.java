 /**
  * EditEntityScreen
  * 
  * Class representing the screen that allows users to edit properties of
  * grid entities, including triggers and appearance.
  * 
  * @author Willy McHie
  * Wheaton College, CSCI 335, Spring 2013
  */
 
 package edu.wheaton.simulator.gui;
 
 import java.awt.BorderLayout;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.ArrayList;
 
 import javax.swing.*;
 
 public class EditEntityScreen extends Screen {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 4021299442173260142L;
 
 	private JTextField nameField;
 
 	private JColorChooser colorTool;
 
 	private ArrayList<JTextField> fieldNames; 
 
 	private ArrayList<JTextField> fieldValues;
 
 	private ArrayList<JComboBox> fieldTypes;
 
 	private String[] typeNames =  {"Integer", "Double", "String"};
 
 	private ArrayList<JButton> fieldDeleteButtons;
 
 	private ArrayList<JPanel> fieldSubPanels;
 
 	private Component glue;
 
 	private JButton addFieldButton;
 
 	private ArrayList<JTextField> triggerNames;
 
 	private ArrayList<JTextField> triggerPriorities;
 
 	private ArrayList<JTextField> triggerConditions;
 
 	private ArrayList<JTextField> triggerResults;
 
 	private ArrayList<JButton> triggerDeleteButtons;
 
 	public EditEntityScreen(final ScreenManager sm) {
 		super(sm);
 		this.setLayout(new BorderLayout());
 		JLabel label = new JLabel("Edit Entities");
 		label.setHorizontalAlignment(SwingConstants.CENTER);
 		label.setHorizontalTextPosition(SwingConstants.CENTER);
 		JTabbedPane tabs = new JTabbedPane();
 		JPanel lowerPanel = new JPanel();
 		JPanel generalPanel = new JPanel();
 		JPanel fieldPanel = new JPanel();
 		//fieldPanel.setBorder(BorderFactory.createLineBorder(Color.black));
 		JPanel fieldPanel2 = new JPanel();
 		//fieldPanel2.setBorder(BorderFactory.createLineBorder(Color.red));
 		JPanel fieldPanel3 = new JPanel();
 		//fieldPanel3.setBorder(BorderFactory.createLineBorder(Color.green));
 		//JPanel fieldSubPanel = new JPanel();
 		//fieldSubPanel.setBorder(BorderFactory.createLineBorder(Color.blue));
 		JPanel triggerPanel = new JPanel();
 		JPanel triggerPanel2 = new JPanel();
 		JLabel generalLabel = new JLabel("General Info");
 		generalLabel.setHorizontalAlignment(SwingConstants.CENTER);
 		JLabel fieldLabel = new JLabel("Field Info");
 		fieldLabel.setHorizontalAlignment(SwingConstants.CENTER);
 		fieldLabel.setPreferredSize(new Dimension(300, 100));
 		JLabel triggerLabel = new JLabel("Trigger Info");
 		triggerLabel.setHorizontalAlignment(SwingConstants.CENTER);
 		triggerLabel.setHorizontalTextPosition(SwingConstants.CENTER);
 		JLabel nameLabel = new JLabel("Name: ");
 		nameField = new JTextField(25);
 		//set max size of name field
 		nameField.setMaximumSize(new Dimension(400, 40));
 		colorTool = new JColorChooser();
 		JButton loadIconButton = new JButton("Load icon");
 
 		JLabel fieldNameLabel = new JLabel("Field Name");
 		fieldNameLabel.setPreferredSize(new Dimension(200, 30));
 		JLabel fieldValueLabel = new JLabel("Field Initial Value");
 		fieldValueLabel.setPreferredSize(new Dimension(400, 30));
 		JLabel fieldTypeLabel = new JLabel("Field Type");
 		fieldNameLabel.setPreferredSize(new Dimension(350, 30));
 
 		fieldNames = new ArrayList<JTextField>();
 		fieldNames.add(new JTextField(25));
 		//set max size of field name text box
 		fieldNames.get(0).setMaximumSize(new Dimension(300, 40));
 		fieldValues = new ArrayList<JTextField>();
 		fieldValues.add(new JTextField(25));
 		fieldValues.get(0).setMaximumSize(new Dimension(300, 40));
 		fieldTypes = new ArrayList<JComboBox>();
 		fieldTypes.add(new JComboBox(typeNames));
 		fieldTypes.get(0).setMaximumSize(new Dimension(200, 40));
 		fieldDeleteButtons = new ArrayList<JButton>();
 		fieldDeleteButtons.add(new JButton("Delete"));
 		fieldSubPanels = new ArrayList<JPanel>();
 		fieldSubPanels.add(new JPanel());
 		addFieldButton = new JButton("Add Field");
 		addFieldButton.addActionListener(
 				new ActionListener() {
					@Override
 					public void actionPerformed(ActionEvent e) {
 						JPanel newPanel = new JPanel();
 						newPanel.setLayout(
 								new BoxLayout(newPanel, 
 										BoxLayout.X_AXIS)
 								);
 						JTextField newName = new JTextField(25);
 						newName.setMaximumSize(new Dimension(300, 40));
 						fieldNames.add(newName);
 						JComboBox newType = new JComboBox(typeNames);
 						newType.setMaximumSize(new Dimension(200, 40));
 						fieldTypes.add(newType);
 						JTextField newValue = new JTextField(25);
 						newValue.setMaximumSize(new Dimension(300, 40));
 						JButton newButton = new JButton("Delete");
 						//newButton.addActionListener(new DeleteListener());
 						fieldDeleteButtons.add(newButton);
 						newPanel.add(newName);
 						newPanel.add(newType);
 						newPanel.add(newValue);
 						newPanel.add(newButton);
 						fieldSubPanels.add(newPanel);
 						//fieldPanel3.add(newPanel);
 
 					}
 				}
 				);
 		JLabel triggerNameLabel = new JLabel("Trigger Name");
 		JLabel triggerPriorityLabel = new JLabel("Trigger Priority");
 		JLabel triggerConditionLabel = new JLabel("Trigger Condition");
 		JLabel triggerResultLabel = new JLabel("Trigger Result");
 		triggerNames = new ArrayList<JTextField>();
 		triggerNames.add(new JTextField(25));
 		triggerPriorities = new ArrayList<JTextField>();
 		triggerPriorities.add(new JTextField(15));
 		//conditions and results: this will probably change
 		triggerConditions = new ArrayList<JTextField>();
 		triggerConditions.add(new JTextField(50));
 		triggerResults = new ArrayList<JTextField>();
 		triggerResults.add(new JTextField(50));
 		triggerDeleteButtons = new ArrayList<JButton>();
 		triggerDeleteButtons.add(new JButton("Delete"));
 		JButton addTriggerButton = new JButton("Add Trigger");
 		JButton cancelButton = new JButton("Cancel");
 		cancelButton.addActionListener(
 				new ActionListener() {
					@Override
 					public void actionPerformed(ActionEvent e) {
 						sm.update(sm.getScreen("Edit Simulation")); 
 					} 
 				}
 				);
 		JButton finishButton = new JButton("Finish");
 		finishButton.addActionListener(
 				new ActionListener() {
					@Override
 					public void actionPerformed(ActionEvent e) {
 						sm.update(sm.getScreen("Edit Simulation")); 
 					} 
 				}
 				);
 
 		lowerPanel.add(cancelButton);
 		lowerPanel.add(finishButton);
 
 		generalPanel.setLayout(
 				new BoxLayout(generalPanel, BoxLayout.PAGE_AXIS)
 				);
 		generalPanel.add(generalLabel);
 		generalPanel.add(nameLabel);
 		generalPanel.add(nameField);
 		//add the icon construction tool to the general panel here
 		generalPanel.add(colorTool);
 		generalPanel.add(loadIconButton);
 
 		//TODO mess with sizes of components
 		fieldPanel.setLayout(
 				new BorderLayout());
 		JPanel fieldUberPanel = new JPanel();
 		//fieldUberPanel.setBorder(BorderFactory.createLineBorder(Color.yellow));
 		fieldUberPanel.setLayout(
 				new BoxLayout(fieldUberPanel, BoxLayout.Y_AXIS)
 				);
 		fieldPanel2.setLayout(
 				new BoxLayout(fieldPanel2, BoxLayout.X_AXIS)
 				);
 		fieldPanel2.setAlignmentX(LEFT_ALIGNMENT);
 		fieldPanel3.setLayout(
 				new BoxLayout(fieldPanel3, BoxLayout.Y_AXIS)
 				);
 		fieldSubPanels.get(0).setLayout(
 				new BoxLayout(fieldSubPanels.get(0), BoxLayout.X_AXIS)
 				);
 		fieldPanel.add(fieldLabel, BorderLayout.NORTH);
 		fieldLabel.setAlignmentX(CENTER_ALIGNMENT);
 		fieldPanel2.add(Box.createHorizontalGlue());
 		fieldPanel2.add(fieldNameLabel);
 		fieldNameLabel.setHorizontalAlignment(SwingConstants.LEFT);
 		fieldNameLabel.setHorizontalTextPosition(SwingConstants.TRAILING);
 		fieldNameLabel.setAlignmentX(LEFT_ALIGNMENT);
 		fieldPanel2.add(fieldTypeLabel);
 		fieldTypeLabel.setHorizontalAlignment(SwingConstants.LEFT);
 		fieldPanel2.add(fieldValueLabel);
 		fieldPanel2.add(Box.createHorizontalGlue());
 		fieldValueLabel.setHorizontalAlignment(SwingConstants.CENTER);
 		fieldSubPanels.get(0).add(fieldNames.get(0));
 		fieldSubPanels.get(0).add(fieldTypes.get(0));
 		fieldSubPanels.get(0).add(fieldValues.get(0));
 		fieldSubPanels.get(0).add(fieldDeleteButtons.get(0));
 		fieldPanel3.add(fieldSubPanels.get(0));
 		glue = Box.createVerticalGlue();
 		fieldPanel3.add(addFieldButton);
 		fieldPanel3.add(glue);
 		fieldSubPanels.get(0).setAlignmentY(TOP_ALIGNMENT);
 		fieldUberPanel.add(fieldPanel2);
 		fieldPanel2.setAlignmentX(CENTER_ALIGNMENT);
 		fieldUberPanel.add(fieldPanel3);
 		fieldPanel.add(fieldUberPanel, BorderLayout.CENTER);
 		//fieldPanel.add(addFieldButton, BorderLayout.SOUTH);
 
 		triggerPanel.setLayout(
 				new BoxLayout(triggerPanel, BoxLayout.PAGE_AXIS)
 				);
 		triggerPanel2.setLayout(new GridLayout(15, 5));
 		triggerPanel.add(triggerLabel);
 		triggerPanel2.add(triggerNameLabel);
 		triggerPanel2.add(triggerPriorityLabel);
 		triggerPanel2.add(triggerConditionLabel);
 		triggerPanel2.add(triggerResultLabel);
 		triggerPanel.add(triggerPanel2);
 		triggerPanel.add(addTriggerButton);
 
 		tabs.addTab("General", generalPanel);
 		tabs.addTab("Fields", fieldPanel);
 		tabs.addTab("Triggers",  triggerPanel);
 
 		this.add(label, BorderLayout.NORTH);
 		this.add(tabs, BorderLayout.CENTER);
 		this.add(lowerPanel, BorderLayout.SOUTH);
 
 	}
 
 	@Override
 	public void actionPerformed(ActionEvent e) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void sendInfo() {
 		// TODO Auto-generated method stub
 
 	}
 
 }
