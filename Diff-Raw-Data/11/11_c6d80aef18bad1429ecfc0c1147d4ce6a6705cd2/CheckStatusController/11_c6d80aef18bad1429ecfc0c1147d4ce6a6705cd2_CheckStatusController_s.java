 package com.cockpitconfig.controllers;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.math.BigInteger;
 import java.net.URL;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.HashMap;
 
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.ibatis.session.SqlSessionFactory;
 import org.codehaus.jackson.JsonGenerationException;
 import org.codehaus.jackson.map.JsonMappingException;
 import org.codehaus.jackson.map.ObjectMapper;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.servlet.ModelAndView;
 
 import com.cockpitconfig.db.AssertionConditionDAO;
 import com.cockpitconfig.db.AssertionGroupDAO;
 import com.cockpitconfig.db.NotificationOccurrencesDAO;
 import com.cockpitconfig.db.SourcesDAO;
 import com.cockpitconfig.db.TimeConstraintsDAO;
 import com.cockpitconfig.objects.AssertionCondition;
 import com.cockpitconfig.objects.AssertionGroup;
 import com.cockpitconfig.objects.NotificationOccurrence;
 import com.cockpitconfig.objects.TimeConstraints;
 
 @Controller
 @RequestMapping("/checkStatus")
 public class CheckStatusController {
 
	ArrayList<NotificationOccurrence> notificationOccurences = new ArrayList<NotificationOccurrence>();
 
 	public enum TimeFrameEnum {
 		PER_STEP, PER_5_STEP, PER_10_STEP, PER_25_STEP, PER_50_STEP
 	}
 
 	@RequestMapping(method = RequestMethod.GET)
 	public ModelAndView CheckStatus(HttpServletResponse response)
 			throws Exception {
 
 		ModelAndView model = new ModelAndView("checkStatus");
 
 		SqlSessionFactory sf = MyBatisSqlSessionFactory.getSqlSessionFactory();
 		AssertionGroupDAO agDao = new AssertionGroupDAO(sf);
 		ArrayList<AssertionGroup> allRules = agDao.getAllrules();
 
 		for (int i = 0; i < allRules.size(); ++i) {
 			AssertionGroup tempObj = allRules.get(i);
 			checkRule(tempObj.getId(), tempObj.getSource(),
 					tempObj.getConstraintName(), tempObj.getCommunicationID(),
 					sf, response);
 		}
 
 		ObjectMapper mapper = new ObjectMapper();
 		model.addObject("json",
 				mapper.writeValueAsString(notificationOccurences));
		// response.setContentType("application/json;charset=UTF-8");
		// response.setHeader("Cache-Control", "no-cache");
		//
		//
		// response.getWriter().write(
		// mapper.writeValueAsString(notificationOccurences));
 		return model;
 	}
 
 	/**
 	 * Function which monitor each rule
 	 * 
 	 * @param PK
 	 *            Primary Key of assertion group table
 	 * @param sourcePK
 	 *            SourcePK stored in Assertion Group table
 	 * @param sf
 	 * @throws Exception
 	 */
 	private void checkRule(int PK, int sourcePK, String constraintName,
 			int communicationID, SqlSessionFactory sf,
 			HttpServletResponse response) throws Exception {
 
 		AssertionConditionDAO acDao = new AssertionConditionDAO(sf);
 		SourcesDAO sourcesDao = new SourcesDAO(sf);
 		String sourceUrl = sourcesDao.getSourceUrlForGivenPK(sourcePK);
 
 		URL urlToMonitor = new URL(sourceUrl);
 		BufferedReader availableStreams = new BufferedReader(
 				new InputStreamReader(urlToMonitor.openStream()));
 		String inputStream;
 
 		HashMap<String, String> streamValueMap = new HashMap<String, String>();
 
 		while ((inputStream = availableStreams.readLine()) != null) {
 			String key = inputStream.substring(0, inputStream.indexOf(','));
 			String value = inputStream.substring(inputStream.indexOf(',') + 1);
 			streamValueMap.put(key, value);
 		}
 
 		availableStreams.close();
 
 		ArrayList<AssertionCondition> rulesToMonitor = acDao.getRuleRow(PK);
 
 		TimeConstraintsDAO tcDao = new TimeConstraintsDAO(sf);
 		ArrayList<TimeConstraints> constrainedTime = tcDao.getfrequencyRow(PK);
 
 		for (int i = 0; i < rulesToMonitor.size(); ++i) {
 			AssertionCondition temp = rulesToMonitor.get(i);
 			String tempStream = temp.getStream();
 			int timeFrameIndex = temp.getTimeFrameID();
 			int assertionConditionID = temp.getId();
 			int notificationLevelID = temp.getNotificationID();
 			int assertionIndex = temp.getAssertionIndex();
 			String values = streamValueMap.get(tempStream);
 
 			String header = values.substring(0, values.indexOf('|'));
 			String dataValue = values.substring(values.indexOf('|') + 1);
 			String[] valueList = dataValue.split(",");
 
 			// Condition is to check whether "is/are" selected or "has slope"
 			if (temp.getMaxDelta() == null && temp.getMinDelta() == null) {
 				// This condition is to check whether "equal to" is selected
 				if (temp.getMinVal() != null && temp.getMaxVal() != null) {
 					if (timeFrameIndex == TimeFrameEnum.PER_STEP.ordinal()) {
 						int increamentSize = 1;
 						checkAgainstIsAreEqualTo(increamentSize, valueList,
 								temp.getMaxVal(), assertionConditionID,
 								notificationLevelID, constraintName,
 								communicationID, assertionIndex, header,
 								constrainedTime, sf, response);
 					} else if (timeFrameIndex == TimeFrameEnum.PER_5_STEP
 							.ordinal()) {
 						int increamentSize = 5;
 						checkAgainstIsAreEqualTo(increamentSize, valueList,
 								temp.getMaxVal(), assertionConditionID,
 								notificationLevelID, constraintName,
 								communicationID, assertionIndex, header,
 								constrainedTime, sf, response);
 					} else if (timeFrameIndex == TimeFrameEnum.PER_10_STEP
 							.ordinal()) {
 						int increamentSize = 10;
 						checkAgainstIsAreEqualTo(increamentSize, valueList,
 								temp.getMaxVal(), assertionConditionID,
 								notificationLevelID, constraintName,
 								communicationID, assertionIndex, header,
 								constrainedTime, sf, response);
 					} else if (timeFrameIndex == TimeFrameEnum.PER_25_STEP
 							.ordinal()) {
 						int increamentSize = 25;
 						checkAgainstIsAreEqualTo(increamentSize, valueList,
 								temp.getMaxVal(), assertionConditionID,
 								notificationLevelID, constraintName,
 								communicationID, assertionIndex, header,
 								constrainedTime, sf, response);
 					} else if (timeFrameIndex == TimeFrameEnum.PER_50_STEP
 							.ordinal()) {
 						int increamentSize = 50;
 						checkAgainstIsAreEqualTo(increamentSize, valueList,
 								temp.getMaxVal(), assertionConditionID,
 								notificationLevelID, constraintName,
 								communicationID, assertionIndex, header,
 								constrainedTime, sf, response);
 					}
 				} else if (temp.getMinVal() != null) {
 					if (timeFrameIndex == TimeFrameEnum.PER_STEP.ordinal()) {
 						int increamentSize = 1;
 						checkAgainstIsAreGreaterThan(increamentSize, valueList,
 								temp.getMinVal(), assertionConditionID,
 								notificationLevelID, constraintName,
 								communicationID, assertionIndex, header,
 								constrainedTime, sf, response);
 					} else if (timeFrameIndex == TimeFrameEnum.PER_5_STEP
 							.ordinal()) {
 						int increamentSize = 5;
 						checkAgainstIsAreGreaterThan(increamentSize, valueList,
 								temp.getMinVal(), assertionConditionID,
 								notificationLevelID, constraintName,
 								communicationID, assertionIndex, header,
 								constrainedTime, sf, response);
 					} else if (timeFrameIndex == TimeFrameEnum.PER_10_STEP
 							.ordinal()) {
 						int increamentSize = 10;
 						checkAgainstIsAreGreaterThan(increamentSize, valueList,
 								temp.getMinVal(), assertionConditionID,
 								notificationLevelID, constraintName,
 								communicationID, assertionIndex, header,
 								constrainedTime, sf, response);
 					} else if (timeFrameIndex == TimeFrameEnum.PER_25_STEP
 							.ordinal()) {
 						int increamentSize = 25;
 						checkAgainstIsAreGreaterThan(increamentSize, valueList,
 								temp.getMinVal(), assertionConditionID,
 								notificationLevelID, constraintName,
 								communicationID, assertionIndex, header,
 								constrainedTime, sf, response);
 					} else if (timeFrameIndex == TimeFrameEnum.PER_50_STEP
 							.ordinal()) {
 						int increamentSize = 50;
 						checkAgainstIsAreGreaterThan(increamentSize, valueList,
 								temp.getMinVal(), assertionConditionID,
 								notificationLevelID, constraintName,
 								communicationID, assertionIndex, header,
 								constrainedTime, sf, response);
 					}
 				} else {
 					if (timeFrameIndex == TimeFrameEnum.PER_STEP.ordinal()) {
 						int increamentSize = 1;
 						checkAgainstIsAreLessThan(increamentSize, valueList,
 								temp.getMaxVal(), assertionConditionID,
 								notificationLevelID, constraintName,
 								communicationID, assertionIndex, header,
 								constrainedTime, sf, response);
 					} else if (timeFrameIndex == TimeFrameEnum.PER_5_STEP
 							.ordinal()) {
 						int increamentSize = 5;
 						checkAgainstIsAreLessThan(increamentSize, valueList,
 								temp.getMaxVal(), assertionConditionID,
 								notificationLevelID, constraintName,
 								communicationID, assertionIndex, header,
 								constrainedTime, sf, response);
 					} else if (timeFrameIndex == TimeFrameEnum.PER_10_STEP
 							.ordinal()) {
 						int increamentSize = 10;
 						checkAgainstIsAreLessThan(increamentSize, valueList,
 								temp.getMaxVal(), assertionConditionID,
 								notificationLevelID, constraintName,
 								communicationID, assertionIndex, header,
 								constrainedTime, sf, response);
 					} else if (timeFrameIndex == TimeFrameEnum.PER_25_STEP
 							.ordinal()) {
 						int increamentSize = 25;
 						checkAgainstIsAreLessThan(increamentSize, valueList,
 								temp.getMaxVal(), assertionConditionID,
 								notificationLevelID, constraintName,
 								communicationID, assertionIndex, header,
 								constrainedTime, sf, response);
 					} else if (timeFrameIndex == TimeFrameEnum.PER_50_STEP
 							.ordinal()) {
 						int increamentSize = 50;
 						checkAgainstIsAreLessThan(increamentSize, valueList,
 								temp.getMaxVal(), assertionConditionID,
 								notificationLevelID, constraintName,
 								communicationID, assertionIndex, header,
 								constrainedTime, sf, response);
 					}
 				}
 			} else {
 				if (temp.getMinDelta() != null && temp.getMaxDelta() != null) {
 					if (timeFrameIndex == TimeFrameEnum.PER_STEP.ordinal()) {
 						int increamentSize = 1;
 						checkAgainstHasSlopeIsEqualTo(increamentSize,
 								valueList, temp.getMaxDelta(),
 								assertionConditionID, notificationLevelID,
 								constraintName, communicationID,
 								assertionIndex, header, constrainedTime, sf,
 								response);
 					} else if (timeFrameIndex == TimeFrameEnum.PER_5_STEP
 							.ordinal()) {
 						int increamentSize = 5;
 						checkAgainstHasSlopeIsEqualTo(increamentSize,
 								valueList, temp.getMaxDelta(),
 								assertionConditionID, notificationLevelID,
 								constraintName, communicationID,
 								assertionIndex, header, constrainedTime, sf,
 								response);
 					} else if (timeFrameIndex == TimeFrameEnum.PER_10_STEP
 							.ordinal()) {
 						int increamentSize = 10;
 						checkAgainstHasSlopeIsEqualTo(increamentSize,
 								valueList, temp.getMaxDelta(),
 								assertionConditionID, notificationLevelID,
 								constraintName, communicationID,
 								assertionIndex, header, constrainedTime, sf,
 								response);
 					} else if (timeFrameIndex == TimeFrameEnum.PER_25_STEP
 							.ordinal()) {
 						int increamentSize = 25;
 						checkAgainstHasSlopeIsEqualTo(increamentSize,
 								valueList, temp.getMaxDelta(),
 								assertionConditionID, notificationLevelID,
 								constraintName, communicationID,
 								assertionIndex, header, constrainedTime, sf,
 								response);
 					} else if (timeFrameIndex == TimeFrameEnum.PER_50_STEP
 							.ordinal()) {
 						int increamentSize = 50;
 						checkAgainstHasSlopeIsEqualTo(increamentSize,
 								valueList, temp.getMaxDelta(),
 								assertionConditionID, notificationLevelID,
 								constraintName, communicationID,
 								assertionIndex, header, constrainedTime, sf,
 								response);
 					}
 				} else if (temp.getMinDelta() != null) {
 					if (timeFrameIndex == TimeFrameEnum.PER_STEP.ordinal()) {
 						int increamentSize = 1;
 						checkAgainstHasSlopeGreaterThan(increamentSize,
 								valueList, temp.getMinDelta(),
 								assertionConditionID, notificationLevelID,
 								constraintName, communicationID,
 								assertionIndex, header, constrainedTime, sf,
 								response);
 					} else if (timeFrameIndex == TimeFrameEnum.PER_5_STEP
 							.ordinal()) {
 						int increamentSize = 5;
 						checkAgainstHasSlopeGreaterThan(increamentSize,
 								valueList, temp.getMinDelta(),
 								assertionConditionID, notificationLevelID,
 								constraintName, communicationID,
 								assertionIndex, header, constrainedTime, sf,
 								response);
 					} else if (timeFrameIndex == TimeFrameEnum.PER_10_STEP
 							.ordinal()) {
 						int increamentSize = 10;
 						checkAgainstHasSlopeGreaterThan(increamentSize,
 								valueList, temp.getMinDelta(),
 								assertionConditionID, notificationLevelID,
 								constraintName, communicationID,
 								assertionIndex, header, constrainedTime, sf,
 								response);
 					} else if (timeFrameIndex == TimeFrameEnum.PER_25_STEP
 							.ordinal()) {
 						int increamentSize = 25;
 						checkAgainstHasSlopeGreaterThan(increamentSize,
 								valueList, temp.getMinDelta(),
 								assertionConditionID, notificationLevelID,
 								constraintName, communicationID,
 								assertionIndex, header, constrainedTime, sf,
 								response);
 					} else if (timeFrameIndex == TimeFrameEnum.PER_50_STEP
 							.ordinal()) {
 						int increamentSize = 50;
 						checkAgainstHasSlopeGreaterThan(increamentSize,
 								valueList, temp.getMinDelta(),
 								assertionConditionID, notificationLevelID,
 								constraintName, communicationID,
 								assertionIndex, header, constrainedTime, sf,
 								response);
 					}
 				} else {
 					if (timeFrameIndex == TimeFrameEnum.PER_STEP.ordinal()) {
 						int increamentSize = 1;
 						checkAgainstHasSlopeLessThan(increamentSize, valueList,
 								temp.getMaxDelta(), assertionConditionID,
 								notificationLevelID, constraintName,
 								communicationID, assertionIndex, header,
 								constrainedTime, sf, response);
 					} else if (timeFrameIndex == TimeFrameEnum.PER_5_STEP
 							.ordinal()) {
 						int increamentSize = 5;
 						checkAgainstHasSlopeLessThan(increamentSize, valueList,
 								temp.getMaxDelta(), assertionConditionID,
 								notificationLevelID, constraintName,
 								communicationID, assertionIndex, header,
 								constrainedTime, sf, response);
 					} else if (timeFrameIndex == TimeFrameEnum.PER_10_STEP
 							.ordinal()) {
 						int increamentSize = 10;
 						checkAgainstHasSlopeLessThan(increamentSize, valueList,
 								temp.getMaxDelta(), assertionConditionID,
 								notificationLevelID, constraintName,
 								communicationID, assertionIndex, header,
 								constrainedTime, sf, response);
 					} else if (timeFrameIndex == TimeFrameEnum.PER_25_STEP
 							.ordinal()) {
 						int increamentSize = 25;
 						checkAgainstHasSlopeLessThan(increamentSize, valueList,
 								temp.getMaxDelta(), assertionConditionID,
 								notificationLevelID, constraintName,
 								communicationID, assertionIndex, header,
 								constrainedTime, sf, response);
 					} else if (timeFrameIndex == TimeFrameEnum.PER_50_STEP
 							.ordinal()) {
 						int increamentSize = 50;
 						checkAgainstHasSlopeLessThan(increamentSize, valueList,
 								temp.getMaxDelta(), assertionConditionID,
 								notificationLevelID, constraintName,
 								communicationID, assertionIndex, header,
 								constrainedTime, sf, response);
 					}
 				}
 			}
 		}
 	}
 
 	/**
 	 * Function which returns current data or time
 	 * 
 	 * @param dateFormat
 	 * @return String containing current date or time
 	 */
 	public String now(String dateFormat) {
 		Calendar cal = Calendar.getInstance();
 		SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
 		return sdf.format(cal.getTime());
 	}
 
 	/**
 	 * Checks a row of a rule against current value when rule contains "is/are"
 	 * and "equal to". Upon violation, insert a row in NotificationOccurrence
 	 * table
 	 * 
 	 * @param increamentSize
 	 *            describes steps to check against
 	 * @param valueList
 	 *            contains the current value
 	 * @param maxVal
 	 *            value of user supplied data same as minVal in this case
 	 * @param assertionConditionID
 	 *            PK of row in assertionCondition table
 	 * @param notificationLevelID
 	 *            PK of notificationLevel in NotificationLevel table
 	 */
 
 	private void checkAgainstIsAreEqualTo(int increamentSize,
 			String[] valueList, BigInteger maxValue, int assertionConditionID,
 			int notificationLevelID, String constraintName,
 			int communicationID, int assertionIndex, String header,
 			ArrayList<TimeConstraints> constrainedTime, SqlSessionFactory sf,
 			HttpServletResponse response) {
 
 		for (int j = 0; j < valueList.length; j = j + increamentSize) {
 			if (!isDisabled(header, constrainedTime, j)
 					&& valueList[j].equals("None") == false) {
 				String s = valueList[j].substring(0, valueList[j].indexOf("."));
 				BigInteger dataValue = new BigInteger(s);
 				int checkCondition = maxValue.compareTo(dataValue);
 				if (checkCondition == 0) {
 					insertActivity(notificationLevelID, assertionConditionID,
 							constraintName, assertionIndex,
 							(int) Float.parseFloat(valueList[j]), maxValue, sf,
 							1);
 				}
 			}
 		}
 	}
 
 	/**
 	 * Checks a row of a rule against current value when rule contains "is/are"
 	 * and "greater than". Upon violation, insert a row in
 	 * NotificationOccurrence table
 	 * 
 	 * @param increamentSize
 	 *            describes steps to check against
 	 * @param valueList
 	 *            contains the current value
 	 * @param minVal
 	 *            value of user supplied data
 	 * @param assertionConditionID
 	 *            PK of row in assertionCondition table
 	 * @param notificationLevelID
 	 *            PK of notificationLevel in NotificationLevel table
 	 */
 
 	private void checkAgainstIsAreGreaterThan(int increamentSize,
 			String[] valueList, BigInteger minValue, int assertionConditionID,
 			int notificationLevelID, String constraintName,
 			int communicationID, int assertionIndex, String header,
 			ArrayList<TimeConstraints> constrainedTime, SqlSessionFactory sf,
 			HttpServletResponse response) {
 
 		for (int j = 0; j < valueList.length; j = j + increamentSize) {
 			if (!isDisabled(header, constrainedTime, j)
 					&& valueList[j].equals("None") == false) {
 				String s = valueList[j].substring(0, valueList[j].indexOf("."));
 				BigInteger dataValue = new BigInteger(s);
 				int checkCondition = dataValue.compareTo(minValue);
 				if (checkCondition > 0) {
 					insertActivity(notificationLevelID, assertionConditionID,
 							constraintName, assertionIndex,
 							(int) Float.parseFloat(valueList[j]), minValue, sf,
 							2);
 				}
 			}
 		}
 	}
 
 	/**
 	 * Checks a row of a rule against current value when rule contains "is/are"
 	 * and "less than". Upon violation, insert a row in NotificationOccurrence
 	 * table
 	 * 
 	 * @param increamentSize
 	 *            describes steps to check against
 	 * @param valueList
 	 *            contains the current value
 	 * @param maxVal
 	 *            value of user supplied data
 	 * @param assertionConditionID
 	 *            PK of row in assertionCondition table
 	 * @param notificationLevelID
 	 *            PK of notificationLevel in NotificationLevel table
 	 */
 
 	private void checkAgainstIsAreLessThan(int increamentSize,
 			String[] valueList, BigInteger maxValue, int assertionConditionID,
 			int notificationLevelID, String constraintName,
 			int communicationID, int assertionIndex, String header,
 			ArrayList<TimeConstraints> constrainedTime, SqlSessionFactory sf,
 			HttpServletResponse response) {
 
 		for (int j = 0; j < valueList.length; j = j + increamentSize) {
 			if (!isDisabled(header, constrainedTime, j)
 					&& valueList[j].equals("None") == false) {
 				String s = valueList[j].substring(0, valueList[j].indexOf("."));
 				BigInteger dataValue = new BigInteger(s);
 				int checkCondition = maxValue.compareTo(dataValue);
 				if (checkCondition > 0) {
 					insertActivity(notificationLevelID, assertionConditionID,
 							constraintName, assertionIndex,
 							(int) Float.parseFloat(valueList[j]), maxValue, sf,
 							3);
 				}
 			}
 		}
 	}
 
 	/**
 	 * Checks a row of a rule against current value when rule contains
 	 * "has slope" and "is equal to". Upon violation, insert a row in
 	 * NotificationOccurrence table
 	 * 
 	 * @param increamentSize
 	 *            "per step" specified by user
 	 * @param valueList
 	 *            graphite value
 	 * @param maxValue
 	 *            value supplied by user to check against graphite value
 	 * @param assertionConditionID
 	 *            PK of assertionCondition table
 	 * @param notificationLevelID
 	 *            PK of notification table
 	 * @param constraintName
 	 *            name of the rule
 	 * @param communicationID
 	 *            communication medium
 	 * @param assertionIndex
 	 *            index of the constraint
 	 * @param header
 	 *            header of the graphite
 	 * @param constrainedTime
 	 *            "disabled on" value as supplied by user
 	 * @param sf
 	 *            object of SqlsessionFactory
 	 * @param response
 	 *            object of HttpServletResponse
 	 * @throws JsonGenerationException
 	 * @throws JsonMappingException
 	 * @throws IOException
 	 */
 
 	private void checkAgainstHasSlopeIsEqualTo(int increamentSize,
 			String[] valueList, BigInteger maxValue, int assertionConditionID,
 			int notificationLevelID, String constraintName,
 			int communicationID, int assertionIndex, String header,
 			ArrayList<TimeConstraints> constrainedTime, SqlSessionFactory sf,
 			HttpServletResponse response) {
 
 		for (int j = 0; j < valueList.length - increamentSize; j = j
 				+ increamentSize) {
 			if (valueList[j + increamentSize].equals("None") == false
 					&& valueList[j].equals("None") == false) {
 				String s1 = valueList[j + increamentSize].substring(0,
 						valueList[j + increamentSize].indexOf("."));
 				String s0 = valueList[j]
 						.substring(0, valueList[j].indexOf("."));
 				BigInteger dataValue1 = new BigInteger(s1);
 				BigInteger dataValue0 = new BigInteger(s0);
 
 				Integer increamentStep = new Integer(increamentSize);
 				String in = increamentStep.toString();
 				BigInteger parsedStep = new BigInteger(in);
 
 				BigInteger LHS = dataValue1.subtract(dataValue0);
 				BigInteger RHS = maxValue.multiply(parsedStep);
 
 				if (LHS.equals(RHS)) {
 					insertActivity(notificationLevelID, assertionConditionID,
 							constraintName, assertionIndex,
 							(int) Float.parseFloat(valueList[j]), maxValue, sf,
 							4);
 				}
 			}
 		}
 	}
 
 	/**
 	 * Checks a row of a rule against current value when rule contains
 	 * "has slope" and "greater than". Upon violation, insert a row in
 	 * NotificationOccurrence table
 	 * 
 	 * @param increamentSize
 	 *            "per step" specified by user
 	 * @param valueList
 	 *            graphite value
 	 * @param maxValue
 	 *            value supplied by user to check against graphite value
 	 * @param assertionConditionID
 	 *            PK of assertionCondition table
 	 * @param notificationLevelID
 	 *            PK of notification table
 	 * @param constraintName
 	 *            name of the rule
 	 * @param communicationID
 	 *            communication medium
 	 * @param assertionIndex
 	 *            index of the constraint
 	 * @param header
 	 *            header of the graphite
 	 * @param constrainedTime
 	 *            "disabled on" value as supplied by user
 	 * @param sf
 	 *            object of SqlsessionFactory
 	 * @param response
 	 *            object of HttpServletResponse
 	 * @throws JsonGenerationException
 	 * @throws JsonMappingException
 	 * @throws IOException
 	 */
 
 	private void checkAgainstHasSlopeGreaterThan(int increamentSize,
 			String[] valueList, BigInteger minValue, int assertionConditionID,
 			int notificationLevelID, String constraintName,
 			int communicationID, int assertionIndex, String header,
 			ArrayList<TimeConstraints> constrainedTime, SqlSessionFactory sf,
 			HttpServletResponse response) {
 
 		for (int j = 0; j < valueList.length - increamentSize; j = j
 				+ increamentSize) {
 			if (valueList[j + increamentSize].equals("None") == false
 					&& valueList[j].equals("None") == false) {
 				String s1 = valueList[j + increamentSize].substring(0,
 						valueList[j + increamentSize].indexOf("."));
 				String s0 = valueList[j]
 						.substring(0, valueList[j].indexOf("."));
 				BigInteger dataValue1 = new BigInteger(s1);
 				BigInteger dataValue0 = new BigInteger(s0);
 
 				Integer increamentStep = new Integer(increamentSize);
 				String in = increamentStep.toString();
 				BigInteger parsedStep = new BigInteger(in);
 
 				BigInteger LHS = dataValue1.subtract(dataValue0);
 				BigInteger RHS = minValue.multiply(parsedStep);
 
 				if (LHS.compareTo(RHS) > 0) {
 					insertActivity(notificationLevelID, assertionConditionID,
 							constraintName, assertionIndex,
 							(int) Float.parseFloat(valueList[j]), minValue, sf,
 							5);
 				}
 			}
 		}
 	}
 
 	/**
 	 * Checks a row of a rule against current value when rule contains
 	 * "has slope" and "less than". Upon violation, insert a row in
 	 * NotificationOccurrence table
 	 * 
 	 * @param increamentSize
 	 *            "per step" specified by user
 	 * @param valueList
 	 *            graphite value
 	 * @param maxValue
 	 *            value supplied by user to check against graphite value
 	 * @param assertionConditionID
 	 *            PK of assertionCondition table
 	 * @param notificationLevelID
 	 *            PK of notification table
 	 * @param constraintName
 	 *            name of the rule
 	 * @param communicationID
 	 *            communication medium
 	 * @param assertionIndex
 	 *            index of the constraint
 	 * @param header
 	 *            header of the graphite
 	 * @param constrainedTime
 	 *            "disabled on" value as supplied by user
 	 * @param sf
 	 *            object of SqlsessionFactory
 	 * @param response
 	 *            object of HttpServletResponse
 	 * @throws JsonGenerationException
 	 * @throws JsonMappingException
 	 * @throws IOException
 	 */
 
 	private void checkAgainstHasSlopeLessThan(int increamentSize,
 			String[] valueList, BigInteger maxValue, int assertionConditionID,
 			int notificationLevelID, String constraintName,
 			int communicationID, int assertionIndex, String header,
 			ArrayList<TimeConstraints> constrainedTime, SqlSessionFactory sf,
 			HttpServletResponse response) {
 
 		for (int j = 0; j < valueList.length - increamentSize; j = j
 				+ increamentSize) {
 			if (valueList[j + increamentSize].equals("None") == false
 					&& valueList[j].equals("None") == false) {
 				String s1 = valueList[j + increamentSize].substring(0,
 						valueList[j + increamentSize].indexOf("."));
 				String s0 = valueList[j]
 						.substring(0, valueList[j].indexOf("."));
 				BigInteger dataValue1 = new BigInteger(s1);
 				BigInteger dataValue0 = new BigInteger(s0);
 
 				Integer increamentStep = new Integer(increamentSize);
 				String in = increamentStep.toString();
 				BigInteger parsedStep = new BigInteger(in);
 
 				BigInteger LHS = dataValue1.subtract(dataValue0);
 				BigInteger RHS = maxValue.multiply(parsedStep);
 
 				if (RHS.compareTo(LHS) > 0) {
 					insertActivity(notificationLevelID, assertionConditionID,
 							constraintName, assertionIndex,
 							(int) Float.parseFloat(valueList[j]), maxValue, sf,
 							6);
 				}
 			}
 		}
 	}
 
 	/**
 	 * Function which insert row and adds NotificationOccurrence object so as to
 	 * return JSON as a response
 	 * 
 	 * @param notificationLevelID
 	 *            PK of notification table
 	 * @param assertionConditionID
 	 *            PK of assertionCondition table
 	 * @param constraintName
 	 *            name of the rule
 	 * @param assertionIndex
 	 *            index of the constraint
 	 * @param value
 	 *            graphite value
 	 * @param maxValue
 	 *            value supplied by user to check against graphite value
 	 * @param sf
 	 *            object of SqlsessionFactory
 	 * @param descriptionIndex
 	 *            describes the rule condition i.e. isAre/has slope and equal
 	 *            to/greater than/less than to set the description field of
 	 *            notificationOccurrence object
 	 */
 	private void insertActivity(int notificationLevelID,
 			int assertionConditionID, String constraintName,
 			int assertionIndex, int value, BigInteger maxValue,
 			SqlSessionFactory sf, int descriptionIndex) {
 
 		String curentTime = now("H:mm:ss");
 		String currentDate = now("yy/MM/dd");
 		String alertType = new String();
 		switch (notificationLevelID) {
 		case 0:
 			alertType = "INFO";
 			break;
 		case 1:
 			alertType = "WARNING";
 			break;
 		case 2:
 			alertType = "ALERT";
 			break;
 		default:
 			break;
 		}
 
 		NotificationOccurrence notiOccurrence = new NotificationOccurrence();
 		notiOccurrence.setAssertionConditionID(assertionConditionID);
 		notiOccurrence.setTimeOccur(curentTime);
 		notiOccurrence.setDateOccur(currentDate);
 		notiOccurrence.setType(alertType);
 		switch (descriptionIndex) {
 		case 1:
 			notiOccurrence.setDescription("[" + constraintName
 					+ "] | Stream index " + assertionIndex + " (" + value
 					+ ") = " + maxValue);
 			break;
 		case 2:
 			notiOccurrence.setDescription("[" + constraintName
 					+ "] | Stream index " + assertionIndex + " (" + value
 					+ ") > " + maxValue);
 			break;
 		case 3:
 			notiOccurrence.setDescription("[" + constraintName
 					+ "] | Stream index " + assertionIndex + " (" + value
 					+ ") < " + maxValue);
 			break;
 		case 4:
 			notiOccurrence.setDescription("[" + constraintName
 					+ "] | Stream index " + assertionIndex + " (" + value
 					+ ") = " + maxValue);
 			break;
 		case 5:
 			notiOccurrence.setDescription("[" + constraintName
 					+ "] | Stream index " + assertionIndex + " (" + value
 					+ ") > " + maxValue);
 			break;
 		case 6:
 			notiOccurrence.setDescription("[" + constraintName
 					+ "] | Stream index " + assertionIndex + " (" + value
 					+ ") < " + maxValue);
 			break;
 		default:
 			break;
 		}
 
 		NotificationOccurrencesDAO notiOccDao = new NotificationOccurrencesDAO(
 				sf);
 		notiOccDao.setRow(notiOccurrence);
 
 		notificationOccurences.add(notiOccurrence);
 	}
 
 	/**
 	 * Function to check rules against time constraint specified by users.
 	 * 
 	 * @param header
 	 * @param constrainedTime
 	 * @param index
 	 * @return
 	 */
 	private boolean isDisabled(String header,
 			ArrayList<TimeConstraints> constrainedTime, int index) {
 		String[] headerList = header.split(",");
 		String startTime = headerList[0];
 		// String endTime = headerList[1];
 		String stepSize = headerList[2];
 
 		Date constrinedDate = new Date(Long.parseLong(startTime + index
 				* Integer.parseInt(stepSize)) * 1000);
 		Calendar cal = Calendar.getInstance();
 		cal.setTime(constrinedDate);
 
 		int day = cal.get(Calendar.DAY_OF_WEEK);
 
 		ArrayList<String> constraindDay = getDayTimes(constrainedTime, day);
 
 		String HH = new SimpleDateFormat("HH").format(constrinedDate);
 		String MM = new SimpleDateFormat("mm").format(constrinedDate);
 
 		for (int i = 0; i < constraindDay.size(); i = i + 2) {
 			if (constraindDay.get(i) != null) {
 				if (checkForTime(HH, MM, constraindDay.get(i),
 						constraindDay.get(i + 1)) == true) {
 					return true;
 				}
 			}
 		}
 
 		return false;
 	}
 
 	private boolean checkForTime(String epochStartTimes, String epochEndTimes,
 			String constrainedStartTimes, String constrainedEndTimes) {
 		String[] constrainedStart = constrainedStartTimes.split(":");
 		String[] constrainedEnd = constrainedEndTimes.split(":");
 
 		int epochTimeHH = Integer.parseInt(epochStartTimes);
 		int epochTimeMM = Integer.parseInt(epochEndTimes);
 		int constrainedStartTimeHH = Integer.parseInt(constrainedStart[0]);
 		int constrainedStartTimeMM = Integer.parseInt(constrainedStart[1]);
 		int constrainedEndTimeHH = Integer.parseInt(constrainedEnd[0]);
 		int constrainedEndTimeMM = Integer.parseInt(constrainedEnd[1]);
 
 		if (epochTimeHH < constrainedStartTimeHH) {
 			return false;
 		} else if (epochTimeHH > constrainedEndTimeHH) {
 			return false;
 		} else {
 			if (epochTimeHH == constrainedStartTimeHH
 					&& epochTimeMM < constrainedStartTimeMM) {
 				return false;
 			} else if (epochTimeHH == constrainedEndTimeHH
 					&& epochTimeMM > constrainedEndTimeMM) {
 				return false;
 			} else {
 				return true;
 			}
 		}
 	}
 
 	/**
 	 * Function to check whether rule is enabled or disabled on a particular day
 	 * 
 	 * @param constraintTime
 	 *            timeConstraint list
 	 * @param day
 	 *            Day as calculated by epoch time. graphite day
 	 * @return list of string containing 2 entries for everyday if day is enable
 	 *         then first entry represents start timing and second entry
 	 *         represents end timing else null in both entries
 	 */
 	private ArrayList<String> getDayTimes(
 			ArrayList<TimeConstraints> constraintTime, int day) {
 		ArrayList<String> dayTime = new ArrayList<String>();
 
 		switch (day) {
 		case 0:
 			for (int i = 0; i < constraintTime.size(); ++i) {
 				TimeConstraints temp = constraintTime.get(i);
 				if (temp.getSunday() == 1) {
 					int hhStart = temp.getStartHour();
 					int mmStart = temp.getStartMin();
 					int hhEnd = temp.getEndHour();
 					int mmEnd = temp.getEndMin();
 					dayTime.add(hhStart + ":" + mmStart);
 					dayTime.add(hhEnd + ":" + mmEnd);
 				} else {
 					dayTime.add(null);
 					dayTime.add(null);
 				}
 			}
 			break;
 		case 1:
 			for (int i = 0; i < constraintTime.size(); ++i) {
 				TimeConstraints temp = constraintTime.get(i);
 				if (temp.getMonday() == 1) {
 					int hhStart = temp.getStartHour();
 					int mmStart = temp.getStartMin();
 					int hhEnd = temp.getEndHour();
 					int mmEnd = temp.getEndMin();
 					dayTime.add(hhStart + ":" + mmStart);
 					dayTime.add(hhEnd + ":" + mmEnd);
 				} else {
 					dayTime.add(null);
 					dayTime.add(null);
 				}
 			}
 			break;
 		case 2:
 			for (int i = 0; i < constraintTime.size(); ++i) {
 				TimeConstraints temp = constraintTime.get(i);
 				if (temp.getTuesday() == 1) {
 					int hhStart = temp.getStartHour();
 					int mmStart = temp.getStartMin();
 					int hhEnd = temp.getEndHour();
 					int mmEnd = temp.getEndMin();
 					dayTime.add(hhStart + ":" + mmStart);
 					dayTime.add(hhEnd + ":" + mmEnd);
 				} else {
 					dayTime.add(null);
 					dayTime.add(null);
 				}
 			}
 			break;
 		case 3:
 			for (int i = 0; i < constraintTime.size(); ++i) {
 				TimeConstraints temp = constraintTime.get(i);
 				if (temp.getWednesday() == 1) {
 					int hhStart = temp.getStartHour();
 					int mmStart = temp.getStartMin();
 					int hhEnd = temp.getEndHour();
 					int mmEnd = temp.getEndMin();
 					dayTime.add(hhStart + ":" + mmStart);
 					dayTime.add(hhEnd + ":" + mmEnd);
 				} else {
 					dayTime.add(null);
 					dayTime.add(null);
 				}
 			}
 			break;
 		case 4:
 			for (int i = 0; i < constraintTime.size(); ++i) {
 				TimeConstraints temp = constraintTime.get(i);
 				if (temp.getThursday() == 1) {
 					int hhStart = temp.getStartHour();
 					int mmStart = temp.getStartMin();
 					int hhEnd = temp.getEndHour();
 					int mmEnd = temp.getEndMin();
 					dayTime.add(hhStart + ":" + mmStart);
 					dayTime.add(hhEnd + ":" + mmEnd);
 				} else {
 					dayTime.add(null);
 					dayTime.add(null);
 				}
 			}
 			break;
 		case 5:
 			for (int i = 0; i < constraintTime.size(); ++i) {
 				TimeConstraints temp = constraintTime.get(i);
 				if (temp.getFriday() == 1) {
 					int hhStart = temp.getStartHour();
 					int mmStart = temp.getStartMin();
 					int hhEnd = temp.getEndHour();
 					int mmEnd = temp.getEndMin();
 					dayTime.add(hhStart + ":" + mmStart);
 					dayTime.add(hhEnd + ":" + mmEnd);
 				} else {
 					dayTime.add(null);
 					dayTime.add(null);
 				}
 			}
 			break;
 		case 6:
 			for (int i = 0; i < constraintTime.size(); ++i) {
 				TimeConstraints temp = constraintTime.get(i);
 				if (temp.getSaturday() == 1) {
 					int hhStart = temp.getStartHour();
 					int mmStart = temp.getStartMin();
 					int hhEnd = temp.getEndHour();
 					int mmEnd = temp.getEndMin();
 					dayTime.add(hhStart + ":" + mmStart);
 					dayTime.add(hhEnd + ":" + mmEnd);
 				} else {
 					dayTime.add(null);
 					dayTime.add(null);
 				}
 			}
 			break;
 
 		default:
 			break;
 		}
 
 		return dayTime;
 	}
 
 }
