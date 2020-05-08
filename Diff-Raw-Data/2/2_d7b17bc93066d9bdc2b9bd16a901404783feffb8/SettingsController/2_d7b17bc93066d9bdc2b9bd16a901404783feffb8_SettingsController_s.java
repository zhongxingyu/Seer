 package controllers;
 
 import models.Organizer;
 import models.UserProfile;
 import views.gui.Settings;
 
 import javax.swing.*;
 import java.awt.Color;
 import java.awt.event.ActionEvent;
 
 public class SettingsController extends Controller {
 	
 	public SettingsController(Settings ref) {
 		this.ref = ref;
 	}
 
 	@Override
 	public void actionPerformed(ActionEvent arg0) {
 		
 		Organizer o = Organizer.getInstance();
 		UserProfile logged = o.getCurrentUser().getUserProfile();
 		
 		/**
 		 * Saves up-to-date data from Settings view after OK to current user profile
 		 */
 		
 		logged.setPath(ref.getPath());
 		
 		logged.setVelocity(ref.getVelocity());
 		
 		logged.setIconPath(ref.getIconPath());
 		
 		Color[] modColors = ref.getColorChoices();
 		
 		if(modColors != null) {
 			
 			for(int i=0; i<5; ++i)
 				logged.setPriorityColor(i+1, modColors[i]);
 			
 		}
 
         if(ref.getDisplayFirstHour()>=ref.getDisplayLastHour()){
             JOptionPane alert = new JOptionPane("Start hour cannot be equal or after end hour",
                    JOptionPane.ERROR_MESSAGE, JOptionPane.OK_OPTION);
             alert.createDialog(ref, "").setVisible(true);
             if(alert.getValue().equals(0))
                 return;
         } else {
             logged.setDisplayFirstHour(ref.getDisplayFirstHour());
             logged.setDisplayLastHour(ref.getDisplayLastHour());
         }
 
 		o.update();
 		o.notifyObservers();
 		
 		ref.dispose();
 	}
 	
 	private Settings ref;
 }
