 import static org.junit.Assert.*;
 
 import org.junit.Test;
 
 public class BoatTest {
 
 	@Test
 	
 	public void canImplementVehicle() {
 		Boat b = new Boat();
 		assertEquals("Can get make", "None", b.getMake());
 	}
 	
 	@Test
 	
 	public void defaultConstructor() { 
 		Boat b = new Boat();
 		assertEquals("Default range is correct", 0, b.getRange());
 		assertEquals("Default radius is correct", 0, b.getSteeringWheelRadius());
 		assertEquals("Default make is correct", "None", b.getMake());
 		assertEquals("Default model is correct", "None", b.getModel());
 		assertEquals("Default year is correct", 0, b.getYear());
 	}
 	
 	@Test
 	
 	public void paramConstrubtor() {
 		Boat b = new Boat(20, "Blueberry", "Spam", 2006, 50);
		assertEquals("Specified range is correct", 50, b.getRange());
		assertEquals("Specified radius is correct", 20, b.getSteeringWheelRadius());
		assertEquals("Specified make is correct", "Blueberry", b.getMake());
		assertEquals("Specified model is correct", "Spam", b.getModel());
		assertEquals("Specified year is correct", 2006, b.getYear());
 	}
 	
 	@Test
 	
 	public void outputDetails() {
 		Boat b = new Boat(20, "oof", "rab", 2008, 100);
 		String boatDetails = "Boat\nMake:\toof\nModel:\trab\nYear:\t2008\nRange:\t100\n\n";
 		assertEquals("Range calculation for gasoline cars works", boatDetails, b.getDetails());
 	}
 
 }
