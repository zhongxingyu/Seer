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
 
 package net.sf.okapi.common;
 
 /**
  * The type of events used when working with the pipeline and its
  * associated interfaces like IFilter, IFilterWriter, etc.
  */
 public enum EventType {
 
 	/**
 	 * Indicates the start of an input document.
 	 * A StartDocument resource should be associated with this event.  
 	 */
 	START_DOCUMENT,
 	
 	/**
 	 * Indicates the end of an input document.
 	 * An Ending resource should be associated with this event.  
 	 */
 	END_DOCUMENT,
 	
 	/**
 	 * Indicates the start of a sub-document.
 	 * A StartSubDocument resource should be associated with this event. 
 	 */
 	START_SUBDOCUMENT,
 	
 	/**
 	 * Indicates the end of a sub-document.
 	 * An Ending resource should be associated with this event.  
 	 */
 	END_SUBDOCUMENT,
 	
 	/**
 	 * Indicates the start of a group. For example, the start tag of the &lt;table> element in HTML.
 	 * A StartGroup resource should be associated with this event. 
 	 */
 	START_GROUP,
 	
 	/**
 	 * Indicates the end of a group.
 	 * An Ending resource should be associated with this event.  
 	 */
 	END_GROUP,
 	
 	/**
 	 * Indicates a text unit. For example, a paragraph in an HTML document.
 	 * A TextUnit resource should be associated with this event. 
 	 */
 	TEXT_UNIT,
 	
 	/**
 	 * Indicates a document part. Document parts are used to carry chunks of the input document that
 	 * have no translatable data, but may have properties.
 	 * A DocumentPart resource should be associated with this event. 
 	 */
 	DOCUMENT_PART,
 		
 	/**
	 * Indicates that the user has canceled the process.
 	 * No resource are associated with this event.  
 	 */
 	CANCELED,
 	
 	/**
 	 * Use by some pipelines to notify steps that events have been exhausted.
 	 * No resource are associated with this event.  
 	 */
 	FINISHED,
 	
 	/**
 	 * File level event. A File resource should be associated with this event.
 	 */
 	FILE_RESOURCE
 }
