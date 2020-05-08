 /*-
  * Copyright (c) 2008, Derek Konigsberg
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
 
 import java.util.Vector;
 
 import net.rim.device.api.ui.Field;
 import net.rim.device.api.ui.FieldChangeListener;
 import net.rim.device.api.ui.Font;
 import net.rim.device.api.ui.Manager;
 import net.rim.device.api.ui.UiApplication;
 import net.rim.device.api.ui.component.BasicEditField;
 import net.rim.device.api.ui.component.CheckboxField;
 import net.rim.device.api.ui.component.Dialog;
 import net.rim.device.api.ui.component.LabelField;
 import net.rim.device.api.ui.component.NumericChoiceField;
 import net.rim.device.api.ui.component.ObjectChoiceField;
 import net.rim.device.api.ui.component.PasswordEditField;
 import net.rim.device.api.ui.component.SeparatorField;
 import net.rim.device.api.ui.component.TextField;
 import net.rim.device.api.ui.text.TextFilter;
 
 import org.logicprobe.LogicMail.LogicMailResource;
 import org.logicprobe.LogicMail.conf.AccountConfig;
 import org.logicprobe.LogicMail.conf.ConnectionConfig;
 import org.logicprobe.LogicMail.conf.IdentityConfig;
 import org.logicprobe.LogicMail.conf.ImapConfig;
 import org.logicprobe.LogicMail.conf.MailSettings;
 import org.logicprobe.LogicMail.conf.OutgoingConfig;
 import org.logicprobe.LogicMail.conf.PopConfig;
 import org.logicprobe.LogicMail.model.AccountNode;
 import org.logicprobe.LogicMail.model.MailManager;
 import org.logicprobe.LogicMail.model.MailRootNode;
 import org.logicprobe.LogicMail.model.MailboxNode;
 
 /**
  * Account configuration screen
  */
 public class AccountConfigScreen extends AbstractConfigScreen {
     // Top-level fields
     private BorderedFieldManager headerFieldManager;
     private BasicEditField accountNameField;
     private ObjectChoiceField pageField;
     private Manager contentFieldManager;
 
     // Basic settings fields
     private LabelField incomingServerLabelField;
     private BasicEditField serverNameField;
     private ObjectChoiceField serverSecurityField;
     private BasicEditField serverPortField;
     private BasicEditField serverUserField;
     private PasswordEditField serverPassField;
     private ObjectChoiceField identityField;
     private ObjectChoiceField outgoingServerField;
 
     // Folder settings fields
     private LabelField sentFolderChoiceLabel;
     private LabelField sentFolderChoiceButtonLabel;
     private LabelField draftFolderChoiceLabel;
     private LabelField draftFolderChoiceButtonLabel;
 
     // Advanced settings fields (both)
     private NumericChoiceField initialFolderMessagesChoiceField;
     private NumericChoiceField folderMessageIncrementChoiceField;
     private NumericChoiceField maximumFolderMessagesChoiceField;
     private ObjectChoiceField networkTransportChoiceField;
     private CheckboxField enableWiFiCheckboxField;
 
     // Advanced settings fields (IMAP)
     private BasicEditField imapFolderPrefixField;
     private BasicEditField imapMaxMessageSizeEditField;
     private BasicEditField imapMaxFolderDepthEditField;
     private CheckboxField imapShowOnlySubscribedField;
     // Advanced settings fields (POP)
     private BasicEditField popMaxLinesEditField;
 
     private Manager[] pageFieldManagers;
    private static final int PAGE_BASIC    = 0;
    private static final int PAGE_FOLDER   = 1;
    private static final int PAGE_ADVANCED = 2;
    
     private boolean accountSaved;
     private boolean createDefaultIdentity;
     private AccountConfig accountConfig;
     private IdentityConfig[] identityConfigs;
     private OutgoingConfig[] outgoingConfigs;
     private MailboxNode selectedSentFolder;
     private MailboxNode selectedDraftFolder;
     private FieldChangeListener fieldChangeListener;
 
     private static class NullOutgoingConfig extends OutgoingConfig {
         public String toString() {
             return resources.getString(LogicMailResource.CONFIG_ACCOUNT_NONE);
         }
         public long getUniqueId() {
             return -1;
         }
     }
 
     /**
      * Instantiates a new account configuration screen.
      * 
      * @param acctConfig The account configuration instance.
      */
     public AccountConfigScreen(AccountConfig accountConfig) {
         super(getTitle(accountConfig));
 
         this.accountConfig = accountConfig;
         this.accountSaved = false;
 
         MailSettings mailSettings = MailSettings.getInstance();
 
         int numIdentities = mailSettings.getNumIdentities();
         if(numIdentities > 0) {
             identityConfigs = new IdentityConfig[numIdentities];
             for(int i=0; i<numIdentities; ++i) {
                 identityConfigs[i] = mailSettings.getIdentityConfig(i);
             }
             createDefaultIdentity = false;
         }
         else {
             identityConfigs = new IdentityConfig[1];
             identityConfigs[0] = new IdentityConfig();
             identityConfigs[0].setIdentityName("Default");
             createDefaultIdentity = true;
             this.setDirty(true);
         }
 
         int numOutgoing = mailSettings.getNumOutgoing();
         outgoingConfigs = new OutgoingConfig[numOutgoing+1];
         outgoingConfigs[0] = new NullOutgoingConfig();
         for(int i=0; i<numOutgoing; ++i) {
             outgoingConfigs[i+1] = mailSettings.getOutgoingConfig(i);
         }
 
         fieldChangeListener = new FieldChangeListener() {
             public void fieldChanged(Field field, int context) {
                 AcctCfgScreen_fieldChanged(field, context);
             }};
 
         initFields();
 
         IdentityConfig selectedIdentityConfig = this.accountConfig.getIdentityConfig();
         if(selectedIdentityConfig != null) {
             identityField.setSelectedIndex(selectedIdentityConfig);
         }
 
         OutgoingConfig selectedOutgoingConfig = this.accountConfig.getOutgoingConfig();
         if(selectedOutgoingConfig != null) {
             outgoingServerField.setSelectedIndex(selectedOutgoingConfig);
         }
     }
 
     private static String getTitle(AccountConfig accountConfig) {
         StringBuffer buf = new StringBuffer();
         buf.append(resources.getString(LogicMailResource.APPNAME));
         buf.append(" - ");
         buf.append(resources.getString(LogicMailResource.CONFIG_ACCOUNT_TITLE));
         if(accountConfig instanceof ImapConfig) {
             buf.append(" (IMAP)");
         }
         else if(accountConfig instanceof PopConfig) {
             buf.append(" (POP)");
         }
         return buf.toString();
     }
 
     /**
      * Initializes the UI fields.
      */
     private void initFields() {
         Font boldFont = getFont().derive(Font.BOLD);
 
         accountNameField = new BasicEditField(
                 resources.getString(LogicMailResource.CONFIG_ACCOUNT_NAME) + ' ',
                 accountConfig.getAcctName());
         accountNameField.setFont(boldFont);
 
         pageField = new ObjectChoiceField(
                 resources.getString(LogicMailResource.CONFIG_ACCOUNT_PAGE),
                 new String[] {
                     resources.getString(LogicMailResource.CONFIG_ACCOUNT_PAGE_BASIC),
                     resources.getString(LogicMailResource.CONFIG_ACCOUNT_PAGE_FOLDER),
                     resources.getString(LogicMailResource.CONFIG_ACCOUNT_PAGE_ADVANCED)
                 });
 
         pageField.setChangeListener(fieldChangeListener);
 
         pageFieldManagers = new Manager[3];
        pageFieldManagers[PAGE_BASIC] = initFieldsBasic();
        pageFieldManagers[PAGE_FOLDER] = initFieldsFolder();
        pageFieldManagers[PAGE_ADVANCED] = initFieldsAdvanced();
 
         // Container for the active settings page
        contentFieldManager = pageFieldManagers[PAGE_BASIC];
 
         headerFieldManager = new BorderedFieldManager(BorderedFieldManager.BOTTOM_BORDER_NONE);
         headerFieldManager.add(accountNameField);
         headerFieldManager.add(pageField);
         add(headerFieldManager);
         add(contentFieldManager);
     }
 
     /**
      * Initializes the UI fields for the basic settings page.
      */
     private Manager initFieldsBasic() {
         Font boldFont = getFont().derive(Font.BOLD);
 
         Manager manager = new BorderedFieldManager(BorderedFieldManager.BOTTOM_BORDER_NORMAL | Field.USE_ALL_HEIGHT);
         incomingServerLabelField = new LabelField(
                 resources.getString(LogicMailResource.CONFIG_ACCOUNT_INCOMING_SERVER),
                 Field.NON_FOCUSABLE);
         incomingServerLabelField.setFont(boldFont);
 
         serverNameField = new HostnameEditField(resources.getString(LogicMailResource.CONFIG_ACCOUNT_SERVER) + ' ', accountConfig.getServerName());
         serverSecurityField = new ObjectChoiceField(
                 resources.getString(LogicMailResource.CONFIG_ACCOUNT_SECURITY),
                 new Object[] {
                     resources.getString(LogicMailResource.CONFIG_ACCOUNT_SECURITY_NEVER),
                     resources.getString(LogicMailResource.CONFIG_ACCOUNT_SECURITY_TLS_IF_AVAILABLE),
                     resources.getString(LogicMailResource.CONFIG_ACCOUNT_SECURITY_TLS),
                     resources.getString(LogicMailResource.CONFIG_ACCOUNT_SECURITY_SSL)},
                     accountConfig.getServerSecurity());
         serverSecurityField.setChangeListener(fieldChangeListener);
         serverPortField = new BasicEditField(
                 resources.getString(LogicMailResource.CONFIG_ACCOUNT_PORT) + ' ',
                 Integer.toString(accountConfig.getServerPort()),
                 5, TextField.NO_NEWLINE);
         serverPortField.setFilter(TextFilter.get(TextFilter.NUMERIC));
         serverUserField = new BasicEditField(resources.getString(
                 LogicMailResource.CONFIG_ACCOUNT_USERNAME) + ' ',
                 accountConfig.getServerUser(),
                 256, TextField.NO_NEWLINE);
         serverPassField = new PasswordEditField(
                 resources.getString(LogicMailResource.CONFIG_ACCOUNT_PASSWORD) + ' ',
                 accountConfig.getServerPass(),
                 256, TextField.NO_NEWLINE);
         identityField = new ObjectChoiceField(
                 resources.getString(LogicMailResource.CONFIG_ACCOUNT_IDENTITY) + ' ',
                 identityConfigs, 0);
         outgoingServerField = new ObjectChoiceField(
                 resources.getString(LogicMailResource.CONFIG_ACCOUNT_OUTGOING_SERVER) + ' ',
                 outgoingConfigs, 0);
 
         manager.add(incomingServerLabelField);
         manager.add(serverNameField);
         manager.add(serverSecurityField);
         manager.add(serverPortField);
         manager.add(serverUserField);
         manager.add(serverPassField);
         manager.add(new SeparatorField());
         manager.add(identityField);
         manager.add(outgoingServerField);
         manager.add(new LabelField());
         
         return manager;
     }
 
     /**
      * Initializes the UI fields for the folder settings page.
      */
     private Manager initFieldsFolder() {
         Manager manager = new BorderedFieldManager(BorderedFieldManager.BOTTOM_BORDER_NORMAL | Field.USE_ALL_HEIGHT);
         selectedSentFolder = accountConfig.getSentMailbox();
         selectedDraftFolder = accountConfig.getDraftMailbox();
 
         sentFolderChoiceLabel = new LabelField(resources.getString(LogicMailResource.CONFIG_ACCOUNT_SENT_MESSAGE_FOLDER) + ' ');
         sentFolderChoiceButtonLabel = new LabelField(createSelectedMailboxString(selectedSentFolder), Field.FOCUSABLE | Field.HIGHLIGHT_FOCUS | Field.FIELD_RIGHT | LabelField.ELLIPSIS);
         draftFolderChoiceLabel = new LabelField(resources.getString(LogicMailResource.CONFIG_ACCOUNT_DRAFT_MESSAGE_FOLDER) + ' ');
         draftFolderChoiceButtonLabel = new LabelField(createSelectedMailboxString(selectedDraftFolder), Field.FOCUSABLE | Field.HIGHLIGHT_FOCUS | Field.FIELD_RIGHT | LabelField.ELLIPSIS);
 
         manager.add(sentFolderChoiceLabel);
         manager.add(sentFolderChoiceButtonLabel);
         manager.add(draftFolderChoiceLabel);
         manager.add(draftFolderChoiceButtonLabel);
         manager.add(new LabelField());
         
         return manager;
     }
 
     /**
      * Initializes the UI fields for the advanced settings page.
      */
     private Manager initFieldsAdvanced() {
         Manager manager = new BorderedFieldManager(BorderedFieldManager.BOTTOM_BORDER_NORMAL | Field.USE_ALL_HEIGHT);
         
         initialFolderMessagesChoiceField = new NumericChoiceField(
                 resources.getString(LogicMailResource.CONFIG_ACCOUNT_MESSAGES_TO_LOAD),
                 5, 50, 5);
         initialFolderMessagesChoiceField.setSelectedValue(accountConfig.getInitialFolderMessages());
         initialFolderMessagesChoiceField.setChangeListener(fieldChangeListener);
         
         folderMessageIncrementChoiceField = new NumericChoiceField(
                 resources.getString(LogicMailResource.CONFIG_ACCOUNT_LOAD_INCREMENT),
                 1, 20, 1);
         folderMessageIncrementChoiceField.setSelectedValue(accountConfig.getFolderMessageIncrement());
         
         maximumFolderMessagesChoiceField = new NumericChoiceField(
                 resources.getString(LogicMailResource.CONFIG_ACCOUNT_MESSAGES_TO_RETAIN),
                 20, 500, 20);
         maximumFolderMessagesChoiceField.setSelectedValue(accountConfig.getMaximumFolderMessages());
         maximumFolderMessagesChoiceField.setChangeListener(fieldChangeListener);
         
         manager.add(initialFolderMessagesChoiceField);
         manager.add(folderMessageIncrementChoiceField);
         manager.add(maximumFolderMessagesChoiceField);
         manager.add(new SeparatorField());
         
         String[] transportChoices = {
                 resources.getString(LogicMailResource.CONFIG_ACCOUNT_TRANSPORT_USE_GLOBAL),
                 resources.getString(LogicMailResource.CONFIG_GLOBAL_TRANSPORT_AUTO),
                 resources.getString(LogicMailResource.CONFIG_GLOBAL_TRANSPORT_DIRECT_TCP),
                 resources.getString(LogicMailResource.CONFIG_GLOBAL_TRANSPORT_MDS),
                 resources.getString(LogicMailResource.CONFIG_GLOBAL_TRANSPORT_WAP2),
                 resources.getString(LogicMailResource.CONFIG_GLOBAL_TRANSPORT_WIFI_ONLY)
         };
         networkTransportChoiceField = new ObjectChoiceField(
                 resources.getString(LogicMailResource.CONFIG_GLOBAL_NETWORK_TRANSPORT),
                 transportChoices,
                 getTransportChoice(accountConfig.getTransportType()));
         networkTransportChoiceField.setChangeListener(fieldChangeListener);
         
         enableWiFiCheckboxField = new CheckboxField(
                 resources.getString(LogicMailResource.CONFIG_GLOBAL_ENABLE_WIFI),
                 accountConfig.getEnableWiFi());
         if(networkTransportChoiceField.getSelectedIndex() == 0) {
             enableWiFiCheckboxField.setChecked(false);
             enableWiFiCheckboxField.setEditable(false);
         }
         
         manager.add(networkTransportChoiceField);
         manager.add(enableWiFiCheckboxField);
         manager.add(new SeparatorField());
         
         if(accountConfig instanceof ImapConfig) {
             ImapConfig imapConfig = (ImapConfig)accountConfig;
 
             imapFolderPrefixField = new BasicEditField(
                     resources.getString(LogicMailResource.CONFIG_ACCOUNT_IMAP_FOLDER_PREFIX) + ' ',
                     imapConfig.getFolderPrefix());
 
             imapMaxMessageSizeEditField = new BasicEditField(
                     resources.getString(LogicMailResource.CONFIG_ACCOUNT_IMAP_DOWNLOAD_LIMIT) + ' ',
                     Integer.toString(imapConfig.getMaxMessageSize() / 1024));
             imapMaxMessageSizeEditField.setFilter(TextFilter.get(TextFilter.NUMERIC));
 
             imapMaxFolderDepthEditField = new BasicEditField(
                     resources.getString(LogicMailResource.CONFIG_ACCOUNT_IMAP_FOLDER_LIMIT) + ' ',
                     Integer.toString(imapConfig.getMaxFolderDepth()));
             imapMaxFolderDepthEditField.setFilter(TextFilter.get(TextFilter.NUMERIC));
 
             imapShowOnlySubscribedField = new CheckboxField(
                     resources.getString(LogicMailResource.CONFIG_ACCOUNT_IMAP_ONLY_SUBSCRIBED_FOLDERS),
                     imapConfig.getOnlySubscribedFolders());
 
             manager.add(imapFolderPrefixField);
             manager.add(imapMaxMessageSizeEditField);
             manager.add(imapMaxFolderDepthEditField);
             manager.add(imapShowOnlySubscribedField);
         }
         else if(accountConfig instanceof PopConfig) {
             PopConfig popConfig = (PopConfig)accountConfig;
 
             popMaxLinesEditField = new BasicEditField(
                     resources.getString(LogicMailResource.CONFIG_ACCOUNT_POP_DOWNLOAD_LIMIT) + ' ',
                     Integer.toString(popConfig.getMaxMessageLines()));
             popMaxLinesEditField.setFilter(TextFilter.get(TextFilter.NUMERIC));
 
             manager.add(popMaxLinesEditField);
         }
         manager.add(new LabelField());
         
         return manager;
     }
 
     public void AcctCfgScreen_fieldChanged(Field field, int context) {
         if(field == serverSecurityField) {
             if(accountConfig instanceof PopConfig) {
                 if(serverSecurityField.getSelectedIndex() == ConnectionConfig.SECURITY_SSL) {
                     serverPortField.setText("995");
                 }
                 else {
                     serverPortField.setText("110");
                 }
             }
             else if(accountConfig instanceof ImapConfig) {
                 if(serverSecurityField.getSelectedIndex() == ConnectionConfig.SECURITY_SSL) {
                     serverPortField.setText("993");
                 }
                 else {
                     serverPortField.setText("143");
                 }
             }
         }
         else if(field == pageField) {
             final int index = pageField.getSelectedIndex();
             UiApplication.getUiApplication().invokeLater(new Runnable() {
                 public void run() {
                     if(contentFieldManager != pageFieldManagers[index]) {
                         replace(contentFieldManager, pageFieldManagers[index]);
                         contentFieldManager = pageFieldManagers[index];
                     }
                 }
             });
         }
         else if(field == networkTransportChoiceField) {
             if(networkTransportChoiceField.getSelectedIndex() == 0) {
                 enableWiFiCheckboxField.setChecked(false);
                 enableWiFiCheckboxField.setEditable(false);
             }
             else if(networkTransportChoiceField.getSelectedIndex() == 5) {
                 enableWiFiCheckboxField.setChecked(true);
                 enableWiFiCheckboxField.setEditable(false);
             }
             else {
                 enableWiFiCheckboxField.setChecked(true);
                 enableWiFiCheckboxField.setEditable(true);
             }
         }
         else if(field == initialFolderMessagesChoiceField) {
             int value = initialFolderMessagesChoiceField.getSelectedValue();
             if(maximumFolderMessagesChoiceField.getSelectedValue() < value) {
                 maximumFolderMessagesChoiceField.setSelectedValue(value);
                 
                 // If the closest value was lower than the changed-field value,
                 // pick the next item in the field to make sure we end up with
                 // a larger value selected.
                 if(maximumFolderMessagesChoiceField.getSelectedValue() < value) {
                     maximumFolderMessagesChoiceField.setSelectedIndex(
                             maximumFolderMessagesChoiceField.getSelectedIndex() + 1);
                 }
             }
         }
         else if(field == maximumFolderMessagesChoiceField) {
             int value = maximumFolderMessagesChoiceField.getSelectedValue();
             if(initialFolderMessagesChoiceField.getSelectedValue() > value) {
                 initialFolderMessagesChoiceField.setSelectedValue(value);
             }
         }
     }
 
     /* (non-Javadoc)
      * @see net.rim.device.api.ui.Screen#trackwheelUnclick(int, int)
      */
     protected boolean trackwheelUnclick(int status, int time) {
        if(getFieldWithFocus() == contentFieldManager && contentFieldManager == pageFieldManagers[PAGE_FOLDER]) {
            if(contentFieldManager.getFieldWithFocus() == sentFolderChoiceButtonLabel) {
                 showFolderSelection(sentFolderChoiceButtonLabel);
                 return true;
             }
            else if(contentFieldManager.getFieldWithFocus() == draftFolderChoiceButtonLabel) {
                 showFolderSelection(draftFolderChoiceButtonLabel);
                 return true;
             }
             else {
                 return super.trackwheelUnclick(status, time);
             }
         }
         else {
             return super.trackwheelUnclick(status, time);
         }
     }
 
     private void showFolderSelection(LabelField choiceButtonLabel) {
         String titleText;
         if(choiceButtonLabel == sentFolderChoiceButtonLabel) {
             titleText = resources.getString(LogicMailResource.CONFIG_ACCOUNT_SENT_MESSAGE_FOLDER); 
         }
         else if(choiceButtonLabel == draftFolderChoiceButtonLabel) {
             titleText = resources.getString(LogicMailResource.CONFIG_ACCOUNT_DRAFT_MESSAGE_FOLDER);
         }
         else {
             return;
         }
 
         // Build an array containing the current account node, if it already exists,
         // and any local account nodes.
         MailRootNode mailRootNode = MailManager.getInstance().getMailRootNode();
         Vector accountNodeVector = new Vector(2);
         accountNodeVector.addElement(mailRootNode.getLocalAccount());
         
         AccountNode currentAccountNode = mailRootNode.findAccountForConfig(accountConfig);
         if(currentAccountNode != null) {
             accountNodeVector.addElement(currentAccountNode);
         }
         
         AccountNode[] accountNodes = new AccountNode[accountNodeVector.size()];
         accountNodeVector.copyInto(accountNodes);
 
         MailboxSelectionDialog dialog = new MailboxSelectionDialog(titleText, accountNodes);
         if(choiceButtonLabel == sentFolderChoiceButtonLabel) {
             dialog.setSelectedMailboxNode(selectedSentFolder);
         }
         else if(choiceButtonLabel == draftFolderChoiceButtonLabel) {
             dialog.setSelectedMailboxNode(selectedDraftFolder);
         }
         dialog.doModal();
 
         MailboxNode selectedNode = dialog.getSelectedMailboxNode();
         if(selectedNode != null) {
             choiceButtonLabel.setText(createSelectedMailboxString(selectedNode));
             if(choiceButtonLabel == sentFolderChoiceButtonLabel) {
                 if(selectedSentFolder != selectedNode) {
                     selectedSentFolder = selectedNode;
                     this.setDirty(true);
                 }
             }
             else if(choiceButtonLabel == draftFolderChoiceButtonLabel) {
                 if(selectedDraftFolder != selectedNode) {
                     selectedDraftFolder = selectedNode;
                     this.setDirty(true);
                 }
             }
         }
     }
 
     private String createSelectedMailboxString(MailboxNode mailboxNode) {
         StringBuffer buf = new StringBuffer();
         if(mailboxNode != null) {
             buf.append(mailboxNode.getParentAccount().toString());
             buf.append(": ");
             buf.append(mailboxNode.toString());
         }
         else {
             buf.append('<' + resources.getString(LogicMailResource.CONFIG_ACCOUNT_NONE) + '>');
         }
         return buf.toString();
     }
 
     private static int getTransportChoice(int transportSetting) {
         int result;
         switch(transportSetting) {
         case ConnectionConfig.TRANSPORT_GLOBAL:
             result = 0;
             break;
         case ConnectionConfig.TRANSPORT_AUTO:
             result = 1;
             break;
         case ConnectionConfig.TRANSPORT_DIRECT_TCP:
             result = 2;
             break;
         case ConnectionConfig.TRANSPORT_MDS:
             result = 3;
             break;
         case ConnectionConfig.TRANSPORT_WAP2:
             result = 4;
             break;
         case ConnectionConfig.TRANSPORT_WIFI_ONLY:
             result = 5;
             break;
         default:
             result = 0;
             break;
         }
         return result;
     }
     
     private static int getTransportSetting(int transportChoice) {
         int result;
         switch(transportChoice) {
         case 0:
             result = ConnectionConfig.TRANSPORT_GLOBAL;
             break;
         case 1:
             result = ConnectionConfig.TRANSPORT_AUTO;
             break;
         case 2:
             result = ConnectionConfig.TRANSPORT_DIRECT_TCP;
             break;
         case 3:
             result = ConnectionConfig.TRANSPORT_MDS;
             break;
         case 4:
             result = ConnectionConfig.TRANSPORT_WAP2;
             break;
         case 5:
             result = ConnectionConfig.TRANSPORT_WIFI_ONLY;
             break;
         default:
             result = ConnectionConfig.TRANSPORT_GLOBAL;
             break;
         }
         return result;
     }
     
     /**
      * Determines if this screen is dirty.
      * Custom implementation provided to exempt the page selection field
      * from the check, but make sure all of the pages themselves are includes.
      * 
      * @see net.rim.device.api.ui.Screen#isDirty()
      */
     public boolean isDirty() {
         if(accountNameField.isDirty()) { return true; }
 
         for(int i=0; i<pageFieldManagers.length; i++) {
             if(pageFieldManagers[i].isDirty()) {
                 return true;
             }
         }
 
         return false;
     }
 
     /* (non-Javadoc)
      * @see net.rim.device.api.ui.container.MainScreen#onSavePrompt()
      */
     protected boolean onSavePrompt() {
         if(accountNameField.getText().length() > 0 &&
                 serverNameField.getText().length() > 0 &&
                 serverPortField.getText().length() > 0) {
             return super.onSavePrompt();
         }
         else {
             int result =
                 Dialog.ask(resources.getString(LogicMailResource.CONFIG_PROMPT_INCOMPLETE),
                         new String[] {
                     resources.getString(LogicMailResource.MENUITEM_DISCARD),
                     resources.getString(LogicMailResource.MENUITEM_CANCEL) }, 0);
             if(result == 0) {
                 return true;
             }
             else {
                 return false;
             }
         }
     }
 
     /* (non-Javadoc)
      * @see net.rim.device.api.ui.Screen#save()
      */
     public void save() {
         this.accountConfig.setAcctName(accountNameField.getText());
         this.accountConfig.setServerName(serverNameField.getText());
         this.accountConfig.setServerSecurity(serverSecurityField.getSelectedIndex());
         this.accountConfig.setServerPort(Integer.parseInt(serverPortField.getText()));
         this.accountConfig.setServerUser(serverUserField.getText());
         this.accountConfig.setServerPass(serverPassField.getText());
 
         IdentityConfig selectedIdentityConfig = (IdentityConfig)identityField.getChoice(identityField.getSelectedIndex());
         if(createDefaultIdentity) {
             String userName = serverUserField.getText();
             if(userName.length() == 0) {
                 userName = "user";
             }
             selectedIdentityConfig.setEmailAddress(userName + '@' + serverNameField.getText());
             MailSettings.getInstance().addIdentityConfig(selectedIdentityConfig);
             createDefaultIdentity = false;
         }
         this.accountConfig.setIdentityConfig(selectedIdentityConfig);
 
         OutgoingConfig selectedOutgoingConfig = (OutgoingConfig)outgoingServerField.getChoice(outgoingServerField.getSelectedIndex());
         if(selectedOutgoingConfig.getUniqueId() == -1) {
             this.accountConfig.setOutgoingConfig(null);
         }
         else {
             this.accountConfig.setOutgoingConfig(selectedOutgoingConfig);
         }
 
         this.accountConfig.setSentMailbox(selectedSentFolder);
         this.accountConfig.setDraftMailbox(selectedDraftFolder);
 
         this.accountConfig.setInitialFolderMessages(initialFolderMessagesChoiceField.getSelectedValue());
         this.accountConfig.setFolderMessageIncrement(folderMessageIncrementChoiceField.getSelectedValue());
         this.accountConfig.setMaximumFolderMessages(maximumFolderMessagesChoiceField.getSelectedValue());
         
         this.accountConfig.setTransportType(getTransportSetting(networkTransportChoiceField.getSelectedIndex()));
         this.accountConfig.setEnableWiFi(enableWiFiCheckboxField.getChecked());
         
         if(accountConfig instanceof ImapConfig) {
             ImapConfig imapConfig = (ImapConfig)accountConfig;
 
             imapConfig.setFolderPrefix(imapFolderPrefixField.getText().trim());
 
             try {
                 imapConfig.setMaxMessageSize(Integer.parseInt(
                         imapMaxMessageSizeEditField.getText()) * 1024);
             } catch (Exception e) { }
 
             try {
                 imapConfig.setMaxFolderDepth(Integer.parseInt(
                         imapMaxFolderDepthEditField.getText()));
             } catch (Exception e) { }
 
             imapConfig.setOnlySubscribedFolders(imapShowOnlySubscribedField.getChecked());
         }
         else if(accountConfig instanceof PopConfig) {
             PopConfig popConfig = (PopConfig)accountConfig;
 
             try {
                 popConfig.setMaxMessageLines(Integer.parseInt(popMaxLinesEditField.getText()));
             } catch (Exception e) { }
         }
 
         accountSaved = true;
     }
 
     public boolean acctSaved() {
         return accountSaved;
     }
 }
