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
 
 package net.sf.okapi.common.annotation;
 
 import java.util.concurrent.ConcurrentHashMap;
 
 /**
  * Provides annotation mechanism to the resources.
  */
 public class Annotations {
 	
 	private ConcurrentHashMap<Class<? extends IAnnotation>, IAnnotation> annotations;
 
 	/**
 	 * Creates a new Annotations object.
 	 */
 	public Annotations () {
 		annotations = new ConcurrentHashMap<Class<? extends IAnnotation>, IAnnotation>();
 	}
 	
 	/**
 	 * Sets an annotation.
 	 * @param annotation The annotation object to set.
 	 */
	public <T extends IAnnotation> void set (T annotation) {
 		annotations.put(annotation.getClass(), annotation);
 	}
 	
 	/**
 	 * Gets the annotation for a given type.
 	 * @param annotationType Type of the annotation to retrieve.
 	 * @return The found annotation, or null if no annotation of the given type was found. 
 	 */
	public <A extends IAnnotation> A get (Class<A> annotationType) {
		return annotationType.cast(annotations.get(annotationType) );
 	}
 
 	/**
 	 * Removes all the annotations in this object.
 	 */
 	public void clear () {
 		annotations.clear();
 	}
 
 }
