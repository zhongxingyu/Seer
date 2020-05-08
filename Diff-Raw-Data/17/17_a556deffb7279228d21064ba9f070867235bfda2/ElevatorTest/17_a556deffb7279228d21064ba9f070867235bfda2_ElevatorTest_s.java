 package com.bzn.codestory.elevator;
 
 import static com.bzn.codestory.elevator.Command.CLOSE;
 import static com.bzn.codestory.elevator.Command.DOWN;
 import static com.bzn.codestory.elevator.Command.NOTHING;
 import static com.bzn.codestory.elevator.Command.OPEN;
 import static com.bzn.codestory.elevator.Command.UP;
 import static org.fest.assertions.Assertions.assertThat;
 
 import org.junit.Before;
 import org.junit.Test;
 
 public class ElevatorTest {
 
 	private Elevator elevator;
 	private static final int MIDDLE_FLOOR = 10;
 
 	@Before
 	public void setUp() {
 		elevator = new Elevator();
 	}
 
 	@Test
 	public void should_stay_idle_when_no_stimulus() throws Exception {
 		checkNextCommandSomeTimes(UP, MIDDLE_FLOOR);
 		checkNextCommands(NOTHING);
 	}
 
 	@Test
 	public void should_open_doors_if_call_at_initial_floor() throws Exception {
 		elevator.call(0, Direction.UP);
 		checkNextCommands(OPEN);
 	}
 
 	@Test
 	public void should_close_doors_if_opened() throws Exception {
 		elevator.call(0, Direction.UP);
 		checkNextCommands(OPEN);
 		elevator.userEntered();
 		checkNextCommands(CLOSE);
 	}
 
 	@Test
 	public void should_go_to_middle_floor_when_no_calls() throws Exception {
 		elevator.call(0, Direction.UP);
 		checkNextCommands(OPEN);
 		elevator.userEntered();
 		checkNextCommands(CLOSE);
 		checkNextCommandSomeTimes(UP, MIDDLE_FLOOR);
 		checkNextCommands(NOTHING);
 	}
 
 	@Test
 	public void should_go_down_to_middle_floor_when_floor_11_and_no_calls()
 			throws Exception {
 		elevator.call(12, Direction.DOWN);
 		checkNextCommandSomeTimes(UP, 12);
 		checkNextCommands(OPEN);
 		elevator.userEntered();
 		checkNextCommands(CLOSE, DOWN, DOWN, NOTHING);
 	}
 
 	@Test
 	public void oneUser_calling_at_0_and_going_to_1() throws Exception {
 		elevator.call(0, Direction.UP);
 		checkNextCommands(OPEN);
 		elevator.userEntered();
 		assertThat(elevator.usersInCabin()).isEqualTo(1);
 		elevator.goTo(1);
 		checkNextCommands(CLOSE, UP, OPEN);
 		elevator.userExited();
 		assertThat(elevator.usersInCabin()).isEqualTo(0);
 		checkNextCommands(CLOSE);
 		checkNextCommandSomeTimes(UP, MIDDLE_FLOOR - 1);
 	}
 
 	@Test
 	public void oneUser_calling_at_0_and_going_to_2() throws Exception {
 		elevator.call(0, Direction.UP);
 		checkNextCommands(OPEN);
 		elevator.userEntered();
 		assertThat(elevator.usersInCabin()).isEqualTo(1);
 		elevator.goTo(2);
 		checkNextCommands(CLOSE, UP, UP, OPEN);
 		elevator.userExited();
 		assertThat(elevator.usersInCabin()).isEqualTo(0);
 		checkNextCommands(CLOSE);
 	}
 
 	@Test
 	public void oneUser_calling_at_1_and_going_to_0() throws Exception {
 		elevator.call(1, Direction.DOWN);
 		checkNextCommands(UP, OPEN);
 		elevator.userEntered();
 		assertThat(elevator.usersInCabin()).isEqualTo(1);
 		elevator.goTo(0);
 		checkNextCommands(CLOSE, DOWN, OPEN);
 		elevator.userExited();
 		assertThat(elevator.usersInCabin()).isEqualTo(0);
 		checkNextCommands(CLOSE);
 	}
 
 	@Test
 	public void oneUser_calling_at_2_with_elevator_at_three_and_going_to_five()
 			throws Exception {
 		elevator = new Elevator(3);
 		elevator.call(2, Direction.UP);
 		checkNextCommands(DOWN, OPEN);
 		elevator.userEntered();
 		assertThat(elevator.usersInCabin()).isEqualTo(1);
 		elevator.goTo(5);
 		checkNextCommands(CLOSE, UP, UP, UP, OPEN);
 		elevator.userExited();
 		assertThat(elevator.usersInCabin()).isEqualTo(0);
 		checkNextCommands(CLOSE);
 	}
 
 	@Test
 	public void twoUsers_calling_at_0_and_going_to_1_and_2() throws Exception {
 		elevator.call(0, Direction.UP);
 		elevator.call(0, Direction.UP);
 		checkNextCommands(OPEN);
 		elevator.userEntered();
 		elevator.userEntered();
 		assertThat(elevator.usersInCabin()).isEqualTo(2);
 		elevator.goTo(1);
 		elevator.goTo(2);
 		checkNextCommands(CLOSE, UP, OPEN);
 		elevator.userExited();
 		assertThat(elevator.usersInCabin()).isEqualTo(1);
 		checkNextCommands(CLOSE, UP, OPEN);
 		elevator.userExited();
 		assertThat(elevator.usersInCabin()).isEqualTo(0);
 		checkNextCommands(CLOSE);
 	}
 
 	@Test
 	public void twoUsers_calling_at_0_and_1_and_going_to_2_and_3()
 			throws Exception {
 		elevator.call(0, Direction.UP);
 		elevator.call(1, Direction.UP);
 		checkNextCommands(OPEN);
 		elevator.userEntered();
 		elevator.goTo(2);
 		checkNextCommands(CLOSE, UP, OPEN);
 		elevator.userEntered();
 		elevator.goTo(3);
 		checkNextCommands(CLOSE, UP, OPEN);
 		elevator.userExited();
 		checkNextCommands(CLOSE, UP, OPEN);
 		elevator.userExited();
 		checkNextCommands(CLOSE);
 	}
 
 	@Test
 	public void oneUser_calling_at_0_and_one_user_calling_down_at_two()
 			throws Exception {
 		elevator.call(0, Direction.UP);
 		elevator.call(2, Direction.DOWN);
 		checkNextCommands(OPEN);
 		elevator.userEntered();
 		elevator.goTo(3);
 		checkNextCommands(CLOSE, UP, UP, UP, OPEN);
 		elevator.userExited();
 		checkNextCommands(CLOSE, DOWN, OPEN);
 		elevator.userEntered();
 		elevator.goTo(0);
 		checkNextCommands(CLOSE, DOWN, DOWN, OPEN);
 		elevator.userExited();
 		checkNextCommands(CLOSE);
 	}
 
 	@Test
 	public void oneUser_calling_at_1_and_one_user_calling_down_at_two()
 			throws Exception {
 		elevator.call(1, Direction.UP);
 		elevator.call(2, Direction.DOWN);
 		checkNextCommands(UP, OPEN);
 		elevator.userEntered();
 		elevator.goTo(3);
 		checkNextCommands(CLOSE, UP, UP, OPEN);
 		elevator.userExited();
 		checkNextCommands(CLOSE, DOWN, OPEN);
 		elevator.userEntered();
 		elevator.goTo(0);
 		checkNextCommands(CLOSE, DOWN, DOWN, OPEN);
 		elevator.userExited();
 		checkNextCommands(CLOSE);
 	}
 
 	@Test
 	public void should_increment_frequencies() throws Exception {
 		// Not a magic number
 		final int numberOfCalls = 42;
 		final int floor = 3;
 		final Direction direction = Direction.DOWN;
 
 		assertThat(elevator.getFrequencies()[floor]).isZero();
 
 		for (int callCounter = 0; callCounter < numberOfCalls; callCounter++) {
 			elevator.call(floor, direction);
 		}
 		assertThat(elevator.getFrequencies()[floor]).isEqualTo(numberOfCalls);
 
 		final int newFloors = 7;
 		final int newCabinSize = 5;
 		elevator.reset(0, newFloors - 1, newCabinSize);
 
 		assertThat(elevator.getFrequencies()[floor]).isZero();
 
 	}
 
 	@Test
 	public void should_go_6ft_under() throws Exception {
 		elevator.reset(-6, 6, 6);
 		elevator.call(-6, Direction.UP);
 		checkNextCommands(DOWN, DOWN, DOWN, DOWN, DOWN, DOWN, OPEN);
 	}
 
 	@Test
 	public void should_open_for_safety_reasons() throws Exception {
 		elevator.reset(0, 3, 2);
 		elevator.call(1, Direction.UP);
 		elevator.call(1, Direction.UP);
 		elevator.call(2, Direction.UP);
 		checkNextCommands(UP, OPEN);
 		elevator.userEntered();
 		elevator.userEntered();
 		elevator.goTo(2);
 		elevator.goTo(3);
 		checkNextCommands(CLOSE, UP, OPEN);
 		elevator.userExited();
 		elevator.userEntered();
 		checkNextCommands(CLOSE, UP, OPEN);
 	}
 
 	@Test
 	public void should_refuse_if_cabin_full() throws Exception {
 		elevator.reset(0, 3, 2);
 		elevator.call(1, Direction.UP);
 		elevator.call(1, Direction.UP);
 		elevator.call(2, Direction.UP);
 		checkNextCommands(UP, OPEN);
 		elevator.userEntered();
 		elevator.userEntered();
 		elevator.goTo(3);
 		elevator.goTo(3);
 		checkNextCommands(CLOSE, UP, UP, OPEN);
 		elevator.userExited();
 		elevator.userExited();
 		checkNextCommands(CLOSE, DOWN, OPEN);
 	}
 
 	@Test
 	public void should_change_direction_when_cabin_full_and_no_GoTo_above()
 			throws Exception {
 		elevator.reset(0, 3, 2);
 		elevator.call(1, Direction.UP);
 		elevator.call(2, Direction.DOWN);
 		elevator.call(2, Direction.DOWN);
 		elevator.call(3, Direction.DOWN);
 		checkNextCommands(UP, OPEN);
 		elevator.userEntered();
 		elevator.goTo(2);
 		checkNextCommands(CLOSE, UP, OPEN);
 		elevator.userExited();
 		elevator.userEntered();
 		elevator.userEntered();
 		elevator.goTo(0);
 		elevator.goTo(0);
 		checkNextCommands(CLOSE, DOWN, DOWN, OPEN);
 	}
 
 	@Test
 	public void should_not_close_doors_in_the_nose_of_users() {
 		elevator.reset(0, 3, 5);
 		elevator.call(1, Direction.UP);
 		checkNextCommands(UP, OPEN);
 		elevator.userEntered();
 		elevator.call(1, Direction.DOWN);
 		checkNextCommands(NOTHING);
 		elevator.userEntered();
 		checkNextCommands(CLOSE);
 	}
 
 	private void checkNextCommandSomeTimes(Command command, int times) {
 		for (int i = 0; i < times; i++) {
 			checkNextCommands(command);
 		}
 	}
 
 	private void checkNextCommands(Command... commands) {
 		for (Command expected : commands) {
 			assertThat(elevator.nextCommand()).isEqualTo(expected);
 		}
 	}
 }
