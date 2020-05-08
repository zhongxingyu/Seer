 /*-
  * Copyright (c) 2009, Derek Konigsberg
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
 
 import org.logicprobe.LogicMail.PlatformInfo;
 import org.logicprobe.LogicMail.model.AccountNode;
 import org.logicprobe.LogicMail.model.MailRootNode;
 import org.logicprobe.LogicMail.model.MailboxNode;
 import org.logicprobe.LogicMail.model.MessageNode;
 import org.logicprobe.LogicMail.model.Node;
 import org.logicprobe.LogicMail.model.NodeVisitor;
 import org.logicprobe.LogicMail.model.OutgoingMessageNode;
 
 import net.rim.device.api.system.Bitmap;
 
 /**
  * Provides icons for the mail object model nodes.
  */
 public class NodeIcons {
     protected static NodeIcons instance = createInstance();
     private NodeIconVisitor visitor = new NodeIconVisitor();
 
     private final Bitmap localAccountIcon;
     private final Bitmap networkAccountIcon0;
     private final Bitmap networkAccountIcon1;
     private final Bitmap folderIcon;
     private final Bitmap folderNewIcon;
     private final Bitmap inboxFolderIcon;
     private final Bitmap inboxNewFolderIcon;
     private final Bitmap outboxFolderIcon;
     private final Bitmap draftsFolderIcon;
     private final Bitmap sentFolderIcon;
     private final Bitmap trashFolderIcon;
     private final Bitmap trashFullFolderIcon;
     private final Bitmap openedMessageIcon;
     private final Bitmap unopenedMessageIcon;
     private final Bitmap repliedMessageIcon;
     private final Bitmap forwardedMessageIcon;
     private final Bitmap repliedForwardedMessageIcon;
     private final Bitmap flaggedMessageIcon;
     private final Bitmap draftMessageIcon;
     private final Bitmap deletedMessageIcon;
     private final Bitmap unknownMessageIcon;
     private final Bitmap junkMessageIcon;
     private final Bitmap openedUnloadedMessageIcon;
     private final Bitmap unopenedUnloadedMessageIcon;
     private final Bitmap repliedUnloadedMessageIcon;
     private final Bitmap forwardedUnloadedMessageIcon;
     private final Bitmap repliedForwardedUnloadedMessageIcon;
     private final Bitmap flaggedUnloadedMessageIcon;
     private final Bitmap draftUnloadedMessageIcon;
     private final Bitmap deletedUnloadedMessageIcon;
     private final Bitmap unknownUnloadedMessageIcon;
     private final Bitmap junkUnloadedMessageIcon;
     private final Bitmap sendReceiveMessageIcon;
     private final Bitmap sendErrorMessageIcon;
     
     public final static int ICON_FOLDER = 0;
 
     protected NodeIcons() {
         // Load account icons
         localAccountIcon    = Bitmap.getBitmapResource("account_local.png");
         networkAccountIcon0 = Bitmap.getBitmapResource("account_network_offline.png");
         networkAccountIcon1 = Bitmap.getBitmapResource("account_network_online.png");
 
         // Load folder icons
         folderIcon          = Bitmap.getBitmapResource("folder.png");
         folderNewIcon       = Bitmap.getBitmapResource("folder_new.png");
         inboxFolderIcon     = Bitmap.getBitmapResource("folder_inbox.png");
         inboxNewFolderIcon  = Bitmap.getBitmapResource("folder_inbox_new.png");
         outboxFolderIcon    = Bitmap.getBitmapResource("folder_outbox.png");
         draftsFolderIcon    = Bitmap.getBitmapResource("folder_drafts.png");
         sentFolderIcon      = Bitmap.getBitmapResource("folder_sent.png");
         trashFolderIcon     = Bitmap.getBitmapResource("folder_trash.png");
         trashFullFolderIcon = Bitmap.getBitmapResource("folder_trash_full.png");
 
         // Load message icons
         openedMessageIcon    = Bitmap.getBitmapResource("mail_opened.png");
         unopenedMessageIcon  = Bitmap.getBitmapResource("mail_unopened.png");
         repliedMessageIcon   = Bitmap.getBitmapResource("mail_replied.png");
         forwardedMessageIcon = Bitmap.getBitmapResource("mail_forwarded.png");
         repliedForwardedMessageIcon = Bitmap.getBitmapResource("mail_replied_forwarded.png");
         flaggedMessageIcon   = Bitmap.getBitmapResource("mail_flagged.png");
         draftMessageIcon     = Bitmap.getBitmapResource("mail_draft.png");
         deletedMessageIcon   = Bitmap.getBitmapResource("mail_deleted.png");
         unknownMessageIcon   = Bitmap.getBitmapResource("mail_unknown.png");
         junkMessageIcon      = Bitmap.getBitmapResource("mail_junk.png");
         openedUnloadedMessageIcon    = Bitmap.getBitmapResource("mail_opened_unloaded.png");
         unopenedUnloadedMessageIcon  = Bitmap.getBitmapResource("mail_unopened_unloaded.png");
         repliedUnloadedMessageIcon   = Bitmap.getBitmapResource("mail_replied_unloaded.png");
         forwardedUnloadedMessageIcon = Bitmap.getBitmapResource("mail_forwarded_unloaded.png");
         repliedForwardedUnloadedMessageIcon = Bitmap.getBitmapResource("mail_replied_forwarded_unloaded.png");
         flaggedUnloadedMessageIcon   = Bitmap.getBitmapResource("mail_flagged_unloaded.png");
         draftUnloadedMessageIcon     = Bitmap.getBitmapResource("mail_draft_unloaded.png");
         deletedUnloadedMessageIcon   = Bitmap.getBitmapResource("mail_deleted_unloaded.png");
         unknownUnloadedMessageIcon   = Bitmap.getBitmapResource("mail_unknown_unloaded.png");
         junkUnloadedMessageIcon      = Bitmap.getBitmapResource("mail_junk_unloaded.png");
         sendReceiveMessageIcon = Bitmap.getBitmapResource("mail_send_receive.png");
         sendErrorMessageIcon   = Bitmap.getBitmapResource("mail_send_error.png");
     }
 
     private static NodeIcons createInstance() {
         NodeIcons result;
         if(PlatformInfo.getInstance().hasTouchscreen()) {
             try {
                 Class touchNodeIconsClass =
                     Class.forName("org.logicprobe.LogicMail.ui.TouchNodeIcons");
                 result = (NodeIcons)touchNodeIconsClass.newInstance();
             } catch (ClassNotFoundException e) {
                 result = new NodeIcons();
             } catch (InstantiationException e) {
                 result = new NodeIcons();
             } catch (IllegalAccessException e) {
                 result = new NodeIcons();
             }
         }
         else {
             result = new NodeIcons();
         }
         return result;
     }
 
     public static Bitmap getIcon(Node node) {
         return instance.getIconImpl(node);
     }
 
     public static Bitmap getIcon(int type) {
         return instance.getIconImpl(type);
     }
 
     private Bitmap getIconImpl(Node node) {
         visitor.clearIcon();
         node.accept(visitor);
         return visitor.getIcon();
     }
 
     private Bitmap getIconImpl(int type) {
         Bitmap icon;
         switch(type) {
         case ICON_FOLDER:
             icon = folderIcon;
             break;
         default:
             icon = null;
         }
         return icon;
     }
 
     protected class NodeIconVisitor implements NodeVisitor {
         protected Bitmap icon;
 
         public void visit(MailRootNode node) { }
 
         public void visit(AccountNode node) {
             switch(node.getStatus()) {
             case AccountNode.STATUS_LOCAL:
                 this.icon = localAccountIcon;
                 break;
             case AccountNode.STATUS_OFFLINE:
                 this.icon = networkAccountIcon0;
                 break;
             case AccountNode.STATUS_ONLINE:
                 this.icon = networkAccountIcon1;
                 break;
             }
         }
 
         public void visit(MailboxNode node) {
             switch(node.getType()) {
             case MailboxNode.TYPE_INBOX:
                 if(node.getUnseenMessageCount() > 0) {
                     this.icon = inboxNewFolderIcon;
                 }
                 else {
                     this.icon = inboxFolderIcon;
                 }
                 break;
             case MailboxNode.TYPE_DRAFTS:
                 this.icon = draftsFolderIcon;
                 break;
             case MailboxNode.TYPE_SENT:
                 this.icon = sentFolderIcon;
                 break;
             case MailboxNode.TYPE_TRASH:
                 if(node.getMessageCount() > 0) {
                     this.icon = trashFullFolderIcon;
                 }
                 else {
                     this.icon = trashFolderIcon;
                 }
                 break;
             case MailboxNode.TYPE_OUTBOX:
                 this.icon = outboxFolderIcon;
                 break;
             case MailboxNode.TYPE_NORMAL:
             default:
                 if(node.getUnseenMessageCount() > 0) {
                     this.icon = folderNewIcon;
                 }
                 else {
                     this.icon = folderIcon;
                 }
                 break;
             }
         }
 
         public void visit(MessageNode node) {
             if(node instanceof OutgoingMessageNode) {
                 OutgoingMessageNode outgoingNode = (OutgoingMessageNode)node;
                 if(outgoingNode.isSending()) {
                     this.icon = sendReceiveMessageIcon;
                 }
                 else if(outgoingNode.hasRecipientError()) {
                     this.icon = sendErrorMessageIcon;
                 }
                 else {
                     this.icon = unopenedMessageIcon;
                 }
             }
             else {
                 boolean unloaded = !node.hasCachedContent() && !node.hasMessageContent() && node.isCachable();
                 int flags = node.getFlags();
                 if((flags & MessageNode.Flag.DELETED) != 0) {
                     this.icon = unloaded ? deletedUnloadedMessageIcon : deletedMessageIcon;
                 }
                 else if((flags & MessageNode.Flag.JUNK) != 0) {
                     this.icon = unloaded ? junkUnloadedMessageIcon : junkMessageIcon;
                 }
                 else if((flags & MessageNode.Flag.FLAGGED) != 0) {
                     this.icon = unloaded ? flaggedUnloadedMessageIcon : flaggedMessageIcon;
                 }
                 else if((flags & MessageNode.Flag.DRAFT) != 0) {
                     this.icon = unloaded ? draftUnloadedMessageIcon: draftMessageIcon;
                 }
                 else if((flags & MessageNode.Flag.ANSWERED) != 0
                         && (flags & MessageNode.Flag.FORWARDED) != 0) {
                     this.icon = unloaded ? repliedForwardedUnloadedMessageIcon: repliedForwardedMessageIcon;
                 }
                 else if((flags & MessageNode.Flag.ANSWERED) != 0) {
                     this.icon = unloaded ? repliedUnloadedMessageIcon: repliedMessageIcon;
                 }
                 else if((flags & MessageNode.Flag.FORWARDED) != 0) {
                     this.icon = unloaded ? forwardedUnloadedMessageIcon: forwardedMessageIcon;
                 }
                 else if((flags & MessageNode.Flag.SEEN) != 0) {
                     this.icon = unloaded ? openedUnloadedMessageIcon : openedMessageIcon;
                 }
                else if((flags & MessageNode.Flag.RECENT) != 0) {
                    this.icon = unloaded ? unopenedUnloadedMessageIcon: unopenedMessageIcon;
                }
                 else {
                     this.icon = unloaded ? unknownUnloadedMessageIcon: unknownMessageIcon;
                 }
             }
         }
 
         public void clearIcon() {
             this.icon = null;
         }
 
         public Bitmap getIcon() {
             return this.icon;
         }
     }
 }
