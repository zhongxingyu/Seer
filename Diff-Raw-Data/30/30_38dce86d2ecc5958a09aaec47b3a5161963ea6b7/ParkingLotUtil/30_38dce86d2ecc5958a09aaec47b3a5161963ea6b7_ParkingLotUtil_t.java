 package com.parking.main.utils;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.parking.main.model.ParkingLotInformation;
 import com.parking.main.model.ParkingLotInformation.Spot;
 import com.parking.main.model.ParkingLotInformation.Spot.Booking;
 
 public class ParkingLotUtil {
 	
 	public ParkingLotInformation getParkingLotInfoById (int id) {
 		
 		List<ParkingLotInformation> plist = getParkingLotsList();
 		
 		for (ParkingLotInformation plInfo : plist){
 			if (plInfo.getId() == id){
 				return plInfo;
 			}
 				
 		}
 		
 		return null;
 	}
 	
 	public List<ParkingLotInformation> getParkingLotsList (){
 
 		List<ParkingLotInformation> pLots = new ArrayList<ParkingLotInformation> ();
 		
		pLots.add(getPLData(0, "Diamond Parking Queen Anne",
 				"3161 Elliott Avenue #100 Seattle, WA 98121 (206) 447-7275",
 				"http://grfx.cstv.com/schools/utva/graphics/auto/DiamondParking.jpg",
 				5,
 				10.0
 				));
 		
		pLots.add(getPLData(1, "Mercer Street Garage",
 				"Lower Queen Anne, Seattle, WA 0.6 mi E  (206) 684-2489",
 				"http://grfx.cstv.com/schools/utva/graphics/auto/DiamondParking.jpg",
 				10,
 				14
 				));
 		
		pLots.add(getPLData(2, "Seattle Center 5th Ave N Parking Garage", 
 				"516 Harrison Street Seattle, WA 98109 (206) 684-7200", 
 				"http://grfx.cstv.com/schools/utva/graphics/auto/DiamondParking.jpg",
 				4, 
 				12
 				));
 		
		pLots.add(getPLData(3, "Standard Parking - Goat Hill Garage",
 				"415 6th Avenue Seattle, WA 98109 (206) 783-6199",
 				"http://grfx.cstv.com/schools/utva/graphics/auto/DiamondParking.jpg",
 				5,
 				18
 				));
  		
 		return pLots;
 	}
 
	private ParkingLotInformation getPLData(int id, String name, String addr, String url, int num, double rate) {
 		
		ParkingLotInformation pLot = new ParkingLotInformation(id);
 		pLot.setName(name);
 		pLot.setAddress(addr);
 		pLot.setPhotoURL(url);
 		pLot.setTotalSpots(num);
 		pLot.setRate(rate);
 		
 		pLot.setSpaces(getPSpaceData(pLot));
 		
 		return pLot;
 	}
 
 	private List<Spot> getPSpaceData(ParkingLotInformation pLot) {
 		
 		List<Spot> spots = new ArrayList<Spot>();
 		
 		Spot spt = pLot.new Spot();
 		
 		spt.setBookings(getBookingData(spt));
 		
 		spots.add(spt);
 		
 		return spots;
 	}
 
 	private List<Booking> getBookingData(Spot spt) {
 		List<Booking> bookings = new ArrayList<Booking>();
 		
 		Booking bkg = spt.new Booking();
 		
 		bookings.add(bkg);
 		
 		return bookings;
 	}
 
 }
