 /*===========================================================================
   Copyright (C) 2009-2011 by the Okapi Framework contributors
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
 
 package net.sf.okapi.filters.json;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.UnsupportedEncodingException;
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.logging.Logger;
 import java.util.regex.Pattern;
 
 import net.sf.okapi.common.BOMNewlineEncodingDetector;
 import net.sf.okapi.common.Event;
 import net.sf.okapi.common.EventType;
 import net.sf.okapi.common.IParameters;
 import net.sf.okapi.common.UsingParameters;
 import net.sf.okapi.common.Util;
 import net.sf.okapi.common.encoder.EncoderManager;
 import net.sf.okapi.common.exceptions.OkapiIOException;
 import net.sf.okapi.common.exceptions.OkapiUnsupportedEncodingException;
 import net.sf.okapi.common.filters.FilterConfiguration;
 import net.sf.okapi.common.filters.IFilter;
 import net.sf.okapi.common.filters.IFilterConfigurationMapper;
 import net.sf.okapi.common.filterwriter.GenericFilterWriter;
 import net.sf.okapi.common.filterwriter.IFilterWriter;
 import net.sf.okapi.common.LocaleId;
 import net.sf.okapi.common.resource.Ending;
 import net.sf.okapi.common.resource.RawDocument;
 import net.sf.okapi.common.resource.StartDocument;
 import net.sf.okapi.common.resource.TextFragment;
 import net.sf.okapi.common.resource.ITextUnit;
 import net.sf.okapi.common.resource.TextUnit;
 import net.sf.okapi.common.skeleton.GenericSkeleton;
 import net.sf.okapi.common.skeleton.GenericSkeletonWriter;
 import net.sf.okapi.common.skeleton.ISkeletonWriter;
 
 /**
  * Implements the IFilter interface for JSON files.
  */
 @UsingParameters(Parameters.class)
 public class JSONFilter implements IFilter {
 
 	private enum TOKEN {
 		STARTOBJECT,
 		ENDOBJECT,
 		STARTARRAY,
 		ENDARRAY,
 		ENDINPUT,
 		STRING,
 		KEYVALUESEP,
 		SEPARATOR,
 		THING,
 	}
 	
 	private static final String MIMETYPE = "application/json";
 	
 	private final Logger logger = Logger.getLogger(getClass().getName());
 
 	private Parameters params;
 	private String encoding;
 	private LocaleId srcLoc;
 	private boolean hasUTF8BOM;
 	private String lineBreak;
 	private String inputText;
 	private int tuId;
 	private int otherId;
 	private String docName;
 	private LinkedList<Event> queue;
 	private boolean canceled;
 	private int current;
 	private int startRead;
 	private StringBuilder buffer;
 	private int startString;
 	private int endString;
 	private boolean hasNext;
 	private Pattern exceptions;
 	private EncoderManager encoderManager;
 	
 	public JSONFilter () {
 		params = new Parameters();
 	}
 	
 	public void cancel () {
 		canceled = true;
 	}
 
 	public void close () {
 		// Nothing to do
 	}
 
 	public ISkeletonWriter createSkeletonWriter () {
 		return new GenericSkeletonWriter();
 	}
 
 	public IFilterWriter createFilterWriter () {
 		return new GenericFilterWriter(createSkeletonWriter(), getEncoderManager());
 	}
 
 	public List<FilterConfiguration> getConfigurations () {
 		List<FilterConfiguration> list = new ArrayList<FilterConfiguration>();
 		list.add(new FilterConfiguration(getName(),
 			MIMETYPE,
 			getClass().getName(),
 			"JSON (JavaScript Object Notation)",
 			"Configuration for JSON files",
 			null,
 			".json;"));
 		return list;
 	}
 
 	public EncoderManager getEncoderManager () {
 		if ( encoderManager == null ) {
 			encoderManager = new EncoderManager();
 			encoderManager.setMapping(MIMETYPE, "net.sf.okapi.common.encoder.DefaultEncoder");
 		}
 		return encoderManager;
 	}
 
 	public String getDisplayName () {
 		return "JSON Filter";
 	}
 
 	public String getMimeType () {
 		return MIMETYPE;
 	}
 
 	public String getName () {
 		return "okf_json";
 	}
 
 	public IParameters getParameters () {
 		return params;
 	}
 
 	public boolean hasNext () {
 		return hasNext;
 	}
 
 	public Event next () {
 		// Cancel if requested
 		if ( canceled ) {
 			hasNext = false;
 			queue.clear();
 			queue.add(new Event(EventType.CANCELED));
 		}
 		
 		// parse if needed
 		if ( queue.size() == 0 ) {
 			parse();
 		}
 		return queue.poll();
 	}
 
 	public void open (RawDocument input) {
 		open(input, true);
 	}
 
 	public void open (RawDocument input,
 		boolean generateSkeleton)
 	{
 		BOMNewlineEncodingDetector detector = new BOMNewlineEncodingDetector(input.getStream(), input.getEncoding());
 		detector.detectAndRemoveBom();
 		input.setEncoding(detector.getEncoding());
 		encoding = input.getEncoding();
 		
 		BufferedReader reader = null;		
 		try {
 			reader = new BufferedReader(new InputStreamReader(detector.getInputStream(), encoding));
 		}
 		catch ( UnsupportedEncodingException e ) {
 			throw new OkapiUnsupportedEncodingException(
 				String.format("The encoding '%s' is not supported.", encoding), e);
 		}
 		srcLoc = input.getSourceLocale();
 		hasUTF8BOM = detector.hasUtf8Bom();
 		lineBreak = detector.getNewlineType().toString();
 		if ( input.getInputURI() != null ) {
 			docName = input.getInputURI().getPath();
 		}
 		
 		//TODO: Optimize this with a better 'readToEnd()'
 		StringBuilder tmp = new StringBuilder();
 		char[] buf = new char[2048];
 		int count = 0;
 		try {
 			while (( count = reader.read(buf)) != -1 ) {
 				tmp.append(buf, 0, count);
 			}
 		}
 		catch ( IOException e ) {
 			throw new OkapiIOException("Error reading the input.", e);
 		}
 		finally {
 			if ( reader != null ) {
 				try {
 					reader.close();
 				}
 				catch ( IOException e ) {
 					throw new OkapiIOException("Error closing the input.", e);
 				}
 			}
 		}
 		
 		// Set the input string
 		inputText = tmp.toString().replace(lineBreak, "\n");
 		current = 0;
 		tuId = 0;
 		otherId = 0;
 		buffer = new StringBuilder();
 		
 		// Pre-compile exceptions or set them to null
 		if ( Util.isEmpty(params.getExceptions()) ) {
 			exceptions = null;
 		}
 		else {
 			exceptions = Pattern.compile(params.getExceptions());
 		}
 		
 		// Compile code finder rules
 		if ( params.getUseCodeFinder() ) {
 			params.getCodeFinder().compile();
 		}
 		
 		// Set the start event
 		queue = new LinkedList<Event>();
 		StartDocument startDoc = new StartDocument(String.valueOf(++otherId));
 		startDoc.setName(docName);
 		startDoc.setEncoding(encoding, hasUTF8BOM);
 		startDoc.setLocale(srcLoc);
 		startDoc.setLineBreak(lineBreak);
 		startDoc.setFilterParameters(getParameters());
 		startDoc.setFilterWriter(createFilterWriter());
 		startDoc.setType(MIMETYPE);
 		startDoc.setMimeType(MIMETYPE);
 		queue.add(new Event(EventType.START_DOCUMENT, startDoc));
 		hasNext = true;
 	}
 
 	public void setFilterConfigurationMapper (IFilterConfigurationMapper fcMapper) {
 	}
 
 	public void setParameters (IParameters params) {
 		this.params = (Parameters)params;
 	}
 
 	private void parse () {
 		startRead = current;
 		TOKEN token;
 		int state = 0;
 		String key = "";
 		boolean prevWasString = false;
 		
 		while ( (token = readToken()) != TOKEN.ENDINPUT ) {
 			switch ( state ) {
 			case 0: // Start state
 				switch ( token ) {
 				case KEYVALUESEP:
 					key = buffer.toString();
 					prevWasString = false;
 					state = 1;
 					break;
 				case SEPARATOR:
 				case ENDARRAY:
 					if (( prevWasString ) && ( params.getExtractStandalone() )) {
 						if ( processString(null) ) return;
 					}
 					prevWasString = false;
 					break;
 				case STRING:
 					prevWasString = true;
 					break;
 				default:
 					prevWasString = false;
 					break;
 				}
 				break;
 
 			case 1: // After a key
 				state = 0;
 				prevWasString = false;
 				if ( token == TOKEN.STRING ) {
 					if ( processString(key) ) return;
 				}
 				break;
 			}
 		}
 
 		// End of input
 		Ending ending = new Ending(String.valueOf(++otherId));
 		// Skeleton if needed
 		if ( startRead < current ) {
 			GenericSkeleton skel = new GenericSkeleton();
 			if ( endString < current ) {
 				skel.append(inputText.substring(startRead, current+1).replace("\n", lineBreak));
 			}
 			ending.setSkeleton(skel);
 		}
 		queue.add(new Event(EventType.END_DOCUMENT, ending));
 		hasNext = false;
 	}
 	
 	private TOKEN readToken () {
 		while ( true ) {
 			if ( current+1 >= inputText.length() ) {
 				return  TOKEN.ENDINPUT;
 			}
 			
 			switch ( inputText.charAt(++current) ) {
 			case '{':
 				return TOKEN.STARTOBJECT;
 			case '}':
 				return TOKEN.ENDOBJECT;
 			case '[':
 				return TOKEN.STARTARRAY;
 			case ']':
 				return TOKEN.ENDARRAY;
 			case ':':
 				return TOKEN.KEYVALUESEP;
 			case ',':
 				return TOKEN.SEPARATOR;
 			case '"':
 				return readString();
 			default:
 				if ( !Character.isWhitespace(inputText.charAt(current)) ) {
 					return readThing();
 				}
 				// Else do nothing
 				break;
 			}
 		}
 	}
 	
 	private TOKEN readString () {
 		int state = 0;
 		startString = current;
 		buffer.setLength(0);
 
 		while ( true ) {
 			if ( current+1 >= inputText.length() ) {
 				throw new OkapiIOException("Unexpected end of input when parsing.");
 			}
 			
 			switch ( state ) {
 			case 0:
 				switch ( inputText.charAt(++current) ) {
 				case '"': // End of string
 					endString = current;
 					return TOKEN.STRING;
 				case '\\': // Start escape
 					state = 1;
 					break;
 				default:
 					buffer.append(inputText.charAt(current));
 					break;
 				}
 				break;
 
 			case 1: // After '\'
 				state = 0;
 				switch ( inputText.charAt(++current) ) {
 				case 'b':
 				case 'f':
 				case 'n':
 				case 'r':
 				case 't':
 					buffer.append('\\');
 					buffer.append(inputText.charAt(current));
 					break;
 				case 'u':
 					state = 2;
 					break;
 				case '\\':
 				case '/':
 				case '"':
 					buffer.append('\\');
 					buffer.append(inputText.charAt(current));
 					break;
 				default: // Unexpected escape sequence
 					logger.warning(String.format("Unexpected escape sequence '\\%c'.",
 						inputText.charAt(current)));
 					buffer.append('\\');
 					buffer.append(inputText.charAt(current));
 					break;
 				}
 				break;
 
 				case 2: // After bslash-u
 					current++;
 					String tmp = inputText.substring(current, current+4);
 					buffer.append((char)Integer.parseInt(tmp, 16));
 					current += 3;
 					state = 0;
 					break;
 			}
 		}
 	}
 
 	private TOKEN readThing () {
 		buffer.setLength(0);
 		while ( true ) {
 			if ( current+1 >= inputText.length() ) {
 				throw new OkapiIOException("Unexpected end of input when parsing.");
 			}
 			
 			switch ( inputText.charAt(++current) ) {
 			case '}':
 			case ']':
 				current--; // For next readToken()
 				return TOKEN.THING;
 			default:
 				if ( Character.isWhitespace(inputText.charAt(current)) ) {
 					// No need for current--;
 					return TOKEN.THING;
 				}
 				// Else: keep adding to this token
 				buffer.append(inputText.charAt(current));
 				break;
 			}
 		}
 	}
 
 	private boolean processString (String resName) {
 		// Process key-specific case
 		if ( !Util.isEmpty(resName) ) {
 			// Treat options for key+value pairs
 			boolean extract = params.getExtractAllPairs();
 			if ( exceptions != null ) {
 				if ( exceptions.matcher(resName).find() ) {
 					// It's an exception, so we reverse the extraction flag
 					extract = !extract;
 				}
 			}
 			if ( !extract ) { // Not to extract
 				return false;
 			}
 		}
 		// Else: This is a stand-alone string, we have already tested if
 		// if was to be extracted.
 		
 		// Create the new text unit
 		ITextUnit tu = new TextUnit(String.valueOf(++tuId));
 		
 		// Create the text and process its inline codes if requested
 		TextFragment tf = new TextFragment(buffer.toString());
 		if ( params.getUseCodeFinder() ) {
 			params.getCodeFinder().process(tf);
 		}
 		tu.setSourceContent(tf);
 		
 		// Sets the name if available
 		if ( !Util.isEmpty(resName) ) {
 			tu.setName(resName);
 		}
 
 		// Compute and set the skeleton
 		GenericSkeleton skel = new GenericSkeleton();
 		skel.append(inputText.substring(startRead, startString+1).replace("\n", lineBreak));
 		skel.addContentPlaceholder(tu);
 		if ( endString < current ) {
			skel.append(inputText.substring(endString, current).replace("\n", lineBreak));
 		}
 		tu.setSkeleton(skel);
 		tu.setMimeType(MIMETYPE);
 		
 		queue.add(new Event(EventType.TEXT_UNIT, tu));
 		return true;
 	}
 
 }
