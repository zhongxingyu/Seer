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
 package org.logicprobe.LogicMail.mail.smtp;
 
 import org.logicprobe.LogicMail.conf.ConnectionConfig;
 import org.logicprobe.LogicMail.conf.GlobalConfig;
 import org.logicprobe.LogicMail.conf.MailSettings;
 import org.logicprobe.LogicMail.conf.MailSettingsEvent;
 import org.logicprobe.LogicMail.conf.MailSettingsListener;
 import org.logicprobe.LogicMail.conf.OutgoingConfig;
 import org.logicprobe.LogicMail.mail.MailException;
 import org.logicprobe.LogicMail.mail.OutgoingMailClient;
 import org.logicprobe.LogicMail.mail.RecipientException;
 import org.logicprobe.LogicMail.message.Message;
 import org.logicprobe.LogicMail.message.MessageEnvelope;
 import org.logicprobe.LogicMail.message.MessageMimeConverter;
 import org.logicprobe.LogicMail.util.Connection;
 import org.logicprobe.LogicMail.util.NetworkConnector;
 import org.logicprobe.LogicMail.util.MailMessageParser;
 
 import java.io.IOException;
 
 import java.util.Hashtable;
 
 /**
  * Implements an SMTP client
  */
 public class SmtpClient implements OutgoingMailClient {
     private final NetworkConnector networkConnector;
     private final MailSettings mailSettings;
     private final GlobalConfig globalConfig;
     private final OutgoingConfig outgoingConfig;
     private final SmtpProtocol smtpProtocol;
     private String hostname;
     private Connection connection;
     private boolean isFresh;
     private boolean openStarted;
     private String username;
     private String password;
     
     /**
      * Table of supported server capabilities
      */
     private Hashtable capabilities;
     
     private MailSettingsListener mailSettingsListener = new MailSettingsListener() {
             public void mailSettingsSaved(MailSettingsEvent e) {
                 mailSettings_MailSettingsSaved(e);
             }
         };
 
     /** Creates a new instance of SmtpClient */
     public SmtpClient(NetworkConnector networkConnector, GlobalConfig globalConfig, OutgoingConfig outgoingConfig) {
         this.networkConnector = networkConnector;
     	this.globalConfig = globalConfig;
         this.outgoingConfig = outgoingConfig;
         this.mailSettings = MailSettings.getInstance();
         smtpProtocol = new SmtpProtocol();
 
         if (outgoingConfig.getUseAuth() > 0) {
             username = outgoingConfig.getServerUser();
             password = outgoingConfig.getServerPass();
         } else {
             username = null;
             password = null;
         }
 
         openStarted = false;
         mailSettings.addMailSettingsListener(mailSettingsListener);
     }
 
     private void mailSettings_MailSettingsSaved(MailSettingsEvent e) {
         // Check for a list change, where we no longer exist afterwards
         if((e.getListChange() & MailSettingsEvent.LIST_CHANGED_OUTGOING) != 0
                 && !mailSettings.containsOutgoingConfig(outgoingConfig)) {
             // We have been deleted, so unregister to make sure we
             // no longer affect the system and can be garbage collected.
             mailSettings.removeMailSettingsListener(mailSettingsListener);
         }
 
         // Check for a change to the global or account network settings
         if((e.getGlobalChange() & GlobalConfig.CHANGE_TYPE_NETWORK) != 0
                 || (e.getConfigChange(outgoingConfig) & OutgoingConfig.CHANGE_TYPE_CONNECTION) != 0) {
             // Refresh authentication information from the configuration
             username = outgoingConfig.getServerUser();
             password = outgoingConfig.getServerPass();
         }
     }
 
     public boolean open() throws IOException, MailException {
         if (!openStarted) {
             connection = networkConnector.open(outgoingConfig);
             smtpProtocol.setConnection(connection);
 
             // Eat the initial server response
             connection.receive();
 
             hostname = globalConfig.getLocalHostname();
 
             if (hostname == null || hostname.length() == 0) {
                 hostname = connection.getLocalAddress();
                 
                 if(hostname == null || hostname.length() == 0) {
                     hostname = System.getProperty("microedition.hostname");
                 }
             }
 
             capabilities = smtpProtocol.executeExtendedHello(hostname);
             openStarted = true;
         }
         
         // TLS initialization
         int serverSecurity = outgoingConfig.getServerSecurity();
         if((serverSecurity == ConnectionConfig.SECURITY_TLS_IF_AVAILABLE && capabilities.containsKey("STARTTLS"))
         		|| (serverSecurity == ConnectionConfig.SECURITY_TLS)) {
         	smtpProtocol.executeStartTLS();
         	connection = networkConnector.getConnectionAsTLS(connection);
         	smtpProtocol.setConnection(connection);
         	
         	// Re-execute the EHLO command, since some servers require this
         	capabilities = smtpProtocol.executeExtendedHello(hostname);
         }
         
         if (outgoingConfig.getUseAuth() > 0) {
             boolean result = smtpProtocol.executeAuth(outgoingConfig.getUseAuth(),
                     username, password);
 
             if (!result) {
                 return false;
             }
         }
 
         isFresh = true;
         openStarted = false;
 
         return true;
     }
 
     public void close() throws IOException, MailException {
        openStarted = false;
        if(connection != null) {
            if(connection.isConnected()) {
                try {
                    smtpProtocol.executeQuit();
                } catch (Exception exp) { }
            }
            connection.close();
            connection = null;
         }
     }
 
     public ConnectionConfig getConnectionConfig() {
         return outgoingConfig;
     }
 
     public boolean isConnected() {
         return connection != null && connection.isConnected();
     }
 
     public String getUsername() {
         return username;
     }
 
     public void setUsername(String username) {
         this.username = username;
     }
 
     public String getPassword() {
         return password;
     }
 
     public void setPassword(String password) {
         this.password = password;
     }
 
     public String sendMessage(MessageEnvelope envelope, Message message)
         throws IOException, MailException {
         if (!isFresh) {
             smtpProtocol.executeReset();
         }
 
         isFresh = false;
 
         // serialize the message
         MessageMimeConverter messageMime = new MessageMimeConverter(message);
 
         String mimeStr = messageMime.toMimeString();
 
         StringBuffer buffer = new StringBuffer();
 
         // Generate the headers
         buffer.append(MailMessageParser.generateMessageHeaders(envelope, true));
         
         // Add the body
         buffer.append(mimeStr);
 
         // Send the message
         if (!smtpProtocol.executeMail(stripEmail(envelope.from[0]))) {
             throw new MailException("Error with sender");
         }
 
         for (int i = 0; i < envelope.to.length; i++) {
             String address = stripEmail(envelope.to[i]);
             if (!smtpProtocol.executeRecipient(address)) {
                 throw new RecipientException(RecipientException.RECIPIENT_TO, address);
             }
         }
 
         if (envelope.cc != null) {
             for (int i = 0; i < envelope.cc.length; i++) {
                 String address = stripEmail(envelope.cc[i]);
                 if (!smtpProtocol.executeRecipient(address)) {
                     throw new RecipientException(RecipientException.RECIPIENT_CC, address);
                 }
             }
         }
 
         if (envelope.bcc != null) {
             for (int i = 0; i < envelope.bcc.length; i++) {
                 String address = stripEmail(envelope.bcc[i]);
                 if (!smtpProtocol.executeRecipient(address)) {
                     throw new RecipientException(RecipientException.RECIPIENT_BCC, address);
                 }
             }
         }
 
         String rawMessage = buffer.toString();
 
         if (!smtpProtocol.executeData(rawMessage)) {
             throw new MailException("Error sending message");
         }
 
         return rawMessage;
     }
 
     private static String stripEmail(String input) {
         int p = input.indexOf('<');
         int q = input.indexOf('>');
 
         if ((p == -1) || (q == -1) || (q <= p)) {
             return input;
         } else {
             return input.substring(p + 1, q);
         }
     }
 }
