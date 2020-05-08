 package com.kn.groupBuilder.Gui.Menu.Listener;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JComboBox;
 import javax.swing.JTextField;
 
import com.kn.groupBuilder.FileOperations.Reader.FileReaderHelper;
 import com.kn.groupBuilder.Gui.Menu.SettingsFrame;
import com.kn.groupBuilder.Gui.TableModels.GroupTableModel;
import com.kn.groupBuilder.Gui.TableModels.MemberTableModel;
 import com.kn.groupBuilder.Storage.Pojo;
 import com.kn.groupBuilder.Storage.Settings;
 
 import dennis.markmann.MyLibraries.GuiJobs.DefaultFrames.PathChooser;
 
 /**
  * Listener for the settings frame. Used to fill the different values and to close the window.
  * 
  * @author dennis.markmann
  * @since JDK.1.7.0_21
  * @version 1.0
  */
 
 public class SettingsFrameListener implements ActionListener {
 
     private final SettingsFrame settingsFrame;
     private final Pojo pojo;
     private final JTextField pathField;
     private final JComboBox<String> languageBox;
     private final JCheckBox archivingBox;
     private final JTextField archiveField;
     private final JCheckBox sendMailsAutomatically;
     private final JCheckBox printOutAutomatically;
 
     public SettingsFrameListener(
             final SettingsFrame settingsFrame,
             final Pojo pojo,
             final JTextField pathField,
             final JComboBox<String> languageBox,
             final JCheckBox archivingBox,
             final JTextField archiveField,
             final JCheckBox sendMailsAutomatically,
             final JCheckBox printOutAutomatically) {
 
         this.settingsFrame = settingsFrame;
         this.pojo = pojo;
         this.pathField = pathField;
         this.languageBox = languageBox;
         this.archivingBox = archivingBox;
         this.archiveField = archiveField;
         this.sendMailsAutomatically = sendMailsAutomatically;
         this.printOutAutomatically = printOutAutomatically;
     }
 
     @Override
     public final void actionPerformed(final ActionEvent event) {
 
         final JButton buttonClicked = (JButton) event.getSource();
 
         if (buttonClicked.getName().compareTo("pathButton") == 0) {
             this.pojo.getSettings().setPath(new PathChooser().changePath());
             this.settingsFrame.updatePathField(this.pathField, this.pojo);
            new FileReaderHelper().readXMLFiles(this.pojo);
            MemberTableModel.refreshTable();
            GroupTableModel.refreshTable();
 
         } else if (buttonClicked.getName().compareTo("saveButton") == 0) {
             final Settings settings = this.pojo.getSettings();
 
             settings.setArchived(this.readCheckBox(this.archivingBox));
             settings.setSendMailAutomatically(this.readCheckBox(this.sendMailsAutomatically));
             settings.setPrintAutomatically(this.readCheckBox(this.printOutAutomatically));
             settings.setLanguage(this.pojo.getLanguageList()[this.languageBox.getSelectedIndex()]);
             settings.setArchivingDays(Integer.parseInt(this.archiveField.getText()));
 
             this.settingsFrame.closeWindow();
 
         } else if (buttonClicked.getName().compareTo("closeButton") == 0) {
             this.settingsFrame.closeWindow();
         }
     }
 
     private boolean readCheckBox(final JCheckBox checkBox) {
 
         if (checkBox.isSelected()) {
             return (true);
         }
         return (false);
 
     }
 }
