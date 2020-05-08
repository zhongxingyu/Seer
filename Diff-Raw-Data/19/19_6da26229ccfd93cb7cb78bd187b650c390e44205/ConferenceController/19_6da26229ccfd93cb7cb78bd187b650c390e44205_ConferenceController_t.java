 package com.imeeting.mvc.controller;
 
 import java.io.IOException;
 import java.sql.SQLException;
 import java.util.Collection;
 import java.util.LinkedList;
 import java.util.List;
 
 import javax.annotation.PostConstruct;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.springframework.dao.DataAccessException;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.bind.annotation.ResponseBody;
 
 import com.imeeting.constants.ConferenceConstants;
 import com.imeeting.framework.ContextLoader;
 import com.imeeting.mvc.model.conference.ConferenceDB;
 import com.imeeting.mvc.model.conference.ConferenceManager;
 import com.imeeting.mvc.model.conference.ConferenceModel;
 import com.imeeting.mvc.model.conference.attendee.AttendeeModel;
 import com.imeeting.mvc.model.conference.attendee.AttendeeModel.OnlineStatus;
 import com.richitec.donkey.client.DonkeyClient;
 import com.richitec.donkey.client.DonkeyHttpResponse;
 import com.richitec.ucenter.model.UserDAO;
 import com.richitec.util.Pager;
 import com.richitec.util.RandomString;
 import com.richitec.vos.client.VOSClient;
 
 /**
  * 
  * client --- create conference request --> Server client <-- response with
  * confId --- Server client --- attendee list --> Server client <-- response
  * from server --- Server client send short message to attendees.
  * 
  * @author huuguanghui
  * 
  */
 
 @Controller
 @RequestMapping(value = "/conference")
 public class ConferenceController extends ExceptionController {
 	public static final int PageSize = 20;
 
 	private static Log log = LogFactory.getLog(ConferenceController.class);
 
 	private ConferenceManager conferenceManager;
 	private DonkeyClient donkeyClient;
 	private ConferenceDB conferenceDao;
 	private UserDAO userDao;
 	private VOSClient vosClient;
 
 	@PostConstruct
 	public void init() {
 		conferenceManager = ContextLoader.getConferenceManager();
 		donkeyClient = ContextLoader.getDonkeyClient();
 		conferenceDao = ContextLoader.getConferenceDAO();
 		userDao = ContextLoader.getUserDAO();
 		vosClient = ContextLoader.getVOSClient();
 	}
 
 	/**
 	 * create a new conference and response with the id of the conference.
 	 * 
 	 * @param String
 	 *            moderator - Moderator's userId.
 	 * @param String
 	 *            attendeeList - An JSON Array that contains all attendees'
 	 *            userId.
 	 * @throws IOException
 	 * @throws SQLException
 	 * @throws JSONException
 	 */
 	@RequestMapping(value = "/create")
 	public void create(
 			HttpServletResponse response,
 			@RequestParam(value = "username") String userName,
 			@RequestParam(value = "attendees", required = false) String attendeeList)
 			throws IOException, DataAccessException, JSONException {
 		// step 0. check account balance
 		Double balance = vosClient.getAccountBalance(userName);
 		if (balance == null) {
 			log.warn("Error balance (" + balance + ") for user <" + userName
 					+ "> to create conference.");
 			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
 			return;
 		}
 
 		if (balance < 1.0) {
 			log.warn("Not enough money (" + balance + ") for user <" + userName
 					+ "> to create conference.");
 			response.sendError(HttpServletResponse.SC_PAYMENT_REQUIRED);
 			return;
 		}
 
 		// step 1. create ConferenceModel in memory
 		String conferenceId = RandomString.genRandomNum(6);
 		ConferenceModel conference = conferenceManager.creatConference(
 				conferenceId, userName);
 
 		AttendeeModel owner = new AttendeeModel(userName, OnlineStatus.online);
 		conference.addAttendee(owner);
 
 		if (attendeeList != null && attendeeList.length() > 0) {
 			try {
 				JSONArray attendeesJsonArray = new JSONArray(attendeeList);
 				for (int i = 0; i < attendeesJsonArray.length(); i++) {
 					String name = attendeesJsonArray.getString(i);
 					AttendeeModel attendee = new AttendeeModel(name);
 					conference.addAttendee(attendee);
 				}
 				conference.fillNicknameForEachAttendee();
 			} catch (JSONException e) {
 				log.error("\nCannot parse attendees : " + attendeeList);
 				conferenceManager.removeConference(conferenceId);
 				throw e;
 			}
 		}
 
 		// step 2. save ConferenceModel in Database.
 		try {
 			conferenceDao.saveConference(conference, null);
 		} catch (DataAccessException e) {
 			log.error("\nSave conference <" + conferenceId
 					+ "> to database error : \n" + "Message : "
 					+ e.getMessage());
 			conferenceManager.removeConference(conferenceId);
 			throw e;
 		}
 
 		// step 3. create audio conference
 		conference.setAudioConfId(conferenceId);
 		Integer vosPhoneNumber = userDao.getVOSPhoneNumber(userName);
 		DonkeyHttpResponse donkeyResp = donkeyClient.createNoControlConference(
 				conferenceId, vosPhoneNumber.toString(),
 				conference.getAllAttendeeName(), conferenceId);
 		if (null == donkeyResp || !donkeyResp.isAccepted()) {
 			log.error("Create audio conference error : "
 					+ (null == donkeyResp ? "NULL Response" : donkeyResp
 							.getStatusCode()));
 			conferenceManager.removeConference(conferenceId);
 			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
 					"Cannot create audio conference");
 			return;
 		}
 
 		// step 4. response to user
 		JSONObject ret = new JSONObject();
 		ret.put(ConferenceConstants.conferenceId.name(), conferenceId);
 		ret.put(ConferenceConstants.audioConfId.name(), conferenceId);
 		ret.put(ConferenceConstants.owner.name(), conference.getOwnerName());
 		response.setStatus(HttpServletResponse.SC_CREATED);
 		response.getWriter().print(ret.toString());
 	}
 
 	/**
 	 * destroy conference
 	 * 
 	 * @param response
 	 * @param userName
 	 * @param conferenceId
 	 * @throws IOException
 	 * @throws SQLException
 	 */
 	@RequestMapping(value = "/destroy")
 	public void destroy(HttpServletResponse response,
 			@RequestParam(value = "username") String userName,
 			@RequestParam String conferenceId) throws IOException {
 		log.debug("destroy conference");
 		ConferenceModel conference = conferenceManager
 				.getConference(conferenceId);
 
 		if (!userName.equals(conference.getOwnerName())) {
 			response.sendError(HttpServletResponse.SC_FORBIDDEN,
 					"only owner can destory conference");
 			return;
 		}
 		
 		conferenceManager.closeConference(conferenceId);
 		conferenceManager.notifyConferenceDestoryed(conferenceId);
 		response.setStatus(HttpServletResponse.SC_OK);
 	}
 
 	/**
 	 * Get all conferences by user name.
 	 * 
 	 * @param response
 	 * @param username
 	 * @throws IOException
 	 * @throws SQLException
 	 * @throws JSONException
 	 */
 	@RequestMapping(value = "/conflist")
 	public void confList(
 			HttpServletResponse response,
 			@RequestParam(value = "offset", required = false, defaultValue = "1") int offset,
 			@RequestParam(value = "username") String username)
 			throws IOException, DataAccessException, JSONException {
 
 		int count = conferenceDao.getConferenceTotalCount(username);
 		JSONArray confs = conferenceDao.getConferenceWithAttendeesList(
 				username, offset, PageSize);
 
 		String url = "/conference/list" + "?";
 		Pager pager = new Pager(offset, PageSize, count, url);
 
 		JSONObject ret = new JSONObject();
 		JSONObject jsonPager = new JSONObject();
 		jsonPager.put("offset", pager.getOffset());
 		jsonPager.put("pagenumber", pager.getPageNumber());
 		jsonPager.put("hasPrevious", pager.getHasPrevious());
 		jsonPager.put("hasNext", pager.getHasNext());
 		jsonPager.put("previousPage", pager.getPreviousPage());
 		jsonPager.put("nextPage", pager.getNextPage());
 		jsonPager.put("count", pager.getSize());
 		ret.put("pager", jsonPager);
 		ret.put("list", confs);
 
 		response.getWriter().print(ret.toString());
 	}
 
 	@RequestMapping(value = "/list")
 	@Deprecated
 	public void list(
 			HttpServletResponse response,
 			@RequestParam(value = "offset", required = false, defaultValue = "1") int offset,
 			@RequestParam(value = "username") String username)
 			throws IOException, DataAccessException, JSONException {
 
 		int count = conferenceDao.getConferenceTotalCount(username);
 		JSONArray confs = conferenceDao.getConferenceWithAttendeesListOld(
 				username, offset, PageSize);
 
 		String url = "/conference/list" + "?";
 		Pager pager = new Pager(offset, PageSize, count, url);
 
 		JSONObject ret = new JSONObject();
 		JSONObject jsonPager = new JSONObject();
 		jsonPager.put("offset", pager.getOffset());
 		jsonPager.put("pagenumber", pager.getPageNumber());
 		jsonPager.put("hasPrevious", pager.getHasPrevious());
 		jsonPager.put("hasNext", pager.getHasNext());
 		jsonPager.put("previousPage", pager.getPreviousPage());
 		jsonPager.put("nextPage", pager.getNextPage());
 		jsonPager.put("count", pager.getSize());
 		ret.put("pager", jsonPager);
 		ret.put("list", confs);
 
 		response.getWriter().print(ret.toString());
 	}
 
 	/**
 	 * get attendee list of the conference which has been opened already
 	 * 
 	 * @param conferenceId
 	 * @param response
 	 * 
 	 * @throws IOException
 	 */
 	@RequestMapping(value = "/attendeeList")
 	public void attendeeList(HttpServletResponse response,
 			@RequestParam String conferenceId) throws IOException {
 		ConferenceModel model = conferenceManager.getConference(conferenceId);
 		Collection<AttendeeModel> attendees = model.getAvaliableAttendees();
 		JSONArray ret = new JSONArray();
 		if (attendees != null && attendees.size() > 0) {
 			for (AttendeeModel att : attendees) {
 				ret.put(att.toJson());
 			}
 		} else {
 			log.error("no attendees in conference <" + conferenceId + ">");
 		}
 		response.getWriter().print(ret.toString());
 	}
 
 	/**
 	 * Add attendees by moderator.
 	 * 
 	 * @param response
 	 * @param conferenceId
 	 * @param attendees
 	 *            - json array string
 	 * @throws IOException
 	 * @throws SQLException
 	 * @throws JSONException
 	 */
 	@RequestMapping(value = "/invite")
 	public void invite(HttpServletResponse response,
 			@RequestParam String conferenceId, @RequestParam String attendees)
 			throws IOException, JSONException {
 		log.info("invite attendees");
 		if (null == attendees || attendees.length() < 0) {
 			response.sendError(HttpServletResponse.SC_BAD_REQUEST);
 			return;
 		}
 
 		List<AttendeeModel> addedAttendeeList = new LinkedList<AttendeeModel>();
 		List<String> addedAttendeeNameList = new LinkedList<String>();
 		ConferenceModel conference = conferenceManager
 				.getConference(conferenceId);
 		JSONArray attendeesJsonArray = new JSONArray(attendees);
 		for (int i = 0; i < attendeesJsonArray.length(); i++) {
 			String name = attendeesJsonArray.getString(i);
 			if (!conference.containsAttendee(name)) {
 				AttendeeModel attendee = new AttendeeModel(name);
 				conference.addAttendee(attendee);
 				addedAttendeeList.add(attendee);
 				addedAttendeeNameList.add(name);
 			} else {
 				AttendeeModel attendee = conference.getAttendee(name);
 				if (attendee.isKickout()) {
 					attendee.invite();
 				}
 			}
 		}
 		conference.fillNicknameForEachAttendee();
 
 		if (addedAttendeeList.size() > 0) {
 			conferenceDao.saveAttendeeBeans(conferenceId, addedAttendeeList);
 
 			// add attendees to audio conference
 			DonkeyHttpResponse donkeyResp = donkeyClient.addMoreAttendee(
 					conference.getAudioConfId(), addedAttendeeNameList,
 					conferenceId);
 			if (null == donkeyResp || !donkeyResp.isAccepted()) {
 				log.error("Add attenddes to audio conference <"
 						+ conference.getAudioConfId()
 						+ "> error : "
 						+ (null == donkeyResp ? "NULL Response" : donkeyResp
 								.getStatusCode()));
 				response.sendError(
 						HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
 						"Cannot add attendees to audio conference");
 				return;
 			}
 		}
 
 		// notify all attendees to update attendee list
 		conference.notifyAttendeesToUpdateMemberList();
 
 		response.setStatus(HttpServletResponse.SC_OK);
 	}
 
 	/**
 	 * Moderator can kick out any attendee of his conference.
 	 * 
 	 * @throws IOException
 	 * 
 	 */
 	@RequestMapping(value = "/kickout")
 	public void kickout(HttpServletResponse response,
 			@RequestParam(value = "conferenceId") String conferenceId,
 			@RequestParam(value = "dstUserName") String dstUserName)
 			throws IOException {
 		log.debug("kickout conference - username: " + dstUserName
 				+ "conferenceId: " + conferenceId);
 		ConferenceModel conferenceModel = conferenceManager
 				.getConference(conferenceId);
 		AttendeeModel attendee = conferenceModel.getAttendee(dstUserName);
 		if (attendee == null) {
 			// user are prohibited to join the conference for he isn't in it
 			response.sendError(HttpServletResponse.SC_FORBIDDEN, "Not Invited!");
 			return;
 		}
 		// update the status
 		attendee.kickout();
 		// remove from database
 		conferenceDao.removeAttendee(conferenceId, attendee);
 
 		// update phone call status and hang up this call
 		if (attendee.statusHangup()) {
 			String sipUri = DonkeyClient.generateSipUriFromPhone(dstUserName);
 			DonkeyHttpResponse donkeyResp = donkeyClient.unjoinConference(
 					conferenceModel.getAudioConfId(), sipUri, conferenceId);
 			if (null == donkeyResp || !donkeyResp.isAccepted()) {
 				log.error("Hangup <"
 						+ dstUserName
 						+ "> in conference <"
 						+ conferenceId
 						+ "> failed : "
 						+ (null == donkeyResp ? "NULL Response" : donkeyResp
 								.getStatusCode()));
 			}
 		}
 
 		conferenceModel.notifyAttendeeKickOut(dstUserName);
 	}
 
 	/**
 	 * Attendee join conference
 	 * 
 	 * @param response
 	 * @param confId
 	 * @param userName
 	 * @throws SQLException
 	 * @throws IOException
 	 * @throws JSONException
 	 */
 	@RequestMapping(value = "/join")
 	public void join(HttpServletResponse response,
 			@RequestParam(value = "conferenceId") String conferenceId,
 			@RequestParam(value = "username") String userName)
 			throws DataAccessException, IOException, JSONException {
 
 		ConferenceModel conference = conferenceManager
 				.getConference(conferenceId);
 		if (null == conference) {
 			log.error("Cannot join <" + userName + "> to conference <"
 					+ conferenceId + "> beacause the conference is not going.");
 			response.sendError(HttpServletResponse.SC_NOT_FOUND);
 			return;
 		}
 
 		AttendeeModel attendee = conference.getAttendee(userName);
 		if (attendee == null) {
 			log.error("Cannot join <" + userName + "> to conference <"
 					+ conferenceId + "> beacause it is not invited.");
 			response.sendError(HttpServletResponse.SC_FORBIDDEN);
 			return;
 		}
 
 		if (attendee.isKickout()) {
 			log.warn("Cannot join <" + userName + "> to conference <"
 					+ conferenceId + "> beacause it is kicked out.");
 			response.sendError(HttpServletResponse.SC_FORBIDDEN);
 			return;
 		} else {
 			attendee.join();
 		}
 
 		// notify other people that User has joined
 		conference.broadcastAttendeeStatus(attendee);
 
 		JSONObject ret = new JSONObject();
 		ret.put(ConferenceConstants.conferenceId.name(),
 				conference.getConferenceId());
 		ret.put(ConferenceConstants.audioConfId.name(),
 				conference.getAudioConfId());
 		ret.put(ConferenceConstants.owner.name(), conference.getOwnerName());
 		response.setStatus(HttpServletResponse.SC_OK);
 		response.getWriter().print(ret.toString());
 	}
 
 	/**
 	 * Attendee unjoin conference
 	 * 
 	 * @param response
 	 * @param confId
 	 * @param userName
 	 * @throws IOException
 	 * @throws SQLException
 	 */
 	@RequestMapping(value = "/unjoin")
 	public void unjoin(HttpServletResponse response,
 			@RequestParam(value = "conferenceId") String conferenceId,
 			@RequestParam(value = "username") String userName)
 			throws IOException, DataAccessException {
 		log.debug("unjoin conference - username: " + userName
 				+ "conferenceId: " + conferenceId);
 		ConferenceModel conferenceModel = conferenceManager
 				.getConference(conferenceId);
 		AttendeeModel attendee = conferenceModel.getAttendee(userName);
 		if (attendee == null) {
 			// user are prohibited to join the conference for he isn't in it
 			response.sendError(HttpServletResponse.SC_FORBIDDEN, "Not Invited!");
 			return;
 		}
 		// update the status
 		attendee.unjoin();
 
 		// update phone call status and hang up this call
 		if (attendee.statusHangup()) {
 			String sipUri = DonkeyClient.generateSipUriFromPhone(userName);
 			DonkeyHttpResponse donkeyResp = donkeyClient.hangupAttendee(
 					conferenceModel.getAudioConfId(), sipUri, conferenceId);
 			if (null == donkeyResp || !donkeyResp.isAccepted()) {
 				log.error("Hangup <"
 						+ userName
 						+ "> in conference <"
 						+ conferenceId
 						+ "> failed : "
 						+ (null == donkeyResp ? "NULL Response" : donkeyResp
 								.getStatusCode()));
 			}
 		}
 		// notify other people that User has unjoined
 		conferenceModel.broadcastAttendeeStatus(attendee);
 
//		conferenceManager.removeConferenceIfEmpty(conferenceId);
 
 		response.setStatus(HttpServletResponse.SC_OK);
 	}
 
 	@RequestMapping(value = "/updateAttendeeStatus")
 	public void updateAttendeeStatus(
 			HttpServletResponse response,
 			@RequestParam(value = "conferenceId") String conferenceId,
 			@RequestParam(value = "username") String userName,
 			@RequestParam(value = "online_status", required = false) String onlineStatus,
 			@RequestParam(value = "video_status", required = false) String videoStatus)
 			throws IOException {
 		log.info("updateAttendeeStatus - conferenceId: " + conferenceId
 				+ " username: " + userName + " online_status: " + onlineStatus
 				+ " video status: " + videoStatus);
 		ConferenceModel conferenceModel = conferenceManager
 				.getConference(conferenceId);
 		conferenceModel.updateAttendeeStatus(userName, onlineStatus,
 				videoStatus);
 	}
 
 	/**
 	 * Call phone number of userId.
 	 * 
 	 * @param response
 	 * @param confId
 	 * @param username
 	 * @throws IOException
 	 */
 	@RequestMapping(value = "/call")
 	public void call(HttpServletResponse response,
 			@RequestParam(value = "conferenceId") String conferenceId,
 			@RequestParam(value = "dstUserName") String dstUserName)
 			throws IOException {
 		ConferenceModel conference = conferenceManager
 				.getConference(conferenceId);
 		AttendeeModel attendee = conference.getAttendee(dstUserName);
 		if (attendee == null) {
 			response.sendError(HttpServletResponse.SC_FORBIDDEN, "Not Invited!");
 			return;
 		}
 
 		// transfer attendee status from Initial to CallWait
 		if (!attendee.statusCall()) {
 			log.error("Cannot call <" + dstUserName
 					+ ">, beacuse attendee status is "
 					+ attendee.getPhoneCallStatus());
 			response.sendError(HttpServletResponse.SC_CONFLICT,
 					"Conflicted Command!");
 			return;
 		}
 
 		String sipUri = DonkeyClient.generateSipUriFromPhone(dstUserName);
 		DonkeyHttpResponse donkeyResp = donkeyClient.callAttendee(
 				conference.getAudioConfId(), sipUri, conferenceId);
 		if (null == donkeyResp || !donkeyResp.isAccepted()) {
 			attendee.statusCallTerminated();
 			log.error("Call <"
 					+ dstUserName
 					+ "> in conferece <"
 					+ conferenceId
 					+ "> failed : "
 					+ (null == donkeyResp ? "NULL Response" : donkeyResp
 							.getStatusCode()));
 			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
 					"Call <" + dstUserName + "> failed!");
 			return;
 		}
 		conference.broadcastAttendeeStatus(attendee);
 		response.setStatus(HttpServletResponse.SC_OK);
 	}
 
 	@RequestMapping(value = "/callAll")
 	public void callAll(HttpServletResponse response,
 			@RequestParam(value = "conferenceId") String conferenceId) {
 		ConferenceModel conference = conferenceManager
 				.getConference(conferenceId);
 		for (AttendeeModel attendee : conference.getAvaliableAttendees()) {
 			callAttendee(conference, attendee);
 		}
 		conference.notifyAttendeesToUpdateMemberList();
 		response.setStatus(HttpServletResponse.SC_OK);
 	}
 
 	private void callAttendee(ConferenceModel conference, AttendeeModel attendee) {
 		// transfer attendee status from Initial to CallWait
 		if (!attendee.statusCall()) {
 			log.error("Cannot call <" + attendee.getUsername()
 					+ ">, beacuse attendee status is "
 					+ attendee.getPhoneCallStatus());
 			return;
 		}
 
 		String sipUri = DonkeyClient.generateSipUriFromPhone(attendee
 				.getUsername());
 		DonkeyHttpResponse donkeyResp = donkeyClient.callAttendee(
 				conference.getAudioConfId(), sipUri,
 				conference.getConferenceId());
 		if (null == donkeyResp || !donkeyResp.isAccepted()) {
 			attendee.statusCallTerminated();
 			log.error("Call <"
 					+ attendee.getUsername()
 					+ "> in conferece <"
 					+ conference.getConferenceId()
 					+ "> failed : "
 					+ (null == donkeyResp ? "NULL Response" : donkeyResp
 							.getStatusCode()));
 			return;
 		}
 	}
 
 	/**
 	 * Hang up phone of userId.
 	 * 
 	 * @param response
 	 * @param confId
 	 * @param username
 	 * @throws IOException
 	 */
 	@RequestMapping(value = "/hangup", method = RequestMethod.POST)
 	public void hangup(HttpServletResponse response,
 			@RequestParam(value = "conferenceId") String conferenceId,
 			@RequestParam(value = "dstUserName") String dstUserName)
 			throws IOException {
 		ConferenceModel conference = conferenceManager
 				.getConference(conferenceId);
 		AttendeeModel attendee = conference.getAttendee(dstUserName);
 		if (attendee == null) {
 			response.sendError(HttpServletResponse.SC_FORBIDDEN);
 			return;
 		}
 
 		// transfer attendee status from Initial to CallWait
 		if (!attendee.statusHangup()) {
 			log.error("Cannot hangup <" + dstUserName
 					+ ">, beacuse attendee status is "
 					+ attendee.getPhoneCallStatus());
 			response.sendError(HttpServletResponse.SC_CONFLICT);
 			return;
 		}
 
 		String sipUri = DonkeyClient.generateSipUriFromPhone(dstUserName);
 		DonkeyHttpResponse donkeyResp = donkeyClient.hangupAttendee(
 				conference.getAudioConfId(), sipUri, conferenceId);
 		if (null == donkeyResp || !donkeyResp.isAccepted()) {
 			log.error("Hangup <"
 					+ dstUserName
 					+ "> in conference <"
 					+ conferenceId
 					+ "> failed : "
 					+ (null == donkeyResp ? "NULL Response" : donkeyResp
 							.getStatusCode()));
 			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
 					"Hangup <" + dstUserName + "> failed!");
 			return;
 		}
 		response.setStatus(HttpServletResponse.SC_OK);
 	}
 
 	/**
 	 * Mute phone of userId.
 	 * 
 	 * @param response
 	 * @param confId
 	 * @param username
 	 * @throws IOException
 	 */
 	@RequestMapping(value = "/mute", method = RequestMethod.POST)
 	public void mute(HttpServletResponse response,
 			@RequestParam(value = "conferenceId") String conferenceId,
 			@RequestParam(value = "dstUserName") String dstUserName)
 			throws IOException {
 		ConferenceModel conference = conferenceManager
 				.getConference(conferenceId);
 		AttendeeModel attendee = conference.getAttendee(dstUserName);
 		if (attendee == null) {
 			response.sendError(HttpServletResponse.SC_FORBIDDEN);
 			return;
 		}
 
 		String sipUri = DonkeyClient.generateSipUriFromPhone(dstUserName);
 		DonkeyHttpResponse donkeyResp = donkeyClient.muteAttendee(
 				conference.getAudioConfId(), sipUri, conferenceId);
 		if (null == donkeyResp || !donkeyResp.isAccepted()) {
 			log.error("Mute <"
 					+ dstUserName
 					+ "> in conference <"
 					+ conferenceId
 					+ "> failed : "
 					+ (null == donkeyResp ? "NULL Response" : donkeyResp
 							.getStatusCode()));
 			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
 					"Mute <" + dstUserName + "> failed!");
 			return;
 		}
 		response.setStatus(HttpServletResponse.SC_OK);
 	}
 
 	/**
 	 * Unmute phone of userId.
 	 * 
 	 * @param response
 	 * @param confId
 	 * @param username
 	 * @throws IOException
 	 */
 	@RequestMapping(value = "/unmute", method = RequestMethod.POST)
 	public void unmute(HttpServletResponse response,
 			@RequestParam(value = "conferenceId") String conferenceId,
 			@RequestParam(value = "dstUserName") String dstUserName)
 			throws IOException {
 		ConferenceModel conference = conferenceManager
 				.getConference(conferenceId);
 		AttendeeModel attendee = conference.getAttendee(dstUserName);
 		if (attendee == null) {
 			response.sendError(HttpServletResponse.SC_FORBIDDEN);
 			return;
 		}
 
 		String sipUri = DonkeyClient.generateSipUriFromPhone(dstUserName);
 		DonkeyHttpResponse donkeyResp = donkeyClient.unmuteAttendee(
 				conference.getAudioConfId(), sipUri, conferenceId);
 		if (null == donkeyResp || !donkeyResp.isAccepted()) {
 			log.error("Unmute <"
 					+ dstUserName
 					+ "> in conference <"
 					+ conferenceId
 					+ "> failed : "
 					+ (null == donkeyResp ? "NULL Response" : donkeyResp
 							.getStatusCode()));
 			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
 					"Unmute <" + dstUserName + "> failed!");
 			return;
 		}
 		response.setStatus(HttpServletResponse.SC_OK);
 	}
 
 	@RequestMapping("/editTitle")
 	public void editTitle(
 			@RequestParam(value = "conferenceId") String conferenceId,
 			@RequestParam(value = "title") String title,
 			HttpServletResponse response) throws DataAccessException {
 		int r = conferenceDao.editConferenceTitle(conferenceId, title);
 		if (1 != r) {
 			log.error("editTitle for conference <" + conferenceId + "> error");
 		}
 	}
 
 	@RequestMapping("/hide")
 	public void hideConference(HttpServletResponse response,
 			@RequestParam(value = "conferenceId") String conferenceId,
 			@RequestParam(value = "username") String userName)
 			throws DataAccessException {
 		int r = conferenceDao.hideConference(conferenceId, userName);
 		if (1 != r) {
 			log.error("hide conference <" + conferenceId + "> for user <"
 					+ userName + "> result=" + r);
 		}
 	}
 
 	@RequestMapping(value = "/heartbeat", method = RequestMethod.POST)
 	public @ResponseBody
 	String heartbeat(HttpSession session,
 			@RequestParam(value = "conferenceId") String conferenceId,
 			@RequestParam(value = "username") String userName) {
 		ConferenceModel conference = conferenceManager
 				.getConference(conferenceId);
 		AttendeeModel attendee = conference.getAttendee(userName);
 		if (null != attendee) {
 			attendee.heartBeat();
 		}
 		return "ok";
 	}
 }
