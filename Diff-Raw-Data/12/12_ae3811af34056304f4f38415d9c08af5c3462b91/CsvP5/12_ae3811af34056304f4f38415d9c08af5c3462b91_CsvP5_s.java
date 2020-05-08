 //
 // CsvP5.java
 // ##library.name## (v.##library.prettyVersion##) is released under the MIT License.
 //
 // Copyright (c) 2012, ##author.name## http://www.fh-potsdam.de
 //
 // Permission is hereby granted, free of charge, to any person obtaining a copy
 // of this software and associated documentation files (the "Software"), to deal
 // in the Software without restriction, including without limitation the rights
 // to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 // copies of the Software, and to permit persons to whom the Software is
 // furnished to do so, subject to the following conditions:
 //
 // The above copyright notice and this permission notice shall be included in
 // all copies or substantial portions of the Software.
 //
 // THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 // IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 // FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 // AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 // LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 // OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 // THE SOFTWARE.
 //
 
 package de.fhpotsdam.io.csv;
 
 //import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.logging.ConsoleHandler;
 import java.util.logging.Handler;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import processing.core.*;
 
 /**
  * CsvP5
  * 
  * based on Ben Fry's Visualizing Data Book
 * @author Paul Vollmer, Tim Pulver
 * @modified 2012-11-13
 * @version 0.1
 * @example SimpleExample.pde
  */
 public class CsvP5 {
 	private static final String DEFAULT_SEPARATOR = ",";
 	private static final String DEFAULT_COMMENT = "#";
 	private static final String QUOTATION_MARK = "\"";
 	private static final boolean REMOVE_ENCLOSING_QUOTATION_MARKS_DEFAULT = true;
 
 	PApplet p5; // processing reference for text loading and other stuff
 	private String filename; // filename to load
 	private String separator; // default separator is ','
 	private String comment; // default comment char is '#'
 	private boolean hasEnclosingQuotationMarks;
 	private boolean hasHeadline = false; // if the document has a headline
 	private HashMap<Integer, String> headlines; // stores the headlines (column index, headline name)
 
 	// flags - will be set while processing
 	private boolean isComplete = false;
 	private int firstIncompleteLine = -1;
 	
 	// Contains total number of rows/columns after loading a file.
 	// rowCounts will not contain headline if hasHeadline(true) has been called
 	private int rowCount, columnCount;
 	
 	/**
 	 * The actual csv stored in an data array [row][column].
 	 */
 	public String[][] data;
 	
 	// Logger for logging / debugging
 	private final static Logger LOGGER = Logger.getLogger(CsvP5.class .getName());
 	private final static Level DEFAULT_DEBUG_LEVEL = Level.INFO;  
 	private static Level debugLevel;
 
 	/*
 	 * ======================================================================|
 	 * CONSTRUCTORS ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~|
 	 * ======================================================================|
 	 */
 	
 	/**
 	 * Forbidden, use the other constructors instead
 	 */
 	@SuppressWarnings("unused")
 	private CsvP5(){}
 	
 	/**
 	 * Constructor with least arguments, use this if your csv-file  
 	 * uses commas as separator and '#' for comments.
 	 * @param p Use "this" from within your Processing main sketch
 	 */
 	public CsvP5(PApplet p) {
 		LOGGER.log( Level.FINEST, "Constructor called");
 		init(p, "", DEFAULT_SEPARATOR, DEFAULT_COMMENT, REMOVE_ENCLOSING_QUOTATION_MARKS_DEFAULT);
 	}
 
 	/**
 	 * Constructor with least arguments, use this if your csv-file  
 	 * uses commas as separator and '#' for comments.
 	 * @param p Use "this" from within your Processing main sketch
 	 * @param filename Filename of a csv-file in your data-folder e.g. "awesome_data.csv"
 	 */
 	public CsvP5(PApplet p, String filename) {
 		LOGGER.log( Level.FINEST, "Constructor called");
 		init(p, filename, DEFAULT_SEPARATOR, DEFAULT_COMMENT, REMOVE_ENCLOSING_QUOTATION_MARKS_DEFAULT);
 	}
 	
 	/**
 	 * Constructor with additional separator argument, use this if your csv-file 
 	 * <b>not</b> uses commas as separators.
 	 * @param p Use "this" from within your Processing main sketch
 	 * @param filename Filename of a csv-file in your data-folder e.g. "awesome_data.csv"
 	 * @param separator The character/string to use as separator e.g. ";" or "\t" (Tab)
 	 */
 	public CsvP5(PApplet p, String filename, String separator) {
 		LOGGER.log(Level.FINEST, "Constructor called");
 		init(p, filename, separator, DEFAULT_COMMENT, REMOVE_ENCLOSING_QUOTATION_MARKS_DEFAULT);
 	}
 	
 	/**
 	 * Constructor with additional separator and quotation-mark arguments, use this if your csv-file 
 	 * <b>not</b> uses commas as separators and has encolsing quotation marks 
 	 * e.g. "data1";"data2",... 
 	 * @param p Use "this" from within your Processing main sketch
 	 * @param filename Filename of a csv-file in your data-folder e.g. "awesome_data.csv"
 	 * @param separator The character/string to use as separator e.g. ";" or "\t" (Tab)
 	 * @param removeEnclosingQuotationMarks If the data fields are surrounded by 
 	 * enclosing quotation marks (e.g. "data1";"data2"), pass <i>true</> here. 
 	 */
 	public CsvP5(PApplet p, String filename, String separator, boolean removeEnclosingQuotationMarks) {
 		LOGGER.log(Level.FINEST, "Constructor called");
 		init(p, filename, separator, DEFAULT_COMMENT,
 				removeEnclosingQuotationMarks);
 	}
 	/**
 	 * Constructor with additional separator, comment character and quotation-mark arguments, use this if your csv-file 
	 * <b>not</b> uses commas as separatorsand <b>not</> uses '#' as comment indicators. 
 	 * @param p Use "this" from within your Processing main sketch
 	 * @param filename Filename of a csv-file in your data-folder e.g. "awesome_data.csv"
 	 * @param separator The character/string to use as separator e.g. ";" or "\t" (Tab)
 	 * @param comment The character/string which is used to introduce comments
 	 * @param removeEnclosingQuotationMarks If the data fields are surrounded by 
	 * enclosing quotation marks (e.g. "data1";"data2"), pass <i>true</> here. 
 	 */
 	public CsvP5(PApplet p, String filename, String separator, String comment, boolean removeEnclosingQuotationMarks) {
 		LOGGER.log(Level.FINEST, "Constructor called");
 		init(p, filename, separator, comment, removeEnclosingQuotationMarks);
 	}
 	
 	/*
 	 * ======================================================================|
 	 * PUBLIC FUNCTIONS ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~|
 	 * ======================================================================|
 	 */
 
 	/**
 	 * Resets the processing-flags and starts the actual CSV-processing.
 	 * @param filename The csv filepath.
 	 */
 	public void load(String filename) {
 		this.filename = filename;
 		if(filename.equals("")){
 			LOGGER.log(Level.WARNING, "Could not load file '" + filename + "' - filename is empty!");
 			return;
 		}
 		load();
 	}
 	
 	/**
 	 * Resets the processing-flags and starts the actual CSV-processing.
 	 */
 	public void load() {
 		LOGGER.log(Level.FINEST, "Load called");
 		if(filename.equals("")){
 			LOGGER.log(Level.WARNING, "No filename has been set! Make sure to call setFilename() before!");
 			return;
 		}
 		resetFlags();
 		loadFile(filename, separator, comment, hasEnclosingQuotationMarks);
 	}
 
 	/**
 	 * Has to be called if your CSV-file has a headline. Default is no headline. 
 	 * @param b True, if is has a headline, false otherwise
 	 */
 	public void hasHeadline(boolean b){
 		LOGGER.log(Level.FINEST, "hasHeadline set to " + b);
 		this.hasHeadline = b;
 	}
 
 	/**
 	 * Returns true, if there were no problems parsing the file. 
 	 * @return
 	 */
 	public boolean isComplete() {
 		return isComplete;
 	}
 
 	/**
 	 * If {@link #isComplete()} returns true, which means, there were errors 
 	 * parsing the CSV-file, this will return the first line with parsing-errors. 
 	 * @return The first problematic line.
 	 */
 	public int firstIncompleteLine() {
 		return firstIncompleteLine;
 	}
 	
 	/**
 	 * Returns the number of rows. If the document has headlines and 
 	 * hasHeadlines(true) has been called before CSV-import, this will <b>not</b> 
 	 * contain the headline.
 	 * @return rowCount Number of rows (without headline)
 	 */
 	public int getRowCount() {
 		return rowCount;
 	}
 
 	/**
 	 * Returns the number of columns.
 	 * @return columnCount Number of columns
 	 */
 	public int getColumnCount() {
 		return columnCount;
 	}
 
 	/**
 	 * Returns a column index by its name, returns -1 if no column has been
 	 * found. This can be used to find out a column index of a certain CSV-headline. 
 	 * Note, that this does not take care of the fact, that there can be more than 
 	 * one headlines with the same name. 
 	 * @param name name of the column
 	 * @return integer index value of the column
 	 */
 	public int getColumnIndex(String name) {
 		for (Map.Entry<Integer, String> e : headlines.entrySet()) {
 		    int key = e.getKey();
 		    String value = e.getValue();
 		    if(value.equals(name)){
 		    	return key;
 		    }
 		}
 		LOGGER.log(Level.WARNING, "No column named '" + name + "' was found");
 		return -1;
 	}
 
 	/**
 	 * Returns the name of a headline. To get the first headline, pass <i>0</>. 
 	 * @param column the Column number, which name you want to know
 	 * @return String The column name (headline)
 	 */
 	public String getHeadlineName(int column) {
 		if(headlines.containsKey(column)){
 			return headlines.get(column);
 		}
 		else{
 			LOGGER.log(Level.WARNING, "There is no headline with index " + column 
 					+ ". Did you call hasHeadline(true) before loading the file?");
 			return "";
 		}
 	}
 
 	/**
 	 * Returns the String of a specific row and column (untouched from the CSV-file).
 	 * @param rowIndex Row number
 	 * @param columnIndex Column number
 	 * @return String The data field as String
 	 */
 	public String getString(int rowIndex, int columnIndex) {
 		return data[rowIndex][columnIndex];
 	}
 
 	/**
 	 * Returns the String of a specific row and column.
 	 * @param columnName name of the column
 	 * @param rowIndex row number
 	 * @return String The data field as String
 	 */
 	public String getString(String columnName, int rowIndex) {
 		return getString(rowIndex, getColumnIndex(columnName));
 	}
 
 	/**
 	 * Returns the integer of a specific row and column.
 	 * @param columnName name of the column
 	 * @param rowIndex row number
 	 * @return integer The data field as integer
 	 */
 	public int getInt(String columnName, int rowIndex) {
 		return getInt(rowIndex, getColumnIndex(columnName));
 	}
 
 	/**
 	 * Returns the integer of a specific row and column.
 	 * @param rowIndex row number
 	 * @param columnIndex column number
 	 * @return integer The data field as integer
 	 */
 	public int getInt(int rowIndex, int columnIndex) {
 		String sElement = getString(rowIndex, columnIndex);
 		int ret = -1;
 		// return -1 when string is empty
 		if(sElement == ""){
 			return ret;
 		}
 		try {
 			ret = Integer.parseInt(sElement);
 		} catch (NumberFormatException e) {
 			LOGGER.log(Level.WARNING, "Could not parse "
 							+ sElement
 							+ " to int! "
 							+ "Seems like you tried to parse a non-integer data field.");
 		}
 		return ret;
 	}
 
 	/**
 	 * Returns the float of a specific row and column.
 	 * @param columnName name of the column
 	 * @param rowIndex row number
 	 * @return The data field as float
 	 */
 	public float getFloat(String columnName, int rowIndex) {
 		return getFloat(rowIndex, getColumnIndex(columnName));
 	}
 
 	/**
 	 * Returns the float of a specific row and column.
 	 * @param columnName name of the column
 	 * @param rowIndex row number
 	 * @return The data field as float
 	 */
 	public float getFloat(int rowIndex, int columnIndex) {
 		String sElement = getString(rowIndex, columnIndex);
 		float ret = -1;
 		try {
 			ret = Float.parseFloat(sElement);
 		} catch (NumberFormatException e) {
 			LOGGER.log(Level.WARNING, "Could not parse "
 							+ sElement
 							+ " to float! "
 							+ "Seems like you tried to parse a non-integer data field.");
 		}
 		return ret;
 	}
 	
 	/**
 	 * Returns the current separator char/string.
 	 * @return the separator
 	 */
 	public String getSeparator(){
 		return separator;
 	}
 	
 	/**
 	 * Returns the current char/string used to begin a comment
 	 * @return the comment string
 	 */
 	public String getCommentIndicator(){
 		return comment;
 	}
 	
 	/*
 	 * ======================================================================|
 	 * PUBLIC SETTERS ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~|
 	 * ======================================================================|
 	 */
 	
 	/**
 	 * Assigns a new separator, default is {@link #DEFAULT_SEPARATOR}
 	 * @param separator the new separator to use
 	 */
 	public void setSeparator(String separator){
 		this.separator = separator;
 	}
 	
 	/**
 	 * Sets a new comment indicator, default is {@link #DEFAULT_COMMENT}
 	 * @param comment the new comment indicator
 	 */
 	public void setCommentIndicator(String comment){
 		LOGGER.log(Level.FINE, "Comment indicator set to " + comment); 
 		this.comment = comment;
 	}
 	
 	/**
 	 * Sets a new relative filename pointing to a CSV-File within the data directory. 
 	 * (e.g. "awesome_data.csv")
 	 * @param filename
 	 */
 	public void setFilename(String filename){
 		LOGGER.log(Level.FINE, "Filename set to " + filename);
 		this.filename = filename;
 	}
 	
 	/**
 	 * If there are problems, try to call this using <i>Level.FINEST</i>. 
 	 */
 	public static void setDebugLevel(Level level){
 		debugLevel = level;
 		LOGGER.log(Level.INFO, "Debug Level set to " + level.getName());
 		initLogLevel();
 	}
 	
 		
 	/*
 	 * ======================================================================|
 	 * PRIVATE FUNCTIONS ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~|
 	 * ======================================================================|
 	 */
 	
 	/**
 	 * Get's called by every constructor to store / initiate the class variables. 
 	 */
 	private void init(PApplet p, String filename, String separator,
 			String comment, boolean hasEnclosingQuotationMarks) {
 		LOGGER.log(Level.FINEST, "Initialising variables");
 		this.p5 = p;
 		this.filename = filename;
 		this.separator = separator;
 		this.comment = comment;
 		this.hasEnclosingQuotationMarks = hasEnclosingQuotationMarks;
 		debugLevel = DEFAULT_DEBUG_LEVEL;
 		initLogLevel();
 	}
 
 	/**
 	 * Resets the processing flags, which can be used to see if there were 
 	 * errors while loading/parsing the file
 	 */
 	private void resetFlags() {
 		LOGGER.log(Level.FINE, "Resetting the debug flags");
 		isComplete = false;
 		firstIncompleteLine = -1;
 	}
 	
 	/**
 	 * Sets the current log level to {@link #debugLevel}.
 	 */
 	private static void initLogLevel(){
 		// Set new level on logger
 		LOGGER.setLevel(debugLevel);
 	    // Handler for console (reuse it if it already exists)
 	    Handler consoleHandler = null;
 	    //see if there is already a console handler
 	    for (Handler handler : LOGGER.getHandlers()) {
 	        if (handler instanceof ConsoleHandler) {
 	            //found the console handler
 	            consoleHandler = handler;
 	            break;
 	        }
 	    }
 	    if (consoleHandler == null) {
 	        //there was no console handler found, create a new one
 	        consoleHandler = new ConsoleHandler();
 	        LOGGER.addHandler(consoleHandler);
 	    }
 	    //set the console handler to fine:
 	    consoleHandler.setLevel(debugLevel);
 	}
 	
 	/**
 	 * Returns the number of elements in the first non-comment line
 	 * 
 	 * @param rows The CSV-data
 	 * @return Number of elements
 	 */
 	private int getNumberOfElements(String[] rows) {
 		// find first data line, skip empty lines
 		for (int i = 0; i < rows.length; i++) {
 			// skip empty rows
 			if (PApplet.trim(rows[i]).length() == 0) {
 				continue;
 			}
 			// skip comment lines
 			if (rows[i].startsWith(comment)) {
 				continue;
 			}
 			String[] pieces = PApplet.split(rows[i], separator);
 			if (hasEnclosingQuotationMarks) {
 				removeEnclosingQuotationMarks(pieces);
 			}
 			// Get rid of unnecessary leading and ending spaces
 			// and return number of elements of first "usable" line
 			pieces = PApplet.trim(pieces);
 			return pieces.length;
 		}
 		LOGGER.log(Level.WARNING, "Seams like ther are only comment or blank lines!");
 		return 0;
 	}
 
 	/**
 	 * Checks if the elemnets start and end with quotation marks (e.g. "data" -> data) 
 	 * and deltes them if found.  
 	 * This will only delete the <b>first</b> and </last> quotation mark. 
 	 * If there are more, they will be left untouched.
 	 * @param arr Array of Strings
 	 */
 	private void removeEnclosingQuotationMarks(String[] arr) {
 		LOGGER.log(Level.FINE, "Removing enclosing quotation marks");
 		for (int i = 0; i < arr.length; i++) {
 			if (arr[i].startsWith(QUOTATION_MARK)
 					&& arr[i].endsWith(QUOTATION_MARK) && arr[i].length() > 1) {
 				// remove starting and ending quotation marks
 				arr[i] = arr[i].substring(1, arr[i].length() - 1);
 			}
 		}
 	}
 
 	/**
 	 * Does the actual CSV file parsing, skips empty and commented-out lines.  
 	 * Gets called by other loadFile() methods.  
 	 * @param filename The filename of the csv-file within the data directory
 	 * @param separator Separator char/string
 	 * @param comments Introducing char/string for comments
 	 * @param removeEnclosingQuotationMarks Whether or not the data-elements are 
 	 * surrounded by quotation marks 
 	 */
 	private void loadFile(String filename, String separator, String comment, boolean removeEnclosingQuotationMarks) {
 		LOGGER.log(Level.FINE, "Beginning to load file: " + filename, ", separator: " 
 				+ separator + ", comment: " + comment + ", removeEnclosingQuotationMarks: " + removeEnclosingQuotationMarks
 				+ "hasHeadline: " + hasHeadline);
 		if(hasHeadline){
 			headlines = new HashMap<Integer, String>();
 		}
 		String[] rows = p5.loadStrings(filename);
 
 		data = new String[rows.length][];
 		int nFirstLineElements = getNumberOfElements(rows);
 		LOGGER.log(Level.FINEST, "number of elements in first line " + nFirstLineElements);
 
 		for (int i = 0; i < rows.length; i++) {
 			isComplete = true;
 			// skip empty rows
 			if (PApplet.trim(rows[i]).length() == 0) {
 				continue;
 			}
 			// skip comment lines
 			if (rows[i].startsWith(comment)) {
 				continue;
 			}
 
 			String[] pieces = PApplet.split(rows[i], separator);
 			if (removeEnclosingQuotationMarks) {
 				removeEnclosingQuotationMarks(pieces);
 			}
 			// Get rid of unnecessary leading and ending spaces
 			data[rowCount] = PApplet.trim(pieces);
 			// check if the number of elements is the same as in the first line
 			if (data[rowCount].length != nFirstLineElements) {
 				LOGGER.log(Level.WARNING, "Found an incomplete line in the CSV! Row: " + rowCount 
 						+ ". Number of elements in first row and this row do not match!");
 				// set flag if not
 				isComplete = false;
 				if (firstIncompleteLine != -1) {
 					firstIncompleteLine = rowCount;
 				}
 			}
 			rowCount++;
 		}
 		int startIndex = 0;
 		if(hasHeadline){
 			startIndex = 1;
 			// store the headlines
 			for(int i=0; i<nFirstLineElements; i++){
 				headlines.put(i, data[0][i]);
 			}
 		}
 		// resize the 'data' array as necessary, 
 		// if there is the headline remove it from the array
 		data = (String[][]) PApplet.subset(data, startIndex, rowCount-startIndex);
 		// update rowCount
 		rowCount = data.length;
 		// Store the number of columns (data entries per line)
 		if (data.length >= 1) {
 			columnCount = data[0].length;
 		}
 		else{
 			LOGGER.log(Level.WARNING, "There was an error importing the data. You can try to: " +
 					"Set the separator and make sure that the line endings are okay");
 		}
 	}
 }
