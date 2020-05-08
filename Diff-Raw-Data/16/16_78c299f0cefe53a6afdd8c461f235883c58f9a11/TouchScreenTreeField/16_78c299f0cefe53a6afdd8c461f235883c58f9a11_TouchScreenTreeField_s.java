 /*-
  * Copyright (c) 2009, Derek Konigsberg
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions
  * are met:
  *
  * 1. Redistributions of source code must retain the above copyright
  *    notice, this list of conditions and the following disclaimer.
  * 2. Redistributions in binary form must reproduce the above copyright
  *    notice, this list of conditions and the following disclaimer in the
  *    documentation and/or other materials provided with the distribution.
  * 3. Neither the name of the project nor the names of its
  *    contributors may be used to endorse or promote products derived
  *    from this software without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
  * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
  * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
  * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
  * COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
  * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
  * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
  * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
  * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
  * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
  * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
  * OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 package org.logicprobe.LogicMail.ui;
 
 import net.rim.device.api.system.Bitmap;
 import net.rim.device.api.ui.Graphics;
 import net.rim.device.api.ui.TouchEvent;
 import net.rim.device.api.ui.component.TreeField;
 import net.rim.device.api.ui.component.TreeFieldCallback;
 
 /**
  * TreeField implementation with some additional code for touchscreen
  * usability.  This version adds a mark on the right side of the field,
  * and uses it to distinguish between selection and expansion for nodes
  * that offer the choice.
  */
 public class TouchScreenTreeField extends TreeField {
 	private TreeFieldCallback callback;
 	private static Bitmap chevronIcon = Bitmap.getBitmapResource("chevron_right_black_15x22.png");
 	private static Bitmap chevronIconHighlighted = Bitmap.getBitmapResource("chevron_right_white_15x22.png");
 	private static int chevronIconWidth = chevronIcon.getWidth();
 	private static int chevronIconHeight = chevronIcon.getHeight();
 	
 	public TouchScreenTreeField(TreeFieldCallback callback, long style) {
 		super(new TreeFieldCallbackProxy(), style);
 		this.callback = callback;
 	}
 	
 	private static class TreeFieldCallbackProxy implements TreeFieldCallback {
 		public void drawTreeItem(TreeField treeField, Graphics graphics,
 				int node, int y, int width, int indent) {
 			((TouchScreenTreeField)treeField).treeFieldDrawTreeItem(treeField, graphics, node, y, width, indent);
 		}
 	}
 	
 	private void treeFieldDrawTreeItem(
 			TreeField treeField, Graphics graphics,
 			int node, int y, int width, int indent) {
 		int drawWidth = width;
 		if(isNodeSelectable(node)) {
 			int rowWidth = width + indent;
 			int xPos = rowWidth - (chevronIconWidth * 2);
 			
 			if(getCurrentNode() == node) {
				graphics.drawBitmap(xPos, y, chevronIconWidth, chevronIconHeight, chevronIconHighlighted, 0, 0);
 			}
 			else {
				graphics.drawBitmap(xPos, y, chevronIconWidth, chevronIconHeight, chevronIcon, 0, 0);
 			}
 			
 			drawWidth -= (rowWidth - xPos);
 		}
 		callback.drawTreeItem(treeField, graphics, node, y, drawWidth, indent);
 	}
 
 	private boolean isNodeSelectable(int node) {
 		Object cookie = this.getCookie(node);
 		if(cookie instanceof TreeFieldNode) {
 			return ((TreeFieldNode)cookie).isNodeSelectable();
 		}
 		else {
 			return false;
 		}
 	}
 	
 	/* (non-Javadoc)
 	 * @see net.rim.device.api.ui.Field#touchEvent(net.rim.device.api.ui.TouchEvent)
 	 */
 	protected boolean touchEvent(TouchEvent message) {
 		if(message.getEvent() == TouchEvent.CLICK) {
 			int x = message.getX(1);
 			int y = message.getY(1);
 			if(x < 0 || x >= this.getWidth() || y < 0 || y >= this.getWidth()) {
 				return super.touchEvent(message);
 			}
 			else {
 				if(x < (this.getWidth() - this.getRowHeight() * 2)) {
 					int node = this.getCurrentNode();
 					if(node != -1 && getFirstChild(node) != -1) {
 						// This seems to cause the node to automatically toggle
 						return true;
 					}
 					else {
 						return false;
 					}
 				}
 				else {
 					return false;
 				}
 			}
 		}
 		return super.touchEvent(message);
 	}
 }
