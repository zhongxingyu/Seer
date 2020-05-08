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
 
 import com.google.api.explorer.client.base.Schema.Property;
 import com.google.api.explorer.client.parameter.schema.SchemaForm.SchemaEditor;
 import com.google.common.collect.Lists;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.json.client.JSONArray;
 import com.google.gwt.json.client.JSONValue;
 import com.google.gwt.uibinder.client.UiBinder;
 import com.google.gwt.uibinder.client.UiField;
 import com.google.gwt.uibinder.client.UiHandler;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.HTMLPanel;
 import com.google.gwt.user.client.ui.InlineLabel;
 import com.google.gwt.user.client.ui.Widget;
 
 import java.util.List;
 
 /**
  * {@link SchemaEditor} for array values. The elements of the array will each
  * have their own editors which provide the string value of this editor.
  * 
  * @author jasonhall@google.com (Jason Hall)
  */
 class ArraySchemaEditor extends Composite implements SchemaEditor {
 
   private static ArraySchemaEditorUiBinder uiBinder = GWT.create(ArraySchemaEditorUiBinder.class);
 
   interface ArraySchemaEditorUiBinder extends UiBinder<Widget, ArraySchemaEditor> {
   }
 
   private final SchemaForm schemaForm;
   private final Property items;
   private final List<SchemaEditor> editors = Lists.newArrayList();
 
   @UiField HTMLPanel panel;
   @UiField InlineLabel newItem;
 
   ArraySchemaEditor(SchemaForm schemaForm, Property items) {
     initWidget(uiBinder.createAndBindUi(this));
     this.schemaForm = schemaForm;
     this.items = items;
   }
 
   @UiHandler("newItem")
   void newItem(ClickEvent event) {
     addItem();
   }
 
   @Override
   public Widget render(Property ignored) {
     return this;
   }
 
   private void addItem() {
     // Get the correct editor to show for the type of array element.
     final SchemaEditor editor = schemaForm.getSchemaEditorForProperty(items);
     editors.add(editor);
 
     // Render the widget and make an ArrayElement widget out of it
     final Widget rendered = editor.render(items);
     final ArrayElement el = new ArrayElement(rendered);
     el.remove.addClickHandler(new ClickHandler() {
       @Override
       public void onClick(ClickEvent event) {
         // When the element is removed in the UI, remove it from the list of
         // editors we care about.
         panel.remove(el);
         editors.remove(editor);
       }
     });
     panel.add(el);
   }
 
   @Override
   public JSONValue getJSONValue() {
     JSONArray arr = new JSONArray();
     for (int i = 0; i < editors.size(); i++) {
      arr.set(0, editors.get(i).getJSONValue());
     }
     return arr;
   }
 }
