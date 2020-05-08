 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.network.monitor.service;
 
 import com.network.monitor.domain.EmailSettings;
 import com.network.monitor.domain.Setting;
 import com.network.monitor.util.FileUtil;
 import java.io.File;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  *
  * @author opetridean
  */
 public class AppSettingsService {
 
     public static final String SETTINGS_PATH = "./settings";
     public static final String SETTINGS_FILE = "app.properties";
     public static final String EMAIL_SETTINGS_FILE = "-email.properties";
     public static final String SMS_URL = "sms.url=";
     public static final String SMS_SENDER_NAME = "sms.sender.name=";
     public static final String SMS_PHONE_NUMBER = "sms.phone.number=";
     public static final String SMS_TEXT = "sms.text=";
     public static final String SMS_URL_SINGTEL = "sms.url.singtel=";
     public static final String SMS_SINGTEL_SENDER_NAME = "sms.singtel.sender.name=";
     public static final String SMS_SINGTEL_PHONE_NUMBER = "sms.singtel.phone.number=";
     public static final String SMS_SINGTEL_TEXT = "sms.singtel.text=";
     public static final String MONITORING_TYPE = "monitoring.type=";
     public static final String EMAIL_CONFIG_NAME = "email.config.name=";
     public static final String EMAIL_USER = "email.user=";
     public static final String EMAIL_PASSWORD = "email.password=";
     public static final String EMAIL_HOST = "email.host=";
     public static final String EMAIL_PORT = "email.port=";
     public static final String EMAIL_ENABLED = "email.enabled=";
     public static final String EMAIL_DEFAULT = "email.default=";
     public static final String EMAIL_PRIORITY = "email.priority=";
 
     public void saveSettings(Setting setting) {
         List<String> linesToSave = new ArrayList<String>();
 
         linesToSave.add(SMS_URL + setting.getSmsUrlWebStar());
         linesToSave.add(SMS_SENDER_NAME + setting.getSmsTestWebStarSenderName());
         linesToSave.add(SMS_PHONE_NUMBER + setting.getSmsTestWebStarPhoneNumber());
         linesToSave.add(SMS_TEXT + setting.getSmsTestWebStarTestMessage());
 
         linesToSave.add(SMS_URL_SINGTEL + setting.getSmsUrlSingTel());
         linesToSave.add(SMS_SINGTEL_SENDER_NAME + setting.getSmsTestSingTelSenderName());
         linesToSave.add(SMS_SINGTEL_PHONE_NUMBER + setting.getSmsTestSingTelPhoneNumber());
         linesToSave.add(SMS_SINGTEL_TEXT + setting.getSmsTestSingTelTestMessage());
         
         linesToSave.add(MONITORING_TYPE + setting.getMonitoringType());
 
         FileUtil.saveContentToFile(SETTINGS_PATH, SETTINGS_FILE, linesToSave);
 
         if (setting.getEmailSettings() != null) {
             for (EmailSettings emailSettings : setting.getEmailSettings()) {
                 linesToSave.add(EMAIL_CONFIG_NAME + emailSettings.getConfigName());
                 linesToSave.add(EMAIL_USER + emailSettings.getUserName());
                 linesToSave.add(EMAIL_PASSWORD + emailSettings.getPassword());
                 linesToSave.add(EMAIL_HOST + emailSettings.getServerHost());
                 linesToSave.add(EMAIL_PORT + emailSettings.getServerPort());
                 linesToSave.add(EMAIL_ENABLED + emailSettings.isEnabled());
                 linesToSave.add(EMAIL_DEFAULT + emailSettings.isDefaultEmail());
 
                 FileUtil.saveContentToFile(SETTINGS_PATH, emailSettings.getConfigName() + EMAIL_SETTINGS_FILE, linesToSave);
             }
         }
 
     }
 
     public void addEmailSettings(EmailSettings emailSettings) {
         List<String> linesToSave = new ArrayList<String>();
 
         linesToSave.add(EMAIL_CONFIG_NAME + emailSettings.getConfigName());
         linesToSave.add(EMAIL_USER + emailSettings.getUserName());
         linesToSave.add(EMAIL_PASSWORD + emailSettings.getPassword());
         linesToSave.add(EMAIL_HOST + emailSettings.getServerHost());
         linesToSave.add(EMAIL_PORT + emailSettings.getServerPort());
         linesToSave.add(EMAIL_ENABLED + emailSettings.isEnabled());
         linesToSave.add(EMAIL_DEFAULT + emailSettings.isDefaultEmail());
         linesToSave.add(EMAIL_PRIORITY + emailSettings.getPriorityId());
 
         FileUtil.saveContentToFile(SETTINGS_PATH, emailSettings.getPriorityId() + "-" +
                 emailSettings.getConfigName() + EMAIL_SETTINGS_FILE, linesToSave);
     }
     
     public void updateEmailSettings(EmailSettings emailSettings, String oldConfigName) {
         List<String> linesToSave = new ArrayList<String>();
 
         linesToSave.add(EMAIL_CONFIG_NAME + emailSettings.getConfigName());
         linesToSave.add(EMAIL_USER + emailSettings.getUserName());
         linesToSave.add(EMAIL_PASSWORD + emailSettings.getPassword());
         linesToSave.add(EMAIL_HOST + emailSettings.getServerHost());
         linesToSave.add(EMAIL_PORT + emailSettings.getServerPort());
         linesToSave.add(EMAIL_ENABLED + emailSettings.isEnabled());
         linesToSave.add(EMAIL_DEFAULT + emailSettings.isDefaultEmail());
         linesToSave.add(EMAIL_PRIORITY + emailSettings.getPriorityId());
         FileUtil.deleteConfigFile(SETTINGS_PATH, oldConfigName + EMAIL_SETTINGS_FILE);
 
        FileUtil.saveContentToFile(SETTINGS_PATH, emailSettings.getConfigName() + EMAIL_SETTINGS_FILE, linesToSave);
     }
 
     public void deleteEmailSettings(String configName) {
 
         FileUtil.deleteConfigFile(SETTINGS_PATH, configName + EMAIL_SETTINGS_FILE);
     }
 
     public Setting getSettings() {
         List<String> linesToSave = new ArrayList<String>();
         Setting setting = new Setting();
         List<EmailSettings> emailSettings = new ArrayList<EmailSettings>();
         linesToSave = FileUtil.readContentFromFileAsList(SETTINGS_PATH, SETTINGS_FILE);
 
         for (String line : linesToSave) {
             if (line.startsWith(SMS_URL) && line.split("=").length == 2) {
                 setting.setSmsUrlWebStar(line.split("=")[1]);
             }
             if (line.startsWith(SMS_SENDER_NAME) && line.split("=").length == 2) {
                 setting.setSmsTestWebStarSenderName(line.split("=")[1]);
             }
             if (line.startsWith(SMS_PHONE_NUMBER) && line.split("=").length == 2) {
                 setting.setSmsTestWebStarPhoneNumber(line.split("=")[1]);
             }
             if (line.startsWith(SMS_TEXT) && line.split("=").length == 2) {
                 setting.setSmsTestWebStarTestMessage(line.split("=")[1]);
             }
             if (line.startsWith(SMS_URL_SINGTEL) && line.split("=").length == 2) {
                 setting.setSmsUrlSingTel(line.split("=")[1]);
             }
              if (line.startsWith(SMS_SINGTEL_SENDER_NAME) && line.split("=").length == 2) {
                 setting.setSmsTestSingTelSenderName(line.split("=")[1]);
             }
             if (line.startsWith(SMS_SINGTEL_PHONE_NUMBER) && line.split("=").length == 2) {
                 setting.setSmsTestSingTelPhoneNumber(line.split("=")[1]);
             }
             if (line.startsWith(SMS_SINGTEL_TEXT) && line.split("=").length == 2) {
                 setting.setSmsTestSingTelTestMessage(line.split("=")[1]);
             }
             if (line.startsWith(MONITORING_TYPE) && line.split("=").length == 2) {
                 setting.setMonitoringType(line.split("=")[1]);
             }
         }
 
         List<File> fileList = FileUtil.getFilesFromPathThatStartWith(SETTINGS_PATH, EMAIL_SETTINGS_FILE);
         for (File file : fileList) {
             linesToSave = FileUtil.readContentFromFileAsList(SETTINGS_PATH, file.getName());
             EmailSettings emailSetting = new EmailSettings();
             for (String line : linesToSave) {
                 if (line.startsWith(EMAIL_CONFIG_NAME)) {
                     emailSetting.setConfigName(line.split("=")[1]);
                 }
                 if (line.startsWith(EMAIL_HOST)) {
                     emailSetting.setServerHost(line.split("=")[1]);
                 }
                 if (line.startsWith(EMAIL_PORT)) {
                     emailSetting.setServerPort(line.split("=")[1]);
                 }
                 if (line.startsWith(EMAIL_USER)) {
                     emailSetting.setUserName(line.split("=")[1]);
                 }
                 if (line.startsWith(EMAIL_PASSWORD)) {
                     emailSetting.setPassword(line.split("=")[1]);
                 }
                  if (line.startsWith(EMAIL_PRIORITY)) {
                     emailSetting.setPriorityId(Integer.parseInt(line.split("=")[1]));
                 }
             }
             emailSettings.add(emailSetting);
         }
 
         setting.setEmailSettings(emailSettings);
 
         return setting;
     }
 }
