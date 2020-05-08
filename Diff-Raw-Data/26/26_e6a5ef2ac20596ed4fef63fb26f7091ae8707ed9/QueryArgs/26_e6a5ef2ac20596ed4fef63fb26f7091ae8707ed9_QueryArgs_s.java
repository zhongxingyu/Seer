 package com.tack.android.util;
 
 import android.net.Uri;
 import android.os.Parcel;
 import android.os.Parcelable;
 
 /**
  * Wrapper class for ContentProvider query params.
  * 
  * @author Joshua Jamison
  *
  */
 public class QueryArgs implements Parcelable {
   public Uri uri;
   public String[] projection;
   public String selection;
   public String[] selectionArgs;
   public String sortOrder;
 
   public QueryArgs(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
     this.uri = uri;
     this.projection = projection;
     this.selection = selection;
     this.selectionArgs = selectionArgs;
     this.sortOrder = sortOrder;
   }
   
   public QueryArgs(QueryArgs qa) {
     uri = qa.uri;
     projection = qa.projection;
     selection = qa.selection;
     selectionArgs = qa.selectionArgs;
     sortOrder = qa.sortOrder;
   }
 
   /* Parcelable implementation */
 
   public QueryArgs(Parcel parcel) {
     // Restore instance from parcel
     uri = parcel.readParcelable(Uri.class.getClassLoader());
    parcel.readStringArray(projection);
     selection = parcel.readString();
    parcel.readStringArray(selectionArgs);
     sortOrder = parcel.readString();
   }
 
   public static final Parcelable.Creator<QueryArgs> CREATOR = new Parcelable.Creator<QueryArgs>() {
     @Override
     public QueryArgs createFromParcel(Parcel source) {
       return new QueryArgs(source);
     }
 
     @Override
     public QueryArgs[] newArray(int size) {
       return new QueryArgs[size];
     }
   };
 
   @Override
   public void writeToParcel(Parcel dest, int flags) {
     // Store instance to parcel
     dest.writeParcelable(uri, flags);
    dest.writeStringArray(projection);
     dest.writeString(selection);
    dest.writeStringArray(selectionArgs);
     dest.writeString(sortOrder);
   }
 
   @Override
   public int describeContents() {
     return 0;
   }
 }
