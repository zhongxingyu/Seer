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
 
 public class GeolocationTag extends SimpleTagSupport {
 
     private String id;
     private String name;
     private boolean disabled;
 
     private static final String CONTENT_WRAPPER_CLASS = ".mobi-panel-stack";
 
 
     public void doTag() throws IOException {
 
         PageContext pageContext = (PageContext) getJspContext();
 
         boolean isEnhanced = TagUtil.isEnhancedBrowser(pageContext);
         Writer out = pageContext.getOut();
 
         out.write("<span ");
 
         if (id != null && !"".equals(id)) {
             out.write(" id=\'" + getId() + "\'");
         }
         out.write("> <input type=\'hidden\' ");
 
         if (id != null && !"".equals(id)) {
             out.write(" id=\'" + getId() + "_locHidden\'");
            out.write(" name=\'" + getId() + "_field\'");
         }
 
         if (disabled) {
             out.write(" disabled=\'true\'");
         }
 
         out.write("/>");  // end input
        out.write("/span>");   // end span
 
         StringBuilder sb = new StringBuilder(255);
         String fnCall = "document.getElementById(\"" + getId() + "_locHidden\").value=pos.coords.latitude+\",\"+pos.coords.longitude;";
         sb.append(fnCall);
 
         // todo: behaviours (?) and singleSubmit
 
        String finalScript = "navigator.geolocation.getCurrentPosition(function(pos) { " + sb.toString() + "} );";
         out.write("<script ");
         if (id != null && !"".equals(id)) {
            out.write(" id = \'" + id + "_script\' ");
         }
 
         out.write(finalScript);
        out.write("/>");
 
     }
 
     public String getId() {
         return id;
     }
 
     public void setId(String id) {
         this.id = id;
     }
 
     public String getName() {
         return name;
     }
 
     public void setName(String name) {
         this.name = name;
     }
 
     public boolean isDisabled() {
         return disabled;
     }
 
     public void setDisabled(boolean disabled) {
         this.disabled = disabled;
     }
 }
