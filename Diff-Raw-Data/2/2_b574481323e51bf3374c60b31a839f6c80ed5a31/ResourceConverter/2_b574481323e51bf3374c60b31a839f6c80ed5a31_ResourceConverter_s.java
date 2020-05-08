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
 ===========================================================================*/
 
 package net.sf.okapi.common.skeleton;
 
 import java.security.InvalidParameterException;
 import java.util.List;
 import java.util.logging.Logger;
 
 import net.sf.okapi.common.Event;
 import net.sf.okapi.common.EventType;
 import net.sf.okapi.common.IResource;
 import net.sf.okapi.common.ISkeleton;
 import net.sf.okapi.common.LocaleId;
 import net.sf.okapi.common.resource.DocumentPart;
 import net.sf.okapi.common.resource.IReferenceable;
 import net.sf.okapi.common.resource.MultiEvent;
 import net.sf.okapi.common.resource.StartDocument;
 import net.sf.okapi.common.resource.TextUnit;
 
 /**
  * Simplifies events, i.e. splits the generic skeleton of a given event resource into parts to contain no references.
  * The skeleton parts are attached to newly created DOCUMENT_PART events.
  * Original references are converted either to skeleton parts, or TEXT_UNIT events.
  * The sequence of DOCUMENT_PART and TEXT_UNIT events is packed into a single MULTI_EVENT event.
  */
 public class ResourceConverter {
 	private final Logger logger = Logger.getLogger(getClass().getName());
 	private boolean isMultilingual;
 	private LocaleId trgLoc;
 	private GenericSkeletonWriter writer;
 	private GenericSkeleton newSkel;
 	
 	public ResourceConverter(boolean isMultilingual, LocaleId trgLoc, String outEncoding) {
 		super();
 		this.isMultilingual = isMultilingual;
 		this.trgLoc = trgLoc;
 		writer = new GenericSkeletonWriter();
 		newSkel = new GenericSkeleton();
 //		StartDocument sd = new StartDocument("");
 //		sd.setMultilingual(false); // !!! 
 //		writer.processStartDocument(trgLoc, outEncoding, null, null, sd); // sets writer fields
 	}
 
 //	public void setMultilingual(boolean isMultilingual) {
 //		this.isMultilingual = isMultilingual;
 //	}
 //	
 //	public void setTargetLocale(LocaleId trgLoc) {
 //		this.trgLoc = trgLoc;
 //	}
 //	
 //	private void addEvent(MultiEvent me, Event event) {
 //		// TODO merge adjacent DocumentParts 
 //	}
 //	
 //	private void addDP(MultiEvent me, String id, GenericSkeleton skel) {
 //		DocumentPart dp = new DocumentPart(id, false, skel); 
 //		addEvent(me, new Event(EventType.DOCUMENT_PART, dp));
 //	}
 //	
 	
 	/**
 	 * Merges adjacent document parts into one. Will work for simple resources only.
 	 */	
 	public static MultiEvent packMultiEvent(MultiEvent me) {
 		Event prevEvent = null;
 		MultiEvent newME = new MultiEvent();
 		newME.setId(me.getId());
 		
 		for (Event event : me) {
 			if (prevEvent != null && 
 				event != null &&
 				prevEvent.getEventType() == EventType.DOCUMENT_PART && 
 				event.getEventType() == EventType.DOCUMENT_PART) {
 				
 				// Append to prev event's skeleton
 				IResource res = event.getResource(); 
 				ISkeleton skel = res.getSkeleton();
 				if (skel instanceof GenericSkeleton) {
 					IResource prevRes = prevEvent.getResource(); 
 					ISkeleton prevSkel = prevRes.getSkeleton();
 					if (prevSkel instanceof GenericSkeleton)
 						((GenericSkeleton) prevSkel).add((GenericSkeleton) skel);
 				}				
 			}
 			else {
 				newME.addEvent(event);
 				prevEvent = event;
 			}			
 		}
 		return newME;
 	}
 	
 	/**
 	 * Converts a given event into a multi-event if it contains references in its skeleton, or passes it on if 
 	 * either the skeleton is no instance of GenericSkeleton, contains no references, or the resource is referent.
 	 * @param event the given event
 	 * @return the given event or a newly created multi-event
 	 */
 	public Event convert(Event event) {
 		if (event == null)
 			throw new InvalidParameterException("Event cannot be null");
 		
 		IResource res = event.getResource();
 		if (res == null)
 			return event;
 		
 		ISkeleton skel = res.getSkeleton();
		if (!(skel == null)) {
 			return event;
 		}		
 		if (!(skel instanceof GenericSkeleton)) {
 			return event;
 		}
 		
 		if (res instanceof IReferenceable) {			
 			if  (((IReferenceable) res).isReferent()) {
 				writer.addToReferents(event);
 				// The referent is not processed at this point (only later from an event referencing it)
 				return event;
 			}
 		}
 		
 //		switch (event.getEventType()) {
 //		case START_DOCUMENT:
 //		case END_DOCUMENT:
 //		case START_SUBDOCUMENT:
 //		case END_SUBDOCUMENT:
 //		case START_GROUP:
 //		case END_GROUP:
 //		case TEXT_UNIT:
 //		case DOCUMENT_PART:
 //			break;
 //		default:
 //			return event; // All other events with a skeleton are not processed
 //		}
 		
 		// Process the resource's skeleton
 		MultiEvent me = new MultiEvent();
 		processResource(res, me);
 		
 		// Different event types are processed differently
 		switch (event.getEventType()) {
 		case START_DOCUMENT:
 			StartDocument sd = (StartDocument) res;
 			sd.setMultilingual(false); // Simple resources 
 			// No break here
 		case END_DOCUMENT:
 		case START_SUBDOCUMENT:
 		case END_SUBDOCUMENT:
 		case START_GROUP:
 		case END_GROUP:
 			// The original event (the skeleton should be deleted) precedes in the resulting multi-event DPs/TUs 
 			// created from its original skeleton parts
 			res.setSkeleton(null);
 			me.addEvent(event, 0);  
 			break;
 		case TEXT_UNIT:
 		case DOCUMENT_PART:
 			break;
 		default:
 			return event;
 		}
 								
 		return new Event(EventType.MULTI_EVENT, packMultiEvent(me));
 	}
 		
 //	public Event toMultiEvent(Event event, LocaleId targetLocale) {
 //		if (event == null)
 //			throw new InvalidParameterException("Event cannot be null");
 //		
 //		IResource res = event.getResource();
 //		if (res == null)
 //			return wrapEvent(event);
 //		
 //		ISkeleton skel = res.getSkeleton();
 //		if (!(skel instanceof GenericSkeleton)) {
 //			// TODO log
 //			return wrapEvent(event);
 //		}
 //		
 //		MultiEvent me = new MultiEvent(); 
 //		List<GenericSkeletonPart> parts = ((GenericSkeleton) skel).getParts();
 //		
 //		switch (event.getEventType()) {
 //		case TEXT_UNIT:
 //		}
 //	
 //		
 //		
 //		return wrapEvent(event); // TODO replace with real stuff		
 //	}
 //
 //	public static Event fromMultiEvent(Event event) {
 //		if (event == null)
 //			throw new InvalidParameterException("Event cannot be null");
 //		if (event.getEventType() != EventType.MULTI_EVENT)
 //			throw new InvalidParameterException("MULTI_EVENT type is expected");
 //		MultiEvent me = (MultiEvent) event.getResource();
 //		//if (me.iterator().)
 //		
 //		return null;		
 //	}
 //	
 //	private Event wrapEvent(Event event) {
 //		MultiEvent me = new MultiEvent();
 //		me.addEvent(event);
 //		return new Event(EventType.MULTI_EVENT, me);
 //	}
 	
 	private void flushSkeleton(String resId, int dpIndex, MultiEvent me) {
 		if (newSkel.isEmpty()) return;
 			
 		me.addEvent(new Event(EventType.DOCUMENT_PART, new DocumentPart(String.format("%s_%d", resId, dpIndex), false, newSkel)));
 		newSkel = new GenericSkeleton(); // newSkel.clear() would damage an already sent skeleton
 	}
 	
 	private void addTU(MultiEvent me, String resId, int tuIndex, TextUnit tu) {
 		String id = null;		
 		if (tuIndex == 1)
 			id = resId;
 		else {
 			logger.warning("Duplicate TU: " + resId);
 			id = String.format("%s_%d", resId, tuIndex);
 		}
 		
 		TextUnit newTU = tu.clone();
 		newTU.setId(id);
 		newTU.setSkeleton(null);
 		
 		me.addEvent(new Event(EventType.TEXT_UNIT, newTU));
 	}
 
 	
 	/**
 	 * Creates events from skeleton parts of a given resource, adds created events to a given multi-event resource.
 	 */
 	private void processResource(IResource resource, MultiEvent me) {
 		if (resource == null)
 			throw new InvalidParameterException("Resource cannot be null");
 		if (me == null)
 			throw new InvalidParameterException("MultiEvent object cannot be null");
 		
 		ISkeleton skel = resource.getSkeleton();
 		if (!(skel instanceof GenericSkeleton)) return;
 		List<GenericSkeletonPart> parts = ((GenericSkeleton) skel).getParts();
 		
 		int dpCounter = 0;
 		int tuCounter = 0;
 		String resId = resource.getId();
 //		if (resource instanceof INameable)
 //			mimeType = ((INameable) resource).getMimeType();
 		
 		for (GenericSkeletonPart part : parts) {
 			if (SkeletonUtil.isText(part)) {
 				//newSkel.add(part.toString());
 				newSkel.add(writer.getString(part, 1));
 			}				
 			else if (SkeletonUtil.isReference(part)) {
 				flushSkeleton(resId, ++dpCounter, me);				
 				
 				IReferenceable referent = writer.getReference(SkeletonUtil.getRefId(part));
 				if (referent instanceof IResource)
 					processResource((IResource) referent, me);
 			}
 			else if (SkeletonUtil.isSourcePlaceholder(part, resource)) {
 				processSourcePlaceholder(part, resource, me, resId, tuCounter, dpCounter);
 			}
 			else if (SkeletonUtil.isTargetPlaceholder(part, resource)) {
 				processTargetPlaceholder(part, resource, me, resId, tuCounter, dpCounter);
 			}
 			else if (SkeletonUtil.isValuePlaceholder(part, resource)) {
 				// For both isMultilingual true/false
 				newSkel.add(writer.getString(part, 1));
 			}
 			else if (SkeletonUtil.isExtSourcePlaceholder(part, resource)) {
 				checkExtParent(part.getParent(), resId);
 				processSourcePlaceholder(part, resource, me, resId, tuCounter, dpCounter);
 			}
 			else if (SkeletonUtil.isExtTargetPlaceholder(part, resource)) {
 				checkExtParent(part.getParent(), resId);
 				processTargetPlaceholder(part, resource, me, resId, tuCounter, dpCounter);
 			}
 			else if (SkeletonUtil.isExtValuePlaceholder(part, resource)) {
 				// For both isMultilingual true/false
 				checkExtParent(part.getParent(), resId);
 				newSkel.add(writer.getString(part, 1));
 			}
 		}
 		flushSkeleton(resId, ++dpCounter, me); // Flush remaining skeleton tail
 	}
 
 	private void processSourcePlaceholder(GenericSkeletonPart part, IResource resource, 
 			MultiEvent me, String resId, int tuCounter, int dpCounter) {
 		if (isMultilingual) {
 			if (part.parent instanceof TextUnit)
 				newSkel.add(writer.getContent((TextUnit) part.parent, null, 0)); // Source goes to skeleton
 			else {
 				logger.warning("The self-reference must be a text-unit: " + resId);
 				newSkel.add(part.parent.toString());
 			}
 		}
 		else {
 			flushSkeleton(resId, ++dpCounter, me);
 			addTU(me, resId, ++tuCounter, (TextUnit) resource);
 		}
 	}	
 	
 	private void processTargetPlaceholder(GenericSkeletonPart part, IResource resource, 
 			MultiEvent me, String resId, int tuCounter, int dpCounter) {
 		// For both isMultilingual true/false
 		if (part.getLocale() == trgLoc) {
 			flushSkeleton(resId, ++dpCounter, me);
 			addTU(me, resId, ++tuCounter, (TextUnit) resource);
 		}
 		else {
 			newSkel.add(writer.getContent((TextUnit) resource, trgLoc, 1));
 		}
 	}
 	
 	private boolean checkExtParent(IResource parent, String resId) {
 		if (parent instanceof IReferenceable) {
 			IReferenceable r = (IReferenceable) parent;
 			if (!r.isReferent()) {
 				logger.warning("Referent flag is not set in parent: " + resId);
 				return false;
 			}
 			return true;
 		}
 		else {
 			logger.warning("Invalid parent type: " + resId);
 			return false;
 		}
 	}
 }
