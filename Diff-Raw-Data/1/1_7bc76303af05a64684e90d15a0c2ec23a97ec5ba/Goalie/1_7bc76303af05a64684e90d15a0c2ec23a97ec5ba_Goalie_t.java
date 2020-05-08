 package team25;
 
 import hockey.api.GoalKeeper;
 import hockey.api.Position;
 import hockey.api.Util;
 
 public class Goalie extends GoalKeeper {
     // Middle of our own goalcage, on the goal line
     protected static final Position GOAL_POSITION = new Position(-2600, 0);
     protected static final Position OPPONENT_GOAL_POSITION = new Position(2600, 0);
 
     // Number of the goalie.
     public int getNumber() { return 1; }
 
     // Name of the goalie.
     public String getName() { return "Bengan"; }
 
     // Left handed goalie
     public boolean isLeftHanded() { return true; }
 
     // Initiate
     public void init() { }
 
     // Face off
     public void faceOff() { }
 
     // Called when the goalie is about to receive a penalty shot
     public void penaltyShot() { }
 
     // Intelligence of goalie.
     public void step() {
 	    
       if (hasPuck()) {
         shoot(OPPONENT_GOAL_POSITION, MAX_SHOT_SPEED);
       } else {
         goalie_movement();
       }
     }
 
     private void goalie_movement() {
       int CAGE_RADIUS = 100;
       double ang = Util.datan2(getPuck(), GOAL_POSITION);
 
       ang = Util.clamp(-90,ang, 90);
       
       double heading = getTargetHeading();
 
       double newX = Util.clamp(GOAL_POSITION.getX() + 50, GOAL_POSITION.getX() + Math.cos(ang)*CAGE_RADIUS, 0);
 
       double newY = GOAL_POSITION.getY() + Math.sin(ang)*CAGE_RADIUS;
 
       if (newY > 0 && getPuck().getY() < 0) {
         newY = -newY;
       } else if (newY < 0 && getPuck().getY() > 0) {
         newY = -newY;
       }
       
       skate((int)newX, (int)newY, 100);
 	    
       //Face the puck
       turn(getPuck(), MAX_TURN_SPEED);
       
     }
 
 }
