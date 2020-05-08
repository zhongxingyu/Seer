 package org.tophat.android.mapping;
 
 import java.util.ArrayList;
 import java.util.Map;
 
 import android.os.Parcel;
 import android.os.Parcelable;
 
 /**
  * 
  * This class is mapping for the user 
  * @author Kevin
  *
  */
 public class Player extends Mapping implements Parcelable {
 
 	public static String API_URI = "players";
 	private String name;
 	private String photo = null;
 	private Game game;
 	private Team team;
 	private Double latitude = 0.0;
 	private Double longitude = 0.0;
 	
 	/**
 	 * Provided under the map key of "score" in the highest level, provided as an Integer.
 	 */
 	private Integer score = 0;
 
 	/**
 	 * @return the score
 	 */
 	public Integer getScore() {
 		return score;
 	}
 
 	/**
 	 * @param score the score to set
 	 */
 	public void setScore(Integer score) {
 		this.score = score;
 		
 		this.setAttribute("score", score);
 	}
 
 	/**
 	 * @return the time
 	 */
 	public String getTime() {
 		return time;
 	}
 
 	/**
 	 * @param time the time to set
 	 */
 	public void setTime(String time) {
 		this.time = time;
 		
 		this.setAttribute("time", time);
 	}
 
 	/**
 	 * @return the user
 	 */
 	public User getUser() {
 		return user;
 	}
 
 	/**
 	 * @param user the user to set
 	 */
 	public void setUser(User user) {
 		this.user = user;
 		
 		this.setAttribute("user", user.getMap());
 	}
 
 	/**
 	 * Provided under the map key of "time" in the highest level, as a date formatted in standard python datetime to String
 	 */
 	private String time = "";
 	
 	/**
 	 * Provided under the map key of "user" in the highest level.
 	 */
 	private User user;
 	
 	/**
 	 * 
 	 */
 	public Player()
 	{
 		super();
 		this.setAccessUrl(this.API_URI);
 	}
 	
 	/**
 	 * Setup the User object with the details supplied from the JSON from the platform.
 	 * @param player
 	 */
 	public Player(Map<String, Object> player)
 	{
 		super (player);
 		
 		if (player.containsKey("id"))
 			this.setId((Integer)player.get("id"));
 		
 		if (player.containsKey("time"))
 			this.setTime((String)player.get("time"));
 		
 		if (player.containsKey("longitude"))
 			this.setLongitude((Double)player.get("longitude"));
 		
 		if (player.containsKey("latitude"))
 			this.setLatitude((Double)player.get("latitude"));
 		
 		if (player.containsKey("name"))
 			this.setName((String)player.get("name"));
 		
 		if (player.containsKey("score"))
 			this.setScore((Integer)player.get("score"));
 		
 		if (player.containsKey("game"))
 			this.setGame(new Game((Map<String, Object>)player.get("game")));
 		
 		if (player.containsKey("user"))
 			this.setUser(new User((Map<String, Object>)player.get("user")));
 		
 		if (player.containsKey("team"))
 			this.setTeam(new Team((Map<String, Object>)player.get("team")));
 	}
 
 	/**
 	 * @return the name
 	 */
 	public String getName() {
 		return name;
 	}
 
 
 	/**
 	 * @param name the name to set
 	 */
 	public void setName(String name) {
 		this.name = name;
 		
 		this.setAttribute("name", name);
 	}
 
 	
 	// Parcelling part
 	public Player(Parcel in){
 		this.setId(in.readInt());
 		this.setName(in.readString());
 		this.setGame((Game)in.readParcelable(Game.class.getClassLoader()));
 		this.setLatitude(in.readDouble());
 		this.setLongitude(in.readDouble());
 		this.setScore(in.readInt());
 		this.setTime(in.readString());
 		this.setUser((User)in.readParcelable(Game.class.getClassLoader()));
 	}
 
 	@Override
 	public int describeContents() {
 		// TODO Auto-generated method stub
 		return 0;
 	}
 	
     /**
     *
     * This field is needed for Android to be able to
     * create new objects, individually or as arrays.
     *
     * This also means that you can use use the default
     * constructor to create the object and use another
     * method to hyrdate it as necessary.
     *
     * I just find it easier to use the constructor.
     * It makes sense for the way my brain thinks ;-)
     *
     */
    public static final Parcelable.Creator CREATOR =
    	new Parcelable.Creator() {
            public Player createFromParcel(Parcel in) {
                return new Player(in);
            }
 
            public Player[] newArray(int size) {
                return new Player[size];
            }
        };
 
 	@Override
 	public void writeToParcel(Parcel dest, int flags) {
 		dest.writeInt(this.getId());
 		dest.writeString(this.getName());
 		dest.writeParcelable(this.getGame(), 0);
 		dest.writeDouble(this.getLatitude());
 		dest.writeDouble(this.getLongitude());
 		dest.writeInt(this.getScore());
 		dest.writeString(this.getTime());
 		dest.writeParcelable(this.getUser(), 0);
 	}
 
 	/**
 	 * @return the longitude
 	 */
 	public Double getLongitude() {
 		return longitude;
 	}
 
 	/**
 	 * @param longitude the longitude to set
 	 */
 	public void setLongitude(Double longitude) {
 		this.longitude = longitude;
 		
 		this.setAttribute("longitude", longitude);
 	}
 
 	/**
 	 * @return the latitude
 	 */
 	public Double getLatitude() {
 		return latitude;
 	}
 
 	/**
 	 * @param latitude the latitude to set
 	 */
 	public void setLatitude(Double latitude) {
 		this.latitude = latitude;
 		
 		this.setAttribute("latitude", latitude);
 	}
 
 	/**
 	 * @return the game
 	 */
 	public Game getGame() {
 		return game;
 	}
 
 	/**
 	 * @param game the game to set
 	 */
 	public void setGame(Game game) {
 		this.game = game;
 		
		this.setAttribute("game", game.getMap());
 	}
 
 	/**
 	 * @return the photo
 	 */
 	public String getPhoto() {
 		return photo;
 	}
 
 	/**
 	 * @param photo the photo to set
 	 */
 	public void setPhoto(String photo) {
 		this.photo = photo;
 		
 		this.setAttribute("photo", photo);
 	}
 	
 	/**
 	 * @return the team
 	 */
 	public Team getTeam() {
 		return team;
 	}
 
 	/**
 	 * @param team the team to set
 	 */
 	public void setTeam(Team team) {
 		this.team = team;
 	}
 
 }
