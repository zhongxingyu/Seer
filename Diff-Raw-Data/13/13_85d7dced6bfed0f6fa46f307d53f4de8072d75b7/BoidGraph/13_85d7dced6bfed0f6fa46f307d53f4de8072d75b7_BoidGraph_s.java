 /*
  * Copyright 2006 - 2012
  *     Antoine Dutot	<antoine.dutot@graphstream-project.org>
  *     Guilhelm Savin	<guilhelm.savin@graphstream-project.org>
  * 
  * This file is part of gs-boids <http://graphstream-project.org>.
  * 
  * gs-boids is a library whose purpose is to provide a boid behavior to a set of
  * particles.
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
 
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.lang.reflect.InvocationTargetException;
 import java.util.HashMap;
 
 import org.graphstream.graph.Graph;
 import org.graphstream.graph.NodeFactory;
 import org.graphstream.graph.implementations.AbstractNode;
 import org.graphstream.graph.implementations.AdjacencyListGraph;
 import org.graphstream.stream.file.FileSourceDGS;
 import org.graphstream.ui.swingViewer.Viewer;
 import org.graphstream.ui.swingViewer.util.Camera;
 import org.miv.pherd.ParticleBox;
 import org.miv.pherd.ntree.Anchor;
 import org.miv.pherd.ntree.CellSpace;
 import org.miv.pherd.ntree.OctreeCellSpace;
 
 import java.util.Random;
 
 /**
  * Shared data for boids.
  * 
  * @author Damien Olivier
  * @author Guilhelm Savin
  * @author Antoine Dutot
  */
 public class BoidGraph extends AdjacencyListGraph {
 
 	public static enum Parameter {
 		MAX_STEPS, AREA, SLEEP_TIME, STORE_FORCES_ATTRIBUTES, REMOVE_CAUGHT_BOIDS, NORMALIZE_MODE, RANDOM_SEED
 	}
 
 	/**
 	 * Number of steps to run the simulation, 0 means infinity.
 	 */
 	protected int maxSteps;
 
 	/**
 	 * The radius of the explored area. The real area range is [-area..area] in
 	 * all three dimensions.
 	 */
 	protected double area;
 
 	/**
 	 * Number of milliseconds to sleep between each particle computation step.
 	 */
 	protected int sleepTime;
 
 	/**
 	 * Store the forces as attributes so that each listener can retrieve the
 	 * force vectors.
 	 */
 	protected boolean storeForcesAttributes;
 
 	/**
 	 * Remove the boids caught by a predator ?.
 	 */
 	protected boolean removeCaughtBoids;
 
 	/**
 	 * Normalise boids attraction/repulsion vectors (make the boids move
 	 * constantly, since very small vectors can be extended).
 	 */
 	protected boolean normalizeMode;
 
 	/**
 	 * The fixed random seed.
 	 */
 	protected long randomSeed;
 
 	protected CellSpace space;
 
 	/**
 	 * The particles.
 	 */
 	protected ParticleBox pbox;
 
 	/**
 	 * Species for boids.
 	 */
 	protected HashMap<String, BoidSpecies> boidSpecies;
 
 	/**
 	 * The main loop condition.
 	 */
 	protected boolean loop;
 
 	/**
 	 * Current step.
 	 */
 	protected int step;
 
 	/**
 	 * Random number generator.
 	 */
 	protected Random random;
 
 	/**
 	 * New context.
 	 */
 	public BoidGraph() {
 		super("boids-context");
 		setNodeFactory(new BoidFactory());
 
 		int maxParticlesPerCell = 10;
 
 		random = new Random();
 		randomSeed = random.nextLong();
 		random = new Random(randomSeed);
 		loop = false;
 		normalizeMode = true;
 		removeCaughtBoids = false;
 		storeForcesAttributes = false;
 		sleepTime = 20;
 		area = 1;
 		maxSteps = 0;
 		boidSpecies = new HashMap<String, BoidSpecies>();
 		space = new OctreeCellSpace(new Anchor(-area, -area, -area),
 				new Anchor(area, area, area));
 		pbox = new ParticleBox(maxParticlesPerCell, space, new BoidCellData());
 	}
 
 	public BoidGraph(String dgsConfig) throws IOException {
 		this();
 		loadDGSConfiguration(dgsConfig);
 	}
 
 	/**
 	 * Load configuration from a dgs file or resource. See 'configExample.dgs'
 	 * for an example of dgs configuration. The loader try to open the file
 	 * first. If file is not found, loader try to get the resource using
 	 * {@link java.lang.ClassLoader#getResourceAsStream(String)}.
 	 * 
 	 * @param dgs
 	 *            path to the DGS file or resource containing the configuration.
 	 * @throws IOException
 	 *             if something wrong happens with io.
 	 */
 	public void loadDGSConfiguration(String dgs) throws IOException {
 		InputStream in;
 
 		try {
 			in = new FileInputStream(dgs);
 		} catch (FileNotFoundException e) {
 			in = getClass().getResourceAsStream(dgs);
 
 			if (in == null)
 				throw e;
 		}
 
 		loadDGSConfiguration(in);
 		in.close();
 	}
 
 	/**
 	 * Load configuration from a dgs file. See 'configExample.dgs' for an
 	 * example of dgs configuration.
 	 * 
 	 * @param in
 	 * @throws IOException
 	 *             if something wrong happens with io.
 	 */
 	public void loadDGSConfiguration(InputStream in) throws IOException {
 		FileSourceDGS config = new FileSourceDGS();
 
 		config.addSink(this);
 		config.readAll(in);
 		config.removeSink(this);
 	}
 
 	// Access
 
 	public CellSpace getSpace() {
 		return space;
 	}
 
 	public double getArea() {
 		return area;
 	}
 
 	public void setArea(double area) {
 		this.area = area;
 
 		Anchor lo = new Anchor(-area, -area, -area);
 		Anchor hi = new Anchor(area, area, area);
 
 		this.space.resize(lo, hi);
 	}
 
 	public long getRandomSeed() {
 		return randomSeed;
 	}
 
 	public void setRandomSeed(long randomSeed) {
 		this.randomSeed = randomSeed;
 		this.random = new Random(randomSeed);
 	}
 
 	public int getSleepTime() {
 		return sleepTime;
 	}
 
 	public void setSleepTime(int sleepTime) {
 		this.sleepTime = sleepTime;
 	}
 
 	public boolean isCaughtBoidsRemoved() {
 		return removeCaughtBoids;
 	}
 
 	public void setRemoveCaughtBoids(boolean removeCaughtBoids) {
 		this.removeCaughtBoids = removeCaughtBoids;
 	}
 
 	public boolean isNormalizeMode() {
 		return normalizeMode;
 	}
 
 	public void setNormalizeMode(boolean on) {
 		normalizeMode = on;
 	}
 
 	public boolean isForcesAttributesStored() {
 		return storeForcesAttributes;
 	}
 
 	public void setStoreForcesAttributes(boolean storeForcesAttributes) {
 		this.storeForcesAttributes = storeForcesAttributes;
 	}
 
 	public int getMaxParticlesPerCell() {
 		return pbox.getNTree().getMaxParticlePerCell();
 	}
 
 	public int getMaxSteps() {
 		return maxSteps;
 	}
 	
 	public void setMaxSteps(int maxSteps) {
 		this.maxSteps = maxSteps;
 	}
 
 	/**
 	 * The current particle box.
 	 * 
 	 * @return The particle box.
 	 */
 	public ParticleBox getPbox() {
 		return pbox;
 	}
 
 	public BoidSpecies getOrCreateSpecies(String name) {
 		return getOrCreateSpecies(name, null);
 	}
 
 	public BoidSpecies getOrCreateSpecies(String name, String clazz) {
 		BoidSpecies species = boidSpecies.get(name);
 
 		if (species == null) {
 			if (clazz == null)
 				species = new BoidSpecies(this, name);
 			else {
 				try {
 					Class<?> classObj = Class.forName(clazz);
 					Object obj = classObj.getConstructor(BoidGraph.class,
 							String.class).newInstance(this, name);
 
 					if (obj instanceof BoidSpecies) {
 						species = (BoidSpecies) obj;
 					} else {
 						String msg = String.format(
 								"not a species class : '%s'", clazz);
 
 						throw new RuntimeException(msg);
 					}
 				} catch (ClassNotFoundException e) {
 					throw new RuntimeException(e);
 				} catch (InstantiationException e) {
 					throw new RuntimeException(e);
 				} catch (IllegalAccessException e) {
 					throw new RuntimeException(e);
 				} catch (IllegalArgumentException e) {
 					throw new RuntimeException(e);
 				} catch (SecurityException e) {
 					throw new RuntimeException(e);
 				} catch (InvocationTargetException e) {
 					throw new RuntimeException(e);
 				} catch (NoSuchMethodException e) {
 					throw new RuntimeException(e);
 				}
 			}
 
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
 
 	public BoidSpecies addDefaultSpecies() {
 		return getOrCreateSpecies("default");
 	}
 
 	public void deleteSpecies(String name) {
 		if (!name.equals("default"))
 			boidSpecies.remove(name);
 	}
 
 	// Commands
 
 	public void set(String paramName, String value)
 			throws IllegalArgumentException {
 		Parameter param = Parameter.valueOf(paramName.toUpperCase());
 		set(param, value);
 	}
 
 	public void set(Parameter param, String value) {
 		switch (param) {
 		case MAX_STEPS:
 			setMaxSteps(Integer.parseInt(value));
 			break;
 		case AREA:
 			setArea(Float.parseFloat(value));
 			break;
 		case SLEEP_TIME:
 			setSleepTime(Integer.parseInt(value));
 			break;
 		case STORE_FORCES_ATTRIBUTES:
 			setStoreForcesAttributes(Boolean.parseBoolean(value));
 			break;
 		case REMOVE_CAUGHT_BOIDS:
 			setRemoveCaughtBoids(Boolean.parseBoolean(value));
 			break;
 		case NORMALIZE_MODE:
 			setNormalizeMode(Boolean.parseBoolean(value));
 			break;
 		case RANDOM_SEED:
 			setRandomSeed(Long.parseLong(value));
 			break;
 		}
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
 
 	public void step() {
 		stepBegins(step + 1);
 	}
 
 	@Override
 	public void stepBegins(double step) {
 		super.stepBegins(step);
 		pbox.step();
 	}
 
 	public boolean isLooping() {
 		return loop;
 	}
 
 	@Override
 	public Viewer display(boolean autoLayout) {
 		Viewer v = super.display(autoLayout);
 		Camera cam = v.getDefaultView().getCamera();
 		cam.setGraphViewport(-area, -area, area, area);
 
 		return v;
 	}
 
 	protected void sleep(int milliseconds) {
 		try {
 			Thread.sleep(milliseconds);
 		} catch (InterruptedException e) {
 		}
 	}
 
 	@Override
 	protected void addNodeCallback(AbstractNode node) {
 		Boid b = (Boid) node;
 		b.getSpecies().register(b);
 
 		super.addNodeCallback(node);
 	}
 
 	@Override
 	protected void removeNodeCallback(AbstractNode node) {
 		Boid b = (Boid) node;
 		b.getSpecies().unregister(b);
 
 		super.removeNodeCallback(node);
 	}
 
 	@Override
 	protected void attributeChanged(String sourceId, long timeId,
 			String attribute, AttributeChangeEvent event, Object oldValue,
 			Object newValue) {
 		String key = attribute;
 
 		if (key.startsWith("boids.")) {
 			key = key.substring("boids.".length());
 
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
 					BoidSpecies species;
 
 					if (key == null && newValue != null
 							&& newValue instanceof String)
 						species = getOrCreateSpecies(name, (String) newValue);
 					else
 						species = getOrCreateSpecies(name);
 
 					if (key != null) {
 						try {
 							species.set(key, newValue == null ? null : newValue
 									.toString());
 						} catch (IllegalArgumentException e) {
 							System.err.printf("(WW) invalid parameter '%s'\n",
 									key);
 							System.err.printf("     ignoring it.\n");
 						}
 					}
 
 					break;
 				}
 			} else
 				set(key, newValue == null ? null : newValue.toString());
 		}
 
 		super.attributeChanged(sourceId, timeId, attribute, event, oldValue,
 				newValue);
 	}
 
 	private class BoidFactory implements NodeFactory<Boid> {
 		public Boid newInstance(String id, Graph graph) {
 			BoidSpecies species = null;
 
 			if (id.indexOf('.') != -1)
 				species = boidSpecies.get(id.substring(0, id.indexOf('.')));
 
 			if (species == null)
 				species = getDefaultSpecies();
 
 			Boid b = species.createBoid(id);
 			pbox.addParticle(b.getParticle());
 
 			return b;
 		}
 	}
 
 	public static void main(String... args) {
 		BoidGraph ctx = new BoidGraph();
 
 		try {
 			ctx.loadDGSConfiguration(BoidGraph.class
 					.getResourceAsStream("configExample.dgs"));
 		} catch (Exception e1) {
 			e1.printStackTrace();
 		}
 
 		ctx.display(false);
 		ctx.loop();
 	}
 }
