 package com.mobeelizer.java;
 
 import java.io.InputStream;
 import java.util.Locale;
 
 import com.mobeelizer.java.api.MobeelizerMode;
 
 /**
  * Holder of Mobeelizer SDK configuration.
  * 
  * @since 1.0
  */
 public class MobeelizerConfiguration {
 
     private InputStream definition;
 
     private String device;
 
     private String deviceIdentifier = "0";
 
     private String packageName;
 
     private MobeelizerMode mode = MobeelizerMode.TEST;
 
     private String instance;
 
     private String user;
 
     private String password;
 
     private String url;
 
     InputStream getDefinition() {
         return definition;
     }
 
     /**
      * Sets the stream with the definition XML.
      * 
      * @param definition
      *            definition
      * @since 1.0
      */
     public void setDefinition(final InputStream definition) {
         this.definition = definition;
     }
 
     String getDevice() {
         return device;
     }
 
     /**
      * Sets the name of the device.
      * 
      * @param device
      *            device
      * @since 1.0
      */
     public void setDevice(final String device) {
         this.device = device;
     }
 
     String getPackageName() {
         return packageName;
     }
 
     /**
      * Sets the name of the package containing model classes.
      * 
      * @param packageName
      *            packageName
      * @since 1.0
      */
     public void setPackageName(final String packageName) {
         this.packageName = packageName;
     }
 
     MobeelizerMode getMode() {
         return mode;
     }
 
     /**
      * Sets the mode. By default TEST is set.
      * 
      * @param mode
      *            mode
      * @since 1.0
      */
     public void setMode(final MobeelizerMode mode) {
         this.mode = mode;
     }
 
     String getInstance() {
         return instance == null && mode != null ? mode.name().toLowerCase(Locale.ENGLISH) : instance;
     }
 
     /**
      * Sets the name of the instance. By default lower-cased mode is set (test or production).
      * 
      * @param instance
      *            instance
      * @since 1.0
      */
     public void setInstance(final String instance) {
         this.instance = instance;
     }
 
     String getUser() {
         return user;
     }
 
     /**
      * Sets the login of the user.
      * 
      * @param user
      *            user
      * @since 1.0
      */
     public void setUser(final String user) {
         this.user = user;
     }
 
     String getPassword() {
         return password;
     }
 
     /**
      * Sets the password of the user.
      * 
     * @param user
     *            user
      * @since 1.0
      */
     public void setPassword(final String password) {
         this.password = password;
     }
 
     void setUrl(final String url) {
         this.url = url;
     }
 
     String getUrl() {
         return url;
     }
 
     /**
      * Sets the string identifying the device.
      * 
      * @param deviceIdentifier
      *            deviceIdentifier
      * @since 1.0
      */
     public void setDeviceIdentifier(final String deviceIdentifier) {
         this.deviceIdentifier = deviceIdentifier;
     }
 
     String getDeviceIdentifier() {
         return deviceIdentifier;
     }
 
 }
