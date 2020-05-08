 /*
  * Copyright (C) 2011 Google Inc.
  * 
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
  * use this file except in compliance with the License. You may obtain a copy of
  * the License at
  * 
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations under
  * the License.
  */
 
 package com.google.api.explorer.client.parameter.schema;
 
 import com.google.api.explorer.client.AppState;
 import com.google.api.explorer.client.Resources;
 import com.google.api.explorer.client.base.Schema;
 import com.google.api.explorer.client.base.Schema.Property;
 import com.google.common.base.Function;
 import com.google.common.base.Joiner;
 import com.google.common.base.Predicate;
 import com.google.common.collect.Iterables;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Maps;
 import com.google.gwt.dom.client.Document;
 import com.google.gwt.dom.client.Style.Unit;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.event.logical.shared.ValueChangeEvent;
 import com.google.gwt.event.logical.shared.ValueChangeHandler;
 import com.google.gwt.event.shared.HandlerRegistration;
 import com.google.gwt.user.client.ui.CheckBox;
 import com.google.gwt.user.client.ui.HTMLPanel;
 import com.google.gwt.user.client.ui.HasValue;
 import com.google.gwt.user.client.ui.InlineLabel;
 
 import java.util.Collections;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 /**
  * Tree-based UI for selecting fields to request, based on the response schema
  * for a method.
  * 
  * @author jasonhall@google.com (Jason Hall)
  */
 public class FieldsEditor extends HTMLPanel implements HasValue<Boolean> {
 
   private static final Joiner JOINER = Joiner.on(',').skipNulls();
 
   private final AppState appState;
   private final String key;
   private final CheckBox root;
   private final Map<String, HasValue<Boolean>> children = Maps.newHashMap();
 
   public FieldsEditor(AppState appState, String key) {
     super("");
 
     this.appState = appState;
     this.key = key;
     root = new CheckBox(key.isEmpty() ? "Select all/none" : key);
    root.setValue(false);
     root.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
       @Override
       public void onValueChange(ValueChangeEvent<Boolean> event) {
         for (HasValue<Boolean> checkBox : children.values()) {
           checkBox.setValue(event.getValue(), true);
         }
       }
     });
     add(root);
   }
 
  /**
   * Sets the properties this field will have, if it is an object.
   */
   public void setProperties(Map<String, Property> properties) {
     List<String> keys = Lists.newArrayList(properties.keySet());
     Collections.sort(keys);
 
     HTMLPanel inner = new HTMLPanel("");
     inner.getElement().getStyle().setPaddingLeft(20, Unit.PX);
 
     for (String childKey : keys) {
       final Property property = properties.get(childKey);
       final Map<String, Property> childProperties = property.getProperties();
       final Property items = property.getItems();
 
       if (childProperties == null && items == null) {
         // This is a simple field
         CheckBox checkBox = new CheckBox(childKey);
        checkBox.setValue(root.getValue());
         checkBox.setTitle(property.getDescription());
         children.put(childKey, checkBox);
         checkBox.getElement().appendChild(Document.get().createBRElement());
         inner.add(checkBox);
       } else {
 
         final FieldsEditor editor = new FieldsEditor(appState, childKey);
         children.put(childKey, editor);
         inner.add(editor);
 
         if (childProperties != null) {
           editor.setProperties(childProperties);
         } else if (property.getRef() != null) {
           editor.setRef(property.getRef());
         } else if (items != null) {
           if (items.getProperties() != null) {
             editor.setProperties(items.getProperties());
           } else if (items.getRef() != null) {
             editor.setRef(items.getRef());
           }
         }
       }
     }
     add(inner);
   }
 
  /**
   * Denotes that this is an object whose definition is in another Schema, and
   * that it should be filled in with the correct fields when expanded.
   */
   void setRef(final String ref) {
     final InlineLabel expando = new InlineLabel("+");
     add(expando);
     expando.addStyleName(Resources.INSTANCE.style().clickable());
     expando.setTitle("Click to show more fields");
     expando.addClickHandler(new ClickHandler() {
       @Override
       public void onClick(ClickEvent event) {
         Schema sch = appState.getCurrentService().getSchemas().get(ref);
         setProperties(sch.getProperties());
         remove(expando);
       }
     });
   }
 
  /** Returns the string for this field, including sub-fields if it has any. */
   public String genFieldsString() {
     // If there are no children, that means it's an expandable item that is not
     // expanded. Return just the key in this case.
     if (children.isEmpty() && root.getValue()) {
       return key;
     }
     // If all fields are checked, rather than return
     // "object(all,fields,in,the,object)" just return "object"
     if (getValue()) {
       return key;
     }
 
     // Recursively construct the fields string of all selected children.
     String directFields =
         JOINER.join(Iterables.transform(children.entrySet(),
             new Function<Map.Entry<String, HasValue<Boolean>>, String>() {
               @Override
               public String apply(Entry<String, HasValue<Boolean>> input) {
                 if (input.getValue() instanceof FieldsEditor) {
                   return ((FieldsEditor) input.getValue()).genFieldsString();
                 } else {
                   return input.getValue().getValue() ? input.getKey() : null;
                 }
               }
             }));
 
     if (directFields.isEmpty()) {
       // No children were selected, so this object is not needed.
       return null;
     } else if (!directFields.contains(",")) {
       // If only one field is checked, rather than return
       // "object(onlyOneField)", return "object/onlyOneField"
       if (key.isEmpty()) {
         return directFields;
       } else {
         return key + '/' + directFields;
       }
     }
 
     StringBuilder sb = new StringBuilder();
     if (!key.isEmpty()) {
       sb.append(key).append('(');
     }
 
     sb.append(directFields);
 
     if (!key.isEmpty()) {
       sb.append(')');
     }
     return sb.toString();
   }
 
  /**
   * Returns this field's checked value, or if it has children, whether all its
   * children are checked.
   */
   @Override
   public Boolean getValue() {
     if (children.isEmpty()) {
       return root.getValue();
     }
 
     return Iterables.all(children.entrySet(),
         new Predicate<Map.Entry<String, HasValue<Boolean>>>() {
           @Override
           public boolean apply(Entry<String, HasValue<Boolean>> input) {
             return input.getValue().getValue();
           }
         });
   }
 
  /**
   * Sets this field's checked value, and all of its childrens' if it has any.
   */
   @Override
   public void setValue(Boolean value) {
     for (HasValue<Boolean> hasValue : children.values()) {
       hasValue.setValue(value);
     }
     this.root.setValue(value);
   }
 
  /** Adds a ValueChangeHandler to this field. */
   @Override
   public HandlerRegistration addValueChangeHandler(ValueChangeHandler<Boolean> handler) {
     return root.addValueChangeHandler(handler);
   }
 
  /** Sets this checkboxes value. */
   @Override
   public void setValue(Boolean value, boolean fireEvents) {
     root.setValue(value, fireEvents);
   }
 }
