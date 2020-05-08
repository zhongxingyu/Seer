 package tutorials.slickout.gameplay.level;
  
 import org.newdawn.slick.Animation;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.geom.Shape;
 import org.newdawn.slick.geom.Transform;
 import org.newdawn.slick.geom.Vector2f;
  
 public class CollidableAnimationObject extends AnimationObject implements
 		ICollidableObject {
  
	protected Shape collisionShape;
 	private int collisionType;
  
 	//private Vector2f deviation;
  
 	public CollidableAnimationObject(String name, Animation animation, Vector2f position, Shape collisionShape, int collisionType) {
 		super(name, animation, position);
  
 		this.collisionShape = collisionShape;
 		this.collisionType = collisionType;
 	}
  
 	@Override
 	public void setPosition(Vector2f position) {
 		super.setPosition(position);
 	}
  
  
 	@Override
 	public Shape getNormalCollisionShape() {
 		return collisionShape;
 	}
  
  
 	@Override
 	public Shape getCollisionShape() {
 		return collisionShape.transform( Transform.createTranslateTransform(position.x, position.y));
 	}
 	
 	public void setCollisionShape(Shape collisionShape){
 		this.collisionShape = collisionShape;
 	}
  
 	@Override
 	public int getCollisionType() {
 		return collisionType;
 	}
  
 	public Animation getAnimation() {
 		return animation;
 	}
  
 	@Override
 	public void render(Graphics graphics) {
 		super.render(graphics);
  
 		//graphics.draw(getCollisionShape());
 	}
  
 	@Override
 	public boolean isCollidingWith(ICollidableObject collidable){
 		return this.getCollisionShape().intersects(collidable.getCollisionShape());
 	}
  
 }
