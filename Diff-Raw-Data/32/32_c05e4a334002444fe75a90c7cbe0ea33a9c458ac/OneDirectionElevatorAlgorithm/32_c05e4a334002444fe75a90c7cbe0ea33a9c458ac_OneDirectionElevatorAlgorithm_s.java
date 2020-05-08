 package com.bzn.codestory.elevator;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class OneDirectionElevatorAlgorithm extends ElevatorAlgorithm {
 
 	private Logger logger = LoggerFactory.getLogger(OneDirectionElevatorAlgorithm.class);
 
 	public OneDirectionElevatorAlgorithm(Elevator elevator) {
 		super(elevator);
 	}
 
 	@Override
 	public boolean shouldClose() {
 		if (getElevator().getCurrentDirection().isNil()) {
			return !getElevator().hasUsersCallingFromCurrentFloor();
 		} else {
 			return !getElevator().hasUsersCallingFromCurrentFloorGoingInSameDirection();
 		}
 	}
 
 	@Override
 	public boolean shouldOpen() {
 		if (shouldChangeDirection()) {
 			getElevator().setCurrentDirection(Direction.NIL);
 		}
 		return getElevator().hasUsersCallingFromCurrentFloorGoingInSameDirection() || getElevator().hasUsersGoingToCurrentFloor()
 				|| (getElevator().getCurrentDirection().isNil() && getElevator().hasUsersCallingFromCurrentFloor());
 	}
 
 	@Override
 	public boolean shouldChangeDirection() {
 		boolean shouldChange = false;
 		String reason = "";
 		if (!getElevator().hasUsersCallingFromCurrentFloorGoingInSameDirection()) {
 			if (getElevator().getCurrentDirection().isUp()) {
 				shouldChange = getElevator().countUsersAboveCurrentFloor() == 0;
 				reason = "no more users above";
 			}
 			if (getElevator().getCurrentDirection().isDown()) {
 				shouldChange = getElevator().countUsersBelowCurrentFloor() == 0;
 				reason = "no more users below";
 			}
 		}
 		if (shouldChange) {
 			logger.info("changing direction at floor {}: cause: {}", getElevator().currentFloor, reason);
 		}
 		return shouldChange;
 	}
 
 	@Override
 	public boolean shouldGoUp() {
 		if (getElevator().getCurrentDirection().isNil()) {
			return getElevator().countUsersBelowCurrentFloor() <= getElevator().countUsersAboveCurrentFloor();
 		}
 		return getElevator().getCurrentDirection().isUp();
 	}
 
 }
