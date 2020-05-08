 /*===========================================================================
   Copyright (C) 2011 by the Okapi Framework contributors
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
 
 package net.sf.okapi.filters.wiki;
 
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
 import java.util.HashMap;
 
 import net.sf.okapi.common.Event;
 import net.sf.okapi.common.IParameters;
 import net.sf.okapi.common.LocaleId;
 import net.sf.okapi.common.Util;
 import net.sf.okapi.common.encoder.EncoderManager;
 import net.sf.okapi.common.exceptions.OkapiFileNotFoundException;
 import net.sf.okapi.common.exceptions.OkapiIOException;
 import net.sf.okapi.common.filterwriter.GenericContent;
 import net.sf.okapi.common.filterwriter.IFilterWriter;
 import net.sf.okapi.common.resource.DocumentPart;
 import net.sf.okapi.common.resource.ITextUnit;
 import net.sf.okapi.common.resource.StartDocument;
 import net.sf.okapi.common.resource.TextContainer;
 import net.sf.okapi.common.resource.TextFragment;
 import net.sf.okapi.common.skeleton.GenericSkeleton;
 import net.sf.okapi.common.skeleton.ISkeletonWriter;
 
 /**
  * @author aaron.madlon-kay
  * 
  */
 public class WikiWriter implements IFilterWriter {
 	
	private static final String SELF_REF = "$self$";
	private static final String DUMMY_SKELETON = String.format(
			"%s%s%s",
			TextFragment.REFMARKER_START,
			SELF_REF,
			TextFragment.REFMARKER_END);
	
 	private OutputStream output;
 	private String outputPath;
 	private OutputStreamWriter writer;
 	private LocaleId language;
 	private String encoding;
 	private String linebreak;
 	private File tempFile;
 	private GenericContent formatter;
 	private HashMap<String, String> referencedText;
 
 	public WikiWriter() {
 		formatter = new GenericContent();
 		referencedText = new HashMap<String, String>();
 	}
 
 	@Override
 	public String getName() {
 		return "WikiWriter";
 	}
 
 	@Override
 	public void setOptions(LocaleId language, String defaultEncoding) {
 		this.language = language;
 		this.encoding = defaultEncoding;
 	}
 
 	@Override
 	public void setOutput(String path) {
 		close(); // Make sure previous is closed
 		this.outputPath = path;
 	}
 
 	@Override
 	public void setOutput(OutputStream output) {
 		close(); // Make sure previous is closed
 		this.outputPath = null; // If we use the stream, we can't use the path
 		this.output = output; // then assign the new stream
 	}
 
 	@Override
 	public Event handleEvent(Event event) {
 
 		switch (event.getEventType()) {
 		case START_DOCUMENT:
 			processStartDocument(event);
 			break;
 		case END_DOCUMENT:
 			processEndDocument();
 			break;
 		case START_GROUP:
 			processStartGroup(event);
 			break;
 		case END_GROUP:
 			processEndGroup();
 			break;
 		case TEXT_UNIT:
 			processTextUnit(event);
 			break;
 		case DOCUMENT_PART:
 			processDocumentPart(event);
 			break;
 		default:
 			event = Event.NOOP_EVENT;
 			break;
 		}
 		return event;
 	}
 
 	private void processEndGroup() {
 
 	}
 
 	private void processStartGroup(Event event) {
 
 	}
 
 	@Override
 	public void close() {
 		if (writer == null) {
 			return;
 		}
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
 			if (tempFile != null) {
 				dest = new FileOutputStream(outputPath);
 				orig = new FileInputStream(tempFile);
 				byte[] buffer = new byte[2048];
 				int len;
 				while ((len = orig.read(buffer)) > 0) {
 					dest.write(buffer, 0, len);
 				}
 			}
 		} catch (IOException e) {
 			err = e;
 		} finally {
 			// Make sure we close both files
 			if (dest != null) {
 				try {
 					dest.close();
 				} catch (IOException e) {
 					err = e;
 				}
 				dest = null;
 			}
 			if (orig != null) {
 				try {
 					orig.close();
 				} catch (IOException e) {
 					err = e;
 				}
 				orig = null;
 				if (err != null)
 					throw new RuntimeException(err);
 				else {
 					if (tempFile != null) {
 						tempFile.delete();
 						tempFile = null;
 					}
 				}
 			}
 		}
 	}
 
 	@Override
 	public IParameters getParameters() {
 		return null;
 	}
 
 	@Override
 	public void setParameters(IParameters params) {
 	}
 
 	@Override
 	public void cancel() {
 		// TODO
 	}
 
 	@Override
 	public EncoderManager getEncoderManager() {
 		// This writer does not use skeleton
 		return null;
 	}
 
 	@Override
 	public ISkeletonWriter getSkeletonWriter() {
 		return null;
 	}
 
 	private void createWriter(StartDocument startDoc) {
 		linebreak = startDoc.getLineBreak();
 		try {
 			tempFile = null;
 			// If needed, create the output stream from the path provided
 			if (output == null) {
 				boolean useTemp = false;
 				File f = new File(outputPath);
 				if (f.exists()) {
 					// If the file exists, try to remove
 					useTemp = !f.delete();
 				}
 				if (useTemp) {
 					// Use a temporary output if we can overwrite for now
 					// If it's the input file, IFilter.close() will free it
 					// before we
 					// call close() here (that is if IFilter.close() is called
 					// correctly
 					tempFile = File.createTempFile("wkmrkTmp", null);
 					output = new BufferedOutputStream(new FileOutputStream(
 							tempFile.getAbsolutePath()));
 				} else { // Make sure the directory exists
 					Util.createDirectories(outputPath);
 					output = new BufferedOutputStream(new FileOutputStream(
 							outputPath));
 				}
 			}
 
 			// Get the encoding of the original document
 			String originalEnc = startDoc.getEncoding();
 			// If it's undefined, assume it's the default of the system
 			if (originalEnc == null) {
 				originalEnc = Charset.defaultCharset().name();
 			}
 			// Check if the output encoding is defined
 			if (encoding == null) {
 				// if not: Fall back on the encoding of the original
 				encoding = originalEnc;
 			}
 			// Create the output
 			writer = new OutputStreamWriter(output, encoding);
 			// Set default UTF-8 BOM usage
 			boolean useUTF8BOM = false; // On all platforms
 			// Check if the output encoding is UTF-8
 			if ("utf-8".equalsIgnoreCase(encoding)) {
 				// If the original was UTF-8 too
 				if ("utf-8".equalsIgnoreCase(originalEnc)) {
 					// Check whether it had a BOM or not
 					// Linux tools do not like BOM
 					useUTF8BOM = false; // startDoc.hasUTF8BOM();
 				}
 			}
 			// Write out the BOM if needed
 			Util.writeBOMIfNeeded(writer, useUTF8BOM, encoding);
 		} catch (FileNotFoundException e) {
 			throw new OkapiFileNotFoundException(e);
 		} catch (IOException e) {
 			throw new OkapiIOException(e);
 		}
 	}
 
 	private void processStartDocument(Event event) {
 		StartDocument sd = event.getStartDocument();
 
 		// Create the output
 		createWriter(sd);
 		
 		referencedText.clear();
 	}
 
 	private void processEndDocument() {
 		close();
 	}
 
 	private void processTextUnit(Event event) {
 		
 		ITextUnit tu = event.getTextUnit();
 		
 		GenericSkeleton skel = (GenericSkeleton) tu.getSkeleton();
 		
 		StringBuilder tuText = new StringBuilder();
		
		tuText.append(skel != null ? skel.toString() : DUMMY_SKELETON);
 		
 		// Body of TU
 		TextContainer bodyContainer = null;
 		if (tu.hasTarget(language)) bodyContainer = tu.getTarget(language);
 		else bodyContainer = tu.getSource();
 		
 		referencedText.put("$self$", formatter.printSegmentedContent(bodyContainer, false, true));
 		
 		// Replace all refmarkers with the appropriate content.
 		Object[] refMarkInfo = TextFragment.getRefMarker(tuText);
 		while (refMarkInfo != null) {
 			assert(referencedText.containsKey(refMarkInfo[0]));
 			tuText.replace(
 					(Integer) refMarkInfo[1],
 					(Integer) refMarkInfo[2],
 					referencedText.get(refMarkInfo[0]));
 			refMarkInfo = TextFragment.getRefMarker(tuText);
 		}
 		
 		if (tu.getReferenceCount() > 0) {
 			referencedText.put(tu.getId(), tuText.toString());
 			return;
 		}
 
 		try {
 			writer.write(tuText.toString());
 		} catch (IOException e) {
 			throw new OkapiIOException("Error writing a wiki markup file.", e);
 		}
 	}
 
 	private void processDocumentPart(Event event) {
 		try {
 			DocumentPart part = event.getDocumentPart();
 			writer.write(part.toString());
 
 		} catch (IOException e) {
 			throw new OkapiIOException("Error writing a wiki markup file.", e);
 		}
 	}
 
 }
