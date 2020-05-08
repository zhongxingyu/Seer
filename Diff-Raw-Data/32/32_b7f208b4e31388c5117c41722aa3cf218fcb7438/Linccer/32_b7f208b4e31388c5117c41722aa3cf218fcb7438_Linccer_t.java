 /*******************************************************************************
  * Copyright (C) 2009, 2010, Hoccer GmbH Berlin, Germany <www.hoccer.com> These coded instructions,
  * statements, and computer programs contain proprietary information of Hoccer GmbH Berlin, and are
  * copy protected by law. They may be used, modified and redistributed under the terms of GNU
  * General Public License referenced below. Alternative licensing without the obligations of the GPL
  * is available upon request. GPL v3 Licensing: This file is part of the "Linccer Java-API". Linccer
  * Java-API is free software: you can redistribute it and/or modify it under the terms of the GNU
  * General Public License as published by the Free Software Foundation, either version 3 of the
  * License, or (at your option) any later version. Linccer Java-API is distributed in the hope that
  * it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
  * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details. You
  * should have received a copy of the GNU General Public License along with Linccer Java-API. If
  * not, see <http://www.gnu.org/licenses/>.
  ******************************************************************************/
 package com.hoccer.api;
 
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Date;
 import java.util.List;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.ParseException;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.methods.HttpDelete;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPut;
 import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.HTTP;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.util.Log;
 
 public class Linccer extends CloudService {
 
     private Environment        mEnvironment                  = new Environment();
     private EnvironmentStatus  mEnvironmentStatus;
     private boolean            mAutoSubmitEnvironmentChanges = true;
 
     private HttpGet            mPeekRequest                  = null;
     protected volatile boolean mPeekStopped                  = false;
     private Object             mPeekLock                     = new Object();
 
     public Linccer(ClientConfig config) {
         super(config);
     }
 
     @Override
     protected void finalize() throws Throwable {
         disconnect();
         super.finalize();
     }
 
     public void disconnect() throws UpdateException {
         HttpResponse response;
         try {
             String uri = mConfig.getClientUri() + "/environment";
             HttpDelete request = new HttpDelete(sign(uri));
             response = getHttpClient().execute(request);
         } catch (Exception e) {
             throw new UpdateException("could not update gps measurement for "
                     + mConfig.getClientUri() + " because of " + e);
         }
 
         if (response.getStatusLine().getStatusCode() != 200) {
             throw new UpdateException(
                     "could not delete environment because server responded with status "
                             + response.getStatusLine().getStatusCode());
         }
     }
 
     private void onEnvironmentChanged(Environment environment) throws UpdateException,
             ClientProtocolException, IOException {
         mEnvironment = environment;
 
         if (mAutoSubmitEnvironmentChanges) {
             submitEnvironment();
         }
     }
 
     public void submitEnvironment() throws UpdateException, ClientProtocolException, IOException {
         HttpResponse response;
         long startTime = 0;
         try {
             String uri = mConfig.getClientUri() + "/environment";
             HttpPut request = new HttpPut(sign(uri));
            request.setEntity(new StringEntity(mEnvironment.toJson().toString(), HTTP.UTF_8));
             startTime = System.currentTimeMillis();
             Log.v("Linncer", "submit environment uri = " + uri);
             Log.v("Linncer", "submit environment = " + mEnvironment.toJson().toString());
             response = getHttpClient().execute(request);
         } catch (JSONException e) {
             mEnvironmentStatus = null;
             throw new UpdateException("could not update gps measurement for "
                     + mConfig.getClientUri() + " because of " + e);
         } catch (UnsupportedEncodingException e) {
             mEnvironmentStatus = null;
             throw new UpdateException("could not update gps measurement for "
                     + mConfig.getClientUri() + " because of " + e);
         }
 
         if (response.getStatusLine().getStatusCode() != 201) {
             try {
                 mEnvironmentStatus = null;
                 throw new UpdateException(
                         "could not update environment because server responded with "
                                 + response.getStatusLine().getStatusCode() + ": "
                                 + convertResponseToString(response));
             } catch (ParseException e) {
             } catch (IOException e) {
             }
             throw new UpdateException("could not update environment because server responded with "
                     + response.getStatusLine().getStatusCode() + " and an unparsable body");
         }
 
         try {
             mEnvironmentStatus = new EnvironmentStatus(convertResponseToJsonObject(response));
         } catch (Exception e) {
             mEnvironmentStatus = null;
             throw new UpdateException("could not update environment because server responded with "
                     + response.getStatusLine().getStatusCode() + " and an ill formed body: "
                     + e.getMessage());
         }
         int latency = (int) (System.currentTimeMillis() - startTime);
 
         mEnvironment.setNetworkLatency(latency);
     }
 
     /**
      * checks network latency in milliseconds writes it to the environment
      */
     // public int measureNetworkLatency() {
     // String uri = mConfig.getClientUri();
     // HttpHead request = new HttpHead(sign(uri));
     // long startTime = System.currentTimeMillis();
     // try {
     // HttpResponse response = getHttpClient().execute(request);
     // if (response == null || response.getStatusLine().getStatusCode() != 200) {
     // return -1;
     // }
     // } catch (ClientProtocolException e) {
     // return -2;
     // } catch (IOException e) {
     // return -3;
     // } catch (Exception e) {
     // return -4;
     // }
     //
     // int latency = (int) (System.currentTimeMillis() - startTime);
     //
     // mEnvironment.setNetworkLatency(latency);
     //
     // return latency;
     // }
 
     public void onGpsChanged(double latitude, double longitude, int accuracy)
             throws UpdateException, ClientProtocolException, IOException {
         onGpsChanged(latitude, longitude, accuracy, new Date());
     }
 
     public void onGpsChanged(double latitude, double longitude, int accuracy, Date date)
             throws UpdateException, ClientProtocolException, IOException {
         mEnvironment.setGpsMeasurement(latitude, longitude, accuracy, date);
         onEnvironmentChanged(mEnvironment);
     }
 
     public void onGpsChanged(double latitude, double longitude, int accuracy, long time)
             throws UpdateException, ClientProtocolException, IOException {
         onGpsChanged(latitude, longitude, accuracy, new Date(time));
     }
 
     public void onNetworkChanged(double latitude, double longitude, int accuracy)
             throws UpdateException, ClientProtocolException, IOException {
         onNetworkChanged(latitude, longitude, accuracy, new Date());
     }
 
     public void onNetworkChanged(double latitude, double longitude, int accuracy, Date date)
             throws UpdateException, ClientProtocolException, IOException {
         mEnvironment.setNetworkMeasurement(latitude, longitude, accuracy, date);
         onEnvironmentChanged(mEnvironment);
     }
 
     public void onNetworkChanged(double latitude, double longitude, int accuracy, long time)
             throws UpdateException, ClientProtocolException, IOException {
         onNetworkChanged(latitude, longitude, accuracy, new Date(time));
     }
 
     public void onWifiChanged(List<String> bssids) throws UpdateException, ClientProtocolException,
             IOException {
         mEnvironment.setWifiMeasurement(bssids, new Date());
         onEnvironmentChanged(mEnvironment);
     }
 
     public void onClientNameChanged(String newClientName) throws UpdateException,
             ClientProtocolException, IOException {
         mEnvironment.setClientName(newClientName);
         onEnvironmentChanged(mEnvironment);
     }
 
     public String getClientName() {
         return mEnvironment.getClientName();
     }
 
     public void onWifiChanged(String[] bssids) throws UpdateException, ClientProtocolException,
             IOException {
         onWifiChanged(Arrays.asList(bssids));
     }
 
     public void onSelectedClientsChanged(ArrayList<String> selectedClients) throws UpdateException,
             ClientProtocolException, IOException {
         mEnvironment.setSelectedClients(selectedClients);
         onEnvironmentChanged(mEnvironment);
     }
 
     public JSONObject share(String mode, JSONObject payload) throws BadModeException,
             ClientActionException, CollidingActionsException {
         return share(mode, "", payload);
     }
 
     public JSONObject share(String mode, String options, JSONObject payload)
             throws BadModeException, ClientActionException, CollidingActionsException {
 
         mode = mapMode(mode);
         int statusCode;
         try {
             do {
                 String uri = mConfig.getClientUri() + "/action/" + mode + "?" + options;
                 HttpPut request = new HttpPut(sign(uri));
                request.setEntity(new StringEntity(payload.toString(), HTTP.UTF_8));
                 HttpResponse response = getHttpClient().execute(request);
 
                 statusCode = response.getStatusLine().getStatusCode();
                 switch (statusCode) {
                     case 204:
                         return null;
                     case 200:
                         return (JSONObject) convertResponseToJsonArray(response).get(0);
                     case 409:
                         throw new CollidingActionsException("The constrains of '" + mode
                                 + "' were violated. Try again.");
                     default:
                         // handled at the end of the method
                 }
             } while (statusCode == 504);
 
         } catch (JSONException e) {
             throw new ClientActionException("Data Error. Could not share.", e);
         } catch (ClientProtocolException e) {
             throw new ClientActionException("HTTP Error. Could not share data.", e);
         } catch (IOException e) {
             throw new ClientActionException("Network Error. Could not share data.", e);
         } catch (ParseException e) {
             throw new ClientActionException("Parsing failed. Could not share data.", e);
         } catch (UpdateException e) {
             throw new ClientActionException("Update failed. Could not share data. ", e);
         }
 
         throw new ClientActionException("Server Error " + statusCode + ". Could not share data.");
 
     }
 
     public JSONObject receive(String mode) throws BadModeException, ClientActionException,
             CollidingActionsException {
         return receive(mode, "");
     }
 
     public JSONObject receive(String mode, String options) throws BadModeException,
             ClientActionException, CollidingActionsException {
 
         mode = mapMode(mode);
         int statusCode;
 
         try {
             do {
                 String uri = mConfig.getClientUri() + "/action/" + mode + "?" + options;
                 HttpGet request = new HttpGet(sign(uri));
                 HttpResponse response = getHttpClient().execute(request);
 
                 statusCode = response.getStatusLine().getStatusCode();
                 switch (statusCode) {
                     case 204:
                         return null;
                     case 200:
                         return convertResponseToJsonArray(response).getJSONObject(0);
                     case 409:
                         throw new CollidingActionsException("The constrains of '" + mode
                                 + "' were violated. Try again.");
                     default:
                         // handled at the end of the method
                 }
             } while (statusCode == 504);
 
         } catch (JSONException e) {
             throw new ClientActionException("Data Error. Could not receive.", e);
         } catch (ClientProtocolException e) {
             throw new ClientActionException("HTTP Error. Could not receive data.", e);
         } catch (IOException e) {
             throw new ClientActionException("Network Error. Could not receive data.", e);
         } catch (ParseException e) {
             throw new ClientActionException("Parsing failed. Could not receive data.", e);
         } catch (UpdateException e) {
             throw new ClientActionException("Update failed. Could not receive data. ", e);
         }
 
         throw new ClientActionException("Server Error " + statusCode + ". Could not receive data.");
     }
 
     public void abortPeek() {
         if (mPeekStopped == true) {
             return;
         }
         mPeekStopped = true;
         synchronized (mPeekLock) {
             if (mPeekRequest != null)
                 mPeekRequest.abort();
         }
     }
 
     public JSONObject peek(String groupID) throws ClientActionException {
 
         int statusCode;
 
         mPeekStopped = false;
         try {
             do {
                 String uri = mConfig.getClientUri() + "/peek"; // https://<server>/v3/clients/<uuid>
                 if (groupID != null) {
                     uri += "?group_id=" + groupID;
                 }
                 // uri = sign(uri);
 
                 Log.v("Linncer", "peeking uri = " + uri);
 
                 HttpGet request = new HttpGet(uri);
                 if (mPeekStopped == true)
                     return null;
 
                 synchronized (mPeekLock) {
                     mPeekRequest = request;
                 }
                 HttpResponse response = getHttpClient().execute(request);
 
                 statusCode = response.getStatusLine().getStatusCode();
                 String body = convertResponseToString(response);
                 switch (statusCode) {
                     case 200:
                         Log.v("Linncer", "peek response = " + body);
                         JSONObject json = null;
                         try {
                             json = new JSONObject(body);
                         } catch (Exception e) {
                             throw new ParseException("could not parse the json '" + body + "'");
                         }
 
                         return json;
                         // return convertResponseToJsonObject(response);
                     default:
                         // handled at the end of the method
                 }
             } while (statusCode == 504);
 
             // } catch (JSONException e) {
             // throw new ClientActionException("JSON Data Format Error. Could not peek.", e);
         } catch (ClientProtocolException e) {
             throw new ClientActionException("HTTP Error. Could not peek data.", e);
         } catch (IOException e) {
             Log.v("Linncer", "peek IOException, what=" + e.getMessage());
             // e.printStackTrace();
             if (mPeekStopped) {
                 throw new ClientActionException("Peek aborted.", e);
             }
             throw new ClientActionException("Network Error. Could not peek data.", e);
         } catch (ParseException e) {
             throw new ClientActionException("Parsing failed. Could not peek data.", e);
             // } catch (UpdateException e) {
             // throw new ClientActionException("Update failed. Could not peek data. ", e);
         }
 
         throw new ClientActionException("Server Error " + statusCode + ". Could not peek.");
     }
 
     public String getUri() {
         return mConfig.getClientUri();
     }
 
     public EnvironmentStatus getEnvironmentStatus() {
         return mEnvironmentStatus;
     }
 
     private String mapMode(String mode) throws BadModeException {
         if (mode.equals("1:1") || mode.equals("one-to-one")) {
             return "one-to-one";
         } else if (mode.equals("1:n") || mode.equals("one-to-many")) {
             return "one-to-many";
         } else if (mode.equals("n:n") || mode.equals("many-to-many")) {
             return "many-to-many";
         }
 
         throw new BadModeException("the provided mode name '" + mode + "' could not be mapped");
     }
 
     public boolean autoSubmitEnvironmentChanges() {
         return mAutoSubmitEnvironmentChanges;
     }
 
     public void autoSubmitEnvironmentChanges(boolean flag) {
         mAutoSubmitEnvironmentChanges = flag;
     }
 }
