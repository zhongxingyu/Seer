 package edu.umass.ciir;
 
import java.io.BufferedReader;
 import java.io.Reader;
 
 public class StringRecordReader extends FileRecordReader<String> {
 
 	/**
 	 * Constructor, opens the file for reading.  If this successfully completes the client must
 	 * call close() to ensure the file is closed!
 	 * 
 	 * @param fileToRead
 	 */
 	public StringRecordReader(Reader reader, boolean catchParseExceptions, PrefixFilter prefixFilter) throws Exception {
 		super(reader, catchParseExceptions, prefixFilter);
 	}
 	
 	@Override
 	protected String parseLine(String input) throws Exception {
 		return input;
 	}
 	
 
 }
