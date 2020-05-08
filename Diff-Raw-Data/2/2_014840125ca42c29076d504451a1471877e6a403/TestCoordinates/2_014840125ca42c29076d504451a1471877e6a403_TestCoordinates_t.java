 package se.chalmers.project14.test;
 
 import junit.framework.TestCase;
 import se.chalmers.project14.model.Door;
 
 public class TestCoordinates extends TestCase {
 
	private Door door = new Door();
 
 	public void testGetDoors() {
 		door.setDoor("121212,121212");
 		String getDoor = door.getDoorCoordinates();
 		String expectedResult = "121212,121212";
 		assertEquals(expectedResult, getDoor);
 	}
 
 	public void testGetBuilding() {
 		door.setBuilding("Maskinhuset");
 		String getBuilding = door.getBuilding();
 		String expectedResult = "Maskinhuset";
 		assertEquals(expectedResult, getBuilding);
 	}
 }
