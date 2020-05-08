 package com.service.user.action;
 
 import com.service.user.form.LoginForm;
 import com.service.user.form.RegisterForm;
 import com.tmp.bookmark.di.CustomerAccDataInterface;
 import com.tmp.bookmark.di.CustomerDataInterface;
 import com.tmp.bookmark.model.Customer;
 import com.tmp.bookmark.model.CustomerAcc;
 
 import org.apache.struts2.ServletActionContext;
 
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.sql.Connection;
 import java.sql.SQLException;
 
 import static com.tmp.bookmark.util.JavaUtil.generateCustomerID;
 
 /**
  * Created with IntelliJ IDEA.
  * User: alex
  * Date: 2/18/13
  * Time: 11:58 AM
  * To change this template use File | Settings | File Templates.
  */
 public class RegisterAction extends BaseAction {
 	private LoginForm loginForm;
 	private RegisterForm registerForm;
 
     private Connection con;
 
     public String execute() {
         String target = "success";
 
 		HttpServletRequest request = ServletActionContext.getRequest();
 
         Customer customer = initiateNewCustomer();
 
         if (customer == null) {
             addActionError("Registration_Duplicate", "Duplicate Email Address Dectected.");
             target = "input";
         }
 
		request.setAttribute("customer", customer);
 
         return target;
     }
 
     private Customer initiateNewCustomer() {
         con = connectionManager.getConnection();
         Customer customer = null;
 
         if (isCustomerDuplicate() == false) {
             customer = addNewCustomer();
             addNewCustomerAcc(customer);
         }
 
         return customer;
     }
 
     private void addNewCustomerAcc(Customer customer) {
         CustomerAcc customerAcc = new CustomerAcc();
 
         customerAcc.setCustomerID(customer.getCustomerID());
         customerAcc.setPassword(registerForm.getPassword());
         customerAcc.setEmail(registerForm.getPassword());
 
         CustomerAccDataInterface.getInstance().insertCustomerAcc(con, customerAcc);
 
         try {
             con.commit();
         } catch (SQLException e) {
             e.printStackTrace();
         }
     }
 
     private Customer addNewCustomer() {
         String customerID = generateCustomerID(registerForm.getFirst_name(), registerForm.getLast_name());
         if (CustomerDataInterface.getInstance().getCustomerByID(con, customerID) != null) {
             addNewCustomer();
         }
 
         Customer customer = new Customer();
         customer.setFirstName(registerForm.getFirst_name());
         customer.setLastName(registerForm.getLast_name());
         customer.setCountry(registerForm.getCountry());
         customer.setCustomerID(customerID);
 
         CustomerDataInterface.getInstance().insertCustomer(con, customer);
 
         try {
             con.commit();
         } catch (SQLException e) {
             e.printStackTrace();
         }
 
         return customer;
     }
 
 
 
     private boolean isCustomerDuplicate() {
         Boolean result = true;
         if (CustomerAccDataInterface.getInstance().getCustomerByEmail(con, registerForm.getEmail()) == null) {
             return false;
         }
         return result;
     }
 
     public void validate() {
         if (registerForm.getEmail().length() == 0) {
             addFieldError("registerForm.email", getText("error.general.email"));
         }
         if (registerForm.getPassword().length() == 0) {
             addFieldError("registerForm.password", getText("error.general.password"));
         }
         if (registerForm.getVerify_password().length() == 0) {
             addFieldError("registerForm.verify_password", getText("error.general.verify_password"));
         }
         if (registerForm.getFirst_name().length() == 0) {
             addFieldError("registerForm.first_name", getText("error.general.first_name"));
         }
         if (registerForm.getLast_name().length() == 0) {
             addFieldError("registerForm.last_name", getText("error.general.last_name"));
         }
         if (registerForm.getCountry().length() == 0) {
             addFieldError("registerForm.country", getText("error.general.country"));
         }
 
         if (!registerForm.getPassword().equals(registerForm.getVerify_password())) {
            	addActionError("Registration", "Passwords do not match.");
         }
 
     }
 
 	public RegisterForm getRegisterForm() {
 		return registerForm;
 	}
 
 	public void setRegisterForm(RegisterForm registerForm) {
 		this.registerForm = registerForm;
 	}
 }
