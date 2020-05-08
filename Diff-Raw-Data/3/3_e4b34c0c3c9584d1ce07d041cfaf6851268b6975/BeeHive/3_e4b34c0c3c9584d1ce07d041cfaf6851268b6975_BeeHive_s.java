 package de.unihalle.sim.entities;
 
 import de.unihalle.sim.main.BeeSimulation;
 import de.unihalle.sim.util.Position;
 
 public class BeeHive extends PositionedEntity {
 
 	// http://en.wikipedia.org/wiki/List_of_cities_in_Italy
 
 	private static final double EGG_SPAWN_RATE = BeeSimulation.getInputData().getEggSpawnRate();
 	private static final double INITIAL_INFECTION_PERCENTAGE = BeeSimulation.getInputData()
 			.getInitialInfectionPercentage();
 	private static final double WORKER_BEE_PERCENTAGE = BeeSimulation.getInputData().getWorkerBeePercentage();
 
 	private int _populationCapacity;
 	private int _currentPopulation = 0;
 	private int _currentWorkerBeePopulation = 0;
 	private int _beeCounter = 0;
 	private int _storedNectar = 0;
 
 	public BeeHive(Position position, int capacity) {
 		super();
 		_populationCapacity = capacity;
 		_position = position;
 	}
 
 	@Event
 	public void spawnBee() {
 		if (_populationCapacity > _currentPopulation) {
 			boolean newBeeIsWorker = _currentWorkerBeePopulation < _currentPopulation * WORKER_BEE_PERCENTAGE;
 			Bee newBee = Bee.create(this, newBeeIsWorker);
 			register(newBee, getName() + "." + (newBeeIsWorker ? "WorkerBee" : "HiveBee") + _beeCounter);
 			BeeSimulation.getEnvironment().addBee(newBee);
 			if (newBeeIsWorker) {
 				_currentWorkerBeePopulation++;
 			}
 			_beeCounter++;
 			_currentPopulation++;
 		}
 		schedule("spawnBee", EGG_SPAWN_RATE);
 	}
 
 	@Override
 	public void initialize() {
 		infoWithPosition("I am alive!");
 		fillHive();
 		schedule("spawnBee", EGG_SPAWN_RATE);
 	}
 
 	private void fillHive() {
 		for (int i = 0; i < _populationCapacity; i++) {
 			boolean newBeeIsWorker = _currentWorkerBeePopulation < _currentPopulation * WORKER_BEE_PERCENTAGE;
 			Bee newBee = Bee.create(this, newBeeIsWorker);
 			register(newBee, getName() + "." + (newBeeIsWorker ? "WorkerBee" : "HiveBee") + _beeCounter);
 			BeeSimulation.getEnvironment().addBee(newBee);
 			if (newBeeIsWorker) {
 				_currentWorkerBeePopulation++;
 			}
 			_beeCounter++;
 			_currentPopulation++;
 		}
 		BeeSimulation.getEnvironment().applyInitialInfectionToHive(this, INITIAL_INFECTION_PERCENTAGE);
 	}
 
 	public void reportDead(Bee deadBee) {
 		if (_currentPopulation <= 0) {
 			// Exception is caught by Tortuga framework
 			// throw new RuntimeException("A bee of an empty hive wanted to die. This is impossible.");
 			System.err.println("A bee of an empty hive wanted to die. This is impossible.");
 			System.exit(1);
 		}
 		BeeSimulation.getEnvironment().removeBee(deadBee);
 		_currentPopulation--;
 	}
 
 	public void storeNectar(int amount) {
 		_storedNectar += amount;
 	}
 
 	public int getStoredNectar() {
 		return _storedNectar;
 	}
 
 }
