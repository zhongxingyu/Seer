 // Released under GPLv2 or later. See http://www.gnu.org/ for details.
 package tags.util;
 
 import java.util.Set;
 import java.util.Map;
 import java.util.Queue;
 import java.util.HashSet;
 import java.util.HashMap;
 import java.util.PriorityQueue;
 import java.util.Comparator;
 
 /**
 ** DOCUMENT. SPU = Shortest-Paths Union
 */
 public class SPUProbabilityInferer implements ScoreInferer<Probability> {
 
 	final protected double REDUCE;
 
 	public SPUProbabilityInferer(double REDUCE) {
 		this.REDUCE = REDUCE;
 	}
 
 	public SPUProbabilityInferer() {
 		this(0.0625);
 	}
 
 	/**
 	** {@inheritDoc}
 	*/
 	public <A> Probability inferScore(Map<A, Set<A>> incoming, Map<A, Probability> seeds, A subj) {
 		if (!incoming.containsKey(subj)) { return Probability.MIN_VALUE; }
 		if (seeds.containsKey(subj)) { return seeds.get(subj); }
 
 		// Dijkstra's algorithm. OPT NORM could possibly put this and the
 		// implementation in Naming into the same code
 
 		// 1. Init
 		Set<A> left = new HashSet<A>(seeds.keySet());
 		final Map<A, Integer> step = new HashMap<A, Integer>(incoming.size()<<1);
 		PriorityQueue<A> queue = new PriorityQueue<A>(incoming.size(), new Comparator<A>() {
 			@Override public int compare(A a1, A a2) {
 				return step.get(a1).compareTo(step.get(a2));
 			}
 		});
 
 		for (A n: incoming.keySet()) {
 			step.put(n, Integer.MAX_VALUE);
 			queue.add(n);
 		}
 		queue.remove(subj);
 		step.put(subj, 0);
 		queue.add(subj);
 
 		// 2. Loop
 		while (!queue.isEmpty()) {
 			A node = queue.poll();
 			Integer s = step.get(node);
 
 			Set<A> inc = incoming.get(node);
 			for (A in: inc) {
				if (s < step.get(in)-1) {
 					queue.remove(in);
 					step.put(in, s+1);
 					queue.add(in);
 				}
 			}
 
 			left.remove(node);
 			if (left.isEmpty()) { break; }
 		}
 
 		double union = 1;
 		for (Map.Entry<A, Probability> en: seeds.entrySet()) {
			int s = step.get(en.getKey());
			if (s == Integer.MAX_VALUE) { continue; }
			//System.out.println(union + " " + en.getValue().val + " " + s);
			union *= (1 - en.getValue().val * Math.pow(REDUCE, s));
 		}
		//System.out.println(union);
		//System.out.println("----");
 
 		return new Probability(1 - union);
 	}
 
 }
