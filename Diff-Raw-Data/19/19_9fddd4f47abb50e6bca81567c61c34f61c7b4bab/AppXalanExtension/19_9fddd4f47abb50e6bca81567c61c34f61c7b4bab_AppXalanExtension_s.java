 package uk.ac.ebi.ae15.utils;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.xalan.extensions.XSLProcessorContext;
 import org.apache.xalan.templates.ElemExtensionCall;
 import org.apache.xpath.NodeSet;
 import org.w3c.dom.Document;
 import org.w3c.dom.DocumentFragment;
 import org.w3c.dom.Element;
 import org.w3c.dom.NodeList;
 import uk.ac.ebi.ae15.app.Application;
 import uk.ac.ebi.ae15.components.DownloadableFilesRegistry;
 import uk.ac.ebi.ae15.components.Experiments;
 import uk.ac.ebi.ae15.utils.files.FtpFileEntry;
 import uk.ac.ebi.ae15.utils.search.ExperimentSearch;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.transform.TransformerException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.List;
 
 public class AppXalanExtension
 {
     // logging machinery
     private static final Log log = LogFactory.getLog(AppXalanExtension.class);
     // Application reference
     private static Application application;
     // Accession RegExp filter
     private static final RegExpHelper accessionRegExp = new RegExpHelper("^E-\\w{4}-\\d+$", "i");
 
     public static String toUpperCase( String str )
     {
         return str.toUpperCase();
     }
 
     public static String toLowerCase( String str )
     {
         return str.toLowerCase();
     }
 
     public static String capitalize( String str )
     {
         return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
     }
 
     public static String fileSizeToString( long size )
     {
         StringBuilder str = new StringBuilder();
         if (922L > size ) {
             str.append(size).append(" B");
         } else if (944128L > size) {
             str.append(String.format("%.0f KB", (Long.valueOf(size).doubleValue()/1024.0)));
         } else if (1073741824L > size) {
             str.append(String.format("%.1f MB", (Long.valueOf(size).doubleValue()/1048576.0)));
         } else if (1099511627776L > size) {
             str.append(String.format("%.2f GB", (Long.valueOf(size).doubleValue()/1073741824.0)));
         }
         return str.toString();
     }
 
     public static String normalizeSpecies( String species )
     {
         // if more than one word: "First second", otherwise "First"
         String[] spArray = species.trim().split("\\s");
         if (0 == spArray.length) {
             return "";
         } else if (1 == spArray.length) {
             return capitalize(spArray[0]);
         } else {
             return capitalize(spArray[0] + ' ' + spArray[1]);
         }
     }
 
     public static String trimTrailingDot( String str )
     {
         if (str.endsWith(".")) {
             return str.substring(0, str.length() - 2);
         } else
             return str;
     }
 
     public static String dateToRfc822()
     {
         return new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z").format(new Date());
     }
 
     public static String dateToRfc822( String dateString )
     {
         if (null != dateString && 0 < dateString.length()) {
             try {
                 Date date = new SimpleDateFormat("yyyy-MM-dd").parse(dateString);
                 dateString = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z").format(date);
             } catch ( Throwable x ) {
                 log.debug("Caught an exception:", x);
             }
         } else {
             dateString = "";
         }
 
         return dateString;
     }
 
     public static boolean isExperimentInWarehouse( String accession )
     {
         return ((Experiments) application.getComponent("Experiments"))
                 .isInWarehouse(accession);
     }
 
     public static boolean isExperimentAccessible( String accession, String userId )
     {
         return ((Experiments) application.getComponent("Experiments"))
                 .isAccessible(accession, userId);
     }
 
     public static NodeSet getFilesForAccession( String accession ) throws InterruptedException
     {
         try {
             DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
             DocumentBuilder db = dbf.newDocumentBuilder();
             Document doc = db.newDocument();
 
             DocumentFragment df = doc.createDocumentFragment();
 
             List<FtpFileEntry> files = ((DownloadableFilesRegistry) application.getComponent("DownloadableFilesRegistry"))
                     .getFilesMap()
                     .getEntriesByAccession(accession);
             if (null != files) {
                 for ( FtpFileEntry file : files ) {
                     Element fileElt = doc.createElement("file");
                     fileElt.setAttribute("kind", FtpFileEntry.getKind(file));
                     fileElt.setAttribute("extension", FtpFileEntry.getExtension(file));
                     fileElt.setAttribute("name", FtpFileEntry.getName(file));
                     fileElt.setAttribute("size", String.valueOf(file.getSize()));
                     fileElt.setAttribute("lastmodified", new SimpleDateFormat("d MMMMM yyyy, HH:mm").format(new Date(file.getLastModified())));
                     df.appendChild(fileElt);
                 }
             }
             Thread.sleep(1);
             return new NodeSet(df);
 
         } catch (InterruptedException x) {
             log.warn("Method interrupted");
             throw x;
         } catch ( Throwable x ) {
             log.error("Caught an exception:", x);
         }
         return new NodeSet();
     }
 
     public static boolean testRegexp( String input, String pattern, String flags )
     {
         boolean result = false;
 
         try {
             return new RegExpHelper(pattern, flags).test(input);
         } catch ( Throwable t ) {
             log.debug("Caught an exception:", t);
         }
 
         return result;
     }
 
     private static String keywordToPattern( String keyword, boolean wholeWord )
     {
         return (wholeWord ? "\\b\\Q" + keyword + "\\E\\b" : "\\Q" + keyword + "\\E");
     }
 
     public static boolean testExperiment( NodeList nl, String userId, String keywords, String wholeWords, String species, String array, String experimentType, String inAtlas )
     {
         try {
             if (null != nl && 0 < nl.getLength()) {
                 Element elt = (Element) nl.item(0);
 
                 if (inAtlas.equals("true") && elt.getAttribute("loadedinatlas").equals(""))
                     return false;
 
                 String textIdx = elt.getAttribute("textIdx");
                 ExperimentSearch search = ((Experiments) application.getComponent("Experiments")).getSearch();
 
                 if (!userId.equals("0") && !search.matchUser(textIdx, userId))
                     return false;
 
                 if (accessionRegExp.test(keywords) && !search.matchAccession(textIdx, keywords))
                     return false;
 
                 if (!keywords.equals("") && !search.matchText(textIdx, keywords, wholeWords.equals("true")))
                     return false;
 
                 if (!species.equals("") && !search.matchSpecies(textIdx, species))
                     return false;
 
                 if (!array.equals("") && !search.matchArray(textIdx, array))
                     return false;
 
                 return  experimentType.equals("") || search.matchExperimentType(textIdx, experimentType);
             }
 
         } catch ( Throwable x ) {
             log.error("Caught an exception:", x);
         }
         return false;
     }
 
     public static String markKeywords( String input, String keywords, String wholeWords )
     {
         String result = input;
 
         try {
             if (null != keywords && 0 < keywords.length()) {
                 if (2 < keywords.length() && '"' == keywords.charAt(0) && '"' == keywords.charAt(keywords.length() - 1)) {
                     // if keywords are adorned with double-quotes we do phrase matching
                     result = new RegExpHelper("(" + keywordToPattern(keywords.substring(1, keywords.length() - 1), true) + ")", "ig").replace(result, "\u00ab$1\u00bb");
                 } else {
 
                    String[] kwdArray = keywords.split("\\s");
                     for ( String keyword : kwdArray ) {
                         result = new RegExpHelper("(" + keywordToPattern(keyword, wholeWords.equals("true")) + ")", "ig").replace(result, "\u00ab$1\u00bb");
                     }
                 }
             }
             boolean shouldRemoveExtraMarkers = true;
             String newResult;
 
             while ( shouldRemoveExtraMarkers ) {
                newResult = new RegExpHelper("\u00ab([^\u00ab\u00bb]*)\u00ab", "ig").replace(result, "\u00ab$1");
                 shouldRemoveExtraMarkers = !newResult.equals(result);
                 result = newResult;
             }
             shouldRemoveExtraMarkers = true;
             while ( shouldRemoveExtraMarkers ) {
                newResult = new RegExpHelper("\u00bb([^\u00ab\u00bb]*)\u00bb", "ig").replace(result, "$1\u00bb");
                 shouldRemoveExtraMarkers = !newResult.equals(result);
                 result = newResult;
             }
         } catch ( Throwable x ) {
             log.error("Caught an exception:", x);
         }
 
         return result;
     }
 
     public static void logInfo( XSLProcessorContext c, ElemExtensionCall extElt )
     {
         try {
             log.info(extElt.getAttribute("select", c.getContextNode(), c.getTransformer()));
         } catch ( TransformerException e ) {
             log.debug("Caught an exception:", e);
         }
     }
 
     public static void logDebug( XSLProcessorContext c, ElemExtensionCall extElt )
     {
         try {
             log.debug(extElt.getAttribute("select", c.getContextNode(), c.getTransformer()));
         } catch ( TransformerException e ) {
             log.debug("Caught an exception:", e);
         }
     }
 
     public static void setApplication( Application app )
     {
         application = app;
     }
 }
