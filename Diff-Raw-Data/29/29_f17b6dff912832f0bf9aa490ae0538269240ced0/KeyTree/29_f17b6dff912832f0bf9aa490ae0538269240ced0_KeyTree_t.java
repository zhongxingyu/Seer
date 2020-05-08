 /*
  * Copyright (C) 2003, 2004  Pascal Essiembre, Essiembre Consultant Inc.
  * 
  * This file is part of Essiembre ResourceBundle Editor.
  * 
  * Essiembre ResourceBundle Editor is free software; you can redistribute it 
  * and/or modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 2.1 of the License, or (at your option) any later version.
  * 
  * Essiembre ResourceBundle Editor is distributed in the hope that it will be 
  * useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  * 
  * You should have received a copy of the GNU Lesser General Public
  * License along with Essiembre ResourceBundle Editor; if not, write to the 
  * Free Software Foundation, Inc., 59 Temple Place, Suite 330, 
  * Boston, MA  02111-1307  USA
  */
 package com.essiembre.eclipse.i18n.resourcebundle.editors;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 
 import org.eclipse.jface.dialogs.InputDialog;
 import org.eclipse.jface.window.Window;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.KeyAdapter;
 import org.eclipse.swt.events.KeyEvent;
 import org.eclipse.swt.events.MouseAdapter;
 import org.eclipse.swt.events.MouseEvent;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.graphics.Font;
 import org.eclipse.swt.graphics.FontData;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.MessageBox;
 import org.eclipse.swt.widgets.Tree;
 import org.eclipse.swt.widgets.TreeItem;
 
 import com.essiembre.eclipse.i18n.resourcebundle.preferences.Preferences;
 
 /**
  * Tree for displaying and navigating through resource bundle keys.
  * @author Pascal Essiembre
  * @version $Author$ $Revision$ $Date$
  */
 public class KeyTree extends Tree {
 
     /** Font when a tree item as no child. */
     private Font groupFont; //TODO make this one bold + gray
     /** Default font for tree item. */
     private Font keyFont;
     //TODO add a font for when a group is also a key (bold + black)
     
     /** All tree items, keyed by key or group key name. */
     private Map keyItems = new HashMap();
     
     
     /** All bundles. */
     private Bundles bundles;
     
     /**
      * Constructor.
      * @param parent parent composite
      * @param bundles all bundles
      */
     public KeyTree(Composite parent, final Bundles bundles) {
         super(parent, SWT.SINGLE | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
         this.bundles = bundles;
 
         // Compute fonts
         keyFont = getFont();
         FontData[] fontData = getFont().getFontData();
         for (int i = 0; i < fontData.length; i++) {
             fontData[i].setStyle(SWT.BOLD);
         }
         groupFont = new Font(getDisplay(), fontData);
 
 //        setFont(orphanFont);
         refresh();
         GridData gridData = new GridData();
         gridData.verticalAlignment = GridData.FILL;
         gridData.grabExcessVerticalSpace = true;
         gridData.horizontalAlignment = GridData.FILL;
         gridData.grabExcessHorizontalSpace = true;
         setLayoutData(gridData);
         addSelectionListener(new SelectionAdapter () {
             public void widgetSelected(SelectionEvent event) {
                 bundles.refreshTextBoxes(getSelectedKey());
             }
         });
         addKeyListener(new KeyAdapter() {
             public void keyReleased(KeyEvent event) {
                 if (event.character == SWT.DEL) {
                     String key = getSelectedKey();
                     TreeItem item = getSelectedItem();
                     MessageBox msgBox = new MessageBox(
                             getShell(), SWT.ICON_QUESTION|SWT.OK|SWT.CANCEL);
                     if (item.getItemCount() == 0) {
                         // Delete single key
                         msgBox.setMessage("Are you sure you want to "
                                 + "delete \"" + key + "\"?");
                         msgBox.setText("Delete key?");
                         if (msgBox.open() == SWT.OK) {
                             bundles.removeKey(key);
                             refresh();
                             bundles.refreshTextBoxes(key);
                         }
                     } else {
                         // Delete group
                         msgBox.setMessage("Are you sure you want to "
                                 + "delete all keys under \"" 
                                 + item.getText() + "\"?");
                         msgBox.setText("Delete keys?");
                         if (msgBox.open() == SWT.OK) {
                             String[] keys = 
                                     getAllKeysInGroup(getSelectedItem());
                             for (int i = 0; i < keys.length; i++) {
                                 bundles.removeKey(keys[i]);
                             }
                             refresh();
                             bundles.refreshTextBoxes(key);
                         }
                     }
                 }
             }
         });
         addMouseListener(new MouseAdapter() {
             public void mouseDoubleClick(MouseEvent event) {
                 String key = getSelectedKey();
                 TreeItem item = getSelectedItem();
                 if (item.getItemCount() == 0) {
                     // Rename single item
                     InputDialog dialog = new InputDialog(
                             getShell(), "Rename key",
                             "Rename \"" + key + "\" to:", key, null);
                     dialog.open();
                     if (dialog.getReturnCode() == Window.OK ) {
                         String newKey = dialog.getValue();
                         bundles.modifyKey(key, newKey);
                         refresh(newKey);
                     }
                 } else {
                     // Rename all keys in group
                    String path = getItemPath(item);
                     InputDialog dialog = new InputDialog(
                             getShell(), "Rename key group",
                            "Rename key group \"" + path 
                                   + "\" to (all nested keys wll be renamed):",
                            path, null);
                     dialog.open();
                     if (dialog.getReturnCode() == Window.OK ) {
                         String newGroup = dialog.getValue();
                         String[] keys = getAllKeysInGroup(getSelectedItem());
                         for (int i = 0; i < keys.length; i++) {
                             //TODO ensure key/newGroup are full (unique)
                             bundles.modifyKey(keys[i], keys[i].replaceFirst(
                                    "^" + path, newGroup));
                         }
                         refresh(newGroup);
                     }
                 }
             }
         });
     }
 
     public TreeItem getSelectedItem() {
         TreeItem item = null;
         if (getSelection().length > 0) {
             item = getSelection()[0];
         }
         return item;
     }
 
     public String getSelectedKey() {
         String key = null;
         TreeItem item = getSelectedItem();
         if (item != null) {
             key = (String) item.getData();
         }
         return key;
     }
     
     public void refresh() {
         refresh(null);
     }
     
     public void refresh(String selectedKey) {
         keyItems.clear();
         removeAll();
         for (Iterator iter = bundles.getKeys().iterator(); iter.hasNext();) {
             String key = (String) iter.next();
             if (true) {
                 // Grouped display
                 addGroupKeyItem(key);
             } else {
                 // Flat display
                 TreeItem keyItem = null;
                 keyItem = new TreeItem(this, SWT.NONE);
                 keyItem.setText(key);
                 keyItem.setData(key);
                 keyItems.put(key, keyItem);
             }
         }
         if (selectedKey != null) {
             setSelection(new TreeItem[] {
                     (TreeItem) keyItems.get(selectedKey) });
             showSelection();
         }
     }
     
     private String addGroupKeyItem(String key) {
         //TODO have a method to escape some values.
         String escapedSeparator = "\\" + Preferences.getKeyGroupSeparator();
         
         String[] groups = key.split(escapedSeparator);
         TreeItem treeItem = null;
         StringBuffer group = new StringBuffer();
         for (int i = 0; i < groups.length - 1; i++) {
             if (i > 0) {
                 group.append(Preferences.getKeyGroupSeparator());
             }
             group.append(groups[i]);
             TreeItem groupItem = (TreeItem) keyItems.get(group.toString());
             // Create new group
             if (groupItem == null) {
                 if (treeItem == null) {
                     groupItem = new TreeItem(this, SWT.NONE);
                 } else {
                     groupItem = new TreeItem(treeItem, SWT.NONE);
                 }
             }
             groupItem.setText(groups[i]);
             groupItem.setFont(groupFont);
             keyItems.put(group.toString(), groupItem);
             treeItem = groupItem;
         }
         // Add leaf
         String keyLeaf = groups[groups.length - 1];
         if (treeItem == null) {
             treeItem = new TreeItem(this, SWT.NONE);
         } else {
             treeItem = new TreeItem(treeItem, SWT.NONE);
         }
         treeItem.setText(keyLeaf);
         treeItem.setData(key);
         if (group.length() > 0) {
             group.append(Preferences.getKeyGroupSeparator());
         }
         group.append(keyLeaf);
         keyItems.put(group.toString(), treeItem);
         return group.toString();
     }
     
     /**
      * Gets all keys under a group item.  This includes all descendants, not
      * just direct ones.
      * @param groupItem item under which to get all keys
      * @return all keys under given group item
      */
     public String[] getAllKeysInGroup(TreeItem groupItem) {
         Collection allItems = new ArrayList();
         allItems.add(groupItem);
         findAllItemsInGroup(allItems, groupItem);
         Collection keys = new ArrayList();
         for (Iterator iter = allItems.iterator(); iter.hasNext();) {
             TreeItem item = (TreeItem) iter.next();
             if (item.getData() != null) {
                 keys.add((String) item.getData());
             }
         }
         return (String[]) keys.toArray(new String[]{});
     }
     
     /**
      * Store all intems in given group, in given collection.
      * @param treeItems all tree items found under group, plus initial content
      * @param groupItem item under which to get children
      */
     private void findAllItemsInGroup(
             Collection treeItems, TreeItem groupItem) {
         TreeItem[] items = groupItem.getItems();
         treeItems.addAll(Arrays.asList(items));
         for (int i = 0; i < items.length; i++) {
             findAllItemsInGroup(treeItems, items[i]);
         }
     }
    
    /**
     * Gets an item's path, which is all labels in a branch, separated by
     * preffered character separator.
     * @param item tree item
     * @return path
     */
    private String getItemPath(TreeItem item) {
        StringBuffer path = new StringBuffer(item.getText());
        TreeItem parentItem = item;
        while ((parentItem = parentItem.getParentItem()) != null) {
            path.insert(0, Preferences.getKeyGroupSeparator());
            path.insert(0, parentItem.getText());
        }
        return path.toString();
    }
 }
