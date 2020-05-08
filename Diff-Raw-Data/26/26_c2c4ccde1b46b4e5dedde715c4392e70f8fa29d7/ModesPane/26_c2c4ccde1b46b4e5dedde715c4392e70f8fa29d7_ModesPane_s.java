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
 
 package com.dmdirc.addons.ui_swing.components.modes;
 
 import com.dmdirc.addons.ui_swing.UIUtilities;
 import com.dmdirc.addons.ui_swing.components.text.TextLabel;
 import com.dmdirc.config.ConfigManager;
 
 import java.awt.Insets;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Set;
 import java.util.TreeSet;
 
 import javax.swing.BorderFactory;
 import javax.swing.JCheckBox;
 import javax.swing.JPanel;
 import javax.swing.UIManager;
 
 import net.miginfocom.swing.MigLayout;
 
 /**
  * Abstract panel to retrieve, show and set user and channel modes.
  */
 public abstract class ModesPane extends JPanel {
 
     /** The checkboxes used for boolean modes. */
     private final Map<String, JCheckBox> modeCheckBoxes;
     /** The ParamModePanels used for parameter-requiring modes. */
     private final Map<String, ParamModePanel> modeInputs;
     /** Boolean modes panel. */
     private final JPanel booleanModesPanel;
     /** Param modes panel. */
     private final JPanel paramModesPanel;
     /** Modes set, used for layout. */
     private final Set<String> modes;
 
     public ModesPane() {
         super();
 
        setLayout(new MigLayout("flowy, fillx", "fill", ""));
        booleanModesPanel = new JPanel(new MigLayout("wrap 2, fillx"));
        paramModesPanel = new JPanel(new MigLayout("wrap 2, fillx"));
         modes = new TreeSet<String>(new ModesComparator());
         modeCheckBoxes = new HashMap<String, JCheckBox>();
         modeInputs = new HashMap<String, ParamModePanel>();
         this.setOpaque(UIUtilities.getTabbedPaneOpaque());
     }
 
     public Map<String, JCheckBox> getBooleanModes() {
         return modeCheckBoxes;
     }
 
     public Map<String, ParamModePanel> getParamModes() {
         return modeInputs;
     }
 
     /** Initialises the modes panel. */
     protected void initModesPanel() {
         getBooleanModes().clear();
        final String booleanModes = getOurBooleanModes();
         final String ourBooleanModes = getOurBooleanModes();
         // Lay out all the boolean mode checkboxes
         for (int i = 0; i < booleanModes.length(); i++) {
             final String mode = booleanModes.substring(i, i + 1);
             final boolean state = ourBooleanModes.split(" ")[0].contains(
                     mode.subSequence(0, 1));
             final String text = getModeText(mode);
             final String tooltip = getModeTooltip(mode);
 
             final JCheckBox checkBox = new JCheckBox(text, state);
             checkBox.setMargin(new Insets(0, 0, 0, 0));
             checkBox.setToolTipText(tooltip);
             checkBox.setOpaque(false);
 
             getBooleanModes().put(mode, checkBox);
             if (isModeEnabled(mode)) {
                 checkBox.setEnabled(true);
             } else if (!isModeSettable(mode)) {
                 checkBox.setEnabled(false);
             }
         }
 
         getParamModes().clear();
         final String paramModes = getAllParamModes();
         // Lay out all the parameter-requiring modes
         for (int i = 0; i < paramModes.length(); i++) {
             final String mode = paramModes.substring(i, i + 1);
             final String value = getParamModeValue(mode);
             final boolean state = ourBooleanModes.split(" ")[0].contains(
                     mode.subSequence(0, 1));
 
             final ParamModePanel panel = new ParamModePanel(mode, state, value,
                     getConfigManager());
 
             getParamModes().put(mode, panel);
         }
 
         layoutComponents();
     }
 
     /** Lays out the components. */
     protected void layoutComponents() {
         booleanModesPanel.removeAll();
         paramModesPanel.removeAll();
         modes.clear();
         modes.addAll(getBooleanModes().keySet());
         if (modes.isEmpty()) {
             booleanModesPanel.add(new TextLabel("No boolean modes."));
         }
         for (String mode : modes) {
            booleanModesPanel.add(getBooleanModes().get(mode));
         }
         modes.clear();
 
         modes.addAll(getParamModes().keySet());
         if (modes.isEmpty()) {
             paramModesPanel.add(new TextLabel("No parameter modes."));
         }
         for (String mode : modes) {
             final ParamModePanel modePanel = getParamModes().get(mode);
             paramModesPanel.add(modePanel.getCheckboxComponent());
             paramModesPanel.add(modePanel.getValueComponent(), "growx, pushx");
         }
 
         booleanModesPanel.setBorder(BorderFactory.createTitledBorder(
                 UIManager.getBorder("TitledBorder.border"), "Boolean modes"));
         paramModesPanel.setBorder(BorderFactory.createTitledBorder(
                 UIManager.getBorder("TitledBorder.border"), "Parameter modes"));
 
         booleanModesPanel.setOpaque(UIUtilities.getTabbedPaneOpaque());
         paramModesPanel.setOpaque(UIUtilities.getTabbedPaneOpaque());
 
        add(booleanModesPanel);
        add(paramModesPanel);
     }
 
     /** Updates the panel. */
     public void update() {
         setVisible(false);
         removeAll();
         initModesPanel();
         setVisible(true);
     }
 
     /**
      * Gets the description for a given mode
      *
      * @param mode Mode to be described
      *
      * @return Description of mode
      */
     private String getModeText(final String mode) {
         if (hasModeValue(mode)) {
             return getModeValue(mode) + " [+" + mode + "]";
         } else {
             return "Mode " + mode;
         }
     }
 
     /**
      * Gets a tooltip text for a mode, shows the description and mode value.
      *
      * @param mode Mode to be described
      *
      * @return Tooltip text for a mode
      */
     private String getModeTooltip(final String mode) {
         if (hasModeValue(mode)) {
             return "Mode " + mode + ": " + getModeValue(mode);
         } else {
             return "Mode " + mode + ": Unknown";
         }
     }
 
     /**
      * Processes the channel settings dialog and constructs a mode string for
      * changed modes, then sends this to the server.
      */
     public void save() {
         boolean changed = false;
         final String booleanModes = getAvailableBooleanModes();
         final String ourBooleanModes = getOurBooleanModes();
         final String paramModes = getAllParamModes();
 
         for (int i = 0; i < booleanModes.length(); i++) {
             final String mode = booleanModes.substring(i, i + 1);
             final boolean state = ourBooleanModes.split(" ")[0].contains(
                     mode.subSequence(0, 1));
 
             if (getBooleanModes().get(mode) != null
                     && state != getBooleanModes().get(mode).isSelected()) {
                 changed = true;
                 alterMode(getBooleanModes().get(mode).isSelected(), mode, "");
             }
         }
 
         for (int i = 0; i < paramModes.length(); i++) {
             final String mode = paramModes.substring(i, i + 1);
             final String value = getParamModeValue(mode);
             final boolean state =
                     ourBooleanModes.split(" ")[0].contains(
                     mode.subSequence(0, 1));
             final ParamModePanel paramModePanel = getParamModes().get(mode);
 
             if (state != paramModePanel.getState()
                     || !value.equals(paramModePanel.getValue())) {
                 changed = true;
                 alterMode(paramModePanel.getState(), mode, paramModePanel.getValue());
             }
         }
         if (changed) {
             flushModes();
         }
     }
 
     /**
      * Returns the config manager to retrieve values from
      *
      * @return Config manager
      */
     public abstract ConfigManager getConfigManager();
 
     /**
      * Checks whether there is a plain text description for this mode.
      *
      * @param mode Mode to check
      *
      * @return true iif there is a plain text description
      */
     public abstract boolean hasModeValue(final String mode);
 
     /**
      * Returns the plain text description for a mode.
      *
      * @param mode Mode to check
      *
      * @return Valid plain text description for a mode, or an empty string
      */
     public abstract String getModeValue(final String mode);
 
     /**
      * Is this mode enabled, are we allowed to set it?
      *
      * @param mode Mode to check
      *
      * @return true iif the mode is enabled
      */
     public abstract boolean isModeEnabled(final String mode);
 
     /**
      * Is this mode settable by us?
      *
      * @param mode Mode to check
      *
      * @return true iif the mode is settable
      */
     public abstract boolean isModeSettable(final String mode);
 
     /**
      * Returns all the available boolean modes.
      *
      * @return string containing all available boolean modes
      */
     public abstract String getAvailableBooleanModes();
 
     /**
      * Returns which boolean modes are set.
      *
      * @return string containing all set boolean modes
      */
     public abstract String getOurBooleanModes();
 
     /**
      * Returns all available param modes.
      *
      * @return string containing all available param modes
      */
     public abstract String getAllParamModes();
 
     /**
      * Returns the value of a given parameter mode.
      *
      * @param mode Mode to check
      *
      * @return value of a mode or an empty string
      */
     public abstract String getParamModeValue(final String mode);
 
     /**
      * Queues the specified mode change to be flushed with flushmodes.
      *
      * @param add Whether to add or remove the specified mode
      * @param mode The mode to be changed
      * @param parameter Optional parameter needed to make change
      */
     public abstract void alterMode(final boolean add, final String mode,
             final String parameter);
 
     /**
      * Sends the queued mode changes to the server.
      */
     public abstract void flushModes();
 }
