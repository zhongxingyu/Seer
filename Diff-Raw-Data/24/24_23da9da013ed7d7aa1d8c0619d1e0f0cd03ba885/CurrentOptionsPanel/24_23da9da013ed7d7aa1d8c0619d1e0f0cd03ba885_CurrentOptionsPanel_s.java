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
 
 import com.dmdirc.addons.ui_swing.UIUtilities;
 import com.dmdirc.addons.ui_swing.components.ImageButton;
 import com.dmdirc.ui.IconManager;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.swing.JComponent;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 
 import net.miginfocom.swing.MigLayout;
 
 /**
  * Current options panel.
  */
 public final class CurrentOptionsPanel extends JPanel implements
         ActionListener {
 
     /**
      * A version number for this class. It should be changed whenever the class
      * structure is changed (or anything else that would prevent serialized
      * objects being unserialized with the new class).
      */
     private static final long serialVersionUID = 2;
     /** Parent settings panel. */
     private final SettingsPanel parent;
     /** Curent options to display. */
     private final List<JComponent> settings;
 
     /**
      * Creates a new instance of CurrentOptionsPanel.
      *
      * @param parent Parent settings panel.
      */
     protected CurrentOptionsPanel(final SettingsPanel parent) {
         super();
 
         this.parent = parent;
 
         this.setOpaque(UIUtilities.getTabbedPaneOpaque());
         settings = new ArrayList<JComponent>();
     }
 
     /** Clears all the current options. */
     protected void clearOptions() {
         settings.clear();
         populateCurrentSettings();
     }
 
     /**
      * Adds a current option.
      *
      * @param optionName option to add
      * @param type Option type
      * @param value Option value
      */
     protected void addOption(final JComponent setting) {
         settings.add(setting);
 
         populateCurrentSettings();
     }
 
     /**
      * Deletes a current option.
      *
      * @param optionName Option to delete
      * @param type Option type
      */
     protected void delOption(final JComponent setting) {
         settings.remove(setting);
         populateCurrentSettings();
     }
 
     /**
      * Adds an option to the current options pane.
      * @param configName config option name
      * @param displayName config option display name
      * @param panel parent panel
      * @param component Option component to add
      */
     private void addCurrentOption(final JComponent component) {
         final JLabel label = new JLabel();
        final ImageButton<JComponent> button = new ImageButton(
                 component.getName(),
                 IconManager.getIconManager().getIcon("close-inactive"),
                 IconManager.getIconManager().getIcon("close-active"));
         button.setObject(component);
 
         label.setText(component.getName() + ": ");
         label.setLabelFor(component);
 
         button.addActionListener(this);
 
         add(label);
         add(component, "");
         add(button, "wrap");
     }
 
 
     /** Populates the current settings. */
     protected void populateCurrentSettings() {
         setVisible(false);
 
         setLayout(new MigLayout("fillx, aligny top, wmax 100%"));
 
         removeAll();
 
         for (JComponent setting : settings) {
             addCurrentOption(setting);
         }
 
         setVisible(true);
     }
 
     /**
      * {@inheritDoc}
      *
      * @param e Action performed
      */
     @Override
     public void actionPerformed(final ActionEvent e) {
         final JComponent setting = ((ImageButton<JComponent>)
                 e.getSource()).getObject();
         delOption(setting);
         parent.addAddableOption(setting);
     }
 
 }
