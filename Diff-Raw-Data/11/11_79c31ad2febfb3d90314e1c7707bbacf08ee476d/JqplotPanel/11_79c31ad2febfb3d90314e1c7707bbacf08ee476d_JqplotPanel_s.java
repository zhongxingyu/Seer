 /**
  * Copyright 2011 the original author or authors.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
  * implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package de.inren.frontend.jqplot;
 
 import java.io.IOException;
 import java.util.Scanner;
 
 import lombok.extern.slf4j.Slf4j;
 
 import org.apache.wicket.Component;
 import org.apache.wicket.behavior.AttributeAppender;
 import org.apache.wicket.behavior.Behavior;
 import org.apache.wicket.markup.head.CssHeaderItem;
 import org.apache.wicket.markup.head.IHeaderResponse;
 import org.apache.wicket.markup.head.JavaScriptHeaderItem;
 import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
 import org.apache.wicket.markup.html.panel.Panel;
 import org.apache.wicket.model.IModel;
 import org.apache.wicket.model.Model;
 import org.apache.wicket.request.resource.CssResourceReference;
 import org.apache.wicket.request.resource.JavaScriptResourceReference;
 import org.apache.wicket.util.resource.IResourceStream;
 import org.apache.wicket.util.resource.ResourceStreamNotFoundException;
 
 /**
  * @author Ingo Renner
  *
  */
 @Slf4j
 public class JqplotPanel extends Panel {
 
     public JqplotPanel(String id, IModel<IJqplotDefinition> model) {
         super(id, model);
     }
     
     @Override
     protected void onInitialize() {
         super.onInitialize();
 
         add(getJqplotBehavior());
         add(new AttributeAppender("class", Model.of("jqplot-target")));
     }
 
     private IJqplotDefinition getIJqplotDefinition() {
         return (IJqplotDefinition) getDefaultModelObject();
     }
     
     private String createJquery() {
 
         StringBuilder builder = new StringBuilder();
         builder.append("$.jqplot('").append(getMarkupId()).append("', ");
         builder.append(getIJqplotDefinition().getPlotData());
         builder.append(", ");
         builder.append(getIJqplotDefinition().getPlotConfiguration());
         builder.append(");\r\n");
 
         return builder.toString();
     }
 
     private Behavior getJqplotBehavior() {
         return new Behavior() {
 
             @Override
             public void renderHead(Component component, IHeaderResponse response) {
                 super.renderHead(component, response);
                 
                 response.render(JavaScriptHeaderItem.forReference(new JavaScriptResourceReference(JqplotPanel.class, "jquery.jqplot/jquery.jqplot.min.js")));
                 response.render(CssHeaderItem.forReference(new CssResourceReference(JqplotPanel.class, "jquery.jqplot/jquery.jqplot.min.css")));
                 for (String resource : getIJqplotDefinition().getAdditionalResources()) {
                     response.render(JavaScriptHeaderItem.forReference(new JavaScriptResourceReference(JqplotPanel.class, resource)));
                 }
 
                 response.render(OnDomReadyHeaderItem.forScript(createJquery()));
 
                 final JavaScriptResourceReference jsr = new JavaScriptResourceReference(JqplotPanel.class, "JqplotPanel.js");
                 jsr.getResource().setCompress(true);
                 IResourceStream is = jsr.getResource().getResourceStream();
                 final String jqplotPanel_js = convertStreamToString(is);
                 if (jqplotPanel_js!=null) {
                    log.info("JqplotPanel.js conmtent:\n" + jqplotPanel_js );
                     response.render(OnDomReadyHeaderItem.forScript(jqplotPanel_js));
                 } else {
                     log.error("Could not read JqplotPanel.js");
                 }
             }
 
         };
     }
     
     public static String convertStreamToString(IResourceStream is) {
         String res = null;
         try {
             @SuppressWarnings("resource")
             Scanner s = new Scanner(is.getInputStream(), "UTF-8").useDelimiter("\\A");
             res = s.hasNext() ? s.next() : null;
             is.close();
         } catch (ResourceStreamNotFoundException e) {
             log.error(e.getMessage(), e);
             res = null;
         } catch (IOException e) {
             log.error(e.getMessage(), e);
             res = null;
         }
         return res;
     }
 }
