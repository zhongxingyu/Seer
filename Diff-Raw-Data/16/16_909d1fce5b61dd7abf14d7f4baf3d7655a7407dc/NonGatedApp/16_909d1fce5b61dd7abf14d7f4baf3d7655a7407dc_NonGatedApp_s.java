 package controllers;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.math.BigInteger;
 import java.security.SecureRandom;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.Locale;
 
 import jobs.ProcessInboundMessage;
 import jobs.RunFirstSMSRule;
 import models.AddScores;
 import models.BlogCampaign;
 import models.GoogleCampaign;
 import models.Lead;
 import models.PhoneQuery;
 import models.SMSCampaign;
 import models.User;
 
 import org.apache.commons.io.FileUtils;
 
 import play.Logger;
 import play.Play;
 import play.data.validation.MaxSize;
 import play.data.validation.Required;
 import play.libs.Crypto;
 import play.mvc.Controller;
 
 import com.google.i18n.phonenumbers.NumberParseException;
 import com.google.i18n.phonenumbers.PhoneNumberUtil;
 import com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberFormat;
 import com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberType;
 import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;
 import com.google.i18n.phonenumbers.geocoding.PhoneNumberOfflineGeocoder;
 import common.Constants;
 import common.MarketoUtility;
 import common.RegionUtil;
 
 public class NonGatedApp extends Controller {
 
 	public static void registerUser(String munchkinId, String pw1, String pw2,
 			String sec1, String sec2, Boolean eula) {
 		if (munchkinId == null && pw1 == null && pw2 == null) {
 			render();
 		}
 
 		if (munchkinId != null) {
 			munchkinId = munchkinId.trim().toUpperCase();
 		}
 
 		if (eula == null || eula != true) {
 			statusMessage("You must accept the terms of use", true);
 		}
 
 		Logger.debug("mId:%s; Pass:%s", munchkinId, pw1);
 		User user = User.find("byMunchkinId", munchkinId).first();
 		if (user != null) {
 			statusMessage("User already exists", true);
 		} else if (!pw1.equals(pw2)) {
 			statusMessage("Passwords do not match", true);
 		} else if (sec1 == null || "".equals(sec1) || sec2 == null
 				|| "".equals(sec2)) {
 			statusMessage("Secrets have to be valid strings", true);
 		} else {
 			User u1 = new User();
 			u1.munchkinId = munchkinId.toUpperCase();
 			u1.password = pw1;
 			u1.secret1 = sec1;
 			u1.secret2 = sec2;
 			u1.eulaAccepted = eula;
 			Date dt = new Date();
 			u1.eulaAcceptedTS = dt.getTime();
 			u1.save();
 			Application.index("Welcome " + u1.munchkinId);
 		}
 
 		// should never reach here
 		render();
 	}
 
 	public static void forgotPassword(int init, @Required String munchkinId,
 			@Required @MaxSize(2000) String sec1,
 			@Required @MaxSize(2000) String sec2) {
 		if (init == 1) {
 			render();
 		}
 
 		if (init != 1 && validation.hasErrors()) {
 			String errMsg = "";
 			for (play.data.validation.Error error : validation.errors()) {
 				errMsg += error.message();
 			}
 			statusMessage(errMsg, true);
 		}
 
 		if (munchkinId != null) {
 			munchkinId = munchkinId.trim().toUpperCase();
 		}
 
 		Logger.debug("mId:%s; Sec1:%s Sec2:%s", munchkinId, sec1, sec2);
 		User user = User.find("byMunchkinId", munchkinId).first();
 		if (user == null) {
 			statusMessage("Unknown User", true);
 		} else if (!sec1.equals(user.secret1) && !sec2.equals(user.secret2)) {
 			statusMessage("Secrets do not match", true);
 		} else {
 			SecureRandom random = new SecureRandom();
 			String newPass = new BigInteger(130, random).toString(32);
 			user.password = Crypto.passwordHash(newPass);
 			user.save();
 			Logger.debug("Reset password for user %s", munchkinId);
 			String msg = "Welcome - your new password is " + newPass;
 			statusMessage(msg, false);
 			// login(msg);
 			// Application.index("Welcome - your new password is " + newPass);
 		}
 
 		// should never reach here
 		render();
 	}
 
 	public static void statusMessage(String msg, boolean isError) {
 		String type = "";
 		if (isError) {
 			type = "alert";
 		} else {
 			type = "success";
 		}
 		render(msg, type);
 	}
 
 	public static void phoneQuery(String munchkinId, String leadId,
 			String phoneNum, String format) {
 
 		String user = Security.connected();
 		Logger.debug("User is %s", user);
 
 		if (munchkinId == null || leadId == null || phoneNum == null
 				|| munchkinId.equals("") || leadId.equals("")
 				|| phoneNum.equals("")) {
 			renderText("{\"result\" : \"error - must provide munchkinId, leadId"
 					+ " and phoneNumber\" }");
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
 			String country = getCountryName(phoneUtil, phoneObj);
 			region = geocoder.getDescriptionForNumber(phoneObj, Locale.ENGLISH);
 			if (region != null && region.contains(",")) {
 				String[] values = region.split(",");
 				city = values[0].trim();
 				state = values[1].trim();
 			} else {
 				// Special handling for DC, US and Canada
 				if (region.equals("United States") || region.equals("Canada")) {
 					// do not set city or state
 				} else if (region.contains("D.C") || region.contains("DC")) {
 					city = "Washington";
 					state = "DC";
 				} else {
 					String regionCode = RegionUtil.getStateShortCode(region);
 					state = regionCode;
 				}
 			}
 
 			String phType = "";
 
 			PhoneNumberType type = phoneUtil.getNumberType(phoneObj);
 			phType = type.toString();
 
 			String retVal = createJsonForPhoneQueryResponse(leadId, phoneNum,
 					format, number, city, state, country, phType);
 
 			pq.formattedNum = number;
 			pq.leadId = leadId;
 			pq.city = city;
 			pq.state = state;
 			pq.type = phType;
 			pq.country = country;
 			pq.save();
 
 			Logger.debug("phoneQuery returns :%s", retVal);
 			renderText(retVal);
 
 		} catch (NumberParseException e) {
 			Logger.debug("phoneQuery error");
 			renderText("{\"result\" : \"error - could not parse phone number\" }");
 		}
 
 	}
 
 	private static java.lang.String getCountryName(PhoneNumberUtil phoneUtil,
 			PhoneNumber number) {
 		String regionCode = phoneUtil.getRegionCodeForNumber(number);
 		return getRegionDisplayName(regionCode, Locale.ENGLISH);
 	}
 
 	private static java.lang.String getRegionDisplayName(
 			java.lang.String regionCode, Locale english) {
 		return (regionCode == null || regionCode.equals("ZZ") || regionCode
 				.equals(PhoneNumberUtil.REGION_CODE_FOR_NON_GEO_ENTITY)) ? ""
 				: new Locale("", regionCode).getDisplayCountry(Locale.ENGLISH);
 	}
 
 	public static void addScores(String munchkinId, String leadId,
 			String score1, String score2) {
 		try {
 			if (munchkinId == null || leadId == null || munchkinId.equals("")
 					|| leadId.equals("")) {
 				renderText("{\"result\" : \"error - must provide munchkinId and leadId\" }");
 			}
 			int sc1 = Integer.valueOf(score1);
 			int sc2 = Integer.valueOf(score2);
 			int total = sc1 + sc2;
 
 			Logger.debug("Adding scores %d and %d", sc1, sc2);
 			AddScores as = new AddScores();
 			as.munchkinId = munchkinId;
 			as.leadId = leadId;
 			as.score1 = sc1;
 			as.score2 = sc2;
 			as.save();
 
 			String resp = createJsonForAddScoreResponse(leadId, sc1, sc2, total);
 			renderJSON(resp);
 		} catch (NumberFormatException ne) {
 			renderJSON("{\"result\": \"error - could not parse scores\"}");
 			throw ne;
 		}
 	}
 
 	public static void addLead(String munchkinId, Long scId, String leadId,
 			String phoneNumber, String country, String unsub) {
 		try {
 			if (munchkinId == null || leadId == null || scId == null
 					|| munchkinId.equals("") || leadId.equals("")
 					|| "".equals(scId)) {
 				renderText("{\"result\" : \"error - must provide munchkinId, scId and leadId\" }");
 			}
 			if (phoneNumber == null) {
 				phoneNumber = "";
 			}
 			if (country == null) {
 				country = "";
 			}
 			boolean unsubscribed = true;
 			if (unsub == null || "".equals(unsub) || "0".equals(unsub)) {
 				unsubscribed = false;
 			}
 
 			Logger.debug("Adding new lead with id: %s to sub: %s", leadId,
 					munchkinId);
 			List<Lead> leadList = Lead.find("munchkinId = ? and leadId = ? ",
 					munchkinId, Integer.valueOf(leadId)).fetch();
 			Lead sendToLead = null;
 			if (leadList != null && leadList.size() > 0) {
 				Logger.debug(
 						"Lead with id:%s already exists.  Updating attributes",
 						leadId);
 				for (Lead ld : leadList) {
					ld.phoneNumber = phoneNumber;
					ld.country = country;
 					ld.unsubscribed = unsubscribed;
 					ld.save();
 					sendToLead = ld;
 				}
 			} else {
 				Logger.debug("Creating new lead with id:%s in sub:%s", leadId,
 						munchkinId);
 				Lead ld = new Lead();
 				ld.munchkinId = munchkinId;
 				ld.leadId = Integer.valueOf(leadId);
				ld.country = country;
				ld.phoneNumber = phoneNumber;
 				ld.unsubscribed = unsubscribed;
 				ld.save();
 				sendToLead = ld;
 
 			}
 
 			if (sendToLead != null) {
 				Logger.debug("Run First SMS Rule for Lead:%s", leadId);
 				leadList = new ArrayList<Lead>();
 				leadList.add(sendToLead);
 				new RunFirstSMSRule(scId, leadList).now();
 			}
 
 		} catch (NumberFormatException ne) {
 			renderJSON("{\"result\": \"error - could not parse LeadId\"}");
 			throw ne;
 		}
 		renderJSON("{\"result\": \"LeadId " + leadId
 				+ " has been added/updated \"}");
 
 	}
 
 	private static String createJsonForAddScoreResponse(
 			java.lang.String string2, int sc1, int sc2, int total) {
 		return new String("{\"Score1\":" + sc1 + ",\"Score2\":" + sc2
 				+ ",\"total\":" + total + "}");
 	}
 
 	private static String createJsonForPhoneQueryResponse(String leadId,
 			String phoneNum, String format, String number, String city,
 			String state, String country, String phType) {
 		String countryIso2 = RegionUtil.getCountryCode(country);
 		String retval = "{\"id\":\"" + leadId + "\",\"originalNum\":\""
 				+ phoneNum + "\",\"format\":\"" + format
 				+ "\",\"formattedNum\":\"" + number + "\",\"type\":\"" + phType
 				+ "\",\"country\":\"" + country + "\",\"countryIso2\":\""
 				+ countryIso2 + "\",\"city\":\"" + city + "\",\"state\":\""
 				+ state + "\"}";
 		return retval;
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
 		// uppercase for backward compat
 		String dirName = urlBase + munchkinId.toUpperCase();
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
 
 	private static void incrementCampaignCounter(String munchkinId) {
 		GoogleCampaign gc = getGoogleCampaignFromMunchkin(munchkinId);
 		gc.numEntries++;
 		gc.save();
 	}
 
 	protected static GoogleCampaign getGoogleCampaignFromUrl(String url) {
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
 
 	public static BlogCampaign getBlogCampaignFromUrl(java.lang.String url) {
 		MarketoUtility mu = new MarketoUtility();
 		BlogCampaign bc = (BlogCampaign) mu.readSettings(url,
 				Constants.CAMPAIGN_BLOG);
 		return bc;
 	}
 
 }
