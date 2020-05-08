 package team219;
 
 /*
 Based largely on the example swarm2 player fromm lecture slides.
 - Also try to use medbays though if injured.
 - And use artillery if lots of enemies around but not too close.
 - artillery strategy is a bit simple.
 - first unit or two are scouts incase we're facing a nukebot.
 
 */
 
 
 import battlecode.common.*;
 
 public class RobotPlayer{
 	
 	static RobotController rc;
 	static int mult = 145;   //hopefully unique for clean comms.
    static int DONT_LAY=1;
    static int LAY_MINES=2;
 	static int status = DONT_LAY;//1 is don't lay mines, 2 is lay mines
 
 	static int injured_health = 20;
 	static int full_health = 40;
    static int SUPERIORITY = 15;
    static int CAPTURE_PRIVACY_RADIUS = 17;
    static int MINE_AROUND_HQ = 4;
    static int MEDBAY_RANGE = 200;
    static int SHIELD_RANGE = 100;
    static int OFFSET_MINE = 1;
    static int OFFSET_MED = 2;
    static int OFFSET_THREAT = 3;
    static int OFFSET_ART = 4;
    static int RUSH_ROUND = 200;
 	public static void run(RobotController myRC){
 		rc = myRC;
 		if (rc.getTeam()==Team.A)
 			mult = 314;   //so we can play ourselves needs to differ fromm above.
 		
 		while(true){
 			try{
 				if (rc.getType()==RobotType.SOLDIER){
                if(Clock.getRoundNum()<3){ //make the first scout
                   scoutCode();//this may finish if enemies are seen.
                   soldierCode();
                }else if((Clock.getRoundNum() %20)==0){
                   capturerCode();
                   soldierCode();
                }else{
                   soldierCode();
                }
 				}else if (rc.getType()==RobotType.HQ){
 					hqCode();
 				}else if (rc.getType()==RobotType.MEDBAY){
 					medbayCode();
 				}else if (rc.getType()==RobotType.ARTILLERY){
 					artilleryCode();
 				}
 
 			}catch (Exception e){
 				System.out.println("caught exception before it killed us:");
 				e.printStackTrace();
 			}
 			rc.yield();
 		}
 	}
    
    private static void soldierCode(){
 		MapLocation rallyPt = rc.getLocation();
       boolean injured=false;
       boolean unshielded=false;
 		while(true){
 			try{
 
             Robot[] allies = rc.senseNearbyGameObjects(Robot.class,14,rc.getTeam());
             Robot[] enemies = rc.senseNearbyGameObjects(Robot.class,10000000,rc.getTeam().opponent());
             Robot[] nearbyEnemies = rc.senseNearbyGameObjects(Robot.class,14,rc.getTeam().opponent());
             boolean useRadio=rc.getTeamPower() > (GameConstants.BROADCAST_READ_COST * 3);
             int hqThreatDist=10000;
             if(useRadio){
                hqThreatDist= rc.readBroadcast(getChannel()+OFFSET_THREAT);
             }
 
             MapLocation myLoc=rc.getLocation();
 
             if(rc.getEnergon() < injured_health)
                injured=true;
             if(rc.getEnergon() ==  full_health) //we're healed.  maxEnergon didn'tt work?
                injured=false;
 
             if(Clock.getRoundNum() > 300){
                if(rc.getShields()<40){
                   unshielded=true;
                }
                if(rc.getShields()> 60){
                   unshielded=false;
                }
             }
 
             if(rc.getLocation().distanceSquaredTo(rc.senseHQLocation()) > hqThreatDist ){
                //hq under threat.
                if (rc.isActive()){
                         freeGo(rc.senseHQLocation(),allies,enemies,nearbyEnemies);
                }
             }else if(injured || ((nearbyEnemies.length ==0) && (rc.getEnergon() < full_health ) )){
             // deviate if low health
                if (rc.isActive()){
  //                 int medChannel=getChannel()+OFFSET_MED;
                   //MapLocation medbayLoc= IntToMaplocation(rc.readBroadcast(medChannel));
                   MapLocation medbayLoc= nearestMedbay(myLoc);
                   if(medbayLoc!= null){
                      Direction finalDir = myLoc.directionTo(medbayLoc);
                      if (Math.random()<.1)
                         finalDir = finalDir.rotateRight();
                      boolean okToDefuse=(nearbyEnemies.length < 1)||
                      ( (allies.length > (3 * nearbyEnemies.length) )&& (rc.getRobot().getID()%3
                      ==0) );
                      simpleMove(finalDir, myLoc, okToDefuse);
                   
                   }else{
                      //head for nearest encampment. (it'll cap as medbay)
                      MapLocation futureMedbay=getNearbyNeutralEncampment(rc,50);
                      if(futureMedbay!= null){
                         freeGo(futureMedbay, allies, enemies, nearbyEnemies);
                      }else{
                         freeGo(rc.senseHQLocation(),allies,enemies,nearbyEnemies);
                      }
                   }
                }
             }else if(unshielded){
                if (rc.isActive()){
                   MapLocation shieldLoc= nearestShield(myLoc);
                   if(shieldLoc!= null){
                      Direction finalDir = myLoc.directionTo(shieldLoc);
                      if (Math.random()<.1)
                         finalDir = finalDir.rotateRight();
                      boolean okToDefuse=(nearbyEnemies.length < 1)||
                         ( (allies.length > (3 * nearbyEnemies.length) )&& (rc.getRobot().getID()%3
                                                                            ==0) );
                      simpleMove(finalDir, myLoc, okToDefuse);
 
                   }else{
                      //head for nearest encampment. (it'll cap as shield)
                      MapLocation futureshield=getNearbyNeutralEncampment(rc,50);
                      if(futureshield!= null){
                         freeGo(futureshield, allies, enemies, nearbyEnemies);
                      }else{
                         freeGo(rc.senseHQLocation(),allies,enemies,nearbyEnemies);
                      }
                   }
                }
             }else{
                if(useRadio){
                   //receive rally point from HQ
                   MapLocation received = IntToMaplocation(rc.readBroadcast(getChannel()));
                   if (received!= null){
                      rallyPt = received;
                      rc.setIndicatorString(0,"goal: "+rallyPt.toString());
                   }
                   //receive mining command
                   int ir = rc.readBroadcast(getChannel()+OFFSET_MINE);
                   if (ir!=0&&ir<=2)
                      status = ir;
                }else{
                   status=DONT_LAY;
                   rallyPt=getNearbyNeutralEncampment(rc, 14);
                   rc.setIndicatorString(0,"no radio: "+rallyPt.toString());
                }
 
                if (rc.isActive()){
                   if (status == DONT_LAY){//don't lay mines
                      //move toward received goal, using swarm behavior
                      freeGo(rallyPt,allies,enemies,nearbyEnemies);
                   }else if (status == LAY_MINES){//lay mines!
                      if (goodPlace(rc.getLocation(),nearbyEnemies.length,enemies.length)&&rc.senseMine(rc.getLocation())==null){
                         rc.layMine();
                      }else{
                         freeGo(rallyPt,allies,enemies,nearbyEnemies);
                      }
                   }
                }
             }
          }catch (Exception e){
             System.out.println("Soldier Exception");
             e.printStackTrace();
 			}
 			rc.yield();
 		}
 	}
 
 
 
 	private static boolean goodPlace(MapLocation location, int nearbyEnemies, int enemies) {
 
       if((nearbyEnemies > 0)|| (enemies == 0)) return false; //if dont mine if enemy not seen yet, or close
       if((nearbyEnemies == 0) && (location.distanceSquaredTo(rc.senseHQLocation())<=MINE_AROUND_HQ))
          return true;
 		return ((2*location.x+location.y)%5==0);//pickaxe without gaps
 //		return ((3*location.x+location.y)%8==0);//pickaxe with gaps
 //		return ((location.x+location.y)%2==0);//checkerboard
 	}
 	//Movement system
 	private static void freeGo(MapLocation target, Robot[] allies,Robot[] enemies,Robot[] nearbyEnemies) throws GameActionException {
 		//This robot will be attracted to the goal and repulsed from other things
 		MapLocation myLoc = rc.getLocation();
 		Direction toTarget = myLoc.directionTo(target);
 		int targetWeighting = targetWeight(myLoc.distanceSquaredTo(target));
 		MapLocation goalLoc = myLoc.add(toTarget,targetWeighting);//toward target, TODO weighted by the distance?
 		
 		if (enemies.length==0){
 			//find closest allied robot. repel away from that robot.
 			if(allies.length>0){
 				MapLocation closestAlly = findClosest(allies);
             //unless they're capturing (defend them).
             boolean notYetCaptured= (rc.senseEncampmentSquares(closestAlly,0,Team.NEUTRAL).length== 1 ); 
             if(rc.senseEncampmentSquare(closestAlly) && (notYetCaptured )){
                goalLoc = goalLoc.add(myLoc.directionTo(closestAlly),10);
             }else{
                goalLoc = goalLoc.add(myLoc.directionTo(closestAlly),-3);
             }
 			}
 		}else if (allies.length<nearbyEnemies.length+3){
 			if(allies.length>0){//find closest allied robot. attract to that robot.
 				MapLocation closestAlly = findClosest(allies);
 				goalLoc = goalLoc.add(myLoc.directionTo(closestAlly),5);
 			}
 			if((nearbyEnemies.length>0) && (rc.senseHQLocation().distanceSquaredTo(myLoc) > 37 )){//avoid enemy unless defending.
 				MapLocation closestEnemy = findClosest(nearbyEnemies);
 				goalLoc = goalLoc.add(myLoc.directionTo(closestEnemy),-10);
 			}
 		}else if (allies.length>=nearbyEnemies.length*3){
 			if(allies.length>0){
 				MapLocation closestAlly = findClosest(allies);
 				goalLoc = goalLoc.add(myLoc.directionTo(closestAlly),5);
 			}
 			if(nearbyEnemies.length>0){
 				MapLocation closestEnemy = findClosest(nearbyEnemies);
 				goalLoc = goalLoc.add(myLoc.directionTo(closestEnemy),10);
          }else{// no nearby enemies; go toward far enemy
             if(enemies.length>0){
                MapLocation closestEnemy = findClosest(enemies);
                goalLoc = goalLoc.add(myLoc.directionTo(closestEnemy),10);
             }else{
                goalLoc = goalLoc.add(myLoc.directionTo( rc.senseEnemyHQLocation()),9);
             }
 			}
 		}
 		//TODO repel from allied mines?
 		//now use that direction
 		Direction finalDir = myLoc.directionTo(goalLoc);
 		if (Math.random()<.1)
 			finalDir = finalDir.rotateRight();
 		simpleMove(finalDir, myLoc,nearbyEnemies.length < 1);
 	}
 	private static int targetWeight(int dSquared){
 		if (dSquared>100){
 			return 5;
 		}else if (dSquared>9){
 			return 2;
 		}else{
 			return 1;
 		}
 	}
    private static MapLocation getNearbyNeutralEncampment(RobotController rc, int radius) throws Exception{
       MapLocation nearestEncamp= null;
       MapLocation[] nearEncamps=
       rc.senseEncampmentSquares(rc.getLocation(),radius, Team.NEUTRAL);
       int nearest_dist=10000;
       for(int i=0; i < nearEncamps.length; i++){
          if(rc.getLocation().distanceSquaredTo(nearEncamps[i]) < nearest_dist){
             nearest_dist=rc.getLocation().distanceSquaredTo(nearEncamps[i]);
             nearestEncamp=nearEncamps[i];
          }
       }
       return nearestEncamp;
    }
 
 
 	private static void simpleMove(Direction dir, MapLocation myLoc, boolean defuseMines) throws GameActionException {
 		//first try to capture an encampment
       int numEnemies =
                      rc.senseNearbyGameObjects(Robot.class,CAPTURE_PRIVACY_RADIUS,rc.getTeam().opponent()).length;
       int numArtilleryTargets=rc.senseNearbyGameObjects(Robot.class, RobotType.ARTILLERY.attackRadiusMaxSquared, rc.getTeam().opponent()).length;
 		MapLocation enemyLoc = rc.senseEnemyHQLocation();
 
 
       int existingGens = 0;
       int existingSupp = 0;
       int existingMed =0;
       int existingArt =0;
       int existingShield =0;
 
       Robot[] friendlyObject = rc.senseNearbyGameObjects(Robot.class,10000,rc.getTeam());
       //get all objects.
       int found=0;
       // check numbers of each.  Would be more efficient to do one pass than one
       // for each type..
       RobotInfo rInfo;
       for(int i=0; i < friendlyObject.length; i++){
          rInfo = rc.senseRobotInfo(friendlyObject[i]);
          if(rInfo.type == RobotType.GENERATOR){
             existingGens++;
          }else if(rInfo.type == RobotType.SUPPLIER){
             existingSupp++;
          }else if(rInfo.type == RobotType.MEDBAY){
             existingMed++;
          }else if(rInfo.type == RobotType.ARTILLERY){
             existingArt++;
          }else if(rInfo.type == RobotType.SHIELDS){
             existingShield++;
          }
 
       }
 
 
 
 		if
       (defuseMines && (rc.getTeamPower() > rc.senseCaptureCost()) && rc.senseEncampmentSquare(myLoc) &&
       (numEnemies<1) && notMakeEncampWall(myLoc) ){//leisure indicator
 			if(rc.getEnergon() < injured_health){
             //check if a medbay is a adjacent first.  If it is, build artillery. lols.
             //int medChannel=getChannel()+ OFFSET_MED;
             //MapLocation medbayLoc= IntToMaplocation(rc.readBroadcast(medChannel));
             MapLocation medbayLoc = nearestMedbay(myLoc);
             if(medbayLoc!= null){
                if(rc.getLocation().isAdjacentTo(medbayLoc)){
                   rc.captureEncampment(RobotType.ARTILLERY);
                }else{
                   rc.captureEncampment(RobotType.MEDBAY);
                }
             }else{
                rc.captureEncampment(RobotType.MEDBAY);
             }
          }else if((numArtilleryTargets>3) ||(enemyLoc.distanceSquaredTo(myLoc)<
          (RobotType.ARTILLERY.attackRadiusMaxSquared*2) )){ 
                   rc.captureEncampment(RobotType.ARTILLERY);
          }else if((existingArt < 1)&&(Math.random()>.75)){
                   rc.captureEncampment(RobotType.ARTILLERY);
          }else if(existingMed < 1){
                   rc.captureEncampment(RobotType.MEDBAY); 
          }else if((existingShield<2)&&(Clock.getRoundNum()<300)){
             rc.captureEncampment(RobotType.SHIELDS); 
          }else if((Math.random()<.7)&& (existingGens<6)){
             rc.captureEncampment(RobotType.GENERATOR); 
          }else if(existingSupp<10){
 				rc.captureEncampment(RobotType.SUPPLIER); 
 						}else{
             doMove(dir,myLoc,defuseMines); // other things didn't work out.
          }
 		}else{
          //then consider moving
          doMove(dir,myLoc,defuseMines);
       }
    }
    private static void doMove(Direction dir, MapLocation myLoc, boolean defuseMines) throws GameActionException{
 
       int[] directionOffsets = {0,1,-1,2,-2};
       Direction lookingAtCurrently = null;
       //argh, vims indenting has issues.
 lookAround: for (int d:directionOffsets){
                lookingAtCurrently = Direction.values()[(dir.ordinal()+d+8)%8];
                Team currentMine = rc.senseMine(myLoc.add(lookingAtCurrently));
                if(rc.canMove(lookingAtCurrently)&&(defuseMines||(!defuseMines&&(currentMine==rc.getTeam()||currentMine==null)))){
                   moveOrDefuse(lookingAtCurrently);
                   break lookAround;
                }
 
       } 
    }
 
 
             private static boolean notMakeEncampWall(MapLocation target) throws GameActionException{
       // Function to tell if capturing a square makes an encampmentWall.
       int Xobjects=0;
       int Yobjects=0;
       int diagObjects=0;
 //FIXME The better way to do this is by verifying if we're making a blocking structure.
 // aint nobody got not time for that, so I'm going to count how many friendly encampments are close.
       if (rc.senseObjectAtLocation(target.add(Direction.EAST)) != null)              Xobjects++;
       if (rc.senseObjectAtLocation(target.add(Direction.EAST).add(Direction.EAST)) != null)    Xobjects++;
       if (rc.senseObjectAtLocation(target.add(Direction.WEST)) != null)              Xobjects++;
       if (rc.senseObjectAtLocation(target.add(Direction.WEST).add(Direction.WEST)) != null)    Xobjects++;          
 
       if (rc.senseObjectAtLocation(target.add(Direction.NORTH)) != null)             Yobjects++;
       if (rc.senseObjectAtLocation(target.add(Direction.NORTH).add(Direction.NORTH)) != null)  Yobjects++;
       if (rc.senseObjectAtLocation(target.add(Direction.SOUTH)) != null)             Yobjects++;
       if (rc.senseObjectAtLocation(target.add(Direction.SOUTH).add(Direction.SOUTH)) != null)  Yobjects++;          
 
       if (rc.senseObjectAtLocation(target.add(Direction.NORTH_EAST)) != null)             diagObjects++;
       if (rc.senseObjectAtLocation(target.add(Direction.NORTH_WEST)) != null)  diagObjects++;
       if (rc.senseObjectAtLocation(target.add(Direction.SOUTH_EAST)) != null)             diagObjects++;
       if (rc.senseObjectAtLocation(target.add(Direction.SOUTH_WEST)) != null)  diagObjects++;          
 
 
 
       if((Xobjects >= 2) || (Yobjects >= 2) || ((Xobjects + Yobjects+diagObjects) >2 ))
          return false; //makes a wall
       else
          return true;
 
    }
 
 	private static void moveOrDefuse(Direction dir) throws GameActionException{
 		MapLocation ahead = rc.getLocation().add(dir);
 		Team mineAhead = rc.senseMine(ahead);
 		if(mineAhead!=null&&mineAhead!= rc.getTeam()){
 			rc.defuseMine(ahead);
 		}else{
 			rc.move(dir);			
 		}
 	}
 	private static MapLocation findClosest(Robot[] enemyRobots) throws GameActionException {
 		int closestDist = 1000000;
 		MapLocation closestEnemy=null;
 		for (int i=0;i<enemyRobots.length;i++){
 			Robot arobot = enemyRobots[i];
 			RobotInfo arobotInfo = rc.senseRobotInfo(arobot);
 			int dist = arobotInfo.location.distanceSquaredTo(rc.getLocation());
 			if (dist<closestDist){
 				closestDist = dist;
 				closestEnemy = arobotInfo.location;
 			}
 		}
 		return closestEnemy;
 	}
 //HQ
 	private static void hqCode(){
 		MapLocation myLoc = rc.getLocation();
 		MapLocation enemyLoc = rc.senseEnemyHQLocation();
 		MapLocation rallyPt = myLoc.add(myLoc.directionTo(enemyLoc),5);
 		while(true){
 			try{
 				
 				if (rc.isActive()) {
                int numFriendlies=rc.senseNearbyGameObjects(Robot.class,1000,rc.getTeam()).length;
                int numEnemies =
                rc.senseNearbyGameObjects(Robot.class,10000000,rc.getTeam().opponent()).length;
 					// Spawn a soldier
 					//			Robot[] alliedRobots = rc.senseNearbyGameObjects(Robot.class,100000,rc.getTeam());
                int beGreaterBy= rc.hasUpgrade(Upgrade.FUSION) ? SUPERIORITY*2:SUPERIORITY;
 
 					if(((rc.getTeamPower()-40>10)||(Clock.getRoundNum()<2))&&(numFriendlies < (numEnemies +beGreaterBy))){
 						lookAround: for (Direction d:Direction.values()){
                      if(d == Direction.OMNI)
 								break lookAround;
                      if(d == Direction.NONE)
 								break lookAround;
							if (rc.canMove(d)&&(d != Direction.OMNI)&&(d != Direction.NONE)){
 								rc.spawn(d);
                         //System.out.println("friendlies" + numFriendlies + "spawning");
 								break lookAround;
 							}
 						}
 					}else if (!rc.hasUpgrade(Upgrade.PICKAXE)){
 						rc.researchUpgrade(Upgrade.PICKAXE);
 					}else if (!rc.hasUpgrade(Upgrade.DEFUSION)){
 						rc.researchUpgrade(Upgrade.DEFUSION);
 					}else if (!rc.hasUpgrade(Upgrade.FUSION)){
 						rc.researchUpgrade(Upgrade.FUSION);
 					}else if (!rc.hasUpgrade(Upgrade.NUKE)){
 						rc.researchUpgrade(Upgrade.NUKE);
 					}
 
 				}
 				
 				//move the rally point if it is a capfutureencampment
 				MapLocation[] alliedEncampments = rc.senseAlliedEncampmentSquares();
 				if (alliedEncampments.length>0&&among(alliedEncampments,rallyPt)){
 					MapLocation closestEncampment = captureEncampments(alliedEncampments);
 					if (closestEncampment!= null){
 						rallyPt = closestEncampment;
 					}
 				}
 				
 				if(rc.getEnergon()<300||Clock.getRoundNum()>RUSH_ROUND||rc.senseEnemyNukeHalfDone()){//kill enemy if nearing round limit or injured
 					rallyPt = enemyLoc;
 				}
 				
 				//message allies about where to go
 				int channel = getChannel();
 				int msg = MapLocationToInt(rallyPt);
 				rc.broadcast(channel, msg);
 				rc.setIndicatorString(0,"Posted "+msg+" to "+channel);
 				
 				//message allies about whether to mine
             Robot[] nearbyEnemies=rc.senseNearbyGameObjects(Robot.class,1000000,rc.getTeam().opponent());
             int numNearbyEnemies=nearbyEnemies.length;
 				if
             ( (rc.hasUpgrade(Upgrade.PICKAXE) && (numNearbyEnemies==0) )|| 
             ((numNearbyEnemies==0)&&(Clock.getRoundNum()<150)&&(nearestMedbay(rc.senseHQLocation())!=
             null) )){
 					rc.broadcast(getChannel()+OFFSET_MINE, LAY_MINES);
 				}else{
 					rc.broadcast(getChannel()+OFFSET_MINE, DONT_LAY);
             }
 
             MapLocation closestEnemy=findClosest(nearbyEnemies);
             if(closestEnemy !=null){
                int threatDistance=rc.senseHQLocation().distanceSquaredTo(closestEnemy);
                //rc.broadcast(getChannel()+3, threatDistance);
                rc.broadcast(getChannel() + OFFSET_THREAT, threatDistance);
             }else{
                rc.broadcast(getChannel() + OFFSET_THREAT, 100000);
             }
 
 				
 			}catch (Exception e){
 				System.out.println("Soldier Exception");
 				e.printStackTrace();
 			}
 			rc.yield();
 		}
 	}
 	private static void medbayCode(){
 		MapLocation myLoc = rc.getLocation();
       int roundsSinceUsed=0;
 		while(true){
 			try{
 				
             //if (rc.isActive()) {
                // See how long since a friendly's been adjacent.  Suidice if > 100 & teampower low.
                Robot[] allies = rc.senseNearbyGameObjects(Robot.class,4,rc.getTeam());
                if(allies.length <2){//often encampments next to medbays.
                   roundsSinceUsed++;
                }else{
                   roundsSinceUsed=0;
                }
             //}
             if((roundsSinceUsed > 200) && (rc.getTeamPower() < 50 )){
                rc.suicide();
             }
 
 			}catch (Exception e){
 				System.out.println("Soldier Exception");
 				e.printStackTrace();
 			}
 			rc.yield();
 		}
 	}
    private static void artilleryCode(){
       MapLocation myLoc = rc.getLocation();
       while(true){
          try{
 
             if (rc.isActive()) {
 
                Robot[] nearbyEnemies = rc.senseNearbyGameObjects(Robot.class,RobotType.ARTILLERY.attackRadiusMaxSquared,rc.getTeam().opponent());
                
                if(nearbyEnemies.length > 0){
                   rc.attackSquare(findClosest(nearbyEnemies));
                }
             }
          }catch (Exception e){
             System.out.println("Soldier Exception");
             e.printStackTrace();
          }
          rc.yield();
       }
    }
 
 	//Messaging functions
 	public static int getChannel(){
 		int channel = ((Clock.getRoundNum()/5)*mult)%GameConstants.BROADCAST_MAX_CHANNELS;
 		//int channel = (5*mult)%GameConstants.BROADCAST_MAX_CHANNELS;
 		return channel;
 	}
 	public static int MapLocationToInt(MapLocation loc){
 		return loc.x*1000+loc.y;
 	}
 	public static MapLocation IntToMaplocation(int mint){
 		int y = mint%1000;
 		int x = (mint-y)/1000;
 		if(x==0&&y==0){
 			return null;
 		}else{
 			return new MapLocation(x,y);
 		}
 	}
 //locating encampment
 	public static MapLocation captureEncampments(MapLocation[] alliedEncampments) throws GameActionException{
 		MapLocation[] allEncampments = rc.senseAllEncampmentSquares();
 		//locate uncaptured encampments within a certain radius
 		MapLocation[] neutralEncampments = new MapLocation[allEncampments.length];
 		int neInd = 0;
 		
 		// Compute nearest encampment (counting the enemy HQ)
 		outer: for(MapLocation enc: allEncampments) {
 			for(MapLocation aenc: alliedEncampments) 
 				if(aenc.equals(enc))
 					continue outer;
 			if(rc.senseHQLocation().distanceSquaredTo(enc)<= Math.pow(Clock.getRoundNum()/10, 2)){
 				//add to neutral encampments list
 				neutralEncampments[neInd]=enc;
 				neInd=neInd+1;
 			}
 		}
 		rc.setIndicatorString(2, "neutral enc det "+neInd+" round "+Clock.getRoundNum());
 		
 		if (neInd>0){
 			//proceed to an encampment and capture it
 			int which = (int) ((Math.random()*100)%neInd);
 			MapLocation campLoc = neutralEncampments[which];
 			return campLoc;
 		}else{//no encampments to capture; change state
 			return null;
 		}
 	}
    
    public static MapLocation nearestMedbay(MapLocation myLoc) throws GameActionException{
       Robot[] friendlyObject = rc.senseNearbyGameObjects(Robot.class, MEDBAY_RANGE,rc.getTeam());
       RobotInfo rInfo;
       MapLocation nearestMedbay=null;
       int nearestMedbayDist=10000;
       for(int i=0; i < friendlyObject.length; i++){
          rInfo = rc.senseRobotInfo(friendlyObject[i]);
          if(rInfo.type == RobotType.MEDBAY){
 
             if(nearestMedbay==null){
                nearestMedbay=rInfo.location;
                nearestMedbayDist=rInfo.location.distanceSquaredTo(myLoc);
             }else if(rInfo.location.distanceSquaredTo(myLoc) < nearestMedbayDist){
                nearestMedbay=rInfo.location;
                nearestMedbayDist=rInfo.location.distanceSquaredTo(myLoc);
             }
 
          }
       }
       return nearestMedbay;
    }
 
    public static MapLocation nearestShield(MapLocation myLoc) throws GameActionException{
       Robot[] friendlyObject = rc.senseNearbyGameObjects(Robot.class, SHIELD_RANGE,rc.getTeam());
       RobotInfo rInfo;
       MapLocation nearestShield=null;
       int nearestShieldDist=10000;
       for(int i=0; i < friendlyObject.length; i++){
          rInfo = rc.senseRobotInfo(friendlyObject[i]);
          if(rInfo.type == RobotType.SHIELDS){
 
             if(nearestShield==null){
                nearestShield=rInfo.location;
                nearestShieldDist=rInfo.location.distanceSquaredTo(myLoc);
             }else if(rInfo.location.distanceSquaredTo(myLoc) < nearestShieldDist){
                nearestShield=rInfo.location;
                nearestShieldDist=rInfo.location.distanceSquaredTo(myLoc);
             }
 
          }
       }
       return nearestShield;
    }
 	private static boolean among(MapLocation[] alliedEncampments,
 			MapLocation rallyPt) {
 		for(MapLocation enc:alliedEncampments){
 			if(enc.equals(rallyPt))
 				return true;
 		}
 		return false;
 	}
 
    private static void scoutCode(){
       MapLocation myLoc = rc.getLocation();
       while(true){
          try{
 
             if (rc.isActive()) { 
                
                Robot[] nearbyEnemies = rc.senseNearbyGameObjects(Robot.class, 40,rc.getTeam().opponent());
                if(nearbyEnemies.length>1)
                   return;//scouting's done.
 
 
                boolean rotating_left_on_cant_move = (Math.random() < 0.5);
 
                // aim to move directly towards enemy HQ
                Direction dir = rc.getLocation().directionTo(rc.senseEnemyHQLocation());
 
                for (int i = 0; i < 8; i++) {
 
                   if (rc.isActive()) {
                      MapLocation location_of_dir = rc.getLocation().add(dir);
 
                      if (rc.senseMine(location_of_dir) != null) {
                         rc.defuseMine(location_of_dir);
                         break;
                      } else {
 
                         if (rc.canMove(dir)) {
                            //rc.setIndicatorString(0, "Last direction moved:"+dir.toString());
                            //System.out.println("About to move" +  rc.getLocation().toString() + dir.toString() );
                            rc.move(dir);
                            //System.out.println("Moved" +  rc.getLocation().toString() + dir.toString() );
                            //rc.breakpoint();
                            i=8;
                            break;
                         } else {
                            if (rotating_left_on_cant_move) {
                               dir = dir.rotateLeft();
                            } else {
                               dir = dir.rotateRight();
                            }
                         }
                      }
                   }
                }
             }
          }catch (Exception e){
             System.out.println("Scout Exception");
             e.printStackTrace();
          }
          rc.yield();
       }
    }
    private static void capturerCode(){
       MapLocation myLoc = rc.getLocation();
       while(true){
          try{
 
             if (rc.isActive()) { 
                
                Robot[] nearbyEnemies = rc.senseNearbyGameObjects(Robot.class, 40,rc.getTeam().opponent());
                if(nearbyEnemies.length>1)
                   return;//scouting's done.
 
 
                boolean rotating_left_on_cant_move = (Math.random() < 0.5);
 
                // aim to move directly towards an encampment
                MapLocation target = getNearbyNeutralEncampment(rc, 10000);
                if(target ==null)
                   return;
                Direction dir = rc.getLocation().directionTo(target);
                if((dir == Direction.NONE)||(dir == Direction.OMNI))
                   return;
                for (int i = 0; i < 8; i++) {
                
                   if (rc.isActive()) {
                      MapLocation location_of_dir = rc.getLocation().add(dir);
 
                      if (rc.senseMine(location_of_dir) != null) {
                         rc.defuseMine(location_of_dir);
                         break;
                      } else {
 
                         if (rc.canMove(dir)) {
                            //rc.setIndicatorString(0, "Last direction moved:"+dir.toString());
                            //System.out.println("About to move" +  rc.getLocation().toString() + dir.toString() );
                            rc.move(dir);
                            //System.out.println("Moved" +  rc.getLocation().toString() + dir.toString() );
                            //rc.breakpoint();
                            i=8;
                            break;
                         } else {
                            if (rotating_left_on_cant_move) {
                               dir = dir.rotateLeft();
                            } else {
                               dir = dir.rotateRight();
                            }
                         }
                      }
                   }
                }
             }
          }catch (Exception e){
             System.out.println("Scout Exception");
             e.printStackTrace();
          }
          rc.yield();
       }
    }
 
 }
 
