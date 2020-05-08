 package pl.michalorman.spu.service;
 
 import pl.michalorman.spu.model.Parcel;
 
 public class ParcelService {
 
 	public Parcel getParcel(String parcelId) {
 		Parcel parcel = new Parcel();
 		parcel.setId(parcelId);
 		parcel.setLatitude("54.49");
 		parcel.setLongitude("14.196");
 		return parcel;
 	}
 
 }
