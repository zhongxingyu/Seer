 package ufly.entities;
 
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.StringTokenizer;
 import java.util.Vector;
 
 import javax.jdo.PersistenceManager;
 import javax.jdo.Query;
 import javax.jdo.annotations.IdGeneratorStrategy;
 import javax.jdo.annotations.PersistenceCapable;
 import javax.jdo.annotations.Persistent;
 import javax.jdo.annotations.PrimaryKey;
 
 
 import com.google.appengine.api.datastore.Key;
 import com.google.appengine.api.datastore.KeyFactory;
 
 import java.text.ParsePosition;
 
 @PersistenceCapable (detachable="true")
 public class Flight extends SuperEntity{
 
 	/*------------ CONSTRUCTORS ------------*/
 	/**
 	 * Create a Flight object
 	 * @param flightNumber 				: The Flight's flight number
 	 * @param origin					: The Flight's place of origin
 	 * @param destination				: The Flight's place of destination
 	 * @param departure					: The Flight's departure date
 	 * @param arrival					: The Flight's arrival date
 	 * @param allowableMealTypes		: A vector of Meals available on this Flight
 	 * @param seatingArrangementLayout	: The string which determines the Flight's SeatingArrangment instance
 	 *
 	 * Example initialization
 	 * JDV - Why are do we not pass objects Flight(string,Airport,Airport,Date,Date,Meal,Airplane)??
 	 * new Flight (	####
 	 * 				YVR
 	 * 				LAX
 	 * 				yyyy/MM/dd HH:mm
 	 * 				yyyy/MM/dd HH:mm
 	 * 				BF-CK-PK
 	 * 				BOEING_777
 	 */
 	public Flight(String flightNumber, String origin, String destination, String departure, String arrival, String allowableMealTypes, String aircraftModel,Integer priceInCents)
 	{
 		/*
 		 *  Create a new flight - need to check error using iterator
 		 *
 		 */
 
 
 		this.k = KeyFactory.createKey(Flight.class.getSimpleName(), flightNumber+departure);
 
 		this.flightNumber = null;
 		this.origin = null;
 		this.destination = null;
 		this.departure = null;
 		this.arrival = null;
 		this.allowableMealTypes=null;
 		this.priceInCents=priceInCents;
 		this.flightNumber = flightNumber;
 
 		Query q;
 
 		// Query for origin Airport key
 		PersistenceManager pm = PMF.get().getPersistenceManager();
 		try
 		{
 			q = pm.newQuery(Airport.class);
 			@SuppressWarnings("unchecked")
 			List<Airport> originResults=(List<Airport>) q.execute();
 			Iterator<Airport> it = originResults.iterator();
 			while(it.hasNext())
 			{
 				Airport a = it.next();
 				if(a.getCallSign().equalsIgnoreCase(origin))
 				{
 					this.origin=a.getKey();
 					break;
 				}
 			}
 			if( this.origin == null )
 			{
 				throw new NullPointerException();
 			}
 		}finally{
 			pm.close();
 		}
 
 		// Retrieve detached Airport object for modifying
 		Airport tempOrigin = Airport.getAirport(this.origin);
 		// Add Flight's key to origin Airport
 		tempOrigin.addDepartingFlight(this.k);
 
 		// Query for destination Airport key
 		pm = PMF.get().getPersistenceManager();
 		try
 		{
 			q = pm.newQuery(Airport.class);
 			@SuppressWarnings("unchecked")
 			List<Airport> destinationResults=(List<Airport>) q.execute();
 			Iterator<Airport> it = destinationResults.iterator();
 			while(it.hasNext())
 			{
 				Airport a = it.next();
 				if(a.getCallSign().equalsIgnoreCase(destination))
 				{
 					this.destination=a.getKey();
 					break;
 				}
 			}
 			if( this.destination == null )
 			{
 				throw new NullPointerException();
 			}
 		}finally{
 			pm.close();
 		}
 
 		// Retrieve detached Airport object for modifying
 		Airport tempDestination = Airport.getAirport(this.destination);
 		// Add Flight's key to destination Airport
 		tempDestination.addArrivalFlight(this.k);
 
 
 		SimpleDateFormat convertToDate = new SimpleDateFormat("yyyy/MM/dd HH:mm");
 		//Departure
 		this.departure = convertToDate.parse(departure, new ParsePosition(0));
 		//Arrival
 		this.arrival = convertToDate.parse(arrival, new ParsePosition(0));
 
 		// Set available in-flight meals
 		//Format of the allowMeal vector input string: "CK-BF-PK"
 		StringTokenizer st = new StringTokenizer(allowableMealTypes, "-");
 		Vector<Meal> thisFlightMeals = new Vector<Meal>();
 		while (st.hasMoreTokens())
 		{
 	         String mealType = st.nextToken();
 	         if (mealType.equalsIgnoreCase("ck")) {
 					thisFlightMeals.add(Meal.chicken);
 				}
 				else if (mealType.equalsIgnoreCase("bf")) {
 					thisFlightMeals.add(Meal.beef);
 
 				}
 				else if (mealType.equalsIgnoreCase("pk")) {
 					thisFlightMeals.add(Meal.pork);
 				}
 				else if (mealType.equalsIgnoreCase("FH")) {
 					thisFlightMeals.add(Meal.fish);
 
 				}
 				else if (mealType.equalsIgnoreCase("VG")) {
 					thisFlightMeals.add(Meal.veggie);
 				}
 	     }
 		/*Vector<Meal> thisFlightMeals = new Vector<Meal>();
 		for (int i = 0; i < allowableMealTypes.length(); i+=2) {
 			String firstChar = Character.toString(allowableMealTypes.charAt(i));
 			String seconChar = Character.toString(allowableMealTypes.charAt(i+1));
 			String mealType = firstChar + seconChar;
 
 			if (mealType.equalsIgnoreCase("ck")) {
 				thisFlightMeals.add(Meal.chicken);
 			}
 			else if (mealType.equalsIgnoreCase("bf")) {
 				thisFlightMeals.add(Meal.beef);
 
 			}
 			else if (mealType.equalsIgnoreCase("pk")) {
 				thisFlightMeals.add(Meal.pork);
 			}
 			else if (mealType.equalsIgnoreCase("FH")) {
 				thisFlightMeals.add(Meal.fish);
 
 			}
 			else if (mealType.equalsIgnoreCase("VG")) {
 				thisFlightMeals.add(Meal.veggie);
 			}
 
 		}*/
 		this.allowableMealTypes = thisFlightMeals;
 
 		// Create Flight's seating arrangement
 		SeatingArrangement sa = new SeatingArrangement(aircraftModel);
 		// Have to makePersistant here otherwise I won't know the SeatingArrangement's key
 		// TODO use superclass makePersistent method
 		pm = PMF.get().getPersistenceManager();
 		try{
 			pm.makePersistent(sa);
 		}finally{
 			pm.close();
 		}
 		this.seatingArrangement = sa.getKey();
 		/*if( this.seatingArrangement == null )
 			System.out.println("[Flight] seating arrangement is null");
 		else
 			System.out.println("[Flight] seating arrangement is OK");*/
 
 		// Initialise flightBookings
 		flightBookings = new Vector<Key>();
 
 		// Finally, add Flight to datastore
 		pm = PMF.get().getPersistenceManager();
 		try{
 			pm.makePersistent(this);
 		}finally{
 			pm.close();
 		}
 	}
 
 	public Flight(String flightNumber,
 			Airport origin,
 			Airport destination, 
 			Date departure, 
 			Date arrival, 
 			Vector<Meal> allowableMealTypes, 
 			SeatingArrangement seatingArrangement)
 	{
 		this.flightNumber=flightNumber;
 		this.origin= origin.getKey();
 		this.destination=destination.getKey();
 		this.departure= departure;
 		this.arrival= arrival;
 		this.allowableMealTypes=allowableMealTypes;
 		this.seatingArrangement=seatingArrangement.getKey();
 		this.makePersistant();
 	}
 	/*------------ CLASS METHODS ------------*/
 	public static List<Flight> getAllFlights()
 	{
 		PersistenceManager pm = PMF.get().getPersistenceManager();
 		try
 		{
 			Query q = pm.newQuery(Flight.class);
             @SuppressWarnings("unchecked")
             List<Flight> results = (List<Flight>) q.execute();
             return results;
 		}finally{
 			pm.close();
 		}
 	}
 	/**
 	 * Get flight by number and departing time
 	 * @param flightNumber String
 	 * @param departure String Date (yyyy/MM/dd HH:mm)
 	 * @return
 	 */
 	public static Flight getFlight(String flightNumber,Date departure)
 	{
 		Flight toRet= null;
 		Date dayTime =  (Date) departure.clone();
 		PersistenceManager pm = PMF.get().getPersistenceManager();
 		try{
 			Query q = pm.newQuery(Flight.class, "flightNumber == FlightNoParam && " +
 												"departure ==  DateParam" );
 			q.declareImports("import java.util.Date" );
 			Object[] param = new Object[2];
 			param[0]=flightNumber;
 			param[1]=dayTime;
 			q.declareParameters("String FlightNoParam,Date DateParam");
 			List<Flight> result=(List<Flight>)q.executeWithArray(param);
 			if (result.size() >0)
 			{
 				toRet=result.get(0);
 			}			
 		}
 		finally{
 			pm.close();
 		}
 		return toRet;
 	}
 	
 	/**
 	 * Get total booked flights by flightnumber (for adminstats)
 	 * @param flightNumber String
 	 * @return
 	 */
 	@SuppressWarnings("unchecked")
 	public static int getBookedFlightsByFlightNum(String flightNumber)
 	{
 		int bookedflights=0;
 		List<Flight> toRet= null;
 		PersistenceManager pm = PMF.get().getPersistenceManager();
 		Query q = pm.newQuery(Flight.class );
 		q.setFilter("flightNumber == FlightNoParam");
 		q.declareParameters("String FlightNoParam");
 		toRet=(List<Flight>)q.executeWithArray(flightNumber);
 		Iterator<Flight> it = toRet.iterator();
 		try{		
 		
 			while (it.hasNext())
 			{
 				bookedflights+=it.next().getNumBookedFlights();
 				//pm.detachCopy(it.next());
 				
 			}
 		}
 		finally{
 			pm.close();
 		}
 		return bookedflights;
 
 	}
 	
 	/**
 	 * Get total booked flights by flightnumber (for admin stats) 
 	 * @param flightNumber String
 	 * @return
 	 */
 	@SuppressWarnings("unchecked")
 	public static int getTotalFlightsByFlightNum(String flightNumber)
 	{
 		int totalflights=0;
 		List<Flight> toRet= null;
 		PersistenceManager pm = PMF.get().getPersistenceManager();
 		Query q = pm.newQuery(Flight.class );
 		q.setFilter("flightNumber == FlightNoParam");
 		q.declareParameters("String FlightNoParam");
 		toRet=(List<Flight>)q.executeWithArray(flightNumber);
 		Iterator<Flight> it = toRet.iterator();
 		try{		
 			return toRet.size();
 		}
 		finally{
 			pm.close();
 		}
 
 		
 	}
 	
 	public static Flight getFlight(Key flightKey)
 	{
 		PersistenceManager pm = PMF.get().getPersistenceManager();
 		Flight f, detached = null;
 		try{
 		    	f = pm.getObjectById(Flight.class, flightKey);
 		        detached = pm.detachCopy(f);
 		    }
 		catch( javax.jdo.JDOException e)
 		{
 			e.printStackTrace();
 		}
 		finally {
 			pm.close();
 		}
 		return detached;
 	}
 	public static Flight getFlight(String flightKey)
 	//TODO: this should be merged into getFlight(Key flightKey)
 	{
 		PersistenceManager pm = PMF.get().getPersistenceManager();
 		Flight f, detached = null;
 		try{
 			f = pm.getObjectById(Flight.class, flightKey);
 		    detached = pm.detachCopy(f);
 		}
 		finally {
 			pm.close();
 		}
 		return detached;
 	}
 	public static List<Flight> getFlightsOnDate(Date day)
 	{
 		final long TWENTYFOUR_HOURS =24000*3600;
 		/*get date stuff set up*/
 		List<Flight> toRet=null;
 		Date startTime = new Date(day.getTime());
 		Date endTime = new Date(day.getTime()+TWENTYFOUR_HOURS);
 		PersistenceManager pm = PMF.get().getPersistenceManager();
 		try{
 			Query q = pm.newQuery(Flight.class, "departure >  startDateParam && departure < endDateParam" );
 			q.declareImports("import java.util.Date" );
 			q.declareParameters("Date startDateParam,Date endDateParam");
 			q.setOrdering("departure asc");
 			toRet=(List<Flight>)q.executeWithArray(startTime,endTime);
 			Iterator<Flight> it = toRet.iterator();
 			while (it.hasNext())
 			{
 				pm.detachCopy(it.next());
 			}
 		}
 		finally{
 			pm.close();
 		}
 		return toRet;
 	}
 	/**
 	 *
 	 * @param origin the airport to fly from,
 	 * @param day the o
 	 * @return list of flights departing from airport origin within 24 hours of dayTime
 	 */
 
 	public static List<Flight> getFlightsOriginDate(Airport origin,Date dayTime) {
 		final long TWENTYFOUR_HOURS =24000*3600;
 		/*get date stuff set up*/
 		List<Flight> toRet=null;
 		Date startTime = new Date(dayTime.getTime());
 		Date endTime = new Date(dayTime.getTime()+TWENTYFOUR_HOURS);
 
 		PersistenceManager pm = PMF.get().getPersistenceManager();
 		try{
 			Query q = pm.newQuery(Flight.class, "origin == originParam && " +
 												"departure >  startDateParam && departure < endDateParam" );
 			q.declareImports("import com.google.appengine.api.datastore.Key;import java.util.Date" );
 			Object[] param = new Object[3];
 			param[0]=origin.getKey();
 			param[1]=startTime;
 			param[2]=endTime;
 			q.declareParameters("Key originParam, Date startDateParam,Date endDateParam");
 			toRet=(List<Flight>)q.executeWithArray(param);
 			Iterator<Flight> it = toRet.iterator();
 			while (it.hasNext())
 			{
 				pm.detachCopy(it.next());
 			}
 		}
 		finally{
 			pm.close();
 		}
 		return toRet;
 	}
 
 	public static List<Flight> getFlightsConnectingDateTime(Airport origin,Airport destination,Date dayTime)
 	{
 
 		final long HOUR_IN_MILLIS = 1000*3600;
 		final long MAX_STOPOVER_HOURS = 6;
 		List<Flight> toRet=new Vector<Flight>();
 		List<Flight>possibleFlights=Flight.getFlightsOriginDate(origin, dayTime);
 		Iterator<Flight> it = possibleFlights.iterator();
 		while (it.hasNext())
 		{
 			Flight f=it.next();
 
 			if(f.getDestination().equals(destination) &&
 					f.getArrival().before(new Date(dayTime.getTime()+MAX_STOPOVER_HOURS*HOUR_IN_MILLIS)) &&
 					f.getDeparture().after(dayTime))
 			{
 				toRet.add(f);
 			}
 				
 		}
 		
 		return toRet;
 	}
 	/**
 	 * @param newMeals	: new list of meals to update to
 	 */
 	public void changeAllowableMeals(Vector<Meal> newMeals )
 	{
 		PersistenceManager pm= PMF.get().getPersistenceManager();
 		try
 		{
 			this.allowableMealTypes=newMeals;
 			pm.makePersistent(this);
 
 		}finally
 		{
 			pm.close();
 		}
 	}
 	/**
 	 * @param newArrival	: new arrival date to update to
 	 */
 	public void changeArrival(Date newArrival)
 	{
 		PersistenceManager pm= PMF.get().getPersistenceManager();
 		try
 		{
 			this.arrival=newArrival;
 			pm.makePersistent(this);
 
 		}finally
 		{
 			pm.close();
 		}
 	}
 
 	/**
 	 * @param newDeparture	: new departure date to update to
 	 */
 	public void changeDeparture(Date newDeparture)
 	{
 		PersistenceManager pm= PMF.get().getPersistenceManager();
 		try
 		{
 			this.departure=newDeparture;
 			pm.makePersistent(this);
 
 		}finally
 		{
 			pm.close();
 		}
 	}
 
 	/**
 	 * @param dest	: new destination to update to
 	 */
 	public void changeDestination(Airport dest)
 	{
 		PersistenceManager pm= PMF.get().getPersistenceManager();
 		try
 		{
 			this.destination=dest.getKey();
 			pm.makePersistent(this);
 
 		}finally
 		{
 			pm.close();
 		}
 	}
 
 	/*------------ MODIFIERS ------------*/
 	/**
 	 * @param newFlightNumber	: new flight number to update to
 	 */
 	public void changeFlightNumber(String newFlightNumber)
 	{
 		PersistenceManager pm= PMF.get().getPersistenceManager();
 		try
 		{
 			this.flightNumber=newFlightNumber;
 			pm.makePersistent(this);
 
 		}finally
 		{
 			pm.close();
 		}
 	}
 	/**
 	 * 
 	 * @param newFlightNumber
 	 */
 	public void changePrice(Integer priceInCents)
 	{
 		PersistenceManager pm= PMF.get().getPersistenceManager();
 		try
 		{
 			this.priceInCents=priceInCents;
 			pm.makePersistent(this);
 
 		}finally
 		{
 			pm.close();
 		}
 	}
 	/**
 	 * @param orig	: new origin to update to
 	 */
 	public void changeOrigin(Airport orig)
 	{
 		PersistenceManager pm= PMF.get().getPersistenceManager();
 		try
 		{
 			this.origin=orig.getKey();
 			pm.makePersistent(this);
 
 		}finally
 		{
 			pm.close();
 		}
 	}
 
 	/**
 	 * @param newSeatingArrangement	: new seating arrangement to update to
 	 */
 	// Don't think we want this. Can get really messy with this...
 	/*public void changeSeatingArrangement(SeatingArrangement newSeatingArrangement )
 	{
 		PersistenceManager pm= PMF.get().getPersistenceManager();
 		try
 		{
 			this.seatingArrangement=newSeatingArrangement;
 			pm.makePersistent(this);
 
 		}finally
 		{
 			pm.close();
 		}
 	}*/
 
 	/**
 	 * @param newFlightNumber	: new flight number to update to
 	 */
 	public void addFlightBooking(Key fbKey)
 	{
 		PersistenceManager pm= PMF.get().getPersistenceManager();
 		try
 		{
 			this.flightBookings.add(fbKey);
 			pm.makePersistent(this);
 
 		}finally
 		{
 			pm.close();
 		}
 	}
 	
 	/**
 	 * @param fb	: remove fb from flightbooking vector
 	 */
 	public void removeBooking(Key fb)
 	{
 		PersistenceManager pm= PMF.get().getPersistenceManager();
 
 		try
 		{	
 			Iterator<Key> itr=this.flightBookings.iterator();
 			while(itr.hasNext()){
 				Key cur_fb = itr.next();
 				if( cur_fb.toString().equals(fb.toString()) )
 				{
 					this.flightBookings.remove(cur_fb);//since a flightbooking is unique this works
 					pm.makePersistent(this);
 					break;
 				}
 			}
 		}finally
 		{
 			pm.close();
 		}
 	}
 	
 	/**
 	 * @return the allowableMealTypes
 	 */
 	public Vector<Meal> getAllowableMeals()
 	{
 		return this.allowableMealTypes;
 	}
 	public HashMap<String,Object> getHashMap()
 	{
 		HashMap flightAttributes = new HashMap<String,Object>();
 		flightAttributes.put("flightNo", this.getFlightNumber());
 		
 		flightAttributes.put("flightOrigin", this.getOrigin().getCity());
 		flightAttributes.put("flightDestination", this.getDestination().getCity());
 		
 		flightAttributes.put("departs", this.getDeparture());
 		
 		flightAttributes.put("arrives",this.getArrival());
 		
 		long durationInMins = (this.getArrival().getTime()-this.getDeparture().getTime())/1000/60;
 		flightAttributes.put("durationInMins", new Long(durationInMins));
 		
 		if (this.priceInCents == null)
 		{
 			this.changePrice(500000);
 		}
 		flightAttributes.put("price", new Integer(this.priceInCents));
 		Vector<Seat> seatObjs=this.getSeatingArrangement().getAvailableSeats();
 		String[] seats=new String[seatObjs.size()];
 		for(int i=0;i<seatObjs.size();i++)
 		{
 			Integer row = seatObjs.get(i).getRowNumber();
 			Character col = seatObjs.get(i).getColumn();
 			seats[i]=row.toString()+" "+col;
 		}
 		flightAttributes.put("seatingArrangement",this.getSeatingArrangement().getHashMap());
 		flightAttributes.put("availableSeats",seats);
 		StringBuffer allowableMeals=new StringBuffer();
 		for(Meal m:this.getAllowableMeals())
 		{
 			allowableMeals.append(m.toString());
 			allowableMeals.append("|");
 		}
 		flightAttributes.put("allowableMeals",allowableMeals.toString());
 		return flightAttributes;
 	}
 	/**
 	 * @return the arrival date
 	 */
 	public Date getArrival()
 	{
 		return this.arrival;
 	}
 
 	/**
 	 * @return the departure date
 	 */
 	public Date getDeparture()
 	{
 		return this.departure;
 	}
 	public Vector<FlightBooking> getBookings()
 	{
 		Vector<FlightBooking> toRet = new Vector(this.flightBookings.size());
 		for(Key fbK:this.flightBookings){
 			toRet.add(FlightBooking.getFlightBooking(fbK));
 		}
 		return toRet;
 	}
 	/**
 	 * @return the destination airport
 	 */
 	public Airport getDestination()
 	{
 		return Airport.getAirport(this.destination);
 	}
 
 	/*------------ ACCESSORS ------------*/
 	/**
 	 * @return the flightNumber
 	 */
 	public String getFlightNumber()
 	{
 		return this.flightNumber;
 	}
 
 	public Key getKey()
 	{
 		return this.k;
 	}
 
 	/**
 	 * @return the origin airport
 	 */
 	public Airport getOrigin()
 	{
 		return Airport.getAirport(this.origin);
 	}
 
 	/**
 	 * @return the seatingArragement
 	 */
 	public SeatingArrangement getSeatingArrangement()
 	{
 		return SeatingArrangement.getSeatingArrangement(this.seatingArrangement);
 	}
 	/**
 	 * @return the number of booked flights
 	 */
 	public int getNumBookedFlights()
 	{
 		return flightBookings.size();
 	}
 	public int getCapacity(){
 		SeatingArrangement sa=this.getSeatingArrangement();
 		return sa.getNumRows() * sa.getNumColumns();
 	}
 	public Integer getPriceInCents()
 	{
 		return priceInCents;
 	}
 
 
 	@SuppressWarnings("deprecation")
 	@Override
 	public String toString() {
 		return "Flight [flightNumber=" + flightNumber + ", origin=" + origin.toString()
 				+ ", destination=" + destination.toString()+ ", departure=" + departure.toLocaleString()
				+ ", arrival=" + arrival + ", allowableMealTypes="
 				+ allowableMealTypes + ", seatingArragement="
 				+ seatingArrangement + ", flightBookings=" + flightBookings.toString() + " #elems: " + flightBookings.size()
 				+ "]";
 	}
 	/*------------ VARIABLES ------------*/
 	@PrimaryKey
 	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
 	private Key k; 	// Use a flightNumber and departure concatenated string to serve as entity key (to avoid using numeric ID) for now
 	@Persistent
 	private String flightNumber;					// The Flight's flight number e.g. CX838. This and the departure Date determines the Flight.
 	@Persistent
 	private Key origin;							// The Flight's place of origin. Note a new Airport entity should not be created.
 	@Persistent
 	private Key destination;					// The Flight's place of destination. Note a new Airport entity should not be created.
 	@Persistent
 	private Date departure;							// The Flight's departure date (defined with year, month, date, hrs, min)
 	@Persistent
 	private Date arrival;							// The Flight's arrival date (defined with year, month, date, hrs, min)
 	@Persistent
 	private Vector<Meal> allowableMealTypes;		// The meals available on this Flight
 	@Persistent
 	private Key seatingArrangement;					// Each Flight will have one seating arrangement layout
 	@Persistent
 	private Integer priceInCents;					//Price for the flight
 	@Persistent
 	private Vector<Key> flightBookings;	// The bookings made on this Flight
 }
