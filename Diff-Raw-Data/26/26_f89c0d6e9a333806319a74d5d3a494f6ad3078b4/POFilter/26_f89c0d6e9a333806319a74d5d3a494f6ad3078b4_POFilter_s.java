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
 import java.io.InputStreamReader;
 import java.io.UnsupportedEncodingException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Logger;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import net.sf.okapi.common.BOMNewlineEncodingDetector;
 import net.sf.okapi.common.Event;
 import net.sf.okapi.common.EventType;
 import net.sf.okapi.common.IParameters;
 import net.sf.okapi.common.IResource;
 import net.sf.okapi.common.MimeTypeMapper;
 import net.sf.okapi.common.Util;
 import net.sf.okapi.common.exceptions.OkapiIllegalFilterOperationException;
 import net.sf.okapi.common.exceptions.OkapiIOException;
 import net.sf.okapi.common.exceptions.OkapiUnsupportedEncodingException;
 import net.sf.okapi.common.filters.FilterConfiguration;
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
 
 	public static final String PROPERTY_PLURALFORMS = "pluralforms";
 	public static final String PROPERTY_TRANSNOTE = "transnote";
 	public static final String PROPERTY_REFERENCES = "references";
 	
 	private static final String DOMAIN_SEP = "::";
 	private static final String DOMAIN_NONE = "messages";
 	private static final String DOMAIN_DEFAULT = "default";
 	private static final int DEFAULT_NPLURALS = 2;  // Default = "Germanic langauges" (per Gettext doc)
 
 	private static final int PLURALFORMS_VALUEGROUP = 3;
 	private static final Pattern pluralformsPattern = Pattern.compile(
 		"(plural-forms:)(\\s*)(.*?)(\\\\n|\\z)",
 		Pattern.CASE_INSENSITIVE);
 
 	private static final int NPLURALS_VALUEGROUP = 4;
 	private static final Pattern npluralsPattern = Pattern.compile(
 		"nplurals(\\s*)(=)(\\s*)(\\d*)(;|\\\\n|\\z)",
 		Pattern.CASE_INSENSITIVE);
 
 	private static final int CHARSET_VALUEGROUP = 6;
 	private static final Pattern charsetPattern = Pattern.compile(
 		"(content-type)(\\s*):(.*?)charset(\\s*)=(\\s*)(.*?)([\\\\|;|\\n])",
 		Pattern.CASE_INSENSITIVE);
 			
 	private final Logger logger = Logger.getLogger(getClass().getName());
 
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
 	private int nPlurals;
 	private int level;
 	private int pluralMode; // 0=not in a plural, 1=inside a plural, 2=closing a plural 
 	private int pluralCount;
 	private boolean readLine;
 	private String msgID;
 	private String locNote;
 	private String transNote;
 	private String references;
 	private String msgIDPlural;
 	private String domain;
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
 
 	public List<FilterConfiguration> getConfigurations () {
 		List<FilterConfiguration> list = new ArrayList<FilterConfiguration>();
 		list.add(new FilterConfiguration(getName(),
 			MimeTypeMapper.PO_MIME_TYPE,
 			getClass().getName(),
 			"PO (Standard)",
 			"Standard bilingual PO files"));
 		list.add(new FilterConfiguration(getName()+"-monolingual",
 			MimeTypeMapper.PO_MIME_TYPE,
 			getClass().getName(),
 			"PO (Monolingual)",
 			"Monolingual PO files (msgid is a real ID, not the source text).",
 			"monolingual.fprm"));
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
 			return new Event(EventType.CANCELED);
 		}
 		if ( parseState == 1 ) return start();
 		else return readItem();
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
 
 		// Initializes the variables
 		nPlurals = DEFAULT_NPLURALS;
 		tuId = 0;
 		otherId = 0;
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
 
 		// Detect and remove BOM
 		BOMNewlineEncodingDetector detector = new BOMNewlineEncodingDetector(input.getStream(), input.getEncoding());
 		detector.detectAndRemoveBom();
 		input.setEncoding(detector.getEncoding());
 		encoding = input.getEncoding();
 				
 		// Open the input stream
 		try {
 			reader = new BufferedReader(new InputStreamReader(detector.getInputStream(), input.getEncoding()));
 		}
 		catch ( UnsupportedEncodingException e ) {
 			throw new OkapiUnsupportedEncodingException(
 				String.format("The encoding '%s' is not supported.", encoding), e);
 		}
 	
 		srcLang = input.getSourceLanguage();
 		trgLang = input.getTargetLanguage();
 		hasUTF8BOM = detector.hasUtf8Bom();
 		lineBreak = detector.getNewlineType().toString();
 		if ( input.getInputURI() != null ) {
 			docName = input.getInputURI().getPath();
 		}
 		
 		// Try to read the header info
 		if ( detectInformation() ) {
 			// Need to re-open the file with modified encoding
 			try {
 				reader.close();
 			}
 			catch ( IOException e ) {
 				throw new OkapiIOException("Error re-opening the input.", e);
 			}
 			input.setEncoding(encoding);
 			reader = new BufferedReader(input.getReader());
 		}
 	}
 	
 	private Event start () {
 		parseState = 2;
 		StartDocument startDoc = new StartDocument(String.valueOf(++otherId));
 		startDoc.setName(docName);
 		startDoc.setEncoding(encoding, hasUTF8BOM);
 		startDoc.setLanguage(srcLang);
 		startDoc.setFilterParameters(params);
 		startDoc.setFilterWriter(createFilterWriter());
 		startDoc.setLineBreak(lineBreak);
 		startDoc.setType(MimeTypeMapper.PO_MIME_TYPE);
 		startDoc.setMimeType(getMimeType());
 		startDoc.setMultilingual(params.bilingualMode);
 		return new Event(EventType.START_DOCUMENT, startDoc);
 	}
 	
 	private Event readItem () {
 		//boolean skip = false;
 		skel = new GenericSkeleton();
 		tu = null;
 
 		if ( pluralMode == 0 ) {
 			msgID = "";
 			locNote = "";
 			transNote = "";
 			references = "";
 			hasFuzzyFlag = false;
 		}
 		else if ( pluralMode == 2 ) { // Closing plural group?
 			// Reset the plural variables
 			hasFuzzyFlag = false;
 			pluralMode = 0;
 			msgIDPlural = "";
 			pluralCount = 0;
 			// Close the group
 			level--;
 			Ending ending = new Ending(String.valueOf(++otherId));
 			ending.setSkeleton(skel);
 			return new Event(EventType.END_GROUP, ending);
 		}
 		else if ( pluralMode == 1 ) { // Inside a plural group
 			if ( hasFuzzyFlag ) {
 				tu = new TextUnit(null); // Id is set after
 				tu.setTargetProperty(trgLang, new Property(Property.APPROVED, "no", false));
 			}
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
 					close();
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
 				skel.append(lineBreak);
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
 				// Copy the text unit info to the group if needed
 				if ( tu != null ) {
 					Property prop = tu.getTargetProperty(trgLang, Property.APPROVED);
 					if ( prop != null ) {
 						startGroup.setTargetProperty(trgLang, prop);
 					}
 					// Make sure the skeleton placeholders point to the group not the text unit.
 					skel.changeSelfReferents(startGroup);
 				}
 				startGroup.setSkeleton(skel);
 				level++; // New level for next item
 				startGroup.setType("x-gettext-plurals");
 				startGroup.setMimeType(getMimeType());
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
 				throw new OkapiIllegalFilterOperationException(Res.getString("extraPluralMsgStr"));
 			}
 			// Check if we reached the last plural form
 			// Note that PO files have at least 2 plural entries even if nplural=1
 			pluralCount++;
 			switch ( nPlurals ) {
 			case 1:
 			case 2:
 				if ( pluralCount == 2 ) pluralMode = 2;
 				break;
 			default: // Above 2
 				if ( pluralCount == nPlurals ) pluralMode = 2;
 				break;
 			}
 			// Then proceed as a normal entry
 		}
 		else if ( pluralMode != 0 ) {
 			throw new OkapiIllegalFilterOperationException(Res.getString("missingPluralMsgStr"));
 		}
 
 		// Get the message string
 		StringBuilder tmp = new StringBuilder(getQuotedString(false));
 		
 		// Check for header entry, and update it if required
 		if ( msgID.length() == 0 ) {
 			// Initialize the header and its string
 			String id = String.valueOf(++otherId);
 			DocumentPart dp = new DocumentPart(id, false, skel);
 			tmp.insert(0, "\""+lineBreak+"\"");
 			tmp.append("\""+lineBreak);
 			String TMPMARKER = "\u001E";
 
 			// Look for encoding field
 			Matcher m = charsetPattern.matcher(tmp.toString());
 			if ( m.find() ) { // prepare for property creation
 				tmp.replace(m.start(CHARSET_VALUEGROUP), m.end(CHARSET_VALUEGROUP),
 					TMPMARKER + Property.ENCODING + "=" + encoding + TMPMARKER);
 			}
 			// Look for plural form field
 			m = pluralformsPattern.matcher(tmp.toString());
 			if ( m.find() ) { // prepare for property creation
 				tmp.replace(m.start(PLURALFORMS_VALUEGROUP), m.end(PLURALFORMS_VALUEGROUP),
 					TMPMARKER + PROPERTY_PLURALFORMS + "=" + m.group(PLURALFORMS_VALUEGROUP) + TMPMARKER);
 			}
 
 			// Loop through the inserted properties, create them and add to the skeleton
 			int start = 0;
 			int n1, n2, mid;
 			while ( (n1 = tmp.indexOf(TMPMARKER, start)) > -1 ) {
 				n2 = tmp.indexOf(TMPMARKER, n1+1);
 				// Text before
 				skel.append(tmp.substring(start, n1).replace("\\n", "\\n\""+lineBreak+"\""));
 				String data = tmp.substring(n1+1, n2);
 				mid = data.indexOf('=');
 				// Create property
 				Property prop = new Property(data.substring(0, mid),
 					data.substring(mid+1), false);
 				dp.setProperty(prop);
 				skel.addValuePlaceholder(dp, prop.getName(), "");
 				start = n2+1;
 			}
 			String end = tmp.substring(start).replace("\\n", "\\n\""+lineBreak+"\"");
 			// Remove last empty string if needed
 			if ( end.endsWith("\"\""+lineBreak) ) {
 				end = end.substring(0, end.length()-(2+lineBreak.length()));
 			}
 			skel.append(end);
 
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
 		//TODO: Need to adjust for each format
 		tu.setMimeType(getMimeType());
 		
 		if ( locNote.length() > 0 ) {
 			tu.setProperty(new Property(Property.NOTE, locNote));
 		}
 		if ( transNote.length() > 0 ) {
 			tu.setProperty(new Property(PROPERTY_TRANSNOTE, transNote));
 		}
 		if ( references.length() > 0 ) {
 			tu.setProperty(new Property(PROPERTY_REFERENCES, references));
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
 				tc.setContent(toAbstract(new TextFragment(tmp.toString())));
 				if ( !hasFuzzyFlag ) {
 					tu.setTargetProperty(trgLang, new Property(Property.APPROVED, "yes", true));
 				}
 				// Synchronizes source and target codes as much as possible
 				tc.synchronizeCodes(tu.getSourceContent());
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
 			toAbstract(tu.setSourceContent(new TextFragment(tmp.toString())));
 		}
 
 		// Translate flag should be set to no for no-0 case of 1-plural-type forms
 		// Should be true otherwise
 		if (( pluralMode != 0 ) && ( nPlurals == 1 ) && ( pluralCount-1 > 0 )) {
 			tu.setIsTranslatable(false);
 		}
 		// Else: it is TextUnit is translatable by default
 
 		skel.addContentPlaceholder(tu, trgLang);
 		skel.append("\""+lineBreak);
 		
 		return new Event(EventType.TEXT_UNIT, tu);
 	}
 
 /* Copy before Aug-22-09 changes
 	private Event processMsgStr () {
 		// Check for plural form
 		if ( textLine.indexOf("msgstr[") == 0 ) {
 			// Check if we are indeed in plural mode
 			if ( pluralMode == 0 ) {
 				throw new OkapiIllegalFilterOperationException(Res.getString("extraPluralMsgStr"));
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
 			throw new OkapiIllegalFilterOperationException(Res.getString("missingPluralMsgStr"));
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
 			
 			//TODO: plural forms
 			
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
 			else skel.add(lineBreak);
 
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
 		//TODO: Need to adjust for each format
 		tu.setMimeType(getMimeType());
 		
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
 				// Synchronizes source and target codes as much as possible
 				tc.synchronizeCodes(tu.getSourceContent());
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
 */		
 	
 	private String getQuotedString (boolean forMsgID) {
 		StringBuilder  sbTmp = new StringBuilder();
 		try {
 			// Get opening quote
 			int nPos1 = textLine.indexOf('"');
 			if ( nPos1 == -1 ) {
 				throw new OkapiIllegalFilterOperationException(Res.getString("missingStartQuote"));
 			}
 			// Get closing quote
 			int nPos2 = textLine.lastIndexOf('"');
 			if (( nPos2 == -1 ) || ( nPos2 == nPos1 )) {
 				throw new OkapiIllegalFilterOperationException(Res.getString("missingEndQuote"));
 			}
 			if ( forMsgID ) {
 				skel.append(textLine+lineBreak);
 			}
 			else {
 				// Copy codes before text in code buffer
 				skel.append(textLine.substring(0, nPos1+1));
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
 			throw new OkapiIllegalFilterOperationException(Res.getString("problemWithQuotes"));
 		}
 	}
 
 	private TextFragment toAbstract (TextFragment frag) {
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
 	 		reader.read(buffer, 0, 1024);
 	 		reader.reset();
 	 		String tmp = new String(buffer);  
 
 			// Try to detect plural information
 			Matcher m = pluralformsPattern.matcher(tmp);
 			if ( m.find() ) {
 				String data = m.group(PLURALFORMS_VALUEGROUP);
 				m = npluralsPattern.matcher(data);
 				if ( m.find() ) {
 					try {
 						nPlurals = Integer.valueOf(m.group(NPLURALS_VALUEGROUP));
 					}
 					catch ( NumberFormatException e ) {
 						//TODO: If file not a POT, it may be an error, other wise it is normal
 						// The value was likely to be a place-holder
 						// Just swallow the error
 						nPlurals = DEFAULT_NPLURALS; // Make sure to reset to default
 					}
 					if ( nPlurals < 0 ) {
 						nPlurals = DEFAULT_NPLURALS; // Make sure to reset to default
 						logger.warning(String.format(Res.getString("npluralsInvalid"), data, nPlurals));
 					}
 				}
 				else { // Missing nplurals field
 					nPlurals = DEFAULT_NPLURALS; // Make sure to reset to default
 					logger.warning(String.format(Res.getString("npluralsNotDetected"), data, nPlurals));
 				}
 			}
 			// Else: no plural definition found, use default
 
 			// Try to detect encoding information
 			m = charsetPattern.matcher(tmp);
 			if ( m.find() ) {
 				if ( m.group(CHARSET_VALUEGROUP).equalsIgnoreCase("charset") ) {
 					// POT may have 'charset' for encoding:
 					// We ignore it and use the auto-detected or default encoding
 					// Use the encoding already set
 					return false;
 				}
 				if ( autoDetected ) {
 					if ( !encoding.equalsIgnoreCase(m.group(CHARSET_VALUEGROUP)) ) {
 						// Difference between auto-detected and internal
 						// Auto-detected wins
 						//TODO: Warning that the internal encoding may be wrong!
 					}
 					// Else: Same as auto-detected, keep that one
 				}
 				else { // No auto-detection before
 					// Compare with the default
 					if ( !encoding.equalsIgnoreCase(m.group(CHARSET_VALUEGROUP)) ) {
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
