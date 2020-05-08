 package projectrts.model.core;
 
 import java.util.List;
 
 import projectrts.model.core.abilities.AttackAbility;
 import projectrts.model.core.abilities.BuildTowerAbility;
 import projectrts.model.core.abilities.DeliverResourceAbility;
 import projectrts.model.core.abilities.GatherResourceAbility;
 import projectrts.model.core.abilities.MineResourceAbility;
 import projectrts.model.core.abilities.MoveAbility;
 import projectrts.model.core.abilities.OffensiveSpellAbility;
 import projectrts.model.core.abilities.TrainWorkerAbility;
 import projectrts.model.core.entities.Headquarter;
 import projectrts.model.core.entities.IEntity;
 import projectrts.model.core.entities.IPlayerControlledEntity;
 import projectrts.model.core.entities.Resource;
 import projectrts.model.core.entities.Unit;
 import projectrts.model.core.entities.Warrior;
 import projectrts.model.core.entities.Worker;
 import projectrts.model.core.pathfinding.AStar;
 import projectrts.model.core.pathfinding.World;
 
 /**
  * The main model class of the RTS Game
  * The class handles the world and they players in the game
  * @author Bjrn Persson Mattson, Modified by Filip Brynfors, Jakob Svensson
  */
 public class GameModel implements IGame {
 	private World world = World.getInstance();
 	private EntityManager entityManager = EntityManager.getInstance();
 	private Player humanPlayer = new Player();
 	// TODO Plankton: Implement some sort of AI
 	private Player aiPlayer = new Player();
 	
 	static {
 		try
 		{
 			// Initialize the entity classes.
 			Class.forName(Warrior.class.getName());
 			Class.forName(Unit.class.getName());
 			Class.forName(Worker.class.getName());
 			Class.forName(Resource.class.getName());
 			Class.forName(Headquarter.class.getName());
 			
 			// Initialize the ability classes.
 			Class.forName(AttackAbility.class.getName());
 			Class.forName(BuildTowerAbility.class.getName());
 			Class.forName(DeliverResourceAbility.class.getName());
 			Class.forName(GatherResourceAbility.class.getName());
 			Class.forName(MineResourceAbility.class.getName());
 			Class.forName(MoveAbility.class.getName());
 			Class.forName(OffensiveSpellAbility.class.getName());
 			Class.forName(TrainWorkerAbility.class.getName());
 						
 		}
 		catch (ClassNotFoundException any)
 		{
 			any.printStackTrace();
 		}
     }
 		
 	public GameModel() {
 		world.initializeWorld(P.INSTANCE.getWorldHeight(), P.INSTANCE.getWorldWidth());
 		AStar.initialize(world);
 		entityManager.addNewPCE(Unit.class.getSimpleName(), humanPlayer, new Position(50, 50));
 		entityManager.addNewPCE(Worker.class.getSimpleName(), humanPlayer, new Position(55, 55));
		entityManager.addNewPCE(Headquarter.class.getSimpleName(), humanPlayer, new Position(60, 60));
		entityManager.addNewNPCE(Resource.class.getSimpleName(), new Position(40, 50));
 
 
 	}
 	
 	@Override
 	public void update(float tpf) {
 		entityManager.update(tpf);
 	}
 
 	@Override
 	public IPlayer getPlayer() {
 		return humanPlayer;
 	}
 
 	@Override
 	public List<IEntity> getAllEntities() {
 		return entityManager.getAllEntities();
 	}
 	
 	public List<IPlayerControlledEntity> getEntitiesOfPlayer() {
 		return entityManager.getEntitiesOfPlayer(humanPlayer);
 	}
 }
