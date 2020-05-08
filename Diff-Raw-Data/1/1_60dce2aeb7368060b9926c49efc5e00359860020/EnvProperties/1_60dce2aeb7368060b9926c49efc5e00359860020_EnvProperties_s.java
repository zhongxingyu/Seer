 package com.centny.jetty4a.server;
 
 import java.util.Properties;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 public class EnvProperties extends Properties {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = -6505115094420031968L;
 
 	public EnvProperties() {
 		super();
 	}
 
 	public EnvProperties(Properties pro) {
 		super(pro);
 	}
 
 	/**
 	 * covert value to env by match $(*).
 	 * 
 	 * @param val
 	 *            target value.
 	 * @return converted value.
 	 */
 	public static String envVal(String val) {
 		if (val == null || val.length() == 0) {
 			return val;
 		}
 		Pattern ptn = Pattern.compile("\\$\\([^\\)]*\\)");
 		Matcher mch = ptn.matcher(val);
 		StringBuffer sb = new StringBuffer();
 		String tmp;
 		while (mch.find()) {
 			String key = mch.group();
 			key = key.substring(2, key.length() - 1);
 			String kval = "";
 			tmp = System.getenv(key);
 			if (tmp != null) {
 				kval = tmp;
 			}
 			tmp = System.getProperty(key);
 			if (tmp != null) {
 				kval = tmp;
 			}
 			mch.appendReplacement(sb, kval);
 		}
 		return sb.toString();
 	}
 
 	@Override
 	public String getProperty(String name, String defaultValue) {
 		return envVal(super.getProperty(name, defaultValue));
 	}
 
 	@Override
 	public String getProperty(String name) {
 		return envVal(super.getProperty(name));
 	}
 
 	@Override
 	public synchronized Object get(Object key) {
 		Object obj = super.get(key);
 		if (obj instanceof String) {
 			return envVal((String) obj);
 		} else {
 			return obj;
 		}
 	}
 
 	public void append(Properties other) {
 		for (Object key : other.keySet()) {
 			this.setProperty((String) key, other.getProperty((String) key));
 		}
 	}
 
 	public void appendTo(Properties other) {
 		for (Object key : this.keySet()) {
 			other.setProperty((String) key, this.getProperty((String) key));
 		}
 	}
 }
