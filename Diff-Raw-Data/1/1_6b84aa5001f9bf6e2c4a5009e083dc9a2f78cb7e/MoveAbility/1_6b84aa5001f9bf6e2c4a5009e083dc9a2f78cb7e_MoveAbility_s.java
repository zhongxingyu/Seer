 package projectrts.model.core.abilities;
 
 import projectrts.model.core.P;
 import projectrts.model.core.Position;
 import projectrts.model.core.entities.PlayerControlledEntity;
import projectrts.model.core.utils.ModelUtils;
 
 /**
  * An ability for attacking
  * @author Filip Brynfors
  *
  */
 public class MoveAbility extends AbstractAbility {
 	private PlayerControlledEntity entity;
 	private Position targetPosition;
 	
 	
 	public MoveAbility(){
 
 	}
 	
 	@Override
 	public String getName() {
 		return "Attack";
 	}
 	
 	@Override
 	public void useAbility(PlayerControlledEntity attacker, Position pos){
 		this.entity = attacker;
 		this.targetPosition = pos;
 		
 		//TODO: Are these needed?
 		setActive(false);
 		setFinnished(false);
 	}
 
 	@Override
 	public void update(float tpf) {
 		
 		if(isActive() && !isFinnished()){
 			
 			
 			entity.setPosition(determinePath(targetPosition, tpf));
 			if (entity.getPosition().equals(targetPosition))
 			{
 				setFinnished(true);
 			}
 			
 		}
 	}
 
 	private Position determinePath(Position target, float tpf){
 		// TODO Extremely simple path algorithm
 		float stepSize = P.INSTANCE.getUnitLength()*tpf;
 		Position myPos = entity.getPosition();
 		float newX = 0;
 		float newY = 0;
 		
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
