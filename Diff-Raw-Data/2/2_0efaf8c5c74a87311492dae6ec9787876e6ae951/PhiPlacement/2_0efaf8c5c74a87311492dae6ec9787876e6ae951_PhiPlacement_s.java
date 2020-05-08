 package uk.ac.ic.doc.analysis;
 
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.Map;
 import java.util.Queue;
 import java.util.Set;
 
 import uk.ac.ic.doc.analysis.dominance.DomFront;
 import uk.ac.ic.doc.analysis.dominance.DomFront.DomInfo;
 import uk.ac.ic.doc.analysis.dominance.DomMethod;
 import uk.ac.ic.doc.cfg.model.BasicBlock;
 import uk.ac.ic.doc.cfg.model.Cfg;
 
 public class PhiPlacement {
 
 	private GlobalsAndDefsFinder finder;
 	private Map<BasicBlock, DomInfo> domInfo;
 	private Map<BasicBlock, Set<String>> phis = new HashMap<BasicBlock, Set<String>>();
 
 	public PhiPlacement(Cfg graph) throws Exception {
 		finder = new GlobalsAndDefsFinder(graph);
 		domInfo = new DomFront(new DomMethod(graph)).run();
 
 		for (String name : finder.globals()) {
 			Collection<BasicBlock> definingLocations = finder
 					.definingLocations(name);
 
 			// Variables can be global (used in multiple blocks) yet never
 			// defined. This most likely indicates a true global in the Python
 			// sense of the word or a function name that has been imported (also
 			// a Python global really).
 			if (definingLocations == null)
 				continue;
 
 			Queue<BasicBlock> worklist = new LinkedList<BasicBlock>(
 					definingLocations);
 			Set<BasicBlock> doneList = new HashSet<BasicBlock>();
 			while (!worklist.isEmpty()) {
 				BasicBlock workItem = worklist.remove();
 				doneList.add(workItem);
 
 				for (BasicBlock frontierBlock : domInfo.get(workItem).dominanceFrontiers) {
 
 					Set<String> phiTargetsAtLocation = phis.get(frontierBlock);
 					if (phiTargetsAtLocation == null) {
 						phiTargetsAtLocation = new HashSet<String>();
 						phis.put(frontierBlock, phiTargetsAtLocation);
 					}
 
 					phiTargetsAtLocation.add(name);
 
					if (doneList.contains(frontierBlock))
 						worklist.add(frontierBlock);
 				}
 			}
 		}
 	}
 
 	public Iterable<String> phiTargets(BasicBlock location) {
 		return phis.get(location);
 	}
 
 	public Map<BasicBlock, DomInfo> getDomInfo() {
 		return domInfo;
 	}
 }
