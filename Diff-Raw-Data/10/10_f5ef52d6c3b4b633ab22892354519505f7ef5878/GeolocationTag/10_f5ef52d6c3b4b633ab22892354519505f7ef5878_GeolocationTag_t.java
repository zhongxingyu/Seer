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
     private String value;
     private boolean disabled;
 
     private static final String CONTENT_WRAPPER_CLASS = ".mobi-panel-stack";
 
 
     public void doTag() throws IOException {
 
         PageContext pageContext = (PageContext) getJspContext();
 
         boolean isEnhanced = TagUtil.isEnhancedBrowser(pageContext);
         Writer out = pageContext.getOut();
 
         out.write("<span ");
 
         if (id != null && !"".equals(id)) {
             out.write(" id='" + getId() + "'");
         }
         out.write(">");
         out.write("<input type='hidden' ");
 
        if (id != null && !"".equals(id)) {
            out.write(" id='" + getId() + "_locHidden'");
            out.write(" name='" + getId() + "' ");
            out.write(" value='' ");
         }
 
         if (disabled) {
             out.write("disabled='true' ");
         }
 
         out.write(">");  // end input
 
         out.write("<script ");
         if (id != null && !"".equals(id)) {
             out.write("id ='" + id + "_script' ");
         }
         out.write(">\n");
         out.write("iceStoreLocation = function(id, coords)  {");
         out.write("if (!coords) { return; } ");
         out.write("var el = document.getElementById(id + '_locHidden');");
         out.write("var parts = el.value.split(',');");
         out.write("if (4 != parts.length) {parts = new Array(4)};");
         out.write("parts[0] = coords.latitude;");
         out.write("parts[1] = coords.longitude;");
         out.write("parts[2] = coords.altitude;");
         out.write("el.value = parts.join();");
         out.write("}\n");
         out.write("iceStoreDirection = function(id, head)  {");
         out.write("var el = document.getElementById(id + '_locHidden');");
         out.write("var parts = el.value.split(',');");
         out.write("if (4 != parts.length) {parts = new Array(4)}");
         out.write("parts[3] = head;");
         out.write("el.value = parts.join();");
         out.write("}\n");
         //need getCurrentPosition and watchPosition to ensure location is
         //available after page loads
         out.write("iceStoreLocation('" + getId() + "', window.icegeocoords);");
         out.write("navigator.geolocation.getCurrentPosition(function(pos){");
         out.write("window.icegeocoords =  pos.coords;");
         out.write("iceStoreLocation('" + getId() + "', pos.coords);");
         out.write("});\n");
         out.write("navigator.geolocation.watchPosition(function(pos){");
         out.write("window.icegeocoords =  pos.coords;");
         out.write("iceStoreLocation('" + getId() + "', pos.coords);");
         out.write("});\n");
         out.write("window.addEventListener('deviceorientation', function(orient){");
         out.write("iceStoreDirection('" + getId() + "', orient.webkitCompassHeading);");
         out.write("});\n");
         out.write("</script>");
         out.write("</span>");   // end span
 
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
 
     public String getValue() {
         return value;
     }
 
     public void setValue(String value) {
         this.value = value;
     }
 
 }
