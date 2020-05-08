 package org.clustering.model;
 
 import java.util.HashMap;
 import java.util.HashSet;
 
 public class Item {
 	
 
 	private final int itemNumber;
 	private HashSet<String> keywords;
 	private HashMap<Item, Double> distances;
 
 	public Item(int itemNumber) {
 		this.itemNumber = itemNumber;
 		keywords = new HashSet<String>();
 		distances = new HashMap<Item, Double>();
 	}
 
 	public double getDistance(Item item) {
 		Double dist = distances.get(item);
 		return dist;
 	}
 
 	public void addKeyword(String keyword) {
 		keywords.add(keyword);
 	}
 
 	public void calcDistance(Item item, int numKeywords) {
		double mutualKeywords = 0;
 		for(String keyword : getKeywords()) {
 			if(item.getKeywords().contains(keyword)) mutualKeywords++;
 		}
 		double distance = mutualKeywords/numKeywords;
 		setDistance(item, distance);
 		item.setDistance(this, distance);
 	}
 
 	private void setDistance(Item item, double distance) {
 		distances.put(item, distance);
 	}
 
 	private HashSet<String> getKeywords() {
 		return keywords;
 	}
 	
 	@Override
 	public String toString() {
 		return "Item "+itemNumber+" keywords: "+keywords.size();
 	}
 
 }
