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
 
 package com.dmdirc.addons.ui_swing.components.expandingsettings;
 
 import com.dmdirc.addons.ui_swing.PrefsComponentFactory;
 import com.dmdirc.addons.ui_swing.UIUtilities;
 import com.dmdirc.addons.ui_swing.components.text.TextLabel;
 import com.dmdirc.config.Identity;
 import com.dmdirc.config.prefs.PreferencesCategory;
 import com.dmdirc.config.prefs.PreferencesSetting;
 
 import com.dmdirc.util.DoubleMap;
 
 import javax.swing.BorderFactory;
 import javax.swing.JComponent;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.UIManager;
 
 import net.miginfocom.swing.MigLayout;
 
 /**
  * Settings panel.
  */
 public class SettingsPanel extends JPanel {
 
     /**
      * A version number for this class. It should be changed whenever the class
      * structure is changed (or anything else that would prevent serialized
      * objects being unserialized with the new class).
      */
     private static final long serialVersionUID = 3;
     /** Config manager. */
     private final transient Identity config;
     /** Current Settings. */
     private final DoubleMap<PreferencesSetting, JComponent> settings;
     /** Info label. */
     private TextLabel infoLabel;
     /** Current options panel. */
     private CurrentOptionsPanel currentOptionsPanel;
     /** Add option panel. */
     private AddOptionPanel addOptionPanel;
     /** Current options scroll pane. */
     private JScrollPane scrollPane;
     /** Use external padding. */
     private final boolean padding;
     /** Preferences Category. */
     private PreferencesCategory category;
 
     /**
      * Creates a new instance of SettingsPanel.
      *
      * @param config Config to use
      * @param infoText Info blurb.
      */
     public SettingsPanel(final Identity config, final String infoText) {
         this(config, infoText, true);
     }
 
     /**
      * Creates a new instance of SettingsPanel.
      *
      * @param config Config to use
      * @param infoText Info blurb.
      * @param padding Should we add padding to the panel?
      */
     public SettingsPanel(final Identity config, final String infoText,
             final boolean padding) {
         super();
 
         settings = new DoubleMap<PreferencesSetting, JComponent>();
 
         this.setOpaque(UIUtilities.getTabbedPaneOpaque());
         this.config = config;
         this.padding = padding;
 
         initComponents(infoText);
         layoutComponents();
     }
 
     /**
      * Initialises the components.
      *
      * @param infoText Info blurb.
      */
     private void initComponents(final String infoText) {
         infoLabel = new TextLabel(infoText);
         infoLabel.setVisible(!infoText.isEmpty());
 
         addOptionPanel = new AddOptionPanel(this);
         currentOptionsPanel = new CurrentOptionsPanel(this);
         scrollPane = new JScrollPane(currentOptionsPanel);
 
         scrollPane.setBorder(BorderFactory.createTitledBorder(UIManager.
                 getBorder("TitledBorder.border"), "Current settings"));
         addOptionPanel.setBorder(BorderFactory.createTitledBorder(UIManager.
                 getBorder("TitledBorder.border"), "Add new setting"));
 
         scrollPane.setOpaque(UIUtilities.getTabbedPaneOpaque());
         scrollPane.getViewport().setOpaque(UIUtilities.getTabbedPaneOpaque());
     }
 
     /**
      * Lays out the components.
      */
     private void layoutComponents() {
         setLayout(new MigLayout("fill, wrap 1, hidemode 3, "
                 + (padding ? "ins rel" : "ins 0")));
 
         add(infoLabel, "growx");
         add(scrollPane, "grow, pushy");
         add(addOptionPanel, "growx");
     }
 
     /**
      * Adds an option to the settings panel.
      *
      * @param optionName Option name
      * @param displayName Display name
      * @param type Option type
      */
     public void addOption(final PreferencesCategory category) {
         if (config == null) {
             return;
         }
 
         this.category = category;
 
         for (PreferencesSetting setting : category.getSettings()) {
             if (settings.get(setting) == null) {
                 final JComponent component = PrefsComponentFactory
                         .getComponent(setting);
                 component.setName(setting.getTitle());
                 settings.put(setting, component);
             }
             if (setting.isSet()) {
                 addCurrentOption(settings.get(setting));
             } else {
                 addAddableOption(settings.get(setting));
             }
         }
     }
 
     /** Updates the options. */
     public void update() {
         addOptionPanel.clearOptions();
         currentOptionsPanel.clearOptions();
 
         for (PreferencesSetting setting : category.getSettings()) {
             if (setting.isSet()) {
                 addCurrentOption(settings.get(setting));
             } else {
                 addAddableOption(settings.get(setting));
             }
         }
     }
 
     /** Saves the options to the config. */
     public void save() {
        if (category != null) {
            category.save();
        }
     }
 
     /** Dismisses the options changed. */
     public void dismiss() {
        if (category != null) {
            category.dismiss();
        }
     }
 
     /**
      * Adds a current option.
      *
      * @param optionName option to add
      * @param type Option type
      * @param value Option value
      */
     protected void addCurrentOption(final JComponent setting) {
         currentOptionsPanel.addOption(setting);
     }
 
     /**
      * Deletes a current option.
      *
      * @param optionName Option to delete
      * @param type Option type
      */
     protected void removeCurrentOption(final JComponent setting) {
         currentOptionsPanel.delOption(setting);
     }
 
     /**
      * Adds an addable option.
      *
      * @param optionName Option name
      */
     protected void addAddableOption(final JComponent setting) {
         settings.getKey(setting).setValue(null);
         addOptionPanel.addOption(setting);
     }
 
     /**
      * Returns the component associated with a setting.
      *
      * @param component The component to get the setting for
      *
      * @return Setting or null if not found
      */
     public PreferencesSetting getSettingForComponent(final JComponent comp) {
         return settings.getKey(comp);
     }
 }
