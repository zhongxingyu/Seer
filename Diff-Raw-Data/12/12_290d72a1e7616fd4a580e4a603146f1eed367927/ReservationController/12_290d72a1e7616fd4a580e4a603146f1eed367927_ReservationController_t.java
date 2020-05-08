 package sse.client.controller;
 
 import sse.client.dto.RoomReservationDTO;
 import sse.client.util.RoomSortByNumber;
 import sse.ejb.ReservationService;
 import sse.ejb.dao.CustomerDAO;
 import sse.ejb.dao.ReservationDAO;
 import sse.ejb.dao.RoomDAO;
 import sse.model.Customer;
 import sse.model.Reservation;
 import sse.model.Room;
 
 import javax.ejb.EJB;
 import javax.faces.application.FacesMessage;
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.SessionScoped;
 import javax.faces.context.FacesContext;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Date;
 import java.util.List;
 
 @ManagedBean(name = "reservationCtrl")
 @SessionScoped
 public class ReservationController {
 	
 	private List<Room> freeRooms;
 	private List<Customer> customers;
 	private Room selectedRoom;
 	private RoomReservationDTO roomToRemove;
 	
 	private Customer selectedCustomer;
 	private Double selectedDiscount;
 	private List<RoomReservationDTO> selectedRoomReservations;
 	private String filterRoomTxt;
 	private String filterCustTxt;
 
 	private Date fromDate;
 	private Date toDate;
 
     private List<Reservation> arrivals;
     private List<Reservation> departures;
 
     private List<Reservation> reservationsForSelectedCustomer;
     private Reservation reservationToRemove;
     private Reservation reservationToEdit;
 
 	@EJB
 	ReservationService reservationService;
 	
 	@EJB
 	private RoomDAO roomDAO;
 	
 	@EJB 
 	CustomerDAO customerDAO;
 	
 	@EJB
 	ReservationDAO reservationDAO;
 
 	public ReservationController() {
 		selectedRoomReservations = new ArrayList<RoomReservationDTO>();
 		selectedDiscount = new Double(0d);
 		freeRooms = new ArrayList<Room>();
 		
 	}
 
 	public Room getSelectedRoom() {
 		return selectedRoom;
 	}
 
 	public void setSelectedRoom(Room selectedRoom) {
 
 		RoomReservationDTO r = new RoomReservationDTO(selectedRoom);
 		selectedRoomReservations.add(r);
 		freeRooms.remove(selectedRoom);
 		this.selectedRoom = selectedRoom;
 	}
 
 	public Customer getSelectedCustomer() {
 		return selectedCustomer;
 	}
 
 	public void setSelectedCustomer(Customer selectedCustomer) {
 		this.selectedCustomer = selectedCustomer;
 		this.reservationsForSelectedCustomer = reservationService.getReservationsForCustomer(selectedCustomer);
 		
 	}
 
 	public List<Room> getFreeRooms() {
 
 		return freeRooms;
 	}
 
 	public void setFreeRooms(List<Room> freeRooms) {
 		this.freeRooms = freeRooms;
 	}
 
 	public Date getFromDate() {
 		return fromDate;
 	}
 
 	public void setFromDate(Date fromDate) {
 		this.fromDate = fromDate;
 	}
 
 	public Date getToDate() {
 		return toDate;
 	}
 
 	public void setToDate(Date toDate) {
 		this.toDate = toDate;
 	}
 
 	public List<Customer> getCustomers() {
 		return customers;
 	}
 
 	public void setCustomers(List<Customer> customers) {
 		this.customers = customers;
 	}
 	
 
 	public List<RoomReservationDTO> getSelectedRoomReservations() {
 		return selectedRoomReservations;
 	}
 
 	public void setSelectedRoomReservations(List<RoomReservationDTO> selectedRoomReservations) {
 		this.selectedRoomReservations = selectedRoomReservations;
 	}
 		
 
 	public RoomReservationDTO getRoomToRemove() {
 		return roomToRemove;
 	}
 
 	public void setRoomToRemove(RoomReservationDTO roomToRemove) {
 		selectedRoomReservations.remove(roomToRemove);
 		freeRooms.add(roomToRemove.getRoom());
 		Collections.sort(freeRooms, new RoomSortByNumber());
 		this.roomToRemove = roomToRemove;
 	}
 
 	public Double getSelectedDiscount() {
 		return selectedDiscount;
 	}
 
 	public void setSelectedDiscount(Double selectedDiscount) {
 		this.selectedDiscount = selectedDiscount;
 	}
 	
 	public String getFilterCustTxt() {
 		return filterCustTxt;
 	}
 
 	public void setFilterCustTxt(String filterCustTxt) {
 		this.filterCustTxt = filterCustTxt;
 	}
 
 	public String getFilterRoomTxt() {
 		return filterRoomTxt;
 	}
 
 	public void setFilterRoomTxt(String filterRoomTxt) {
 		this.filterRoomTxt = filterRoomTxt;
 	}
 
 	public String findFreeRooms() {
 		
 		//TODO ad message
 		if(fromDate != null && toDate !=  null){
 			freeRooms = reservationService.getFreeRoomsInTimespan(fromDate,  toDate);
 		}
 		return "";
 	}
 
 	public String doReservation(){
 		for ( RoomReservationDTO res: selectedRoomReservations) {
 			System.out.println("Roomid: " + res.getRoom().getId() + " selectedRate: " + res.getSelectedRate());
 			Reservation reservation = new Reservation();
 			reservation.setCustomer(selectedCustomer);
 			reservation.setRoom(res.getRoom());
 			reservation.setDiscount(selectedDiscount);
 			reservation.setRoomRate(res.getSelectedRate());
 			reservation.setOccupacyAdult(res.getOccupacyAdult());
 			reservation.setOccupacyChild(res.getOccupacyChild());
 			reservation.setFromDate(fromDate);
 			reservation.setToDate(toDate);
			reservation.setProcessed(false);
 			
 			reservationService.doReservation(reservation);
 			String msg = "Reservation saved";
 			FacesContext.getCurrentInstance().addMessage(null,
 					new FacesMessage(msg, msg));
 			
 		}
 		
 		selectedRoomReservations.clear();
 		selectedCustomer = null;
 		fromDate = null;
 		toDate = null;
 		selectedDiscount = 0d;
 		freeRooms.clear();
 				
 		return "";
 	}
 	
 	//TODO write a validator?
 	public Boolean getAllowSubmit(){
 		Boolean allow = true;
 		if (selectedRoomReservations.isEmpty()){
 			allow = false;
 		}
 		else{
 			for (RoomReservationDTO roomreservation : selectedRoomReservations) {
 				if(roomreservation.getSelectedRate() == null ){
 					allow = false;
 				}					
 			}
 		}
 		
 		if(toDate == null || fromDate == null || selectedCustomer == null){
 			allow = false;
 		}
 		return !allow;
 	}
 	
 	//TODO filter einbauen??
 	public List<Room> filterRooms() {
 		return null;
 	}
 	
 	public List<Customer> filterCust() {
 		if (!filterCustTxt.equals("")) {
 			return customers = customerDAO.findByNamedQuery("filterCustomers", filterCustTxt);
 		} else {
 			return customers = customerDAO.findAll();
 		}
 	}
 
 	public String loadNew() {
 
 		customers = reservationService.getAllCustomers();
 		selectedCustomer = null;
 		selectedRoom = null;
 		selectedRoomReservations.clear();
 		freeRooms.clear();
 		fromDate = null;
 		toDate = null;
 		roomToRemove = null;
 		return "reservation.xhtml";
 	}
 	
 	public String loadEdit() {
 
 		customers = reservationService.getAllCustomers();
 		selectedCustomer = null;
 		selectedRoom = null;
 		selectedRoomReservations.clear();
 		freeRooms.clear();
 		fromDate = null;
 		toDate = null;
 		roomToRemove = null;
 		return "editReservations.xhtml";
 	}
 
 	
     public List<Reservation> getArrivals() {
         arrivals = reservationService.getReservationsByArrival(new Date());
         return arrivals;
     }
 
     public void setArrivals(List<Reservation> arrivals) {
         this.arrivals = arrivals;
     }
 
     public List<Reservation> getDepartures() {
         departures = reservationService.getReservationsByDeparture(new Date());
         return departures;
     }
 
     public void setDepartures(List<Reservation> departures) {
         this.departures = departures;
     }
 
 	public List<Reservation> getReservationsForSelectedCustomer() {
 		return reservationsForSelectedCustomer;
 	}
 
 	public void setReservationsForSelectedCustomer(List<Reservation> reservationsForSelectedCustomer) {
 		this.reservationsForSelectedCustomer = reservationsForSelectedCustomer;
 	}
 
 	public Reservation getReservationToRemove() {
 		return reservationToRemove;
 	}
 
 	public void setReservationToRemove(Reservation reservationToRemove) {
 		this.reservationToRemove = reservationToRemove;
 		System.out.println("DEBUG: ############ remove reservation "+ reservationToRemove.getId());
 		
 		reservationsForSelectedCustomer.remove(reservationToRemove);
 		reservationDAO.delete(reservationToRemove);
 	}
 
 	public void setReservationToEdit(Reservation reservationToEdit) {
 		this.reservationToEdit = reservationToEdit;
 		System.out.println("DEBUG: ------------- edit reservation "+ reservationToEdit.getId());
 	}
 
 	public Reservation getReservationToEdit() {
 		return reservationToEdit;
 	}
 	
 	public void cancelEdit() {
 		System.out.println("DEBUG: ------------- cancelEdit");
 		this.reservationToEdit = null;
 	}
 	
 	public void storno() {
 		System.out.println("DEBUG: ------------- storno");
 		// TODO Storno Preis berechnen, Rechnung an Customer schicken.
 		// TODO Remove from DB??
 		reservationsForSelectedCustomer.remove(reservationToEdit);
 		reservationDAO.delete(reservationToEdit);
 	}
 	
 	public void earlyDeparture() {
 		System.out.println("DEBUG: ------------- earlyDeparture");
 		for (Reservation r : reservationsForSelectedCustomer) {
 			if (r.getId() == reservationToEdit.getId()) {
 				r.setToDate(reservationToEdit.getToDate());
 			}
 		}
 		reservationDAO.save(reservationToEdit);
 		// TODO Storno Preis berechnen, Rechnung an Customer schicken.
 	}
     
 }
