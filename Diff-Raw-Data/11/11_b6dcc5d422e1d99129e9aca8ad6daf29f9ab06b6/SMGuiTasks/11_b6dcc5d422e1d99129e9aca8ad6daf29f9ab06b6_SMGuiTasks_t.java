 package com.redhat.qe.sm.gui.tasks;
 
 import java.util.Properties;
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
			//dismiss the dialog
			ldtp().click(UI.OK_error);
 			throw new AssertionError("Error dialog was displayed.");
 		}
 		log.log(Level.FINER, "Error dialog did not appear");
 	}
 	
 	public void register(String username, String password, String systemName, boolean autoSubscribe){
 		ldtp().click(UI.registration);
 		ldtp().waitTilGuiExist(UI.redhatLogin);
 		ldtp().setTextValue(UI.redhatLogin, username);
 		ldtp().setTextValue(UI.password, password);
 		if (systemName != null) ldtp().setTextValue(UI.systemName, systemName);
 		ldtp().setChecked(UI.automaticallySubscribe, autoSubscribe);
 		ldtp().click(UI.register);
 		checkForError();
 	}
 
 	public void unregister(){
 		ldtp().click(UI.unregister);
 		ldtp().waitTilGuiExist(UI.questionDialog, 5);
 		ldtp().click(UI.yes);
 		checkForError();
 	}
 	
 	public Properties getAllFacts(){
 		Properties p = new Properties();
 		ldtp().click(UI.systemFacts);
 		ldtp().waitTilGuiExist(UI.factsDialog);
 		try {
 			int rows = ldtp().getRowCount(UI.factsTable);
 			log.finer("Found " + rows + " rows in facts table.");
 			for (int i=0; i<rows; i++){
 				p.put(ldtp().getCellValue(UI.factsTable, i, 0),	 ldtp().getCellValue(UI.factsTable, i, 1));
 			}	
 		}
 		finally {
 			ldtp().click(UI.close_facts);
 		}
 		return p;
 	}
 	
 	public void subscribeTo(String... productNames){
 		ldtp().click(UI.add);
 		ldtp().waitTilGuiExist(UI.subscribeDialog);
 		for (String productName: productNames){
 			ldtp().checkRow(UI.compatibleSubscriptions, ldtp().getTableRowIndex(UI.compatibleSubscriptions, productName), 0);
 		}
 		ldtp().click(UI.apply_subscribe);
 		checkForError();
 	}
 	
 	public void unsubscribeFrom(String... productNames){
 		for (String productName: productNames) {
 			ldtp().selectRowPartialMatch(UI.subscriptions, productName);
 			ldtp().click(UI.unsubscribe);
 			ldtp().waitTilGuiExist(UI.questionDialog);
 			ldtp().click(UI.yes);
 		}
 	}
 }
