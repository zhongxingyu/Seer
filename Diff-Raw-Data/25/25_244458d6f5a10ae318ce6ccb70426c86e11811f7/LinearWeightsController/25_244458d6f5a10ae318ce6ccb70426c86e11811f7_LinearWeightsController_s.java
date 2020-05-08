 /**
  *
  */
 package cwcore;
 
 import cobweb.Agent;
 import cobweb.Controller;
 import cobweb.globals;
 import cobweb.params.CobwebParam;
 import cwcore.ComplexAgent.SeeInfo;
 
 public class LinearWeightsController implements cobweb.Controller {
 
 	/**
 	 *
 	 */
 	private static final long serialVersionUID = -4755777042224299222L;
 
 	public static final int ENERGY_THRESHOLD = 160;
 
 	public static final int INPUT_COUNT = 10;
 	public static final int OUTPUT_COUNT = 6;
 
 	private static double[] runningOutputMean = new double[OUTPUT_COUNT];
 
 	public static final String[] inputNames = { "Constant", "Energy", "Distance to agent", "Distance to food",
 		"Distance to obstacle", "Direction", "Memory", "Communication", "Age", "Random" };
 
 	public static final String[] outputNames = { "Memory", "Communication", "Left", "Right", "Forward", "Asexual Breed" };
 
 	private static float mutationCounter = 0;
 
 	private static final double UPDATE_RATE = 0.001;
 
 	public static double[] getRunningOutputMean() {
 		return runningOutputMean;
 	}
 
 	private int memSize;
 
 	private int commSize;
 
 	private LinearWeightsControllerParams params;
 
 	public LinearWeightsController() {
 		// nothing
 	}
 
 	public void addClientAgent(Agent theAgent) {
 		// Nothing
 	}
 
 	public void controlAgent(Agent theAgent) {
 		ComplexAgent agent;
 		if (theAgent instanceof ComplexAgent) {
 			agent = (ComplexAgent) theAgent;
 		} else {
 			return;
 		}
 		SeeInfo get = agent.distanceLook();
 		int type = get.getType();
 		int dist = get.getDist();
 		double variables[] = {
 				1.0,
 				((double) agent.getEnergy() / (ENERGY_THRESHOLD)),
 				type == ComplexEnvironment.FLAG_AGENT ? 
 						(ComplexAgent.LOOK_DISTANCE - dist) / (double) ComplexAgent.LOOK_DISTANCE : 0,
 						type == ComplexEnvironment.FLAG_FOOD ? 
 								(ComplexAgent.LOOK_DISTANCE - dist) / (double) ComplexAgent.LOOK_DISTANCE : 0,
 								type == ComplexEnvironment.FLAG_STONE || type == ComplexEnvironment.FLAG_WASTE ? 
 										(ComplexAgent.LOOK_DISTANCE - dist) / 4 : 0,
										agent.getIntFacing() / 2, (double) agent.getMemoryBuffer() / (1 << memSize - 1),
										(double) agent.getCommInbox() / (1 << commSize - 1), Math.max(agent.getAge() / 100.0, 2),
 										globals.random.nextGaussian() 
 		};
 
 		double memout = 0.0;
 		double commout = 0.0;
 		double left = 0.0;
 		double right = 0.0;
 		double step = 0.0;
 		double asexflag = 0.0;
 		for (int eq = 0; eq < OUTPUT_COUNT; eq++) {
 			double res = 0.0;
 			variables[9] = globals.random.nextGaussian();
 			for (int v = 0; v < INPUT_COUNT; v++) {
 				res += params.data[v][eq] * variables[v];
 			}
 
 			runningOutputMean[eq] *= (1 - UPDATE_RATE);
 			runningOutputMean[eq] += UPDATE_RATE * res;
 
 			if (eq == 0)
 				memout = res;
 			else if (eq == 1)
 				commout = res;
 			else if (eq == 2)
 				left = res;
 			else if (eq == 3)
 				right = res;
 			else if (eq == 4)
 				step = res;
 			else if (eq == 5)
 				asexflag = res;
 		}
 
 		agent.setMemoryBuffer((int) memout);
 		agent.setCommOutbox((int) commout);
 		agent.setAsexFlag(asexflag > 0.50);
 
 		if (right > left && right > step)
 			agent.turnRight();
 		else if (left > right && left > step)
 			agent.turnLeft();
 		else if (step > 0)
 			agent.step();
 	}
 
 	public CobwebParam getParams() {
 		return params;
 	}
 
 	private void mutate(float mutation) {
 		mutationCounter += INPUT_COUNT * OUTPUT_COUNT * mutation;
 		while (mutationCounter > 1) {
 			int i = globals.random.nextInt(params.data.length);
 			int j = globals.random.nextInt(params.data[i].length);
 			params.data[i][j] += globals.random.nextGaussian() * 0.5;
 			mutationCounter -= 1;
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see cobweb.Agent.Controller#removeClientAgent(cobweb.Agent)
 	 */
 	public void removeClientAgent(Agent theAgent) {
 		// Nothing
 	}
 
 	public void setParams(CobwebParam params) {
 		this.params = (LinearWeightsControllerParams) params;
 	}
 
 	public void setupFromEnvironment(int memSize, int commSize, CobwebParam params) {
 		this.params = (LinearWeightsControllerParams) params;
 		this.memSize = memSize;
 		this.commSize = commSize;
 	}
 
 	public void setupFromParent(Controller p, float mutation) {
 		if (!(p instanceof LinearWeightsController))
 			throw new RuntimeException("Parent's controller type must match the child's");
 
 		LinearWeightsController pa = (LinearWeightsController) p;
 		this.params = pa.params.copy();
 
 		mutate(mutation);
 	}
 
 	public void setupFromParents(Controller p1, Controller p2, float mutation) {
 		if (!(p1 instanceof LinearWeightsController) || !(p2 instanceof LinearWeightsController)) {
 			throw new RuntimeException("Parent's controller type must match the child's");
 		}
 		LinearWeightsController pa1 = (LinearWeightsController) p1;
 		LinearWeightsController pa2 = (LinearWeightsController) p2;
 
 		params = pa1.params.copy();
 
 		for (int i = 0; i < params.data.length; i++) {
 			for (int j = 0; j < params.data[i].length; j++) {
 				if (globals.random.nextBoolean()) {
 					params.data[i][j] = pa2.params.data[i][j];
 				}
 			}
 		}
 
 		mutate(mutation);
 	}
 
 	public double similarity(LinearWeightsController other) {
 		int diff = 0;
 		for (int i = 0; i < params.data.length; i++) {
 			for (int j = 0; j < params.data[i].length; j++) {
 				diff += Math.abs(this.params.data[i][j] * this.params.data[i][j] - other.params.data[i][j]
 				                                                                                        * other.params.data[i][j]);
 			}
 		}
 		return Math.max(0, (100.0 - diff) / 100.0);
 	}
 
 }
