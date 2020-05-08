 package tw.wn.rover;
 
import org.junit.Ignore;
 import org.junit.Test;
 import tw.wn.orientation.Orientation;
 import tw.wn.roverstatus.RoverStatus;
 
 import static org.junit.Assert.assertEquals;
 
 public class RoverTest {
 
     private Rover rover;
 
    @Ignore
     @Test
     public void runCommandAndChangeStatusFor12N() {
         rover = new Rover(new RoverStatus(1, 2, Orientation.N));
         rover.run("LMLMLMLMM");
         assertEquals(new Rover(new RoverStatus(1, 5, Orientation.N)), rover);
     }
 
    @Ignore
     @Test
     public void runCommandAndChangeStatusFor33E() {
         rover = new Rover(new RoverStatus(3, 3, Orientation.E));
         rover.run("MMRMMRMRRM");
         assertEquals(new Rover(new RoverStatus(5, 5, Orientation.E)), rover);
     }
 }
