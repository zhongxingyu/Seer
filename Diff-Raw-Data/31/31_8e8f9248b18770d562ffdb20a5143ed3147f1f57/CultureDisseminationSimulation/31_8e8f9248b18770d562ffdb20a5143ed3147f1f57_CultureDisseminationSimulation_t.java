 package br.edu.cultural.simulation;
 
 import java.lang.reflect.InvocationTargetException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.Random;
 
 import br.edu.cultural.network.CulturalNetwork;
 import br.edu.cultural.plot.Plot;
 
 public abstract class CultureDisseminationSimulation implements Runnable {
 	public enum SimulationState {
 		RUNNING, STOPPED, FINISHED
 	}
 
 	public CulturalNetwork nw;
 	protected final List<Plot<?, ?>> plots = new ArrayList<Plot<?, ?>>();
 	protected final List<Integer> defer_node_update = new ArrayList<Integer>();
 
 	protected final Random rand = new Random();
 	protected int speed = 100;
 	public SimulationState state = SimulationState.STOPPED;
 
 	// Statistics
 	protected long sim_start_ms;
 	protected long sim_finish_ms = -1;
 	protected int iterations = 0;
 	protected int interactions = 0;
 
 	protected List<SimulationEventListener> listeners = Collections
 			.synchronizedList(new ArrayList<SimulationEventListener>());
 	private boolean defer_update;
 
 	public void run() {
 		this.iterations = 0;
 		this.sim_start_ms = System.currentTimeMillis();
 
 		for (SimulationEventListener lis : listeners) {
 			lis.started();
 		}
 
 		while (!(this.state == SimulationState.FINISHED)) {
 			pause();
 			throttle();
 			defer_run();
 			if (iterations % 100000 == 0) {
 				nw.initInteractionList(false);
 			}
 		}
 		this.sim_finish_ms = System.currentTimeMillis();
 
 		for (SimulationEventListener lis : listeners) {
 			lis.finished();
 		}
 	}
 
 	protected abstract void defer_run();
 
 	public void print_execution_statistics() {
 		System.out.print(execution_statistics_string());
 	}
 
 	protected void interacted(int node) {
 		if (defer_update) {
 			deferred_representation_update(node);
 		} else {
 			nw.update_representations(node);
 		}
 		for (SimulationEventListener lis : listeners) {
 			lis.interaction(node / nw.size, node % nw.size);
 		}
 		interactions++;
 	}
 
 	protected void deferred_representation_update(Integer node) {
 		nw.is_node_active[node] = true;
 		for (int nbrIdx = 0; nbrIdx < nw.degree[node]; nbrIdx++) {
 			nw.is_node_active[nw.node_neighbor(node, nbrIdx)] = true;
 		}
		if (interactions % nw.refresh_rate == 0) {
 				nw.initInteractionList(false);
 		}
 	}
 	
 	public String execution_statistics_string() {
 		double iterations_per_second = iterations() / elapsed_time_in_seconds();
 		double interactions_per_second = interactions()
 				/ elapsed_time_in_seconds();
 		StringBuilder sb = new StringBuilder();
 		sb.append("\n");
 		sb.append(String.format(
 				"Simulation: %d iterations elapsed in %f seconds.",
 				iterations(), elapsed_time_in_seconds()));
 		sb.append("\n");
 		sb.append(String.format(
 				"Average execution speed: %f iterations/second",
 				iterations_per_second));
 		sb.append("\n");
 		sb.append(String.format("Time: %d epochs", epoch()));
 		sb.append("\n");
 		sb.append(String.format("%d node interactions.", interactions()));
 		sb.append("\n");
 		sb.append(String.format(
 				"Average node interaction speed: %f interactions/second",
 				interactions_per_second));
 		sb.append("\n");
 		return sb.toString();
 	}
 
 	/**
 	 * Performs a single step of the simulation
 	 */
 	public abstract void simulation_step();
 
 	public void stop() {
 		this.state = SimulationState.STOPPED;
 	}
 
 	public void start() {
 		this.state = SimulationState.RUNNING;
 	}
 
 	public void quit() {
 		this.state = SimulationState.FINISHED;
 	}
 
 	public void setSpeed(int speed) {
 		this.speed = speed;
 	}
 
 	public void setDefer_update(boolean deferUpdate) {
 		defer_update = deferUpdate;
 	}
 
 	public void addPlot(Plot<?, ?> p) {
 		if (!plots.contains(p))
 			plots.add(p);
 	}
 
 	public void removePlot(Plot<?, ?> p) {
 		plots.remove(p);
 	}
 
 	private void pause() {
 		while (this.state == SimulationState.STOPPED) {
 			try {
 				Thread.sleep(33);
 			} catch (InterruptedException e) {
 			}
 		}
 	}
 
 	private void throttle() {
 		if (this.speed < 100) {
 			try {
 				Thread.sleep((long) ((100 - speed) * 0.1));
 			} catch (InterruptedException e) {
 			}
 		}
 	}
 
 	public boolean toggleMonitor(int nodeClicked) {
 		if (this.nw.monitorNodes.contains(nodeClicked)) {
 			this.nw.monitorNodes.remove(new Integer(nodeClicked));
 			return false;
 		} else {
 			this.nw.monitorNodes.add(nodeClicked);
 			return true;
 		}
 	}
 
 	public long sim_start_ms() {
 		return sim_start_ms;
 	}
 
 	public long sim_finish_ms() {
 		return sim_finish_ms;
 	}
 
 	public long elapsed_time() {
 		return (sim_finish_ms == -1 ? System.currentTimeMillis()
 				: sim_finish_ms)
 				- sim_start_ms;
 	}
 
 	public double elapsed_time_in_seconds() {
 		return elapsed_time() / 1000.0;
 	}
 
 	public int iterations() {
 		return iterations;
 	}
 
 	public void addListener(SimulationEventListener lis) {
 		if (!listeners.contains(lis))
 			listeners.add(lis);
 	}
 
 	public void removeListener(SimulationEventListener lis) {
 		listeners.remove(lis);
 	}
 
 	public static interface SimulationEventListener {
 		public void started();
 
 		public void iteration();
 
 		public void interaction(int i, int j);
 
 		public void epoch();
 
 		public void finished();
 	}
 
 	public static class SimulationEventAdapter implements
 			SimulationEventListener {
 		public void started() {
 		}
 
 		public void iteration() {
 		}
 
 		public void interaction(int i, int j) {
 		}
 
 		public void epoch() {
 		}
 
 		public void finished() {
 		}
 	}
 
 	public int epoch() {
 		return this.iterations / this.nw.n_nodes;
 	}
 
 	public Integer interactions() {
 		return interactions;
 	}
 
 	public CultureDisseminationSimulation(CulturalNetwork nw) {
 		this.nw = nw;
 	}
 
 	public static CultureDisseminationSimulation factory(
 			Class<? extends CultureDisseminationSimulation> clazz,
 			CulturalNetwork nw) {
 		try {
 			return clazz.getConstructor(CulturalNetwork.class).newInstance(nw);
 		} catch (IllegalArgumentException e) {
 			e.printStackTrace();
 			return null;
 		} catch (SecurityException e) {
 			e.printStackTrace();
 			return null;
 		} catch (InstantiationException e) {
 			e.printStackTrace();
 			return null;
 		} catch (IllegalAccessException e) {
 			e.printStackTrace();
 			return null;
 		} catch (InvocationTargetException e) {
 			e.printStackTrace();
 			return null;
 		} catch (NoSuchMethodException e) {
 			e.printStackTrace();
 			return null;
 		}
 	}
 
 }
