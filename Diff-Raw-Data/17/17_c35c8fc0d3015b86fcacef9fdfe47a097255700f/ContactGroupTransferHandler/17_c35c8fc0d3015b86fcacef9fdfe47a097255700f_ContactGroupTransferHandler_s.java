 /**
  * $Revision: $
  * $Date: $
  *
  * Copyright (C) 2006 Jive Software. All rights reserved.
  *
  * This software is published under the terms of the GNU Lesser Public License (LGPL),
  * a copy of which is included in this distribution.
  */
 
 package org.jivesoftware.spark.ui;
 
 import org.jivesoftware.smack.Roster;
 import org.jivesoftware.smack.RosterEntry;
 import org.jivesoftware.smack.RosterGroup;
 import org.jivesoftware.smack.XMPPException;
 import org.jivesoftware.spark.SparkManager;
 import org.jivesoftware.spark.util.SwingWorker;
 import org.jivesoftware.spark.util.log.Log;
 
 import javax.swing.DefaultListModel;
 import javax.swing.JComponent;
 import javax.swing.JList;
 import javax.swing.TransferHandler;
 
 import java.awt.datatransfer.DataFlavor;
 import java.awt.datatransfer.Transferable;
 import java.awt.datatransfer.UnsupportedFlavorException;
 import java.io.IOException;
 import java.util.Collection;
 import java.util.Iterator;
 
 public class ContactGroupTransferHandler extends TransferHandler {
 
 
     private static final DataFlavor flavors[] = {DataFlavor.imageFlavor, DataFlavor.javaFileListFlavor};
 
 
     public int getSourceActions(JComponent c) {
         return TransferHandler.MOVE;
     }
 
 
     public boolean canImport(JComponent comp, DataFlavor flavor[]) {
         if (!(comp instanceof JList)) {
             return false;
         }
 
 
         JList list = (JList)comp;
         ContactGroup group = getContactGroup(list);
         if ((group.isSharedGroup() && !flavor[0].equals(DataFlavor.javaFileListFlavor)) || group.isUnfiledGroup() || group.isOfflineGroup() || (!group.hasAvailableContacts() && flavor[0].equals(DataFlavor.javaFileListFlavor))) {
             return false;
         }
 
         for (int i = 0, n = flavor.length; i < n; i++) {
             for (int j = 0, m = flavors.length; j < m; j++) {
                 if (flavor[i].equals(flavors[j])) {
                     return true;
                 }
             }
         }
         return false;
     }
 
     protected void exportDone(JComponent c, Transferable data, int action) {
         if (data == null) {
             return;
         }
 
         if (c instanceof JList) {
             JList list = (JList)c;
             ContactGroup group = getContactGroup(list);
             try {
                 ContactItem item = (ContactItem)data.getTransferData(flavors[0]);
                 if (action == MOVE) {
                 }
             }
             catch (UnsupportedFlavorException e) {
                 Log.error(e);
             }
             catch (IOException e) {
                 Log.error(e);
             }
 
         }
     }
 
 
     public Transferable createTransferable(JComponent comp) {
 
         if (comp instanceof JList) {
             JList list = (JList)comp;
             ContactItem source = (ContactItem)list.getSelectedValue();
             Transferable transferable = new ContactItemTransferable(source);
             return transferable;
         }
         return null;
     }
 
     public boolean importData(JComponent comp, Transferable t) {
         if (comp instanceof JList) {
             JList list = (JList)comp;
             ContactGroup group = getContactGroup(list);
 
             if (t.isDataFlavorSupported(flavors[0])) {
                 try {
                     ContactItem item = (ContactItem)t.getTransferData(flavors[0]);
                     int index = list.getSelectedIndex();
                     DefaultListModel model = (DefaultListModel)list.getModel();
                     int size = model.getSize();
                     for (int i = 0; i < size; i++) {
                         ContactItem it = (ContactItem)model.getElementAt(i);
                         if (it.getNickname().equals(item.getNickname())) {
                             return false;
                         }
                     }
 
                     addContactItem(group, item);
                     return true;
                 }
                 catch (UnsupportedFlavorException ignored) {
                 }
                 catch (IOException ignored) {
                 }
             }
             else if (t.isDataFlavorSupported(flavors[1])) {
                 try {
                     Object o = t.getTransferData(flavors[1]);
                     if (o instanceof java.util.Collection) {
                         Collection files = (Collection)o;
                         ContactItem source = (ContactItem)list.getSelectedValue();
                         if (source == null || source.getFullJID() == null) {
                             return false;
                         }
 
                         // Otherwise fire files dropped event.
                         SparkManager.getWorkspace().getContactList().fireFilesDropped(files, source);
                     }
                 }
                 catch (UnsupportedFlavorException e) {
                     Log.error(e);
                 }
                 catch (IOException e) {
                     Log.error(e);
                 }
             }
         }
         return false;
     }
 
     public class ContactItemTransferable implements Transferable {
 
         private ContactItem item;
 
         public ContactItemTransferable(ContactItem item) {
             this.item = item;
         }
 
         // Returns supported flavors
         public DataFlavor[] getTransferDataFlavors() {
             return new DataFlavor[]{DataFlavor.imageFlavor};
         }
 
         // Returns true if flavor is supported
         public boolean isDataFlavorSupported(DataFlavor flavor) {
             return DataFlavor.imageFlavor.equals(flavor);
         }
 
         // Returns image
         public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
             if (!DataFlavor.imageFlavor.equals(flavor)) {
                 throw new UnsupportedFlavorException(flavor);
             }
             return item;
         }
     }
 
     private ContactGroup getContactGroup(JList list) {
         ContactList contactList = SparkManager.getWorkspace().getContactList();
         Iterator groups = contactList.getContactGroups().iterator();
         while (groups.hasNext()) {
             ContactGroup group = (ContactGroup)groups.next();
             if (group.getList() == list) {
                 return group;
             }
 
             ContactGroup subGroup = getSubContactGroup(group, list);
             if (subGroup != null) {
                 return subGroup;
             }
         }
         return null;
     }
 
     private ContactGroup getSubContactGroup(ContactGroup group, JList list) {
         Iterator subGroups = group.getContactGroups().iterator();
         while (subGroups.hasNext()) {
             ContactGroup g = (ContactGroup)subGroups.next();
             if (g.getList() == list) {
                 return g;
             }
 
             // Search subs
             ContactGroup g1 = getSubContactGroup(g, list);
             if (g1 != null) {
                 return g1;
             }
         }
 
         return null;
     }
 
 
     private ContactGroup getContactGroup(String groupName) {
         ContactList contactList = SparkManager.getWorkspace().getContactList();
         ContactGroup group = contactList.getContactGroup(groupName);
         return group;
     }
 
     private void addContactItem(final ContactGroup contactGroup, final ContactItem item) {
         ContactItem newContact = new ContactItem(item.getNickname(), item.getFullJID());
         newContact.setPresence(item.getPresence());
         newContact.setIcon(item.getIcon());
         newContact.getNicknameLabel().setFont(item.getNicknameLabel().getFont());
         contactGroup.addContactItem(newContact);
         contactGroup.clearSelection();
 
         final ContactGroup oldGroup = getContactGroup(item.getGroupName());
 
         SwingWorker worker = new SwingWorker() {
             public Object construct() {
                 Roster roster = SparkManager.getConnection().getRoster();
                 RosterEntry entry = roster.getEntry(item.getFullJID());
 
                 RosterGroup groupFound = null;
 
                for(RosterGroup group : roster.getGroups()){
                     if (group.getName().equals(contactGroup.getGroupName())) {
                         try {
                             groupFound = group;
                             group.addEntry(entry);
                         }
                         catch (XMPPException e1) {
                             Log.error(e1);
                         }
                     }
                 }
 
                 // This is a new group
                 if (groupFound == null) {
                     groupFound = roster.createGroup(contactGroup.getGroupName());
                     try {
                         groupFound.addEntry(entry);
                     }
                     catch (XMPPException e) {
                         Log.error(e);
                     }
                 }
                return "ok";
             }
 
             public void finished() {
                // Now try and remove the group from the old one.
                removeContactItem(oldGroup, item);
             }
 
         };
 
         worker.start();
     }
 
 
     public boolean removeContactItem(ContactGroup contactGroup, ContactItem item) {
         if (contactGroup.isSharedGroup()) {
             return false;
         }
 
         if (contactGroup.isUnfiledGroup()) {
             contactGroup.removeContactItem(item);
             contactGroup.fireContactGroupUpdated();
             return true;
         }
 
         // Remove entry from Roster Group
         Roster roster = SparkManager.getConnection().getRoster();
         RosterEntry entry = roster.getEntry(item.getFullJID());
 
         RosterGroup rosterGroup = null;
 
        for(RosterGroup group : roster.getGroups()){
             if (group.getName().equals(contactGroup.getGroupName())) {
                 try {
                     rosterGroup = group;
                     group.removeEntry(entry);
                 }
                 catch (XMPPException e1) {
                     return false;
                 }
             }
         }
 
         if (rosterGroup == null) {
             return false;
         }
 
         if (!rosterGroup.contains(entry)) {
             contactGroup.removeContactItem(item);
             return true;
         }
 
         return false;
     }
 }
 
