 /*
 
 Copyright: 2010 Bindley Bioscience Center, Purdue University
 
 License: X11 license.
 
 	Permission is hereby granted, free of charge, to any person
 	obtaining a copy of this software and associated documentation
 	files (the "Software"), to deal in the Software without
 	restriction, including without limitation the rights to use,
 	copy, modify, merge, publish, distribute, sublicense, and/or sell
 	copies of the Software, and to permit persons to whom the
 	Software is furnished to do so, subject to the following
 	conditions:
 
 	The above copyright notice and this permission notice shall be
 	included in all copies or substantial portions of the Software.
 
 	THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 	EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 	OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 	NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 	HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 	WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 	FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 	OTHER DEALINGS IN THE SOFTWARE.
 
 */
 
 package edu.purdue.bbc.io;
 
 import edu.purdue.bbc.util.attributes.Attributes;
 
import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.Writer;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.regex.Pattern;
 
 /**
  * A class for writing a set of tabular data to a file.
  */
 public class CSVTableWriter {
	protected BufferedWriter output;
 	protected String [] keys;
 	protected String delimiter;
 	protected boolean useQuotes;
 	public static final Pattern NUMBER_PATTERN = 
 		Pattern.compile("(\\d+\\.?\\d*|\\.\\d+)([Ee]\\d+)?");
 
 	/**
 	 * Creates a new IdentificationWriter.
 	 * 
 	 * @param output The File to write output to.
 	 * @param keys The keys for the new CSV table.
 	 */
 	public CSVTableWriter( File output, Collection<String> keys ) 
 	                       throws IOException {
 		this( new FileWriter( output ), 
 		      keys.toArray( new String[ keys.size( )]), ",", true );
 	}
 
 	/**
 	 * Creates a new IdentificationWriter.
 	 * 
 	 * @param output The File to write output to.
 	 * @param keys The keys for the new CSV table.
 	 */
 	public CSVTableWriter( File output, String[] keys ) throws IOException {
 		this( new FileWriter( output ), keys, ",", true );
 	}
 
 	/**
 	 * Creates a new IdentificationWriter.
 	 * 
 	 * @param output The File to write output to.
 	 * @param keys The keys for the new CSV table.
 	 */
 	public CSVTableWriter( File output, String[] keys, boolean useQuotes ) 
 	                       throws IOException {
 		this( new FileWriter( output ), keys, ",", useQuotes );
 	}
 	
 	/**
 	 * Creates a new IdentificationWriter.
 	 * 
 	 * @param output The Writer to write output to.
 	 * @param keys The keys for the new CSV table.
 	 */
 	public CSVTableWriter( Writer output, Collection<String> keys ) 
 	                       throws IOException{
 		this( output, keys.toArray( new String[ keys.size( )]), ",", true );
 	}
 
 	/**
 	 * Creates a new IdentificationWriter.
 	 * 
 	 * @param output The Writer to write output to.
 	 * @param keys The keys for the new CSV table.
 	 */
 	public CSVTableWriter( Writer output, String[] keys ) throws IOException {
 		this( output, keys, "," , true);
 	}
 
 	/**
 	 * Creates a new IdentificationWriter.
 	 * 
 	 * @param output The File to write output to.
 	 * @param keys The keys for the new CSV table.
 	 * @param delimiter The delimiter for the values in the table.
 	 */
 	public CSVTableWriter( File output, Collection<String> keys, String delimiter ) 
 			throws IOException {
 		this( new FileWriter( output ), keys, delimiter, true );
 	}
 
 	/**
 	 * Creates a new IdentificationWriter.
 	 * 
 	 * @param output The File to write output to.
 	 * @param keys The keys for the new CSV table.
 	 * @param delimiter The delimiter for the values in the table.
 	 */
 	public CSVTableWriter( File output, String[] keys, String delimiter )
 			throws IOException {
 		this( new FileWriter( output ), keys, delimiter, true );
 	}
 
 	/**
 	 * Creates a new IdentificationWriter.
 	 * 
 	 * @param output The Writer to write output to.
 	 * @param keys The keys for the new CSV table.
 	 * @param delimiter The delimiter for the values in the table.
 	 */
 	public CSVTableWriter( Writer output, Collection<String> keys, 
 	                       String delimiter ) throws IOException {
 		this( output, keys.toArray( new String[ keys.size( )]), delimiter, true );
 	}
 
 	/**
 	 * Creates a new Identification writer with the default headers.
 	 * 
 	 * @param output The Writer to write output to.
 	 * @param keys The keys for the new CSV table.
 	 * @param delimiter The delimiter for the values in the table.
 	 */
 	public CSVTableWriter( Writer output, String[] keys, String delimiter ) 
 			throws IOException {
 		this( output, keys, delimiter, true );
 	}
 
 	/**
 	 * Creates a new IdentificationWriter.
 	 * 
 	 * @param output The Writer to write output to.
 	 * @param keys The keys for the new CSV table.
 	 * @param delimiter The delimiter for the values in the table.
 	 */
 	public CSVTableWriter( Writer output, Collection<String> keys, String delimiter,
 	                       boolean useQuotes ) throws IOException {
 		this( output, keys.toArray( new String[ keys.size( )]), delimiter, useQuotes );
 	}
 
 	/**

 	 * Creates a new Identification writer with the default headers.
 	 * 
 	 * @param output The Writer to write output to.
 	 * @param keys The keys for the new CSV table.
 	 * @param delimiter The delimiter for the values in the table.
 	 */
 	public CSVTableWriter( Writer output, String[] keys, String delimiter, 
 	                       boolean useQuotes ) throws IOException {
 		this.keys = keys;
		this.output = new BufferedWriter( output );
 		this.delimiter = delimiter;
 		this.useQuotes = useQuotes;
 		for ( int i=0; i < this.keys.length; i++ ) {
 			if ( i > 0 )
 				output.write( delimiter );
 			if ( useQuotes && !NUMBER_PATTERN.matcher( keys[ i ] ).matches( )) {
 				output.write( "\"" + keys[ i ] + "\"" );
 			} else {
 				output.write( keys[ i ]);
 			}
 		}
		this.output.newLine( );
 	}
 
 	/**
 	 * Writes a line to the CSV output.
 	 * 
 	 * @param values A Map containing the values to be written.
 	 */
 	public void write( Map<String,? extends Object> values ) throws IOException {
 		for ( int i=0; i < this.keys.length; i++ ) {
 			if ( i > 0 )
 				output.write( delimiter );
 			Object value = values.get( keys[ i ]);
 			if ( value != null ) {
 				if ( useQuotes && !NUMBER_PATTERN.matcher( value.toString( )).matches( )) {
 					output.write( "\"" + value.toString( ) + "\"" );
 				} else {
 					output.write( value.toString( ));
 				}
 			}
 		}
		output.newLine( );
 	}
 
 	/**
 	 * Writes a new line to the CSV output.
 	 * 
 	 * @param values An array of values to be written to the next line.
 	 */
 	public void write( Object[] values ) throws IOException {
 		for ( int i=0; i < this.keys.length; i++ ) {
 			if ( i > 0 )
 				output.write( delimiter );
 			if ( i <= values.length && values[ i ] != null ) {
 				if ( useQuotes && !NUMBER_PATTERN.matcher( values[ i ].toString( )).matches( )) {
 					output.write( "\"" + values[ i ].toString( ) + "\"" );
 				} else {
 					output.write( values[ i ].toString( ));
 				}
 			}
 		}
		output.newLine( );
 	}
 
 	/**
 	 * Writes a new line to the CSV output.
 	 * 
 	 * @param values An Attributes object whose values are to be written out.
 	 */
 	public void write( Attributes<? extends Object> values ) throws IOException {
 		for ( int i=0; i < this.keys.length; i++ ) {
 			if ( i > 0 )
 				output.write( delimiter );
 			Object value = values.getAttribute( keys[ i ]);
 			if ( value != null ) {
 				if ( useQuotes && !NUMBER_PATTERN.matcher( value.toString( )).matches( )) {
 					output.write( "\"" + value.toString( ) + "\"" );
 				} else {
 					output.write( value.toString( ));
 				}
 			}
 		}
		output.newLine( );
 	}
 
 	/**
 	 * Returns the keys being used by this writer. 
 	 * 
 	 * @return The keys for the file being written.
 	 */
 	public String[] getKeys( ) {
 		return this.keys;
 	}
 
 
 	/**
 	 * Closes the output.
 	 */
 	public void close( ) throws IOException {
 		output.close( );
 	}
 	
 }
 
