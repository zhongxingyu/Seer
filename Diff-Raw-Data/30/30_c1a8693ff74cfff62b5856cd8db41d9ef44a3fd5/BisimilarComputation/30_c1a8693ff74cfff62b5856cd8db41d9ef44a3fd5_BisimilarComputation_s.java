 package se705.bisimulation;
 
 import java.util.HashSet;
 import java.util.Set;
 import java.util.TreeSet;
 
 import se705.bisimulation.lts.Process;
 import se705.bisimulation.lts.Transition;
 import se705.bisimulation.util.StringUtil;
 
 public class BisimilarComputation {
 	private final Process processP;
 	private final Process processQ;
	private final HashSet<Set<String>> rho;
 	private final Set<Transition> ts;
 
 	public BisimilarComputation(final Process p, final Process q) {
 		this.processP = p;
 		this.processQ = q;
 		this.ts = new HashSet<Transition>();
 		this.ts.addAll(p.getTransitions());
 		this.ts.addAll(q.getTransitions());
 
 		Set<String> allStates = new HashSet<String>();
 		allStates.addAll(p.getStates());
 		allStates.addAll(q.getStates());
 
 		Set<Set<String>> rhoinit = new HashSet<Set<String>>();
 		rhoinit.add(allStates);
 
 		Set<String> sigma = new TreeSet<String>();
 		sigma.addAll(p.getActions());
 		sigma.addAll(q.getActions());
 
 		this.rho = new HashSet<Set<String>>(rhoinit);
 		Set<Set<String>> waiting = new HashSet<Set<String>>(rhoinit);
 
 		while (true) {
 			Set<String> pprime = this.popStates(waiting);
 
 			for (String action : sigma) {
 				Set<Set<String>> matchP = new HashSet<Set<String>>();
 
 				for (Set<String> partition : this.rho) {
 					Set<String> tap = this.t(partition, pprime, action);
 
 					if (!tap.isEmpty() && !tap.equals(partition)) {
 						matchP.add(partition);
 					}
 				}
 
 				for (Set<String> partition : matchP) {
 					Set<Set<String>> splitP = this.splitP(partition, pprime, action);
 
 					this.rho.remove(partition);
 					this.rho.addAll(splitP);
 
 					waiting.remove(partition);
 					waiting.addAll(splitP);
 				}
 			}
 
 			if (waiting.isEmpty()) {
 				break;
 			}
 		}
 	}
 
 	private Set<String> popStates(final Set<Set<String>> stateSets) {
 		Set<String> states = stateSets.iterator().next();
 		stateSets.remove(states);
 		return states;
 	}
 
 	private Set<Set<String>> splitP(final Set<String> partition, final Set<String> pprime, final String action) {
 		Set<Set<String>> splitP = new HashSet<Set<String>>();
 
 		Set<String> tap = this.t(partition, pprime, action);
 		Set<String> nottap = new HashSet<String>(partition);
 		nottap.removeAll(tap);
 
 		splitP.add(tap);
 		splitP.add(nottap);
 
 		return splitP;
 	}
 
 	private Set<String> t(final Set<String> partition, final Set<String> pprime, final String action) {
 		Set<String> acc = new HashSet<String>();
 
 		for (String s : partition) {
 			for (String d : pprime) {
 				if (this.ts.contains(new Transition(s, action, d))) {
 					acc.add(s);
 				}
 			}
 		}
 
 		return acc;
 	}
 
 	public boolean isBisimilar() {
 		boolean isBisimilar = false;
 
 		for (Set<String> partition : this.rho) {
 			boolean fromP = false, fromQ = false;
 
 			for (String state : partition) {
 				fromP = fromP || this.processP.getStates().contains(state);
 				fromQ = fromQ || this.processQ.getStates().contains(state);
 			}
 
 			isBisimilar = fromP && fromQ;
 
 			// If there is a state from p without a bisimilar state in q, then
 			// the processes are not bisimilar
 			if (!isBisimilar) {
 				break;
 			}
 		}
 
 		return isBisimilar;
 	}
 
 	@Override
 	public String toString() {
 		StringBuilder out = new StringBuilder();
 
 		out.append("Bisimulation Results\n");
 
 		for (Set<String> partition : this.rho) {
 			out.append(StringUtil.join(StringUtil.wrapEach(StringUtil.removePrefixEach(partition), "state(%s)"), ",") + "\n");
 		}
 
 		out.append("Bisimulation Answer\n");
 		out.append(this.isBisimilar() ? "Yes" : "No");
 
 		return out.toString();
 	};
 }
