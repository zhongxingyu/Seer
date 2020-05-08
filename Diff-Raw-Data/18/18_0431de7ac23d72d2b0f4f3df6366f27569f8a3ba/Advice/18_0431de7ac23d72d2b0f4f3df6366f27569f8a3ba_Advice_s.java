 package org.linnaeus.bean;
 
 import android.os.Parcel;
 import android.os.Parcelable;
 
 /**
  * Created by IntelliJ IDEA.
  * User: Romchee
  * Date: 19/07/11
  * Time: 17:19
  * To change this template use File | Settings | File Templates.
  */
 public class Advice implements Parcelable{
 
     public static final Parcelable.Creator<Advice> CREATOR = new
             Parcelable.Creator<Advice>() {
                 public Advice createFromParcel(Parcel in) {
                     return new Advice(in);
                 }
                     public Advice[] newArray(int size) {
                         return new Advice[size];
                     }
                 };
 
     private String name;
     private String description;
     private double rating;
 
     public Advice(Parcel parcel) {
         name = parcel.readString();
         description = parcel.readString();
         rating = parcel.readDouble();
     }
 
     public int describeContents() {
         return this.hashCode();
     }
 
     public void writeToParcel(Parcel parcel, int i) {
         parcel.writeString(name);
         parcel.writeString(description);
         parcel.writeDouble(rating);
     }
 
     public Advice() {
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
 
     public double getRating() {
         return rating;
     }
 
     public void setRating(double rating) {
         this.rating = rating;
     }
 
     @Override
     public String toString() {
        return name + "/n" + description
                + "/n" + "Rating: " + rating;
     }
 }
