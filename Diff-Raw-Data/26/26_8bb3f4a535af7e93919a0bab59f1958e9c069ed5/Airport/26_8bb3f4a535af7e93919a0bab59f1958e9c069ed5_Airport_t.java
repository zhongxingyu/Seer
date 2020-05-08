 package ufly.entities;
 
 import java.util.List;
 import java.util.Vector;
 
 import javax.jdo.PersistenceManager;
 import javax.jdo.Query;
 import javax.jdo.annotations.Extension;
 import javax.jdo.annotations.IdGeneratorStrategy;
 import javax.jdo.annotations.PersistenceCapable;
 import javax.jdo.annotations.Persistent;
 import javax.jdo.annotations.PrimaryKey;
 
 @PersistenceCapable
 public class Airport {
 
 	/*------------ CONSTRUCTORS ------------*/
 	/**
 	 * Create an Airport object
 	 * @param callsign	: The Airport's callsign
 	 * @param city		: The Airport's city
 	 */
 	public Airport(String callsign, String city)
 	{
 		this.callsign = callsign;
 		this.city = city;
 		PersistenceManager pm = PMF.get().getPersistenceManager();
 		try{
 			pm.makePersistent(this);
 		}finally{
 			pm.close();
 		}
 	}
 
 
 	/*------------MODIFIERS--------------*/
 	/**
	 * @param newCallSign	: new  to update to
 	 */
	public void changeCallSign(String newCallSign)
 	{
 		PersistenceManager pm= PMF.get().getPersistenceManager();
 		try
 		{
 			this.callsign=newCallSign;
 			pm.makePersistent(this);
 
 		}finally
 		{
 			pm.close();
 		}
	}
 
 	/**
 	 * @param newCity	: new City to update to
 	 */
 	public void changeCity(String newCity)
 	{
 		PersistenceManager pm= PMF.get().getPersistenceManager();
 		try
 		{
 			this.city=newCity;
 			pm.makePersistent(this);
 
 		}finally
 		{
 			pm.close();
 		}
 	}
 
 	/**
 	 * @param newDepartingFlight	: new Flight to add to list of flights
 	 */
 	public void addDepartingFlight(Flight newDepartingFlight)
 	{
 		PersistenceManager pm= PMF.get().getPersistenceManager();
 		try
 		{
 			this.departures.add(newDepartingFlight);
 			pm.makePersistent(this);
 
 		}finally
 		{
 			pm.close();
 		}
 	}
 
 	/**
 	 * @param newDepartingFlight	: new Flight to add to list of flights
 	 */
 	public void addArrivalFlight(Flight newArrivalFlight)
 	{
 		PersistenceManager pm= PMF.get().getPersistenceManager();
 		try
 		{
 			this.arrivals.add(newArrivalFlight);
 			pm.makePersistent(this);
 
 		}finally
 		{
 			pm.close();
 		}
 	}
 
 	/**
 	 * @param DepartFlight	: flight to be removed from departures
 	 */
 	public void removeDepartingFlight(Flight DepartFlight)
 	{
 		PersistenceManager pm= PMF.get().getPersistenceManager();
 		try
 		{
 			this.departures.remove(DepartFlight);
 			pm.makePersistent(this);
 
 		}finally
 		{
 			pm.close();
 		}
 	}
 
 	/**
 	 * @param ArrivalFlight	: flight to be removed from arrivals
 	 */
 	public void removeArrivalFlight(Flight ArrivalFlight)
 	{
 		PersistenceManager pm= PMF.get().getPersistenceManager();
 		try
 		{
 			this.arrivals.remove(ArrivalFlight);
 			pm.makePersistent(this);
 
 		}finally
 		{
 			pm.close();
 		}
 	}
 
 
 
 	public static List<Airport> getAllAirports()
 	{
 		PersistenceManager pm = PMF.get().getPersistenceManager();
 		Query q = pm.newQuery(Airport.class);
         /**
          * in order to check this we need to check every element to see if it
          * is of type User, too much work, plus we should not get any
          * other type. We just suppress the warning
          */
         @SuppressWarnings("unchecked")
 		List<Airport> results = (List<Airport>) q.execute();
         return results;
 	}
 	@Override
 	public String toString() {
 		return "Airport [callsign=" + callsign + ", city=" + city + "]";
 	}
 
 
 	/*------------ACCESSORS--------------*/
 	/**
 	 * @return the callsign
 	 */
	public String getCallSign()
 	{
 		return this.callsign;
	}
 
 	/**
 	 * @return the city
 	 */
 	public String getCity()
 	{
 		return this.city;
 	}
 
 	/**
 	 * @return a vector of departure flights from this
 	 */
 	public Vector<Flight> getDepartingFlight()
 	{
 		return this.departures;
 	}
 	/**
 	 * @return a vector of arrival flights from this
 	 */
 	public Vector<Flight> getArrivingFlight()
 	{
 		return this.arrivals;
 	}
 
 
 	/*------------ VARIABLES ------------*/
 	@PrimaryKey
 	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
 	@Extension(vendorName="datanucleus",key="gae.encoded-pk",value="true")
	private String encodedCallsign;		// Note: encodedCallsign is populated automatically
 	@Persistent
     @Extension(vendorName="datanucleus", key="gae.pk-name", value="true")
     private String callsign;			// The Airport's International Air Transport Association (IATA) airport code. e.g. YVR. Uniquely identifies an Airport.
 	@Persistent
 	private String city;				// The Airport's city e.g. Vancouver
 	@Persistent(mappedBy = "origin")
 	private Vector<Flight> departures; 	// The Airport's departing Flights
 	@Persistent(mappedBy = "destination")
 	private Vector<Flight> arrivals;	// The Airport's arriving Flights
 }
