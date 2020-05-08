 package faction;
 
 import java.util.*;
 
 import settings.Globals;
 import faction.Reputation.ReputationLevel;
 
 import planning.*;
 import ui.GameFrame;
 import ui.prompt.*;
 import ui.prompt.PromptTreeWorldParameter.WorldFilter;
 import universe.*;
 
 public abstract class Faction 
 {
 	//
 	// DATA
 	//
 	// Faction Data
 	public String factionName;
 	
 	protected World homeWorld;
 	protected Set<World> controlledWorlds = new HashSet<World>();
 	public Set<World> knownWorlds = new HashSet<World>();
 	
 	protected int numResources;
 	public double morale = 0.25;
 	
 	protected Set<World> knownGateAddresses = new HashSet<World>();
 	public Set<World> getKnownGateAddresses() {
 		return knownGateAddresses;
 	}
 
 	public Set<World> getKnownWorldLocations() {
 		return knownWorldLocations;
 	}
 
 	protected Set<World> knownWorldLocations = new HashSet<World>();
 	
 	protected Map<Faction, Reputation> factionReputations = new HashMap<Faction, Reputation>();
 	
 	protected TechLevel tech = new TechLevel();
 	
 	// Personality
 	protected int aggression;
 	public int getAggression() {
 		return aggression;
 	}
 
 	public int getDiplomacy() {
 		return diplomacy;
 	}
 
 	public int getScience() {
 		return science;
 	}
 
 	protected int diplomacy;
 	protected int science;
 	
 	// Planning Data
 	public boolean isPlayerControlled = false;
 	public int timeToReplan = 0;
 	public Stack<Task> plan = new Stack<Task>();
 	public Task mNextTask;
 	
 	//
 	// TROOP AND SHIP MANAGEMENT
 	//
 	public void increaseMorale() {
 		morale += .05;
 		if(morale > Globals.MAX_MORALE) morale = Globals.MAX_MORALE;
 		if(this == Universe.playerFaction || homeWorld.hasSpy(Universe.playerFaction))
 			if(morale == Globals.MAX_MORALE)
 				GameFrame.addToLog(factionName + " is feeling good!  Morale is at maximum!");
 			else
 				GameFrame.addToLog(factionName + " is feeling good!  Morale has gone up.");
 		
 	}
 	
 	public void decreaseMorale() {
 		morale -= morale <= 0 ? .05 : 0;
 		if(this == Universe.playerFaction || homeWorld.hasSpy(Universe.playerFaction))
 			if(morale == Globals.MIN_MORALE)
 				GameFrame.addToLog(factionName + " is feeling lousy!  Morale is at minimum!");
 			else
 				GameFrame.addToLog(factionName + " is feeling lousy.  Morale has gone down.");
 	}
 	
 	public void increaseTroops(int amount)
 	{
 		increaseTroops(amount, homeWorld);
 	}
 	
 	public void increaseTroops(int amount, World w)
 	{
 		w.addTroops(this, amount);
 	}
 	
 	public void gainTroopsPassive()
 	{
 		for(World w : controlledWorlds)
 		{
 			gainTroopsPassive(w);
 		}
 	}
 	
 	public void gainTroopsPassive(World w)
 	{
 		increaseTroops((int)(w.getPassiveTroops()), w);
 	}
 	
 	public void gainTroopsActive(World w)
 	{
		int numTroops = Math.min((int)(w.getPassiveTroops() * morale) * Globals.TROOP_RESOURCE_BUILD_COST, numResources);
 		increaseTroops(numTroops, w);
 		removeResources(numTroops);
 	}
 	
 	public void decreaseTroops(int amount)
 	{
 		decreaseTroops(amount, homeWorld);
 	}
 	
 	public void decreaseTroops(int amount, World w)
 	{
 		w.removeTroops(this, amount);
 	}
 	
 	public int getNumArmies() 
 	{
 		int armies = 0;
 		
 		for(World w : knownWorlds)
 		{
 			armies += getNumArmies(w);
 		}
 		
 		return armies;
 	}
 	
 	public int getNumArmies(World w)
 	{
 		return w.getTroopCount(this);
 	}
 	
 	public void increaseShips(int amount)
 	{
 		increaseShips(amount, homeWorld);
 	}
 	
 	public void increaseShips(int amount, World w)
 	{
 		w.addShips(this, amount);
 	}
 	
 	public void decreaseShips(int amount)
 	{
 		decreaseShips(amount, homeWorld);
 	}
 	
 	public void decreaseShips(int amount, World w)
 	{
 		w.removeShips(this, amount);
 	}
 	
 	public int getNumShips() 
 	{
 		int armies = 0;
 		
 		for(World w : knownWorlds)
 		{
 			armies += getNumShips(w);
 		}
 		
 		return armies;
 	}
 	
 	public int getNumShips(World w)
 	{
 		return w.getShipCount(this);
 	}
 	
 	//
 	// RESOURCES
 	//
 	private void gainResourcesPassive()
 	{
 		for(World w : controlledWorlds)
 		{
 			addResources((int)(tech.resourceEfficiency * w.getPassiveResources()));
 		}
 	}
 	
 	public void gainResourcesActive()
 	{
 		for(World w : controlledWorlds)
 		{
 			addResources((int)(tech.resourceEfficiency * w.getActiveResources()));
 		}
 	}
 	
 	public void addResources(int amount)
 	{
 		this.numResources += amount;
 	}
 	
 	public void removeResources(int amount)
 	{
 		this.numResources -= amount;
 	}
 	
 	public int getNumResources() 
 	{
 		return numResources;
 	}
 	
 	//
 	// REPUTATIONS
 	//
 	public boolean isEnemy(Faction f)
 	{
 		if(!factionReputations.containsKey(f))
 			return false;
 		
 		Reputation r = factionReputations.get(f);
 		return (r.compareReputation(ReputationLevel.ENEMY) <= 0);
 	}
 	
 	public List<Faction> getEnemies()
 	{
 		List<Faction> enemies = new ArrayList<Faction>();
 		
 		for(Faction f : factionReputations.keySet())
 		{
 			if(isEnemy(f))
 				enemies.add(f);
 		}
 		
 		return enemies;
 	}
 	
 	private void initReputation(Faction f)
 	{
 		if(!factionReputations.containsKey(f))
 		{
 			Reputation r = new Reputation();			
 			factionReputations.put(f, r);
 			f.factionReputations.put(this, r);
 		}
 	}
 	
 	public double getReputationNumber(Faction f)
 	{		
 		initReputation(f);
 		
 		return factionReputations.get(f).currentRep;
 	}
 	
 	public ReputationLevel getReputation(Faction f)
 	{
 		initReputation(f);
 		
 		return factionReputations.get(f).reputationLevel;
 	}
 	
 	public void increaseReputation(Faction f, double amount)
 	{
 		initReputation(f);
 		
 		factionReputations.get(f).adjustReputation(amount);
 		if(this == Universe.playerFaction || homeWorld.hasSpy(Universe.playerFaction))
 			GameFrame.addToLog(factionName + " is feeling better about the " + f.factionName + ".  Reputation has gone up.");
 	}
 	
 	public void decreaseReputation(Faction f, double repChange)
 	{
 		initReputation(f);
 		
 		factionReputations.get(f).adjustReputation(-1 * repChange);
 		if(this == Universe.playerFaction || homeWorld.hasSpy(Universe.playerFaction))
 			GameFrame.addToLog(factionName + " is feeling worse about the " + f.factionName + ".  Reputation has gone down.");
 	}
 	
 	//
 	// KNOWLEDGE
 	//
 	public boolean knowsGateAddress(World world) 
 	{
 		return knownGateAddresses.contains(world);
 	}
 	
 	public void learnGateAddress(World world)
 	{
 		if(world.hasGate) {
 			knownGateAddresses.add(world);
 			knownWorlds.add(world);
 		}
 		
 		if(this == Universe.playerFaction || world.hasSpy(Universe.playerFaction))
 			GameFrame.addToLog(factionName + " has learned the gate address of " + world.name);
 	}
 	
 	public boolean knowsLocation(World world) 
 	{
 		return knownWorldLocations.contains(world);
 	}
 	
 	public void learnWorldLocation(World world) 
 	{
 		knownWorldLocations.add(world);
 		knownWorlds.add(world);
 		if(this == Universe.playerFaction || world.hasSpy(Universe.playerFaction))
 			GameFrame.addToLog(factionName + " has learned the location of " + world.name);
 	}
 	
 	//
 	// WORLDS
 	//
 	public void setHomeWorld(World w)
 	{
 		gainWorldControl(w);
 		homeWorld = w;
 	}
 	
 	public World getHomeWorld()
 	{
 		return homeWorld;
 	}
 	
 	public Set<World> getControlledWorlds()
 	{
 		return controlledWorlds;
 	}
 	
 	public Set<World> getKnownWorlds() {
 		return knownWorlds;
 	}
 	
 	public void gainWorldControl(World w)
 	{
 		if(!controlledWorlds.contains(w))
 		{
 			w.setControllingFaction(this);
 			controlledWorlds.add(w);
 			learnGateAddress(w);
 			learnWorldLocation(w);
 			
 			if(homeWorld == null)
 				homeWorld = w;
 		}
 	}
 	
 	public void loseWorldControl(World w)
 	{		
 		controlledWorlds.remove(w);
 		
 		if(homeWorld.equals(w))
 		{
 			homeWorld = null;
 			
 			if(controlledWorlds.size() > 0)
 			{
 				for(World wor : controlledWorlds)
 				{
 					if(homeWorld == null || getDefenseStrength(wor) > getDefenseStrength(homeWorld))
 						homeWorld = wor;
 				}
 			}
 		}
 		
 		if(this == Universe.playerFaction || w.hasSpy(Universe.playerFaction))
 			GameFrame.addToLog(factionName + " has lost control of " + w.name);
 	}
 	
 	//Does THIS have intel on FACTION
 	public boolean hasIntel(Faction faction) {
 		 return (faction.getHomeWorld().hasSpy(this));
 	}
 	
 	//
 	// TURN SIMULATION
 	//
 	public void setNextAction(Task next)
 	{
 		mNextTask = next;
 	}
 	
 	public void doTurn() 
 	{
 		// Upkeep
 		gainResourcesPassive();
 		gainTroopsPassive();
 		
 		// Planning
 		if(isPlayerControlled)
 		{
 			mNextTask.perform(this);
 		}
 		else
 		{
 			getNextPlannedTask().perform(this);	
 		}
 	}
 	
 	// TODO: IMPLEMENT FOR PLAYER CONTROLLED
 	//Should only be called by playerControlled factions
 	public PromptTree getAvailableActions()
 	{
 		/*
 		 * Tasks able to perform:
 		 * 
 		 * GENERAL TASKS:
 		 * Raise Morale
 		 * Gather Resources
 		 * Training
 		 * Troop Movement
 		 * Sabotage
 		 * Research
 		 * Wait
 		 * 
 		 * Training:
 		 * Train Troops - controlled world
 		 * Build Ship - controlled world
 		 * Buy Ships - controlled world, number 
 		 * 
 		 * Movement:
 		 * FlyTroopsTo - world with troops, world known loc
 		 * TransferShipsTo - world with troops, world known loc
 		 * TransportTroopsByGate - world with troops, world known gate
 		 * 
 		 * Sabotage:
 		 * PlantSpy(by gate, ship, from planet) - non-controlled
 		 * Sabotage Tasks - non-controlled
 		 * 
 		 * Research:
 		 * Undirected
 		 * Directed Resource
 		 * Directed Offense
 		 * Directed Defense
 		 * 
 		 * */
 		final Faction player = this;
 		PromptTree ret = new PromptTree("Player Action", "What would you like to do:");
 		
 		// Basic Tasks
 		ret.addChildPrompt(new PromptTreeLeaf(new RaiseMoraleTask(null)), this);
 		ret.addChildPrompt(new PromptTreeLeaf(new GatherResourcesTask(1, null)), this);
 		
 		// Research
 		PromptTree research = new PromptTree("Research", "Please select the type of research:");
 		research.addChildPrompt(new PromptTreeLeaf(new SearchForTechnologyTask(null), "Undirected Research", ""), this);
 		research.addChildPrompt(new PromptTreeLeaf(new DirectedResearchTask(Globals.RESOURCE_RESEARCH, null), "Resource Efficiency Research", ""), this);
 		research.addChildPrompt(new PromptTreeLeaf(new DirectedResearchTask(Globals.OFFENSE_RESEARCH, null), "Offensive Technology Research", ""), this);
 		research.addChildPrompt(new PromptTreeLeaf(new DirectedResearchTask(Globals.DEFENSE_RESEARCH, null), "Defensive Technology Research", ""), this);
 		ret.addChildPrompt(research, new PromptFilter()
 		{
 			@Override
 			public boolean allowPrompt(PromptTree pt) 
 			{
 				return pt.hasAllowableChildren();
 			}
 		});
 		
 		// Training / Construction
 		final PromptTreeWorldParameter training = new PromptTreeWorldParameter("Training / Shipbuilding", "Where would you like to increase numbers:", this, WorldFilter.CONTROLLED_WORLD);
 		PromptTree trainingSub = new PromptTree("", "What would you like to do:");
 		trainingSub.addChildPrompt(new PromptTreeLeaf("Train Troops", "", new TaskParameterizer()
 		{
 			@Override
 			public Task generateTask() 
 			{
 				return new TrainTroopsTask((World)training.getValue(), Globals.WORLD_TROOP_POPULATION_CAP, null);
 			}
 		}), this);
 		trainingSub.addChildPrompt(new PromptTreeLeaf("Build Ships", "", new TaskParameterizer()
 		{
 			@Override
 			public Task generateTask() 
 			{
 				return new BuildShipTask((World)training.getValue(), Globals.WORLD_SHIP_POPULATION_CAP, null);
 			}
 		}), this);
 		final PromptTreeNumericParameter shipCount = new PromptTreeNumericParameter("Buy Ships", "How many ships would you like to buy:", player);
 		shipCount.addChildPrompt(new PromptTreeLeaf("", "", new TaskParameterizer()
 		{
 			@Override
 			public Task generateTask() 
 			{
 				return new BuyShipTask(aggression, null);
 			}
 		}), this);
 		trainingSub.addChildPrompt(shipCount, new PromptFilter()
 		{
 			@Override
 			public boolean allowPrompt(PromptTree pt) 
 			{
 				World w = (World)training.getValue();
 				return (w.getShipCount(player) >= Globals.WORLD_SHIP_POPULATION_CAP);
 			}
 		});
 		training.addChildPrompt(trainingSub);
 		ret.addChildPrompt(training, new PromptFilter()
 		{
 			@Override
 			public boolean allowPrompt(PromptTree pt) 
 			{
 				boolean hasWorlds = player.getControlledWorlds().size() > 0;
 				boolean worldsWithSpace = true;
 				
 				return hasWorlds && worldsWithSpace;
 			}
 		});
 		
 		// Movement
 		
 		
 		// Wait
 		ret.addChildPrompt(new PromptTreeLeaf(new WaitTask(null)), this);
 		
 		return ret;
 	}
 	
 	//
 	// PLANNING STUFFS
 	//
 	public Task getSuperTask() {
 		
 		ArrayList<Task> taskList = getTaskList();
 		double totalFlavor = 0;		
 		
 		for(Task task : taskList)
 		{
 			if(!task.canPerform(this))
 				continue;
 			totalFlavor += task.getFlavorMatch(this);
 		}
 		if(totalFlavor <= 0)
 			return new WaitTask(null);
 		double flavorPick = new Random().nextDouble() * totalFlavor;
 		for(Task task : taskList)
 		{
 			if(!task.canPerform(this))
 				continue;
 			flavorPick -= task.getFlavorMatch(this);
 			
 			if(flavorPick <= 0)
 				return task;
 		}
 		
 		return new WaitTask(null);
 		
 	};
 	
 	protected ArrayList<Task> getTaskList() {
 		ArrayList<Task> taskList = new ArrayList<Task>();
 		for(Faction enemy : getEnemies()) {
 			for(World to : enemy.getControlledWorlds()) {
 				for(World from : getControlledWorlds()) {
 					Task attack = new AttackTask(from, to, enemy, 
 							new Random().nextInt(100) + 50, null);
 					if(attack.canPerform(this))
 						taskList.add(attack);
 				}
 			}
 		}
 		
 		for(Faction faction : Universe.factions) {
 			if(faction != this) {
 				for(World world : faction.getControlledWorlds()) {
 					Task sabotage = new SabotageTask(world, null);
 					if(sabotage.canPerform(this))
 						taskList.add(sabotage);
 				}
 			}
 		}
 		Task defend = new DefendTask(getHomeWorld(), null);
 		
 		Task research = new ResearchTask(null);
 		if(defend.canPerform(this))
 			taskList.add(defend);
 		
 		if(research.canPerform(this))
 			taskList.add(research);
 		
 		return taskList;
 	}
 
 	public void replan() {
 		if(timeToReplan <= 0 || plan.isEmpty()) { 
 			plan.clear(); 
 			timeToReplan = 20;
 			plan.add(getSuperTask());	
 		}
 		while(plan.peek().isBaseTask() != true) {
 			if(plan.peek().isCompleted(this)){ 
 				plan.pop();
 				if(plan.isEmpty()) replan();
 			}
 			else plan.add(plan.peek().getNextStep(this));
 		}
 	}
 	
 	public boolean isDefeated()
 	{
 		return (homeWorld == null);
 	}
 	
 	public abstract boolean didWin();
 	
 	public boolean needToReplan() {
 		//Replan if your plan is empty, you need to replan, or you don't have a base task to perform
 		return plan.isEmpty() || timeToReplan <= 0 || plan.peek().isBaseTask() != true; 
 	}
 	
 	public Task getNextPlannedTask() {
 		if(needToReplan())
 			replan();
 		timeToReplan--;
 		System.out.println(this + " " + plan);
 		return plan.pop();
 	}
 	
 	public double getAttackStrength(World world) {
 		return tech.offensiveCapabilities*(getNumArmies(world) + Globals.SHIP_TROOP_POWER_RATIO*getNumShips(world));
 	}
 	
 	public double getDefenseStrength(World world) {
 		return tech.defensiveCapabilities*(getNumArmies(world) + Globals.SHIP_TROOP_POWER_RATIO*getNumShips(world));
 	}
 	
 	public String toString() {
 		return factionName + " Resources: " + getNumResources();
 	}
 	
 	//
 	// Inner Class
 	//
 	public class TechLevel
 	{
 		//
 		// RESOURCES
 		//
 		public double resourceEfficiency = .2;
 		
 		//
 		// TRANSIT SPEED
 		//
 		//public double hyperdriveEfficiency = 1;
 		
 		//
 		// COMBAT PROWESS
 		//
 		public double defensiveCapabilities = 1;
 		public double offensiveCapabilities = 1;
 		
 		public int compareTo(TechLevel techLevel) {
 			return (int)((resourceEfficiency + /*hyperdriveEfficiency +*/ defensiveCapabilities + offensiveCapabilities)
 					- (techLevel.resourceEfficiency + /*techLevel.hyperdriveEfficiency +*/ techLevel.defensiveCapabilities + techLevel.offensiveCapabilities));
 		}
 
 		public boolean isMinimum() {
 			return (resourceEfficiency + /*hyperdriveEfficiency +*/ defensiveCapabilities + offensiveCapabilities) == Globals.MIN_TECH_LEVEL;
 		}
 		
 		public boolean isMaximum() {
 			return (resourceEfficiency + /*hyperdriveEfficiency +*/ defensiveCapabilities + offensiveCapabilities) == Globals.MAX_TECH_LEVEL;
 		}
 		
 		public double getTotalTechLevel()
 		{
 			return resourceEfficiency + /*hyperdriveEfficiency +*/ defensiveCapabilities + offensiveCapabilities;
 		}
 		
 		public double getCurrentLevel(int direction)
 		{
 			switch(direction){
 			case Globals.RESOURCE_RESEARCH:
 				return tech.resourceEfficiency;
 //			case Globals.HYPERDRIVE_RESEARCH:
 //				return tech.hyperdriveEfficiency;
 			case Globals.DEFENSE_RESEARCH:
 				return tech.defensiveCapabilities;
 			case Globals.OFFENSE_RESEARCH:
 				return tech.offensiveCapabilities;
 			default:
 				return 0;
 			}
 		}
 
 		public boolean isMaximum(int direction) {
 			switch(direction){
 			case Globals.RESOURCE_RESEARCH:
 				return tech.resourceEfficiency == Globals.MAX_RESOURCE_EFFICIENCY;
 //			case Globals.HYPERDRIVE_RESEARCH:
 //				return tech.hyperdriveEfficiency == Globals.MAX_HYPERDRIVE_EFFICIENCY;
 			case Globals.DEFENSE_RESEARCH:
 				return tech.defensiveCapabilities == Globals.MAX_DEFENSIVE_CAPABILITIES;
 			case Globals.OFFENSE_RESEARCH:
 				return tech.offensiveCapabilities == Globals.MAX_OFFENSIVE_CAPABILITIES;
 			default:
 				return false;
 			}
 		}
 	}
 	
 	public TechLevel getTechLevel() {
 		return tech;
 	}
 
 	public void improveTechLevel() {
 		improveTechLevel(new Random().nextInt(4));
 	}
 	
 	public void improveTechLevel(int direction) {
 		switch(direction) {
 		case Globals.RESOURCE_RESEARCH:
 			tech.resourceEfficiency+=.1;
 			if(tech.resourceEfficiency > Globals.MAX_RESOURCE_EFFICIENCY) tech.resourceEfficiency = Globals.MAX_RESOURCE_EFFICIENCY;
 			break;
 //		case 1:
 //			tech.hyperdriveEfficiency+=.5;
 //			if(tech.hyperdriveEfficiency > Globals.MAX_HYPERDRIVE_EFFICIENCY) tech.hyperdriveEfficiency = Globals.MAX_HYPERDRIVE_EFFICIENCY;
 //			break;
 		case Globals.DEFENSE_RESEARCH:
 			tech.defensiveCapabilities+=.5;
 			if(tech.defensiveCapabilities > Globals.MAX_DEFENSIVE_CAPABILITIES) tech.defensiveCapabilities = Globals.MAX_DEFENSIVE_CAPABILITIES;
 			break;
 		case Globals.OFFENSE_RESEARCH:
 			tech.offensiveCapabilities+=.5;
 			if(tech.offensiveCapabilities > Globals.MAX_OFFENSIVE_CAPABILITIES) tech.offensiveCapabilities = Globals.MAX_OFFENSIVE_CAPABILITIES;
 		default:
 			break;
 		}
 
 		if(this == Universe.playerFaction || homeWorld.hasSpy(Universe.playerFaction))
 			GameFrame.addToLog(factionName + " has improved their tech level.");
 	}
 	
 	public void reduceTechLevel() {
 		reduceTechLevel(new Random().nextInt(4));
 	}
 
 	public void reduceTechLevel(int direction) {
 		switch(direction) {
 		case 0:
 			tech.resourceEfficiency-=.1;
 			if(tech.resourceEfficiency < Globals.MIN_RESOURCE_EFFICIENCY) tech.resourceEfficiency = Globals.MIN_RESOURCE_EFFICIENCY;
 			break;
 //		case 1:
 //			tech.hyperdriveEfficiency-=.5;
 //			if(tech.hyperdriveEfficiency < Globals.MIN_HYPERDRIVE_EFFICIENCY) tech.hyperdriveEfficiency = Globals.MIN_HYPERDRIVE_EFFICIENCY;
 //			break;
 		case 2:
 			tech.defensiveCapabilities-=.5;
 			if(tech.defensiveCapabilities < Globals.MIN_DEFENSIVE_CAPABILITIES) tech.defensiveCapabilities = Globals.MIN_DEFENSIVE_CAPABILITIES;
 			break;
 		case 3:
 			tech.offensiveCapabilities-=.5;
 			if(tech.offensiveCapabilities < Globals.MIN_OFFENSIVE_CAPABILITIES) tech.offensiveCapabilities = Globals.MIN_OFFENSIVE_CAPABILITIES;
 		default:
 			break;
 		}
 
 		if(this == Universe.playerFaction || homeWorld.hasSpy(Universe.playerFaction))
 			GameFrame.addToLog(factionName + " has had their tech level lowered.");
 	}
 	
 	
 }
