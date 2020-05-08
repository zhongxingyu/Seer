 package nl.tudelft.otsim.Simulators.LaneSimulator;
 
 import nl.tudelft.otsim.GUI.Main;
 
 /**
  * This class has the common functionality of regular vehicles and temporary
  * lane change vehicles. This is the position on the network and relative to
  * neighboring movables. Common methods are related to position, neighbors and
  * visualization.
  */
 
 public abstract class Movable  {
 	/** Serial number */
 	public final int id;
 
     /** Main model. */
     public Model model;
 
     /** Lane where the movable is at. */
     public Lane lane;
 
     /** Position on the lane. */
     public double x;
 
     /** Speed of the movable [m/s]. */
     public double v;
     
     /** Acceleration of the movable [m/s^2]. */
     public double a;
 
     /** Movable length [m]. */
     public double l;
 
     /** Global coordinate */
     public java.awt.geom.Point2D.Double global;
     
     /** Normalized heading of the vehicle. */
     public java.awt.geom.Point2D.Double heading = new java.awt.geom.Point2D.Double();
     
     private Movable[] neighbors = new Movable[6];
     
     /* Allowed values for the neighbor parameter of getNeighbor */
     /* The Java enum cannot be used in subtract and exclusive or which would have been so nice */ 
 	/** Left neighboring lane; upstream */
     public final static int LEFT_UP = 0;
 	/** Left neighboring lane; downstream */
 	public final static int LEFT_DOWN = 1; 
 	/** Own lane; upstream */
 	public final static int UP = 2; 
 	/** Own lane; downstream */
 	public final static int DOWN = 3;
 	/** Right neighboring lane; upstream */
 	public final static int RIGHT_UP = 4 ;
 	/** Right neighboring lane; downstream */
 	public final static int RIGHT_DOWN = 5;
 
     /** Value for flip parameter of getNeighbor to flip nothing */
     public final static int FLIP_NONE = 0;
     /** Value for flip parameter of getNeighbor to flip left for right */
     public final static int FLIP_LR = 10;
     /** Value for flip parameter of getNeighbor to flip up for down */
     public final static int FLIP_UD = 20;
     /** Value for flip parameter of getNeighbor to flip left for right and up for down */
     public final static int FLIP_DIAGONAL = 30;
     
     /**
      * Retrieve a neighbor of this Movable
      * @param direction Integer; one of the direction values LEFT_UP, LEFT_DOWN, UP, DOWN, RIGHT_UP, RIGHT_DOWN
      * @return Movable; the selected neighbor (which may be null)
      */
     public Movable getNeighbor (int direction) {
     	return neighbors[direction];
     }
     
     /**
      * Flip a direction of a neighbor
      * @param direction Integer; direction that must be flipped
      * @param flip Integer; how must the direction be flipped
      * @return Integer; the flipped direction
      */
     public static int flipDirection (int direction, int flip) {
     	final int maxDirection = 5;
     	switch (flip) {
     	case 0: return direction;
     	case FLIP_LR: return direction = (maxDirection - direction) ^ 1;
     	case FLIP_UD: return direction ^ 1;
     	case FLIP_DIAGONAL: return maxDirection - direction;
     	default: throw new Error("Bad flip value: " + flip);
     	}
     }
     
     /**
      * Return the UP or a DOWN component of a direction
      * @param direction Integer; direction to examine
      * @return Integer; UP if the direction is LEFT_UP, UP, or RIGHT_UP;
      * DOWN if the direction is LEFT_DOWN, DOWN, or RIGHTDOWN
      */
     public static int alignDirection (int direction) {
     	return (direction % 2) + UP;
     }
     
     /**
      * Update a neighbor of this Movable.
      * @param direction Integer; the direction of the neighbor that must be updated
      * @param newNeighbor Movable; the new neighbor in the specified direction (may be null)
      */
     public void setNeighbor (int direction, Movable newNeighbor) {
     	neighbors[direction] = newNeighbor;
     }
     
     /**
      * Return a textual description of a direction.
      * @param direction Integer; direction to describe textually
      * @return String; the text that describes the direction
      */
     public static String directionToString(int direction) {
     	switch (direction) {
     	case UP: return "UP";
     	case DOWN: return "DOWN";
     	case LEFT_UP: return "LEFT_UP";
     	case LEFT_DOWN: return "LEFT_DOWN";
     	case RIGHT_UP: return "RIGHT_UP";
     	case RIGHT_DOWN: return "RIGHT_DOWN";
     	default: return "UNDEFINED DIRECTION";
     	}
     }
 
     /**
      * Return a textual description of a flip.
      * @param flip Integer; the flip to describe textually
      * @return String; the text that describes the flip
      */
     public static String flipToString(int flip) {
     	switch (flip) {
     	case FLIP_NONE: return "NONE";
     	case FLIP_UD: return "UD";
     	case FLIP_LR: return "LR";
     	case FLIP_DIAGONAL: return "DIAGONAL";
     	default: return "UNDEFINED FLIP";
     	}
     }
     
     /** Upstream movable, if any */
     //public Movable up;
 
     /** Downstream movable, if any */
     //public Movable down;
 
     /** Left upstream movable, if any */
     //public Movable leftUp;
 
     /** Left downstream movable, if any */
     //public Movable leftDown;
 
     /** Right upstream movable, if any */
     //public Movable rightUp;
 
     /** Right downstream movable, if any */
     //public Movable rightDown;
 
     /** Marker string for Matlab. */
     public java.lang.String marker;
 
     /** Matlab handle(s), which are simply double values. */
     public double[] handle;
 
     /** Left indicator on. */
     public boolean leftIndicator = false;
 
     /** Right indicator on. */
     public boolean rightIndicator = false;
 
     /** Boolean which is used for pointer book-keeping. */
     protected boolean justExceededLane = false;
     
     /**
      * Constructor that sets the main model.
      * @param model Main model.
      */
     public Movable(Model model) {
     	id = ++model.nextMovableId;
         this.model = model;
     }
 
     /** 
      * Finds neighbors in adjacent lanes or checks for overtaking. This will
      * make sure that the adjacent neighbors are correct.
      */
     public void updateNeighbours() {
     	int[] directions = { LEFT_UP, RIGHT_UP };
     	for (int direction : directions) {
         	Lane neighborLane = LEFT_UP == direction ? lane.left : lane.right;
         	if (null == neighborLane) {	// this is the easy case
         		setNeighbor(direction, null);
         		setNeighbor(flipDirection(direction, FLIP_UD), null);
         	} else {
                 final double xAdj = getAdjacentX(LEFT_UP == direction ? Model.latDirection.LEFT : Model.latDirection.RIGHT);
                 Movable neighborUp = getNeighbor(direction);
         		// Find the closest upstream Movable
 	            if ((null != neighborUp) && (neighborUp.lane.downSplit != neighborLane.downSplit))
 	            	neighborUp = null;	// expensive search is needed
 	            if (null == neighborUp)	// "expensive search" needed
 	            	neighborUp = neighborLane.findVehicle(xAdj, Model.longDirection.UP);
 	            else {		// "inexpensive search" will do
 	            	// Search upstream from the current neighborUp until we have a Movable that is UP w.r.t. this Movable  
 	            	while ((null != neighborUp) && (neighborUp.x + neighborLane.xAdj(neighborUp.lane) >= xAdj))
 	            		neighborUp = neighborUp.getNeighbor(UP);	// we've been overtaken
 	            	// Search downstream for the last vehicle that is NOT ahead of this Movable
 	            	while (null != neighborUp) {
 	            		Movable down = neighborUp.getNeighbor(DOWN);
 	            		if (null == down)
 	            			break;
 	            		if (down.x + neighborLane.xAdj(down.lane) <= xAdj)
 	            			neighborUp = down;	// we have overtaken this Movable
 	            		else
 	            			break;
 	            	}
 	            }
 	            setNeighbor(direction, neighborUp);	
 	            // Find the closest downstream Movable
 	            Movable neighborDown;
 	            if ((null != neighborUp) && (neighborUp.lane.downSplit == neighborLane.downSplit))
 	            	neighborDown = neighborUp.getNeighbor(DOWN);
 	            else {
 	            	neighborDown = neighborLane.findVehicle(xAdj, Model.longDirection.DOWN);
 	            	if (null != neighborDown) {	// Check for perfectly parallel neighbor
 	            		Movable check = neighborDown.getNeighbor(UP);
 	            		if ((null != check) && (neighborUp != check))
 	            			neighborDown = check;	// There was one!
 	            	}
 	            }
 	            setNeighbor(flipDirection(direction, FLIP_UD), neighborDown);
 	    	}
     	}    		
     }
 
     /**
      * Sets this vehicle as adjacent neighbor of own adjacent neighbors. When
      * put on a lane a vehicle may be the adjacent neighbor for many vehicles
      * on adjacent lanes. This method checks if the adjacent neighbors of these
      * vehicles are either the <tt>up</tt> or <tt>down</tt> vehicle and adjusts 
      * that to this vehicle if so.
      */
     public void updateNeighboursInv() {
         // indirect pointers
         /* 
          * Vehicle A is put on the right lane. Then if vehicle B has C as
          * rightUp or vehicle B has no rightUp, A becomes the rightUp of B. This
          * procedure is continued for as long as B or vehicles downstream are in
          * any of these situations. Note that the use of isSameLane() prevents
          * that invalid pointers passing merges or splits are defined.
          * ---------    ---------    ---------
          *        B           B          |  B
          * ---------    ---------    ---------
          *  C  A           A           A |
          * ---------    ---------    ---------
          * 
          * This drawing is for LEFT_DOWN; mirror for the other orientations...
          */
     	final int[] directions = { LEFT_DOWN, RIGHT_DOWN, LEFT_UP, RIGHT_UP };
     	for (int direction : directions)
    		for (Movable vehicleB = getNeighbor(direction); null != vehicleB; vehicleB = vehicleB.getNeighbor(alignDirection(direction)))
     			if (((vehicleB.getNeighbor(flipDirection(direction, FLIP_DIAGONAL)) == getNeighbor(alignDirection(flipDirection(direction, FLIP_UD)))) && (null != getNeighbor(alignDirection(flipDirection(direction, FLIP_UD)))))
    					|| ((null == vehicleB.getNeighbor(flipDirection(direction, FLIP_DIAGONAL))) && (null != vehicleB.lane.right) && vehicleB.lane.right.isSameLane(lane)))
     				vehicleB.setNeighbor(flipDirection(direction, FLIP_DIAGONAL), this);
     			else
     				break;
 
         // one-directional pointers
         /*
          * --------------------------
          *  C   A                    >>
          * -- . . . ----- . . . -----
          *   \      >>   \  B   >>
          *    ------      ------
          * Vehicle A is pasted onto the road. When vehicle B does not have a
          * leftUp or if it is C, A will become the leftUp of B. Search
          * downstream until vehicles are found within the lane. This situation
          * also applies with up/downstream switched.
          */
         pasteAsAdjacentLeader(lane, Model.longDirection.DOWN);
         pasteAsAdjacentLeader(lane, Model.longDirection.UP);
     }
 
     /**
      * Sets the global x and y positions, as implemented by a subclass.
      */
     public abstract void setXY();
     
     /**
      * Returns the global x and y at the lane center.
      * @return 2-element array with x and y at the lane.
      */
     public java.awt.geom.Point2D.Double atLaneXY() {
         return lane.XY(x);
     }
 
     /**
      * Returns the location on the adjacent lane, keeping lane length
      * difference in mind.
      * @param dir Defines direction. Use -1 for left lane, 1 for right lane.
      * @return X location on adjacent lane.
      */
     public double getAdjacentX(Model.latDirection dir) {
         return lane.getAdjacentX(x, dir);
     }
 
     /**
      * Abstract method to translate a vehicle.
      * @param dx Distance to be translated [m].
      */
     public abstract void translate(double dx);
 
     /**
      * Returns the net headway to a given vehicle. This vehicle should be on the
      * same or an adjacent lane, or anywhere up- or downstream of those lanes.
      * @param leader Leading vehicle, not necessarily the 'down' vehicle.
      * @return Net headway with leader [m].
      */
     public double getHeadway(Movable leader) {
         // Ignore leader which is on the other side of a merge but which came
         // from another lane. It should also be only partially past the conflict.
         // The conflict should deal with the situation.
         if ((leader == getNeighbor(DOWN)) && (leader.lane.upMerge != null) && (leader.lane.upMerge != lane.upMerge) &&
                 ((null == leader.lane.upMerge.mergeOrigin) || !leader.lane.upMerge.mergeOrigin.isSameLane(lane)) && 
                 leader.lane.upMerge.xAdj(leader.lane)+leader.x<leader.l) {
             return Double.POSITIVE_INFINITY;
         }
         double s = 0;
         double xAdjTmp;
         if (lane==leader.lane)
             s = leader.x - x; // same lane
         else if (lane==leader.lane.left)
             s = leader.getAdjacentX(Model.latDirection.LEFT) - x; // leader is right
         else if (lane==leader.lane.right)
             s = leader.getAdjacentX(Model.latDirection.RIGHT) - x; // leader is left
         else if ((xAdjTmp=lane.xAdj(leader.lane)) != 0)
             s = leader.x + xAdjTmp - x; // leader is up- or downstream
         else if ((xAdjTmp=lane.xAdj(leader.lane.left)) != 0)
             s = leader.getAdjacentX(Model.latDirection.LEFT) + xAdjTmp - x; // leader is on right lane up- or downstream
         else if (lane.right!=null && (xAdjTmp=lane.right.xAdj(leader.lane)) != 0)
             s = leader.x + xAdjTmp - getAdjacentX(Model.latDirection.RIGHT); // leader is on right lane up- or downstream (no up/down lane)
         else if ((xAdjTmp=lane.xAdj(leader.lane.right)) != 0)
             s = leader.getAdjacentX(Model.latDirection.RIGHT) + xAdjTmp - x; // leader is on left lane up- or downstream
         else if (lane.left!=null && (xAdjTmp=lane.left.xAdj(leader.lane)) != 0)
             s = leader.x + xAdjTmp - getAdjacentX(Model.latDirection.LEFT); // leader is on left lane up- or downstream (no up/down lane)
         else if (this instanceof Vehicle) {
             // leader may actually be a leader of the lane change vehicle
             /*
              * This happens for a neighbor of an lcVehicle as:
              * ------------
              *          A
              * ------------
              *     B
              * ------------
              *     C
              * ------------
              * Vehicle A wants the acceleration B->A so it won't cut B off. The
              * acceleration is calculated by the driver of vehicle B. However, B
              * is a lane change vehicle (of C) and has no driver.
              * "B.getDriver()" returns the driver of vehicle C. That driver then
              * needs a headway between it's vehicle (C) and A. This will not be
              * found and so the headway between B and A will be needed.
              */
             Vehicle veh = (Vehicle) this;
             if (veh.lcVehicle == null) {
                 // give warning as vehicles are not adjacent
                 System.err.println("Headway not found from lanes: "+x+"@"+lane.id+"->"+leader.x+"@"+leader.lane.id+", returning Inf");
                 s = Double.POSITIVE_INFINITY;
             } else
                 s = veh.lcVehicle.getHeadway(leader);
         }
         s = s-leader.l; // gross -> net
         return s;
     }
     
     /**
      * Returns the distance between a vehicle and a RSU.
      * @param rsu RSU.
      * @return Distance [m] between vehicle and RSU.
      */
     public double getDistanceToRSU(RSU rsu) {
         return rsu.x + lane.xAdj(rsu.lane) - x;
     }
 
     /**
      * Deletes a vehicle entirely while taking care of any neighbor reference
      * to the vehicle.
      */
     public void delete() {
         /* When deleting a vehicle, all pointers to it need to be removed in
          * order for the garbage collector to remove the object from memory.
          * Vehicles are referenced from: model, lane, OBU, driver, trajectory,
          * lcVehicle<->vehicle and neighboring vehicles.
          */
         
         // remove from lane and neighbors
         cut();
 
         // remove from various objects
         if (this instanceof Vehicle) {
             Vehicle veh = (Vehicle) this;
             // lcVehicle
             if (veh.lcVehicle != null) {
                 veh.lcVehicle.delete(); // will be removed from memory
                 veh.lcVehicle = null;
             }
             // model
             model.removeVehicle(veh);
             // trajectory
             if (veh.trajectory != null) {
                 veh.trajectory.vehicle = null; // data remains, vehicle does not
                 veh.trajectory = null;
             }
             // OBU
             if (veh.isEquipped()) {
                 veh.OBU.delete(); // should remove pointers set in constructor
                 veh.OBU.vehicle = null;
                 veh.OBU = null; // OBU will be removed from memory
             }
             // delete storages of driver
             veh.driver.accelerations = null;
             veh.driver.antFromLeft = null;
             veh.driver.antFromRight = null;
             veh.driver.antInLane = null;
             // driver
             veh.driver.vehicle = null;
             veh.driver = null; // driver will be removed from memory
         } else if (this instanceof LCVehicle) {
             LCVehicle veh = (LCVehicle) this;
             // model
             model.removeVehicle(veh);
             // vehicle
             veh.vehicle.lcVehicle = null;
             veh.vehicle = null;
         }
     }
 
     /**
      * Cuts a vehicle from a lane. All pointers from the lane and neighbors
      * to this vehicle are updated or removed.
      */
     public void cut() {
         // remove from lane vector
         lane.vehicles.remove(this);
 
         /*
          * All pointers from neighbors need to be updated or removed. Finding
          * all vehicles with pointers to this could be performed in two ways:
          *   1) loop all vehicles in the model
          *   2) start at own neighbors and search
          * For efficiency, the 2nd option is chosen. This can become a bit
          * complex as pointers can be:
          *   1) Asymmetric: vehicles have pointers to each other but not as each
          *      others opposites. This can happen especially for a lane changing
          *      vehicle as the lcVehicle is exactly adjacent. As a result the
          *      vehicles are up- or downstream depending on double precision
          *      evaluation of '<', '<=', '=>' and '>'. This can be different
          *      when evaluating from one to the other or the other way around.
          *      Asymmetric pointers can be found by looking one vehicle up- or
          *      downstream. More is not needed as these pointers only occur for
          *      practically adjacent vehicles.
          *   2) Indirect: vehicle 1 has a pointer to vehicle 2 but vehicle 2
          *      does not have a pointer to vehicle 1. However, the two vehicles
          *      are still indirectly connected. For example:
          *        --------------------
          *         A             G
          *        --------------------
          *          B  C  D  E  F   H
          *        --------------------
          *      where vehicles B-F have G as leftDown but G only has F as
          *      rightUp. This situation is solved by moving upstream from
          *      vehicle F while the leftDown vehicle is G.
          *   3) One-directional: This is a pointer where one vehicle lacks a
          *      certain neighbor that would make it indirect. These situations
          *      are discussed below.
          */
 
         // reset pointers in own lane
         
     	final int[] directions = { UP, DOWN };
     	for (int direction : directions) {
     		Movable vehicleG = getNeighbor(direction);
     		if (null != vehicleG) {
     			Movable reverseLink = vehicleG.getNeighbor(flipDirection(direction, FLIP_UD));
     			if (this == reverseLink)
     				vehicleG.setNeighbor(flipDirection(direction, FLIP_UD), getNeighbor(flipDirection(direction, FLIP_UD)));
     		}
     	}
         /* 
          * Own lane pointers are one-directional for the last vehicle on a 
          * split, or the first vehicle on a merge. Loop all split or merge 
          * lanes, find the nearest vehicle and update pointers.
          */
     	for (int direction : directions) {
     		if (null == getNeighbor(direction)) {
     			java.util.ArrayList<Movable> candidates;
     			if (DOWN == direction) {
     				if (null == lane.downSplit)
     					continue;
     				candidates = findVehiclesDownstreamOfSplit(lane);
     			} else {
     				if (null == lane.upMerge)
     					continue;
     				candidates = findVehiclesUpstreamOfMerge(lane);
     			}
     			// TODO explain how this "finds the nearest vehicle" or fix the description
     			for (Movable candidate : candidates)
     				if (this == candidate.getNeighbor(flipDirection(direction, FLIP_UD)))
     					candidate.setNeighbor(flipDirection(direction, FLIP_UD), getNeighbor(flipDirection(direction, FLIP_UD)));
     		}
     	}
 
         // reset pointers in adjacent lanes
     	final int[] fourDirections = { LEFT_DOWN, LEFT_UP, RIGHT_DOWN, RIGHT_UP };
     	for (int direction : fourDirections) {
     		Movable neighbor = getNeighbor(direction);
     		if ((null != neighbor) && (this == neighbor.getNeighbor(flipDirection(direction, FLIP_LR)))) {
     			//Movable newNeighbor = getNeighbor(alignDirection(direction));
     			//String neighborString = "null";
     			//if (null != newNeighbor)
     			//	neighborString = newNeighbor.toString();
     			//System.out.println("(1)" + neighbor.toString() + " setting neighbor " + directionToString(flipDirection(direction, FLIP_LR)) + " to " + neighborString);
     			neighbor.setNeighbor(flipDirection(direction, FLIP_LR), getNeighbor(alignDirection(direction)));
     			neighbor = neighbor.getNeighbor(alignDirection(direction));
     		}
     		while ((null != neighbor) && (this == neighbor.getNeighbor(flipDirection(direction, FLIP_DIAGONAL)))) {
     			//Movable newNeighbor = getNeighbor(flipDirection(alignDirection(direction), FLIP_UD));
     			//String neighborString = "null";
     			//if (null != newNeighbor)
     			//	neighborString = newNeighbor.toString();
     			//System.out.println("(2)" + neighbor.toString() + " setting neighbor " + directionToString(flipDirection(direction, FLIP_DIAGONAL)) + " to " + neighborString);
     			neighbor.setNeighbor(flipDirection(direction, FLIP_DIAGONAL), getNeighbor(flipDirection(alignDirection(direction), FLIP_UD)));
     			neighbor = neighbor.getNeighbor(alignDirection(direction));
     		}
     	}
 
         // one-directional pointers
         /*
          * --------------------------
          *      A                    >>
          * -- . . . ----- . . . -----
          *   \      >>   \  B   >>
          *    ------      ------
          * Vehicle B has A as leftUp, but A has no rightDown. When A is cut, B
          * (and any downstream) needs to be found by moving downstream from A
          * as long as there are no vehicles and then check vehicles on adjacent
          * lanes.
          */
         cutAsAdjacentLeader(lane, Model.longDirection.DOWN);
         cutAsAdjacentLeader(lane, Model.longDirection.UP);
         
         /*
          * -----------
          *            \    A
          * . . . . . . -----------
          *      C        B        >>
          * -----------------------
          * Vehicle A will be deleted, vehicle C has A as leftDown but vehicle B
          * does not anymore because there is no adjacent lane. This can only
         * happen as vehicles move beyond the end of the lane. Therefore only an
         * upstream search is useful.
          */
         if (x > lane.l) {
             int[] twoDirections = { LEFT_DOWN, RIGHT_DOWN };
         	for (int direction : twoDirections) {
         		Lane searchLane = LEFT_DOWN == direction ? lane.right : lane.left;
         		if (null != searchLane)
         			for (Movable vehicleC = searchLane.findVehicle(searchLane.l, Model.longDirection.UP); 
         					(null != vehicleC) && (this == vehicleC.getNeighbor(direction)); vehicleC = vehicleC.getNeighbor(UP)) {
         				//System.out.println("Clearing " + Movable.directionToString(direction) + " of vehicleC " + vehicleC.toString());
         				vehicleC.setNeighbor(direction, null);        				
         			}
         	}
         }
 
         // check connection consistency (debug)
         if (model.debug)
             model.checkForRemainingPointers(this);
 
         // delete own references
         int[] allDirections = { UP, DOWN, LEFT_UP, LEFT_DOWN, RIGHT_UP, RIGHT_DOWN };
         for (int direction : allDirections)
         	setNeighbor(direction, null);
     }
     
     /**
      * Returns a set of vehicles that is upstream of this, or an upstream merge.
      * In case no vehicle is found upstream of one of the merge lanes, any 
      * further merge is used to continue searching.
      * @param merge Lane to look for vehicles upstream of.
      * @return Set of vehicles that is upstream of this, or an upstream merge.
      */
     protected java.util.ArrayList<Movable> findVehiclesUpstreamOfMerge(Lane merge) {
         java.util.ArrayList<Movable> out = new java.util.ArrayList<Movable>();
         for (Lane j : merge.upMerge.ups) {
         	if (j.marked)
         		continue;
         	j.marked = true;
             Movable d = j.findVehicle(j.l, Model.longDirection.UP);
             if (d != null)
                 out.add(d);
             else if (j.upMerge!=null)
                 out.addAll(findVehiclesUpstreamOfMerge(j));
             j.marked = false;
         }
         return out;
     }
     
     /**
      * Returns a set of vehicles that is downstream of this, or a downstream 
      * split. In case no vehicle is found downstream of one of the split lanes,
      * any further split is used to continue searching.
      * @param split Lane to look for vehicles upstream of.
      * @return Set of vehicles that is downstream of this, or a downstream split.
      */
     protected java.util.ArrayList<Movable> findVehiclesDownstreamOfSplit(Lane split) {
         java.util.ArrayList<Movable> out = new java.util.ArrayList<Movable>();
         for (Lane j : split.downSplit.downs) {
         	if (j.marked)
         		continue;
         	j.marked = true;
             Movable d = j.findVehicle(0, Model.longDirection.DOWN);
             if (d != null)
                 out.add(d);
             else if (j.downSplit!=null)
                 out.addAll(findVehiclesDownstreamOfSplit(j));
             j.marked = false;
         }
         return out;
     }
     
     /**
      * Removes movable as adjacent leader for vehicles on up- or downstream 
      * lanes including merging and splitting lanes.
      * @param k Lane to move up- or downstream from.
      * @param lon Longitudinal direction.
      */
     protected void cutAsAdjacentLeader(Lane k, Model.longDirection lon) {
         if (Model.longDirection.DOWN == lon) {
             for (Lane n : k.downs)
                 cutAsAdjacentLeader0(n, lon);
             if (k.down != null)
                 cutAsAdjacentLeader0(k.down, lon);
         } else {
             for (Lane n : k.ups)
                 cutAsAdjacentLeader0(n, lon);
             if (k.up != null)
                 cutAsAdjacentLeader0(k.up, lon);
         }
     }
     
     /**
      * Removes movable as adjacent leader on adjacent lanes and continues an up-
      * or downstream movement if required.
      * @param k Current lane to look left and right from.
      * @param lon Longitudinal direction.
      */
     protected void cutAsAdjacentLeader0(Lane k, Model.longDirection lon) {
         if (Model.longDirection.DOWN == lon) {	// look downstream
         	int[] directions = { LEFT_UP, RIGHT_UP };
         	for (int direction : directions) {
         		Lane otherLane = LEFT_UP == direction ? k.right : k.left;
         		if (null != otherLane) {
         			Movable m = otherLane.findVehicle(0, lon);
         			if ((null != m) && (this != m.getNeighbor(direction)))
         				m = m.getNeighbor(flipDirection(alignDirection(direction), FLIP_UD));
         			for ( ; (null != m) && (this == m.getNeighbor(direction)); m = m.getNeighbor(flipDirection(alignDirection(direction), FLIP_UD))) {
         				//Movable newNeighbor = getNeighbor(UP);
         				//String neighborString = "null";
         				//if (null != newNeighbor)
         				//	neighborString = newNeighbor.toString();
         				//System.out.println("cutAsAdjacentLeader0: changing " + Movable.directionToString(direction) + " of vehicle " + m.toString() + " to " + neighborString);
         				m.setNeighbor(direction, getNeighbor(UP));
         			}
         		}
         	}
             if (k.vehicles.isEmpty()) {
                 // no vehicles encountered, move on
             	if (k.marked)
             		return;
             	k.marked = true;
                 cutAsAdjacentLeader(k, lon);
                 k.marked = false;
             } // else stop: further vehicles have vehicles at k as neighbor
         } else {	// look upstream
         	int[] directions = { LEFT_DOWN, RIGHT_DOWN };
         	for (int direction : directions) {
         		Lane otherLane = LEFT_DOWN == direction ? k.right : k.left;
         		if (null != otherLane)
         			for (Movable m = otherLane.findVehicle(otherLane.l, lon); (null != m) && (this == m.getNeighbor(direction)); m = m.getNeighbor(UP))
         				m.setNeighbor(direction, getNeighbor(DOWN));
         	}
             if (k.vehicles.isEmpty()) {
                 // no vehicles encountered, move on
             	if (k.marked)
             		return;
             	k.marked = true;
                 cutAsAdjacentLeader(k, lon);
                 k.marked = false;
             } else {
                 // check if there is any vehicle that has not just exceeded its lane
                 int i = 0;
                 while (i < k.vehicles.size()) {
                     if (!k.vehicles.get(i).justExceededLane) {
                         /* 
                          * A vehicle was found that has been there for at least
                          * one time step. This vehicle is the neighbor of any
                          * further upstream vehicles. The search can be stopped.
                          */ 
                         k = null;
                         break;
                     }
                     i++;
                 }
                 if (null != k) {
                     /*
                      * Vehicle(s) were found, but they all just exceeded their 
                      * lane. Search needs to be continued as adjacent vehicles
                      * may still have pointers to this movable which is cut.
                      */
                     cutAsAdjacentLeader(k, lon);
                 }
             }
         }
     }
     
     /**
      * Sets movable as adjacent leader for vehicles on up- or downstream 
      * lanes including merging and splitting lanes.
      * @param k Lane to move up- or downstream from.
      * @param lon Longitudinal direction.
      */
     protected void pasteAsAdjacentLeader(Lane k, Model.longDirection lon) {
         if (Model.longDirection.DOWN == lon) {
             for (Lane n : k.downs)
                 pasteAsAdjacentLeader0(n, lon);
             if (k.down != null)
                 pasteAsAdjacentLeader0(k.down, lon);
         } else {
             for (Lane n : k.ups)
                 pasteAsAdjacentLeader0(n, lon);
             if (k.up != null)
                 pasteAsAdjacentLeader0(k.up, lon);
         }
     }
     
     /**
      * Sets movable as adjacent leader on adjacent lanes and continues an up-
      * or downstream movement if required.
      * @param k Current lane to look left and right from.
      * @param lon Longitudinal direction.
      */
     protected void pasteAsAdjacentLeader0(Lane k, Model.longDirection lon) {
         if (Model.longDirection.DOWN == lon) {	// look downstream
         	int[] directions = { LEFT_UP, RIGHT_UP };
         	for (int direction : directions) {
         		Lane otherLane = LEFT_UP == direction ? k.right : k.left;
         		if (null != otherLane)
         			for (Movable m = otherLane.findVehicle(0, lon); 
         					(null != m) && (m.getNeighbor(direction) == getNeighbor(UP)) && (null != m.getNeighbor(direction)); 
         					m = m.getNeighbor(DOWN))
         				m.setNeighbor(direction, this);        					
         	}
         	if (k.vehicles.isEmpty()) {
             	if (k.marked)
             		return;
             	k.marked = true;
                 pasteAsAdjacentLeader(k, lon); // look downstream
                 k.marked = false;
             } else
                 k = null; // stop; further vehicles have vehicles at k as neighbor
         } else {	// look upstream
         	int[] directions = { LEFT_DOWN, RIGHT_DOWN };
         	for (int direction : directions) {
         		Lane otherLane = LEFT_DOWN == direction ? k.right : k.left;
         		if (null != otherLane)
         			for (Movable m = otherLane.findVehicle(otherLane.l, lon);
         					(null != m) && (m.getNeighbor(direction) == getNeighbor(DOWN)) && (null != m.getNeighbor(direction));
         					m = m.getNeighbor(UP))
         				m.setNeighbor(direction, this);
         	}
             if (k.vehicles.isEmpty()) {
             	if (k.marked)
             		return;
             	k.marked = true;
                 pasteAsAdjacentLeader(k, lon); // look upstream
                 k.marked = false;
             } else
                 k = null; // stop: further vehicles have vehicles at k as neighbor
         }
     }
 
     /**
      * Places a vehicle on a lane, sets new neighbors and sets this vehicle as
      * neighbor of surrounding vehicles.
      * @param atLane Lane where the vehicle needs to be placed at.
      * @param atX Location where the vehicle needs to be placed at.
      */
     public void paste(Lane atLane, double atX) {
         // In case the lane is exceeded, change the lane to search on. This
         // could occur when searching for neighbors when ending a lane change
         // within the same time step a lane is exceeded.
         if ((atX > atLane.l) && (atLane.down != null)) {
             paste(atLane.down, atX - atLane.l);
             return;
         }
         // find up/down neighbors
         setNeighbor(UP, atLane.findVehicle(atX, Model.longDirection.UP));
         if (null != getNeighbor(UP)) {
         	setNeighbor(DOWN, getNeighbor(UP).getNeighbor(DOWN));	// put this Movable in between
         	if ((null == getNeighbor(UP).getNeighbor(DOWN)) && (getNeighbor(UP).lane.downSplit != atLane.downSplit))
         		setNeighbor(DOWN, atLane.findVehicle(atX, Model.longDirection.DOWN)); // just passed split, so up has no down (as this was cut)
         	else
         		getNeighbor(UP).setNeighbor(DOWN, this);
         } else
         	setNeighbor(DOWN, atLane.findVehicle(atX, Model.longDirection.DOWN));
         if ((null != getNeighbor(DOWN)) && (getNeighbor(DOWN).lane.upMerge == atLane.upMerge))
         	getNeighbor(DOWN).setNeighbor(UP, this);	// same lane, down has this as up
         // set properties
         lane = atLane;
         x = atX;
         // Set pointers to this of vehicles at other side of split or merge.
         if ((null != lane.upMerge) && (null == getNeighbor(UP))) {
             java.util.ArrayList<Movable> ups = findVehiclesUpstreamOfMerge(lane);
             for (Movable d : ups)
             	if (((null == d.getNeighbor(DOWN)) || (d.getNeighbor(DOWN) == getNeighbor(DOWN))) 
             			&& ((null == d.lane.downSplit) || (d.lane.downSplit == lane.downSplit)))
             		d.setNeighbor(DOWN, this);
         }
         if ((lane.downSplit != null) && (null == getNeighbor(DOWN))) {
             java.util.ArrayList<Movable> downs = findVehiclesDownstreamOfSplit(lane);
             for (Movable d : downs)
             	if (((null == d.getNeighbor(UP)) || (d.getNeighbor(UP) == getNeighbor(UP)))
             			&& ((null == d.lane.upMerge) || (d.lane.upMerge == lane.upMerge)))
             		d.setNeighbor(UP, this);
         }
         // add to lane vector
         atLane.vehicles.add(this);
         // set adjacent neighbors
         updateNeighbours();
         // set adjacent neighbors' new neighbors (this vehicle)
         updateNeighboursInv();
     }
     
     /**
      * Returns the driver of any movable.
      * @return Driver of the movable.
      */
     public abstract Driver getDriver();
 
     /**
      * Retrieve the {@link Lane} of this Movable.
      * @return {@link Lane}; the current Lane of this Movable
      */
     public Lane getLane_r() {
     	return lane;
     }
     
     /**
      * Retrieve the longitudinal position in the current {@link Lane}.
      * @return String; the longitudinal position in the current {@link Lane}
      */
     public String getLongitudinalPositionInLane_r() {
     	return String.format(Main.locale, "%.2fm (of %.2fm)", x, lane.l);
     }
     
     @Override
 	public String toString() {
     	String location = "null";
     	if (null != global)
     		location = String.format(Main.locale, "%.3f,%.3f", global.x, global.y);
     	String comment = "";
     	if (this instanceof LCVehicle)
     		comment = " owner is " + ((LCVehicle) this).vehicle.toString();
     	return String.format (Main.locale, "%d at (%s), (%s%s)", id, location, getClass().getName(), comment);
     }
     
     private String printNeighbor(String caption, int direction) {
     	Movable neighbor = getNeighbor(direction);
     	if (null == neighbor)
     		return "";
     	return " " + caption + " " + neighbor.toString();
     }
     
     /**
      * Show the connectivity between Movables (for debugging).
      * @return String
      */
     public String linkedNeighbors() {
     	return printNeighbor("up", UP) + printNeighbor("down", DOWN) 
     			+ printNeighbor("leftUp", LEFT_UP) + printNeighbor("rightUp", RIGHT_UP)
     			+ printNeighbor("leftDown", LEFT_DOWN) + printNeighbor("rightDown", RIGHT_DOWN);
     }
     
     /**
      * Retrieve the leader of this Movable.
      * @return Movable; the leader of this Movable (may be null)
      */
     public Movable getLeader_r () {
     	return getNeighbor(DOWN);
     }
     
     /**
      * Retrieve the left leader of this Movable.
      * @return Movable; the left leader of this Movable (may be null)
      */
     public Movable getLeaderLeft_r () {
     	return getNeighbor(LEFT_DOWN);
     }
 
     /**
      * Retrieve the right leader of this Movable.
      * @return Movable; the right leader of this Movable (may be null)
      */
     public Movable getLeaderRight_r () {
     	return getNeighbor(RIGHT_DOWN);
     }
 
     /**
      * Retrieve the follower of this Movable.
      * @return Movable; the follower of this Movable (may be null)
      */
     public Movable getFollower_r () {
     	return getNeighbor(UP);
     }
     
     /**
      * Retrieve the left follower of this Movable.
      * @return Movable; the left follower of this Movable (may be null)
      */
     public Movable getFollowerLeft_r () {
     	return getNeighbor(LEFT_UP);
     }
 
     /**
      * Retrieve the right follower of this Movable.
      * @return Movable; the right follower of this Movable (may be null)
      */
     public Movable getFollowerRight_r () {
     	return getNeighbor(RIGHT_UP);
     }
 
 }
