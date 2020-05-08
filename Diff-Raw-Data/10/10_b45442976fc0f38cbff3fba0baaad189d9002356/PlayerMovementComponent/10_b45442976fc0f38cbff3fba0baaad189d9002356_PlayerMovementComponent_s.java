 package components;
 
 import game.Controller;
 
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.geom.Vector2f;
 import org.newdawn.slick.state.StateBasedGame;
 
 public class PlayerMovementComponent extends Component {
 
 	private float direction;
 	private float speed;
 
 	public PlayerMovementComponent(String id) {
 		this.id = id;
 	}
 
 	public float getSpeed() {
 		return speed;
 	}
 
 	public float getDirection() {
 		return direction;
 	}
 
 	@Override
 	public void update(GameContainer gc, StateBasedGame sb, int delta) {
 		float rotation = owner.getRotation();
 		float scale = owner.getScale();
 		Vector2f position = owner.getPosition();
 
 		Input input = gc.getInput();
 
 		if (Controller.isP1ButtonPressed("Left", input)
 				|| Controller.isP1ButtonPressed("LeftAlt", input)) {
			rotation += -0.2f * delta;
 		}
 
 		if (Controller.isP1ButtonPressed("Right", input)
 				|| Controller.isP1ButtonPressed("RightAlt", input)) {
 			rotation += 0.2f * delta;
 		}
 
		if (Controller.isP1ButtonPressed("Up", input)
				|| Controller.isP1ButtonPressed("UpAlt", input))
 
 		{
 			float hip = 0.4f * delta;
 
 			position.x += hip
 					* java.lang.Math.sin(java.lang.Math.toRadians(rotation));
 			position.y -= hip
 					* java.lang.Math.cos(java.lang.Math.toRadians(rotation));
 		}
 
		if (Controller.isP1ButtonPressed("Down", input)
				|| Controller.isP1ButtonPressed("DownAlt", input))
 
 		{
 			float hip = -0.4f * delta;
 
 			position.x += hip
 					* java.lang.Math.sin(java.lang.Math.toRadians(rotation));
 			position.y -= hip
 					* java.lang.Math.cos(java.lang.Math.toRadians(rotation));
 		}
 
 		owner.setPosition(position);
 		owner.setRotation(rotation);
 		owner.setScale(scale);
 
 	}
 
 }
