 package com.app47.embeddedagent;
 
 import java.util.Date;
 import java.util.Enumeration;
 import java.util.Vector;
 
 import org.json.JSONObject;
 
 
 public class EmbeddedAgent {
 	public static void updateAgent() {
 		ServiceConfigAgent.handleRequest(ServiceConfigAgent.REQ_UPDATE, null);
 	}
 	
 	/**
 	 * Configures the Embedded Agent using String values stored in a resource
 	 * file. Should only be used if the required String values are stored in an
 	 * class in the project
 	 */
 	public static void configureAgent() {
 		ServiceConfigAgent.handleRequest(ServiceConfigAgent.REQ_RES, null);
 	}
 
 	/**
 	 * Configures the Embedded Agent using the provided appID and default values
 	 * for other configuration options
 	 * 
 	 * @param appID
 	 *            Your application's AppID, provided through the web interface
 	 */
 	public static void configureAgentWithAppID(String appID) {
 		ServiceConfigAgent.handleRequest(ServiceConfigAgent.REQ_APP_ID, appID);
 	}
 
 	/**
 	 * Use this method in your Application's onResume() method to set up the
 	 * Embedded Agent
 	 */
 	public static void onResume() {
 		HelperConfigAgent.checkForConfigUpdates();
 		HelperSessionAgent.onResumeSessionCheck();
 	}
 	
 
 
     /**
      * Use this method in your Application's onPause() method to set up the Embedded Agent
      */
     public static void onPause() {
         HelperSessionAgent.onPauseSessionCheck();
         //clear out active timed events
         IOHelperEventsAgent.writeActiveTimedEvents(new JSONObject());
     }
 
 	/**
 	 * Use this method to start a timed event in your application
 	 * 
 	 * @param name - name of the timed event
 	 * @param tags - tags of the timed event to help sort your data
 	 * @return - UUID of the event or null if the Agent is not enabled or there was an error starting the timed event
 	 * 
 	 */
     public static String startTimedEvent(String name, Vector tags) { 
     	return ServiceEventAgent.startTimedEvent(name, tags);
     }
 	 
     /**
 	 * Use this method to start a timed event in your application
 	 * 
 	 * @param name - name of the timed event
 	 * @param tags - tags of the timed event to help sort your data
 	 * @return - UUID of the event or null if the Agent is not enabled or there was an error starting the timed event
 	 */
 	  public static String startTimedEvent(String name, String tags) { 
 		  Vector vect = new Vector();
 		  vect.addElement(tags);
 		  return startTimedEvent(name, vect); 
 	  }
 	  
 	/**
 	 * Use this method to start a timed event in your application
 	 * 
 	 * @param name - name of the timed event
 	 * @return - UUID of the event or null if the Agent is not enabled or there was an error starting the timed event
 	*/  
 	 public static String startTimedEvent(String name) {
 		 Vector vect = new Vector();
 		 return startTimedEvent(name, vect);
 	 }
 	 
      
 	 /**
 	 * Ends a timed event for your application
 	 * 
 	 * @param uuid
 	 *            the UUID of the timed event to stop
 	 */
 	
 	 public static void endTimedEvent(String uuid) { 
 		 ServiceEventAgent.handleRequest(ServiceEventAgent.FINISH_TIMED, null, null, uuid); 
 	 }
 	 
 	 /**
 	 * Use this method to send a generic event for your application
 	 * 
      * @param name - name of the timed event
      * @param tags - tags of the timed event to help sort your data
      * @return - UUID of the event or null if the Agent is not enabled or there was an error starting the timed event
      * 
      */
 	 public static void sendGenericEvent(String name, Vector tags) { 
 		 ServiceEventAgent.handleRequest(ServiceEventAgent.SEND_GENERIC_EVENT,
 				 tags, name , null);
 	 }
 		 
 	 /**
 	  * Use this method to send a generic event for your applicationn
 	  * 
 	  * @param name - name of the timed event
 	  * @param tags - tags of the timed event to help sort your data
 	  * @return - UUID of the event or null if the Agent is not enabled or there was an error starting the timed event
 	  */
 	 public static void sendGenericEvent(String name, String tags) { 
 		 Vector vect = new Vector();
 		 vect.addElement(tags);
 		 ServiceEventAgent.handleRequest(ServiceEventAgent.SEND_GENERIC_EVENT,
 				 vect, name , null);
 	 }
 		  
 	 /**
 	  * Use this method to send a generic event for your application
 	  * 
 	  * @param name - name of the timed event
 	  * @return - UUID of the event or null if the Agent is not enabled or there was an error starting the timed event
 	  */  
 	 public static void sendGenericEvent(String name) {
 		 Vector vect = new Vector();
 		 ServiceEventAgent.handleRequest(ServiceEventAgent.SEND_GENERIC_EVENT,
 				 vect, name , null);
 	 }	 
 	 
 	 public static String[] allKeysForConfigurationGroup(String group){
 	      JSONObject configGroup = IOHelperConfigAgent.loadConfigGroup(group);
 	      if(configGroup == null){
 	        return null;
 	      }
 	      
 	      Vector vect = new Vector();
	      Enumeration ienum = configGroup.keys();
	      while(ienum.hasMoreElements()) {
	    	  vect.addElement(ienum.nextElement());
 	      }
 	      
 	      String[] keys = new String[vect.size()];
 	      for(int i=0; i<vect.size();i++){
 	        keys[i] = (String) vect.elementAt(i);
 	      }
 	      return keys;
 	 }
 
 	 public static String[] configurationGroupNames(){
 	      JSONObject configGroups = IOHelperConfigAgent.loadConfigGroups();
 	      if(configGroups == null){
 	        return null;
 	      }
 	      Vector groupsV = new Vector();
	      Enumeration ienum = configGroups.keys();
	      while(ienum.hasMoreElements()) {
	    	  groupsV.addElement((String)ienum.nextElement());
 	      }
 	      
 	      String[] names = new String[groupsV.size()];
 	      for(int i=0 ; i < groupsV.size() ; i++) {
 	    	names[i] = (String) groupsV.elementAt(i);  
 	      }
 	      
 	      return names;
 	 }
 	 
 	 public static String configurationStringForKey(String key, String group){
 	      return configurationStringForKey(key , group , null);
 	 }
 
 	 public static String configurationStringForKey(String key, String group, String defaultVal){
 	      JSONObject item = HelperConfigAgent.getConfigGroupItem(group, key);
 	      try{
 	        String value = item.getString("value");
 	        String type = item.getString("type");
 	        if(!type.equals("String") || value == null){
 	          return defaultVal;
 	        }else return value;
 	      }catch(Exception e){
 	        return defaultVal;
 	      }
 	 }
 	 
 	 public static Float configurationNumberForKey(String key, String group){
 	      return configurationNumberForKey(key, group, null);
 	 }
 
 	 public static Float configurationNumberForKey(String key, String group, Float defaultVal){
 	            JSONObject item = HelperConfigAgent.getConfigGroupItem(group,key);
 	      try{
 	        Float value = new Float(Float.parseFloat(item.getString("value")));
 	        String type = item.getString("type");
 	        if(!type.equals("Number") || value == null){
 	          return defaultVal;
 	        }else return value;
 	      }catch(Exception e){
 	        return defaultVal;
 	      }
 	 }
 
 	    public static Date configurationDateForKey(String key, String group){
 	      return configurationDateForKey(key,group,null);
 	    }
 
 	    public static Date configurationDateForKey(String key, String group, Date defaultVal){
 	      JSONObject item = HelperConfigAgent.getConfigGroupItem(group,key);
 	      try{
 	        Date value = new Date(Long.parseLong(item.getString("value")));
 	        String type = item.getString("type");
 	        if(!type.equals("Date") || value == null){
 	          return defaultVal;
 	        }else return value;
 	      }catch(Exception e){
 	        return defaultVal;
 	      }
 	    }
 
 
 	    public static Boolean configurationBoolForKey(String key, String group){
 	      return configurationBoolForKey(key,group,null);
 	    }
 
 	    public static Boolean configurationBoolForKey(String key, String group, Boolean defaultVal){
 	            JSONObject item = HelperConfigAgent.getConfigGroupItem(group,key);
 	      try{
 	        String value = item.getString("value");
 	        String type = item.getString("type");
 	        boolean typeCheck = (value.equals("1") || value.toLowerCase().equals("true") || value.toLowerCase().equals("yes"));
 	        if(!type.equals("Yes/No") || value == null){
 	          return defaultVal;
 	        }else return new Boolean(typeCheck);
 	      }catch(Exception e){
 	        return defaultVal;
 	      }
 	    }
 
 
 	    public static Object configurationObjectForKey(String key, String group){
 	      return configurationObjectForKey(key,group,null);
 	    }
 
 	    public static Object configurationObjectForKey(String key, String group, Object defaultVal){
 	            JSONObject item = HelperConfigAgent.getConfigGroupItem(group,key);
 	      try{
 	        Object value = item.get("value");
 	        if(value == null){
 	          return defaultVal;
 	        }else return value;
 	      }catch(Exception e){
 	        return defaultVal;
 	      }
 	    }
 }
