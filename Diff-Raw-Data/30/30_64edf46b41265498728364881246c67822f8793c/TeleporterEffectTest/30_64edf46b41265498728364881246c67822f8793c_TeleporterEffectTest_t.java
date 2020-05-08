 /**
  * 
  */
 package test.effect;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertSame;
 import static org.junit.Assert.assertTrue;
 import effect.IdentityDiskEffect;
 import effect.LightGrenadeEffect;
 import effect.PowerFailureEffect;
 import effect.PowerFailureLightGrenadeEffect;
 import effect.TeleporterEffect;
 import game.Game;
 import game.Player;
 import game.PlayerColour;
 import grid.Coordinate;
 import grid.Square;
 import grid.TestCaseOneGridBuilder;
 import item.Teleporter;
 
 import org.junit.Before;
 import org.junit.Test;
 
 /**
  * @author Maxim
  *
  */
 public class TeleporterEffectTest {
 	
 	/**
 	 * 
 	 */
 	Game game;
 	
 	/**
 	 * 
 	 */
 	Square sq1,sq2;
 	
 	Teleporter t1,t2;
 	
 	/**
 	 * 
 	 */
 	TeleporterEffect te1,te2;
 	
 	PowerFailureLightGrenadeEffect pflge;
 	LightGrenadeEffect lge;
 	PowerFailureEffect pfe;
 	IdentityDiskEffect ide;
 	
 	Player player1;
 
 
 	/**
 	 * @throws java.lang.Exception
 	 */
 	@Before
 	public void setUp() throws Exception {
 		game = new Game(TestCaseOneGridBuilder.class);
 		sq1 = game.getGrid().getSquareAtCoordinate(new Coordinate(2, 1));
 		sq2 = game.getGrid().getSquareAtCoordinate(new Coordinate(3, 1));
 		t1 = new Teleporter(null);
 		t2 = new Teleporter(null);
 		
 		sq1.addItem(t1);
 		sq2.addItem(t2);
 		
 		t1.setDestination(t2);
 		t2.setDestination(t1);
 		
 		
 		
 		
 		te1 = (TeleporterEffect) sq1.getEffects()[0];
 		te2 = (TeleporterEffect) sq2.getEffects()[0];
 		
 		player1 = new Player(sq1,PlayerColour.RED);
 		sq1.setPlayer(player1);
 		
 		pflge = new PowerFailureLightGrenadeEffect();
 		lge = new LightGrenadeEffect();
 		pfe = new PowerFailureEffect();
 		ide = new IdentityDiskEffect();
 	}
 
 	/**
 	 * Test method for {@link effect.TeleporterEffect#linkEffect(effect.Effect)}.
 	 */
 	@Test
 	public void testLinkEffect() {
 		//link teleporter effect
 		te1.linkEffect(te2);
 		assertEquals(1,sq1.getEffects().length);
 		assertTrue(sq1.getEffects()[0] instanceof TeleporterEffect);		
 		
 		//link power failure light grenade effect
 		te1.linkEffect(pflge);
 		assertEquals(1,sq1.getEffects().length);
 		assertTrue(sq1.getEffects()[0] instanceof TeleporterEffect);
 		
 	
 		//link light grenade effect
 		te1.linkEffect(lge);
 		assertEquals(1,sq1.getEffects().length);
 		assertTrue(sq1.getEffects()[0] instanceof TeleporterEffect);
 		
 		//link power failure effect
 		te1.linkEffect(pfe);
 		assertEquals(1,sq1.getEffects().length);
 		assertTrue(sq1.getEffects()[0] instanceof TeleporterEffect);
 		
 		//link identity disk effect
 		te1.linkEffect(ide);
 		assertEquals(1,sq1.getEffects().length);
 		assertTrue(sq1.getEffects()[0] instanceof TeleporterEffect);		
 		
 	}
 
 	/**
 	 * Test method for {@link effect.TeleporterEffect#unlinkEffect(effect.Effect)}.
 	 */
 	@Test
 	public void testUnlinkEffect() {
 		//unlink power failure light grenade effect
 		te1.unlinkEffect(pflge);
 		assertEquals(1,sq1.getEffects().length);
 		assertTrue(sq1.getEffects()[0] instanceof TeleporterEffect);
 		
 	
 		//unlink light grenade effect
 		te1.unlinkEffect(lge);
 		assertEquals(1,sq1.getEffects().length);
 		assertTrue(sq1.getEffects()[0] instanceof TeleporterEffect);
 		
 		//unlink power failure effect
 		te1.unlinkEffect(pfe);
 		assertEquals(1,sq1.getEffects().length);
 		assertTrue(sq1.getEffects()[0] instanceof TeleporterEffect);		
 		
 		//unlink identity disk effect
 		te1.unlinkEffect(ide);
 		assertEquals(1,sq1.getEffects().length);
 		assertTrue(sq1.getEffects()[0] instanceof TeleporterEffect);	
 		
 		//unlink teleporter effect
 		te1.unlinkEffect(te1);
 		assertEquals(0,sq1.getEffects().length);
 		
 		
 	}
 
 	/**
 	 * Test method for {@link effect.TeleporterEffect#canCombineWith(effect.Effect)}.
 	 */
 	@Test
 	public void testCanCombineWith() {
 		//check light grenade effect
 		assertFalse(te1.canCombineWith(lge));
 						
 		//check power failure effect
 		assertFalse(te1.canCombineWith(pfe));
 		
 		//check power failure light grenade effect
 		assertFalse(te1.canCombineWith(pflge));
 		
 		//check teleporter effect
 		assertFalse(te1.canCombineWith(te2));
 		
 		//check identity disk effect
 		assertFalse(te1.canCombineWith(ide));
 	}
 
 	/**
 	 * Test method for {@link effect.TeleporterEffect#getDestination()}.
 	 */
 	@Test
 	public void testGetDestination() {
 		assertSame(t2,te1.getDestination());
 	}
 
 	/**
 	 * Test method for {@link effect.TeleporterEffect#onStart(game.Actor)}.
 	 */
 	@Test
 	public void testOnStart() {
 		te1.onStart(player1);
 		assertTrue(sq1.getEffects()[0] == te1);
 	}
 
 	/**
 	 * Test method for {@link effect.TeleporterEffect#onStep(game.Actor)}.
 	 */
 	@Test
 	public void testOnStep() {
 		te1.onStep(player1);
 		assertTrue(sq1.getEffects()[0] == te1);
 		
 		assertSame(player1,sq2.getPlayer());
 	}
 
 	/**
 	 * Test method for {@link effect.TeleporterEffect#onLand(game.Actor)}.
 	 */
 	@Test
 	public void testOnLand() {
 		te1.onLand(player1);
 		assertTrue(sq1.getEffects()[0] == te1);
 	}
 
 	/**
 	 * Test method for {@link effect.TeleporterEffect#onLeave(game.Actor)}.
 	 */
 	@Test
 	public void testOnLeave() {
 		te1.onLeave(player1);
 		assertTrue(sq1.getEffects()[0] == te1);
 	}
 
 }
