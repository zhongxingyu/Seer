 package edu.wheaton.simulator.gui;
 
 import java.awt.Color;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.util.ArrayList;
 
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JLabel;
 import javax.swing.JScrollPane;
 import javax.swing.JSpinner;
 import javax.swing.JTextField;
 
 /**
  * 
  * @author daniel.davenport daniel.gill
  *
  */
 public class EditTriggerScreen extends Screen {
 
 	private static final long serialVersionUID = 3261558461232576081L;
 
 	private JButton addConditional;
 	
 	private JButton addBehavior;
 	
 	private JTextField nameField;
 	
 	private JSpinner prioritySpinner;
 	
 	private ArrayList<JComboBox> conditionals;
 	
 	private ArrayList<JComboBox> behaviors;
 	
 	private JScrollPane conditionalLayout;
 	
 	private JScrollPane behaviorLayout;
 	
 	public EditTriggerScreen(Manager sm) {
 		super(sm);
 		this.setLayout(new GridBagLayout());
 		GridBagConstraints constraints = new GridBagConstraints();
 		addNameField(constraints);
 		addSpinner(constraints); 
 		addIf(constraints); 
 		addConditionsLayout(constraints);
 		addThen(constraints);
 		addBehaviorLayout(constraints);		
 	}
 
 	private void addBehaviorLayout(GridBagConstraints constraints) {
 		behaviorLayout = new JScrollPane();
 		constraints.gridwidth = 3; 
 		constraints.gridheight = 1;
 		constraints.gridx = 0;
 		constraints.gridy = 4; 
 		behaviorLayout.setBackground(Color.black);
 		add(behaviorLayout, constraints);
 	}
 
 	private void addThen(GridBagConstraints constraints) {
 		JLabel thenLabel = new JLabel("Then:"); 
 		constraints.gridwidth = 1; 
 		constraints.gridheight = 1;
 		constraints.gridx = 0;
 		constraints.gridy = 3; 
 		add(thenLabel, constraints);
 		
 	}
 
 	private void addConditionsLayout(GridBagConstraints constraints) {
 		conditionalLayout = new JScrollPane();
 		constraints.gridwidth = 3; 
 		constraints.gridheight = 1;
 		constraints.gridx = 0;
 		constraints.gridy = 2; 
 		conditionalLayout.setBackground(Color.blue);
 		add(conditionalLayout, constraints);
 	}
 
 	private void addIf(GridBagConstraints constraints) {
 		JLabel ifLabel = new JLabel("If:"); 
 		constraints.gridwidth = 1; 
 		constraints.gridheight = 1;
 		constraints.gridx = 0;
 		constraints.gridy = 1;  
 		add(ifLabel, constraints);
 	}
 
 	private void addSpinner(GridBagConstraints constraints) {
 		prioritySpinner = new JSpinner(); 
 		constraints.gridwidth = 1; 
 		constraints.gridheight = 1;
 		constraints.gridx = 2;
 		constraints.gridy = 0; 
 		//TODO: Set minimum value of spinner to 0. 
 		add(prioritySpinner, constraints);
 	}
 
 	private void addNameField(GridBagConstraints constraints){
 		nameField = new JTextField();
 		constraints.gridheight = 1; 
 		constraints.gridwidth = 2; 
 		constraints.gridx = 0; 
 		constraints.gridy = 0; 
 		this.add(nameField, constraints);
 	}
 
 	@Override
 	public void load() {
 		// TODO Auto-generated method stub
 
 	}
 	
 
 }
