 /*===========================================================================
   Copyright (C) 2010 by the Okapi Framework contributors
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
  * Implements the base object for the parts that make up a content.
  */
 public class TextPart {
 
 	/**
 	 * Text fragment of this part.
 	 */
 	public TextFragment text;
 
 	/**
 	 * Creates an empty part.
 	 */
 	public TextPart () {
 		text = new TextFragment();
 	}
 	
 	/**
	 * Creates a new TextPart with a given {@link TextFragment}.
	 * @param text the {@link TextFragment} for this new part.
 	 */
 	public TextPart (TextFragment text) {
 		if ( text == null ) {
 			text = new TextFragment();
 		}
 		this.text = text;
 	}
 	
 	/**
 	 * Creates a new TextPart with a given text string.
 	 * @param text the text for this new part.
 	 */
 	public TextPart (String text) {
 		this.text = new TextFragment(text);
 	}
 
 	@Override
 	public TextPart clone () {
 		return new TextPart(text.clone());
 	}
 
 	@Override
 	public String toString () {
 		if ( text == null ) return null;
 		return text.toString();
 	}
 
 	/**
 	 * Gets the text fragment for this part.
 	 * @return the text fragment for this part.
 	 */
 	public TextFragment getContent () {
 		return text;
 	}
 	
 	/**
	 * Sets the {@link TextFragment} for this part.
	 * @param fragment the {@link TextFragment} to assign to this part.
	 * It must not be null.
 	 */
 	public void setContent (TextFragment fragment) {
 		this.text = fragment;
 	}
 	
 	/**
	 * Indicates if this part is a {@link Segment}.
	 * @return true if the part is a {@link Segment}, false if it is not.
 	 */
 	public boolean isSegment () {
 		return false;
 	}
 
 }
