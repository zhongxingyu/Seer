 package com.bzn.codestory.elevator;
 
 import static com.bzn.codestory.elevator.Command.CLOSE;
 import static com.bzn.codestory.elevator.Command.DOWN;
 import static com.bzn.codestory.elevator.Command.NOTHING;
 import static com.bzn.codestory.elevator.Command.UP;
 
 import java.util.SortedMap;
 
 import com.google.common.annotations.VisibleForTesting;
 import com.google.common.collect.Maps;
 
 public class Elevator {
 
 	private static final int DEFAULT_FLOORS = 20;
 
 	private static final int DEFAULT_CABIN_SIZE = 30;
 
 	int currentFloor;
 	private int cabinSize;
 	private int lower;
 	private int higher;
 	private Orders orders;
 	private boolean open;
 	private int usersInCabin;
 	private Direction currentDirection;
 	private SortedMap<Integer, Integer> frequencies;
 
 	private int currentTime;
 
 	public Elevator() {
 		this(0);
 	}
 
 	@VisibleForTesting
 	Elevator(int startFloor) {
 		reset(0, DEFAULT_FLOORS - 1, DEFAULT_CABIN_SIZE);
 		currentFloor = startFloor;
 	}
 
 	public void reset(int lower, int higher, int cabinSize) {
 		this.lower = lower;
 		this.higher = higher;
 		resetFrequencies(lower, higher);
 		currentFloor = 0;
 		this.cabinSize = cabinSize;
 		reset();
 	}
 
 	private void resetFrequencies(int lower, int higher) {
 		frequencies = Maps.newTreeMap();
 		for (int floor = lower; floor < higher + 1; floor++) {
 			frequencies.put(floor, 0);
 		}
 	}
 
 	private void reset() {
 		currentFloor = 0;
 		orders = new Orders(lower, higher);
 		open = false;
 		usersInCabin = 0;
 		currentDirection = Direction.NIL;
 		currentTime = 0;
 	}
 
 	public Command nextCommand() {
 		currentTime++;
 		if (open) {
 			if (shouldClose()) {
 				return close();
 			} else {
 				return doNothing();
 			}
 		}
 		if (shouldOpen()) {
 			return open();
 		} else if (orders.hasOrder()) {
 			if (shouldGoUp()) {
 				return up();
 			} else {
 				return down();
 			}
 		}
 		return idle();
 	}
 
 	private boolean shouldClose() {
 		return isCabinFull() || !orders.hasCallsFrom(currentFloor);
 	}
 
 	private boolean shouldGoUp() {
 		if (currentDirection.isNil()) {
 			if (isCabinFull()) {
 				return orders.countGoToBelow(currentFloor) <= orders
 						.countGoToAbove(currentFloor);
 			} else {
 				return orders.countOrdersBelow(currentFloor) <= orders
 						.countOrdersAbove(currentFloor);
 			}
 		}
 		return currentDirection.isUp();
 	}
 
 	private boolean shouldOpen() {
 		if (shouldChangeDirection()) {
 			currentDirection = Direction.NIL;
 		}
 		if (isCabinFull()) {
 			return orders.hasGoTo(currentFloor);
 		} else {
 			return orders.hasOrderTo(currentFloor, currentDirection)
 					|| (currentDirection.isNil() && orders
 							.hasOrderTo(currentFloor));
 		}
 	}
 
 	private boolean shouldChangeDirection() {
 		boolean shouldChange = true;
 		if (currentDirection.isUp()) {
 			if (isCabinFull()) {
 				shouldChange = orders.countGoToAbove(currentFloor) == 0;
 			} else {
 				shouldChange = orders.countOrdersAbove(currentFloor) == 0;
 			}
 		}
 		if (currentDirection.isDown()) {
 			shouldChange = orders.countOrdersBelow(currentFloor) == 0;
 		}
 		return shouldChange;
 	}
 
 	private boolean isCabinFull() {
 		return usersInCabin == cabinSize;
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
 		return Command.OPEN;
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
		return frequencies.size() / 2;
 	}
 
 	private Command doNothing() {
 		currentDirection = Direction.NIL;
 		return NOTHING;
 	}
 
 	public void call(int floor, Direction direction) {
 		orders.add(new Call(floor, direction, currentTime));
 		incrementFrequency(floor);
 	}
 
 	public void goTo(int floor) {
 		Direction dir = Direction.UP;
 		if (floor < currentFloor) {
 			dir = Direction.DOWN;
 		}
 		orders.add(new GoTo(floor, dir));
 	}
 
 	public void userEntered() {
 		usersInCabin++;
 		orders.forgetOldestCallAtFloor(currentFloor);
 	}
 
 	public int usersInCabin() {
 		return usersInCabin;
 	}
 
 	public void userExited() {
 		usersInCabin--;
 		orders.forgetOldestGoToAtFloor(currentFloor);
 	}
 
 	public Integer[] getFrequencies() {
 		if (frequencies == null) {
 			resetFrequencies(0, DEFAULT_FLOORS);
 		}
 		return frequencies.values().toArray(new Integer[0]);
 	}
 
 	private void incrementFrequency(int floor) {
 		frequencies.put(floor, frequencies.get(floor) + 1);
 	}
 
 	public ElevatorStatus getStatus() {
 		return new ElevatorStatus(currentFloor, usersInCabin, cabinSize, open,
 				currentDirection, frequencies);
 	}
 
 	public int getCurrentTime() {
 		return currentTime;
 	}
 
 }
