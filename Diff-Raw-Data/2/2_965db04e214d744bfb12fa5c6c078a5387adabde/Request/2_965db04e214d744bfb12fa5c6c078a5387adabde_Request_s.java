 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.concurrent.TimeUnit;
 
 /**
  * @author Alexandru Grigoroi
  *
  * Instantiatable Class which represents information about Requests.
  * It also allows the application to get the starting and ending
  * dates of a particular request as well as to get and set the status
  * of a particular request.<br><br>
  * 
  * As well as an ID, requests have numbers which are traditionally
  * used to identify them. <br><br>
  * 
  * The methods contain checks for invalid request, which will result in
  * Exceptions being thrown, and also enforces some "business
  * rules" such as checking for dates in the past.
  *
  */
 public class Request {
 	
 	private static final int MINIMUM_DRIVERS_AVAILABLE = 10;
 	private static String[] statusMessage = new String[]{"Awaiting Approval", "Approved", "Rejected"};
 	private static SimpleDateFormat parser = new SimpleDateFormat("dd/MM/yyyy"); //Used to parse Date strings
 	
 	private Boolean exists = false;	
 	private int id;
 	private Date startDate;
 	private Date endDate;
 	private int status;		//can take values of 0, 1 or 2. Each value corresponds to a different status message
 	private Driver driver;		
 	
 	public Request(int id)
 	{
 		try
 		{
 			this.id = id;
 			this.startDate = RequestInfo.getStartDate(id);
 			this.endDate = RequestInfo.getEndDate(id);
 			this.status = RequestInfo.getStatus(id);
 			Integer driver_id = RequestInfo.getDriver(id);
 			this.driver = new Driver(driver_id.toString());
 			this.exists = true;
 		}
 		catch(Exception ex)
 		{
 			this.exists = false;
 		}
 	}
 	
 	public Request(Date startDate, Date endDate, Driver driver) throws Exception
 	{
 		if(startDate.after(endDate))
 			throw new Exception("End date is before start date");
 		this.startDate = startDate;
 		this.endDate = endDate;
 		this.driver = driver;
 		if(driver.getHolidaysLeft() < getLength())
 			throw new Exception("Holiday duration is too big");
 		Date date = new Date(startDate.getTime());
		Driver[] drivers = Driver.getDrivers();
 		while(date.compareTo(endDate) <= 0)  //iterate through every day in request
 		{
 			int availableDrivers = 0;  //Available drivers must be initialised to 0 for each new day
 			for(Driver thisDriver: drivers)
 			{
 				if(thisDriver.isAvailable(date))  //Checks whether a driver is available to work
 					availableDrivers++;
 				if(availableDrivers > MINIMUM_DRIVERS_AVAILABLE)  //Exit for loop if available drivers
 					break;					  //exceeds the minimum no. of drivers
 			}
 			if(availableDrivers <= MINIMUM_DRIVERS_AVAILABLE)  //throw an exception if there isn't enough drivers
 				throw new Exception("Holidays for " + date + " are already fully booked");
 			date.setTime(date.getTime() + 24*60*60*1000);
 		}
 		this.status = 0;
 		this.exists = false;
 	}
 	
 	public Request(String start, String end, Driver driver) throws ParseException, Exception
 	{
 		this(parser.parse(start), parser.parse(end), driver);
 	}
 
 	/**
          * Returns the duration of the request in days
          */
 	public int getLength()
 	{
 		return (int)(TimeUnit.MILLISECONDS.toDays(endDate.getTime()
                 - startDate.getTime())) + 1;
 	}
 	
 	/**
          * Returns whether the Requests exists
          */
 	public Boolean getExists() 
 	{
 		return exists;
 	}
 
 	/**
          * Returns the database ID of the request
          */
 	public int getId() 
 	{
 		return id;
 	}
 
 	/**
          * Returns the starting date of the request
          */
 	public Date getStartDate() 
 	{
 		return startDate;
 	}
 
 	/**
          * Returns the ending date of the request
          */
 	public Date getEndDate() 
 	{
 		return endDate;
 	}
 
 	/**
          * Returns the status of the request
          */
 	public int getStatus() 
 	{
 		return status;
 	}
 
 	/**
          * Inserts the request into the database
          */
 	public void save() throws Exception
 	{
 		if(!exists)
 		{
 			RequestInfo.insert(driver.getID(), this.startDate, this.endDate, this.status);
 			driver.takeHoliday(this);
 		}
 		else
 		  throw new Exception("This request has already been sent");
 	}
 
 	/**
          * Returns requests requested by a driver
 	 * @param the driver id of the driver
          */	
 	public static Request[] getByDriver(int driver)
 	{
 		int[] ids = RequestInfo.findRequestByDriver(driver);
 		Request[] requests = new Request[ids.length];
 		for(int i=0;i<ids.length;i++)
 			requests[i] = new Request(ids[i]);
 		return requests;
 	}
 	
 	/**
          * Returns the requests requested by a driver
 	 * @param an instance of a driver
          */	
 	public static Request[] getByDriver(Driver driver)
 	{
 		return getByDriver(driver.getID());
 	}
 	
 	/**
          * 
          */
 	public String getStatusMessage()
 	{
 		return statusMessage[this.status];
 	}
 }
