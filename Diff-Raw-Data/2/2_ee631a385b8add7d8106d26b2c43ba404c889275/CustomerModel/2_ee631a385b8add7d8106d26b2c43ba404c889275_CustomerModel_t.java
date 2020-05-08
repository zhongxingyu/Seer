 /**
  * 
  */
 package tk.c4se.halt.ih31.nimunimu.model;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import lombok.val;
 import tk.c4se.halt.ih31.nimunimu.dto.Customer;
 import tk.c4se.halt.ih31.nimunimu.exception.DBAccessException;
 import tk.c4se.halt.ih31.nimunimu.repository.CustomerRepository;
 
 /**
  * @author ne_Sachirou
  * 
  */
 public class CustomerModel implements DoPostModel {
 	/**
 	 * 
 	 * @param req
 	 * @param resp
 	 * @throws DBAccessException
 	 */
 	public void postRequest(HttpServletRequest req, HttpServletResponse resp)
 			throws DBAccessException {
 		val repo = new CustomerRepository();
 		Customer customer = new Customer();
 		setProperties(customer, req);
 		try {
 			repo.insert(customer);
 		} catch (DBAccessException e) {
 			e.printStackTrace();
 			throw e;
 		}
 	}
 
 	/**
 	 * 
 	 * @param req
 	 * @param resp
 	 * @throws DBAccessException
 	 */
 	public void putRequest(HttpServletRequest req, HttpServletResponse resp)
 			throws DBAccessException {
 		val idStr = req.getParameter("id");
 		val repo = new CustomerRepository();
 		Customer customer = null;
 		try {
 			customer = repo.find(idStr);
 		} catch (DBAccessException e) {
 			e.printStackTrace();
 			throw e;
 		}
 		if (customer == null) {
 			throw new DBAccessException("Customer " + idStr
 					+ " is not found in DB.");
 		}
 		setProperties(customer, req);
 		try {
 			repo.update(customer);
 		} catch (DBAccessException e1) {
 			e1.printStackTrace();
 			throw e1;
 		}
 	}
 
 	/**
 	 * 
 	 * @param req
 	 * @param resp
 	 * @throws DBAccessException
 	 */
 	public void deleteRequest(HttpServletRequest req, HttpServletResponse resp)
 			throws DBAccessException {
 		val idStr = req.getParameter("id");
 		val repo = new CustomerRepository();
 		Customer customer = null;
 		try {
 			customer = repo.find(idStr);
 		} catch (DBAccessException e) {
 			e.printStackTrace();
 			throw e;
 		}
 		if (customer == null) {
			throw new DBAccessException("Customer " + idStr
 					+ " is not found in DB.");
 		}
 		try {
 			repo.delete(customer);
 		} catch (DBAccessException e1) {
 			e1.printStackTrace();
 			return;
 		}
 	}
 
 	private void setProperties(Customer customer, HttpServletRequest req) {
 		customer.setName(req.getParameter("name"));
 		customer.setZipcode(req.getParameter("zipcode"));
 		customer.setAddress(req.getParameter("address"));
 		customer.setTel(req.getParameter("tel"));
 		customer.setFax(req.getParameter("fax"));
 		customer.setPerson(req.getParameter("person"));
 		customer.setBillingCutoffDate(req.getParameter("billing_cutoff_date"));
 		customer.setCreditLimit(req.getParameter("credit_limit"));
 	}
 }
