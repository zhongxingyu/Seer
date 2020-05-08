 package util;
 
 public interface AllConstants {
 
 	public static interface actions {
 
 		public static final String user = "user";
 	}
 
 	public static interface HBaseContants {
 	}
 
 	public static interface ServerConfigs {
 
 		public static final String configsFolderPath = "AppConfigs/";
 		public static final String UserAvatarLocation = "UserAvatarLocation";
 		public static final String AvatarAccessPoint ="AvatarAccessPoint";
 		public static final String UndefinedAvatarLocation="UndefinedAvatarLocation";
 	}
 
 	public static interface api_entryPoints {
 		public static final String api_subject = "subjects";
 		public static final String api_defaultsubject="defaultsubject";
 		public static final String api_health = "health";
 		public static final String api_datastream = "datastreams";
 		public static final String api_title = "title";
 		public static final String api_device = "devices";
 		public static final String api_datapoints = "datapoints";
 		public static final String api_datasummaries = "datasummaries";
 		public static final String api_following = "followings";
 		public static final String api_follower = "followers";
 		public static final String api_devicebinding = "devicebinding";
 		public static final String api_user = "users";
 		public static final String api_myaccount = "myaccount";
 		public static final String api_lifestyle = "lifestyle";
 		public static final String api_location = "location";
 		public static final String api_register = "register";
 		public static final String api_avatar = "avatar";
 		public static final String api_GetToken = "gettoken";
 
 		public static final String api_search = "search";
 		public static final String api_list = "list";
 		public static final String version1 = "v1";
 		public static final String api_url = "/" + version1 + "/";
 		public static final String api_datablocks = "datablocks";
 		public static final String requset_api_callback = "jsonpCallback";
 		public static final String request_api_loginid = "loginid";
 		public static final String request_api_targetid = "targetid";
 		public static final String request_api_start = "start";
 		public static final String request_api_startDate = "startdate";
 		public static final String request_api_at = "at";
 		public static final String request_devicetemplateID = "devicetemplateid";
 		public static final String request_api_end = "end";
 		public static final String request_api_endDate = "enddate";
 		public static final String request_api_YearMonthDay = "date";
 		public static final String request_api_blockid = "blockid";
 		public static final String request_api_onlyParentSubjects = "onlyparents";
 		public static final String request_api_true = "true";
 		public static final String request_api_unit_id = "unitid";
 		public static final String request_api_keywords = "keywords";
 		public static final String request_api_startpage = "startpage";
 		public static final String request_api_dataformat = "dateformat";
 		public static final String request_max = "max";
 		public static final String header_api_token = "accesstoken";
 		public static final String wildcardsubject_all = "all";
 	}
 
 	public interface HibernateConsts {
 		public static final int UserSearch_maxPageSize = 500;
 		public static final int UserList_maxPageSize = 500;
 	}
 
 	public interface ProgramConts {
 
 		public static final String existWildcardSubject = "exist_wildcard_subject";
 		public static final String existSubjectAndDatastreamID = "exist_datastream_ID";
 		public static final String allow = "allow";
 		public static final String subject_medical_device_purpose = "medical_device";
 		public static final String succeed = "succeed";
 		public static final String result = "result";
 		public static final String exportSetting_MAX = "max";
 		public static final String total_points = "total_points";
 		public static final String total_records = "total_records";
 		public static final String follower_status_active = "active";
 		public static final String follower_status_pending = "pending";
 		public static final String follower_status_ignored = "ignored";
 		public static final String job_status_pending = "pending";
 		public static final String job_method_delete = "delete";
 		public static final String job_targetObject_datastream = "datastream";
 		public static final String job_targetObject_datablock = "datablock";
 		public static final String visibleSet_PUBLIC = "public";
 		public static final String defaultDS_Name_steps = "steps";
 		public static final String defaultDS_Name_sleep = "sleep";
 		public static final String defaultDS_Name_distancetravel = "distance_travel";
 		public static final String defaultDS_Name_floor_climbed = "floor_climbed";
 		public static final String defaultDS_Name_calories_burned = "calories_burned";
 		public static final String defaultDS_Name_weight = "weight";
 		public static final String defaultDS_Name_location = "location";
 		public static final String defaultDS_Name_ecg = "ecg";
		public static final String defaultDS_Name_eeg="eeg";
		public static final String defaultDS_Name_heart_rate="heart_rate";
 
 	}
 
 	public interface HealthConts {
 		// public static final String UnitSymbol_Steps="steps";
 		// public static final String UnitLabel_Steps="steps";
 		// public static final String UnitSymbol_distance_travel="km";
 		// public static final String UnitLabel_distance_travel="distance";
 		// public static final String UnitSymbol_floor_climbed="floors";
 		// public static final String UnitLabel_floor_climbed="floor_climbed";
 		// public static final String UnitSymbol_calories_burned="calories";
 		// public static final String
 		// UnitLabel_calories_burned="calaories_burned";
 		// public static final String UnitSymbol_weight="kg";
 		// public static final String UnitLabel_weight="weight";
 		// public static final String UnitSymbol_location="steps";
 		// public static final String UnitLabel_location="steps";
 		// public static final String UnitSymbol_Sleep="times";
 		// public static final String UnitLabel_Sleep="awake";
 		// public static final String UnitSymbol_ECG="";
 		// public static final String UnitLabel_Sleep="awake";
 		public static final String unit_default_template_id_steps = "default_steps";
 		public static final String unit_default_template_id_sleep = "default_sleep";
 		public static final String unit_default_template_id_distance_travel = "default_distance_travel";
 		public static final String unit_default_template_id_floor_climbed = "default_floor_climb";
 		public static final String unit_default_template_id_calories_burned = "default_calories_burned";
 		public static final String unit_default_template_id_weight = "default_weight";
 		public static final String unit_default_template_id_location = "default_location";
 		public static final String unit_default_template_id_ecg = "default_ecg";
 		public static final String default_health_subject_purpose = "default_health";
 		public static final String default_health_SubjectName = "default_health";
 		public static final String defaultDatastreamPurpose = "default_health";
 		
 		public static final String System_Default_subject_Name = "default_subject";
 
 	}
 
 	public interface ExternalAPIConsts {
 		public static final String fitbit_device = "fitbit";
 	}
 
 	public interface ValidDictionary {
 
 		public static final String Valid = "valid";
 	}
 
 	public interface HttpStatusCode {
 
 		public static int OK = 200;// request processed successfully.
 		public static int BAD_REQUEST = 400;
 		public static int NOT_AUTHORIZED = 401;// Not Authorized: either you
 												// need to provide
 												// authentication credentials,
 												// or the credentials provided
 												// aren't valid.
 		public static int FORBIDDEN = 403;// Cosm understands your request, but
 											// refuses to fulfill it. An
 											// accompanying error message should
 											// explain why.
 		public static int NOT_FOUND = 404; // either you're requesting an
 											// invalid URI or the resource in
 											// question doesn't exist (eg. no
 											// such feed).
 		public static int Unprocessable_Entity = 422;// either you're requesting
 														// an invalid URI or the
 														// resource in question
 														// doesn't exist (eg. no
 														// such feed).
 		public static int INTERNAL_SERVER_ERROR = 500;// Internal Server Error:
 														// Something went
 														// wrong... Please post
 														// to the forum about it
 														// and we will
 														// investigate.
 		public static int NO_SERVER_ERROR = 503;// usually occurs when there are
 												// too many requests coming into
 												// Cosm - if you get this from
 												// an API request then the error
 												// message will be returned in
 												// XML in the response.
 	}
 
 	public interface ErrorDictionary {
 
 		public static final String MISSING_DATA = "missing_data";
 		public static final String email_Exist = "email_address_exist";
 		public static final String LoginID_Exist = "loginid_exist";
 		public static final String WrongVeriCode = "wrong_code";
 		public static final String unknownFault = "unknown";
 		public static final String INPUT_DATE_FORMAT_ERROR = "input_date_format_error";
 		public static final String Unauthorized_Access = "unauthorized_access";
 		public static final String Unknown_StreamID = "unknown_stream_id";
 		public static final String NO_STREAM_UNIT_EXIST = "no_datastream_unit_exist";
 		public static final String MORE_THAN_ONE_DATASTREAM_UNIT = "more_than_one_datastream_unit_exist";
 			public static final String Unknown_StreamTitle = "unknown_stream_title";
 		public static final String Unknown_SubjectID = "unknown_subject_id";
 		public static final String NO_SLEEP_RECORD = "no_sleep_record_exist";
 		
 		public static final String Invalid_ValueType = "invalid_value_type_name";
 		public static final String InputValue_Oversize = "input_over_size";
 		public static final String Hibernate_Internal_Error = "hibernate_internal_error";
 		public static final String HBase_Internal_Error = "hbase_internal_error";
 		public static final String Input_Json_Format_Error = "json_input_syntax_error";
 		public static final String No_Input_Single_Datapoints = "no_input_single_datapoints";
 		public static final String No_Input_Datapoints = "no_input_datapoints";
 		public static final String Input_file_format_error = "input_file_format_error";
 		public static final String Invalid_Unit_ID = "invalid_unit_id";
 		public static final String Invalid_Datablock_ID = "invalid_datablock_id";
 		public static final String Invalid_LoginID = "invalid_loginid";
 		public static final String Invalid_wrong_password = "invalid_password";
 		public static final String Invalid_login_token_id = "invalid_login_tokenid";
 		public static final String login_token_expired = "login_token_expired";
 		public static final String Invalid_Login_format = "invalid_login_format";
 		public static final String Invalid_password_format = "invalid_password_format";
 		public static final String Internal_Fault = "internal_fault";
 		public static final String Invalid_Target_LoginID = "invalid_target_loginid";
 		public static final String following_exist = "following_exist";
 		public static final String already_exist_wildcard_subject = "subject_wildcard_exist";
 		public static final String already_exist_subject_datastream = "subject_datastream_follow_exist";
 		public static final String Not_exist_follow = "follow_not_exist";
 		public static final String Invalid_date_format = "invalid_date_input_format";
 		public static final String SYSTEM_ERROR_NO_DEFAULT_HEALTH_SUBJECT = "sys_error_no_default_subject";
 		public static final String SYSTEM_ERROR_NO_DEFAULT_SUBJECT = "sys_error_no_default_subject";
 		public static final String Cannot_find_request_avatar = "cannot_find_request_avatar";
 	}
 
 	public interface DeviceErrorDictionary {
 
 		public static final String MISSING_DATA = "missing_data";
 		public static final String email_Exist = "email_address_exist";
 		public static final String LoginID_Exist = "loginid_exist";
 		public static final String WrongVeriCode = "wrong_code";
 		public static final String unknownFault = "unknown";
 		public static final String INPUT_DATE_FORMAT_ERROR = "input_data_format_error";
 		public static final String Unauthorized_Access = "unauthorized_access";
 		public static final String Invalid_deviceid = "invalid_deviceid";
 		public static final String Unknown_DeviceID = "unknown_deviceid";
 		public static final String Invalid_ValueType = "invalid_value_type_name";
 		public static final String InputValue_Oversize = "input_over_size";
 		public static final String Hibernate_Internal_Error = "hibernate_internal_error";
 		public static final String HBase_Internal_Error = "hbase_internal_error";
 		public static final String Input_Json_Format_Error = "json_input_syntax_error";
 		public static final String Invalid_Unit_ID = "invalid_unit_id";
 		public static final String Invalid_Datablock_ID = "invalid_datablock_id";
 		public static final String Internal_Fault = "internal_fault";
 		public static final String Invalid_Target_LoginID = "invalid_target_loginid";
 		public static final String already_exist_wildcard_subject = "subject_wildcard_exist";
 		public static final String already_exist_subject_datastream = "subject_datastream_follow_exist";
 		public static final String Unknown_DeviceSerialID = "unknown_device_serial_id";
 		public static final String DeviceBindingAlreadyActived = "device_binding_already_actived";
 		public static final String DeviceBindingNotActived = "device_binding_not_actived";
 		public static final String InvalidDeviceTemplateID = "invalid_device_template_id";
 		public static final String DeviceTemplateParsingError = "device_template_parsing_error";
 		public static final String Invalid_device_serial_id = "Invalid_device_serial_id";
 	}
 }
