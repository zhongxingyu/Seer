 /*
  * Provider.java
  * Author: Nick Grauel
  * Group: Nick Grauel, Tyler Janowski, Nick DeRossi
  * This class represents a provider that provides services for an event.
  */
 
 import java.util.*;
 
 
 public class Provider {
     //--------------------Instance Variables------------------------
     protected String name;              					//Provider name.
     protected GregorianCalendar open;   					//Time that the provider opens.
     protected GregorianCalendar close;  					//Time that the provider closes.
     protected int capacity;             					//Provider capacity.
     protected String location;
     protected String service;
     protected ArrayList<GregorianCalendar> datesReserved;	//Dates the provider is in use.
     
     //Constructor
     public Provider(String n, String serv, String locale, GregorianCalendar o, GregorianCalendar c,
             int cap)
     {
         name = n;
         location = locale;
         service = serv;
         open = o;
         close = c;
         capacity = cap;
         datesReserved = new ArrayList<GregorianCalendar>();
     }
     
     //Returns the list of dates the provider is reserved.
     public ArrayList<GregorianCalendar> getDatesReserved() { return datesReserved; }
 
     //Adds a pair of dates (start/end date) to the list of dates reserved.
     public void addReservedDate(GregorianCalendar start, GregorianCalendar end)
     {
     	datesReserved.add(start);
     	datesReserved.add(end);
     }
     
     /*Returns true if there is a conflict between a given date and the dates the
      *provider is reserved.
      */
     public boolean isConflict(GregorianCalendar start, GregorianCalendar end)
     {
     	boolean conflicting = false;
     	Date sDate = start.getTime();
     	Date eDate = end.getTime();
     	for(int x = 0; x < datesReserved.size(); x += 2)
     	{
    		Date s = datesRes.get(x).getTime();
    		Date e = dateRes.get(x+1).getTime();
     		if(sDate.compareTo(s) < 0 && eDate.compareTo(s) > 0)
                 conflicting = true;
             if(sDate.compareTo(e) < 0 && eDate.compareTo(e) > 0)
                 conflicting = true;
             if(sDate.compareTo(s) < 0 && eDate.compareTo(e) > 0)
                 conflicting = true;
             if(sDate.compareTo(s) > 0 && eDate.compareTo(e) < 0)
                 conflicting = true;
     	}
     	return conflicting;
     }
     
     //Returns the provider name.
     public String getName() { return name; }
 
     //Returns the service type of the provider
     public String getService() { return service; }
 
     //Returns the location of the provider
     public String getLocation() { return location; }
     
     //Returns the opening time for the provider.
     public GregorianCalendar getOpenTime() { return open; }
     
     //Returns the closing time for the provider.
     public GregorianCalendar getClosingTime() { return close; }
     
     //Returns the maximum capacity for the provider.
     public int getCapacity() { return capacity; }
     
     //Returns string representation of the provider.
     public String toString()
     {
         String s = "";
         s += "Name: " + name + ", Place: " + location 
                 + ", Service: " + service + ", Open: "  
                 + open.get(Calendar.HOUR_OF_DAY) + ":" 
                 + open.get(Calendar.MINUTE) + ", Close: " 
                 + close.get(Calendar.HOUR_OF_DAY) + ":" 
                 + close.get(Calendar.MINUTE);
         return s;
     }
 }
