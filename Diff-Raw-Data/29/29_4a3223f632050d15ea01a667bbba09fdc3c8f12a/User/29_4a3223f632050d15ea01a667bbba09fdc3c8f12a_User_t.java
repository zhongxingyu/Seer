 package lastfm;
 import java.util.*;
 
 public class User {
 	private String ID, name, realName, country, gender;
 	private int age, playCount, playLists;
 	private Date registrationDate;
 	private HashMap<Track, ArrayList<String>> hsTracks;
 	
 	
 	public String getUserID() {
		return this.ID;
 	}
 	public void setUserID(String ID) {
 		this.ID = ID;
 	}
 	public String getName() {
		return this.name;
 	}
 	public void setName(String name) {
 		this.name = name;
 	}
 	public String getRealName() {
		return this.realName;
 	}
 	public void setRealname(String realName) {
 		this.realName = realName;
 	}
 	public String getCountry() {
		return this.country;
 	}
 	public void setCountry(String country) {
 		this.country = country;
 	}
 	public String getGender() {
		return this.gender;
 	}
 	public void setGender(String gender) {
 		this.gender = gender;
 	}
 	public int getAge() {
		return this.age;
 	}
 	public void setAge(int age) {
 		this.age = age;
 	}
 	public int getPlayCount() {
		return this.playCount;
 	}
 	public void setPlayCount(int playCount) {
 		this.playCount = playCount;
 	}
 	public int getPlayLists() {
		return this.playLists;
 	}
 	public void setPlayLists(int playLists) {
 		this.playLists = playLists;
 	}
 	public Date getRegistrationDate() {
		return this.registrationDate;
 	}
 	public void setRegistrationDate(Date registrationDate) {
 		this.registrationDate = registrationDate;
 	}
 	public HashMap<Track, ArrayList<String>> getHsTracks() {
		return this.hsTracks;
 	}
 	public void setHsTracks(HashMap<Track, ArrayList<String>> hsTracks) {
 		this.hsTracks = hsTracks;
 	}
 	
 	public void addTrack(Track track){
 		if(this.hsTracks.containsKey(track)){
 			ArrayList<String> timeOfP = this.hsTracks.get(track);
 			timeOfP.add(track.getTimeofPlay());
 			this.hsTracks.put(track, timeOfP);
 		}else{
 			ArrayList<String> timeOfP = new ArrayList<String>();
 			timeOfP.add(track.getTimeofPlay());
 			this.hsTracks.put(track, timeOfP);
 		}
 	}
 	
 	/*public boolean removeTrack(Track track){
 		return this.hsTracks.remove(track);
 	}	
 	*/
 	public boolean equals(Object o) {
 		if (o instanceof User)
 			return this.ID.equals(((User) o).getUserID());
 		else return false;
 	}
 	
 	public int hashCode() {
 		return this.ID.hashCode();
 	}	
 }
