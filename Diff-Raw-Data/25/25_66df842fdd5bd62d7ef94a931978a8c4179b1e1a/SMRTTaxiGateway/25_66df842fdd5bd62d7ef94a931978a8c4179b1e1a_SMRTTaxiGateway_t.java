 /**
  * Copyright (c) 2012 Maven Lab Private Limited. All rights reserved.
  */
 package com.mavenlab.caspian.interfacing.company.smrt;
 
 import java.io.Serializable;
 import java.math.BigDecimal;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Collection;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.logging.Logger;
 
 import javax.ejb.Stateless;
 import javax.inject.Inject;
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 import javax.persistence.TemporalType;
 
 import org.apache.axis.types.UnsignedByte;
 import org.tempuri.AddressPoint;
 import org.tempuri.ErrorCode;
 import org.tempuri.GetBookingObject;
 import org.tempuri.JobQueryInternetBookingView;
 
 import com.mavenlab.caspian.dto.Address;
 import com.mavenlab.caspian.dto.Addresses;
 import com.mavenlab.caspian.dto.Booking;
 import com.mavenlab.caspian.dto.Bookings;
 import com.mavenlab.caspian.dto.Job;
 import com.mavenlab.caspian.interfacing.Gateway;
 import com.mavenlab.caspian.interfacing.Request;
 import com.mavenlab.caspian.interfacing.Response;
 import com.mavenlab.caspian.interfacing.request.AddressRequest;
 import com.mavenlab.caspian.interfacing.request.BookingRequest;
 import com.mavenlab.caspian.interfacing.request.CancelJobRequest;
 import com.mavenlab.caspian.interfacing.request.GetBookingRequest;
 import com.mavenlab.caspian.interfacing.request.RedispatchJobRequest;
 import com.mavenlab.caspian.interfacing.response.AddressResponse;
 import com.mavenlab.caspian.interfacing.response.BookingResponse;
 import com.mavenlab.caspian.interfacing.response.CancelJobResponse;
 import com.mavenlab.caspian.interfacing.response.ErrorResponse;
 import com.mavenlab.caspian.interfacing.response.GetBookingResponse;
 import com.mavenlab.caspian.interfacing.response.RedispatchJobResponse;
 import com.mavenlab.caspian.model.Cancel;
 import com.mavenlab.caspian.model.ClientRequest;
 import com.mavenlab.caspian.model.TaxiType;
 import com.mavenlab.caspian.util.CollectionUtil;
 import com.mavenlab.caspian.util.DateUtil;
 
 /**
  * Class : SMRTTaxiGateway.java
  * 
  * @author <a href="mailto:yogie@mavenlab.com">Yogie Kurniawan</a>
  * @since Jul 26, 2012 2:46:49 PM
  */
 @Stateless
 public class SMRTTaxiGateway implements Gateway, Serializable {
 
 	private static final long serialVersionUID = 4858819854830446812L;
 
 	@Inject
 	private Logger log;
 
 	@PersistenceContext
 	private EntityManager em;
 	
 	/**
 	 * Get number of active booking
 	 * @param msisdn
 	 * @param startDate
 	 * @param endDate
 	 * @return {@link Long}
 	 */
 	private long getNumberOfActiveBooking(String msisdn, Date startDate, Date endDate) {
 		try {
 			return  (Long) em.createNamedQuery("caspian.entity.Booking.countMaxNumBooking")
 							.setParameter("startHour", startDate)
 							.setParameter("endHour", endDate)
 							.setParameter("msisdn", msisdn).getSingleResult();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return 0;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * com.mavenlab.caspian.interfacing.Gateway#bookingCab(com.mavenlab.caspian
 	 * .interfacing.Request)
 	 */
 	@Override
 	public Response bookingCab(Request request) {
 		BookingRequest req = (BookingRequest) request;
 
 		if (req.getAddressRef() == null || req.getNumberOfCabs() < 1 || req.getTaxiType() < 0 || req.getSchedule() == null || (req.getSchedule() != null && (!req.getSchedule().equalsIgnoreCase(Booking.ADVANCE) && !req.getSchedule().equalsIgnoreCase(Booking.NOW))) || (req.getSchedule() != null && req.getSchedule().equalsIgnoreCase("ADVANCE") && (req.getPickupDate() == null || req.getPickupTime() == null || req.getDestination() == null))) {
 			return new ErrorResponse(Response.STATUS_FAILED, req.getMessages().get("com.mavenlab.caspian.message.InvalidParameter"));
 		}
 
 		if (req.getNumberOfCabs() > req.getClientRequest().getTaxiCompany().getMaxCab()) {
 			return new ErrorResponse(Response.STATUS_FAILED, req.getMessages().get("com.mavenlab.caspian.message.InvalidNumberOfCabs"));
 		}
 
 		Calendar bookingDT = Calendar.getInstance();
 		bookingDT.setTime(new Date());
 		
 		String sMaxBookingLimit = req.getProperties().get("com.mavenlab.caspian.MaxNewBookingTimeLimit");
 		int maxNewBookingTimeLimit = sMaxBookingLimit == null ? 15 : Integer.parseInt(sMaxBookingLimit);
 		String sMaxNumBookingLimit = req.getProperties().get("com.mavenlab.caspian.MaxNumberOfNewBookingLimit");
 		int maxNumBookingLimit = sMaxNumBookingLimit == null ? 3 : Integer.parseInt(sMaxNumBookingLimit);
 		
 		Date endHour = new Date();
 		Calendar startHourCal = Calendar.getInstance();
 		startHourCal.setTime(endHour);
 		startHourCal.add(Calendar.MINUTE, (maxNewBookingTimeLimit * (-1)));
 		Date startHour = startHourCal.getTime();
 		
 		long noOfBooking = 0;
 		try {
 			String msisdn = req.getMsisdn();
			String addressRef = req.getAddressRef();
			if(addressRef.endsWith("REBOOK")) {
				addressRef = addressRef.split("-")[0];
				req.setAddressRef(addressRef);
				msisdn = msisdn + "-REBOOK";
 			}
			noOfBooking = getNumberOfActiveBooking(msisdn, startHour, endHour);

 		}catch (Exception e) {
 			e.printStackTrace();
 		}
 		if(noOfBooking >= maxNumBookingLimit) {
 			return new ErrorResponse(Response.STATUS_FAILED, req.getMessages().get("com.mavenlab.caspian.message.MaxNumberOfBookingExceed"));
 		}
 			
 		Calendar requiredPickupDT = Calendar.getInstance();
 		requiredPickupDT.setTime(bookingDT.getTime());
 
 		UnsignedByte bookingType = new UnsignedByte(1);
 		String destination = req.getDestination();
 		String paxName = req.getName();
 		
 		if (req.getSchedule().equalsIgnoreCase(Booking.ADVANCE)) {
 			bookingType.setValue(2);
 			Date advDate = req.getPickupDateTime();
 			if (advDate == null) {
 				return new ErrorResponse(673, "Invalid pickup date time format");
 			}
 
 			// if(DateUtil.getDifferentMinutes(new Date(), advDate) > (24 * 60))
 			// {
 			// return new ErrorResponse(Response.STATUS_FAILED,
 			// req.getMessages().get("Invalid pickup date time format"));
 			// }
 			requiredPickupDT.setTime(advDate);
 			destination = req.getDestination();
 		}
 
 		int pickupAddressRef = 0;
 		try {
 			pickupAddressRef = Integer.valueOf(req.getAddressRef());
 		} catch (Exception e) {
 			log.info("SMRT ADDRESS REF INVALID: " + req.getAddressRef());
 			return new ErrorResponse(669, "Address is invalid. Please try again.");
 		}
 		String pickupPoint = req.getPickupLocation();
 		short requiredVehicleCount = (short) req.getNumberOfCabs();
 		short requestedVehicleCat = (short) req.getTaxiType();
 		String paxPhone = req.getMsisdn();
 		String email = req.getEmail();
 		String bookingIPAddress = req.getClientIp();
 		
 		ClientRequest cReq = request.getClientRequest();
 		String bookingBy = cReq.getClient().getName();
 		int timeout = Integer.parseInt(request.getProperties().get("com.mavenlab.caspian.Timeout"));
 		try {
 			log.info("[" + request.getKeyword() + "][" + request.getMsisdn() + "] => CALLING API");
 			SMRTProxy proxy = SMRTProxy.getInstance(cReq.getTaxiCompany().getPrimaryURL(), timeout);
 			log.info("PICKUP DATE TIME " + requiredPickupDT);
 			log.info("PICKUP DATE TIME " + DateUtil.formatToString("dd/MM/yyyy HH:mm", requiredPickupDT.getTime()));
 			long result = proxy.submitBooking(bookingDT, requiredPickupDT, pickupAddressRef, pickupPoint, requestedVehicleCat, destination, requiredVehicleCount, paxPhone, email, bookingBy, bookingIPAddress, bookingType, paxName);
 			cReq.setStatusRequest(1);
 			log.info("[" + request.getKeyword() + "][" + request.getMsisdn() + "] => CALL API SUCCESSFULLY");
 
 			if (result > 1) {
 				com.mavenlab.caspian.model.Booking booking = new com.mavenlab.caspian.model.Booking(req);
 				booking.setBookingId(String.valueOf(result));
 				em.persist(booking);
 				createJob(booking);
 				return new BookingResponse(1, "Successful Booking");
 			}
 			log.info("[" + request.getKeyword() + "][" + request.getMsisdn() + "] => SMRT BOOKING FAILED - " + result);
 			return new ErrorResponse(Response.STATUS_FAILED, "Booking Failed");
 		} catch (Exception e) {
 			if (timeout(e.getCause())) {
 				try {
 					log.info("[" + request.getKeyword() + "][" + request.getMsisdn() + "] => CALLING SECONDARY API DUE TO TIMEOUT");
 					SMRTProxy proxy = SMRTProxy.getInstance(cReq.getTaxiCompany().getSecondaryURL(), timeout);
 					long result = proxy.submitBooking(bookingDT, requiredPickupDT, pickupAddressRef, pickupPoint, requestedVehicleCat, destination, requiredVehicleCount, paxPhone, email, bookingBy, bookingIPAddress, bookingType, paxName);
 					cReq.setStatusRequest(1);
 					log.info("[" + request.getKeyword() + "][" + request.getMsisdn() + "] => CALL API SUCCESSFULLY");
 
 					if (result > 1) {
 						com.mavenlab.caspian.model.Booking booking = new com.mavenlab.caspian.model.Booking(req);
 						booking.setBookingId(String.valueOf(result));
 						em.persist(booking);
 						createJob(booking);
 						return new BookingResponse(1, "Successful Booking");
 					}
 					log.info("[" + request.getKeyword() + "][" + request.getMsisdn() + "] => SMRT BOOKING FAILED - " + result);
 					return new ErrorResponse(Response.STATUS_FAILED, "Booking Failed");
 				} catch (Exception e1) {
 					e1.printStackTrace();
 				}
 			}
 			return new ErrorResponse(666, "Service Unavailable");
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * com.mavenlab.caspian.interfacing.Gateway#getActiveBookingList(com.mavenlab
 	 * .caspian.interfacing.Request)
 	 */
 	@Override
 	public Response getActiveBookingList(Request request) {
 		GetBookingRequest req = (GetBookingRequest) request;
 
 		ClientRequest cReq = request.getClientRequest();
 
 		String bTimeLimit = req.getProperties().get("com.mavenlab.caspian.BookingTimeLimit");
 		int timeLimit = (bTimeLimit == null) ? 15 : Integer.parseInt(bTimeLimit);
 
 		String cLimit = req.getProperties().get("com.mavenlab.caspian.GetBookingRequestLimit");
 		int checkLimit = (cLimit == null) ? 3 : Integer.parseInt(cLimit);
 
 		String cWait = req.getProperties().get("com.mavenlab.caspian.GetBookingTimeWaiting");
 		int checkWait = (cWait == null) ? 30 : Integer.parseInt(cWait);
 
 		Date date = new Date(System.currentTimeMillis() - (timeLimit * 60 * 1000));
 		List<GetBookingObject> bookings = new ArrayList<GetBookingObject>();
 
 		List<com.mavenlab.caspian.model.Booking> smrtBookings = em.createQuery("FROM com.mavenlab.caspian.model.Booking WHERE status = 'active' AND msisdn = :msisdn AND pickupDate > :date AND bookingId IS NOT NULL AND taxiCompany = 'SMRT' ORDER BY bookingDate DESC").setParameter("msisdn", req.getMsisdn()).setParameter("date", date).getResultList();
 		Map<String, List<com.mavenlab.caspian.model.Job>> bookingJobs = new HashMap<String, List<com.mavenlab.caspian.model.Job>>();
 		List<Booking> localBookings = new ArrayList<Booking>();
 
 		if (smrtBookings.size() == 0) {
 			log.info("No Booking Found.");
 			return constructBookingResponse(bookings, localBookings, bookingJobs, req);
 		}
 		int timeout = Integer.parseInt(request.getProperties().get("com.mavenlab.caspian.Timeout"));
 
 		long cTime = new Date(System.currentTimeMillis()).getTime();
 		int noOfChecks = 0;
 
 		for (com.mavenlab.caspian.model.Booking b : smrtBookings) {	
 			boolean exist = false;
 			long bTime = b.getUpdatedAt().getTime();
 			noOfChecks = (b.getNoOfChecks() + 1);
 			checkWait = noOfChecks == 1 ? 9 : checkWait;
 			
 			List<com.mavenlab.caspian.model.Job> jobs = em.createNamedQuery("caspian.entity.job.findByBookingId").setParameter("bookingId", b.getId()).getResultList();
 			log.info("BOOKING ID " + b.getId() + " - JOBS SIZE " + jobs.size());
 			if(!bookingJobs.containsKey(b.getBookingId()))
 				bookingJobs.put(b.getBookingId(), jobs);
 			else {
 				bookingJobs.get(b.getBookingId()).addAll(jobs);
 				exist = true;
 			} 
 			
 			if(isJobNeedUpdate(jobs) && noOfChecks > checkLimit) {
 				noOfChecks = checkLimit;
 				log.info("[" + request.getKeyword() + "][" + request.getMsisdn() + "] => BOOKING #" + b.getId() + " STILL NEED UPDATE");
 			}
 				
 			if (noOfChecks > checkLimit || (cTime - bTime) < (checkWait * 1000)) {
 				if (jobs != null && jobs.size() > 0) {
 					log.info("[" + request.getKeyword() + "][" + request.getMsisdn() + "] => GET LOCAL JOBS");
 					localBookings.addAll(constructBookingByJobs(jobs, b));
 				}
 				continue;
 			}
 			
 
 			try {		
 				if(exist) continue;
 				
 				log.info("[" + request.getKeyword() + "][" + request.getMsisdn() + "] => CALLING API");
 				SMRTProxy proxy = SMRTProxy.getInstance(cReq.getTaxiCompany().getPrimaryURL(), timeout);
 				List<GetBookingObject> objs = proxy.getBooking(b.getBookingId(), req.getMsisdn());
 				if (objs != null && objs.size() > 0)
 					bookings.addAll(objs);
 				
 				if(isStoppedBooking(objs))
 					em.createNativeQuery("UPDATE bookings SET no_of_checks = :num, updated_at = :date WHERE id = :id").setParameter("num", checkLimit).setParameter("id", b.getId()).setParameter("date", new Date(), TemporalType.TIMESTAMP).executeUpdate();
 				else
 					em.createNativeQuery("UPDATE bookings SET no_of_checks = (no_of_checks + 1), updated_at = :date WHERE id = :id").setParameter("id", b.getId()).setParameter("date", new Date(), TemporalType.TIMESTAMP).executeUpdate();
 				
 				log.info("[" + request.getKeyword() + "][" + request.getMsisdn() + "] => CALL API SUCCESSFULLY");
 
 			} catch (Exception e) {
 				if (timeout(e.getCause())) {
 					try {
 						log.info("[" + request.getKeyword() + "][" + request.getMsisdn() + "] => CALLING SECONDARY API");
 
 						SMRTProxy proxy = SMRTProxy.getInstance(cReq.getTaxiCompany().getSecondaryURL(), timeout);
 						List<GetBookingObject> objs = proxy.getBooking(b.getBookingId(), req.getMsisdn());
 						if (objs != null && objs.size() > 0)
 							bookings.addAll(objs);
 						
 						if(isStoppedBooking(objs))
 							em.createNativeQuery("UPDATE bookings SET no_of_checks = 3 WHERE id = :id").setParameter("id", b.getId()).executeUpdate();
 						else
 							em.createNativeQuery("UPDATE bookings SET no_of_checks = (no_of_checks + 1) WHERE id = :id").setParameter("id", b.getId()).executeUpdate();						
 						
 						log.info("[" + request.getKeyword() + "][" + request.getMsisdn() + "] => CALL SECONDARY API SUCCESSFULLY");
 
 					} catch (Exception e1) {
 						e1.printStackTrace();
 					}
 				}
 				return new ErrorResponse(666, "Service Unavailable");
 			}
 		}
 
 		cReq.setStatusRequest(1);
 		GetBookingResponse response = (GetBookingResponse) constructBookingResponse(bookings, localBookings, bookingJobs, req);
 		if (!localBookings.isEmpty()) {
 			for(Booking b : localBookings) {
 				if(!b.isDispatch())
 					response.getBookings().getBookings().add(b);
 			}
 		}
 		CollectionUtil.sort(response.getBookings().getBookings(), "bookingDateMilis", false);
 		return response;
 	}
 
 	/**
 	 * Check if jobs need an update
 	 * @param jobs
 	 * @return {@link Boolean}
 	 */
 	private boolean isJobNeedUpdate(List<com.mavenlab.caspian.model.Job> jobs) {
 		for(com.mavenlab.caspian.model.Job job : jobs) {
 			if(!job.getJobStatus().equals(Job.FAILED) && !job.getJobStatus().equals(Job.CONFIRMED) && !job.getJobStatus().equals(Job.CANCELLED) && !job.getJobStatus().equals(Job.COMPLETED) && !job.getJobStatus().equals(Job.INCOMPLETED)) {
 				return true;
 			}
 		}
 		return false;
 	}
 	
 	/**
 	 * Check if job need an update
 	 * @param jobs
 	 * @return {@link Boolean}
 	 */
 	private boolean isJobNeedUpdate(com.mavenlab.caspian.model.Job job) {
 		if(!job.getJobStatus().equals(Job.FAILED) && !job.getJobStatus().equals(Job.CONFIRMED) && !job.getJobStatus().equals(Job.CANCELLED) && !job.getJobStatus().equals(Job.COMPLETED) && !job.getJobStatus().equals(Job.INCOMPLETED)) {
 			return true;
 		}
 		return false;
 	}
 
 	/**
 	 * Construct Booking by Jobs
 	 * 
 	 * @param jobs
 	 * @param booking
 	 * @return {@link Collection}
 	 */
 	private Collection<? extends Booking> constructBookingByJobs(List<com.mavenlab.caspian.model.Job> jobs, com.mavenlab.caspian.model.Booking booking) {
 		List<Booking> bookings = new ArrayList<Booking>();
 		for (com.mavenlab.caspian.model.Job j : jobs) {
 			Booking b = new Booking();
 			b.setStatus(j.getJobStatus());
 			b.setJobno(String.valueOf(j.getJobId()));
 
 			String address = booking.getBlockNo() + " " + booking.getRoadName();
 			if (booking.getBuildingName() != null && !booking.getBuildingName().isEmpty()) {
 				address += " " + booking.getBuildingName();
 			}
 			address += " " + booking.getPostalCode();
 			b.setAddress(address);
 			b.setPickupLocation(booking.getPickupPoint());
 			b.setTaxiType(j.getTaxiType());
 			b.setSchedule(booking.getSchedule());
 
 			// Calendar pickupCal =
 			// DateUtil.clearTimeZoneFormat(job.getRequiredPickupDt());
 			b.setPickupDate(DateUtil.formatToString("dd/MM/yyyy", booking.getPickupDate()));
 			b.setPickupTime(DateUtil.formatToString("HH:mm", booking.getPickupDate()) + " HRS");
 
 			b.setBookingDate(DateUtil.formatToString("dd/MM/yyyy", booking.getBookingDate()));
 			b.setBookingTime(DateUtil.formatToString("HH:mm", booking.getBookingDate()) + " HRS");
 
 			b.setDestination(booking.getDestination());
 
 			b.setBookingFee(j.getBookingFee());
 
 			b.setTaxiNumber(j.getTaxiNumber());
 
 			b.setEta(j.getEta());
 
 			b.setDispatch(j.isDispatch());
 			bookings.add(b);
 		}
 		return bookings;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * com.mavenlab.caspian.interfacing.Gateway#cancelJob(com.mavenlab.caspian
 	 * .interfacing.Request)
 	 */
 	@Override
 	public Response cancelJob(Request request) {
 		CancelJobRequest req = (CancelJobRequest) request;
 		if (req.getJobNo() == null) {
 			return new ErrorResponse(Response.STATUS_FAILED, req.getMessages().get("com.mavenlab.caspian.message.InvalidParameter"));
 		}
 		ClientRequest cReq = request.getClientRequest();
 		int timeout = Integer.parseInt(request.getProperties().get("com.mavenlab.caspian.Timeout"));
 
 		com.mavenlab.caspian.model.Job job = null;
 		boolean reebook = req.getJobNo().endsWith("REBOOK") ? true : false;
 		try {
 			String jobNo = req.getJobNo();
 			if(reebook) {
 				jobNo = jobNo.split("-")[0];
 			}
 			job = (com.mavenlab.caspian.model.Job) em.createNamedQuery("caspian.entity.job.findByJobId").setParameter("jobId", Long.valueOf(jobNo)).getSingleResult();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		if (job == null) {
 			return new ErrorResponse(Response.STATUS_FAILED, req.getMessages().get("com.mavenlab.caspian.message.InvalidJobNo"));
 		} else if (job.getJobStatus().equals(com.mavenlab.caspian.model.Job.CANCELLED)) {
 			return new ErrorResponse(Response.STATUS_FAILED, req.getMessages().get("com.mavenlab.caspian.message.JobAlreadyCancelled"));
 		//} else if (!(job.getJobStatus().equals(com.mavenlab.caspian.model.Job.PROCESSING) || job.getJobStatus().equals(com.mavenlab.caspian.model.Job.CONFIRMED))) {
 		//	return new ErrorResponse(Response.STATUS_FAILED, req.getMessages().get("com.mavenlab.caspian.message.JobCannotBeCancelled"));
 		} else if (job.getStatus().equals(com.mavenlab.caspian.model.Job.PENDING)) {
 			return new ErrorResponse(Response.STATUS_FAILED, req.getMessages().get("com.mavenlab.caspian.message.JobCannotBeCancelled"));
 		} else if (job.getStatus().equals(com.mavenlab.caspian.model.Job.ONBOARD)) {
 			return new ErrorResponse(Response.STATUS_FAILED, req.getMessages().get("com.mavenlab.caspian.message.JobAlreadyOnBoard"));
 		} else if (job.getStatus().equals(com.mavenlab.caspian.model.Job.COMPLETED)) {
 			return new ErrorResponse(Response.STATUS_FAILED, req.getMessages().get("com.mavenlab.caspian.message.JobAlreadyCompleted"));
 		}
 
 		String cLimit = req.getProperties().get("com.mavenlab.caspian.GetBookingRequestLimit");
 		int checkLimit = (cLimit == null) ? 3 : Integer.parseInt(cLimit);
 		
 		com.mavenlab.caspian.model.Booking booking = em.find(com.mavenlab.caspian.model.Booking.class, job.getBookingId());
 
 		try {
 			log.info("[" + request.getKeyword() + "][" + request.getMsisdn() + "] => CALLING API FOR CHECK JOB");
 			SMRTProxy proxy = SMRTProxy.getInstance(cReq.getTaxiCompany().getPrimaryURL(), timeout);
 			List<GetBookingObject> bookings = proxy.getBooking(booking.getBookingId(), req.getMsisdn(), job.getJobNo());
 			JobQueryInternetBookingView jobObject = null;
 			for(GetBookingObject b : bookings) {
 				JobQueryInternetBookingView j = b.getBookingJob();
 				if(j.getJobId() == Long.valueOf(job.getJobNo())) {
 					jobObject = j;
 					break;
 				}
 			}
 			if(jobObject == null) {
 				return new ErrorResponse(Response.STATUS_FAILED, req.getMessages().get("com.mavenlab.caspian.message.JobCannotBeCancelled"));
 			} else {
 				ErrorResponse errorResponse = null;
 				int jobStatus = jobObject.getJobStatus().intValue();
 				if(jobStatus == 10) {
 					job.setJobStatus(com.mavenlab.caspian.model.Job.CANCELLED);
 					errorResponse = new ErrorResponse(Response.STATUS_FAILED, req.getMessages().get("com.mavenlab.caspian.message.JobAlreadyCancelled"));
 				} else if(jobStatus == 9) {
 					job.setJobStatus(com.mavenlab.caspian.model.Job.ONBOARD);
 					errorResponse = new ErrorResponse(Response.STATUS_FAILED, req.getMessages().get("com.mavenlab.caspian.message.JobAlreadyOnBoard"));
 				} else if(jobStatus == 12) {
 					job.setJobStatus(com.mavenlab.caspian.model.Job.COMPLETED);
 					errorResponse = new ErrorResponse(Response.STATUS_FAILED, req.getMessages().get("com.mavenlab.caspian.message.JobAlreadyCompleted"));
 				} else if(jobStatus == 2 || jobStatus == 5) {
 					if(!reebook) {
 						job.setJobStatus(com.mavenlab.caspian.model.Job.FAILED);
 						errorResponse = new ErrorResponse(Response.STATUS_FAILED, req.getMessages().get("com.mavenlab.caspian.message.JobCannotBeCancelled"));
 					}
 				}
 
 				if(errorResponse != null) {
 					em.merge(job);
 					em.flush();
 					return errorResponse;
 				}
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		
 		try {
 			log.info("[" + request.getKeyword() + "][" + request.getMsisdn() + "] => CALLING API");
 			SMRTProxy proxy = SMRTProxy.getInstance(cReq.getTaxiCompany().getPrimaryURL(), timeout);
 			ErrorCode code = proxy.cancelJob(job.getJobNo(), cReq.getClient().getName());
 
 			cReq.setStatusRequest(1);
 			log.info("[" + request.getKeyword() + "][" + request.getMsisdn() + "] => CALL API SUCCESSFULLY");
 
 			if (code.getReturnCode() <= 0) {
 				return new ErrorResponse(Response.STATUS_FAILED, code.getCodeDescription());
 			}
 
 			Cancel cancel = new Cancel(req);
 			em.persist(cancel);
 			
 			booking.setNoOfChecks(0);
 			em.merge(booking);
 			em.flush();
 
 			return new CancelJobResponse(code.getReturnCode(), "Successful Cancelation");
 		} catch (Exception e) {
 			if (timeout(e.getCause())) {
 				try {
 					log.info("[" + request.getKeyword() + "][" + request.getMsisdn() + "] => CALLING SECONDARY API DUE TO TIMEOUT");
 					SMRTProxy proxy = SMRTProxy.getInstance(cReq.getTaxiCompany().getSecondaryURL(), timeout);
 					ErrorCode code = proxy.cancelJob(req.getJobNo(), cReq.getClient().getName());
 					cReq.setStatusRequest(1);
 					log.info("[" + request.getKeyword() + "][" + request.getMsisdn() + "] => CALL API SUCCESSFULLY");
 
 					if (code.getReturnCode() <= 0) {
 						return new ErrorResponse(Response.STATUS_FAILED, code.getCodeDescription());
 					}
 
 					Cancel cancel = new Cancel(req);
 					em.persist(cancel);
 
 					booking.setNoOfChecks(0);
 					em.merge(booking);
 					em.flush();
 					
 					return new CancelJobResponse(code.getReturnCode(), "Successful Cancelation");
 				} catch (Exception e1) {
 					e1.printStackTrace();
 				}
 			}
 			return new ErrorResponse(666, "Service Unavailable");
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * com.mavenlab.caspian.interfacing.Gateway#findAddress(com.mavenlab.caspian
 	 * .interfacing.Request)
 	 */
 	@Override
 	public Response findAddress(Request request) {
 		AddressRequest req = (AddressRequest) request;
 		if (req.getKeyedInAddress() == null) {
 			return new ErrorResponse(Response.STATUS_FAILED, req.getMessages().get("com.mavenlab.caspian.message.InvalidParameter"));
 		}
 		ClientRequest cReq = request.getClientRequest();
 		int timeout = Integer.parseInt(request.getProperties().get("com.mavenlab.caspian.Timeout"));
 
 		try {
 			log.info("[" + request.getKeyword() + "][" + request.getMsisdn() + "] => CALLING API");
 			SMRTProxy proxy = SMRTProxy.getInstance(cReq.getTaxiCompany().getPrimaryURL(), timeout);
 			List<AddressPoint> addresses = proxy.getAddressRef(req.getKeyedInAddress());
 			cReq.setStatusRequest(1);
 			log.info("[" + request.getKeyword() + "][" + request.getMsisdn() + "] => CALL API SUCCESSFULLY");
 
 			return constructAddressResponse(addresses);
 		} catch (Exception e) {
 			if (timeout(e.getCause())) {
 				try {
 					log.info("[" + request.getKeyword() + "][" + request.getMsisdn() + "] => CALLING SECONDARY API DUE TO TIMEOUT");
 					SMRTProxy proxy = SMRTProxy.getInstance(cReq.getTaxiCompany().getSecondaryURL(), timeout);
 					List<AddressPoint> addresses = proxy.getAddressRef(req.getKeyedInAddress());
 					cReq.setStatusRequest(1);
 					log.info("[" + request.getKeyword() + "][" + request.getMsisdn() + "] => CALL API SUCCESSFULLY");
 
 					return constructAddressResponse(addresses);
 				} catch (Exception e1) {
 					e1.printStackTrace();
 				}
 			}
 			return new ErrorResponse(666, "Service Unavailable");
 		}
 	}
 	
 	/**
 	 * Check for failed booking
 	 * @return
 	 */
 	private boolean isStoppedBooking(List<GetBookingObject> objects) {
 		for(GetBookingObject obj : objects) {
 			JobQueryInternetBookingView job = obj.getBookingJob();
 			int status = job.getJobStatus().intValue();
 			if(status != 2 && status != 5 && status != 8 &&status != 10 && status != 9 && status != 12 && status != 13) {
 				return false;
 			}
 		}
 		return true;
 	}
 
 	/**
 	 * Create Job data
 	 * 
 	 * @param booking
 	 */
 	private void createJob(com.mavenlab.caspian.model.Booking booking) {
 		for (int i = 0; i < booking.getNumberOfCabs(); i++) {
 			com.mavenlab.caspian.model.Job job = new com.mavenlab.caspian.model.Job(booking);
 			em.persist(job);
 		}
 	}
 
 	/**
 	 * Construct Get Booking List
 	 * @param localBookings 
 	 * 
 	 * @param bookings
 	 * @param req
 	 * @return
 	 */
 	private Response constructBookingResponse(List<GetBookingObject> bookingObjects, List<Booking> localBookings, Map<String, List<com.mavenlab.caspian.model.Job>> bookingJobs, Request req) {
 		GetBookingResponse response = new GetBookingResponse();
 
 		String bTimeLimit = req.getProperties().get("com.mavenlab.caspian.BookingTimeLimit");
 		int timeLimit = (bTimeLimit == null) ? 15 : Integer.parseInt(bTimeLimit);
 		Date date = new Date(System.currentTimeMillis() - (timeLimit * 60 * 1000));
 
 		Bookings bookings = new Bookings();
 		for (GetBookingObject o : bookingObjects) {
 			JobQueryInternetBookingView job = o.getBookingJob();
 			log.info("[" + req.getKeyword() + "][" + req.getMsisdn() + "] => PROCESSING JOB => JOBID=" + job.getJobId() + "|STATUSID=" + job.getJobStatus().intValue() +"|BOOKINGID=" + job.getBookingId());
 
 			com.mavenlab.caspian.model.Job localJob = getJob(bookingJobs.get(String.valueOf(job.getBookingId())), job.getJobId());
 			
 			if(localJob!=null) {
 				if(localJob.getJobStatus().equals(com.mavenlab.caspian.model.Job.FAILED) && localJob.isDispatch())
 					localJob = null;
 				else if(localJob.getJobStatus().equals(com.mavenlab.caspian.model.Job.CANCELLED) && localJob.isDispatch())
 					continue;
 			}
 			
 			if (localJob == null) {	
 				localJob = getEmptyJobNo(bookingJobs.get(String.valueOf(job.getBookingId())));
 				if (localJob == null) {
 					continue;
 				} 
 				log.info("[" + req.getKeyword() + "][" + req.getMsisdn() + "] => NEW EMPTY JOB => JOBNO=" + localJob.getJobId() + "|JOBID="+ localJob.getJobNo() + "|STATUSID=" + job.getJobStatus().intValue());
 			} else {
 				log.info("[" + req.getKeyword() + "][" + req.getMsisdn() + "] => LOCAL JOB FOUND => JOBNO=" + localJob.getJobId() + "|JOBID="+ localJob.getJobNo() + "|STATUSID=" + job.getJobStatus().intValue());
 			}
 			
 			//localJob = em.find(com.mavenlab.caspian.model.Job.class, localJob.getId());
 			//log.info("LOCAL JOB: " + localJob.getId());	
 			int status = job.getJobStatus().intValue();
 			String jobStatus = Job.PENDING;
 			switch (status) {
 			case 8:
 				jobStatus = Job.CONFIRMED;
 				break;
 			case 9:
 				jobStatus = Job.ONBOARD;
 				break;
 			case 12:
 				jobStatus = Job.COMPLETED;
 				break;
 			case 13:
 				jobStatus = Job.INCOMPLETED;
 				break;
 			case 10:
 				jobStatus = Job.CANCELLED;
 				break;
 			case 2:
 				jobStatus = Job.FAILED;
 				break;
 			case 5:
 				jobStatus = Job.FAILED;
 				break;
 			default:
 				jobStatus = Job.PROCESSING;
 				break;
 			}
 
 			/**
 			 * Change logic
 			 */
 			Calendar pickupCal = DateUtil.clearTimeZoneFormat(job.getRequiredPickupDt());
 			if (jobStatus.equals(Job.FAILED) || jobStatus.equals(Job.CANCELLED) || jobStatus.equals(Job.CONFIRMED)) {
 				if (pickupCal.getTime().before(date)) {
 					// log.info("JOBNO=" +
 					// job.getJobId()+"|STATUS="+jobStatus+"|MSISDN=" +
 					// job.getPaxPhone());
 					continue;
 				}
 			}
 
 			Booking booking = new Booking();
 			booking.setStatus(jobStatus);
 			booking.setJobno(String.valueOf(localJob.getJobId()));
 
 			localJob.setJobStatus(booking.getStatus());
 			localJob.setJobNo(String.valueOf(job.getJobId()));
 			
 			log.info("[" + req.getKeyword() + "][" + req.getMsisdn() + "] => UPDATE JOB => JOBNO=" + booking.getJobno() + "|JOBID="+ job.getJobId() + "|STATUS=" + jobStatus + "|STATUSID=" + status);
 
 			String address = job.getPickupBlock() + " " + job.getPickupRoad();
 			if (job.getPickupBuilding() != null && !job.getPickupBuilding().isEmpty()) {
 				address += " " + job.getPickupBuilding();
 			}
 			address += " " + job.getPickupPostalCode();
 			booking.setAddress(address);
 			booking.setPickupLocation(job.getPickupPoint());
 
 			int taxiId = job.getVehCatId();
 			booking.setTaxiType("");
 			if (taxiId > 1) {
 				try {
 					TaxiType taxiType = (TaxiType) em.createNamedQuery("caspian.entity.TaxiType.findByCompanyCategoryId").setParameter("companyId", 2).setParameter("categoryId", taxiId).getSingleResult();
 					booking.setTaxiType(taxiType.getName());
 				} catch (Exception e) {
 					log.info("TAXI NOT FOUND: " + taxiId);
 				}
 			}
 			localJob.setTaxiType(booking.getTaxiType());
 
 			String schedule = job.getBookingType().intValue() == 1 ? Booking.NOW : Booking.ADVANCE;
 			booking.setSchedule(schedule);
 
 			// Calendar pickupCal =
 			// DateUtil.clearTimeZoneFormat(job.getRequiredPickupDt());
 			booking.setPickupDate(DateUtil.formatToString("dd/MM/yyyy", pickupCal.getTime()));
 			booking.setPickupTime(DateUtil.formatToString("HH:mm", pickupCal.getTime()) + " HRS");
 
 			Calendar bookingCal = DateUtil.clearTimeZoneFormat(job.getBookingDt());
 			//booking.setBookingDate(DateUtil.formatToString("dd/MM/yyyy", bookingCal.getTime()));
 			//booking.setBookingTime(DateUtil.formatToString("HH:mm", bookingCal.getTime()) + " HRS");
 			booking.setBookingDate(DateUtil.formatToString("dd/MM/yyyy", localJob.getCreatedAt()));
 			booking.setBookingTime(DateUtil.formatToString("HH:mm", localJob.getCreatedAt()) + " HRS");
 			
 			booking.setDestination(job.getDestination());
 
 			BigDecimal fee = job.getBookingFee();
 			fee = fee.setScale(2, BigDecimal.ROUND_DOWN);
 			booking.setBookingFee(fee.toString());
 			localJob.setBookingFee(booking.getBookingFee());
 
 			booking.setTaxiNumber(job.getVehicleNum());
 			localJob.setTaxiNumber(booking.getTaxiNumber());
 
 			booking.setEta("");
 			if (jobStatus.equals(Job.CONFIRMED)) {
 				Calendar etaCal = DateUtil.clearTimeZoneFormat(job.getEta());
 				booking.setEta("" + etaCal.get(Calendar.MINUTE));
 			}
 			
 			localJob.setEta(booking.getEta());
 			em.merge(localJob);
 			em.flush();
 			booking.setDispatch(localJob.isDispatch());
 			
 			Booking localBooking = getLocalBooking(localBookings, booking.getJobno());
 			if(localBooking!=null) {
 				localBookings.remove(localBooking);
 			}
 			if(!booking.isDispatch())
 				bookings.getBookings().add(booking);
 
 		}
 		response.setBookings(bookings);
 		response.setStatus(1);
 		response.setMessage("");
 		return response;
 	}
 
 	/**
 	 * @param localBookings
 	 * @param jobno
 	 * @return
 	 */
 	private Booking getLocalBooking(List<Booking> localBookings, String jobno) {
 		for(Booking b : localBookings) {				
 			if(b.getJobno().equals(jobno)) {
 				return b;
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * Get a Job
 	 * 
 	 * @param jobs
 	 * @param jobId
 	 * @return {@link Job}
 	 */
 	private com.mavenlab.caspian.model.Job getJob(List<com.mavenlab.caspian.model.Job> jobs, long jobId) {
 		log.info("LOCAL JOB SIZE => " + jobs.size());
 		for (com.mavenlab.caspian.model.Job j : jobs) {
 			log.info(j.getJobNo() + " - " + jobId);
 			if (j.getJobNo() != null) {
 				log.info(j.getJobNo() + " CHECKING..");
 				long jobno = Long.valueOf(j.getJobNo());
 				if (jobno == jobId) {
 					return j;
 				}
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * Get a Job
 	 * 
 	 * @param jobs
 	 * @param jobId
 	 * @return {@link Job}
 	 */
 	private com.mavenlab.caspian.model.Job getEmptyJobNo(List<com.mavenlab.caspian.model.Job> jobs) {
 		for (com.mavenlab.caspian.model.Job j : jobs) {
 			if (j.getJobNo() == null) {
 				return j;
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * Construct Addresss Response
 	 * 
 	 * @param addresses
 	 * @return {@link Response}
 	 */
 	private Response constructAddressResponse(List<AddressPoint> addressPoints) {
 		if (addressPoints.size() == 0) {
 			return new ErrorResponse(669, "Address is invalid. Please try again.");
 		}
 
 		AddressResponse response = new AddressResponse();
 		Addresses adds = new Addresses();
 		List<Address> addresses = new ArrayList<Address>();
 		Map<String, String> mapAddress = new HashMap<String, String>();
 		for (AddressPoint a : addressPoints) {
 			Address address = new Address();
 			address.setAddressReference(String.valueOf(a.getAddressRef()));
 			address.setBlock(a.getBlock());
 			address.setBuildingName(a.getBuilding());
 			address.setLatitude(String.valueOf(a.getAddy().doubleValue()));
 			address.setLongitude(String.valueOf(a.getAddx().doubleValue()));
 			address.setPostalCode(a.getPostalCode());
 			address.setRoadName(a.getRoad());
 
 			// Filter duplicate address
 			if (mapAddress.containsKey(address.getFullAddress())) {
 				continue;
 			} else {
 				mapAddress.put(address.getFullAddress(), address.getFullAddress());
 			}
 
 			addresses.add(address);
 		}
 		adds.setAddresses(addresses);
 		response.setAddresses(adds);
 		return response;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * com.mavenlab.caspian.interfacing.Gateway#convertLatitudeLongitude(com
 	 * .mavenlab.caspian.interfacing.Request)
 	 */
 	@Override
 	public Response convertLatitudeLongitude(Request request) {
 		return new ErrorResponse(Response.STATUS_FAILED, "API is not available.");
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * com.mavenlab.caspian.interfacing.Gateway#getLatestThreePickup(com.mavenlab
 	 * .caspian.interfacing.Request)
 	 */
 	@Override
 	public Response getLatestThreePickup(Request request) {
 		return new ErrorResponse(Response.STATUS_FAILED, "API is not available.");
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * com.mavenlab.caspian.interfacing.Gateway#jobQuery(com.mavenlab.caspian
 	 * .interfacing.Request)
 	 */
 	@Override
 	public Response jobQuery(Request request) {
 		return new ErrorResponse(Response.STATUS_FAILED, "API is not available.");
 	}
 
 	private boolean timeout(Throwable t) {
 		log.info(t.getMessage() + " - " + t.toString());
 		if (t != null && (t.toString().contains("java.net.ConnectException") || t.toString().contains("java.net.SocketTimeoutException"))) {
 			log.info("Connection timeout, starting failover calling..");
 			return true;
 		}
 		return false;
 	}
 
 	/* (non-Javadoc)
 	 * @see com.mavenlab.caspian.interfacing.Gateway#redispatchJob(com.mavenlab.caspian.interfacing.Request)
 	 */
 	@Override
 	public Response redispatchJob(Request request) {
 		RedispatchJobRequest req = (RedispatchJobRequest) request;
 		if (req.getJobNo() == null) {
 			return new ErrorResponse(Response.STATUS_FAILED, req.getMessages().get("com.mavenlab.caspian.message.InvalidParameter"));
 		}
 		ClientRequest cReq = request.getClientRequest();
 		int timeout = Integer.parseInt(request.getProperties().get("com.mavenlab.caspian.Timeout"));
 
 		com.mavenlab.caspian.model.Job job = null;
 		try {
 			job = (com.mavenlab.caspian.model.Job) em.createNamedQuery("caspian.entity.job.findByJobId").setParameter("jobId", Long.valueOf(req.getJobNo())).getSingleResult();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		if (job == null) {
 			return new ErrorResponse(Response.STATUS_FAILED, req.getMessages().get("com.mavenlab.caspian.message.InvalidJobNo"));
 		} 
 
 		try {
 			log.info("[" + request.getKeyword() + "][" + request.getMsisdn() + "] => CALLING API");
 			SMRTProxy proxy = SMRTProxy.getInstance(cReq.getTaxiCompany().getPrimaryURL(), timeout);
 			ErrorCode code = proxy.redispatchJob(Long.valueOf(job.getJobNo()));
 
 			cReq.setStatusRequest(1);
 			log.info("[" + request.getKeyword() + "][" + request.getMsisdn() + "] => CALL API SUCCESSFULLY");
 
 			if (code.getReturnCode() <= 0) {
 				return new ErrorResponse(Response.STATUS_FAILED, code.getCodeDescription());
 			}
 			
 			/**
 			 * Creating redispatch booking data
 			 */
 			createRedispatchBookingData(job);
 			
 			return new RedispatchJobResponse(code.getReturnCode(), "Successful Redispatch Job");
 		} catch (Exception e) {
 			if (timeout(e.getCause())) {
 				try {
 					log.info("[" + request.getKeyword() + "][" + request.getMsisdn() + "] => CALLING SECONDARY API DUE TO TIMEOUT");
 					SMRTProxy proxy = SMRTProxy.getInstance(cReq.getTaxiCompany().getSecondaryURL(), timeout);
 					ErrorCode code = proxy.redispatchJob(Long.valueOf(req.getJobNo()));
 					cReq.setStatusRequest(1);
 					log.info("[" + request.getKeyword() + "][" + request.getMsisdn() + "] => CALL API SUCCESSFULLY");
 
 					if (code.getReturnCode() <= 0) {
 						return new ErrorResponse(Response.STATUS_FAILED, code.getCodeDescription());
 					}
 
 					createRedispatchBookingData(job);
 
 					return new RedispatchJobResponse(code.getReturnCode(), "Successful Cancelation");
 				} catch (Exception e1) {
 					e1.printStackTrace();
 				}
 			}
 			return new ErrorResponse(666, "Service Unavailable");
 		}
 	}
 
 	/**
 	 * @param job
 	 * @throws Exception 
 	 */
 	private void createRedispatchBookingData(com.mavenlab.caspian.model.Job job) throws Exception {
 		try {
 			com.mavenlab.caspian.model.Booking booking = em.find(com.mavenlab.caspian.model.Booking.class, job.getBookingId());
 		
 			com.mavenlab.caspian.model.Booking newBooking = new com.mavenlab.caspian.model.Booking(booking);
 			em.persist(newBooking);
 			
 			job.setDispatch(Boolean.TRUE);
 			em.merge(job);
 			em.flush();
 			
 			com.mavenlab.caspian.model.Job newJob = new com.mavenlab.caspian.model.Job(newBooking);
 			if(job.getJobStatus().equals(com.mavenlab.caspian.model.Job.FAILED)) {
 				newJob.setJobNo(job.getJobNo());
 			}
 			em.persist(newJob);
 			
 			//createJob(newBooking);
 		} catch (Exception e) {
 			e.printStackTrace();
 			throw new Exception(e.getCause());
 		}
 		
 	}
 }
