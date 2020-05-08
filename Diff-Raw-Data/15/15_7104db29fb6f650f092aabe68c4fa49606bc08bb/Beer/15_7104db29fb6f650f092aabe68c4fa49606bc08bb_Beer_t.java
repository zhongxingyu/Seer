 package com.itllp.barleylegalhomebrewers.ontap;
 
 import com.itllp.barleylegalhomebrewers.ontap.util.EqualsUtil;
 
 public class Beer {
 
 	private final int id;
 	private String beerName;
 	private String brewerFirstName;
 	private String brewerLastName;
 	
 	public Beer(int id) {
 		this.id = id;
 	}
 
 	public int getId() {
 		return id;
 	}
 
 	public void setBeerName(String newName) {
 		beerName = newName;
 	}
 
 	public String getBeerName() {
 		return beerName;
 	}
 
 	@Override
 	public boolean equals(Object aThat) {
 		if ( this == aThat ) return true;
 		if ( !(aThat instanceof Beer) ) return false;
 		Beer that = (Beer)aThat;
 		return EqualsUtil.areEqual(this.id, that.id) 
 			&& EqualsUtil.areEqual(this.beerName, that.beerName)
 			&& EqualsUtil.areEqual(this.brewerFirstName, that.brewerFirstName)
 			&& EqualsUtil.areEqual(this.brewerLastName, that.brewerLastName)
 			;
 	}
 
 	@Override 
 	public int hashCode() {
 		return id;
 	  }
 
 	public String toString() {
 		return "id: " + id
 				+ " beerName: \"" + beerName + "\""
 				+ " brewerFirstName: " + brewerFirstName
 				+ " brewerLastName: " + brewerLastName
 				;
 	}
 
 	public void setBrewerFirstName(String name) {
 		brewerFirstName = name;
 	}
 	
 	public void setBrewerLastName(String name) {
 		brewerLastName = name;
 	}

	public String getBrewerName() {
		return brewerFirstName + " " + brewerLastName;
	}
 	
 }
