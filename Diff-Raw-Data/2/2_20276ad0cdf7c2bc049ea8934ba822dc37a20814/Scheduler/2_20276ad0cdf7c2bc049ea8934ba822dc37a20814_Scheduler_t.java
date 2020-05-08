 import java.io.BufferedReader;
 import java.io.IOException;
 import java.nio.charset.Charset;
 import java.nio.file.Files;
 import java.nio.file.Path;
 import java.nio.file.Paths;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 public class Scheduler {
 	private Charset cs = Charset.forName("UTF-8");
 	private List<Operation> ops; // all operations in order of appearance
 	private Map<String, List<Operation>> opsMap; // map from date to operations on it
 	// cache
 	private List<Conflict> conflicts;
 
 	public Scheduler(Path pathSchedule) {
 		try (BufferedReader br = Files.newBufferedReader(pathSchedule, cs)) {
 			ops = new ArrayList<>();
 			opsMap = new HashMap<>();
 			Pattern pattern = Pattern.compile("(r|w)(\\d)(\\w+)");
 			int lineNo = 0;
 			while (true) {
 				lineNo++;
 				String line = br.readLine();
 				if (line == null) break;
 				Matcher matcher = pattern.matcher(line);
 				if (!matcher.matches()) {
 					System.err.println("Line " + lineNo + " (" + line + ") doesn't conform to the pattern " + pattern);
 					System.exit(0);
 				}
 				Operation op = new Operation(matcher.group(1), Integer.valueOf(matcher.group(2)), matcher.group(3));
 				ops.add(op);
 				List<Operation> opsd = opsMap.get(op.d);
 				if (opsd == null) opsd = new ArrayList<>();
 				opsd.add(op);
 				opsMap.put(op.d, opsd);
 			}
 		} catch (IOException e) {
 			System.err.println("Error reading from " + pathSchedule);
 			e.printStackTrace();
 		}
 	}
 
 	// 1
 	List<Conflict> getConflicts() {
 		if (conflicts != null) return conflicts;
 		conflicts = new ArrayList<>();
 		for (Entry<String, List<Operation>> e : opsMap.entrySet()) { // ops for every date
 			List<Operation> opsd = e.getValue();
 			for (int i = 0; i < opsd.size(); i++) { // check for every op
 				Operation op1 = opsd.get(i);
 				for (int j = i; j < opsd.size(); j++) { // if following ops...
 					Operation op2 = opsd.get(j);
 					if (op1.t == op2.t) continue; // not the same transaction
 					if (op1.rw == Operation.RW.R && op2.rw == Operation.RW.R) continue; // R-R is save
 					// all other combinations involving a write *can* result in a conflict
 					// read after write as in the example on the sheet is always a conflict
 					conflicts.add(new Conflict(op1, op2));
 				}
 
 			}
 		}
 		return conflicts;
 	}
 
 	void printConflicts() {
 		System.out.println("-- Conflicts:");
 		for (Conflict c : getConflicts()) {
 			System.out.println(c);
 		}
 		System.out.println();
 	}
 
 	// 2
 	List<String> getSerializabilityGraph() {
 //		StringBuffer sb = new StringBuffer(); // use Lists instead to avoid platform-specific line-ending and to check for duplicates
 		List<String> r = new ArrayList<>();
 		r.add("digraph SerializabilityGraph {");
 		List<Conflict> conflicts = getConflicts();
 		for (Conflict c : conflicts) {
			String s = "\t" + c.a.t + " -> " + c.b.t + ";";
 			if (r.contains(s)) continue; // only one arrow even if multiple conflicts
 			r.add(s);
 		}
 		r.add("}");
 		return r;
 	}
 
 	void saveSerializabilityGraph(Path pathDotFile) {
 		List<String> graph = getSerializabilityGraph();
 		try {
 			Files.write(pathDotFile, graph, cs); // NIO
 		} catch (IOException e1) {
 			e1.printStackTrace();
 		}
 //		try (BufferedWriter writer = Files.newBufferedWriter(pathDotFile, cs)) {
 //			writer.write(graph);
 //			writer.flush();
 //		} catch (IOException e) {
 //			System.err.println("Error writing to " + pathDotFile);
 //			e.printStackTrace();
 //		}
 	}
 
 	void printSerializabilityGraph() {
 		System.out.println("-- Serializability graph:");
 		for (String line : getSerializabilityGraph()) {
 			System.out.println(line);
 		}
 		Path graphPath = Paths.get("graph.dot");
 		System.out.println("Graph will be saved in " + graphPath);
 		System.out.println("Try: dot -Tpng graph.dot > graph.png");
 		saveSerializabilityGraph(graphPath);
 		System.out.println();
 	}
 
 	// 3
 	List<Integer> getCycles() {
 		Map<Integer, List<Integer>> m = new HashMap<>(); // better: Guava MultiMap
 		// build multimap
 		for (Conflict c : getConflicts()) {
 			List<Integer> l = m.get(c.a.t);
 			if (l == null) l = new ArrayList<>();
 			l.add(c.b.t);
 			m.put(c.a.t, l);
 		}
 //		m = new HashMap<>(); // for testing
 //		m.put(1, Arrays.asList(2,3));
 //		m.put(3, Arrays.asList(1));
 //		m.put(2, new ArrayList<Integer>());
 		System.out.println("Map of conflicts: " + m);
 		// search cycles
 		for (Integer t : m.keySet()) { // for each conflicting T
 			// start a new DFS
 			List<Integer> cycle = new ArrayList<>();
 			// and recursively continue with connected components
 			System.out.println("start DFS with " + t);
 			if (recCycle(m, t, cycle, new ArrayList<Integer>())) return cycle;
 		}
 		return null;
 	}
 
 	/**
 	 * @param m
 	 *            MultiMap containing all connections
 	 * @param x
 	 *            Current node
 	 * @param cycle
 	 *            Will contain the result if a cycle was found
 	 * @param acc
 	 *            Accumulator containing visited nodes
 	 * @return true if a circle was found, false otherwise
 	 */
 	boolean recCycle(Map<Integer, List<Integer>> m, int x, List<Integer> cycle, List<Integer> acc) {
 		System.out.println("recCycle(m, " + x + ", " + cycle + ", " + acc + ")");
 		List<Integer> lacc = new ArrayList<>(acc); // local accumulator for branching (we need a new list to avoid changing upstream values)
 		lacc.add(x);
 		for (int i : m.get(x)) {
 			if (lacc.contains(i)) {
 				lacc.add(i);
 				cycle.addAll(lacc);
 				return true;
 			}
 			if (recCycle(m, i, cycle, lacc)) return true;
 		}
 		return false;
 	}
 
 	void printCycles() {
 		System.out.println("-- Cycle detection:");
 		List<Integer> cycles = getCycles();
 		if (cycles == null) {
 			System.out.println("No cycles...");
 		} else {
 			System.out.println("Found at least one cycle:");
 			System.out.println(cycles);
 		}
 	}
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		if (args.length != 1) {
 			System.err.println("Usage: java Scheduler scheduleFile");
 			return;
 		}
 		// parse input file
 		Scheduler scheduler = new Scheduler(Paths.get(args[0]));
 		// 1. determine set of conflicting operation pairs
 		scheduler.printConflicts();
 		// 2. construct serializability graph
 		scheduler.printSerializabilityGraph();
 		// 3. check the graph for cycles
 		scheduler.printCycles();
 	}
 
 	private static class Operation {
 		enum RW {
 			R, W
 		};
 
 		RW rw;
 		int t;
 		String d;
 
 		public Operation(RW rw, int transaction, String date) {
 			this.rw = rw;
 			this.t = transaction;
 			this.d = date;
 		}
 
 		public Operation(String rw, int transaction, String date) {
 			this(rw.equals("r") ? RW.R : RW.W, transaction, date);
 		}
 
 		@Override
 		public String toString() {
 			return rw.toString().toLowerCase() + String.valueOf(t) + "(" + d + ")";
 		}
 	}
 
 	private static class Conflict {
 		Operation a;
 		Operation b;
 
 		public Conflict(Operation a, Operation b) {
 			this.a = a;
 			this.b = b;
 		}
 
 		@Override
 		public String toString() {
 			return a + "<" + b;
 		}
 	}
 
 }
