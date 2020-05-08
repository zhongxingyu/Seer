 /*
 *
 *
 * Author: Konstantin S. Vishnivetsky
 * E-mail: info@siplabs.ru
 * Copyright (C) 2011 SibTelCom, JSC., certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 *
 *
 */
 
 package org.sipfoundry.sipxconfig.phone.yealink;
 
 public class yealinkConstants {
 
     public static final String MIME_TYPE_PLAIN = "text/plain";
     public static final String MIME_TYPE_XML = "text/xml";
 
     public static final String XML_DIAL_NOW = "dialnow.xml";
     public static final String XML_CONTACT_DATA = "directory.xml";
     public static final String WEB_ITEMS_LEVEL = "webitemslevel.cfg";
     public static final String VENDOR = "Yealink";
 
     // Line specific settings used in /etc/yealinkPhone/line.xml
     public static final String USER_ID_V6X_SETTING = "account/UserName";
     public static final String USER_ID_V7X_SETTING = "basic/user_name";
     public static final String AUTH_ID_V6X_SETTING = "account/AuthName";
     public static final String AUTH_ID_V7X_SETTING = "basic/auth_name";
     public static final String DISPLAY_NAME_V6X_SETTING = "account/DisplayName";
     public static final String DISPLAY_NAME_V7X_SETTING = "basic/display_name";
     public static final String PASSWORD_V6X_SETTING = "account/password";
     public static final String PASSWORD_V7X_SETTING = "basic/password";
     public static final String REGISTRATION_SERVER_HOST_V6X_SETTING = "account/SIPServerHost";
     public static final String REGISTRATION_SERVER_HOST_V7X_SETTING = "basic/sip_server_host";
     public static final String REGISTRATION_SERVER_PORT_V6X_SETTING = "account/SIPServerPort";
     public static final String REGISTRATION_SERVER_PORT_V7X_SETTING = "basic/sip_server_port";
     public static final String OUTBOUND_HOST_V6X_SETTING = "account/OutboundHost";
     public static final String OUTBOUND_HOST_V7X_SETTING = "basic/outbound_host";
     public static final String OUTBOUND_PORT_V6X_SETTING = "account/OutboundPort";
     public static final String OUTBOUND_PORT_V7X_SETTING = "basic/outbound_port";
     public static final String BACKUP_OUTBOUND_HOST_V6X_SETTING = "account/BackOutboundHost";
     public static final String BACKUP_OUTBOUND_HOST_V7X_SETTING = "basic/backup_outbound_host";
     public static final String BACKUP_OUTBOUND_PORT_V6X_SETTING = "account/BackOutboundPort";
     public static final String BACKUP_OUTBOUND_PORT_V7X_SETTING = "basic/backup_outbound_port";
     public static final String VOICE_MAIL_NUMBER_V6X_SETTING = "Message/VoiceNumber";
     public static final String VOICE_MAIL_NUMBER_V7X_SETTING = "basic/voice_mail.number";
 
     // Phone specific settings used in /etc/yealinkPhone/phone.xml
     public static final String DNS_SERVER1_V6X_SETTING = "DNS/PrimaryDNS";
     public static final String DNS_SERVER1_V7X_SETTING = "network/network.primary_dns";
     public static final String DNS_SERVER2_V6X_SETTING = "DNS/SecondaryDNS";
     public static final String DNS_SERVER2_V7X_SETTING = "network/network.secondary_dns";
     public static final String LOCAL_TIME_SERVER1_V6X_SETTING = "Time/TimeServer1";
     public static final String LOCAL_TIME_SERVER1_V7X_SETTING = "time/local_time.ntp_server1";
     public static final String LOCAL_TIME_SERVER2_V6X_SETTING = "Time/TimeServer2";
     public static final String LOCAL_TIME_SERVER2_V7X_SETTING = "time/local_time.ntp_server2";
     public static final String LOCAL_TIME_ZONE_V6X_SETTING = "Time/TimeZone";
     public static final String LOCAL_TIME_ZONE_V7X_SETTING = "time/local_time.time_zone";
     public static final String SYSLOG_SERVER_V6X_SETTING = "SYSLOG/SyslogdIP";
     public static final String SYSLOG_SERVER_V7X_SETTING = "network/syslog.server";
     public static final String REMOTE_PHONEBOOK_0_URL_V6X_SETTING = "RemotePhoneBook/URL0";
     public static final String REMOTE_PHONEBOOK_0_URL_V7X_SETTING = "remote-phonebook/remote_phonebook.data.1.url";
     public static final String REMOTE_PHONEBOOK_0_NAME_V6X_SETTING = "RemotePhoneBook/Name0";
     public static final String REMOTE_PHONEBOOK_0_NAME_V7X_SETTING = "remote-phonebook/remote_phonebook.data.1.name";
     public static final String FIRMWARE_SERVER_ADDRESS_SETTING = "firmware/server_ip";
     public static final String FIRMWARE_URL_V6X_SETTING = "firmware/url";
     public static final String FIRMWARE_URL_V7X_SETTING = "downloads/firmware.url";
     public static final String FIRMWARE_HTTP_URL_SETTING = "firmware/http_url";
     public static final String FIRMWARE_NAME_SETTING = "firmware/firmware_name";
     public static final String AUTOPROVISIONING_SERVER_URL_V6X_SETTING = "autoprovision/strServerURL";
     public static final String AUTOPROVISIONING_SERVER_URL_V7X_SETTING = "auto-provisioning/auto_provision.server.url";
     public static final String AUTOPROVISIONING1_SERVER_URL_V7X_SETTING = "auto-provisioning/autoprovision.1.url";
     public static final String AUTOPROVISIONING_SERVER_ADDRESS_SETTING = "autoprovision/server_address";
     public static final String AUTOPROVISIONING1_SERVER_ADDRESS_SETTING = "autoprovision1/Server_address";
     public static final String ADVANCED_MUSIC_SERVER_URI_V6X_SETTING = "account/MusicServerUri";
     public static final String ADVANCED_MUSIC_SERVER_URI_V7X_SETTING = "advanced/music_server_uri";
     public static final String LANG_FILE_NAME_V6X_SETTING = "LangFile/server_address";
     public static final String LANG_FILE_NAME_V7X_SETTING = "downloads/gui_lang.url";
 // T2X except T20
     public static final String LOGO_FILE_NAME_V6X_SETTING = "Logo/server_address";
     public static final String LOGO_FILE_NAME_V7X_SETTING = "downloads/lcd_logo.url";
 // T3X and VP530 only
     public static final String WALLPAPER_FILE_NAME_V6X_SETTING = "Logo/server_address";
     public static final String WALLPAPER_FILE_NAME_V7X_SETTING = "downloads/wallpaper_upload.url";
 // T3X
     public static final String SCREENSAVER_FILE_NAME_V6X_SETTING = "Logo/server_address";
     public static final String SCREENSAVER_FILE_NAME_V7X_SETTING = "downloads/screen_saver.pic.url";
 
    public static final String DIAL_NOW_URL_V6X_SETTING = "DialNow/URL";
     public static final String DIAL_NOW_URL_V7X_SETTING = "downloads/dialplan_dialnow.url";
 
 
 }
