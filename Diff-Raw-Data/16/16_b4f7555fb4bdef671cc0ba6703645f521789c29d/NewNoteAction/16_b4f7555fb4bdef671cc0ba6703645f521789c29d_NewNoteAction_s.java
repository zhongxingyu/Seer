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
 package org.openstreetmap.josm.plugins.notes.gui.action;
 
 import static org.openstreetmap.josm.tools.I18n.tr;
 
 import java.awt.event.ActionEvent;
 import java.io.IOException;
 import java.util.LinkedList;
 import java.util.List;
 
 import javax.swing.JOptionPane;
 
 import org.openstreetmap.josm.Main;
 import org.openstreetmap.josm.data.coor.LatLon;
 import org.openstreetmap.josm.gui.widgets.HistoryChangedListener;
 import org.openstreetmap.josm.io.OsmApiException;
 import org.openstreetmap.josm.io.OsmTransferException;
 import org.openstreetmap.josm.plugins.notes.ConfigKeys;
 import org.openstreetmap.josm.plugins.notes.Note;
 import org.openstreetmap.josm.plugins.notes.NotesPlugin;
 import org.openstreetmap.josm.plugins.notes.api.NewAction;
 import org.openstreetmap.josm.plugins.notes.gui.dialogs.TextInputDialog;
 
 public class NewNoteAction extends NotesAction {
 
     private static final long serialVersionUID = 1L;
 
     private NotesPlugin plugin;
 
     private String result;
 
     private LatLon latlon;
 
     private NewAction newAction = new NewAction();
 
     public NewNoteAction(NotesPlugin plugin, LatLon latlon) {
         super(tr("New note"), plugin.getDialog());
         this.plugin = plugin;
         this.latlon = latlon;
     }
 
     @Override
     protected void doActionPerformed(ActionEvent e) throws IOException, InterruptedException {
         List<String> history = new LinkedList<String>(Main.pref.getCollection(ConfigKeys.NOTES_NEW_HISTORY, new LinkedList<String>()));
         HistoryChangedListener l = new HistoryChangedListener() {
             public void historyChanged(List<String> history) {
                 Main.pref.putCollection(ConfigKeys.NOTES_NEW_HISTORY, history);
             }
         };
 
         result = TextInputDialog.showDialog(
                 Main.map,
                 tr("Create note"),
                 tr("Describe the problem precisely"),
                 NotesPlugin.loadIcon("new_note22.png"),
                 history, l);
 
         if(result == null) {
             canceled = true;
         }
     }
 
     @Override
     public void execute() throws IOException {
         if (result.length() > 0) {
             try {
                 Note n = newAction.execute(latlon, result);
                 plugin.getDataSet().add(n);
             } catch (OsmApiException e) {
                 String reason;
                 if (e.getErrorHeader().contains("capability")) {
                    reason = tr("your version of JOSM does not support note creation. Please upgrade to JOSM version 6076.");
                 } else {
                     reason = e.getErrorHeader();
                 }
                 e.printStackTrace();
                 JOptionPane.showMessageDialog(
                         Main.parent,
                         tr("Could not create a new note because {0}", reason),
                         tr("Error create a new note"),
                         JOptionPane.WARNING_MESSAGE
                 );
             } catch (OsmTransferException e) {
                 e.printStackTrace();
                 JOptionPane.showMessageDialog(
                         Main.parent,
                         tr("Could not create a new note because {0}", e.getMessage()),
                         tr("Error create a new note"),
                         JOptionPane.WARNING_MESSAGE
                 );
             }
             if (Main.pref.getBoolean(ConfigKeys.NOTES_API_DISABLED)) {
                 plugin.updateGui();
             } else {
                 plugin.updateData();
             }
         }
     }
 
     @Override
     public String toString() {
         return tr("Create: " + result);
     }
 
     @Override
     public NotesAction clone() {
         NewNoteAction action = new NewNoteAction(plugin, latlon);
         action.canceled = canceled;
         action.result = result;
         action.latlon = latlon;
         return action;
     }
 }
