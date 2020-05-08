 package calendar;
 
 import java.beans.PropertyChangeListener;
 import java.beans.PropertyChangeSupport;
 import java.sql.Date;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Collection;
 
 /*
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 */
 
 /**
  * Default Calendar Selection Model
  * Supports basic selection of Appointments and Dates with variable parameters
  */
 public class DefaultCalendarSelectionModel implements CalendarSelectionModel {
 	PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);
 	ArrayList<CalendarSelectionListener> selectionListener = new ArrayList<CalendarSelectionListener>();
 
 //	private static final Log LOG = LogFactory.getLog(DefaultCalendarSelectionModel.class);
 	private int selectionMode = 0;
 	private Collection<UniqueAppointment> selectedAppointments = new ArrayList<UniqueAppointment>();
 	private Collection<Date> selectedDates = new ArrayList<Date>();
 	private UniqueAppointment lastSelectedAppointment;
 	private Date lastSelectedDate;
 	private boolean delayEvents = false;
 	protected final ArrayList<CalendarSelectionEvent> delayedEvents = new ArrayList<CalendarSelectionEvent>();
 	private AppointmentCalendar calendar;
 
     /**
      * Constructs the DefaultCalendarSelectionModel
      * @param calendar the Calendar for which this DefaultCalendarSelectionModel is used
      */
     public DefaultCalendarSelectionModel(AppointmentCalendar calendar)
     {
         this.calendar = calendar;
     }
 
 	public void clearAppointmentSelection() {
 		ArrayList<UniqueAppointment> appointments = new ArrayList<UniqueAppointment>();
 		appointments.addAll(selectedAppointments);
 
 		for(UniqueAppointment appointment:appointments)
 		{
 			selectedAppointments.remove(appointment);
 
 			fireSelectionChangeEvent(new CalendarSelectionEvent(this, CalendarSelectionEvent.SelectionType.REMOVED, appointment.getAppointment(), appointment.getDate()));
 		}
 
 		lastSelectedAppointment = null;
 	}
 
 	public void clearDateSelection() {
 		ArrayList<Date> dates = new ArrayList<Date>();
 		dates.addAll(selectedDates);
 
 		for(Date date:dates)
 		{
 			selectedDates.remove(date);
 
 			fireSelectionChangeEvent(new CalendarSelectionEvent(this, CalendarSelectionEvent.SelectionType.REMOVED, date));
 		}
 
 		lastSelectedDate = null;
 	}
 
 	public UniqueAppointment getLastSelectedAppointment() {
 		return lastSelectedAppointment;
 	}
 
 	public Date getLastSelectedDate() {
 		return lastSelectedDate;
 	}
 
 	public Collection<Date> getSelectedDates() {
 		return selectedDates;
 	}
 
 	public Collection<UniqueAppointment> getSelectedAppointments() {
 		return selectedAppointments;
 	}
 
 	public int getDateSelectionCount() {
 		return selectedDates.size();
 	}
 
 	public int getSelectionMode() {
 		return this.selectionMode;
 	}
 
 	public void setSelectionMode(int selectionMode) {
 		int oldVal = this.selectionMode;
 		this.selectionMode = selectionMode;
 
 		// appointment selection set to none - clear all appointment selections
 		if((selectionMode & CalendarSelectionModel.APPOINTMENT_BITMASK) == 0) {
 			clearAppointmentSelection();
 		}
 
 		// date selection set to none - clear all date selections
 		if((selectionMode & CalendarSelectionModel.DATE_BITMASK) == 0) {
 			clearDateSelection();
 		}
 
 		// if you switch to single appointment selection and more than one appointment
 		// is selected, deselect all except the last selected appointment
 		if( ((selectionMode & CalendarSelectionModel.SINGLE_APPOINTMENT_SELECTION) == CalendarSelectionModel.SINGLE_APPOINTMENT_SELECTION) &&
 			getAppointmentSelectionCount() > 1) {
 				UniqueAppointment lastSelApp = getLastSelectedAppointment();
 				clearAppointmentSelection();
 				addSelection(lastSelApp);
 		}
 
 		// if you switch to single date selection and more than one date
 		// is selected, deselect all except the last selected date
 		if( ((selectionMode & CalendarSelectionModel.SINGLE_DATE_SELECTION) == CalendarSelectionModel.SINGLE_DATE_SELECTION) &&
 			getDateSelectionCount() > 1) {
 				Date lastSelDate = getLastSelectedDate();
 				clearDateSelection();
 				addSelection(lastSelDate);
 		}
 
 		propertyChangeSupport.firePropertyChange("selectionMode", oldVal, selectionMode);
 	}
 
 	private void addSelection(UniqueAppointment appointment) {
 		selectedAppointments.add(appointment);
 
 		fireSelectionChangeEvent(new CalendarSelectionEvent(this, CalendarSelectionEvent.SelectionType.ADDED, appointment.getAppointment(), appointment.getDate()));
 	}
 
 	public void addSelection(Appointment appointment, Date date) {
 		if((selectionMode & CalendarSelectionModel.DESELECT_DATE_ON_APPOINTMENT_SELECTION) == CalendarSelectionModel.DESELECT_DATE_ON_APPOINTMENT_SELECTION)
 		{
 			clearDateSelection();
 		}
 
 		if((selectionMode & CalendarSelectionModel.SINGLE_APPOINTMENT_SELECTION) == CalendarSelectionModel.SINGLE_APPOINTMENT_SELECTION)
 		{
 			clearAppointmentSelection();
 
 
 			UniqueAppointment uniqueAppointment = new UniqueAppointment(appointment, date);
 			lastSelectedAppointment = uniqueAppointment;
 			addSelection(uniqueAppointment);
 
 			return;
 		}
 
 		if((selectionMode & CalendarSelectionModel.MULTIPLE_APPOINTMENT_SELECTION) == CalendarSelectionModel.MULTIPLE_APPOINTMENT_SELECTION)
 		{
 			UniqueAppointment uniqueAppointment = new UniqueAppointment(appointment, date);
 
 			lastSelectedAppointment = uniqueAppointment;
 			addSelection(uniqueAppointment);
 
             return;
         }
 	}
 
 	private void addSelectionFireEvent(Date date)
 	{
 		lastSelectedDate = date;
 		selectedDates.add(date);
 
 		fireSelectionChangeEvent(new CalendarSelectionEvent(this, CalendarSelectionEvent.SelectionType.ADDED, date));
 	}
 
 	public void addSelection(Date date) {
 		if((selectionMode & CalendarSelectionModel.DESELECT_APPOINTMENT_ON_DATE_SELECTION) == CalendarSelectionModel.DESELECT_APPOINTMENT_ON_DATE_SELECTION)
 		{
 			clearAppointmentSelection();
 		}
 
 		if((selectionMode & CalendarSelectionModel.SINGLE_DATE_SELECTION) == CalendarSelectionModel.SINGLE_DATE_SELECTION)
 		{
 			clearDateSelection();
 
 			addSelectionFireEvent(date);
 			return;
 		}
 
 		if((selectionMode & CalendarSelectionModel.MULTIPLE_DATE_SELECTION) == CalendarSelectionModel.MULTIPLE_DATE_SELECTION)
 		{
 			addSelectionFireEvent(date);
 
             return;
 		}
 
 	}
 
 	private void addSelectionCheckModifierKeys(Appointment appointment, Date date, ModifierKeyStatus keyStatus)
 	{
 		if((selectionMode & CalendarSelectionModel.MULTIPLE_APPOINTMENT_SELECTION) == CalendarSelectionModel.MULTIPLE_APPOINTMENT_SELECTION)
 		{
             if(keyStatus.ctrlKey && isSelected(appointment, date)) {
                 removeSelection(appointment, date);
                 return;
             }
 
             if(!keyStatus.ctrlKey)
                 clearAppointmentSelection();
         }
 
 		addSelection(appointment, date);
 	}
 
     public void addSelectionInterval(Date from, Date until) {
         if(until.getTime() < from.getTime()) {
             addSelectionInterval(until, from);
             return;
         }
         
         Calendar cal1 = Calendar.getInstance();
         cal1.setTimeInMillis(from.getTime());
         Calendar cal2 = (Calendar)cal1.clone();
         cal2.setTimeInMillis(until.getTime());
 
         while(cal1.before(cal2)) {
             addSelection(new Date(cal1.getTimeInMillis()));
             cal1.add(Calendar.DAY_OF_YEAR, +1);
         }
     }
     
     Date lastSelectedDateShift = null;
 
     private void addSelectionCheckModifierKeys(Date date, ModifierKeyStatus keyStatus)
 	{
 		if((selectionMode & CalendarSelectionModel.MULTIPLE_DATE_SELECTION) == CalendarSelectionModel.MULTIPLE_DATE_SELECTION)
 		{
             if(keyStatus.ctrlKey && !keyStatus.shiftKey && isSelected(date)) {
                 removeSelection(date);
                 return;
             }
 
             if(!keyStatus.ctrlKey && !keyStatus.shiftKey)
                 clearDateSelection();
 
             if(!keyStatus.ctrlKey && keyStatus.shiftKey && lastSelectedDateShift != null && date != null) {
                 clearDateSelection();
                 addSelectionInterval(lastSelectedDateShift, date);
                 return;
             }
         }
 
 		addSelection(date);
         lastSelectedDateShift = date;
     }
 
 	public int getAppointmentSelectionCount() {
 		return selectedAppointments.size();
 	}
 
 	public void removeSelection(Appointment appointment, Date date) {
         selectedAppointments.remove(new UniqueAppointment(appointment, date));
 
         fireSelectionChangeEvent(new CalendarSelectionEvent(this, CalendarSelectionEvent.SelectionType.REMOVED, appointment, date));
 	}
 
 	public void removeSelection(Date date) {
 		removeAllDatesOnDayOfDate(date);
 
 		fireSelectionChangeEvent(new CalendarSelectionEvent(this, CalendarSelectionEvent.SelectionType.REMOVED, date));
 	}
 
 	public void addCalendarSelectionListener(CalendarSelectionListener listener) {
 		selectionListener.add(listener);
 	}
 
 	public void addPropertyChangeListener(PropertyChangeListener listener) {
 		if(listener == null) {
 			throw new IllegalArgumentException("listener must not be null");
 		}
 		propertyChangeSupport.addPropertyChangeListener(listener);
 	}
 
 	public void removePropertyChangeListener(PropertyChangeListener listener) {
 		if(propertyChangeSupport != null)
 			propertyChangeSupport.removePropertyChangeListener(listener);
 	}
 
 	public void removeCalendarSelectionListener(CalendarSelectionListener listener) {
 		selectionListener.remove(listener);
 	}
 
 	private void fireSelectionChangeEvent(CalendarSelectionEvent e) {
 		if(delayEvents) {
 			delayedEvents.add(e);
 			return;
 		}
 
 		for(CalendarSelectionListener listener:selectionListener)
 		{
             if(e.getAffectedComponent() == CalendarSelectionEvent.SelectionComponent.APPOINTMENT) // check if an appointment was removed from the list before firing a selection remove event
                 if(e.getType() == CalendarSelectionEvent.SelectionType.REMOVED)
                     if(!calendar.getCalendarModel().getAppointments(e.getDate()).contains(e.getAppointment()))
                         continue;
 			listener.valueChanged(e);
 		}
 	}
 
 	public void clickAppointment(Appointment appointment, Date date, ModifierKeyStatus keyStatus) {
         addSelectionCheckModifierKeys(appointment, date, keyStatus);
 	}
 
 	public void clickDate(Date date, ModifierKeyStatus keyStatus) {
         addSelectionCheckModifierKeys(date, keyStatus);
 	}
 
     public void doubleClickAppointment(Appointment appointment, Date date, ModifierKeyStatus keyStatus) {
     }
 
     public void doubleClickDate(Date date, ModifierKeyStatus keyStatus) {
     }
 
     private void removeAllDatesOnDayOfDate(Date date)
 	{
 		Calendar cal1 = Calendar.getInstance();
 		Calendar cal2 = Calendar.getInstance();
 		cal2.setTime(date);
 
 		Collection<Date> selectedDatesClone = new ArrayList<Date>();
 		selectedDatesClone.addAll(selectedDates);
 
 		for(Date checkDate:selectedDatesClone)
 		{
 			cal1.setTime(checkDate);
 			if(cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
 					cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR))
 			{
 				selectedDates.remove(checkDate);
 
 				fireSelectionChangeEvent(new CalendarSelectionEvent(this, CalendarSelectionEvent.SelectionType.REMOVED, checkDate));
 			}
 		}
 	}
 
 	public boolean isSelected(Appointment appointment, Date date) {
         return selectedAppointments.contains(new UniqueAppointment(appointment, date));
     }
 
 	public boolean isSelected(Date date) {
 		Calendar cal1 = Calendar.getInstance();
 		cal1.setTime(date);
 
 		Calendar cal2 = Calendar.getInstance();
 		for(Date cmpDate:selectedDates)
 		{
 			cal2.setTime(cmpDate);
 			if(cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
 				cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR))
 			{
 				return true;
 			}
 		}
 
 		return false;
 	}
 
 	public void fireDelayedFinalEvents() {
 		for(CalendarSelectionEvent e:delayedEvents)
 		{
             for(CalendarSelectionListener l:selectionListener)
 			{
                 if(e.getAffectedComponent() == CalendarSelectionEvent.SelectionComponent.APPOINTMENT) // check if an appointment was removed from the list before firing a selection remove event
                    if(e.getType() == CalendarSelectionEvent.SelectionType.REMOVED)
                        if(!calendar.getCalendarModel().getAppointments(e.getDate()).contains(e.getAppointment()))
                             continue;
                 l.valueChanged(e);
 			}
 		}
 
 		delayedEvents.clear();
 	}
 
 	public void fireDelayedIntermediateEvents() {
 	}
 
 	public boolean getDelayEvents() {
 		return delayEvents;
 	}
 
 	public void setDelayEvents(boolean arg0) {
 		delayEvents = arg0;
 	}
 }
