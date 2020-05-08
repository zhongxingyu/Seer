 package org.scripts.combat.util;
 
 import java.util.LinkedList;
 import java.util.List;
 
 import org.powerbot.core.script.job.Container;
 import org.powerbot.core.script.job.LoopTask;
 import org.powerbot.game.api.methods.Calculations;
 import org.powerbot.game.api.methods.interactive.Players;
 import org.powerbot.game.api.wrappers.Tile;
 
 public class AutomaticPathMaker {
 	
 	private static final int DISTANCE_INTERVAL = 5;
 	
 	private List<Tile> tilePath = new LinkedList<Tile>();
 	
 	private static abstract class PathMakerLoopTask extends LoopTask {
 		
 		protected boolean stop = false;
 		
 	}
 	
 	private PathMakerLoopTask loopTask;
 	
 	/**
 	 * Starts creating a path and adding a loop task for it using the specified container.
 	 */
 	public void startMakingPath(Container container) {
 		tilePath.add(Players.getLocal().getLocation());
 		loopTask = new PathMakerLoopTask() {
 			
 			@Override
 			public int loop() {
 				if (!stop) {
 					if (Calculations.distance(tilePath.get(tilePath.size() - 1), Players.getLocal().getLocation()) >= DISTANCE_INTERVAL) {
 						tilePath.add(Players.getLocal().getLocation());
 					}
 				} else {
 					//stop or discard this task.
 				}
 				return 50;
 			}
 		
 		};
 		container.submit(loopTask);
 	}
 	
 	/**
 	 * Gets the path generated.
 	 * @return The path.
 	 */
 	public Tile[] getPath() {
		return (Tile[]) tilePath.toArray();
 	}
 	
 	/**
 	 * Sets whether or not for the path maker to stop.
 	 * @param stop Whether or not the path maker stops.
 	 */
 	public void setStop(boolean stop) {
 		loopTask.stop = stop;
 	}
 	
 	/**
 	 * Checks whether or not the path maker is running.
 	 * @return <code>true</code> if running, otherwise <code>false</code>.
 	 */
 	public boolean isRunning() {
 		return !loopTask.stop;
 	}
 	
 	/**
 	 * Resets the tile path.
 	 */
 	public void reset() {
 		tilePath.clear();
 	}
 
 }
