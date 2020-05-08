 package cwcore;
 
 import java.awt.Color;
 import java.io.Serializable;
 import java.util.Collection;
 import java.util.LinkedHashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Set;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 
 import cobweb.ColorLookup;
 import cobweb.Direction;
 import cobweb.DrawingHandler;
 import cobweb.Environment;
 import cobweb.Environment.Location;
 import cobweb.Point2D;
 import cobweb.TypeColorEnumeration;
 import cwcore.ComplexEnvironment.Drop;
 import cwcore.ComplexEnvironment.Waste;
 import cwcore.broadcast.BroadcastPacket;
 import cwcore.complexParams.AgentMutator;
 import cwcore.complexParams.ComplexAgentParams;
 import cwcore.complexParams.ContactMutator;
 import cwcore.complexParams.SpawnMutator;
 import cwcore.complexParams.StepMutator;
 import driver.ControllerFactory;
 
 /**
  * Consists of implementations of the TickScheduler.Client and the 
  * Serializable classes, and is an extension of the cobweb.Agent class. 
  * 
  * <p>During each tick of a simulation, each ComplexAgent instance will 
  * be used to call the tickNotification method.  This is done in the 
  * TickScheduler.doTick private method. 
  * 
  * @author ???
  * @see cobweb.Agent
  * @see ComplexAgent#tickNotification(long)
  * @see java.io.Serializable
  *
  */
 public class ComplexAgent extends cobweb.Agent implements cobweb.TickScheduler.Client, Serializable{
 
 	/**
 	 * This class provides the information of what an agent sees.
 	 *
 	 */
 	static class SeeInfo {
 		private int dist;
 
 		private int type;
 
 		/**
 		 * Contains the information of what the agent sees.
 		 * 
 		 * @param d Distance to t.
 		 * @param t Type of object seen.
 		 */
 		public SeeInfo(int d, int t) {
 			dist = d;
 			type = t;
 		}
 
 		/**
 		 * @return How far away the object is.
 		 */
 		public int getDist() {
 			return dist;
 		}
 
 		/**
 		 * @return What the agent sees (rock, food, etc.)
 		 */
 		public int getType() {
 			return type;
 		}
 	}
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = -5310096345506441368L;
 
 	/** Default mutable parameters of each agent type. */
 
 	private static ComplexAgentParams defaultParams[];
 
 	protected static AgentSimilarityCalculator simCalc;
 
 	public static Collection<String> logDataAgent(int i) {
 		List<String> blah = new LinkedList<String>();
 		for (SpawnMutator mut : spawnMutators) {
 			for (String s : mut.logDataAgent(i))
 				blah.add(s);
 		}
 		return blah;
 	}
 
 	public static Iterable<String> logDataTotal() {
 		List<String> blah = new LinkedList<String>();
 		for (SpawnMutator mut : spawnMutators) {
 			for (String s : mut.logDataTotal())
 				blah.add(s);
 		}
 		return blah;
 	}
 
 	public static Collection<String> logHederAgent() {
 		List<String> blah = new LinkedList<String>();
 		for (SpawnMutator mut : spawnMutators) {
 			for (String s : mut.logHeadersAgent())
 				blah.add(s);
 		}
 		return blah;
 	}
 
 	public static Iterable<String> logHederTotal() {
 		List<String> blah = new LinkedList<String>();
 		for (SpawnMutator mut : spawnMutators) {
 			for (String s : mut.logHeaderTotal())
 				blah.add(s);
 		}
 		return blah;
 	}
 
 	/** Sets the default mutable parameters of each agent type. */
 	public static void setDefaultMutableParams(ComplexAgentParams[] params) {
 		defaultParams = params.clone();
 		for (int i = 0; i < params.length; i++) {
 			defaultParams[i] = (ComplexAgentParams) params[i].clone();
 		}
 	}
 
 	public static void setSimularityCalc(AgentSimilarityCalculator calc) {
 		simCalc = calc;
 	}
 
 	protected int agentType = 0;
 
 
 	public ComplexAgentParams params;
 
 	/* energy gauge */
 	protected int energy;
 	/* Prisoner's Dilemma */
 	private int agentPDStrategy; // tit-for-tat or probability
 	int pdCheater; // The agent's action; 1 == cheater, else
 	// cooperator
 	private int lastPDMove; // Remember the opponent's move in the last game
 
 	private int commInbox;
 
 	private int commOutbox;
 	// memory size is the maximum capacity of the number of cheaters an agent
 	// can remember
 	protected long photo_memory[];
 	private int photo_num = 0;
 	protected boolean want2meet = false;
 	boolean cooperate;
 	private long birthTick = 0;
 	protected long age = 0;
 
 	/* Waste variables */
 	private int wasteCounterGain;
 	private int wasteCounterLoss;
 
 	private int memoryBuffer;
 
 
 	protected ComplexAgent breedPartner;
 	private Color color = Color.lightGray;
 
 	private boolean asexFlag;
 
 	protected ComplexAgentInfo info;
 
 	// pregnancyPeriod is set value while pregPeriod constantly changes
 	protected int pregPeriod;
 
 	protected boolean pregnant = false;
 
 	public static final int LOOK_DISTANCE = 4;
 
 	/** The current tick we are in (or the last tick this agent was notified */
 	protected long currTick = 0;
 
 	private static ColorLookup colorMap = TypeColorEnumeration.getInstance();
 
 	static Set<ContactMutator> contactMutators = new LinkedHashSet<ContactMutator>();
 
 	static Set<StepMutator> stepMutators = new LinkedHashSet<StepMutator>();
 
 	private static final cobweb.Direction[] dirList = { cobweb.Environment.DIRECTION_NORTH,
 		cobweb.Environment.DIRECTION_SOUTH, cobweb.Environment.DIRECTION_WEST, cobweb.Environment.DIRECTION_EAST,
 		cobweb.Environment.DIRECTION_NORTHEAST, cobweb.Environment.DIRECTION_SOUTHEAST,
 		cobweb.Environment.DIRECTION_NORTHWEST, cobweb.Environment.DIRECTION_SOUTHWEST };
 
 	public static void addMutator(AgentMutator mutator) {
 		if (mutator instanceof SpawnMutator)
 			spawnMutators.add((SpawnMutator) mutator);
 
 		if (mutator instanceof ContactMutator)
 			contactMutators.add((ContactMutator) mutator);
 
 		if (mutator instanceof StepMutator)
 			stepMutators.add((StepMutator) mutator);
 	}
 
 	public static void clearMutators() {
 		spawnMutators.clear();
 		contactMutators.clear();
 		stepMutators.clear();
 	}
 
 	protected cobweb.Direction facing = cobweb.Environment.DIRECTION_NORTH;
 
 	private static Set<SpawnMutator> spawnMutators = new LinkedHashSet<SpawnMutator>();
 
 	boolean mustFlip = false;
 
 	protected ComplexEnvironment environment;
 
 	public ComplexAgent() {
 
 	}
 
 	/**
 	 * Constructor with two parents
 	 *
 	 * @param pos spawn position
 	 * @param parent1 first parent
 	 * @param parent2 second parent
 	 * @param strat PD strategy
 	 */
 	public void init(cobweb.Environment.Location pos, ComplexAgent parent1, ComplexAgent parent2, int strat) {
 		init(ControllerFactory.createFromParents(parent1.getController(), parent2.getController(),
 				parent1.params.mutationRate));
 		InitFacing();
 
 		copyConstants(parent1);
 
 		environment = ((ComplexEnvironment) (pos.getEnvironment()));
 		info = environment.addAgentInfo(agentType, parent1.info, parent2.info, strat);
 
 		move(pos);
 
 		pos.getEnvironment().getScheduler().addSchedulerClient(this);
 
 		for (SpawnMutator mutator : spawnMutators)
 			mutator.onSpawn(this, parent1, parent2);
 
 	}
 
 
 	/**
 	 * Constructor with a parent; standard asexual copy
 	 *
 	 * @param pos spawn position
 	 * @param parent parent
 	 * @param strat PD strategy
 	 */
 	protected void init(cobweb.Environment.Location pos, ComplexAgent parent, int strat) {
 		init(ControllerFactory.createFromParent(parent.getController(), parent.params.mutationRate));
 		InitFacing();
 
 		copyConstants(parent);
 		environment = ((ComplexEnvironment) (pos.getEnvironment()));
 		info = environment.addAgentInfo(agentType, parent.info, strat);
 
 		move(pos);
 
 		pos.getEnvironment().getScheduler().addSchedulerClient(this);
 
 		for (SpawnMutator mutator : spawnMutators)
 			mutator.onSpawn(this, parent);
 	}
 
 	/**   */
 	public void init(int agentT, int doCheat, ComplexAgentParams agentData, Direction facingDirection, Location pos) {
 		init(ControllerFactory.createNew(agentData.memoryBits, agentData.communicationBits));
 		setConstants(doCheat, agentData);
 		this.facing = facingDirection;
 
 		environment = ((ComplexEnvironment) (pos.getEnvironment()));
 		info = environment.addAgentInfo(agentT, doCheat);
 
 		move(pos);
 
 		pos.getEnvironment().getScheduler().addSchedulerClient(this);
 
 		for (SpawnMutator mutator : spawnMutators)
 			mutator.onSpawn(this);
 	}
 
 	/**
 	 * Constructor with no parent agent; creates an agent using "immaculate conception" technique
 	 *
 	 * @param agentType agent type
 	 * @param pos spawn position
 	 * @param doCheat start PD off cheating?
 	 * @param agentData agent parameters
 	 */
 	public void init(int agentType, Location pos, int doCheat, ComplexAgentParams agentData) {
 		init(ControllerFactory.createNew(agentData.memoryBits, agentData.communicationBits));
 		setConstants(doCheat, agentData);
 
 		InitFacing();
 
 		params = agentData;
 		environment = ((ComplexEnvironment) (pos.getEnvironment()));
 		info = environment.addAgentInfo(agentType, doCheat);
 		this.agentType = agentType;
 
 		move(pos);
 
 		pos.getEnvironment().getScheduler().addSchedulerClient(this);
 
 		for (SpawnMutator mutator : spawnMutators)
 			mutator.onSpawn(this);
 	}
 
 	private void afterTurnAction() {
 		energy -= energyPenalty();
 		if (energy <= 0)
 			die();
 		if (!pregnant)
 			tryAsexBreed();
 		if (pregnant) {
 			pregPeriod--;
 		}
 	}
 
 	@Override
 	public long birthday() {
 		return birthTick;
 	}
 
 	void broadcastCheating(cobweb.Environment.Location loc) { // []SK
 		// String message = "Cheater encountered (" + loc.v[0] + " , " + loc.v[1] + ")";
 		String message = Long.toString(((ComplexAgent) loc.getAgent()).id);
 		BroadcastPacket msg = new BroadcastPacket(BroadcastPacket.CHEATER, id, message, energy
 				, params.broadcastEnergyBased, params.broadcastFixedRange, getPosition());
 		environment.commManager.addPacketToList(msg);
 		// new CommPacket sent
 		energy -= params.broadcastEnergyCost; // Deduct broadcasting cost from energy
 	}
 
 	/**
 	 * Creates a new communication packet.  The energy to broadcast is 
 	 * deducted here.
 	 * 
 	 * @param loc The location of food.
 	 */
 	void broadcastFood(cobweb.Environment.Location loc) { // []SK
 		String message = loc.toString();
 		BroadcastPacket msg = new BroadcastPacket(BroadcastPacket.FOOD, id, message, energy
 				, params.broadcastEnergyBased, params.broadcastFixedRange, getPosition());
 		environment.commManager.addPacketToList(msg);
 		// new CommPacket sent
 		energy -= params.broadcastEnergyCost; // Deduct broadcasting cost from energy
 	}
 
 	/**
 	 * @return True if agent has enough energy to broadcast
 	 */
 	protected boolean canBroadcast() {
 		return energy > params.broadcastEnergyMin;
 	}
 
 	/**
 	 * @param destPos The location of the agents next position.
 	 * @return True if agent can eat this type of food.
 	 */
 	public boolean canEat(cobweb.Environment.Location destPos) {
 		return params.foodweb.canEatFood[environment.getFoodType(destPos)];
 	}
 
 	/**
 	 * @param adjacentAgent The agent attempting to eat.
 	 * @return True if the agent can eat this type of agent.
 	 */
 	protected boolean canEat(ComplexAgent adjacentAgent) {
 		boolean caneat = false;
 		caneat = params.foodweb.canEatAgent[adjacentAgent.getAgentType()];
 		if (this.energy > params.breedEnergy)
 			caneat = false;
 
 		return caneat;
 	}
 
 	/**
 	 * @param destPos The location of the agents next position.
 	 * @return True if location exists and is not occupied by anything
 	 */
 	boolean canStep(Location destPos) {
 		// The position must be valid...
 		if (destPos == null)
 			return false;
 		// and the destination must be clear of stones
 		if (destPos.testFlag(ComplexEnvironment.FLAG_STONE))
 			return false;
 		// and clear of wastes
 		if (destPos.testFlag(ComplexEnvironment.FLAG_DROP))
 			return environment.dropArray[destPos.v[0]][destPos.v[1]].canStep();
 		// as well as other agents...
 		if (destPos.getAgent() != null)
 			return false;
 		return true;
 	}
 
 	boolean checkCredibility(long agentId) {
 		// check if dispatcherId is in list
 		// if (agentId != null) {
 		for (int i = 0; i < params.pdMemory; i++) {
 			if (photo_memory[i] == agentId) {
 				return false;
 			}
 		}
 		// }
 		return true;
 	}
 
 
 	BroadcastPacket checkforBroadcasts() {
 		return environment.commManager.findPacket(getPosition());
 	}
 
 	//@Override
 	//	public Object clone() {
 	//		ComplexAgent cp = new ComplexAgent(getAgentType(), pdCheater, params, facing);
 	//		//cp.hibernate();
 	//		return cp;
 	//	}
 
 	void communicate(ComplexAgent target) {
 		target.setCommInbox(commOutbox);
 	}
 
 	public void copyConstants(ComplexAgent p) {
 		setConstants(p.pdCheater, (ComplexAgentParams) defaultParams[p.getAgentType()].clone());
 	}
 
 	@Override
 	public void die() {
 		super.die();
 
 		for (SpawnMutator mutator : spawnMutators) {
 			mutator.onDeath(this);
 		}
 
 		info.setDeath(((ComplexEnvironment) position.getEnvironment()).getTickCount());
 	}
 
 	/**
 	 * This method allows the agent to see what is in front of it.
 	 * 
 	 * @return What the agent sees and at what distance.
 	 */
 	public SeeInfo distanceLook() {
 		Direction d = facing;
 		cobweb.Environment.Location destPos = getPosition().getAdjacent(d);
 		if (getPosition().checkFlip(d)) 
 			d = d.flip();
 		for (int dist = 1; dist <= LOOK_DISTANCE; ++dist) {
 
 			// We are looking at the wall
 			if (destPos == null)
 				return new SeeInfo(dist, ComplexEnvironment.FLAG_STONE);
 
 			// Check for stone...
 			if (destPos.testFlag(ComplexEnvironment.FLAG_STONE))
 				return new SeeInfo(dist, ComplexEnvironment.FLAG_STONE);
 
 			// If there's another agent there, then return that it's a stone...
 			if (destPos.getAgent() != null && destPos.getAgent() != this)
 				return new SeeInfo(dist, ComplexEnvironment.FLAG_AGENT);
 
 			// If there's food there, return the food...
 			if (destPos.testFlag(ComplexEnvironment.FLAG_FOOD))
 				return new SeeInfo(dist, ComplexEnvironment.FLAG_FOOD);
 
 			if (destPos.testFlag(ComplexEnvironment.FLAG_DROP))
 				return new SeeInfo(dist, ComplexEnvironment.FLAG_DROP);
 
 			destPos = destPos.getAdjacent(d);
 			if (getPosition().checkFlip(d)) 
 				d = d.flip();
 		}
 		return new SeeInfo(LOOK_DISTANCE, 0);
 	}
 
 	/**
 	 * The agent eats the food (food flag is set to false), and 
 	 * gains energy and waste according to the food type.
 	 * 
 	 * @param destPos Location of food.
 	 */
 	public void eat(cobweb.Environment.Location destPos) {
 		// TODO: CHECK if setting flag before determining type is ok
 		// Eat first before we can produce waste, of course.
 		destPos.setFlag(ComplexEnvironment.FLAG_FOOD, false);
 		// Gain Energy according to the food type.
 		if (environment.getFoodType(destPos) == agentType) {
 			energy += params.foodEnergy;
 			wasteCounterGain -= params.foodEnergy;
 			info.addFoodEnergy(params.foodEnergy);
 		} else {
 			energy += params.otherFoodEnergy;
 			wasteCounterGain -= params.otherFoodEnergy;
 			info.addOthers(params.otherFoodEnergy);
 		}
 	}
 
 	/**
 	 * The agent eats the adjacent agent by killing it and gaining 
 	 * energy from it.
 	 * 
 	 * @param adjacentAgent The agent being eaten.
 	 */
 	protected void eat(ComplexAgent adjacentAgent) {
 		int gain = (int) (adjacentAgent.energy * params.agentFoodEnergy);
 		energy += gain;
 		wasteCounterGain -= gain;
 		info.addCannibalism(gain);
 		adjacentAgent.die();
 	}
 
 	public double energyPenalty() {
 		if (!params.agingMode)
 			return 0.0;
 		double tempAge = currTick - birthTick;
 		assert(tempAge == age);
 		int penaltyValue = Math.min(Math.max(0, energy), (int)(params.agingRate
 				* (Math.tan(((tempAge / params.agingLimit) * 89.99) * Math.PI / 180))));
 
 		return penaltyValue;
 	}
 
 	cobweb.Agent getAdjacentAgent() {
 		cobweb.Environment.Location destPos = getPosition().getAdjacent(facing);
 		if (destPos == null) {
 			return null;
 		}
 		return destPos.getAgent();
 	}
 
 	public long getAge() {
 		return age;
 	}
 
 	@Override
 	public int getAgentPDAction() {
 		return pdCheater;
 	}
 
 	@Override
 	public int getAgentPDStrategy() {
 		return agentPDStrategy;
 	}
 
 	public int getAgentType() {
 		return params.type;
 	}
 
 	@Override
 	public Color getColor() {
 		return color;
 	}
 
 	public int getCommInbox() {
 		return commInbox;
 	}
 
 	public int getCommOutbox() {
 		return commOutbox;
 	}
 
 	// get agent's drawing information given the UI
 	@Override
 	public void getDrawInfo(DrawingHandler theUI) {
 		Color stratColor;
 
 		// is agents action is 1, it's a cheater therefore it's
 		// graphical representation will a have red boundary
 		if (pdCheater == 1) {
 			stratColor = Color.red;
 		} else {
 			// cooperator, black boundary
 			stratColor = Color.black;
 		}
 		// based on agent type
 		theUI.newAgent(getColor(), colorMap.getColor(agentType, 1), stratColor, new Point2D(getPosition().v[0],
 				getPosition().v[1]), new Point2D(facing.v[0], facing.v[1]));
 	}
 
 	/**
 	 * return Agent's energy
 	 */
 	@Override
 	public int getEnergy() {
 		return energy;
 	}
 
 	public ComplexAgentInfo getInfo() {
 		return info;
 	}
 
 	/**
 	 * North = 0
 	 * <br>East = 1
 	 * <br>South = 2
 	 * <br>West = 3
 	 * 
 	 * @return A number representation of the direction the agent is facing.
 	 */
 	public int getIntFacing() {
 		if (facing.equals(cobweb.Environment.DIRECTION_NORTH))
 			return 0;
 		if (facing.equals(cobweb.Environment.DIRECTION_EAST))
 			return 1;
 		if (facing.equals(cobweb.Environment.DIRECTION_SOUTH))
 			return 2;
 		if (facing.equals(cobweb.Environment.DIRECTION_WEST))
 			return 3;
 		return 0;
 	}
 
 	public Direction getFacing() {
 		return facing;
 	}
 
 	public int getMemoryBuffer() {
 		return memoryBuffer;
 	}
 
 	/**
 	 * Provide a random direction for the agent to face.
 	 */
 	private void InitFacing() {
 		double f = cobweb.globals.random.nextFloat();
 		if (f < 0.25)
 			facing = cobweb.Environment.DIRECTION_NORTH;
 		else if (f < 0.5)
 			facing = cobweb.Environment.DIRECTION_SOUTH;
 		else if (f < 0.75)
 			facing = cobweb.Environment.DIRECTION_EAST;
 		else
 			facing = cobweb.Environment.DIRECTION_WEST;
 	}
 
 	public boolean isAsexFlag() {
 		return asexFlag;
 	}
 
 	/**
 	 * The agent will remember the last variable number of agents that 
 	 * cheated it.  How many cheaters it remembers is determined by its 
 	 * PD memory size.
 	 * 
 	 * @param othersID In a game of PD, the opposing agents ID
 	 */
 	protected void iveBeenCheated(int othersID) {
 
 		if (params.pdMemory > 0) {
 			photo_memory[photo_num++] = othersID;
 
 			if (photo_num >= params.pdMemory) {
 				photo_num = 0;
 			}
 		}
 
 		broadcastCheating(getPosition());
 	}
 
 	public long look() {
 		cobweb.Environment.Location destPos = getPosition().getAdjacent(facing);
 		// If the position is invalid, then we're looking at a stone...
 		if (destPos == null)
 			return ComplexEnvironment.FLAG_STONE;
 		// Check for stone...
 		if (destPos.testFlag(ComplexEnvironment.FLAG_STONE))
 			return ComplexEnvironment.FLAG_STONE;
 		// If there's another agent there, then return that it's a stone...
 		if (destPos.getAgent() != null)
 			return ComplexEnvironment.FLAG_STONE;
 		// If there's food there, return the food...
 		if (destPos.testFlag(ComplexEnvironment.FLAG_FOOD))
 			return ComplexEnvironment.FLAG_FOOD;
 		// waste check
 		if (destPos.testFlag(ComplexEnvironment.FLAG_DROP))
 			return ComplexEnvironment.FLAG_DROP;
 
 		// Return an empty tile
 		return 0;
 	}
 
 	public Node makeNode(Document doc) {
 
 		Node agent = doc.createElement("Agent");
 
 		Element agentTypeElement = doc.createElement("agentType"); 
 		agentTypeElement.appendChild(doc.createTextNode(agentType +"")); 
 		agent.appendChild(agentTypeElement); 
 
 
 		Element doCheatElement = doc.createElement("doCheat"); 
 		doCheatElement.appendChild(doc.createTextNode(pdCheater +"")); 
 		agent.appendChild(doCheatElement); 
 
 		Element paramsElement = doc.createElement("params"); 
 
 		params.saveConfig(paramsElement, doc);
 
 		agent.appendChild(paramsElement);
 
 		Element directionElement = doc.createElement("direction");
 
 		facing.saveAsANode(directionElement, doc);
 
 		agent.appendChild(directionElement);
 
 		return agent;
 	}
 
 	@Override
 	public void move(Location newPos) {
 		super.move(newPos);
 		info.addPathStep(newPos);
 		if (mustFlip) {
 			if (facing.equals(Environment.DIRECTION_NORTH))
 				facing = Environment.DIRECTION_SOUTH;
 			else if (facing.equals(Environment.DIRECTION_SOUTH))
 				facing = Environment.DIRECTION_NORTH;
 			else if (facing.equals(Environment.DIRECTION_EAST))
 				facing = Environment.DIRECTION_WEST;
 			else if (facing.equals(Environment.DIRECTION_WEST))
 				facing = Environment.DIRECTION_EAST;
 		}
 	}
 
 	/**
 	 * This method initializes the agents actions in an iterated prisoner's 
 	 * dilemma game.  The agent can use the following strategies described 
 	 * by the agentPDStrategy integer:
 	 * 
 	 * <p>0. Default
 	 * 
 	 * <p>The agents decision to defect or cooperate is chosen randomly.  
 	 * The probability of choosing either is determined by the agents 
 	 * pdCoopProb parameter.
 	 * 
 	 * <p>1. Tit for Tat
 	 * 
 	 * <p>The agent will initially begin with a cooperate, but will then choose 
 	 * whatever the opposing agent chose last.  For example, the agent begins 
 	 * with a cooperate, but if the opposing agent has chosen to defect, then 
 	 * the agent will choose to defect next round.
 	 * 
 	 */
 	public void playPD() {
 
 		double coopProb = params.pdCoopProb / 100.0d; // static value for now
 
 		if (params.pdTitForTat) { // if true then agent is playing TitForTat
 			agentPDStrategy = 1; // set Strategy to 1 i.e. TitForTat
 			if (lastPDMove == -1)// if this is the first move
 				lastPDMove = 0; // start by cooperating
 			// might include probability bias within TitForTat strategy...not
 			// currently implemented
 			pdCheater = lastPDMove;
 		} else {
 			agentPDStrategy = 0; // $$$$$$ added to ensure Strategy is set to
 			// 0, i.e. probability. Apr 22
 			pdCheater = 0; // agent is assumed to cooperate
 			float rnd = cobweb.globals.random.nextFloat();
 			if (rnd > coopProb)
 				pdCheater = 1; // agent defects depending on
 			// probability
 		}
 
 		info.setStrategy(pdCheater);
 
 		return;
 	}
 
 	/**
 	 *Prisoner's dilemma is played between the two agents using the strategies 
 	 *assigned in playPD().  The agent will use its PD memory to remember agents 
 	 *that cheat it, which will affect whether an agent will want to meet another,
 	 *and its credibility.
 	 *
 	 *<p>How Prisoner's Dilemma is played:
 	 *
 	 *<p>Prisoner's dilemma is a game between two agents when they come in to 
 	 *contact with each other.  The game determines how much energy each agent 
 	 *receives after contact.  Each agent has two options: cooperate or defect.
 	 *The agents choice to cooperate or defect is determined by the strategy the 
 	 *agent is using (see playPD() method).  The agents choices can lead to 
 	 *one of four outcomes:
 	 *
 	 *<p> 1. REWARD for mutual cooperation (Both agents cooperate)
 	 *
 	 *<p> 2. SUCKER's payoff (Opposing agent defects; this agent cooperates)
 	 *
 	 *<p> 3. TEMPTATION to defect (Opposing agent cooperates; this agent defects)
 	 *
 	 *<p> 4. PUNISHMENT for mutual defection (Both agents defect)
 	 *
 	 *<p>The best strategy for both agents is to cooperate.  However, if an agent 
 	 *chooses to defect when the other cooperates, the defecting agent will have 
 	 *a greater advantage.  For a true game of PD, the energy scores for each 
 	 *outcome should follow this rule: TEMPTATION > REWARD > PUNISHMENT > SUCKER
 	 *
 	 *<p>Here is an example of how much energy an agent could receive:
 	 *<br> REWARD     =>     5
 	 *<br> SUCKER     =>     2
 	 *<br> TEMPTATION =>     8
 	 *<br> PUNISHMENT =>     3
 	 *
 	 * @param adjacentAgent Agent playing PD with
 	 * @param othersID ID of the adjacent agent.
 	 * @see ComplexAgent#playPD()
 	 * @see <a href="http://en.wikipedia.org/wiki/Prisoner's_dilemma">Prisoner's Dilemma</a>
 	 */
 	public void playPDonStep(ComplexAgent adjacentAgent, int othersID) {
 		playPD();
 		adjacentAgent.playPD();
 
 		lastPDMove = adjacentAgent.pdCheater; // Adjacent Agent's action is assigned to the last move memory of the
 		// agent
 		adjacentAgent.lastPDMove = pdCheater; // Agent's action is assigned to the last move memory of the adjacent
 		// agent
 
 		/*
 		 * TODO LOW: The ability for the PD game to contend for the Get the food tiles immediately around each agents
 		 */
 
 		/* 0 = cooperate. 1 = defect */
 
 		final int PD_COOPERATE = 0;
 		final int PD_DEFECT = 1;
 
 		if (pdCheater == PD_COOPERATE && adjacentAgent.pdCheater == PD_COOPERATE) {
 			/* REWARD */
 			energy += environment.PD_PAYOFF_REWARD;
 			adjacentAgent.energy += environment.PD_PAYOFF_REWARD;
 
 		} else if (pdCheater == PD_COOPERATE && adjacentAgent.pdCheater == PD_DEFECT) {
 			/* SUCKER */
 			energy += environment.PD_PAYOFF_SUCKER;
 			adjacentAgent.energy += environment.PD_PAYOFF_TEMPTATION;
 
 			iveBeenCheated(othersID);
 
 		} else if (pdCheater == PD_DEFECT && adjacentAgent.pdCheater == PD_COOPERATE) {
 			/* TEMPTATION */
 			energy += environment.PD_PAYOFF_TEMPTATION;
 			adjacentAgent.energy += environment.PD_PAYOFF_SUCKER;
 
 		} else if (pdCheater == PD_DEFECT && adjacentAgent.pdCheater == PD_DEFECT) {
 			/* PUNISHMENT */
 			energy += environment.PD_PAYOFF_PUNISHMENT;
 			adjacentAgent.energy += environment.PD_PAYOFF_PUNISHMENT; // $$$$$$
 
 			iveBeenCheated(othersID);
 		}
 
 	}
 
 	void receiveBroadcast() {
 		BroadcastPacket commPacket = null;
 
 		commPacket = checkforBroadcasts();
 		if (commPacket == null)
 			return;
 
 		// check if dispatcherId is in list
 		// TODO what does this do?
 		checkCredibility(commPacket.getDispatcherId());
 
 		int type = commPacket.getType();
 		switch (type) {
 			case BroadcastPacket.FOOD:
 				receiveFoodBroadcast(commPacket);
 				break;
 			case BroadcastPacket.CHEATER:
 				receiveCheatingBroadcast(commPacket);
 				break;
 			default:
 				Logger myLogger = Logger.getLogger("COBWEB2");
 				myLogger.log(Level.WARNING, "Unrecognised broadcast type");
 		}
 	}
 
 	void receiveCheatingBroadcast(BroadcastPacket commPacket) {
 		String message = commPacket.getContent();
 		long cheaterId = 0;
 		cheaterId = Long.parseLong(message);
 		photo_memory[photo_num] = cheaterId;
 	}
 
 	void receiveFoodBroadcast(BroadcastPacket commPacket) {
 		String message = commPacket.getContent();
 		String[] xy = message.substring(1, message.length() - 1).split(",");
 		int x = Integer.parseInt(xy[0]);
 		int y = Integer.parseInt(xy[1]);
 		thinkAboutFoodLocation(x, y);
 
 	}
 
 	public void setAsexFlag(boolean asexFlag) {
 		this.asexFlag = asexFlag;
 	}
 
 
 	@Override
 	public void setColor(Color c) {
 		color = c;
 	}
 
 	public void setCommInbox(int commInbox) {
 		this.commInbox = commInbox;
 	}
 
 	public void setCommOutbox(int commOutbox) {
 		this.commOutbox = commOutbox;
 	}
 
 	/**
 	 * Sets the complex agents parameters.
 	 * 
 	 * @param pdCheat
 	 * @param agentData The ComplexAgentParams used for this complex agent.
 	 */
 	public void setConstants(int pdCheat, ComplexAgentParams agentData) {
 
 		this.params = agentData;
 
 		this.agentType = agentData.type;
 
 		this.pdCheater = pdCheat;
 		energy = agentData.initEnergy;
 		wasteCounterGain = params.wasteLimitGain;
 		setWasteCounterLoss(params.wasteLimitLoss);
 
 		photo_memory = new long[params.pdMemory];
 
 		// $$$$$$ Modified the following block. we are not talking about RESET
 		// here. Apr 18
 		/*
 		 * agentPDStrategy = 1; // Tit-for-tat or probability based
 		 * agentPDAction = -1; // The agent's action; 1 = cheater, else
 		 * cooperator $$$$$ this parameter seems useless, there is another
 		 * PDaction parameter above already. Apr 18 lastPDMove = -1; // Remember
 		 * the opponent's move in the last game
 		 */
 		this.agentPDStrategy = 0; // FIXME agentData.agentPDStrategy;
 		// above all, sometimes a new
 		// agent need copy PDStrategy
 		// from its parent. See
 		// copyConstants( ComplexAgent p
 		// )
 		this.lastPDMove = 0; // FIXME agentData.lastPDMove;
 		// "KeepOldAgents" need pass this
 		// parameter. (as a reasonable side
 		// effect, the parameter of a parent
 		// would also pass to its child)
 		// See ComplexEnvironment.load(cobweb.Scheduler s, Parser p/*
 		// java.io.Reader r */) @ if (keepOldAgents[0]) {...
 
 	}
 
 	public void setMemoryBuffer(int memoryBuffer) {
 		this.memoryBuffer = memoryBuffer;
 	}
 
 	/*
 	 * return the measure of similarity between this agent and the 'other' ranging from 0.0 to 1.0 (identical)
 	 */
 	@Override
 	public double similarity(cobweb.Agent other) {
 		if (!(other instanceof ComplexAgent))
 			return 0.0;
 		return // ((GeneticController) controller)
 		// .similarity((GeneticController) ((ComplexAgent) other)
 		// .getController());
 		((LinearWeightsController) controller).similarity((LinearWeightsController) other.getController());
 	}
 
 	@Override
 	public double similarity(int other) {
 		return 0.5; // ((GeneticController) controller).similarity(other);
 	}
 
 	/**
 	 * During a step, the agent can encounter four different circumstances: 
 	 * 1. Nothing is in its way.  
 	 * 2. Contact with another agent.  
 	 * 3. Run into waste.
 	 * 4. Run into a rock.
 	 * 
 	 * <p> 1. Nothing in its way: 
 	 * 
 	 * <p>If the agent can move into the next position, the first thing it will do 
 	 * is check for food.  If it finds food, then the agent may 
 	 * broadcast a message containing the location of the food.  The agent may 
 	 * then eat the food.  If after eating the food the agent was pregnant, a check 
 	 * will be made to see if the child can be produced now.  If the agent was not 
 	 * pregnant, then a-sexual breeding will be attempted.
 	 * 
 	 * <p>This method will then iterate through all  mutators used in the simulation 
 	 * and call onStep for each step mutator.  The agent will then move.  If it 
 	 * was found that the agent was ready to produce a child, then a new agent 
 	 * is created.
 	 * 
 	 * <p> 2. Contact with another agent:
 	 * 
 	 * <p> Contact mutators are iterated through and the onContact method is called 
 	 * for each used within the simulation.  The agent will eat the agent if it can.  
 	 * 
 	 * <p> If prisoner's dilemma is being used for this simulation, then a check is 
 	 * made to see if both agents want to meet each other (True if no bad memories of 
 	 * adjacent agent).  If the adjacent agent was not eaten and both agents want to 
 	 * meet each other, then the possibility of breeding will be looked in to.  If 
 	 * breeding is not possible, then prisoner's dilemma will be played.  If prisoner's 
 	 * dilemma is not used, then only breeding is checked for.
 	 * 
 	 * <p> An energy penalty is deducted for bumping into another agent.
 	 * 
 	 * <p> 3 and 4. Run into waste/rock:
 	 * 
 	 * <p> Energy penalties are deducted from the agent.
 	 * 
 	 * @see ComplexAgent#playPDonStep(ComplexAgent, int)
 	 */
 	public void step() {
 		cobweb.Agent adjAgent;
 		mustFlip = getPosition().checkFlip(facing);
 		cobweb.Environment.Location destPos = getPosition().getAdjacent(facing);
 
 		if (canStep(destPos)) {
 
 			onstepFreeTile(destPos);
 
 		} else if ((adjAgent = getAdjacentAgent()) != null && adjAgent instanceof ComplexAgent
 				&& ((ComplexAgent) adjAgent).info != null) {
 			// two agents meet
 
 			ComplexAgent adjacentAgent = (ComplexAgent) adjAgent;
 
 
 			onstepAgentBump(adjacentAgent);
 
 		} // end of two agents meet
 		else {
 			// Non-free tile (rock/waste/etc) bump
 			energy -= params.stepRockEnergy;
 			wasteCounterLoss -= params.stepRockEnergy;
 			info.useRockBumpEnergy(params.stepRockEnergy);
 			info.addRockBump();
 		}
 		energy -= energyPenalty();
 
 		if (energy <= 0)
 			die();
 
 		if (energy < params.breedEnergy) {
 			pregnant = false;
 			breedPartner = null;
 		}
 
 		if (pregnant) {
 			pregPeriod--;
 		}
 	}
 
 	protected void onstepFreeTile(cobweb.Environment.Location destPos) {
 		// Check for food...
 		cobweb.Environment.Location breedPos = null;
 		if (destPos.testFlag(ComplexEnvironment.FLAG_FOOD)) {
 			if (params.broadcastMode & canBroadcast()) {
 				broadcastFood(destPos);
 			}
 			if (canEat(destPos)) {
 				eat(destPos);
 			}
 			if (pregnant && energy >= params.breedEnergy && pregPeriod <= 0) {
 
 				breedPos = getPosition();
 				energy -= params.initEnergy;
 				energy -= energyPenalty();
 				wasteCounterLoss -= params.initEnergy;
 				info.useOthers(params.initEnergy);
 
 			} else {
 				if (!pregnant)
 					tryAsexBreed();
 			}
 		}
 
 		for (StepMutator m : stepMutators)
			m.onStep(this, destPos, getPosition());
 
 		move(destPos);
 
 		if (breedPos != null) {
 
 			if (breedPartner == null) {
 				info.addDirectChild();
 				ComplexAgent child = (ComplexAgent)AgentSpawner.spawn();
 				child.init(breedPos, this, this.pdCheater);
 			} else {
 				// child's strategy is determined by its parents, it has a
 				// 50% chance to get either parent's strategy
 				int childStrategy = -1;
 				if (this.pdCheater != -1) {
 					boolean choose = cobweb.globals.random.nextBoolean();
 					if (choose) {
 						childStrategy = this.pdCheater;
 					} else {
 						childStrategy = breedPartner.pdCheater;
 					}
 				}
 
 				info.addDirectChild();
 				breedPartner.info.addDirectChild();
 				ComplexAgent child = (ComplexAgent)AgentSpawner.spawn();
 				child.init(breedPos, this, breedPartner, childStrategy);
 				info.addSexPreg();
 			}
 			breedPartner = null;
 			pregnant = false;
 		}
 		energy -= params.stepEnergy;
 		wasteCounterLoss -= params.stepEnergy;
 		info.useStepEnergy(params.stepEnergy);
 		info.addStep();
 	}
 
 	protected void onstepAgentBump(ComplexAgent adjacentAgent) {
 		for (ContactMutator mut : contactMutators) {
 			mut.onContact(this, adjacentAgent);
 		}
 
 		if (canEat(adjacentAgent)) {
 			eat(adjacentAgent);
 		}
 
 		if (this.pdCheater != -1) {// $$$$$ if playing Prisoner's
 			// Dilemma. Please refer to ComplexEnvironment.load, "// spawn new random agents for each type"
 			want2meet = true;
 		}
 
 		int othersID = adjacentAgent.info.getAgentNumber();
 		// scan the memory array, is the 'other' agents ID is found in the array,
 		// then choose not to have a transaction with him.
 		for (int i = 0; i < params.pdMemory; i++) {
 			if (photo_memory[i] == othersID) {
 				want2meet = false;
 			}
 		}
 		// if the agents are of the same type, check if they have enough
 		// resources to breed
 		if (adjacentAgent.agentType == agentType) {
 
 			double sim = 0.0;
 			boolean canBreed = !pregnant && energy >= params.breedEnergy && params.sexualBreedChance != 0.0
 			&& cobweb.globals.random.nextFloat() < params.sexualBreedChance;
 
 			// Generate genetic similarity number
 			sim = simCalc.similarity(this, adjacentAgent);
 
 			if (sim >= params.commSimMin) {
 				communicate(adjacentAgent);
 			}
 
 			if (canBreed && sim >= params.breedSimMin
 					&& ((want2meet && adjacentAgent.want2meet) || (pdCheater == -1))) {
 				pregnant = true;
 				pregPeriod = params.sexualPregnancyPeriod;
 				breedPartner = adjacentAgent;
 			}
 		}
 		// perform the transaction only if non-pregnant and both agents want to meet
 		if (!pregnant && want2meet && adjacentAgent.want2meet) {
 
 			playPDonStep(adjacentAgent, othersID);
 		}
 		energy -= params.stepAgentEnergy;
 		setWasteCounterLoss(getWasteCounterLoss() - params.stepAgentEnergy);
 		info.useAgentBumpEnergy(params.stepAgentEnergy);
 		info.addAgentBump();
 	}
 
 	private void thinkAboutFoodLocation(int x, int y) {
 		Location target = this.getPosition().getEnvironment().getLocation(x, y);
 
 		double closeness = 1;
 
 		if (!target.equals(getPosition()))
 			closeness = 1 / target.distance(this.getPosition());
 
 		int o =(int)Math.round(closeness * ((1 << this.params.communicationBits) - 1));
 
 		setCommInbox(o);
 	}
 
 	/**
 	 * Controls what happens to the agent on this tick.  If the 
 	 * agent is still alive, what happens to the agent is determined 
 	 * by the controller.
 	 * 
 	 * @param tick The time in the simulation
 	 * @see cobweb.Controller#controlAgent(cobweb.Agent)
 	 */
 	public void tickNotification(long tick) {
 		if (!isAlive())
 			return;
 
 		/* The current tick */
 		currTick = tick;
 
 		/* Hack to find the birth tick... */
 		if (birthTick == 0)
 			birthTick = currTick;
 
 		age++;
 
 		/* Time to die, Agent (mister) Bond */
 		if (params.agingMode) {
 			if ((currTick - birthTick) >= params.agingLimit) {
 				die();
 				return;
 			}
 		}
 
 		beforeController();
 
 		/* Move/eat/reproduce/etc */
 		controller.controlAgent(this);
 
 		afterController();
 
 		/* Produce waste if able */
 		if (params.wasteMode && shouldPoop())
 			tryPoop();
 
 		/* Check if broadcasting is enabled */
 		if (params.broadcastMode)
 			receiveBroadcast();
 	}
 
 	protected void beforeController() {
 
 	}
 
 	protected void afterController() {
 
 	}
 
 	public void tickZero() {
 		// nothing
 	}
 
 	/**
 	 * If the agent has enough energy to breed, is randomly chosen to breed, 
 	 * and its asexFlag is true, then the agent will be pregnant and set to 
 	 * produce a child agent after the agent's asexPregnancyPeriod is up.
 	 */
 	void tryAsexBreed() {
 		if (asexFlag && energy >= params.breedEnergy && params.asexualBreedChance != 0.0
 				&& cobweb.globals.random.nextFloat() < params.asexualBreedChance) {
 			pregPeriod = params.asexPregnancyPeriod;
 			pregnant = true;
 		}
 	}
 
 	private boolean shouldPoop() {
 		if (wasteCounterGain <= 0 && params.wasteLimitGain > 0) {
 			wasteCounterGain += params.wasteLimitGain;
 			return true;
 		} else if (getWasteCounterLoss() <= 0 && params.wasteLimitLoss > 0) {
 			setWasteCounterLoss(getWasteCounterLoss() + params.wasteLimitLoss);
 			return true;
 		}
 		return false;
 	}
 
 
 	/**
 	 * Produce waste
 	 */
 	private void tryPoop() {
 		forceDrop(new Waste(currTick, params.wasteInit, params.wasteDecay));
 	}	
 
 	private void forceDrop(Drop d) {
 		boolean added = false;
 
 		// For this method, "adjacent" implies tiles around the agent including
 		// tiles that are diagonally adjacent
 
 		cobweb.Environment.Location loc;
 
 		// Place the drop at an available location adjacent to the agent
 		for (int i = 0; i < dirList.length; i++) {
 			loc = getPosition().getAdjacent(dirList[i]);
 			if (loc != null && loc.getAgent() == null && !loc.testFlag(ComplexEnvironment.FLAG_STONE)
 					&& !loc.testFlag(ComplexEnvironment.FLAG_DROP) && !loc.testFlag(ComplexEnvironment.FLAG_FOOD)) {
 				loc.setFlag(ComplexEnvironment.FLAG_FOOD, false);
 				loc.setFlag(ComplexEnvironment.FLAG_STONE, false);
 				loc.setFlag(ComplexEnvironment.FLAG_DROP, true);
 				environment.setDrop(loc, d);
 				break;
 			}
 		}
 
 		/*
 		 * Crowded! IF there is no empty tile in which to drop the waste, we can replace a food tile with a waste
 		 * tile... / This function is assumed to add a waste tile! That is, this function assumes an existence of at
 		 * least one food tile that it will be able to replace with a waste tile. Nothing happens otherwise.
 		 */
 		if (!added) {
 			for (int i = 0; i < dirList.length; i++) {
 				loc = getPosition().getAdjacent(dirList[i]);
 				if (loc != null && loc.getAgent() == null && loc.testFlag(ComplexEnvironment.FLAG_FOOD)) {
 					loc.setFlag(ComplexEnvironment.FLAG_FOOD, false);
 					loc.setFlag(ComplexEnvironment.FLAG_DROP, true);
 					environment.setDrop(loc, d);
 					break;
 				}
 			}
 		}
 	}
 
 	/**
 	 * This method makes the agent turn left.  It does this by updating 
 	 * the direction of the agent and subtracts the amount of 
 	 * energy it took to turn.
 	 */
 	public void turnLeft() {
 		cobweb.Direction newFacing = new cobweb.Direction(2);
 		newFacing.v[0] = facing.v[1];
 		newFacing.v[1] = -facing.v[0];
 		facing = newFacing;
 		energy -= params.turnLeftEnergy;
 		setWasteCounterLoss(getWasteCounterLoss() - params.turnLeftEnergy);
 		info.useTurning(params.turnLeftEnergy);
 		info.addTurn();
 		afterTurnAction();
 	}
 
 	/**
 	 * This method makes the agent turn right.  It does this by updating 
 	 * the direction of the agent subtracts the amount of energy it took 
 	 * to turn.
 	 */
 	public void turnRight() {
 		cobweb.Direction newFacing = new cobweb.Direction(2);
 		newFacing.v[0] = -facing.v[1];
 		newFacing.v[1] = facing.v[0];
 		facing = newFacing;
 		energy -= params.turnRightEnergy;
 		setWasteCounterLoss(getWasteCounterLoss() - params.turnRightEnergy);
 		info.useTurning(params.turnRightEnergy);
 		info.addTurn();
 		afterTurnAction();
 	}
 
 
 	@Override
 	public int type() {
 		return agentType;
 	}
 
 	public void setWasteCounterLoss(int wasteCounterLoss) {
 		this.wasteCounterLoss = wasteCounterLoss;
 	}
 
 
 	public int getWasteCounterLoss() {
 		return wasteCounterLoss;
 	}
 
 }
