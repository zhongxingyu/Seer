 /**
  * Copyright (c) 2011 Canoo Engineering AG info@canoo.com
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  * THE SOFTWARE.
  */
 
 package org.gwt.contentflow4gwt.client;
 
 import com.google.gwt.core.client.JavaScriptObject;
 import com.google.gwt.dom.client.Element;
 import com.google.gwt.dom.client.Node;
 
 final class ContentFlowWrapper extends JavaScriptObject {
     protected ContentFlowWrapper() {
     }
 
     public native void init() /*-{
         this.init();
     }-*/;
 
     public native ContentFlowItemWrapper getItem(int index) /*-{
         return this.items[index];
     }-*/;
 
     public native int getNumberOfItems() /*-{
         return this.getNumberOfItems();
     }-*/;
 
     public native ContentFlowItemWrapper getActiveItem() /*-{
         return this.getActiveItem();
     }-*/;
 
     public native void moveTo(int itemIndex) /*-{
         return this.moveTo(itemIndex);
     }-*/;
     
     public native void addItem(Element el, int index) /*-{
     	return this.addItem(el, index);
     }-*/;
     
     public native Node removeItem(int itemIndex) /*-{
     	return this.rmItem(itemIndex);
     }-*/;
     
     public native void holdPos(boolean holdSlider, boolean holdPos) /*-{
     	return this._initStep(holdSlider, holdPos);
     }-*/;
     
     public native void refreshActiveItem(int timeout) /*-{
     	// Call to window.setTimeout => in function: lost of 'this'
     	console.log("Refresh active item");
     	var self = this;
     	window.setTimeout(function () {
 	    	if(self._currentPosition =="undefined" || isNaN(self._currentPosition) || self._currentPosition < 0) {
 	    		self._activeItem = self._getItemByPosition(0);
 	    	}
 	    	else {
 	    		self._activeItem = self._getItemByPosition(self._currentPosition);
 	    	}
 			self.moveTo(0);
     	}, timeout);
 	}-*/;
 }
