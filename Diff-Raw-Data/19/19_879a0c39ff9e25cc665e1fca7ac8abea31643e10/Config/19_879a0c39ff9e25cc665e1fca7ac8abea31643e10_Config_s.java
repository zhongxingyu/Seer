 package com.geniatech.client_box;
 
 public class Config{
 	public static final boolean DEBUG=true;
 	
 	public static final String MULTICAST_HOST = "224.0.0.0";
 	public static final String MULTICAST_SEND_HOS = "192.168.1.255";
 	public static final int MULTICAST_PORT = 65501;
 	public static final int DATA_PORT = 65502;
 	
 	public static final int REPORT_ID_REP_NUM = 3;
 	public static final int REPORT_TIME = 2;
	public static final int WAIT_WIFI_TIME = 20;  //20s
 	public static final int LISTEN_TIME_OUT = 10000; //10s
 	
 	public static final String DEF_AP_SSID = "BOX_ID_1234";
 	public static final String DEF_AP_PASSWORD = "12345678";
 	public static final int DEF_AP_SEC_TYPE = 1;  // 0:open, 1:wpa psk, 2:wpa2 psk
 	
 	/* message format: cmd#boxID=xxx#boxIP=xxx#  */
 	public static final String CMD_BOX_REPORT_ID = "rptID";
 	public static final String CMD_BOX_ID = "boxID=";
 	public static final String CMD_BOX_IP = "boxIP=";
 	
 	public static final String CMD_BOX_SEC_TYPE = "secT=";
 	public static final String CMD_BOX_SSID = "SSID=";
 	public static final String CMD_BOX_PASSWORD = "passw=";
 	
 	public static final String CMD_BOX_WIFI_2_AP = "w2a";
 	public static final String CMD_BOX_AP_2_WIFI = "a2w";
 	
 	public static final String CMD_PHONE_REQUEST_ID = "rqtID";
 	public static final String CMD_PHONE_IP = "phnIP";
 }
