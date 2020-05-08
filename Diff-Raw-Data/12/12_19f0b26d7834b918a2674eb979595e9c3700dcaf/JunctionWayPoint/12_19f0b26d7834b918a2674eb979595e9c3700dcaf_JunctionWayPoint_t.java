 package environment;
 
 import common.IVector;
 
 import driver.Animus;
 
 public class JunctionWayPoint extends WayPoint {
 
 	/**
 	 * instance variables
 	 */
 	protected IJunction junction;
 	protected IVector position;
 	
 	/**
 	 * constructor
 	 * @param lane
 	 * @param junction
 	 */
 	public JunctionWayPoint (ILane lane, IJunction junction){
 		super(lane);
 		this.lane = lane;
 		this.junction = junction;
 	}
 	
 	
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public int compareTo(IWayPoint o) {
 		// TODO Auto-generated method stub
 		return 0;
 	}
 	
 	/**
 	 * return the corresponding junction
 	 * @return IJunction
 	 */
 	public IJunction getJunction(){
 		return this.junction;
 	}
 	
	/*
	* the next two methods are inconsistent as far as they call a method getVehiclePosition() when they actually
	* are on a WayPoint. Nevertheless the method is the correct one as it gets the coordinates according to a percentage
	* on the lane.
	* the value of 0.8 indicates that you see the junction as early as you have passed 79% percent of the lanes length
	* before the junction.
	*/
	
 	/**
 	 * {@inheritDoc}
 	 */	
	
 	@Override
 	public float getXPos () throws Exception{
 		if (this.position == null){
 			this.position = this.lane.getVehiclePosition((float)(this.lane.getLength()*0.8f));
 		}
 		return this.position.getComponent(0);
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 */	
 	@Override
 	public float getYPos () throws Exception{
 		if (this.position == null){
 			this.position = this.lane.getVehiclePosition((float)(this.lane.getLength()*0.8f));
 		}
 		return this.position.getComponent(1);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void visitHandleWayPoint(Animus animus) {
 		animus.handleWayPoint(this);
 	}
 
 }
