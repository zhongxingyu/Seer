 package org.eclipse.dltk.ui;
 
 import java.text.MessageFormat;
 
 import org.eclipse.osgi.util.NLS;
 
 public class Messages extends NLS {
 	private static final String BUNDLE_NAME = "org.eclipse.dltk.ui.messages"; //$NON-NLS-1$
	public static String DLTKExecuteExtensionHelper_natureAttributeMustBeSpecifiedAndCorrect;
 	public static String PluginImagesHelper_imageRegistryAlreadyDefined;
 	static {
 		// initialize resource bundle
 		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
 	}
 
 	public static String format(String message, Object object) {
 		return MessageFormat.format(message, new Object[] { object });
 	}
 
 	public static String format(String message, Object[] objects) {
 		return MessageFormat.format(message, objects);
 	}
 
 	private Messages() {
 	}
 }
