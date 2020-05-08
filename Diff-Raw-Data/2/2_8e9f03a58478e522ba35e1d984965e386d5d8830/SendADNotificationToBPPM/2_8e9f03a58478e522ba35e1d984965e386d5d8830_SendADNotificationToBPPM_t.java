 /**
  * Copyright 2013 AppDynamics
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.appdynamics.bmc.bppmClient;
 /**
  *  This class will be used to relay AppDynamics notifications to the 
  *  BMC ProactiveNet Performance Management (BPPM) Server via the the
  *  BMC Impact Manager Web Services interface.
  *
  * Copyright (c) AppDynamics, Inc.
  * @author Pranta Das
  * Created on: December 9, 2011.
  * Updated on: April 10, 2012 for 3.4 support.
  * Updated on: October 15, 2013 for 3.7 support.
  */
 
 import com.appdynamics.common.*;
 import com.appdynamics.bmc.bppmClient.enums.*;
 import com.appdynamics.bmc.bppmClient.stubs.Execution_Fault;
 import com.appdynamics.bmc.bppmClient.stubs.ImpactManagerStub;
 import com.appdynamics.bmc.bppmClient.stubs.ImpactManagerStub.NameValue;
 
 import org.apache.axis2.AxisFault;
 import org.apache.log4j.Logger;
 import org.apache.log4j.MDC;
 import java.lang.management.ManagementFactory;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.net.InetAddress;
 import java.net.UnknownHostException;
 import java.rmi.RemoteException;
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Date;
 import java.util.Properties;
 
 public class SendADNotificationToBPPM implements NotificationParameters 
 {
 
     static {
         MDC.put("PID", ManagementFactory.getRuntimeMXBean().getName());
    }
 
 	private static Logger logger = 
 				Logger.getLogger(
 			com.appdynamics.bmc.bppmClient.SendADNotificationToBPPM.class);
 
 	private static final String BMC_SOURCE_MACHINE_HOST = "mc_host";
 	private static final String BMC_SOURCE_MACHINE_HOST_ADDRESS = 
 															"mc_host_address";
 	private static final String BMC_SOURCE_MACHINE_ORIGIN_CLASS =
 														   "mc_origin_class";
 	private static final String BMC_SOURCE_MACHINE_COMPONENT_CAPTION = 
 														   "mc_object";
 	
 	private static final String BMC_TOOL_RULE = "mc_tool_rule";
 	private static final String BMC_TOOL_KEY = "mc_tool_key";
 	private static final String BMC_TOOL_ID = "mc_tool_id";
 	private static final String BMC_OBJECT_URI = "mc_object_uri";
 	
 	private static final String BMC_SITUATION_MESSAGE = "msg";
 	private static final String BMC_INCIDENT_TIME = "mc_incident_time";
 	private static final String BMC_METRIC_NAME = "mc_parameter";
 	private static final String BMC_METRIC_VALUE = "mc_parameter_value";
 	private static final String BMC_METRIC_THRESHOLD = "mc_parameter_threshold";
 	private static final String BMC_APPLICATION_NAME = "ad_application_name";
 	private static final String BMC_APPLICATION_ID = "ad_application_id";
 	private static final String BMC_TAG = "ad_tag";
 	private static final String BMC_PRIORITY = "mc_priority";
 	private static final String BMC_SEVERITY = "severity";
 
 
 	private static final String BMC_PERIOD_IN_MINUTES ="ad_period_in_minutes";
 	 
 	private static final String BMC_AFFECTED_ENTITY_TYPE =
 											"ad_affected_entity_type";
 	private static final String BMC_AFFECTED_ENTITY_NAME = 
 											"ad_affected_entity_name";
 	private static final String BMC_AFFECTED_ENTITY_ID = 
 											"ad_affected_entity_id";
 	private static final String BMC_EVALUATION_ENTITY_TYPE =
 												"ad_evaluation_entity_type";
 	private static final String BMC_EVALUATION_ENTITY_NAME = 
 												"ad_evaluation_entity_name";
 	private static final String BMC_EVALUATION_ENTITY_ID = 
 												"ad_evaluation_entity_id";
 
 	private static final String BMC_SCOPE_TYPE ="ad_scope_type";
 	private static final String BMC_SCOPE_NAME = "ad_scope_name";
 	private static final String BMC_SCOPE_ID = "ad_scope_id";
 	
 	
 	private static final String BMC_METRIC_ID = "ad_parameter_id";
 	private static final String BMC_OPERATOR = "ad_operator";
 	private static final String BMC_CONDITION_UNIT_TYPE = 
 												"ad_condition_unit_type";
 	private static final String BMC_USE_DEFAULT_BASELINE = 
 													"ad_use_default_baseline";
 	private static final String BMC_BASELINE_NAME = "ad_baseline_name";
 	private static final String BMC_BASELINE_ID = "ad_baseline_id";
 	
 
 	private static final String BMC_EVENT_SUMMARY_ID = "ad_event_summary_id";
 	private static final String BMC_EVENT_SUMMARY_TIME ="ad_event_summary_time";
 	private static final String BMC_EVENT_SUMMARY_TYPE ="ad_event_summary_type";
 	private static final String BMC_EVENT_SEVERITY="ad_event_severity";
 	private static final String BMC_EVENT_TYPE = "ad_event_types";
 	private static final String BMC_NUMBER_OF_EVENTS_FOR_EVENT_TYPE = 
 							"ad_number_of_events_for_event_type";
 
 	private static final String BMC_POLICY_EVENT_TYPE = "ad_policy_event_type";
 	
 	/**
 	 * Parameter data types
 	 */
 	 static final String STRING = "STRING";
 	 static final String INT = "INT";
 	 static final String LONG = "LONG";
 	 static final String STRING_ARRAY = "STRING_ARRAY";
 	 static final String INT_ARRAY = "INT_ARRAY";
 	 static final String LONG_ARRAY = "LONG_ARRAY";
 
 		private static final String 	
 			bppmClientHome = System.getProperty("BPPM_CLIENT_HOME"),
 			fileName = bppmClientHome+"/conf/bppmClient.properties";
 		private static String userName = "admin", password = "admin";
 		private static final String BANNER = "***************************"+
 					"*****************************************************";				
 	 static String hostAddr;
 	 static String hostName;
 
 	 static 
 	 {
 		 try
 		 {
 			 InetAddress addr = InetAddress.getLocalHost();
 			 hostAddr = addr.getHostAddress();
 			 hostName = addr.getHostName();
 		 } 
 		 catch (UnknownHostException e) 
 		 {
 			 System.out.println("Unable to get hostname");
 			 e.printStackTrace();
 		 }
 	 }
 	 /**
 	  * Mandatory BMC attributes
 	  */
 	  static final String[][] mandatoryBMCEventAttributes =
 	 	 {
 	 			{BMC_SOURCE_MACHINE_HOST, STRING, hostName},
 	 			{BMC_SOURCE_MACHINE_HOST_ADDRESS, STRING, hostAddr},
 	 			{BMC_SOURCE_MACHINE_ORIGIN_CLASS, STRING, 
 	 										"AD2BPPM-INTEGRATION-CLIENT"},
 	 			{BMC_SOURCE_MACHINE_COMPONENT_CAPTION,STRING,
 	 										hostName+":IncidentNotification"}
 	 	 };
 
 
 	 static final String[][] pvnParmsAndTypes = 
 	{		
 	 /*[0]*/{APPLICATION_NAME, STRING, BMC_APPLICATION_NAME},
 	 /*[1]*/{APPLICATION_ID, LONG, BMC_APPLICATION_ID},
 	 /*[2]*/{PVN_ALERT_TIME, INT, BMC_INCIDENT_TIME},
 	 /*[3]*/{PRIORITY, INT, BMC_PRIORITY},
 	 /*[4]*/{SEVERITY, INT, BMC_SEVERITY},
 	 /*[5]*/{TAG, STRING, BMC_TAG},
 	 /*[6]*/{PVN_RULE_NAME, STRING, BMC_TOOL_RULE},
 	 /*[7]*/{PVN_RULE_ID, LONG, BMC_TOOL_KEY},
 	 /*[8]*/{PVN_TIME_PERIOD_IN_MINUTES, INT, BMC_PERIOD_IN_MINUTES},
 	 /*[9]*/{PVN_AFFECTED_ENTITY_TYPE, STRING, BMC_AFFECTED_ENTITY_TYPE},
 	 /*[10]*/{PVN_AFFECTED_ENTITY_NAME, STRING, BMC_AFFECTED_ENTITY_NAME},
 	 /*[11]*/{PVN_AFFECTED_ENTITY_ID, LONG, BMC_AFFECTED_ENTITY_ID},
 	 /*[12]*/{PVN_NUMBER_OF_EVALUATION_ENTITIES, INT, null},
 	 /*[13]*/{PVN_EVALUATION_ENTITY_TYPE, STRING, BMC_EVALUATION_ENTITY_TYPE},
 	 /*[14]*/{PVN_EVALUATION_ENTITY_NAME, STRING, BMC_EVALUATION_ENTITY_NAME},
 	 /*[15]*/{PVN_EVALUATION_ENTITY_ID, LONG, BMC_EVALUATION_ENTITY_ID},
 	 /*[16]*/{NUMBER_OF_TRIGGERED_CONDITIONS_PER_EVALUATION_ENTITY, INT, null},
 	 /*[17]*/{PVN_TC_SCOPE_TYPE, STRING, BMC_SCOPE_TYPE},
 	 /*[18]*/{PVN_TC_SCOPE_NAME, STRING, BMC_SCOPE_NAME},
 	 /*[19]*/{PVN_TC_SCOPE_ID, LONG, BMC_SCOPE_ID},	 
 	 /*[20]*/{PVN_TC_CONDITION_NAME, STRING, BMC_METRIC_NAME},
 	 /*[21]*/{PVN_TC_CONDITION_ID, STRING, BMC_METRIC_ID},
 	 /*[22]*/{PVN_TC_OPERATOR, STRING, BMC_OPERATOR},
 	 /*[23]*/{PVN_TC_CONDITION_UNIT_TYPE, STRING, BMC_CONDITION_UNIT_TYPE},
 	 /*[24]*/{PVN_TC_USE_DEFAULT_BASELINE, STRING, BMC_USE_DEFAULT_BASELINE},
 	 /*[25]*/{PVN_TC_BASELINE_NAME, STRING, BMC_BASELINE_NAME},
 	 /*[26]*/{PVN_TC_BASELINE_ID, LONG, BMC_BASELINE_ID},
 	 /*[27]*/{PVN_TC_THRESHOLD_VALUE, STRING, BMC_METRIC_THRESHOLD},
 	 /*[28]*/{PVN_TC_OBSERVED_VALUE, STRING, BMC_METRIC_VALUE},
 	 /*[29]*/{PVN_SUMMARY_MESSAGE, STRING, BMC_SITUATION_MESSAGE},
 	 /*[30]*/{PVN_INCIDENT_ID, STRING, BMC_TOOL_ID},
 	 /*[31]*/{CONTROLLER_DEEP_LINK_URL, STRING, BMC_OBJECT_URI},
      /*[32]*/{PVN_EVENT_TYPE, STRING, BMC_POLICY_EVENT_TYPE}
 
 	};
 	
 	 static final int PVN_NUM_OF_EVAL_ENTITIES_INDEX = 12;
 	 static final int PVN_NUM_OF_EVAL_ENTITIES_ATTRS = 3;
 	 static final int PVN_NUM_OF_TRIG_CONDS_INDEX = 16;
 	 static final int PVN_NUM_OF_TRIG_CONDS_ATTRS = 12;
 	 static final int PVN_TC_CONDITION_UNIT_TYPE_INDEX = 23;
 	 static final int PVN_NUMBER_OF_BASELINE_PARMS = 3;
 	 
 
 	 static final String[][] eventNotificationParmsAndTypes = 
 	{		
 	 /*[0]*/{APPLICATION_NAME, STRING, BMC_APPLICATION_NAME},
 	 /*[1]*/{APPLICATION_ID, LONG, BMC_APPLICATION_ID},
 	 /*[2]*/{EN_TIME, INT, BMC_INCIDENT_TIME},
 	 /*[3]*/{PRIORITY, INT, BMC_PRIORITY},
 	 /*[4]*/{SEVERITY, STRING, BMC_SEVERITY},
 	 /*[5]*/{TAG, STRING, BMC_TAG},
 	 /*[6]*/{EN_NAME, STRING, BMC_TOOL_RULE},
 	 /*[7]*/{EN_ID, LONG, BMC_TOOL_KEY},
 	 /*[8]*/{EN_INTERVAL_IN_MINUTES, INT, BMC_PERIOD_IN_MINUTES },
 	 /*[9]*/{EN_NUMBER_OF_EVENT_TYPES, INT, null},
 	/*[10]*/{EN_EVENT_TYPE, STRING_ARRAY, BMC_EVENT_TYPE},
 	/*[11]*/{EN_NUMBER_OF_EVENTS, INT_ARRAY, 
 							BMC_NUMBER_OF_EVENTS_FOR_EVENT_TYPE},
 	/*[12]*/{EN_NUMBER_OF_EVENT_SUMMARIES, INT, null},
 	/*[13]*/{EN_EVENT_SUMMARY_ID, LONG, BMC_EVENT_SUMMARY_ID},
 	/*[14]*/{EN_EVENT_SUMMARY_TIME, STRING, BMC_EVENT_SUMMARY_TIME},
 	/*[15]*/{EN_EVENT_SUMMARY_TYPE, STRING, BMC_EVENT_SUMMARY_TYPE},
 	/*[16]*/{EN_EVENT_SEVERITY, STRING, BMC_EVENT_SEVERITY},
 	/*[17]*/{EN_EVENT_SUMMARY_STRING, STRING, BMC_SITUATION_MESSAGE},
 	/*[18]*/{CONTROLLER_DEEP_LINK_URL, STRING, BMC_OBJECT_URI}
 	};
 
 	 static final int EN_NUM_OF_EV_TYPS_INDEX = 9;
 	 static final int EN_NUM_OF_EV_TYPS_ATTRS = 2;
 	 static final int EN_NUM_OF_EV_SUMRY_INDEX = 12;
 	 static final int EN_NUM_OF_EV_SUMRY_ATTRS = 5;
 
 	 private static final DateFormat df = 
 			new SimpleDateFormat("EEE MMM dd kk:mm:ss z yyyy");
 	 
 	 private static final String TRUE = "true";
 	
 	/**
 	 * Default property values
 	 */
 	private static final String DEFAULT_ENDPOINT_ADDRESS
 				= "http://localhost:8080/axis2/services/ImpactManager";
 	private static final String DEFAULT_CELLNAME = "local";
 	
 	
 	/**
 	 * 	Connection properties
 	 */
 	private static final String IIWS_URL_PROPERTY = "IIWS_URL";
 	private static final String CELLNAME_PROPERTY = "CellName";
 	private static final String BINDING_TIMEOUT_PROPERTY = "BindingTimeOut";
 	private static final String USER_PROPERTY = "User";
 	private static final String PASSWORD_PROPERTY = "Password";	
 
 	
 	static String 			    endPointAddress, cellName;
 	static Properties 		    theProperties;
 	static PrintWriter			logPrintWriter;
 	static int 			        bindingTimeOut = 10; //default timeout(seconds)
 	static ImpactManagerStub 	iiwsStub;
 	static long 			    connId;
 	static String 				deepLinkUrl;
 	
 	/**
 	 * Set up connection to the web server using the global configuration
 	 * information from the input properties file
 	 *  
 	 * @return true if successful, false otherwise
 	 */
 	private static boolean setupConnection()
 	{
 		/**
 		 * Read the BPPM client properties file for the connection properties
 		 */
 		theProperties = new Properties();
 		endPointAddress = DEFAULT_ENDPOINT_ADDRESS;
 		cellName = DEFAULT_CELLNAME;
 
 		System.getProperties();
 
 		logger.info(" Using input properties file:" +
 				fileName);
 		
 	    FileInputStream propFile = null;
 		try 
 		{
 			propFile = new FileInputStream(fileName);
 		} 
 		catch (FileNotFoundException e) 
 		{
 			logger.error(e);
 		}
 		try 
 		{
 			theProperties.load(propFile);
 		} 
 		catch (IOException e) 
 		{
 			logger.error(e);
 		}
 		String strVal;
 
 		if ((strVal = theProperties.getProperty(IIWS_URL_PROPERTY)) != null)
 			endPointAddress = strVal.trim();
 
 		if ((strVal = theProperties.getProperty(CELLNAME_PROPERTY)) != null)
 			cellName = strVal.trim();
 
 		if ((strVal = theProperties.getProperty(BINDING_TIMEOUT_PROPERTY)) 
 									!= null)
 			bindingTimeOut = Integer.parseInt(strVal);
 
 		if ((strVal = theProperties.getProperty(USER_PROPERTY)) != null)
 			userName = strVal.trim();
 
 		if ((strVal = theProperties.getProperty(PASSWORD_PROPERTY)) != null)
 			password = strVal.trim();
 		
 		try
 		{
 			logger.info("Calling new ImpactManagerStub on:"+endPointAddress);
 			iiwsStub = new ImpactManagerStub(endPointAddress);
 		} 
 		catch (AxisFault e)
 		{
 			logger.error(
 			 " Could not create new ImpactmanagerStub"+
 				"on endPoint:" +endPointAddress+", caught an AxisFault: " , e);
 			return false;
 		}
 		
 		if (iiwsStub == null)
 		{
 			logger.error(
 					 " Handle to Impact Manager is null");
 			return false;
 		}
 
 		iiwsStub._getServiceClient().getOptions().setTimeOutInMilliSeconds(
 									bindingTimeOut * 1000);
 		logger.info(" ImpactManagerStub created "+
 						"and timeout set to "+ bindingTimeOut + " seconds");
 
 		boolean foundCell = false;
 		
 		ImpactManagerStub.GetCellInfo getCellInfo_input = 
 								new ImpactManagerStub.GetCellInfo();
 		 
 		try
 		{
 			ImpactManagerStub.GetCellInfo_output 
 				getCellInfo_output = iiwsStub.getCellInfo( getCellInfo_input);
 			ImpactManagerStub.CellInfo[] cellInfos =   
 				getCellInfo_output.getCellInfo_Array().getCellInfo_element();
 
 			logger.info(" Getting cell info:");
 			for (int i = 0; i < cellInfos.length; i++)
 			{
 				logger.info("	"+cellInfos[i].getCell_type()+" "
 							+cellInfos[i].getCell_name()+" "
 							+cellInfos[i].getEncryption_key()+" "
 							+cellInfos[i].getCell_hostname());
 
 				if (cellInfos[i].getCell_name().equalsIgnoreCase(cellName))
 					foundCell = true;
 			}
 			ImpactManagerStub.Connect connectInput = 
 								new ImpactManagerStub.Connect();
 			connectInput.setImname(cellName);
 			connectInput.setUserName(userName);
 			connectInput.setPassword(password);
 			connectInput.setBufferType(
 					ImpactManagerStub.IMBufferType.BMCII_BUFFER_MODE_DEFAULT );
 			ImpactManagerStub.Connect_output connectOutput 
 								= iiwsStub.connect(connectInput);
 			connId = connectOutput.getConnectionId();
 
 			if (connId == 0)
 			{
 				logger.error(" ERROR: Connect to cell:"
 							+cellName+" failed.");
 				return false;
 			}
 		} 
 		catch (Execution_Fault e)
 		{
 			logger.error("ERROR:Caught Execution_Fault on"+
 						" bmciiws_getCellinfo.", e);
 			return false;
 
 		} 
 		catch (RemoteException e)
 		{
 			logger.error("ERROR:Caught RemoteException on"+
 					" bmciiws_getCellinfo.", e);
 			return false;
 		}
 		if (!foundCell)
 		{
 			logger.error(" ERROR: The cellName:"+cellName+
 						" is not defined in mcell.dir of iiws side");
 			return false;
 		}
 		
 		logger.info(" Connected to:[" + cellName +"]");
 
 		return true;
 	}
 	
 	/**
 	 * Tear down connection to the web server 
 	 */
 	private static void teardownConnection()
 	{
 		logger.info(" Calling disconnect");
 	
 		ImpactManagerStub.Disconnect disconnect_input = 
 							new ImpactManagerStub.Disconnect ();
 		disconnect_input.setConnection(connId);
 		disconnect_input.setDeleteBuffer(false);
 		logger.info(" Disconnecting connectHandle:" 
 					+ connId);
 	
 		try 
 		{
 			if (iiwsStub != null)
 				iiwsStub.disconnect(disconnect_input);
 		} 
 		catch (RemoteException e) 
 		{
 			logger.error(e);
 		} 
 		catch (Execution_Fault e) 
 		{
 			logger.error(e);
 		}
		logger.info(" Disconnected:" + connId);
 	}
 
 	/**
 	 * Send an event to BPPM via the Impact Manager Web Server 
 	 */
 	private static void sendEvent(ImpactManagerStub.Event event,
 								  String eventClass) 
 							throws RemoteException, Execution_Fault
 	{
 		// send event to the cell
 		ImpactManagerStub.SendEvent send_input = 
 							new ImpactManagerStub.SendEvent();
 		send_input.setConnection(connId);
 		send_input.setMessage(event);
 		send_input.setTimeout(6000);
 		send_input.setMessageClass(eventClass);
 		send_input.setMessageType(
 				ImpactManagerStub.IMMessageType.MSG_TYPE_NEW_EVENT);
 			
 		logger.info(" Sending event to BPPM.");
 		ImpactManagerStub.SendEvent_output send_output 
 							= iiwsStub.sendEvent(send_input);
 		logger.info(" Response of sendEvent is:"
 								+ send_output.getResponse());
 	}
 	
 	/**
 	 * Utility function to convert an Integer array to an int array
 	 * 
 	 * @param Integer[] - An array of Integers
 	 * 
 	 * @return int[] - An array of ints
 	 */
 	public static int[] toPrimitiveIntArray(Integer[] array) 
 	{  
         if (array == null) 
         {  
             return null;  
         } 
         else if (array.length == 0) 
         {  
             return new int[0];  
         }  
         
         final int[] result = new int[array.length];  
         
         for (int i = 0; i < array.length; i++) 
         {  
             result[i] = array[i].intValue();  
         }  
         return result;  
     }  
 	
 	/**
 	 * Utility function to convert an Long array to an long array
 	 * 
 	 * @param Long[] - An array of Longs
 	 * 
 	 * @return long[] - An array of longs
 	 */
 	public static long[] toPrimitiveLongArray(Long[] array) 
 	{  
         if (array == null) 
         {  
             return null;  
         } 
         else if (array.length == 0) 
         {  
             return new long[0];  
         }  
         
         final long[] result = new long[array.length];  
         
         for (int i = 0; i < array.length; i++) 
         {  
             result[i] = array[i].longValue();
         }  
         return result;  
     }  
 	/**
 	 * Utility function to set a name value pair
 	 * 
 	 * @param nameValues - An array of name value pairs
 	 * @param name - the name of the attribute to set
 	 * @param valueObject - the value of the attribute to set
 	 * @param dataType - the data type of the attribute
 	 */
 	private static void setNameValuePair(
 					 ArrayList<ImpactManagerStub.NameValue> nameValues, 
 					 String name,
 					 String valueObject,
 					 String dataType)
 	{		
 		
 		valueObject = transformIfNecessary(name, (String) valueObject);
 		
 		ImpactManagerStub.NameValue nvPair = new ImpactManagerStub.NameValue();
 		
 		nvPair.setName(name);
 
 		ImpactManagerStub.Value value = new ImpactManagerStub.Value();
 
 		if (dataType.equalsIgnoreCase(INT))
 		{
 			value.setInt_value(Integer.parseInt(valueObject));
 		}
 		else if (dataType.equalsIgnoreCase(LONG))
 		{
 			value.setLong_value(Long.parseLong(valueObject));
 		}
 		else
 		{
 			value.setString_value(valueObject);
 		}
 		
 		nvPair.setValue(value);
 		nvPair.setValue_type(
 				ImpactManagerStub.DataType.Factory.fromValue(dataType));
 		nameValues.add(nvPair);
 	}
 	
 	/**
 	 * Utility function to set a name value pair
 	 * 
 	 * @param nameValues - An array list of name value pairs
 	 * @param name - the name of the attribute to set
 	 * @param valueObjectArray - the array of values of the attribute to set
 	 * @param dataType - the data type of the attribute
 	 */
 	private static void setNameValuePair(
 			 ArrayList<ImpactManagerStub.NameValue> nameValues, 
 			 String name,
 			 Object valueObjectArray[],
 			 String dataType)
 	{		
 		ImpactManagerStub.NameValue nvPair = new ImpactManagerStub.NameValue();
 		
 		nvPair.setName(name);
 
 		ImpactManagerStub.Value value = new ImpactManagerStub.Value();
 		if (dataType.equalsIgnoreCase(STRING_ARRAY))
 		{
 			ImpactManagerStub.ArrayOf_String aos = 
 						new ImpactManagerStub.ArrayOf_String();
 			
 			aos.setString_element((String[]) Arrays.copyOf(valueObjectArray, 
 						valueObjectArray.length, String[].class));
 			
 			
 			value.setStringArray(aos);
 		}
 		else
 		if (dataType.equalsIgnoreCase(INT_ARRAY))
 		{
 			ImpactManagerStub.ArrayOf_Int aoi = 
 				new ImpactManagerStub.ArrayOf_Int();
 			aoi.setInt_element((int[]) 
 					toPrimitiveIntArray(Arrays.copyOf(valueObjectArray, 
 					valueObjectArray.length, Integer[].class)));
 			value.setIntArray(aoi);
 		}
 		else
 		if (dataType.equalsIgnoreCase(LONG_ARRAY))
 		{
 			ImpactManagerStub.ArrayOf_Long aol = 
 				new ImpactManagerStub.ArrayOf_Long();
 			aol.setLong_element((long[]) 
 					toPrimitiveLongArray(Arrays.copyOf(valueObjectArray, 
 					valueObjectArray.length, Long[].class)));
 			value.setLongArray(aol);
 		}
 
 		nvPair.setValue(value);
 		nvPair.setValue_type(
 				ImpactManagerStub.DataType.Factory.fromValue(dataType));
 		nameValues.add(nvPair);
 	}
 	
 	/**
 	 *	Transforms the value of an attribute if necessary. 
 	 *
 	 * @param name
 	 * @param valueString
 	 * @return
 	 */
 	private static String transformIfNecessary(String name, String valueString)
 	{
 		String newValueString = valueString;
 
 		if (name.equalsIgnoreCase(BMC_SEVERITY) ||
 					name.equalsIgnoreCase(BMC_EVENT_SEVERITY))
 		{
 			if (valueString.equalsIgnoreCase(SEVERITY_ERROR))
 				newValueString = Integer.toString(
 						Severity.CRITICAL.getCode());
 			else
 			if (valueString.equalsIgnoreCase(SEVERITY_WARN))
 				newValueString = Integer.toString(
 						Severity.MINOR.getCode());
 			else
 			if (valueString.equalsIgnoreCase(SEVERITY_INFO))
 					newValueString = Integer.toString(
 						Severity.INFO.getCode());
 			else
 				newValueString = Integer.toString(
 						Severity.UNKNOWN.getCode());
 		}
 		else 
 		if (name.equalsIgnoreCase(BMC_PRIORITY))
 		{
 			switch(Integer.parseInt(valueString))
 			{
 				case 1: newValueString = Integer.toString(
 							Priority.PRIORITY_1.getCode());
 						break;
 				case 2: newValueString = Integer.toString(
 							Priority.PRIORITY_2.getCode());
 						break;
 				case 3: newValueString = Integer.toString(
 							Priority.PRIORITY_3.getCode());
 						break;
 				case 4: newValueString = Integer.toString(
 							Priority.PRIORITY_4.getCode());
 						break;
 				case 5: newValueString = Integer.toString(
 							Priority.PRIORITY_5.getCode());
 						break;
 			}
 		}
 		else
 		if (name.equalsIgnoreCase(BMC_INCIDENT_TIME))
 		{
 		    Date date;
 			try 
 			{
 				date = df.parse(valueString);
 				newValueString = Integer.toString((int) (date.getTime()/1000));
 			} 
 			catch (ParseException e) 
 			{
 				logger.error(e);
 			}  
 		}
 		else
 		if (name.equalsIgnoreCase(BMC_OBJECT_URI))
 		{
 			
 			deepLinkUrl = valueString;
 		}
 		
 		return newValueString;
 	}
 
 	private static void completeDeepLinkUrl(
 					ArrayList<ImpactManagerStub.NameValue> nameValues)
 	{
 		long incidentOrEventId = 0L;
 		for (int i=0; i < nameValues.size(); i++)
 		{
 			if (nameValues.get(i) != null)
 			{
 				if (nameValues.get(i).getName().equalsIgnoreCase(
 			    		   							BMC_EVENT_SUMMARY_ID))
 				{
 					incidentOrEventId=
 							nameValues.get(i).getValue().getLong_value();
 				}
 				else
 				if (nameValues.get(i).getName().equalsIgnoreCase(
    							BMC_TOOL_ID))
 				{
 					incidentOrEventId=
 						Long.parseLong(
 								nameValues.get(i).getValue().getString_value());					
 				}
 				else
 				if (nameValues.get(i).getName().equalsIgnoreCase(
 																BMC_OBJECT_URI))
 				{
 					ImpactManagerStub.Value value = 
 									new ImpactManagerStub.Value();
 					value.setString_value(deepLinkUrl+incidentOrEventId);
 					nameValues.get(i).setValue(value);
 					return;
 				}
 			}
 		}
 		
 	}	
 
 	/**
 	 * This method will send an event notification received from AppDynamics 
 	 * to BPPM
 	 * 
 	 * @param args - the list of arguments received with the event notification
 	 * @return true if all goes well, false otherwise
 	 */	
 	private static boolean sendEventNotification(String args[])
 	{
 		try
 		{
 
 			int numEventTypes=0;
 			
 			try 
 			{
 				numEventTypes = Integer.parseInt(
 									args[EN_NUM_OF_EV_TYPS_INDEX+1]);
 			}
 			catch(NumberFormatException nfe)
 			{
 				logger.error("Unable to parse numEventTypes from:"+args[EN_NUM_OF_EV_TYPS_INDEX+1], nfe);
 			}
 			
 			int numEventSummariesIndex = EN_NUM_OF_EV_TYPS_INDEX+2
 									+(numEventTypes*EN_NUM_OF_EV_TYPS_ATTRS);
 									
 
             int numEventSummaries = 0;
             try
             {
 		    	numEventSummaries = Integer.parseInt(args[numEventSummariesIndex]);
             }
             catch(NumberFormatException nfe)
             {
                 logger.error("Unable to parse numEventSummaries from:"+args[numEventSummariesIndex], nfe);
             }
 
 			int numParms = EN_NUM_OF_EV_TYPS_INDEX+2
 					 +(numEventTypes*EN_NUM_OF_EV_TYPS_ATTRS)
 					 +(numEventSummaries*EN_NUM_OF_EV_SUMRY_ATTRS)+1,
 			argsIndex=1,
 			parmIndex=0;
 			ImpactManagerStub.Event event = new ImpactManagerStub.Event();
 			@SuppressWarnings("unchecked")
 			ArrayList<ImpactManagerStub.NameValue>[] nameValues = 
 									new ArrayList[numEventSummaries];
 			nameValues[0]= new ArrayList<ImpactManagerStub.NameValue>();
 			int m;
 			for (m=0; m < mandatoryBMCEventAttributes.length; m++)
 			{
 				setNameValuePair(nameValues[0],
 								 mandatoryBMCEventAttributes[m][0],
 								 mandatoryBMCEventAttributes[m][2],
 								 mandatoryBMCEventAttributes[m][1]);	
 			}
 			
 
 			for (int i=0; i < numParms; i++)
 			{
 				if ((argsIndex-1) == EN_NUM_OF_EV_TYPS_INDEX)
 				{
 					argsIndex++;
 					StringBuffer eventTypes = new StringBuffer("{");
 					StringBuffer numberOfEventsForEventType = new StringBuffer("{");
 					for (int j = 0; j < numEventTypes; 
 								j++, i+=EN_NUM_OF_EV_TYPS_ATTRS)
 					{
                         if (j > 0)
                         {
                             eventTypes.append(", ");
                             numberOfEventsForEventType.append(", ");
                         }
 						eventTypes.append(args[argsIndex++]);
 						numberOfEventsForEventType.append(
                                 args[argsIndex++]);
 					}
 
                     setNameValuePair(nameValues[0],
                             eventNotificationParmsAndTypes[
                                        EN_NUM_OF_EV_TYPS_INDEX+1][2],
                             eventTypes.append("}").toString(),
                             eventNotificationParmsAndTypes[
                                     EN_NUM_OF_EV_TYPS_INDEX+1][1]);
 					
                     setNameValuePair(nameValues[0],
                             eventNotificationParmsAndTypes[
                                        EN_NUM_OF_EV_TYPS_INDEX+2][2],
                             numberOfEventsForEventType.append("}").toString(),
                             eventNotificationParmsAndTypes[
                                     EN_NUM_OF_EV_TYPS_INDEX+2][1]);
 
                     parmIndex += EN_NUM_OF_EV_TYPS_ATTRS+1;
 				}
 				else 
 				if (argsIndex == numEventSummariesIndex)
 				{
 						argsIndex++;
 						@SuppressWarnings("unchecked")
 						ArrayList<ImpactManagerStub.NameValue> tmp = 
 							(ArrayList<NameValue>) nameValues[0].clone();
 						for (int j = 0; j < numEventSummaries; 
 									j++, i+=EN_NUM_OF_EV_SUMRY_ATTRS)
 						{
 							if (j > 0)
 							{
 								nameValues[j]= 
 									new ArrayList<ImpactManagerStub.NameValue>();
 								
 								nameValues[j].addAll(tmp);
 
 							}
 							for (int k=0; k < EN_NUM_OF_EV_SUMRY_ATTRS; k++)
 							{
 	                            setNameValuePair(nameValues[j], 
 	                                    eventNotificationParmsAndTypes[
 	                                           EN_NUM_OF_EV_SUMRY_INDEX+1+k][2],
 	                                    args[argsIndex++], 
 	                                    eventNotificationParmsAndTypes[
 	                                           EN_NUM_OF_EV_SUMRY_INDEX+1+k][1]);
 							}
 						}
 						parmIndex += EN_NUM_OF_EV_SUMRY_ATTRS+1;
 					}
 					else
 					{
 						setNameValuePair(nameValues[0], 
 								     eventNotificationParmsAndTypes[parmIndex][2],
 								     args[argsIndex++], 
 								     eventNotificationParmsAndTypes[parmIndex][1]);
 						parmIndex++;
 					}
 			}
 			
 			for (int i = 0; i < numEventSummaries; i++)
 			{
 				if (i > 0)
 				{
 					for (int j = nameValues[0].size()-1, 
 							k= nameValues[i].size()-1; j > k; j--)
 					{
 						nameValues[i].add(nameValues[0].get(j));
 					}
 						
 				}
 				completeDeepLinkUrl(nameValues[i]);
 				event.setSubject("AppDynamics Event Notification");
 				ImpactManagerStub.NameValue[] nvArray = new 
 											ImpactManagerStub.NameValue[
 				                                          nameValues[i].size()];
 				System.arraycopy(nameValues[i].toArray(), 0, nvArray, 0, 
 												nameValues[i].size());
 				event.setNameValue_element(nvArray);
 				sendEvent(event,  APPDYNAMICS_EVENT_NOTIFICATION);
 			}
 		} 
 		catch (RemoteException e) 
 		{
 			logger.error(e);
 			return false;
 		} 
 		catch (Execution_Fault e)
 		{
 			logger.error(e);
 			return false;
 		} 
 		
 		return true;
 	}
 
 		
 	/**
 	 * This method will send an policy violation notification received from 
 	 * AppDynamics to BPPM
 	 * 
 	 * @param args - the list of arguments received with the violation 
 	 * notification
 	 * 
 	 * @return true if all goes well, false otherwise
 	 */
 	private static boolean sendPolicyViolationNotification(String args[])
 	{
         int numEvaluationEntities =0;
 		try
         {
             numEvaluationEntities = Integer.parseInt(
                     args[PVN_NUM_OF_EVAL_ENTITIES_INDEX+1]);
         }
         catch(NumberFormatException nfe)
         {
             logger.error("Unable to parse numEvaluationEntities from:"+args[PVN_NUM_OF_EVAL_ENTITIES_INDEX+1], nfe);
 
         }
 		int	numParms = PVN_NUM_OF_EVAL_ENTITIES_INDEX
 				+1+(numEvaluationEntities*PVN_NUM_OF_EVAL_ENTITIES_ATTRS)+2,
 			argsIndex = 1,
 			parmIndex=0;
 
 		try
 		{
 			@SuppressWarnings("unchecked")
 			ArrayList<ImpactManagerStub.NameValue>[] nameValues = 
 									new ArrayList[numEvaluationEntities*50];
 			nameValues[0]= new ArrayList<ImpactManagerStub.NameValue>();
 
 			
 			for (int man=0; man < mandatoryBMCEventAttributes.length; man++)
 			{
 				setNameValuePair(nameValues[0],
 								 mandatoryBMCEventAttributes[man][0],
 								 mandatoryBMCEventAttributes[man][2],
 								 mandatoryBMCEventAttributes[man][1]);
 			}
 
 			while (argsIndex < args.length)
 			{
 				if ((argsIndex-1) == PVN_NUM_OF_EVAL_ENTITIES_INDEX)
 				{
 					@SuppressWarnings("unchecked")
 					ArrayList<ImpactManagerStub.NameValue> tmp = 
 						(ArrayList<ImpactManagerStub.NameValue>) 
 								nameValues[0].clone();
 					argsIndex++;
 					for (int j = 0; j < numEvaluationEntities; j++)
 					{
 
 						if (j > 0)
 						{
 							nameValues[j]= 
 								new ArrayList<ImpactManagerStub.NameValue>();
 							nameValues[j].addAll(tmp);
 						}
 
 						for (int k=0; k < PVN_NUM_OF_EVAL_ENTITIES_ATTRS; k++)
 						{
 
 							if ((PVN_NUM_OF_EVAL_ENTITIES_INDEX+k+2) == 
 								PVN_NUM_OF_TRIG_CONDS_INDEX)
 								{
 									argsIndex++;
 
 									int numTriggeredConditions = 0;
                                     try
                                     {
 										numTriggeredConditions=Integer.parseInt(args[argsIndex]);
                                     }
                                     catch(NumberFormatException nfe)
                                     {
                                         logger.error("Unable to parse numTriggeredConditions from:"
                                                 +args[argsIndex], nfe);
 
                                     }
 									numParms += PVN_NUM_OF_TRIG_CONDS_ATTRS+1;
 									@SuppressWarnings("unchecked")
 									ArrayList<ImpactManagerStub.NameValue> tmp2  
 									  =	(ArrayList<ImpactManagerStub.NameValue>) 
 												nameValues[0].clone();
 									argsIndex++;
 									int l;
 									for (l = 0; l < numTriggeredConditions; l++)
 									{
 
 										if (l > 0)
 										{
 											nameValues[l]= 
 												new ArrayList<
 												ImpactManagerStub.NameValue>();
 											nameValues[l].addAll(tmp2);
 										}
 
 										for (int m=0; 
 												 m<PVN_NUM_OF_TRIG_CONDS_ATTRS; 
 												 m++)
 										{
 											setNameValuePair(nameValues[l], 
 											   pvnParmsAndTypes[
 					                            PVN_NUM_OF_TRIG_CONDS_INDEX+1+m]
 					                            							[2],
 					                           args[argsIndex++], 
 					                           pvnParmsAndTypes[
 					                            PVN_NUM_OF_TRIG_CONDS_INDEX+1+m]
 					                            						[1]);
 											if ((PVN_NUM_OF_TRIG_CONDS_INDEX+1+m) 
 													== 
 											   PVN_TC_CONDITION_UNIT_TYPE_INDEX) 
 											{
 												if (!args[argsIndex-1].startsWith(
 																		BASELINE_PREFIX))
 												{
 													m+=PVN_NUMBER_OF_BASELINE_PARMS;
 													numParms -=3;
                                                 }
 												else if (args[argsIndex].equalsIgnoreCase(
 															TRUE))
 												{
 													m+=PVN_NUMBER_OF_BASELINE_PARMS-1;
 													numParms -=2;
                                                 }
 											}
 										}
 									}
 									
 								}
 								else
 								{
 									
 			                        setNameValuePair(nameValues[j], 
 			                                  pvnParmsAndTypes[
 			                                     PVN_NUM_OF_EVAL_ENTITIES_INDEX+1+k][2],
 			                                  args[argsIndex++], 
 			                                  pvnParmsAndTypes[
 			                                     PVN_NUM_OF_EVAL_ENTITIES_INDEX+1+k][1]
 			                                  );
 								}
 							}
 					}
 					
 					parmIndex += (PVN_NUM_OF_EVAL_ENTITIES_ATTRS+PVN_NUM_OF_TRIG_CONDS_ATTRS+2);
 				}
 				else
 				{
 					setNameValuePair(nameValues[0], 
 							pvnParmsAndTypes[parmIndex][2],
 							args[argsIndex++], 
 							pvnParmsAndTypes[parmIndex][1]);
 					parmIndex++;
 				}
 				
 			}
 
 			for (int i = 0; nameValues[i] != null; i++)
 			{
 				if (i > 0)
 				{
 					for (int j=0, count=0, size = nameValues[0].size(); 
 							count < 3 && j < size; j++)
 					{
 						if (nameValues[0].get(j) != null)
 						{
 							if ((nameValues[0].get(j).getName().
 									equalsIgnoreCase(BMC_SITUATION_MESSAGE))
 								|| (nameValues[0].get(j).getName().
 										equalsIgnoreCase(BMC_OBJECT_URI))
 								|| (nameValues[0].get(j).getName().
 										equalsIgnoreCase(BMC_TOOL_ID))
 								)
 							{
 								nameValues[i].add(nameValues[0].get(j));
 								count++;
 							}
 						}
 					}
 				}
 				
 				completeDeepLinkUrl(nameValues[i]);
 
 				ImpactManagerStub.Event event = new ImpactManagerStub.Event();
 				event.setSubject("AppDynamics Policy Violation Notification");
 				ImpactManagerStub.NameValue[] nvArray = new 
 								ImpactManagerStub.NameValue[
 								                          nameValues[i].size()];
 				System.arraycopy(nameValues[i].toArray(), 0, nvArray, 0, 
 										nameValues[i].size());
 				event.setNameValue_element(nvArray);
 				sendEvent(event, APPDYNAMICS_POLICY_VIOLATION_NOTIFICATION);
 			}
 			
 		} 
 		catch (RemoteException e) 
 		{
 			logger.error(e);
 			return false;
 		} 
 		catch (Execution_Fault e)
 		{
 			logger.error(e);
 			return false;
 		}
 		
 		return true;
 	}
 
     static void removeDoubleQuotes(String[] args)
     {
 
         for (int i=0; i < args.length; i++)
         {
             args[i]=args[i].replaceAll("\"", "");
         }
     }
 
 	/**
 	 * Main method called from AppDynamics custom action shell script 
 	 * or batch file.
 	 * 
 	 * @param args - arguments passed
 	 */
 	public static void main(String[] args) 
 	{
 		
 		if (args.length < 10)
 		{
 			logger.error("Too few arguments"+
 									" ... exiting");
 			System.exit(-1);
 		}
 		
 		
 		logger.info(BANNER);
 		logger.info("Received notification parameters:"+
 				Arrays.toString(args));
 
         removeDoubleQuotes(args);
 
 		int rc = 0;
 
 		try 
 		{
 			String notificationType = null;
 			boolean connectionSetup = setupConnection();
 		
 			if (!connectionSetup)
 			{
 				logger.error("Unable to setup connection");
 				System.exit(-1);
 			}
 	
 			notificationType = args[0];
 			
 			if (notificationType.equalsIgnoreCase(EVENT_NOTIFICATION))
 			{
 				logger.info(
 					"*** Processing event notification from AppDynamics");
 				rc = sendEventNotification(args) ? 0 : -1;
 			}
 			else 
 			if(notificationType.equalsIgnoreCase(POLICY_VIOLATION_NOTIFICATION))
 			{
 				logger.info("*** Processing policy violation notification from"+
 									" AppDynamics");				
 				rc = sendPolicyViolationNotification(args) ? 0 : -1;
 			}
 			else
 			{
 				throw new IllegalArgumentException(notificationType);
 			}
 			
 			teardownConnection();
 
 			logger.info(BANNER);				
 		} 
 		catch (Throwable t) 
 		{
 			logger.error(t);
 		}
 		
 		System.exit(rc);
 	}//main
 	
 }
