 /*******************************************************************************
  * Copyright (c) 2005, 2012 eBay Inc.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  *******************************************************************************/
 package org.eclipse.vjet.dsf.jsnative;
 
 import org.eclipse.vjet.dsf.jsnative.anno.Alias;
 import org.eclipse.vjet.dsf.jsnative.anno.DOMSupport;
 import org.eclipse.vjet.dsf.jsnative.anno.DomLevel;
 import org.eclipse.vjet.dsf.jsnative.anno.JsMetatype;
 import org.eclipse.vjet.dsf.jsnative.anno.Property;
 
 
 
 /*
  *           attribute unsigned long width;
            attribute unsigned long height;
   readonly attribute unsigned long videoWidth;
   readonly attribute unsigned long videoHeight;
            attribute DOMString poster;
 
  */
 @Alias("HTMLVideoElement")
 @DOMSupport(DomLevel.ONE)
 @JsMetatype
 public interface HtmlVideo extends HtmlElement {
 	
 	@Property long getHeight();
	@Property void setHeight(int height);
 	
 	@Property long getWidth();
	@Property void setWidth(int width);
 	
 	@Property long getVideoWidth();
 	@Property long getVideoHeight();
 	
 //	@Property boolean getAutoPlay();
 //	@Property void setAutoPlay(boolean autoPlay);
 //	
 //	@Property boolean getAutoBuffer();
 //	@Property void setAutoBuffer(boolean autoBuffer);
 //	
 //	@Property boolean getLoop();
 //	@Property void setLoop(boolean loop);
 //	
 	@Property String getPoster();
 	@Property void setPoster(String poster);
 //	
 //	@Property String getSrc();
 //	@Property void setSrc(String src);
 //	
 	
 	@DOMSupport(DomLevel.ZERO)
 	@Property(name="onblur")
 	Object getOnBlur();
 	
 	
 	@DOMSupport(DomLevel.ZERO)
 	@Property(name="onblur")
 	void setOnBlur(Object functionRef);
 	
 	
 	@DOMSupport(DomLevel.ZERO)
 	@Property(name="onfocus")
 	Object getOnFocus();
 	
 	
 	@DOMSupport(DomLevel.ZERO)
 	@Property(name="onfocus")
 	void setOnFocus(Object functionRef);
 	
 	
 	@DOMSupport(DomLevel.ZERO)
 	@Property(name="onkeydown")
 	Object getOnKeyDown();
 	
 	
 	@DOMSupport(DomLevel.ZERO)
 	@Property(name="onkeydown")
 	void setOnKeyDown(Object functionRef);
 	
 	
 	@DOMSupport(DomLevel.ZERO)
 	@Property(name="onkeypress")
 	Object getOnKeyPress();
 	
 	
 	@DOMSupport(DomLevel.ZERO)
 	@Property(name="onkeypress")
 	void setOnKeyPress(Object functionRef);
 	
 	
 	@DOMSupport(DomLevel.ZERO)
 	@Property(name="onkeyup")
 	Object getOnKeyUp();
 	
 	
 	@DOMSupport(DomLevel.ZERO)
 	@Property(name="onkeyup")
 	void setOnKeyUp(Object functionRef);
 	
 	
 	@DOMSupport(DomLevel.ZERO)
 	@Property(name="onresize")
 	Object getOnResize();
 	
 	
 	@DOMSupport(DomLevel.ZERO)
 	@Property(name="onresize")
 	void setOnResize(Object functionRef);
 	
 	
 	@DOMSupport(DomLevel.ZERO)
 	@Property(name="onclick")
 	Object getOnClick();
 	
 	
 	@DOMSupport(DomLevel.ZERO)
 	@Property(name="onclick")
 	void setOnClick(Object functionRef);
 	
 	
 	@DOMSupport(DomLevel.ZERO)
 	@Property(name="ondblclick")
 	Object getOnDblClick();
 	
 	
 	@DOMSupport(DomLevel.ZERO)
 	@Property(name="ondblclick")
 	void setOnDblClick(Object functionRef);
 	
 	
 	@DOMSupport(DomLevel.ZERO)
 	@Property(name="onmousedown")
 	Object getOnMouseDown();
 	
 	
 	@DOMSupport(DomLevel.ZERO)
 	@Property(name="onmousedown")
 	void setOnMouseDown(Object functionRef);
 	
 	
 	@DOMSupport(DomLevel.ZERO)
 	@Property(name="onmouseup")
 	Object getOnMouseUp();
 	
 	
 	@DOMSupport(DomLevel.ZERO)
 	@Property(name="onmouseup")
 	void setOnMouseUp(Object functionRef);
 	
 	
 	@DOMSupport(DomLevel.ZERO)
 	@Property(name="onmousemove")
 	Object getOnMouseMove();
 	
 	
 	@DOMSupport(DomLevel.ZERO)
 	@Property(name="onmousemove")
 	void setOnMouseMove(Object functionRef);
 	
 	
 	@DOMSupport(DomLevel.ZERO)
 	@Property(name="onmouseout")
 	Object getOnMouseOut();
 	
 	
 	@DOMSupport(DomLevel.ZERO)
 	@Property(name="onmouseout")
 	void setOnMouseOut(Object functionRef);
 	
 	
 	@DOMSupport(DomLevel.ZERO)
 	@Property(name="onmouseover")
 	Object getOnMouseOver();
 	
 	
 	@DOMSupport(DomLevel.ZERO)
 	@Property(name="onmouseover")
 	void setOnMouseOver(Object functionRef);
 }
