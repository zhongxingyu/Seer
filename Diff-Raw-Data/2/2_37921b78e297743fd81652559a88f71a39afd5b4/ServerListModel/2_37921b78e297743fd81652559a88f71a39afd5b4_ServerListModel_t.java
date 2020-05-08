 /*
  * Copyright (c) 2006-2010 Chris Smith, Shane Mc Cormack, Gregory Holmes
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
  * SOFTWARE.
  */
 
 package com.dmdirc.addons.ui_swing.dialogs.serverlist;
 
 import com.dmdirc.actions.wrappers.PerformWrapper.PerformDescription;
 import com.dmdirc.actions.wrappers.PerformWrapper.PerformType;
 import com.dmdirc.logger.ErrorLevel;
 import com.dmdirc.logger.Logger;
 import com.dmdirc.serverlists.ServerEntry;
 import com.dmdirc.serverlists.ServerGroup;
 import com.dmdirc.serverlists.ServerGroupItem;
 import com.dmdirc.serverlists.ServerList;
 import com.dmdirc.util.ListenerList;
 
 import java.io.IOException;
 import java.net.URI;
 import java.util.List;
 
 import javax.swing.tree.DefaultMutableTreeNode;
 import javax.swing.tree.DefaultTreeModel;
 
 /**
  * Model proxying requests from the core server list model to the swing ui.
  */
 public class ServerListModel {
 
     /** Server list. */
     private final ServerList list = new ServerList();
     /** Listener list. */
     private final ListenerList listeners;
     /** Active server item. */
     private ServerGroupItem activeItem;
 
     /**
      * Creates a new server list model.
      */
     public ServerListModel() {
         listeners = new ListenerList();
     }
 
     /**
      * Returns a populated tree model for this server list model.
      *
      * @return Populated tree model
      */
     public DefaultTreeModel getTreeModel() {
         return populateModel(new DefaultTreeModel(
                 new DefaultMutableTreeNode("All Servers")));
     }
 
     /**
      * Has this model got any groups?
      *
      * @return true iif there are server groups
      */
     public boolean hasItems() {
         return list.getServerGroups().size() > 0;
     }
 
     /**
      * Populates a tree model for this server list model.
      *
      * @param model Un-populated tree model to populate
      *
      * @return Populated tree model
      */
     public DefaultTreeModel populateModel(final DefaultTreeModel model) {
         for (ServerGroup group : list.getServerGroups()) {
             final DefaultMutableTreeNode child = new DefaultMutableTreeNode(
                     group);
             model.insertNodeInto(child, (DefaultMutableTreeNode) model
                     .getRoot(), model.getChildCount(model.getRoot()));
             model.nodeStructureChanged((DefaultMutableTreeNode) model
                     .getRoot());
             addGroups(model, child, group.getItems());
         }
         return model;
     }
 
     /**
      * Recursively adds groups to the specified tree model.
      *
      * @param model Tree model
      * @param parent Parent node to populate
      * @param items Items to add to parent node
      */
     private void addGroups(final DefaultTreeModel model,
             final DefaultMutableTreeNode parent,
             final List<ServerGroupItem> items) {
         for (ServerGroupItem group : items) {
             final DefaultMutableTreeNode child = new DefaultMutableTreeNode(
                     group);
             model.insertNodeInto(child, parent, model.getChildCount(parent));
             if (group instanceof ServerGroup) {
                 addGroups(model, child, ((ServerGroup) group).getItems());
             }
         }
     }
 
     /**
      * Adds a server list listener to be notified of changes.
      *
      * @param listener Listener to add
      */
     public void addServerListListener(final ServerListListener listener) {
         listeners.add(ServerListListener.class, listener);
     }
 
     /**
      * Sets the selected item in this model.
      *
      * @param item Newly selected item
      */
     public void setSelectedItem(final ServerGroupItem item) {
         activeItem = item;
         for (ServerListListener listener : listeners.get(
                 ServerListListener.class)) {
             listener.serverGroupChanged(item);
         }
     }
 
     /**
      * Gets the perform description for the selected item.
      *
      * @return Perform description for the active sever group item
      */
     public PerformDescription getSelectedItemPerformDescription() {
         PerformDescription perform;
         if (activeItem instanceof ServerEntry) {
             perform = new PerformDescription(PerformType.SERVER, activeItem
                     .getName());
         } else if (activeItem instanceof ServerGroup
                 && ((ServerGroup) activeItem).getNetwork() != null) {
                 perform = new PerformDescription(PerformType.NETWORK,
                         ((ServerGroup) activeItem).getNetwork());
         } else {
             perform = null;
         }
         return perform;
     }
 
     /**
      * Gets the currently selected item.
      *
      * @return Currently selected item
      */
     public ServerGroupItem getSelectedItem() {
         return activeItem;
     }
 
     /**
      * Saves the changes.
      *
      * @param  save Do we need to save changes
      */
     public void dialogClosed(final boolean save) {
         for (ServerListListener listener : listeners.get(
                 ServerListListener.class)) {
             listener.dialogClosed(save);
         }
     }
 
     /**
      * Adds a group to this model.
      *
      * @param parentGroup Parent group
      * @param groupName Group name (not null or empty)
      * @param networkName Network name (may be null or empty)
      */
     public void addGroup(final ServerGroup parentGroup,
             final String groupName, final String networkName) {
         final ServerGroup sg = new ServerGroup(groupName);
         if (networkName != null && !networkName.isEmpty()) {
             sg.setNetwork(networkName);
         }
         try {
             if (parentGroup == null) {
                 list.addServerGroup(sg);
             } else {
                parentGroup.addItem(sg);
             }
             for (ServerListListener listener : listeners.get(
                     ServerListListener.class)) {
                 listener.serverGroupAdded(parentGroup, sg);
             }
         } catch (final IOException ex) {
             Logger.userError(ErrorLevel.MEDIUM, "Unable to create group", ex);
         }
     }
 
     /**
      * Adds a group to this model.
      *
      * @param parentGroup Parent group
      * @param entryName  name (not null or empty)
      * @param url Valid URI
      */
     public void addEntry(final ServerGroup parentGroup, final String entryName,
             final URI url) {
         final ServerGroupItem sg = new ServerEntry(parentGroup, entryName, url,
                 null);
         parentGroup.addItem(sg);
         for (ServerListListener listener : listeners.get(
                 ServerListListener.class)) {
             listener.serverGroupAdded(parentGroup, sg);
         }
     }
 }
