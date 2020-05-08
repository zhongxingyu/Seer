 /**
  * Copyright 2011 Henric Persson (henric.persson@gmail.com)
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *    http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  *
  */
 
 package burrito.client.crud.input;
 
 
 import burrito.client.crud.generic.CrudField;
 import burrito.client.crud.generic.fields.StringField;
 
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.Widget;
 
 public class ReadOnlyTextCrudInputField implements CrudInputField<String> {
 
 	private StringField field;
 	private Label label;
 
 	public ReadOnlyTextCrudInputField(StringField field) {
 		this.field = field;
 		String value = (String) field.getValue();
 		label = new Label(value != null ? value : "(Not set)");
 		label.addStyleName("k5-ReadOnlyTextCrudInputField");
		label.addStyleName("readOnly");
 	}
 
 	@Override
 	public CrudField getCrudField() {
 		return field;
 	}
 
 	@Override
 	public Widget getDisplayWidget() {
 		return label;
 	}
 
 	@Override
 	public String getValue() {
 		return (String) field.getValue();
 	}
 
 	@Override
 	public void load(String value) {
 	}
 }
