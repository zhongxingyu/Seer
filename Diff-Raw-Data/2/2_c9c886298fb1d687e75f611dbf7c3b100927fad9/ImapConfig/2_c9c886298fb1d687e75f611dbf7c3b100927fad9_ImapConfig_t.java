 /*-
  * Copyright (c) 2007, Derek Konigsberg
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
 
 import java.io.DataInput;
 import java.util.Vector;
 
 import net.rim.device.api.util.Arrays;
 
 import org.logicprobe.LogicMail.model.AccountNode;
 import org.logicprobe.LogicMail.model.MailManager;
 import org.logicprobe.LogicMail.model.MailRootNode;
 import org.logicprobe.LogicMail.model.MailboxNode;
 import org.logicprobe.LogicMail.util.SerializableHashtable;
 
 /**
  * Configuration object to store settings for
  * IMAP mail accounts.
  */
 public class ImapConfig extends AccountConfig {
     private MailboxNode[] refreshMailboxes;
     private long[] refreshMailboxIds;
     private String folderPrefix;
     private int maxMessageSize;
     private int maxFolderDepth;
     private boolean onlySubscribedFolders;
     private boolean enableCompression;
 
     /**
      * Instantiates a new connection configuration with defaults.
      */
     public ImapConfig() {
         super();
     }
 
     /**
      * Instantiates a new connection configuration from serialized data.
      * 
      * @param input The input stream to deserialize from
      */
     public ImapConfig(DataInput input) {
         super(input);
     }
 
     /* (non-Javadoc)
      * @see org.logicprobe.LogicMail.conf.AccountConfig#setDefaults()
      */
     protected void setDefaults() {
         super.setDefaults();
         setServerPort(143);
         this.refreshMailboxes = null;
         this.refreshMailboxIds = null;
         this.maxMessageSize = 32768;
         this.maxFolderDepth = 4;
         this.folderPrefix = "";
         this.onlySubscribedFolders = true;
        this.enableCompression = false;
     }
 
     /* (non-Javadoc)
      * @see org.logicprobe.LogicMail.conf.AccountConfig#toString()
      */
     public String toString() {
         String text = getAcctName().concat(" (IMAP)");
         return text;
     }
 
     /**
      * Gets the mailboxes to be automatically refreshed.
      */
     public MailboxNode[] getRefreshMailboxes() {
         if(refreshMailboxIds == null || refreshMailboxIds.length == 0) {
             return new MailboxNode[0];
         }
         else if(refreshMailboxes == null || refreshMailboxes.length == 0) {
             MailRootNode rootNode = MailManager.getInstance().getMailRootNode();
             AccountNode accountNode = rootNode.findAccountForConfig(this);
             if(accountNode == null) {
                 return new MailboxNode[0];
             }
             
             MailboxNode rootMailbox = accountNode.getRootMailbox();
             Vector foundMailboxes = new Vector(refreshMailboxIds.length);
             for(int i=0; i<refreshMailboxIds.length; i++) {
                 MailboxNode mailbox = findMailboxNode(rootMailbox, refreshMailboxIds[i]);
                 if(mailbox != null) { foundMailboxes.addElement(mailbox); }
             }
             
             refreshMailboxes = new MailboxNode[foundMailboxes.size()];
             foundMailboxes.copyInto(refreshMailboxes);
         }
 
         return refreshMailboxes;
     }
     
     /**
      * Sets the mailboxes to be automatically refreshed.
      */
     public void setRefreshMailboxes(MailboxNode[] refreshMailboxes) {
         if(this.refreshMailboxes != null
                 && Arrays.equals(this.refreshMailboxes, refreshMailboxes)) {
             return;
         }
         else if(refreshMailboxes == null || refreshMailboxes.length == 0) {
             this.refreshMailboxes = null;
             this.refreshMailboxIds = null;
             changeType |= CHANGE_TYPE_REFRESH;
         }
         else {
             int size = refreshMailboxes.length;
             this.refreshMailboxes = new MailboxNode[size];
             this.refreshMailboxIds = new long[size];
             for(int i=0; i<size; i++) {
                 this.refreshMailboxes[i] = refreshMailboxes[i];
                 this.refreshMailboxIds[i] = refreshMailboxes[i].getUniqueId();
             }
             changeType |= CHANGE_TYPE_REFRESH;
         }
     }
 
     /**
      * Gets the folder prefix.
      * 
      * @return The folder prefix
      */
     public String getFolderPrefix() {
         return folderPrefix;
     }
 
     /**
      * Sets the folder prefix.
      * 
      * @param folderPrefix The new folder prefix
      */
     public void setFolderPrefix(String folderPrefix) {
         if(!this.folderPrefix.equals(folderPrefix)) {
             this.folderPrefix = folderPrefix;
             changeType |= CHANGE_TYPE_ADVANCED;
         }
     }
 
     /**
      * Gets the maximum message size.
      * 
      * @return The maximum message size
      */
     public int getMaxMessageSize() {
         return this.maxMessageSize;
     }
 
     /**
      * Sets the maximum message size.
      * 
      * @param maxMessageSize The new maximum message size
      */
     public void setMaxMessageSize(int maxMessageSize) {
         if(this.maxMessageSize != maxMessageSize) {
             this.maxMessageSize = maxMessageSize;
             changeType |= CHANGE_TYPE_LIMITS;
         }
     }
 
     /**
      * Gets the maximum folder depth.
      * 
      * @return The maximum folder depth
      */
     public int getMaxFolderDepth() {
         return maxFolderDepth;
     }
 
     /**
      * Sets the maximum folder depth.
      * 
      * @param maxFolderDepth The new maximum folder depth
      */
     public void setMaxFolderDepth(int maxFolderDepth) {
         if(this.maxFolderDepth != maxFolderDepth) {
             this.maxFolderDepth = maxFolderDepth;
             changeType |= CHANGE_TYPE_LIMITS;
         }
     }
 
     /**
      * Gets whether to only load subscribed folders.
      * 
      * @return whether to load only subscribed folders
      */
     public boolean getOnlySubscribedFolders() {
         return onlySubscribedFolders;
     }
 
     /**
      * Sets whether to only load subscribed folders.
      * 
      * @param onlySubscribedFolders true, if only subscribed folders should be loaded
      */
     public void setOnlySubscribedFolders(boolean onlySubscribedFolders) {
         if(this.onlySubscribedFolders != onlySubscribedFolders) {
             this.onlySubscribedFolders = onlySubscribedFolders;
             changeType |= CHANGE_TYPE_ADVANCED;
         }
     }
 
     /**
      * Gets whether to enable compression.
      *
      * @return whether to enable compression
      */
     public boolean getEnableCompression() {
         return enableCompression;
     }
     
     /**
      * Sets whether to enable compression.
      *
      * @param enableCompression true, if compression is enabled
      */
     public void setEnableCompression(boolean enableCompression) {
         if(this.enableCompression != enableCompression) {
             this.enableCompression = enableCompression;
             changeType |= CHANGE_TYPE_ADVANCED;
         }
     }
     
     /* (non-Javadoc)
      * @see org.logicprobe.LogicMail.conf.AccountConfig#writeConfigItems(org.logicprobe.LogicMail.util.SerializableHashtable)
      */
     public void writeConfigItems(SerializableHashtable table) {
         super.writeConfigItems(table);
         
         if(refreshMailboxIds != null) {
             table.put("account_imap_refreshMailboxIds", refreshMailboxIds);
         }
         else {
             table.put("account_imap_refreshMailboxIds", new long[0]);
         }
         
         if(folderPrefix != null) {
             table.put("account_imap_folderPrefix", folderPrefix);
         }
         else {
             table.put("account_imap_folderPrefix", "");
         }
         table.put("account_imap_maxMessageSize", new Integer(maxMessageSize));
         table.put("account_imap_maxFolderDepth", new Integer(maxFolderDepth));
         table.put("account_imap_onlySubscribedFolders", new Boolean(onlySubscribedFolders));
         table.put("account_imap_enableCompression", new Boolean(enableCompression));
     }
 
     /* (non-Javadoc)
      * @see org.logicprobe.LogicMail.conf.AccountConfig#readConfigItems(org.logicprobe.LogicMail.util.SerializableHashtable)
      */
     public void readConfigItems(SerializableHashtable table) {
         super.readConfigItems(table);
         Object value;
 
         value = table.get("account_imap_refreshMailboxIds");
         if(value instanceof long[]) {
             refreshMailboxIds = (long[])value;
         }
         
         value = table.get("account_imap_folderPrefix");
         if(value instanceof String) {
             folderPrefix = (String)value;
         }
         value = table.get("account_imap_maxMessageSize");
         if (value instanceof Integer) {
             maxMessageSize = ((Integer) value).intValue();
         }
 
         value = table.get("account_imap_maxFolderDepth");
         if (value instanceof Integer) {
             maxFolderDepth = ((Integer) value).intValue();
         }
         value = table.get("account_imap_onlySubscribedFolders");
         if(value instanceof Boolean) {
             onlySubscribedFolders = ((Boolean)value).booleanValue();
         }
         value = table.get("account_imap_enableCompression");
         if(value instanceof Boolean) {
             enableCompression = ((Boolean)value).booleanValue();
         }
     }
 }
