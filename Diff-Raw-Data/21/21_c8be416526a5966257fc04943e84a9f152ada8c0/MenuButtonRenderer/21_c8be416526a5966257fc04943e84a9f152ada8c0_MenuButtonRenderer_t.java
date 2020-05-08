 /*
  * Original Code Copyright Prime Technology.
  * Subsequent Code Modifications Copyright 2011-2012 ICEsoft Technologies Canada Corp. (c)
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
  *
  * NOTE THIS CODE HAS BEEN MODIFIED FROM ORIGINAL FORM
  *
  * Subsequent Code Modifications have been made and contributed by ICEsoft Technologies Canada Corp. (c).
  *
  * Code Modification 1: Integrated with ICEfaces Advanced Component Environment.
  * Contributors: ICEsoft Technologies Canada Corp. (c)
  *
  * Code Modification 2: (ICE-6978) Used JSONBuilder to add the functionality of escaping JS output.
  * Contributors: ICEsoft Technologies Canada Corp. (c)
  * Contributors: ______________________
  */
 package org.icefaces.ace.component.menubutton;
 
 import java.io.IOException;
 
 import javax.faces.FacesException;
 import javax.faces.component.UIComponent;
 import javax.faces.context.FacesContext;
 import javax.faces.context.ResponseWriter;
 import org.icefaces.ace.component.menu.AbstractMenu;
 import org.icefaces.ace.component.menu.BaseMenuRenderer;
 
 import org.icefaces.ace.component.menuitem.MenuItem;
 import org.icefaces.ace.component.menuseparator.MenuSeparator;
 import org.icefaces.ace.util.ComponentUtils;
 import org.icefaces.ace.util.JSONBuilder;
 import org.icefaces.render.MandatoryResourceComponent;
 
 @MandatoryResourceComponent(tagName="menuButton", value="org.icefaces.ace.component.menubutton.MenuButton")
 public class MenuButtonRenderer extends BaseMenuRenderer {
 
     @Override
 	public void encodeEnd(FacesContext context, UIComponent component) throws IOException {
 		AbstractMenu menu = (AbstractMenu) component;
 
 		if(menu.shouldBuildFromModel()) {
 			menu.buildMenuFromModel();
 		}
 
 		encodeMarkup(context, menu);
 	}
 	
    protected void encodeMarkup(FacesContext context, AbstractMenu abstractMenu) throws IOException {
 		ResponseWriter writer = context.getResponseWriter();
         MenuButton button = (MenuButton) abstractMenu;
 		String clientId = button.getClientId(context);
 		String buttonId = clientId + "_button";
 		
 		writer.startElement("span", button);
 		writer.writeAttribute("id", clientId, "id");
 
        String styleClass = button.getStyleClass();
        if(styleClass != null) writer.writeAttribute("class", styleClass, "class");
        String style = button.getStyle();
        if(style != null) writer.writeAttribute("style", style, "style");
 
         //button
 		writer.startElement("button", null);
 		writer.writeAttribute("id", buttonId, null);
 		writer.writeAttribute("name", buttonId, null);
 		writer.writeAttribute("type", "button", null);
 		if(button.getValue() != null) {
 			writer.write(button.getValue());
 		}
		
		// script to (re)initialize when only the button is dynamically updated
		writer.startElement("script", button);
		writer.writeAttribute("type", "text/javascript", null);
		writer.write("ice.ace.jq(function(){var b = ice.ace.jq(ice.ace.escapeClientId('"+buttonId+"')); if (!b.hasClass('ui-button')) { "+getInitCall(writer, button, clientId)+" }});");
		writer.endElement("script");
		
 		writer.endElement("button");
 
         //menu
         writer.startElement("ul", null);
 
 		for(UIComponent child : button.getChildren()) {
 
 			if(child.isRendered()) {
                 writer.startElement("li", child);
                 if (child instanceof MenuItem)
                     encodeMenuItem(context, (MenuItem) child);
                 writer.endElement("li");
 			}
 		}
 
 		writer.endElement("ul");
 		
 		encodeScript(context, button);
 		
 		writer.endElement("span");
 	}
 
 	protected void encodeScript(FacesContext context, AbstractMenu abstractMenu) throws IOException {
 		ResponseWriter writer = context.getResponseWriter();
         MenuButton button = (MenuButton) abstractMenu;
 		String clientId = button.getClientId(context);
 		
 		UIComponent form = ComponentUtils.findParentForm(context, button);
 		if(form == null) {
 			throw new FacesException("MenuButton : \"" + clientId + "\" must be inside a form element");
 		}
 		
 		String formClientId = form.getClientId(context);
 		
 		writer.startElement("script", button);
 		writer.writeAttribute("type", "text/javascript", null);
 
 		writer.write(getInitCall(writer, button, clientId));

		writer.endElement("script");
	}
	
	private String getInitCall(ResponseWriter writer, MenuButton button, String clientId)  throws IOException {
         JSONBuilder json = JSONBuilder.create();
 		writer.write(this.resolveWidgetVar(button) + " = new ");
         json.beginFunction("ice.ace.MenuButton").
             item(clientId).
             beginMap().
 
                 beginMap("animation").
                     entry("animated", button.getEffect()).
                     entry("duration", button.getEffectDuration()).
                 endMap().
                 entry("zindex", button.getZindex());
 
                 if(button.isDisabled()) {
                     json.entry("disabled", true);
                 }
             json.endMap().
         endFunction();
		
		return json.toString();
 	}
 }
