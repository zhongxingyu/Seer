 package edu.umn.pinkpanthers.beerfinder.data;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.google.android.maps.GeoPoint;
 
import android.location.Location;
 import android.os.Parcel;
 import android.os.Parcelable;
 
 /**
  * Model class for a Venue.
  * 
  */
 public class Venue implements Parcelable, Comparable<Venue> {
 
     public static String SELECTED_VENUE = "SELECTED_VENUE";
 
     private String venueId;
     private String name;
     private String address;
     private String phoneNumber;
     private GeoPoint location;
     private List<String> beerIds;
 
     public static final Parcelable.Creator<Venue> CREATOR = new Parcelable.Creator<Venue>() {
         public Venue createFromParcel(Parcel in) {
             return new Venue(in);
         }
 
         public Venue[] newArray(int size) {
             return new Venue[size];
         }
     };
 
     public Venue(Parcel in) {
         readFromParcel(in);
     }
 
     public Venue(String venueId, 
             String name, 
             String address, 
             String phoneNumber, 
             GeoPoint location, 
             List<String> beerIds) 
 {
     this.venueId = venueId;
     this.name = name;
     this.address = address;
     this.phoneNumber = phoneNumber;
     this.location = location;
     this.beerIds = beerIds;
 }
 
     @Override
     public void writeToParcel(Parcel dest, int flags) {
         dest.writeString(venueId);
         dest.writeString(name);
         dest.writeString(address);
         dest.writeString(phoneNumber);
        //dest.writeParcelable(location, flags);
         dest.writeList(beerIds);
     }
 
     private void readFromParcel(Parcel in) {
         venueId = in.readString();
         name = in.readString();
         address = in.readString();
         phoneNumber = in.readString();
        location = in.readParcelable(GeoPoint.class.getClassLoader());
         beerIds = new ArrayList<String>();
         in.readList(beerIds, String.class.getClassLoader());
     }
 
     public int compareTo(Venue otherVenue) {
         if (name != null && otherVenue.name != null) {
             return name.compareToIgnoreCase(otherVenue.name);
         }
         return 0;
     }
 
     @Override
     public int describeContents() {
         return 0;
     }
 
     public String getName() {
         return name;
     }
 
     public String getAddress() {
         return address;
     }
 
     public String getPhoneNumber() {
         return phoneNumber;
     }
 
     public GeoPoint getLocation() {
         return location;
     }
 
 }
