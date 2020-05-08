 package com.eucalyptus.webui.server.dictionary;
 
 public class DBTableColName {
 	public interface USER {
 		public final String ID = "user_id";
 		public final String NAME = "user_name";
 		public final String PWD = "user_pwd";
 		public final String TITLE = "user_title";
 		public final String MOBILE = "user_mobile";
 		public final String EMAIL = "user_email";
 		public final String TYPE = "user_type";
 		public final String STATE = "user_state";
 		public final String REG_STATUS = "user_reg_state";
 		public final String GROUP_ID = "group_id";
 		public final String ACCOUNT_ID = "account_id";
 	}
 
 	public interface USER_KEY {
 		public final String ID = "key_id";
 		public final String AKEY = "key_akey";
 		public final String SKEY = "key_skey";
 		public final String ACTIVE = "key_active";
 		public final String CREATED_DATE = "key_created_date";
 		public final String USER_ID = "user_id";
 	}
 	
 	public interface USER_CERT {
 		public final String ID = "cert_id";
 		public final String CERT_ID = "cert_cert_id";
 		public final String PEM = "cert_pem";
 		public final String ACTIVE = "cert_active";
 		public final String REVOKED = "cert_revoked";
 		public final String CREATED_DATE = "cert_created_date";
 		public final String USER_ID = "user_id";
 	}
 	
 	public interface USER_APP {
 		public final String ID = "ua_id";
 		public final String APP_TIME = "ua_apptime";
 		public final String SRV_STARTINGTIME = "ua_srv_startingtime";
 		public final String SRV_ENDINGTIME = "ua_srv_endingtime";
 		public final String STATUS = "ua_status";
 		public final String DEL = "ua_del";
 		public final String COMMENT = "ua_comment";
 		public final String KEYPAIR = "ua_keypair";
 		public final String SECURITY_GROUP = "ua_security_group";
 		public final String USER_ID = "user_id";
 		public final String TEMPLATE_ID = "template_id";
 		public final String VM_IMAGE_TYPE_ID = "vit_id";
 		public final String EUCA_VI_KEY = "euca_vi_key";
 	}	
 
 	public interface USER_POLICY {
 		public final String ID = "policy_id";
 		public final String NAME = "policy_name";
 		public final String VERSION = "policy_version";
 		public final String TEXT = "policy_text";
 		public final String ACCOUNT_ID = "account_id";
 		public final String GROUP_ID = "group_id";
 		public final String USER_ID = "user_id";
 	}
 
 	public interface ACCOUNT {
 		public final String ID = "account_id";
 		public final String NAME = "account_name";
 		public final String EMAIL = "account_email";
 		public final String DES = "account_descrip";
 		public final String STATE = "account_state";
 	}
 
 	public interface GROUP {
 		public final String ID = "group_id";
 		public final String NAME = "group_name";
 		public final String DESCRIPTION = "group_descrip";
 		public final String STATE = "group_state";
 		public final String ACCOUNT_ID = "account_id";
 	}
 
 	public interface USER_RESET_PWD {
 		public final String ID = "user_reset_pwd_id";
 		public final String CODE = "user_reset_pwd_code";
 		public final String USER_ID = "user_id";
 	}
 
 	public interface SERVER {
 		public final String ID = "server_id";
 		public final String NAME = "server_name";
 		public final String MARK = "server_mark";
 		public final String IMAGE = "server_image";
 		public final String CONF = "server_conf";
 		public final String IP = "server_ip";
 		public final String BW = "server_bw";
 		public final String STATE = "server_state";
 		public final String ROOM = "server_room";
 		public final String STARTTIME = "server_starttime";
 	}
 
 	public interface CPU {
 		public final String ID = "cpu_id";
 		public final String NAME = "cpu_name";
 		public final String VENDOR = "cpu_vendor";
 		public final String MODEL = "cpu_model";
 		public final String GHZ = "cpu_ghz";
 		public final String CACHE = "cpu_cache";
 		public final String SERVER_ID = "server_id";
 	}
 
 	public interface CPU_SERVICE {
 		public final String ID = "cs_id";
 		public final String STARTTIME = "cs_starttime";
 		public final String LIFE = "cs_life";
 		public final String STATE = "cs_state";
 		public final String CPU_ID = "cpu_id";
 		public final String USER_ID = "user_id";
 	}
 	
 	public interface MEMORY {
 		public final String ID = "mem_id";
 		public final String NAME = "mem_name";
 		public final String TOTAL = "mem_total";
 		public final String SERVER_ID = "server_id";
 	}
 	
 	public interface MEM_SERVICE {
 		public final String ID = "ms_id";
 		public final String USED = "ms_used";
 		public final String STARTTIME = "ms_starttime";
 		public final String LIFE = "ms_life";
 		public final String STATE = "ms_state";
 		public final String USER_ID = "user_id";
 		public final String MEM_ID = "mem_id";
 	}
 	
 	public interface DISK {
 		public final String ID = "disk_id";
 		public final String NAME = "disk_name";
 		public final String TOTAL = "disk_total";
 		public final String SERVER_ID = "server_id";
 	}
 	
 	public interface DISK_SERVICE {
 		public final String ID = "ds_id";
 		public final String USED = "ds_used";
 		public final String STARTTIME = "ds_starttime";
 		public final String LIFE = "ds_life";
 		public final String STATE = "ds_state";
 		public final String USER_ID = "user_id";
 		public final String DISK_ID = "disk_id";
 	}
 	
 	public interface IP {
 		public final String ID = "ip_id";
 		public final String ADDR = "ip_addr";
 		public final String TYPE = "ip_type";
 	}
 	
 	public interface IP_SERVICE {
 		public final String ID = "is_id";
 		public final String STARTTIME = "is_starttime";
 		public final String LIFE = "is_life";
 		public final String STATE = "is_state";
 		public final String IP_ID = "ip_id";
 		public final String VM_ID = "vm_id";
 		public final String USER_ID = "user_id";
 	}
 	
 	public interface VM {
 		public final String ID = "vm_id";
 		public final String MARK = "vm_mark";
 	}
 	
 	public interface VM_SERVICE {
 		public final String USER_ID = "user_id"; 
 		public final String VM_ID = "vm_id";
 	}
 	
 	public interface VM_IMAGE_TYPE {
 		public final String ID = "vit_id"; 
 		public final String OS = "vit_os";
 		public final String VER = "vit_ver";
 		public final String DEL = "vit_del";
		public final String EUCA_VIT_ID = "euca_vit_id";
 	}
 	
 	public interface BW_SERVICE {
 		public final String ID = "bs_id";
 		public final String STARTTIME = "bs_starttime";
 		public final String LIFE = "bs_life";
 		public final String IP_ID = "ip_id";
 		public final String USER_ID = "user_id";
 		public final String BANDWIDTH = "bs_bw";
 	}
 	
 	public interface TEMPLATE {
 		public final String ID = "template_id";
 		public final String MARK = "template_mark";
 		public final String CPU = "template_cpu";
 		public final String MEM = "template_mem";
 		public final String DISK = "template_disk";
 		public final String BW = "template_bw";
 		public final String IMAGE = "template_image";
 		public final String STARTTIME = "template_starttime";
 		public final String NCPUS = "template_ncpus";
 	}
 	
 	public interface HISTORY {
 		public final String ID = "history_id";
 		public final String ACTION = "history_action";
 		public final String REASON = "history_reason";
 		public final String DATE = "history_date";
 		public final String USER_ID = "history_user_id";
 		public final String VM_ID = "history_vm_id";
 	}
 }
