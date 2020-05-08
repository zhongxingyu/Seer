 /*-
  * Copyright (c) 2006, Derek Konigsberg
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions
  * are met:
  *
  * 1. Redistributions of source code must retain the above copyright
  *    notice, this list of conditions and the following disclaimer.
  * 2. Redistributions in binary form must reproduce the above copyright
  *    notice, this list of conditions and the following disclaimer in the
  *    documentation and/or other materials provided with the distribution.
  * 3. Neither the name of the project nor the names of its
  *    contributors may be used to endorse or promote products derived
  *    from this software without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
  * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
  * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
  * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
  * COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
  * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
  * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
  * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
  * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
  * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
  * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
  * OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 package org.logicprobe.LogicMail.conf;
 
 import org.logicprobe.LogicMail.PlatformInfo;
 import org.logicprobe.LogicMail.util.Serializable;
 import org.logicprobe.LogicMail.util.SerializableHashtable;
 import org.logicprobe.LogicMail.util.UniqueIdGenerator;
 
 import java.io.DataInput;
 import java.io.DataOutput;
 import java.io.IOException;
 
 import javax.microedition.io.Connector;
 import javax.microedition.io.file.FileConnection;
 
 /**
  * Store the global configuration for LogicMail.
  */
 public class GlobalConfig implements Serializable {
     private long uniqueId;
     private int changeType;
     
     /** Global network settings. */
     public static final int CHANGE_TYPE_NETWORK = 0x01;
     /** Global data storage settings. */
     public static final int CHANGE_TYPE_DATA = 0x02;
     /** Global prompt settings. */
     public static final int CHANGE_TYPE_PROMPTS = 0x04;
     /** Global other settings. */
     public static final int CHANGE_TYPE_OTHER = 0x08;
 
     /** Prefer plain text display for messages */
     public static final int MESSAGE_DISPLAY_PLAIN_TEXT = 0;
     /** Prefer HTML display for messages */
     public static final int MESSAGE_DISPLAY_HTML = 1;
 
     public static final int EXPUNGE_PROMPT = 0;
     public static final int EXPUNGE_ALWAYS = 1;
     public static final int EXPUNGE_NEVER = 2;
     
     /** language code to use for the UI, or null for system default */
     private String languageCode;
     /** true to enable Unicode normalization */
     private boolean unicodeNormalization;
     /** Preferred message display format */
     private int messageDisplayFormat;
     /** True for ascending, false for descending */
     private boolean dispOrder;
     /** Filesystem for local file storage */
     private String filesystemRoot = "";
     /** Preferred network transport type */
     private int transportType;
     /** Whether WiFi should be used if available */
     private boolean enableWiFi;
     /** Connection debugging */
     private boolean connDebug;
     /** Hide deleted messages */
     private boolean hideDeletedMsg;
     /** Local host name override */
     private String localHostname;
     /** Whether to prompt on deleting messages. */
     private boolean promptOnDelete;
     /** The expunge-related behavior when leaving a mailbox. */
     private int expungeMode;
 
     public static String FILE_URL_PREFIX = "file:///";
     public static String FILESYSTEM_DISABLED = "<NONE>";
 
     /**
      * Instantiates a new global configuration.
      */
     public GlobalConfig() {
         setDefaults();
     }
 
     /**
      * Instantiates a new global configuration.
      * 
      * @param input The input data to deserialize the contents from
      */
     public GlobalConfig(DataInput input) {
         try {
             deserialize(input);
         } catch (IOException ex) {
             setDefaults();
         }
     }
 
     /**
      * Sets the defaults.
      */
     private void setDefaults() {
         uniqueId = UniqueIdGenerator.getInstance().getUniqueId();
         this.languageCode = "";
         this.unicodeNormalization = false;
         this.messageDisplayFormat = GlobalConfig.MESSAGE_DISPLAY_PLAIN_TEXT;
         this.dispOrder = false;
         this.transportType = ConnectionConfig.TRANSPORT_AUTO;
         this.enableWiFi = true;
         this.hideDeletedMsg = true;
         this.localHostname = "";
         this.filesystemRoot = "";
         this.promptOnDelete = true;
         this.expungeMode = GlobalConfig.EXPUNGE_PROMPT;
         changeType = 0;
     }
 
     /**
      * Sets the language code.
      * 
      * @param languageCode the new language code, or an empty string for the system default.
      */
     public void setLanguageCode(String languageCode) {
         if(!this.languageCode.equals(languageCode)) {
             this.languageCode = languageCode;
             changeType |= CHANGE_TYPE_OTHER;
         }
     }
 
     /**
      * Gets the language code.
      * 
      * @return the language code, or an empty string for the system default.
      */
     public String getLanguageCode() {
         return languageCode;
     }
 
     /**
      * Sets whether unicode normalization is enabled.
      * 
      * @param unicodeNormalization True if unicode normalization is enabled
      */
     public void setUnicodeNormalization(boolean unicodeNormalization) {
         if(this.unicodeNormalization != unicodeNormalization) {
             this.unicodeNormalization = unicodeNormalization;
             changeType |= CHANGE_TYPE_OTHER;
         }
     }
 
     /**
      * Gets whether unicode normalization is enabled.
      * 
      * @return True if unicode normalization is enabled
      */
     public boolean getUnicodeNormalization() {
         return unicodeNormalization;
     }
 
     /**
      * Sets the preferred message display format.
      * 
      * @param messageDisplayFormat the new preferred message display format
      */
     public void setMessageDisplayFormat(int messageDisplayFormat) {
         if(this.messageDisplayFormat != messageDisplayFormat) {
             this.messageDisplayFormat = messageDisplayFormat;
             changeType |= CHANGE_TYPE_OTHER;
         }
     }
 
     /**
      * Gets the preferred message display format.
      * 
      * @return the preferred message display format
      */
     public int getMessageDisplayFormat() {
         return messageDisplayFormat;
     }
 
     /**
      * Set the message display order.
      * 
      * @param dispOrder True for ascending, false for descending
      */
     public void setDispOrder(boolean dispOrder) {
         if(this.dispOrder != dispOrder) {
             this.dispOrder = dispOrder;
             changeType |= CHANGE_TYPE_OTHER;
         }
     }
 
     /**
      * Get the message display order.
      * 
      * @return True for ascending, false for descending
      */
     public boolean getDispOrder() {
         return dispOrder;
     }
 
     /**
      * Get the root filesystem URL for local file storage.
      * 
      * @param filesystemRoot The root URL of the local filesystem, or {@link #FILESYSTEM_DISABLED}.
      */
     public void setFilesystemRoot(String filesystemRoot) {
         if(filesystemRoot == null ||
                 !(filesystemRoot.startsWith(FILE_URL_PREFIX)
                 || filesystemRoot.equals(FILESYSTEM_DISABLED))) {
             throw new IllegalArgumentException();
         }
         if(this.filesystemRoot == null || !this.filesystemRoot.equals(filesystemRoot)) {
             this.filesystemRoot = filesystemRoot;
             changeType |= CHANGE_TYPE_DATA;
         }
     }
     
     /**
      * Set the root filesystem URL for local file storage.
      * 
      * @return The root URL of the local filesystem, or {@link #FILESYSTEM_DISABLED}.
      */
     public String getFilesystemRoot() {
         return filesystemRoot;
     }
     
     /**
      * Set the root URL for local file storage.
      * 
      * @return The valid local data URL, or <code>null</code>.
      */
     public String getLocalDataLocation() {
         if(filesystemRoot == null || filesystemRoot.length() == 0
                 || filesystemRoot.equals(FILESYSTEM_DISABLED)) {
             return null;
         }
         else {
             String validLocation = validateLocalDataLocation(filesystemRoot);
             if(validLocation != null && validLocation.length() > 0) {
                 return validLocation;
             }
             else {
                 return null;
             }
         }
     }
 
     /**
      * Gets the preferred network transport type.
      * 
      * @return the preferred network transport type
      */
     public int getTransportType() {
         return transportType;
     }
 
     /**
      * Sets the preferred network transport type.
      * 
      * @param transportType the new preferred network transport type
      */
     public void setTransportType(int transportType) {
         if(this.transportType != transportType) {
             this.transportType = transportType;
             changeType |= CHANGE_TYPE_NETWORK;
         }
     }
 
     /**
      * Gets whether to use WiFi if available.
      * 
      * @return whether to use WiFi if available
      */
     public boolean getEnableWiFi() {
         return enableWiFi;
     }
 
     /**
      * Sets whether to use WiFi if available.
      * 
      * @param enableWiFi whether to use WiFi if available
      */
     public void setEnableWiFi(boolean enableWiFi) {
         if(this.enableWiFi != enableWiFi) {
             this.enableWiFi = enableWiFi;
             changeType |= CHANGE_TYPE_NETWORK;
         }
     }
 
     /**
      * Gets the connection debugging mode.
      * 
      * @return True to enable, false to disable
      */
     public boolean getConnDebug() {
         return connDebug;
     }
 
     /**
      * Sets the connection debugging mode.
      * 
      * @param connDebug True if enabled, false if disabled
      */
     public void setConnDebug(boolean connDebug) {
         if(this.connDebug != connDebug) {
             this.connDebug = connDebug;
             changeType |= CHANGE_TYPE_OTHER;
         }
     }
 
     /**
      * Gets whether deleted messages should be hidden.
      * 
      * @return True if hidden, false if shown
      */
     public boolean getHideDeletedMsg() {
         return hideDeletedMsg;
     }
 
     /**
      * Sets whether deleted messages should be hidden.
      * 
      * @param hideDeletedMsg True to hide, false to show
      */
     public void setHideDeletedMsg(boolean hideDeletedMsg) {
         if(this.hideDeletedMsg != hideDeletedMsg) {
             this.hideDeletedMsg = hideDeletedMsg;
             changeType |= CHANGE_TYPE_OTHER;
         }
     }
 
     /**
      * Gets the local hostname override.
      * 
      * @return The local hostname
      */
     public String getLocalHostname() {
         return this.localHostname;
     }
 
     /**
      * Sets the local hostname override.
      * 
      * @param localHostname The local hostname
      */
     public void setLocalHostname(String localHostname) {
         if(!this.localHostname.equals(localHostname)) {
             this.localHostname = localHostname;
             changeType |= CHANGE_TYPE_OTHER;
         }
     }
 
     /**
      * Gets whether to prompt on message delete.
      * 
      * @return the prompt on delete setting
      */
     public boolean getPromptOnDelete() {
         return promptOnDelete;
     }
     
     /**
      * Sets whether to prompt on message delete.
      * 
      * @param promptOnDelete the new prompt on delete setting
      */
     public void setPromptOnDelete(boolean promptOnDelete) {
         if(this.promptOnDelete != promptOnDelete) {
             this.promptOnDelete = promptOnDelete;
             changeType |= CHANGE_TYPE_PROMPTS;
         }
     }
 
     /**
      * Gets the expunge mode.
      * 
      * @return the expunge mode
      */
     public int getExpungeMode() {
         return expungeMode;
     }
     
     /**
      * Sets the expunge mode.
      * 
      * @param expungeMode the new expunge mode
      */
     public void setExpungeMode(int expungeMode) {
         if(this.expungeMode != expungeMode) {
             this.expungeMode = expungeMode;
             changeType |= CHANGE_TYPE_PROMPTS;
         }
     }
     
     /* (non-Javadoc)
      * @see org.logicprobe.LogicMail.util.Serializable#serialize(java.io.DataOutput)
      */
     public void serialize(DataOutput output) throws IOException {
         output.writeLong(uniqueId);
 
         SerializableHashtable table = new SerializableHashtable();
 
         table.put("global_languageCode", languageCode);
         table.put("global_unicodeNormalization", new Boolean(unicodeNormalization));
         table.put("global_messageDisplayFormat", new Integer(messageDisplayFormat));
         table.put("global_dispOrder", new Boolean(dispOrder));
         table.put("global_filesystemRoot", filesystemRoot);
         table.put("global_transportType", new Integer(transportType));
         table.put("global_enableWiFi", new Boolean(enableWiFi));
         table.put("global_connDebug", new Boolean(connDebug));
         table.put("global_hideDeletedMsg", new Boolean(hideDeletedMsg));
         table.put("global_localHostname", localHostname);
         table.put("global_promptOnDelete", new Boolean(promptOnDelete));
         table.put("global_expungeMode", new Integer(expungeMode));
 
         table.serialize(output);
         changeType = 0;
     }
 
     /* (non-Javadoc)
      * @see org.logicprobe.LogicMail.util.Serializable#deserialize(java.io.DataInput)
      */
     public void deserialize(DataInput input) throws IOException {
         setDefaults();
         uniqueId = input.readLong();
 
         SerializableHashtable table = new SerializableHashtable();
         table.deserialize(input);
 
         Object value;
 
         value = table.get("global_languageCode");
         if(value instanceof String) {
             languageCode = (String)value;
         }
         value = table.get("global_unicodeNormalization");
         if(value instanceof Boolean) {
             unicodeNormalization = ((Boolean)value).booleanValue();
         }
         value = table.get("global_messageDisplayFormat");
         if (value instanceof Integer) {
             messageDisplayFormat = ((Integer) value).intValue();
         }
         value = table.get("global_dispOrder");
         if (value instanceof Boolean) {
             dispOrder = ((Boolean) value).booleanValue();
         }
         value = table.get("global_filesystemRoot");
         if (value instanceof String) {
             this.filesystemRoot = (String)value;
         }
         value = table.get("global_transportType");
         if(value instanceof Integer) {
             transportType = ((Integer)value).intValue();
         }
         value = table.get("global_enableWiFi");
         if(value instanceof Boolean) {
             enableWiFi = ((Boolean)value).booleanValue();
         }
         value = table.get("global_connDebug");
         if (value instanceof Boolean) {
             connDebug = ((Boolean) value).booleanValue();
         }
         value = table.get("global_hideDeletedMsg");
         if (value instanceof Boolean) {
             hideDeletedMsg = ((Boolean) value).booleanValue();
         }
         value = table.get("global_localHostname");
         if (value instanceof String) {
             localHostname = (String) value;
         }
         value = table.get("global_promptOnDelete");
         if(value instanceof Boolean) {
             promptOnDelete = ((Boolean)value).booleanValue();
         }
         value = table.get("global_expungeMode");
         if(value instanceof Integer) {
             expungeMode = ((Integer)value).intValue();
         }
         
         if(filesystemRoot == null || filesystemRoot.length() == 0) {
             // Filesystem not set, and not disabled either, so we default to
             // picking the first usable filesystem.  If none is available, then
             // we mark file support as disabled.
             String[] fsRoots = PlatformInfo.getInstance().getFilesystemRoots();
             if(fsRoots.length > 0) {
                 filesystemRoot = fsRoots[0];
             }
             else {
                 filesystemRoot = FILESYSTEM_DISABLED;
             }
         }
        
        changeType = 0;
     }
 
     /* (non-Javadoc)
      * @see org.logicprobe.LogicMail.util.Serializable#getUniqueId()
      */
     public long getUniqueId() {
         return uniqueId;
     }
 
     /**
      * Checks if this object has changed since it was last saved.
      * 
      * @return the change type, if applicable
      */
     public int getChangeType() {
         return changeType;
     }
 
     /**
      * Checks provided filesystem root to make sure it exists, creating any
      * intermediate directories as necessary, and returns a fully qualified
      * file URL.
      * 
      * @param fsRoot filesystem root to validate
      * @return fully qualified and valid file URL, or null if one could not
      * be created
      */
     private static String validateLocalDataLocation(String fsRoot) {
         String url;
         if(fsRoot != null) {
             // Clean up the string, removing everything but the base
             int p = fsRoot.indexOf('/', FILE_URL_PREFIX.length() - 1);
             int q = fsRoot.indexOf('/', p + 1);
             if(p != -1 && q != -1 && p < q) {
                 fsRoot = fsRoot.substring(p + 1, q + 1);
             }
 
             // Add the prefix
             url = FILE_URL_PREFIX + fsRoot;
 
             // Append the necessary elements, creating directories as necessary
             if(url.indexOf("Card/") != -1) {
                 try {
                     FileConnection conn = (FileConnection)Connector.open(url + "BlackBerry/");
                     if(!conn.exists()) { conn.mkdir(); }
                     url = conn.getURL(); conn.close();
 
                     conn = (FileConnection)Connector.open(url + "logicmail/");
                     if(!conn.exists()) { conn.mkdir(); }
                     url = conn.getURL(); conn.close();
                 } catch (IOException e) {
                     url = null;
                 }
             }
             else if(url.indexOf("store/") != -1) {
                 try {
                     FileConnection conn = (FileConnection)Connector.open(url + "home/");
                     if(!conn.exists()) { conn.mkdir(); }
                     url = conn.getURL(); conn.close();
 
                     conn = (FileConnection)Connector.open(url + "user/");
                     if(!conn.exists()) { conn.mkdir(); }
                     url = conn.getURL(); conn.close();
 
                     conn = (FileConnection)Connector.open(url + "logicmail/");
                     if(!conn.exists()) { conn.mkdir(); }
                     url = conn.getURL(); conn.close();
                 } catch (IOException e) {
                     url = null;
                 }
             }
             else {
                 try {
                     FileConnection conn = (FileConnection)Connector.open(url + "logicmail/");
                     if(!conn.exists()) { conn.mkdir(); }
                     url = conn.getURL(); conn.close();
                 } catch (IOException e) {
                     url = null;
                 }
             }
         }
         else {
             url = null;
         }
         return url;
     }
 }
