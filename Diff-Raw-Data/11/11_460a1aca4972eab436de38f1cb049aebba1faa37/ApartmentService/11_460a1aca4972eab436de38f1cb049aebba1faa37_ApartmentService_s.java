 package com.hansson.rento;
 
 import java.text.DateFormat;
 import java.util.Date;
 import java.util.LinkedList;
 import java.util.List;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.scheduling.annotation.Scheduled;
 import org.springframework.stereotype.Service;
 
 import com.google.gson.Gson;
 import com.hansson.rento.apartments.ApartmentsInterface;
 import com.hansson.rento.apartments.BengtAkessonsApartments;
 import com.hansson.rento.apartments.CAFastigheterApartments;
 import com.hansson.rento.apartments.HeimstadenApartments;
 import com.hansson.rento.apartments.KSFastigheterApartments;
 import com.hansson.rento.apartments.KarlskronahemApartments;
 import com.hansson.rento.apartments.MagistratusFastigheterApartments;
 import com.hansson.rento.apartments.PBAApartments;
 import com.hansson.rento.apartments.SBFApartments;
 import com.hansson.rento.apartments.TrossoWamoApartments;
 import com.hansson.rento.apartments.UtklippanApartments;
 import com.hansson.rento.dao.ApartmentDAO;
 import com.hansson.rento.entities.Apartment;
 
 @Service
 public class ApartmentService {
 	
 	// * ********CAUTION********
 	// * Entering html scraping area.. prepare yourself for some nasty stuff!
 	// * ********CAUTION********
 	private static final Logger mLog = LoggerFactory.getLogger("rento");
 	private List<ApartmentsInterface> mLandlords = new LinkedList<ApartmentsInterface>() {
 
 		/**
 		 * 
 		 */
 		private static final long serialVersionUID = -2411798345463453006L;
 		// Add new implementations of the ApartmentsInterface here to include them in the scan loop
 		{
 			add(new HeimstadenApartments());
 			add(new CAFastigheterApartments());
 			add(new TrossoWamoApartments());
 			add(new KarlskronahemApartments());
 			add(new BengtAkessonsApartments());
 			add(new KSFastigheterApartments());
 			add(new MagistratusFastigheterApartments());
 			add(new PBAApartments());
 			add(new SBFApartments());
 			add(new UtklippanApartments());
 
 		}
 	};
 	
 	@Autowired
 	private ApartmentDAO apartmentDAO;
 
 	@Scheduled(fixedDelayString = "3600000")
 	public void updateApartmentList() {
		apartmentDAO.createTable();
 		Apartment apartment = new Apartment();
 		apartment.setAddress("asdsad");
 		apartment.setArea("asdasd");
 		apartment.setCity("asdasd");
 		apartment.setIdentifier("asdasd");
 		apartment.setLandlord("asdasd");
 		apartment.setRent(324);
 		apartment.setRooms(234.23);
 		apartment.setSize(3);
 		apartment.setUrl("asdasd");
 		apartmentDAO.create(apartment);
 //		List<Apartment> apartmentList = new LinkedList<Apartment>();
 //		for (ApartmentsInterface currentLandlord : mLandlords) {
 //			mLog.info("Processing " + currentLandlord.getClass().getSimpleName());
 //			apartmentList.addAll(currentLandlord.getAvailableApartments());
 //		}
 //		apartmentDAO.create(null);
 	}
 }
