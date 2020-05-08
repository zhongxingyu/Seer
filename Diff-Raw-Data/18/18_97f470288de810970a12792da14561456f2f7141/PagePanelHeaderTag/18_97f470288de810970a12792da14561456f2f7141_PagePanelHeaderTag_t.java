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
 
 import java.io.IOException;
 import java.util.logging.Logger;
 
 import javax.servlet.jsp.JspTagException;
 import javax.servlet.jsp.tagext.Tag;
 
 import org.icemobile.util.HTML;
 import org.icemobile.util.CSSUtils;
 
 
 /**
  *
  */
 public class PagePanelHeaderTag extends BaseBodyTag {
 
    public static final String HEADER_CLASS = "mobi-pagePanel-header ui-header ui-header-fixed " + CSSUtils.STYLECLASS_BAR_A;
     private static Logger LOG = Logger.getLogger(PagePanelHeaderTag.class.getName());
     public PagePanelTag mParent;
 
     public void setParent(Tag parent) {
        if (!(parent instanceof PagePanelTag)) {
            throw new IllegalArgumentException("PagePanelHeaderTag must be child of PagePanelTag");
        }
        mParent = (PagePanelTag) parent;
    }
 
     public int doStartTag() throws JspTagException {
         StringBuilder headerClass = new StringBuilder(HEADER_CLASS);
         if (mParent != null) {
             mParent.setHasHeader(true);
             if (mParent.getStyleClass() !=null){
                 headerClass.append(" ").append(mParent.getStyleClass()) ;
             }
          }
         TagWriter writer = new TagWriter(pageContext);
         try {
             writer.startElement(HTML.DIV_ELEM);
             writer.writeAttribute(HTML.CLASS_ATTR, headerClass.toString());
             writer.startElement(HTML.DIV_ELEM);
             writer.writeAttribute(HTML.CLASS_ATTR, PagePanelTag.CTR_CLASS);
             writer.closeOffTag();
         } catch (IOException ioe) {
             LOG.severe("IOException writing PagePanelHeader: " + ioe);
         }
         return EVAL_BODY_INCLUDE;
     }
 
     public int doEndTag() throws JspTagException {
         TagWriter writer = new TagWriter(pageContext);
         writer.push(HTML.DIV_ELEM);
         writer.push(HTML.DIV_ELEM);
         try {
             writer.endElement();
             writer.endElement();
         } catch (IOException ioe) {
             LOG.severe("IOException closing PagePanelHeader: " + ioe);
         }
         return EVAL_PAGE;
     }
 }
