 package com.villainrom.otaupdater.utility;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.os.Parcel;
 import android.os.Parcelable;
 
 public class Update implements Parcelable {
 	public String name;
 	public String description;
 	public String url;
 	public List<String> dependencyUpdateNames = new ArrayList<String>();
 
 	public Update() {
 	}
 	
 	@Override
 	public int describeContents() {
 		return 0;
 	}
 
 	@Override
 	public void writeToParcel(Parcel dest, int flags) {
 		dest.writeString(name);
 		dest.writeString(description);
 		dest.writeString(url);
 		dest.writeStringList(dependencyUpdateNames);
 	}
 
     public static final Parcelable.Creator<Update> CREATOR = new Parcelable.Creator<Update>() {
         @Override
 		public Update createFromParcel(Parcel in) {
             return new Update(in);
         }
 
         @Override
 		public Update[] newArray(int size) {
             return new Update[size];
         }
     };
     
    protected Update(Parcel in) {
     	name = in.readString();
     	description = in.readString();
     	url = in.readString();
     	in.readStringList(dependencyUpdateNames);
     }
     
     @Override
     public String toString() {
     	return name;
     }
 }
