 /**
  * 
  */
 package test.effect;
 
import static org.junit.Assert.*;
import item.Teleporter;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

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
 
 /**
  * @author Maxim
  *
  */
 public class IdentityDiskEffectTest {
 	
 	/**
 	 * 
 	 */
 	Game game;
 	
 	/**
 	 * 
 	 */
 	Square sq1,sq2;
 	
 	IdentityDiskEffect ide;
 
 	
 	PowerFailureLightGrenadeEffect pflge;
 	LightGrenadeEffect lge;
 	PowerFailureEffect pfe;
 	
 	Player player1;
 
 
 
 	/**
 	 * @throws java.lang.Exception
 	 */
 	@Before
 	public void setUp() throws Exception {
 		game = new Game(TestCaseOneGridBuilder.class);
 		sq1 = game.getGrid().getSquareAtCoordinate(new Coordinate(2, 1));
 		sq2 = game.getGrid().getSquareAtCoordinate(new Coordinate(3, 1));
 		ide = new IdentityDiskEffect();
 		player1 = new Player(sq1,PlayerColour.RED);
 		sq1.setPlayer(player1);
 		sq1.addEffect(ide);
 		
 		lge = new LightGrenadeEffect();
 		pfe = new PowerFailureEffect();
 		pflge = new PowerFailureLightGrenadeEffect();
 	}
 
 	/**
 	 * Test method for {@link effect.IdentityDiskEffect#linkEffect(effect.Effect)}.
 	 */
 	@Test
 	public void testLinkEffect() {		
 		
 		//link power failure light grenade effect
 		ide.linkEffect(pflge);
 		assertEquals(1,sq1.getEffects().length);
 		assertTrue(sq1.getEffects()[0] == ide);
 		
 	
 		//link light grenade effect
 		ide.linkEffect(lge);
 		assertEquals(1,sq1.getEffects().length);
 		assertTrue(sq1.getEffects()[0] == ide);
 		
 		//link power failure effect
 		ide.linkEffect(pfe);
 		assertEquals(1,sq1.getEffects().length);
 		assertTrue(sq1.getEffects()[0] == ide);
 		
 		//link identity disk effect
 		ide.linkEffect(ide);
 		assertEquals(1,sq1.getEffects().length);
 		assertTrue(sq1.getEffects()[0] == ide);	
 	}
 
 	/**
 	 * Test method for {@link effect.IdentityDiskEffect#unlinkEffect(effect.Effect)}.
 	 */
 	@Test
 	public void testUnlinkEffect() {
 		//unlink power failure light grenade effect
 		ide.unlinkEffect(pflge);
 		assertEquals(1,sq1.getEffects().length);
 		assertTrue(sq1.getEffects()[0] == ide);
 		
 	
 		//unlink light grenade effect
 		ide.unlinkEffect(lge);
 		assertEquals(1,sq1.getEffects().length);
 		assertTrue(sq1.getEffects()[0] == ide);
 		
 		//unlink power failure effect
 		ide.unlinkEffect(pfe);
 		assertEquals(1,sq1.getEffects().length);
 		assertTrue(sq1.getEffects()[0] == ide);		
 		
 		//unlink identity disk effect
 		ide.unlinkEffect(ide);
 		assertEquals(0,sq1.getEffects().length);
 	}
 
 	/**
 	 * Test method for {@link effect.IdentityDiskEffect#canCombineWith(effect.Effect)}.
 	 */
 	@Test
 	public void testCanCombineWith() {
 		//check light grenade effect
 		assertFalse(ide.canCombineWith(lge));
 						
 		//check power failure effect
 		assertFalse(ide.canCombineWith(pfe));
 		
 		//check power failure light grenade effect
 		assertFalse(ide.canCombineWith(pflge));
 		
 		//check identity disk effect
 		assertFalse(ide.canCombineWith(ide));
 	}
 
 	/**
 	 * Test method for {@link effect.IdentityDiskEffect#onStep(game.Actor)}.
 	 */
 	@Test
 	public void testOnStep() {
 		ide.onStep(player1);
 		assertTrue(sq1.getEffects()[0] == ide);
 	}
 
 	/**
 	 * Test method for {@link effect.IdentityDiskEffect#onLeave(game.Actor)}.
 	 */
 	@Test
 	public void testOnLeave() {
 		ide.onLeave(player1);
 		assertTrue(sq1.getEffects()[0] == ide);
 	}
 
 	/**
 	 * Test method for {@link effect.IdentityDiskEffect#onStart(game.Actor)}.
 	 */
 	@Test
 	public void testOnStart() {
 		ide.onStart(player1);
 		assertEquals(0,sq1.getEffects().length);
 		assertEquals(0,player1.getActionsLeft());
 	}
 
 	/**
 	 * Test method for {@link effect.IdentityDiskEffect#onLand(game.Actor)}.
 	 */
 	@Test
 	public void testOnLand() {
 		ide.onLand(player1);
 		assertTrue(sq1.getEffects()[0] == ide);
 	}
 
 }
