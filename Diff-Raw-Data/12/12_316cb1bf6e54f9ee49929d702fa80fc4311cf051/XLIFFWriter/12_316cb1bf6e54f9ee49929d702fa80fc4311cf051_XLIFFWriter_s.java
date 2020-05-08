 /*===========================================================================
   Copyright (C) 2009-2010 by the Okapi Framework contributors
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
 
 import net.sf.okapi.common.Util;
 import net.sf.okapi.common.XMLWriter;
 import net.sf.okapi.common.annotation.AltTranslation;
 import net.sf.okapi.common.annotation.AltTranslationsAnnotation;
 import net.sf.okapi.common.filterwriter.XLIFFContent;
 import net.sf.okapi.common.LocaleId;
 import net.sf.okapi.common.resource.Property;
 import net.sf.okapi.common.resource.Segment;
 import net.sf.okapi.common.resource.TextContainer;
 import net.sf.okapi.common.resource.TextUnit;
 
 /**
  * Writer for creating XLIFF document.
  */
 public class XLIFFWriter {
 	
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
 
 	private boolean inFile;
 	private boolean copySource = true;
 	private boolean placeholderMode = false;
 	private boolean setApprovedAsNoTranslate = false;
 	private boolean includeNoTranslate = true;
 	private boolean useSourceForTranslated = false;
 
 	public XLIFFWriter () {
 		xliffCont = new XLIFFContent();
 	}
 	
 	public void setCopySource (boolean copySource) {
 		this.copySource = copySource;
 	}
 	
 	public void setPlaceholderMode (boolean placeholderMode) {
 		this.placeholderMode = placeholderMode;
 	}
 	
 	/**
 	 * Sets the flag indicating if the source text is used in the target, even if 
 	 * a target is available.
 	 * <p>This is for the tools where we trust the target will be obtained by the tool
 	 * from the TMX from original. This to allow editing of pre-translated items in XLIFF 
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
 		writer.writeAttributeString("xmlns", "urn:oasis:names:tc:xliff:document:1.2");
 
 		if ( !Util.isEmpty(message) ) {
 			writer.writeLineBreak();
 			writer.writeComment(message);
 		}
 		writer.writeLineBreak();
 	}
 
 	/**
 	 * Writes the end of this the document and close it.
 	 * If a &lt;file> element is currently opened, it is closed automatically. 
 	 */
 	public void close () {
 		if ( writer != null ) {
 			if ( inFile ) {
 				writeEndFile();
 			}
 			writer.writeEndElementLineBreak(); // xliff
 			writer.writeEndDocument();
 			writer.close();
 			writer = null;
 		}
 	}
 
 	/**
 	 * Writes the start of a &lt;file> element.
 	 * <p>each call to this method must have a corresponding call to {@link #writeEndFile()}.
 	 * @param original the value for the <code>original</code> attribute. If null: "unknown" is used.
 	 * @param dataType the value ofr the <code>datatype</code> attribute. If null: "x-undefined" is used. 
 	 * @param skeletonPath external skeleton information (can be null).
 	 * @see #writeEndFile()
 	 */
 	public void writeStartFile (String original,
 		String dataType,
 		String skeletonPath)
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
 		// TODO: other standard XLIFF content types
 		else dataType = "x-"+dataType;
 		writer.writeAttributeString("datatype", dataType);
 		writer.writeLineBreak();
 		
 		// Write out external skeleton info if available 
 		if ( !Util.isEmpty(skeletonPath) ) {
 			writer.writeStartElement("header");
 			writer.writeStartElement("skl");
 			writer.writeStartElement("external-file");
 			writer.writeAttributeString("href", skeletonPath);
 			writer.writeEndElement(); // external-file
 			writer.writeEndElement(); // skl
 			writer.writeEndElementLineBreak(); // header
 		}
 
 		inFile = true;
 		writer.writeStartElement("body");
 		writer.writeLineBreak();
 	}
 	
 	
 	/**
 	 * Writes the end of a &lt;file> element.
	 * This method should be called for each call to {@link #writeStartFile(String, String, String, String)}.
	 * @see #writeStartFile(String, String, String, String)
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
 			writeStartFile(original, dataType, skeletonPath);
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
 	public void writeTextUnit (TextUnit tu) {
 		// Check if we need to set the entry as non-translatable
 		if ( setApprovedAsNoTranslate ) {
 			Property prop = tu.getTargetProperty(trgLoc, Property.APPROVED);
 			if (( prop != null ) && prop.getValue().equals("yes") ) {
 				tu.setIsTranslatable(false);
 			}
 		}
 		// Check if we need to skip non-translatable entries
 		if ( !includeNoTranslate && !tu.isTranslatable() ) {
 			return;
 		}
 
 		if ( !inFile ) {
 			writeStartFile(original, dataType, skeletonPath);
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
 		writer.writeLineBreak();
 
 		// Get the source container
 		TextContainer tc = tu.getSource();
 		boolean srcHasText = tc.hasText(false);
 
 		//--- Write the source
 		
 		writer.writeStartElement("source");
 		writer.writeAttributeString("xml:lang", srcLoc.toBCP47());
 		// Write full source content (always without segments markers
 		writer.writeRawXML(xliffCont.toSegmentedString(tc, 0, false, false, placeholderMode));
 		writer.writeEndElementLineBreak(); // source
 		// Write segmented source (with markers) if needed
 		if ( tc.hasBeenSegmented() ) {
 			writer.writeStartElement("seg-source");
 			writer.writeRawXML(xliffCont.toSegmentedString(tc, 0, false, true, placeholderMode));
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
 				writer.writeRawXML(xliffCont.toSegmentedString(tc, 0, false, tc.hasBeenSegmented(), placeholderMode));
 				writer.writeEndElementLineBreak(); // target
 			}
 
 			// Possible alternate translations
 			// We re-get the target because tc could be coming from the source
 			TextContainer altCont = tu.getTarget(trgLoc);
 			if ( altCont != null ) {
 				// From the segments
 				for ( Segment seg : altCont.getSegments() ) {
 					writeAltTranslations(seg.getAnnotation(AltTranslationsAnnotation.class),
 						altCont.hasBeenSegmented(), seg);
 				}
 				// From the target container
 				writeAltTranslations(altCont.getAnnotation(AltTranslationsAnnotation.class), false, null);
 			}
 		}
 		
 		// Note
 		if ( tu.hasProperty(Property.NOTE) ) {
 			writer.writeStartElement("note");
 			writer.writeString(tu.getProperty(Property.NOTE).getValue());
 			writer.writeEndElementLineBreak(); // note
 		}
 
 		writer.writeEndElementLineBreak(); // trans-unit
 	}
 
 	/**
 	 * Writes possible alternate translations
 	 * @param ann the annotation with the alternate translations (can be null)
 	 * @param hasBeenSegmented indicates if the annotation comes from a segmented container
 	 * (this is needed because a non-segmented container is still  with a Segment). 
 	 * @param segment the segment where the annotation comes from, or null  if the
 	 * annotation comes from the container.
 	 */
 	private void writeAltTranslations (AltTranslationsAnnotation ann,
 		boolean hasBeenSegmented,
 		Segment segment)
 	{
 		if ( ann == null ) {
 			return;
 		}
 		for ( AltTranslation alt : ann ) {
 			writer.writeStartElement("alt-trans");
 			if (( segment != null ) && hasBeenSegmented ) {
 				writer.writeAttributeString("mid", segment.getId());
 			}
 			if ( alt.getScore() > 0 ) {
 				writer.writeAttributeString("match-quality", String.format("%d", alt.getScore()));
 			}
 			if ( !Util.isEmpty(alt.getOrigin()) ) {
 				writer.writeAttributeString("origin", alt.getOrigin());
 			}
 			TextUnit altTu = alt.getEntry();
 			if ( !altTu.isEmpty() ) {
 				writer.writeStartElement("source");
 				writer.writeAttributeString("xml:lang", alt.getSourceLocale().toBCP47());
 				// Write full source content (always without segments markers
 				writer.writeRawXML(xliffCont.toSegmentedString(alt.getSource(), 0, false, false, placeholderMode));
 				writer.writeEndElementLineBreak(); // source
 			}
 			writer.writeStartElement("target");
 			writer.writeAttributeString("xml:lang", alt.getTargetLocale().toBCP47());
 			writer.writeRawXML(xliffCont.toSegmentedString(alt.getTarget(), 0, false, false, placeholderMode));
 			writer.writeEndElementLineBreak(); // target
 			writer.writeEndElementLineBreak(); // alt-trans
 		}
 	}
 
 }
