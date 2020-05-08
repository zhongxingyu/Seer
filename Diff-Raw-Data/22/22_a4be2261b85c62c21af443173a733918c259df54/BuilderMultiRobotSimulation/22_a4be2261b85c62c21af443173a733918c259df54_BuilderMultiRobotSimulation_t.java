 package ussr.builder;
 
 //import ckbot.CKBotStandard;
 import ussr.description.Robot;
 import ussr.description.setup.WorldDescription;
 import ussr.model.Controller;
 import ussr.physics.PhysicsFactory;
 import ussr.physics.PhysicsLogger;
 import ussr.physics.PhysicsParameters;
 import ussr.physics.PhysicsSimulation;
 import ussr.samples.GenericSimulation;
 import ussr.samples.ObstacleGenerator;
 import ussr.samples.atron.ATRON;
 import ussr.samples.ckbot.CKBotStandard;
 import ussr.samples.mtran.MTRAN;
 import ussr.samples.odin.modules.OdinBall;
 import ussr.samples.odin.modules.OdinBattery;
 import ussr.samples.odin.modules.OdinHinge;
 import ussr.samples.odin.modules.OdinMuscle;
 import ussr.samples.odin.modules.OdinSpring;
 import ussr.samples.odin.modules.OdinTube;
 import ussr.samples.odin.modules.OdinWheel;
 
 /**
  * The main responsibility of this class is to start default simulation
  * for ATRON,MTRAN and Odin modular robots.
  * @author Konstantinas
  */
 public class BuilderMultiRobotSimulation extends GenericSimulation {
 
 	
 	/**
 	 * The obstacle generator for generating line of box obstacles
 	 */
 	protected static ObstacleGenerator.ObstacleType obstacleType = ObstacleGenerator.ObstacleType.LINE;
 	
 	
 	/**
 	 * Returns the robot in the simulation.
 	 */
 	@Override
 	protected Robot getRobot() {		
 		return null;
 	}
 
 	/**
 	 * Starts multi-robot simulation for ATRON,MTRAN and Odin.
 	 * @param args, passed arguments.
 	 */
 	public static void main( String[] args ) {
 		/*Activate connectors*/
 		GenericSimulation.setConnectorsAreActive(true);
 		BuilderMultiRobotSimulation simulation = new BuilderMultiRobotSimulation();
 		/* Specify realistic collision*/
 		PhysicsParameters.get().setRealisticCollision(true);
 		/* Specify simulation step*/
 		PhysicsParameters.get().setPhysicsSimulationStepSize(0.0035f);
 		/* Specify smallest resolution factor so that simulation can handle more modules.
 		 * This is done in order to "in a way" avoid stack overflow */
 		PhysicsParameters.get().setResolutionFactor(1);
 		simulation.runSimulation(null,true);
 	}
 	/**
 	 * Define the simulation to run.
 	 * @param world, the world description.
 	 * @param startPaused, the state of simulation. True if paused and false if running.
 	 */
 	public void runSimulation(WorldDescription world, boolean startPaused) {
 		PhysicsLogger.setDefaultLoggingLevel();
 		/* Create the simulation*/
 		final PhysicsSimulation simulation = PhysicsFactory.createSimulator();
 		
 		/*Set ATRON robot to simulation and assign default controller to it*/
 		ATRON atron = new ATRON(){
 			public Controller createController() {
 				return new ATRONControllerDefault() {
 					public void activate() {
 						//delay(10000);
 						super.activate();
 					}
 				};
 			}
 		};
 		atron.setGentle();// Currently builder supports only this type of ATRON
 		//atron.setRubberRing();
 		simulation.setRobot(atron,"ATRON");
 		simulation.setRobot(atron,"ATRON rubberRing gentle");
 		simulation.setRobot(atron,"default");
 		
 		/*Set MTRAN robot to simulation and assign default controller to it*/
 		simulation.setRobot(new MTRAN(){
 			public Controller createController() {
 				return new MTRANControllerDefault() {
 					public void activate() {
 						//delay(10000);
 						super.activate();
 					}
 				};
 			}},"MTRAN");
 		
 		/*Set different Odin modules  to simulation and assign default controllers to them */
 		simulation.setRobot(new OdinMuscle(){
 			public Controller createController() {
 				return new OdinControllerDefault() {
 					public void activate() {
 						//delay(10000);
 						super.activate();
 					}
 				};
 			}},"OdinMuscle");
 		simulation.setRobot(new OdinWheel(){
 			public Controller createController() {
 				return new OdinControllerDefault();
 			}},"OdinWheel");
 		simulation.setRobot(new OdinHinge(){
 			public Controller createController() {
 				return new OdinControllerDefault();
 			}},"OdinHinge");
 
 		simulation.setRobot(new OdinBattery(){
 			public Controller createController() {
 				return new OdinControllerDefault();
 			}},"OdinBattery");
 
 		simulation.setRobot(new OdinBall(){
 			public Controller createController() {
 				return new OdinControllerDefault();
 			}},"OdinBall");
 
 		simulation.setRobot(new OdinSpring(){
 			public Controller createController() {
 				return new OdinControllerDefault();
 			}},"OdinSpring");
 
 		simulation.setRobot(new OdinTube(){
 			public Controller createController() {
 				return new OdinControllerDefault();
 			}},"OdinTube");	
 		 simulation.setRobot(new CKBotStandard(){
 	        	public Controller createController() {
 	        		return new CKBotControllerDefault();
 	        	}},"CKBotStandard");
 		
         /*Create the world description of simulation and set it to simulation*/
 		if(world==null) world = createWorld()
 		;
 		simulation.setWorld(world);
 		/*Simulation should be in paused state (static)in the beginning*/
 		simulation.setPause(startPaused);
 		/* Start the simulation*/
 		simulation.start();
 	}
 
 	/**
 	 * Create a world description for simulation.
 	 * @return world, the world description.
 	 */
 	private static WorldDescription createWorld() {
 		WorldDescription world = new WorldDescription();	        
 		world.setPlaneSize(100);		
	  	//ObstacleGenerator generator = new ObstacleGenerator();
	    //generator.obstacalize(obstacleType, world); // activate to add obstacles
 	    // world.setPlaneTexture(WorldDescription.WHITE_GRID_TEXTURE);
 		return world;
 	}
 }
