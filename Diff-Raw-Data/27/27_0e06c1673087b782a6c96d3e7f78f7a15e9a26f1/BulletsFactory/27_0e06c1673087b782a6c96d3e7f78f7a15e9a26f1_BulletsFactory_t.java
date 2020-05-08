 package fr.umlv.escape.weapon;
 
 import org.jbox2d.dynamics.Body;
 import org.jbox2d.dynamics.Filter;
 
 import android.graphics.Bitmap;
 import android.graphics.Point;
 
 import fr.umlv.escape.front.FrontApplication;
 import fr.umlv.escape.game.Game;
 import fr.umlv.escape.world.Bodys;
 
 /**
  * Bullet's factory
  */
 public class BulletsFactory {
 	private static BulletsFactory theBulletsFactory;
 
 	private BulletsFactory(){
 	}
 
 	public enum Bullets {
		BASICMISSILE,
 		FIREBALL,
 		SHIBOLEET,
 		XRAY
 	}
 
 	/**
 	 * @param bulletName kind of bullet to create
 	 * @param startPosition it's the position where the bullet appear
 	 * @param playerBullet if it's a player bullet
 	 * @return the new bullet
 	 */
 	public Bullet createBullet(String bulletName, Point startPosition, Boolean playerBullet) {
 
 		Body body;
 		Bullet bullet;
 		Bullets currentBullet = Bullets.valueOf(bulletName.toUpperCase());
 		Bitmap img = FrontApplication.frontImage.getImage(bulletName);
 
 		switch(currentBullet){
		case BASICMISSILE : 
 			body = Bodys.createBasicCircle(startPosition.x, startPosition.y, img.getWidth(), 0);
 			bullet= new BasicMissile(bulletName, body, playerBullet, img);
 			break;
 		case FIREBALL : 
 			body = Bodys.createBasicCircle(startPosition.x, startPosition.y, img.getWidth(), 0);
 			bullet= new FireBall(bulletName, body, playerBullet, img);
 			break;
 		case SHIBOLEET : 
 			body = Bodys.createBasicCircle(startPosition.x, startPosition.y, img.getWidth(), 0);
 			bullet= new Shiboleet(bulletName, body, playerBullet, img);
 			break;
 		case XRAY : 
 			body = Bodys.createBasicRectangle(-100, FrontApplication.HEIGHT/2, img.getWidth(),  img.getHeight(), 0);
 			bullet= new XRay(bulletName, body, playerBullet, img);
 			body.getFixtureList().setSensor(true);
 			break;
 		default : throw new IllegalArgumentException(bulletName+" isn't a legal bullet");
 		}
 		body.setActive(false);
 		Filter filter=new Filter();
 		filter.categoryBits=32;
 		filter.maskBits=2;
 		body.getFixtureList().setFilterData(filter);
 
 		Game.getTheGame().getFrontApplication().getBattleField().addBullet(bullet);
 		return bullet;
 	}
 
 	/**
 	 * Instantiate or return the Bullet's factory
 	 * @return singleton bullet's factory
 	 */
 	public static BulletsFactory getTheBulletsFactory(){
 		if(BulletsFactory.theBulletsFactory==null){
 			BulletsFactory.theBulletsFactory = new BulletsFactory();
 		}
 		return BulletsFactory.theBulletsFactory;
 	}
 }
