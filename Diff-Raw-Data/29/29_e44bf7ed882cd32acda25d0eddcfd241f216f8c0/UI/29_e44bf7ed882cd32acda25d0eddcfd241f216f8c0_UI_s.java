 package com.redhat.qe.sm.gui.locators;
 
 import com.redhat.qe.ldtpclient.Element;
 
 public class UI {
 
 	protected static UI instance = null;
 	public static UI getInstance() {
 		if (instance == null)	instance = new UI();
 		return instance;
 	}
 	
 	private UI() {
 		super();
 	}
 	
 	public static String mainWindowName = "manage_subscriptions_dialog";
 	
 	public static Element mainWindow = new Element (mainWindowName, "");
 	public static Element closeButton = new Element (mainWindowName, "button_close");
 	public static Element addButton = new Element (mainWindowName, "button_add1");
 	public static Element registrationButton = new Element (mainWindowName, "account_settings");
 	public static Element updateButton = new Element (mainWindowName, "button_update1");
 	public static Element unsubscribeButton = new Element (mainWindowName, "button_unsubscribe1");
 	
 	
 	public static String registerDialogName = "register_dialog";
 
 	public static Element registerDialog = new Element (registerDialogName, "");
 	public static Element redhatLogin = new Element (registerDialogName, "account_login");
 	public static Element password = new Element (registerDialogName, "account_password");
 	public static Element systemName = new Element (registerDialogName, "consumer_name");
 	public static Element automaticallySubscribe = new Element (registerDialogName, "auto_bind");
 	public static Element register = new Element (registerDialogName, "register_button");
 	
 
 }
