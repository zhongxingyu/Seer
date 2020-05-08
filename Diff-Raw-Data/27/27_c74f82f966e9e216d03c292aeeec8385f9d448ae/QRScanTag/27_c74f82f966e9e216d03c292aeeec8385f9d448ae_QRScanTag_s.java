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
  *
  */
 
 package org.icemobile.jsp.tags;
 
 import javax.servlet.jsp.PageContext;
 import javax.servlet.jsp.tagext.SimpleTagSupport;
 import java.io.IOException;
 import java.io.Writer;
 
 public class QRScanTag extends SimpleTagSupport {
 
     private String id;
     private String style;
     private String styleClass;
 
     public void doTag() throws IOException {
 
         PageContext pageContext = (PageContext) getJspContext();
         boolean isEnhanced = TagUtil.isEnhancedBrowser(pageContext);
         Writer out = pageContext.getOut();
 
         // todo; do the -sx method of buffer detection as well.
 
         StringBuffer buffer = new StringBuffer(TagUtil.SPAN_TAG);
         if (!isEnhanced) {
 
            buffer.append(" ").append(TagUtil.INPUT_TAG).append(" type=\"text\"");
             buffer.append(" id=\"").append(getId()).append("\"");
             buffer.append(" name=\"").append(getId()).append("\">");
             buffer.append(TagUtil.INPUT_TAG_END).append(TagUtil.SPAN_TAG_END);
             out.write(buffer.toString());
             return;
         }
 
         buffer.append(" id=\"").append(getId()).append("\" >");
         buffer.append(" <input type=\"button\" id=\"");
        buffer.append(getId()).append("-button\" value=\"Scan QR Code\"");
         buffer.append(" style=\"mobi-button mobi-button-default");
 
         // User defined class
         String style = getStyleClass();
         if (style != null && !style.equals("")) {
             buffer.append(" ").append(style);
         }
         buffer.append("\"");
 
         // User defined style
         String userStyle = getStyle();
         if (userStyle != null && !userStyle.equals("")) {
             buffer.append(" style=\"").append(userStyle).append("\"");
         }
 
         String script;
 //        if (isAuxUpload)  {
 //            script = Utils.getICEmobileSXScript("scan", getId());
 //        } else {
         script = "ice.scan('" + getId() + "');";
 //        }
 
         buffer.append(" onclick=\"").append(script).append("\"").append(TagUtil.INPUT_TAG_END);
         out.write(buffer.toString());
     }
 
     public String getId() {
         return id;
     }
 
     public void setId(String id) {
         this.id = id;
     }
 
     public String getStyle() {
         return style;
     }
 
     public void setStyle(String style) {
         this.style = style;
     }
 
     public String getStyleClass() {
         return styleClass;
     }
 
     public void setStyleClass(String styleClass) {
         this.styleClass = styleClass;
     }
 }
