 /**
  * The contents of this file are subject to the Mozilla Public
  * License Version 1.1 (the "License"); you may not use this file
  * except in compliance with the License. You may obtain a copy of
  * the License at http://www.mozilla.org/MPL/
  *
  * Software distributed under the License is distributed on an "AS
  * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
  * implied. See the License for the specific language governing
  * rights and limitations under the License.
  *
  * The Original Code is "EINRC-4 / Meta Project".
  *
  * The Initial Developer of the Original Code is TietoEnator.
  * The Original Code code was developed for the European
  * Environment Agency (EEA) under the IDA/EINRC framework contract.
  *
  * Copyright (C) 2000-2002 by European Environment Agency.  All
  * Rights Reserved.
  *
  * Original Code: Jaanus Heinlaid (TietoEnator)
  */
  
 package eionet.util;
 
 import java.io.*;
 import java.net.*;
 import java.util.*;
 import java.security.*;
 
 //import eionet.meta.Log;
 
 /**
  * This is a class containing several useful utility methods.
  *
  * @author Jaanus Heinlaid
  */
 public class Util {
 	
 	private static final int BUF_SIZE = 1024;
     
     /**
      * A method for determining if a String is void.
      * And by void we mean either null or zero-length.
      * Returns true, if the string IS void.
      */
      
     public static boolean voidStr(String s){
         if (s == null)
             return true;
         if (s.length() == 0)
             return true;
         
         return false;
     }
     
     /**
      * A method for calculating and formatting the current
      * date and time into a String for a log.
      */
      
     public static String logTime(){
         
         Date date = new Date();
         String month = String.valueOf(date.getMonth());
         month = (month.length() < 2) ? ("0" + month) : month;
         String day = String.valueOf(date.getDate());
         day = (day.length() < 2) ? ("0" + day) : day;
         String hours = String.valueOf(date.getHours());
         hours = (hours.length() < 2) ? ("0" + hours) : hours;
         String minutes = String.valueOf(date.getMinutes());
         minutes = (minutes.length() < 2) ? ("0" + minutes) : minutes;
         String seconds = String.valueOf(date.getSeconds());
         seconds = (seconds.length() < 2) ? ("0" + seconds) : seconds;
         
         String time = "[" + month;
         time = time + "/" + day;
         time = time + " " + hours;
         time = time + ":" + minutes;
         time = time + ":" + seconds;
         time = time + "] ";
         
         return time;
     }
     
     /**
      * A method for formatting the given timestamp into a String
      * for history.
      */
      
     public static String historyDate(long timestamp){
         
         Date date = new Date(timestamp);
         String year = String.valueOf(1900 + date.getYear());
        String month = String.valueOf(date.getMonth() + 1);
         month = (month.length() < 2) ? ("0" + month) : month;
         String day = String.valueOf(date.getDate());
         day = (day.length() < 2) ? ("0" + day) : day;
         String hours = String.valueOf(date.getHours());
         hours = (hours.length() < 2) ? ("0" + hours) : hours;
         String minutes = String.valueOf(date.getMinutes());
         minutes = (minutes.length() < 2) ? ("0" + minutes) : minutes;
         String seconds = String.valueOf(date.getSeconds());
         seconds = (seconds.length() < 2) ? ("0" + seconds) : seconds;
         
         String time = year;
         time = time + "/" + month;
         time = time + "/" + day;
         time = time + " " + hours;
         time = time + ":" + minutes;
         
         return time;
     }
 
 	/**
 	 * A method for formatting the given timestamp into a String released_datasets.jsp.
 	 */
      
 	public static String releasedDate(long timestamp){
         
 		Date date = new Date(timestamp);
 		
 		String year = String.valueOf(1900 + date.getYear());
 		String month = String.valueOf(date.getMonth());
 		String day = String.valueOf(date.getDate());
 		day = (day.length() < 2) ? ("0" + day) : day;
 		
 		Hashtable months = new Hashtable();
 		months.put("0", "January");
 		months.put("1", "February");
 		months.put("2", "March");
 		months.put("3", "April");
 		months.put("4", "May");
 		months.put("5", "June");
 		months.put("6", "July");
 		months.put("7", "August");
 		months.put("8", "September");
 		months.put("9", "October");
 		months.put("10", "November");
 		months.put("11", "December");
 		
 		String time = day + " " + months.get(month) + " " + year;
 		return time;
 	}
 
 	/**
 	 * 
 	 */
      
 	public static String pdfDate(long timestamp){
         
 		Date date = new Date(timestamp);
 		
 		String year = String.valueOf(1900 + date.getYear());
 		String month = String.valueOf(date.getMonth() + 1);
 		month = (month.length() < 2) ? ("0" + month) : month;
 		String day = String.valueOf(date.getDate());
 		day = (day.length() < 2) ? ("0" + day) : day;
 		
 		return day + "/" + month + "/" + year;
 	}
 
     /**
      * A method for calculating time difference in MILLISECONDS,
      * between a date-time specified in input parameters and the
      * current date-time.<BR><BR>
      * This should be useful for calculating sleep time for code
      * that has a certain schedule for execution.
      *
      * @param   hour    An integer from 0 to 23. If less than 0
      *                  or more than 23, then the closest next
      *                  hour to current hour is taken.
      * @param   date    An integer from 1 to 31. If less than 1
      *                  or more than 31, then the closest next
      *                  date to current date is taken.
      * @param   month   An integer from Calendar.JANUARY to Calendar.DECEMBER.
      *                  If out of those bounds, the closest next
      *                  month to current month is taken.
      * @param   wday    An integer from 1 to 7. If out of those bounds,
      *                  the closest next weekday to weekday month is taken.
      * @param   zone    A String specifying the time-zone in which the
      *                  calculations should be done. Please see Java
      *                  documentation an allowable time-zones and  formats.
      * @return          Time difference in milliseconds.
      */
     public static long timeDiff(int hour, int date, int month, int wday, String zone){
         
         GregorianCalendar cal = new GregorianCalendar(TimeZone.getTimeZone(zone));
         if (cal == null)
             cal = new GregorianCalendar(TimeZone.getDefault());
         cal.set(Calendar.MINUTE, 0);
         cal.set(Calendar.SECOND, 0);
         cal.set(Calendar.MILLISECOND, 0);
         
         cal.setFirstDayOfWeek(Calendar.MONDAY);
         
         /* here we force the hour to be one of the defualts
         if (hour < 0) hour = 0;
         if (hour > 23) hour = 23;
         */
         int cur_hour = cal.get(Calendar.HOUR);
         
         if (cal.get(Calendar.AM_PM) == Calendar.PM)
             cur_hour = 12 + cur_hour;
         
         // here we assume that every full hour is accepted
         /*if (hour < 0 || hour > 23){
             
             hour = cur_hour>=23 ? 0 : cur_hour + 1;
         }*/
         
         if (wday >= 1 && wday <= 7){
             
             int cur_wday = cal.get(Calendar.DAY_OF_WEEK);
             if (hour < 0 || hour > 23){
                 if (cur_wday != wday)
                     hour = 0;
                 else
                     hour = cur_hour>=23 ? 0 : cur_hour + 1;
             }
             
             int amount = wday-cur_wday;
             if (amount < 0) amount = 7 + amount;
             if (amount == 0 && cur_hour >= hour) amount = 7;
             cal.add(Calendar.DAY_OF_WEEK, amount);
         }
         // do something about when every date is accepted
         else if (month >= Calendar.JANUARY && month <= Calendar.DECEMBER){
             if (date < 1) date = 1;
             if (date > 31) date = 31;
             int cur_month = cal.get(Calendar.MONTH);
             int amount = month-cur_month;
             if (amount < 0) amount = 12 + amount;
             if (amount == 0){
                 if (cal.get(Calendar.DATE) > date)
                     amount = 12;
                 else if (cal.get(Calendar.DATE) == date){
                     if (cur_hour >= hour)
                         amount = 12;
                 }
             }
             //cal.set(Calendar.DATE, date);
             cal.add(Calendar.MONTH, amount);
             if (date > cal.getActualMaximum(Calendar.DATE))
                 date = cal.getActualMaximum(Calendar.DATE);
             cal.set(Calendar.DATE, date);
         }
         else if (date >= 1 && date <= 31){
             int cur_date = cal.get(Calendar.DATE);
             if (cur_date > date)
                 cal.add(Calendar.MONTH, 1);
             else if (cur_date == date){
                 if (cur_hour >= hour)
                     cal.add(Calendar.MONTH, 1);
             }
             cal.set(Calendar.DATE, date);
         }
         else{
             if (hour < 0 || hour > 23){
                 hour = cur_hour>=23 ? 0 : cur_hour + 1;
             }
             if (cur_hour >= hour) cal.add(Calendar.DATE, 1);
         }
         
         if (hour >= 12){
             cal.set(Calendar.HOUR, hour - 12);
             cal.set(Calendar.AM_PM, Calendar.PM);
         }
         else{
             cal.set(Calendar.HOUR, hour);
             cal.set(Calendar.AM_PM, Calendar.AM);
         }
         
         Date nextDate = cal.getTime();
         Date currDate = new Date();
 
         long nextTime = cal.getTime().getTime();
         long currTime = (new Date()).getTime();
 
         return nextTime-currTime;
     }
 
     /**
      * A method for counting occurances of a substring in a string.
      */
     public static int countSubString(String str, String substr){
         int count = 0;
         while (str.indexOf(substr) != -1) {count++;}
         return count;
     }
     
     /**
      * A method for creating a unique digest of a String message.
      *
      * @param   src         String to be digested.
      * @param   algosrithm  Digesting algorithm (please see Java
      *                      documentation for allowable values).
      * @return              A unique String-typed digest of the input message.
      */
     public static String digest(String src, String algorithm) throws GeneralSecurityException{
         
         byte[] srcBytes = src.getBytes();
         byte[] dstBytes = new byte[16];
         
         MessageDigest md = MessageDigest.getInstance(algorithm);
         md.update(srcBytes);
         dstBytes = md.digest();
         md.reset();
         
         StringBuffer buf = new StringBuffer();
         for (int i=0; i<dstBytes.length; i++){
             Byte byteWrapper = new Byte(dstBytes[i]);
             buf.append(String.valueOf(byteWrapper.intValue()));
         }
         
         return buf.toString();
     }
     
     /**
      * A method for creating a unique Hexa-Decimal digest of a String message.
      *
      * @param   src         String to be digested.
      * @param   algosrithm  Digesting algorithm (please see Java
      *                      documentation for allowable values).
      * @return              A unique String-typed Hexa-Decimal digest of the input message.
      */
     public static String digestHexDec(String src, String algorithm) throws GeneralSecurityException {
         
         byte[] srcBytes = src.getBytes();
         byte[] dstBytes = new byte[16];
         
         MessageDigest md = MessageDigest.getInstance(algorithm);
         md.update(srcBytes);
         dstBytes = md.digest();
         md.reset();
         
         StringBuffer buf = new StringBuffer();
         for (int i=0; i<dstBytes.length; i++){
             Byte byteWrapper = new Byte(dstBytes[i]);
             int k = byteWrapper.intValue();
             String s = Integer.toHexString(byteWrapper.intValue());
             if (s.length() == 1) s = "0" + s;
             buf.append(s.substring(s.length() - 2));
         }
         
         return buf.toString();
     }
     
     ///
     
     public static String strLiteral(String in) {
     in = (in != null ? in : "");
     StringBuffer ret = new StringBuffer("'");
 
     for (int i = 0; i < in.length(); i++) {
       char c = in.charAt(i);
       if (c == '\'')
         ret.append("''");
       else
         ret.append(c);
     }
     ret.append('\'');
 
     return ret.toString();
   }
 
     /**
      * A method for replacing < > tags in string for web layout
      */
     public static String replaceTags(String in) {
         return replaceTags(in, false);
     }
     public static String replaceTags(String in, boolean inTextarea) {
 	    in = (in != null ? in : "");
 	
 	
 	    StringBuffer ret = new StringBuffer();
 	
 	    for (int i = 0; i < in.length(); i++) {
 	      char c = in.charAt(i);
 	      if (c == '<')
 	        ret.append("&lt;");
 	      else if (c == '>')
 	        ret.append("&gt;");
 	      else if (c == '\n' && inTextarea==false)
 	        ret.append("<BR>");
 	      else
 	        ret.append(c);
 	    }
 	    String retString = ret.toString();
 	    if (inTextarea == false)
 	        retString=setAnchors(retString, true, 50);
 	
 	    return retString;
 	}
 	
     /**
      * A method for replacing substrings in string
      */
     public static String Replace(String str, String oldStr, String replace) {
         str = (str != null ? str : "");
 
         StringBuffer buf = new StringBuffer();
         int found = 0;
         int last=0;
 
         while ((found = str.indexOf(oldStr, last)) >= 0) {
             buf.append(str.substring(last, found));
             buf.append(replace);
             last = found+oldStr.length();
         }
         buf.append(str.substring(last));
         return buf.toString();
 	}
 
 	/**
 	* Finds all urls in a given string and replaces them with HTML anchors.
 	* If boolean newWindow==true then target will be a new window, else no.
 	* If boolean cutLink>0 then cut the displayed link lenght cutLink.
 	*/
 	public static String setAnchors(String s, boolean newWindow, int cutLink){
 
 		StringBuffer buf = new StringBuffer();
         
 		StringTokenizer st = new StringTokenizer(s, " \t\n\r\f", true);
 		while (st.hasMoreTokens()) {
 			String token = st.nextToken();
 			if (!isURL(token))
 				buf.append(token);
 			else{
 				StringBuffer _buf = new StringBuffer("<a ");
 				if (newWindow) _buf.append("target=\"_blank\" ");
 				_buf.append("href=\"");
 				_buf.append(token);
 				_buf.append("\">");
 				
 				if (cutLink<token.length())
 					_buf.append(token.substring(0, cutLink)).append("...");
 				else
 					_buf.append(token);
 					
 				_buf.append("</a>");
 				buf.append(_buf.toString());
 			}
 		}
         
 		return buf.toString();
 	}
       
     /**
     * Finds all urls in a given string and replaces them with HTML anchors.
     * If boolean newWindow==true then target will be a new window, else no.
     */
     public static String setAnchors(String s, boolean newWindow){
         
         return setAnchors(s, newWindow, 0);
     }
   
     /**
     * Finds all urls in a given string and replaces them with HTML anchors
     * with target being a new window.
     */
     public static String setAnchors(String s){
         
         return setAnchors(s, true);
     }
     
     /**
     * Checks if the given string is a well-formed URL
     */
     public static boolean isURL(String s){
         try {
             URL url = new URL(s);
         }
         catch (MalformedURLException e){
             return false;
         }
         
         return true;
     }
     
     /**
     *
     */
     public static boolean implementsIF(Class c, String ifName){
         
         boolean f = false;
         Class[] ifs = c.getInterfaces();
         for (int i=0; ifs!=null && i<ifs.length; i++){
             Class ifClass = ifs[i];
             if (ifClass.getName().endsWith(ifName))
                 return true;
         }
         
         return f;
     }
     
     /*
      * Return's a throwable's stack trace in a string 
      */
     public static String getStack(Throwable t){
 		ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
 		t.printStackTrace(new PrintStream(bytesOut));
 		return bytesOut.toString();
     }
 
 	/*
 	 * Return's indicator-image name according to given status 
 	 */
 	public static String getStatusImage(String status){
 		
 		if (status==null) status = "Incomplete";
 		
 		if (status.equals("Incomplete"))
 			return "dd_status_1.gif";
 		else if (status.equals("Candidate"))
 			return "dd_status_2.gif";
 		else if (status.equals("Recorded"))
 			return "dd_status_3.gif";
 		else if (status.equals("Qualified"))
 			return "dd_status_4.gif";
 		else if (status.equals("Released"))
 			return "dd_status_5.gif";
 		else
 			return "dd_status_1.gif";
 	}
 
 	/*
 	 * Return's a sequence of radics illustrating the given status 
 	 */
 	public static String getStatusRadics(String status){
 		
 		if (status==null) status = "Incomplete";
 		
 		if (status.equals("Incomplete"))
 			return "&radic;";
 		else if (status.equals("Candidate"))
 			return "&radic;&radic;";
 		else if (status.equals("Recorded"))
 			return "&radic;&radic;&radic;";
 		else if (status.equals("Qualified"))
 			return "&radic;&radic;&radic;&radic;";
 		else if (status.equals("Released"))
 			return "&radic;&radic;&radic;&radic;&radic;";
 		else
 			return "&radic;";
 	}
 
 	/*
 	 * Return's a sortable string of the given status, taking into account
 	 * the business-logical order of statuses   
 	 */
 	public static String getStatusSortString(String status){
 		
 		if (status==null) status = "Incomplete";
 		
 		if (status.equals("Incomplete"))
 			return "1";
 		else if (status.equals("Candidate"))
 			return "2";
 		else if (status.equals("Recorded"))
 			return "3";
 		else if (status.equals("Qualified"))
 			return "4";
 		else if (status.equals("Released"))
 			return "5";
 		else
 			return "1";
 	}
 
 	/*
 	 * 
 	 */
 	public static String getIcon(String path){
 		
 		String s = path==null ? null : path.toLowerCase();
 		
 		if (s==null)
 			return "icon_unknown.gif";
 		else if (s.endsWith(".pdf"))
 			return "icon_pdf.jpg";
 		else if (s.endsWith(".doc"))
 			return "icon_doc.gif";
 		else if (s.endsWith(".rtf"))
 			return "icon_doc.gif";
 		else if (s.endsWith(".xls"))
 			return "icon_xls.gif";
 		else if (s.endsWith(".ppt"))
 			return "icon_ppt.gif";
 		else if (s.endsWith(".txt"))
 			return "icon_txt.gif";
 		else if (s.endsWith(".zip"))
 			return "icon_zip.gif";
 		else if (s.endsWith(".htm"))
 			return "icon_html.gif";
 		else if (s.endsWith(".html"))
 			return "icon_html.gif";
 		else if (s.endsWith(".xml"))
 			return "icon_xml.jpg";
 		else if (s.endsWith(".xsd"))
 			return "icon_xml.jpg";
 		else
 			return "icon_unknown.gif";
 	}
 
 	/**
 	 * Method used in JSP to determine weather the row with a given index is
 	 * odd or even. Returns a String used by JSP to set the style correspondingly
 	 * and with as little code as possible.
 	 */
 	public static String isOdd(int displayed){
 		String isOdd = (displayed % 2 != 0) ? "_odd" : "";
 		return isOdd;
 	}
 
 	/**
 	 *  
 	 */	
 	public static String getUrlContent(String url){
 
 		int i;
 		byte[] buf = new byte[1024];		
 		InputStream in = null;
 		ByteArrayOutputStream out = new ByteArrayOutputStream();
 
 		try{
 			URL _url = new URL(url);
 			HttpURLConnection httpConn = (HttpURLConnection)_url.openConnection();
 					
 			in = _url.openStream();
 			while ((i=in.read(buf, 0, buf.length)) != -1){
 				out.write(buf, 0, i);
 			}
 			out.flush();
 		}
 		catch (IOException e){
 			return e.toString().trim();
 		}
 		finally{
 			try{
 				if (in!=null) in.close();
 				if (out!=null) out.close();
 			}
 			catch (IOException e){}
 		}
 		
 		return out.toString().trim();
 	}
     
     /**
     * Converts HTML/XML escape sequences (a la &#147; or &amp;)
     * in the given to UNICODE.
     */
     public static String escapesToUnicode(String literal) {
         
         return literal;
         
         /*if (literal == null)
             return null;
         
         UnicodeEscapes unicodeEscapes = null;
         
         StringBuffer buf = new StringBuffer();
         for (int i=0; i<literal.length(); i++){
             
             char c = literal.charAt(i);
             
             if (c=='&'){
                 int j = literal.indexOf(";", i);
                 if (j > i){
                     char cc = literal.charAt(i+1);
                     int decimal = -1;
                     if (cc=='#'){
                         // handle Unicode decimal escape
                         String sDecimal = literal.substring(i+2, j);
                         
                         try{
                             decimal = Integer.parseInt(sDecimal);
                         }
                         catch (Exception e){}
                     }
                     else{
                         // handle entity
                         String ent = literal.substring(i+1, j);
                         if (unicodeEscapes == null)
                             unicodeEscapes = new UnicodeEscapes();
                         decimal = unicodeEscapes.getDecimal(ent);
                     }
                     
                     if (decimal >= 0){
                         // if decimal was found, use the corresponding char. otherwise stick to c.
                         c = (char)decimal;
                         i = j;
                     }
                 }
             }
             
             buf.append(c);
         }
         
         return buf.toString();*/
     }
     
 	public static void write(InputStream in, OutputStream out) throws IOException{
 		
 		int i = 0;
 		byte[] buf = new byte[BUF_SIZE];
 		
 		try{
 			while ((i=in.read(buf, 0, buf.length)) != -1){
 				out.write(buf, 0, i);
 			}
 		}
 		finally{
 			if (in!=null) in.close();
 			out.close();
 		}
 	}
 
     /**
     * main
     */
     public static void main(String[] args){
 
     	String s = getUrlContent("http://www.eionet.eu.int/boxes/DD/box1/view_teaser_box?vis=standard&width=270");
 		System.out.println(s);
     }
 }
