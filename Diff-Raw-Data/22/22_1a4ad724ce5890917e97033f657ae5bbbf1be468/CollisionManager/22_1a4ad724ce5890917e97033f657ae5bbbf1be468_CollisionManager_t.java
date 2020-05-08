 import java.util.ArrayList;
 
 
 public class CollisionManager
 {	
 	public static void checkAndHandleCollisions(Bird bird, ArrayList<Entity> entities)
 	{
 		for (Entity entity : entities)
 		{
 			if(entity.colidable)
 			{
				if(entity.boundingShape.intersects(bird.getCrap().getBoundingShape()) && bird.getCrap().isActive)
 				{
 					entity.handleCollision(entity);
 					bird.getCrap().handleCollision(entity);
 				}
 			}
 		}
 	}
 }
