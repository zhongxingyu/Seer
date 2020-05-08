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
 package org.helix.mobile.component.page;
 
 import java.io.IOException;
 import java.util.List;
 import java.util.ListIterator;
 import java.util.Map;
 import javax.faces.application.ProjectStage;
 import javax.faces.component.UIComponent;
 import javax.faces.component.UIOutput;
 import javax.faces.component.UIViewRoot;
 import javax.faces.context.FacesContext;
 import javax.faces.context.ResponseWriter;
 import org.primefaces.renderkit.CoreRenderer;
 
 public class PageRenderer extends CoreRenderer {
 
     public static final String HelixLibraryName = "helix";
     
     @Override
     public void encodeBegin(FacesContext context, UIComponent component) throws IOException {        
         ResponseWriter writer = context.getResponseWriter();
         Page page = (Page) component;
         UIComponent config = page.getFacet("config");
         UIComponent preinit = page.getFacet("preinit");
         UIComponent postinit = page.getFacet("postinit");
         
         writer.write("<!DOCTYPE html>\n");
         writer.startElement("html", page);
         if(page.getManifest() != null) {
             writer.writeAttribute("manifest", page.getManifest(), "manifest");
         }
         
         writer.startElement("head", page);
         
         //viewport meta
         writer.startElement("meta", null);
         writer.writeAttribute("name", "viewport", null);
        writer.writeAttribute("content", "initial-scale=1.0", null);
         writer.endElement("meta");
 
         writer.startElement("title", null);
         writer.write(page.getTitle());
         writer.endElement("title");
         
         if(preinit != null) {
             preinit.encodeAll(context);
         }
         
         
         // jQuery first
         if (context.isProjectStage(ProjectStage.Development)) {
             renderResource(context, "jquery-2.0.2.js", "javax.faces.resource.Script", HelixLibraryName, null);
         } else {
             renderResource(context, "jquery-2.0.2.min.js", "javax.faces.resource.Script", HelixLibraryName, null);
         }
         
         // config options; must happen before we include jQuery Mobile, otherwise
         // we miss the mobileinit event.
         writer.startElement("script", null);
         writer.writeAttribute("type", "text/javascript", null);
         
         // Initialize jQuery Mobile
         writer.write("$(document).bind('mobileinit', function(){");
         writer.write("$.mobile.ajaxEnabled = false;");
         //writer.write("$.mobile.linkBindingEnabled = false;");
         writer.write("$.mobile.hashListeningEnabled = false;");
         writer.write("$.mobile.pushStateEnabled = false;");
         
         if(page.getLoadingMessage() != null) {
             writer.write("$.mobile.loadingMessage = '" + page.getLoadingMessage() + "';");
         }
         if(page.getDefaultPageTransition() != null) {
             writer.write("$.mobile.defaultPageTransition = '" + page.getDefaultPageTransition() + "';");
         }
         if(page.getDefaultDialogTransition() != null) {
             writer.write("$.mobile.defaultDialogTransition = '" + page.getDefaultDialogTransition() + "';");
         }
         
         if(config != null) {
             config.encodeAll(context);
         }
         
         writer.write("});");
         
         writer.endElement("script");
         
         // Then override with pf-mobile content.
         renderResource(context, "helix-mobile-full.css", "javax.faces.resource.Stylesheet", HelixLibraryName, null);
         renderResource(context, "css/helix.overrides.css", "javax.faces.resource.Stylesheet", HelixLibraryName, null);
         renderResource(context, "cordova-full.js", "javax.faces.resource.Script", HelixLibraryName, null);
         renderResource(context, "helix-mobile-full.js", "javax.faces.resource.Script", HelixLibraryName, null);
         
         writer.startElement("script", null);
         writer.writeAttribute("type", "text/javascript", null);
         
         // Set a global variable with the context root.
         writer.write("Helix.contextRoot = '" + context.getExternalContext().getRequestContextPath() + "';");
         
         writer.endElement("script");
         
         // Registered resources - from primefaces
         UIViewRoot viewRoot = context.getViewRoot();
         ListIterator<UIComponent> iter = (viewRoot.getComponentResources(context, "head")).listIterator();
         while (iter.hasNext()) {
             writer.write("\n");
             UIComponent resource = (UIComponent) iter.next();
             resource.encodeAll(context);
         }
 
         // Then handle the user's postinit facet.
         if(postinit != null) {
             List<UIComponent> children = postinit.getChildren();
             for (UIComponent postinitChild : children) {
                 postinitChild.encodeAll(context);
             }
         }
         
         writer.endElement("head");
 
         writer.startElement("body", page);
         writer.writeAttribute("style", "overflow: hidden;", null);
     }
 
     @Override
     public void encodeEnd(FacesContext context, UIComponent component) throws IOException {
         ResponseWriter writer = context.getResponseWriter();
 
         writer.endElement("body");
         writer.endElement("html");
 
     }
 
     public static void renderResource(FacesContext context, 
             String resourceName, 
             String renderer, 
             String library,
             String versionName) throws IOException {
         UIOutput resource = new UIOutput();
         resource.setRendererType(renderer);
 
         Map<String, Object> attrs = resource.getAttributes();
         if (versionName != null) {
             resourceName = resourceName + ";pmmVer=" + versionName;
         }
         attrs.put("name", resourceName);
         attrs.put("library", library);
         attrs.put("target", "head");
         
         resource.encodeAll(context);
     }
 }
