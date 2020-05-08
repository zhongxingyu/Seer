 /*******************************************************************************
  * Copyright (c) 2005, 2012 eBay Inc.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  *******************************************************************************/
 package org.eclipse.vjet.dsf.active.dom.html;
 
 import org.eclipse.vjet.dsf.html.dom.DVideo;
 import org.eclipse.vjet.dsf.html.dom.EHtmlAttr;
 import org.eclipse.vjet.dsf.jsnative.HtmlVideo;
 import org.eclipse.vjet.dsf.jsnative.anno.BrowserType;
 
 public class AHtmlVideo extends AMediaElement implements HtmlVideo {
 
 	private static final long serialVersionUID = 1L;
 	
 	//
 	// Constructor(s)
 	//
 	protected AHtmlVideo(AHtmlDocument doc, DVideo video) {
 		super(doc, video);
 		populateScriptable(AHtmlVideo.class, doc == null ? BrowserType.IE_6P : doc.getBrowserType());
 	}
 
 	//
 	// API
 	//	
 	public long getHeight() {
 		return getInt(getDVideo().getHtmlHeight());
 	}
 	public void setHeight(int height) {
 		getDVideo().setHtmlHeight(height);
 		onAttrChange(EHtmlAttr.height, height);
 	}
 	
 	public long getWidth() {
 		return getInt(getDVideo().getHtmlWidth());
 	}
 	public void setWidth(int width) {
 		getDVideo().setHtmlWidth(width);
 		onAttrChange(EHtmlAttr.width, width);
 	}
 	
 	public String getSrc() {
 		return getDVideo().getHtmlSrc();
 	}
 	public void setSrc(String src) {
 		getDVideo().setHtmlSrc(src);
 		onAttrChange(EHtmlAttr.src, src);
 	}
 	
 	public String getPoster() {
 		return getDVideo().getHtmlPoster();
 	}
 	public void setPoster(String poster) {
 		getDVideo().setHtmlPoster(poster);
 		onAttrChange(EHtmlAttr.poster, poster);
 	}
 	
 	public boolean getAutoBuffer() {
 		return getDVideo().getHtmlAutoBuffer();
 	}
 	public void setAutoBuffer(boolean autoBuffer) {
 		setHtmlAttribute(EHtmlAttr.autobuffer, autoBuffer);
 		onAttrChange(EHtmlAttr.autobuffer, autoBuffer);
 	}
 	
 	public boolean getAutoPlay() {
 		return getDVideo().getHtmlAutoPlay();
 	}
 	public void setAutoPlay(boolean autoPlay) {
 		setHtmlAttribute(EHtmlAttr.autoplay, autoPlay);
 		onAttrChange(EHtmlAttr.autoplay, autoPlay);
 	}
 	
 	public boolean getLoop() {
 		return getDVideo().getHtmlLoop();
 	}
 	public void setLoop(boolean loop) {
 		setHtmlAttribute(EHtmlAttr.loop, loop);
 		onAttrChange(EHtmlAttr.loop, loop);
 	}
 	
 	public boolean getControls() {
 		return getDVideo().getHtmlControls();
 	}
 	public void setControls(boolean controls) {
 		setHtmlAttribute(EHtmlAttr.controls, controls);
 		onAttrChange(EHtmlAttr.controls, controls);
 	}
 	
 	//
 	// Events
 	//
 
 	// Since property name is 'onblur', Rhino invokes this method.
 	public Object getOnblur() {
 		return getOnBlur();
 	}
 	
 	// Since property name is 'onfocus', Rhino invokes this method.
 	public Object getOnfocus() {
 		return getOnFocus();
 	}
 	
 	// For Rhino
 	public void setOnblur(Object functionRef) {
 		setOnBlur(functionRef);
 	}
 	
 	// For Rhino
 	public void setOnfocus(Object functionRef) {
 		setOnFocus(functionRef);
 	}
 	
 	// Since property name is 'onkeydown', Rhino invokes this method.
 	public Object getOnkeydown() {
 		return getOnKeyDown();
 	}
 	
 	// For Rhino
 	public void setOnkeydown(Object functionRef) {
 		setOnKeyDown(functionRef);
 	}
 	
 	// Since property name is 'onkeypress', Rhino invokes this method.
 	public Object getOnkeypress() {
 		return getOnKeyPress();
 	}
 	
 	// For Rhino
 	public void setOnkeypress(Object functionRef) {
 		setOnKeyPress(functionRef);
 	}
 	
 	// Since property name is 'onkeyup', Rhino invokes this method.
 	public Object getOnkeyup() {
 		return getOnKeyUp();
 	}
 	
 	// For Rhino
 	public void setOnkeyup(Object functionRef) {
 		setOnKeyUp(functionRef);
 	}
 	
 	// Since property name is 'onresize', Rhino invokes this method.
 	public Object getOnresize() {
 		return getOnResize();
 	}
 	
 	// For Rhino
 	public void setOnresize(Object functionRef) {
 		setOnResize(functionRef);
 	}
 	
 	// Since property name is 'onclick', Rhino invokes this method.
 	public Object getOnclick() {
 		return getOnClick();
 	}
 	
 	// For Rhino
 	public void setOnclick(Object functionRef) {
 		setOnClick(functionRef);
 	}
 	
 	// Since property name is 'ondblclick', Rhino invokes this method.
 	public Object getOndblclick() {
 		return getOnDblClick();
 	}
 	
 	// For Rhino
 	public void setOndblclick(Object functionRef) {
 		setOnDblClick(functionRef);
 	}
 	
 	// Since property name is 'onmousedown', Rhino invokes this method.
 	public Object getOnmousedown() {
 		return getOnMouseDown();
 	}
 	
 	// For Rhino
 	public void setOnmousedown(Object functionRef) {
 		setOnMouseDown(functionRef);
 	}
 	
 	// Since property name is 'onmousemove', Rhino invokes this method.
 	public Object getOnmousemove() {
 		return getOnMouseMove();
 	}
 	
 	// For Rhino
 	public void setOnmousemove(Object functionRef) {
 		setOnMouseMove(functionRef);
 	}
 	
 	// Since property name is 'onmouseout', Rhino invokes this method.
 	public Object getOnmouseout() {
 		return getOnMouseOut();
 	}
 	
 	// For Rhino
 	public void setOnmouseout(Object functionRef) {
 		setOnMouseOut(functionRef);
 	}
 	
 	// Since property name is 'onmouseover', Rhino invokes this method.
 	public Object getOnmouseover() {
 		return getOnMouseOver();
 	}
 	
 	// For Rhino
 	public void setOnmouseover(Object functionRef) {
 		setOnMouseOver(functionRef);
 	}
 	
 	// Since property name is 'onmouseup', Rhino invokes this method.
 	public Object getOnmouseup() {
 		return getOnMouseUp();
 	}
 	
 	// For Rhino
 	public void setOnmouseup(Object functionRef) {
 		setOnMouseUp(functionRef);
 	}
 	
 	//
 	// Private
 	//
 	private DVideo getDVideo() {
 		return (DVideo) getDNode();
 	}
 
	@Override
	public void setHeight(long height) {
		// TODO Auto-generated method stub
		
	}
 
	@Override
	public void setWidth(long width) {
		// TODO Auto-generated method stub
		
	}
 
 	@Override
 	public long getVideoWidth() {
 		// TODO Auto-generated method stub
 		return 0;
 	}
 
 	@Override
 	public long getVideoHeight() {
 		// TODO Auto-generated method stub
 		return 0;
 	}
 	
 }
