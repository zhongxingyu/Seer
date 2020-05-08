 package biosim.app.twostateants;
 
 import biosim.core.agent.Agent;
 import biosim.core.body.Body;
 import biosim.core.body.AphaenogasterCockerelli;
 import biosim.core.body.AbstractAnt;
 import biosim.core.gui.GUISimulation;
 import biosim.core.sim.Simulation;
 import biosim.core.sim.Environment;
 //import biosim.core.sim.Obstacle;
 import biosim.core.sim.RectObstacle;
 
 import biosim.app.tutorial.AvoidAntLogger;
 
 import sim.util.MutableDouble2D;
 public class TwoStateAnt implements Agent {
 	AbstractAnt antBody;
 	public double timeNearAnt;
 	public double timeAvoiding;
 	double lastTime;
 	public int state;
 	public static final int AVOID=1;
 	public static final int APPROACH=2;
 	public static final double VISIT_TIME=5.0; //seconds
 	public static final double VISIT_RANGE=AphaenogasterCockerelli.SIZE*2; //meters
 	public static final double AVOID_TIME=10.0;
 	public TwoStateAnt(AbstractAnt b){
 		antBody = b;
 		timeAvoiding = timeNearAnt = 0.0;
 		state = APPROACH;
 		lastTime = 0.0;
 	}
 	public double[] act(double time){
 		double[] rv = new double[3];
 		//our default is to move forward in a straight line
 		rv[0] = 0.024; 	//24mm per second straight ahead
 		rv[1] = 0.0;	//Ants *can* move laterally, but ours won't for now
 		rv[2] = 0.0;	//no rotational velocity by default
 		//get a vector towards the nearest thing so we can avoid/approach it
 		MutableDouble2D ant = new MutableDouble2D();
 		boolean sawAnt = antBody.getNearestSameAgentVec(ant);
 		MutableDouble2D wall = new MutableDouble2D();
 		boolean sawWall = antBody.getNearestObstacleVec(wall);
 		if(state == AVOID){
 			timeAvoiding += time - lastTime;
 			MutableDouble2D avoidPoint = null;
 			if(sawWall){
 				avoidPoint = wall;
 			} else if(sawAnt){
 				avoidPoint = ant;
 			} 
 			if(avoidPoint != null){
 				if(avoidPoint.y > 0) rv[2] = Math.toRadians(-40.0);
 				else rv[2] = Math.toRadians(40.0);
 			}
 			if(timeAvoiding > AVOID_TIME){
				timeAvoiding = 0.0;
 				state = APPROACH;
 			}
 		} else if(state == APPROACH){
 			if(sawAnt){
 				if(ant.length() <= VISIT_RANGE){
 					timeNearAnt += time-lastTime;
 				} else {
 					timeNearAnt = 0.0;
 				}
 				//head to the ant
 				rv[0] = 0.024*(ant.length()-antBody.getSize())/antBody.getNearestSameAgentVecSensorRange();
 				rv[2] = Math.toRadians(40.0)*Math.signum(ant.angle())*((Math.PI/2)-Math.abs(ant.angle()))/(Math.PI/2);
 				//switch to avoid if we've visited long enough
 				if(timeNearAnt > VISIT_TIME){
					timeNearAnt = 0.0;
 					state = AVOID;
 				}
 			} else if(sawWall){
				timeNearAnt = 0.0;
 				rv[2] = Math.toRadians(-40.0)*Math.signum(wall.angle());
 			}
 		}
 		lastTime = time;
 		return rv;
 	}
 	
 	public static final double WIDTH=0.2;
 	public static final double HEIGHT=0.2;
 	public static void main(String[] args){
 		//set up the environment
 		int numAnts = 10;
 		Environment env = new Environment(WIDTH,HEIGHT,1.0/30.0);
 		env.addStaticPOI("nest",WIDTH/2,0.02);
 		env.addObstacle(new RectObstacle(0.01,0.2), 0.19,  0.0);//east wall
 		env.addObstacle(new RectObstacle(0.01,0.2),  0.0,  0.0);//west
 		env.addObstacle(new RectObstacle(0.2,0.01),  0.0,  0.0);//north
 		env.addObstacle(new RectObstacle(0.2,0.01),  0.0, 0.19);//south
 		//add agents
 		AphaenogasterCockerelli[] bodies = new AphaenogasterCockerelli[numAnts];
 		for(int i=0;i<bodies.length;i++){
 			bodies[i] = new AphaenogasterCockerelli();
 			env.addBody(bodies[i]);
 		}
 		Agent[] agents = new Agent[numAnts];
 		for(int i=0;i<agents.length;i++){
 			agents[i] = new TwoStateAnt(bodies[i]);
 			bodies[i].setAgent(agents[i]);
 		}
 		//env.runSimulation(args);
 		Simulation sim = env.newSimulation();
 		sim.addLogger(new TwoStateLogger());
 		GUISimulation gui = new GUISimulation(sim);
 		gui.createController();
 	}
 
 }
