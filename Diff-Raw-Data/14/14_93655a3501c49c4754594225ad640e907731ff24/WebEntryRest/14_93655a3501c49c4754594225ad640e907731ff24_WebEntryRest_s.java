 package com.mavenlab.jetset.rest;
 
 import java.util.concurrent.TimeUnit;
 
 import javax.ejb.Stateless;
 import javax.inject.Inject;
 import javax.persistence.EntityManager;
 import javax.servlet.http.HttpServletResponse;
 import javax.ws.rs.FormParam;
 import javax.ws.rs.POST;
 import javax.ws.rs.Path;
 import javax.ws.rs.Produces;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.MediaType;
 
 import org.jboss.logging.Logger;
 import org.jboss.seam.solder.logging.Category;
 
 import com.mavenlab.jetset.controller.EntryController;
 import com.mavenlab.jetset.controller.PrizeController;
 import com.mavenlab.jetset.controller.StationController;
 import com.mavenlab.jetset.model.Prize;
 import com.mavenlab.jetset.model.Station;
 import com.mavenlab.jetset.model.WebEntry;
 
 @Path("/web_entry")
 @Stateless
 public class WebEntryRest {
 	
 	@Inject
 	@Category("jetset.WebReceiver")
 	private Logger log;
 	
 	public final static String PATTERN_MOBILE_NUMBER = "^[0-9\\-\\s]+$";
 	public final static String PATTERN_EMAIL = "^[\\w\\-]([\\.\\w])+[\\w]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
 	public final static String PATTERN_RECEIPT7 = "^[1-3]\\-[0-9]{6}$";
 	public final static String PATTERN_RECEIPT6 = "^[1-3]\\-[0-9]{5}$";
 	
 	@Inject
 	private EntityManager em;
 	
 	@Inject
 	private StationController stationController;
 
 	@Inject
 	private EntryController entryController;
 
 	@Inject
 	private PrizeController prizeController;
 
 	@Path("/add")
 	@POST
 	@Produces(MediaType.APPLICATION_JSON)
 	public WebEntryResponse addEntry(@FormParam("name") String name, 
 			@FormParam("nric") String nric, 
 			@FormParam("number") String number,
 			@FormParam("email") String email,
 			@FormParam("payment") String payment,
 			@FormParam("cc") String cc,
 			@FormParam("station") Integer stationNo,
 			@FormParam("receipt") String receipt,
 			@FormParam("grade") String grade,
 			@FormParam("member") Boolean member,
 			@FormParam("subscribe") Boolean subscribe, 
 			@FormParam("agree") Boolean agree,
 			@Context HttpServletResponse resp) {	
 		
 		log.info(nric + ", " + number + ", " + 
 				email + ", " + payment + ", " + 
 				cc + ", " + stationNo + ", " + 
 				receipt + ", " + grade + ", " + 
 				subscribe + ", " + agree);
 
 		WebEntryResponse response = new WebEntryResponse();
 
 		boolean flag = true;
 		
 		if(name == null || name.trim().equals("")) {
 			response.addMessage("name", "Please enter your name");
 			flag = false;
 		}
 
 		if(nric == null || nric.trim().equals("")) {
 			response.addMessage("nric", "Please enter your NRIC/Passport No.");
 			flag = false;
 //		} else if(!nric.toUpperCase().matches(MOReceiver.PATTERN_NRIC)) {
 //			response.addMessage("nric", "Please enter correct NRIC/Passport No.");
 //			flag = false;
 		}
 
 		if(number == null || number.trim().equals("")) {
 			response.addMessage("contact", "Please enter your contact number");
 			flag = false;
 		} else if(!number.toUpperCase().matches(PATTERN_MOBILE_NUMBER)) {
 			response.addMessage("contact", "Please enter correct contact number");
 			flag = false;
 		}
 		
 		if(email != null && email.trim().length() > 0 && !email.toUpperCase().matches(PATTERN_EMAIL)) {
 			response.addMessage("email", "Please enter correct email address");
 			flag = false;
 		}
 		
 		if(payment == null || payment.trim().equals("")) {
 			response.addMessage("payment", "Please select a payment method");
 			flag = false;
 		}
 		
 		if(payment != null && payment.equals("Other Credit Card") && (cc == null || cc.trim().equals(""))) {
 			response.addMessage("cc", "Please select the credit card used");
 			flag = false;
 		}
 		
 		Station station = null;
 
 		if(stationNo == null) {
 			response.addMessage("station", "Please select correct station");
 			flag = false;
 		} else {
 			station = (Station) stationController.getStationById(stationNo);
 			
 			if(station == null) {
 				response.addMessage("station", "Please select correct station");
 				flag = false;
 			}
 		}
 		
		if((receipt == null || receipt.trim().equals("")) || 
				(receipt != null && !receipt.toUpperCase().matches(MOReceiver.PATTERN_RECEIPT)) ||
 				(station.getId() == 14 && !receipt.toUpperCase().matches(MOReceiver.PATTERN_RECEIPT14) && payment.equals("Shell Card")) ||
 				(station.getId() == 14 && !receipt.toUpperCase().matches(PATTERN_RECEIPT6) && !payment.equals("Shell Card")) ||
 				(station.getId() != 14 && receipt.toUpperCase().matches(MOReceiver.PATTERN_RECEIPT14))) {
			response.addMessage("receipt", "Please enter your receipt number");
 			flag = false;
 		}
 
 		if(grade == null || grade.trim().equals("")) {
 			response.addMessage("grade", "Please enter your fuel grade");
 			flag = false;
 		}
 
 		if(member == null) {
 			response.addMessage("member", "Please select if you are a member");
 			flag = false;
 		}
 
 //		if(subscribe == null || !subscribe) {
 //			response.addMessage("subscribe", "Please check to subscribe");
 //			flag = false;
 //		}
 
 		if(agree == null || !agree) {
 			response.addMessage("agree", "Please check to agree to Shell Terms and Conditions");
 			flag = false;
 		}
 		
 		if(flag) {
 			try {
 				WebEntry webEntry = new WebEntry();
 				webEntry.setName(name);
 				webEntry.setNric(nric.toUpperCase());
 				webEntry.setMsisdn(number);
 				webEntry.setEmail(email);
 				webEntry.setPaymentMode(payment);
 				webEntry.setCcType(cc);
 				webEntry.setReceipt(receipt.toUpperCase());
 				webEntry.setFuelGrade(grade);
 				webEntry.setSubscribe(subscribe);
 				webEntry.setAgree(agree);
 				webEntry.setStation(station);
 				webEntry.setMember(member);
 				
 				if(payment.equals("UOB Credit/Debit Card")) {
 					webEntry.setChance(2);
 				}
 				
 				MOReceiver.lock.tryLock(900, TimeUnit.SECONDS);
 				
 				boolean duplicate = entryController.isDuplicate(webEntry, true);
 				
 				if(duplicate) {
 					response.addMessage("error", "We have already received this entry. Please check your entry is correct. For assitance, call 1800-467-4355 Mon-Fri, 9am-5pm.");
 					response.setStatus(WebEntryResponse.STATUS_FAILED);
 				} else {
 
 					Prize prize = prizeController.getRandomPrize();
 					
 					if(prize == null) {
 						webEntry.setStatus("no prize");
 					} else {
 						webEntry.setPrize(prize);
 					}
 
 					em.persist(webEntry);
 
 					response.setStatus(WebEntryResponse.STATUS_OK);
 					response.addMessage("entryId", Integer.toString(webEntry.getId()));
 				}
 				
 			} catch (InterruptedException e) {
 				log.error("LOCK FAILED: " + e.getMessage());
 				response.setStatus(WebEntryResponse.STATUS_FAILED);
 				response.addMessage("lock", "failed");
 			} finally {
 				MOReceiver.lock.unlock();
 			}
 		} else {
 			response.setStatus(WebEntryResponse.STATUS_FAILED);
 		}
 		
 		return response;
 	}
 }
 
