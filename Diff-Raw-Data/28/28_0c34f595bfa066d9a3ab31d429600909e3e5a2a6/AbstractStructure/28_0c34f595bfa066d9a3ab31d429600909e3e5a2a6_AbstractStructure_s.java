 package projectrts.model.entities;
 
 import projectrts.model.world.Position;
 
 /**
  * An abstract class for structures
  * 
  * @author Jakob Svensson
  * 
  */
 public abstract class AbstractStructure extends AbstractPlayerControlledEntity {
 	protected boolean deposit = false;
	// TODO Jakob: PMD: It is somewhat confusing to have a field name with the same name as a method
	// Change the method name to isTrainingUnit()  /Bjrn
 	protected boolean trainingUnit = false;
 
 	/**
 	 * When subclassing, invoke this to initialize the entity.
 	 * 
 	 * @param owner
 	 *            The owner of the entity.
 	 * @param spawnPos
 	 *            The initial position of the entity.
 	 */
 	protected void initialize(Player owner, Position spawnPos) {
 		super.initialize(owner, spawnPos);
 	}
 
 	/**
 	 * Returns whether the structure is a drop-off point for resources or not
 	 * 
 	 * @return true if the structure is able for deposits, false otherwise
 	 */
 	public boolean isDeposit() {
 		return deposit;
 	}
 
	public boolean trainingUnit() {
 		return trainingUnit;
 	}
 
 	public void setTrainingUnit(boolean b) {
 		trainingUnit = b;
 	}
 }
