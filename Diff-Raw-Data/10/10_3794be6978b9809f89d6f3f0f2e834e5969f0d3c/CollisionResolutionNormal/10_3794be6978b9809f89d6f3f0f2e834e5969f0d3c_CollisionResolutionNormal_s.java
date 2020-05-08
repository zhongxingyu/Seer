 package linewars.gamestate.mapItems.strategies.movement;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 
 import linewars.gamestate.Position;
 import linewars.gamestate.mapItems.Unit;
 
 public class CollisionResolutionNormal {
 	private List<Position> collisionsFromLastTick = new ArrayList<Position>();
 	private boolean hadToGoBackwardsLastTick = false;
 	private static final double resolveCollisionAttemptMoveFactor = 0.1;
 	
 	public void notifyOfCollision(Position direction) {
 		collisionsFromLastTick.add(direction);
 	}
 	
 	public Position adjustTargetFromNonmoving(Unit unit, double speed){
 		boolean collisions = collisionsFromLastTick.size() != 0;
		
 		if(!collisions){
 			hadToGoBackwardsLastTick = true;
 			return unit.getPosition();
 		}
 		
 		Position averageCollisionDirection = new Position(0, 0);
 		for(Position toInclude : collisionsFromLastTick){
			averageCollisionDirection.add(toInclude);
 		}
 		
 		if(averageCollisionDirection.length() < 0.01){
 			return unit.getPosition();
 		}
 		
 		Position relativeTarget = averageCollisionDirection.scale(1.0 / averageCollisionDirection.length());
		relativeTarget = relativeTarget.scale(speed * resolveCollisionAttemptMoveFactor);
		
 		collisionsFromLastTick.clear();
 		return unit.getPosition().add(relativeTarget);
 	}
 	
 	public Position adjustTarget(Position currentTarget, Unit unit, double speed){
 		boolean collisions = false;
 		if(collisionsFromLastTick.size() != 0){
 			collisions = true;
 			currentTarget = adjustTargetHelper(currentTarget, unit, speed);
 		}
 
 		//if we are just now free of collisions
 		if(!collisions && hadToGoBackwardsLastTick){
 			//then we need to move slowly since we're possibly just going to jump back into collision anyways
 			Position relativeTarget = currentTarget.subtract(unit.getPosition());
 			relativeTarget = relativeTarget.scale(resolveCollisionAttemptMoveFactor);
 			currentTarget = unit.getPosition().add(relativeTarget);
 		}
 
 		if(collisions == false){
 			hadToGoBackwardsLastTick = false;
 		}
 		
 		collisionsFromLastTick.clear();
 		return currentTarget;
 	}
 
 	private Position adjustTargetHelper(Position currentTarget, Unit unit, double speed) {
 
 		//this unit might be colliding with some mapitems
 		//if this is the case, we probably need to change target
 		Position relativeTarget = currentTarget.subtract(unit.getPosition());
 		
 		Position adjustedClockwise = relativeTarget;
 		Position adjustedCounterClockwise = relativeTarget;
 		
 		boolean modified = false;
 		boolean giveUpClockwise = false;
 		boolean giveUpCounterClockwise = false;
 		do{
 			modified = false;
 			for(Position toConsider : collisionsFromLastTick){
 				if(!giveUpClockwise && adjustedClockwise.dot(toConsider) > 0){//if the angle between these two vectors is acute
 					//then this means we have to adjust further in a clockwise direction
 					adjustedClockwise = toConsider.orthogonal();//this always returns a vector rotated clockwise from the original
 					if(adjustedClockwise.dot(relativeTarget) <= 0){//if the adjusted target would take us farther away from target
 						//we should just give up
 						giveUpClockwise = true;
 					}else{
 						modified = true;
 					}
 				}
 				if(!giveUpCounterClockwise && adjustedCounterClockwise.dot(toConsider) > 0){
 					modified = true;
 					adjustedCounterClockwise = toConsider.orthogonal().scale(-1);
 					if(adjustedCounterClockwise.dot(relativeTarget) <= 0){
 						giveUpCounterClockwise = true;
 					}else{
 						modified = true;
 					}
 				}
 				if(modified){//if either of the vectors were modified
 					break;//then we have to recheck their compatibility with all of the collisions
 				}
 			}
 		}while(modified);
 		
 		if(!giveUpClockwise && !giveUpCounterClockwise){//if both vectors will still get us closer
 			adjustedClockwise = adjustedClockwise.normalize();
 			adjustedCounterClockwise = adjustedCounterClockwise.normalize();
 			//if the angle between relativeTarget and adjustedClockwise is more obtuse than the angle 
 			//between relativeTarget and adjustedCounterClockwise
 			if(relativeTarget.dot(adjustedClockwise) < relativeTarget.dot(adjustedCounterClockwise)){
 				giveUpClockwise = true;
 			}else{
 				giveUpCounterClockwise = true;
 			}
 		}
 		
 		if(!giveUpClockwise){
 			relativeTarget = adjustedClockwise.normalize().scale(speed);
 		}else if(!giveUpCounterClockwise){
 			relativeTarget = adjustedCounterClockwise.normalize().scale(speed);
 		}
 		
 		if(giveUpClockwise && giveUpCounterClockwise){
 			hadToGoBackwardsLastTick = true;
 			relativeTarget = relativeTarget.scale(-1 * resolveCollisionAttemptMoveFactor);
 			//add a tiny bit of random noise to make infinite oscillations impossible
 			Position noise = relativeTarget.orthogonal();
 			double noiseFactor = new Random(unit.getGameState().getTimerTick() + unit.getID()).nextDouble();
 			noiseFactor -= 0.5;
 			noiseFactor = noiseFactor * relativeTarget.length() * .1;
 			noise = noise.scale(noiseFactor);
 			relativeTarget = relativeTarget.add(noise);
 		}
 		
 		return relativeTarget.add(unit.getPosition());
 	}
 
 }
