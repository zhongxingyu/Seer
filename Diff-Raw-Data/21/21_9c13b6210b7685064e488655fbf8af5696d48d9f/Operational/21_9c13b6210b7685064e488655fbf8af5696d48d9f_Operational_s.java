 package models;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import org.joda.time.DateTime;
 import org.joda.time.Period;
 
 import play.modules.morphia.Model;
 
 import com.google.code.morphia.annotations.Embedded;
 import com.google.code.morphia.annotations.Entity;
 import com.google.code.morphia.annotations.Reference;
 import com.google.code.morphia.query.Query;
 
 @Entity
 public class Operational extends Model {
 	
 	public String bNo;
 	
 	public String status;
 	
 	@Embedded
 	public Vessel vessel;
 	
 	public String voyage;
 	
 	@Embedded
 	public Port port;
 	
 	@Embedded
 	public Customer customer;
 	
 	@Embedded
 	public Agent agent;
 	
 	public String statusAgent;
 	
 	@Embedded
 	public Agent subAgent;
 	
 	@Embedded
 	public Booking booking;
 	
 	@Embedded
 	public Berthing berthing;
 	
 	@Embedded
 	public Departure departure;
 	
 	@Embedded
 	public FinalCharge finalCharge;
 	
 	public Integer tugPtr;
 	public Double tugfix;
 	public Double tugvar;
 	
 	public Operational(Vessel vessel, String voyage, Port port,
 			Customer customer, Agent agent, String statusAgent, 
 			Agent subAgent) {
 		this.vessel = vessel;
 		this.voyage = voyage;
 		this.port = port;
 		this.customer = customer;
 		this.agent = agent;
 		this.statusAgent = statusAgent;
 		this.subAgent = subAgent;
 		this.status = "New";
 		this.tugPtr = this.searchIntegerArray(port.getTugTariff(), vessel.grt);
 		if(this.tugPtr == -1){
 			this.tugfix = 0.0;
 			this.tugvar = 0.0;
 		} else {
 			this.tugfix = port.costtariff.tug.get(this.tugPtr).fixed;
 			this.tugvar = port.costtariff.tug.get(this.tugPtr).var;
 		}
 	}
 	
 	public void oBooking(Date eta, Date etd, Double bookTugIn, 
 			Double bookTugOut, String cargo, int cargoWeight) {
 		this.booking = new Booking(eta, etd, bookTugIn, bookTugOut, cargo, cargoWeight);
 		this.status = "New";
 		this.booking.additional = new ArrayList();
 		eBookingExpenses();
 	}
 	
 	public void oBooking(Date eta, Date etd, Double bookTugIn, 
 			Double bookTugOut, String cargo, int cargoWeight, 
 			List<Additional> additional) {
 		this.booking = new Booking(eta, etd, bookTugIn, bookTugOut, cargo, cargoWeight);
 		this.status = "New";
 		this.booking.additional = additional;
 		eBookingExpenses();
 	}
 	
 	public void oBerthing(Date ata, Date etd, Double berthTugIn,
 			String cargo, int cargoWeight) {
 		this.berthing = new Berthing(ata, etd, berthTugIn, cargo, cargoWeight);
 		this.status = "Berthing";
 		this.booking.additional = new ArrayList();
 		eBerthingExpenses();
 	}
 	
 	public void oDeparture(Date atd, Double departTugOut, 
 			String cargo, int cargoWeight) {
 		this.departure = new Departure(atd, departTugOut, cargo, cargoWeight);
 		this.status = "Departure";
 		this.booking.additional = new ArrayList();
 		eDepartureExpenses();
 	}
 	
 	public void oFinalCharge(Double actualTugIn, Double actualTugOut,
 			String cargo, int cargoWeight) {
 		this.finalCharge = new FinalCharge(actualTugIn, actualTugOut, cargo, 
 				cargoWeight);
 		this.status = "Final";
 		this.booking.additional = new ArrayList();
 		aFinalCharges();
 	}
 	
 	public void eBookingExpenses() {
 		DateTime dtEta = new DateTime(booking.eta);
 		DateTime dtEtd = new DateTime(booking.etd);
 		Period days = new Period(dtEta, dtEtd);
 		int periodDay = days.toStandardDays().getDays();
 		
 		this.booking.harbor = vessel.grt * port.costtariff.harbour * periodHarbor(periodDay);
 		this.booking.queue = vessel.grt * port.costtariff.quay * periodDay;
 		this.booking.pilot = ((vessel.grt * port.costtariff.pilotvar) + 
 				port.costtariff.pilotfix) * 2;
 		this.booking.light = vessel.grt * port.costtariff.light;
 		this.booking.tug = ((vessel.grt * this.tugvar) + 
 				this.tugfix) * (booking.bookTugIn + booking.bookTugOut);
 		this.booking.gnt = (this.booking.harbor + this.booking.queue + this.booking.pilot + 
 				this.booking.tug) * 10 / 100;
 		
 	}
 	
 	public void eBerthingExpenses() {
 		DateTime dtAta = new DateTime(berthing.ata);
 		DateTime dtEtd = new DateTime(berthing.etd);
 		Period days = new Period(dtAta, dtEtd);
 		int periodDay = days.toStandardDays().getDays();
 		
 		this.berthing.harbor = vessel.grt * port.costtariff.harbour * periodHarbor(periodDay);
 		this.berthing.queue = vessel.grt * port.costtariff.quay * periodDay;
 		this.berthing.pilot = ((vessel.grt * port.costtariff.pilotvar) + 
 				port.costtariff.pilotfix) * 2;
 		this.berthing.light = vessel.grt * port.costtariff.light;
 		this.berthing.tug = ((vessel.grt * tugvar) + 
 				tugfix) * (berthing.berthTugIn + booking.bookTugOut);
 		this.berthing.gnt = (this.berthing.harbor + this.berthing.queue + this.berthing.pilot + 
 				this.berthing.tug) * 10 / 100;
 		
 	}
 	
 	public void eDepartureExpenses() {
 		DateTime dtAta = new DateTime(berthing.ata);
 		DateTime dtAtd = new DateTime(departure.atd);
 		Period days = new Period(dtAta, dtAtd);
 		int periodDay = days.toStandardDays().getDays();
 		
 		this.departure.harbor = vessel.grt * port.costtariff.harbour * periodHarbor(periodDay);
 		this.departure.queue = vessel.grt * port.costtariff.quay * periodDay;
 		this.departure.pilot = ((vessel.grt * port.costtariff.pilotvar) + 
 				port.costtariff.pilotfix) * 2;
 		this.departure.light = vessel.grt * port.costtariff.light;
 		this.departure.tug = ((vessel.grt * tugvar) + tugfix) 
 		* (berthing.berthTugIn + departure.departTugOut);
 		this.departure.gnt = (this.departure.harbor + this.departure.queue + this.departure.pilot + 
 				this.departure.tug) * 10 / 100;
 		
 	}
 	
 	public void aFinalCharges() {
 		DateTime dtAta = new DateTime(berthing.ata);
 		DateTime dtAtd = new DateTime(departure.atd);
 		Period days = new Period(dtAta, dtAtd);
 		int periodDay = days.toStandardDays().getDays();
 		
 		this.finalCharge.harbor = vessel.grt * port.costtariff.harbour * periodHarbor(periodDay);
 		this.finalCharge.queue = vessel.grt * port.costtariff.quay * periodDay;
 		this.finalCharge.pilot = ((vessel.grt * port.costtariff.pilotvar) + 
 				port.costtariff.pilotfix) * 2;
 		this.finalCharge.light = vessel.grt * port.costtariff.light;
 		this.finalCharge.tug = ((vessel.grt * tugvar) + tugfix) 
 		* (finalCharge.actualTugIn + finalCharge.actualTugOut);
 		this.finalCharge.gnt = (this.finalCharge.harbor + this.finalCharge.queue + 
 				this.finalCharge.pilot + this.finalCharge.tug) * 10 / 100;
 		
 	}
 	
 	public void approvalBooking(Boolean approve) {
 		if(approve) {
 			this.status = "Booking Approved";
 		} else {
 			this.status = "Booking Rejected";
 		}
 	}
 	
 	public void approvalBerthing(Boolean approve) {
 		if(approve) {
 			this.status = "Berthing Approved";
 		} else {
 			this.status = "Berthing Rejected";
 		}
 	}
 	
 	public void approvalDeparture(Boolean approve) {
 		if(approve) {
 			this.status = "Departure Approved";
 		} else {
 			this.status = "Departure Rejected";
 		}
 	}
 	
 	public void approvalFinal(Boolean approve) {
 		if(approve) {
 			this.status = "Final Approved";
 		} else {
 			this.status = "Final Reject";
 		}
 	}
 	
 	public Integer searchIntegerArray(Integer[] minimum, Integer grt) {
 		int bot = 0;
 		int top = minimum.length - 1;
 		if (top == 1) {
 			if(grt < minimum[1] && grt > minimum[0]) {return 0;}
 			else return -1;
 		} else {
 			while(bot <= top) {
				int mid = (bot + top) / 2;
				if(grt < minimum[mid+1] && grt > minimum[mid]) return mid;
				else if(grt < minimum[mid]) top = mid - 1;
				else if(grt > minimum[mid]) bot = mid + 1;
 			}
 			return -1;
 		}
 	}
 	
 	public Integer periodHarbor(int day) {
 		if (day >= 1 && day <= 10) {
 			return 1;
 		} else if (day >= 11 && day <= 20) {
 			return 2;
 		} else {
 			return 2;
 		}
 	}
 	
 }
