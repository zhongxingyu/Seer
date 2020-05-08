 package edu.teco.dnd.eclipse.moduleView;
 
 import org.eclipse.osgi.util.NLS;
 
 public class Messages extends NLS {
 	private static final String BUNDLE_NAME = "edu.teco.dnd.eclipse.moduleView.messages"; //$NON-NLS-1$
 	public static String ModuleView_MODULE_TABLE_TOOLTIP;
 	public static String ModuleView_COLUMN_MODULE_LOCATION;
 	public static String ModuleView_COLUMN_MODULE_ID;
 	public static String ModuleView_COLUMN_MODULE_NAME;
 	public static String ModuleView_START_STOP_BUTTON_TOOLTIP;
 	public static String StartStopButtonActivator_BUTTON_ERROR;
 	public static String StartStopButtonActivator_BUTTON_SERVER_STARTING;
 	public static String StartStopButtonActivator_BUTTON_SERVER_STOPPING;
 	public static String StartStopButtonActivator_BUTTON_START_SERVER;
 	public static String StartStopButtonActivator_BUTTON_STOP_SERVER;
 	static {
 		// initialize resource bundle
 		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
 	}
 
 	private Messages() {
 	}
 }
