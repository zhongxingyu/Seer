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
 
 package net.sf.okapi.filters.openxml;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.logging.Logger;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipFile;
 import java.util.zip.ZipOutputStream;
 
 import net.sf.okapi.common.Event;
 import net.sf.okapi.common.EventType;
 import net.sf.okapi.common.IParameters;
 import net.sf.okapi.common.Util;
 import net.sf.okapi.common.exceptions.*;
 import net.sf.okapi.common.filterwriter.GenericFilterWriter;
 import net.sf.okapi.common.filterwriter.IFilterWriter;
 import net.sf.okapi.common.LocaleId;
 import net.sf.okapi.common.resource.DocumentPart;
 import net.sf.okapi.common.resource.Ending;
 import net.sf.okapi.common.resource.Property;
 import net.sf.okapi.common.resource.StartDocument;
 import net.sf.okapi.common.resource.StartSubDocument;
 import net.sf.okapi.common.resource.TextUnit;
 import net.sf.okapi.common.skeleton.ZipSkeleton;
 //import net.sf.okapi.filters.abstractmarkup.Parameters;
 
 /**
  * <p>Implements the IFilterWriter interface for the OpenXMLFilter, which
  * filters Microsoft Office Word, Excel, and Powerpoint Documents. OpenXML 
  * is the format of these documents.
  * 
  * <p>Since OpenXML files are Zip files that contain XML documents,
  * this filter writer handles writing out the zip file, and
  * uses OpenXMLContentSkeletonWriter to output the XML documents.
  * 
  */
 
 public class OpenXMLZipFilterWriter implements IFilterWriter {
 
 	public final static int MSWORD=1;
 	public final static int MSEXCEL=2;
 	public final static int MSPOWERPOINT=3;
 	public final static int MSWORDCHART=4; // DWH 4-16-09
 	public final static int MSEXCELCOMMENT=5; // DWH 5-13-09
 	public final static int MSWORDDOCPROPERTIES=6; // DWH 5-25-09
 	private String outputPath;
 	private ZipFile zipOriginal;
 	private ZipOutputStream zipOut;
 	private byte[] buffer;
 	private LocaleId outLang;
 	private ZipEntry subDocEntry;
 	private IFilterWriter subDocWriter;
 	private OpenXMLContentSkeletonWriter subSkelWriter;
 	private File tempFile;
 	private File tempZip;
 	private YamlParameters params; // DWH 7-16-09
 	private static final Logger LOGGER = Logger.getLogger(OpenXMLZipFilterWriter.class.getName());
 
 	/**
 	 * Cancels processing of a filter; yet to be implemented.
 	 */
 	public void cancel () {
 		//TODO: implement cancel()
 	}
 	
 	/**
 	 * Closes the zip file.
 	 */
 	public void close () {
 		if ( zipOut == null ) return;
 		IOException err = null;
 		InputStream orig = null;
 		OutputStream dest = null;
 		try {
 			// Close the output
 			zipOut.close();
 			zipOut = null;
 
 			// If it was in a temporary file, copy it over the existing one
 			// If the IFilter.close() is called before IFilterWriter.close()
 			// this should allow to overwrite the input.
 			if ( tempZip != null ) {
 				dest = new FileOutputStream(outputPath);
 				orig = new FileInputStream(tempZip); 
 				int len;
 				while ( (len = orig.read(buffer)) > 0 ) {
 					dest.write(buffer, 0, len);
 				}
 			}
 			buffer = null;
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
 				if ( err != null ) {
 					throw new OkapiIOException("Error closing MS Office 2007 file.");
 				} else {
 					if ( tempZip != null ) {
 						tempZip.delete();
 						tempZip = null;
 					}
 				}
 			}
 		}
 	}
 
 	/**
 	 * Gets the name of the filter writer.
 	 */
 	public String getName () {
 		return "OpenXMLZipFilterWriter"; 
 	}
 
 	/**
 	 * Handles an event.  Passes all but START_DOCUMENT, END_DOCUMENT,
                * and DOCUMENT_PART to subdocument processing.
 	 * @param event the event to process
 	 */
 	public Event handleEvent (Event event) {
 		switch ( event.getEventType() ) {
 		case START_DOCUMENT:
 			processStartDocument((StartDocument)event.getResource());
 			break;
 		case DOCUMENT_PART:
 			processDocumentPart(event);
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
 		case TEXT_UNIT:
 		case START_GROUP:
 		case END_GROUP:
 /* now done in OpenXMLContentSkeletonWriter.getContent(TextUnit,LocaleId,int) DWH 10-27-09
 			if (event.getEventType()==EventType.TEXT_UNIT) // DWH 10-27-09 if in a Text box
 			{
 				TextUnit txu = (TextUnit) event.getResource();
 				Property prop = txu.getProperty("TextBoxLevel");
 				int nTextBoxLevel = 0;
 				if (prop!=null)
 				{
 					try
 					{
 						nTextBoxLevel = Integer.parseInt(prop.getValue());
 					}
 					catch(Exception e) {}
 				}
 				subSkelWriter.setNTextBoxLevel(nTextBoxLevel); // DWH 10-27-09 add getSkelWriter to GenericFilterWriter
 			}
 */
 			subDocWriter.handleEvent(event);
 			break;
 		case CANCELED:
 			break;
 		}
 		return event;
 	}
 
 	public void setOptions (LocaleId language,
 		String defaultEncoding)
 	{
 		outLang = language;
 	}
 
 	public void setOutput (String path) {
 		outputPath = path;
 	}
 
 	public void setOutput (OutputStream output) {
 		// Not supported for this filter
 		throw new UnsupportedOperationException(
 			"Method is not supported for this class.");
 	}
 
 	/**
 	 * Processes the start document for the whole zip file by
                * initializing a temporary output file, and and output stream.
 	 * @param res a resource for the start document
 	 */
 
 	private void processStartDocument (StartDocument res) {
 		try {
 			buffer = new byte[2048];
 			ZipSkeleton skel = (ZipSkeleton)res.getSkeleton();
 			zipOriginal = skel.getOriginal(); // if OpenXML filter was closed, this ZipFile has been marked for close
 			File fZip = new File(zipOriginal.getName()); // so get its name
 			zipOriginal = new ZipFile(fZip,ZipFile.OPEN_READ); // and re-open it
			
 			tempZip = null;
 			// Create the output stream from the path provided
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
 				tempZip = File.createTempFile("zfwTmpZip", null);
 				zipOut = new ZipOutputStream(new FileOutputStream(tempZip.getAbsolutePath()));
 			}
 			else { // Make sure the directory exists
 				Util.createDirectories(outputPath);
 				zipOut = new ZipOutputStream(new FileOutputStream(outputPath));
 			}
 		}
 		catch ( FileNotFoundException e ) {
 			throw new OkapiFileNotFoundException("Existing file could not be overwritten.");
 		}
 		catch ( IOException e ) {
 			throw new OkapiIOException("File could not be written.");
 		}
 	}
 	
 	private void processEndDocument () {
 		close();
 	}
 	
 	/**
 	 * This passes a file that doesn't need processing from the input zip
                * file to the output zip file.
 	 * @param event corresponding to the file to be passed through
 	 */
 	private void processDocumentPart (Event event) {
 		// Treat top-level ZipSkeleton events
 		String naym;
 		ZipEntry entree;
 		DocumentPart res = (DocumentPart)event.getResource();
 		if ( res.getSkeleton() instanceof ZipSkeleton ) {
 			ZipSkeleton skel = (ZipSkeleton)res.getSkeleton();
 			ZipEntry entry = skel.getEntry();
 			// Copy the entry data
 			try {
 				naym = entry.getName();
 				entree = new ZipEntry(naym);
 				zipOut.putNextEntry(entree);
 				InputStream input = zipOriginal.getInputStream(entry); 
 				int len;
 				while ( (len = input.read(buffer)) > 0 ) {
 					zipOut.write(buffer, 0, len);
 				}
 				input.close();
 				zipOut.closeEntry();
 			}
 			catch ( IOException e ) {
 				throw new OkapiIOException("Error writing zip file entry.");
 			}
 		}
 		else { // Otherwise it's a normal skeleton event
 			subDocWriter.handleEvent(event);
 		}
 	}
 
 	/**
 	 * Starts processing a new file withing the zip file.  It looks for the 
                * element type of "filetype" in the yaml parameters which need to
                * be set before handleEvent is called, and need to be the same as
                * the parameters on the START_SUBDOCUMENT event from the
                * OpenXMLFilter (by calling setParameters).  Once the type of the
                * file is discovered from the Parameters, a subdoc writer is 
                * created from OpenXMLContentSkeletonWriter, and a temporary
                * output file is created.
 	 * @param res resource of the StartSubDocument
 	 */
 	private void processStartSubDocument (StartSubDocument res) {
 		int nZipType = 0; // DWH 4-13-09
 		String sZipType; // DWH 4-13-09
 		
 		ZipSkeleton skel = (ZipSkeleton)res.getSkeleton();
 		subDocEntry = skel.getEntry();
 
 		// Set the temporary path and create it
 		try {
 			tempFile = File.createTempFile("zfwTmp", null);
 		}
 		catch ( IOException e ) {
 			throw new OkapiIOException("Error opening temporary zip output file.");
 		}
 		
 		// Instantiate the filter writer for that entry
 //		subDocWriter = new GenericFilterWriter(new GenericSkeletonWriter());
 /*	DWH 6-27-09 now cparams are included in the StartSubdocument
 		try // DWH 4-13-09 whole try/catch
 		{
 			sZipType = params.getTaggedConfig().getElementType("filetype");
 			if (sZipType.equals("MSWORD")) // DWH 4-13-09 whole if-else
 				nZipType = MSWORD;
 			else if (sZipType.equals("MSEXCEL"))
 				nZipType = MSEXCEL;
 			else if (sZipType.equals("MSPOWERPOINT"))
 				nZipType = MSPOWERPOINT;
 			else if (sZipType.equals("MSWORDCHART"))
 				nZipType = MSWORDCHART;
 		}
 		catch(Exception e)
 		{
 			nZipType = 0;
 			LOGGER.log(Level.WARNING,"Zip component is not a known file type.");
 		} // leave the zip type as 0 = unknown
 */
 		nZipType = ((ConditionalParameters)res.getFilterParameters()).nFileType; // DWH 6-27-09
 		subSkelWriter = new OpenXMLContentSkeletonWriter(nZipType); // DWH 10-27-09 subSkelWriter
 		subDocWriter = new GenericFilterWriter(subSkelWriter); // DWH 10-27-09
 		subDocWriter.setOptions(outLang, "UTF-8");
 		subDocWriter.setOutput(tempFile.getAbsolutePath());
 		
 		StartDocument sd = new StartDocument("sd");
 		sd.setLineBreak("\n");
 		sd.setSkeleton(res.getSkeleton());
 		subDocWriter.handleEvent(new Event(EventType.START_DOCUMENT, sd));
 	}
 	
 	/**
 	 * Finishes writing the subdocument temporary file, then adds it as an
                * entry in the temporary zip output file.
 	 * @param res resource of the end subdocument
 	 */
 	private void processEndSubDocument (Ending res) {
 		try {
 			// Finish writing the sub-document
 			subDocWriter.handleEvent(new Event(EventType.END_DOCUMENT, res));
 			subDocWriter.close();
 
 			// Create the new entry from the temporary output file
 			zipOut.putNextEntry(new ZipEntry(subDocEntry.getName()));
 			InputStream input = new FileInputStream(tempFile); 
 			int len;
 			while ( (len = input.read(buffer)) > 0 ) {
 				zipOut.write(buffer, 0, len);
 			}
 			input.close();
 			zipOut.closeEntry();
 			// Delete the temporary file
 			tempFile.delete();
 		}
 		catch ( IOException e ) {
 			throw new OkapiIOException("Error closing zip output file.");
 		}
 	}
 	public void setParameters(IParameters params) // DWH 7-16-09
 	{
 		this.params = (YamlParameters)params;
 	}
 	public YamlParameters getParameters() // DWH 7-16-09
 	{
 		return params;
 	}
 }
