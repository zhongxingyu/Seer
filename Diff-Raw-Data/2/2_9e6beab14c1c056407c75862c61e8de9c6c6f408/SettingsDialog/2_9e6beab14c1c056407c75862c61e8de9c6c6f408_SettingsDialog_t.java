 /*
  *  Copyright (C) 2010 wusel
  * 
  *  This program is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  * 
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  * 
  *  You should have received a copy of the GNU General Public License
  *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package de.wusel.partyplayer.gui.dialog;
 
 import java.awt.Window;
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import javax.swing.AbstractListModel;
 import javax.swing.JButton;
 import javax.swing.JDialog;
 import javax.swing.JFileChooser;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.JPanel;
 import javax.swing.JSeparator;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 import net.miginfocom.swing.MigLayout;
 import org.apache.log4j.Logger;
 import org.jdesktop.application.Action;
 import org.jdesktop.application.Application;
 
 /**
  *
  * @author wusel
  */
 public class SettingsDialog extends JDialog {
 
     private static final Logger log = Logger.getLogger(SettingsDialog.class);
     private DialogStatus status = DialogStatus.CANCELED;
     private final FileListModel listModel;
     private JList fileList;
 
     public SettingsDialog(Window owner, List<File> files) {
         super(owner, ModalityType.APPLICATION_MODAL);
         listModel = new FileListModel(files);
         initUI();
         pack();
         setResizable(true);
         setSize(240, 300);
         setLocationRelativeTo(owner);
         setTitle(getText("dialog.title"));
     }
 
     private void initUI() {
         setLayout(new MigLayout("fill", "[] [grow] []", "[] [] [] [grow] []"));
         add(new JLabel(getText("layout.folder.title")));
         add(new JSeparator(), "grow, span, wrap");
         fileList = new JList(listModel);
         add(fileList, "span 2 3, grow");
 
         final JButton addButton = new JButton(getAction("folderAdd"));
         addButton.setText(null);
 
         final JButton removeButton = new JButton(getAction("folderDelete"));
         removeButton.setText(null);
         
         fileList.addListSelectionListener(new ListSelectionListener() {
 
             @Override
             public void valueChanged(ListSelectionEvent e) {
                 if (!e.getValueIsAdjusting()) {
                     removeButton.setEnabled(fileList.getSelectedIndex() != -1);
                 }
             }
         });
         add(addButton, "wrap");
         add(removeButton, "wrap, grow");
         add(new JSeparator(), "newline push, span, grow, aligny top, wrap");
         JPanel buttonPanel = new JPanel(new MigLayout("insets 0 0 0 0, fill"));
         JButton cancelButton = new JButton(getAction("cancel"));
         buttonPanel.add(cancelButton, "split 2, tag cancel");
         JButton okButton = new JButton(getAction("ok"));
         buttonPanel.add(okButton, "tag ok");
         add(buttonPanel, "span 3, growx");
     }
 
     public List<File> getSelectedDirectories() {
         return Collections.unmodifiableList(listModel.files);
     }
 
     @Action
     public void folderAdd() {
         JFileChooser fileChooser = new JFileChooser();
         fileChooser.setMultiSelectionEnabled(true);
         fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
         int result = fileChooser.showOpenDialog(rootPane);
         if (result == JFileChooser.APPROVE_OPTION) {
             File[] selectedFiles = fileChooser.getSelectedFiles();
             for (File file : selectedFiles) {
                 listModel.addFile(file);
             }
         }
     }
 
     @Action
     public void folderDelete() {
         int[] selectedValues = fileList.getSelectedIndices();
         for (int i = selectedValues.length - 1; i >= 0; i--) {
             listModel.removeFile(selectedValues[i]);
         }
 
     }
 
     @Action
     public void ok() {
         status = DialogStatus.CONFIRMED;
         dispose();
     }
 
     @Action
     public void cancel() {
         status = DialogStatus.CANCELED;
         dispose();
     }
 
     public DialogStatus getStatus() {
         return status;
     }
 
     public static final class FileListModel extends AbstractListModel {
 
         private final List<File> files = new ArrayList<File>();
 
         private FileListModel(List<File> files) {
             this.files.addAll(files);
         }
 
         @Override
         public int getSize() {
             return files.size();
         }
 
         @Override
         public Object getElementAt(int index) {
             return files.get(index);
         }
 
         public void addFile(File file) {
             this.files.add(file);
             fireIntervalAdded(file, this.files.size(), this.files.size());
         }
 
         public void removeFile(int index) {
             this.files.remove(index);
             fireIntervalRemoved(this, index, index);
         }
     }
 
     private String getText(String textKey) {
         return Application.getInstance().getContext().getResourceMap(SettingsDialog.class).getString(textKey);
     }
 
     private javax.swing.Action getAction(String actionKey) {
         return Application.getInstance().getContext().getActionMap(this).get(actionKey);
     }
 }
