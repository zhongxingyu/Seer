 package de.grundid.recycling.merge;
 
 import java.util.List;
 
 import de.grundid.recycling.RecyclingData;
 
 public class RecyclingContainer {
 
 	private List<RecyclingData> recyclingData;
 	private List<RecyclingHn> recyclingHn;
 
 	public List<RecyclingData> getRecyclingData() {
 		return recyclingData;
 	}
 
 	public void setRecyclingData(List<RecyclingData> recyclingData) {
 		this.recyclingData = recyclingData;
 	}
 
 	public List<RecyclingHn> getRecyclingHn() {
 		return recyclingHn;
 	}
 
 	public void setRecyclingHn(List<RecyclingHn> recyclingHn) {
 		this.recyclingHn = recyclingHn;
 	}
 
 }
