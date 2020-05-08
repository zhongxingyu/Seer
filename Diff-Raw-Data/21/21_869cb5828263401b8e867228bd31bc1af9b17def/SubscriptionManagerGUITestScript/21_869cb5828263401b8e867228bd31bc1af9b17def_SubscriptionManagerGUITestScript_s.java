 package com.redhat.qe.sm.base;
 
 import org.testng.annotations.BeforeSuite;
 import org.testng.annotations.Test;
 
 import com.redhat.qe.ldtpclient.LDTPClient;
 import com.redhat.qe.sm.gui.locators.UI;
 import com.redhat.qe.sm.gui.tasks.SMGuiTasks;
 
 
 public class SubscriptionManagerGUITestScript extends SubscriptionManagerBaseTestScript {
 
 	protected static UI ui = UI.getInstance();
 	protected static SMGuiTasks tasks = SMGuiTasks.getInstance();
 	protected static LDTPClient ldtpInstance = null;
 	
 	
 	public static LDTPClient ldtp() {
 		return ldtpInstance;
 	}
 	
 	@BeforeSuite
 	public void startLDTP(){
 		ldtpInstance  = new LDTPClient("http://"  + clienthostname + ":8001/");
 		ldtp().init();
		ldtp().launchApp("subscription-manager-gui", new String[] {});
 		ldtp().waitTilGuiExist(UI.mainWindow);
 	}
 	
 	//test test, really belongs elsewhere, but here now for convenience - jweiss
 	@Test
 	public void register(){
 		tasks.register(clientusername, clientpassword, clienthostname, true);
 	}
 }
