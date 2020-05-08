 package com.malcom.library.android.module.notifications.gcm;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Random;
 import java.util.TimeZone;
 
 import android.content.Context;
 import android.content.Intent;
 import android.util.Log;
 
 import com.google.android.gcm.GCMRegistrar;
 import com.google.gson.Gson;
 import com.google.gson.GsonBuilder;
 import com.malcom.library.android.module.core.MCMCoreAdapter;
 import com.malcom.library.android.module.notifications.EnvironmentType;
 import com.malcom.library.android.module.notifications.MCMNotificationModule;
 import com.malcom.library.android.module.notifications.NotificationAck;
 import com.malcom.library.android.module.notifications.NotificationRegistration;
 import com.malcom.library.android.module.notifications.services.PendingAcksDeliveryService;
 import com.malcom.library.android.utils.HttpDateUtils;
 import com.malcom.library.android.utils.MalcomHttpOperations;
 import com.malcom.library.android.utils.ToolBox;
 import com.malcom.library.android.utils.ToolBox.HTTP_METHOD;
 import com.malcom.library.android.utils.encoding.DigestUtils;
 import com.malcom.library.android.utils.encoding.base64.Base64;
 
 
 /**
  * Helper class used to communicate with the Malcom server.
  * 
  * - Device registration
  * - Device un-registration
  * - ACKs
  * 
  * @author	Malcom Ventures, S.L.
  * @since	2012
  */
 public final class MalcomServerUtilities {
 	
 	//private enum HTTP_METHOD{POST,DELETE};
     private static final int MAX_ATTEMPTS = 5;
     private static final int MAX_ATTEMPTS_UNREG = 5;
     private static final int BACKOFF_MILLI_SECONDS = 2000;
     private static final Random random = new Random();
 
     private static final String PARAM_DEVICE_REGID = "regId";
     private static final String PARAM_DEVICEUDID = "deviceUdid";
     private static final String PARAM_NOTIFICATION_ID = "notificationId";
     private static final String PARAM_NOTIFICATION_SEGMENT_ID = "segmentId";
     private static final String PARAM_ENVIRONMENT = "environment";
     private static final String PARAM_APPLICATION_CODE = "appCode";
     private static final String PARAM_APPLICATION_SECRETKEY = "appSecretKey";
     private static final String PARAM_APPLICATION_ENVIRONMENT_TYPE = "appEnvironmentType";
     
     
     /**
      * Register this account/device pair within the server.
      *
      * @return whether the registration succeeded or not.
      * 
      * @param context
      * @param regId
      * @param environment
      * @param appCode
      * @param appSecretKey
      * @return
      */
     public static boolean register(final Context context, final String regId, EnvironmentType environment, 
     							   final String appCode, final String appSecretKey) {
         Log.i(MCMNotificationModule.TAG, "Registering device (regId = " + regId + ") ...");
         
         long backoff = BACKOFF_MILLI_SECONDS + random.nextInt(1000);
         String serverUrl = MCMCoreAdapter.getInstance().coreGetProperty(MCMCoreAdapter.PROPERTIES_MALCOM_BASEURL) + MCMNotificationModule.notification_registry;
         
         Map<String, String> params = new HashMap<String, String>();        
         params.put(PARAM_DEVICE_REGID, regId);
         params.put(PARAM_DEVICEUDID,ToolBox.device_getId(context));
         params.put(PARAM_ENVIRONMENT, environment.name());        
         params.put(PARAM_APPLICATION_CODE, appCode);
         params.put(PARAM_APPLICATION_SECRETKEY, appSecretKey);
         
         // Once GCM returns a registration id, we need to register it in Malcom 
         // server. As the server might be down, we will retry it a couple
         // times.
         for (int i = 1; i <= MAX_ATTEMPTS; i++) {
         	if(!GCMRegistrar.isRegistered(context)){
         		break; //Is not registered, no sense doing registration.
         	}
         	
             Log.d(MCMNotificationModule.TAG, "Attempt #" + i + " to register");
             try {
             	Log.d(MCMNotificationModule.TAG,"Registering in Malcom ("+i+"/"+MAX_ATTEMPTS+")...");
             	serverDoRegister(serverUrl, params);            	
                 GCMRegistrar.setRegisteredOnServer(context, true);
                 Log.d(MCMNotificationModule.TAG,"Device successfully registered in Malcom.");                
                 return true;
             } catch (Exception e) {
                 Log.e(MCMNotificationModule.TAG, "Failed to register on attempt " + i, e);
                 if (i == MAX_ATTEMPTS) {
                     break;
                 }
                 
                 try {
                     Log.d(MCMNotificationModule.TAG, "Sleeping for " + backoff + " ms before retry");
                     Thread.sleep(backoff);
                 } catch (InterruptedException e1) {
                     // Activity finished before we complete - exit.
                     Log.d(MCMNotificationModule.TAG, "Thread interrupted: abort remaining retries!");
                     Thread.currentThread().interrupt();
                     return false;
                 }
                 // increase backoff exponentially
                 backoff *= 2;
             }
         }
         
         if(!GCMRegistrar.isRegistered(context)){
         	Log.e(MCMNotificationModule.TAG,"Device registration with Malcom aborted. Device not registered!");
         }else{
         	Log.e(MCMNotificationModule.TAG,"Device registration with Malcom failed!");
         }
         return false;
     }
 
     /**
      * Unregister this account/device pair within the server.
      * 
      * @param context
      * @param regId
      * @param appCode
      * @param appSecretKey
      * whether the un-registration succeeded or not.
      */
     public static boolean unregister(final Context context, final String regId,
     							  final String appCode, final String appSecretKey) {
         Log.i(MCMNotificationModule.TAG, "Unregistering device from Malcom (regId = " + regId + ") ...");
         
         long backoff = BACKOFF_MILLI_SECONDS + random.nextInt(1000);
         
         for (int i = 1; i <= MAX_ATTEMPTS_UNREG; i++) {
         	if(GCMRegistrar.isRegistered(context)){
         		break; //Is registered, no sense doing un-registration.
         	}
         	
         	Log.d(MCMNotificationModule.TAG, "Attempt #" + i + " to unregister");
         	
         	try {
             	Map<String, String> params = new HashMap<String, String>();
                 params.put(PARAM_APPLICATION_CODE, appCode);
                 params.put(PARAM_APPLICATION_SECRETKEY, appSecretKey);
                 params.put(PARAM_DEVICEUDID, ToolBox.device_getId(context));
                 
                 //Set the unregistration URL for later usage.
     			String serverUrl = MCMCoreAdapter.getInstance().coreGetProperty(MCMCoreAdapter.PROPERTIES_MALCOM_BASEURL) + MCMNotificationModule.notification_deregister;
     			serverUrl=serverUrl.replaceAll(MCMNotificationModule.notification_deregister_param_appCode, appCode);
                 serverUrl=serverUrl.replaceAll(MCMNotificationModule.notification_deregister_param_udid, ToolBox.device_getId(context));
                 
                 System.out.println("Unregister url: "+serverUrl);
     			
                 Log.d(MCMNotificationModule.TAG,"Un-Registering in Malcom ("+i+"/"+MAX_ATTEMPTS+")...");
             	serverDoUnRegister(serverUrl, params);        	
                 GCMRegistrar.setRegisteredOnServer(context, false);
                 Log.d(MCMNotificationModule.TAG,"Device successfully un-registered from Malcom.");
                 return true;
             } catch (Exception e) {            
             	Log.e(MCMNotificationModule.TAG, "Failed to un-register on attempt " + i, e);
                 if (i == MAX_ATTEMPTS) {
                     break;
                 }
                 
                 try {
                     Log.d(MCMNotificationModule.TAG, "Sleeping for " + backoff + " ms before retry");
                     Thread.sleep(backoff);
                 } catch (InterruptedException e1) {
                     // Activity finished before we complete - exit.
                     Log.d(MCMNotificationModule.TAG, "Thread interrupted: abort remaining un-registration retries!");
                     Thread.currentThread().interrupt();
                     return false;
                 }
                 // increase backoff exponentially
                 backoff *= 2;
             }
         }
         
         if(GCMRegistrar.isRegistered(context)){
         	Log.i(MCMNotificationModule.TAG,"Device un-registration with Malcom aborted!");
         }else{
         	Log.e(MCMNotificationModule.TAG,"Device un-registration with Malcom failed!");
         }
         return false;
     }
     
     
     /**
      * Sends the ACK to the server.
      * 
      * @param context
      * @param notId
      * @param segmentId
      * @param environmentType
      * @param appCode
      * @param appSecretKey
      */
     public static void doAck(final Context context, final String notId, final String segmentId,
 			  				 final String environmentType, final String appCode, final String appSecretKey){
     	
     	Log.i(MCMNotificationModule.TAG, "Doing ACK (notId = " + notId + ") ...");
         
         try {
         	Map<String, String> params = new HashMap<String, String>();
             params.put(PARAM_APPLICATION_CODE, appCode);
             params.put(PARAM_APPLICATION_SECRETKEY, appSecretKey);
             params.put(PARAM_DEVICEUDID,ToolBox.device_getId(context));
             params.put(PARAM_NOTIFICATION_ID,notId);
             params.put(PARAM_NOTIFICATION_SEGMENT_ID,segmentId);
             params.put(PARAM_APPLICATION_ENVIRONMENT_TYPE,environmentType);
             
             //Set the unregistration URL for later usage.
			String serverUrl = MCMCoreAdapter.SERVER_URL + MCMNotificationModule.notification_ack;			
 			
 			serverDoAck(context, serverUrl, params);
         	
             Log.d(MCMNotificationModule.TAG,"Notification ACK successfully done.");            
         } catch (Exception e) {
            Log.e(MCMNotificationModule.TAG, "Failed to ACK!.",e);
         }
     	
     	
     }
     
     
     
     //AUXILIAR METHODS
     
     
    /*
     * Makes the registration POST request to the Malcom API.
     *
     * @param endpoint 	POST address.
     * @param params 	request parameters.
     *
     * @throws IOException propagated from POST.
     */
     private static void serverDoRegister(String endpoint, Map<String, String> params) throws Exception {
     	
         URL url;
         try {
             url = new URL(endpoint);
         } catch (MalformedURLException e) {
             throw new IllegalArgumentException("invalid url: " + endpoint);
         }
                         
         String jsonBody = null;
         
         //Prepare the registration Object and get the JSON for the body
         NotificationRegistration registration = new NotificationRegistration();
         registration.setEnvironment(params.get(PARAM_ENVIRONMENT));
         registration.setToken(params.get(PARAM_DEVICE_REGID));
         registration.setUdid(params.get(PARAM_DEVICEUDID));
         registration.setApplicationCode(params.get(PARAM_APPLICATION_CODE));       
         
         //...get the JSON body from the object using Google JSON library
         Gson gson = new GsonBuilder().disableHtmlEscaping().create();
         jsonBody = "{\"NotificationRegistration\":" + gson.toJson(registration) + "}";
                 
         Log.v(MCMNotificationModule.TAG, "Sending device registration body: '" + jsonBody + "' to " + url);
         
 		
         String appCode = params.get(PARAM_APPLICATION_CODE);
 		String appSecretKey = params.get(PARAM_APPLICATION_SECRETKEY);
 		
 		MalcomHttpOperations.sendPostToMalcom(endpoint, "/v3/notification/registry/application", jsonBody, appCode, appSecretKey);
 		
     }
     
     
    /*
     * Makes the un-registration (DELETE) request to the Malcom API.
     *
     * @param endpoint 	DELETE address.
     * @param params 	request parameters.
     *
     * @throws IOException propagated from POST.
     */
     private static void serverDoUnRegister(String endpoint, Map<String, String> params) throws Exception {
     	
         URL url;
         try {	
             url = new URL(endpoint);
         } catch (MalformedURLException e) {
             throw new IllegalArgumentException("invalid url: " + endpoint);
         }
         
         Log.v(MCMNotificationModule.TAG, "Sending device un-registration request to " + url);        
                 
         try { 
         	//Prepare required data for headers, these headers are requested by Malcom API.
             String malcomDate = HttpDateUtils.formatDate(new Date());
             String headers = "x-mcm-date:" + malcomDate+"\n";
 			
             //For unregistration i must pass the final url with corresponding application and udid.
             //(and also add "/malcom-api/" before the endpoint of the service, why this?)
             String resource = "/"+MCMNotificationModule.notification_deregister;
             resource=resource.replaceAll(MCMNotificationModule.notification_deregister_param_appCode, params.get(PARAM_APPLICATION_CODE));
             resource=resource.replaceAll(MCMNotificationModule.notification_deregister_param_udid, params.get(PARAM_DEVICEUDID));
             System.out.println("Resource: "+resource);
 			String password = ToolBox.deliveries_getDataToSign(headers, null, null, "DELETE", resource, null);				
 			password = DigestUtils.calculateRFC2104HMAC(password, params.get(PARAM_APPLICATION_SECRETKEY));
 			            
 			Map<String, String> headersData = new HashMap<String, String>();
 			headersData.put("Authorization", "basic " + new String(Base64.encode(new String(params.get(PARAM_APPLICATION_CODE) + ":" + password).getBytes())));
 			headersData.put("x-mcm-date", malcomDate);
 			
 			System.out.println("Endpoint al desregistrar: "+endpoint);
 			
 			ToolBox.net_httpclient_doAction(HTTP_METHOD.DELETE, endpoint, null, headersData);
         
         } catch(Exception e) {
         	Log.e(MCMNotificationModule.TAG, "Error sending un-registration data to Malcom service url '"+url.toString()+"': "+e.getMessage(),e);
 			throw e;
             
         } 
     }
     
     /*
      * Makes the ACK request (POST) to Malcom API.
      *  
      * @param endpoint
      * @param params
      * @throws Exception
      */
     private static void serverDoAck(final Context context, String endpoint, Map<String, String> params) throws Exception {
     	
         URL url;
         try {
             url = new URL(endpoint);
         } catch (MalformedURLException e) {
             throw new IllegalArgumentException("invalid url: " + endpoint);
         }
                         
         String jsonBody = null;
         Date date = new Date();
         
         //Prepare the registration Object and get the JSON for the body
         NotificationAck ack = new NotificationAck();
         ack.setApplicationCode(params.get(PARAM_APPLICATION_CODE));
         ack.setUdid(params.get(PARAM_DEVICEUDID));
         ack.setId(Long.valueOf(params.get(PARAM_NOTIFICATION_ID)));
         if(params.get(PARAM_NOTIFICATION_SEGMENT_ID)!=null)
         	ack.setSegmentId(Long.valueOf(params.get(PARAM_NOTIFICATION_SEGMENT_ID)));
         ack.setEnvironment(params.get(PARAM_APPLICATION_ENVIRONMENT_TYPE));        
         ack.setCreated(formatDate(date));
         ack.setAckDate(formatDate(date));
         
         //...get the JSON body from the object using Google JSON library
         Gson gson = new GsonBuilder().disableHtmlEscaping().create();
         jsonBody = "{\"notificationReceipt\":" + gson.toJson(ack) + "}";
         
         if(!ToolBox.network_haveNetworkConnection(context)){
         	Log.v(MCMNotificationModule.TAG, "Sending ACK aborted. No network available. Caching for later delivery.");
         	//Saves the failed delivered ack to disk.
         	cacheAck(context,jsonBody);
         }else{
         
 	        Log.v(MCMNotificationModule.TAG, "Sending ACK body: '" + jsonBody + "' to " + url);
 	        
 	        try {
 	        	String appCode = params.get(PARAM_APPLICATION_CODE);
 				String appSecretKey = params.get(PARAM_APPLICATION_SECRETKEY);
 				
 				MalcomHttpOperations.sendPostToMalcom(endpoint, MCMNotificationModule.notification_ack, jsonBody, appCode, appSecretKey);
 	        	
 	        } catch(Exception e) {
 	        	Log.e(MCMNotificationModule.TAG, "Error sending ACK to Malcom service url '"+url.toString()+"': "+e.getMessage(),e);	        	
 	        	//Saves the failed delivered ack to disk.
 	        	cacheAck(context,jsonBody);
 				throw e;
 	        } 
         }
         
         //launches the service to send pending deliveries.
     	Intent ackSvcIntent = new Intent(context, PendingAcksDeliveryService.class);
     	context.startService(ackSvcIntent);
     }
     
     public static final String DEFAULT_DATE_HOUR_FORMAT = "yyyy-MM-dd'T'HH:mm";
     
     private static String formatDate(Date date){
     	SimpleDateFormat f = new SimpleDateFormat(DEFAULT_DATE_HOUR_FORMAT);
     	f.setTimeZone(TimeZone.getTimeZone("UTC"));
     	return (f.format(new Date()));
     }
     
     
     /*
      * Saves the ACK to disk for later delivery.
      *  
      * @param context
      * @param ackData
      */
     private static synchronized void cacheAck(Context context, String ackData){
 		//Save the beacon for later send
 		try {
 			String name = MCMNotificationModule.CACHED_ACK_FILE_PREFIX +DigestUtils.md5Hex(ackData.getBytes());
 			ToolBox.storage_storeDataInInternalStorage(context, name, ackData.getBytes());
 		} catch (Exception e) {
 			Log.e(MCMNotificationModule.TAG,"Error saving the beacon for later delivery ("+e.getMessage()+")",e);
 		}
 	}
     
 }
