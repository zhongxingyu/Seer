 /**
  * 
  */
 package tk.c4se.halt.ih31.nimunimu.controller;
 
 import java.io.IOException;
 
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import lombok.val;
 import tk.c4se.halt.ih31.nimunimu.dto.CustomerOrder;
 import tk.c4se.halt.ih31.nimunimu.dto.Member;
 import tk.c4se.halt.ih31.nimunimu.dto.MemberAuthority;
 import tk.c4se.halt.ih31.nimunimu.dto.CustomerOrderSheet;
 import tk.c4se.halt.ih31.nimunimu.exception.DBAccessException;
 import tk.c4se.halt.ih31.nimunimu.model.CustomerOrderSheetModel;
 import tk.c4se.halt.ih31.nimunimu.repository.CustomerOrderRepository;
 import tk.c4se.halt.ih31.nimunimu.repository.CustomerOrderSheetDetailRepository;
 import tk.c4se.halt.ih31.nimunimu.repository.CustomerOrderSheetRepository;
 
 /**
  * @author ne_Sachirou
  * 
  */
@WebServlet("/sales/customer_order_sheet")
 public class SalesCustomerOrdrerSheetController extends Controller {
 	private static final long serialVersionUID = 1L;
 
 	public SalesCustomerOrdrerSheetController() {
 		super();
 		title = "注文書詳細";
 		partial = "/sales/customer_order_sheet.jsp";
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
 		val idStr = req.getParameter("id");
 		CustomerOrderSheet sheet = null;
 		CustomerOrder order = null;
 		try {
 			sheet = new CustomerOrderSheetRepository().find(idStr);
 			if (sheet != null) {
 				val details = new CustomerOrderSheetDetailRepository()
 						.allByCustomerOrderSheetId(sheet.getId());
 				sheet.setCustomerOrderSheetDetails(details);
 				order = new CustomerOrderRepository()
 						.findByCustomerOrderSheetId(sheet.getId());
 				order.setCustomerOrderSheet(sheet);
 			} else {
 				order = new CustomerOrder();
 				order.setMemberId(((Member) req.getAttribute("loginMember"))
 						.getId());
 			}
 		} catch (DBAccessException e) {
 			e.printStackTrace();
 			resp.sendError(502, e.getMessage());
 			return;
 		}
 		req.setAttribute("sheet", sheet);
 		req.setAttribute("order", order);
 		forward(req, resp);
 	}
 
 	@Override
 	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
 			throws ServletException, IOException {
 		super.doPost(req, resp);
 		val model = new CustomerOrderSheetModel();
 		try {
 			processDoPostRequest(req, resp, model);
 		} catch (IOException e) {
 			resp.sendError(502, e.getMessage());
 			return;
 		}
 		forward(req, resp);
 	}
 }
