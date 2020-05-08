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
 
 package org.icefaces.ace.component.autocompleteentry;
 
 import org.icefaces.ace.meta.annotation.Component;
 import org.icefaces.ace.meta.annotation.Property;
 import org.icefaces.ace.meta.baseMeta.HtmlInputTextMeta;
 import org.icefaces.ace.meta.annotation.Expression;
 import org.icefaces.ace.meta.annotation.ClientBehaviorHolder;
 import org.icefaces.ace.meta.annotation.ClientEvent;
 import org.icefaces.ace.api.IceClientBehaviorHolder;
 
 import javax.faces.application.ResourceDependencies;
 import javax.faces.application.ResourceDependency;
 import javax.el.MethodExpression;
 import javax.el.ValueExpression;
 
import javax.faces.model.SelectItem;

 import java.util.List;
 
 @Component(
         tagName = "autoCompleteEntry",
         componentClass = "org.icefaces.ace.component.autocompleteentry.AutoCompleteEntry",
         generatedClass = "org.icefaces.ace.component.autocompleteentry.AutoCompleteEntryBase",
         extendsClass = "javax.faces.component.html.HtmlInputText",
 		rendererClass   = "org.icefaces.ace.component.autocompleteentry.AutoCompleteEntryRenderer",
         componentFamily = "org.icefaces.ace.AutoCompleteEntry",
         componentType = "org.icefaces.ace.component.AutoCompleteEntry",
 		rendererType    = "org.icefaces.ace.component.AutoCompleteEntryRenderer",
         tlddoc = "AutoCompleteEntry is an text input component that presents available value options as the user types." +
                  "<p>For more information, see the " +
                  "<a href=\"http://wiki.icefaces.org/display/ICE/AutoCompleteEntry\">AutoCompleteEntry Wiki Documentation</a>."
 )
 @ResourceDependencies({
 	@ResourceDependency(library = "icefaces.ace", name = "util/ace-jquery.js"),
 	@ResourceDependency(library = "icefaces.ace", name = "autocompleteentry/autocompleteentry.js")
 })
 public class AutoCompleteEntryMeta extends HtmlInputTextMeta {
 	
     @Property(tlddoc = "", defaultValue="")
     private int styleClass;
 	
     @Property(tlddoc = "")
     private String listVar;
 	
     @Property(tlddoc = "", defaultValue="10")
     private int rows;
 	
     @Property(tlddoc = "", defaultValue="150")
     private int width;
 	
    /*@Property(tlddoc = "")
    private SelectItem selectedItem;*/
 	
     @Property(tlddoc = "")
     private List listValue;
 	
     @Property(tlddoc = "", defaultValue="")
     private String options;
 	
     @Property(expression= Expression.METHOD_EXPRESSION, methodExpressionArgument="org.icefaces.ace.component.autocompleteentry.TextChangeEvent", tlddoc = "")
     private MethodExpression textChangeListener;
 	
 	@Property(tlddoc="Defines the method of filter comparison used, default is \"startsWith\". " +
             "Types available include: \"contains\", \"exact\", \"startsWith\", \"endsWith\" and \"none\". " +
 			"Typically, \"none\" will be used in cases where more complex, custom filtering is needed.", defaultValue="startsWith")
 	private String filterMatchMode;
 	
 	@Property(expression = Expression.VALUE_EXPRESSION,
             tlddoc="ValueExpression that specifies the property of the data object to use for filtering values. " +
 			"This only applies when listvar is used and the rendering is done by means of a facet.")
 	private Object filterBy;
 }
