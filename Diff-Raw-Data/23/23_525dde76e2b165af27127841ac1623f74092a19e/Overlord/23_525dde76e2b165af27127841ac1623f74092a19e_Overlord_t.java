 package core;
 
 import java.io.IOException;
 import java.text.ParseException;
 
 import logging.Log;
 import logging.RobotGraph;
 import map.Map;
 import map.ParseMap;
 import control.Control;
 import data_collection.DataCollection;
 import orc.Orc;
 import rrt.PathPlanning;
 import state_machine.StateMachine;
 import uORCInterface.OrcController;
 
 public class Overlord extends Thread {
 
 	OrcController orcControl;
 	Orc orc;
 
 	DataCollection dc;
 	StateEstimator se;
 	StateMachine sm;
 	PathPlanning pp;
 	Control c;
 
 	Log l;
 	private long startTime;
 	private Delta delta;
 	private Map map;
 
 	public Overlord() {
 		try {
 			map = Map.getInstance();
 
 			map.setMap(ParseMap.parseFile("cheat_map.txt"));
 			//m.setMap(ParseMap.parseFile("construction_map_2013.txt"));
             //m.setMap(ParseMap.parseFile("challenge_2013.txt"));
 
 			try {Thread.sleep(5000);} catch (InterruptedException e) {}
 			
 			orcControl = new OrcController(new int[] { 0, 1 });
 			orc = Orc.makeOrc();
			
			
 			dc = DataCollection.getInstance();
 			se = StateEstimator.getInstance();
 			se.numBlocksLeft = map.getBlocks().size();
 			sm = StateMachine.getInstance();
 			pp = PathPlanning.getInstance();
 			c = Control.getInstance();
 			//delta = Delta.getInstance();
 
 
 			l = Log.getInstance();
 
 		} catch (IOException e) {
 			e.printStackTrace();
 		} catch (ParseException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public void start() {
 		while (true) {
 			System.out.println("justed updated");
 			startTime = System.currentTimeMillis();
 			
 			dc.step();
 			se.step();
 			sm.step();
 
 			map.update();
 			
 			pp.step();
 			c.step();
 			
 			//delta.step();
 			
 			l.updatePose();
 			try {Thread.sleep(50-(System.currentTimeMillis()-startTime));} catch (Exception e){};
 		}
 	}
 
 	public static void main(String[] args) {
 		System.out.println("Main");
 
 		Overlord me = new Overlord();
 		System.out.println("About to start");
 		me.start();
 	}
 }
