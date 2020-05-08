 package br.ufrj.jfirn.simulator;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import br.ufrj.jfirn.common.Robot;
 import br.ufrj.jfirn.intelligent.sensors.Eye;
 import br.ufrj.jfirn.intelligent.sensors.PositioningSystem;
 import br.ufrj.jfirn.intelligent.sensors.SightData;
 import br.ufrj.jfirn.simulator.renderer.SimpleSwingRenderer;
 import br.ufrj.jfirn.simulator.renderer.SimulationRenderer;
 
 
 /**
  * Engine to run online simulations.
  * 
  */
 public class Engine {
 
 	private final Set<Robot> robots = new HashSet<>();
 	private final Set<Eye> eyes = new HashSet<>();
 	private final Set<PositioningSystem> gpss = new HashSet<>();
 	private final List<SimulationRenderer> renderers = new ArrayList<>();
 
 	public Engine() {
 		this.renderers.add( new SimpleSwingRenderer() );
 		//this.renderers.add( new WriterRenderer(new OutputStreamWriter(System.out)) );
 	}
 
 	public Engine(SimulationRenderer renderer) {
 		this.renderers.add( renderer );
 	}
 
 	
 	public void simulate(int iterations) {
 		init();
 
 		for (int i = 0; i < iterations; i++) {
 			sense();
 			move();
 			render();
 		}
 
 		for (SimulationRenderer renderer : renderers) {
 			renderer.done();
 		}
 	}
 
 	public void addRobot(Robot robot) {
 		this.robots.add(robot);
 	}
 
 	public void addIntelligentRobot(Robot robot, Eye eye, PositioningSystem gps) {
 		this.robots.add(robot);
 		this.eyes.add(eye);
 		this.gpss.add(gps);
 	}
 
 	private void init() {
 		//nothing to do yet...
 	}
 
 	private void sense() {
 		//this will send a GPS signal to all robots that have an embedded GPS
 		for (PositioningSystem gps : gpss) {
 			Robot r = (Robot)gps;
 			gps.onPositioningData(r.position(), r.direction(), r.speed());
 		}
 
 		//TODO sensing robots should both be solved efficiently. Maybe we should consider implementing a quadtree?
 		for(Eye eye : eyes) {
 			final Set<SightData> seenRobots = new HashSet<>();
 			for(Robot r : robots) {
 				if ( eye.sees(r) ) {
 					seenRobots.add(
 						new SightData(r.hashCode(), r.position(), r.speed(), r.direction())
 					);
 				}
 			}
			if (!seenRobots.isEmpty()) {
				eye.onSight(seenRobots);
			}
 		}
 	}
 
 	private void move() {
 		for (Robot robot : this.robots) {
 			robot.move();
 		}
 	}
 
 	/**
 	 * Render stuff (if a renderer exists).
 	 */
 	private void render() {
 		if (!renderers.isEmpty()) {
 			for (Robot robot : this.robots) {
 				for (SimulationRenderer renderer : renderers) {
 					renderer.draw(robot);
 				}
 			}
 			for (SimulationRenderer renderer : renderers) {
 				renderer.nextTick();
 			}
 		}
 	}
 
 }
