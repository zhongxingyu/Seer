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
 
 import com.dmdirc.addons.ui_swing.components.performpanel.PerformPanel;
 import com.dmdirc.serverlists.ServerGroupItem;
 
 import javax.swing.BorderFactory;
 import javax.swing.JPanel;
 import javax.swing.UIManager;
import javax.swing.border.Border;
 
 import net.miginfocom.swing.MigLayout;
 
 /**
  * Panel to show and edit performs for a server group.
  */
 public class Perform extends JPanel implements ServerListListener {
 
     /**
      * A version number for this class. It should be changed whenever the class
      * structure is changed (or anything else that would prevent serialized
      * objects being unserialized with the new class).
      */
     private static final long serialVersionUID = 2;
     /** Perform panel. */
     private final PerformPanel performPanel;
     /** Server list model. */
     private final ServerListModel model;
    /** Platform border. */
    private final Border border;
 
     /**
      * Creates a new perform panel backed by the specified model.
      *
      * @param model Backing model
      */
     public Perform(final ServerListModel model) {
         super();
 
         this.model = model;
         performPanel = new PerformPanel();
 
         addListeners();
         if (model.getSelectedItemPerformDescription() != null) {
             performPanel.switchPerform(model
                     .getSelectedItemPerformDescription());
         }
 
        border = UIManager.getBorder("TitledBorder.border");
        setBorder(BorderFactory.createTitledBorder(border, "Network perform"));
         setLayout(new MigLayout("fill"));
         add(performPanel, "grow");
     }
 
     /**
      * Adds required listeners.
      */
     private void addListeners() {
         model.addServerListListener(this);
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
 
     /** {@inheritDoc} */
     @Override
     public void serverGroupChanged(final ServerGroupItem item) {
        if (item.getGroup() == item) {
            setBorder(BorderFactory.createTitledBorder(border,
                    "Network perform"));
        } else {
            setBorder(BorderFactory.createTitledBorder(border,
                    "Server perform"));
        }
         performPanel.switchPerform(model.getSelectedItemPerformDescription());
     }
 
     /** {@inheritDoc} */
     @Override
     public void dialogClosed(final boolean save) {
         if (save) {
             performPanel.savePerform();
         }
     }
 }
