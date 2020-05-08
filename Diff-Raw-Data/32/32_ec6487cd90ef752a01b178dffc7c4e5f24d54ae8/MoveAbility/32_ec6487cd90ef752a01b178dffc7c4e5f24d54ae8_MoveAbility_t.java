 package projectrts.model.core.abilities;
 
 import javax.vecmath.Vector2d;
 
 import projectrts.model.core.AStar;
 import projectrts.model.core.AStarPath;
 import projectrts.model.core.P;
 import projectrts.model.core.Position;
 import projectrts.model.core.entities.PlayerControlledEntity;
 import projectrts.model.core.utils.ModelUtils;
 
 /**
  * An ability for attacking
  * @author Filip Brynfors, modified by Bjorn Persson Mattsson
  *
  */
 public class MoveAbility extends AbstractAbility {
 	private PlayerControlledEntity entity;
 	private Position targetPosition;
 	
 	private AStar aStar;
 	private AStarPath path;
 	
 	
 	public MoveAbility(){
 
 		this.aStar = new AStar();
 	}
 	
 	@Override
 	public String getName() {
 		return "Move";
 	}
 	
 	@Override
 	public void useAbility(PlayerControlledEntity entity, Position pos){
 		this.entity = entity;
 		this.targetPosition = pos;
 		
 		//TODO: Are these needed?
 		setActive(true);
 		setFinished(false);
 	}
 
 	@Override
 	public void update(float tpf) {
 		if(isActive() && !isFinished()){
 			
			double stepSize = P.INSTANCE.getUnitLength()*tpf;
			entity.setPosition(determineNextStep(stepSize, entity, targetPosition));
 			if (entity.getPosition().equals(targetPosition))
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
 	private Position determineNextStep(double stepLength, PlayerControlledEntity entity, Position targetPos)
 	{
 		// TODO Don't update the path every update.
 		path = aStar.calculatePath(entity.getPosition(), targetPos);
 		
 		Position outputPos = entity.getPosition();
 		
 		while (stepLength > 0) // repeat until the whole step is taken (or no nodes are left in the path)
 		{
 			if (path.nrOfNodesLeft() < 1)
 			{
 				break;
 			}
 			Position nextNode = path.getNextNodePosition();
 			double distanceToNextNode = ModelUtils.INSTANCE.getDistance(outputPos, nextNode);
 			
 			if (distanceToNextNode > stepLength)
 			{
 				Vector2d direction = Position.getVectorBetween(outputPos, nextNode);
 				outputPos = outputPos.add(stepLength, direction);
 				stepLength = 0;
 			}
 			else //if (distanceToNextNode <= stepLength)
 			{
 				stepLength -= distanceToNextNode;
 				outputPos = nextNode;
 				path.removeNodeFromPath();
 			}
 		}
 		return outputPos;
 	}
 
 	private Position determinePath(Position target, float tpf){
 		// TODO Extremely simple path algorithm
 		double stepSize = P.INSTANCE.getUnitLength()*tpf;
 		Position myPos = entity.getPosition();
 		double newX = 0;
 		double newY = 0;
 		
 		// For x axis
 		if (Math.abs(myPos.getX() - target.getX()) < stepSize)
 		{
 			newX = target.getX();
 		}
 		else if (myPos.getX() < target.getX())
 		{
 			newX = myPos.getX()+stepSize;
 		}
 		else// if (myPos.getX() > target.getX())
 		{
 			newX = myPos.getX()-stepSize;
 		}
 		
 		// For y axis
 		if (Math.abs(myPos.getY() - target.getY()) < stepSize)
 		{
 			newY = target.getY();
 		}
 		else if (myPos.getY() < target.getY())
 		{
 			newY = myPos.getY()+stepSize;
 		}
 		else// if (myPos.getY() > target.getY())
 		{
 			newY = myPos.getY()-stepSize;
 		}
 		
 		return new Position(newX, newY);
 	}
 
 }
