 package entities;
 
 import org.lwjgl.util.vector.Vector3f;
 import org.newdawn.slick.GameContainer;
 
 import util.Assets;
 import base.World;
 
 public class AIFish extends Fish {
 
 	private Vector3f nextPoint;
 
 	public AIFish(World world) {
 		super(Assets.FISH_STILL2, 2.1f, world.getRandomClearPosition(),
 				new Vector3f(), 8f, 0.01f, world);
 
 		selectNextPoint();
 	}
 
 	private void selectNextPoint() {
 		nextPoint = world.getRandomClearPosition();
 	}
 
 	@Override
 	protected Vector3f getAccelerationDirection(GameContainer gc) {
 
 		Vector3f aim = Vector3f.sub(nextPoint, position, null);
 
 		if (aim.length() < 200) {
 			selectNextPoint();
 		}
 
 		return aim;
 
 	}
 
 	@Override
 	protected void doSomething() {
 		// TODO Auto-generated method stub
 
 	}
 
 }
