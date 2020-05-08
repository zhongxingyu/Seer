 package strategies.line_follower;
 
 import static robot.Platform.ENGINE;
 import static robot.Platform.HEAD;
 import lejos.util.Stopwatch;
 import robot.Platform;
 import strategies.Strategy;
 import utils.Utils.Side;
 
 public class LineFollowerController extends Strategy {
 
     private static final int SEARCH_LINE_SPEED = 400;
     private static final int HALT_TIME = 2000;
     private static final int MAX_GAP_SIZE = 140; // > 100mm
     
     private final LineFinderStrategy finder = new LineFinderStrategy();
     private final LineFollowerStrategy follower = new LineFollowerStrategy();
     
     private State state = State.OFF_LINE;
     private Stopwatch timer = new Stopwatch();
     private float noLineDistance = 0;
     
     @Override
     protected void doInit() {
         State.OFF_LINE.transitionTo(this);
         
         System.out.println("init: " + state);
     }
 
     @Override
     protected void doRun() {
         switch (state) {
             case ON_LINE:
                 follower.run();
 
                 if (follower.isFinished()) {                    
                     State.JUST_LOST_LINE.transitionTo(this);
                     
                     System.out.println("on_line -> " + state);
                 }
                 break;
             case JUST_LOST_LINE:
                 finder.run();
                 
                 if (finder.isFinished()) {                    
                     State.ON_LINE.transitionTo(this);
                     
                     System.out.println("just_lost_line -> " + state);
                 } else if (timer.elapsed() > HALT_TIME) {
                     State.OFF_LINE.transitionTo(this);
                     
                     System.out.println("just_lost_line -> " + state);
                 }
                 break;
             case OFF_LINE:
                 noLineDistance += ENGINE.estimateDistance();
                 
                 finder.run();
                 
                 if (finder.isFinished()) {                    
                     State.ON_LINE.transitionTo(this);
                     
                     System.out.println("off_line -> " + state);
                 } else if (noLineDistance > MAX_GAP_SIZE) {
                     State.OFF_LINE_NO_GAP.transitionTo(this);
                     
                     System.out.println("off_line -> " + state);
                 }
                 break;
             case OFF_LINE_NO_GAP:
                 // TODO: Distance to long for a gap, more sophisticated algorithms anyone?!
                 if (!HEAD.isMoving()) {
                     setFinished();
                     
                     System.out.println("finished");
                 }
                 break;
         }
     }
     
     public State getState() {
         return state;
     }
     
     /**
      * Sets/Overwrites the current state of the LineFollowerController instance.
      * May be used to select a better start state depending on the environment.
      *  
      * @param state the new state set
      */
     public void setState(final State state) {
         this.state = state;
     }
     
     public int getSpeed() {
         return follower.getSpeed();
     }
     
     /**
      * @see LineFollowerStrategy#setSpeed
      */
     public void setSpeed(final int speed) {
         follower.setSpeed(speed);
     }
     
     public Side getTrackingSide() {
         return follower.getTrackingSide();
     }
     
     /**
      * Sets the tracking side. This method should not be called if the robot
      * is following a line.
      * 
      * @see LineFollowerStrategy#setTrackingSide
      */
     public void setTrackingSide(final Side side) {
         finder.setTrackingSide(side);
         follower.setTrackingSide(side);
     }
     
     /**
      * Checks if the light sensor reads a value high enough to follow a line.
      * 
      * @return true if the light sensor reads "good" values, else false 
      */
     public boolean lineValueOk() {
         return Platform.HEAD.getLight() > LineFinderStrategy.DETECTION_THRESHOLD; 
     }
     
     public static enum State {
         ON_LINE {
             @Override
             void doTransition(final LineFollowerController ctrl) {                
                 ctrl.follower.init();
             }
         },
         JUST_LOST_LINE {
             @Override
             void doTransition(LineFollowerController ctrl) {
                 ctrl.finder.init(); // init line finder
 
                 ctrl.timer.reset(); // reset timer for movement start
             }
         }, 
         OFF_LINE {
             @Override
             void doTransition(final LineFollowerController ctrl) {
                 ctrl.noLineDistance = 0;
 
                 ENGINE.move(SEARCH_LINE_SPEED);
             }
         },
         OFF_LINE_NO_GAP {
             @Override
             void doTransition(final LineFollowerController ctrl) {
                HEAD.moveTo(0, true);
                 ENGINE.stop();
             }
         };
         
         // FOR INTERNAL USE ONLY, DO NOT CALL THIS METHOD DIRECTLY!
         abstract void doTransition(final LineFollowerController ctrl);
         
         final void transitionTo(final LineFollowerController ctrl) {
             ctrl.state = this;
             
             doTransition(ctrl);
         }
     }
 }
