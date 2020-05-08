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
  * An interface for menu controllers.
  * 
  * @author ken
 * @deprecated The menu system is not used in BUG 2.0.  API present for compatibility.
  * 
  */
 public interface IMenuController {
 
 	/**
 	 * Tell the controller that a button event has occurred.
 	 * 
 	 * @param buttonId
 	 */
 	public abstract void buttonPress(int buttonId);
 
 	/**
 	 * Redraw existing content on the menu display.
 	 */
 	public abstract void refreshDisplay();
 
 	/**
 	 * Reset menu to root and refresh the display.
 	 */
 	public abstract void resetDisplay();
 
 	/**
 	 * Clean up the display.
 	 */
 	public abstract void clearDisplay();
 
 	/**
 	 * @return <code>true</code> if the menu is on the root node (at rest).
 	 */
 	public abstract boolean isRoot();
 
 	/**
 	 * 
 	 * @param path
 	 *            The path to check the current node against
 	 * @return <code>true</code> if the current node corresponds to this path
 	 */
 	public abstract boolean isCurrentNode(String path);
 
 	/**
 	 * Moves up the tree towards the root.
 	 * 
 	 * @param numLevels
 	 *            The number of times to move up. If this is greater than the
 	 *            number of steps to the root, the current node will be set to
 	 *            the root.
 	 */
 	public abstract void selectParent(int numLevels);
 
 }
