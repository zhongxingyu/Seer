 package org.nines;
 
 import java.io.File;
 
 import org.apache.commons.lang.StringEscapeUtils;
 
 public class TextUtils {
 
     /**
      * Normalize whitespace; collapse into one space/tab and one linefeed
      * @param srcText
      * @return
      */
     public static String normalizeWhitespace(final String srcText) {
         String[] lines = srcText.replaceAll("\n+", "\n").split("\n");
         StringBuffer out = new StringBuffer();
         for ( int i =0; i<lines.length; i++) {
             String line = lines[i];
             line = line.replaceAll("\t", " ");
             line = line.replaceAll(" +", " ");
            line = line.trim();
             if ( line.length() > 0) {
                 out.append( line ).append("\n");
             }
         }  
         return out.toString().trim();
     }
     
     /**
      * Remove unknown UTF-8 characters (0xFFFD) and log warnings for each
      * @param value
      * @return
      */
     public static String stripUnknownUTF8(final String value, ErrorReport errorReport, final File file) {
         return stripUnknownUTF8(value, errorReport, file, null);
     }
     public static String stripUnknownUTF8(final String value, ErrorReport errorReport, final String url) {
         return stripUnknownUTF8(value, errorReport, null, url);
     }
     public static String stripUnknownUTF8(final String value, ErrorReport errorReport, final File file, final String url) {
         
         String fileName = "";
         if (file != null) {
             fileName = file.toString();
         }
         
         // Look for unknown character and warn
         int curPos= 0;
         while ( true ) {
             int pos = value.indexOf("\ufffd", curPos);
             if (pos == -1) {
                 break;
             }
             curPos = pos+1;
             
             String snip = value.substring(Math.max(0, pos-25), Math.min(value.length(), pos+25));            
             errorReport.addError(new IndexerError(fileName, url, 
                     "Invalid UTF-8 character at position " + pos
                     + " of field text"
                     + "\n  Snippet: ["+snip+"]"));
                 
         }
         return value.replaceAll("\ufffd", "");
     }
 
     /**
      * Unescape all sequences. Check for invalid remaining sequences and strip them out.
      * @param srcText The unclean text.
      * @return Cleaned text!
      */
     public static String stripEscapeSequences(final String srcText, ErrorReport errorReport, final String uri) {
         return stripEscapeSequences(srcText, errorReport, null, uri);
     }
     public static String stripEscapeSequences(final String srcText, ErrorReport errorReport, final File file) {
         return stripEscapeSequences(srcText, errorReport, file, null);
     }
     public static String stripEscapeSequences(final String srcText, ErrorReport errorReport, final File file, final String uri) {
 
         String fileName = "";
         if (file != null) {
             fileName = file.toString();
         }
         
         String cleaned = StringEscapeUtils.unescapeXml(srcText);
         
         int startPos = 0;
         while (true) {
             int pos = cleaned.indexOf("&#", startPos);
             if (pos == -1) {
                 break;
             } else {
                 // look for a trainling ; to end the sequence
                 int pos2 = cleaned.indexOf(";", pos);
                 if (pos2 > -1) {
                     // this is likely an escape sequence
                     if (pos2 <= pos + 6) {
 
                         // dump the bad sequence
                         String bad = cleaned.substring(pos, pos2 + 1);
                         cleaned = cleaned.replaceAll(bad, "");
                         errorReport.addError(new IndexerError(fileName, uri,
                             "Removed potentially invalid escape sequece [" + bad + "]"));
                         startPos = pos;
 
                     } else {
 
                         // no close ; found. Just skip over the &#
                         startPos = pos + 2;
                     }
 
                 } else {
                     // NO ; found - skip over the &#
                     startPos = pos + 2;
                 }
             }
         }
         return cleaned;
     }
 }
