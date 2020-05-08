 /*******************************************************************************
  * Copyright (C) 2009, 2010, Hoccer GmbH Berlin, Germany <www.hoccer.com>
  * 
  * These coded instructions, statements, and computer programs contain
  * proprietary information of Hoccer GmbH Berlin, and are copy protected
  * by law. They may be used, modified and redistributed under the terms
  * of GNU General Public License referenced below. 
  *    
  * Alternative licensing without the obligations of the GPL is
  * available upon request.
  * 
  * GPL v3 Licensing:
  * 
  * This file is part of the "Linccer Java-API".
  * 
  * Linccer Java-API is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * Linccer Java-API is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with Linccer Java-API. If not, see <http://www.gnu.org/licenses/>.
  ******************************************************************************/
 package com.hoccer.api;
 
 import java.io.IOException;
 import java.util.Arrays;
 import java.util.Date;
 import java.util.List;
 import java.util.concurrent.TimeUnit;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.ParseException;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.methods.HttpDelete;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPut;
 import org.apache.http.entity.StringEntity;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 public class Linccer extends CloudService {
 
     private Environment       mEnvironment = new Environment();
     private EnvironmentStatus mEnvironmentStatus;
 
     public Linccer(ClientConfig config) {
         super(config);
     }
 
     @Override
     protected void finalize() throws Throwable {
         disconnect();
         super.finalize();
     }
 
     // hacky workaround to make sure the connection manager does not wait for a free connection for
     // ever
     private void resetHttpClient() {
         getHttpClient().getConnectionManager().shutdown();
         setupHttpClient();
     }
 
     public void disconnect() throws UpdateException {
         HttpResponse response;
         try {
             resetHttpClient();
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
 
     private void onEnvironmentChanged(Environment environment) throws UpdateException {
         mEnvironment = environment;
 
         HttpResponse response;
         try {
             resetHttpClient();
             String uri = mConfig.getClientUri() + "/environment";
             HttpPut request = new HttpPut(sign(uri));
             request.setEntity(new StringEntity(mEnvironment.toJson().toString()));
             response = getHttpClient().execute(request);
         } catch (Exception e) {
             throw new UpdateException("could not update gps measurement for "
                     + mConfig.getClientUri() + " because of " + e);
         } finally {
             getHttpClient().getConnectionManager().closeIdleConnections(1, TimeUnit.MICROSECONDS);
         }
 
         if (response.getStatusLine().getStatusCode() != 201) {
             try {
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
             throw new UpdateException("could not update environment because server responded with "
                     + response.getStatusLine().getStatusCode() + " and an ill formed body: "
                    + e.getMessage());
         }
 
     }
 
     public void onGpsChanged(double latitude, double longitude, int accuracy)
             throws UpdateException {
         onGpsChanged(latitude, longitude, accuracy, new Date());
     }
 
     public void onGpsChanged(double latitude, double longitude, int accuracy, Date date)
             throws UpdateException {
         mEnvironment.setGpsMeasurement(latitude, longitude, accuracy, date);
         onEnvironmentChanged(mEnvironment);
     }
 
     public void onGpsChanged(double latitude, double longitude, int accuracy, long time)
             throws UpdateException {
         onGpsChanged(latitude, longitude, accuracy, new Date(time));
     }
 
     public void onNetworkChanged(double latitude, double longitude, int accuracy)
             throws UpdateException {
         onNetworkChanged(latitude, longitude, accuracy, new Date());
     }
 
     public void onNetworkChanged(double latitude, double longitude, int accuracy, Date date)
             throws UpdateException {
         mEnvironment.setNetworkMeasurement(latitude, longitude, accuracy, date);
         onEnvironmentChanged(mEnvironment);
     }
 
     public void onNetworkChanged(double latitude, double longitude, int accuracy, long time)
             throws UpdateException {
         onNetworkChanged(latitude, longitude, accuracy, new Date(time));
     }
 
     public void onWifiChanged(List<String> bssids) throws UpdateException {
         mEnvironment.setWifiMeasurement(bssids, new Date());
         onEnvironmentChanged(mEnvironment);
     }
 
     public void onWifiChanged(String[] bssids) throws UpdateException {
         onWifiChanged(Arrays.asList(bssids));
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
             resetHttpClient();
             String uri = mConfig.getClientUri() + "/action/" + mode + "?" + options;
             HttpPut request = new HttpPut(sign(uri));
             request.setEntity(new StringEntity(payload.toString()));
             HttpResponse response = getHttpClient().execute(request);
             getHttpClient().getConnectionManager().closeIdleConnections(1, TimeUnit.MICROSECONDS);
 
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
 
         } catch (JSONException e) {
             throw new ClientActionException("could not share payload " + payload.toString()
                     + " because of " + e);
         } catch (ClientProtocolException e) {
             throw new ClientActionException("could not share payload " + payload.toString()
                     + " because of " + e);
         } catch (IOException e) {
             throw new ClientActionException("could not share payload " + payload.toString()
                     + " because of " + e);
         } catch (ParseException e) {
             throw new ClientActionException("could not share payload " + payload.toString()
                     + " because of " + e);
         } catch (UpdateException e) {
             throw new ClientActionException("could not share payload " + payload.toString()
                     + " because of " + e);
         } finally {
             getHttpClient().getConnectionManager().closeIdleConnections(1, TimeUnit.MICROSECONDS);
         }
 
         throw new ClientActionException("could not share payload " + payload.toString()
                 + " because server responded with status code " + statusCode);
 
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
             resetHttpClient();
             String uri = mConfig.getClientUri() + "/action/" + mode + "?" + options;
             HttpGet request = new HttpGet(sign(uri));
             HttpResponse response = getHttpClient().execute(request);
             getHttpClient().getConnectionManager().closeIdleConnections(1, TimeUnit.MICROSECONDS);
 
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
 
         } catch (JSONException e) {
             throw new ClientActionException("could not receive payload because of " + e);
         } catch (ClientProtocolException e) {
             throw new ClientActionException("could not receive payload because of " + e);
         } catch (IOException e) {
             throw new ClientActionException("could not receive payload because of " + e);
         } catch (ParseException e) {
             throw new ClientActionException("could not receive payload because of " + e);
         } catch (UpdateException e) {
             throw new ClientActionException("could not receive payload because of " + e);
         } finally {
             getHttpClient().getConnectionManager().closeIdleConnections(1, TimeUnit.MICROSECONDS);
         }
 
         throw new ClientActionException(
                 "could not receive payload because server responded with status code " + statusCode);
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
 
 }
