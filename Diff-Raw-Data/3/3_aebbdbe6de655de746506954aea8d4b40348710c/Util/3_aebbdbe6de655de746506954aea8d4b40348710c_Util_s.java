 /* **********************************************************************
     Copyright 2006 Rensselaer Polytechnic Institute. All worldwide rights reserved.
 
     Redistribution and use of this distribution in source and binary forms,
     with or without modification, are permitted provided that:
        The above copyright notice and this permission notice appear in all
         copies and supporting documentation;
 
         The name, identifiers, and trademarks of Rensselaer Polytechnic
         Institute are not used in advertising or publicity without the
         express prior written permission of Rensselaer Polytechnic Institute;
 
     DISCLAIMER: The software is distributed" AS IS" without any express or
     implied warranty, including but not limited to, any implied warranties
     of merchantability or fitness for a particular purpose or any warrant)'
     of non-infringement of any current or pending patent rights. The authors
     of the software make no representations about the suitability of this
     software for any particular purpose. The entire risk as to the quality
     and performance of the software is with the user. Should the software
     prove defective, the user assumes the cost of all necessary servicing,
     repair or correction. In particular, neither Rensselaer Polytechnic
     Institute, nor the authors of the software are liable for any indirect,
     special, consequential, or incidental damages related to the software,
     to the maximum extent the law permits.
 */
 
 package edu.rpi.sss.util;
 
 import java.io.InputStream;
 import java.net.URLDecoder;
 import java.net.URLEncoder;
 import java.text.MessageFormat;
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Properties;
 import java.util.Random;
 import java.util.StringTokenizer;
 
 /**
 *
 * @author   Mike Douglass
 * @version  1.0
 *
 * A number of bitty utility routines.
 */
 public class Util {
   /** set true to generate some output */
   public static boolean debugTrace;
 
   private Util() {} // Don't instantiate this
 
   /** Load a named resource as a Properties object
    *
    * @param name    String resource name
    * @return Properties populated from the resource
    * @throws Throwable
    */
   public static Properties getPropertiesFromResource(String name) throws Throwable {
     Properties pr = new Properties();
     InputStream is = null;
 
     try {
       try {
         // The jboss?? way - should work for others as well.
         ClassLoader cl = Thread.currentThread().getContextClassLoader();
         is = cl.getResourceAsStream(name);
       } catch (Throwable clt) {}
 
       if (is == null) {
         // Try another way
         is = Util.class.getResourceAsStream(name);
       }
 
       if (is == null) {
         throw new Exception("Unable to load properties file" + name);
       }
 
       pr.load(is);
 
       //if (debug) {
       //  pr.list(System.out);
       //  Logger.getLogger(Util.class).debug(
       //      "file.encoding=" + System.getProperty("file.encoding"));
       //}
       return pr;
     } finally {
       if (is != null) {
         try {
           is.close();
         } catch (Throwable t1) {}
       }
     }
   }
 
   /** Format a message consisting of a format string
    *
    * @param fmt
    * @param arg
    * @return String formatted message
    */
   public static String fmtMsg(String fmt, String arg) {
     Object[] o = new Object[1];
     o[0] = arg;
 
     return MessageFormat.format(fmt, o);
   }
 
   /** Format a message consisting of a format string plus two string parameters
    *
    * @param fmt
    * @param arg1
    * @param arg2
    * @return String formatted message
    */
   public static String fmtMsg(String fmt, String arg1, String arg2) {
     Object[] o = new Object[2];
     o[0] = arg1;
     o[1] = arg2;
 
     return MessageFormat.format(fmt, o);
   }
 
   /** Format a message consisting of a format string plus one integer parameter
    *
    * @param fmt
    * @param arg
    * @return String formatted message
    */
   public static String fmtMsg(String fmt, int arg) {
     Object[] o = new Object[1];
     o[0] = new Integer(arg);
 
     return MessageFormat.format(fmt, o);
   }
 
   private static final char[] randChars = {
     '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
     'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M',
     'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'};
 
   /** Creates a string of given length where each character comes from a
    * set of values 0-9 followed by A-Z.
    *
    * @param length    returned string will be this long. Less than 1k + 1
    * @param maxVal    maximum ordinal value of characters.  If < than 0,
                       return null.  If > 35, 35 is used instead.
    * @return String   the random string
    */
   public static String makeRandomString(int length, int maxVal) {
     if (length < 0) {
       return null;
     }
 
     length = Math.min(length, 1025);
 
     if (maxVal < 0) {
       return null;
     }
 
     maxVal = Math.min(maxVal, 35);
 
     StringBuffer res = new StringBuffer();
     Random rand = new Random();
 
     for (int i = 0; i <= length; i++) {
       res.append(randChars[rand.nextInt(maxVal + 1)]);
     }
 
     return res.toString();
   }
 
   /** Add a string to a string array of a given maximum length. Truncates
    * the string array if required.
    *
    * New entries go at the end. old get dropped off the front.
    *
    * @param  sarray     String[] to be updated
    * @param  val        new entry
    * @param  maxEntries Number of entries we keep.
    * @return String[]   Modified sarray
    */
   public static String[] appendTextToArray(String[] sarray, String val,
                                     int maxEntries) {
     if (sarray == null) {
       if (maxEntries > 0) {
         sarray = new String[1];
         sarray[0] = val;
       }
       return sarray;
     }
 
     if (sarray.length > maxEntries) {
       String[] neb = new String[maxEntries];
       System.arraycopy(sarray, sarray.length - maxEntries,
                        neb, 0, maxEntries);
       sarray = neb;
       sarray[sarray.length - 1] = val;
       neb = null;
       return sarray;
     }
 
     if (sarray.length < maxEntries) {
       int newLen = sarray.length + 1;
       String[] neb = new String[newLen];
       System.arraycopy(sarray, 0,
                        neb, 0, sarray.length);
       sarray = neb;
       sarray[sarray.length - 1] = val;
       neb = null;
 
       return sarray;
     }
 
     if (maxEntries > 1) {
       System.arraycopy(sarray, 1,
                        sarray, 0, sarray.length - 1);
     }
 
     sarray[sarray.length - 1] = val;
     return sarray;
   }
 
   /** Return a String representing the given String array, achieved by
    * URLEncoding the individual String elements then concatenating with
    *intervening blanks.
    *
    * @param  val    String[] value to encode
    * @return String encoded value
    */
   public static String encodeArray(String[] val){
     if (val == null) {
       return null;
     }
 
     int len = val.length;
 
     if (len == 0) {
       return "";
     }
 
     StringBuffer sb = new StringBuffer();
 
     for (int i = 0; i < len; i++) {
       if (i > 0) {
         sb.append(" ");
       }
 
       String s = val[i];
 
       try {
         if (s == null) {
           sb.append("\t");
         } else {
           sb.append(URLEncoder.encode(s, "UTF-8"));
         }
       } catch (Throwable t) {
         throw new RuntimeException(t);
       }
     }
 
     return sb.toString();
   }
 
   /** Return a StringArray resulting from decoding the given String which
    * should have been encoded by encodeArray
    *
    * @param  val      String value encoded by encodeArray
    * @return String[] decoded value
    */
   public static String[] decodeArray(String val){
     if (val == null) {
       return null;
     }
 
     int len = val.length();
 
     if (len == 0) {
       return new String[0];
     }
 
     ArrayList<String> al = new ArrayList<String>();
     int i = 0;
 
     while (i < len) {
       int end = val.indexOf(" ", i);
 
       String s;
       if (end < 0) {
         s = val.substring(i);
         i = len;
       } else {
         s = val.substring(i, end);
         i = end + 1;
       }
 
       try {
         if (s.equals("\t")) {
           al.add(null);
         } else {
           al.add(URLDecoder.decode(s, "UTF-8"));
         }
       } catch (Throwable t) {
         throw new RuntimeException(t);
       }
     }
 
     return (String[])al.toArray(new String[al.size()]);
   }
 
   /** Return true if Strings are equal including possible null
    *
    * @param thisStr
    * @param thatStr
    * @return boolean true for equal
    */
   public static boolean equalsString(String thisStr, String thatStr) {
     if ((thisStr == null) && (thatStr == null)) {
       return true;
     }
 
     if (thisStr == null) {
       return false;
     }
 
     return thisStr.equals(thatStr);
   }
 
   /** Compare two strings. null is less than any non-null string.
    *
    * @param s1       first string.
    * @param s2       second string.
    * @return int     0 if the s1 is equal to s2;
    *                 <0 if s1 is lexicographically less than s2;
    *                 >0 if s1 is lexicographically greater than s2.
    */
   public static int compareStrings(String s1, String s2) {
     if (s1 == null) {
       if (s2 != null) {
         return -1;
       }
 
       return 0;
     }
 
     if (s2 == null) {
       return 1;
     }
 
     return s1.compareTo(s2);
   }
 
   /** We get a lot of zero length (or all white space) strings in the web world.
    * This will return null for a zero length.
    *
    * @param  val    String request parameter value
    * @return String null for null or zero lengt val, val otherwise.
    */
   public static String checkNull(String val) {
     if (val == null) {
       return null;
     }
 
    if (val.trim().length() == 0) {
       return null;
     }
 
     return val;
   }
 
   /** We get a lot of zero length strings in the web world. This will return
    * false for null or zero-length.
    *
    * @param  val    String request parameter value
    * @return boolean true for length > 0
    */
   public static boolean present(String val) {
     return checkNull(val) != null;
   }
 
   /** Turn a comma separated list into a List.
    * Throws exception for invalid list.
    *
    * @param val     String comma separated list
    * @param emptyOk Empty elements are OK
    * @return List of elements, never null
    * @throws Throwable for invalid list
    */
   public static List<String> getList(String val, boolean emptyOk) throws Throwable {
     List<String> l = new LinkedList<String>();
 
     if ((val == null) || (val.length() == 0)) {
       return l;
     }
 
     StringTokenizer st = new StringTokenizer(val, ",", false);
     while (st.hasMoreTokens()) {
       String token = st.nextToken().trim();
 
       if ((token == null) || (token.length() == 0)) {
         if (!emptyOk) {
           // No empty strings
 
           throw new Exception("List has an empty element.");
         }
         l.add("");
       } else {
         // Got non-empty element
         l.add(token);
       }
     }
 
     return l;
   }
 }
