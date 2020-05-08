 package edu.mines.csci598.recycler.frontend.motion;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import org.apache.log4j.Logger;
 
 import edu.mines.csci598.recycler.frontend.GameLogic;
 import edu.mines.csci598.recycler.frontend.MotionState;
 import edu.mines.csci598.recycler.frontend.Recyclable;
 import edu.mines.csci598.recycler.frontend.graphics.Coordinate;
 import edu.mines.csci598.recycler.frontend.graphics.GameScreen;
 import edu.mines.csci598.recycler.frontend.graphics.Line;
 import edu.mines.csci598.recycler.frontend.graphics.Path;
 import edu.mines.csci598.recycler.frontend.utils.GameConstants;
 
 public class ConveyorBelt extends ItemMover{
 	private static final Logger logger = Logger.getLogger(ConveyorBelt.class);
 	
     private final Path path;
 
     //Left Path
     private static final Line bottomLineLeft = new Line(GameConstants.LEFT_BOTTOM_PATH_START_X, GameConstants.LEFT_BOTTOM_PATH_START_Y,
             GameConstants.LEFT_BOTTOM_PATH_END_X, GameConstants.LEFT_BOTTOM_PATH_END_Y);
     private static final Line verticalLineLeft = new Line(GameConstants.LEFT_VERTICAL_PATH_START_X, GameConstants.LEFT_VERTICAL_PATH_START_Y,
             GameConstants.LEFT_VERTICAL_PATH_END_X, GameConstants.LEFT_VERTICAL_PATH_END_Y);
     private static final Line topLineLeft = new Line(GameConstants.LEFT_TOP_PATH_START_X, GameConstants.LEFT_TOP_PATH_START_Y,
             GameConstants.LEFT_TOP_PATH_END_X, GameConstants.LEFT_TOP_PATH_END_Y);
     public static final Path CONVEYOR_BELT_PATH_LEFT = new Path(Arrays.asList(bottomLineLeft, verticalLineLeft, topLineLeft));
 
     //Right Path
     private static final Line bottomLineRight= new Line(GameConstants.RIGHT_BOTTOM_PATH_START_X, GameConstants.RIGHT_BOTTOM_PATH_START_Y,
             GameConstants.RIGHT_BOTTOM_PATH_END_X, GameConstants.RIGHT_BOTTOM_PATH_END_Y);
     private static final Line verticalLineRight = new Line(GameConstants.RIGHT_VERTICAL_PATH_START_X, GameConstants.RIGHT_VERTICAL_PATH_START_Y,
             GameConstants.RIGHT_VERTICAL_PATH_END_X, GameConstants.RIGHT_VERTICAL_PATH_END_Y);
     private static final Line topLineRight = new Line(GameConstants.RIGHT_TOP_PATH_START_X, GameConstants.RIGHT_TOP_PATH_START_Y,
             GameConstants.RIGHT_TOP_PATH_END_X, GameConstants.RIGHT_TOP_PATH_END_Y);
     public static final Path CONVEYOR_BELT_PATH_RIGHT = new Path(Arrays.asList(bottomLineRight,verticalLineRight,topLineRight));
 
     public ConveyorBelt(GameLogic game,GameScreen gameScreen,Path path) {
         super(GameConstants.INITIAL_SPEED_IN_PIXELS_PER_SECOND);
     	recyclables = new ArrayList<Recyclable>();
     	this.path = path;
     }
     
     public int getNumRecyclablesOnConveyor(){
         return recyclables.size();
     }
     
     /*
      * Returns the next touchable recyclable
      */
     public Recyclable getNextRecyclableThatIsTouchable(){
         Recyclable ret;
         int index=0;
         ret = recyclables.get(index);
         while(!(ret.isTouchable()) && index < recyclables.size()){
             index++;
             if(index<recyclables.size()-1)
                 ret = recyclables.get(index);
         }
 
         return ret;
     }
 
     public void setSpeed(double pctOfFullSpeed) {
     	final double baseSpeedPixPerSecond = GameConstants.INITIAL_SPEED_IN_PIXELS_PER_SECOND;
         final double maxSpeedPixPerSecond = GameConstants.FINAL_SPEED_IN_PIXELS_PER_SECOND;
         
     	if(pctOfFullSpeed >= 1){
     		speedPixPerSecond = maxSpeedPixPerSecond;
     	}
     	
     	speedPixPerSecond = baseSpeedPixPerSecond + (maxSpeedPixPerSecond - baseSpeedPixPerSecond) * pctOfFullSpeed;
     }
 	
 
 	/**
 	 * Advances the conveyor belt.  All the items on it will get carried along for the ride!
 	 * In addition to doing the standard motion of items, it also updates their MotionState.
 	 * @param currentTimeSec in milliseconds
 	 */
 	@Override
 	public void moveItems(double currentTimeSec) {
 		super.moveItems(currentTimeSec);
 		
         // Update all the current items
 		for(Recyclable recyclable : recyclables){
             Coordinate position = recyclable.getPosition();
 			if(position.getY()<GameConstants.SPRITE_BECOMES_UNTOUCHABLE){
                 recyclable.setMotionState(MotionState.CHUTE);
 
             }
 			else if(position.getY()<GameConstants.SPRITE_BECOMES_TOUCHABLE){
                 if(recyclable.getMotionState()==MotionState.CHUTE){
                 	recyclable.setMotionState(MotionState.CONVEYOR);
                 }
             }
 		}
 	}
 
 	public Path getPath() {
 		return path;
 	}
 }
