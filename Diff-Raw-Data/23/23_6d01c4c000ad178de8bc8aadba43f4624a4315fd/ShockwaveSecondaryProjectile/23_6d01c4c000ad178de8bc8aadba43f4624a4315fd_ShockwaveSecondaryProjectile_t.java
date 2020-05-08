 package chalmers.TDA367.B17.weapons;
 
 import org.newdawn.slick.geom.Vector2f;
 
 import chalmers.TDA367.B17.controller.GameController;
import chalmers.TDA367.B17.model.AbstractObstacle;
 import chalmers.TDA367.B17.model.AbstractProjectile;
 import chalmers.TDA367.B17.model.AbstractTank;
 import chalmers.TDA367.B17.model.Entity;
 import chalmers.TDA367.B17.model.MapBounds;
 
 public class ShockwaveSecondaryProjectile extends AbstractProjectile {
 
 	//Used to check the amount of times this shockwave has hit a target. 
 	ShockwaveProjectile shockproj;
 
 	/**
 	 * Create a new ShockwaveSecondaryProjectile.
 	 * @param id The id
 	 * @param tank The tank it belongs to
 	 * @param position The position
 	 * @param sp The ShockwaveProjectile that is the origin
 	 */
 	public ShockwaveSecondaryProjectile(int id, AbstractTank tank, Vector2f position, ShockwaveProjectile sp) {
 		super(id, tank, position, new Vector2f(1,1), 100, 0, 10, 1200);	
 		setSpeed(0.05f);
 		setSize(new Vector2f(2,2));
 		spriteID = "proj_energy";
 		shockproj = sp;
 		GameController.getInstance().getWorld().addEntity(this);
 	}
 
 	@Override
 	public void update(int delta){
 		if(getDurationTimer() <= 0)
 			shockproj = null;
 		super.update(delta);
 	}
 
 	@Override
 	public void didCollideWith(Entity entity){
 		if(entity instanceof AbstractProjectile || entity == getTank()){
 			return;
		}else if(entity instanceof MapBounds || entity instanceof AbstractObstacle){
 			this.destroy();
 		}else if(entity instanceof AbstractTank){
 			/*
 			 * Adds the amount of times the tank has been damaged.
 			 * One tank can only be damaged up to five times by a single shockwave.
 			 */
 			int tmp = shockproj.tankDamaged(getTank());
 			if(tmp <= 5){
 				damageTarget((AbstractTank)entity);
 			}
 		}
 	}
 }
