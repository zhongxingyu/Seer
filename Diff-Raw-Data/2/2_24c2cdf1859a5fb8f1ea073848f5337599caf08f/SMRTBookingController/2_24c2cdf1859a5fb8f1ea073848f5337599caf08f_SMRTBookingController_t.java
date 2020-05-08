 /**
  * Copyright (c) 2012 Maven Lab Private Limited. All rights reserved.
  */
 package com.mavenlab.caspian.controller;
 
 import java.io.Serializable;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.Map;
 import java.util.logging.Logger;
 
 import javax.annotation.PostConstruct;
 import javax.enterprise.context.SessionScoped;
 import javax.faces.application.FacesMessage;
 import javax.faces.context.FacesContext;
 import javax.inject.Inject;
 import javax.inject.Named;
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 import javax.servlet.http.HttpServletRequest;
 import javax.validation.constraints.Pattern;
 
 import com.mavenlab.caspian.dto.Address;
 import com.mavenlab.caspian.dto.Booking;
 import com.mavenlab.caspian.interfacing.GatewayRest;
 import com.mavenlab.caspian.interfacing.Response;
 import com.mavenlab.caspian.interfacing.response.AddressResponse;
 import com.mavenlab.caspian.interfacing.response.ErrorResponse;
 import com.mavenlab.caspian.interfacing.response.GetBookingResponse;
 import com.mavenlab.caspian.model.Client;
 import com.mavenlab.caspian.model.Job;
 import com.mavenlab.caspian.model.TaxiCompany;
 import com.mavenlab.caspian.model.TaxiType;
 import com.mavenlab.caspian.session.PropertiesManager;
 import com.mavenlab.caspian.util.DateUtil;
 
 /**
  * Class : LocationController.java
  * 
  * @author <a href="mailto:yogie@mavenlab.com">Yogie Kurniawan</a>
  * @since Aug 7, 2012 3:04:12 PM
  */
 @Named("smrtBookingController")
 @SessionScoped
 public class SMRTBookingController implements Serializable {
 
 	private static final long serialVersionUID = -4277135961285843276L;
 
 	@Inject
 	private Logger log;
 	
 	@Inject
 	private PropertiesManager pm;
 	
 	@PersistenceContext
 	private EntityManager em;
 
 	private String name;
 	private String fullAddress = "Singapore";
 
 	private Address address;
 	private List<Address> addresses = new ArrayList<Address>();
 	private List<TaxiType> taxiTypes = new ArrayList<TaxiType>();
 	private List<Booking> bookings = new ArrayList<Booking>();
 
 	private Map<String, String> props;
 	private Map<String, String> msgs;
 	
 	private String pickupAddress;
 	private String pickupPoint;
 
 	@Pattern(regexp = "(8|9)[0-9]{7}", message = "MOBILE NO: Must have 8 digits and first digit must starts with 8 or 9")
 	private String msisdn;
 
 	private int taxiType;
 	private int numberOfCabs;
 
 	private boolean advanceBooking;
 	private Date pickupDateTime;
 	private String pickupDateTimeString;
 	private String destination;
 	private String schedule;
 
 	private TaxiCompany taxiCompany;
 	private Client client;
 	private String deviceId = "JnVh0lXaCgd1BBakENR6WAbBcxlntT3BccAnop4MXHJSaivfTr";
 	
 	private String jobNoRebook;
 	private boolean rebook;
 
 	@Inject
 	private GatewayRest gateway;
 
 	@SuppressWarnings("unchecked")
 	@PostConstruct
 	public void init() {
 		taxiTypes = em.createNamedQuery("caspian.entity.TaxiType.findByCompany").setParameter("companyId", 2).getResultList();
 		taxiCompany = em.find(TaxiCompany.class, 2);
 		client = em.find(Client.class, 2);
 		advanceBooking = false;
 		schedule = Booking.NOW;
 	}
 	
 	public void reset() {
 		jobNoRebook = null;
 		rebook = false;
 		name = null;
 		msisdn = null;
 		destination = null;
 		advanceBooking = false;
 		schedule = Booking.NOW;
 		pickupDateTimeString = null;
 		taxiType = 0;
 		numberOfCabs = 0;
 		pickupAddress = null;
 		pickupPoint = null;
 
 		log.info("RESET");
 	}
 
 	public void enableAdvance() {
 		setAdvanceBooking(Boolean.TRUE);
 		setSchedule(Booking.ADVANCE);
 		log.info("ENABLE ADVANCE BOOKING");
 	}
 
 	public void disableAdvance() {
 		setAdvanceBooking(Boolean.FALSE);
 		setSchedule(Booking.NOW);
 		log.info("DISABLE ADVANCE BOOKING");
 	}
 
 	public void toggleAdvBooking() {
 		if(advanceBooking) {
 			enableAdvance();
 		} else {
 			disableAdvance();
 		}
 	}
 	
 	/**
 	 * Get Taxi Category Name
 	 * 
 	 * @param taxiType
 	 * @return {@link String}
 	 */
 	public String getTaxiCategory(int taxiType) {
 		for (TaxiType t : taxiTypes) {
 			if (t.getCategoryId() == taxiType) {
 				return t.getName();
 			}
 		}
 		return "ANY";
 	}
 
 	/**
 	 * Select a pickup address
 	 * 
 	 * @param address
 	 * @return String
 	 */
 	public String selectPickupAddress(String address) {
 		setPickupAddress(address);
		return "confirm.htm";
 	}
 
 	/**
 	 * Proceed
 	 * 
 	 * @return String
 	 */
 	public String proceed() {
 		try {
 			log.info("VALIDATING PICKUP DATE TIME");
 			
 			String minBT = getProps().get("com.mavenlab.caspian.AdvanceBookingMinimalMinutes");
 			String maxBT = getProps().get("com.mavenlab.caspian.AdvanceBookingMaximalHours");
 			String errMsg = getMsgs().get("com.mavenlab.caspian.message.InvalidPickupDateTimeAdvanceBooking");
 			int minBookingTime = (minBT == null) ? 30 : Integer.parseInt(minBT); //minute
 			int maxBookingTime = (maxBT == null) ? 72 : Integer.parseInt(maxBT); //hour
 			errMsg = (errMsg == null) ? "Sorry, we are unable to proceed with your selected pickup date time" : errMsg;
 			
 			Date nowDate = new Date(System.currentTimeMillis());
 			Date dateMax = new Date(nowDate.getTime() + (maxBookingTime * 60*60*1000));
 			
 			if(pickupDateTimeString == null || (pickupDateTimeString!=null && pickupDateTimeString.trim().isEmpty())) {
 				setPickupDateTime(nowDate); 
 			} else {
 				setPickupDateTime(DateUtil.formatToDate("dd/MM/yyyy HH:mm", pickupDateTimeString));
 			}
 		
 			log.info("PICKUP DATE TIME: " + pickupDateTime);
 			
 			if(pickupDateTime.before(nowDate) || pickupDateTime.after(dateMax)) {
 				log.info("VALIDATE FAILED");
 				FacesContext.getCurrentInstance().addMessage("null", new FacesMessage(FacesMessage.SEVERITY_ERROR, errMsg, errMsg));
 				return "pm:new";
 			}
 			
 			//check advance booking or immediate booking
 			if((pickupDateTime.getTime() - nowDate.getTime()) <= (minBookingTime * 60*1000)) {
 				disableAdvance();
 			} else {
 				enableAdvance();
 			}
 
 		} catch(Exception e) {
 			e.printStackTrace();
 			FacesContext.getCurrentInstance().addMessage("null", new FacesMessage(FacesMessage.SEVERITY_ERROR, e.getMessage(), e.getMessage()));
 		}
 		
 		log.info("PROCEED: " + pickupAddress);
 		name = name.toUpperCase();
 		destination = destination.toUpperCase();
 		pickupPoint = pickupPoint != null ? pickupPoint.toUpperCase() : null;
 		addresses = new ArrayList<Address>();
 		address = null;
 		if (pickupAddress != null && !pickupAddress.isEmpty()) {
 			pickupAddress = pickupAddress.toUpperCase();
 			FacesContext context = FacesContext.getCurrentInstance();
 			HttpServletRequest req = (HttpServletRequest) context.getExternalContext().getRequest();
 			try {
 				Response response = gateway.findAddresses("", msisdn, 2, taxiCompany.getId(), client.getMerchantKey(), deviceId, req.getRemoteAddr(), pickupAddress, req);
 				if (response instanceof ErrorResponse) {
 					throw new Exception(response.getMessage());
 				}
 				if (response.getStatus() > 0) {
 					AddressResponse res = (AddressResponse) response;
 					addresses = res.getAddresses().getAddresses();
 					if (addresses != null && addresses.size() > 1) {
 						return "pm:address";
 					} else if (addresses != null && addresses.size() == 1) {
 						address = addresses.get(0);
 						setFullAddress(address.getFullAddress());
 						pickupAddress = getFullAddress();
 						return "confirm.htm";
 					}
 				} else {
 					throw new Exception(response.getMessage());
 				}
 			} catch (Exception e) {
 				e.printStackTrace();
 				FacesContext.getCurrentInstance().addMessage("null", new FacesMessage(FacesMessage.SEVERITY_ERROR, e.getMessage(), e.getMessage()));
 			}
 		}
 		return "pm:new";
 	}
 
 	/**
 	 * Check Status
 	 */
 	public void checkStatus() {
 		log.info("CHECK STATUS => " + msisdn);
 
 		FacesContext context = FacesContext.getCurrentInstance();
 		HttpServletRequest req = (HttpServletRequest) context.getExternalContext().getRequest();
 		try {
 			Response response = gateway.getActiveBookingList("", msisdn, 2, taxiCompany.getId(), client.getMerchantKey(), deviceId, req.getRemoteAddr(), req);
 			if (response instanceof ErrorResponse) {
 				throw new Exception(response.getMessage());
 			}
 			if (response.getStatus() > 0) {
 				GetBookingResponse res = (GetBookingResponse) response;
 				bookings = res.getBookings().getBookings();
 			} else {
 				throw new Exception(response.getMessage());
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 			FacesContext.getCurrentInstance().addMessage("null", new FacesMessage(FacesMessage.SEVERITY_ERROR, e.getMessage(), e.getMessage()));
 		}
 	}
 
 	/**
 	 * Detect and Address
 	 */
 	@SuppressWarnings("rawtypes")
 	public void detectAddress() {
 		FacesContext context = FacesContext.getCurrentInstance();
 		HttpServletRequest req = (HttpServletRequest) context.getExternalContext().getRequest();
 		Map map = context.getExternalContext().getRequestParameterMap();
 		Object parameter = map.get("fulladdress");
 		try {
 			String phoneNo = "92424086";
 			if (msisdn != null && !msisdn.isEmpty()) {
 				phoneNo = msisdn;
 			}
 			Response response = gateway.findAddresses("", phoneNo, 2, taxiCompany.getId(), client.getMerchantKey(), deviceId, req.getRemoteAddr(), parameter.toString(), req);
 			if (response instanceof ErrorResponse) {
 				throw new Exception(response.getMessage());
 			}
 			if (response.getStatus() > 0) {
 				AddressResponse res = (AddressResponse) response;
 				addresses = res.getAddresses().getAddresses();
 				if (addresses != null && addresses.size() > 0) {
 					setFullAddress(addresses.get(0).getFullAddress());
 					pickupAddress = getFullAddress();
 				} else {
 					setFullAddress("UNABLE TO DETECT LOCATION");
 					pickupAddress = null;
 				}
 			} else {
 				throw new Exception(response.getMessage());
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 			FacesContext.getCurrentInstance().addMessage("null", new FacesMessage(FacesMessage.SEVERITY_ERROR, e.getMessage(), e.getMessage()));
 		}
 	}
 
 	/**
 	 * Cancel Booking
 	 */
 	public void cancel() {
 		FacesContext context = FacesContext.getCurrentInstance();
 		HttpServletRequest req = (HttpServletRequest) context.getExternalContext().getRequest();
 		Map map = context.getExternalContext().getRequestParameterMap();
 		Object parameter = map.get("bookingId");
 		try {
 			Response response = gateway.cancelCab("", msisdn, 2, taxiCompany.getId(), client.getMerchantKey(), deviceId, req.getRemoteAddr(), parameter.toString(), req);
 			if (response instanceof ErrorResponse) {
 				throw new Exception(response.getMessage());
 			}
 			if (response.getStatus() > 0) {
 				log.info("SUCCESSFULLY CANCEL => " + parameter.toString());
 			} else {
 				throw new Exception(response.getMessage());
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 			FacesContext.getCurrentInstance().addMessage("null", new FacesMessage(FacesMessage.SEVERITY_ERROR, e.getMessage(), e.getMessage()));
 		}
 	}
 
 	public String rebooking(String jobNo) {
 		try {
 			setJobNoRebook(jobNo);
 			rebook = true;
 			Date nowDate = new Date();
 			String minBT = getProps().get("com.mavenlab.caspian.AdvanceBookingMinimalMinutes");
 			int minBookingTime = (minBT == null) ? 30 : Integer.parseInt(minBT); //minute
 			//String maxBT = getProps().get("com.mavenlab.caspian.AdvanceBookingMaximalHours");
 			//int maxBookingTime = (maxBT == null) ? 72 : Integer.parseInt(maxBT); //hour
 			
 			Job job = (Job) em.createNamedQuery("caspian.entity.job.findByJobId").setParameter("jobId", Long.parseLong(jobNo)).getSingleResult();
 			Long bookingId = job.getBookingId();
 			com.mavenlab.caspian.model.Booking booking = em.find(com.mavenlab.caspian.model.Booking.class, bookingId);
 			
 			setMsisdn(booking.getMsisdn());
 			setName(booking.getName());
 			
 			String pickupaddress = booking.getBlockNo() + " " + booking.getRoadName();
 			if (booking.getBuildingName() != null && !booking.getBuildingName().isEmpty()) {
 				pickupaddress += " " + booking.getBuildingName();
 			}
 			pickupaddress += " " + booking.getPostalCode();
 			
 			setPickupAddress(pickupaddress);
 			setPickupPoint(booking.getPickupPoint());
 			setNumberOfCabs(1);
 			setTaxiType(booking.getTaxiType());
 			setSchedule(booking.getSchedule());
 			setPickupDateTime(booking.getPickupDate());
 			setPickupDateTimeString(DateUtil.formatToString("dd/MM/yyyy HH:mm", booking.getPickupDate()));
 			setDestination(booking.getDestination());
 			
 			if(booking.getSchedule().equals(Booking.NOW)) {
 				setPickupDateTime(nowDate);
 				disableAdvance();
 				return "confirmRebook.htm";
 			} else {
 				if(booking.getPickupDate().getTime() - nowDate.getTime() <= ((minBookingTime * 60*1000))) {
 					disableAdvance();
 					return "confirmRebook.htm";
 				}
 				enableAdvance();
 			}
 			
 			return "new.htm";
 		} catch(Exception e) {
 			e.printStackTrace();
 			FacesContext.getCurrentInstance().addMessage("null", new FacesMessage(FacesMessage.SEVERITY_ERROR, e.getMessage(), e.getMessage()));
 		}
 		
 		return "pm:status";
 	}
 	
 	/**
 	 * Confirm Booking
 	 * 
 	 * @return {@link String}
 	 */
 	public String confirm() {
 		log.info("CONFIRM: " + msisdn + "|" + address + "|" + taxiType);
 		log.info(client.getMerchantKey() + "|" + taxiCompany.getId());
 		FacesContext context = FacesContext.getCurrentInstance();
 		HttpServletRequest req = (HttpServletRequest) context.getExternalContext().getRequest();
 		try {
 			String addressRef = address.getAddressReference();
 			if(rebook && (jobNoRebook!=null && !jobNoRebook.isEmpty())) {
 				try {
 					addressRef = addressRef + "-REBOOK";
 					gateway.cancelCab("", msisdn, 2, taxiCompany.getId(), client.getMerchantKey(), deviceId, req.getRemoteAddr(), jobNoRebook+"-REBOOK", req);
 				}catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 			Response response = gateway.bookingCab("", msisdn, 2, taxiCompany.getId(), client.getMerchantKey(), deviceId, req.getRemoteAddr(), address.getBlock(), address.getRoadName(), address.getBuildingName(), address.getPostalCode(), addressRef, pickupPoint, numberOfCabs, taxiType, schedule, DateUtil.formatToString("dd/MM/yyyy", pickupDateTime), DateUtil.formatToString("HH:mm", pickupDateTime), destination, name, req);
 			if (response instanceof ErrorResponse) {
 				throw new Exception(response.getMessage());
 			}
 
 			if (response.getStatus() > 0) {
 				rebook = false;
 				jobNoRebook = null;
 				return "status.htm";
 			} else {
 				throw new Exception(response.getMessage());
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 			FacesContext.getCurrentInstance().addMessage("null", new FacesMessage(FacesMessage.SEVERITY_ERROR, e.getMessage(), e.getMessage()));
 		}
 		return "pm:confirm";
 	}
 	
 	public String confirmRebook() {
 		log.info("CONFIRM REBOOK: " + jobNoRebook + "|" + msisdn + "|" + address + "|" + taxiType);
 		log.info(client.getMerchantKey() + "|" + taxiCompany.getId());
 		FacesContext context = FacesContext.getCurrentInstance();
 		HttpServletRequest req = (HttpServletRequest) context.getExternalContext().getRequest();
 		try {
 			Response response = gateway.redispatchJob("", msisdn, 2, taxiCompany.getId(), client.getMerchantKey(), deviceId, req.getRemoteAddr(), jobNoRebook, req);
 			if (response instanceof ErrorResponse) {
 				throw new Exception(response.getMessage());
 			}
 
 			if (response.getStatus() > 0) {
 				rebook = false;
 				jobNoRebook = null;
 				return "status.htm";
 			} else {
 				throw new Exception(response.getMessage());
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 			FacesContext.getCurrentInstance().addMessage("null", new FacesMessage(FacesMessage.SEVERITY_ERROR, e.getMessage(), e.getMessage()));
 		}
 		return "pm:confirmRebook";
 	}
 
 	/**
 	 * @return the addresses
 	 */
 	public List<Address> getAddresses() {
 		return addresses;
 	}
 
 	/**
 	 * @param addresses
 	 *            the addresses to set
 	 */
 	public void setAddresses(List<Address> addresses) {
 		this.addresses = addresses;
 	}
 
 	/**
 	 * @return the fullAddress
 	 */
 	public String getFullAddress() {
 		return fullAddress;
 	}
 
 	/**
 	 * @param fullAddress
 	 *            the fullAddress to set
 	 */
 	public void setFullAddress(String fullAddress) {
 		this.fullAddress = fullAddress;
 	}
 
 	/**
 	 * @return the name
 	 */
 	public String getName() {
 		return name;
 	}
 
 	/**
 	 * @param name the name to set
 	 */
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	/**
 	 * @return the address
 	 */
 	public Address getAddress() {
 		return address;
 	}
 
 	/**
 	 * @param address
 	 *            the address to set
 	 */
 	public void setAddress(Address address) {
 		this.address = address;
 	}
 
 	/**
 	 * @return the pickupAddress
 	 */
 	public String getPickupAddress() {
 		return pickupAddress;
 	}
 
 	/**
 	 * @param pickupAddress
 	 *            the pickupAddress to set
 	 */
 	public void setPickupAddress(String pickupAddress) {
 		this.pickupAddress = pickupAddress;
 	}
 
 	/**
 	 * @return the pickupDateTimeString
 	 */
 	public String getPickupDateTimeString() {
 		return pickupDateTimeString;
 	}
 
 	/**
 	 * @param pickupDateTimeString the pickupDateTimeString to set
 	 */
 	public void setPickupDateTimeString(String pickupDateTimeString) {
 		this.pickupDateTimeString = pickupDateTimeString;
 	}
 
 	/**
 	 * @return the msisdn
 	 */
 	public String getMsisdn() {
 		return msisdn;
 	}
 
 	/**
 	 * @param msisdn
 	 *            the msisdn to set
 	 */
 	public void setMsisdn(String msisdn) {
 		this.msisdn = msisdn;
 	}
 
 	/**
 	 * @return the pickupPoint
 	 */
 	public String getPickupPoint() {
 		return pickupPoint;
 	}
 
 	/**
 	 * @param pickupPoint
 	 *            the pickupPoint to set
 	 */
 	public void setPickupPoint(String pickupPoint) {
 		this.pickupPoint = pickupPoint;
 	}
 
 	/**
 	 * @return the taxiType
 	 */
 	public int getTaxiType() {
 		return taxiType;
 	}
 
 	/**
 	 * @param taxiType
 	 *            the taxiType to set
 	 */
 	public void setTaxiType(int taxiType) {
 		this.taxiType = taxiType;
 	}
 
 	/**
 	 * @return the numberOfCabs
 	 */
 	public int getNumberOfCabs() {
 		return numberOfCabs;
 	}
 
 	/**
 	 * @param numberOfCabs
 	 *            the numberOfCabs to set
 	 */
 	public void setNumberOfCabs(int numberOfCabs) {
 		this.numberOfCabs = numberOfCabs;
 	}
 
 	/**
 	 * @return the advanceBooking
 	 */
 	public boolean isAdvanceBooking() {
 		return advanceBooking;
 	}
 
 	/**
 	 * @param advanceBooking
 	 *            the advanceBooking to set
 	 */
 	public void setAdvanceBooking(boolean advanceBooking) {
 		this.advanceBooking = advanceBooking;
 	}
 
 	/**
 	 * @return the pickupDateTime
 	 */
 	public Date getPickupDateTime() {
 		return pickupDateTime;
 	}
 
 	/**
 	 * @param pickupDateTime
 	 *            the pickupDateTime to set
 	 */
 	public void setPickupDateTime(Date pickupDateTime) {
 		this.pickupDateTime = pickupDateTime;
 	}
 
 	/**
 	 * @return the destination
 	 */
 	public String getDestination() {
 		return destination;
 	}
 
 	/**
 	 * @param destination
 	 *            the destination to set
 	 */
 	public void setDestination(String destination) {
 		this.destination = destination;
 	}
 
 	/**
 	 * @return the taxiTypes
 	 */
 	public List<TaxiType> getTaxiTypes() {
 		return taxiTypes;
 	}
 
 	/**
 	 * @param taxiTypes
 	 *            the taxiTypes to set
 	 */
 	public void setTaxiTypes(List<TaxiType> taxiTypes) {
 		this.taxiTypes = taxiTypes;
 	}
 
 	/**
 	 * @return the schedule
 	 */
 	public String getSchedule() {
 		return schedule;
 	}
 
 	/**
 	 * @param schedule
 	 *            the schedule to set
 	 */
 	public void setSchedule(String schedule) {
 		this.schedule = schedule;
 	}
 
 	/**
 	 * @return the taxiCompany
 	 */
 	public TaxiCompany getTaxiCompany() {
 		return taxiCompany;
 	}
 
 	/**
 	 * @param taxiCompany
 	 *            the taxiCompany to set
 	 */
 	public void setTaxiCompany(TaxiCompany taxiCompany) {
 		this.taxiCompany = taxiCompany;
 	}
 
 	/**
 	 * @return the client
 	 */
 	public Client getClient() {
 		return client;
 	}
 
 	/**
 	 * @param client
 	 *            the client to set
 	 */
 	public void setClient(Client client) {
 		this.client = client;
 	}
 
 	/**
 	 * @return the bookings
 	 */
 	public List<Booking> getBookings() {
 		return bookings;
 	}
 
 	/**
 	 * @param bookings
 	 *            the bookings to set
 	 */
 	public void setBookings(List<Booking> bookings) {
 		this.bookings = bookings;
 	}
 	
 	/**
 	 * Format from Date to String
 	 * 
 	 * @param pattern
 	 * @param date
 	 * @return String
 	 */
 	public static String formatToString(String pattern, Date date) {
 		try {
 			SimpleDateFormat sdf = new SimpleDateFormat(pattern);
 
 			return sdf.format(date);
 
 		} catch (Exception e) {
 			return null;
 		}
 	}
 
 	/**
 	 * @return the props
 	 */
 	public Map<String, String> getProps() {
 		if(props == null) {
 			props = pm.getProperties();
 		}
 		return props;
 	}
 
 	/**
 	 * @param props the props to set
 	 */
 	public void setProps(Map<String, String> props) {
 		this.props = props;
 	}
 
 	/**
 	 * @return the msgs
 	 */
 	public Map<String, String> getMsgs() {
 		if(msgs == null) {
 			msgs = pm.getMessages();
 		}
 		return msgs;
 	}
 
 	/**
 	 * @param msgs the msgs to set
 	 */
 	public void setMsgs(Map<String, String> msgs) {
 		this.msgs = msgs;
 	}
 
 	/**
 	 * @return the jobNoRebook
 	 */
 	public String getJobNoRebook() {
 		return jobNoRebook;
 	}
 
 	/**
 	 * @param jobNoRebook the jobNoRebook to set
 	 */
 	public void setJobNoRebook(String jobNoRebook) {
 		this.jobNoRebook = jobNoRebook;
 	}
 
 }
