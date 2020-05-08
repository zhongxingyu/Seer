 package projectrts.core;
 
 public class Unit implements IUnit {
 
 	private Position position;
 	
	public Unit()
 	{
 	}
 	
 	@Override
 	public Position getPosition() {
		// TODO Unit.getPosition()
		return null;
 	}
 	
 	/**
 	 * Updates the unit.
 	 * @param tpf Time per frame
 	 */
 	public void update(float tpf)
 	{
 		// TODO Unit.update()
 	}
 	
 	/**
 	 * Orders the unit to move to the provided position.
 	 * @param p Target position
 	 */
 	public void moveTo(Position p)
 	{
 		// TODO Unit.moveTo()
	}	
 }
