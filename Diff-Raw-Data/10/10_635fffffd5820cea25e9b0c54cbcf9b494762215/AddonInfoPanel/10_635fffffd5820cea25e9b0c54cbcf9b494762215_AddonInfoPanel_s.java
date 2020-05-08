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
 
 package com.dmdirc.addons.ui_swing.components.addonpanel;
 
 import com.dmdirc.addons.ui_swing.components.text.TextLabel;
 import com.dmdirc.util.ListenerList;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.JCheckBox;
 import javax.swing.JPanel;
 
 import net.miginfocom.swing.MigLayout;
 
 /**
  * Simple panel describing an addon toggle.
  */
 public class AddonInfoPanel extends JPanel implements ActionListener,
         AddonToggleListener {
 
     /** Java serialisation version UID. */
     private static final long serialVersionUID = 1L;
     /** Addon toggle. */
     private AddonToggle addonToggle;
     /** Plugin description text label. */
     private final TextLabel description;
     /** Status toggle button. */
     private final JCheckBox status;
     /** Label to describe status checkbox. */
     private final TextLabel statusLabel;
     /** Uninstall button. */
     //private final JButton uninstall;
     /** Should we check this addon for updates? */
     private final JCheckBox update;
     /** Label to describe update checkbox. */
     //private final TextLabel updateLabel;
     /** Listener list. */
     private final ListenerList listeners;
 
     /**
      * Creates a new addon info panel.
      */
     public AddonInfoPanel() {
         description = new TextLabel();
         status = new JCheckBox();
         statusLabel = new TextLabel("Enable this addon", false);
         //uninstall = new JButton("Uninstall");
         update = new JCheckBox();
         //updateLabel = new TextLabel("Check for updates for this addon", false);
         listeners = new ListenerList();
 
         layoutComponents();
         update.addActionListener(this);
         status.addActionListener(this);
     }
 
     /**
      * Lays out the components in this panel.
      */
     private void layoutComponents() {
         setLayout(new MigLayout("ins 0, fill", "[65%!][]", ""));
 
         add(description, "grow, spany 3");
         add(status, "aligny top");
         add(statusLabel, "grow, push, wrap");
         //add(update, "aligny top");
         //add(updateLabel, "grow, push, wrap, gapbottom rel");
         //add(uninstall, "grow, spanx 2, wrap");
     }
 
     /**
      * Sets the addon toggle this panel should display information about.
      *
      * @param addonToggle Addon toggle to display, or null
      */
     public void setAddonToggle(final AddonToggle addonToggle) {
         if (this.addonToggle != null) {
             this.addonToggle.removeListener(this);
         }
         addonToggle.addListener(this);
         this.addonToggle = addonToggle;
         if (addonToggle == null) {
             description.setText("");
             status.setEnabled(false);
         } else {
             status.setEnabled(true);
             description.setText("<b>" + addonToggle.getName() + "</b> "
                     + addonToggle.getVersion() + " by "
                     + addonToggle.getAuthor()
                     + "<br><br>" + addonToggle.getDescription());
             status.setSelected(addonToggle.getState());
             update.setSelected(addonToggle.getUpdateState());
         }
     }
 
     /**
      * Adds an addon toggle listener to this panel, this proxies whatever
      * addon listener is being displayed.
      *
      * @param listener Listener to add
      */
     public void addListener(final AddonToggleListener listener) {
         listeners.add(AddonToggleListener.class, listener);
     }
 
     /**
      * Removes an addon toggle listener from this panel.
      *
      * @param listener Listener to remove
      */
     public void removeListener(final AddonToggleListener listener) {
         listeners.remove(AddonToggleListener.class, listener);
     }
 
     /** {@inheritDoc} */
     @Override
     public void addonToggled() {
         listeners.getCallable(AddonToggleListener.class).addonToggled();
     }
 
     /**
      * {@inheritDoc}
      *
      * @param e Action event
      */
     @Override
     public void actionPerformed(final ActionEvent e) {
         if (addonToggle == null) {
             return;
         }
         if (e.getSource() == update) {
             addonToggle.setUpdateState(update.isSelected());
         } else if (e.getSource() == status) {
             addonToggle.setState(status.isSelected());
         }
     }
 
 }
