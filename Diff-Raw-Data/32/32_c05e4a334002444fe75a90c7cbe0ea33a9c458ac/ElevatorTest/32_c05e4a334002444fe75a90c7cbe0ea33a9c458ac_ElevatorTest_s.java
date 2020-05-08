 package com.bzn.codestory.elevator;
 
 import static com.bzn.codestory.elevator.Command.CLOSE;
 import static com.bzn.codestory.elevator.Command.DOWN;
 import static com.bzn.codestory.elevator.Command.NOTHING;
 import static com.bzn.codestory.elevator.Command.OPEN;
 import static com.bzn.codestory.elevator.Command.OPEN_DOWN;
 import static com.bzn.codestory.elevator.Command.OPEN_UP;
 import static com.bzn.codestory.elevator.Command.UP;
 import static org.fest.assertions.Assertions.assertThat;
 
 import org.junit.Before;
 import org.junit.Ignore;
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
 		checkNextCommandSomeTimes(UP, MIDDLE_FLOOR);
 		checkNextCommands(NOTHING);
 	}
 
 	@Test
 	public void should_go_to_real_middle_floor_when_no_calls_and_lower_floor_underground()
 			throws Exception {
 		elevator.reset(-2, 4, 30); /* middle floor should be 1 */
 		checkNextCommands(UP, NOTHING);
 	}
 
 	@Test
 	public void should_go_down_to_middle_floor_when_floor_11_and_no_calls()
 			throws Exception {
 		elevator.call(1, Direction.UP);
 		checkNextCommands(UP, OPEN_UP);
 		elevator.userEntered();
 		elevator.goTo(11);
 		checkNextCommands(CLOSE);
 		checkNextCommandSomeTimes(UP, 10);
 		checkNextCommands(OPEN);
 		elevator.userExited();
 		checkNextCommands(CLOSE, DOWN, NOTHING);
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
 		elevator.goTo(1);
 		elevator.userEntered();
 		elevator.goTo(2);
 		assertThat(elevator.usersInCabin()).isEqualTo(2);
 		checkNextCommands(CLOSE, UP, OPEN_UP);
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
 		checkNextCommands(CLOSE, UP, OPEN_UP);
 		elevator.userEntered();
 		elevator.goTo(3);
 		checkNextCommands(CLOSE, UP, OPEN_UP);
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
 		checkNextCommands(CLOSE, DOWN, OPEN_DOWN);
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
 		checkNextCommands(UP, OPEN_UP);
 		elevator.userEntered();
 		elevator.goTo(3);
 		checkNextCommands(CLOSE, UP, UP, OPEN);
 		elevator.userExited();
 		checkNextCommands(CLOSE, DOWN, OPEN_DOWN);
 		elevator.userEntered();
 		elevator.goTo(0);
 		checkNextCommands(CLOSE, DOWN, DOWN, OPEN);
 		elevator.userExited();
 		checkNextCommands(CLOSE);
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
 		checkNextCommands(UP, OPEN_UP);
 		elevator.userEntered();
 		elevator.goTo(2);
 		elevator.userEntered();
 		elevator.goTo(3);
 		checkNextCommands(CLOSE, UP, OPEN_UP);
 		elevator.userExited();
 		elevator.userEntered();
 		elevator.goTo(3);
 		checkNextCommands(CLOSE, UP, OPEN);
 	}
 
 	@Test
 	public void should_refuse_if_cabin_full() throws Exception {
 		elevator.reset(0, 3, 2);
 		elevator.call(1, Direction.UP);
 		elevator.call(1, Direction.UP);
 		elevator.call(2, Direction.UP);
 		checkNextCommands(UP, OPEN_UP);
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
 	public void should_skip_users_going_the_other_way() throws Exception {
 		elevator.reset(0, 3, 2);
 		elevator.call(1, Direction.UP);
 		elevator.call(2, Direction.DOWN);
 		elevator.call(2, Direction.DOWN);
 		elevator.call(3, Direction.DOWN);
 		checkNextCommands(UP, OPEN_UP);
 		elevator.userEntered();
 		elevator.goTo(2);
 		checkNextCommands(CLOSE, UP, OPEN_UP);
 		elevator.userExited();
 		checkNextCommands(CLOSE, UP, OPEN);
 		elevator.userEntered();
 		elevator.goTo(0);
 		checkNextCommands(CLOSE, DOWN, OPEN_DOWN);
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
 		checkNextCommands(UP, OPEN_UP);
 		elevator.userEntered();
 		elevator.goTo(2);
 		elevator.call(1, Direction.UP);
 		checkNextCommands(NOTHING);
 		elevator.userEntered();
 		elevator.goTo(2);
 		checkNextCommands(CLOSE);
 	}
 
 	@Test
 	public void should_change_direction_without_forgetting_users_at_floor() {
 		elevator.reset(0, 3, 5);
 		elevator.call(1, Direction.DOWN);
 		checkNextCommands(UP, OPEN);
 		elevator.userEntered();
 		elevator.goTo(0);
 		elevator.call(1, Direction.DOWN);
		checkNextCommands(NOTHING);
		elevator.userEntered();
		elevator.goTo(0);
 		checkNextCommands(CLOSE);
 	}
 
 	@Test
 	public void should_not_reopen_if_cabin_full() {
 		elevator.reset(0, 3, 2);
 		elevator.call(1, Direction.UP);
 		elevator.call(1, Direction.UP);
 		checkNextCommands(UP, OPEN_UP);
 		elevator.userEntered();
 		elevator.userEntered();
 		elevator.call(1, Direction.UP);
 		checkNextCommands(CLOSE);
 	}
 
 	@Test
 	public void should_detect_not_stop_to_take_users_if_cabin_full() {
 		elevator.reset(0, 3, 2);
 		makeSeveralCalls(1, Direction.UP, 2);
 		makeSeveralCalls(2, Direction.UP, 3);
 		makeSeveralCalls(3, Direction.DOWN, 10);
 		checkNextCommands(UP, OPEN_UP);
 		elevator.userEntered();
 		elevator.goTo(3);
 		elevator.userEntered();
 		elevator.goTo(3);
 		checkNextCommands(CLOSE, UP, UP, OPEN);
 	}
 
 	@Test
 	public void should_not_take_user_to_bottom_if_he_wants_to_go_up() {
 		elevator.reset(0, 5, 10);
 		elevator.call(4, Direction.UP);
 		checkNextCommands(UP, UP, UP, UP);
 		elevator.call(1, Direction.DOWN);
 		elevator.call(2, Direction.UP);
 		checkNextCommands(OPEN_UP);
 		elevator.userEntered();
 		elevator.goTo(5);
 		checkNextCommands(CLOSE, UP);
 	}
 
 	@Test
 	public void elevator_should_not_stay_at_one_floor_forever() {
 		elevator.reset(0, 5, 10);
 		elevator.call(0, Direction.UP);
 		checkNextCommands(OPEN);
 		elevator.userEntered();
 		elevator.goTo(4);
 		elevator.call(3, Direction.UP);
 		elevator.call(3, Direction.DOWN);
 		elevator.call(3, Direction.UP);
 		checkNextCommands(CLOSE, UP, UP, UP, OPEN_UP);
 		elevator.userEntered();
 		elevator.goTo(5);
 		elevator.userEntered();
 		elevator.goTo(5);
 		checkNextCommands(CLOSE, UP);
 	}
 
 	@Test
 	@Ignore("vip mode is not activated")
 	public void should_stop_only_to_pick_vips_or_to_drop_people_in_vip_mode() {
 		elevator.reset(0, 40, 6);
 		makeSeveralCalls(1, Direction.UP, 4);
 		elevator.call(35, Direction.DOWN);
 		checkNextCommands(UP, OPEN);
 		elevator.userEntered();
 		elevator.goTo(36);
 		elevator.userEntered();
 		elevator.goTo(36);
 		elevator.userEntered();
 		elevator.goTo(36);
 		elevator.userEntered();
 		elevator.goTo(36);
 		checkNextCommands(CLOSE);
 		checkNextCommandSomeTimes(UP, 35);
 		checkNextCommands(OPEN);/* étage 36 */
 		elevator.userExited();
 		elevator.userExited();
 		elevator.userExited();
 		elevator.userExited();
 	}
 
 	@Test
 	@Ignore("vip mode is not activated")
 	public void one_no_vip_calling_at_36_and_one_vip_calling_at_33_should_change_direction()
 			throws Exception {
 		elevator.reset(0, 37, 3);
 		makeSeveralCalls(1, Direction.UP, 2);
 		elevator.call(36, Direction.DOWN);
 		elevator.call(34, Direction.DOWN);
 		elevator.call(34, Direction.DOWN);
 		checkNextCommands(UP, OPEN);
 		elevator.userEntered();
 		elevator.goTo(34);
 		elevator.userEntered();
 		elevator.goTo(34);
 		checkNextCommands(CLOSE);
 		checkNextCommandSomeTimes(UP, 33);
 		checkNextCommands(OPEN);/* étage 34 */
 		elevator.call(33, Direction.DOWN);
 		elevator.call(33, Direction.DOWN);
 		elevator.userExited();
 		elevator.userExited();
 		elevator.userEntered();
 		elevator.goTo(1);
 		elevator.userEntered();
 		elevator.goTo(1);/* on repasse en rush mode... */
 		checkNextCommands(CLOSE, DOWN, OPEN); /* étage 33 */
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
 
 	private void makeSeveralCalls(int floor, Direction direction, int times) {
 		for (int i = 0; i < times; i++) {
 			elevator.call(floor, direction);
 		}
 	}
 }
