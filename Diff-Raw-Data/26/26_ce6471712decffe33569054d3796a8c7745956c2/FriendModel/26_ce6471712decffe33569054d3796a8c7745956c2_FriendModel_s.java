 package wei.mark.pingpongboss.misc.model;
 
 import android.os.Parcel;
 import android.os.Parcelable;
 
 public class FriendModel implements Parcelable {
 	String id;
 	String name;
 
 	public FriendModel(String id, String name) {
 		this.id = id;
 		this.name = name;
 	}
 
 	public FriendModel(Parcel in) {
 		id = in.readString();
 		name = in.readString();
 	}
 
 	public String getId() {
 		return id;
 	}
 
 	public void setId(String id) {
 		this.id = id;
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	public static final Parcelable.Creator<FriendModel> CREATOR = new Parcelable.Creator<FriendModel>() {
 		public FriendModel createFromParcel(Parcel in) {
 			return new FriendModel(in);
 		}
 
 		public FriendModel[] newArray(int size) {
 			return new FriendModel[size];
 		}
 	};
 
 	@Override
 	public int describeContents() {
 		return 0;
 	}
 
 	@Override
 	public void writeToParcel(Parcel dest, int flags) {
 		dest.writeString(id);
 		dest.writeString(name);
 	}
 }
