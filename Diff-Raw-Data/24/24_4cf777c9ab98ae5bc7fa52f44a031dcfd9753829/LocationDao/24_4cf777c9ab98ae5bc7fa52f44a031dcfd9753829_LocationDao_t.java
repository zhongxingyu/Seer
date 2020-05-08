 package daos;
 
 import java.util.List;

import model.Location;

 import org.hibernate.Session;
 
 import db.HibernateUtil;
 
 /**
  * This class is responsible of supplying services related to the Location entity which require database access.
  * Singleton class.
  */
 public class LocationDao {
 
 	private static LocationDao instance = null;
 
 	private LocationDao() {}
 
 	public static LocationDao getInstance() {
 		if (instance == null) {
 			instance = new LocationDao();
 		}
 		return instance;
 	}
 	
 	/**
 	 * Get a list of all the locations that are stored in the database
 	 */
 	public List<Location> getLocations(){
 		
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
		@SuppressWarnings("unchecked")
 		List<Location> result = (List<Location>) session.createQuery("from Location").list();
 		session.getTransaction().commit();
 		return result;
 	}
 	
 	
 	/**
 	 * Get a location from the database by its database key ID
 	 * @param id
 	 * @return
 	 */
 	public Location getLocationById(String id){
 		
 		return (Location)HibernateUtil.getSessionFactory().getCurrentSession().createQuery(
 				"select Loc from Location Loc where Loc.locationId = :locId")
                 .setEntity("locId", id)
                 .uniqueResult();
 		
 	}
 	
 	
 	/**
 	 * Add a new location to the database
 	 * @param location
 	 * @return
 	 */
 	public Location addLocation(Location location){
 	Location loc = new Location(location.getName(), location.getAddress(), location.getMaxCapacity(), location.getContactName(), location.getPhone1(), location.getPhone2());
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		session.merge(loc);
 		session.getTransaction().commit();
 
 		return loc;
 		
 	}
 	
 	/**
 	 * Update an existing location in the database
 	 * @param location
 	 * @return
 	 */
 	public Location updateLocation(Location location){
		
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
		session.update(location); 
 		session.getTransaction().commit();
		return location;
 		
 	}
 }
