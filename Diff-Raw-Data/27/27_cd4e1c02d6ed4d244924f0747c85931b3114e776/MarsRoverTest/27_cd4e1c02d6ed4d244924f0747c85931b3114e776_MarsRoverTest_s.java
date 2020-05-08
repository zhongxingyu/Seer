 package test;
 
 
 import main.MarsRover;
 import org.junit.Test;
 import org.mockito.Mock;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.List;
 
 import static org.hamcrest.CoreMatchers.is;
 import static org.junit.Assert.assertThat;
 
 public class MarsRoverTest {
 
     @Test
     public void shouldReadInputFromFile() throws IOException {
         MarsRover marsRover=new MarsRover();
        List teststring = marsRover.readFromFile("C:\\Users\\sabhinay\\Mars-Rover\\Mars-Rover1\\src\\main\\input.txt");
        assertThat((String) teststring.get(0),is("5 5"));
     }
 }
