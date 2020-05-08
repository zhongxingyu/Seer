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
 package org.logicprobe.LogicMail.ui;
 
 import net.rim.device.api.ui.Field;
 import net.rim.device.api.ui.FieldChangeListener;
 import net.rim.device.api.ui.component.BasicEditField;
 import net.rim.device.api.ui.component.ButtonField;
 import net.rim.device.api.ui.component.CheckboxField;
 import net.rim.device.api.ui.component.ObjectChoiceField;
 import net.rim.device.api.ui.component.RichTextField;
 import net.rim.device.api.ui.component.SeparatorField;
 import net.rim.device.api.ui.text.TextFilter;
 
 import org.logicprobe.LogicMail.LogicMailResource;
 import org.logicprobe.LogicMail.conf.GlobalConfig;
 import org.logicprobe.LogicMail.conf.MailSettings;
 
 
 /**
  * Configuration screen
  */
 public class GlobalConfigScreen extends BaseCfgScreen
     implements FieldChangeListener {
     private MailSettings mailSettings;
     private BasicEditField fldRetMsgCount;
     private ObjectChoiceField fldDispOrder;
     private BasicEditField fldImapMaxMsgSize;
     private BasicEditField fldImapMaxFolderDepth;
     private BasicEditField fldPopMaxLines;
     private ObjectChoiceField fldWifiMode;
     private CheckboxField fldConnDebug;
     private CheckboxField fldHideDeletedMsg;
     private CheckboxField fldOverrideHostname;
     private BasicEditField fldLocalHostname;
     private ButtonField btSave;
     private String localHostname;
 
     public GlobalConfigScreen() {
         super("LogicMail - " +
             resources.getString(LogicMailResource.CONFIG_GLOBAL_TITLE));
 
         mailSettings = MailSettings.getInstance();
 
         GlobalConfig config = mailSettings.getGlobalConfig();
        localHostname = config.getLocalHostname();
 
         add(new RichTextField(resources.getString(
                     LogicMailResource.CONFIG_GLOBAL_GLOBAL_SETTINGS),
                 Field.NON_FOCUSABLE));
 
         fldRetMsgCount = new BasicEditField("  " +
                 resources.getString(
                     LogicMailResource.CONFIG_GLOBAL_MESSAGE_COUNT) + " ",
                 Integer.toString(config.getRetMsgCount()));
         fldRetMsgCount.setFilter(TextFilter.get(TextFilter.NUMERIC));
         add(fldRetMsgCount);
 
         String[] orderTypes = {
                 resources.getString(LogicMailResource.MENUITEM_ORDER_ASCENDING),
                 resources.getString(LogicMailResource.MENUITEM_ORDER_DESCENDING)
             };
 
         if (!config.getDispOrder()) {
             fldDispOrder = new ObjectChoiceField("  " +
                     resources.getString(
                         LogicMailResource.CONFIG_GLOBAL_MESSAGE_ORDER) + " ",
                     orderTypes, 0);
         } else {
             fldDispOrder = new ObjectChoiceField("  " +
                     resources.getString(
                         LogicMailResource.CONFIG_GLOBAL_MESSAGE_ORDER) + " ",
                     orderTypes, 1);
         }
 
         add(fldDispOrder);
 
         fldHideDeletedMsg = new CheckboxField(resources.getString(
                     LogicMailResource.CONFIG_GLOBAL_HIDE_DELETED_MESSAGES),
                 config.getHideDeletedMsg());
         add(fldHideDeletedMsg);
 
         String[] wifiModes = {
                 resources.getString(LogicMailResource.MENUITEM_DISABLED),
                 resources.getString(LogicMailResource.MENUITEM_PROMPT),
                 resources.getString(LogicMailResource.MENUITEM_ALWAYS)
             };
         fldWifiMode = new ObjectChoiceField("  " +
                 resources.getString(LogicMailResource.CONFIG_GLOBAL_WIFI_MODE) +
                 " ", wifiModes, config.getWifiMode());
         add(fldWifiMode);
 
         add(new RichTextField(resources.getString(
                     LogicMailResource.CONFIG_GLOBAL_IMAP_SETTINGS),
                 Field.NON_FOCUSABLE));
         fldImapMaxMsgSize = new BasicEditField("  " +
                 resources.getString(
                     LogicMailResource.CONFIG_GLOBAL_IMAP_DOWNLOAD_LIMIT) + " ",
                 Integer.toString(config.getImapMaxMsgSize() / 1024));
         fldImapMaxMsgSize.setFilter(TextFilter.get(TextFilter.NUMERIC));
         add(fldImapMaxMsgSize);
 
         fldImapMaxFolderDepth = new BasicEditField("  " +
                 resources.getString(
                     LogicMailResource.CONFIG_GLOBAL_IMAP_FOLDER_LIMIT) + " ",
                 Integer.toString(config.getImapMaxFolderDepth()));
         fldImapMaxFolderDepth.setFilter(TextFilter.get(TextFilter.NUMERIC));
         add(fldImapMaxFolderDepth);
 
         add(new RichTextField(resources.getString(
                     LogicMailResource.CONFIG_GLOBAL_POP_SETTINGS),
                 Field.NON_FOCUSABLE));
         fldPopMaxLines = new BasicEditField("  " +
                 resources.getString(
                     LogicMailResource.CONFIG_GLOBAL_POP_DOWNLOAD_LIMIT) + " ",
                 Integer.toString(config.getPopMaxLines()));
         fldPopMaxLines.setFilter(TextFilter.get(TextFilter.NUMERIC));
         add(fldPopMaxLines);
 
         boolean overrideHostname = localHostname.length() > 0;
        fldOverrideHostname = new CheckboxField(resources.getString(
                    LogicMailResource.CONFIG_GLOBAL_OVERRIDE_HOSTNAME),
                 overrideHostname);
         fldOverrideHostname.setChangeListener(this);
         add(fldOverrideHostname);
 
         if (overrideHostname) {
            fldLocalHostname = new BasicEditField("  " +
                    resources.getString(
                        LogicMailResource.CONFIG_GLOBAL_HOSTNAME) + " ",
                    localHostname);
         } else {
             String hostname = System.getProperty("microedition.hostname");
            fldLocalHostname = new BasicEditField("  " +
                    resources.getString(
                        LogicMailResource.CONFIG_GLOBAL_HOSTNAME) + " ",
                     ((hostname != null) ? hostname : "localhost"));
             fldLocalHostname.setEditable(false);
         }
 
         add(fldLocalHostname);
         fldConnDebug = new CheckboxField(resources.getString(
                     LogicMailResource.CONFIG_GLOBAL_CONNECTION_DEBUGGING),
                 config.getConnDebug());
         add(fldConnDebug);
 
         add(new SeparatorField());
 
         btSave = new ButtonField(resources.getString(
                     LogicMailResource.MENUITEM_SAVE), Field.FIELD_HCENTER);
         btSave.setChangeListener(this);
         add(btSave);
     }
 
     public void fieldChanged(Field field, int context) {
         if (field == btSave) {
             onClose();
         } else if (field == fldOverrideHostname) {
             if (fldOverrideHostname.getChecked()) {
                 fldLocalHostname.setText(localHostname);
                 fldLocalHostname.setEditable(true);
             } else {
                 String hostname = System.getProperty("microedition.hostname");
                 fldLocalHostname.setText((hostname != null) ? hostname
                                                             : "localhost");
                 fldLocalHostname.setEditable(false);
             }
         }
     }
 
     public void save() {
         GlobalConfig config = mailSettings.getGlobalConfig();
 
         try {
             config.setRetMsgCount(Integer.parseInt(fldRetMsgCount.getText()));
         } catch (Exception e) {
         }
 
         if (fldDispOrder.getSelectedIndex() == 0) {
             config.setDispOrder(false);
         } else {
             config.setDispOrder(true);
         }
 
         config.setHideDeletedMsg(fldHideDeletedMsg.getChecked());
 
         config.setWifiMode(fldWifiMode.getSelectedIndex());
 
         try {
             config.setImapMaxMsgSize(Integer.parseInt(
                     fldImapMaxMsgSize.getText()) * 1024);
         } catch (Exception e) {
         }
 
         try {
             config.setImapMaxFolderDepth(Integer.parseInt(
                     fldImapMaxFolderDepth.getText()));
         } catch (Exception e) {
         }
 
         try {
             config.setPopMaxLines(Integer.parseInt(fldPopMaxLines.getText()));
         } catch (Exception e) {
         }
 
         if (fldOverrideHostname.getChecked()) {
             config.setLocalHostname(fldLocalHostname.getText().trim());
         } else {
             config.setLocalHostname("");
         }
 
         config.setConnDebug(fldConnDebug.getChecked());
         mailSettings.saveSettings();
     }
 }
