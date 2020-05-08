 /*===========================================================================
   Copyright (C) 2009-2010 by the Okapi Framework contributors
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
 
 package net.sf.okapi.common.resource;
 
 /**
  * Implement a special content part that is a segment.
 * A segment is a {@link #TextPart} with an identifier.
  */
 public class Segment extends TextPart {
 	
 	/**
 	 * Identifier of this segment.
 	 */
 	public String id;
 	
 	/**
 	 * Creates an empty Segment object with a null identifier.
 	 */
 	public Segment () {
 		super(new TextFragment());
 	}
 	
 	/**
 	 * Creates a Segment object with a given identifier and a given
 	 * text fragment.
 	 * @param id identifier for the new segment (Can be null).
 	 * @param text text fragment for the new segment.
 	 */
 	public Segment (String id,
 		TextFragment text)
 	{
 		super(text);
 		this.id = id;
 	}
 
 	@Override
 	public Segment clone () {
 		return new Segment(id, text.clone());
 	}
 	
 	@Override
 	public boolean isSegment () {
 		return true;
 	}
 
 	/**
 	 * Gets the identifier for this segment.
 	 * @return the identifier for this segment.
 	 */
 	public String getId () {
 		return id;
 	}
 
 }
