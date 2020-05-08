 /*
  * Copyright IBM Corp. 2012
  */
 
 package org.rstl;
 
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.Arrays;
 import java.util.List;
 
import org.apache.commons.lang.StringEscapeUtils;
 
 /**
  * Utility class to allow template to filter or transform variable values.
  * TODO: This class needs to have a dynamic way of accepting new filters 
  *
  */
 public class VarUtil {
 	private static String[] builtInFilters = {"urlencode", "upper", "lower", "join", "xmlescape" };
 	
 	private static List<String> builtInList = Arrays.asList(builtInFilters);
 	
 	public static String getFilterFunction(String filterName) {
 		if (builtInList.contains(filterName)) {
 			return "VarUtil." + filterName + "(";
 		} else {
 			return "VarUtil.noop(";
 		}
 	}
 	
 	/**
 	 * URLEncode a string (per Java rules)
 	 * @param s
 	 * @return
 	 */
 	public static String urlencode(String s) {
 		String retval = s;
 		URI u = null;
 		/*
 		retval = retval.replaceAll(" ", "%20");
 		return retval;
 		*/
 		
 		try {
 			u = new URI(null, null, s, null);
 		} catch (URISyntaxException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		if (null != u) {
 			return u.toASCIIString();
 		} else {
 			return retval.replaceAll(" ", "%20");
 		}
 	}
 
 	/**
 	 * Return upper case version of string
 	 * @param s
 	 * @return
 	 */
 	public static String upper(String s) {
 		String retval = s.toUpperCase();
 		return retval;
 	}
 	
 	public static String lower(String s) {
 		String retval = s.toLowerCase();
 		return retval;
 	}
 	
 	public static String join(List<Object> list) {
 		StringBuilder sb = new StringBuilder();
 		for (Object x: list) {
 			sb.append(x.toString());
 		}
 		return sb.toString();
 	}
 	
 	public static String join(String s) {
 		return s;
 	}
 	
 	public static String xmlescape(String s) {
 		return StringEscapeUtils.escapeXml(s);
 	}
 	
 	public static String noop(String s) {
 		return s;
 	}
 }
