 package test.effect.unit;
 
 import static org.junit.Assert.*;
 
 import item.IdentityDisk;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import effect.Effect;
 import effect.ForceFieldEffect;
 import effect.IdentityDiskEffect;
 import effect.LightGrenadeEffect;
 import effect.PowerFailureEffect;
 import effect.PowerFailureLightGrenadeEffect;
 import effect.TeleporterEffect;
 import game.Game;
 import game.Player;
 import game.Race;
 import grid.Coordinate;
 import grid.Square;
 import grid.TestCaseThreeGridBuilder;
 
 public class ForceFieldEffectTest {	
 	
 	private Square sq1;
 	private Square sq2;
 	private Player player1;
 	private IdentityDisk idd;
 	
 	private Game game;
 	
 	private ForceFieldEffect ffe;
 	private LightGrenadeEffect lge;
 	private PowerFailureEffect pfe;
 	private IdentityDiskEffect ide;
 	private TeleporterEffect te;
 	
 	@Before
 	public void setUp() throws Exception {
 		game = new Game(new TestCaseThreeGridBuilder(),new Race());
 		
 		sq1 = game.getGrid().getSquareAtCoordinate(new Coordinate(5, 1));
 		sq2 = game.getGrid().getSquareAtCoordinate(new Coordinate(6, 1));
 		ffe = new ForceFieldEffect();
 		player1 = sq1.getPlayer();
 		sq2.addEffect(ffe);
 		idd = new IdentityDisk();
 		sq2.addItem(idd);
 		
 		lge = new LightGrenadeEffect();
 		pfe = new PowerFailureEffect();
 		ide = new IdentityDiskEffect();
 		te = new TeleporterEffect(null);
 	}
 	
 	/**
 	 * Test method for {@link effect.ForceFieldEffect#isPriorTo(Effect)}.
 	 */
 	@Test
	public void testGetIndex() {
		assertTrue(ffe.isPriorTo(null) == 3);
 	}
 	
 	/**
 	 * Test method for {@link effect.ForceFieldEffect#linkEffect(effect.Effect)}.
 	 */
 	@Test
 	public void testLinkEffect() {
 		//link light grenade effect
 		ffe.linkEffect(lge);
 		assertEquals(1,sq2.getEffects().length);
 		assertTrue(sq2.getEffects()[0] == ffe);
 		
 		//link power failure effect
 		ffe.linkEffect(pfe);
 		assertEquals(1,sq2.getEffects().length);
 		assertTrue(sq2.getEffects()[0] == ffe);
 		
 		//link identity disk effect
 		ffe.linkEffect(ide);
 		assertEquals(1,sq2.getEffects().length);
 		assertTrue(sq2.getEffects()[0] == ffe);	
 		
 		//link teleporter effect
 		ffe.linkEffect(te);
 		assertEquals(1,sq2.getEffects().length);
 		assertTrue(sq2.getEffects()[0] == ffe);	
 		
 		// link force field effect
 		ffe.linkEffect(ffe);
 		assertEquals(1, sq2.getEffects().length);
 		assertTrue(sq2.getEffects()[0] == ffe);
 	}
 	
 	/**
 	 * Test method for {@link effect.ForceFieldEffect#unlinkEffect(effect.Effect)}.
 	 */
 	@Test
 	public void testUnlinkEffect() {
 		//unlink teleporter effect
 		ffe.unlinkEffect(te);
 		assertEquals(1,sq2.getEffects().length);
 		assertTrue(sq2.getEffects()[0] == ffe);
 	
 		//unlink light grenade effect
 		ffe.unlinkEffect(lge);
 		assertEquals(1,sq2.getEffects().length);
 		assertTrue(sq2.getEffects()[0] == ffe);
 		
 		//unlink power failure effect
 		ffe.unlinkEffect(pfe);
 		assertEquals(1,sq2.getEffects().length);
 		assertTrue(sq2.getEffects()[0] == ffe);		
 		
 		//unlink identity disk effect
 		ffe.unlinkEffect(ide);
 		assertEquals(1,sq2.getEffects().length);
 		assertTrue(sq2.getEffects()[0] == ffe);	
 		
 		// unlink force field effect
 		ffe.unlinkEffect(ffe);
 		assertEquals(0, sq2.getEffects().length);
 	}
 	
 	/**
 	 * Test method for {@link effect.ForceFieldEffect#canCombineWith(effect.Effect)}.
 	 */
 	@Test
 	public void testCanCombineWith() {
 		assertFalse(ffe.canCombineWith(new PowerFailureEffect()));
 		assertFalse(ffe.canCombineWith(new LightGrenadeEffect()));
 		assertFalse(ffe.canCombineWith(new PowerFailureLightGrenadeEffect()));
 		assertFalse(ffe.canCombineWith(new IdentityDiskEffect()));
 		assertFalse(ffe.canCombineWith(new TeleporterEffect(null)));
		assertFalse(ffe.canCombineWith(new ForceFieldEffect()));
 	}
 	
 	/**
 	 * Test method for {@link effect.ForceFieldEffect#onStart(game.Actor)}.
 	 */
 	@Test
 	public void testOnStart() {
 		int actionsLeft = player1.getActionsLeft();
 		ffe.onStart(player1);
 		assertEquals(player1.getActionsLeft(), actionsLeft);
 		
 		ffe.onStart(idd);
 		assertTrue(idd.getLocation() == sq2);
 		
 		assertTrue(sq2.getEffects()[0] == ffe);
 	}
 	
 	/**
 	 * Test method for {@link effect.ForceFieldEffect#onStep(game.Actor)}.
 	 */
 	@Test
 	public void testOnStep() {
 		int actionsLeft = player1.getActionsLeft();
 		ffe.onStep(player1);
 		assertEquals(player1.getActionsLeft(), actionsLeft);
 		
 		ffe.onStep(idd);
 		assertFalse(idd.getLocation() == sq2);
 		
 		assertTrue(sq2.getEffects()[0] == ffe);
 	}
 	
 	/**
 	 * Test method for {@link effect.ForceFieldEffect#onLand(game.Actor)}.
 	 */
 	@Test
 	public void testOnLand() {
 		fail("not implemented");
 	}
 	
 	/**
 	 * Test method for {@link effect.ForceFieldEffect#onLeave(game.Actor)}.
 	 */
 	@Test
 	public void testOnLeave() {
 		fail("not implemented");
 	}
 
 }
