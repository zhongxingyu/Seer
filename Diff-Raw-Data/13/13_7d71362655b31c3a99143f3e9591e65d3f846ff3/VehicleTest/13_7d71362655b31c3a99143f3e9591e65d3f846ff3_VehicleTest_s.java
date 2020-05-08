 package carrental;
 
 import org.junit.Test;
 import static org.junit.Assert.*;
import java.util.ArrayList;
import java.util.Arrays;
 
 /**
  *
  * @author Niclas
  */
 public class VehicleTest {
 
     private static Vehicle vehicle;
 
     public VehicleTest() {
         vehicle = new Vehicle(1, 2, "This is a description", "This is the licenseplate", 20000, "Additional comment");
     }
     
     /**
      * Test of updateObject method, of class Vehicle.
      */
     @Test
     public void testUpdateObject() {
         assertEquals(2, vehicle.getVehicleType());
        //ArrayList<String> update = new ArrayList<String>();
        String[] strings = new String[]{
            "3", 
            "This is a NEW description", 
            "This is the NEW licenseplate", 
            "10000", 
            "NEW Additional comment"
        };
        vehicle.updateObject(new ArrayList<>(Arrays.asList(strings)));
         //test that the vehicle's fields equals the new values.
         assertEquals(3, vehicle.getVehicleType());
         assertEquals("This is a NEW description", vehicle.getDescription());
         assertEquals("This is the NEW licenseplate", vehicle.getLicensplate());
         assertEquals(10000, vehicle.getOdo());
         assertEquals("NEW Additional comment", vehicle.getAdditional());
     }
 
     /**
      * Test of getID method, of class Vehicle.
      */
     @Test
     public void testGetID() {
         assertEquals(1, vehicle.getID());
         vehicle = new Vehicle(4, 2, "This is a description", "This is the licenseplate", 42, "Additional comment");
         assertEquals(4, vehicle.getID());
     }
 }
