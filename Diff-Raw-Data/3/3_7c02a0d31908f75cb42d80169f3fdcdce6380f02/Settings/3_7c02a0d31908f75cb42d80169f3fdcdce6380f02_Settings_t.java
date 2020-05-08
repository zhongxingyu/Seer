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
 
 import com.dmdirc.addons.ui_swing.components.expandingsettings.SettingsPanel;
 import com.dmdirc.addons.ui_swing.components.expandingsettings.SettingsPanel.OptionType;
 import com.dmdirc.config.IdentityManager;
 import com.dmdirc.serverlists.ServerGroup;
 import com.dmdirc.serverlists.ServerGroupItem;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import javax.swing.JPanel;
 
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
 
     /**
      * Instantiates a new settings panel.
      *
      * @param model Backing model
      */
     public Settings(final ServerListModel model) {
         super();
         this.model = model;
         addListeners();
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
                         getNetworkConfig(item.getName()), ""));
             } else if (item == null) {
                panels.put(null, new SettingsPanel(null, ""));
             } else {
                 panels.put(item, new SettingsPanel(IdentityManager.
                         getServerConfig(item.getName()), ""));
             }
             addSettings(panels.get(item));
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
     private void addSettings(final SettingsPanel settingsPanel) {
         settingsPanel.addOption("channel.splitusermodes", "Split user modes",
                 OptionType.CHECKBOX);
         settingsPanel.addOption("channel.sendwho", "Send WHO",
                 OptionType.CHECKBOX);
         settingsPanel.addOption("channel.showmodeprefix", "Show mode prefix",
                 OptionType.CHECKBOX);
 
         settingsPanel.addOption("general.cyclemessage", "Cycle message",
                 OptionType.TEXTFIELD);
         settingsPanel.addOption("general.kickmessage", "Kick message",
                 OptionType.TEXTFIELD);
         settingsPanel.addOption("general.partmessage", "Part message",
                 OptionType.TEXTFIELD);
 
         settingsPanel.addOption("ui.backgroundcolour", "Background colour",
                 OptionType.COLOUR);
         settingsPanel.addOption("ui.foregroundcolour", "Foreground colour",
                 OptionType.COLOUR);
         settingsPanel.addOption("ui.frameBufferSize", "Textpane buffer limit",
                 OptionType.SPINNER);
 
         settingsPanel.addOption("ui.inputBufferSize", "Input buffer size",
                 OptionType.SPINNER);
         settingsPanel.addOption("ui.textPaneFontName", "Textpane font name",
                 OptionType.TEXTFIELD);
         settingsPanel.addOption("ui.textPaneFontSize", "Textpane font size",
                 OptionType.SPINNER);
 
         settingsPanel.addOption("ui.inputbackgroundcolour",
                 "Input field background colour",
                 OptionType.COLOUR);
         settingsPanel.addOption("ui.inputforegroundcolour",
                 "Input field foreground colour",
                 OptionType.COLOUR);
         settingsPanel.addOption("ui.nicklistbackgroundcolour",
                 "Nicklist background colour",
                 OptionType.COLOUR);
         settingsPanel.addOption("ui.nicklistforegroundcolour",
                 "Nicklist foreground colour",
                 OptionType.COLOUR);
         settingsPanel.addOption("ui.shownickcoloursinnicklist",
                 "Show coloured nicks in nicklist",
                 OptionType.CHECKBOX);
         settingsPanel.addOption("ui.shownickcoloursintext",
                 "Show coloured nicks in textpane",
                 OptionType.CHECKBOX);
 
         settingsPanel.addOption("general.closechannelsonquit",
                 "Close channels on quit",
                 OptionType.CHECKBOX);
         settingsPanel.addOption("general.closechannelsondisconnect",
                 "Close channels on disconnect",
                 OptionType.CHECKBOX);
         settingsPanel.addOption("general.closequeriesonquit",
                 "Close queries on quit",
                 OptionType.CHECKBOX);
         settingsPanel.addOption("general.closequeriesondisconnect",
                 "Close queries on disconnect",
                 OptionType.CHECKBOX);
         settingsPanel.addOption("general.quitmessage", "Quit message",
                 OptionType.TEXTFIELD);
         settingsPanel.addOption("general.reconnectmessage", "Reconnect message",
                 OptionType.TEXTFIELD);
         settingsPanel.addOption("general.rejoinchannels",
                 "Rejoin channels on reconnect",
                 OptionType.CHECKBOX);
         settingsPanel.addOption("general.pingtimeout", "Ping timeout",
                 OptionType.SPINNER);
     }
 }
