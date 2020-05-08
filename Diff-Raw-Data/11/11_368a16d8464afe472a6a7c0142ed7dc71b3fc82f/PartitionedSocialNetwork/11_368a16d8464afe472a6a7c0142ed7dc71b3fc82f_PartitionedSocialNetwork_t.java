 package org.computer.knauss.reqtDiscussion.model.socialNetwork;
 
 import org.computer.knauss.reqtDiscussion.model.Discussion;
 import org.computer.knauss.reqtDiscussion.model.DiscussionEvent;
 import org.computer.knauss.reqtDiscussion.model.partition.IDiscussionOverTimePartition;
 
 public class PartitionedSocialNetwork extends SocialNetwork {
 
 	@Override
 	public double getWeight(Node actor1, Node actor2) {
		if (actor1.equals(actor2))
			return 0.0;
		
 		double ret = 0;
 
 		boolean actor1Found;
 		boolean actor2Found;
 
 		for (Discussion wi : getDiscussions()) {
 			IDiscussionOverTimePartition p = getDiscussionOverTimePartition();
 			p.setDiscussionEvents(wi.getAllComments());
 			// System.out.println("Partitions: " + p.getPartitionCount());
 			for (int i = 0; i < p.getPartitionCount(); i++) {
 				actor1Found = false;
 				actor2Found = false;
 				// System.out.println(i + " - " + p
 				// .getWorkitemsForPartition(i).length);
 				for (DiscussionEvent wc : p.getDiscussionEventForPartition(i)) {
 					if (actor1.getLabel().equals(wc.getCreator()))
 						actor1Found = true;
 					if (actor2.getLabel().equals(wc.getCreator()))
 						actor2Found = true;
 				}
 				if (actor1Found && actor2Found) {
 					ret++;
 				}
 			}
 		}
 
 		// System.out.println(actor1 + "-> " + actor2 + "=" + ret);
 		return ret;
 	}
 
 }
