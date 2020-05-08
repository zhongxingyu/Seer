 package com.redhat.qe.sm.gui.tasks;
 
import java.util.logging.Level;
import java.util.logging.Logger;

 import com.redhat.qe.ldtpclient.LDTPClient;
 import com.redhat.qe.sm.base.SubscriptionManagerGUITestScript;
 import com.redhat.qe.sm.gui.locators.UI;
 
 public class SMGuiTasks {
 	UI ui = UI.getInstance();
	protected static Logger log = Logger.getLogger(SMGuiTasks.class.getName());

 	protected static SMGuiTasks instance = null;
 	public static SMGuiTasks getInstance() {
 		if (instance == null)	instance = new SMGuiTasks();
 		return instance;
 	}
 	
 	private SMGuiTasks() {
 		super();
 	}
 	
 	protected LDTPClient ldtp() {
 		return SubscriptionManagerGUITestScript.ldtp();
 	}
 	
	public void checkForError() {		
		if (ldtp().waitTilGuiExist(UI.errorDialog, 3) == 1) {
			throw new AssertionError("Error dialog was displayed.");
		}
		log.log(Level.FINER, "Error dialog did not appear");
	}
	
 	public void register(String username, String password, String systemName, boolean autoSubscribe){
 		ldtp().click(UI.registrationButton);
 		ldtp().waitTilGuiExist(UI.registerDialog);
 		ldtp().setTextValue(UI.redhatLogin, username);
 		ldtp().setTextValue(UI.password, password);
 		if (systemName != null) ldtp().setTextValue(UI.systemName, systemName);
 		ldtp().setChecked(UI.automaticallySubscribe, autoSubscribe);
 		ldtp().click(UI.register);
 	}
 
 }
