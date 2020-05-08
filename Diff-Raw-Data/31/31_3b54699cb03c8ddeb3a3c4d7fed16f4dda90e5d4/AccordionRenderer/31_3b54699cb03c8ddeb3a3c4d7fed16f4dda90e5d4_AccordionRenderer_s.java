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
  * Code Modification 2: [ADD BRIEF DESCRIPTION HERE]
  * Contributors: ______________________
  * Contributors: ______________________
  */
 package org.icefaces.ace.component.accordion;
 
 import java.io.IOException;
 import java.util.Map;
 
 import javax.faces.component.UIComponent;
 import javax.faces.context.FacesContext;
 import javax.faces.context.ResponseWriter;
 import javax.faces.event.PhaseId;
 
 //import org.icefaces.ace.component.tabview.Tab;
 import org.icefaces.ace.component.ajax.AjaxBehavior;
 import org.icefaces.ace.event.AccordionPaneChangeEvent;
 import org.icefaces.ace.renderkit.CoreRenderer;
import org.icefaces.ace.util.HTML;
 import org.icefaces.ace.util.JSONBuilder;
 import org.icefaces.render.MandatoryResourceComponent;
 import org.icefaces.util.EnvUtils;
 
 @MandatoryResourceComponent(tagName="accordion", value="org.icefaces.ace.component.accordion.Accordion")
 public class AccordionRenderer extends CoreRenderer {
 
 	@Override
 	public void decode(FacesContext context, UIComponent component) {
 		Accordion acco = (Accordion) component;
 		Map<String, String> params = context.getExternalContext().getRequestParameterMap();
         String activeIndex = params.get(acco.getClientId(context) + "_active");
 		
 		if(activeIndex != null) {
             if(activeIndex.equals("false"))         //collapsed all
                 acco.setActiveIndex(-1);
             else
                 acco.setActiveIndex(Integer.valueOf(activeIndex));
 		}
 
         if(acco.isTabChangeRequest(context) && acco.getActiveIndex() >= 0) {
 			if (acco.getPaneChangeListener() != null) {
 				AccordionPaneChangeEvent changeEvent = new AccordionPaneChangeEvent(acco, acco.findTabToLoad(context));
 				changeEvent.setPhaseId(PhaseId.INVOKE_APPLICATION);
 				acco.queueEvent(changeEvent);
 			}
 			decodeBehaviors(context, component);
         }
 	}
 
 	@Override
 	public void encodeEnd(FacesContext context, UIComponent component) throws IOException {
         Map<String, String> params = context.getExternalContext().getRequestParameterMap();
 		Accordion acco = (Accordion) component;
 
 		encodeMarkup(context, acco);
 	}
 	
 	protected void encodeMarkup(FacesContext context, Accordion accordionPanel) throws IOException {
 		ResponseWriter writer = context.getResponseWriter();
 		String clientId = accordionPanel.getClientId(context);
		boolean ariaEnabled = EnvUtils.isAriaEnabled(context);

 		writer.startElement("div", null);
 		writer.writeAttribute("id", clientId, null);
 		String style = accordionPanel.getStyle();
 		if(style != null) writer.writeAttribute("style", style, null);
 		String styleClass = accordionPanel.getStyleClass();
 		if(styleClass != null) writer.writeAttribute("class", styleClass, null);
 
         writer.startElement("div", null);
 
        if (ariaEnabled) encodeAriaAttributes(writer);
 		encodeTabs(context, accordionPanel);
   
         writer.endElement("div");
 
         encodeStateHolder(context, accordionPanel);
 		encodeScript(context, accordionPanel);
 
 		writer.endElement("div");
 	}
 
    private void encodeAriaAttributes(ResponseWriter writer) throws IOException {
        writer.writeAttribute(HTML.ARIA_MULTI_SELECTABLE_ATTR, "true", null);
    }

    protected void encodeScript(FacesContext context, Accordion acco) throws IOException {
 		ResponseWriter writer = context.getResponseWriter();
         JSONBuilder jb = JSONBuilder.create();
         String clientId = acco.getClientId(context);
         int activeIndex = acco.getActiveIndex();
         boolean hasTabChangeListener = acco.getPaneChangeListener() != null;
 
         for (String eventId : acco.getClientBehaviors().keySet()) {
             if (eventId.equals("panechange") && !((AjaxBehavior)(acco.getClientBehaviors().get(eventId).get(0))).isDisabled()) {
                 hasTabChangeListener = true;
             }
         }
 
 		writer.startElement("script", null);
 		writer.writeAttribute("type", "text/javascript", null);
 
         jb.initialiseVar(resolveWidgetVar(acco))
           .beginFunction("ice.ace.create")
           .item("AccordionPanel")
           .beginArray()
           .item(clientId)
 		  .beginMap()
          .entry("animated", acco.getEffect());
 
 		String event = acco.getEvent();
 
         if (event != null) jb.entry("event", event);
 		if (!acco.isAutoHeight()) jb.entry("autoHeight", false);
 		if (acco.isCollapsible()) jb.entry("collapsible", true);
 		if (acco.isFillSpace()) jb.entry("fillSpace", true);
 		if (acco.isDisabled()) jb.entry("disabled", true);
         if (hasTabChangeListener) jb.entry("ajaxTabChange", true);
 
         encodeClientBehaviors(context, acco, jb);
 
         jb.endMap()
           .endArray()
           .endFunction();
 
         writer.write(jb.toString());
 		
 		writer.endElement("script");
 	}
 
 	protected void encodeStateHolder(FacesContext context, Accordion accordionPanel) throws IOException {
 		ResponseWriter writer = context.getResponseWriter();
 		String clientId = accordionPanel.getClientId(context);
 		String stateHolderId = clientId + "_active"; 
 		
 		writer.startElement("input", null);
 		writer.writeAttribute("type", "hidden", null);
 		writer.writeAttribute("id", stateHolderId, null);
 		writer.writeAttribute("name", stateHolderId, null);
 		writer.writeAttribute("value", accordionPanel.getActiveIndex(), null);
 		writer.endElement("input");
 	}
 	
 	protected void encodeTabs(FacesContext context, Accordion acco) throws IOException {
 		ResponseWriter writer = context.getResponseWriter();

 		for(int i=0; i < acco.getChildCount(); i++) {
 			UIComponent kid = acco.getChildren().get(i);
 			
 			if(kid.isRendered() && kid instanceof AccordionPane) {
 				AccordionPane tab = (AccordionPane) kid;
                 String clientId = kid.getClientId(context);
 				
 				//title
 				writer.startElement("h3", null);
                 writer.writeAttribute("id", clientId + "_header", null);
 				writer.startElement("a", null);
 				writer.writeAttribute("href", "#", null);
 				if(tab.getTitle() != null) {
 					writer.write(tab.getTitle());
 				}
 				writer.endElement("a");
 				writer.endElement("h3");
 				
 				//content
 				writer.startElement("div", null);
                 writer.writeAttribute("id", clientId, null);
 
 				writer.startElement("div", null);
                 writer.writeAttribute("id", clientId + "_content", null);
 				
                 tab.encodeAll(context);

 				writer.endElement("div");
 				writer.endElement("div");
 			}
 		}
 	}
 
     @Override
 	public void encodeChildren(FacesContext context, UIComponent component) throws IOException {
 		//Do nothing
 	}
 
     @Override
 	public boolean getRendersChildren() {
 		return true;
 	}
 }
