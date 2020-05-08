 package de.jwic.demo.basics;
 
 import java.text.DateFormat;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.Locale;
 import java.util.TimeZone;
 
 import de.jwic.base.ControlContainer;
 import de.jwic.base.IControlContainer;
 import de.jwic.controls.Button;
 import de.jwic.controls.DateChangedListener;
 import de.jwic.controls.DatePicker;
 import de.jwic.controls.DateTimePicker;
 import de.jwic.controls.LabelControl;
 import de.jwic.events.SelectionEvent;
 import de.jwic.events.SelectionListener;
 import de.jwic.events.ValueChangedEvent;
 import de.jwic.events.ValueChangedListener;
 import de.jwic.samples.controls.propeditor.PropertyEditorView;
 
 public class DatePickerDemo extends ControlContainer {
 
 	private DatePicker datePicker;
 	private DateTimePicker datePickerMaster, datePickerSlave;
 	private DateTimePicker dateTimePicker;
 	private LabelControl lblInfo;
 	private Button btCurrentDate;
 
 	/**
 	 * Constructor.
 	 * @param container
 	 */
 	public DatePickerDemo(IControlContainer container) {
 		super(container);
 		// create the button instance
 		datePicker = new DatePicker(this, "datePicker");
 		dateTimePicker = new DateTimePicker(this, "dateTimePicker");
 		
 		Button btn = new Button(this, "btn");
 		btn.setTitle("Switch locale");
 
 		btn.addSelectionListener(new SelectionListener() {
 			private boolean DE = false;
 
 			public void objectSelected(SelectionEvent event) {
 				if (DE) {
 					datePicker.setLocale(Locale.ENGLISH);
 					dateTimePicker.setLocale(Locale.ENGLISH);
 					DE = false;
 				} else {
 					datePicker.setLocale(Locale.GERMAN);
 					dateTimePicker.setLocale(Locale.GERMAN);
 					DE = true;
 				}
 			}
 		});
 		
 		btCurrentDate = new Button(this, "btCurrentDate");
 		btCurrentDate.setTitle("Set Current Date/Time (Default TZ)");
 		btCurrentDate.addSelectionListener(new SelectionListener() {
 			
 			@Override
 			public void objectSelected(SelectionEvent event) {
 				DateFormat dateFormatter = DateFormat.getDateTimeInstance(DateFormat.LONG,
 						DateFormat.LONG);
 				dateFormatter.setTimeZone(TimeZone.getDefault());
 				DateFormat dateFormatter2 = DateFormat.getDateTimeInstance(DateFormat.LONG,
 						DateFormat.LONG);
 				dateFormatter2.setTimeZone(TimeZone.getTimeZone("UTC"));
 				Date d = Calendar.getInstance().getTime();
 				datePicker.setDate(d);
 				dateTimePicker.setDate(d);
 				datePickerMaster.setDate(d);
 				lblInfo.setText("Date is set to: "
 						+ dateFormatter.format(d) + "(" + dateFormatter2.format(d) +")");
 			}
 		});
 		
 		DateChangedListener listener = new DateChangedListener() {
 
 			public void onDateChanged(Date oldDate, Date newDate) {
 				DateFormat dateFormatter = DateFormat.getDateTimeInstance(DateFormat.LONG,
 						DateFormat.LONG);
 				dateFormatter.setTimeZone(TimeZone.getDefault());
 				if (newDate != null) {
 					lblInfo.setText("Selected Date is: "
 							+ dateFormatter.format(newDate));
 				} else {
 					lblInfo.setText("Selected Date is: null");
 				}
 			}
 		};
 		
 		datePicker.addDateChangedListener(listener);
		
		
 		dateTimePicker.addDateChangedListener(listener);
		
		
 		datePicker.setUpdateOnChange(true);
 				
 		lblInfo = new LabelControl(this, "lblInfo");
 		
 		final PropertyEditorView propEditor = new PropertyEditorView(this, "propEditor");
 		propEditor.setBean(dateTimePicker);
 		
 		datePicker.addValueChangedListener(new ValueChangedListener() {
 			/* (non-Javadoc)
 			 * @see de.jwic.events.ValueChangedListener#valueChanged(de.jwic.events.ValueChangedEvent)
 			 */
 			public void valueChanged(ValueChangedEvent event) {
 				propEditor.loadValues();	 // reload values.			
 			}
 		});
 				
		DateChangedListener listener2 = new DateChangedListener() {
			
			@Override
			public void onDateChanged(Date oldDate, Date newDate) {
				System.out.println(oldDate+" to "+newDate);
			}
		};
		
 		propEditor.loadValues(); // refresh values.
 		Date d = Calendar.getInstance().getTime();
 		datePickerMaster = new DateTimePicker(this, "dateTimeMaster");
		datePickerMaster.setUpdateOnChange(true);
		datePickerMaster.addDateChangedListener(listener2);
 		datePickerMaster.setDate(d);
 		datePickerSlave = new DateTimePicker(this, "dateTimeSlave");
		datePickerSlave.setUpdateOnChange(true);
		datePickerSlave.addDateChangedListener(listener2);
 		datePickerSlave.setMaster(datePickerMaster);
 		
 	}
 	
 
 }
