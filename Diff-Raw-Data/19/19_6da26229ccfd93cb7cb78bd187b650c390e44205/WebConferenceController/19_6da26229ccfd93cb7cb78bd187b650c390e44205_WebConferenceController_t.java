 package com.imeeting.mvc.controller;
 
 import java.io.IOException;
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
 import org.springframework.web.servlet.ModelAndView;
 
 import com.imeeting.constants.ConferenceConstants;
 import com.imeeting.constants.WebConstants;
 import com.imeeting.framework.ContextLoader;
 import com.imeeting.mvc.model.addressbook.AddressBookDAO;
 import com.imeeting.mvc.model.conference.ConferenceDB;
 import com.imeeting.mvc.model.conference.ConferenceManager;
 import com.imeeting.mvc.model.conference.ConferenceModel;
 import com.imeeting.mvc.model.conference.attendee.AttendeeModel;
 import com.imeeting.mvc.model.conference.attendee.AttendeeModel.OnlineStatus;
 import com.imeeting.web.user.UserBean;
 import com.mongodb.DBObject;
 import com.richitec.donkey.client.DonkeyClient;
 import com.richitec.donkey.client.DonkeyHttpResponse;
 import com.richitec.ucenter.model.UserDAO;
 import com.richitec.util.RandomString;
 import com.richitec.vos.client.VOSClient;
 
 @Controller
 @RequestMapping(value = "/webconf")
 public class WebConferenceController {
 
 	private static Log log = LogFactory.getLog(WebConferenceController.class);
 
 	private ConferenceManager conferenceManager;
 	private ConferenceDB conferenceDao;
 	private DonkeyClient donkeyClient;
 	private AddressBookDAO addressBookDao;
 	private UserDAO userDao;
 	private VOSClient vosClient;
 
 	@PostConstruct
 	public void init() {
 		conferenceManager = ContextLoader.getConferenceManager();
 		conferenceDao = ContextLoader.getConferenceDAO();
 		donkeyClient = ContextLoader.getDonkeyClient();
 		addressBookDao = ContextLoader.getAddressBookDAO();
 		userDao = ContextLoader.getUserDAO();
 		vosClient = ContextLoader.getVOSClient();
 	}
 
 	@RequestMapping(method = RequestMethod.GET)
 	public String join() {
 		return "webconf/join";
 	}
 
 	@RequestMapping(value = "arrange")
 	public ModelAndView arrange(HttpSession session) {
 		ModelAndView mv = new ModelAndView();
 		UserBean user = (UserBean) session.getAttribute(UserBean.SESSION_BEAN);
 		List<DBObject> contacts = addressBookDao.getAllContacts(user.getUserName(),
 				null);
 		mv.addObject(WebConstants.addressbook.name(), contacts);
 
 		mv.setViewName("webconf/arrange");
 		return mv;
 	}
 
 	@RequestMapping(value = "create")
 	public void create(
 			HttpServletResponse response,
 			HttpSession session,
 			@RequestParam(value = "title", required = false) String title,
 			@RequestParam(value = "attendees", required = false) String attendeeList)
 			throws JSONException, IOException {
 		log.info("attendees: " + attendeeList);
 		UserBean user = (UserBean) session.getAttribute(UserBean.SESSION_BEAN);
 		
 		//step 0. check account balance
 		Double balance = vosClient.getAccountBalance(user.getUserName());
 		if (balance == null) {
 			log.warn("Error balance (" +  balance +") for user <" + 
 					user.getUserName() + "> to create conference.");
 			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
 			return;
 		}
 		
 		if (balance < 1.0){
 			log.warn("Not enough money (" +  balance +") for user <" + 
 					user.getUserName() + "> to create conference.");
 			response.sendError(HttpServletResponse.SC_PAYMENT_REQUIRED);
 			return;
 		}
 
 		// step 1. create ConferenceModel in memory
 		String conferenceId = RandomString.genRandomNum(6);
 		ConferenceModel conference = conferenceManager.creatConference(
 				conferenceId, user.getUserName());
 
 		AttendeeModel owner = new AttendeeModel(user.getUserName(),
 				OnlineStatus.online);
 		conference.addAttendee(owner);
 
 		if (attendeeList != null && attendeeList.length() > 0) {
 			try {
 				JSONArray attendeesJsonArray = new JSONArray(attendeeList);
 				for (int i = 0; i < attendeesJsonArray.length(); i++) {
 					String userName = attendeesJsonArray.getString(i);
 					AttendeeModel attendee = new AttendeeModel(userName);
 					conference.addAttendee(attendee);
 				}
 				conference.fillNicknameForEachAttendee();
 				conference.sendSMSToAttendees(attendeesJsonArray);
 			} catch (JSONException e) {
 				log.error("\nCannot parse attendees : " + attendeeList);
 				conferenceManager.removeConference(conferenceId);
 				throw e;
 			}
 		}
 
 		// step 2. save ConferenceModel in Database.
 		try {
 			conferenceDao.saveConference(conference, title);
 		} catch (DataAccessException e) {
 			log.error("\nSave conference <" + conferenceId
 					+ "> to database error : \n" + "Message : "
 					+ e.getMessage());
 			conferenceManager.removeConference(conferenceId);
 			throw e;
 		}
 
 		// step 3. create audio conference
 		conference.setAudioConfId(conferenceId);
 		Integer vosPhoneNumber = userDao.getVOSPhoneNumber(user.getUserName());
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
 
 		session.setAttribute(ConferenceConstants.conferenceId.name(), conferenceId);
 		// step 4. response to user
 		JSONObject ret = new JSONObject();
 		ret.put(ConferenceConstants.conferenceId.name(), conferenceId);
 		response.setStatus(HttpServletResponse.SC_CREATED);
 		response.getWriter().print(ret.toString());
 	}
 
 	@RequestMapping(value = "enterConf")
 	public ModelAndView joinOwnerToConf(HttpSession session) {
 		UserBean user = (UserBean) session.getAttribute(UserBean.SESSION_BEAN);
 		String conferenceId = (String) session.getAttribute(ConferenceConstants.conferenceId.name());
 		ModelAndView mv = new ModelAndView();
 		
 		ConferenceModel conference = conferenceManager.getConference(conferenceId);
 		AttendeeModel attendee = conference.getAttendee(user.getUserName());
 		
 		attendee.heartBeat();
 
 		// notify all attendees to update attendee list
 		conference.notifyAttendeesToUpdateMemberList();
 
 		mv.addObject("conference", conference);
 		mv.setViewName("webconf/conf");
 		
 		return mv;
 	}
 	
 	@RequestMapping(method = RequestMethod.POST)
 	public ModelAndView join(HttpSession session,
 			@RequestParam(value = "confId") String confId) {
 		UserBean user = (UserBean) session.getAttribute(UserBean.SESSION_BEAN);
 		ModelAndView mv = new ModelAndView();
 		ConferenceModel conference = conferenceManager.getConference(confId);
 		if (null == conference) {
 			mv.addObject("errorInfo", "noconference");
 			mv.setViewName("webconf/join");
 			return mv;
 		}
 
 		AttendeeModel attendee = conference.getAttendee(user.getUserName());
 		if (null == attendee) {
 			attendee = new AttendeeModel(user.getUserName());
 			attendee.setNickname(user.getNickName());
 			conference.addAttendee(attendee);
 			conferenceDao.saveAttendee(confId, attendee);
 
 			// add attendees to audio conference
 			DonkeyHttpResponse donkeyResp = donkeyClient.addAttendee(
 					conference.getAudioConfId(), attendee.getUsername(),
 					conference.getConferenceId());
 			if (null == donkeyResp || !donkeyResp.isAccepted()) {
 				log.error("Add attenddes to audio conference <"
 						+ conference.getAudioConfId()
 						+ "> error : "
 						+ (null == donkeyResp ? "NULL Response" : donkeyResp
 								.getStatusCode()));
 				// TODO: join conference failed
 				mv.addObject("errorInfo", "donkeyFailed");
 				mv.setViewName("webconf/join");
 				return mv;
 			}
 		}
 
 		if (attendee.isKickout()) {
 			mv.addObject("errorInfo", "kickout");
 			mv.setViewName("webconf/join");
 			return mv;
 		}
 
 		attendee.join();
 		attendee.heartBeat();
 
 		// notify all attendees to update attendee list
 		conference.notifyAttendeesToUpdateMemberList();
 
 		mv.addObject("conference", conference);
 		mv.setViewName("webconf/conf");
 		return mv;
 	}
 
 	@RequestMapping(value = "unjoin", method = RequestMethod.GET)
 	public String unjoin(HttpSession session, HttpServletResponse response,
 			@RequestParam(value = "confId") String confId) throws IOException {
 		UserBean user = (UserBean) session.getAttribute(UserBean.SESSION_BEAN);
 		ConferenceModel conferenceModel = conferenceManager
 				.getConference(confId);
 		if (null == conferenceModel){
 		    log.error("Conference <" + confId + "> is null.");
 		    return "redirect:/myconference";
 		}
 		
 		AttendeeModel attendee = conferenceModel.getAttendee(user.getUserName());
 		if (attendee == null) {
 			// user are prohibited to join the conference for he isn't in it
 			response.sendError(HttpServletResponse.SC_FORBIDDEN, "Not Invited!");
 		} else {
 			// update the status
 			attendee.unjoin();
 
 			// update phone call status and hang up this call
 			if (attendee.statusHangup()) {
 				String sipUri = DonkeyClient.generateSipUriFromPhone(user
 						.getUserName());
 				DonkeyHttpResponse donkeyResp = donkeyClient.hangupAttendee(
 						conferenceModel.getAudioConfId(), sipUri,
 						conferenceModel.getConferenceId());
 				if (null == donkeyResp || !donkeyResp.isAccepted()) {
 					log.error("Hangup <"
 							+ user.getUserName()
 							+ "> in conference <"
 							+ conferenceModel.getConferenceId()
 							+ "> failed : "
 							+ (null == donkeyResp ? "NULL Response"
 									: donkeyResp.getStatusCode()));
 				}
 			}
 
 			// notify other people that User has unjoined
 			conferenceModel.broadcastAttendeeStatus(attendee);
 
//			conferenceManager.removeConferenceIfEmpty(confId);
 		}
 
 		return "redirect:/myconference";
 	}
 
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
 	public void callAll(
 			HttpServletResponse response,
 			@RequestParam(value = "conferenceId") String conferenceId){
 		ConferenceModel conference = conferenceManager.getConference(conferenceId);
 		for (AttendeeModel attendee : conference.getAvaliableAttendees()){
 			callAttendee(conference, attendee);
 		}
 		conference.notifyAttendeesToUpdateMemberList();
 		response.setStatus(HttpServletResponse.SC_OK);
 	}
 	
 	private void callAttendee(ConferenceModel conference, AttendeeModel attendee){
 		// transfer attendee status from Initial to CallWait
 		if (!attendee.statusCall()) {
 			log.error("Cannot call <" + attendee.getUsername()
 					+ ">, beacuse attendee status is "
 					+ attendee.getPhoneCallStatus());
 			return;
 		}
 		
 		String sipUri = DonkeyClient.generateSipUriFromPhone(attendee.getUsername());
 		DonkeyHttpResponse donkeyResp = donkeyClient.callAttendee(
 				conference.getAudioConfId(), sipUri, conference.getConferenceId());
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
 	
 	@RequestMapping(value = "/hangupAll")
 	public void hangupAll(			
 			HttpServletResponse response,
 			@RequestParam(value = "conferenceId") String conferenceId){
 		ConferenceModel conference = conferenceManager.getConference(conferenceId);
 		for (AttendeeModel attendee : conference.getAvaliableAttendees()){
 			hangupAttendee(conference, attendee);
 		}
 		conference.notifyAttendeesToUpdateMemberList();
 		response.setStatus(HttpServletResponse.SC_OK);
 	}
 	
 	private void hangupAttendee(ConferenceModel conference, AttendeeModel attendee){
 		// transfer attendee status from Initial to CallWait
 		if (!attendee.statusHangup()) {
 			log.error("Cannot hangup <" + attendee.getUsername()
 					+ ">, beacuse attendee status is "
 					+ attendee.getPhoneCallStatus());
 		}
 
 		String sipUri = DonkeyClient.generateSipUriFromPhone(attendee.getUsername());
 		DonkeyHttpResponse donkeyResp = donkeyClient.hangupAttendee(
 				conference.getAudioConfId(), sipUri, conference.getConferenceId());
 		if (null == donkeyResp || !donkeyResp.isAccepted()) {
 			log.error("Hangup <"
 					+ attendee.getUsername()
 					+ "> in conference <"
 					+ conference.getConferenceId()
 					+ "> failed : "
 					+ (null == donkeyResp ? "NULL Response" : donkeyResp
 							.getStatusCode()));
 			return;
 		}
 	}
 	
 	@RequestMapping(value = "/attendeeList")
 	public ModelAndView attendeeList(@RequestParam String conferenceId)
 			throws IOException {
 		ConferenceModel conference = conferenceManager
 				.getConference(conferenceId);
 		ModelAndView mv = new ModelAndView();
 		mv.addObject("conference", conference);
 		mv.setViewName("webconf/attendeelist");
 		return mv;
 	}
 
 	@RequestMapping(value = "/heartbeat", method = RequestMethod.POST)
 	public @ResponseBody
 	String heartbeat(HttpSession session,
 			@RequestParam(value = "conferenceId") String conferenceId) {
 		UserBean user = (UserBean) session.getAttribute(UserBean.SESSION_BEAN);
 		ConferenceModel conference = conferenceManager
 				.getConference(conferenceId);
 		if (null == conference){
 		    log.error("heartbeat to null conference <" + conferenceId + ">");
 		    return "null";
 		}
 		AttendeeModel attendee = conference.getAttendee(user.getUserName());
 		if (null != attendee) {
 			attendee.heartBeat();
 		}
 
 		return "ok";
 	}
 }
