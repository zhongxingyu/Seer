 /*===========================================================================
   Copyright (C) 2009 by the Okapi Framework contributors
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
 
 package net.sf.okapi.filters.po;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.Reader;
 import java.io.StringReader;
 import java.net.URI;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import net.sf.okapi.common.BOMAwareInputStream;
 import net.sf.okapi.common.Event;
 import net.sf.okapi.common.EventType;
 import net.sf.okapi.common.IParameters;
 import net.sf.okapi.common.IResource;
 import net.sf.okapi.common.MimeTypeMapper;
 import net.sf.okapi.common.Util;
 import net.sf.okapi.common.exceptions.BadFilterInputException;
 import net.sf.okapi.common.exceptions.IllegalFilterOperationException;
 import net.sf.okapi.common.exceptions.OkapiIOException;
 import net.sf.okapi.common.filters.IFilter;
 import net.sf.okapi.common.filterwriter.GenericFilterWriter;
 import net.sf.okapi.common.filterwriter.IFilterWriter;
 import net.sf.okapi.common.resource.DocumentPart;
 import net.sf.okapi.common.resource.Ending;
 import net.sf.okapi.common.resource.INameable;
 import net.sf.okapi.common.resource.RawDocument;
 import net.sf.okapi.common.resource.Property;
 import net.sf.okapi.common.resource.StartDocument;
 import net.sf.okapi.common.resource.StartGroup;
 import net.sf.okapi.common.resource.TextContainer;
 import net.sf.okapi.common.resource.TextFragment;
 import net.sf.okapi.common.resource.TextUnit;
 import net.sf.okapi.common.skeleton.GenericSkeleton;
 import net.sf.okapi.common.skeleton.GenericSkeletonWriter;
 import net.sf.okapi.common.skeleton.ISkeletonWriter;
 
 /**
  * Implements the IFilter interface for PO files.
  */
 public class POFilter implements IFilter {
 
 	private static final String DOMAIN_SEP = "::";
 	private static final String DOMAIN_NONE = "messages";
 	private static final String DOMAIN_DEFAULT = "default";
 
 	private static final Pattern pluralPattern = Pattern.compile(
 		"nplurals(\\s*?)=(\\s*?)(\\d*?)([\\\\|;|\\n])",
 		Pattern.CASE_INSENSITIVE);
 
 	private static final Pattern charsetPattern = Pattern.compile(
 		"(content-type)(\\s*?):(.*?)charset(\\s*?)=(\\s*?)(.*?)([\\\\|;|\\n])",
 		Pattern.CASE_INSENSITIVE);
 		
 	private Parameters params;
 	private BufferedReader reader;
 	private boolean canceled;
 	private String encoding;
 	private boolean autoDetected;
 	private String textLine;
 	private int tuId;
 	private int otherId;
 	private String lineBreak;
 	private int parseState = 0;
 	private GenericSkeleton skel;
 	private TextUnit tu;
 	private String docName;
 	private String srcLang;
 	private String trgLang;
 	private boolean hasUTF8BOM;
 	private int nbPlurals;
 	private int level;
 	private int pluralMode;
 	private int pluralCount;
 	private boolean readLine;
 	private String msgID;
 	private String locNote;
 	private String transNote;
 	private String references;
 	private String msgIDPlural;
 	private String domain;
 	private InputStream startingInput;
 	private URI inputURI;
 	private boolean hasFuzzyFlag;
 	
 	public POFilter () {
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
 		return "okf_po";
 	}
 	
 	public String getMimeType () {
 		return MimeTypeMapper.PO_MIME_TYPE;
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
 			return new Event(EventType.CANCELED);
 		}
 		if ( parseState == 1 ) return start();
 		else return readItem();
 	}
 
 	public void open (RawDocument input) {
 		open(input, true);
 	}
 	
 	public void open (RawDocument input,
 		boolean generateSkeleton)
 	{
 		setOptions(input.getSourceLanguage(), input.getTargetLanguage(),
 			input.getEncoding(), generateSkeleton);
 		if ( input.getInputCharSequence() != null ) {
 			open(input.getInputCharSequence());
 		}
 		else if ( input.getInputURI() != null ) {
 			open(input.getInputURI());
 		}
 		else if ( input.getInputStream() != null ) {
 			open(input.getInputStream());
 		}
 		else {
 			throw new BadFilterInputException("RawDocument has no input defined.");
 		}
 	}
 	
 	private void open (InputStream input) {
 		try {
 			startingInput = input;
 			// Open the input reader from the provided stream
 			BOMAwareInputStream bis = new BOMAwareInputStream(input, encoding);
 			// Correct the encoding if we have detected a different one
 			encoding = bis.detectEncoding();
 			autoDetected = bis.autoDtected();
 			hasUTF8BOM = bis.hasUTF8BOM();
 			commonOpen(new InputStreamReader(bis, encoding));
 		}
 		catch ( IOException e ) {
 			throw new OkapiIOException(e);
 		}
 	}
 
 	private void open (URI inputURI) {
 		try {
 			docName = inputURI.getPath();
 			this.inputURI = inputURI;
 			open(inputURI.toURL().openStream());
 		}
 		catch ( IOException e ) {
 			throw new OkapiIOException(e);
 		}
 	}
 
 	private void open (CharSequence inputText) {
 		encoding = "UTF-16";
 		hasUTF8BOM = false;
 		autoDetected = true;
 		commonOpen(new StringReader(inputText.toString()));
 	}
 	
 	private void setOptions (String sourceLanguage,
 		String targetLanguage,
 		String defaultEncoding,
 		boolean generateSkeleton)
 	{
 		encoding = defaultEncoding;
 		srcLang = sourceLanguage;
 		trgLang = targetLanguage;
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
 
 	private void commonOpen (Reader inputReader) {
 		close();
 		parseState = 1;
 		canceled = false;
 
 		// Initializes the variables
 		nbPlurals = 0;
 		tuId = 0;
 		otherId = 0;
 		lineBreak = System.getProperty("line.separator");
 		pluralMode = 0;
 		pluralCount = 0;
 		readLine = true;
 		msgIDPlural = "";
 		level = 0;
 		domain = DOMAIN_NONE; // Default domain prefix
 		// Compile code finder rules
 		if ( params.useCodeFinder ) {
 			params.codeFinder.compile();
 		}
 
 		// Open the input reader from the provided reader
 		reader = new BufferedReader(inputReader);
 		// Try to read the header info
 		if ( detectInformation() ) {
 			// Need to re-open the file with modified encoding
 			try {
 				reader.close();
 				if ( docName != null ) { // Was open from URI
 					startingInput.close();
 					startingInput = inputURI.toURL().openStream();
 				}
 				// Else: was open from startingInput 
 				reader = new BufferedReader(new InputStreamReader(startingInput, encoding));
 			}
 			catch ( IOException e ) {
 			}
 		}
 	}
 	
 	private Event start () {
 		parseState = 2;
 		StartDocument startDoc = new StartDocument(String.valueOf(++otherId));
 		startDoc.setName(docName);
 		startDoc.setEncoding(encoding, hasUTF8BOM);
 		startDoc.setLanguage(srcLang);
 		startDoc.setFilterParameters(params);
 		startDoc.setLineBreak(lineBreak);
		startDoc.setType("text/x-po");
		startDoc.setMimeType("text/x-po");
 		startDoc.setMultilingual(params.bilingualMode);
 		return new Event(EventType.START_DOCUMENT, startDoc);
 	}
 	
 	private Event readItem () {
 		//boolean skip = false;
 		skel = new GenericSkeleton();
 		tu = null;
 		hasFuzzyFlag = false;
 
 		if ( pluralMode == 0 ) {
 			msgID = "";
 			locNote = "";
 			transNote = "";
 			references = "";
 		}
 		else if ( pluralMode == 2 ) { // Closing plural group?
 			// Reset the plural variables
 			pluralMode = 0;
 			msgIDPlural = "";
 			pluralCount = 0;
 			// Close the group
 			level--;
 			Ending ending = new Ending(String.valueOf(++otherId));
 			ending.setSkeleton(skel);
 			return new Event(EventType.END_GROUP, ending);
 		}
 
 		while ( true ) {
 			if ( readLine ) {
 				try {
 					textLine = reader.readLine();
 				} catch ( IOException e ) {
 					throw new OkapiIOException(e);
 				}
 				if ( textLine == null ) {
 					// No more lines
 					if ( level > 0 ) { // Check if a group is open
 						level--;
 						Ending ending = new Ending(String.valueOf(++otherId));
 						ending.setSkeleton(skel);
 						return new Event(EventType.END_GROUP, ending);
 					}
 					// Else: end of the document
 					Ending ending = new Ending("ed");
 					ending.setSkeleton(skel);
 					parseState = 0;
 					return new Event(EventType.END_DOCUMENT, ending);
 				}
 			}
 			else {
 				readLine = true;
 			}
 
 			// Check for empty lines
 			if ( textLine.trim().length() == 0 ) {
 				skel.append(textLine+lineBreak);
 				continue;
 			}
 
 			// Check for 'extracted' comments (developers comments)
 			if ( textLine.startsWith("#.") ) {
 				skel.append(textLine+lineBreak);
 				// Store as a localization note
 				if ( locNote.length() > 0 ) locNote += lineBreak;
 				locNote += textLine.substring(2).trim();
 				// Check for directives
 				//TODO: for later: params.locDir.process(textLine);
 				continue;
 			}
 			
 			// Check for reference comments
 			if ( textLine.startsWith("#:") ) {
 				skel.append(textLine+lineBreak);
 				// Store as a reference property
 				if ( references.length() > 0 ) references += lineBreak;
 				references += textLine.substring(2).trim();
 				continue;
 			}
 
 			// Check for obsolete entries
 			if ( textLine.startsWith("#~") ) {
 				// They just go to the skeleton
 				skel.append(textLine+lineBreak);
 				continue;
 			}
 			
 			// Translators comments
 			if ( textLine.startsWith("# ") || textLine.startsWith("#\t") ) {
 				skel.append(textLine+lineBreak);
 				// Store as a localization note
 				if ( transNote.length() > 0 ) transNote += lineBreak;
 				transNote += textLine.substring(2).trim();
 				continue;
 			}
 			
 			// Check for flags
 			if ( textLine.startsWith("#,") ) {
 				if ( tu == null ) {
 					tu = new TextUnit(null); // No id yet, it will be set later
 				}
 				int pos = textLine.indexOf("fuzzy");
 				if ( params.bilingualMode && ( pos > -1 )) { // No fuzzy flag or monolingual mode
 					skel.append(textLine.substring(0, pos));
 					skel.addValuePlaceholder(tu, Property.APPROVED, trgLang);
 					tu.setTargetProperty(trgLang, new Property(Property.APPROVED, "no", false));
 					hasFuzzyFlag = true;
 					skel.append(textLine.substring(pos+5));
 				}
 				else {
 					skel.append(textLine);
 				}
 				skel.append(lineBreak);
 				continue;
 			}
 
 			// Check for new domain group
 			if ( textLine.startsWith("domain") ) {
 				// Check if we are closing an existing one
 				if ( level > 0 ) {
 					readLine = false; // Do not re-read this line next call
 					level--;
 					domain = DOMAIN_NONE; // Default
 					Ending ending = new Ending(String.valueOf(++otherId));
 					ending.setSkeleton(skel);
 					return new Event(EventType.END_GROUP, ending);
 				}
 				// Else: Start of domain group
 				skel.append(textLine);
 				StartGroup startGroup = new StartGroup(null, String.valueOf(++otherId));
 				startGroup.setSkeleton(skel);
 				startGroup.setType("x-gettext-domain");
 				setDomainName(startGroup);
 				level++;
 				return new Event(EventType.START_GROUP, startGroup);
 			}
 
 			// Check for plural entry
 			if ( textLine.startsWith("msgid_plural") ) {
 				pluralMode = 1;
 				msgIDPlural = getQuotedString(true);
 				// Start a plural group
 				StartGroup startGroup = new StartGroup(null, String.valueOf(++otherId));
 				startGroup.setSkeleton(skel);
 				level++; // New level for next item
 				startGroup.setType("x-gettext-plurals");
 				return new Event(EventType.START_GROUP, startGroup);
 			}
 			
 			// Check for the message ID
 			if ( textLine.startsWith("msgid") ) {
 				//if ( params.bilingualMode && !hasFuzzyFlag ) {
 				//	// Add the place for a fuzzy flag
 				//	// So the value can be created at output if needed
 				//	if ( tu == null ) {
 				//		tu = new TextUnit(null); // No id yet, it will be set later
 				//	}
 				//	skel.append("#, ");
 				//	skel.addValuePlaceholder(tu, Property.APPROVED, trgLang);
 				//	skel.append(lineBreak);
 				//	hasFuzzyFlag = true;
 				//	tu.setTargetProperty(trgLang, new Property(Property.APPROVED, "yes", false));
 				//}
 				msgID = getQuotedString(true);
 				continue;
 			}
 
 			// Check for message string
 			if ( textLine.indexOf("msgstr") == 0 ) {
 				Event event = processMsgStr();
 				if ( event != null ) return event;
 				// Else continue
 				continue;
 			}
 			
 			// Anything else: just add to the skeleton
 			skel.append(textLine+lineBreak);
 		
 		} // End of while
 	}
 
 	private Event processMsgStr () {
 		// Check for plural form
 		if ( textLine.indexOf("msgstr[") == 0 ) {
 			// Check if we are indeed in plural mode
 			if ( pluralMode == 0 ) {
 				throw new IllegalFilterOperationException(Res.getString("extraPluralMsgStr"));
 			}
 			// Check if we reached the last plural form
 			// Note that PO files have at least 2 plural entries even if nplural=1
 			pluralCount++;
 			switch ( nbPlurals ) {
 			case 1:
 			case 2:
 				if ( pluralCount == 2 ) pluralMode = 2;
 				break;
 			default: // Above 2
 				if ( pluralCount == nbPlurals ) pluralMode = 2;
 				break;
 			}
 			// Then proceed as a normal entry
 		}
 		else if ( pluralMode != 0 ) {
 			throw new IllegalFilterOperationException(Res.getString("missingPluralMsgStr"));
 		}
 
 		// Get the message string
 		String tmp = getQuotedString(false);
 		
 		// Check for header entry, and update it if required
 		if ( msgID.length() == 0 ) {
 			String id = String.valueOf(++otherId);
 			DocumentPart dp = new DocumentPart(id, false, skel);
 			String part1 = "\""+lineBreak+"\""+tmp;
 			String part2 = "\""+lineBreak;
 			boolean hasProp = false;
 			
 			// Create the modifiable property for the encoding in the header
 			Matcher m = charsetPattern.matcher(tmp);
 			if ( m.find() ) { // Replace the encoding by the reference marker
 				dp.setProperty(new Property(Property.ENCODING, encoding, false));
 				part1 = "\""+lineBreak+"\""+tmp.substring(0, m.start(6));
 				hasProp = true;
 				part2 = tmp.substring(m.end(6))+"\""+lineBreak;
 			}
 			
 			// Always reformat the lines for this entry
 			part1 = part1.replace("\\n", "\\n\""+lineBreak+"\"");
 			part2 = part2.replace("\\n", "\\n\""+lineBreak+"\"");
 			if ( part2.endsWith("\"\""+lineBreak) ) {
 				// Remove last empty string if needed
 				part2 = part2.substring(0, part2.length()-(2+lineBreak.length()));
 			}
 			
 			// Add the parts to the skeleton
 			skel.append(part1);
 			if ( hasProp ) {
 				skel.addValuePlaceholder(dp, Property.ENCODING, "");
 			}
 			if ( part2 != null ) {
 				skel.append(part2);
 			}
 			skel.add(lineBreak);
 
 			// Send this entry as a document part
 			return new Event(EventType.DOCUMENT_PART, dp);
 		}
 
 		// Else: We have a text unit to send
 		// Create it if it was not done yet
 		if ( tu == null ) tu = new TextUnit(null);
 		// Set the ID and other info
 		tu.setId(String.valueOf(++tuId));
 		tu.setPreserveWhitespaces(true);
 		tu.setSkeleton(skel);
		tu.setMimeType("text/x-po");
 		
 		if ( locNote.length() > 0 ) {
 			tu.setProperty(new Property(Property.NOTE, locNote));
 		}
 		if ( transNote.length() > 0 ) {
 			tu.setProperty(new Property("transnote", transNote));
 		}
 		if ( references.length() > 0 ) {
 			tu.setProperty(new Property("references", references));
 		}
 
 		// Set the text and possibly its translation
 		// depending on the processing mode
 		if ( params.bilingualMode ) {
 			String sID = msgID;
 			if (( pluralMode != 0 ) && ( pluralCount-1 > 0 )) {
 				sID = msgIDPlural;
 			}
 			// Add the source text and parse it
 			toAbstract(tu.setSourceContent(new TextFragment(sID)));
 			// Create an ID if requested
 			if ( params.makeID ) {
 				// Note we always use msgID for resname, not msgIDPlural
 				if ( pluralMode == 0 ) {
 					tu.setName(Util.makeID(domain+DOMAIN_SEP+msgID));
 				}
 				else {
 					tu.setName(Util.makeID(domain+DOMAIN_SEP+msgID)
 						+ String.format("-%d", pluralCount-1));
 				}
 			}
 			// Set the translation if one exists
 			if ( tmp.length() > 0 ) {
 				TextContainer tc = tu.createTarget(trgLang, false, IResource.CREATE_EMPTY);
 				tc.setContent(toAbstract(new TextFragment(tmp)));
 				if ( !hasFuzzyFlag ) {
 					tu.setTargetProperty(trgLang, new Property(Property.APPROVED, "yes", true));
 				}
 			}
 			//else { // Correct the approved property
 			//	tu.getTargetProperty(trgLang, Property.APPROVED).setValue("no");
 			//}
 		}
 		else { // Parameters.MODE_MONOLINGUAL
 			if ( pluralMode == 0 ) {
 				tu.setName(domain+DOMAIN_SEP+msgID);
 			}
 			else {
 				tu.setName(domain+DOMAIN_SEP+msgID
 					+ String.format("-%d", pluralCount-1));
 			}
 			// Add the source and parse it 
 			toAbstract(tu.setSourceContent(new TextFragment(tmp)));
 		}
 
 		// Translate flag should be set to no for no-0 case of 1-plural-type forms
 		// Should be true otherwise
 		if (( pluralMode != 0 ) && ( nbPlurals == 1 ) && ( pluralCount-1 > 0 )) {
 			tu.setIsTranslatable(false);
 		}
 		// Else: it is TextUnit is translatable by default
 
 		skel.addContentPlaceholder(tu, trgLang);
 		skel.append("\""+lineBreak);
 		
 		return new Event(EventType.TEXT_UNIT, tu);
 	}
 		
 	private String getQuotedString (boolean forMsgID) {
 		StringBuilder  sbTmp = new StringBuilder();
 		try {
 			// Get opening quote
 			int nPos1 = textLine.indexOf('"');
 			if ( nPos1 == -1 ) {
 				throw new IllegalFilterOperationException(Res.getString("missingStartQuote"));
 			}
 			// Get closing quote
 			int nPos2 = textLine.lastIndexOf('"');
 			if (( nPos2 == -1 ) || ( nPos2 == nPos1 )) {
 				throw new IllegalFilterOperationException(Res.getString("missingEndQuote"));
 			}
 			if ( forMsgID ) {
 				skel.append(textLine+lineBreak);
 			}
 			else {
 				// Copy codes before text in code buffer
 				skel.append(textLine.substring(0, nPos1+1));
 				//TODO: make sure: The ending part is generated automatically when writing
 			}
 			// Copy text in text buffer
 			sbTmp.append(textLine.substring(nPos1+1, nPos2));
 
 			// Check for spliced strings
 			String sTmp;
 			while ( true ) {
 				textLine = reader.readLine();
 				if ( textLine == null ) {
 					// No more lines
 					return sbTmp.toString();
 				}
 				else {
 					sTmp = textLine.trim();
 					// Check if it's a quoted line detected
 					if ( sTmp.startsWith("\"") ) {
 						// Get opening quote
 						nPos1 = textLine.indexOf('"');
 						if ( nPos1 == -1 ) {
 							throw new Exception(Res.getString("missingStartQuote"));
 						}
 						// Get closing quote
 						nPos2 = textLine.lastIndexOf('"');
 						if (( nPos2 == -1 ) || ( nPos2 == nPos1 )) {
 							throw new Exception(Res.getString("missingEndQuote"));
 						}
 						if ( forMsgID ) {
 							skel.append(textLine+lineBreak);
 						}
 						// Else: No need to put white spaces in codes buffer
 						// Then add the text	
 						sbTmp.append(textLine.substring(nPos1+1, nPos2));
 					}
 					else { // No more following quoted lines: end of text
 						readLine = false;
 						return sbTmp.toString();
 					}
 				}
 			}
 		}
 		catch ( Throwable e ) {
 			//LogMessage(LogType.ERROR, E.Message + "\n" + E.StackTrace);
 			throw new IllegalFilterOperationException(Res.getString("problemWithQuotes"));
 		}
 	}
 
 	private TextFragment toAbstract (TextFragment frag) {
 		//TODO: Possibly, un-escaping, \n to line-breaks, etc.?
 
 		// Sets the inline codes
 		if ( params.useCodeFinder ) {
 			params.codeFinder.process(frag);
 		}
 		return frag;
 	}
 	
 	private void setDomainName (INameable res) {
 		// The domain name is the second part of the line
 		String[] aTokens = textLine.split("\\s");
 		//TODO: Is domain quoted or not???
 		if ( aTokens.length < 2 ) {
 			// No name, use a default
 			domain = DOMAIN_DEFAULT;
 		}
 		else {
 			domain = aTokens[1];
 			res.setName(domain);
 		}
 	}
 
 
 	/**
 	 * Detects declared encoding and plural form.
 	 * @return True if the reader needs to be re-opened with a new encoding,
 	 * false if not.
 	 */
  	private boolean detectInformation () {
  		char[] buffer;
  		try {
  			// Read the a chunk of the beginning of the file
 			reader.mark(1024);
 	 		buffer = new char[1024];
 	 		int n = reader.read(buffer, 0, 1024);
 	 		reader.reset();
 	 		String tmp = new String(buffer);  
 
 	 		// try to detect the line-break type
 			n = tmp.indexOf('\n');
 			if ( n == -1 ) {
 				n = tmp.indexOf('\r');
 				if ( n != -1 ) lineBreak = "\r";
 				// Else: cannot detect, use default
 			}
 			else {
 				if ( tmp.charAt(n-1) != '\r' ) lineBreak = "\n";
 				// Else: same as default
 			}
 	 		
 			// Try to detect plural information
 			Matcher m = pluralPattern.matcher(tmp);
 			if ( m.find() ) {
 				nbPlurals = Integer.valueOf(m.group(3));
 			}
 			// Else: no plural definition found, use default
 
 			// Try to detect encoding information
 			m = charsetPattern.matcher(tmp);
 			if ( m.find() ) {
 				if ( autoDetected ) {
 					if ( !encoding.equalsIgnoreCase(m.group(6)) ) {
 						// Difference between auto-detected and internal
 						// Auto-detected wins
 						//TODO: Warning that the internal encoding may be wrong!
 					}
 					// Else: Same as auto-detected, keep that one
 				}
 				else { // No auto-detection before
 					// Compare with the default
 					if ( !encoding.equalsIgnoreCase(m.group(6)) ) {
 						// Internal wins over default
 						encoding = m.group(6);
 						// And we need to re-open the reader with the new encoding
 						return true;
 					}
 					// Else: default and declared encoding are the same
 				}
 			}
 			// Else: Use the encoding already set
 			return false;
 		}
  		catch ( IOException e ) {
  			throw new OkapiIOException(e);
 		}
  		finally {
  			buffer = null;
  		}
  	}
  
 }
