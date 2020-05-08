 package se.chalmers.dryleafsoftware.androidrally.libgdx.actions;
 
 import java.util.List;
 
 import se.chalmers.dryleafsoftware.androidrally.libgdx.gameboard.RobotView;
 
 /**
  * This Action simple stops any other action to perform for the specified duration.
  * 
  * @author
  *
  */
 public class HolderAction extends GameAction {
 
 	/**
 	 * Creates a new instance with the specified duration.
	 * @param duration The duration of the action.
 	 */
 	public HolderAction(int duration) {
 		super(-1, duration);
 	}
 
 	@Override
 	public void action(List<RobotView> robots) {
 		start();
 	}
 
 }
