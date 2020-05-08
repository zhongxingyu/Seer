 package org.hermitcrab.entity;
 
 import android.os.Parcel;
 import android.os.Parcelable;
 
 import com.google.gson.annotations.SerializedName;
 
 public class Software implements Parcelable {
 	@SerializedName("ID")
 	public String id;
 
 	@SerializedName("Name")
 	public String name;
 
 	@SerializedName("Url")
 	public String url;
 
 	@SerializedName("RelativeUrl")
 	public String relativeUrl;
 
 	@SerializedName("Votes")
 	public int votes;
 
 	@SerializedName("IconUrl")
 	public String iconUrl;
 
 	@SerializedName("Platforms")
 	public String[] platforms;
 
 	@SerializedName("License")
 	public String license;
 
 	@SerializedName("ShortDescription")
 	public String shortDescription;
 
 	@SerializedName("Tags")
 	public String[] tags;
 
 	@SerializedName("Items")
 	public Software[] items;
 
 	@Override
 	public int describeContents() {
 		return 0;
 	}
 
 	@Override
 	public void writeToParcel(Parcel dest, int flags) {
 		dest.writeString(id);
 		dest.writeString(name);
 		dest.writeString(url);
 		dest.writeString(relativeUrl);
 		dest.writeInt(votes);
 		dest.writeString(iconUrl);
 		dest.writeStringArray(platforms);
 		dest.writeString(license);
 		dest.writeString(shortDescription);
 		dest.writeStringArray(tags);
 		dest.writeParcelableArray(items, flags);
 	}
 
 	public static final Parcelable.Creator<Software> CREATOR = new Creator<Software>() {
 
 		@Override
 		public Software[] newArray(int size) {
 			return new Software[size];
 		}
 
 		@Override
 		public Software createFromParcel(Parcel source) {
 			return new Software(source);
 		}
 	};
 
 	public Software(Parcel in) {
 		id = in.readString();
 		name = in.readString();
 		url = in.readString();
 		relativeUrl = in.readString();
 		votes = in.readInt();
 		iconUrl = in.readString();
		in.readStringArray(platforms);
 		license = in.readString();
 		shortDescription = in.readString();
		in.readStringArray(tags);
 		Parcelable[] parcels = in.readParcelableArray(Software.class
 				.getClassLoader());
		items = new Software[parcels.length];
		for (int i = 0; i < parcels.length; i++) {
			items[i] = (Software) parcels[i];
 		}
 
 	}
 }
