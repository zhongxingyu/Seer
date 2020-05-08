 package projectrts.model.abilities;
 
 import javax.vecmath.Vector2d;
 
 import projectrts.model.abilities.pathfinding.AStar;
 import projectrts.model.abilities.pathfinding.AStarPath;
 import projectrts.model.abilities.pathfinding.AStarUser;
 import projectrts.model.entities.AbstractPlayerControlledEntity;
 import projectrts.model.world.INode;
 import projectrts.model.world.Position;
 import projectrts.model.world.World;
 
 /**
  * An ability for moving
  * 
  * @author Filip Brynfors, modified by Bjorn Persson Mattsson
  * 
  */
 public class MoveAbility extends AbstractAbility implements IStationaryAbility,
 		ITargetAbility, AStarUser {
 	private Position targetPosition;
 
 	private World world;
 	private INode occupiedNode;
 
 	private AStarPath path;
 	private boolean waitingForPath = false;
 
 	static {
 		AbilityFactory.registerAbility(MoveAbility.class.getSimpleName(),
 				new MoveAbility());
 	}
 
 	/**
 	 * When subclassing, invoke this to initialize the ability.
 	 */
 	protected void initialize(AbstractPlayerControlledEntity entity) {
 		this.entity = entity;
 		this.world = World.INSTANCE;
 		this.occupiedNode = world.getNodeAt(entity.getPosition());
 	}
 
 	@Override
 	public String getName() {
 		return "Move";
 	}
 
 	@Override
 	public void useAbility(Position pos) {
 		this.targetPosition = pos;
 
 		// Refreshing path as soon as a click is made
 		refreshPath();
 
 		setActive(true);
 		setFinished(false);
 	}
 
 	@Override
 	public void update(float tpf) {
 		if (isActive() && !isFinished() && !waitingForPath) {
 			moveToNewPosition(tpf);
 
 			// if finished
 			if (path.isEmpty()) { // if at target
 				waitingForPath = false;
 				setFinished(true);
 			}
 		}
 	}
 
 	private void moveToNewPosition(float tpf) {
		// TODO Plankton: PMD: Avoid if (x != y) ..; else ..;
 		if (path != null && !path.isEmpty()) { // if path already exists
 			entity.setPosition(calculateNextPosition(tpf));
 		} else // if (!pathAlreadyExists)
 		{
 			refreshPath();
 		}
 	}
 
 	/**
 	 * Returns the position of the next step using A* algorithm.
 	 * 
 	 * @param stepLength
 	 *            Length of the step the entity can take this update.
 	 * @param entity
 	 *            The entity that's moving.
 	 * @param targetPos
 	 *            The position that the entity will move towards.
 	 * @return Position of next step.
 	 */
 	private Position calculateNextPosition(float tpf) {
 		Position newPosition = entity.getPosition();
 		Position nextNodePos = path.getNextNode().getPosition();
 
 		double stepLength = tpf * entity.getSpeed(); // *nodeModifier
 		double distanceToNextNode = Position.getDistance(newPosition,
 				nextNodePos);
 
 		if (stepLength >= distanceToNextNode) {
 			newPosition = nextNodePos.copy();
 			refreshPath();
 		} else // if (stepLength < distanceToNextNode)
 		{
 			Vector2d direction = Position.getVectorBetween(newPosition,
 					nextNodePos);
 			newPosition = newPosition.add(stepLength, direction);
 		}
 		return newPosition;
 	}
 
 	private void refreshPath() {
 		waitingForPath = true;
 		AStar.calculatePath(entity.getPosition(), targetPosition, 2,
 				entity.getEntityID(), this);
 	}
 
 	@Override
 	public AbstractAbility createAbility(AbstractPlayerControlledEntity entity) {
 		MoveAbility newAbility = new MoveAbility();
 		newAbility.initialize(entity);
 		return newAbility;
 	}
 
 	/**
 	 * Updates the target without calculating a new path right away.
 	 * 
 	 * @param newTarget
 	 *            The new target.
 	 */
 	public void updateTarget(Position newTarget) {
 		this.targetPosition = newTarget.copy();
 	}
 
 	/**
 	 * @return the occupiedNode
 	 */
 	public INode getOccupiedNode() {
 		return occupiedNode;
 	}
 
 	@Override
 	public void receivePath(AStarPath newPath) {
 		if (this.waitingForPath) {
 			this.path = newPath;
 
 			if (!path.isEmpty()) {
 				world.setNodesOccupied(occupiedNode, entity.getSize(), 0);
 				this.occupiedNode = path.getNextNode().getNode();
 				world.setNodesOccupied(occupiedNode, entity.getSize(),
 						entity.getEntityID());
 			}
 
 			waitingForPath = false;
 		}
 	}
 
 	@Override
 	public String getInfo() {
 		return "Moves to the selected position";
 	}
 }
