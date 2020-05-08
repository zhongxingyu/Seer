 import java.util.ArrayList;
 
 //import robot.Robot;
 /**
  * Stub class - placeholder for Charlie and Henry's class
  * 
  * @author Haley B-E and Brendan Redmond
  * @version 0.1.0
  */
 public class Builder  
 {
     public Builder()
     {
         
     }
     
     public ArrayList<RobotActor> getRobots()
     {
         ArrayList<RobotActor> robots = new ArrayList<RobotActor>();
        for (int i = 0; i < 8; i++)
        {
            robots.add(new RobotActor());
        }
         
         return robots;
     }
 }
