 /*
  * Copyright (c) 2008-2009, Intel Corporation.
  * Copyright (c) 2006-2007, The Trustees of Stanford University.
  * All rights reserved.
  */
package chord.project.analyses.slicer;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import joeq.Class.jq_Method;
 import joeq.Compiler.Quad.BasicBlock;
 import joeq.Compiler.Quad.Operator;
 import joeq.Compiler.Quad.Quad;
 import joeq.Compiler.Quad.Inst;
 import joeq.Compiler.Quad.Operand.MethodOperand;
 import joeq.Compiler.Quad.Operator.Invoke;
 import joeq.Compiler.Quad.Operator.Return;
 
 import chord.util.tuple.object.Pair;
 import chord.program.Program;
 import chord.program.Location;
 import chord.analyses.alias.ICICG;
 import chord.analyses.alias.ThrOblAbbrCICGAnalysis;
 import chord.doms.DomI;
 import chord.doms.DomM;
 import chord.project.Project;
 import chord.project.analyses.JavaAnalysis;
 import chord.util.ArraySet;
 
 /**
  * Implementation of the Reps-Horwitz-Sagiv algorithm for
  * summary-based dataflow analysis.
  * 
  * @author Mayur Naik (mhn@cs.stanford.edu)
  */
 public abstract class BackwardRHSAnalysis<PE extends IPathEdge, SE extends ISummaryEdge>
 		extends JavaAnalysis {
     protected  static boolean DEBUG = false;
 
 	// get the path edge at exit of main method m
 	public abstract PE getInitPathEdge(jq_Method m);
 
 	// get the path edge in callee for target method m called from call site q
 	// with caller path edge pe
 	public abstract PE getInitPathEdge(Quad q, jq_Method m, PE pe);
 
 	// get outgoing path edge from q, given incoming path edge pe into q.
 	//  q is guaranteed to not be an invoke statement, return statement,
 	// entry basic block, or exit basic block
 	public abstract PE getMiscPredPathEdge(Quad q, PE pe);
  
 	// get outgoing path edge from q given incoming path edge pe into q.
 	// q is an invoke statement with target method java.lang.Thread.start()
 	public abstract PE getForkPredPathEdge(Quad q, PE pe);
 
 	// q is an invoke statement
 	// get path edge to predecessor of q given path edge into q and summary
 	// edge of a target method of the call site q
 	// returns null if the path edge into q is not compatible with the
 	// summary edge
 	public abstract PE getInvkPredPathEdge(Quad q, PE clrPE, SE tgtSE);
 	public abstract boolean doMerge();
 
 	// m is a method and pe is a path edge from exit to entry of m
 	// that must be lifted to a summary edge
 	public abstract SE getSummaryEdge(jq_Method m, PE pe);
 
 	private List<Pair<Location, PE>> workList =
 		new ArrayList<Pair<Location, PE>>();
 	private boolean doMerge = doMerge();
 	private Map<Inst, Set<PE>> pathEdges =
 		new HashMap<Inst, Set<PE>>();
 	private Map<jq_Method, Set<SE>> summEdges =
 		new HashMap<jq_Method, Set<SE>>();
 
 	private DomI domI;
 	private DomM domM;
 
 	private Map<Quad, Location> invkQuadToLoc =
 		new HashMap<Quad, Location>();
 	private ICICG cicg;
 	private Map<jq_Method, Set<Quad>> callersMap =
 		new HashMap<jq_Method, Set<Quad>>();
 	private Map<Quad, Set<jq_Method>> targetsMap =
 		new HashMap<Quad, Set<jq_Method>>();
 
 	private Set<Quad> getCallers(jq_Method m) {
 		Set<Quad> callers = callersMap.get(m);
 		if (callers == null) {
 			callers = cicg.getCallers(m);
 			callersMap.put(m, callers);
 		}
 		return callers;
 	}
 
 	private Set<jq_Method> getTargets(Quad i) {
 		Set<jq_Method> targets = targetsMap.get(i);
 		if (targets == null) {
 			targets = cicg.getTargets(i);
 			targetsMap.put(i, targets);
 		}
 		return targets;
 	}
 
 	public void run() {
 		domI = (DomI) Project.getTrgt("I");
 		Project.runTask(domI);
 		domM = (DomM) Project.getTrgt("M");
 		Project.runTask(domM);
 		ThrOblAbbrCICGAnalysis cicgAnalysis =
 			(ThrOblAbbrCICGAnalysis) Project.getTrgt("throbl-abbr-cicg-java");
 		Project.runTask(cicgAnalysis);
 		cicg = cicgAnalysis.getCallGraph();
 		jq_Method main = Program.v().getMainMethod();
 		init(main);
 		while (!workList.isEmpty()) {
 			int last = workList.size() - 1;
 			Pair<Location, PE> pair = workList.remove(last);
 			Location loc = pair.val0;
 			PE pe = pair.val1;
 			Quad q = loc.q;
 			if (DEBUG) System.out.println("Processing loc: " + loc + " PE: " + pe);
 			if (q == null) {
 				BasicBlock bb = loc.bb;
 				if (bb.isExit()) {
 					for (Object o : bb.getPredecessorsList()) {
 						BasicBlock bb2 = (BasicBlock) o;
 						Quad q2;
 						int q2Idx;
 						int n = bb2.size();
 						if (n == 0) {
 							q2 = null;
 							q2Idx = -1;
 						} else {
 							q2 = bb2.getQuad(n - 1);
 							q2Idx = 0;
 						}
 						Location loc2 = new Location(loc.m, bb2, q2Idx, q2);
 						addPathEdge(loc2, pe);
 					}
 				} else {
 					assert (bb.isEntry()); 
 					processEntry(loc.m, pe);
 				}
 			} else {
 				Operator op = q.getOperator();
 				if (op instanceof Invoke) {
 					processInvoke(loc, pe);
 				} else {
 					PE pe2 = getMiscPredPathEdge(q, pe);
 					propagateToPred(loc.m, loc.bb, loc.qIdx, pe2);
 				}
 			}
 		}
 	}
 
 	private void propagateToPred(jq_Method m, BasicBlock bb, int qIdx, PE pe) {
 		if (qIdx != 0) {
 			int q2Idx = qIdx - 1;
 			Quad q2 = bb.getQuad(q2Idx);
 			Location loc2 = new Location(m, bb, q2Idx, q2);
 			addPathEdge(loc2, pe);
 			return;
 		}
 		for (Object o : bb.getPredecessorsList()) {
 			BasicBlock bb2 = (BasicBlock) o;
 			int q2Idx;
 			Quad q2;
 			int n = bb2.size();
 			if (n == 0) {
 				q2Idx = -1;
 				q2 = null;
 			} else {
 				q2Idx = 0;
 				q2 = bb2.getQuad(n - 1);
 			}
 			Location loc2 = new Location(m, bb2, q2Idx, q2);
 			addPathEdge(loc2, pe);
 		}
 	}
 
 	private void addPathEdge(Location loc, PE pe) {
 		if (DEBUG) System.out.println("\tChecking if " + loc + " has PE: " + pe);
 		Quad q = loc.q;
 		Inst i = (q != null) ? q : loc.bb;
         Set<PE> peSet = pathEdges.get(i);
 		PE peToAdd = pe;
         if (peSet == null) {
             peSet = new HashSet<PE>();
             pathEdges.put(i, peSet);
             if (q != null && (q.getOperator() instanceof Invoke))
                 invkQuadToLoc.put(q, loc);
 			peSet.add(pe);
 			if (DEBUG) System.out.println("\tNo, adding it as first PE");
         } else if (doMerge) {
 			boolean matched = false;
 			for (PE pe2 : peSet) {
 				if (pe2.matchesSrcNodeOf(pe)) {
 					if (DEBUG) System.out.println("\tNo, but matches PE: " + pe2);
 					boolean changed = pe2.mergeWith(pe);
 					if (DEBUG) System.out.println("\tNew PE after merge: " + pe2); 
 					if (!changed) {
 						if (DEBUG) System.out.println("\tExisting PE did not change");
 						return;
 					}
 					if (DEBUG) System.out.println("\tExisting PE changed");
 					// pe2 is already in pathEdges(i), so no need to add it;
 					// but it may or may not be in workList
 					for (int j = workList.size() - 1; j >= 0; j--) {
 						Pair<Location, PE> pair = workList.get(j);
 						PE pe3 = pair.val1;
 						if (pe3 == pe2)
 							return;
 					}
 					peToAdd = pe2;
 					matched = true;
 					break;
 				}
 			}
 			if (!matched) {
 				if (DEBUG) System.out.println("\tNo, adding");
 				peSet.add(pe);
 			}
 		} else if (!peSet.add(pe)) {
 			if (DEBUG) System.out.println("\tYes, not adding");
 			return;
 		}
 		assert (peToAdd != null);
 		if (DEBUG) System.out.println("\tAlso adding to worklist");
 		Pair<Location, PE> pair = new Pair<Location, PE>(loc, peToAdd);
 		workList.add(pair);
 	}
 
 	private void init(jq_Method root) {
 		workList.clear();
 		pathEdges.clear();
 		summEdges.clear();
 		PE pe = getInitPathEdge(root);
 		BasicBlock bb = root.getCFG().exit();
 		Location loc = new Location(root, bb, -1, null);
 		addPathEdge(loc, pe);
 	}
 
 	private void processInvoke(Location loc, PE pe) {
 		Quad q = loc.q;
         jq_Method m = Program.v().getMethod(q);
 		for (jq_Method m2 : getTargets(q)) {
 			if (DEBUG) System.out.println("\tTarget: " + m2);
 			Set<SE> seSet = summEdges.get(m2);
 			boolean found = false;
 			if (seSet != null) {
 				for (SE se : seSet) {
 					if (DEBUG) System.out.println("\tTesting SE: " + se);
 					if (propagateSEtoPE(pe, loc, m, se)) {
 						if (DEBUG) System.out.println("\tMatched");
 						found = true;
 						if (doMerge)
 							break;
 					} else {
 						if (DEBUG) System.out.println("\tDid not match");
 					}
 				}
 			}
 			if (!found) {
 				if (DEBUG) System.out.println("\tNo SE found");
 				PE pe2 = getInitPathEdge(q, m2, pe);
 				BasicBlock bb2 = m2.getCFG().exit();
 				Location loc2 = new Location(m2, bb2, -1, null);
 				addPathEdge(loc2, pe2);
 			}
 		}
 	}
 
 	private void processEntry(jq_Method m, PE pe) {
 		SE se = getSummaryEdge(m, pe);
 		Set<SE> seSet = summEdges.get(m);
 		if (DEBUG) System.out.println("\tChecking if " + m + " has SE: " + se);
 		SE seToAdd = se;
 		if (seSet == null) {
 			seSet = new HashSet<SE>();
 			summEdges.put(m, seSet);
 			seSet.add(se);
 			if (DEBUG) System.out.println("\tNo, adding it as first SE");
 		} else if (doMerge) {
 			boolean matched = false;
 			for (SE se2 : seSet) {
 				if (se2.matchesSrcNodeOf(se)) {
 					if (DEBUG) System.out.println("\tNo, but matches SE: " + se2);
 					boolean changed = se2.mergeWith(se);
 					if (DEBUG) System.out.println("\tNew SE after merge: " + se2); 
 					if (!changed) {
 						if (DEBUG) System.out.println("\tExisting SE did not change");
 						return;
 					}
 					if (DEBUG) System.out.println("\tExisting SE changed");
 					// se2 is already in summEdges(m), so no need to add it
 					seToAdd = se2;
 					matched = true;
 					break;
 				}
 			}
 			if (!matched) {
 				if (DEBUG) System.out.println("\tNo, adding");
 				seSet.add(se);
 			}
 		} else if (!seSet.add(se)) {
 			if (DEBUG) System.out.println("\tYes, not adding");
 			return;
 		}
 		for (Quad q2 : getCallers(m)) {
 			jq_Method m2 = Program.v().getMethod(q2);
 			if (DEBUG) System.out.println("\tCaller: " + q2 + " in " + m2);
 			Set<PE> peSet = pathEdges.get(q2);
 			if (peSet == null)
 				continue;
 			// make a copy because propagateSEtoPE might add a
 			// path edge to this set itself
 			// TODO: fix this eventually
 			peSet = new ArraySet<PE>(peSet);
 			Location loc2 = invkQuadToLoc.get(q2);
 			for (PE pe2 : peSet) {
 				if (DEBUG) System.out.println("\tTesting PE: " + pe2);
 				boolean match = propagateSEtoPE(pe2, loc2, m2, seToAdd);
 				if (match) {
 					if (DEBUG) System.out.println("\tMatched");
 				} else {
 					if (DEBUG) System.out.println("\tDid not match");
 				}
 			}
 		}
 	}
 
 	private boolean propagateSEtoPE(PE clrPE, Location loc,
 			jq_Method clrM, SE tgtSE) {
 		Quad q = loc.q;
         PE pe2 = getInvkPredPathEdge(q, clrPE, tgtSE);
 		if (pe2 == null)
 			return false;
         propagateToPred(loc.m, loc.bb, loc.qIdx, pe2);
         return true;
 	}
 }
 
