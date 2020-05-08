 package org.zend.php.common.callout;
 
 import org.eclipse.osgi.util.NLS;
 
 
 public class Messages extends NLS {
	private static final String BUNDLE_NAME = "com.zend.php.ui.callout.messages"; //$NON-NLS-1$
 	public static String MessageWithHelpBody_0;
 	static {
 		// initialize resource bundle
 		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
 	}
 
 	private Messages() {
 	}
 }
