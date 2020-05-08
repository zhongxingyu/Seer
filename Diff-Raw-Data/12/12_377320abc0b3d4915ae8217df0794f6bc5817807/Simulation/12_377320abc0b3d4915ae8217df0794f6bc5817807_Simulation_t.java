 package org.andrill.conop.search;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.text.DecimalFormat;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import java.util.concurrent.Callable;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.Future;
 import java.util.concurrent.ThreadPoolExecutor;
 
 import org.andrill.conop.search.constraints.ConstraintChecker;
 import org.andrill.conop.search.constraints.EventChecker;
 import org.andrill.conop.search.constraints.NullChecker;
 import org.andrill.conop.search.listeners.ConsoleProgressListener;
 import org.andrill.conop.search.listeners.Listener;
 import org.andrill.conop.search.listeners.Listener.Mode;
 import org.andrill.conop.search.listeners.SnapshotListener;
 import org.andrill.conop.search.listeners.StoppingListener;
 import org.andrill.conop.search.mutators.ConstrainedMutator;
 import org.andrill.conop.search.mutators.MutationStrategy;
 import org.andrill.conop.search.mutators.RandomMutator;
 import org.andrill.conop.search.objectives.MatrixPenalty;
 import org.andrill.conop.search.objectives.ObjectiveFunction;
 import org.andrill.conop.search.objectives.PlacementPenalty;
 import org.andrill.conop.search.schedules.CoolingSchedule;
 import org.andrill.conop.search.schedules.ExponentialSchedule;
 import org.andrill.conop.search.schedules.LinearSchedule;
 
 import com.google.common.collect.Lists;
 import com.google.common.io.Closeables;
 import com.google.common.util.concurrent.MoreExecutors;
 
 /**
  * Reads a simulation configuration from a properties file.
  * 
  * @author Josh Reed (jareed@andrill.org)
  */
 public class Simulation {
 	private static final DecimalFormat D = new DecimalFormat("0.00");
 
 	/**
 	 * Runs a standard simulation.
 	 * 
 	 * @param args
 	 *            the arguments.
 	 */
 	public static void main(final String[] args) {
 		// load the simulation configuration
 		Simulation config = new Simulation(new File(args[0]));
 		Run run = config.getRun();
 
 		// find the optimal placement
 		long start = System.currentTimeMillis();
 		List<Listener> listeners = Lists.newArrayList(config.getListeners());
 
 		try {
 			Solution solution = runSimulation(config, run, Solution.initial(run), listeners, Mode.TUI);
 			long elapsed = (System.currentTimeMillis() - start) / 60000;
 			System.out.println("Elapsed time: " + elapsed + " minutes.  Final score: " + D.format(solution.getScore()));
 		} catch (RuntimeException e) {
 			Throwable cause = e;
 			while (cause.getCause() != null) {
 				cause = cause.getCause();
 			}
 			long elapsed = (System.currentTimeMillis() - start) / 60000;
 			System.out.println("Simulation aborted after " + elapsed + " minutes: " + cause.getMessage());
 		}
 	}
 
 	/**
 	 * Runs a simulation.
 	 * 
 	 * @param simulation
 	 *            the simulation.
 	 * @param run
 	 *            the run.
 	 * @param initial
 	 *            the initial solution.
 	 * @param listeners
 	 *            the listeners.
 	 * @return the solution.
 	 */
 	public static Solution runSimulation(final Simulation simulation, final Run run, final Solution initial,
 			final List<Listener> listeners, final Mode mode) {
 		int serial = simulation.getSerialRunCount();
 		int parallel = simulation.getParallelProcessCount();
 		ExecutorService pool = MoreExecutors.getExitingExecutorService((ThreadPoolExecutor) Executors
 				.newFixedThreadPool(parallel));
 
 		Solution solution = initial;
 		for (int i = 0; i < serial; i++) {
 			if (serial > 1) {
 				System.out.println("Starting iteration " + (i + 1) + "/" + serial);
 			}
 			List<Future<Solution>> tasks = Lists.newArrayList();
 			final Solution next = new Solution(solution.getRun(), solution.getEvents());
 			if (parallel > 1) {
 				System.out.println("Starting a swarm of " + parallel + " CONOP processes");
 			}
 			for (int j = 0; j < parallel; j++) {
 				// create our CONOP process
 				final CONOP conop = new CONOP(simulation.getConstraints(), simulation.getMutator(),
 						simulation.getObjectiveFunction(), simulation.getSchedule());
 
 				// add our listeners
 				if (j == 0) {
 					for (Listener l : listeners) {
 						conop.addListener(l);
 					}
 				}
 
 				// start a parallel process
 				tasks.add(pool.submit(new Callable<Solution>() {
 					public Solution call() throws AbortedException {
 						return conop.solve(run, new Solution(run, next.getEvents()));
 					}
 				}));
 			}
 
 			// get our best solution from the parallel tasks
 			Solution best = null;
			Exception cause = null;
 			for (Future<Solution> f : tasks) {
 				try {
 					Solution s = f.get();
 					if ((best == null) || (s.getScore() < best.getScore())) {
 						best = s;
 					}
				} catch (Exception e) {
 					cause = e;
 				}
 			}
 			if (best == null) {
 				throw new RuntimeException("All runs aborted", cause);
 			}
 			solution = best;
 		}
 
 		// run the endgame simulation
 		File endgame = simulation.getEndgame();
 		if (endgame == null) {
 			return solution;
 		} else {
 			System.out.println("Starting endgame scenario: " + endgame.getName());
 			return runSimulation(new Simulation(endgame), run, solution, listeners, mode);
 		}
 	}
 
 	protected ConstraintChecker constraints;
 	protected final File directory;
 	protected List<Listener> listeners;
 	protected MutationStrategy mutator;
 	protected ObjectiveFunction objective;
 	protected final Properties properties;
 	protected Run run;
 	protected CoolingSchedule schedule;
 
 	/**
 	 * Create a new Simulation.
 	 * 
 	 * @param file
 	 *            the file.
 	 */
 	public Simulation(final File file) {
 		properties = new Properties();
 		directory = file.getParentFile();
 		FileInputStream fis = null;
 		try {
 			fis = new FileInputStream(file);
 			properties.load(fis);
 		} catch (FileNotFoundException e) {
 			throw new RuntimeException(e);
 		} catch (IOException e) {
 			throw new RuntimeException(e);
 		} finally {
 			Closeables.closeQuietly(fis);
 		}
 	}
 
 	/**
 	 * Gets the configured {@link ConstraintChecker}.
 	 * 
 	 * <pre>
 	 * Key: constraints 
 	 * Values: 
 	 * 		null  - {@link NullChecker} (default) 
 	 * 		event - {@link EventChecker}
 	 * </pre>
 	 * 
 	 * @return the configured ConstraintChecker.
 	 */
 	@SuppressWarnings("serial")
 	public ConstraintChecker getConstraints() {
 		if (constraints == null) {
 			constraints = lookup(properties.getProperty("constraints", "default"), ConstraintChecker.class,
 					new HashMap<String, ConstraintChecker>() {
 						{
 							put("null", new NullChecker());
 							put("event", new EventChecker());
 							put("default", new NullChecker());
 						}
 					});
 		}
 		return constraints;
 	}
 
 	/**
 	 * Gets the endgame simulation.
 	 * 
 	 * @return the endgame simulation.
 	 */
 	public File getEndgame() {
 		String endgame = properties.getProperty("endgame");
 		if (endgame != null) {
 			File f = new File(directory, endgame);
 			if (f.exists()) {
 				return f;
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * Get the list of listeners.
 	 * 
 	 * @return the list of listeners.
 	 */
 	@SuppressWarnings("serial")
 	public List<Listener> getListeners() {
 		if (listeners == null) {
 			listeners = Lists.newArrayList();
 			Map<String, Listener> map = new HashMap<String, Listener>() {
 				{
 					put("console", new ConsoleProgressListener());
 					put("snapshot", new SnapshotListener());
 					put("stopping", new StoppingListener());
 				}
 			};
 
 			// lookup our listeners
 			for (String name : properties.getProperty("listeners", "").split(",")) {
 				Listener l = lookup(name, Listener.class, map);
 				if (l != null) {
 					listeners.add(l);
 				}
 			}
 		}
 		return listeners;
 	}
 
 	/**
 	 * Gets the configured {@link MutationStrategy}.
 	 * 
 	 * <pre>
 	 * Key: mutator
 	 * Values:
 	 * 		random      - {@link RandomMutator} (default)
 	 * 		constrained - {@link ConstrainedMutator}
 	 * </pre>
 	 * 
 	 * @return the configured MutationStrategy.
 	 */
 	@SuppressWarnings("serial")
 	public MutationStrategy getMutator() {
 		if (mutator == null) {
 			mutator = lookup(properties.getProperty("mutator", "default"), MutationStrategy.class,
 					new HashMap<String, MutationStrategy>() {
 						{
 							put("random", new RandomMutator());
 							put("constrained", new ConstrainedMutator());
 							put("default", new RandomMutator());
 						}
 					});
 		}
 		return mutator;
 	}
 
 	/**
 	 * Gets the configured {@link ObjectiveFunction}.
 	 * 
 	 * <pre>
 	 * Key: score
 	 * Values:
 	 * 		placement - {@link PlacementPenalty} (default)
 	 * 		matrix    - {@link MatrixPenalty}
 	 * </pre>
 	 * 
 	 * @return the configured ObjectiveFunction.
 	 */
 	@SuppressWarnings("serial")
 	public ObjectiveFunction getObjectiveFunction() {
 		if (objective == null) {
 			objective = lookup(properties.getProperty("objective", "default"), ObjectiveFunction.class,
 					new HashMap<String, ObjectiveFunction>() {
 						{
 							put("placement", new PlacementPenalty());
 							put("matrix", new MatrixPenalty());
 							put("default", new PlacementPenalty());
 						}
 					});
 		}
 		return objective;
 	}
 
 	/**
 	 * Gets the parallel process count.
 	 * 
 	 * @return the parallel count.
 	 */
 	public int getParallelProcessCount() {
 		return Integer.parseInt(properties.getProperty("swarm", "1"));
 	}
 
 	/**
 	 * Gets the run for this simulation.
 	 * 
 	 * @return the run.
 	 */
 	public Run getRun() {
 		if (run == null) {
 			boolean overrideWeights = !Boolean.getBoolean(properties.getProperty("weights", "true"));
 			File runDir = new File(directory, properties.getProperty("data", "."));
 			String sectionFile = properties.getProperty("data.sectionFile", "sections.sct");
 			String eventFile = properties.getProperty("data.eventFile", "events.evt");
 			String loadFile = properties.getProperty("data.loadFile", "loadfile.dat");
 
 			run = Run.loadCONOP9Run(new File(runDir, sectionFile), new File(runDir, eventFile), new File(runDir,
 					loadFile), overrideWeights);
 		}
 		return run;
 	}
 
 	/**
 	 * Gets the configured {@link CoolingSchedule}.
 	 * 
 	 * <pre>
 	 * Key: schedule
 	 * Values:
 	 * 		exponential - {@link ExponentialSchedule} (default)
 	 * 		linear      - {@link LinearSchedule}
 	 * 
 	 * Additional Keys:
 	 * 		schedule.initial    - initial temperature (double)
 	 * 		schedule.delta      - temperature delta (double)
 	 * 		schedule.stepsPer   - steps per temperature (long)
 	 * 		schedule.noProgress - stop after steps with no progress (long)
 	 * </pre>
 	 * 
 	 * @return the configured CoolingSchedule.
 	 */
 	@SuppressWarnings("serial")
 	public CoolingSchedule getSchedule() {
 		if (schedule == null) {
 			schedule = lookup(properties.getProperty("schedule", "default"), CoolingSchedule.class,
 					new HashMap<String, CoolingSchedule>() {
 						{
 							put("exponential", new ExponentialSchedule());
 							put("linear", new LinearSchedule());
 							put("default", new ExponentialSchedule());
 						}
 					});
 		}
 		return schedule;
 	}
 
 	/**
 	 * Gets the serial run count.
 	 * 
 	 * @return the serial count.
 	 */
 	public int getSerialRunCount() {
 		return Integer.parseInt(properties.getProperty("series", "1"));
 	}
 
 	protected <E> E instantiate(final String name, final Class<E> type) {
 		Class<?> clazz;
 		try {
 			clazz = Class.forName(name);
 			if (type.isAssignableFrom(clazz)) {
 				return type.cast(clazz.newInstance());
 			} else {
 				System.err.println("Class " + name + " does not implement the " + type.getName() + " interface");
 			}
 		} catch (ClassNotFoundException e) {
 			System.err.println("Unable to find class " + name + ".  Check your classpath.");
 		} catch (InstantiationException e) {
 			System.err.println("Unable to find class " + name + ".  Check your classpath.");
 		} catch (IllegalAccessException e) {
 			System.err.println("Unable to find class " + name + ".  Check your classpath.");
 		}
 		return null;
 	}
 
 	protected <E extends Configurable> E lookup(final String key, final Class<E> type, final Map<String, E> map) {
 		String name = key.trim();
 		if ("".equals(name)) {
 			return null;
 		}
 		E instance = null;
 		if (map.containsKey(name)) {
 			instance = map.get(name);
 		} else {
 			instance = instantiate(name, type);
 		}
 		if ((instance == null) && map.containsKey("default")) {
 			instance = map.get("default");
 			System.err.println("Unknown " + type.getName() + " '" + name + "', defaulting to " + instance);
 		}
 		if (instance != null) {
 			instance.configure(properties);
 		}
 		return instance;
 	}
 }
