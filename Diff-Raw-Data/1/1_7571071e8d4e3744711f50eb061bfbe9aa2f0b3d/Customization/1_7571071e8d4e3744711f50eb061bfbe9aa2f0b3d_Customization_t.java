 /*
  * Funambol is a mobile platform developed by Funambol, Inc.
  * Copyright (C) 2008 Funambol, Inc.
  *
  * This program is free software; you can redistribute it and/or modify it under
  * the terms of the GNU Affero General Public License version 3 as published by
  * the Free Software Foundation with the addition of the following permission
  * added to Section 15 as permitted in Section 7(a): FOR ANY PART OF THE COVERED
  * WORK IN WHICH THE COPYRIGHT IS OWNED BY FUNAMBOL, FUNAMBOL DISCLAIMS THE
  * WARRANTY OF NON INFRINGEMENT  OF THIRD PARTY RIGHTS.
  *
  * This program is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
  * details.
  *
  * You should have received a copy of the GNU Affero General Public License
  * along with this program; if not, see http://www.gnu.org/licenses or write to
  * the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
  * MA 02110-1301 USA.
  *
  * You can contact Funambol, Inc. headquarters at 643 Bair Island Road, Suite
  * 305, Redwood City, CA 94063, USA, or at email address info@funambol.com.
  *
  * The interactive user interfaces in modified source and object code versions
  * of this program must display Appropriate Legal Notices, as required under
  * Section 5 of the GNU Affero General Public License version 3.
  *
  * In accordance with Section 7(b) of the GNU Affero General Public License
  * version 3, these Appropriate Legal Notices must retain the display of the
  * "Powered by Funambol" logo. If the display of the logo is not reasonably
  * feasible for technical reasons, the Appropriate Legal Notices must display
  * the words "Powered by Funambol".
  */
 
 package com.funambol.client.customization;
 
 import com.funambol.client.ui.Bitmap;
 import com.funambol.platform.DeviceInfoInterface;
 import com.funambol.sync.client.StorageLimit;
 
 import java.util.Enumeration;
 
 /**
  * Defines the list of customization properties which every single client
  * shall implement.
  */
 public interface Customization {
 
     // Account customization properties
     public String   getServerUriDefault();
     public String   getUserDefault();
     public String   getPasswordDefault();
 
     // Application customization properties
     public String   getApplicationFullname();
     public String   getApplicationTitle();
     public String   getCompanyName();
     public String   getAboutCopyright();
     public String   getAboutSite();
     public String   getLicense();
     public String   getVersion();
     public String   getSupportEmailAddress();
     public boolean  showAboutLicence();
     public boolean  showPoweredBy();
     public String   getPoweredBy();
     public Bitmap   getPoweredByLogo();
     public boolean  showPortalInfo();
     public String   getPortalURL();
 
     // Sources customization properties
     public String   getContactType();
     public String   getCalendarType();
     public String   getTaskType();
     public String   getNoteType();
     public String   getDefaultSourceUri(int id);
     public boolean  isSourceActive(int id);
     public boolean  isSourceEnabledByDefault(int id);
     /**
      * Gets default sync source available direction modes
      * @deprecated use {@link Customization#getDefaultSourceSyncModes(int, DeviceInfoInterface.DeviceRole)} instead
      */
     public int[]    getDefaultSourceSyncModes(int id);
     /**
      * Gets default sync source available direction modes, based on device information
      * @param id
      * @param one of the predefined values of {@link DeviceInfoInterface.DeviceRole}
      */
     public int[]    getDefaultSourceSyncModes(int id, DeviceInfoInterface.DeviceRole deviceRole);
     /**
      * Gets default sync source direction mode
      * @deprecated use {@link Customization#getDefaultSourceSyncMode(int, DeviceInfoInterface.DeviceRole)} instead
      */
     public int      getDefaultSourceSyncMode(int id);
     /**
      * Gets default sync source direction mode, based on device information
      * @param id
      * @param one of the predefined values of {@link DeviceInfoInterface.DeviceRole}
      */
     public int      getDefaultSourceSyncMode(int id, DeviceInfoInterface.DeviceRole deviceRole);
     public Bitmap   getSourceIcon(int id);
     public Bitmap   getSourceDisabledIcon(int id);
     public Enumeration getAvailableSources();
     public int[]    getSourcesOrder();
     public boolean  showNonWorkingSources();
 
     // Settings customization properties
     public boolean  syncUriEditable();
     public boolean  getCheckCredentialsInLoginScreen();
     public boolean  isSourceUriVisible();
     public boolean  isSyncDirectionVisible();
     public boolean  lockLogLevel();
     public int      getLockedLogLevel();
     public boolean  isLogEnabledInSettingsScreen();
     public boolean  sendLogEnabled();
     public String   getLogFileName();
     public boolean  isBandwidthSaverEnabled();
     public boolean  useBandwidthSaverContacts();
     public boolean  useBandwidthSaverEvents();
     public boolean  useBandwidthSaverMedia();
     public boolean  enableRefreshCommand();
     public int      getDefaultPollingInterval();
     public boolean  isS2CSmsPushEnabled();
     public int      getS2CPushSmsPort();
     public int      getDefaultSyncMode();
     public int[]    getPollingPimIntervalChoices();
     public boolean  showSyncModeInSettingsScreen();
     public boolean  showC2SPushInSettingsScreen();
     public int[]    getAvailableSyncModes();
 
     // Home screen customization properties
     public boolean  syncAllOnMainScreenRequired();
     public boolean  syncAllActsAsCancelSync();
     public boolean  showSyncIconOnSelection();
     public Bitmap   getSyncAllIcon();
     public Bitmap   getSyncAllBackground();
     public Bitmap   getSyncAllHighlightedBackground();
     public Bitmap   getButtonBackground();
     public Bitmap   getButtonHighlightedBackground();
     public Bitmap   getOkIcon();
     public Bitmap   getErrorIcon();
     public Bitmap   getStatusSelectedIcon();
     public Bitmap[] getStatusIconsForAnimation();
     public Bitmap[] getStatusHugeIconsForAnimation();
 
     // MSU customization properties
     public boolean  getMobileSignupEnabled();
     public int      getDefaultMSUValidationMode();
     public boolean  getShowSignupSuccededMessage();
     public boolean  getAddShowPasswordField();
     public String   getTermsAndConditionsUrl();
     public String   getPrivacyPolicyUrl();
     public boolean  getPrefillPhoneNumber();
 
     // Misc customization properties
     public boolean  getUseWbxml();
     public boolean  getContactsImportEnabled();
     public int      getDefaultAuthType();
     public boolean  checkForUpdates();
     public boolean  enableUpdaterManager();
     public long     getCheckUpdtIntervalDefault();
     public long     getReminderUpdtIntervalDefault();
     public String   getHttpUploadPrefix();
     public StorageLimit getStorageLimit();
     public boolean getCheckCredentialsViaConfigSync();
 
     // Max allowed file size for media
     public long getMaxAllowedFileSizeForFiles();
     public long getMaxAllowedFileSizeForVideos();
 
     /**
      * First sync behavior on upload for media
      * @param sourceId id of the media sync source
      * @param deviceRole kind of device
      *
      * @return max number of items to send to server upon first sync
      */
     public int getFirstSyncMediaUploadLimit(int sourceId, DeviceInfoInterface.DeviceRole deviceRole);
     /**
      * First sync behavior on download for media
      * @param sourceId id of the media sync source
      * @param deviceRole kind of device
      *
      * @return max number of items to receive from server upon first sync
      */
     public int getFirstSyncMediaDownloadLimit(int sourceId, DeviceInfoInterface.DeviceRole deviceRole);
 
     /**
      * Specifies if the user shall be warned before accessing the network at the
      * beginning of each sync
      */
     public boolean getShowNetworkUsageWarningForProfiles();
 
 }
 
