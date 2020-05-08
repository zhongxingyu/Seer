 package org.crowdball;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertTrue;
 
 import org.joda.time.DateTimeUtils;
 import org.junit.After;
 import org.junit.Test;
 
 /**
  *                         |
  *             A           |           B
  *                         |
  *                         |
  * -10---------------------0--------------------10
  * 
  */
 public class BallTest {
 	
 	@Test
 	public void ball_moves_when_hit() {
 		Ball ball = new Ball();
 		ball.hitRecievedFromA();
 		assertFalse(ball.speed() == 0);
 	}
 	
 	@Test
 	public void ball_changes_direction_when_hit() {
 		Ball ball = new Ball();
 		ball.hitRecievedFromB();
 		assertTrue(ball.speed() < 0);
 	}
 	
 	@Test
 	public void start_game() throws Exception {
 		Ball ball = new Ball();
 		
 		ball.resetAndStart();
 		
 		assertEquals(0, ball.position());
 		assertEquals(-Ball.SINGLE_HIT, ball.speed());
 	}
 	
 	@Test
 	public void one_second_old_game_should_have_ball_in_correct_position() throws Exception {
 		DateTimeUtils.setCurrentMillisFixed(0);
 		Ball ball = new Ball();
 		
 		ball.resetAndStart();
 		DateTimeUtils.setCurrentMillisFixed(1000);
 		
 		assertEquals(-1, ball.position());		
 	}
 	
 	@Test
 	public void hundred_second_old_game_should_have_ball_in_correct_position() throws Exception {
 		DateTimeUtils.setCurrentMillisFixed(0);
 		Ball ball = new Ball();
 		
 		ball.resetAndStart();
 		DateTimeUtils.setCurrentMillisFixed(100000);
 		
 		assertEquals(-100, ball.position());		
 	}
 
 	@After
     public void unfreeze() {
         DateTimeUtils.setCurrentMillisSystem();
     }
 	
 	@Test
 	public void should_stop_when_hit_and_speed_is_single_hit() throws Exception {
 		Ball ball = new Ball();
 		
 		ball.resetAndStart();
 		ball.hitRecievedFromA();
 		
 		assertEquals(0, ball.speed());
 	}
 	
 	@Test
	public void should_not_increase_speed_beyound_max() throws Exception {
 		Ball ball = new Ball();
 		
 		ball.setPosition(-5); // On side B
 		ball.setSpeed(-Ball.SINGLE_HIT); //Ball traveling leftwards  at a speed of one.
 		
 		for(int i = 0; i < 21; i++) {
 			ball.hitRecievedFromA();
 		}
 		
		assertEquals(Ball.MAX_SPEED, ball.speed() );		
 	}
 	
 	@Test
	public void should_not_decrease_speed_beyound_max() throws Exception {
 		Ball ball = new Ball();
 		ball.setPosition(5);
 		ball.setSpeed(Ball.SINGLE_HIT);
 		for(int i = 0; i < 21; i++) {
 			ball.hitRecievedFromB();
 		}
		assertEquals(-Ball.MAX_SPEED, ball.speed());		
 	}
 	
 	@Test
 	public void ball_should_be_game_over_when_it_hits_wall() throws Exception {
 		DateTimeUtils.setCurrentMillisFixed(0);
 		Ball ball = new Ball();
 		ball.setPosition(Ball.ROOM_SIZE - 1);
 		ball.setSpeed(Ball.SINGLE_HIT*10);
 		DateTimeUtils.setCurrentMillisFixed(1000);
 		System.out.print(ball.position());
 		assertTrue(ball.gameOver());
 		
 	}
 	
 }
