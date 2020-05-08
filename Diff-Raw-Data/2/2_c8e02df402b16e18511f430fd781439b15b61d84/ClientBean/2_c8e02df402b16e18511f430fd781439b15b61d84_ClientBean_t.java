 package fr.emse.server;
 
 import java.util.List;
 
 import javax.ejb.LocalBean;
 import javax.ejb.Stateless;
 import javax.jws.WebMethod;
 import javax.jws.WebParam;
 import javax.jws.WebService;
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 import javax.persistence.Query;
 
 /**
  * Session Bean implementation class ClientBean
  */
 @Stateless
 @LocalBean
 @WebService(serviceName = "ClientService")
 public class ClientBean implements ClientBeanRemote {
 
 	@PersistenceContext(unitName = "admin-unit")
 	EntityManager em;
 
 	/**
 	 * Default constructor.
 	 */
 	public ClientBean() {
 		// TODO Auto-generated constructor stub
 	}
 
 	@SuppressWarnings("unchecked")
 	@Override
 	@WebMethod(operationName = "itineraries")
 	public List<Itinerary> getItineraries() {
 		Query query = em.createQuery("SELECT m from Itinerary as m");
 		return (List<Itinerary>) query.getResultList();
 	}
 
 	@Override
 	@WebMethod(operationName = "usingItinerary")
 	public void usingItinerary(@WebParam(name = "itineraryId") int id) {
 		Itinerary itinerary = (Itinerary) em.find(Itinerary.class, id);
 		itinerary.isUsed();
		em.merge(itinerary);
 	}
 
 }
