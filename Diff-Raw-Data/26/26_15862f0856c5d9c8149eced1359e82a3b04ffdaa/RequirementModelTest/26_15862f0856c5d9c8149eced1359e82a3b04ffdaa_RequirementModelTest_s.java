 package edu.wpi.cs.wpisuitetng.modules.RequirementManager.models;
 
 import static org.junit.Assert.*;
 
 import java.util.ArrayList;
 
 import org.junit.Test;
 
 public class RequirementModelTest {
 
 	@Test
 	public void addRequirementTests(){
 		RequirementModel model = RequirementModel.getInstance();
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
 	public void addRequirementListTest() {
 		RequirementModel model2 = RequirementModel.getInstance();
 		Requirement req1 = new Requirement(3, "Req1", "Tester");
 		Requirement req2 = new Requirement(4, "Req2", "Another tester");
 		Requirement req3 = new Requirement(5, "Req3", "Last tester requirement");
 		Requirement reqList[] = new Requirement[3];
 		reqList[0] = req1;
 		reqList[1] = req2;
 		reqList[2] = req3;
 		
		model2.addRequirements(reqList);
		Requirement returned1 = model2.getElementAt(0);
		Requirement returned2 = model2.getElementAt(1);
		Requirement returned3 = model2.getElementAt(2);
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
 }
