 package uk.ac.ebi.arrayexpress.utils.saxon;
 
 import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.om.*;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import uk.ac.ebi.arrayexpress.app.Application;
 import uk.ac.ebi.arrayexpress.components.DownloadableFilesRegistry;
 import uk.ac.ebi.arrayexpress.components.Experiments;
 import uk.ac.ebi.arrayexpress.components.SaxonEngine;
 import uk.ac.ebi.arrayexpress.components.SearchEngine;
 import uk.ac.ebi.arrayexpress.utils.RegExpHelper;
 import uk.ac.ebi.arrayexpress.utils.files.FtpFileEntry;
 
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.List;
 
 public class ExtFunctions
 {
     // logging machinery
     private static final Logger logger = LoggerFactory.getLogger(ExtFunctions.class);
     // Accession RegExp filter
     private static final RegExpHelper accessionRegExp = new RegExpHelper("^E-\\w{4}-\\d+$", "i");
 
     public static String toUpperCase(String str)
     {
         return str.toUpperCase();
     }
 
     public static String toLowerCase(String str)
     {
         return str.toLowerCase();
     }
 
     public static String capitalize(String str)
     {
         return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
     }
 
     public static String fileSizeToString(long size)
     {
         StringBuilder str = new StringBuilder();
         if (922L > size) {
             str.append(size).append(" B");
         } else if (944128L > size) {
             str.append(String.format("%.0f KB", (Long.valueOf(size).doubleValue() / 1024.0)));
         } else if (1073741824L > size) {
             str.append(String.format("%.1f MB", (Long.valueOf(size).doubleValue() / 1048576.0)));
         } else if (1099511627776L > size) {
             str.append(String.format("%.2f GB", (Long.valueOf(size).doubleValue() / 1073741824.0)));
         }
         return str.toString();
     }
 
     public static String describeQuery(String keywords, String wholeWords, String species, String array, String experimentType, String inAtlas)
     {
         StringBuilder desc = new StringBuilder();
         if (!keywords.trim().equals("")) {
             desc.append("'").append(keywords).append("'");
         }
         if (!species.trim().equals("")) {
             if (0 != desc.length()) {
                 desc.append(" and ");
             }
             desc.append("species '").append(species).append("'");
         }
         if (!array.trim().equals("")) {
             if (0 != desc.length()) {
                 desc.append(" and ");
             }
             desc.append("array '").append(array).append("'");
         }
         if (!experimentType.trim().equals("")) {
             if (0 != desc.length()) {
                 desc.append(" and ");
             }
             desc.append("experiment type '").append(experimentType).append("'");
         }
 
         if (0 != desc.length()) {
             desc.insert(0, "matching ");
         }
 
         if (testCheckbox(inAtlas)) {
             if (0 != desc.length()) {
                 desc.append(" and ");
             }
             desc.append("present in ArrayExpress Atlas");
         }
         return desc.toString();
     }
 
     public static String normalizeSpecies(String species)
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
 
     public static String trimTrailingDot(String str)
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
 
     public static String dateToRfc822(String dateString)
     {
         if (null != dateString && 0 < dateString.length()) {
             try {
                 Date date = new SimpleDateFormat("yyyy-MM-dd").parse(dateString);
                 dateString = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z").format(date);
             } catch (Throwable x) {
                 logger.debug("Caught an exception:", x);
             }
         } else {
             dateString = "";
         }
 
         return dateString;
     }
 
     public static void createIndex(XPathContext context)
     {
         logger.debug("About to create index");
         ((SearchEngine)Application.getAppComponent("SearchEngine")).createIndex();
     }
 
     public static void commitIndex(XPathContext context)
     {
         logger.debug("Done creating index");
         ((SearchEngine)Application.getAppComponent("SearchEngine")).commitIndex();
     }
 
     public static int createIndexDocument(XPathContext context)
     {
         ((SearchEngine)Application.getAppComponent("SearchEngine")).newIndexDocument((NodeInfo)context.getContextItem());
         return 0;
     }
 
    public static void addIndexField(XPathContext context, int documentId, String fieldName, SequenceIterator value, int flags)
     {
        FastStringBuffer sb = new FastStringBuffer(1024);
        try {
            boolean first = true;
            String sep = " ";
            while (true) {
                Item item = value.next();
                if (null == item) {
                    break;
                }
                if (!first) {
                    sb.append(sep);
                }
                first = false;
                sb.append(item.getStringValueCS());
            }
        } catch (Throwable x) {
            logger.error("Caught an exception:", x);
        }

        ((SearchEngine)Application.getAppComponent("SearchEngine")).addIndexField(fieldName, sb.toString(), flags);
     }
 
     public static void addDocumentToIndex(XPathContext context, int documentId)
     {
         ((SearchEngine)Application.getAppComponent("SearchEngine")).addIndexDocument();
     }
 
     public static SequenceIterator searchIndex(XPathContext context, String queryString)
     {
         List<NodeInfo> nodes = ((SearchEngine)Application.getAppComponent("SearchEngine")).queryIndex(queryString);
         if (null != nodes) {
             return new NodeListIterator(nodes);
         }
 
         return null;
     }
 
     /* ***************************************************** */
     public static boolean isExperimentInWarehouse(String accession)
     {
         return ((Experiments)Application.getAppComponent("Experiments"))
             .isInWarehouse(accession);
     }
 
     public static boolean isExperimentAccessible(String accession, String userId)
     {
         return ((Experiments)Application.getAppComponent("Experiments"))
             .isAccessible(accession, userId);
     }
 
     public static NodeInfo getFilesForAccession(String accession) throws InterruptedException
     {
         try {
             List<FtpFileEntry> files = ((DownloadableFilesRegistry)Application.getAppComponent("DownloadableFilesRegistry"))
                 .getFilesMap()
                 .getEntriesByAccession(accession);
             if (null != files) {
                 StringBuilder sb = new StringBuilder("<files>");
                 for (FtpFileEntry file : files) {
                     sb.append("<file kind=\"")
                         .append(FtpFileEntry.getKind(file))
                         .append("\" extension=\"")
                         .append(FtpFileEntry.getExtension(file))
                         .append("\" name=\"")
                         .append(FtpFileEntry.getName(file))
                         .append("\" size=\"")
                         .append(String.valueOf(file.getSize()))
                         .append("\" lastmodified=\"")
                         .append(new SimpleDateFormat("d MMMMM yyyy, HH:mm").format(new Date(file.getLastModified())))
                         .append("\"/>");
                     Thread.sleep(1);
                 }
                 sb.append("</files>");
                 return ((SaxonEngine)Application.getAppComponent("SaxonEngine")).buildDocument(sb.toString());
             }
         } catch (InterruptedException x) {
             logger.warn("Method interrupted");
             throw x;
         } catch (Throwable x) {
             logger.error("Caught an exception:", x);
         }
         return null;
     }
 
     public static boolean testRegexp(String input, String pattern, String flags)
     {
         boolean result = false;
 
         try {
             return new RegExpHelper(pattern, flags).test(input);
         } catch (Throwable t) {
             logger.debug("Caught an exception:", t);
         }
 
         return result;
     }
 
     private static String keywordToPattern(String keyword, boolean wholeWord)
     {
         return (wholeWord ? "\\b\\Q" + keyword + "\\E\\b" : "\\Q" + keyword + "\\E");
     }
 
     private static boolean testCheckbox(String check)
     {
         return (null != check && (check.toLowerCase().equals("true") || check.toLowerCase().equals("on")));
     }
 
     public static boolean testExperiment(XPathContext context, String userId, String keywords, String wholeWords, String species, String array, String experimentType, String inAtlas)
     {
         final int textIdxFPrint = context.getNamePool().getFingerprint("", "textidx");
         final int loadedInAtlasFPrint = context.getNamePool().getFingerprint("", "loadedinatlas");
         try {
             NodeInfo node = (NodeInfo)context.getContextItem();
             String textIdx = node.getAttributeValue(textIdxFPrint);
             String loadedInAtlas = node.getAttributeValue(loadedInAtlasFPrint);
 
             if (testCheckbox(inAtlas) && null != loadedInAtlas && loadedInAtlas.equals(""))
                 return false;
 
 //            ExperimentSearch search = ((Experiments) Application.getAppComponent("Experiments")).getSearch();
 //
 //            if (!userId.equals("0") && !search.matchUser(textIdx, userId))
 //                return false;
 //
 //            if (accessionRegExp.test(keywords) && !search.matchAccession(textIdx, keywords))
 //                return false;
 //
 //            if (!keywords.equals("") && !search.matchText(textIdx, keywords, testCheckbox(wholeWords)))
 //                return false;
 //
 //            if (!species.equals("") && !search.matchSpecies(textIdx, species))
 //                return false;
 //
 //            if (!array.equals("") && !search.matchArray(textIdx, array))
 //                return false;
 //
 //            return  experimentType.equals("") || search.matchExperimentType(textIdx, experimentType);
         } catch (Throwable x) {
             logger.error("Caught an exception:", x);
         }
         return true;
     }
 
     private static RegExpHelper removeDupeMarkers1RegExp = new RegExpHelper("\u00ab([^\u00ab\u00bb]*)\u00ab", "ig");
     private static RegExpHelper removeDupeMarkers2RegExp = new RegExpHelper("\u00bb([^\u00ab\u00bb]*)\u00bb", "ig");
 
     public static String markKeywords(String input, String keywords, String wholeWords)
     {
         String result = input;
 
         try {
             if (null != keywords && 0 < keywords.length()) {
                 if (2 < keywords.length() && '"' == keywords.charAt(0) && '"' == keywords.charAt(keywords.length() - 1)) {
                     // if keywords are adorned with double-quotes we do phrase matching
                     result = new RegExpHelper("(" + keywordToPattern(keywords.substring(1, keywords.length() - 1), true) + ")", "ig").replace(result, "\u00ab$1\u00bb");
                 } else {
 
                     String[] kwdArray = keywords.split("\\s+");
                     for (String keyword : kwdArray) {
                         result = new RegExpHelper("(" + keywordToPattern(keyword, testCheckbox(wholeWords)) + ")", "ig").replace(result, "\u00ab$1\u00bb");
                     }
                 }
             }
             boolean shouldRemoveExtraMarkers = true;
             String newResult;
 
             while (shouldRemoveExtraMarkers) {
                 newResult = removeDupeMarkers1RegExp.replace(result, "\u00ab$1");
                 shouldRemoveExtraMarkers = !newResult.equals(result);
                 result = newResult;
             }
             shouldRemoveExtraMarkers = true;
             while (shouldRemoveExtraMarkers) {
                 newResult = removeDupeMarkers2RegExp.replace(result, "$1\u00bb");
                 shouldRemoveExtraMarkers = !newResult.equals(result);
                 result = newResult;
             }
         } catch (Throwable x) {
             logger.error("Caught an exception:", x);
         }
 
         return result;
     }
 }
