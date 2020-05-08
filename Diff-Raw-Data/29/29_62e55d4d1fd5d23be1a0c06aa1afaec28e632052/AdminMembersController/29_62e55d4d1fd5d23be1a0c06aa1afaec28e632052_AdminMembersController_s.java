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
 
 import tk.c4se.hal.ih31.nimunimu.dto.Member;
 import tk.c4se.halt.ih31.nimunimu.exception.DBAccessException;
 import tk.c4se.halt.ih31.nimunimu.repository.MemberRepository;
 
 /**
  * @author ne_Sachirou
  */
 @WebServlet("/admin/members")
 public class AdminMembersController extends Controller {
 	private static final long serialVersionUID = 1L;
 
 	@Override
 	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
 			throws ServletException, IOException {
 		List<Member> members = null;
 		try {
 			members = new MemberRepository().all();
 		} catch (DBAccessException e) {
 			e.printStackTrace();
 		}
 		req.setAttribute("members", members);
		forward(req, resp, "admin / members", "/admin/members.jsp");
 	}
 }
