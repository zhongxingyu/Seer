 /*
  * Copyright (c) 2006-2011 Chris Smith, Shane Mc Cormack, Gregory Holmes
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
 
 import com.dmdirc.addons.ui_swing.components.expandingsettings.SettingsPanel;
 import com.dmdirc.config.IdentityManager;
 import com.dmdirc.addons.serverlists.ServerGroup;
 import com.dmdirc.addons.serverlists.ServerGroupItem;
 import com.dmdirc.config.ConfigManager;
 import com.dmdirc.config.Identity;
 import com.dmdirc.config.prefs.PreferencesManager;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Map.Entry;
 import javax.swing.BorderFactory;
 
 import javax.swing.JPanel;
 import javax.swing.UIManager;
 import javax.swing.border.Border;
 
 import net.miginfocom.swing.MigLayout;
 
 /**
  * Panel for listing and adding settings to the group item.
  */
 public class Settings extends JPanel implements ServerListListener {
 
     /**
      * A version number for this class. It should be changed whenever the class
      * structure is changed (or anything else that would prevent serialized
      * objects being unserialized with the new class).
      */
     private static final long serialVersionUID = 2;
     /** Server list model. */
     private final ServerListModel model;
     /** Settings panel. */
     private final Map<ServerGroupItem, SettingsPanel> panels =
             new HashMap<ServerGroupItem, SettingsPanel>();
     /** Platform border. */
     private final Border border;
 
     /**
      * Instantiates a new settings panel.
      *
      * @param model Backing model
      */
     public Settings(final ServerListModel model) {
         super();
         this.model = model;
         addListeners();
         border = UIManager.getBorder("TitledBorder.border");
         setBorder(BorderFactory.createTitledBorder(border, "Network Settings"));
         setLayout(new MigLayout("fill, ins 0"));
         add(getSettingsPanel(model.getSelectedItem()), "grow, push");
     }
 
     /**
      * Adds required listeners.
      */
     private void addListeners() {
         model.addServerListListener(this);
     }
 
     /** {@inheritDoc} */
     @Override
     public void serverGroupChanged(final ServerGroupItem item) {
         setVisible(false);
         removeAll();
         if (item != null) {
             if (item.getGroup() == item) {
                 setBorder(BorderFactory.createTitledBorder(border,
                         "Network settings"));
             } else {
                 setBorder(BorderFactory.createTitledBorder(border,
                         "Server settings"));
             }
         }
         add(getSettingsPanel(item), "grow, push");
         setVisible(true);
     }
 
     /**
      * Gets a settings panel for the specified group item, creating it if
      * required.
      *
      * @param item Group item panel
      *
      * @return Settings panel for group item
      */
     private SettingsPanel getSettingsPanel(final ServerGroupItem item) {
         if (!panels.containsKey(item)) {
             if (item instanceof ServerGroup) {
                 panels.put(item, new SettingsPanel(IdentityManager.
                         getNetworkConfig(item.getName()), "", false));
             } else if (item == null) {
                 panels.put(null, new SettingsPanel(null, "", false));
             } else {
                 panels.put(item, new SettingsPanel(IdentityManager.
                         getServerConfig(item.getName()), "", false));
             }
            addSettings(panels.get(item), new ConfigManager("irc", "", 
                    item.getGroup().getNetwork(), item.getName()),
                    IdentityManager.getServerConfig(item.getName()));
         }
         return panels.get(item);
     }
 
     /** {@inheritDoc} */
     @Override
     public void dialogClosed(final boolean save) {
         if (save) {
             for (Entry<ServerGroupItem, SettingsPanel> entry : panels.entrySet()) {
                 entry.getValue().save();
             }
         }
     }
 
     /** {@inheritDoc} */
     @Override
     public void serverGroupAdded(final ServerGroupItem parent,
             final ServerGroupItem group) {
         //Ignore
     }
 
     /** {@inheritDoc} */
     @Override
     public void serverGroupRemoved(final ServerGroupItem parent,
             final ServerGroupItem group) {
         //Ignore
     }
 
     /**
      * Adds the settings to the panel.
      * 
      * @param settingsPanel Settings panel to add settings to
      */
     private void addSettings(final SettingsPanel settingsPanel,
             final ConfigManager manager, final Identity identity) {
         settingsPanel.addOption(PreferencesManager.getPreferencesManager()
                 .getServerSettings(manager, identity));
     }
 }
