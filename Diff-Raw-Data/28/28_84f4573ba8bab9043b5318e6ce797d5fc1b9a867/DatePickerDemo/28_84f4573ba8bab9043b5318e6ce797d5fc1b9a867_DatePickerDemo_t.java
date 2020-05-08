 /*
  * Copyright 2005-2007 jWic group (http://www.jwic.de)
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  *
  * de.jwic.ecolib.samples.controls.DatePickerDemo
  * Created on 13.11.2012
  * $Id:$
  */
 package de.jwic.ecolib.samples.controls;
 
 import java.text.DateFormat;
 import java.util.Date;
 import java.util.Locale;
 
 import de.jwic.base.ControlContainer;
 import de.jwic.base.IControlContainer;
 import de.jwic.controls.Button;
 import de.jwic.controls.DateChangedListener;
 import de.jwic.controls.DatePicker;
 import de.jwic.controls.DateTimePicker;
 import de.jwic.controls.InputBox;
 import de.jwic.controls.LabelControl;
 import de.jwic.events.SelectionEvent;
 import de.jwic.events.SelectionListener;
 import de.jwic.samples.controls.propeditor.PropertyEditorView;
 
 /**
  * 
  * @author bogdan
  */
 public class DatePickerDemo extends ControlContainer {
 
 	/**
 	 * @param container
 	 * @param name
 	 */
 	public DatePickerDemo(final IControlContainer container, String name) {
 		super(container, name);
 
 		final PropertyEditorView propEditor = new PropertyEditorView(this, "propEditor");
 		
 		final DatePicker datePickerControl = new DatePicker(this,
 				"datePicker");
		datePickerControl.setUpdateOnChange(true);
 		
 		// datePickerControl.setLocale(Locale.KOREAN);
 
 		Button btn = new Button(this, "btn");
 		btn.setTitle("Switch locale");
 
 		btn.addSelectionListener(new SelectionListener() {
 			private boolean DE = true;
 
 			public void objectSelected(SelectionEvent event) {
 				if (DE) {
 					datePickerControl.setLocale(Locale.ENGLISH);
 					datePickerControl.getLocale();
 					DE = false;
 				} else {
 					datePickerControl.setLocale(Locale.GERMAN);
 					DE = true;
 				}
 				System.out.println(datePickerControl.getLocale().toString());
 
 			}
 		});
 
 		Button btn2 = new Button(this, "btn2");
 		btn2.setTitle("Toggle Show Month");
 
 		btn2.addSelectionListener(new SelectionListener() {
 
 			public void objectSelected(SelectionEvent event) {
 				if (datePickerControl.isChangeMonth()) {
 					datePickerControl.setChangeMonth(false);
 
 				} else {
 					datePickerControl.setChangeMonth(true);
 
 				}
 				System.out.println(datePickerControl.getLocale().toString());
 
 			}
 		});
 		Button btn3 = new Button(this, "btn3");
 		btn3.setTitle("Toggle Show Year");
 
 		btn3.addSelectionListener(new SelectionListener() {
 
 			public void objectSelected(SelectionEvent event) {
 				if (datePickerControl.isChangeYear()) {
 					datePickerControl.setChangeYear(false);
 
 				} else {
 					datePickerControl.setChangeYear(true);
 
 				}
 				System.out.println(datePickerControl.getLocale().toString());
 
 			}
 		});
 
 		Button btn4 = new Button(this, "btn4");
 		btn4.setTitle("setDate(null)");
 
 		btn4.addSelectionListener(new SelectionListener() {
 
 			public void objectSelected(SelectionEvent event) {
 				datePickerControl.setDate(null);
 			}
 		});
 		
 		final Button btn5 = new Button(this, "btn5");
 		btn5.setTitle("setDisabled");
 
 		btn5.addSelectionListener(new SelectionListener() {
 			boolean enabled = true;
 			public void objectSelected(SelectionEvent event) {
 				enabled = !enabled;
 				datePickerControl.setEnabled(enabled);
 				btn5.setTitle(enabled ? "setDisabled" : "setEnabled");
 				
 			}
 		});
 
 
 		final LabelControl lbl = new LabelControl(this, "label");
 
 		datePickerControl.addDateChangedListener(new DateChangedListener() {
 
 			public void onDateChanged(Date oldDate, Date newDate) {
 				DateFormat dateFormatter = DateFormat.getDateInstance(
 						DateFormat.LONG, datePickerControl.getLocale());
 				if (newDate != null) {
 					lbl.setText("Selected Date is: "
 							+ dateFormatter.format(newDate));
 				} else {
 					lbl.setText("Selected Date is: null");
 				}
 			}
 		});
 
 		final InputBox dateFormat = new InputBox(this,
 				"dateFormat");
 		Button btnDateFormat = new Button(this, "btnDateFormat");
 		btnDateFormat.setTitle("Set Date Format");
 		btnDateFormat.addSelectionListener(new SelectionListener() {
 
 			@Override
 			public void objectSelected(SelectionEvent event) {
 				String format = dateFormat.getText();
 
 				datePickerControl.setDateFormat(dateFormat.getText());
 
 			}
 		});
 		
 		
 		final DateTimePicker dateTimePicker = new DateTimePicker(this,"dateTimePicker");
 		final DateTimePicker slaveDateTimePicker = new DateTimePicker(this,"slaveDateTimePicker");
 		slaveDateTimePicker.setMaster(dateTimePicker);
 		
 		
 		Button b = new Button(this, "slaveDateTrigger");
 		b.setTitle("Display Master/Slave Dates");
 		b.addSelectionListener(new SelectionListener() {
 			
 			@Override
 			public void objectSelected(SelectionEvent event) {
 				lbl.setText("Salve date: " + slaveDateTimePicker.getDate() + " Master Date: " + dateTimePicker.getDate() +" DatePicker Date: "+datePickerControl.getDate() );
 				
 			}
 		});
 		
 		propEditor.setBean(datePickerControl);
 
 	}
 
 }
