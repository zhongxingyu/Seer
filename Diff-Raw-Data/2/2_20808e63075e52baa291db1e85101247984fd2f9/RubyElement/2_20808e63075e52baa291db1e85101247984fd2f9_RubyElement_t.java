 /*
  * Author: C.Williams
  *
  *  Copyright (c) 2004 RubyPeople. 
  *
  *  This file is part of the Ruby Development Tools (RDT) plugin for eclipse.
  *  You can get copy of the GPL along with further information about RubyPeople 
  *  and third party software bundled with RDT in the file 
  *  org.rubypeople.rdt.core_0.4.0/RDT.license or otherwise at 
  *  http://www.rubypeople.org/RDT.license.
  *
  *  RDT is free software; you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation; either version 2 of the License, or
  *  (at your option) any later version.
  *
  *  RDT is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of 
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  * 
  *  You should have received a copy of the GNU General Public License
  *  along with RDT; if not, write to the Free Software
  *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
  */
 package org.rubypeople.rdt.internal.core.parser;
 
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Set;
 
 /**
  * @author Chris
  * 
  * To change the template for this generated type comment go to Window -
  * Preferences - Java - Code Generation - Code and Comments
  */
 public class RubyElement implements IRubyElement {
 
 	protected String access;
 	protected String name;
 	protected Position start;
 	protected Position end;
 	protected Set elements = new HashSet();
 
 	public static final String PUBLIC = "public";
 	public static final String PRIVATE = "private";
 	public static final String READ = "read";
 	public static final String WRITE = "write";
 
 	protected RubyElement(String name, Position start) {
 		this.start = start;
 		this.name = name;
 	}
 
 	/**
 	 * @return
 	 */
 	public String getName() {
 		return name;
 	}
 
 	/**
 	 * @return
 	 */
 	public Position getStart() {
 		return start;
 	}
 
 	/**
 	 * @return
 	 */
 	public Position getEnd() {
 		return end;
 	}
 
 	/**
 	 * @return
 	 */
 	public String getAccess() {
 		return access;
 	}
 
 	public void setAccess(String newAccess) {
 		access = newAccess;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see java.lang.Object#hashCode()
 	 */
 	public int hashCode() {
 		return name.hashCode();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see java.lang.Object#equals(java.lang.Object)
 	 */
 	public boolean equals(Object arg0) {
 		if (arg0 instanceof RubyElement) {
 			RubyElement element = (RubyElement) arg0;
 			return element.name.equals(this.name);
 		}
 		return false;
 	}
 
 	/**
 	 * @param end
 	 */
 	public void setEnd(Position end) {
 		this.end = end;
 	}
 
 	/**
 	 * @return
 	 */
 	public int getElementCount() {
 		return elements.size();
 	}
 
 	/**
 	 * @param method
 	 */
 	public void addElement(RubyElement method) {
 		elements.add(method);
 	}
 
 	/**
 	 * @param element
 	 * @return
 	 */
 	public boolean contains(RubyElement element) {
 		return elements.contains(element);
 	}
 
 	public RubyElement getElement(String name) {
 		for (Iterator iter = elements.iterator(); iter.hasNext();) {
 			RubyElement element = (RubyElement) iter.next();
 			if (element.getName().equals(name)) { return element; }
 		}
 		return null;
 	}
 
 	public boolean isOutlineElement() {
 		return true;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.rubypeople.rdt.internal.core.parser.IRubyElement#getElements()
 	 */
 	public Object[] getElements() {
 		Set outlineElements = new HashSet();
 		for (Iterator iter = elements.iterator(); iter.hasNext();) {
 			RubyElement element = (RubyElement) iter.next();
 			if (element.isOutlineElement())
 				outlineElements.add(element);
 			else {
 				Object[] elements = element.getElements();
 				if (elements.length > 0) {
					outlineElements.addAll(Arrays.asList(elements));
 				} else {
 					continue;
 				}
 			}
 		}
 		return outlineElements.toArray();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.rubypeople.rdt.internal.core.parser.IRubyElement#hasElements()
 	 */
 	public boolean hasElements() {
 		return getElements().length > 0;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see java.lang.Object#toString()
 	 */
 	public String toString() {
 		return getClass().getName() + ": " + getName() + ", [" + getStart() + "," + getEnd() + "]";
 	}
 }
