 package uk.ac.cam.groupproj.racethewild;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.List;
 import java.util.Map;
 import java.util.Random;
 
 import android.content.Context;
 import android.content.res.Resources;
 
 public class Engine {
 	
 	public final static String NODEPASS_MESSAGE = "uk.ac.cam.groupproj.racethewild.NODEPASS";
 	public final static String ANIMAL_NUMBER_MESSAGE = "uk.ac.cam.groupproj.racethewild.ANIMAL_NUMBER";
 
 	private PlayerStats stats;
 	private List<Node> nodes;
 	private Map<Integer, Animal> animalDictionary;
 	private Resources resources;
 	
 	private static Engine engine;
 	
 	public static Engine get() { return engine; }
 
 	public PlayerStats getStats() { return stats; }
 	
 	public List<Node> getNodeList() {
 		return nodes;
 	}
 	
 	/** Get data from the sat-nav process. */
 	public SatNavUpdate fetchSatNavData() {
 		// Temporary implementation generates random update.
 		Random random = new Random();
 		SatNavUpdate newUpdate = new SatNavUpdate(50 + random.nextInt(200), random.nextInt(200));
 		// TODO: Implement fully.
 		
 		// Accumulation.
 		stats.processSatNav(newUpdate);
 		return new SatNavUpdate(stats.getAccumulatedDistance(), 
 				stats.getAccumulatedMovePoints());
 	}
 	
 	/** Returns a List of all Animals, sorted by ID.
 	 ** Needed by the Collection Scene. */
 	public List<Animal> getAllAnimals() {
 		Collection<Animal> animalCollection = animalDictionary.values();
 		List<Animal> animals = new ArrayList<Animal>();
 		for(Animal animal : animalCollection) {
 			animals.add(animal);
 		}
 		Collections.sort(animals);
 		return animals;
 	}
 	
 	/** Returns the animal corresponding to the input ID */
 	public Animal getAnimal(int ID) {
 		return animalDictionary.get(ID);
 	}
 
 	/** Called at start-up to create the engine and initialise it. */
 	public static void initialise(Context c) { // Initial setup when app is started.
 		if (engine != null) return; // Ignore subsequent calls.
 		engine = new Engine(c);
 		// TODO: Start the sat-nav process, if not running.
 		// Load node data.
 		try {
 			engine.nodes = XmlParser.createNodes(engine.resources.getXml(R.xml.nodedata));
 		} catch(XmlReadException e) {
 			System.err.println(e.getMessage());
 			System.err.println("Failed to load nodes, killing the app.");
 			System.exit(-1);
 		}
 		
 		// Load animal data.
 		try {
 			engine.animalDictionary = XmlParser.createDictionary(engine.resources.getXml(R.xml.animaldata));
 		} catch(XmlReadException e) {
 			System.err.println(e.getMessage());
 			System.err.println("Failed to load animal dictionary, killing the app.");
 			System.exit(-2);
 		}
 		// Load the player's stats.
 		engine.stats = PlayerStats.load(c);
 		// Update animal colours based on loaded data.
 		for(Integer id : engine.stats.getGreyAnimals()) 
 			Engine.get().changeColour(id, Colour.Grey, c);
 		for(Integer id : engine.stats.getBlackAnimals()) 
 			Engine.get().changeColour(id, Colour.Black, c);
 	}
 	
 	/** Sets the colour of the animal with the given ID to the given colour. */
 	public void changeColour(int animalID, Colour colour, Context c) {
 		// Update the Animal object.
 		Animal animal = this.getAnimal(animalID);
 		animal.setColour(colour);
 		if(colour == Colour.Grey) {
			// Populate the animal in the world.
			populateAnimal(animal);
 			// Update in PlayerStats.
 			stats.addGreyAnimal(animalID);
 		}
 		else if(colour == Colour.Black) {
 			// Update in PlayerStats.
 			stats.addBlackAnimal(animalID);
 		}
 		// Save.
 		stats.save(c);
 	}
 	
 	/** Releases the given animal and resets distance and movement point accumulation. */
 	public void checkIn(Animal animal, Context c) {
 		if(animal != null) {
 			this.changeColour(animal.getID(), Colour.Grey, c);
 		}
 		// Update playerstats.
 		stats.checkIn();
 		stats.save(c);
 	}
 	
 	/** Takes the name of a node, and returns the object associated with it. */
 	public Node lookupNode(String name) throws NodeNotFoundException {
 		for (Node node : nodes) {
 			if (node.hasName(name)) return node;
 		}
 		// Fail if the node isn't found.
 		throw new NodeNotFoundException();
 	}
 	
 	/** Adds an animal to its associated nodes. */
 	protected void populateAnimal(Animal animal) {
 		// Don't populate undiscovered animals.
 		if (animal.getColour() == Colour.White) return;
 		
 		Node[] animalNodes = animal.getNodes();
 		for (int i = 0; i < animalNodes.length; i++) {
			animalNodes[i].addAnimal(animal);
 		}
 	};
 				//Add an animal to the Nodes in which it should appear.
 	
 	public static void reset(Context c) {
 		engine.stats = new PlayerStats();
 		engine.stats.save(c);
 		engine = null;
 		Engine.initialise(c);
 	}
 
 	/** Create the engine at app start.
 	 *  Takes a Resources reference to access resource files. */
 	private Engine(Context c) {
 		this.resources = c.getResources();
 	}
 	
	/** Part of initialisation. Loads the player data at start-up. */
 }
