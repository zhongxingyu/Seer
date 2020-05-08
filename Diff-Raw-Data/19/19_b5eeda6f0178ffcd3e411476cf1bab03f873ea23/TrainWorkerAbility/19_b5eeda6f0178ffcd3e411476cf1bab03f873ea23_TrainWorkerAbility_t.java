 package projectrts.model.abilities;
 
 import projectrts.model.entities.PlayerControlledEntity;
 import projectrts.model.entities.Worker;
 
 /**
  * A class that trains a unit
  * @author Jakob Svensson
  *
  */
 public class TrainWorkerAbility extends AbstractCreationAbility implements INotUsingMoveAbility {
 	private static float buildTime = 5; 
 	private static int buildCost = 50; 
 	
 	static {
 		AbilityFactory.INSTANCE.registerAbility(TrainWorkerAbility.class.getSimpleName(), new TrainWorkerAbility());
 	}
 	
 	/**
 	 * When subclassing, invoke this to initialize the ability.
 	 */
 protected void initialize(PlayerControlledEntity entity) {
 		super.initialize(entity);
 		this.setBuildCost(buildCost);
 		this.setBuildTime(buildTime);
 		this.setEntityToTrain(Worker.class.getSimpleName());
 	}
 	
 	@Override
 	public String getName() {
 		return "Train Worker";
 	}
 
 	@Override
 	public AbstractAbility createAbility(PlayerControlledEntity entity) {
 		TrainWorkerAbility newAbility = new TrainWorkerAbility();
 		newAbility.initialize(entity);
 		return newAbility;
 	}
 	
 	@Override
 	public String getInfo() {
		return "Trains a new Worker\nCost: " +buildCost;
 	}
 
 }
