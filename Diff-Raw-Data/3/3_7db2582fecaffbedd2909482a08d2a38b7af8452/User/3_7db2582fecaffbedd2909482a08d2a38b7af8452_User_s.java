 package jp.knct.di.c6t.model;
 
 import java.util.LinkedList;
 import java.util.List;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.os.Parcel;
 import android.os.Parcelable;
 import android.util.Log;
 
 public class User implements Parcelable {
 
 	private static final String ID = "id";
 	private static final String NAME = "name";
 	private static final String AREA = "area";
 
 	public static List<User> parseUsers(JSONArray users) throws JSONException {
 		List<User> userList = new LinkedList<User>();
 		for (int i = 0; i < users.length(); i++) {
 			User user = parseJSON(users.getJSONObject(i));
 			userList.add(user);
 		}
 		return userList;
 	}
 
 	public static JSONArray convertUsersToJsonArray(List<User> users) {
 		JSONArray userList = new JSONArray();
 		for (User user : users) {
 			userList.put(user.toJSON());
 		}
 		return userList;
 	}
 
 	public static User parseJSONString(String user) throws JSONException {
 		return parseJSON(new JSONObject(user));
 	}
 
 	public static User parseJSON(JSONObject user) throws JSONException {
 		int id = user.getInt(ID);
 		String name = user.getString(NAME);
 		String area = user.getString(AREA);
 		return new User(name, area, id);
 	}
 
 	private int id = -1;
 	private String name;
 	private String area;
 
 	public User(String name, String area, int id) {
 		setId(id);
 		setName(name);
 		setArea(area);
 	}
 
 	public User(String name, String area) {
 		this(name, area, -1);
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
 
 	public String getArea() {
 		return area;
 	}
 
 	public void setArea(String area) {
 		this.area = area;
 	}
 
 	public JSONObject toJSON() {
 		try {
 			return new JSONObject()
 					.put(ID, getId())
 					.put(NAME, getName())
 					.put(AREA, getArea());
 		}
 		catch (JSONException e) {
 			e.printStackTrace();
 			Log.d("User", "JSON Error!!!!!!!!!!!!!!!!");
 			return null;
 		}
 	}
 
 	@Override
 	public int describeContents() {
 		// TODO Auto-generated method stub
 		return 0;
 	}
 
 	@Override
 	public void writeToParcel(Parcel dest, int flags) {
 		dest.writeInt(getId());
 		dest.writeString(getName());
 		dest.writeString(getArea());
 	}
 
 	public static final Parcelable.Creator<User> CREATOR = new Parcelable.Creator<User>() {
 		@Override
 		public User createFromParcel(Parcel source) {
 			int id = source.readInt();
			String area = source.readString();
 			String name = source.readString();
 			return new User(name, area, id);
 		}
 
 		@Override
 		public User[] newArray(int size) {
 			// TODO Auto-generated method stub
 			return new User[size];
 		}
 	};
 }
