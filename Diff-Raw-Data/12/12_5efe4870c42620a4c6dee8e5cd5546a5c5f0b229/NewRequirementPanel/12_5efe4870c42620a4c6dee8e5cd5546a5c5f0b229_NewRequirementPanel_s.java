 package edu.wpi.cs.wpisuitetng.modules.RequirementManager.view.Requirements;
 
 import java.awt.Color;
 import java.awt.Font;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.JButton;
 import javax.swing.JPanel;
 import javax.swing.SpringLayout;
 
 import edu.wpi.cs.wpisuitetng.modules.RequirementManager.models.Requirement;
 import edu.wpi.cs.wpisuitetng.modules.RequirementManager.models.RequirementModel;
 import edu.wpi.cs.wpisuitetng.modules.RequirementManager.models.characteristics.Iteration;
 import edu.wpi.cs.wpisuitetng.modules.RequirementManager.models.characteristics.RequirementPriority;
 import edu.wpi.cs.wpisuitetng.modules.RequirementManager.models.characteristics.RequirementStatus;
 import edu.wpi.cs.wpisuitetng.modules.RequirementManager.models.characteristics.RequirementType;
 import edu.wpi.cs.wpisuitetng.modules.RequirementManager.view.ViewEventController;
 /**
  * 
  * @author Pi 
  * @author Chris
  * @author Brian
  *
  */
 //
 public class NewRequirementPanel extends RequirementPanel 
 {	
 	/**
 	 * Constructor for a new requirement panel
 	 * @param reqModel Local requirement model for containing data
 	 */
 	public NewRequirementPanel() {
 		setLayout(new GridLayout(1, 3));
 
 
 		this.add(buildLeftPanel()); //add left panel
 		this.add(buildRightPanel()); //add right panel
 	}
 	
 	/**
 	 * Builds the right panel
 	 */
 	@Override
 	protected JPanel buildRightPanel()
 	{
 		super.buildRightPanel();
 
 		dropdownStatus.setEnabled(false);
 		boxIteration.setEnabled(false);
 		//setup the buttons
 		JPanel buttonPanel = new JPanel();
 		JButton buttonUpdate = new JButton("Create");
 		JButton buttonCancel = new JButton("Cancel");
 		JButton buttonClear = new JButton("Clear");
 		
 		// Construct the add requirement controller and add it to the update button
 		buttonUpdate.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent e)
 			{
 				boolean isNameValid;
 				boolean isDescriptionValid;
 				boolean isEstimateValid;
 				
 				if (boxName.getText().length() >= 100)
 				{
 					isNameValid = false;
 					errorName.setText("No more than 100 chars");
 					errorName.setFont(new Font("Serif", Font.PLAIN, 10));
 					errorName.setForeground(Color.RED);
 				}
 				else if(boxName.getText().trim().length() <= 0)
 				{
 					isNameValid = false;
 					errorName.setText("** Name is REQUIRED");
 					errorName.setFont(new Font("Serif", Font.PLAIN, 10));
 					errorName.setForeground(Color.RED);
 				}
 				else
 				{
 					errorName.setText("");
 					isNameValid = true;
 					
 				}
 				if (boxDescription.getText().trim().length() <= 0)
 				{
 					isDescriptionValid = false;
 					errorDescription.setText("** Description is REQUIRED");
 					errorDescription.setFont(new Font("Serif", Font.PLAIN, 10));
 					errorDescription.setForeground(Color.RED);
 				}
 				else
 				{	
 					errorDescription.setText("");
 					isDescriptionValid = true;
 				}
 				
 				if (boxEstimate.getText().trim().length() <= 0)
 				{
 					boxEstimate.setText("");
 					errorEstimate.setText("");
 					isEstimateValid = true;
 				}
 				else if(!(isInteger(boxEstimate.getText())))
 				{
 					errorEstimate.setText("** Please enter a non-negative integer");
 					isEstimateValid = false;
 					errorEstimate.setForeground(Color.RED);
 				}
 				else if(Integer.parseInt(boxEstimate.getText())<0)
 				{
 					errorEstimate.setText("** Please enter a non-negative integer");
 					isEstimateValid = false;
 					errorEstimate.setForeground(Color.RED);
 				}
 				else
 				{
 					errorEstimate.setText("");
 					isEstimateValid = true;
 				}
 				
 				if(isNameValid && isDescriptionValid && isEstimateValid)
 				{
 					update();
 				}
 			}
 		});
 		
 		buttonClear.addActionListener(new ActionListener(){
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				clear();
 			}
 		
 		});
 		
 		buttonCancel.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				cancel();
 			}
 		});
 
 		buttonPanel.add(buttonUpdate);
 		buttonPanel.add(buttonClear);
 		buttonPanel.add(buttonCancel);
 		SpringLayout rightLayout = (SpringLayout)rightPanel.getLayout();
 		
 		rightLayout.putConstraint(SpringLayout.NORTH, buttonPanel, 15,
 				SpringLayout.SOUTH, errorEstimate);
 		rightLayout.putConstraint(SpringLayout.WEST, buttonPanel, 15,
 				SpringLayout.WEST, rightPanel);
 		
 		rightPanel.add(buttonPanel);
 		
 		return rightPanel;
 	}
 	
 	/**
 	 * Updates the requirement.
 	 */
 	private void update()
 	{
 		// Extract the name, release number, and description from the GUI fields
 		String stringName = this.boxName.getText();
 		String stringReleaseNum = this.boxReleaseNum.getText();
 		String stringDescription = this.boxDescription.getText();
 		String stringIteration = this.boxIteration.getText();
 		String stringEstimate = this.boxEstimate.getText();
 		
 		RequirementPriority priority;
 		RequirementStatus status;
 		RequirementType type;
 		int estimate = stringEstimate.trim().length() == 0 ? 0 : Integer.parseInt(stringEstimate);
 		
 		Iteration iteration = new Iteration(stringIteration);
 		
 		// Extract the status from the GUI
 		status = (RequirementStatus)this.dropdownStatus.getSelectedItem();
 		type = (RequirementType)this.dropdownType.getSelectedItem();
 		// Extract which radio is selected for the priority
 		boolean stateHigh = priorityHigh.isSelected();
 		boolean stateMedium = priorityMedium.isSelected();
 		boolean stateLow = priorityLow.isSelected();
 		boolean stateBlank = priorityBlank.isSelected();
 
 		// Convert the priority string to its corresponding enum
 		if (stateHigh)
 			priority = RequirementPriority.HIGH;
 		else if (stateMedium)
 			priority = RequirementPriority.MEDIUM;
 		else if (stateLow)
 			priority = RequirementPriority.LOW;
 		else if(stateBlank)
 			priority = RequirementPriority.BLANK;
 		else
 			priority = RequirementPriority.BLANK;
 
 		// Create a new requirement object based on the extracted info
 		Requirement newRequirement = new Requirement(RequirementModel.getInstance().getNextID(), stringName, stringDescription);
 		newRequirement.setRelease(stringReleaseNum);
 		newRequirement.setStatus(status);
 		newRequirement.setPriority(priority);
 		newRequirement.setType(type);
 		newRequirement.setEstimate(estimate);
 		newRequirement.setIteration(iteration);
 		RequirementModel.getInstance().addRequirement(newRequirement);
 		ViewEventController.getInstance().removeTab(this);
 	}
 	
 	/**
 	 * Clears the editing of the requirement.
 	 */
 	private void clear() 
 	{
 		boxName.setText(null);
 		boxDescription.setText(null);
 		dropdownStatus.setSelectedItem("Not Selected");
 		group.clearSelection();
 		boxReleaseNum.setText(null);
 		errorName.setText(null);
 		errorDescription.setText(null);
 		repaint(); //repaint the entire panel.
 	}
 	
 	/**
 	 * Cancels the editing of the requirement.
 	 */
 	private void cancel()
 	{
 		ViewEventController.getInstance().removeTab(this);
 	}
 }
