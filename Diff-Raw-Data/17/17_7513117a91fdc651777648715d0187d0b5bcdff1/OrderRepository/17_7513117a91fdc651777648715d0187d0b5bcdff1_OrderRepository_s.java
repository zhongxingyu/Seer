 package ch.hslu.appe.fs1301.data;
 
 import java.util.List;
 
 import ch.hslu.appe.fs1301.data.shared.iAPPEEntityManager;
 import ch.hslu.appe.fs1301.data.shared.iOrderRepository;
 import ch.hslu.appe.fs1301.data.shared.entity.Bestellung;
 
 import com.google.inject.Inject;
 
 public class OrderRepository extends BaseRepository<Bestellung> implements iOrderRepository {
 
 	@Inject
 	public OrderRepository(iAPPEEntityManager entityManager) {
 		super(entityManager);
 	}
 
 	@Override
 	public List<Bestellung> getOrdersById(int... ids) throws IllegalArgumentException {
 		if (ids.length == 0)
 			throw new IllegalArgumentException("ids");
 		
		StringBuilder query = new StringBuilder(100);
		query.append("SELECT b FROM Bestellung b WHERE b.id in (");
		for(int id : ids) {
			query.append(id);
			query.append(',');
 		}
		query.setCharAt(query.length() - 1, ')');
		
		return executeQuery(query);
 	}
 
 }
