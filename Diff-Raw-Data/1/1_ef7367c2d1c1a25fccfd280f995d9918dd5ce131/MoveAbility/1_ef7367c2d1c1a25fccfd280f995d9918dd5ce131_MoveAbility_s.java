 package projectrts.model.entities.abilities;
 
 import javax.vecmath.Vector2d;
 
 import projectrts.model.constants.P;
 import projectrts.model.entities.AbstractAbility;
 import projectrts.model.entities.PlayerControlledEntity;
 import projectrts.model.pathfinding.AStar;
 import projectrts.model.pathfinding.AStarNode;
 import projectrts.model.pathfinding.AStarPath;
 import projectrts.model.pathfinding.World;
 import projectrts.model.utils.ModelUtils;
 import projectrts.model.utils.Position;
 
 /**
  * An ability for moving
  * @author Filip Brynfors, modified by Bjorn Persson Mattsson
  *
  */
 public class MoveAbility extends AbstractAbility {
 	private PlayerControlledEntity entity;
 	private Position targetPosition;
 	
 	private World world;
 	private AStar aStar;
 	private AStarPath path;
 	private boolean pathRefresh = true;
 	
 	static {
 		AbilityFactory.INSTANCE.registerAbility(MoveAbility.class.getSimpleName(), new MoveAbility());
 	}
 	
 	/**
 	 * When subclassing, invoke this to initialize the ability.
 	 */
 	protected void initialize() {
 		this.aStar = AStar.getInstance();
 		this.world = World.getInstance();
 	}
 	
 	@Override
 	public String getName() {
 		return "Move";
 	}
 	
 	@Override
 	public void useAbility(PlayerControlledEntity entity, Position pos){
 		this.entity = entity;
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
 			path = aStar.calculatePath(entity.getPosition(), targetPos, entity.getEntityID());
 		}
 		
 		Position outputPos = entity.getPosition();
 		
 		while (stepLength > 0) // repeat until the whole step is taken (or no nodes are left in the path)
 		{
 			if (path.nrOfNodesLeft() < 1)
 			{
 				break;
 			}
 			AStarNode nextNode = path.getNextNode();
 			double distanceToNextNode = ModelUtils.INSTANCE.getDistance(outputPos, nextNode.getPosition());
 			
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
 				path = aStar.calculatePath(outputPos, targetPos, entity.getEntityID());
 				
 				if (path.nrOfNodesLeft() > 0)
 				{
 					world.setNodesOccupied(nextNode.getNode(),
 							entity.getSize(), 0);
 					world.setNodesOccupied(path.getNextNode().getNode(),
 							entity.getSize(), entity.getEntityID());
 				}
 			}
 		}
 		return outputPos;
 	}
 	
 	@Override
 	public AbstractAbility createAbility() {
 		MoveAbility newAbility = new MoveAbility();
 		newAbility.initialize();
 		return newAbility;
 	}
 }
