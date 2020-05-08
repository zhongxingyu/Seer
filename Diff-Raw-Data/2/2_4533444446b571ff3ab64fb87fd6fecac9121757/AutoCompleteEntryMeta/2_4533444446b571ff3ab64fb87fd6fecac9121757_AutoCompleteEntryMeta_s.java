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
 import javax.el.ValueExpression;
 
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
         tlddoc = "AutoCompleteEntry is a text input component that presents possible valid options as the user types. " +
 				"The options can be a list of SelectItem's specified in a child <f:selectItems /> tag. It is also possible " +
 				"to specify a list of arbitrary data objects (i.e. POJOs) through the listValue attribute. In this case, a facet " +
 				"named \"row\" should be nested inside this component. This allows for more flexible rendering of each row, making it possible " +
 				"to render other components or HTML for each row and to display different properties of the data object. " +
                  "<p>For more information, see the " +
                  "<a href=\"http://wiki.icefaces.org/display/ICE/AutoCompleteEntry\">AutoCompleteEntry Wiki Documentation</a>."
 )
 @ResourceDependencies({
 	@ResourceDependency(library = "icefaces.ace", name = "util/ace-jquery.js"),
 	@ResourceDependency(library = "icefaces.ace", name = "autocompleteentry/autocompleteentry.js")
 })
 @ClientBehaviorHolder(events = {
 	@ClientEvent( name="submit",
 		javadoc="Fired any time the value of the text input field is submitted to the server, either by typing a symbol, clicking on an option or pressing enter.",
 		tlddoc="Fired any time the value of the text input field is submitted to the server, either by typing a symbol, clicking on an option or pressing enter",
 		defaultRender="@all", defaultExecute="@all" ) },
 	defaultEvent="submit" )
 public class AutoCompleteEntryMeta extends HtmlInputTextMeta {
 	
     @Property(tlddoc = "Style class name of the container element.", defaultValue="")
     private String styleClass;
 	
     @Property(tlddoc = "Variable name to use for referencing each data object in the list when rendering via a facet.")
     private String listVar;
 	
     @Property(tlddoc = "The maximum number of possible options to show to the user.", defaultValue="10")
     private int rows;
 	
     @Property(tlddoc = "The width of the text input field, in pixels.", defaultValue="150")
     private int width;
 	
     @Property(tlddoc = "The SelectItem or arbitrary data object that is currently selected, if any.")
     private Object selectedItem;
 	
     @Property(tlddoc = "When rendering via a facet, this attribute specifies the list of data objects that contains all possible options.")
     private List listValue;
 	
     @Property(tlddoc = "", defaultValue="")
     private String options;
 	
 	@Property(tlddoc="Defines the method of filter comparison used, default is \"startsWith\". " +
             "Types available include: \"contains\", \"exact\", \"startsWith\", \"endsWith\" and \"none\". " +
 			"Typically, \"none\" will be used in cases where more complex, custom filtering is needed or when " +
 			"option values need to be loaded lazily (e.g. from a data base).", defaultValue="startsWith")
 	private String filterMatchMode;
 	
 	@Property(expression = Expression.VALUE_EXPRESSION,
             tlddoc="ValueExpression that specifies the property of the data object to use for filtering values. " +
 			"This only applies when listvar is used and the rendering is done by means of a facet.")
 	private Object filterBy;
 
     @Property(tlddoc = "Indicator indicating that the user is required to provide a submitted value for this input component.")
     private String requiredIndicator;
 
     @Property(tlddoc = "Indicator indicating that the user is NOT required to provide a submitted value for this input component.")
     private String optionalIndicator;
 
     @Property(tlddoc = "Position of label relative to input field. Supported values are \"left/right/top/bottom/inField\".")
     private String labelPosition;
 
     @Property(tlddoc = "Position of input-required or input-optional indicator relative to input field or label. " +
             "Supported values are \"left/right/top/bottom/labelLeft/labelRight\". ")
     private String indicatorPosition;
 	
     @Property(tlddoc = "Delay in milliseconds for showing the list of possible matches after typing a character.", defaultValue="0")
     private int delay;
 
    @Property(tlddoc = "Minimum number of characters that must be in the text field in order to produce the list of possible matches.", defaultValue="1")
     private int minChars;
 
     @Property(tlddoc = "Boolean value that indicates whether the filtering should be case sensitive or not.", defaultValue="false")
     private boolean caseSensitive;
 
     @Property(tlddoc = "Maximum height in pixels of the list of possible matches (if 0, then the size is automatically adjusted to show all possible matches).")
     private int height;
 
     @Property(tlddoc = "Direction in which to show the list of possible matches. Possible values are \"up\", \"down\", and \"auto\".")
     private String direction;	
 	
 }
