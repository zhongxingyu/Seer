 package org.clustering.classifier;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 
 import org.clustering.model.HierarchicalCluster;
 import org.clustering.model.Item;
 
 public class HierarchicalClassifier {
 
 	public HierarchicalCluster createHierarchicalCluster(List<Item> items) {
 
 		List<HierarchicalCluster> initialClusters = createInitialClusters(items);
 
 		while(initialClusters.size() > 1) {
 			initialClusters = mergeClusters(initialClusters);
 		}
 		
 		return initialClusters.get(0);
 	}
 
 	private List<HierarchicalCluster> mergeClusters(
 			List<HierarchicalCluster> initialClusters) {
 		ArrayList<HierarchicalCluster> nextLevel = new ArrayList<HierarchicalCluster>();
 		while (initialClusters.size() > 1) {
 			int mI = 0, mJ = 0;
 			double maxSim = 0;
 			for (int i = 0; i < initialClusters.size(); i++) {
 				for (int j = i+1; j < initialClusters.size(); j++) {
 					double similarity = getSimilarity(initialClusters.get(i),
 							initialClusters.get(j));
 					if (similarity > maxSim) {
 						maxSim = similarity;
 						mI = i;
 						mJ = j;
 					}
 				}
 			}
 			HierarchicalCluster iCluster = initialClusters.get(mI);
 			HierarchicalCluster jCluster = initialClusters.get(mJ);
 			// remove j first, because j is always behind i
 			initialClusters.remove(mJ);
 			initialClusters.remove(mI);
 			nextLevel.add(new HierarchicalCluster(maxSim, iCluster, jCluster));
 		}
 		if(initialClusters.size() == 1) {
 			nextLevel.add(initialClusters.get(0));
 		}
 		return nextLevel;
 	}
 
 	private double getSimilarity(HierarchicalCluster c1,
 			HierarchicalCluster c2) {
 		//compare average inter distance
 		double avgInterDistC1C2= getAvgInterDistance(c1, c2);
		return 1.0 - avgInterDistC1C2;
		
		//double completeLinkSimilarity = getComple
		
 		
 	}
 
 	private double getAvgInterDistance(HierarchicalCluster c1, HierarchicalCluster c2) {
 		//for each item in C1 get its average distance to elements in C2
 		//the distance of C1 to C2 is then average of these average distance. 
 		HashSet<Item> itemsC1 = c1.getItems();
 		HashSet<Item> itemsC2 = c2.getItems();
 		
 		double sumAvgDist = 0.0;
 		double avgInterDist = 0.0;
 		for(Item item1 : itemsC1){
 			double sumItem = 0.0;
 			for(Item item2 : itemsC2){
 				sumItem += item1.getDistance(item2);
 			}
 			sumAvgDist += sumItem / itemsC2.size();
 		}
 		
 		avgInterDist = sumAvgDist / itemsC1.size();
 		return avgInterDist;
 	}
 
 	private List<HierarchicalCluster> createInitialClusters(List<Item> items) {
 		ArrayList<HierarchicalCluster> result = new ArrayList<HierarchicalCluster>();
 		for (Item item : items) {
 			result.add(new HierarchicalCluster(item));
 		}
 		return result;
 	}
 
 }
