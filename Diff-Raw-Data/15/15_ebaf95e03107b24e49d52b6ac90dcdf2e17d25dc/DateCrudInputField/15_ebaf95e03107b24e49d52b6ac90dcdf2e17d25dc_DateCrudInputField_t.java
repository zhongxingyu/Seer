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
 
 import java.util.Date;
 
 
 import burrito.client.crud.generic.CrudField;
 import burrito.client.crud.generic.fields.DateField;
 import burrito.client.widgets.date.DateTimePickerWidget;
 
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.Widget;
 
 public class DateCrudInputField implements CrudInputField<Date> {
 
 	private DateField field;
 	private DateTimePickerWidget datePicker;
 	private Label label;
 
 	public DateCrudInputField(DateField field) {
 		this.field = field;
 		Date value = (Date) field.getValue();
 		if (field.isReadOnly()) {
			label = new Label(value != null ? value.toString() : "(Not set)");
			label.addStyleName("readOnly");
 		}
 		else {
 			datePicker = new DateTimePickerWidget(field.isRequired());
 			load(value);
 		}
 	}
 
 	public Widget getDisplayWidget() {
 		if (datePicker != null) return datePicker;
 		return label;
 	}
 
 	public Date getValue() {
 		if (datePicker != null) return datePicker.getDate();
 		return (Date) field.getValue();
 	}
 
 	public void load(Date value) {
 		if (datePicker != null) datePicker.setDate(value);
 	}
 
 	public CrudField getCrudField() {
 		field.setValue(getValue());
 		return field;
 	}
 }
