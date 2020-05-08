 package cratig.beamrider.ship;
 
 import org.andengine.util.adt.pool.GenericPool;
 
 public class ShipBulletPool extends GenericPool<ShipBullet>{
 
 	public static ShipBulletPool instance;
 	
 	public static ShipBulletPool sharedShipBulletPool() {
 		if (instance == null) {
 			instance = new ShipBulletPool();
 		}
 		
 		return instance;
 	}
 	
 	private ShipBulletPool() {
 		super();
 	}
 	
 	@Override
 	protected ShipBullet onAllocatePoolItem() {
 		return new ShipBullet();
 	}
 	
	protected void onHandleRecycleItem(final ShipBullet bullet) {
 		bullet.sprite.clearEntityModifiers();
 		bullet.sprite.clearUpdateHandlers();
 		bullet.sprite.setVisible(false);
 		bullet.sprite.detachSelf();
 	}
 
 }
