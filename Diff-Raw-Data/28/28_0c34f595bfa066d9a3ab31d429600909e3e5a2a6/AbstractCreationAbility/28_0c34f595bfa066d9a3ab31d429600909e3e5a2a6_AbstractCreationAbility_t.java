 package projectrts.model.abilities;
 
 import projectrts.model.abilities.pathfinding.AStar;
 import projectrts.model.entities.AbstractPlayerControlledEntity;
 import projectrts.model.entities.AbstractStructure;
 import projectrts.model.entities.EntityManager;
 import projectrts.model.entities.Player;
 import projectrts.model.world.Position;
 
 /**
  * An abstract class that holds common code for training units
  * 
  * @author Jakob Svensson
  * 
  */
 abstract class AbstractCreationAbility extends AbstractAbility {
 
 	private float buildTime;
 	private int buildCost;
 	private float buildTimeLeft;
 	private static float cooldown = 0.5f;
 	private Position spawnPos;
 	private String entityToTrain;
 
 	protected void initialize(AbstractPlayerControlledEntity entity) {
 		this.entity = entity;
 		this.setCooldown(cooldown);
 	}
 
 	@Override
 	public void update(float tpf) {
 		if (isActive() && !isFinished()) {
 			if (buildTimeLeft <= 0) {
 				spawnPos = AStar.getClosestUnoccupiedNode(entity.getPosition(),
 						null, 0).getPosition();
 				EntityManager.INSTANCE.addNewPCE(entityToTrain,
 						(Player) entity.getOwner(), spawnPos);
 				setFinished(true);
 				AbstractStructure as = (AbstractStructure) entity;
 				as.setTrainingUnit(false);
 				buildTimeLeft = buildTime;
 				pcs.firePropertyChange("BuildCompleted", entity, null);
 			} else {
 				pcs.firePropertyChange("TrainTimeLeft", entity,
 						(int) (buildTimeLeft - tpf + 1));
 				buildTimeLeft -= tpf;
 			}
 		}
 	}
 
 	@Override
 	public void useAbility(Position target) {
 		AbstractStructure as = (AbstractStructure) entity;
		if (isActive() || as.isTrainingUnit()) {
 			pcs.firePropertyChange("AlreadyTraining", null, null);
 		} else {
 			Player owner = (Player) entity.getOwner();
 			if (owner.getResources() >= buildCost) {
 				owner.modifyResource(-buildCost);
 				setActive(true);
 				setFinished(false);
 				buildTimeLeft = buildTime;
 				as.setTrainingUnit(true);
 			} else {
 				pcs.firePropertyChange("NotEnoughResources", null, null);
 			}
 		}
 	}
 
 	protected void setBuildTime(float buildTime) {
 		this.buildTime = buildTime;
 	}
 
 	protected void setBuildCost(int buildCost) {
 		this.buildCost = buildCost;
 	}
 
 	protected void setEntityToTrain(String name) {
 		this.entityToTrain = name;
 	}
 
 	@Override
 	public void abortAbility() {
 		// Override so it won't be aborted if the ability is used again before
 		// it's finished
 	}
 }
