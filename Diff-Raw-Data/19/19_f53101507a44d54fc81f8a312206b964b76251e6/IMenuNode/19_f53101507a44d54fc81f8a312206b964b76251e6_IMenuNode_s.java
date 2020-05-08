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
  * The Bug menu is a tree structure. This class represents the model interface
  * for the menu. A menu node can be either a container (non executing) or a leaf
  * (executing).
  * 
  * @author ken
  * 
  */
 public interface IMenuNode {
 	public IMenuNode getParent();
 
 	public IMenuNode setParent(IMenuNode parent);
 
 	/**
 	 * @return All children for this node.
 	 */
 	public List getChildren();
 
 	/**
 	 * @return <code>true</code> if the children should be sorted, and <code>
 	 * false</code> if they should not be.
 	 */
 	public boolean sortChildren();
 
 	/**
 	 * @return The Comparator to sort by if <code>sortChildren()</code> returns
 	 *         <code>true</code>. If <code>null</code> is returned, and
 	 *         <code>sortChildren()</code> returns true, the natural ordering,
 	 *         taken from the <code>compareTo()</code> method, will be used.
 	 */
 	public Comparator getComparator();
 
 	/**
 	 * @return <code>true</code> if number of children greater than 0.
 	 */
 	public boolean hasChildren();
 
 	/**
 	 * If a node is busy, the menu system will display a spinner in the place of
 	 * the node. The user will be able to go back to other nodes while the node
 	 * is busy. The correct way to utilize this feature is to set the node to
 	 * busy, spawn a new Thread to take care of the work, and set the node to
 	 * not busy after the thread completes. This way, the menu system will not
 	 * block.
 	 * 
 	 * @return <code>true</code> if the node is busy and <code>false</code> if
 	 *         the node is not busy.
 	 */
 	public boolean isBusy();
 
 	/**
 	 * 
 	 * @return Alternate text to display when a node is busy. If
 	 *         <code>null</code>, use the node name.
 	 */
 	public String getBusyName();
 
 	/**
 	 * Iterate through ancestors until one of specified type is found. NULL if
 	 * none found.
 	 * 
 	 * @param type
 	 * @return
 	 */
 	public IMenuNode getFirstParentOfType(String type);
 
 	/**
 	 * If menu node is a leaf, the user has selected this node for execution.
 	 * Specific logic should be implemented here.
 	 * 
 	 * @param display
 	 * @throws Exception
 	 */
 	public void execute() throws Exception;
 
 	/**
 	 * @return next IMenuNode in tree that is current (list index + 1) in same
 	 *         level.
 	 */
 	public IMenuNode getNextSibling();
 
 	/**
 	 * @return previous IMenuNode in tree that is current (list index - 1) in
 	 *         same level.
 	 */
 	public IMenuNode getPreviousSibling();
 
 	/**
 	 * Name property used in the UI.
 	 * 
 	 * @return
 	 */
 	public abstract String getName();
 
 	/**
 	 * Sets the name property used in the UI. Not unique.
 	 * 
 	 * @param name
 	 */
 	public abstract void setName(String name);
 
 	/**
 	 * 
 	 * @return a '.' separated path for this node.
 	 */
 	public abstract String getPath();
 
 	/**
 	 * 
 	 * @return <code>true</code> if this node should currently display an error,
 	 *         <code>false</code> otherwise.
 	 */
 	public abstract boolean isError();
 
 	/**
 	 * 
 	 * @return The name to display if <code>isError()</code> returns
 	 *         <code>true</code>.
 	 */
 	public abstract String getErrorName();
 }
