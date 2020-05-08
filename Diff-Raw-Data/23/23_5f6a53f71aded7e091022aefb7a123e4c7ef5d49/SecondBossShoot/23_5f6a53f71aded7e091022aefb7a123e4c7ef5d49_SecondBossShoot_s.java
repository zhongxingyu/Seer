 package fr.umlv.escape.weapon;
 
 import org.jbox2d.common.Vec2;
 
 import android.graphics.Point;
 
 import fr.umlv.escape.Objects;
 import fr.umlv.escape.ship.SecondBoss;
 import fr.umlv.escape.ship.Ship;
 
 /**
  * Class that represent the shoot behavior of the second boss
  * @implements {@link Shootable}.
  */
 public class SecondBossShoot implements Shootable{
 	private long lastShoot=0;
 	private SecondBoss shipBoss;
 	private long lastChangeState;
 	
 	/**
 	 * Constructor
 	 * @param shipBoss The {@link Ship} of the boss.
 	 */
 	public SecondBossShoot(SecondBoss shipBoss) {
 		Objects.requireNonNull(shipBoss);
 		
 		this.shipBoss=shipBoss;
 		this.lastChangeState=0;
 	}
 	
 	/**
 	 * Manage how the boss have to shoot depending to his state.
 	 */
 	@Override
 	public boolean shoot(Weapon weapon,int x,int y) {
 		Objects.requireNonNull(weapon);
 		
 		long currentTime=System.currentTimeMillis();
 		if(currentTime-lastChangeState>500){
 			 if(!shipBoss.isInvisible()){
 				 shipBoss.incLvlInvisible();
 				 lastChangeState=currentTime;
 			 }
 			 else{
 				 if((shipBoss.getLvlInvisible()==7) && (currentTime-lastChangeState>7000)){
 					 shipBoss.decLvlInvisible();
 					 lastChangeState=currentTime;
 				 }
 				 else if(shipBoss.getLvlInvisible()<7){
 					 shipBoss.decLvlInvisible();
 					 lastChangeState=currentTime;
 				 }
 			 }
 		}
 		if(shipBoss.getPosXCenter()<0){
 			weapon.setGhostShoot(1);
 		}
 		if(currentTime-lastShoot>500){
 			int ghostShoot=weapon.getGhostShoot();
 			if(ghostShoot>0){
 				weapon.setGhostShoot(ghostShoot-1);
 				lastShoot=currentTime;	
 				return false;
 			}
 		}
 		if(weapon.getLoadingBullet()!=null){
 			weapon.getLoadingBullet().loadPower();
 			if(weapon.getLoadingBullet().getCurrentLoad() == weapon.getLoadingBullet().getMaxLoad()){
 				return true;
 			}
 			return false;
 		}
 		if(currentTime-lastShoot>2000){
 			Point positionShip = new Point(x,y);
 			weapon.setLoadingBullet(weapon.fire(positionShip));
 			lastShoot=currentTime;
 			return false;
 		}
 		return true;
 	}
 	
 	@Override
 	public void fire(Weapon weapon) {
 		Objects.requireNonNull(weapon);
 		
 		if(weapon == null) throw new IllegalArgumentException("Weapon can't be Null");
 		Bullet b = weapon.getLoadingBullet();
		if(b == null) throw new IllegalStateException("Bullet can't be Null");
 		b.fire(new Vec2(0f,2f));
 		weapon.setLoadingBullet(null);
 	}
 }
