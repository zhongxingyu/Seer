 package ultraextreme.model.entity;
 
 import javax.vecmath.Vector2d;
 
 import ultraextreme.model.util.Constants;
 import ultraextreme.model.util.Dimension;
 import ultraextreme.model.util.ObjectName;
 import ultraextreme.model.util.Position;
 import ultraextreme.model.util.Rotation;
 
 /**
  * An abstract class representing an in-game "physical" entity.
  * 
  * @author Bjorn Persson Mattsson, Viktor Anderling, Johan Gronvall
  * 
  */
 public abstract class AbstractEntity implements IEntity {
 
 	private Position position;
 
 	private Position prevPosition;
 
 	private Rotation rotation;
 
 	private int width;
 
 	private int height;
 
 	private ObjectName objectName;
 
 	/**
 	 * Creates a nameless entity at the position 0,0 and with the side 0
 	 */
 	public AbstractEntity() {
 		this(0, 0, 0, 0, new Rotation(0), null);
 	}
 
 	public AbstractEntity(double x, double y, int width, int height,
 			Rotation rotation, ObjectName objectName) {
 		this.rotation = rotation;
 		this.position = new Position(x, y);
 		this.prevPosition = new Position(x, y);
 		this.width = width;
 		this.height = height;
 		this.objectName = objectName;
 
 	}
 
 	/**
 	 * Move the entity a given x an y units.
 	 * 
 	 * @param x
 	 *            Number of x units the entity shall move with.
 	 * @param y
 	 *            Number of y units the entity shall move with.
 	 */
 	public void move(double x, double y) {
 		prevPosition.setPosition(position);
 		Vector2d rotatedVector = rotation.getRotatedCoordinates(x, y);
 		position.setX(position.getX() + rotatedVector.x);
 		position.setY(position.getY() + rotatedVector.y);
 	}
 
 	/**
 	 * Sets the position of this entity to the given position.
 	 * 
 	 * @param position
 	 *            The given position.
 	 */
 	public void setPosition(Position position) {
 		this.position = new Position(position);
 	}
 
 	/**
 	 * Returns this entity's position.
 	 * 
 	 * @return A new position with the same values as this position.
 	 */
 	@Override
 	public Position getPosition() {
 		return new Position(this.position);
 	}
 	
 	//TODO test this getCenteredPosition()
 	/**
 	 * Returns the position at the center of this entity.
 	 * 
 	 * @return A new position at the center of this entity.
 	 */
 	public Position getCenteredPosition() {
		return new Position(position.getX() + getWidth() / 2, position.getY() + getHeight() / 2);
 	}
 
 	/**
 	 * Returns true if and only if the entity is entirely outside of the screen
 	 * 
 	 * @return true if and only if the entity is entirely outside of the screen
 	 */
 	public boolean isOutOfScreen() {
 		Dimension screen = Constants.getInstance().getLevelDimension();
 		return position.getY() - height < 0 || position.getX() - width < 0
 				|| position.getX() > screen.getX()
 				|| position.getY() > screen.getY();
 	}
 
 	/**
 	 * Determines whether this entity collides with another entity.
 	 * 
 	 * @param other
 	 *            The other entity.
 	 * @return true if and only if the two entities are colliding.
 	 */
 	@Override
 	public boolean collidesWith(IEntity other) {
 		// Rectangle collision detection
 		double left1 = this.getPosition().getX();
 		double top1 = this.getPosition().getY();
 		double right1 = left1 + this.getWidth();
 		double bottom1 = top1 + this.getHeight();
 
 		double left2 = other.getPosition().getX();
 		double top2 = other.getPosition().getY();
 		double right2 = left2 + other.getWidth();
 		double bottom2 = top2 + other.getHeight();
 
 		return !(bottom1 < top2 || top1 > bottom2 || right1 < left2 || left1 > right2);
 	}
 
 	// public void getHitbox() {
 	// }
 
 	@Override
 	public int getWidth() {
 		return width;
 	}
 
 	@Override
 	public int getHeight() {
 		return height;
 	}
 
 	public Vector2d getNormalizedDirection() {
 		Vector2d v = new Vector2d(position.getX() - prevPosition.getX(),
 				position.getY() - prevPosition.getY());
 		v.normalize();
 		return v;
 	}
 
 	/**
 	 * Returns The direction this entity are facing.
 	 * 
 	 * @return The direction this entity are facing.
 	 */
 	public Rotation getRotation() {
 		return rotation;
 	}
 
 	/**
 	 * Returns what kind of entity this is as an ObjectName
 	 * 
 	 * @return what kind of entity this is as an ObjectName
 	 */
 	public ObjectName getObjectName() {
 		return objectName;
 	}
 
 	public abstract double getSpeedMod();
 }
