 package de.unikassel.ann.io;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.util.logging.Logger;
 
 import org.supercsv.exception.SuperCSVException;
 import org.supercsv.prefs.CsvPreference;
 
 public class BasicCsvReader {
 	
 	static protected Logger log = Logger.getAnonymousLogger();
 
 	static protected CsvPreference pref = new CsvPreference('\"', ';', "\r\n");
 	
	static public boolean checkEndOfFileAndMarkPosition(Exception e, BufferedReader bufferedReader) {
 		if (e instanceof SuperCSVException) {
 			if (((SuperCSVException)e).getCsvContext().lineSource.get(0).toString().equals("#>")) {
//				try {
//					bufferedReader.mark(1);
//				} catch (IOException e1) {
//					e1.printStackTrace();
//				}
 				return true;
 			} else {
 				e.printStackTrace();
 			}
 		} else {
 			e.printStackTrace();
 		}
 		return false;
 	}
 	
 
 }
