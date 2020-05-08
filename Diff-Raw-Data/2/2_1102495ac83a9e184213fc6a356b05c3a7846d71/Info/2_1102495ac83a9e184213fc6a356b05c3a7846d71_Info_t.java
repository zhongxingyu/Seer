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
 
 import com.dmdirc.addons.ui_swing.UIUtilities;
 import com.dmdirc.addons.ui_swing.components.text.HTMLLabel;
 import com.dmdirc.serverlists.ServerGroupItem;
 import com.dmdirc.ui.core.util.URLHandler;
 
 import java.net.URI;
 import java.util.Map.Entry;
 
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.event.HyperlinkEvent;
 import javax.swing.event.HyperlinkEvent.EventType;
 import javax.swing.event.HyperlinkListener;
 
 import net.miginfocom.swing.MigLayout;
 
 /**
  * Panel for showing server group item information.
  */
 public class Info extends JPanel implements HyperlinkListener,
         ServerListListener {
 
     /**
      * A version number for this class. It should be changed whenever the class
      * structure is changed (or anything else that would prevent serialized
      * objects being unserialized with the new class).
      */
     private static final long serialVersionUID = 2;
     /** Info pane. */
     private final HTMLLabel infoLabel;
     /** Link label. */
     private final HTMLLabel linkLabel;
     /** Server list model. */
     private final ServerListModel model;
     /** Info scroll panel. */
     private final JScrollPane sp;
 
     /**
      * Creates a new info panel.
      *
      * @param model Model to pull information from
      */
     public Info(final ServerListModel model) {
         super();
 
         this.model = model;
 
         infoLabel = new HTMLLabel();
         linkLabel = new HTMLLabel();
         sp = new JScrollPane(infoLabel);
 
         setLayout(new MigLayout("fill, ins 0"));
         add(sp, "grow, push");
         add(linkLabel, "grow");
 
         addListeners();
         serverGroupChanged(model.getSelectedItem());
     }
 
     /**
      * Adds required listeners.
      */
     private void addListeners() {
         linkLabel.addHyperlinkListener(this);
         model.addServerListListener(this);
     }
 
     /** {@inheritDoc} */
     @Override
     public void hyperlinkUpdate(final HyperlinkEvent e) {
         if (e.getEventType() == EventType.ACTIVATED) {
             URLHandler.getURLHander().launchApp(e.getURL());
         }
     }
 
     /** {@inheritDoc} */
     @Override
     public void serverGroupChanged(final ServerGroupItem item) {
         setVisible(false);
         infoLabel.setVisible(false);
         linkLabel.setVisible(false);
         if (item == null) {
             infoLabel.setText("");
             linkLabel.setText("");
         } else {
             final StringBuilder sb = new StringBuilder();
             sb.append("<html><b>");
            sb.append(item.getPath());
             sb.append("</b><br>");
             if (item.getGroup() == null) {
                 sb.append("");
             } else {
                 sb.append(item.getGroup().getDescription());
             }
             sb.append("</html>");
             infoLabel.setText(sb.toString());
             sb.setLength(0);
             for (Entry<String, URI> entry : item.getGroup().getLinks()
                     .entrySet()) {
                 if (sb.length() != 0) {
                     sb.append("<br>");
                 }
                 sb.append("<a href=\"");
                 sb.append(entry.getValue().toString());
                 sb.append("\">");
                 sb.append(entry.getKey());
                 sb.append("</a>");
             }
             linkLabel.setText(sb.toString());
         }
         UIUtilities.resetScrollPane(sp);
         infoLabel.setVisible(true);
         linkLabel.setVisible(true);
         setVisible(true);
     }
 
     /** {@inheritDoc} */
     @Override
     public void dialogClosed(final boolean save) {
         //Ignore
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
 }
