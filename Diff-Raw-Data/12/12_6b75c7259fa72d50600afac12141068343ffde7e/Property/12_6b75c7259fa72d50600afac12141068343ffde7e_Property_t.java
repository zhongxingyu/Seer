 /*******************************************************************************
  * Copyright (c) 2012 Emanuele Tamponi.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the GNU Public License v3.0
  * which accompanies this distribution, and is available at
  * http://www.gnu.org/licenses/gpl.html
  * 
  * Contributors:
  *     Emanuele Tamponi - initial API and implementation
  ******************************************************************************/
 package com.ios;
 
 import java.util.Set;
 
 
 
 public class Property {
 	
 	public static final String ANY = "*";
 
 	private final IObject root;
 	
 	private final String path;
 	
 	private final String[] tokens;
 	
 	public static class Temporary extends IObject {
 		public Object content;
 		
 		public Temporary(Object content) {
 			setContent("content", content);
 		}
 	}
 	
 	public Property(Object content) {
 		this(new Temporary(content), "content");
 	}
 
 	public Property(IObject root, String path) {
 		this.root = root;
 		this.path = path;
 		this.tokens = path.split("\\.");
 	}
 	
 	public <T> T getContent() {
 		return root.getContent(path);
 	}
 	
 	public <T> T getContent(Class<T> contentType) {
 		return (T)getContent();
 	}
 	
 	public void setContent(Object content) {
 		if (path.contains(ANY)) {
 			int indexPre = path.indexOf('*');
 			int indexAfter = indexPre + 2;
 			IList<? extends IObject> list;
 			if (indexPre > 0) {
 				indexPre -= 1;
 				list = root.getContent(path.substring(0, indexPre), IList.class); 
 			} else {
 				list = (IList<? extends IObject>)root;
 			}
 			if (list != null) {
 				for(int i = 0; i < list.size(); i++) {
 					if (list.get(i) != null) 
 						list.get(i).setContent(path.substring(indexAfter), content);
 				}
 			}
 		} else {
 			root.setContent(path, content);
 		}
 	}
 	
 	public IObject getRoot() {
 		return root;
 	}
 	
 	public String getPath() {
 		return path;
 	}
 	
 	public String[] getPathTokens() {
 		return tokens;
 	}
 	
 	@Override
 	public boolean equals(Object o) {
 		if (o instanceof Property) {
 			Property other = (Property)o;
 			return this.root == other.root && this.path.equals(other.path);
 		} else {
 			return false;
 		}
 	}
 	
 	@Override
 	public String toString() {
 		return path + ": " + (getContent() == null ? "<null>" : getContent());
 	}
 	
 	public boolean isParent(Property complete) {
 		return isPrefix(complete, true);
 	}
 	
 	public boolean isPrefix(Property complete, boolean parentOnly) {
 		if (this.root != complete.root)
 			return false;
 
 		String[] prefixTokens = this.tokens;
 		String[] completeTokens = complete.tokens;
 		if (!this.path.isEmpty()) {
 			if (prefixTokens.length > completeTokens.length)
 				return false;
 			
 			for(int i = 0; i < prefixTokens.length; i++) {
 				if (completeTokens[i].equals(ANY) || prefixTokens[i].equals(ANY))
 					continue;
 				if (!prefixTokens[i].equals(completeTokens[i]))
 					return false;
 			}
 			
 			return parentOnly ? prefixTokens.length == completeTokens.length-1 : true;
 		} else {
 			return parentOnly ? completeTokens[0].length() != 0 && completeTokens.length == 1 : true;
 		}
 	}
 	
 	public boolean includes(Property other) {
 		if (other.isPrefix(this, false)) {
 			if (this.path.isEmpty())
 				return other.path.isEmpty();
 			else
 				return this.tokens.length == other.tokens.length;
 		} else
 			return false;
 	}
 	
 	public IObject getParent() {
 		if (!path.contains("."))
 			return root;
 		
 		String parentPath = path.substring(0, path.lastIndexOf('.'));
 		if (parentPath.endsWith("ANY"))
 			throw new UnsupportedOperationException("Cannot request parent of a generic path: " + path);
 		return root.getContent(parentPath, IObject.class);
 	}
 	
 	public Property getLocalProperty() {
 		if (!path.contains("."))
 			return this;
 		
 		return new Property(getParent(), path.substring(path.lastIndexOf('.')+1));
 	}
 	
 	public Class<?> getContentType() {
 		return getContentType(false);
 	}
 	
 	public Class<?> getContentType(boolean runtime) {
 		if (runtime) {
 			Object content = getContent();
 			return content == null ? getContentType(false) : content.getClass();
 		} else {
 			Property local = getLocalProperty();
 			return local.root.getContentType(local.path, false);
 		}
 	}
 
 	@Override
 	public int hashCode() {
 		return root.hashCode() + path.hashCode();
 	}
 
 	public Set<Class> getValidContentTypes() {
 		return getParent().getValidContentTypes(getLocalProperty().getPath());
 	}
 	
 	public boolean isBound() {
 		return getParent().getBoundProperties().contains(getLocalProperty());
 	}
 	
 	public boolean isUnbound() {
 		return !isBound();
 	}
 
 	public String getLastPart() {
 		if (!path.contains("."))
 			return path;
 		else
 			return path.substring(path.lastIndexOf('.')+1);
 	}

	public boolean isLocal() {
		return root.getProperties().contains(this);
	}
 	
 }
