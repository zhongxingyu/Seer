 /*
  * Copyright (c) 2008-2009, Intel Corporation.
  * Copyright (c) 2006-2007, The Trustees of Stanford University.
  * All rights reserved.
  */
 package chord.analyses.escape.dynamic;
 
 import gnu.trove.TIntHashSet;
 import gnu.trove.TIntIntHashMap;
 import gnu.trove.TIntIterator;
 import gnu.trove.TIntObjectHashMap;
 
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Set;
 
 import joeq.Compiler.Quad.Quad;
 import chord.doms.DomE;
 import chord.instr.InstrScheme;
 import chord.program.Program;
 import chord.project.Chord;
 import chord.project.Project;
 import chord.project.Properties;
 import chord.project.analyses.DynamicAnalysis;
 import chord.project.analyses.ProgramRel;
 import chord.util.ChordRuntimeException;
 
 /**
  * Dynamic thread-escape analysis where concrete objects are abstracted using
  * allocation sites and flow sensitivity is abstracted away.
  * 
  * Outputs the following relations:
  * <ul>
  * <li><tt>visitedE</tt> containing each heap-accessing statement that was
  * visited at least once.</li>
  * <li><tt>escE</tt> containing those visited heap-accessing statements that
  * were deemed to access thread-escaping data by the chosen thread-escape
  * analysis.</li>
  * </ul>
  * 
  * @author Omer Tripp (omertrip@post.tau.ac.il)
  * @author Mayur Naik (mhn@cs.stanford.edu)
  */
 @Chord(name = "dynamic-thresc-alloc-fi-java",
 	   namesOfSigns = { "visitedE", "escE" },
 	   signs = { "E0", "E0" })
 public class DynamicFIEscapeAnalysisUsingAlloc extends DynamicAnalysis {
 
 	private static class Node {
 		
 		public static final Node SHARED = new Node(0);
 		private static final TIntObjectHashMap<Node> cache = new TIntObjectHashMap<Node>(10);
 		
 		public static Node findOrCreate(int id) {
 			Node result = (id == 0) ? SHARED : cache.get(id);
 			if (result == null) {
 				result = new Node(id);
 				cache.put(id, result);
 			}
 			return result;
 		}
 		
 		private Node(int id) {
 			this.id = id;
 		}
 		
 		public final int id;
 
 		@Override
 		public int hashCode() {
 			final int prime = 31;
 			int result = 1;
 			result = prime * result + id;
 			return result;
 		}
 
 		@Override
 		public boolean equals(Object obj) {
 			if (this == obj)
 				return true;
 			if (obj == null)
 				return false;
 			if (getClass() != obj.getClass())
 				return false;
 			Node other = (Node) obj;
 			if (id != other.id)
 				return false;
 			return true;
 		}
 		
 		@Override
 		public String toString() {
 			return "Node " + id;
 		}
 	}
 	
 	private HashMap<Node, Set<Node>> reachabilityGraph;
 	private InstrScheme instrScheme;
 	private TIntIntHashMap obj2alloc;
 	private Map<Node, TIntHashSet> node2locs;
 	
 	private ProgramRel relVisitedE;
 	private ProgramRel relEscE;
 
 	public InstrScheme getInstrScheme() {
 		if (instrScheme != null)
 			return instrScheme;
     	instrScheme = new InstrScheme();
     	instrScheme.setNewAndNewArrayEvent(true, false, true);
     	instrScheme.setPutstaticReferenceEvent(false, false, false, false, true);
     	instrScheme.setThreadStartEvent(false, false, true);
 
     	instrScheme.setGetfieldPrimitiveEvent(true, false, true, false);
     	instrScheme.setPutfieldPrimitiveEvent(true, false, true, false);
     	instrScheme.setAloadPrimitiveEvent(true, false, true, false);
     	instrScheme.setAstorePrimitiveEvent(true, false, true, false);
 
     	instrScheme.setGetfieldReferenceEvent(true, false, true, false, false);
     	instrScheme.setPutfieldReferenceEvent(true, false, true, true, true);
     	instrScheme.setAloadReferenceEvent(true, false, true, false, false);
     	instrScheme.setAstoreReferenceEvent(true, false, true, true, true);
     	
     	return instrScheme;
 	}
 
 	public void initAllPasses() {
 		obj2alloc = new TIntIntHashMap();
 		reachabilityGraph = new HashMap<Node, Set<Node>>();
 		node2locs = new HashMap<Node, TIntHashSet>();
 		
 		relVisitedE = (ProgramRel) Project.getTrgt("visitedE");
 		relEscE = (ProgramRel) Project.getTrgt("escE");
 	}
 
 	public void initPass() {
 		obj2alloc.clear();
 	}
 
 	public void donePass() {
 		System.out.println("***** STATS *****");
 		int numVisitedE = getAllVisited().size();
		System.out.println("numVisitedE computed");
 		Set<Node> esc = getReachableFromShared();
		System.out.println("esc computed");
 		int numAllocEsc = esc.size();
 		TIntHashSet escE = new TIntHashSet();
 		for (Node n : esc) {
 			TIntHashSet S = node2locs.get(n);
 			if (S != null)
 				escE.addAll(S.toArray());
 		}
 		System.out.println("numAllocEsc: " + numAllocEsc);
 		System.out.println("numVisitedE: " + numVisitedE + " numEscE: " + escE.size());
 	}
 
 	private Set<Node> getReachableFromShared() {
 		Set<Node> esc = new HashSet<Node>(10);
 		Set<Node> worklist = new HashSet<Node>(5);
		Set<Node> visited = new HashSet<Node>(5);
 		worklist.add(Node.SHARED);
 		while (!worklist.isEmpty()) {
 			Iterator<Node> it = worklist.iterator();
 			worklist = new HashSet<Node>(5);
 			while (it.hasNext()) {
 				Node n = it.next();
				visited.add(n);
 				esc.add(n);
 				Set<Node> succSet = reachabilityGraph.get(n);
 				if (succSet != null) {
					for (Node succ : succSet) {
						if (!visited.contains(succ)) {
							worklist.add(succ);
						}
					}
 				}
 			}
 		}
 		return esc;
 	}
 
 	private TIntHashSet getAllVisited() {
 		TIntHashSet all = new TIntHashSet();
 		for (TIntHashSet S : node2locs.values()) {
 			all.addAll(S.toArray());
 		}
 		return all;
 	}
 
 	public void doneAllPasses() {
 		relVisitedE.zero();
 		TIntHashSet allVisited = getAllVisited();
 		TIntIterator iterator1 = allVisited.iterator();
 		while (iterator1.hasNext()) {
 			int next = iterator1.next();
 			if (next != -1)
 				relVisitedE.add(next);
 		}
 		relVisitedE.save();
 		relEscE.zero();
 		Set<Node> esc = getReachableFromShared();
 		for (Node n : esc) {
 			TIntHashSet S = node2locs.get(n);
 			if (S != null) {
 				TIntIterator iterator2 = S.iterator();
 				while (iterator2.hasNext()) {
 					int next = iterator2.next();
 					if (next != -1)
 						relEscE.add(next);
 				}
 			}
 		}
 		relEscE.save();
 		
 		DomE domE = instrumentor.getDomE();
 		Program program = Program.v();
 		String outDirName = Properties.outDirName;
 		try {
 			PrintWriter writer;
 			writer = new PrintWriter(new FileWriter(new File(outDirName, "dynamic_visitedE.txt")));
 			TIntIterator iterator3 = allVisited.iterator();
 			while (iterator3.hasNext()) {
 				int next = iterator3.next();
 				if (next != -1) {
 					Quad q = (Quad) domE.get(next);
 					String s = program.toVerboseStr(q);
 					writer.println(s);
 				}
 			}
 			writer.close();
 			writer = new PrintWriter(new FileWriter(new File(outDirName, "dynamic_escE.txt")));
 			for (Node n : esc) {
 				TIntHashSet S = node2locs.get(n);
 				if (S != null) {
 					TIntIterator iterator4 = S.iterator();
 					while (iterator4.hasNext()) {
 						int next = iterator4.next();
 						if (next != -1) {
 							Quad q = (Quad) domE.get(next);
 							String s = program.toVerboseStr(q);
 							writer.println(s);
 						}
 					}
 				}
 			}
 			writer.close();
 		} catch (IOException ex) {
 			throw new ChordRuntimeException(ex);
 		}
 	}
 
 	public void processNewOrNewArray(int h, int t, int o) {
 		if (h >= 0 && o != 0) {
 			obj2alloc.put(o, h);
 		}
 	}
 
 	public void processPutstaticReference(int e, int t, int b, int f, int o) {
 		if (o != 0) {
 			if (obj2alloc.containsKey(o)) {
 				doBookKeeping(-1, Node.SHARED.id, obj2alloc.get(o));
 			}
 		}
 	}
 
 	public void processThreadStart(int p, int t, int o) {
 		if (o != 0) {
 			if (obj2alloc.containsKey(o)) {
 				doBookKeeping(-1, Node.SHARED.id, obj2alloc.get(o));
 			}
 		}
 	}
 	
 	public void processPutfieldReference(int e, int t, int b, int f, int o) {
 		// We ignore strong updates.
 		if (e >= 0 && b != 0 && o != 0) {
 			if (obj2alloc.containsKey(b) && obj2alloc.containsKey(o)) {
 				int srcId = obj2alloc.get(b);
 				int trgtId = obj2alloc.get(o);				
 				doBookKeeping(e, srcId, trgtId);
 			}
 		}
 	}
 
 	public void processAstoreReference(int e, int t, int b, int i, int o) {
 		// We ignore strong updates.
 		if (e >= 0 && b != 0 && o != 0) {
 			if (obj2alloc.containsKey(b) && obj2alloc.containsKey(o)) {
 				int srcId = obj2alloc.get(b);
 				int trgtId = obj2alloc.get(o);
 				doBookKeeping(e, srcId, trgtId);
 			}
 		}
 	}
 	
 	public void processGetfieldPrimitive(int e, int t, int b, int f) {
 		if (e >= 0 && b != 0) {
 			if (obj2alloc.containsKey(b)) {
 				Node n = Node.findOrCreate(obj2alloc.get(b));
 				updateNode2Locations(e, n);
 			}
 		}
 	}
 
 	public void processAloadPrimitive(int e, int t, int b, int i) {
 		if (e >= 0 && b != 0) {
 			if (obj2alloc.containsKey(b)) {
 				Node n = Node.findOrCreate(obj2alloc.get(b));
 				updateNode2Locations(e, n);
 			}
 		}
 	}
 
 	public void processPutfieldPrimitive(int e, int t, int b, int f) {
 		if (e >= 0 && b != 0) {
 			if (obj2alloc.containsKey(b)) {
 				Node n = Node.findOrCreate(obj2alloc.get(b));
 				updateNode2Locations(e, n);
 			}
 		}
 	}
 
 	public void processAstorePrimitive(int e, int t, int b, int i) {
 		if (e >= 0 && b != 0) {
 			if (obj2alloc.containsKey(b)) {
 				Node n = Node.findOrCreate(obj2alloc.get(b));
 				updateNode2Locations(e, n);
 			}
 		}
 	}
 
 	public void processGetfieldReference(int e, int t, int b, int f, int o) {
 		if (e >= 0 && b != 0) {
 			if (obj2alloc.containsKey(b)) {
 				Node n = Node.findOrCreate(obj2alloc.get(b));
 				updateNode2Locations(e, n);
 			}
 		}
 	}
 
 	public void processAloadReference(int e, int t, int b, int i, int o) {
 		if (e >= 0 && b != 0) {
 			if (obj2alloc.containsKey(b)) {
 				Node n = Node.findOrCreate(obj2alloc.get(b));
 				updateNode2Locations(e, n);
 			}
 		}
 	}
 	
 	private void doBookKeeping(int loc, int srcId, int trgtId) {
 		Node src = Node.findOrCreate(srcId);
 		updateNode2Locations(loc, src);	
 		updateReachability(trgtId, src);
 	}
 
 	private void updateReachability(int trgtId, Node src) {
 		Set<Node> succSet = reachabilityGraph.get(src);
 		if (succSet == null) {
 			succSet = new HashSet<Node>(1);
 			reachabilityGraph.put(src, succSet);
 		}
 		Node trgt = Node.findOrCreate(trgtId);
 		succSet.add(trgt);
 	}
 
 	private void updateNode2Locations(int loc, Node src) {
 		TIntHashSet S = node2locs.get(src);
 		if (S == null) {
 			S = new TIntHashSet();
 			node2locs.put(src, S);
 		}
 		S.add(loc);
 	}
 }
