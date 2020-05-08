 package se.chalmers.krogkollen.pub;
 import com.google.android.gms.maps.model.LatLng;
 /**
  * A class representing a pub
  * @author Jonathan Nilsfors
  *
  */
 public class Pub implements IPub{
 
 	private String name;
 	private String description;
 	private String openingHours;
 	private int ageRestriction;
 	private int queueTime;
 	private LatLng coordinates;
 	private final int ID;
 
 	/**
 	 * A constructor for creating a new pub
	 * @param name - the name of the pub
	 * @param description - a short description of the pub
	 * @param openingHours - the pubs opening hours
	 * @param ageRestriction - the age restriction
	 * @param queueTime- the present queue time
	 * @param latitude - the latitude coordinate
	 * @param longitude - the longitude coordinate
	 * @param id - the id of the pub
 	 */
 	public Pub(String name, String description, String openingHours, int ageRestriction, int queueTime, double latitude, double longitude, int id){
 		this.name = name;
 		this.description = description;
 		this.openingHours = openingHours;
 		this.ageRestriction = ageRestriction;
 		this.setQueueTime(queueTime);
 		this.ID = id;
 		coordinates = new LatLng(latitude, longitude);
 	}
 
 	@Override
 	public String getName() {
 		return this.name;
 	}
 
 	@Override
 	public String getDescription() {
 		return this.description;
 	}
 
 	@Override
 	public String getOpeningHours() {
 		return this.openingHours;
 	}
 
 	@Override
 	public int getAgeRestriction() {
 		return this.ageRestriction;
 	}
 
 	@Override
 	public int getQueueTime() {
 		return this.queueTime;
 	}
 
 	@Override
 	public int getQueueTimeLastUpdatedTimestamp() {
 		// TODO Auto-generated method stub
 		return 0;
 	}
 
 	@Override
 	public LatLng getCoordinates() {
 		return new LatLng(this.coordinates.latitude, this.coordinates.longitude);
 	}
 
 	@Override
 	public void setQueueTime(int queueTime) {
 		//No negative time allowed
 		if(queueTime < 0){
 			this.queueTime = 0;
 		}
 		else{
 			this.queueTime = queueTime;
 		}
 
 	}
 
 	@Override
 	public int getID() {
 		return this.ID;
 	}
 
 }
