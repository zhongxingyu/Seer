 package driver;
 
 import common.IRectangle;
 import common.IVector;
 import common.Rectangle;
 import common.Vector;
 
 public class DriverViewCircleSector implements IDriverView {
 	/**
 	 * The properties of the view
 	 */
 	private IVector direction;
 	private IVector position;
 	private float angle;
 	private float radius;
 	
 	public DriverViewCircleSector(){
 		
 	}
 	
 	/**
 	 * constructor with direction and position
 	 * @param direction
 	 * @param position
 	 */
 	public DriverViewCircleSector(IVector direction,IVector position){
 		this.direction = direction;
 		this.position = position;
 	}
 	
 	/**
 	 * constructor with angle and distance
 	 * @param angle
 	 * @param distance
 	 */
 	
 	public DriverViewCircleSector(float angle, float distance){
 		this.angle = angle;
 		this.radius = distance;
 	}
 	
 	/**
 	 * private constructor for complete instance creation
 	 * @param direction
 	 * @param position
 	 * @param angle
 	 * @param distance
 	 */
 	
 	private DriverViewCircleSector(IVector direction, IVector position, float angle, float distance){
 		this.direction = direction;
 		this.position = position;
 		this.angle = angle;
 		this.radius = distance;
 	}
 	
 	
 	@Override
 	public void setAngle(float value) {
 		this.angle = value;
 	}
 
 	@Override
 	public void setDistance(float value) {
 		this.radius = value;
 	}
 
 	@Override
 	public void setDirection(IVector value) {
 		this.direction = value;
 	}
 
 	@Override
 	public void setPosition(IVector value) {
 		this.position = value;
 	}
 
 	@Override
 	public float getAngle() {
 		return angle;
 	}
 
 	@Override
 	public float getDistance() {
 		return radius;
 	}
 
 	@Override
 	public IVector getPosition() {
 		return position;
 	}
 
 	@Override
 	public IVector getDirection() {
 		return direction;
 	}
 
 	@Override
 	public boolean checkWayPoint(IVector position) {
 		IVector relative = position.sub(this.position);
		if (relative.norm() < radius){
 			relative = relative.rotate(-direction.getAngle());
			if (2*Math.PI-angle/2 >= relative.getAngle() || angle/2 <= relative.getAngle()){
 				return true;
 			}
 		}
 		return false;
 	}
 	
 	@Override
 	public IDriverView clone (){
 		return new DriverViewCircleSector(this.direction,this.position,this.angle,this.radius);
 	}
 
 	@Override
 	public IRectangle getBoundingBox() {
		IVector topRight = new Vector (new float[]{1,1});
		IVector bottomLeft = new Vector (new float[]{-1,-1});
 		IRectangle bbox = new Rectangle(
			this.position.add(bottomLeft.normalize().multiply(radius)),
			this.position.add(topRight.normalize().multiply(radius))
 		);
 		return bbox;
 	}
 
 }
