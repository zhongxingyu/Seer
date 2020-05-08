 package aic2010.services;
 
 import aic2010.model.Customer;
 import java.math.BigDecimal;
 import java.util.List;
 import javax.jws.WebMethod;
 import javax.jws.WebParam;
 import javax.jws.WebService;
 
 
@WebService(targetNamespace="http://infosys.tuwien.ac.at/aic10/ass1/dto/customer")
 public interface CustomerService {
 
     @WebMethod(operationName="get_customer")
     public Customer getCustomer(@WebParam(name="id") String id);
 
     @WebMethod(operationName="get_all_customers")
     public List<Customer> getCustomers();
 
     @WebMethod(operationName="add_customer")
     public void addCustomer(@WebParam(name="customer") Customer customer);
 
     @WebMethod(operationName="update_customer")
     public void updateCustomer(@WebParam(name="customer") Customer customer);
 
     @WebMethod(operationName="delete_customer")
     public void deleteCustomer(@WebParam(name="id") String id);
 
     @WebMethod(operationName="notify")
     public void notify(@WebParam(name="customer") Customer customer, @WebParam(name="message") String message);
 
     @WebMethod(operationName="update_account")
     public void updateAccount(@WebParam(name="customer") Customer customer, @WebParam(name="changed_value") BigDecimal changedValue);
 }
