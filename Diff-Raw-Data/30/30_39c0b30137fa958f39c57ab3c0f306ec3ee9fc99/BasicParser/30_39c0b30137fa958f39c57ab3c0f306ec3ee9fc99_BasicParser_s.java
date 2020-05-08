 /**
  * Copyright 2012 Wim Rijnders <wrijnders@gmail.com>
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  *
  *=========================================================================
  *
  * TODO:
  * ====
  *
  * - Clean up and figure out all the throws-clauses
  * - Handle collecting of overrides in strings and charsets properly
  */
 package nl.axizo.parser;
 
 import java.io.*;
 import java.lang.reflect.Method;
 import java.lang.reflect.InvocationTargetException;
 import java.util.regex.*;
 import java.util.Vector;
 import java.util.regex.Pattern;
 
 
 public class BasicParser {
 	private static boolean showFirstTwoLines = false;
 	private static int     seqNr          = 1;
 
 	private String buffer =  "" ;
 
 	private Pattern whitespace;
 
 
 	/**
  	 * Load the parser buffer.
  	 *
  	 * @param buffer content to parse, or filename containing content to parse.
  	 * @param loadFromFile if true, load contents of given filename. Otherwise, load
  	 *                     buffer direct.
  	 */
 	public BasicParser(String buffer, boolean loadFromFile) {
 
 		whitespace      = Pattern.compile( "[ \r\n\t]+");
 
 		if ( loadFromFile ) {
 			this.buffer = loadfile( buffer );
 		} else {
 			this.buffer = buffer;
 		}
 		//trace( "curpos: " + curLine( 0 ) );
 	}
 
 	public BasicParser(String filename) {
 		this(filename, true);
 	}
 
 	public static void setDoneOutput   ( boolean val ) { Util.setDoneOutput( val); }
 	public static void setTraceLevel   ( int     val ) { Util.setTraceLevel( val); }
 	public static void setFirstTwoLines( boolean val ) { showFirstTwoLines = val; }
 
 	public static boolean getFirstTwoLines() { return showFirstTwoLines; }
 
 
 	protected static int getNextSeq() { return seqNr++; }
 
 	/**
 	 * Read contents of a file into a String.
 	 */
 	protected static String loadfile(String filename) {
 		String buffer = "";
 
 		File file = new File(filename);
 
 		if ( !file.exists() ) return null;
 
 		try {
 			FileReader reader = new FileReader(file);
 
 			final int BUF_SIZE = 1024;
 			char[] array = new char[ BUF_SIZE];
 
 			// read the entire content of the file into the string
 			int countRead = 0;
 			int total = 0;
 			while( (countRead = reader.read( array, 0, BUF_SIZE ) ) != -1 ) {
 				buffer += new String( array, 0, countRead );
 				total += countRead;
 			} 
 
 			trace( "Read in " + total + " characters.");
 
 			reader.close();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 
 		return buffer;
 	}
 
 
 	/**
 	 * Return the line in the buffer starting at given position, and also 
 	 * the entire next line.
 	 */
 	public String curLine(int curpos) {
 		String ret = "";
 
 		// Find second EOL starting from curPos
 		int eolIndex = buffer.indexOf( "\n", curpos);
 
 		if ( eolIndex != -1 ) {
 			int tempIndex = buffer.indexOf( "\n", eolIndex + 1);
  	
 			if ( tempIndex != -1 ) eolIndex = tempIndex;
 		}
 
 		// If no index found, assume that we are at the end of the buffer
 		if ( eolIndex == -1 ) {
 			ret = buffer.substring( curpos);
 		} else {
 			ret = buffer.substring( curpos, eolIndex);
 		}
 
 		return ret;
 	}
 
 	protected static void trace  (int level, String str) { Util.trace( level  , str); }
 	protected static void trace  (String str) { Util.trace( str); }
 	protected static void info   (String str) { Util.info( str); }
 	protected static void warning(String str) { Util.warning( str); }
 	protected static void error  (String str) { Util.error( str); }
 
 
 
 	/**
 	 * Make a dynamic call to a rule.
 	 *
 	 * The intention is to call generated syntax rules dynamically. The name is 
 	 * intentionally chosen short (one letter), because this call appears often in the
 	 * generated code, and we want to keep the latter readable.
 	 *
 	 * @param method   Name of method to call
 	 * @param oldState Parse state of the calling method
 	 * @param doThrow  If true, throw a ParseException if parsing fails. Default is false.
 	 * @param ignore   If true, do not save parsed content to parse tree. Default is false.
 	 */
 	protected boolean s(
 		String method,
 		State oldState,
 		boolean doThrow,
 		boolean ignore
 	) throws ParseException {
 		return Util.s( this, method, oldState, doThrow, ignore );
 	}
 
 
 	/**
 	 * Make a dynamic call to a rule.
 	 *
  	 * Override of {@link #s(String,State,boolean,boolean) s(String,State,boolean,boolean)}, with
  	 * addition of parsed values to parse tree. 
  	 */
 	protected boolean s( String method, State oldState, boolean doThrow ) throws ParseException {
 		return Util.s( this, method, oldState, doThrow, false ); 
 	}
 
 
 	/**
 	 * Make a dynamic call to a rule.
 	 *
  	 * Override of {@link #s(String,State,boolean,boolean) s(String,State,boolean,boolean)}, with
  	 * throwing on error disabled and
  	 * addition of parsed values to parse tree. 
  	 */
 	protected boolean s( String method, State oldState ) throws
 			ParseException//, 
 			//NoSuchMethodException, 
 			//IllegalAccessException 
 	{
 		return Util.s( this, method, oldState, false, false ); 
 	}
 
 
 	protected boolean parseString( String str, State state, boolean doThrow, boolean ignore )
 			throws ParseException {
 
 /* WRI: NOT NEEDED. regionMatches handles this case fine
  
 		// Special case end of file handling.
 		// If string to test goes past buffer, there can never be a match
 		if ( state.getCurpos() + str.length() > buffer.length() ) {
 			if ( doThrow ) throw new ParseException();
 			return false;
 		}
 */
 		//String curStr = buffer.substring( state.getCurpos(), state.getCurpos() + str.length() );
 		//if ( curStr.equals( str ) ) {
 		if ( buffer.regionMatches( state.getCurpos(), str, 0, str.length() ) ) {
 			state.matched( str, "string", ignore );
 			return true;
 		} else {
 			if ( doThrow ) throw new ParseException();
 			return false;
 		}
 	}
 
 	protected boolean parseString( String str, State state, boolean doThrow )  
 			throws ParseException {
 
 		return parseString( str, state, doThrow, false);
 	} 
 
 	protected boolean parseString( String str, State state ) throws ParseException {
 		return parseString( str, state, false );
 	}
 
 
 	protected boolean parseCharset( Pattern pattern, State state, boolean doThrow,
 			boolean ignore ) throws ParseException {
 
 		Matcher matcher = pattern.matcher( buffer );
 		if ( matcher.find( state.getCurpos() ) && state.getCurpos() == matcher.start() ) {
 			String str = matcher.group();
 			state.matched( str ,  "charset", ignore );
 			return true;
 		} else {
 			if ( doThrow ) throw new ParseException();
 			return false;
 		}
 	}
 
 	protected boolean parseCharset( Pattern pattern, State state, boolean doThrow )
 			throws ParseException {
 		return parseCharset( pattern, state, doThrow, false );
 	}
 
 	protected boolean parseCharset( Pattern pattern, State state )
 			throws ParseException {
 		return parseCharset( pattern, state, false, false );
 	}
 
 
 	/**
 	 * Save the Node tree to file.
 	 *
 	 * Write the values of the node contained in the passed state parameter to file.
 	 * Values of child nodes are written indented in relation to the parent node. 
 	 * the output is a textual representation of the Node-tree.
 	 */
 	public static void saveNodes( State state, String localFileName ) {
 
 		if ( localFileName == null ) {
 			localFileName = "nodes.txt";
 		}
 
 		//String output = state.getCurNode().show( getFirstTwoLines() );
 		String output = state.getOutput( getFirstTwoLines() );
 
 		try {
 			Util.saveFile( localFileName, output);
 		} catch ( IOException e ) {
 			error("saveNodes exception: "+ e.toString() );
 		}
 	}
 
 	/**
  	 * Get the final status of the parsing.
  	 *
  	 * Returns string with final status info.
 	 * Status info is also sent to the logger.
  	 */
 	public String showFinalResult( State state ) {
 		String out = "";
 		try {
 			if ( eol(state.getCurpos() ) ) {
 				out = "Parsing completed succesfully.";
 				info( out );
 			} else { 
 				if ( state.getErrorPos() != -1 ) {
 					out = "Error in label '" + state.getErrorMethod() + "' at: " + 
 						curLine( state.getErrorPos() );
 				} else {
					out = "Parsing failed";
 				}
 				error ( out );
 			}
 			trace( "curpos: " + curLine( state.getCurpos() ) );
 		} catch ( ParseException e ) {
 			out =  "Exception: " + e.toString();
 			error( out );
 		}
 		return out;
 	}
 
 
 	/** 
  	 * Determine if given position in buffer is at the end of the buffer.
  	 * 
  	 * @return true if and end of buffer, false otherwise.
  	 */
 	public boolean eol(int curpos) throws ParseException {
 		if ( curpos > buffer.length() ) { 
 			throw new ParseException( "Parser passed end of input buffer. Curpos:" + 
 				curpos + "; buffer length: " + buffer.length() );
 		}
 
 		return curpos == buffer.length();
 	}
 
 
 	/**
  	 * Default implementation of whitespace.
  	 *
  	 * Skip tabs, end of lines and spaces.
  	 *
  	 * This whitespace handling is enabled by default. Differing
  	 * implementations should be overridden in subclasses.
  	 */
 	public boolean WS_intern(State state ) throws ParseException {
 		parseCharset( whitespace, state);
 		return true;
 	}
 
 
 	/**
  	 * Default WS rule for all parsers.
  	 */
 	protected boolean WS(State state ) throws ParseException {
 
 		s( "WS_intern", state, false, true);
 
 		return true;
 	}
 
 
 	/**
      * Placeholder for the parser entry method.
      * Should be defined in derived parsers.
      */
 	public State parse() {
 		State state = new State();
 		state.setError("Method parse() not defined.");
 		return state;
 	}
 }
 
