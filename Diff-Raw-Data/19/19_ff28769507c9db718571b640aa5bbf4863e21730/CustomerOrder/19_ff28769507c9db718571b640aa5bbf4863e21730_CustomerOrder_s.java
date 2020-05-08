 package de.tud.in.middleware.order;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.persistence.CascadeType;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
 import javax.persistence.ManyToOne;
 import javax.persistence.OneToMany;
 
 import de.tud.in.middleware.customers.Customer;
 import de.tud.in.middleware.products.ProductInstance;
 
 @Entity
 public final class CustomerOrder implements Serializable {
 	private static final long serialVersionUID = 1L;
 
 	@Id
	@GeneratedValue
 	private Integer id;
 
 	private final List<ProductInstance> productInstances;
 	@ManyToOne
 	private Customer customer;
	private OrderState state;
 
 	public CustomerOrder() {
 		super();
 		productInstances = new ArrayList<ProductInstance>();
 	}
 
 	public Integer getId() {
 		return id;
 	}
 
 	public void setId(final Integer id) {
 		this.id = id;
 	}
 
 	@OneToMany(cascade = CascadeType.PERSIST)
 	public List<ProductInstance> getProductInstances() {
 		return productInstances;
 	}
 
 	public Customer getCustomer() {
 		return customer;
 	}
 
 	// TODO Ask on next meeting, how @OneToMany(cascade=CascadeType.PERSIST) at Customer#get...
 	// works and if the following will work.
 	public void setCustomer(final Customer customer) {
 		if (this.customer != null) {
 			this.customer.getOrders().remove(this);
 		}
 		this.customer = customer;
 	}
 
 	public OrderState getState() {
 		return state;
 	}
 
 	public void setState(final OrderState state) {
 		// TODO Do I have to some JPA stuff here?
 		this.state = state;
 	}
 }
