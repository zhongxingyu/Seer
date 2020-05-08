 import lejos.nxt.*;
 
 class Movement{
 	
 	private Direction lastTurn = Direction.LEFT;
 	
 	/*
 	 * This method makes the robot drive FRONT or BACK depending on the direction it is given.
 	 */
 	public void drive(Direction direction){ 	
 		if(direction == Direction.FRONT){
 			Motor.A.FRONT();
 			Motor.B.FRONT();
 		}
 		else if(direction == Direction.BACK){
 			Motor.A.backward();
 			Motor.B.backward();
 		}
 	}
 	
 	/*
 	 * This method makes the robot stop.
 	 */
 	public void stop(){
 		Motor.A.stop();
 		Motor.B.stop();
 	}
 	
 	/*
 	 * This method makes the robot turn 90 deg to the given direction
 	 */
 	public void turnNXT(Direction direction){
 		if(direction == Direction.RIGHT){
 			Motor.A.rotate(237);
 			
 		}
 		else if(direction == Direction.LEFT){
 			Motor.B.rotate(237);
 		}
 	}
 	
 	/*
 	 * This is a basic turning mechanism depending on the direction it is given.
 	 * This method is not used!
 	 */
 	public void wallTurn(Direction fbDirection, Direction lrDirection) throws InterruptedException{
 		stop();
 		drive(fbDirection);
 		Thread.sleep(700);
 		turnNXT(lrDirection);
 		
 	}
 	
 	/*
 	 * This method keeps track of witch direction the robot turned last time.
 	 */
 	public void setLastTurn(Direction lastTurn){
 		this.lastTurn = lastTurn;
 	}
 	
 	public Direction getLastTurn(){
 		return this.lastTurn;
 	}
 	
	public Direction checkForObstacles(){
 		Direction turn;
 		boolean left = SonicSensor.isObject(Direction.LEFT);
 		boolean front = SonicSensor.isObject(Direction.FRONT);
 		boolean right = SonicSensor.isObject(Direction.RIGHT);
 		
 		if(!left && !right){
 			turn = getLastTurn(); 
 		}
 		else if(left && right){
 			turn = Direction.BACK;
 		}
 		else if(left && !right){
 			turn = Direction.RIGHT;
 		}
 		else if(!left && right){
 			turn = Direction.LEFT;
 		}
 		return turn;	
 	}
 	
 	public Direction nextTurn(Direction prevTurn){
 		Direction nextTurn = (prevTurn == Direction.LEFT)?Direction.RIGHT:Direction.LEFT;
 		return nextTurn;
 	}
 	
 	/*
 	 * This method is used to avoid objects.
 	 */
 	public void avoidObstacle(){
 		Direction temp = checkForObstacles();
 		Direction nextTurn = nextTurn(temp);
 		if(temp != null){
 			if(temp != Direction.BACK){
 				turnNXT(temp);// turns either right or left depending on the feedback from SonicSensor.checkForObstacles()
 				// Implement!: turn UltraSonic Sensor in the other direction left = right, right = left.
 				drive(Direction.FRONT);
 				Thread.sleep(1500);
 				if(checkForObstacles() != temp && checkForObstacles() != Direction.BACK){ // True if the robot is not cornered
 					turnNXT(nextTurn);// Turns the opposite direction so it is able to go around an obstacle(1st turn)
 					// Implement!: turn UltraSonic Sensor in the other direction left = right, right = left.
 					drive(Direction.FRONT);
 					Thread.sleep(1500);
 					if(checkForObstacles() != temp && checkForObstacles() != Direction.BACK){
 						turnNXT(nextTurn);// Turns it once more in the same direction(2nd turn)
 						// Implement!: turn UltraSonic Sensor in the other direction left = right, right = left.
 						drive(Direction.FRONT);
 						Thread.sleep(1500);
 						if(checkForObstacles() != nextTurn && checkForObstacles() != Direction.BACK){
 							turnNXT(temp);// Turns it in the opposite direction(3rd turn)
 						}
 						else{ // Turns 180 deg
 							turnNXT(temp);
 							turnNXT(temp);
 						}
 					}
					else if(SonicSensor.checkForObstacles() == temp){
 						turnNXT(temp);
 					}
 					else{ // Turns 180 deg
 						turnNXT(temp);
 						turnNXT(temp);
 					}
 				}
 				else{ // Turns 90 deg
 					turnNXT(temp);
 				}
 				
 			}
 			else{ // Turns 180 deg
 				turnNXT(temp);
 				turnNXT(temp);
 			}
 		}	
 	}
 }
