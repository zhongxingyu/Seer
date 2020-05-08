 package se.chalmers.tda367.group15.game.controllers;
 
 import java.awt.geom.Rectangle2D;
 import java.awt.geom.Rectangle2D.Float;
 import java.util.List;
 import java.util.Map;
 
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.util.pathfinding.AStarPathFinder;
 import org.newdawn.slick.util.pathfinding.Path;
 import org.newdawn.slick.util.pathfinding.TileBasedMap;
 
 import se.chalmers.tda367.group15.game.models.AbstractCharacterModel;
 import se.chalmers.tda367.group15.game.models.AbstractMeleeWeaponModel;
 import se.chalmers.tda367.group15.game.models.AbstractMovingModel;
 import se.chalmers.tda367.group15.game.models.AbstractNpcModel;
 import se.chalmers.tda367.group15.game.models.AbstractProjectileModel;
 import se.chalmers.tda367.group15.game.models.MeleeSwingModel;
 import se.chalmers.tda367.group15.game.views.CharacterView;
 
 /**
  * AbstractNpcController implements methods for pathfinding and movement of npc
  * characters.
  * 
  * @author Carl Jansson
  */
 public abstract class AbstractNpcController extends
 		AbstractMovingModelController {
 
 	private long swingTimer = 0;
 	private final int ENEMY_DAMAGE_MODIFIER = 2;
 
 	/**
 	 * The path controller is traveling
 	 */
 	private Path myPath;
 
 	/**
 	 * Current step of the path
 	 */
 	private int currentStep;
 
 	/**
 	 * the path finder
 	 */
 	private AStarPathFinder myPathFinder;
 
 	/**
 	 * waiting time start for creating new path
 	 */
 	private long pauseTime;
 
 	/**
 	 * Time to wait before creating new path
 	 */
 	private long waitTime;
 
 	/**
 	 * with of x to walk in
 	 */
 	private int deltaX;
 
 	/**
 	 * height of y to walk in
 	 */
 	private int deltaY;
 
 	/**
 	 * X coordinate of area to walk in
 	 */
 	private int startX;
 
 	/**
 	 * Y coordinate of area to walk in
 	 */
 	private int startY;
 
 	/**
 	 * Create a new npc controller.
 	 * 
 	 * @param gameController
 	 *            The GameController to be used.
 	 * @param model
 	 *            The model that this controller should control.
 	 * @param map
 	 *            The tile based map to be used.
 	 */
 	public AbstractNpcController(GameController gameController,
 			AbstractNpcModel model, TileBasedMap map) {
 		super(gameController);
 		this.setpathFinder(new AStarPathFinder(map, 500, true));
 		this.setModel(model);
 		this.setView(new CharacterView(model));
 		this.setDefaultTiles(model.getMinTileX(), model.getMaxTileX(),
 				model.getMinTileY(), model.getMaxTileY());
 		this.pauseTime = 0;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public abstract void render(GameContainer container, Graphics g)
 			throws SlickException;
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void update(GameContainer container, int delta,
 			List<Float> staticBounds,
 			Map<AbstractMovingModel, Float> dynamicBounds)
 			throws SlickException {
 	}
 
 	/**
 	 * Set path finder
 	 * 
 	 * @param pathFinder
 	 *            the pathfinder to use
 	 */
 	public void setpathFinder(AStarPathFinder pathFinder) {
 		this.myPathFinder = pathFinder;
 	}
 
 	/**
 	 * get current pathfinder
 	 * 
 	 * @return the active pathfinder
 	 */
 	public AStarPathFinder getPathFinder() {
 		return myPathFinder;
 	}
 
 	/**
 	 * Makes npc move along path if path exists. Sets npc rotation to next path
 	 * node.
 	 * 
 	 * @param model
 	 *            the model that moves
 	 * @param delta
 	 *            time since last update
 	 * @param dynamicBounds
 	 *            other moving models
 	 */
 	public void moveAlongPath(AbstractMovingModel model, int delta,
 			Map<AbstractMovingModel, Float> dynamicBounds) {
 		if (myPath.getLength() >= currentStep) {
 			float diffX = model.getX() - (myPath.getX(currentStep) * 32);
 			float diffY = model.getY() - (myPath.getY(currentStep) * 32);
 
 			double dir = Math.atan2(diffY, diffX);
 			model.setRotation(Math.toDegrees(dir));
 
 			float speedY = (float) (model.getVelocity() * Math.sin(dir));
 			float speedX = (float) (model.getVelocity() * Math.cos(dir));
 
 			float tmpNewX = model.getX() - (delta * speedX);
 			float tmpNewY = model.getY() - (delta * speedY);
 
 			// if dynamic collision set path to null so a new random path will
 			// be created
 			if (isDynamicCollision(tmpNewX, tmpNewY, dynamicBounds)) {
 				myPath = null;
 				// Set the new positions
 			} else {
 				if (!isDynamicCollision(tmpNewX, model.getY(), dynamicBounds)) {
 					model.setX(tmpNewX);
 				}
 
 				if (!isDynamicCollision(model.getX(), tmpNewY, dynamicBounds)) {
 					model.setY(tmpNewY);
 				}
 
 				int currX = (int) (model.getX() + (model.getWidth() / 2)) / 32;
 				int currY = (int) (model.getY() + (model.getHeight() / 2)) / 32;
 
 				if (currX == myPath.getX(currentStep)
 						&& currY == myPath.getY(currentStep)) {
 					currentStep++;
 				}
 			}
 		}
 	}
 
 	/**
 	 * Make a new random path. Starting with current position. Goal is defined
 	 * by setDefaultTiles.
 	 */
 	public void calculateRandomPath() {
 		int tarX = startX + (int) (Math.random() * deltaX);
 		int tarY = startY + (int) (Math.random() * deltaY);
 
 		calculateNewPath(tarX, tarY);
 	}
 
 	/**
 	 * Calculates a new path from current position to (x, y)
 	 * 
 	 * @param tarX
 	 *            ending X position in tiles
 	 * @param tarY
 	 *            ending Y position in tiles
 	 */
 	public void calculateNewPath(int tarX, int tarY) {
		myPath = getPathFinder().findPath(null, (int) getModel().getX() / 32,
				(int) getModel().getY() / 32, tarX, tarY);
 		currentStep = 1;
 	}
 
 	/**
 	 * If time == 0 starts a new random timer. If time has pased sets time to 0
 	 * and returns true
 	 * 
 	 * @return true if random time has passed
 	 */
 	public boolean pauseTimer() {
 		if (pauseTime == 0) {
 			pauseTime = System.currentTimeMillis();
 			waitTime = (long) (2000 * Math.random());
 		} else if (System.currentTimeMillis() >= waitTime + pauseTime) {
 			pauseTime = 0;
 			return true;
 		}
 		return false;
 	}
 
 	/**
 	 * check if there is a path or if end of it reached.
 	 * 
	 * @return true if path is null or current step is end of path
 	 */
 	public boolean existsPath() {
		if (myPath == null || currentStep >= myPath.getLength()){
		return true;
 		}
		return false;
 	}
 
 	/**
 	 * Set max and min positions. If given coordinates not allowed sets entire
 	 * map as default.
 	 * 
 	 * (x1, y1) and (x2, y2) is upper left corner and lower right corner of tile
 	 * box for model to move in. Model do not have to start in this box and when
 	 * they track hero they might move outside this box. But default movement
 	 * position will always be inside. If x1, x2, y1, y2 not used entire map
 	 * will be set as default.
 	 * 
 	 * @param x
 	 *            minimum x tile random movement will occur on
 	 * @param x2
 	 *            maximum x tile random movement will occur on
 	 * @param y
 	 *            minimum y tile random movement will occur on
 	 * @param y2
 	 *            maximum y tile random movement will occur on
 	 */
 	public void setDefaultTiles(int x, int x2, int y, int y2) {
 
 		if (x >= 0 && y >= 0 && x < x2 && y < y2 && x2 <= 32 && y2 <= 24) {
 			deltaX = x2 - x;
 			deltaY = y2 - y;
 			startX = x;
 			startY = y;
 
 		} else {
 			deltaX = 32;
 			deltaY = 24;
 			startX = 0;
 			startY = 0;
 		}
 	}
 
 	/**
 	 * Time the fire method so animation can run its course
 	 */
 	public void fireTimed() {
 		if (System.currentTimeMillis() - swingTimer > ((AbstractCharacterModel) getModel())
 				.getCurrentWeapon().getFiringSpeed()) {
 			swingTimer = System.currentTimeMillis();
 			fire();
 		}
 	}
 
 	/**
 	 * Method to tell npc to fire a weapon of some kind
 	 */
 	public abstract void fire();
 
 	/**
 	 * Method for checking if two pixel points are in sight of each other.
 	 * 
 	 * @param staticBounds
 	 *            The obstruction that can get in the way
 	 * @param point1X
 	 *            first points x position
 	 * @param point1Y
 	 *            first points y position
 	 * @param point2X
 	 *            second points x position
 	 * @param point2Y
 	 *            second points y position
 	 * @return true if they are in sight of each other
 	 */
 	public boolean isInSight(List<Rectangle2D.Float> staticBounds,
 			float point1X, float point1Y, float point2X, float point2Y) {
 
 		for (Rectangle2D.Float rect : staticBounds) {
 
 			if (recIntersectLine(rect, point1X, point1Y, point2X, point2Y)) {
 				return false;
 			}
 		}
 		return true;
 	}
 
 	/**
 	 * Check if line defined by two points is intersected by a rectangle rect.
 	 * 
 	 * @param rect
 	 *            the Rectangle that maybe intersects
 	 * @param point1X
 	 *            x variable of first point
 	 * @param point1Y
 	 *            y variable of first point
 	 * @param point2X
 	 *            x variable of second point
 	 * @param point2Y
 	 *            y variable of second point
 	 * @return true if line is intersected by rect
 	 */
 	public boolean recIntersectLine(Rectangle2D.Float rect, float point1X,
 			float point1Y, float point2X, float point2Y) {
 		return (linesIntersect(point1X, point1Y, point2X, point2Y, rect.getX(),
 				rect.getY(), rect.getX() + rect.getWidth(),
 				rect.getY() + rect.getHeight()) || linesIntersect(point1X,
 				point1Y, point2X, point2Y, rect.getX(),
 				rect.getY() + rect.getHeight(), rect.getX() + rect.getWidth(),
 				rect.getY()));
 	}
 
 	/**
 	 * This method is taken from java-gaming.org where it was posted by user JGO
 	 * Knight. For some reason Java default intersect method as declared in the
 	 * interface Shape doesn't work.
 	 * 
 	 * JGO Knights own description:
 	 * 
 	 * The fastest way to test if 2 line segments intersect. Tests if the line
 	 * segment from (x1, y1) to (x2, y2) intersects the line segment from (x3,
 	 * y3) to (x4, y4). My tests showed that this method was about 25% faster
 	 * than java.awt.geom.Line2D.linesIntersect(x1, y1, x2, y2, x3, y3, x4, y4):
 	 * 
 	 * @param x1
 	 * @param y1
 	 * @param x2
 	 * @param y2
 	 * @param x3
 	 * @param y3
 	 * @param x4
 	 * @param y4
 	 * @return true if intersect
 	 * @author JGO Knight
 	 */
 	public static boolean linesIntersect(double x1, double y1, double x2,
 			double y2, double x3, double y3, double x4, double y4) {
 		// Return false if either of the lines have zero length
 		if (x1 == x2 && y1 == y2 || x3 == x4 && y3 == y4) {
 			return false;
 		}
 		// Fastest method, based on Franklin Antonio's
 		// "Faster Line Segment Intersection" topic "in Graphics Gems III" book
 		// (http://www.graphicsgems.org/)
 		double ax = x2 - x1;
 		double ay = y2 - y1;
 		double bx = x3 - x4;
 		double by = y3 - y4;
 		double cx = x1 - x3;
 		double cy = y1 - y3;
 
 		double alphaNumerator = by * cx - bx * cy;
 		double commonDenominator = ay * bx - ax * by;
 		if (commonDenominator > 0) {
 			if (alphaNumerator < 0 || alphaNumerator > commonDenominator) {
 				return false;
 			}
 		} else if (commonDenominator < 0) {
 			if (alphaNumerator > 0 || alphaNumerator < commonDenominator) {
 				return false;
 			}
 		}
 		double betaNumerator = ax * cy - ay * cx;
 		if (commonDenominator > 0) {
 			if (betaNumerator < 0 || betaNumerator > commonDenominator) {
 				return false;
 			}
 		} else if (commonDenominator < 0) {
 			if (betaNumerator > 0 || betaNumerator < commonDenominator) {
 				return false;
 			}
 		}
 		if (commonDenominator == 0) {
 			// This code wasn't in Franklin Antonio's method. It was added by
 			// Keith Woodward.
 			// The lines are parallel.
 			// Check if they're collinear.
 			double y3LessY1 = y3 - y1;
 			double collinearityTestForP3 = x1 * (y2 - y3) + x2 * (y3LessY1)
 					+ x3 * (y1 - y2); // see
 										// http://mathworld.wolfram.com/Collinear.html
 			// If p3 is collinear with p1 and p2 then p4 will also be collinear,
 			// since p1-p2 is parallel with p3-p4
 			if (collinearityTestForP3 == 0) {
 				// The lines are collinear. Now check if they overlap.
 				if (x1 >= x3 && x1 <= x4 || x1 <= x3 && x1 >= x4 || x2 >= x3
 						&& x2 <= x4 || x2 <= x3 && x2 >= x4 || x3 >= x1
 						&& x3 <= x2 || x3 <= x1 && x3 >= x2) {
 					if (y1 >= y3 && y1 <= y4 || y1 <= y3 && y1 >= y4
 							|| y2 >= y3 && y2 <= y4 || y2 <= y3 && y2 >= y4
 							|| y3 >= y1 && y3 <= y2 || y3 <= y1 && y3 >= y2) {
 						return true;
 					}
 				}
 			}
 			return false;
 		}
 		return true;
 	}
 
 	/**
 	 * Swing the the currently equipped weapon of the enemy
 	 */
 	protected void swingWeapon() {
 		AbstractCharacterModel model = (AbstractCharacterModel) getModel();
 
 		// Run the swinging animation for the weapon
 		AbstractMeleeWeaponModel weapon = (AbstractMeleeWeaponModel) model
 				.getCurrentWeapon();
 		CharacterView view = (CharacterView) getView();
 		view.runAttackAnimation();
 
 		AbstractProjectileModel newSwing = new MeleeSwingModel();
 
 		float modelAngle = (float) Math.toRadians(model.getRotation());
 		float modelMiddleX = model.getX() + model.getWidth() / 2;
 		float heroMiddleY = model.getY() + model.getHeight() / 2;
 
 		float modelFaceX = modelMiddleX - (float) Math.cos(modelAngle)
 				* ((model.getWidth()));
 		float modelFaceY = heroMiddleY - (float) Math.sin(modelAngle)
 				* ((model.getHeight()));
 
 		newSwing.setX(modelFaceX - newSwing.getWidth() / 2);
 		newSwing.setY(modelFaceY - newSwing.getHeight() / 2);
 		newSwing.setRotation(model.getRotation());
 		newSwing.setDamage(model.getCurrentWeapon().getDamage()
 				* ENEMY_DAMAGE_MODIFIER);
 		newSwing.setAlive(true);
 
 		RoomController currentRoom = getGameController().getRoomsController()
 				.getCurrentRoom();
 
 		currentRoom.addSwing(newSwing);
 	}
 }
