 package nl.tudelft.jpacman.game;
 
 import java.util.Random;
 import java.util.concurrent.Executors;
 import java.util.concurrent.ScheduledExecutorService;
 import java.util.concurrent.ScheduledFuture;
 import java.util.concurrent.TimeUnit;
 
 import nl.tudelft.jpacman.level.Level;
 import nl.tudelft.jpacman.model.Direction;
 import nl.tudelft.jpacman.model.Ghost;
 
 /**
  * Moves the ghosts around on the board in a random manner. Every 100 ms a
  * random ghost will be selected and moved one square in a random available
  * (i.e. passable area) direction.
  * 
  * @author Jeroen Roosen
  */
 public class RandomGhostController implements GhostController {
 
 	/**
 	 * Delay between two subsequent ghost moves in milliseconds.
 	 */
 	private static final int MOVE_DELAY = 100;
 
 	/**
 	 * The level to manipulate.
 	 */
 	private final Level currentLevel;
 
 	/**
 	 * The service that will schedule and execute the moves.
 	 */
 	private final ScheduledExecutorService scheduler;
 
 	/**
 	 * The task executed by the service that can be cancelled when no longer
 	 * needed.
 	 */
 	private ScheduledFuture<?> task;
 
 	/**
 	 * Creates a new ghost controller for the specified level.
 	 * 
 	 * @param level
 	 *            The level containing a board and the ghosts to move around.
 	 */
 	public RandomGhostController(Level level) {
 		this.currentLevel = level;
 		this.scheduler = Executors.newSingleThreadScheduledExecutor();
 	}
 
 	@Override
 	public void start() {
 		task = scheduler.scheduleAtFixedRate(new Runnable() {
 
 			@Override
 			public void run() {
 				Random randomizer = new Random();
 				Ghost[] ghosts = currentLevel.getGhosts().toArray(new Ghost[] {});
 				Ghost randomGhost = ghosts[randomizer.nextInt(ghosts.length)];
 
 				Direction[] directions = Direction.values();
 				int randomDirIndex = randomizer.nextInt(directions.length);
 				Direction randomDirection = directions[randomDirIndex];
 
 				while (!randomGhost.getSquare().squareAt(randomDirection)
 						.isAccessibleTo(randomGhost)) {
 					randomDirection = directions[++randomDirIndex
 							% directions.length];
 				}
 
 				currentLevel.move(randomGhost, randomDirection);
 			}
 		}, 0, MOVE_DELAY, TimeUnit.MILLISECONDS);
 	}
 
 	@Override
 	public void stop() {
		task.cancel(false);
 	}
 }
