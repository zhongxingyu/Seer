 package jp.knct.di.c6t.model;
 
 import java.util.LinkedList;
 import java.util.List;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.os.Parcel;
 import android.os.Parcelable;
 import android.util.Log;
 
 import com.google.android.gms.maps.model.LatLng;
 
 public class Route implements Parcelable {
 	private static final String NAME = "name";
 	private static final String START_LOCATION = "start_location";
 	private static final String DESCRIPTION = "description";
 	private static final String QUESTS = "quests";
 
 	public static Route parseJSONString(String route) throws JSONException {
 		return parseJSON(new JSONObject(route));
 	}
 
 	public static Route parseJSON(JSONObject routeJSON) throws JSONException {
 		String name = routeJSON.getString(NAME);
 		LatLng location = Quest.parseLocation(routeJSON.getString(START_LOCATION));
 		String description = routeJSON.getString(DESCRIPTION);
 		List<Quest> quests = Quest.parseQuests(routeJSON.getJSONArray(QUESTS));
 		return new Route(location, name, description, quests);
 	}
 
 	private String name;
 	private String description;
 	private List<Quest> quests;
	private double latitude;
	private double longitude;
 
 	public Route(LatLng startLocation, String name, String description, List<Quest> quests) {
 		this.setStartLocation(startLocation);
 		this.setName(name);
 		this.setDescription(description);
 		this.setQuests(quests);
 	}
 
 	public Route() {
 		this(null, "", "", new LinkedList<Quest>());
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
 		return new LatLng(latitude, longitude);
 	}
 
 	public void setStartLocation(LatLng startLocation) {
		latitude = startLocation.latitude;
		longitude = startLocation.longitude;
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
 
 	public JSONObject toJSON() {
 		try {
 			return new JSONObject()
 					.put(NAME, getName())
 					.put(START_LOCATION, Quest.serializeLocation(getStartLocation()))
 					.put(DESCRIPTION, getDescription())
 					.put(QUESTS, Quest.convertQuestsToJsonArray(getQuests()));
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
 		dest.writeString(getName());
 		dest.writeString(getDescription());
 		dest.writeDouble(getStartLocation().latitude);
 		dest.writeDouble(getStartLocation().longitude);
 		dest.writeTypedList(getQuests());
 	}
 
 	public static final Parcelable.Creator<Route> CREATOR = new Parcelable.Creator<Route>() {
 		@Override
 		public Route createFromParcel(Parcel source) {
 			String name = source.readString();
 			String description = source.readString();
 			Double latitude = source.readDouble();
 			Double longitude = source.readDouble();
 			List<Quest> quests = new LinkedList<Quest>();
 			source.readTypedList(quests, Quest.CREATOR);
 			LatLng location = new LatLng(latitude, longitude);
 			return new Route(location, name, description, quests);
 		}
 
 		@Override
 		public Route[] newArray(int size) {
 			// TODO Auto-generated method stub
 			return new Route[size];
 		}
 	};
 }
