     This file is part of  EasyTest CodeGen, a project to generate 
 
 package org.easetech.easytest.codegen;
 
 import java.util.Enumeration;
 import java.util.Properties;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
 * A String Helper class
 * @author Ravi Polampelli
 *
 */
 
 public class StringHelper {
  
     /**
      * An instance of logger associated.
      */
     protected static final Logger LOG = LoggerFactory.getLogger(StringHelper.class);
     
     private final static String VARIABLE_START = "${";
     private final static String VARIABLE_END   = "}";
     private final static int    MAX_DEPTH      = 10;
 
     /**
      * Emulates the method indexOf in StringBuffer JDK 1.4 in JDKs prior to that. :-(
      */
     public static int indexOfStringInStringBuffer(StringBuffer sb, String str) {
 
         int returnValue;
 
         returnValue = -1;
 
         if ((sb != null) && (str != null)) {
             returnValue = sb.indexOf(str); 
         }
 
         return returnValue;
     }
 
     public static int indexOfTwoPartString(String code, String partOne, String partTwo, int startIndex) {
         int returnValue = -1;
         int possibleOne;
         int possibleTwo;
         int possibleIndex;
         int codeLength;
         boolean endOfCode;
 
         if ((code != null) && (partOne != null) && (partTwo != null)) {
 
             possibleIndex = Math.max(startIndex, 0);
             codeLength = code.length();
             endOfCode = false;
             while ((returnValue == -1) && (!endOfCode)) {
                 possibleOne = code.indexOf(partOne, possibleIndex);  // Where does the first part start.
 
                 if (possibleOne>-1) {
 
                     possibleIndex = possibleOne + partOne.length();  // go over whitespaces
                     possibleTwo   = code.indexOf(partTwo, possibleIndex);
 
                     while ((possibleIndex < codeLength) && (Character.isWhitespace(code.charAt(possibleIndex)))){
                         possibleIndex++;
                     }
 
                     if (possibleIndex == possibleTwo) {
                         returnValue = possibleOne;
                     }
 
                 } else {
                     endOfCode = true;
                 }
             }
 
         } else {
             err("indexOfTwoPartString() code="+code+" partOne="+partOne+" partTwo="+partTwo);
         }
 
         return returnValue;
     }
 
     public static String firstToUpper(String s) {
 
         StringBuffer sb = new StringBuffer(s);
 
         if (sb.length() > 0) {
             sb.replace(0, 1, "" + Character.toUpperCase(sb.charAt(0)));
         }
 
         return sb.toString();
     }
 
     public static String replaceVariables(String template, Properties properties) {
     	LOG.info("replaceVariables started,template:"+template+" ,properties"+properties);
         String       returnValue = null;
         StringBuffer sb;
         boolean      foundSomething;
         boolean      foundKey;
         Enumeration  e;
         String       key, variable, replacement;
         int          idx, idxOld;
         int depth;
 
         if ((template != null) && (properties != null)) {
             depth = 0;
             sb = new StringBuffer(template);
 
             do {
                 foundSomething = false;
                 e              = properties.propertyNames();
 
                 while (e.hasMoreElements()) {
                     key      = (String) e.nextElement();
                     variable = VARIABLE_START + key + VARIABLE_END;
                     idxOld = -1;
                     do {
                         foundKey = false;
                         idx      = StringHelper.indexOfStringInStringBuffer(sb, variable);
 
                         if (idx >= 0) {
                             if (idx<=idxOld) {
                                 depth++;
                             }
                             foundKey = true;
                             foundSomething = true;
                             replacement = properties.getProperty(key);
                             idxOld = idx+replacement.length();
                             sb.replace(idx, idx + variable.length(), replacement);
                         }
                     } while ((foundKey) && (depth<MAX_DEPTH));
                 }
                 depth++;
             } while ((foundSomething) && (depth<MAX_DEPTH));
             if (depth >= MAX_DEPTH) {
                 throw new RuntimeException("Error in templates: To many recursions.");
             }
 
             returnValue = sb.toString();
 
         } else {
             err("replaceVariables() template="+template+" properties="+properties);
         }
         LOG.info("replaceVariables finished,returnValue:"+returnValue);
         return returnValue;
     }
 
     /**
      * Compares to StringBuffers by their content (and nothing else).
      * This is helpful, since two StringBuffer's may have the same
      * content, but the method equals returns false. It seems, some
      * other attributes of StringBuffer are considered as well, unfortuantely.
      */
     public static boolean haveEqualContent(StringBuffer a, StringBuffer b) {
         boolean returnValue = false;
 
         if ((a != null) && (b != null)) {
             returnValue = a.toString().equals(b.toString());
         } // no else
 
         if ((a == null) && (b == null)) {
             returnValue = true;
         } // no else
 
         return returnValue;
     }
 
     private static void err(String msg) {
         System.out.println("StringHelper."+msg);
     }
 
 	public static String getFilePath(String packageName, String className) {
 		packageName = packageName.replace('.', '/');		
 		return packageName+"/"+className+".xls";
 	}
 
 	public static String getSetterName(String name) {
 		String setterName = "set";
 		setterName = setterName + name.substring(0,1).toUpperCase() + name.substring(1);
 		return setterName;
 	}
 }

