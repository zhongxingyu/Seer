 
 package pl.pwr.smartkill.obj;
 
 
 public class Position{
    	private boolean disqualification;
    	private String lat;
    	private String lng;
    	private Number match;
    	private Number offense;
    	private Number points_hunter;
    	private Number points_prey;
    	private String type;
    	private String updated_at;
    	private Number user;
   	private Profile profile;
   	
   	public Profile getProfile() {
		return profile;
	}
   	public void setProfile(Profile profile) {
		this.profile = profile;
	}
 
  	public boolean getDisqualification(){
 		return this.disqualification;
 	}
 	public void setDisqualification(boolean disqualification){
 		this.disqualification = disqualification;
 	}
  	public String getLat(){
 		return this.lat;
 	}
 	public void setLat(String lat){
 		this.lat = lat;
 	}
  	public String getLng(){
 		return this.lng;
 	}
 	public void setLng(String lng){
 		this.lng = lng;
 	}
  	public Number getMatch(){
 		return this.match;
 	}
 	public void setMatch(Number match){
 		this.match = match;
 	}
  	public Number getOffense(){
 		return this.offense;
 	}
 	public void setOffense(Number offense){
 		this.offense = offense;
 	}
  	public Number getPoints_hunter(){
 		return this.points_hunter;
 	}
 	public void setPoints_hunter(Number points_hunter){
 		this.points_hunter = points_hunter;
 	}
  	public Number getPoints_prey(){
 		return this.points_prey;
 	}
 	public void setPoints_prey(Number points_prey){
 		this.points_prey = points_prey;
 	}
  	public String getType(){
 		return this.type;
 	}
 	public void setType(String type){
 		this.type = type;
 	}
  	public String getUpdated_at(){
 		return this.updated_at;
 	}
 	public void setUpdated_at(String updated_at){
 		this.updated_at = updated_at;
 	}
  	public Number getUser(){
 		return this.user;
 	}
 	public void setUser(Number user){
 		this.user = user;
 	}
 }
