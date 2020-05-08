 package com.optimalbyte.snake;
 
 import java.awt.Color;
 import java.awt.Graphics2D;
 import java.awt.Rectangle;
 import java.awt.geom.Ellipse2D;
 import java.util.Random;
 
 /**
  * Represents the fruit that the player will (attempt to) make the snake eat.
  *
  * @author samuraiblood2
  */
 public class FruitEntity extends Entity {
 
 	/** The random number generator. */
 	private Random rand = new Random();
 	
 	/**
 	 * We use the constructor to pass an instance of the Game to this class.
 	 *
 	 * @param game An instance of Game.
 	 */
 	public FruitEntity(Game game) {
 		super(game);
 
 		spawnFruit();
 	}
 
 	/**
 	 * @see Entity#onDraw(Graphics2D)
 	 */
 	@Override
 	public void onDraw(Graphics2D graphics) {
 		graphics.setColor(Color.RED);
 		graphics.fill(new Ellipse2D.Double(location.getX(), location.getY(), Game.SIZE, Game.SIZE));
 	}
 
 	/**
 	 * @see Entity#onCollision(Entity)
 	 */
 	@Override
 	public void onCollision(Entity other) {
 		if (other instanceof SnakeEntity.TailEntity) {
 
 			// XXX: On the off chance that the fruit were to spawn 
 			// underneath the snakes tail, we respawn the fruit.
 			if (atPosition(((SnakeEntity.TailEntity) other).getLocation())) {
 				spawnFruit();
 			}
 		}
 	}
 	
 	/**
 	 * Spawns the fruit at a randomly generated spot in the game.
 	 */
 	public void spawnFruit() {
 		Rectangle tile = game.getTiles().get(rand.nextInt(game.getTiles().size()));
 		setLocation(tile.getLocation());
 	}
 }
