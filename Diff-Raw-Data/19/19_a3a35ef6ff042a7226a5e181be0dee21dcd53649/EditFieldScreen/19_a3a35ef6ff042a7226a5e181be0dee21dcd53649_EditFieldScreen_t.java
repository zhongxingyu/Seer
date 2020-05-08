 /**
  * EditFieldScreen
  * 
  * Class representing the screen that allows users to edit
  * the properties of a local or global field.
  * 
  * @author Willy McHie
  * Wheaton College, CSCI 335, Spring 2013
  */
 
 package edu.wheaton.simulator.gui;
 
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.*;
 
 public class EditFieldScreen extends Screen {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 8001531208716520432L;
 	
 	private JTextField nameField;
 	
 	private String[] typeNames =  {"Integer", "Double", "String"};
 	
 	private JComboBox fieldType;
 	
 	private JTextField initValue;
 	
 	private JTextField xLoc;
 	
 	private JTextField yLoc;
 	
 	public EditFieldScreen(final ScreenManager sm) {
 		super(sm);
 		this.setLayout(new BorderLayout());
 		JLabel label = new JLabel("Edit Field");
 		label.setHorizontalAlignment(SwingConstants.CENTER);
 		label.setPreferredSize(new Dimension(300, 150));
 		JPanel mainPanel = new JPanel();
 		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
 		JPanel panel1 = new JPanel();
 		panel1.setLayout(new BoxLayout(panel1, BoxLayout.X_AXIS));
 		JPanel panel2 = new JPanel();
 		panel2.setLayout(new BoxLayout(panel2, BoxLayout.X_AXIS));
 		JPanel panel3 = new JPanel();
 		panel3.setLayout(new BoxLayout(panel3, BoxLayout.X_AXIS));
 		JPanel buttonPanel = new JPanel();
 		JLabel nameLabel = new JLabel("Field Name: ");
 		nameLabel.setHorizontalAlignment(SwingConstants.RIGHT);
 		nameField = new JTextField(40);
 		nameField.setMaximumSize(new Dimension(300, 40));
 		JLabel typeLabel = new JLabel("Field Type: ");
 		typeLabel.setHorizontalAlignment(SwingConstants.RIGHT);
 		fieldType = new JComboBox(typeNames);
 		fieldType.setMaximumSize(new Dimension(300, 40));
 		JLabel valueLabel = new JLabel("Initial Value: ");
 		valueLabel.setHorizontalAlignment(SwingConstants.RIGHT);
 		initValue = new JTextField(40);
 		initValue.setMaximumSize(new Dimension(300, 40));
 		xLoc = new JTextField();
 		yLoc = new JTextField();
 		JButton cancelButton = new JButton("Cancel");
 		cancelButton.setPreferredSize(new Dimension(120, 60));
 		cancelButton.addActionListener(
 				new ActionListener() {
					@Override
 					public void actionPerformed(ActionEvent e) {
 						sm.update(sm.getScreen("Edit Simulation")); 
 						} 
 					}
 				);
 		JButton finishButton = new JButton("Finish");
 		finishButton.setPreferredSize(new Dimension(120, 60));
 		finishButton.addActionListener(
 				new ActionListener() {
					@Override
 					public void actionPerformed(ActionEvent e) {
 						sm.update(sm.getScreen("Edit Simulation")); 
 						} 
 					}
 				);
 		
 		panel1.add(nameLabel);
 		panel1.add(nameField);
 		panel2.add(typeLabel);
 		panel2.add(fieldType);
 		panel3.add(valueLabel);
 		panel3.add(initValue);
 		buttonPanel.add(cancelButton);
 		buttonPanel.add(finishButton);
 		mainPanel.add(panel1);
 		mainPanel.add(Box.createRigidArea(new Dimension (0, 15)));
 		mainPanel.add(panel2);
 		mainPanel.add(Box.createRigidArea(new Dimension (0, 15)));
 		mainPanel.add(panel3);
 		mainPanel.add(buttonPanel);
 		this.add(label, BorderLayout.NORTH);
 		this.add(mainPanel, BorderLayout.CENTER);
 		
 	}
 
 
 	public void addComponents(JPanel panel) {
 		// TODO Auto-generated method stub
 
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
