 package ultraextreme.model.entity;
 
 import ultraextreme.model.util.Constants;
 import ultraextreme.model.util.PlayerID;
 import ultraextreme.model.util.Rotation;
 
 /**
  * 
  * @author Daniel Jonsson
  * 
  */
 public abstract class AbstractBulletTest extends AbstractEntityTest {
 
 	@Override
 	protected AbstractEntity getNewAbstractEntity(double x, double y,
 			int width, int height, Rotation direction) {
 		return getNewAbstractBullet(x, y, width, height, PlayerID.PLAYER1,
 				direction);
 	}
 
 	protected abstract AbstractBullet getNewAbstractBullet(double x, double y,
 			int width, int height, PlayerID playerId, Rotation direction);
 
 	public abstract void testDoMovement();
 
 	public void testGetPlayerId() {
 		AbstractBullet bullet = newBullet(PlayerID.PLAYER1);
 		assertEquals(bullet.getPlayerId(), PlayerID.PLAYER1);
 
 		bullet = newBullet(PlayerID.PLAYER1);
 		assertEquals(bullet.getPlayerId(), PlayerID.PLAYER1);
 
 		bullet = newBullet(PlayerID.ENEMY);
 		assertEquals(bullet.getPlayerId(), PlayerID.ENEMY);
 	}
 
 	@Override
 	public void testGetSpeedMod() {
 		AbstractBullet bullet = newBullet();
		assertEquals(bullet.getSpeedMod(), Constants.getBulletSpeedModifier());
 	}
 
 	private AbstractBullet newBullet() {
 		return newBullet(PlayerID.PLAYER1);
 	}
 
 	private AbstractBullet newBullet(PlayerID playerId) {
 		return getNewAbstractBullet(10, 20, 30, 40, playerId, new Rotation(0));
 	}
 }
