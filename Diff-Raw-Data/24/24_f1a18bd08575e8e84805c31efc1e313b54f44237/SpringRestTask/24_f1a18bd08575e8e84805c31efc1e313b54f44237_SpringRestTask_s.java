 /**
  *
  * Copyright Notice
  *  ----------------
  *
  * The copyright in this document is the property of
  * Bath Institute of Medical Engineering.
  *
  * Without the written consent of Bath Institute of Medical Engineering
  * given by Contract or otherwise the document must not be copied, reprinted or
  * reproduced in any material form, either wholly or in part, and the contents
  * of the document or any method or technique available there from, must not be
  * disclosed to any other person whomsoever.
  *
  *  Copyright 2013-2014 Bath Institute of Medical Engineering.
  * --------------------------------------------------------------------------
  *
  */
 
 package com.projectnocturne.server;
 
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.preference.PreferenceManager;
 
 import com.fasterxml.jackson.core.JsonProcessingException;
 import com.fasterxml.jackson.databind.ObjectMapper;
 import com.fasterxml.jackson.databind.SerializationFeature;
 import com.projectnocturne.NocturneApplication;
 import com.projectnocturne.SettingsActivity;
 import com.projectnocturne.datamodel.DbMetadata;
 import com.projectnocturne.datamodel.DbMetadata.RegistrationStatus;
 import com.projectnocturne.datamodel.RESTResponseMsg;
 import com.projectnocturne.datamodel.SensorReading;
 import com.projectnocturne.datamodel.UserDb;
 
 import org.springframework.http.converter.ByteArrayHttpMessageConverter;
 import org.springframework.http.converter.StringHttpMessageConverter;
 import org.springframework.http.converter.json.GsonHttpMessageConverter;
 import org.springframework.web.client.RestTemplate;
 
 /**
  * @author andy
  */
 public final class SpringRestTask extends AsyncTask<Object, String, RESTResponseMsg> {
 
     public static final String URI_SEND_SENSOR_READING = "/sensors/reading";
     public static final Object URI_USER_CHECK_STATUS = "/users/user_check_status";
     private static final String LOG_TAG = SpringRestTask.class.getSimpleName() + "::";
     public static String URI_ALERTS_RESPOND = "/alerts/respond";
     public static String URI_ALERTS_SEND = "/alerts/send";
     public static String URI_USER_REGISTER = "/users/register";
     public static String URI_USERS_GET = "/users";
     private final Context ctx;
     private final Handler handler;
     private String uri;
     private String url;
 
     public SpringRestTask(final Context contxt, final Handler hndlr) {
         ctx = contxt;
         handler = hndlr;
     }
 
     /*
      * (non-Javadoc)
      *
      * @see android.os.AsyncTask#doInBackground(java.lang.Object[])
      */
     @Override
     protected RESTResponseMsg doInBackground(final Object... params) {
 
         final RequestMethod reqMthd = RequestMethod.valueOf(params[0].toString());
         uri = params[1].toString();
         url = getServerAddress(ctx) + uri;
 
         RESTResponseMsg retStr = null;
         if (uri.equals(URI_USER_REGISTER)) {
             retStr = doUserRegister(reqMthd, url, (UserDb) params[2]);
         } else if (uri.equals(URI_USERS_GET)) {
             // retStr = doUserRegister(reqMthd, url, (User) params[2]);
         } else if (uri.equals(URI_SEND_SENSOR_READING)) {
             retStr = doSendSensorReading(reqMthd, url, (SensorReading) params[2]);
         }
         return retStr;
     }
 
     private RestTemplate getRestTemplate() {
         // Create a new RestTemplate instance
         final RestTemplate restTemplate = new RestTemplate();
 
         // Add the JSON and String message converters
         restTemplate.getMessageConverters().add(new ByteArrayHttpMessageConverter());
         // restTemplate.getMessageConverters().add(new MappingJacksonHttpMessageConverter());
         restTemplate.getMessageConverters().add(new GsonHttpMessageConverter());
         restTemplate.getMessageConverters().add(new StringHttpMessageConverter());
 
         return restTemplate;
     }
 
     /**
      * @param reqMthd
      * @param url
      * @param sensorReading
      * @return
      */
     private RESTResponseMsg doSendSensorReading(final RequestMethod reqMthd, final String url, final SensorReading sensorReading) {
         NocturneApplication.d(LOG_TAG + "doSendSensorReading()");
 
         // Create a new RestTemplate instance
         final RestTemplate restTemplate = getRestTemplate();
 
         // Make the HTTP POST request, marshaling the request to JSON, and the
         // response to a String
         final RESTResponseMsg response = restTemplate.postForObject(url, sensorReading, RESTResponseMsg.class);
 
         return response;
     }
 
     /**
      * @param reqMthd
      * @param url
      * @param user
      */
     private RESTResponseMsg doUserRegister(final RequestMethod reqMthd, final String url, final UserDb user) {
         NocturneApplication.d(LOG_TAG + "doUserRegister()");
 
         String jsonStr = "";
         try {
             final ObjectMapper mapper = new ObjectMapper();
             mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
             jsonStr = mapper.writeValueAsString(user.getUserObj());
         } catch (final JsonProcessingException e1) {
             NocturneApplication.e(LOG_TAG + "doUserRegister() exception converting user to json", e1);
         }
 
         // Create a new RestTemplate instance
         final RestTemplate restTemplate = getRestTemplate();
 
         // Make the HTTP POST request, marshaling the request to JSON, and the
         // response to a String
         RESTResponseMsg response = null;
         Message msg;
         try {
             //response = restTemplate.postForObject("http://rest-service.guides.spring.io/greeting", user.getUserObj(), RESTResponseMsg.class);
 
             response = restTemplate.postForObject(url, user.getUserObj(), RESTResponseMsg.class);
             msg = handler.obtainMessage(DbMetadata.RegistrationStatus_ACCEPTED);
             final Bundle b = new Bundle();
             b.putParcelable("RESTResponseMsg", response);
             msg.setData(b);
             NocturneApplication.getInstance().getDataModel().setRegistrationStatus(RegistrationStatus.REQUEST_ACCEPTED);
             NocturneApplication.d(LOG_TAG + "doUserRegister() success [" + response.toString() + "]");
         } catch (final Exception e) {
             NocturneApplication.e(LOG_TAG + "doUserRegister() exception posting request", e);
             NocturneApplication.getInstance().getDataModel().setRegistrationStatus(RegistrationStatus.REQUEST_DENIED);
             msg = handler.obtainMessage(DbMetadata.RegistrationStatus_DENIED);
             response = new RESTResponseMsg();
            response.setMessage("Server Connection Failed [" + e.getMessage() + "]\nPlease Try Again");
             final Bundle b = new Bundle();
             b.putParcelable("RESTResponseMsg", response);
             msg.setData(b);
         }
         handler.sendMessage(msg);
 
         return response;
     }
 
     private String getServerAddress(final Context ctx) {
         final SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(ctx);
         String serverIpAddr = SettingsActivity.PREF_SERVER_ADDRESS_DEFAULT;
         final String serverAddr = "http://" + settings.getString(SettingsActivity.PREF_SERVER_ADDRESS, serverIpAddr) + ":" + settings.getString(SettingsActivity.PREF_SERVER_PORT, SettingsActivity.PREF_SERVER_PORT_DEFAULT) + "/";
         return serverAddr;
     }
 
     /*
      * (non-Javadoc)
      *
      * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
      */
     @Override
     protected void onPostExecute(final RESTResponseMsg result) {
         NocturneApplication.d(LOG_TAG + "onPostExecute(" + (result == null ? "" : result.getStatus()) + ")");
         super.onPostExecute(result);
     }
 
     /*
      * (non-Javadoc)
      *
      * @see android.os.AsyncTask#onPreExecute()
      */
     @Override
     protected void onPreExecute() {
         super.onPreExecute();
     }
 
     /*
      * (non-Javadoc)
      *
      * @see android.os.AsyncTask#onProgressUpdate(java.lang.Object[])
      */
     @Override
     protected void onProgressUpdate(final String... values) {
         super.onProgressUpdate(values);
     }
 
     public enum RequestMethod {
         GET, POST
     }
 
 }
