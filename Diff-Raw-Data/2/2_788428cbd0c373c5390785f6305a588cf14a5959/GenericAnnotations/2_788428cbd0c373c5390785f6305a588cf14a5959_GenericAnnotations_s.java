 /*===========================================================================
   Copyright (C) 2012 by the Okapi Framework contributors
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
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.List;
 
 import net.sf.okapi.common.Util;
 import net.sf.okapi.common.resource.Code;
 import net.sf.okapi.common.resource.ITextUnit;
 import net.sf.okapi.common.resource.InlineAnnotation;
 import net.sf.okapi.common.resource.TextContainer;
 
 /**
  * Provides access to a list of {@link GenericAnnotation}.
  * <p>This annotation can be used inline as well as on structural objects.
  */
 public class GenericAnnotations extends InlineAnnotation implements Iterable<GenericAnnotation> {
 
 	private static final String ANNOTATION_SEPARATOR = "\u001d";
 
 	private List<GenericAnnotation> list;
 	
 	/**
 	 * Adds a set of annotations to a given text unit. If the text unit has
 	 * no annotation set already attached, one is created and attached,
 	 * otherwise all the annotations of the set passed as argument are added to
 	 * the existing set. 
 	 * @param tu the text unit where to attached the new set.
 	 * @param newSet the new set to add.
 	 */
 	static public void addAnnotations (ITextUnit tu,
 		GenericAnnotations newSet)
 	{
 		GenericAnnotations current = tu.getAnnotation(GenericAnnotations.class);
 		if ( current == null ) tu.setAnnotation(newSet); 
 		else current.addAll(newSet);
 	}
 	
 	/**
 	 * Adds a set of annotations to a given text container. If the text container has
 	 * no annotation set already attached, one is created and attached,
 	 * otherwise all the annotations of the set passed as argument are added to
 	 * the existing set. 
 	 * @param tc the text container where to attached the new set.
 	 * @param newSet the new set to add.
 	 */
 	static public void addAnnotations (TextContainer tc,
 		GenericAnnotations newSet)
 	{
 		GenericAnnotations current = tc.getAnnotation(GenericAnnotations.class);
 		if ( current == null ) tc.setAnnotation(newSet); 
 		else current.addAll(newSet);
 	}
 	
 	/**
 	 * Adds a set of annotations to a given inline code. If the inline code has
 	 * no annotation set attached yet one is created and attached,
 	 * otherwise all the annotations of the set passed as argument are added to
 	 * the existing set. 
 	 * @param code the code where to add the annotation.
	 * @param newset the new set to add.
 	 */
 	static public void addAnnotations (Code code,
 		GenericAnnotations newSet)
 	{
 		GenericAnnotations current = (GenericAnnotations)code.getAnnotation(GenericAnnotationType.GENERIC);
 		if ( current == null ) code.setAnnotation(GenericAnnotationType.GENERIC, newSet);
 		else current.addAll(newSet);
 	}
 	
 	/**
 	 * Creates an empty annotation set.
 	 */
 	public GenericAnnotations () {
 		// Empty annotation list
 	}
 	
 	/**
 	 * Creates an annotation set initialized with a storage string
 	 * created with {@link #toString()}.
 	 * @param storage the storage string to use.
 	 */
 	public GenericAnnotations (String storage) {
 		fromString(storage);
 	}
 	
 	/**
 	 * Creates an annotation set and add a given one.
 	 * @param annotation the annotation to add.
 	 */
 	public GenericAnnotations (GenericAnnotation annotation) {
 		this.add(annotation);
 	}
 	
 	@Override
 	public GenericAnnotations clone () {
 		return new GenericAnnotations(this.toString());
 	}
 	
 	/**
 	 * Gets the number of annotations in this annotation set.
 	 * @return the number of annotations in this annotation set.
 	 */
 	public int size () {
 		if ( list == null ) return 0;
 		else return list.size();
 	}
 	
 	/**
 	 * Gets a list of all the annotations of a given type.
 	 * <p>The returned list is a new lits but its items are the same as the items in the original list.
 	 * @param type the type of annotation to look for.
 	 * @return the list of all annotations of the given type, the list may be empty but is never null.
 	 */
 	public List<GenericAnnotation> getAnnotations (String type) {
 		if ( Util.isEmpty(list) ) return Collections.emptyList();
 		List<GenericAnnotation> res = new ArrayList<GenericAnnotation>(); 
 		for ( GenericAnnotation ann : list ) {
 			if ( ann.getType().equals(type) ) {
 				res.add(ann);
 			}
 		}
 		return res;
 	}
 
 	/**
 	 * Gets the first occurrence of an annotation for a given type.
 	 * @param type the type of annotation to retrieve.
 	 * @return the first annotation of the given type, or null if none exists.
 	 */
 	public GenericAnnotation getFirstAnnotation (String type) {
 		if ( Util.isEmpty(list) ) return null;
 		for ( GenericAnnotation ann : list ) {
 			if ( ann.getType().equals(type) ) {
 				return ann;
 			}
 		}
 		return null;
 	}
 	
 	/**
 	 * Gets the unmodifiable list of all the annotation in this annotation set.
 	 * @return the live list, or an empty one if there are no annotations.
 	 */
 	public List<GenericAnnotation> getAllAnnotations () {
 		if ( Util.isEmpty(list) ) return Collections.emptyList();
 		return Collections.unmodifiableList(list);
 	}
 	
 	/**
 	 * Indicates if there is at least one annotation of a given type.
 	 * @param type the type of annotation to look for.
 	 * @return true if there is at least one annotation of a given type, false otherwise.
 	 */
 	public boolean hasAnnotation (String type) {
 		if ( Util.isEmpty(list) ) return false;
 		for ( GenericAnnotation ann : list ) {
 			if ( ann.getType().equals(type) ) return true;
 		}
 		return false;
 	}
 	
 	/**
 	 * Removes all annotations from this list.
 	 */
 	public void clear () {
 		if ( list != null ) list.clear();
 	}
 	
 	/**
 	 * Removes a given annotation from this list.
 	 * @param annotation the annotation to remove.
 	 */
 	public void remove (GenericAnnotation annotation) {
 		list.remove(annotation);
 	}
 
 	/**
 	 * Creates an new annotation and add it to this list.
 	 * @param type the type of annotation to create.
 	 * @return the new annotation.
 	 */
 	public GenericAnnotation add (String type) {
 		GenericAnnotation ann = new GenericAnnotation(type);
 		if ( list == null ) list = new ArrayList<GenericAnnotation>();
 		list.add(ann);
 		return ann;
 	}
 	
 	/**
 	 * Adds an existing annotation to this list.
 	 * @param annotation the annotation to add.
 	 * @return the annotation that was added.
 	 */
 	public GenericAnnotation add (GenericAnnotation annotation) {
 		if ( list == null ) list = new ArrayList<GenericAnnotation>();
 		list.add(annotation);
 		return annotation;
 	}
 	
 	public void addAll (GenericAnnotations annotations) {
 		if ( annotations == null ) return;
 		if ( annotations.size() == 0 ) return;
 		// There is something to add: make sure we have a place where to copy
 		if ( list == null ) list = new ArrayList<GenericAnnotation>();
 		// Add all annotations of the given annotation set
 		list.addAll(annotations.getAllAnnotations());
 	}
 	
 	public void addAll (List<GenericAnnotation> newItems) {
 		if ( Util.isEmpty(newItems) ) return;
 		// There is something to add: make sure we have a place where to copy
 		if ( list == null ) list = new ArrayList<GenericAnnotation>();
 		// Add all annotations of the given list
 		list.addAll(newItems);
 	}
 	
 	@Override
 	public String toString () {
 		// Format: <baseClassData or empty><sep><dataForAnnotation1><sep>...
 		if ( Util.isEmpty(list) ) {
 			return (data == null ? "" : data);
 		}
 		// Else: store the annotations
 		StringBuilder sb = new StringBuilder();
 		if ( data != null ) sb.append(data);
 		for ( GenericAnnotation ann : list ) {
 			sb.append(ANNOTATION_SEPARATOR); 
 			sb.append(ann.toString());
 		}
 		return sb.toString();
 	}
 	
 	@Override
 	public void fromString (String storage) {
 		String[] parts = storage.split(ANNOTATION_SEPARATOR, 0);
 		if ( !parts[0].isEmpty() ) data = parts[0];
 		for ( int i=1; i<parts.length; i++ ) {
 			GenericAnnotation ann = add("z"); // This type will be replaced by fromString
 			ann.fromString(parts[i]);
 		}
 	}
 
 	@Override
 	public Iterator<GenericAnnotation> iterator() {
 		if ( list == null ) {
 			List<GenericAnnotation> tmp = Collections.emptyList();
 			return tmp.iterator();
 		}
 		return list.iterator();
 	}
 
 }
