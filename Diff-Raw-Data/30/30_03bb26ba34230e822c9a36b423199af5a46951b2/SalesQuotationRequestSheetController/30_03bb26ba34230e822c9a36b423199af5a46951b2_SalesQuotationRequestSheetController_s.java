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
 import tk.c4se.halt.ih31.nimunimu.dto.QuotationRequestSheet;
 import tk.c4se.halt.ih31.nimunimu.exception.DBAccessException;
 import tk.c4se.halt.ih31.nimunimu.model.QuotationRequestSheetModel;
 import tk.c4se.halt.ih31.nimunimu.repository.CustomerOrderRepository;
 import tk.c4se.halt.ih31.nimunimu.repository.QuotationRequestSheetDetailRepository;
 import tk.c4se.halt.ih31.nimunimu.repository.QuotationRequestSheetRepository;
 
 /**
  * @author ne_Sachirou
  * 
  */
@WebServlet("sales/quotation_request_sheet")
 public class SalesQuotationRequestSheetController extends Controller {
 	private static final long serialVersionUID = 1L;
 
 	public SalesQuotationRequestSheetController() {
 		super();
 		title = "見積依頼書詳細";
 		partial = "/sales/quotation_request_sheet.jsp";
 		authorities.add(MemberAuthority.ADMIN);
 		authorities.add(MemberAuthority.SALES);
 		authorities.add(MemberAuthority.SALES_MANAGER);
 		authorities.add(MemberAuthority.ACCOUNTING);
 	}
 
 	@Override
 	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
 			throws ServletException, IOException {
 		super.doGet(req, resp);
 		val idStr = req.getParameter("id");
 		QuotationRequestSheet sheet = null;
 		CustomerOrder order = null;
 		try {
 			sheet = new QuotationRequestSheetRepository().find(idStr);
 			if (sheet != null) {
 				val details = new QuotationRequestSheetDetailRepository()
 						.allByQuotationRequestSheetId(sheet.getId());
 				sheet.setQuotationRequestSheetDetails(details);
 				order = new CustomerOrderRepository()
 						.findByQuotationRequestSheetId(sheet.getId());
 				order.setQuotationRequestSheet(sheet);
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
 		val model = new QuotationRequestSheetModel();
 		try {
 			processDoPostRequest(req, resp, model);
 		} catch (IOException e) {
 			resp.sendError(502, e.getMessage());
 			return;
 		}
 		forward(req, resp);
 	}
 }
