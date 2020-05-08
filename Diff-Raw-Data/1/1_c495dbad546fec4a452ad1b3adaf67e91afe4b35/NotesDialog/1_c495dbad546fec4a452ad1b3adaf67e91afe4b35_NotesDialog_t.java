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
 package org.openstreetmap.josm.plugins.notes.gui;
 
 import static org.openstreetmap.josm.tools.I18n.tr;
 
 import java.awt.BorderLayout;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 
 import javax.swing.Action;
 import javax.swing.DefaultListModel;
 import javax.swing.JButton;
 import javax.swing.JList;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTabbedPane;
 import javax.swing.JToggleButton;
 import javax.swing.ListSelectionModel;
 import javax.swing.SwingConstants;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 
 import org.openstreetmap.josm.Main;
 import org.openstreetmap.josm.gui.MapView;
 import org.openstreetmap.josm.gui.MapView.LayerChangeListener;
 import org.openstreetmap.josm.gui.dialogs.ToggleDialog;
 import org.openstreetmap.josm.gui.layer.Layer;
 import org.openstreetmap.josm.plugins.notes.ConfigKeys;
 import org.openstreetmap.josm.plugins.notes.Note;
 import org.openstreetmap.josm.plugins.notes.NotesObserver;
 import org.openstreetmap.josm.plugins.notes.NotesPlugin;
 import org.openstreetmap.josm.plugins.notes.gui.action.ActionQueue;
 import org.openstreetmap.josm.plugins.notes.gui.action.AddCommentAction;
 import org.openstreetmap.josm.plugins.notes.gui.action.CloseNoteAction;
 import org.openstreetmap.josm.plugins.notes.gui.action.NotesAction;
 import org.openstreetmap.josm.plugins.notes.gui.action.NotesActionObserver;
 import org.openstreetmap.josm.plugins.notes.gui.action.PointToNewNoteAction;
 import org.openstreetmap.josm.plugins.notes.gui.action.PopupFactory;
 import org.openstreetmap.josm.plugins.notes.gui.action.ReopenAction;
 import org.openstreetmap.josm.plugins.notes.gui.action.ToggleConnectionModeAction;
 import org.openstreetmap.josm.tools.OsmUrlToBounds;
 import org.openstreetmap.josm.tools.Shortcut;
 
 public class NotesDialog extends ToggleDialog implements NotesObserver, ListSelectionListener, LayerChangeListener, MouseListener, NotesActionObserver {
 
     private static final long serialVersionUID = 1L;
     private JPanel bugListPanel, queuePanel;
     private DefaultListModel bugListModel;
     private JList bugList;
     private JList queueList;
     private NotesPlugin notesPlugin;
     private boolean fireSelectionChanged = true;
     private JButton refresh;
     private JButton addComment;
     private JButton closeIssue;
     private JButton reopenNote;
     private JButton processQueue = new JButton(tr("Process queue"));
     private JToggleButton newIssue = new JToggleButton();
     private JToggleButton toggleConnectionMode;
     private JTabbedPane tabbedPane = new JTabbedPane();
     private boolean queuePanelVisible = false;
     private final ActionQueue actionQueue = new ActionQueue();
 
     private boolean buttonLabels = Main.pref.getBoolean(ConfigKeys.NOTES_BUTTON_LABELS);
 
     public NotesDialog(final NotesPlugin plugin) {
         super(tr("OpenStreetMap Notes"), "note_icon24.png",
                 tr("Opens the OpenStreetMap Notes window and activates the automatic download"), Shortcut.registerShortcut(
                         "view:osmnotes", tr("Toggle: {0}", tr("Open OpenStreetMap Notes")), KeyEvent.VK_B,
                         Shortcut.ALT_SHIFT), 150);
 
         notesPlugin = plugin;
         bugListPanel = new JPanel(new BorderLayout());
         bugListPanel.setName(tr("Bug list"));
         add(bugListPanel, BorderLayout.CENTER);
 
         bugListModel = new DefaultListModel();
         bugList = new JList(bugListModel);
         bugList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
         bugList.addListSelectionListener(this);
         bugList.addMouseListener(this);
         bugList.setCellRenderer(new NotesBugListCellRenderer());
         bugListPanel.add(new JScrollPane(bugList), BorderLayout.CENTER);
 
         // create dialog buttons
         GridLayout layout = buttonLabels ? new GridLayout(3, 2) : new GridLayout(1, 6);
         JPanel buttonPanel = new JPanel(layout);
         refresh = new JButton(tr("Refresh"));
         refresh.setToolTipText(tr("Refresh"));
         refresh.setIcon(NotesPlugin.loadIcon("view-refresh22.png"));
         refresh.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 int zoom = OsmUrlToBounds.getZoom(Main.map.mapView.getRealBounds());
                 // check zoom level
                 if (zoom > 15 || zoom < 9) {
                     JOptionPane.showMessageDialog(Main.parent,
                             tr("The visible area is either too small or too big to download data from OpenStreetMap Notes"),
                             tr("Warning"), JOptionPane.INFORMATION_MESSAGE);
                     return;
                 }
 
                 plugin.updateData();
             }
         });
         bugListPanel.add(buttonPanel, BorderLayout.SOUTH);
         Action toggleConnectionModeAction = new ToggleConnectionModeAction(this, notesPlugin);
         toggleConnectionMode = new JToggleButton(toggleConnectionModeAction);
         toggleConnectionMode.setToolTipText(ToggleConnectionModeAction.MSG_OFFLINE);
         boolean offline = Main.pref.getBoolean(ConfigKeys.NOTES_API_OFFLINE);
         toggleConnectionMode.setIcon(NotesPlugin.loadIcon("online22.png"));
         toggleConnectionMode.setSelectedIcon(NotesPlugin.loadIcon("offline22.png"));
         if(offline) {
             // inverse the current value and then do a click, so that
             // we are offline and the gui represents the offline state, too
             Main.pref.put(ConfigKeys.NOTES_API_OFFLINE, false);
             toggleConnectionMode.doClick();
         }
 
         AddCommentAction addCommentAction = new AddCommentAction(this);
         addComment = new JButton(addCommentAction);
         addComment.setEnabled(false);
         addComment.setToolTipText((String) addComment.getAction().getValue(Action.NAME));
         addComment.setIcon(NotesPlugin.loadIcon("add_comment22.png"));
         CloseNoteAction closeIssueAction = new CloseNoteAction(this);
         closeIssue = new JButton(closeIssueAction);
         closeIssue.setEnabled(false);
         closeIssue.setToolTipText((String) closeIssue.getAction().getValue(Action.NAME));
         closeIssue.setIcon(NotesPlugin.loadIcon("closed_note22.png"));
         PointToNewNoteAction nia = new PointToNewNoteAction(newIssue, notesPlugin);
         newIssue.setAction(nia);
         newIssue.setToolTipText((String) newIssue.getAction().getValue(Action.NAME));
         newIssue.setIcon(NotesPlugin.loadIcon("new_note22.png"));
         ReopenAction reopenAction = new ReopenAction(this);
         reopenNote = new JButton(reopenAction);
         reopenNote.setIcon(NotesPlugin.loadIcon("reopen_note22.png"));
         reopenNote.setToolTipText(reopenNote.getAction().getValue(Action.NAME).toString());
 
         buttonPanel.add(toggleConnectionMode);
         buttonPanel.add(refresh);
         buttonPanel.add(newIssue);
         buttonPanel.add(addComment);
         buttonPanel.add(closeIssue);
         buttonPanel.add(reopenNote);
 
         queuePanel = new JPanel(new BorderLayout());
         queuePanel.setName(tr("Queue"));
         queueList = new JList(getActionQueue());
         queueList.setCellRenderer(new NotesQueueListCellRenderer());
         queuePanel.add(new JScrollPane(queueList), BorderLayout.CENTER);
         queuePanel.add(processQueue, BorderLayout.SOUTH);
         processQueue.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 Main.pref.put(ConfigKeys.NOTES_API_OFFLINE, "false");
                 setConnectionMode(false);
                 try {
                     getActionQueue().processQueue();
 
                     // refresh, if the api is enabled
                     if(!Main.pref.getBoolean(ConfigKeys.NOTES_API_DISABLED)) {
                         plugin.updateData();
                     }
                 } catch (Exception e1) {
                     System.err.println("Couldn't process action queue");
                     e1.printStackTrace();
                 }
             }
         });
         tabbedPane.add(queuePanel);
 
         if (buttonLabels) {
             toggleConnectionMode.setHorizontalAlignment(SwingConstants.LEFT);
             refresh.setHorizontalAlignment(SwingConstants.LEFT);
             addComment.setHorizontalAlignment(SwingConstants.LEFT);
             closeIssue.setHorizontalAlignment(SwingConstants.LEFT);
             newIssue.setHorizontalAlignment(SwingConstants.LEFT);
            reopenNote.setHorizontalAlignment(SwingConstants.LEFT);
         } else {
             toggleConnectionMode.setText(null);
             refresh.setText(null);
             addComment.setText(null);
             closeIssue.setText(null);
             newIssue.setText(null);
         }
 
         addCommentAction.addActionObserver(this);
         closeIssueAction.addActionObserver(this);
         setConnectionMode(offline);
 
         MapView.addLayerChangeListener(this);
     }
 
     @Override
     public void destroy() {
         super.destroy();
         MapView.removeLayerChangeListener(this);
 
     }
 
     public synchronized void update(final Collection<Note> dataset) {
         // create a new list model
         bugListModel = new DefaultListModel();
         List<Note> sortedList = new ArrayList<Note>(dataset);
         Collections.sort(sortedList, new BugComparator());
         for (Note note : sortedList) {
             bugListModel.addElement(note);
         }
         bugList.setModel(bugListModel);
     }
 
     public void valueChanged(ListSelectionEvent e) {
         if (bugList.getSelectedValues().length == 0) {
             addComment.setEnabled(false);
             closeIssue.setEnabled(false);
             return;
         }
 
         List<Note> selected = new ArrayList<Note>();
         for (Object n : bugList.getSelectedValues()) {
         	Note note = (Note)n;
             selected.add(note);
 
             switch(note.getState()) {
             case closed:
                 addComment.setEnabled(false);
                 closeIssue.setEnabled(false);
             case open:
                 addComment.setEnabled(true);
                 closeIssue.setEnabled(true);
             }
 
             scrollToSelected(note);
         }
 
         // CurrentDataSet may be null if there is no normal, edible map
         // If so, a temporary DataSet is created because it's the simplest way
         // to fire all necessary events so OSB updates its popups.
         List<Note> ds = notesPlugin.getLayer().getDataSet();
         if (fireSelectionChanged) {
             if(ds == null)
                 ds = new ArrayList<Note>();
         }
     }
 
     private void scrollToSelected(Note node) {
         for (int i = 0; i < bugListModel.getSize(); i++) {
             Note current = (Note)bugListModel.get(i);
             if (current.getId()== node.getId()) {
                 bugList.scrollRectToVisible(bugList.getCellBounds(i, i));
                 bugList.setSelectedIndex(i);
                 return;
             }
         }
     }
 
     public void activeLayerChange(Layer oldLayer, Layer newLayer) {
     }
 
     public void layerAdded(Layer newLayer) {
         if (newLayer == notesPlugin.getLayer()) {
             update(notesPlugin.getDataSet());
             Main.map.mapView.moveLayer(newLayer, 0);
         }
     }
 
     public void layerRemoved(Layer oldLayer) {
         if (oldLayer == notesPlugin.getLayer()) {
             bugListModel.removeAllElements();
         }
     }
 
     public void zoomToNote(Note node) {
         Main.map.mapView.zoomTo(node.getLatLon());
     }
 
     public void mouseClicked(MouseEvent e) {
         if (e.getButton() == MouseEvent.BUTTON1) {
             Note selectedNote = getSelectedNote();
             if(selectedNote != null) {
                 notesPlugin.getLayer().replaceSelection(selectedNote);
                 if (e.getClickCount() == 2) {
                     zoomToNote(selectedNote);
                 }
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
         if (e.isPopupTrigger()) {
             int selectedRow = bugList.locationToIndex(e.getPoint());
             bugList.setSelectedIndex(selectedRow);
             Note selectedNote = getSelectedNote();
             if(selectedNote != null) {
                 PopupFactory.createPopup(selectedNote, this).show(e.getComponent(), e.getX(), e.getY());
             }
         }
     }
 
     public void mouseEntered(MouseEvent e) {
     }
 
     public void mouseExited(MouseEvent e) {
     }
 
     public void actionPerformed(NotesAction action) {
         if (action instanceof AddCommentAction || action instanceof CloseNoteAction) {
             update(notesPlugin.getDataSet());
         }
     }
 
     private static class BugComparator implements Comparator<Note> {
 
         public int compare(Note o1, Note o2) {
             Note.State state1 = o1.getState();
             Note.State state2 = o2.getState();
             if (state1.equals(state2)) {
                 return o1.getFirstComment().getText().compareTo(o2.getFirstComment().getText());
             }
             return state1.compareTo(state2);
         }
 
     }
 
     public void showQueuePanel() {
         if(!queuePanelVisible) {
             remove(bugListPanel);
             tabbedPane.add(bugListPanel, 0);
             add(tabbedPane, BorderLayout.CENTER);
             tabbedPane.setSelectedIndex(0);
             queuePanelVisible = true;
             invalidate();
             repaint();
         }
     }
 
     public void hideQueuePanel() {
         if(queuePanelVisible) {
             tabbedPane.remove(bugListPanel);
             remove(tabbedPane);
             add(bugListPanel, BorderLayout.CENTER);
             queuePanelVisible = false;
             invalidate();
             repaint();
         }
     }
 
     public Note getSelectedNote() {
         if(bugList.getSelectedValue() != null) {
             return (Note)bugList.getSelectedValue();
         } else {
             return null;
         }
     }
 
     public void setSelectedNote(Note note) {
         if(note == null) {
             bugList.clearSelection();
         } else {
             bugList.setSelectedValue(note, true);
         }
     }
 
     public void setConnectionMode(boolean offline) {
         refresh.setEnabled(!offline);
         setTitle(tr("OpenStreetMap Notes ({0})", (offline ? tr("offline") : tr("online"))));
         toggleConnectionMode.setSelected(offline);
     }
 
     public void selectionChanged(Collection<Note> newSelection) {
         if(newSelection.size() == 1) {
             Note selectedNote = newSelection.iterator().next();
             if(notesPlugin.getLayer() != null && notesPlugin.getLayer().getDataSet() != null
                     && notesPlugin.getLayer().getDataSet() != null
                     && notesPlugin.getLayer().getDataSet().contains(selectedNote))
             {
                 setSelectedNote(selectedNote);
             } else {
                 bugList.clearSelection();
             }
         } else {
             bugList.clearSelection();
         }
     }
 
     public ActionQueue getActionQueue() {
         return actionQueue;
     }
 
     @Override
     public void setEnabled(boolean enabled) {
         super.setEnabled(enabled);
 
         bugList.setEnabled(enabled);
         queueList.setEnabled(enabled);
         addComment.setEnabled(enabled);
         closeIssue.setEnabled(enabled);
     }
 }
