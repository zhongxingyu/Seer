 package chalmers.TDA367.B17.weapons;
 
 import org.newdawn.slick.geom.*;
 
 import chalmers.TDA367.B17.controller.GameController;
import chalmers.TDA367.B17.model.AbstractObstacle;
 import chalmers.TDA367.B17.model.AbstractProjectile;
 import chalmers.TDA367.B17.model.AbstractTank;
 import chalmers.TDA367.B17.model.Entity;
 import chalmers.TDA367.B17.model.MapBounds;
 
 public class BounceProjectile extends AbstractProjectile {
 	
 	/**
 	 * Create a new BounceProjectile.
 	 * @param id The id
 	 * @param tank The tank it belongs to
 	 * @param position The position
 	 */
 	public BounceProjectile(int id, AbstractTank tank, Vector2f position) {
 		super(id, tank, position, new Vector2f(1,1), 100, -100, 5, 3000);
 		setSpeed(0.25f);
 		setSize(new Vector2f(5f, 10f));
 		spriteID = "proj_fire";
 		GameController.getInstance().getWorld().addEntity(this);
 	}
 	
 	@Override
 	public void didCollideWith(Entity entity){
 		if(entity instanceof AbstractProjectile || entity == getTank()){
 			return;
 		} else if (entity instanceof MapBounds){
 			MapBounds tmpMapBounds = (MapBounds)entity;
 			if(Math.abs(getPosition().x - tmpMapBounds.getShape().getX()) < 5 || 
 					Math.abs(getPosition().x - tmpMapBounds.getShape().getWidth()) < 5){
 			setDirection(new Vector2f(-getDirection().x, getDirection().y));
 			}else if(true){
 			setDirection(new Vector2f(getDirection().x, -getDirection().y));
 			}
		}else if(entity instanceof AbstractObstacle){
			AbstractObstacle tmpAbstractObstacle = (AbstractObstacle)entity;
			if(Math.abs(getPosition().x - tmpAbstractObstacle.getShape().getX()) < 5 || 
					Math.abs(getPosition().x - tmpAbstractObstacle.getShape().getWidth()) < 5){
 			setDirection(new Vector2f(-getDirection().x, getDirection().y));
 			}else if(true){
 			setDirection(new Vector2f(getDirection().x, -getDirection().y));
 			}
 		}else if(entity instanceof AbstractTank){
 			damageTarget((AbstractTank)entity);
 		}
 	}
 }
