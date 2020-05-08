 package projectrts.model;
 
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertTrue;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import projectrts.model.entities.AbstractPlayerControlledEntity;
 import projectrts.model.entities.EntityManager;
 import projectrts.model.entities.Player;
 import projectrts.model.entities.Warrior;
 import projectrts.model.world.Position;
 
 public class MicroAITest {
	// TODO Markus: PMD: Perhaps 'aiPlayer' could be replaced by a local variable.
 	private Player aiPlayer;
 	private Player humanPlayer;
 	private GameModel model;
 	private AbstractPlayerControlledEntity myWarrior;
 	private AbstractPlayerControlledEntity enemyWarrior;
 
 	@Before
 	public void setUp() {
 		model = new GameModel();
 		aiPlayer = (Player) model.getAIPlayer();
 		humanPlayer = (Player) model.getHumanPlayer();
 		EntityManager.INSTANCE.addNewPCE(Warrior.class.getSimpleName(),
 				humanPlayer, new Position(32.5, 42.5));
 		EntityManager.INSTANCE.addNewPCE(Warrior.class.getSimpleName(),
 				aiPlayer, new Position(32.5, 32.5));
 		model.update(1f);
 		myWarrior = EntityManager.INSTANCE.getPCEAtPosition(new Position(32.5,
 				42.5), humanPlayer);
 		enemyWarrior = EntityManager.INSTANCE.getPCEAtPosition(new Position(
 				32.5, 32.5), aiPlayer);
 	}
 
 	@Test
 	public void testUpdate() {
 		// Set the position of the human controlled warrior to just outside the
 		// enemyWarriors sight range.
 		myWarrior.setPosition(new Position(enemyWarrior.getPosition().getX(),
 				enemyWarrior.getPosition().getY() + myWarrior.getSightRange()
 						+ 1));
 
 		// Check that the enemy warrior stays put.
 		Position enemyPosition = enemyWarrior.getPosition();
 		model.update(1);
 		assertTrue(enemyWarrior.getPosition().equals(enemyPosition));
 
 		// Set the warriors within view distance of each other.
 		myWarrior.setPosition(new Position(32.5, 34.5));
 		enemyWarrior.setPosition(new Position(32.5, 34.5 + enemyWarrior
 				.getSightRange()));
 
 		// Select the warrior controlled by the human player and make sure it is
 		// selected.
 		EntityManager.INSTANCE.select(new Position(32.5, 34.5), humanPlayer);
 		assertTrue(EntityManager.INSTANCE.isSelected(myWarrior));
 
 		// Update the model and check that the human controlled warrior is dead
 		// and that the enemy hasn't taken any damage.
 		for (int i = 0; i < 500; i++) {
 			model.update(1.0f);
 		}
 		assertTrue(myWarrior.isDead());
 		assertFalse(enemyWarrior.getCurrentHealth() < enemyWarrior
 				.getMaxHealth());
 
 	}
 }
