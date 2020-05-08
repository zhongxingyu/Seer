 package edu.wpi.cs.wpisuitetng.modules.RequirementManager.view.Requirements;
 
 
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.ItemEvent;
 import java.awt.event.KeyEvent;
 import java.beans.PropertyChangeEvent;
 
 import javax.swing.JButton;
 import javax.swing.JPanel;
 import javax.swing.SpringLayout;
 
 import edu.wpi.cs.wpisuitetng.modules.RequirementManager.models.Requirement;
 import edu.wpi.cs.wpisuitetng.modules.RequirementManager.models.RequirementModel;
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
 
 public class NewRequirementPanel extends RequirementPanel 
 {	
 	
 	private JButton buttonUpdate = new JButton("Create");
 	private JButton buttonCancel = new JButton("Cancel");
 	private JButton buttonClear = new JButton("Clear");
 	
 	private Requirement newRequirement = new Requirement(RequirementModel.getInstance().getNextID(), "", "");
 	
 	/**
 	 * Constructor for a new requirement panel
 	 * @param reqModel Local requirement model for containing data
 	 */
 	public NewRequirementPanel() {
 		contentPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
 
 		contentPanel.add(buildLeftPanel()); //add left panel
 		contentPanel.add(buildRightPanel()); //add right panel
 		
 		contentPanel.setMinimumSize(new Dimension(500,465));
 		contentPanel.setPreferredSize(new Dimension(500,465));
 		
 		this.setViewportView(contentPanel);
 	}
 	
 	/**
 	 * Builds the left panel
 	 */
 	@Override
 	protected JPanel buildLeftPanel()
 	{
 		super.buildLeftPanel();
 		getBoxIteration().setEnabled(false);
 		getBoxIteration().setBackground(leftPanel.getBackground());
 		return leftPanel;
 	}
 	
 	/**
 	 * Builds the right panel
 	 */
 	@Override
 	protected JPanel buildRightPanel()
 	{
 		super.buildRightPanel();
 
 		getDropdownStatus().setEnabled(false);
 		
 		//setup the buttons
 		JPanel buttonPanel = new JPanel();
 		
 		// Construct the add requirement controller and add it to the update button
 		buttonUpdate.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent e)
 			{				
 				if(validateFields())
 					{
 						update();
 						removeNewReqTab();
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
 				removeNewReqTab();
 			}
 		});
 
 		buttonPanel.add(buttonUpdate);
 		buttonPanel.add(buttonClear);
 		buttonPanel.add(buttonCancel);
 		
 		SpringLayout rightLayout = (SpringLayout)rightPanel.getLayout();
 		
 		rightLayout.putConstraint(SpringLayout.NORTH, buttonPanel, 15,
 				SpringLayout.SOUTH, getErrorEstimate());
 		rightLayout.putConstraint(SpringLayout.WEST, buttonPanel, 15,
 				SpringLayout.WEST, rightPanel);
 		
 		rightPanel.add(buttonPanel);
 		
 		return rightPanel;
 	}
 	
 	/**
 	 * Updates the requirement.
 	 */
 	public void update()
 	{
 		// Extract the name, release number, and description from the GUI fields
 		String stringName = this.getBoxName().getText();
 		String stringReleaseNum = this.getBoxReleaseNum().getText();
 		String stringDescription = this.getBoxDescription().getText();
 		String stringIteration = this.getBoxIteration().getText();
 		String stringEstimate = this.getBoxEstimate().getText();
 		
 		RequirementPriority priority;
 		RequirementStatus status;
 		RequirementType type;
 		int estimate = stringEstimate.trim().length() == 0 ? 0 : Integer.parseInt(stringEstimate);
 		
 		// Extract the status from the GUI
 		status = (RequirementStatus)this.getDropdownStatus().getSelectedItem();
 		type = (RequirementType)this.getDropdownType().getSelectedItem();
 		// Extract which radio is selected for the priority
 		boolean stateHigh = getPriorityHigh().isSelected();
 		boolean stateMedium = getPriorityMedium().isSelected();
 		boolean stateLow = getPriorityLow().isSelected();
 		boolean stateBlank = getPriorityBlank().isSelected();
 
 		// Convert the priority string to its corresponding enum
 		if (stateHigh)
 			priority = RequirementPriority.HIGH;
 		else if (stateMedium)
 			priority = RequirementPriority.MEDIUM;
 		else if (stateLow)
 			priority = RequirementPriority.LOW;
 		else 
 			priority = RequirementPriority.BLANK;
 
 
 		// Set to true to indicate the requirement is being newly created
 		boolean created = true;
 		
 		// Create a new requirement object based on the extracted info
 		getNewRequirement().setName(stringName);
 		getNewRequirement().setDescription(stringDescription);		
 		getNewRequirement().setRelease(stringReleaseNum);
 		getNewRequirement().setStatus(status, created);
 		getNewRequirement().setPriority(priority, created);
 		getNewRequirement().setType(type);
 		getNewRequirement().setEstimate(estimate);
 		getNewRequirement().setIteration(stringIteration, created);
 		getNewRequirement().getHistory().add("REQUIREMENT CREATED");
 
 		RequirementModel.getInstance().addRequirement(getNewRequirement());
 		
 	}
 	
 	private void removeNewReqTab()
 	{
 		ViewEventController.getInstance().removeTab(this);
 	}
 	
 	/**
 	 * Clears the editing of the requirement.
 	 */
 	private void clear() 
 	{
 		getBoxName().setText("");
 		getBoxDescription().setText("");
 		this.getPriorityBlank().setSelected(true);
 		getDropdownType().setSelectedItem(RequirementType.BLANK);
 		getBoxReleaseNum().setText("");
 		getBoxEstimate().setText("");
 		
 		this.getErrorEstimate().setText("");
 		getBoxEstimate().setBorder(defaultBorder);
 		this.getErrorDescription().setText("");
 		getBoxDescription().setBorder(defaultBorder);
 		this.getErrorName().setText("");
 		getBoxName().setBorder(defaultBorder);
 		repaint(); //repaint the entire panel.
 	}
 	
 
 
 	/**
 	 * @return the buttonUpdate
 	 */
 	public JButton getButtonUpdate() {
 		return buttonUpdate;
 	}
 
 
 
 	/**
 	 * @return the buttonCancel
 	 */
 	public JButton getButtonCancel() {
 		return buttonCancel;
 	}
 
 
 
 	/**
 	 * @return the buttonClear
 	 */
 	public JButton getButtonClear() {
 		return buttonClear;
 	}
 
 
 
 	public Requirement getNewRequirement() {
 		return newRequirement;
 	}
 	
 	/**
 	 * Returns whether any field in the panel has been changed
 	 */
 	public boolean anythingChanged()
 	{
 		boolean nameChanged = !(getBoxName().getText().trim().equals(""));
 		boolean descriptionChanged = !(getBoxDescription().getText().trim().equals(""));
 		boolean releaseChanged = !(getBoxReleaseNum().getText().trim().equals(""));
 		//boolean iterationChanged = !(getBoxIteration().getText().trim().equals(""));
 		boolean typeChanged = !(((RequirementType)getDropdownType().getSelectedItem()) == RequirementType.BLANK);
 
 		boolean priorityChanged = !getPriorityBlank().isSelected();
 		
 		boolean estimateChanged = !(getBoxEstimate().getText().trim().equals(""));
 
 		boolean anythingChanged = nameChanged || descriptionChanged || releaseChanged ||  
 				typeChanged || priorityChanged || estimateChanged;
 		
 		return anythingChanged;
 	}
 	
 
 	@Override
 	public void keyTyped(KeyEvent e) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void keyPressed(KeyEvent e) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void keyReleased(KeyEvent e) {
 		// TODO change the boolean expression // test case fail because of this
 		this.buttonUpdate.setEnabled(getBoxName().getText().trim().length() > 0 && getBoxDescription().getText().trim().length() > 0);	
 		this.buttonClear.setEnabled(anythingChanged());
 		
 		//check that estimate is valid to enable iterations.
 		boolean validEstimate = true;
 		
 		try
 		{
 			int estimate = Integer.parseInt(getBoxEstimate().getText().trim());
 			validEstimate = estimate > 0;
 		}
 		catch (Exception ex)
 		{
 			validEstimate = false;
 		}
 		
 		this.getBoxIteration().setEnabled(validEstimate);
 		
 		this.repaint();				
 	}
 
 	@Override
 	public void itemStateChanged(ItemEvent e) {
		this.buttonUpdate.setEnabled(getBoxName().getText().trim().length() > 0 && getBoxDescription().getText().trim().length() > 0 );	
 		this.buttonClear.setEnabled(anythingChanged());		
 		this.repaint();				
 	}
 
 }
