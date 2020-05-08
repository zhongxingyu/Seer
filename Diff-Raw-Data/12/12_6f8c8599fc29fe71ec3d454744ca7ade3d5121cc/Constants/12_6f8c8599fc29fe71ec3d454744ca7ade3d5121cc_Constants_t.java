 package com.samteladze.delta.statistics.utils;
 
 public class Constants 
 {
 	public static final String LayoutNextLine = "\r\n";
 	public static final String LayoutEndItem = "$$$\r\n";
 	public static final String LayoutEndSection = "%%%\r\n";
 	public static final String LayoutSeparator = "------------------------------\r\n";
 	
 	public static final String EmptyStr = "";	
 	
 	public static final String DateFormat = "MM/dd/yyyy h:mm:ss aa";	
 	
 	public static final String StatisticsPackageNameText = "Package name: ";
 	public static final String StatisticsAppNameText = "App name: ";
 	public static final String StatisticsVersionNameText = "Version name: ";
 	public static final String StatisticsVersionCodeText = "Version code: ";
 	public static final String StatisticsLastUpdateTimeText = "Last update time: ";
 	public static final String StatisticsNumberOfAppsText = "Number of applications: ";
 	public static final String StatisticsDeviceIDText = "Device ID: ";
 	
 	public static final String StatisticsActiveNetTypeText = "Active network type: ";
 	public static final String StatisticsActiveNetSubtypeText = "Active network subtype: ";
 	public static final String StatisticsActiveNetCoarseStateText = "Active network coarse-grained state: ";
 	public static final String StatisticsActiveNetFineStateText = "Active network fine-grained state: ";
 	public static final String StatisticsWifiNetTypeText = "Network type: ";
 	public static final String StatisticsWifiNetSubtypeText = "Wi-Fi network subtype: ";
 	public static final String StatisticsWifiNetCoarseStateText = "Wi-Fi network coarse-grained state: ";
 	public static final String StatisticsWifiNetFineStateText = "Wi-Fi network fine-grained state: ";
 	public static final String StatisticsMobileNetTypeText = "Network type: ";
 	public static final String StatisticsMobileNetSubtypeText = "Mobile network subtype: ";
 	public static final String StatisticsMobileNetCoarseStateText = "Mobile network coarse-grained state: ";
 	public static final String StatisticsMobileNetFineStateText = "Mobile network fine-grained state: ";
 	public static final String StatisticsNoActiveNetText = "No active network";
 	public static final String StatisticsNoWifiNetText = "No Wi-Fi network";
 	public static final String StatisticsNoMobileNetText = "No Mobile network";
 	public static final String StatisticsLanguageText = "Language: ";
 	public static final String StatisticsCountryText = "Country: ";
 	
 	public static final String StatisticsTimeZoneNameText = "Time zone name: ";
 	public static final String StatisticsTimeZoneIDText = "Time zone ID: ";
 	public static final String StatisticsTimeZoneDSTText = "Daylight savings: ";
 	public static final String StatisticsTimeZoneInDSTText = "Daylight savings apply: ";
 	
 	public static final String StatisticsNone = "none";
 	
 	public static final String NotificationProgressText = "Collecting statistics...";
 	public static final String NotificationFinishText = "Statistics collection finished"; 
 	public static final String NotificationAppNameText = "DELTA Statistics";
 	
 	public static final String MsgNoStatistics = "Statistics for your device has not been collected yet. " +
												 "Please click \"Collect statistics\" button to collect it now.";
 	public static final String MsgEULA = "1. This application is a part of DELTA research project that is done " +
 										 "by Nikolai Samteladze (nsamteladze@mail.usf.edu) at the Department " +
 									 	 "of Computer Science and Engineering at the University of South Florida. " +
 										 "The primary purpose of this application is gathering statistics about the " +
 									 	 "applications installed on your device (such as name, size, installation date, " +
 										 "last update date) and network connectivity (availability of Wi-Fi and Mobile " +
 									 	 "networks). Application also collects your device ID and your default country and " +
 										 "language. For the best of our knowledge, it is impossible to determine the device's " +
 									 	 "user or any other personal information based on the collected statistics. " +
 										 "The collected statistics will be further used for research purposes. Collected " +
 									 	 "statistics can be obtained using <E-mail statistics> button in the application or " +
 										 "by e-mail request to <delta.satistics@gmail.com>. " + "\r\n" + "\r'n" +
 										 "2. This application is provided to you, the end user, by the application creator, " +
 										 "Nikolai Samteladze, as is. The application is provided free of charge by the developer. " +
 										 "However, the means of delivering the application to your device may include a service " +
 										 "fee. The developer, Nikolai Samteladze, or the University of South Florida and any of its " +
 										 "employees or students cannot be held responsible for any damage or loss that you may " +
 										 "suffer by the use or installation of this application to your device. Use at your own " +
 										 "risk." + "\r\n" + "\r\n" +
 										 "By clicking OK you agree to all the terms of the agreement above and allow the developer " +
 										 "to use the collected data from your device for research purposes.";
 	public static final String MsgTitleEULA = "End User License Agreement";
 	public static final String MsgCollectionInProcess = "Statistics is currently collecting. Please wait for this process to finish.";
 	public static final String MsgAppTitle = "DELTA Statistics";
 	public static final String MsgAppWelcome = "Welcome to DELTA Statistics!";
 	public static final String MsgFirstRun = 	"This application will automatically collect statistics about " +
 												"the installed applications and networks connectivity on your device. " +
												"You can get the collected statistics by clicking on the \"E-mail statistics\" " +
 												"button.";
 }
