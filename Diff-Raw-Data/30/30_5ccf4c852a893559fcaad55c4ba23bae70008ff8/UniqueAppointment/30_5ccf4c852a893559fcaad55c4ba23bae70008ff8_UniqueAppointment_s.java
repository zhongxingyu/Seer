 package calendar;
 
 import java.sql.Date;
 import java.util.Calendar;
 
 /**
  * Wrapper class to represent Unique Appointments in a Selection, as
  * an Appointment only exists once for several days, while in the Calendar
  * it exists for every day it is displayed 
  * @author Florian Roks
  *
  */
 public class UniqueAppointment {
 	private Appointment appointment;
 	private Date date;
 	
 	/**
 	 * Constructs a UniqueAppointment appointment for the Date date
 	 * @param appointment
 	 * @param date
 	 */
 	public UniqueAppointment(Appointment appointment, Date date)
 	{
 		this.appointment = appointment;
 		this.date = date;
 	}
 	
 	/**
 	 * Returns the Date of the Appointment 
 	 * @return
 	 */
 	public Date getDate()
 	{
 		return date;
 	}
 	
 	/**
 	 * Returns the Appointment
 	 * @return
 	 */
 	public Appointment getAppointment()
 	{
 		return appointment;
 	}
 
     @Override
     public int hashCode()
     {
         Calendar cal1 = Calendar.getInstance();
         cal1.setTimeInMillis(this.date.getTime());
 
         return cal1.get(Calendar.DAY_OF_YEAR) ^ cal1.get(Calendar.YEAR) ^ this.appointment.hashCode();
     }
 
     @Override
 	public boolean equals(Object obj)
 	{
 		if(obj instanceof UniqueAppointment)
 		{
 			UniqueAppointment cmp = (UniqueAppointment)obj;
 			Calendar cal1 = Calendar.getInstance();
 			cal1.setTimeInMillis(this.getDate().getTime());
 			
 			Calendar cal2 = Calendar.getInstance();
 			cal2.setTimeInMillis(cmp.getDate().getTime());
 
 			if(cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
 				cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR))
 			{
				if(cmp.getAppointment().equals(this.getAppointment()))
 				{
 					return true;
 				}
 			}
 		}
 		
 		return false;
 	}
 }
