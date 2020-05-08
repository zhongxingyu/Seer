 package balle.strategy;
 
 import org.apache.log4j.Logger;
 
 import balle.controller.Controller;
 import balle.main.SimpleWorldGUI;
 import balle.world.AbstractWorld;
 import balle.world.Snapshot;
 import balle.world.processing.AbstractWorldProcessor;
 
 public class StrategyRunner extends AbstractWorldProcessor {
 
	private final Controller controller;
 	private final static Logger LOG = Logger.getLogger(StrategyRunner.class);
 
 	private Strategy currentStrategy;
 	private final SimpleWorldGUI gui;
 
 	/**
 	 * Initialises strategy runner
 	 * 
 	 * @param controller
 	 *            controller that will be used to move the robot
 	 * @param world
 	 *            world that will be used
 	 * @param gui
 	 *            GUI that Drawables will be drawn on.
 	 */
 	public StrategyRunner(Controller controller, AbstractWorld world,
 			SimpleWorldGUI gui) {
 		super(world);
 		this.controller = controller;
 		this.currentStrategy = null;
 		this.gui = gui;
 	}
 
 	@Override
 	protected void actionOnStep() {
 
 	}
 
 	@Override
 	protected void actionOnChange() {
 		if (currentStrategy != null) {
 
 			Snapshot snapshot = getSnapshot();
 			try {
 				currentStrategy.step(controller, snapshot);
 			} catch (Exception e) {
 				LOG.error("Strategy raised exception" + e.toString());
 
 				for (StackTraceElement se : e.getStackTrace())
 					LOG.debug(se.toString());
 
 				controller.stop();
 			}
 			gui.setDrawables(currentStrategy.getDrawables());
 		}
 
 	}
 
 	/**
 	 * Stops the current running strategy
 	 */
 	public void stopStrategy() {
 		if (currentStrategy != null) {
 			LOG.info("Stopping " + currentStrategy.getClass().getName());
 			currentStrategy.stop(controller);
 			currentStrategy = null;
 		}
 	}
 
 	/**
 	 * Starts the strategy
 	 * 
 	 * @param strategy
 	 *            the strategy
 	 */
 	public void startStrategy(Strategy strategy) {
 		// Stop the old strategy
 		if (currentStrategy != null)
 			stopStrategy();
 		// Start the new strategy
 		currentStrategy = strategy;
 		LOG.info("Started " + currentStrategy.getClass().getName());
 	}
 
 	@Override
 	public void cancel() {
 		super.cancel();
 		stopStrategy();
 		LOG.info("StrategyRunner canceled");
 	}
 
 	@Override
 	public String toString() {
 		return this.getName();
 	}
 
 	@Override
 	public void start() {
 		super.start();
 		LOG.info("StrategyRunner initialised");
 	}
 }
