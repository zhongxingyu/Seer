 package model;
 
 import static org.junit.Assert.*;
 
 import java.awt.Dimension;
 
 import java.awt.geom.Point2D;
 import java.awt.geom.Point2D.Double;
 import java.util.ArrayList;
 
 import org.junit.Before;
 import org.junit.Test;
 
 public class GameTest {
 	Game game;
 	Point2D PLAYERSMOVEMENT;
 	Dimension RESOLUTION;
 
 	@Before
 	public void setUp() {
 		String playername = "player1";
 		PLAYERSMOVEMENT = new Point2D.Double();
 		RESOLUTION = new Dimension(840, 460);
 		game = new Game(playername, RESOLUTION);
 	}
 
 	@Test
 	public void test_that_at_level_zero_there_are_no_asteroids() {
 		assertEquals(new ArrayList<SpaceObject>(), game.objects);
 	}
 
 	@Test
 	public void test_that_at_level_one_there_are_two_asteroids() {
 		game.objects = new ArrayList<SpaceObject>();
 		assertEquals(0, game.level);
 		game.nextLevel();
 
 		assertEquals(1, game.level);
 		assertEquals(2, game.objects.size());
 	}
 
 	@Test
 	public void test_that_at_level_two_there_are_four_asteroids() {
 		game.nextLevel();
 		game.objects = new ArrayList<SpaceObject>();
 
 		assertEquals(1, game.level);
 		game.nextLevel();
 
 		assertEquals(2, game.level);
 		assertEquals(4, game.objects.size());
 	}
 
 	@Test
 	public void test_that_two_generated_asteroids_dont_collide() {
 		assertEquals(0, game.level);
 		game.nextLevel();
 		assertEquals(1, game.level);
 
 		assertFalse(game.objects.get(0).overlap(game.objects.get(1)));
 	}
 
 	@Test
 	public void test_that_at_level_one_two_points_are_to_be_gained() {
 		assertEquals(0, game.level);
 		game.objects = new ArrayList<SpaceObject>();
 		game.nextLevel();
 		assertEquals(1, game.level);
 
 		assertEquals(2, game.objects.size());
 		assertEquals(2, game.getTotalPoints());
 	}
 
 	@Test
 	public void test_that_at_level_two_four_points_are_to_be_gained() {
 		assertEquals(0, game.level);
 		game.objects = new ArrayList<SpaceObject>();
 		game.setLevel(2);
 		assertEquals(2, game.level);
 
 		assertEquals(4, game.objects.size());
 		assertEquals(4, game.getTotalPoints());
 	}
 
 	@Test
 	public void test_that_at_level_three_eight_points_are_to_be_gained() {
 		assertEquals(0, game.level);
 		game.objects = new ArrayList<SpaceObject>();
 		game.setLevel(3);
 		assertEquals(3, game.level);
 
 		assertEquals(6, game.objects.size());
 		assertEquals(8, game.getTotalPoints());
 	}
 
 	@Test
 	public void test_that_get_Asteroids_Count_returns_0_if_there_are_no_asteroids() {
 		game.objects = new ArrayList<SpaceObject>();
 
 		assertEquals(0, game.getAsteroidCount());
 
 	}
 
 	@Test
 	public void test_that_get_Asteroids_Count_returns_the_count_of_asteroids_if_there_are_any() {
 		game.objects = new ArrayList<SpaceObject>();
 		game.nextLevel();
 
 		assertEquals(game.objects.size(), game.getAsteroidCount());
 	}
 
 	@Test
 	public void test_that_get_Asteroids_Count_returns_the_count_of_asteroids_and_ignores_shots() {
 		game.objects = new ArrayList<SpaceObject>();
 		game.nextLevel();
 		game.objects.add(new Shot(new Point2D.Double(), new Point2D.Double()));
 		game.objects.add(new Shot(new Point2D.Double(), new Point2D.Double()));
 
 		assertEquals(game.objects.size() - 2, game.getAsteroidCount());
 	}
 
 	@Test
 	public void test_that_run_moves_the_objects() {
 		game.objects = new ArrayList<SpaceObject>();
		Point2D oldPlace = new Point2D.Double(100, 100);
		game.objects.add(new Asteroid((Point2D) oldPlace.clone(), 10, new Point2D.Double(10, 10)));
 
 		game.run(0, false, false);
 
		assertNotSame(oldPlace.getX(), game.objects.get(0).getPosition().getX());
		assertNotSame(oldPlace.getY(), game.objects.get(0).getPosition().getY());
 	}
 
 	@Test
 	public void test_that_run_moves_the_player_if_forward_is_true() {
 		game.objects = new ArrayList<SpaceObject>();
 		game.player = new Player("Player", new Point2D.Double(100, 100));
 
 		game.run(0, true, false);
 
 		assertNotSame(new Point2D.Double(100, 100), game.player.getPosition());
 	}
 
 	@Test
 	public void test_that_run_lets_the_player_on_place_if_he_has_no_direction() {
 		game.objects = new ArrayList<SpaceObject>();
 		game.player = new Player("Player", new Point2D.Double(100, 100));
 
 		game.run(0, false, false);
 
 		assertEquals(new Point2D.Double(100, 100), game.player.getPosition());
 	}
 
 	@Test
 	public void test_that_run_increments_the_level_if_no__more_Asteroids_are_available() {
 		game.objects = new ArrayList<SpaceObject>();
 		int lastLevel = game.level;
 
 		game.run(0, false, false);
 
 		assertEquals(lastLevel + 1, game.level);
 	}
 
 	@Test
 	public void test_that_run_generates_new_Asteroids_if_there_are_none() {
 		game.objects = new ArrayList<SpaceObject>();
 
 		game.run(0, false, false);
 
 		assertTrue(game.objects.size() > 0);
 	}
 
 	@Test
 	public void test_that_a_collision_between_asteroids_causes_a_bounce() {
 		game.objects = new ArrayList<SpaceObject>();
 		Asteroid asteroid1 = new Asteroid(new Point2D.Double(100, 100), 10, new Point2D.Double(10, 10));
 		Asteroid asteroid2 = new Asteroid(new Point2D.Double(114, 114), 10, new Point2D.Double(-10, -10));
 		game.objects.add(asteroid1);
 		game.objects.add(asteroid2);
 		assertTrue(asteroid1.overlap(asteroid2));
 
 		game.run(0, false, false);
 
 		assertFalse(asteroid1.overlap(asteroid2));
 	}
 
 	@Test
 	public void test_that_a_collision_between_asteroid_and_player_causes_a_gameOverException() {
 		game.player.position = new Point2D.Double(100, 100);
 		game.objects.add(new Asteroid(new Point2D.Double(111, 111), 10, new Point2D.Double(-10, -10)));
 
 		try {
 			game.run(0, false, false);
 			fail("There should be thrown a gameoverexception");
 		}
 		catch (RuntimeException ex) {
 			assertEquals(GameOverException.class, ex.getClass());
 			assertEquals(String.valueOf(game.score), ex.getMessage());
 		}
 	}
 
 	@Test
 	public void test_that_a_collision_between_shot_and_player_causes_a_gameOverException() {
 		game.player.setDirection(new Point2D.Double(10, 10)).position = new Point2D.Double(100, 100);
 		game.objects.add(new Shot(new Point2D.Double(111, 111), new Point2D.Double(-10, -10)));
 
 		try {
 			game.run(0, false, false);
 			fail("There should be thrown a gameoverexception");
 		}
 		catch (RuntimeException ex) {
 			assertEquals(GameOverException.class, ex.getClass());
 			assertEquals(String.valueOf(game.score), ex.getMessage());
 		}
 	}
 
 	@Test
 	public void test_that_a_collision_between_asteroid_and_shot_destroys_both() {
 		Shot shot = new Shot(new Point2D.Double(100, 100), new Point2D.Double(10, 10));
 		Asteroid asteroid = new Asteroid(new Point2D.Double(119, 119), 10, new Point2D.Double(-10, -10));
 		game.objects.add(shot);
 		game.objects.add(asteroid);
 
 		assertEquals(2, game.objects.size());
 		game.run(0, false, false);
 
 		assertFalse(game.objects.contains(shot));
 		assertFalse(game.objects.contains(asteroid));
 	}
 
 	@Test
 	public void test_that_a_collision_between_asteroid_and_shot_increases_the_score() {
 		game.objects.add(new Shot(new Point2D.Double(100, 100), new Point2D.Double(10, 10)));
 		Asteroid asteroid = new Asteroid(new Point2D.Double(121, 121), 10, new Point2D.Double(-10, -10));
 		game.objects.add(asteroid);
 		int previousScore = game.score;
 		int thisScore = asteroid.getPoints();
 
 		game.run(0, false, false);
 
 		assertEquals(thisScore + previousScore, game.score);
 	}
 
 	@Test
 	public void test_that_a_shot_which_collides_with_borders_disappears() {
 		game.objects = new ArrayList<SpaceObject>();
 		Shot shot = new Shot(new Point2D.Double(), new Point2D.Double(1, 1));
 		game.objects.add(shot);
 
 		assertTrue(shot.getBorders(RESOLUTION).contains(true));
 		game.run(0, false, false);
 
 		assertEquals(game.getAsteroidCount(), game.objects.size());
 	}
 
 	@Test
 	public void test_that_a_shot_which_collides_with_borders_doesnt_increase_the_score() {
 		game.objects = new ArrayList<SpaceObject>();
 		Shot shot = new Shot(new Point2D.Double(), new Point2D.Double(1, 1));
 		game.objects.add(shot);
 		game.score = 0;
 
 		assertTrue(shot.getBorders(RESOLUTION).contains(true));
 		game.run(0, false, false);
 
 		assertEquals(0, game.score);
 	}
 
 	@Test
 	public void test_that_launch_creates_a_new_shot() {
 		game.objects = new ArrayList<SpaceObject>();
 
 		game.launchAShot();
 
 		assertEquals(1, game.objects.size() - game.getAsteroidCount());
 	}
 
 	@Test
 	public void test_that_launch_creates_a_new_shot_which_is_aimed_in_the_players_direction_with_direction_top() {
 		game.player.alignment = 0;
 
 		game.launchAShot();
 		SpaceObject object = game.objects.get(0);
 
 		assertEquals(new Point2D.Double(0, -10), object.direction);
 	}
 
 	@Test
 	public void test_that_launch_creates_a_new_shot_which_is_aimed_in_the_players_direction_with_direction_right() {
 		game.player.alignment = 90;
 
 		game.launchAShot();
 		SpaceObject object = game.objects.get(0);
 
 		assertEquals(new Point2D.Double(10, 0), roundPoint(object.direction));
 	}
 
 	@Test
 	public void test_that_launch_creates_a_new_shot_which_is_aimed_in_the_players_direction_with_direction_bottom() {
 		game.player.alignment = 180;
 
 		game.launchAShot();
 		SpaceObject object = game.objects.get(0);
 
 		assertEquals(new Point2D.Double(0, 10), roundPoint(object.direction));
 	}
 
 	@Test
 	public void test_that_launch_creates_a_new_shot_which_is_aimed_in_the_players_direction_with_direction_left() {
 		game.player.alignment = 270;
 
 		game.launchAShot();
 		SpaceObject object = game.objects.get(0);
 
 		assertEquals(new Point2D.Double(-10, 0), roundPoint(object.direction));
 	}
 
 	@Test
 	public void test_that_launch_creates_a_new_shot_which_is_aimed_in_the_players_direction_with_direction_top_right() {
 		game.player.alignment = 45;
 
 		game.launchAShot();
 		SpaceObject object = game.objects.get(0);
 
 		assertEquals(new Point2D.Double(7, -7), roundPoint(object.direction));
 	}
 
 	@Test
 	public void test_that_launch_creates_a_new_shot_which_has_a_direction_higher_zero() {
 		game.objects = new ArrayList<SpaceObject>();
 
 		game.launchAShot();
 		SpaceObject object = game.objects.get(0);
 
 		assertNotSame(new Point2D.Double(), object.direction);
 	}
 
 	@Test
 	public void test_that_launch_creates_a_new_shot_which_is_positioned_near_the_player_on_top() {
 		game.objects = new ArrayList<SpaceObject>();
 		game.player.setAlignment(0);
 		game.player.position = new Point2D.Double(100, 100);
 		Point2D expectedPosition = new Point2D.Double(100, 80);
 
 		game.launchAShot();
 		SpaceObject object = game.objects.get(0);
 
 		assertEquals(expectedPosition, object.position);
 	}
 
 	@Test
 	public void test_that_launch_creates_a_new_shot_which_is_positioned_near_the_player_on_right() {
 		game.objects = new ArrayList<SpaceObject>();
 		game.player.setAlignment(90);
 		game.player.position = new Point2D.Double(100, 100);
 		Point2D expectedPosition = new Point2D.Double(120, 100);
 
 		game.launchAShot();
 		SpaceObject object = game.objects.get(0);
 
 		assertEquals(expectedPosition, object.position);
 	}
 
 	@Test
 	public void test_that_launch_creates_a_new_shot_which_is_positioned_near_the_player_on_bottom() {
 		game.objects = new ArrayList<SpaceObject>();
 		game.player.setAlignment(180);
 		game.player.position = new Point2D.Double(100, 100);
 		Point2D expectedPosition = new Point2D.Double(100, 120);
 
 		game.launchAShot();
 		SpaceObject object = game.objects.get(0);
 
 		assertEquals(expectedPosition, object.position);
 	}
 
 	@Test
 	public void test_that_launch_creates_a_new_shot_which_is_positioned_near_the_player_on_left() {
 		game.objects = new ArrayList<SpaceObject>();
 		game.player.setAlignment(270);
 		game.player.position = new Point2D.Double(100, 100);
 		Point2D expectedPosition = new Point2D.Double(80, 100);
 
 		game.launchAShot();
 		SpaceObject object = game.objects.get(0);
 
 		assertEquals(expectedPosition, object.position);
 	}
 
 	@Test
 	public void test_that_run_with_a_positive_alignment_turns_the_player_right() {
 		game.player.alignment = 0;
 
 		game.run(10, false, false);
 
 		assertEquals(10, game.player.alignment);
 	}
 
 	@Test
 	public void test_that_run_with_a_negative_alignment_turns_the_player_right() {
 		game.player.alignment = 0;
 
 		game.run(-10, false, false);
 
 		assertEquals(350, game.player.alignment);
 	}
 
 	@Test
 	public void test_that_run_with_forward_moves_the_player_forward() {
 		game.player.position = new Point2D.Double(100, 100);
 
 		game.run(0, true, false);
 
 		assertEquals(new Point2D.Double(100, 100 - Game.PLAYER_SPEED), game.player.position);
 	}
 
 	@Test
 	public void test_that_run_with_shot_generates_a_shot() {
 		game.run(0, false, false);
 		int oldObjectsCount = game.objects.size();
 		game.objects.get(0).position = new Point2D.Double(100, 100);
 		game.objects.get(1).position = new Point2D.Double(500, 500);
 		game.player.position = new Point2D.Double(300,300);
 
 		game.run(0, false, true);
 		
 		assertEquals(oldObjectsCount + 1, game.objects.size());
 	}
 
 	@Test
 	public void test_that_a_shot_which_reaches_a_border_gets_destroyed(){
 		game.run(0, false, false);
 		Shot shot = new Shot(game.player.position, Game.SHOT_SPEED, game.player.alignment, game.resolution);
 		shot.position = new Point2D.Double(0, 100);
 		game.objects.add(shot);
 		int oldCount = game.objects.size();
 		
 		game.run(0, false, false);
 		
 		assertEquals(oldCount - 1, game.objects.size());
 		assertFalse(game.objects.contains(shot));
 	}
 
 	private Point2D roundPoint(Point2D direction) {
 		return new Point2D.Double((int) Math.round(direction.getX()), (int) Math.round(direction.getY()));
 	}
 
 }
