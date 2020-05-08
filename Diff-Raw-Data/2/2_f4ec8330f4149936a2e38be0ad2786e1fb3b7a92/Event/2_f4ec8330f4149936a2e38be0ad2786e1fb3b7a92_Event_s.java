 package edu.nrao.dss.client.util.dssgwtcal;
 
 import java.util.ArrayList;
 import java.util.Date;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.i18n.client.DateTimeFormat;
 
 // an event represents an 'appointment' in the real world, and can map to one or more appointments.
 // appointments map to blocks of time on our calendar.
 
 public class Event {
 	public String title;
 	public String description;
 	public Date start;
 	public Date end;
 	public boolean selected;
 	private ArrayList<Appointment> appointments = new ArrayList<Appointment>();
 	private long msInDay = 24 * 60 * 60 * 1000;
 	// Nov 1 - March 8, 2009
 	//private long msGmtOffset = 5 * 60 * 60 * 1000;
 	
     private static final DateTimeFormat DATE_FORMAT = DateTimeFormat.getFormat("yyyy-MM-dd HH:mm:ss");
     
 	public Event(String title, String description, Date start, Date end) {
 		this.title = title;
 		this.description = description;
 		this.start = start;
 		this.end = getSafeEndDate(end); //don't end at midnight, but 1 min. before
 		createAppointments();
 	}
 	
 	private long getGmtOffsetMs(Date dt) {
 		long offset;
 		// TODO: how to get this to work forever?
 	    // 5 hours between Nov 1 2009, March 14, 2010, otherwise, 4 hours
 		Date nov1_2009  = DATE_FORMAT.parse("2009-11-01 00:00:00");
 		Date mar14_2010 = DATE_FORMAT.parse("2010-03-14 00:00:00");
 		if ((dt.before(nov1_2009)) || (dt.after(mar14_2010))) {
 			offset = 4;
 		} else {
 			offset = 5;
 		}
 	    return offset * 60 * 60 * 1000; // milliseconds
 	}
 	
 	private long getGmtOffsetMs(long day) {
 		Date dt = new Date(day * msInDay);
 		return getGmtOffsetMs(dt);
 	}
 	
 	private long getDayOffset(Date dt) {
 		return (dt.getTime() - getGmtOffsetMs(dt)) % msInDay;
 	}
 	
 	// avoid wrap-around when a time block ends on midnight
 	private Date getSafeEndDate(Date end) {
 		if (getDayOffset(end) == 0) {
 			return new Date(end.getTime() - (60 * 1000)); // loose a minute! 
 		} else {
 			return end;
 		}
 	}
 	
 	// Date -> GMT day number
 	private long getDay(Date dt) {
 		long time = dt.getTime();
 		return (time - getGmtOffsetMs(dt)) / msInDay;
 	}
 	
 	// GMT day number -> Date
 	private Date getDayDate(long day) {
 		long gmtDay = (msInDay * day) + getGmtOffsetMs(day);
 		return new Date(gmtDay);
 	}
 	
 
 
 	// GMT day number -> last few seconds of that day as Date
 	private Date getEndDayDate(long day) {
 		// the next day is from day + i, so get that, then subtract a few seconds
		long gmtDay = (msInDay * (day + 1)) + getGmtOffsetMs(day);
 		long msOffset = 60 * 1000; // 1 min. 
 		return new Date(gmtDay - msOffset);
 		
 	}
 	private int getDaySpan() {
         long dayStart = getDay(start);
         long dayEnd   = getDay(end);
         return (int) (dayEnd - dayStart);
         
 	}
 	private void createAppointments() {
 		// map this single event to one or more appointments according to whether
 		// or not the event spans a GMT day.
 		long dayStart = getDay(start);
 //		long dayEnd   = getDay(end);
 		int daySpan = getDaySpan();
 		Date apptStart;
 		Date apptEnd;
  		for (int i = 0; i <= daySpan; i++) {
 		   // first?	
 		   if (i == 0) {
 			   apptStart = new Date(start.getTime());
 		   } else {
 			   // all the next appointments start at the start of the day
 			   apptStart = getDayDate(dayStart + i);
 		   }
 		   // last?
 		   if (i == daySpan) {
 			   apptEnd = new Date(end.getTime());
 		   } else {
 			   // all continuing appointments start at the end of the day
 			   apptEnd = getEndDayDate(dayStart + i);
 		   }
 //		   String strStart = DateTimeFormat.getFormat("yyyy-MM-dd HH:mm:SS").format(apptStart);
 //		   String strEnd   = DateTimeFormat.getFormat("yyyy-MM-dd HH:mm:SS").format(apptEnd);
 
 		   // our Event becomes one or more of their Appointments
 		   Appointment appt = new Appointment();
 		   appt.setStart(apptStart);
 		   appt.setEnd(apptEnd);
 		   
 		   // TODO: format tittle and description better
 		   appt.setTitle(title); //title + " : " + Integer.toString(i));
 		   String desc = description;
 		   if (daySpan > 0) {
 			   desc = desc + " (Day " + Integer.toString(i + 1) + ")";
 		   }
 		   appt.setDescription(desc);
 		   
 		   // TODO: scores -> colors?
 		   appt.addStyleName("gwt-appointment-blue");
 		   
 		   appointments.add(appt);
 		}
 		 
 	}
 	public ArrayList<Appointment> getAppointments() {
 		return appointments;
 	}
 }
