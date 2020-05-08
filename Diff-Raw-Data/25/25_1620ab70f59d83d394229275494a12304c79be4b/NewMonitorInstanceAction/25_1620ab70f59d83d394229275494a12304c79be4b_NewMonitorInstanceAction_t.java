 /*
  * Copyright (c) 2012 Alvin R. de Leon. All Rights Reserved.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package org.piraso.ui.base.action;
 
 import org.piraso.ui.api.GeneralSettingsModel;
 import org.piraso.ui.api.WorkingSetSettings;
 import org.piraso.ui.api.manager.ModelEvent;
 import org.piraso.ui.api.manager.ModelOnChangeListener;
 import org.piraso.ui.api.manager.SingleModelManagers;
 import org.piraso.ui.base.ContextMonitorDispatcher;
 import org.piraso.ui.base.NewMonitorInstanceDialog;
 import org.piraso.ui.base.manager.ModelManagers;
 import org.openide.awt.*;
 import org.openide.util.ImageUtilities;
 import org.openide.util.actions.Presenter;
 
 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.Iterator;
 import java.util.List;
 
 /**
  * New Context monitor action
  */
 @ActionID(category="File", id="org.piraso.ui.base.action.NewMonitorInstanceAction")
 @ActionRegistration(displayName="New Monitor Instance")
 @ActionReferences({
         @ActionReference(path="Menu/File", position=270),
         @ActionReference(path="Toolbars/File", position=270)
 })
 public class NewMonitorInstanceAction extends AbstractAction implements ActionListener, Presenter.Menu, Presenter.Toolbar {
     private static final String SMALL_ICON_PATH = "org/piraso/ui/base/icons/new.png";
     private static final String LARGE_ICON_PATH = "org/piraso/ui/base/icons/new24.png";
 
     private static final int MAX_MENU_ITEM_COUNT = 25;
 
     public NewMonitorInstanceAction() {
         putValue("iconBase", SMALL_ICON_PATH);
         putValue(Action.SHORT_DESCRIPTION, "New Monitor Instance");
     }
 
     @Override
     public void actionPerformed(ActionEvent e) {
         new NewMonitorInstanceDialog().setVisible(true);
     }
 
     @Override
     public JMenuItem getMenuPresenter() {
         JMenu menu = new JMenu("New Monitor Instance");
         menu.setIcon(ImageUtilities.loadImageIcon(SMALL_ICON_PATH, true));
         addMenuItems(menu);
 
         ModelManagers.MONITORS.addModelOnChangeListener(new SyncMenuItemsHandler(menu));
         ModelManagers.PROFILES.addModelOnChangeListener(new SyncMenuItemsHandler(menu));
         SingleModelManagers.GENERAL_SETTINGS.addModelOnChangeListener(new SyncMenuItemsHandler(menu));
 
         return menu;
     }
 
     @Override
     public Component getToolbarPresenter() {
         JPopupMenu popup = new JPopupMenu();
         addMenuItems(popup);
 
         ModelManagers.MONITORS.addModelOnChangeListener(new SyncMenuItemsHandler(popup));
         ModelManagers.PROFILES.addModelOnChangeListener(new SyncMenuItemsHandler(popup));
         SingleModelManagers.GENERAL_SETTINGS.addModelOnChangeListener(new SyncMenuItemsHandler(popup));
 
         JButton button = DropDownButtonFactory.createDropDownButton(ImageUtilities.loadImageIcon(LARGE_ICON_PATH, false), popup);
         Actions.connect(button, this);
 
         return button;
     }
 
     private void addMenuItems(final JComponent menu) {
         List<String> profiles = ModelManagers.PROFILES.getNames();
         GeneralSettingsModel model = SingleModelManagers.GENERAL_SETTINGS.get();
         WorkingSetSettings workingSet = SingleModelManagers.WORKING_SET.get();
 
         int added = 0;
 
         for(Iterator<String> itr = profiles.iterator(); itr.hasNext() && added <= MAX_MENU_ITEM_COUNT;) {
             String profileName = itr.next();
 
             if(model.getWorkingSetName() != null) {
                 String regex = workingSet.getRegex(model.getWorkingSetName());
 
                 if(!profileName.matches(regex)) {
                     continue;
                 }
             }
 
 
             JMenuItem item = new JMenuItem(String.format("Profile: %s", profileName));
             item.addActionListener(new ProfileHandler(profileName));
             menu.add(item);
             added++;
         }
 
         if(added > 0) {
             addSeparator(menu);
         }
 
         List<String> monitors = ModelManagers.MONITORS.getNames();
         for(Iterator<String> itr = monitors.iterator(); itr.hasNext() && added <= MAX_MENU_ITEM_COUNT;) {
             String monitorName = itr.next();
 
             if(model.getWorkingSetName() != null) {
                 String regex = workingSet.getRegex(model.getWorkingSetName());
 
                if(regex != null && !monitorName.matches(regex)) {
                     continue;
                 }
             }
 
             JMenuItem item = new JMenuItem(String.format("Monitor: %s", monitorName));
             item.addActionListener(new MonitorHandler(monitorName));
             menu.add(item);
             added++;
         }
 
         if(added > 0) {
             addSeparator(menu);
         }
 
         JMenuItem item = new JMenuItem(String.format("Others..."));
         item.addActionListener(this);
         menu.add(item);
     }
 
     private void addSeparator(final JComponent menu) {
         if(menu instanceof JMenu) {
             ((JMenu) menu).addSeparator();
         } else if(menu instanceof JPopupMenu) {
             ((JPopupMenu) menu).addSeparator();
         }
     }
 
     private class MonitorHandler implements ActionListener {
 
         private String monitorName;
 
         private MonitorHandler(String monitorName) {
             this.monitorName = monitorName;
         }
 
         @Override
         public void actionPerformed(ActionEvent e) {
             ContextMonitorDispatcher.forwardByMonitorName(monitorName);
         }
     }
 
     private class ProfileHandler implements ActionListener {
 
         private String profileName;
 
         private ProfileHandler(String profileName) {
             this.profileName = profileName;
         }
 
         @Override
         public void actionPerformed(ActionEvent e) {
             ContextMonitorDispatcher.forwardByProfileName(profileName);
         }
     }
 
     private class SyncMenuItemsHandler implements ModelOnChangeListener {
         private JComponent component;
 
         private SyncMenuItemsHandler(JComponent component) {
             this.component = component;
         }
 
         @Override
         public void onChange(ModelEvent evt) {
             component.removeAll();
             addMenuItems(component);
         }
     }
 }
