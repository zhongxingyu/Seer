 package com.maprice.pointa;
 
public static class PManager {
 
 	private static PManager sInstance;
 
 	private static IPAdProvider sAdInstance;
 	private static IPAnalyticsProvider sAnalyticsInstance;
 	private static IPCrashReportProvider sCrashReportInstance;
 	private static IPServerProvider sServerInstance;
 	private static IPSocialProvider sSocialInstance;
 
 	public PManager sharedInstance(){
 		if(sInstance == null){
 			sInstance = new PManager();
 		}
 		return sInstance;
 	}
 
	public initPManager(Context pContext, boolean enableAds, boolean enableAnalytics, boolean enableCrashReports, boolean enableServer, boolean enableSocial){
 		//Get json object from XML
 		// TODO: read enables/disables from XML maybe?
 		
 		if(enableAds){
 			String lAdProvider = "AdMob";
			sAdInstance = Class.forName(lAdProvider);
 		}
 
 		if(enableAnalytics){	
 			String lAnalyticsProvider = "GoogleAnalytics";
			sAnalyticsInstance = Class.forName(lAnalyticsProvider);
 		}
 
 		if(enableCrashReports){
 			String lCrashReportProvider = "Crashlytics";
			sCrashReportInstance = Class.forName(lAnalyticsProvider);
 		}
 
 		if(enableServer){
 			String lServerProvider = "Parse";
			sServerInstance Class.forName(lServerProvider);
 		}
 
 		if(enableSocial){
 			String lSocialProvider = "Parse";
			sSocialInstance = Class.forName(lSocialProvider);
 		}
 	}
 
 	public IPAdProvider getAd(){
 		if(sAdInstance == null){
 			throw new RuntimeException("Need to specify an Ad provider in PointAConfig.xml");
 		} else {
 			return sAdInstance;
 		}
 	}
 
 	public IPAnalyticsProvider getAnalytics(){
 		if(sAnalyticsInstance == null){
 			throw new RuntimeException("Need to specify an Ad provider in PointAConfig.xml");
 		} else {
 			return sAnalyticsInstance;
 		}
 	}
 
 	public IPCrashReportProvider getCrashReporter(){
 		if(sCrashReportInstance == null){
 			throw new RuntimeException("Need to specify an Ad provider in PointAConfig.xml");
 		} else {
 			return sCrashReportInstance;
 		}
 	}
 
 	public IPServerProvider getServerProvider(){
 		if(sServerInstance == null){
 			throw new RuntimeException("Need to specify an Ad provider in PointAConfig.xml");
 		} else {
 			return sServerInstance;
 		}
 	}
 
 	public IPSocialProvider getSocial(){
 		if(sSocialInstance == null){
 			throw new RuntimeException("Need to specify an Ad provider in PointAConfig.xml");
 		} else {
 			return sSocialInstance;
 		}
 	}
 
 
 
 }
