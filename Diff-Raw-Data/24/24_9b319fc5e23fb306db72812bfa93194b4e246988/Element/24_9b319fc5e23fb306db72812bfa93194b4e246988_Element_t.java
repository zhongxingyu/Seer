 /*******************************************************************************
  * Copyright (c) 2009 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *     Zend Technologies
  *******************************************************************************/
 package org.eclipse.dltk.core.index.sql;
 
 import java.io.Serializable;
 
 /**
  * This POJO represents model element.
  * 
  * @author michael
  */
 public class Element implements Serializable {
 
 	private static final long serialVersionUID = 1L;
 	private int id;
 	private int type;
 	private int flags;
 	private int offset;
 	private int length;
 	private int nameOffset;
 	private int nameLength;
 	private String name;
	private String camelCaseName;
 	private String metadata;
 	private String qualifier;
 	private String parent;
 	private int fileId;
 	private boolean isReference;
 
 	public Element(int id, int type, int flags, int offset, int length,
			int nameOffset, int nameLength, String name, String camelCaseName,
			String metadata, String qualifier, String parent, int fileId,
			boolean isReference) {
 		super();
 		this.id = id;
 		this.type = type;
 		this.flags = flags;
 		this.offset = offset;
 		this.length = length;
 		this.nameOffset = nameOffset;
 		this.nameLength = nameLength;
 		this.name = name;
		this.camelCaseName = camelCaseName;
 		this.metadata = metadata;
 		this.qualifier = qualifier;
 		this.parent = parent;
 		this.fileId = fileId;
 		this.isReference = isReference;
 	}
 
 	public int getId() {
 		return id;
 	}
 
 	public int getType() {
 		return type;
 	}
 
 	public int getFlags() {
 		return flags;
 	}
 
 	public int getOffset() {
 		return offset;
 	}
 
 	public int getLength() {
 		return length;
 	}
 
 	public int getNameOffset() {
 		return nameOffset;
 	}
 
 	public int getNameLength() {
 		return nameLength;
 	}
 
 	public String getName() {
 		return name;
 	}
 
	public String getCamelCaseName() {
		return camelCaseName;
	}

 	public String getMetadata() {
 		return metadata;
 	}
 
 	public String getQualifier() {
 		return qualifier;
 	}
 
 	public String getParent() {
 		return parent;
 	}
 
 	public int getFileId() {
 		return fileId;
 	}
 
 	public boolean isReference() {
 		return isReference;
 	}
 
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result + id;
		result = prime * result + type;
 		return result;
 	}
 
 	public boolean equals(Object obj) {
 		if (this == obj)
 			return true;
 		if (obj == null)
 			return false;
 		if (getClass() != obj.getClass())
 			return false;
 		Element other = (Element) obj;
 		if (id != other.id)
 			return false;
		if (type != other.type)
			return false;
 		return true;
 	}
 
 	public String toString() {
		return "Element [type=" + type + ", isReference=" + isReference
				+ ", name=" + name + "]";
 	}
 }
