 /*
  * Copyright (c) 2006-2011 DMDirc Developers
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
 import com.dmdirc.ui.IconManager;
 
 import java.awt.Color;
 import java.awt.Font;
 
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JSeparator;
 import javax.swing.UIManager;
 import javax.swing.text.StyleConstants;
 
 import net.miginfocom.swing.MigLayout;
 
 /**
  * Renders an addon for display in the plugin panel.
  */
 public class AddonCell extends JPanel implements AddonToggleListener {
 
     /**
      * A version number for this class. It should be changed whenever the class
      * structure is changed (or anything else that would prevent serialized
      * objects being unserialized with the new class).
      */
     private static final long serialVersionUID = 1;
     /** Addon toggle object. */
     private final AddonToggle info;
     /** Name of the addon. */
     private final TextLabel name;
     /** Status label. */
     private final TextLabel status;
     /** Addon icon. */
     private final JLabel icon;
     /** Icon manager to retrieve icons from. */
     private final IconManager iconManager;
 
     /**
      * Creates a new addon cell representing the specified addon info.
      *
      * @param info PluginInfoToggle or ThemeToggle
      * @param iconManager Icon manager to retrieve icons from
      */
     public AddonCell(final AddonToggle info, final IconManager iconManager) {
         super();
 
         this.iconManager = iconManager;
         this.info = info;
 
         name = new TextLabel(false);
         status = new TextLabel(false);
         status.setAlignment(StyleConstants.ALIGN_RIGHT);
         icon = new JLabel();
 
         info.addListener(this);
         init();
     }
 
     /**
      * Initialises the addon cell.
      */
     private void init() {
         setLayout(new MigLayout("fill, ins 0"));
         Color foreground = UIManager.getColor("Table.foreground");
         if (!info.getState()) {
             foreground = foreground.brighter().brighter().brighter();
         }
         icon.setIcon(iconManager.getIcon("addon"));
         name.setText(info.getName());
         status.setText(info.getState() ? "Enabled" : "Disabled");
         name.setForeground(foreground);
         name.setFont(name.getFont().deriveFont(Font.BOLD));
         status.setForeground(foreground);
 
         final int initialPadding;
        if (info.getPluginInfo().getMetaData().getParents().length == 0) {
             initialPadding = 20;
         } else {
             initialPadding = 30;
         }
         add(icon, "gaptop rel, gapbottom rel, gapleft rel, "
                     + ", wmax " + initialPadding + ", right");
         add(name, "gaptop rel, gapbottom rel, " + "wmin 50% - "
                 + initialPadding + ", wmax 50% - " + initialPadding);
         add(status, "gaptop rel, gapbottom rel, gapright rel, " + "wmin 50% - "
                 + initialPadding + ", wmax 50% - " + initialPadding);
         add(new JSeparator(), "newline, spanx, growx, pushx");
     }
 
     /**
      * Returns the addon toggle object associated with this cell.
      *
      * @return Addon toggle
      */
     public Object getObject() {
         return info;
     }
 
     /**
      * Is this addon enabled or disabled?
      *
      * @return true iif enabled
      */
     public boolean isToggled() {
         return info.getState();
     }
 
     /** {@inheritDoc} */
     @Override
     public void setForeground(final Color fg) {
         if (name != null) {
             name.setForeground(fg);
         }
         if (status != null) {
             status.setForeground(fg);
         }
     }
 
     /** {@inheritDoc} */
     @Override
     public void addonToggled() {
         status.setText(info.getState() ? "Enabled" : "Disabled");
     }
 }
