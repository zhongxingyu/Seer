 package brown.puzzles.liarliar;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
import java.util.Set;
 
 /**
  * @author Matt Brown
  * @date Feb 18, 2010
  */
 public class LiarDetectorImpl implements LiarDetector {
 
 	private Partition<Accuser> partition1 = new Partition<Accuser>("p1");
 
 	private Partition<Accuser> partition2 = new Partition<Accuser>("p2");
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see brown.puzzles.liarliar.LiarDetector#detect(java.util.Collection)
 	 */
 	public Response detect(Collection<Accuser> accusers) {
 
 		log("starting with size " + accusers.size());
 
 		List<Accuser> group = new ArrayList<Accuser>(accusers);
 
 		List<Accuser> p1Queue = new ArrayList<Accuser>();
 		List<Accuser> p2Queue = new ArrayList<Accuser>();
 
 		while (!group.isEmpty()) {
 
 			// enqueue the first item in the list.
 			// if we are coming back here after iterations of the below loop, it
 			// means we have a disconnected graph and we need to start working
 			// on another segment of the graph. Partition1 is picked
 			// arbitrarily.
 			Accuser root = group.remove(0);
 
 			p1Queue.add(root);
 
 			while (!p1Queue.isEmpty() || !p2Queue.isEmpty()) {
 
 				while (!p1Queue.isEmpty()) {
 					Accuser p1 = p1Queue.remove(0);
 					log("popped " + p1 + " from p1Queue");
 
					if (!partition1.contains(p1)) {
 						addToPartition(partition1, p1);
 
 						p2Queue.addAll(p1.getAccused());
 						group.removeAll(p1.getAccused());
 						p2Queue.addAll(p1.getAccusedBy());
 						group.removeAll(p1.getAccusedBy());
 					}
 				}
 
 				while (!p2Queue.isEmpty()) {
 					Accuser p2 = p2Queue.remove(0);
 					log("popped " + p2 + " from p2Queue");
 
					if (!partition2.contains(p2)) {
 						addToPartition(partition2, p2);
 
 						p1Queue.addAll(p2.getAccused());
 						group.removeAll(p2.getAccused());
 						p1Queue.addAll(p2.getAccusedBy());
 						group.removeAll(p2.getAccusedBy());
 					}
 				}
 
 			}
 		}
 
 		return new Response(partition1.size(), partition2.size());
 	}
 
 	private void addToPartition(Partition<Accuser> partition, Accuser node) {
 		log("adding " + node + " to " + partition);
 		partition.add(node);
 	}
 
	// these parameter names suck
	private void iterate(List<Accuser> currentQueue, Set<Accuser> thisSet, List<Accuser> otherQueue,
			Set<Accuser> otherSet, Collection<Accuser> group) {
		while (!currentQueue.isEmpty()) {
			Accuser next = currentQueue.remove(0);

			otherQueue.addAll(next.getAccused());
			for (Accuser a : next.getAccused()) {
				if (!thisSet.contains(a)) {
					otherSet.add(a);
				}
			}
			group.remove(next);
		}
 	}
 
 	private static void log(String message) {
		System.out.println(message);
 	}
 }
