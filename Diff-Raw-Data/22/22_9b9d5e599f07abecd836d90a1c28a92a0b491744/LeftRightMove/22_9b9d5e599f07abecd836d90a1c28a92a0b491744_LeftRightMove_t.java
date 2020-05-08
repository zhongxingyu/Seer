 package fr.umlv.escape.move;
 
 import org.jbox2d.common.Vec2;
 import org.jbox2d.dynamics.Body;
 import fr.umlv.escape.Objects;
import fr.umlv.escape.front.FrontApplication;
 import fr.umlv.escape.world.EscapeWorld;
 
 /**
  * This class represent a cycling left move followed by a right move.
  * @implements {@link Movable}
  */
 public class LeftRightMove implements Movable{
 	@Override
 	public void move(Body body) {
 		Objects.requireNonNull(body);
 
 		if(body.getLinearVelocity().x==0){
 			Vec2 v2 =new Vec2(2.0f, 0.0f);
 			body.setLinearVelocity(v2);
 		}
		if(body.getPosition().x*EscapeWorld.SCALE>=FrontApplication.WIDTH-90){
 			Vec2 v2 =new Vec2(-2.0f, 0.0f);
 			body.setLinearVelocity(v2);
 		}
 		if(body.getPosition().x*EscapeWorld.SCALE<=90){
 			Vec2 v2 =new Vec2(2.0f, 0.0f);
 			body.setLinearVelocity(v2);
 		}
 	}
 	
 	@Override
 	public void move(Body body, Vec2 force) {
 		this.move(body);
 	}
 }
