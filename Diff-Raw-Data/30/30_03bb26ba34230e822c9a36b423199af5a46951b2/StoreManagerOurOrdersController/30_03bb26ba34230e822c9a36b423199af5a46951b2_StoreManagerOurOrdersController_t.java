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
 
 import tk.c4se.halt.ih31.nimunimu.dto.MemberAuthority;
 import tk.c4se.halt.ih31.nimunimu.dto.OurOrder;
 import tk.c4se.halt.ih31.nimunimu.exception.DBAccessException;
 import tk.c4se.halt.ih31.nimunimu.repository.OurOrderRepository;
 
 /**
  * @author ne_Sachirou
  * 
  */
@WebServlet("/store_manager/our_orders")
 public class StoreManagerOurOrdersController extends Controller {
 	private static final long serialVersionUID = 1L;
 
 	public StoreManagerOurOrdersController() {
 		super();
 		title = "発注一覧";
 		partial = "/store_manager/our_orders.jsp";
 		authorities.add(MemberAuthority.ADMIN);
 		authorities.add(MemberAuthority.SALES_MANAGER);
 		authorities.add(MemberAuthority.STORE_MANAGER);
 		authorities.add(MemberAuthority.ACCOUNTING);
 	}
 
 	@Override
 	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
 			throws ServletException, IOException {
 		super.doGet(req, resp);
 		List<OurOrder> orders = null;
 		try {
 			orders = new OurOrderRepository().all();
 		} catch (DBAccessException e) {
 			e.printStackTrace();
 		}
 		req.setAttribute("orders", orders);
 		forward(req, resp);
 	}
 }
