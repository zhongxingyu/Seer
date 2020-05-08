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
 package org.primefaces.mobile.component.page;
 
 import java.io.IOException;
 import java.util.Map;
 import javax.faces.application.Resource;
 import javax.faces.component.UIComponent;
 import javax.faces.component.UIViewRoot;
 import javax.faces.context.FacesContext;
 import javax.faces.context.ResponseWriter;
 import org.primefaces.config.ConfigContainer;
 import org.primefaces.context.RequestContext;
 import org.primefaces.mobile.util.Constants;
 import org.primefaces.renderkit.CoreRenderer;
 
 public class PageRenderer extends CoreRenderer {
 
     @Override
     public void encodeBegin(FacesContext context, UIComponent component) throws IOException {        
         ResponseWriter writer = context.getResponseWriter();
         ConfigContainer cc = RequestContext.getCurrentInstance().getApplicationContext().getConfig();
         Page page = (Page) component;
         UIComponent meta = page.getFacet("meta");
         UIComponent config = page.getFacet("config");
         UIComponent preinit = page.getFacet("preinit");
         UIComponent postinit = page.getFacet("postinit");
         
         if(page.isMini()) {
             context.getAttributes().put(Constants.MINI_FORMS, true);
         }
         
         //Theme
         String theme = context.getExternalContext().getInitParameter(Constants.THEME_PARAM);
 
         writer.write("<!DOCTYPE html>\n");
         writer.startElement("html", page);
         if(page.getManifest() != null) {
             writer.writeAttribute("manifest", page.getManifest(), "manifest");
         }
                 
         writer.startElement("head", page);
         
         //viewport meta
         writer.startElement("meta", page);
         writer.writeAttribute("name", "viewport", null);
         writer.writeAttribute("content", page.getViewport(), null);
         writer.endElement("meta");
         
         //user defined meta
         if(meta != null) {
             meta.encodeAll(context);
         }
 
         writer.startElement("title", page);
         writer.write(page.getTitle());
         writer.endElement("title");
         
         if(preinit != null) {
             preinit.encodeAll(context);
         }
 
         if(theme != null && theme.equals("none")) {
             renderResource(context, "structure.css", "javax.faces.resource.Stylesheet", "primefaces-mobile");
         }
         else {
             renderResource(context, "mobile.css", "javax.faces.resource.Stylesheet", "primefaces-mobile");
         }
         
         renderResource(context, "jquery/jquery.js", "javax.faces.resource.Script", "primefaces");
 
         //config options
         writer.startElement("script", null);
         writer.writeAttribute("type", "text/javascript", null);
         
         writer.write("$(document).bind('mobileinit', function(){");
         writer.write("$.mobile.ajaxEnabled = false;");               
         writer.write("$.mobile.pushStateEnabled = false;");        
         writer.write("$.mobile.page.prototype.options.domCache = true;");
         
         if (page.getLoadingMessage() != null) {
             writer.write("$.mobile.loader.prototype.options.text = '" + page.getLoadingMessage() + "';");
             writer.write("$.mobile.loader.prototype.options.textVisible = 'true';");
         }
         if(page.getDefaultPageTransition() != null) writer.write("$.mobile.defaultPageTransition = '" + page.getDefaultPageTransition() + "';");
         if(page.getDefaultDialogTransition() != null) writer.write("$.mobile.defaultDialogTransition = '" + page.getDefaultDialogTransition() + "';");
         
         if(config != null) {
             config.encodeAll(context);
         }
         
         writer.write("});");
         
         writer.endElement("script");
                
         renderResource(context, "mobile.js", "javax.faces.resource.Script", "primefaces-mobile");
        renderResource(context, "core/core.js", "javax.faces.resource.Script", "primefaces");
         
         if(cc.isClientSideValidationEnabled()) {
              encodeValidationResources(context, cc.isBeanValidationAvailable());
             
             writer.startElement("script", null);
             writer.writeAttribute("type", "text/javascript", null);
             writer.write("PrimeFaces.settings.locale = '" + context.getViewRoot().getLocale() + "';");
             writer.write("PrimeFaces.settings.validateEmptyFields = " + cc.isValidateEmptyFields() + ";");
             writer.write("PrimeFaces.settings.considerEmptyStringNull = " + cc.isInterpretEmptyStringAsNull() + ";");
             writer.endElement("script");
         }          
         
         renderResource(context, "primefaces-mobile.js", "javax.faces.resource.Script", "primefaces-mobile");        
         
         if(postinit != null) {
             postinit.encodeAll(context);
         }
         
         //Registered Resources
         UIViewRoot viewRoot = context.getViewRoot();
         for (UIComponent resource : viewRoot.getComponentResources(context, "head")) {            
             if (resource.getAttributes().containsKey("com.sun.faces.facelets.MARK_ID")) {
                 resource.encodeAll(context);                
             }            
         }    
                        
         writer.endElement("head");
 
         writer.startElement("body", page);
         writer.writeAttribute("onunload", "", null);        
     }
 
     @Override
     public void encodeEnd(FacesContext context, UIComponent component) throws IOException {
         ResponseWriter writer = context.getResponseWriter();
 
         writer.endElement("body");
         writer.endElement("html");
 
     }
 
     protected void renderResource(FacesContext context, String resourceName, String renderer, String library) throws IOException {
         UIComponent resource = context.getApplication().createComponent("javax.faces.Output");
         resource.setRendererType(renderer);
 
         Map<String, Object> attrs = resource.getAttributes();
         attrs.put("name", resourceName);
         attrs.put("library", library);
         attrs.put("target", "head");        
 
         resource.encodeAll(context);
     }    
     
     protected void encodeValidationResources(FacesContext context, boolean beanValidationEnabled) throws IOException {
         ResponseWriter writer = context.getResponseWriter();
         Resource resource = context.getApplication().getResourceHandler().createResource("validation/validation.js", "primefaces");
         
         if(resource != null) {
             writer.startElement("script", null);
             writer.writeAttribute("type", "text/javascript", null);
             writer.writeAttribute("src", resource.getRequestPath(), null);
             writer.endElement("script");
         }
         
         if(beanValidationEnabled) {
             resource = context.getApplication().getResourceHandler().createResource("validation/beanvalidation.js", "primefaces");
         
             if(resource != null) {
                 writer.startElement("script", null);
                 writer.writeAttribute("type", "text/javascript", null);
                 writer.writeAttribute("src", resource.getRequestPath(), null);
                 writer.endElement("script");
             }
         }
     }    
 }
