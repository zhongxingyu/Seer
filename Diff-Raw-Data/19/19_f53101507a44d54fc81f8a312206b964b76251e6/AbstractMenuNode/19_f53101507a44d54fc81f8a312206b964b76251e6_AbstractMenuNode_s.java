 /*******************************************************************************
  * Copyright (c) 2008, 2009 Bug Labs, Inc.
  * All rights reserved.
  * 
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  *    - Redistributions of source code must retain the above copyright notice,
  *      this list of conditions and the following disclaimer.
  *    - Redistributions in binary form must reproduce the above copyright
  *      notice, this list of conditions and the following disclaimer in the
  *      documentation and/or other materials provided with the distribution.
  *    - Neither the name of Bug Labs, Inc. nor the names of its contributors may be
  *      used to endorse or promote products derived from this software without
  *      specific prior written permission.
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
  * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
  * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
  * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
  * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
  * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
  * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
  * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
  * POSSIBILITY OF SUCH DAMAGE.
  *******************************************************************************/
 package com.buglabs.menu;
 
 import java.util.Comparator;
 import java.util.List;
 
 /**
  * Implements common functionality for a menu node.
  * 
  * @author kgilmer
  * 
  */
 public abstract class AbstractMenuNode implements IMenuNode, Comparable {
 	private static final long serialVersionUID = 244185876040942855L;
 
 	private String name;
 
 	private IMenuNode parent;
 
 	public AbstractMenuNode(String name) {
 		this.name = name;
 	}
 
 	public AbstractMenuNode(String name, IMenuNode parent) {
 		this.name = name;
 		this.parent = parent;
 	}
 
 	/*
 	 * If you extend AbstractMenuNode and wish to have children nodes this
 	 * method needs to be overridden.
 	 * 
 	 * (non-Javadoc)
 	 * 
 	 * @see com.buglabs.menu.IMenuNode#getChildren()
 	 */
 	public List getChildren() {
 		return null;
 	}
 
 	public boolean sortChildren() {
 		return true;
 	}
 
 	public Comparator getComparator() {
 		return null;
 	}
 
 	public IMenuNode getParent() {
 		return parent;
 	}
 
 	public IMenuNode setParent(IMenuNode parent) {
 		this.parent = parent;
 		return parent;
 	}
 
 	public boolean hasChildren() {
 		List c = getChildren();
 
 		if (c == null) {
 			return false;
 		}
 
 		return getChildren().size() > 0;
 	}
 
 	public boolean isBusy() {
 		return false;
 	}
 
 	public String getBusyName() {
 		return null;
 	}
 
 	public IMenuNode getFirstParentOfType(String type) {
 		IMenuNode node = this;
 
 		while ((node = node.getParent()) != null) {
 			if (node.getClass().getName().equals(type)) {
 				return node;
 			}
 		}
 
 		return null;
 	}
 
 	public void execute() throws Exception {
 		// by default no action is taken.
 	}
 
 	public IMenuNode getNextSibling() {
 		int me = getSiblingIndex();
 
 		if (me > -1) {
 			List sibs = this.getParent().getChildren();
 			if (sibs.size() >= (me + 2)) {
 				return (IMenuNode) sibs.get(me + 1);
 			}
 		}
 
 		return null;
 	}
 
 	public IMenuNode getPreviousSibling() {
 		int me = getSiblingIndex();
 
 		if (me > 0) {
 			return (IMenuNode) this.getParent().getChildren().get(me - 1);
 		}
 
 		return null;
 	}
 
 	private int getSiblingIndex() {
 		if (this.getParent() == null) {
 			// node has no parent and as such no siblings.
 			return -1;
 		} else if (this.getParent().getChildren() == null) {
 			// there is a no forward association between parent and child.
 			throw new RuntimeException("There is a no forward association between parent and child.");
 		}
 		List sibs = this.getParent().getChildren();
 		return sibs.indexOf(this);
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	public String toString() {
 		return name;
 	}
 
 	public boolean equals(Object arg0) {
 		if (arg0 instanceof IMenuNode) {
 			if (((IMenuNode) arg0).getName().equals(this.getName())) {
 				return true;
 			}
 			return false;
 		}
 
 		return super.equals(arg0);
 	}
 
 	public int compareTo(Object arg0) {
 		if (arg0 instanceof IMenuNode) {
 			return this.getName().compareTo(((IMenuNode) arg0).getName());
 		}
 
 		return this.getName().compareTo(arg0.toString());
 	}
 
 	/*
 	 * It is not smart to use this in a node's constructor. If you do that and a
 	 * set of submenus has not been registered yet, you will end up with an
 	 * incomplete path. (non-Javadoc)
 	 * 
 	 * @see com.buglabs.menu.IMenuNode#getPath()
 	 */
 	public String getPath() {
 		StringBuffer sb = new StringBuffer();
 
 		IMenuNode n = this;
 		sb.append(this.getName());
 		n = this.getParent();
 
 		while (n != null) {
 			String s = sb.toString();
 			sb = new StringBuffer(n.getName() + ".");
 			sb.append(s);
 			n = n.getParent();
 		}
 
 		sb.deleteCharAt(0);
 		return sb.toString();
 	}
 
 	public boolean isError() {
 		return false;
 	}
 
 	public String getErrorName() {
 		return null;
 	}
 }
