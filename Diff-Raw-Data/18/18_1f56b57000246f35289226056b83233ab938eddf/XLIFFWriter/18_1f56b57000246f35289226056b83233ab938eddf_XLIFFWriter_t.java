 /*===========================================================================
   Copyright (C) 2009-2012 by the Okapi Framework contributors
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
 
 package net.sf.okapi.common.filterwriter;
 
 import java.io.OutputStream;
 
 import net.sf.okapi.common.Event;
 import net.sf.okapi.common.IParameters;
 import net.sf.okapi.common.Util;
 import net.sf.okapi.common.XMLWriter;
 import net.sf.okapi.common.annotation.AltTranslation;
 import net.sf.okapi.common.annotation.AltTranslationsAnnotation;
 import net.sf.okapi.common.annotation.TermsAnnotation;
 import net.sf.okapi.common.encoder.EncoderManager;
 import net.sf.okapi.common.exceptions.OkapiUnsupportedEncodingException;
 import net.sf.okapi.common.filterwriter.XLIFFContent;
 import net.sf.okapi.common.LocaleId;
 import net.sf.okapi.common.query.MatchType;
 import net.sf.okapi.common.resource.Ending;
 import net.sf.okapi.common.resource.Property;
 import net.sf.okapi.common.resource.Segment;
 import net.sf.okapi.common.resource.StartDocument;
 import net.sf.okapi.common.resource.StartGroup;
 import net.sf.okapi.common.resource.StartSubDocument;
 import net.sf.okapi.common.resource.TextContainer;
 import net.sf.okapi.common.resource.ITextUnit;
 import net.sf.okapi.common.skeleton.ISkeletonWriter;
 
 /**
  * Writer for creating XLIFF document.
  */
 public class XLIFFWriter implements IFilterWriter {
 
 	/**
 	 * URI for the XLIFF 1.2 namespace.
 	 */
 	public static final String NS_XLIFF12 = "urn:oasis:names:tc:xliff:document:1.2";
 	/**
 	 * URI for the Okapi XLIFF extensions namespace.
 	 */
 	public static final String NS_XLIFFOKAPI = "okapi-framework:xliff-extensions";
 	/**
 	 * Name of the Okapi XLIFF extension matchType.
 	 */
 	public static final String OKP_MATCHTYPE = "matchType";
 	
 	public static final String NS_ITS20 = "http://www.w3.org/2005/11/its";
 	
 	private static final String RESTYPEVALUES = 
 		";auto3state;autocheckbox;autoradiobutton;bedit;bitmap;button;caption;cell;"
 		+ "checkbox;checkboxmenuitem;checkedlistbox;colorchooser;combobox;comboboxexitem;"
 		+ "comboboxitem;component;contextmenu;ctext;cursor;datetimepicker;defpushbutton;"
 		+ "dialog;dlginit;edit;file;filechooser;fn;font;footer;frame;grid;groupbox;"
 		+ "header;heading;hedit;hscrollbar;icon;iedit;keywords;label;linklabel;list;"
 		+ "listbox;listitem;ltext;menu;menubar;menuitem;menuseparator;message;monthcalendar;"
 		+ "numericupdown;panel;popupmenu;pushbox;pushbutton;radio;radiobuttonmenuitem;rcdata;"
 		+ "row;rtext;scrollpane;separator;shortcut;spinner;splitter;state3;statusbar;string;"
 		+ "tabcontrol;table;textbox;togglebutton;toolbar;tooltip;trackbar;tree;uri;userbutton;"
 		+ "usercontrol;var;versioninfo;vscrollbar;window;";
 
 	private XMLWriter writer;
 	private XLIFFContent xliffCont;
 	private String skeletonPath;
 	
 	private LocaleId srcLoc;
 	private LocaleId trgLoc;
 	private String dataType;
 	private String original;
 	
 	private String fwOutputPath; // Used in IFilterWriter mode
 	private String fwConfigId; // Used in IFilterWriter mode
 	private String fwInputEncoding; // Used in IFilterWriter mode
 
 	private boolean inFile;
 	private boolean hasFile;
 	private boolean useSourceForTranslated = false;
 
 	private boolean escapeGt = false;
 	private boolean placeholderMode = true;
 	
 	private boolean includeNoTranslate = true;
 	private boolean setApprovedAsNoTranslate = false;
 	private boolean copySource = true;
 	private boolean includeAltTrans = true;
 	
 	/**
 	 * Creates an XLIFF writer object.
 	 */
 	public XLIFFWriter () {
 		xliffCont = new XLIFFContent();
 	}
 	
 	/**
 	 * Sets the copySource flag indicating to copy the source at the target spot if there is no target defined.
 	 * @param copySource true to copy the source at the target spot if there is no target defined.
 	 */
 	public void setCopySource (boolean copySource) {
 		this.copySource = copySource;
 	}
 	
 	/**
 	 * Sets the flag indicating if the inline code should use the place-holder notation (g and x elements).
 	 * @param placeholderMode true if the inline code should use the place-holder notation.
 	 */
 	public void setPlaceholderMode (boolean placeholderMode) {
 		this.placeholderMode = placeholderMode;
 	}
 	
 	/**
 	 * Sets the flag indicating if '>' should be escaped or not.
 	 * @param escapeGt true to always escape '>', false to not escape it.
 	 */
 	public void setEscapeGt (boolean escapeGt) {
 		this.escapeGt = escapeGt;
 	}
 	
 	/**
 	 * Sets the flag indicating if the source text is used in the target, even if 
 	 * a target is available.
 	 * <p>This is for the tools where we trust the target will be obtained by the tool
 	 * from the TMX from original. This is to allow editing of pre-translated items in XLIFF 
 	 * editors that use directly the <target> element.
 	 * @param useSourceForTranslated true to use the source in the target even if a target text
 	 * is available.
 	 */
 	public void setUseSourceForTranslated (boolean useSourceForTranslated) {
 		this.useSourceForTranslated = useSourceForTranslated;
 	}
 	
 	/**
 	 * Sets the flag indicating if non-translatable text units should be output or not.
 	 * @param includeNoTranslate true to include non-translatable text unit in the output.
 	 */
 	public void setIncludeNoTranslate (boolean includeNoTranslate) {
 		this.includeNoTranslate = includeNoTranslate;
 	}
 	
 	/**
 	 * Sets the flag indicating if alt-trans elements should be output or not.
 	 * @param includeAltTrans true to include alt-trans element in the output.
 	 */
 	public void setIncludeAltTrans (boolean includeAltTrans) {
 		this.includeAltTrans = includeAltTrans;
 	}
 	
 	/**
 	 * Sets the flag indicating to mark as not translatable all entries that are approved.
 	 * @param setApprovedasNoTranslate true to mark approved entries as not translatable.
 	 */
 	public void setSetApprovedAsNoTranslate (boolean setApprovedasNoTranslate) {
 		this.setApprovedAsNoTranslate = setApprovedasNoTranslate;
 	}
 
 	/**
 	 * Creates a new XLIFF document.
 	 * @param xliffPath the full path of the document to create.
 	 * @param skeletonPath the path for the skeleton, or null for no skeleton.
 	 * @param srcLoc the source locale.
 	 * @param trgLoc the target locale, or null for no target.
 	 * @param dataType the value for the <code>datatype</code> attribute.
 	 * @param original the value for the <code>original</code> attribute.
 	 * @param message optional comment to put at the top of the document (can be null).
 	 */
 	public void create (String xliffPath,
 		String skeletonPath,
 		LocaleId srcLoc,
 		LocaleId trgLoc,
 		String dataType,
 		String original,
 		String message)
 	{
 		if ( writer != null ) {
 			close();
 		}
 		this.skeletonPath = skeletonPath;
 		this.original = original;
 		this.srcLoc = srcLoc;
 		this.trgLoc = trgLoc;
 		this.dataType = dataType;
 		
 		writer = new XMLWriter(xliffPath);
 		writer.writeStartDocument();
 		writer.writeStartElement("xliff");
 		writer.writeAttributeString("version", "1.2");
 		writer.writeAttributeString("xmlns", NS_XLIFF12);
 		writer.writeAttributeString("xmlns:okp", NS_XLIFFOKAPI); 
 
 		if ( !Util.isEmpty(message) ) {
 			writer.writeLineBreak();
 			writer.writeComment(message, false);
 		}
 		writer.writeLineBreak();
 	}
 
 	/**
 	 * Writes the end of this the document and close it.
 	 * If a &lt;file> element is currently opened, it is closed automatically. 
 	 */
 	public void close () {
 		if ( writer != null ) {
 			if ( !hasFile ) {
 				writeStartFile(original, dataType, skeletonPath, fwConfigId, fwInputEncoding, null);
 			}
 			if ( inFile ) {
 				writeEndFile();
 			}
 			writer.writeEndElementLineBreak(); // xliff
 			writer.writeEndDocument();
 			writer.close();
 			writer = null;
 		}
 		fwConfigId = null;
 		fwInputEncoding = null;
 		skeletonPath = null;
 	}
 
 	/**
 	 * Writes the start of a &lt;file> element.
 	 * <p>each call to this method must have a corresponding call to {@link #writeEndFile()}.
 	 * @param original the value for the <code>original</code> attribute. If null: "unknown" is used.
 	 * @param dataType the value for the <code>datatype</code> attribute. If null: "x-undefined" is used. 
 	 * @param skeletonPath optional external skeleton information, or null.
 	 * @see #writeEndFile()
 	 */
 	public void writeStartFile (String original,
 		String dataType,
 		String skeletonPath)
 	{
 		writeStartFile(original, dataType, skeletonPath, null, null, null);
 	}
 	
 	/**
 	 * Writes the start of a &lt;file> element.
 	 * <p>each call to this method must have a corresponding call to {@link #writeEndFile()}.
 	 * @param original the value for the <code>original</code> attribute. If null: "unknown" is used.
 	 * @param dataType the value for the <code>datatype</code> attribute. If null: "x-undefined" is used. 
 	 * @param skeletonPath optional external skeleton information, or null.
 	 * @param extraForHeader optional extra raw valid XLIFF to place in the header, or null.
 	 * @see #writeEndFile()
 	 */
 	public void writeStartFile (String original,
 		String dataType,
 		String skeletonPath,
 		String extraForHeader)
 	{
 		writeStartFile(original, dataType, skeletonPath, null, null, extraForHeader);
 	}
 	
 	/**
 	 * Internal method to write the start of a &lt;file> element.
 	 * <p>each call to this method must have a corresponding call to {@link #writeEndFile()}.
 	 * @param original the value for the <code>original</code> attribute. If null: "unknown" is used.
 	 * @param dataType the value ofr the <code>datatype</code> attribute. If null: "x-undefined" is used. 
 	 * @param skeletonPath external skeleton information (can be null).
 	 * @param configId the optional filter configuration id used to extract the original (IFilterWriter mode), or null.
 	 * @param inputEncoding the optional encoding of the input file (IFilterWriter mode), or null.
 	 * @param extraForHeader optional extra raw valid XLIFF to place in the header, or null.
 	 * @see #writeEndFile()
 	 */
 	private void writeStartFile (String original,
 		String dataType,
 		String skeletonPath,
 		String configId,
 		String inputEncoding,
 		String extraForHeader)
 	{
 		writer.writeStartElement("file");
 		writer.writeAttributeString("original",
 			(original!=null) ? original : "unknown");
 		writer.writeAttributeString("source-language", srcLoc.toBCP47());
 		if ( trgLoc != null ) {
 			writer.writeAttributeString("target-language", trgLoc.toBCP47());
 		}
 		
 		if ( dataType == null ) dataType = "x-undefined";
 		else if ( dataType.equals("text/html") ) dataType = "html";
 		else if ( dataType.equals("text/xml") ) dataType = "xml";
 		else if ( !dataType.startsWith("x-") ) {
 			dataType = "x-"+dataType;
 		}
 		writer.writeAttributeString("datatype", dataType);
 
 		if ( !Util.isEmpty(inputEncoding) ) {
 			writer.writeAttributeString("okp:inputEncoding", inputEncoding);
 		}
 		if ( !Util.isEmpty(configId) ) {
 			writer.writeAttributeString("okp:configId", configId);
 		}
 		writer.writeLineBreak();
 		
 		// Write out external skeleton info if available 
 		if ( !Util.isEmpty(skeletonPath) || !Util.isEmpty(extraForHeader) ) {
 			writer.writeStartElement("header");
 			if ( !Util.isEmpty(skeletonPath) ) {
 				writer.writeStartElement("skl");
 				writer.writeStartElement("external-file");
 				writer.writeAttributeString("href", skeletonPath);
 				writer.writeEndElement(); // external-file
 				writer.writeEndElement(); // skl
 			}
 			if ( !Util.isEmpty(extraForHeader) ) {
 				writer.writeRawXML(extraForHeader);
 			}
 			writer.writeEndElementLineBreak(); // header
 		}
 
 		inFile = hasFile = true;
 		writer.writeStartElement("body");
 		writer.writeLineBreak();
 	}
 	
 	
 	/**
 	 * Writes the end of a &lt;file> element.
 	 * This method should be called for each call to {@link #writeStartFile(String, String, String)}.
 	 * @see #writeStartFile(String, String, String)
 	 */
 	public void writeEndFile () {
 		writer.writeEndElementLineBreak(); // body
 		writer.writeEndElementLineBreak(); // file
 		inFile = false;
 	}
 	
 	/**
 	 * Writes the start of a &lt;group> element.
 	 * @param id the value for the <code>id</code> attribute of the group (must not be null).
 	 * @param resName the value for the <code>resname</code> attribute of the group (can be null).
 	 * @param resType the value for the <code>restype</code> attribute of the group (can be null).
 	 * @see #writeEndGroup()
 	 */
 	public void writeStartGroup (String id,
 		String resName,
 		String resType)
 	{
 		if ( !inFile ) {
 			writeStartFile(original, dataType, skeletonPath, fwConfigId, fwInputEncoding, null);
 		}
 		writer.writeStartElement("group");
 		writer.writeAttributeString("id", id);
 		if ( !Util.isEmpty(resName) ) {
 			writer.writeAttributeString("resname", resName);
 		}
 		if ( !Util.isEmpty(resType) ) {
 			if ( resType.startsWith("x-") || ( RESTYPEVALUES.contains(";"+resType+";")) ) {
 				writer.writeAttributeString("restype", resType);
 			}
 			else { // Make sure the value is valid
 				writer.writeAttributeString("restype", "x-"+resType);
 			}
 		}
 		writer.writeLineBreak();
 	}
 	
 	/**
 	 * Writes the end of a &lt;group> element.
 	 * @see #writeStartGroup(String, String, String).
 	 */
 	public void writeEndGroup () {
 		writer.writeEndElementLineBreak(); // group
 	}
 	
 	/**
 	 * Writes a text unit as a &lt;trans-unit> element.
 	 * @param tu the text unit to output.
 	 */
 	public void writeTextUnit (ITextUnit tu) {
 		writeTextUnit(tu, null);
 	}
 	
 	/**
 	 * Writes a text unit as a &lt;trans-unit> element.
 	 * @param tu the text unit to output.
 	 */
 	public void writeTextUnit (ITextUnit tu,
 		String phaseName)
 	{
 		// Avoid writing out some entries in non-IFilterWriter mode
 		if ( fwConfigId == null ) {
 			// Check if we need to set the entry as non-translatable
 			if ( setApprovedAsNoTranslate ) {
 				Property prop = tu.getTargetProperty(trgLoc, Property.APPROVED);
 				if (( prop != null ) && prop.getValue().equals("yes") ) {
 					tu.setIsTranslatable(false);
 				}
 			}
 			// Check if we need to skip non-translatable entries
 			if ( !tu.isTranslatable() && !includeNoTranslate ) {
 				return;
 			}
 		}
 
 		if ( !inFile ) {
 			writeStartFile(original, dataType, skeletonPath, fwConfigId, fwInputEncoding, null);
 		}
 
 		writer.writeStartElement("trans-unit");
 		writer.writeAttributeString("id", tu.getId());
 		String tmp = tu.getName();
 		if ( !Util.isEmpty(tmp) ) {
 			writer.writeAttributeString("resname", tmp);
 		}
 		tmp = tu.getType();
 		if ( !Util.isEmpty(tmp) ) {
 			if ( tmp.startsWith("x-") || ( RESTYPEVALUES.contains(";"+tmp+";")) ) {
 				writer.writeAttributeString("restype", tmp);
 			}
 			else { // Make sure the value is valid
 				writer.writeAttributeString("restype", "x-"+tmp);
 			}
 		}
 		if ( !tu.isTranslatable() ) {
 			writer.writeAttributeString("translate", "no");
 		}
 		if ( phaseName != null ) {
 			writer.writeAttributeString("phase-name", phaseName);
 		}
 
 		if ( trgLoc != null ) {
 			if ( tu.hasTargetProperty(trgLoc, Property.APPROVED) ) {
 				if ( tu.getTargetProperty(trgLoc, Property.APPROVED).getValue().equals("yes") ) {
 					writer.writeAttributeString(Property.APPROVED, "yes");
 				}
 				// "no" is the default
 			}
 		}
 		
 		if ( tu.preserveWhitespaces() ) {
 			writer.writeAttributeString("xml:space", "preserve");
 		}
 		
 		// TU-level ITS properties (as attribute)
 		if ( tu.hasProperty(Property.ITS_EXTERNALRESREF) ) {
 			writer.writeAttributeString("xmlns:okp", NS_XLIFFOKAPI);
			writer.writeAttributeString("okp:itsExternalResourceRef",
 				tu.getProperty(Property.ITS_EXTERNALRESREF).getValue());
 		}
 		
 		writer.writeLineBreak();
 
 		// Get the source container
 		TextContainer tc = tu.getSource();
 		boolean srcHasText = tc.hasText(false);
 
 		//--- Write the source
 		
 		writer.writeStartElement("source");
 		writer.writeAttributeString("xml:lang", srcLoc.toBCP47());
 		// Write full source content (always without segments markers
 		writer.writeRawXML(xliffCont.toSegmentedString(tc, 0, escapeGt, false, placeholderMode));
 		writer.writeEndElementLineBreak(); // source
 		// Write segmented source (with markers) if needed
 		if ( tc.hasBeenSegmented() ) {
 			writer.writeStartElement("seg-source");
 			writer.writeRawXML(xliffCont.toSegmentedString(tc, 0, escapeGt, true, placeholderMode));
 			writer.writeEndElementLineBreak(); // seg-source
 		}
 
 		//--- Write the target
 		
 		if ( trgLoc != null ) {
 			// At this point tc contains the source
 			// Do we have an available target to use instead?
 			tc = tu.getTarget(trgLoc);
 			boolean outputTarget = true;
 			if ( useSourceForTranslated || ( tc == null ) || ( tc.isEmpty() )
 				|| ( srcHasText && !tc.hasText(false) )) {
 				tc = tu.getSource(); // Fall back to source
 				// Output copy of source only if requested
 				outputTarget = copySource;
 			}
 
 			if ( outputTarget ) {
 				writer.writeStartElement("target");
 				writer.writeAttributeString("xml:lang", trgLoc.toBCP47());
 				// Now tc hold the content to write. Write it with or without marks
 				writer.writeRawXML(xliffCont.toSegmentedString(tc, 0, escapeGt, tc.hasBeenSegmented(), placeholderMode));
 				writer.writeEndElementLineBreak(); // target
 			}
 
 			// Possible alternate translations
 			if ( includeAltTrans ) {
 				// We re-get the target because tc could be coming from the source
 				TextContainer altCont = tu.getTarget(trgLoc);
 				if ( altCont != null ) {
 					// From the target container
 					writeAltTranslations(altCont.getAnnotation(AltTranslationsAnnotation.class), null);
 					// From the segments
 					for ( Segment seg : altCont.getSegments() ) {
 						writeAltTranslations(seg.getAnnotation(AltTranslationsAnnotation.class), seg);
 					}
 				}
 			}
 		}
 		
 		// Notes
 		if ( tu.hasProperty(Property.NOTE) ) {
 			writer.writeStartElement("note");
 			writer.writeString(tu.getProperty(Property.NOTE).getValue());
 			writer.writeEndElementLineBreak(); // note
 		}
 		if ( tu.hasProperty(Property.TRANSNOTE) ) {
 			writer.writeStartElement("note");
 			writer.writeAttributeString("from", "translator");
 			writer.writeString(tu.getProperty(Property.TRANSNOTE).getValue());
 			writer.writeEndElementLineBreak(); // note
 		}
 		
 		// Temporary output for terms annotation
 		//TODO: replace this by extended element linked with <mrk>
 		TermsAnnotation ann = tu.getSource().getAnnotation(TermsAnnotation.class);
 		if (( ann != null ) && ( ann.size() > 0 )) {
 			writer.writeStartElement("note");
 			writer.writeAttributeString("annotates", "source");
 			writer.writeString("Terms:\n"+ann.toString());
 			writer.writeEndElementLineBreak(); // note
 		}
 
 		// ITS properties (as element)
 		if ( tu.hasProperty(Property.ITS_DOMAINS) ) {
 			writer.writeStartElement("okp:itsDomains");
 			writer.writeAttributeString("xmlns:dc", "http://purl.org/dc/elements/1.1/");
 			for ( String value : tu.getProperty(Property.ITS_DOMAINS).getValue().split("\t", 0) ) {
 				writer.writeStartElement("okp:item");
 				writer.writeAttributeString("dc:subject", value);
 				writer.writeEndElement();
 			}
 			writer.writeEndElement();
 		}
 
 		writer.writeEndElementLineBreak(); // trans-unit
 	}
 
 	/**
 	 * Writes possible alternate translations
 	 * @param ann the annotation with the alternate translations (can be null)
 	 * @param segment the segment where the annotation comes from, or null  if the
 	 * annotation comes from the container.
 	 */
 	private void writeAltTranslations (AltTranslationsAnnotation ann,
 		Segment segment)
 	{
 		if ( ann == null ) {
 			return;
 		}
 		for ( AltTranslation alt : ann ) {
 			writer.writeStartElement("alt-trans");
 			if ( segment != null ) {
 				writer.writeAttributeString("mid", segment.getId());
 			}
 			if ( alt.getCombinedScore() > 0 ) {
 				writer.writeAttributeString("match-quality", String.format("%d", alt.getCombinedScore()));
 			}
 			if ( !Util.isEmpty(alt.getOrigin()) ) {
 				writer.writeAttributeString("origin", alt.getOrigin());
 			}
 			if ( alt.getType() != MatchType.UKNOWN ) {
 				writer.writeAttributeString("okp:"+OKP_MATCHTYPE, alt.getType().toString());
 			}
 			TextContainer cont = alt.getSource();
 			if ( !cont.isEmpty() ) {
 				writer.writeStartElement("source");
 				writer.writeAttributeString("xml:lang", alt.getSourceLocale().toBCP47());
 				// Write full source content (always without segments markers
 				writer.writeRawXML(xliffCont.toSegmentedString(cont, 0, escapeGt, false, placeholderMode));
 				writer.writeEndElementLineBreak(); // source
 			}
 			writer.writeStartElement("target");
 			writer.writeAttributeString("xml:lang", alt.getTargetLocale().toBCP47());
 			writer.writeRawXML(xliffCont.toSegmentedString(alt.getTarget(), 0, escapeGt, false, placeholderMode));
 			writer.writeEndElementLineBreak(); // target
 			writer.writeEndElementLineBreak(); // alt-trans
 		}
 	}
 
 	@Override
 	public void cancel () {
 		// Nothing for now
 	}
 
 	@Override
 	public EncoderManager getEncoderManager () {
 		// None
 		return null;
 	}
 
 	@Override
 	public ISkeletonWriter getSkeletonWriter () {
 		return null;
 	}
 
 	@Override
 	public String getName () {
 		return getClass().getName();
 	}
 
 	@Override
 	public IParameters getParameters () {
 		// Not used for now
 		return null;
 	}
 
 	@Override
 	public Event handleEvent (Event event) {
 		switch ( event.getEventType() ) {
 		case START_DOCUMENT:
 			processStartDocument((StartDocument)event.getResource());
 			break;
 		case END_DOCUMENT:
 			processEndDocument();
 			break;
 		case START_SUBDOCUMENT:
 			processStartSubDocument((StartSubDocument)event.getResource());
 			break;
 		case END_SUBDOCUMENT:
 			processEndSubDocument((Ending)event.getResource());
 			break;
 		case START_GROUP:
 			processStartGroup((StartGroup)event.getResource());
 			break;
 		case END_GROUP:
 			processEndGroup((Ending)event.getResource());
 			break;
 		case TEXT_UNIT:
 			processTextUnit(event.getTextUnit());
 			break;
 		}
 		return event;
 	}
 
 	@Override
 	public void setOptions (LocaleId locale,
 		String defaultEncoding)
 	{
 		trgLoc = locale;
 		// Ignore encoding as we always use UTF-8
 	}
 
 	@Override
 	public void setOutput (String path) {
 		fwOutputPath = path;
 	}
 
 	@Override
 	public void setOutput (OutputStream output) {
 		// Not supported
 		throw new OkapiUnsupportedEncodingException("Stream-based output not supported.");
 		
 	}
 
 	@Override
 	public void setParameters (IParameters params) {
 		// Not used for now
 		// Options are set individually
 	}
 
 	// Use for IFilterWriter mode
 	private void processStartDocument (StartDocument resource) {
 		// trgLoc was set before
 		// fwOutputPath was set before
 		create(fwOutputPath, null, resource.getLocale(), trgLoc, resource.getMimeType(), resource.getName(), null);
 
 		// Additional variables specific to IFilterWriter mode
 		fwInputEncoding = resource.getEncoding();
 		IParameters params = resource.getFilterParameters();
 		if ( params == null ) fwConfigId = null;
 		else fwConfigId = params.getPath();
 	}
 
 	// Use for IFilterWriter mode
 	private void processEndDocument () {
 		// All is done in close() (used by both modes)
 		close();
 	}
 
 	// Use for IFilterWriter mode
 	private void processStartSubDocument (StartSubDocument resource) {
 		writeStartFile(resource.getName(), resource.getMimeType(), null,
 			fwConfigId, fwInputEncoding, null);		
 	}
 
 	// Use for IFilterWriter mode
 	private void processEndSubDocument (Ending resource) {
 		writeEndFile();
 	}
 
 	// Use for IFilterWriter mode
 	private void processStartGroup (StartGroup resource) {
 		writeStartGroup(resource.getId(), resource.getName(), resource.getType());
 	}
 
 	// Use for IFilterWriter mode
 	private void processEndGroup (Ending resource) {
 		writer.writeEndElementLineBreak(); // group
 	}
 
 	// Use for IFilterWriter mode
 	private void processTextUnit (ITextUnit tu) {
 		writeTextUnit(tu, null);
 	}
 }
