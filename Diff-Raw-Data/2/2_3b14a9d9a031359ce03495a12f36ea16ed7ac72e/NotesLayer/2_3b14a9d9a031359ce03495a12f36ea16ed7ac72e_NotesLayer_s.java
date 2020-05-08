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
 import java.util.List;
 
 import javax.swing.Action;
 import javax.swing.Icon;
 import javax.swing.ImageIcon;
 import javax.swing.JToolTip;
 
 import org.apache.commons.lang.StringEscapeUtils;
 
 import org.openstreetmap.josm.Main;
 import org.openstreetmap.josm.actions.RenameLayerAction;
 import org.openstreetmap.josm.data.Bounds;
 import org.openstreetmap.josm.data.osm.visitor.BoundingXYVisitor;
 import org.openstreetmap.josm.gui.MapView;
 import org.openstreetmap.josm.gui.dialogs.LayerListDialog;
 import org.openstreetmap.josm.gui.dialogs.LayerListPopup;
 import org.openstreetmap.josm.gui.layer.Layer;
 import org.openstreetmap.josm.plugins.notes.Note.Comment;
 import org.openstreetmap.josm.plugins.notes.gui.NotesDialog;
 import org.openstreetmap.josm.tools.ColorHelper;
 
 public class NotesLayer extends Layer implements MouseListener {
 
     private List<Note> data;
     private List<Note> selection = new ArrayList<Note>(1);
 
     private JToolTip tooltip = new JToolTip();
 
     private static ImageIcon iconError = NotesPlugin.loadIcon("open_note16.png");
     private static ImageIcon iconValid = NotesPlugin.loadIcon("closed_note16.png");
 
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
         return tr("Displays OpenStreetMap notes");
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
 
             g.drawImage(icon.getImage(), p.x - (width / 2), p.y - height, new ImageObserver() {
                 public boolean imageUpdate(Image img, int infoflags, int x, int y, int width, int height) {
                     return false;
                 }
             });
         }
 
         if(selection == null)
             return;
 
         // This loop renders the selection border and tooltips so they get drawn
         // on top of the bug icons
         for (Note note : selection) {
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
             g.drawRect(p.x-(width/2), p.y-height, width-1, height-1);
 
             // draw description
             StringBuilder sb = new StringBuilder("<html>");
             //sb.append(note.getFirstComment().getText());
             List<Comment> comments = note.getComments();
             String sep = "";
             for(Comment comment: comments) {
             	String commentText = comment.getText();
             	//closing a note creates an empty comment that we don't want to show
             	if(commentText != null && commentText.trim().length() > 0) {
 	            	sb.append(sep);
 	            	String userName = comment.getUser().getName();
 	            	if(userName == null || userName.trim().length() == 0) {
 	            		userName = "&lt;Anonymous&gt;";
 	            	}
 	            	sb.append(userName);
 	            	sb.append(":<br/>");
 	            	String htmlText = StringEscapeUtils.escapeHtml(comment.getText());
 	            	htmlText = htmlText.replaceAll("\n", "<br/>");
 	            	sb.append(htmlText);
             	}
            	sep = "<hr>";
             }
             sb.append("</html>");
 
             // draw description as a tooltip
             tooltip.setTipText(sb.toString());
 
             int tx = p.x + (width / 2) + 5;
             int ty = p.y - height -1;
             g.translate(tx, ty);
 
             // This limits the width of the tooltip to 1/2 of the drawing
             // area, which makes longer tooltips actually readable (they
             // would disappear if scrolled too much to the right)
 
             // Need to do this twice as otherwise getPreferredSize doesn't take
             // the reduced width into account
             for(int x = 0; x < 2; x++) {
                 Dimension d = tooltip.getUI().getPreferredSize(tooltip);
                 d.width = Math.min(d.width, (mv.getWidth()*1/2));
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
         return NotesPlugin.loadIcon("open_note16.png");
     }
 
     private Note getNearestNode(Point p) {
         double snapDistance = 10;
         double minDistanceSq = Double.MAX_VALUE;
         Note minPrimitive = null;
         for (Note note : data) {
             Point sp = Main.map.mapView.getPoint(note.getLatLon());
             //move the hotpoint location up to the center of the displayed icon where people are likely to click
             sp.setLocation(sp.getX(), sp.getY() - iconError.getIconHeight()/2);
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
             Note n = getNearestNode(e.getPoint());
             if(n != null && data.contains(n)) {
                 selection.add(n);
             } else {
                 selection = new ArrayList<Note>();
             }
             dialog.setSelectedNote(n);
             Main.map.mapView.repaint();
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
             Note n = getNearestNode(e.getPoint());
             if(n != null && data.contains(n)) {
                 System.out.println("Popup goes here?");
                 //PopupFactory.createPopup(n, dialog).show(e.getComponent(), e.getX(), e.getY());
             }
         }
     }
 
     public void mouseEntered(MouseEvent e) {}
 
     public void mouseExited(MouseEvent e) {}
 
     public List<Note> getDataSet() {
         return data;
     }
 
     public void replaceSelection(Note selected) {
         selection.clear();
         selection.add(selected);
         Main.map.mapView.repaint();
     }
 }
