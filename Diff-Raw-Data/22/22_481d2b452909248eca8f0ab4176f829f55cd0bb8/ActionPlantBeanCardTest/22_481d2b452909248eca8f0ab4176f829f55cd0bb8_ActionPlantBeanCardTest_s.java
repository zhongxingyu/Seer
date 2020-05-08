 import static org.hamcrest.CoreMatchers.not;
 import static org.junit.Assert.assertThat;
 import static org.junit.matchers.JUnitMatchers.hasItem;
 import org.junit.Before;
 import org.junit.Test;
 import bohnanza.core.BeanCard;
 import bohnanza.core.Card;
import bohnanza.core.Field;
 import bohnanza.core.IllegalActionException;
 import bohnanza.core.shared.actions.PlantBean;
 import bohnanza.standard.model.BohnanzaPlayer;
 import bohnanza.standard.model.EBeanType;
 import bohnanza.standard.model.StandardGame;
 
 /** Test class for the Player.
  * @author Anne van de Venis & Damiaan van der Kruk */
 public class ActionPlantBeanCardTest {
 
 	private static final String PLAYER_NAME = "Player";
 	private BohnanzaPlayer player;
 	private StandardGame game;
	private Card card1;
	private Card card2;
	private Field field;
 
 	@Before
 	public void setUp() throws IllegalActionException {
 		player = new BohnanzaPlayer(PLAYER_NAME);
 		game = new StandardGame();
 		game.addPlayer(player);
 		card1 = new BeanCard(EBeanType.BLACKEYEDBEAN);
 		card2 = new BeanCard(EBeanType.CHILIBEAN);
 		field = player.getBeanFields().get(0);
 	}
 
 	@Test
 	public void testPlant() throws IllegalActionException {
 		player.addCardToHand(card1);
 		player.addCardToHand(card2);
 		new PlantBean(game, player, card1, field).handle();
		assertThat("Card wasn't removed from the hand", player.getHand(), not(hasItem(card1)));
 		assertThat("Card wasn't planted in the players field", field.getCards(), hasItem(card1));
 	}
 
 	@Test(expected = IllegalActionException.class)
 	public void testPlantNoCardsInHands() throws IllegalActionException {
 		player.addCardToHand(card1);
 		new PlantBean(game, player, card2, field).handle();
 	}
 
 	@Test(expected = IllegalActionException.class)
 	public void testPlantInNotMatchingField() throws IllegalActionException {
 		player.addCardToHand(card1);
 		player.addCardToHand(card2);
 		new PlantBean(game, player, card1, field).handle();
 		new PlantBean(game, player, card2, field).handle();
 	}
 }
