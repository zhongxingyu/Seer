 package se.chalmers.dryleafsoftware.androidrally.libgdx.actions;
 
 import java.util.List;
 
 import se.chalmers.dryleafsoftware.androidrally.libgdx.AnimatedImage;
 import se.chalmers.dryleafsoftware.androidrally.libgdx.gameboard.MapBuilder;
 import se.chalmers.dryleafsoftware.androidrally.libgdx.gameboard.RobotView;
 
 /**
  * This action changes its robot's life and damage values.
  * 
  * @author
  * 
  */
 public class HealthAction extends GameAction {
 
 	private final int damage, lives;
 
 	public static final int DECREASE_ONE = -2;
 	public static final int INCREASE_ONE = -3;
 	private final AnimationAction hitAnim;
	private int newDamage = -1;
	private int newLive = -1;
 
 	/**
 	 * Creates a new instance which will handle the robot with the specified ID.
 	 * 
 	 * @param robotID
 	 *            The ID of the robot to handle.
 	 * @param damage
 	 *            The damage to set to the robot. Supports UNCHANGED.
 	 * @param lives
 	 *            The number of lives to set to the robot. Supports UNCHANGED.
 	 * @param animation
 	 *            The animation to display when the robot is hit by a laser.
 	 */
 	public HealthAction(int robotID, int damage, int lives,
 			AnimatedImage animation) {
 		super(robotID, 0);
 		this.damage = damage;
 		this.lives = lives;
 		this.hitAnim = new AnimationAction(robotID, damage, animation);
 	}
 
	private void changeHealth(RobotView robot) {
 		if (damage != UNCHANGED) {
 			if (damage == INCREASE_ONE) {
 				newDamage = robot.getDamage() + 1;
 			} else if (damage == DECREASE_ONE) {
 				newDamage = robot.getDamage() - 1;
 			} else {
 				newDamage = damage;
 			}
 		}
 		if (lives != UNCHANGED) {
 			if (lives == INCREASE_ONE) {
 				newLive = robot.getLives() + 1;
 			} else if (lives == DECREASE_ONE) {
 				newLive = robot.getLives() - 1;
 			} else {
 				newLive = lives;
 			}
 		}
	}

	@Override
	public void action(List<RobotView> robots, MapBuilder map) {
		start();
		RobotView robot = robots.get(getRobotID());
		int oldDamage = robot.getDamage();
		changeHealth(robot);
 		if (oldDamage < newDamage) {
 			hitAnim.setDuration(getDuration());
 			hitAnim.action(robots, map);
 		}
 	}
 
 	@Override
 	public void cleanUp(List<RobotView> robots, MapBuilder map) {
 		RobotView robot = robots.get(getRobotID());
		if (newDamage == -1 && newLive == -1) {
			changeHealth(robot);
		}
 		robot.setDamage(newDamage);
 		robot.setLives(newLive);
 		if (robots.get(getRobotID()).getLives() == 0) {
 			robots.get(getRobotID()).setDead(true);
 		}
 		hitAnim.cleanUp(robots, map);
 	}
 }
