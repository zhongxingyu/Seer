 /*******************************************************************************
  * Copyright (c) 2013 WPI-Suite
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors: Team Rolling Thunder
  ******************************************************************************/
 package edu.wpi.cs.wpisuitetng.modules.requirementmanager.view;
 
 import static org.junit.Assert.assertEquals;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import edu.wpi.cs.wpisuitetng.modules.requirementmanager.MockNetwork;
 import edu.wpi.cs.wpisuitetng.modules.requirementmanager.models.characteristics.RequirementPriority;
 import edu.wpi.cs.wpisuitetng.modules.requirementmanager.models.characteristics.RequirementStatus;
 import edu.wpi.cs.wpisuitetng.modules.requirementmanager.models.characteristics.RequirementType;
 import edu.wpi.cs.wpisuitetng.modules.requirementmanager.models.iterations.Iteration;
 import edu.wpi.cs.wpisuitetng.modules.requirementmanager.models.iterations.IterationModel;
 import edu.wpi.cs.wpisuitetng.modules.requirementmanager.view.requirements.RequirementPanel;
 import edu.wpi.cs.wpisuitetng.network.Network;
 import edu.wpi.cs.wpisuitetng.network.configuration.NetworkConfiguration;
 
 public class NewRequirementPanelTest {
 
 	/**
 	 * Setting up using Network and Iteration
 	 * @throws Exception
 	 */
 	@Before
 	public void setUp() throws Exception {
 		// Mock Network
 		Network.initNetwork(new MockNetwork());
 		Network.getInstance().setDefaultNetworkConfiguration(
 				new NetworkConfiguration("http://wpisuitetng"));
 		// Mock Iteration
 		Iteration iterationTest = new Iteration(0,"Backlog");
 		IterationModel.getInstance().setBacklog(iterationTest);
 		 
 	}
 
 	/**
 	 * check whether the field is enabled or not as default
 	 */
 	@Test 
 	public void defaultEnability()
 	{
 		// Create new requirement panel
 		RequirementPanel testNew = new RequirementPanel(-1);
 		
 		// Check
 		assertEquals(true, testNew.getInfoPanel().getBoxName().isEnabled());
 		assertEquals(true, testNew.getInfoPanel().getBoxDescription().isEnabled());
 		assertEquals(false, testNew.getInfoPanel().getBoxIteration().isEnabled());
 		assertEquals(true, testNew.getInfoPanel().getDropdownType().isEnabled());
 		assertEquals(false, testNew.getInfoPanel().getDropdownStatus().isEnabled());
 		assertEquals(true, testNew.getInfoPanel().getPriorityBlank().isEnabled());
 		assertEquals(true, testNew.getInfoPanel().getBoxEstimate().isEnabled());
 		assertEquals(false, testNew.getButtonPanel().getButtonOK().isEnabled());
 		assertEquals(false, testNew.getButtonPanel().getButtonClear().isEnabled());
 		assertEquals(true, testNew.getButtonPanel().getButtonCancel().isEnabled());
 		
 	}
 	
 	/**
 	 * check for the default case when starting a new requirement panel
 	 */
 	@Test
 	public void defaultField()
 	{
 		// Create new requirement panel
 		RequirementPanel testNew = new RequirementPanel(-1);
 		
 		// Check
 		assertEquals(RequirementStatus.NEW, testNew.getInfoPanel().getDropdownStatus().getSelectedItem());
 		assertEquals(false, testNew.getInfoPanel().getPriorityHigh().isSelected());
 		assertEquals(false, testNew.getInfoPanel().getPriorityMedium().isSelected());
 		assertEquals(false, testNew.getInfoPanel().getPriorityLow().isSelected());
 		assertEquals(true, testNew.getInfoPanel().getPriorityBlank().isSelected());
 		
 	}
 	
 	
 	/**
 	 * check for enability when required fields are not filled in
 	 */
 	@Test
 	public void errorRequiredFieldTest() {
 		// Create new requirement panel
 		RequirementPanel testNew = new RequirementPanel(-1);
 		
 		// a field is added correctly but both name and description are filled with blanks
 		testNew.getInfoPanel().getBoxEstimate().setText("-134");
 		testNew.getInfoPanel().getBoxName().setText("  ");
 		testNew.getInfoPanel().getBoxDescription().setText("Desc.");
 		
 		// release pressed key
 		testNew.getInfoPanel().keyReleased(null);
 		
 		// can't create because no name/description, but a field has been changed
 		assertEquals(false, testNew.getButtonPanel().getButtonOK().isEnabled());
 		assertEquals(true, testNew.getButtonPanel().getButtonClear().isEnabled());
 		assertEquals(true, testNew.getButtonPanel().getButtonCancel().isEnabled());
 		
 		// blank description
 		testNew.getInfoPanel().getBoxName().setText("Name");
 		testNew.getInfoPanel().getBoxDescription().setText(" ");
 		
 		// release pressed key
 		testNew.getInfoPanel().keyReleased(null);
 		
 		// can't create because no name/description, but a field has been changed
 		assertEquals(false, testNew.getButtonPanel().getButtonOK().isEnabled());
 		assertEquals(true, testNew.getButtonPanel().getButtonClear().isEnabled());
 		assertEquals(true, testNew.getButtonPanel().getButtonCancel().isEnabled());
 		
 
 	}
 	
 	/**
 	 * check the error when extreme cases occur
 	 */
 	@Test
 	public void invalidFieldTest()
 	{
 		// Create new requirement panel
 		RequirementPanel testNew = new RequirementPanel(-1);
 		
 		// pre-defined variable
		String errorMessageNoninteger = "Estimate must be non-negative integer";
 		String errorMessageNoMore100 = "No more than 100 chars";
 		String testDescription = "testDescription";
 		String hundredCharText = "0";
 		
 		for(int i = 0; i<100; i++)
 		{
 			hundredCharText += "0";
 		}
 		
 		// fill in invalid field
 		testNew.getInfoPanel().getBoxName().setText(hundredCharText);
 		testNew.getInfoPanel().getBoxDescription().setText(testDescription);
 		testNew.getInfoPanel().getBoxEstimate().setText("-134");
 		
 		// release pressed key
 		testNew.getInfoPanel().keyReleased(null);
 		
 		// can't create because no name/description, but a field has been changed
 		assertEquals(false, testNew.getButtonPanel().getButtonOK().isEnabled());
 		assertEquals(true, testNew.getButtonPanel().getButtonClear().isEnabled());
 		assertEquals(true, testNew.getButtonPanel().getButtonCancel().isEnabled());
 		testNew.getButtonPanel().getButtonOK().doClick();
 		testNew.getInfoPanel().validateFields(true);
 
 		
 		// error messages are shown
 		assertEquals(errorMessageNoMore100,testNew.getInfoPanel().getErrorName().getText());
 		assertEquals(errorMessageNoninteger,testNew.getInfoPanel().getErrorEstimate().getText());
 		
 		
 	}
 
 	
 	/**
 	 * Check enability when valid fields are filled
 	 */
 	@Test
 	public void validRequirementCreation()
 	{
 		// Create new requirement panel
 		RequirementPanel testNew = new RequirementPanel(-1);
 		
 		String testName = "testName";
 		String testDescription = "testDescription";
 		
 		// add fields
 		testNew.getInfoPanel().getBoxName().setText(testName);
 		testNew.getInfoPanel().getBoxDescription().setText(testDescription);
 		
 		testNew.getInfoPanel().keyReleased(null);
 		
 		assertEquals(true, testNew.getButtonPanel().getButtonOK().isEnabled());
 		assertEquals(true, testNew.getButtonPanel().getButtonClear().isEnabled());
 		assertEquals(true, testNew.getButtonPanel().getButtonCancel().isEnabled());
 	}
 	
 	/**
 	 * Testing clear button
 	 */
 	@Test
 	public void clearButtonTest() 
 	{	
 		// Create new requirement panel
 		RequirementPanel testNew = new RequirementPanel(-1);
 		
 		String testName = "testName";
 		String testDescription = "testDescription";
 		
 		testNew.getInfoPanel().getBoxName().setText(testName);
 		testNew.getInfoPanel().getBoxDescription().setText(testDescription);
 		testNew.getInfoPanel().getBoxEstimate().setText("4");
 		testNew.getInfoPanel().keyReleased(null);
 		
 		// set to each field random stuffs to test clear functionality
 		assertEquals(true, testNew.getButtonPanel().getButtonClear().isEnabled());
 		// clear fields
 		testNew.getButtonPanel().getButtonClear().doClick();
 		
 		assertEquals("",testNew.getInfoPanel().getBoxName().getText());
 		assertEquals("",testNew.getInfoPanel().getErrorDescription().getText());
 		assertEquals("",testNew.getInfoPanel().getErrorEstimate().getText());
 		
 		
 	}
 	
 	
 	/**
 	 * Testing update button
 	 */
 	@Test
 	public void updateButtonTest()
 	{
 		// Create new requirement panel
 		RequirementPanel testNew = new RequirementPanel(-1);
 		
 		String testName = "testName";
 		String testDescription = "testDescription";
 
 		// adding fields
 		testNew.getInfoPanel().getBoxName().setText(testName);
 		testNew.getInfoPanel().getBoxDescription().setText(testDescription);
 		testNew.getInfoPanel().getDropdownType().setSelectedItem(RequirementType.THEME);
 		testNew.getInfoPanel().getPriorityHigh().doClick();
 		testNew.getInfoPanel().getBoxEstimate().setText("4");
 		testNew.getInfoPanel().getBoxIteration().setText("Backlog");
 		testNew.getInfoPanel().keyReleased(null);
 		
 		assertEquals(true, testNew.getButtonPanel().getButtonOK().isEnabled());
 		assertEquals(true, testNew.getButtonPanel().getButtonClear().isEnabled());
 		assertEquals(true, testNew.getButtonPanel().getButtonCancel().isEnabled());
 		
 		testNew.getInfoPanel().update();
 		
 		
 		
 	}
 	
 	/**
 	 * Testing multiple updates
 	 */
 	@Test
 	public void allTestUpdate()
 	{
 		// Create new requirement panel
 		RequirementPanel testNew = new RequirementPanel(-1);
 		
 		String testName = "testName";
 		String testDescription = "testDescription";
 		
 		// adding fields
 		testNew.getInfoPanel().getBoxName().setText(testName);
 		testNew.getInfoPanel().getBoxDescription().setText(testDescription);
 		testNew.getInfoPanel().getDropdownType().setSelectedItem(RequirementType.SCENARIO);
 		testNew.getInfoPanel().getPriorityMedium().doClick();
 		testNew.getInfoPanel().getBoxEstimate().setText("0");
 		testNew.getInfoPanel().keyReleased(null);
 		testNew.getInfoPanel().validateFields(true);
 		// click update (without closing in order to retrieve information)
 		testNew.getInfoPanel().update();
 		
 		// Check 
 		assertEquals(testName,testNew.getDisplayRequirement().getName());
 		assertEquals(testDescription,testNew.getDisplayRequirement().getDescription());
 		assertEquals(RequirementType.SCENARIO,testNew.getDisplayRequirement().getType());
 		assertEquals("Backlog",testNew.getDisplayRequirement().getIteration());
 		assertEquals(RequirementStatus.NEW,testNew.getDisplayRequirement().getStatus());
 		assertEquals(RequirementPriority.MEDIUM,testNew.getDisplayRequirement().getPriority());
 		assertEquals(0,testNew.getDisplayRequirement().getEstimate());
 		
 		
 		// add more fields
 		testNew.getInfoPanel().getDropdownType().setSelectedItem(RequirementType.EPIC);
 		testNew.getInfoPanel().getPriorityLow().doClick();
 		testNew.getInfoPanel().validateFields(true);
 		testNew.getInfoPanel().update();
 
 		// check more results
 		assertEquals(RequirementType.EPIC,testNew.getDisplayRequirement().getType());
 		assertEquals(RequirementPriority.LOW,testNew.getDisplayRequirement().getPriority());
 		
 		// add different fields
 		testNew.getInfoPanel().getDropdownType().setSelectedItem(RequirementType.NONFUNCTIONAL);
 		testNew.getInfoPanel().getPriorityBlank().doClick();
 		testNew.getInfoPanel().validateFields(true);
 		testNew.getInfoPanel().update();
 
 		assertEquals(RequirementType.NONFUNCTIONAL,testNew.getDisplayRequirement().getType());
 		assertEquals(RequirementPriority.BLANK,testNew.getDisplayRequirement().getPriority());
 
 		
 	}
 
 }
