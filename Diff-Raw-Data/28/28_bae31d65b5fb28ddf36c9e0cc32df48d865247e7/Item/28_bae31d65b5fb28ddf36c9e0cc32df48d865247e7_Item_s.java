 package com.youpony.amuse;
 
 import java.io.Serializable;
 
 import android.os.Parcel;
 import android.os.Parcelable;
 
public class Item implements Serializable, Parcelable{
 	
 	String name, author, year, description, mostra, url, id ;
 	int e_id;
 	Item(){
 	}
 	
 	@Override
 	public String toString(){
 		return name;
 	}
 	
 	public int returnId(){
 		return e_id;
 	}
 	
 	public String toUrl(){
 		return url;
 	}
	
	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}
 
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		// TODO Auto-generated method stub
		
	}
 
 
 }
