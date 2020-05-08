 package kkckkc.jsourcepad.action;
 
 import kkckkc.jsourcepad.model.Application;
 import kkckkc.jsourcepad.model.SettingsManager;
 import kkckkc.jsourcepad.model.WindowSettings;
 import kkckkc.jsourcepad.util.action.BaseAction;
 
 import java.awt.event.ActionEvent;
 
public class ViewShowHideProjectDrawerAction extends BaseAction {
     public ViewShowHideProjectDrawerAction() {
     }
 
     @Override
     public void actionPerformed(ActionEvent e) {
         SettingsManager settingsManager = Application.get().getSettingsManager();
 
         WindowSettings ws = settingsManager.get(WindowSettings.class);
         ws.setShowProjectDrawer(! ws.isShowProjectDrawer());
         settingsManager.update(ws);
     }
 }
