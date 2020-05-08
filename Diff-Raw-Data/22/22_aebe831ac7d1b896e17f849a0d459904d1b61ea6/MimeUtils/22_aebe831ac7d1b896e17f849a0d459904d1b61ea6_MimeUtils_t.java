 package edu.mayo.phenoportal.server.utils;
 
 import java.io.File;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.logging.Logger;
 
 import edu.mayo.phenoportal.server.exception.PhenoportalFileException;
 import eu.medsea.mimeutil.MimeType;
 import eu.medsea.mimeutil.MimeUtil;
 
 public class MimeUtils {
 
     public static final String[] XML_MIMES = { "application/xml", "text/xml" };
     public static final String[] HTML_MIMES = { "text/html" };
     public static final String[] XLS_MIMES = { "application/x-msexcel", "application/vnd.ms-excel",
             "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
             "application/msword" };
     public static final String[] ZIP_MIMES = { "application/zip", "application/x-zip",
             "application/x-compressed" };
     public static final String[] WORD_MIMES = { "application/msword",
             "application/vnd.openxmlformats-officedocument.wordprocessingml.document" };
     public static final String[] SAVE_MIMES = { "application/xml", "text/xml", "text/html",
             "application/vnd.ms-excel", "application/x-msexcel",
             "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "application/zip",
             "application/x-zip", "application/x-compressed", "application/msword",
             "application/vnd.openxmlformats-officedocument.wordprocessingml.document" };
 
     private static Logger logger = Logger.getLogger(MimeUtils.class.getName());
 
     public static String getMimeType(File file) throws PhenoportalFileException {
         String mime;
         Collection mimeTypes = MimeUtil.getMimeTypes(file);
         MimeType mimeType = MimeUtil.getMostSpecificMimeType(mimeTypes);
         mime = mimeType.toString();
         return mime;
     }
 
     public static boolean isXmlFile(File file) throws PhenoportalFileException {
         return Arrays.asList(XML_MIMES).contains(getMimeType(file));
     }
 
     public static boolean isXlsFile(File file) throws PhenoportalFileException {
         return Arrays.asList(XLS_MIMES).contains(getMimeType(file));
     }
 
     public static boolean isHtmlFile(File file) throws PhenoportalFileException {
         return Arrays.asList(HTML_MIMES).contains(getMimeType(file));
     }
 
     public static boolean isZipFile(File file) throws PhenoportalFileException {
         return Arrays.asList(ZIP_MIMES).contains(getMimeType(file));
     }
 
     public static boolean isWordFile(File file) throws PhenoportalFileException {
         return Arrays.asList(WORD_MIMES).contains(getMimeType(file));
     }
 
     public static boolean isSaveFile(File file) throws PhenoportalFileException {
         return Arrays.asList(SAVE_MIMES).contains(getMimeType(file));
     }
 
     public static boolean isXmlFile(String mime) {
         return Arrays.asList(XML_MIMES).contains(mime);
     }
 
     public static boolean isXlsFile(String mime) {
         return Arrays.asList(XLS_MIMES).contains(mime);
     }
 
     public static boolean isHtmlFile(String mime) {
         return Arrays.asList(HTML_MIMES).contains(mime);
     }
 
     public static boolean isZipFile(String mime) {
         return Arrays.asList(ZIP_MIMES).contains(mime);
     }
 
     public static boolean isWordFile(String mime) {
         return Arrays.asList(WORD_MIMES).contains(mime);
     }
 
     public static boolean isSaveFile(String mime) {
         return Arrays.asList(SAVE_MIMES).contains(mime);
     }
 
 }
