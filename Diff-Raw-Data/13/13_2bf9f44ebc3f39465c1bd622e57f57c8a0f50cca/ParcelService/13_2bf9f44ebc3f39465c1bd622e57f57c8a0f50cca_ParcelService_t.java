 package pl.michalorman.spu.service;
 
import org.springframework.stereotype.Service;

 import pl.michalorman.spu.model.Parcel;
 
@Service
 public class ParcelService {
 
 	public Parcel getParcel(String parcelId) {
 		Parcel parcel = new Parcel();
 		parcel.setId(parcelId);
 		parcel.setLatitude("54.49");
 		parcel.setLongitude("14.196");
 		return parcel;
 	}
 
 }
