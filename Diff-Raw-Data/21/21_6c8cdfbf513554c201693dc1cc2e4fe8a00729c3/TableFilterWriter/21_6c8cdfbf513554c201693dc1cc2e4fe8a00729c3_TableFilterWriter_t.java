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
 
 package net.sf.okapi.steps.formatconversion;
 
 import java.io.BufferedOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.io.OutputStreamWriter;
 import java.nio.charset.Charset;
 
 import net.sf.okapi.common.Event;
 import net.sf.okapi.common.IParameters;
 import net.sf.okapi.common.Util;
 import net.sf.okapi.common.encoder.EncoderManager;
 import net.sf.okapi.common.exceptions.OkapiIOException;
 import net.sf.okapi.common.filterwriter.GenericContent;
 import net.sf.okapi.common.filterwriter.IFilterWriter;
 import net.sf.okapi.common.filterwriter.TMXContent;
 import net.sf.okapi.common.filterwriter.XLIFFContent;
 import net.sf.okapi.common.LocaleId;
 import net.sf.okapi.common.resource.Segment;
 import net.sf.okapi.common.resource.ISegments;
 import net.sf.okapi.common.resource.StartDocument;
 import net.sf.okapi.common.resource.TextContainer;
 import net.sf.okapi.common.resource.TextFragment;
 import net.sf.okapi.common.resource.ITextUnit;
 import net.sf.okapi.common.skeleton.ISkeletonWriter;
 
 /**
  * Implementation of the {@link IFilterWriter} interface for table-type output.
  */
 public class TableFilterWriter implements IFilterWriter {
 
 	private static final int INLINE_ORIGINAL = 0;
 	private static final int INLINE_GENERIC = 1;
 	private static final int INLINE_TMX = 2;
 	private static final int INLINE_XLIFF = 3;
 	private static final int INLINE_XLIFF_GX = 4;
 	
 	private TableFilterWriterParameters params;
 	private OutputStreamWriter writer;
 	private OutputStream outputStream;
 	private String outputPath;
 	private LocaleId trgLoc;
 	private String encoding;
 	private File tempFile;
 	private String linebreak = System.getProperty("line.separator");
 	private int inlineType;
 	private TMXContent tmxFmt;
 	private XLIFFContent xliffFmt;
 	private GenericContent genericFmt;
 	private boolean useDQ;
 	private boolean escapeTab;
 	
 	public TableFilterWriter () {
 		params = new TableFilterWriterParameters();
 	}
 	
 	public void cancel () {
 		//TODO: support cancel
 	}
 
 	public void close () {
 		if ( writer == null ) return;
 		IOException err = null;
 		InputStream orig = null;
 		OutputStream dest = null;
 		try {
 			// And writer
 			writer.close();
 			writer = null;
 			if ( outputStream != null ) {
 				outputStream.close();
 				outputStream = null;
 			}
 
 			// If it was in a temporary file, copy it over the existing one
 			// If the IFilter.close() is called before IFilterWriter.close()
 			// this should allow to overwrite the input.
 			if ( tempFile != null ) {
 				dest = new FileOutputStream(outputPath);
 				orig = new FileInputStream(tempFile); 
 				byte[] buffer = new byte[2048];
 				int len;
 				while ( (len = orig.read(buffer)) > 0 ) {
 					dest.write(buffer, 0, len);
 				}
 			}
 		}
 		catch ( IOException e ) {
 			err = e;
 		}
 		finally {
 			// Make sure we close both files
 			if ( dest != null ) {
 				try {
 					dest.close();
 				}
 				catch ( IOException e ) {
 					err = e;
 				}
 				dest = null;
 			}
 			if ( orig != null ) {
 				try {
 					orig.close();
 				} catch ( IOException e ) {
 					err = e;
 				}
 				orig = null;
 				if ( err != null ) throw new RuntimeException(err);
 				else {
 					if ( tempFile != null ) {
 						tempFile.delete();
 						tempFile = null;
 					}
 				}
 			}
 		}
 	}
 
 	public String getName () {
 		return "TableFilterWriter";
 	}
 
 	public EncoderManager getEncoderManager () {
 		return null;
 	}
 	
 	@Override
 	public ISkeletonWriter getSkeletonWriter () {
 		return null;
 	}
 
 	public IParameters getParameters () {
 		return params;
 	}
 
 	public Event handleEvent (Event event) {
 		switch ( event.getEventType() ) {
 		case START_BATCH:
 			initialize();
 			break;
 		case START_DOCUMENT:
 			handleStartDocument(event);
 			break;
 		case TEXT_UNIT:
 			handleTextUnit(event);
 			break;
 		case END_DOCUMENT:
 			close();
 			break;
 		}
 		return event;
 	}
 
 	public void setOptions (LocaleId locale,
 		String defaultEncoding)
 	{
 		trgLoc = locale;
 		encoding = defaultEncoding;
 	}
 
 	public void setOutput (String path) {
 		outputPath = path;
 	}
 
 	public void setOutput (OutputStream output) {
 		outputStream = output;
 	}
 
 	public void setParameters (IParameters params) {
 		this.params = (TableFilterWriterParameters)params;
 	}
 
 	private void initialize () {
 		useDQ = params.getUseDoubleQuotes();
 		escapeTab = (params.getSeparator().indexOf('\t') > -1);
 		if ( params.getInlineFormat().equals(TableFilterWriterParameters.INLINE_GENERIC) ) {
 			inlineType = INLINE_GENERIC;
 			genericFmt = new GenericContent();
 		}
 		else if ( params.getInlineFormat().equals(TableFilterWriterParameters.INLINE_TMX) ) {
 			inlineType = INLINE_TMX;
 			tmxFmt = new TMXContent();
 		}
 		else if ( params.getInlineFormat().equals(TableFilterWriterParameters.INLINE_XLIFF) ) {
 			inlineType = INLINE_XLIFF;
 			xliffFmt = new XLIFFContent();
 		}
 		else if ( params.getInlineFormat().equals(TableFilterWriterParameters.INLINE_XLIFFGX) ) {
 			inlineType = INLINE_XLIFF_GX;
 			xliffFmt = new XLIFFContent();
 		}
 		else {
 			inlineType = INLINE_ORIGINAL;
 		}
 	}
 	
 	private void handleStartDocument (Event event) {
 		try {
 			StartDocument sd = (StartDocument)event.getResource();
 			tempFile = null;
 			// If needed, create the output stream from the path provided
 			if ( outputStream == null ) {
 				boolean useTemp = false;
 				File f = new File(outputPath);
 				if ( f.exists() ) {
 					// If the file exists, try to remove
 					useTemp = !f.delete();
 				}
 				if ( useTemp ) {
 					// Use a temporary output if we can overwrite for now
 					// If it's the input file, IFilter.close() will free it before we
 					// call close() here (that is if IFilter.close() is called correctly
 					tempFile = File.createTempFile("gfwTmp", null);
 					outputStream = new BufferedOutputStream(new FileOutputStream(tempFile.getAbsolutePath()));
 				}
 				else { // Make sure the directory exists
 					Util.createDirectories(outputPath);
 					outputStream = new BufferedOutputStream(new FileOutputStream(outputPath));
 				}
 			}
 			
 			// Get the encoding of the original document
 			String originalEnc = sd.getEncoding();
 			// If it's undefined, assume it's the default of the system
 			if ( originalEnc == null ) {
 				originalEnc = Charset.defaultCharset().name();
 			}
 			// Check if the output encoding is defined
 			if ( encoding == null ) {
 				// if not: Fall back on the encoding of the original
 				encoding = originalEnc;
 			}
 			// Create the output
 			writer = new OutputStreamWriter(outputStream, encoding);
 			// Set default UTF-8 BOM usage
 			boolean useUTF8BOM = false; // On all platforms
 			// Check if the output encoding is UTF-8
 			if ( "utf-8".equalsIgnoreCase(encoding) ) {
 				// If the original was UTF-8 too
 				if ( "utf-8".equalsIgnoreCase(originalEnc) ) {
 					// Check whether it had a BOM or not
 					useUTF8BOM = sd.hasUTF8BOM();
 				}
 			}
 			// Write out the BOM if needed
 			Util.writeBOMIfNeeded(writer, useUTF8BOM, encoding);
 		}
 		catch ( FileNotFoundException e ) {
 			throw new RuntimeException(e);
 		}
 		catch ( IOException e ) {
 			throw new RuntimeException(e);
 		}
 	}
 	
 	private void handleTextUnit (Event event) {
 		ITextUnit tu = event.getTextUnit();
 
 		TextContainer srcCont = tu.getSource();
 		TextContainer trgCont = tu.getTarget(trgLoc);
 		if ( trgCont == null ) {
 			trgCont = new TextContainer(); // Empty
 		}
		
 		// If not segmented: index the whole entry
 		if ( !srcCont.contentIsOneSegment() ) {
 			writeRow(srcCont.getFirstContent(), trgCont.getFirstContent());
 			return;
 		}
 
 		// Else: check if we have the same number of segments
 		if ( trgCont.getSegments().count() != srcCont.getSegments().count() ) { 
 			// If not: Fall back to full entry
 			writeRow(srcCont.getUnSegmentedContentCopy(), trgCont.getUnSegmentedContentCopy());
 			//TODO: Log a warning
 			return;
 		}
 		// If we do have the same number of segments:
 		// Output each of them
 		ISegments trgSegs = trgCont.getSegments();
 		for ( Segment srcSeg : srcCont.getSegments() ) {
 			Segment trgSeg = trgSegs.get(srcSeg.id);
 			if ( trgSeg == null ) {
 				writeRow(srcSeg.text, null);
 			}
 			else {
 				writeRow(srcSeg.text, trgSeg.text);
 			}
 		}
 	}
 
 	private void writeRow (TextFragment srcFrag,
 		TextFragment trgFrag)
 	{
 		try {
 			if ( useDQ ) writer.write("\"");
 			writer.write(format(srcFrag));
 			if ( useDQ ) writer.write("\""+params.getSeparator()+"\"");
 			else writer.write(params.getSeparator());
 			if ( trgFrag != null ) writer.write(format(trgFrag));
 			if ( useDQ ) writer.write("\"");			
 			writer.write(linebreak);
 		}
 		catch ( IOException e ) {
 			throw new OkapiIOException("Error writing row.", e);
 		}
 	}
 
 	private String format (TextFragment frag) {
 		String tmp;
 		switch ( inlineType ) {
 		case INLINE_GENERIC:
 			tmp = genericFmt.setContent(frag).toString();
 			break;
 		case INLINE_TMX:
 			tmp = tmxFmt.setContent(frag).toString();
 			break;
 		case INLINE_XLIFF:
 			tmp = xliffFmt.setContent(frag).toString();
 			break;
 		case INLINE_XLIFF_GX:
 			tmp = xliffFmt.setContent(frag).toString(true);
 			break;
 		default:
 			tmp = frag.toText();
 			break;
 		}
 		if ( useDQ ) {
 			// Automatically escape \ and " if text is double-quoted
 			tmp = tmp.replace("\\", "\\\\");
 			tmp = tmp.replace("\"", "\\\"");
 		}
 		if ( escapeTab ) {
 			return tmp.replace("\t", "\\t");
 		}
 		return tmp;
 	}
 
 }
