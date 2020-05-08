 package fr.umlv.escape.weapon;
 
 import org.jbox2d.dynamics.Filter;
 
 import android.graphics.Point;
 
 import fr.umlv.escape.Objects;
 import fr.umlv.escape.gesture.Gesture;
 import fr.umlv.escape.gesture.GestureDetector;
 
 /**
  * Class that represent the shoot behavior of the {@link Player}.
  * @implements {@link Shootable}.
  */
 public class ShootPlayer implements Shootable{
 	private final GestureDetector gestureDetector;
 	
 	/**
 	 * Constructor
 	 * @param gesture {@link Gesture} used by the {@link Player}.
 	 */
 	public ShootPlayer(GestureDetector gestureDetector) {
 		Objects.requireNonNull(gestureDetector);
 		
 		this.gestureDetector=gestureDetector;
 	}
 	
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
 			Filter filter=new Filter();
 			filter.categoryBits=8;
 			filter.maskBits=4;
 			bullet.getBody().getFixtureList().setFilterData(filter);
 			bullet.getBody().setActive(true);
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
			b.fire(gestureDetector.getLastForce());
 			weapon.setLoadingBullet(null);
 		}
 	}
 }
