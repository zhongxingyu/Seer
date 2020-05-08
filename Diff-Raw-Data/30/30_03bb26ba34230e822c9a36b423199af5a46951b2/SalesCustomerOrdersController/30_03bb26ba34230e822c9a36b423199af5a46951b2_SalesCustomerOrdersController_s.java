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
 
 import tk.c4se.halt.ih31.nimunimu.dto.CustomerOrder;
 import tk.c4se.halt.ih31.nimunimu.dto.MemberAuthority;
 import tk.c4se.halt.ih31.nimunimu.exception.DBAccessException;
 import tk.c4se.halt.ih31.nimunimu.repository.CustomerOrderRepository;
 
 /**
  * @author ne_Sachirou
  * 
  */
@WebServlet("sales/customer_orders")
 public class SalesCustomerOrdersController extends Controller {
 	private static final long serialVersionUID = 1L;
 
 	public SalesCustomerOrdersController() {
 		super();
 		title = "受注一覧";
 		partial = "/sales/customer_orders.jsp";
 		authorities.add(MemberAuthority.ADMIN);
 		authorities.add(MemberAuthority.SALES);
 		authorities.add(MemberAuthority.SALES_MANAGER);
 		authorities.add(MemberAuthority.STORE_MANAGER);
 		authorities.add(MemberAuthority.ACCOUNTING);
 	}
 
 	@Override
 	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
 			throws ServletException, IOException {
 		super.doGet(req, resp);
 		List<CustomerOrder> orders = null;
 		try {
 			orders = new CustomerOrderRepository().all();
 		} catch (DBAccessException e) {
 			e.printStackTrace();
 		}
 		req.setAttribute("orders", orders);
 		forward(req, resp);
 	}
 }
