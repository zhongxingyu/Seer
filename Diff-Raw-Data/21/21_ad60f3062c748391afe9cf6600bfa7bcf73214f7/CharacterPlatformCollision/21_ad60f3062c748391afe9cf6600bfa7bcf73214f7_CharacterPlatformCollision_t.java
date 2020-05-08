 package collision;
 
import physiceEngine.NewtonianSprite;
 import setting.Platform;
 
 /**
  * @author Ian McMahon
  */
 
 import com.golden.gamedev.object.Sprite;
 import com.golden.gamedev.object.collision.CollisionGroup;
 
 public class CharacterPlatformCollision extends CollisionGroup{
 	//Character groups should deal with what happens to the character after collision. 
 	//May have to be separated with player/enemy distinctions.
 	//Platforms will react depending on their type of decorating.
 	
 	@Override
 	public void collided(Sprite arg0, Sprite arg1) {
 		collided(arg0, ((Platform) arg1));
 	}
 	
 	public void collidid(NewtonianSprite character, Platform platform){
 		switch(collisionSide){
 		case(BOTTOM_TOP_COLLISION):
 			platform.hitFromBottomAction();
 		case(TOP_BOTTOM_COLLISION):
 			platform.hitFromTopAction();
 		case(LEFT_RIGHT_COLLISION):
 			platform.hitFromLeftAction();
 		case(RIGHT_LEFT_COLLISION):
 			platform.hitFromRightAction();
 		}
 	}
 
 }
