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
 
import static org.junit.Assert.*;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import edu.wpi.cs.wpisuitetng.modules.requirementmanager.MockNetwork;
 import edu.wpi.cs.wpisuitetng.modules.requirementmanager.models.Requirement;
 import edu.wpi.cs.wpisuitetng.modules.requirementmanager.models.characteristics.RequirementPriority;
 import edu.wpi.cs.wpisuitetng.modules.requirementmanager.models.characteristics.RequirementStatus;
 import edu.wpi.cs.wpisuitetng.modules.requirementmanager.models.characteristics.RequirementType;
import edu.wpi.cs.wpisuitetng.modules.requirementmanager.models.characteristics.TransactionHistory;
 import edu.wpi.cs.wpisuitetng.modules.requirementmanager.models.iterations.Iteration;
 import edu.wpi.cs.wpisuitetng.modules.requirementmanager.models.iterations.IterationModel;
 import edu.wpi.cs.wpisuitetng.modules.requirementmanager.view.requirements.EditRequirementPanel;
import edu.wpi.cs.wpisuitetng.modules.requirementmanager.view.requirements.NewRequirementPanel;
 import edu.wpi.cs.wpisuitetng.network.Network;
 import edu.wpi.cs.wpisuitetng.network.configuration.NetworkConfiguration;
 
 public class EditRequirementPanelTest {
 
 	@Before
 	public void setUp() throws Exception {
 		// Mock network
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
 	public void defaultEnabilityAndField()
 	{
 		// Create a mock requirement
 		Requirement testRequirement = new Requirement();
 		String testName = "test: Name";
 		String testDescription = "test: Description";
 		String testRelease = "1.0.2";
 		int testEstimate = 0;
 		//int testActualEffort = 0;
 		
 		// Set fields of the mock requirement
 		testRequirement.setName(testName);
 		testRequirement.setDescription(testDescription);
 		testRequirement.setRelease(testRelease);
 		testRequirement.setStatus(RequirementStatus.NEW, true);
 		testRequirement.setPriority(RequirementPriority.LOW,true);
 		testRequirement.setEstimate(testEstimate);
 		testRequirement.setType(RequirementType.EPIC);
 		//testRequirement.setEstimate(testActualEffort);
 		
 		// Create an editRequirementPanel
 		EditRequirementPanel testEdit = new EditRequirementPanel(testRequirement);
 		
 		// Check enability of all fields
 		assertEquals(true, testEdit.getBoxName().isEnabled());
 		assertEquals(true, testEdit.getBoxDescription().isEnabled());
 		assertEquals(false, testEdit.getBoxIteration().isEnabled());
 		assertEquals(true, testEdit.getDropdownType().isEnabled());
 		assertEquals(true, testEdit.getDropdownStatus().isEnabled());
 		assertEquals(true, testEdit.getPriorityHigh().isEnabled());
 		assertEquals(true, testEdit.getPriorityMedium().isEnabled());
 		assertEquals(true, testEdit.getPriorityLow().isEnabled());
 		assertEquals(true, testEdit.getPriorityBlank().isEnabled());
 		assertEquals(true, testEdit.getBoxEstimate().isEnabled());
 		assertEquals(false, testEdit.getButtonUpdate().isEnabled());
 		assertEquals(false, testEdit.getButtonClear().isEnabled());
 		assertEquals(true, testEdit.getButtonCancel().isEnabled());
 		assertEquals(true, testEdit.getButtonDelete().isEnabled());
 		
 		// Check for default values in each field
 		assertEquals(testName,testEdit.getBoxName().getText());
 		assertEquals(testDescription,testEdit.getBoxDescription().getText());
 		assertEquals(testRelease,testEdit.getBoxReleaseNum().getText());
 		assertEquals(RequirementStatus.NEW,testEdit.getDropdownStatus().getSelectedItem());
 		assertEquals(true,testEdit.getPriorityLow().isSelected());
 		assertEquals("0",testEdit.getBoxEstimate().getText());
 		assertEquals(RequirementType.EPIC,testEdit.getDropdownType().getSelectedItem());
 		
 	}
 
 	/**
 	 * Check the error messages when invalid fields are filled
 	 */
 	@Test
 	public void errorFieldTest() 
 	{
 		// Create mock requirement
 		Requirement testRequirement = new Requirement();
 		
 		//int testActualEffort = 0;
 		
 		//testRequirement.setEstimate(testActualEffort);
 		
 		EditRequirementPanel testEdit = new EditRequirementPanel(testRequirement);
 		
 		String errorMessageNoninterger = "** Please enter a non-negative integer";
 		String errorMessageNoMore100 = "No more than 100 chars";
 		String errorMessageRequiredDescription = "** Description is REQUIRED";
 		
 		String hundredCharText = "0";
 		// Generate Hundred character string
 		for(int i = 0; i<100; i++)
 		{
 			hundredCharText = hundredCharText +"0";
 		}
 		
 		// adding invalid fields
 		testEdit.getBoxName().setText(hundredCharText);
 		testEdit.getBoxEstimate().setText("-134");
 		testEdit.getBoxDescription().setText("Desc.");
 		testEdit.keyReleased(null);
 		testEdit.getButtonUpdate().doClick();
 		
 		// has to be nonnegative, has to have name, has to have description
 		assertEquals(errorMessageNoninterger,testEdit.getErrorEstimate().getText());
 		assertEquals(errorMessageNoMore100,testEdit.getErrorName().getText());
 
 		// Iteration is unable, Dropdown status is enable
 		assertEquals(false, testEdit.getBoxIteration().isEnabled());
 		assertEquals(true, testEdit.getDropdownStatus().isEnabled());
 		
 		testEdit.getBoxName().setText(hundredCharText);
 		testEdit.getBoxEstimate().setText("StringCharacter");
 		testEdit.getBoxDescription().setText(null);
 		testEdit.getButtonUpdate().doClick();
 		
 		assertEquals(errorMessageNoMore100,testEdit.getErrorName().getText());
 		assertEquals(errorMessageNoninterger,testEdit.getErrorEstimate().getText());
 		assertEquals(errorMessageRequiredDescription,testEdit.getErrorDescription().getText());
 		
 	}
 	
 	
 	/**
 	 * Check when required fields are not filled
 	 */
 	@Test
 	public void errorRequiredFieldTest() {
 		
 		// Moc requirement
 		Requirement testRequirement = new Requirement();
 		String testName = "test: Name";
 		String testDescription = "test: Description";
 		String testRelease = "1.0.2";
 		int testEstimate = 0;
 		//int testActualEffort = 0;
 		testRequirement.setName(testName);
 		testRequirement.setDescription(testDescription);
 		testRequirement.setRelease(testRelease);
 		testRequirement.setStatus(RequirementStatus.NEW, true);
 		testRequirement.setPriority(RequirementPriority.HIGH,true);
 		testRequirement.setEstimate(testEstimate);
 		testRequirement.setType(RequirementType.EPIC);
 		//testRequirement.setEstimate(testActualEffort)/;
 		
 		EditRequirementPanel testEdit = new EditRequirementPanel(testRequirement);
 		// a field is added correctly but both name and description are filled with blanks
 		testEdit.getBoxEstimate().setText("-134");
 		testEdit.getBoxName().setText("  ");
 		testEdit.getBoxDescription().setText("Desc.");
 		// release pressed key
 		testEdit.keyReleased(null);
 		
 		// can't create because no name/description, but a field has been changed
 		assertEquals(false, testEdit.getButtonUpdate().isEnabled());
 		assertEquals(true, testEdit.getButtonClear().isEnabled());
 		assertEquals(true, testEdit.getButtonCancel().isEnabled());
 		
 		testEdit.getBoxName().setText("Name");
 		testEdit.getBoxDescription().setText(" ");
 		// release pressed key
 		testEdit.keyReleased(null);
 		
 		// can't create because no name/description, but a field has been changed
 		assertEquals(false, testEdit.getButtonUpdate().isEnabled());
 		assertEquals(true, testEdit.getButtonClear().isEnabled());
 		assertEquals(true, testEdit.getButtonCancel().isEnabled());
 		
 
 	}
 	
 	/**
 	 * Check when undo the change
 	 */
 	@Test
 	public void undoChangeButtonTest() {
 		
 		Requirement testRequirement = new Requirement();
 		String testName = "test: Name";
 		String testDescription = "test: Description";
 		String testRelease = "1.0.2";
 		int testEstimate = 0;
 		//int testActualEffort = 0;
 		
 		testRequirement.setName(testName);
 		testRequirement.setDescription(testDescription);
 		testRequirement.setRelease(testRelease);
 		testRequirement.setStatus(RequirementStatus.NEW, true);
 		testRequirement.setPriority(RequirementPriority.MEDIUM,true);
 		testRequirement.setEstimate(testEstimate);
 		testRequirement.setType(RequirementType.EPIC);
 		//testRequirement.setEstimate(testActualEffort);
 		
 		EditRequirementPanel testEdit = new EditRequirementPanel(testRequirement);
 		
 		// set to each field random stuffs to test clear functionality
 		
 		testEdit.getBoxName().setText(testName);
 		testEdit.getBoxDescription().setText(testDescription);
 		testEdit.getBoxIteration().setText("Iteration test");
 		testEdit.getDropdownType().setSelectedItem(RequirementType.SCENARIO);
 		testEdit.getDropdownStatus().setSelectedItem(RequirementStatus.INPROGRESS);
 		testEdit.getPriorityMedium().doClick();
 		testEdit.getBoxEstimate().setText("4");
 		
 		
 		testEdit.getButtonClear().doClick();
 		
 		assertEquals(testName,testEdit.getBoxName().getText());
 		assertEquals(testDescription,testEdit.getBoxDescription().getText());
 		assertEquals(testRelease,testEdit.getBoxReleaseNum().getText());
 		assertEquals(RequirementStatus.NEW,testEdit.getDropdownStatus().getSelectedItem());
 		assertEquals(true,testEdit.getPriorityMedium().isSelected());
 		assertEquals("0",testEdit.getBoxEstimate().getText());
 		assertEquals(RequirementType.EPIC,testEdit.getDropdownType().getSelectedItem());
 		
 		
 	}
 	
 	/**
 	 * test fifferent fields
 	 */
 	@Test
 	public void updateButtonTest2()
 	{
 		Requirement testRequirement = new Requirement();
 		String testName = "test: Name";
 		String testDescription = "test: Description";
 		String testRelease = "1.0.2";
 		int testEstimate = 0;
 		//int testActualEffort = 0;
 		
 		String updateTestName = "Update test: Name";
 		String updateTestDescription = "Update test: Description";
 		String updateTestRelease = "1.0.3";
 		int updateTestEstimate = 1;
 		
 		testRequirement.setName(testName);
 		testRequirement.setDescription(testDescription);
 		testRequirement.setRelease(testRelease);
 		testRequirement.setStatus(RequirementStatus.NEW, true);
 		testRequirement.setPriority(RequirementPriority.LOW,true);
 		testRequirement.setEstimate(testEstimate);
 		testRequirement.setType(RequirementType.EPIC);
 		testRequirement.setIteration("Backlog",true);
 		//testRequirement.setEstimate(testActualEffort);
 		
 		EditRequirementPanel testEdit = new EditRequirementPanel(testRequirement);
 		
 		testEdit.getBoxName().setText(updateTestName);
 		testEdit.getBoxDescription().setText(updateTestDescription);
 		testEdit.getDropdownType().setSelectedItem(RequirementType.THEME);
 		testEdit.getDropdownStatus().setSelectedItem(RequirementStatus.INPROGRESS);
 		testEdit.getPriorityHigh().doClick();
 		testEdit.getBoxEstimate().setText("4");
 		testEdit.getBoxIteration().setText("Iteration test");
 		testEdit.update();
 		
 		assertEquals(updateTestName,testEdit.getRequirementBeingEdited().getName());
 		assertEquals(updateTestDescription,testEdit.getRequirementBeingEdited().getDescription());
 		assertEquals(RequirementType.THEME,testEdit.getRequirementBeingEdited().getType());
 		assertEquals("Iteration test",testEdit.getRequirementBeingEdited().getIteration());
 		assertEquals(RequirementStatus.INPROGRESS,testEdit.getRequirementBeingEdited().getStatus());
 		assertEquals(RequirementPriority.HIGH,testEdit.getRequirementBeingEdited().getPriority());
 		assertEquals(4,testEdit.getRequirementBeingEdited().getEstimate());
 	}
 	
 }
 	
 	
