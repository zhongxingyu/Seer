 package backend.environment;
 
 import java.util.HashMap;
 import java.util.List;
 
 import math.Vec;
 
 public class Prey extends Animal {
 	Waypoint previousTarget = null;
 	
 	public Prey(Prey other)					{	super(other);	}
 	public Prey(Vec position, Vec velocity)	{	super(position, velocity);	}
 	public Prey()							{	super();	}
 	public Prey(double x, double y, double xvel, double yvel, double size)	{	super(x,y,xvel,yvel,size);	}
 	
 	public double weighted(double x)	{
 		return Math.pow(x, 3)*4+0.5;
 	}
 	
 	public void setTarget(Waypoint t)	{
 		previousTarget = getTarget();
 		super.setTarget(t);
 	}
 	
 	/* Here we have two general approaches when dealing with multiple vectors:
 	 * 1. take a weighted average of the vectors
 	 * 2. use an accumulator: order the vectors by priority, start adding them up and when the 
 	 * 			total length exceeds some length limit, stop adding and truncate
 	 * 
 	 * here we have a number of sources of vectors:
 	 * 		1. collision avoidance pushes the prey away from fellow prey
 	 * 		2. velocity matching makes the prey try and match it's fellow prey member's velocities (to go in the same direction)
 	 * 		3. flock centering pulls the prey towards all the other prey members
 	 * 
 	 * //A total source vector for each of these sources is calculated using weighted averaging. Then the three vectors are placed in
 	 * //an accumulator to find the final velocity.
 	 * We used to use an accumulator, now we take an "informed weighted sum" of the weighted averages. To understand what I mean by "informed"
 	 * consider the case where we weight collisionAvoidance 0.2, flockCentering 0.2, velocityMatching 0.1 and predatorAvoidance 0.5.
 	 * Now if there were no predator's to avoid, then the other factors should make a total weighting of 1 and not 0.5. This "accounting for
 	 * absent impulses" is what we understand as "informed".
 	 * 
 	 * Note: Collision avoidance and flock centering aren't linearly dependent on the distance of the other prey.
 	 */
 	public void calculateUpdate(List<Element> influences) {
 		//calculate the sums
 		Vec collisionAvoidance = new Vec(),
 			velocityMatching = new Vec(),
 			flockCentering = new Vec(),
 			predatorAvoidance = new Vec(),
 			waypointAttraction = new Vec(),
 			obstacleAvoidance = new Vec();
 		
 		/* obstacleAvoidance has a dynamic weight. this is
 		 * because we can't afford to EVER run into obstacles.
 		 */
 		double collisionAvoidanceWeight = 0.15,
 			   velocityMatchingWeight = 0.1,
 			   flockCenteringWeight = 0.15,
 			   predatorAvoidanceWeight = 0.4,
 			   waypointAttractionWeight = 0.2;
 		int neighbourhoodCount = 0, predatorCount = 0, obstacleCount = 0;
 		HashMap<Waypoint, Integer> flockTargets = new HashMap<Waypoint, Integer>();
 		for(Element e : influences)	{
 			if(e instanceof Obstacle)	{
 				Vec mostLeft = null,
 						mostRight = null;
 				double mostLeftAngle = 0,
 						mostRightAngle = 0;
 				Vec dir = velocity.unit();
 				for(Waypoint w : (Obstacle)e)	{
 					//get the cos of the angle between this waypoint and the dir vector
 					Vec wdir = w.getPosition().minus(getPosition());
 					double dot = dir.dot(wdir.unit());	//this will be in [-1,1]
 					dot = 1-(dot+1)/2;			//now it's in [0,1] with 0 being 0 degrees and 1 being 180
 					//figure out which side (left or right) this angle is on
 					dot *= dir.crossCompare(wdir);
 					System.out.println(dot);
 					if(mostLeft == null || mostLeftAngle > dot)	{
 						mostLeft = wdir;
 						mostLeftAngle = dot;
 					}
 					if(mostRight == null || mostRightAngle < dot)	{
 						mostRight = wdir;
 						mostRightAngle = dot;
 					}
 				}
 				System.out.println(mostLeft + " " + mostRight + "\n");
 				if(mostLeft != null || mostRight != null)	{
					obstacleAvoidance = (Math.abs(mostLeftAngle) < Math.abs(mostRightAngle) ? mostLeft : mostRight).unit();
 					obstacleCount++;
 				}
 			}
 			else {
 				Vec dir = e.getPosition().minus(getPosition());
 				if(dir.size() > 0 && dir.size() <= getRadius())	{
 					if(e instanceof Prey)	{
 						neighbourhoodCount ++;
 						collisionAvoidance = collisionAvoidance.plus(dir.unit().mult(Math.pow((getRadius()-dir.size())/getRadius(),1)).neg());
 						//this needs to be fixed, it's very haxxy that I must divide by e.getMaxSpeed() to get the truncated-to-unit vector, e.velocity
 						velocityMatching = velocityMatching.plus(e.getVelocity().mult(1.0/e.getMaxSpeed()));
 						flockCentering = flockCentering.plus(dir.unit().mult(Math.pow(dir.size()/getRadius(),1)));
 						
 						//take target suggestions from flock members who are in front
 						if(e.getPosition().minus(getPosition()).dot(velocity) > 0)	{
 							Integer count = flockTargets.get(e.getTarget());
 							count = (count == null ? 0 : count) + 1;
 							flockTargets.put(e.getTarget(), count);
 						}
 					}
 					else if(e instanceof Predator)	{
 						predatorCount ++;
 						predatorAvoidance = predatorAvoidance.plus(dir.unit().mult(Math.pow((getRadius()-dir.size())/getRadius(), 1.0/3)).neg());
 					}
 				}
 			}
 		}
 		
 		if(getTarget() != null)	{
 			waypointAttraction = target.getPosition().minus(getPosition()).truncate(1);
 			//if we reach the waypoint we start moving to the next one
 			if(getPosition().minus(getTarget().getPosition()).size() < getSize()+getTarget().getSize())
 				setTarget(getTarget().getTarget());
 			//otherwise we see if the majority of the flock has updated their target
 			else	{
 				Waypoint mVote = null;
 				double flockCount = 0, totalFlock = 0;
 				for(Waypoint w : flockTargets.keySet())			{
 					if(w != previousTarget && flockTargets.get(w) > flockCount)	{
 						flockCount = flockTargets.get(w);
 						mVote = w;
 					}
 					totalFlock += flockTargets.get(w);
 				}
 				if(mVote != null)	{
 					/* if we are sufficiently close to the waypoint and sufficiently many flock
 					 * members have started towards the next, then we can move to the next one
 					 */
 					if(flockCount/totalFlock > 0.5 && getPosition().minus(getTarget().getPosition()).size() < getTarget().getRadius()*0.5)
 						setTarget(mVote);
 				}
 			}
 		}
 		
 		//take the average weighting
 		if(predatorCount > 0)
 			predatorAvoidance = predatorAvoidance.mult(1.0/predatorCount);
 		if(neighbourhoodCount > 0)	{
 			collisionAvoidance = collisionAvoidance.mult(1.0/neighbourhoodCount);
 			velocityMatching = velocityMatching.mult(1.0/neighbourhoodCount);
 			flockCentering = flockCentering.mult(1.0/neighbourhoodCount);
 		}
 		if(obstacleCount > 0)
 			obstacleAvoidance = obstacleAvoidance.mult(1.0/obstacleCount);
 		//if(waypointCount > 0)	waypointAttraction = waypointAttraction.mult(1.0/waypointCount);
 		
 		//now perform accumulation\
 		/*Vec ret = new Vec(predatorAvoidance);
 		if(ret.size() < 1)
 			ret = ret.plus(collisionAvoidance).plus(flockCentering).mult(0.5);
 		if(ret.size() < 1)
 			ret = ret.plus(velocityMatching);*/
 		Vec ret = predatorAvoidance.mult(predatorAvoidanceWeight)
 						.plus(collisionAvoidance.mult(collisionAvoidanceWeight)
 						.plus(flockCentering.mult(flockCenteringWeight)
 						.plus(velocityMatching.mult(velocityMatchingWeight)
 						.plus(waypointAttraction.mult(waypointAttractionWeight)))))
 						.mult(1.0/(predatorAvoidanceWeight+collisionAvoidanceWeight+flockCenteringWeight+velocityMatchingWeight+waypointAttractionWeight));
 		velocity = velocity.plus(ret.truncate(1)).plus(obstacleAvoidance.mult(10)).truncate(1);
 	}
 	
 	public Object clone()	{
 		return new Prey(this);
 	}
 }
