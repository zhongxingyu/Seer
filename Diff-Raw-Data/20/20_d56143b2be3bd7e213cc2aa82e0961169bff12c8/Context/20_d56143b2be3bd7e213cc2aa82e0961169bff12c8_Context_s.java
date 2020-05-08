 /*
  * Copyright 2006 - 2011 
  *     Julien Baudry	<julien.baudry@graphstream-project.org>
  *     Antoine Dutot	<antoine.dutot@graphstream-project.org>
  *     Yoann Pigné		<yoann.pigne@graphstream-project.org>
  *     Guilhelm Savin	<guilhelm.savin@graphstream-project.org>
  * 
  * This file is part of GraphStream <http://graphstream-project.org>.
  * 
  * GraphStream is a library whose purpose is to handle static or dynamic
  * graph, create them from scratch, file or any source and display them.
  * 
  * This program is free software distributed under the terms of two licenses, the
  * CeCILL-C license that fits European law, and the GNU Lesser General Public
  * License. You can  use, modify and/ or redistribute the software under the terms
  * of the CeCILL-C license as circulated by CEA, CNRS and INRIA at the following
  * URL <http://www.cecill.info> or under the terms of the GNU LGPL as published by
  * the Free Software Foundation, either version 3 of the License, or (at your
  * option) any later version.
  * 
  * This program is distributed in the hope that it will be useful, but WITHOUT ANY
  * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
  * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
  * 
  * You should have received a copy of the GNU Lesser General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  * 
  * The fact that you are presently reading this means that you have had
  * knowledge of the CeCILL-C and LGPL licenses and that you accept their terms.
  */
 package org.graphstream.boids;
 
 import java.util.HashMap;
 
 import org.graphstream.graph.Graph;
 import org.graphstream.graph.NodeFactory;
 import org.graphstream.graph.implementations.AdjacencyListGraph;
 import org.graphstream.ui.spriteManager.Sprite;
 import org.graphstream.ui.spriteManager.SpriteManager;
 import org.graphstream.ui.swingViewer.Viewer;
 import org.graphstream.ui.swingViewer.util.Camera;
 import org.miv.pherd.ParticleBox;
 import org.miv.pherd.ntree.Anchor;
 import org.miv.pherd.ntree.CellSpace;
 import org.miv.pherd.ntree.OctreeCellSpace;
 import org.util.Environment;
 
 import java.util.Random;
 
 /**
  * Shared data for boids.
  * 
  * @author Damien Olivier
  * @author Guilhelm Savin
  * @author Antoine Dutot
  */
 public class Context extends AdjacencyListGraph {
 	// Attribute
 
 	/**
 	 * Number of steps to run the simulation, 0 means infinity.
 	 */
 	public int maxSteps = 0;
 
 	/**
 	 * The radius of the explored area. The real area range is [-area..area] in
 	 * all three dimensions.
 	 */
 	public float area = 1;
 
 	/**
 	 * Number of species.
 	 */
 	public int speciesCount = 3;
 
 	/**
 	 * Maximum number of particles per cell.
 	 */
 	public int maxParticlesPerCell = 10;
 
 	/**
 	 * Number of milliseconds to sleep between each particle computation step.
 	 */
 	public int sleepTime = 20;
 
 	/**
 	 * Store the forces as attributes so that each listener can retrieve the
 	 * force vectors.
 	 */
 	public boolean storeForcesAttributes = false;
 
 	/**
 	 * Remove the boids caught by a predator ?.
 	 */
 	public boolean removeCaughtBoids = false;
 
 	/**
 	 * Normalise boids attraction/repulsion vectors (make the boids move
 	 * constantly, since very small vectors can be extended).
 	 */
 	public boolean normalizeMode = true;
 
 	/**
 	 * Actual configuration directory.
 	 */
 	public String configDir = System.getProperty("user.dir");
 
 	/**
 	 * The file separator.
 	 */
 	public String fileSep = System.getProperty("file.separator");
 
 	/**
 	 * Show a GUI to monitor boids ?.
 	 */
 	public boolean showGui = false;
 
 	/**
 	 * public void setSleepTime( int sleepTime ) { this.sleepTime = sleepTime; }
 	 * 
 	 * Add a mouse "boid" if the GUI is used.
 	 */
 	public boolean showMouse = false;
 
 	/**
 	 * The fixed random seed.
 	 */
 	public long randomSeed = 1;
 
 	public String[] speciesFiles = null;
 
 	// Attribute
 
 	/**
 	 * The environment.
 	 */
 	protected Environment env;
 
 	protected CellSpace space;
 
 	/**
 	 * The particles.
 	 */
 	protected ParticleBox pbox;
 
 	// Attributes
 
 	/**
 	 * Species for boids.
 	 */
 	protected HashMap<String, BoidSpecies> boidSpecies;
 
 	/**
 	 * Species for the predator.
 	 */
 	// protected PredatorSpecies predatorSpecies;
 
 	/**
 	 * The mouse representation. The mouse is a particle like boids.
 	 */
 	// protected Mouse mouse;
 
 	/**
 	 * The main loop condition.
 	 */
 	protected boolean loop = false;
 
 	/**
 	 * Current step.
 	 */
 	protected int step = 0;
 
 	/**
 	 * Random number generator.
 	 */
 	public Random random = new Random();
 
 	protected boolean enableFakeStop = false;
 
 	// Construction
 
 	/**
 	 * New uninitialised context.
 	 * 
 	 * Use setup().
 	 */
 	public Context() {
 		super("boids-context");
 		setNodeFactory(new BoidFactory());
 		//nodeFactory = new BoidFactory();
 		boidSpecies = new HashMap<String, BoidSpecies>();
 		//boidSpecies.put("default", new BoidSpecies(this, "default"));
 		getOrCreateSpecies("default");
 		space = new OctreeCellSpace(new Anchor(-2, -2, -2), new Anchor(2, 2, 2));
 		pbox = new ParticleBox(maxParticlesPerCell, space, new BoidCellData());
 	}
 
 	// Access
 
 	public void setSpeciesFiles(String files) {
 		speciesFiles = files.split(",");
 
 		if (speciesFiles != null)
 			for (int i = 0; i < speciesFiles.length; i++)
 				speciesFiles[i] = speciesFiles[i].trim();
 	}
 
 	public CellSpace getSpace() {
 		return space;
 	}
 
 	public float getArea() {
 		return area;
 	}
 
 	public int getMaxParticlesPerCell() {
 		return maxParticlesPerCell;
 	}
 
 	/**
 	 * The current environment.
 	 * 
 	 * @return The environment.
 	 */
 	public Environment getEnv() {
 		return env;
 	}
 
 	/**
 	 * The current particle box.
 	 * 
 	 * @return The particle box.
 	 */
 	public ParticleBox getPbox() {
 		return pbox;
 	}
 
 	/**
 	 * The GUI mouse particle.
 	 * 
 	 * @return The mouse particle.
 	 */
 	// public Mouse getMouse() {
 	// /// return mouse;
 	// }
 
 	public BoidSpecies getOrCreateSpecies(String name) {
 		BoidSpecies species = boidSpecies.get(name);
 
 		if (species == null) {
 			species = new BoidSpecies(this, name);
 			boidSpecies.put(name, species);
 
 			System.out.printf("new species : %s\n", name);
 		}
 
 		return species;
 	}
 
 	public BoidSpecies getSpecies(String name) {
 		return boidSpecies.get(name);
 	}
 
 	public int getSpeciesCount() {
 		return boidSpecies.size();
 	}
 
 	public BoidSpecies getDefaultSpecies() {
 		return getSpecies("default");
 	}
 
 	public void deleteSpecies(String name) {
 		if (!name.equals("default"))
 			boidSpecies.remove(name);
 	}
 
 	// Commands
 
 	public void setArea(float area) {
 		this.area = area;
 	}
 
 	public void setSpeciesCount(int speciesCount) {
 		this.speciesCount = speciesCount;
 	}
 
 	public void setStoreForcesAttributes(boolean storeForcesAttributes) {
 		this.storeForcesAttributes = storeForcesAttributes;
 	}
 
 	public void setConfigDir(String configDir) {
 		this.configDir = configDir;
 	}
 
 	public void setShowGui(boolean showGui) {
 		this.showGui = showGui;
 	}
 
 	public void setMaxSteps(int maxSteps) {
 		this.maxSteps = maxSteps;
 	}
 
 	public void setShowMouse(boolean showMouse) {
 		this.showMouse = showMouse;
 	}
 
 	public void setRandomSeed(long randomSeed) {
 		this.randomSeed = randomSeed;
 	}
 
 	public void setSleepTime(int sleepTime) {
 		this.sleepTime = sleepTime;
 	}
 
 	public void setRemoveCaughtBoids(boolean removeCaughtBoids) {
 		this.removeCaughtBoids = removeCaughtBoids;
 	}
 
 	/**
 	 * Stop the main simulation loop.
 	 */
 	public void stopLoop() {
 		loop = false;
 	}
 
 	/**
 	 * Run the simulation in a loop.
 	 */
 	public void loop() {
 		loop = true;
 
 		while (loop) {
 			pbox.step();
 
 			for (BoidSpecies sp : boidSpecies.values())
 				sp.terminateLoop();
 
 			sleep(sleepTime);
 
 			step++;
 
 			if (maxSteps > 0 && step > maxSteps)
 				loop = false;
 		}
 	}
 
 	@Override
 	public void stepBegins(double step) {
 		pbox.step();
 	}
 
 	public boolean isLooping() {
 		return loop;
 	}
 
 	protected void sleep(int milliseconds) {
 		try {
 			Thread.sleep(milliseconds);
 		} catch (InterruptedException e) {
 		}
 	}
 
 	@Override
 	protected void attributeChanged(String sourceId, long timeId,
 			String attribute, AttributeChangeEvent event, Object oldValue,
 			Object newValue) {
 		String key = attribute;
 
 		if (key.startsWith("species.")) {
 			key = key.substring("species.".length());
 			String name;
 
 			if (key.indexOf('.') > 0) {
 				name = key.substring(0, key.indexOf('.'));
 				key = key.substring(name.length() + 1);
 			} else {
 				name = key;
 				key = null;
 			}
 
 			switch (event) {
 			case REMOVE:
 				if (boidSpecies.containsKey(name))
 					deleteSpecies(name);
 
 				break;
 			case ADD:
 			case CHANGE:
 				BoidSpecies species = getOrCreateSpecies(name);
 
 				if (key != null)
 					species.set(key,
 							newValue == null ? null : newValue.toString());
 
 				break;
 			}
 		}
 
 		super.attributeChanged(sourceId, timeId, attribute, event, oldValue,
 				newValue);
 	}
 
 	private class BoidFactory implements NodeFactory<Boid> {
 		public Boid newInstance(String id, Graph graph) {
 			Boid b = new Boid(graph, id);
 			pbox.addParticle(b.getParticle());
 
 			return b;
 		}
 	}
 
 	public static void main(String... args) {
 		Context ctx = new Context();
 		BoidSpecies species = ctx.getDefaultSpecies();
		species.angleOfView = -1;
 		
 		ctx.addAttribute("ui.quality");
 		ctx.addAttribute("ui.antialias");
 		ctx.addAttribute("ui.stylesheet", "node { size: 4px; } edge { fill-color: grey; }");
 		Viewer viewer = ctx.display(false);
 
 		Camera cam = viewer.getDefaultView().getCamera();
 		
 		cam.setGraphViewport(-2, -2, 2, 2);
 		
 		for (int i = 0; i < species.count; i++)
 			ctx.addNode(String.format("boid%03d", i));
 
 		while (true) {
 			ctx.stepBegins(0);
 			
 			try {
 				Thread.sleep(50);
 			} catch (InterruptedException e) {
 			}
 		}
 	}
 }
