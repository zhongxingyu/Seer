 package environment;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Stack;
 
 import common.GlobalConstants;
 import common.IVector;
 import common.Vector;
 
 import driver.IDriverView;
 
 public class WayPointManager implements IPlacableManager {
 
 	/**
 	 * instance variables
 	 */
 	
 	private static WayPointManager instance;
 	private Stack<IPlacable> stack;
 	private List<IWayPoint> waypoints;
 	private PlacableTreeNode root;
 
 	/**
 	 * private constructor
 	 * @throws Exception
 	 */
 	
 	private WayPointManager() throws Exception {
 		this.stack = new Stack<IPlacable>();
 		this.waypoints = new ArrayList<IWayPoint>();
 	}
 
 	/**
 	 * get the instance reference
 	 * @return refrence to singleton instance
 	 * @throws Exception
 	 */
 	
 	public synchronized static WayPointManager getInstance() throws Exception {
 		if (WayPointManager.instance == null) {
 			WayPointManager.instance = new WayPointManager();
 		}
 		return WayPointManager.instance;
 	}
 	
 	/**
 	 * Destroy the Way Point manager 
 	 */
 	public static void destroy() {
 		WayPointManager.instance = null;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void add(IPlacable placable){
		// TODO dig deeper into the exception issue posted on github by schtibe, logically there is only one reason
		// why this could throw an Exception and it's within PlacableTreeNode.add()
 		if (this.root != null){
 			this.root.add(placable);
 		}else{
 			stack.push(placable);
 			waypoints.add((IWayPoint)placable);
 		}
 	}
 
 
 	/**
 	 * {@inheritDoc}
 	 * @param movable
 	 * @param newX
 	 * @param newY
 	 */
 	public boolean move(IMovable movable, float newX, float newY) {
 		// TODO this is a workaround to make development of the rest of the solution go on while we have an issue here
 		// with the move method
 		try{
 			root.remove(movable);
 			movable.updatePosition(new Vector(new float[]{newX,newY}));
 			root.add(movable);
 		}catch (Exception ex){
 			System.out.println("ex in move");
 		}
 		//return root.move(movable, newX, newY);
 		return true;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void remove(IPlacable placable) throws Exception{
 		root.remove(placable);
 	}
 
 	/**
 	 * returns a list of way points contained in the driver view's hit box
 	 * @param view
 	 * @return list of way points
 	 * @TODO Use the calculation of the driver view
 	 */
 	public List<IPlacable> findWayPoints(IDriverView view) {
 		float halfViewAngle = (view.getAngle()/2);
 		
 		IVector upperBound = (view.getDirection().rotate(halfViewAngle)).normalize().multiply(view.getDistance());//.add(view.getPosition());
 		IVector lowerBound = (view.getDirection().rotate(-halfViewAngle)).normalize().multiply(view.getDistance());//.add(view.getPosition());
 
 		upperBound = upperBound.add(view.getPosition());
 		lowerBound = lowerBound.add(view.getPosition());
 
 		IVector[] xtremes = getMinMaxVectors(new IVector[]{view.getPosition(),upperBound,lowerBound});
 		IVector min = xtremes[0];
 		IVector max = xtremes[1];//.sub(min);
 		List<IPlacable> found = root.findInArea(
 				min.getComponent(0), 
 				min.getComponent(1), 
 				max.getComponent(0), 
 				max.getComponent(1)
 		);
 		
 		List<IPlacable> ret = new ArrayList<IPlacable>();
 		
 	
 		for (IPlacable pl : found) {
 			if (view.checkWayPoint(new Vector(new float[]{pl.getXPos(), pl.getYPos()}))) {
 				ret.add(pl);
 			}
 		}
 		
 		return ret;
 	}
 	
 	/**
 	 * creates the bounding box of a given vector array (min and max values)
 	 * @param input
 	 * @return
 	 */
 	
 	private IVector[] getMinMaxVectors (IVector[] input){
 		IVector min = input[0].clone();
 		IVector max = input[0].clone();
 		for (IVector vec : input){
 			float minX = min.getComponent(0);
 			float minY = min.getComponent(1);
 			float maxX = max.getComponent(0);
 			float maxY = max.getComponent(1);
 			if (vec.getComponent(0) < minX){
 				minX = vec.getComponent(0);
 			}
 			if (vec.getComponent(1)<minY){
 				minY = vec.getComponent(1);
 			}
 			min = new Vector (new float[]{minX,minY});
 			if (vec.getComponent(0) > maxX){
 				maxX = vec.getComponent(0);
 			}
 			if (vec.getComponent(1) > maxY){
 				maxY = vec.getComponent(1);
 			}
 			max = new Vector(new float[]{maxX,maxY});
 		}
 		return new IVector[]{min,max};
 	}
 	
 	/**
 	 * sets all the way points
 	 * @throws Exception
 	 */
 	
 	public void setWayPoints() throws Exception {
 		IVector[] minMax = GlobalConstants.getInstance().getWorldBoundaries();
 		IVector max = minMax[1].sub(minMax[0]);
 		
 		this.root = new PlacableTreeNode(
 			minMax[0].getComponent(0), 
 			minMax[0].getComponent(1), 
 			max.getComponent(0),
 			max.getComponent(1), 
 			6
 		);
 		
 		if (!stack.isEmpty()){
 			IPlacable placable = stack.pop();
 			while (placable != null){
 				WayPointManager.getInstance().add(placable);
 				if (!stack.empty()){
 					placable = stack.pop();
 				}else{
 					placable = null;
 				}
 			}
 		}
 	}
 	
 	/**
 	 * returns a list of all the way points
 	 * @return
 	 */
 	
 	public List<IWayPoint> getWayPoints() {
 		return this.waypoints;
 	}
 }
