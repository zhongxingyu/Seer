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
 
 import java.util.HashMap;
 import java.util.Map;
 
 import de.myreality.chronos.util.BasicFamilyObject;
 
 /**
  * Basic implementation of a data node
  * 
  * @author Miguel Gonzalez <miguel-gonzalez@gmx.de>
  * @since 0.8alpha
  * @version 0.8alpha
  */
 public class BasicDataNode extends BasicFamilyObject<DataNode> implements
 		DataNode {	
 
 	// ===========================================================
 	// Constants
 	// ===========================================================
 	
 	private static final long serialVersionUID = 1L;
 
 	// ===========================================================
 	// Fields
 	// ===========================================================
 	
 	private Map<String, String> attributes;
 	
 	private String content;
 	
 	private String name;
 
 	// ===========================================================
 	// Constructors
 	// ===========================================================
 	
	BasicDataNode() {
		
	}
	
 	BasicDataNode(String name) {
 		this(name, "");
 	}
 	
 	BasicDataNode(String name, String content, Map<String, String> attributes) {
 		this.attributes = attributes;
 		this.name = name;
 		this.content = content;	
 		
 		if (attributes == null) {
 			this.attributes = new HashMap<String, String>();
 		}
 		
 		this.content = content.trim();
 	}
 	
 	BasicDataNode(String name, String content) {
 		this(name, content, null);
 	}
 	
 	BasicDataNode(String name, Map<String, String> attributes) {
 		this(name, "", attributes);
 	}
 
 	// ===========================================================
 	// Getters and Setters
 	// ===========================================================
 
 	// ===========================================================
 	// Methods from Superclass
 	// ===========================================================
 
 	@Override
 	public void addAttribute(String name, String value) {
 		attributes.put(name, value);
 	}
 
 	@Override
 	public String getAttribute(String key) {
 		return attributes.get(key);
 	}
 
 	@Override
 	public Map<String, String> getAttributes() {
 		return attributes;
 	}
 
 	@Override
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	@Override
 	public String getName() {
 		return name;
 	}
 
 	@Override
 	public void setContent(String content) {
 		this.content = content;
 		this.content = content.trim();
 	}
 
 	@Override
 	public String getContent() {
 		return content;
 	}
 
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result
 				+ ((attributes == null) ? 0 : attributes.hashCode());
 		result = prime * result + ((content == null) ? 0 : content.hashCode());
 		result = prime * result + ((name == null) ? 0 : name.hashCode());
 		return result;
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj)
 			return true;
 		if (obj == null)
 			return false;
 		if (getClass() != obj.getClass())
 			return false;
 		BasicDataNode other = (BasicDataNode) obj;
 		if (attributes == null) {
 			if (other.attributes != null)
 				return false;
 		} else if (!attributes.equals(other.attributes))
 			return false;
 		if (content == null) {
 			if (other.content != null)
 				return false;
 		} else if (!content.equals(other.content))
 			return false;
 		if (name == null) {
 			if (other.name != null)
 				return false;
 		} else if (!name.equals(other.name))
 			return false;
 		return true;
 	}
 
 	@Override
 	public String toString() {
 		return "BasicDataNode [attributes=" + attributes + ", content="
 				+ content + ", name=" + name + "]";
 	}
 	
 	
 	
 	
 
 	// ===========================================================
 	// Methods
 	// ===========================================================
 
 	// ===========================================================
 	// Inner classes
 	// ===========================================================
 }
