 package com.orbious.util;
 
 // $Id: Helper.java 11 2009-12-04 14:07:11Z app $
 
 import java.io.BufferedReader;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.HashSet;
 import java.util.Vector;
 import org.apache.log4j.Logger;
 import com.orbious.extractor.Config;
 import com.orbious.extractor.SentenceMapEntry;
 import com.orbious.extractor.SentenceMapEntry.Likelihood;
 import com.orbious.extractor.SentenceMapEntry.SentenceEntrySubType;
 import com.orbious.extractor.SentenceMapEntry.SentenceEntryType;
 
 
 /**
  * Static helper methods.
  * 
  * @author dave
  * @version 1.0
  * @since 1.0
  */
 
 public class Helper {
 
   /**
    * A direction indicator, used by 
    * {@link Helper#moveToNonWhitespace(DIRN, char[], int)}.
    */
   public enum DIRN { LEFT, RIGHT };
 
   /**
    * Private Constructor
    */
   private Helper() { }
   
   /**
    * Returns a <code>String</code> containing text extracted from 
    * <code>buf</code> of size <code>size</code> relative to the position 
    * <code>pos</code>. Also includes a string specifying the location
    * of <code>idx</code>.
    * <code>start</code> and <code>end</code> are 
    * automatically update to the <code>buf</code> 
    * extremium's if required.
    * 
    * @param buf   Text buffer.
    * @param idx   The <code>buf</code> array index specifying the 
    *              middle point for extraction.
    * @param size    The length of the <code>String</code> to return.
    * 
    * @return    A <code>String</code> containing text extracted from 
    *            <code>buf</code>.
    */
 
   public static String getDebugStringFromCharBuf(char[] buf, int idx, int size) {
     String str;
     String id;
     int start;
     int end;
 
     str = "";
     id = "";
 
     start = idx-(size/2);
     if ( start < 0 ) {
       size += Math.abs(start*2);
       start = 0;
     }
     
     end = idx+(size/2)+1;
     if ( end >= buf.length ) {
       end = buf.length;
     }
     
     for ( int i = start; i < end; i++ ) {
       str += buf[i];
       if ( i == idx ) {
         id += "|";
       } else {
         id += "-";
       }
     }
 
     return( str + "\n" + id );
   }
   
   /**
    * Returns a <code>String</code> containing text extracted from 
    * <code>buf</code> of size <code>size</code> relative to the position 
    * <code>pos</code>. <code>start</code> and <code>end</code> are 
    * automatically update to the <code>buf</code> 
    * extremium's if required.
    * 
    * @param buf   Text buffer.
    * @param idx   The <code>buf</code> array index specifying the 
    *              middle point for extraction.
    * @param size    The length of the <code>String</code> to return.
    * @param width   The width of the debug string where a newline is inserted
    *                at each length <code>width</code> in the debug string.
    *                If <code>-1</code>, no newline is inserted.
    * @return    A <code>String</code> containing text extracted from 
    *            <code>buf</code>.
    */
   public static String getDebugStringFromCharBuf(final char[] buf,
       int idx, int size, int width) { 
     String str;
     int modct;
     int start;
     int end;
     
     str = "";
     modct = 1;
     
     start = idx-(size/2);
     if ( start < 0 ) {
       size += Math.abs(start*2);
       start = 0;
     }
     
     end = idx+(size/2)+1;
     if ( end > buf.length ) {
       end = buf.length;
     }
  
     for ( int i = start; i < end; i++ ) {
       str += buf[i];
       
       if ( (width != -1) && (modct % width == 0) ) {
         str += "\n";
       }
       modct++;
     }
     
     str += "\n";
     return(str);        
   }
   
   /**
    * Returns a debugging string for an array of <code>boolean</code>'s 
    * (which is used in the sentence extraction algorithm).
    * 
    * @param template    A text buffer of the same size as <code>buf</code>
    *                    used to insert whitespace characters.    
    * @param buf   An array of <code>boolean</code>'s.
    * @param idx   The position in <code>buf</code> to begin writing
    *              the debug string.
    * @param size    The number of entries to examine in <code>buf</code>.     
    * @param width   The width of the debug string where a newline
    *                is inserted at each length <code>width</code> in the debug
    *                string. If <code>-1</code>, no newline is inserted.
    * 
    * @return    A debug string for <code>buf</code>.
    */
   public static String getDebugStringFromBoolBuf(final char[] template, 
       final boolean[] buf, int idx, int size, int width) { 
     String str;
     int modct;
     int start;
     int end;
     
     str = "";
     modct = 1;
     
     start = idx-(size/2);
     if ( start < 0 ) {
       size += Math.abs(start*2);
       start = 0;
     }
     
     end = idx+(size/2)+1;
     if ( end > buf.length ) {
       end = buf.length;
     }
  
     for ( int i = start; i < end; i++ ) {
       if ( Character.isWhitespace(template[i]) ) {
         str += " ";
       } else {
         if ( buf[i] ) {
           str += ".";
         } else {
           str += "+";
         }
       }
       
       if ( (width != -1) && (modct % width == 0) ) {
         str += "\n";
       }
       modct++;
     }
     
     str += "\n";
     return(str);    
   }
   
   /**
    * Returns a debugging string for an array of <code>SentenceMapEntry</code>'s.
    * 
    * @param template    A text buffer of the same size as <code>buf</code>
    *                    used to insert whitespace characters.
    * @param buf   An array of <code>SentenceMapEntry</code>'s.
    * @param idx   The position in <code>buf</code> to begin writing
    *              the debug string.
    * @param size    The number of entries to examine in <code>buf</code>.     
    * @param width   The width of the debug string where a newline
    *                is inserted at each length <code>width</code> in the debug
    *                string. If <code>-1</code>, no newline is inserted.
    * 
    * @return    A debug string for <code>buf</code>.
    */
   public static String getDebugStringFromSentenceMap(final char[] template,
       final SentenceMapEntry[] buf, int idx, int size, int width) { 
     String str;
     int modct;
     SentenceMapEntry entry;
     int start;
     int end;
     
     str = "";
     modct = 1;
     
     start = idx-(size/2);
     if ( start < 0 ) {
       size += Math.abs(start*2);
       start = 0;
     }
     
     end = idx+(size/2)+1;
     if ( end >= buf.length ) {
       end = buf.length;
     }
     
     // e - Likely end from start
     // E - likely end
     // U - unlikely end
     // s - Likely start from end
     // S - likely start
     // u - unlikely start
     for ( int i = start; i < end; i++ ) {
       if ( Character.isWhitespace(template[i]) ) {
         str += " ";
       } else {
         entry = buf[i];
         if ( entry == null ) {
           str += ".";
         } else if ( entry.type() == SentenceEntryType.END ) {
           if ( entry.subtype() == SentenceEntrySubType.END_FROM_START ) {
             // this is always likely
             str += "e";
           } else {
             // 
             if ( entry.likelihood() == Likelihood.LIKELY ) {
               str += "E";
             } else {
               str += "U";
             }
           }
         } else if ( entry.type() == SentenceEntryType.START ) {
           if ( entry.subtype() == SentenceEntrySubType.START_FROM_END ) {
             // always likely 
             str += "s";
           } else {
             if ( entry.likelihood() == Likelihood.LIKELY ) {
               str += "S";
             } else {
               str += "n";
             }
           }
         }
       }
       
       if ( (width != -1) && (modct % width == 0) ) {
         str += "\n";
       }
       modct++;
     }
     
     str += "\n";
     return(str);
   }
   
   /**
    * Convert the contents of a <code>String</code> to a 
    * <code>HashSet</code> separated on <code>Character</code> boundaries.
    * 
    * @param str   The <code>String</code> to interrogate.
    * 
    * @return    A <code>HashSet</code> containing the contents of the 
    *            <code>String</code> <code>str</code>.
    */
   public static HashSet<Character> cvtStringToHashSet(String str) {
     char[] buf = str.toCharArray();
     HashSet<Character> hs = new HashSet<Character>();
     
     for ( int i = 0; i < buf.length; i++ ) {
     hs.add(buf[i]);
     }
     
     return(hs);
   }
   
   /**
    * Converts the contents of the file <code>filename</code> to 
    * a <code>HashSet</code> of <code>String</code>' which each line in the 
    * file occupying an entry in the <code>HashSet</code>.
    * 
    * @param filename    The absolute filename to parse.
    * @param lowercase   Convert text to lowercase before adding to <code>HashSet</code>.
    * @return    A <code>HashSet</code> containing the contents
    *            of <code>filename</code>.
    */
   public static HashSet<String> cvtFileToHashSet(String filename, boolean lowercase) {
     Logger logger;
     HashSet<String> hs;
     BufferedReader br;
     String resourceStr;
     
     br = null;
     
     //resourceStr = ClassLoader.getSystemResource(filename).getFile();
    resourceStr = "".getClass().getClassLoader().getResource(filename).getFile();
     logger = Logger.getLogger(Config.LOGGER_REALM.asStr());
     try {
       br = new BufferedReader(new FileReader(resourceStr));
     } catch ( FileNotFoundException fnfe ) {
       logger.fatal("Failed to open file " + filename + "(" + resourceStr + ")", fnfe);
     }
     
     hs = new HashSet<String>();
 
     try {
       String wd;
       while ( (wd = br.readLine()) != null ) {
         if ( !wd.matches("#.*") ) {
           // ignore comments.
           if ( lowercase ) {
             hs.add(wd.toLowerCase());
           } else {
             hs.add(wd);
           }
         } 
       }
     } catch ( IOException ioe ) {
       logger.fatal("Failed to read names file " + 
           Config.NAMES_FILENAME, ioe);
     }
     
     logger.info("Extracted " + hs.size() + " entries from " + filename);
     return(hs);
   }
   
   /**
    * Convert a <code>Vector</code> of <code>String</code>'s to a single 
    * <code>String</code> separated by whitespace.
    * 
    * @param words    A list of <code>String</code>'s to convert.
    * 
    * @return    The <code>Vector</code> with its <code>String</code>
    *            contents appended to a <code>String</code>.
    */
   
   public static String cvtVectorToString(Vector<String> words) {
     String str = "";
 
     for ( int i = 0; i < words.size(); i++ ) {
       if ( i+1 < words.size() ) {
         str += words.get(i) + " ";
       } else {
         str += words.get(i);
       }
     }
 
     str += "";
     return(str);
   }
   
   /**
    * Determines if the previous non-whitespace character in <code>buf</code> 
    * from <code>idx</code> is a letter.
    * 
    * @param buf   Text buffer.
    * @param idx   Position in <code>buf</code>.
    * 
    * @return    <code>true</code> if the first previous non-whitespace
    *            character is a letter, <code>false</code> otherwise.
    */
   public static boolean isPreviousLetter(final char[] buf, int idx) { 
     int i;
 
     i = moveToNonWhitespace(DIRN.LEFT, buf, idx);
     if ( i == idx ) {
       return(false);
     }
     
     return( Character.isLetter(buf[i]) );
   }
   
   /**
    * Determines if the next non-whitespace character in <code>buf</code> 
    * from <code>idx</code> is a letter.
    * 
    * @param buf   Text buffer.
    * @param idx   Position in <code>buf</code>.
    * 
    * @return    <code>true</code> if the first next non-whitespace
    *            character is a letter, <code>false</code> otherwise.
    */
   public static boolean isNextLetter(final char[] buf, int idx) {
     int i;
     
     i = moveToNonWhitespace(DIRN.RIGHT, buf, idx);
     if ( i == idx ) {
       return(false);
     }
     
     return( Character.isLetter(buf[i]) );
   }
   
   /**
    * Determines if the previous non-whitespace character in <code>buf</code> 
    * from <code>idx</code> is a number.
    * 
    * @param buf   Text buffer.
    * @param idx   Position in <code>buf</code>.
    * 
    * @return    <code>true</code> if the first previous non-whitespace
    *            character is a number, <code>false</code> otherwise.
    */
   public static boolean isPreviousNumber(final char[] buf, int idx) {
     int i;
     
     i = moveToNonWhitespace(DIRN.LEFT, buf, idx);
     if ( i == idx ) {
       return(false);
     }
     
     return( Character.isDigit(buf[i]) );
   }
   
   /**
    * Determines if the next non-whitespace character in <code>buf</code> 
    * from <code>idx</code> is a number.
    * 
    * @param buf   Text buffer.
    * @param idx   Position in <code>buf</code>.
    * 
    * @return    <code>true</code> if the first next non-whitespace
    *            character is a number, <code>false</code> otherwise.
    */
   public static boolean isNextNumber(final char[] buf, int idx) {
     int i;
     
     i = moveToNonWhitespace(DIRN.RIGHT, buf, idx);
     if ( i == idx ) {
       return(false);
     }
     
     return( Character.isDigit(buf[i]) );
   }
   
   /**
    * Returns an index that points to the first non-whitespace character
    * in <code>buf</code>.
    * 
    * @param dirn  Either <code>LEFT</code> or <code>RIGHT</code>.
    * @param buf   Text buffer.
    * @param idx   Position in <code>buf</code>.
    * 
    * @return    The first non-whitespace index in <code>buf</code> either
    *            in the <code>LEFT</code> or <code>RIGHT</code> direction.
    */
   public static int moveToNonWhitespace(DIRN dirn, final char[] buf, int idx) {
     int i;
     
     if ( dirn == DIRN.LEFT ) {
       i = idx-1;
       while ( (i > 0) && Character.isWhitespace(buf[i]) ) {
         i--;
       }
     } else {
       i = idx+1;
       while ( (i < buf.length) && Character.isWhitespace(buf[i]) ) {
         i++;
       }
     }
     
     return(i);
   }
   
   
 }
