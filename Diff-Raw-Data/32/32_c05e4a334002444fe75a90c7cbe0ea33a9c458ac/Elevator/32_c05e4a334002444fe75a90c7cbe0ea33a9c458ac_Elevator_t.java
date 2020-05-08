 package com.bzn.codestory.elevator;
 
 import static com.bzn.codestory.elevator.Command.CLOSE;
 import static com.bzn.codestory.elevator.Command.DOWN;
 import static com.bzn.codestory.elevator.Command.NOTHING;
 import static com.bzn.codestory.elevator.Command.UP;
 
 import java.util.List;
 
 import com.google.common.annotations.VisibleForTesting;
 
 public class Elevator {
 
 	private static final int DEFAULT_FLOORS = 20;
 
 	private static final int DEFAULT_CABIN_SIZE = 30;
 
 	int currentFloor;
 	private int cabinSize;
 	private int lower;
 	private int higher;
 	private Users users;
 	private boolean open;
 	private Direction currentDirection;
 
 	private CrowdedElevatorAlgorithm crowdedElevatorAlgorithm;
 	private OneDirectionElevatorAlgorithm oneDirectionElevatorAlgorithm;
 	private VipElevatorAlgorithm vipElevatorAlgorithm;
 
 	private int currentTime;
 
 	public Elevator() {
 		this(0);
 	}
 
 	@VisibleForTesting
 	Elevator(int startFloor) {
 		reset(0, DEFAULT_FLOORS - 1, DEFAULT_CABIN_SIZE);
 		currentFloor = startFloor;
 		crowdedElevatorAlgorithm = new CrowdedElevatorAlgorithm(this);
 		oneDirectionElevatorAlgorithm = new OneDirectionElevatorAlgorithm(this);
 		vipElevatorAlgorithm = new VipElevatorAlgorithm(this);
 	}
 
 	public void reset(int lower, int higher, int cabinSize) {
 		this.lower = lower;
 		this.higher = higher;
 		currentFloor = 0;
 		this.cabinSize = cabinSize;
 		reset();
 	}
 
 	private void reset() {
 		currentFloor = 0;
 		users = new Users(lower, higher);
 		open = false;
 		currentDirection = Direction.NIL;
 		currentTime = 0;
 	}
 
 	public Command nextCommand() {
 		ElevatorAlgorithm algorithm = chooseAlgorithm();
 		increaseCurrentTime();
 		if (open) {
 			if (algorithm.shouldClose()) {
 				return close();
 			} else {
 				return doNothing();
 			}
 		}
 		if (algorithm.shouldOpen()) {
 			return open();
 		} else if (users.hasOrder()) {
 			if (algorithm.shouldGoUp()) {
 				return up();
 			} else {
 				return down();
 			}
 		}
 		return idle();
 	}
 
 	private void increaseCurrentTime() {
 		currentTime++;
 	}
 
 	private ElevatorAlgorithm chooseAlgorithm() {
 		if (isCabinFull()) {
 			return crowdedElevatorAlgorithm;
 		} else {
 			return oneDirectionElevatorAlgorithm;
 		}
 	}
 
 	private boolean rushInCabin() {
 		return users.countUsersInCabin() > cabinSize / 3;
 	}
 
 	private boolean isCabinFull() {
 		return users.countUsersInCabin() == cabinSize;
 	}
 
 	private Command down() {
 		currentFloor--;
 		currentDirection = Direction.DOWN;
 		return DOWN;
 	}
 
 	private Command up() {
 		currentFloor++;
 		currentDirection = Direction.UP;
 		return UP;
 	}
 
 	private Command close() {
 		open = false;
 		return CLOSE;
 	}
 
 	private Command open() {
 		open = true;
 		return Command.open(currentDirection);
 	}
 
 	private Command idle() {
 		Command idleCommand = doNothing();
 		if (currentFloor > getMiddleFloor()) {
 			idleCommand = down();
 		} else if (currentFloor < getMiddleFloor()) {
 			idleCommand = up();
 		}
 		return idleCommand;
 	}
 
 	private int getMiddleFloor() {
 		return lower + Math.abs(higher - lower + 1) / 2;
 	}
 
 	private Command doNothing() {
 		currentDirection = Direction.NIL;
 		return NOTHING;
 	}
 
 	public void call(int floor, Direction direction) {
 		users.userCalls(new Call(floor, direction), currentTime);
 	}
 
 	public void goTo(int floor) {
 		Direction dir = Direction.UP;
 		if (floor < currentFloor) {
 			dir = Direction.DOWN;
 		}
 		users.userWantsToGoTo(new GoTo(floor, dir), currentFloor);
 	}
 
 	public void userEntered() {
 		users.enter(currentFloor, currentDirection, currentTime);
 	}
 
 	public void userExited() {
 		users.userExitsAt(currentFloor);
 	}
 
 	public ElevatorStatus getStatus() {
 		return new ElevatorStatus(currentFloor, usersInCabin(), cabinSize, open, currentDirection, users);
 	}
 
 	public int getCurrentTime() {
 		return currentTime;
 	}
 
 	public Direction getCurrentDirection() {
 		return currentDirection;
 	}
 
 	public Users getUsers() {
 		return users;
 	}
 
 	public int getLower() {
 		return lower;
 	}
 
 	public int getHigher() {
 		return higher;
 	}
 
 	public void setCurrentDirection(Direction direction) {
 		this.currentDirection = direction;
 	}
 
 	private int getRemainingPlacesInCabin() {
 		return cabinSize - users.countUsersInCabin();
 	}
 
 	public int getPotentialScoreForCurrentFloor() {
 		return getPotentialScoreForFloor(currentFloor);
 	}
 
 	public int getPotentialScoreForFloor(int floor) {
 		List<User> usersToInspect = users.getUsersThatCanBeTakenInCabinAtFloor(floor, getRemainingPlacesInCabin());
 		int floorPotentialScore = 0;
 		for (User user : usersToInspect) {
 			floorPotentialScore = floorPotentialScore
 					+ user.getPotentialMaxScore(currentFloor, currentDirection, lower, higher, currentTime);
 		}
 		return floorPotentialScore;
 	}
 
 	public boolean isAtTop() {
 		return currentFloor == higher;
 	}
 
 	public boolean isAtBottom() {
 		return currentFloor == lower;
 	}
 
 	public int usersInCabin() {
 		return users.countUsersInCabin();
 	}
 
 	public boolean hasUsersCallingFromCurrentFloor() {
 		return users.hasUsersCallingFrom(currentFloor);
 	}
 
 	public boolean hasUsersCallingFromCurrentFloorGoingInSameDirection() {
 		return users.hasUsersCallingFrom(currentFloor, currentDirection);
 	}
 
 	public boolean hasUsersGoingToCurrentFloor() {
 		return users.hasUsersGoingTo(currentFloor);
 	}
 
 	public int countUsersAboveCurrentFloor() {
 		return users.countUsersAbove(currentFloor);
 	}
 
 	public int countUsersBelowCurrentFloor() {
 		return users.countUsersBelow(currentFloor);
 	}
 
	public int countUsersGoingAbove() {
		return users.countGoToAbove(currentFloor);
	}

	public int countUsersGoingBelow() {
		return users.countGoToBelow(currentFloor);
	}

 }
