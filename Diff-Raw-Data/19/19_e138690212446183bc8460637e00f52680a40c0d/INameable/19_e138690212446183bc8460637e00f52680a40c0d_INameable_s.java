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
 
 import java.util.Set;
 
 import net.sf.okapi.common.IResource;
 
 /**
  * Provides the methods common to all resources that can be named and have properties. 
  */
 public interface INameable extends IResource {
 
 	/**
	 * Gets the name of this resource. The resource name is the equivalent of the XLIFF resname attribute.
 	 * @return This resource name, or null if there is none.
 	 */
 	public String getName () ;
 	
 	/**
 	 * Sets the name of this resource. The resource name is the equivalent of the XLIFF resname attribute.
 	 * @param name New name to set.
 	 */
 	public void setName (String name);
 
 	/**
 	 * Gets the type information associated with this resource. For example "button".
 	 * @return The type information associated with this resource.
 	 */
 	public String getType ();
 	
 	/**
 	 * Sets the type information associated with this resource. For example "button".
 	 * @param value The new type information.
 	 */
 	public void setType (String value);
 	
 	/**
 	 * Gets the type of content of this resource. For example "text/xml".
 	 * @return The type of content of this resource.
 	 */
 	public String getMimeType ();
 	
 	/**
 	 * Sets the type of content of this resource. For example "text/xml".
 	 * @param value The new type of content of this resource.
 	 */
 	public void setMimeType (String value);
 	
 	/**
 	 * Gets the resource-level property for a given name.
 	 * @param name Name of the property to retrieve.
 	 * @return The property or null if it does not exist.
 	 */
 	public Property getProperty (String name);
 
 	/**
 	 * Sets a resource-level property. If a property already exists it is overwritten.
 	 * @param property The new property to set.
 	 * @return The property that has been set.
 	 */
 	public Property setProperty (Property property);
 	
 	/**
 	 * Removes a resource-level property of a given name. If the property does not exists
 	 * nothing happens.
 	 * @param name The name of the property to remove.
 	 */
 	public void removeProperty (String name);
 	
 	/**
 	 * Gets the names of all the resource-level properties for this resource.
 	 * @return All the names of the resource-level properties for this resource.
 	 */
 	public Set<String> getPropertyNames ();
 
 	/**
 	 * Indicates if a resource-level property exists for a given name.
 	 * @param name The name of the resource-level property to query.
 	 * @return True if a resource-level property exists, false otherwise.
 	 */
 	public boolean hasProperty (String name);
 
 	/**
 	 * Gets the source property for a given name.
 	 * @param name The name of the source property to retrieve.
 	 * @return The property or null if it does not exist.
 	 */
 	public Property getSourceProperty (String name);
 
 	/**
 	 * Sets a source property. If a property already exists it is overwritten. 
 	 * @param property The new property to set.
 	 * @return The property that has been set.
 	 */
 	public Property setSourceProperty (Property property);
 	
 	/**
 	 * Removes a source property of a given name. If the property does not exists
 	 * nothing happens.
 	 * @param name The name of the property to remove.
 	 */
 	public void removeSourceProperty (String name);
 	
 	/**
 	 * Gets the names of all the source properties for this resource.
 	 * @return All the names of the source properties for this resource.
 	 */
 	public Set<String> getSourcePropertyNames ();
 
 	/**
 	 * Indicates if a source property exists for a given name.
 	 * @param name The name of the source property to query.
 	 * @return True if a source property exists, false otherwise.
 	 */
 	public boolean hasSourceProperty (String name);
 
 	/**
 	 * Gets the target property for a given name and target language.
 	 * @param language The language of the property to retrieve. The language code is case-sensitive.
 	 * @param name The name of the property to retrieve. This name is case-sensitive.
 	 * @return The property or null if it does not exist.
 	 */
 	public Property getTargetProperty (String language,
 		String name);
 	
 	/**
 	 * Sets a target property. If a property already exists it is overwritten.
 	 * @param language The target language for which this property should be set. The language code
 	 * is case-sensitive.
 	 * @param property The new property to set. This name is case-sensitive.
 	 * @return The property that has been set.
 	 */
 	public Property setTargetProperty (String language,
 		Property property);
 	
 	/**
 	 * Removes a target property of a given name. If the property does not exists
 	 * nothing happens.
 	 * @param language The target language for which this property should be set. The language code
 	 * is case-sensitive.
 	 * @param name The name of the property to remove.
 	 */
 	public void removeTargetProperty (String language,
 		String name);
 	
 	/**
 	 * Gets the names of all the properties for a given target language in this resource.
 	 * @param language The target language to query. The language code is case-sensitive.
 	 * @return All the names of the target properties for the given language in this resource.
 	 */
 	public Set<String> getTargetPropertyNames (String language);
 
 	/**
 	 * Gets the names of all the target languages for this resource.
 	 * @return All the names of the target languages for this resource.
 	 */
 	public Set<String> getTargetLanguages ();
 	
 	/**
 	 * Indicates if a property exists for a given name and target language.
 	 * @param language The target language to query. The language code is case-sensitive.
 	 * @param name The name of the property to query.
 	 * @return True if a property exists, false otherwise.
 	 */
 	public boolean hasTargetProperty (String language,
 		String name);
 
 	/**
 	 * Creates or get a target property based on the corresponding source.
 	 * @param language The target language to use. The language code is case-sensitive.
 	 * @param name The name of the property to create (or retrieve)
 	 * @param overwriteExisting True to overwrite any existing property.
 	 * False to not create a new property if one exists already. 
 	 * @param creationOptions Creation options:
 	 * <ul><li>CREATE_EMPTY: Creates an empty property, only the read-only flag 
 	 * of the source is copied.</li>
 	 * <li>COPY_CONTENT: Creates a new property with all its data copied from 
 	 * the source.</li></ul>
 	 * @return The property that was created, or retrieved. 
 	 */
 	public Property createTargetProperty (String language,
 		String name,
 		boolean overwriteExisting,
 		int creationOptions);
 
 	/**
 	 * Indicates if the content of this resource is translatable.
 	 * By default this indicator is set to true for all resources. 
 	 * @return True if the content of this resource is translatable. False if
 	 * it is not translatable.
 	 */
 	public boolean isTranslatable ();
 	
 	/**
 	 * Sets the flag indicating if the content of this resource is translatable.
 	 * @param value True to indicate that the content of this resource is translatable.
 	 */
 	public void setIsTranslatable (boolean value);
 
 	/**
 	 * Indicates if the white-spaces in the content of this resource should be preserved.
 	 * By default this indicator is set to false for all resources. 
 	 * @return True if the white-spaces in the content of this resource should be preserved.
 	 */
 	public boolean preserveWhitespaces ();
 
 	/**
 	 * sets the flag indicating if the white-spaces in the content of this resource should be preserved.
 	 * @param value True to indicate that the white-spaces in the content of this resource should be preserved.
 	 */
 	public void setPreserveWhitespaces (boolean value);
 	
 }
