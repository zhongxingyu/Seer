 package net.sf.jmoney.pricehistory.resources;
 
 import org.eclipse.osgi.util.NLS;
 
 public class Messages extends NLS {
	private static final String BUNDLE_NAME = "net.sf.jmoney.reconciliation.resources.messages"; //$NON-NLS-1$
 	public static String PriceInfo_Date;
 	public static String PriceInfo_Price;
 	public static String CommodityPricingInfo_Currency;
 	public static String CommodityPricingInfo_Prices;
 	static {
 		// initialize resource bundle
 		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
 	}
 
 	private Messages() {
 	}
 }
