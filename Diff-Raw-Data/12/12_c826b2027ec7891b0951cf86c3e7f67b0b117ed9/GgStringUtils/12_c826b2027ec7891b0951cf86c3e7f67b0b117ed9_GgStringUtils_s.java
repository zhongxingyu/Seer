 /**
    This file is part of GoldenGate Project (named also GoldenGate or GG).
 
    Copyright 2009, Frederic Bregier, and individual contributors by the @author
    tags. See the COPYRIGHT.txt in the distribution for a full listing of
    individual contributors.
 
    All GoldenGate Project is free software: you can redistribute it and/or 
    modify it under the terms of the GNU General Public License as published 
    by the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.
 
    GoldenGate is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.
 
    You should have received a copy of the GNU General Public License
    along with GoldenGate .  If not, see <http://www.gnu.org/licenses/>.
  */
 package goldengate.common.utility;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.nio.charset.Charset;
 import java.sql.Timestamp;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 import org.dom4j.Node;
 
 import goldengate.common.exception.FileTransferException;
 import goldengate.common.exception.InvalidArgumentException;
 import goldengate.common.logging.GgInternalLogger;
 import goldengate.common.logging.GgInternalLoggerFactory;
 
 /**
  * Various utilities for reading files, transforming dates, ...
  * 
  * @author Frederic Bregier
  *
  */
 public class GgStringUtils {
     /**
      * Internal Logger
      */
     private static final GgInternalLogger logger = GgInternalLoggerFactory
             .getLogger(GgStringUtils.class);
     
     /**
      * Format used for Files
      */
     public static final Charset UTF8 = Charset.forName("UTF-8");
     
     /**
      * Read a file and return its content in String format
      * @param filename
      * @return the content of the File in String format
      * @throws InvalidArgumentException for File not found
      * @throws FileTransferException for reading exception
      */
     public static String readFileException(String filename) throws InvalidArgumentException, FileTransferException {
         File file = new File(filename);
         char [] chars = new char[(int) file.length()];
         FileReader fileReader;
         try {
             fileReader = new FileReader(file);
         } catch (FileNotFoundException e) {
             logger.error("File not found while trying to access: "+filename, e);
             throw new InvalidArgumentException("File not found while trying to access",e);
             //return null;
         }
         try {
             fileReader.read(chars);
         } catch (IOException e) {
             logger.error("Error on File while trying to read: "+filename, e);
             throw new FileTransferException("Error on File while trying to read",e);
             //return null;
         }
         return new String(chars);
     }
     /**
      * Read file and return "" if an error occurs
      * @param filename
      * @return the string associated with the file, or "" if an error occurs
      */
     public static String readFile(String filename) {
         try {
             return readFileException(filename);
         } catch (InvalidArgumentException e) {
             logger.error("Error while trying to open: "+filename,e);
             return "";
         } catch (FileTransferException e) {
             logger.error("Error while trying to read: "+filename,e);
             return "";
         }
     }
     /**
      * Simple Date format
      */
     private static SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmssSSS");
     /**
      * Get a date in String and return the corresponding Timestamp
      * @param date
      * @return the corresponding Timestamp
      */
     public static Timestamp fixDate(String date) {
         Timestamp tdate = null;
         date = date.replaceAll("/|:|\\.| |-", "");
         if (date.length() > 0) {
             if (date.length() < 15) {
                 int len = date.length();
                 date += "000000000000000".substring(len);
             }
             try {
                 Date ddate = format.parse(date);
                 tdate = new Timestamp(ddate.getTime());
             } catch (ParseException e) {
                logger.info("start",e);
             }
         }
         return tdate;
     }
     /**
      * From a date in String format and a Timestamp, return the Timestamp as :<br>
      * if before = null as date<br>
      * if before != null and before < date, as date<br>
      * if before != null and before >= date, as before end of day (23:59:59:9999)
      * @param date
      * @param before
      * @return the end date
      */
     public static Timestamp fixDate(String date, Timestamp before) {
         Timestamp tdate = null;
         date = date.replaceAll("/|:|\\.| |-", "");
         if (date.length() > 0) {
             if (date.length() < 15) {
                 int len = date.length();
                 date += "000000000000000".substring(len);
             }
             try {
                 Date ddate = format.parse(date);
                 if (before != null) {
                     Date bef = new Date(before.getTime());
                     if (bef.compareTo(ddate) >= 0) {
                         ddate = new Date(bef.getTime()+1000*3600*24-1);
                     }
                 }
                 tdate = new Timestamp(ddate.getTime());
             } catch (ParseException e) {
                logger.info("start",e);
             }
         }
         return tdate;
     }
     
     /**
      * Read a boolean value (0,1,true,false) from a node
      * @param node
      * @return the corresponding value
      */
     public static boolean getBoolean(Node node) {
         String val = node.getText();
         boolean bval;
         try {
             int ival = Integer.parseInt(val);
             bval = (ival == 1) ? true : false;
         } catch (NumberFormatException e) {
             bval = Boolean.parseBoolean(val);
         }
         return bval;
     }
     /**
      * Read an integer value from a node
      * @param node
      * @return the corresponding value
      * @throws InvalidArgumentException 
      */
     public static int getInteger(Node node) throws InvalidArgumentException {
         if (node == null)
             throw new InvalidArgumentException("Node empty");
         String val = node.getText();
         int ival;
         try {
             ival = Integer.parseInt(val);
         } catch (NumberFormatException e) {
             throw new InvalidArgumentException("Incorrect value");
         }
         return ival;
     }
     /**
      * Make a replacement of first "find" string by "replace" string into the StringBuilder
      * @param builder
      * @param find
      * @param replace
      */
     public static boolean replace(StringBuilder builder, String find, String replace) {
         int start = builder.indexOf(find);
         if (start == -1) {
             return false;
         }
         int end = start+find.length();
         builder.replace(start, end, replace);
         return true;
     }
     /**
      * Make replacement of all "find" string by "replace" string into the StringBuilder
      * @param builder
      * @param find
      * @param replace
      */
     public static void replaceAll(StringBuilder builder, String find, String replace) {
         while (replace(builder, find, replace)) {
         }
     }
 }
