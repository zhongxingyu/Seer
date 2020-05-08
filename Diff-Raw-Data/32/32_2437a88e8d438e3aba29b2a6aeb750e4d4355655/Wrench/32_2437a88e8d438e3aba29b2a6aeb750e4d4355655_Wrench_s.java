 package se.chalmers.dryleafsoftware.androidrally.model.gameBoard;
 
 import se.chalmers.dryleafsoftware.androidrally.model.robots.Robot;
 
 /**
  * Representing a wrench on the gameBoard. Robots staying on this element will repair
  * their damage with a fix amount.
  * @author
  *
  */
 public class Wrench implements BoardElement{
 	private int repairAmount;
 	
 	/**
 	 * Creates a new wrench element with a specific repair amount.
 	 * @param repairAmount the amount of damage on a robot which will be repaired.
 	 */
 	public Wrench(int repairAmount){
 		this.repairAmount = repairAmount;
 	}
 	
 	@Override
 	public void action(Robot robot) {
		robot.damage(-repairAmount);
 	}
 
 	@Override
 	public void instantAction(Robot robot) {
 		// Do nothing
 	}
 
 }
