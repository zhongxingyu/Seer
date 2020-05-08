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
 	
 	/**
 	 * {@inheritDoc}
 	 */	
 	@Override
 	public float getXPos () throws Exception{
 		if (this.position == null){
			// TODO what does that do?? seems very strange
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
