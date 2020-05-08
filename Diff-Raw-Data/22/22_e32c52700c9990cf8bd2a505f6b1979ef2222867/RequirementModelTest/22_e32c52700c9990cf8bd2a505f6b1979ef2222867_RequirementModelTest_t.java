 /*******************************************************************************
  * Copyright (c) 2013 WPI-Suite
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  ******************************************************************************/
 package edu.wpi.cs.wpisuitetng.modules.requirementmanager.models;
 
 import static org.junit.Assert.*;
 
 import java.util.ArrayList;
 import java.util.List;
 
import org.junit.Before;
 import org.junit.Test;
 
import edu.wpi.cs.wpisuitetng.modules.requirementmanager.MockNetwork;
import edu.wpi.cs.wpisuitetng.modules.requirementmanager.models.iterations.Iteration;
import edu.wpi.cs.wpisuitetng.modules.requirementmanager.models.iterations.IterationModel;
import edu.wpi.cs.wpisuitetng.modules.requirementmanager.view.ViewEventController;
import edu.wpi.cs.wpisuitetng.modules.requirementmanager.view.overview.OverviewTable;
import edu.wpi.cs.wpisuitetng.network.Network;
import edu.wpi.cs.wpisuitetng.network.configuration.NetworkConfiguration;

 public class RequirementModelTest {
 
	
	
	@Before
	public void setUp() throws Exception {
		ViewEventController viewCon = ViewEventController.getInstance();
		
		OverviewTable ovTable = new OverviewTable(null, null);
		viewCon.setOverviewTable(ovTable);
		 
	}
	
 	@Test
 	public void addRequirementTests(){
 		RequirementModel model = RequirementModel.getInstance();
 		model.emptyModel();	// make sure this is a fresh model
 		assertEquals(model.getSize(), 0);
 		Requirement object = new Requirement(4, "requirement a", "A requirement used for testing.");
 		model.addRequirement(object);
 		assertEquals(model.getSize(), 1);
 		Requirement objectReturned = model.getElementAt(0);
 		assertEquals(objectReturned.getName(), "requirement a");
 		assertEquals(objectReturned.getId(), 4);
 		assertEquals(objectReturned.getDescription(), "A requirement used for testing.");
 	}
 	
 	@Test
 	public void removeRequirementTest() {
 		RequirementModel model = RequirementModel.getInstance();
 		model.emptyModel();	// make sure this is a fresh model
 		assertEquals(0, model.getSize());
 		Requirement object = new Requirement(2, "THE REQUIREMENT", "The Test");
 		Requirement object2 = new Requirement(3, "other requirement", "The other test");
 		model.addRequirement(object);
 		assertEquals(1, model.getSize());
 		model.addRequirement(object2);
 		assertEquals(2, model.getSize());
 		model.removeRequirement(3);
 		assertEquals(1, model.getSize());
 		model.removeRequirement(2);
 		assertEquals(0, model.getSize());
 		model.removeRequirement(10);	// testing on something that is not there
 	}
 	
 	@Test
 	public void addRequirementListTest() {
 		RequirementModel model = RequirementModel.getInstance();
 		model.emptyModel();
 		assertEquals(0, model.getSize());
 		Requirement req1 = new Requirement(3, "Req1", "Tester");
 		Requirement req2 = new Requirement(4, "Req2", "Another tester");
 		Requirement req3 = new Requirement(5, "Req3", "Last tester requirement");
 		Requirement reqList[] = new Requirement[3];
 		reqList[0] = req1;
 		reqList[1] = req2;
 		reqList[2] = req3;
 		
 		model.addRequirements(reqList);
 		Requirement returned1 = model.getElementAt(2);
 		Requirement returned2 = model.getElementAt(1);
 		Requirement returned3 = model.getElementAt(0);
 		assertEquals(returned1.getName(), "Req1");
 		assertEquals(returned1.getId(), 3);
 		assertEquals(returned1.getDescription(), "Tester");
 		assertEquals(returned2.getName(), "Req2");
 		assertEquals(returned2.getId(), 4);
 		assertEquals(returned2.getDescription(), "Another tester");
 		assertEquals(returned3.getName(), "Req3");
 		assertEquals(returned3.getId(), 5);
 		assertEquals(returned3.getDescription(), "Last tester requirement");
 
 	}
 	
 	@Test
 	public void listRequirementTest() {
 		RequirementModel model = RequirementModel.getInstance();
 		model.emptyModel();
 		assertEquals(0, model.getSize());
 		Requirement req1 = new Requirement(3, "Req1", "Tester");
 		Requirement req2 = new Requirement(4, "Req2", "Another tester");
 		Requirement req3 = new Requirement(5, "Req3", "Last tester requirement");
 		Requirement reqList[] = new Requirement[3];
 		reqList[0] = req1;
 		reqList[1] = req2;
 		reqList[2] = req3;
 		
 		model.addRequirements(reqList);
 		List<Requirement> returnedList = model.getRequirements();
 		assertEquals(reqList[0], returnedList.get(0));
 		assertEquals(reqList[1], returnedList.get(1));
 		assertEquals(reqList[2], returnedList.get(2));
 	}
 }
