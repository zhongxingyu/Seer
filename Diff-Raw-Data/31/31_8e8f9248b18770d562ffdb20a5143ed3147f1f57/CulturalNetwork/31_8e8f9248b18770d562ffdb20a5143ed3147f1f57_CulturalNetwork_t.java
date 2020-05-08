 /**
  * 
  */
 package br.edu.cultural.network;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Random;
 
 /**
  * @author muggler
  * 
  */
 public class CulturalNetwork {
 	final Random rand = new Random();
 
 	// Network characteristics
 	public static final int MAX_DEGREE = 8;
 
 	public final int size;
 	public final int n_nodes;
 	public final int features;
 	public final int traits;
 	public final boolean periodicBoundary;
 	
	public int refresh_rate;
 
 	/** The network state representation */
 	public final int[][] states; // size^2 (~100kB)
 
 	/** Stores connectivity information */
 	final int[][] adj_matrix; // 8*size^2 (~100kB)
 
 	/** Gives the degree of each node */
 	public final int[] degree;
 
 	/** Stores all nodes that may interact */
 	public final List<Integer> interactiveNodes = new ArrayList<Integer>();
 
 	public final List<Integer> monitorNodes = new ArrayList<Integer>();
 
 	/** Stores the activity state of each node */
 	public final boolean[] is_node_active;
 
 	private int refreshAdjust = 10;
 
 	public CulturalNetwork(int size, int features, int traits, boolean periodicBoundary) {
 		this.size = size;
 		this.n_nodes = size * size;
 		this.features = features;
 		this.traits = traits;
 		this.periodicBoundary = periodicBoundary;
		this.refresh_rate = calc_update_rate();
 
 		this.states = new int[this.n_nodes][this.features];
 		this.adj_matrix = new int[this.n_nodes][MAX_DEGREE];
 		this.is_node_active = new boolean[this.n_nodes];
 		this.degree = new int[this.n_nodes];
 		this.init_adj_matrix(periodicBoundary);
 		this.random_starting_distribution();
 	}
 	
 	private int calc_update_rate() {
 		return (int) (refreshAdjust*this.features*(1+Math.sqrt(1+4*0.2*this.n_nodes/25.0))/2.0);
 	}
 
 	public CulturalNetwork(File f) throws IOException{
 		if(f.exists() && f.canRead()){
 			FileReader fr = new FileReader(f);
 			BufferedReader r = new BufferedReader(fr);
 			String[] params = r.readLine().split(":");
 			
 			this.size = Integer.parseInt(params[0]);
 			this.n_nodes = size * size;
 			this.features = Integer.parseInt(params[1]);
 			this.traits = Integer.parseInt(params[2]);
 			this.periodicBoundary = Boolean.parseBoolean(params[3]);
			this.refresh_rate = calc_update_rate();
 			
 			this.states = new int[this.n_nodes][this.features];
 			this.adj_matrix = new int[this.n_nodes][MAX_DEGREE];
 			this.is_node_active = new boolean[this.n_nodes];
 			this.degree = new int[this.n_nodes];
 			
 			for (int i = 0; i < this.n_nodes; i++) {
 				String[] stateStr = r.readLine().toString().split(":");
 				int[] state = new int [stateStr.length];
 				for (int j = 0; j < stateStr.length; j++) {
 					state[j] = Integer.parseInt(stateStr[j]);
 				}
 				System.arraycopy(state, 0, this.states[i], 0, this.features);
 			}
 			String line = r.readLine();
 			if (line != null && line.startsWith("monitor:")){
 				String[] monitor = line.substring(line.indexOf("[")+1, line.indexOf("]")).split(",");
 				for (String s : monitor) {
 					this.monitorNodes.add(Integer.parseInt(s));
 				}
 			}
 		}else {
 			throw new IOException("File does not exist or read not allowed");
 		}
 		this.init_adj_matrix(periodicBoundary);
 		this.initInteractionList(true);
 	}
 
 	public void init_adj_matrix(boolean periodicBoundary) {
 
 		for (int nd = 0; nd < this.n_nodes; nd++) {
 
 			degree[nd] = 0;
 			int i = nd / size;
 			int j = nd % size;
 			
 			if(periodicBoundary){
 				adj_matrix[nd][degree[nd]++] = j + (i == 0 ? this.size - 1 : i - 1) * size; // up
 				adj_matrix[nd][degree[nd]++] = (j == 0 ? this.size -1 : j - 1) + (i * size); // left
 				adj_matrix[nd][degree[nd]++] = j + (i == size - 1 ? 0 : i + 1) * size; // down
 				adj_matrix[nd][degree[nd]++] = (j == size - 1 ? 0 : j + 1) + (i * size); // right
 			}else {
 				if (i > 0) // not on line 0
 					adj_matrix[nd][degree[nd]++] = j + (i - 1) * size; // up
 				
 				if (j > 0) // not on column 0
 					adj_matrix[nd][degree[nd]++] = (j - 1) + (i * size); // left
 				
 				if (i < size - 1) // not on last line
 					adj_matrix[nd][degree[nd]++] = j + (i + 1) * size; // down
 				
 				if (j < size - 1) // not on last column
 					adj_matrix[nd][degree[nd]++] = (j + 1) + (i * size); // right
 			}
 		}
 	}
 
 	public void initInteractionList(boolean complete) {
 		this.interactiveNodes.clear();
 		for (int nd = 0; nd < this.n_nodes; nd++) {
 			if (complete || (is_node_active[nd] != false)) {
 				is_node_active[nd] = false;
 				for (int nbr_idx = 0; nbr_idx < degree[nd]; nbr_idx++) {
 					int similarity = 0;
 					int nbr = adj_matrix[nd][nbr_idx];
 					for (int f = 0; f < features; f++)
 						if (states[nd][f] == states[nbr][f])
 							similarity++;
 					if ((similarity > 0) && (similarity < features)) {
 						is_node_active[nd] = true;
 						interactiveNodes.add(nd);
 						break;
 					}
 				}
 			}
 		}
 	}
 	
 	public Map<Integer, Integer> count_cultures() {
 		Map<Integer, Integer> cultureSizes = new HashMap<Integer, Integer>();
 		for (int nd = 0; nd < n_nodes; nd++) {
 			cultureSizes.put(Arrays.hashCode(states[nd]), (cultureSizes
 					.get(Arrays.hashCode(states[nd])) == null ? 0
 					: cultureSizes.get(Arrays.hashCode(states[nd]))) + 1);
 		}
 		return cultureSizes;
 	}
 	
 	public Integer count_interactive_edges(){
 		Integer interactive_edges = 0;
 		for (int nd = 0; nd < this.n_nodes; nd++) {
 			is_node_active[nd] = false;
 			for (int nbr_idx = 0; nbr_idx < degree[nd]; nbr_idx++) {
 				int similarity = 0;
 				int nbr = adj_matrix[nd][nbr_idx];
 				for (int f = 0; f < features; f++)
 					if (states[nd][f] == states[nbr][f])
 						similarity++;
 				if ((similarity > 0) && (similarity < features)) {
 					interactive_edges++;
 				}
 			}
 		}
 		return interactive_edges;
 	}
 
 	public void update_representations(int nd) {
 
 		int[] nd_state = this.states[nd];
 
 		is_node_active[nd] = false;
 		interactiveNodes.remove(new Integer(nd));
 
 		for (int k0 = 0; k0 < degree[nd]; k0++) {
 
 			int nbr = this.adj_matrix[nd][k0];
 			int[] nbr_state = this.states[nbr];
 
 			is_node_active[nbr] = false;
 			interactiveNodes.remove(new Integer(nbr));
 			
 			if(is_node_active[nd] == false){
 				if (is_interaction_possible(nd_state, nbr_state)) {
 					is_node_active[nd] = true;
 					interactiveNodes.add(nd);
 					is_node_active[nbr] = true;
 					interactiveNodes.add(nbr);
 					continue;
 				}
 			}
 
 			for (int k1 = 0; k1 < degree[nbr] && is_node_active[nbr] == false; k1++) {
 				int nbr_nbr = this.adj_matrix[nbr][k1];
 				int[] nbr_nbr_state = this.states[nbr_nbr];
 
 				if (is_interaction_possible(nbr_state, nbr_nbr_state)) {
 					is_node_active[nbr] = true;
 					interactiveNodes.add(nbr);
 					break;
 				}
 			}
 		}
 	}
 
 	public static boolean is_interaction_possible(int[] ndState, int[] nbrState) {
 		boolean has_different_element = false;
 		boolean has_equal_element = false;
 
 		for (int i = 0; i < ndState.length; i++) {
 			if (!has_different_element && ndState[i] != nbrState[i]) {
 				has_different_element = true;
 			}
 			if (!has_equal_element && ndState[i] == nbrState[i]) {
 				has_equal_element = true;
 			}
 			if (has_different_element && has_equal_element)
 				return true;
 		}
 		return false;
 	}
 
 	public String interaction_list_to_string() {
 		StringBuilder sb = new StringBuilder("Interactive node count: "
 				+ interactiveNodes.size() + "\n");
 		sb.append("Interactive nodes:\n");
 		Object[] values = this.interactiveNodes.toArray();
 		Arrays.sort(values);
 		for (Object o : values) {
 			Integer nd = (Integer) o;
 			sb.append(String.format("Node %d/(%d, %d)\n", nd, nd / size, nd
 					% size));
 		}
 		return sb.toString();
 	}
 
 	public String network_state_to_string() {
 		StringBuilder sb = new StringBuilder("Network state:\n");
 		for (int nd = 0; nd < this.n_nodes; nd++) {
 			sb.append(String.format("Node %d/(%d, %d): ", nd, nd / size, nd
 					% size));
 			for (int f = 0; f < features; f++) {
 				sb.append(states[nd][f]);
 			}
 			sb.append("\n");
 		}
 		return sb.toString();
 	}
 
 	public String adjacency_matrix_to_string() {
 		StringBuilder sb = new StringBuilder();
 		sb.append("Adjacency matrix:\n");
 		for (int nd = 0; nd < this.n_nodes; nd++) {
 			sb.append(String.format("node %d/(%d,%d):\t", nd, nd / size, nd
 					% size));
 			for (int j = 0; j < degree[nd]; j++) {
 				int nbr = adj_matrix[nd][j];
 				sb.append(String.format("%d/(%d,%d)\t", nbr, nbr / size, nbr
 						% size));
 			}
 			sb.append("\n");
 		}
 		return sb.toString();
 	}
 
 	public void random_starting_distribution() {
 		for (int nd = 0; nd < this.n_nodes; nd++) {
 			states[nd] = State.random_node_state(features, traits);
 		}
 		this.initInteractionList(true);
 	}
 
 	public void bubble_random_starting_distribution(int bubble_radius,
 			int[] state) {
 
 		 for (int nd = 0; nd < this.n_nodes; nd++) {
 		 states[nd] = State.random_node_state(features, traits);
 		 }
 
 		int lin_centro = this.size / 2;
 		int col_centro = this.size / 2;
 		int no_centro = col_centro + lin_centro * this.size;
 
 		System.arraycopy(state, 0, this.states[no_centro], 0, this.features);
 
 		bubble_radius -= bubble_radius % 2;
 		int step_size = 1;
 		int lin_atual = lin_centro;
 		int col_atual = col_centro;
 		int no_atual = col_atual + lin_atual * this.size;
 
 		while (true) {
 
 			// cima
 			int to = lin_atual - step_size;
 			while (lin_atual > to) {
 				lin_atual--;
 				no_atual = col_atual + lin_atual * this.size;
 				System.arraycopy(state, 0, this.states[no_atual], 0,
 						this.features);
 			}
 
 			// esquerda
 			to = col_atual - step_size;
 			while (col_atual > to) {
 				col_atual--;
 				no_atual = col_atual + lin_atual * this.size;
 				System.arraycopy(state, 0, this.states[no_atual], 0,
 						this.features);
 			}
 
 			if (bubble_radius > 2 && step_size < bubble_radius - 1)
 				step_size++;
 
 			// baixo
 			to = lin_atual + step_size;
 			while (lin_atual < to) {
 				lin_atual++;
 				no_atual = col_atual + lin_atual * this.size;
 				System.arraycopy(state, 0, this.states[no_atual], 0,
 						this.features);
 			}
 
 			if (step_size == bubble_radius - 1)
 				break;
 
 			// direita
 			to = col_atual + step_size;
 			while (col_atual < to) {
 				col_atual++;
 				no_atual = col_atual + lin_atual * this.size;
 				System.arraycopy(state, 0, this.states[no_atual], 0,
 						this.features);
 			}
 			step_size++;
 		}
 		this.initInteractionList(true);
 	}
 
 	public void striped_starting_distribution() {
 		int[] state1 = new int[features];
 		int[] state2 = new int[features];
 		state1[0] = state2[0] = traits - 1;
 		for (int i = 1; i < state1.length; i++) {
 			state1[i] = 0;
 			state2[i] = traits - 1;
 		}
 		for (int nd = 0; nd < n_nodes; nd++) {
 			if (nd % 2 == 0) {
 				System.arraycopy(state1, 0, states[nd], 0, state1.length);
 			} else {
 				System.arraycopy(state2, 0, states[nd], 0, state2.length);
 			}
 		}
 		this.initInteractionList(true);
 	}
 
 	public void homogeneous_distribution() {
 		int[] randst = State.random_node_state(features, traits);
 		for (int nd = 0; nd < n_nodes; nd++) {
 			System.arraycopy(randst, 0, states[nd], 0, randst.length);
 		}
 		this.initInteractionList(true);
 	}
 
 	public boolean is_state_valid(int[] state) {
 		for (int i = 0; i < features; i++) {
 			if (state[i] < 0 || state[i] >= traits)
 				return false;
 		}
 		return true;
 	}
 
 	public Integer random_interactive_node() {
 		Integer idx = rand.nextInt(this.interactiveNodes.size());
 		return this.interactiveNodes.get(idx);
 	}
 
 	public List<Integer> interactive_nodes() {
 		return Collections.unmodifiableList(interactiveNodes);
 	}
 
 	public int degree(int node) {
 		return degree[node];
 	}
 
 	public int node_neighbor(int node, int nbrIdx) {
 		if (nbrIdx > degree[node])
 			return -1;
 		return adj_matrix[node][nbrIdx];
 	}
 	
 	public void save_to_file(File f) throws IOException{
 		if(!f.exists()) f.createNewFile();
 		if(f.canWrite()){
 			FileWriter fw = new FileWriter(f);
 			fw.write(String.valueOf(this.size));
 			fw.write(':');
 			fw.write(String.valueOf(this.features));
 			fw.write(':');
 			fw.write(String.valueOf(this.traits));
 			fw.write(':');
 			fw.write(String.valueOf(this.periodicBoundary));
 			fw.write('\n');
 			for (int i = 0; i < states.length; i++) {
 				int [] state = states[i];
 				for (int j = 0; j < state.length; j++) {
 					fw.write(String.valueOf(state[j]));
 					if(j != state.length -1) fw.write(':');
 				}
 				fw.write('\n');
 			}
 			if(!monitorNodes.isEmpty()) fw.write("monitor:[");
 			for (Integer node : monitorNodes) {
 				fw.write(Integer.toString(node));
 				fw.write(monitorNodes.indexOf(node) == monitorNodes.size() -1 ? ']' : ',');
 			}
 			fw.close();
 		}
 	}
 
 	public void setRefreshAdjust(int refreshAdjust) {
 		this.refreshAdjust  = refreshAdjust;
		this.refresh_rate = calc_update_rate();
 	}
 }
