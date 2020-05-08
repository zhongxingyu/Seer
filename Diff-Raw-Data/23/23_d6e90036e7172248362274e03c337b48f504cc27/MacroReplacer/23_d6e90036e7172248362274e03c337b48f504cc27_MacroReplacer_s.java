 /**
   * Copyright (C) 2012, Laboratorio di Valutazione delle Prestazioni - Politecnico di Milano
 
   * This program is free software; you can redistribute it and/or modify
   * it under the terms of the GNU General Public License as published by
   * the Free Software Foundation; either version 2 of the License, or
   * (at your option) any later version.
 
   * This program is distributed in the hope that it will be useful,
   * but WITHOUT ANY WARRANTY; without even the implied warranty of
   * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   * GNU General Public License for more details.
 
   * You should have received a copy of the GNU General Public License
   * along with this program; if not, write to the Free Software
   * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
   */
 package jmt.framework.data;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 /**
  * <p><b>Name:</b> MacroReplacer</p> 
  * <p><b>Description:</b> 
  * This class is used to replace macros basing on variable values
  * </p>
  * <p><b>Date:</b> 25/jun/2012
  *    <b>Time:</b> 19:57:49</p>
  * @author Bertoli Marco
  * @version 1.0
  */
 public class MacroReplacer {
 	public static final String MACRO_WORKDIR = "${jmt.work.dir}";
 	
 	private static final String SYS_PROP_WORKDIR = "jmt.work.dir";
 	private static final String DEFAULT_WORKDIR = "${user.home}/JMT/";
 
 	
 	private static final String TOKEN = "${"; 
 	private static final Pattern MACRO_PATTERN = Pattern.compile("\\$\\{([^}]+)\\}");
 	
 	public static enum Type {SYSTEM, APP, ALL}
 	
 	/**
 	 * Replace system variables inside the given string.
 	 * Variables are expressed as <code>${variable}</code>
 	 * @param str the string to replace
 	 * @return replaced string
 	 */
 	public static String replace(String str) {
 		return replace(Type.ALL, str);
 	}
 	
 	/**
 	 * Replace variables inside the given string.
 	 * Variables are expressed as <code>${variable}</code>
 	 * @param replacementType the type of replacement
 	 * @param str the string to replace
 	 * @return replaced string
 	 */
 	public static String replace(Type replacementType, String str) {
		if (str.indexOf(TOKEN) < 0) {
 			return str;
 		}
 		
 		StringBuffer ret = new StringBuffer(str.length());
 		Matcher m = MACRO_PATTERN.matcher(str);
 		while (m.find()) {
 			String value = evaluateReplacement(replacementType, m.group(1));
 			if (value == null) {
 				value = m.group(0);
 			}
 			m.appendReplacement(ret, Matcher.quoteReplacement(value));
 		}
 		m.appendTail(ret);
 		return ret.toString();
 	}
 	
 	/**
 	 * Evaluate macro replacements
 	 * @param replacementType the type of replacement
 	 * @param macro the macro to replace
 	 * @return replaced macro
 	 */
 	private static String evaluateReplacement(Type replacementType, String macro) {
 		switch (replacementType) {
 			case SYSTEM:
 				return System.getProperty(macro);
 			case APP:
 				if (SYS_PROP_WORKDIR.equals(macro)) {
					return replace(Type.SYSTEM, System.getProperty(SYS_PROP_WORKDIR, DEFAULT_WORKDIR));
 				} else {
 					return null;
 				}
 			case ALL:
 				String ret = evaluateReplacement(Type.APP, macro);
 				if (ret != null) {
 					return ret;
 				} else {
 					return evaluateReplacement(Type.SYSTEM, macro);
 				}
 			default:
 				return null;
 		}
 	}
 	
	public static void main(String[] args) {
		System.out.println(replace("Test: ${user.home}/JMT/"));
		System.out.println(replace("Test2: ${user.dir}/JMT/"));
		System.out.println(replace("Test3: ${user.dirko}/JMT/"));
 	}
 }
