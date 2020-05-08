 /*******************************************************************************
  * Mission Control Technologies, Copyright (c) 2009-2012, United States Government
  * as represented by the Administrator of the National Aeronautics and Space 
  * Administration. All rights reserved.
  *
  * The MCT platform is licensed under the Apache License, Version 2.0 (the 
  * "License"); you may not use this file except in compliance with the License. 
  * You may obtain a copy of the License at 
  * http://www.apache.org/licenses/LICENSE-2.0.
  *
  * Unless required by applicable law or agreed to in writing, software 
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT 
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the 
  * License for the specific language governing permissions and limitations under 
  * the License.
  *
  * MCT includes source code licensed under additional open source licenses. See 
  * the MCT Open Source Licenses file included with this distribution or the About 
  * MCT Licenses dialog available at runtime from the MCT Help menu for additional 
  * information. 
  *******************************************************************************/
 package gov.nasa.arc.mct.fastplot.view;
 
 import gov.nasa.arc.mct.fastplot.bridge.PlotConstants;
 
 import java.text.DecimalFormat;
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 import java.util.TimeZone;
 
 import javax.swing.InputVerifier;
 import javax.swing.JComponent;
 import javax.swing.JFormattedTextField;
 
 
 public class TimeTextField extends JFormattedTextField {
 	private static final long serialVersionUID = 340661035024526680L;
 
 	static final private int DAYS_POSITION = 0;
 	static final private int HOURS_POSITION = 4;
 	static final private int MINUTES_POSITION = 7;
 	static final private int SECONDS_POSITION = 10;
 	private static final int NUM_COLUMNS = 9;
 	private GregorianCalendar modelCalendar = new GregorianCalendar();
 
 	private int yearValue;
 	private static final DecimalFormat dayFormat = new DecimalFormat("000");
 	private static final DecimalFormat hhmmssFormat = new DecimalFormat("00");
 
 	public TimeTextField(AbstractFormatter formatter) {
 		super(formatter);
 		setValue("001/12:00:00");
 		modelCalendar.setTimeZone(TimeZone.getTimeZone(PlotConstants.DEFAULT_TIME_ZONE));
 		yearValue = modelCalendar.get(Calendar.YEAR);
 		setInputVerifier(new TimeVerifier());
 		setColumns(NUM_COLUMNS);
 		setHorizontalAlignment(JFormattedTextField.RIGHT);
 	}
 
 	public void setTime(GregorianCalendar calendar) {
 		StringBuilder builder = new StringBuilder();
 		builder.append(dayFormat.format(calendar.get(Calendar.DAY_OF_YEAR)) + "/");
 		builder.append(hhmmssFormat.format(calendar.get(Calendar.HOUR_OF_DAY)) + ":");
 		builder.append(hhmmssFormat.format(calendar.get(Calendar.MINUTE)) + ":");
 		builder.append(hhmmssFormat.format(calendar.get(Calendar.SECOND)));
 		setValue(builder.toString());
 		yearValue = calendar.get(Calendar.YEAR);
 	}
 	
 
 	public long getValueInMillis() {
 		String text = (String) getValue();
 		if (text != null && text.length() == 12) {
 			String days = text.substring(DAYS_POSITION, 3);
 			String hours = text.substring(HOURS_POSITION, 6);
 			String minutes = text.substring(MINUTES_POSITION, 9);
 			String seconds = text.substring(SECONDS_POSITION, 12);
 			int daysValue = Integer.parseInt(days);
 			int hoursValue = Integer.parseInt(hours);
 			int minutesValue = Integer.parseInt(minutes);
 			int secondsValue = Integer.parseInt(seconds);
 
 			GregorianCalendar calculate = new GregorianCalendar();
 			calculate.setTimeZone(TimeZone.getTimeZone(PlotConstants.DEFAULT_TIME_ZONE));
 			calculate.set(Calendar.YEAR, yearValue);
 			calculate.set(Calendar.DAY_OF_YEAR, daysValue);
 			calculate.set(Calendar.HOUR_OF_DAY, hoursValue);
 			calculate.set(Calendar.MINUTE, minutesValue);
 			calculate.set(Calendar.SECOND, secondsValue);
 			return calculate.getTimeInMillis();
 		}
 		return modelCalendar.getTimeInMillis();
 	}
 
 	class TimeVerifier extends InputVerifier {
 		
 		private void setTimeValue(int dayOfYear, int hourOfDay, int minute, int second) {
 			StringBuilder builder = new StringBuilder();
 			builder.append(dayFormat.format(dayOfYear) + "/");
 			builder.append(hhmmssFormat.format(hourOfDay) + ":");
 			builder.append(hhmmssFormat.format(minute) + ":");
 			builder.append(hhmmssFormat.format(second));
 			setValue(builder.toString());
 		}
 		
 		@Override
 		public boolean verify(JComponent component) {
 			TimeTextField field = (TimeTextField) component;
 			String text = field.getText();
 			if (text == null || text.length() == 0) {
 				return true;
 			}
 			String days = text.substring(DAYS_POSITION, 3);
 			String hours = text.substring(HOURS_POSITION, 6);
 			String minutes = text.substring(MINUTES_POSITION, 9);
 			String seconds = text.substring(SECONDS_POSITION, 12);
 
 			int daysValue = Integer.parseInt(days);
 			int hoursValue = Integer.parseInt(hours);
 			int minutesValue = Integer.parseInt(minutes);
 			int secondsValue = Integer.parseInt(seconds);
 			if (daysValue == 0) { //DOY starts a 1
 				field.setSelectionStart(DAYS_POSITION);
 				field.setSelectionEnd(3);
 				return false;
 			}
 			// Check seconds field. Carry over values > 59
 			if (secondsValue >= 60) {
				minutesValue += Double.valueOf(Math.floor(secondsValue/60)).intValue();
 				secondsValue = secondsValue % 60;
 			}
 			// Check minutes field. Carry over values > 59
 			if (minutesValue >= 60) {
				hoursValue += Double.valueOf(Math.floor(minutesValue/60)).intValue();
 				minutesValue = minutesValue % 60;
 			}
 			// Check hour of day field. Carry over values > 23
 			if (hoursValue >= 24) {
				daysValue += Double.valueOf(Math.floor(hoursValue/24)).intValue(); 
 				hoursValue = hoursValue % 24;
 			}
 			if ((modelCalendar.isLeapYear(modelCalendar.get(Calendar.YEAR)) &&
 					daysValue > 366)  || 
 					(!modelCalendar.isLeapYear(modelCalendar.get(Calendar.YEAR)) &&
 					daysValue > 365)) {
 				yearValue = (modelCalendar.get(Calendar.YEAR)+Double.valueOf(Math.floor(daysValue/365)).intValue());
 				daysValue = (daysValue % 365) + 1;
 			}
 			setTimeValue(daysValue, hoursValue, minutesValue, 
 					secondsValue);
 			return field.isEditValid();
 		}
 	}
 }
