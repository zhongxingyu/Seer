 /*******************************************************************************
  * Copyleft 2012 Massimiliano Leone - massimiliano.leone@iubris.net .
  * 
  * Location.java is part of 'Socrates'
  * 
  * 'Socrates' is free software; you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as published by
  * the Free Software Foundation; either version 3 of the License, or
  * (at your option) any later version.
  * 
  * 'Socrates' is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Lesser General Public License for more details.
  * 
  * You should have received a copy of the GNU Lesser General Public License
  * along with 'Socrates' ; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301 USA
  ******************************************************************************/
 package net.iubris.socrates.model.http.response.data.geocoding;
 
 import com.google.android.maps.GeoPoint;
 import com.google.api.client.util.Key;
 
 public class Location {
 	
 	@Key("lat")
 	private double latitude;
 	@Key("lng")
 	private double longitude;
 	public double getLatitude() {
 		return latitude;
 	}
 	public double getLongitude() {
 		return longitude;
 	}	
	public GeoPoint toGeoPoint() {
 		return new GeoPoint((int)(latitude*1E6), (int)(longitude*1E6));
 	}
 	/*
 	public String toString() {
 		return Verboser.reflectiveToString(this);
 	}*/
 }
