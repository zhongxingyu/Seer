 /* Copyright Sebastian Rockel 2010
  * Basic Pioneer 2DX class
  */
 
 package robot;
 
 import javaclient3.PlayerClient;
 import javaclient3.PlayerException;
 import javaclient3.Position2DInterface;
 import javaclient3.structures.PlayerConstants;
 
 // This class represents a minimal or standard coniguration
 // of a Pioneer 2DX robot at TAMS laboratory at University Hamburg
 // informatics center
 // It can be instantiated or inherited to add other devices.
 public class Pioneer2dx {
 	// Required to every Pioneer2dx
 	public PlayerClient playerclient = null;
 	Position2DInterface posi  = null;
 	
 	// To be implemented in subclass when needed
 	LaserUrg 			laser = null;
 	Sonar				sonar = null;
 	
 	int id = -1;
 	double speed = -1.0;
 	double turnrate = -1.0;
 	enum StateType {
 		LWALL_FOLLOWING,
 		RWALL_FOLLOWING,
 	    COLLISION_AVOIDANCE,
 	    WALL_SEARCHING
 	}
 	StateType currentState;
 	enum viewDirectType {
 	   LEFT,
 	   RIGHT,
 	   FRONT,
 	   BACK,
 	   LEFTFRONT,
 	   RIGHTFRONT,
 	   LEFTREAR,
 	   RIGHTREAR,
 	   ALL
 	}
 	// Parameters TODO shall be in own config file or superclass
 	final double VEL       = 0.3;///< Normal_advance_speed in meters per sec.
 	final double TURN_RATE = 40; ///< Max wall following turnrate in deg per sec.
 	                             /// Low values: Smoother trajectory but more
 	                             /// restricted
 	final double STOP_ROT  = 30; ///< Stop rotation speed.
 	                             /// Low values increase maneuverability in narrow
 	                             /// edges, high values let the robot sometimes be
 	                             /// stuck.
 	final double TRACK_ROT =  40; /// Goal tracking rotation speed in degrees per sec.
 	final double YAW_TOLERANCE = 20;///< Yaw tolerance for ball tracking in deg
 	final double DIST_TOLERANCE = 0.5;///< Distance tolerance before stopping in meters
 	final double WALLFOLLOWDIST = 0.5; ///< Preferred wall following distance in meters.
 	final double STOP_WALLFOLLOWDIST = 0.2; ///< Stop distance in meters.
 	final double WALLLOSTDIST  = 1.5; ///< Wall attractor in meters before loosing walls.
 	final double SHAPE_DIST = 0.3; ///< Min Radius from sensor for robot shape.
 	// Laser ranger
 	final double LMAXANGLE = 240; ///< Laser max angle in degree
 	final int BEAMCOUNT = 2; ///< Number of laser beams taken for one average distance measurement
 	final double DEGPROBEAM   = 0.3515625; ///< 360./1024. in degree per laser beam
 	final double LPMAX     = 5.0;  ///< max laser range in meters
 	final double COS45     = 0.83867056795; ///< Cos(33);
 	final double INV_COS45 = 1.19236329284; ///< 1/COS45
 	final double DIAGOFFSET  = 0.1;  ///< Laser to sonar diagonal offset in meters.
 	final double HORZOFFSET  = 0.15; ///< Laser to sonar horizontal offset in meters.
 	final double MOUNTOFFSET = 0.1;  ///< Sonar vertical offset at back for laptop mount.
 	final int LMIN  = 175;/**< LEFT min angle.       */ final int LMAX  = 240; ///< LEFT max angle.
 	final int LFMIN = 140;/**< LEFTFRONT min angle.  */ final int LFMAX = 175; ///< LEFTFRONT max angle.
 	final int FMIN  = 100;/**< FRONT min angle.      */ final int FMAX  = 140; ///< FRONT max angle.
 	final int RFMIN = 65; /**< RIGHTFRONT min angle. */ final int RFMAX = 100; ///< RIGHTFRONT max angle.
 	final int RMIN  = 0;  /**< RIGHT min angle.      */ final int RMAX  = 65;  ///< RIGHT max angle.
 	
 	//Debugging
 //	final boolean DEBUG_LASER = true;
 //	final boolean DEBUG_STATE = true;
 //	final boolean DEBUG_SONAR = true;
 //	final boolean DEBUG_DIST  = true;
 	//final boolean DEBUG_POSITION = true;
 	
 	final boolean DEBUG_LASER = false;
 	final boolean DEBUG_STATE = false;
 	final boolean DEBUG_SONAR = false;
 	final boolean DEBUG_DIST  = false;
 	final boolean DEBUG_POSITION = false;
 	
 	// Constructor: do all playerclient communication setup here
 	public Pioneer2dx (String name, int port, int id) {
 		try {
 			// Connect to the Player server and request access to Position
 			this.playerclient  = new PlayerClient (name, port);
			this.playerclient.runThreaded (-1, -1);
 			this.id = id;
 			System.out.println("Running playerclient with id: "
 					+ this.id
 					+ " and name: "
 					+ this.playerclient.getName());
 			this.posi = this.playerclient.requestInterfacePosition2D (0, PlayerConstants.PLAYER_OPEN_MODE);
 		} catch (PlayerException e) {
 			System.err.println ("Pioneer2dx: > Error connecting to Player: ");
 			System.err.println ("    [ " + e.toString() + " ]");
 			System.exit (1);
 		}
 		// Has to be called in object constructor!
 		// Otherwise program will block forever
 		//playerclient.runThreaded (-1, -1);
 	}
 		
 	/// Returns the minimum distance of the given arc.
 	/// Algorithm calculates the average of BEAMCOUNT beams
 	/// to define a minimum value per degree.
 	/// @param Range of angle (degrees)
 	/// @return Minimum distance in range
 	final double getDistanceLas ( int minAngle, int maxAngle ) {
 		double minDist         = LPMAX; ///< Min distance in the arc.
 	    double distCurr        = LPMAX; ///< Current distance of a laser beam
 
 	    if (this.laser != null) {
 	    	if ( !(minAngle<0 || maxAngle<0 || minAngle>=maxAngle || minAngle>=LMAXANGLE || maxAngle>LMAXANGLE ) ) {
 
 	    		final int minBIndex = (int)(minAngle/DEGPROBEAM); ///< Beam index of min deg.
 	    		final int maxBIndex = (int)(maxAngle/DEGPROBEAM); ///< Beam index of max deg.
 	    		double sumDist     = 0.; ///< Sum of BEAMCOUNT beam's distance.
 	    		double averageDist = LPMAX; ///< Average of BEAMCOUNT beam's distance.
 
 	    		float[] laserValues = this.laser.getRanges();
 
 	    		for (int beamIndex=minBIndex; beamIndex<maxBIndex; beamIndex++) {
 	    			//distCurr = lp->GetRange(beamIndex);
 	    			distCurr = laserValues[beamIndex];
 
 	    			//distCurr<0.02 ? sumDist+=LPMAX : sumDist+=distCurr;
 	    			if (distCurr < 0.02) {
 	    				sumDist += LPMAX;
 	    			} else {
 	    				sumDist += distCurr;
 	    			}
 	    			//sumDist += lp->GetRange(beamIndex);
 	    			if((beamIndex-minBIndex) % BEAMCOUNT == 1) { ///< On each BEAMCOUNT's beam..
 	    				averageDist = sumDist/BEAMCOUNT; ///< Calculate the average distance.
 	    				sumDist = 0.; ///< Reset sum of beam average distance
 	    				// Calculate the minimum distance for the arc
 	    				//averageDist<minDist ? minDist=averageDist : minDist;
 	    				if (averageDist < minDist) {
 	    					minDist = averageDist;
 	    				}
 	    			}
 	    			if ( DEBUG_LASER ) {
 	    				System.out.println("beamInd: " + beamIndex
 	    						+ "\tsumDist: " + sumDist
 	    						+ "\taveDist: " + averageDist
 	    						+ "\tminDist: " + minDist);
 	    			}
 	    		}
 	    	}
 	    }
 		return minDist;
 	}
 
 	/// Returns the minimum distance of the given view direction.
 	/// Robot shape shall be considered here by weighted SHAPE_DIST.
 	/// Derived arcs, sonars and weights from graphic "PioneerShape.fig".
 	/// NOTE: ALL might be slow due recursion, use it only for debugging!
 	/// @param Robot view direction
 	/// @return Minimum distance of requested view Direction
 	final double getDistance( viewDirectType viewDirection )
 	{		
 		float[] sonarValues = new float[16];
 
 		if (this.sonar != null) {
 			sonarValues = this.sonar.getRanges();
 		} else {
 			for (int i=0; i<16; i++) {
 				sonarValues[i] = (float)this.LPMAX;
 			}
 		}
 		// Scan safety areas for walls
 		switch (viewDirection) {
 		case LEFT      : return Math.min(getDistanceLas(LMIN,  LMAX) -HORZOFFSET-SHAPE_DIST, Math.min(sonarValues[0], sonarValues[15])-SHAPE_DIST);
 		case RIGHT     : return Math.min(getDistanceLas(RMIN,  RMAX) -HORZOFFSET-SHAPE_DIST, Math.min(sonarValues[7], sonarValues[8]) -SHAPE_DIST);
 		case FRONT     : return Math.min(getDistanceLas(FMIN,  FMAX)            -SHAPE_DIST, Math.min(sonarValues[3], sonarValues[4]) -SHAPE_DIST);
 		case RIGHTFRONT: return Math.min(getDistanceLas(RFMIN, RFMAX)-DIAGOFFSET-SHAPE_DIST, Math.min(sonarValues[5], sonarValues[6]) -SHAPE_DIST);
 		case LEFTFRONT : return Math.min(getDistanceLas(LFMIN, LFMAX)-DIAGOFFSET-SHAPE_DIST, Math.min(sonarValues[1], sonarValues[2]) -SHAPE_DIST);
 		case BACK      : return Math.min(sonarValues[11], sonarValues[12])-MOUNTOFFSET-SHAPE_DIST; // Sorry, only sonar at rear
 		case LEFTREAR  : return Math.min(sonarValues[13], sonarValues[14])-MOUNTOFFSET-SHAPE_DIST; // Sorry, only sonar at rear
 		case RIGHTREAR : return Math.min(sonarValues[9] , sonarValues[10])-MOUNTOFFSET-SHAPE_DIST; // Sorry, only sonar at rear
 		case ALL       : return Math.min(getDistance(viewDirectType.LEFT),
 				Math.min(getDistance(viewDirectType.RIGHT),
 						Math.min(getDistance(viewDirectType.FRONT),
 								Math.min(getDistance(viewDirectType.BACK),
 										Math.min(getDistance(viewDirectType.RIGHTFRONT),
 												Math.min(getDistance(viewDirectType.LEFTFRONT),
 														Math.min(getDistance(viewDirectType.LEFTREAR), getDistance(viewDirectType.RIGHTREAR) )))))));
 		default: return 0.; // Should be recognized if happens
 		}
 	}
 	
 	/// Calculates the turnrate from range measurement and minimum wall follow
 	/// distance.
 	/// @param Current state of the robot.
 	/// @returns Turnrate to follow wall.
 	final double wallfollow ()
 	{
 		double DistLFov  = 0;
 		double DistL     = 0;
 		double DistLRear = 0;
 		//double DistFront = 0;
 
 		// As long global goal is WF set it by default
 		// Will potentially be overridden by higher prior behaviours
 		this.currentState = StateType.LWALL_FOLLOWING;
 
 		DistLFov  = getDistance(viewDirectType.LEFTFRONT);
 		
 		// do simple (left) wall following
 		//do naiv calculus for turnrate; weight dist vector
 		this.turnrate = Math.atan( (COS45*DistLFov - WALLFOLLOWDIST ) * 4 );
 			
 		// Normalize turnrate
 		if (this.turnrate > Math.toRadians(TURN_RATE)) {
 			this.turnrate = Math.toRadians(TURN_RATE);
 		} else if (this.turnrate < -Math.toRadians(TURN_RATE)) {
 			this.turnrate = -Math.toRadians(TURN_RATE);
 		}
 
 		// TODO implement wall searching behavior
 		DistL     = getDistance(viewDirectType.LEFT);
 		DistLRear = getDistance(viewDirectType.LEFTREAR);
 		// Go straight if no wall is in distance (front, left and left front)
 		if (DistLFov  >= WALLLOSTDIST  &&
 			DistL     >= WALLLOSTDIST  &&
 			DistLRear >= WALLLOSTDIST     )
 		{
 			this.turnrate = 0.;
 			this.currentState = StateType.WALL_SEARCHING;
 		}
 
 		return this.turnrate;
 	}
 
 	// Biased by left wall following
 	final double collisionAvoid ()
 	{
 		// Scan FOV for Walls
 		double distLeftFront  = getDistance(viewDirectType.LEFTFRONT);
 		double distFront      = getDistance(viewDirectType.FRONT);
 		double distRightFront = getDistance(viewDirectType.RIGHTFRONT);
 
 		double distFrontRight = (distFront + distRightFront) / 2;
 		double distFrontLeft  = (distFront + distLeftFront)  / 2;
 
 		if ((distFrontLeft  < STOP_WALLFOLLOWDIST) ||
 				(distFrontRight < STOP_WALLFOLLOWDIST)   )
 		{
 			this.currentState = StateType.COLLISION_AVOIDANCE;
 			// Turn right as long we want left wall following
 			return -Math.toRadians(STOP_ROT);
 		} else {
 			return this.turnrate;
 		}
 	}
 
 	/// @todo Code review
 	final double calcspeed ()
 	{
 		double tmpMinDistFront = Math.min(getDistance(viewDirectType.LEFTFRONT), Math.min(getDistance(viewDirectType.FRONT), getDistance(viewDirectType.RIGHTFRONT)));
 		double tmpMinDistBack  = Math.min(getDistance(viewDirectType.LEFTREAR), Math.min(getDistance(viewDirectType.BACK), getDistance(viewDirectType.RIGHTREAR)));
 		double speed = VEL;
 
 		if (tmpMinDistFront < WALLFOLLOWDIST) {
 			speed = VEL * (tmpMinDistFront/WALLFOLLOWDIST);
 
 			// Do not turn back if there is a wall!
 			if (tmpMinDistFront<0 && tmpMinDistBack<0)
 				//tmpMinDistBack<tmpMinDistFront ? speed=(VEL*tmpMinDistFront)/(tmpMinDistFront+tmpMinDistBack) : speed;
 				if (tmpMinDistBack < tmpMinDistFront){
 					speed = (VEL*tmpMinDistFront)/(tmpMinDistFront+tmpMinDistBack);
 				}
 				//speed=(VEL*(tmpMinDistBack-tmpMinDistFront))/SHAPE_DIST;
 				//tmpMinDistBack<tmpMinDistFront ? speed=(VEL*(tmpMinDistFront-tmpMinDistBack))/WALLFOLLOWDIST : speed;
 		}
 		return speed;
 	}
 
 	/// Checks if turning the robot is not causing collisions.
 	/// Implements more or less a rotation policy which decides depending on
 	/// obstacles at the 4 robot edge surounding spots
 	/// To not interfere to heavy to overall behaviour turnrate is only inverted (or
 	/// set to zero)
 	/// @param Turnrate
 	/// @todo Code review
 	final double checkrotate ()
 	{
 		double saveTurnrate = 0.;
 		
 		if (this.turnrate < 0) { // Right turn
 			if (getDistance(viewDirectType.LEFTREAR) < 0) {
 				saveTurnrate = 0;
 			}
 			if (getDistance(viewDirectType.RIGHT) < 0) {
 				saveTurnrate = 0;
 			}
 		} else if (this.turnrate > 0){ // Left turn
 			if (getDistance(viewDirectType.RIGHTREAR) < 0) {
 				saveTurnrate = 0;
 			}
 			if (getDistance(viewDirectType.LEFT) < 0) {
 				saveTurnrate = 0;
 			}
 		}
 		return saveTurnrate;
 	}
 
 	final void readSensors () {
 		///< This blocks until new data comes; 10Hz by default
 		this.playerclient.readAll();
 		if (this.sonar != null) { this.sonar.updateRanges(); };
 		if (this.laser != null) { this.laser.updateRanges(); };
 	}
 	final void plan () {
 		double tmp_turnrate = 0.;
 		
 //		if (DEBUG_SONAR && this.sonar != null){
 //			float[] sonarValues = this.sonar.getRanges();	
 //			int 	sonarCount  = this.sonar.getCount();
 //
 //			System.out.println();
 //			for(int i=0; i< sonarCount; i++)
 //				System.out.println("Sonar " + i + ": " + sonarValues[i]);
 //		}
 
 		// (Left) Wall following
 		this.turnrate = wallfollow();
 		// Collision avoidance overrides other turnrate if neccessary!
 		// May change this.turnrate or this.currentState
 		this.turnrate = collisionAvoid();
 		
 		// Set speed dependend on the wall distance
 		this.speed = calcspeed();
 
 		// Check if rotating is safe
 		// tune turnrate controlling here
 		tmp_turnrate = checkrotate();
 
 		// Fusion of the vectors makes a smoother trajectory
 //		this.turnrate = (tmp_turnrate + this.turnrate) / 2;
 		double weight = 0.5;
 		this.turnrate = weight*tmp_turnrate + (1-weight)*this.turnrate;
 		if (DEBUG_STATE) {
 			System.out.printf("turnrate/speed/state:\t%5.2f\t%5.2f\t%s\n", this.turnrate, this.speed, this.currentState.toString());
 		}
 		if (DEBUG_DIST) {
 			if (this.laser != null) {
 				System.out.print("Laser (l/lf/f/rf/r/rb/b/lb):\t");
 				System.out.print(getDistanceLas(LMIN,  LMAX)-HORZOFFSET);	System.out.print("\t");
 				System.out.print(getDistanceLas(LFMIN, LFMAX)-DIAGOFFSET);	System.out.print("\t");
 				System.out.print(getDistanceLas(FMIN,  FMAX));				System.out.print("\t");
 				System.out.print(getDistanceLas(RFMIN, RFMAX)-DIAGOFFSET);	System.out.print("\t");
 				System.out.print(getDistanceLas(RMIN,  RMAX) -HORZOFFSET);
 				System.out.println("\t" + "XXX" + "\t" + "XXX" + "\t" + "XXX");
 			} else {
 				System.out.println("No laser available!");
 			}
 
 			if (this.sonar != null) {
 				float[] sonarValues = this.sonar.getRanges();		
 				System.out.print("Sonar (l/lf/f/rf/r/rb/b/lb):\t");
 				System.out.print(Math.min(sonarValues[15],sonarValues[0]));	System.out.print("\t");
 				System.out.print(Math.min(sonarValues[1], sonarValues[2]));   System.out.print("\t");
 				System.out.print(Math.min(sonarValues[3], sonarValues[4]));   System.out.print("\t");
 				System.out.print(Math.min(sonarValues[5], sonarValues[6]));   System.out.print("\t");
 				System.out.print(Math.min(sonarValues[7], sonarValues[8]));   System.out.print("\t");
 				System.out.print(Math.min(sonarValues[9], sonarValues[10])-MOUNTOFFSET); System.out.print("\t");
 				System.out.print(Math.min(sonarValues[11],sonarValues[12])-MOUNTOFFSET); System.out.print("\t");
 				System.out.println(Math.min(sonarValues[13],sonarValues[14])-MOUNTOFFSET);
 			} else {
 				System.out.println("No sonar available!");
 			}
 			
 			System.out.print("Shape (l/lf/f/rf/r/rb/b/lb):\t");
 			System.out.print(getDistance(viewDirectType.LEFT)); System.out.print("\t");
 			System.out.print(getDistance(viewDirectType.LEFTFRONT));  System.out.print("\t");
 			System.out.print(getDistance(viewDirectType.FRONT));      System.out.print("\t");
 			System.out.print(getDistance(viewDirectType.RIGHTFRONT)); System.out.print("\t");
 			System.out.print(getDistance(viewDirectType.RIGHT));      System.out.print("\t");
 			System.out.print(getDistance(viewDirectType.RIGHTREAR));  System.out.print("\t");
 			System.out.print(getDistance(viewDirectType.BACK));       System.out.print("\t");
 			System.out.println(getDistance(viewDirectType.LEFTREAR));
 		}
 		if (DEBUG_POSITION) {
 			System.out.print(posi.getData().getPos().getPx());	System.out.print("\t");
 			System.out.print(posi.getData().getPos().getPy());	System.out.print("\t");
 			System.out.println(java.lang.Math.toDegrees(posi.getData().getPos().getPa()));
 		}
 	}
 
 /// Command the motors
 	final void execute() {
 		this.posi.setSpeed(speed, turnrate);
 	}
 	public void update() {
 		readSensors();
 		plan();
 		execute();
 	}
 }
