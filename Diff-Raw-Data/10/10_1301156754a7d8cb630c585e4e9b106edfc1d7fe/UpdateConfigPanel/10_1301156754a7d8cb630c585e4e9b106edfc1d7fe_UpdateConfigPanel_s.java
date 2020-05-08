 /*
  * Copyright (c) 2006-2015 DMDirc Developers
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
 
 package com.dmdirc.addons.ui_swing.dialogs.prefs;
 
 import com.dmdirc.ClientModule.GlobalConfig;
 import com.dmdirc.ClientModule.UserConfig;
 import com.dmdirc.DMDircMBassador;
 import com.dmdirc.addons.ui_swing.components.GenericTableModel;
 import com.dmdirc.addons.ui_swing.components.PackingTable;
 import com.dmdirc.config.prefs.PreferencesInterface;
 import com.dmdirc.events.UserErrorEvent;
 import com.dmdirc.interfaces.config.AggregateConfigProvider;
 import com.dmdirc.interfaces.config.ConfigChangeListener;
 import com.dmdirc.interfaces.config.ConfigProvider;
 import com.dmdirc.interfaces.config.IdentityController;
 import com.dmdirc.logger.ErrorLevel;
 import com.dmdirc.updater.UpdateChannel;
 import com.dmdirc.updater.UpdateChecker;
 import com.dmdirc.updater.UpdateComponent;
 import com.dmdirc.updater.manager.CachingUpdateManager;
 import com.dmdirc.updater.manager.UpdateStatus;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.inject.Inject;
 import javax.swing.DefaultComboBoxModel;
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JComboBox;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 
 import net.miginfocom.swing.MigLayout;
 
 /**
  * Updates configuration UI.
  */
 public class UpdateConfigPanel extends JPanel implements ActionListener,
         PreferencesInterface, ConfigChangeListener {
 
     /** A version number for this class. */
     private static final long serialVersionUID = 1;
     /** Global checkbox. */
     private JCheckBox enable;
     /** Table scroll pane, */
     private JScrollPane scrollPane;
     /** Component table model. */
     private GenericTableModel<UpdateComponentHolder> tableModel;
     /** Check now button. */
     private JButton checkNow;
     /** Update channel. */
     private JComboBox<UpdateChannel> updateChannel;
     /** The configuration to write settings changes to. */
     private final ConfigProvider userConfig;
     /** The configuration to read global settings from. */
     private final AggregateConfigProvider globalConfig;
     /** The manager to read update information from. */
     private final CachingUpdateManager updateManager;
     /** Controller to pass to the update checker. */
     private final IdentityController identityController;
     /** The event bus to post errors to. */
     private final DMDircMBassador eventBus;
 
     /**
      * Instantiates a new update config panel.
      *
      * @param userConfig         The configuration to write settings changes to.
      * @param globalConfig       The configuration to read global settings from.
      * @param updateManager      The manager to read update information from.
      * @param identityController Controller to pass to the update checker.
      * @param eventBus           The event bus to post the errors to
      */
     @Inject
     public UpdateConfigPanel(
             @UserConfig final ConfigProvider userConfig,
             @GlobalConfig final AggregateConfigProvider globalConfig,
             final CachingUpdateManager updateManager,
             final IdentityController identityController,
             final DMDircMBassador eventBus) {
         this.userConfig = userConfig;
         this.globalConfig = globalConfig;
         this.updateManager = updateManager;
         this.identityController = identityController;
         this.eventBus = eventBus;
 
         initComponents();
         loadModel();
         addListeners();
         layoutComponents();
     }
 
     @Override
     public void save() {
         userConfig.setOption("updater", "enable", enable.isSelected());
         userConfig.setOption("updater", "channel", updateChannel.getSelectedItem().toString());
 
         for (int i = 0; i < tableModel.getRowCount(); i++) {
             final String componentName = tableModel.getValue(i).getComponent().getName();
             if (tableModel.getValue(i).isEnabled()) {
                 userConfig.unsetOption("updater", "enable-" + componentName);
             } else {
                 userConfig.setOption("updater", "enable-" + componentName, false);
             }
         }
     }
 
     private void loadModel() {
         updateManager.getComponents().stream()
                 .map(this::createUpdateComponentHolder)
                 .forEach(tableModel::addValue);
     }
 
     @SuppressWarnings("TypeMayBeWeakened")
     private UpdateComponentHolder createUpdateComponentHolder(final UpdateComponent component) {
         return new UpdateComponentHolder(component,
                 updateManager.getStatus(component) != UpdateStatus.CHECKING_NOT_PERMITTED,
                 component.getVersion().toString());
     }
 
     /**
      * Initialises the components.
      */
     private void initComponents() {
         enable = new JCheckBox();
         scrollPane = new JScrollPane();
         tableModel = new GenericTableModel<>(UpdateComponentHolder.class,
                 (row, column) -> column == 1,
                 (v, row, column) -> {
                     if (column == 1) {
                         tableModel.getValue(row).setEnabled((Boolean) v);
                     }
                 },
                 "getComponentName", "isEnabled", "getVersion");
         tableModel.setHeaderNames("Component", "Enabled?", "Version");
         final PackingTable table = new PackingTable(tableModel, scrollPane);
         checkNow = new JButton("Check now");
         checkNow.setEnabled(globalConfig.getOptionBool("updater", "enable"));
         updateChannel = new JComboBox<>(new DefaultComboBoxModel<>(UpdateChannel.values()));
 
         enable.setSelected(globalConfig.getOptionBool("updater", "enable"));
         UpdateChannel channel = UpdateChannel.NONE;
         try {
             channel = UpdateChannel.valueOf(globalConfig.getOption("updater", "channel"));
         } catch (IllegalArgumentException e) {
             eventBus.publishAsync(new UserErrorEvent(ErrorLevel.LOW, e,
                     "Invalid setting for update channel, defaulting to none.", ""));
         }
         updateChannel.setSelectedItem(channel);
         scrollPane.setViewportView(table);
     }
 
     /**
      * Adds the listeners.
      */
     private void addListeners() {
         checkNow.addActionListener(this);
         globalConfig.addChangeListener("updater", "enable", this);
         enable.addActionListener(this);
     }
 
     /**
      * Lays out the components.
      */
     private void layoutComponents() {
         setLayout(new MigLayout("fill, ins 0, hmax 500"));
 
         add(new JLabel("Update checking:"), "split");
         add(enable, "growx");
         add(updateChannel, "growx, pushx, wrap");
         add(scrollPane, "wrap, grow, push");
         add(checkNow, "right");
     }
 
     @Override
     public void actionPerformed(final ActionEvent e) {
         if (enable == e.getSource()) {
             checkNow.setEnabled(enable.isSelected());
         } else {
            UpdateChecker.checkNow(updateManager, identityController, eventBus);
         }
     }
 
     @Override
     public void configChanged(final String domain, final String key) {
         checkNow.setEnabled(globalConfig.getOptionBool("updater", "enable"));
     }
 
 }
