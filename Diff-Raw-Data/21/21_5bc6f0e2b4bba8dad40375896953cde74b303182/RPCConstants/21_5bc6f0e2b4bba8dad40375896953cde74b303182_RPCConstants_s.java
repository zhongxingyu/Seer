 /**
  * 
  */
 package it.infn.chaos.mds;
 
 /**
  * @author bisegni
  */
 public class RPCConstants {
 	// comamnd description constant
 	// key for action domain definition
 	public static final String	CS_CMDM_ACTION_DOMAIN					= "cs|cmdm|act_domain";
 
 	// key for action name definition
 	public static final String	CS_CMDM_ACTION_NAME						= "cs|cmdm|act_name";
 
 	// !key for action message
 	public static final String	CS_CMDM_ACTION_MESSAGE					= "cs|cmdm|act_msg";
 	
 	// !key for the specify the id of the request(the current message is an
 	// asnwer)
 	public static final String	CS_CMDM_MESSAGE_ID						= "cs|cmdm|act_msg_id";
 
 	// key action submission result
	public static final String	CS_CMDM_ACTION_SUBMISSION_RESULT		= "cs|cmdm|act_sub_result";
 
 	// key action submission error message
 	public static final String	CS_CMDM_ACTION_SUBMISSION_ERROR_MESSAGE	= "cs|cmdm|act_sub_emsg";
 
 	// key action submission error domain
 	public static final String	CS_CMDM_ACTION_SUBMISSION_ERROR_DOMAIN	= "cs|cmdm|act_sub_edom";
 
 	// key action submission error code
 	public static final String	CS_CMDM_ACTION_SUBMISSION_ERROR_CODE	= "cs|cmdm|act_sub_ecode";
 
 	// key
 	public static final String	CS_CMDM_REMOTE_HOST_IP					= "cs|cmdm|rh_ip";
 
 	public static final String	CS_CMDM_ANSWER_DOMAIN					= "cs|cmdm|rh_ans_domain";
 	// !key for the ip where to send the answer
 	public static final String	CS_CMDM_ANSWER_ACTION					= "cs|cmdm|rh_ans_action";
 	// key
 	public static final String	CS_CMDM_REMOTE_HOST_ANSWER_IP			= "cs|cmdm|rh_ans_ip";
 
 	// key
 	public static final String	CS_CMDM_REMOTE_HOST_ANSWER_MESSAGE_ID	= "cs|cmdm|rh_ans_msg_id";
 
 	// dataset
 	public static final String	DATASET_DEVICE_ID						= "cs|cm|ds_attr_dom";
 	public static final String	DATASET_DESCRIPTION						= "cs|cm|ds_desc";
 	public static final String	DATASET_ATTRIBUTE_NAME					= "cs|cm|ds_attr_name";
 	public static final String	DATASET_ATTRIBUTE_DESCRIPTION			= "cs|cm|ds_attr_desc";
 	public static final String	DATASET_ATTRIBUTE_DIRECTION				= "cs|cm|ds_attr_dir";
 	public static final String	DATASET_ATTRIBUTE_TYPE					= "cs|cm|ds_attr_type";
 	public static final String	DATASET_ATTRIBUTE_DEFAULT_VALUE			= "cs|cmdm|act_desc_par_default_value";
 	public static final String	DATASET_ATTRIBUTE_MAX_RANGE				= "cs|cmdm|act_desc_par_max_range";
 	public static final String	DATASET_ATTRIBUTE_MIN_RANGE				= "cs|cmdm|act_desc_par_min_range";
 	public static final String	CONTROL_UNIT_INSTANCE					= "cs|cm|cu_instance";
 	public static final String	CONTROL_UNIT_INSTANCE_NETWORK_ADDRESS	= "cs|cm|cu_instance_net_address";
 	public static final String	CONTROL_UNIT_NAME						= "cs|cm|cu_name";
 	public static final String	CONTROL_UNIT_AUTOSTART					= "cs|cm|cu|autostart";
 
 	public static final String	DEVICE_SCHEDULE_DELAY					= "cs|cm|cu|thr|sch_del";
 
 	public static final String	LIVE_DATASERVER_HOST_PORT				= "cs|dm|ld|server_address";
 }
