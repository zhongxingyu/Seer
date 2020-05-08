 /*
  * Copyright 2006 wingS development team.
  *
  * This file is part of wingS (http://wingsframework.org).
  *
  * wingS is free software; you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License
  * as published by the Free Software Foundation; either version 2.1
  * of the License, or (at your option) any later version.
  *
  * Please see COPYING for the complete licence.
  */
 
 package org.wingx.plaf.css;
 
 import java.io.IOException;
 import java.util.*;
 
 import org.wings.*;
 import org.wings.header.SessionHeaders;
 import org.wings.header.Header;
 import org.wings.event.SParentFrameEvent;
import org.wings.event.SParentFrameListener;
 import org.wings.io.Device;
 import org.wings.plaf.css.*;
import org.wings.plaf.css.script.OnPageRenderedScript;
 import org.wings.plaf.css.script.OnHeadersLoadedScript;
 import org.wings.plaf.Update;
 import org.wings.session.ScriptManager;
 import org.wingx.XSuggest;
 
 /**
  *
  * @author Christian Schyma
  */
 public class SuggestCG
     extends TextFieldCG
     implements org.wingx.plaf.SuggestCG
 {
     protected final List<Header> headers = new ArrayList<Header>();
     
     public SuggestCG() {
         headers.add(Utils.createExternalizedJSHeaderFromProperty(Utils.JS_YUI_AUTOCOMPLETE));
         headers.add(Utils.createExternalizedCSSHeader("org/wingx/suggest/suggest.css"));
         headers.add(Utils.createExternalizedJSHeader("org/wingx/suggest/suggest.js"));
     }
 
     public void installCG(final SComponent comp) {
         super.installCG(comp);
         SessionHeaders.getInstance().registerHeaders(headers);
     }
 
     public void uninstallCG(SComponent component) {
         super.uninstallCG(component);
         SessionHeaders.getInstance().deregisterHeaders(headers);
     }
 
     protected void onChangeSubmitListener(STextField textField) {
     }
 
     protected void printPostInput(Device device, STextField textField) throws IOException {
         device.print("<div id=\"" + textField.getName() + "_popup\"></div>");
         StringBuilder builder = STRING_BUILDER.get();
         builder.setLength(0);
 
         String name = textField.getName();
         builder.append("var ds_");
         builder.append(name);
        builder.append(" = new wingS.suggest.SuggestDS(\"");
         builder.append(name);
         builder.append("\",\"");
         builder.append(name);
         builder.append("_popup\");\n");
        builder.append("var ac_");
         builder.append(name);
        builder.append(" = new YAHOO.widget.AutoComplete(\"");
         builder.append(name);
         builder.append("\", \"");
         builder.append(name);
         builder.append("_popup\", ds_");
         builder.append(name);
         builder.append(");\n");
 
         if (textField.getDocumentListeners().length > 1 || textField instanceof SFormattedTextField) {
             builder.append("var onchange_");
             builder.append(name);
             builder.append(" = function(sType, aArgs) { wingS.request.sendEvent(null, true, " + !textField.isReloadForced() + "); };\n");
            builder.append("ac_");
             builder.append(name);
             builder.append(".textboxBlurEvent.subscribe(onchange_" + name + ");\n");
         }
         ScriptManager.getInstance().addScriptListener(new OnHeadersLoadedScript(builder.toString()));
     }
 
     public void parentFrameAdded(SParentFrameEvent e) {
         SessionHeaders.getInstance().registerHeaders(headers);
     }
 
     public void parentFrameRemoved(SParentFrameEvent e) {
         SessionHeaders.getInstance().deregisterHeaders(headers);
     }
 
     public Update getSuggestionsUpdate(XSuggest suggest, String text, List<Map.Entry<String,String>> suggestions) {
         return new SuggestUpdate(suggest, text, suggestions);
     }
 
     protected class SuggestUpdate
         extends AbstractUpdate
     {
         private String string;
         private List<Map.Entry<String,String>> suggestions;
 
         public SuggestUpdate(SComponent component, String string, List<Map.Entry<String,String>> suggestions) {
             super(component);
             this.string = string;
             this.suggestions = suggestions;
         }
 
         public int getPriority() {
             return 0;
         }
 
         public Handler getHandler() {
             UpdateHandler handler = new UpdateHandler("suggest");
             handler.addParameter(component.getName());
             handler.addParameter(string);
             for (Map.Entry suggestion : suggestions) {
                 handler.addParameter(suggestion.getKey());
                 handler.addParameter(suggestion.getValue());
             }
             return handler;
         }
     }
 }
