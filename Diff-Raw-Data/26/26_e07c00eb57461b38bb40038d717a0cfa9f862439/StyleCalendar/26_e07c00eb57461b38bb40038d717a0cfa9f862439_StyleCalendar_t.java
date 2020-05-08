 package org.vaadin.risto.stylecalendar;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 
 import org.vaadin.risto.stylecalendar.widgetset.client.ui.VStyleCalendar;
 
 import com.vaadin.terminal.PaintException;
 import com.vaadin.terminal.PaintTarget;
 import com.vaadin.ui.AbstractField;
 import com.vaadin.ui.ClientWidget;
 
 /**
  * 
  * StyleCalendar is designed to be a simple, easily stylable calendar component.
  * 
  * {@link DateOptionsGenerator} can be used to generate stylenames externally.
  * 
  * @author Risto Yrjänä / Vaadin
  * 
  */
 @ClientWidget(VStyleCalendar.class)
 public class StyleCalendar extends AbstractField {
 
     private static final long serialVersionUID = 7797206568110243067L;
 
     private DateOptionsGenerator dateOptionsGenerator;
 
     private boolean renderControls;
 
     private boolean renderHeader;
 
     private boolean renderWeekNumbers;
 
     private Date showingDate = null;
 
     private List<Integer> disabledRenderedDays;
 
     private Date enabledStartDate;
 
     private Date enabledEndDate;
 
     private boolean nextMonthEnabled;
 
     private boolean prevMonthEnabled;
 
     /**
      * Create a new StyleCalendar instance. Header, controls and week numbers
      * are rendered by default.
      */
     public StyleCalendar() {
         super();
         setRenderHeader(true);
         setRenderControls(true);
         setRenderWeekNumbers(true);
 
         setShowingDate(new Date());
     }
 
     /**
      * Create a new StyleCalendar instance. Header, controls and week numbers
      * are rendered by default.
      * 
      * @param caption
      *            components caption
      */
     public StyleCalendar(String caption) {
         this();
         setCaption(caption);
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see com.vaadin.ui.AbstractField#getType()
      */
     @Override
     public Class<?> getType() {
         return Date.class;
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see
      * com.vaadin.ui.AbstractField#paintContent(com.vaadin.terminal.PaintTarget)
      */
     @Override
     public void paintContent(PaintTarget target) throws PaintException {
         super.paintContent(target);
 
         // init calendar and date related variables
         Date selectedDate = (Date) getValue();
         Date today = new Date();
         Date showingDate = getShowingDate();
         Locale locale = getLocale();
 
         // for remembering disabled variables sent to client
         disabledRenderedDays = new ArrayList<Integer>();
         nextMonthEnabled = true;
         prevMonthEnabled = true;
 
         Calendar calendar = getCalendarInstance();
 
         calendar.setTime(getShowingDate());
         calendar.set(Calendar.DAY_OF_MONTH, 1);
 
         int daysInWeek = calendar.getActualMaximum(Calendar.DAY_OF_WEEK);
         int firstDayOfWeek = calendar.getFirstDayOfWeek();
 
         // set main tag attributes
         target.addAttribute("renderWeekNumbers", isRenderWeekNumbers());
         target.addAttribute("renderHeader", isRenderHeader());
         target.addAttribute("renderControls", isRenderControls());
 
         // render header
         if (isRenderHeader()) {
             target.startTag("header");
             target.addAttribute("currentYear", calendar.get(Calendar.YEAR));
             target.addAttribute("currentMonth",
                     getMonthCaption(calendar.getTime(), 0, true));
 
             // render controls
             if (isRenderControls()) {
                 target.startTag("controls");
 
                 target.addAttribute("prevMonth",
                         getMonthCaption(calendar.getTime(), -1, false));
 
                 if (isDisabledMonth(calendar.getTime(), -1)) {
                     target.addAttribute("prevMonthDisabled", true);
                     prevMonthEnabled = false;
                 }
 
                 target.addAttribute("nextMonth",
                         getMonthCaption(calendar.getTime(), 1, false));
 
                 if (isDisabledMonth(calendar.getTime(), 1)) {
                     target.addAttribute("nextMonthDisabled", true);
                     nextMonthEnabled = false;
                 }
 
                 target.endTag("controls");
             }
             target.endTag("header");
         }
 
         // render weekday names
         Calendar calendarForWeekdays = (Calendar) calendar.clone();
         calendarForWeekdays.set(Calendar.DAY_OF_WEEK, firstDayOfWeek);
 
         String[] weekDaysArray = new String[daysInWeek];
         for (int weekday = 0; weekday < daysInWeek; weekday++) {
             weekDaysArray[weekday] = calendarForWeekdays.getDisplayName(
                     Calendar.DAY_OF_WEEK, Calendar.SHORT, locale);
             calendarForWeekdays.add(Calendar.DAY_OF_WEEK, 1);
         }
 
         target.addVariable(this, "weekDayNames", weekDaysArray);
 
         // so we get the right amount of weeks to render
         calendar.setMinimalDaysInFirstWeek(1);
 
         // render weeks and days
 
         int numberOfWeeks = calendar.getActualMaximum(Calendar.WEEK_OF_MONTH);
 
         for (int week = 1; week < numberOfWeeks + 1; week++) {
             calendar.setTime(showingDate);
             calendar.set(Calendar.WEEK_OF_MONTH, week);
             target.startTag("week");
 
             target.addAttribute("number", calendar.get(Calendar.WEEK_OF_YEAR));
 
             // reset to the start of the week
             calendar.set(Calendar.DAY_OF_WEEK, firstDayOfWeek);
 
             for (int day = 0; day < daysInWeek; day++) {
                 target.startTag("day");
                 target.addAttribute("daynumber",
                         calendar.get(Calendar.DAY_OF_MONTH));
 
                 // compute styles for given day
                 StringBuilder dayStyle = new StringBuilder();
 
                 if (dayEquals(calendar.getTime(), today)) {
                     dayStyle.append("today");
                 }
 
                 if (dayEquals(calendar.getTime(), selectedDate)) {
                     dayStyle.append(" ");
                     dayStyle.append("selected");
                 }
 
                 if (monthEquals(calendar.getTime(), showingDate)) {
                     dayStyle.append(" ");
                     dayStyle.append("currentmonth");
                     target.addAttribute("clickable", true);
                 } else {
                     dayStyle.append(" ");
                     dayStyle.append("othermonth");
                     target.addAttribute("clickable", false);
                 }
 
                 if (isWeekend(calendar.getTime())) {
                     dayStyle.append(" ");
                     dayStyle.append("weekend");
                 }
 
                 if (getDateOptionsGenerator() != null) {
                     String generatedStyle = getDateOptionsGenerator()
                             .getStyleName(calendar.getTime(), this);
                     if (generatedStyle != null) {
                         dayStyle.append(" ");
                         dayStyle.append(generatedStyle);
                     }
 
                     if (isDisabledDate(calendar.getTime())) {
                         target.addAttribute("disabled", true);
                         disabledRenderedDays.add(calendar
                                .get(Calendar.DAY_OF_YEAR));
                     }
                 }
 
                 String dayStyleString = dayStyle.toString();
                 if (!dayStyleString.isEmpty()) {
                     target.addAttribute("style", dayStyleString);
                 }
 
                 target.endTag("day");
 
                 // move to the next day
                 calendar.add(Calendar.DAY_OF_WEEK, 1);
             }
             target.endTag("week");
         }
     }
 
     @Override
     public void changeVariables(Object source, Map<String, Object> variables) {
         super.changeVariables(source, variables);
 
         // user clicked on a day
         if (variables.containsKey("clickedDay")) {
             Integer clickedDay = (Integer) variables.get("clickedDay");
 
             if (!isDisabled(clickedDay)) {
                Date selectedDate = constructNewDateValue((Integer) variables
                         .get("clickedDay"));
                 setValue(selectedDate);
             } else {
                 // Ch-ch-cheater. Do nothing.
             }
         }
 
         if (variables.containsKey("prevClick")) {
             if (isPrevMonthAllowed()) {
                 showPreviousMonth();
             } else {
                 // Ch-ch-cheater. Do nothing.
             }
 
         } else if (variables.containsKey("nextClick")) {
             if (isNextMonthAllowed()) {
                 showNextMonth();
             } else {
                 // Ch-ch-cheater. Do nothing.
             }
         }
 
     }
 
     /**
      * Set the style generator used. This is called on every day shown.
      * 
      * @param dateOptionsGenerator
      */
     public void setDateOptionsGenerator(
             DateOptionsGenerator dateOptionsGenerator) {
         this.dateOptionsGenerator = dateOptionsGenerator;
         requestRepaint();
     }
 
     /**
      * @return the dateOptionsGenerator currently used
      */
     public DateOptionsGenerator getDateOptionsGenerator() {
         return dateOptionsGenerator;
     }
 
     /**
      * Set if the controls (next/prev month) be rendered.
      * 
      * @param renderControls
      */
     public void setRenderControls(boolean renderControls) {
         this.renderControls = renderControls;
         requestRepaint();
     }
 
     /**
      * Check if the controls are currently rendered.
      * 
      * @return the renderControls
      */
     public boolean isRenderControls() {
         return renderControls;
     }
 
     /**
      * Set if the header (current month + controls) should be rendered.
      * 
      * @param renderHeader
      *            the renderHeader to set
      */
     public void setRenderHeader(boolean renderHeader) {
         this.renderHeader = renderHeader;
         requestRepaint();
     }
 
     /**
      * Check if the header is currently rendered.
      * 
      * @return the renderHeader
      */
     public boolean isRenderHeader() {
         return renderHeader;
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see com.vaadin.ui.AbstractComponent#setLocale(java.util.Locale)
      */
     @Override
     public void setLocale(Locale locale) {
         super.setLocale(locale);
         requestRepaint();
     }
 
     /**
      * Set if week numbers should be rendered.
      * 
      * @param renderWeekNumbers
      */
     public void setRenderWeekNumbers(boolean renderWeekNumbers) {
         this.renderWeekNumbers = renderWeekNumbers;
         requestRepaint();
     }
 
     /**
      * Check if the week numbers are currently rendered.
      * 
      * @return the renderWeekNumbers
      */
     public boolean isRenderWeekNumbers() {
         return renderWeekNumbers;
     }
 
     /**
      * Render the previous month.
      */
     public void showPreviousMonth() {
         Calendar calendar = getCalendarInstance();
         calendar.setTime(getShowingDate());
         calendar.set(Calendar.DAY_OF_MONTH, 1);
         calendar.add(Calendar.MONTH, -1);
 
         setShowingDate(calendar.getTime());
     }
 
     /**
      * Render the next month.
      */
     public void showNextMonth() {
         Calendar calendar = getCalendarInstance();
         calendar.setTime(getShowingDate());
         calendar.set(Calendar.DAY_OF_MONTH, 1);
         calendar.add(Calendar.MONTH, 1);
 
         setShowingDate(calendar.getTime());
     }
 
     /**
      * Render the previous year.
      */
     public void showPreviousYear() {
         Calendar calendar = getCalendarInstance();
         calendar.setTime(getShowingDate());
         calendar.set(Calendar.DAY_OF_MONTH, 1);
         calendar.add(Calendar.YEAR, -1);
 
         setShowingDate(calendar.getTime());
     }
 
     /**
      * Render the next year.
      */
     public void showNextYear() {
         Calendar calendar = getCalendarInstance();
         calendar.setTime(getShowingDate());
         calendar.set(Calendar.DAY_OF_MONTH, 1);
         calendar.add(Calendar.YEAR, 1);
 
         setShowingDate(calendar.getTime());
     }
 
     /**
      * Set the month to be shown.
      * 
      * @param showingDate
      */
     public void setShowingDate(Date monthToShow) {
         showingDate = monthToShow;
         requestRepaint();
     }
 
     /**
      * @return the month currently shown
      */
     public Date getShowingDate() {
         return showingDate;
     }
 
     /**
      * Set the date range that the user can select. If either date is null,
      * selection to that direction is not limited.
      * 
      * @param start
      *            start of enabled dates, inclusive
      * @param end
      *            end of enabled dates, inclusive
      */
     public void setEnabledDateRange(Date start, Date end) {
         enabledStartDate = start;
         enabledEndDate = end;
         requestRepaint();
     }
 
     protected Calendar getCalendarInstance() {
         return Calendar.getInstance(getLocale());
     }
 
     protected Calendar getResetCalendarInstance(Date date) {
         Calendar calendar = getCalendarInstance();
         calendar.setTime(date);
         resetCalendarTimeFields(calendar);
         return calendar;
     }
 
     protected String getMonthCaption(Date date, int amount, boolean longCaption) {
         Calendar calendar = getCalendarInstance();
         calendar.setTime(date);
 
         calendar.roll(Calendar.MONTH, amount);
 
         int displayLength = longCaption ? Calendar.LONG : Calendar.SHORT;
 
         return calendar.getDisplayName(Calendar.MONTH, displayLength,
                 getLocale());
     }
 
     protected boolean dayEquals(Date day1, Date day2) {
         if ((day1 == null || day2 == null) && day1 != day2) {
             return false;
         }
 
         Calendar c1 = getCalendarInstance();
         c1.setTime(day1);
 
         Calendar c2 = getCalendarInstance();
         c2.setTime(day2);
 
         resetCalendarTimeFields(c1);
 
         resetCalendarTimeFields(c2);
 
         return c1.equals(c2);
     }
 
     protected boolean monthEquals(Date day1, Date day2) {
         if ((day1 == null || day2 == null) && day1 != day2) {
             return false;
         }
 
         Calendar c1 = getCalendarInstance();
         c1.setTime(day1);
 
         Calendar c2 = getCalendarInstance();
         c2.setTime(day2);
 
         int month1 = c1.get(Calendar.MONTH);
         int month2 = c2.get(Calendar.MONTH);
 
         return month1 == month2;
     }
 
    protected Date constructNewDateValue(int newDay) {
         Date showingDate = getShowingDate();
         Calendar calendar = getCalendarInstance();
         calendar.setTime(showingDate);
 
         calendar.set(Calendar.DAY_OF_MONTH, newDay);
 
         return calendar.getTime();
     }
 
    protected Calendar constructNewCalendarValue(int newDay) {
        Date showingDate = getShowingDate();
        Calendar calendar = getCalendarInstance();
        calendar.setTime(showingDate);

        calendar.set(Calendar.DAY_OF_MONTH, newDay);

        return calendar;
    }

     protected boolean isWeekend(Date date) {
         Calendar calendar = getCalendarInstance();
         calendar.setTime(date);
         int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
         return (dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY);
     }
 
     /**
      * @param clickedDay
      *            the day of month that was clicked
      * @return true if the day is disabled, false otherwise
      */
    protected boolean isDisabled(int clickedDayNro) {
        Calendar clickedDate = constructNewCalendarValue(clickedDayNro);
        int dayOfYearClicked = clickedDate.get(Calendar.DAY_OF_YEAR);
        return disabledRenderedDays.contains(dayOfYearClicked);
     }
 
     /**
      * @return true if it is allowed to advance the calendar to the next month
      */
     protected boolean isNextMonthAllowed() {
         return isRenderControls() && isRenderHeader() && nextMonthEnabled;
     }
 
     /**
      * @return true if it is allowed to go back to the next month
      */
     protected boolean isPrevMonthAllowed() {
         return isRenderControls() && isRenderHeader() && prevMonthEnabled;
     }
 
     /**
      * @param time
      * @param i
      * @return
      */
     protected boolean isDisabledMonth(Date date, int amount) {
         Calendar calendar = getCalendarInstance();
         calendar.setTime(date);
         calendar.add(Calendar.MONTH, amount);
         Date month = calendar.getTime();
 
         if (enabledStartDate != null) {
             if (month.before(enabledStartDate)
                     && !monthEquals(month, enabledStartDate)) {
                 return true;
             }
         }
 
         if (enabledEndDate != null) {
             if (month.after(enabledEndDate)
                     && !monthEquals(month, enabledEndDate)) {
                 return true;
             }
         }
 
         return false;
     }
 
     /**
      * @param time
      * @return
      */
     protected boolean isDisabledDate(Date date) {
         if (getDateOptionsGenerator().isDateDisabled(date, this)) {
             return true;
 
         } else {
             boolean isAfterStart = true;
             boolean isBeforeEnd = true;
 
             Calendar today = getResetCalendarInstance(date);
 
             if (enabledStartDate != null) {
                 Calendar enabledStart = getResetCalendarInstance(enabledStartDate);
                 isAfterStart = today.after(enabledStart)
                         || dayEquals(date, enabledStartDate);
             }
 
             if (enabledEndDate != null) {
                 Calendar enabledEnd = getResetCalendarInstance(enabledEndDate);
                 isBeforeEnd = today.before(enabledEnd)
                         || dayEquals(date, enabledEndDate);
             }
 
             // if the date is between the enabled range
             return !(isAfterStart && isBeforeEnd);
         }
     }
 
     protected void resetCalendarTimeFields(Calendar calendar) {
         calendar.set(Calendar.MILLISECOND, 0);
         calendar.set(Calendar.SECOND, 0);
         calendar.set(Calendar.MINUTE, 0);
         calendar.set(Calendar.HOUR, 0);
         calendar.set(Calendar.HOUR_OF_DAY, 0);
         calendar.set(Calendar.AM_PM, 0);
     }
 
     /**
      * Interface for for setting options for dates with the StyleCalendar.
      * 
      * @author Risto Yrjänä / IT Mill Ltd.
      * 
      */
     public interface DateOptionsGenerator {
 
         /**
          * This method is called on every date of the currently shown month.
          * 
          * @param date
          *            currently rendered date
          * @param context
          *            the calling StyleCalendar instance
          * @return the desired style name, or null
          */
         public String getStyleName(Date date, StyleCalendar context);
 
         /**
          * This method is called on every date of the currently shown month.
          * 
          * @param date
          *            currently rendered date
          * @param context
          *            the calling StyleCalendar instance
          * @return true if selecting this date should be disabled, false
          *         otherwise
          */
         public boolean isDateDisabled(Date date, StyleCalendar context);
     }
 }
