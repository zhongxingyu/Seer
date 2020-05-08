 // 
 // MobeelizerRealConnectionManager.java
 // 
 // Copyright (C) 2012 Mobeelizer Ltd. All Rights Reserved.
 //
 // Mobeelizer SDK is free software; you can redistribute it and/or modify it 
 // under the terms of the GNU Affero General Public License as published by 
 // the Free Software Foundation; either version 3 of the License, or (at your
 // option) any later version.
 //
 // This program is distributed in the hope that it will be useful, but WITHOUT
 // ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or 
 // FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License
 // for more details.
 //
 // You should have received a copy of the GNU Affero General Public License 
 // along with this program; if not, write to the Free Software Foundation, Inc., 
 // 51 Franklin St, Fifth Floor, Boston, MA  02110-1301 USA
 // 
 
 package com.mobeelizer.mobile.android;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.http.HttpHost;
 import org.apache.http.client.methods.HttpRequestBase;
 import org.apache.http.conn.params.ConnRouteParams;
 
 import android.content.Context;
 import android.net.ConnectivityManager;
 import android.net.Proxy;
 import android.util.Log;
 
 import com.mobeelizer.java.api.MobeelizerMode;
 import com.mobeelizer.java.api.MobeelizerOperationError;
 import com.mobeelizer.java.connection.MobeelizerAuthenticateResponse;
 import com.mobeelizer.java.connection.MobeelizerConnectionService;
 import com.mobeelizer.java.connection.MobeelizerConnectionServiceDelegate;
 import com.mobeelizer.java.connection.MobeelizerConnectionServiceImpl;
 import com.mobeelizer.java.errors.MobeelizerOperationErrorImpl;
 import com.mobeelizer.java.errors.MobeelizerOperationStatus;
 
 class MobeelizerRealConnectionManager implements MobeelizerConnectionManager {
 
     private static final String TAG = "mobeelizer:mobeelizerrealconnectionmanager";
 
     private MobeelizerConnectionService connectionService;
 
     private final MobeelizerApplication application;
 
     public MobeelizerRealConnectionManager(final MobeelizerApplication application) {
         this.application = application;
         this.connectionService = new MobeelizerConnectionServiceImpl(new MobeelizerConnectionServiceDelegate() {
 
             @Override
             public void setProxyIfNecessary(final HttpRequestBase request) {
                 MobeelizerRealConnectionManager.this.setProxyIfNecessary(request);
             }
 
             @Override
             public void logInfo(final String message) {
                 Log.i(TAG, message);
             }
 
             @Override
             public void logDebug(final String message) {
                 Log.d(TAG, message);
             }
 
             @Override
             public boolean isNetworkAvailable() {
                 return MobeelizerRealConnectionManager.this.isNetworkAvailable();
             }
 
             @Override
             public String getVersionDigest() {
                 return application.getVersionDigest();
             }
 
             @Override
             public String getVendor() {
                 return application.getVendor();
             }
 
             @Override
             public String getUser() {
                 return application.getUser();
             }
 
             @Override
             public String getUrl() {
                 return application.getUrl();
             }
 
             @Override
             public String getSdkVersion() {
                 return "android-sdk-" + Mobeelizer.VERSION;
             }
 
             @Override
             public String getPassword() {
                 return application.getPassword();
             }
 
             @Override
             public String getInstance() {
                 return application.getInstance();
             }
 
             @Override
             public String getDeviceIdentifier() {
                 return application.getDeviceIdentifier();
             }
 
             @Override
             public String getDevice() {
                 return application.getDevice();
             }
 
             @Override
             public String getApplication() {
                 return application.getApplication();
             }
 
             @Override
             public MobeelizerMode getMode() {
                 return application.getMode();
             }
 
         });
     }
 
     @Override
     public MobeelizerLoginResponse login() {
         boolean networkConnected = isNetworkAvailable();
 
         if (!networkConnected) {
             String[] roleAndInstanceGuid = getRoleAndInstanceGuidFromDatabase(application);
 
             if (roleAndInstanceGuid[0] == null) {
                 Log.e(TAG, "Login failure. Missing connection failure.");
                 return new MobeelizerLoginResponse(MobeelizerOperationErrorImpl.missingConnectionError());
             } else {
                 Log.i(TAG, "Login '" + application.getUser() + "' from database successful.");
                 return new MobeelizerLoginResponse(null, roleAndInstanceGuid[1], roleAndInstanceGuid[0], false);
             }
         }
 
         MobeelizerAuthenticateResponse response = connectionService.authenticate(application.getUser(),
                 application.getPassword(), "android", application.getRemoteNotificationToken());
 
         if (response != null) {
             if (response.getError() != null) {
                 String[] roleAndInstanceGuid = getRoleAndInstanceGuidFromDatabase(application);
 
                 if (roleAndInstanceGuid[0] == null) {
                    return new MobeelizerLoginResponse(response.getError());
                 } else {
                     return new MobeelizerLoginResponse(null, roleAndInstanceGuid[1], roleAndInstanceGuid[0], false);
                 }
             }
             boolean initialSyncRequired = isInitialSyncRequired(application, response.getInstanceGuid());
 
             setRoleAndInstanceGuidInDatabase(application, response.getRole(), response.getInstanceGuid());
             Log.i(TAG, "Login '" + application.getUser() + "' successful.");
             return new MobeelizerLoginResponse(null, response.getInstanceGuid(), response.getRole(), initialSyncRequired);
         } else {
             Log.e(TAG, "Login failure. Authentication error.");
             clearRoleAndInstanceGuidInDatabase(application);
             return new MobeelizerLoginResponse(MobeelizerOperationErrorImpl.authenticationFailure());
         }
     }
 
     @Override
     public MobeelizerOperationStatus<String> sendSyncAllRequest() {
         return connectionService.sendSyncAllRequest();
     }
 
     @Override
     public MobeelizerOperationStatus<String> sendSyncDiffRequest(final File outputFile) {
         return connectionService.sendSyncDiffRequest(outputFile);
     }
 
     @Override
     public MobeelizerOperationError waitUntilSyncRequestComplete(final String ticket) {
         return connectionService.waitUntilSyncRequestComplete(ticket);
     }
 
     @Override
     public File getSyncData(final String ticket) throws IOException {
         return connectionService.getSyncData(ticket);
     }
 
     @Override
     public MobeelizerOperationError confirmTask(final String ticket) {
         return connectionService.confirmTask(ticket);
     }
 
     @Override
     public MobeelizerOperationError registerForRemoteNotifications(final String token) {
         return connectionService.registerForRemoteNotifications(token);
     }
 
     @Override
     public MobeelizerOperationError unregisterForRemoteNotifications(final String token) {
         return connectionService.unregisterForRemoteNotifications(token);
     }
 
     @Override
     public MobeelizerOperationError sendRemoteNotification(final String device, final String group, final List<String> users,
             final Map<String, String> notification) {
         return connectionService.sendRemoteNotification(device, group, users, notification);
     }
 
     @Override
     public boolean isNetworkAvailable() {
         ConnectivityManager connectivityManager = (ConnectivityManager) application.getContext().getSystemService(
                 Context.CONNECTIVITY_SERVICE);
 
         if (isConnected(connectivityManager)) {
             return true;
         }
 
         for (int i = 0; i < 10; i++) {
             if (isConnecting(connectivityManager)) {
                 // wait for connection
                 try {
                     Thread.sleep(500);
                 } catch (InterruptedException e) {
                     Log.w(TAG, e.getMessage(), e);
                     break;
                 }
 
                 if (isConnected(connectivityManager)) {
                     return true;
                 }
             }
         }
 
         return false;
     }
 
     private boolean isInitialSyncRequired(final MobeelizerApplication application, final String instanceGuid) {
         return application.getInternalDatabase().isInitialSyncRequired(application.getInstance(), instanceGuid,
                 application.getUser());
     }
 
     private String[] getRoleAndInstanceGuidFromDatabase(final MobeelizerApplication application) {
         return application.getInternalDatabase().getRoleAndInstanceGuid(application.getInstance(), application.getUser(),
                 application.getPassword());
     }
 
     private void setRoleAndInstanceGuidInDatabase(final MobeelizerApplication application, final String role,
             final String instanceGuid) {
         application.getInternalDatabase().setRoleAndInstanceGuid(application.getInstance(), application.getUser(),
                 application.getPassword(), role, instanceGuid);
     }
 
     private void clearRoleAndInstanceGuidInDatabase(final MobeelizerApplication application) {
         application.getInternalDatabase().clearRoleAndInstanceGuid(application.getInstance(), application.getUser());
     }
 
     private boolean isConnecting(final ConnectivityManager connectivityManager) {
         return connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isConnectedOrConnecting()
                 || connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).isConnectedOrConnecting();
     }
 
     private boolean isConnected(final ConnectivityManager connectivityManager) {
         return connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isConnected()
                 || connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).isConnected();
     }
 
     private void setProxyIfNecessary(final HttpRequestBase request) {
         String proxyHost = Proxy.getHost(application.getContext());
         if (proxyHost == null) {
             return;
         }
 
         int proxyPort = Proxy.getPort(application.getContext());
         if (proxyPort < 0) {
             return;
         }
 
         HttpHost proxy = new HttpHost(proxyHost, proxyPort);
         ConnRouteParams.setDefaultProxy(request.getParams(), proxy);
     }
 
 }
