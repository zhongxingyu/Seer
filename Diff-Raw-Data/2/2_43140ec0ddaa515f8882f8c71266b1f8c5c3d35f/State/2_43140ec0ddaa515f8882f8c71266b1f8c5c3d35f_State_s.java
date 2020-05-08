 package de.kit.irobot.util;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import de.kit.irobot.labyrinth.LabyrinthLeaving;
 import de.kit.irobot.labyrinth.LabyrinthRoaming;
 import de.kit.irobot.labyrinth.SwampCrossing;
 import de.kit.irobot.lineFollow.AvoidObstacle;
 import de.kit.irobot.lineFollow.FindLine;
 import de.kit.irobot.lineFollow.StayOnLine;
 import de.kit.irobot.subsumption.Arbitrator;
 import de.kit.irobot.weihnachtsbaum.FindBaum;
 
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
 			return toList(labyrinthRoaming, swampsCrossing, labyrinthLeaving);
 		}
 	},
 	// RAMPUP,
 //	BRIDGE {
 //		@Override
 //		protected List<BaseBehaviour> getBehaviourList(BehaviourController controller) {
 //			// TODO Auto-generated method stub
 //			return null;
 //		}
 //	},
 	LINEFOLLOW {
 		@Override
 		protected List<BaseBehaviour> getBehaviourList(BehaviourController controller) {
 			FindLine findLine = new FindLine(getConfig(), controller);
 			StayOnLine stayOnLine = new StayOnLine(getConfig(), controller);
 			AvoidObstacle avoidObstacle = new AvoidObstacle(getConfig(), controller);
 			return toList(findLine, stayOnLine, avoidObstacle);
 		}
 	},
 	/*
 	 * RAMPDOWN, TURNTABLE, CHRISTMASTREE, BOSS,
 	 */
 	CHRISTMASTREE {
 		@Override
 		protected List<BaseBehaviour> getBehaviourList(BehaviourController controller) {
 			return toList(new FindBaum(getConfig(), controller));
 		}
 //	},
 //	FINISH {
 //		@Override
 //		protected List<BaseBehaviour> getBehaviourList(BehaviourController controller) {
 //			// TODO Auto-generated method stub
 //			return null;
 //		}
 	};
 	
 	
 	private final BehaviourController controller;
 	private final Arbitrator arbitrator;
 	
 	
 	
 	private State() {
 		controller = new BehaviourController();
 		List<BaseBehaviour> behaviourList = getBehaviourList(controller);
 		BaseBehaviour[] behaviours = behaviourList.toArray(new BaseBehaviour[behaviourList.size() + 1]);
 		behaviours[behaviourList.size()] = new ShowMenu(getConfig(), controller);
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
 		startNextState();
 	}
 	
 	public void startOnly() {
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
 	
 	
 	public static String[] getMenuItems() {
 		State[] states = values();
 		String[] menuItems = new String[states.length + 1];
 		for (int i = 0; i < states.length; i++) {
			menuItems[i] = states.toString();
 		}
 		menuItems[states.length] = "EXIT";
 		return menuItems;
 	}
 	
 }
