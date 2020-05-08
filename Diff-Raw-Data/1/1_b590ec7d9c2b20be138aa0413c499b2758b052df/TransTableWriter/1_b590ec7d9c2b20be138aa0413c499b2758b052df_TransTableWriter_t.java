 /*===========================================================================
   Copyright (C) 2011-2012 by the Okapi Framework contributors
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
 
 package net.sf.okapi.filters.transtable;
 
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
 import net.sf.okapi.common.exceptions.OkapiFileNotFoundException;
 import net.sf.okapi.common.exceptions.OkapiIOException;
 import net.sf.okapi.common.filterwriter.GenericContent;
 import net.sf.okapi.common.filterwriter.IFilterWriter;
 import net.sf.okapi.common.LocaleId;
 import net.sf.okapi.common.resource.ISegments;
 import net.sf.okapi.common.resource.Segment;
 import net.sf.okapi.common.resource.StartDocument;
 import net.sf.okapi.common.resource.StartGroup;
 import net.sf.okapi.common.resource.StartSubDocument;
 import net.sf.okapi.common.resource.TextContainer;
 import net.sf.okapi.common.resource.ITextUnit;
 import net.sf.okapi.common.resource.TextFragment;
 import net.sf.okapi.common.skeleton.ISkeletonWriter;
 
 public class TransTableWriter implements IFilterWriter {
 
 	public static final String CRUMBS_PREFIX = "okpCtx";
 	public static final String SUBDOCUMENT_CRUMB = "sd=";
 	public static final String GROUP_CRUMB = "gp=";
 	public static final String TEXTUNIT_CRUMB = "tu=";
 	public static final String SEGMENT_CRUMB = "s=";
 	public static final String SIGNATURE = "TransTable";
 	public static final String VERSION = "V1";
 	public static final String ESCAPEABLE = "\\\"abfnrtv";
 	
 	private static final String LINEBREAK = System.getProperty("line.separator");
 
 	private Parameters params;
 	private OutputStream output;
 	private String outputPath;
 	private OutputStreamWriter writer;
 	private File tempFile;
 	private LocaleId language;
 	private String encoding;
 	private GenericContent fmt;
 	private String crumbs;
 	
 	public TransTableWriter () {
 		params = new Parameters();
 		fmt = new GenericContent();
 	}
 	
 	public void cancel () {
 		//TODO
 	}
 
 	public void close () {
 		if ( writer == null ) return;
 		IOException err = null;
 		InputStream orig = null;
 		OutputStream dest = null;
 		try {
 			// Close the output
 			writer.close();
 			writer = null;
 			output.close();
 			output = null;
 
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
 				}
 				catch ( IOException e ) {
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
 		return "TableWriter";
 	}
 
 	public EncoderManager getEncoderManager () {
 		// This writer does not use skeleton
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
 		case START_DOCUMENT:
 			processStartDocument(event);
 			break;
 		case END_DOCUMENT:
 			processEndDocument();
 			break;
 		case START_SUBDOCUMENT:
 			processStartSubDocument(event);
 			break;
 		case END_SUBDOCUMENT:
 			processEndSubDocument();
 			break;
 		case START_GROUP:
 			processStartGroup(event);
 			break;
 		case END_GROUP:
 			processEndGroup(event);
 			break;
 		case TEXT_UNIT:
 			processTextUnit(event);
 			break;
 		}
 		return event;
 	}
 	
 	public void setOptions (LocaleId language,
 		String defaultEncoding)
 	{
 		this.language = language;
 		this.encoding = defaultEncoding;
 	}
 
 	public void setOutput (String path) {
 		close(); // Make sure previous is closed
 		this.outputPath = path;
 	}
 
 	public void setOutput (OutputStream output) {
 		close(); // Make sure previous is closed
 		this.outputPath = null; // If we use the stream, we can't use the path
 		this.output = output; // then assign the new stream
 	}
 
 	public void setParameters (IParameters params) {
 		params = (Parameters)params;
 	}
 
 	private void processStartDocument (Event event) {
 		try {
 			StartDocument sd = (StartDocument)event.getResource();
 			// Create the output
 			createWriter(sd);
 			// Writer header
 			writer.write(String.format("%s%s\t%s\t%s"+LINEBREAK, SIGNATURE, VERSION,
 				sd.getLocale().toString(), language.toString()));
 			crumbs = CRUMBS_PREFIX;
 		}
 		catch ( IOException e ) {
 			throw new OkapiIOException("Error writing the header.", e);
 		}
 	}
 
 	private void processEndDocument () {
 		close();
 	}
 
 	private void processStartSubDocument (Event event) {
 		StartSubDocument ssd = (StartSubDocument)event.getResource();
 		pushCrumb(SUBDOCUMENT_CRUMB+ssd.getId());
 	}
 	
 	private void processEndSubDocument () {
 		popCrumb();
 	}
 
 	private void processStartGroup (Event event) {
 		StartGroup sg = (StartGroup)event.getResource();
 		pushCrumb(GROUP_CRUMB+sg.getId());
 	}
 
 	private void processEndGroup (Event event) {
 		popCrumb();
 	}
 
 	private void popCrumb () {
 		int n = crumbs.lastIndexOf(':');
 		crumbs = crumbs.substring(0, n);
 	}
 	
 	private void pushCrumb (String crumb) {
 		crumbs += (":"+crumb);
 	}
 	
 	private void processTextUnit (Event event) {
 		try {
 			ITextUnit tu = event.getTextUnit();
 			if ( tu.isEmpty() ) return; // Do not write out entries with empty source
 			
 			if ( params.getAllowSegments() ) {
 				processWithSegments(tu);
 			}
 			else {
 				processWithoutSegments(tu);
 			}
 		}
 		catch ( Throwable e ) {
 			throw new OkapiIOException("Error writing a text unit.", e);
 		}
 	}
 	
 	private void processWithSegments (ITextUnit tu) {
 		try {
 			ISegments srcSegs = tu.getSourceSegments();
 			ISegments trgSegs = null;
 			TextContainer tc = tu.getTarget(language);
 			if ( tc != null ) {
 				trgSegs = tc.getSegments();
 			}
 	
 			for ( Segment srcSeg : srcSegs ) {
 				// Write the ID
 				writer.write("\"" + crumbs + ":" + TEXTUNIT_CRUMB+tu.getId() + ":" + SEGMENT_CRUMB+srcSeg.getId()+ "\"\t");
 				
 				// Write the source
 				writeQuotedContent(srcSeg.getContent());
				writer.write("\t");
 				
 				// Write the target
 				if ( trgSegs != null ) {
 					Segment trgSeg = trgSegs.get(srcSeg.getId());
 					if ( trgSeg != null ) {
 						writeQuotedContent(trgSeg.getContent());
 					}
 				}
 				// EOL
 				writer.write(LINEBREAK);
 			}
 		}
 		catch ( IOException e ) {
 			throw new OkapiIOException("Error writing segments.", e);
 		}
 	}
 	
 	private void processWithoutSegments (ITextUnit tu) {
 		try {
 			// ID reference to allow merging back and duplication of msgid text
 			writer.write("\"" + crumbs + ":" + TEXTUNIT_CRUMB+tu.getId() + "\"\t");
 			// Source
 			writeQuotedContent(tu.getSource());
 			writer.write("\t");
 			// Target
 			TextContainer tc = tu.getTarget(language);
 			if ( tc != null ) {
 				writeQuotedContent(tc);
 			}
 			// EOL
 			writer.write(LINEBREAK);
 		}
 		catch ( IOException e ) {
 			throw new OkapiIOException("Error writing text unit.", e);
 		}
 	}
 
 	private void writeQuotedContent (TextFragment tf) {
 		try {
 			String tmp = fmt.fromFragmentToLetterCoded(tf);
 			tmp = escapeIfNeeded(tmp);
 			writer.write("\"");
 			writer.write(tmp); // No wrapping needed
 			writer.write("\"");
 		}
 		catch ( IOException e ) {
 			throw new OkapiIOException("Error writing a quoted text.", e);
 		}
 	}
 	
 	private void writeQuotedContent (TextContainer tc) {
 		try {
 			String tmp;
 			if ( tc.contentIsOneSegment() ) {
 				tmp = fmt.fromFragmentToLetterCoded(tc.getFirstContent());
 			}
 			else { // If the container is segmented
 				tmp = fmt.fromFragmentToLetterCoded(tc.getUnSegmentedContentCopy());
 			}
 			tmp = escapeIfNeeded(tmp);
 			writer.write("\"");
 			writer.write(tmp); // No wrapping needed
 			writer.write("\"");
 		}
 		catch ( IOException e ) {
 			throw new OkapiIOException("Error writing a quoted text.", e);
 		}
 	}
 
 	// We assume that if a bslash id followed by a valid escapeable char
 	// it is a valid escape.
 	// This means unescaped paths won't be escaped in some cases:
 	// c:\abc should be c:\\abc but won't because \a is valid escape
 	private String escapeIfNeeded (String in) {
 		char prev = '\0';
 		StringBuilder tmp = new StringBuilder(in.length());
 		for ( int i=0; i<in.length(); i++ ) {
 			switch ( in.charAt(i) ) {
 			case '\\':
 				if (( i < in.length()-1 ) && ( ESCAPEABLE.indexOf(in.charAt(i+1)) != -1 )) {
 					// We assume it's an escape
 					tmp.append('\\');
 					tmp.append(in.charAt(i+1));
 					i++;
 				}
 				else { // It's an isolated '\'
 					tmp.append("\\\\");
 				}
 				prev = '\0';
 				continue;
 			case '"':
 				if ( prev != '\\' ) {
 					tmp.append('\\');
 				}
 				tmp.append(in.charAt(i));
 				break;
 			case '\n':
 				tmp.append("\\n");
 				break;
 			default:
 				tmp.append(in.charAt(i));
 				break;
 			}
 			prev = in.charAt(i);
 		}
 		return tmp.toString();
 	}
 
 	private void createWriter (StartDocument startDoc) {
 		try {
 			tempFile = null;
 			// If needed, create the output stream from the path provided
 			if ( output == null ) {
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
 					tempFile = File.createTempFile("pofwTmp", null);
 					output = new BufferedOutputStream(new FileOutputStream(tempFile.getAbsolutePath()));
 				}
 				else { // Make sure the directory exists
 					Util.createDirectories(outputPath);
 					output = new BufferedOutputStream(new FileOutputStream(outputPath));
 				}
 			}
 			
 			// Get the encoding of the original document
 			String originalEnc = startDoc.getEncoding();
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
 			writer = new OutputStreamWriter(output, encoding);
 			// Set default UTF-8 BOM usage
 			boolean useUTF8BOM = false; // On all platforms
 			// Check if the output encoding is UTF-8
 			if ( "utf-8".equalsIgnoreCase(encoding) ) {
 				// If the original was UTF-8 too
 				if ( "utf-8".equalsIgnoreCase(originalEnc) ) {
 					// Check whether it had a BOM or not
 					// Most PO-aware tools are Linux and do not like BOM
 					useUTF8BOM = false; // startDoc.hasUTF8BOM();
 				}
 			}
 			// Write out the BOM if needed
 			Util.writeBOMIfNeeded(writer, useUTF8BOM, encoding);
 		}
 		catch ( FileNotFoundException e ) {
 			throw new OkapiFileNotFoundException(e);
 		}
 		catch ( IOException e ) {
 			throw new OkapiIOException(e);
 		}
 	}
 
 }
