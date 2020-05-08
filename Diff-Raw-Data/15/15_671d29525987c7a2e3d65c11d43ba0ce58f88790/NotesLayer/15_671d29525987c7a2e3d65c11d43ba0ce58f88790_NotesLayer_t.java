 /* Copyright (c) 2013, Ian Dees
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  *
  * 1. Redistributions of source code must retain the above copyright notice,
  *    this list of conditions and the following disclaimer.
  * 2. Redistributions in binary form must reproduce the above copyright notice,
  *    this list of conditions and the following disclaimer in the documentation
  *    and/or other materials provided with the distribution.
  * 3. Neither the name of the project nor the names of its
  *    contributors may be used to endorse or promote products derived from this
  *    software without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
  * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
  * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
  * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
  * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
  * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
  * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
  * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
  * POSSIBILITY OF SUCH DAMAGE.
  */
 package org.openstreetmap.josm.plugins.notes;
 
 import static org.openstreetmap.josm.tools.I18n.tr;
 
 import java.awt.Dimension;
 import java.awt.Graphics2D;
 import java.awt.Image;
 import java.awt.Point;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.image.ImageObserver;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
 import javax.swing.Action;
 import javax.swing.Icon;
 import javax.swing.ImageIcon;
 import javax.swing.JToolTip;
 
 import org.openstreetmap.josm.Main;
 import org.openstreetmap.josm.actions.RenameLayerAction;
 import org.openstreetmap.josm.data.Bounds;
 import org.openstreetmap.josm.data.osm.visitor.BoundingXYVisitor;
 import org.openstreetmap.josm.gui.MapView;
 import org.openstreetmap.josm.gui.dialogs.LayerListDialog;
 import org.openstreetmap.josm.gui.dialogs.LayerListPopup;
 import org.openstreetmap.josm.gui.layer.Layer;
 import org.openstreetmap.josm.plugins.notes.gui.NotesDialog;
 import org.openstreetmap.josm.tools.ColorHelper;
 
 public class NotesLayer extends Layer implements MouseListener {
 
     private List<Note> data;
 
     private Collection<Note> selection;
 
     private JToolTip tooltip = new JToolTip();
 
     private static ImageIcon iconError = NotesPlugin.loadIcon("icon_error16.png");
     private static ImageIcon iconValid = NotesPlugin.loadIcon("icon_valid16.png");
 
     private NotesDialog dialog;
 
     public NotesLayer(List<Note> dataSet, String name, NotesDialog dialog) {
         super(name);
         this.data = dataSet;
         this.dialog = dialog;
 
         // if the map layer has been closed, while we are requesting the osb db,
         // the mapframe is null, so we check that, before installing the mouse listener
         if(Main.map != null && Main.map.mapView != null) {
             Main.map.mapView.addMouseListener(this);
         }
     }
 
     @Override
     public Object getInfoComponent() {
         return getToolTipText();
     }
 
     @Override
     public Action[] getMenuEntries() {
         return new Action[]{
                 LayerListDialog.getInstance().createShowHideLayerAction(),
                 LayerListDialog.getInstance().createDeleteLayerAction(),
                 SeparatorLayerAction.INSTANCE,
                 new RenameLayerAction(null, this),
                 SeparatorLayerAction.INSTANCE,
                 new LayerListPopup.InfoAction(this)};
     }
 
     @Override
     public String getToolTipText() {
         return tr("Displays OpenStreetBugs issues");
     }
 
     @Override
     public boolean isMergable(Layer other) {
         return false;
     }
 
     @Override
     public void mergeFrom(Layer from) {}
 
     @Override
     public void paint(Graphics2D g, MapView mv, Bounds bounds) {
         // This loop renders all the bug icons
         for (Note note : data) {
             // don't paint deleted nodes
 
             Point p = mv.getPoint(note.getLatLon());
 
             ImageIcon icon = null;
             switch(note.getState()) {
                 case closed:
                    icon = iconValid;
                     break;
                 case open:
                    icon = iconError;
                     break;
             }
             int width = icon.getIconWidth();
             int height = icon.getIconHeight();
 
             g.drawImage(icon.getImage(), p.x - (width / 2), p.y - (height / 2), new ImageObserver() {
                 public boolean imageUpdate(Image img, int infoflags, int x, int y, int width, int height) {
                     return false;
                 }
             });
         }
 
         if(selection == null)
             return;
 
         // This loop renders the selection border and tooltips so they get drawn
         // on top of the bug icons
         for (Note note : data) {
             if(!selection.contains(note))
                 continue;
 
             // draw selection border
             Point p = mv.getPoint(note.getLatLon());
 
             ImageIcon icon = null;
             switch(note.getState()) {
                 case closed:
                     icon = iconError;
                     break;
                 case open:
                     icon = iconValid;
                     break;
             }
             int width = icon.getIconWidth();
             int height = icon.getIconHeight();
 
             g.setColor(ColorHelper.html2color(Main.pref.get("color.selected")));
             g.drawRect(p.x-(width/2), p.y-(height/2), width-1, height-1);
 
             // draw description
             String desc = note.getFirstComment().getText();
             if(desc == null)
                 continue;
 
             // format with html
             StringBuilder sb = new StringBuilder("<html>");
             sb.append(desc.replaceAll("<hr />", "<hr>"));
             sb.append("</html>");
             desc = sb.toString();
 
             // draw description as a tooltip
             tooltip.setTipText(desc);
 
             int tx = p.x + (width / 2) + 5;
             int ty = (p.y - height / 2) -1;
             g.translate(tx, ty);
 
             // This limits the width of the tooltip to 2/3 of the drawing
             // area, which makes longer tooltips actually readable (they
             // would disappear if scrolled too much to the right)
 
             // Need to do this twice as otherwise getPreferredSize doesn't take
             // the reduced width into account
             for(int x = 0; x < 2; x++) {
                 Dimension d = tooltip.getUI().getPreferredSize(tooltip);
                 d.width = Math.min(d.width, (mv.getWidth()*2/3));
                 tooltip.setSize(d);
                 tooltip.paint(g);
             }
 
             g.translate(-tx, -ty);
         }
     }
 
     @Override
     public void visitBoundingBox(BoundingXYVisitor v) {}
 
     @Override
     public Icon getIcon() {
         return NotesPlugin.loadIcon("icon_error16.png");
     }
 
     private Note getNearestNode(Point p) {
         double snapDistance = 10;
         double minDistanceSq = Double.MAX_VALUE;
         Note minPrimitive = null;
         for (Note note : data) {
             Point sp = Main.map.mapView.getPoint(note.getLatLon());
             double dist = p.distanceSq(sp);
             if (minDistanceSq > dist && p.distance(sp) < snapDistance) {
                 minDistanceSq = p.distanceSq(sp);
                 minPrimitive = note;
             }
             // prefer already selected node when multiple nodes on one point
             else if(minDistanceSq == dist && selection.contains(note) && !selection.contains(minPrimitive))
             {
                 minPrimitive = note;
             }
         }
         return minPrimitive;
     }
 
     public void mouseClicked(MouseEvent e) {
         if(e.getButton() == MouseEvent.BUTTON1) {
             if(Main.map.mapView.getActiveLayer() == this) {
                 Note n = getNearestNode(e.getPoint());
                 if(n != null && data.contains(n)) {
                     List<Note> selected = new ArrayList<Note>();
                     selected.add(n);
                     selection.addAll(selected);
                 } else {
                     selection = new ArrayList<Note>();
                 }
                 //fireSelectionChanged();
             }
         }
     }
 
     public void mousePressed(MouseEvent e) {
         mayTriggerPopup(e);
     }
 
     public void mouseReleased(MouseEvent e) {
         mayTriggerPopup(e);
     }
 
     private void mayTriggerPopup(MouseEvent e) {
         if(e.isPopupTrigger()) {
             if(Main.map.mapView.getActiveLayer() == this) {
                 Note n = getNearestNode(e.getPoint());
                 if(n != null && data.contains(n)) {
                     System.out.println("Popup goes here?");
                     //PopupFactory.createPopup(n, dialog).show(e.getComponent(), e.getX(), e.getY());
                 }
             }
         }
     }
 
     public void mouseEntered(MouseEvent e) {}
 
     public void mouseExited(MouseEvent e) {}
 
     public List<Note> getDataSet() {
         return data;
     }
 }
