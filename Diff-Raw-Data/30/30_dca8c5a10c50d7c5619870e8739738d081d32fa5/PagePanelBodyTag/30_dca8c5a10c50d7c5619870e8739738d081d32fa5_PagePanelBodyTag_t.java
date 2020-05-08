 /*
  * Copyright 2004-2012 ICEsoft Technologies Canada Corp.
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
 import javax.servlet.jsp.tagext.TagSupport;
 import java.io.IOException;
 import java.io.Writer;
 import java.util.logging.Logger;
 
 /**
  *
  */
 public class PagePanelBodyTag extends TagSupport {
 
     public static final String BODY_CLASS = "mobi-pagePanel-body";
     public static final String BODY_NO_HEADER_CLASS = "mobi-pagePanel-body-noheader";
     public static final String BODY_NO_FOOTER_CLASS = "mobi-pagePanel-body-nofooter";
 
     private static Logger LOG = Logger.getLogger(PagePanelBodyTag.class.getName());
 
 
     public int doStartTag() throws JspTagException {
 
         Writer out = pageContext.getOut();
 
         StringBuilder tag = new StringBuilder(TagUtil.DIV_TAG);
         tag.append(" class=\"").append(BODY_CLASS);
         if (noHeader) {
             tag.append(" ").append(BODY_NO_HEADER_CLASS);
         }
         if (noFooter) {
             tag.append(" ").append(BODY_NO_FOOTER_CLASS);
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
 }
