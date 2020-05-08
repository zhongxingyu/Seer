 package main;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class RoverExecutor {
     public static List<RoverPosition> executeRovers(List<String> input) {
         int i = 0;
         List<RoverPosition> roverPositionList = new ArrayList<RoverPosition>();
         while (i < input.size()) {
 
             String position = input.get(i);
             String command = input.get(i + 1);
             RoverPosition roverPosition = RoverPosition.parse(position);
             RoverCommand.execute(roverPosition, command);
             roverPositionList.add(roverPosition);

             i += 2;
         }
         return roverPositionList;
     }
 }
