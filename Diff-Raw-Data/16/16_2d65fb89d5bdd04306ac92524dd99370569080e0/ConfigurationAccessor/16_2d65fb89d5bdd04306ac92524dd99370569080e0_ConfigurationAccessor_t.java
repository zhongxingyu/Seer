 package com.ghlh.conf;
 
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Properties;
 
 import org.apache.log4j.Logger;
 
 public class ConfigurationAccessor {
 	public final static String CONFIG_FILE = "Config.properties";
 	public final static String TRADE_VENDOR = "TradeVendor";
 	private static Properties prop = new Properties();
 
 	private static Logger logger = Logger
 			.getLogger(ConfigurationAccessor.class);
 
 	private static ConfigurationAccessor instance = new ConfigurationAccessor();
 
 	public static ConfigurationAccessor getInstance() {
 		return instance;
 	}
 
 	private ConfigurationAccessor() {
 		try {
 			logger.debug("loading");
 			prop.load(new FileInputStream(CONFIG_FILE));
 			logger.debug("loading1");
 		} catch (Exception ex) {
 			logger.error("Read configuration throw exception:", ex);
 		}
 	}
 
 	public String getTradeVendorName() {
 		String vendor = prop.getProperty(TRADE_VENDOR);
 		return vendor.substring(0, vendor.indexOf("-"));
 	}
 
 	public String getTradeVendor() {
 		String vendor = prop.getProperty(TRADE_VENDOR);
 		return vendor.substring(vendor.indexOf("-") + 1);
 	}
 
 	public void saveTradeVendor(String vendor) {
 		prop = new Properties();
 		prop.setProperty("TradeVendor", vendor + "-"
 				+ getSoftwareConfigurationMap(vendor));
 		try {
 			prop.store(new FileOutputStream(CONFIG_FILE), "Saved on "
 					+ new Date());
 		} catch (Exception ex) {
 			logger.error("Save configuration throw exception:", ex);
 		}
 	}
 
 	private String getSoftwareConfigurationMap(String vendor) {
 		Map map = new HashMap();
 		map.put("֤ȯ", "zxzq");
 		map.put("֤ȯ", "zszq");
 		String result = map.get(vendor).toString();
 		return result;
 	}
 
	private boolean openSoftwareTrade = true;
 
 	public boolean isOpenSoftwareTrade() {
 		return openSoftwareTrade;
 	}
 
 	public void setOpenSoftwareTrade(boolean openSoftwareTrade) {
 		this.openSoftwareTrade = openSoftwareTrade;
 	}
 
 }
