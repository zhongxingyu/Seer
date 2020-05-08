 package com.github.mnicky.bible4j;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.HashMap;
 import java.util.Map;
 
 import com.github.mnicky.bible4j.data.BibleBook;
 
 public final class Utils {
     
     //FIXME: repair filepath
    private static final String BIBLE_BOOK_ABBRS_FILE = "/bibleBookAbbrs.conf";
     private static final String COMMENT_CHAR = "#";
     private static final String SPLIT_CHAR = ":";
     
     /**
      * This is static library class, therefore it is not possible to instantiate it.
      */
     private Utils() {}
     
     public static BibleBook getBibleBookNameByAbbr(String abbr) {
 	
 	Map<String, BibleBook> bookNames;
 	try {
 	    bookNames = getAbbrBookMap();
 	} catch (IOException e) {
 	    throw new RuntimeException("BibleBook name could not be retrieved.", e);
 	}
 	
	BibleBook book = bookNames.get(abbr);
	
	if (book == null)
	    throw new IllegalArgumentException("Bible book abbreviation '" + abbr + "' is unknown.");
	
	return book;
 	
     }
 
     public static Map<String, BibleBook> getAbbrBookMap() throws IOException {
 	Map<String, BibleBook> bookNames = new HashMap<String, BibleBook>();
 	BufferedReader r = null;
 	String line;
	
	try {	    
	    r = new BufferedReader(new InputStreamReader(Utils.class.getResourceAsStream(BIBLE_BOOK_ABBRS_FILE), "utf-8"));
	    
 	    while ((line = r.readLine()) != null) {
 		if (line.length() > 0 && !line.startsWith(COMMENT_CHAR)) {
 		    String[] abbrAndName = line.split(SPLIT_CHAR);
 		    if (abbrAndName.length > 2)
 			throw new RuntimeException("Bad format in configuration file " + BIBLE_BOOK_ABBRS_FILE);
 		    //System.out.println(abbrAndName[0] + " " + abbrAndName[1]);
 		    bookNames.put(abbrAndName[0], BibleBook.getBibleBookByName(abbrAndName[1]));
 		}
 	    }
 	} finally {
 	    r.close();
 	}
 
 	return bookNames;
     }
     
     public static Map<BibleBook, String> getBookAbbrMap() throws IOException {
 	Map<BibleBook, String> bookNames = new HashMap<BibleBook, String>();
 	BufferedReader r = null;
 	String line;
 
 	try {
 	    r = new BufferedReader(new InputStreamReader(new FileInputStream(BIBLE_BOOK_ABBRS_FILE), "utf-8"));
 
 	    while ((line = r.readLine()) != null) {
 		if (line.length() > 0 && !line.startsWith(COMMENT_CHAR)) {
 		    String[] abbrAndName = line.split(SPLIT_CHAR);
 		    if (abbrAndName.length > 2)
 			throw new RuntimeException("Bad format in configuration file " + BIBLE_BOOK_ABBRS_FILE);
 		    bookNames.put(BibleBook.getBibleBookByName(abbrAndName[1]), abbrAndName[0]);
 		}
 	    }
 	} finally {
 	    r.close();
 	}
 
 	return bookNames;
     }
     
     
     
     //for testing purposes
     public static void main(String[] args) {
	System.out.println(Utils.getBibleBookNameByAbbr("mt"));
 	System.out.println("finished");
     }
 
 }
