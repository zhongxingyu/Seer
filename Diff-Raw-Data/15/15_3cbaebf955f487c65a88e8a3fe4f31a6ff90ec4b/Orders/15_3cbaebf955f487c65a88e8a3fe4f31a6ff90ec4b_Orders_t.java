 package cz.fi.muni.pv243.eshop.model;
 
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
import javax.persistence.OneToOne;
 
 @Entity
 public class Orders {
 
 	@Id
 	@GeneratedValue
 	private Long id;
	@OneToOne
 	private Customer customer;
 	private boolean open;
 
 	// private Tuple<Integer, Integer> tuple;
 
 	public Orders() {
 		super();
 	}
 
 	public Orders(Customer customer) {
 		this.customer = customer;
 		this.open = true;
 	}
 
 	// private List<Product> products;
 
 	/**
 	 * @return the id
 	 */
 	public Long getId() {
 		return id;
 	}
 
 	/**
 	 * @param id
 	 *            the id to set
 	 */
 	public void setId(Long id) {
 		this.id = id;
 	}
 
 	/**
 	 * @return the customer
 	 */
 	public Customer getCustomer() {
 		return customer;
 	}
 
 	/**
 	 * @param customer
 	 *            the customer to set
 	 */
 	public void setCustomer(Customer customer) {
 		this.customer = customer;
 	}
 
 	/**
 	 * @return the open
 	 */
 	public boolean isOpen() {
 		return open;
 	}
 
 	/**
 	 * @param open
 	 *            the open to set
 	 */
 	public void setOpen(boolean open) {
 		this.open = open;
 	}
 
 	// /**
 	// * @return the products
 	// */
 	// public List<Product> getProducts() {
 	// return products;
 	// }
 	//
 	// /**
 	// * @param products
 	// * the products to set
 	// */
 	// public void setProducts(List<Product> products) {
 	// this.products = products;
 	// }
 
 }
