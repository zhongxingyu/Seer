 package controllers;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 
 import jobs.ProcessInboundMessage;
 import jobs.SyncListAndExecFormula;
 import jobs.SyncListAndRunFirstCampaign;
 import models.FormulaCampaign;
 import models.GoogleCampaign;
 import models.PhoneQuery;
 import models.SMSCampaign;
 
 import org.apache.commons.io.FileUtils;
 
 import play.Logger;
 import play.Play;
 import play.mvc.Controller;
 import play.mvc.Router;
 
 import com.google.i18n.phonenumbers.NumberParseException;
 import com.google.i18n.phonenumbers.PhoneNumberUtil;
 import com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberFormat;
 import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;
 import com.google.i18n.phonenumbers.geocoding.PhoneNumberOfflineGeocoder;
 import com.marketo.mktows.wsdl.ResultSyncLead;
 import com.twilio.sdk.resource.list.AccountList;
 import common.Constants;
 import common.MarketoUtility;
 import common.RegionUtil;
 import common.TwilioUtility;
 
 public class Application extends Controller {
 
 	public static void index(String url) {
 
 		if (url == null) {
 			render();
 		} else if (url.endsWith("sms_settings.html")) {
 			Logger.info("Looking up campaignURL %s", url);
 			// Check to see if this has been configured previously
 			List<SMSCampaign> msExisting = SMSCampaign.find(
 					"campaignURL = ? and status = ?", url,
 					Constants.CAMPAIGN_STATUS_ACTIVE).fetch();
 			if (msExisting.size() == 1) {
 				SMSCampaign ms = msExisting.get(0);
 				Logger.info("campaign[%d] was configured previously", ms.id);
 				/*
 				 * renderText(
 				 * "This campaign has been configured previously for account :"
 				 * + ms.munchkinAccountId);
 				 */
 				savedConfig(Constants.CAMPAIGN_SMS, ms.id);
 			}
 
 			MarketoUtility mu = new MarketoUtility();
 			SMSCampaign sc = (SMSCampaign) mu.readSettings(url,
 					Constants.CAMPAIGN_SMS);
 			if (sc == null) {
 				Logger.info("Unable to read settings from %s", url);
 				renderText("Unable to read settings from %s", url);
 			} else if (sc.munchkinAccountId.equals("null")) {
 				Logger.info("campaign[%d] does not have a valid munchkin id",
 						sc.id);
 				renderText("Please provide the correct munchkin account id");
 			} else if (sc.smsGatewayID.equals("null")
 					|| sc.smsGatewayPassword.equals("null")) {
 				Logger.info(
 						"campaign[%d] does not have the sms Gateway id or password",
 						sc.id);
 				renderText("Please configure a valid twilio Account ID and Secret in your program my tokens");
 			} else if (sc.soapUserId.equals("null")
 					|| sc.soapEncKey.equals("null")) {
 				Logger.info(
 						"campaign[%d] does not have the Marketo soap credentials",
 						sc.id);
 				renderText("Please provide your Marketo soap credentials");
 			} else if (sc.rules.size() == 0) {
 				Logger.info("campaign[%d] does not have any campaign rules",
 						sc.id);
 				renderText("Please provide rules for the SMS campaign");
 			} else if (sc.leadListWithPhoneNumbers.equals("null")) {
 				Logger.info("campaign[%d] does not lead list set", sc.id);
 				renderText("Please provide a static list from with leads whose phone numbers are known");
 			} else if (sc.phoneNumFieldApiName.equals("null")) {
 				Logger.info(
 						"campaign[%d] does not have a phoneNumFieldApiName.  Using Phone instead",
 						sc.id);
 				sc.phoneNumFieldApiName = Constants.DEFAULT_PHONE_FIELD_API_NAME;
 			}
 
 			ResultSyncLead dummyLead = mu.createNewLead(sc, "+1smstesting");
 			if (dummyLead == null) {
 				/* Insufficient - must make a call */
 				Logger.info("Cannot connect with soap credentials [%s:%s]",
 						sc.soapUserId, sc.soapEncKey);
 				renderText("The SOAP credentials you provided are invalid");
 			} else {
 				mu.deleteLead(sc, dummyLead);
 			}
 
 			AccountList accounts = TwilioUtility.getAccounts(sc.smsGatewayID,
 					sc.smsGatewayPassword);
 			if (accounts == null || accounts.getTotal() == 0) {
 				Logger.info("No accounts set up with SMS gateway [%s:%s]",
 						sc.smsGatewayID, sc.smsGatewayPassword);
 				renderText("Please setup the SMS gateway account and retry");
 			}
 
 			sc.status = Constants.CAMPAIGN_STATUS_ACTIVE;
 			sc.save();
 			Logger.debug("campaign[%d] has been saved", sc.id);
 
 			Map<String, Object> map = new HashMap();
 			// Generate URL to listen for inbound SMS
 			map.put("campaignId", sc.id);
 			String urlBase = Play.configuration.getProperty("mkto.serviceUrl");
 			String callBackurl = urlBase
 					+ Router.reverse("Application.smsCallback", map).url;
 			Logger.debug("campaign[%d] - callback URL is %s", sc.id,
 					callBackurl);
 
 			// create Twilio application and save appId in database
 			try {
 				// Logger.info(
 				// "campaign[%d] - creating new SMS gateway application",
 				// sc.id, callBackurl);
 				// String appId =
 				// TwilioUtility.createApplication(sc.smsGatewayID,
 				// sc.smsGatewayPassword, "SMSSubscriber", callBackurl,
 				// "GET");
 				// sc.smsGatewayApplicationId = appId;
 				// Logger.info(
 				// "campaign[%d] - New SMS gateway application [%s] created successfully",
 				// sc.id, appId);
 				boolean setApp = TwilioUtility.setCallbackUrl(sc.smsGatewayID,
 						sc.smsGatewayPassword, callBackurl,
 						sc.smsGatewayPhoneNumber);
 				if (setApp) {
 					Logger.info("campaign[%d] - SMS application will now respond to inbound msgs");
 				} else {
 					Logger.info("campaign[%d] - SMS application will NOT respond to inbound msgs");
 				}
 				sc.save();
 			} catch (Exception e) {
 				// Log this properly
 				Logger.fatal(
 						"campaign[%d] - Could not create a new SMS gateway application [%s]",
 						sc.id, e.getMessage());
 				renderHtml("Exception while creating Twilio Application for SMS callback");
 			}
 
 			// kick off background thread to do read the list and run outgoing
 			// campaign
 			Logger.info(
 					"campaign[%d] - Kicking off background task to fetch lead list",
 					sc.id);
 			new SyncListAndRunFirstCampaign(sc).in(2);
 
 			savedConfig(Constants.CAMPAIGN_SMS, sc.id);
 		} else if (url.endsWith("google_settings.html")) {
 			processGoogleCampaign(url);
 		} else if (url.endsWith("formula_settings.html")) {
 			processFormula(url);
 
 		} else {
 			renderHtml("Campaign url must be sms_settings.html"
 					+ " or google_settings.html" + " or formula_settings.html");
 		}
 	}
 
 	private static void processGoogleCampaign(String url) {
 		GoogleCampaign gc = getGoogleCampaignFromUrl(url);
 
 		/*
 		 * For Testing gc.munchkinAccountId = "1234"; gc.save();
 		 * 
 		 * addGCLID("1234", "idnum", "add", "hsd84jk", "marketo target", "null",
 		 * "2013-06-21 12:40:03");
 		 */
 
 		showConversionFiles(Constants.CAMPAIGN_GOOG, gc);
 	}
 
 	private static GoogleCampaign getGoogleCampaignFromUrl(String url) {
 		MarketoUtility mu = new MarketoUtility();
 		GoogleCampaign gc = null;
 		List<GoogleCampaign> gcExisting = GoogleCampaign.find(
 				"campaignURL = ?", url).fetch();
 
 		if (gcExisting.size() == 1) {
 			gc = gcExisting.get(0);
 			Logger.info("campaign[%d] was configured previously", gc.id);
 		} else {
 			gc = (GoogleCampaign) mu.readSettings(url, Constants.CAMPAIGN_GOOG);
 			GoogleCampaign ngc = getGoogleCampaignFromMunchkin(gc.munchkinId);
 			ngc.campaignURL = url;
 			ngc.save();
 		}
 		return gc;
 	}
 
 	private static GoogleCampaign getGoogleCampaignFromMunchkin(
 			String munchkinId) {
 		MarketoUtility mu = new MarketoUtility();
 		GoogleCampaign gc = null;
 		List<GoogleCampaign> gcExisting = GoogleCampaign.find("munchkinId = ?",
 				munchkinId).fetch();
 
 		if (gcExisting.size() == 1) {
 			gc = gcExisting.get(0);
 			Logger.info("campaign[%d] was configured previously", gc.id);
 		} else {
 			gc = new GoogleCampaign();
 			gc.munchkinId = munchkinId;
 			gc.numEntries = 0;
 			gc.save();
 		}
 		return gc;
 	}
 
 	private static void incrementCampaignCounter(String munchkinId) {
 		GoogleCampaign gc = getGoogleCampaignFromMunchkin(munchkinId);
 		gc.numEntries++;
 		gc.save();
 	}
 
 	private static void processFormula(String url) {
 		MarketoUtility mu = new MarketoUtility();
 		FormulaCampaign fc = (FormulaCampaign) mu.readSettings(url,
 				Constants.CAMPAIGN_FORMULA);
 		fc.save();
 		Logger.info(
 				"campaign[%d] - Kicking off background task to fetch lead list",
 				fc.id);
 		new SyncListAndExecFormula(fc).in(2);
 
 		execFormulaStatus(fc.id);
 	}
 
 	public static void showConversionFiles(int campaignGoog, GoogleCampaign gc) {
 		String urlBase = Play.configuration.getProperty("mkto.serviceUrl");
 		String dirBase = Play.configuration.getProperty("mkto.googBaseDir");
 		String dirName = dirBase + gc.munchkinId;
 		List<String> allConversionFiles = new ArrayList<String>();
 		File dirFile = new File(dirName);
 		File[] listOfFiles = dirFile.listFiles();
 		if (listOfFiles != null) {
 			for (File f : listOfFiles) {
 				String fqFileName = urlBase + "/public/google/" + gc.munchkinId
 						+ "/" + f.getName();
 				Logger.debug("File name is : %s", fqFileName);
 				allConversionFiles.add(fqFileName);
 			}
 		}
 		render(allConversionFiles);
 	}
 
 	public static void savedConfig(int campaignType, Long campaignId) {
 		SMSCampaign sc = SMSCampaign.findById(campaignId);
 		List<SMSCampaign> allCampaigns = new ArrayList<SMSCampaign>();
 		if (sc != null) {
 			allCampaigns = SMSCampaign.find(
 					"munchkinAccountId = ? and status = ?",
 					sc.munchkinAccountId, Constants.CAMPAIGN_STATUS_ACTIVE)
 					.fetch();
 		}
 		render(allCampaigns);
 	}
 
 	public static void execFormulaStatus(Long campaignId) {
 		FormulaCampaign fc = FormulaCampaign.findById(campaignId);
 		List<FormulaCampaign> allCampaigns = new ArrayList<FormulaCampaign>();
 		if (fc != null) {
 			allCampaigns = FormulaCampaign.find("munchkinAccountId = ?",
 					fc.munchkinAccountId).fetch();
 		}
 		render(allCampaigns);
 	}
 
 	public static void smsCallback(String campaignId, String SmsSid,
 			String AccountSid, String From, String To, String Body,
 			String FromCountry) {
 		// look up application in database - if not present, ignore message
 		SMSCampaign sc = SMSCampaign.findById(Long.valueOf(campaignId));
 		if (sc == null) {
 			Logger.fatal("campaign[%s] does not exist", campaignId);
 			renderText("Sorry, we do not know anything about this campaign");
 		}
 		sc.numRecvd++;
 		sc.save();
 		Logger.debug(
 				"campaign [%s] received inbound sms from %s in country %s",
 				sc.id, From, FromCountry);
 		new ProcessInboundMessage(sc, AccountSid, From, Body, FromCountry)
 				.in(2);
 		renderHtml("I just received an SMS" + campaignId);
 	}
 
 	public static void addGCLID(String munchkinId, String leadId,
 			String action, String gclid, String convName, String convValue,
 			String convTime) {
 		// always write to the latest.csv file in the folder
 		String urlBase = Play.configuration.getProperty("mkto.googBaseDir");
 		String dirName = urlBase + munchkinId;
 		File dirFile = new File(dirName);
 		try {
 			Logger.debug("Trying to create directory : %s", dirName);
 			FileUtils.forceMkdir(dirFile);
 			String fileName = dirName + "/latest.csv";
 			Logger.debug("Checking if file : %s exists", fileName);
 			File latestFile = new File(fileName);
 			if (!latestFile.exists()) {
 				Logger.debug("About to create file : %s ", fileName);
 				createGoogleConversionFile(latestFile);
 			}
 			Date date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
 					.parse(convTime);
 			String newDateString = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss a")
 					.format(date); // 9:00
 			String payload = "\n" + action + "," + gclid + "," + convName + ","
 					+ convValue + "," + newDateString;
 			Logger.debug("Writing %s to file : %s ", payload, fileName);
 			appendToFile(fileName, payload);
 			incrementCampaignCounter(munchkinId);
 		} catch (IOException e) {
 			Logger.fatal("Unable to create directory/write to file : %s",
 					e.getMessage());
 			e.printStackTrace();
 		} catch (ParseException e) {
 			Logger.fatal("Unable to parse date : %s", e.getMessage());
 			e.printStackTrace();
 		}
 	}
 
 	public static void phoneQuery(String munchkinId, String leadId,
 			String phoneNum, String format) {
 		if (munchkinId == null || phoneNum == null || munchkinId.equals("")
 				|| phoneNum.equals("")) {
 			renderText("{\"result\" : \"error - must provide munchkinId and phoneNumber\" }");
 		}
 		Logger.debug("received phone query for %s", phoneNum);
 		PhoneQuery pq = new PhoneQuery();
 		pq.munchkinId = munchkinId;
 		pq.phoneNum = phoneNum;
 
 		PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
 		PhoneNumberOfflineGeocoder geocoder = PhoneNumberOfflineGeocoder
 				.getInstance();
 
 		String number = "";
 		PhoneNumber phoneObj;
 		try {
 			phoneObj = phoneUtil.parse(phoneNum, "US");
 			if (format.equalsIgnoreCase(Constants.PHONE_FORMAT_E164)) {
 				pq.format = Constants.PHONE_FORMAT_E164;
 				number = phoneUtil.format(phoneObj, PhoneNumberFormat.E164);
 			} else if (format
 					.equalsIgnoreCase(Constants.PHONE_FORMAT_INTERNATIONAL)) {
 				pq.format = Constants.PHONE_FORMAT_INTERNATIONAL;
 				number = phoneUtil.format(phoneObj,
 						PhoneNumberFormat.INTERNATIONAL);
 			} else if (format.equalsIgnoreCase(Constants.PHONE_FORMAT_NATIONAL)) {
 				pq.format = Constants.PHONE_FORMAT_NATIONAL;
 				number = phoneUtil.format(phoneObj, PhoneNumberFormat.NATIONAL);
 			}
 
 			String city = "";
 			String state = "";
 			String region = "";
 			region = geocoder.getDescriptionForNumber(phoneObj, Locale.ENGLISH);
 			if (region != null && region.contains(",")) {
 				String[] values = region.split(",");
 				city = values[0].trim();
 				state = values[1].trim();
 			} else {
				String regionCode = RegionUtil.getStateShortCode(region);
 				// Special handling for DC, US and Canada
				if (regionCode.equals("US") || regionCode.equals("Canada")) {
					// do not set city or state
				} else if (regionCode.contains("D.C")
						|| regionCode.contains("DC")) {
 					city = "Washington";
 					state = "DC";
 				} else {
 					state = regionCode;
 				}
 			}
 
 			String retVal = createJson(leadId, phoneNum, format, number, city,
 					state);
 			pq.formattedNum = number;
 			pq.city = city;
 			pq.state = state;
 			pq.save();
 
 			Logger.debug("phoneQuery returns :%s", retVal);
 			renderText(retVal);
 
 		} catch (NumberParseException e) {
 			Logger.debug("phoneQuery error");
 			renderText("{\"result\" : \"error - could not parse phone number\" }");
 		}
 
 	}
 
 	private static String createJson(String leadId, String phoneNum,
 			String format, String number, String city, String state) {
 		String retval = "{\"id\":\"" + leadId + "\",\"originalNum\":\""
 				+ phoneNum + "\",\"format\":\"" + format
 				+ "\",\"formattedNum\":\"" + number + "\",\"city\":\"" + city
 				+ "\",\"state\":\"" + state + "\"}";
 		return retval;
 	}
 
 	private static void appendToFile(String fileName, String payload)
 			throws IOException {
 		FileWriter fileWriter = new FileWriter(fileName, true);
 		BufferedWriter bufferWriter = new BufferedWriter(fileWriter);
 		bufferWriter.write(payload);
 		bufferWriter.close();
 	}
 
 	private static void createGoogleConversionFile(File file)
 			throws IOException {
 		String googFileHeader = Play.configuration
 				.getProperty("mkto.googFileHeader");
 		Logger.debug("Writing %s to file : %s ", googFileHeader, file.getPath());
 		FileUtils.writeStringToFile(file, googFileHeader);
 	}
 
 	public static void cancelCampaign(String id) {
 		SMSCampaign sc = SMSCampaign.findById(Long.valueOf(id));
 		if (sc != null) {
 			Logger.info("About to cancel campaign [%s]", id);
 		}
 		// TwilioUtility.deleteApplication(sc.smsGatewayID,
 		// sc.smsGatewayPassword,
 		// sc.smsGatewayApplicationId);
 		sc.status = Constants.CAMPAIGN_STATUS_CANCELED;
 		sc.save();
 		Logger.info("Canceled campaign [%s]", id);
 		renderHtml("Canceled campaign successfully");
 	}
 
 }
