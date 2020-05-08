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
 
 import com.google.gwt.dom.client.DivElement;
 import com.google.gwt.dom.client.Document;
 import com.google.gwt.dom.client.Element;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.Image;
 import com.google.gwt.user.client.ui.SimplePanel;
 import com.google.gwt.user.client.ui.Widget;
 
 public class PhotoView extends Composite {
 	private Object pojo;
 	/**
 	 * Creates a standard ContentFlow item with image and caption.
 	 * @param image use {@link com.reveregroup.gwt.imagepreloader.client.FitImage} if you want the image to be preloaded before displayed.
 	 * Images must be preloaded to be rendered correctly and completely in some browsers.
 	 * @param caption Text that will appear below the image.
 	 */
 	public PhotoView(Image image, String caption) {
 		this(image, caption, null);
 	}
 
 	public PhotoView(Image image, String caption, Object pojo) {
		initWidget(createWidget(image.getElement(), caption));
 		this.pojo = pojo;
 	}
 
 	private static Widget createWidget(Element imageElement, String captionText) {
 		SimplePanel result = new SimplePanel();
 		result.setStyleName("item");
 //		result.getElement().setAttribute("href", imageElement.getAttribute("src"));
 		DivElement caption = Document.get().createDivElement();
 		caption.setClassName("caption");
 		caption.setInnerHTML(captionText);
 		imageElement.setClassName("content");
 		result.getElement().appendChild(imageElement);
 		result.getElement().appendChild(caption);
 
 		return result;
 	}
 
 	public Object getPojo() {
 		return pojo;
 	}
 
 	public void setPojo(Object pojo) {
 		this.pojo = pojo;
 	}
 
 }
