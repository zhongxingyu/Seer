 package com.redhat.qe.sm.base;
 
 import java.util.Properties;
 
 import org.testng.annotations.AfterSuite;
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
 		String binary = System.getProperty("rhsm.gui.binary", "subscription-manager-gui");
 		ldtp().launchApp(binary, new String[] {});
 		ldtp().waitTilGuiExist(UI.mainWindow);
 	}
 	
 	//test test, really belongs elsewhere, but here now for convenience - jweiss
 	@Test(enabled=true)
 	public void register(){
 		tasks.register(clientusername, clientpassword, clienthostname, true);
 		tasks.checkForError();
 	}
 	
	@Test(dependsOnMethods="register", enabled=false)
 	public void unregister(){
 		tasks.unregister();
 	}
 	
 	@Test(enabled=false)
 	public void printFacts(){
 		Properties p = tasks.getAllFacts();
 		log.info("Retrieved facts:" + p.toString());
 	}
 	
	@Test
 	public void subscribeTest(){
 		tasks.subscribeTo("RHEL Workstation");
 	}
 	
 	@AfterSuite
 	public void closeSM(){
 		ldtp().closeWindow("Subscription Manager");
 	}
 	
 }
