 /*
  * Copyright (c) 2012, Indraprastha Institute of Information Technology,
  * Delhi (IIIT-D) and The Regents of the University of California.
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions
  * are met:
  * 1. Redistributions of source code must retain the above copyright
  *    notice, this list of conditions and the following disclaimer.
  * 2. Redistributions in binary form must reproduce the above
  *    copyright notice, this list of conditions and the following
  *    disclaimer in the documentation and/or other materials provided
  *    with the distribution.
  * 3. Neither the names of the Indraprastha Institute of Information
  *    Technology, Delhi and the University of California nor the names
  *    of their contributors may be used to endorse or promote products
  *    derived from this software without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE IIIT-D, THE REGENTS, AND CONTRIBUTORS
  * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
  * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
  * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE IIITD-D, THE REGENTS
  * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
  * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
  * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
  * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
  * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
  * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
  * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
  * SUCH DAMAGE.
  *
  */
 /*
  * Name: Const.java
  * Project: SensorAct-VPDS
  * Version: 1.0
  * Date: 2012-04-14
  * Author: Pandarasamy Arjunan, Haksoo Choi
  */
 package edu.pc3.sensoract.vpds.constants;
 
 /**
  * Defines various constants. 
  * 
  * @author Pandarasamy Arjunan, Haksoo Choi, Manaswi Saha
  * @version 1.0
  */
 
 public class Const {
 
 	/*
 	 * General 
 	 */
 	
 	public static final String repoName = "SensorActRepo";
 	public static final String repoURL = "http://localhost:9000/";
 	//public static final String repoURL = "http:muc.iiitd.com:9000/";
 	
 	public static final String SENSORACT = "SensorAct";
 	public static final String APPLICATION_NAME = SENSORACT;
 
 	public static final String TODO = "Yet to implement. Please stay tuned!";
 
 	public static final String MSG_SUCCESS = "Success";
 	public static final String MSG_FAILED = "Failed";
 	public static final String MSG_ERROR = "Error ";
 	public static final String MSG_NONE = "None";
 	public static final String DELIM1 = "-> ";
 	public static final String DELIM2 = ": ";
 
 	public static final int SUCCESS = 0;
 
 	// owner.conf file keys
 	public static final String OWNER_CONFIG_FILENAME = "owner.conf";
 	public static final String OWNER_NAME = "owner.name";
 	public static final String OWNER_PASSWORD = "owner.password";
 	public static final String OWNER_EMAIL = "owner.email";
 	public static final String OWNER_OWNERKEY = "owner.ownerkey";
 	public static final String OWNER_UPLOADKEY = "owner.uploadkey";
 	public static final String OWNER_ACTUATIONKEY = "owner.actuationkey";
 	
 	/*
 	 * API names
 	 */
 	public static final String API_REPO_INFO = "repo/info";
 	public static final String API_USER_LOGIN = "user/login";
 	public static final String API_USER_REGISTER = "user/register";
 	public static final String API_USER_LIST = "user/list";
 	
 	//public static final String API_USER_GENERATE_REPOKEY = "user/generate/repokey";
 	public static final String API_KEY_GENERATE = "key/generate";
 	public static final String API_KEY_DELETE = "key/delete";
 	public static final String API_KEY_LIST = "key/list";
 	public static final String API_KEY_ENABLE = "key/enable";
 	public static final String API_KEY_DISABLE = "key/disable";
 
 	public static final String API_DEVICE_ADD = "device/add";
 	public static final String API_DEVICE_DELETE = "device/delete";
 	public static final String API_DEVICE_GET = "device/get";
 	public static final String API_DEVICE_LIST = "device/list";
 	public static final String API_DEVICE_SEARCH = "device/search";
 	public static final String API_DEVICE_SHARE = "device/share";
 	public static final String API_DEVICE_ACTUATE = "device/actuate";
 	public static final String API_DEVICE_LIST_ACTUATION_REQUEST = "device/actuationrequest/list";
 	public static final String API_DEVICE_CANCEL_ACTUATION_REQUEST = "device/actuationrequest/cancel";
 
 	public static final String API_DEVICE_TEMPLATE_ADD = "device/template/add";
 	public static final String API_DEVICE_TEMPLATE_DELETE = "device/template/delete";
 	public static final String API_DEVICE_TEMPLATE_GET = "device/template/get";
 	public static final String API_DEVICE_TEMPLATE_LIST = "device/template/list";
 	public static final String API_DEVICE_TEMPLATE_GLOBAL_LIST = "device/template/global/list";
 
 	public static final String API_GUARDRULE_ADD = "guardrule/add";
 	public static final String API_GUARDRULE_DELETE = "guardrule/delete";
 	public static final String API_GUARDRULE_GET = "guardrule/get";
 	public static final String API_GUARDRULE_LIST = "guardrule/list";
 	public static final String API_GUARDRULE_ASSOCIATION_ADD = "guardrule/association/add";
 	public static final String API_GUARDRULE_ASSOCIATION_DELETE = "guardrule/association/delete";
 	public static final String API_GUARDRULE_ASSOCIATION_GET = "guardrule/association/get";
 	public static final String API_GUARDRULE_ASSOCIATION_LIST = "guardrule/association/list";
 
 	public static final String API_TASKLET_ADD = "tasklet/add";
 	public static final String API_TASKLET_DELETE = "tasklet/delete";
 	public static final String API_TASKLET_GET = "tasklet/get";
 	public static final String API_TASKLET_LIST = "tasklet/list";
 	
 	public static final String API_TASKLET_EXECUTE = "tasklet/execute";
 	public static final String API_TASKLET_STATUS = "tasklet/status";
 	public static final String API_TASKLET_CANCEL = "tasklet/cancel";
 	
 	public static final String API_DATA_UPLOAD_WAVESEGMENT = "data/upload/wavesegment";
 	public static final String API_DATA_QUERY = "data/query";
 
 	/*
 	 * API parameter names
 	 */
 	public static final String PARAM_USERNAME = "username";
 	public static final String PARAM_PASSWORD = "password";
 	public static final String PARAM_EMAIL = "email";
 	public static final String PARAM_SECRETKEY = "secretkey";
 	public static final String PARAM_KEY = "key";
 
 	public static final String PARAM_DEVICEPROFILE = "deviceprofile";
 	public static final String PARAM_NAME = "name";
 	public static final String PARAM_IP = "IP";
 	public static final String PARAM_LOCATION = "location";
 	public static final String PARAM_TAGS = "tags";
 	public static final String PARAM_LATITUDE = "latitude";
 	public static final String PARAM_LONGITUDE = "longitude";
 	public static final String PARAM_SENSORS = "sensors";
 	public static final String PARAM_ACTUATORS = "actuators";	
 	public static final String PARAM_SID = "sid";
 	public static final String PARAM_CHANNELS = "channels";
 	public static final String PARAM_TYPE = "type";
 	public static final String PARAM_UNIT = "unit";
 	public static final String PARAM_SAMPLING_PERIOD = "samplingperiod";
 
 	public static final String PARAM_DATA = "data";
 	public static final String PARAM_CHANNEL = "channel";
 	public static final String PARAM_EPOCTIME = "epoctime";
 	public static final String PARAM_READINGS = "readings";
 	public static final String PARAM_DEVICENAME = "devicename";
 	public static final String PARAM_TEMPLATENAME = "templatename";
 	
 	public static final String PARAM_TASKLETNAME = "taskletname";
 	public static final String PARAM_TASKLETID = "taskletid";
 	public static final String PARAM_DESC = "desc";
 
 	public static final String PARAM_WHEN = "when";
 	public static final String PARAM_EXECUTE = "execute";
 	
 	public static final String PARAM_ALLOW = "allow";
 	public static final String PARAM_READ = "read";
 	public static final String PARAM_WRITE = "write";
 	
 	/*
 	 * API parameter validation limits
 	 */
 	public static final int USERNAME_MIN_LENGTH = 8;
 	public static final int USERNAME_MAX_LENGTH = 20;
 
 	public static final int PASSWORD_MIN_LENGTH = 8;
 	public static final int PASSWORD_MAX_LENGTH = 20;
 
 	public static final int EMAIL_MIN_LENGTH = 6;
 	public static final int EMAIL_MAX_LENGTH = 40;
 
 	public static final int SECRETKEY_MIN_LENGTH = 32;  // UUID length
 	public static final int SECRETKEY_MAX_LENGTH = 32;
 
 	public static final int DEVICENAME_MIN_LENGTH = 2;
 	public static final int DEVICENAME_MAX_LENGTH = 20;
 
 	public static final int TEMPLATENAME_MIN_LENGTH = 2;
 	public static final int TEMPLATENAME_MAX_LENGTH = 20;
 
 	public static final int LOCATION_MIN_LENGTH = 2;
 	public static final int LOCATION_MAX_LENGTH = 50;
 
 	public static final int TAGS_MIN_LENGTH = 2;
 	public static final int TAGS_MAX_LENGTH = 100;
 
 	public static final double LATITUDE_MIN_VALUE = -90.0;
 	public static final double LATITUDE_MAX_VALUE = 90.0;
 
 	public static final double LONGITUDE_MIN_VALUE = -180.0;
 	public static final double LONGITUDE_MAX_VALUE = 180.0;
 
 	public static final int SAMPLING_PERIOD_MIN_VALUE = 1;
 	public static final int SAMPLING_PERIOD_MAX_VALUE = 60;
 
 	public static final int DEVICEPROFILENAME_MIN_LENGTH = 2;
 	public static final int DEVICEPROFILENAME_MAX_LENGTH = 20;
 
 	public static final int SENSORNAME_MIN_LENGTH = 2;
 	public static final int SENSORNAME_MAX_LENGTH = 20;
 
 	public static final int SENSORID_MIN_VALUE = 1;
 	public static final int SENSORID_MAX_VALUE = 1000;
 
 	public static final int CHANNELNAME_MIN_LENGTH = 1;
 	public static final int CHANNELNAME_MAX_LENGTH = 20;
 
 	public static final int CHANNELTYPE_MIN_LENGTH = 2;
 	public static final int CHANNELTYPE_MAX_LENGTH = 20;
 
 	public static final int CHANNELUNIT_MIN_LENGTH = 2;
 	public static final int CHANNELUNIT_MAX_LENGTH = 20;
 
 	public static final int ACTUATORNAME_MIN_LENGTH = 2;
 	public static final int ACTUATORNAME_MAX_LENGTH = 20;
 
 	public static final int TASKLETNAME_MIN_LENGTH = 2;
 	public static final int TASKLETNAME_MAX_LENGTH = 40;
 
 	public static final int TASKLETID_MIN_LENGTH = 2;
 	public static final int TASKLETID_MAX_LENGTH = 100;
 
 	public static final int TASKLET_DESC_MIN_LENGTH = 2;
 	public static final int TASKLET_DESC_MAX_LENGTH = 200;
 
 	public static final int TASKLET_PARAM_MAX_LENGTH = 20;
 	public static final int TASKLET_PARAM_VALUE_MAX_LENGTH = 100;
 
 	/*
 	 * API parameter validation error messages
 	 */
 	public static final String MSG_REQUIRED = " is required";
 	public static final String MSG_LENGTH = " length must be ";
 	public static final String MSG_MIN_LENGTH = " minimum length is ";
 	public static final String MSG_MAX_LENGTH = " maximum length is ";
 	public static final String MSG_MIN_VALUE = " minimum value is ";
 	public static final String MSG_MAX_VALUE = " maximum value is ";
 	public static final String MSG_INVALID = " is invalid";
 	public static final String MSG_EMPTY = " is empty";
 
 	/*
 	 * API Error messages
 	 */
 	public static final String SYSTEM_ERROR = "System error";
 	public static final String VALIDATION_FAILED = "Validation failed";
 	public static final String INVALID_JSON = "Invalid JSON object";
 	public static final String EMPTY_JSON = "Empty JSON object";
 
 
 	/*
 	 * API success/failure messages
 	 */	
 	public static final String LOGIN_FAILED = "Login failed";
 	public static final String USER_REGISTERED = "New Userprofile registered";
 	public static final String USER_ALREADYEXISTS = "Userprofile already exists";
 	
 	public static final String KEY_NOTFOUND = "Key not found";
 	public static final String KEY_DELETED = "Key deleted";
 	public static final String KEY_ENABLED = "Key enabled";
 	public static final String KEY_DISABLED = "Key disabled";
 	
 
 	public static final String DEVICE_ADDED = "New device added";
 	public static final String DEVICE_ALREADYEXISTS = "Device already exists";
 	public static final String DEVICE_DELETED = "Device successfully deleted";
 	public static final String DEVICE_NOTFOUND = "Device not found";	
 	public static final String DEVICE_NODEVICE_FOUND = "No device found";
 
 	public static final String DEVICE_TEMPLATE_ADDED = "New device template added";
 	public static final String DEVICE_TEMPLATE_ALREADYEXISTS = "Device template already exists";
 	public static final String DEVICE_TEMPLATE_DELETED = "Device template successfully deleted";
 	public static final String DEVICE_TEMPLATE_NOTFOUND = "Device template not found";	
 	public static final String DEVICE_TEMPLATE_NOTEMPLATE_FOUND = "No device template found";
 
 	public static final String UNREGISTERED_SECRETKEY = "Unregistered secretkey";
 	public static final String UNREGISTERED_USERNAME = "Unregistered username";
 
 	public static final String UPLOAD_WAVESEGMENT_SUCCESS = "Wavesegment stored successfully";
 
 	public static final String GUARDRULE_ADDED = "New guard rule added";
 	public static final String GUARDRULE_ALREADYEXISTS = "Guard rule exists";
 	public static final String GUARDRULE_DELETED = "Guard rule successfully deleted";
 	public static final String GUARDRULE_ASSOCIATION_ADDED = "Guard rule successfully associated";
 	public static final String GUARDRULE_ASSOCIATION_DELETED = "Guard rule successfully dissociated";
 	public static final String GUARDRULE_ASSOCIATION_NOTDELETED = "Guard rule unsuccessful dissociation";
 	
 	public static final String GUARDRULE_NOTFOUND = "No guard rule found";
 	public static final String GUARDRULE_ASSOCIATION_NOTFOUND = "No guard rule association found";
 	
 	public static final String TASKLET_NOTFOUND = "Tasklet not found";
 	public static final String TASKLET_SCHEDULED = "Tasklet scheduled";
 	public static final String TASKLET_NOTSCHEDULED = "Tasklet not scheduled";
 	
 	public static final String TASKLET_CANCELED = "Tasklet cancelled";
 	public static final String TASKLET_NOTCANCELED = "Tasklet failed to cancel";
 	public static final String TASKLET_ALREADY_SCHEDULED = "Tasklet already scheduled";
 	public static final String TASKLET_FAILED_TO_SCHEDULE = "Tasklet failed to schedule";
 	
 	
 	public static final String ACTREQUEST_NOTFOUND = "Actuation request not found";
 	public static final String ACTREQUEST_LISTFAILED = "Failed to retrieve the list at this time.";
 	
 	private Const() {
 	}
 }
