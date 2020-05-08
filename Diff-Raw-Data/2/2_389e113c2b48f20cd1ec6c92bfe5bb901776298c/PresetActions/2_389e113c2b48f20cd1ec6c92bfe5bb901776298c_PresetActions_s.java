 package com.intelix.digihdmi.app.actions;
 
 import com.intelix.digihdmi.app.DigiHdmiApp;
 import com.intelix.digihdmi.app.tasks.ApplyPresetTask;
 import com.intelix.digihdmi.app.tasks.LoadPresetsListTask;
 import com.intelix.digihdmi.app.tasks.SavePresetTask;
 import com.intelix.digihdmi.app.tasks.SavePresetsListTask;
 import com.intelix.digihdmi.app.views.dialogs.NameChangeDlg;
 import java.awt.event.ActionEvent;
 import javax.swing.AbstractButton;
 import javax.swing.JOptionPane;
 import javax.swing.JTextField;
 import org.jdesktop.application.Action;
 import org.jdesktop.application.Application;
 import org.jdesktop.application.Task;
 
 public class PresetActions {
 
     DigiHdmiApp appInstance = null;
 
     public PresetActions() {
         appInstance = (DigiHdmiApp) Application.getInstance();
     }
 
     @Action (block=Task.BlockingScope.WINDOW)
     public Task showPresetListForLoad() {
         Task t = new LoadPresetsListTask(Application.getInstance());
         t.setInputBlocker(appInstance.new BusyInputBlocker(t));
         return t;
     }
 
     @Action
     public Task showPresetListForSave() {
         Task t = new SavePresetsListTask(Application.getInstance());
         t.setInputBlocker(appInstance.new BusyInputBlocker(t));
         return t;
     }
 
     @Action (block=Task.BlockingScope.WINDOW)
     public Task applyPresetAndShowMatrixView(ActionEvent ev) {
         AbstractButton b = (AbstractButton) ev.getSource();
         int index = b.getName().lastIndexOf('_') + 1;
         int presetNumber = Integer.parseInt(b.getName().substring(index));
 
         // show the matrix view
         appInstance.showMatrixView();
 
         // populate the matrix view with results from preset application
         Task t = new ApplyPresetTask(appInstance, presetNumber);
         t.setInputBlocker(appInstance.new BusyInputBlocker(t));
         return t;
     }
 
     @Action (block=Task.BlockingScope.WINDOW)
     public Task savePresetAndShowMatrixView(ActionEvent ev) {
         AbstractButton b = (AbstractButton) ev.getSource();
         int index = b.getName().lastIndexOf('_') + 1;
         int presetNumber = Integer.parseInt(b.getName().substring(index));
 
         // Get the new name of the preset
         NameChangeDlg dlg = new NameChangeDlg(appInstance.getMainFrame(),
                "",appInstance.getDevice().getIONameLength());
         dlg.setVisible(true);
 
         appInstance.showMatrixView();
 
         String newName = dlg.getTheName();
         if (!dlg.isCancelled() && newName.length() > 0)
         {
             // populate the matrix view with results from preset application
             Task t = new SavePresetTask(appInstance, presetNumber, newName);
             t.setInputBlocker(appInstance.new BusyInputBlocker(t));
             return t;
         } else {
             // show the matrix view
             return null;
         }
     }
 }
