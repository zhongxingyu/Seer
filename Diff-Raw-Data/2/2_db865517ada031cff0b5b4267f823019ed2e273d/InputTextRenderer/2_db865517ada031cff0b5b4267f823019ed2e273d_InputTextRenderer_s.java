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
 
 package com.icesoft.faces.component.ext.renderkit;
 
 import com.icesoft.faces.component.ExtendedAttributeConstants;
 import com.icesoft.faces.component.IceExtended;
 import com.icesoft.faces.component.ext.HtmlInputText;
 import com.icesoft.faces.component.ext.KeyEvent;
 import com.icesoft.faces.component.ext.taglib.Util;
 import com.icesoft.faces.context.effects.LocalEffectEncoder;
 import com.icesoft.faces.renderkit.dom_html_basic.DomBasicRenderer;
 import com.icesoft.faces.renderkit.dom_html_basic.HTML;
 import com.icesoft.faces.renderkit.dom_html_basic.PassThruAttributeWriter;
 
 import javax.faces.component.UIComponent;
 import javax.faces.context.FacesContext;
 import javax.faces.context.ResponseWriter;
 import javax.faces.event.ActionEvent;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Map;
 
 public class InputTextRenderer extends com.icesoft.faces.renderkit.dom_html_basic.InputTextRenderer {
     // LocalEffectEncoder takes ownership of any passthrough attributes
     private static final String[] jsEvents = LocalEffectEncoder.maskEvents(
             ExtendedAttributeConstants.getAttributes(
                     ExtendedAttributeConstants.ICE_INPUTTEXT));
     private static final String[] passThruAttributes =
             ExtendedAttributeConstants.getAttributes(
                     ExtendedAttributeConstants.ICE_INPUTTEXT,
                     jsEvents);
     private static Map rendererJavascript;
     private static Map rendererJavascriptPartialSubmit;
 
     static {
         rendererJavascript = new HashMap();
         rendererJavascript.put(HTML.ONKEYUP_ATTR,
                 DomBasicRenderer.ICESUBMIT);
         rendererJavascript.put(HTML.ONKEYPRESS_ATTR,
                "var e = $event(event); if (e.isEnterKey()) e.cancelDefaultAction();");
         rendererJavascript.put(HTML.ONFOCUS_ATTR,
                 "setFocus(this.id);");
         rendererJavascript.put(HTML.ONBLUR_ATTR,
                 "setFocus('');");
         rendererJavascript.put(HTML.ONMOUSEDOWN_ATTR, "this.focus();");
         rendererJavascriptPartialSubmit = new HashMap();
         rendererJavascriptPartialSubmit.put(HTML.ONKEYUP_ATTR,
                 DomBasicRenderer.ICESUBMIT);
         rendererJavascriptPartialSubmit.put(HTML.ONFOCUS_ATTR,
                 "setFocus(this.id);");
         rendererJavascriptPartialSubmit.put(HTML.ONBLUR_ATTR,
                 "setFocus('');" + DomBasicRenderer.ICESUBMITPARTIAL);
         rendererJavascriptPartialSubmit.put(HTML.ONMOUSEDOWN_ATTR, "this.focus();");
     }
 
     protected void renderHtmlAttributes(FacesContext facesContext, ResponseWriter writer, UIComponent uiComponent)
             throws IOException {
         PassThruAttributeWriter.renderHtmlAttributes(
                 writer, uiComponent, passThruAttributes);
         //renderer is responsible to write the autocomplete attribute
         Object autoComplete = ((HtmlInputText) uiComponent).getAutocomplete();
         if (autoComplete != null) {
             writer.writeAttribute(HTML.AUTOCOMPLETE_ATTR, autoComplete, null);
         }
         Map rendererJS = ((IceExtended) uiComponent).getPartialSubmit()
                 ? rendererJavascriptPartialSubmit : rendererJavascript;
 
         LocalEffectEncoder.encode(
                 facesContext, uiComponent, jsEvents, rendererJS, null, writer);
     }
 
     public void decode(FacesContext facesContext, UIComponent uiComponent) {
         super.decode(facesContext, uiComponent);
         Object focusId = facesContext.getExternalContext()
                 .getRequestParameterMap().get(FormRenderer.getFocusElementId());
         if (focusId != null) {
             if (focusId.toString()
                     .equals(uiComponent.getClientId(facesContext))) {
                 ((HtmlInputText) uiComponent).setFocus(true);
             } else {
                 ((HtmlInputText) uiComponent).setFocus(false);
             }
         }
 
         if (Util.isEventSource(facesContext, uiComponent)) {
             queueEventIfEnterKeyPressed(facesContext, uiComponent);
         }
     }
 
 
     public void queueEventIfEnterKeyPressed(FacesContext facesContext,
                                             UIComponent uiComponent) {
         try {
             KeyEvent keyEvent =
                     new KeyEvent(uiComponent, facesContext.getExternalContext()
                             .getRequestParameterMap());
             if (keyEvent.getKeyCode() == KeyEvent.CARRIAGE_RETURN) {
                 uiComponent.queueEvent(new ActionEvent(uiComponent));
             }
         } catch (Exception e) {
             e.printStackTrace();
         }
     }
 }
