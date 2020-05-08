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
  * de.jwic.ecolib.controls.datapicker.DatePicker
  * Created on 12.11.2012
  * $Id:$
  */
 package de.jwic.controls;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.Locale;
 import java.util.TimeZone;
 
 import org.apache.log4j.Logger;
 
 import de.jwic.base.IControlContainer;
 import de.jwic.base.IncludeJsOption;
 import de.jwic.events.ValueChangedEvent;
 import de.jwic.events.ValueChangedListener;
 
 /**
  * 
  * @author bogdan
  */
 public class DatePicker extends InputBox {
 	private static final long serialVersionUID = 1L;
 
 	public static final String NO_FORMAT = "noformat";
 	private Locale locale;
 	private Long currentTime;
 	private final List<DateChangedListener> listeners = new ArrayList<DateChangedListener>();
 	private boolean changeMonth = true;
 	private boolean changeYear = true;
 	private String dateFormat = NO_FORMAT;
 	private int numberOfMonths = 1;
 	private boolean showWeek = false;
 	private Date date;
 	private Date minDate, maxDate;
 
	private boolean updateOnChange;
	
 	private boolean iconTriggered = false;
 	private TimeZone timeZone;
 
 	private static final Logger log = Logger.getLogger(DatePicker.class);
 
 	/**
 	 * @param container
 	 */
 	public DatePicker(IControlContainer container) {
 		super(container);
 		init();
 		
 	}
 
 	/**
 	 * @param container
 	 * @param name
 	 */
 	public DatePicker(IControlContainer container, String name) {
 		super(container, name);
 		init();
 	}
 
 	/**
 	 * 
 	 */
 	protected void init() {
 		locale = this.getSessionContext().getLocale();
 		this.setDateFormat(this.getSessionContext().getDateFormat());
 		this.setTimeZone(getSessionContext().getTimeZone());
 		field.addValueChangedListener(new ValueChangedListener() {
 			
 			@Override
 			public void valueChanged(ValueChangedEvent event) {
 				String newVal = event.getNewValue();
 				String oldVal = event.getOldValue();
 				
 				Long newLong = null;
 				Long oldLong = null;
 				try{
 					newLong= Long.parseLong(newVal);
 				}catch(NumberFormatException ex){}
 				try{
 					oldLong= Long.parseLong(oldVal);
 				}catch(NumberFormatException ex){};
 				
 				Date newDate= null;
 				Date oldDate  = null;
 				if(oldLong!=null){
 					oldDate = new Date(oldLong);
 				}
 				if(newLong !=null){
 					newDate = new Date(newLong);
 				}
 				setDate(newDate);
 				notifyListeners(oldDate,newDate);
 			}
 		});
 
 	}
 
 	/**
 	 * @return the locale
 	 */
 	public Locale getLocale() {
 		return locale;
 	}
 
 	/**
 	 * Calculates timezone specific date.
 	 * @param time
 	 * @return
 	 */
 	private Date getTimezoneSpecificDate(Long time){
 		long offset = getTimeZone().getOffset(time);
 		Date d = new Date(time-offset);
 		return d;
 	}
 	
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see de.jwic.controls.InputBoxControl#actionPerformed(java.lang.String,
 	 * java.lang.String)
 	 */
 	@Override
 	public void actionPerformed(String actionId, String parameter) {
 		if ("datechanged".equals(actionId)) {
 			Date oldDate = getDate();
 			currentTime = Long.valueOf(parameter);
 			Date newDate = getTimezoneSpecificDate(currentTime);
 			date = newDate;
 			notifyListeners(oldDate, newDate);
 		}
 		if ("localeNotFound".equals(actionId)) {
 			this.setLocale(Locale.ENGLISH);
 			log.info("The selected locale was not found. Defaulting back to Locale.ENGLISH");
 		}
 		if ("dateisempty".equals(actionId)) {
 			this.setDate(null);
 		}
 
 	}
 
 	private void notifyListeners(Date oldDate, Date newDate) {
 		for (DateChangedListener d : listeners) {
 			d.onDateChanged(oldDate, newDate);
 		}
 	}
 
 	/**
 	 * @param locale
 	 *            the locale to set
 	 * 
 	 * <br/>
 	 *            If the locale is not supported the control defaults to
 	 *            Locale.ENGLISH
 	 */
 
 	public void setLocale(Locale locale) {
 		this.locale = locale;
 		this.setDate(this.getDate());
 	}
 
 	/**
 	 * @param date
 	 *            the date to set
 	 * 
 	 */
 	public void setDate(Date date) {
 		//Date oldDate = this.getDate();
 		this.date = date;
 		if(date != null){
 			long offset = getTimeZone().getOffset(date.getTime());
 			currentTime = offset + date.getTime();
 			this.field.setValue(String.valueOf(date.getTime()));
 		}else {
 			currentTime = null;
 			this.field.setValue("");
 
 		}
 		this.requireRedraw();
 		
 
 	}
 	
 	/**
 	 * 
 	 * @return
 	 */
 	public TimeZone getTimeZone() {
 		return timeZone;
 	}
 
 	/**
 	 * 
 	 * @param timeZone
 	 */
 	public void setTimeZone(TimeZone timeZone) {
 		this.timeZone = timeZone;
 	}
 	
 	
 	/**
 	 * get current date as long. For internal use.
 	 * @return
 	 */
 	public Long getCurrentTime() {
 		return currentTime;
 	}
 
 	/**
 	 * @return the date
 	 */
 	public Date getDate() {
 		return date;
 	}
 
 	/**
 	 * 
 	 * @param dcl
 	 *            Add a DateChangeListener to this DatePickerControl that fires
 	 *            when a new date for this control is selected or set form code
 	 * 
 	 * 
 	 */
 	public void addDateChangedListener(final DateChangedListener dcl) {
 		this.listeners.add(dcl);
 	}
 
 	public void removeDateChangedListener(DateChangedListener dcl) {
 		this.listeners.remove(dcl);
 	}
 
 	/**
 	 * @param showMonth
 	 *            set true to show month drop down list <br/>
 	 *            Default is true
 	 */
 	public void setChangeMonth(boolean showMonth) {
 
 		this.changeMonth = showMonth;
 		this.requireRedraw();
 	}
 
 	/**
 	 * @param showYear
 	 *            set true to show year drop down list <br/>
 	 *            Default is true
 	 */
 	public void setChangeYear(boolean showYear) {
 		this.changeYear = showYear;
 		this.requireRedraw();
 	}
 
 	/**
 	 * @return true if this control is set to show the month selection dropdown
 	 *         list
 	 */
 	@IncludeJsOption
 	public boolean isChangeMonth() {
 		return changeMonth;
 	}
 
 	/**
 	 * @return true if this control is set to show the year selection dropdown
 	 *         list
 	 */
 	@IncludeJsOption
 	public boolean isChangeYear() {
 		return changeYear;
 	}
 
 	/**
 	 * 
 	 * @param dateFormat
 	 *            the format of the date (in SimpleDateFormat)<br/>
 	 *            the String representing the format can be null or empty in
 	 *            wich case it defaults to DatePickerControl.NO_FORMAT
 	 * 
 	 */
 	public void setDateFormat(String dateFormat) {
 		if (dateFormat != null && !dateFormat.isEmpty()) {
 			this.dateFormat = dateFormat;
 		} else {
 			this.dateFormat = NO_FORMAT;
 		}
 		this.requireRedraw();
 
 	}
 
 	/**
 	 * 
 	 * @return the date format if it was set to a non-null/non-empty value or
 	 *         DatePickerControl.NO_FORMAT
 	 * 
 	 */
 	public String getDateFormat() {
 		return dateFormat;
 	}
 
 	/**
 	 * 
 	 * @return the number of months to be displayed
 	 */
 	@IncludeJsOption
 	public int getNumberOfMonths() {
 		return numberOfMonths;
 	}
 
 	/**
 	 * 
 	 * @param the
 	 *            number of months to be displayed
 	 */
 	public void setNumberOfMonths(int numberOfMonths) {
 		this.numberOfMonths = numberOfMonths;
 		this.requireRedraw();
 	}
 
 	/**
 	 * 
 	 * @return
 	 */
 	@IncludeJsOption
 	public boolean isShowWeek() {
 		return showWeek;
 	}
 
 	/**
 	 * 
 	 * @param showWeek
 	 */
 	public void setShowWeek(boolean showWeek) {
 		this.showWeek = showWeek;
 		this.requireRedraw();
 	}
 
 	/**
 	 * 
 	 * @return
 	 */
 	public boolean isIconTriggered() {
 		return iconTriggered;
 	}
 
 	/**
 	 * 
 	 * @param iconTriggered
 	 */
 	public void setIconTriggered(boolean iconTriggered) {
 		this.iconTriggered = iconTriggered;
 		this.requireRedraw();
 	}
 
 	/**
 	 * @return the minDate
 	 */
 	@IncludeJsOption
 	public Date getMinDate() {
 		if(minDate == null)
 			return null;
 		long offset = getTimeZone().getOffset(minDate.getTime());
 		return new Date(offset + minDate.getTime());
 	}
 
 	/**
 	 * Returns minDate + timeZoneOffset!
 	 * @param minDate the minDate to set
 	 */
 	public void setMinDate(Date minDate) {
 		this.minDate = minDate;
 	}
 
 	/**
 	 * Returns maxDate + timeZoneOffset!
 	 * @return the maxDate
 	 */
 	@IncludeJsOption
 	public Date getMaxDate() {
 		if(maxDate == null)
 			return null;
 		long offset = getTimeZone().getOffset(maxDate.getTime());
 		return new Date(offset + maxDate.getTime());
 	}
 
 	/**
 	 * @param maxDate the maxDate to set
 	 */
 	public void setMaxDate(Date maxDate) {
 		this.maxDate = maxDate;
 	}
	
	/**
	 * @return
	 */
	@IncludeJsOption
	public boolean isUpdateOnChange() {
		return updateOnChange;
	}
	
	
	/**
	 * @param updateOnChange
	 */
	public void setUpdateOnChange(boolean updateOnChange) {
		this.updateOnChange = updateOnChange;
	}
 
 }
