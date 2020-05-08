 //
 // CsvP5.java
 // CsvP5 (v.##library.prettyVersion##) is released under the MIT License.
 //
 // Copyright (c) 2012, Tim Pulver & Paul Vollmer http://www.fh-potsdam.de
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
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.logging.ConsoleHandler;
 import java.util.logging.Handler;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import processing.core.*;
 import de.fhpotsdam.util.math.Math;
 
 /**
  * CsvP5
  * Reading CSV(Character Separated Values) files.
  * Based on an example from Ben Fry's Visualizing Data Book.
  * @author ##author.name##
  * @example csvP5simple
  */
 public class CsvP5 {
 	private static final String DEFAULT_SEPARATOR = ",";
 	private static final String DEFAULT_COMMENT = "#";
 	private static final String QUOTATION_MARK = "\"";
 	private static final boolean REMOVE_ENCLOSING_QUOTATION_MARKS_DEFAULT = true;
 
 	private PApplet p5;                            // processing reference for text loading and other stuff
 	private String filename;                       // filename to load
 	private String separator;                      // default separator is ','
 	private String comment;                        // default comment char is '#'
 	private boolean hasEnclosingQuotationMarks;
 	private boolean hasRowHeaders = false;          // if the document has horizontal headers
 	private boolean hasColumnHeaders = false;       // if the document has vertical headers
 	private HashMap<Integer, String> columnHeaders;    // stores the headlines (column index, headline name)
 
 	// flags - will be set while processing
 	private boolean isComplete = false;
 	private int firstIncompleteLine = -1;
 	
 	// Contains total number of rows/columns after loading a file.
 	// rowCounts will not contain headline if hasHeadline(true) has been called, 
 	// will not contain skipped lines e.g. when you called startAtRow(...) before and empty lines. 
 	private int totalRows, totalColumns;
 	// Writer object - use for all modifications / writing
 	public CsvP5Writer writer;
 	
 	/**
 	 * The actual csv stored in an data array [row][column].
 	 */
 	private String[][] data;
 	
 	public Math math;
 	
 	// Logger for logging / debugging
 	private final static Logger LOGGER = Logger.getLogger(CsvP5.class .getName());
 	private final static Level DEFAULT_DEBUG_LEVEL = Level.INFO;  
 	private static Level debugLevel;
 
 	/*
 	 * ======================================================================|
 	 * CONSTRUCTORS ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~|
 	 * ======================================================================|
 	 */
 	
 	// Forbidden, use the other constructor instead
 	@SuppressWarnings("unused")
 	private CsvP5(){}
 
 	/**
 	 * Constructor for CsvP5.
 	 * @param p Use "this" from within your Processing main sketch
 	 * @param filename Filename of a csv-file in your data-folder e.g. "awesome_data.csv"
 	 */
 	public CsvP5(PApplet p, String filename) {
 		LOGGER.log( Level.FINEST, "Constructor called");
 		init(p, filename, DEFAULT_SEPARATOR, DEFAULT_COMMENT, REMOVE_ENCLOSING_QUOTATION_MARKS_DEFAULT);
 	}
 	
 	
 	/*
 	 * ======================================================================|
 	 * PUBLIC FUNCTIONS ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~|
 	 * ======================================================================|
 	 */
 	
 	/**
 	 * Resets the processing-flags and starts the actual CSV-processing. 
 	 * All functions like setSeparator() or setComment() must be called <b>before</b> calling load()!
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
 	 * Has to be called if the first row of your CSV-file (on top) contains headers. Default is no column headers (false). 
 	 * @param b True, if is has column headers, false otherwise
 	 */
 	public void hasColumnHeaders(boolean b){
 		LOGGER.log(Level.FINEST, "hasRowHeaders set to " + b);
 		this.hasColumnHeaders = b;
 	}
 	
 	/**
 	 * Has to be called if the first <b>column</b> of your CSV-file (left) contains headers. Default is no row headers (false). 
 	 * @param b True, if is has row headers, false otherwise
 	 */
 	public void hasRowHeaders(boolean b){
 		LOGGER.log(Level.FINEST, "hasRowHeaders set to " + b);
 		this.hasRowHeaders = b;
 	}
 
 	/**
 	 * This is an indicator if loading the CSV-file went smooth. 
 	 * If it happened, that the number of the separator char/string  
 	 * in every row differs, this will return false. There is no check for  
 	 * holes, so parsing a row like "data,,data,data" will be fine and 
 	 * just contain a blank element.
 	 * @return true, if there were no problems parsing the file. 
 	 */
 	public boolean isComplete() {
 		return isComplete;
 	}
 
 	/**
 	 * If {@link #isComplete()} returns false, which means, there were errors 
 	 * parsing the CSV-file, this returns the row index of the CSV-file, which can be useful if 
 	 * you search for "bad spots" in the CSV-file and need to fix them manually.    
 	 * @return The index of the first problematic row, note that index begins at 0, 
 	 * line numbers of most text editors start at 1.
 	 */
 	public int getFirstIncompleteRowIndex() {
 		return firstIncompleteLine;
 	}
 	
 	/**
 	 * Returns the number of rows. If the document contains row headers and  
 	 * {@link #hasRowHeaders(boolean)} has been called with <i>true</i> before CSV-import, this will <b>not</b> 
 	 * contain the header row.
 	 * @return rowCount number of rows (without header row)
 	 */
 	public int getTotalRows() {
 		return totalRows;
 	}
 
 	/**
 	 * Returns the number of columns.
 	 * @return columnCount Number of columns
 	 */
 	public int getTotalColumns() {
 		return totalColumns;
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
 		for (Map.Entry<Integer, String> e : columnHeaders.entrySet()) {
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
 	public String getColumnHeader(int column) {
		LOGGER.log(Level.FINEST, "Function start");
 		if(columnHeaders.containsKey(column)){
			LOGGER.log(Level.FINEST, "Header with index " + column + "found!");
			LOGGER.log(Level.FINEST, "Headername: " + columnHeaders.get(column));
 			return columnHeaders.get(column);
 		}
 		else{
 			LOGGER.log(Level.WARNING, "There is no headline with index " + column 
 					+ ". Did you call hasHeadline(true) before loading the file?");
 			return "";
 		}
 	}
 	
 	/*
 	 * ======================================================================|
 	 * PUBLIC ELEMENT GETTERS ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~|
 	 * ======================================================================|
 	 */
 
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
 							+ "Seems like you tried to parse a non-float data field.");
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
 	 * @param commentIndicator the new comment indicator
 	 */
 	public void setCommentIndicator(String commentIndicator){
 		LOGGER.log(Level.FINE, "Comment indicator set to " + commentIndicator); 
 		this.comment = commentIndicator;
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
 	 * PRIVATE FUNCTIONS ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~|
 	 * ======================================================================|
 	 */
 	
 	/**
 	 * Get's called by the constructor to store / initiate the class variables. 
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
 	 * Helper function, returns the number of elements in the first 
 	 * non-comment line, will be used to see of the number of elements 
 	 * from every row match each other (if the CSV file is complete).	 * 
 	 * @param rows The raw CSV-data
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
 	 * Checks if the elements start and end with quotation marks (e.g. "data" -> data) 
 	 * and deletes them if found.  
 	 * This will only delete the <b>first</b> and <b>last</b> quotation mark,  
 	 * if there are more, they will be left untouched.
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
				+ "hasHeadline: " + hasColumnHeaders);
		if(hasColumnHeaders){
 			columnHeaders = new HashMap<Integer, String>();
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
 			data[totalRows] = PApplet.trim(pieces);
 			// check if the number of elements is the same as in the first line
 			if (data[totalRows].length != nFirstLineElements) {
 				LOGGER.log(Level.WARNING, "Found an incomplete line in the CSV! Row: " + totalRows 
 						+ ". Number of elements in first row and this row do not match!");
 				// set flag if not
 				isComplete = false;
 				if (firstIncompleteLine != -1) {
 					firstIncompleteLine = totalRows;
 				}
 			}
 			totalRows++;
 		}
 		int startIndex = 0;
		if(hasColumnHeaders){
 			startIndex = 1;
 			// store the headlines
 			for(int i=0; i<nFirstLineElements; i++){
 				columnHeaders.put(i, data[0][i]);
 			}
 		}
 		// resize the 'data' array as necessary, 
 		// if there is the headline remove it from the array
 		data = (String[][]) PApplet.subset(data, startIndex, totalRows-startIndex);
 		// update rowCount
 		totalRows = data.length;
 		// Store the number of columns (data entries per line)
 		if (data.length >= 1) {
 			totalColumns = data[0].length;
 		}
 		else{
 			LOGGER.log(Level.WARNING, "There was an error importing the data. You can try to: " +
 					"Set the separator and make sure that the line endings are okay");
 		}
 	}
 }
