 package balle.world;
 
 import java.util.ArrayList;
 
 import balle.controller.ControllerListener;
 import balle.simulator.SimulatorWorld;
 import balle.simulator.SoftBot;
 import balle.strategy.bezierNav.ControllerHistoryElement;
 import balle.world.objects.Pitch;
 
 /**
  * Uses a simulator to account for latency in system.
  * 
  */
 public class SimulatedWorld extends BasicWorld implements ControllerListener {
 
 	/**
 	 * The simulator for this simulated world.
 	 */
 	protected SimulatorWorld worldModel;
 
 	/**
 	 * Our robots history
 	 */
 	protected ArrayList<ControllerHistoryElement> controllerHistory;
 
 	/**
 	 * Our robots virtual duplicate
 	 */
 	protected SoftBot virtual;
 
 	/**
 	 * Time that the update method was called
 	 */
 	protected long updateTimestamp = -1;
 
 	/**
 	 * Current time in the worldModel
 	 */
 	protected long simulatorTimestamp = -1;
 
 	/**
 	 * 
 	 * @param worldModel
 	 * @param balleIsBlue
 	 * @param goalIsLeft
 	 * @param pitch
 	 */
 	public SimulatedWorld(SimulatorWorld worldModel, boolean balleIsBlue,
 			boolean goalIsLeft, Pitch pitch) {
 
 		super(balleIsBlue, goalIsLeft, pitch);
 		this.worldModel = worldModel;
 
 		if (balleIsBlue)
 			virtual = worldModel.getBlueSoft();
 		else
 			virtual = worldModel.getYellowSoft();
 
 		controllerHistory = new ArrayList<ControllerHistoryElement>();
 	}
 
 	/**
 	 * Make sure world model is updated too.
 	 */
 	@Override
 	public void update(double yPosX, double yPosY, double yDeg, double bPosX,
 			double bPosY, double bDeg, double ballPosX, double ballPosY,
 			long timestamp) {
 
 		// Update this.prev with update information.
 		super.update(yPosX, yPosY, yDeg, bPosX, bPosY, bDeg, ballPosX,
 				ballPosY, timestamp);
 
 		// Record time.
 		updateTimestamp = System.currentTimeMillis();
 		simulatorTimestamp = prev.getTimestamp();
 
 		// Update world model with snapshot information.
 		worldModel.setWithSnapshot(prev, isBlue());
 
 		// Roll worldModel forward until current time.
 		simulate(updateTimestamp);
 	}
 
 	/**
 	 * Roll the world simulation forward from the startTime, to the end time.
 	 * 
 	 * @param endTime
 	 *            Time to pause the simulation.
 	 * @param startTime
 	 *            Time to start the simulation.
 	 */
 	protected void simulate(long endTime) {
 		long startTime = simulatorTimestamp;
 
 		// clean up the history (ensure there is at least one element left in
 		// history)
 		while (controllerHistory.size() > 1
 				&& controllerHistory.get(0).getTimestamp() < startTime) {
 			controllerHistory.remove(0);
 		}
 
 		// setup a simulator using the current snapshot (assume we are blue)
 		float lastLPower = 0, lastRPower = 0;
 		if (controllerHistory.size() > 0) {
 			ControllerHistoryElement lastState = controllerHistory.get(0);
 			lastLPower = lastState.getPowerLeft();
 			lastRPower = lastState.getPowerRight();
 		}
 
 		virtual.setWheelSpeeds((int) lastLPower, (int) lastRPower);
 
 		// worldModel.setStartTime(startTime);
 
 		// use the controllerHistory to simulate the wheelspeeds
 		// // run a simulation while adjusting wheel speeds
 		long maxTD = 50; // low values may make this less accurate
 		for (int i = 0; i < controllerHistory.size(); i++) {
 			ControllerHistoryElement curr = controllerHistory.get(i);
 			long nextTime = endTime;
 
 			if (i < controllerHistory.size() - 1)
 				nextTime = controllerHistory.get(i + 1).getTimestamp();
 			for (long tD = maxTD; startTime < nextTime; tD = Math.min(startTime
 					+ tD, nextTime)
 					- startTime) {
 				// simulator.getBlueSoft().setWheelSpeeds(900, 900);
 				virtual.setWheelSpeeds(curr.getPowerLeft(),
 						curr.getPowerRight());
 				worldModel.update(tD);
 				worldModel.getWorld().step(tD / 1000f, 8, 3);
 				// System.out.println(tD);
 				// System.out
 				// .println(world.getSnapshot().getBalle().getPosition());
 				startTime += tD;
 			}
 		}
 	}
 
 	protected Snapshot getSnapshotFromWorldModel(long timestamp) {
 		return worldModel.getSnapshot(timestamp, prev.getPitch(),
 				prev.getOpponentsGoal(), prev.getOwnGoal(), isBlue());
 	}
 
 	/**
 	 * Gets a snapshot of the predicted world state last time the world
 	 * information was propagated.
 	 */
 	@Override
 	public Snapshot getSnapshot() {
 		Snapshot pred = worldModel.getSnapshot(updateTimestamp, prev.getPitch(),
 				prev.getOpponentsGoal(), prev.getOwnGoal(), isBlue());
 
 		// !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
 		// ONLY USE BALL POSITION FROM SIMULATOR, TILL ROBOT PREDICITION IS
 		// FIXED
 		// !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
 
 //		return worldModel.getSnapshot(updateTimestamp, prev.getPitch(),
 //				prev.getOpponentsGoal(), prev.getOwnGoal(), isBlue());
 
		return new Snapshot(this, prev.getOpponent(), prev.getBalle(),
		 pred.getBall(), prev.getOpponentsGoal(), prev.getOwnGoal(),
		 prev.getPitch(), pred.getTimestamp());
 	}
 
 	public Snapshot estimateAt(long time) {
 		if (time > simulatorTimestamp) {
 			simulate(time);
 			return getSnapshotFromWorldModel(time);
 		} else {
 
 			// TODO caching or similar?
 
 			return null;
 		}
 	}
 
 	/**
 	 * 
 	 * @param che
 	 */
 	@Override
 	public void commandSent(ControllerHistoryElement che) {
 		controllerHistory.add(che);
 	}
 }
