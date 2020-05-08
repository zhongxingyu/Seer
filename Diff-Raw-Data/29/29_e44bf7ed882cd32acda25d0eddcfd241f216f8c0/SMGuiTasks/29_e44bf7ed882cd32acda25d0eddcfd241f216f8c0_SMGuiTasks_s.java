 package com.redhat.qe.sm.gui.tasks;
 
 import com.redhat.qe.ldtpclient.LDTPClient;
 import com.redhat.qe.sm.base.SubscriptionManagerGUITestScript;
 import com.redhat.qe.sm.gui.locators.UI;
 
 public class SMGuiTasks {
 	UI ui = UI.getInstance();
	
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
