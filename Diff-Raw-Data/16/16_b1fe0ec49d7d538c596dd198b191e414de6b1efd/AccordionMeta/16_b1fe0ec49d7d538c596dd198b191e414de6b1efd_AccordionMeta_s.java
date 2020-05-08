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
 
 package org.icefaces.mobi.component.accordion;
 
 import org.icefaces.ace.meta.annotation.Component;
 import org.icefaces.ace.meta.annotation.Property;
 import org.icefaces.ace.meta.annotation.Expression;
 import org.icefaces.ace.meta.baseMeta.UIPanelMeta;
 import org.icefaces.ace.meta.annotation.ClientBehaviorHolder;
 import org.icefaces.ace.meta.annotation.ClientEvent;
 import org.icefaces.ace.meta.annotation.Required;
 import javax.el.MethodExpression;
 
 
 @Component(
         tagName = "accordion",
         componentClass = "org.icefaces.mobi.component.accordion.Accordion",
         rendererClass = "org.icefaces.mobi.component.accordion.AccordionRenderer",
         generatedClass = "org.icefaces.mobi.component.accordion.AccordionBase",
         componentType = "org.icefaces.Accordion",
         rendererType = "org.icefaces.AccordionRenderer",
         extendsClass = "javax.faces.component.UIPanel",
         componentFamily = "org.icefaces.Accordion",
         tlddoc = "This mobility component allows panels to be placed within accordion controls in  " +
                 "a stacked manner.  Only a single pane may be active at a time."
 )
 public class AccordionMeta extends UIPanelMeta {
 
     @Property( tlddoc="id of the panel that is active in the accordion.")
     private String currentId;
 
 	@Property(tlddoc="Inline style of the container element.")
 	private String style;
 
 	@Property(tlddoc="Style class of the container element.")
 	private String styleClass;
 
 	@Property(tlddoc="Disables or enables the accordion.", defaultValue="false")
 	private boolean disabled;
 
 	@Property(tlddoc="Effect to use when toggling the panes.", defaultValue="slide")
 	private String effect;
 
 	@Property(tlddoc="When enabled, pane with highest content is used to calculate the height.", defaultValue="true")
 	private boolean autoHeight;
 
     @Property(defaultValue="200px",
             tlddoc="fixeHeight can be used when autoHeight is false. Must be valid height for element.style.height")
     private String fixedHeight;
 
  	@Property(tlddoc="Server side listener to invoke when active pane changes",
                   expression= Expression.METHOD_EXPRESSION,
                   methodExpressionArgument="javax.faces.event.ValueChangeEvent")
 	private MethodExpression paneChangeListener;
 }
