 /*
  * Copyright (C) 2013 Frederic Dreyfus
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package org.vaadin.addons.tuningdatefield;
 
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.lang.reflect.Method;
 import java.text.DateFormatSymbols;
 import java.util.Calendar;
 import java.util.Locale;
 
 import org.joda.time.DateTimeConstants;
 import org.joda.time.Days;
 import org.joda.time.LocalDate;
 import org.joda.time.Months;
 import org.joda.time.YearMonth;
 import org.joda.time.format.DateTimeFormat;
 import org.joda.time.format.DateTimeFormatter;
 import org.vaadin.addons.tuningdatefield.event.CalendarOpenEvent;
 import org.vaadin.addons.tuningdatefield.event.CalendarOpenListener;
 import org.vaadin.addons.tuningdatefield.event.DateChangeEvent;
 import org.vaadin.addons.tuningdatefield.event.DateChangeListener;
 import org.vaadin.addons.tuningdatefield.event.DayClickEvent;
 import org.vaadin.addons.tuningdatefield.event.DayClickListener;
 import org.vaadin.addons.tuningdatefield.event.MonthChangeEvent;
 import org.vaadin.addons.tuningdatefield.event.MonthChangeListener;
 import org.vaadin.addons.tuningdatefield.event.ResolutionChangeEvent;
 import org.vaadin.addons.tuningdatefield.event.ResolutionChangeEvent.Resolution;
 import org.vaadin.addons.tuningdatefield.event.ResolutionChangeListener;
 import org.vaadin.addons.tuningdatefield.event.YearChangeEvent;
 import org.vaadin.addons.tuningdatefield.event.YearChangeListener;
 import org.vaadin.addons.tuningdatefield.widgetset.client.TuningDateFieldRpc;
 import org.vaadin.addons.tuningdatefield.widgetset.client.TuningDateFieldState;
 import org.vaadin.addons.tuningdatefield.widgetset.client.ui.calendar.CalendarItem;
 import org.vaadin.addons.tuningdatefield.widgetset.client.ui.calendar.CalendarResolution;
 
 import com.google.gwt.thirdparty.guava.common.base.Objects;
 import com.vaadin.data.Property;
 import com.vaadin.data.Validator.InvalidValueException;
 import com.vaadin.data.util.converter.Converter;
 import com.vaadin.data.util.converter.Converter.ConversionException;
 import com.vaadin.data.validator.RangeValidator;
 import com.vaadin.event.FieldEvents.BlurEvent;
 import com.vaadin.event.FieldEvents.BlurListener;
 import com.vaadin.event.FieldEvents.BlurNotifier;
 import com.vaadin.event.FieldEvents.FocusEvent;
 import com.vaadin.event.FieldEvents.FocusListener;
 import com.vaadin.event.FieldEvents.FocusNotifier;
 import com.vaadin.shared.MouseEventDetails;
 import com.vaadin.ui.AbstractField;
 import com.vaadin.ui.DateField.UnparsableDateString;
 import com.vaadin.ui.TextField;
 import com.vaadin.util.ReflectTools;
 
 /**
  * A date picker with a Joda {@link LocalDate} as model.<br>
  * <p>
  * Usage:<br>
  * 
  * <pre>
  * TuningDateField tuningDateField = new TuningDateField();
  * tuningDateField.setLocale(Locale.US); // optional
  * // A null range means no limit
  * tuningDateField.setDateRange(new LocalDate(2013, MAY, 10), new LocalDate(2013, JUNE, 5), &quot;The date must be between &quot;
  *         + startDate + &quot; and &quot; + endDate);
  * tuningDateField.setLocalDate(new LocalDate(2013, MAY, 15));
  * tuningDateField.setCellItemCustomizer(myTuningDateFieldCustomizer); // To customize cells of calendar
  * </pre>
  * 
  * 
  * 
  * <p>
  * The {@link TuningDateField} displays a {@link TextField} with proper LocalDate converter and a toggle button to
  * display a calendar.<br>
  * The default converter will use a short format . You can set your own formatter using
  * {@link #setDateTimeFormatterPattern(String)}.
  * 
  * <p>
  * To acess the {@link LocalDate} value of this field use the {@link #getLocalDate()} method which will return
  * <code>null</code> if the text value is null or if it is invalid.
  * 
  * 
  * <p>
  * You can customize cells of the calendar using the {@link CellItemCustomizer} and its convenient default
  * {@link CellItemCustomizerAdapter}. <br>
  * Example of a customizer which will apply even style to even days and odd styles for odd days in the calendar with
  * {@link CalendarResolution#DAY} resolution.<br>
  * It will also disable the 25th of December 2013:
  * 
  * <pre>
  * public class MyTuningDateFieldCustomizer extends TuningDateFieldCustomizerAdapter {
  * 
  *     &#064;Override
  *     public String getStyle(LocalDate date, TuningDateField calendar) {
  *         return date.getDayOfMonth() % 2 == 0 ? &quot;even&quot; : &quot;odd&quot;;
  *     }
  * 
  *     &#064;Override
  *     public boolean isEnabled(LocalDate date, TuningDateField calendar) {
  *         if (date.equals(new LocalDate(2013, DECEMBER, 25))) {
  *             return false;
  *         } else {
  *             return true;
  *         }
  *     }
  * }
  * </pre>
  * 
  * 
  * 
  * <p>
  * The primary stylename of the calendar is <code>tuning-datefield-calendar</code><br>
  * <br>
  * 
  * CSS styles for calendar {@link CalendarResolution#DAY} :
  * <ul>
  * <li>today : if cell represents the current day</li>
  * <li>selected : if cell represents the selected day</li>
  * <li>currentmonth : if cell represents a day on current calendar month</li>
  * <li>previousmonth : if cell represents a day on previous calendar month</li>
  * <li>nextmonth : if cell represents a day on next calendar month</li>
  * <li>weekend : if cell represents a week-end</li>
  * <li>enabled : if cell is enabled</li>
  * <li>disabled : if cell is disabled</li>
  * </ul>
  * 
  * CSS styles for calendar {@link CalendarResolution#MONTH} :
  * <ul>
  * <li>selected : if cell represents the selected month</li>
  * <li>currentmonth : if cell represents a day on current calendar month</li>
  * <li>enabled : if cell is enabled</li>
  * <li>disabled : if cell is disabled</li>
  * </ul>
  * 
  * CSS styles for calendar {@link CalendarResolution#YEAR} :
  * <ul>
  * <li>selected : if cell represents the selected month</li>
  * <li>currentyear : if cell represents a day on current calendar month</li>
  * <li>enabled : if cell is enabled</li>
  * <li>disabled : if cell is disabled</li>
  * </ul>
  * 
  * 
  * 
  * 
  * @author Frederic.Dreyfus
  * 
  */
 public class TuningDateField extends AbstractField<String> implements BlurNotifier, FocusNotifier {
 
     private static final long serialVersionUID = 5261965803349750329L;
 
     /**
      * The cell item customizer which allows to customize calendar cells.
      */
     private CellItemCustomizer cellItemCustomizer;
 
     /**
      * The date range validator if a range is defined
      * 
      * @see #setDateRange(LocalDate, LocalDate, String)
      */
     private RangeValidator<LocalDate> dateRangeValidator;
 
     // private boolean dayPicker = true;
     protected CalendarResolution calendarResolution = CalendarResolution.DAY;
 
     // The dateTimeFormatter pattern (ex: yyyy/MM/dd)
     protected String dateTimeFormatterPattern = null;
 
     // Internal use : as DateTimeFormatter is not Serializable, it's rebuilt from the dateTimeFormatterPattern
     private transient DateTimeFormatter dateTimeFormatter;
     // Internal use : the following 4 values are computed once at init and if the locale changes.
     protected transient String[] monthTexts; // Jan, Feb, Mar
     protected transient String[] shortMonthTexts; // Jan, Feb, Mar
     protected transient String[] weekDayNames; // Sun, Mon, Tue, ...
     protected transient Integer firstDayOfWeek; // 1 in France (monday), 7 in the US (sunday)
     protected transient Integer lastDayOfWeek; // 7 in France (sunday), 6 in the US (saturday)
 
     /**
      * True to disable weekends.
      * 
      * @see #setWeekendDisabled(boolean)
      */
     protected boolean weekendDisabled = true;
 
     /**
      * <code>true</code> do disable previous month (default to true)
      */
     protected boolean previousMonthDisabled = true;
 
     /**
      * <code>true</code> do disable next month (default to true)
      */
     protected boolean nextMonthDisabled = true;
 
     /**
      * True to enable controls
      * 
      * @see #setControlsEnabled(boolean)
      */
     protected boolean controlsEnabled = true;
 
     /**
      * True to enable/disabled dateText field edition
      * 
      * @see #setDateTextReadOnly(boolean)
      */
     private boolean dateTextReadOnly;
 
     // Internal use : the month currently displayed in the calendar
     protected YearMonth yearMonthDisplayed;
 
     // Internal use : the year currently displayed in the calendar
     private int yearDisplayed;
 
     // Internal use
     protected boolean calendarOpen;
 
     // Internal use: the current calendarItems displayed
     protected CalendarItem[] calendarItems;
 
     // Internal use : true when UI has a parsable valid string
     boolean uiHasValidDateString = true;
 
     private String parseErrorMessage = "Date format not recognized";
 
     /**
      * True to dsiplay a fixed number of day rows in the calendar day resolution (even with a row of next month days)
      */
     private boolean displayFixedNumberOfDayRows;
 
     public TuningDateField() {
         init();
         setValue(null);
     }
 
     public TuningDateField(String caption) {
         this();
         setCaption(caption);
         init();
     }
 
     public TuningDateField(Property<?> dataSource) {
         this(null, dataSource);
     }
 
     public TuningDateField(String caption, Property<?> dataSource) {
         init();
         setCaption(caption);
         setPropertyDataSource(dataSource);
 
     }
 
     public TuningDateField(String caption, LocalDate value) {
         init();
         setCaption(caption);
         setLocalDate(value);
 
     }
 
     private void init() {
         setupLocaleBasedStaticData(getLocale());
         initConverter();
         setYearMonthDisplayed(YearMonth.now());
         registerRpc();
 
         addValueChangeListener(new ValueChangeListener() {
 
             private static final long serialVersionUID = -8632906562585439165L;
 
             @Override
             public void valueChange(com.vaadin.data.Property.ValueChangeEvent event) {
                 fireEvent(new DateChangeEvent(TuningDateField.this, (LocalDate) getConvertedValue()));
             }
         });
     }
 
     /**
      * Initialize the {@link LocalDate} converter for the text field.
      */
     private void initConverter() {
 
         Converter<String, LocalDate> converter = new Converter<String, LocalDate>() {
 
             private static final long serialVersionUID = -2161506497954814519L;
 
             @Override
             public LocalDate convertToModel(String value, Class<? extends LocalDate> targetType, Locale locale)
                     throws com.vaadin.data.util.converter.Converter.ConversionException {
                 if (value == null) {
                     return null;
                 }
                 LocalDate modelValue = null;
                 try {
                     modelValue = dateTimeFormatter.parseLocalDate(value);
                 } catch (IllegalArgumentException e) {
                     throw new ConversionException("Cannot convert to model");
                 }
                 return modelValue;
             }
 
             @Override
             public String convertToPresentation(LocalDate value, Class<? extends String> targetType, Locale locale)
                     throws com.vaadin.data.util.converter.Converter.ConversionException {
                 if (value == null) {
                     return null;
                 }
                 String presentationValue = null;
                 try {
                     presentationValue = dateTimeFormatter.print(value);
                 } catch (IllegalArgumentException e) {
                     throw new ConversionException("Cannot convert to presentation");
                 }
 
                 return presentationValue;
             }
 
             @Override
             public Class<LocalDate> getModelType() {
                 return LocalDate.class;
             }
 
             @Override
             public Class<String> getPresentationType() {
                 return String.class;
             }
 
         };
         setConverter(converter);
     }
 
     protected void registerRpc() {
         registerRpc(new TuningDateFieldRpc() {
 
             private static final long serialVersionUID = 3572898507878457932L;
 
             @Override
             public void onCalendarOpen() {
                 TuningDateField.this.onCalendarOpen();
 
             }
 
             @Override
             public void onCalendarClosed() {
                 calendarOpen = false;
                 markAsDirty();
             }
 
             @Override
             public void calendarItemClicked(Integer itemIndex, Integer relativeDateIndex, MouseEventDetails mouseDetails) {
                 onCalendarItemClicked(itemIndex, relativeDateIndex, mouseDetails);
             }
 
             @Override
             public void dateTextChanged(String dateText) {
                 try {
                     // First try to convert to model in order to check if text is parseable
                     if (dateText != null) {
                         dateTimeFormatter.parseLocalDate(dateText);
                     }
 
                     // If parsing text is successful, set value
                     uiHasValidDateString = true;
                     setComponentError(null);
                     setValue(dateText);
                 } catch (IllegalArgumentException e) {
                     // Date is not parseable, keep previous value
                     uiHasValidDateString = false;
                     markAsDirty();
                 }
             }
 
             @Override
             public void previousControlClicked() {
                 if (controlsEnabled) {
                     goToPreviousCalendarPage();
                 } else {
                     // wtf ? should never happen
                 }
             }
 
             @Override
             public void nextControlClicked() {
                 if (controlsEnabled) {
                     goToNextCalendarPage();
                 } else {
                     // wtf ? should never happen
                 }
             }
 
             @Override
             public void resolutionControlClicked() {
                 if (controlsEnabled) {
                     swithToHigherCalendarResolution();
                 } else {
                     // wtf ? should never happen
                 }
             }
 
         });
     }
 
     public void setLocale(Locale locale) {
         super.setLocale(locale);
         // reinitialize static data based on locale (monthText, day names, etc...)
         boolean localeModified = Objects.equal(getLocale(), locale);
         if (localeModified) {
             setupLocaleBasedStaticData(locale);
         }
     }
 
     private void setupLocaleBasedStaticData(Locale locale) {
 
         if (locale == null) {
             locale = getLocale();
         }
 
         // Fall back to default locale
         if (locale == null) {
             locale = Locale.getDefault();
         }
 
         if (dateTimeFormatterPattern == null) {
             dateTimeFormatter = DateTimeFormat.shortDate().withLocale(locale);
         } else {
             dateTimeFormatter = DateTimeFormat.forPattern(dateTimeFormatterPattern).withLocale(locale);
         }
         monthTexts = new DateFormatSymbols(locale).getMonths();
         shortMonthTexts = new DateFormatSymbols(locale).getShortMonths();
 
         // These can be different locale that the translation one
         if (firstDayOfWeek == null) {
             firstDayOfWeek = getFirstDayOfWeek(locale);
         }
         if (lastDayOfWeek == null) {
             lastDayOfWeek = getLastDayOfWeek(locale);
         }
         weekDayNames = getWeekDayNames(locale, firstDayOfWeek);
     }
 
     /**
      * Sets the date range of this tuningDateField
      * 
      * @param startDate
      *            the start date (included). <code>null</code> for unlimited
      * @param endDate
      *            the end date (included). <code>null</code> for unlimited
      * @param errorMessage
      *            the error message
      */
     public void setDateRange(LocalDate startDate, LocalDate endDate, String errorMessage) {
         if (startDate != null && endDate != null && endDate.isBefore(startDate)) {
             throw new IllegalArgumentException("Cannot have a date range with end date " + endDate
                     + " before start date " + startDate);
         }
 
         removeDateRange();
         dateRangeValidator = new RangeValidator<LocalDate>(errorMessage, LocalDate.class, startDate, endDate);
         addValidator(dateRangeValidator);
 
         markAsDirty();
     }
 
     public void removeDateRange() {
         if (dateRangeValidator != null) {
             removeValidator(dateRangeValidator);
         }
     }
 
     /**
      * Returns <code>true</code> if :
      * <ol>
      * <li>date is in range</li>
      * <li>date is not a week-end, or if it is then week-ends are not disabled</li>
      * <li>date is not disabled by {@link CellItemCustomizer}</li>
      * </ol>
      * 
      * @param date
      *            the date
      * @return <code>true</code> if date is enabled, else returns <code>false</code>
      */
     protected boolean isDateEnabled(LocalDate date) {
         boolean enabled = false;
 
         enabled = isDateInRange(date);
         if (!enabled) {
             return enabled;
         }
 
         if (isWeekend(date) && isWeekendDisabled()) {
             return false;
         }
 
         if (enabled && cellItemCustomizer != null) {
             enabled = cellItemCustomizer.isEnabled(date, this);
         }
 
         return enabled;
     }
 
     /**
      * Returns <code>true</code> if date is in range, else returns <code>false</code>
      * 
      * @param date
      *            the date.
      * @return <code>true</code> if date is in range, else returns <code>false</code>
      */
     private boolean isDateInRange(LocalDate date) {
         if (dateRangeValidator == null) {
             return true;
         } else {
             return dateRangeValidator.isValid(date);
         }
     }
 
     /**
      * <code>true</code> if date is a week-end, else returns <code>false</code>. <br>
      * Override this method for custom week-ends days.
      * 
      * @param date
      *            the date
      * @return <code>true</code> if date is a week-end, else returns <code>false</code>
      */
     protected boolean isWeekend(LocalDate date) {
         return date.getDayOfWeek() >= DateTimeConstants.SATURDAY;
     }
 
     @Override
     public TuningDateFieldState getState() {
         return (TuningDateFieldState) super.getState();
     }
 
     @Override
     public void beforeClientResponse(boolean initial) {
         super.beforeClientResponse(initial);
 
         // For days of first week that are in previous month
         // Get first day of week of last week's previous month
         if (getValue() != null) {
             ((TuningDateFieldState) getState()).setDisplayedDateText(getValue());
         } else {
             ((TuningDateFieldState) getState()).setDisplayedDateText(null);
         }
         ((TuningDateFieldState) getState()).setCalendarOpen(calendarOpen);
         ((TuningDateFieldState) getState()).setDateTextReadOnly(dateTextReadOnly);
 
         // We send calendar state only if it's open
         if (calendarOpen) {
             ((TuningDateFieldState) getState()).setControlsEnabled(controlsEnabled);
             ((TuningDateFieldState) getState()).setCalendarResolution(calendarResolution);
 
             if (calendarResolution.equals(CalendarResolution.DAY)) {
                 YearMonth yearMonthDisplayed = getYearMonthDisplayed();
                 String displayedMonthText = monthTexts[yearMonthDisplayed.getMonthOfYear() - 1];
                 ((TuningDateFieldState) getState()).setCalendarResolutionText(displayedMonthText + " "
                         + yearMonthDisplayed.getYear());
                 ((TuningDateFieldState) getState()).setWeekHeaderNames(weekDayNames);
                 calendarItems = buildDayItems();
             } else if (calendarResolution.equals(CalendarResolution.MONTH)) {
                 calendarItems = buildMonthItems();
                 ((TuningDateFieldState) getState()).setCalendarResolutionText(Integer.toString(yearMonthDisplayed
                         .getYear()));
             } else if (calendarResolution.equals(CalendarResolution.YEAR)) {
                 calendarItems = buildYearItems();
                 ((TuningDateFieldState) getState()).setCalendarResolutionText(getCalendarFirstYear() + " - "
                         + getCalendarLastYear());
             }
             ((TuningDateFieldState) getState()).setCalendarItems(calendarItems);
         }
 
     }
 
     @Override
     public void validate() throws InvalidValueException {
         /*
          * To work properly in form we must throw exception if there is currently a parsing error in the datefield.
          * Parsing error is kind of an internal validator.
          */
         if (!uiHasValidDateString) {
             throw new UnparsableDateString(parseErrorMessage);
         }
         super.validate();
     }
 
     @Override
     protected void setInternalValue(String newValue) {
         if (!uiHasValidDateString) {
             // clear component error and parsing flag
             setComponentError(null);
             uiHasValidDateString = true;
         }
 
         super.setInternalValue(newValue);
     }
 
     protected CalendarItem[] buildDayItems() {
 
         LocalDate calendarFirstDay = getCalendarFirstDay();
         LocalDate calendarLastDay = getCalendarLastDay();
 
         LocalDate firstDayOfMonth = yearMonthDisplayed.toLocalDate(1);
         LocalDate lastDayOfMonth = yearMonthDisplayed.toLocalDate(1).dayOfMonth().withMaximumValue();
 
         LocalDate today = LocalDate.now();
         int numberOfDays = Days.daysBetween(calendarFirstDay, calendarLastDay).getDays() + 1;
         LocalDate date = calendarFirstDay;
 
         CalendarItem[] calendarItems = new CalendarItem[numberOfDays];
         LocalDate currentValue = getLocalDate();
         for (int i = 0; i < numberOfDays; i++, date = date.plusDays(1)) {
             calendarItems[i] = new CalendarItem();
 
             calendarItems[i].setIndex(i);
             if (date.getMonthOfYear() == yearMonthDisplayed.getMonthOfYear()) {
                 calendarItems[i].setRelativeDateIndex(date.getDayOfMonth());
             } else {
                 calendarItems[i].setRelativeDateIndex(-date.getDayOfMonth());
             }
 
             String calendarItemContent = null;
             if (cellItemCustomizer != null) {
                 calendarItemContent = cellItemCustomizer.renderDay(date, this);
             }
 
             // fallback to default value
             if (calendarItemContent == null) {
                 calendarItemContent = Integer.toString(date.getDayOfMonth());
             }
             calendarItems[i].setText(calendarItemContent);
 
             StringBuilder style = new StringBuilder();
 
             if (date.equals(today)) {
                 style.append("today ");
             }
 
             if (currentValue != null && date.equals(currentValue)) {
                 style.append("selected ");
             }
 
             if (date.isBefore(firstDayOfMonth)) {
                 style.append("previousmonth ");
                 calendarItems[i].setEnabled(!isPreviousMonthDisabled());
             } else if (date.isAfter(lastDayOfMonth)) {
                 style.append("nextmonth ");
                 calendarItems[i].setEnabled(!isNextMonthDisabled());
             } else {
                 style.append("currentmonth ");
                 calendarItems[i].setEnabled(isDateEnabled(date));
             }
 
             if (isWeekend(date)) {
                 style.append("weekend ");
             }
 
             if (cellItemCustomizer != null) {
                 String generatedStyle = cellItemCustomizer.getStyle(date, this);
                 if (generatedStyle != null) {
                     style.append(generatedStyle);
                     style.append(" ");
                 }
 
                 String tooltip = cellItemCustomizer.getTooltip(date, this);
                 if (tooltip != null) {
                     calendarItems[i].setTooltip(tooltip);
                 }
             }
 
             String computedStyle = style.toString();
             if (!computedStyle.isEmpty()) {
                 calendarItems[i].setStyle(computedStyle);
             }
         }
         return calendarItems;
     }
 
     protected CalendarItem[] buildMonthItems() {
 
         YearMonth calendarFirstMonth = getCalendarFirstMonth();
         YearMonth calendarLastMonth = getCalendarLastMonth();
 
         YearMonth currentMonth = YearMonth.now();
 
         int numberOfMonths = Months.monthsBetween(calendarFirstMonth, calendarLastMonth).getMonths() + 1;
 
         CalendarItem[] calendarItems = new CalendarItem[numberOfMonths];
         YearMonth month = calendarFirstMonth;
         LocalDate currentValue = getLocalDate();
         YearMonth currentYearMonthValue = currentValue == null ? null : new YearMonth(currentValue.getYear(),
                 currentValue.getMonthOfYear());
         for (int i = 0; i < numberOfMonths; i++, month = month.plusMonths(1)) {
             calendarItems[i] = new CalendarItem();
 
             calendarItems[i].setIndex(i);
             calendarItems[i].setRelativeDateIndex(month.getMonthOfYear());
             calendarItems[i].setEnabled(true); // By default
 
             StringBuilder style = new StringBuilder("");
 
             if (month.equals(currentMonth)) {
                 style.append("currentmonth ");
             }
 
             if (currentYearMonthValue != null && month.equals(currentYearMonthValue)) {
                 style.append("selected ");
             }
 
             if (cellItemCustomizer != null) {
                 String generatedStyle = cellItemCustomizer.getStyle(month, this);
                 if (generatedStyle != null) {
                     style.append(generatedStyle);
                     style.append(" ");
                 }
 
                 String tooltip = cellItemCustomizer.getTooltip(month, this);
                 if (tooltip != null) {
                     calendarItems[i].setTooltip(tooltip);
                 }
             }
 
             if (isMonthEnabled(month)) {
                 calendarItems[i].setEnabled(true);
             }
 
             String computedStyle = style.toString();
             if (!computedStyle.isEmpty()) {
                 calendarItems[i].setStyle(computedStyle);
             }
 
             String calendarItemContent = null;
             if (cellItemCustomizer != null) {
                 calendarItemContent = cellItemCustomizer.renderMonth(currentYearMonthValue, this);
             }
             // fallback to default value
             if (calendarItemContent == null) {
                 calendarItemContent = shortMonthTexts[i];
             }
 
             calendarItems[i].setText(calendarItemContent);
         }
         return calendarItems;
     }
 
     protected CalendarItem[] buildYearItems() {
 
         int calendarFirstYear = getCalendarFirstYear();
         int calendarLastYear = getCalendarLastYear();
 
         int currentYear = YearMonth.now().getYear();
         int numberOfYears = calendarLastYear - calendarFirstYear + 1;
 
         CalendarItem[] calendarItems = new CalendarItem[numberOfYears];
         int year = calendarFirstYear;
         LocalDate currentValue = getLocalDate();
         Integer currentYearValue = currentValue == null ? null : currentValue.getYear();
         for (int i = 0; i < numberOfYears; i++, year++) {
             calendarItems[i] = new CalendarItem();
 
             calendarItems[i].setIndex(i);
             calendarItems[i].setRelativeDateIndex(year);
             calendarItems[i].setEnabled(true);
 
             StringBuilder style = new StringBuilder("");
 
             if (year == currentYear) {
                 style.append("currentyear ");
             }
 
             if (currentYearValue != null && year == currentYearValue) {
                 style.append("selected ");
             }
 
             if (isYearEnabled(year)) {
                 calendarItems[i].setEnabled(true);
             }
 
             if (cellItemCustomizer != null) {
                 String generatedStyle = cellItemCustomizer.getStyle(year, this);
                 if (generatedStyle != null) {
                     style.append(generatedStyle);
                     style.append(" ");
                 }
 
                 String tooltip = cellItemCustomizer.getTooltip(year, this);
                 if (tooltip != null) {
                     calendarItems[i].setTooltip(tooltip);
                 }
             }
 
             String computedStyle = style.toString();
             if (!computedStyle.isEmpty()) {
                 calendarItems[i].setStyle(computedStyle);
             }
 
             String calendarItemContent = null;
             if (cellItemCustomizer != null) {
                 calendarItemContent = cellItemCustomizer.renderYear(year, this);
             }
             // fallback to default value
             if (calendarItemContent == null) {
                 calendarItemContent = Integer.toString(year);
             }
             calendarItems[i].setText(calendarItemContent);
 
         }
         return calendarItems;
     }
 
     /**
      * Sets the localDate value of this field.
      * 
      * @param localDate
      *            the localDate
      */
     public void setLocalDate(LocalDate localDate) {
         setConvertedValue(localDate);
     }
 
     /**
      * As the value of the field is a String, the representation may be corrupted to be parsed into a {@link LocalDate}.
      * In that case we return <code>null</code>.
      * 
      * @return the localDate value of this field if defined and valid, else returns <code>null</code>.
      */
     public LocalDate getLocalDate() {
         try {
             return (LocalDate) getConvertedValue();
         } catch (ConversionException e) {
             // In that case the value is invalid
             return null;
         }
 
     }
 
     /**
      * @return the first day of the calendar. As there are 7 columns displayed, if the first day of month is not in the
      *         first column, we fill previous column items with days of previous month.
      */
     private LocalDate getCalendarFirstDay() {
         LocalDate firstDayOfMonth = yearMonthDisplayed.toLocalDate(1);
 
         int calendarFirstDayOfWeek = firstDayOfWeek;
         int numberOfDaysSinceFirstDayOfWeek = (firstDayOfMonth.getDayOfWeek() - calendarFirstDayOfWeek + 7) % 7;
 
         return firstDayOfMonth.minusDays(numberOfDaysSinceFirstDayOfWeek);
     }
 
     /**
      * @return the last day of the calendar. As there are 7 columns displayed, if the last day of month is not in the
      *         last column, we fill next column items with days of next month.
      */
     private LocalDate getCalendarLastDay() {
         LocalDate lastDayOfMonth = yearMonthDisplayed.toLocalDate(1).dayOfMonth().withMaximumValue();
 
         int calendarLastDayOfWeek = lastDayOfWeek;
 
         int numberOfDaysUntilLastDayOfWeek = (calendarLastDayOfWeek - lastDayOfMonth.getDayOfWeek() + 7) % 7;
 
         LocalDate lastDay = lastDayOfMonth.plusDays(numberOfDaysUntilLastDayOfWeek);
         if (isDisplayFixedNumberOfDayRows()) {
             // Always display 6 day rows
             int numberOfDays = Days.daysBetween(getCalendarFirstDay(), lastDay).getDays() + 1;
             if (numberOfDays / 7 < 5) {
                 lastDay = lastDay.plusDays(14);
             } else if (numberOfDays / 7 < 6) {
                 lastDay = lastDay.plusDays(7);
             }
         }
 
         return lastDay;
 
     }
 
     private YearMonth getCalendarFirstMonth() {
         return new YearMonth(yearDisplayed, 1);
     }
 
     private YearMonth getCalendarLastMonth() {
         return new YearMonth(yearDisplayed, 12);
 
     }
 
     /**
      * If current year displayed is 1954, the range is 1949-1960.
      * 
      * @return the calendar first year.
      */
     protected int getCalendarFirstYear() {
         return yearDisplayed - yearDisplayed % 10 - 1;
     }
 
     /**
      * If current year displayed is 1954, the range is 1949-1960
      * 
      * @return the calendar last year.
      */
     protected int getCalendarLastYear() {
         return yearDisplayed - yearDisplayed % 10 + 10;
     }
 
     /**
      * Returns the selected date from the dayOfMonth. <br>
      * If dayOfMonth is negative, selected date is on previous or next month according its value.
      * 
      * @param dayOfMonth
      *            the day of month
      * @return the selected date from the dayOfMonth.
      */
     private LocalDate getSelectedDate(int dayOfMonth) {
         if (dayOfMonth >= 0) {
             return yearMonthDisplayed.toLocalDate(dayOfMonth);
         } else {
             if (dayOfMonth < -7) {
                 // previous month
                 return new LocalDate(yearMonthDisplayed.minusMonths(1).toLocalDate(-dayOfMonth));
             } else {
                 // next month
                 return new LocalDate(yearMonthDisplayed.plusMonths(1).toLocalDate(-dayOfMonth));
             }
         }
 
     }
 
     private YearMonth getSelectedMonth(int monthOfYear) {
         return new YearMonth(yearDisplayed, monthOfYear);
     }
 
     /**
      * Called when the calendar is open on client-side
      */
     private void onCalendarOpen() {
         calendarResolution = CalendarResolution.DAY;
         LocalDate currentValue = getLocalDate();
         if (currentValue != null) {
             yearMonthDisplayed = new YearMonth(currentValue);
         } else if (yearMonthDisplayed == null) {
             yearMonthDisplayed = YearMonth.now();
         }
 
         fireEvent(new CalendarOpenEvent(this, yearMonthDisplayed));
 
         calendarOpen = true;
 
         markAsDirty();
     }
 
     /**
      * Called when user clicked on cell item
      * 
      * @param itemIndex
      *            the item index
      * @param relativeDateIndex
      *            is dayOfMonth in day resolution, monthOfYear in month resolution, year in year resolution
      * @param mouseDetails
      *            the mouse details info
      */
     protected void onCalendarItemClicked(int itemIndex, int relativeDateIndex, MouseEventDetails mouseDetails) {
         if (calendarResolution.equals(CalendarResolution.DAY)) {
             if (isDateEnabled(getSelectedDate(relativeDateIndex))) { // We check the date is not disabled
                 LocalDate selectedDate = getSelectedDate(relativeDateIndex);
                 fireEvent(new DayClickEvent(this, mouseDetails, selectedDate));
                 setConvertedValue(selectedDate);
                 // Should now close the calendar
                 calendarOpen = false;
             }
         } else if (calendarResolution.equals(CalendarResolution.MONTH)) {
             if (isMonthEnabled(getSelectedMonth(relativeDateIndex))) {
                 YearMonth selectedMonth = getSelectedMonth(relativeDateIndex);
                 setYearMonthDisplayed(selectedMonth);
                 setCalendarResolution(CalendarResolution.DAY);
                 fireEvent(new ResolutionChangeEvent(this, Resolution.DAY));
                 fireEvent(new MonthChangeEvent(this, selectedMonth));
             }
         } else if (calendarResolution.equals(CalendarResolution.YEAR)) {
             if (isYearEnabled(relativeDateIndex)) {
                 setYearMonthDisplayed(new YearMonth(relativeDateIndex, getYearMonthDisplayed().getMonthOfYear()));
                 setCalendarResolution(CalendarResolution.MONTH);
                 fireEvent(new ResolutionChangeEvent(this, Resolution.MONTH));
             }
         }
     }
 
     /**
      * Called when user clicked on the next page control
      */
     public void goToNextCalendarPage() {
         if (calendarResolution.equals(CalendarResolution.DAY)) {
             setYearMonthDisplayed(yearMonthDisplayed.plusMonths(1));
             fireEvent(new MonthChangeEvent(this, yearMonthDisplayed));
         } else if (calendarResolution.equals(CalendarResolution.MONTH)) {
             setYearMonthDisplayed(yearMonthDisplayed.plusYears(1));
             fireEvent(new YearChangeEvent(this, yearDisplayed));
         } else if (calendarResolution.equals(CalendarResolution.YEAR)) {
             setYearMonthDisplayed(yearMonthDisplayed.plusYears(10));
         }
 
     }
 
     /**
      * Called when user clicked on the previous page control
      */
     public void goToPreviousCalendarPage() {
         if (calendarResolution.equals(CalendarResolution.DAY)) {
             setYearMonthDisplayed(yearMonthDisplayed.minusMonths(1));
             fireEvent(new MonthChangeEvent(this, yearMonthDisplayed));
         } else if (calendarResolution.equals(CalendarResolution.MONTH)) {
             setYearMonthDisplayed(yearMonthDisplayed.minusYears(1));
             fireEvent(new YearChangeEvent(this, yearDisplayed));
         } else if (calendarResolution.equals(CalendarResolution.YEAR)) {
             setYearMonthDisplayed(yearMonthDisplayed.minusYears(10));
         }
 
     }
 
     /**
      * Called when user clicked on the resolution control
      */
     public void swithToHigherCalendarResolution() {
         if (calendarResolution.equals(CalendarResolution.DAY)) {
             setCalendarResolution(CalendarResolution.MONTH);
             fireEvent(new ResolutionChangeEvent(this, Resolution.MONTH));
         } else if (calendarResolution.equals(CalendarResolution.MONTH)) {
             setCalendarResolution(CalendarResolution.YEAR);
             fireEvent(new ResolutionChangeEvent(this, Resolution.YEAR));
         }
         markAsDirty();
     }
 
     /**
      * Returns true if month is enabled. Default implementations returns {@link CellItemCustomizer} value if any.
      * 
      * @param yearMonth
      *            the month
      * @return true if month is enabled.
      */
     protected boolean isMonthEnabled(YearMonth yearMonth) {
         if (cellItemCustomizer != null) {
             return cellItemCustomizer.isEnabled(yearMonth, this);
         }
         return true;
     }
 
     /**
      * Returns true if year is enabled. Default implementations returns {@link CellItemCustomizer} value if any.
      * 
      * @param year
      *            the year
      * @return true if year is enabled.
      */
     protected boolean isYearEnabled(int year) {
         if (cellItemCustomizer != null) {
             return cellItemCustomizer.isEnabled(year, this);
         }
         return true;
     }
 
     /**
      * Returns the week header names in the order of appearance in the calendar.<br>
      * Ex for {@link Locale#FRANCE} : [lun., mar., mer., jeu., ven., sam., dim.] Ex for {@link Locale#US} : [Sun, Mon,
      * Tue, Wed, Thu, Fri, Sat]
      * 
      * @param locale
      *            the locale
      * @param firstDayOfWeek
      *            the first day of week
      * @return the week header names in the order of appearance in the calendar.
      */
     protected String[] getWeekDayNames(Locale locale, int firstDayOfWeek) {
         String[] weekHeaderNames = new String[7];
 
         String[] weekDays = DateFormatSymbols.getInstance(locale).getShortWeekdays();
         for (int i = 0; i < 7; i++) {
             weekHeaderNames[i] = weekDays[(firstDayOfWeek + i) % 7 + 1];
         }
         return weekHeaderNames;
     }
 
     /**
      * Gets the first day of the week, in the given locale.
      * 
      * @param locale
      *            the locale
      * @return a value in the range of {@link DateTimeConstants#MONDAY} to {@link DateTimeConstants#SUNDAY}.
      */
     private final int getFirstDayOfWeek(Locale locale) {
         Calendar calendar;
         if (locale != null) {
             calendar = Calendar.getInstance(locale);
         } else {
             calendar = Calendar.getInstance();
         }
         return ((calendar.getFirstDayOfWeek() + 5) % 7) + 1;
     }
 
     /**
      * Gets the last day of the week, in the given locale.
      * 
      * @param locale
      *            the locale
      * @return a value in the range of {@link DateTimeConstants#MONDAY} to {@link DateTimeConstants#SUNDAY}.
      */
     private final int getLastDayOfWeek(Locale locale) {
         Calendar calendar;
         if (locale != null) {
             calendar = Calendar.getInstance(locale);
         } else {
             calendar = Calendar.getInstance();
         }
         return ((calendar.getFirstDayOfWeek() + 4) % 7) + 1;
     }
 
     public static final Method DAY_CLICK_METHOD = ReflectTools.findMethod(DayClickListener.class, "dayClick",
             DayClickEvent.class);
 
     public void addDayClickListener(DayClickListener listener) {
         addListener(DayClickEvent.class, listener, DAY_CLICK_METHOD);
     }
 
     public void removeItemClickListener(DayClickListener listener) {
         removeListener(DayClickEvent.class, listener, DAY_CLICK_METHOD);
     }
 
     public static final Method CALENDAR_OPEN_METHOD = ReflectTools.findMethod(CalendarOpenListener.class,
             "calendarOpen", CalendarOpenEvent.class);
 
     public void addCalendarOpenListener(CalendarOpenListener listener) {
         addListener(CalendarOpenEvent.class, listener, CALENDAR_OPEN_METHOD);
     }
 
     public void removeCalendarOpenListener(CalendarOpenListener listener) {
         removeListener(CalendarOpenEvent.class, listener, CALENDAR_OPEN_METHOD);
     }
 
     public static final Method DATE_CHANGE_METHOD = ReflectTools.findMethod(DateChangeListener.class, "dateChange",
             DateChangeEvent.class);
 
     public void addDateChangeListener(DateChangeListener listener) {
         addListener(DateChangeEvent.class, listener, DATE_CHANGE_METHOD);
     }
 
     public void removeDateChangeListener(DateChangeListener listener) {
         removeListener(DateChangeEvent.class, listener, DATE_CHANGE_METHOD);
     }
 
     public static final Method MONTH_CHANGE_METHOD = ReflectTools.findMethod(MonthChangeListener.class, "monthChange",
             MonthChangeEvent.class);
 
     public void addMonthChangeListener(MonthChangeListener listener) {
         addListener(MonthChangeEvent.class, listener, MONTH_CHANGE_METHOD);
     }
 
     public void removeMonthChangeListener(MonthChangeListener listener) {
         removeListener(MonthChangeEvent.class, listener, MONTH_CHANGE_METHOD);
     }
 
     public static final Method YEAR_CHANGE_METHOD = ReflectTools.findMethod(YearChangeListener.class, "yearChange",
             YearChangeEvent.class);
 
     public void addYearChangeListener(YearChangeListener listener) {
         addListener(YearChangeEvent.class, listener, YEAR_CHANGE_METHOD);
     }
 
     public void removeYearChangeListener(YearChangeListener listener) {
         removeListener(YearChangeEvent.class, listener, YEAR_CHANGE_METHOD);
     }
 
     public static final Method RESOLUTION_CHANGE_METHOD = ReflectTools.findMethod(ResolutionChangeListener.class,
             "resolutionChange", ResolutionChangeEvent.class);
 
     public void addResolutionChangeListener(ResolutionChangeListener listener) {
         addListener(ResolutionChangeEvent.class, listener, RESOLUTION_CHANGE_METHOD);
     }
 
     public void removeResolutionChangeListener(ResolutionChangeListener listener) {
         removeListener(ResolutionChangeEvent.class, listener, RESOLUTION_CHANGE_METHOD);
     }
 
     @Override
     public Class<? extends String> getType() {
         return String.class;
     }
 
     // Used to rebuild transient variables
     private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
         in.defaultReadObject();
         setupLocaleBasedStaticData(getLocale());
     }
 
     /**
      * Sets the first day of week (1=Monday, 2=Tuesday,...,7=SUNDAY). <br>
      * If not defined it will used the one from the Locale.
      * 
      * @param firstDayOfWeek
      *            the first day of week
      */
     public void setFirstDayOfWeek(int firstDayOfWeek) {
         this.firstDayOfWeek = firstDayOfWeek;
         setupLocaleBasedStaticData(getLocale());
         markAsDirty();
     }
 
     /**
      * Sets the last day of week (1=Monday, 2=Tuesday,...,7=SUNDAY). <br>
      * If not defined it will used the one from the Locale.
      * 
      * @param lastDayOfWeek
      *            the last day of week
      */
     public void setLastDayOfWeek(int lastDayOfWeek) {
         this.lastDayOfWeek = lastDayOfWeek;
         setupLocaleBasedStaticData(getLocale());
         markAsDirty();
     }
 
     /**
      * @return the monthDisplayed
      */
     public YearMonth getYearMonthDisplayed() {
         return yearMonthDisplayed;
     }
 
     /**
      * @param yearMonthDisplayed
      *            the yearMonthDisplayed to set
      */
     public void setYearMonthDisplayed(YearMonth yearMonthDisplayed) {
         this.yearMonthDisplayed = yearMonthDisplayed;
         this.yearDisplayed = yearMonthDisplayed.getYear();
         markAsDirty();
     }
 
     /**
      * @return the yearDisplayed
      */
     public Integer getYearDisplayed() {
         return yearDisplayed;
     }
 
     /**
      * @param yearDisplayed
      *            the yearDisplayed to set
      */
     public void setYearDisplayed(Integer yearDisplayed) {
         this.yearDisplayed = yearDisplayed;
         markAsDirty();
     }
 
     /**
      * Returns the {@link DateTimeFormatter} used.
      * 
      * @return the {@link DateTimeFormatter} used.
      */
     public DateTimeFormatter getDateTimeFormatter() {
         return dateTimeFormatter;
     }
 
     /**
      * @return the dateTimeFormatterPattern
      */
     public String getDateTimeFormatterPattern() {
         return dateTimeFormatterPattern;
     }
 
     /**
      * @param dateTimeFormatterPattern
      *            the dateTimeFormatterPattern to set
      */
     public void setDateTimeFormatterPattern(final String dateTimeFormatterPattern) {
         this.dateTimeFormatterPattern = dateTimeFormatterPattern;
         if (dateTimeFormatterPattern == null) {
             dateTimeFormatter = DateTimeFormat.shortDate().withLocale(getLocale());
         } else {
             dateTimeFormatter = DateTimeFormat.forPattern(dateTimeFormatterPattern).withLocale(getLocale());
         }
     }
 
     /**
      * @return the weekendDisabled
      */
     public boolean isWeekendDisabled() {
         return weekendDisabled;
     }
 
     /**
      * @param weekendDisabled
      *            the weekendDisabled to set
      */
     public void setWeekendDisabled(boolean weekendDisabled) {
         this.weekendDisabled = weekendDisabled;
     }
 
     /**
      * @return the calendarResolution
      */
     public CalendarResolution getCalendarResolution() {
         return calendarResolution;
     }
 
     /**
      * @param calendarResolution
      *            the calendarResolution to set
      */
     public void setCalendarResolution(CalendarResolution calendarResolution) {
         this.calendarResolution = calendarResolution;
     }
 
     /**
      * @return the controlsEnabled
      */
     public boolean isControlsEnabled() {
         return controlsEnabled;
     }
 
     /**
      * @param controlsEnabled
      *            the controlsEnabled to set
      */
     public void setControlsEnabled(boolean controlsEnabled) {
         this.controlsEnabled = controlsEnabled;
         markAsDirty();
     }
 
     /**
      * @return the cellItemCustomizer
      */
     public CellItemCustomizer getCellItemCustomizer() {
         return cellItemCustomizer;
     }
 
     /**
      * @param cellItemCustomizer
      *            the cellItemCustomizer to set
      */
     public void setCellItemCustomizer(CellItemCustomizer cellItemCustomizer) {
         this.cellItemCustomizer = cellItemCustomizer;
     }
 
     /**
      * @return the dateTextReadOnly
      */
     public boolean isDateTextReadOnly() {
         return dateTextReadOnly;
     }
 
     /**
      * @param dateTextReadOnly
      *            the dateTextReadOnly to set
      */
     public void setDateTextReadOnly(boolean dateTextReadOnly) {
         this.dateTextReadOnly = dateTextReadOnly;
         markAsDirty();
     }
 
     public boolean isPreviousMonthDisabled() {
         return previousMonthDisabled;
     }
 
     public void setPreviousMonthDisabled(boolean previousMonthDisabled) {
         this.previousMonthDisabled = previousMonthDisabled;
     }
 
     public boolean isNextMonthDisabled() {
         return nextMonthDisabled;
     }
 
     public void setNextMonthDisabled(boolean nextMonthDisabled) {
         this.nextMonthDisabled = nextMonthDisabled;
     }
 
     @Override
     public void addFocusListener(FocusListener listener) {
         addListener(FocusEvent.EVENT_ID, FocusEvent.class, listener, FocusListener.focusMethod);
     }
 
     /**
      * @deprecated As of 7.0, replaced by {@link #addFocusListener(FocusListener)}
      **/
     @Override
     @Deprecated
     public void addListener(FocusListener listener) {
         addFocusListener(listener);
     }
 
     @Override
     public void removeFocusListener(FocusListener listener) {
         removeListener(FocusEvent.EVENT_ID, FocusEvent.class, listener);
     }
 
     /**
      * @deprecated As of 7.0, replaced by {@link #removeFocusListener(FocusListener)}
      **/
     @Override
     @Deprecated
     public void removeListener(FocusListener listener) {
         removeFocusListener(listener);
     }
 
     @Override
     public void addBlurListener(BlurListener listener) {
         addListener(BlurEvent.EVENT_ID, BlurEvent.class, listener, BlurListener.blurMethod);
     }
 
     /**
      * @deprecated As of 7.0, replaced by {@link #addBlurListener(BlurListener)}
      **/
     @Override
     @Deprecated
     public void addListener(BlurListener listener) {
         addBlurListener(listener);
     }
 
     @Override
     public void removeBlurListener(BlurListener listener) {
         removeListener(BlurEvent.EVENT_ID, BlurEvent.class, listener);
     }
 
     /**
      * @deprecated As of 7.0, replaced by {@link #removeBlurListener(BlurListener)}
      **/
     @Override
     @Deprecated
     public void removeListener(BlurListener listener) {
         removeBlurListener(listener);
     }
 
     @Override
     public void focus() {
         super.focus();
     }
 
     public String getParseErrorMessage() {
         return parseErrorMessage;
     }
 
     public void setParseErrorMessage(String parseErrorMessage) {
         this.parseErrorMessage = parseErrorMessage;
     }
 
     public boolean isDisplayFixedNumberOfDayRows() {
         return displayFixedNumberOfDayRows;
     }
 
     /**
      * Set to <code>true</code> to fix the number of lines in the calendar day resolution (even with a row of next month
      * days)
      * 
     * @param displayFixedNumberOfDayRows
      *            <code>true</code> to fix the number of lines in the calendar day resolution
      */
     public void setDisplayFixedNumberOfDayRows(boolean displayFixedNumberOfDayRows) {
         this.displayFixedNumberOfDayRows = displayFixedNumberOfDayRows;
     }
 
 }
