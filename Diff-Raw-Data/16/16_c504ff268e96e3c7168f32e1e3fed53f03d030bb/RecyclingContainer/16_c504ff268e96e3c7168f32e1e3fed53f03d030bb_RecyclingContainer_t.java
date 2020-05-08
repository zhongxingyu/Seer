 package de.grundid.recycling.merge;
 
 import java.util.List;
 
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

 import de.grundid.recycling.RecyclingData;
 
@JsonInclude(Include.NON_NULL)
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
