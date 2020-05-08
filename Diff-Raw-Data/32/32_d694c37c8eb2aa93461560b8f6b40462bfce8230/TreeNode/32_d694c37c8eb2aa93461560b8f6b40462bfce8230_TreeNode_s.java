 /*
  * TreeNode.java
  *
  * Copyright (C) 2012
  *
  * This file is part of Proyecto persistenceGeo
  *
  * This software is free software; you can redistribute it and/or modify it
  * under the terms of the GNU General Public License as published by the Free
  * Software Foundation; either version 2 of the License, or (at your option) any
  * later version.
  *
  * This software is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  * details.
  *
  * You should have received a copy of the GNU General Public License along with
  * this library; if not, write to the Free Software Foundation, Inc., 51
  * Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
  *
  * As a special exception, if you link this library with other files to produce
  * an executable, this library does not by itself cause the resulting executable
  * to be covered by the GNU General Public License. This exception does not
  * however invalidate any other reasons why the executable file might be covered
  * by the GNU General Public License.
  *
  * Authors:: Antonio Hernández (mailto:ahernandez@emergya.com)
  */
 package com.emergya.persistenceGeo.dto;
 
 /**
  * Tree node implementation
  *
  * @author <a href="mailto:ahernandez@emergya.com">ahernandez</a>
  * @author <a href="mailto:adiaz@emergya.com">adiaz</a>
  *
  */
 public class TreeNode implements Treeable {
 
 	private static final long serialVersionUID = 7848394150785096968L;
 
     /**
      *  The original object
      */
     private Object data;
 
     /**
      *  TRUE if this node isn't a container of nodes
      */
 	private boolean leaf;
 
     /**
      *  A string that indicates what was the original class
      */
     private String type;
     
     /**
      * Node's id
      */
     private Long id;
     
     /**
      * Text to show
      */
     private String text;
 
 	public TreeNode(Object origin, Long id, String text, String type, boolean isLeaf) {
 		super();
         this.data = origin;
         this.id = id;
         this.text = text;
         this.type = type;
         this.leaf = isLeaf;
 	}
 
 	public TreeNode(AbstractDto origin, boolean isLeaf, String type) {
 		this(origin, origin.getId(), origin.getName(), type, isLeaf);
 	}
 
 	public TreeNode(AbstractDto origin, boolean isLeaf) {
         this(origin, isLeaf, origin.getClass().getSimpleName());
 	}
 
 	public TreeNode(AbstractDto origin) {
         this(origin, true, origin.getClass().getSimpleName());
 	}
 
 	public Object getData() {
 		return data;
 	}
 
 	public void setData(Object data) {
 		this.data = data;
 	}
 	
 	public boolean getLeaf() {
 		return leaf;
 	}
 
 	public void setLeaf(boolean leaf) {
 		this.leaf = leaf;
 	}
 
 	public String getType() {
 		return type;
 	}
 
 	public void setType(String type) {
 		this.type = type;
 	}
 
 	public Long getId() {
 		return id;
 	}
 
 	public void setId(Long id) {
 		this.id = id;
 	}
 
 	public String getText() {
 		return text;
 	}
 
 	public void setText(String text) {
 		this.text = text;
 	}
 	
 }
