 package tw.wn.marsrovers;
 
 import org.junit.Test;
 import tw.wn.rover.Rover;
 
 import static org.junit.Assert.assertEquals;
 
 public class MarsRoverTest {
     MarsRovers marsRover = new MarsRovers();
     String output = marsRover.exectue("5 5\n" +
             "1 2 N\n" +
             "LMLMLMLMM\n" +
             "3 3 E\n" +
             "MMRMMRMRRM");
     @Test
     public void inputAndOutput() {
        assertEquals(output, "1 2 N\n" +
                "3 3 E\n");
     }
 
     @Test
     public void display12NRover(){
         Rover rover = marsRover.getRover(0);
        assertEquals(MarsRovers.display(rover), "1 2 N\n");
     }
 
     @Test
     public void display33ERover(){
         Rover rover = marsRover.getRover(1);
        assertEquals(MarsRovers.display(rover),"3 3 E\n");
     }
 }
