 package fi.cosky.sdk;
 /*
  * This file is subject to the terms and conditions defined in
  * file 'LICENSE.txt', which is part of this source code package.
  */
 
 /**
  *
 *	LocationData contains support for addresses and coordinates.
 *  
  */
 public class LocationData extends BaseData {
 
 	private CoordinateData Coordinate;
     private AddressData Address;
     private int Id;
 
     public CoordinateData getCoordinate() {
 		return Coordinate;
 	}
 
 	public AddressData getAddress() {
 		return Address;
 	}
 
 	public void setAddress(AddressData address) {
 		Address = address;
 	}
 
 	public int getId() {
         return Id;
     }
 
     public void setId(int id) {
         Id = id;
     }
 
     public CoordinateData getCoordinatesData() {
         return Coordinate;
     }
 
     public void setCoordinatesData(CoordinateData coordinatesData) {
         Coordinate = coordinatesData;
     }
 
 
 }
