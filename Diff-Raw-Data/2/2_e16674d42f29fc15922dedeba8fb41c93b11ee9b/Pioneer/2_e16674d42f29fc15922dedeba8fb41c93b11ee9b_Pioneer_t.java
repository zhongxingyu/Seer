 /**
  * Copyright Sebastian Rockel 2010
  * Basic Pioneer 2DX class
  */
 package robot;
 
 import robot.external.IPioneer;
 import data.Position;
 import device.Device;
 
 /**
  * This class represents a minimal or standard coniguration
  * of a Pioneer 2DX robot at TAMS laboratory at University Hamburg
  * informatics center
  * It can be instantiated or inherited to add other devices.
  * @author sebastian
  *
  */
 public class Pioneer extends Robot implements IPioneer
 {	
     StateType currentState = StateType.SET_SPEED;
     
     /** Watchdog timer for stuck checking */
     boolean timerIsOccured = false;
 
     Position lastPosition;
     int stuckCounter = -1;
 
 
     /**
      * @deprecated Use @see #Pioneer(Device[]) instead.
      * Creates a Pioneer robot object.
      * @param roboDevices The robot devices this robot can access.
      */
     public Pioneer (Device roboDevices) {
 		super(roboDevices);
 	}
     /**
      * Creates a Pioneer robot.
      * @param roboDevList The device list provided to this robot.
      */
     public Pioneer (Device[] roboDevList)
     {
         super(roboDevList);
     }
 	
 	/**
 	 * Checks the robot's current state and performs appropriate actions.
 	 * Implements a subsumption architecture for robot behaviors.
 	 */
     @Override protected void update()
 	{
 	    debugSensorData();
 	    
 	    StateType curState = getCurrentState();
 	    
 	    switch (curState)
 	    {
             case WALL_SEARCHING :
     
             case LWALL_FOLLOWING :
     	  
     	    case COLLISION_AVOIDANCE :
     	    {
                 setTurnrate( getLeftWallfollow( MAXTURNRATE ) );
                 setTurnrate( getCollisionAvoid( getTurnrate() ) );
                 setTurnrate( getEscapeWhenStuck( getTurnrate() ) );
                 setTurnrate( getSafeTurnrate( getTurnrate() ) );
                 setSpeed( getSafeSpeed( MAXSPEED ) );
                
                 commandMotors( getSpeed(), getTurnrate() );
     	        break;
     	    }
     	    case SET_SPEED :
     	    {
     	        double safeSpeed = getSafeSpeed( getSpeed() );
     	        double safeTurnrate = getSafeTurnrate( getTurnrate() );
 
     	        commandMotors( safeSpeed, safeTurnrate );
     	        break;
     	    }
     	    case STUCK_ESCAPING :
     	    {
     	        setTurnrate(-Math.toRadians(MAXTURNRATE));
                 setSpeed( getSafeSpeed( -MAXSPEED ) );
                 setTurnrate( getSafeTurnrate( getTurnrate() ) );
                
                 commandMotors( getSpeed(), getTurnrate() );
                 
                 if (getSafeSpeed( MAXSPEED) >= MAXSPEED*0.5)
                     setCurrentState(StateType.LWALL_FOLLOWING);
                 
     	        break;
     	    }
     	    case STOPPED :
     	        
     	    default :
     	    {
     	        updateStop();
     	        break;
     	    }
 	    }
 	}
     // TODO check and debug
     double getEscapeWhenStuck(double newTurnrate)
     {        
         if (stuckCounter <= 0)
         {
             /** Read odometry values */
             lastPosition = getPosi().getPosition();
         }
         else
         {
             if (stuckCounter >= 20)
             {
                 stuckCounter = -1;
 //                if (lastPosition.distanceTo(getPosi().getPosition()) < 0.1 )
                 if (lastPosition.isNearTo(getPosi().getPosition(), 0.1, 0.17) == true)
                 {
                     timerIsOccured = true;
                 }
             }
         }
         if (timerIsOccured == true)
         {
             timerIsOccured = false;
             setCurrentState(StateType.STUCK_ESCAPING);
         }
         
         stuckCounter += 1;
         
         return newTurnrate;
     }
     /**
      * Sets the motor speed and syncs the underlying positioning device.
      * @param newSpeed The new speed to command.
      * @param newTurnrate The new turnrate to command.
      */
     void commandMotors(double newSpeed, double newTurnrate)
     {
         updateSpeed( newSpeed );
         updateTurnrate( newTurnrate );
         updatePosi();
     }
 	/**
 	 * Updates the motor speed immediately.
 	 */
 	void updatePosi()
 	{
 	    if (getPosi() != null)
 	    {
 	        try
 	        {
 	            getPosi().syncSpeed();
 	        } 
 	        catch (Exception e)
 	        {
 	            String log = "Error updating position device "+this;
 	            logger.severe(log);
 //	            throw new IllegalStateException(log);
 	        }
 	    }
 	}
 	/**
 	 * Stops the motors immediately if not already stopped.
 	 */
 	void updateStop()
 	{
 	    if (getCurrentState() != StateType.STOPPED) {
 	        stop();
 	    }
 	}
 	/**
 	 * Stops the robot motion immediately.
 	 */
 	public void stop()
 	{
 //	    if (getCurrentState().equals(StateType.STOPPED) == false)
 //	    {
 	        setCurrentState(StateType.STOPPED);
 
 	        if (getPlanner() != null)
 	            getPlanner().stop();
 
 //	        if (getSimu() == null)
 //	            if (getPosi() != null)
 //	                getPosi().disableMotor();
 
 	        setTurnrate(0.0);
 	        setSpeed(0.0);
 
 	        commandMotors( getSpeed(), getTurnrate());
 //	    }
 	}
 
 	/**
 	 * Tries to set the speed given.
 	 * The actual speed might be lower due to obstacles.
 	 * @param saveSpeed The speed to set.
 	 */
 	void updateSpeed(double saveSpeed)
 	{
 	    if (getPosi() != null)
 	    {
 	        if (Math.abs(saveSpeed) > MINSPEED)
 	        {
 	            getPosi().setSpeed(saveSpeed);
 	        }
 	        else
 	        {
 	            getPosi().setSpeed(0.0);
 	        }
 	    }
 	    
 	}
 	/**
 	 * Tries to set the turnrate given.
 	 * The actual turnrate might be lower due to obstacles.
 	 * @param saveTurnrate The turnrate to set.
 	 */
 	void updateTurnrate(double saveTurnrate)
 	{
 	    if (getPosi() != null)
 	    {
 	        if (Math.abs(saveTurnrate) > MINTURNRATE)
 	        {
 	            getPosi().setTurnrate(saveTurnrate);
 	        }
 	        else
 	        {
 	            getPosi().setTurnrate(0.0);
 	        }
 	    }
 	}
 	/**
 	 * Returns the minimum distance of the given arc.
 	 * Algorithm calculates the average of BEAMCOUNT beams
 	 * to define a minimum value per degree.
 	 * @param minAngle Minimum angle to check (degree).
 	 * @param maxAngle Maximum angle to check (degree).
 	 * @return Minimum distance in range.
 	 */
 	final double getMinLasRange ( int minAngle, int maxAngle )
 	{
 		double minDist  = LPMAX; /** Minimal distance in the arc. */
 		double distCurr = LPMAX; /** Current distance of a ranger beam. */
 
 		if (getLaser() != null)
 		{
 			if ( !(minAngle<0 || maxAngle<0 || minAngle>=maxAngle || minAngle>=LMAXANGLE || maxAngle>LMAXANGLE ) )
 			{
 				final int minBIndex = (int)(minAngle/DEGPROBEAM); /** Beam index of min deg. */
 				final int maxBIndex = (int)(maxAngle/DEGPROBEAM); /** Beam index of max deg. */
 				double sumDist = 0.; /** Sum of BEAMCOUNT beam's distance. */
 				double averageDist = LPMAX; /** Average of BEAMCOUNT beam's distance. */
 
 				/** Read dynamic ranger data */
 				int	rangerCount = getLaser().getCount();
 				double[] rangerValues = getLaser().getRanges();
 
 				/** Consistency check for error ranger readings */
 				if (minBIndex<rangerCount && maxBIndex<rangerCount)
 				{
 					for (int beamIndex=minBIndex; beamIndex<maxBIndex; beamIndex++)
 					{
 						distCurr = rangerValues[beamIndex];
 
 						if (distCurr < MINRANGE)
 						{
 							sumDist += LPMAX;
 						}
 						else
 						{
 							sumDist += distCurr;
 						}
 
                         /** On each BEAMCOUNT's beam.. */
 						if((beamIndex-minBIndex) % BEAMCOUNT == 1)
 						{
 							averageDist = sumDist/BEAMCOUNT; /** Calculate the average distance. */
 							sumDist = 0.; /** Reset sum of beam average distance */
 
 							/** Calculate the minimum distance of the arc */
 							if (averageDist < minDist)
 							{
 								minDist = averageDist;
 							}
 						}
 						if ( isDebugLaser )
 						{
 							System.out.printf("beamInd: %3d\tsumDist: %5.2f\taveDist: %5.2f\tminDist: %5.2f\n",
 									beamIndex,
 									sumDist,
 									averageDist,
 									minDist);
 						}
 					}
 				}
 				else /** Index is out of bounds */
 				{
 					minDist = SHAPE_DIST;
 				}
 			}
 		}
 		return minDist;
 	}
 	/**
 	 * If there are less valid range values than @see SONARCOUNT
 	 * than the array contains fake (max) values.
 	 * @return The sonar range values.
 	 */
 	final double[] getSonarRanges()
 	{    
 	    double[] sonarValues = null;
         int sonarCount = 0;
 
         if (getSonar() != null)
         {
             /** Read recent sonar data */
             sonarValues = getSonar().getRanges();
             sonarCount  = getSonar().getCount();
         }
         /** In case we get a sonar count == 0 */
         if (sonarCount < 1)
         {
             sonarValues = new double[SONARCOUNT];
         }
         
         /** Check for dynamic sonar ranger availability. */
         for (int i=SONARCOUNT; i>0; i--)
         {
             /** Fill not available ranges with max values. */
             if (i > sonarCount)
             {
                 sonarValues[i-1] = SONARMAX;
             }
             else
             {
                 break;
             }
         }
         /** Corect error ranges */
         for (int i=0; i<SONARCOUNT; i++)
         {
         	if (sonarValues[i] > IPioneer.MINRANGE &&
         		sonarValues[i] < IPioneer.LPMAX )
         	{/** Value looks good */}
         	else
         	{
         		sonarValues[i] = IPioneer.LPMAX;        		
         	}
         }
         
         return sonarValues;
 	}
 	/**
 	 * Returns the minimum distance of the given view direction.
 	 * Robot shape shall be considered here by weighted SHAPE_DIST.
 	 * Derived arcs, sonars and weights from graphic "PioneerShape.fig".
 	 * NOTE: ALL might be slow due recursion, use it only for debugging!
 	 * @param viewDirection Robot view direction
 	 * @return Minimum distance of requested view Direction.
 	 */
 	final double getDistance( viewDirectType viewDirection )
 	{		
 	    double[] sonarValues = getSonarRanges();
 	    
 		/** Scan safety areas for walls. */
 		switch (viewDirection)
 		{
     		case LEFT      : return Math.min(getMinLasRange(LMIN,  LMAX) -HORZOFFSET-SHAPE_DIST, Math.min(sonarValues[0], sonarValues[15])-SHAPE_DIST);
     		case RIGHT     : return Math.min(getMinLasRange(RMIN,  RMAX) -HORZOFFSET-SHAPE_DIST, Math.min(sonarValues[7], sonarValues[8]) -SHAPE_DIST);
     		case FRONT     : return Math.min(getMinLasRange(FMIN,  FMAX)            -SHAPE_DIST, Math.min(sonarValues[3], sonarValues[4]) -SHAPE_DIST);
     		case RIGHTFRONT: return Math.min(getMinLasRange(RFMIN, RFMAX)-DIAGOFFSET-SHAPE_DIST, Math.min(sonarValues[5], sonarValues[6]) -SHAPE_DIST);
     		case LEFTFRONT : return Math.min(getMinLasRange(LFMIN, LFMAX)-DIAGOFFSET-SHAPE_DIST, Math.min(sonarValues[1], sonarValues[2]) -SHAPE_DIST);
     		case BACK      : return Math.min(sonarValues[11], sonarValues[12])-MOUNTOFFSET-SHAPE_DIST; /** Sorry, only sonar at rear. */
     		case LEFTREAR  : return Math.min(sonarValues[13], sonarValues[14])-MOUNTOFFSET-SHAPE_DIST; /** Sorry, only sonar at rear. */
     		case RIGHTREAR : return Math.min(sonarValues[9] , sonarValues[10])-MOUNTOFFSET-SHAPE_DIST; /** Sorry, only sonar at rear. */
     		case ALL       : return Math.min(getDistance(viewDirectType.LEFT),
     				Math.min(getDistance(viewDirectType.RIGHT),
     						Math.min(getDistance(viewDirectType.FRONT),
     								Math.min(getDistance(viewDirectType.BACK),
     										Math.min(getDistance(viewDirectType.RIGHTFRONT),
     												Math.min(getDistance(viewDirectType.LEFTFRONT),
     														Math.min(getDistance(viewDirectType.LEFTREAR), getDistance(viewDirectType.RIGHTREAR) )))))));
     		default: return 0.; /** Should be recognized if happens. */
 		}
 	}
 	/**
 	 * @return An sonar range array containing maximum (fake) range values.
 	 */
 	double[] maxSonarValues()
 	{
 	    double[] sonarValues = new double[SONARCOUNT];
 	   
 	    for (int i=0; i<SONARCOUNT; i++)
         {
             sonarValues[i] = SONARMAX;
         }
 
 	    return sonarValues;
 	}
 
 	/**
 	 * Plan a left wall follow trajectory.
 	 * Calculates the turnrate from range measurement and minimum wall follow
 	 * distance.
 	 * @return The new turnrate regarding the left wall (if any).
 	 */
 	final double getLeftWallfollow (double maxTurnrate)
 	{
 		double DistLFov  = 0;
 		double DistL     = 0;
 		double DistLRear = 0;
 		double newTurnrate;
 
 		DistLFov = getDistance(viewDirectType.LEFTFRONT);
 
 		/**
 		 * Do simple (left) wall following
 		 * do naiv calculus for turnrate; weight dist vector
 		 */
 		newTurnrate = Math.atan( (COS45*DistLFov - WALLFOLLOWDIST ) * 4 );
 
 		/** Normalize turnrate */
 		if (newTurnrate > Math.toRadians(maxTurnrate))
 		{
 		    newTurnrate = Math.toRadians(maxTurnrate);
 		}
 		else
 		    if (newTurnrate < -Math.toRadians(maxTurnrate))
 		    {
 		        newTurnrate = -Math.toRadians(maxTurnrate);
 		    }
 
 		DistL     = getDistance(viewDirectType.LEFT);
 		DistLRear = getDistance(viewDirectType.LEFTREAR);
 		
 		/** Go straight if no wall is in distance (front, left and left front). */
 		if (DistLFov  >= WALLLOSTDIST  &&
     		DistL     >= WALLLOSTDIST  &&
     		DistLRear >= WALLLOSTDIST     )
 		{
 		    newTurnrate = 0.0;
 			setCurrentState( StateType.WALL_SEARCHING );
 		}
 		else
 		{
 			setCurrentState( StateType.LWALL_FOLLOWING );
 		}
 
 		return newTurnrate;
 	}
 
 	/**
 	 * Collision avoidance overrides other turnrate if necessary!
 	 * May change turnrate or current state.
 	 * Biased by left wall following
 	 */
 	final double getCollisionAvoid (double checkTurnrate)
 	{
 		/** Scan FOV for Walls. */
 		double distLeftFront  = getDistance(viewDirectType.LEFTFRONT);
 		double distFront      = getDistance(viewDirectType.FRONT);
 		double distRightFront = getDistance(viewDirectType.RIGHTFRONT);
 
 		double distFrontRight = (distFront + distRightFront) / 2;
 		double distFrontLeft  = (distFront + distLeftFront)  / 2;
 
 		if ((distFrontLeft  < STOP_WALLFOLLOWDIST) ||
 			(distFrontRight < STOP_WALLFOLLOWDIST)   )
 		{
 			// TODO make a random maneuver when stuck
 		    setCurrentState( StateType.COLLISION_AVOIDANCE );
 			/** Turn right as long we want left wall following. */
 			return -Math.toRadians(STOP_ROT);
 		}
 		else
 		{
 			return checkTurnrate;
 		}
 	}
 
 	/**
 	 * Calculates a safe speed regarding collision avoiding obstacles.
 	 * The Speed will be zero if to near to any obstacle.
 	 * @param maxSpeed The speed being trying to set.
 	 * @return The safe speed.
 	 */
 	final double getSafeSpeed (double maxSpeed)
 	{
 		double tmpMinDistFront = Math.min(getDistance(viewDirectType.LEFTFRONT), Math.min(getDistance(viewDirectType.FRONT), getDistance(viewDirectType.RIGHTFRONT)));
 		double tmpMinDistBack  = Math.min(getDistance(viewDirectType.LEFTREAR), Math.min(getDistance(viewDirectType.BACK), getDistance(viewDirectType.RIGHTREAR)));
 		double speed = maxSpeed;
 
 		/**
 		 * Check forward direction
 		 */
 		if (maxSpeed > 0.0)
 		{
 		    if (tmpMinDistFront < WALLFOLLOWDIST)
 		    {
 		        speed = maxSpeed * (tmpMinDistFront/WALLFOLLOWDIST);
 
 		        /** Do not turn back if there is a wall! */
 		        if (tmpMinDistFront<0 && tmpMinDistBack<0)
 		        {
 		            if (tmpMinDistBack < tmpMinDistFront)
 		            {
 		                speed = (maxSpeed*tmpMinDistFront)/(tmpMinDistFront+tmpMinDistBack);
 		            }
 		        }
 		    }
 		}
 		else
 		{
 		    /**
 	         * Check backward direction
 	         */
 		    if (maxSpeed < 0.0)
 		    {
 		        if (tmpMinDistBack < WALLFOLLOWDIST)
 		        {
 		            speed = maxSpeed * (tmpMinDistBack/WALLFOLLOWDIST);
 
 		            /** Do not turn forward if there is a wall! */
 		            if (tmpMinDistBack<0 && tmpMinDistFront<0)
 		            {
 		                if (tmpMinDistFront < tmpMinDistBack)
 		                {
 		                    speed = (maxSpeed*tmpMinDistBack)/(tmpMinDistBack+tmpMinDistFront);
 		                }
 		            }
 		        }
 		    }
 		}
 		return speed;
 	}
 
 	/**
 	 * Checks if turning the robot is not causing collisions.
 	 * Implements more or less a rotation policy which decides depending on
 	 * obstacles at the 4 robot edge surounding spots
 	 * To not interfere to heavy to overall behaviour turnrate is only inverted (or
 	 * set to zero)
 	 * @return Safe turnrate.
 	 */
 	final double getSafeTurnrate (double maxTurnrate)
 	{
 		double saveTurnrate = maxTurnrate;
 
 		/**
 		 * Check for a right turn.
 		 */
 		if (maxTurnrate < 0)
 		{
 			if (getDistance(viewDirectType.LEFTREAR) < 0)
 			{
 				saveTurnrate = 0;
 			}
 			if (getDistance(viewDirectType.RIGHT) < 0)
 			{
 				saveTurnrate = 0;
 			}
 		/**
 		 * Check for a left turn.
 		 */
 		}
 		else
 		    if (maxTurnrate > 0)
 		    {
 		        if (getDistance(viewDirectType.RIGHTREAR) < 0)
 		        {
 		            saveTurnrate = 0;
 		        }
 		        if (getDistance(viewDirectType.LEFT) < 0)
 		        {
 		            saveTurnrate = 0;
 		        }
 		    }
 		
 		return saveTurnrate;
 	}
 
     /**
      * @return the currentState
      */
     public StateType getCurrentState()
     {
         return currentState;
     }
 
     /**
     * @param newCurrentState the currentState to set
      */
     void setCurrentState(StateType newCurrentState)
     {
         currentState = newCurrentState;
     }
     public void setWallfollow()
     {
         if (getSimu() == null)
             getPosi().enableMotor();
         
         setCurrentState(StateType.LWALL_FOLLOWING);
     }
     /**
      * Sets the robot into manual mode.
      * In this mode the speed and the turnrate of the motors can be modified.
      */
     public void setCommand()
     {
         if (getSimu() == null)
             getPosi().enableMotor();
         
         setCurrentState(StateType.SET_SPEED);        
     }
 
     @SuppressWarnings("unused")
     void debugSensorData()
     {
         if (isDebugSonar && getSonar() != null) {
             double[] sonarValues = getSonarRanges();  
     
             System.out.print("\nSonar");
             for(int i=0; i<SONARCOUNT; i++)
                 System.out.printf(" [%d]:%4.1f ", i, sonarValues[i]);
         }
     
         if (isDebugState) {
             System.out.printf("turnrate/speed/state:\t%5.2f\t%5.2f\t%s\n", getTurnrate(), getSpeed(), ""+currentState);
         }
         if (isDebugDistance) {
             if (getLaser() != null) {
                 System.out.print("Laser (l/lf/f/rf/r/rb/b/lb):\t");
                 System.out.printf("%5.2f", getMinLasRange(LMIN,  LMAX)-HORZOFFSET); System.out.print("\t");
                 System.out.printf("%5.2f", getMinLasRange(LFMIN, LFMAX)-DIAGOFFSET);    System.out.print("\t");
                 System.out.printf("%5.2f", getMinLasRange(FMIN,  FMAX));                System.out.print("\t");
                 System.out.printf("%5.2f", getMinLasRange(RFMIN, RFMAX)-DIAGOFFSET);    System.out.print("\t");
                 System.out.printf("%5.2f", getMinLasRange(RMIN,  RMAX) -HORZOFFSET);
                 System.out.println("\t" + " XXXX" + "\t" + " XXXX" + "\t" + " XXXX");
             } else {
                 System.out.println("No laser available!");
             }
     
             if (getSonar() != null) {
                 double[] sonarValues = getSonarRanges();
                 int sonarCount = getSonar().getCount();
                 if (sonarCount >= SONARCOUNT) {
                     System.out.print("Sonar (l/lf/f/rf/r/rb/b/lb):\t");
                     System.out.printf("%5.2f", Math.min(sonarValues[15],sonarValues[0]));   System.out.print("\t");
                     System.out.printf("%5.2f", Math.min(sonarValues[1], sonarValues[2]));   System.out.print("\t");
                     System.out.printf("%5.2f", Math.min(sonarValues[3], sonarValues[4]));   System.out.print("\t");
                     System.out.printf("%5.2f", Math.min(sonarValues[5], sonarValues[6]));   System.out.print("\t");
                     System.out.printf("%5.2f", Math.min(sonarValues[7], sonarValues[8]));   System.out.print("\t");
                     System.out.printf("%5.2f", Math.min(sonarValues[9], sonarValues[10])-MOUNTOFFSET); System.out.print("\t");
                     System.out.printf("%5.2f", Math.min(sonarValues[11],sonarValues[12])-MOUNTOFFSET); System.out.print("\t");
                     System.out.printf("%5.2f\n", Math.min(sonarValues[13],sonarValues[14])-MOUNTOFFSET);
                 } else {
                     System.out.println("Sonar count: "+sonarCount);
                 }
             } else {
                 System.out.println("No sonar available!");
             }
     
             System.out.print("Shape (l/lf/f/rf/r/rb/b/lb):\t");
             System.out.printf("%5.2f", getDistance(viewDirectType.LEFT)); System.out.print("\t");
             System.out.printf("%5.2f", getDistance(viewDirectType.LEFTFRONT));  System.out.print("\t");
             System.out.printf("%5.2f", getDistance(viewDirectType.FRONT));      System.out.print("\t");
             System.out.printf("%5.2f", getDistance(viewDirectType.RIGHTFRONT)); System.out.print("\t");
             System.out.printf("%5.2f", getDistance(viewDirectType.RIGHT));      System.out.print("\t");
             System.out.printf("%5.2f", getDistance(viewDirectType.RIGHTREAR));  System.out.print("\t");
             System.out.printf("%5.2f", getDistance(viewDirectType.BACK));       System.out.print("\t");
             System.out.printf("%5.2f\n", getDistance(viewDirectType.LEFTREAR));
         }
         if (isDebugPosition) {
             System.out.printf("%5.2f", posi.getPosition().getX());  System.out.print("\t");
             System.out.printf("%5.2f", posi.getPosition().getY());  System.out.print("\t");
             System.out.printf("%5.2f\n", Math.toDegrees(posi.getPosition().getYaw()));
         }
         
     }
 }
 
