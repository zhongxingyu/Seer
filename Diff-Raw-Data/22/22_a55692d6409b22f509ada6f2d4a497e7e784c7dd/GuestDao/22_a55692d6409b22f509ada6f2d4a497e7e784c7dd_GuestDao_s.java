 package guest;
 
 import java.util.List;
 
 import javax.ejb.Stateless;
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 import javax.persistence.Query;
 import javax.persistence.TypedQuery;
 
 @Stateless
 public class GuestDao {
 	// Injected database connection:
 	@PersistenceContext
 	private EntityManager em;
 
 	// Stores a new guest:
 	public void persist(Guest guest) {
 		em.persist(guest);
 	}
 
 	// Retrieves all the guests:
 	public List<Guest> getAllGuests() {
 		TypedQuery<Guest> query = em.createQuery(
 				"SELECT g FROM Guest g ORDER BY g.id", Guest.class);
 		return query.getResultList();
 	}
 
 	public void deleteByName(String name) {
 		Query query = em
 				.createQuery("DELETE FROM Guest g WHERE g.name = :name");
 		query.setParameter("name", name).executeUpdate();
 	}
 
 	public void updateNameById(String id, String newName) {
 		// Query query = em.createQuery(
 		// "UPDATE Guest g SET g.name = :name " +
 		// "WHERE g.id = :id");
 		// query.setParameter("name", newName).setParameter("id",
 		// id).executeUpdate();
 		Guest guest = em.find(Guest.class, Long.valueOf(id));
 		if (guest != null) {
 			guest.setName(newName);
 		}
 	}
 
 	public int getCountByDefaultObject() {
 		Query query = em
 				.createQuery("SELECT FROM RegularGuest g WHERE g.address.fullAddress = :address");
 		String defaultAddress = "default regular address for simplicity";
 		return query.setParameter("address", defaultAddress).getResultList().size();
 	}
 }
