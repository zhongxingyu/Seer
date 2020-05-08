 package biosim.app.fishknn;
 
 import biosim.core.agent.Agent;
 import biosim.core.body.AbstractFish;
 import biosim.core.body.NotemigonusCrysoleucas;
 import biosim.core.gui.GUISimulation;
 import biosim.core.sim.Environment;
 import biosim.core.sim.Simulation;
 import biosim.core.sim.RectObstacle;
 
 import sim.util.MutableDouble2D;
 import sim.util.Double3D;
 
 import java.io.File;
 import java.io.IOException;
 
 
 public class FishZones extends FishKNN{
 	double prevTime = 0.0;
 	public FishZones(AbstractFish b){
 		super(b,null);
 		//fishBody = b;
 	}
 	public void init(){
 		//System.gc();
 	}
 	public void finish(){
 	}
 	public void act(double time){
 		double[] rv = new double[3];
 		MutableDouble2D wall = new MutableDouble2D();
 		boolean sawWall = fishBody.getNearestObstacleVec(wall);
 		if(!sawWall) wall.x = wall.y = 0.0;
 		MutableDouble2D[] zones = new MutableDouble2D[fishBody.getNumZones()];
 		boolean zonesWorked = fishBody.getZoneCoMVecs(zones);
 		MutableDouble2D desiredDir = new MutableDouble2D(1.0,0.0);
 		if(zonesWorked){
 			boolean[] fishInZone = new boolean[zones.length];
 			for(int i=0;i<zones.length;i++) fishInZone[i] = (zones[i].x != 0.0 || zones[i].y != 0.0);
 			if(fishInZone[0]){
 				desiredDir = zones[0].dup().negate();
 			} else if(fishInZone[1] && fishInZone[2]){
 				desiredDir = zones[1].dup().normalize().addIn(zones[2].dup().normalize()).multiplyIn(0.5);
 			} else if(fishInZone[1]){
 				desiredDir = zones[1].dup();
			} else if(fishInZone[2]){
 				desiredDir = zones[2].dup();
 			}
 		}
 		if(sawWall && wall.length() < NotemigonusCrysoleucas.REPULSION_ZONE_RANGE){
 			desiredDir = wall.dup().negate().normalize();
 		}
 		rv[0] = NotemigonusCrysoleucas.SIZE/2.0;
 		rv[1] = 0.0;
 		rv[2] = (desiredDir.lengthSq()>0)?desiredDir.angle():0.0;
 		fishBody.setDesiredVelocity(rv[0],rv[1],rv[2]);
 	}
 	
 	
 	public static final double WIDTH=2.50;//2.5;
 	public static final double HEIGHT=1.5;//1.5;
 	
 	public static void main(String[] args){
 		int numFish = 30;
 		Environment env = new Environment(WIDTH,HEIGHT,1.0/30.0);
 		env.addObstacle(new RectObstacle(0.01,HEIGHT), WIDTH-0.01,  0.0);//east wall
 		env.addObstacle(new RectObstacle(0.01,HEIGHT),  0.0,  0.0);//west
 		env.addObstacle(new RectObstacle(WIDTH,0.01),  0.0,  0.0);//north
 		env.addObstacle(new RectObstacle(WIDTH,0.01),  0.0, HEIGHT-0.01);//south
 		env.setToroidal(true);
 		//add agents
 		NotemigonusCrysoleucas[] bodies = new NotemigonusCrysoleucas[numFish];
 		for(int i=0;i<bodies.length;i++){
 			bodies[i] = new NotemigonusCrysoleucas();
 			env.addBody(bodies[i]);
 		}
 		Agent[] agents = new Agent[numFish];
 		for(int i=0;i<agents.length;i++){
 			agents[i] = new FishZones(bodies[i]);
 			bodies[i].setAgent(agents[i]);
 		}
 		System.gc();
 		//env.runSimulation(args);
 		Simulation sim = env.newSimulation();
 		//sim.addLogger(new FishKNNLogger());
 		GUISimulation gui = new GUISimulation(sim);
 		gui.setPortrayalClass(NotemigonusCrysoleucas.class, FishPortrayal.class);
 		//if(numFish > 30){
 			FishPortrayal.bi = null;
 		//}
 		gui.setDisplaySize((int)(WIDTH*500),(int)(HEIGHT*500));
 		gui.createController();
 	}
 }
