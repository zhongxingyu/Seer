 package edu.wpi.cs.wpisuitetng.modules.RequirementManager.view.Requirements;
 
 import java.awt.Dimension;
 import java.awt.FlowLayout;
import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.JButton;
 import javax.swing.JPanel;
import javax.swing.JTextField;
 import javax.swing.SpringLayout;
 
 import edu.wpi.cs.wpisuitetng.modules.RequirementManager.controller.UpdateRequirementController;
 import edu.wpi.cs.wpisuitetng.modules.RequirementManager.models.Requirement;
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
 @SuppressWarnings("serial")
 public class EditRequirementPanel extends RequirementPanel 
 {	
 	private Requirement requirementBeingEdited;
 	private JButton buttonDelete;
 	
 	
 	/**
 	 * Constructor for a new requirement panel
 	 * @param reqModel Local requirement model for containing data
 	 */
 	public EditRequirementPanel(Requirement req) {
 		contentPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
 
 		contentPanel.add(buildLeftPanel()); //add left panel
 		contentPanel.add(buildRightPanel()); //add right panel
 		
 		contentPanel.setMinimumSize(new Dimension(500,465));
 		contentPanel.setPreferredSize(new Dimension(500,465));
 		
 		this.setViewportView(contentPanel);
 		
 		requirementBeingEdited = req;
 		fillFieldsForRequirement();
 	}
 	
 	/**
 	 * Fills the fields of the edit requirement panel based on the current settings of the
 	 * edited requirement.
 	 */
 	private void fillFieldsForRequirement()
 	{
 		boxName.setText(requirementBeingEdited.getName());
 		boxDescription.setText(requirementBeingEdited.getDescription());
 		boxEstimate.setText(String.valueOf(requirementBeingEdited.getEstimate()));
 		boxReleaseNum.setText(requirementBeingEdited.getRelease());
 		dropdownStatus.setSelectedItem(requirementBeingEdited.getStatus());
 		dropdownType.setSelectedItem(requirementBeingEdited.getType());
 		boxIteration.setText(requirementBeingEdited.getIteration().toString());
 		
 		switch(requirementBeingEdited.getPriority())
 		{
 		case BLANK:
 			priorityBlank.setSelected(true);
 			break;
 		case LOW:
 			priorityLow.setSelected(true);
 			break;
 		case MEDIUM:
 			priorityMedium.setSelected(true);
 			break;
 		case HIGH:
 			priorityHigh.setSelected(true);
 			break;
 		}
 		
 		if(requirementBeingEdited.getStatus() == RequirementStatus.INPROGRESS || requirementBeingEdited.getStatus() == RequirementStatus.COMPLETE)
 		{
 			boxEstimate.setEnabled(false);
 		}
 		else
 		{
 			boxEstimate.setEnabled(true);
 		}
 		
 		if(requirementBeingEdited.getStatus() == RequirementStatus.INPROGRESS) buttonDelete.setEnabled(false);
 		if(requirementBeingEdited.getStatus() == RequirementStatus.DELETED) disableComponents(); 
 		if(!(requirementBeingEdited.getEstimate() > 0)) boxIteration.setEnabled(false);
 		
 		//reset the error messages.
 		this.errorEstimate.setText("");
 		boxEstimate.setBorder(defaultBorder);
 		this.errorDescription.setText("");
 		boxDescription.setBorder(defaultBorder);
 		this.errorName.setText("");
 		boxName.setBorder(defaultBorder);
 		
 		
 		repaint();
 	}
 	
 	/**
 	 * Builds the right panel
 	 */
 	@Override
 	protected JPanel buildRightPanel()
 	{
 		super.buildRightPanel();
 
 		//setup the buttons
 		JPanel buttonPanel = new JPanel();
 		JPanel buttonPanel2 = new JPanel();
 		JButton buttonUpdate = new JButton("Update");
 		JButton buttonClear = new JButton("Undo Changes");
 		buttonDelete = new JButton("Delete");
 		JButton buttonCancel = new JButton("Cancel");
 		
 		// Construct the add requirement controller and add it to the update button
 		buttonUpdate.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent e)
 			{
 				if(validateFields()) update();
 			}
 		});
 		
 		buttonClear.addActionListener(new ActionListener(){
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				fillFieldsForRequirement();
 			}
 		
 		});
 		
 		buttonCancel.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				cancel();
 			}
 		});
 		
 		buttonDelete.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent e)
 			{
 				deleteRequirement();
 			}
 		});
 
 		buttonPanel.add(buttonUpdate);
 		buttonPanel.add(buttonClear);
 		buttonPanel2.add(buttonDelete);
 		buttonPanel2.add(buttonCancel);
 		
 		SpringLayout rightLayout = (SpringLayout)rightPanel.getLayout();
 		
 		rightLayout.putConstraint(SpringLayout.NORTH, buttonPanel, 15,
 				SpringLayout.SOUTH, errorEstimate);
 		rightLayout.putConstraint(SpringLayout.WEST, buttonPanel, 15,
 				SpringLayout.WEST, rightPanel);
 		
 		rightLayout.putConstraint(SpringLayout.NORTH, buttonPanel2, 15,
 				SpringLayout.SOUTH, buttonPanel);
 		rightLayout.putConstraint(SpringLayout.WEST, buttonPanel2, 15,
 				SpringLayout.WEST, rightPanel);
 		
 		rightPanel.add(buttonPanel);
 		rightPanel.add(buttonPanel2);
 		
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
 		String stringEstimate = this.boxEstimate.getText();
 		String stringIteration = this.boxIteration.getText();
 
 
 		RequirementPriority priority;
 		RequirementStatus status;
 		RequirementType type = (RequirementType)dropdownType.getSelectedItem();
 		
 		int estimate = stringEstimate.trim().length() == 0 ? 0 : Integer.parseInt(stringEstimate);
 		// Extract the status from the GUI
 		status = (RequirementStatus)this.dropdownStatus.getSelectedItem();
 		Iteration iteration = new Iteration(stringIteration);
 		// Extract which radio is selected for the priority
 		//		If requirement deleted {}		
 		//		estimate = iteration.getEstimate()- estimate;
 		boolean stateHigh = priorityHigh.isSelected();
 		boolean stateMedium = priorityMedium.isSelected();
 		boolean stateLow = priorityLow.isSelected();
 
 		// Convert the priority string to its corresponding enum
 		if (stateHigh)
 			priority = RequirementPriority.HIGH;
 		else if (stateMedium)
 			priority = RequirementPriority.MEDIUM;
 		else if (stateLow)
 			priority = RequirementPriority.LOW;
 		else
 			priority = RequirementPriority.BLANK;
 
 		// Set to false to indicate the requirement is being newly created
 		boolean created = false;
 		
 		// Create a new requirement object based on the extracted info
 		requirementBeingEdited.setName(stringName);
 		requirementBeingEdited.setRelease(stringReleaseNum);
 		requirementBeingEdited.setDescription(stringDescription);
 		requirementBeingEdited.setStatus(status, created);
 		requirementBeingEdited.setPriority(priority, created);
 		requirementBeingEdited.setEstimate(estimate);
 		requirementBeingEdited.setIteration(iteration, created);
 		requirementBeingEdited.setType(type);
 		UpdateRequirementController.getInstance().updateRequirement(requirementBeingEdited);
 		ViewEventController.getInstance().refreshTable();
 		ViewEventController.getInstance().removeTab(this);
 	}
 	
 	/**
 	 * Cancels the editing of the requirement.
 	 */
 	private void cancel()
 	{
 		ViewEventController.getInstance().removeTab(this);
 	}
 	
 	/**
 	 * Deletes the requirement.  Sets all fields uneditable, sets status to deleted and 
 	 */
 	private void deleteRequirement()
 	{
 		if(this.requirementBeingEdited.getStatus() == RequirementStatus.INPROGRESS) return;
 		
 		this.dropdownStatus.setSelectedItem(RequirementStatus.DELETED);
 		
 		requirementBeingEdited.setStatus(RequirementStatus.DELETED, false);	
 		
 		
 		UpdateRequirementController.getInstance().updateRequirement(requirementBeingEdited);
 		
 		ViewEventController.getInstance().refreshTable();
 		ViewEventController.getInstance().removeTab(this);
 	}
 	
 	
 	/**
 	 * Disables all the components of the editing panel besides the status dropdown.
 	 */
 	private void disableComponents()
 	{
 		this.boxName.setEnabled(false);
 		this.boxDescription.setEnabled(false);
 		this.boxEstimate.setEnabled(false);
 		this.boxReleaseNum.setEnabled(false);
 		this.dropdownType.setEnabled(false);
 		this.boxIteration.setEnabled(false);
 		this.priorityHigh.setEnabled(false);
 		this.priorityMedium.setEnabled(false);
 		this.priorityLow.setEnabled(false);
 		this.priorityBlank.setEnabled(false);
 		
 		this.buttonDelete.setEnabled(false);
 	}
 	
 }
