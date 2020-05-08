 package ultraextreme.model.enemy;
 
 import ultraextreme.model.entity.EnemyShip;
 import ultraextreme.model.item.AbstractWeapon;
 import ultraextreme.model.item.WeaponFactory;
 import ultraextreme.model.util.ObjectName;
 import ultraextreme.model.util.PlayerID;
 import ultraextreme.model.util.Position;
 import ultraextreme.model.util.Rotation;
 
 /**
  * Class representing an enemy which moves in a parabola-like pattern this is
  * simulated by making the enemy move in an elliptic pattern
  * 
  * @author Johan Gronvall
  */
 public class ParabolaEnemy extends AbstractEnemy {
 
 	private static final int SHIP_SIZE = 30;
 	private static final int SCORE = 40;
 
 	private Position startPoint;
 	private Position midPoint;
 	private Position endPoint;
	private float speed = 100f;
 
 	private ParabolaEnemy(EnemyShip ship, AbstractWeapon weapon) {
 		super(ship, weapon);
 	}
 
 	/**
 	 * Creates a new ParabolaEnemy OBS: endPosition should be placed outside
 	 * screen so that the enemy gets removed
 	 * 
 	 * @param startPoint
 	 *            where the ParabolaEnemy will appear
 	 * @param midPoint
 	 *            a point between the startPoint and the endPoint (in x
 	 *            coordinate), specifying the look of the movement curve
 	 * @param endPosition
 	 *            where the ParabolaEnemy will move towards
 	 * @param weaponName
 	 *            what kind of weapon this enemy will wield
 	 */
 	public ParabolaEnemy(Position startPoint, Position midPoint,
 			Position endPoint, ObjectName weaponName) {
		super(new EnemyShip(startPoint, SHIP_SIZE, SHIP_SIZE, 15,
 				ObjectName.PARABOLA_ENEMYSHIP), WeaponFactory
 				.getNewWeapon(weaponName));
 		if (!midPointIsInMiddle(startPoint.getX(), midPoint.getX(),
 				endPoint.getX())) {
 			throw new IllegalArgumentException(
 					"MidPoints x-coordinate must lie between startPoints"
 							+ " x-coordinate and endPoints x-coordinate");
 		}
 		this.startPoint = startPoint;
 		this.midPoint = midPoint;
 		this.endPoint = endPoint;
 
 		// negate speed if the curve goes from right to left
 		if (endPoint.getX() - startPoint.getX() < 0) {
 			speed *= -1;
 		}
 	}
 
 	/**
 	 * returns true if and only if midX lies between startX and endX
 	 * 
 	 * @return true if and only if midX lies between startX and endX
 	 */
 	private boolean midPointIsInMiddle(double startX, double midX, double endX) {
 		return (startX > midX && midX > endX) || (startX < midX && midX < endX);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public int getScoreValue() {
 		return SCORE;
 	}
 
 	/**
 	 * update called to make this enemy move in the parabola like pattern aswell
 	 * as make it fire at a set interval
 	 */
 	@Override
 	public void update(float timePassed) {
 		Position newPosition = this.getShip().getPositionClone();
 		double newX = newPosition.getX() + speed * timePassed;
 		newPosition.setX(newX);
 		newPosition.setY(calcY(newX));
 		this.getShip().setPosition(newPosition);
 		this.getWeapon().fire(this.getShip().getCenteredPositionClone(),
 				PlayerID.ENEMY, new Rotation(0), timePassed);
 	}
 
 	/**
 	 * Calculates a vertical value for a quadratic function using Lagrange's
 	 * from of the interpolation polynomial The formula takes a flipped Y axis
 	 * in consideration
 	 * 
 	 * @param x
 	 *            the horizontal coordinate
 	 * @return a new value for Y
 	 */
 	private double calcY(double x) {
 		double x0 = startPoint.getX();
 		double x1 = midPoint.getX();
 		double x2 = endPoint.getX();
 		double l0 = ((x - x1) * (x - x2)) / ((x0 - x1) * (x0 - x2));
 		double l1 = ((x - x0) * (x - x2)) / ((x1 - x0) * (x1 - x2));
 		double l2 = ((x - x0) * (x - x1)) / ((x2 - x0) * (x2 - x1));
 		return startPoint.getY() * l0 + midPoint.getY() * l1 + endPoint.getY()
 				* l2;
 	}
 }
