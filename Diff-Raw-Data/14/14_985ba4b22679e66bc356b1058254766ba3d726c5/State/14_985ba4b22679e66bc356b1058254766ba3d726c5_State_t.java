 package de.kit.irobot.util;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import de.kit.irobot.bridge.BridgeCrossing;
 import de.kit.irobot.christmastree.ChristmasTreeLeaving;
 import de.kit.irobot.christmastree.ChristmasTreeTracking;
 import de.kit.irobot.labyrinth.LabyrinthEntering;
 import de.kit.irobot.labyrinth.LabyrinthLeaving;
 import de.kit.irobot.labyrinth.LabyrinthRoaming;
 import de.kit.irobot.labyrinth.SwampCrossing;
 import de.kit.irobot.linefollow.AvoidObstacle;
 import de.kit.irobot.linefollow.EndOfLine;
 import de.kit.irobot.linefollow.FindLine;
 import de.kit.irobot.linefollow.StayOnLine;
 import de.kit.irobot.subsumption.Arbitrator;
 import de.kit.irobot.turntable.TurnTableCrossing;
 
 public enum State {
 
 	//	START {
 	//		protected List<BaseBehaviour> getBehaviourList(BehaviourController controller) {
 	//			// TODO Auto-generated method stub
 	//			return null;
 	//		}
 	//	},
 	// BLUETOOTHSTART,
 	LABYRINTH {
 		@Override
 		protected List<BaseBehaviour> getBehaviourList(BehaviourController controller) {
 			LabyrinthRoaming labyrinthRoaming = new LabyrinthRoaming(getConfig(), controller);
 			SwampCrossing swampsCrossing = new SwampCrossing(getConfig(), controller);
 			LabyrinthLeaving labyrinthLeaving = new LabyrinthLeaving(getConfig(), swampsCrossing, controller);
 			LabyrinthEntering labyrinthEntering = new LabyrinthEntering(getConfig(), controller);
 			return toList(labyrinthRoaming, swampsCrossing, labyrinthLeaving, labyrinthEntering);	// Behaviours mit aufsteigenden Prioritäten
 		}
 	},
 	BRIDGE {
 		@Override
 		protected List<BaseBehaviour> getBehaviourList(BehaviourController controller) {
 			return toList(new BridgeCrossing(getConfig(), controller));
 		}
 	},
 	LINEFOLLOW {
 		@Override
 		protected List<BaseBehaviour> getBehaviourList(BehaviourController controller) {
 			FindLine findLine = new FindLine(getConfig(), controller);
 			StayOnLine stayOnLine = new StayOnLine(getConfig(), controller);
 			AvoidObstacle avoidObstacle = new AvoidObstacle(getConfig(), controller);
 			EndOfLine endOfLine = new EndOfLine(getConfig(), controller, findLine, stayOnLine);
 			return toList(findLine, stayOnLine, avoidObstacle, endOfLine);	// Behaviours mit aufsteigenden Prioritäten
 		}
 	},
 	/*
 	 * BOSS,
 	 */
 	CHRISTMASTREE {
 		@Override
 		protected List<BaseBehaviour> getBehaviourList(BehaviourController controller) {
 			ChristmasTreeTracking christmasTreeTracking = new ChristmasTreeTracking(getConfig(), controller);
 			ChristmasTreeLeaving christmasTreeLeaving = new ChristmasTreeLeaving(getConfig(), controller);
 			return toList(christmasTreeTracking, christmasTreeLeaving);	// Behaviours mit aufsteigenden Prioritäten
 		}
 	},
 	//	FINISH {
 	//		@Override
 	//		protected List<BaseBehaviour> getBehaviourList(BehaviourController controller) {
 	//			// TODO Auto-generated method stub
 	//			return null;
 	//		}
 	//	},
 	TURNTABLE {
 		@Override
 		protected List<BaseBehaviour> getBehaviourList(BehaviourController controller) {
 			return toList(new TurnTableCrossing(getConfig(), controller));	// Behaviours mit aufsteigenden Prioritäten
 		}
 	};
 
 
 	private final BehaviourController controller;
 	private final Arbitrator arbitrator;
 
 	private final List<BaseBehaviour> behaviourList;
 
 
 
 	private State() {
 		controller = new BehaviourController(getConfig());
 		behaviourList = getBehaviourList(controller);
		behaviourList.add(new ShowMenu(getConfig(), controller));
		BaseBehaviour[] behaviours = behaviourList.toArray(new BaseBehaviour[behaviourList.size()]);
 		arbitrator = new Arbitrator(behaviours, true);
 	}
 
 
 
 	protected Config getConfig() {
 		return Config.CONFIG;
 	}
 
 	protected List<BaseBehaviour> toList(BaseBehaviour... behaviours) {
 		List<BaseBehaviour> behaviourList = new ArrayList<BaseBehaviour>();
 		for (BaseBehaviour behaviour : behaviours) {
 			behaviourList.add(behaviour);
 		}
 		return behaviourList;
 	}
 
 	protected abstract List<BaseBehaviour> getBehaviourList(BehaviourController controller);
 
 	public void start() {
 		startOnly();
 		if (!controller.isKilled()) {
 			startNextState();
 		}
 	}
 
 	public void startOnly() {
 		reset();
 		controller.start();
 		arbitrator.start();
 	}
 
 	private void startNextState() {
 		State next = nextState();
 		if (next != null) {
 			next.start();
 		}
 	}
 
 	private State nextState() {
 		State[] states = values();
 		for (int i = 0; i < states.length; i++) {
 			if ((this == states[i]) && i < (states.length - 1)) {
 				return states[i + 1];
 			}
 		}
 		return null;
 	}
 
 	private void reset() {
 		for (BaseBehaviour behaviour : behaviourList) {
 			behaviour.reset();
 		}
 	}
 
 }
