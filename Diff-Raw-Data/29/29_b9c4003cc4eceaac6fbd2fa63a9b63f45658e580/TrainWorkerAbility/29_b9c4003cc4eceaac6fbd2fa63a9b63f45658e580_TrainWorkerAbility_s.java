 package projectrts.model.core.abilities;
 
 import projectrts.model.core.EntityManager;
 import projectrts.model.core.Player;
 import projectrts.model.core.Position;
 import projectrts.model.core.entities.PlayerControlledEntity;
 
 /**
  * A class that trains a unit
  * @author Jakob Svensson
  *
  */
 public class TrainWorkerAbility extends AbstractAbility{
 	private PlayerControlledEntity structure;
 	private float buildTime = 5; //TODO Jakob: Decide buidlTime and maybe set as a constant
 	private int buildCost = 50; //TODO Jakob: Decide buidlCost and maybe set as a constant
 	private Position spawnPos;
 	
 	static {
 		AbilityFactory.INSTANCE.registerAbility(TrainWorkerAbility.class.getSimpleName(), new TrainWorkerAbility());
 	}
 	
 	private TrainWorkerAbility() {
 		super();
 	}
 	
 	@Override
 	public String getName() {
 		return "TrainWorker";
 	}
 
 	@Override
 	public void update(float tpf) {
 		if(isActive() && !isFinished()){
 			if(buildTime<=0){
 				EntityManager.getInstance().addNewPCE("Worker", (Player)structure.getOwner(),spawnPos);
 				setFinished(true);
 				buildTime =5;
 			}else{
 				buildTime-=tpf;
 			}
 			System.out.println(buildTime);
 		}
 	}
 
 	@Override
 	public void useAbility(PlayerControlledEntity caster, Position target) {
 		structure = caster;
 		Player owner = (Player)structure.getOwner();
 		owner.modifyResource(-buildCost); //TODO Jakob: Check if player has enough resources
 		spawnPos = new Position(structure.getPosition().getX()+structure.getSize(),
				structure.getPosition().getX()+structure.getSize()); //TODO Jakob: Decide spawnPos, Rally points?
 		setActive(true);
 		setFinished(false);
 	}
 
 	@Override
 	public AbstractAbility createAbility() {
 		return new TrainWorkerAbility();
 	}
 
 }
