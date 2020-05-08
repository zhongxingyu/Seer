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
 
 package net.sf.okapi.steps.msbatchtranslation;
 
 import java.util.ArrayList;
 import java.util.Hashtable;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import net.sf.okapi.common.Event;
 import net.sf.okapi.common.EventType;
 import net.sf.okapi.common.IParameters;
 import net.sf.okapi.common.IResource;
 import net.sf.okapi.common.LocaleId;
 import net.sf.okapi.common.UsingParameters;
 import net.sf.okapi.common.Util;
 import net.sf.okapi.common.annotation.AltTranslationsAnnotation;
 import net.sf.okapi.common.filterwriter.TMXWriter;
 import net.sf.okapi.common.pipeline.BasePipelineStep;
 import net.sf.okapi.common.pipeline.annotations.StepParameterMapping;
 import net.sf.okapi.common.pipeline.annotations.StepParameterType;
 import net.sf.okapi.common.query.QueryResult;
 import net.sf.okapi.common.resource.ISegments;
 import net.sf.okapi.common.resource.ITextUnit;
 import net.sf.okapi.common.resource.MultiEvent;
 import net.sf.okapi.common.resource.Segment;
 import net.sf.okapi.common.resource.TextContainer;
 import net.sf.okapi.common.resource.TextFragment;
 import net.sf.okapi.common.resource.TextUnitUtil;
 import net.sf.okapi.connectors.microsoft.MicrosoftMTConnector;
 
 @UsingParameters(Parameters.class)
 public class MSBatchTranslationStep extends BasePipelineStep {
 
 	private final Logger logger = LoggerFactory.getLogger(getClass());
 	
 	private final static int MAXEVENTS = 20;
 	
 	private Parameters params;
 	private TMXWriter tmxWriter;
 	private LinkedList<Event> events;
 	private int maxEvents = MAXEVENTS;
 	private MicrosoftMTConnector conn;
 	private LocaleId sourceLocale;
 	private LocaleId targetLocale;
 	private String rootDir;
 	private String inputRootDir;
 	private Map<String, String> attributes;
 	private boolean needReset;
 
 	public MSBatchTranslationStep () {
 		params = new Parameters();
 	}
 	
 	private void closeAndClean () {
 		if ( tmxWriter != null ) {
 			tmxWriter.writeEndDocument();
 			tmxWriter.close();
 			tmxWriter = null;
 		}
 		if ( events != null ) {
 			events.clear();
 			events = null;
 		}
 	}
 	
 	@Override
 	public String getDescription () {
 		return "Annotates text units with Microsoft Translator matches or/and creates a TM from them."
 			+ " Expects: filter events. Sends back: filter events.";
 	}
 
 	@Override
 	public String getName () {
 		return "Microsoft Batch Translation";
 	}
 
 	@StepParameterMapping(parameterType = StepParameterType.SOURCE_LOCALE)
 	public void setSourceLocale (LocaleId sourceLocale) {
 		this.sourceLocale = sourceLocale;
 	}
 	
 	@StepParameterMapping(parameterType = StepParameterType.TARGET_LOCALE)
 	public void setTargetLocale (LocaleId targetLocale) {
 		this.targetLocale = targetLocale;
 	}
 	
 	@StepParameterMapping(parameterType = StepParameterType.ROOT_DIRECTORY)
 	public void setRootDirectory (String rootDir) {
 		this.rootDir = rootDir;
 	}
 	
 	@StepParameterMapping(parameterType = StepParameterType.INPUT_ROOT_DIRECTORY)
 	public void setInputRootDirectory (String inputRootDir) {
 		this.inputRootDir = inputRootDir;
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
 	protected Event handleStartBatch (Event event) {
 		events = new LinkedList<Event>();
 		maxEvents = params.getMaxEvents();
 		if (( maxEvents < 1 ) || ( maxEvents > 1000 )) maxEvents = MAXEVENTS;
 		
 		// Initialize the engine
 		conn = new MicrosoftMTConnector();
 		net.sf.okapi.connectors.microsoft.Parameters prm = (net.sf.okapi.connectors.microsoft.Parameters)conn.getParameters();
 		prm.setClientId(params.getClientId());
 		prm.setSecret(params.getSecret());
		prm.setCategory(params.getCategory());
 		conn.setLanguages(sourceLocale, targetLocale);
 		conn.setMaximumHits(params.getMaxMatches());
 		conn.setThreshold(params.getThreshold());
 		
 		// Create the TMX output if requested
 		if ( params.getMakeTmx() ) {
 			String tmxOutputPath = Util.fillRootDirectoryVariable(params.getTmxPath(), rootDir);
 			tmxOutputPath = Util.fillInputRootDirectoryVariable(tmxOutputPath, inputRootDir);
 			tmxOutputPath = LocaleId.replaceVariables(tmxOutputPath, sourceLocale, targetLocale);
 			tmxWriter = new TMXWriter(tmxOutputPath);
 			tmxWriter.writeStartDocument(sourceLocale, targetLocale, getClass().getCanonicalName(),
 				"1", "sentence", null, "unknown");
 		
 			// Set the attributes to write in the TMX
 			attributes = new Hashtable<String, String>();
 			if ( params.getMarkAsMT() ) {
 				attributes.put("creationid", Util.MTFLAG);
 			}
 			attributes.put("Txt::Origin", "Microsoft-Translator");
 		}
 		
 		return event;
 	}
 
 	@Override
 	public Event handleEvent (Event event) {
 		switch ( event.getEventType() ) {
 		case START_BATCH:
 			return handleStartBatch(event);
 		// Events to store until the next trigger
 		case TEXT_UNIT:
 		case DOCUMENT_PART:
 		case START_GROUP:
 		case END_GROUP:
 		case START_SUBFILTER:
 		case END_SUBFILTER:
 			// Store and possibly trigger
 			return storeAndPossiblyProcess(event, false);
 		// Events that force the trigger if needed
 		case CUSTOM:
 		case MULTI_EVENT:
 		case START_SUBDOCUMENT: // Could have text units between start document and sub-document
 		case END_DOCUMENT:
 		case END_SUBDOCUMENT:
 			return storeAndPossiblyProcess(event, true);
 		// Events that should clean up
 		case CANCELED:
 		case END_BATCH:
 			closeAndClean();
 			break;
 			// Events before any storing or after triggers	
 		case START_BATCH_ITEM:
 		case END_BATCH_ITEM:
 		case RAW_DOCUMENT:
 		case START_DOCUMENT:
 			break; // Do nothing special
 		}
 		return event;
 	}
 	
 	private Event processEvents () {
 		// Do the translations
 		getTranslations();
 		// Translations are done
 		// Now we sent all the stored events down the pipeline
 		needReset = true; // To reset the list next time around
 		return new Event(EventType.MULTI_EVENT, new MultiEvent(events));
 	}
 	
 	private Event storeAndPossiblyProcess (Event event,
 		boolean mustProcess)
 	{
 		// Reset if needed
 		if ( needReset ) {
 			needReset = false;
 			events.clear();
 		}
 		// Add the event
 		events.add(event);
 		// And trigger the process if needed
 		if ( mustProcess || ( events.size() >= maxEvents )) {
 			return processEvents();
 		}
 		// Else, if we just store this event, we pass a no-operation event down for now
 		return Event.NOOP_EVENT;
 	}
 	
 	private void getTranslations () {
 		if ( events.isEmpty() ) {
 			return; // Nothing to do
 		}
 	
 		// Process the text unit to leverage
 		ArrayList<TextFragment> fragments = new ArrayList<TextFragment>();
 		ArrayList<String> segIds = new ArrayList<String>();
 		ArrayList<String> tuIds = new ArrayList<String>();
 		
 		// Gather the text fragment to translate
 		for ( Event event : events ) {
 			if ( event.isTextUnit() ) {
 				ITextUnit tu = event.getTextUnit();
 				// Skip non-translatable entries
 				if ( !tu.isTranslatable() ) continue;
 				
 				TextContainer trgCont = tu.getTarget(targetLocale);
 				if ( tu.getSource().hasBeenSegmented() ) {
 					ISegments trgSegs = null;
 					if ( trgCont != null ) trgSegs = trgCont.getSegments();
 					for ( Segment srcSeg : tu.getSourceSegments() ) {
 						// Skip segments without text
 						if ( !srcSeg.text.hasText() ) {
 							continue;
 						}
 						// Skip segments with existing candidate if requested
 						if ( params.getOnlyWhenWithoutCandidate() && hasExistingCandidate(srcSeg, trgSegs) ) {
 							continue;
 						}
 						// Add the segment to the list of source
 						fragments.add(srcSeg.text);
 						tuIds.add(tu.getId());
 						segIds.add(tu.getId()+"\f"+srcSeg.getId());
 					}
 				}
 				else { // Not segmented: Look at container-level
 					TextFragment srcFrag = tu.getSource().getFirstContent();
 					if ( !srcFrag.hasText() ) {
 						continue;
 					}
 					// Skip entry with existing candidate if required
 					if ( params.getOnlyWhenWithoutCandidate() && hasExistingCandidate(trgCont) ) {
 						continue;
 					}
 					// Add the text to the list of source (it may be more than one sentence)
 					fragments.add(srcFrag);
 					tuIds.add(tu.getId());
 					segIds.add(null);
 				}
 				
 			}
 		}
 		
 		// Nothing to translate
 		if ( fragments.isEmpty() ) {
 			return;
 		}
 		// Call the translation engine
 		List<List<QueryResult>> list = conn.batchQuery(fragments);
 		if ( Util.isEmpty(list) ) {
 			logger.warn("No translation generated.");
 			return;
 		}
 		
 		// Place the translations
 		int entryIndex = 0;
 		for ( Event event : events ) {
 			// Skip non-text-unit events
 			if ( !event.isTextUnit() ) continue;
 			// Skip non-translatable entries
 			ITextUnit tu = event.getTextUnit();
 			if ( !tu.isTranslatable() ) continue;
 			
 			TextContainer trgCont = tu.getTarget(targetLocale);
 			if ( tu.getSource().hasBeenSegmented() ) {
 				// Process all the segments for this text unit				
 				for ( Segment srcSeg : tu.getSourceSegments() ) {
 					if ( !srcSeg.text.hasText() ) {
 						continue;
 					}
 					// Skip until we reach next segment that was translated
 					if ( !segIds.get(entryIndex).equals(tu.getId()+"\f"+srcSeg.getId()) ) continue;
 					
 					// Go through the results for that source
 					List<QueryResult> resList = list.get(entryIndex);
 					entryIndex++; // For next time
 					boolean firstMatch = true;
 					for ( QueryResult res : resList ) {
 						// Write to TMX if needed
 						if ( tmxWriter != null ) {
 							tmxWriter.writeTU(res.source, res.target, null, attributes);
 						}
 						// Determine if we fill the target (first match and it is above threshold) 
 						boolean fill = false;
 						if ( firstMatch && params.getFillTarget() ) {
 							fill = (res.getCombinedScore() >= params.getFillTargetThreshold());
 						}
 						
 						if ( fill || params.getAnnotate() ) {
 							// Create the target container if needed
 							if ( trgCont == null ) {
 								trgCont = tu.createTarget(targetLocale, false, IResource.COPY_SEGMENTATION);
 							}
 							Segment trgSeg = trgCont.getSegments().get(srcSeg.id);
 							if ( trgSeg == null ) {
 								trgSeg = new Segment(srcSeg.id);
 								trgCont.getSegments().append(trgSeg);
 							}
 
 							// Annotate if requested
 							if ( params.getAnnotate() ) {
 								TextUnitUtil.addAltTranslation(trgSeg,
 									res.toAltTranslation(srcSeg.text, sourceLocale, targetLocale));
 							}
 								
 							// Fill the target if requested
 							if ( fill ) {
 								// If not empty we do not fill (no overwriting)
 								if ( trgSeg.text.isEmpty() ) {
 									trgSeg.text.setCodedText(res.target.getCodedText(), res.target.getClonedCodes());
 								}
 							}
 						}
 						// Next entry will not be the first
 						firstMatch = false;
 					}
 				}				
 			}
 			else { // Not segmented: work at the container level
 				if ( !tu.getSource().getFirstContent().hasText() ) {
 					continue;
 				}
 				// Skip TUs until we reach the next one that was translated
 				if ( !tuIds.get(entryIndex).equals(tu.getId()) ) continue;
 				
 				// Go through the results for that source
 				List<QueryResult> resList = list.get(entryIndex);
 				entryIndex++; // For next time
 				boolean firstMatch = true;
 				for ( QueryResult res : resList ) {
 					// Write to TMX if needed
 					if ( tmxWriter != null ) {
 						tmxWriter.writeTU(res.source, res.target, null, attributes);
 					}
 					// Determine if we fill the target (first match and it is above threshold) 
 					boolean fill = false;
 					if ( firstMatch && params.getFillTarget() ) {
 						fill = (res.getCombinedScore() >= params.getFillTargetThreshold());
 					}
 					
 					if ( fill || params.getAnnotate() ) {
 						// Create the target container if needed
 						if ( trgCont == null ) {
 							trgCont = tu.createTarget(targetLocale, false, IResource.COPY_SEGMENTATION);
 						}
 						// Annotate if requested
 						if ( params.getAnnotate() ) {
 							TextFragment srcFrag = tu.getSource().getFirstContent();
 							TextUnitUtil.addAltTranslation(trgCont,
 								res.toAltTranslation(srcFrag, sourceLocale, targetLocale));
 						}
 						// Fill the target if requested
 						if ( fill ) {
 							TextFragment trgFrag = trgCont.getFirstContent();
 							// If not empty we do not fill (no overwriting)
 							if ( trgFrag.isEmpty() ) {
 								trgFrag.setCodedText(res.target.getCodedText(), res.target.getClonedCodes());
 							}
 						}
 					}
 					// Next entry will not be the first
 					firstMatch = false;
 				}
 			}
 		}		
 		
 //		// Place back the translations
 //		// We need to do the same loop as when gathering the strings
 //		// as we assume the results are in the same order
 //		int entryIndex = 0;
 //		for ( Event event : events ) {
 //			// Skip non-text-unit events
 //			if ( !event.isTextUnit() ) continue;
 //
 //			ITextUnit tu = event.getTextUnit();
 //			// Skip non-translatable entries
 //			if ( !tu.isTranslatable() ) continue;
 //			
 //			TextContainer trgCont = tu.getTarget(targetLocale);
 //			
 //			for ( Segment srcSeg : tu.getSourceSegments() ) {
 //				if ( !srcSeg.text.hasText() ) continue;
 //				if ( list.size() < entryIndex-1 ) {
 //					logger.warn(String.format("Discrepancy between the number of source and translations for text unit id='%s'", tu.getId()));
 //					continue;
 //				}
 //				Segment trgSeg = null;
 //
 //				// Go through the matches for that segment
 //				List<QueryResult> resList = list.get(entryIndex);
 //				entryIndex++; // For next time
 //				boolean firstMatch = true;
 //				for ( QueryResult res : resList ) {
 //					// Determine if we fill the target (first match and it is above threshold) 
 //					boolean fill = false;
 //					if ( firstMatch && params.getFillTarget() ) {
 //						fill = (res.score >= params.getFillTargetThreshold());
 //					}
 //					
 //					// Get hold of the target segment where to annotate or fill
 //					// Re-use the same within this loop
 //					if (( trgSeg == null ) && ( fill || params.getAnnotate() )) {
 //						if ( trgCont == null ) {
 //							trgCont = tu.createTarget(targetLocale, false, IResource.COPY_SEGMENTATION);
 //						}
 //						trgSeg = trgCont.getSegments().get(srcSeg.id);
 //						if ( trgSeg == null ) {
 //							trgSeg = new Segment(srcSeg.id);
 //							trgCont.getSegments().append(trgSeg);
 //						}
 //						else { // Is it empty?
 //							// If not empty we do not fill (no overwriting)
 //							fill = trgSeg.text.isEmpty();
 //						}
 //					}
 //
 //					// Write to TMX if needed
 //					if ( tmxWriter != null ) {
 //						tmxWriter.writeTU(res.source, res.target, null, attributes);
 //					}
 //					
 //					// Annotate if requested
 //					if ( params.getAnnotate() ) {
 //						TextUnitUtil.addAltTranslation(trgSeg,
 //							res.toAltTranslation(srcSeg.text, sourceLocale, targetLocale));
 //					}
 //						
 //					// Fill the target if requested
 //					if ( fill ) {
 //						trgSeg.text = res.target;
 //					}
 //					
 //					firstMatch = false;
 //				}
 //			}
 //		}
 		
 		// We are done: the translations are annotations
 		// and/or written out in the TMX output
 	}
 	
 	private boolean hasExistingCandidate (Segment srcSeg,
 		ISegments trgSegs)
 	{
 		if ( trgSegs == null ) return false; // No target at all
 		
 		Segment trgSeg = trgSegs.get(srcSeg.getId());
 		if ( trgSeg == null ) return false; // No target segment
 
 		// Do we have the annotation?
 		AltTranslationsAnnotation ann = trgSeg.getAnnotation(AltTranslationsAnnotation.class);
 		if ( ann == null ) return false; // No AltTranslationsAnnotation
 		
 		// Do we have at least one entry
 		return (ann.getFirst() != null);
 	}
 	
 	private boolean hasExistingCandidate (TextContainer frag) {
 		if ( frag == null ) return false; // No target at all
 		
 		// Do we have the annotation?
 		AltTranslationsAnnotation ann = frag.getAnnotation(AltTranslationsAnnotation.class);
 		if ( ann == null ) return false; // No AltTranslationsAnnotation
 		
 		// Do we have at least one entry
 		return (ann.getFirst() != null);
 	}
 	
 }
