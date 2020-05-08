 package com.cffreedom.utils;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.math.BigDecimal;
 import java.math.BigInteger;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.List;
 import java.util.Set;
 
 import org.apache.commons.codec.binary.Base64;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * Original Class: com.cffreedom.utils.Convert
  * @author markjacobsen.net (http://mjg2.net/code)
  * Copyright: Communication Freedom, LLC - http://www.communicationfreedom.com
  * 
  * Free to use, modify, redistribute.  Must keep full class header including 
  * copyright and note your modifications.
  * 
  * If this helped you out or saved you time, please consider...
  * 1) Donating: http://www.communicationfreedom.com/go/donate/
  * 2) Shoutout on twitter: @MarkJacobsen or @cffreedom
  * 3) Linking to: http://visit.markjacobsen.net
  * 
  * Changes:
  * 2013-04-24 	markjacobsen.net 	Added toString(InputStream)
  * 2013-04-30 	markjacobsen.net 	Added toArrayListOfStrings()
  * 2013-05-08 	markjacobsen.net 	Added toDate(long)
  * 2013-05-29 	markjacobsen.net 	Handling string dates in the form yyyy-MM-dd better in toDate(val, mask)
  * 2013-06-05 	markjacobsen.net 	Added toBoolean() methods
  * 2013-06-12	markjacobsen.net 	Handling string dates in the form yyyy-MM-dd HH:mm:ss better in toDate(val, mask)
  * 									Added toLong(String) and toString(Date)
  * 2013-07-15	markjacobsen.net 	Added toDelimitedString()
  * 2013-08-27 	markjacobsen.net 	Added toBigInteger()
  * 2013-09-30 	markjacobsen.net 	Added toInteger()
  * 2013-10-06 	markjacobsen.net 	Added toCents()
  * 2013-10-07 	MarkJacobsen.net	Added toBigDecimal()
  * 2013-11-18 	MarkJacobsen.net 	Changed toArrayListOfStrings() to toListOfStrings()
  * 2013-11-24 	MarkJacobsen.net	Added toSHA512()
  * 2013-12-01	MarkJacobsen.net	Additional toBigDecimal() converters
  * 2013-12-13 	MarkJacobsen.net 	Added toString(boolean val)
  * 2014-11-25 	MarkJacobsen.net 	Added toStringList(List<String>, delimiter)
  */
 public class Convert
 {
 	private static final Logger logger = LoggerFactory.getLogger("com.cffreedom.utils.Convert");
 	
 	public static String toBase64(String val)
 	{
 		return new String(Base64.encodeBase64(val.getBytes()));
 	}
 	
 	public static String toSHA512(String val, String salt) throws NoSuchAlgorithmException
 	{
 	    MessageDigest md = MessageDigest.getInstance("SHA-512");
         md.update(salt.getBytes());
         byte[] bytes = md.digest(val.getBytes());
         StringBuilder sb = new StringBuilder();
         for(int i=0; i< bytes.length ;i++)
         {
             sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
         }
         return sb.toString();
 	}
 
 	public static String toMd5(String val) throws NoSuchAlgorithmException
 	{
 		MessageDigest md = MessageDigest.getInstance("MD5");
 		md.update(val.getBytes());
 
 		byte byteData[] = md.digest();
 
 		// convert the byte to hex format method 1
 		StringBuffer sb = new StringBuffer();
 		for (int i = 0; i < byteData.length; i++) {
 			sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
 		}
 
 		logger.debug("Digest(in hex format):: {}", sb.toString());
 
 		// convert the byte to hex format method 2
 		StringBuffer hexString = new StringBuffer();
 		for (int i = 0; i < byteData.length; i++)
 		{
 			String hex = Integer.toHexString(0xff & byteData[i]);
 			if (hex.length() == 1)
 				hexString.append('0');
 			hexString.append(hex);
 		}
 
 		return hexString.toString();
 	}
 	
 	public static String toDelimitedString(String[] vals, String delimiter)
 	{
 		String ret = "";
 		for (String val : vals)
 		{
 			ret += val + delimiter;
 		}
 		return ret.substring(0, ret.length() - 1);
 	}
 	
 	public static String toDelimitedString(Set<String> vals, String delimiter)
 	{
 		String ret = "";
 		for (String val : vals)
 		{
 			ret += val + delimiter;
 		}
 		return ret.substring(0, ret.length() - 1);
 	}
 	
 	public static String toString(Date val) { return toString(val, FormatUtils.MASK_FULL_TIMESTAMP); }
 	public static String toString(Date val, String mask)
 	{
 		return FormatUtils.formatDate(mask, val);
 	}
 
 	public static String toString(Long val)
 	{
 		return String.valueOf(val.longValue());
 	}
 	
 	public static String toString(int val)
     {
         return (new Integer(val)).toString();
     }
 	
 	public static String toString(byte val)
 	{
 		return Byte.toString(val);
 	}
 	
 	public static String toString(byte[] val)
 	{
 		return new String(val);
 	}
 	
 	/**
 	 * Return val as a string (lower case)
 	 * @param val
 	 * @return
 	 */
 	public static String toString(boolean val)
 	{
 		if (val == true) { return "true"; }
 		else { return "false"; }
 	}
 	
 	public static String toString(char val)
 	{
 		return Character.toString(val);
 	}
 	
 	public static String toString(char[] val)
 	{
 		return new String(val);
 	}
 	
 	public static String toString(InputStream val) throws IOException
 	{
 		StringBuilder sb = new StringBuilder();
 		String line = null;
 		BufferedReader br = new BufferedReader(new InputStreamReader(val));
 		while ((line = br.readLine()) != null) {
 			sb.append(line);
 		}
 		return sb.toString();
 	}
 	
 	/**
 	 * Return a delimited string list of values
 	 * @param vals
 	 * @return
 	 */
 	public static String toStringList(List<String> vals, String delimiter)
 	{
 		if (vals == null) { return null; }
 		
 		String ret = "";
 		
		for (String val : vals)
 		{
			ret += val + delimiter;
 		}
		
		return ret.substring(0, ret.length() - delimiter.length());
 	}
 	
 	public static List<String> toListOfStrings(String[] vals)
 	{
 		List<String> ret = new ArrayList<String>();
 		for (int x = 0; x < vals.length; x++)
 		{
 			ret.add(vals[x]);
 		}
 		return ret;
 	}
 	
 	//------------------------------------------------------------------
 	// Int methods
 	public static int toInt(String val)
 	{
 		return (new Integer(val)).intValue();	
 	}
 	
 	public static int toInt(long val)
 	{
 		return (new Long(val)).intValue();	
 	}
 	
 	public static int toInt(double val)
 	{
 		return (new Double(val)).intValue();	
 	}
 	
 	public static int toInt(boolean val)
 	{
 	    if (val == true)
 	    {
 	        return 1;
 	    }else{
 	        return 0;
 	    }
 	}
 	
 	public static int[] toIntArray(String[] vals)
 	{
 		int[] retArray = new int[vals.length];
 		for (int x = 0; x < vals.length; x++)
 		{
 			retArray[x] = toInt(vals[x]);
 		}
 		return retArray;	
 	}
 
 	public static Integer toInteger(int val)
 	{
 		return new Integer(val);
 	}
 	
 	//------------------------------------------------------------------
     // Boolean methods
 	public static boolean toBoolean(int val)
 	{
 		if (val == 0){
 			return false;
 		}else{
 			return true;
 		}
 	}
 	
 	public static boolean toBoolean(String val)
 	{
 		if  (
 			(val == null) ||
 			(val.trim().length() == 0) ||
 			(val.substring(0, 1).equalsIgnoreCase("0") == true) ||  // Note: that's a zero
 			(val.substring(0, 1).equalsIgnoreCase("N") == true) ||
 			(val.substring(0, 1).equalsIgnoreCase("F") == true)
 			)
 		{
 			return false;
 		}
 		else
 		{
 			return true;
 		}
 	}
 	
 	//------------------------------------------------------------------
     // Long methods
 	public static long toLong(String val)
 	{
 		return (new Long(val)).longValue();	
 	}
 	
 	public static long toLong(int val)
 	{
 		return (new Long(val)).longValue();
 	}
 	
     //------------------------------------------------------------------
     // Calendar methods
     public static Calendar toCalendar(String val, String mask) throws ParseException
     {
     	if (val == null) return null;
     	try {
 	    	Calendar cal = Calendar.getInstance();
 	        cal.setTime(toDate(val, mask));
 	        return cal;
     	} catch (Exception e) { logger.error("Not a date {}", val); return null; }
     }
    
     public static Calendar toCalendar(java.util.Date val)
     {
     	if (val == null) return null;
     	try {
 	    	Calendar cal = Calendar.getInstance();
 	        cal.setTime(val);
 	        return cal;
     	} catch (Exception e) { logger.error("Not a date {}", val); return null; }
     }
    
     //------------------------------------------------------------------
     // Date/Time methods           
     public static java.util.Date toDate(Calendar val)
     {
     	if (val == null) return null;
         try {
             return val.getTime();
         } catch (Exception e) { logger.error("Not a date {}", val); return null; }
     }
    
     public static java.util.Date toDate(java.sql.Date val)
     {
     	if (val == null) return null;
         try {
             return (java.util.Date)val;
         } catch (Exception e) { logger.error("Not a date {}", val); return null; }
     }
    
     public static java.util.Date toDate(String val)
     {
     	return toDate(val, DateTimeUtils.MASK_DEFAULT_DATE);
     }
     
     public static java.util.Date toDate(long val)
     {
     	return new Date(val);
     }
    
     public static java.util.Date toDate(String val, String mask)
     {
         String retVal = val;
        
         try
         {
         	if (mask.compareTo(DateTimeUtils.MASK_DEFAULT_DATE) == 0)
             {
         		String[] parts = val.split("/"); // split(a_sVal, "/");
                 if ( (parts.length == 3) && (parts[2].length() != 4) )
                 {
                 	String year = toString(DateTimeUtils.year(new java.util.Date()));
                     year = year.substring(0, 4 - parts[2].length());
                     year = year + parts[2];
                     retVal = parts[0] + "/" + parts[1] + "/" + year;
                 }
             }
         	else if (mask.compareTo(DateTimeUtils.MASK_FILE_DATESTAMP) == 0)
         	{
         		String tmp = val.substring(5, val.length()) + "-" + val.substring(0, 4);
         		retVal = tmp.replace('-', '/');
         		mask = DateTimeUtils.MASK_DEFAULT_DATE; // Have to reset it to parse correctly
         	}
         	else if (mask.compareTo(DateTimeUtils.MASK_FULL_TIMESTAMP) == 0)
         	{
         		String tmp = val.substring(5, 10) + "-" + val.substring(0, 4) + " " + val.substring(11, val.length());
         		retVal = tmp.replace('-', '/');
         		mask = "MM/dd/yyyy HH:mm:ss"; // Have to use to parse correctly
         	}
                    
             DateFormat df = new SimpleDateFormat(mask);
             return df.parse(retVal);
         }
         catch (Exception e) { e.printStackTrace(); return null; }
     }
     
     public static java.util.Date toDateNoTime(java.util.Date val)
     {
     	return toDate(DateTimeUtils.dateFormat(val));
     }
    
     public static java.util.Date[] toDateArray(String[] vals) throws ParseException
     {
         return toDateArray(vals, DateTimeUtils.MASK_DEFAULT_DATE);
     }
                
     public static java.util.Date[] toDateArray(String[] vals, String mask) throws ParseException
     {
         java.util.Date[] dateArray = new java.util.Date[vals.length];
         for (int x = 0; x < vals.length; x++)
         {
         	dateArray[x] = toDate(vals[x], mask);
         }
         return dateArray;         
     }
    
     @SuppressWarnings("deprecation")
 	public static java.sql.Date toSqlDate(String val)
     {
         try {
             return new java.sql.Date(java.sql.Date.parse(val));
         } catch (Exception e) { return null; }
     }
    
     public static java.sql.Date toSqlDate(java.util.Date val)
     {
         try {
             return (java.sql.Date)val;
         } catch (Exception e) { return null; }
     }
    
     public static java.util.Date toTime(String val) throws Exception
     {
         try {
         	DateFormat df = new SimpleDateFormat(DateTimeUtils.MASK_TIME_12_HOUR);
             return df.parse(val);
         } catch (Exception e) { return null; }
     }
    
     /***
     * This function converts a standard java.util.Date to a
      * DB2 Formated date.
      * @param inDate java.util.Date to convert to DB2 date string
     * @return DB2 date string
     */
     public static String toDB2DateString(java.util.Date val){
         DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
         return df.format(val);
     }
    
     /***
     * This function converts a standard java.sql.Date to a
      * DB2 Formated date.
      * @param inDate java.sql.Date to convert to DB2 date string
     * @return DB2 date string
     */       
     public static String toDB2DateString(java.sql.Date val){
        
         return val.toString();
     }          
     /***
     * This function converts a standard java.util.Date to a
      * DB2 Formated date.
      * @param inDate java.util.Date to convert to DB2 date string
     * @return DB2 date string
     * @throws ParseException
     */
     public static java.sql.Date toDB2Date(String val) throws ParseException{
         DateFormat df;
         if (val.trim().charAt(4)=='-'){
             df = new SimpleDateFormat("yyyy-MM-dd");
         } else {
             df = new SimpleDateFormat("MM/dd/yyyy");
         }
        
         java.util.Date tempdate = df.parse(val);
         java.sql.Date db2date = (java.sql.Date)tempdate;
 
         return db2date;
     }
    
     /**
     * This function converts a standard java.util.Date to a
     * java.sql.Timestamp suitable for a db TIMESTAMP or DATETIME
     * @param a_dVal java.util.Date to convert to a Timestamp object
     * @return java.sql.Timestamp object
     */
     public static java.sql.Timestamp toTimestamp(java.util.Date val)
     {
     	if (val == null) {
     		return null;
     	} else {
     		return new java.sql.Timestamp(val.getTime());
     	}
     }
     
     public static java.sql.Timestamp toTimestamp(java.util.Calendar val)
     {
     	return toTimestamp(Convert.toDate(val));
     }
     
     public static BigDecimal toBigDecimal(String val)
     {
     	return new BigDecimal(val);
     }
     
     public static BigDecimal toBigDecimal(int val)
     {
     	return new BigDecimal(val);
     }
     
     public static BigDecimal toBigDecimal(long val)
     {
     	return new BigDecimal(val);
     }
     
     public static BigDecimal toBigDecimal(double val)
     {
     	return BigDecimal.valueOf(val);
     }
     
     public static BigDecimal toBigDecimalFromCents(long cents)
     {
     	return toBigDecimal(cents).divide(toBigDecimal(100));
     }
     
     public static BigInteger toBigInteger(long val)
     {
     	return new BigInteger(toString(val));
     }
     
     public static BigInteger toBigInteger(int val)
     {
     	return new BigInteger(toString(val));
     }
 
     public static int toCents(BigDecimal dollarAmt)
 	{
 		return dollarAmt.movePointRight(2).intValue();
 	}
     
     public static int toCents(double dollarAmount)
     {
     	return toInt(dollarAmount * 100);
     }
     
     public static int toCents(float dollarAmount)
     {
     	return toInt(dollarAmount * 100);
     }
 }
