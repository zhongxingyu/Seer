 package com.gentics.cr.monitoring;
 
 import com.gentics.cr.configuration.GenericConfiguration;
 
 public class MonitorFactory {
 
 	private static boolean monitorenabled = false;
 	private static final String MONITOR_ENABLE_KEY="monitoring";
 	
 	public static synchronized void init(GenericConfiguration config)
 	{
 		String s_mon = config.getString(MONITOR_ENABLE_KEY);
 		if(s_mon!=null)
 		{
 			monitorenabled = Boolean.parseBoolean(s_mon);
 		}
 	}
 	
 	public static UseCase startUseCase(String identifyer)
 	{
 		if(monitorenabled)
 		{
 			return new UseCase(com.jamonapi.MonitorFactory.start(identifyer),monitorenabled);
 		}
		return new UseCase(null,monitorenabled);
 		
 	}
 }
