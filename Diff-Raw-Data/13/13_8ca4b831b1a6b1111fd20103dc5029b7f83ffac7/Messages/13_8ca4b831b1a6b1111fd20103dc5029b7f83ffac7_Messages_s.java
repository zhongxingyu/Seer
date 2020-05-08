 package net.sf.jmoney.stocks.resources;
 
 import org.eclipse.osgi.util.NLS;
 
 public class Messages extends NLS {
 	private static final String BUNDLE_NAME = "net.sf.jmoney.stocks.resources.messages"; //$NON-NLS-1$
 
 	public static String NavigationTreeNode_stocks;
 	public static String NavigationTree_stockDescription;
 
 	static {
 		// initialize resource bundle
 		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
 	}
 
 	private Messages() {
 		// Private constructor
 	}
 
 }
