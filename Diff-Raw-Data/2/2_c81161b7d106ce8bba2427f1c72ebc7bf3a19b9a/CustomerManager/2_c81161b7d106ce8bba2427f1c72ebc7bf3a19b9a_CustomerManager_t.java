 package cz.muni.fi.pv168;
 
import java.util.List;
 import javax.sql.DataSource;
 
 /**
  * Created by IntelliJ IDEA.
  * User: fivekeyem, janinko
  * Date: 3/14/11
  */
 public interface CustomerManager {
 
     Customer createCustomer(Customer customer);
 
     Customer deleteCustomer(Customer customer);
 
     Customer updateCustomer(Customer customer);
 
     List<Customer> getAllCustomers();
 
     Customer getCustomerById(int id);
 
     void setDs(DataSource ds);
 }
