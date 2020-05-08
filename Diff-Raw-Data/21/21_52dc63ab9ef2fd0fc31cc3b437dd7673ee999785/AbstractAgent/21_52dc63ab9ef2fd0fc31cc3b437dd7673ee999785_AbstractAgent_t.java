 package chalmers.dax021308.ecosystem.model.agent;
 
 import java.awt.Color;
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Random;
 
 import chalmers.dax021308.ecosystem.model.population.IPopulation;
 import chalmers.dax021308.ecosystem.model.util.AgentPath;
 import chalmers.dax021308.ecosystem.model.util.Container;
 import chalmers.dax021308.ecosystem.model.util.FixedSizeAgentQueueObjectPriorityQueue;
 import chalmers.dax021308.ecosystem.model.util.Gender;
 import chalmers.dax021308.ecosystem.model.util.Position;
 import chalmers.dax021308.ecosystem.model.util.Vector;
 import chalmers.dax021308.ecosystem.model.environment.SurroundingsSettings;
 
 /**
  * AbstractAgent with neighbourlist.
  * 
  * @author Henrik Abstract class for handling the dummy methods
  */
 public abstract class AbstractAgent implements IAgent {
 	protected Position position;
 	protected Position nextPosition;
 	protected Position reUsedPosition;
 	protected String name;
 	protected Color color;
 	protected boolean groupBehaviour;
 	protected int width;
 	protected int height;
 	protected int capacity = Integer.MAX_VALUE;
 	protected int lifeLength = 0;
 	protected double energy = 1000; // set specific energy level in subclasses
 	protected int trophicLevel; //Is this used?
 	protected Vector velocity;
 	protected Gender gender;
 	protected AgentPath focusedPreyPath;
 	protected double fitness;
 	protected double maxSpeed;
 	protected double visionRange;
 	protected double maxAcceleration;
 	protected Container<IAgent> focusedPrey;
 	protected boolean isAlive = true;
 	protected boolean hungry = true;
 	protected static final boolean USE_PRIORITY_NEIGHBOURS = true;
 	protected static final int K_NEAREST_NEIGHBOURS = 5; 
 
 	/* Neighbour list module variables */
 	protected List<IAgent> preyNeighbours;
 	protected List<IAgent> predNeighbours;
 	protected List<IAgent> neutralNeighbours;
 	
 	protected FixedSizeAgentQueueObjectPriorityQueue preyNeighboursQueue;
 	protected FixedSizeAgentQueueObjectPriorityQueue predNeighboursQueue;
 	protected FixedSizeAgentQueueObjectPriorityQueue neutralNeighboursQueue;
 
 	//protected List<IObstacle> obstacles; // TODO remove this? Was only used by PigAgent
 	private int neighbourCounter;
 	private static final int NEIGHBOURS_UPDATE_THRESHOLD = 10;
 	protected static Random ran = new Random();
 
 	protected final static double INTERACTION_RANGE = 10;
 	protected final static double EATING_RANGE = 5;
	protected final static double FOCUS_RANGE = 150;
	protected final static double OBSTACLE_SAFETY_DISTANCE = 10;
 	protected static final double VELOCITY_DECAY = 1;
 
 	public AbstractAgent(String name, Position position, Color color,
 			int width, int height, Vector velocity, double maxSpeed,
 			double visionRange, double maxAcceleration) {
 		this.name = name;
 		this.position = position;
 		this.color = color;
 		this.width = width;
 		this.height = height;
 		this.velocity = velocity;
 		this.maxSpeed = maxSpeed;
 		this.visionRange = visionRange;
 		this.maxAcceleration = maxAcceleration;
 		this.focusedPreyPath = new AgentPath();
 		/*
 		 * LinkedList for fast changing of Agents, consider ArrayList for less
 		 * memory
 		 */
 		preyNeighbours = new ArrayList<IAgent>(256);
 		predNeighbours = new ArrayList<IAgent>(256);
 		neutralNeighbours = new ArrayList<IAgent>(256);
 		preyNeighboursQueue = new FixedSizeAgentQueueObjectPriorityQueue(K_NEAREST_NEIGHBOURS);
 		predNeighboursQueue = new FixedSizeAgentQueueObjectPriorityQueue(K_NEAREST_NEIGHBOURS);
 		neutralNeighboursQueue = new FixedSizeAgentQueueObjectPriorityQueue(K_NEAREST_NEIGHBOURS);
 		
 		focusedPrey = new Container<IAgent>(null);
 		// To update the first time.
 		neighbourCounter = ran.nextInt(NEIGHBOURS_UPDATE_THRESHOLD);
 	}
 
 	public AbstractAgent(String name, Position p, Color c, int width,
 			int height, Vector velocity, double maxSpeed, double visionRange,
 			double maxAcceleration, int capacity, boolean groupBehaviour) {
 
 		// To update the first time.
 		neighbourCounter = ran.nextInt(NEIGHBOURS_UPDATE_THRESHOLD);
 	}
 
 	/**
 	 * Clone constructor. Use this to create a copy.
 	 * 
 	 * @param a
 	 *            - The agent to clone.
 	 */
 	public AbstractAgent(AbstractAgent a) {
 		this(a.name, new Position(a.position), a.color, a.width, a.height,
 				new Vector(a.velocity), a.maxSpeed, a.visionRange,
 				a.maxAcceleration);
 		this.focusedPreyPath = a.focusedPreyPath;
 	}
 
 	public AbstractAgent(String name, Position pos, Color color, int width,
 			int height) {
 		this.name = name;
 		position = new Position(pos);
 		this.color = color;
 		this.width = width;
 		this.height = height;
 
 		/*
 		 * LinkedList for fast changing of Agents, consider ArrayList for less
 		 * memory
 		 */
 		preyNeighbours = new LinkedList<IAgent>();
 		predNeighbours = new LinkedList<IAgent>();
 		neutralNeighbours = new LinkedList<IAgent>();
 
 		// To update the first time.
 		neighbourCounter = NEIGHBOURS_UPDATE_THRESHOLD;
 	}
 
 	@Override
 	public Position getPosition() {
 		return position;
 	}
 
 	@Override
 	public void setPosition(Position position) {
 		this.position = position;
 	}
 
 	@Override
 	public String getName() {
 		return name;
 	}
 
 	@Override
 	public Color getColor() {
 		return color;
 	}
 
 	@Override
 	public int getWidth() {
 		return width;
 	}
 
 	@Override
 	public int getHeight() {
 		return height;
 	}
 
 	@Override
 	public Vector getVelocity() {
 		return velocity;
 	}
 
 	@Override
 	public void setVelocity(Vector velocity) {
 		this.velocity.setVector(velocity.getX(), velocity.getY());
 
 	}
 
 	@Override
 	public double getFitness() {
 		return fitness;
 	}
 
 	@Override
 	public void setFitness(double fitness) {
 		this.fitness = fitness;
 	}
 
 	@Override
 	public Gender getGender() {
 		return gender;
 	}
 
 	@Override
 	public int getLifeLength() {
 		return lifeLength;
 	}
 
 	@Override
 	public int getTrophicLevel() {
 		return trophicLevel;
 	}
 
 	@Override
 	public double getMaxAcceleration() {
 		return maxAcceleration;
 	}
 
 	@Override
 	public double getMaxSpeed() {
 		return maxSpeed;
 	}
 
 	@Override
 	public double getVisionRange() {
 		return visionRange;
 	}
 
 	@Override
 	public IAgent cloneAgent() throws CloneNotSupportedException {
 		return (IAgent) clone();
 	}
 
 	@Override
 	protected Object clone() throws CloneNotSupportedException {
 		AbstractAgent a = new AbstractAgent(name, position, color, width,
 				height, velocity, maxSpeed, visionRange, maxAcceleration) {
 			@Override
 			public void calculateNextPosition(List<IPopulation> predators,
 					List<IPopulation> preys, List<IPopulation> neutral,
 					SurroundingsSettings surroundings) {
 			}
 
 			@Override
 			public List<IAgent> reproduce(IAgent agent, int populationSize,
 					SurroundingsSettings surroundings) {
 				// TODO Auto-generated method stub
 				return null;
 			}
 		};
 		a.focusedPreyPath = focusedPreyPath;
 		return a;
 	}
 
 	/**
 	 * Updates an agents position after all calculations for all agents have
 	 * been done.
 	 */
 	@Override
 	public void updatePosition() {
 		this.reUsedPosition = position;
 		this.position = nextPosition;
 		this.lifeLength++;
 	}
 
 	/**
 	 * Update the neighbourlist, should be done once in a while.
 	 * <p>
 	 * Warning, heavy computation! Needs optimizations.
 	 * 
 	 * @param neutral
 	 * @param prey
 	 * @param pred
 	 * @author Erik
 	 */
 	public void updateNeighbourList(List<IPopulation> neutral,
 			List<IPopulation> prey, List<IPopulation> pred) {
 		if (neighbourCounter++ < NEIGHBOURS_UPDATE_THRESHOLD) {
 			// Don't update just yet.
 			return;
 		}
 		// Log.v("Updating Neighbourlist! Agent: " + toString());
 		neighbourCounter = 0;
 		// neutralNeighbours = new
 		// ArrayList<IAgent>(2*neutralNeighbours.size());
 		// predNeighbours = new ArrayList<IAgent>(2*predNeighbours.size());
 		// preyNeighbours = new ArrayList<IAgent>(2*preyNeighbours.size());
 
 		
 		
 		if(USE_PRIORITY_NEIGHBOURS) {
 			preyNeighboursQueue.clear();
 			predNeighboursQueue.clear();
 			if(groupBehaviour)
 				neutralNeighboursQueue.clear();
 		} else {
 			predNeighbours.clear();
 			preyNeighbours.clear();
 			if(groupBehaviour)
 				neutralNeighbours.clear();
 		}
 
 		if(groupBehaviour){
 			for (IPopulation p : neutral) {
 				for (IAgent a : p.getAgents()) {
 					if(a != this) {
 						double distance = a.getPosition().getDistance(position);
 						if (distance <= visionRange) {
 							if(USE_PRIORITY_NEIGHBOURS) {
 								neutralNeighboursQueue.insertWithOverflow(new AgentQueueObject(a, distance));
 							} else {
 								neutralNeighbours.add(a);
 							}
 							
 						}
 					}
 				}
 			}
 		}
 		
 		for (IPopulation p : prey) {
 			for (IAgent a : p.getAgents()) {
 				double distance = a.getPosition().getDistance(position);
 				if (distance <= visionRange) {
 					if(USE_PRIORITY_NEIGHBOURS) {
 						preyNeighboursQueue.insertWithOverflow(new AgentQueueObject(a, distance));
 					} else {
 						preyNeighbours.add(a);
 					}
 					
 					
 				}
 			}
 		}
 
 		for (IPopulation p : pred) {
 			for (IAgent a : p.getAgents()) {
 				double distance = a.getPosition().getDistance(position);
 				if (distance <= visionRange) {
 					if(USE_PRIORITY_NEIGHBOURS) {
 						predNeighboursQueue.insertWithOverflow(new AgentQueueObject(a, distance));
 					} else {
 						predNeighbours.add(a);
 					}
 					
 					
 				}
 			}
 		}
 		
 		if(USE_PRIORITY_NEIGHBOURS) {
 			preyNeighbours = preyNeighboursQueue.getAgentsInHeapAsList();
 			predNeighbours = predNeighboursQueue.getAgentsInHeapAsList();
 			if(groupBehaviour)
 				neutralNeighbours = neutralNeighboursQueue.getAgentsInHeapAsList();
 			
 //			System.out.println("--------- " + this.name + " NEUTRAL ---------");
 //			System.out.println(neutralNeighboursQueue.getHeapAsList().toString());
 //			System.out.println("---------------------------------------------------------------");
 //			
 //			System.out.println("--------- " + this.name + " PREY ---------");
 //			System.out.println(preyNeighboursQueue.getHeapAsList().toString());
 //			System.out.println("---------------------------------------------------------------");
 //			
 //			System.out.println("--------- " + this.name + " PREDATOR ---------");
 //			System.out.println(predNeighboursQueue.getHeapAsList().toString());
 //			System.out.println("---------------------------------------------------------------");
 		}
 		
 	}
 
 	@Override
 	public synchronized boolean tryConsumeAgent() {
 		if (isAlive) {
 			isAlive = false;
 			return true;
 		}
 		return false;
 	}
 
 	@Override
 	public boolean isAlive() {
 		return isAlive;
 	}
 
 	/**
 	 * Create a new IAgent from the raw parameters.
 	 * 
 	 * @return
 	 */
 	public static IAgent createFromFile(String input) {
 		String[] inputArray = input.split(";");
 		// String name = inputArray[0];
 		Position pos = new Position(Double.parseDouble(inputArray[0]),
 				Double.parseDouble(inputArray[1]));
 		Color c = new Color(Integer.parseInt(inputArray[2]),
 				Integer.parseInt(inputArray[3]),
 				Integer.parseInt(inputArray[4]));
 		int width = Integer.parseInt(inputArray[5]);
 		int height = Integer.parseInt(inputArray[6]);
 		Vector v = new Vector(Double.parseDouble(inputArray[7]),
 				Double.parseDouble(inputArray[8]));
 		// int maxSpeed = Integer.parseInt(inputArray[9]);
 		// int visionRange = Integer.parseInt(inputArray[10]);
 		// int maxAcceleration = Integer.parseInt(inputArray[11]);
 
 		AbstractAgent ab = new AbstractAgent("Noname", pos, c, width, height,
 				v, 0, 0, 0) {
 
 			@Override
 			public void calculateNextPosition(List<IPopulation> predators,
 					List<IPopulation> preys, List<IPopulation> neutral,
 					SurroundingsSettings surroundings) {
 
 			}
 
 			@Override
 			public List<IAgent> reproduce(IAgent agent, int populationSize,
 					SurroundingsSettings surroundings) {
 				// TODO Auto-generated method stub
 				return null;
 			}
 		};
 		return ab;
 	}
 
 	@Override
 	public String toBinaryString() {
 		StringBuilder sb = new StringBuilder();
 		sb.append(roundTwoDecimals(position.getX()));
 		sb.append(';');
 		sb.append(roundTwoDecimals(position.getY()));
 		sb.append(';');
 		sb.append(color.getRed());
 		sb.append(';');
 		sb.append(color.getGreen());
 		sb.append(';');
 		sb.append(color.getBlue());
 		sb.append(';');
 		sb.append(width);
 		sb.append(';');
 		sb.append(height);
 		sb.append(';');
 		sb.append(roundTwoDecimals(velocity.x));
 		sb.append(';');
 		sb.append(roundTwoDecimals(velocity.y));
 		// sb.append(';');
 		// sb.append(maxSpeed);
 		// sb.append(';');
 		// sb.append(visionRange);
 		// sb.append(';');
 		// sb.append(maxAcceleration);
 		return sb.toString();
 	}
 
 	@Override
 	public String toString() {
 		return "Agent - name: " + name + " Position:" + position.toString()
 				+ " Velocity: " + velocity.toString();
 	}
 
 	public static double roundTwoDecimals(double num) {
 		double result = num * 100;
 		result = Math.round(result);
 		result = result / 100;
 		return result;
 	}
 
 	@Override
 	public boolean isLookingTasty(IAgent agent, double visionRange) {
 		return true;
 	}
 
 	@Override
 	public void eat() { //TODO: This should not be here. Remove to force subclasses to implement.
 		// Do nothing special, should be overriden by advanced agents.
 	}
 	
 	@Override
 	public boolean isHungry(){
 		return hungry;
 	}
 	
 	@Override
 	public double impactForcesBy(){
 		return 1;
 	}
 	
 	public int getPreyNeighbourSize(){
 		return preyNeighbours.size();
 	}
 	
 	public int getPredatorNeighbourSize(){
 		return predNeighbours.size();
 	}
 	
 	public int getNeutralNeighbourSize(){
 		return neutralNeighbours.size();
 	}
 	
 	@Override
 	public List<Position> getFocusedPath(){
 		return focusedPreyPath.getPath();
 	}
 
 }
