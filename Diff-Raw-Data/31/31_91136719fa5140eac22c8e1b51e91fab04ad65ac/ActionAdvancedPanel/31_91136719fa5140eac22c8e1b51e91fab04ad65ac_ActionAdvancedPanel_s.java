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
 
 package com.dmdirc.addons.ui_swing.dialogs.actioneditor;
 
 import javax.swing.BorderFactory;
 import javax.swing.JCheckBox;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 import javax.swing.UIManager;
 
 import net.miginfocom.swing.MigLayout;
 
 /**
  * Advanced options panel, show concurrency groups and enabled states.
  */
 public class ActionAdvancedPanel extends JPanel {
 
     /**
      * A version number for this class. It should be changed whenever the class
      * structure is changed (or anything else that would prevent serialized
      * objects being unserialized with the new class).
      */
     private static final long serialVersionUID = 1;
     private JLabel enabledLabel;
     private JCheckBox enabled;
     private JLabel groupLabel;
     private JTextField group;
     private JLabel stopLabel;
     private JCheckBox stop;
 
     /**
      * Creates a new panel to configure advanced options for actions.
      */
     public ActionAdvancedPanel() {
         initComponents();
         layoutComponents();
     }
 
     /** Initialises the components. */
     private void initComponents() {
         setBorder(BorderFactory.createTitledBorder(UIManager.getBorder(
                 "TitledBorder.border"), "Advanced"));
         enabled = new JCheckBox("", true);
         group = new JTextField();
         stop = new JCheckBox("", false);
         enabledLabel = new JLabel("Enabled: ");
         groupLabel = new JLabel("Concurrency group: ");
         stopLabel = new JLabel("Stop default behaviour: ");
     }
 
     private void layoutComponents() {
         setLayout(new MigLayout("fillx, pack"));
         add(enabledLabel, "");
         add(enabled, "growx");
         add(stopLabel, "");
         add(stop, "growx, pushx, wrap");
         add(groupLabel, "");
         add(group, "growx, pushx, spanx");
     }
 
     /**
      * Gets the concurrency group for this action.
      *
      * @return Current concurrency group
      */
     public String getConcurrencyGroup() {
         return group.getText();
     }
 
     /**
      * Is the action currently enabled?
      *
      * @return true iif enabled
      */
     public boolean isActionEnabled() {
         return enabled.isSelected();
     }
 
     /**
      * Sets whether this action should be enabled.
      *
      * @param actionEnabled true to enable, false to disable
      */
     public void setActionEnabled(final boolean actionEnabled) {
         enabled.setSelected(actionEnabled);
     }
 
     /**
      * Sets this actions concurrency group.
      *
      * @param actionGroup New concurrency group
      */
     public void setConcurrencyGroup(final String actionGroup) {
         group.setText(actionGroup);
     }
 
     /**
      * Does the action currently stop the default behaviour?
      *
      * @return true iif stopped
      */
     public boolean isActionStopped() {
         return stop.isSelected();
     }
 
     /**
      * Sets whether this action should be enabled.
      *
      * @param actionStopped true to enable, false to disable
      */
     public void setActionStopped(final boolean actionStopped) {
         stop.setSelected(actionStopped);
     }
 }
