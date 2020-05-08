 package org.jboss.tools.livereload.ui.internal.command;
 
 import org.eclipse.osgi.util.NLS;
 
 public class DialogMessages extends NLS {
 
 	private static final String BUNDLE_NAME = DialogMessages.class.getName();
 
 	static {
 		NLS.initializeMessages(BUNDLE_NAME, DialogMessages.class);
 	}
 
 	private DialogMessages() {
 		// Do not instantiate
 	}
 
 	public static String QRCODE_DIALOG_NAME;
 	public static String QRCODE_DIALOG_TITLE;
 	public static String QRCODE_DIALOG_MESSAGE;
 	
 	public static String REMOTE_CONNECTIONS_DIALOG_TITLE;
 	public static String REMOTE_CONNECTIONS_DIALOG_MESSAGE;
 
 	public static String SCRIPT_INJECTION_DIALOG_TITLE;
 	public static String SCRIPT_INJECTION_DIALOG_MESSAGE;
 	
 	public static String LIVERELOAD_SERVER_DIALOG_TITLE;
	public static String LIVERELOAD_SERVER_DIALOG_MESSAGE;
 }
