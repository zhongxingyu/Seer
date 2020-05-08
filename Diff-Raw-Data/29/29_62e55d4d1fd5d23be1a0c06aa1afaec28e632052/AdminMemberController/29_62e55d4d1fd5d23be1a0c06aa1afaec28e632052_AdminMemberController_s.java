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
 import tk.c4se.hal.ih31.nimunimu.dto.Member;
 import tk.c4se.halt.ih31.nimunimu.exception.DBAccessException;
 import tk.c4se.halt.ih31.nimunimu.model.MemberModel;
 import tk.c4se.halt.ih31.nimunimu.repository.MemberRepository;
 
 /**
  * @author ne_Sachirou
  */
 @WebServlet("/admin/member")
 public class AdminMemberController extends Controller {
 	private static final long serialVersionUID = 1L;
 
 	@Override
 	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
 			throws ServletException, IOException {
 		val id = req.getParameter("id");
 		@val
 		Member member;
 		try {
 			member = new MemberRepository().find(id);
 		} catch (DBAccessException e) {
 			e.printStackTrace();
 			member = null;
 		}
 		req.setAttribute("member", member);
		forward(req, resp, "admin / member", "/admin/member.jsp");
 	}
 
 	@Override
 	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
 			throws ServletException, IOException {
 		val requestType = req.getParameter("requestType");
 		val model = new MemberModel();
 		try {
 			if (requestType.equals("POST")) {
 				model.postRequest(req, resp);
 			} else if (requestType.equals("PUT")) {
 				model.putRequest(req, resp);
 			} else if (requestType.equals("DELETE")) {
 				model.deleteRequest(req, resp);
 			} else {
 				resp.sendError(502, "Unknown request type: " + requestType);
 				return;
 			}
 		} catch (DBAccessException e) {
 			e.printStackTrace();
 			resp.sendError(502, e.getMessage());
 			return;
 		}
		forward(req, resp, "admin / member", "/admin/member.jsp");
 	}
 }
