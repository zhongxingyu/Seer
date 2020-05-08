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
 
 package net.sf.okapi.steps.rainbowkit.postprocess;
 
 import java.io.File;
 import java.util.List;
 import java.util.logging.Logger;
 
 import net.sf.okapi.common.Event;
 import net.sf.okapi.common.EventType;
 import net.sf.okapi.common.IParameters;
 import net.sf.okapi.common.IResource;
 import net.sf.okapi.common.LocaleId;
 import net.sf.okapi.common.MimeTypeMapper;
 import net.sf.okapi.common.Range;
 import net.sf.okapi.common.exceptions.OkapiBadFilterInputException;
 import net.sf.okapi.common.filters.IFilter;
 import net.sf.okapi.common.filters.IFilterConfigurationMapper;
 import net.sf.okapi.common.filterwriter.IFilterWriter;
 import net.sf.okapi.common.resource.ISegments;
 import net.sf.okapi.common.resource.Property;
 import net.sf.okapi.common.resource.RawDocument;
 import net.sf.okapi.common.resource.Segment;
 import net.sf.okapi.common.resource.TextContainer;
 import net.sf.okapi.common.resource.ITextUnit;
 import net.sf.okapi.common.resource.TextUnitUtil;
 import net.sf.okapi.filters.rainbowkit.Manifest;
 import net.sf.okapi.filters.rainbowkit.MergingInfo;
 
 public class Merger {
 
 	private static final Logger LOGGER = Logger.getLogger(Merger.class.getName());
 
 	private IFilter filter;
 	private IFilterWriter writer;
 	private Manifest manifest;
 	private LocaleId trgLoc;
 	private IFilterConfigurationMapper fcMapper;
 	private boolean skipEmptySourceEntries;
 	private boolean useSource;
 	private boolean preserveSegmentation;
 	
 	public Merger (Manifest manifest,
 		IFilterConfigurationMapper fcMapper,
 		boolean preserveSegmentation)
 	{
 		this.fcMapper = fcMapper;
 		this.manifest = manifest;
 		trgLoc = manifest.getTargetLocale();
 		this.preserveSegmentation = preserveSegmentation;
 	}
 	
 	public void close () {
 		if ( writer != null ) {
 			writer.close();
 			writer = null;
 		}
 		if ( filter != null ) {
 			filter.close();
 			filter = null;
 		}
 	}
 
 	public Event handleEvent (Event event) {
 		switch ( event.getEventType() ) {
 		case TEXT_UNIT:
 			processTextUnit(event);
 			break;
 		case END_DOCUMENT:
 			processEndDocument();
 			break;
 		}
 		return event;
 	}
 
 	public void startMerging (MergingInfo info) {
 		LOGGER.info("Merging: "+info.getRelativeInputPath());
 		// Create the filter for this original file
 		filter = fcMapper.createFilter(info.getFilterId(), filter);
 		if ( filter == null ) {
 			throw new OkapiBadFilterInputException(String.format("Filter cannot be created (%s).", info.getFilterId()));
 		}
 		IParameters fprm = filter.getParameters();
 		if ( fprm != null ) {
 			fprm.fromString(info.getFilterParameters());
 		}
 
 		File file = new File(manifest.getOriginalDirectory() + info.getRelativeInputPath());
 		RawDocument rd = new RawDocument(file.toURI(), info.getInputEncoding(),
 			manifest.getSourceLocale(), trgLoc);
 		
 		filter.open(rd);
 		writer = filter.createFilterWriter();
 		writer.setOptions(trgLoc, info.getTargetEncoding());
 		writer.setOutput(manifest.getMergeDirectory()+info.getRelativeTargetPath());
 		
 		// Skip entries with empty source for PO
 		skipEmptySourceEntries = ( info.getExtractionType().equals(Manifest.EXTRACTIONTYPE_PO)
 			|| info.getExtractionType().equals(Manifest.EXTRACTIONTYPE_TRANSIFEX) );
 		// Use the source of the input as the translation for XINI, etc.
 		//TO uncomment when ready useSource = info.getExtractionType().equals(Manifest.EXTRACTIONTYPE_ONTRAM);
 		
 		Event event = null;
 		if ( filter.hasNext() ) {
 			// Should be the start-document event
 			event = filter.next();
 		}
 		if (( event == null ) || ( event.getEventType() != EventType.START_DOCUMENT )) {
 			LOGGER.severe("The start document event is missing when parsing the original file.");
 			return;
 		}
 		writer.handleEvent(event);
 	}
 
 	private void processEndDocument () {
 		// Finish to go through the original file
 		while ( filter.hasNext() ) {
 			writer.handleEvent(filter.next());
 		}
 	}
 	
 	private void processTextUnit (Event event) {
 		// Get the unit from the translation file
 		ITextUnit traTu = event.getTextUnit();
 		// If it's not translatable:
 		if ( !traTu.isTranslatable() ) {
 			return; // Do nothing: the original will be handled by processUntilTextUnit()
 		}
 		// search for the corresponding event in the original
 		Event oriEvent = processUntilTextUnit();
 		if ( oriEvent == null ) {
 			LOGGER.severe(String.format("No corresponding text unit for id='%s' in the original file.",
 				traTu.getId()));
 			return;
 		}
 		// Get the actual trans-unit object of the original
 		ITextUnit oriTu = oriEvent.getTextUnit();
 
 		// Check the IDs
 		if ( !traTu.getId().equals(oriTu.getId()) ) {
 			LOGGER.severe(String.format("De-synchronized files: translated TU id='%s', Original TU id='%s'.",
 				traTu.getId(), oriTu.getId()));
 			return;
 		}
 		
 		// Check if we have a translation
 		TextContainer trgTraCont;
 		if ( useSource ) trgTraCont = traTu.getSource();
 		else trgTraCont = traTu.getTarget(trgLoc);
 		
 		if ( trgTraCont == null ) {
			if ( !oriTu.getSource().hasText() ) {
 				// Warn only if there source is not empty
 				LOGGER.warning(String.format("No translation found for TU id='%s'. Using source instead.", traTu.getId()));
 			}
 			writer.handleEvent(oriEvent); // Use the source
 			return;
 		}
 		
 		// Process the "approved" property
 		boolean isTransApproved = false;
 		Property traProp;
 		if ( useSource ) traProp = traTu.getSourceProperty(Property.APPROVED);
 		else traProp = traTu.getTargetProperty(trgLoc, Property.APPROVED);
 		
 		if ( traProp != null ) {
 			isTransApproved = traProp.getValue().equals("yes");
 		}
 		if ( !isTransApproved && manifest.getUseApprovedOnly() ) {
 			// Not approved: use the source
 			LOGGER.warning(String.format("Item id='%s': Target is not approved. Using source instead.", traTu.getId()));
 			writer.handleEvent(oriEvent); // Use the source
 			return;
 		}
 		
 		// Do we need to preserve the segmentation for merging (e.g. TTX case)
 		boolean mergeAsSegments = false;
 		if ( oriTu.getMimeType() != null ) { 
 			if ( oriTu.getMimeType().equals(MimeTypeMapper.TTX_MIME_TYPE)
 				|| oriTu.getMimeType().equals(MimeTypeMapper.XLIFF_MIME_TYPE) ) {
 				mergeAsSegments = true;
 			}
 		}
 		
 		// Set the container for the source
 		TextContainer srcOriCont = oriTu.getSource();
 
 		// If we do not need to merge segments then we must join all for the merge
 		// We also remember the ranges to set them back after merging
 		List<Range> srcRanges = null;
 		List<Range> trgRanges = null;
 		if ( !mergeAsSegments ) {
 			// Join only if needed
 			if ( !srcOriCont.contentIsOneSegment() ) {
 				srcRanges = srcOriCont.getSegments().getRanges();
 				srcOriCont.joinAll();
 			}
 			if ( !trgTraCont.contentIsOneSegment() ) {
 				trgRanges = trgTraCont.getSegments().getRanges();
 				trgTraCont.joinAll();
 			}
 		}
 		
 		// Perform the transfer of the inline codes
 		// At this point most formats will have the whole content in a single segment
 		// but we still work based on the segment(s) to handle the other cases
 		ISegments trgTraSegs = trgTraCont.getSegments();
 		for ( Segment srcOriSeg : srcOriCont.getSegments() ) {
 			Segment trgTraSeg = trgTraSegs.get(srcOriSeg.id);
 			if ( trgTraSeg == null ) {
 				LOGGER.warning(String.format("Item id='%s': No translation found for the segment '%s'. Using source instead.",
 					traTu.getId(), srcOriSeg.id));
 				// Use the source instead
 			}
 			else {
 				TextUnitUtil.adjustTargetCodes(srcOriSeg.text, trgTraSeg.text, true, true, null, oriTu);
 			}
 		}
 		// Check if the target has more segments
 		if ( srcOriCont.getSegments().count() < trgTraCont.getSegments().count() ) {
 			LOGGER.warning(String.format("Item id='%s': There is at least one extra segment in the translation file.\n"
 				+ "Extra segments are not merged into the translated output.",
 				traTu.getId()));
 		}
 		
 		oriTu.setTarget(trgLoc, trgTraCont);
 		
 		// Update/add the 'approved' flag of the entry to merge if available
 		// (for example to remove the fuzzy flag in POs or set the approved attribute in XLIFF)
 		if ( manifest.getUpdateApprovedFlag() ) {
 			Property oriProp = oriTu.createTargetProperty(trgLoc, Property.APPROVED, false, IResource.CREATE_EMPTY);
 			if ( traProp != null ) {
 				oriProp.setValue(traProp.getValue());
 			}
 			else {
 				oriProp.setValue("yes");
 			}
 		}
 		
 		writer.handleEvent(oriEvent);
 		
 		// Set back the segmentation if we modified it for the merge
 		if ( preserveSegmentation ) {
 			if ( srcRanges != null ) {
 				srcOriCont.getSegments().create(srcRanges);
 			}
 			if ( trgRanges != null ) {
 				trgTraCont.getSegments().create(trgRanges);
 			}
 		}
 	}
 
 	/**
 	 * Get events in the original document until the next text unit.
 	 * Any event before is passed to the writer.
 	 * @return the event of the next text unit, or null if no next text unit is found.
 	 */
 	private Event processUntilTextUnit () {
 		while ( filter.hasNext() ) {
 			Event event = filter.next();
 			if ( event.getEventType() == EventType.TEXT_UNIT ) {
 				ITextUnit tu = event.getTextUnit();
 				if ( !tu.isTranslatable() ) {
 					// Do not merge the translation for non-translatable
 					writer.handleEvent(event);
 					continue;
 				}
 				if ( skipEmptySourceEntries && tu.isEmpty() ) {
 					// For some types of package: Do not merge the translation for non-translatable
 					writer.handleEvent(event);
 					continue;
 				}
 				return event;
 			}
 			// Else: write out the event
 			writer.handleEvent(event);
 		}
 		// This text unit is extra in the translated file
 		//TODO: log error
 		return null;
 	}
 }
