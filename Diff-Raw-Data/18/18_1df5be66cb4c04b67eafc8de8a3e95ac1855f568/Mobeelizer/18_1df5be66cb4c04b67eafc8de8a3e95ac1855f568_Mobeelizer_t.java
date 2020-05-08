 // 
 // Mobeelizer.java
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
 
 package com.mobeelizer.java;
 
 import java.io.IOException;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.concurrent.Executor;
 import java.util.concurrent.Executors;
 
 import org.apache.http.client.methods.HttpRequestBase;
 
 import com.mobeelizer.java.api.MobeelizerFile;
 import com.mobeelizer.java.api.MobeelizerMode;
 import com.mobeelizer.java.api.MobeelizerModel;
 import com.mobeelizer.java.api.user.MobeelizerUser;
 import com.mobeelizer.java.connection.MobeelizerAuthenticateResponse;
 import com.mobeelizer.java.connection.MobeelizerConnectionService;
 import com.mobeelizer.java.connection.MobeelizerConnectionServiceDelegate;
 import com.mobeelizer.java.connection.MobeelizerConnectionServiceImpl;
 import com.mobeelizer.java.definition.MobeelizerApplicationDefinition;
 import com.mobeelizer.java.definition.MobeelizerDefinitionConverter;
 import com.mobeelizer.java.definition.MobeelizerDefinitionParser;
 
 /**
  * Entry point to the Mobeelizer application.
  * 
 * @since 1.0
  */
 public class Mobeelizer {
 
     private final MobeelizerApplicationDefinition applicationDefinition;
 
     private final MobeelizerDefinitionConverter mobeelizerDefinitionConverter;
 
     private final MobeelizerSyncService syncService;
 
     private final MobeelizerConnectionService connectionService;
 
     private final Set<MobeelizerModel> definition = new HashSet<MobeelizerModel>();
 
     private final Executor executor = Executors.newSingleThreadExecutor();
 
     private final MobeelizerConfiguration configuration;
 
     /**
      * Version of Mobeelizer SDK.
      */
     public static final String VERSION = "${project.version}";
 
     /**
      * Creates new Mobeelizer SDK.
      * 
      * @param configuration
      *            configuration
      * @since 1.0
      */
     public Mobeelizer(final MobeelizerConfiguration configuration) {
         this.configuration = configuration;
         if (configuration.getDevice() == null) {
             throw new IllegalStateException("Device must be set in configuration.");
         }
 
         if (configuration.getMode() == null) {
             throw new IllegalStateException("Mode must be set in configuration.");
         }
 
         if (configuration.getUser() == null) {
             throw new IllegalStateException("Login must be set in configuration.");
         }
 
         if (configuration.getPassword() == null) {
             throw new IllegalStateException("Password must be set in configuration.");
         }
 
         if (configuration.getPackageName() == null) {
             throw new IllegalStateException("Package name must be set in configuration.");
         }
 
         if (configuration.getDefinition() == null) {
             throw new IllegalStateException("Definition file must be set in configuration.");
         }
 
         mobeelizerDefinitionConverter = new MobeelizerDefinitionConverter();
 
         applicationDefinition = MobeelizerDefinitionParser.parse(configuration.getDefinition());
 
         connectionService = new MobeelizerConnectionServiceImpl(new MobeelizerConnectionServiceDelegate() {
 
             @Override
             public void setProxyIfNecessary(final HttpRequestBase request) {
                 // empty
             }
 
             @Override
             public void logInfo(final String message) {
                 // TODO
             }
 
             @Override
             public void logDebug(final String message) {
                 // TODO
             }
 
             @Override
             public boolean isNetworkAvailable() {
                 return true;
             }
 
             @Override
             public String getVersionDigest() {
                 return applicationDefinition.getDigest();
             }
 
             @Override
             public String getVendor() {
                 return applicationDefinition.getVendor();
             }
 
             @Override
             public String getUser() {
                 return configuration.getUser();
             }
 
             @Override
             public String getUrl() {
                 return configuration.getUrl();
             }
 
             @Override
             public String getSdkVersion() {
                 return "java-sdk-" + Mobeelizer.VERSION;
             }
 
             @Override
             public String getPassword() {
                 return configuration.getPassword();
             }
 
             @Override
             public String getInstance() {
                 return configuration.getInstance();
             }
 
             @Override
             public String getDeviceIdentifier() {
                 return configuration.getDeviceIdentifier();
             }
 
             @Override
             public String getDevice() {
                 return configuration.getDevice();
             }
 
             @Override
             public String getApplication() {
                 return applicationDefinition.getApplication();
             }
 
             @Override
             public MobeelizerMode getMode() {
                 return configuration.getMode();
             }
 
         });
 
         MobeelizerAuthenticateResponse authenticate;
 
         try {
             authenticate = connectionService.authenticate(configuration.getUser(), configuration.getPassword());
         } catch (IOException e) {
             throw new IllegalStateException("Cannot authenticate user: " + e.getMessage(), e);
         }
 
         if (authenticate == null) {
             throw new IllegalStateException("User with login " + configuration.getUser() + " cannot be authorized");
         }
 
         definition.addAll(mobeelizerDefinitionConverter.convert(applicationDefinition, configuration.getPackageName(),
                 authenticate.getRole()));
 
         syncService = new MobeelizerSyncService(definition, connectionService);
     }
 
     /**
      * Gets model definition.
      * 
      * @return definition
      * @since 1.0
      */
     public Set<MobeelizerModel> getDefinition() {
         return definition;
     }
 
     /**
      * Authenticates user with given login and password.
      * 
      * @return user's role or null if not authenticated
      * @since 1.0
      */
     public String authenticate(final String login, final String password) {
         try {
             MobeelizerAuthenticateResponse authenticate = connectionService.authenticate(login, password);
             return authenticate == null ? null : authenticate.getRole();
         } catch (IOException e) {
             throw new IllegalStateException("Cannot authenticate user: " + e.getMessage(), e);
         }
     }
 
     /**
      * Gets all users' groups.
      * 
      * @return groups
      * @since 1.0
      */
     public List<String> getGroups() {
         try {
             return connectionService.getGroups();
         } catch (IOException e) {
             throw new IllegalStateException("Cannot get groups: " + e.getMessage(), e);
         }
     }
 
     /**
      * Gets all users.
      * 
      * @return users
      * @since 1.0
      */
     public List<MobeelizerUser> getUsers() {
         try {
             return connectionService.getUsers();
         } catch (IOException e) {
             throw new IllegalStateException("Cannot get users: " + e.getMessage(), e);
         }
     }
 
     /**
      * Gets user with given login.
      * 
      * @param login
      *            login
      * @return user
      * @since 1.0
      */
     public MobeelizerUser getUser(final String login) {
         try {
             return connectionService.getUser(login);
         } catch (IOException e) {
             throw new IllegalStateException("Cannot get user: " + e.getMessage(), e);
         }
     }
 
     /**
      * Creates new user.
      * 
      * @param user
      *            user to create
      * @since 1.0
      */
     public void createUser(final MobeelizerUser user) {
         try {
             connectionService.createUser(user);
         } catch (IOException e) {
             throw new IllegalStateException("Cannot create user: " + e.getMessage(), e);
         }
     }
 
     /**
      * Updates existing user.
      * 
      * @param user
      *            user to update
      * @since 1.0
      */
     public void updateUser(final MobeelizerUser user) {
         try {
             connectionService.updateUser(user);
         } catch (IOException e) {
             throw new IllegalStateException("Cannot update user: " + e.getMessage(), e);
         }
     }
 
     /**
      * Deletes user with given login.
      * 
      * @param login
      *            login
      * @return true if successfully deleted
      * @since 1.0
      */
     public boolean deleteUser(final String login) {
         if (configuration.getUser().equals(login)) {
             return false;
         }
         try {
             return connectionService.deleteUser(login);
         } catch (IOException e) {
             throw new IllegalStateException("Cannot delete user: " + e.getMessage(), e);
         }
     }
 
     /**
      * Start a full sync. After finished callback will be invoked.
      * 
      * @param callback
      *            callback
      * @since 1.0
      * @see MobeelizerSyncCallback
      */
     public void syncAll(final MobeelizerSyncCallback callback) {
         executor.execute(new Runnable() {
 
             @Override
             public void run() {
                 syncService.syncAll(callback);
             }
 
         });
     }
 
     /**
      * Start a differential sync. After finished callback will be invoked.
      * 
      * @param entities
      *            new entities to send to the cloud
      * @param files
      *            new files to send to the cloud
      * @param callback
      *            callback
      * @since 1.0
      * @see MobeelizerSyncCallback
      */
     public void sync(final Iterable<Object> entities, final Iterable<MobeelizerFile> files, final MobeelizerSyncCallback callback) {
         executor.execute(new Runnable() {
 
             @Override
             public void run() {
                 syncService.sync(entities, files, callback);
             }
 
         });
     }
 
     /**
      * Sends remote notification to all users.
      * 
      * @param notification
      *            notification to send
      * @since 1.0
      */
     public void sendRemoteNotification(final Map<String, String> notification) {
         sendRemoteNotification(notification, null, null, null);
     }
 
     /**
      * Sends remote notification to all users on specified device.
      * 
      * @param notification
      *            notification to send
      * @param device
      *            device
      * @since 1.0
      */
     public void sendRemoteNotificationToDevice(final Map<String, String> notification, final String device) {
         sendRemoteNotification(notification, null, null, device);
     }
 
     /**
      * Sends remote notification to given users.
      * 
      * @param notification
      *            notification to send
      * @param users
      *            list of users
      * @since 1.0
      */
     public void sendRemoteNotificationToUsers(final Map<String, String> notification, final List<String> users) {
         sendRemoteNotification(notification, users, null, null);
     }
 
     /**
      * Sends remote notification to given users on specified device.
      * 
      * @param notification
      *            notification to send
      * @param users
      *            list of users
      * @param device
      *            device
      * @since 1.0
      */
     public void sendRemoteNotificationToUsersOnDevice(final Map<String, String> notification, final List<String> users,
             final String device) {
         sendRemoteNotification(notification, users, null, device);
     }
 
     /**
      * Sends remote notification to given group.
      * 
      * @param notification
      *            notification to send
      * @param group
      *            group
      * @since 1.0
      */
     public void sendRemoteNotificationToGroup(final Map<String, String> notification, final String group) {
         sendRemoteNotification(notification, null, group, null);
     }
 
     /**
      * Sends remote notification to given group on specified device.
      * 
      * @param notification
      *            notification to send
      * @param group
      *            group
      * @param device
      *            device
      * @since 1.0
      */
     public void sendRemoteNotificationToGroupOnDevice(final Map<String, String> notification, final String group,
             final String device) {
         sendRemoteNotification(notification, null, group, device);
     }
 
     private void sendRemoteNotification(final Map<String, String> notification, final List<String> users, final String group,
             final String device) {
         try {
             connectionService.sendRemoteNotification(device, group, users, notification);
         } catch (IOException e) {
             throw new IllegalStateException("Cannot send remote notification: " + e.getMessage(), e);
         }
     }
 
 }
