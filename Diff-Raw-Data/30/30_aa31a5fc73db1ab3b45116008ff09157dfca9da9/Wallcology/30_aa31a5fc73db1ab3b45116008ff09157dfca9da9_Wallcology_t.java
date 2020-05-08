 /*
  * Created Aug 1, 2011
  */
 package ltg.ps.phenomena.wallcology;
 
 import java.io.IOException;
 import java.io.StringWriter;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import ltg.ps.api.phenomena.ActivePhenomena;
 import ltg.ps.api.phenomena.PhenomenaWindow;
 import ltg.ps.phenomena.wallcology.population_calculators.HardCodedCalculator;
 import ltg.ps.phenomena.wallcology.population_calculators.PopulationCalculator;
 import ltg.ps.phenomena.wallcology.support.WallcologyPersistence;
 
 import org.dom4j.Document;
 import org.dom4j.DocumentException;
 import org.dom4j.DocumentHelper;
 import org.dom4j.Element;
 import org.dom4j.io.OutputFormat;
 import org.dom4j.io.XMLWriter;
 
 /**
  * Wallcology implementation of the abstract class ActivePhenomena
  *
  * @author Gugo
  */
 public class Wallcology extends ActivePhenomena {
 
 	// Components
 	private WallcologyPersistence db = null;
 	private PopulationCalculator pc = null;
 
 	// Simulation
 	private List<Wall> currentPhaseWalls = null;
 	private List<Wall> prevPhaseWalls = null;
 	private List<Wall> nextPhaseWalls = null;
 	private WallcologyPhase currentPhase = null;
 	private WallcologyPhase prevPhase = null;
 	private WallcologyPhase nextPhase = null;
 	// Phase trasition
 	private long totalTransitionTime = -1;
 	private long elapsedTransitionTime = -1;
 	private long startTime = -1;
 	// Unused simulation data
 	private List<Wall> microworlds = null;
 	private List<TaggedCreature> taggedbugs = null;
 	private List<Species> species = null;
 	// Other wallscopes parameters
 	private boolean editEnabled = false;
 
 
 	/**
 	 * @param instanceId
 	 */
 	public Wallcology(String instanceId) {
 		super(instanceId);
 		// Set update time to 1 minute
 		setSleepTime(60);
 		// Init components
 		db = new WallcologyPersistence(this);
 		//pc = new DBCalculator();
 		pc = new HardCodedCalculator();
 		// Init walls lists
 		currentPhaseWalls = new ArrayList<Wall>();
 		prevPhaseWalls = new ArrayList<Wall>();
 		nextPhaseWalls = new ArrayList<Wall>();
 		// Init unused data
 		microworlds = new ArrayList<Wall>();
 		taggedbugs = new ArrayList<TaggedCreature>();
 		species = new ArrayList<Species>();
 	}
 
 
 	/* (non-Javadoc)
 	 * @see ltg.ps.api.phenomena.Phenomena#configureWindows(java.lang.String)
 	 */
 	@Override
 	public void configureWindows(String windowsXML) {
 		// reset the windows
 		phenWindows.clear();
 		// create new windows
 		Document doc = null;
 		try {
 			doc = DocumentHelper.parseText(windowsXML);
 			@SuppressWarnings("unchecked")
 			List<Element> windows = doc.getRootElement().elements();
 			for(Element e: windows) {
 				if(e.attributeValue("type").equals("client")) {
 					WallcologyWindow wcwin = new WallcologyWindow(
 							e.attributeValue("id"),
 							Integer.valueOf(e.attributeValue("x")),
 							Integer.valueOf(e.attributeValue("y")),
 							e.attributeValue("wall")
 							);
 					phenWindows.add(wcwin);
 				}
 				if(e.attributeValue("type").equals("micro")) {
 					phenWindows.add(new WallcologyMicroWindow(e.attributeValue("id")));
 				}
 				if(e.attributeValue("type").equals("control")) {
 					phenWindows.add(new WallcologyControlWindow(e.attributeValue("id")));
 				}
 				if(e.attributeValue("type").equals("notifier")) {
 					phenWindows.add(new WallcologyNotifierWindow(e.attributeValue("id")));
 				}
 			}
 			db.save();
 		} catch (DocumentException e) {
 			log.warn("Impossible to configure wallcology windows");
 		}
 	}
 
 
 	/* (non-Javadoc)
 	 * @see ltg.ps.api.phenomena.ActivePhenomena#configure(java.lang.String)
 	 */
 	@SuppressWarnings("unchecked")
 	@Override
 	public void configure(String configXML) {
 		// reset all simulation data!!!
 		currentPhaseWalls.clear();
 		prevPhaseWalls.clear();
 		nextPhaseWalls.clear();
 		currentPhase = null;
 		prevPhase = null;
 		nextPhase = null;
 		totalTransitionTime = -1;
 		elapsedTransitionTime = -1;
 		startTime = -1;
 		//... including unused data
 		species.clear();
 		taggedbugs.clear();
 		microworlds.clear();
 		
 		// Load state from XML
 		Document doc = null;
 		try {
 			doc = DocumentHelper.parseText(configXML);
 			Element el = doc.getRootElement();
 			// Phases
 			if(el.elementTextTrim("currentPhase")!=null && !el.elementTextTrim("currentPhase").equals(""))
 				currentPhase = WallcologyPhase.valueOf(el.elementTextTrim("currentPhase"));
 			if(el.elementTextTrim("prevPhase")!=null && !el.elementTextTrim("prevPhase").equals(""))
 				prevPhase = WallcologyPhase.valueOf(el.elementTextTrim("prevPhase"));
 			if(el.elementTextTrim("nextPhase")!=null && !el.elementTextTrim("nextPhase").equals(""))
 				nextPhase = WallcologyPhase.valueOf(el.elementTextTrim("nextPhase"));
 			// Times
 			if(el.elementTextTrim("startTime")!=null && !el.elementTextTrim("startTime").equals(""))
 				startTime = Long.parseLong(el.elementTextTrim("startTime"));
 			else
 				startTime = Calendar.getInstance().getTimeInMillis(); //start time set to right now
 			if(el.elementTextTrim("totalTransitionTime")!=null && !el.elementTextTrim("totalTransitionTime").equals(""))
 				totalTransitionTime = Long.parseLong(el.elementTextTrim("totalTransitionTime"));
 			if(el.elementTextTrim("elapsedTransitionTime")!=null && !el.elementTextTrim("elapsedTransitionTime").equals(""))
 				elapsedTransitionTime = Long.parseLong(el.elementTextTrim("elapsedTransitionTime"));
 			// Wallscope parameters
 			if(el.elementTextTrim("editEnabled")!=null && !el.elementTextTrim("editEnabled").equals(""))
 				editEnabled = Boolean.parseBoolean(el.elementTextTrim("editEnabled"));
 			// Current Walls
 			Wall wtemp = null;
 			List<Element> xmlwalls = el.element("walls").elements();
 			for (Element el1: xmlwalls) {
 				// Parse
 				wtemp = parseWall(el1);
 				// Add wall to currentWall
 				currentPhaseWalls.add(wtemp);
 			}
 			// Previous walls
 			wtemp = null;
 			if(el.element("prevPhaseWalls")!=null) {
 				List<Element> xmlprevWalls = el.element("prevPhaseWalls").elements();
 				for (Element el1: xmlprevWalls) {
 					// Parse
 					wtemp = parseWall(el1);
 					// Add wall to currentWall
 					prevPhaseWalls.add(wtemp);
 				}
 			}
 			// Next walls
 			wtemp = null;
 			if(el.element("nextPhaseWalls")!=null) {
 				List<Element> xmlnextWalls = el.element("nextPhaseWalls").elements();
 				for (Element el1: xmlnextWalls) {
 					// Parse
 					wtemp = parseWall(el1);
 					// Add wall to currentWall
 					nextPhaseWalls.add(wtemp);
 				}
 			}
 			// First update that loads the proper data into the walls
 			updatePopulation();
 			// Save!!!
 			db.save();
 			this.setChanged();
 		} catch (DocumentException e) {
 			log.warn("Impossible to configure Wallcology");
 		}
 	}
 
 
 	private Wall parseWall(Element el1) {
 		HashMap<String, Integer> wallpopulation = null;
 		// Population
 		@SuppressWarnings("unchecked")
 		List<Element> creatures = el1.element("population").elements();
 		wallpopulation = new HashMap<String, Integer>();
 		for (Element el2: creatures) {
 			wallpopulation.put(
 					el2.getName(),
 					Integer.valueOf(el2.getTextTrim())
 					);
 		}
 		// Parameters
 		Wall wtemp = new Wall(el1.attributeValue("id"),
 				Integer.valueOf(el1.attributeValue("width")),
 				Integer.valueOf(el1.attributeValue("height")),
 				Float.valueOf(el1.element("environment").elementTextTrim("temperature")),
 				Float.valueOf(el1.element("environment").elementTextTrim("humidity")),
 				Float.valueOf(el1.element("environment").elementTextTrim("light")),
 				wallpopulation
 				);
 		return wtemp;
 	}
 
 
 	@Override
 	public void restore() {
 		db.restore();
 	}
 
 
 	@Override
 	public void cleanup() {
 		db.cleanup();
 	}
 
 
 	@Override
 	protected synchronized void updatePhenomena() throws InterruptedException {
 		// Update transition time if in transition phase
 		updatePhase();
 		updatePopulation();	
 		db.save();
 		this.setChanged(); 
 	}
 
 
 	private void updatePhase() {
 		if (currentPhase.equals(WallcologyPhase.TRANSIT_PHASE)) {
 			if (elapsedTransitionTime >= totalTransitionTime) {
 				completePhaseTransition();
 				return;
 			}
 			elapsedTransitionTime++;
 		}
 	}
 	
 	
 	private void updatePopulation() {
 		// Update population...
 		if (prevPhase==null && nextPhase==null) {
 			//...stable 
 			pc.updatePopulationStable(currentPhaseWalls, currentPhase);
 		} else {
 			// ... transition
 			pc.updatePopulationTransit(nextPhase, currentPhaseWalls, prevPhaseWalls, 
 					nextPhaseWalls, totalTransitionTime, elapsedTransitionTime);
 		}
 	}
 
 
 	public String toXML() {
 		Element root = DocumentHelper.createElement(instanceName);
 
 		// Windows
 		Element windows = DocumentHelper.createElement("windows");
 		for(PhenomenaWindow w: phenWindows) {
 			Element win = DocumentHelper.createElement("win");
 			win.addAttribute("id", w.getWindowId());
 			if(w instanceof WallcologyWindow) {
 				win.addAttribute("type", "client");
 				if(((WallcologyWindow) w).getWallId() != null)
 					win.addAttribute("wall", ((WallcologyWindow) w).getWallId());
 				win.addAttribute("x", String.valueOf(((WallcologyWindow) w).getX()));
 				win.addAttribute("y", String.valueOf(((WallcologyWindow) w).getY()));
 			}
 			if(w instanceof WallcologyMicroWindow) {
 				win.addAttribute("type", "micro");
 			}
 			if(w instanceof WallcologyControlWindow) {
 				win.addAttribute("type", "control");
 			}
 			if(w instanceof WallcologyNotifierWindow) {
 				win.addAttribute("type", "notifier");
 			}
 			windows.add(win);
 		}
 		root.add(windows);
 
 		// Config
 		Element config = DocumentHelper.createElement("config");
 		// Phases
 		if (currentPhase!=null)
 			config.addElement("currentPhase").addText(String.valueOf(currentPhase));
 		if (nextPhase!=null)
 			config.addElement("nextPhase").addText(String.valueOf(nextPhase));
 		if (prevPhase!=null)
 			config.addElement("prevPhase").addText(String.valueOf(prevPhase));
 		// Times
 		if (startTime!=-1)
 			config.addElement("startTime").addText(String.valueOf(startTime));
 		if (totalTransitionTime!=-1)
 			config.addElement("totalTransitionTime").addText(String.valueOf(totalTransitionTime));
 		if (elapsedTransitionTime!=-1)
 			config.addElement("elapsedTransitionTime").addText(String.valueOf(elapsedTransitionTime));
 		// Wallscope parameters
 		config.addElement("editEnabled").addText(String.valueOf(editEnabled));
 		// Current walls
 		if(!currentPhaseWalls.isEmpty())
 			config.add(wallsToXML(currentPhaseWalls, "walls"));
 		// Prev walls
 		if(!prevPhaseWalls.isEmpty())
 			config.add(wallsToXML(prevPhaseWalls, "prevPhaseWalls"));
 		// Next walls
 		if(!nextPhaseWalls.isEmpty())
 			config.add(wallsToXML(nextPhaseWalls, "nextPhaseWalls"));
 		
 		// Add configuration to the tree
 		root.add(config);
 		// Create document
 		return removeXMLDeclaration(DocumentHelper.createDocument(root));
 	}
 	
 	
 	private Element wallsToXML(List<Wall> walls, String elName) {
 		Element wallsdescription = DocumentHelper.createElement(elName);
 		for(Wall w : walls) {
 			Element wall = DocumentHelper.createElement("wall");
 			wall.addAttribute("id", w.getId());
 			wall.addAttribute("width", String.valueOf(w.getWidth()));
 			wall.addAttribute("height", String.valueOf(w.getHeight()));
 			Element adjacency = DocumentHelper.createElement("adjacency");
 			List<Wall> adjacencyList = w.getAdjacents();
 			if(adjacencyList != null && !adjacencyList.isEmpty()) {
 				for(Wall s : adjacencyList) {
 					adjacency.addElement("adjacentWall").addText(s.getId());
 				}
 				wall.add(adjacency);
 			}
 			Element env = DocumentHelper.createElement("environment");
 			env.addElement("temperature").addText(String.valueOf(w.getTemperature()));
 			env.addElement("humidity").addText(String.valueOf(w.getHumidity()));
 			env.addElement("light").addText(String.valueOf(w.getLight()));
 			wall.add(env);
 			Element pop = DocumentHelper.createElement("population");
 			Map<String, Integer> populationList = w.getPopulation();
 			if(populationList != null) {
 				for (Map.Entry<String, Integer> m : populationList.entrySet()) {
 					pop.addElement(m.getKey()).addText(String.valueOf(m.getValue()));
 				}
 			}
 			wall.add(pop);
 			wallsdescription.add(wall);
 		}
 		return wallsdescription;
 	}
 
 	
 	public String removeXMLDeclaration(Document doc) {
 		StringWriter w = new StringWriter();
 		OutputFormat f =  OutputFormat.createPrettyPrint();
 		f.setSuppressDeclaration(true);
 		XMLWriter xw = new XMLWriter(w, f);
 		try {
 			xw.write(doc);
 		} catch (IOException e1) {
 			log.error("Unable to print to a string? Really?");
 		}
 		return w.toString();
 	}
 
 	
 	public Wall getWall(String id) {
		for(Wall w : currentPhaseWalls) {
 			if(w.getId().equals(id))
 				return w;
 		}
 		return null;
 	}
 
 	public List<Wall> getWalls() {
 		return this.currentPhaseWalls;
 	}
 
 	public List<Wall> getMicroworlds() {
 		return this.microworlds;
 	}
 
 	public synchronized void tagCreature(TaggedCreature c) {
 		taggedbugs.add(c);
 	}
 
 	public synchronized void cleartags() {
 		taggedbugs.clear();
 	}
 
 	public synchronized void startExperiment(Wall mw) {
 		if(microworlds.remove(mw))
 			microworlds.add(mw);
 		db.save();
 		notifyObservers();
 	}
 
 	public synchronized void terminateExperiment(Wall mw) {
 		microworlds.remove(mw);
 		db.save();
 		notifyObservers();
 	}
 
 	public String toString() {
 		String str = "";
 		str += this.instanceName + "\n";
 		for(Wall wid : currentPhaseWalls) {
 			str =  str + wid.getId() + " ";
 			Set<String> keys = wid.getPopulation().keySet();
 			for(String s: keys)
 				str = str + s + ": " + wid.getPopulation().get(s) + ", ";
 					str += "\n";
 		}
 		return str;
 	}
 
 
 	public WallcologyPhase getPhase() {
 		return currentPhase;
 	}
 	
 	
 	public boolean getEditEnabled() {
 		return this.editEnabled;
 	}
 
 	
 
 	public synchronized void setPhase(WallcologyPhase nextPhase, Map<String, Environment> nextWalls, long transitionLength) {
 		if(currentPhase!=WallcologyPhase.TRANSIT_PHASE) {
 			// Set phases
 			this.nextPhase = nextPhase;
 			prevPhase = currentPhase;
 			currentPhase = WallcologyPhase.TRANSIT_PHASE;
 			// Transition times
 			totalTransitionTime = transitionLength;
 			elapsedTransitionTime = 0;
 			// Set walls
 			prevPhaseWalls.addAll(currentPhaseWalls);
 			nextPhaseWalls = setNextPhaseWalls(nextWalls);
 			currentPhaseWalls.clear();
 			currentPhaseWalls.addAll(nextPhaseWalls);
 			// First refresh of population
 			pc.updatePopulationTransit(nextPhase, currentPhaseWalls, prevPhaseWalls, 
 					nextPhaseWalls, totalTransitionTime, elapsedTransitionTime);
 			// Save!!!
 			db.save();
 		} else {
 			// trying to change to a different phase with a transition 
 			// already in progress - skip...
 			if (!nextPhase.equals(this.nextPhase))
 				return;
 			// If length is shorter than elapsed time - terminate transition...
 			if (transitionLength < elapsedTransitionTime) {
 				completePhaseTransition();
 			} else {
 				// ... otherwise update the transit speed...
 				totalTransitionTime = transitionLength;
 				// ... and the window
 				// Save!!!
 				db.save();
 			}
 		}
 	}
 
 
 	private List<Wall> setNextPhaseWalls(Map<String, Environment> nextWalls) {
 		Environment e;
 		Wall ww = null;
 		List<Wall> nw = new ArrayList<Wall>();
 		for(Wall w : currentPhaseWalls) {
 			e = nextWalls.get(w.getId());
 			ww = w.copy();
 			ww.setTemperature(e.getTemperature());
 			ww.setLight(e.getLight());
 			ww.setHumidity(e.getHumidity());
 			nw.add(ww);
 		}
 		return nw;
 	}
 
 
 	/**
 	 * Completes a phase transition
 	 */
 	private synchronized void completePhaseTransition() {
 		// Phase
 		currentPhase = nextPhase;
 		nextPhase = null;
 		prevPhase = null;
 		// Transition times
 		totalTransitionTime = -1;
 		elapsedTransitionTime = -1;
 		// Walls
 		nextPhaseWalls.clear();
 		prevPhaseWalls.clear();
 		// Save!!!
 		db.save();
 	}
 	
 	
 	public synchronized void setEditEnabled(boolean value) {
 		this.editEnabled = value;
 		db.save();
 		this.setChanged();
 		this.notifyObservers();
 	}
 	
 
 
 	public synchronized void editEnvironment(Wall original, Wall modified) {
 		for(Wall w: currentPhaseWalls) {
 			if (w == original) {
 				w.setTemperature(modified.getTemperature());
 				w.setLight(modified.getLight());
 				w.setHumidity(modified.getHumidity());
 				db.save();
 				//log.info("Change wall from " + transitionTime + " : t->" +original.getTemperature()+ " l->"+original.getLight() +" h->"+original.getHumidity());
 				//log.info("..............to " + transitionTime + " : t->" +modified.getTemperature()+ " l->"+modified.getLight() +" h->"+modified.getHumidity());
 
 			}
 		}
 	}
 
 }
