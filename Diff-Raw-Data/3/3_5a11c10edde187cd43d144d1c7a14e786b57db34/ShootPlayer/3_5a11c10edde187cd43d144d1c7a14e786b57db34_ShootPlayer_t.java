 package fr.umlv.escape.weapon;
 
 import java.util.ArrayList;
 
 import org.jbox2d.common.Vec2;
 import org.jbox2d.dynamics.Filter;
 
 import android.graphics.Point;
 
 import fr.umlv.escape.Objects;
 import fr.umlv.escape.gesture.Gesture;
 import fr.umlv.escape.gesture.GestureDetector;
 import fr.umlv.escape.ship.Ship;
 import fr.umlv.escape.world.EscapeWorld;
 
 /**
  * Class that represent the shoot behavior of the {@link Player}.
  * @implements {@link Shootable}.
  */
 public class ShootPlayer implements Shootable, Gesture{
 	private Vec2 force;
 	
 	private boolean canShoot(Weapon weapon){
 		Objects.requireNonNull(weapon);
 		
 		return weapon.getLoadingBullet() == null && weapon.canShoot();
 	}
 	@Override
 	public boolean shoot(Weapon weapon,int x,int y) {
 		Objects.requireNonNull(weapon);
 		if(canShoot(weapon)){
 			Point positionShip = new Point(x,y);
 			Bullet bullet=weapon.fire(positionShip);
 			/*Filter filter=new Filter();
 			filter.categoryBits=8;
 			filter.maskBits=4;
 			bullet.getBody().getFixtureList().setFilterData(filter);*/
 			bullet.getBody().setActive(true);
			bullet.getBody().setLinearDamping(3);
 			weapon.setLoadingBullet(bullet);
 			return true;
 		}
 		return false;
 	}
 	
 	@Override
 	public void fire(Weapon weapon) {
 		Objects.requireNonNull(weapon);
 		
 		Bullet b = weapon.getLoadingBullet();
 		if(b != null){
 			if(force == null){
 				force = new Vec2(0,-2);
 			}
			b.getBody().setLinearDamping(0);
 			b.fire(force);
 			weapon.setLoadingBullet(null);
 			force = null;
 		}
 	}
 
 	@Override
 	public boolean isRecognized(ArrayList<Point> pointList) {
 		Point firstPoint;
 		Point lastPoint;
 		firstPoint = pointList.get(0);
 		lastPoint = pointList.get(pointList.size()-1);
 		force=new Vec2((lastPoint.x-firstPoint.x)/EscapeWorld.SCALE,(lastPoint.y-firstPoint.y)/EscapeWorld.SCALE);
 		return true;
 	}
 
 	@Override
 	public void apply(Ship ship) {
 		fire(ship.getCurrentWeapon());
 	}
 }
