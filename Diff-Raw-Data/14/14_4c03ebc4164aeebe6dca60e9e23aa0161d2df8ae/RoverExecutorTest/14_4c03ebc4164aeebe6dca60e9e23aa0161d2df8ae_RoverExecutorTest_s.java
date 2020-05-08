 package test;
 
 import main.Rover;
 import main.RoverCommand;
 import main.RoverExecutor;
 import org.junit.Test;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import static org.hamcrest.CoreMatchers.is;
 import static org.junit.Assert.assertThat;
 import static org.mockito.Mockito.mock;
 
 public class RoverExecutorTest {
     @Test
     public void shouldExecuteRoverCommands(){
         Rover rover = mock(Rover.class);
         RoverCommand roverCommand= mock(RoverCommand.class);
         List<String> inputList = new ArrayList<String>();
         inputList.add("1 2 N");
         inputList.add("MLMLM");
         inputList.add("1 3 E");
         inputList.add("MLM");
         inputList.add("1 4 W");
         inputList.add("RRM");
 
         List<Rover> roverList = RoverExecutor.executeRovers(inputList);
        assertThat(roverList.get(0).toString(),is("0 2 S"));
         assertThat(roverList.get(1).toString(),is("2 4 N"));
         assertThat(roverList.get(2).toString(),is("2 4 E"));
 
     }
 }
