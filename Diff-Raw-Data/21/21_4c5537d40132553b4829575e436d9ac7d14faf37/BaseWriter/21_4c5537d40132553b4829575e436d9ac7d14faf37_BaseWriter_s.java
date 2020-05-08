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
 ============================================================================*/
 
 package net.sf.okapi.applications.rainbow.packages;
 
 import net.sf.okapi.common.IParameters;
 import net.sf.okapi.common.Util;
 import net.sf.okapi.common.annotation.ScoresAnnotation;
 import net.sf.okapi.common.encoder.EncoderManager;
 import net.sf.okapi.common.filterwriter.TMXWriter;
 import net.sf.okapi.common.LocaleId;
 import net.sf.okapi.common.resource.AltTransAnnotation;
 import net.sf.okapi.common.resource.Property;
 import net.sf.okapi.common.resource.TextContainer;
 import net.sf.okapi.common.resource.TextUnit;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.OutputStream;
 
 public abstract class BaseWriter implements IWriter {
 
 	protected Manifest manifest;
 	protected int docID;
 	protected String inputRoot;
 	protected String relativeWorkPath;
 	protected String relativeSourcePath;
 	protected String relativeTargetPath;
 	protected String sourceEncoding;
 	protected String targetEncoding;
 	protected String filterID;
 	protected TMXWriter tmxWriterApproved;
 	protected String tmxPathApproved;
 	protected TMXWriter tmxWriterUnApproved;
 	protected String tmxPathUnApproved;
 	protected TMXWriter tmxWriterAlternate;
 	protected String tmxPathAlternate;
 	protected TMXWriter tmxWriterLeverage;
 	protected String tmxPathLeverage;
 	protected LocaleId trgLoc;
 	protected String encoding;
 	protected String outputPath;
 	protected boolean preSegmented;
 	protected String creationTool;
 	protected EncoderManager encoderManager;
 	
 	public BaseWriter () {
 		manifest = new Manifest();
 		manifest.setReaderClass(getReaderClass());
 	}
 	
 	public void cancel () {
 		//TODO: implement cancel()
 	}
 	
 	public void setInformation (LocaleId sourceLocale,
 		LocaleId targetLocale,
 		String projectID,
 		String outputFolder,
 		String packageID,
 		String sourceRoot,
 		boolean preSegmented,
 		String creationTool)
 	{
 		manifest.setSourceLanguage(sourceLocale);
 		trgLoc = targetLocale;
 		manifest.setTargetLanguage(trgLoc);
 		manifest.setProjectID(projectID);
 		manifest.setRoot(outputFolder);
 		manifest.setPackageID(packageID);
 		manifest.setPackageType(getPackageType());
 		this.inputRoot = sourceRoot;
 		this.preSegmented = preSegmented;
 		this.creationTool = creationTool;
 	}
 
 	public void setEncoderManager (EncoderManager encoderManager) {
 		this.encoderManager = encoderManager;
 	}
 	
 	public void writeStartPackage () {
 		// Create the root directory
 		Util.createDirectories(manifest.getRoot());
 		
 		String tmp = manifest.getSourceLocation();
 		if (( tmp != null ) && ( tmp.length() > 0 )) {
 			Util.createDirectories(manifest.getRoot() + File.separator + tmp + File.separator);
 		}
 		
 		tmp = manifest.getTargetLocation();
 		if (( tmp != null ) && ( tmp.length() > 0 )) {
 			Util.createDirectories(manifest.getRoot() + File.separator + tmp + File.separator);
 		}
 
 		tmp = manifest.getOriginalLocation();
 		if (( tmp != null ) && ( tmp.length() > 0 )) {
 			Util.createDirectories(manifest.getRoot() + File.separator + tmp + File.separator);
 		}
 
 		// No need to create the folder structure for the 'done' folder
 		// It will be done when merging
 		
 		// Create the reference TMX (approved pre-translations found in the source files)
 		if ( tmxPathApproved == null ) {
 			tmxPathApproved = manifest.getRoot() + File.separator + "approved.tmx";
 		}
 		tmxWriterApproved = new TMXWriter(tmxPathApproved);
 		tmxWriterApproved.writeStartDocument(manifest.getSourceLanguage(),
 			manifest.getTargetLanguage(), creationTool, null, null, null, null);
 
 		// Create the reference TMX (un-approved pre-translations found in the source files)
 		if ( tmxPathUnApproved == null ) {
 			tmxPathUnApproved = manifest.getRoot() + File.separator + "unapproved.tmx";
 		}
 		tmxWriterUnApproved = new TMXWriter(tmxPathUnApproved);
 		tmxWriterUnApproved.writeStartDocument(manifest.getSourceLanguage(),
 			manifest.getTargetLanguage(), creationTool, null, null, null, null);
 
 		// Create the reference TMX (alternate found in the source files)
 		if ( tmxPathAlternate == null ) {
 			tmxPathAlternate = manifest.getRoot() + File.separator + "alternate.tmx";
 		}
 		tmxWriterAlternate = new TMXWriter(tmxPathAlternate);
 		tmxWriterAlternate.writeStartDocument(manifest.getSourceLanguage(),
 			manifest.getTargetLanguage(), creationTool, null, null, null, null);
 
 		// Create the reference TMX (pre-translation TM)
 		if ( tmxPathLeverage == null ) {
 			tmxPathLeverage = manifest.getRoot() + File.separator + "leverage.tmx";
 		}
 		tmxWriterLeverage = new TMXWriter(tmxPathLeverage);
 		tmxWriterLeverage.writeStartDocument(manifest.getSourceLanguage(),
 			manifest.getTargetLanguage(), creationTool, null, null, null, null);
 	}
 
 	public void writeEndPackage (boolean createZip) {
 		try {
 			// Save the manifest
 			if ( manifest != null ) {
 				manifest.Save();
 			}
			if ( createZip ) {
				// Zip the package if needed
                //TODO: What if manifest is null?
				Compression.zipDirectory(manifest.getRoot(), manifest.getRoot() + ".zip");
			}
 	
 			tmxWriterApproved.writeEndDocument();
 			tmxWriterApproved.close();
 			if ( tmxWriterApproved.getItemCount() == 0 ) {
 				File file = new File(tmxPathApproved);
 				file.delete();
 			}
 
 			tmxWriterUnApproved.writeEndDocument();
 			tmxWriterUnApproved.close();
 			if ( tmxWriterUnApproved.getItemCount() == 0 ) {
 				File file = new File(tmxPathUnApproved);
 				file.delete();
 			}
 
 			tmxWriterAlternate.writeEndDocument();
 			tmxWriterAlternate.close();
 			if ( tmxWriterAlternate.getItemCount() == 0 ) {
 				File file = new File(tmxPathAlternate);
 				file.delete();
 			}
 
 			tmxWriterLeverage.writeEndDocument();
 			tmxWriterLeverage.close();
 			if ( tmxWriterLeverage.getItemCount() == 0 ) {
 				File file = new File(tmxPathLeverage);
 				file.delete();
 			}
 		}
 		catch ( IOException e ) {
 			throw new RuntimeException(e);
 		}
 	}
 	
 	public void createOutput (int docID,
 		String relativeSourcePath,
 		String relativeTargetPath,
 		String sourceEncoding,
 		String targetEncoding,
 		String filterID,
 		IParameters filterParams)
 	{
 		if ( relativeSourcePath == null ) throw new NullPointerException();
 		if ( relativeTargetPath == null ) throw new NullPointerException();
 		if ( sourceEncoding == null ) throw new NullPointerException();
 		if ( targetEncoding == null ) throw new NullPointerException();
 		if ( filterID == null ) throw new NullPointerException();
 
 		this.docID = docID;
 		this.relativeSourcePath = relativeSourcePath;
 		this.relativeTargetPath = relativeTargetPath;
 		this.sourceEncoding = sourceEncoding;
 		this.targetEncoding = targetEncoding;
 		this.filterID = filterID;
 		
 		// If needed copy the original input to the package
 		String subFolder = manifest.getOriginalLocation();
 		if (( subFolder == null ) || ( subFolder.length() == 0 )) return;
 				
 		String inputPath = inputRoot + File.separator + relativeSourcePath;
 		String docPrefix = String.format("%d.", docID);
 			
 		String destination = manifest.getRoot() + File.separator + subFolder
 			+ File.separator + docPrefix + "ori"; // docPrefix has a dot
 		Util.copyFile(inputPath, destination, false);
 			
 		String paramsCopy = manifest.getRoot() + File.separator + subFolder
 			+ File.separator + docPrefix + "fprm";
 		if ( filterParams != null ) {
 			filterParams.save(paramsCopy);
 		}
 		
 		// Set the options for the actual writer
 		String outputPath = manifest.getRoot() + File.separator
 			+ ((manifest.getSourceLocation().length() == 0 ) ? "" : (manifest.getSourceLocation() + File.separator)) 
 			+ relativeWorkPath;
 		setOptions(trgLoc, targetEncoding);
 		setOutput(outputPath);
 	}
 
 	public void createCopies (int docID,
 		String relativeSourcePath)
 	{
 		if ( relativeSourcePath == null ) throw new NullPointerException();
 
 		String inputPath = inputRoot + File.separator + relativeSourcePath;
 		String outputPath = manifest.getRoot() + File.separator
 			+ ((manifest.getSourceLocation().length() == 0 ) ? "" : (manifest.getSourceLocation() + File.separator)) 
 			+ relativeSourcePath;
 
 		// If needed copy the original input to the package
 		String subFolder = manifest.getOriginalLocation();
 		if (( subFolder != null ) && ( subFolder.length() > 0 )) {
 			String docPrefix = String.format("%d.", docID);
 			String destination = manifest.getRoot() + File.separator + subFolder
 				+ File.separator + docPrefix + "ori"; // docPrefix has a dot
 			Util.copyFile(inputPath, destination, false);
 		}
 			
 		// Copy to the work folder
 		Util.createDirectories(outputPath);
 		Util.copyFile(inputPath, outputPath, false);
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
 		throw new UnsupportedOperationException();
 	}
 
 	public void writeTMXEntries (TextUnit tu) {
 		// Write the items in the TM if needed
 		TextContainer tc = tu.getTarget(trgLoc);
 		if (( tc != null ) && ( !tc.isEmpty() )) {
 			if ( tu.getSourceContent().isEmpty() ||
 				( tu.getSourceContent().hasText(false) && !tc.hasText(false) )) {
 				return; // Target has code and/or spaces only
 			}
 			boolean done = false;
 			if ( tu.hasTargetProperty(trgLoc, Property.APPROVED) ) {
 				if ( tu.getTargetProperty(trgLoc, Property.APPROVED).getValue().equals("yes") ) {
 					tmxWriterApproved.writeItem(tu, null);
 					done = true;
 				}
 			}
 			if ( !done ) {
 				ScoresAnnotation scores = tu.getTarget(trgLoc).getAnnotation(ScoresAnnotation.class);
 				if ( scores != null ) {
 					writeScoredItem(tu, scores);
 				}
 				else {
 					tmxWriterUnApproved.writeItem(tu, null);
 				}
 			}
 		}
 
 		// Check for alternates
 		AltTransAnnotation alt = tu.getAnnotation(AltTransAnnotation.class);
 		if ( alt != null ) {
 			alt.startIteration();
 			while ( alt.moveToNext() ) {
 				TextUnit altTu = alt.getEntry();
 				tmxWriterAlternate.writeItem(altTu, null, true);
 			}
 		}
 	}
 
 	public void writeScoredItem (TextUnit item,
 		ScoresAnnotation scores)
 	{
 		// By default, matches go in the leverage TM
 		tmxWriterLeverage.writeItem(item, null);
 	}
 
 }
