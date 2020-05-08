 package game.components.graphics;
 
 import game.components.ComponentMessage;
 import game.components.ComponentType;
 import game.components.interfaces.IRenderComponent;
 import game.components.physics.Movement;
 import game.entities.IEntity;
 import math.Rectangle;
import math.Vector2;
 import math.time.GameTime;
 
 import org.newdawn.slick.Graphics;
 
 public class OutlineNext implements IRenderComponent {
   private IEntity owner;
   private Movement movement;
  private Vector2 next;
 
   public OutlineNext() {
    next = new Vector2();
   }
 
   @Override
   public ComponentType getComponentType() {
     return ComponentType.GRAPHIC;
   }
 
   @Override
   public void reciveMessage(final ComponentMessage message, final Object args) {
     // Do nothing
   }
 
   @Override
   public void render(final Graphics g) {
     final Rectangle body = owner.getBody();
 
    g.drawRect(next.x, next.y, body.getWidth(), body.getHeight());
   }
 
   @Override
   public void setOwner(final IEntity owner) {
     this.owner = owner;
     this.movement = (Movement) owner.getComponent(ComponentType.MOVEMENT);
   }
 
   @Override
   public void update(final GameTime time) {
    next = owner.getBody().getMin().add(
      movement.getVelocity().multiply(time.getFrameLength()));
   }
 }
