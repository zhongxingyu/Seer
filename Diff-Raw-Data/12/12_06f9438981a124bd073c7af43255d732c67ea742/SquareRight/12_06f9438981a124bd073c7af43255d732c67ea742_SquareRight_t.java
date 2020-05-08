 package fr.umlv.escape.move;
 
 import org.jbox2d.common.Vec2;
 import org.jbox2d.dynamics.Body;
 
 import fr.umlv.escape.Objects;
 import fr.umlv.escape.front.FrontApplication;
 import fr.umlv.escape.game.Game;
 import fr.umlv.escape.world.EscapeWorld;
 
 /**
  * This class represent a square move. The object will be follow
  * a square path in the direction of the hand of a clock. This move never stop itself
  * @implements {@link Movable}
  */
 public class SquareRight implements Movable{
 	private int nbCall=0;
 	
 	@Override
 	public void move(Body body) {
 		Objects.requireNonNull(body);
 		
 		Vec2 v2 =new Vec2();
 		switch(nbCall){
 		case 0:
 			if(body.getPosition().y*EscapeWorld.SCALE>(FrontApplication.WIDTH/3-120)){
 				return;
 			}
 			v2.set(2f,0f);
 			break;
 		case 1:
 			if(body.getPosition().x*EscapeWorld.SCALE<(FrontApplication.WIDTH/3)){
 				return;
 			}
 			v2.set(0f,2f);
 			break;
 		case 2:
 			if(body.getPosition().y*EscapeWorld.SCALE<(FrontApplication.WIDTH/3)){
 				return;
 			}
 			v2.set(-2f,0f);
 			break;
 		case 3:
 			if(body.getPosition().x*EscapeWorld.SCALE>(FrontApplication.WIDTH/3-120)){
 				return;
 			}
 			v2.set(0f,-2f);
 			break;
 		default:
 			throw new AssertionError();
 		}
 		body.setLinearVelocity(v2);
 		nbCall=(nbCall+1)%4;
 	}
 	
 	@Override
 	public void move(Body body, Vec2 force) {
 		this.move(body);
 	}
 }
