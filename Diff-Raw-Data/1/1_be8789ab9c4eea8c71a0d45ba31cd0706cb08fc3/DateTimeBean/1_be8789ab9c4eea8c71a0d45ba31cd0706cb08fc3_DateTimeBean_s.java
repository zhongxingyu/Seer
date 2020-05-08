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
 
 package org.icefaces.samples.showcase.example.ace.date;
 
 import java.io.Serializable;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.Locale;
 import java.util.TimeZone;
 
 import javax.annotation.PostConstruct;
 import javax.faces.bean.CustomScoped;
 import javax.faces.bean.ManagedBean;
 import javax.faces.event.ValueChangeEvent;
 
 import org.icefaces.samples.showcase.metadata.annotation.ComponentExample;
 import org.icefaces.samples.showcase.metadata.annotation.ExampleResource;
 import org.icefaces.samples.showcase.metadata.annotation.ExampleResources;
 import org.icefaces.samples.showcase.metadata.annotation.ResourceType;
 import org.icefaces.samples.showcase.metadata.context.ComponentExampleImpl;
 
 @ComponentExample(parent = DateEntryBean.BEAN_NAME, title = "example.ace.dateentry.timeentry.title", description = "example.ace.dateentry.timeentry.description", example = "/resources/examples/ace/date/datetimeentry.xhtml")
 @ExampleResources(resources = {
 // xhtml
 		@ExampleResource(type = ResourceType.xhtml, title = "datetimeentry.xhtml", resource = "/resources/examples/ace/date/datetimeentry.xhtml"),
 		// Java Source
 		@ExampleResource(type = ResourceType.java, title = "DateTimeBean.java", resource = "/WEB-INF/classes/org/icefaces/samples/showcase/example/ace/date/DateTimeBean.java") })
 @ManagedBean(name = DateTimeBean.BEAN_NAME)
 @CustomScoped(value = "#{window}")
 public class DateTimeBean extends ComponentExampleImpl<DateTimeBean> implements
 		Serializable {
 	public static final String BEAN_NAME = "dateTime";
 
 	private static final String PATTERN_DATE = "MM/dd/yyyy";
 	private static final String PATTERN_TIME = "h:mm:ss a";
 	private static final String PATTERN_BOTH = PATTERN_DATE + " "
 			+ PATTERN_TIME;
 
 	private Date selectedDate;
 	private String timeType = "both";
 	private String pattern = PATTERN_BOTH;
 	private boolean timeOnly = false;
 
 	public DateTimeBean() {
 		super(DateTimeBean.class);
 
 		Calendar calendar = Calendar.getInstance(
 				TimeZone.getTimeZone("Canada/Mountain"), Locale.getDefault());
 		selectedDate = calendar.getTime();
 	}
 
 	public Date getSelectedDate() {
 		return selectedDate;
 	}
 
 	public String getTimeType() {
 		return timeType;
 	}
 
 	public String getPattern() {
 		return pattern;
 	}
 
 	public boolean getTimeOnly() {
 		return timeOnly;
 	}
 
 	public void setSelectedDate(Date selectedDate) {
 		if (pattern.equals(PATTERN_TIME)) {
 			// Only update the time portion of the date
 			Calendar currentDate = Calendar.getInstance();
 			currentDate.setTime(this.selectedDate);
 
 			Calendar newDate = Calendar.getInstance();
 			newDate.setTime(selectedDate);
 			currentDate.set(Calendar.HOUR_OF_DAY,
 					newDate.get(Calendar.HOUR_OF_DAY));
 			currentDate.set(Calendar.MINUTE, newDate.get(Calendar.MINUTE));
 			currentDate.set(Calendar.SECOND, newDate.get(Calendar.SECOND));
 
 			this.selectedDate = currentDate.getTime();
 
 		} else if (pattern.equals(PATTERN_DATE)) {
 			// Only update the date portion of the date
 			Calendar currentDate = Calendar.getInstance();
 			currentDate.setTime(this.selectedDate);
 
 			Calendar newDate = Calendar.getInstance();
 			newDate.setTime(selectedDate);
 			currentDate.set(Calendar.DAY_OF_MONTH,
 					newDate.get(Calendar.DAY_OF_MONTH));
 			currentDate.set(Calendar.MONTH, newDate.get(Calendar.MONTH));
 			currentDate.set(Calendar.YEAR, newDate.get(Calendar.YEAR));
 
 		} else {
 			// Overwrite the whole object
 			this.selectedDate = selectedDate;
 		}
 	}
 
 	public void setTimeType(String timeType) {
 		this.timeType = timeType;
 	}
 
 	public void setPattern(String pattern) {
 		this.pattern = pattern;
 	}
 
 	public void setTimeOnly(boolean timeOnly) {
 		this.timeOnly = timeOnly;
 	}
 
 	@PostConstruct
 	public void initMetaData() {
 		super.initMetaData();
 	}
 
 	public void typeChanged(ValueChangeEvent event) {
 		String val = event.getNewValue().toString();
 
 		if ("time".equals(val)) {
 			pattern = PATTERN_TIME;
 			timeOnly = true;
 		} else if ("date".equals(val)) {
 			pattern = PATTERN_DATE;
 			timeOnly = false;
 		} else {
 			pattern = PATTERN_BOTH;
 			timeOnly = false;
 		}
 	}
 }
