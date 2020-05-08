 package se.chalmers.dryleafsoftware.androidrally.libgdx.actions;
 
 import java.util.List;
 
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.scenes.scene2d.actions.Actions;
 
 import se.chalmers.dryleafsoftware.androidrally.libgdx.gameboard.MapBuilder;
 import se.chalmers.dryleafsoftware.androidrally.libgdx.gameboard.RobotView;
 
 /**
  * A SingleAction only moves one robot.
  * 
  * @author 
  *
  */
 public class SingleAction extends GameAction {
 
 	private final int dir, posX, posY;
 	
 	/**
 	 * Creates a new instance which will work on the robot with the specified ID.
 	 * @param robotID The ID of the robot to work on.
 	 * @param dir The direction to rotate the direction to.
 	 * @param posX The X-coordinate to move to. Note: Array position.
 	 * @param posY The Y-coordinate to move to. Note: Array position.
 	 */
 	public SingleAction(int robotID, int dir, int posX, int posY) {
 		super(robotID, 1000);
 		this.dir = dir;
 		this.posX = posX;
 		this.posY = posY;
 	}
 
 	@Override
 	public void action(List<RobotView> robots, MapBuilder map) {
 		start();
 		Vector2 pos = map.convertToMapY(posX, posY);
 		robots.get(getRobotID()).addAction(Actions.parallel(
 				Actions.moveTo(pos.x, pos.y, getDuration() / 1000f),
 				Actions.rotateBy(getValidDir((int) robots.get(getRobotID()).getRotation(), -dir * 90),
 						getDuration() / 1000f)));
 		
 	}
 	
 	@Override
 	public void cleanUp(List<RobotView> robots, MapBuilder map) {
		robots.get(getRobotID()).clearActions();
 		Vector2 pos = map.convertToMapY(posX, posY);
 		robots.get(getRobotID()).setPosition(pos.x, pos.y);
 		robots.get(getRobotID()).setRotation(-dir * 90);
 	}
 	
 	/**
 	 * Calculates the shortest rotation required to rotate to the target direction.
 	 * @param current The current direction.
 	 * @param target The desired direction.
 	 * @return The shortest direction to rotate.
 	 */
 	private static int getValidDir(int current, int target) {
 		int result = target - current;
 		return Math.abs(result) > 180 ? (result % 360) - (int)Math.signum(result) * 360 : result;
 	}
 }
