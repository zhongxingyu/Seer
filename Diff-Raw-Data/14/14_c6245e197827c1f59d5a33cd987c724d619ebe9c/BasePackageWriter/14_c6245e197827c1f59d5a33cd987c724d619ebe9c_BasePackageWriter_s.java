 /*===========================================================================
   Copyright (C) 2010-2011 by the Okapi Framework contributors
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
 
 package net.sf.okapi.steps.rainbowkit.common;
 
 import java.io.File;
 import java.io.OutputStream;
 import java.util.logging.Logger;
 
 import net.sf.okapi.common.Event;
 import net.sf.okapi.common.IParameters;
 import net.sf.okapi.common.LocaleId;
 import net.sf.okapi.common.Util;
 import net.sf.okapi.common.annotation.AltTranslation;
 import net.sf.okapi.common.annotation.AltTranslationsAnnotation;
 import net.sf.okapi.common.encoder.EncoderManager;
 import net.sf.okapi.common.filters.FilterConfigurationMapper;
 import net.sf.okapi.common.filterwriter.TMXWriter;
 import net.sf.okapi.common.resource.ISegments;
 import net.sf.okapi.common.resource.Property;
 import net.sf.okapi.common.resource.Segment;
 import net.sf.okapi.common.resource.TextContainer;
 import net.sf.okapi.common.resource.TextFragment;
 import net.sf.okapi.common.resource.ITextUnit;
 import net.sf.okapi.common.skeleton.ISkeletonWriter;
 import net.sf.okapi.filters.rainbowkit.Manifest;
 import net.sf.okapi.filters.rainbowkit.MergingInfo;
 import net.sf.okapi.steps.rainbowkit.creation.Parameters;
 
 public abstract class BasePackageWriter implements IPackageWriter {
 
 	protected final Logger logger = Logger.getLogger(getClass().getName());
 
 	protected Parameters params;
 	protected Manifest manifest;
 	protected int docId;
 	protected String extractionType;
 	protected ISkeletonWriter skelWriter;
 	
 	protected TMXWriter tmxWriterApproved;
 	protected String tmxPathApproved;
 	protected String tempTmxPathApproved;
 	
 	protected TMXWriter tmxWriterUnApproved;
 	protected String tmxPathUnApproved;
 	protected String tempTmxPathUnApproved;
 	
 	protected TMXWriter tmxWriterAlternates;
 	protected String tmxPathAlternates;
 	protected String tempTmxPathAlternates;
 	
 	protected TMXWriter tmxWriterLeverage;
 	protected String tmxPathLeverage;
 	protected String tempTmxPathLeverage;
 	
 	protected boolean copiedTargetsLikeApproved = false;
 	protected boolean useLetterCodes = false;
 	protected boolean zeroBasedLetterCodes = true;
 	
 	public BasePackageWriter (String extractionType) {
 		this.extractionType = extractionType;
 		manifest = new Manifest();
 	}
 	
 	@Override
 	public IParameters getParameters () {
 		return params;
 	}
 
 	@Override
 	public void setParameters (IParameters params) {
 		this.params = (Parameters)params;
 	}
 	
 	@Override
 	public void setBatchInformation (String packageRoot,
 		LocaleId srcLoc,
 		LocaleId trgLoc,
 		String inputRoot,
 		String packageId,
 		String projectId,
 		String creatorParams,
 		String tempPackageRoot)
 	{
 		manifest.setInformation(packageRoot, srcLoc, trgLoc, inputRoot,
 			packageId, projectId, creatorParams, tempPackageRoot);
 	}
 
 	public String getMainOutputPath () {
 		return manifest.getPath();
 	}
 	
 	@Override
 	public void cancel () {
 		// TODO
 	}
 
 	@Override
 	public EncoderManager getEncoderManager () {
 		// Not used
 		return null;
 	}
 
 	@Override
 	public ISkeletonWriter getSkeletonWriter () {
 		return null;
 	}
 
 	@Override
 	public Event handleEvent (Event event) {
 		switch ( event.getEventType() ) {
 		case START_BATCH:
 			processStartBatch();
 			break;
 		case END_BATCH:
 			processEndBatch();
 			break;
 		case START_BATCH_ITEM:
 			processStartBatchItem();
 			break;
 		case RAW_DOCUMENT:
 			processRawDocument(event);
 			break;
 		case END_BATCH_ITEM:
 			processEndBatchItem();
 			break;
 		case START_DOCUMENT:
 			processStartDocument(event);
 			break;
 		case END_DOCUMENT:
 			processEndDocument(event);
 			break;
 		case START_SUBDOCUMENT:
 			processStartSubDocument(event);
 			break;
 		case END_SUBDOCUMENT:
 			processEndSubDocument(event);
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
 		case DOCUMENT_PART:
 			processDocumentPart(event);
 			break;
 		}
 		// This writer is not supposed to change the event, so we return the same
 		return event;
 	}
 
 	@Override
 	public void setOptions (LocaleId locale,
 		String defaultEncoding)
 	{
 		throw new UnsupportedOperationException("Use setDocumentInformation instead.");
 	}
 
 	@Override
 	public void setOutput (String path) {
 		throw new UnsupportedOperationException("Use setDocumentInformation instead.");
 	}
 
 	@Override
 	public void setOutput (OutputStream output) {
 		throw new UnsupportedOperationException("Output to stream not supported for now");
 	}
 
 	protected void processStartBatch () {
 		docId = 0;
 		initializeTMXWriters();
 	}
 	
 	protected void setTMXInfo (boolean generate,
 		String pathApproved,
 		boolean useLetterCodes,
 		boolean zerobasedletterCodes)
 	{
 		this.useLetterCodes = useLetterCodes;
 		this.zeroBasedLetterCodes = zerobasedletterCodes;
 		if ( !generate ) {
 			tmxPathApproved = null;
 			tmxPathUnApproved = null;
 			tmxPathAlternates = null;
 			tmxPathLeverage = null;
 			return;
 		}
 		
 		if ( pathApproved == null ) {
 			if ( tmxPathApproved == null ) {
 				tmxPathApproved = manifest.getTmDirectory() + "approved.tmx";
 				tempTmxPathApproved = manifest.getTmDirectory() + "approved.tmx";
 			}
 		}
 		else {
 			tmxPathApproved = pathApproved;
 			//TOFIX: Case of overridden approved TMX not supported if tempPackageRoot is not the package root
 			tempTmxPathApproved = pathApproved;
 		}
 		
 		if ( tmxPathUnApproved == null ) {
 			tmxPathUnApproved = manifest.getTmDirectory() + "unapproved.tmx";
 			tempTmxPathUnApproved = manifest.getTmDirectory() + "unapproved.tmx";
 		}
 		
 		if ( tmxPathAlternates == null ) {
 			tmxPathAlternates = manifest.getTmDirectory() + "alternates.tmx";
 			tempTmxPathAlternates = manifest.getTmDirectory() + "alternates.tmx";
 		}
 		
 		if ( tmxPathLeverage == null ) {
 			tmxPathLeverage = manifest.getTmDirectory() + "leverage.tmx";
 			tempTmxPathLeverage = manifest.getTmDirectory() + "leverage.tmx";
 		}
 		
 	}
 	
 	protected void initializeTMXWriters () {
 		if ( tmxPathApproved != null ) {
 			tmxWriterApproved = new TMXWriter(tempTmxPathApproved);
 			tmxWriterApproved.setLetterCodedMode(useLetterCodes, zeroBasedLetterCodes);
 			tmxWriterApproved.writeStartDocument(manifest.getSourceLocale(),
 				manifest.getTargetLocale(), getClass().getName(), null, null, null, null);
 		}
 
 		if ( tmxPathUnApproved != null ) {
 			tmxWriterUnApproved = new TMXWriter(tempTmxPathUnApproved);
 			tmxWriterUnApproved.setLetterCodedMode(useLetterCodes, zeroBasedLetterCodes);
 			tmxWriterUnApproved.writeStartDocument(manifest.getSourceLocale(),
 				manifest.getTargetLocale(), getClass().getName(), null, null, null, null);
 		}
 
 		if ( tmxPathAlternates != null ) {
 			tmxWriterAlternates = new TMXWriter(tempTmxPathAlternates);
 			tmxWriterAlternates.setLetterCodedMode(useLetterCodes, zeroBasedLetterCodes);
 			tmxWriterAlternates.writeStartDocument(manifest.getSourceLocale(),
 				manifest.getTargetLocale(), getClass().getName(), null, null, null, null);
 		}
 
 		if ( tmxPathLeverage != null ) {
 			tmxWriterLeverage = new TMXWriter(tempTmxPathLeverage);
 			tmxWriterLeverage.setLetterCodedMode(useLetterCodes, zeroBasedLetterCodes);
 			tmxWriterLeverage.writeStartDocument(manifest.getSourceLocale(),
 				manifest.getTargetLocale(), getClass().getName(), null, null, null, null);
 		}
 	}
 
 	protected void processEndBatch () {
 		if ( params.getOutputManifest() ) {
 			manifest.save(null);
 		}
 
 		if ( tmxWriterApproved != null ) {
 			tmxWriterApproved.writeEndDocument();
 			tmxWriterApproved.close();
 			if ( tmxWriterApproved.getItemCount() == 0 ) {
 				File file = new File(tempTmxPathApproved);
 				file.delete();
 			}
 		}
 		
 		if ( tmxWriterUnApproved != null ) {
 			tmxWriterUnApproved.writeEndDocument();
 			tmxWriterUnApproved.close();
 			if ( tmxWriterUnApproved.getItemCount() == 0 ) {
 				File file = new File(tempTmxPathUnApproved);
 				file.delete();
 			}
 		}
 
 		if ( tmxWriterAlternates != null ) {
 			tmxWriterAlternates.writeEndDocument();
 			tmxWriterAlternates.close();
 			if ( tmxWriterAlternates.getItemCount() == 0 ) {
 				File file = new File(tempTmxPathAlternates);
 				file.delete();
 			}
 		}
 		
 		if ( tmxWriterLeverage != null ) {
 			tmxWriterLeverage.writeEndDocument();
 			tmxWriterLeverage.close();
 			if ( tmxWriterLeverage.getItemCount() == 0 ) {
 				File file = new File(tempTmxPathLeverage);
 				file.delete();
 			}
 		}
		close();
 	}
 
 	protected void processStartBatchItem () {
 		// Do nothing by default
 	}
 
 	protected void processEndBatchItem () {
 		// Do nothing by default
 	}
 	
 	protected void processRawDocument (Event event) {
 		String ori = manifest.getOriginalDirectory();
 		if ( Util.isEmpty(ori) ) return; // No copy to be done
 		
 		// Else: copy the original
 		MergingInfo info = manifest.getItem(docId);
 		String inputPath = manifest.getInputRoot() + info.getRelativeInputPath();
 		String outputPath = ori + info.getRelativeInputPath();
 		Util.copyFile(inputPath, outputPath, false);
 	}
 
 	@Override
 	public void setDocumentInformation (String relativeInputPath,
 		String filterConfigId,
 		String filterParameters,
 		String inputEncoding,
 		String relativeTargetPath,
 		String targetEncoding,
 		ISkeletonWriter skelWriter)
 	{
 		if ( Util.isEmpty(filterConfigId) ) {
 			manifest.addDocument(++docId, Manifest.EXTRACTIONTYPE_NONE, relativeInputPath, "", filterParameters,
 				inputEncoding, relativeTargetPath, targetEncoding);
 		}
 		else {
 			this.skelWriter = skelWriter;
 			String res[] = FilterConfigurationMapper.splitFilterFromConfiguration(filterConfigId);
 			manifest.addDocument(++docId, extractionType, relativeInputPath, res[0], filterParameters,
 				inputEncoding, relativeTargetPath, targetEncoding);
 		}
 	}
 	
 	protected void processStartDocument (Event event) {
 		String ori = manifest.getOriginalDirectory();
 		if ( Util.isEmpty(ori) ) return; // No copy to be done
 		
 		// Else: copy the original
 		MergingInfo info = manifest.getItem(docId);
 		String inputPath = manifest.getInputRoot() + info.getRelativeInputPath();
 		String outputPath = ori + info.getRelativeInputPath();
 		Util.copyFile(inputPath, outputPath, false);
 	}
 
 	protected void processEndDocument (Event event) {
 		// Do nothing by default
 	}
 
 	protected void processStartSubDocument (Event event) {
 		// Do nothing by default
 	}
 
 	protected void processEndSubDocument (Event event) {
 		// Do nothing by default
 	}
 
 	protected void processStartGroup (Event event) {
 		// Do nothing by default
 	}
 
 	protected void processEndGroup (Event event) {
 		// Do nothing by default
 	}
 
 	protected void processDocumentPart (Event event) {
 		// Do nothing by default
 	}
 
 	protected abstract void processTextUnit (Event event);
 
 	protected void writeTMXEntries (ITextUnit tu) {
 		// Check if we have a target
 		LocaleId trgLoc = manifest.getTargetLocale();
 		TextContainer tc = tu.getTarget(trgLoc);
 		if ( tc == null ) {
 			return; // No target
 		}
 		if ( !tu.getSource().hasText(false) ) {
 			return; // Empty or no-text source
 		}
 		
 		// Process translation(s) in the container itself (if there is one)
 		boolean done = false;
 		if ( !tc.isEmpty() ) {
 			if ( tu.hasTargetProperty(trgLoc, Property.APPROVED) ) {
 				if ( tu.getTargetProperty(trgLoc, Property.APPROVED).getValue().equals("yes") ) {
 					// Write existing translation that was approved
 					if ( tmxWriterApproved != null ) {
 						tmxWriterApproved.writeItem(tu, null);
 						done = true;
 					}
 				}
 			}
 			if ( !done ) {
 				// If un-approved and source == target: don't count it as a translation
 				if ( tu.getSource().compareTo(tc, true) != 0 ) {
 					// Write existing translation not yet approved
 					if ( tmxWriterUnApproved != null ) {
 						tmxWriterUnApproved.writeItem(tu, null);
 						done = true;
 					}
 				}
 			}
 		}
 		
 		// Look for annotations
 		// In each segment
 		ISegments srcSegs = tu.getSource().getSegments();
 		for ( Segment seg : tc.getSegments() ) {
 			Segment srcSeg = srcSegs.get(seg.id);
 			if ( srcSeg == null ) continue;
 			writeAltTranslations(seg.getAnnotation(AltTranslationsAnnotation.class), srcSeg.text);
 		}
 		// In the target container
 		TextFragment srcOriginal;
 		if ( tu.getSource().contentIsOneSegment() ) {
 			srcOriginal = tu.getSource().getFirstContent();
 		}
 		else {
 			srcOriginal = tu.getSource().getUnSegmentedContentCopy();
 		}
 		writeAltTranslations(tc.getAnnotation(AltTranslationsAnnotation.class), srcOriginal);
 
 	}
 
 	private void writeAltTranslations (AltTranslationsAnnotation ann,
 		TextFragment srcOriginal)
 	{
 		if ( ann == null ) {
 			return;
 		}
 		for ( AltTranslation alt : ann ) {
 			if ( alt.getFromOriginal() ) {
 				// If it's coming from the original it's a true alternate (e.g. XLIFF one)
 				if ( tmxWriterAlternates != null ) {
 					tmxWriterAlternates.writeAlternate(alt, srcOriginal);
 				}
 			}
 			else {
 				// Otherwise the translation is from a leveraging step
 				if ( tmxWriterLeverage != null ) {
 					tmxWriterLeverage.writeAlternate(alt, srcOriginal);
 				}
 			}
 		}
 	}
 	
 }
