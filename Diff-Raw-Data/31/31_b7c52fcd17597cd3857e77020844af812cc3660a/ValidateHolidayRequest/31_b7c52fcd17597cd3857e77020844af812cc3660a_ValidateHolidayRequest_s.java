 import java.util.Calendar;
 import java.util.GregorianCalendar;
 import java.util.Date;
 import org.joda.time.DateTime;
 import org.joda.time.Days;
 
 public class ValidateHolidayRequest
 {
   private static String[]
     monthNoValue = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug","Sep", "Oct", "Nov", "Dec"};
     
     public static HolidayRequestExceptionMsg HREM 
                   = new HolidayRequestExceptionMsg();
   //Check if a given interval is possible to be a driver holiday
   //Written by: Oak. Last modified: 17/02/13
   public static boolean validateRequest
     (Date dateFrom, Date dateTo, int driverID)
   {
     //validate holiday lenght
     if(dateTo.getYear() > dateFrom.getYear()) //Over the year
     {
       System.out.println("OVER THE YEAR REQUEST");
       Date endOfThisYear
         = new Date(dateFrom.getYear(), 11, 31, 0, 0, 0);
       Date startOfNextYear 
         = new Date(dateTo.getYear(), 0, 1, 0, 0, 0);
      if(!validateHolidayLength(dateFrom, endOfThisYear, driverID))
       {
         System.out.println("The request is not valid: This year");
         return false;
       }//if
      else if(!validateHolidayLength(startOfNextYear, dateTo, driverID))
       {
         System.out.println("The request is not valid: Next year");
         return false;    
       }//else if
    }//if. over the year
    
    if(!validateHolidayLength(dateFrom, dateTo, driverID))
     {
       return false;
     }//with in one year
     
     //check if max driver at each DATE interval
     if(!checkDateInterval(dateFrom, dateTo))
     {
       return false;
     }
       
     System.out.println("The request is valid");
     return true;  
   }//validateRequest
   
   //For a given interval, check if each date in the interval has
   //been booked by less than 10 drivers. 
   //Written by: Oak. Last modified: 17/02/13
   public static boolean checkDateInterval(Date dateFrom, Date dateTo)
   {
     GregorianCalendar currentCal = new GregorianCalendar
       (dateFrom.getYear(), dateFrom.getMonth(), 
       dateFrom.getDate(), 0, 0, 0);
     
     //WARNING: dateFrom cannot be reused after the loop.
     Date currentDate = dateFrom;
     do
     {
       System.out.println("Checking: " + currentDate.getDate());
       if(!dateAvailable(currentDate)) 
       {
         return false;  
       }
       
       currentCal.add(Calendar.DATE, 1);
       currentDate.setDate(currentCal.get(Calendar.DATE));
       currentDate.setMonth(currentCal.get(Calendar.MONTH));
       currentDate.setYear(currentCal.get(Calendar.YEAR));
     }while(!currentDate.after(dateTo));//While not after
     return true;
   }//checkDateInterval
   
   //Check if a given date is possible to be a driver holiday
   //Written by: Oak. Last modified: 17/02/13
   public static boolean dateAvailable(Date givenDate)
   {
     int[] driverIDs = DriverInfo.getDrivers();
     int notAvailable = 0;
     for (int i=0; i<driverIDs.length; i++)
     {
       if(!(DriverInfo.isAvailable(driverIDs[i], givenDate)))
       {
         notAvailable++; 
       }
     }//for
     if(notAvailable > 9) 
     {
       HREM.dateExceptionMsg = ("Date: " + givenDate.getDate() + "/" +
                               givenDate.getMonth() + "/" + 
                               givenDate.getYear() + 
                               " is NOT available");
       return false;
     }//if
     else 
     {
       return true;
     }//else
   }//dateAvailable
   
   //Check if the requested holiday length is valid
   //Written by: Oak. Last modified: 17/02/13
   public static boolean validateHolidayLength
    (Date dateFrom, Date dateTo, int driverID)
   {
    int interval = calculateInterval(dateFrom, dateTo); 
    //How many days does the driver have left for his holiday
    int maxHoliday = 25 - DriverInfo.getHolidaysTaken(driverID);
     if(interval > maxHoliday)
     {
       HREM.lengthExceptionMsg = 
         ("You have requested " + interval + 
          (interval > 1? "days ": "day ") + "for " + dateTo.getYear() +
          ", while the maximum is " + maxHoliday + 
          (maxHoliday > 1? "days ": "day "));    
       return false;
     }
     return true;
   }//validateHolidayLength
   
   //Written by: Nathan. Last modified: 18/02/13
   public static DateTime dateToDateTime(Date date)
   {
       String dateString = date.toString();
       //System.out.println(dateString);
       String dateStringArray[] = dateString.split(" ", 6);
       //System.out.println(dateStringArray[1] + dateStringArray[2] + dateStringArray[5]);
       int newMonth = 0;
       for(int i = 0; i < monthNoValue.length; i++)
       {
           if(monthNoValue[i].equals(dateStringArray[1]))
           {
               newMonth = i + 1;
           }
       }
       int newDay = Integer.parseInt(dateStringArray[2]);
       int newYear = Integer.parseInt(dateStringArray[5]);
       newYear -= 1900;
       return new DateTime(newYear, newMonth, newDay, 0, 0);
   }
   
   //Written by: Nathan. Last modified: 18/02/13
   public static int calculateInterval(Date dateFrom, Date dateTo)
   {
       if(dateFrom.equals(dateTo))
       {
           return 1;
       }
       
       DateTime dateTimeFrom = dateToDateTime(dateFrom);
       DateTime dateTimeTo = dateToDateTime(dateTo);
       // return days between + 1 becasue want to include the dateFrom and dateTo
       // in the interval
       return Days.daysBetween(dateTimeFrom, dateTimeTo).getDays() + 1;
       
   }  
 }//class
