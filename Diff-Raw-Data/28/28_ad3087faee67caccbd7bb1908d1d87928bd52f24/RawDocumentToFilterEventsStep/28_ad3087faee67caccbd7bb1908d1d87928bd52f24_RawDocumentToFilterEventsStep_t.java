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
 
 package net.sf.okapi.steps.common;
 
 import net.sf.okapi.common.Event;
 import net.sf.okapi.common.EventType;
 import net.sf.okapi.common.Util;
 import net.sf.okapi.common.filters.IFilter;
 import net.sf.okapi.common.pipeline.BasePipelineStep;
 import net.sf.okapi.common.resource.RawDocument;
 
 /**
  * Converts a {@link RawDocument} into filter events.
  * This class implements the {@link net.sf.okapi.common.pipeline.IPipelineStep}
  * interface for a step that takes a {@link RawDocument} and uses a provided 
  * {@link IFilter} implementation to generate its corresponding events.
  * @see FilterEventsToRawDocumentStep
  * @see FilterEventsWriterStep 
  */
 public class RawDocumentToFilterEventsStep extends BasePipelineStep {
 	
 	private IFilter filter;
 	private boolean isDone;
 
 	/**
 	 * Creates a new RawDocumentToFilterEventsStep object.
 	 * This constructor is needed to be able to instantiate an object from newInstance()
 	 */
 	public RawDocumentToFilterEventsStep () {
 	}
 	
 	/**
 	 * Sets the filter for this RawDocumentToEventsStep object.
 	 * @param filter the filter to use.
 	 */
 	public void setFilter (IFilter filter) {
 		this.filter = filter;
 	}
 
 	public String getName() {
 		return "RawDocument to Filter Events";
 	}
 	
 	public String getDescription () {
 		return "Convert a RawDocument into filter events.";
 	}
 	
 	@Override
 	public Event handleEvent (Event event) {
 		switch ( event.getEventType() ) {
 		case START_BATCH:
 			isDone = true;
 			break;
 
 		case START_BATCH_ITEM:
 			// Needed because the process() method of the pipeline expects
 			// hasEvents to be set to true to prime things.
 			isDone = false;
 			return event;
 		
 		// Initialize the filter on RAW_DOCUMENT
 		case RAW_DOCUMENT:
 			if ( Util.isEmpty(getContext().getFilterConfigurationId(0)) ) {
 				// No filter configuration provided: just pass it down
 				isDone = true;
 				return event;
 			}
			// Else: Get the filter to use
			filter = getContext().getFilterConfigurationMapper().createFilter(
				getContext().getFilterConfigurationId(0), filter);
			if ( filter == null ) {
				throw new RuntimeException("Unsupported filter type.");
 			}
			isDone = false;
 			// Open the document
 			filter.open((RawDocument)event.getResource());
			// Return the first event from the filter
			return filter.next();
 		}
 
 		if ( isDone ) {
 			return event;
 		}
 		else {
 			// Get events from the filter
 			Event e = filter.next();
 			if ( e.getEventType() == EventType.END_DOCUMENT) {
 				// END_DOCUMENT is the end of this raw document
 				isDone = true;
 			}
 			return e;
 		}
 	}
 
 	@Override
 	public boolean isDone() {
 		return isDone;
 	}
 
 	public void destroy() {
 		filter.close();
 	}
 
 	public void cancel() {
 		filter.cancel();
 	}
 	
 }
