 package org.clustering.model;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class Cluster {
 
 	private Item centroid;
 	private boolean centroidChanged;
 	private List<Item> members;
 
 	public Cluster(Item initialCentoroid) {
 		this.centroid = initialCentoroid;
 		this.centroidChanged = true;
 		members = new ArrayList<Item>();
 		members.add(centroid);
 	}
 
 	public Item getCentroid() {
 		return centroid;
 	}
 
 	public void addItem(Item item) {
 		this.members.add(item);
 	}
 
 	public void computeNewCnetroid() {
 		double[] distances = new double[members.size()];
		for(int i =0; i < members.size(); i++) {
			for(int j = i+1; j<members.size(); j++) {
 				double distance = members.get(i).getDistance(members.get(j));
				distances[i]+=distance;
				distances[j]+=distance;
 			}
 		}
 		int lastIndex = 0;
		double smallestDistance = Double.MAX_VALUE;		
		for(int i = 0; i < distances.length; i++) {
			if(distances[i] < smallestDistance) {
 				smallestDistance = distances[i];
 				lastIndex = i;
 			}
 		}
 		Item newCentroid = members.get(lastIndex);
		if(newCentroid != centroid){
			centroidChanged = true;
		}
 		centroid = newCentroid;
 	}
 
 	public void removeItem(Item item) {
 		members.remove(item);
 	}
 
 	public boolean contains(Item item) {
 		return members.contains(item);
 	}
	
	public boolean centroidChanged(){
 		return centroidChanged;
 	}
 
 }
