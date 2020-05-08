 /**
  * 
  */
 package org.cweili.feed.util;
 
 import org.apache.commons.lang3.StringUtils;
 
 /**
  * 
  * @author Cweili
  * @version 2013-4-18 上午9:28:32
  * 
  */
 public class Utils {
 
 	public static String cdataSpecialChars(String input) {
		return StringUtils.replace(input, "]]>", "]]&gt;");
 	}
 
 	public static String htmlSpecialChars(String input) {
 
 		input = StringUtils.replace(input, "&amp;", "&");
 		input = StringUtils.replace(input, "&", "&amp;");
 		input = StringUtils.replace(input, "\'", "&apos;");
 		input = StringUtils.replace(input, "\"", "&quot;");
 		input = StringUtils.replace(input, "<", "&lt;");
 		input = StringUtils.replace(input, ">", "&gt;");
 		input = StringUtils.replace(input, "«", "&laquo;");
 		input = StringUtils.replace(input, "»", "&raquo;");
 		input = StringUtils.stripToEmpty(input);
 
 		return input;
 	}
 }
