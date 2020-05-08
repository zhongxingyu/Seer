 package mapthatset.aiplayer.appliedRules;
 
 import java.util.HashSet;
 import java.util.Set;
 
 import mapthatset.aiplayer.util.Knowledge;
 
 public class AppliedDistribution extends AbstractAppliedRule {
 	
 	@Override
 	public Set<Knowledge> apply() {
 		Knowledge tmp = ku.get(0);
 		
 		Set<Knowledge> result = new HashSet<Knowledge>();
		result.add(tmp);
 		
 		for (int p : tmp.getPreimage()) {
 			Set<Integer> preimage = new HashSet<Integer>();
 			preimage.add(p);
 			
 			Set<Integer> image = new HashSet<Integer>();
 			image.addAll(tmp.getImage());
 			
 			Knowledge k = new Knowledge(preimage, image);
 			result.add(k);
 		}
 		
 		return result;
 	}
 	
 	@Override
 	public double getPriorityPenalty() {
 		// TODO Auto-generated method stub
 		return 30;
 	}
 }
