 /* Chronos - Game Development Toolkit for Java game developers. The
  * original source remains:
  * 
  * Copyright (c) 2013 Miguel Gonzalez http://my-reality.de
  * 
  * This source is provided under the terms of the BSD License.
  * 
  * Copyright (c) 2013, Chronos
  * All rights reserved.
  * 
  * Redistribution and use in source and binary forms, with or 
  * without modification, are permitted provided that the following 
  * conditions are met:
  * 
  *  * Redistributions of source code must retain the above 
  *    copyright notice, this list of conditions and the 
  *    following disclaimer.
  *  * Redistributions in binary form must reproduce the above 
  *    copyright notice, this list of conditions and the following 
  *    disclaimer in the documentation and/or other materials provided 
  *    with the distribution.
  *  * Neither the name of the Chronos/my Reality Development nor the names of 
  *    its contributors may be used to endorse or promote products 
  *    derived from this software without specific prior written permission.
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND 
  * CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, 
  * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF 
  * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE 
  * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS 
  * BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, 
  * OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, 
  * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, 
  * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY 
  * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR 
  * TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT 
  * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY 
  * OF SUCH DAMAGE.
  */
 package de.myreality.chronos.resources;
 
 import java.io.Serializable;
 
 import de.myreality.chronos.util.FamilyObject;
 import de.myreality.chronos.util.IDProvider;
 
 /**
  * A resource definition defines how a resource looks like. It defines children,
  * attributes and provides functionality of a parent group which is always named
  * as "root" on default.
  * 
  * @author Miguel Gonzalez <miguel-gonzalez@gmx.de>
  * @since 0.8alpha
  * @version 0.8alpha
  */
 public interface ResourceDefinition extends Serializable,
 		FamilyObject<ResourceDefinition>, IDProvider {
 
 	// ===========================================================
 	// Constants
 	// ===========================================================
 
 	// Resource element tag
 	public static final String RESOURCE_TAG = "resource";
 
 	// Name of the id attribute
 	public static final String ID = "id";
 
 	// Name of the type attribute
 	public static final String TYPE = "type";
 
 	// ===========================================================
 	// Methods
 	// ===========================================================
 
 	/**
 	 * Returns the id of this definition
 	 * 
 	 * @return current definition id
 	 */
 	String getId();
 
 	/**
 	 * Returns the current value of this resource
 	 * 
 	 * @return current content value
 	 */
 	String getValue();
 
 	/**
 	 * Returns the type if the resource. The type has to be always the name of
 	 * the class which represents the resource.
 	 * <p>
 	 * For instance the type of <code>String</code> will be "String" or for
 	 * <code>Image</code> it will be "Image". Everything else will be false.
 	 * 
	 * @return
 	 */
 	String getType();
 
 	/**
 	 * Returns the current group ob this definition
 	 * 
 	 * @return current definition group
 	 */
 	ResourceGroup getGroup();
 
 	/**
 	 * Returns the name of the group (default is root)
 	 */
 	String getGroupId();
 
 	/**
 	 * Determines if this definition is deferred or not. Deferred definitions
 	 * will be loaded when they are needed.
 	 * 
 	 * @return True when deferred
 	 */
 	boolean isDeferred();
 
 	/**
 	 * Sets a new resource type
 	 * 
 	 * @param type
 	 *            type of this definition
 	 */
 	void setType(String type);
 
 	/**
 	 * Sets a new value of this definition
 	 * 
 	 * @param value
 	 *            value of this definition
 	 */
 	void setValue(String value);
 
 	/**
 	 * Sets a new id of this definition
 	 * 
 	 * @param id
 	 *            id of this definition
 	 */
 	void setId(String id);
 
 	/**
 	 * Sets a new group and assign it to this definition
 	 * 
 	 * @param group
 	 *            group of this resource definition
 	 */
 	void setGroup(ResourceGroup group);
 
 	/**
 	 * Sets a new deferred state
 	 * 
 	 * @param deferred
 	 *            new deferred state
 	 */
 	void setDeferred(boolean deferred);
 
 	/**
 	 * Adds a new attribute to this definition
 	 * 
 	 * @param name
 	 *            name of the attribute
 	 * @param value
 	 *            value of the attribute
 	 */
 	void addAttribute(String name, String value);
 
 	/**
 	 * Returns the attribute for a specific attribute name (the key)
 	 * 
 	 * @param key
 	 *            name of the attribute
 	 * @return value of the attribute
 	 */
 	String getAttribute(String key);
 
 }
