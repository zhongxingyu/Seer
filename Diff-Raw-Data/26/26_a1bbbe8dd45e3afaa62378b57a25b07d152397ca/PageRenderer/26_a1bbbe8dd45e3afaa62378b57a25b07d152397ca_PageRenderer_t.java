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
 import java.util.List;
 import java.util.ListIterator;
 import java.util.Map;
 import javax.el.ELContext;
 import javax.el.ExpressionFactory;
 import javax.el.ValueExpression;
 import javax.faces.component.UIComponent;
 import javax.faces.component.UIOutput;
 import javax.faces.component.UIViewRoot;
 import javax.faces.context.FacesContext;
 import javax.faces.context.ResponseWriter;
 import org.primefaces.mobile.util.Constants;
 import org.primefaces.renderkit.CoreRenderer;
 
 public class PageRenderer extends CoreRenderer {
 
     /* SAH */
     public static final String LibraryName = "primefaces-mobile";
     
     @Override
     public void encodeBegin(FacesContext context, UIComponent component) throws IOException {        
         ResponseWriter writer = context.getResponseWriter();
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
         writer.startElement("meta", null);
         writer.writeAttribute("name", "viewport", null);
        writer.writeAttribute("content", "initial-scale=1.0", null);
         writer.endElement("meta");
         
         /*
         //user defined meta
         if(meta != null) {
             meta.encodeAll(context);
         }*/
 
         writer.startElement("title", null);
         writer.write(page.getTitle());
         writer.endElement("title");
         
         if(preinit != null) {
             preinit.encodeAll(context);
         }
         
         // Output PF theme
         if(theme != null) {
             ELContext elContext = context.getELContext();
             ExpressionFactory expressionFactory = context.getApplication().getExpressionFactory();
             ValueExpression ve = expressionFactory.createValueExpression(elContext, theme, String.class);
 
             theme = (String) ve.getValue(elContext);
         } 
         else {
             theme = "aristo";   //default
         }
 
         if(theme != null && !theme.equals("none")) {
             renderResource(context, "theme.css", "javax.faces.resource.Stylesheet", "primefaces-" + theme, null);
         }
         
         // jQuery
         renderResource(context, "jquery/jquery.js", "javax.faces.resource.Script", "primefaces", null);
         
         // Output primefaces content first.
         renderResource(context, "primefaces.js", "javax.faces.resource.Script", "primefaces", null);
         
         // Then JQM content
         // Now in the POM
         //renderResource(context, "jquery.mobile-1.2.0.js", "javax.faces.resource.Script", LibraryName, null);
         //renderResource(context, "jquery.mobile-1.2.0.css", "javax.faces.resource.Stylesheet", LibraryName, null);        
 
         // Then override with pf-mobile content.
         renderResource(context, "primefaces-mobile-full.css", "javax.faces.resource.Stylesheet", LibraryName, null);
         renderResource(context, "primefaces-mobile-full.js", "javax.faces.resource.Script", LibraryName, null);
         
         // Registered resources - from primefaces
         UIViewRoot viewRoot = context.getViewRoot();
         ListIterator<UIComponent> iter = (viewRoot.getComponentResources(context, "head")).listIterator();
         while (iter.hasNext()) {
             writer.write("\n");
             UIComponent resource = (UIComponent) iter.next();
             resource.encodeAll(context);
         }
         
         // Moved specification to POM so that we can use the PrimeFaces extensions
         // resource optimizer to compress the JS/CSS.
         // iScroll with jQuery mobile fix: https://github.com/meckdahl/iscroll/tree/master/src
         //renderResource(context, "iscroll.js", "javax.faces.resource.Script", LibraryName, null);
         //renderResource(context, "primefaces-mobile.js", "javax.faces.resource.Script", LibraryName, null);
         //renderResource(context, "mobile.css", "javax.faces.resources.Stylesheet", LibraryName, null);
         
         // Cordova JS files.
         //renderResource(context, "cordova.js", "javax.faces.resource.Script", LibraryName, null);
         //renderResource(context, "ExternalFileUtil.js", "javax.faces.resource.Script", LibraryName, null);
         
         // Then override with my own CSS/scripts.
         //renderResource(context, "mobilehelix.css", "javax.faces.resource.Stylesheet", LibraryName, null);
 
         // Then handle the user's postinit facet.
         if(postinit != null) {
             List<UIComponent> children = postinit.getChildren();
             for (UIComponent postinitChild : children) {
                 postinitChild.encodeAll(context);
             }
         }
         
         // Finally output the init script.
         
         //config options
         writer.startElement("script", null);
         writer.writeAttribute("type", "text/javascript", null);
         
         // Cordova JS files.
         /*writer.write("if ($.browser.webkit) {");
         writer.write("$.getScript('http://__mh_local/cordova');");
         writer.write("$.getScript('http://__mh_local/ExternalFileUtil');");
         writer.write("}");*/
 
         // Set a global variable with the context root.
         writer.write("PrimeFaces.contextRoot = '" + context.getExternalContext().getRequestContextPath() + "';");
         
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
