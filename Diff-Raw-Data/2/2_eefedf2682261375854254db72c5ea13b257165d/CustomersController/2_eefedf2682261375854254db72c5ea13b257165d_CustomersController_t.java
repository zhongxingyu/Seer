 /**
  * 
  */
 package tk.c4se.halt.ih31.nimunimu.controller;
 
 import java.io.IOException;
 import java.util.List;
 
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import tk.c4se.halt.ih31.nimunimu.dto.Customer;
 import tk.c4se.halt.ih31.nimunimu.dto.MemberAuthority;
 import tk.c4se.halt.ih31.nimunimu.exception.DBAccessException;
 import tk.c4se.halt.ih31.nimunimu.repository.CustomerRepository;
 
 /**
  * @author ne_Sachirou
  * 
  */
@WebServlet("/customers")
 public class CustomersController extends Controller {
 	private static final long serialVersionUID = 1L;
 
 	public CustomersController() {
 		super();
 		title = "顧客一覧";
 		partial = "/customers.jsp";
 		authorities.add(MemberAuthority.ADMIN);
 		authorities.add(MemberAuthority.SALES);
 		authorities.add(MemberAuthority.SALES_MANAGER);
 		authorities.add(MemberAuthority.STORE);
 		authorities.add(MemberAuthority.STORE_MANAGER);
 		authorities.add(MemberAuthority.ACCOUNTING);
 	}
 
 	@Override
 	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
 			throws ServletException, IOException {
 		super.doGet(req, resp);
 		List<Customer> customers = null;
 		try {
 			customers = new CustomerRepository().all();
 		} catch (DBAccessException e) {
 			e.printStackTrace();
 		}
 		req.setAttribute("customers", customers);
 		forward(req, resp);
 	}
 }
