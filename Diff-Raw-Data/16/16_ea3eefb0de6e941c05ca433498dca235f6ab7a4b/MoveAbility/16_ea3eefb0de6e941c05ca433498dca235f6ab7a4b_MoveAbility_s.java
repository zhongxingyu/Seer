 package projectrts.model.abilities;
 
 import javax.vecmath.Vector2d;
 
 import projectrts.model.abilities.pathfinding.AStar;
 import projectrts.model.abilities.pathfinding.AStarNode;
 import projectrts.model.abilities.pathfinding.AStarPath;
 import projectrts.model.entities.PlayerControlledEntity;
 import projectrts.model.world.INode;
 import projectrts.model.world.Position;
 import projectrts.model.world.World;
 
 /**
  * An ability for moving
  * @author Filip Brynfors, modified by Bjorn Persson Mattsson
  *
  */
 public class MoveAbility extends AbstractAbility implements INotUsingMoveAbility, ITargetAbility {
 	private PlayerControlledEntity entity;
 	private Position targetPosition;
 	
 	private World world;
 	private INode occupiedNode;
 
 	private AStarPath path;
 	private boolean pathRefresh = true;
 	
 	static {
 		AbilityFactory.INSTANCE.registerAbility(MoveAbility.class.getSimpleName(), new MoveAbility());
 		AStar.initialize(World.getInstance());
 	}
 	
 	/**
 	 * When subclassing, invoke this to initialize the ability.
 	 */
 	protected void initialize(PlayerControlledEntity entity) {
 		this.entity = entity;
 		this.world = World.getInstance();
 		this.occupiedNode = world.getNodeAt(entity.getPosition());
 	}
 	
 	@Override
 	public String getName() {
 		return "Move";
 	}
 	
 	@Override
 	public void useAbility(Position pos){
 		this.targetPosition = pos;
 		
 		// Want to refresh path as soon as a click is made
 		this.pathRefresh = true;
 		
 		setActive(true);
 		setFinished(false);
 	}
 
 	@Override
 	public void update(float tpf) {
 		if(isActive() && !isFinished()){
 			entity.setPosition(determineNextStep(tpf, entity, targetPosition));
 			
 			if (path.nrOfNodesLeft() == 0)
 			{
 				setFinished(true);
 			}
 			
 		}
 	}
 	
 	/**
 	 * Returns the position of the next step using A* algorithm.
 	 * @param stepLength Length of the step the entity can take this update.
 	 * @param entity The entity that's moving.
 	 * @param targetPos The position that the entity will move towards.
 	 * @return Position of next step.
 	 */
 	private Position determineNextStep(float tpf, PlayerControlledEntity entity, Position targetPos)
 	{
 		double stepLength = tpf*entity.getSpeed();
 		
 		if (path == null || path.nrOfNodesLeft() < 1 || pathRefresh )
 		{
 			pathRefresh = false;
 			refreshPath(occupiedNode.getPosition(), targetPos,
 					occupiedNode,
 					entity.getEntityID(), entity.getSize());
 			/*
 			path = aStar.calculatePath(entity.getPosition(), targetPos, entity.getEntityID());
 			world.setNodesOccupied(world.getNodeAt(entity.getPosition()), entity.getSize(), 0);
 			*/
 		}
 		
 		Position outputPos = entity.getPosition();
 		
 		while (stepLength > 0) // repeat until the whole step is taken (or no nodes are left in the path)
 		{
 			if (path.nrOfNodesLeft() < 1)
 			{
 				break;
 			}
 			AStarNode nextNode = path.getNextNode();
 			double distanceToNextNode = Position.getDistance(outputPos, nextNode.getPosition());
 			
 			if (distanceToNextNode > stepLength)
 			{
 				Vector2d direction = Position.getVectorBetween(outputPos, nextNode.getPosition());
 				direction.normalize();
 				outputPos = outputPos.add(stepLength, direction);
 				stepLength = 0;
 			}
 			else //if (distanceToNextNode <= stepLength)
 			{
 				stepLength -= distanceToNextNode;
 				outputPos = nextNode.getPosition().copy();
 				refreshPath(outputPos, targetPos, nextNode.getNode(),
 						entity.getEntityID(), entity.getSize());
 			}
 		}
 		return outputPos;
 	}
 	
 	private void refreshPath(Position herePos, Position targetPos, INode hereNode,
 			int entityID, float entitySize)
 	{
		path = AStar.calculatePath(herePos, targetPos, 5, entityID);
 		if (path.nrOfNodesLeft() > 0)
 		{
 			world.setNodesOccupied(hereNode,
 					entitySize, 0);
 			this.occupiedNode = path.getNextNode().getNode();
 			world.setNodesOccupied(this.occupiedNode,
 					entitySize, entityID);
 		}
 	}
 	
 	@Override
 	public AbstractAbility createAbility(PlayerControlledEntity entity) {
 		MoveAbility newAbility = new MoveAbility();
 		newAbility.initialize(entity);
 		return newAbility;
 	}
 	
 	public void updateTarget(Position newTarget)
 	{
 		this.targetPosition = newTarget.copy();
 	}
 	
 	/**
 	 * @return the occupiedNode
 	 */
 	public INode getOccupiedNode() {
 		return occupiedNode;
 	}
 }
