 package it.gas.altichierock.insert;
 
 import it.gas.altichierock.database.DatabaseHandler;
 import it.gas.altichierock.database.entities.Product;
 import it.gas.altichierock.database.entities.Ticket;
 import it.gas.altichierock.database.entities.TicketContent;
 
 import java.sql.Date;
 import java.sql.Time;
 import java.util.List;
 
 import javax.persistence.EntityManager;
 import javax.persistence.EntityTransaction;
 import javax.persistence.NoResultException;
 import javax.persistence.TypedQuery;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class InsertLogic {
 	private Logger log = LoggerFactory.getLogger(InsertLogic.class);
 	private DatabaseHandler dh;
 	private EntityManager em;
 	private EntityTransaction tx;
 
 	public InsertLogic() {
 		dh = DatabaseHandler.getInstance();
 		em = dh.getEntityManager();
 		tx = dh.getTransaction();
 	}
 
 	public List<Product> getProducts() {
 		log.debug("getProducts()");
 		TypedQuery<Product> query = em.createNamedQuery("item.ordered.all",
 				Product.class);
 		return query.getResultList();
 	}
 	
 	public int storeKeeper(ReceiptKeeper rk) {
 		tx.begin();
 		//get the id
 		TypedQuery<Integer> query = em.createNamedQuery("order.maxidtoday", Integer.class);
		int id = 1;
 		try {
			id = query.getSingleResult() + 1;
 		} catch (NoResultException e) {
 			id = 1;
 		}
 		//get the time
 		long d = System.currentTimeMillis();
 		//make the ticket
 		Ticket t = new Ticket();
 		t.setTicketid(id);
 		t.setCreateDate(new Date(d));
 		t.setCreateTime(new Time(d));
 		//hoping for tha best! now with the rest
 		for (int i = 0; i < rk.getSize(); i++) {
 			TicketContent tc = new TicketContent();
 			tc.setLineNumber(i);
 			tc.setQuantity(rk.getQuantity(i));
 			tc.setDescription(rk.getDescription(i));
 			tc.setPrice(rk.getPrice(i));
 			em.persist(tc); //content
 			t.getContent().add(tc);
 		}
 		em.persist(t); //ticket
 		tx.commit();
 		return id;
 	}
 
 }
