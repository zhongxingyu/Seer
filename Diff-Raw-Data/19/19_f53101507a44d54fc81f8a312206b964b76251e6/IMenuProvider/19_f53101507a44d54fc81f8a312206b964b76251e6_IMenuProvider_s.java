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
 
 /**
  * Implementors provide a menu system to the end user. Clients can provide
  * sub-menus.
  * 
  * @author ken
  * 
  */
 public interface IMenuProvider {
 	/**
 	 * @param path
 	 *            String denotion of path using '.' character as seperator. To
 	 *            contribute to the root menu pass an empty string or
 	 *            <code>null</code>. Example: "child1.subchild1.me".
 	 * @param menu
 	 *            Reference to the sub-root menu.
 	 */
 	public void registerMenu(String path, IMenuNode menu);
 
 	/**
 	 * Notify the menu provider that a menu node has changed. Should cause the
 	 * menu system to refresh if the path is visible on the screen.
 	 * 
 	 * @param path
 	 */
 	public void notifyMenuUpdate(String path);
 
 	/**
 	 * Remove a submenu.
 	 * 
 	 * @param path
 	 */
 	public void unregisterMenu(String path);
 
 	/**
 	 * Moves up the tree towards the root.
 	 * 
 	 * @param numLevels
 	 *            The number of times to move up. If this is greater than the
 	 *            number of steps to the root, the current node will be set to
 	 *            the root.
 	 */
 	public void selectParent(int numLevels);
 
 	/**
 	 * 
 	 * @param path
 	 * @return <code>true</code> if path is the current node, <code>false</code>
 	 *         otherwise.
 	 */
 	public boolean isCurrentNode(String path);
 
 }
