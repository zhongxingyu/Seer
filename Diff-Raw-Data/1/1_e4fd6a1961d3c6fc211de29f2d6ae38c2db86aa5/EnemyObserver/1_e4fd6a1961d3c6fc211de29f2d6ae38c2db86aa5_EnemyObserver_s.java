 package euclidstand;
 
 import java.util.Observer;
 import java.util.Observable;
 import java.util.List;
 import java.util.logging.Logger;
 
 import com.jme.scene.Node;
 import com.jme.renderer.Renderer;
 
 // TODO: Initialise bad guy attributes (appearance, speed, damage)
 /**
  * Initialises and observes the state of enemies in the game, creating new
  * ones when needed.
  */
 public final class EnemyObserver extends EntityObserver implements Observer {
 
 	private static final Logger logger =
 			Logger.getLogger(EnemyObserver.class.getName());
 	private final Renderer renderer;
 	private final Node enemyNode;
 	private Entity target = null;
 	private int createdBaddies = 0;
 	private int currentBaddies = 0;
 
 	private EnemyObserver(List<Entity> entitiesToAdd,
 			Renderer renderer,
 			Entity target,
 			Node enemyNode) {
 		super(entitiesToAdd);
 		this.renderer = renderer;
 		this.target = target;
 		this.enemyNode = enemyNode;
 	}
 
 	/**
 	 * Factory method for EnemyObserver.
 	 *
 	 * Creates an initial wave of enemies
 	 * @param entitiesToAdd the list of entitiesToAdd
 	 * @param renderer current renderer
 	 * @param target for new enemies
 	 * @param sceneNode for current scene
 	 * @return an instance of EnemyObserver
 	 */
 	public static EnemyObserver getObserver(
 			List<Entity> entitiesToAdd,
 			Renderer renderer,
 			Entity target,
 			Node sceneNode) {
 		Node enemyNode = new Node("Enemies");
 		EnemyObserver observer =
 				new EnemyObserver(entitiesToAdd, renderer, target, enemyNode);
 		sceneNode.attachChild(observer.enemyNode);
 		observer.createWave();
 		return observer;
 	}
 
 	private void createWave() {
 		for (int i = 0; i < 50; i++) {
 			createEnemy();
 		}
 	}
 
 	private void createEnemy() {
 		createdBaddies += 1;
 		String name = "Badguy" + createdBaddies;
 		EnemyEntity badguy = new EnemyEntity(Factory.buildBaddie(name, renderer), target);
 
 		entitiesToAdd.add(badguy);
 		badguy.addObserver(this);
 		enemyNode.attachChild(badguy.getSelf());
 		currentBaddies += 1;
 	}
 
 	public void update(Observable o, Object arg) {
 		logger.info("Enemy died");
 		currentBaddies -= 1;
 		Entity entity = (Entity) o;
 		enemyNode.detachChild(entity.getSelf());
 
 		if (currentBaddies == 0) {
 			createWave();
 		}
 	}
 }
