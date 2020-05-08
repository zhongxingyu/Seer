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
 
 package net.sf.okapi.common.resource;
 
 import java.util.Hashtable;
 import java.util.Set;
 import java.util.concurrent.ConcurrentHashMap;
 
 import net.sf.okapi.common.IResource;
 import net.sf.okapi.common.ISkeleton;
 import net.sf.okapi.common.annotation.Annotations;
 import net.sf.okapi.common.annotation.IAnnotation;
 
 /**
  * Basic unit of extraction from a filter and also the resource associated with the filter event TEXT_UNIT.
  * The TextUnit object holds the extracted source text, all its properties and 
  * annotations, and any target corresponding data.
  */
 public class TextUnit implements INameable, IReferenceable {
 
 	private static final int TARGETS_INITCAP = 2;
 	
 	private String id;
 	private int refCount;
 	private String name;
 	private String type;
 	private boolean isTranslatable = true;
 	private boolean preserveWS;
 	private ISkeleton skeleton;
 	private Hashtable<String, Property> properties;
 	private Annotations annotations;
 	private TextContainer source;
 	private String mimeType;
 	private ConcurrentHashMap<String, TextContainer> targets;
 
 	/**
 	 * Creates a new TextUnit object with its identifier.
 	 * @param id the identifier of this resource.
 	 */
 	public TextUnit (String id) {
 		create(id, null, false, null);
 	}
 
 	/**
 	 * Creates a new TextUnit object with its identifier and a text.
 	 * @param id the identifier of this resource.
 	 * @param sourceText the initial text of the source.
 	 */
 	public TextUnit (String id,
 		String sourceText)
 	{
 		create(id, sourceText, false, null);
 	}
 
 	/**
 	 * Creates a new TextUnit object with its ID, a text, and a flag indicating if it is a referent or not.
 	 * @param id the identifier of this resource.
 	 * @param sourceText the initial text of the source.
 	 * @param isReferent indicates if this resource is a referent (i.e. is referred to
 	 * by another resource) or not.
 	 */
 	public TextUnit (String id,
 		String sourceText,
 		boolean isReferent)
 	{
 		create(id, sourceText, isReferent, null);
 	}
 
 	/**
 	 * Creates a new TextUnit object with its identifier, a text, a flag indicating 
 	 * if it is a referent or not, and a given MIME type.
 	 * @param id the identifier of this resource.
 	 * @param sourceText the initial text of the source.
 	 * @param isReferent indicates if this resource is a referent (i.e. is referred to
 	 * by another resource) or not.
 	 * @param mimeType the MIME type identifier for the content of this TextUnit.
 	 */
 	public TextUnit (String id,
 		String sourceText,
 		boolean isReferent,
 		String mimeType)
 	{
 		create(id, sourceText, isReferent, mimeType);
 	}
 
 	private void create (String id,
 		String sourceText,
 		boolean isReferent,
 		String mimeType)
 	{
 		targets = new ConcurrentHashMap<String, TextContainer>(TARGETS_INITCAP);
 		this.id = id;
 		refCount = (isReferent ? 1 : 0); 
 		this.mimeType = mimeType;
 		source = new TextContainer();
 		if ( sourceText != null ) {
 			source.text.append(sourceText);
 		}
 	}
 
 	/**
 	 * Gets the string representation of the source text of this TextUnit.
 	 * @return the source text of this TextUnit.
 	 */
 	@Override
 	public String toString () {
 		return source.toString();
 	}
 	
 	public String getId () {
 		return id;
 	}
 
 	public void setId (String id) {
 		this.id = id;
 	}
 
 	public ISkeleton getSkeleton () {
 		return skeleton;
 	}
 
 	public void setSkeleton (ISkeleton skeleton) {
 		this.skeleton = skeleton;
 	}
 
 	public String getName () {
 		return name;
 	}
 
 	public void setName (String name) {
 		this.name = name;
 	}
 
 	public String getType () {
 		return type;
 	}
 	
 	public void setType (String value) {
 		type = value;
 	}
 	
 	public <A extends IAnnotation> A getAnnotation (Class<A> annotationType) {
 		if ( annotations == null ) return null;
 		return annotationType.cast(annotations.get(annotationType) );
 	}
 
 	public void setAnnotation (IAnnotation annotation) {
 		if ( annotations == null ) {
 			annotations = new Annotations();
 		}
 		annotations.set(annotation);
 	}
 
 	public Property getProperty (String name) {
 		if ( properties == null ) return null;
 		return properties.get(name);
 	}
 
 	public Property setProperty (Property property) {
 		if ( properties == null ) properties = new Hashtable<String, Property>();
 		properties.put(property.getName(), property);
 		return property;
 	}
 	
 	public void removeProperty (String name) {
 		if ( properties != null ) {
 			properties.remove(name);
 		}
 	}
 	
 	public Set<String> getPropertyNames () {
 		if ( properties == null ) properties = new Hashtable<String, Property>();
 		return properties.keySet();
 	}
 
 	public boolean hasProperty (String name) {
 		if ( properties == null ) return false;
 		return properties.containsKey(name);
 	}
 
 	public Property getSourceProperty (String name) {
 		return source.getProperty(name);
 	}
 
 	public Property setSourceProperty (Property property) {
 		return source.setProperty(property);
 	}
 	
 	public Set<String> getSourcePropertyNames () {
 		return source.getPropertyNames();
 	}
 	
 	public void removeSourceProperty (String name) {
 		source.removeProperty(name);
 	}
 	
 	public boolean hasSourceProperty (String name) {
 		return source.hasProperty(name);
 	}
 
 	public Property getTargetProperty (String language,
 		String name)
 	{
 		TextContainer tc = getTarget(language);
 		if ( tc == null ) return null;
 		return tc.getProperty(name);
 	}
 
 	public Property setTargetProperty (String language,
 		Property property)
 	{
 		return createTarget(language, false, IResource.CREATE_EMPTY).setProperty(property);
 	}
 
 	public void removeTargetProperty (String language,
 		String name)
 	{
 		TextContainer tc = getTarget(language);
 		if ( tc != null ) {
 			tc.removeProperty(name);
 		}
 	}
 	
 	public Set<String> getTargetPropertyNames (String language) {
 		TextContainer tc = createTarget(language, false, IResource.CREATE_EMPTY);
 		return tc.getPropertyNames();
 	}
 
 	public boolean hasTargetProperty (String language,
 		String name)
 	{
 		TextContainer tc = getTarget(language);
 		if ( tc == null ) return false;
 		return (tc.getProperty(name) != null);
 	}
 
 	public Set<String> getTargetLanguages () {
 		return targets.keySet();
 	}
 
 	public Property createTargetProperty (String language,
 		String name,
 		boolean overwriteExisting,
 		int creationOptions)
 	{
 		// Get the target or create an empty one
 		TextContainer tc = createTarget(language, false, CREATE_EMPTY);
 		// Get the property if it exists
 		Property prop = tc.getProperty(name);
 		// If it does not exists or if we overwrite: create a new one
 		if (( prop == null ) || overwriteExisting ) {
 			// Get the source property
 			prop = source.getProperty(name);
 			if ( prop == null ) {
 				// If there is no source, create an empty property
 				return tc.setProperty(new Property(name, "", false));
 			}
 			else { // If there is a source property
 				// Create a copy, empty or not depending on the options
 				if ( creationOptions == CREATE_EMPTY ) {
 					return tc.setProperty(new Property(name, "", prop.isReadOnly()));
 				}
 				else {
 					return tc.setProperty(prop.clone());
 				}
 			}
 		}
 		return prop;
 	}
 
 	public boolean isTranslatable () {
 		return isTranslatable;
 	}
 	
 	public void setIsTranslatable (boolean value) {
 		isTranslatable = value;
 	}
 
 	public boolean isReferent () {
 		return (refCount > 0);
 	}
 
 	public void setIsReferent (boolean value) {
 		refCount = (value ? 1 : 0 );
 	}
 	
 	public int getReferenceCount () {
 		return refCount;
 	}
 	
 	public void setReferenceCount (int value) {
 		refCount = value;
 	}
 
 	/**
 	 * Gets the source object for this TextUnit (a {@link TextContainer} object).
 	 * @return the source object for this TextUnit.
 	 */
 	public TextContainer getSource () {
 		return source;
 	}
 	
 	/**
 	 * Sets the source object for this TextUnit. Any existing source object is overwritten.
 	 * @param textContainer the source object to set.
 	 * @return the source object that has been set.
 	 */
 	public TextContainer setSource (TextContainer textContainer) {
 		if ( textContainer == null ) {
 			throw new NullPointerException("The source container of a TextUnit cannot be null.");
 		}
 		source = textContainer;
 		return source;
 	}
 	
 	/**
 	 * Gets the target object for this TextUnit for a given language.
 	 * @param language the language to query.
 	 * @return the target object for this TextUnit for the given language, or null if
 	 * it does not exist.
 	 */
 	public TextContainer getTarget (String language) {
 		return targets.get(language);
 	}
 
 	/**
 	 * Sets the target object for this TextUnit for a given language.
 	 * Any existing target object for the given language is overwritten.
 	 * To set a target object based on the source, use the 
 	 * {@link #createTarget(String, boolean, int)} method.
 	 * @param language the target language. 
 	 * @param text the target object to set.
 	 * @return the target object that has been set.
 	 */
 	public TextContainer setTarget (String language,
 		TextContainer text)
 	{
 		targets.put(language, text);
 		return text;
 	}
 	
 	/**
 	 * Removes a given target object from this TextUnit.  
 	 * @param language the target language to remove.
 	 */
 	public void removeTarget (String language) {
 		if ( hasTarget(language) ) {
 			targets.remove(language);
 		}
 	}
 	
 	/**
 	 * Indicates if there is a target object for a given language for this TextUnit. 
 	 * @param language the language to query.
 	 * @return true if a target object exists for the given language, false otherwise.
 	 */
 	public boolean hasTarget (String language) {
 		return (targets.get(language) != null);
 	}
 	
 	/**
 	 * Creates or get the target for this TextUnit.
 	 * @param language the target language.
 	 * @param overwriteExisting true to overwrite any existing target for the given language.
 	 * False to not create a new target object if one already exists for the given language. 
 	 * @param creationOptions creation options:
 	 * <ul><li>CREATE_EMPTY: Create an empty target object.</li>
 	 * <li>COPY_CONTENT: Copy the text of the source (and any associated in-line code).</li>
 	 * <li>COPY_PROPERTIES: Copy the source properties.</li>
 	 * <li>COPY_ALL: Same as (COPY_CONTENT|COPY_PROPERTIES).</li></ul>
 	 * @return the target object that was created, or retrieved. 
 	 */
 	public TextContainer createTarget (String language,
 		boolean overwriteExisting,
 		int creationOptions)
 	{
 		TextContainer trgCont = targets.get(language);
 		if (( trgCont == null ) || overwriteExisting ) {
 			trgCont = getSource().clone(
 				(creationOptions & COPY_PROPERTIES) == COPY_PROPERTIES);
 			if ( creationOptions == CREATE_EMPTY ) {
 				trgCont.clear();
 			}
 			targets.put(language, trgCont);
 		}
 		return trgCont;
 	}
 
 	/**
 	 * Gets the content of the source for this TextUnit (a {@link TextFragment} object).
 	 * @return the content of the source for this TextUnit.
 	 */
 	public TextFragment getSourceContent () {
        //How is this different from getSource()?
 		return source;
 	}
 	
 	/**
 	 * Sets the content of the source for thsi TextUnit.
 	 * @param content the new content to set.
 	 * @return the new content of the source for this TextUnit. 
 	 */
 	public TextFragment setSourceContent (TextFragment content) {
		//set methods should generally not return anything
        //In this case it seems non-intuitive to be returning the TextContainer when setting the text fragment on the
        //textcontainer
        //Also, doesn't setting the content actually end up appending the content?
        source.setContent(content);
 		return source;
 	}
 
 	/**
 	 * Gets the content of the target for a given language for this TextUnit.
 	 * @param language the language to query.
 	 * @return the content of the target for the given language for this TextUnit.
 	 */
 	public TextFragment getTargetContent (String language) {
 		TextContainer tc = getTarget(language);
 		if ( tc == null ) return null;
 		return tc.getContent();
 	}
 	
 	/**
 	 * Sets the content of the target for a given language for this TextUnit.
 	 * @param language the language to set.
 	 * @param content the new content to set.
 	 * @return the new content for the given target language for this text unit. 
 	 */
 	public TextFragment setTargetContent (String language,
 		TextFragment content)
 	{
 		TextContainer tc = createTarget(language, false, CREATE_EMPTY);
 		tc.setContent(content);
 		return tc;
 	}
 
 	public String getMimeType () {
 		return mimeType;
 	}
 	
 	public void setMimeType (String mimeType) {
 		this.mimeType = mimeType;
 	}	
 
 	/**
 	 * Indicates if the source text of this TextUnit is empty.
 	 * @return true if the source text of this TextUnit is empty, false otherwise.
 	 */
 	public boolean isEmpty () {
 		return (( source.text == null ) || ( source.text.length() == 0 ));
 	}
 
 	public boolean preserveWhitespaces () {
 		return preserveWS;
 	}
 	
 	public void setPreserveWhitespaces (boolean value) {
 		preserveWS = value;
 	}
 	
 }
