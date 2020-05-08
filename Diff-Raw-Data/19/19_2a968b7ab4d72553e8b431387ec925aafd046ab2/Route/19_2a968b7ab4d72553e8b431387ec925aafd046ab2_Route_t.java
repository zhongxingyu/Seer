 package jp.knct.di.c6t.model;
 
 import java.text.ParseException;
 import java.util.Date;
 import java.util.LinkedList;
 import java.util.List;
 
 import jp.knct.di.c6t.util.MapUtil;
 import jp.knct.di.c6t.util.TimeUtil;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.os.Parcel;
 import android.os.Parcelable;
 import android.util.Log;
 
 import com.google.android.gms.maps.model.LatLng;
 
 public class Route implements Parcelable {
 
 	private static final String ID = "id";
 	private static final String NAME = "name";
 	private static final String START_LOCATION = "start_location";
 	private static final String DESCRIPTION = "description";
 	private static final String QUESTS = "quests";
 	private static final String USER = "user";
 	private static final String PLAYED_COUNT = "played_count";
 	private static final String ACHIEVEMENT_COUNT = "achievement_count";
 	private static final String CREATED_AT = "created_at";
 
 	public static List<Route> parseRoutes(JSONArray routes) throws JSONException, ParseException {
 		List<Route> routeList = new LinkedList<Route>();
 		for (int i = 0; i < routes.length(); i++) {
 			Route route = parseJSON(routes.getJSONObject(i));
 			routeList.add(route);
 		}
 		return routeList;
 	}
 
 	public static Route parseJSONString(String route) throws JSONException, ParseException {
 		return parseJSON(new JSONObject(route));
 	}
 
 	public static Route parseJSON(JSONObject routeJSON) throws JSONException, ParseException {
 		int id = routeJSON.getInt(ID);
 		String name = routeJSON.getString(NAME);
 		LatLng location = MapUtil.parseLocation(routeJSON.getString(START_LOCATION));
 		String description = routeJSON.getString(DESCRIPTION);
 		List<Quest> quests = Quest.parseQuests(routeJSON.getJSONArray(QUESTS));
 		User user = User.parseJSON(routeJSON.getJSONObject(USER));
 		int playedCount = routeJSON.getInt(PLAYED_COUNT);
 		int achievedCount = routeJSON.getInt(ACHIEVEMENT_COUNT);
 		Date createdAt = TimeUtil.parse(routeJSON.getString(CREATED_AT));
 		return new Route(location, name, description, quests, id, user, playedCount, achievedCount, createdAt);
 	}
 
 	private int id = -1;
 	private String name;
 	private String description;
 	private List<Quest> quests;
 	private double latitude = INITIAL_LAT_LNG_VALUE;
 	private double longitude = INITIAL_LAT_LNG_VALUE;
 	private static double INITIAL_LAT_LNG_VALUE = 0xfffff;
 	private User user;
 	private int playedCount;
 	private int achievedCount;
 	private Date createdAt;
 
 	public Route(LatLng startLocation, String name, String description, List<Quest> quests, int id, User user, int playedCount, int achievedCount, Date createdAt) {
 		setId(id);
 		setStartLocation(startLocation);
 		setName(name);
 		setDescription(description);
 		setQuests(quests);
 		setUser(user);
 		setPlayedCount(playedCount);
 		setAchievedCount(achievedCount);
 		setCreatedAt(createdAt);
 	}
 
 	public Route(LatLng startLocation, String name, String description, List<Quest> quests, int id) {
 		this(startLocation, name, description, quests, id, null, 0, 0, null);
 	}
 
 	public Route(LatLng startLocation, String name, String description, List<Quest> quests) {
 		this(startLocation, name, description, quests, -1);
 	}
 
 	public Route() {
 		this(null, "", "", new LinkedList<Quest>());
 	}
 
 	public int getId() {
 		return id;
 	}
 
 	public void setId(int id) {
 		this.id = id;
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	public String getDescription() {
 		return description;
 	}
 
 	public void setDescription(String description) {
 		this.description = description;
 	}
 
 	public LatLng getStartLocation() {
 		if (latitude == INITIAL_LAT_LNG_VALUE || longitude == INITIAL_LAT_LNG_VALUE) {
 			return null;
 		}
 		return new LatLng(latitude, longitude);
 	}
 
 	public void setStartLocation(LatLng startLocation) {
 		if (startLocation == null) {
 			latitude = INITIAL_LAT_LNG_VALUE;
 			longitude = INITIAL_LAT_LNG_VALUE;
 		}
 		else {
 			latitude = startLocation.latitude;
 			longitude = startLocation.longitude;
 		}
 	}
 
 	public List<Quest> getQuests() {
 		return quests;
 	}
 
 	public void setQuests(List<Quest> quests) {
 		this.quests = quests;
 	}
 
 	public void addQuest(Quest quest) {
 		this.quests.add(quest);
 	}
 
 	public User getUser() {
 		return user;
 	}
 
 	public void setUser(User user) {
 		this.user = user;
 	}
 
 	public int getPlayedCount() {
 		return playedCount;
 	}
 
 	public void setPlayedCount(int playedCount) {
 		this.playedCount = playedCount;
 	}
 
 	public int getAchievedCount() {
 		return achievedCount;
 	}
 
 	public void setAchievedCount(int achievedCount) {
 		this.achievedCount = achievedCount;
 	}
 
 	public Date getCreatedAt() {
 		return createdAt;
 	}
 
 	public void setCreatedAt(Date createdAt) {
 		this.createdAt = createdAt;
 	}
 
 	public JSONObject toJSON() {
 		User user = getUser();
		JSONObject userJSON = (user == null) ?
				null :
				user.toJSON();

		Date date = getCreatedAt();
		String dateString = TimeUtil.format((date == null) ?
				new Date(0) :
				date);

 		try {
 			return new JSONObject()
 					.put(ID, getId())
 					.put(NAME, getName())
 					.put(START_LOCATION, MapUtil.serializeLocation(getStartLocation()))
 					.put(DESCRIPTION, getDescription())
 					.put(QUESTS, Quest.convertQuestsToJsonArray(getQuests()))
 					.put(USER, userJSON)
 					.put(PLAYED_COUNT, getPlayedCount())
 					.put(ACHIEVEMENT_COUNT, getAchievedCount())
					.put(CREATED_AT, dateString);
 		}
 		catch (JSONException e) {
 			e.printStackTrace();
 			Log.d("Route", "JSON Error!!!!!!!!!!!!!!!!");
 			return null;
 		}
 	}
 
 	public boolean isValid() {
 		// TODO Auto-generated method stub
 		return true;
 	}
 
 	@Override
 	public int describeContents() {
 		// TODO Auto-generated method stub
 		return 2;
 	}
 
 	@Override
 	public void writeToParcel(Parcel dest, int flags) {
 		dest.writeInt(getId());
 		dest.writeString(getName());
 		dest.writeString(getDescription());
 		dest.writeDouble(getStartLocation().latitude);
 		dest.writeDouble(getStartLocation().longitude);
 		dest.writeTypedList(getQuests());
 		dest.writeParcelable(getUser(), -1);
 		dest.writeInt(getPlayedCount());
 		dest.writeInt(getAchievedCount());
 		dest.writeSerializable(getCreatedAt());
 	}
 
 	public static final Parcelable.Creator<Route> CREATOR = new Parcelable.Creator<Route>() {
 		@Override
 		public Route createFromParcel(Parcel source) {
 			int id = source.readInt();
 			String name = source.readString();
 			String description = source.readString();
 			Double latitude = source.readDouble();
 			Double longitude = source.readDouble();
 			List<Quest> quests = new LinkedList<Quest>();
 			source.readTypedList(quests, Quest.CREATOR);
 			LatLng location = new LatLng(latitude, longitude);
 			User user = source.readParcelable(User.class.getClassLoader());
 			int playedCount = source.readInt();
 			int achievedCount = source.readInt();
 			Date createdAt = (Date) source.readSerializable();
 			return new Route(location, name, description, quests, id, user, playedCount, achievedCount, createdAt);
 		}
 
 		@Override
 		public Route[] newArray(int size) {
 			// TODO Auto-generated method stub
 			return new Route[size];
 		}
 	};
 }
