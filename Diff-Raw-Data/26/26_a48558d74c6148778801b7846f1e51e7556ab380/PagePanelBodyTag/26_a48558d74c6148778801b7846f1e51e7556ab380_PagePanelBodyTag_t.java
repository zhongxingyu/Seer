 /*
  * Copyright 2004-2013 ICEsoft Technologies Canada Corp.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the
  * License. You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an "AS
  * IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
  * express or implied. See the License for the specific language
  * governing permissions and limitations under the License.
  */
 
 package org.icemobile.jsp.tags;
 
 import javax.servlet.jsp.JspTagException;
 import javax.servlet.jsp.tagext.Tag;
 import javax.servlet.jsp.tagext.TagSupport;
 import java.io.IOException;
 import java.io.Writer;
 import java.util.logging.Logger;
 
 /**
  *
  */
 public class PagePanelBodyTag extends TagSupport {
 
     public static final String BODY_CLASS = "mobi-pagePanel-body ";
     public static final String BODY_NO_HEADER_CLASS = "mobi-pagePanel-body-noheader";
     public static final String BODY_NO_FOOTER_CLASS = "mobi-pagePanel-body-nofooter";
     public PagePanelTag mParent;
     private String swatch = "c";
     
     private static Logger LOG = Logger.getLogger(PagePanelBodyTag.class.getName());
      public void setParent(Tag parent) {
         if (!(parent instanceof PagePanelTag)) {
             throw new IllegalArgumentException("PagePanelBodyTag must be child of PagePanelTag");
         }
         mParent = (PagePanelTag) parent;
     }
 
     public int doStartTag() throws JspTagException {
        StringBuilder bodyClass = new StringBuilder(BODY_CLASS+"ui-body-"+swatch);
         if (this.noHeader || !mParent.isHasHeader()){
             bodyClass.append(" ").append(BODY_NO_HEADER_CLASS);
         }
         if (this.noFooter){
             bodyClass.append(" ").append(BODY_NO_FOOTER_CLASS);
         }
         if (mParent.getStyleClass()!=null) {
             bodyClass.append(" ").append(mParent.getStyleClass());
         }
         Writer out = pageContext.getOut();
         StringBuilder tag = new StringBuilder(TagUtil.DIV_TAG);
         tag.append(" class=\"").append(bodyClass.toString());
         /* style is not available for body element of pagePanel for JSF */
         if (getStyle()!=null){
             tag.append("  style=\"").append(getStyle());
         }
         tag.append("\"");
         tag.append(">");
         tag.append(TagUtil.DIV_TAG).append(">");
         try {
             out.write(tag.toString());
         } catch (IOException ioe) {
             LOG.severe("IOException writing PagePanelBody: " + ioe);
         }
         return EVAL_BODY_INCLUDE;
     }
 
 
     public int doEndTag() throws JspTagException {
         Writer out = pageContext.getOut();
         try {
             out.write(TagUtil.DIV_TAG_END);
             out.write(TagUtil.DIV_TAG_END);
         } catch (IOException ioe) {
             LOG.severe("IOException closing PagePanelBody: " + ioe);
         }
         return EVAL_PAGE;
     }
 
     private boolean noHeader;
     private boolean noFooter;
     private String style;
 
     public boolean isNoHeader() {
         return noHeader;
     }
 
     public void setNoHeader(boolean noHeader) {
         this.noHeader = noHeader;
     }
 
     public boolean isNoFooter() {
         return noFooter;
     }
 
     public void setNoFooter(boolean noFooter) {
         this.noFooter = noFooter;
     }
 
     public String getStyle() {
         return style;
     }
 
     public void setStyle(String style) {
         this.style = style;
     }
     
     public String getSwatch(){ return swatch; }
     public void setSwatch(String swatch){ this.swatch = swatch; }
 
 }
