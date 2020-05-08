 /**
  * Copyright (c) 2012 Maven Lab Private Limited. All rights reserved.
  */
 package com.mavenlab.caspian.interfacing.company.smrt;
 
 import java.rmi.RemoteException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Calendar;
 import java.util.List;
 import java.util.TimeZone;
 import java.util.logging.Logger;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.apache.axis.AxisFault;
 import org.apache.axis.types.UnsignedByte;
 import org.tempuri.AddressPoint;
 import org.tempuri.Booking;
 import org.tempuri.BookingWebServiceSoapProxy;
 import org.tempuri.BookingWebServiceSoapStub;
 import org.tempuri.ErrorCode;
 import org.tempuri.GetBookingObject;
 import org.tempuri.Job;
 import org.tempuri.Vehicle;
 import org.tempuri.VehicleCategory;
 import org.tempuri.VehicleMake;
 import org.tempuri.VehicleModel;
 
 import com.mavenlab.caspian.util.DateUtil;
 import com.mavenlab.caspian.util.GeolocateUtil;
 
 /**
  * Class : SMRTProxy.java
  * 
  * @author <a href="mailto:yogie@mavenlab.com">Yogie Kurniawan</a>
  * @since Jul 26, 2012 2:47:59 PM
  */
 public class SMRTProxy {
 
 	private static SMRTProxy instance = null;
 
 	private String endPoint;
 	private int timeout;
 
 	private static BookingWebServiceSoapProxy proxy;
 	private static BookingWebServiceSoapStub stub;
 
 	private final static String POSTAL_CODE_PATTERN = "[0-9]{6}";
 	private final static String BLOCK_NO_PATTERN = "[0-9]{1,4}";
 	private final static String MOBILE_NO_PATTERN = "(8|9)[0-9]{7}";
 
 	private Logger log = Logger.getLogger(this.getClass().getSimpleName());
 
 	/**
 	 * @param endPoint
 	 * @param timeout 
 	 */
 	private SMRTProxy(String endPoint, int timeout) {
 		super();
 		this.endPoint = endPoint;
 		this.timeout = timeout;
 		init();
 	}
 
 	/**
 	 * Init proxy
 	 */
 	private void init() {
 		proxy = new BookingWebServiceSoapProxy(getEndPoint());
 		try {
 			stub = new BookingWebServiceSoapStub();
 			stub._setProperty(BookingWebServiceSoapStub.ENDPOINT_ADDRESS_PROPERTY, getEndPoint());
 			stub.setTimeout((1000 * getTimeout()));
 		} catch (AxisFault e) {
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * Get Address Ref
 	 * 
 	 * @param keyedInAddress
 	 * @return {@link List}
 	 */
 	public List<AddressPoint> getAddressRef(java.lang.String keyedInAddress) throws Exception {
 		List<AddressPoint> address = new ArrayList<AddressPoint>();
 		try {
 			String buildingName = "";
 			String blockNumber = "";
 			String streetName = keyedInAddress.toUpperCase();
 			String postalCode = "";
 			String expressCabCode = "";
 			String taxiStandCode = "";
 
 			streetName = streetName.replaceAll("BLK", "").trim();
 
 			Pattern pattern = Pattern.compile(POSTAL_CODE_PATTERN);
 			Matcher matcher = pattern.matcher(streetName);
 			if (matcher.find()) {
 				postalCode = matcher.group().trim();
 				streetName = streetName.replaceFirst(postalCode, "").trim();
 			}
 
 			pattern = Pattern.compile(BLOCK_NO_PATTERN);
 			matcher = pattern.matcher(streetName);
 			if (matcher.find()) {
 				blockNumber = matcher.group().trim();
 				streetName = streetName.replaceFirst(blockNumber, "").trim();
 			}
 			if (!postalCode.isEmpty()) {
 				blockNumber = "";
 				streetName = "";
 				buildingName = "";
 			}
 
 			AddressPoint[] points = stub.getAddressRef(buildingName, blockNumber, streetName, postalCode, expressCabCode, taxiStandCode);
 			if (points != null && points.length > 0)
 				address = Arrays.asList(points);
 			else {
 //				if (blockNumber.isEmpty() && postalCode.isEmpty() && address.isEmpty()) {
 //					points = stub.getAddressRef(streetName, blockNumber, streetName, postalCode, expressCabCode, taxiStandCode);
 //					log.info("FIND BUILDING NAME => " + streetName);
 //					if (points != null)
 //						address = Arrays.asList(points);
 //				}
 				if(!blockNumber.isEmpty())
 					postalCode = GeolocateUtil.getPostalCode(blockNumber + " " + streetName);
 				else
 					postalCode = GeolocateUtil.getPostalCode(streetName);
 				log.info("POSTAL CODE GEOLOCATION: " + postalCode);
 				streetName = "";
 				points = stub.getAddressRef(buildingName, blockNumber, streetName, postalCode, expressCabCode, taxiStandCode);
 				if (points != null && points.length > 0)
 					address = Arrays.asList(points);
 			}
 
 		} catch (RemoteException e) {
 			e.printStackTrace();
 			throw new Exception(e.getCause());
 		}
 		return address;
 	}
 
 	/**
 	 * Cancel Job
 	 * 
 	 * @param jobId
 	 * @param cancelBy
 	 * @return {@link ErrorCode}
 	 */
 	public ErrorCode cancelJob(String jobId, String cancelBy) throws Exception {
 		try {
 			return stub.cancelJob(Long.valueOf(jobId).longValue(), "", 3, cancelBy);
 		} catch (Exception e) {
 			e.printStackTrace();
 			throw new Exception(e.getCause());
 		}
 	}
 
 	/**
 	 * Submit a booking
 	 * 
 	 * @param bookingDT
 	 * @param requiredPickupDT
 	 * @param pickupAddressRef
 	 * @param pickupPoint
 	 * @param requestedVehicleCat
 	 * @param destination
 	 * @param requiredVehicleCount
 	 * @param paxPhone
 	 * @param email
 	 * @param bookingBy
 	 * @param bookingIPAddress
 	 * @param bookingType
 	 * @return {@link Long}
 	 */
 	public long submitBooking(java.util.Calendar bookingDT, java.util.Calendar requiredPickupDT, int pickupAddressRef, java.lang.String pickupPoint, short requestedVehicleCat, java.lang.String destination, short requiredVehicleCount, java.lang.String paxPhone, java.lang.String email, String bookingBy, java.lang.String bookingIPAddress, UnsignedByte bookingType, String name) throws Exception {
 		try {
 			Calendar bookingCal = DateUtil.getGMTFormat(bookingDT);
 			Calendar pickupCal = DateUtil.getGMTFormat(requiredPickupDT);
 			requiredPickupDT.setTimeZone(TimeZone.getTimeZone("GMT"));
 			return stub.submitBooking(bookingCal, pickupCal, pickupAddressRef, pickupPoint, requestedVehicleCat, (short) 0, "", (short) 0, destination, requiredVehicleCount, "", 0, name, paxPhone, "", "", (short) 0, new UnsignedByte(5), email, (short) 0, new UnsignedByte(0), (short) 0, false, (short) 0, bookingBy, new UnsignedByte(4), bookingIPAddress, name, paxPhone, bookingType);
 		} catch (Exception e) {
 			e.printStackTrace();
 			throw new Exception(e.getCause());
 
 		}
 	}
 	
 	/**
 	 * Redispatch Job
 	 * 
 	 * @param jobId
 	 * @return {@link Long}
 	 * @throws Exception 
 	 */
 	public ErrorCode redispatchJob(long jobId) throws Exception {
 		try {
 			return stub.redispatchJob(jobId);
 		} catch (Exception e) {
 			e.printStackTrace();
 			throw new Exception(e.getCause());
 		}
 	}
 
 	/**
 	 * Get Booking By Msisdn
 	 * 
 	 * @param Msisdn
 	 * @return {@link List}
 	 */
 	public List<GetBookingObject> getBookingByMsisdn(String phoneNo) throws Exception {
 		List<GetBookingObject> books = new ArrayList<GetBookingObject>();
 		try {
 			GetBookingObject[] bookings = stub.getBooking("", phoneNo, (long) 0, "", (long) 0);
 			// System.out.println("BY MSISDN");
 			// System.out.println("==========");
 			// for (GetBookingObject o : bookings) {
 			// JobQueryInternetBookingView job = o.getBookingJob();
 			// System.out.println("BOOKING ID: " + job.getBookingId());
 			// System.out.println("JOB ID: " +job.getJobId());
 			// System.out.println("STATUS: " + job.getJobStatus().intValue());
 			// System.out.println("------------------------");
 			// }
 			// System.out.println("==============");
 			books = Arrays.asList(bookings);
 		} catch (Exception e) {
 			e.printStackTrace();
 			throw new Exception(e.getCause());
 
 		}
 		return books;
 	}
 
 	/**
 	 * Get Booking
 	 * 
 	 * @param bookingId
 	 * @param msisdn
 	 * @return {@link List}
 	 */
 	public List<GetBookingObject> getBooking(String bookingId, String msisdn) throws Exception {
 		List<GetBookingObject> books = new ArrayList<GetBookingObject>();
 		try {
 			GetBookingObject[] bookings = stub.getBooking("", msisdn, Long.valueOf(bookingId).longValue(), "", (long) 0);
 			books = Arrays.asList(bookings);
 		} catch (Exception e) {
 			e.printStackTrace();
 			throw new Exception(e.getCause());
 		}
 		return books;
 	}
 	
 	/**
 	 * Get Booking
 	 * 
 	 * @param bookingId
 	 * @param msisdn
 	 * @param jobId
 	 * @return {@link List}
 	 */
	public List<GetBookingObject> getBooking(String bookingId, String jobId, String msisdn) throws Exception {
 		List<GetBookingObject> books = new ArrayList<GetBookingObject>();
 		try {
 			GetBookingObject[] bookings = stub.getBooking("", msisdn, Long.valueOf(bookingId).longValue(), "", Long.valueOf(jobId));
 			books = Arrays.asList(bookings);
 		} catch (Exception e) {
 			e.printStackTrace();
 			throw new Exception(e.getCause());
 		}
 		return books;
 	}
 
 	/**
 	 * Get Booking
 	 * 
 	 * @param bookingId
 	 * @return {@link List}
 	 */
 	public Booking getBookingById(long bookingId) throws Exception {
 		try {
 			Booking booking = stub.getBookingById(bookingId);
 			System.out.println("BY BOOKING ID");
 			System.out.println("=============");
 			for (Job job : booking.getJobCollection()) {
 				System.out.println("BOOKING ID: " + booking.getBookingId());
 				System.out.println("JOB ID: " + job.getJobId());
 				System.out.println("STATUS: " + job.getJobStatus().intValue());
 				System.out.println("------------------------");
 			}
 			System.out.println("===============");
 			return booking;
 		} catch (Exception e) {
 			e.printStackTrace();
 			throw new Exception(e.getCause());
 		}
 	}
 
 	public void getVehicle() {
 		try {
 			VehicleCategory[] categories = proxy.getAllVehicleCategory();
 			for (int i = 0; i < categories.length; i++) {
 				VehicleCategory vc = categories[i];
 				System.out.println(vc.getVehCatId() + "|" + vc.getVehTypeId());
 				System.out.println(vc.getCode() + "|" + vc.getDescription());
 			}
 
 			VehicleModel[] models = proxy.getAllVehicleModel();
 			for (int j = 0; j < models.length; j++) {
 				VehicleModel m = models[j];
 				System.out.println("----------Models---------");
 				System.out.println(m.getVehModelId() + "|" + m.getCode() + "|" + m.getDescription());
 				System.out.println(m.getVehCatId() + "|" + m.getVehMakeId() + "|" + m.getTaxiClassId());
 
 				Vehicle[] vehicles = m.getVehicleCollection();
 				for (int n = 0; n < vehicles.length; n++) {
 					Vehicle v = vehicles[n];
 					System.out.println("-------Vehicles------");
 					System.out.println(v.getVehId() + "|" + v.getVehicleNum() + "|" + v.getCompanyId());
 					System.out.println("-----End Vehicles----");
 				}
 				System.out.println("--------End Models-------");
 			}
 
 			VehicleMake[] makes = proxy.getAllVehicleMake();
 			for (int k = 0; k < makes.length; k++) {
 				VehicleMake v = makes[k];
 				System.out.println("-------Vehicle---------");
 				System.out.println(v.getVehMakeId() + "|" + v.getCode() + "|" + v.getDescription());
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * @return the endPoint
 	 */
 	public String getEndPoint() {
 		return endPoint;
 	}
 
 	/**
 	 * @return the timeout
 	 */
 	public int getTimeout() {
 		return timeout;
 	}
 
 	/**
 	 * @return the instance
 	 */
 	public static SMRTProxy getInstance(String endPoint, int timeout) {
 		if (instance == null) {
 			instance = new SMRTProxy(endPoint, timeout);
 			System.out.println("INITIALIZING PROXY...");
 		} else {
 			proxy.setEndpoint(endPoint);
 			stub._setProperty(BookingWebServiceSoapStub.ENDPOINT_ADDRESS_PROPERTY, endPoint);
 			stub.setTimeout((1000 * timeout));
 		}
 		return instance;
 	}
 
 	public static void main(String[] args) {
 		// System.out.println(DateUtil.formatToDate("dd/MM/yyyy HH:mm",
 		// "17/08/2012" + " " + "11:00"));
 		// System.out.println(DateUtil.getDifferentMinutes(new Date(),
 		// DateUtil.formatToDate("dd/MM/yyyy HH:mm", "17/08/2012" + " " +
 		// "17:00")));
 		// System.out.println("92424086".matches(MOBILE_NO_PATTERN));
 		// String status = "CANCELLED";
 		// System.out.println(status.equals("PENDING") ||
 		// status.equals("CONFIRMED"));
 		// System.out.println(status.equals("FAILED") ||
 		// status.equals("CANCELLED"));
 
 		System.out.println("FIND,BOOK".contains("CHECK") && "CDG".contains("CDG"));
 
 		String keyedInAddress = "507 ANG MO KIO AVENUE 8 560507";
 		String buildingName = "";
 		String blockNumber = "";
 		String streetName = keyedInAddress.toUpperCase();
 		String postalCode = "";
 		String expressCabCode = "";
 		String taxiStandCode = "";
 
 		streetName = streetName.replaceAll("BLK", "").trim();
 
 		Pattern pattern = Pattern.compile(POSTAL_CODE_PATTERN);
 		Matcher matcher = pattern.matcher(streetName);
 		if (matcher.find()) {
 			postalCode = matcher.group().trim();
 			streetName = streetName.replaceFirst(postalCode, "").trim();
 		}
 
 		pattern = Pattern.compile(BLOCK_NO_PATTERN);
 		matcher = pattern.matcher(streetName);
 		if (matcher.find()) {
 			blockNumber = matcher.group().trim();
 			streetName = streetName.replaceFirst(blockNumber, "").trim();
 		}
 
 		System.out.println(blockNumber);
 		System.out.println(streetName);
 		System.out.println(postalCode);
 
 		Calendar bookingCal = Calendar.getInstance();
 		bookingCal.setTimeZone(TimeZone.getTimeZone("Asia/Singapore"));
 		System.out.println(bookingCal.getTimeZone());
 		System.out.println(bookingCal.get(Calendar.HOUR_OF_DAY));
 	}
 }
