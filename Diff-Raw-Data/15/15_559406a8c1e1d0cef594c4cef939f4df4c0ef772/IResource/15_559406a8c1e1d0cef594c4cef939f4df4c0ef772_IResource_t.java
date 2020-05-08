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
 
 import net.sf.okapi.common.annotation.IAnnotation;
 
 /**
  * Common set of features all the types of resources associated with events have.  
  */
 public interface IResource {
 	
 	public static final int CREATE_EMPTY = 0;
 	public static final int COPY_CONTENT = 0x01;
 	public static final int COPY_PROPERTIES = 0x02;
 	public static final int COPY_ALL = (COPY_CONTENT | COPY_PROPERTIES);
 
 	/**
 	 * Gets the identifier of the resource. This identifier is unique per extracted document and by type of resource.
 	 * This value is filter-specific. It and may be different from one extraction 
 	 * of the same document to the next. It can a sequential number or not, incremental 
 	 * or not, and it can be not a number.
 	 * It has no correspondence in the source document ("IDs" coming from the source document
 	 * are "names" and not available for all resources).
 	 * @return the identifier of this resource.
 	 */
 	public String getId ();
 	
 	/**
 	 * Sets the identifier of this resource.
 	 * @param id the new identifier value.
 	 * @see #getId()
 	 */
 	public void setId (String id);
 	
 	/**
 	 * Gets the skeleton object for this resource.
 	 * @return the skeleton object for this resource or null if there is none.
 	 */
 	public ISkeleton getSkeleton ();
 	
 	/**
 	 * Sets the skeleton object for this resource.
 	 * @param skeleton the skeleton object to set.
 	 */
 	public void setSkeleton (ISkeleton skeleton);
 
 	/**
 	 * Gets the annotation object for a given class for this resource.
	 * @param annotationType the class of the annotation object to retrieve.
 	 * @return the annotation for the given class for this resource. 
 	 */
 	public <A extends IAnnotation> A getAnnotation(Class<A> annotationType);
 
 	/**
 	 * Sets an annotation object or this resource.
 	 * @param annotation the annotation object to set.
 	 */
 	public void setAnnotation (IAnnotation annotation);
 	
 }
