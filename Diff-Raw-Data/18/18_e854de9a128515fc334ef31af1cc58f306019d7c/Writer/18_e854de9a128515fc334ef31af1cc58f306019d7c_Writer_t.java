 /*===========================================================================
   Copyright (C) 2008 by the Okapi Framework contributors
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
 
 package net.sf.okapi.applications.rainbow.packages.xliff;
 
 import java.io.File;
 
 import net.sf.okapi.applications.rainbow.packages.BaseWriter;
 import net.sf.okapi.common.Event;
 import net.sf.okapi.common.IParameters;
 import net.sf.okapi.common.Util;
 import net.sf.okapi.common.XMLWriter;
 import net.sf.okapi.common.filterwriter.XLIFFContent;
 import net.sf.okapi.common.resource.Ending;
 import net.sf.okapi.common.resource.Property;
 import net.sf.okapi.common.resource.StartDocument;
 import net.sf.okapi.common.resource.StartGroup;
 import net.sf.okapi.common.resource.StartSubDocument;
 import net.sf.okapi.common.resource.TextContainer;
 import net.sf.okapi.common.resource.TextUnit;
 
 /**
  * Implements IWriter for generic XLIFF translation packages.
  */
 public class Writer extends BaseWriter {
 	
 	private static final String   EXTENSION = ".xlf";
 
 	protected Options options;
 
 	private XMLWriter writer = null;
 	private XLIFFContent xliffCont;
 	private boolean excludeNoTranslate = false;
 	private boolean useSourceForTranslated = false;
 	private boolean inFile;
 	private String srcLang;
 	private String docMimeType;
 
 	public Writer () {
 		super();
 		options = new Options();
 		xliffCont = new XLIFFContent();
 	}
 	
 	public String getPackageType () {
 		return "xliff";
 	}
 	
 	public String getReaderClass () {
 		//TODO: Use dynamic name
 		return "net.sf.okapi.applications.rainbow.packages.xliff.Reader";
 	}
 	
 	@Override
 	public void writeStartPackage () {
 		// Set source and target if they are not set yet
 		// This allow other package types to be derived from this one.
 		String tmp = manifest.getSourceLocation();
 		if (( tmp == null ) || ( tmp.length() == 0 )) {
 			manifest.setSourceLocation("work");
 		}
 		tmp = manifest.getTargetLocation();
 		if (( tmp == null ) || ( tmp.length() == 0 )) {
 			manifest.setTargetLocation("work");
 		}
 		tmp = manifest.getOriginalLocation();
 		if (( tmp == null ) || ( tmp.length() == 0 )) {
 			manifest.setOriginalLocation("original");
 		}
 		if (( tmp == null ) || ( tmp.length() == 0 )) {
 			manifest.setDoneLocation("done");
 		}
 		super.writeStartPackage();
 	}
 
 	@Override
 	public void createOutput (int docID,
 		String relativeSourcePath,
 		String relativeTargetPath,
 		String sourceEncoding,
 		String targetEncoding,
 		String filtersettings,
 		IParameters filterParams)
 	{
 		relativeWorkPath = relativeSourcePath;
 		
 		// OmegaT specific options
 		if ( manifest.getPackageType().equals("omegat") ) {
 			// OmegaT does not support sub-folder, so we flatten the structure
 			// and make sure identical filename do not clash
 			relativeWorkPath = String.format("%d.%s", docID,
 				Util.getFilename(relativeSourcePath, true));
 			
 			// Do not export items with translate='no'
 			excludeNoTranslate = true;
 			
 			// If translated found: replace the target text by the source.
 			// Trusting the target will be gotten from the TMX from original
 			// This to allow editing of pre-translated items in XLIFF editors
 			// that use directly the <target> element.
 			useSourceForTranslated = true;
 		}
 		
 		relativeWorkPath += EXTENSION;
 		super.createOutput(docID, relativeSourcePath, relativeTargetPath,
 			sourceEncoding, targetEncoding, filtersettings, filterParams);
 		if ( writer == null ) writer = new XMLWriter();
 		else writer.close(); // Else: make sure the previous output is closed
 
 		writer.create(manifest.getRoot() + File.separator
 			+ ((manifest.getSourceLocation().length() == 0 ) ? "" : (manifest.getSourceLocation() + File.separator)) 
 			+ relativeWorkPath);
 	}
 
 	public void close () {
 		if ( writer != null ) {
 			writer.close();
 			writer = null;
 		}
 	}
 
 	public String getName() {
 		return getClass().getName();
 	}
 
 	public IParameters getParameters () {
 		return null;
 	}
 
 	public void setParameters (IParameters params) {
 	}
 
 	public Event handleEvent (Event event) {
 		switch ( event.getEventType() ) {
 		case START_DOCUMENT:
 			processStartDocument((StartDocument)event.getResource());
 			break;
 		case END_DOCUMENT:
 			processEndDocument();
 			close();
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
 			processTextUnit((TextUnit)event.getResource());
 			break;
 		}
 		return event;
 	}
 
 	private void processStartDocument (StartDocument resource) {
 		srcLang = resource.getLanguage();
 		writer.writeStartDocument();
 		writer.writeStartElement("xliff");
 		writer.writeAttributeString("version", "1.2");
 		writer.writeAttributeString("xmlns", "urn:oasis:names:tc:xliff:document:1.2");
 		docMimeType = resource.getMimeType();
 		if (( options.message != null ) && ( options.message.length() > 0 )) {
 			writer.writeComment(options.message);
 		}
 	}
 
 	private void processEndDocument () {
 		if ( inFile ) writeEndFile();
 		writer.writeEndElementLineBreak(); // xliff
 		writer.writeEndDocument();
 		close();
 
 		manifest.addDocument(docID, relativeWorkPath, relativeSourcePath,
 			relativeTargetPath, sourceEncoding, targetEncoding, filterID);
 	}
 
 	private void processStartSubDocument (StartSubDocument resource) {
 		writeStartFile(resource.getName(), resource.getMimeType());		
 	}
 	
 	private void writeStartFile (String original,
 		String contentType)
 	{
 		writer.writeStartElement("file");
 		writer.writeAttributeString("original",
 			(original!=null) ? original : "unknown");
 		writer.writeAttributeString("source-language", srcLang);
 		writer.writeAttributeString("target-language", trgLang);
 		writer.writeAttributeString("datatype",
 			(contentType!=null) ? contentType : "x-undefined");
 		writer.writeLineBreak();
 		inFile = true;
 
 //		writer.writeStartElement("header");
 //		writer.writeEndElement(); // header
 		
 		writer.writeStartElement("body");
 		writer.writeLineBreak();
 	}
 	
 	private void processEndSubDocument (Ending resource) {
 		writeEndFile();
 	}
 	
 	private void writeEndFile () {
 		writer.writeEndElementLineBreak(); // body
 		writer.writeEndElementLineBreak(); // file
 		inFile = false;
 	}
 	
 	private void processStartGroup (StartGroup resource) {
 		if ( !inFile ) writeStartFile(relativeSourcePath, docMimeType);
 		writer.writeStartElement("group");
 		writer.writeAttributeString("id", resource.getId());
 		String tmp = resource.getName();
 		if (( tmp != null ) && ( tmp.length() != 0 )) {
 			writer.writeAttributeString("resname", tmp);
 		}
 		tmp = resource.getType();
 		if (( tmp != null ) && ( tmp.length() != 0 )) {
 			writer.writeAttributeString("restype", tmp);
 		}
 		writer.writeLineBreak();
 	}
 	
 	private void processEndGroup (Ending resource) {
 		writer.writeEndElementLineBreak(); // group
 	}
 	
 	private void processTextUnit (TextUnit tu) {
 		if ( excludeNoTranslate && !tu.isTranslatable() ) {
 			return;
 		}
 		if ( !inFile ) writeStartFile(relativeSourcePath, docMimeType);
 
 		writer.writeStartElement("trans-unit");
 		writer.writeAttributeString("id", String.valueOf(tu.getId()));
 		String tmp = tu.getName();
 		if (( tmp != null ) && ( tmp.length() != 0 )) {
 			writer.writeAttributeString("resname", tmp);
 		}
 		tmp = tu.getType();
 		if (( tmp != null ) && ( tmp.length() != 0 )) {
 			writer.writeAttributeString("restype", tmp);
 		}
 		if ( !tu.isTranslatable() )
 			writer.writeAttributeString("translate", "no");
 
 		if ( tu.hasTargetProperty(trgLang, Property.APPROVED) ) {
 			if ( tu.getTargetProperty(trgLang, Property.APPROVED).getValue().equals("yes") ) {
 				writer.writeAttributeString(Property.APPROVED, "yes");
 			}
 			// "no" is the default
 		}
 		
 		if ( tu.preserveWhitespaces() )
 			writer.writeAttributeString("xml:space", "preserve");
 		writer.writeLineBreak();
 
 		// Get the source container
 		TextContainer tc = tu.getSource();
 
 		//--- Write the source
 		
 		writer.writeStartElement("source");
 		writer.writeAttributeString("xml:lang", manifest.getSourceLanguage());
 		// Write full source content (always without segments markers
 		writer.writeRawXML(xliffCont.toSegmentedString(tc, 0, false, false,
 			options.gMode));
 		writer.writeEndElementLineBreak(); // source
 		// Write segmented source (with markers) if needed
 		if ( tc.isSegmented() ) {
 			writer.writeStartElement("seg-source");
 			writer.writeRawXML(xliffCont.toSegmentedString(tc, 0, false, true,
 				options.gMode));
 			writer.writeEndElementLineBreak(); // seg-source
 		}
 
 		//--- Write the target
 		
 		writer.writeStartElement("target");
 		writer.writeAttributeString("xml:lang", manifest.getTargetLanguage());
 		// At this point tc contains the source
 		// Do we have an available target to use instead?
		tc = tu.getTarget(trgLang);
		if ( useSourceForTranslated || ( tc == null ) || ( tc.isEmpty() )) {
			tc = tu.getSource(); // Go back to the source
 		}
 		
 		// Write out TMX entries
 		super.writeTMXEntries(tu);
 		
 		// Now tc hold the content to write. Write it with or without marks
 		writer.writeRawXML(xliffCont.toSegmentedString(tc, 0, false, tc.isSegmented(),
 			options.gMode));
 		writer.writeEndElementLineBreak(); // target
 		
 		// Note
 		if ( tu.hasProperty("note") ) {
 			writer.writeStartElement("note");
 			writer.writeString(tu.getProperty("note").getValue());
 			writer.writeEndElementLineBreak(); // note
 		}
 
 		writer.writeEndElementLineBreak(); // trans-unit
 	}
 
 	public IParameters getOptions () {
 		return options;
 	}
 
 	public void setOptions (IParameters options) {
 		this.options = (Options)options;
 	}
 
 }
