 package chalmers.dax021308.ecosystem.model.agent;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.util.Collections;
 import java.util.List;
 
 import chalmers.dax021308.ecosystem.model.population.IPopulation;
 import chalmers.dax021308.ecosystem.model.util.Gender;
 import chalmers.dax021308.ecosystem.model.util.Position;
 import chalmers.dax021308.ecosystem.model.util.Vector;
 /**
  * 
  * @author Henrik
  * Abstract class for handling the dummy methods
  */
 public abstract class AbstractAgent implements IAgent {
 	protected Position position;
 	protected Position nextPosition;
 	protected String name;
 	protected Color color;
 	protected int width;
 	protected int height;
 	protected int capacity;
 	protected Vector velocity;
 	protected Gender gender;
 	protected double fitness;
 	protected double maxSpeed;
 	protected double visionRange;
 	protected double maxAcceleration;
 	protected final static double INTERACTION_RANGE = 4;
 	protected final static double WALL_CONSTANT = 1;
 	protected static final double VELOCITY_DECAY = 1;
 	
 	public AbstractAgent(String name, Position p, Color c, int width, int height, Vector velocity, 
 			double maxSpeed, double visionRange, double maxAcceleration){
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
 	}
 	
 	public AbstractAgent(String name, Position p, Color c, int width, int height, Vector velocity, 
 			double maxSpeed, double visionRange, double maxAcceleration, int capacity){
 		this(name, p, c, width, height, velocity, maxSpeed, visionRange, maxAcceleration);
 		this.capacity = capacity;
 	}
 	
 	/**
 	 * Clone constructed. Use this to create a copy.
 	 * @param a
 	 */
 	public AbstractAgent(AbstractAgent a) {
 		 this(a.name, a.position, a.color, a.width, a.height, a.velocity, a.maxSpeed, a.visionRange, a.maxAcceleration);
 	}
 	
 	public AbstractAgent(String name, Position pos, Color color,
 			int width, int height) {
 		this.name = name;
 		position = pos;
 		this.color = color;
 		this.width = width;
 		this.height = height;
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
 	public void updatePosition() {
 		this.position = new Position(nextPosition);
 	}
 
 	@Override
 	public Gender getGender() {
 		return gender;
 	}
 	
 	@Override
 	public IAgent cloneAgent() throws CloneNotSupportedException {
 		return (IAgent) clone();
 	}
 	
 	@Override
 	protected Object clone() throws CloneNotSupportedException {
 		AbstractAgent a = new AbstractAgent(name, new Position(position), color, width, height, new Vector(velocity), maxSpeed, visionRange, maxAcceleration) {
 			@Override
 			public void calculateNextPosition(List<IPopulation> predators,
 					List<IPopulation> preys, List<IPopulation> neutral, Dimension dim) {
 			}
 			@Override
 			public List<IAgent> reproduce(IAgent agent, int populationSize) {
 				return Collections.emptyList();
 			}
 			@Override
 			public String toBinaryString() {
 				// TODO Auto-generated method stub
 				return null;
 			}
 
 		};
 		return a;
 	}
 	
 	/**
 	 * @author Sebbe
 	 * @param neutral the population of neutral agents that this agent should be separated from (not collide with).
 	 * @return a vector with the force that this agent feels from other neutral agents in order not to collide with them.
 	 * <p>
 	 * Warning! Not optimal for linked-lists, due to O(n) complexity of linked list get(n) method. 
 	 * TODO: Special method for linked list using collection.iterator(), hasNext() & next().
 	 */
 	protected Vector getSeparationForce(List<IPopulation> neutral){
 		//Allocating new object here is ok since its only 1 per method call. //Erik
 		Vector separationForce = new Vector(0,0);
 		//int nVisiblePredators = 0; //Unused?
 		IPopulation pop;
 		int popSize = neutral.size();
 		for(int j = 0 ; j < popSize ; j++) {
 			pop = neutral.get(j);
 			int size = pop.getAgents().size();
 			List<IAgent> agents = pop.getAgents();
 			IAgent agent;
 			for(int i = 0; i < size; i++) {
 				agent = agents.get(i);
 				if(agent != this) {
 					Position p = agent.getPosition();
 					double distance = getPosition().getDistance(p);
 					if(distance<=INTERACTION_RANGE){ //If neutral is in vision range for prey
 						/*
 						 * Create a vector that points away from the neutral.
 						 * TODO: Remove the "new Vector" and replace with doubles. This will be called alot of times.
 						 * Low level programming is crucial.
 						 */
 						Vector newForce = new Vector(this.getPosition(),p);
 						
 						/*
 						 * Add this vector to the separation force, with proportion to how close the neutral agent is.
 						 * Closer agents will affect the force more than those far away. 
 						 */
 						double norm = newForce.getNorm();
 						double v = 1/(norm*distance*distance);
 						newForce.x = newForce.x * v;
 						newForce.y = newForce.y * v;
 						separationForce.x = separationForce.x + newForce.x;
 						separationForce.y = separationForce.y + newForce.y;
 						
 						//nVisiblePredators++;//Unused?
 					}
 				}
 			}
 		}
 		return separationForce;		
 	}
 	
 	/**
 	 * * @author Sebbe
 	 * The environment force is at the moment defined as 1/(wall-constant*(distance to wall)^2).
 	 * The agents feel the forces from the wall directly to the left, right, top and bottom.
 	 * @param dim The dimensions of the rectangular environment.
 	 * @return A vector with the force that an agent feel from its environment.
 	 */
 	protected Vector getEnvironmentForce(Dimension dim){
 		/*
 		 * The positions below is just an orthogonal projection on to the walls.
 		 */
 		Position xWallLeft = new Position(0,this.getPosition().getY());
 		Position xWallRight = new Position(dim.getWidth(),this.getPosition().getY());
 		Position yWallBottom = new Position(this.getPosition().getX(),0);
 		Position yWallTop = new Position(this.getPosition().getX(),dim.getHeight());
 		
 		/*
 		 * There is a "-1" in the equation just to make it more unlikely that they actually make it to the wall,
 		 * despite the force they feel (can be interpreted as they stop 1 pixel before the wall).
 		 */
 		Vector environmentForce = new Vector(0,0);
 		double xWallLeftForce = 0;
 		double xWallRightForce = 0;
 		double yWallBottomForce = 0;
 		double yWallTopForce = 0;
 		
 		/*
 		 * Only interacts with walls that are closer than INTERACTION_RANGE.
 		 */
 		double leftWallDistance = this.getPosition().getDistance(xWallLeft);
 		if(leftWallDistance<=INTERACTION_RANGE){
 			xWallLeftForce = 1/Math.pow((leftWallDistance-1.0)/WALL_CONSTANT,2);
 		}
 		
 		double rightWallDistance = this.getPosition().getDistance(xWallRight);
 		if(rightWallDistance<=INTERACTION_RANGE){
 			xWallRightForce = -1/Math.pow((rightWallDistance-1.0)/WALL_CONSTANT,2);
 		}
 		
 		double bottomWallDistance = this.getPosition().getDistance(yWallBottom);
 		if(bottomWallDistance<=INTERACTION_RANGE){
 			yWallBottomForce = 1/Math.pow((bottomWallDistance-1.0)/WALL_CONSTANT,2);
 		}
 		
 		double topWallDistance = this.getPosition().getDistance(yWallTop);
 		if(topWallDistance<=INTERACTION_RANGE){
 			yWallBottomForce = yWallTopForce = -1/Math.pow((topWallDistance-1.0)/WALL_CONSTANT,2);
 		}
 		
 		/*
 		 * Add the forces from left and right to form the total force from walls in x-axis.
 		 * Add the forces from top and bottom to form the total force from walls in y-axis.
 		 * Create a force vector of the forces.
 		 */
 		double xForce = (xWallLeftForce + xWallRightForce);
 		double yForce = (yWallBottomForce + yWallTopForce);
 		environmentForce.setVector(xForce, yForce);
 		
 		return environmentForce;
 	}
 
 	
 	/**
 	 * Create a new IAgent from the raw parameters.
 	 * @return
 	 */
 	public static IAgent createFromFile(String input) {
 		String[] inputArray = input.split(";");
 		String name = inputArray[0];
 		Position pos = new Position(Integer.parseInt(inputArray[1]), Integer.parseInt(inputArray[2]));
 		Color c = new Color(Integer.parseInt(inputArray[3]), Integer.parseInt(inputArray[4]), Integer.parseInt(inputArray[5]));
 		int width = Integer.parseInt(inputArray[6]);
 		int height = Integer.parseInt(inputArray[7]);
 		Vector v = new Vector(Integer.parseInt(inputArray[8]), Integer.parseInt(inputArray[9]));
 		int maxSpeed        = Integer.parseInt(inputArray[10]);
 		int visionRange     = Integer.parseInt(inputArray[11]);
 		int maxAcceleration = Integer.parseInt(inputArray[12]);
 		
 		AbstractAgent ab = new AbstractAgent(name, pos, c, width, height, v, maxSpeed, visionRange, maxAcceleration) {
 			
 			@Override
 			public List<IAgent> reproduce(IAgent agent, int populationSize) {
 				return null;
 			}
 			
 			@Override
 			public void calculateNextPosition(List<IPopulation> predators,
 					List<IPopulation> preys, List<IPopulation> neutral, Dimension dim) {
 				
 			}
 		};
 		return ab;
 	}
 	
 	@Override
 	public String toBinaryString() {
 		StringBuilder sb = new StringBuilder();
 		sb.append(name);
 		sb.append(';');
 		sb.append(position.getX());
 		sb.append(';');
 		sb.append(position.getY());
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
 		sb.append(velocity.x);
 		sb.append(';');
 		sb.append(velocity.y);
 		sb.append(';');
 		sb.append(maxSpeed);
 		sb.append(';');
 		sb.append(visionRange);
 		sb.append(';');
 		sb.append(maxAcceleration);
 		return sb.toString();
 	}
 
 }
