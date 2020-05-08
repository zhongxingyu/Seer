 /*
  * Copyright 2009-2011 Prime Technology.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.primefaces.mobile.renderkit;
 
 import java.io.IOException;
 import javax.faces.component.UIComponent;
 import javax.faces.component.html.HtmlOutputLink;
 import javax.faces.context.FacesContext;
 import javax.faces.context.ResponseWriter;
import org.primefaces.mobile.util.MobileUtils;
 import org.primefaces.renderkit.CoreRenderer;
 import org.primefaces.util.HTML;
 
 public class OutputLinkRenderer extends CoreRenderer {
     
     @Override
 	public void encodeEnd(FacesContext context, UIComponent component) throws IOException {
 		ResponseWriter writer = context.getResponseWriter();
 		HtmlOutputLink link = (HtmlOutputLink) component;
 		String clientId = link.getClientId(context);
 
         writer.startElement("a", link);
         writer.writeAttribute("id", clientId, "id");
         writer.writeAttribute("href", "javascript:void(0);", null);
         
         if(link.getStyleClass() != null) {
             writer.writeAttribute("class", link.getStyleClass(), null);
         }
 
         String href = (String) link.getValue();
         StringBuilder onclick = new StringBuilder();
         if(link.getOnclick() != null) {
             onclick.append(link.getOnclick()).append(";");
         }
         
         if(href != null) {
             if(href.startsWith("#")) {
                onclick.append(MobileUtils.buildNavigation(href));
             }
             else {
                 href = getResourceURL(context, href);    //external page
                 
                 onclick.append("window.location.href='").append(href).append("';");
             }            
         }
         
         if(onclick.length() > 0) {
             writer.writeAttribute("onclick", onclick.toString(), "onclick");
         }
 
         renderPassThruAttributes(context, link, HTML.LINK_ATTRS, HTML.CLICK_EVENT);
 
         renderChildren(context, link);
 
         writer.endElement("a");
 	}
 
     @Override
 	public void encodeChildren(FacesContext context, UIComponent component) throws IOException {
 		//Do Nothing
 	}
 
     @Override
 	public boolean getRendersChildren() {
 		return true;
 	}
 }
