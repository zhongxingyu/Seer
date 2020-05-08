 package calendar.plaf;
 
 import java.io.IOException;
 import calendar.AppointmentCalendar;
 import calendar.CalendarModel;
 import calendar.Appointment;
 import calendar.CalendarSelectionEvent;
 import calendar.CalendarSelectionModel;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.wings.SComponent;
 import org.wings.io.Device;
 import org.wings.io.StringBuilderDevice;
 import org.wings.plaf.Update;
 import org.wings.plaf.css.AbstractComponentCG;
 import org.wings.plaf.css.AbstractUpdate;
 import org.wings.plaf.css.UpdateHandler;
 
 import java.text.DateFormat;
 import java.util.Calendar;
 import java.util.Collection;
 import java.sql.Date;
 import java.util.List;
 import org.wings.header.Header;
 import java.util.ArrayList;
 import org.wings.plaf.css.Utils;
 import org.wings.header.SessionHeaders;
 import java.awt.Color;
 import calendar.IAppointment;
 import calendar.CustomCellRenderer;
 
 /**
  * PLAF Code Generator for the Calendar Component 
  * @author Florian Roks
  *
  */
 public class CalendarCG extends AbstractComponentCG<AppointmentCalendar> {
 	private final Calendar tempCal = new java.util.GregorianCalendar();
 	private static final long serialVersionUID = -1466863715729057747L;
 	private final transient static Log LOG = LogFactory.getLog(CalendarCG.class);
 	
 	protected final static List<Header> headers;
 	static {
 		headers = new ArrayList<Header>();
 		headers.add(Utils.createExternalizedCSSHeader("calendar/css/calendar.css"));
 		headers.add(Utils.createExternalizedJSHeader("calendar/js/calendar.js"));
 		headers.add(Utils.createExternalizedJSHeaderFromProperty(Utils.JS_YUI_DOM));
 	}
 	
 	@Override
 	public void installCG(final AppointmentCalendar calendar) {
 		super.installCG(calendar);
 		
 		SessionHeaders.getInstance().registerHeaders(headers);
 	}
 	
 	Calendar today = Calendar.getInstance();
 	Calendar viewToday = Calendar.getInstance();
 	
 	/**
 	 * Sets the "Today-Date" for the View (The Date the view is constructed around)
 	 * This may be removed later 
 	 * @param todayDate
 	 */
 	public void setToday(java.util.Date todayDate)
 	{
 		this.viewToday.setTime(todayDate);
 	}
 	
 	private void writeHeader(final Device device, final AppointmentCalendar calendar) throws IOException
 	{
 		device.print("<table");
 		Utils.writeAllAttributes(device, calendar);
 		device.print("><tr><td>");
 	}
 	
 	private void writeFooter(final Device device, final AppointmentCalendar calendar) throws IOException
 	{
 		device.print("</td></tr></table>");  
 	}
 	
 	private void writeClickability(final Device device, final String dateOrApp, final AppointmentCalendar calendar) throws IOException
 	{
 		device.print(" onClick=\"");
 		Utils.quote(device, "javascript:AppCalendar.click" + dateOrApp + "(this, event, "+ "\"" + calendar.getName() + "\")", true, false, true);
 		device.print("\"");
 	}
 	
 	private void writeWeekInternal(final Device device, final AppointmentCalendar calendar) throws IOException
 	{
 		writeHeader(device, calendar);
 		
		tempCal.setTime(calendar.getCalendarModel().getVisibleFrom());

 		device.print("<table id=\"weekview\"><tr>");
 		while(tempCal.getTime().before(calendar.getCalendarModel().getVisibleUntil()))
 		{
 			CustomCellRenderer cellRenderer = calendar.getCalendarModel().getCustomCellRenderer();
 			if(cellRenderer != null)
 			{
 				cellRenderer.writeCell(device, tempCal, calendar);
 				continue;
 			}
 
 			device.print("<td class=\"" + this.getCellClassname(calendar, tempCal, null) + "\"");
 			
 			String uniqueID = tempCal.get(Calendar.YEAR) + ":" + tempCal.get(Calendar.DAY_OF_YEAR);
 			device.print(" id=\"");
 			Utils.quote(device, uniqueID, true, false, true);
 			device.print("\"");
 			
 			if((calendar.getSelectionModel().getSelectionMode()&CalendarSelectionModel.DATE_BITMASK) != 0) {
 				writeClickability(device, "Date", calendar);
 			}
 			
 			device.print(">");
 			
 			device.print("<div class=\"weekdaytitle\">");
 			device.print(tempCal.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, calendar.getLocale()));
 			DateFormat formatter = DateFormat.getDateInstance(DateFormat.SHORT, calendar.getLocale());
 			device.print(" ");
 			device.print(formatter.format(tempCal.getTime()));
 			device.print("</div>");
 
 			Collection<IAppointment> appointments = calendar.getCalendarModel().getAppointments(new Date(tempCal.getTimeInMillis()));
 
 			if(appointments == null)
 			{
 				device.print("</td>");
 				tempCal.add(Calendar.DAY_OF_YEAR, +1);
 				continue; 
 			}
 			
 			int i=0;
 			for(IAppointment appointment:appointments)
 			{
 				if(i > calendar.getCalendarModel().getMaxNumberAppointmentsPerCell())
 					break;
 				
 				this.writeAppointment(device, calendar, appointment, tempCal);
 				i++;
 			}
 			
 			device.print("</td>");
 			tempCal.add(Calendar.DAY_OF_YEAR, +1);
 		}
 		device.print("</tr></table>");
 		
 		writeFooter(device, calendar);
 	}
 	
 	private void writeDayInternal(final Device device, final AppointmentCalendar calendar) throws IOException
 	{
 		writeHeader(device, calendar);
 		
		tempCal.setTime(calendar.getCalendarModel().getDate());
 		
 		device.print("<table id=\"dayview\"><tr><td>");
 		device.print("<div class=\"daycontainer\">");
 
 		if(calendar.getCalendarModel().getCustomCellRenderer() != null)
 		{
 			calendar.getCalendarModel().getCustomCellRenderer().writeCell(device, tempCal, calendar);
 		}
 		else {
 			
 			DateFormat formatter = DateFormat.getDateInstance(DateFormat.LONG, calendar.getLocale());
 			
 			String dayTitle = tempCal.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, calendar.getCalendarModel().getLocale()) + ", " + formatter.format(calendar.getCalendarModel().getDate());
 			
 			device.print("<div class=\"daytitle\">" + dayTitle + "</div>");
 			device.print("<div class=\"dayappcontainer\"");
 			String uniqueID = tempCal.get(Calendar.YEAR) + ":" + tempCal.get(Calendar.DAY_OF_YEAR);
 			device.print(" id=\"");
 			Utils.quote(device, uniqueID, true, false, true);
 			device.print("\"");
 			if((calendar.getSelectionModel().getSelectionMode()&CalendarSelectionModel.DATE_BITMASK) != 0) {
 				writeClickability(device, "Date", calendar);
 			}
 			device.print(">");
 			
 			
 			Collection<IAppointment> appointments = calendar.getCalendarModel().getAppointments(new java.sql.Date(tempCal.getTimeInMillis()));
 			if(appointments != null)
 			{
 				int i = 0;
 				for(IAppointment appointment:appointments)
 				{
 					if(i >= calendar.getCalendarModel().getMaxNumberAppointmentsPerCell())
 						continue;
 					
 					writeAppointment(device, calendar, appointment, tempCal);
 					
 					i++;
 				}
 			}
 			device.print("</div>");
 
 		}
 		device.print("</div>");
 
 		device.print("</td></tr></table>");
 		
 		writeFooter(device, calendar);
 	}
 	
 	private String getCellClassname(final AppointmentCalendar calendar, final Calendar cellCal, final Calendar viewDate)
 	{
 		java.sql.Date sqlCellDate = new java.sql.Date(cellCal.getTimeInMillis());
 		switch(calendar.getCalendarModel().getView())
 		{
 			case MONTH:
 				if(calendar.getSelectionModel().isSelected(sqlCellDate))
 					return "selected";
 				
 				if(cellCal.get(Calendar.DAY_OF_YEAR) == viewDate.get(Calendar.DAY_OF_YEAR) &&
 					cellCal.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR))
 							return "today";
 					else
 						if(cellCal.get(Calendar.MONTH) != viewDate.get(Calendar.MONTH))
 							return "notthismonth";
 						else
 							return "";
 			case WEEK:
 				if(calendar.getSelectionModel().isSelected(sqlCellDate))
 					return "selected";
 				
 				if(cellCal.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
 					cellCal.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR))
 				{
 					return "container-today";
 				}
 				else
 				{
 					return "container";
 				}
 		}
 		
 		return "";
 	}
 	
 	private void writeMonthInternal(final Device device, final AppointmentCalendar calendar) throws IOException
 	{
 		CalendarModel model = calendar.getCalendarModel();
 		
		tempCal.setTime(model.getVisibleFrom());
 		
 		writeHeader(device, calendar);
 		
 		device.print("<table id=\"monthview\">");
 		device.print("<thead>");
 		
 		for(int column = 0; column < model.getColumnCount(); column++)
 		{
 			device.print("<th>" + tempCal.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, calendar.getLocale()) + "</th>");
 			tempCal.add(Calendar.DAY_OF_MONTH, 1);
 		}
 		tempCal.setTime(model.getVisibleFrom());
 		device.print("</thead><tbody>");
 		
 		for(int row = 0; row < model.getRowCount(); row++) 
 		{
 			device.print("<tr>");
 			for(int column = 0; column < model.getColumnCount(); column++)
 			{
 				if(model.getCustomCellRenderer() != null)
 				{
 					model.getCustomCellRenderer().writeCell(device, tempCal, calendar);
 					continue;
 				}
 				
 				device.print("<td class=\"" + this.getCellClassname(calendar, tempCal, viewToday) + "\"");
 
 				String uniqueID = tempCal.get(Calendar.YEAR) + ":" + tempCal.get(Calendar.DAY_OF_YEAR);
 				device.print(" id=\"");
 				Utils.quote(device, uniqueID, true, false, true);
 				device.print("\"");
 				
 				if((calendar.getSelectionModel().getSelectionMode()&CalendarSelectionModel.DATE_BITMASK) != 0) {
 					writeClickability(device, "Date", calendar);
 				}
 				
 				device.print(">");
 
 				device.print("<span class=\"dayofmonth\">" + tempCal.get(Calendar.DAY_OF_MONTH) + "</span><br />");
 				Collection<IAppointment> appointments = model.getAppointments(new java.sql.Date(tempCal.getTimeInMillis()));
 				if(appointments == null)
 				{
 					tempCal.add(Calendar.DAY_OF_MONTH, 1);
 					continue;
 				}
 
 				int i = 0;
 				for(IAppointment appointment:appointments)
 				{
 					if(i >= model.getMaxNumberAppointmentsPerCell())
 						continue;
 					
 					writeAppointment(device, calendar, appointment, tempCal);
 					
 					i++;
 				}
 
 				tempCal.add(Calendar.DAY_OF_MONTH, 1);
 				device.print("</td>");
 			}
 			device.print("</tr>");
 			tempCal.add(Calendar.DAY_OF_MONTH, (7 % model.getColumnCount()));
 		}
 		device.print("</tbody>");
 		device.print("</table>");
 		
 		writeFooter(device, calendar);
 	}
 	
 	private String getCSSColorAttributes(Color foregroundColor, Color backgroundColor)
 	{
 		return "color:"+String.format("#%02X%02X%02X", foregroundColor.getRed(), foregroundColor.getGreen(), foregroundColor.getBlue()) + "; background-color:" + String.format("#%02X%02X%02X", backgroundColor.getRed(), backgroundColor.getGreen(), backgroundColor.getBlue()) + ";";
 	}
 	
 	@Override
 	public void writeInternal(final Device device, final AppointmentCalendar calendar) throws IOException {
 		
 		CalendarModel model = calendar.getCalendarModel();
 		assert model.getVisibleFrom().before(model.getVisibleUntil());
 		
 		switch(model.getView())
 		{
 			case NONE:
 				device.print("no model chosen");
 			break;
 			case MONTH:
 				writeMonthInternal(device, calendar);
 			break;
 			case WEEK:
 				writeWeekInternal(device, calendar);
 			break;
 			case DAY:
 				writeDayInternal(device, calendar);
 			break;
 		}
 		
 		writePopup(device, calendar, null);
 	}
 	
 	/**
 	 * Writes the given appointment (in calendar) for "today" to device
 	 * This is used by the DefaultCellRenderer and for SelectionUpdates
 	 * @param device
 	 * @param calendar
 	 * @param appointment
 	 * @param today
 	 * @throws IOException
 	 */
 	public void writeAppointment(final Device device, final AppointmentCalendar calendar, final IAppointment appointment, final Calendar today) throws IOException
 	{	
 		java.sql.Date sqlDate = new Date(today.getTimeInMillis());
 		String uniqueID = calendar.getCalendarModel().getUniqueAppointmentID(sqlDate, appointment);
 		
 		switch(calendar.getCalendarModel().getView())
 		{
 			case MONTH:
 			{
 				if(!calendar.getSelectionModel().isSelected(appointment, sqlDate))
 				{
 					device.print("<div class=\"event\"");
 					
 					if(appointment.getForegroundColor() != null && appointment.getBackgroundColor() != null)
 					{
 						device.print(" style=\"" + getCSSColorAttributes(appointment.getForegroundColor(), appointment.getBackgroundColor()) + "\"");
 					}
 				}
 				else
 				{
 					device.print("<div class=\"selected\"");
 				}
 				
 				device.print(" id=\"");
 				Utils.quote(device, uniqueID, true, false, true);
 				device.print("\"");
 				
 				device.print(" onmouseover=\"");
 
 				Utils.quote(device, "javascript:AppCalendar.loadPopup(this, event, \"" + calendar.getName() + "\")", true, false, true); 
 				device.print("\"");
 				device.print(" onmouseout=\"javascript:AppCalendar.hidePopup(this)\"");
 				
 				if( (calendar.getSelectionModel().getSelectionMode()&CalendarSelectionModel.APPOINTMENT_BITMASK) != 0) {
 					writeClickability(device, "Appointment", calendar);
 				}
 				device.print(">");
 				
 				if(appointment.getAppointmentType() == IAppointment.AppointmentType.NORMAL)
 				{
 					String startTime = DateFormat.getTimeInstance(DateFormat.SHORT).format(appointment.getAppointmentStartDate());
 					device.print(startTime + " ");
 				}
 				
 				device.print(appointment.getAppointmentName());
 				device.print("</div>");
 			}
 			break;
 			case DAY:
 			{
 				Date start = appointment.getAppointmentStartDate();
 				Date end = appointment.getAppointmentEndDate();
 
 				if(!calendar.getSelectionModel().isSelected(appointment, sqlDate))
 				{
 					if(appointment.getAppointmentType() == IAppointment.AppointmentType.NORMAL)
 					{
 						if(appointment.isAppointmentRecurring())
 							device.print("<div class=\"recurringappointment\"");
 						else
 							device.print("<div class=\"normalappointment\"");
 					}
 					else
 					{
 						device.print("<div class=\"alldayappointment\""); 
 					}
 	
 					if(appointment.getForegroundColor() != null && appointment.getBackgroundColor() != null)
 						device.print(" style=\"" + getCSSColorAttributes(appointment.getForegroundColor(), appointment.getBackgroundColor()) + "\""); 
 				}
 				else
 				{
 					device.print("<div class=\"selected\"");
 				}
 				
 				device.print(" id=\"");
 				Utils.quote(device, uniqueID, true, false, true);
 				device.print("\"");
 				 
 				device.print(" onmouseover=\"");
 
 				Utils.quote(device, "javascript:AppCalendar.loadPopup(this, event, \"" + calendar.getName() + "\")", true, false, true); 
 				device.print("\"");
 				device.print(" onmouseout=\"javascript:AppCalendar.hidePopup(this)\"");
 				
 				if( (calendar.getSelectionModel().getSelectionMode()&CalendarSelectionModel.APPOINTMENT_BITMASK) != 0) {
 					writeClickability(device, "Appointment", calendar);
 				}
 						
 				device.print(">");
 
 				if(appointment.getAppointmentType() == IAppointment.AppointmentType.NORMAL)
 				{
 					DateFormat timeFormatter = DateFormat.getTimeInstance(DateFormat.SHORT, calendar.getLocale());
 					device.print("<div class=\"time\">" + timeFormatter.format(start) + " - " + timeFormatter.format(end) + "</div>");
 				}
 				else
 				{
 					device.print("<div class=\"time\">" + Appointment.getAppointmentTypeString(appointment.getAppointmentType(), calendar.getLocale()) + "</div>");
 				}
 				device.print("<div class=\"name\">" + appointment.getAppointmentName() +  "</div>");
 				
 				DateFormat dateFormatter = DateFormat.getDateInstance(DateFormat.SHORT, calendar.getLocale());
 				
 				Calendar cal1 = Calendar.getInstance();
 				cal1.setTime(appointment.getAppointmentStartDate());
 				Calendar cal2 = Calendar.getInstance();
 				cal1.setTime(appointment.getAppointmentEndDate());
 
 				if(cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR) && cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR))
 					device.print("<div class=\"date\">" + dateFormatter.format(appointment.getAppointmentStartDate()) + "</div>");
 				else
 					device.print("<div class=\"date\">" + dateFormatter.format(appointment.getAppointmentStartDate()) + " - " + dateFormatter.format(appointment.getAppointmentEndDate()) + "</div>");
 				
 				if(appointment.getAppointmentDescription() != null && appointment.getAppointmentDescription().length() > 0)
 					device.print("<div class=\"description\">" + appointment.getAppointmentDescription() +  "</div>");
 				
 				if(appointment.isAppointmentRecurring())
 					device.print("<div class=\"recurringweekdays\">" + Appointment.getAppointmentRecurringDaysString(appointment.getAppointmentRecurringDays(), calendar.getLocale()) +  "</div>");
 
 				if(appointment.getAdditionalAppointmentInformation() != null && appointment.getAdditionalAppointmentInformation().length() > 0)
 					device.print("<div class=\"additional\">" + appointment.getAdditionalAppointmentInformation() +  "</div>");
 
 				device.print("</div>");
 			}
 			break;
 			case WEEK:
 			{
 				Date start = appointment.getAppointmentStartDate();
 				Date end = appointment.getAppointmentEndDate();
 				
 				if(!calendar.getSelectionModel().isSelected(appointment, sqlDate))
 				{
 					if(appointment.getAppointmentType() == IAppointment.AppointmentType.NORMAL)
 					{
 						if(appointment.isAppointmentRecurring())
 							device.print("<div class=\"recurringappointment\"");
 						else
 							device.print("<div class=\"normalappointment\"");
 					}
 					else
 					{
 						device.print("<div class=\"alldayappointment\""); 
 					}
 	
 					if(appointment.getForegroundColor() != null && appointment.getBackgroundColor() != null)
 						device.print(" style=\"" + getCSSColorAttributes(appointment.getForegroundColor(), appointment.getBackgroundColor()) + "\""); 
 				}
 				else
 				{
 					device.print("<div class=\"selected\"");
 				}
 				
 				device.print(" id=\"");
 				Utils.quote(device, uniqueID, true, false, true);
 				device.print("\"");
 				
 				device.print(" onmouseover=\"");
 
 				Utils.quote(device, "javascript:AppCalendar.loadPopup(this, event, \"" + calendar.getName() + "\")", true, false, true); 
 				device.print("\"");
 				device.print(" onmouseout=\"javascript:AppCalendar.hidePopup(this)\"");
 				
 				if( (calendar.getSelectionModel().getSelectionMode()&CalendarSelectionModel.APPOINTMENT_BITMASK) != 0) {
 					writeClickability(device, "Appointment", calendar);
 				}
 						
 				device.print(">");
 
 				if(appointment.getAppointmentType() == IAppointment.AppointmentType.NORMAL)
 				{
 					DateFormat timeFormatter = DateFormat.getTimeInstance(DateFormat.SHORT, calendar.getLocale());
 					device.print("<div class=\"time\">" + timeFormatter.format(start) + " - " + timeFormatter.format(end) + "</div>");
 				}
 				else
 				{
 					device.print("<div class=\"time\">" + Appointment.getAppointmentTypeString(appointment.getAppointmentType(), calendar.getLocale()) + "</div>");
 				}
 				device.print("<div class=\"name\">" + appointment.getAppointmentName() +  "</div>");
 				
 				DateFormat dateFormatter = DateFormat.getDateInstance(DateFormat.SHORT, calendar.getLocale());
 				
 				Calendar cal1 = Calendar.getInstance();
 				cal1.setTime(appointment.getAppointmentStartDate());
 				Calendar cal2 = Calendar.getInstance();
 				cal1.setTime(appointment.getAppointmentEndDate());
 
 				if(cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR) && cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR))
 					device.print("<div class=\"date\">" + dateFormatter.format(appointment.getAppointmentStartDate()) + "</div>");
 				else
 					device.print("<div class=\"date\">" + dateFormatter.format(appointment.getAppointmentStartDate()) + " - " + dateFormatter.format(appointment.getAppointmentEndDate()) + "</div>");
 				
 				if(appointment.getAppointmentDescription() != null && appointment.getAppointmentDescription().length() > 0)
 					device.print("<div class=\"description\">" + appointment.getAppointmentDescription() +  "</div>");
 				
 				if(appointment.isAppointmentRecurring())
 					device.print("<div class=\"recurringweekdays\">" + Appointment.getAppointmentRecurringDaysString(appointment.getAppointmentRecurringDays(), calendar.getLocale()) +  "</div>");
 
 				if(appointment.getAdditionalAppointmentInformation() != null && appointment.getAdditionalAppointmentInformation().length() > 0)
 					device.print("<div class=\"additional\">" + appointment.getAdditionalAppointmentInformation() +  "</div>");
 
 				device.print("</div>");
 			}
 			break;
 		}
 	}
 	
 	/**
 	 * Writes a Popup for appointment to device
 	 * @param device
 	 * @param calendar
 	 * @param appointment
 	 * @throws IOException
 	 */
 	public void writePopup(final Device device, final AppointmentCalendar calendar, final Appointment appointment) throws IOException
 	{
 		device.print("<div id=\"" + calendar.getName() + "-Popup\">");
 		writePopupInnerHTML(device, calendar, appointment);
 		device.print("</div>");
 	} 
 
 	/**
 	 * Writes the inner HTML of a popup to device
 	 * This is used by the PopupUpdate (AJAX update get)
 	 * @param device
 	 * @param calendar
 	 * @param appointment
 	 * @throws IOException
 	 */
 	public void writePopupInnerHTML(final Device device, final AppointmentCalendar calendar, final IAppointment appointment) throws IOException
 	{
 		// on the first call (to render the initial div, appointment will be null
 		if(appointment == null)
 		{
 			device.print("<span>...</span>");
 			return;
 		}
 		
 		device.print("<span class=\"name\">" + appointment.getAppointmentName() + "</span><br />");
 		if(appointment.getAppointmentDescription() != null && appointment.getAppointmentDescription().length() > 0)
 			device.print("<span class=\"description\">" + appointment.getAppointmentDescription() + "</span><br />");
 
 		DateFormat format = DateFormat.getDateInstance(DateFormat.LONG, calendar.getLocale());
 
 		Calendar cal1 = Calendar.getInstance();
 		cal1.setTime(appointment.getAppointmentStartDate());
 		Calendar cal2 = Calendar.getInstance();
 		cal2.setTime(appointment.getAppointmentEndDate());
 
 		if(cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR) && 
 					cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR))
 			device.print("<div id=\"startenddate\">" + format.format(appointment.getAppointmentStartDate()) + "</div>");
 		else
 			device.print("<div id=\"startenddate\">" + format.format(appointment.getAppointmentStartDate()) + " - " + format.format(appointment.getAppointmentEndDate()) + "</div>");
 			
 		if(appointment.getAppointmentType() == IAppointment.AppointmentType.NORMAL)
 		{
 			DateFormat formatTime = DateFormat.getTimeInstance(DateFormat.SHORT, calendar.getLocale());
 
 			device.print("<span class=\"timeframe\">" + formatTime.format(appointment.getAppointmentStartDate()) + 
 							"-" + formatTime.format(appointment.getAppointmentEndDate()) +
 							"</span><br />");
 		}
 		else // if the appointment ain't normal, it must be ALLDAY => timeframe is useless
 			device.print("<span class=\"appointmenttype\">" + Appointment.getAppointmentTypeString(appointment.getAppointmentType(), calendar.getLocale()) + "</span><br />");
 		
 		if(appointment.isAppointmentRecurring())
 			device.print("<span class=\"recurringdays\">" + Appointment.getAppointmentRecurringDaysString(appointment.getAppointmentRecurringDays(), calendar.getLocale()) + "</span><br />");
 	
 		String additionalInformation = appointment.getAdditionalAppointmentInformation();
 		if(additionalInformation != null && additionalInformation.length() > 0)
 			device.print("<span class=\"additional\">" + additionalInformation + "</span><br />");
 	}
 
 	/**
 	 * Gets an Selection Update for the Updates caused by event 
 	 * @param calendar
 	 * @param selectionModel
 	 * @param event
 	 * @return
 	 */
 	public Update getSelectionUpdate(AppointmentCalendar calendar, CalendarSelectionModel selectionModel, CalendarSelectionEvent event)
 	{
 		return new SelectionUpdate(calendar, selectionModel, event);
 	}
 	
 	/**
 	 * Gets a Popup update for the given uniqueAppointmentID
 	 * @param calendar
 	 * @param uniqueAppointmentID
 	 * @return
 	 */
 	public Update getPopupUpdate(AppointmentCalendar calendar, String uniqueAppointmentID)
 	{
 		String[] data = uniqueAppointmentID.split(":");
 		// data[0] = Year, data[1] = day of year, data[2] appointment number of the day (starting with 0)
 		// this is NOT safe, as the input from browser could be manipulated and lead to a exception 
 		Calendar cal = Calendar.getInstance();
 		cal.set(Calendar.YEAR, Integer.parseInt(data[0]));
 		cal.set(Calendar.DAY_OF_YEAR, Integer.parseInt(data[1]));
 		
 		Collection<IAppointment> appointments = calendar.getCalendarModel().getAppointments(new Date(cal.getTimeInMillis()));
 		if(appointments != null)
 		{
 			Object appointmentArray[] = appointments.toArray();
 			return new AppointmentPopupUpdate(calendar, (IAppointment)appointmentArray[Integer.parseInt(data[2])]);
 		}
 		
 		return null;
 	}
 	
 	/**
 	 * Constructs a SelectionUpdate
 	 * @author Florian Roks
 	 *
 	 */
 	@SuppressWarnings("unchecked")
 	public class SelectionUpdate extends AbstractUpdate {
 		CalendarSelectionModel model;
 		CalendarSelectionEvent event;
 		AppointmentCalendar calendar;
 		
 		/**
 		 * Constructs a SelectionUpdate for the given Model and event
 		 * @param component
 		 * @param model
 		 * @param event
 		 */
 		@SuppressWarnings("unchecked")
 		public SelectionUpdate(final SComponent component, final CalendarSelectionModel model, final CalendarSelectionEvent event) {
 			super(component);
 			
 			this.model = model;
 			this.event = event;
 			this.calendar = (AppointmentCalendar)component;
 		}
 
 		@Override
 		public int hashCode()
 		{
 			return super.hashCode() ^ this.getHandler().hashCode() ^ this.getHandler().getParameters().hashCode(); 
 		}
 		
 		@Override
 		public boolean equals(Object obj)
 		{
 			if(obj == this)
 				return true;
 			if(obj == null || obj.getClass() != this.getClass())
 				return false;
 			
 			if(obj instanceof Update)
 			{
 				Update other = (Update)obj;
 				if(!this.getComponent().equals(other.getComponent()))
 					return false;
 				if(this.getProperty() != other.getProperty())
 					return false;
 				if(this.getPriority() != other.getPriority())
 					return false;
 				if(!this.getHandler().equals(other.getHandler()))
 					return false;
 			}
 			else
 			{
 				return false;
 			}
 			return false;
 		}
 		
 		@Override
 		public Handler getHandler() {
 			Calendar cal = Calendar.getInstance();
 			cal.setTime(event.getDate());
 			switch(event.getAffectedComponent())
 			{
 				case DATE:
 					String elementID = cal.get(Calendar.YEAR) + ":" + cal.get(Calendar.DAY_OF_YEAR);
 					Calendar viewCal = Calendar.getInstance();
 					viewCal.setTime(this.calendar.getDate());
 					UpdateHandler chandler = new UpdateHandler("className");
 					chandler.addParameter(elementID);
 					chandler.addParameter(((CalendarCG)calendar.getCG()).getCellClassname(this.calendar, cal, viewCal));
 					return chandler;
 					
 				case APPOINTMENT:
 					UpdateHandler handler = new UpdateHandler("component");
 					// add the component id
 					String uniqueAppointmentID = calendar.getCalendarModel().getUniqueAppointmentID(event.getDate(), event.getAppointment());
 					if(uniqueAppointmentID == null)
 					{
 						LOG.info("invalid appointment was sent: date:" + event.getDate().toLocaleString() + " app: "+event.getAppointment());
 						return null;
 					}
 					handler.addParameter(uniqueAppointmentID);
 					String htmlCode = "";
 					String exception = null;
 					try {
 						StringBuilderDevice htmlDevice = new StringBuilderDevice(1024);
 						((CalendarCG)component.getCG()).writeAppointment(htmlDevice, this.calendar, event.getAppointment(), cal);
 						htmlCode = htmlDevice.toString();
 						handler.addParameter(htmlCode);
 					} catch(Throwable t) {
 						exception = t.getClass().getName();
 					}
 					if(exception != null)
 						handler.addParameter(exception);
 					return handler;
 			}
 			
 			LOG.fatal("this should never happen!"); 
 			return null;
 		}
 	}
 	
 	/**
 	 * AppointmentPopupUpdate - Update to be sent when a Popup is requested
 	 * @author Florian Roks
 	 *
 	 */
 	public class AppointmentPopupUpdate extends AbstractUpdate {
 		String htmlCode = "";
 		String exception = null;
 		IAppointment appointment;
 		
 		/**
 		 * Constructs a Popup Update which is to be sent after a popup request 
 		 * @param component
 		 * @param appointment
 		 */
 		@SuppressWarnings("unchecked")
 		public AppointmentPopupUpdate(SComponent component, IAppointment appointment) {
 			super(component);
 			
 			this.appointment = appointment;
 		}
 
 		@Override
 		public Handler getHandler() {
 			UpdateHandler handler = new UpdateHandler("text");
 			String htmlCode = "";
 			
 			try
 			{
 				StringBuilderDevice htmlDevice = new StringBuilderDevice(1024);
 				((CalendarCG)component.getCG()).writePopupInnerHTML(htmlDevice, (AppointmentCalendar)component, this.appointment);
 				htmlCode = htmlDevice.toString();
 			}
 			catch(Throwable t)
 			{
 				LOG.fatal("An error occured during rendering of AppointmentCalendar-Popup");
 				exception = t.getClass().getName();
 			}
 			handler.addParameter(component.getName() + "-Popup");
 			handler.addParameter(htmlCode);
 			
 			if(exception != null) {
 				handler.addParameter(exception);
 			}
 			
 			return handler;
 		}
 		
 	}
 	
 }
