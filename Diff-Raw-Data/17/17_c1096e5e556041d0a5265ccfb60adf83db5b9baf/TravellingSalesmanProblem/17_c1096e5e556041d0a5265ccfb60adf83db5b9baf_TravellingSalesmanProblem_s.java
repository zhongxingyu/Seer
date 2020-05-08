 package problems;
 
 import java.io.BufferedReader;
 import java.io.DataInputStream;
 import java.io.FileInputStream;
 import java.io.InputStreamReader;
 import java.lang.reflect.Array;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.PriorityQueue;
 import java.util.Random;
 
 public class TravellingSalesmanProblem extends SearchProblem {
 	private int size;
 	private float[][] dist;
 	private int initial;
 
 	public TravellingSalesmanProblem(String problemPath) {
 		loadfromFile(problemPath);
 	}
 
 	public TravellingSalesmanProblem(boolean random, int size) {
 		generateProblem(random, size);
 	}
 
 	private void generateProblem(boolean random, int size) {
 		this.size = size;
 		dist = new float[size][size];
 		for (int i = 0; i < size; i++) {
 			dist[i][i] = 0;
 		}
 		Random generator = null;
 		if (random) {
 			generator = new Random();
 		} else {
 			generator = new Random(4713);
 		}
 		for (int i = 0; i < size - 1; i++) {
 			for (int j = i + 1; j < size; j++) {
 				dist[i][j] = generator.nextInt(211);
 				dist[j][i] = dist[i][j];
 			}
 		}
		for (int i = 0; i < size; i++) {
			System.out.println((Arrays.toString(dist[i])));
		}
 	}
 
 	// reads in and sets size and dist[][] from a file
 	// first line of file : #cities
 	// following (#cities)^2 lines : distances
 	// cities with no connection should have a distance of -1
 	private void loadfromFile(String problemPath) {
 		try {
 			DataInputStream in = new DataInputStream(new FileInputStream(
 					problemPath));
 			BufferedReader br = new BufferedReader(new InputStreamReader(in));
 			String line;
 			boolean firstLine = true;
 			int i = 0;
 			int j = 0;
 			while ((line = br.readLine()) != null) {
 				if (line.length() > 0
 						&& !(line.startsWith("//") || line.startsWith(" ") || line
 								.startsWith(System
 										.getProperty("line.separator")))) {
 					if (firstLine) {
 						size = Integer.parseInt(line);
 						dist = new float[size][size];
 						firstLine = false;
 					} else {
 						if (j >= size) {
 							j = 0;
 							i++;
 						}
 						dist[i][j] = Float.parseFloat(line);
 						j++;
 					}
 				}
 			}
 			in.close();
 		} catch (Exception e) {
 			System.out.println("Error reading problemfile");
 			e.printStackTrace();
 			System.exit(1);
 		}
 	}
 
 	@Override
 	public State init() {
 		initial = 0;
 		return new TSPState(0);
 	}
 
 	@Override
 	public boolean isGoal(State state) {
 		if (((TSPState) state).getVisited().size() == size + 1) {
 			return true;
 		}
 		return false;
 	}
 
 	@Override
 	public ArrayList<ActionStatePair> succ(State state) {
 		ArrayList<ActionStatePair> successors = new ArrayList<ActionStatePair>();
 		int current = ((TSPState) state).getCurrent();
 		ArrayList visited = ((TSPState) state).getVisited();
 		for (int j = 0; j < size; j++) {
 			if (dist[current][j] > 0) {
 				if (allowed(visited, j)) {
 					TSPAction action = new TSPAction(current, j);
 					ArrayList<Integer> newVisited = new ArrayList<Integer>(
 							visited);
 					newVisited.add(Integer.valueOf(j));
 					TSPState newState = new TSPState(j, newVisited);
 					successors.add(new ActionStatePair(action, newState));
 				}
 			}
 		}
 		return successors;
 	}
 
 	private boolean allowed(ArrayList visited, int destination) {
 		if (visited.contains(Integer.valueOf(destination))) {
 			if (destination == initial && visited.size() == size) {
 				return true;
 			}
 			return false;
 		}
 		return true;
 	}
 
 	@Override
 	public float cost(Action action) {
 		return dist[((TSPAction) action).from][((TSPAction) action).to];
 	}
 
 	public float getH(State cState) {
 		float heuri = 0;
 		TSPState TSPcState = (TSPState) cState;
 		ArrayList visited = ((TSPState) cState).getVisited();
 		int current = TSPcState.getCurrent();
 		if (visited.size() == size + 1) { // If State is Goal
 			return heuri;
 		} else if (visited.size() == size) { // If every City is visited
 			return dist[current][initial];
 		} else { // Else
 			float minInit = Float.MAX_VALUE;
 			float minNext = Float.MAX_VALUE;
 			ArrayList<Integer> notVisited = new ArrayList<Integer>();
 
 			for (int i = 0; i < size; i++) {
 				if (!visited.contains(i)) {
 					notVisited.add(i);
 					float distanceInit = dist[initial][i];
 					
 					if (minInit > distanceInit && distanceInit > 0) {
 						minInit = distanceInit;
 					}
 					float distanceNext = dist[current][i];
 					if (minNext > distanceNext && distanceNext > 0) {
 						minNext = distanceNext;
 					}
 				}
 			}
 			//float prim = prim(notVisited, notVisited.size());
 			float prim = primAlt(notVisited, notVisited.size());
 			heuri = minInit + minNext + prim;
 			return heuri;
 		}
 	}
 
 	private float primAlt(ArrayList<Integer> Y, int size2) {
 		float cost = 0;
 		ArrayList<Integer> V = new ArrayList<Integer>();
 		ArrayList<Integer> notV = new ArrayList<Integer>(Y); // Y\V
 		V.add(Y.get(0));
 		notV.remove(Y.get(0));
 		while (V.size() < Y.size()) {
 			//search minimal edge from V to notV = Y\V
 			float minimalEdge=Float.MAX_VALUE;
 			int to=-1;
 			for (int i = 0; i < V.size(); i++) {
 				for (int j = 0; j < notV.size(); j++) {
 					float d = dist[V.get(i)][notV.get(j)];
 					if (minimalEdge > d && d>0) {
 						minimalEdge = d;
 						to=j;
 					}
 				}
 			}
 			//insert new node in V
 			V.add(notV.get(to));
 			//remove node from notV
 			notV.remove(to);
 			cost=cost+minimalEdge;
 		}
 		return cost;
 	}
 
 	private float prim(ArrayList<Integer> notVisited, int size) {
 		if (notVisited.size() <= 2) {
 			if (notVisited.size() == 1) {
 				return 0;
 			} else {
 				return dist[notVisited.get(0)][notVisited.get(1)];
 			}
 		}
 		float cost = 0;
 		boolean[] visited = new boolean[size];
 		float[] dists = new float[size];
 
 		for (int i = 0; i < size; i++) {
 			dists[i] = Integer.MAX_VALUE;
 		}
 		dists[0] = 0;
 		visited[0] = true;
 
 		for (int i = 0; i < size; i++) {
 			float x = Float.MAX_VALUE;
 			int y = -1;
 			for (int j = 0; j < size; j++) {
 				if (!visited[j] && dists[i] < dist[i][j]) {
 					dists[j] = dist[notVisited.get(i)][notVisited.get(j)];
 					if (dists[j] < x) {
 						x = dists[j];
 						y = j;
 					}
 				}
 			}
 			if (y > 0) {
 				visited[y] = true;
 			}
 
 		}
 
 		for (float i : dists) {
 			cost += i;
 		}
 
 		return cost;
 	}
 }
