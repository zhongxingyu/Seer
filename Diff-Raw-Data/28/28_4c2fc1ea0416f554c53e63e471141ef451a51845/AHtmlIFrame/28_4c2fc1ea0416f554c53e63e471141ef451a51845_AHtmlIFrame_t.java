 /*******************************************************************************
  * Copyright (c) 2005, 2012 eBay Inc.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  *******************************************************************************/
 package org.eclipse.vjet.dsf.active.dom.html;
 
 import org.eclipse.vjet.dsf.html.dom.DIFrame;
 import org.eclipse.vjet.dsf.html.dom.EHtmlAttr;
 import org.eclipse.vjet.dsf.jsnative.HtmlDocument;
 import org.eclipse.vjet.dsf.jsnative.HtmlIFrame;
 import org.eclipse.vjet.dsf.jsnative.anno.BrowserType;
 
 public class AHtmlIFrame extends AHtmlElement implements HtmlIFrame {
 	
 	private static final long serialVersionUID = 1L;
 	
 	private AHtmlDocument m_contentDocument = null;
 
 	protected AHtmlIFrame(AHtmlDocument doc, DIFrame node) {
 		super(doc, node);
 		populateScriptable(AHtmlIFrame.class, doc == null ? BrowserType.IE_6P : doc.getBrowserType());
 	}
 	
 	public HtmlDocument getDocument() {
 		return m_contentDocument;
 	}
 	
 	public HtmlDocument getContentDocument() {
 		return m_contentDocument;
 	}
 
 	public String getAlign() {
 		return getDIFrame().getHtmlAlign();
 	}
 
	public Object getFrameBorder() {
 		return getDIFrame().getHtmlFrameBorder();
 	}
 
 	public String getHeight() {
 		return getDIFrame().getHtmlHeight();
 	}
 
 	public String getLongDesc() {
 		return getDIFrame().getHtmlLongDesc();
 	}
 
 	public String getMarginHeight() {
 		return getDIFrame().getHtmlMarginHeight();
 	}
 
 	public String getMarginWidth() {
 		return getDIFrame().getHtmlMarginWidth();
 	}
 
 	public String getName() {
 		return getDIFrame().getHtmlName();
 	}
 
 	/**
 	 * Returns whether or not an iframe should have scrollbars
 	 * @return string of "yes" or "no"
 	 * @see DIFrame.SCROLLING_YES and DIFrame.SCROLLING_NO
 	 */
 	public String getScrolling() {
 		return getDIFrame().getHtmlScrolling();
 	}
 
 	public String getSrc() {
 		return getDIFrame().getHtmlSrc();
 	}
 
 	public String getWidth() {
 		return getDIFrame().getHtmlWidth();
 	}
 
 	public void setAlign(String align) {
 		getDIFrame().setHtmlAlign(align);
 		onAttrChange(EHtmlAttr.align, align);
 	}
 
 	public void setFrameBorder(Object on) {
 		if(on instanceof String){
 			getDIFrame().setHtmlFrameBorder((String)on);
 			onAttrChange(EHtmlAttr.frameborder, (String)on);
 		}
 		if(on instanceof Integer){
 			getDIFrame().setHtmlFrameBorder(((Integer)on).toString());
 			onAttrChange(EHtmlAttr.frameborder, (Integer)on);
 		}
 	}
 
 	public void setHeight(String height) {
 		getDIFrame().setHtmlHeight(height);
 		onAttrChange(EHtmlAttr.height, height);
 	}
 
 	public void setLongDesc(String longDesc) {
 		getDIFrame().setHtmlLongDesc(longDesc);
 		onAttrChange(EHtmlAttr.longdesc, longDesc);
 	}
 
 	public void setMarginHeight(String marginHeight) {
 		getDIFrame().setHtmlMarginHeight(marginHeight);
 		onAttrChange(EHtmlAttr.marginheight, marginHeight);
 	}
 
 	public void setMarginWidth(String marginWidth) {
 		getDIFrame().setHtmlMarginWidth(marginWidth);
 		onAttrChange(EHtmlAttr.marginwidth, marginWidth);
 	}
 
 	public void setName(String name) {
 		getDIFrame().setHtmlName(name);
 		onAttrChange(EHtmlAttr.name, name);
 	}
 
 	/**
 	 * Sets whether or not an iframe should have scrollbars
 	 * @param scrolling string of "yes" or "no"
 	 * @see DFrame.SCROLLING_YES and DFrame.SCROLLING_NO
 	 */
 	public void setScrolling(String scrolling) {
 		getDIFrame().setHtmlScrolling(scrolling);
 		onAttrChange(EHtmlAttr.scrolling, scrolling);
 	}
 
 	public void setSrc(String src) {
 		getDIFrame().setHtmlSrc(src);
 		setContentDocument(AHtmlHelper.getContentDocument(this, src));
 		onAttrChange(EHtmlAttr.src, src);
 	}
 
 	public void setWidth(String width) {
 		getDIFrame().setHtmlWidth(width);
 		onAttrChange(EHtmlAttr.width, width);
 	}
 	
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
 	
 	// Since property name is 'onkeyup', Rhino invokes this method.
 	public Object getOnload() {
 		return getOnLoad();
 	}
 	
 	// For Rhino
 	public void setOnload(Object functionRef) {
 		setOnLoad(functionRef);
 	}
 	
 	// Since property name is 'onunload', Rhino invokes this method.
 	public Object getOnunload() {
 		return getOnLoad();
 	}
 	
 	// For Rhino
 	public void setOnunload(Object functionRef) {
 		setOnLoad(functionRef);
 	}
 	
 	private DIFrame getDIFrame() {
 		return (DIFrame) getDNode();
 	}
 
 	void setContentDocument(AHtmlDocument frameDoc) {
 		m_contentDocument = frameDoc;
 	}
 
 }
