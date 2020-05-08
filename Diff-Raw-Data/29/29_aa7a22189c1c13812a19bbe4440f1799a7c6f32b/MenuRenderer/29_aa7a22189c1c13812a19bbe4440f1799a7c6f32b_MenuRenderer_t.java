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
 package org.icefaces.ace.component.menu;
 
 import java.io.IOException;
 import java.util.Iterator;
 
 import javax.faces.component.UIComponent;
 import javax.faces.context.FacesContext;
 import javax.faces.context.ResponseWriter;
 
 import org.icefaces.ace.component.menuitem.MenuItem;
 import org.icefaces.ace.component.submenu.Submenu;
 import org.icefaces.ace.component.menuseparator.MenuSeparator;
 import org.icefaces.ace.util.JSONBuilder;
 import org.icefaces.ace.util.Utils;
 import org.icefaces.render.MandatoryResourceComponent;
 
 @MandatoryResourceComponent(tagName="menu", value="org.icefaces.ace.component.menu.Menu")
 public class MenuRenderer extends BaseMenuRenderer {
 
     @Override
 	public void encodeEnd(FacesContext context, UIComponent component) throws IOException {
 		AbstractMenu menu = (AbstractMenu) component;
 
 		if(menu.shouldBuildFromModel()) {
 			menu.buildMenuFromModel();
 		}
 
 		encodeMarkup(context, menu);
 	}
 	
     protected void encodeScript(FacesContext context, AbstractMenu abstractMenu) throws IOException {
 		ResponseWriter writer = context.getResponseWriter();
         Menu menu = (Menu) abstractMenu;
 		String clientId = menu.getClientId(context);
 		String widgetVar = this.resolveWidgetVar(menu);
         String position = menu.getPosition();
         String type = menu.getType();
 
 		writer.startElement("script", null);
 		writer.writeAttribute("type", "text/javascript", null);
 
         JSONBuilder json = JSONBuilder.create();
 		writer.write(widgetVar + " = new ");
         json.beginFunction("ice.ace.Menu").
             item(clientId).
             beginMap().
 
                 entry("position", menu.getPosition()).
                 entry("zindex", menu.getZindex()).
 
                 //animation
                 beginMap("animation").
                     entry("animated", menu.getEffect()).
                     entry("duration", menu.getEffectDuration()).
                 endMap();
 
                 if(type.equalsIgnoreCase("sliding")) {
                     json.entry("mode", "sliding").
                     entry("backLinkText", menu.getBackLabel()).
                     entry("maxHeight", menu.getMaxHeight());
                 }
         
                 if(position.equalsIgnoreCase("dynamic")) {
                    json.entry("my", menu.getMy()).
                    entry("at", menu.getAt());
 
                    String triggerId = menu.getTrigger();
					if (triggerId != null) {
						UIComponent trigger = menu.findComponent(triggerId);
						if(trigger != null) {
							json.entry("trigger", trigger.getClientId(context)).
							entry("triggerEvent", menu.getTriggerEvent());
						}
						else {
							json.entry("trigger", triggerId);
						}
					}
                 }
 
                 json.entryNonNullValue("styleClass", menu.getStyleClass()).
                 entryNonNullValue("style", menu.getStyle()).
             endMap().
         endFunction();
 
         writer.write(json.toString());
 
 		writer.endElement("script");
 	}
 
 	protected void encodeMarkup(FacesContext context, AbstractMenu abstractMenu) throws IOException {
 		ResponseWriter writer = context.getResponseWriter();
         Menu menu = (Menu) abstractMenu;
 		String clientId = menu.getClientId(context);
         boolean tiered = menu.isTiered() || !menu.getType().equalsIgnoreCase("plain");
 
         writer.startElement("span", menu);
 		writer.writeAttribute("id", clientId, "id");
 
 		writer.startElement("ul", null);
 
         if(tiered) {
             encodeTieredMenuContent(context, menu);
         }
         else {
             encodePlainMenuContent(context, menu, false);
         }
 
 		writer.endElement("ul");
 		
 		encodeScript(context, menu);
 
         writer.endElement("span");
 	}
 
     protected void encodeTieredMenuContent(FacesContext context, UIComponent component) throws IOException {
         ResponseWriter writer = context.getResponseWriter();
 
         for(Iterator<UIComponent> iterator = component.getChildren().iterator(); iterator.hasNext();) {
             UIComponent child = (UIComponent) iterator.next();
 
             if(child.isRendered()) {
 
                 writer.startElement("li", null);
 
                 if(child instanceof MenuItem) {
                     encodeMenuItem(context, (MenuItem) child);
                 } else if(child instanceof MenuSeparator) {
                     // we just need <li></li>
                 } else if(child instanceof Submenu) {
                     encodeTieredSubmenu(context, (Submenu) child);
                 }
 
                 writer.endElement("li");
             }
         }
     }
 
 	protected void encodeTieredSubmenu(FacesContext context, Submenu submenu) throws IOException{
 		ResponseWriter writer = context.getResponseWriter();
         String icon = submenu.getIcon();
         String label = submenu.getLabel();
 		boolean disabled = submenu.isDisabled();
 
         //title
         writer.startElement("a", null);
 		if (disabled) {
 			writer.writeAttribute("class", "ui-state-disabled", null);
 		} else {
 			writer.writeAttribute("href", "#", null);
 		}
 
         if(icon != null) {
             writer.startElement("span", null);
             writer.writeAttribute("class", icon + " wijmo-wijmenu-icon-left", null);
             writer.endElement("span");
         }
 
         if(label != null) {
             writer.startElement("span", null);
             String style = submenu.getStyle();
             if (style != null && style.trim().length() > 0) {
                 writer.writeAttribute("style", style, "style");
             }
             Utils.writeConcatenatedStyleClasses(writer, "wijmo-wijmenu-text", submenu.getStyleClass());
             writer.write(submenu.getLabel());
             writer.endElement("span");
         }
 
         writer.endElement("a");
 
         //submenus and menuitems
 		if(submenu.getChildCount() > 0 && !disabled) {
 			writer.startElement("ul", null);
 
 			encodeTieredMenuContent(context, submenu);
 
 			writer.endElement("ul");
 		}
 	}
 
     protected void encodePlainMenuContent(FacesContext context, UIComponent component, boolean disableChildren) throws IOException{
 		ResponseWriter writer = context.getResponseWriter();
 
         for(Iterator<UIComponent> iterator = component.getChildren().iterator(); iterator.hasNext();) {
             UIComponent child = (UIComponent) iterator.next();
 
             if(child.isRendered()) {
 
                 if(child instanceof MenuItem) {
                     writer.startElement("li", null);
                     encodeMenuItem(context, (MenuItem) child, disableChildren);
                     writer.endElement("li");
                 } else if(child instanceof MenuSeparator) {
                     encodeMenuSeparator(context);
                 } else if(child instanceof Submenu) {
                     encodePlainSubmenu(context, (Submenu) child, disableChildren);
                 }
                 
             }
         }
     }
 
     protected void encodePlainSubmenu(FacesContext context, Submenu submenu, boolean disableChildren) throws IOException {
         ResponseWriter writer = context.getResponseWriter();
         String label = submenu.getLabel();
 		boolean disabled = submenu.isDisabled();
 
         //title
         writer.startElement("li", null);
         writer.startElement("h3", null);
         if(label != null) {
 			if (disabled || disableChildren) {
 				writer.writeAttribute("class", "ui-state-disabled", null);
 			} else {
 				String style = submenu.getStyle();
 				if (style != null && style.trim().length() > 0) {
 					writer.writeAttribute("style", style, "style");
 				}
 				Utils.writeConcatenatedStyleClasses(writer, "", submenu.getStyleClass());
 			}
             writer.write(label);
         }
         writer.endElement("h3");
         writer.endElement("li");
 
         encodePlainMenuContent(context, submenu, disabled || disableChildren);
 	}
 }
