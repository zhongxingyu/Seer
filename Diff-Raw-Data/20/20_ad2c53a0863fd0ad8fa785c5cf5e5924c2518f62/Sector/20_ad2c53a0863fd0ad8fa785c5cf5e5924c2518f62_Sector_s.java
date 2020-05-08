 package com.uruwolf.vominer.data;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * Contains basic location information for a specific sector
  * @author Steve "Uru" West <uruwolf@gmail.com>
  */
 public class Sector {
 	private int id;
 	private String system;
	private String aplhaCoord;
 	private String numCoord;
 	private String notes;
 	private final List<Mineral> mineralList;
 	
 	/**
 	 * Creates an empty sector
 	 */
 	public Sector(){
 		this("","","", -1, "");
 	}
 	
 	public Sector(String system, String alpha, String num, int id, String notes){
 		mineralList = new ArrayList<Mineral>();
 		
 		setSystem(system);
 		setAplhaCoord(alpha);
 		setNumCoord(num);
 		setId(id);
 		setNotes(notes);
 	}
 	
 	/**
 	 * @return the system
 	 */
 	public String getSystem() {
 		return system;
 	}
 	/**
 	 * @param system the system to set
 	 */
 	public void setSystem(String system) {
 		this.system = system;
 	}
 	/**
 	 * @return the aplhaCoord
 	 */
 	public String getAplhaCoord() {
		return aplhaCoord;
 	}
 	/**
 	 * @param aplhaCoord the aplhaCoord to set
 	 */
	public void setAplhaCoord(String aplhaCoord) {
		this.aplhaCoord = aplhaCoord;
 	}
 	/**
 	 * @return the numCoord
 	 */
 	public String getNumCoord() {
 		return numCoord;
 	}
 	/**
 	 * @param numCoord the numCoord to set
 	 */
 	public void setNumCoord(String numCoord) {
 		this.numCoord = numCoord;
 	}
 
 	/**
 	 * @return the notes
 	 */
 	public String getNotes() {
 		return notes;
 	}
 
 	/**
 	 * @param notes the notes to set
 	 */
 	public void setNotes(String notes) {
 		this.notes = notes;
 	}
 
 	/**
 	 * @return the id
 	 */
 	public int getId() {
 		return id;
 	}
 
 	/**
 	 * @param id the id to set
 	 */
 	public void setId(int id) {
 		this.id = id;
 	}
 	
 	public void addMineral(Mineral newMineral){
 		newMineral.setSector(this);
 		mineralList.add(newMineral);
 	}
 	
 	public void removeMineral(Mineral toRemove){
 		toRemove.setSector(null);
 		mineralList.remove(toRemove);
 	}
 	
 	public List<Mineral> getMinerals(){
 		return mineralList;
 	}
 }
