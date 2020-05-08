 package chalmers.dax021308.ecosystem.model.agent;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Random;
 import java.util.Set;
 
 import chalmers.dax021308.ecosystem.model.environment.obstacle.IObstacle;
 import chalmers.dax021308.ecosystem.model.population.IPopulation;
 import chalmers.dax021308.ecosystem.model.util.Gender;
 import chalmers.dax021308.ecosystem.model.util.IShape;
 import chalmers.dax021308.ecosystem.model.util.Log;
 import chalmers.dax021308.ecosystem.model.util.Position;
 import chalmers.dax021308.ecosystem.model.util.Vector;
 
 /**
  * AbstractAgent with neighbourlist.
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
 	protected int capacity;
 	protected int lifeLength;
 	protected int energy = 1000; // set specific energy level in subclasses
 	protected int trophicLevel;
 	protected Vector velocity;
 	protected Gender gender;
 	protected double fitness;
 	protected double maxSpeed;
 	protected double visionRange;
 	protected double maxAcceleration;
 	protected IAgent focusedPrey;
 	private boolean isAlive = true;
 	
 	/* Neighbour list module variables */
 	protected List<IAgent> preyNeighbours;
 	protected List<IAgent> predNeighbours;
 	protected List<IAgent> neutralNeighbours;
 	
 	protected List<IObstacle> obstacles;
 	private int neighbourCounter;
 	private static final int NEIGHBOURS_UPDATE_THRESHOLD = 10;
 	protected static Random ran = new Random();
 
 	protected final static double INTERACTION_RANGE = 10;
 	protected final static double EATING_RANGE = 5;
 	protected final static double FOCUS_RANGE = 100;
 	protected final static double ENVIRONMENT_CONSTANT = 50;
 	protected final static double OBSTACLE_CONSTANT = 50;
 	protected static final double VELOCITY_DECAY = 1;
 	protected static final double RANDOM_FORCE_MAGNITUDE = 0.05;
 
 	public AbstractAgent(String name, Position p, Color c, int width,
 			int height, Vector velocity, double maxSpeed, double visionRange,
 			double maxAcceleration) {
 		this.name = name;
 		position = p;
 		color = c;
 		this.width = width;
 		this.height = height;
 		this.velocity = velocity;
 		this.maxSpeed = maxSpeed;
 		this.visionRange = visionRange;
 		this.maxAcceleration = maxAcceleration;
 		this.capacity = Integer.MAX_VALUE;
 		this.lifeLength = 0;
 		
 		/* LinkedList for fast changing of Agents, consider ArrayList for less memory */
 		preyNeighbours    = new ArrayList<IAgent>(256);
 		predNeighbours    = new ArrayList<IAgent>(256);
 		neutralNeighbours = new ArrayList<IAgent>(256);
 		
 		//To update the first time.
 		neighbourCounter = ran.nextInt(NEIGHBOURS_UPDATE_THRESHOLD);
 	}
 
 	public AbstractAgent(String name, Position p, Color c, int width,
 			int height, Vector velocity, double maxSpeed, double visionRange,
 			double maxAcceleration, int capacity, boolean groupBehaviour) {
 
 		this(name, p, c, width, height, velocity, maxSpeed, visionRange,
 				maxAcceleration);
 		this.capacity = capacity;
 		this.groupBehaviour = groupBehaviour;
 	}
 
 	/**
 	 * Clone constructor. Use this to create a copy.
 	 * @param a - The agent to clone.
 	 */
 	public AbstractAgent(AbstractAgent a) {
 		this(a.name, new Position(a.position), a.color, a.width, a.height,
 				new Vector(a.velocity), a.maxSpeed, a.visionRange,
 				a.maxAcceleration);
 	}
 
 	public AbstractAgent(String name, Position pos, Color color, int width,
 			int height) {
 		this.name = name;
 		position = new Position(pos);
 		this.color = color;
 		this.width = width;
 		this.height = height;
 		
 		/* LinkedList for fast changing of Agents, consider ArrayList for less memory */
 		preyNeighbours    = new LinkedList<IAgent>();
 		predNeighbours    = new LinkedList<IAgent>();
 		neutralNeighbours = new LinkedList<IAgent>();
 		
 		//To update the first time.
 		neighbourCounter = NEIGHBOURS_UPDATE_THRESHOLD;
 	}
 
 	@Override
 	public Position getPosition() {
 		return position;
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
 		this.velocity = velocity;
 
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
 		return this.lifeLength;
 	}
 
 	@Override
 	public int getEnergy() {
 		return energy;
 	}
 	
 	@Override
 	public int getTrophicLevel() {
 		return trophicLevel;
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
 					Dimension dim, IShape shape, List<IObstacle> obstacles) {
 			}
 
 			@Override
			public List<IAgent> reproduce(IAgent agent, int populationSize, List<IObstacle> obstacles, IShape shape, Dimension gridDimension) {
 				return Collections.emptyList();
 			}
 
 		};
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
 	 * <p> Warning, heavy computation!
 	 * Needs optimizations.
 	 * @param neutral
 	 * @param prey
 	 * @param pred
 	 * @author Erik 
 	 */
 	public void updateNeighbourList(List<IPopulation> neutral, List<IPopulation> prey, List<IPopulation> pred) {	
 		if(neighbourCounter++ < NEIGHBOURS_UPDATE_THRESHOLD) {
 			//Don't update just yet.
 			return;
 		}
 //		Log.v("Updating Neighbourlist! Agent: " + toString());
 		neighbourCounter = 0;
 //		neutralNeighbours = new ArrayList<IAgent>(2*neutralNeighbours.size());
 //		predNeighbours = new ArrayList<IAgent>(2*predNeighbours.size());
 //		preyNeighbours = new ArrayList<IAgent>(2*preyNeighbours.size());
 
 		neutralNeighbours.clear();
 		predNeighbours.clear();
 		preyNeighbours.clear();
 		
 		for(IPopulation p : neutral) {
 			for(IAgent a : p.getAgents()) {
 				if (a.getPosition().getDistance(position) <= visionRange) {
 					neutralNeighbours.add(a);
 				}
 			}
 		}
 		
 		for(IPopulation p : prey) {
 			for(IAgent a : p.getAgents()) {
 				if (a.getPosition().getDistance(position) <= visionRange) {
 					preyNeighbours.add(a);
 				}
 			}
 		}
 		
 		for(IPopulation p : pred) {
 			for(IAgent a : p.getAgents()) {
 				if (a.getPosition().getDistance(position) <= visionRange) {
 					predNeighbours.add(a);
 				}
 			}
 		}
 	}
 
 	/**
 	 * A random force that the agent gets influenced by. Can be
 	 * interpreted as an estimation error that the agent does in where
 	 * to head.
 	 * @return A vector pointing approximately in the same direction as the
 	 * agents velocity.
 	 * @author Sebbe
 	 */
 	protected Vector randomForce() {
 		double randX = -RANDOM_FORCE_MAGNITUDE + 2 * RANDOM_FORCE_MAGNITUDE
 				* Math.random();
 		double randY = -RANDOM_FORCE_MAGNITUDE + 2 * RANDOM_FORCE_MAGNITUDE
 				* Math.random();
 		return new Vector(velocity.x + randX, velocity.y + randY);
 	}
 
 	/**
 	 * The agent is influences by the mutual interaction force
 	 * because it is subject to attraction and repulsion from other
 	 * individuals that it wants to group with. This force describes the
 	 * relationship between the tendency to steer towards other groups
 	 * of agents, but not be to close to them either.
 	 * @param neutral - The population of neutral agents.
 	 * @return A vector with the force that this agent feels from other neutral
 	 * agents in that it interacts with.
 	 * @author Sebbe
 	 */
 	protected Vector mutualInteractionForce() {
 		Vector mutualInteractionForce = new Vector(0, 0);
 		Vector newForce = new Vector(0, 0);
 			IAgent agent;
 			int size = neutralNeighbours.size();
 			for (int i = 0; i < size; i++) {
 				agent = neutralNeighbours.get(i);
 				if (agent != this) {
 					Position p = agent.getPosition();
 					double distance = getPosition().getDistance(p);
 					double Q = 0; // Q is a function of the distance.
 					if (distance <= INTERACTION_RANGE) {
 						Q = -20 * (INTERACTION_RANGE - distance);
 					} else {
 						Q = 1;
 					}
 					newForce.x = p.getX() - this.getPosition().getX();
 					newForce.y = p.getY() - this.getPosition().getY();
 					double norm = newForce.getNorm();
 					double v = Q / (norm * distance);
 					newForce.x = newForce.x * v;
 					newForce.y = newForce.y * v;
 					mutualInteractionForce.x = (mutualInteractionForce.x + newForce.x);
 					mutualInteractionForce.y = (mutualInteractionForce.y + newForce.y);
 				}
 			}
 		return mutualInteractionForce;
 	}
 
 	/**
 	 * The tendency of an agent to continue moving forward with
 	 * its velocity.
 	 * @return The forward thrust force.
 	 * @author Sebbe
 	 */
 	protected Vector forwardThrust() {
 		double a = 0.1; // Scaling constant
 		double x = velocity.x;
 		double y = velocity.y;
 		double norm = velocity.getNorm();
 		Vector forwardThrust = new Vector(a * x / norm, a * y / norm);
 		return forwardThrust;
 	}
 
 	/**
 	 * This is the force that makes neighbouring agents to
 	 * equalize their velocities and therefore go in the same direction.
 	 * The sphere of incluence is defined as 2*INTERACTION_RANGE at the
 	 * moment.
 	 * @param neutral - The population of neutral agents.
 	 * @return a vector with the force influencing the agents to steer in the
 	 * same direction as other nearby agents.
 	 * @author Sebbe
 	 */
 	protected Vector arrayalForce() {
 		Vector arrayalForce = new Vector(0, 0);
 		Vector newForce = new Vector();
 		double nAgentsInVision = 0;
 		int size = neutralNeighbours.size();
 		IAgent agent;
 		for (int i = 0; i < size; i++) {
 			agent = neutralNeighbours.get(i);
 			if (agent != this) {
 				Position p = agent.getPosition();
 				double distance = getPosition().getDistance(p);
 				if (distance <= INTERACTION_RANGE * 2) {
 					newForce.setVector(0, 0);
 					newForce.add(agent.getVelocity());
 					newForce.add(velocity);
 					double h = 4; // Scaling constant
 					newForce.x *= h;
 					newForce.y *= h;
 					arrayalForce.x = (arrayalForce.x + newForce.x);
 					arrayalForce.y = (arrayalForce.y + newForce.y);
 					nAgentsInVision = nAgentsInVision + 1.0;
 				}
 			}
 		}
 		if (nAgentsInVision > 0) {
 			arrayalForce.x /= nAgentsInVision;
 			arrayalForce.y /= nAgentsInVision;
 		}
 		return arrayalForce;
 	}
 
 	/**
 	 * The environment force is at the moment defined as
 	 * 1/((wall-constant)*(distance to wall))^2. The agents feel the forces from
 	 * the wall directly to the left, right, top and bottom.
 	 * @param dim - The dimensions of the rectangular environment.
 	 * @return A vector with the force that an agent feel from its environment.
 	 * @author Sebbe
 	 */
 	protected Vector getEnvironmentForce(Dimension dim, IShape shape) {
 		/*
 		 * The positions below is just an orthogonal projection on to the walls.
 		 */
 		Position xWallLeft = shape.getXWallLeft(dim, position);
 		Position xWallRight = shape.getXWallRight(dim, position);
 		Position yWallBottom = shape.getYWallBottom(dim, position);
 		Position yWallTop = shape.getYWallTop(dim, position);
 
 		/*
 		 * There is a "-1" in the equation just to make it more unlikely that
 		 * they actually make it to the wall, despite the force they feel (can
 		 * be interpreted as they stop 1 pixel before the wall).
 		 */
 		Vector environmentForce = new Vector(0, 0);
 		double xWallLeftForce = 0;
 		double xWallRightForce = 0;
 		double yWallBottomForce = 0;
 		double yWallTopForce = 0;
 
 		/*
 		 * Only interacts with walls that are closer than INTERACTION_RANGE.
 		 */
 		double distance = 1;
 		double leftWallDistance = this.getPosition().getDistance(xWallLeft);
 		if (leftWallDistance <= INTERACTION_RANGE) {
 			xWallLeftForce = 1 / (leftWallDistance * leftWallDistance);
 		}
 
 		double rightWallDistance = this.getPosition().getDistance(xWallRight);
 		if (rightWallDistance <= INTERACTION_RANGE) {
 			xWallRightForce = -1 / (rightWallDistance * rightWallDistance);
 		}
 
 		double bottomWallDistance = this.getPosition().getDistance(yWallBottom);
 		if (bottomWallDistance <= INTERACTION_RANGE) {
 			yWallBottomForce = 1 / (bottomWallDistance * bottomWallDistance);
 		}
 
 		double topWallDistance = this.getPosition().getDistance(yWallTop);
 		if (topWallDistance <= INTERACTION_RANGE) {
 			yWallBottomForce = yWallTopForce = -1 / (topWallDistance * topWallDistance);
 		}
 
 		/*
 		 * Add the forces from left and right to form the total force from walls
 		 * in x-axis. Add the forces from top and bottom to form the total force
 		 * from walls in y-axis. Create a force vector of the forces.
 		 */
 		double xForce = (xWallLeftForce + xWallRightForce);
 		double yForce = (yWallBottomForce + yWallTopForce);
 		environmentForce.setVector(xForce, yForce);
 
 		return environmentForce.multiply(ENVIRONMENT_CONSTANT);
 	}
 	
 	@Override
 	public Vector getObstacleForce(List<IObstacle> obstacles){
 		Vector obstacleForce = new Vector();
 		for(IObstacle o: obstacles){
 			if(o.isCloseTo(this.position, INTERACTION_RANGE)) {
 				Position obstaclePos = o.closestBoundary(this.position);
 				double distance = position.getDistance(obstaclePos);
 				if(distance <= INTERACTION_RANGE){
 					Vector singleForce = new Vector(position, obstaclePos);
 					singleForce.toUnitVector().multiply(1/(distance*distance));
 					obstacleForce.add(singleForce);
 				}
 			}	
 		}
 		return obstacleForce.multiply(OBSTACLE_CONSTANT);
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
 	 * @return
 	 */
 	public static IAgent createFromFile(String input) {
 		String[] inputArray = input.split(";");
 //		String name = inputArray[0];
 		Position pos = new Position(Double.parseDouble(inputArray[0]), Double.parseDouble(inputArray[1]));
 		Color c = new Color(Integer.parseInt(inputArray[2]),
 					Integer.parseInt(inputArray[3]),
 					Integer.parseInt(inputArray[4]));
 		int width = Integer.parseInt(inputArray[5]);
 		int height = Integer.parseInt(inputArray[6]);
 		Vector v = new Vector(Double.parseDouble(inputArray[7]), Double.parseDouble(inputArray[8]));
 //		int maxSpeed = Integer.parseInt(inputArray[9]);
 //		int visionRange = Integer.parseInt(inputArray[10]);
 //		int maxAcceleration = Integer.parseInt(inputArray[11]);
 
 		AbstractAgent ab = new AbstractAgent("Noname", pos, c, width, height, v,
 				0, 0, 0) {

			@Override
			public List<IAgent> reproduce(IAgent agent, int populationSize, List<IObstacle> obstacles, IShape shape, Dimension gridDimension) {
				return null;
			}

 			@Override
 			public void calculateNextPosition(List<IPopulation> predators,
 					List<IPopulation> preys, List<IPopulation> neutral,
 					Dimension dim, IShape shape, List<IObstacle> obstacles) {
 
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
 //		sb.append(';');
 //		sb.append(maxSpeed);
 //		sb.append(';');
 //		sb.append(visionRange);
 //		sb.append(';');
 //		sb.append(maxAcceleration);
 		return sb.toString();
 	}
 	
 	@Override
 	public String toString() {
 		return "Agent - name: " + name + " Position:" + position.toString() + " Velocity: " + velocity.toString();
 	}
 	
 	
 	public static double roundTwoDecimals(double num) {
 		double result = num * 100;
 		result = Math.round(result);
 		result = result / 100;
 		return result;
 	}
 }
