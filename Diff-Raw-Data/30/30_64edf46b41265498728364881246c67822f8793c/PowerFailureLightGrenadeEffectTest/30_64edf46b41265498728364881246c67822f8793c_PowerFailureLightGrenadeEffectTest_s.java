 /**
  * 
  */
 package test.effect;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertTrue;
import junit.framework.Assert;
 import effect.Effect;
 import effect.LightGrenadeEffect;
 import effect.PowerFailureEffect;
 import effect.PowerFailureLightGrenadeEffect;
 import game.Game;
 import game.Player;
 import game.PlayerColour;
 import grid.Coordinate;
 import grid.Square;
 import grid.TestCaseOneGridBuilder;
 
 import org.junit.Before;
 import org.junit.Test;
 
 /**
  * @author Bart
  *
  */
 public class PowerFailureLightGrenadeEffectTest {
 	
 	/**
 	 * 
 	 */
 	Game game;
 	
 	/**
 	 * 
 	 */
 	Square sq1,sq2;
 	
 	/**
 	 * 
 	 */
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
 		pflge = new PowerFailureLightGrenadeEffect();
 		player1 = new Player(sq1,PlayerColour.RED);
 		sq1.setPlayer(player1);
 		sq1.addEffect(pflge);
 		lge = new LightGrenadeEffect();
 		pfe = new PowerFailureEffect();
 
 
 	}
 	
 	
 	
 
 
 	/**
 	 * Test method for {@link effect.PowerFailureLightGrenadeEffect#onStep(game.Actor)}.
 	 */
 	@Test
 	public void testOnStep() {
 		pflge.onStep(player1);
 		assertEquals(0,sq1.getEffects().length);
 		assertEquals(0,player1.getActionsLeft());
 		assertEquals(1,player1.getActionDamage());
 	}
 
 
 
 
 
 	/**
 	 *
 	 * @see effect.PowerFailureLightGrenadeEffect#linkEffect(Effect)
 	 */
 	@Test
 	public void testLinkEffect() {
 		//link power failure light grenade effect
 		PowerFailureLightGrenadeEffect pflge2 = new PowerFailureLightGrenadeEffect();
 		pflge.linkEffect(pflge2);
 		assertEquals(1,sq1.getEffects().length);
 		assertTrue(sq1.getEffects()[0] == pflge); 
 		
 	
 		//link light grenade effect
 		pflge.linkEffect(lge);
 		assertEquals(1,sq1.getEffects().length);
 		assertTrue(sq1.getEffects()[0] instanceof PowerFailureLightGrenadeEffect);
 		
 		//link power failure effect
 		pflge.linkEffect(pfe);
 		assertEquals(1,sq1.getEffects().length);
 		assertTrue(sq1.getEffects()[0] instanceof PowerFailureLightGrenadeEffect);		
 
 		
 	}
 	
 	/**
 	 *
 	 * @see effect.PowerFailureLightGrenadeEffect#unlinkEffect(Effect)
 	 */
 	@Test
 	public void testUnlinkEffectLGE() {
 		//unlink light grenade effect
 		pflge.unlinkEffect(lge);
 		assertEquals(1,sq1.getEffects().length);
 		assertTrue(sq1.getEffects()[0] instanceof PowerFailureEffect); 
 				
 	}
 
 
 	
 	/**
 	 *
 	 * @see effect.PowerFailureLightGrenadeEffect#unlinkEffect(Effect)
 	 */
 	@Test
 	public void testUnlinkEffectPFE() {
 		//unlink power failure effect
 		pflge.unlinkEffect(pfe);
 		assertEquals(1,sq1.getEffects().length);
 		assertTrue(sq1.getEffects()[0] instanceof LightGrenadeEffect); 
 	}
 				
 				
 
 	/**
 	 *
 	 * @see effect.PowerFailureLightGrenadeEffect#canCombineWith(Effect)
 	 */
 	@Test
 	public void canCombineWith() {
 		//check light grenade effect
 		assertFalse(pflge.canCombineWith(lge));
 				
 		//check power failure effect
 		assertFalse(pflge.canCombineWith(pfe));
 	}
 
 }
