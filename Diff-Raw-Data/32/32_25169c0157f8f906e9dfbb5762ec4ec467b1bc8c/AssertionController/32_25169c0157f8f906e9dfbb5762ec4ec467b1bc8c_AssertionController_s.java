 package com.cockpitconfig.controllers;
 
 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 import java.math.BigInteger;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.regex.Pattern;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import net.sf.json.JSONObject;
 
 import org.apache.ibatis.session.SqlSessionFactory;
 import org.springframework.web.servlet.ModelAndView;
 import org.springframework.web.servlet.mvc.AbstractController;
 
 import com.cockpitconfig.db.AssertionConditionDAO;
 import com.cockpitconfig.db.AssertionGroupDAO;
 import com.cockpitconfig.db.CommunicationViaEmailDAO;
 import com.cockpitconfig.db.NotificationLevelDAO;
 import com.cockpitconfig.db.SourcesDAO;
 import com.cockpitconfig.db.TimeConstraintsDAO;
 import com.cockpitconfig.db.TimeFrameDAO;
 import com.cockpitconfig.objects.AssertionCondition;
 import com.cockpitconfig.objects.AssertionGroup;
 import com.cockpitconfig.objects.CommunicationViaEmail;
 import com.cockpitconfig.objects.NotificationLevel;
 import com.cockpitconfig.objects.Sources;
 import com.cockpitconfig.objects.TimeConstraints;
 import com.cockpitconfig.objects.TimeFrame;
 
 public class AssertionController extends AbstractController {
 
 	private final int INITIAL_VALUE = Integer.MIN_VALUE;
 	private static String REGEX = ":";
 	int lastInsertedGroupIndex = INITIAL_VALUE;
 
 	@Override
 	protected ModelAndView handleRequestInternal(HttpServletRequest request,
 			HttpServletResponse response) throws Exception {
 		ModelAndView mav = new ModelAndView("AssertionScreen", "AssertRules",
				"A"); // TODO: last argument is message
 		handleFormSubmission(request, mav);
 
 		// Following parameters would be received by AJAX Request when user
 		// saves current Assertion Screen
 		String existingRule = request.getParameter("existingRule");
 		String selectedSource = request.getParameter("selectedSource");
 		String TimeFrameIndex = request.getParameter("TimeFrameIndex");
 		String NotificationIndex = request.getParameter("NotificationIndex");
 		String isAreIndex = request.getParameter("isAreIndex");
 		String slopeIndex = request.getParameter("slopeIndex");
 		String ruleIndex = request.getParameter("totalRule");
 		String numberField = request.getParameter("numberValue");
 		String ruleName = request.getParameter("ruleName");
 		String startTime = request.getParameter("startHour");
 		String endTime = request.getParameter("endHour");
 		String communicationVia = request.getParameter("communicationVia");
 		String recipents = request.getParameter("recipents");
 		String source = request.getParameter("source");
 		String stream = request.getParameter("stream");
 		String ruleToDelete = request.getParameter("ruleToDelete");
 
 		String[] selectedDays = request.getParameterValues("selectedDays");
 
 		if (existingRule != null) {
 			// If user selects an existing rule then load the screen
 			updateScreen(existingRule, response);
 			/*
 			 * if (communicationVia != null) { commMedium =
 			 * Integer.parseInt(communicationVia); }
 			 */
 			return mav;
 		}
 
 		if (selectedSource != null) { // To load stream for a given source
 			updateStreams(selectedSource, response);
 			return mav;
 		}
 
 		if (ruleToDelete != null) {
 			deleteRule(ruleToDelete);
 		}
 
 		int ifExists = INITIAL_VALUE;
 		int pkForSourceUrl = INITIAL_VALUE;
 
 		if (ruleName != null && communicationVia != null && source != null) {
 
 			// PK from Source table to insert it into AssertionGroup table
 			pkForSourceUrl = getPKForSourceUrl(source);
 
 			// Whether rule exists or not, if yes then return PK else -1
 			ifExists = getPKForRule(ruleName);
 
 			if (ifExists == -1) {
 				lastInsertedGroupIndex = setAssertionGroupID(ruleName,
 						pkForSourceUrl, communicationVia);
 			} else {
 				// When updated screen is 'saved'
 				lastInsertedGroupIndex = ifExists;
 
 				// Before saving new screen remove all rules from
 				// assertionCondition Table
 				removeExistingRules(lastInsertedGroupIndex);
 
 				// Before saving new screen remove all rules from
 				// timeConstraints Table
 				removeExistingTimeConstraints(lastInsertedGroupIndex);
 
 				// updateMedium(commMedium, lastInsertedGroupIndex);
 				removeExistingMedium(lastInsertedGroupIndex);
 			}
 		}
 
 		// If all parameters are defined insert a row into assertionCondition
 		// Table
 		if (TimeFrameIndex != null && NotificationIndex != null
 				&& isAreIndex != null && slopeIndex != null
 				&& ruleIndex != null && numberField != null && stream != null) {
 			BigInteger value = new BigInteger(numberField);
 			setAssertionCondition(stream, Integer.parseInt(isAreIndex),
 					Integer.parseInt(slopeIndex), value,
 					Integer.parseInt(TimeFrameIndex),
 					Integer.parseInt(NotificationIndex),
 					Integer.parseInt(ruleIndex), lastInsertedGroupIndex);
 		}
 
 		// If all parameters are defined insert a row into timeConstraints Table
 		if (selectedDays != null && selectedDays[0] != "" && startTime != null
 				&& endTime != null) {
 			setTimeConstraints(selectedDays, startTime, endTime,
 					lastInsertedGroupIndex);
 		}
 
 		// If communicationMedium is selected as Email then set email recipnet
 		// in communicationViaEmail table.
 		if (communicationVia != null && Integer.parseInt(communicationVia) == 0) {
 			setEmailRecipent(recipents, lastInsertedGroupIndex);
 		}
 
 		return mav; // return modelandview object
 	}
 
 	/**
 	 * Function which removes row of existing rules from assertionCondition with
 	 * id = grpID when user modifies the rule
 	 * 
 	 * @param grpID
 	 *            ID corresponding to existing rule
 	 */
 	private void removeExistingRules(int grpID) {
 
 		SqlSessionFactory sf = (SqlSessionFactory) getServletContext()
 				.getAttribute("sqlSessionFactory");
 		AssertionConditionDAO assDao = new AssertionConditionDAO(sf);
 
 		assDao.removeRulesWithID(grpID);
 	}
 
 	/**
 	 * Function which removes row of existing rules from timeConstraints with id
 	 * = grpID when user modifies the rule
 	 * 
 	 * @param grpID
 	 *            ID corresponding to existing rule
 	 */
 	private void removeExistingTimeConstraints(int grpID) {
 		SqlSessionFactory sf = (SqlSessionFactory) getServletContext()
 				.getAttribute("sqlSessionFactory");
 		TimeConstraintsDAO tcDao = new TimeConstraintsDAO(sf);
 
 		tcDao.removeTimeConstraintsWithID(grpID);
 	}
 
 	private int getPKForSourceUrl(String source) {
 		SqlSessionFactory sf = (SqlSessionFactory) getServletContext()
 				.getAttribute("sqlSessionFactory");
 
 		SourcesDAO sourceDao = new SourcesDAO(sf);
 
 		return sourceDao.getPKForSource(source);
 	}
 
 	/**
 	 * Function which removes row of existing rules from communicationViaEmail
 	 * with id = grpID when user modifies the rule
 	 * 
 	 * @param grpID
 	 *            ID corresponding to existing rule
 	 */
 	private void removeExistingMedium(int grpID) {
 		SqlSessionFactory sf = (SqlSessionFactory) getServletContext()
 				.getAttribute("sqlSessionFactory");
 		AssertionGroupDAO agDao = new AssertionGroupDAO(sf);
 		ArrayList<AssertionGroup> ag = agDao.getGrpRow(grpID);
 		if (ag.get(0).getCommunicationID() == 0) {
 			CommunicationViaEmailDAO cveDao = new CommunicationViaEmailDAO(sf);
 			cveDao.removeEmailRecipentWithID(grpID);
 		} else {
 			// Remove from Nagios Table
 		}
 	}
 
 	/**
 	 * Function which return primary key from assertion group table for given
 	 * ruleName
 	 * 
 	 * @param ruleName
 	 *            Name of the Rule
 	 * @return primary key of assertiongroup table corresponding to given
 	 *         ruleName
 	 */
 	private int getPKForRule(String ruleName) {
 		SqlSessionFactory sf = (SqlSessionFactory) getServletContext()
 				.getAttribute("sqlSessionFactory");
 		AssertionGroupDAO assertionGDao = new AssertionGroupDAO(sf);
 
 		int pk = assertionGDao.getPKForRule(ruleName);
 		return pk;
 	}
 
 	/**
 	 * Invoked when communication Medium is select as E-Mail. Function inserts a
 	 * row in to communicationViaEmail table with given recipent and
 	 * assertionGroupID
 	 * 
 	 * @param recipents
 	 *            E-Mail id of the user to which application should inform upon
 	 *            violation of constraints
 	 * @param assertionGrpIndex
 	 *            Primary key of assertiongroup table corresponding to current
 	 *            screen
 	 */
 	private void setEmailRecipent(String recipents, int assertionGrpIndex) {
 		CommunicationViaEmail viaEmail = new CommunicationViaEmail();
 		viaEmail.setAssertionGroupID(assertionGrpIndex);
 		viaEmail.setRecipents(recipents);
 
 		SqlSessionFactory sf = (SqlSessionFactory) getServletContext()
 				.getAttribute("sqlSessionFactory");
 		CommunicationViaEmailDAO viaEmailDao = new CommunicationViaEmailDAO(sf);
 		viaEmailDao.addEmailRecipients(viaEmail);
 	}
 
 	/**
 	 * Function which stores Time Constraints for a Rule in the Database.
 	 * 
 	 * @param selectedDays
 	 *            Each element of array corresponds to a day, if day is selected
 	 *            then true otherwise false
 	 * @param startTime
 	 *            start time(HH:MM format) for constraint to start
 	 * @param endTime
 	 *            end time(HH:MM format) for constraint to end
 	 * @param assertionGrpIndex
 	 *            index of this constraint among all constraint set for a rule
 	 */
 	private void setTimeConstraints(String[] selectedDays, String startTime,
 			String endTime, int assertionGrpIndex) {
 
 		TimeConstraints tc = new TimeConstraints();
 		tc.setAssertionGroupID(assertionGrpIndex);
 
 		boolean[] checkedDays = new boolean[8];
 		for (int i = 0; i < checkedDays.length; ++i) {
 			checkedDays[i] = false;
 		}
 
 		for (int i = 0; i < selectedDays.length; ++i) {
 			checkedDays[Integer.parseInt(selectedDays[i])] = true;
 		}
 
 		Pattern timeSplit = Pattern.compile(REGEX);
 		String[] startField = timeSplit.split(startTime);
 		String[] endField = timeSplit.split(endTime);
 		tc.setStartHour(Integer.parseInt(startField[0]));
 		tc.setStartMin(Integer.parseInt(startField[1]));
 		tc.setEndHour(Integer.parseInt(endField[0]));
 		tc.setEndMin(Integer.parseInt(endField[1]));
 
 		SqlSessionFactory sf = (SqlSessionFactory) getServletContext()
 				.getAttribute("sqlSessionFactory");
 		TimeConstraintsDAO tcDao = new TimeConstraintsDAO(sf);
 
 		for (int i = 0; i < checkedDays.length; ++i) {
 			if (checkedDays[i] == true) {
 				switch (i) {
 				case 0:
 					break;
 				case 1:
 					tc.setMonday(1);
 					break;
 				case 2:
 					tc.setTuesday(1);
 					break;
 				case 3:
 					tc.setWednesday(1);
 					break;
 				case 4:
 					tc.setThursday(1);
 					break;
 				case 5:
 					tc.setFriday(1);
 					break;
 				case 6:
 					tc.setSaturday(1);
 					break;
 				case 7:
 					tc.setSunday(1);
 					break;
 				default:
 					break;
 				}
 			}
 		}
 
 		tcDao.setTimeConstraintForRule(tc);
 	}
 
 	/**
 	 * Function whicih returns the ID of the last inserted Rule
 	 * 
 	 * @param ruleName
 	 *            Name of the rule
 	 * @param communicationVia
 	 *            communication Medium
 	 * @return
 	 */
 	private int setAssertionGroupID(String ruleName, int source,
 			String communicationVia) {
 
 		AssertionGroup ag = new AssertionGroup();
 		ag.setConstraintName(ruleName);
 		ag.setSource(source);
 		ag.setCommunicationID(Integer.parseInt(communicationVia));
 
 		SqlSessionFactory sf = (SqlSessionFactory) getServletContext()
 				.getAttribute("sqlSessionFactory");
 		AssertionGroupDAO agDao = new AssertionGroupDAO(sf);
 		agDao.setConstraintName(ag);
 
 		return agDao.getLastInsertedIndex();
 	}
 
 	private void deleteRule(String ruleName) {
 
 		SqlSessionFactory sf = (SqlSessionFactory) getServletContext()
 				.getAttribute("sqlSessionFactory");
 		AssertionGroupDAO agDao = new AssertionGroupDAO(sf);
 		agDao.removeRule(ruleName);
 	}
 
 	/**
 	 * Function which sets all the parameters for a rule
 	 * 
 	 * @param isAreIndex
 	 *            1 = is/are, 2 = has slope
 	 * @param slopeIndex
 	 *            1 = less than, 2 = equal to, 3 = greater than
 	 * @param numberField
 	 *            contains the value of number field
 	 * @param TimeFrameIndex
 	 *            Index of given TimeFrame as described in TIMEFRAME table.
 	 * @param NotificationIndex
 	 *            Index of given Notification as described in NOTIFICATION
 	 *            table.
 	 * @param ruleIndex
 	 *            Index of the row in the rule
 	 * @param groupID
 	 *            Group ID of the rule(as stored in ASSERTIONGROUP table)
 	 */
 	private void setAssertionCondition(String stream, int isAreIndex,
 			int slopeIndex, BigInteger numberField, int TimeFrameIndex,
 			int NotificationIndex, int ruleIndex, int groupID) {
 
 		AssertionCondition ac = new AssertionCondition();
 		ac.setStream(stream);
 
 		if (isAreIndex == 1) {
 			if (slopeIndex == 1) {
 				ac.setMaxVal(numberField);
 				ac.setMaxDelta(null);
 				ac.setMinVal(null);
 				ac.setMinDelta(null);
 			} else if (slopeIndex == 2) {
 				ac.setMaxVal(numberField);
 				ac.setMinVal(numberField);
 				ac.setMaxDelta(null);
 				ac.setMinDelta(null);
 			} else if (slopeIndex == 3) {
 				ac.setMinVal(numberField);
 				ac.setMaxVal(null);
 				ac.setMaxDelta(null);
 				ac.setMinDelta(null);
 			}
 		} else if (isAreIndex == 2) {
 			if (slopeIndex == 1) {
 				ac.setMaxDelta(numberField);
 				ac.setMaxVal(null);
 				ac.setMinVal(null);
 				ac.setMinDelta(null);
 			} else if (slopeIndex == 2) {
 				ac.setMaxDelta(numberField);
 				ac.setMinDelta(numberField);
 				ac.setMaxVal(null);
 				ac.setMinVal(null);
 			} else if (slopeIndex == 3) {
 				ac.setMinDelta(numberField);
 				ac.setMaxVal(null);
 				ac.setMaxDelta(null);
 				ac.setMinVal(null);
 			}
 		}
 
 		ac.setTimeFrameID(TimeFrameIndex);
 		ac.setNotificationID(NotificationIndex);
 		ac.setAssertionIndex(ruleIndex);
 		ac.setAssertionGroupID(groupID);
 
 		SqlSessionFactory sf = (SqlSessionFactory) getServletContext()
 				.getAttribute("sqlSessionFactory");
 		AssertionConditionDAO assertionDao = new AssertionConditionDAO(sf);
 		assertionDao.addNewRule(ac);
 	}
 
 	/**
 	 * Function which sends back values from Server Side to Client side using
 	 * ModelAndView object
 	 * 
 	 * @param request
 	 *            HttpServletRequest Object
 	 * @param mav
 	 *            ModelAndView Object
 	 */
 	private void handleFormSubmission(HttpServletRequest request,
 			ModelAndView mav) {
 
 		SqlSessionFactory sf = (SqlSessionFactory) getServletContext()
 				.getAttribute("sqlSessionFactory");
 
 		AssertionGroupDAO agDao = new AssertionGroupDAO(sf);
 		ArrayList<String> groupName = agDao.getRuleNames();
 		String[] rulesTemp = new String[groupName.size()];
 		for (int i = 0; i < groupName.size(); ++i) {
 			rulesTemp[i] = groupName.get(i);
 		}
 		mav.addObject("ruleNames", rulesTemp);
 
 		SourcesDAO sourcesDao = new SourcesDAO(sf);
 		ArrayList<Sources> sourceList = sourcesDao.getTotalSources();
 		String[] tempSources = new String[sourceList.size()];
 		for (int i = 0; i < sourceList.size(); ++i) {
 			tempSources[i] = sourceList.get(i).getUrl();
 		}
 		mav.addObject("sources", tempSources);
 
 		TimeFrameDAO timeframeDao = new TimeFrameDAO(sf);
 		ArrayList<TimeFrame> timeframeFrames = timeframeDao.getAllTimeFrames();
 		String[] timeFrameTemp = new String[timeframeFrames.size()];
 		for (int i = 0; i < timeframeFrames.size(); ++i) {
 			timeFrameTemp[i] = timeframeFrames.get(i).getFrame();
 		}
 		mav.addObject("frames", timeFrameTemp);
 
 		NotificationLevelDAO notificationlevelDao = new NotificationLevelDAO(sf);
 		ArrayList<NotificationLevel> notificationlevelLevels = notificationlevelDao
 				.getAllNotificationLevels();
 		String[] notificationLevelTemp = new String[notificationlevelLevels
 				.size()];
 		for (int i = 0; i < notificationlevelLevels.size(); ++i) {
 			notificationLevelTemp[i] = notificationlevelLevels.get(i)
 					.getLevel();
 		}
 		mav.addObject("levels", notificationLevelTemp);
 
 	}
 
 	private void updateStreams(String selectedSource,
 			HttpServletResponse response) throws Exception {
 		URL sources = new URL(selectedSource);
 		BufferedReader availableStreams = new BufferedReader(
 				new InputStreamReader(sources.openStream()));
 		String inputStream;
 
 		ArrayList<String> streamList = new ArrayList<String>();
 		ArrayList<String> stepSizeList = new ArrayList<String>();
 		Integer streamCount = 0;
 		while ((inputStream = availableStreams.readLine()) != null) {
 			streamList.add(inputStream.substring(0, inputStream.indexOf(',')));
 			String value = inputStream.substring(inputStream.indexOf(',') + 1);
 			String header = value.substring(0, value.indexOf('|'));
 			String[] headerList = header.split(",");
 			String steps = headerList[2];
 			stepSizeList.add(steps);
 			streamCount++;
 		}
 
 		availableStreams.close();
 		response.setContentType("application/json;charset=UTF-8");
 		response.setHeader("Cache-Control", "no-cache");
 		JSONObject jsonResult = new JSONObject();
 
 		jsonResult.put("streams", streamList);
 		jsonResult.put("stepSizeList", stepSizeList);
 		jsonResult.put("streamCount", streamCount);
 
 		response.getWriter().write(jsonResult.toString());
 		response.getWriter().close();
 	}
 
 	/**
 	 * Function which executes when user selects an existing rule from Database
 	 * 
 	 * @param existingRuleName
 	 *            Name of the rule selected by User
 	 * @param response
 	 *            HttpServlet response object
 	 * @throws Exception
 	 *             Exception thrown
 	 */
 	private void updateScreen(String existingRuleName,
 			HttpServletResponse response) throws Exception {
 		int grpID = getPKForRule(existingRuleName);
 		SqlSessionFactory sf = (SqlSessionFactory) getServletContext()
 				.getAttribute("sqlSessionFactory");
 
 		AssertionConditionDAO acDao = new AssertionConditionDAO(sf);
 
 		// Retrieve values from assertionCondition Table
 		ArrayList<AssertionCondition> ruleInfo = acDao.getRuleRow(grpID);
 		String[] existingStreams = new String[ruleInfo.size()];
 		BigInteger[] existingMinVal = new BigInteger[ruleInfo.size()];
 		BigInteger[] existingMinDelta = new BigInteger[ruleInfo.size()];
 		BigInteger[] existingMaxVal = new BigInteger[ruleInfo.size()];
 		BigInteger[] existingMaxDelta = new BigInteger[ruleInfo.size()];
 		int[] existingTimeFrame = new int[ruleInfo.size()];
 		int[] existingNotification = new int[ruleInfo.size()];
 		int[] existingIndex = new int[ruleInfo.size()];
 
 		for (int i = 0; i < ruleInfo.size(); ++i) {
 			existingStreams[i] = ruleInfo.get(i).getStream();
 			existingMinVal[i] = ruleInfo.get(i).getMinVal();
 			existingMaxVal[i] = ruleInfo.get(i).getMaxVal();
 			existingMinDelta[i] = ruleInfo.get(i).getMinDelta();
 			existingMaxDelta[i] = ruleInfo.get(i).getMaxDelta();
 			existingTimeFrame[i] = ruleInfo.get(i).getTimeFrameID();
 			existingNotification[i] = ruleInfo.get(i).getNotificationID();
 			existingIndex[i] = ruleInfo.get(i).getAssertionIndex();
 		}
 
 		// Retrieve values from TimeConstraint Table
 		TimeConstraintsDAO tcDao = new TimeConstraintsDAO(sf);
 		ArrayList<TimeConstraints> frequencyInfo = tcDao.getfrequencyRow(grpID);
 		int[] monday = new int[frequencyInfo.size()];
 		int[] tuesday = new int[frequencyInfo.size()];
 		int[] wednesday = new int[frequencyInfo.size()];
 		int[] thursday = new int[frequencyInfo.size()];
 		int[] friday = new int[frequencyInfo.size()];
 		int[] saturday = new int[frequencyInfo.size()];
 		int[] sunday = new int[frequencyInfo.size()];
 		int[] startHour = new int[frequencyInfo.size()];
 		int[] endHour = new int[frequencyInfo.size()];
 		int[] startMin = new int[frequencyInfo.size()];
 		int[] endMin = new int[frequencyInfo.size()];
 
 		for (int i = 0; i < frequencyInfo.size(); ++i) {
 			monday[i] = frequencyInfo.get(i).getMonday();
 			tuesday[i] = frequencyInfo.get(i).getTuesday();
 			wednesday[i] = frequencyInfo.get(i).getWednesday();
 			thursday[i] = frequencyInfo.get(i).getThursday();
 			friday[i] = frequencyInfo.get(i).getFriday();
 			saturday[i] = frequencyInfo.get(i).getSaturday();
 			sunday[i] = frequencyInfo.get(i).getSunday();
 			startHour[i] = frequencyInfo.get(i).getStartHour();
 			endHour[i] = frequencyInfo.get(i).getEndHour();
 			startMin[i] = frequencyInfo.get(i).getStartMin();
 			endMin[i] = frequencyInfo.get(i).getEndMin();
 		}
 
 		// Retrieve values from AssertionGroup Table Table
 		AssertionGroupDAO agDao = new AssertionGroupDAO(sf);
 		int communicationMedium = agDao.getCommunicationID(grpID);
 		SourcesDAO src = new SourcesDAO(sf);
 		String existSource = src.getSourceUrlForGivenPK(agDao
 				.getSourceKeyForGivenRule(existingRuleName));
 
 		String recipient;
 
 		response.setContentType("application/json;charset=UTF-8");
 		response.setHeader("Cache-Control", "no-cache");
 		JSONObject jsonResult = new JSONObject();
 
 		jsonResult.put("communicationVia", communicationMedium);
 		jsonResult.put("existSource", existSource);
 
 		// If communication Medium is Email
 		if (communicationMedium == 0) {
 			// Retrieve values from CommunicationViaEmail Table
 			CommunicationViaEmailDAO emailDao = new CommunicationViaEmailDAO(sf);
 			recipient = emailDao.getrecipient(grpID);
 			jsonResult.put("recipent", recipient);
 		}
 
 		// Sending Value to Client as JSON Object
 		jsonResult.put("existingStreams", existingStreams);
 		jsonResult.put("existingMinVal", existingMinVal);
 		jsonResult.put("existingMaxVal", existingMaxVal);
 		jsonResult.put("existingMinDelta", existingMinDelta);
 		jsonResult.put("existingMaxDelta", existingMaxDelta);
 		jsonResult.put("existingTimeFrame", existingTimeFrame);
 		jsonResult.put("existingNotification", existingNotification);
 		jsonResult.put("existingIndex", existingIndex);
 		jsonResult.put("monday", monday);
 		jsonResult.put("tuesday", tuesday);
 		jsonResult.put("wednesday", wednesday);
 		jsonResult.put("thursday", thursday);
 		jsonResult.put("friday", friday);
 		jsonResult.put("saturday", saturday);
 		jsonResult.put("sunday", sunday);
 		jsonResult.put("startHour", startHour);
 		jsonResult.put("endHour", endHour);
 		jsonResult.put("startMin", startMin);
 		jsonResult.put("endMin", endMin);
 
 		response.getWriter().write(jsonResult.toString());
 		response.getWriter().close();
 	}
 }
