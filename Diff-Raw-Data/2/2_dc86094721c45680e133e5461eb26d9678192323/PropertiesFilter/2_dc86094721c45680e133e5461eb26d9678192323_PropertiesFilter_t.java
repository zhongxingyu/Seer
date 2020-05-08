 /*===========================================================================
   Copyright (C) 2008-2009 by the Okapi Framework contributors
 -----------------------------------------------------------------------------
   This library is free software; you can redistribute it and/or modify it 
   under the terms of the GNU Lesser General Public License as published by 
   the Free Software Foundation; either version 2.1 of the License, or (at 
   your option) any later version.
 
   This library is distributed in the hope that it will be useful, but 
   WITHOUT ANY WARRANTY; without even the implied warranty of 
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser 
   General Public License for more details.
 
   You should have received a copy of the GNU Lesser General Public License 
   along with this library; if not, write to the Free Software Foundation, 
   Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 
   See also the full LGPL text here: http://www.gnu.org/copyleft/lesser.html
 ===========================================================================*/
 
 package net.sf.okapi.filters.properties;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.UnsupportedEncodingException;
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import java.util.regex.Pattern;
 
 import net.sf.okapi.common.BOMNewlineEncodingDetector;
 import net.sf.okapi.common.Event;
 import net.sf.okapi.common.EventType;
 import net.sf.okapi.common.IParameters;
 import net.sf.okapi.common.MimeTypeMapper;
 import net.sf.okapi.common.Util;
 import net.sf.okapi.common.exceptions.OkapiIOException;
 import net.sf.okapi.common.exceptions.OkapiUnsupportedEncodingException;
 import net.sf.okapi.common.filters.FilterConfiguration;
 import net.sf.okapi.common.filters.IFilter;
 import net.sf.okapi.common.filterwriter.GenericFilterWriter;
 import net.sf.okapi.common.filterwriter.IFilterWriter;
 import net.sf.okapi.common.resource.Ending;
 import net.sf.okapi.common.resource.RawDocument;
 import net.sf.okapi.common.resource.Property;
 import net.sf.okapi.common.resource.StartDocument;
 import net.sf.okapi.common.resource.TextUnit;
 import net.sf.okapi.common.skeleton.GenericSkeleton;
 import net.sf.okapi.common.skeleton.GenericSkeletonWriter;
 import net.sf.okapi.common.skeleton.ISkeletonWriter;
 
 /**
  * Implements the IFilter interface for properties files.
  */
 public class PropertiesFilter implements IFilter {
 
 	private static final int RESULT_END     = 0;
 	private static final int RESULT_ITEM    = 1;
 	private static final int RESULT_DATA    = 2;
 
 	private final Logger logger = Logger.getLogger(getClass().getName());
 
 	private Parameters params;
 	private BufferedReader reader;
 	private boolean canceled;
 	private String encoding;
 	private TextUnit tuRes;
 	private LinkedList<Event> queue;
 	private String textLine;
 	private long lineNumber;
 	private long lineSince;
 	private long position;
 	private int tuId;
 	private int otherId;
 	private Pattern keyConditionPattern;
 	private String lineBreak;
 	private int parseState = 0;
 	private GenericSkeleton skel;
 	private String docName;
 	private String srcLang;
 	private boolean hasUTF8BOM;
 	
 	public PropertiesFilter () {
 		params = new Parameters();
 	}
 	
 	public void cancel () {
 		canceled = true;
 	}
 
 	public void close () {
 		try {
 			if ( reader != null ) {
 				reader.close();
 				reader = null;
 				docName = null;
 			}
 			parseState = 0;
 		}
 		catch ( IOException e) {
 			throw new OkapiIOException(e);
 		}
 	}
 
 	public String getName () {
 		return "okf_properties";
 	}
 	
 	public String getMimeType () {
 		return MimeTypeMapper.PROPERTIES_MIME_TYPE;
 	}
 
 	public List<FilterConfiguration> getConfigurations () {
 		List<FilterConfiguration> list = new ArrayList<FilterConfiguration>();
 		list.add(new FilterConfiguration(getName(),
 			MimeTypeMapper.PROPERTIES_MIME_TYPE,
 			getClass().getName(),
 			"Java Properties",
 			"Java properties files (Output used \\uHHHH escapes)"));
		list.add(new FilterConfiguration(getName()+"-outputNotEscaped",
 			MimeTypeMapper.PROPERTIES_MIME_TYPE,
 			getClass().getName(),
 			"Java Properties (Output not escaped)",
 			"Java properties files (Characters in the output encoding are not escaped)",
 			"outputNotEscaped.fprm"));
 		return list;
 	}
 
 	public IParameters getParameters () {
 		return params;
 	}
 
 	public boolean hasNext () {
 		return (parseState > 0);
 	}
 
 	public Event next () {
 		// Cancel if requested
 		if ( canceled ) {
 			parseState = 0;
 			queue.clear();
 			queue.add(new Event(EventType.CANCELED));
 		}
 		
 		// Process queue if it's not empty yet
 		if ( queue.size() > 0 ) {
 			return queue.poll();
 		}
 		
 		// Continue the parsing
 		int n;
 		boolean resetBuffer = true;
 		do {
 			switch ( n = readItem(resetBuffer) ) {
 			case RESULT_DATA:
 				// Don't send the skeleton chunk now, wait for the complete one
 				resetBuffer = false;
 				break;
 			case RESULT_ITEM:
 				// It's a text-unit, the skeleton is already set
 				return new Event(EventType.TEXT_UNIT, tuRes);
 			default:
 				resetBuffer = true;
 				break;
 			}
 		} while ( n > RESULT_END );
 		
 		// Set the ending call
 		Ending ending = new Ending(String.valueOf(++otherId));
 		ending.setSkeleton(skel);
 		parseState = 0;
 		return new Event(EventType.END_DOCUMENT, ending);
 	}
 
 	public void open (RawDocument input) {
 		open(input, true);
 	}
 	
 	public void setParameters (IParameters params) {
 		this.params = (Parameters)params;
 	}
 
 	public ISkeletonWriter createSkeletonWriter() {
 		return new GenericSkeletonWriter();
 	}
 
 	public IFilterWriter createFilterWriter () {
 		return new GenericFilterWriter(createSkeletonWriter());
 	}
 
 	public void open (RawDocument input,
 		boolean generateSkeleton)
 	{
 		close();
 		parseState = 1;
 		canceled = false;
 
 		// Open the input reader from the provided reader
 		BOMNewlineEncodingDetector detector = new BOMNewlineEncodingDetector(input.getStream(), input.getEncoding());
 		detector.detectAndRemoveBom();
 		input.setEncoding(detector.getEncoding());
 		encoding = input.getEncoding();
 		
 		try {
 			reader = new BufferedReader(new InputStreamReader(detector.getInputStream(), encoding));
 		}
 		catch ( UnsupportedEncodingException e ) {
 			throw new OkapiUnsupportedEncodingException(
 				String.format("The encoding '%s' is not supported.", encoding), e);
 		}
 		srcLang = input.getSourceLanguage();
 		hasUTF8BOM = detector.hasUtf8Bom();
 		lineBreak = detector.getNewlineType().toString();
 		if ( input.getInputURI() != null ) {
 			docName = input.getInputURI().getPath();
 		}
 		
 		// Initializes the variables
 		tuId = 0;
 		otherId = 0;
 		lineNumber = 0;
 		lineSince = 0;
 		position = 0;
 		// Compile conditions
 		if ( params.useKeyCondition ) {
 			keyConditionPattern = Pattern.compile(params.keyCondition); 
 		}
 		else {
 			keyConditionPattern = null;
 		}
 		// Compile code finder rules
 		if ( params.useCodeFinder ) {
 			params.codeFinder.compile();
 		}
 		// Set the start event
 		queue = new LinkedList<Event>();
 
 		StartDocument startDoc = new StartDocument(String.valueOf(++otherId));
 		startDoc.setName(docName);
 		startDoc.setEncoding(encoding, hasUTF8BOM);
 		startDoc.setLanguage(srcLang);
 		startDoc.setFilterParameters(params);
 		startDoc.setFilterWriter(createFilterWriter());
 		startDoc.setLineBreak(lineBreak);
 		startDoc.setType("text/x-properties");
 		startDoc.setMimeType("text/x-properties");
 		queue.add(new Event(EventType.START_DOCUMENT, startDoc));
 	}
 
 	private int readItem (boolean resetBuffer) {
 		try {
 			if ( resetBuffer ) {
 				skel = new GenericSkeleton();
 			}
 			
 			StringBuilder keyBuffer = new StringBuilder();
 			StringBuilder textBuffer = new StringBuilder();
 			String value = "";
 			String key = "";
 			String note = "";
 			boolean isMultiline = false;
 			int startText = 0;
 			long lS = -1;
 
 			while ( true ) {
 				if ( !getNextLine() ) {
 					return RESULT_END;
 				}
 				// Else: process the line
 
 				// Remove any leading white-spaces
 				String tmp = Util.trimStart(textLine, "\t\r\n \f");
 
 				if ( isMultiline ) {
 					value += tmp;
 				}
 				else {
 					// Empty lines
 					if ( tmp.length() == 0 ) {
 						skel.append(textLine);
 						skel.append(lineBreak);
 						continue;
 					}
 
 					// Comments
 					boolean isComment = (( tmp.charAt(0) == '#' ) || ( tmp.charAt(0) == '!' ));
 					if ( isComment ) tmp = tmp.substring(1);
 					if ( params.extraComments && !isComment ) {
 						if ( tmp.charAt(0) == ';' ) {
 							isComment = true; // .NET-style
 							tmp = tmp.substring(1);
 						}
 						else if ( tmp.startsWith("//") ) {
 							isComment = true; // C++/Java-style,
 							tmp = tmp.substring(2);
 						}
 					}
 
 					if ( isComment ) {
 						params.locDir.process(tmp);
 						skel.append(textLine);
 						skel.append(lineBreak);
 						if ( params.commentsAreNotes ) {
 							if ( note.length() > 0 ) note += "\n";
 							note += tmp;
 						}
 						continue;
 					}
 
 					// Get the key
 					boolean bEscape = false;
 					int n = 0;
 					for ( int i=0; i<tmp.length(); i++ ) {
 						if ( bEscape ) bEscape = false;
 						else {
 							if ( tmp.charAt(i) == '\\' ) {
 								bEscape = true;
 								continue;
 							}
 							if (( tmp.charAt(i) == ':' ) || ( tmp.charAt(i) == '=' )
 								|| ( Character.isWhitespace(tmp.charAt(i)) )) {
 								// That the first white-space after the key
 								n = i;
 								break;
 							}
 						}
 					}
 
 					// Get the key
 					if ( n == 0 ) {
 						// Line empty after the key
 						n = tmp.length();
 					}
 					key = tmp.substring(0, n);
 
 					// Gets the value
 					boolean bEmpty = true;
 					boolean bCheckEqual = true;
 					for ( int i=n; i<tmp.length(); i++ ) {
 						if ( bCheckEqual && (( tmp.charAt(i) == ':' )
 							|| ( tmp.charAt(i) == '=' ))) {
 							bCheckEqual = false;
 							continue;
 						}
 						if ( !Character.isWhitespace(tmp.charAt(i)) ) {
 							// That the first white-space after the key
 							n = i;
 							bEmpty = false;
 							break;
 						}
 					}
 
 					if ( bEmpty ) n = tmp.length();
 					value = tmp.substring(n);
 					// Real text start point (adjusted for trimmed characters)
 					startText = n + (textLine.length() - tmp.length());
 					// Use m_nLineSince-1 to not count the current one
 					lS = (position-(textLine.length()+(lineSince-1))) + startText;
 					lineSince = 0; // Reset the line counter for next time
 				}
 
 				// Is it a multi-lines entry?
 				if ( value.endsWith("\\") ) {
 					// Make sure we have an odd number of ending '\'
 					int n = 0;
 					for ( int i=value.length()-1;
 						(( i > -1 ) && ( value.charAt(i) == '\\' ));
 						i-- ) n++;
 
 					if ( (n % 2) != 0 ) { // Continue onto the next line
 						value = value.substring(0, value.length()-1);
 						isMultiline = true;
 						// Preserve parsed text in case we do not extract
 						if ( keyBuffer.length() == 0 ) {
 							keyBuffer.append(textLine.substring(0, startText));
 							startText = 0; // Next time we get the whole line
 						}
 						textBuffer.append(textLine.substring(startText));
 						continue; // Read next line
 					}
 				}
 
 				// Check for key condition
 				// Directives overwrite the key condition
 				boolean extract = true;
 				if ( params.locDir.isWithinScope() ) {
 					extract = params.locDir.isLocalizable(true);
 				}
 				else { // Check for key condition
 					if (  keyConditionPattern != null ) {
 						if ( params.extractOnlyMatchingKey ) {
 							if ( !keyConditionPattern.matcher(key).matches() )
 								extract = false;
 						}
 						else { // Extract all but items with matching keys
 							if ( keyConditionPattern.matcher(key).matches() )
 								extract = false;
 						}
 					}
 					else { // Outside directive scope: check if we extract text outside
 						extract = params.locDir.localizeOutside();
 					}
 				}
 
 				if ( extract ) {
 					tuRes = new TextUnit(String.valueOf(++tuId),
 						unescape(value));
 					tuRes.setName(key);
 					tuRes.setMimeType("text/x-properties");
 					tuRes.setPreserveWhitespaces(true);
 					if ( note.length() > 0 ) {
 						tuRes.setProperty(new Property(Property.NOTE, note, true));
 					}
 				}
 
 				if ( extract ) {
 					// Parts before the text
 					if ( keyBuffer.length() == 0 ) {
 						// Single-line case
 						keyBuffer.append(textLine.substring(0, startText));
 					}
 					skel.append(keyBuffer.toString());
 					skel.addContentPlaceholder(tuRes, null);
 					// Line-break
 					skel.append(lineBreak);
 				}
 				else {
 					skel.append(keyBuffer.toString());
 					skel.append(textBuffer.toString());
 					skel.append(textLine);
 					skel.append(lineBreak);
 					return RESULT_DATA;
 				}
 
 				if ( params.useCodeFinder )
 					params.codeFinder.process(tuRes.getSourceContent());
 				
 				tuRes.setSkeleton(skel);
 				tuRes.setSourceProperty(new Property("start", String.valueOf(lS), true));
 				return RESULT_ITEM;
 			}
 		}
 		catch ( IOException e ) {
 			throw new OkapiIOException(e);
 		}
 	}
 
 	/**
 	 * Gets the next line of the string or file input.
 	 * @return True if there was a line to read,
 	 * false if this is the end of the input.
 	 */
 	private boolean getNextLine ()
 		throws IOException
 	{
 		while ( true ) {
 			textLine = reader.readLine();
 			if ( textLine != null ) {
 				lineNumber++;
 				lineSince++;
 				// We count char instead of byte, while the BaseStream.Length is in byte
 				// Not perfect, but better than nothing.
 				position += textLine.length() + lineBreak.length(); // +n For the line-break
 			}
 			return (textLine != null);
 		}
 	}
 
 	/**
 	 * Un-escapes slash-u+HHHH characters in a string.
 	 * @param text The string to convert.
 	 * @return The converted string.
 	 */
 	//TODO: Deal with escape ctrl, etc. \n should be converted
 	private String unescape (String text) {
 		if ( text.indexOf('\\') == -1 ) return text;
 		StringBuilder tmpText = new StringBuilder();
 		for ( int i=0; i<text.length(); i++ ) {
 			if ( text.charAt(i) == '\\' ) {
 				switch ( text.charAt(i+1) ) {
 				case 'u':
 					if ( i+5 < text.length() ) {
 						try {
 							int nTmp = Integer.parseInt(text.substring(i+2, i+6), 16);
 							tmpText.append((char)nTmp);
 						}
 						catch ( Exception e ) {
 							logMessage(Level.WARNING,
 								String.format(Res.getString("INVALID_UESCAPE"),
 								text.substring(i+2, i+6)));
 						}
 						i += 5;
 						continue;
 					}
 					else {
 						logMessage(Level.WARNING,
 							String.format(Res.getString("INVALID_UESCAPE"),
 							text.substring(i+2)));
 					}
 					break;
 				case '\\':
 					tmpText.append("\\\\");
 					i++;
 					continue;
 				case 'n':
 					tmpText.append("\n");
 					i++;
 					continue;
 				case 't':
 					tmpText.append("\t");
 					i++;
 					continue;
 				}
 			}
 			else tmpText.append(text.charAt(i));
 		}
 		return tmpText.toString();
 	}
 	
 	private void logMessage (Level level,
 		String text)
 	{
 		logger.log(level, String.format(Res.getString("LINE_LOCATION"), lineNumber) + text);
 	}
 
 }
