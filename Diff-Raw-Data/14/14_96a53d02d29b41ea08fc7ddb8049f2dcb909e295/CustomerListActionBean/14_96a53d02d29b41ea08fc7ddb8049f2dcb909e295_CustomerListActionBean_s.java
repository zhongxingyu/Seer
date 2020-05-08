 package com.nordpos.mobile.stripes.action;
 
 /**
  *
  * @author Andrey Svininykh <svininykh@gmail.com>
  */
 import com.nordpos.mobile.stripes.dao.CustomerPersist;
 import com.nordpos.mobile.stripes.model.Customer;
 import java.util.List;
 import net.sourceforge.stripes.action.DefaultHandler;
 import net.sourceforge.stripes.action.ForwardResolution;
 import net.sourceforge.stripes.action.RedirectResolution;
 import net.sourceforge.stripes.action.Resolution;
 
 
 public class CustomerListActionBean extends BaseActionBean {
 
     private static final String LIST = "/WEB-INF/jsp/customer_list.jsp";
     private static final String VIEW = "/WEB-INF/jsp/customer_view.jsp";
     private CustomerPersist customerDao = CustomerPersist.getInstance();
     private String customerId;
 
     @DefaultHandler
     public Resolution list() {
         return new ForwardResolution(LIST);
     }
 
     public Resolution view() {
         return new ForwardResolution(VIEW);
     }
 
 //    public Resolution delete() {
 //        customerDao.delete(customerId);
 //        return new RedirectResolution(getClass());
 //    }
 
    public void setCustomerId(String id) {
        customerId = id;
    }

    public Customer getCustomer() {
        return customerDao.findCustomer(customerId);
    }
 
     public List<Customer> getCustomers() {
         return customerDao.findCustomers();
     }
 }
