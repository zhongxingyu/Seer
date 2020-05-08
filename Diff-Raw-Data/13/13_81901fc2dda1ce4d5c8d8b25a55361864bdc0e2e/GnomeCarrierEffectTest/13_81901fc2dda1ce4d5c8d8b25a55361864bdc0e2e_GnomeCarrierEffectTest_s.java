 package gt.general.aura;
 
 import static org.junit.Assert.*;
 
 import gt.general.Character;
 import gt.general.CharacterAttributes;
 import gt.general.Hero;
 import gt.plugin.helloworld.HelloWorld;
 
 import org.bukkit.entity.Player;
 import org.getspout.spoutapi.SpoutManager;
 import org.getspout.spoutapi.player.SpoutPlayer;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.powermock.api.mockito.PowerMockito;
 import org.powermock.core.classloader.annotations.PrepareForTest;
 import org.powermock.modules.junit4.PowerMockRunner;
 
 import static org.mockito.Mockito.*;
 
 @RunWith(PowerMockRunner.class)
 @PrepareForTest(SpoutManager.class)
 public class GnomeCarrierEffectTest {
	private GnomeCarrierEffect effect;
 	private Hero hero;
 	private Player playerMock;
 	private double delta = 1e-20;
 
 	@Before
 	public void setup() {
 		playerMock = mock(Player.class);
 		hero = new Hero(playerMock);
		effect = new GnomeCarrierEffect();
 		
 		SpoutPlayer sp = mock(SpoutPlayer.class);
 		PowerMockito.mockStatic(SpoutManager.class);
 		when(SpoutManager.getPlayer(any(Player.class))).thenReturn(sp);
 		
 	}
 	
 	@Test
 	public void initialisationTest() {
 		assertTrue(hero.getEffects().isEmpty());
 		assertTrue(hero.getAuras().isEmpty());
 		
 		hero.simulateEffects();
 		assertEquals(Hero.DEFAULT_HERO_SPEED, hero.getCurrentSpeed(), delta);
 	}
 	
 	@Test
 	public void effectTest() {
 		assertEquals(Hero.DEFAULT_HERO_SPEED, hero.getCurrentSpeed(), delta);
 		
 		double currentSpeed = hero.getCurrentSpeed();
 		while (hero.getCurrentSpeed() > 0) {
			hero.addEffect(effect);
 			hero.simulateEffects();
 
			currentSpeed -= Hero.DEFAULT_HERO_SPEED*GnomeCarrierEffect.SLOWRATE;
 			assertEquals(currentSpeed, hero.getCurrentSpeed(), delta);
 		}
 	}
 
 }
