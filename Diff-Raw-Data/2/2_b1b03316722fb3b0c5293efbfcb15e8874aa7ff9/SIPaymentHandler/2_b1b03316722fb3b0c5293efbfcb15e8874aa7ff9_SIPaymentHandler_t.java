 package org.alt60m.hr.si.servlet.dbio;
 
 import org.alt60m.hr.si.model.dbio.*;
 import org.alt60m.ministry.model.dbio.Staff;
 import org.alt60m.servlet.*;
 import org.alt60m.util.OnlinePayment;
 import org.alt60m.util.SendMessage;
 import org.alt60m.util.ObjectHashUtil;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import java.util.*;
 
 //This class handles all of the Stint Application related actions
 
 public class SIPaymentHandler {
 	private static Log log = LogFactory.getLog(SIPaymentHandler.class);
 	
 	private static final boolean debug = false;
 	private static final boolean PAYMENTTESTMODE = false;
 	private static final String MERCHANTACCTNUM = "stint*1";
	private static final String AUTHNETPASSWORD = "66VFfK9Z8Wn74ax9";
 	private static final String EMAILFROM = "help@campuscrusadeforchrist.com";
 	private static final String APPYEAR = SIUtil.CURRENT_SI_YEAR;
 
 	/*
 	  All actionhandler methods should return an ActionResults object after
 	  having set the view using setView("viewname")   
 	*/
 
 	/**
 	 * this method returns the next view depending on the type of payment the chose
 	 */
 	protected ActionResults postChoosePayment(Action action) {
 		ActionResults ar = new ActionResults();
 		log.debug("in " + action.getName() + " handler method!");
 
 		//determine our next view
 		String page = (String) action.getValues().get("page");
 		if ("payment2".equals(page)) {
 			String paymentmethod = (String) action.getValues().get("ChoosePayment");
 			if ("CCard".equals(paymentmethod))
 				page = "payccard";
 			else if ("Mail".equals(paymentmethod))
 				page = "paymail";
 			else if ("Staff".equals(paymentmethod))
 				page = "paystaff";
 		}
 		ar.setView(page);
 		return ar;
 	}
 
 	/**
 	 * processes a mail payment
 	 * 
 	 * @param action
 	 * @return ActionResults
 	 */
 	protected ActionResults postMailPayment(Action action) {
 		ActionResults ar = new ActionResults();
 		log.debug("in " + action.getName() + " handler method!");
 		try {
 			String page = (String) action.getValues().get("page");
 			if ("paymentsoon".equals(page)) {
 				log.debug("-->mail");
 				String appid = (String) action.getValues().get("ApplicationID");
 				paymentMail(appid);
 				ar.setView("payment");
 			} else {
 				ar.setView(page);
 			}
 		} catch (Exception e) {
 			ar.putValue("ErrorMessage", "Error processing Mail payment: " + e.getMessage());
 			ar.setView("paymail");
 			log.error(e.getMessage(), e);
 		}
 
 		return ar;
 	}
 
 	/**
 	 * processes credit card payment
 	 * 
 	 * @param action
 	 * @return ActionResults
 	 */
 	protected ActionResults postCCardPayment(Action action) {
 		ActionResults ar = new ActionResults();
 		log.debug("in " + action.getName() + " handler method!");
 
 		//determine our next view
 		String page = (String) action.getValues().get("page");
 
 		if (!page.equals("payment3")) {
 			ar.setView(page);
 			return ar; //early return
 		}
 
 		//** copied from MSInfo
 
 		Hashtable formData = action.getValues();
 		Hashtable ht = null;
 		String appid = (String) formData.get("ApplicationID");
 
 		try {
 			String ErrorMessage = "";
 
 			//check to make sure all required fields are valid
 			//TODO: also check expiration date in future/ccnum is valid
 
 			ErrorMessage = paymentCheckCreditCardValues(formData);
 
 			if (!"".equals(ErrorMessage)) //anything but blank
 				{
 				log.warn("Error processing credit card payment: " + ErrorMessage);
 				ar.setView("payccard");
 				ar.putValue("ErrorMessage", ErrorMessage);
 				return ar; //early return
 			}
 
 			//have values we need, post.
 			ht = paymentCreditCard(appid, formData);
 			String status = (String) ht.get("Status");
 			String response = (String) ht.get("ErrorMessage");
 
 			//is successful?
 			if ("Success".equals(status)) {
 				log.info("Credit Card Payment was successful");
 				markApplicationPaid(appid);
 				ar.setView("payment");
 			} else {
 				log.warn("Error processing credit card payment: " + status);
 				ar.setView("payccard");
 				ar.putValue("ErrorMessage", "Error processing CCard payment:" + status + "--" + response);
 			}
 
 		} catch (Exception e) {
 			ar.putValue("ErrorMessage", "Exception processing CCard payment: " + e.getMessage());
 			ar.setView("payccard");
 			log.error(e.getMessage(), e);
 		}
 
 		//** end copy        
 
 		return ar;
 	}
 
 	/**
 	 * chooses a staff that will (maybe) pay for this application via staff acct xfer
 	 * 
 	 * @param action
 	 * @return ActionResults
 	 */
 	protected ActionResults chooseStaffForPayment(Action action) {
 		Hashtable h = new Hashtable();
 		ActionResults ar = new ActionResults();
 		log.debug("in " + action.getName() + " handler method!");
 
 		String appid = (String) action.getValues().get("ApplicationID");
 		String persid = (String) action.getValues().get("SIPersonID");
 
 		String page = (String) action.getValues().get("page");
 
 		if (!page.equals("paymentsubmit")) {
 			ar.setView(page);
 			return ar; //early return
 		}
 
 		try {
 			log.debug("-->staff");
 
 			//record the payment via staff member intention
 			h = paymentStaff(appid, action.getValues());
 			String paymentId = h.get("PaymentID").toString(); // get paymentID from paymentStaff
 
 			//send an email to the staff person
 			action.putValue("PaymentID",paymentId); // persist paymentID to the email
 			action.putValue("ServerName", action.getServerName());
 			if (!sendStaffPaymentEmail(persid, appid, action.getValues())) {
 				ar.putValue("ErrorMessage", "There was a problem sending the email.  Please contact the system administrator.  There may have been a problem with that Staff Member's email address.");
 			}
 
 			ar.setView("payment");
 		} catch (Exception e) {
 			ar.putValue("ErrorMessage", "Error processing Mail payment: " + e.getMessage());
 			ar.setView("paystaff");
 			log.error(e.getMessage(), e);
 		}
 
 		return ar;
 	}
 
 	/**
 	 * when a staff person follows the link that is emailed them through the application process
 	 * it fires this action.  This action should return the applicant's person object.
 	 * 
 	 * @param action
 	 * @return ActionResults
 	 */
 	protected ActionResults paymentFromStaff(Action action) {
 		ActionResults ar = new ActionResults();
 		log.debug("in " + action.getName() + " handler method!");
 
 		//set the view to paymentfromstaff
 		ar.setView("paymentfromstaff");
 
 		//validate the personid coming in
 		String appid = (String) action.getValues().get("encodedAppID");
 		if (appid == null) {
 			log.warn("There was no ApplicationID associated with your request.  You followed an invalid link.");
 			ar.putValue("ErrorMessage", "There was no ApplicationID associated with your request.  You followed an invalid link, the application is removed, etc.  Please contact the System Administrator if you think this isn't the case.");
 			return ar;
 		}
 
 		//get the appid for this person.
 		String personid = (String) action.getValues().get("encodedPersID");
 		if (personid == null) {
 			log.warn("Could not find personid for this person!");
 			ar.putValue("ErrorMessage", "There was no PersonID associated with your request.  You followed an invalid link, the application is removed, etc.  Please contact the System Administrator if you think this isn't the case.");
 			return ar;
 		}
 		//get the payid for this staff person
 		String payid = (String) action.getValues().get("encodedPayID");
 		if(payid==null)
 		{
 			log.warn("Could not find payid for this staff person!");
 			StringBuffer s = new StringBuffer();
 			s.append("There was no PaymentID associated with your request.  ");
 			s.append("Please contact the applicant to process another payment request.  Thank You!");
 			ar.putValue("ErrorMessage",s.toString());
 			ar.setView("paymentfromstaffthanks");
 			return ar;
 		}
 
 		//setup hashtable of info we'll dump into the actionresults (yuk)
 		Hashtable info = new Hashtable();
 
 		//look up the payments and build the values needed for the jsp page. (doing it this way for fastest time done)
 		try {
 			SIPerson person = SIUtil.getSIPerson(personid);
 
 			Hashtable payment = SIUtil.getApplicationPaymentForStaff(appid, payid);
 
 			if (payment.size() == 0) {
 				log.warn("There were no StaffIntent payment types found.  This is a strange error.");
 				info.put("ErrorMessage", "The record saved by the Applicant that had the required information could not be found.  This is an internal error.  Please contact the System Administrator.");
 			}
 		    String applicationAmount = payment.get("Credit").toString();
 		    String staffAccountNo = payment.get("AccountNo").toString();
 		    String paymentId = payment.get("PaymentID").toString();
 			String type = payment.get("Type").toString();
 			String posted = payment.get("Posted").toString();
 
 			if (posted.equals("true")) {
 				if (type.equals("Staff Payment Refusal")) {
 					//build a msg back to the staff member
 					StringBuffer s = new StringBuffer();
 					s.append("We have noted your refusal to pay for this application fee.  ");
 					s.append("The system has generated an email to the applicant, but it might");
 					s.append(" be a good idea for you to let them know, too.  Thanks!");
 					ar.putValue("ErrorMessage",s.toString());
 					ar.setView("paymentfromstaffthanks");
 					return ar; // early return
 				}
 				else {
 					//build a msg back to the staff member
 					StringBuffer s = new StringBuffer();
 					s.append("We have noted your agreement to pay for this application fee.  Thank You!");
 					ar.putValue("ErrorMessage",s.toString());
 					ar.setView("paymentfromstaffthanks");
 					return ar; // early return
 				}
 			}
 			else {
 				if (type.equals("Staff Intent")) {
 					log.debug("Found a payment record... setting values for jsp..."); // continue
 				}
 				else {
 					//build a msg back to the staff member
 					StringBuffer s = new StringBuffer();
 					s.append("There was no current <B>Staff Intent</B> payment type found.  ");
 					s.append("Please contact the applicant to process another payment request.  Thanks!");
 					ar.putValue("ErrorMessage",s.toString());
 					ar.setView("paymentfromstaffthanks");
 					return ar; // early return
 				}
 			}
 			try {
 				applicationAmount = "" + Float.valueOf(applicationAmount).intValue();
 			} catch (NumberFormatException nfe) {
 				log.error("getPaymentFromStaffInfo: Couldn't convert applicationAmount float: " + applicationAmount, nfe);
 			}
 
 			//put in each item
 			info.put("paymentId", paymentId);
 			info.put("applicationAmount", applicationAmount);
 			info.put("staffAccountNo", staffAccountNo);
 			info.put("applicantName", person.getFirstName() + " " + person.getLastName());
 			info.put("applicantEmail", person.getCurrentEmail());
 			info.put("applicantPhone", person.getCurrentHomePhone());
 			info.put("PersonID", personid);
 			info.put("ApplicationID", appid);
 
 			log.debug("applicationAmmount:" + applicationAmount);
 			log.debug("staffAccountNo:" + staffAccountNo);
 
 		} catch (Exception e) {
 			log.error(e.getMessage(), e);
 		}
 
 		ar.addHashtable("info", info);
 
 		return ar;
 
 	}
 
 	/**
 	 * finds staff member information of those matching firstname and lastname
 	 * given when trying to choose someone who will pay for the application via
 	 * staff account transfer (wow, that was a crummy explanation)
 	 * 
 	 * @param action
 	 * @return ActionResults
 	 */
 	protected ActionResults postFindStaffForPayment(Action action) {
 		ActionResults ar = new ActionResults();
 		log.debug("in " + action.getName() + " handler method!");
 		String page = (String) action.getValues().get("page");
 
 		if (!page.equals("payment3")) {
 			ar.setView(page);
 			return ar; //early return
 		}
 
 		Hashtable h = getMatchingStaff((String) action.getValues().get("firstname"), (String) action.getValues().get("lastname"));
 
 		if (h.containsKey("ErrorMessage"))
 			ar.putValue("ErrorMessage", (String) h.get("ErrorMessage"));
 		else
 			ar.addHashtable("staffInfo", h);
 		ar.putValue("firstname", (String) action.getValues().get("firstname"));
 		ar.putValue("lastname", (String) action.getValues().get("lastname"));
 		ar.setView("paystaff");
 		return ar;
 	}
 
 	protected ActionResults postPaymentFromStaff(Action action) {
 		ActionResults ar = new ActionResults();
 		ar.setView("paymentfromstaffthanks");
 		log.debug("in " + action.getName() + " handler method!");
 
 		Hashtable formData = action.getValues();
 
 		String PaymentType = (String) formData.get("ChoosePayment");
 		String AccountNumber = "";
 		String personid = (String) formData.get("PersonID");
 		String appid = (String) formData.get("ApplicationID");
 		String PaymentID = (String) formData.get("PaymentID");
 		String StaffAccountNumber = (String) formData.get("staffAccountNo");
 		String amount = (String) formData.get("Amount");
 
 		try
         {
 			SIPayment paymentCheck = new SIPayment();
 			paymentCheck.setPaymentID(PaymentID);
 			paymentCheck.select();
 			String Posted = String.valueOf(paymentCheck.getPosted());
 			if (Posted.equals("true")) {
 				//build a msg back to the staff member
 				StringBuffer s = new StringBuffer();
 				s.append("This form has already been processed.  Thank You!");
 				ar.putValue("ErrorMessage",s.toString());
 				ar.setView("paymentfromstaffthanks");
 				return ar; // early return
 			}
 		}
 		catch(Exception e)
 		{
 			log.error(e.getMessage(), e);
 			ar.putValue("ErrorMessage",e.getMessage());
 			return ar;
 		}
 
 		if (PaymentType == null) {
 			log.warn("Payment Type was null.  Internal Error of some type");
 			ar.putValue("ErrorMessage", "Payment Type was null.  Internal Error of some type.");
 			return ar;
 		} else if ("No".equals(PaymentType)) {
 			log.info("Staff chose not to pay application fee.");
 
 			try {
 				//note it in our db
 				log.debug("Creating Staff Payment Refusal.");
 				SIPayment payment = new SIPayment();
 				payment.setPaymentID(PaymentID);
 				payment.select();
 
 				payment.setType("Staff Payment Refusal");
 				payment.setPosted(true);
 				payment.setPaymentDate(new Date());
 				payment.setPostedDate(new Date());
 				payment.setPaymentFor("STINT System");
 				payment.setAccountNo(AccountNumber);
 				payment.setAuthCode(StaffAccountNumber);
 				payment.setComment("Payment refused by " + StaffAccountNumber + ".");
 				payment.setCredit(0f);
 
 				payment.persist();
 
 				//build a msg back to the staff member
 				StringBuffer s = new StringBuffer();
 				s.append("We have noted your refusal to pay for this application fee.  ");
 				s.append("The system has generated an email to the applicant, but it might");
 				s.append(" be a good idea for you to let them know, too.  Thanks!");
 				ar.putValue("ErrorMessage", s.toString());
 
 			} catch (Exception e) {
 				log.error(e.getMessage(), e);
 				ar.putValue("ErrorMessage", e.getMessage());
 				return ar;
 			}
 
 			if (!emailPaymentStaffRefusal(personid)) {
 				ar.putValue("ErrorMessage", "Failure sending email message to applicant.  Contact System Administrator.");
 				return ar; //early return
 			}
 			return ar; //early return
 		}
 		//following options do not return, but flow onward.
 		else if ("MyAccount".equals(PaymentType)) {
 			log.info("Staff chose to pay via their account.");
 			AccountNumber = (String) formData.get("staffAccountNo");
 		} else if ("AnotherAccount".equals(PaymentType)) {
 			log.info("Staff chose to pay via another account.");
 			AccountNumber = (String) formData.get("OtherAccount");
 		} else {
 			log.warn("Payment Type was not recognized.  Internal Error of some type .");
 			ar.putValue("ErrorMessage", "Payment Type was not recognized.  Internal Error of some type.");
 			return ar;
 		}
 
 		//create staff payment
 		try {
 			log.debug("Creating Staff Payment.");
 			SIPayment payment = new SIPayment();
 			payment.setPaymentID(PaymentID);
 			payment.select();
 
 			payment.setType("Staff Payment");
 			payment.setPosted(true);
 			payment.setPaymentDate(new Date());
 			payment.setPostedDate(new Date());
 			payment.setPaymentFor("STINT System");
 			payment.setAccountNo(AccountNumber);
 			payment.setAuthCode(StaffAccountNumber);
 			payment.setComment("Payment authorized by " + StaffAccountNumber + ".");
 			try {
 				payment.setCredit(Float.valueOf(amount).floatValue());
 			} catch (NumberFormatException nfe) {
 				payment.setCredit(35.00f);
 			}
 
 			payment.persist();
 
 			//mark person as paid.
 			markApplicationPaid(appid);
 
 			//tell them about it
 			if (!emailPaymentStaffAcceptance(personid)) {
 				ar.putValue("ErrorMessage", "Failure sending email message to the applicant advising them of your payment, however your payment has been processed.");
 				return ar;
 			}
 //			//tell tool owner about it so he can actually make the transfer
 			if (!emailToolOwnerPaymentStaffAcceptance(payment)) {
 				ar.putValue("ErrorMessage", "Failure sending email message to the applicant advising them of your payment, however your payment has been processed.");
 				return ar;
 			}
 
 			ar.putValue("ErrorMessage", "We have noted your payment of this applicant's application fee and emailed the applicant.  Thank you!");
 
 		} catch (Exception e) {
 			log.error(e.getMessage(), e);
 			ar.putValue("ErrorMessage", e.getMessage());
 			return ar;
 		}
 
 		return ar;
 	}
 
 	/**
 	 * finds all the applicants with applications that are as yet unpaid
 	 * with matching firstname/lastname
 	 * 
 	 * @param action
 	 * @return ActionResults
 	 */
 	protected ActionResults postCoordinatorPaymentFind(Action action) {
 		ActionResults ar = new ActionResults();
 		ar.setView("receivepayment");
 		log.debug("in " + action.getName() + " handler method!");
 
 		Hashtable people = getMatchingUnPaidPeople((String) action.getValue("firstname"), (String) action.getValue("lastname"));
 		if (people.containsKey("ErrorMessage")) {
 			ar.putValue("ErrorMessage", (String) people.get("ErrorMessage"));
 		}
 
 		ar.addHashtable("people", people);
 
 		return ar;
 	}
 
 	protected ActionResults postReceiveCoordinatorPayments(Action action) {
 		ActionResults ar = new ActionResults();
 		ar.setView("receivepayment");
 		log.debug("in " + action.getName() + " handler method!");
 
 		String appid = (String) action.getValues().get("ApplicationID"); //person who got the payment
 		log.debug("posting Received payment for appid:" + appid);
 		if (appid != null) {
 			try {
 				SIPayment payment = new SIPayment();
 
 				String type = "ReceivePayment";
 
 				payment.setType(type);
 				payment.setPosted(true);
 				payment.setPaymentDate(new Date());
 				payment.setPostedDate(new Date());
 				payment.setPaymentFor("STINT System");
 				payment.setFk_ApplicationID(appid);
 
 				payment.persist();
 
 				this.markApplicationPaid(appid);
 
 				log.debug("app marked paid.");
 
 				ar.setView("receivepayment");
 			} catch (Exception e) {
 				log.error(e.getMessage(), e);
 				ar.putValue("ErrorMessage", "There was a problem with your request: " + e.getMessage());
 				ar.setView("receivepayment");
 			}
 		}
 		return ar;
 
 	}
 
 	//********************************************************************************//    
 	//****  payment utility methods  ****//
 	//********************************************************************************//    
 
 	/**
 	 * checks for the required fields to be present in ccard post 
 	 * 
 	 *  ccNum,ccExpM,ccExpY,ccAmt,FirstName,LastName,
 	 *  Address,City,State,Zip,Country,Phone,Email
 	 * 
 	 */
 	public String paymentCheckCreditCardValues(Hashtable formData) {
 		StringBuffer m = new StringBuffer(); //m=missing
 
 		if (isEmpty(formData.get("FirstName")))
 			m.append("<LI>FirstName ");
 		if (isEmpty(formData.get("LastName")))
 			m.append("<LI>LastName ");
 		if (isEmpty(formData.get("Address1")))
 			m.append("<LI>Address1 ");
 		if (isEmpty(formData.get("City")))
 			m.append("<LI>City ");
 		if (isEmpty(formData.get("State")))
 			m.append("<LI>State ");
 		if (isEmpty(formData.get("Zip")))
 			m.append("<LI>Zip ");
 		if (isEmpty(formData.get("CCardType")))
 			m.append("<LI>Card Type ");
 		if (isEmpty(formData.get("CCNum")))
 			m.append("<LI>Card Number ");
 		if (isEmpty(formData.get("CCExpM")))
 			m.append("<LI>Expiration Month ");
 		if (isEmpty(formData.get("CCExpY")))
 			m.append("<LI>Expiration Year ");
 
 		if (m.length() > 0)
 			//                m=new StringBuffer("The following required values were missing:<BR><UL>" + m.toString() + "</UL>");
 			m = new StringBuffer("Please enter all required fields highlighted in yellow below.");
 		return m.toString();
 	}
 
 	//if is null or empty string, isempty=true
 	//kb 10/28/02
 	private boolean isEmpty(Object obj) {
 		return (obj == null || "".equals(obj.toString()));
 	}
 
 	//The following method shamelessly copied from CRSInfo - kb 10/14 (and then MSInfo 12/3/02)
 	//  payment Info Code
 	//  Mostly by David Bowdoin, starting 7/15/2002
 
 	//Created:  7/17/02, DMB
 	/**
 	 *  Hashtable paymentInfo expects (ccNum,ccExpM,ccExpY,ccAmt,FirstName,LastName,Address,City,State,Zip,Country,Phone,Email )
 	 *  registrationID = applicationid
 	 */
 	public Hashtable paymentCreditCard(String registrationID, Hashtable ccPaymentInfo) throws Exception {
 		log.info("Processing Credit Card for registration " + registrationID);
 		ccPaymentInfo.put("InvoiceNum", registrationID);
 		ccPaymentInfo.put("Description", "STINT System Payment");
 		ccPaymentInfo.put("CustID", registrationID);
 
 		OnlinePayment onlinePay = new OnlinePayment();
 
 		// if PAYMENTTESTMODE==true then uses Visa Test Card # 4007000000027
 		onlinePay.setTestMode(PAYMENTTESTMODE);
 
 		onlinePay.setMerchantInfo(MERCHANTACCTNUM, AUTHNETPASSWORD);
 
 		Hashtable results = onlinePay.processCreditCard(ccPaymentInfo);
 
 		log.debug("--> authorize.net status  : " + results.get("Status"));
 		log.debug("--> authorize.net response: " + results.get("Response"));
 
 		Hashtable paymentHash = new Hashtable();
 
 		//check for testmode
 		if (PAYMENTTESTMODE) {
 			log.debug("TESTMODE=TRUE: Setting results to true no matter what they really were.");
 			results.put("Status", "Success");
 		}
 
 		if (results.get("Status").equals("Success")) {
 			SIPayment payment = new SIPayment();
 
 			payment.setCredit(Float.parseFloat("" + SIApplication.calcApplicationFee()));
 			payment.setAuthCode((String) results.get("AuthCode"));
 			payment.setType("Credit card payment");
 			payment.setPosted(true);
 			payment.setPaymentDate(new Date());
 			payment.setPostedDate(new Date());
 			payment.setPaymentFor("STINT System");
 			payment.setFk_ApplicationID(registrationID);
 			log.debug("----> Fk_ApplicationID = " + payment.getFk_ApplicationID());
 			payment.persist();
 
 			paymentHash = ObjectHashUtil.obj2hash(payment);
 
 			//DEBUG output only.
 			for (Iterator ii = paymentHash.keySet().iterator(); ii.hasNext();) {
 				String key = (String) ii.next();
 				Object val = (Object) paymentHash.get(key);
 				log.debug("payment hash after post to authorize.net: " + key + " -- " + val.toString());
 			}
 
 			//TODO:
 			//org.alt60m.hr.ms.util.EmailConfirm email = new org.alt60m.hr.ms.util.EmailConfirm();
 			//email.createCreditCardReceipt(payment, (String)ccPaymentInfo.get("CCNum"), (String)ccPaymentInfo.get("CCExpM"), (String)ccPaymentInfo.get("CCExpY"), ccPaymentInfo.get("PaymentAmt"));
 			//email.send();
 		} else {
 			//Results: {Status=Could not connect to payment system. Please try again later.}
 			paymentHash.put("ErrorMessage", results.get("Response"));
 		}
 
 		paymentHash.put("Status", results.get("Status"));
 
 		return paymentHash;
 	}
 
 	/**
 	* marks an application as paid.  kb 10/16/02
 	*/
 	private void markApplicationPaid(String appid) throws Exception {
 		log.debug("Marking Application (id=" + appid + ") paid.");
 
 		SIApplication a = new SIApplication(appid);
 		a.setIsPaid(true);
 		a.persist();
 	}
 
 	/**
 	 * pay by mail intentions.  record the intention
 	 * 
 	 * @param WsnApplicationid
 	 * @return String
 	 * @throws Exception
 	 */
 	private void paymentMail(String appid) throws Exception {
 
 		log.debug("Creating Mail Payment.");
 		SIPayment payment = new SIPayment();
 
 		payment.setType("Mail Intent");
 		payment.setPosted(false);
 		payment.setPaymentDate(new Date());
 		payment.setPostedDate(new Date());
 		payment.setPaymentFor("STINT System");
 		payment.setFk_ApplicationID(appid);
 
 		payment.persist();
 		log.debug("done saving mail payment.");
 
 	}
 
 	/**
 	* payment via a staff member (request account transfer from them)
 	*/
 	private Hashtable paymentStaff(String appid, Hashtable formData) throws Exception {
 		String amount = (String) formData.get("Amount");
 		String accountno = (String) formData.get("AccountNo");
 
 		Hashtable h = new Hashtable();
 		log.debug("Creating Staff Payment.");
 		SIPayment payment = new SIPayment();
 
 		payment.setType("Staff Intent");
 		payment.setPosted(false);
 		payment.setPaymentDate(new Date());
 		payment.setPostedDate(new Date());
 		payment.setPaymentFor("STINT System");
 		payment.setFk_ApplicationID(appid);
 		try {
 			payment.setCredit(Float.valueOf(amount).floatValue());
 		} catch (NumberFormatException nfe) {
 			payment.setCredit(35.00f); //NOTE! shouldn't ever need this, but...
 		}
 		payment.setAccountNo(accountno);
 
 		payment.persist();
 		String paymentId = payment.getPaymentID();
 		h.put("PaymentID",paymentId); // add paymentID to the hashtable
 
 		return h;
 	}
 
 	/**
 	*  Send email to staff member who the applicant designated.
 	*/
 	public boolean sendStaffPaymentEmail(String personid, String appid, Hashtable formData) {
 		try {
 			SIPerson person = (SIPerson) SIUtil.getObject(personid, "SIPersonID", SIAppHandler.PERSONCLASS);
 
 			String refFullName = formData.get("FirstName") + " " + formData.get("LastName");
 			String referenceLink = "http://" + formData.get("ServerName") + "/servlet/SIController?action=paymentFromStaff&encodedAppID=" + appid + "&encodedPersID=" + personid + "&encodedPayID=" + formData.get("PaymentID");
 			String applicantFullName = person.getFirstName() + " " + person.getLastName();
 			String applicantEmailAddress = person.getCurrentEmail();
 			String applicantHomePhone = person.getCurrentHomePhone();
 			String staffEmail = (String) formData.get("Email");
 
 			StringBuffer text = new StringBuffer("Dear ");
 			text.append(refFullName);
 			text.append(":\n\n"+"<BR><BR>");
 			text.append(applicantFullName);
 			text.append(" has just applied for an exciting STINT opportunity with Campus Crusade for Christ and has indicated you have agreed to pay the application fee.  If you could take a minute and indicate the account to use, we can continue with the application process.  The application materials will then be reviewed and a decision will be given as soon as possible.\n\n"+"<BR><BR>");
 			text.append(" The payment form is on our secure web site.  Just click on the link below to go directly to the payment form for ");
 			text.append(applicantFullName);
 			text.append(".  The applicant's application will not processed until payment is made.  Thank you for your help in sending this laborer into the harvest!\n\n"+"<BR><BR>");
 			text.append("You may contact the applicant by email at ");
 			text.append(applicantEmailAddress);
 			text.append(" or by phone at ");
 			text.append(applicantHomePhone);
 			text.append(".\n\n"+"<BR><BR>");
 			text.append("From the link below, you can either indicate the account number to use for payment or indicate your unwillingness to pay for this application.  Either way, please follow the link and make a determination.\n"+"<BR>");
 			text.append("Thank you very much.\n\n"+"<BR><BR>");
 			text.append("Sincerely,\n"+"<BR>");
 			text.append("Campus Crusade for Christ\n\n\n"+"<BR><BR>");
 			text.append("<a href=" + referenceLink + ">Click here to access reference form for " + applicantFullName + "</a>.\n\n"+"<BR><BR>");
 			text.append("If the above link does not work.  Please use the following link.  The link should be all in one line, so if it is split up into more than one line, you may need to copy and paste the link into a web browser.\n\n"+"<BR><BR>");
 			text.append("<a href=" + referenceLink + ">" + referenceLink + "</a>\n\n"+"<BR><BR>");
 
 			SendMessage msg = new SendMessage();
 			msg.setTo(staffEmail);
 			//msg.setCc(applicantEmailAddress); don't want to copy the student on this email so they can't initiate payment creatively on their own.
 			msg.setFrom(EMAILFROM);
 			msg.setSubject("Application Payment request for " + applicantFullName);
 			msg.setBody(text.toString(), "text/html");
 
 			msg.send();
 
 			return true;
 		} catch (Exception e) {
 			log.error("sendStaffPaymentEmail(): send email failed.", e);
 			return false;
 		}
 	}
 
 	/**
 	 * returns list of staff who match the criteria. 
 	 * @param formData
 	 * @return Hashtable
 	 */
 	private Hashtable getMatchingStaff(String firstname, String lastname) {
 		Hashtable retvals = new Hashtable();
 
 		try {
 			if (firstname == null || lastname == null || "".equals(firstname) || "".equals(lastname)) {
 				retvals.put("ErrorMessage", "You must enter both a first and last name.");
 				return retvals;
 			}
 
 			// if either name has an apostrophe in it, change to '' instead.
 			firstname = doubleApostrophe(firstname, 0);
 			lastname = doubleApostrophe(lastname, 0);
 
 			String whereClause = "isSecure <> 'T' AND firstname like '" + firstname + "%' AND lastname='" + lastname + "'";
 			Collection c = ObjectHashUtil.list((new Staff()).selectList(whereClause));
 			log.debug("Number of Staff Matches found: " + c.size());
 
 			for (Iterator i = c.iterator(); i.hasNext();) {
 				Hashtable h = (Hashtable) i.next();
 				String toput = "AccountNo=" + (String) h.get("AccountNo") + "&" + "Email=" + (String) h.get("Email") + "&" + "FirstName=" + (String) h.get("FirstName") + "&" + "LastName=" + (String) h.get("LastName");
 
 				String name = (String) h.get("FirstName") + " " + (String) h.get("LastName");
 
 				retvals.put(name, toput);
 
 				//log.debug("Found a match: " + toput);
 
 			}
 
 		} catch (Exception e) {
 			retvals.put("ErrorMessage", "There was an error with your request: " + e.getMessage());
 			return retvals;
 		}
 
 		return retvals;
 
 	}
 
 	/**
 	 * emails the applicant about the staff having paid their app fee.
 	 * @param personid
 	 * @return boolean
 	 */
 	private boolean emailPaymentStaffAcceptance(String personid) {
 		try {
 
 			SIPerson person = (SIPerson) SIUtil.getSIPerson(personid);
 
 			String applicantFullName = person.getFirstName() + " " + person.getLastName();
 			String applicantEmailAddress = person.getCurrentEmail();
 
 			StringBuffer text = new StringBuffer("Dear ");
 			text.append(applicantFullName);
 			text.append(":\n\n");
 			text.append("A staff member has just paid for your application via account transfer.");
 			text.append("Thank you very much.\n\n");
 			text.append("Sincerely,\n");
 			text.append("Campus Crusade for Christ\n\n\n");
 
 			log.debug("TEXT=" + text.toString() + "=");
 			log.debug("person.getCurrentEmail()=" + person.getCurrentEmail());
 			log.debug("fromEmailAddress=" + this.EMAILFROM);
 
 			// check for applicant email
 			if (applicantEmailAddress == null  ||  applicantEmailAddress.trim().equals("")){
 				log.warn(	"applicantEmailAddress does not exist, disregard SendMessage to applicant");
 				return true;
 			}
 			else {
 				SendMessage msg = new SendMessage(); 
 				msg.setTo(applicantEmailAddress);
 				msg.setFrom(EMAILFROM);
 				msg.setSubject("Application Payment Notification");
 				msg.setBody(text.toString());
 				msg.send();
 			return true;
 			}
 		} catch (Exception e) {
 			log.error("emailpaymentstaffacceptance(): send email failed.", e);
 			return false;
 		}
 	}
 	/**
 	 * emails matt griffith about the staff having paid.
 	 * I know that hard coding matt griffith's name is bad, but there it is
 	 * @param personid
 	 * @return boolean
 	 */
 	private boolean emailToolOwnerPaymentStaffAcceptance(SIPayment payment) {
 		try {
 			StringBuffer text = new StringBuffer("To whom it may concern: \n\n");
 			text.append("Take: ");
 			text.append(payment.getCredit()+"\n");
 			text.append("From: ");
 			text.append(payment.getAccountNo()+"\n");
 			text.append("Authorized by: ");
 			text.append(payment.getAuthCode()+"\n");
 			text.append("Payment record ID: ");
 			text.append(payment.getPaymentID()+"\n\n");
 			text.append("Sincerely,\n");
 			text.append("Campus Crusade for Christ\n\n\n");
 
 			SendMessage msg = new SendMessage(); 
 			msg.setTo("matt.griffith@uscm.org"); // HARD CODED!!!
 			msg.setFrom(EMAILFROM);
 			msg.setSubject("Application Payment Notification");
 			msg.setBody(text.toString());
 			msg.send();
 			return true;
 		} catch (Exception e) {
 			log.error("emailToolOwnerPaymentStaffAcceptance(): send email failed.", e);
 			return false;
 		}
 	}
 	/**
 	 * emails the applicant about the staff having paid their app fee.
 	 * @param WsnApplicationid
 	 * @return boolean
 	 */
 	private boolean emailPaymentStaffRefusal(String personid) {
 		try {
 
 			SIPerson person = (SIPerson) SIUtil.getSIPerson(personid);
 
 			String applicantFullName = person.getFirstName() + " " + person.getLastName();
 			String applicantEmailAddress = person.getCurrentEmail();
 			String emailContextA = "";
 			String emailContextB = "";
 			String emailContextC = "";
 			String emailContextD = ":\n\n"+"<BR><BR>";
 			String emailContextE = "";
 			if (applicantEmailAddress == null  ||  applicantEmailAddress.trim().equals("")){
 				emailContextA = "Call Center,"+"<BR><BR>";
 				emailContextB = "This email was intended for ";
 				emailContextC = " (personID " + personid + "), but they have no current email.  Please contact them to make other payment arrangements.";
 				emailContextD = "\n\n"+"<BR><BR>";
 				emailContextE = "---------------------------Original Message---------------------------"+"<BR><BR>";
 				applicantEmailAddress = EMAILFROM;
 			}
 
 
 			StringBuffer text = new StringBuffer("Dear ");
 			text.append(emailContextA + emailContextB + applicantFullName + emailContextC);
 			text.append(emailContextD);
 			text.append(emailContextE);
 			text.append("The staff member you requested payment from for your Application has just ");
 			text.append("notified us of their refusal to pay.  Please contact them or make other payment ");
 			text.append("arrangements.  You can log onto the system and pay via credit card or check/money order.\n\n"+"<BR><BR>");
 			text.append("Sorry for the inconvenience...  Please remember your application will not be processed until the fee is paid.\n\n"+"<BR><BR>");
 			text.append("Sincerely,\n"+"<BR>");
 			text.append("Campus Crusade for Christ\n\n\n"+"<BR><BR>");
 			
 			log.debug("TEXT=" + text.toString() + "=");
 			log.debug("person.getCurrentEmail()=" + person.getCurrentEmail());
 			log.debug("fromEmailAddress=" + this.EMAILFROM);
 
 			SendMessage msg = new SendMessage();//"smtp.comcast.net"
 			msg.setTo(applicantEmailAddress);
 			msg.setFrom(EMAILFROM);
 			msg.setSubject("Application Payment Notification");
 			msg.setBody(text.toString(), "text/html");
 
 			msg.send();
 
 			return true;
 		} catch (Exception e) {
 			log.error("emailpaymentstaffacceptance(): send email failed.", e);
 			return false;
 		}
 
 	}
 
 	/**
 	 * retrieves the info for people with unpaid applications.  utility method
 	 * 
 	 * @param firstname
 	 * @param lastname
 	 * @return Hashtable
 	 */
 	private Hashtable getMatchingUnPaidPeople(String firstname, String lastname) {
 
 		log.debug("Looking for payments...");
 		Hashtable retvals = new Hashtable();
 
 		try {
 
 			log.debug(" search--> " + firstname + " " + lastname);
 
 			if (firstname == null || lastname == null || "".equals(firstname) || "".equals(lastname)) {
 				log.debug("You must enter both a first and a last name");
 				retvals.put("ErrorMessage", "You must enter both a first and last name.");
 				return retvals;
 			}
 
 			SIApplication a = new SIApplication();
 			String query = "SELECT b.* FROM ministry_person a, hr_si_applications b where a.firstname like '" + firstname + "%' AND a.lastname like '" + lastname + "' AND b.isPaid='F' AND a.fk_ssmUserId = b.fk_ssmUserid";
 			Collection c = ObjectHashUtil.list(a.selectSQLList(query));
 			for (Iterator i = c.iterator(); i.hasNext();) {
 				Hashtable h = (Hashtable) i.next();
 				SIPerson p = new SIPerson();
 				p.setFk_ssmUserID(Integer.parseInt((String)h.get("Fk_ssmUserID")));
 				p.select();
 				Hashtable ph = ObjectHashUtil.obj2hash(p);
 				String toput = "ApplicationID=" + (String) h.get("ApplicationID") + "&" + "CurrentEmail=" + (String) ph.get("CurrentEmail") + "&" + "FirstName=" + (String) ph.get("FirstName") + "&" + "LastName=" + (String) ph.get("LastName");
 
 				String name = (String) h.get("FirstName") + " " + (String) h.get("LastName");
 
 				h.put("link", toput); //add link to the this person's hashtable
 
 				retvals.put(name, h);
 				log.debug(name + " added with hash of " + h.size());
 
 				log.debug("Found a match: " + toput);
 
 			}
 
 		} catch (Exception e) {
 			log.error(e.getMessage(), e);
 			retvals.put("ErrorMessage", "You must enter both a first and last name.");
 			return retvals;
 		}
 
 		return retvals;
 	}
 
 	/**
 	 * changes any occurrence of ' to ''  call with: (string, 0)
 	 * @param target
 	 * @param fromIndex
 	 * @return String
 	 */
 	public static String doubleApostrophe(String target, int fromIndex) {
 		// (recursive)
 		StringBuffer sbuf = new StringBuffer("");
 		//see if there is an apostrophe in the target
 		int apindex = target.indexOf("'", fromIndex);
 		if (apindex > -1) {
 			//so we have an apostrophe in target.
 			//add the chars up to and including the apostrophe to sbuf
 			sbuf.append(target.substring(fromIndex, apindex));
 			//add the double apostrophe
 			sbuf.append("''");
 			//now recursively add the rest
 			sbuf.append(doubleApostrophe(target, apindex + 1));
 
 		} else {
 			//just do a return here for efficiency sake
 			return target.substring(fromIndex, target.length());
 		}
 
 		return sbuf.toString();
 	}
 
 }
