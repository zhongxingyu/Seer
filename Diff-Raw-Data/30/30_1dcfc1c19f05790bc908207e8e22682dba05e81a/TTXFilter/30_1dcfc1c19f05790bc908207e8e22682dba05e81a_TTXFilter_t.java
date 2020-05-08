 /*===========================================================================
   Copyright (C) 2008-2010 by the Okapi Framework contributors
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
 
 package net.sf.okapi.filters.ttx;
 
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.logging.Logger;
 
 import javax.xml.stream.XMLInputFactory;
 import javax.xml.stream.XMLStreamConstants;
 import javax.xml.stream.XMLStreamException;
 import javax.xml.stream.XMLStreamReader;
 
 import net.sf.okapi.common.BOMNewlineEncodingDetector;
 import net.sf.okapi.common.Event;
 import net.sf.okapi.common.EventType;
 import net.sf.okapi.common.IParameters;
 import net.sf.okapi.common.IdGenerator;
 import net.sf.okapi.common.MimeTypeMapper;
 import net.sf.okapi.common.UsingParameters;
 import net.sf.okapi.common.Util;
 import net.sf.okapi.common.annotation.AltTranslation;
 import net.sf.okapi.common.annotation.AltTranslationsAnnotation;
 import net.sf.okapi.common.encoder.EncoderManager;
 import net.sf.okapi.common.exceptions.OkapiIOException;
 import net.sf.okapi.common.filters.FilterConfiguration;
 import net.sf.okapi.common.filters.IFilter;
 import net.sf.okapi.common.filters.IFilterConfigurationMapper;
 import net.sf.okapi.common.filterwriter.GenericFilterWriter;
 import net.sf.okapi.common.filterwriter.IFilterWriter;
 import net.sf.okapi.common.LocaleId;
 import net.sf.okapi.common.query.MatchType;
 import net.sf.okapi.common.resource.Code;
 import net.sf.okapi.common.resource.DocumentPart;
 import net.sf.okapi.common.resource.Ending;
 import net.sf.okapi.common.resource.RawDocument;
 import net.sf.okapi.common.resource.Property;
 import net.sf.okapi.common.resource.Segment;
 import net.sf.okapi.common.resource.StartDocument;
 import net.sf.okapi.common.resource.TextContainer;
 import net.sf.okapi.common.resource.TextFragment;
 import net.sf.okapi.common.resource.TextPart;
 import net.sf.okapi.common.resource.TextUnit;
 import net.sf.okapi.common.resource.TextFragment.TagType;
 import net.sf.okapi.common.skeleton.GenericSkeleton;
 import net.sf.okapi.common.skeleton.ISkeletonWriter;
 
 @UsingParameters(Parameters.class)
 public class TTXFilter implements IFilter {
 
 	private final static String MATCHPERCENT = "MatchPercent";
 	private final static String ORIGIN = "Origin";
 	
 	// Characters no considered as 'text' in TTX (for un-segmented entries)
 	private final static String TTXNOTEXTCHARS = "\u00a0~`!@#$%^&*()_+=-{[}]|\\:;\"'<,>.?/";
 	
 	private final static String TARGETLANGUAGE_ATTR = "TargetLanguage";
 	private final Logger logger = Logger.getLogger(getClass().getName());
 	
 	private boolean hasNext;
 	private XMLStreamReader reader;
 	private String docName;
 	private int tuId;
 	private IdGenerator otherId;
 	private LocaleId srcLoc;
 	private LocaleId trgLoc;
 	private String srcLangCode;
 	private String trgLangCode;
 	private String trgDefFont;
 	private LinkedList<Event> queue;
 	private boolean canceled;
 	private GenericSkeleton skel;
 	private TextUnit tu;
 	private Parameters params;
 	//private boolean sourceDone;
 	//private boolean targetDone;
 	private String encoding;
 	private String lineBreak;
 	private boolean hasUTF8BOM;
 	private StringBuilder buffer;
 //	private boolean useDF;
 	private boolean insideContent;
 	private TTXSkeletonWriter skelWriter;
 	private EncoderManager encoderManager;
 	
 	public TTXFilter () {
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
 			}
 			hasNext = false;
 		}
 		catch ( XMLStreamException e) {
 			throw new OkapiIOException(e);
 		}
 	}
 
 	public String getName () {
 		return "okf_ttx";
 	}
 
 	public String getDisplayName () {
 		return "TTX Filter (BETA)";
 	}
 
 	public String getMimeType () {
 		return MimeTypeMapper.TTX_MIME_TYPE;
 	}
 
 	public List<FilterConfiguration> getConfigurations () {
 		List<FilterConfiguration> list = new ArrayList<FilterConfiguration>();
 		list.add(new FilterConfiguration(getName(),
 			MimeTypeMapper.TTX_MIME_TYPE,
 			getClass().getName(),
 			"TTX",
 			"Configuration for Trados TTX documents.",
 			null,
 			".ttx;"));
 		list.add(new FilterConfiguration(getName()+"-noForcedTuv",
 			MimeTypeMapper.TTX_MIME_TYPE,
 			getClass().getName(),
 			"TTX (without forced Tuv in output)",
 			"Configuration for Trados TTX documents without forcing Tuv in output.",
 			"noForcedTuv.fprm",
 			".ttx;"));
 		return list;
 	}
 	
 	public EncoderManager getEncoderManager () {
 		if ( encoderManager == null ) {
 			encoderManager = new EncoderManager();
 			encoderManager.setMapping(MimeTypeMapper.TTX_MIME_TYPE, "net.sf.okapi.common.encoder.XMLEncoder");
 		}
 		return encoderManager;
 	}
 	
 	public IParameters getParameters () {
 		return params;
 	}
 
 	public boolean hasNext () {
 		return hasNext;
 	}
 
 	public Event next () {
 		try {
 			// Check for cancellation first
 			if ( canceled ) {
 				queue.clear();
 				queue.add(new Event(EventType.CANCELED));
 				hasNext = false;
 			}
 			
 			// Parse next if nothing in the queue
 			if ( queue.isEmpty() ) {
 				if ( !read() ) {
 					Ending ending = new Ending(otherId.createId());
 					ending.setSkeleton(skel);
 					queue.add(new Event(EventType.END_DOCUMENT, ending));
 				}
 			}
 			
 			// Return the head of the queue
 			if ( queue.peek().getEventType() == EventType.END_DOCUMENT ) {
 				hasNext = false;
 			}
 			return queue.poll();
 		}
 		catch ( XMLStreamException e ) {
 			throw new OkapiIOException(e);
 		}
 	}
 
 	public void open (RawDocument input) {
 		open(input, true);
 	}
 	
 	public void open (RawDocument input,
 		boolean generateSkeleton)
 	{
 		try {
 			close();
 			canceled = false;
 
 			XMLInputFactory fact = XMLInputFactory.newInstance();
 			fact.setProperty(XMLInputFactory.IS_COALESCING, true);
 			
 			//fact.setXMLResolver(new DefaultXMLResolver());
 			//TODO: Resolve the re-construction of the DTD, for now just skip it
 			fact.setProperty(XMLInputFactory.SUPPORT_DTD, false);
 
 			// Determine encoding based on BOM, if any
 			input.setEncoding("UTF-8"); // Default for XML, other should be auto-detected
 			BOMNewlineEncodingDetector detector = new BOMNewlineEncodingDetector(input.getStream(), input.getEncoding());
 			detector.detectBom();
 			if ( detector.isAutodetected() ) {
 				reader = fact.createXMLStreamReader(input.getStream(), detector.getEncoding());
 			}
 			else {
 				reader = fact.createXMLStreamReader(input.getStream());
 			}
 
 			String realEnc = reader.getCharacterEncodingScheme();
 			if ( realEnc != null ) encoding = realEnc;
 			else encoding = input.getEncoding();
 
 			// Set the language codes for the skeleton writer
 			if ( skelWriter == null ) {
 				skelWriter = new TTXSkeletonWriter(params.getForceSegments());
 			}
 
 			srcLoc = input.getSourceLocale();
 			if ( srcLoc == null ) throw new NullPointerException("Source language not set.");
 			srcLangCode = srcLoc.toString().toUpperCase();
 			skelWriter.setSourceLanguageCode(srcLangCode);
 			
 			trgLoc = input.getTargetLocale();
 			if ( trgLoc == null ) throw new NullPointerException("Target language not set.");
 			trgLangCode = trgLoc.toString().toUpperCase(); // Default to create new entries
 			skelWriter.setTargetLanguageCode(trgLangCode);
 			
 			hasUTF8BOM = detector.hasUtf8Bom();
 			lineBreak = detector.getNewlineType().toString();
 			if ( input.getInputURI() != null ) {
 				docName = input.getInputURI().getPath();
 			}
 
 			insideContent = false;
 			tuId = 0;
 			otherId = new IdGenerator(null, "o");
 			// Set the start event
 			hasNext = true;
 			queue = new LinkedList<Event>();
 			buffer = new StringBuilder();
 			trgDefFont = null;
 			
 //			useDF = false;
 //			// By default, for now, use DF for CJK only
 //			if ( trgLoc.sameLanguageAs("ko")
 //				|| trgLoc.sameLanguageAs("zh")
 //				|| trgLoc.sameLanguageAs("ja") ) {
 //				useDF = true;
 //			}
 			
 			StartDocument startDoc = new StartDocument(otherId.createId());
 			startDoc.setName(docName);
 			startDoc.setEncoding(encoding, hasUTF8BOM);
 			startDoc.setLocale(srcLoc);
 			startDoc.setFilterParameters(getParameters());
 			startDoc.setFilterWriter(createFilterWriter());
 			startDoc.setType(MimeTypeMapper.TTX_MIME_TYPE);
 			startDoc.setMimeType(MimeTypeMapper.TTX_MIME_TYPE);
 			startDoc.setMultilingual(true);
 			startDoc.setLineBreak(lineBreak);
 			queue.add(new Event(EventType.START_DOCUMENT, startDoc));
 
 			// The XML declaration is not reported by the parser, so we need to
 			// create it as a document part when starting
 			skel = new GenericSkeleton();
 			startDoc.setProperty(new Property(Property.ENCODING, encoding, false));
 			skel.append("<?xml version=\"1.0\" encoding=\"");
 			skel.addValuePlaceholder(startDoc, Property.ENCODING, LocaleId.EMPTY);
 			skel.append("\"?>");
 			startDoc.setSkeleton(skel);
 		}
 		catch ( XMLStreamException e) {
 			throw new OkapiIOException(e);
 		}
 	}
 	
 	@Override
 	public void setFilterConfigurationMapper (IFilterConfigurationMapper fcMapper) {
 	}
 
 	public void setParameters (IParameters params) {
 		this.params = (Parameters)params;
 	}
 
 	public ISkeletonWriter createSkeletonWriter() {
 		if ( skelWriter == null ) {
 			skelWriter = new TTXSkeletonWriter(params.getForceSegments());
 		}
 		return skelWriter;
 	}
 
 	public IFilterWriter createFilterWriter () {
 		return new GenericFilterWriter(createSkeletonWriter(), getEncoderManager());
 	}
 
 	private boolean whitespacesOnly (String text) {
 		for ( int i=0; i<text.length(); i++ ) {
 			if ( !Character.isWhitespace(text.charAt(i)) ) return false;
 		}
 		return true;
 	}
 	
 	private boolean read () throws XMLStreamException {
 		skel = new GenericSkeleton();
 		buffer.setLength(0);
 
 		while ( true ) {
 			switch ( reader.getEventType() ) {
 			case XMLStreamConstants.START_ELEMENT:
 				String name = reader.getLocalName();
 				if ( "Tu".equals(name) || "ut".equals(name) || "df".equals(name) ) {
 					if ( processTextUnit(name) ) return true;
 					// We may return on an end-tag (e.g. Raw), so check for it
 					if ( reader.getEventType() == XMLStreamConstants.START_ELEMENT ) { 
 						buildStartElement(true);
 						// The element at the exit may be different than at the call
 						// so we refresh the name here to store the correct ending
 						name = reader.getLocalName(); 
 						storeUntilEndElement(name);
 					}
 					continue; // reader.next() was called
 				}
 				else if ( "UserSettings".equals(name) ){
 					processUserSettings();
 				}
 				else if ( "Raw".equals(name) ) {
 					insideContent = true;
 					buildStartElement(true);
 				}
 				else {
 					buildStartElement(true);
 				}
 				break;
 				
 			case XMLStreamConstants.END_ELEMENT:
 				buildEndElement(true);
 				break;
 				
 			case XMLStreamConstants.SPACE: // Non-significant spaces
 				skel.append(reader.getText().replace("\n", lineBreak));
 				break;
 
 			case XMLStreamConstants.CHARACTERS:
 			case XMLStreamConstants.CDATA:
 				if ( insideContent && !whitespacesOnly(reader.getText()) ) {
 					if ( processTextUnit(null) ) return true;
 					continue; // next() was called
 				}
 				else {
 					skel.append(Util.escapeToXML(reader.getText().replace("\n", lineBreak), 0, true, null));
 				}
 				break;
 				
 			case XMLStreamConstants.COMMENT:
 				skel.append("<!--"+ reader.getText().replace("\n", lineBreak) + "-->");
 				break;
 				
 			case XMLStreamConstants.PROCESSING_INSTRUCTION:
 				skel.append("<?"+ reader.getPITarget() + " " + reader.getPIData() + "?>");
 				break;
 				
 			case XMLStreamConstants.DTD:
 				//TODO: Reconstruct the DTD declaration
 				// but how? nothing is available to do that
 				break;
 				
 			case XMLStreamConstants.ENTITY_REFERENCE:
 			case XMLStreamConstants.ENTITY_DECLARATION:
 			case XMLStreamConstants.NAMESPACE:
 			case XMLStreamConstants.NOTATION_DECLARATION:
 			case XMLStreamConstants.ATTRIBUTE:
 				break;
 			case XMLStreamConstants.START_DOCUMENT:
 				break;
 			case XMLStreamConstants.END_DOCUMENT:
 				break;
 			}
 			
 			if ( reader.hasNext() ) reader.next();
 			else return false;
 		}
 	}
 
 	/* A text unit starts with either non-whitespace text, internal ut, df, or Tu.
 	 * It ends with end of Raw, external ut, or end of df not corresponding to 
 	 * a df included in the text unit. Tuv elements are segments.
 	 */
 	// Returns true if it is a text unit we need to return now
 	private boolean processTextUnit (String startTag) {
 		try {
 			// Send any previous tag as document part
 			createDocumentPartIfNeeded();
 
 			// Initialize variable for this text unit
 			boolean inTarget = false;
 			tu = new TextUnit(null); // No id yet
 			TextContainer srcCont = tu.getSource();
 			ArrayList<TextFragment> trgFragments = new ArrayList<TextFragment>();
 			ArrayList<AltTranslation> altTranslations = new ArrayList<AltTranslation>();
 			TextFragment srcSegFrag = null;
 			TextFragment trgSegFrag = null;
 			AltTranslation altTrans = null;
 			TextFragment inter = new TextFragment();
 			TextFragment current = inter;
 			boolean returnValueAfterTextUnitDone = true;
 
 			String tmp;
 			String name;
 			boolean moveToNext = false;
 			int dfCount = 0;
 			boolean changeFirst = false;
 			boolean done = false;
 			boolean inTU = false;
 			
 			while ( !done ) {
 				// Move to next event if required 
 				if ( moveToNext ) reader.next();
 				else moveToNext = true;
 				
 				// Process the event
 				switch ( reader.getEventType() ) {
 				
 				case XMLStreamConstants.CHARACTERS:
 				case XMLStreamConstants.CDATA:
 				case XMLStreamConstants.SPACE:
 					current.append(reader.getText());
 					break;
 					
 				case XMLStreamConstants.START_ELEMENT:
 					name = reader.getLocalName();
 					if ( !inTU && name.equals("ut") ) {
 						if ( !isInline(name) ) { // Non-inline ut
 							done = true;
 							returnValueAfterTextUnitDone = false;
 							continue;
 						}
 					}
 					else if ( name.equals("Tu") ) { // New segment
 						// Start new segment
 						inTU = true;
 						inTarget = false;
 						srcSegFrag = new TextFragment();
 						trgSegFrag = null;
 						altTrans = null;
 						if ( !inter.isEmpty() ) {
 							changeFirst = srcCont.isEmpty();
 							srcCont.append(inter);
 							inter = null;
 						}
 						current = srcSegFrag;
 						// Get Tu info
 						tmp = reader.getAttributeValue(null, MATCHPERCENT);
 						String origin = reader.getAttributeValue(null, ORIGIN);
 						if (( tmp != null ) || ( origin != null )) {
 							int value = 0;
 							if ( tmp != null ) {
 								try {
 									value = Integer.valueOf(tmp);
 								}
 								catch ( Throwable e ) {
 									logger.warning(String.format("Unexpected value in %s attribute (%s)", MATCHPERCENT, tmp));
 								}
 							}
 							if ( value > 0 ) {
 								altTrans = new AltTranslation(srcLoc, trgLoc, null, null, null, MatchType.UKNOWN,
 									value, ((origin==null) ? AltTranslation.ORIGIN_SOURCEDOC : origin));
 							}
 						}
 						continue;
 					}
 					else if ( name.equals("Tuv") ) { // New language content
 						tmp = reader.getAttributeValue(null, "Lang");
 						if ( tmp != null ) {
 							inTarget = trgLoc.equals(tmp);
 						}
 						else { // Just in case we don't have Lang
 							logger.warning(String.format("Attribute Lang is missing in Tuv (after text unit '%d'", tuId));
 							inTarget = !inTarget;
 						}
 						if ( inTarget ) {
 							// Get start on target
 							trgSegFrag = new TextFragment();
 							current = trgSegFrag;
 						}
 						// Else: source is already set
 						continue;
 					}
 					else if ( name.equals("df") ) {
 						// We have to use placeholder for df because they don't match ut nesting order
 						dfCount++;
 						Code code = current.append(TagType.PLACEHOLDER, "x-df-s", "", -1);
 						code.setOuterData(buildStartElement(false));
 						continue;
 					}
 					// Inline to include in this segment
 					TagType tagType = TagType.PLACEHOLDER;
 					String type = "ph";
 					int idToUse = -1;
 					tmp = reader.getAttributeValue(null, "Type");
 					if ( tmp != null ) {
 						if ( tmp.equals("start") ) {
 							tagType = TagType.OPENING;
 							type = "Xpt";
 						}
 						else if ( tmp.equals("end") ) {
 							tagType = TagType.CLOSING;
 							type = "Xpt";
 							idToUse = -1;
 						}
 					}
 					appendCode(tagType, idToUse, name, type, false, current);
 					break;
 
 				case XMLStreamConstants.END_ELEMENT:
 					name = reader.getLocalName();
 					if ( name.equals("Raw") ) { // End of document
 						done = true;
 					}
 					else if ( name.equals("df") ) {
 						// We have to use placeholder for df because they don't match ut nesting order
 						dfCount--;
 						Code code = current.append(TagType.PLACEHOLDER, "x-df-e", "", -1); //(inTarget ? ++trgId : ++srcId));
 						code.setOuterData(buildEndElement(false));
 						continue;
 					}
 					// Possible end of segment
 					if ( done || name.equals("Tu") ) {
 						if ( srcSegFrag != null ) { // Add the segment if we have one
 							srcCont.getSegments().append(srcSegFrag);
 							// Change first part to non-segment if needed
 							if ( changeFirst ) {
 								srcCont.changePart(0);
 								changeFirst = false;
 							}
 							// If the target is not there, we copy the source instead
 							// TTX should not have source-only TU
 							trgFragments.add((trgSegFrag==null) ? srcSegFrag.clone() : trgSegFrag);
 							// Set the alt-trans target if we had an alt-trans
 							if ( altTrans != null ) {
 								// Use target or source if target is not available (rare case)
 								// Falling back to source is not great but it's how the TTX file is, and this allows to preserve score, etc.
 								altTrans.setTarget(trgLoc, trgFragments.get(trgFragments.size()-1));
 							}
 							altTranslations.add(altTrans);
 							srcSegFrag = null;
 							trgSegFrag = null;
 							altTrans = null;
 							inter = new TextFragment();
 							current = inter; // Start storing inter-segment part
 							// A Tu stops the current segment, but not the text unit
 						}
 						else if (( inter != null ) && !inter.isEmpty() ) { // If no source segment: only content
 							srcCont.append(current);
 							srcSegFrag = null;
 							trgSegFrag = null;
 							inter = new TextFragment();
 							current = inter; // Start storing inter-segment part
 						}
 						inTU = false;
 						continue; // Stop here
 					}
 					break;
 				}
 			}
 
 			// Check if we had only non-segmented text
 			if (( inter != null) && !inter.isEmpty() ) {
 				srcCont.append(inter);
 			}
 			
 			// Check if this it is worth sending as text unit
 			if ( !hasText(srcCont) ) { // Use special hasText()
 				// No text-type characters
 				if ( skelWriter == null ) {
 					skelWriter = new TTXSkeletonWriter(params.getForceSegments());
 				}
 				skelWriter.checkForFilterInternalUse(lineBreak);
 				// Not really a text unit: convert to skeleton
 				// Use the skeleton writer processFragment() to get the output
 				// so any outer data is generated.
 				if ( srcCont.contentIsOneSegment() ) {
 					skel.append(skelWriter.processFragment(srcCont.getFirstContent()));
 				}
 				else { // Merge all if there is more than one segment
 					skel.append(skelWriter.processFragment(srcCont.getUnSegmentedContentCopy()));
 				}
 				tu = null;
 				return false; // No return from filter
 			}
 			
 			// Else genuine text unit, finalize and send
 			String movedCodes = "";
 			if ( srcCont.hasBeenSegmented() ) {
 				TextContainer cont = srcCont.clone();
 				int i = 0;
 				for ( Segment seg : cont.getSegments() ) {
 					seg.text = trgFragments.get(i);
 					AltTranslation altTmp = altTranslations.get(i);
 					if ( altTmp != null ) {
 						AltTranslationsAnnotation ann = new AltTranslationsAnnotation();
 						ann.add(altTmp);
 						seg.setAnnotation(ann);
 					}
 					i++;
 				}
 				tu.setTarget(trgLoc, cont);
 			}
 			else { // We assume pre-segmented entry don't have the overlapping DF problem.
 				// If they are un-segmented they may, and we try to fix it here:
 				if ( dfCount < 0 ) { // Extra </df> in content
 					movedCodes = moveDFCodesToString(srcCont, dfCount);
 				}
 			}
 			
 			tu.setId(String.valueOf(++tuId));
 			skel.addContentPlaceholder(tu); // Used by the TTXFilterWriter
 			tu.setSkeleton(skel);
 			tu.setPreserveWhitespaces(true);
 			tu.setMimeType(MimeTypeMapper.TTX_MIME_TYPE);
 			queue.add(new Event(EventType.TEXT_UNIT, tu));
 			skel = new GenericSkeleton();
 			if ( !Util.isEmpty(movedCodes) ) {
 				skel.add(movedCodes);
 			}
 			return returnValueAfterTextUnitDone;
 		}
 //		catch ( IndexOutOfBoundsException e ) {
 //			throw new OkapiIOException("Out of bounds.", e);
 //		}
 		catch ( XMLStreamException e) {
 			throw new OkapiIOException("Error processing top-level ut element.", e);
 		}
 	}
 	
 	/**
 	 * Moves the trailing DF codes to a string until dfCount is 0.
 	 * The container must not be segmented yet.
 	 * @param tc the container to update.
 	 * @param dfCount the df counter.
 	 * @return the string with removed codes.
 	 */
 	private String moveDFCodesToString (TextContainer tc, int dfCount) {
 		if ( dfCount >= 0 ) return "";
 		String tmp = "";
 		TextFragment tf = tc.get(0).text; 
 		StringBuilder ctext = new StringBuilder(tf.getCodedText());
 		List<Code> codes = tc.getFirstContent().getCodes();
 		
 		for ( int i=ctext.length()-1; i>=0; i-- ) {
 			if ( TextFragment.isMarker(ctext.charAt(i)) ) {
 				Code code = codes.get(TextFragment.toIndex(ctext.charAt(i+1)));
 				if (( code.getType() != null ) && code.getType().equals("x-df-e") ) {
 					// Copy the code data at the front of the skeleton string
 					tmp = code.getOuterData() + tmp;
 					// Remove the code from the fragment
 					tf.remove(i, i+2);
 					// Get the coded text again 
 					ctext.setLength(0);
 					ctext.append(tf.getCodedText());
 					if ( ++dfCount == 0 ) break;
 				}
 			}
 		}
 		return tmp;
 	}
 	
 	private boolean hasText (TextContainer tc) {
 		for ( TextPart part : tc ) {
 			String text = part.getContent().getCodedText();
 			for ( int i=0; i<text.length(); i++ ) {
 				if ( TextFragment.isMarker(text.charAt(i)) ) {
 					i++; // Skip index
 					continue;
 				}
 				// Not a marker: test the type of character
 				if ( !Character.isWhitespace(text.charAt(i)) ) {
 					// Extra TTX-no-text specific checks
 					if ( TTXNOTEXTCHARS.indexOf(text.charAt(i)) == -1 ) {
 						// Not a non-white-space that is not a TTX-no-text: that's text
 						return true;
 					}
 				}
 			}
 		}
 		return false;
 	}
 
 	private String buildStartElement (boolean store) {
 		StringBuilder tmp = new StringBuilder();
 		String prefix = reader.getPrefix();
 		if (( prefix == null ) || ( prefix.length()==0 )) {
 			tmp.append("<"+reader.getLocalName());
 		}
 		else {
 			tmp.append("<"+prefix+":"+reader.getLocalName());
 		}
 
 		int count = reader.getNamespaceCount();
 		for ( int i=0; i<count; i++ ) {
 			prefix = reader.getNamespacePrefix(i);
 			tmp.append(String.format(" xmlns%s=\"%s\"",
 				((prefix!=null) ? ":"+prefix : ""),
 				reader.getNamespaceURI(i)));
 		}
 		String attrName;
 		
 		count = reader.getAttributeCount();
 		for ( int i=0; i<count; i++ ) {
 			if ( !reader.isAttributeSpecified(i) ) continue; // Skip defaults
 			prefix = reader.getAttributePrefix(i);
 			attrName = String.format("%s%s",
 				(((prefix==null)||(prefix.length()==0)) ? "" : prefix+":"),
 				reader.getAttributeLocalName(i));
 			// Test for target language place-holder
 			if ( TARGETLANGUAGE_ATTR.equals(attrName) ) {
 				tmp.append(" "+TARGETLANGUAGE_ATTR+"=\"");
 				skel.append(tmp.toString());
 				//TODO: replace direct write by property: skel.addValuePlaceholder(referent, TARGETLANGUAGE_ATTR, locId);
 				skel.append(trgLangCode);
 				tmp.setLength(0);
 				tmp.append("\"");
 			}
 			else {
 				tmp.append(String.format(" %s=\"%s\"", attrName,
 					Util.escapeToXML(reader.getAttributeValue(i).replace("\n", lineBreak), 3, true, null)));
 			}
 		}
 		tmp.append(">");
 		if ( store ) skel.append(tmp.toString());
 		return tmp.toString();
 	}
 	
 	private String buildEndElement (boolean store) {
 		StringBuilder tmp = new StringBuilder();
 		String prefix = reader.getPrefix();
 		if (( prefix != null ) && ( prefix.length()>0 )) {
 			tmp.append("</"+prefix+":"+reader.getLocalName()+">");
 		}
 		else {
 			tmp.append("</"+reader.getLocalName()+">");
 		}
 		if ( store ) skel.append(tmp.toString());
 		return tmp.toString();
 	}
 
 	private void processUserSettings () {
 		 if ( skelWriter == null ) {
 			 skelWriter = new TTXSkeletonWriter(params.getForceSegments());
 		 }
 		// Check source language
 		String tmp = reader.getAttributeValue(null, "SourceLanguage");
 		if ( !Util.isEmpty(tmp) ) {
 			 if ( !srcLoc.equals(tmp) ) {
 				 logger.warning(String.format("Specified source was '%s' but source language in the file is '%s'.\nUsing '%s'.",
 					srcLoc.toString(), tmp, tmp));
 				 srcLoc = LocaleId.fromString(tmp);
 				 srcLangCode = tmp;
 				 skelWriter.setSourceLanguageCode(srcLangCode);
 			 }
 		}
 
 		// Check target language
 		tmp = reader.getAttributeValue(null, TARGETLANGUAGE_ATTR);
 		if ( !Util.isEmpty(tmp) ) {
 			 if ( !trgLoc.equals(tmp) ) {
 				 logger.warning(String.format("Specified target was '%s' but target language in the file is '%s'.\nUsing '%s'.",
 					trgLoc.toString(), tmp, tmp));
 				 trgLoc = LocaleId.fromString(tmp);
 				 trgLangCode = tmp;
 				 skelWriter.setTargetLanguageCode(trgLangCode);
 			 }
 		}
 		if ( tmp != null ) {
 			//TODO: set property for TargetLanguage
 		}
 
 		trgDefFont = reader.getAttributeValue(null, "TargetDefaultFont");
 		if ( Util.isEmpty(trgDefFont) ) {
 			trgDefFont = "Arial"; // Default
 		}
 
 		buildStartElement(true);
 	}
 
 	// Case of a UT element outside a TUV, that is an un-segmented/translate code.
 //	private void processTopSpecialElement (String tagName) {
 //		try {
 //			boolean isInline = isInline(tagName);
 //			if ( isInline ) {
 //				// It's internal, and not in a TU/TUV yet
 //				processNewTU();
 //				// reader.next() has been called already 
 //			}
 //			else {
 //				if ( tagName.equals("ut") ) { // UT that should not be inline
 //					// Keep copying into the skeleton until end of element
 //					storeStartElement();
 //					storeUntilEndElement("ut"); // Includes the closing tag
 //				}
 //				else { // DF external
 //					storeStartElement();
 //					reader.next();
 //				}
 //			}
 //		}
 //		catch ( XMLStreamException e) {
 //			throw new OkapiIOException("Error processing top-level ut element.", e);
 //		}
 //	}
 	
 	private void storeUntilEndElement (String name) throws XMLStreamException {
 		int eventType;
 		while ( reader.hasNext() ) {
 			eventType = reader.next();
 			switch ( eventType ) {
 			case XMLStreamConstants.START_ELEMENT:
 				buildStartElement(true);
 				break;
 			case XMLStreamConstants.END_ELEMENT:
 				if ( name.equals(reader.getLocalName()) ) {
 					buildEndElement(true);
 					reader.next(); // Move forward
 					return;
 				}
 				// Else: just store the end
 				buildEndElement(true);
 				break;
 			case XMLStreamConstants.SPACE:
 			case XMLStreamConstants.CDATA:
 			case XMLStreamConstants.CHARACTERS:
 				//TODO: escape unsupported chars
 				skel.append(Util.escapeToXML(reader.getText().replace("\n", lineBreak), 0, true, null));
 				break;
 			case XMLStreamConstants.COMMENT:
 				//addTargetIfNeeded();
 				skel.append("<!--"+ reader.getText().replace("\n", lineBreak) + "-->");
 				break;
 			case XMLStreamConstants.PROCESSING_INSTRUCTION:
 				skel.append("<?"+ reader.getPITarget() + " " + reader.getPIData() + "?>");
 				break;
 			}
 		}
 	}
 
 	private boolean isInline (String tagName) {
 		if ( tagName.equals("df") ) {
 			return true;
 		}
 		String tmp = reader.getAttributeValue(null, "Style");
 		if ( tmp != null ) {
 			return  !"external".equals(tmp);
 		}
 		else {
 			// If no Style attribute: check for Class as some are indicator of external type.
 			tmp = reader.getAttributeValue(null, "Class");
 			if ( tmp != null ) {
 				return !"procinstr".equals(tmp);
 			}
 		}
 		return true; // Default is internal
 	}
 
 	private void createDocumentPartIfNeeded () {
 		// Make a document part with skeleton between the previous event and now.
 		// Spaces can go with Tu to reduce the number of events.
 		// This allows to have only the Tu skeleton parts with the TextUnit event
 		if ( !skel.isEmpty(true) ) {
 			DocumentPart dp = new DocumentPart(otherId.createId(), false, skel);
 			queue.add(new Event(EventType.DOCUMENT_PART, dp));
 			skel = new GenericSkeleton(); // And create a new skeleton for the next event
 		}
 	}
 	
 	/**
 	 * Appends a code, using the content of the node. Do not use for <g>-type tags.
 	 * @param tagType The type of in-line code.
 	 * @param id the id of the code to add.
 	 * @param tagName the tag name of the in-line element to process.
 	 * @param type the type of code (bpt and ept must use the same one so they can match!) 
 	 * @param store true if we need to store the data in the skeleton.
 	 */
 	private void appendCode (TagType tagType,
 		int id,
 		String tagName,
 		String type,
 		boolean store,
 		TextFragment content)
 	{
 		try {
 			int endStack = 1;
 			StringBuilder innerCode = new StringBuilder();
 			StringBuilder outerCode = null;
 			outerCode = new StringBuilder();
 			outerCode.append("<"+tagName);
 			int count = reader.getAttributeCount();
 			String prefix;
 			for ( int i=0; i<count; i++ ) {
 				if ( !reader.isAttributeSpecified(i) ) continue; // Skip defaults
 				prefix = reader.getAttributePrefix(i); 
 				outerCode.append(String.format(" %s%s=\"%s\"",
 					(((prefix==null)||(prefix.length()==0)) ? "" : prefix+":"),
 					reader.getAttributeLocalName(i),
 					Util.escapeToXML(reader.getAttributeValue(i).replace("\n", lineBreak), 3, true, null)));
 			}
 			outerCode.append(">");
 			
 			int eventType;
 			while ( reader.hasNext() ) {
 				eventType = reader.next();
 				switch ( eventType ) {
 				case XMLStreamConstants.START_ELEMENT:
 					if ( store ) buildStartElement(store);
 					StringBuilder tmpg = new StringBuilder();
 					if ( tagName.equals(reader.getLocalName()) ) {
 						endStack++; // Take embedded elements into account 
 					}
 					prefix = reader.getPrefix();
 					if (( prefix == null ) || ( prefix.length()==0 )) {
 						tmpg.append("<"+reader.getLocalName());
 					}
 					else {
 						tmpg.append("<"+prefix+":"+reader.getLocalName());
 					}
 					count = reader.getNamespaceCount();
 					for ( int i=0; i<count; i++ ) {
 						prefix = reader.getNamespacePrefix(i);
						tmpg.append(String.format(" xmlns%s=\"%s\"",
 							((prefix!=null) ? ":"+prefix : ""),
 							reader.getNamespaceURI(i)));
 					}
 					count = reader.getAttributeCount();
 					for ( int i=0; i<count; i++ ) {
 						if ( !reader.isAttributeSpecified(i) ) continue; // Skip defaults
 						prefix = reader.getAttributePrefix(i); 
 						tmpg.append(String.format(" %s%s=\"%s\"",
 							(((prefix==null)||(prefix.length()==0)) ? "" : prefix+":"),
 							reader.getAttributeLocalName(i),
 							Util.escapeToXML(reader.getAttributeValue(i).replace("\n", lineBreak), 3, true, null)));
 					}
 					tmpg.append(">");
 					innerCode.append(tmpg.toString());
 					outerCode.append(tmpg.toString());
 					break;
 					
 				case XMLStreamConstants.END_ELEMENT:
 					if ( store ) buildEndElement(store);
 					if ( tagName.equals(reader.getLocalName()) ) {
 						if ( --endStack == 0 ) {
 							Code code = content.append(tagType, type, innerCode.toString(), id);
 							outerCode.append("</"+tagName+">");
 							code.setOuterData(outerCode.toString());
 							return;
 						}
 						// Else: fall thru
 					}
 					// Else store the close tag in the outer code
 					prefix = reader.getPrefix();
 					if (( prefix == null ) || ( prefix.length()==0 )) {
 						innerCode.append("</"+reader.getLocalName()+">");
 						outerCode.append("</"+reader.getLocalName()+">");
 					}
 					else {
 						innerCode.append("</"+prefix+":"+reader.getLocalName()+">");
 						outerCode.append("</"+prefix+":"+reader.getLocalName()+">");
 					}
 					break;
 
 				case XMLStreamConstants.CHARACTERS:
 				case XMLStreamConstants.CDATA:
 				case XMLStreamConstants.SPACE:
 					innerCode.append(reader.getText());//TODO: escape unsupported chars
 					outerCode.append(Util.escapeToXML(reader.getText(), 0, true, null));
 					if ( store ) //TODO: escape unsupported chars
 						skel.append(Util.escapeToXML(reader.getText(), 0, true, null));
 					break;
 				}
 			}
 		}
 		catch ( XMLStreamException e) {
 			throw new OkapiIOException(e);
 		}
 	}
 	
 }
