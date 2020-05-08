 package calendar;
 
 /*import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;*/
 import org.wings.LowLevelEventListener;
 import org.wings.SComponent;
 
 import calendar.plaf.NewCalendarCG;
 
 import java.sql.Date;
 import java.util.*;
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.awt.datatransfer.Transferable;
 import java.awt.datatransfer.DataFlavor;
 import org.wings.plaf.Update;
 import org.wings.*;
 import org.wings.event.SMouseEvent;
 import org.wings.sdnd.DefaultTransferable;
 import org.wings.sdnd.CustomDragHandler;
 import org.wings.sdnd.SDropMode;
 
 
 /**
  * Calendar Component - always starts in the MONTH view
  * @author Holger Engels, Florian Roks
  *
  */
 public class AppointmentCalendar extends SComponent implements LowLevelEventListener {
 	private static final long serialVersionUID = 2537490018780756796L;
 	//private static final Log LOG = LogFactory.getLog(CalendarExample.class);
 
     // TODO: default.properties when moving to wingx
     private static final NewCalendarCG calCG = new NewCalendarCG();
 
     private CalendarModel model;
     private CalendarSelectionModel selectionModel;
 
     private ModifierKeyStatus modifierKeyStatus = new ModifierKeyStatus();
 
     /**
      * Forwards Selection Events to the CG
      */
     private CalendarSelectionListener fwdSelectionEvents = new CalendarSelectionListener() {
 		public void valueChanged(CalendarSelectionEvent e) {
             if(!AppointmentCalendar.this.getCalendarModel().isVisible(e.getDate()))
                 return;
 
             if(isUpdatePossible() && AppointmentCalendar.class.isAssignableFrom(AppointmentCalendar.this.getClass()))
 			{
                 Update update = ((NewCalendarCG)getCG()).getSelectionUpdate(AppointmentCalendar.this, selectionModel, e);
                 if(update == null)
                 {
                     System.out.println("selection update is null!");
                     return;
                 }
                 update(update);
 			}
 			else
 			{
 				reload();
 			}
 		}
     };
 
     private PropertyChangeListener fwdSelectionModeChange = new PropertyChangeListener() {
 		public void propertyChange(PropertyChangeEvent evt) {
 			// reload the page to remove/add the onClick event handlers to the calendar
 			// and/or change the selections (i.e. if the user switches from multiple appointments to single)
 			if(evt.getPropertyName().equals("selectionMode")) {
 				reload();
 			}
 		}
 
     };
 
     private CalendarViewChangeListener fwdCalendarViewEvents = new CalendarViewChangeListener() {
 		public void valueChanged(CalendarViewChangeEvent e) {
 			switch(e.getType())
 			{
 				case DATE:
 					reload();
 				break;
 				case APPOINTMENT:
 					reload();
 				break;
 				case VIEW:
                     CalendarSelectionModel model = AppointmentCalendar.this.getSelectionModel();
                     if(model != null) {
                         Date lastSelected = model.getLastSelectedDate();
                         if(lastSelected == null) {
                             UniqueAppointment app = model.getLastSelectedAppointment();
                             if(app != null)
                                 lastSelected = app.getDate();
                         }
                         if(lastSelected != null)
                             AppointmentCalendar.this.setDate(lastSelected);
                     }
                     reload();
 				break;
 			}
 		}
     };
 
     private PropertyChangeListener fwdPropChangeCalModel = new PropertyChangeListener() {
 
 		public void propertyChange(PropertyChangeEvent evt) {
 			// Reload the component if the locale changes
             if(evt.getPropertyName().equals("locale")) {
 				reload();
 			}
             if(evt.getPropertyName().equals("mergeWeekends")) {
                 reload();
             }
             if(evt.getPropertyName().equals("preAppointmentsChange")) {
                 getSelectionModel().clearAppointmentSelection();
             }
         }
 
     };
 
     /**
      * Constructs the Calendar component using the current (Server) Time/Date for the current Day/Month/Year and the Servers locale with the DefaultCalendarModel
      */
 	public AppointmentCalendar() {
 		this(Locale.getDefault());
 	}
 
 	/**
 	 * Constructs the Calendar component using the current (Server) Time/Date for the current Day/Month/Year with the DefaultCalendarModel
 	 * @param locale Locale
 	 * @param model CalendarModel to use
 	 */
 	public AppointmentCalendar(Locale locale, CalendarModel model) {
 		this(new Date(Calendar.getInstance().getTimeInMillis()), locale, model);
 	}
 
     /**
      * Constructs the Calendar component using the current (Server) Time/Date for the current Day/Month/Year and given Locale with the DefaultCalendarModel
      * @param locale Locale to create the calendar with
      */
 	public AppointmentCalendar(Locale locale) {
 		this(Calendar.getInstance().getTime(), locale);
 	}
 
 
 	/**
 	 * Constructs the Calendar component using the given date and locale with the DefaultCalendarModel
 	 * @param date Date to build the calendar around
 	 * @param locale Locale for the calendar instance
 	 */
 	public AppointmentCalendar(java.util.Date date, Locale locale)
 	{
 		this(new Date(date.getTime()), locale);
 	}
 
 	/**
 	 * Constructs the Calendar component using the given <code>java.sql.Date</code>
 	 * @param date <code>Date</code> to be used for the Calendar
 	 * @param locale Locale to be used for the calendar instance
 	 */
 	public AppointmentCalendar(Date date, Locale locale)
 	{
 		this(date, locale, null);
 
 	}
 
 	/**
 	 * Constructs the Calendar component using the given parameters
 	 * @param date
 	 * @param locale
 	 * @param model
 	 */
 	public AppointmentCalendar(Date date, Locale locale, CalendarModel model)
 	{
 		super();
 
         // TODO: default.properties when moving to wingx
         this.setCG(calCG);
 
 		if(model == null)
 			setCalendarModel(new DefaultCalendarModel());
 		else
 			setCalendarModel(model);
 
         setStyle("AppointmentCalendar");
 		setSelectionModel(new DefaultCalendarSelectionModel(this));
 		setDate(date);
         setLocale(locale);
 
         getSession().getDispatcher().register(this);
 
         installTransferHandler();
 		//getSelectionModel().setSelectionMode(CalendarSelectionModel.SINGLE_EXCLUSIVE_DATE_OR_APPOINTMENT_SELECTION);
 		//getSelectionModel().setSelectionMode(CalendarSelectionModel.MULTIPLE_APPOINTMENT_SELECTION | CalendarSelectionModel.MULTIPLE_DATE_SELECTION);
 	}
 
 	/**
 	 * Sets the CalendarModel - please note that all previously added CalendarViewChangeListener have to reregister
 	 * @param model
 	 */
 	public void setCalendarModel(CalendarModel model)
 	{
 		if(model == null) {
 			throw new IllegalArgumentException("model must be not null");
         }
 
         CalendarModel oldVal = this.model;
 
 		if(oldVal == null) {
 			this.model = model;
 			this.model.addPropertyChangeListener(fwdPropChangeCalModel);
 			this.model.addCalendarViewChangeListener(fwdCalendarViewEvents);
 		} else {
 			this.model = model;
 			oldVal.removePropertyChangeListener(fwdPropChangeCalModel);
 			oldVal.removeCalendarViewChangeListener(fwdCalendarViewEvents);
 			this.model.addPropertyChangeListener(fwdPropChangeCalModel);
 			this.model.addCalendarViewChangeListener(fwdCalendarViewEvents);
 		}
 
 		propertyChangeSupport.firePropertyChange("model", oldVal, model);
 	}
 
 	/**
 	 * Sets the Date this calendar is build around
 	 * @param date Sets the date this calendar is build around
 	 */
 	public void setDate(java.util.Date date) {
 		// looks bad, but gets the job done
 		setDate(new Date(date.getTime()));
 	}
 
 	/**
 	 * Sets the Date this calendar is build around
 	 * @param date Sets the date this calendar is build around
 	 */
 	public void setDate(Date date) {
 		getCalendarModel().setDate(date);
 	}
 
 	/**
 	 * Gets the Date this Calendar is build around
 	 * @return Returns the Date this Calendar is build around
 	 */
 	public Date getDate() {
 		return getCalendarModel().getDate();
 	}
 
 	/**
 	 * Returns the currently used <code>CalendarModel</code> for this instance of Calendar
 	 * @return The currently used <code>CalendarModel</code>
 	 */
 	public CalendarModel getCalendarModel() {
 		return model;
 	}
 
 	/**
 	 * Sets the Locale of the Calendar
 	 * @param locale Locale to be set
 	 */
 	public void setLocale(Locale locale)
 	{
 		getCalendarModel().setLocale(locale);
 	}
 
 	/**
 	 * Returns the locale of the Calendar
 	 * @return
 	 */
 	public Locale getLocale()
 	{
 		return getCalendarModel().getLocale();
 	}
 
 	/**
 	 * Returns the SelectionModel of this calendar
 	 * @return SelectionModel of this calendar
 	 */
 	public CalendarSelectionModel getSelectionModel()
 	{
 		return this.selectionModel;
 	}
 
 	/**
 	 * Sets the SelectionMOdel of this calendar
 	 * @param selectionModel
 	 */
 	public void setSelectionModel(CalendarSelectionModel selectionModel)
 	{
 		if(selectionModel == null) {
 			throw new IllegalArgumentException("selectionModel must be non null");
 		}
 
 		CalendarSelectionModel oldVal = this.selectionModel;
 		this.selectionModel = selectionModel;
 
 		if(oldVal == null) {
 			selectionModel.addCalendarSelectionListener(fwdSelectionEvents);
 			selectionModel.addPropertyChangeListener(this.fwdSelectionModeChange);
 		}
 
 		if(oldVal != null && oldVal != selectionModel) {
 			oldVal.removePropertyChangeListener(this.fwdSelectionModeChange);
 			oldVal.removeCalendarSelectionListener(fwdSelectionEvents);
 			selectionModel.addCalendarSelectionListener(fwdSelectionEvents);
 			selectionModel.addPropertyChangeListener(this.fwdSelectionModeChange);
 		}
 
 		propertyChangeSupport.firePropertyChange("selectionModel", oldVal, this.selectionModel);
 	}
 
 	/**
 	 * Adds a CalendarViewChangeListener
 	 * @param listener Listener to be added
 	 */
 	public void addCalendarViewChangeListener(CalendarViewChangeListener listener)
 	{
 		getCalendarModel().addCalendarViewChangeListener(listener);
 	}
 
 	/**
 	 * Removes a CalendarViewChangeListener
 	 * @param listener Listener to be removed
 	 */
 	public void removeCalendarViewChangeListener(CalendarViewChangeListener listener)
 	{
 		getCalendarModel().removeCalendarViewChangeListener(listener);
 	}
 
 
 	/**
 	 * Adds a selection Change Listener to the Component
 	 * @param listener The SelectionChangeListener to be added
 	 */
 	public void addCalendarSelectionListener(CalendarSelectionListener listener)
 	{
 		getSelectionModel().addCalendarSelectionListener(listener);
 	}
 
 	/**
 	 * Remove a selection Change Listener from the Component
 	 * @param listener The SelectionChangeListener to be removed
 	 */
 	public void removeCalendarSelectionChangeListener(CalendarSelectionListener listener)
 	{
 		getSelectionModel().removeCalendarSelectionListener(listener);
 	}
 
 	@Override
 	public void processLowLevelEvent(String componentName, String[] values)
 	{
 		String[] processedValues;
 
 		if(values[0].contains(";"))
 			processedValues = values[0].split(";");
 		else
 			processedValues = values;
 
 		SForm.addArmedComponent(this);
 
 		for(int i=0; i<processedValues.length; i++)
 		{
 			String value = processedValues[i];
 			if(value.length() < 2)
 				continue;
 
 			getSelectionModel().setDelayEvents(true);
 
             if(value.startsWith("q:")) {
 				// q: means update the popup window for more information
                 Update update = ((NewCalendarCG)this.getCG()).getPopupUpdate(this, value.substring(2).split("_")[1]);
 				if(update != null && isUpdatePossible())
 					update(update);
 			} else if(value.startsWith("ctrlKey")) {
 				modifierKeyStatus.ctrlKey = Boolean.parseBoolean(value.split("=")[1]);
 			} else if(value.startsWith("shiftKey")) {
 				modifierKeyStatus.shiftKey = Boolean.parseBoolean(value.split("=")[1]);
 			} else if(value.startsWith("altKey")) {
 				modifierKeyStatus.altKey = Boolean.parseBoolean(value.split("=")[1]);
 			} else if(value.startsWith("d:")) {
 				Calendar cal = Calendar.getInstance();
                 String[] data = value.substring(2).split("_")[1].split(":");
                 cal.set(Calendar.YEAR, Integer.parseInt(data[0]));
 				cal.set(Calendar.DAY_OF_YEAR, Integer.parseInt(data[1]));
                 if(data.length > 2) {
                     cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(data[2]));
                     cal.set(Calendar.MINUTE, 0);
                     cal.set(Calendar.SECOND, 0);
                     cal.set(Calendar.MILLISECOND, 0);
                 }
 				this.getSelectionModel().clickDate(new java.sql.Date(cal.getTimeInMillis()), modifierKeyStatus);
 			} else if(value.startsWith("a:")) {
 				Calendar cal = Calendar.getInstance();
 				String[] data = value.substring(2).split("_")[1].split(":");
 				cal.set(Calendar.YEAR, Integer.parseInt(data[0]));
 				cal.set(Calendar.DAY_OF_YEAR, Integer.parseInt(data[1]));
 				Appointment appointment = getCalendarModel().getAppointmentFromID(value.split("_")[1]);
 
 				this.getSelectionModel().clickAppointment(appointment, new Date(cal.getTimeInMillis()), modifierKeyStatus);
 			} else if(value.startsWith("da:")) {
 				Calendar cal = Calendar.getInstance();
 				String[] data = value.substring(2).split("_")[1].split(":");
 				cal.set(Calendar.YEAR, Integer.parseInt(data[0]));
 				cal.set(Calendar.DAY_OF_YEAR, Integer.parseInt(data[1]));
 				Appointment appointment = getCalendarModel().getAppointmentFromID(value.split("_")[1]);
 
 				this.getSelectionModel().doubleClickAppointment(appointment, new Date(cal.getTimeInMillis()), modifierKeyStatus);
 			} else if(value.startsWith("dd:")) {
                 Calendar cal = Calendar.getInstance();
                 String[] data = value.substring(2).split("_")[1].split(":");
                 cal.set(Calendar.YEAR, Integer.parseInt(data[0]));
                 cal.set(Calendar.DAY_OF_YEAR, Integer.parseInt(data[1]));
                 if(data.length > 2) {
                     cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(data[2]));
                     cal.set(Calendar.MINUTE, 0);
                     cal.set(Calendar.SECOND, 0);
                     cal.set(Calendar.MILLISECOND, 0);
                 }
 
                 this.getSelectionModel().doubleClickDate(new java.sql.Date(cal.getTimeInMillis()), modifierKeyStatus);
 			}
 		}
 
 		modifierKeyStatus.reset();
 	}
 
 	public void fireFinalEvents() {
 		super.fireFinalEvents();
 	}
 
 	public void fireIntermediateEvents() {
 		SForm.addArmedComponent(this);
 
 		// all state changes are done, fire the events to get updates
 		getSelectionModel().fireDelayedFinalEvents();
 		getSelectionModel().setDelayEvents(false);
 	}
 
 	public boolean isEpochCheckEnabled() {
 		return false;
 	}
 
     /**
      * Drag and Drop stuff
      */
     private SDropMode dropMode = null;
     private boolean dragEnabled = false;
 
 
 
     protected static final class DropLocation extends STransferHandler.DropLocation {
         private Date date = null;
 
         public DropLocation(AppointmentCalendar calendar, SPoint point) {
             super(point);
 
             if(point == null || point.getCoordinates() == null)
                 return;
             
             String dateString = point.getCoordinates();
             if(!dateString.matches("[0-9]+:[0-9]+")) // java matches with /^$/, so this is fine
                 return;
 
             Calendar cal = Calendar.getInstance();
             String[] dateParts = dateString.split(":");
 
             cal.set(Calendar.YEAR, Integer.parseInt(dateParts[0]));
             cal.set(Calendar.DAY_OF_YEAR, Integer.parseInt(dateParts[1]));
             cal.set(Calendar.HOUR_OF_DAY, 0);
             cal.set(Calendar.MINUTE, 0);
             cal.set(Calendar.SECOND, 0);
             cal.set(Calendar.MILLISECOND, 0);
             
             this.date = new java.sql.Date(cal.getTimeInMillis());
         }
 
         public java.sql.Date getDate() {
             return date;
         }
     }
 
     public void setDropMode(SDropMode dropMode) {
         this.dropMode = dropMode;
 
         getSession().getSDragAndDropManager().addDropTarget(this, "calendar");
     }
 
     public SDropMode getDropMode() {
         return this.dropMode;
     }
 
     protected DropLocation dropLocationForPoint(SPoint p) {
         return new AppointmentCalendar.DropLocation(this, p);
     }
 
     private void installTransferHandler() {
         if(getTransferHandler() == null) {
             setTransferHandler(new DefaultTransferHandler());
         }
     }
 
     public void setDragEnabled(boolean dragEnabled) {
         if(getSelectionModel() == null && dragEnabled == true)
             throw new IllegalStateException("Unable to enable DND - no selection mode set in " + this);
 
         if(dragEnabled != this.dragEnabled) {
             if(dragEnabled) {
                 this.getSession().getSDragAndDropManager().addDragSource(this, "calendar");
             } else {
                 this.getSession().getSDragAndDropManager().removeDragSource(this);
             }
 
             this.dragEnabled = dragEnabled;
         }
     }
 
     protected static class AppointmentTransferable extends DefaultTransferable {
         private static DataFlavor[] flavors;
         private Collection collection;
 
         static {
             try
             {
                 flavors = new DataFlavor[] {
                     new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType + "; class=java.util.Collection")
                 };
             } catch(ClassNotFoundException e) {
             }
         }
 
         public AppointmentTransferable(Collection appointments) {
             super(flavors);
 
             this.collection = appointments;
         }
 
         protected Object getDataForClass(DataFlavor dataFlavor, Class<?> aClass) {
             if(dataFlavor.getPrimaryType().equals("application")) {
                 // application/x-java-jvm-local-objectref
                 if(dataFlavor.getMimeType().startsWith(DataFlavor.javaJVMLocalObjectMimeType)) {
                     return collection;
                 }
             }
             
             return null;
         }
     }
 
     protected static class DefaultTransferHandler extends STransferHandler implements CustomDragHandler {
         public DefaultTransferHandler() {
             super(null);
         }
 
         protected Transferable createTransferable(SComponent component) {
             if(component instanceof AppointmentCalendar) {
                 AppointmentCalendar calendar = (AppointmentCalendar)component;
                 return new AppointmentTransferable(calendar.getSelectionModel().getSelectedAppointments());
             }
             
             return null;
         }
 
         public int getSourceActions(SComponent component) {
             return COPY;
         }
 
         public boolean dragStart(SComponent source, SComponent target, int action, SMouseEvent event) {
             String[] coords = event.getPoint().getCoordinates().split(":");
             try {
                 if(coords.length < 5)
                     return false;
 
                 boolean ctrlKey = false;
                 boolean shiftKey = false;
                 for(int i=0; i<coords.length; ++i) {
                     String[] keyVal = coords[i].split("=");
                     if("ctrlKey".equals(keyVal[0])) {
                         ctrlKey = Boolean.parseBoolean(keyVal[1]);
                     } else if("shiftKey".equals(keyVal[0])) {
                         shiftKey = Boolean.parseBoolean(keyVal[1]);
                     }
                 }
 
                 if(source instanceof AppointmentCalendar) {
                     AppointmentCalendar calendar = (AppointmentCalendar)source;
                     ModifierKeyStatus keyStatus = new ModifierKeyStatus();
                     keyStatus.ctrlKey = ctrlKey;
                     keyStatus.shiftKey = shiftKey;
                     Appointment appointment = calendar.getCalendarModel().getAppointmentFromID(coords[2].substring(coords[2].indexOf("_")+1) + ":" + coords[3] + ":" + coords[4]);
                     Calendar cal = Calendar.getInstance();
                     cal.set(Calendar.YEAR, Integer.parseInt(coords[2].substring(coords[2].indexOf("_")+1)));
                     cal.set(Calendar.DAY_OF_YEAR, Integer.parseInt(coords[3]));
                     Date date = new Date(cal.getTimeInMillis());
                     calendar.getSelectionModel().clickAppointment(appointment, date, keyStatus);
                 }
             } catch(Exception e) {
                 e.printStackTrace();
             }
             return false;
         }
     }
 }
