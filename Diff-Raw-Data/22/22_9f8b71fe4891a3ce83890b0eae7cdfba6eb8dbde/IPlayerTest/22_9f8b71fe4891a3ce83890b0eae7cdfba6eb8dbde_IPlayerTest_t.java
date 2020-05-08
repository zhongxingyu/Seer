 package ultraextreme.model;
 
 import junit.framework.TestCase;
 import ultraextreme.model.entity.AbstractEntity;
 import ultraextreme.model.util.PlayerID;
 import ultraextreme.model.util.Position;
 
 /**
  * 
  * @author Daniel Jonsson
  * 
  */
 public class IPlayerTest extends TestCase {
 
 	private IPlayer player;
 	private BulletManager bulletManager;
 	private PlayerID playerId;
 
 	@Override
 	protected void setUp() throws Exception {
 		super.setUp();
 		this.resetInstanceVariables();
 	}
 
 	private void resetInstanceVariables() {
 		bulletManager = new BulletManager();
 		playerId = PlayerID.PLAYER1;
 		player = new Player(playerId, bulletManager);
 	}
 
 	public void testGetPlayerShip() {
 		AbstractEntity playerShip = player.getShip();
 		assertTrue(playerShip == player.getShip());
 	}
 
 	public void testGetPlayerId() {
 		assertEquals(player.getPlayerId(), PlayerID.PLAYER1);
 	}
 
 	/**
 	 * Test the update method with a lot of different values.
 	 */
 	public void testUpdate() {
		// FIXME: Doesn't test the drop bomb feature yet.
 		updateTester(0, 0, false, false);
 		updateTester(5, 10, false, false);
 		updateTester(-5, -10, false, false);
 		updateTester(0, 0, true, true);
 		updateTester(5, 10, true, true);
 		updateTester(-5, -10, true, true);
 		updateTester(100, -100, true, true);
 	}
 
 	/**
 	 * Helper method that runs an update on the player and checks wether his
 	 * ship has moved and if his weapons have been fired.
 	 * 
 	 * @param dX
 	 *            Delta X distance that he has moved.
 	 * @param dY
 	 *            Delta Y distance that he has moved.
 	 * @param fireWeapons
 	 *            If he has fired his guns.
 	 * @param dropBomb
 	 *            If he has dropped a bomb.
 	 */
 	private void updateTester(int dX, int dY, boolean fireWeapons,
 			boolean dropBomb) {
 		this.resetInstanceVariables();
 		Position pOld = new Position(player.getShip().getPosition());
 		ModelInput m = new ModelInput(dX, dY, fireWeapons, dropBomb);
 		player.update(m, 1);
 		Position pNew = player.getShip().getPosition();
 		assertEquals(pOld.getX() + dX, pNew.getX());
 		assertEquals(pOld.getY() + dY, pNew.getY());
		if (fireWeapons)
			assertTrue(bulletManager.getNewBullets().size() > 0);
		else
			assertTrue(bulletManager.getNewBullets().size() == 0);
		//assertEquals(bulletManager.isBombDropped(), dropBomb);
 	}
 }
