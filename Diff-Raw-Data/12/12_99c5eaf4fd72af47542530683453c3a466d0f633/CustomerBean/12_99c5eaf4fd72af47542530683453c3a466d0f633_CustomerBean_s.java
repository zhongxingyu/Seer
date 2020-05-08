 package cz.fi.muni.pv243.eshop.controller;
 
 import java.io.Serializable;
 import java.util.List;
 import java.util.logging.Logger;
 
 import javax.annotation.PostConstruct;
 import javax.enterprise.event.Observes;
 import javax.enterprise.event.Reception;
 import javax.enterprise.inject.Produces;
 import javax.faces.application.FacesMessage;
 import javax.faces.bean.SessionScoped;
 import javax.faces.context.FacesContext;
 import javax.inject.Inject;
 import javax.inject.Named;
 
 import cz.fi.muni.pv243.eshop.model.Customer;
 import cz.fi.muni.pv243.eshop.service.CustomerManager;
 
 @Named("customersBean")
 @SessionScoped
 public class CustomerBean implements Serializable {
 
 	private static final long serialVersionUID = 1L;
 	@Inject
 	private CustomerManager customerManager;
 
 	private static List<Customer> customerList;
 	@Inject
 	private Logger log;
 
 	private Customer newCustomer;
 
 	@Inject
 	private FacesContext facesContext;
 
 	public List<Customer> getCustomerList() {
 		return customerList;
 	}
 
 	public void updateAction(Customer customer) {
 		System.out.println("update");
 		customerManager.update(customer);
 	}
 
 	public void onCustomerListChanged(
 			@SuppressWarnings("cdi-observer") @Observes(notifyObserver = Reception.IF_EXISTS) final Customer customer) {
 		retrieveAllCustomers();
 	}
 
 	@PostConstruct
 	public void retrieveAllCustomers() {
 		customerList = customerManager.getCustomers();
 		initNewCustomer();
 
 	}
 
 	@Produces
 	@Named
 	public Customer getNewCustomer() {
 		return newCustomer;
 	}
 
 	public void register() throws Exception {
 		if (newCustomer.getPassword() == null) {
 			facesContext.addMessage("addForm:password", new FacesMessage(
 					"Cannot have empty password"));
 			log.finest("Registration: empty password");
 			initNewCustomer();
 		} else {
 			Customer customer = customerManager.isRegistred(newCustomer
 					.getEmail());
 			if (customer == null) {
 				customerManager.addCustomer(newCustomer);
 				log.info("Registration: adding new customer");
 				facesContext.addMessage("addForm:registerButton",
 						new FacesMessage("Customer was added"));
 				initNewCustomer();
 			} else {
 				log.info("Registration: trying to use already registred email");
 				facesContext.addMessage("addForm:registerButton",
 						new FacesMessage("User is already registered"));
 				initNewCustomer();
 			}
 
 		}
 	}
 
 	public void initNewCustomer() {
 		newCustomer = new Customer();
 	}
 
 }
