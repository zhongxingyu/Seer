 package fr.umlv.escape.weapon;
 
 import org.jbox2d.dynamics.Body;
 import org.jbox2d.dynamics.Filter;
 
 import android.graphics.Point;
 
 import fr.umlv.escape.front.BitmapFactory;
 import fr.umlv.escape.game.Game;
 import fr.umlv.escape.world.Bodys;
 
 /**
  * Bullet's factory
  */
 public class BulletsFactory {
 	private static BulletsFactory theBulletsFactory;
 
 	private BulletsFactory(){
 	}
 
 	/**
 	 * @param bulletName kind of bullet to create
 	 * @param startPosition it's the position where the bullet appear
 	 * @param playerBullet if it's a player bullet
 	 * @return the new bullet
 	 */
 	public Bullet createBullet(String bulletName, Point startPosition, Boolean playerBullet) {
 
 		Image img = FrontImages.getImage(bulletName);
 		if( img == null){
 			img=BitmapFactory.getTheImagesFactory().createBulletImage(bulletName);
 			FrontImages.addImages(bulletName, img);
 		}
 		
 		Body body;
 		Bullet bullet;
 		switch(bulletName){
 		case "BasicMissile" : 
			body = Bodys.createBasicCircle((float)startPosition.getX(),(float)startPosition.getY(), img.getWidth(null), 0);
 			bullet= new BasicMissile(bulletName, body, playerBullet);
 			break;
 		case "FireBall" : 
			body = Bodys.createBasicCircle((float)startPosition.getX(),(float)startPosition.getY(), img.getWidth(null), 0);
 			bullet= new FireBall(bulletName, body, playerBullet);
 			break;
 		case "Shiboleet" : 
			body = Bodys.createBasicCircle((float)startPosition.getX(),(float)startPosition.getY(), img.getWidth(null), 0);
 			bullet= new Shiboleet(bulletName, body, playerBullet);
 			break;
 		case "XRay" : 
 			body = Bodys.createBasicRectangle(-100, Game.getTheGame().getHeight()/2, img.getWidth(null),  img.getHeight(null), 0);
 			bullet= new XRay(bulletName, body, playerBullet);
 			body.getFixtureList().setSensor(true);
 			break;
 		default : throw new IllegalArgumentException(bulletName+" isn't a legal bullet");
 		}
 		body.setActive(false);
 		Filter filter=new Filter();
 		filter.categoryBits=32;
 		filter.maskBits=2;
 		body.getFixtureList().setFilterData(filter);
 
 		DisplayableMonitor.addBullet(bullet);
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
