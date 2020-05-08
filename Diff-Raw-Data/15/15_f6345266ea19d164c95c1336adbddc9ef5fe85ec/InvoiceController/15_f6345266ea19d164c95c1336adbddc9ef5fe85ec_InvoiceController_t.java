 package sse.client.controller;
 
 import java.math.BigDecimal;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import javax.ejb.EJB;
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.SessionScoped;
 
 import org.joda.time.LocalDate;
 
 import sse.ejb.ReservationService;
 import sse.ejb.dao.BillDAO;
 import sse.ejb.dao.CustomerDAO;
 import sse.ejb.dao.ReservationDAO;
 import sse.model.Bill;
 import sse.model.Customer;
 import sse.model.Reservation;
 
 @ManagedBean(name = "invoiceCtrl")
 @SessionScoped
 public class InvoiceController {
 
 	private Date departureDate;
 	private List<Customer> customers;
 	private Customer selectedCustomer;
 	private String filterCustTxt;
 	private List<Reservation> reservations;
 	private List<Reservation> selectedReservations;
 	private Reservation stornoReservation;
 	private Boolean showReservations = false;
 	private Boolean enableActions = true;
 	private Bill invoice;
 
 	@EJB
 	private CustomerDAO customerDao;
 	@EJB
 	ReservationService reservationService;
 	@EJB
 	private ReservationDAO reservationDao;
 	@EJB
 	private BillDAO billDao;
 
 	public String load() {
 		invoice = new Bill();
 		reservations = new ArrayList<Reservation>();
 		selectedReservations = new ArrayList<Reservation>();
 		departureDate = new Date();
 		customers = customerDao.findAll();
 		filterCustTxt = "";
 		return "invoiceCustomer.xhtml";
 	}
 	
 	public String finish() {
 		
 		billDao.save(invoice);	
 		
 		for(Reservation r : invoice.getReservations()) {
 			r.setProcessed(true);
 			reservationDao.save(r);
 		}
 		
 		return "index.xhtml";
 	}
 	
	public String cancel() {
 		return "index.xhtml";
 	}
 
 	public Date getDepartureDate() {
 		return departureDate;
 	}
 
 	public void setDepartureDate(Date departureDate) {
 		this.departureDate = departureDate;
 	}
 
 	public List<Customer> getCustomers() {
 		return customers;
 	}
 
 	public void setCustomers(List<Customer> customers) {
 		this.customers = customers;
 	}
 
 	public Customer getSelectedCustomer() {
 		return selectedCustomer;
 	}
 
 	public void setSelectedCustomer(Customer selectedCustomer) {
 		this.selectedCustomer = selectedCustomer;
 	}
 
 	public String getFilterCustTxt() {
 		return filterCustTxt;
 	}
 
 	public void setFilterCustTxt(String filterCustTxt) {
 		this.filterCustTxt = filterCustTxt;
 	}
 
 	public List<Customer> filterCust() {
 		if (!filterCustTxt.equals("")) {
 			customers = customerDao.findByNamedQuery("filterCustomers",
 					filterCustTxt);
 		} else {
 			customers = customerDao.findAll();
 		}
 
 		return customers;
 	}
 
 	public String loadReservations() {
 
 		reservations.clear();
 		
 		for (Customer c : this.customers) {
 			if (c.getSelected()) {
 				reservations.addAll(reservationService
 						.getReservationsForCustomer(c));
 				this.showReservations = true;
 			}
 		}
 		
 		LocalDate departure = LocalDate.fromDateFields(departureDate);
 		
 		for (Reservation r : this.reservations) {
 			
 			LocalDate temp = LocalDate.fromDateFields(r.getToDate());
 			if(temp.isAfter(departure)) {
 				departure = new LocalDate(temp);
 			}
 			
 		}
 		
 		departureDate = departure.toDateMidnight().toDate();
 
 		return "invoiceCustomer.xhtml";
 
 	}
 
 	public String doStorno() {
 
 		selectedReservations.clear();
 		selectedReservations = getSelectedReservations();
 
 		BigDecimal amount = BigDecimal.ZERO;
 		LocalDate storno = LocalDate.fromDateFields(departureDate);
 
 		for (Reservation r : selectedReservations) {
 			LocalDate start = LocalDate.fromDateFields(r.getFromDate());
 			LocalDate end = LocalDate.fromDateFields(r.getToDate());
 
 			BigDecimal discount = new BigDecimal(r.getDiscount());
 
 			while (start.isBefore(end)) {
 
 				if (start.isBefore(storno)) {
 					amount = amount.add(r.getRoomRate().multiply(
 							BigDecimal.ONE.subtract(discount)));
 				} else {
 					amount = amount.add(r.getRoomRate().multiply(
 							new BigDecimal(0.7)));
 				}
 
 				start = start.plusDays(1);
 			}
 		}
 
 		invoice.setAmount(amount);
 		invoice.setReservations(selectedReservations);
 		invoice.setRoomNumber("" + System.currentTimeMillis());
 		invoice.setDate(new Date());
 
 
 		return "invoice.xhtml";
 
 	}
 
 	public String doInvoice() {
 
 		selectedReservations.clear();
 		selectedReservations = getSelectedReservations();
 
 		BigDecimal amount = BigDecimal.ZERO;
 
 		for (Reservation r : selectedReservations) {
 			LocalDate start = LocalDate.fromDateFields(r.getFromDate());
 			LocalDate end = LocalDate.fromDateFields(r.getToDate());
 
 			BigDecimal discount = new BigDecimal(r.getDiscount());
 
 			while (start.isBefore(end)) {
 
 				amount = amount.add(r.getRoomRate().multiply(
 						BigDecimal.ONE.subtract(discount)));
 				start = start.plusDays(1);
 			}
 		}
 
 		invoice.setAmount(amount);
 		invoice.setReservations(selectedReservations);
 		invoice.setRoomNumber("" + System.currentTimeMillis());
 		invoice.setDate(new Date());
 
 		return "invoice.xhtml";
 	}
 
 	private List<Reservation> getSelectedReservations() {
 
 		List<Reservation> temp = new ArrayList<Reservation>();
 		for (Reservation r : reservations) {
 			if (r.getSelected() != null && r.getSelected()) {
 				temp.add(r);
 				enableActions = true;
 			}
 		}
 
 		if (temp.size() == 0) {
 			enableActions = false;
 		}
 
 		return temp;
 	}
 
 	public void setStornoReservation(Reservation stornoReservation) {
 		this.stornoReservation = stornoReservation;
 	}
 
 	public Reservation getStornoReservation() {
 		return stornoReservation;
 	}
 
 	public List<Reservation> getReservations() {
 		return reservations;
 	}
 
 	public void setReservations(List<Reservation> reservations) {
 		this.reservations = reservations;
 	}
 
 	public Boolean getShowReservations() {
 		return showReservations;
 	}
 
 	public void setShowReservations(Boolean showReservations) {
 		this.showReservations = showReservations;
 	}
 
 	public void setSelectedReservations(List<Reservation> selectedReservations) {
 		this.selectedReservations = selectedReservations;
 	}
 
 	public Boolean getEnableActions() {
 		return enableActions;
 	}
 
 	public void setEnableActions(Boolean enableActions) {
 		this.enableActions = enableActions;
 	}
 
 	public Bill getInvoice() {
 		return invoice;
 	}
 
 	public void setInvoice(Bill invoice) {
 		this.invoice = invoice;
 	}
 
 }
