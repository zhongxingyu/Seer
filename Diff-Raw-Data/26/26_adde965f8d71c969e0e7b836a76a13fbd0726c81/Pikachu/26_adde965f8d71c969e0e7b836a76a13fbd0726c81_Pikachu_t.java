 package kkc;
 
 import java.awt.Color;
 import robocode.AdvancedRobot;
 import robocode.ScannedRobotEvent;
 import static robocode.util.Utils.normalRelativeAngleDegrees;
 
 /**
  * Pikachu is a robot in robocode that tracks enemy robot's energy levels.
  * If the enemy has a drop in energy we assume the enemy fired a bullet and 
  * our robot changes directions to avoid the bullet.  Then our robot scans
  * its radar to lock the gun on the enemy and fire bullet with power varying
  * according to distance of enemy.
  * 
  * @author Kellie Canida
  *
  */
 public class Pikachu extends AdvancedRobot {
   
   double energyOfEnemy = 100; //Initial energy of enemy at start of battle
   int directionToMove = 1; //Direction our robot will move
   int gunDirection = 1; //Initialized to 1 to create oscillating effect
   int scanDirection = 1; //Initialized to 1 to create oscillating effect
   
   
   @Override
   public void run() {
     //Set the color of our robot
     setColors(new Color(229,218,42), new Color(231,74,69), 
         new Color(0,0,0), new Color(255,255,1), Color.pink);
     
     //Do an initial radar scan of entire field to pick up our enemy robot.
    setTurnRadarRight(360 * scanDirection);
     
   }
   
  @Override
   public void onScannedRobot(ScannedRobotEvent event) {
     //Always stay at 90 degree angle to the enemy.
    setTurnRight(event.getBearing() + 90 - 30 * directionToMove);
     
     //If the enemy has a drop in energy of <=3 we assume the enemy has fired so we move.
     double changeInEnergy = energyOfEnemy - event.getEnergy();
    if (changeInEnergy > 0 && changeInEnergy <= 3) {
       //Our robot changes direction and moves closer towards enemy.
       directionToMove = -directionToMove;
      setAhead((event.getDistance() / 4 + 25) * directionToMove);
     }
     
     //Changes scanDirection so that radar oscillates. 
     scanDirection = -scanDirection; 
     setTurnRadarRight(360 * scanDirection);
     
     //Turn gun to face enemy.  Normalize angle so gun turns the shortest distance.
    setTurnGunRight(normalRelativeAngleDegrees(
                    getHeading() - getGunHeading() + event.getBearing()));
     
     //Fire at target with power varying with distance.
    if (event.getDistance() < 10 ) {
       fire(2);
     }   
    else if (event.getDistance() < 50) {
       fire(1.5);
     }
    else if (event.getDistance() < 200) {
       fire(1);
     }
     else {
       doNothing();
     }
     
     //Update the energy level of the enemy.
     energyOfEnemy = event.getEnergy();
     
   }
 }
