 //dresselJames07.java
 
 //Fault: static defect
 //Error: incorrect interal state
 //Failure: External, incorrect behavior
 
 import static org.junit.Assert.*;
 
 import org.junit.Test;
 
 
 public class dresselJames07 {
 	
 	/*Part A Identify the fault:
 	 * The fault is that Vehicle's clone returns new Vehicle(this.x) instead of 
 	 * using super.clone. The result is that Object's clone is never called, so
 	 * the result is not of the correct class, and thus, cannot be cast into a 
 	 * Truck.   
 	 */
 
 	//Part B If possible, identify a test case that does not execute the fault.
 	
 	
 	@Test
	public void testCNoErrorState() {
		//Part C
		//Test that executes fault, but does not result in an error state
		Vehicle vehicleOriginal = new Vehicle(23);
		Vehicle vehicleClone = (Vehicle)vehicleOriginal.clone();
		
		assertFalse(vehicleOriginal==vehicleClone);
		assertEquals(vehicleOriginal.getClass(), vehicleClone.getClass());
		assertEquals(vehicleOriginal, vehicleClone);//Not strictly necessary, but Bloch recommends
		
		}
	
 
 	//Part D if possible identify a test case that results in an error, but not a failure. Hint: Don't forget about the program counter
 	//Part E For the given test case, identify the first error state. Be sure to describe the complete state. 
 	//Part F Fix the fault and verify that the given test now produces the expected output. 
 	
 
 }
