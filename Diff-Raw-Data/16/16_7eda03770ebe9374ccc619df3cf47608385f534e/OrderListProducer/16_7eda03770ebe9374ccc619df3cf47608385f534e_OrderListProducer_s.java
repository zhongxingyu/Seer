 package cz.fi.muni.pv243.eshop.data;
 
 import java.util.List;
 
 import javax.annotation.PostConstruct;
 import javax.enterprise.context.RequestScoped;
 import javax.enterprise.event.Observes;
 import javax.enterprise.event.Reception;
 import javax.enterprise.inject.Produces;
 import javax.inject.Inject;
 import javax.inject.Named;
 import javax.persistence.EntityManager;
 import javax.persistence.criteria.CriteriaBuilder;
 import javax.persistence.criteria.CriteriaQuery;
 import javax.persistence.criteria.Expression;
 import javax.persistence.criteria.Root;
 
 import org.jboss.seam.security.Identity;
 
 import cz.fi.muni.pv243.eshop.model.Customer;
 import cz.fi.muni.pv243.eshop.model.Orders;
import cz.fi.muni.pv243.eshop.service.CustomerManager;
 
 @RequestScoped
 public class OrderListProducer {
 	@Inject
 	private EntityManager em;
 
 	private List<Orders> orders;
 
 	@Inject
 	private Identity identity;
 
	@Inject
	private CustomerManager customerManager;
 
 	@Produces
 	@Named("customerOrders")
 	public List<Orders> getOrders() {
 		return orders;
 	}
 
 	public void onOrderListChanged(
 			@Observes(notifyObserver = Reception.IF_EXISTS) final Orders orders) {
 		System.out.println("Observing");
 		retrieveAllCustomersOrders();
 	}
 
 	@PostConstruct
 	public void retrieveAllCustomersOrders() {
 		System.out.println("retrieve all customers?");
 		CriteriaBuilder cb = em.getCriteriaBuilder();
 		CriteriaQuery<Orders> criteria = cb.createQuery(Orders.class);
 		Root<Orders> orders = criteria.from(Orders.class);
		Customer customer = customerManager.isRegistred(identity.getUser()
				.toString());
 		criteria.select(orders).orderBy(cb.asc(orders.get("id")));
 		Expression<Customer> customerExpression = orders.get("customer");
		this.orders = em.createQuery(criteria).getResultList();
		// .where(cb.equal(customerExpression, customer))
 	}
 }
