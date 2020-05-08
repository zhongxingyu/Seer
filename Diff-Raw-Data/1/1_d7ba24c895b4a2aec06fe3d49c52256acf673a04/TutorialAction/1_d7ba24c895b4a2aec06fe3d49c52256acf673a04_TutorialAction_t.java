 package com.edu.webapp.action;
 
 import java.io.IOException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 import java.util.Set;
 import java.util.TimeZone;
 import java.util.Map.Entry;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.springframework.mail.MailException;
 
 import com.edu.Constants;
 import com.edu.model.Tutorial;
 import com.edu.model.TutorialSchedule;
 import com.edu.model.TutorialScheduleStudent;
 import com.edu.model.TutorialScheduleStudentKey;
 import com.edu.model.User;
 import com.edu.util.DateUtil;
 import com.edu.webapp.util.RequestUtil;
 import com.opensymphony.xwork2.Preparable;
 
 /**
  * Action for facilitating User Management feature.
  */
 public class TutorialAction extends BaseAction implements Preparable {
 	private static final long serialVersionUID = 6776558938712115192L;
 	private List<Tutorial> tutorials;
 	private Tutorial tutorial;
 	private Long id;
 	private String query;
 	//
 	private List<TutorialSchedule> tutorialSchedules;
 	private TutorialSchedule tutorialSchedule;
 
 	/**
 	 * Grab the entity from the database before populating with request parameters
 	 */
 	public void prepare() {
 		getRequest().getSession();
 		// prevent failures on new
 		if (getRequest().getMethod().equalsIgnoreCase("post")) {
 			String tutorialId = getRequest().getParameter("tutorial.id");
 			if (!(tutorialId == null || "".equals(tutorialId))) {
 				tutorial = tutorialManager.getTutorial(new Long(tutorialId));
 			}
 			String tutorialScheduleId = getRequest().getParameter(
 					"tutorialSchedule.id");
 			if (!(tutorialScheduleId == null || "".equals(tutorialScheduleId))) {
 				tutorialSchedule = tutorialManager
 						.getTutorialSchedule(new Long(tutorialScheduleId));
 			}
 		}
 		try {
 			//			mailMessage.setFrom("AppFuse <outute@163.com>");
 			//			mailMessage.setSubject("Test");
 			//			mailMessage.setTo("Tyler<iffiff1@163.com>");
 			//
 			//			Map<String, Object> model = new HashMap<String, Object>();
 			//			model.put("user", getUser());
 			//			// TODO: figure out how to get bundle specified in struts.xml
 			//			// model.put("bundle", getTexts());
 			//			model.put("message", "testest");
 			//			model.put("applicationURL", "test");
 			//			mailEngine.sendMessage(mailMessage, templateName, model);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * Holder for tutorials to display on list screen
 	 *
 	 * @return list of tutorial
 	 */
 	public List<Tutorial> getTutorials() {
 		return tutorials;
 	}
 
 	public void setId(Long id) {
 		this.id = id;
 	}
 
 	public Long getId() {
 		return id;
 	}
 
 	public Tutorial getTutorial() {
 		return tutorial;
 	}
 
 	public void setTutorial(Tutorial tutorial) {
 		this.tutorial = tutorial;
 	}
 
 	public void setQ(String q) {
 		this.query = q;
 	}
 
 	public List<TutorialSchedule> getTutorialSchedules() {
 		return tutorialSchedules;
 	}
 
 	public TutorialSchedule getTutorialSchedule() {
 		return tutorialSchedule;
 	}
 
 	public void setTutorialSchedule(TutorialSchedule tutorialSchedule) {
 		this.tutorialSchedule = tutorialSchedule;
 	}
 
 	/**
 	 * Delete the tutorial passed in.
 	 *
 	 * @return success
 	 */
 	public String delete() {
 		if (tutorial.getId() != null) {
 			tutorialManager.removeTutorial(tutorial.getId());
 			List<Object> args = new ArrayList<Object>();
 			args.add(tutorial.getName());
 			saveMessage(getText("Tutorial.deleted", args));
 		}
 		return SUCCESS;
 	}
 
 	/**
 	 * Grab the tutorial from the database based on the "id" passed in.
 	 *
 	 * @return success if tutorial found
 	 * @throws IOException can happen when sending a "forbidden" from response.sendError()
 	 */
 	public String edit() throws IOException {
 		// if a user's id is passed in
 		if (id != null) {
 			// lookup the tutorial using tutorial id
 			tutorial = tutorialManager.getTutorial(id);
 		} else {
 			return null;
 		}
 
 		return SUCCESS;
 	}
 
 	/**
 	 * Default: just returns "success"
 	 *
 	 * @return "success"
 	 */
 	public String execute() {
 		return SUCCESS;
 	}
 
 	/**
 	 * Sends users to "mainMenu" when !from.equals("list"). Sends everyone else to "cancel"
 	 *
 	 * @return "mainMenu" or "cancel"
 	 */
 	public String cancel() {
 		if (!"list".equals(from)) {
 			return "mainMenu";
 		}
 		return "cancel";
 	}
 
 	/**
 	 * Save tutorial
 	 *
 	 * @return success if everything worked, otherwise input
 	 * @throws Exception when setting "access denied" fails on response
 	 */
 	public String save() throws Exception {
 
 		//Integer originalVersion = tutorial.getVersion();
 		HttpServletRequest request = getRequest();
 		boolean isNew = tutorial != null && tutorial.getId() == null;
 		// only attempt to change tutorial if user is tutor or admin
 		// for other users, prepare() method will handle populating
 		if (isRole(Constants.ADMIN_ROLE) || isRole(Constants.TUTOR_ROLE)) {
 			if (isNew) {
 				User loginUser;
 				Set<User> tutors;
 
 				tutorial.setEnabled(true);
 				tutorial.setTutorialLocked(false);
 				tutorial.setTutorialExpired(false);
 				// TODO to be implemented
 				tutorial.setSchedule(new Date());
 				tutorial.setCreateTime(new Date());
 				tutorial.setModifyTime(new Date());
 				tutorial.setOpenDays(5);
 				// Set the login user as the tutor
 				tutors = new HashSet<User>();
 				User user = getUser();
 				if (user != null) {
 					tutors.add(user);
 				}
 				tutorial.setTutors(tutors);
 			}
 			try {
 				tutorialManager.saveTutorial(tutorial);
				getRequest().getSession().setAttribute("newTutorialId", tutorial.getId());
 			} catch (Exception ade) {
 				ade.printStackTrace();
 				// thrown by UserSecurityAdvice configured in aop:advisor userManagerSecurity
 				log.warn(ade.getMessage());
 				getResponse().sendError(HttpServletResponse.SC_FORBIDDEN);
 				return null;
 			}
 
 			if (!"list".equals(from)) {
 				// add success messages
 				saveMessage(getText("tutorial.saved"));
 				return SUCCESS;
 			} else {
 				// add success messages
 				List<Object> args = new ArrayList<Object>();
 				args.add(tutorial.getName());
 				if (isNew) {
 					saveMessage(getText("tutorial.added", args));
 					// Send an account information e-mail
 					mailMessage.setSubject(getText("signup.email.subject"));
 					try {
 						sendUserMessage(userManager.getUserByUsername(request
 								.getRemoteUser()), getText(
 								"newtutorial.email.message", args), RequestUtil
 								.getAppURL(getRequest()));
 					} catch (MailException me) {
 						addActionError(me.getCause().getLocalizedMessage());
 					}
 					return SUCCESS;
 				} else {
 					saveMessage(getText("tutorial.updated", args));
 					tutorial = tutorialManager.getTutorial(tutorial.getId());
 					return INPUT;
 				}
 			}
 		}
 		log.warn("Failed to save tutorial");
 		getResponse().sendError(HttpServletResponse.SC_FORBIDDEN);
 		return null;
 	}
 
 	/**
 	 * Fetch all tutorials from database and put into local "tutorials" variable for retrieval in the UI.
 	 *
 	 * @return "success" if no exceptions thrown
 	 */
 	public String list() {
 		tutorials = tutorialManager.search(query);
 		return SUCCESS;
 	}
 
 	/**
 	 * Fetch all tutorials from database and put into local "tutorials" variable for retrieval in the UI.
 	 *
 	 * @return "success" if no exceptions thrown
 	 */
 	public String listAll() {
 		tutorials = tutorialManager.getTutorials();
 		if (!tutorials.isEmpty()) {
 			tutorial = tutorials.get(0);
 		}
 		return SUCCESS;
 	}
 
 	/**
 	 * Fetch all users from database and put into local "tutorials" variable for retrieval in the UI.
 	 *
 	 * @return "success" if no exceptions thrown
 	 */
 	public String test() {
 		tutorials = tutorialManager.search(query);
 		return SUCCESS;
 	}
 
 	/**
 	 * list all TutorialSchedules of the tutorial
 	 * @return
 	 * @author <a href="mailto:iffiff1@hotmail.com">Tyler Chen</a> 
 	 * @since 2011-10-18
 	 */
 	public String listAllTutorialSchedule() {
 		id = id == null ? tutorial.getId() : id;
 		if (id == null) {
 			tutorialSchedules = new ArrayList<TutorialSchedule>();
 		} else {
 			tutorialSchedules = tutorialManager
 					.getAllTutorialScheduleByTutorialId(id);
 		}
 		return SUCCESS;
 	}
 
 	/**
 	 * save a TutorialSchedule to the tutorial 
 	 * @return
 	 * @author <a href="mailto:iffiff1@hotmail.com">Tyler Chen</a> 
 	 * @since 2011-10-18
 	 */
 	public String addTutorialSchedule() {
 		String fromTime = getRequest().getParameter("fromTime");
 		String toTime = getRequest().getParameter("toTime");
 		int from1 = 0;
 		int to1 = 0;
 		{
 			fromTime = fromTime == null ? "" : fromTime.trim().toLowerCase();
 			toTime = toTime == null ? "" : toTime.trim().toLowerCase();
 			if (fromTime.endsWith("pm")) {
 				String[] times = fromTime.replace("pm", "").split(":");
 				from1 = Integer.valueOf(times.length > 0 ? times[0] : "0") * 60
 						* 12
 						+ Integer.valueOf(times.length > 1 ? times[1] : "0");
 			} else {
 				String[] times = fromTime.replace("am", "").split(":");
 				from1 = Integer.valueOf(times.length > 0 ? times[0] : "0") * 60
 						+ Integer.valueOf(times.length > 1 ? times[1] : "0");
 			}
 			if (toTime.endsWith("pm")) {
 				String[] times = toTime.replace("pm", "").split(":");
 				to1 = Integer.valueOf(times.length > 0 ? times[0] : "0") * 60
 						* 12
 						+ Integer.valueOf(times.length > 1 ? times[1] : "0");
 			} else {
 				String[] times = toTime.replace("am", "").split(":");
 				to1 = Integer.valueOf(times.length > 0 ? times[0] : "0") * 60
 						+ Integer.valueOf(times.length > 1 ? times[1] : "0");
 			}
 			if (from1 >= to1 || from1 < 0 || to1 >= 24 * 60) {
 				addFieldError("From", "is invalid value.");
 				return SUCCESS;
 			}
 		}
 		tutorialSchedule.setCreateTime(new Date());
 		Date startDate = tutorialSchedule.getStartDate();
 		{
 			Calendar c = Calendar.getInstance();
 			c.setTime(startDate);
 			c.set(Calendar.HOUR_OF_DAY, from1 / 60);
 			c.set(Calendar.MINUTE, from1 % 60);
 			tutorialSchedule.setFromTime(fixTimeZoneInput(c.getTime()));
 		}
 		tutorialSchedule.setModifyTime(new Date());
 		{
 			Calendar c = Calendar.getInstance();
 			c.setTime(startDate);
 			c.set(Calendar.HOUR_OF_DAY, to1 / 60);
 			c.set(Calendar.MINUTE, to1 % 60);
 			tutorialSchedule.setToTime(fixTimeZoneInput(c.getTime()));
 		}
 		{
 			tutorialSchedule.setStartDate(DateUtil.clearTimes(
 					tutorialSchedule.getFromTime()).getTime());
 		}
 		if (tutorialSchedule.getId() == null) {
 			tutorialSchedule.setTutorial(tutorial);
 		}
 		{
 			User user = getUser();
 			List<TutorialSchedule> had = tutorialManager
 					.findTutorTutorialSchedule(user.getId(), startDate,
 							startDate);
 			long newFrom = tutorialSchedule.getFromTime().getTime();
 			long newTo = tutorialSchedule.getToTime().getTime();
 			boolean hasError = false;
 			for (TutorialSchedule ts : had) {
 				long tsFrom = DateUtil
 						.changeToDate(ts.getFromTime(), startDate).getTime();
 				long tsTo = DateUtil.changeToDate(ts.getToTime(), startDate)
 						.getTime();
 				if (!ts.getId().equals(tutorialSchedule.getId())
 						&& (newFrom >= tsFrom && newFrom <= tsTo || newTo >= tsFrom
 								&& newTo <= tsTo)) {
 					super.addActionError("tutorial: name="
 							+ ts.getTutorial().getName() + ", id="
 							+ ts.getTutorial().getId()
 							+ ", tutorial schedule time conflict.");
 					hasError = true;
 				}
 			}
 			if (hasError) {
 				return SUCCESS;
 			}
 		}
 		try {
 			tutorialManager.saveTutorialSchedule(tutorialSchedule);
 		} catch (Exception ade) {
 			ade.printStackTrace();
 			// thrown by UserSecurityAdvice configured in aop:advisor userManagerSecurity
 			log.warn(ade.getMessage());
 			return SUCCESS;
 		}
 		//tutorialSchedule = tutorialManager.getTutorialSchedule(tutorialSchedule.getId());
 		//System.out.println(tutorialSchedule.getStartDate());
 		return SUCCESS;
 	}
 
 	/**
 	 * remove a TutorialSchedule from the tutorial
 	 * @return
 	 * @author <a href="mailto:iffiff1@hotmail.com">Tyler Chen</a> 
 	 * @since 2011-10-18
 	 */
 	public String removeTutorialSchedule() {
 		if (tutorialSchedule.getId() != null) {
 			tutorialManager.removeTutorialSchedule(tutorialSchedule.getId());
 			List<Object> args = new ArrayList<Object>();
 			args.add(tutorialSchedule.toString());
 			saveMessage(getText("TutorialSchedule.deleted", args));
 		}
 		return SUCCESS;
 	}
 
 	/**
 	 * find someone's registered tutorial
 	 * @return
 	 * @author <a href="mailto:iffiff1@hotmail.com">Tyler Chen</a> 
 	 * @since 2011-10-25
 	 */
 	public String findRegisteredTutorial() {
 		User user = getUser();
 		if (user != null) {
 			tutorials = tutorialManager.findTutorialsByUserId(user.getId());
 		}
 		return SUCCESS;
 	}
 
 	/**
 	 * find someone's registered tutorial schedule
 	 * @return
 	 * @author <a href="mailto:iffiff1@hotmail.com">Tyler Chen</a> 
 	 * @since 2011-10-25
 	 */
 	public String findRegisteredTutorialSchedule() {
 		User user = getUser();
 		int totalCost = 0;
 		if (user != null) {
 			List<TutorialScheduleStudent> find = tutorialManager
 					.findTutorialSchedulesByUserId(tutorial.getId(), user
 							.getId());
 			tutorialSchedules = new ArrayList<TutorialSchedule>();
 			for (TutorialScheduleStudent tss : find) {
 				TutorialSchedule clone = tss.getTutorialSchedule().clone();
 				clone.setFromTime(DateUtil.changeToDate(clone.getFromTime(),
 						tss.getId().getLectureDate()));
 				clone.setToTime(DateUtil.changeToDate(clone.getToTime(), tss
 						.getId().getLectureDate()));
 				tutorialSchedules.add(clone);
 				if (clone.getTutorial().getType() == Tutorial.TYPE_WORKSHOP) {
 					totalCost += clone.getTutorial().getCost();
 				} else {
 					totalCost += clone.getCost();
 				}
 			}
 		}
 		{
 			getRequest().setAttribute("totalCost", totalCost);
 		}
 		return SUCCESS;
 	}
 
 	/**
 	 * search tutorials
 	 * @return
 	 * @author <a href="mailto:iffiff1@hotmail.com">Tyler Chen</a> 
 	 * @since 2011-10-22
 	 */
 	public String findTutorials() {
 		String name = null, tutorName = null, sortBy = null;
 		Integer category = null;
 		Date start = null, end = null;
 		{
 			name = getRequest().getParameter("search.name");
 			name = name == null ? null : name.trim();
 		}
 		{
 			String startStr = getRequest().getParameter("search.start");
 			if (startStr != null && (startStr.trim()).length() > 0) {
 				try {
 					start = DateUtil.convertStringToDate(startStr);
 				} catch (Exception e) {
 					addFieldError("start date", "is invalid value(MM/dd/yyyy).");
 				}
 			}
 		}
 		{
 			String endStr = getRequest().getParameter("search.end");
 			if (endStr != null && (endStr.trim()).length() > 0) {
 				try {
 					end = DateUtil.convertStringToDate(endStr);
 				} catch (Exception e) {
 					addFieldError("end date", "is invalid value(MM/dd/yyyy).");
 				}
 			}
 		}
 		{
 			tutorName = getRequest().getParameter("search.tutorName");
 			tutorName = tutorName == null ? null : tutorName.trim();
 		}
 		{
 			sortBy = getRequest().getParameter("search.sortBy");
 			sortBy = sortBy == null ? null : sortBy.trim();
 		}
 		{
 			String categoryStr = getRequest().getParameter("search.category");
 			try {
 				category = Integer.valueOf(categoryStr.trim());
 			} catch (Exception e) {
 			}
 		}
 		tutorials = tutorialManager.findTutorials(id, name, start, end,
 				tutorName, category, sortBy);
 		return SUCCESS;
 	}
 
 	/**
 	 * view a tutorial for booking
 	 * @return
 	 * @author <a href="mailto:iffiff1@hotmail.com">Tyler Chen</a> 
 	 * @since 2011-10-22
 	 */
 	public String viewTutorial() {
 		tutorialSchedules = new ArrayList<TutorialSchedule>();
 		if (tutorial == null) {
 			return SUCCESS;
 		}
 		Date start = null, end = null;
 		{
 			String startStr = getRequest().getParameter("search.start");
 			if (startStr != null && (startStr.trim()).length() > 0) {
 				try {
 					start = DateUtil.convertStringToDate(startStr);
 				} catch (Exception e) {
 					addFieldError("start date", "is invalid value(MM/dd/yyyy).");
 				}
 			}
 		}
 		{
 			String endStr = getRequest().getParameter("search.end");
 			if (endStr != null && (endStr.trim()).length() > 0) {
 				try {
 					end = DateUtil.convertStringToDate(endStr);
 				} catch (Exception e) {
 					addFieldError("end date", "is invalid value(MM/dd/yyyy).");
 				}
 			}
 		}
 		start = start == null ? end == null ? new Date() : end : start;
 		{
 			getRequest().setAttribute("startDate", start);
 		}
 		for (TutorialSchedule ts : tutorial.getTutorialSchedules()) {
 			if (DateUtil.isInDate(ts.getStartDate(), start, ts
 					.getDurationType(), ts.getEndsOccurrence())) {
 				tutorialSchedules.add(ts);
 			}
 		}
 		return SUCCESS;
 	}
 
 	/**
 	 * booking class for register
 	 * @return
 	 * @author <a href="mailto:iffiff1@hotmail.com">Tyler Chen</a> 
 	 * @since 2011-10-25
 	 */
 	public String bookTutorial() {
 		tutorialSchedules = new ArrayList<TutorialSchedule>();
 		String ids = getRequest().getParameter("book.ids");
 		String dates = getRequest().getParameter("book.dates");
 		if (ids == null || dates == null) {
 			return SUCCESS;
 		}
 		String[] idArr = ids.split(",");
 		String[] dateArr = dates.split(",");
 		if (idArr.length < 1 || idArr.length != dateArr.length) {
 			return SUCCESS;
 		}
 		int totalCost = 0;
 		if (tutorial.getType() == Tutorial.TYPE_WORKSHOP) {
 			Map<String, Integer> idMap = new HashMap<String, Integer>();
 			{
 				for (int i = 0; i < idArr.length; i++) {
 					idMap.put(idArr[i], i);
 				}
 			}
 			for (TutorialSchedule ts : tutorial.getTutorialSchedules()) {
 				if (idMap.containsKey(ts.getId().toString())) {
 					try {
 						TutorialSchedule clone = ts.clone();
 						Date date = DateUtil.convertStringToDate(dateArr[idMap
 								.get(clone.getId().toString())]);
 						clone.setFromTime(DateUtil.changeToDate(clone
 								.getFromTime(), date));
 						clone.setToTime(DateUtil.changeToDate(
 								clone.getToTime(), date));
 						tutorialSchedules.add(clone);
 						totalCost += ts.getTutorial().getCost();
 					} catch (Exception e) {
 					}
 				}
 			}
 		} else if (tutorial.getType() == Tutorial.TYPE_CLASS) {
 			Map<Long, TutorialSchedule> tsMap = new HashMap<Long, TutorialSchedule>();
 			{
 				for (TutorialSchedule ts : tutorial.getTutorialSchedules()) {
 					tsMap.put(ts.getId(), ts);
 				}
 			}
 			for (int i = 0; i < idArr.length; i++) {
 				try {
 					Long tsId = Long.valueOf(idArr[i].trim());
 					TutorialSchedule ts = tsMap.get(tsId);
 					if (ts == null) {
 						continue;
 					}
 					TutorialSchedule clone = ts.clone();
 					Date date = DateUtil.convertStringToDate(dateArr[i]);
 					clone.setFromTime(DateUtil.changeToDate(
 							clone.getFromTime(), date));
 					clone.setToTime(DateUtil.changeToDate(clone.getToTime(),
 							date));
 					tutorialSchedules.add(clone);
 					totalCost += clone.getCost();
 				} catch (Exception e) {
 				}
 			}
 		}
 		{
 			getRequest().setAttribute("totalCost", totalCost);
 		}
 		return SUCCESS;
 	}
 
 	/**
 	 * register all of selected tutorials
 	 * @return
 	 * @author <a href="mailto:iffiff1@hotmail.com">Tyler Chen</a> 
 	 * @since 2011-10-22
 	 */
 	public String registerTutorial() {
 		String ids = getRequest().getParameter("register.ids");
 		String dates = getRequest().getParameter("register.dates");
 		User user = getUser();
 		List<TutorialScheduleStudentKey> list = new ArrayList<TutorialScheduleStudentKey>();
 		if (user != null && ids != null && dates != null) {
 			String[] idArr = ids.split(",");
 			String[] dateArr = dates.split(",");
 			for (int i = 0; i < idArr.length; i++) {
 				try {
 					TutorialScheduleStudentKey key = new TutorialScheduleStudentKey(
 							Long.valueOf(idArr[i].trim()), user.getId(),
 							DateUtil.convertStringToDate(dateArr[i]));
 					list.add(key);
 				} catch (Exception e) {
 				}
 			}
 			tutorialManager.registerTutorial(tutorial.getId(), list
 					.toArray(new TutorialScheduleStudentKey[list.size()]));
 		}
 		return SUCCESS;
 	}
 
 	/**
 	 * cancel specific tutorial schedule
 	 * @return
 	 * @author <a href="mailto:iffiff1@hotmail.com">Tyler Chen</a> 
 	 * @since 2011-10-25
 	 */
 	public String cancelTutorialSchedule() {
 		User user = getUser();
 		Date date = null;
 		try {
 			date = DateUtil.convertStringToDate(getRequest().getParameter(
 					"scheduleDate"));
 		} catch (Exception e) {
 		}
 		if (user != null && tutorialSchedule != null && date != null) {
 			tutorialManager.cancelTutorialSchedule(tutorialSchedule.getId(),
 					user.getId(), date);
 		}
 		return SUCCESS;
 	}
 
 	/**
 	 * cancel specific tutorial
 	 * @return
 	 * @author <a href="mailto:iffiff1@hotmail.com">Tyler Chen</a> 
 	 * @since 2011-10-25
 	 */
 	public String cancelTutorial() {
 		User user = getUser();
 		if (user != null && tutorial != null) {
 			tutorialManager.cancelTutorial(tutorial.getId(), user.getId());
 		}
 		return SUCCESS;
 	}
 
 	/**
 	 * find specific date tutorial schedule
 	 * @return
 	 * @author <a href="mailto:iffiff1@hotmail.com">Tyler Chen</a> 
 	 * @since 2011-10-31
 	 */
 	public String findDayTutorialSchedule() {
 		Date start = null, end = null;
 		{
 			String startStr = getRequest().getParameter("search.start");
 			if (startStr != null && (startStr.trim()).length() > 0) {
 				try {
 					start = DateUtil.convertStringToDate(startStr);
 					end = DateUtil.getMaxDay(start).getTime();
 				} catch (Exception e) {
 					addFieldError("start date", "is invalid value(MM/dd/yyyy).");
 					return SUCCESS;
 				}
 			}
 		}
 		tutorialSchedules = tutorialManager.findTutorTutorialSchedule(getUser()
 				.getId(), start, end);
 		getHighLightDate(start);
 		return SUCCESS;
 	}
 
 	/**
 	 * find specific week tutorial
 	 * @return
 	 * @author <a href="mailto:iffiff1@hotmail.com">Tyler Chen</a> 
 	 * @since 2011-10-31
 	 */
 	public String findWeekTutorial() {
 		Date start = null, end = null;
 		User user = getUser();
 		{
 			String startStr = getRequest().getParameter("search.start");
 			if (startStr != null && (startStr.trim()).length() > 0) {
 				try {
 					start = DateUtil.convertStringToDate(startStr);
 					start = DateUtil.getSundayDay(start);
 					end = DateUtil.getMaxDay(DateUtil.getSaturdayDay(start))
 							.getTime();
 				} catch (Exception e) {
 					addFieldError("start date", "is invalid value(MM/dd/yyyy).");
 				}
 			}
 		}
 		tutorialSchedules = tutorialManager.findStudentTutorialSchedule(
 				user == null ? null : user.getId(), start, end);
 		getHighLightDate(start);
 		return SUCCESS;
 	}
 
 	/**
 	 * find specific month tutorial
 	 * @return
 	 * @author <a href="mailto:iffiff1@hotmail.com">Tyler Chen</a> 
 	 * @since 2011-10-31
 	 */
 	public String findMonthTutorial() {
 		Date start = null, end = null;
 		User user = getUser();
 		{
 			String startStr = getRequest().getParameter("search.start");
 			if (startStr != null && (startStr.trim()).length() > 0) {
 				try {
 					start = DateUtil.convertStringToDate(startStr);
 					start = DateUtil.getMonthFirstDay(start);
 					end = DateUtil.getMaxDay(DateUtil.getMonthLastDay(start))
 							.getTime();
 				} catch (Exception e) {
 					addFieldError("start date", "is invalid value(MM/dd/yyyy).");
 				}
 			}
 		}
 		tutorialSchedules = tutorialManager.findStudentTutorialSchedule(
 				user == null ? null : user.getId(), start, end);
 		//		Map<Long,Tutorial> map = new LinkedHashMap<Long, Tutorial>();
 		//		for(TutorialSchedule ts:tutorialSchedules){
 		//			map.put(ts.getTutorial().getId(), ts.getTutorial());
 		//		}
 		getHighLightDate(start);
 		return SUCCESS;
 	}
 
 	/**
 	 * get the highLight dates and set to request
 	 * @param start
 	 * @author <a href="mailto:iffiff1@hotmail.com">Tyler Chen</a> 
 	 * @since 2011-11-27
 	 */
 	private void getHighLightDate(Date start) {
 		User user = getUser();
 		Date monthFirst = DateUtil.getMonthFirstDay(start);
 		Date monthLast = DateUtil.getMonthLastDay(start);
 
 		StringBuffer sb = new StringBuffer();
 		Map<String, String> map = new HashMap<String, String>();
 		List<TutorialScheduleStudent> forList = new ArrayList<TutorialScheduleStudent>();
 		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
 		{
 			List<TutorialScheduleStudent> list = tutorialManager
 					.findTutorialSchedulesByStudentIdAndDate(user.getId(),
 							monthFirst, monthLast);
 			for (TutorialScheduleStudent tss : list) {
 				Date lectureDate = tss.getId().getLectureDate();
 				map.put(sdf.format(lectureDate), "");
 				if (DateUtil.isSameDate(tss.getId().getLectureDate(), start)) {
 					forList.add(tss);
 				}
 			}
 		}
 		{
 			List<TutorialSchedule> list = tutorialManager
 					.findTutorTutorialSchedule(user.getId(), monthFirst,
 							monthLast);
 			if (list != null && !list.isEmpty()) {
 				Date[] monthDates = DateUtil.getMonthDays(monthFirst);
 				for (Date date : monthDates) {
 					for (TutorialSchedule ts : list) {
 						map.put(sdf.format(ts.getFromTime()), "");
 						if (DateUtil.isInDate(ts.getStartDate(), date, ts
 								.getDurationType(), ts.getEndsOccurrence())) {
 							TutorialScheduleStudent tss = new TutorialScheduleStudent(
 									ts.getId(), user.getId(), ts.getFromTime(),
 									ts.getFromTime());
 							forList.add(tss);
 						}
 					}
 				}
 			}
 		}
 		{//add all high light date
 			for (String key : map.keySet()) {
 				sb.append(key).append(",");
 			}
 		}
 
 		getRequest().setAttribute("startDate", start);
 		getRequest().setAttribute("tutorialScheduleStudent", forList);
 		getRequest().setAttribute("highLight", sb.toString());
 	}
 
 	public String findCurrentTutorials() {
 		getRequest().setAttribute("isCurrent", true);
 		String name = null;
 		{
 			name = getRequest().getParameter("search.name");
 			name = name == null ? null : name.trim();
 		}
 		tutorials = tutorialManager.findCurrentTutorials(25, 0, name);
 		return SUCCESS;
 	}
 
 	public String findHistoryTutorials() {
 		getRequest().setAttribute("isHistory", true);
 		String name = null;
 		{
 			name = getRequest().getParameter("search.name");
 			name = name == null ? null : name.trim();
 		}
 		tutorials = tutorialManager.findHistoryTutorials(25, 0, name);
 		return SUCCESS;
 	}
 
 	/**
 	 * separated tutorial schedules into {hour:scheduleInfo} 
 	 * @param tutorialSchedules
 	 * @return
 	 * @author <a href="mailto:iffiff1@hotmail.com">Tyler Chen</a> 
 	 * @since 2011-10-31
 	 */
 	public static Map<Integer, List<Map<String, Object>>> processDaySchedule(
 			List<TutorialSchedule> tutorialSchedules,
 			List<TutorialScheduleStudent> tsslist, TimeZone timeZone) {
 		Map<Integer, List<Map<String, Object>>> map = new HashMap<Integer, List<Map<String, Object>>>();
 		if (tutorialSchedules == null || tutorialSchedules.isEmpty()) {
 			return map;
 		}
 		for (int i = 0; i < 12; i++) {
 			map.put(i * 2, new ArrayList<Map<String, Object>>());
 		}
 		for (TutorialSchedule ts : tutorialSchedules) {
 			int fromMinute = DateUtil.getMinute(DateUtil.fixTimeZoneOutput(ts
 					.getFromTime(), timeZone));
 			List<Map<String, Object>> list = map.get((fromMinute / 120) * 2);
 			if (list != null) {
 				if (tsslist != null) {
 					boolean contains = false;
 					for (TutorialScheduleStudent tss : tsslist) {
 						if (tss.getId().getTutorialScheduleId().equals(
 								ts.getId())) {
 							list.add(tutorialScheduleToMap(ts, timeZone, true));
 							contains = true;
 							break;
 						}
 					}
 					if (!contains) {
 						list.add(tutorialScheduleToMap(ts, timeZone, false));
 					}
 				} else {
 					list.add(tutorialScheduleToMap(ts, timeZone, false));
 				}
 			}
 		}
 		return map;
 	}
 
 	/**
 	 * separated tutorial schedules into {weekDate:scheduleInfo}
 	 * @param tutorialSchedules
 	 * @return
 	 * @author <a href="mailto:iffiff1@hotmail.com">Tyler Chen</a> 
 	 * @since 2011-10-31
 	 */
 	public static Map<String, List<Map<String, Object>>> processWeekSchedule(
 			List<TutorialSchedule> tutorialSchedules, TimeZone timeZone) {
 		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
 		Map<String, List<Map<String, Object>>> map = new HashMap<String, List<Map<String, Object>>>();
 		if (tutorialSchedules == null || tutorialSchedules.isEmpty()) {
 			return map;
 		}
 		Date start = DateUtil.getSundayDay(tutorialSchedules.get(0)
 				.getFromTime());
 		Date next = start;
 		for (int i = 0; i < 7; i++) {
 			next = i == 0 ? start : DateUtil.nextDate(next);
 			map.put(sdf.format(next), new ArrayList<Map<String, Object>>());
 		}
 		for (TutorialSchedule ts : tutorialSchedules) {
 			List<Map<String, Object>> list = map.get(sdf.format(ts
 					.getFromTime()));
 			if (list != null) {
 				list.add(tutorialScheduleToMap(ts, timeZone, true));
 			}
 		}
 		return map;
 	}
 
 	/**
 	 * separated tutorial schedules into {dayOfMonth:scheduleInfo}
 	 * @param tutorialSchedules
 	 * @return
 	 * @author <a href="mailto:iffiff1@hotmail.com">Tyler Chen</a> 
 	 * @since 2011-10-31
 	 */
 	public static Map<String, List<Map<String, Object>>> processMonthSchedule(
 			List<TutorialSchedule> tutorialSchedules, TimeZone timeZone) {
 		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
 		Map<String, List<Map<String, Object>>> map = new HashMap<String, List<Map<String, Object>>>();
 		if (tutorialSchedules == null || tutorialSchedules.isEmpty()) {
 			return map;
 		}
 		Date start = DateUtil.getMonthFirstDay(tutorialSchedules.get(0)
 				.getFromTime());
 		String dateStr = sdf.format(start);
 		String month = sdf.format(start).substring(0, 6);
 		Date next = start;
 		while (dateStr.indexOf(month) == 0) {
 			map.put(dateStr, new ArrayList<Map<String, Object>>());
 			next = DateUtil.nextDate(next);
 			dateStr = sdf.format(next);
 		}
 		for (TutorialSchedule ts : tutorialSchedules) {
 			List<Map<String, Object>> list = map.get(sdf.format(ts
 					.getFromTime()));
 			if (list != null) {
 				list.add(tutorialScheduleToMap(ts, timeZone, true));
 			}
 		}
 		return map;
 	}
 
 	/**
 	 * separated tutorial schedules into morning, afternoon, evening
 	 * @param tutorialSchedules
 	 * @return
 	 * @author <a href="mailto:iffiff1@hotmail.com">Tyler Chen</a> 
 	 * @since 2011-10-31
 	 */
 	public static Map<String, List<Map<String, Object>>> processTimeAreaTutorialSchedule(
 			List<TutorialSchedule> tutorialSchedules, TimeZone timeZone) {
 		Map<String, List<Map<String, Object>>> map = new HashMap<String, List<Map<String, Object>>>();
 		{
 			map.put("morning", new ArrayList<Map<String, Object>>());
 			map.put("afternoon", new ArrayList<Map<String, Object>>());
 			map.put("evening", new ArrayList<Map<String, Object>>());
 		}
 		for (TutorialSchedule ts : tutorialSchedules) {
 			int fromMinute = DateUtil.getMinute(ts.getFromTime());
 			if (fromMinute / 60 < 12) {
 				map.get("morning").add(
 						tutorialScheduleToMap(ts, timeZone, false));
 			} else if (fromMinute / 60 < 18) {
 				map.get("afternoon").add(
 						tutorialScheduleToMap(ts, timeZone, false));
 			} else {
 				map.get("evening").add(
 						tutorialScheduleToMap(ts, timeZone, false));
 			}
 		}
 		int maxCount = 0;
 		for (Entry<String, List<Map<String, Object>>> entry : map.entrySet()) {
 			maxCount = Math.max(maxCount, entry.getValue().size());
 		}
 		for (Entry<String, List<Map<String, Object>>> entry : map.entrySet()) {
 			maxCount = Math.max(maxCount, entry.getValue().size());
 			while (entry.getValue().size() < maxCount) {
 				entry.getValue().add(null);
 			}
 		}
 		return map;
 	}
 
 	/**
 	 * tutorial schedule information to map
 	 * @param tutorialSchedule
 	 * @return
 	 * @author <a href="mailto:iffiff1@hotmail.com">Tyler Chen</a> 
 	 * @since 2011-10-31
 	 */
 	private static Map<String, Object> tutorialScheduleToMap(
 			TutorialSchedule tutorialSchedule, TimeZone timeZone,
 			boolean isOwned) {
 		Map<String, Object> m = new HashMap<String, Object>();
 		{
 			Tutorial t = tutorialSchedule.getTutorial();
 			Date fixTimeZoneFrom = DateUtil.fixTimeZoneOutput(tutorialSchedule
 					.getFromTime(), timeZone);
 			Date fixTimeZoneTo = DateUtil.fixTimeZoneOutput(tutorialSchedule
 					.getToTime(), timeZone);
 			int fromMinute = DateUtil.getMinute(fixTimeZoneFrom);
 			int toMinute = DateUtil.getMinute(fixTimeZoneTo);
 			m.put("id", t.getId());
 			m.put("tutorial", t);
 			m.put("name", t.getName());
 			m.put("scheduleId", tutorialSchedule.getId());
 			m.put("fromMinute", fromMinute);
 			m.put("isOwned", isOwned);
 			m.put("toMinute", toMinute);
 			SimpleDateFormat sdf = new SimpleDateFormat("HH:mmaaa",
 					Locale.ENGLISH);
 			m.put("fromTime", sdf.format(fixTimeZoneFrom));
 			m.put("toTime", sdf.format(fixTimeZoneTo));
 			if (t.getType() == Tutorial.TYPE_WORKSHOP) {
 				m.put("cost", t.getCost());
 				m.put("maxParticipate", t.getMaxParticipate());
 			} else {
 				m.put("cost", tutorialSchedule.getCost());
 				m.put("maxParticipate", tutorialSchedule.getMaxParticipate());
 			}
 		}
 		return m;
 	}
 }
