 package de.rstandke;
 
 import org.junit.Test;
 
 import static org.fest.assertions.Assertions.assertThat;
 
 public class RoverTest {
 
     @Test
     public void createRover_validDirections_createsRoverObj () throws Exception {
         // arrange
 
         // act
         Rover rover = new Rover(new Position(1,1),Direction.NORTH);
 
         // assert
         assertThat(rover).isNotNull();
 
     }
 
     @Test
     public void go_fbffb_1() throws Exception {
         // arrange
 
         // act
         Rover rover = new Rover(new Position(0, 0), Direction.EAST);
        rover.go(Command.FORWARD, Command.BACKWARD, Command.FORWARD, Command.FORWARD);
 
         // assert
         assertThat(rover).isNotNull();
         assertThat(rover.getPositionOnGrid().getX()).isEqualTo(1);
     }
 
 
 
 }
