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
 
 package com.dmdirc.addons.ui_swing.components.addonpanel;
 
 import com.dmdirc.actions.ActionManager;
 import com.dmdirc.actions.CoreActionType;
 import com.dmdirc.actions.interfaces.ActionType;
 import com.dmdirc.addons.ui_swing.SwingController;
 import com.dmdirc.addons.ui_swing.UIUtilities;
 import com.dmdirc.interfaces.ActionListener;
 import com.dmdirc.plugins.PluginInfo;
 import com.dmdirc.plugins.PluginManager;
 
 import java.awt.Window;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import javax.swing.JTable;
 import javax.swing.table.DefaultTableModel;
 
 /**
  * Lists known plugins, enabling the end user to enable/disable these as well
  * as download new ones.
  */
 public class PluginPanel extends AddonPanel implements ActionListener {
 
     /**
      * A version number for this class. It should be changed whenever the class
      * structure is changed (or anything else that would prevent serialized
      * objects being unserialized with the new class).
      */
     private static final long serialVersionUID = 1;
 
     /**
      * Creates a new instance of PluginPanel.
      *
      * @param parentWindow Parent window
      * @param controller Swing Controller
      */
     public PluginPanel(final Window parentWindow,
             final SwingController controller) {
         super(parentWindow, controller);
 
         ActionManager.getActionManager().registerListener(this,
                 CoreActionType.PLUGIN_REFRESH);
        PluginManager.getPluginManager().getPossiblePluginInfos(true);
     }
 
     /** {@inheritDoc} */
     @Override
     protected JTable populateList(final JTable table) {
         final List<PluginInfo> list = new ArrayList<PluginInfo>();
         list.addAll(PluginManager.getPluginManager().getPluginInfos());
         Collections.sort(list);
 
         UIUtilities.invokeLater(new Runnable() {
 
             /** {@inheritDoc} */
             @Override
             public void run() {
                 ((DefaultTableModel) table.getModel()).setNumRows(0);
                 for (PluginInfo plugin : list) {
                     ((DefaultTableModel) table.getModel()).addRow(
                             new AddonCell[]{
                         new AddonCell(new AddonToggle(plugin, null)), });
                 }
 
                 if (((DefaultTableModel) table.getModel()).getRowCount()
                         > 0) {
                     toggleButton.setEnabled(true);
                 }
 
                 table.repaint();
             }
         });
         return table;
     }
 
     /** {@inheritDoc} */
     @Override
     public void processEvent(final ActionType type, final StringBuffer format,
             final Object... arguments) {
         populateList(addonList);
     }
 
     /** {@inheritDoc} */
     @Override
     protected String getTypeName() {
         return "plugins";
     }
 }
