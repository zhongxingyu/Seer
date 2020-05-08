 package mesquite.treecmp.clustering.HierarchicalTreeClustering;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Comparator;
 import java.util.HashSet;
 import java.util.List;
 import java.util.PriorityQueue;
 import java.util.Set;
 
 import mesquite.lib.Trees;
 import mesquite.lib.duties.DistanceBetween2Trees;
 import mesquite.treecmp.Utils;
 import mesquite.treecmp.clustering.AgglomerativeClustering;
 import mesquite.treecmp.clustering.Configure;
 import mesquite.treecmp.clustering.GroupsForTreeVector;
 
 import org.pr.clustering.hierarchical.Cluster;
 import org.pr.clustering.hierarchical.Hierarchical;
 import org.pr.clustering.hierarchical.LinkageCriterion;
 
 public class HierarchicalTreeClustering extends GroupsForTreeVector implements AgglomerativeClustering {
 	private LinkageCriterion linkageCriterion;
 
 	@Override
 	public List<Integer> calculateClusters(Trees trees,
 			DistanceBetween2Trees distance) {
 		final double[][] distances = Utils.calculateDistanceMatrix(distance, trees, getProject());
 		
 		final Hierarchical clusteringAlgorithm = new Hierarchical(distances, linkageCriterion);
 		clusteringAlgorithm.partition();
 		final Collection<Collection<Integer>> partitioning = getResultFromRootCluster(clusteringAlgorithm.getRootCluster());
 		return Utils.convertToAssignments(trees.size(), partitioning);
 	}
 
 	@Override
 	public boolean startJob(String arguments, Object condition,
 			boolean hiredByName) {
 		return Configure.aglomerativeClusteringAlgorithm(this, containerOfModule());
 	}
 
 	@Override
 	public String getName() {
 		return "Hierarchical clustering algorithm.";
 	}
 	
 	private class ClusterComparator implements Comparator<Cluster> {
 		public int compare(Cluster arg0, Cluster arg1) {
 			if (arg0.distanceBetweenLeftAndRightClusters < arg1.distanceBetweenLeftAndRightClusters) return 1;
 			if (arg0.distanceBetweenLeftAndRightClusters > arg1.distanceBetweenLeftAndRightClusters) return -1;
 			return 0;
 		}		
 	}
 
 	private Collection<Collection<Integer>> getResultFromRootCluster(Cluster root) {
 		Set<Cluster> resultClusters = new HashSet<Cluster>();
 		
 		PriorityQueue<Cluster> queue = new PriorityQueue<Cluster>(1, new ClusterComparator());
 		
 		queue.add(root);		
 		resultClusters.add(root);
 		
 		double maxDistance = root.distanceBetweenLeftAndRightClusters;
 		
		while ((resultClusters.size() < numberOfClusters || numberOfClusters == -1) && !queue.isEmpty()) {
 			Cluster widest = queue.poll();
 			Cluster left = widest.left;
 			Cluster right = widest.right;
 			
 			if (numberOfClusters == -1 && 2*widest.distanceBetweenLeftAndRightClusters < maxDistance)
 				break;
 			
			if (left != null && right != null) {	//make sure it is not a leaf
				queue.add(left);
				queue.add(right);
				
				resultClusters.remove(widest);
				resultClusters.add(left);
				resultClusters.add(right);
			}
 		}
 		
 		Collection<Collection<Integer>> result = new ArrayList<Collection<Integer>>(resultClusters.size());
 		for (Cluster cluster : resultClusters)
 			result.add(cluster.patternIndexes);
 		
 		return result;
 	}
 
 	public void configure(int numbeOfClusters, LinkageCriterion linkageCriterion) {
 		this.numberOfClusters = numbeOfClusters;
 		this.linkageCriterion = linkageCriterion;
 	}
 }
