 /*===========================================================================
   Copyright (C) 2008-2009 by the Okapi Framework contributors
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
 
 package net.sf.okapi.applications.rainbow.utilities.merging;
 
 import java.io.BufferedOutputStream;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.OutputStreamWriter;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import net.sf.okapi.applications.rainbow.lib.FilterAccess;
 import net.sf.okapi.applications.rainbow.lib.Utils;
 import net.sf.okapi.applications.rainbow.packages.IReader;
 import net.sf.okapi.applications.rainbow.packages.Manifest;
 import net.sf.okapi.applications.rainbow.packages.ManifestItem;
 import net.sf.okapi.common.Event;
 import net.sf.okapi.common.EventType;
 import net.sf.okapi.common.IResource;
 import net.sf.okapi.common.Util;
 import net.sf.okapi.common.filters.IFilter;
 import net.sf.okapi.common.filterwriter.IFilterWriter;
 import net.sf.okapi.common.resource.Code;
 import net.sf.okapi.common.resource.RawDocument;
 import net.sf.okapi.common.resource.TextContainer;
 import net.sf.okapi.common.resource.TextUnit;
 import net.sf.okapi.filters.rtf.RTFFilter;
 
 public class Merger {
 
 	private final Logger logger = Logger.getLogger(getClass().getName());
 
 	private Manifest manifest;
 	private IReader reader;
 	private FilterAccess fa;
 	private IFilter inpFilter;
 	private IFilterWriter outFilter;
 	private RTFFilter rtfFilter;
 	private String trgLang;
 
 	public Merger () {
 		fa = new FilterAccess();
 
 		// Get the location of the class source
 		File file = new File(getClass().getProtectionDomain().getCodeSource().getLocation().getFile());
 	    String rootFolder = file.getAbsolutePath();
 	    // Remove the JAR file if running an installed version
 	    if ( rootFolder.endsWith(".jar") ) rootFolder = Util.getDirectoryName(rootFolder);
 	    // Remove the application folder in all cases
 	    rootFolder = Util.getDirectoryName(rootFolder);
 		String sharedFolder = Utils.getOkapiSharedFolder(rootFolder);
 
 		// Load the FilterAccess list
 		fa.loadList(sharedFolder + File.separator + "filters.xml");
 	}
 
 	public void initialize (Manifest manifest) {
 		// Close any previous reader
 		if ( reader != null ) {
 			reader.closeDocument();
 			reader = null;
 		}
 		// Set the manifest and the options
 		this.manifest = manifest;
 		trgLang = manifest.getTargetLanguage();
 	}
 	
 	public void execute (int docId) {
 		ManifestItem item = manifest.getItem(docId);
 		// Skip items not selected for merge
 		if ( !item.selected() ) return;
 
 		// Merge or convert depending on the post-processing selected
 		if ( item.getPostProcessingType().equals(ManifestItem.POSPROCESSING_TYPE_RTF) ) {
 			convertFromRTF(docId, item);
 		}
 		else { // Default: use the reader-driven process
 			merge(docId, item);
 		}
 	}
 	
 	private void convertFromRTF (int docId,
 		ManifestItem item)
 	{
 		OutputStreamWriter writer = null;
 		try {
 			// File to convert
 			String fileToConvert = manifest.getFileToMergePath(docId);
 
 			// Instantiate the reader if needed
 			if ( rtfFilter == null ) {
 				rtfFilter = new RTFFilter();
 			}
 
 			logger.info("\nConverting: " + fileToConvert);
 			
 			//TODO: get LB info from original
 			String lineBreak = Util.LINEBREAK_DOS;
 			
 			// Open the RTF input
 			File f = new File(fileToConvert);
 			//TODO: gusse encoding based on language
 			rtfFilter.open(new RawDocument(f.toURI(), "windows-1252", manifest.getTargetLanguage()));
 				
 			// Open the output document
 			// Initializes the output
 			String outputFile = manifest.getFileToGeneratePath(docId);
 			Util.createDirectories(outputFile);
 			writer = new OutputStreamWriter(new BufferedOutputStream(
 				new FileOutputStream(outputFile)), item.getOutputEncoding());
 			//TODO: check BOM option from original
 			Util.writeBOMIfNeeded(writer, false, item.getOutputEncoding());
 				
 			// Process
 			StringBuilder buf = new StringBuilder();
 			while ( rtfFilter.getTextUntil(buf, -1, 0) == 0 ) {
 				writer.write(buf.toString());
 				writer.write(lineBreak);
 			}
 			
 		}		
 		catch ( Exception e ) {
 			// Log and move on to the next file
 			Throwable e2 = e.getCause();
 			logger.log(Level.SEVERE, "Conversion error. " + ((e2!=null) ? e2.getMessage() : e.getMessage()), e);
 		}
 		finally {
 			if ( rtfFilter != null ) {
 				rtfFilter.close();
 			}
 			if ( writer != null ) {
 				try {
 					writer.close();
 				}
 				catch ( IOException e ) {
 					logger.log(Level.SEVERE, "Conversion error when closing file. " + e.getMessage(), e);
 				}
 			}
 		}
 	}
 	
 	private void merge (int docId,
 		ManifestItem item)
 	{
 		try {
 			// File to merge
 			String fileToMerge = manifest.getFileToMergePath(docId);
 			// Instantiate a package reader of the proper type
 			if ( reader == null ) {
 				reader = (IReader)Class.forName(manifest.getReaderClass()).newInstance();
 			}
 			logger.info("\nMerging: " + fileToMerge);
 
 			// Original and parameters files
 			String originalFile = manifest.getRoot() + File.separator + manifest.getOriginalLocation()
 				+ File.separator + String.format("%d.ori", docId);
 			String paramsFile = manifest.getRoot() + File.separator + manifest.getOriginalLocation()
 				+ File.separator + String.format("%d.fprm", docId);
 			// Load the relevant filter
 			inpFilter = fa.loadFilter(item.getFilterID(), paramsFile, inpFilter);
 			
 			reader.openDocument(fileToMerge, manifest.getSourceLanguage(), manifest.getTargetLanguage());
 			
 			// Initializes the input
 			File f = new File(originalFile);
 			inpFilter.open(new RawDocument(f.toURI(), item.getInputEncoding(),
 				manifest.getSourceLanguage(), trgLang));
 			
 			// Initializes the output
 			String outputFile = manifest.getFileToGeneratePath(docId);
 			Util.createDirectories(outputFile);
 			outFilter = inpFilter.createFilterWriter();
 			outFilter.setOptions(trgLang, item.getOutputEncoding());
 			outFilter.setOutput(outputFile);
 			
 			// Process the document
 			Event event;
 			while ( inpFilter.hasNext() ) {
 				event = inpFilter.next();
 				if ( event.getEventType() == EventType.TEXT_UNIT ) {
 					processTextUnit((TextUnit)event.getResource());
 				}
 				outFilter.handleEvent(event);
 			}
 		}
 		catch ( Exception e ) {
 			// Log and move on to the next file
 			Throwable e2 = e.getCause();
 			logger.log(Level.SEVERE, "Merging error. " + ((e2!=null) ? e2.getMessage() : e.getMessage()), e);
 		}
 		finally {
 			if ( reader != null ) {
 				reader.closeDocument();
 				reader = null;
 			}
 			if ( inpFilter != null ) {
 				inpFilter.close();
 				inpFilter = null;
 			}
 			if ( outFilter != null ) {
 				outFilter.close();
 				outFilter = null;
 			}
 		}
 	}
 
 	private void processTextUnit (TextUnit tu) {
 		// Skip the non-translatable
 		// This means the translate attributes must be the same
 		// in the original and the merging files
 		if ( !tu.isTranslatable() ) return;
 
 		// Get item from the package document
 		// Skip also the read-only ones
 		TextUnit tuFromTrans;
 		while ( true ) {
 			if ( !reader.readItem() ) {
 				// Problem: 
 				logger.log(Level.WARNING,
 					String.format("There is no more items in the package to merge with id=\"%s\".", tu.getId()));
 				// Keep the source
 				return;
 			}
 			tuFromTrans = reader.getItem();
 			if ( !tuFromTrans.isTranslatable() ) continue;
 			else break; // Found next translatable (and likely translated) item
 		}
 			
 		if ( !tu.getId().equals(tuFromTrans.getId()) ) {
 			// Problem: different IDs
 			logger.warning(String.format("ID mismatch: original item id=\"%s\" package item id=\"%s\".",
 				tu.getId(), tuFromTrans.getId()));
 			// Keep the source
 			return;
 		}
 
 		if ( !tuFromTrans.hasTarget(trgLang) ) {
 			// No translation in package
 			if ( !tu.isEmpty() ) {
 				logger.log(Level.WARNING,
 					String.format("Item id=\"%s\": No translation provided.", tu.getId()));
 				tu.setTarget(trgLang, tu.getSource());
 			}
 		}
 
 //		boolean approved = false;
 //		Property prop = tu.getTargetProperty(trgLang, Property.APPROVED);
 //		if (( prop != null ) && prop.getValue().equals("yes") ) {
 //			approved = true;
 //		}
 
 		// Get the translated target, and unsegment it if needed
 		TextContainer fromTrans = tuFromTrans.getTarget(trgLang);
 		if ( fromTrans.isSegmented() ) {
 			fromTrans.mergeAllSegments();
 		}
 
 		// We create a new target if needed
 		TextContainer trgCont = tu.createTarget(trgLang, false, IResource.COPY_ALL);
 
 		// Adjust the codes to use the appropriate ones
 		List<Code> transCodes = fromTrans.getCodes();
 		// Use the ones in translated target, but if empty, take it from source
 		transCodes = transferCodes(transCodes, tu.getSourceContent().getCodes(), tu);
 		
 		// Now set the target coded text and the target codes
 		try {
 			trgCont.setCodedText(fromTrans.getCodedText(), transCodes, false);
 		}
 		catch ( RuntimeException e ) {
 			logger.log(Level.SEVERE,
 				String.format("Inline code error with item id=\"%s\".\n" + e.getLocalizedMessage(), tu.getId()));
 			// Use the source instead, continue the merge
 			tu.setTarget(trgLang, tu.getSource());
 		}
 	}
 
 	/*
 	 * Check the codes in the translated entry, use the original data if there is
 	 * none in the code coming from XLIFF, and generate non-stoping error if
 	 * non-deletable codes are missing.
 	 */
 	private List<Code> transferCodes (List<Code> transCodes,
 		List<Code> oriCodes,
 		TextUnit tu)
 	{
 		// Check if we have at least one code
		if ( transCodes.size() == 0 ) return transCodes;
 		
 		int[] oriIndices = new int[oriCodes.size()];
 		for ( int i=0; i<oriIndices.length; i++ ) oriIndices[i] = i;
 		int done = 0;
 		
 		Code transCode, oriCode;
 		for ( int i=0; i<transCodes.size(); i++ ) {
 			transCode = transCodes.get(i);
 			transCode.setOuterData(null); // Remove XLIFF outer codes
 
 			// Get the data from the original code (match on id)
 			oriCode = null;
 			for ( int j=0; j<oriIndices.length; j++ ) {
 				if ( oriIndices[j] == -1) continue; // Used already
 				if ( oriCodes.get(oriIndices[j]).getId() == transCode.getId() ) {
 					oriCode = oriCodes.get(oriIndices[j]);
 					oriIndices[j] = -1;
 					done++;
 					break;
 				}
 			}
 			if ( oriCode == null ) { // Not found in original (extra in target)
 				if (( transCode.getData() == null )
 					|| ( transCode.getData().length() == 0 )) {
 					// Leave it like that
 					logger.warning(String.format("The extra target code id='%d' does not have corresponding data (item id='%s')",
 						transCode.getId(), tu.getId()));
 				}
 			}
 			else { // Get the data from the original
 				if (( transCode.getData() == null )
 					|| ( transCode.getData().length() == 0 )) {
 					transCode.setData(oriCode.getData());
 				}
 				transCode.setReferenceFlag(oriCode.hasReference());
 			}
 		}
 		
 		// If needed, check for missing codes in translation
 		if ( oriCodes.size() > done ) {
 			// Any index > -1 in source means it was was deleted in target
 			for ( int i=0; i<oriIndices.length; i++ ) {
 				if ( oriIndices[i] != -1 ) {
 					Code code = oriCodes.get(oriIndices[i]);
 					if ( !code.isDeleteable() ) {
 						logger.severe(String.format("The non-deletable code id='%d' (%s) is missing in target (item id='%s')",
 							code.getId(), code.getData(), tu.getId()));
 						logger.info("Source='"+tu.getSource().toString()+"'");
 					}
 				}
 			}
 		}
 		
 		return transCodes;
 	}
 	
 }
