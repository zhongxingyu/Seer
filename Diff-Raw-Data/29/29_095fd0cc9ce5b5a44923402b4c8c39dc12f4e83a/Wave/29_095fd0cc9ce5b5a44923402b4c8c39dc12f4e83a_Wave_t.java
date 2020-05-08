 package linewars.gamestate;
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import linewars.gamestate.mapItems.Gate;
 import linewars.gamestate.mapItems.MapItem;
 import linewars.gamestate.mapItems.MapItemState;
 import linewars.gamestate.mapItems.Unit;
 public class Wave {
 	private Lane owner;
 	private Node origin;
 	private ArrayList<Unit> units;
 	private Wave opponent;
 	
 	/**
 	 * Gets the lane that owns this wave
 	 * 
 	 * @return	the owning lane
 	 */
 	public Lane getLane()
 	{
 		return owner;
 	}
 	
 	/**
 	 * Gets the Node that this wave is walking away from.
 	 * @return The node that this wave is walking away from.
 	 */
 	public Node getOrigin()
 	{
 		return origin;
 	}
 	
 	public Wave(Lane owner, Unit u)
 	{
 		this.owner = owner;
 		opponent = null;
 		units = new ArrayList<Unit>();
 		units.add(u);
 	}
 	
 	public Wave(Lane owner)
 	{
 		this.owner = owner;
 		units = new ArrayList<Unit>();
 		opponent = null;
 	}
 	
 	public Unit[] getUnits()
 	{
 		return units.toArray(new Unit[0]);
 	}
 
 	/**
 	 * Adds the given unit to this wave, making sure that it's owned by the same player as the current owner of this wave.
 	 * @param u The unit to be added.
 	 * @return true if the unit was added and false otherwise.
 	 */
 	public boolean addUnit(Unit u)
 	{
 		//Check if the wave is empty and if it's not, make sure the unit you're trying to add belongs to the same player as the wave.
 		if(!units.isEmpty() && u.getOwner().getPlayerID() != units.get(0).getOwner().getPlayerID()){
 			return false;
 		}
 		return units.add(u);
 	}
 	
 	/**
 	 * Adds all of the units in u to the units in this wave.
 	 * @param u The array of units to be added to this wave.
 	 */
 	public void addUnits(Unit[] u)
 	{
 		for(int i = 0; i < u.length; i++)
 		{
 			this.addUnit(u[i]);
 		}
 	}
 	
 	/**
 	 * Gets the position of the wave within the lane.
 	 * @return a double that represents the percentage of the lane that is between the wave and p0 in the lane.
 	 */
 	public double getPosition()
 	{
 		//TODO implement this method
 		return 0.5;
 	}
 	
 	/**
 	 * Fixes all of the current collisions in this wave.
 	 */
 	private void fixCollisions()
 	{
 		HashMap<Unit, Position> collisionVectors = new HashMap<Unit, Position>();
 		
 		for(int i = 0; i < units.size(); i++)
 		{
 			Unit currentUnit = units.get(i);
 			if(currentUnit.getState() == MapItemState.Moving)
 			{
 				Position collisionVector;
 				MapItem[] collisions = owner.getCollisions(currentUnit);
 				Position totalVector = new Position(0,0);
 				for(int j = 0; j < collisions.length; j++)
 				{
 					double d = Math.sqrt(currentUnit.getPosition().distanceSquared(collisions[j].getPosition()));
 					double dPrime = currentUnit.getRadius() + collisions[j].getRadius();
 					double deltaD = dPrime - d;
 					if(collisions[j].getState() == MapItemState.Moving)
 					{
 						collisionVector = currentUnit.getPosition().subtract(collisions[j].getPosition());
 						collisionVector = collisionVector.scale(deltaD/2);
 					}else{
 						collisionVector = currentUnit.getPosition().subtract(collisions[j].getPosition());
 						collisionVector = collisionVector.scale(deltaD);
 					}
 					totalVector.add(collisionVector);
 				}
 				collisionVectors.put(currentUnit, totalVector);
 			}else{
 				collisionVectors.put(currentUnit, currentUnit.getPosition());
 			}
 		}
 		
 		for(int i = 0; i < units.size(); i++)
 		{
 			units.get(i).setPosition(collisionVectors.get(units.get(i)));
 		}
 	}
 	
 	
 	/**
 	 * Updates all of the units of this wave according to the movement and combat strategies of the units in it.
 	 */
 	//TODO Update this to be general. Right now it assumes a straight lane and units only moving straight.
 	public void update()
 	{
 		Gate destGate = null;
 		for(int i = 0; i < owner.getNodes().length; i++)
 		{
 			if(owner.getNodes()[i] != origin){
 				destGate = owner.getGate(owner.getNodes()[i]);
 			}
 		}
 		double lowestMove = 1;
		if(destGate == null){
			return;//TODO haxed together a NPE fix here, prob still a logical error here
		}
 		Position gatePos = destGate.getPosition();
 		if(opponent == null)
 		{
 			lowestMove = setTransformations(destGate, gatePos, 1);
 			setTransformations(destGate, gatePos, lowestMove);
 		}else{
 			Unit[] enemies = opponent.getUnits();
 			for(int i = 0; i < units.size(); i++)
 			{
 				units.get(i).getCombatStrategy().fight(enemies);
 			}
 		}
 		for(int j = 0; j < units.size(); j++)
 		{
 			units.get(j).getMovementStrategy().move();
 			units.get(j).update();
 		}
 		
 		fixCollisions();
 	}
 	
 
 	/**
 	 * Helper method for the update() method. Sets the targets for all the units in this wave based on the scale and returns the lowest move.
 	 * @param destGate The gate the units are using as their target.
 	 * @param gatePos The position of the target gate.
 	 * @param scale The modifier to the movement of the units.
 	 * @return The lowest move speed found.
 	 */
 	private double setTransformations(Gate destGate, Position gatePos, double scale) {
 		double ret = 1;
 		for(int i = 0; i < units.size(); i++)
 		{
 			Unit u = units.get(i);
 			
 			double destX = 0;
 			Position currentPos = u.getPosition();
 			double plus = currentPos.getX() - (gatePos.getX()+(destGate.getWidth()/2));
 			double minus = currentPos.getX() - (gatePos.getX()-(destGate.getWidth()/2));
 			if(Math.abs(plus) < Math.abs(minus))
 			{
 				destX = plus;
 			}else{
 				destX = minus;
 			}
 			
 			Position destPos = new Position((scale * destX), currentPos.getY());
 			Transformation target = new Transformation(destPos, u.getTransformation().getRotation());
 			double currentSpeed = u.getMovementStrategy().setTarget(target);
 			if(currentSpeed < ret)
 			{
 				ret = currentSpeed;
 			}
 		}
 		return ret;
 	}
 }
