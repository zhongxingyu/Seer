 package niktgar.tod.entity;
 
 import java.util.List;
 
 import lombok.Data;
 import lombok.experimental.Accessors;
 import niktgar.tod.collision.BoundingBox;
 import niktgar.tod.collision.Collidable;
 import niktgar.tod.geometry.Vector;
 import niktgar.tod.sprite.Animation;
 import niktgar.tod.sprite.Sprite;
 
 @Accessors(fluent = true)
 @Data
 public class Entity implements Collidable {
 
     public AnimationState animationState;
     public MovementState movementState;
    
     protected Animation animation;
     protected Vector position;
     protected Vector velocity;
 
     public Entity(Sprite sprite) {
         this.animation = new Animation(sprite);
         initialize();
     }
 
     public Entity(List<Sprite> sprites) {
         this.animation = new Animation(sprites);
         initialize();
     }
 
     public Entity(Animation animation) {
         this.animation = animation;
         initialize();
     }
 
     public void initialize() {
         velocity = new Vector(0, 0);
         this.animationState = AnimationState.IDLE;
         this.movementState = MovementState.DEFAULT;
     }
 
     public void draw() {
         animation.draw(position.snappedX(), position.snappedY());
     }
 
     public void draw(int x, int y) {
         animation.draw(position.snappedX() + x, position.snappedY() + y);
     }
 
     public void update(long delta) {
         position.x += velocity.x / delta;
         position.y += velocity.y / delta;
     }
 
     public void applyForce(final long delta, final Vector vector) {
         velocity.x += vector.x / delta;
         velocity.y += vector.y / delta;
     }
 
     public void doLogic() {
 
     }
 
     @Override
     public BoundingBox bound() {
         return new BoundingBox(position.snappedX(), position.snappedY(), position.snappedX() + animation.width(), position.snappedY() + animation.height());
     }
 
     public void collidedTop(Collidable collidable) {
         position.y = collidable.bound().lrY();
         velocity.y = 0;
     }
 
     public void collidedBottom(Collidable collidable) {
         position.y = collidable.bound().ulY() - animation.height();
     }
 
     public void collidedLeft(Collidable collidable) {
         position.x = collidable.bound().lrX();
     }
 
     public void collidedRight(Collidable collidable) {
         position.x = collidable.bound().ulX() - animation.width();
     }
 
     public void alertFloating() {
 
     }
 
     @Override
     public void collidedWith(Collidable collidable) {
         // TODO Auto-generated method stub
     }
 }
