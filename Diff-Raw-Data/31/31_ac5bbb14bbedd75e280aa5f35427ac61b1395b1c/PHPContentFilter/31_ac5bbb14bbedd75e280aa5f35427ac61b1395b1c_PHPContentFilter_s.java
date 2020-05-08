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
 
 package net.sf.okapi.filters.php;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.UnsupportedEncodingException;
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.List;
 
 import net.sf.okapi.common.BOMNewlineEncodingDetector;
 import net.sf.okapi.common.Event;
 import net.sf.okapi.common.EventType;
 import net.sf.okapi.common.HTMLCharacterEntities;
 import net.sf.okapi.common.IParameters;
 import net.sf.okapi.common.MimeTypeMapper;
 import net.sf.okapi.common.UsingParameters;
 import net.sf.okapi.common.Util;
 import net.sf.okapi.common.encoder.EncoderManager;
 import net.sf.okapi.common.exceptions.OkapiIOException;
 import net.sf.okapi.common.exceptions.OkapiIllegalFilterOperationException;
 import net.sf.okapi.common.exceptions.OkapiUnsupportedEncodingException;
 import net.sf.okapi.common.filters.FilterConfiguration;
 import net.sf.okapi.common.filters.IFilter;
 import net.sf.okapi.common.filters.IFilterConfigurationMapper;
 import net.sf.okapi.common.filters.LocalizationDirectives;
 import net.sf.okapi.common.filterwriter.GenericFilterWriter;
 import net.sf.okapi.common.filterwriter.IFilterWriter;
 import net.sf.okapi.common.resource.Ending;
 import net.sf.okapi.common.resource.ITextUnit;
 import net.sf.okapi.common.resource.Property;
 import net.sf.okapi.common.resource.RawDocument;
 import net.sf.okapi.common.resource.StartDocument;
 import net.sf.okapi.common.resource.TextFragment;
 import net.sf.okapi.common.resource.TextUnit;
 import net.sf.okapi.common.resource.TextFragment.TagType;
 import net.sf.okapi.common.skeleton.GenericSkeleton;
 import net.sf.okapi.common.skeleton.GenericSkeletonWriter;
 import net.sf.okapi.common.skeleton.ISkeletonWriter;
 
 /**
  * Implements the IFilter interface for PHP content. This filter is
  * expected to be called from a parent filter that processed the container.
  */
 @UsingParameters(Parameters.class)
 public class PHPContentFilter implements IFilter {
 
 	private static final int STRTYPE_SINGLEQUOTED = 0;
 	private static final int STRTYPE_DOUBLEQUOTED = 1;
 	private static final int STRTYPE_HEREDOC = 2;
 	private static final int STRTYPE_NOWDOC = 3;
 	private static final int STRTYPE_MIXED = 4;
 	
 	private Parameters params;
 	private String lineBreak;
 	private String inputText;
 	private int tuId;
 	private LinkedList<Event> queue;
 	private boolean hasNext;
 	private int current;
 	private int firstSkelStart;
 	private int skelStart;
 	private int stringStart;
 	private int stringEnd;
 	private TextFragment srcFrag;
 	private ITextUnit textUnit;
 	private GenericSkeleton srcSkel;
 	private int resType;
 	private HTMLCharacterEntities cerList;
 	private EncoderManager encoderManager;
 	private LocalizationDirectives locDir;
 	
 	public PHPContentFilter () {
 		params = new Parameters();
 		cerList = new HTMLCharacterEntities();
 		locDir = new LocalizationDirectives();
 	}
 	
 	public void cancel () {
 		// TODO: Support cancel
 	}
 
 	public void close () {
 		// Nothing to do
 		hasNext = false;
 	}
 
 	public ISkeletonWriter createSkeletonWriter() {
 		return new GenericSkeletonWriter();
 	}
 
 	public IFilterWriter createFilterWriter () {
 		return new GenericFilterWriter(createSkeletonWriter(), getEncoderManager());
 	}
 
 	public List<FilterConfiguration> getConfigurations () {
 		List<FilterConfiguration> list = new ArrayList<FilterConfiguration>();
 		list.add(new FilterConfiguration(getName(),
 			MimeTypeMapper.PHP_MIME_TYPE,
 			getClass().getName(),
 			"PHP Content Default",
 			"Default PHP Content configuration.",
 			null,
 			".php;"));
 		return list;
 	}
 
 	public EncoderManager getEncoderManager () {
 		if ( encoderManager == null ) {
 			encoderManager = new EncoderManager();
 			encoderManager.setMapping(MimeTypeMapper.PHP_MIME_TYPE, "net.sf.okapi.common.encoder.PHPContentEncoder");
 		}
 		return encoderManager;
 	}
 	
 	public String getDisplayName () {
 		return "PHP Content Filter";
 	}
 
 	public String getMimeType () {
 		return MimeTypeMapper.PHP_MIME_TYPE;
 	}
 
 	public String getName () {
 		return "okf_phpcontent";
 	}
 
 	public IParameters getParameters () {
 		return params;
 	}
 
 	public boolean hasNext () {
 		return hasNext;
 	}
 
 	public Event next () {
 		if ( !hasNext ) return null;
 		if ( queue.size() == 0 ) {
 			parse();
 		}
 		Event event = queue.poll();
 		if ( event.getEventType() == EventType.END_DOCUMENT ) {
 			hasNext = false;
 		}
 		return event;
 	}
 
 	public void open (RawDocument input) {
 		open(input, true);
 	}
 
 	public void open (RawDocument input,
 		boolean generateSkeleton)
 	{
 		close(); // Just in case resources need to be freed
 		
 		locDir.reset();
 		locDir.setOptions(params.getUseDirectives(), params.getExtractOutsideDirectives());
 
 		BOMNewlineEncodingDetector detector = new BOMNewlineEncodingDetector(input.getStream(), input.getEncoding());
 		detector.detectAndRemoveBom();
 		input.setEncoding(detector.getEncoding());
 		String encoding = input.getEncoding();
 		
 		BufferedReader reader = null;		
 		try {
 			reader = new BufferedReader(new InputStreamReader(detector.getInputStream(), encoding));
 		}
 		catch ( UnsupportedEncodingException e ) {
 			throw new OkapiUnsupportedEncodingException(
 				String.format("The encoding '%s' is not supported.", encoding), e);
 		}
 		lineBreak = detector.getNewlineType().toString();
 		boolean hasUTF8BOM = detector.hasUtf8Bom();
 		String docName = null;
 		if ( input.getInputURI() != null ) {
 			docName = input.getInputURI().getPath();
 		}
 		
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
 		current = -1;
 		tuId = 0;
 		// Compile code finder rules
 		if ( params.getUseCodeFinder() ) {
 			params.getCodeFinder().compile();
 		}
 
 		// Set the start event
 		queue = new LinkedList<Event>();
 		StartDocument startDoc = new StartDocument("sd");
 		startDoc.setName(docName);
 		startDoc.setEncoding(encoding, hasUTF8BOM);
 		startDoc.setLocale(input.getSourceLocale());
 		startDoc.setLineBreak(lineBreak);
 		startDoc.setFilterParameters(getParameters());
 		startDoc.setFilterWriter(createFilterWriter());
 		startDoc.setType(MimeTypeMapper.PHP_MIME_TYPE);
 		startDoc.setMimeType(MimeTypeMapper.PHP_MIME_TYPE);
 		startDoc.setMultilingual(false);
 		queue.add(new Event(EventType.START_DOCUMENT, startDoc));
 		hasNext = true;
 	}
 
 	@Override
 	public void setFilterConfigurationMapper (IFilterConfigurationMapper fcMapper) {
 	}
 
 	@Override
 	public void setParameters (IParameters params) {
 		this.params = (Parameters)params;
 	}
 
 	private void parse () {
 		int prevState = 0;
 		int state = 0;
 		StringBuilder buf = null;
 		StringBuilder possibleEndKey = null;
 		String endKey = null;
 		int blockType = STRTYPE_HEREDOC;
 		char ch = 0x0;
 		char tch = 0x0;
 		
 		resetStorage();
 		if ( current < 0 ) skelStart = 0;
 		else skelStart = current;
 		firstSkelStart = skelStart;
 		stringEnd = -1;
 		
 		while ( true ) {
 			if ( current+1 >= inputText.length() ) {
 				processTextUnit();
 				// End of input
 				Ending ending = new Ending("ed");
 				if ( skelStart < inputText.length() ) {
 					GenericSkeleton skl = new GenericSkeleton(inputText.substring(skelStart).replace("\n", lineBreak));
 					ending.setSkeleton(skl);
 				}
 				queue.add(new Event(EventType.END_DOCUMENT, ending));
 				return;
 			}
 			
 			if ( state == 0 ) {
 				// In state 0: check for array bracket
 				if ( !Character.isWhitespace(ch) ) tch = ch;
 			}
 			ch = inputText.charAt(++current);
 			
 			switch ( state ) {
 			case 0:
 				switch ( ch ) {
 				case '/':
 					prevState = state;
 					state = 1; // After '/'
 					continue;
 				case '\\': // Escape prefix
 					prevState = state;
 					state = 4;
 					continue;
 				case '<': // Test for heredoc/nowdoc
 					if ( inputText.length() > current+2) {
 						if (( inputText.charAt(current+1) == '<' ) 
 							&& ( inputText.charAt(current+1) == '<' )) {
 							// Gets the keyword
 							current+=2;
 							buf = new StringBuilder();
 							state = 6;
 							continue;
 						}
 						// Else: fall thru
 					}
 					// Else: not a heredoc/nowdoc
 					continue;
 				case '\'': // Single-quoted string
 					prevState = state;
 					state = 9;
 					stringStart = current;
 					buf = new StringBuilder();
 					continue;
 				case '"': // Double-quoted string
 					prevState = state;
 					state = 10;
 					stringStart = current;
 					buf = new StringBuilder();
 					continue;
 				case ';': // End of statement
 				case ',': // End of argument
 				case '=': // Assignment
 					if ( processTextUnit() ) return;
 					continue;
 				}
 				continue;
 				
 			case 1: // After initial '/'
 				if ( ch == '/' ) {
 					state = 2; // Single-line comment: Wait for EOL/EOS
 					buf = new StringBuilder();
 					continue;
 				}
 				if ( ch == '*' ) {
 					state = 3; // Comment: wait for slash+star
 					buf = new StringBuilder();
 					continue;
 				}
 				// Else: Was a normal '/'
 				state = prevState;
 				current--;
 				continue;
 				
 			case 2: // In single-line comment, wait for EOL/EOS
 				if ( ch == '\n' ) {
 					// Process the comment for directives
 					locDir.process(buf.toString());
 					// And go back to previous state
 					state = prevState;
 					continue;
 				}
 				// Else: Store the comment
 				buf.append(ch);
 				continue;
 				
 			case 3: // In multi-line comment, wait for star+slash
 				if ( ch == '*' ) {
 					// Check for next character
 					state = 5;
 					continue;
 				}
 				// Else: Store the comment
 				buf.append(ch);
 				continue;
 				
 			case 4: // After backslash for escape
 				state = prevState;
 				continue;
 				
 			case 5: // After '*', expect slash (from multi-line comment)
 				if ( ch == '/' ) {
 					// Process the comment for directives
 					locDir.process(buf.toString());
 					// And go back to previous state
 					state = prevState;
 					continue;
 				}
 				// Else: 
 				state = 3; // Go back to comment
 				buf.append('*'); // Store the trigger
 				current--;
 				continue;
 				
 			case 6: // After <<<, getting the heredoc key
 				if ( Character.isWhitespace(ch) ) {
 					// End of key
 					if ( buf.toString().startsWith("'") ) {
 						blockType = STRTYPE_NOWDOC;
 						endKey  = Util.trimEnd(Util.trimStart(buf.toString(), "'"), "'");
 					}
 					else if ( buf.toString().startsWith("\"") ) {
 						blockType = STRTYPE_HEREDOC;
 						endKey  = Util.trimEnd(Util.trimStart(buf.toString(), "\""), "\"");
 					}
 					else {
 						blockType = STRTYPE_HEREDOC;
 						endKey = buf.toString();
 					}
 					// Change state to wait for the end of heredoc/nowdoc
 					state = 7;
 					stringStart = current;
 					buf = new StringBuilder();
 					continue;
 				}
 				else {
 					buf.append(ch);
 				}
 				continue;
 				
 			case 7: // Inside a heredoc/nowdoc entry, wait for linebreak
 				if ( ch == '\n' ) {
 					stringEnd = current;
 					possibleEndKey = new StringBuilder();
 					state = 8;
 				}
 				else {
 					buf.append(ch);
 				}
 				continue;
 				
 			case 8: // Parsing the end-key for the heredoc/nowdoc entry
 				switch ( ch ) {
 				case '\n':
 					if ( possibleEndKey.length() > 0 ) { // End of key
 						addString(buf, blockType, tch);
 						state = prevState;
 						continue;
 					}
 					// Else: Sequential line-breaks
 					buf.append("\n"); // Append the previous
 					stringEnd = current; // Reset possible ending point
 					// and stay in this state
 					break;
 				case ';':
 					if ( possibleEndKey.length() > 0 ) { // End of key
 						addString(buf, blockType, tch);
 						state = prevState;
 						continue;
 					}
 					// Else: fall thru
 				default:
 					possibleEndKey.append(ch);
 					if ( !endKey.startsWith(possibleEndKey.toString()) ) {
 						// Not the end key, just part of the text
 						state = 7; // back to inside heredoc entry
 						// Don't forget the initial linebreak of case 7
 						buf.append("\n"+possibleEndKey);
 					}
 				}
 				continue;
 				
 			case 9: // Inside a single-quoted string, wait for closing single quote
 				switch ( ch ) {
 				case '\'':
 					// End of string
 					stringEnd = current;
 					addString(buf, STRTYPE_SINGLEQUOTED, tch);
 					state = prevState;
 					continue;
 				case '\\':
 					if ( inputText.length() > current+1 ) {
 						buf.append('\\');
 						buf.append(inputText.charAt(++current));
 					}
 					else {
 						throw new OkapiIllegalFilterOperationException("Unexpected end.");
 					}
 					continue;
 				case '&':
 					int ucode = getEntity();
 					if ( ucode != -1 ) {
 						buf.append((char)ucode);
 						continue;
 					}
 					// Else: just fall thru to add '&'
 				default:
 					buf.append(ch);
 				}
 				continue;
 
 			case 10: // Inside a double-quoted string, wait for closing double quote
 				if ( ch == '"' ) {
 					// End of string
 					stringEnd = current;
 					addString(buf, STRTYPE_DOUBLEQUOTED, tch);
 					state = prevState;
 					continue;
 				}
 				else if ( ch == '\\' ) {
 					if ( inputText.length() > current+1 ) {
 						buf.append('\\');
 						buf.append(inputText.charAt(++current));
 					}
 					else {
 						throw new OkapiIllegalFilterOperationException("Unexpected end.");
 					}
 				}
 				else {
 					buf.append(ch);
 				}
 				continue;
 			}
 		}
 	}
 
 	// Assumes current points to '&'
 	// Returns -1 if not found, char-value if found and adjust the current position.
 	private int getEntity () {
 		int n = inputText.indexOf(';', current);
 		if ( n == -1 ) return -1;
 		String tmp = inputText.substring(current+1, n);
 		if ( tmp.length() < 1 ) return -1; // Just "&;"
 		int res;
 		if ( tmp.charAt(0) == '#' ) {
 			if ( tmp.length() < 2 ) return -1; // "&#;"
 			try {
 				if ( tmp.charAt(1) == 'x' ) {
 					// Hexadecimal NCR, assume "&#xh..;"
 					res = Integer.parseInt(tmp.substring(2), 16);
 				}
 				else {
 					// Decimal NCR, assume "&#d...;"
 					res = Integer.parseInt(tmp.substring(1));
 				}
 			}
 			catch ( NumberFormatException e ) {
 				return -1; // Syntax error
 			}
 		}
 		else if ( Character.isWhitespace(tmp.charAt(0)) ) {
 			// Case of "& " (to go faster)
 			return -1;
 		}
 		else { // Maybe a real character entity reference
 			cerList.ensureInitialization(false);
 			res = cerList.lookupName(tmp);
 			if ( res == -1 ) return -1; // name not found
 		}
 		current = n; // Update the current position
 		return res;
 	}
 	
 	private void addString (StringBuilder buffer,
 		int type,
 		char lastTokenChar)
 	{
 		if ( lastTokenChar == '[') return; // It's an index
 		if ( buffer.length() == 0 ) return; // Empty string
 
 		if ( stringStart > skelStart ) { // Do we have pre-string codes?
 			if ( srcFrag.isEmpty() ) {
 				// If no text yet: codes go to skeleton
 				srcSkel.add(inputText.substring(skelStart, stringStart+1).replace("\n", lineBreak));
 			}
 			else { // Otherwise they are inline codes
 				srcFrag.append(TagType.PLACEHOLDER, "code",
 					inputText.substring(skelStart, stringStart+1).replace("\n", lineBreak));
 			}
 		}
 		
 		// Set the text and process it for inline codes
		TextFragment tf = new TextFragment(buffer.toString());
 		if ( params.getUseCodeFinder() ) {
 			params.getCodeFinder().process(tf);
 		}
 		
 		// Do we still have text in the fragment? (and started the source?) 
 		if ( !tf.hasText(true) && srcFrag.isEmpty() ) {
 			// If no text yet: string-with-codes-only goes to skeleton
 			srcSkel.add(inputText.substring(stringStart, stringEnd+1).replace("\n", lineBreak));
 		}
 	
 		// Otherwise it's either text or inline codes or both
 		srcFrag.append(tf);
 		// Compute the type
 		if ( resType == -1 ) resType = type;
 		else {
 			if ( resType != type ) {
 				resType = STRTYPE_MIXED;
 			}
 		}
 		// Move the start for the next skeleton part
 		skelStart = stringEnd;
 	}
 	
 	private boolean processTextUnit () {
 		// Check if we have text (as a whole)
 		boolean extract = srcFrag.hasText(false);
 		// Check for directives
 		if ( extract ) {
 			// Do the directive check after auto-skipped strings
 			if ( locDir.isWithinScope() ) {
 				if ( !locDir.isLocalizable(true) ) extract = false; 
 			}
 			else { // Outside directive scope: check if we extract text outside
 				if ( !locDir.localizeOutside() ) extract = false;
 			}
 		}
 		
 		// If no extraction: reset and move on
 		if ( !extract ) {
 			resetStorage();
 			skelStart = firstSkelStart;
 			return false;
 		}
 		
 		// Otherwise: set the text unit data
 		textUnit = new TextUnit(String.valueOf(++tuId));
 		switch ( resType ) {
 		case STRTYPE_DOUBLEQUOTED:
 			textUnit.setType("x-doublequoted");
 			break;
 		case STRTYPE_SINGLEQUOTED:
 			textUnit.setType("x-singlequoted");
 			break;
 		case STRTYPE_HEREDOC:
 			textUnit.setType("x-heredoc");
 			break;
 		case STRTYPE_NOWDOC:
 			textUnit.setType("x-nowdoc");
 			break;
 		case STRTYPE_MIXED:
 			textUnit.setType("x-mixed");
 			textUnit.setProperty(new Property(Property.NOTE,
 				"This entry is a concatenation of different types of strings: beware when moving codes or variables."));
 			break;
 		}
 		srcSkel.addContentPlaceholder(textUnit);
 		textUnit.setMimeType(MimeTypeMapper.PHP_MIME_TYPE);
 		textUnit.getSource().getFirstContent().append(srcFrag);
 		if ( !srcSkel.isEmpty() ) textUnit.setSkeleton(srcSkel);
 		
 		// Any dangling skeleton?
 		if ( skelStart < current ) {
 			srcSkel.add(inputText.substring(skelStart, current).replace("\n", lineBreak));
 		}
 		queue.add(new Event(EventType.TEXT_UNIT, textUnit));
 		skelStart = current;
 		return true;
 	}
 
 	private void resetStorage () {
 		resType = -1;
 		srcFrag = new TextFragment();
 		srcSkel = new GenericSkeleton();
 	}
 
 }
