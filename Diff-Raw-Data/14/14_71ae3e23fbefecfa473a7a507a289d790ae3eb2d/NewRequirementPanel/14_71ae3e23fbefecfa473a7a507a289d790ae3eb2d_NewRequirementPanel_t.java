 package edu.wpi.cs.wpisuitetng.modules.RequirementManager.view.Requirements;
 
 import java.awt.Dimension;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.ButtonGroup;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JRadioButton;
 import javax.swing.JTextArea;
 import javax.swing.JTextField;
 import javax.swing.SpringLayout;
 
 public class NewRequirementPanel extends JPanel 
 {
 
 	private JPanel leftPanel;
 	private JTextField boxName;
 	private JTextArea boxDescription;
 	private JTextArea resultField;
 	
 	private JPanel rightPanel;
 	private JTextField boxReleaseNum;
	private JComboBox<String> dropdownStatus;
 	private JRadioButton priorityHigh;
 	private JRadioButton priorityMedium;
 	private JRadioButton priorityLow;
 	
 	/**
 	 * Default constructor.
 	 */
 	public NewRequirementPanel() {
 
 		setLayout(new GridLayout(1, 2));
 
 
 		this.add(buildLeftPanel()); //add left panel
 		this.add(buildRightPanel()); //add right panel
 	}
 	
 	
 	/**
 	 * Builds the left panel.
 	 * @return the newly created and formatted left panel.
 	 */
 	private JPanel buildLeftPanel()
 	{
 		leftPanel = new JPanel();
 		JLabel labelName = new JLabel("Name");
 		JLabel labelDescription = new JLabel("Description");
 		
 		boxName = new JTextField();
 		boxDescription = new JTextArea();
 		boxDescription.setLineWrap(true);
 
 		boxName.setPreferredSize(new Dimension(200, 20));
 		boxDescription.setPreferredSize(new Dimension(200, 100));
 
 		resultField = new JTextArea(); // For testing purpose
 		resultField.setPreferredSize(new Dimension(200, 200));
 
 		SpringLayout leftLayout = new SpringLayout();
 
 		leftPanel.setLayout(leftLayout);
 
 		leftLayout.putConstraint(SpringLayout.NORTH, labelName, 15,
 				SpringLayout.NORTH, leftPanel);
 		leftLayout.putConstraint(SpringLayout.WEST, labelName, 15,
 				SpringLayout.WEST, leftPanel);
 
 		leftLayout.putConstraint(SpringLayout.NORTH, boxName, 15,
 				SpringLayout.SOUTH, labelName);
 		leftLayout.putConstraint(SpringLayout.WEST, boxName, 15, SpringLayout.WEST,
 				leftPanel);
 
 		leftLayout.putConstraint(SpringLayout.NORTH, labelDescription, 15,
 				SpringLayout.SOUTH, boxName);
 		leftLayout.putConstraint(SpringLayout.WEST, labelDescription, 15,
 				SpringLayout.WEST, leftPanel);
 
 		leftLayout.putConstraint(SpringLayout.NORTH, boxDescription, 15,
 				SpringLayout.SOUTH, labelDescription);
 		leftLayout.putConstraint(SpringLayout.WEST, boxDescription, 15,
 				SpringLayout.WEST, leftPanel);
 
 		leftLayout.putConstraint(SpringLayout.NORTH, resultField, 15,
 				SpringLayout.SOUTH, boxDescription);
 		leftLayout.putConstraint(SpringLayout.WEST, resultField, 15,
 				SpringLayout.WEST, leftPanel);
 		
 		leftPanel.add(labelName);
 		leftPanel.add(boxName);
 		leftPanel.add(labelDescription);
 		leftPanel.add(boxDescription);
 		leftPanel.add(resultField);
 		
 		return leftPanel;
 	}
 	
 	/**
 	 * Builds the right panel
 	 * @return the newly created and formatted right panel.
 	 */
 	private JPanel buildRightPanel()
 	{
 		rightPanel = new JPanel();
 
 		JLabel labelStatus = new JLabel("Status");
 		JLabel labelPriority = new JLabel("Priority");
 		JLabel labelReleaseNum = new JLabel("Release #");
 
 		boxReleaseNum = new JTextField();
 		boxReleaseNum.setPreferredSize(new Dimension(200, 20));
 
 		String[] statusString = { "Not Selected", "New", "In Progress", "Open", "Complete", "Delete" }; //TODO change to enum
		dropdownStatus = new JComboBox<String>(statusString);
 
 		JPanel priorityPanel = new JPanel();
 
 		// Radio buttons
 
 		priorityHigh = new JRadioButton("High");
 		priorityMedium = new JRadioButton("Medium");
 		priorityLow = new JRadioButton("Low");
 		
 		ButtonGroup group = new ButtonGroup();
 		group.add(priorityHigh);
 		group.add(priorityMedium);
 		group.add(priorityLow);
 
 		priorityPanel.add(priorityLow);
 		priorityPanel.add(priorityMedium);
 		priorityPanel.add(priorityHigh);
 
 		//setup the buttons
 		JPanel buttonPanel = new JPanel();
 		JButton buttonUpdate = new JButton("Update");
 		JButton buttonCancel = new JButton("Cancel");
 		JButton buttonDelete = new JButton("Delete");
 
 		buttonUpdate.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				update();
 			}
 		});
 
 		buttonCancel.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				cancel();
 			}
 		});
 
 		buttonDelete.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				delete();
 			}
 		});
 
 		buttonPanel.add(buttonUpdate);
 		buttonPanel.add(buttonCancel);
 		buttonPanel.add(buttonDelete);
 
 		SpringLayout rightLayout = new SpringLayout();
 
 		rightPanel.setLayout(rightLayout);
  
 		//setup the constraints for the layout.
 		rightLayout.putConstraint(SpringLayout.NORTH, labelStatus, 15,
 				SpringLayout.NORTH, rightPanel);
 		rightLayout.putConstraint(SpringLayout.WEST, labelStatus, 15,
 				SpringLayout.WEST, rightPanel);
 
 		rightLayout.putConstraint(SpringLayout.NORTH, dropdownStatus, 15,
 				SpringLayout.SOUTH, labelStatus);
 		rightLayout.putConstraint(SpringLayout.WEST, dropdownStatus, 15,
 				SpringLayout.WEST, rightPanel);
 
 		rightLayout.putConstraint(SpringLayout.NORTH, labelPriority, 15,
 				SpringLayout.SOUTH, dropdownStatus);
 		rightLayout.putConstraint(SpringLayout.WEST, labelPriority, 15,
 				SpringLayout.WEST, rightPanel);
 
 		rightLayout.putConstraint(SpringLayout.NORTH, priorityPanel, 15,
 				SpringLayout.SOUTH, labelPriority);
 		rightLayout.putConstraint(SpringLayout.WEST, priorityPanel, 15,
 				SpringLayout.WEST, rightPanel);
 
 		rightLayout.putConstraint(SpringLayout.NORTH, labelReleaseNum, 15,
 				SpringLayout.SOUTH, priorityPanel);
 		rightLayout.putConstraint(SpringLayout.WEST, labelReleaseNum, 15,
 				SpringLayout.WEST, rightPanel);
 
 		rightLayout.putConstraint(SpringLayout.NORTH, boxReleaseNum, 15,
 				SpringLayout.SOUTH, labelReleaseNum);
 		rightLayout.putConstraint(SpringLayout.WEST, boxReleaseNum, 15,
 				SpringLayout.WEST, rightPanel);
 
 		rightLayout.putConstraint(SpringLayout.NORTH, buttonPanel, 15,
 				SpringLayout.SOUTH, boxReleaseNum);
 		rightLayout.putConstraint(SpringLayout.WEST, buttonPanel, 15,
 				SpringLayout.WEST, rightPanel);
 		
 		rightPanel.add(labelStatus);
 		rightPanel.add(dropdownStatus);
 		rightPanel.add(labelPriority);
 		rightPanel.add(priorityPanel);
 		rightPanel.add(labelReleaseNum);
 		rightPanel.add(boxReleaseNum);
 		rightPanel.add(buttonPanel);
 		
 		return rightPanel;
 	}
 
 	/**
 	 * Updates the requirement based on the inputted information.
 	 */
	private void update()
 	{
 		String stringName = boxName.getText();
 		String stringDescription = boxDescription.getText();
 		String stringStatus = (String) dropdownStatus.getSelectedItem();
 		String stringPriority;
 
 		boolean stateHigh = priorityHigh.isSelected();
 		boolean stateMedium = priorityMedium.isSelected();
 		boolean stateLow = priorityLow.isSelected();
 
 		if (stateHigh)
 			stringPriority = "High";
 		else if (stateMedium)
 			stringPriority = "Medium";
 		else if (stateLow)
 			stringPriority = "Low";
 		else
 			stringPriority = "Not selected";
 
 		String stringReleaseNum = boxReleaseNum.getText();
 
 		String entireData = stringName + "\n";
 		entireData = entireData + stringDescription + "\n";
 		entireData = entireData + stringStatus + "\n";
 		entireData = entireData + stringPriority + "\n";
 		entireData = entireData + stringReleaseNum + "\n";
 
 		resultField.setText(entireData);
 	}
 	
 	/**
 	 * Deletes the requirement.
 	 */
	private void delete()
 	{
 		resultField.setText("Delete");
 	}
 	
 	/**
 	 * Cancels the editing of the requirement.
 	 */
	private void cancel() 
 	{
 		boxName.setText("");
 		boxDescription.setText("");
 		dropdownStatus.setSelectedItem("Not Selected");
 		priorityLow.setSelected(false);
 		priorityMedium.setSelected(false);
 		priorityHigh.setSelected(false);
 		boxReleaseNum.setText("");
 		resultField.setText("");
 		repaint(); //repaint the entire panel.
 	}
  
 }
