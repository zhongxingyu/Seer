 package com.dbstar.model;
 
 import android.content.Intent;
 
 public class GDCommon {
 	public static final int MSG_TASK_FINISHED = 0x10001;
 	public static final int MSG_MEDIA_MOUNTED = 0x10002;
 	public static final int MSG_MEDIA_REMOVED = 0x10003;
 	public static final int MSG_NETWORK_CONNECT = 0x10004;
 	public static final int MSG_NETWORK_DISCONNECT = 0x10005;
 	public static final int MSG_DISK_SPACEWARNING = 0x10006;
 	public static final int MSG_SYSTEM_UPGRADE = 0x10007;
 	public static final int MSG_SYSTEM_FORCE_UPGRADE = 0x10008;
 	public static final int MSG_USER_UPGRADE_CANCELLED = 0x10009;
 
 	public static final int MSG_ADD_TO_FAVOURITE = 0x10010;
 	public static final int MSG_DELETE = 0x10011;
 	public static final int MSG_PLAY_COMPLETED = 0x10012;
 
 	public static final int MSG_USER_CHANGE_GUIDELIST = 0x10012;
 
 	public static final int MSG_GET_NETWORKINFO = 0x10013;
 	public static final int MSG_SET_NETWORKINFO = 0x10014;
 
 	public static final int MSG_DATA_SIGNAL_STATUS = 0x10015;
 	public static final int STATUS_HASSIGNAL = 0;
 	public static final int STATUS_NOSIGNAL = 1;
 	
 	public static final int SYNC_STATUS_TODBSERVER = 0x10016;
 
 	public static final int MSG_SAVE_BOOKMARK = 0x10017;
 	
 	public static final int MSG_UPDATE_COLUMN = 0x10018;
 	public static final int MSG_UPDATE_PREVIEW = 0x10019;
 	public static final int MSG_UPDATE_UIRESOURCE = 0x10020;
 	
 	public static final int MSG_NEW_MAIL = 0x50001;
 
 	public static final int MSG_DHCP_PRIVATEIP_READY = 0x60001;
 	
 	public static final int MSG_UPDATE_POWERCONSUMPTION = 0x70001;
 	public static final int MSG_UPDATE_POWERTOTALCOST = 0x70002;
 	public static final String KeyPowerConsumption = "number";
 	public static final String KeyPowerTotalCost = "cost";
 	
 	public static final int MSG_DISP_NOTIFICATION = 0x80001;
 	public static final int MSG_HIDE_NOTIFICATION = 0x80002;
 	
 	// Ethernet phy connected/disconnected
 	public static final int MSG_ETHERNET_PHYCONECTED = 0x90001;
 	public static final int MSG_ETHERNET_PHYDISCONECTED = 0x90002;
 
	public static final int OSDDISP_TIMEOUT = 900000;
	
 	public static final String KeyDisk = "disk";
 
 	public static final String LangCN = "cho";
 	public static final String LangEN = "eng";
 
 	public static final String ColumnTypeMovie = "1";
 	public static final String ColumnTypeTV = "2";
 	public static final String ColumnTypePreview = "3";
 	public static final String ColumnTypeMyFavourites = "7";
 	public static final String ColumnTypeRecord = "8";
 	public static final String ColumnTypeEntertainment = "9";
 	public static final String ColumnTypeSettings = "L99";
 	public static final String ColumnTypeUserCenter = "L98";
 	public static final String ColumnTypeSmartLife = "SmartLife";
 
 	public static final String ColumnIDReceiveChooser = "L9801";
 	public static final String ColumnIDDownloadStatus = "L9802";
 
 	public static final String ColumnIDGeneralInfoSettings = "L9901";
 	public static final String ColumnIDMultimediaSettings = "L9902";
 	public static final String ColumnIDNetworkSettings = "L9903";
 	public static final String ColumnIDFileBrowser = "L9904";
 	public static final String ColumnIDAdvancedSettings = "L9905";
 	public static final String ColumnIDSmartcardSettings = "L9906";
 
 	public static final String ColumnIDGuodianSmartPower = "G1";
 	public static final String ColumnIDGuodianHomeEfficiency = "G2";
 	public static final String ColumnIDGuodianSmartHome = "G3";
 	public static final String ColumnIDGuodianNews = "G4";
 	
 	public static final String ColumnIDGuodianMyPower = "G101";
 	public static final String ColumnIDGuodianPowerBill = "G102";
 	public static final String ColumnIDGuodianFeeRecord = "G103";
 	public static final String ColumnIDGuodianPowerNews = "G104";
 	public static final String ColumnIDGuodianBusinessNet = "G105";
 	
 	public static final String KeyMediaData = "media_data";
 	public static final String KeyPackgeFile = "packge_file";
 
 	public static final String KeyPublicationID = "publication_id";
 	public static final String KeyPublicationSetID = "publicationset_id";
 	public static final String KeyBookmark = "bookmark";
 
 	public static final String ActionAddFavourite = "com.dbstar.DbstarLauncher.Action.ADD_TO_FAVOURITE";
 	public static final String ActionDelete = "com.dbstar.DbstarLauncher.Action.DELETE";
 	public static final String ActionUpgradeCancelled = "com.dbstar.DbstarLauncher.Action.UPGRADE_CANCELLED";
 	public static final String ActionBookmark = "com.dbstar.DbstarLauncher.Action.BOOKMARK";
 	public static final String ActionPlayCompleted = "com.dbstar.DbstarDVB.Action.PLAY_COMPLETED";
 	public static final String ActionPlayNext = "com.dbstar.DbstarDVB.Action.PLAY_NEXT";
 	public static final String ActionNoNext = "com.dbstar.DbstarDVB.Action.NO_NEXT";
 
 	public static final String ActionGetNetworkInfo = "com.dbstar.DbstarLauncher.Action.GET_NETWORKINFO";
 	public static final String ActionUpateNetworkInfo = "com.dbstar.DbstarLauncher.Action.UPDATE_NETWORKINFO";
 	public static final String ActionSetNetworkInfo = "com.dbstar.DbstarLauncher.Action.SET_NETWORKINFO";
 
 	public static final String ActionScreenOn = Intent.ACTION_SCREEN_ON;
 	public static final String ActionScreenOff = Intent.ACTION_SCREEN_OFF;
 	
 	// Message to UI for smart card
 	public static final int MSG_SMARTCARD_IN = 0x40001;
 	public static final int MSG_SMARTCARD_OUT = 0x40002;
 	public static final int MSG_SMARTCARD_INSERT_OK = 0x40003;
 	public static final int MSG_SMARTCARD_INSERT_FAILED = 0x40004;
 	public static final int MSG_SMARTCARD_REMOVE_OK = 0x40005;
 	public static final int MSG_SMARTCARD_REMOVE_FAILED = 0x40006;
 	
 	// smart card state
 	public static final int SMARTCARD_STATE_INERTING = 0x1001;
 	public static final int SMARTCARD_STATE_INERTOK = 0x1002;
 	public static final int SMARTCARD_STATE_INERTFAILED = 0x1003;
 	public static final int SMARTCARD_STATE_REMOVING = 0x1004;
 	public static final int SMARTCARD_STATE_REMOVEOK = 0x1005;
 	public static final int SMARTCARD_STATE_REMOVEFAILED = 0x1006;
 	public static final int SMARTCARD_STATE_NONE = 0x1000;
 	
 	// playback event
 	public static final int PLAYBACK_COMPLETED = 0x01;
 	
 }
