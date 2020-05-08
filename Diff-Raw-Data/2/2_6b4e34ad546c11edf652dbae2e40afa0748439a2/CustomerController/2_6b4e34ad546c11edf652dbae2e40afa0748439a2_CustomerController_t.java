 package sse.client.controller;
 
 import java.util.List;
 
 import javax.annotation.ManagedBean;
 import javax.enterprise.context.SessionScoped;
 
 import sse.model.Customer;
 
 @ManagedBean("customerCtrl")
 @SessionScoped
 public class CustomerController {
	
 	private List<Customer> customers;
 	private Customer selected;
 	private Boolean delete;
 	
 	public List<Customer> getCustomers() {
 		return customers;
 	}
 	public void setCustomers(List<Customer> customers) {
 		this.customers = customers;
 	}
 	public Customer getSelected() {
 		return selected;
 	}
 	public void setSelected(Customer selected) {
 		this.selected = selected;
 	}
 	
 	public void create() {
 		
 	}
 	
 	public void save() {
 		
 	}
 	
 	public void delete() {
 		this.setDelete(Boolean.TRUE);
 	}
 	
 	public void setDelete(Boolean delete) {
 		this.delete = delete;
 	}
 	public Boolean getDelete() {
 		return delete;
 	}
 	
 }
