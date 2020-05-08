 /*
  *   Copyright 2012 George Norman
  *
  *   Licensed under the Apache License, Version 2.0 (the "License");
  *   you may not use this file except in compliance with the License.
  *   You may obtain a copy of the License at
  *
  *       http://www.apache.org/licenses/LICENSE-2.0
  *
  *   Unless required by applicable law or agreed to in writing, software
  *   distributed under the License is distributed on an "AS IS" BASIS,
  *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *   See the License for the specific language governing permissions and
  *   limitations under the License.
  */
 package com.thruzero.common.jsf.renderer.html5;
 
 import java.io.IOException;
 
 import javax.faces.component.UIComponent;
 import javax.faces.context.FacesContext;
 import javax.faces.render.FacesRenderer;
 
 import com.sun.faces.renderkit.html_basic.TextRenderer;
 import com.thruzero.common.jsf.components.html5.JsfHtml5Component;
 import com.thruzero.common.jsf.renderer.html5.helper.TzInputTypeResponseWriter;
 import com.thruzero.common.jsf.renderer.html5.helper.TzResponseWriter;
 
 /**
  * Adds configurable pass-through attributes to the TzText component.
  *
  * @author George Norman
  */
 @FacesRenderer(componentFamily = JsfHtml5Component.COMPONENT_FAMILY, rendererType = TzTextRenderer.RENDERER_TYPE)
 public class TzTextRenderer extends TextRenderer {
   public static final String RENDERER_TYPE = JsfHtml5Component.COMPONENT_FAMILY + ".TzTextRenderer";
 
   @Override
   public void encodeBegin(FacesContext context, UIComponent component) throws IOException {
    context.setResponseWriter(new TzInputTypeResponseWriter(context.getResponseWriter(), component, this));
 
     super.encodeBegin(context, component);
   }
 
   @Override
   public void encodeEnd(FacesContext context, UIComponent component) throws IOException {
     super.encodeEnd(context, component);
 
     context.setResponseWriter(((TzResponseWriter)context.getResponseWriter()).getWrapped());
   }
 }
